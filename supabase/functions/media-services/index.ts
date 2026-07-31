import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

// ═══════════════════════════════════════════════════════════════════════════
// media-services: firmado/borrado de media y credenciales TURN efímeras.
//
// Sustituye las llaves privadas que antes iban compiladas dentro del APK:
//   - R2 (CLOUDFLARE_ACCESS_KEY_ID / SECRET): presigned PUT + DELETE server-side
//   - Cloudinary (API_SECRET): destroy server-side
//   - Metered (METERED_API_KEY): credenciales TURN efímeras
// Todas las acciones requieren un JWT de usuario autenticado.
// ═══════════════════════════════════════════════════════════════════════════

const SUPABASE_URL = Deno.env.get('SUPABASE_URL') || ''
const SUPABASE_ANON_KEY = Deno.env.get('SUPABASE_ANON_KEY') || ''

const R2_ACCOUNT_ID = Deno.env.get('CLOUDFLARE_ACCOUNT_ID') || ''
const R2_ACCESS_KEY_ID = Deno.env.get('CLOUDFLARE_ACCESS_KEY_ID') || ''
const R2_SECRET_ACCESS_KEY = Deno.env.get('CLOUDFLARE_SECRET_ACCESS_KEY') || ''
const R2_BUCKET = Deno.env.get('CLOUDFLARE_BUCKET_NAME') || ''
const R2_PUBLIC_DOMAIN = Deno.env.get('CLOUDFLARE_PUBLIC_DOMAIN') || ''

const CLOUDINARY_CLOUD_NAME = Deno.env.get('CLOUDINARY_CLOUD_NAME') || ''
const CLOUDINARY_API_KEY = Deno.env.get('CLOUDINARY_API_KEY') || ''
const CLOUDINARY_API_SECRET = Deno.env.get('CLOUDINARY_API_SECRET') || ''

const METERED_API_KEY = Deno.env.get('METERED_API_KEY') || ''

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'POST, GET, OPTIONS',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json', ...CORS_HEADERS },
  })
}

// ───────────────────────────────────────────────────────────────────────────
// Auth: JWT válido requerido
// ───────────────────────────────────────────────────────────────────────────
async function requireUser(req: Request) {
  const authHeader = req.headers.get('Authorization')?.replace('Bearer ', '')
  if (!authHeader) return { user: null, error: jsonResponse({ error: 'No autorizado' }, 401) }

  const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    global: { headers: { Authorization: `Bearer ${authHeader}` } },
  })
  const { data: { user }, error: authError } = await supabase.auth.getUser(authHeader)
  if (authError || !user) {
    return { user: null, error: jsonResponse({ error: 'Token inválido' }, 401) }
  }
  return { user, error: null }
}

// ───────────────────────────────────────────────────────────────────────────
// Cripto helpers
// ───────────────────────────────────────────────────────────────────────────

function bytesToHex(bytes: Uint8Array): string {
  return Array.from(bytes).map((b) => b.toString(16).padStart(2, '0')).join('')
}

async function sha256Hex(data: string): Promise<string> {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(data))
  return bytesToHex(new Uint8Array(digest))
}

async function sha1Hex(data: string): Promise<string> {
  const digest = await crypto.subtle.digest('SHA-1', new TextEncoder().encode(data))
  return bytesToHex(new Uint8Array(digest))
}

async function hmac(key: Uint8Array, data: string): Promise<Uint8Array> {
  const cryptoKey = await crypto.subtle.importKey(
    'raw', key, { name: 'HMAC', hash: 'SHA-256' }, false, ['sign'],
  )
  return new Uint8Array(await crypto.subtle.sign('HMAC', cryptoKey, new TextEncoder().encode(data)))
}

async function hmacHex(key: Uint8Array, data: string): Promise<string> {
  return bytesToHex(await hmac(key, data))
}

// ───────────────────────────────────────────────────────────────────────────
// AWS SigV4 (para Cloudflare R2)
// ───────────────────────────────────────────────────────────────────────────

const REGION = 'auto'
const SERVICE = 's3'
const EMPTY_BODY_HASH = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'

