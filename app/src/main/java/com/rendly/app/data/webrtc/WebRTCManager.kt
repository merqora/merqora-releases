package com.rendly.app.data.webrtc

import android.content.Context
import android.util.Log
import org.webrtc.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WebRTC Manager para transmisiones en vivo P2P
 * - Captura cámara y micrófono
 * - Crea conexiones peer-to-peer
 * - Preparado para escalar a SFU en el futuro
 */
class WebRTCManager(
    private val context: Context,
    private val signalingClient: SignalingClient
) {
    companion object {
        private const val TAG = "WebRTCManager"
        
        // STUN servers gratuitos de Google
        private val ICE_SERVERS = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer()
        )
    }
    
    // Estados
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()
    
    private val _viewerCount = MutableStateFlow(0)
    val viewerCount: StateFlow<Int> = _viewerCount.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // WebRTC components
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var eglBase: EglBase? = null
    
    // Peer connections (uno por espectador en P2P, o uno al SFU en el futuro)
    private val peerConnections = mutableMapOf<String, PeerConnection>()
    
    // Local video sink para preview
    var localVideoSink: VideoSink? = null
        set(value) {
            field = value
            localVideoTrack?.addSink(value)
        }
    
    /**
     * Inicializar WebRTC - llamar una vez al inicio
     */
    fun initialize() {
        Log.d(TAG, "Inicializando WebRTC...")
        
        try {
            // Inicializar EGL context
            eglBase = EglBase.create()
            
            // Inicializar PeerConnectionFactory
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)
            
            // Crear factory con opciones de video
            val options = PeerConnectionFactory.Options()
            
            val videoEncoderFactory = DefaultVideoEncoderFactory(
                eglBase!!.eglBaseContext,
                true,  // enableIntelVp8Encoder
                true   // enableH264HighProfile
            )
            
            val videoDecoderFactory = DefaultVideoDecoderFactory(eglBase!!.eglBaseContext)
            
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(videoEncoderFactory)
                .setVideoDecoderFactory(videoDecoderFactory)
                .createPeerConnectionFactory()
            
            Log.d(TAG, "✓ WebRTC inicializado correctamente")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando WebRTC: ${e.message}", e)
            _error.value = "Error inicializando WebRTC: ${e.message}"
        }
    }
    
    /**
     * Obtener EglBase context para SurfaceViewRenderer
     */
    fun getEglBaseContext(): EglBase.Context? = eglBase?.eglBaseContext
    
    /**
     * Obtener el video track local para conectar al renderer
     */
    fun getLocalVideoTrack(): VideoTrack? = localVideoTrack
    
    /**
     * Iniciar captura de cámara
     */
    fun startCameraCapture(
        width: Int = 1280,
        height: Int = 720,
        fps: Int = 30,
        useFrontCamera: Boolean = true
    ) {
        Log.d(TAG, "Iniciando captura de cámara...")
        
        try {
            val factory = peerConnectionFactory ?: run {
                _error.value = "PeerConnectionFactory no inicializado"
                return
            }
            
            // Crear capturer de cámara
            videoCapturer = createCameraCapturer(useFrontCamera)
            if (videoCapturer == null) {
                _error.value = "No se pudo acceder a la cámara"
                return
            }
            
            // Crear surface helper
            surfaceTextureHelper = SurfaceTextureHelper.create(
                "CaptureThread",
                eglBase!!.eglBaseContext
            )
            
            // Crear video source
            val videoSource = factory.createVideoSource(videoCapturer!!.isScreencast)
            videoCapturer!!.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
            videoCapturer!!.startCapture(width, height, fps)
            
            // Crear video track
            localVideoTrack = factory.createVideoTrack("video_track", videoSource)
            localVideoTrack?.setEnabled(true)
            
            // Agregar sink si existe
            localVideoSink?.let { localVideoTrack?.addSink(it) }
            
            // Crear audio source y track
            val audioConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            }
            val audioSource = factory.createAudioSource(audioConstraints)
            localAudioTrack = factory.createAudioTrack("audio_track", audioSource)
            localAudioTrack?.setEnabled(true)
            
            Log.d(TAG, "✓ Captura de cámara iniciada")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando cámara: ${e.message}", e)
            _error.value = "Error iniciando cámara: ${e.message}"
        }
    }
    
    /**
     * Crear capturer de cámara (frontal o trasera)
     */
    private fun createCameraCapturer(useFrontCamera: Boolean): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        
        // Buscar cámara frontal o trasera según preferencia
        for (deviceName in deviceNames) {
            val isFrontFacing = enumerator.isFrontFacing(deviceName)
            if (useFrontCamera == isFrontFacing) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) {
                    Log.d(TAG, "Usando cámara: $deviceName (frontal: $isFrontFacing)")
                    return capturer
                }
            }
        }
        
        // Fallback: usar cualquier cámara disponible
        for (deviceName in deviceNames) {
            val capturer = enumerator.createCapturer(deviceName, null)
            if (capturer != null) {
                Log.d(TAG, "Fallback - usando cámara: $deviceName")
                return capturer
            }
        }
        
        return null
    }
    
    /**
     * Cambiar entre cámara frontal y trasera
     */
    fun switchCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                Log.d(TAG, "Cámara cambiada. Frontal: $isFrontCamera")
            }
            
            override fun onCameraSwitchError(errorDescription: String?) {
                Log.e(TAG, "Error cambiando cámara: $errorDescription")
            }
        })
    }
    
    /**
     * Iniciar transmisión como broadcaster
     */
    suspend fun startBroadcast(streamId: String) {
        Log.d(TAG, "Iniciando transmisión: $streamId")
        _connectionState.value = ConnectionState.Connecting
        
        try {
            // Conectar a señalización
            signalingClient.connect(streamId, isBroadcaster = true)
            
            // Escuchar nuevos espectadores
            signalingClient.onViewerJoined = { viewerId ->
                Log.d(TAG, "Nuevo espectador: $viewerId")
                createPeerConnectionForViewer(viewerId)
            }
            
            // Escuchar offers/answers de espectadores
            signalingClient.onAnswerReceived = { viewerId, sdp ->
                handleAnswerFromViewer(viewerId, sdp)
            }
            
            // Escuchar ICE candidates
            signalingClient.onIceCandidateReceived = { peerId, candidate ->
                addIceCandidate(peerId, candidate)
            }
            
            _isStreaming.value = true
            _connectionState.value = ConnectionState.Connected
            Log.d(TAG, "✓ Transmisión iniciada")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando transmisión: ${e.message}", e)
            _error.value = "Error iniciando transmisión: ${e.message}"
            _connectionState.value = ConnectionState.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Crear PeerConnection para un nuevo espectador
     */
    private fun createPeerConnectionForViewer(viewerId: String) {
        Log.d(TAG, "Creando PeerConnection para: $viewerId")
        
        val factory = peerConnectionFactory ?: return
        
        val rtcConfig = PeerConnection.RTCConfiguration(ICE_SERVERS).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        
        val peerConnection = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    Log.d(TAG, "ICE candidate local para $viewerId")
                    signalingClient.sendIceCandidate(viewerId, it)
                }
            }
            
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                Log.d(TAG, "ICE state para $viewerId: $state")
                when (state) {
                    PeerConnection.IceConnectionState.CONNECTED -> {
                        _viewerCount.value = peerConnections.size
                    }
                    PeerConnection.IceConnectionState.DISCONNECTED,
                    PeerConnection.IceConnectionState.FAILED -> {
                        removePeerConnection(viewerId)
                    }
                    else -> {}
                }
            }
            
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
        }) ?: return
        
        // Agregar tracks locales
        localVideoTrack?.let { 
            peerConnection.addTrack(it, listOf("stream"))
        }
        localAudioTrack?.let { 
            peerConnection.addTrack(it, listOf("stream"))
        }
        
        // Guardar conexión
        peerConnections[viewerId] = peerConnection
        
        // Crear y enviar offer
        createAndSendOffer(viewerId, peerConnection)
    }
    
    /**
     * Crear y enviar offer SDP al espectador
     */
    private fun createAndSendOffer(viewerId: String, peerConnection: PeerConnection) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }
        
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Log.d(TAG, "Offer creado para $viewerId")
                            signalingClient.sendOffer(viewerId, it)
                        }
                        override fun onCreateFailure(error: String?) {}
                        override fun onSetFailure(error: String?) {
                            Log.e(TAG, "Error setLocalDescription: $error")
                        }
                    }, it)
                }
            }
            
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Error creando offer: $error")
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }
    
    /**
     * Manejar answer SDP del espectador
     */
    private fun handleAnswerFromViewer(viewerId: String, sdp: SessionDescription) {
        Log.d(TAG, "Answer recibido de $viewerId")
        peerConnections[viewerId]?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                Log.d(TAG, "✓ Remote description set para $viewerId")
            }
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Error setRemoteDescription: $error")
            }
        }, sdp)
    }
    
    /**
     * Agregar ICE candidate
     */
    private fun addIceCandidate(peerId: String, candidate: IceCandidate) {
        peerConnections[peerId]?.addIceCandidate(candidate)
    }
    
    /**
     * Remover peer connection
     */
    private fun removePeerConnection(viewerId: String) {
        peerConnections[viewerId]?.close()
        peerConnections.remove(viewerId)
        _viewerCount.value = peerConnections.size
        Log.d(TAG, "Peer connection removido: $viewerId. Total: ${peerConnections.size}")
    }
    
    /**
     * Detener transmisión
     */
    fun stopBroadcast() {
        Log.d(TAG, "Deteniendo transmisión...")
        
        _isStreaming.value = false
        _connectionState.value = ConnectionState.Disconnecting
        
        // Cerrar todas las conexiones
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        
        // Desconectar señalización
        signalingClient.disconnect()
        
        _viewerCount.value = 0
        _connectionState.value = ConnectionState.Disconnected
        
        Log.d(TAG, "✓ Transmisión detenida")
    }
    
    /**
     * Habilitar/deshabilitar video
     */
    fun setVideoEnabled(enabled: Boolean) {
        localVideoTrack?.setEnabled(enabled)
        Log.d(TAG, "Video ${if (enabled) "habilitado" else "deshabilitado"}")
    }
    
    /**
     * Habilitar/deshabilitar audio
     */
    fun setAudioEnabled(enabled: Boolean) {
        localAudioTrack?.setEnabled(enabled)
        Log.d(TAG, "Audio ${if (enabled) "habilitado" else "deshabilitado"}")
    }
    
    /**
     * Liberar recursos
     */
    fun release() {
        Log.d(TAG, "Liberando recursos WebRTC...")
        
        stopBroadcast()
        
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        videoCapturer = null
        
        localVideoTrack?.dispose()
        localVideoTrack = null
        
        localAudioTrack?.dispose()
        localAudioTrack = null
        
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null
        
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        
        eglBase?.release()
        eglBase = null
        
        Log.d(TAG, "✓ Recursos WebRTC liberados")
    }
}

/**
 * Estados de conexión
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnecting : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
