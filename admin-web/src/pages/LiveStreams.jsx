import { useState, useEffect, useRef, useCallback } from 'react'
import {
  Video,
  Eye,
  Clock,
  RefreshCw,
  X,
  AlertCircle,
  WifiOff,
  Heart,
  Send,
  Trash2,
  ChevronDown
} from 'lucide-react'
import { supabase } from '../supabaseClient'
import { Room, RoomEvent } from 'livekit-client'

const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || ''

function LiveStreams() {
  const [streams, setStreams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [selectedStream, setSelectedStream] = useState(null)
  const [deleting, setDeleting] = useState(false)
  const [showBulkDelete, setShowBulkDelete] = useState(false)
  const bulkRef = useRef(null)

  async function deleteStream(id) {
    try {
      setDeleting(true)
      const { data, error } = await supabase.functions.invoke('delete-live-stream', {
        body: { ids: [id] }
      })
      if (error) throw error
      if (!data?.success) throw new Error('Error al eliminar')
      setStreams(prev => prev.filter(s => s.id !== id))
    } catch (err) {
      console.error('Error deleting stream:', err)
      alert('Error al eliminar: ' + err.message)
    } finally {
      setDeleting(false)
    }
  }

  async function bulkDeleteOldStreams(days) {
    try {
      setDeleting(true)
      setShowBulkDelete(false)
      const { data, error } = await supabase.functions.invoke('delete-live-stream', {
        body: { olderThanDays: days }
      })
      if (error) throw error
      if (!data?.success) throw new Error('Error al eliminar')
      await fetchStreams()
    } catch (err) {
      console.error('Error bulk deleting:', err)
      alert('Error al eliminar: ' + err.message)
    } finally {
      setDeleting(false)
    }
  }

  async function fetchStreams() {
    try {
      setLoading(true)
      setError(null)
      const { data, error: fetchError } = await supabase
        .from('live_streams')
        .select('*')
        .order('created_at', { ascending: false })

      if (fetchError) throw fetchError
      setStreams(data || [])
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchStreams()
    const channel = supabase
      .channel('live-streams-admin')
      .on('postgres_changes', { event: 'INSERT', schema: 'public', table: 'live_streams' }, () => fetchStreams())
      .subscribe()

    return () => { supabase.removeChannel(channel) }
  }, [])

  useEffect(() => {
    function handleClick(e) {
      if (bulkRef.current && !bulkRef.current.contains(e.target)) setShowBulkDelete(false)
    }
    if (showBulkDelete) document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [showBulkDelete])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Transmisiones en Vivo</h1>
          <p className="text-text-tertiary text-sm mt-1">
            Visualiza las transmisiones desde los dispositivos
          </p>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 bg-accent-green/10 rounded-lg">
            <div className="w-2 h-2 rounded-full bg-accent-green animate-pulse" />
            <span className="text-xs text-accent-green font-medium">
              {streams.length} activa{streams.length !== 1 ? 's' : ''}
            </span>
          </div>
          <div className="relative" ref={bulkRef}>
            <button onClick={() => setShowBulkDelete(v => !v)} disabled={deleting || loading || streams.length === 0}
              className="p-2.5 bg-mercora-surface rounded-xl border border-primary/10 text-text-secondary hover:text-text-primary hover:border-primary/30 transition-all disabled:opacity-50">
              {deleting ? (
                <RefreshCw className="w-5 h-5 animate-spin" />
              ) : (
                <ChevronDown className="w-5 h-5" />
              )}
            </button>
            {showBulkDelete && (
              <div className="absolute right-0 top-full mt-2 w-56 bg-mercora-surface border border-primary/10 rounded-xl shadow-xl z-50 overflow-hidden">
                <div className="px-4 py-2.5 border-b border-primary/10">
                  <p className="text-xs font-semibold text-text-secondary">Eliminar transmisiones</p>
                </div>
                {[
                  { label: 'Mayores a 1 d\u00eda', days: 1 },
                  { label: 'Mayores a 7 d\u00edas', days: 7 },
                  { label: 'Mayores a 30 d\u00edas', days: 30 },
                  { label: 'Todas las transmisiones', days: null },
                ].map((opt) => (
                  <button key={opt.label} onClick={() => {
                    if (opt.days === null) {
                      if (confirm('¿Eliminar TODAS las transmisiones?')) bulkDeleteOldStreams(99999)
                    } else {
                      bulkDeleteOldStreams(opt.days)
                    }
                  }}
                    className="w-full text-left px-4 py-2.5 text-sm text-text-primary hover:bg-mercora-surface-elevated transition-colors flex items-center gap-2.5">
                    <Trash2 className="w-4 h-4 text-accent-magenta" />
                    {opt.label}
                  </button>
                ))}
              </div>
            )}
          </div>
          <button onClick={fetchStreams} disabled={loading}
            className="p-2.5 bg-mercora-surface rounded-xl border border-primary/10 text-text-secondary hover:text-text-primary hover:border-primary/30 transition-all disabled:opacity-50">
            <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {selectedStream ? (
        <StreamViewer
          stream={selectedStream}
          onClose={() => setSelectedStream(null)}
        />
      ) : (
        <StreamsList streams={streams} loading={loading} error={error} onSelect={setSelectedStream} onRetry={fetchStreams} onDelete={deleteStream} deleting={deleting} />
      )}
    </div>
  )
}

function StreamsList({ streams, loading, error, onSelect, onRetry, onDelete, deleting }) {
  if (error) {
    return (
      <div className="bg-mercora-surface rounded-2xl p-12 border border-primary/10 text-center">
        <AlertCircle className="w-12 h-12 text-accent-magenta mx-auto mb-4" />
        <h3 className="text-text-primary font-semibold mb-2">Error cargando transmisiones</h3>
        <p className="text-text-tertiary text-sm mb-4">{error}</p>
        <button onClick={onRetry} className="btn-primary px-6 py-2.5 rounded-xl text-white font-medium">Reintentar</button>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="bg-mercora-surface rounded-2xl p-6 border border-primary/10 animate-pulse">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-14 h-14 rounded-full bg-mercora-surface-elevated" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-mercora-surface-elevated rounded w-24" />
                <div className="h-3 bg-mercora-surface-elevated rounded w-32" />
              </div>
            </div>
            <div className="h-3 bg-mercora-surface-elevated rounded w-full mb-2" />
            <div className="h-3 bg-mercora-surface-elevated rounded w-3/4" />
          </div>
        ))}
      </div>
    )
  }

  if (streams.length === 0) {
    return (
      <div className="bg-mercora-surface rounded-2xl p-12 border border-primary/10 text-center">
        <div className="w-20 h-20 rounded-full bg-accent-magenta/10 flex items-center justify-center mx-auto mb-4">
          <Video className="w-10 h-10 text-accent-magenta" />
        </div>
        <h3 className="text-text-primary font-semibold text-lg mb-2">Sin transmisiones activas</h3>
        <p className="text-text-tertiary text-sm max-w-md mx-auto">
          Inicia una transmisión desde la sección "En Vivo" en la app de Mercora para verla aquí.
        </p>
      </div>
    )
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {streams.map((stream) => (
        <StreamCard key={stream.id} stream={stream} onSelect={() => onSelect(stream)} onDelete={onDelete} deleting={deleting} />
      ))}
    </div>
  )
}

