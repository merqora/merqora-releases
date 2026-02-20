package com.rendly.app.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.rendly.app.data.model.CallDB
import com.rendly.app.data.model.CallEndReason
import com.rendly.app.data.model.CallState
import com.rendly.app.data.model.CallStatus
import com.rendly.app.data.model.IceCandidateDB
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.webrtc.*
import io.github.jan.supabase.functions.functions

/**
 * ═══════════════════════════════════════════════════════════════
 * CALL REPOSITORY - Motor de llamadas VoIP
 * ═══════════════════════════════════════════════════════════════
 * 
 * Arquitectura:
 * - WebRTC para audio peer-to-peer (baja latencia)
 * - Supabase Realtime para señalización (offer/answer/ICE)
 * - STUN/TURN servers para NAT traversal
 * - AudioManager para gestión de audio del dispositivo
 */
object CallRepository {
    private const val TAG = "CallRepository"
    
    // ═══ Estado de la llamada ═══
    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    
    // ═══ Llamada entrante global (para mostrar en cualquier pantalla) ═══
    private val _incomingCall = MutableStateFlow<CallState?>(null)
    val incomingCall: StateFlow<CallState?> = _incomingCall.asStateFlow()
    
    // ═══ Internos ═══
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var audioManager: AudioManager? = null
    
    // WebRTC
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localAudioSource: AudioSource? = null
    
    // Supabase Realtime channels
    private var callChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var iceChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var globalCallChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    
    // Audio
    private var ringtonePlayer: MediaPlayer? = null
    private var ringbackPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
    // Timer
    private var durationJob: Job? = null
    private var timeoutJob: Job? = null
    
    // Pending ICE candidates (received before remote description set)
    private val pendingIceCandidates = mutableListOf<IceCandidate>()
    private var isRemoteDescriptionSet = false
    
    // Guards against double-reset and double-end
    private var isResetting = false
    private var isEnding = false
    
