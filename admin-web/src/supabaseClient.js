import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://xyrpmmnegzjkbysoocpc.supabase.co'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5cnBtbW5lZ3pqa2J5c29vY3BjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjczOTA1NDcsImV4cCI6MjA4Mjk2NjU0N30.RsQE2JCJbHoNzlqG95Hf4W0QNyx5Xzw5bwvYcfpWKI0'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)

// Helper para obtener conversación completa
export async function getConversationWithMessages(conversationId) {
  const { data: messages, error } = await supabase
    .from('support_messages')
    .select('*')
    .eq('conversation_id', conversationId)
    .order('created_at', { ascending: true })
  
  if (error) throw error
  return messages
}

// Helper para enviar mensaje como agente de soporte
export async function sendAgentMessage(conversationId, content, agentId) {
  const { data, error } = await supabase
    .from('support_messages')
    .insert({
      conversation_id: conversationId,
      role: 'human_support',
      content: content
    })
    .select()
  
  if (error) throw error
  return data[0]
}

// Helper para marcar escalación como resuelta
export async function resolveEscalation(escalationId, resolutionNotes) {
  const { data, error } = await supabase
    .from('ai_escalations')
    .update({
      status: 'resolved',
      resolved_at: new Date().toISOString(),
      resolution_notes: resolutionNotes
    })
    .eq('id', escalationId)
    .select()
  
  if (error) throw error
  return data[0]
}

// Suscribirse a nuevos mensajes en tiempo real
export function subscribeToMessages(conversationId, callback) {
  // Use same channel name as Android app for consistency
  return supabase
    .channel(`support-${conversationId}`)
    .on(
      'postgres_changes',
      {
        event: 'INSERT',
        schema: 'public',
        table: 'support_messages',
        filter: `conversation_id=eq.${conversationId}`
      },
      (payload) => callback(payload.new)
    )
    .subscribe()
}

// Suscribirse a nuevas escalaciones
export function subscribeToEscalations(callback) {
  return supabase
    .channel('escalations')
    .on(
      'postgres_changes',
      {
        event: 'INSERT',
        schema: 'public',
        table: 'ai_escalations'
      },
      (payload) => callback(payload.new)
    )
    .subscribe()
}

// Crear canal para typing broadcast
export function createTypingChannel(conversationId) {
  const channelName = `support-${conversationId}`
  return supabase.channel(channelName)
}

// Enviar evento de typing al usuario
export async function broadcastAgentTyping(channel, isTyping) {
  try {
    await channel.send({
      type: 'broadcast',
      event: 'agent_typing',
      payload: { is_typing: isTyping }
    })
  } catch (error) {
    console.error('Error broadcasting typing:', error)
  }
}

// Guardar respuesta del agente para aprendizaje de IA
export async function saveAgentResponseForLearning(conversationId, userMessage, agentResponse) {
  try {
    // Obtener el último mensaje del usuario antes de la respuesta del agente
    const { data: messages } = await supabase
      .from('support_messages')
      .select('*')
      .eq('conversation_id', conversationId)
      .eq('role', 'user')
      .order('created_at', { ascending: false })
      .limit(1)
    
    if (messages && messages.length > 0) {
      const lastUserMessage = messages[0].content
      
      // Guardar en ai_feedback como ejemplo de aprendizaje
      await supabase
        .from('ai_feedback')
        .insert({
          conversation_id: conversationId,
          message_id: messages[0].id,
          feedback_type: 'agent_response',
          user_message: lastUserMessage,
          agent_response: agentResponse,
          helpful: true
        })
      
      console.log('✓ Respuesta guardada para aprendizaje de IA')
    }
  } catch (error) {
    console.error('Error guardando para aprendizaje:', error)
  }
}
