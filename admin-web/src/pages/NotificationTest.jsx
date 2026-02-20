import { useState, useEffect } from 'react'
import { 
  Bell,
  Heart,
  RefreshCw,
  Zap,
  Image,
  User,
  Send,
  CheckCircle,
  AlertCircle,
  Clock,
  Smartphone,
  Search,
  UserCheck
} from 'lucide-react'
import { supabase } from '../supabaseClient'

// URL de Supabase para Edge Functions (FCM v1 API - más seguro)
const SUPABASE_URL = 'https://xyrpmmnegzjkbysoocpc.supabase.co'
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5cnBtbW5lZ3pqa2J5c29vY3BjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjczOTA1NDcsImV4cCI6MjA4Mjk2NjU0N30.RsQE2JCJbHoNzlqG95Hf4W0QNyx5Xzw5bwvYcfpWKI0'

// Función para enviar push notification via Supabase Edge Function (FCM v1 API)
async function sendPushNotification(tokens, title, body, data = {}, imageUrl = null) {
  if (!tokens || tokens.length === 0) {
    console.log('⚠️ No hay tokens FCM para enviar')
    return { success: false, error: 'No hay tokens' }
  }

  try {
    const response = await fetch(`${SUPABASE_URL}/functions/v1/send-fcm-v1`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${SUPABASE_ANON_KEY}`,
        'apikey': SUPABASE_ANON_KEY,
      },
      body: JSON.stringify({
        tokens,
        title,
        body,
        data,
        image_url: imageUrl
      }),
    })

    const result = await response.json()
    console.log('📬 FCM v1 Response:', result)
    
    if (result.success) {
      return { success: true, result }
    } else {
      return { success: false, error: result.error || 'Error desconocido' }
    }
  } catch (error) {
    console.error('❌ Error enviando push:', error)
    return { success: false, error: error.message }
  }
}

// Obtener tokens FCM de un usuario usando función RPC (bypass RLS)
async function getUserFCMTokens(userId) {
  // Usar función RPC con SECURITY DEFINER para bypass de RLS
  const { data, error } = await supabase
    .rpc('get_user_fcm_tokens', { target_user_id: userId })

  if (error) {
    console.error('Error obteniendo tokens via RPC:', error)
    // Fallback: intentar query directa (solo funciona si hay permisos)
    const { data: fallbackData, error: fallbackError } = await supabase
      .from('fcm_tokens')
      .select('token')
      .eq('user_id', userId)
      .eq('is_active', true)
    
    if (fallbackError) {
      console.error('Error en fallback:', fallbackError)
      return []
    }
    return fallbackData?.map(t => t.token) || []
  }

  return data?.map(t => t.token) || []
}

function NotificationTest() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [realtimeStatus, setRealtimeStatus] = useState('connecting')
  const [actionLoading, setActionLoading] = useState(null)
  const [notification, setNotification] = useState(null)
  const [currentUser, setCurrentUser] = useState(null)
  const [targetUserId, setTargetUserId] = useState('')
  const [sentNotifications, setSentNotifications] = useState([])
  
  // Estado para selector de usuario remitente
  const [users, setUsers] = useState([])
  const [searchQuery, setSearchQuery] = useState('')
  const [searching, setSearching] = useState(false)
  const [senderUser, setSenderUser] = useState(null)

  // Obtener usuario actual y cargar posts
  useEffect(() => {
    getCurrentUser()
    fetchPosts()
    
    // Suscribirse a cambios en notificaciones (para ver las que enviamos)
    const channel = supabase
      .channel('notification-test-channel')
      .on(
        'postgres_changes',
        {
          event: 'INSERT',
          schema: 'public',
          table: 'notifications'
        },
        (payload) => {
          console.log('🔔 Nueva notificación creada:', payload.new)
          setSentNotifications(prev => [payload.new, ...prev].slice(0, 10))
        }
      )
      .subscribe((status) => {
        console.log('Realtime status:', status)
        setRealtimeStatus(status === 'SUBSCRIBED' ? 'connected' : status)
      })

    return () => {
      supabase.removeChannel(channel)
    }
  }, [])

  async function getCurrentUser() {
    const { data: { user } } = await supabase.auth.getUser()
    setCurrentUser(user)
    console.log('Usuario actual:', user?.id)
  }

  function showNotificationToast(message, type = 'info') {
    setNotification({ message, type })
    setTimeout(() => setNotification(null), 4000)
  }

  async function fetchPosts() {
    setLoading(true)
    try {
      // Obtener posts con bypass de RLS usando service role o anon key
      const { data, error } = await supabase
        .from('posts')
        .select('*')
        .order('created_at', { ascending: false })
        .limit(20)

      if (error) throw error
      setPosts(data || [])
      console.log('Posts cargados:', data?.length)
    } catch (error) {
      console.error('Error fetching posts:', error)
      showNotificationToast('Error al cargar posts: ' + error.message, 'error')
    } finally {
      setLoading(false)
    }
  }

  async function searchUsers() {
    if (!searchQuery.trim()) return
    
    setSearching(true)
    try {
      const { data, error } = await supabase
        .from('usuarios')
        .select('*')
        .ilike('username', `%${searchQuery}%`)
        .limit(10)

      if (error) throw error
      setUsers(data || [])
    } catch (error) {
      console.error('Error searching users:', error)
      showNotificationToast('Error buscando usuarios: ' + error.message, 'error')
    } finally {
      setSearching(false)
    }
  }

  async function sendLikeNotification(post) {
    if (!post.user_id) {
      showNotificationToast('Este post no tiene user_id válido', 'error')
      return
    }

    setActionLoading(post.id)
    try {
      // Usar el usuario seleccionado como remitente, o fallback al actual
      const senderUsername = senderUser?.username || 'Usuario'
      const senderId = senderUser?.user_id || currentUser?.id
      const senderAvatar = senderUser?.avatar_url || null

      // Crear notificación de LIKE en la base de datos
      const { error } = await supabase
        .from('notifications')
        .insert({
          recipient_id: post.user_id,
          sender_id: senderId,
          sender_username: senderUsername,
          sender_avatar: senderAvatar,
          type: 'like',
          post_id: post.id,
          post_image: post.images?.[0] || null,
          is_read: false
        })

      if (error) throw error

      // 🔔 ENVIAR PUSH NOTIFICATION VIA FCM
      const tokens = await getUserFCMTokens(post.user_id)
      console.log(`📱 Tokens FCM encontrados: ${tokens.length}`)
      
      if (tokens.length > 0) {
        const pushResult = await sendPushNotification(
          tokens,
          `${senderUsername} le dio like a tu publicación`,
          'Toca para ver',
          {
            type: 'like',
            post_id: post.id,
            sender_id: currentUser?.id,
            sender_username: senderUsername
          }
        )
        
        if (pushResult.success) {
          showNotificationToast(`✅ Notificación + Push enviada a ${post.user_id.slice(0, 8)}...`, 'success')
        } else {
          showNotificationToast(`✅ Notificación enviada (push falló: ${pushResult.error})`, 'warning')
        }
      } else {
        showNotificationToast(`✅ Notificación enviada (sin tokens FCM)`, 'success')
      }
    } catch (error) {
      console.error('Error sending notification:', error)
      showNotificationToast('Error: ' + error.message, 'error')
    } finally {
      setActionLoading(null)
    }
  }

  async function sendCustomNotification() {
    if (!targetUserId.trim()) {
      showNotificationToast('Ingresa un User ID de destino', 'error')
      return
    }

    setActionLoading('custom')
    try {
      const { data: userData } = await supabase
        .from('usuarios')
        .select('username, avatar_url')
        .eq('user_id', currentUser?.id)
        .maybeSingle()

      const senderUsername = userData?.username || 'Admin Web'

      const { error } = await supabase
        .from('notifications')
        .insert({
          recipient_id: targetUserId.trim(),
          sender_id: currentUser?.id,
          sender_username: senderUsername,
          sender_avatar: userData?.avatar_url || null,
          type: 'like',
          message: 'Test de notificación desde Admin Web',
          is_read: false
        })

      if (error) throw error

      // 🔔 ENVIAR PUSH NOTIFICATION VIA FCM
      const tokens = await getUserFCMTokens(targetUserId.trim())
      console.log(`📱 Tokens FCM encontrados: ${tokens.length}`)
      
      if (tokens.length > 0) {
        const pushResult = await sendPushNotification(
          tokens,
          `${senderUsername} le dio like`,
          'Test de notificación',
          {
            type: 'like',
            sender_id: currentUser?.id,
            sender_username: senderUsername
          }
        )
        
        if (pushResult.success) {
          showNotificationToast(`✅ Notificación + Push enviada a ${targetUserId.slice(0, 8)}...`, 'success')
        } else {
          showNotificationToast(`✅ Notificación enviada (push falló: ${pushResult.error})`, 'warning')
        }
      } else {
        showNotificationToast(`✅ Notificación enviada (sin tokens FCM)`, 'success')
      }
      
      setTargetUserId('')
    } catch (error) {
      console.error('Error:', error)
      showNotificationToast('Error: ' + error.message, 'error')
    } finally {
      setActionLoading(null)
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return '-'
    return new Date(dateStr).toLocaleString('es-ES', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-accent-magenta/20 flex items-center justify-center">
              <Bell className="w-6 h-6 text-accent-magenta" />
            </div>
            Test de Notificaciones
          </h1>
          <p className="text-text-muted mt-1">
            Prueba el sistema de notificaciones en tiempo real. Da like a un post y mira el badge en la app.
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Realtime status */}
          <div className={`flex items-center gap-2 px-3 py-2 rounded-lg ${
            realtimeStatus === 'connected' 
              ? 'bg-green-500/20 text-green-400' 
              : 'bg-yellow-500/20 text-yellow-400'
          }`}>
            <Zap className={`w-4 h-4 ${realtimeStatus === 'connected' ? 'animate-pulse' : ''}`} />
            <span className="text-sm font-medium">
              {realtimeStatus === 'connected' ? 'Realtime activo' : 'Conectando...'}
            </span>
          </div>

          <button
            onClick={fetchPosts}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 bg-rendly-surface-elevated text-text-primary rounded-xl hover:bg-primary/20 transition-colors disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            Recargar
          </button>
        </div>
      </div>

      {/* Notification Toast */}
      {notification && (
        <div className={`fixed top-4 right-4 z-50 px-4 py-3 rounded-xl shadow-lg flex items-center gap-2 ${
          notification.type === 'success' ? 'bg-green-500/90 text-white' :
          notification.type === 'error' ? 'bg-red-500/90 text-white' :
          'bg-blue-500/90 text-white'
        }`}>
          {notification.message}
        </div>
      )}

      {/* Info box */}
      <div className="bg-accent-magenta/10 border border-accent-magenta/30 rounded-xl p-4">
        <h3 className="font-semibold text-accent-magenta mb-2">📱 Cómo funciona:</h3>
        <ol className="list-decimal list-inside space-y-1 text-text-secondary text-sm">
          <li>Selecciona un usuario <strong>remitente</strong> abajo (quien "da" el like)</li>
          <li>Abre la app Android y asegúrate de estar logueado</li>
          <li>Da click en el botón ❤️ de cualquier post</li>
          <li>La notificación llegará mostrando el username del remitente</li>
        </ol>
      </div>

      {/* Selector de usuario remitente */}
      <div className="bg-rendly-surface rounded-xl p-4 space-y-4">
        <h3 className="font-semibold text-text-primary flex items-center gap-2">
          <UserCheck className="w-5 h-5 text-blue-400" />
          Seleccionar usuario remitente
        </h3>
        <p className="text-text-muted text-sm">
          Busca y selecciona el usuario que aparecerá como quien da el like (en nombre de quién envías).
        </p>

        <div className="flex gap-2">
          <input
            type="text"
            placeholder="Buscar por username..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && searchUsers()}
            className="flex-1 px-4 py-2 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
          />
          <button
            onClick={searchUsers}
            disabled={searching}
            className="px-4 py-2 bg-primary text-white rounded-xl hover:bg-primary/80 transition-colors disabled:opacity-50"
          >
            {searching ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
          </button>
        </div>

        {/* Usuario seleccionado */}
        {senderUser && (
          <div className="flex items-center gap-3 p-3 bg-blue-500/20 border border-blue-500/30 rounded-lg">
            {senderUser.avatar_url ? (
              <img src={senderUser.avatar_url} alt={senderUser.username} className="w-10 h-10 rounded-full object-cover" />
            ) : (
              <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                <User className="w-5 h-5 text-primary" />
              </div>
            )}
            <div className="flex-1">
              <p className="text-text-primary font-medium">@{senderUser.username}</p>
              <p className="text-text-muted text-xs">Remitente seleccionado</p>
            </div>
            <button
              onClick={() => setSenderUser(null)}
              className="px-3 py-1 text-xs bg-red-500/20 text-red-400 rounded-lg hover:bg-red-500/30"
            >
              Quitar
            </button>
          </div>
        )}

        {/* Lista de usuarios encontrados */}
        {users.length > 0 && (
          <div className="space-y-2 max-h-40 overflow-y-auto">
            {users.map(user => (
              <div 
                key={user.user_id}
                className={`flex items-center gap-3 p-3 rounded-lg cursor-pointer transition-colors ${
                  senderUser?.user_id === user.user_id 
                    ? 'bg-blue-500/20 border border-blue-500/30' 
                    : 'bg-rendly-bg hover:bg-primary/10'
                }`}
                onClick={() => setSenderUser(user)}
              >
                {user.avatar_url ? (
                  <img src={user.avatar_url} alt={user.username} className="w-8 h-8 rounded-full object-cover" />
                ) : (
                  <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center">
                    <User className="w-4 h-4 text-primary" />
                  </div>
                )}
                <div className="flex-1">
                  <p className="text-text-primary font-medium text-sm">@{user.username}</p>
                  <p className="text-text-muted text-xs">{user.user_id?.slice(0, 12)}...</p>
                </div>
                {senderUser?.user_id === user.user_id && (
                  <CheckCircle className="w-5 h-5 text-blue-400" />
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Custom notification sender */}
      <div className="bg-rendly-surface rounded-xl p-4">
        <h3 className="font-semibold text-text-primary mb-3 flex items-center gap-2">
          <Send className="w-5 h-5 text-primary" />
          Enviar notificación a un usuario específico
        </h3>
        <div className="flex gap-3">
          <input
            type="text"
            placeholder="User ID del destinatario (UUID)"
            value={targetUserId}
            onChange={(e) => setTargetUserId(e.target.value)}
            className="flex-1 px-4 py-2 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
          />
          <button
            onClick={sendCustomNotification}
            disabled={actionLoading === 'custom' || !targetUserId.trim()}
            className="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-xl hover:bg-primary/80 transition-colors disabled:opacity-50"
          >
            {actionLoading === 'custom' ? (
              <RefreshCw className="w-4 h-4 animate-spin" />
            ) : (
              <Send className="w-4 h-4" />
            )}
            Enviar
          </button>
        </div>
        <p className="text-text-muted text-xs mt-2">
          Tu ID: <code className="bg-rendly-bg px-1 rounded">{currentUser?.id || 'cargando...'}</code>
        </p>
      </div>

      {/* Recent sent notifications */}
      {sentNotifications.length > 0 && (
        <div className="bg-rendly-surface rounded-xl p-4">
          <h3 className="font-semibold text-text-primary mb-3 flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-green-400" />
            Notificaciones enviadas recientemente
          </h3>
          <div className="space-y-2 max-h-40 overflow-y-auto">
            {sentNotifications.map((notif, idx) => (
              <div key={notif.id || idx} className="flex items-center gap-3 text-sm bg-rendly-bg p-2 rounded-lg">
                <Bell className="w-4 h-4 text-accent-magenta" />
                <span className="text-text-secondary">
                  <span className="text-text-primary font-medium">{notif.type}</span>
                  {' → '}
                  <code className="text-xs bg-rendly-surface px-1 rounded">{notif.recipient_id?.slice(0, 8)}...</code>
                </span>
                <span className="text-text-muted text-xs ml-auto">{formatDate(notif.created_at)}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { label: 'Posts cargados', value: posts.length, color: 'text-text-primary' },
          { label: 'Con imágenes', value: posts.filter(p => p.images?.length > 0).length, color: 'text-blue-400' },
          { label: 'Notif. enviadas', value: sentNotifications.length, color: 'text-green-400' },
          { label: 'Estado', value: realtimeStatus === 'connected' ? '🟢' : '🟡', color: 'text-text-primary' },
        ].map(stat => (
          <div key={stat.label} className="bg-rendly-surface p-4 rounded-xl">
            <p className="text-text-muted text-sm">{stat.label}</p>
            <p className={`text-2xl font-bold ${stat.color}`}>{stat.value}</p>
          </div>
        ))}
      </div>

      {/* Posts grid */}
      <div className="space-y-4">
        <h3 className="font-semibold text-text-primary flex items-center gap-2">
          <Image className="w-5 h-5" />
          Posts disponibles - Click en ❤️ para enviar notificación de like
        </h3>
        
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <RefreshCw className="w-8 h-8 text-primary animate-spin" />
          </div>
        ) : posts.length === 0 ? (
          <div className="text-center py-12 bg-rendly-surface rounded-xl">
            <Image className="w-16 h-16 text-text-muted mx-auto mb-4" />
            <h3 className="text-lg font-medium text-text-primary mb-2">No hay posts</h3>
            <p className="text-text-muted">
              No se encontraron posts en la base de datos
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {posts.map(post => (
              <div 
                key={post.id} 
                className="bg-rendly-surface rounded-xl overflow-hidden border border-primary/10 hover:border-primary/30 transition-colors"
              >
                {/* Image */}
                {post.images?.[0] ? (
                  <div className="aspect-square bg-rendly-bg">
                    <img 
                      src={post.images[0]} 
                      alt={post.title || 'Post'}
                      className="w-full h-full object-cover"
                    />
                  </div>
                ) : (
                  <div className="aspect-square bg-rendly-bg flex items-center justify-center">
                    <Image className="w-12 h-12 text-text-muted" />
                  </div>
                )}

                {/* Content */}
                <div className="p-4 space-y-3">
                  <h4 className="font-semibold text-text-primary line-clamp-1">
                    {post.title || 'Sin título'}
                  </h4>
                  
                  <div className="flex items-center gap-2 text-text-muted text-sm">
                    <User className="w-4 h-4" />
                    <code className="text-xs bg-rendly-bg px-1 rounded">
                      {post.user_id?.slice(0, 12)}...
                    </code>
                  </div>

                  <div className="flex items-center gap-2 text-text-muted text-xs">
                    <Clock className="w-3 h-3" />
                    {formatDate(post.created_at)}
                  </div>

                  {/* Like button */}
                  <button
                    onClick={() => sendLikeNotification(post)}
                    disabled={actionLoading === post.id}
                    className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-red-500/20 text-red-400 rounded-lg hover:bg-red-500/30 transition-colors disabled:opacity-50"
                  >
                    {actionLoading === post.id ? (
                      <RefreshCw className="w-4 h-4 animate-spin" />
                    ) : (
                      <Heart className="w-4 h-4" />
                    )}
                    Dar Like (enviar notificación)
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default NotificationTest