    // STUN/TURN servers
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer()
    )
    
    // ═══════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ═══════════════════════════════════════════════════════════════
    
    private var initialized = false
    
    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        
        appContext = context.applicationContext
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        // Inicializar WebRTC
        initializeWebRTC(context)
        
        // Escuchar llamadas entrantes - con retry hasta que auth esté listo
        subscribeToIncomingCallsWithRetry()
        
        Log.d(TAG, "✅ CallRepository inicializado")
    }
    
    private fun ensureInitialized() {
        if (!initialized && appContext == null) {
            Log.e(TAG, "❌ CallRepository NO inicializado, intentando auto-init...")
        }
        if (peerConnectionFactory == null) {
            appContext?.let { 
                Log.d(TAG, "🔄 Re-inicializando WebRTC...")
                initializeWebRTC(it) 
            }
        }
    }
    
    private fun subscribeToIncomingCallsWithRetry() {
        scope.launch {
            var attempts = 0
            while (attempts < 30) { // Reintentar hasta 30 veces (5 min)
                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    Log.d(TAG, "✅ Auth listo, subscribiendo a llamadas entrantes (intento ${attempts + 1})")
                    subscribeToIncomingCalls()
                    return@launch
                }
                attempts++
                Log.d(TAG, "⏳ Auth no listo, reintentando en 10s (intento $attempts/30)")
                delay(10_000)
            }
            Log.e(TAG, "❌ No se pudo suscribir a llamadas entrantes después de 30 intentos")
        }
    }
    
    private fun initializeWebRTC(context: Context) {
        try {
            Log.d(TAG, "WebRTC: iniciando inicialización...")
            
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)
            Log.d(TAG, "WebRTC: PeerConnectionFactory.initialize() OK")
            
            // Mismo patrón que WebRTCManager.kt (live streaming) que ya funciona
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(PeerConnectionFactory.Options())
                .createPeerConnectionFactory()
            
            Log.d(TAG, "✅ WebRTC inicializado, factory=${peerConnectionFactory != null}")
        } catch (e: Throwable) {
            Log.e(TAG, "❌ Error inicializando WebRTC: ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // INICIAR LLAMADA (CALLER)
    // ═══════════════════════════════════════════════════════════════
    
    suspend fun startCall(
        calleeId: String,
        calleeUsername: String,
        calleeAvatarUrl: String,
        callType: String = "voice"
    ): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
        
        // Verificar que no hay una llamada activa
        if (_callState.value.status != CallStatus.IDLE) {
            Log.w(TAG, "Ya hay una llamada activa")
            return@withContext false
        }
        
        Log.d(TAG, "📞 Iniciando llamada a $calleeUsername ($calleeId)")
        
        // Asegurar que WebRTC esté inicializado
        ensureInitialized()
        
        try {
            // 1. Crear registro de llamada en Supabase
            val callJson = buildJsonObject {
                put("caller_id", currentUserId)
                put("callee_id", calleeId)
                put("call_type", callType)
                put("status", "ringing")
            }
            
            val result = SupabaseClient.database
                .from("calls")
                .insert(callJson) {
                    select()
                }
                .decodeSingle<CallDB>()
            
            val callId = result.id
            Log.d(TAG, "✅ Llamada creada: $callId")
            
            // 2. Actualizar estado local
            _callState.value = CallState(
                callId = callId,
                status = CallStatus.OUTGOING,
                callType = callType,
                isOutgoing = true,
                otherUserId = calleeId,
                otherUsername = calleeUsername,
                otherAvatarUrl = calleeAvatarUrl
            )
            
            // 3. Configurar audio para llamada saliente
            requestAudioFocus()
            playRingback()
            
            // 4. Crear PeerConnection y generar offer
            createPeerConnection(callId, currentUserId)
            createOffer(callId)
            
            // 5. Suscribirse a cambios de esta llamada
            subscribeToCallUpdates(callId, currentUserId)
            
            // 6. Enviar push notification al callee para despertar su app
            sendCallPushNotification(calleeId, calleeUsername, callId, callType)
            
            // 7. Timeout de 60 segundos
            timeoutJob = scope.launch {
                delay(60_000)
                if (_callState.value.status == CallStatus.OUTGOING) {
                    Log.d(TAG, "⏰ Timeout de llamada")
                    endCall(CallEndReason.TIMEOUT)
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando llamada: ${e.message}", e)
            resetState()
            false
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CONTESTAR LLAMADA (CALLEE)
    // ═══════════════════════════════════════════════════════════════
    
    suspend fun answerCall(callId: String) = withContext(Dispatchers.IO) {
        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
        
        Log.d(TAG, "📞 Contestando llamada: $callId")
        
        try {
            // Detener ringtone y vibración
            stopRingtone()
            stopVibration()
            
            // Obtener datos de la llamada
            val callDB = SupabaseClient.database
                .from("calls")
                .select { filter { eq("id", callId) } }
                .decodeSingle<CallDB>()
            
            // Verificar que la llamada sigue activa
            if (callDB.status != "ringing") {
                Log.w(TAG, "Llamada ya no está sonando: ${callDB.status}")
                resetState()
                return@withContext
            }
            
            // Actualizar estado en Supabase
            val now = java.time.OffsetDateTime.now().toString()
            SupabaseClient.database
                .from("calls")
                .update(buildJsonObject {
                    put("status", "answered")
                    put("answered_at", now)
                }) {
                    filter { eq("id", callId) }
                }
            
            // Configurar audio para llamada activa
            requestAudioFocus()
            configureAudioForCall()
            
            // Crear PeerConnection
            createPeerConnection(callId, currentUserId)
            
            // Procesar el offer del caller
            if (callDB.offerSdp != null) {
                val offer = SessionDescription(SessionDescription.Type.OFFER, callDB.offerSdp)
                peerConnection?.setRemoteDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d(TAG, "✅ Remote description (offer) set")
                        isRemoteDescriptionSet = true
                        // Procesar ICE candidates pendientes
                        pendingIceCandidates.forEach { peerConnection?.addIceCandidate(it) }
                        pendingIceCandidates.clear()
                        // Crear answer
                        scope.launch { createAnswer(callId) }
                    }
                    override fun onSetFailure(error: String?) {
                        Log.e(TAG, "Error setting remote description: $error")
                    }
                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) {}
                }, offer)
            }
            
            // Suscribirse a ICE candidates
            subscribeToCallUpdates(callId, currentUserId)
            
            // Actualizar estado UI
            _callState.value = _callState.value.copy(
                status = CallStatus.CONNECTED
            )
            _incomingCall.value = null
            
            // Iniciar timer de duración
            startDurationTimer()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error contestando llamada: ${e.message}", e)
            resetState()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // RECHAZAR / TERMINAR LLAMADA
    // ═══════════════════════════════════════════════════════════════
    
    suspend fun rejectCall(callId: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "❌ Rechazando llamada: $callId")
        stopRingtone()
        stopVibration()
        
        try {
            SupabaseClient.database
                .from("calls")
                .update(buildJsonObject {
                    put("status", "rejected")
                    put("ended_at", java.time.OffsetDateTime.now().toString())
                    put("end_reason", "rejected")
                }) {
                    filter { eq("id", callId) }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error rechazando llamada: ${e.message}")
        }
        
        _incomingCall.value = null
        resetState()
    }
    
    suspend fun endCall(reason: CallEndReason = CallEndReason.NORMAL) = withContext(Dispatchers.IO) {
        if (isEnding) return@withContext
        isEnding = true
        
        val callId = _callState.value.callId ?: run { isEnding = false; return@withContext }
        val duration = _callState.value.durationSeconds
        
        Log.d(TAG, "📞 Terminando llamada: $callId (razón: $reason, duración: ${duration}s)")
        
        // Detener sonidos
        stopRingback()
        stopRingtone()
        stopVibration()
        
        try {
            val statusStr = when (reason) {
                CallEndReason.NORMAL -> "ended"
                CallEndReason.MISSED -> "missed"
                CallEndReason.REJECTED -> "rejected"
                CallEndReason.BUSY -> "busy"
                CallEndReason.NETWORK_ERROR -> "ended"
                CallEndReason.TIMEOUT -> "missed"
            }
            
            SupabaseClient.database
                .from("calls")
                .update(buildJsonObject {
                    put("status", statusStr)
                    put("ended_at", java.time.OffsetDateTime.now().toString())
                    put("duration_seconds", duration)
                    put("end_reason", reason.name.lowercase())
                }) {
                    filter { eq("id", callId) }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error terminando llamada: ${e.message}")
        }
        
        // Actualizar UI con estado finalizado
        _callState.value = _callState.value.copy(
            status = CallStatus.ENDED,
            endReason = reason
        )
        
        // Limpiar después de mostrar brevemente el estado
        scope.launch {
            delay(1500)
            resetState()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CONTROLES EN LLAMADA
    // ═══════════════════════════════════════════════════════════════
    
    fun toggleMute() {
        val newMuted = !_callState.value.isMuted
        localAudioTrack?.setEnabled(!newMuted)
        _callState.value = _callState.value.copy(isMuted = newMuted)
        Log.d(TAG, "🎤 Mute: $newMuted")
    }
    
    fun toggleSpeaker() {
        val newSpeaker = !_callState.value.isSpeakerOn
        setSpeakerOutput(newSpeaker)
        adjustVolumeForMode(newSpeaker)
        _callState.value = _callState.value.copy(isSpeakerOn = newSpeaker)
        Log.d(TAG, "🔊 Speaker: $newSpeaker")
    }
    
    /**
     * Adjust STREAM_VOICE_CALL volume based on speaker/earpiece mode.
     * Speaker mode uses lower volume (~50%) to prevent acoustic echo feedback.
     * Earpiece mode uses higher volume (~80%) for comfortable listening.
     */
    private fun adjustVolumeForMode(isSpeaker: Boolean) {
        val am = audioManager ?: return
        val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        val targetVol = if (isSpeaker) {
            // Speaker: lower volume to prevent echo/feedback loop
            (maxVol * 0.5f).toInt().coerceAtLeast(1)
        } else {
            // Earpiece: comfortable volume
            (maxVol * 0.8f).toInt().coerceAtLeast(maxVol - 2)
        }
        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, targetVol, 0)
        Log.d(TAG, "🔊 Volume adjusted: $targetVol/$maxVol (speaker=$isSpeaker)")
    }
    
    private fun setSpeakerOutput(enabled: Boolean) {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: use setCommunicationDevice API
            if (enabled) {
                val speakerDevice = am.availableCommunicationDevices
                    .firstOrNull { it.type == android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                if (speakerDevice != null) {
                    am.setCommunicationDevice(speakerDevice)
                    Log.d(TAG, "🔊 setCommunicationDevice → SPEAKER")
                }
            } else {
                am.clearCommunicationDevice()
                Log.d(TAG, "🔊 clearCommunicationDevice → EARPIECE")
            }
        } else {
            am.isSpeakerphoneOn = enabled
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // WEBRTC - PeerConnection
    // ═══════════════════════════════════════════════════════════════
    
    private fun createPeerConnection(callId: String, currentUserId: String) {
        Log.d(TAG, "🔧 createPeerConnection: factory=${peerConnectionFactory != null}")
        
        if (peerConnectionFactory == null) {
            Log.e(TAG, "❌ PeerConnectionFactory es NULL, reintentando inicialización...")
            appContext?.let { initializeWebRTC(it) }
        }
        
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            iceTransportsType = PeerConnection.IceTransportsType.ALL
        }
        
        peerConnection = peerConnectionFactory?.createPeerConnection(config, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    Log.d(TAG, "🧊 ICE candidate local: ${it.sdpMid}")
                    scope.launch {
                        sendIceCandidate(callId, currentUserId, it)
                    }
                }
            }
            
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "🧊 ICE state: $state")
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        Log.d(TAG, "✅ Llamada conectada (ICE)")
                        stopRingback()
                        scope.launch(Dispatchers.Main) {
                            if (_callState.value.status != CallStatus.CONNECTED) {
                                _callState.value = _callState.value.copy(status = CallStatus.CONNECTED)
                                configureAudioForCall()
                                startDurationTimer()
                            }
                        }
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED -> {
                        Log.w(TAG, "⚠️ ICE desconectado - reconectando")
                        scope.launch(Dispatchers.Main) {
                            _callState.value = _callState.value.copy(status = CallStatus.RECONNECTING)
                        }
                    }
                    PeerConnection.IceConnectionState.FAILED -> {
                        Log.e(TAG, "❌ ICE falló")
                        scope.launch { endCall(CallEndReason.NETWORK_ERROR) }
                    }
                    else -> {}
                }
            }
            
            override fun onAddStream(stream: MediaStream?) {
                Log.d(TAG, "📺 Stream remoto recibido")
            }
            
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                Log.d(TAG, "Signal state: $state")
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
        })
        
        // Agregar audio track
        localAudioSource = peerConnectionFactory?.createAudioSource(MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        })
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio0", localAudioSource)
        localAudioTrack?.setEnabled(true)
        
        peerConnection?.addTrack(localAudioTrack, listOf("stream0"))
        
        Log.d(TAG, "✅ PeerConnection creada: pc=${peerConnection != null}, audioTrack=${localAudioTrack != null}")
    }
    
    private fun createOffer(callId: String) {
        Log.d(TAG, "📝 createOffer: callId=$callId, peerConnection=${peerConnection != null}")
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }
        
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                Log.d(TAG, "📝 createOffer onCreateSuccess: sdp=${sdp != null}, type=${sdp?.type}")
                sdp?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            Log.d(TAG, "✅ Local description (offer) set")
                            scope.launch {
                                // Guardar offer en Supabase
                                try {
                                    SupabaseClient.database
                                        .from("calls")
                                        .update(buildJsonObject {
                                            put("offer_sdp", it.description)
                                        }) {
                                            filter { eq("id", callId) }
                                        }
                                    Log.d(TAG, "✅ Offer SDP guardado en Supabase")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error guardando offer: ${e.message}")
                                }
                            }
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Error setting local desc: $error")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Error creando offer: $error")
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }
    
    private fun createAnswer(callId: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }
        
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            Log.d(TAG, "✅ Local description (answer) set")
                            scope.launch {
                                try {
                                    SupabaseClient.database
                                        .from("calls")
                                        .update(buildJsonObject {
                                            put("answer_sdp", it.description)
                                        }) {
                                            filter { eq("id", callId) }
                                        }
                                    Log.d(TAG, "✅ Answer SDP guardado en Supabase")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error guardando answer: ${e.message}")
                                }
                            }
                        }
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Error setting local desc (answer): $error")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Error creando answer: $error")
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ICE CANDIDATES
    // ═══════════════════════════════════════════════════════════════
    
    private suspend fun sendIceCandidate(callId: String, senderId: String, candidate: IceCandidate) {
        try {
            Log.d(TAG, "🧊 Enviando ICE candidate: callId=$callId, senderId=$senderId, sdpMid=${candidate.sdpMid}, sdpMLineIndex=${candidate.sdpMLineIndex}")
            SupabaseClient.database
                .from("call_ice_candidates")
                .insert(buildJsonObject {
                    put("call_id", callId)
                    put("sender_id", senderId)
                    put("candidate", candidate.sdp)
                    put("sdp_mid", candidate.sdpMid)
                    put("sdp_m_line_index", candidate.sdpMLineIndex)
                })
            Log.d(TAG, "✅ ICE candidate enviado OK")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando ICE candidate: ${e.message}", e)
        }
    }
    
    private fun processRemoteIceCandidate(candidateStr: String, sdpMid: String?, sdpMLineIndex: Int?) {
        val candidate = IceCandidate(
            sdpMid ?: "",
            sdpMLineIndex ?: 0,
            candidateStr
        )
        
        if (isRemoteDescriptionSet) {
            peerConnection?.addIceCandidate(candidate)
        } else {
            pendingIceCandidates.add(candidate)
            Log.d(TAG, "🧊 ICE candidate pendiente (esperando remote description)")
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // SUPABASE REALTIME - SEÑALIZACIÓN
    // ═══════════════════════════════════════════════════════════════
    
    private fun subscribeToIncomingCalls() {
        scope.launch {
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                if (currentUserId == null) {
                    Log.e(TAG, "❌ subscribeToIncomingCalls: currentUserId es NULL, no se puede suscribir")
                    return@launch
                }
                Log.d(TAG, "📲 subscribeToIncomingCalls: userId=$currentUserId")
                
                globalCallChannel = SupabaseClient.client.channel("global-calls-$currentUserId")
                
                globalCallChannel?.postgresChangeFlow<PostgresAction.Insert>(
                    schema = "public"
                ) {
                    table = "calls"
                    filter = "callee_id=eq.$currentUserId"
                }?.onEach { change ->
                    try {
                        val record = change.record
                        val callId = record["id"]?.jsonPrimitive?.content ?: return@onEach
                        val callerId = record["caller_id"]?.jsonPrimitive?.content ?: return@onEach
                        val callType = record["call_type"]?.jsonPrimitive?.content ?: "voice"
                        val status = record["status"]?.jsonPrimitive?.content ?: "ringing"
                        
                        if (status != "ringing") return@onEach
                        
                        // Verificar si ya estamos en una llamada
                        if (_callState.value.status != CallStatus.IDLE) {
                            // Rechazar automáticamente - ocupado
                            SupabaseClient.database
                                .from("calls")
                                .update(buildJsonObject {
                                    put("status", "busy")
                                    put("ended_at", java.time.OffsetDateTime.now().toString())
                                    put("end_reason", "busy")
                                }) {
                                    filter { eq("id", callId) }
                                }
                            return@onEach
                        }
                        
                        // Obtener info del caller
                        val callerInfo = try {
                            SupabaseClient.database
                                .from("usuarios")
                                .select { filter { eq("user_id", callerId) } }
                                .decodeSingleOrNull<com.rendly.app.data.model.Usuario>()
                        } catch (_: Exception) { null }
                        
                        val incomingState = CallState(
                            callId = callId,
                            status = CallStatus.INCOMING,
                            callType = callType,
                            isOutgoing = false,
                            otherUserId = callerId,
                            otherUsername = callerInfo?.username ?: "Usuario",
                            otherAvatarUrl = callerInfo?.avatarUrl ?: ""
                        )
                        
                        _callState.value = incomingState
                        _incomingCall.value = incomingState
                        
                        // Reproducir ringtone y vibrar
                        playRingtone()
                        startVibration()
                        
                        Log.d(TAG, "📲 Llamada entrante de ${callerInfo?.username}")
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando llamada entrante: ${e.message}")
                    }
                }?.launchIn(scope)
                
                globalCallChannel?.subscribe()
                Log.d(TAG, "✅ Escuchando llamadas entrantes")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribiendo a llamadas: ${e.message}")
            }
        }
    }
    
    private fun subscribeToCallUpdates(callId: String, currentUserId: String) {
        scope.launch {
            try {
                // Canal para cambios en la llamada (answer_sdp, status)
                callChannel = SupabaseClient.client.channel("call-$callId")
                
                callChannel?.postgresChangeFlow<PostgresAction.Update>(
                    schema = "public"
                ) {
                    table = "calls"
                    filter = "id=eq.$callId"
                }?.onEach { change ->
                    try {
                        val record = change.record
                        val status = record["status"]?.jsonPrimitive?.content ?: ""
                        val answerSdp = record["answer_sdp"]?.let { 
                            if (it is JsonNull) null else it.jsonPrimitive.content 
                        }
                        
                        Log.d(TAG, "📡 Call update: status=$status, hasAnswer=${answerSdp != null}")
                        
                        when (status) {
                            "answered" -> {
                                if (_callState.value.isOutgoing && answerSdp != null) {
                                    // Caller recibió el answer
                                    stopRingback()
                                    val answer = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
                                    peerConnection?.setRemoteDescription(object : SdpObserver {
                                        override fun onSetSuccess() {
                                            Log.d(TAG, "✅ Remote description (answer) set")
                                            isRemoteDescriptionSet = true
                                            pendingIceCandidates.forEach { peerConnection?.addIceCandidate(it) }
                                            pendingIceCandidates.clear()
                                        }
                                        override fun onSetFailure(error: String?) {
                                            Log.e(TAG, "Error setting answer: $error")
                                        }
                                        override fun onCreateSuccess(p0: SessionDescription?) {}
                                        override fun onCreateFailure(p0: String?) {}
                                    }, answer)
                                    
                                    timeoutJob?.cancel()
                                }
                            }
                            "ended", "missed", "rejected", "busy" -> {
                                // Skip if already ended/cleaning up (prevents double-reset crash on receiver)
                                if (_callState.value.status == CallStatus.ENDED || _callState.value.status == CallStatus.IDLE) {
                                    Log.d(TAG, "📡 Ignorando update '$status' - ya estamos en ${_callState.value.status}")
                                    return@onEach
                                }
                                
                                val reason = when (status) {
                                    "missed" -> CallEndReason.MISSED
                                    "rejected" -> CallEndReason.REJECTED
                                    "busy" -> CallEndReason.BUSY
                                    else -> CallEndReason.NORMAL
                                }
                                stopRingback()
                                stopRingtone()
                                stopVibration()
                                timeoutJob?.cancel()
                                
                                _callState.value = _callState.value.copy(
                                    status = CallStatus.ENDED,
                                    endReason = reason
                                )
                                _incomingCall.value = null
                                
                                scope.launch {
                                    delay(1500)
                                    resetState()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando call update: ${e.message}")
                    }
                }?.launchIn(scope)
                
                // Canal para ICE candidates del otro usuario
                iceChannel = SupabaseClient.client.channel("ice-$callId")
                
                iceChannel?.postgresChangeFlow<PostgresAction.Insert>(
                    schema = "public"
                ) {
                    table = "call_ice_candidates"
                    filter = "call_id=eq.$callId"
                }?.onEach { change ->
                    try {
                        val record = change.record
                        val senderId = record["sender_id"]?.jsonPrimitive?.content ?: ""
                        
                        // Solo procesar ICE candidates del OTRO usuario
                        if (senderId == currentUserId) return@onEach
                        
                        val candidate = record["candidate"]?.jsonPrimitive?.content ?: return@onEach
                        val sdpMid = record["sdp_mid"]?.let {
                            if (it is JsonNull) null else it.jsonPrimitive.content
                        }
                        val sdpMLineIndex = record["sdp_m_line_index"]?.let {
                            if (it is JsonNull) null else it.jsonPrimitive.content?.toIntOrNull()
                        }
                        
                        Log.d(TAG, "🧊 ICE candidate remoto recibido: $sdpMid")
                        processRemoteIceCandidate(candidate, sdpMid, sdpMLineIndex)
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando ICE candidate: ${e.message}")
                    }
                }?.launchIn(scope)
                
                callChannel?.subscribe()
                iceChannel?.subscribe()
                
                Log.d(TAG, "✅ Suscrito a updates de llamada $callId")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribiendo a call updates: ${e.message}")
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // AUDIO MANAGEMENT
    // ═══════════════════════════════════════════════════════════════
    
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioManager?.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }
    }
    
    private fun configureAudioForCall() {
        val am = audioManager ?: return
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        
        // Set volume based on current speaker/earpiece mode
        adjustVolumeForMode(_callState.value.isSpeakerOn)
        
        // Apply speaker state
        setSpeakerOutput(_callState.value.isSpeakerOn)
    }
    
    private fun playRingtone() {
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtonePlayer = MediaPlayer().apply {
                setDataSource(appContext!!, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reproduciendo ringtone: ${e.message}")
        }
    }
    
    private fun stopRingtone() {
        ringtonePlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        ringtonePlayer = null
    }
    
    private fun playRingback() {
        try {
            val toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringbackPlayer = MediaPlayer().apply {
                setDataSource(appContext!!, toneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setVolume(0.3f, 0.3f)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reproduciendo ringback: ${e.message}")
        }
    }
    
    private fun stopRingback() {
        ringbackPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        ringbackPlayer = null
    }
    
    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 1000, 500, 1000, 500)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 500, 1000, 500), 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrando: ${e.message}")
        }
    }
    
    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error deteniendo vibración: ${e.message}")
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // TIMER & CLEANUP
    // ═══════════════════════════════════════════════════════════════
    
    private fun startDurationTimer() {
        durationJob?.cancel()
        durationJob = scope.launch {
            while (true) {
                delay(1000)
                if (_callState.value.status == CallStatus.CONNECTED) {
                    _callState.value = _callState.value.copy(
                        durationSeconds = _callState.value.durationSeconds + 1
                    )
                }
            }
        }
    }
    
    private fun resetState() {
        // Guard against double-reset (prevents native crash on double dispose)
        if (isResetting) return
        isResetting = true
        
        // WebRTC cleanup - dispose() implicitly closes, no need for close()
        try {
            val pc = peerConnection
            peerConnection = null
            val at = localAudioTrack
            localAudioTrack = null
            val as2 = localAudioSource
            localAudioSource = null
            
            pc?.dispose()
            at?.dispose()
            as2?.dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing WebRTC resources: ${e.message}")
        }
        
        // Realtime cleanup
        scope.launch {
            try {
                callChannel?.unsubscribe()
                iceChannel?.unsubscribe()
            } catch (_: Exception) {}
            callChannel = null
            iceChannel = null
        }
        
        // Timer cleanup
        durationJob?.cancel()
        timeoutJob?.cancel()
        
        // Audio cleanup
        stopRingtone()
        stopRingback()
        stopVibration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager?.clearCommunicationDevice()
        }
        audioManager?.mode = AudioManager.MODE_NORMAL
        audioManager?.isSpeakerphoneOn = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        }
        
        // State cleanup
        isRemoteDescriptionSet = false
        pendingIceCandidates.clear()
        _callState.value = CallState()
        _incomingCall.value = null
        
        // Reset guards
        isResetting = false
        isEnding = false
        
        Log.d(TAG, "🧹 Estado limpiado")
    }
    
    // ═══════════════════════════════════════════════════════════════
    // FCM PUSH NOTIFICATION PARA LLAMADAS
    // ═══════════════════════════════════════════════════════════════
    
    private suspend fun sendCallPushNotification(
        calleeId: String,
        calleeUsername: String,
        callId: String,
        callType: String
    ) {
        try {
            // Obtener FCM tokens del callee
            val tokens = SupabaseClient.database
                .from("fcm_tokens")
                .select { filter { 
                    eq("user_id", calleeId)
                    eq("is_active", true)
                } }
                .decodeList<FcmTokenDB>()
                .map { it.token }
            
            if (tokens.isEmpty()) {
                Log.w(TAG, "No FCM tokens para $calleeId")
                return
            }
            
            // Obtener nombre del caller actual
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return
            val callerInfo = try {
                SupabaseClient.database
                    .from("usuarios")
                    .select { filter { eq("user_id", currentUserId) } }
                    .decodeSingleOrNull<com.rendly.app.data.model.Usuario>()
            } catch (_: Exception) { null }
            
            val callerName = callerInfo?.username ?: "Usuario"
            val callerAvatar = callerInfo?.avatarUrl ?: ""
            
            // Enviar via Edge Function send-fcm-v1
            val payload = buildJsonObject {
                put("tokens", kotlinx.serialization.json.JsonArray(tokens.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                put("title", "Llamada entrante")
                put("body", "$callerName te está llamando")
                put("data", buildJsonObject {
                    put("type", "call")
                    put("call_id", callId)
                    put("caller_id", currentUserId)
                    put("caller_name", callerName)
                    put("caller_avatar", callerAvatar)
                    put("call_type", callType)
                })
            }
            
            SupabaseClient.client.functions.invoke(
                function = "send-fcm-v1",
                body = payload
            )
            
            Log.d(TAG, "✅ Push notification enviada para llamada $callId")
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando push de llamada: ${e.message}")
        }
    }
    
    @kotlinx.serialization.Serializable
    private data class FcmTokenDB(
        val id: String = "",
        val token: String = "",
        @kotlinx.serialization.SerialName("user_id") val userId: String = ""
    )
    
    /** Formato de duración mm:ss */
    fun formatDuration(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
    }
}
