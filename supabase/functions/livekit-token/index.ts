import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { AccessToken } from 'https://esm.sh/livekit-server-sdk@2.8.1'

const LIVEKIT_API_KEY = Deno.env.get('LIVEKIT_API_KEY') || ''
const LIVEKIT_API_SECRET = Deno.env.get('LIVEKIT_API_SECRET') || ''
const LIVEKIT_HOST = Deno.env.get('LIVEKIT_HOST') || ''
const SUPABASE_URL = Deno.env.get('SUPABASE_URL') || ''
const SUPABASE_ANON_KEY = Deno.env.get('SUPABASE_ANON_KEY') || ''
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') || ''

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'POST, OPTIONS',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

function jsonResponse(body: unknown, status: number) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json', ...CORS_HEADERS },
  })
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: CORS_HEADERS })
  }

  try {
    const authHeader = req.headers.get('Authorization')?.replace('Bearer ', '')
    if (!authHeader) {
      return jsonResponse({ error: 'No autorizado' }, 401)
    }

    const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
      global: { headers: { Authorization: `Bearer ${authHeader}` } },
    })
    const { data: { user }, error: authError } = await supabase.auth.getUser(authHeader)
    if (authError || !user) {
      return jsonResponse({ error: 'Token inválido' }, 401)
    }

    const { roomName, role } = await req.json()
    if (!roomName || !role) {
      return jsonResponse({ error: 'roomName y role son requeridos' }, 400)
    }
    if (!['broadcaster', 'viewer'].includes(role)) {
      return jsonResponse({ error: 'role debe ser broadcaster o viewer' }, 400)
    }

    const adminClient = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)
    const { data: profile } = await adminClient
      .from('usuarios')
      .select('username')
      .eq('id', user.id)
      .single()

    if (!LIVEKIT_API_KEY || !LIVEKIT_API_SECRET || !LIVEKIT_HOST) {
      return jsonResponse({ error: 'LiveKit no configurado en el servidor' }, 500)
    }

    const at = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET, {
      identity: role === 'broadcaster' ? `broadcaster-${user.id}` : `viewer-${user.id}`,
      name: profile?.username || user.email || user.id,
      metadata: JSON.stringify({
        userId: user.id,
        username: profile?.username || 'unknown',
        role,
      }),
      ttl: '2h',
    })
    at.addGrant({
      roomJoin: true,
      room: roomName,
      canPublish: role === 'broadcaster',
      canSubscribe: true,
      canPublishData: true,
    })

    const token = await at.toJwt()

    const identity = role === 'broadcaster' ? `broadcaster-${user.id}` : `viewer-${user.id}`

    return jsonResponse({
      token,
      url: LIVEKIT_HOST,
      roomName,
      identity,
    }, 200)
  } catch (err) {
    return jsonResponse({ error: err instanceof Error ? err.message : 'Error interno' }, 500)
  }
})
