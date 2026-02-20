import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { 
  AlertCircle, 
  Clock, 
  MessageSquare,
  User,
  CheckCircle,
  XCircle,
  Filter,
  Search
} from 'lucide-react'
import { supabase, subscribeToEscalations } from '../supabaseClient'

function EscalationCard({ escalation, onResolve }) {
  const timeAgo = (date) => {
    const seconds = Math.floor((new Date() - new Date(date)) / 1000)
    if (seconds < 60) return 'Hace un momento'
    if (seconds < 3600) return `Hace ${Math.floor(seconds / 60)} min`
    if (seconds < 86400) return `Hace ${Math.floor(seconds / 3600)}h`
    return `Hace ${Math.floor(seconds / 86400)} días`
  }
  
  const statusColors = {
    pending: 'bg-accent-gold/20 text-accent-gold',
    assigned: 'bg-accent-blue/20 text-accent-blue',
    resolved: 'bg-accent-green/20 text-accent-green',
    closed: 'bg-text-muted/20 text-text-muted'
  }
  
  const statusLabels = {
    pending: 'Pendiente',
    assigned: 'Asignada',
    resolved: 'Resuelta',
    closed: 'Cerrada'
  }
  
  return (
    <div className="bg-rendly-surface rounded-2xl border border-primary/10 overflow-hidden hover:border-primary/30 transition-all duration-300">
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-primary/10">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-accent-magenta/20 flex items-center justify-center">
            <User className="w-5 h-5 text-accent-magenta" />
          </div>
          <div>
            <p className="text-text-primary font-medium">Usuario</p>
            <p className="text-text-tertiary text-sm">{escalation.user_id?.substring(0, 8)}...</p>
          </div>
        </div>
        <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusColors[escalation.status]}`}>
          {statusLabels[escalation.status]}
        </span>
      </div>
      
      {/* Content */}
      <div className="p-4 space-y-3">
        {/* Reason */}
        <div>
          <p className="text-text-tertiary text-xs mb-1">Razón de escalación</p>
          <p className="text-text-primary">{escalation.reason}</p>
        </div>
        
        {/* Last message */}
        {escalation.last_message && (
          <div className="bg-rendly-bg rounded-xl p-3">
            <p className="text-text-tertiary text-xs mb-1">Último mensaje</p>
            <p className="text-text-secondary text-sm line-clamp-2">{escalation.last_message}</p>
          </div>
        )}
        
        {/* Meta */}
        <div className="flex items-center gap-4 text-text-muted text-sm">
          <div className="flex items-center gap-1">
            <Clock className="w-4 h-4" />
            <span>{timeAgo(escalation.created_at)}</span>
          </div>
          {escalation.confidence_score && (
            <div className="flex items-center gap-1">
              <AlertCircle className="w-4 h-4" />
              <span>Confianza: {escalation.confidence_score}%</span>
            </div>
          )}
        </div>
      </div>
      
      {/* Actions */}
      <div className="flex border-t border-primary/10">
        <Link 
          to={`/chat/${escalation.conversation_id}`}
          className="flex-1 flex items-center justify-center gap-2 py-3 text-primary hover:bg-primary/10 transition-colors"
        >
          <MessageSquare className="w-4 h-4" />
          <span className="text-sm font-medium">Abrir Chat</span>
        </Link>
        {escalation.status === 'pending' && (
          <button 
            onClick={() => onResolve(escalation.id)}
            className="flex-1 flex items-center justify-center gap-2 py-3 text-accent-green hover:bg-accent-green/10 transition-colors border-l border-primary/10"
          >
            <CheckCircle className="w-4 h-4" />
            <span className="text-sm font-medium">Resolver</span>
          </button>
        )}
      </div>
    </div>
  )
}

export default function Escalations() {
  const [escalations, setEscalations] = useState([])
  const [filter, setFilter] = useState('all')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    loadEscalations()
    
    // Suscribirse a nuevas escalaciones
    const subscription = subscribeToEscalations((newEscalation) => {
      setEscalations(prev => [newEscalation, ...prev])
    })
    
    return () => {
      subscription.unsubscribe()
    }
  }, [filter])
  
  async function loadEscalations() {
    setLoading(true)
    try {
      let query = supabase
        .from('ai_escalations')
        .select('*')
        .order('created_at', { ascending: false })
      
      if (filter !== 'all') {
        query = query.eq('status', filter)
      }
      
      const { data, error } = await query
      
      if (error) throw error
      
      // Cargar último mensaje de cada conversación
      const escalationsWithMessages = await Promise.all(
        (data || []).map(async (e) => {
          const { data: messages } = await supabase
            .from('support_messages')
            .select('content')
            .eq('conversation_id', e.conversation_id)
            .order('created_at', { ascending: false })
            .limit(1)
          
          return {
            ...e,
            last_message: messages?.[0]?.content
          }
        })
      )
      
      setEscalations(escalationsWithMessages)
    } catch (error) {
      console.error('Error loading escalations:', error)
    } finally {
      setLoading(false)
    }
  }
  
  async function handleResolve(escalationId) {
    try {
      // 1. Obtener info de la escalación
      const { data: escalation } = await supabase
        .from('ai_escalations')
        .select('conversation_id, user_id')
        .eq('id', escalationId)
        .single()
      
      if (!escalation) {
        alert('Error: Escalación no encontrada')
        return
      }

      // 2. Enviar mensaje especial que activa el rating interactivo
      const feedbackMessage = `__RATING_REQUEST__`

      await supabase
        .from('support_messages')
        .insert({
          conversation_id: escalation.conversation_id,
          role: 'system',
          content: feedbackMessage
        })
      
      console.log('✅ Mensaje de calificación enviado al usuario')
      
      // 3. ELIMINAR la escalación inmediatamente de la lista local
      setEscalations(prev => prev.filter(e => e.id !== escalationId))
      
      // 4. ELIMINAR la escalación de la base de datos (NO esperar)
      const { error: deleteEscError } = await supabase
        .from('ai_escalations')
        .delete()
        .eq('id', escalationId)
      
      if (deleteEscError) {
        console.error('Error eliminando escalación:', deleteEscError)
      } else {
        console.log('🗑️ Escalación eliminada de la base de datos')
      }
      
      // NOTA: NO eliminamos support_conversations ni support_messages
      // porque ai_feedback necesita estos datos para el aprendizaje de IA
      // La conversación se archivará automáticamente después de 24h
      
      console.log('✅ Chat resuelto. El feedback se guardará cuando el usuario califique.')
      
    } catch (error) {
      console.error('❌ Error resolving:', error)
      alert(`Error: ${error.message}`)
      // Recargar en caso de error para sincronizar estado
      loadEscalations()
    }
  }
  
  const filteredEscalations = escalations.filter(e => 
    !search || 
    e.reason?.toLowerCase().includes(search.toLowerCase()) ||
    e.user_id?.toLowerCase().includes(search.toLowerCase())
  )
  
  const filterButtons = [
    { key: 'pending', label: 'Pendientes', count: escalations.filter(e => e.status === 'pending').length },
    { key: 'resolved', label: 'Resueltas', count: escalations.filter(e => e.status === 'resolved').length },
    { key: 'all', label: 'Todas', count: escalations.length },
  ]
  
  return (
    <div className="space-y-6 fade-in">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Escalaciones</h1>
          <p className="text-text-tertiary">Conversaciones que necesitan atención humana</p>
        </div>
        
        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-text-muted" />
          <input
            type="text"
            placeholder="Buscar..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10 pr-4 py-2 bg-rendly-surface border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary w-full md:w-64"
          />
        </div>
      </div>
      
      {/* Filters */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {filterButtons.map(({ key, label, count }) => (
          <button
            key={key}
            onClick={() => setFilter(key)}
            className={`
              flex items-center gap-2 px-4 py-2 rounded-xl whitespace-nowrap transition-all
              ${filter === key 
                ? 'bg-primary text-white' 
                : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
              }
            `}
          >
            <span>{label}</span>
            <span className={`
              px-2 py-0.5 rounded-full text-xs
              ${filter === key ? 'bg-white/20' : 'bg-primary/20 text-primary'}
            `}>
              {count}
            </span>
          </button>
        ))}
      </div>
      
      {/* Grid */}
      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        </div>
      ) : filteredEscalations.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredEscalations.map((escalation) => (
            <EscalationCard 
              key={escalation.id} 
              escalation={escalation}
              onResolve={handleResolve}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-16 bg-rendly-surface rounded-2xl border border-primary/10">
          <CheckCircle className="w-16 h-16 mx-auto mb-4 text-accent-green" />
          <h3 className="text-xl font-semibold text-text-primary mb-2">
            ¡Todo al día!
          </h3>
          <p className="text-text-tertiary">
            No hay escalaciones {filter === 'pending' ? 'pendientes' : ''} en este momento
          </p>
        </div>
      )}
    </div>
  )
}
