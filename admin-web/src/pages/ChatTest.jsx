import { useState, useEffect, useRef } from 'react'
import { 
  MessageSquare,
  Search,
  Send,
  RefreshCw,
  Zap,
  User,
  CheckCircle,
  Clock,
  ArrowLeft,
  UserCircle
} from 'lucide-react'
import { supabase } from '../supabaseClient'

// URL de Supabase para Edge Functions (FCM v1 API)
const SUPABASE_URL = 'https://xyrpmmnegzjkbysoocpc.supabase.co'
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5cnBtbW5lZ3pqa2J5c29vY3BjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjczOTA1NDcsImV4cCI6MjA4Mjk2NjU0N30.RsQE2JCJbHoNzlqG95Hf4W0QNyx5Xzw5bwvYcfpWKI0'

// Función para enviar push notification via Supabase Edge Function (FCM v1 API)
async function sendPushNotification(tokens, title, body, data = {}) {
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
      body: JSON.stringify({ tokens, title, body, data }),
    })

    const result = await response.json()
    console.log('📬 FCM v1 Response:', result)
    return result.success ? { success: true, result } : { success: false, error: result.error || 'Error desconocido' }
  } catch (error) {
    console.error('❌ Error enviando push:', error)
    return { success: false, error: error.message }
  }
}

// Obtener tokens FCM de un usuario usando función RPC
async function getUserFCMTokens(userId) {
  const { data, error } = await supabase.rpc('get_user_fcm_tokens', { target_user_id: userId })
  if (error) {
    console.error('Error obteniendo tokens via RPC:', error)
    const { data: fallbackData } = await supabase
      .from('fcm_tokens')
      .select('token')
      .eq('user_id', userId)
      .eq('is_active', true)
    return fallbackData?.map(t => t.token) || []
  }
  return data?.map(t => t.token) || []
}

