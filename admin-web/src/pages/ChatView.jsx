import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { 
  ArrowLeft, 
  Send, 
  Bot, 
  User,
  Clock,
  CheckCircle,
  AlertCircle,
  MoreVertical,
  Phone,
  Video
} from 'lucide-react'
import { 
  supabase, 
  getConversationWithMessages, 
  sendAgentMessage, 
  subscribeToMessages,
  createTypingChannel,
  broadcastAgentTyping,
  saveAgentResponseForLearning
} from '../supabaseClient'

function MessageBubble({ message, isFromAgent }) {
  const isUser = message.role === 'user'
  const isAI = message.role === 'ai'
  const isAgent = message.role === 'human_support'
  
  const bubbleColors = {
    user: 'bg-rendly-surface-elevated',
    ai: 'bg-primary/20 border border-primary/30',
    human_support: 'bg-accent-blue/20 border border-accent-blue/30',
    system: 'bg-accent-gold/10 border border-accent-gold/30'
  }
  
  const timeString = new Date(message.created_at).toLocaleTimeString('es', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
  
  return (
    <div className={`flex gap-3 ${isUser ? 'flex-row' : 'flex-row-reverse'}`}>
      {/* Avatar */}
      <div className={`
        w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0
        ${isUser ? 'bg-accent-magenta/20' : isAI ? 'bg-primary/20' : 'bg-accent-blue/20'}
      `}>
        {isUser ? (
          <User className="w-4 h-4 text-accent-magenta" />
        ) : isAI ? (
          <Bot className="w-4 h-4 text-primary" />
        ) : (
          <User className="w-4 h-4 text-accent-blue" />
        )}
      </div>
      
      {/* Bubble */}
      <div className={`max-w-[70%] ${isUser ? '' : 'text-right'}`}>
        <div className={`
          inline-block px-4 py-3 rounded-2xl text-left
          ${bubbleColors[message.role]}
        `}>
          {/* Role label */}
          <p className={`text-xs mb-1 ${isUser ? 'text-accent-magenta' : isAI ? 'text-primary' : 'text-accent-blue'}`}>
            {isUser ? 'Usuario' : isAI ? 'IA Rendly' : 'Agente de Soporte'}
          </p>
          
          {/* Content */}
          <p className="text-text-primary whitespace-pre-wrap">{message.content}</p>
          
          {/* Meta */}
          <div className="flex items-center gap-2 mt-2 text-text-muted text-xs">
            <span>{timeString}</span>
            {message.confidence_score && (
              <span className="text-primary">• {message.confidence_score}% confianza</span>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default function ChatView() {
  const { conversationId } = useParams()
  const navigate = useNavigate()
  const messagesEndRef = useRef(null)
  
  const [conversation, setConversation] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [sending, setSending] = useState(false)
  const [loading, setLoading] = useState(true)
  const [typingChannel, setTypingChannel] = useState(null)
  const typingTimeoutRef = useRef(null)
  
  useEffect(() => {
    console.log('ChatView mounted with conversationId:', conversationId)
    loadConversation()
    
    // Suscribirse a nuevos mensajes en tiempo real
    const subscription = subscribeToMessages(conversationId, (newMsg) => {
      // Evitar duplicados - solo agregar si no existe
      setMessages(prev => {
        if (prev.some(m => m.id === newMsg.id)) return prev
        return [...prev, newMsg]
      })
    })
    
    // Crear canal para typing broadcast
    const channel = createTypingChannel(conversationId)
    channel.subscribe()
    setTypingChannel(channel)
    
    return () => {
      subscription.unsubscribe()
      if (channel) {
        broadcastAgentTyping(channel, false)
        channel.unsubscribe()
      }
    }
  }, [conversationId])
  
  useEffect(() => {
    scrollToBottom()
  }, [messages])
  
  function scrollToBottom() {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }
  
  async function loadConversation() {
    setLoading(true)
    try {
      console.log('Loading conversation:', conversationId)
      
      // Cargar conversación
      const { data: conv, error: convError } = await supabase
        .from('support_conversations')
        .select('*')
        .eq('id', conversationId)
        .single()
      
      if (convError) {
        console.error('Error loading conversation data:', convError)
      }
      
      console.log('Conversation loaded:', conv)
      setConversation(conv)
      
      // Cargar mensajes
      const msgs = await getConversationWithMessages(conversationId)
      console.log('Messages loaded:', msgs?.length || 0, 'messages')
      setMessages(msgs || [])
      
    } catch (error) {
      console.error('Error loading conversation:', error)
    } finally {
      setLoading(false)
    }
  }
  
  async function handleSend() {
    if (!newMessage.trim() || sending) return
    
    setSending(true)
    try {
      const messageContent = newMessage.trim()
      await sendAgentMessage(conversationId, messageContent, 'agent-1')
      
      // NO agregar localmente - el mensaje llegará via realtime
      // Esto evita el problema de mensaje doble
      
      // Guardar respuesta para aprendizaje de IA
      await saveAgentResponseForLearning(conversationId, null, messageContent)
      
      // Dejar de mostrar typing
      if (typingChannel) {
        broadcastAgentTyping(typingChannel, false)
      }
      
      setNewMessage('')
      
      // Marcar como asignada si estaba pendiente
      if (conversation?.status === 'escalated') {
        await supabase
          .from('support_conversations')
          .update({ status: 'active' })
          .eq('id', conversationId)
      }
      
    } catch (error) {
      console.error('Error sending message:', error)
    } finally {
      setSending(false)
    }
  }
  
  async function handleResolve() {
    if (!window.confirm('¿Marcar esta conversación como resuelta? Se enviará un mensaje al usuario para calificar la atención.')) {
      return
    }
    
    try {
      // 1. Enviar mensaje de calificación al usuario
      const ratingMessage = `¡Gracias por contactarnos! 🎉

Tu consulta ha sido marcada como resuelta por nuestro equipo de soporte.

¿Cómo calificarías la atención recibida?
⭐ Responde con un número del 1 al 5:
1 = Muy malo
2 = Malo  
3 = Regular
4 = Bueno
5 = Excelente

Tu opinión nos ayuda a mejorar. ¡Gracias!`

      await supabase
        .from('support_messages')
        .insert({
          conversation_id: conversationId,
          role: 'system',
          content: ratingMessage
        })
      
      // 2. Actualizar estado de la conversación
      await supabase
        .from('support_conversations')
        .update({ 
          status: 'resolved',
          resolved_by: 'human'
        })
        .eq('id', conversationId)
      
      // 3. Actualizar escalación como resuelta
      await supabase
        .from('ai_escalations')
        .update({ 
          status: 'resolved',
          resolved_at: new Date().toISOString()
        })
        .eq('conversation_id', conversationId)
      
      // Agregar mensaje local y mostrar confirmación
      setMessages(prev => [...prev, {
        id: `system_${Date.now()}`,
        role: 'system',
        content: ratingMessage,
        created_at: new Date().toISOString()
      }])
      
      alert('✅ Conversación marcada como resuelta. Se envió solicitud de calificación al usuario.')
      
    } catch (error) {
      console.error('Error resolving:', error)
      alert('Error al resolver la conversación')
    }
  }
  
  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }
  
  if (!conversation) {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4">
        <AlertCircle className="w-16 h-16 text-accent-gold" />
        <p className="text-text-secondary text-lg">Conversación no encontrada</p>
        <button 
          onClick={() => navigate('/escalations')}
          className="px-4 py-2 bg-primary text-white rounded-xl hover:bg-primary/80 transition-colors"
        >
          Volver a escalaciones
        </button>
      </div>
    )
  }
  
  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] fade-in">
      {/* Header */}
      <div className="flex items-center justify-between p-4 bg-rendly-surface rounded-t-2xl border border-primary/10 border-b-0">
        <div className="flex items-center gap-4">
          <button 
            onClick={() => navigate(-1)}
            className="p-2 text-text-secondary hover:text-text-primary hover:bg-rendly-surface-elevated rounded-xl transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
              <User className="w-5 h-5 text-primary" />
            </div>
            <div>
              <p className="text-text-primary font-medium">
                Usuario {conversation?.user_id?.substring(0, 8)}...
              </p>
              <div className="flex items-center gap-2 text-text-tertiary text-sm">
                <span className={`
                  w-2 h-2 rounded-full
                  ${conversation?.status === 'active' ? 'bg-accent-green' : 
                    conversation?.status === 'escalated' ? 'bg-accent-gold' : 'bg-text-muted'}
                `} />
                <span className="capitalize">{conversation?.status}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div className="flex items-center gap-2">
          <button 
            onClick={handleResolve}
            className="flex items-center gap-2 px-4 py-2 bg-accent-green/20 text-accent-green rounded-xl hover:bg-accent-green/30 transition-colors"
          >
            <CheckCircle className="w-4 h-4" />
            <span className="text-sm font-medium hidden sm:inline">Resolver</span>
          </button>
        </div>
      </div>
      
      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 bg-rendly-bg border-x border-primary/10 space-y-4">
        {messages.length === 0 ? (
          <div className="text-center py-8 text-text-tertiary">
            No hay mensajes en esta conversación
          </div>
        ) : (
          messages.map((message) => (
            <MessageBubble key={message.id} message={message} />
          ))
        )}
        <div ref={messagesEndRef} />
      </div>
      
      {/* Input */}
      <div className="p-4 bg-rendly-surface rounded-b-2xl border border-primary/10 border-t-0">
        <div className="flex items-center gap-3">
          <input
            type="text"
            value={newMessage}
            onChange={(e) => {
              setNewMessage(e.target.value)
              // Broadcast typing event
              if (typingChannel && e.target.value.trim()) {
                broadcastAgentTyping(typingChannel, true)
                // Reset typing after 2 seconds of inactivity
                if (typingTimeoutRef.current) {
                  clearTimeout(typingTimeoutRef.current)
                }
                typingTimeoutRef.current = setTimeout(() => {
                  broadcastAgentTyping(typingChannel, false)
                }, 2000)
              } else if (typingChannel) {
                broadcastAgentTyping(typingChannel, false)
              }
            }}
            onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSend()}
            placeholder="Escribe tu respuesta al usuario..."
            className="flex-1 px-4 py-3 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
          />
          <button
            onClick={handleSend}
            disabled={!newMessage.trim() || sending}
            className={`
              p-3 rounded-xl transition-all duration-200
              ${newMessage.trim() && !sending
                ? 'bg-primary text-white hover:bg-primary-dark glow-purple'
                : 'bg-rendly-surface-elevated text-text-muted cursor-not-allowed'
              }
            `}
          >
            <Send className="w-5 h-5" />
          </button>
        </div>
        <p className="text-text-muted text-xs mt-2 text-center">
          Los mensajes que envíes aparecerán en el chat del usuario en tiempo real
        </p>
      </div>
    </div>
  )
}
