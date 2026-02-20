package com.rendly.app.ui.screens.live

import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.rendly.app.data.repository.LiveStream
import com.rendly.app.data.repository.LiveStreamRepository
import com.rendly.app.data.webrtc.SignalingClient
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import org.webrtc.*

/**
 * Pantalla para ver una transmisión en vivo como espectador (Viewer)
 * Conecta via WebRTC P2P al broadcaster
 */
@Composable
fun LiveViewerScreen(
    streamId: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estado del stream - se carga desde Supabase si no está en memoria
    var stream by remember { mutableStateOf<LiveStream?>(null) }
    var isLoadingStream by remember { mutableStateOf(true) }
    
    // Cargar stream desde memoria o Supabase
    LaunchedEffect(streamId) {
        isLoadingStream = true
        // Primero buscar en memoria
        val cachedStream = LiveStreamRepository.activeStreams.value.find { it.id == streamId }
        if (cachedStream != null) {
            stream = cachedStream
        } else {
            // Si no está en memoria, buscar en Supabase
            stream = LiveStreamRepository.getStreamById(streamId)
        }
        isLoadingStream = false
    }
    
    // WebRTC components
    val signalingClient = remember { SignalingClient() }
    val eglBase = remember { mutableStateOf<EglBase?>(null) }
    val peerConnectionFactory = remember { mutableStateOf<PeerConnectionFactory?>(null) }
    val peerConnection = remember { mutableStateOf<PeerConnection?>(null) }
    val remoteVideoTrack = remember { mutableStateOf<VideoTrack?>(null) }
    
    // Estados
    var isConnecting by remember { mutableStateOf(true) }
    var isConnected by remember { mutableStateOf(false) }
    var connectionError by remember { mutableStateOf<String?>(null) }
    var viewerCount by remember { mutableStateOf(0) }
    
    // Estado de debug visible
    var connectionStatus by remember { mutableStateOf("Iniciando...") }
    
    // Animación de pulso
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    // Mostrar loading mientras se carga el stream
    if (isLoadingStream) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Color(0xFFEF4444),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cargando transmisión...",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
        return
    }
    
    // Obtener el stream actual (non-null para el resto del código)
    val currentStream = stream
    
    // Si no hay stream, mostrar error y salir
    if (currentStream == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "La transmisión no está disponible",
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onClose) {
                    Text("Volver", color = Color.White)
                }
            }
        }
        return
    }
    
    // Actualizar viewerCount cuando se carga el stream
    LaunchedEffect(currentStream) {
        viewerCount = currentStream.viewerCount
    }
    
    // Inicializar WebRTC y conectar como viewer
    LaunchedEffect(currentStream.id) {
        try {
            // Incrementar viewer count en Supabase usando buildJsonObject
            LiveStreamRepository.incrementViewerCount(currentStream.id)
            viewerCount = currentStream.viewerCount + 1
            
            // Inicializar EGL
            eglBase.value = EglBase.create()
            
            // Inicializar PeerConnectionFactory
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)
            
            val videoDecoderFactory = DefaultVideoDecoderFactory(eglBase.value!!.eglBaseContext)
            val videoEncoderFactory = DefaultVideoEncoderFactory(
                eglBase.value!!.eglBaseContext, true, true
            )
            
            peerConnectionFactory.value = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(videoDecoderFactory)
                .setVideoEncoderFactory(videoEncoderFactory)
                .createPeerConnectionFactory()
            
            connectionStatus = "Configurando WebRTC..."
            
            // Configurar callbacks de señalización
            signalingClient.onOfferReceived = { senderId, sdp ->
                connectionStatus = "✓ Offer recibido de broadcaster"
                scope.launch {
                    connectionStatus = "Creando respuesta..."
                    handleOfferFromBroadcaster(
                        senderId = senderId,
                        sdp = sdp,
                        factory = peerConnectionFactory.value!!,
                        signalingClient = signalingClient,
                        peerConnection = peerConnection,
                        remoteVideoTrack = remoteVideoTrack,
                        onConnected = {
                            connectionStatus = "✓ Conectado!"
                            isConnecting = false
                            isConnected = true
                        },
                        onError = { error ->
                            connectionStatus = "✗ Error: $error"
                            connectionError = error
                        }
                    )
                }
            }
            
            signalingClient.onIceCandidateReceived = { _, candidate ->
                peerConnection.value?.addIceCandidate(candidate)
            }
            
            // Conectar al canal de señalización como viewer
            connectionStatus = "Conectando al canal..."
            signalingClient.connect(currentStream.id, isBroadcaster = false)
            connectionStatus = "Esperando offer del broadcaster..."
            
        } catch (e: Exception) {
            connectionError = "Error: ${e.message}"
            isConnecting = false
        }
    }
    
    // Limpiar al salir
    DisposableEffect(currentStream.id) {
        onDispose {
            scope.launch {
                LiveStreamRepository.decrementViewerCount(currentStream.id)
            }
            signalingClient.disconnect()
            peerConnection.value?.close()
            peerConnectionFactory.value?.dispose()
            eglBase.value?.release()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video remoto del broadcaster
        if (isConnected && remoteVideoTrack.value != null && eglBase.value != null) {
            RemoteVideoView(
                videoTrack = remoteVideoTrack.value!!,
                eglBase = eglBase.value!!,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Overlay de UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            ViewerHeader(
                stream = currentStream,
                viewerCount = viewerCount,
                pulseAlpha = pulseAlpha,
                onClose = onClose
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Estado de conexión con debug visible
            if (isConnecting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFFEF4444),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Conectando a @${currentStream.broadcasterUsername}...",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Estado de debug visible
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = connectionStatus,
                                fontSize = 12.sp,
                                color = Color.Yellow,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            
            // Error de conexión
            connectionError?.let { error ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Manejar offer SDP del broadcaster
 */
private fun handleOfferFromBroadcaster(
    senderId: String,
    sdp: SessionDescription,
    factory: PeerConnectionFactory,
    signalingClient: SignalingClient,
    peerConnection: MutableState<PeerConnection?>,
    remoteVideoTrack: MutableState<VideoTrack?>,
    onConnected: () -> Unit,
    onError: (String) -> Unit
) {
    val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
    )
    
    val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
    }
    
    peerConnection.value = factory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate?) {
            candidate?.let { signalingClient.sendIceCandidate(senderId, it) }
        }
        
        override fun onTrack(transceiver: RtpTransceiver?) {
            transceiver?.receiver?.track()?.let { track ->
                if (track is VideoTrack) {
                    remoteVideoTrack.value = track
                    onConnected()
                }
            }
        }
        
        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
            when (state) {
                PeerConnection.IceConnectionState.CONNECTED -> onConnected()
                PeerConnection.IceConnectionState.FAILED -> onError("Conexión fallida")
                PeerConnection.IceConnectionState.DISCONNECTED -> onError("Desconectado")
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
    })
    
    // Set remote description (offer del broadcaster)
    peerConnection.value?.setRemoteDescription(object : SdpObserver {
        override fun onSetSuccess() {
            // Crear answer
            peerConnection.value?.createAnswer(object : SdpObserver {
                override fun onCreateSuccess(answerSdp: SessionDescription?) {
                    answerSdp?.let { answer ->
                        peerConnection.value?.setLocalDescription(object : SdpObserver {
                            override fun onSetSuccess() {
                                signalingClient.sendAnswer(senderId, answer)
                            }
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onCreateFailure(error: String?) {}
                            override fun onSetFailure(error: String?) {}
                        }, answer)
                    }
                }
                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String?) { onError("Error: $error") }
                override fun onSetFailure(error: String?) {}
            }, MediaConstraints())
        }
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onCreateFailure(error: String?) {}
        override fun onSetFailure(error: String?) { onError("Error: $error") }
    }, sdp)
}

/**
 * Vista de video remoto usando SurfaceViewRenderer
 */
@Composable
private fun RemoteVideoView(
    videoTrack: VideoTrack,
    eglBase: EglBase,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                init(eglBase.eglBaseContext, null)
                setEnableHardwareScaler(true)
                setMirror(false)
                videoTrack.addSink(this)
            }
        },
        modifier = modifier,
        onRelease = { renderer ->
            videoTrack.removeSink(renderer)
            renderer.release()
        }
    )
}

/**
 * Header del viewer
 */
@Composable
private fun ViewerHeader(
    stream: LiveStream,
    viewerCount: Int,
    pulseAlpha: Float,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón cerrar
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Info del broadcaster
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (stream.broadcasterAvatar != null) {
                AsyncImage(
                    model = stream.broadcasterAvatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stream.broadcasterUsername.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "@${stream.broadcasterUsername}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                stream.broadcasterStoreName?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Badge LIVE + viewers
        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Badge LIVE
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFEF4444)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer { alpha = pulseAlpha }
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Text(
                        text = "EN VIVO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Viewers
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "$viewerCount",
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
    }
}
