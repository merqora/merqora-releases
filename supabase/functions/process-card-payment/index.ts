// Supabase Edge Function para procesar pagos con tarjeta tokenizada (Checkout API)
// Deploy: supabase functions deploy process-card-payment --no-verify-jwt

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

// Tipos para la request
interface ProcessPaymentRequest {
  token: string                    // Token de la tarjeta (generado en el cliente)
  payment_method_id: string        // Ej: "visa", "master", "amex"
  transaction_amount: number       // Monto total
  installments: number             // Número de cuotas
  issuer_id?: number               // ID del emisor (banco)
  payer_email: string              // Email del pagador
  payer_identification?: {
    type: string                   // Ej: "CI" para Uruguay
    number: string
  }
  order_id: string                 // ID de la orden en nuestra DB
  description?: string             // Descripción del pago
}

// Tipos para la respuesta de MP
interface MercadoPagoPaymentResponse {
  id: number
  status: string                   // approved, pending, rejected, in_process
  status_detail: string            // Detalle del estado
  payment_method_id: string
  payment_type_id: string
  installments: number
  transaction_amount: number
  currency_id: string
  date_created: string
  date_approved?: string
  external_reference?: string
  description?: string
  card?: {
    first_six_digits: string
    last_four_digits: string
    expiration_month: number
    expiration_year: number
    cardholder: {
      name: string
    }
  }
  payer?: {
    email: string
    identification?: {
      type: string
      number: string
    }
  }
  fee_details?: Array<{
    type: string
    amount: number
    fee_payer: string
  }>
  transaction_details?: {
    net_received_amount: number
    total_paid_amount: number
    installment_amount: number
  }
}

