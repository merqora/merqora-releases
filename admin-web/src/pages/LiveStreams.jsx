import { useState, useEffect, useRef, useCallback, useMemo } from 'react'
import {
  Video,
  Eye,
  User,
  Calendar,
  Clock,
  ArrowLeft,
  RefreshCw,
  X,
  Maximize2,
  Minimize2,
  AlertCircle,
  Wifi,
  WifiOff,
  Heart,
  Send
} from 'lucide-react'
import { supabase } from '../supabaseClient'
import { Room, RoomEvent } from 'livekit-client'

const LIVEKIT_URL = import.meta.env.VITE_LIVEKIT_URL || ''

function LiveStreams() {
  const [streams, setStreams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [selectedStream, setSelectedStream] = useState(null)

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
      .on('postgres_changes', { event: '*', schema: 'public', table: 'live_streams' }, () => fetchStreams())
      .subscribe()

    return () => { supabase.removeChannel(channel) }
  }, [])

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
          <button onClick={fetchStreams} disabled={loading}
            className="p-2.5 bg-vinzay-surface rounded-xl border border-primary/10 text-text-secondary hover:text-text-primary hover:border-primary/30 transition-all disabled:opacity-50">
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
        <StreamsList streams={streams} loading={loading} error={error} onSelect={setSelectedStream} onRetry={fetchStreams} />
      )}
    </div>
  )
}

function StreamsList({ streams, loading, error, onSelect, onRetry }) {
  if (error) {
    return (
      <div className="bg-vinzay-surface rounded-2xl p-12 border border-primary/10 text-center">
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
          <div key={i} className="bg-vinzay-surface rounded-2xl p-6 border border-primary/10 animate-pulse">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-14 h-14 rounded-full bg-vinzay-surface-elevated" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-vinzay-surface-elevated rounded w-24" />
                <div className="h-3 bg-vinzay-surface-elevated rounded w-32" />
              </div>
            </div>
            <div className="h-3 bg-vinzay-surface-elevated rounded w-full mb-2" />
            <div className="h-3 bg-vinzay-surface-elevated rounded w-3/4" />
          </div>
        ))}
      </div>
    )
  }

  if (streams.length === 0) {
    return (
      <div className="bg-vinzay-surface rounded-2xl p-12 border border-primary/10 text-center">
        <div className="w-20 h-20 rounded-full bg-accent-magenta/10 flex items-center justify-center mx-auto mb-4">
          <Video className="w-10 h-10 text-accent-magenta" />
        </div>
        <h3 className="text-text-primary font-semibold text-lg mb-2">Sin transmisiones activas</h3>
        <p className="text-text-tertiary text-sm max-w-md mx-auto">
          Inicia una transmisión desde la sección "En Vivo" en la app de Vinzay para verla aquí.
        </p>
      </div>
    )
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {streams.map((stream) => (
        <StreamCard key={stream.id} stream={stream} onSelect={() => onSelect(stream)} />
      ))}
    </div>
  )
}

