// Edge Function: process-card-payment
// Procesa pagos con tarjeta tokenizada usando Mercado Pago Marketplace + Split Payments.
// Usa el access_token del VENDEDOR (obtenido del order_items, NO del cliente).
// MP divide automáticamente: vendedor recibe (total - comisión), plataforma recibe comisión.
// Vinzay NUNCA custodia el dinero real.
// Deploy: supabase functions deploy process-card-payment

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

interface ProcessPaymentRequest {
  token: string
  payment_method_id: string
  transaction_amount: number
  installments: number
  issuer_id?: number
  payer_email: string
  payer_identification?: { type: string; number: string }
  order_id: string
  description?: string
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  // ── HIGH-1: JWT Authentication ──
  const authHeader = req.headers.get('authorization') || ''
  const supabase = createClient(
    Deno.env.get('SUPABASE_URL') ?? '',
    Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
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
    const requestData: ProcessPaymentRequest = await req.json()
    const { token, payment_method_id, transaction_amount, installments, issuer_id, payer_email, payer_identification, order_id, description } = requestData

    if (!token) return new Response(JSON.stringify({ error: 'Token de tarjeta requerido' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    if (!transaction_amount || transaction_amount <= 0) return new Response(JSON.stringify({ error: 'transaction_amount inválido' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    if (!order_id) return new Response(JSON.stringify({ error: 'order_id requerido' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })

    // ── CRIT-5: Obtener seller_id REAL desde order_items (no confiar en el cliente) ──
    const { data: orderItems, error: itemsError } = await supabase
      .from('order_items')
      .select('seller_id, total_price, post_id, quantity')
      .eq('order_id', order_id)

    if (itemsError || !orderItems || orderItems.length === 0) {
      return new Response(JSON.stringify({ error: 'Orden no encontrada o sin items' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // Verificar que el comprador (del JWT) sea el buyer de la orden
    const { data: orderData } = await supabase
      .from('orders')
      .select('buyer_id, status')
      .eq('id', order_id)
      .single()

    if (!orderData) {
      return new Response(JSON.stringify({ error: 'Orden no encontrada' }), { status: 404, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }
    if (orderData.buyer_id !== authenticatedUserId) {
      return new Response(JSON.stringify({ error: 'Esta orden no te pertenece' }), { status: 403, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }
    // ── MED-2: Bloquear auto-compra ──
    for (const item of orderItems) {
      if (item.seller_id === authenticatedUserId) {
        return new Response(JSON.stringify({ error: 'No puedes comprar tus propios productos' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
      }
    }
    // ── CRIT-6: Verificar si ya existe pago aprobado para esta orden ──
    if (orderData.status === 'paid' || orderData.status === 'refunded') {
      return new Response(JSON.stringify({ error: 'Esta orden ya fue pagada' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // Solo usar el primer seller_id (Split Payments requiere un sponsor por pago)
    const sellerId = orderItems[0].seller_id
    if (!sellerId) {
      return new Response(JSON.stringify({ error: 'La orden no tiene vendedor asignado' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // ── CRIT-6: Idempotency key DETERMINISTA ──
    const idempotencyKey = `payment-${order_id}-${sellerId}`

    // Verificar idempotencia en DB
    const { data: existingPay } = await supabase
      .from('payments')
      .select('mp_payment_id, status')
      .eq('order_id', order_id)
      .maybeSingle()

    if (existingPay && existingPay.status === 'approved') {
      return new Response(JSON.stringify({ error: 'Pago ya procesado para esta orden', existing_payment_id: existingPay.mp_payment_id }), { status: 409, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // ── CRIT-3: Obtener sponsor_id de la plataforma (NO del vendedor) ──
    const { data: mpSettings } = await supabase
      .from('platform_settings')
      .select('value')
      .eq('key', 'marketplace_user_id')
      .single()

    const platformMpUserId = mpSettings?.value?.mp_user_id || Deno.env.get('MERCADOPAGO_MARKETPLACE_USER_ID') || ''
    if (!platformMpUserId) {
      console.error('Marketplace MP User ID no configurado en platform_settings ni env vars')
    }

    // Obtener access_token del vendedor
    const { data: connection, error: connError } = await supabase
      .from('mercadopago_connections')
      .select('mercadopago_user_id, access_token_encrypted')
      .eq('user_id', sellerId)
      .eq('conexion_estado', 'activa')
      .single()

    if (connError || !connection) {
      return new Response(JSON.stringify({ error: 'El vendedor no tiene Mercado Pago conectado.' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // ── CRIT-4: Descifrar token (AES-GCM) ──
    const sellerAccessToken = await decryptToken(connection.access_token_encrypted, Deno.env.get('TOKEN_ENCRYPTION_KEY') || '')
    if (!sellerAccessToken) {
      return new Response(JSON.stringify({ error: 'Error al descifrar token del vendedor' }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // ── MED-1: Leer comisión desde platform_settings ──
    const { data: commSettings } = await supabase
      .from('platform_settings')
      .select('value')
      .eq('key', 'commission_percentage')
      .single()

    const commissionPct = commSettings?.value?.percentage ?? 10.0
    const minFee = commSettings?.value?.min_fee ?? 5.0
    const maxFee = commSettings?.value?.max_fee ?? 5000.0
    let commissionAmount = transaction_amount * (commissionPct / 100.0)
    commissionAmount = Math.max(Math.min(commissionAmount, maxFee), minFee)
    const sellerNetAmount = transaction_amount - commissionAmount

    // ── MED-5: Reservar stock antes del pago ──
    for (const item of orderItems) {
      if (item.post_id) {
        const { error: stockErr } = await supabase.rpc('reserve_stock', {
          p_post_id: item.post_id,
          p_quantity: item.quantity,
        })
        if (stockErr) {
          console.error(`Stock insuficiente para post ${item.post_id}:`, stockErr)
          return new Response(JSON.stringify({ error: `Stock insuficiente para uno de los productos` }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
        }
      }
    }

    // Construir payload con application_fee y sponsor_id = PLATAFORMA
    const paymentData: Record<string, unknown> = {
      token,
      payment_method_id,
      transaction_amount,
      installments: installments || 1,
      payer: { email: payer_email },
      external_reference: order_id,
      description: description || `Pedido #${order_id}`,
      statement_descriptor: 'VINZAY',
      metadata: {
        order_id,
        seller_id: sellerId,
        platform: 'vinzay_android',
        integration_type: 'marketplace_split',
      },
      notification_url: `${Deno.env.get('SUPABASE_URL')}/functions/v1/mp-webhook`,
      binary_mode: true,
      capture: true,
      application_fee: commissionAmount,
      sponsor_id: platformMpUserId,
    }

    if (issuer_id) paymentData.issuer_id = issuer_id
    if (payer_identification?.type && payer_identification?.number) {
      (paymentData.payer as Record<string, unknown>).identification = {
        type: payer_identification.type,
        number: payer_identification.number,
      }
    }

    // Llamar a MP usando el token del VENDEDOR
    const mpResponse = await fetch('https://api.mercadopago.com/v1/payments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${sellerAccessToken}`,
        'X-Idempotency-Key': idempotencyKey,
      },
      body: JSON.stringify(paymentData),
    })

    const paymentResponse = await mpResponse.json()

    if (!mpResponse.ok) {
      // Liberar stock si falla el pago
      for (const item of orderItems) {
        if (item.post_id) {
          await supabase.rpc('release_stock', { p_post_id: item.post_id, p_quantity: item.quantity }).catch(() => {})
        }
      }
      const errorMessage = mapMercadoPagoError(paymentResponse)
      return new Response(JSON.stringify({ error: errorMessage, mp_error: paymentResponse }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    // Guardar resultado - usar payment_status como fuente de verdad (MED-6)
    await supabase
      .from('orders')
      .update({
        payment_status: paymentResponse.status,
        mp_payment_id: paymentResponse.id.toString(),
        payment_method: payment_method_id,
        installments: paymentResponse.installments,
        mp_application_fee: commissionAmount,
        mp_sponsor_id: platformMpUserId,
        split_info: {
          total: transaction_amount,
          commission: commissionAmount,
          commission_pct: commissionPct,
          seller_net: sellerNetAmount,
          sponsor_id: platformMpUserId,
        },
        commission_percentage: commissionPct,
        commission_amount: commissionAmount,
        seller_net_amount: sellerNetAmount,
        idempotency_key: idempotencyKey,
        updated_at: new Date().toISOString(),
        paid_at: paymentResponse.date_approved || null,
      })
      .eq('id', order_id)

    const feeDetails = paymentResponse.fee_details || []
    const feeDetailsJson = feeDetails.length > 0 ? feeDetails : null
    const netReceived = paymentResponse.transaction_details?.net_received_amount || transaction_amount - commissionAmount

    await supabase
      .from('payments')
      .upsert({
        order_id,
        mp_payment_id: paymentResponse.id.toString(),
        status: paymentResponse.status,
        status_detail: paymentResponse.status_detail,
        payment_method_id,
        payment_type_id: paymentResponse.payment_type_id,
        transaction_amount: paymentResponse.transaction_amount,
        installments: paymentResponse.installments,
        card_first_six: paymentResponse.card?.first_six_digits,
        card_last_four: paymentResponse.card?.last_four_digits,
        payer_email,
        mp_fee_details: feeDetailsJson,
        mp_net_received_amount: netReceived,
        mp_commission_amount: commissionAmount,
        mp_financial_status: paymentResponse.status === 'approved' ? 'settled' : paymentResponse.status,
        payment_method_type: paymentResponse.payment_type_id,
        installments_count: paymentResponse.installments,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
      }, { onConflict: 'order_id' })

    if (paymentResponse.status === 'approved') {
      // Confirmar stock
      for (const item of orderItems) {
        if (item.post_id) {
          await supabase.rpc('confirm_stock', { p_post_id: item.post_id, p_quantity: item.quantity }).catch(() => {})
        }
      }

      // CRIT-1: Stats se acreditan vía RPC idempotente
      await supabase.rpc('credit_seller_stats_idempotent', { p_order_id: order_id }).catch(e => {
        console.error('Error credit_seller_stats_idempotent:', e)
      })

      // CRIT-2: Transición de estado unificada via RPC
      await supabase.rpc('transition_order_status', {
        p_order_id: order_id,
        p_new_status: 'paid',
        p_changed_by: 'system',
        p_notes: `Pago aprobado con Split Payments. Total: $${transaction_amount}, Comisión: $${commissionAmount}, Vendedor recibe: $${sellerNetAmount}. MP Payment ID: ${paymentResponse.id}`,
      }).catch(e => console.error('Error transition_order_status:', e))
    }

    return new Response(JSON.stringify({
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
      split_info: { total: transaction_amount, commission: commissionAmount, commission_pct: commissionPct, seller_net: sellerNetAmount, sponsor_id: platformMpUserId },
      card: paymentResponse.card ? { first_six_digits: paymentResponse.card.first_six_digits, last_four_digits: paymentResponse.card.last_four_digits } : null,
      transaction_details: paymentResponse.transaction_details,
    }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })

  } catch (error) {
    console.error('Error procesando pago con split:', error)
    return new Response(JSON.stringify({ error: error.message || 'Error interno del servidor' }), { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
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

function mapMercadoPagoError(response: unknown): string {
  const errorResponse = response as { cause?: Array<{ code: string; description: string }> }
  if (errorResponse.cause && errorResponse.cause.length > 0) {
    const cause = errorResponse.cause[0]
    switch (cause.code) {
      case '2001': case '2002': case '2003': case '2004': return 'El pago fue rechazado por la tarjeta. Verificá los datos o probá con otra tarjeta.'
      case '3001': return 'Tu tarjeta no tiene fondos suficientes.'
      case '3002': return 'Tu tarjeta no está habilitada para compras online.'
      case '3003': return 'El pago excede el límite de tu tarjeta.'
      case '3004': return 'El código de seguridad es incorrecto.'
      case '4001': return 'La tarjeta está vencida.'
      case '4002': return 'Tu tarjeta fue reportada como robada o perdida.'
      case 'cc_rejected_bad_filled_card_number': return 'Número de tarjeta incorrecto.'
      case 'cc_rejected_bad_filled_date': return 'Fecha de vencimiento incorrecta.'
      case 'cc_rejected_bad_filled_security_code': return 'Código de seguridad incorrecto.'
      case 'cc_rejected_insufficient_amount': return 'Fondos insuficientes.'
      case 'cc_rejected_high_risk': return 'El pago fue rechazado por razones de seguridad.'
      case 'cc_rejected_call_for_authorize': return 'Debés autorizar el pago llamando a tu banco.'
      case 'cc_rejected_card_disabled': return 'Tu tarjeta está deshabilitada. Contactá a tu banco.'
      default: return cause.description || 'El pago fue rechazado. Intentá con otra tarjeta.'
    }
  }
  return 'Error procesando el pago. Por favor intentá nuevamente.'
}
