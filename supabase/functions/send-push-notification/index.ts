// ═══════════════════════════════════════════════════════════════════════════════
// MERCORA - FCM Push Notification Edge Function
// Envía notificaciones push a través de Firebase Cloud Messaging
// ═══════════════════════════════════════════════════════════════════════════════

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

// Configuración de Firebase
const FCM_SERVER_KEY = Deno.env.get('FCM_SERVER_KEY') || ''
const FCM_API_URL = 'https://fcm.googleapis.com/fcm/send'

interface PushNotification {
  id: string
  tokens: string[]
  title: string
  body: string
  image_url?: string
  data: Record<string, any>
}

async function sendToFCM(notification: PushNotification): Promise<{ success: boolean; error?: string }> {
  try {
    // Enviar a múltiples tokens
    const response = await fetch(FCM_API_URL, {
      method: 'POST',
      headers: {
        'Authorization': `key=${FCM_SERVER_KEY}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        registration_ids: notification.tokens,
        notification: {
          title: notification.title,
          body: notification.body,
          image: notification.image_url || undefined,
          sound: 'default',
          click_action: 'FLUTTER_NOTIFICATION_CLICK',
          android_channel_id: 'mercora_notifications',
        },
        data: {
          ...notification.data,
          title: notification.title,
          body: notification.body,
        },
        priority: 'high',
        content_available: true,
      }),
    })

    const result = await response.json()
    
    if (result.success > 0) {
      console.log(`✅ Push enviado: ${result.success} éxitos, ${result.failure} fallos`)
      return { success: true }
    } else {
      console.error('❌ FCM Error:', result)
      return { success: false, error: JSON.stringify(result.results) }
    }
  } catch (error) {
    console.error('❌ Error enviando a FCM:', error)
    return { success: false, error: error.message }
  }
}

serve(async (req) => {
  // Handle CORS
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    // ── JWT obligatorio ──
    const authHeader = req.headers.get('authorization') || ''
    const supabaseUrl = Deno.env.get('SUPABASE_URL') || ''
    const supabase = createClient(supabaseUrl, Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') || '')
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

    // Obtener notificaciones pendientes
    const { data: pendingNotifications, error: fetchError } = await supabase
      .rpc('get_pending_push_notifications', { batch_size: 50 })

    if (fetchError) {
      throw new Error(`Error fetching pending: ${fetchError.message}`)
    }

    if (!pendingNotifications || pendingNotifications.length === 0) {
      return new Response(
        JSON.stringify({ message: 'No pending notifications', processed: 0 }),
        { headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    console.log(`📬 Procesando ${pendingNotifications.length} notificaciones push...`)

    let successCount = 0
    let failCount = 0

    // Procesar cada notificación
    for (const notification of pendingNotifications) {
      const result = await sendToFCM(notification)
      
      // Marcar como enviada o fallida
      await supabase.rpc('mark_push_as_sent', {
        push_id: notification.id,
        success: result.success,
        error_msg: result.error || null,
      })

      if (result.success) {
        successCount++
      } else {
        failCount++
      }
    }

    // Auditoría
    try {
      await supabase.from('notification_audit').insert({
        user_id: authenticatedUserId,
        title: 'batch_push',
        body: `Batch processed ${pendingNotifications.length} notifications`,
        tokens_count: 0,
        sent_count: successCount,
        failed_count: failCount,
        created_at: new Date().toISOString(),
      })
    } catch { /* non-critical */ }

    return new Response(
      JSON.stringify({
        message: 'Push notifications processed',
        processed: pendingNotifications.length,
        success: successCount,
        failed: failCount,
      }),
      { headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )

  } catch (error) {
    console.error('❌ Error en Edge Function:', error)
    return new Response(
      JSON.stringify({ error: error.message }),
      { status: 500, headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  }
})
