// Edge Function: mercadopago-oauth-exchange
// Intercambia el código de autorización OAuth por tokens de Mercado Pago.
// Se ejecuta del lado del servidor para mantener el client_secret seguro.
// HIGH-4: Usa auth.uid() del JWT, NO acepta user_id del cliente.
// CRIT-4: Cifra tokens con AES-256-GCM real.
// Deploy: supabase functions deploy mercadopago-oauth-exchange

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

  try {
    // HIGH-1 + HIGH-4: Obtener user_id del JWT, NO del body
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

    const { code } = await req.json()
    if (!code) {
      return new Response(JSON.stringify({ error: 'Código de autorización requerido' }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    const CLIENT_ID = Deno.env.get('MERCADOPAGO_CLIENT_ID')
    const CLIENT_SECRET = Deno.env.get('MERCADOPAGO_CLIENT_SECRET')
    const ENCRYPTION_KEY = Deno.env.get('TOKEN_ENCRYPTION_KEY')

    if (!CLIENT_ID || !CLIENT_SECRET || !ENCRYPTION_KEY) {
      throw new Error('Configuración de Mercado Pago incompleta en variables de entorno')
    }

    const REDIRECT_URI = Deno.env.get('MERCADOPAGO_REDIRECT_URI') || 'mercora://mp-oauth/callback'

    const tokenResponse = await fetch('https://api.mercadopago.com/oauth/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify({
        client_id: CLIENT_ID,
        client_secret: CLIENT_SECRET,
        code,
        redirect_uri: REDIRECT_URI,
        grant_type: 'authorization_code',
      }),
    })

    if (!tokenResponse.ok) {
      const errorData = await tokenResponse.json()
      return new Response(JSON.stringify({ error: 'Error al intercambiar código OAuth', mp_error: errorData }), { status: 400, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })
    }

    const mpData = await tokenResponse.json()
    const tokenExpiresAt = new Date(Date.now() + mpData.expires_in * 1000).toISOString()

    // CRIT-4: Cifrado real AES-256-GCM
    const encryptedAccessToken = await encryptToken(mpData.access_token, ENCRYPTION_KEY)
    const encryptedRefreshToken = await encryptToken(mpData.refresh_token, ENCRYPTION_KEY)

    const { error: upsertError } = await supabase
      .from('mercadopago_connections')
      .upsert({
        user_id: authenticatedUserId,
        mercadopago_user_id: mpData.user_id.toString(),
        mercadopago_seller_id: null,
        access_token_encrypted: encryptedAccessToken,
        refresh_token_encrypted: encryptedRefreshToken,
        token_expires_at: tokenExpiresAt,
        public_key: mpData.public_key,
        scope: mpData.scope,
        conexion_estado: 'activa',
        conexion_actualizada_en: new Date().toISOString(),
      }, { onConflict: 'user_id' })

    if (upsertError) {
      console.error('Error guardando conexión:', upsertError)
      throw new Error('Error al guardar la conexión de Mercado Pago')
    }

    return new Response(JSON.stringify({
      success: true,
      mercadopago_user_id: mpData.user_id.toString(),
      public_key: mpData.public_key,
      live_mode: mpData.live_mode,
      expires_at: tokenExpiresAt,
    }), { status: 200, headers: { ...corsHeaders, 'Content-Type': 'application/json' } })

  } catch (error) {
    console.error('Error en OAuth exchange:', error)
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
  return crypto.subtle.importKey('raw', keyBytes, { name: 'AES-GCM' }, false, ['encrypt', 'decrypt'])
}

async function encryptToken(plaintext: string, keyInput: string): Promise<string> {
  const key = await deriveKey(keyInput)
  const iv = crypto.getRandomValues(new Uint8Array(12))
  const encoded = new TextEncoder().encode(plaintext)
  const encrypted = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, encoded)
  const encryptedArray = new Uint8Array(encrypted)
  const result = new Uint8Array(iv.length + encryptedArray.length)
  result.set(iv)
  result.set(encryptedArray, iv.length)
  return btoa(String.fromCharCode(...result))
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
