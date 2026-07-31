// Edge Function: create-mp-preference
// Crea una preferencia de pago en Mercado Pago (Checkout Pro - fallback).
// Para MARKETPLACE SPLIT: usa el access_token del vendedor + marketplace_fee.
// El vendedor se obtiene de order_items, NO del cliente.
// Deploy: supabase functions deploy create-mp-preference

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  // HIGH-1: JWT
  const authHeader = req.headers.get('authorization') || ''
  const supabase = createClient(
    Deno.env.get('SUPABASE_URL') ?? '',
    Deno.env.get('RENDLY_SERVICE_KEY') ?? ''
  )
  let authenticatedUserId = ''
  try {
    const jwt = authHeader.replace('Bearer ', '')
    const { data: { user }, error: authError } = await supabase.auth.getUser(jwt)
    if (authError || !user) {
      return new Response(JSON.stringify({ error: 'No autorizado' }), { status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }
    authenticatedUserId = user.id
  } catch {
    return new Response(JSON.stringify({ error: 'No autorizado' }), { status: 401, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
  }

  try {
    const { order_id, items, payer_email, external_reference } = await req.json()

    if (!order_id || !items || items.length === 0) {
      return new Response(JSON.stringify({ error: 'order_id e items son requeridos' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // CRIT-5: Obtener seller_id de la orden (no confiar en el cliente)
    const { data: orderItems } = await supabase
      .from('order_items')
      .select('seller_id')
      .eq('order_id', order_id)
      .limit(1)

    const sellerId = orderItems?.[0]?.seller_id
    if (!sellerId) {
      return new Response(JSON.stringify({ error: 'Orden no encontrada o sin vendedor' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // Verificar que el comprador es el dueño de la orden
    const { data: orderData } = await supabase
      .from('orders')
      .select('buyer_id')
      .eq('id', order_id)
      .single()

    if (!orderData || orderData.buyer_id !== authenticatedUserId) {
      return new Response(JSON.stringify({ error: 'No autorizado para esta orden' }), { status: 403, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // Bloquear auto-compra
    if (orderData.buyer_id === sellerId) {
      return new Response(JSON.stringify({ error: 'No puedes comprar tu propio producto' }), { status: 403, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // Obtener access_token del vendedor + platform sponsor_id
    const { data: connection } = await supabase
      .from('mercadopago_connections')
      .select('access_token_encrypted')
      .eq('user_id', sellerId)
      .eq('conexion_estado', 'activa')
      .single()

    if (!connection) {
      return new Response(JSON.stringify({ error: 'El vendedor no tiene Mercado Pago conectado.' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    const sellerAccessToken = await decryptToken(connection.access_token_encrypted, Deno.env.get('TOKEN_ENCRYPTION_KEY') || '')
    if (!sellerAccessToken) {
      return new Response(JSON.stringify({ error: 'Error al descifrar token' }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // CRIT-3: sponsor_id = plataforma
    const { data: mpSettings } = await supabase
      .from('platform_settings')
      .select('value')
      .eq('key', 'marketplace_user_id')
      .single()

    const platformMpUserId = mpSettings?.value?.mp_user_id || Deno.env.get('MERCADOPAGO_MARKETPLACE_USER_ID') || ''

    // MED-1: Comisión desde platform_settings
    const { data: commSettings } = await supabase
      .from('platform_settings')
      .select('value')
      .eq('key', 'commission_percentage')
      .single()

    const commissionPct = commSettings?.value?.percentage ?? 10.0
    const minFee = commSettings?.value?.min_fee ?? 5.0
    const maxFee = commSettings?.value?.max_fee ?? 5000.0
    const total = items.reduce((sum: number, item: { unit_price: number; quantity: number }) => sum + item.unit_price * item.quantity, 0)
    let marketplaceFee = total * (commissionPct / 100.0)
    marketplaceFee = Math.max(Math.min(marketplaceFee, maxFee), minFee)

    const preferenceData = {
      items: items.map((item: { id: string; title: string; quantity: number; unit_price: number; currency_id?: string; picture_url?: string; description?: string }) => ({
        id: item.id,
        title: item.title,
        quantity: item.quantity,
        unit_price: item.unit_price,
        currency_id: item.currency_id || 'UYU',
        picture_url: item.picture_url,
        description: item.description,
      })),
      payer: { email: payer_email || 'comprador@mercora.app' },
      external_reference: external_reference || order_id,
      back_urls: {
        success: `mercora://payment/success?order_id=${order_id}`,
        failure: `mercora://payment/failure?order_id=${order_id}`,
        pending: `mercora://payment/pending?order_id=${order_id}`,
      },
      auto_return: 'approved',
      notification_url: `${Deno.env.get('SUPABASE_URL')}/functions/v1/mp-webhook`,
      metadata: {
        order_id,
        seller_id: sellerId,
        platform: 'mercora_android',
        integration_type: 'marketplace_checkout_pro',
      },
      payment_methods: { installments: 12 },
      marketplace_fee: marketplaceFee,
      sponsor_id: platformMpUserId,
    }

    const mpResponse = await fetch('https://api.mercadopago.com/checkout/preferences', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${sellerAccessToken}`,
      },
      body: JSON.stringify(preferenceData),
    })

    if (!mpResponse.ok) {
      const errorData = await mpResponse.json()
      return new Response(JSON.stringify({ error: `Error de Mercado Pago: ${JSON.stringify(errorData)}` }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    const preference = await mpResponse.json()

    await supabase
      .from('payments')
      .update({
        mp_preference_id: preference.id,
        updated_at: new Date().toISOString(),
      })
      .eq('order_id', order_id)

    const USE_SANDBOX = Deno.env.get('MERCADOPAGO_SANDBOX') === 'true'

    return new Response(JSON.stringify({
      preference_id: preference.id,
      init_point: USE_SANDBOX ? preference.sandbox_init_point : preference.init_point,
      marketplace_fee: marketplaceFee,
      sponsor_id: platformMpUserId,
    }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })

  } catch (error) {
    console.error('Error:', error)
    return new Response(JSON.stringify({ error: error.message }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
  }
})

// ── AES-256-GCM real con key derivation segura ──
async function deriveKey(keyInput: string): Promise<CryptoKey> {
  let keyBytes: Uint8Array
  if (/^[0-9a-f]{64}$/i.test(keyInput)) {
    keyBytes = new Uint8Array(32)
    for (let i = 0; i < 32; i++) keyBytes[i] = parseInt(keyInput.substr(i * 2, 2), 16)
  } else {
    keyBytes = new Uint8Array(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(keyInput)))
  }
  return crypto.subtle.importKey('raw', keyBytes, { name: 'AES-GCM' }, false, ['decrypt'])
}

async function decryptToken(encryptedBase64: string, keyInput: string): Promise<string> {
  try {
    const encrypted = Uint8Array.from(atob(encryptedBase64), c => c.charCodeAt(0))
    const iv = encrypted.slice(0, 12)
    const ciphertextAndTag = encrypted.slice(12)
    try {
      const key = await deriveKey(keyInput)
      const decrypted = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, ciphertextAndTag)
      return new TextDecoder().decode(decrypted)
    } catch {
      const oldKeyBytes = new TextEncoder().encode(keyInput.padEnd(32, 'x').slice(0, 32))
      const oldKey = await crypto.subtle.importKey('raw', oldKeyBytes, { name: 'AES-GCM' }, false, ['decrypt'])
      const decrypted = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, oldKey, ciphertextAndTag)
      return new TextDecoder().decode(decrypted)
    }
  } catch { return '' }
}
