import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { 
  MessageSquare, 
  Bot, 
  User,
  Clock,
  CheckCircle,
  AlertCircle,
  Search,
  Filter
} from 'lucide-react'
import { supabase } from '../supabaseClient'

function ConversationRow({ conversation }) {
  const timeAgo = (date) => {
    const seconds = Math.floor((new Date() - new Date(date)) / 1000)
    if (seconds < 60) return 'Ahora'
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`
    return new Date(date).toLocaleDateString()
  }
  
  const statusIcons = {
    active: <Clock className="w-4 h-4 text-accent-gold" />,
    resolved: <CheckCircle className="w-4 h-4 text-accent-green" />,
    escalated: <AlertCircle className="w-4 h-4 text-accent-magenta" />
  }
  
  const statusColors = {
    active: 'text-accent-gold',
    resolved: 'text-accent-green',
    escalated: 'text-accent-magenta'
  }
  
  return (
    <Link 
      to={`/chat/${conversation.id}`}
      className="flex items-center gap-4 p-4 bg-rendly-surface rounded-xl border border-primary/10 hover:border-primary/30 hover:bg-rendly-surface-elevated transition-all duration-200"
    >
      {/* Avatar */}
      <div className="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center flex-shrink-0">
        <User className="w-6 h-6 text-primary" />
      </div>
      
      {/* Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <p className="text-text-primary font-medium truncate">
            {conversation.user_id?.substring(0, 12)}...
          </p>
          <span className={`flex items-center gap-1 text-xs ${statusColors[conversation.status]}`}>
            {statusIcons[conversation.status]}
            <span className="capitalize">{conversation.status}</span>
          </span>
        </div>
        <p className="text-text-tertiary text-sm truncate">
          {conversation.last_message || 'Sin mensajes'}
        </p>
      </div>
      
      {/* Meta */}
      <div className="flex flex-col items-end gap-1 flex-shrink-0">
        <span className="text-text-muted text-sm">{timeAgo(conversation.updated_at)}</span>
        <div className="flex items-center gap-1 text-text-tertiary text-xs">
          <MessageSquare className="w-3 h-3" />
          <span>{conversation.message_count || 0}</span>
        </div>
      </div>
    </Link>
  )
}

export default function Conversations() {
  const [conversations, setConversations] = useState([])
  const [filter, setFilter] = useState('all')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    loadConversations()
  }, [filter])
  
  async function loadConversations() {
    setLoading(true)
    try {
      let query = supabase
        .from('support_conversations')
        .select('*')
        .order('updated_at', { ascending: false })
        .limit(50)
      
      if (filter !== 'all') {
        query = query.eq('status', filter)
      }
      
      const { data, error } = await query
      
      if (error) throw error
      
      // Cargar conteo de mensajes y último mensaje
      const conversationsWithMeta = await Promise.all(
        (data || []).map(async (conv) => {
          const { data: messages, count } = await supabase
            .from('support_messages')
            .select('content', { count: 'exact' })
            .eq('conversation_id', conv.id)
            .order('created_at', { ascending: false })
            .limit(1)
          
          return {
            ...conv,
            message_count: count || 0,
            last_message: messages?.[0]?.content
          }
        })
      )
      
      setConversations(conversationsWithMeta)
    } catch (error) {
      console.error('Error loading conversations:', error)
    } finally {
      setLoading(false)
    }
  }
  
  const filteredConversations = conversations.filter(c => 
    !search || 
    c.user_id?.toLowerCase().includes(search.toLowerCase())
  )
  
  const filterButtons = [
    { key: 'all', label: 'Todas' },
    { key: 'active', label: 'Activas' },
    { key: 'escalated', label: 'Escaladas' },
    { key: 'resolved', label: 'Resueltas' },
  ]
  
  return (
    <div className="space-y-6 fade-in">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Conversaciones</h1>
          <p className="text-text-tertiary">Historial de todas las conversaciones de soporte</p>
        </div>
        
        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-text-muted" />
          <input
            type="text"
            placeholder="Buscar por usuario..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10 pr-4 py-2 bg-rendly-surface border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary w-full md:w-64"
          />
        </div>
      </div>
      
      {/* Filters */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {filterButtons.map(({ key, label }) => (
          <button
            key={key}
            onClick={() => setFilter(key)}
            className={`
              px-4 py-2 rounded-xl whitespace-nowrap transition-all
              ${filter === key 
                ? 'bg-primary text-white' 
                : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
              }
            `}
          >
            {label}
          </button>
        ))}
      </div>
      
      {/* List */}
      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        </div>
      ) : filteredConversations.length > 0 ? (
        <div className="space-y-3">
          {filteredConversations.map((conversation) => (
            <ConversationRow key={conversation.id} conversation={conversation} />
          ))}
        </div>
      ) : (
        <div className="text-center py-16 bg-rendly-surface rounded-2xl border border-primary/10">
          <MessageSquare className="w-16 h-16 mx-auto mb-4 text-text-muted" />
          <h3 className="text-xl font-semibold text-text-primary mb-2">
            No hay conversaciones
          </h3>
          <p className="text-text-tertiary">
            Las conversaciones aparecerán aquí cuando los usuarios contacten al soporte
          </p>
        </div>
      )}
    </div>
  )
}