serve(async (req) => {
  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // Obtener Access Token de Mercado Pago desde variables de entorno
    const MP_ACCESS_TOKEN = Deno.env.get('MERCADOPAGO_ACCESS_TOKEN')
    if (!MP_ACCESS_TOKEN) {
      throw new Error('MERCADOPAGO_ACCESS_TOKEN no configurado')
    }

    // Obtener datos de la request
    const requestData: ProcessPaymentRequest = await req.json()

    const {
      token,
      payment_method_id,
      transaction_amount,
      installments,
      issuer_id,
      payer_email,
      payer_identification,
      order_id,
      description
    } = requestData

    // Validaciones
    if (!token) {
      return new Response(
        JSON.stringify({ error: 'Token de tarjeta requerido' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    if (!payment_method_id) {
      return new Response(
        JSON.stringify({ error: 'payment_method_id requerido' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    if (!transaction_amount || transaction_amount <= 0) {
      return new Response(
        JSON.stringify({ error: 'transaction_amount inválido' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    if (!payer_email) {
      return new Response(
        JSON.stringify({ error: 'payer_email requerido' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    if (!order_id) {
      return new Response(
        JSON.stringify({ error: 'order_id requerido' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Construir payload para Mercado Pago Payments API
    const paymentData: Record<string, unknown> = {
      token: token,
      payment_method_id: payment_method_id,
      transaction_amount: transaction_amount,
      installments: installments || 1,
      payer: {
        email: payer_email,
      },
      external_reference: order_id,
      description: description || `Pedido #${order_id}`,
      statement_descriptor: 'RENDLY',  // Aparece en el resumen de la tarjeta
      metadata: {
        order_id: order_id,
        platform: 'rendly_android',
        integration_type: 'checkout_api',
      },
      // Configuración de notificaciones
      notification_url: `${Deno.env.get('SUPABASE_URL')}/functions/v1/mp-webhook`,
      // Configuración adicional
      binary_mode: false,  // false permite pagos pendientes
      capture: true,       // Captura automática
    }

    // Agregar issuer_id si está presente
    if (issuer_id) {
      paymentData.issuer_id = issuer_id
    }

    // Agregar identificación del pagador si está presente
    if (payer_identification?.type && payer_identification?.number) {
      (paymentData.payer as Record<string, unknown>).identification = {
        type: payer_identification.type,
        number: payer_identification.number,
      }
    }

    console.log('Procesando pago para orden:', order_id)
    console.log('Payment data:', JSON.stringify({ ...paymentData, token: '***' }))

    // Llamar a la API de Mercado Pago para procesar el pago
    const mpResponse = await fetch('https://api.mercadopago.com/v1/payments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${MP_ACCESS_TOKEN}`,
        'X-Idempotency-Key': `${order_id}-${Date.now()}`,  // Evitar pagos duplicados
      },
      body: JSON.stringify(paymentData),
    })

    const paymentResponse: MercadoPagoPaymentResponse = await mpResponse.json()

    if (!mpResponse.ok) {
      console.error('Error de Mercado Pago:', paymentResponse)
      
      // Mapear errores comunes de MP
      const errorMessage = mapMercadoPagoError(paymentResponse)
      
      return new Response(
        JSON.stringify({ 
          error: errorMessage,
          mp_error: paymentResponse,
        }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    console.log('Pago procesado:', paymentResponse.id, 'Estado:', paymentResponse.status)

    // Guardar resultado del pago en Supabase
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    // Actualizar la orden con el resultado del pago
    const paymentStatus = mapPaymentStatus(paymentResponse.status)
    
    const { error: updateError } = await supabase
      .from('orders')
      .update({
        payment_status: paymentStatus,
        mp_payment_id: paymentResponse.id.toString(),
        payment_method: payment_method_id,
        installments: paymentResponse.installments,
        updated_at: new Date().toISOString(),
      })
      .eq('id', order_id)

    if (updateError) {
      console.error('Error actualizando orden:', updateError)
    }

    // También guardar en la tabla de payments si existe
    await supabase
      .from('payments')
      .upsert({
        order_id: order_id,
        mp_payment_id: paymentResponse.id.toString(),
        status: paymentResponse.status,
        status_detail: paymentResponse.status_detail,
        payment_method_id: payment_method_id,
        payment_type_id: paymentResponse.payment_type_id,
        transaction_amount: paymentResponse.transaction_amount,
        installments: paymentResponse.installments,
        card_first_six: paymentResponse.card?.first_six_digits,
        card_last_four: paymentResponse.card?.last_four_digits,
        payer_email: payer_email,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
      }, { onConflict: 'order_id' })

    // Retornar respuesta exitosa
    return new Response(
      JSON.stringify({
        id: paymentResponse.id,
        status: paymentResponse.status,
        status_detail: paymentResponse.status_detail,
        payment_method_id: paymentResponse.payment_method_id,
        payment_type_id: paymentResponse.payment_type_id,
        installments: paymentResponse.installments,
        transaction_amount: paymentResponse.transaction_amount,
        currency_id: paymentResponse.currency_id,
        date_created: paymentResponse.date_created,
        date_approved: paymentResponse.date_approved,
        card: paymentResponse.card ? {
          first_six_digits: paymentResponse.card.first_six_digits,
          last_four_digits: paymentResponse.card.last_four_digits,
        } : null,
        transaction_details: paymentResponse.transaction_details,
      }),
      { 
        status: 200, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    )

  } catch (error) {
    console.error('Error procesando pago:', error)
    return new Response(
      JSON.stringify({ error: error.message || 'Error interno del servidor' }),
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  }
})

// Mapear estado de pago de MP a nuestro sistema
function mapPaymentStatus(mpStatus: string): string {
  switch (mpStatus) {
    case 'approved':
      return 'approved'
    case 'pending':
    case 'in_process':
    case 'authorized':
      return 'pending'
    case 'rejected':
    case 'cancelled':
    case 'refunded':
    case 'charged_back':
      return 'rejected'
    default:
      return 'pending'
  }
}

// Mapear errores de Mercado Pago a mensajes amigables
function mapMercadoPagoError(response: unknown): string {
  const errorResponse = response as { cause?: Array<{ code: string; description: string }> }
  
  if (errorResponse.cause && errorResponse.cause.length > 0) {
    const cause = errorResponse.cause[0]
    
    switch (cause.code) {
      case '2001':
      case '2002':
      case '2003':
      case '2004':
        return 'El pago fue rechazado por la tarjeta. Verificá los datos o probá con otra tarjeta.'
      case '3001':
        return 'Tu tarjeta no tiene fondos suficientes.'
      case '3002':
        return 'Tu tarjeta no está habilitada para compras online.'
      case '3003':
        return 'El pago excede el límite de tu tarjeta.'
      case '3004':
        return 'El código de seguridad es incorrecto.'
      case '4001':
        return 'La tarjeta está vencida.'
      case '4002':
        return 'Tu tarjeta fue reportada como robada o perdida.'
      case 'cc_rejected_bad_filled_card_number':
        return 'Número de tarjeta incorrecto.'
      case 'cc_rejected_bad_filled_date':
        return 'Fecha de vencimiento incorrecta.'
      case 'cc_rejected_bad_filled_other':
        return 'Datos de tarjeta incorrectos.'
      case 'cc_rejected_bad_filled_security_code':
        return 'Código de seguridad incorrecto.'
      case 'cc_rejected_blacklist':
        return 'Tu tarjeta no puede ser utilizada.'
      case 'cc_rejected_call_for_authorize':
        return 'Debés autorizar el pago llamando a tu banco.'
      case 'cc_rejected_card_disabled':
        return 'Tu tarjeta está deshabilitada. Contactá a tu banco.'
      case 'cc_rejected_duplicated_payment':
        return 'Ya procesaste un pago con este monto recientemente.'
      case 'cc_rejected_high_risk':
        return 'El pago fue rechazado por razones de seguridad.'
      case 'cc_rejected_insufficient_amount':
        return 'Fondos insuficientes.'
      case 'cc_rejected_invalid_installments':
        return 'La cantidad de cuotas no está disponible.'
      case 'cc_rejected_max_attempts':
        return 'Alcanzaste el límite de intentos. Probá con otra tarjeta.'
      default:
        return cause.description || 'El pago fue rechazado. Intentá con otra tarjeta.'
    }
  }
  
  return 'Error procesando el pago. Por favor intentá nuevamente.'
}
