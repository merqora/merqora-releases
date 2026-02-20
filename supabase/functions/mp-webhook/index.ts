// Supabase Edge Function para recibir webhooks/IPN de Mercado Pago
// Deploy: supabase functions deploy mp-webhook --no-verify-jwt

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

// Tipos de notificación de Mercado Pago
interface MPNotification {
  id: number
  live_mode: boolean
  type: string // 'payment', 'plan', 'subscription', 'invoice'
  date_created: string
  user_id: number
  api_version: string
  action: string
  data: {
    id: string
  }
}

// Información del pago de Mercado Pago
interface MPPayment {
  id: number
  status: string // 'approved', 'pending', 'rejected', 'cancelled', 'refunded'
  status_detail: string
  external_reference: string // order_id
  payment_method_id: string
  payment_type_id: string
  installments: number
  transaction_amount: number
  currency_id: string
  date_approved: string | null
  date_created: string
  payer: {
    id: number
    email: string
  }
  metadata: {
    order_id?: string
  }
}

serve(async (req) => {
  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const MP_ACCESS_TOKEN = Deno.env.get('MERCADOPAGO_ACCESS_TOKEN')
    if (!MP_ACCESS_TOKEN) {
      throw new Error('MERCADOPAGO_ACCESS_TOKEN no configurado')
    }

    // Mercado Pago puede enviar notificaciones de diferentes formas
    const url = new URL(req.url)
    const topic = url.searchParams.get('topic') || url.searchParams.get('type')
    const paymentId = url.searchParams.get('id') || url.searchParams.get('data.id')

    let notification: MPNotification | null = null
    
    // Intentar parsear el body si existe
    try {
      const body = await req.text()
      if (body) {
        notification = JSON.parse(body)
      }
    } catch (e) {
      // Body vacío o no JSON, usar query params
    }

    // Determinar el ID del pago
    const effectivePaymentId = notification?.data?.id || paymentId
    const effectiveTopic = notification?.type || topic

    console.log(`Webhook recibido - Topic: ${effectiveTopic}, PaymentId: ${effectivePaymentId}`)

    // Solo procesar notificaciones de pago
    if (effectiveTopic !== 'payment' || !effectivePaymentId) {
      console.log('Notificación ignorada (no es de pago o sin ID)')
      return new Response('OK', { status: 200, headers: corsHeaders })
    }

    // Obtener detalles del pago desde Mercado Pago
    const paymentResponse = await fetch(
      `https://api.mercadopago.com/v1/payments/${effectivePaymentId}`,
      {
        headers: {
          'Authorization': `Bearer ${MP_ACCESS_TOKEN}`,
        },
      }
    )

    if (!paymentResponse.ok) {
      console.error('Error obteniendo pago:', await paymentResponse.text())
      throw new Error('Error obteniendo información del pago')
    }

    const payment: MPPayment = await paymentResponse.json()
    console.log(`Pago ${payment.id}: status=${payment.status}, order=${payment.external_reference}`)

    // Conectar a Supabase
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    // Obtener order_id del external_reference o metadata
    const orderId = payment.external_reference || payment.metadata?.order_id
    if (!orderId) {
      console.error('No se encontró order_id en el pago')
      return new Response('OK', { status: 200, headers: corsHeaders })
    }

    // Mapear status de MP a nuestro status
    const statusMapping: Record<string, string> = {
      'approved': 'approved',
      'pending': 'pending',
      'in_process': 'in_process',
      'rejected': 'rejected',
      'cancelled': 'cancelled',
      'refunded': 'refunded',
      'charged_back': 'charged_back',
    }

    const paymentStatus = statusMapping[payment.status] || payment.status

    // Actualizar el pago en nuestra base de datos
    const { error: paymentError } = await supabase
      .from('payments')
      .update({
        mp_payment_id: payment.id.toString(),
        status: paymentStatus,
        status_detail: payment.status_detail,
        payment_method_id: payment.payment_method_id,
        payment_type_id: payment.payment_type_id,
        installments: payment.installments,
        paid_at: payment.date_approved,
        updated_at: new Date().toISOString(),
      })
      .eq('order_id', orderId)

    if (paymentError) {
      console.error('Error actualizando payment:', paymentError)
    }

    // Actualizar el estado de la orden si el pago fue aprobado
    if (payment.status === 'approved') {
      const { error: orderError } = await supabase
        .from('orders')
        .update({
          status: 'paid',
          updated_at: new Date().toISOString(),
        })
        .eq('id', orderId)

      if (orderError) {
        console.error('Error actualizando order:', orderError)
      }

      // Insertar en el historial de estado
      await supabase
        .from('order_status_history')
        .insert({
          order_id: orderId,
          status: 'paid',
          notes: `Pago aprobado via Mercado Pago. ID: ${payment.id}`,
        })

      // Actualizar stats del vendedor
      const { data: orderItems } = await supabase
        .from('order_items')
        .select('seller_id, subtotal')
        .eq('order_id', orderId)

      if (orderItems) {
        for (const item of orderItems) {
          // Incrementar ventas del vendedor
          await supabase.rpc('increment_seller_sales', {
            p_seller_id: item.seller_id,
            p_amount: item.subtotal,
          })
        }
      }

      console.log(`✅ Orden ${orderId} marcada como pagada`)

    } else if (payment.status === 'rejected' || payment.status === 'cancelled') {
      // Marcar orden como fallida
      await supabase
        .from('orders')
        .update({
          status: 'payment_failed',
          updated_at: new Date().toISOString(),
        })
        .eq('id', orderId)

      await supabase
        .from('order_status_history')
        .insert({
          order_id: orderId,
          status: 'payment_failed',
          notes: `Pago ${payment.status}: ${payment.status_detail}`,
        })

      console.log(`❌ Orden ${orderId} marcada como pago fallido`)
    }

    return new Response('OK', { status: 200, headers: corsHeaders })

  } catch (error) {
    console.error('Error en webhook:', error)
    // Siempre devolver 200 para que MP no reintente
    return new Response('OK', { status: 200, headers: corsHeaders })
  }
})