function ChatTest() {
  const [users, setUsers] = useState([])
  const [searchQuery, setSearchQuery] = useState('')
  const [searching, setSearching] = useState(false)
  const [selectedUser, setSelectedUser] = useState(null)
  const [senderUser, setSenderUser] = useState(null) // Usuario que "envía" el mensaje
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [sending, setSending] = useState(false)
  const [realtimeStatus, setRealtimeStatus] = useState('connecting')
  const [notification, setNotification] = useState(null)
  const [currentUser, setCurrentUser] = useState(null)
  const [conversationId, setConversationId] = useState(null)
  const messagesEndRef = useRef(null)

  useEffect(() => {
    getCurrentUser()
    
    // Suscribirse a cambios en mensajes
    const channel = supabase
      .channel('chat-test-channel')
      .on(
        'postgres_changes',
        {
          event: 'INSERT',
          schema: 'public',
          table: 'messages'
        },
        (payload) => {
          console.log('🔔 Nuevo mensaje:', payload.new)
          if (conversationId && payload.new.conversation_id === conversationId) {
            setMessages(prev => [...prev, payload.new])
          }
        }
      )
      .subscribe((status) => {
        console.log('Realtime status:', status)
        setRealtimeStatus(status === 'SUBSCRIBED' ? 'connected' : status)
      })

    return () => {
      supabase.removeChannel(channel)
    }
  }, [conversationId])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function getCurrentUser() {
    const { data: { user } } = await supabase.auth.getUser()
    setCurrentUser(user)
  }

  function showNotificationToast(message, type = 'info') {
    setNotification({ message, type })
    setTimeout(() => setNotification(null), 4000)
  }

  async function searchUsers() {
    if (!searchQuery.trim()) return
    
    setSearching(true)
    try {
      const { data, error } = await supabase
        .from('usuarios')
        .select('*')
        .ilike('username', `%${searchQuery}%`)
        .limit(20)

      if (error) throw error
      setUsers(data || [])
    } catch (error) {
      console.error('Error searching users:', error)
      showNotificationToast('Error buscando usuarios: ' + error.message, 'error')
    } finally {
      setSearching(false)
    }
  }

  async function selectRecipient(user) {
    setSelectedUser(user)
    setConversationId(null)
    setMessages([])
    
    // Si ya hay un remitente, buscar o crear conversación
    if (senderUser) {
      await findOrCreateConversation(senderUser.user_id, user.user_id)
    }
  }

  async function selectSender(user) {
    setSenderUser(user)
    
    // Si ya hay un destinatario, buscar o crear conversación
    if (selectedUser) {
      await findOrCreateConversation(user.user_id, selectedUser.user_id)
    }
  }

  async function findOrCreateConversation(senderId, recipientId) {
    setLoading(true)
    try {
      // Buscar conversación existente entre estos dos usuarios
      const { data: senderParticipations } = await supabase
        .from('conversation_participants')
        .select('conversation_id')
        .eq('user_id', senderId)

      const senderConvIds = senderParticipations?.map(p => p.conversation_id) || []

      if (senderConvIds.length > 0) {
        const { data: recipientParticipations } = await supabase
          .from('conversation_participants')
          .select('conversation_id')
          .eq('user_id', recipientId)
          .in('conversation_id', senderConvIds)

        if (recipientParticipations?.length > 0) {
          // Conversación encontrada
          const convId = recipientParticipations[0].conversation_id
          setConversationId(convId)
          await loadMessages(convId)
          showNotificationToast('✅ Conversación existente encontrada', 'success')
          return
        }
      }

      // Crear nueva conversación
      const { data: newConv, error: convError } = await supabase
        .from('conversations')
        .insert({})
        .select()
        .single()

      if (convError) throw convError

      // Agregar participantes
      await supabase.from('conversation_participants').insert([
        { conversation_id: newConv.id, user_id: senderId },
        { conversation_id: newConv.id, user_id: recipientId }
      ])

      setConversationId(newConv.id)
      setMessages([])
      showNotificationToast('✅ Nueva conversación creada', 'success')

    } catch (error) {
      console.error('Error finding/creating conversation:', error)
      showNotificationToast('Error: ' + error.message, 'error')
    } finally {
      setLoading(false)
    }
  }

  async function loadMessages(convId) {
    try {
      const { data, error } = await supabase
        .from('messages')
        .select('*')
        .eq('conversation_id', convId)
        .order('created_at', { ascending: true })
        .limit(50)

      if (error) throw error
      setMessages(data || [])
    } catch (error) {
      console.error('Error loading messages:', error)
    }
  }

  async function sendMessage() {
    if (!newMessage.trim() || !conversationId || !senderUser) return

    setSending(true)
    try {
      const now = new Date().toISOString()

      // Insertar mensaje
      const { error: msgError } = await supabase
        .from('messages')
        .insert({
          conversation_id: conversationId,
          sender_id: senderUser.user_id,
          content: newMessage.trim(),
          is_read: false
        })

      if (msgError) throw msgError

      // Actualizar conversación
      await supabase
        .from('conversations')
        .update({
          last_message: newMessage.trim(),
          last_message_at: now,
          updated_at: now
        })
        .eq('id', conversationId)

      // Incrementar unread_count del destinatario
      const { data: participantData } = await supabase
        .from('conversation_participants')
        .select('id, unread_count')
        .eq('conversation_id', conversationId)
        .eq('user_id', selectedUser.user_id)
        .single()

      if (participantData) {
        await supabase
          .from('conversation_participants')
          .update({ unread_count: (participantData.unread_count || 0) + 1 })
          .eq('id', participantData.id)
      }

      setNewMessage('')
      
      // 🔔 ENVIAR PUSH NOTIFICATION
      const tokens = await getUserFCMTokens(selectedUser.user_id)
      console.log(`📱 Tokens FCM encontrados: ${tokens.length}`)
      
      if (tokens.length > 0) {
        const pushResult = await sendPushNotification(
          tokens,
          senderUser.username || 'Nuevo mensaje',
          newMessage.trim().slice(0, 100),
          {
            type: 'message',
            sender_id: senderUser.user_id,
            sender_username: senderUser.username,
            conversation_id: conversationId
          }
        )
        
        if (pushResult.success) {
          showNotificationToast(`✅ Mensaje + Push enviado a @${selectedUser.username}`, 'success')
        } else {
          showNotificationToast(`✅ Mensaje enviado (push falló)`, 'warning')
        }
      } else {
        showNotificationToast(`✅ Mensaje enviado a @${selectedUser.username}`, 'success')
      }

    } catch (error) {
      console.error('Error sending message:', error)
      showNotificationToast('Error: ' + error.message, 'error')
    } finally {
      setSending(false)
    }
  }

  function formatDate(dateStr) {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  function resetSelection() {
    setSelectedUser(null)
    setSenderUser(null)
    setConversationId(null)
    setMessages([])
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-500/20 flex items-center justify-center">
              <MessageSquare className="w-6 h-6 text-blue-400" />
            </div>
            Test de Chat
          </h1>
          <p className="text-text-muted mt-1">
            Envía mensajes como cualquier usuario a cualquier otro usuario. Verifica el badge en la app.
          </p>
        </div>

        <div className="flex items-center gap-3">
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
      <div className="bg-blue-500/10 border border-blue-500/30 rounded-xl p-4">
        <h3 className="font-semibold text-blue-400 mb-2">💬 Cómo funciona:</h3>
        <ol className="list-decimal list-inside space-y-1 text-text-secondary text-sm">
          <li>Busca y selecciona el usuario <strong>destinatario</strong> (quien recibirá el mensaje)</li>
          <li>Busca y selecciona el usuario <strong>remitente</strong> (en nombre de quién envías)</li>
          <li>Escribe un mensaje y envía</li>
          <li>El destinatario verá el badge azul con el número en el icono de chat 💬</li>
        </ol>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* User Search Panel */}
        <div className="bg-rendly-surface rounded-xl p-4 space-y-4">
          <h3 className="font-semibold text-text-primary flex items-center gap-2">
            <Search className="w-5 h-5" />
            Buscar usuarios
          </h3>

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

          {/* User list */}
          <div className="space-y-2 max-h-80 overflow-y-auto">
            {users.length === 0 ? (
              <p className="text-text-muted text-sm text-center py-4">
                Busca usuarios para empezar
              </p>
            ) : (
              users.map(user => (
                <div 
                  key={user.user_id}
                  className="flex items-center gap-3 p-3 bg-rendly-bg rounded-lg hover:bg-primary/10 transition-colors"
                >
                  {user.avatar_url ? (
                    <img 
                      src={user.avatar_url} 
                      alt={user.username}
                      className="w-10 h-10 rounded-full object-cover"
                    />
                  ) : (
                    <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                      <User className="w-5 h-5 text-primary" />
                    </div>
                  )}
                  
                  <div className="flex-1">
                    <p className="text-text-primary font-medium">@{user.username}</p>
                    <p className="text-text-muted text-xs">
                      <code>{user.user_id?.slice(0, 12)}...</code>
                    </p>
                  </div>

                  <div className="flex gap-2">
                    <button
                      onClick={() => selectRecipient(user)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                        selectedUser?.user_id === user.user_id
                          ? 'bg-green-500/20 text-green-400'
                          : 'bg-rendly-surface text-text-secondary hover:bg-green-500/20 hover:text-green-400'
                      }`}
                    >
                      {selectedUser?.user_id === user.user_id ? '✓ Destino' : 'Destino'}
                    </button>
                    <button
                      onClick={() => selectSender(user)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                        senderUser?.user_id === user.user_id
                          ? 'bg-blue-500/20 text-blue-400'
                          : 'bg-rendly-surface text-text-secondary hover:bg-blue-500/20 hover:text-blue-400'
                      }`}
                    >
                      {senderUser?.user_id === user.user_id ? '✓ Remitente' : 'Remitente'}
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Chat Panel */}
        <div className="bg-rendly-surface rounded-xl p-4 flex flex-col h-[500px]">
          {/* Chat header */}
          <div className="flex items-center gap-3 pb-4 border-b border-primary/10">
            {selectedUser && senderUser ? (
              <>
                <button onClick={resetSelection} className="p-2 hover:bg-rendly-bg rounded-lg transition-colors">
                  <ArrowLeft className="w-4 h-4 text-text-muted" />
                </button>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <span className="text-blue-400 text-xs font-medium">De:</span>
                    <span className="text-text-primary font-medium">@{senderUser.username}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-400 text-xs font-medium">Para:</span>
                    <span className="text-text-primary font-medium">@{selectedUser.username}</span>
                  </div>
                </div>
                {loading && <RefreshCw className="w-4 h-4 text-primary animate-spin" />}
              </>
            ) : (
              <div className="flex-1 text-center text-text-muted">
                <UserCircle className="w-12 h-12 mx-auto mb-2 opacity-50" />
                <p className="text-sm">Selecciona remitente y destinatario</p>
              </div>
            )}
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto py-4 space-y-3">
            {messages.map((msg, idx) => {
              const isFromSender = msg.sender_id === senderUser?.user_id
              return (
                <div 
                  key={msg.id || idx}
                  className={`flex ${isFromSender ? 'justify-end' : 'justify-start'}`}
                >
                  <div className={`max-w-[80%] px-4 py-2 rounded-2xl ${
                    isFromSender 
                      ? 'bg-blue-500 text-white rounded-br-sm' 
                      : 'bg-rendly-bg text-text-primary rounded-bl-sm'
                  }`}>
                    <p className="text-sm">{msg.content}</p>
                    <p className={`text-xs mt-1 ${isFromSender ? 'text-blue-200' : 'text-text-muted'}`}>
                      {formatDate(msg.created_at)}
                    </p>
                  </div>
                </div>
              )
            })}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          {conversationId && (
            <div className="flex gap-2 pt-4 border-t border-primary/10">
              <input
                type="text"
                placeholder="Escribe un mensaje..."
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && sendMessage()}
                className="flex-1 px-4 py-2 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
              />
              <button
                onClick={sendMessage}
                disabled={sending || !newMessage.trim()}
                className="px-4 py-2 bg-blue-500 text-white rounded-xl hover:bg-blue-600 transition-colors disabled:opacity-50"
              >
                {sending ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Status cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-rendly-surface p-4 rounded-xl">
          <p className="text-text-muted text-sm">Remitente</p>
          <p className="text-lg font-bold text-blue-400">
            {senderUser ? `@${senderUser.username}` : '-'}
          </p>
        </div>
        <div className="bg-rendly-surface p-4 rounded-xl">
          <p className="text-text-muted text-sm">Destinatario</p>
          <p className="text-lg font-bold text-green-400">
            {selectedUser ? `@${selectedUser.username}` : '-'}
          </p>
        </div>
        <div className="bg-rendly-surface p-4 rounded-xl">
          <p className="text-text-muted text-sm">Mensajes</p>
          <p className="text-lg font-bold text-text-primary">{messages.length}</p>
        </div>
        <div className="bg-rendly-surface p-4 rounded-xl">
          <p className="text-text-muted text-sm">Conversación</p>
          <p className="text-lg font-bold text-text-primary">
            {conversationId ? '✅' : '❌'}
          </p>
        </div>
      </div>
    </div>
  )
}

export default ChatTest
