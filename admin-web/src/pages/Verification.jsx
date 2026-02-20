import { useState, useEffect } from 'react'
import { 
  BadgeCheck, 
  Clock, 
  CheckCircle, 
  XCircle, 
  Eye,
  Search,
  Filter,
  RefreshCw,
  User,
  Store,
  Award,
  Calendar,
  Users,
  FileText,
  Globe,
  ExternalLink,
  ChevronDown,
  AlertTriangle,
  Shield,
  X
} from 'lucide-react'
import { supabase } from '../supabaseClient'

export default function Verification() {
  const [requests, setRequests] = useState([])
  const [stats, setStats] = useState({
    pending: 0,
    under_review: 0,
    approved: 0,
    rejected: 0,
    total: 0
  })
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('pending')
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedRequest, setSelectedRequest] = useState(null)
  const [showDetailModal, setShowDetailModal] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  const [rejectionReason, setRejectionReason] = useState('')
  const [reviewNotes, setReviewNotes] = useState('')

  useEffect(() => {
    loadRequests()
    loadStats()
  }, [filter])

  async function loadRequests() {
    setLoading(true)
    try {
      let query = supabase
        .from('verification_requests')
        .select('*')
        .order('created_at', { ascending: false })

      if (filter !== 'all') {
        query = query.eq('status', filter)
      }

      if (searchTerm) {
        query = query.or(`username.ilike.%${searchTerm}%,display_name.ilike.%${searchTerm}%,email.ilike.%${searchTerm}%`)
      }

      const { data, error } = await query.limit(100)

      if (error) throw error
      setRequests(data || [])
    } catch (error) {
      console.error('Error loading requests:', error)
    } finally {
      setLoading(false)
    }
  }

  async function loadStats() {
    try {
      const { data, error } = await supabase
        .from('verification_requests')
        .select('status')

      if (error) throw error

      const stats = {
        pending: 0,
        under_review: 0,
        approved: 0,
        rejected: 0,
        total: data?.length || 0
      }

      data?.forEach(r => {
        if (stats[r.status] !== undefined) {
          stats[r.status]++
        }
      })

      setStats(stats)
    } catch (error) {
      console.error('Error loading stats:', error)
    }
  }

  async function handleApprove(request) {
    setActionLoading(true)
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      // Update request status
      const { error: requestError } = await supabase
        .from('verification_requests')
        .update({
          status: 'approved',
          reviewed_by: user?.id,
          reviewed_at: new Date().toISOString(),
          review_notes: reviewNotes || null,
          updated_at: new Date().toISOString()
        })
        .eq('id', request.id)

      if (requestError) throw requestError

      // Update user as verified usando RPC (bypasa RLS)
      const { error: userError } = await supabase.rpc('verify_user', {
        target_user_id: request.user_id,
        v_type: request.verification_type || 'personal'
      })

      if (userError) {
        console.error('Error verificando usuario:', userError)
        throw userError
      }

      setShowDetailModal(false)
      setSelectedRequest(null)
      setReviewNotes('')
      loadRequests()
      loadStats()
    } catch (error) {
      console.error('Error approving request:', error)
      alert('Error al aprobar la solicitud')
    } finally {
      setActionLoading(false)
    }
  }

  async function handleReject(request) {
    if (!rejectionReason.trim()) {
      alert('Por favor, ingresa una razón para el rechazo')
      return
    }

    setActionLoading(true)
    try {
      const { data: { user } } = await supabase.auth.getUser()

      const { error } = await supabase
        .from('verification_requests')
        .update({
          status: 'rejected',
          reviewed_by: user?.id,
          reviewed_at: new Date().toISOString(),
          rejection_reason: rejectionReason,
          review_notes: reviewNotes || null,
          updated_at: new Date().toISOString()
        })
        .eq('id', request.id)

      if (error) throw error

      setShowDetailModal(false)
      setSelectedRequest(null)
      setRejectionReason('')
      setReviewNotes('')
      loadRequests()
      loadStats()
    } catch (error) {
      console.error('Error rejecting request:', error)
      alert('Error al rechazar la solicitud')
    } finally {
      setActionLoading(false)
    }
  }

  async function handleMarkUnderReview(request) {
    try {
      const { error } = await supabase
        .from('verification_requests')
        .update({
          status: 'under_review',
          updated_at: new Date().toISOString()
        })
        .eq('id', request.id)

      if (error) throw error
      loadRequests()
      loadStats()
    } catch (error) {
      console.error('Error updating status:', error)
    }
  }

  function getTypeIcon(type) {
    switch (type) {
      case 'personal': return <User className="w-4 h-4" />
      case 'business': return <Store className="w-4 h-4" />
      case 'brand': return <Award className="w-4 h-4" />
      default: return <User className="w-4 h-4" />
    }
  }

  function getTypeLabel(type) {
    switch (type) {
      case 'personal': return 'Personal'
      case 'business': return 'Negocio'
      case 'brand': return 'Marca'
      default: return type
    }
  }

  function getStatusBadge(status) {
    switch (status) {
      case 'pending':
        return (
          <span className="px-2 py-1 bg-yellow-500/20 text-yellow-400 rounded-full text-xs font-medium flex items-center gap-1">
            <Clock className="w-3 h-3" />
            Pendiente
          </span>
        )
      case 'under_review':
        return (
          <span className="px-2 py-1 bg-blue-500/20 text-blue-400 rounded-full text-xs font-medium flex items-center gap-1">
            <Eye className="w-3 h-3" />
            En revisión
          </span>
        )
      case 'approved':
        return (
          <span className="px-2 py-1 bg-green-500/20 text-green-400 rounded-full text-xs font-medium flex items-center gap-1">
            <CheckCircle className="w-3 h-3" />
            Aprobada
          </span>
        )
      case 'rejected':
        return (
          <span className="px-2 py-1 bg-red-500/20 text-red-400 rounded-full text-xs font-medium flex items-center gap-1">
            <XCircle className="w-3 h-3" />
            Rechazada
          </span>
        )
      default:
        return null
    }
  }

  function formatDate(dateString) {
    if (!dateString) return '-'
    return new Date(dateString).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-500/20 flex items-center justify-center">
              <BadgeCheck className="w-6 h-6 text-blue-400" />
            </div>
            Verificación de Cuentas
          </h1>
          <p className="text-text-tertiary mt-1">Gestiona las solicitudes de verificación de usuarios</p>
        </div>
        <button 
          onClick={() => { loadRequests(); loadStats(); }}
          className="p-2 bg-rendly-surface-elevated rounded-xl text-text-secondary hover:text-text-primary transition-colors"
        >
          <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div 
          className={`bg-rendly-surface rounded-xl p-4 cursor-pointer transition-all border-2 ${filter === 'pending' ? 'border-yellow-500' : 'border-transparent hover:border-primary/30'}`}
          onClick={() => setFilter('pending')}
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-yellow-500/20 flex items-center justify-center">
              <Clock className="w-5 h-5 text-yellow-400" />
            </div>
            <div>
              <p className="text-2xl font-bold text-text-primary">{stats.pending}</p>
              <p className="text-xs text-text-tertiary">Pendientes</p>
            </div>
          </div>
        </div>

        <div 
          className={`bg-rendly-surface rounded-xl p-4 cursor-pointer transition-all border-2 ${filter === 'under_review' ? 'border-blue-500' : 'border-transparent hover:border-primary/30'}`}
          onClick={() => setFilter('under_review')}
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
              <Eye className="w-5 h-5 text-blue-400" />
            </div>
            <div>
              <p className="text-2xl font-bold text-text-primary">{stats.under_review}</p>
              <p className="text-xs text-text-tertiary">En revisión</p>
            </div>
          </div>
        </div>

        <div 
          className={`bg-rendly-surface rounded-xl p-4 cursor-pointer transition-all border-2 ${filter === 'approved' ? 'border-green-500' : 'border-transparent hover:border-primary/30'}`}
          onClick={() => setFilter('approved')}
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-green-500/20 flex items-center justify-center">
              <CheckCircle className="w-5 h-5 text-green-400" />
            </div>
            <div>
              <p className="text-2xl font-bold text-text-primary">{stats.approved}</p>
              <p className="text-xs text-text-tertiary">Aprobadas</p>
            </div>
          </div>
        </div>

        <div 
          className={`bg-rendly-surface rounded-xl p-4 cursor-pointer transition-all border-2 ${filter === 'rejected' ? 'border-red-500' : 'border-transparent hover:border-primary/30'}`}
          onClick={() => setFilter('rejected')}
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-red-500/20 flex items-center justify-center">
              <XCircle className="w-5 h-5 text-red-400" />
            </div>
            <div>
              <p className="text-2xl font-bold text-text-primary">{stats.rejected}</p>
              <p className="text-xs text-text-tertiary">Rechazadas</p>
            </div>
          </div>
        </div>

        <div 
          className={`bg-rendly-surface rounded-xl p-4 cursor-pointer transition-all border-2 ${filter === 'all' ? 'border-primary' : 'border-transparent hover:border-primary/30'}`}
          onClick={() => setFilter('all')}
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-primary/20 flex items-center justify-center">
              <Shield className="w-5 h-5 text-primary" />
            </div>
            <div>
              <p className="text-2xl font-bold text-text-primary">{stats.total}</p>
              <p className="text-xs text-text-tertiary">Total</p>
            </div>
          </div>
        </div>
      </div>

      {/* Search Bar */}
      <div className="flex gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-text-muted" />
          <input
            type="text"
            placeholder="Buscar por username, nombre o email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && loadRequests()}
            className="w-full pl-10 pr-4 py-3 bg-rendly-surface border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary transition-colors"
          />
        </div>
        <button
          onClick={loadRequests}
          className="px-6 py-3 bg-primary text-white rounded-xl font-medium hover:bg-primary/80 transition-colors"
        >
          Buscar
        </button>
      </div>

      {/* Requests Table */}
      <div className="bg-rendly-surface rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-primary/10">
                <th className="text-left p-4 text-text-tertiary font-medium text-sm">Usuario</th>
                <th className="text-left p-4 text-text-tertiary font-medium text-sm">Tipo</th>
                <th className="text-left p-4 text-text-tertiary font-medium text-sm">Métricas</th>
                <th className="text-left p-4 text-text-tertiary font-medium text-sm">Fecha</th>
                <th className="text-left p-4 text-text-tertiary font-medium text-sm">Estado</th>
                <th className="text-right p-4 text-text-tertiary font-medium text-sm">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary" />
                  </td>
                </tr>
              ) : requests.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-text-muted">
                    No hay solicitudes {filter !== 'all' && `con estado "${filter}"`}
                  </td>
                </tr>
              ) : (
                requests.map((request) => (
                  <tr 
                    key={request.id} 
                    className="border-b border-primary/5 hover:bg-rendly-surface-elevated transition-colors cursor-pointer"
                    onClick={() => { setSelectedRequest(request); setShowDetailModal(true); }}
                  >
                    <td className="p-4">
                      <div className="flex items-center gap-3">
                        <img 
                          src={request.avatar_url || `https://ui-avatars.com/api/?name=${request.username}&background=A78BFA&color=fff`}
                          alt={request.username}
                          className="w-10 h-10 rounded-full object-cover"
                        />
                        <div>
                          <p className="text-text-primary font-medium">@{request.username}</p>
                          <p className="text-text-muted text-sm">{request.display_name || request.full_legal_name}</p>
                        </div>
                      </div>
                    </td>
                    <td className="p-4">
                      <div className="flex items-center gap-2 text-text-secondary">
                        {getTypeIcon(request.verification_type)}
                        <span className="text-sm">{getTypeLabel(request.verification_type)}</span>
                      </div>
                    </td>
                    <td className="p-4">
                      <div className="flex items-center gap-4 text-sm text-text-muted">
                        <span className="flex items-center gap-1">
                          <Users className="w-4 h-4" />
                          {request.followers_count?.toLocaleString() || 0}
                        </span>
                        <span className="flex items-center gap-1">
                          <FileText className="w-4 h-4" />
                          {request.posts_count?.toLocaleString() || 0}
                        </span>
                      </div>
                    </td>
                    <td className="p-4">
                      <span className="text-text-muted text-sm">{formatDate(request.created_at)}</span>
                    </td>
                    <td className="p-4">
                      {getStatusBadge(request.status)}
                    </td>
                    <td className="p-4 text-right">
                      <button
                        onClick={(e) => { e.stopPropagation(); setSelectedRequest(request); setShowDetailModal(true); }}
                        className="p-2 bg-primary/10 text-primary rounded-lg hover:bg-primary/20 transition-colors"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Detail Modal */}
      {showDetailModal && selectedRequest && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
          <div className="bg-rendly-surface rounded-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="sticky top-0 bg-rendly-surface border-b border-primary/10 p-6 flex items-center justify-between">
              <div className="flex items-center gap-4">
                <img 
                  src={selectedRequest.avatar_url || `https://ui-avatars.com/api/?name=${selectedRequest.username}&background=A78BFA&color=fff`}
                  alt={selectedRequest.username}
                  className="w-14 h-14 rounded-full object-cover"
                />
                <div>
                  <h2 className="text-xl font-bold text-text-primary flex items-center gap-2">
                    @{selectedRequest.username}
                    {getStatusBadge(selectedRequest.status)}
                  </h2>
                  <p className="text-text-muted">{selectedRequest.email}</p>
                </div>
              </div>
              <button 
                onClick={() => { setShowDetailModal(false); setSelectedRequest(null); setRejectionReason(''); setReviewNotes(''); }}
                className="p-2 hover:bg-rendly-surface-elevated rounded-lg transition-colors"
              >
                <X className="w-5 h-5 text-text-muted" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 space-y-6">
              {/* Info Grid */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="bg-rendly-bg rounded-xl p-4">
                  <p className="text-text-muted text-xs mb-1">Tipo de cuenta</p>
                  <p className="text-text-primary font-medium flex items-center gap-2">
                    {getTypeIcon(selectedRequest.verification_type)}
                    {getTypeLabel(selectedRequest.verification_type)}
                  </p>
                </div>
                <div className="bg-rendly-bg rounded-xl p-4">
                  <p className="text-text-muted text-xs mb-1">Seguidores</p>
                  <p className="text-text-primary font-medium">{selectedRequest.followers_count?.toLocaleString() || 0}</p>
                </div>
                <div className="bg-rendly-bg rounded-xl p-4">
                  <p className="text-text-muted text-xs mb-1">Siguiendo</p>
                  <p className="text-text-primary font-medium">{selectedRequest.following_count?.toLocaleString() || 0}</p>
                </div>
                <div className="bg-rendly-bg rounded-xl p-4">
                  <p className="text-text-muted text-xs mb-1">Posts</p>
                  <p className="text-text-primary font-medium">{selectedRequest.posts_count?.toLocaleString() || 0}</p>
                </div>
              </div>

              {/* Details */}
              <div className="space-y-4">
                <div className="bg-rendly-bg rounded-xl p-4">
                  <p className="text-text-muted text-xs mb-2">Nombre legal completo</p>
                  <p className="text-text-primary">{selectedRequest.full_legal_name || '-'}</p>
                </div>

                <div className="bg-rendly-bg rounded-xl p-4">
                  <p className="text-text-muted text-xs mb-2">Bio del perfil</p>
                  <p className="text-text-primary">{selectedRequest.bio || 'Sin bio'}</p>
                </div>

                <div className="bg-rendly-bg rounded-xl p-4">
                  <p className="text-text-muted text-xs mb-2">Razón de la solicitud</p>
                  <p className="text-text-primary whitespace-pre-wrap">{selectedRequest.reason_for_verification || '-'}</p>
                </div>

                {selectedRequest.notable_presence && (
                  <div className="bg-rendly-bg rounded-xl p-4">
                    <p className="text-text-muted text-xs mb-2">Presencia en otras plataformas</p>
                    <p className="text-text-primary whitespace-pre-wrap">{selectedRequest.notable_presence}</p>
                  </div>
                )}

                {selectedRequest.website_url && (
                  <div className="bg-rendly-bg rounded-xl p-4">
                    <p className="text-text-muted text-xs mb-2">Sitio web</p>
                    <a 
                      href={selectedRequest.website_url} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-primary flex items-center gap-1 hover:underline"
                    >
                      <Globe className="w-4 h-4" />
                      {selectedRequest.website_url}
                      <ExternalLink className="w-3 h-3" />
                    </a>
                  </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-rendly-bg rounded-xl p-4">
                    <p className="text-text-muted text-xs mb-1">Fecha de solicitud</p>
                    <p className="text-text-primary text-sm">{formatDate(selectedRequest.created_at)}</p>
                  </div>
                  <div className="bg-rendly-bg rounded-xl p-4">
                    <p className="text-text-muted text-xs mb-1">Cuenta creada</p>
                    <p className="text-text-primary text-sm">{formatDate(selectedRequest.account_created_at)}</p>
                  </div>
                </div>

                {selectedRequest.rejection_reason && (
                  <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-4">
                    <p className="text-red-400 text-xs mb-2 flex items-center gap-1">
                      <AlertTriangle className="w-4 h-4" />
                      Razón del rechazo
                    </p>
                    <p className="text-text-primary">{selectedRequest.rejection_reason}</p>
                  </div>
                )}
              </div>

              {/* Action Section */}
              {selectedRequest.status === 'pending' || selectedRequest.status === 'under_review' ? (
                <div className="space-y-4 border-t border-primary/10 pt-6">
                  <div>
                    <label className="block text-text-secondary text-sm mb-2">Notas de revisión (opcional)</label>
                    <textarea
                      value={reviewNotes}
                      onChange={(e) => setReviewNotes(e.target.value)}
                      placeholder="Notas internas sobre esta solicitud..."
                      className="w-full p-3 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary resize-none"
                      rows={2}
                    />
                  </div>

                  <div>
                    <label className="block text-text-secondary text-sm mb-2">Razón del rechazo (requerido si rechazas)</label>
                    <textarea
                      value={rejectionReason}
                      onChange={(e) => setRejectionReason(e.target.value)}
                      placeholder="Explica por qué se rechaza la solicitud..."
                      className="w-full p-3 bg-rendly-bg border border-primary/20 rounded-xl text-text-primary placeholder-text-muted focus:outline-none focus:border-primary resize-none"
                      rows={2}
                    />
                  </div>

                  <div className="flex gap-3">
                    {selectedRequest.status === 'pending' && (
                      <button
                        onClick={() => handleMarkUnderReview(selectedRequest)}
                        disabled={actionLoading}
                        className="flex-1 py-3 bg-blue-500/20 text-blue-400 rounded-xl font-medium hover:bg-blue-500/30 transition-colors flex items-center justify-center gap-2"
                      >
                        <Eye className="w-4 h-4" />
                        Marcar en revisión
                      </button>
                    )}
                    <button
                      onClick={() => handleReject(selectedRequest)}
                      disabled={actionLoading}
                      className="flex-1 py-3 bg-red-500/20 text-red-400 rounded-xl font-medium hover:bg-red-500/30 transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
                    >
                      {actionLoading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <XCircle className="w-4 h-4" />}
                      Rechazar
                    </button>
                    <button
                      onClick={() => handleApprove(selectedRequest)}
                      disabled={actionLoading}
                      className="flex-1 py-3 bg-green-500 text-white rounded-xl font-medium hover:bg-green-600 transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
                    >
                      {actionLoading ? <RefreshCw className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4" />}
                      Aprobar verificación
                    </button>
                  </div>
                </div>
              ) : (
                <div className="border-t border-primary/10 pt-6">
                  {selectedRequest.reviewed_at && (
                    <p className="text-text-muted text-sm">
                      Revisado el {formatDate(selectedRequest.reviewed_at)}
                    </p>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
