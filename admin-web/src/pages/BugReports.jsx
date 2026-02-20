import { useState, useEffect } from 'react'
import { supabase } from '../supabaseClient'

export default function BugReports() {
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, open, investigating, in_progress, fixed
  const [selectedReport, setSelectedReport] = useState(null)
  const [adminNotes, setAdminNotes] = useState('')
  const [resolutionNotes, setResolutionNotes] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadReports()
    
    // Realtime subscription
    const channel = supabase
      .channel('bug-reports-changes')
      .on(
        'postgres_changes',
        {
          event: 'INSERT',
          schema: 'public',
          table: 'bug_reports'
        },
        (payload) => {
          console.log('🐛 Nuevo reporte de error:', payload.new)
          setReports(prev => [payload.new, ...prev])
          
          // Notificación visual
          if (Notification.permission === 'granted') {
            new Notification('Nuevo Bug Report', {
              body: payload.new.title,
              icon: '/bug-icon.png'
            })
          }
        }
      )
      .on(
        'postgres_changes',
        {
          event: 'UPDATE',
          schema: 'public',
          table: 'bug_reports'
        },
        (payload) => {
          console.log('Bug report actualizado:', payload.new)
          setReports(prev => 
            prev.map(r => r.id === payload.new.id ? payload.new : r)
          )
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

  async function loadReports() {
    setLoading(true)
    try {
      const { data, error } = await supabase
        .from('bug_reports')
        .select('*')
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
      const updateData = { status: newStatus }
      
      if (newStatus === 'fixed' || newStatus === 'closed') {
        updateData.resolved_at = new Date().toISOString()
      }
      
      const { error } = await supabase
        .from('bug_reports')
        .update(updateData)
        .eq('id', reportId)
      
      if (error) throw error
      console.log('✓ Estado actualizado')
    } catch (error) {
      console.error('Error updating status:', error)
    }
  }

  async function updateReportSeverity(reportId, newSeverity) {
    try {
      const { error } = await supabase
        .from('bug_reports')
        .update({ severity: newSeverity })
        .eq('id', reportId)
      
      if (error) throw error
      console.log('✓ Severidad actualizada')
    } catch (error) {
      console.error('Error updating severity:', error)
    }
  }

  async function updateReportPriority(reportId, newPriority) {
    try {
      const { error } = await supabase
        .from('bug_reports')
        .update({ priority: newPriority })
        .eq('id', reportId)
      
      if (error) throw error
      console.log('✓ Prioridad actualizada')
    } catch (error) {
      console.error('Error updating priority:', error)
    }
  }

  async function saveNotes() {
    if (!selectedReport) return
    
    setSaving(true)
    try {
      const updateData = {}
      
      if (adminNotes.trim()) {
        updateData.admin_notes = adminNotes
      }
      
      if (resolutionNotes.trim()) {
        updateData.resolution_notes = resolutionNotes
        updateData.status = 'fixed'
        updateData.resolved_at = new Date().toISOString()
      }
      
      const { error } = await supabase
        .from('bug_reports')
        .update(updateData)
        .eq('id', selectedReport.id)
      
      if (error) throw error
      
      setAdminNotes('')
      setResolutionNotes('')
      console.log('✓ Notas guardadas')
    } catch (error) {
      console.error('Error saving notes:', error)
    } finally {
      setSaving(false)
    }
  }

  const filteredReports = filter === 'all' 
    ? reports 
    : reports.filter(r => r.status === filter)

  const getCategoryLabel = (category) => {
    const labels = {
      crash: '💥 Crash',
      ui: '🎨 UI/Visual',
      performance: '⚡ Rendimiento',
      data: '📊 Datos',
      network: '🌐 Red',
      security: '🔒 Seguridad',
      other: '🐛 Otro'
    }
    return labels[category] || category
  }

  const getStatusColor = (status) => {
    const colors = {
      open: 'bg-red-100 text-red-800',
      investigating: 'bg-yellow-100 text-yellow-800',
      in_progress: 'bg-blue-100 text-blue-800',
      fixed: 'bg-green-100 text-green-800',
      wont_fix: 'bg-gray-100 text-gray-800',
      duplicate: 'bg-purple-100 text-purple-800',
      closed: 'bg-gray-100 text-gray-600'
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  const getSeverityColor = (severity) => {
    const colors = {
      low: 'text-gray-500',
      medium: 'text-blue-500',
      high: 'text-orange-500',
      critical: 'text-red-600 font-bold'
    }
    return colors[severity] || 'text-gray-500'
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
        <h1 className="text-2xl font-bold text-text-primary">Reportes de Errores</h1>
        <p className="text-text-tertiary mt-1">Bugs y problemas reportados por los usuarios</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="text-2xl font-bold text-accent-magenta">{reports.filter(r => r.status === 'open').length}</div>
          <div className="text-sm text-text-tertiary">Abiertos</div>
        </div>
        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="text-2xl font-bold text-accent-blue">{reports.filter(r => r.status === 'in_progress').length}</div>
          <div className="text-sm text-text-tertiary">En Progreso</div>
        </div>
        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="text-2xl font-bold text-accent-gold">{reports.filter(r => r.severity === 'critical').length}</div>
          <div className="text-sm text-text-tertiary">Críticos</div>
        </div>
        <div className="bg-rendly-surface rounded-2xl border border-primary/10 p-4">
          <div className="text-2xl font-bold text-accent-green">{reports.filter(r => r.status === 'fixed').length}</div>
          <div className="text-sm text-text-tertiary">Resueltos</div>
        </div>
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
          Todos ({reports.length})
        </button>
        <button
          onClick={() => setFilter('open')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'open'
              ? 'bg-accent-magenta text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          Abiertos ({reports.filter(r => r.status === 'open').length})
        </button>
        <button
          onClick={() => setFilter('investigating')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'investigating'
              ? 'bg-accent-gold text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          Investigando ({reports.filter(r => r.status === 'investigating').length})
        </button>
        <button
          onClick={() => setFilter('in_progress')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'in_progress'
              ? 'bg-accent-blue text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          En Progreso ({reports.filter(r => r.status === 'in_progress').length})
        </button>
        <button
          onClick={() => setFilter('fixed')}
          className={`px-4 py-2 rounded-xl font-medium transition-all ${
            filter === 'fixed'
              ? 'bg-accent-green text-white'
              : 'bg-rendly-surface text-text-secondary hover:bg-rendly-surface-elevated'
          }`}
        >
          Resueltos ({reports.filter(r => r.status === 'fixed').length})
        </button>
      </div>

      {/* Reports List */}
      <div className="grid gap-4">
        {filteredReports.length === 0 ? (
          <div className="text-center py-12 text-text-tertiary bg-rendly-surface rounded-2xl border border-primary/10">
            No hay reportes con este filtro
          </div>
        ) : (
          filteredReports.map((report) => (
            <div
              key={report.id}
              className="bg-rendly-surface rounded-2xl border border-primary/10 p-5 hover:border-primary/30 transition-all cursor-pointer"
              onClick={() => {
                setSelectedReport(report)
                setAdminNotes(report.admin_notes || '')
                setResolutionNotes(report.resolution_notes || '')
              }}
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-lg">{getCategoryLabel(report.category)}</span>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(report.status)}`}>
                      {report.status}
                    </span>
                    <span className={`text-sm font-medium ${getSeverityColor(report.severity)}`}>
                      {report.severity === 'critical' && '🔴 CRÍTICO'}
                      {report.severity === 'high' && '🟠 Alto'}
                      {report.severity === 'medium' && '🟡 Medio'}
                      {report.severity === 'low' && '⚪ Bajo'}
                    </span>
                  </div>
                  <h3 className="font-semibold text-text-primary mb-1">{report.title}</h3>
                  <p className="text-text-secondary text-sm line-clamp-2">{report.description}</p>
                </div>
              </div>
              
              <div className="flex items-center justify-between text-xs text-text-muted mt-3 pt-3 border-t border-primary/10">
                <div className="flex items-center gap-3">
                  <span>Usuario: {report.user_name || 'Anónimo'}</span>
                  {report.include_device_info && <span className="text-accent-blue">📱 Device Info</span>}
                  {report.include_logs && <span className="text-primary">📋 Logs</span>}
                </div>
                <span>{new Date(report.created_at).toLocaleString('es-ES')}</span>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Modal de detalle */}
      {selectedReport && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50" onClick={() => setSelectedReport(null)}>
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <div className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h2 className="text-xl font-bold text-gray-900 mb-2">{selectedReport.title}</h2>
                  <div className="flex items-center gap-2">
                    <span className="text-2xl">{getCategoryLabel(selectedReport.category)}</span>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(selectedReport.status)}`}>
                      {selectedReport.status}
                    </span>
                  </div>
                </div>
                <button
                  onClick={() => setSelectedReport(null)}
                  className="text-gray-400 hover:text-gray-600 text-2xl"
                >
                  ×
                </button>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-gray-700 block mb-1">Descripción</label>
                  <p className="text-gray-900 bg-gray-50 p-3 rounded whitespace-pre-wrap">{selectedReport.description}</p>
                </div>

                {selectedReport.steps_to_reproduce && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Pasos para Reproducir</label>
                    <p className="text-gray-900 bg-blue-50 p-3 rounded whitespace-pre-wrap">{selectedReport.steps_to_reproduce}</p>
                  </div>
                )}

                {selectedReport.expected_behavior && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Comportamiento Esperado</label>
                    <p className="text-gray-900 bg-green-50 p-3 rounded">{selectedReport.expected_behavior}</p>
                  </div>
                )}

                {selectedReport.actual_behavior && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Comportamiento Actual</label>
                    <p className="text-gray-900 bg-red-50 p-3 rounded">{selectedReport.actual_behavior}</p>
                  </div>
                )}

                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Estado</label>
                    <select
                      value={selectedReport.status}
                      onChange={(e) => updateReportStatus(selectedReport.id, e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500"
                    >
                      <option value="open">Abierto</option>
                      <option value="investigating">Investigando</option>
                      <option value="in_progress">En Progreso</option>
                      <option value="fixed">Resuelto</option>
                      <option value="wont_fix">No se arreglará</option>
                      <option value="duplicate">Duplicado</option>
                      <option value="closed">Cerrado</option>
                    </select>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Severidad</label>
                    <select
                      value={selectedReport.severity}
                      onChange={(e) => updateReportSeverity(selectedReport.id, e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500"
                    >
                      <option value="low">Baja</option>
                      <option value="medium">Media</option>
                      <option value="high">Alta</option>
                      <option value="critical">Crítica</option>
                    </select>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">Prioridad</label>
                    <select
                      value={selectedReport.priority}
                      onChange={(e) => updateReportPriority(selectedReport.id, e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500"
                    >
                      <option value="low">Baja</option>
                      <option value="medium">Media</option>
                      <option value="high">Alta</option>
                      <option value="critical">Crítica</option>
                    </select>
                  </div>
                </div>

                {selectedReport.device_info && selectedReport.include_device_info && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">📱 Información del Dispositivo</label>
                    <div className="bg-gray-50 p-3 rounded text-sm text-gray-600 grid grid-cols-2 gap-2">
                      <div><strong>Fabricante:</strong> {selectedReport.device_info.manufacturer}</div>
                      <div><strong>Modelo:</strong> {selectedReport.device_info.model}</div>
                      <div><strong>Android:</strong> {selectedReport.device_info.android_version}</div>
                      <div><strong>SDK:</strong> {selectedReport.device_info.sdk_int}</div>
                      <div><strong>Marca:</strong> {selectedReport.device_info.brand}</div>
                      <div><strong>Dispositivo:</strong> {selectedReport.device_info.device}</div>
                      <div><strong>Pantalla:</strong> {selectedReport.device_info.screen_width}x{selectedReport.device_info.screen_height}</div>
                      <div><strong>Densidad:</strong> {selectedReport.device_info.screen_density}</div>
                      <div><strong>Memoria Total:</strong> {selectedReport.device_info.total_memory_mb} MB</div>
                      <div><strong>Memoria Libre:</strong> {selectedReport.device_info.free_memory_mb} MB</div>
                    </div>
                  </div>
                )}

                {selectedReport.app_logs && selectedReport.include_logs && (
                  <div>
                    <label className="text-sm font-medium text-gray-700 block mb-1">📋 Logs de la Aplicación</label>
                    <pre className="bg-gray-900 text-green-400 p-3 rounded text-xs overflow-x-auto max-h-64 overflow-y-auto">
                      {selectedReport.app_logs}
                    </pre>
                  </div>
                )}

                <div>
                  <label className="text-sm font-medium text-gray-700 block mb-1">Notas del Administrador</label>
                  <textarea
                    value={adminNotes}
                    onChange={(e) => setAdminNotes(e.target.value)}
                    placeholder="Notas internas sobre este bug..."
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500"
                    rows="3"
                  />
                </div>

                <div>
                  <label className="text-sm font-medium text-gray-700 block mb-1">Notas de Resolución</label>
                  <textarea
                    value={resolutionNotes}
                    onChange={(e) => setResolutionNotes(e.target.value)}
                    placeholder="¿Cómo se resolvió este bug?"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
                    rows="3"
                  />
                </div>

                <button
                  onClick={saveNotes}
                  disabled={saving || (!adminNotes.trim() && !resolutionNotes.trim())}
                  className="w-full px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? 'Guardando...' : 'Guardar Notas'}
                </button>

                <div className="text-xs text-gray-500 pt-4 border-t border-gray-200">
                  <div className="grid grid-cols-2 gap-2">
                    <div>Usuario: {selectedReport.user_name || 'Anónimo'}</div>
                    <div>Email: {selectedReport.user_email || 'No proporcionado'}</div>
                    <div>App Version: {selectedReport.app_version}</div>
                    <div>OS Version: Android {selectedReport.os_version}</div>
                    <div>Fecha: {new Date(selectedReport.created_at).toLocaleString('es-ES')}</div>
                    {selectedReport.resolved_at && (
                      <div>Resuelto: {new Date(selectedReport.resolved_at).toLocaleString('es-ES')}</div>
                    )}
                  </div>
                  <div className="mt-2">ID: {selectedReport.id}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
