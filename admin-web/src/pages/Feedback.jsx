import { useState, useEffect } from 'react'
import { supabase } from '../supabaseClient'

export default function Feedback() {
  const [feedbackList, setFeedbackList] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, pending, reviewing, planned, implemented
  const [selectedFeedback, setSelectedFeedback] = useState(null)
  const [responseText, setResponseText] = useState('')
  const [responding, setResponding] = useState(false)

  useEffect(() => {
    loadFeedback()
    
    // Realtime subscription para app_feedback
    const feedbackChannel = supabase
      .channel('public:app_feedback')
      .on(
        'postgres_changes',
        {
          event: 'INSERT',
          schema: 'public',
          table: 'app_feedback'
        },
        (payload) => {
          console.log('✅ Nuevo feedback recibido:', payload.new)
          setFeedbackList(prev => [payload.new, ...prev])
        }
      )
      .on(
        'postgres_changes',
        {
          event: 'UPDATE',
          schema: 'public',
          table: 'app_feedback'
        },
        (payload) => {
          console.log('✅ Feedback actualizado:', payload.new)
          setFeedbackList(prev => 
            prev.map(f => f.id === payload.new.id ? payload.new : f)
          )
        }
      )
      .subscribe((status) => {
        console.log('📡 Feedback realtime status:', status)
        if (status === 'SUBSCRIBED') {
          console.log('✅ Suscrito a cambios de app_feedback en tiempo real')
        }
      })

    return () => {
      feedbackChannel.unsubscribe()
    }
  }, [])

  async function loadFeedback() {
    setLoading(true)
    try {
      console.log('📥 Cargando app_feedback...')
      const { data, error } = await supabase
        .from('app_feedback')
        .select('*')
        .order('created_at', { ascending: false })
      
      if (error) {
        console.error('❌ Error cargando feedback:', error)
        throw error
      }
      
      console.log(`✅ ${data?.length || 0} feedbacks cargados`)
      setFeedbackList(data || [])
    } catch (error) {
      console.error('❌ Error loading feedback:', error)
      alert(`Error cargando feedback: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  async function updateFeedbackStatus(feedbackId, newStatus) {
    try {
      const { error } = await supabase
        .from('app_feedback')
        .update({ status: newStatus })
        .eq('id', feedbackId)
      
      if (error) throw error
      console.log('✓ Estado actualizado')
    } catch (error) {
      console.error('Error updating status:', error)
    }
  }

  async function updateFeedbackPriority(feedbackId, newPriority) {
    try {
      const { error } = await supabase
        .from('app_feedback')
        .update({ priority: newPriority })
        .eq('id', feedbackId)
      
      if (error) throw error
      console.log('✓ Prioridad actualizada')
    } catch (error) {
      console.error('Error updating priority:', error)
    }
  }

  async function submitResponse() {
    if (!responseText.trim() || !selectedFeedback) return
    
    setResponding(true)
    try {
      const { error } = await supabase
        .from('app_feedback')
        .update({
          admin_response: responseText,
          responded_at: new Date().toISOString(),
          status: 'reviewed'
        })
        .eq('id', selectedFeedback.id)
      
      if (error) throw error
      
      setResponseText('')
      setSelectedFeedback(null)
      console.log('✓ Respuesta enviada')
    } catch (error) {
      console.error('Error sending response:', error)
    } finally {
      setResponding(false)
    }
  }

  const filteredFeedback = filter === 'all' 
    ? feedbackList 
    : feedbackList.filter(f => f.status === filter)

  const getCategoryLabel = (category) => {
    const labels = {
      feature_request: '💡 Nueva función',
      improvement: '📈 Mejora',
      complaint: '😤 Queja',
      praise: '👏 Elogio',
      other: '📝 Otro'
    }
    return labels[category] || category
  }

  const getStatusColor = (status) => {
    const colors = {
      pending: 'bg-yellow-100 text-yellow-800',
      reviewing: 'bg-blue-100 text-blue-800',
      planned: 'bg-purple-100 text-purple-800',
      implemented: 'bg-green-100 text-green-800',
      rejected: 'bg-red-100 text-red-800',
      archived: 'bg-gray-100 text-gray-800'
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  const getPriorityColor = (priority) => {
    const colors = {
      low: 'text-gray-500',
      medium: 'text-blue-500',
      high: 'text-orange-500',
      critical: 'text-red-500'
    }
    return colors[priority] || 'text-gray-500'
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
      <div>
        <h1 className="text-2xl font-bold text-text-primary">Feedback de Usuarios</h1>
        <p className="text-text-tertiary mt-1">Comentarios y sugerencias para mejorar Rendly</p>
      </div>

      {/* Filters */}
      <div className="flex gap-2 flex-wrap">
        <button
          onClick={() => setFilter('all')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'all'
              ? 'bg-primary text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          Todos ({feedbackList.length})
        </button>
        <button
          onClick={() => setFilter('pending')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'pending'
              ? 'bg-accent-gold text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          Pendientes ({feedbackList.filter(f => f.status === 'pending').length})
        </button>
        <button
          onClick={() => setFilter('reviewing')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'reviewing'
              ? 'bg-accent-blue text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          En revisión ({feedbackList.filter(f => f.status === 'reviewing').length})
        </button>
        <button
          onClick={() => setFilter('planned')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'planned'
              ? 'bg-primary text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          Planificado ({feedbackList.filter(f => f.status === 'planned').length})
        </button>
        <button
          onClick={() => setFilter('implemented')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'implemented'
              ? 'bg-accent-green text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          Implementado ({feedbackList.filter(f => f.status === 'implemented').length})
        </button>
      </div>

      {/* Feedback List */}
      <div className="grid gap-4">
        {filteredFeedback.length === 0 ? (
          <div className="text-center py-12 text-text-tertiary bg-rendly-surface rounded-2xl border border-primary/10">
            No hay feedback con este filtro
          </div>
        ) : (
          filteredFeedback.map((feedback) => (
            <div
              key={feedback.id}
              className="bg-rendly-surface rounded-2xl border border-primary/10 p-5 hover:border-primary/30 transition-all cursor-pointer"
              onClick={() => setSelectedFeedback(feedback)}
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-lg">{getCategoryLabel(feedback.category)}</span>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(feedback.status)}`}>
                      {feedback.status}
                    </span>
                    <span className={`text-sm font-medium ${getPriorityColor(feedback.priority)}`}>
                      {feedback.priority === 'high' && '🔴'}
                      {feedback.priority === 'medium' && '🟡'}
                      {feedback.priority === 'low' && '⚪'}
                    </span>
                  </div>
                  <h3 className="font-semibold text-text-primary mb-1">{feedback.title}</h3>
                  <p className="text-text-secondary text-sm line-clamp-2">{feedback.description}</p>
                </div>
                {feedback.rating && (
                  <div className="flex items-center ml-4">
                    <span className="text-yellow-500 text-xl">{'⭐'.repeat(feedback.rating)}</span>
                  </div>
                )}
              </div>
              
              <div className="flex items-center justify-between text-xs text-text-muted mt-3 pt-3 border-t border-primary/10">
                <span>Usuario: {feedback.user_name || 'Anónimo'}</span>
                <span>{new Date(feedback.created_at).toLocaleString('es-ES')}</span>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal de detalle */}
      {selectedFeedback && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50" onClick={() => setSelectedFeedback(null)}>
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <div className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h2 className="text-xl font-bold text-gray-900 mb-2">{selectedFeedback.title}</h2>
                  <div className="flex items-center gap-2">
                    <span className="text-2xl">{getCategoryLabel(selectedFeedback.category)}</span>
                    {selectedFeedback.rating && (
                      <span className="text-yellow-500">{'⭐'.repeat(selectedFeedback.rating)}</span>
                    )}
                  </div>
                </div>
                <button
                  onClick={() => setSelectedFeedback(null)}
                  className="text-gray-400 hover:text-gray-600 text-2xl"
                >
                  ×
                </button>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-gray-700 block mb-1">Descripción</label>
                  <p className="text-gray-900 bg-gray-50 p-3 rounded">{selectedFeedback.description}</p>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Estado</label>
                    <select
                      value={selectedFeedback.status}
                      onChange={(e) => updateFeedbackStatus(selectedFeedback.id, e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500"
                    >
                      <option value="pending">Pendiente</option>
                      <option value="reviewing">En revisión</option>
                      <option value="planned">Planificado</option>
                      <option value="implemented">Implementado</option>
                      <option value="rejected">Rechazado</option>
                      <option value="archived">Archivado</option>
                    </select>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Prioridad</label>
                    <select
                      value={selectedFeedback.priority}
                      onChange={(e) => updateFeedbackPriority(selectedFeedback.id, e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500"
                    >
                      <option value="low">Baja</option>
                      <option value="medium">Media</option>
                      <option value="high">Alta</option>
                      <option value="critical">Crítica</option>
                    </select>
                  </div>
                </div>

                {selectedFeedback.device_info && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Info del Dispositivo</label>
                    <div className="bg-gray-50 p-3 rounded text-sm text-gray-600">
                      <div>Modelo: {selectedFeedback.device_info.manufacturer} {selectedFeedback.device_info.model}</div>
                      <div>Android: {selectedFeedback.device_info.android_version}</div>
                      <div>App: v{selectedFeedback.app_version}</div>
                    </div>
                  </div>
                )}

                {selectedFeedback.admin_response && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Respuesta del Equipo</label>
                    <p className="text-gray-900 bg-green-50 p-3 rounded border border-green-200">{selectedFeedback.admin_response}</p>
                    <p className="text-xs text-gray-500 mt-1">Respondido el {new Date(selectedFeedback.responded_at).toLocaleString('es-ES')}</p>
                  </div>
                )}

                {!selectedFeedback.admin_response && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Responder (Interno)</label>
                    <textarea
                      value={responseText}
                      onChange={(e) => setResponseText(e.target.value)}
                      placeholder="Nota interna sobre este feedback..."
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500"
                      rows="3"
                    />
                    <button
                      onClick={submitResponse}
                      disabled={!responseText.trim() || responding}
                      className="mt-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {responding ? 'Guardando...' : 'Guardar Nota'}
                    </button>
                  </div>
                )}

                <div className="text-xs text-gray-500 pt-4 border-t border-gray-200">
                  <div>Usuario: {selectedFeedback.user_name || 'Anónimo'}</div>
                  <div>Email: {selectedFeedback.user_email || 'No proporcionado'}</div>
                  <div>Fecha: {new Date(selectedFeedback.created_at).toLocaleString('es-ES')}</div>
                  <div>ID: {selectedFeedback.id}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