function rfc3986(input: string): string {
  return encodeURIComponent(input).replace(/[!'()*]/g, (c) => `%${c.charCodeAt(0).toString(16).toUpperCase()}`)
}

function canonicalUri(objectKey: string): string {
  return objectKey.split('/').map(rfc3986).join('/')
}

function amzDates(now = new Date()): { amzDate: string; dateStamp: string } {
  const amzDate = now.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '')
  return { amzDate, dateStamp: amzDate.slice(0, 8) }
}

async function signingKey(dateStamp: string): Promise<Uint8Array> {
  const kDate = await hmac(new TextEncoder().encode(`AWS4${R2_SECRET_ACCESS_KEY}`), dateStamp)
  const kRegion = await hmac(kDate, REGION)
  const kService = await hmac(kRegion, SERVICE)
  return hmac(kService, 'aws4_request')
}

async function r2SignedPut(objectKey: string, contentType: string, bodyHash: string) {
  const { amzDate, dateStamp } = amzDates()
  const host = `${R2_ACCOUNT_ID}.r2.cloudflarestorage.com`

  const headers: Record<string, string> = {
    'host': host,
    'content-type': contentType,
    'x-amz-content-sha256': bodyHash,
    'x-amz-date': amzDate,
  }
  const signedHeaders = Object.keys(headers).sort().join(';')
  const canonicalHeaders = Object.keys(headers)
    .sort()
    .map((k) => `${k}:${headers[k]}\n`)
    .join('')

  const canonicalRequest = [
    'PUT',
    `/${R2_BUCKET}/${canonicalUri(objectKey)}`,
    '',
    canonicalHeaders,
    signedHeaders,
    bodyHash,
  ].join('\n')

  const scope = `${dateStamp}/${REGION}/${SERVICE}/aws4_request`
  const stringToSign = ['AWS4-HMAC-SHA256', amzDate, scope, await sha256Hex(canonicalRequest)].join('\n')
  const signature = await hmacHex(await signingKey(dateStamp), stringToSign)

  return {
    url: `https://${host}/${R2_BUCKET}/${canonicalUri(objectKey)}`,
    headers: {
      'x-amz-date': amzDate,
      'x-amz-content-sha256': bodyHash,
      'Authorization': `AWS4-HMAC-SHA256 Credential=${R2_ACCESS_KEY_ID}/${scope}, SignedHeaders=${signedHeaders}, Signature=${signature}`,
    },
    publicUrl: `${R2_PUBLIC_DOMAIN}/${objectKey}`,
  }
}

async function r2SignedRequest(method: 'DELETE', objectKey: string) {
  const { amzDate, dateStamp } = amzDates()
  const host = `${R2_ACCOUNT_ID}.r2.cloudflarestorage.com`

  const headers: Record<string, string> = {
    'host': host,
    'x-amz-content-sha256': EMPTY_BODY_HASH,
    'x-amz-date': amzDate,
  }
  const signedHeaders = Object.keys(headers).sort().join(';')
  const canonicalHeaders = Object.keys(headers)
    .sort()
    .map((k) => `${k}:${headers[k]}\n`)
    .join('')

  const canonicalRequest = [
    method,
    `/${R2_BUCKET}/${canonicalUri(objectKey)}`,
    '',
    canonicalHeaders,
    signedHeaders,
    EMPTY_BODY_HASH,
  ].join('\n')

  const scope = `${dateStamp}/${REGION}/${SERVICE}/aws4_request`
  const stringToSign = ['AWS4-HMAC-SHA256', amzDate, scope, await sha256Hex(canonicalRequest)].join('\n')
  const signature = await hmacHex(await signingKey(dateStamp), stringToSign)

  return {
    url: `https://${host}/${R2_BUCKET}/${canonicalUri(objectKey)}`,
    headers: {
      'x-amz-date': amzDate,
      'x-amz-content-sha256': EMPTY_BODY_HASH,
      'Authorization': `AWS4-HMAC-SHA256 Credential=${R2_ACCESS_KEY_ID}/${scope}, SignedHeaders=${signedHeaders}, Signature=${signature}`,
    },
  }
}

function sanitizeObjectKey(key: string): string | null {
  if (!key || key.length > 200) return null
  if (key.includes('..') || key.startsWith('/') || key.includes('//')) return null
  if (!/^[a-z0-9][a-z0-9/_.-]*$/i.test(key)) return null
  return key
}

// ───────────────────────────────────────────────────────────────────────────
// Handlers por acción
// ───────────────────────────────────────────────────────────────────────────

async function handleR2UploadSign(body: Record<string, unknown>) {
  const objectKey = sanitizeObjectKey(String(body.objectKey ?? ''))
  const contentType = String(body.contentType ?? 'application/octet-stream')
  const bodyHash = String(body.bodyHash ?? '')
  if (!objectKey) return jsonResponse({ error: 'objectKey inválido' }, 400)
  if (!/^[\w.+-]+\/[\w.+-]+$/.test(contentType)) return jsonResponse({ error: 'contentType inválido' }, 400)
  if (!/^[a-f0-9]{64}$/i.test(bodyHash)) return jsonResponse({ error: 'bodyHash inválido (SHA-256 hex esperado)' }, 400)
  if (!R2_ACCESS_KEY_ID || !R2_SECRET_ACCESS_KEY || !R2_ACCOUNT_ID || !R2_BUCKET || !R2_PUBLIC_DOMAIN) {
    return jsonResponse({ error: 'R2 no configurado en el servidor' }, 500)
  }
  const { url, headers, publicUrl } = await r2SignedPut(objectKey, contentType, bodyHash)
  return jsonResponse({ url, headers, publicUrl })
}

async function handleR2Delete(body: Record<string, unknown>) {
  const objectKey = sanitizeObjectKey(String(body.objectKey ?? ''))
  if (!objectKey) return jsonResponse({ error: 'objectKey inválido' }, 400)
  const { url, headers } = await r2SignedRequest('DELETE', objectKey)
  const res = await fetch(url, { method: 'DELETE', headers })
  if (!res.ok) {
    const errText = await res.text()
    return jsonResponse({ ok: false, error: `R2 delete ${res.status}: ${errText.slice(0, 200)}` }, 502)
  }
  return jsonResponse({ ok: true })
}

async function handleCloudinaryDelete(body: Record<string, unknown>) {
  const publicId = String(body.publicId ?? '')
  if (!publicId || publicId.length > 300 || publicId.includes('..')) {
    return jsonResponse({ error: 'publicId inválido' }, 400)
  }
  if (!CLOUDINARY_API_KEY || !CLOUDINARY_API_SECRET || !CLOUDINARY_CLOUD_NAME) {
    return jsonResponse({ error: 'Cloudinary no configurado en el servidor' }, 500)
  }
  const timestamp = Math.floor(Date.now() / 1000).toString()
  const toSign = `public_id=${publicId}&timestamp=${timestamp}${CLOUDINARY_API_SECRET}`
  const signature = await sha1Hex(toSign)

  const form = new FormData()
  form.set('public_id', publicId)
  form.set('signature', signature)
  form.set('api_key', CLOUDINARY_API_KEY)
  form.set('timestamp', timestamp)

  const res = await fetch(`https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/destroy`, {
    method: 'POST',
    body: form,
  })
  const data = await res.json()
  return jsonResponse({ ok: data?.result === 'ok', result: data?.result ?? null }, res.ok ? 200 : 502)
}

async function handleTurnCredentials() {
  if (!METERED_API_KEY) return jsonResponse({ error: 'Metered no configurado en el servidor' }, 500)
  const res = await fetch(`https://mercora-calls.metered.live/api/v1/turn/credentials?apiKey=${METERED_API_KEY}`)
  if (!res.ok) {
    return jsonResponse({ error: `Metered ${res.status}` }, 502)
  }
  const data = await res.json()
  return jsonResponse({ iceServers: data })
}

// ───────────────────────────────────────────────────────────────────────────
serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: CORS_HEADERS })

  try {
    const { user, error } = await requireUser(req)
    if (error || !user) return error ?? jsonResponse({ error: 'No autorizado' }, 401)

    if (req.method === 'GET') return jsonResponse({ ok: true })

    const body: Record<string, unknown> = await req.json()
    const action = String(body.action ?? '')

    switch (action) {
      case 'r2-upload-sign':
        return await handleR2UploadSign(body)
      case 'r2-delete':
        return await handleR2Delete(body)
      case 'cloudinary-delete':
        return await handleCloudinaryDelete(body)
      case 'turn-credentials':
        return await handleTurnCredentials()
      default:
        return jsonResponse({ error: `Acción desconocida: ${action}` }, 400)
    }
  } catch (err) {
    return jsonResponse({ error: err instanceof Error ? err.message : 'Error interno' }, 500)
  }
})
