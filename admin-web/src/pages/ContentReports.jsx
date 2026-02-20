import { useState, useEffect } from 'react'
import { supabase } from '../supabaseClient'

export default function ContentReports() {
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, pending, reviewing, resolved, dismissed
  const [typeFilter, setTypeFilter] = useState('all') // all, post, rend, user, comment, message, story
  const [selectedReport, setSelectedReport] = useState(null)
  const [adminNotes, setAdminNotes] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadReports()
    
    // Realtime subscription
    const channel = supabase
      .channel('content-reports-changes')
      .on(
        'postgres_changes',
        {
          event: 'INSERT',
          schema: 'public',
          table: 'content_reports'
        },
        (payload) => {
          console.log('🚨 Nuevo reporte de contenido:', payload.new)
          loadReportWithDetails(payload.new.id).then(report => {
            if (report) {
              setReports(prev => [report, ...prev])
            }
          })
          
          // Notificación visual
          if (Notification.permission === 'granted') {
            new Notification('Nuevo Reporte de Contenido', {
              body: `${getContentTypeName(payload.new.content_type)} - ${getReasonName(payload.new.reason)}`,
              icon: '/report-icon.png'
            })
          }
        }
      )
      .on(
        'postgres_changes',
        {
          event: 'UPDATE',
          schema: 'public',
          table: 'content_reports'
        },
        (payload) => {
          console.log('Reporte actualizado:', payload.new)
          loadReportWithDetails(payload.new.id).then(report => {
            if (report) {
              setReports(prev => 
                prev.map(r => r.id === payload.new.id ? report : r)
              )
            }
          })
        }
      )
      .subscribe()

    // Request notification permission
    if (Notification.permission === 'default') {
      Notification.requestPermission()
    }

    return () => {
      channel.unsubscribe()
    }
  }, [])

  async function loadReportWithDetails(reportId) {
    try {
      const { data, error } = await supabase
        .from('content_reports')
        .select(`
          *,
          reporter:reporter_id(id, username, avatar_url),
          reported_user:reported_user_id(id, username, avatar_url)
        `)
        .eq('id', reportId)
        .single()
      
      if (error) throw error
      return data
    } catch (error) {
      console.error('Error loading report details:', error)
      return null
    }
  }

  async function loadReports() {
    setLoading(true)
    try {
      const { data, error } = await supabase
        .from('content_reports')
        .select(`
          *,
          reporter:reporter_id(id, username, avatar_url),
          reported_user:reported_user_id(id, username, avatar_url)
        `)
        .order('created_at', { ascending: false })
      
      if (error) throw error
      setReports(data || [])
    } catch (error) {
      console.error('Error loading reports:', error)
    } finally {
      setLoading(false)
    }
  }

  async function updateReportStatus(reportId, newStatus) {
    try {
      const updateData = { 
        status: newStatus,
        updated_at: new Date().toISOString()
      }
      
      if (newStatus === 'resolved' || newStatus === 'dismissed') {
        updateData.resolved_at = new Date().toISOString()
      }
      
      const { error } = await supabase
        .from('content_reports')
        .update(updateData)
        .eq('id', reportId)
      
      if (error) throw error
      console.log('✓ Estado actualizado')
    } catch (error) {
      console.error('Error updating status:', error)
    }
  }

  async function saveNotes() {
    if (!selectedReport) return
    
    setSaving(true)
    try {
      const { error } = await supabase
        .from('content_reports')
        .update({ 
          admin_notes: adminNotes,
          updated_at: new Date().toISOString()
        })
        .eq('id', selectedReport.id)
      
      if (error) throw error
      console.log('✓ Notas guardadas')
      setSelectedReport(null)
      setAdminNotes('')
    } catch (error) {
      console.error('Error saving notes:', error)
    } finally {
      setSaving(false)
    }
  }

  function getContentTypeName(type) {
    const types = {
      post: 'Publicación',
      rend: 'Rend',
      user: 'Usuario',
      comment: 'Comentario',
      message: 'Mensaje',
      story: 'Historia'
    }
    return types[type] || type
  }

  function getReasonName(reason) {
    const reasons = {
      spam: 'Spam',
      inappropriate: 'Contenido inapropiado',
      harassment: 'Acoso o bullying',
      fake: 'Información falsa',
      violence: 'Violencia',
      hate_speech: 'Discurso de odio',
      scam: 'Estafa o fraude',
      other: 'Otro motivo'
    }
    return reasons[reason] || reason
  }

  function getStatusColor(status) {
    const colors = {
      pending: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
      reviewing: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
      resolved: 'bg-green-500/20 text-green-400 border-green-500/30',
      dismissed: 'bg-gray-500/20 text-gray-400 border-gray-500/30'
    }
    return colors[status] || colors.pending
  }

  function getContentTypeColor(type) {
    const colors = {
      post: 'bg-purple-500/20 text-purple-400',
      rend: 'bg-pink-500/20 text-pink-400',
      user: 'bg-blue-500/20 text-blue-400',
      comment: 'bg-orange-500/20 text-orange-400',
      message: 'bg-cyan-500/20 text-cyan-400',
      story: 'bg-indigo-500/20 text-indigo-400'
    }
    return colors[type] || 'bg-gray-500/20 text-gray-400'
  }

  function getReasonColor(reason) {
    const colors = {
      spam: 'bg-gray-500/20 text-gray-400',
      inappropriate: 'bg-red-500/20 text-red-400',
      harassment: 'bg-orange-500/20 text-orange-400',
      fake: 'bg-blue-500/20 text-blue-400',
      violence: 'bg-red-600/20 text-red-500',
      hate_speech: 'bg-purple-500/20 text-purple-400',
      scam: 'bg-yellow-500/20 text-yellow-400',
      other: 'bg-gray-500/20 text-gray-400'
    }
    return colors[reason] || colors.other
  }

  const filteredReports = reports.filter(report => {
    const matchesStatus = filter === 'all' || report.status === filter
    const matchesType = typeFilter === 'all' || report.content_type === typeFilter
    return matchesStatus && matchesType
  })

  const stats = {
    total: reports.length,
    pending: reports.filter(r => r.status === 'pending').length,
    reviewing: reports.filter(r => r.status === 'reviewing').length,
    resolved: reports.filter(r => r.status === 'resolved').length,
    dismissed: reports.filter(r => r.status === 'dismissed').length
  }

  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-white flex items-center gap-3">
            🚨 Reportes de Contenido
          </h1>
          <p className="text-gray-400 mt-1">
            Gestión de reportes de publicaciones, usuarios y contenido
          </p>
        </div>
        <button
          onClick={loadReports}
          className="px-4 py-2 bg-purple-600 hover:bg-purple-700 rounded-lg text-white font-medium transition-colors"
        >
          ↻ Actualizar
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-5 gap-4 mb-6">
        <div className="bg-gray-800/50 rounded-xl p-4 border border-gray-700">
          <div className="text-2xl font-bold text-white">{stats.total}</div>
          <div className="text-gray-400 text-sm">Total</div>
        </div>
        <div className="bg-yellow-500/10 rounded-xl p-4 border border-yellow-500/30">
          <div className="text-2xl font-bold text-yellow-400">{stats.pending}</div>
          <div className="text-yellow-400/70 text-sm">Pendientes</div>
        </div>
        <div className="bg-blue-500/10 rounded-xl p-4 border border-blue-500/30">
          <div className="text-2xl font-bold text-blue-400">{stats.reviewing}</div>
          <div className="text-blue-400/70 text-sm">En Revisión</div>
        </div>
        <div className="bg-green-500/10 rounded-xl p-4 border border-green-500/30">
          <div className="text-2xl font-bold text-green-400">{stats.resolved}</div>
          <div className="text-green-400/70 text-sm">Resueltos</div>
        </div>
        <div className="bg-gray-500/10 rounded-xl p-4 border border-gray-500/30">
          <div className="text-2xl font-bold text-gray-400">{stats.dismissed}</div>
          <div className="text-gray-400/70 text-sm">Descartados</div>
        </div>
      </div>

      {/* Filters */}
      <div className="flex gap-4 mb-6">
        <div className="flex gap-2">
          <span className="text-gray-400 py-2">Estado:</span>
          {['all', 'pending', 'reviewing', 'resolved', 'dismissed'].map(status => (
            <button
              key={status}
              onClick={() => setFilter(status)}
              className={`px-3 py-1 rounded-lg text-sm font-medium transition-colors ${
                filter === status
                  ? 'bg-purple-600 text-white'
                  : 'bg-gray-700/50 text-gray-400 hover:bg-gray-700'
              }`}
            >
              {status === 'all' ? 'Todos' : 
               status === 'pending' ? 'Pendientes' :
               status === 'reviewing' ? 'En Revisión' :
               status === 'resolved' ? 'Resueltos' : 'Descartados'}
            </button>
          ))}
        </div>
        <div className="flex gap-2">
          <span className="text-gray-400 py-2">Tipo:</span>
          {['all', 'post', 'rend', 'user', 'comment', 'story'].map(type => (
            <button
              key={type}
              onClick={() => setTypeFilter(type)}
              className={`px-3 py-1 rounded-lg text-sm font-medium transition-colors ${
                typeFilter === type
                  ? 'bg-purple-600 text-white'
                  : 'bg-gray-700/50 text-gray-400 hover:bg-gray-700'
              }`}
            >
              {type === 'all' ? 'Todos' : getContentTypeName(type)}
            </button>
          ))}
        </div>
      </div>

      {/* Reports List */}
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin h-8 w-8 border-2 border-purple-500 border-t-transparent rounded-full"></div>
        </div>
      ) : filteredReports.length === 0 ? (
        <div className="text-center py-12 bg-gray-800/30 rounded-xl border border-gray-700">
          <div className="text-4xl mb-4">📭</div>
          <p className="text-gray-400">No hay reportes {filter !== 'all' ? `con estado "${filter}"` : ''}</p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredReports.map(report => (
            <div
              key={report.id}
              className="bg-gray-800/50 rounded-xl p-5 border border-gray-700 hover:border-purple-500/50 transition-colors"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  {/* Header del reporte */}
                  <div className="flex items-center gap-3 mb-3">
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getContentTypeColor(report.content_type)}`}>
                      {getContentTypeName(report.content_type)}
                    </span>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${getReasonColor(report.reason)}`}>
                      {getReasonName(report.reason)}
                    </span>
                    <span className={`px-2 py-1 rounded text-xs font-medium border ${getStatusColor(report.status)}`}>
                      {report.status === 'pending' ? 'Pendiente' :
                       report.status === 'reviewing' ? 'En Revisión' :
                       report.status === 'resolved' ? 'Resuelto' : 'Descartado'}
                    </span>
                  </div>

                  {/* Info del reportante y reportado */}
                  <div className="flex items-center gap-6 mb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-gray-500 text-sm">Reportado por:</span>
                      <div className="flex items-center gap-2">
                        {report.reporter?.avatar_url && (
                          <img 
                            src={report.reporter.avatar_url} 
                            alt="" 
                            className="w-6 h-6 rounded-full"
                          />
                        )}
                        <span className="text-white font-medium">
                          @{report.reporter?.username || 'Usuario desconocido'}
                        </span>
                      </div>
                    </div>
                    {report.reported_user && (
                      <div className="flex items-center gap-2">
                        <span className="text-gray-500 text-sm">Usuario reportado:</span>
                        <div className="flex items-center gap-2">
                          {report.reported_user?.avatar_url && (
                            <img 
                              src={report.reported_user.avatar_url} 
                              alt="" 
                              className="w-6 h-6 rounded-full"
                            />
                          )}
                          <span className="text-red-400 font-medium">
                            @{report.reported_user?.username}
                          </span>
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Descripción */}
                  {report.description && (
                    <div className="bg-gray-900/50 rounded-lg p-3 mb-3">
                      <p className="text-gray-300 text-sm">{report.description}</p>
                    </div>
                  )}

                  {/* Content ID */}
                  <div className="text-xs text-gray-500">
                    ID del contenido: <code className="bg-gray-900 px-2 py-0.5 rounded">{report.content_id}</code>
                  </div>

                  {/* Admin notes */}
                  {report.admin_notes && (
                    <div className="mt-3 bg-purple-500/10 rounded-lg p-3 border border-purple-500/30">
                      <div className="text-xs text-purple-400 mb-1">Notas del admin:</div>
                      <p className="text-gray-300 text-sm">{report.admin_notes}</p>
                    </div>
                  )}
                </div>

                {/* Actions */}
                <div className="flex flex-col gap-2">
                  <div className="text-xs text-gray-500 text-right mb-2">
                    {new Date(report.created_at).toLocaleString('es-ES', {
                      day: '2-digit',
                      month: 'short',
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </div>
                  
                  <select
                    value={report.status}
                    onChange={(e) => updateReportStatus(report.id, e.target.value)}
                    className="bg-gray-700 border border-gray-600 rounded-lg px-3 py-1.5 text-sm text-white focus:outline-none focus:border-purple-500"
                  >
                    <option value="pending">Pendiente</option>
                    <option value="reviewing">En Revisión</option>
                    <option value="resolved">Resuelto</option>
                    <option value="dismissed">Descartado</option>
                  </select>

                  <button
                    onClick={() => {
                      setSelectedReport(report)
                      setAdminNotes(report.admin_notes || '')
                    }}
                    className="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm text-white transition-colors"
                  >
                    ✏️ Notas
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal for notes */}
      {selectedReport && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-800 rounded-2xl p-6 max-w-lg w-full border border-gray-700">
            <h3 className="text-xl font-bold text-white mb-4">
              Notas del Administrador
            </h3>
            
            <div className="mb-4">
              <div className="flex items-center gap-2 mb-2">
                <span className={`px-2 py-1 rounded text-xs font-medium ${getContentTypeColor(selectedReport.content_type)}`}>
                  {getContentTypeName(selectedReport.content_type)}
                </span>
                <span className={`px-2 py-1 rounded text-xs font-medium ${getReasonColor(selectedReport.reason)}`}>
                  {getReasonName(selectedReport.reason)}
                </span>
              </div>
              {selectedReport.description && (
                <p className="text-gray-400 text-sm">{selectedReport.description}</p>
              )}
            </div>

            <textarea
              value={adminNotes}
              onChange={(e) => setAdminNotes(e.target.value)}
              placeholder="Escribe notas sobre este reporte..."
              className="w-full h-32 bg-gray-900 border border-gray-700 rounded-lg p-3 text-white placeholder-gray-500 focus:outline-none focus:border-purple-500 resize-none"
            />

            <div className="flex justify-end gap-3 mt-4">
              <button
                onClick={() => {
                  setSelectedReport(null)
                  setAdminNotes('')
                }}
                className="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg text-white transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={saveNotes}
                disabled={saving}
                className="px-4 py-2 bg-purple-600 hover:bg-purple-700 rounded-lg text-white font-medium transition-colors disabled:opacity-50"
              >
                {saving ? 'Guardando...' : 'Guardar Notas'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
