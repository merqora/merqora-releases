package com.mercora.app.ui.screens.publish

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mercora.app.data.livekit.LiveComment
import com.mercora.app.data.livekit.LiveInteractionManager
import com.mercora.app.ui.theme.*
import com.mercora.app.data.livekit.LiveKitManager
import com.mercora.app.data.livekit.LiveKitState
import com.mercora.app.data.livekit.LiveReaction
import com.mercora.app.data.repository.LiveStreamRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

private val PinkAccent = Color(0xFFFF2E63)
private val BlueAccent = Color(0xFF2D7FF9)
private val DarkBg = Color(0xFF141118)
private val SurfaceDark = Color(0xFF1E1A24)
private val GlassBg = Color(0x33211D2B)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveStreamScreen(
    onClose: () -> Unit,
    onModeSelected: (Int) -> Unit,
    currentModeIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val liveKitManager = remember { LiveKitManager(context) }
    val liveKitState by liveKitManager.state.collectAsState()
    val viewerCount by liveKitManager.viewerCount.collectAsState()

    var title by remember { mutableStateOf("") }
    var useFrontCamera by remember { mutableStateOf(true) }
    var isAudioEnabled by remember { mutableStateOf(true) }
    var streamId by remember { mutableStateOf<String?>(null) }
    var roomName by remember { mutableStateOf<String?>(null) }
    var showTitleDialog by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isLiveKitReady by remember { mutableStateOf(false) }

    var commentInput by remember { mutableStateOf("") }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    var interactionManager by remember { mutableStateOf<LiveInteractionManager?>(null) }

    val comments by interactionManager?.comments?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val likeCount by interactionManager?.likeCount?.collectAsState() ?: remember { mutableStateOf(0) }

    val pendingReactions = remember { mutableStateListOf<FloatingReaction>() }
    val chatListState = rememberLazyListState()

    LaunchedEffect(interactionManager) {
        interactionManager?.reactions?.collect { reaction ->
            val id = UUID.randomUUID().toString()
            pendingReactions.add(FloatingReaction(id, reaction.type, System.currentTimeMillis()))
            if (pendingReactions.size > 30) pendingReactions.removeAt(0)
        }
    }

    LaunchedEffect(comments) {
        if (comments.isNotEmpty()) {
            chatListState.animateScrollToItem(comments.size - 1)
        }
    }

    fun startCameraX() {
        scope.launch {
            try {
                val previewView = previewViewRef.value ?: return@launch
                val provider = context.getCameraProvider()
                provider.unbindAll()
                cameraProvider = provider
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(if (useFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
                    .build()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
            } catch (e: Exception) {
                Log.e("LiveStreamScreen", "Error CameraX: ${e.message}")
            }
        }
    }

    suspend fun stopCameraX() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        delay(600)
    }

    fun endStream() {
        scope.launch {
            if (streamId != null) {
                liveKitManager.disconnect()
                LiveStreamRepository.endStream(streamId!!)
                interactionManager?.disconnect()
            }
            streamId = null
            roomName = null
            isLiveKitReady = false
            elapsedSeconds = 0
            interactionManager = null
            if (cameraPermission.status.isGranted) startCameraX()
        }
    }

    fun toggleLike() {
        interactionManager?.let { mgr ->
            scope.launch {
                val session = com.mercora.app.data.remote.SupabaseClient.auth.currentSessionOrNull()
                val uid = session?.user?.id ?: "local"
                val uname = session?.user?.userMetadata?.get("username")?.toString() ?: "Yo"
                mgr.sendLike(uid, uname)
            }
        }
    }

    fun sendComment() {
        val text = commentInput.trim()
        if (text.isEmpty()) return
        interactionManager?.let { mgr ->
            scope.launch {
                val session = com.mercora.app.data.remote.SupabaseClient.auth.currentSessionOrNull()
                val uid = session?.user?.id ?: "local"
                val uname = session?.user?.userMetadata?.get("username")?.toString() ?: "Yo"
                mgr.sendComment(uid, uname, text)
            }
        }
        commentInput = ""
    }

    LaunchedEffect(cameraPermission.status.isGranted, useFrontCamera) {
        if (cameraPermission.status.isGranted && liveKitState !is LiveKitState.Connected) {
            startCameraX()
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(cameraPermission.status.isGranted) {
        if (cameraPermission.status.isGranted && !audioPermission.status.isGranted) {
            audioPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(liveKitState) {
        when (liveKitState) {
            is LiveKitState.Connected -> {
                liveKitManager.enableCamera(useFrontCamera)
                if (isAudioEnabled) liveKitManager.enableMicrophone()
                isLiveKitReady = true
            }
            is LiveKitState.Error -> isLiveKitReady = false
            is LiveKitState.Disconnected -> isLiveKitReady = false
            else -> {}
        }
    }

    LaunchedEffect(isLiveKitReady) {
        while (isLiveKitReady) {
            delay(1000)
            elapsedSeconds++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            liveKitManager.disconnect()
            interactionManager?.disconnect()
        }
    }

    if (showTitleDialog) {
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = { Text("Título de la transmisión", fontWeight = FontWeight.Bold, color = TextPrimary) },
            containerColor = Surface,
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Ej: Venta especial hoy!", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PinkAccent, unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = PinkAccent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTitleDialog = false
                        val id = UUID.randomUUID().toString()
                        val rName = UUID.randomUUID().toString()
                        streamId = id
                        roomName = rName
                        val mgr = LiveInteractionManager(rName)
                        interactionManager = mgr
                        mgr.connect()
                        scope.launch {
                            isLiveKitReady = false
                            stopCameraX()
                            liveKitManager.connectAsBroadcaster(rName)
                            LiveStreamRepository.startStream(id, rName, title.ifBlank { "En vivo" })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkAccent)
                ) { Text("Iniciar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showTitleDialog = false }) { Text("Cancelar", color = TextMuted) }
            }
        )
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• MAIN UI â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // MAIN UI
    Box(
        modifier = modifier.fillMaxSize().background(DarkBg)
    ) {
        val isLive = liveKitState is LiveKitState.Connected && isLiveKitReady

        if (isLive) {
            // BROADCASTER DASHBOARD
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            ) {
                // Stream Info Bar
                StreamInfoBar(
                    viewerCount = viewerCount,
                    likeCount = likeCount,
                    elapsedSeconds = elapsedSeconds
                )

                // Main Dashboard Content (scrollable)
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Camera Preview Panel
                    item {
                        CameraPreviewPanel(
                            liveKitManager = liveKitManager,
                            useFrontCamera = useFrontCamera,
                            cameraPermissionGranted = cameraPermission.status.isGranted,
                            previewViewRef = previewViewRef,
                            isAudioEnabled = isAudioEnabled,
                            onToggleCamera = {
                                useFrontCamera = !useFrontCamera
                                if (cameraPermission.status.isGranted) startCameraX()
                            },
                            onToggleMic = { isAudioEnabled = !isAudioEnabled }
                        )
                    }

                    // Comments Moderation Panel
                    item {
                        CommentsPanel(
                            comments = comments,
                            onSendMessage = { sendComment() },
                            commentInput = commentInput,
                            onCommentInputChange = { commentInput = it }
                        )
                    }
                }

                // Bottom Controls
                BottomControlBar(
                    isAudioEnabled = isAudioEnabled,
                    onToggleAudio = { isAudioEnabled = !isAudioEnabled },
                    onEndStream = {
                        endStream()
                        onClose()
                    },
                    onShareStream = {
                        val shareText = if (streamId != null) {
                            "Mira mi transmision en vivo! Codigo: $streamId"
                        } else null
                        if (shareText != null) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Compartir transmision"))
                        }
                    }
                )
            }
        } else {
            // â•â•â• PRE-LIVE: preview a pantalla completa â•â•â•
            if (cameraPermission.status.isGranted) {
                CameraPreview(true, previewViewRef)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(280.dp)
                    .background(Brush.verticalGradient(listOf(DarkBg.copy(alpha = 0.7f), Color.Transparent)))
                    .align(Alignment.TopCenter)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(380.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, DarkBg.copy(alpha = 0.75f))))
                    .align(Alignment.BottomCenter)
            )
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (liveKitState is LiveKitState.Disconnected || liveKitState is LiveKitState.Error) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (title.isNotEmpty()) {
                            Text(text = title, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp,
                                fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 12.dp))
                        }
                        Button(
                            onClick = { showTitleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.height(56.dp).widthIn(min = 200.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp)
                        ) {
                            Icon(Icons.Filled.Videocam, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("IR EN VIVO", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            ControlBtn(Icons.Outlined.FlipCameraAndroid, "Cámara", onClick = {
                                useFrontCamera = !useFrontCamera
                                if (cameraPermission.status.isGranted) startCameraX()
                            })
                            ControlBtn(
                                if (isAudioEnabled) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                                if (isAudioEnabled) "Mic" else "Mute",
                                onClick = { isAudioEnabled = !isAudioEnabled }
                            )
                        }
                    }
                }
            }
        }

        // â”€â”€ Hearts animation (right side) â”€â”€
        FloatingHeartsOverlay(
            reactions = pendingReactions,
            modifier = Modifier.fillMaxSize()
        )

        // â”€â”€ Error banner â”€â”€
        if (liveKitState is LiveKitState.Error) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(top = 80.dp).align(Alignment.TopCenter)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = PinkAccent.copy(alpha = 0.9f)) {
                    Text((liveKitState as LiveKitState.Error).message, color = Color.White,
                        fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                }
            }
        }
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• COMPONENTS â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

