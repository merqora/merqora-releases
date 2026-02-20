package com.rendly.app.data.webrtc

import android.util.Log
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

/**
 * Cliente de señalización usando Supabase Realtime
 * Maneja el intercambio de SDP e ICE candidates entre peers
 */
class SignalingClient {
    companion object {
        private const val TAG = "SignalingClient"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var channel: RealtimeChannel? = null
    private var currentStreamId: String? = null
    private var currentUserId: String? = null
    private var isBroadcaster: Boolean = false
    
    // Callbacks
    var onViewerJoined: ((viewerId: String) -> Unit)? = null
    var onViewerLeft: ((viewerId: String) -> Unit)? = null
    var onOfferReceived: ((senderId: String, sdp: SessionDescription) -> Unit)? = null
    var onAnswerReceived: ((senderId: String, sdp: SessionDescription) -> Unit)? = null
    var onIceCandidateReceived: ((senderId: String, candidate: IceCandidate) -> Unit)? = null
    
    /**
     * Conectar al canal de señalización de un stream
     */
    suspend fun connect(streamId: String, isBroadcaster: Boolean) {
        Log.d(TAG, "=== CONECTANDO A SEÑALIZACIÓN ===")
        Log.d(TAG, "Stream ID: $streamId")
        Log.d(TAG, "Es broadcaster: $isBroadcaster")
        
        this.currentStreamId = streamId
        this.isBroadcaster = isBroadcaster
        this.currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
        
        if (currentUserId == null) {
            Log.e(TAG, "ERROR: No hay usuario autenticado")
            return
        }
        
        Log.d(TAG, "User ID: $currentUserId")
        
        try {
            // Conectar a Supabase Realtime
            val realtime = SupabaseClient.client.realtime
            try {
                realtime.connect()
                kotlinx.coroutines.delay(500)
            } catch (e: Exception) {
                if (!e.message.orEmpty().contains("already connected", ignoreCase = true)) {
                    throw e
                }
            }
            
            // Crear canal para este stream
            val channelName = "live-stream-$streamId"
            channel = SupabaseClient.client.channel(channelName)
            
            // Escuchar broadcasts de señalización
            setupSignalingListeners()
            
            // Suscribir al canal
            channel?.subscribe(blockUntilSubscribed = true)
            
            // Si es broadcaster, anunciar el stream
            if (isBroadcaster) {
                broadcastStreamStarted()
            } else {
                // Si es viewer, anunciar que se unió
                announceViewerJoined()
            }
            
            Log.d(TAG, "✓ Conectado a señalización")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error conectando a señalización: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Configurar listeners de señalización
     */
    private fun setupSignalingListeners() {
        val ch = channel ?: return
        
        // Listener para joins de viewers
        ch.broadcastFlow<Map<String, Any>>("viewer_joined").onEach { payload ->
            Log.d(TAG, ">>> EVENTO viewer_joined recibido: $payload")
            val viewerId = payload["viewer_id"]?.toString() ?: return@onEach
            Log.d(TAG, "Viewer ID: $viewerId, Current user: $currentUserId, Es broadcaster: $isBroadcaster")
            if (viewerId != currentUserId && isBroadcaster) {
                Log.d(TAG, "✓ Procesando viewer joined: $viewerId")
                onViewerJoined?.invoke(viewerId)
            }
        }.launchIn(scope)
        
        // Listener para viewer left
        ch.broadcastFlow<Map<String, Any>>("viewer_left").onEach { payload ->
            val viewerId = payload["viewer_id"]?.toString() ?: return@onEach
            if (isBroadcaster) {
                Log.d(TAG, "Viewer left: $viewerId")
                onViewerLeft?.invoke(viewerId)
            }
        }.launchIn(scope)
        
        // Listener para offers SDP
        ch.broadcastFlow<Map<String, Any>>("sdp_offer").onEach { payload ->
            Log.d(TAG, ">>> EVENTO sdp_offer recibido")
            val targetId = payload["target_id"]?.toString() ?: return@onEach
            val senderId = payload["sender_id"]?.toString() ?: return@onEach
            val sdpString = payload["sdp"]?.toString() ?: return@onEach
            
            Log.d(TAG, "Target: $targetId, Sender: $senderId, Current: $currentUserId")
            
            if (targetId == currentUserId) {
                Log.d(TAG, "✓ Offer es para mí, procesando...")
                val sdp = SessionDescription(SessionDescription.Type.OFFER, sdpString)
                onOfferReceived?.invoke(senderId, sdp)
            } else {
                Log.d(TAG, "Offer no es para mí, ignorando")
            }
        }.launchIn(scope)
        
        // Listener para answers SDP
        ch.broadcastFlow<Map<String, Any>>("sdp_answer").onEach { payload ->
            val targetId = payload["target_id"]?.toString() ?: return@onEach
            val senderId = payload["sender_id"]?.toString() ?: return@onEach
            val sdpString = payload["sdp"]?.toString() ?: return@onEach
            
            if (targetId == currentUserId) {
                Log.d(TAG, "Answer recibido de: $senderId")
                val sdp = SessionDescription(SessionDescription.Type.ANSWER, sdpString)
                onAnswerReceived?.invoke(senderId, sdp)
            }
        }.launchIn(scope)
        
        // Listener para ICE candidates
        ch.broadcastFlow<Map<String, Any>>("ice_candidate").onEach { payload ->
            val targetId = payload["target_id"]?.toString() ?: return@onEach
            val senderId = payload["sender_id"]?.toString() ?: return@onEach
            
            if (targetId == currentUserId) {
                val sdpMid = payload["sdp_mid"]?.toString() ?: return@onEach
                val sdpMLineIndex = (payload["sdp_m_line_index"] as? Number)?.toInt() ?: return@onEach
                val candidateString = payload["candidate"]?.toString() ?: return@onEach
                
                Log.d(TAG, "ICE candidate recibido de: $senderId")
                val candidate = IceCandidate(sdpMid, sdpMLineIndex, candidateString)
                onIceCandidateReceived?.invoke(senderId, candidate)
            }
        }.launchIn(scope)
    }
    
    /**
     * Anunciar que el stream ha comenzado (broadcaster)
     */
    private fun broadcastStreamStarted() {
        scope.launch {
            try {
                channel?.broadcast(
                    event = "stream_started",
                    message = buildJsonObject {
                        put("broadcaster_id", currentUserId)
                        put("stream_id", currentStreamId)
                    }
                )
                Log.d(TAG, "Stream anunciado: $currentStreamId")
            } catch (e: Exception) {
                Log.e(TAG, "Error anunciando stream: ${e.message}")
            }
        }
    }
    
    /**
     * Anunciar que un viewer se unió
     */
    private fun announceViewerJoined() {
        scope.launch {
            try {
                Log.d(TAG, "=== ANUNCIANDO VIEWER ===")
                Log.d(TAG, "Viewer ID: $currentUserId")
                Log.d(TAG, "Stream ID: $currentStreamId")
                
                channel?.broadcast(
                    event = "viewer_joined",
                    message = buildJsonObject {
                        put("viewer_id", currentUserId)
                        put("stream_id", currentStreamId)
                    }
                )
                Log.d(TAG, "✓ Viewer anunciado exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error anunciando viewer: ${e.message}", e)
            }
        }
    }
    
    /**
     * Enviar offer SDP a un viewer específico
     */
    fun sendOffer(targetId: String, sdp: SessionDescription) {
        scope.launch {
            try {
                channel?.broadcast(
                    event = "sdp_offer",
                    message = buildJsonObject {
                        put("sender_id", currentUserId)
                        put("target_id", targetId)
                        put("sdp", sdp.description)
                    }
                )
                Log.d(TAG, "Offer enviado a: $targetId")
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando offer: ${e.message}")
            }
        }
    }
    
    /**
     * Enviar answer SDP al broadcaster
     */
    fun sendAnswer(targetId: String, sdp: SessionDescription) {
        scope.launch {
            try {
                channel?.broadcast(
                    event = "sdp_answer",
                    message = buildJsonObject {
                        put("sender_id", currentUserId)
                        put("target_id", targetId)
                        put("sdp", sdp.description)
                    }
                )
                Log.d(TAG, "Answer enviado a: $targetId")
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando answer: ${e.message}")
            }
        }
    }
    
    /**
     * Enviar ICE candidate a un peer específico
     */
    fun sendIceCandidate(targetId: String, candidate: IceCandidate) {
        scope.launch {
            try {
                channel?.broadcast(
                    event = "ice_candidate",
                    message = buildJsonObject {
                        put("sender_id", currentUserId)
                        put("target_id", targetId)
                        put("sdp_mid", candidate.sdpMid)
                        put("sdp_m_line_index", candidate.sdpMLineIndex)
                        put("candidate", candidate.sdp)
                    }
                )
                Log.d(TAG, "ICE candidate enviado a: $targetId")
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando ICE candidate: ${e.message}")
            }
        }
    }
    
    /**
     * Desconectar del canal de señalización
     */
    fun disconnect() {
        Log.d(TAG, "Desconectando de señalización...")
        
        scope.launch {
            try {
                // Anunciar salida si es viewer
                if (!isBroadcaster) {
                    channel?.broadcast(
                        event = "viewer_left",
                        message = buildJsonObject {
                            put("viewer_id", currentUserId)
                            put("stream_id", currentStreamId)
                        }
                    )
                }
                
                channel?.unsubscribe()
                channel = null
                currentStreamId = null
                
                Log.d(TAG, "✓ Desconectado de señalización")
            } catch (e: Exception) {
                Log.e(TAG, "Error desconectando: ${e.message}")
            }
        }
    }
}
