// ═══════════════════════════════════════════════════════════════════════════════
// RENDLY - FCM HTTP v1 API Edge Function
// Envía notificaciones push de forma segura usando OAuth2
// ═══════════════════════════════════════════════════════════════════════════════

import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

// Configuración de Firebase - Se obtiene de variables de entorno de Supabase
const FIREBASE_PROJECT_ID = Deno.env.get('FIREBASE_PROJECT_ID') || 'rendly-app'
const FIREBASE_CLIENT_EMAIL = Deno.env.get('FIREBASE_CLIENT_EMAIL') || ''
const FIREBASE_PRIVATE_KEY = Deno.env.get('FIREBASE_PRIVATE_KEY')?.replace(/\\n/g, '\n') || ''

// Función para crear JWT para OAuth2
async function createJWT(): Promise<string> {
  const header = {
    alg: 'RS256',
    typ: 'JWT'
  }
  
  const now = Math.floor(Date.now() / 1000)
  const payload = {
    iss: FIREBASE_CLIENT_EMAIL,
    sub: FIREBASE_CLIENT_EMAIL,
    aud: 'https://oauth2.googleapis.com/token',
    iat: now,
    exp: now + 3600, // 1 hora
    scope: 'https://www.googleapis.com/auth/firebase.messaging'
  }
  
  // Codificar header y payload
  const encoder = new TextEncoder()
  const headerB64 = btoa(JSON.stringify(header)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  const payloadB64 = btoa(JSON.stringify(payload)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  
  const signatureInput = `${headerB64}.${payloadB64}`
  
  // Importar la clave privada
  const pemContents = FIREBASE_PRIVATE_KEY
    .replace('-----BEGIN PRIVATE KEY-----', '')
    .replace('-----END PRIVATE KEY-----', '')
    .replace(/\s/g, '')
  
  const binaryKey = Uint8Array.from(atob(pemContents), c => c.charCodeAt(0))
  
  const cryptoKey = await crypto.subtle.importKey(
    'pkcs8',
    binaryKey,
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false,
    ['sign']
  )
  
  // Firmar
  const signature = await crypto.subtle.sign(
    'RSASSA-PKCS1-v1_5',
    cryptoKey,
    encoder.encode(signatureInput)
  )
  
  const signatureB64 = btoa(String.fromCharCode(...new Uint8Array(signature)))
    .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  
  return `${signatureInput}.${signatureB64}`
}

// Obtener token de acceso OAuth2
async function getAccessToken(): Promise<string> {
  const jwt = await createJWT()
  
  const response = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion: jwt
    })
  })
  
  const data = await response.json()
  
  if (!response.ok) {
    throw new Error(`OAuth error: ${JSON.stringify(data)}`)
  }
  
  return data.access_token
}

// Enviar notificación via FCM v1 API
async function sendFCMNotification(
  token: string,
  title: string,
  body: string,
  data: Record<string, string> = {},
  imageUrl?: string
): Promise<{ success: boolean; error?: string }> {
  try {
    const accessToken = await getAccessToken()
    
    // DATA-ONLY message: ensures onMessageReceived is ALWAYS called,
    // even when the app is in background/killed. This lets us use custom
    // channels, sounds, and intents on the Android side.
    const allData: Record<string, string> = {
      ...Object.fromEntries(
        Object.entries(data).map(([k, v]) => [k, String(v)])
      ),
      title: String(title),
      body: String(body),
    }
    
    if (imageUrl) {
      allData.image_url = String(imageUrl)
    }
    
    const message: any = {
      token,
      data: allData,
      android: {
        priority: 'high'
      }
    }
    
    const response = await fetch(
      `https://fcm.googleapis.com/v1/projects/${FIREBASE_PROJECT_ID}/messages:send`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ message })
      }
    )
    
    const result = await response.json()
    
    if (!response.ok) {
      return { success: false, error: result.error?.message || JSON.stringify(result) }
    }
    
    return { success: true }
  } catch (error) {
    return { success: false, error: error.message }
  }
}

serve(async (req) => {
  // CORS
  if (req.method === 'OPTIONS') {
    return new Response('ok', {
      headers: {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'POST, OPTIONS',
        'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type'
      }
    })
  }
  
  try {
    const { tokens, title, body, data, image_url } = await req.json()
    
    if (!tokens || !Array.isArray(tokens) || tokens.length === 0) {
      return new Response(
        JSON.stringify({ success: false, error: 'No tokens provided' }),
        { status: 400, headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' } }
      )
    }
    
    if (!title || !body) {
      return new Response(
        JSON.stringify({ success: false, error: 'Title and body are required' }),
        { status: 400, headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' } }
      )
    }
    
    // Verificar que las credenciales estén configuradas
    if (!FIREBASE_CLIENT_EMAIL || !FIREBASE_PRIVATE_KEY) {
      return new Response(
        JSON.stringify({ 
          success: false, 
          error: 'Firebase credentials not configured. Set FIREBASE_CLIENT_EMAIL and FIREBASE_PRIVATE_KEY in Supabase secrets.' 
        }),
        { status: 500, headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' } }
      )
    }
    
    // Enviar a todos los tokens
    const results = await Promise.all(
      tokens.map(token => sendFCMNotification(token, title, body, data || {}, image_url))
    )
    
    const successCount = results.filter(r => r.success).length
    const failures = results.filter(r => !r.success).map(r => r.error)
    
    return new Response(
      JSON.stringify({
        success: successCount > 0,
        sent: successCount,
        total: tokens.length,
        failures: failures.length > 0 ? failures : undefined
      }),
      { 
        status: 200, 
        headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' } 
      }
    )
  } catch (error) {
    return new Response(
      JSON.stringify({ success: false, error: error.message }),
      { status: 500, headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' } }
    )
  }
})