function StreamCard({ stream, onSelect }) {
  const timeAgo = (date) => {
    if (!date) return ''
    const seconds = Math.floor((new Date() - new Date(date)) / 1000)
    if (seconds < 60) return 'Ahora'
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`
    return `${Math.floor(seconds / 86400)}d`
  }

  return (
    <div onClick={onSelect}
      className="bg-vinzay-surface rounded-2xl p-5 border border-primary/10 hover:border-accent-magenta/50 cursor-pointer transition-all duration-300 group">
      <div className="flex items-center gap-4 mb-4">
        <div className="relative">
          <div className="w-14 h-14 rounded-full bg-gradient-to-br from-accent-magenta to-accent-pink flex items-center justify-center text-white font-bold text-xl">
            {stream.broadcaster_avatar ? (
              <img src={stream.broadcaster_avatar} alt="" className="w-full h-full rounded-full object-cover" />
            ) : (
              (stream.broadcaster_username || '?')[0].toUpperCase()
            )}
          </div>
          <div className="absolute -bottom-1 -right-1 w-5 h-5 rounded-full bg-accent-magenta border-2 border-Vinzay-surface flex items-center justify-center">
            <div className="w-2 h-2 rounded-full bg-white animate-pulse" />
          </div>
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="text-text-primary font-semibold truncate">@{stream.broadcaster_username || 'desconocido'}</h3>
          {stream.broadcaster_store_name && <p className="text-text-tertiary text-sm truncate">{stream.broadcaster_store_name}</p>}
        </div>
        <div className="flex-shrink-0">
          <span className="px-2.5 py-1 bg-accent-magenta/20 text-accent-magenta text-xs font-bold rounded">LIVE</span>
        </div>
      </div>

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

      <div className="mt-4 pt-3 border-t border-primary/10 opacity-0 group-hover:opacity-100 transition-opacity">
        <div className="flex items-center gap-2 text-accent-magenta text-sm font-medium">
          <Eye className="w-4 h-4" />
          <span>Ver transmisión</span>
        </div>
      </div>
    </div>
  )
}

/* ═══════════════════════════════════════════════
   StreamViewer - LiveKit Video Player
   ═══════════════════════════════════════════════ */
function StreamViewer({ stream, onClose }) {
  const videoRef = useRef(null)
  const roomRef = useRef(null)
  const channelRef = useRef(null)

  const [status, setStatus] = useState('Conectando...')
  const [isConnected, setIsConnected] = useState(false)
  const [isFullscreen, setIsFullscreen] = useState(false)
  const [connectionError, setConnectionError] = useState(null)
  const [viewerCount, setViewerCount] = useState(stream.viewer_count || 0)
  const [isMuted, setIsMuted] = useState(false)
  const [likeCount, setLikeCount] = useState(0)
  const [commentText, setCommentText] = useState('')
  const [comments, setComments] = useState([])
  const [sessionUser, setSessionUser] = useState(null)
  const retryRef = useRef(0)

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

        room.on(RoomEvent.TrackMuted, (track) => {
          if (track.kind === 'video') setIsMuted(true)
        })

        room.on(RoomEvent.TrackUnmuted, (track) => {
          if (track.kind === 'video') setIsMuted(false)
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

  const timeAgo = (date) => {
    if (!date) return ''
    const seconds = Math.floor((new Date() - new Date(date)) / 1000)
    if (seconds < 60) return 'Ahora'
    if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
    if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`
    return `${Math.floor(seconds / 86400)}d`
  }

  return (
    <div className="bg-vinzay-surface rounded-2xl overflow-hidden border border-primary/10">
      <div className="relative bg-black aspect-video flex items-center justify-center">
        {!isConnected && !connectionError && (
          <div className="absolute inset-0 flex items-center justify-center bg-black/80 z-10">
            <div className="text-center">
              <div className="w-12 h-12 border-2 border-accent-magenta border-t-transparent rounded-full animate-spin mx-auto mb-4" />
              <p className="text-white text-sm font-medium">{status}</p>
              <p className="text-text-tertiary text-xs mt-1">Esperando al broadcaster...</p>
            </div>
          </div>
        )}

        {connectionError && (
          <div className="absolute inset-0 flex items-center justify-center bg-black/80 z-10">
            <div className="text-center max-w-md px-6">
              <WifiOff className="w-12 h-12 text-accent-magenta mx-auto mb-4" />
              <h3 className="text-white font-semibold mb-2">Error de conexión</h3>
              <p className="text-text-tertiary text-sm mb-4">{connectionError}</p>
              {!roomName && (
                <p className="text-text-tertiary text-xs mb-4">El broadcaster debe actualizar la app para usar LiveKit</p>
              )}
              <button onClick={onClose}
                className="px-6 py-2.5 bg-vinzay-surface-elevated text-text-primary rounded-xl border border-primary/10 hover:border-primary/30 transition-all text-sm font-medium">
                Volver
              </button>
            </div>
          </div>
        )}

        {isConnected && (
          <div className="absolute top-3 left-3 z-10 flex items-center gap-2">
            <div className="flex items-center gap-1.5 px-2.5 py-1.5 bg-accent-magenta rounded text-white text-xs font-bold">
              <div className="w-2 h-2 rounded-full bg-white animate-pulse" />
              EN VIVO
            </div>
            {isMuted && (
              <div className="flex items-center gap-1 px-2 py-1.5 bg-yellow-600/80 rounded text-white text-xs">
                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2" />
                </svg>
                Silenciado
              </div>
            )}
            <div className="flex items-center gap-1.5 px-2.5 py-1.5 bg-black/60 rounded text-white text-xs">
              <Eye className="w-3.5 h-3.5" />
              {viewerCount}
            </div>
          </div>
        )}

        <video ref={videoRef} autoPlay playsInline muted={false} controls={isConnected}
          className="w-full h-full object-contain" />

        <div className="absolute top-3 right-3 z-10 flex items-center gap-2">
          <button onClick={() => {
            if (!document.fullscreenElement) {
              document.documentElement.requestFullscreen().catch(() => { })
            } else {
              document.exitFullscreen().catch(() => { })
            }
            setIsFullscreen(!isFullscreen)
          }}
            className="p-2 bg-black/60 rounded-lg text-white hover:bg-black/80 transition-all"
            title="Pantalla completa">
            {isFullscreen ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
          </button>
          <button onClick={onClose}
            className="p-2 bg-black/60 rounded-lg text-white hover:bg-black/80 transition-all"
            title="Cerrar">
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="p-5">
        <div className="flex items-start justify-between mb-4">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-gradient-to-br from-accent-magenta to-accent-pink flex items-center justify-center text-white font-bold text-lg flex-shrink-0">
              {stream.broadcaster_avatar ? (
                <img src={stream.broadcaster_avatar} alt="" className="w-full h-full rounded-full object-cover" />
              ) : (
                (stream.broadcaster_username || '?')[0].toUpperCase()
              )}
            </div>
            <div>
              <h2 className="text-text-primary font-semibold text-lg">@{stream.broadcaster_username || 'desconocido'}</h2>
              {stream.broadcaster_store_name && <p className="text-text-tertiary text-sm">{stream.broadcaster_store_name}</p>}
              <div className="flex items-center gap-3 mt-1 text-text-tertiary text-xs">
                <div className="flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  {timeAgo(stream.started_at || stream.created_at)}
                </div>
                <div className="flex items-center gap-1">
                  <Eye className="w-3 h-3" />
                  {viewerCount} espectadores
                </div>
                <div className="flex items-center gap-1">
                  {isConnected ? (
                    <Wifi className="w-3 h-3 text-accent-green" />
                  ) : (
                    <WifiOff className="w-3 h-3 text-text-muted" />
                  )}
                  <span>{isConnected ? 'Conectado' : status}</span>
                </div>
              </div>
            </div>
          </div>
          {isConnected && (
            <button onClick={sendLike}
              className="flex items-center gap-2 px-4 py-2.5 bg-accent-magenta/10 hover:bg-accent-magenta/20 text-accent-magenta rounded-xl border border-accent-magenta/20 hover:border-accent-magenta/40 transition-all text-sm font-medium">
              <Heart className="w-4 h-4" />
              <span>{likeCount}</span>
            </button>
          )}
        </div>

        {stream.title && (
          <div className="p-3 bg-vinzay-surface-elevated rounded-xl mb-4">
            <p className="text-text-primary text-sm font-medium">{stream.title}</p>
          </div>
        )}

        {/* Comments */}
        {isConnected && (
          <div className="space-y-3">
            <div className="max-h-48 overflow-y-auto space-y-2">
              {comments.slice(-20).map((c) => (
                <div key={c.id} className="flex items-start gap-2">
                  <div className="w-6 h-6 rounded-full bg-accent-magenta/20 flex items-center justify-center text-accent-magenta text-xs font-bold flex-shrink-0 mt-0.5">
                    {(c.username || 'A')[0].toUpperCase()}
                  </div>
                  <div>
                    <span className="text-accent-magenta text-xs font-semibold">@{c.username || 'Anónimo'}</span>
                    <p className="text-text-primary text-sm">{c.text}</p>
                  </div>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <input
                type="text"
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') sendComment() }}
                placeholder="Escribe un comentario..."
                className="flex-1 px-3 py-2 bg-vinzay-surface-elevated border border-primary/10 rounded-xl text-text-primary text-sm placeholder-text-muted outline-none focus:border-accent-magenta/50 transition-colors"
              />
              <button onClick={sendComment} disabled={!commentText.trim()}
                className="p-2 bg-accent-magenta rounded-xl text-white hover:bg-accent-magenta/80 disabled:opacity-40 transition-all">
                <Send className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default LiveStreams
