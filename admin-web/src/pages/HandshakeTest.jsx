import { useState, useEffect } from 'react'
import { 
  Grip,
  Clock,
  CheckCircle,
  XCircle,
  RefreshCw,
  User,
  Package,
  DollarSign,
  MessageSquare,
  AlertCircle,
  Zap
} from 'lucide-react'
import { supabase } from '../supabaseClient'

function HandshakeTest() {
  const [handshakes, setHandshakes] = useState([])
  const [loading, setLoading] = useState(true)
  const [realtimeStatus, setRealtimeStatus] = useState('connecting')
  const [actionLoading, setActionLoading] = useState(null) // ID del handshake en proceso
  const [notification, setNotification] = useState(null)

  // Cargar handshakes al inicio
  useEffect(() => {
    fetchHandshakes()
    
    // Suscribirse a cambios en tiempo real
    const channel = supabase
      .channel('handshake-test-channel')
      .on(
        'postgres_changes',
        {
          event: '*', // INSERT, UPDATE, DELETE
          schema: 'public',
          table: 'handshake_transactions'
        },
        (payload) => {
          console.log('🔔 Handshake change:', payload)
          
          if (payload.eventType === 'INSERT') {
            setHandshakes(prev => [payload.new, ...prev])
            showNotification('🆕 Nuevo handshake recibido!', 'success')
          } else if (payload.eventType === 'UPDATE') {
            setHandshakes(prev => 
              prev.map(h => h.id === payload.new.id ? payload.new : h)
            )
          } else if (payload.eventType === 'DELETE') {
            setHandshakes(prev => prev.filter(h => h.id !== payload.old.id))
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
  }, [])

  function showNotification(message, type = 'info') {
    setNotification({ message, type })
    setTimeout(() => setNotification(null), 4000)
  }

  async function fetchHandshakes() {
    setLoading(true)
    try {
      const { data, error } = await supabase
        .from('handshake_transactions')
        .select('*')
        .order('created_at', { ascending: false })
        .limit(50)

      if (error) throw error
      setHandshakes(data || [])
    } catch (error) {
      console.error('Error fetching handshakes:', error)
      showNotification('Error al cargar handshakes', 'error')
    } finally {
      setLoading(false)
    }
  }

  async function acceptHandshake(handshakeId) {
    setActionLoading(handshakeId)
    try {
      const { error } = await supabase
        .from('handshake_transactions')
        .update({ 
          status: 'ACCEPTED',
          accepted_at: new Date().toISOString()
        })
        .eq('id', handshakeId)

      if (error) throw error
      showNotification('✅ Handshake aceptado!', 'success')
    } catch (error) {
      console.error('Error accepting handshake:', error)
      showNotification('Error al aceptar: ' + error.message, 'error')
    } finally {
      setActionLoading(null)
    }
  }

  async function rejectHandshake(handshakeId) {
    setActionLoading(handshakeId)
    try {
      const { error } = await supabase
        .from('handshake_transactions')
        .update({ status: 'REJECTED' })
        .eq('id', handshakeId)

      if (error) throw error
      showNotification('❌ Handshake rechazado', 'info')
    } catch (error) {
      console.error('Error rejecting handshake:', error)
      showNotification('Error al rechazar: ' + error.message, 'error')
    } finally {
      setActionLoading(null)
    }
  }

  async function cancelHandshake(handshakeId) {
    setActionLoading(handshakeId)
    try {
      const { error } = await supabase
        .from('handshake_transactions')
        .update({ status: 'CANCELLED' })
        .eq('id', handshakeId)

      if (error) throw error
      showNotification('🚫 Handshake cancelado', 'info')
    } catch (error) {
      console.error('Error cancelling handshake:', error)
      showNotification('Error al cancelar: ' + error.message, 'error')
    } finally {
      setActionLoading(null)
    }
  }


  async function confirmTransaction(handshakeId, isInitiator) {
    setActionLoading(handshakeId)
    try {
      const updateData = isInitiator 
        ? { initiator_confirmed: true, status: 'IN_PROGRESS' }
        : { receiver_confirmed: true, status: 'IN_PROGRESS' }

      const { error } = await supabase
        .from('handshake_transactions')
        .update(updateData)
        .eq('id', handshakeId)

      if (error) throw error
      showNotification('✅ Confirmación registrada!', 'success')
    } catch (error) {
      console.error('Error confirming:', error)
      showNotification('Error al confirmar: ' + error.message, 'error')
    } finally {
      setActionLoading(null)
    }
  }

  function getStatusBadge(status) {
    const styles = {
      PROPOSED: { bg: 'bg-yellow-500/20', text: 'text-yellow-400', icon: Clock },
      ACCEPTED: { bg: 'bg-blue-500/20', text: 'text-blue-400', icon: CheckCircle },
      IN_PROGRESS: { bg: 'bg-purple-500/20', text: 'text-purple-400', icon: RefreshCw },
      COMPLETED: { bg: 'bg-green-500/20', text: 'text-green-400', icon: CheckCircle },
      REJECTED: { bg: 'bg-red-500/20', text: 'text-red-400', icon: XCircle },
      CANCELLED: { bg: 'bg-gray-500/20', text: 'text-gray-400', icon: XCircle },
      RENEGOTIATING: { bg: 'bg-orange-500/20', text: 'text-orange-400', icon: MessageSquare },
      DISPUTED: { bg: 'bg-red-500/20', text: 'text-red-400', icon: AlertCircle },
    }
    const style = styles[status] || styles.PROPOSED
    const Icon = style.icon

    return (
      <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-sm font-medium ${style.bg} ${style.text}`}>
        <Icon className="w-4 h-4" />
        {status}
      </span>
    )
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
            <div className="w-10 h-10 rounded-xl bg-primary/20 flex items-center justify-center">
              <Grip className="w-6 h-6 text-primary" />
            </div>
            Test de Handshakes
          </h1>
          <p className="text-text-muted mt-1">
            Prueba el sistema de handshakes desde aquí. Los cambios se reflejan en tiempo real.
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
            onClick={fetchHandshakes}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 bg-rendly-surface-elevated text-text-primary rounded-xl hover:bg-primary/20 transition-colors disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            Recargar
          </button>
        </div>
      </div>

      {/* Notification */}
      {notification && (
        <div className={`fixed top-4 right-4 z-50 px-4 py-3 rounded-xl shadow-lg flex items-center gap-2 animate-pulse ${
          notification.type === 'success' ? 'bg-green-500/90 text-white' :
          notification.type === 'error' ? 'bg-red-500/90 text-white' :
          'bg-blue-500/90 text-white'
        }`}>
          {notification.message}
        </div>
      )}

      {/* Info box */}
      <div className="bg-primary/10 border border-primary/30 rounded-xl p-4">
        <h3 className="font-semibold text-primary mb-2">📱 Instrucciones de prueba:</h3>
        <ol className="list-decimal list-inside space-y-1 text-text-secondary text-sm">
          <li>Abre la app Android y ve a un chat con otro usuario</li>
          <li>Pulsa el botón de handshake (🤝) a la izquierda del input</li>
          <li>Completa los datos y pulsa "Iniciar"</li>
          <li>El handshake aparecerá aquí en tiempo real</li>
          <li>Acepta o rechaza desde esta página para ver el cambio en la app</li>
        </ol>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { label: 'Total', value: handshakes.length, color: 'text-text-primary' },
          { label: 'Pendientes', value: handshakes.filter(h => h.status === 'PROPOSED').length, color: 'text-yellow-400' },
          { label: 'Aceptados', value: handshakes.filter(h => h.status === 'ACCEPTED').length, color: 'text-blue-400' },
          { label: 'Completados', value: handshakes.filter(h => h.status === 'COMPLETED').length, color: 'text-green-400' },
        ].map(stat => (
          <div key={stat.label} className="bg-rendly-surface p-4 rounded-xl">
            <p className="text-text-muted text-sm">{stat.label}</p>
            <p className={`text-2xl font-bold ${stat.color}`}>{stat.value}</p>
          </div>
        ))}
      </div>

      {/* Handshakes list */}
      <div className="space-y-4">
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <RefreshCw className="w-8 h-8 text-primary animate-spin" />
          </div>
        ) : handshakes.length === 0 ? (
          <div className="text-center py-12 bg-rendly-surface rounded-xl">
            <Grip className="w-16 h-16 text-text-muted mx-auto mb-4" />
            <h3 className="text-lg font-medium text-text-primary mb-2">No hay handshakes</h3>
            <p className="text-text-muted">
              Inicia uno desde la app Android para verlo aquí
            </p>
          </div>
        ) : (
          handshakes.map(handshake => (
            <div 
              key={handshake.id} 
              className={`bg-rendly-surface rounded-xl p-5 border-l-4 ${
                handshake.status === 'PROPOSED' ? 'border-yellow-500' :
                handshake.status === 'ACCEPTED' ? 'border-blue-500' :
                handshake.status === 'COMPLETED' ? 'border-green-500' :
                'border-gray-500'
              }`}
            >
              <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-4">
                {/* Info */}
                <div className="space-y-3 flex-1">
                  <div className="flex items-center gap-3">
                    {getStatusBadge(handshake.status)}
                    <span className="text-text-muted text-sm">
                      {formatDate(handshake.created_at)}
                    </span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div className="flex items-center gap-2 text-text-secondary">
                      <User className="w-4 h-4 text-text-muted" />
                      <span className="text-sm">
                        <span className="text-text-muted">Iniciador:</span>{' '}
                        <code className="text-xs bg-rendly-bg px-1 rounded">{handshake.initiator_id?.slice(0, 8)}...</code>
                      </span>
                    </div>
                    <div className="flex items-center gap-2 text-text-secondary">
                      <User className="w-4 h-4 text-text-muted" />
                      <span className="text-sm">
                        <span className="text-text-muted">Receptor:</span>{' '}
                        <code className="text-xs bg-rendly-bg px-1 rounded">{handshake.receiver_id?.slice(0, 8)}...</code>
                      </span>
                    </div>
                  </div>

                  <div className="flex items-start gap-2 text-text-primary">
                    <Package className="w-4 h-4 text-text-muted mt-0.5" />
                    <span className="text-sm">{handshake.product_description}</span>
                  </div>

                  <div className="flex items-center gap-2">
                    <DollarSign className="w-4 h-4 text-green-400" />
                    <span className="text-lg font-bold text-green-400">
                      ${parseFloat(handshake.agreed_price).toFixed(2)}
                    </span>
                    {handshake.counter_price && (
                      <span className="text-sm text-orange-400 ml-2">
                        (Contrapropuesta: ${parseFloat(handshake.counter_price).toFixed(2)})
                      </span>
                    )}
                  </div>

                  {/* Confirmations */}
                  {(handshake.status === 'ACCEPTED' || handshake.status === 'IN_PROGRESS') && (
                    <div className="flex items-center gap-4 pt-2 border-t border-primary/10">
                      <span className={`text-sm ${handshake.initiator_confirmed ? 'text-green-400' : 'text-text-muted'}`}>
                        {handshake.initiator_confirmed ? '✅' : '⏳'} Iniciador confirmó
                      </span>
                      <span className={`text-sm ${handshake.receiver_confirmed ? 'text-green-400' : 'text-text-muted'}`}>
                        {handshake.receiver_confirmed ? '✅' : '⏳'} Receptor confirmó
                      </span>
                    </div>
                  )}
                </div>

                {/* Actions */}
                <div className="flex flex-wrap gap-2">
                  {handshake.status === 'PROPOSED' && (
                    <>
                      <button
                        onClick={() => acceptHandshake(handshake.id)}
                        disabled={actionLoading === handshake.id}
                        className="flex items-center gap-2 px-4 py-2 bg-green-500/20 text-green-400 rounded-lg hover:bg-green-500/30 transition-colors disabled:opacity-50"
                      >
                        {actionLoading === handshake.id ? (
                          <RefreshCw className="w-4 h-4 animate-spin" />
                        ) : (
                          <CheckCircle className="w-4 h-4" />
                        )}
                        Aceptar
                      </button>
                      <button
                        onClick={() => cancelHandshake(handshake.id)}
                        disabled={actionLoading === handshake.id}
                        className="flex items-center gap-2 px-4 py-2 bg-gray-500/20 text-gray-400 rounded-lg hover:bg-gray-500/30 transition-colors disabled:opacity-50"
                      >
                        <XCircle className="w-4 h-4" />
                        Cancelar
                      </button>
                    </>
                  )}

                  {(handshake.status === 'ACCEPTED' || handshake.status === 'IN_PROGRESS') && (
                    <>
                      {!handshake.initiator_confirmed && (
                        <button
                          onClick={() => confirmTransaction(handshake.id, true)}
                          disabled={actionLoading === handshake.id}
                          className="flex items-center gap-2 px-4 py-2 bg-purple-500/20 text-purple-400 rounded-lg hover:bg-purple-500/30 transition-colors disabled:opacity-50"
                        >
                          {actionLoading === handshake.id ? (
                            <RefreshCw className="w-4 h-4 animate-spin" />
                          ) : (
                            <CheckCircle className="w-4 h-4" />
                          )}
                          Confirmar (Iniciador)
                        </button>
                      )}
                      {!handshake.receiver_confirmed && (
                        <button
                          onClick={() => confirmTransaction(handshake.id, false)}
                          disabled={actionLoading === handshake.id}
                          className="flex items-center gap-2 px-4 py-2 bg-blue-500/20 text-blue-400 rounded-lg hover:bg-blue-500/30 transition-colors disabled:opacity-50"
                        >
                          {actionLoading === handshake.id ? (
                            <RefreshCw className="w-4 h-4 animate-spin" />
                          ) : (
                            <CheckCircle className="w-4 h-4" />
                          )}
                          Confirmar (Receptor)
                        </button>
                      )}
                      <button
                        onClick={() => cancelHandshake(handshake.id)}
                        disabled={actionLoading === handshake.id}
                        className="flex items-center gap-2 px-4 py-2 bg-gray-500/20 text-gray-400 rounded-lg hover:bg-gray-500/30 transition-colors disabled:opacity-50"
                      >
                        <XCircle className="w-4 h-4" />
                        Cancelar
                      </button>
                    </>
                  )}

                  {handshake.status === 'COMPLETED' && (
                    <span className="flex items-center gap-2 px-4 py-2 bg-green-500/20 text-green-400 rounded-lg">
                      <CheckCircle className="w-4 h-4" />
                      Completado
                    </span>
                  )}
                </div>
              </div>

              {/* ID for debugging */}
              <div className="mt-3 pt-3 border-t border-primary/10">
                <code className="text-xs text-text-muted">ID: {handshake.id}</code>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default HandshakeTest
