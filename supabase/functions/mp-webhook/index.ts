// Edge Function: mp-webhook
// Recibe webhooks/IPN de Mercado Pago para pagos con Split Payments.
// VALIDA firma del webhook, usa idempotency, NO desembolsa al vendedor
// (MP ya divide automáticamente via Split Payments).
// Deploy: supabase functions deploy mp-webhook --no-verify-jwt

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { crypto } from "https://deno.land/std@0.168.0/crypto/mod.ts"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

interface MPPayment {
  id: number
  status: string
  status_detail: string
  external_reference: string
  payment_method_id: string
  payment_type_id: string
  installments: number
  transaction_amount: number
  currency_id: string
  date_approved: string | null
  date_created: string
  payer: { id?: number; email?: string }
  collector_id: number
  sponsor_id?: number
  fee_details?: Array<{ type: string; amount: number; fee_payer: string }>
  transaction_details?: { net_received_amount: number; total_paid_amount: number }
  marketplace_fee?: number
  metadata: Record<string, unknown>
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // ── Parsear notificación ──
    const url = new URL(req.url)
    const topic = url.searchParams.get('topic') || url.searchParams.get('type')
    const paymentIdFromUrl = url.searchParams.get('id') || url.searchParams.get('data.id')

    let notification: { data?: { id: string }; type?: string } | null = null
    let rawBody = ''
    try {
      rawBody = await req.text()
      if (rawBody) notification = JSON.parse(rawBody)
    } catch { /* ok */ }

    // ── Validación de firma ──
    const webhookSecret = Deno.env.get('MERCADOPAGO_WEBHOOK_SECRET')
    const xSignature = req.headers.get('x-signature')
    const xRequestId = req.headers.get('x-request-id')

    if (webhookSecret && xSignature) {
      const isValid = await validateSignature(xSignature, xRequestId, rawBody, url, webhookSecret)
      if (!isValid) {
        console.warn('Firma de webhook inválida')
        return new Response('Forbidden', { status: 403, headers: corsHeaders })
      }
    }

    const effectivePaymentId = notification?.data?.id || paymentIdFromUrl
    const effectiveTopic = notification?.type || topic

    if (effectiveTopic !== 'payment' || !effectivePaymentId) {
      return new Response('OK', { status: 200, headers: corsHeaders })
    }

    // ── Idempotency: webhook ya procesado? ──
    const idempotencyKey = `webhook-${effectivePaymentId}-${effectiveTopic}`
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    const { data: existingAudit } = await supabase
      .from('payment_audit_log')
      .select('id')
      .eq('idempotency_key', idempotencyKey)
      .maybeSingle()

    if (existingAudit) {
      return new Response('OK - Duplicate', { status: 200, headers: corsHeaders })
    }

    // Obtener pago desde MP
    const MP_ACCESS_TOKEN = Deno.env.get('MERCADOPAGO_ACCESS_TOKEN')
    if (!MP_ACCESS_TOKEN) throw new Error('MERCADOPAGO_ACCESS_TOKEN no configurado')

    const paymentResponse = await fetch(
      `https://api.mercadopago.com/v1/payments/${effectivePaymentId}`,
      { headers: { 'Authorization': `Bearer ${MP_ACCESS_TOKEN}` } }
    )
    if (!paymentResponse.ok) {
      console.error('Error obteniendo pago:', await paymentResponse.text())
      return new Response('OK', { status: 200, headers: corsHeaders })
    }

    const payment: MPPayment = await paymentResponse.json()
    const orderId = payment.external_reference || (payment.metadata?.order_id as string) || ''
    if (!orderId) {
      return new Response('OK - No order_id', { status: 200, headers: corsHeaders })
    }

    // ── CRIT-1 + CRIT-2: Usar RPC unificado ──
    const feeDetails = payment.fee_details || []
    const feeDetailsJson = feeDetails.length > 0 ? feeDetails : null
    const netReceived = payment.transaction_details?.net_received_amount || payment.transaction_amount
    const marketplaceFee = payment.marketplace_fee || 0

    const { error: rpcError } = await supabase.rpc('update_order_from_payment', {
      p_order_id: orderId,
      p_mp_payment_id: payment.id.toString(),
      p_new_status: payment.status,
      p_fee_details: feeDetailsJson,
      p_net_amount: netReceived,
    })

    if (rpcError) {
      console.error('Error en update_order_from_payment:', rpcError)
      // Fallback directo
      const statusMap: Record<string, string> = {
        'approved': 'paid', 'pending': 'pending', 'in_process': 'pending',
        'rejected': 'payment_failed', 'cancelled': 'cancelled', 'refunded': 'refunded', 'charged_back': 'refunded',
      }
      const newStatus = statusMap[payment.status] || 'pending'

      await supabase.from('orders').update({
        status: newStatus,
        payment_status: payment.status,
        mp_payment_id: payment.id.toString(),
        payment_method: payment.payment_method_id,
        installments: payment.installments,
        updated_at: new Date().toISOString(),
        paid_at: payment.date_approved || undefined,
      }).eq('id', orderId)
    }

    // Actualizar payments
    await supabase
      .from('payments')
      .update({
        mp_payment_id: payment.id.toString(),
        status: payment.status,
        status_detail: payment.status_detail,
        payment_method_id: payment.payment_method_id,
        payment_type_id: payment.payment_type_id,
        installments: payment.installments,
        paid_at: payment.date_approved,
        mp_fee_details: feeDetailsJson,
        mp_net_received_amount: netReceived,
        mp_commission_amount: marketplaceFee || 0,
        mp_financial_status: payment.status === 'approved' ? 'settled' : payment.status,
        updated_at: new Date().toISOString(),
      })
      .eq('order_id', orderId)

    // Log de auditoría
    await supabase
      .from('payment_audit_log')
      .insert({
        payment_id: payment.id.toString(),
        order_id: orderId,
        event_type: effectiveTopic || 'payment',
        mp_status: payment.status,
        mp_status_detail: payment.status_detail,
        raw_payload: JSON.parse(JSON.stringify(payment)),
        mp_signature: xSignature || '',
        processing_result: 'processed',
        error_message: '',
        idempotency_key: idempotencyKey,
      })

    return new Response('OK', { status: 200, headers: corsHeaders })

  } catch (error) {
    console.error('Error en webhook:', error)
    return new Response('OK', { status: 200, headers: corsHeaders })
  }
})

async function validateSignature(
  xSignature: string, xRequestId: string | null,
  rawBody: string, url: URL, webhookSecret: string
): Promise<boolean> {
  try {
    const parts = xSignature.split(',')
    let ts = ''
    let hash = ''
    for (const part of parts) {
      const [key, value] = part.split('=')
      if (key?.trim() === 'ts') ts = value?.trim() || ''
      if (key?.trim() === 'v1') hash = value?.trim() || ''
    }
    if (!ts || !hash) return false

    const manifest = `id:${xRequestId || ''};request-id:${xRequestId || ''};ts:${ts};`
    const data = new TextEncoder().encode(manifest + rawBody)
    const keyData = new TextEncoder().encode(webhookSecret)

    const key = await crypto.subtle.importKey('raw', keyData, { name: 'HMAC', hash: 'SHA-256' }, false, ['sign'])
    const signature = await crypto.subtle.sign('HMAC', key, data)
    const computedHash = Array.from(new Uint8Array(signature)).map(b => b.toString(16).padStart(2, '0')).join('')

    return computedHash === hash
  } catch {
    return false
  }
}