// ========================================================================
// DASHBOARD COMPOSABLES
// ========================================================================

@Composable
private fun StreamInfoBar(
    viewerCount: Int,
    likeCount: Int,
    elapsedSeconds: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LivePill()
        Spacer(modifier = Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.4f)) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Visibility, null, tint = Color.White, modifier = Modifier.size(13.dp))
                Text("$viewerCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Surface(shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.4f)) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Favorite, null, tint = PinkAccent, modifier = Modifier.size(13.dp))
                Text("$likeCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Surface(shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.4f)) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Timer, null, tint = Color.White, modifier = Modifier.size(13.dp))
                Text(formatDuration(elapsedSeconds), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
private fun CameraPreviewPanel(
    liveKitManager: LiveKitManager,
    useFrontCamera: Boolean,
    cameraPermissionGranted: Boolean,
    previewViewRef: MutableState<PreviewView?>,
    isAudioEnabled: Boolean,
    onToggleCamera: () -> Unit,
    onToggleMic: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val localTrack = liveKitManager.getLocalCameraTrack()
            val room = liveKitManager.roomRef
            if (localTrack != null && room != null) {
                key("texture_renderer") {
                    AndroidView(
                        factory = { ctx ->
                            io.livekit.android.renderer.TextureViewRenderer(ctx).apply {
                                setMirror(useFrontCamera)
                                setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                room.initVideoRenderer(this)
                                localTrack.addRenderer(this)
                            }
                        },
                        onRelease = { it.release() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                CameraPreview(cameraPermissionGranted, previewViewRef)
            }

            // Gradient overlay at bottom for controls
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                    .align(Alignment.BottomCenter)
            )

            // Camera controls overlay
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallControlButton(
                    icon = Icons.Outlined.FlipCameraAndroid,
                    label = "Girar",
                    onClick = onToggleCamera
                )
                Spacer(modifier = Modifier.width(24.dp))
                SmallControlButton(
                    icon = if (isAudioEnabled) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                    label = if (isAudioEnabled) "Mic" else "Mute",
                    tint = if (isAudioEnabled) Color.White else Color(0xFFFF2E63),
                    onClick = onToggleMic
                )
            }
        }
    }
}

@Composable
private fun SmallControlButton(icon: ImageVector, label: String, tint: Color = Color.White, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = tint, modifier = Modifier.size(18.dp))
        }
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
    }
}