function StreamCard({ stream, onSelect, onDelete, deleting }) {
  const [showDelete, setShowDelete] = useState(false)
  const timeAgo = (date) => {
    if (!date) return ''
    const seconds = Math.floor((new Date() - new Date(date)) / 1000)
    if (seconds < 60) return 'Ahora'
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`
    return `${Math.floor(seconds / 86400)}d`
  }

  return (
    <div
      className="bg-mercora-surface rounded-2xl p-5 border border-primary/10 hover:border-accent-magenta/50 cursor-pointer transition-all duration-300 group relative">
      <div className="flex items-center gap-4 mb-4">
        <div className="relative">
          <div className="w-14 h-14 rounded-full bg-gradient-to-br from-accent-magenta to-accent-pink flex items-center justify-center text-white font-bold text-xl">
            {stream.broadcaster_avatar ? (
              <img src={stream.broadcaster_avatar} alt="" className="w-full h-full rounded-full object-cover" />
            ) : (
              (stream.broadcaster_username || '?')[0].toUpperCase()
            )}
          </div>
          <div className="absolute -bottom-1 -right-1 w-5 h-5 rounded-full bg-accent-magenta border-2 border-mercora-surface flex items-center justify-center">
            <div className="w-2 h-2 rounded-full bg-white animate-pulse" />
          </div>
        </div>
        <div className="flex-1 min-w-0" onClick={onSelect}>
          <h3 className="text-text-primary font-semibold truncate">@{stream.broadcaster_username || 'desconocido'}</h3>
          {stream.broadcaster_store_name && <p className="text-text-tertiary text-sm truncate">{stream.broadcaster_store_name}</p>}
        </div>
        <div className="flex-shrink-0 flex items-center gap-2">
          <button onClick={(e) => { e.stopPropagation(); setShowDelete(true) }}
            className="p-1.5 rounded-lg opacity-0 group-hover:opacity-100 hover:bg-accent-magenta/20 text-text-muted hover:text-accent-magenta transition-all"
            title="Eliminar transmisi\u00f3n">
            <Trash2 className="w-4 h-4" />
          </button>
          <span className="px-2.5 py-1 bg-accent-magenta/20 text-accent-magenta text-xs font-bold rounded">LIVE</span>
        </div>
      </div>

      <div onClick={onSelect}>
        {stream.title && <p className="text-text-primary text-sm font-medium mb-3 line-clamp-2">{stream.title}</p>}

        <div className="flex items-center gap-4 text-text-tertiary text-xs">
          <div className="flex items-center gap-1.5">
            <Eye className="w-3.5 h-3.5" />
            <span>{stream.viewer_count || 0} espectadores</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Clock className="w-3.5 h-3.5" />
            <span>{timeAgo(stream.started_at || stream.created_at)}</span>
          </div>
        </div>
      </div>

      <div onClick={onSelect} className="mt-4 pt-3 border-t border-primary/10 opacity-0 group-hover:opacity-100 transition-opacity">
        <div className="flex items-center gap-2 text-accent-magenta text-sm font-medium">
          <Eye className="w-4 h-4" />
          <span>Ver transmisi\u00f3n</span>
        </div>
      </div>

      {showDelete && (
        <div className="absolute inset-0 bg-mercora-surface/95 rounded-2xl flex items-center justify-center z-20 p-5"
          onClick={(e) => e.stopPropagation()}>
          <div className="text-center">
            <Trash2 className="w-10 h-10 text-accent-magenta mx-auto mb-3" />
            <h4 className="text-text-primary font-semibold mb-1">Eliminar transmisi\u00f3n?</h4>
            <p className="text-text-tertiary text-sm mb-4">
              @{stream.broadcaster_username || 'desconocido'}{stream.title ? ` - ${stream.title}` : ''}
            </p>
            <div className="flex gap-3 justify-center">
              <button onClick={() => setShowDelete(false)}
                className="px-4 py-2 bg-mercora-surface-elevated text-text-primary rounded-xl border border-primary/10 text-sm font-medium hover:border-primary/30 transition-all">
                Cancelar
              </button>
              <button onClick={() => { setShowDelete(false); onDelete(stream.id) }} disabled={deleting}
                className="px-4 py-2 bg-accent-magenta text-white rounded-xl text-sm font-medium hover:bg-accent-magenta/80 transition-all flex items-center gap-2 disabled:opacity-50">
                {deleting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Trash2 className="w-4 h-4" />}
                {deleting ? 'Eliminando...' : 'Eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

/* â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
   StreamViewer - LiveKit Video Player
   â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• */
function StreamViewer({ stream, onClose }) {
  const videoRef = useRef(null)
  const roomRef = useRef(null)
  const channelRef = useRef(null)

  const [status, setStatus] = useState('Conectando...')
  const [isConnected, setIsConnected] = useState(false)
  const [connectionError, setConnectionError] = useState(null)
  const [viewerCount, setViewerCount] = useState(stream.viewer_count || 0)
  const [likeCount, setLikeCount] = useState(0)
  const [commentText, setCommentText] = useState('')
  const [comments, setComments] = useState([])
  const [sessionUser, setSessionUser] = useState(null)
  const retryRef = useRef(0)
  const commentsEndRef = useRef(null)

  useEffect(() => {
    commentsEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  }, [comments])

  const roomName = stream.room_name

  const sendLike = useCallback(() => {
    if (!channelRef.current) return
    channelRef.current.send({
      type: 'broadcast',
      event: 'like',
      payload: { type: 'like', userId: sessionUser?.id || 'anon', username: sessionUser?.user_metadata?.username || 'Anónimo' }
    })
  }, [sessionUser])

  const sendComment = useCallback(() => {
    if (!channelRef.current || !commentText.trim()) return
    channelRef.current.send({
      type: 'broadcast',
      event: 'comment',
      payload: { type: 'comment', userId: sessionUser?.id || 'anon', username: sessionUser?.user_metadata?.username || 'Anónimo', text: commentText.trim() }
    })
    setCommentText('')
  }, [commentText, sessionUser])

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => setSessionUser(data?.session?.user || null))
  }, [])

  useEffect(() => {
    if (!stream?.id || !roomName) {
      setConnectionError('Esta transmisión no tiene sala configurada')
      setStatus('Error')
      return
    }

    let cancelled = false
    let reconnectTimeout = null

    // Disconnect any previous room before starting a new one
    const prevRoom = roomRef.current
    if (prevRoom) {
      prevRoom.disconnect()
      roomRef.current = null
    }

    async function connect() {
      try {
        const { data: { session } } = await supabase.auth.getSession()
        if (!session) {
          if (!cancelled) setConnectionError('No autenticado'); setStatus('Error')
          return
        }

        const { data: fnData, error: fnError } = await supabase.functions.invoke('livekit-token', {
          body: { roomName, role: 'viewer' }
        })

        if (fnError) throw new Error(typeof fnError === 'string' ? fnError : fnError.message || 'Error al obtener token')

        const { token, url } = fnData
        if (!LIVEKIT_URL && !url) throw new Error('LiveKit no configurado (VITE_LIVEKIT_URL)')

        const serverUrl = url || LIVEKIT_URL
        const room = new Room({ adaptiveStream: true, dynacast: true })
        roomRef.current = room

        room.on(RoomEvent.TrackSubscribed, (track, publication, participant) => {
          if (track.kind === 'video') {
            setTimeout(() => {
              if (videoRef.current) {
                try { track.attach(videoRef.current) } catch (e) { console.warn('attach error:', e) }
                videoRef.current.play().catch(() => {})
              }
            }, 100)
            retryRef.current = 0
          }
        })

        room.on(RoomEvent.TrackUnsubscribed, (track, publication, participant) => {
          if (track.kind === 'video' && videoRef.current) {
            try { track.detach(videoRef.current) } catch (e) { /* ignore */ }
          }
        })

        room.on(RoomEvent.Disconnected, () => {
          if (!cancelled) {
            setStatus('Desconectado')
            setIsConnected(false)
          }
        })

        room.on(RoomEvent.ConnectionStateChanged, (state) => {
          if (state === 'connected') {
            setStatus('Conectado')
            setIsConnected(true)
            setConnectionError(null)
            retryRef.current = 0
          } else if (state === 'reconnecting') {
            setStatus('Reconectando...')
          } else if (state === 'disconnected') {
            if (!cancelled) {
              setStatus('Desconectado')
              setIsConnected(false)
            }
          }
        })

        room.on(RoomEvent.ParticipantConnected, () => {
          setViewerCount(room.remoteParticipants.size)
        })
        room.on(RoomEvent.ParticipantDisconnected, () => {
          setViewerCount(room.remoteParticipants.size)
        })

        // Subscribe to live interaction channel
        const interactionChannel = supabase.channel(`live-stream-${roomName}`, {
          config: { broadcast: { selfBroadcast: true } }
        })
        channelRef.current = interactionChannel

        interactionChannel.on('broadcast', { event: 'like' }, ({ payload }) => {
          setLikeCount(prev => prev + 1)
        })
        interactionChannel.on('broadcast', { event: 'comment' }, ({ payload }) => {
          setComments(prev => [...prev.slice(-49), { id: Date.now(), ...payload }])
        })
        interactionChannel.subscribe()

        await room.connect(serverUrl, token, { connectionTimeout: 45000 })

        if (!cancelled) {
          setViewerCount(room.remoteParticipants.size)
        }

      } catch (err) {
        if (!cancelled) {
          console.error('Error LiveKit:', err)
          if (retryRef.current < 3) {
            retryRef.current += 1
            setStatus('Reconectando...')
            reconnectTimeout = setTimeout(() => connect(), 3000)
          } else {
            setConnectionError(err.message || 'Error de conexión')
            setStatus('Error')
          }
        }
      }
    }

    connect()

    return () => {
      cancelled = true
      if (reconnectTimeout) clearTimeout(reconnectTimeout)
      if (roomRef.current) {
        roomRef.current.disconnect()
        roomRef.current = null
      }
      if (channelRef.current) {
        supabase.removeChannel(channelRef.current)
        channelRef.current = null
      }
    }
  }, [stream?.id, roomName])

  // Increment viewer count via API
  useEffect(() => {
    if (!stream?.id) return

    supabase.from('live_streams').select('viewer_count').eq('id', stream.id).single()
      .then(({ data }) => {
        const current = data?.viewer_count || 0
        const newCount = current + 1
        setViewerCount(newCount)
        supabase.from('live_streams').update({ viewer_count: newCount }).eq('id', stream.id).then()
      }).catch(console.error)

    return () => {
      supabase.from('live_streams').select('viewer_count').eq('id', stream.id).single()
        .then(({ data }) => {
          const current = data?.viewer_count || 1
          supabase.from('live_streams').update({ viewer_count: Math.max(0, current - 1) }).eq('id', stream.id).then()
        }).catch(console.error)
    }
  }, [stream?.id])

  return (
    <div className="fixed inset-0 z-50 bg-black flex flex-col overflow-hidden">
      {/* Video layer */}
      <video ref={videoRef} autoPlay playsInline muted={false}
        className="absolute inset-0 w-full h-full object-cover" />

      {/* Connecting overlay */}
      {!isConnected && !connectionError && (
        <div className="absolute inset-0 flex items-center justify-center bg-black/80 z-20">
          <div className="text-center">
            <div className="w-12 h-12 border-2 border-accent-magenta border-t-transparent rounded-full animate-spin mx-auto mb-4" />
            <p className="text-white text-sm font-medium">{status}</p>
            <p className="text-white/50 text-xs mt-1">Esperando al broadcaster...</p>
          </div>
        </div>
      )}

      {/* Error overlay */}
      {connectionError && (
        <div className="absolute inset-0 flex items-center justify-center bg-black/80 z-20">
          <div className="text-center max-w-md px-6">
            <WifiOff className="w-12 h-12 text-accent-magenta mx-auto mb-4" />
            <h3 className="text-white font-semibold mb-2">Error de conexión</h3>
            <p className="text-white/50 text-sm mb-4">{connectionError}</p>
            {!roomName && (
              <p className="text-white/50 text-xs mb-4">El broadcaster debe actualizar la app para usar LiveKit</p>
            )}
            <button onClick={onClose}
              className="px-6 py-2.5 bg-white/10 text-white rounded-xl border border-white/20 hover:border-white/40 transition-all text-sm font-medium">
              Volver
            </button>
          </div>
        </div>
      )}

      {/* Top gradient */}
      <div className="absolute top-0 inset-x-0 h-32 bg-gradient-to-b from-black/70 to-transparent pointer-events-none z-10" />

      {/* Header — broadcaster info + badges + close */}
      <div className="absolute top-0 inset-x-0 z-30 flex items-center gap-2 px-3 pt-3 pb-2"
        style={{ paddingTop: 'max(0.75rem, env(safe-area-inset-top))' }}>
        <div className="w-9 h-9 rounded-full bg-gradient-to-br from-accent-magenta to-accent-pink flex items-center justify-center text-white font-bold text-sm flex-shrink-0 border border-white/30">
          {stream.broadcaster_avatar ? (
            <img src={stream.broadcaster_avatar} alt="" className="w-full h-full rounded-full object-cover" />
          ) : (
            (stream.broadcaster_username || '?')[0].toUpperCase()
          )}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-white text-sm font-semibold truncate leading-tight">@{stream.broadcaster_username || 'desconocido'}</p>
          {stream.title && <p className="text-white/60 text-xs truncate leading-tight">{stream.title}</p>}
        </div>
        <div className="flex items-center gap-1.5 flex-shrink-0">
          <div className="flex items-center gap-1 px-2 py-1 bg-accent-magenta rounded-md text-white text-[11px] font-bold">
            <div className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" />
            EN VIVO
          </div>
          <div className="flex items-center gap-1 px-2 py-1 bg-black/50 rounded-md text-white text-[11px] font-semibold">
            <Eye className="w-3.5 h-3.5" />
            {viewerCount}
          </div>
          <div className="flex items-center gap-1 px-2 py-1 bg-black/50 rounded-md text-white text-[11px] font-semibold">
            <Heart className="w-3.5 h-3.5 text-accent-magenta" />
            {likeCount}
          </div>
          <button onClick={onClose}
            className="p-1.5 bg-black/50 rounded-full text-white active:bg-black/80 transition-all ml-1"
            title="Salir">
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Bottom gradient */}
      <div className="absolute bottom-0 inset-x-0 h-72 bg-gradient-to-t from-black/80 via-black/40 to-transparent pointer-events-none z-10" />

      {/* Comments + composer overlay */}
      <div className="absolute bottom-0 inset-x-0 z-30 px-3 pb-3"
        style={{ paddingBottom: 'max(0.75rem, env(safe-area-inset-bottom))' }}>
        <div className="max-h-[40vh] overflow-y-auto space-y-2.5 mb-3 pr-10 [mask-image:linear-gradient(to_bottom,transparent,black_15%)]">
          {comments.slice(-20).map((c) => (
            <div key={c.id} className="flex items-start gap-2">
              <div className="w-7 h-7 rounded-full bg-accent-magenta flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
                {(c.username || 'A')[0].toUpperCase()}
              </div>
              <div className="min-w-0">
                <p className="text-white/80 text-xs font-semibold leading-tight">{c.username || 'Anónimo'}</p>
                <p className="text-white text-sm leading-snug break-words">{c.text}</p>
              </div>
            </div>
          ))}
          <div ref={commentsEndRef} />
        </div>
        <div className="flex items-center gap-2">
          <input
            type="text"
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') sendComment() }}
            placeholder="Comentar..."
            className="flex-1 h-10 px-4 bg-white/10 backdrop-blur-sm border border-white/25 rounded-full text-white text-sm placeholder-white/50 outline-none focus:border-white/50 transition-colors"
          />
          {commentText.trim() ? (
            <button onClick={sendComment}
              className="w-10 h-10 flex items-center justify-center bg-accent-magenta rounded-full text-white active:scale-95 transition-all flex-shrink-0">
              <Send className="w-4 h-4" />
            </button>
          ) : (
            <button onClick={sendLike}
              className="w-10 h-10 flex items-center justify-center bg-white/10 backdrop-blur-sm border border-white/25 rounded-full text-accent-magenta active:scale-90 transition-all flex-shrink-0">
              <Heart className="w-5 h-5 fill-current" />
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

export default LiveStreams
