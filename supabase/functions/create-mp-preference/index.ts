// Supabase Edge Function para crear preferencias de pago en Mercado Pago
// Deploy: supabase functions deploy create-mp-preference --no-verify-jwt

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

// Tipos para Mercado Pago
interface OrderItem {
  id: string
  title: string
  quantity: number
  unit_price: number
  currency_id: string
  picture_url?: string
  description?: string
}

interface CreatePreferenceRequest {
  order_id: string
  items: OrderItem[]
  payer_email?: string
  external_reference?: string
}

interface MercadoPagoPreference {
  id: string
  init_point: string
  sandbox_init_point: string
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
    const { order_id, items, payer_email, external_reference }: CreatePreferenceRequest = await req.json()

    if (!order_id || !items || items.length === 0) {
      return new Response(
        JSON.stringify({ error: 'order_id e items son requeridos' }),
        { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Obtener URL base para deep links (configurable)
    const APP_SCHEME = Deno.env.get('APP_DEEP_LINK_SCHEME') || 'rendly'
    const SUPABASE_URL = Deno.env.get('SUPABASE_URL')

    // Crear preferencia en Mercado Pago
    const preferenceData = {
      items: items.map(item => ({
        id: item.id,
        title: item.title,
        quantity: item.quantity,
        unit_price: item.unit_price,
        currency_id: item.currency_id || 'UYU', // Pesos uruguayos
        picture_url: item.picture_url,
        description: item.description,
      })),
      payer: payer_email ? { email: payer_email } : undefined,
      external_reference: external_reference || order_id,
      // URLs de retorno - Deep Links para la app Android
      back_urls: {
        success: `${APP_SCHEME}://payment/success?order_id=${order_id}`,
        failure: `${APP_SCHEME}://payment/failure?order_id=${order_id}`,
        pending: `${APP_SCHEME}://payment/pending?order_id=${order_id}`,
      },
      auto_return: 'approved', // Retorno automático solo si el pago es aprobado
      // Webhook para notificaciones de pago (IPN)
      notification_url: `${SUPABASE_URL}/functions/v1/mp-webhook`,
      // Metadata adicional
      metadata: {
        order_id: order_id,
        platform: 'rendly_android',
      },
      // Configuración de pagos
      payment_methods: {
        // Excluir métodos si es necesario
        // excluded_payment_methods: [{ id: 'amex' }],
        // excluded_payment_types: [{ id: 'atm' }],
        installments: 12, // Máximo de cuotas
      },
      // Expiración de la preferencia (24 horas)
      expires: true,
      expiration_date_from: new Date().toISOString(),
      expiration_date_to: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    }

    // Llamar a la API de Mercado Pago
    const mpResponse = await fetch('https://api.mercadopago.com/checkout/preferences', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${MP_ACCESS_TOKEN}`,
      },
      body: JSON.stringify(preferenceData),
    })

    if (!mpResponse.ok) {
      const errorData = await mpResponse.json()
      console.error('Error de Mercado Pago:', errorData)
      throw new Error(`Error de Mercado Pago: ${JSON.stringify(errorData)}`)
    }

    const preference: MercadoPagoPreference = await mpResponse.json()

    // Guardar referencia de la preferencia en Supabase (opcional pero recomendado)
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    await supabase
      .from('payments')
      .update({
        mp_preference_id: preference.id,
        updated_at: new Date().toISOString(),
      })
      .eq('order_id', order_id)

    // Determinar si usar sandbox o producción
    const USE_SANDBOX = Deno.env.get('MERCADOPAGO_SANDBOX') === 'true'

    return new Response(
      JSON.stringify({
        preference_id: preference.id,
        init_point: USE_SANDBOX ? preference.sandbox_init_point : preference.init_point,
        sandbox_init_point: preference.sandbox_init_point,
        production_init_point: preference.init_point,
      }),
      { 
        status: 200, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    )

  } catch (error) {
    console.error('Error:', error)
    return new Response(
      JSON.stringify({ error: error.message }),
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  }
})
