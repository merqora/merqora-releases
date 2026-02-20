import { useState, useEffect } from 'react'
import { supabase } from '../supabaseClient'
import { 
  Brain, 
  TrendingUp, 
  Star,
  MessageSquare,
  CheckCircle,
  XCircle,
  Filter,
  Search,
  BarChart3,
  Award,
  AlertCircle
} from 'lucide-react'

export default function AILearning() {
  const [learningData, setLearningData] = useState([])
  const [stats, setStats] = useState({
    totalFeedback: 0,
    positiveCount: 0,
    negativeCount: 0,
    averageRating: 0,
    learnedResponses: 0
  })
  const [filter, setFilter] = useState('all') // all, positive, negative
  const [ratingFilter, setRatingFilter] = useState('all') // all, 1, 2, 3, 4, 5
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [selectedItem, setSelectedItem] = useState(null)

  useEffect(() => {
    loadLearningData()
    loadStats()
    
    // Realtime subscription
    const channel = supabase
      .channel('ai-learning-changes')
      .on(
        'postgres_changes',
        {
          event: '*',
          schema: 'public',
          table: 'ai_feedback'
        },
        () => {
          console.log('📚 AI feedback actualizado, recargando...')
          loadLearningData()
          loadStats()
        }
      )
      .subscribe()

    return () => {
      channel.unsubscribe()
    }
  }, [])

  async function loadLearningData() {
    setLoading(true)
    try {
      const { data, error } = await supabase
        .from('v_feedback_analysis')
        .select('*')
        .order('created_at', { ascending: false })
      
      if (error) throw error
      
      console.log(`✅ ${data?.length || 0} registros de feedback cargados`)
      setLearningData(data || [])
    } catch (error) {
      console.error('❌ Error loading learning data:', error)
      alert(`Error: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  async function loadStats() {
    try {
      const { data, error } = await supabase
        .from('ai_feedback')
        .select('helpful, rating')
      
      if (error) throw error

      const totalFeedback = data.length
      const positiveCount = data.filter(f => f.helpful).length
      const negativeCount = data.filter(f => !f.helpful).length
      const ratingsSum = data.filter(f => f.rating).reduce((sum, f) => sum + f.rating, 0)
      const ratingsCount = data.filter(f => f.rating).length
      const averageRating = ratingsCount > 0 ? (ratingsSum / ratingsCount).toFixed(1) : 0
      const learnedResponses = data.filter(f => f.helpful && f.rating >= 4).length

      setStats({
        totalFeedback,
        positiveCount,
        negativeCount,
        averageRating,
        learnedResponses
      })
    } catch (error) {
      console.error('Error loading stats:', error)
    }
  }

  const filteredData = learningData.filter(item => {
    // Filter by helpful/not helpful
    if (filter === 'positive' && !item.helpful) return false
    if (filter === 'negative' && item.helpful) return false
    
    // Filter by rating
    if (ratingFilter !== 'all' && item.rating !== parseInt(ratingFilter)) return false
    
    // Search filter
    if (search && !item.feedback_text?.toLowerCase().includes(search.toLowerCase()) &&
        !item.username?.toLowerCase().includes(search.toLowerCase()) &&
        !item.detected_intent?.toLowerCase().includes(search.toLowerCase())) {
      return false
    }
    
    return true
  })

  const getRatingColor = (rating) => {
    if (rating >= 4) return 'text-green-600'
    if (rating === 3) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getRatingBg = (rating) => {
    if (rating >= 4) return 'bg-green-100'
    if (rating === 3) return 'bg-yellow-100'
    return 'bg-red-100'
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-10 h-10 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6 fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
            <Brain className="w-7 h-7 text-primary" />
            Aprendizaje de IA
          </h1>
          <p className="text-text-tertiary mt-1">Feedback de usuarios para mejorar las respuestas del asistente</p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-text-tertiary text-sm">Total Feedback</p>
              <p className="text-2xl font-bold text-text-primary">{stats.totalFeedback}</p>
            </div>
            <MessageSquare className="w-8 h-8 text-accent-blue" />
          </div>
        </div>

        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-text-tertiary text-sm">Positivos</p>
              <p className="text-2xl font-bold text-accent-green">{stats.positiveCount}</p>
            </div>
            <CheckCircle className="w-8 h-8 text-accent-green" />
          </div>
        </div>

        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-text-tertiary text-sm">Negativos</p>
              <p className="text-2xl font-bold text-accent-magenta">{stats.negativeCount}</p>
            </div>
            <XCircle className="w-8 h-8 text-accent-magenta" />
          </div>
        </div>

        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-text-tertiary text-sm">Calificación</p>
              <p className="text-2xl font-bold text-accent-gold flex items-center gap-1">
                {stats.averageRating} <Star className="w-5 h-5 fill-current" />
              </p>
            </div>
            <BarChart3 className="w-8 h-8 text-accent-gold" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-primary to-accent-magenta rounded-2xl p-4 text-white">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-white/70 text-sm">Aprendidos</p>
              <p className="text-2xl font-bold">{stats.learnedResponses}</p>
            </div>
            <Award className="w-8 h-8" />
          </div>
          <p className="text-xs text-white/70 mt-2">Respuestas ≥4⭐ para entrenar IA</p>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4 space-y-4">
        <div className="flex flex-wrap gap-3">
          <button
            onClick={() => setFilter('all')}
            className={`px-4 py-2 rounded-xl font-medium transition-all ${
              filter === 'all'
                ? 'bg-primary text-white'
                : 'bg-rendly-bg text-text-secondary hover:bg-rendly-surface-elevated'
            }`}
          >
            Todos ({learningData.length})
          </button>
          <button
            onClick={() => setFilter('positive')}
            className={`px-4 py-2 rounded-xl font-medium transition-all ${
              filter === 'positive'
                ? 'bg-accent-green text-white'
                : 'bg-rendly-bg text-text-secondary hover:bg-rendly-surface-elevated'
            }`}
          >
            Positivos ({learningData.filter(f => f.helpful).length})
          </button>
          <button
            onClick={() => setFilter('negative')}
            className={`px-4 py-2 rounded-xl font-medium transition-all ${
              filter === 'negative'
                ? 'bg-accent-magenta text-white'
                : 'bg-rendly-bg text-text-secondary hover:bg-rendly-surface-elevated'
            }`}
          >
            Negativos ({learningData.filter(f => !f.helpful).length})
          </button>

          <div className="border-l border-primary/20 mx-2"></div>

          {[5, 4, 3, 2, 1].map(rating => (
            <button
              key={rating}
              onClick={() => setRatingFilter(ratingFilter === rating.toString() ? 'all' : rating.toString())}
              className={`px-3 py-2 rounded-lg font-medium transition flex items-center gap-1 ${
                ratingFilter === rating.toString()
                  ? 'bg-yellow-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {rating}<Star className="w-4 h-4 fill-current" />
            </button>
          ))}
        </div>

        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Buscar por feedback, usuario o intent..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 placeholder-gray-500 focus:outline-none focus:border-purple-500"
          />
        </div>
      </div>

      {/* Learning Data List */}
      <div className="grid gap-4">
        {filteredData.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-xl shadow-sm border border-gray-200">
            <AlertCircle className="w-16 h-16 mx-auto mb-4 text-gray-400" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              No hay feedback con estos filtros
            </h3>
            <p className="text-gray-600">
              Intenta cambiar los filtros o espera a que los usuarios envíen feedback
            </p>
          </div>
        ) : (
          filteredData.map((item) => (
            <div
              key={item.id}
              className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 hover:shadow-md transition cursor-pointer"
              onClick={() => setSelectedItem(item)}
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3 flex-1">
                  <div className={`p-2 rounded-lg ${item.helpful ? 'bg-green-100' : 'bg-red-100'}`}>
                    {item.helpful ? (
                      <CheckCircle className="w-5 h-5 text-green-600" />
                    ) : (
                      <XCircle className="w-5 h-5 text-red-600" />
                    )}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-medium text-gray-900">{item.username || 'Usuario Anónimo'}</span>
                      {item.rating && (
                        <span className={`flex items-center gap-1 text-sm font-medium ${getRatingColor(item.rating)}`}>
                          {item.rating} <Star className="w-4 h-4 fill-current" />
                        </span>
                      )}
                      {item.helpful && item.rating >= 4 && (
                        <span className="px-2 py-0.5 bg-purple-100 text-purple-700 text-xs font-medium rounded-full">
                          ✨ Para aprendizaje
                        </span>
                      )}
                    </div>
                    {item.feedback_text && (
                      <p className="text-gray-700 text-sm line-clamp-2">{item.feedback_text}</p>
                    )}
                  </div>
                </div>
                <span className="text-xs text-gray-500 whitespace-nowrap ml-4">
                  {new Date(item.created_at).toLocaleString('es-ES', { 
                    month: 'short', 
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </span>
              </div>

              <div className="flex items-center gap-4 text-xs text-gray-500 pt-3 border-t border-gray-100">
                <span className="flex items-center gap-1">
                  <MessageSquare className="w-4 h-4" />
                  {item.message_count || 0} mensajes
                </span>
                {item.detected_intent && (
                  <span className="px-2 py-1 bg-blue-50 text-blue-700 rounded-full">
                    {item.detected_intent}
                  </span>
                )}
                {item.conversation_status && (
                  <span className={`px-2 py-1 rounded-full ${
                    item.conversation_status === 'resolved' 
                      ? 'bg-green-50 text-green-700'
                      : 'bg-yellow-50 text-yellow-700'
                  }`}>
                    {item.conversation_status}
                  </span>
                )}
                {item.resolved_by && (
                  <span className="text-gray-600">
                    Resuelto por: {item.resolved_by === 'human' ? '👤 Humano' : '🤖 IA'}
                  </span>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* Detail Modal */}
      {selectedItem && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50" 
          onClick={() => setSelectedItem(null)}
        >
          <div 
            className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto" 
            onClick={(e) => e.stopPropagation()}
          >
            <div className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg ${selectedItem.helpful ? 'bg-green-100' : 'bg-red-100'}`}>
                    {selectedItem.helpful ? (
                      <CheckCircle className="w-6 h-6 text-green-600" />
                    ) : (
                      <XCircle className="w-6 h-6 text-red-600" />
                    )}
                  </div>
                  <div>
                    <h2 className="text-xl font-bold text-gray-900">{selectedItem.username || 'Usuario Anónimo'}</h2>
                    <p className="text-sm text-gray-500">
                      {new Date(selectedItem.created_at).toLocaleString('es-ES')}
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedItem(null)}
                  className="text-gray-400 hover:text-gray-600 text-2xl"
                >
                  ×
                </button>
              </div>

              <div className="space-y-4">
                {selectedItem.rating && (
                  <div className={`p-4 rounded-lg ${getRatingBg(selectedItem.rating)}`}>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Calificación</label>
                    <div className="flex items-center gap-1">
                      {[...Array(5)].map((_, i) => (
                        <Star
                          key={i}
                          className={`w-6 h-6 ${
                            i < selectedItem.rating
                              ? `${getRatingColor(selectedItem.rating)} fill-current`
                              : 'text-gray-300'
                          }`}
                        />
                      ))}
                      <span className={`ml-2 text-lg font-bold ${getRatingColor(selectedItem.rating)}`}>
                        {selectedItem.rating}/5
                      </span>
                    </div>
                  </div>
                )}

                {selectedItem.feedback_text && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Comentario del Usuario</label>
                    <p className="text-gray-900 bg-gray-50 p-3 rounded">{selectedItem.feedback_text}</p>
                  </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Tipo de Feedback</label>
                    <p className="text-gray-900 bg-gray-50 p-2 rounded text-sm">
                      {selectedItem.feedback_type || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Intent Detectado</label>
                    <p className="text-gray-900 bg-gray-50 p-2 rounded text-sm">
                      {selectedItem.detected_intent || 'N/A'}
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Estado Conversación</label>
                    <p className="text-gray-900 bg-gray-50 p-2 rounded text-sm">
                      {selectedItem.conversation_status || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Resuelto Por</label>
                    <p className="text-gray-900 bg-gray-50 p-2 rounded text-sm">
                      {selectedItem.resolved_by === 'human' ? '👤 Agente Humano' : 
                       selectedItem.resolved_by === 'ai' ? '🤖 IA' : 'N/A'}
                    </p>
                  </div>
                </div>

                {selectedItem.helpful && selectedItem.rating >= 4 && (
                  <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
                    <div className="flex items-center gap-2 mb-2">
                      <Brain className="w-5 h-5 text-purple-600" />
                      <span className="font-semibold text-purple-900">Usado para Aprendizaje de IA</span>
                    </div>
                    <p className="text-sm text-purple-700">
                      Este feedback positivo (≥4 estrellas) se está usando para mejorar las respuestas del asistente.
                      La IA aprenderá de la interacción entre el usuario y el agente para manejar consultas similares en el futuro.
                    </p>
                  </div>
                )}

                <div className="text-xs text-gray-500 pt-4 border-t border-gray-200 space-y-1">
                  <div>ID Feedback: {selectedItem.id}</div>
                  <div>ID Conversación: {selectedItem.conversation_id || 'N/A'}</div>
                  <div>Mensajes en conversación: {selectedItem.message_count || 0}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