@Composable
private fun CommentsPanel(
    comments: List<LiveComment>,
    onSendMessage: () -> Unit,
    commentInput: String,
    onCommentInputChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Chat, null, tint = PinkAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Comentarios", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text("${comments.size}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (comments.isEmpty()) {
                Text("Sin comentarios aun", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 16.dp))
            } else {
                val visible = comments.takeLast(10)
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(visible) { idx, comment ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(PinkAccent),
                                    contentAlignment = Alignment.Center) {
                                    Text(comment.username.take(1).uppercase(), fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(comment.username, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.7f))
                                    Text(comment.text, fontSize = 13.sp, color = Color.White, maxLines = 2,
                                        overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick send message
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = commentInput,
                    onValueChange = onCommentInputChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    cursorBrush = SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
                    modifier = Modifier.weight(1f).height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(GlassBg),
                    decorationBox = { inner ->
                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart) {
                            if (commentInput.isEmpty()) Text("Responder...", fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.4f))
                            inner()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(shape = CircleShape, color = PinkAccent, modifier = Modifier.size(34.dp).clickable { onSendMessage() }) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Send, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomControlBar(
    isAudioEnabled: Boolean,
    onToggleAudio: () -> Unit,
    onEndStream: () -> Unit,
    onShareStream: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // End stream button - prominent red
        Button(
            onClick = onEndStream,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2E63)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Filled.Stop, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Finalizar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }

        // Mic toggle
        Surface(
            shape = CircleShape,
            color = if (isAudioEnabled) Color.Black.copy(alpha = 0.3f) else Color(0x44FF2E63),
            modifier = Modifier.size(44.dp).clickable { onToggleAudio() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isAudioEnabled) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                    null,
                    tint = if (isAudioEnabled) Color.White else Color(0xFFFF2E63),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Share button
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier.size(44.dp).clickable { onShareStream() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Share, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

private data class FloatingReaction(val id: String, val emoji: String, val createdAt: Long)

@Composable
private fun CameraPreview(granted: Boolean, viewRef: MutableState<PreviewView?>) {
    if (!granted) return
    key("camerax_preview") {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.BLACK)
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { view -> viewRef.value = view },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun LivePill() {
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )
    Surface(shape = RoundedCornerShape(4.dp), color = PinkAccent) {
        Row(modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(modifier = Modifier.size(5.dp).graphicsLayer { alpha = pulseAlpha }.clip(CircleShape).background(Color.White))
            Text("EN VIVO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun ChatBubble(username: String, text: String, alpha: Float = 1f) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .widthIn(max = 320.dp)
            .graphicsLayer { this.alpha = alpha.coerceIn(0f, 1f) }
    ) {
        // Avatar estilo Instagram Live
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(PinkAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                username.take(1).uppercase(),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                username,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text,
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FloatingHeartsOverlay(reactions: List<FloatingReaction>, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        reactions.forEachIndexed { index, reaction ->
            key(reaction.id) {
                FloatingHeart(
                    emoji = reaction.emoji,
                    createdAt = reaction.createdAt,
                    index = index
                )
            }
        }
    }
}

@Composable
private fun FloatingHeart(emoji: String, createdAt: Long, index: Int) {
    val density = LocalDensity.current
    val swayAmplitude = Random.nextFloat() * 20f + 10f
    val floatDuration = Random.nextInt(2200, 3400)
    val sway = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            sway.animateTo(
                360f,
                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
            )
        }
        launch {
            heartAlpha.animateTo(
                0f,
                animationSpec = tween(durationMillis = floatDuration, easing = FastOutSlowInEasing)
            )
        }
    }

    val yOffset = with(density) { (320 * (1f - heartAlpha.value)).dp }

    // Nace sobre el botón de corazón (abajo derecha del composer) y flota hacia arriba con fade
    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(end = 18.dp, bottom = 56.dp),
        contentAlignment = Alignment.BottomEnd) {
        Text(
            text = emoji,
            fontSize = (22 + Random.nextFloat() * 14).sp,
            modifier = Modifier
                .offset(
                    x = with(density) { (swayAmplitude * sin(sway.value * (kotlin.math.PI.toFloat() / 180f))).dp },
                    y = -yOffset
                )
                .graphicsLayer { alpha = heartAlpha.value.coerceIn(0f, 1f) }
        )
    }
}

@Composable
private fun ControlBtn(icon: ImageVector, label: String, tint: Color = Color.White, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider {
    return suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener({ continuation.resume(future.get()) }, ContextCompat.getMainExecutor(this))
        }
    }
}