package com.vinzay.app.ui.screens.publish

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.vinzay.app.data.repository.RendRepository
import com.vinzay.app.ui.components.toProductCardData
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class RendStep {
    object Camera : RendStep()
    object Gallery : RendStep()
    object Edit : RendStep()
    object Details : RendStep()
}

data class GalleryVideo(
    val id: Long,
    val uri: Uri,
    val duration: Long,
    val dateAdded: Long
)

data class RendMeta(
    val visibility: String = "public",
    val allowOpinions: Boolean = true,
    val allowConsults: Boolean = true,
    val allowDownloads: Boolean = false,
    val allowShares: Boolean = true,
    val hashtags: List<String> = emptyList(),
    val category: String? = null,
    val location: String? = null
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ClipScreen(
    onClose: () -> Unit,
    onModeSelected: (Int) -> Unit,
    currentModeIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    var currentStep by remember { mutableStateOf<RendStep>(RendStep.Camera) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var galleryVideos by remember { mutableStateOf<List<GalleryVideo>>(emptyList()) }
    var isRecording by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    
    var rendTitle by remember { mutableStateOf("") }
    var rendDescription by remember { mutableStateOf("") }
    var productTitle by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    
    val uploadState by RendRepository.uploadState.collectAsState()
    
    // Solo permiso de galería en RendScreen (cámara ya se pidió en HistoryScreen)
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val storagePermissionState = rememberPermissionState(storagePermission)
    
    // Permiso de cámara (solo para verificar, no solicitar)
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Solicitar solo permiso de galería al entrar a Rend
    LaunchedEffect(Unit) {
        if (!storagePermissionState.status.isGranted) {
            storagePermissionState.launchPermissionRequest()
        } else {
            // Si ya tiene permiso, cargar videos inmediatamente
            galleryVideos = loadGalleryVideos(context)
        }
    }
    
    // Cargar videos cuando el permiso cambie a concedido
    LaunchedEffect(storagePermissionState.status.isGranted) {
        if (storagePermissionState.status.isGranted && galleryVideos.isEmpty()) {
            galleryVideos = loadGalleryVideos(context)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
            .statusBarsPadding()
    ) {
        when (currentStep) {
            is RendStep.Camera -> {
                RendCameraView(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    lensFacing = lensFacing,
                    isRecording = isRecording,
                    hasPermission = cameraPermission.status.isGranted,
                    galleryVideos = galleryVideos,
                    onClose = onClose,
                    onFlipCamera = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) 
                            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                    },
                    onRecordingChange = { isRecording = it },
                    onVideoSelected = { uri ->
                        selectedVideoUri = uri
                        currentStep = RendStep.Details
                    },
                    onShowGallery = { currentStep = RendStep.Gallery },
                    currentModeIndex = currentModeIndex,
                    onModeSelected = onModeSelected
                )
            }
            
            is RendStep.Gallery -> {
                RendGalleryView(
                    videos = galleryVideos,
                    selectedUri = selectedVideoUri,
                    onVideoSelect = { selectedVideoUri = it },
                    onBack = { currentStep = RendStep.Camera },
                    onNext = {
                        if (selectedVideoUri != null) currentStep = RendStep.Details
                    }
                )
            }
            
            is RendStep.Edit -> {
                // Editor deshabilitado temporalmente - se salta directo a Details
                currentStep = RendStep.Details
            }
            
            is RendStep.Details -> {
                RendDetailsView(
                    videoUri = selectedVideoUri,
                    title = rendTitle,
                    onTitleChange = { rendTitle = it },
                    description = rendDescription,
                    onDescriptionChange = { rendDescription = it },
                    productTitle = productTitle,
                    onProductTitleChange = { productTitle = it },
                    productPrice = productPrice,
                    onProductPriceChange = { productPrice = it },
                    isPublishing = uploadState.isUploading,
                    onBack = { currentStep = RendStep.Camera },
                    onPublish = { linkedPost, rendMeta ->
                        selectedVideoUri?.let { uri ->
                            scope.launch {
                                // Obtener imagen del producto enlazado (Cloudinary) si existe
                                val productImageUrl = linkedPost?.images?.firstOrNull()
                                
                                // Usar datos del linkedPost si existen, sino usar los campos manuales
                                val finalProductTitle = linkedPost?.title?.takeIf { it.isNotBlank() } 
                                    ?: productTitle.ifEmpty { null }
                                val finalProductPrice = linkedPost?.price 
                                    ?: productPrice.toDoubleOrNull()
                                
                                val result = RendRepository.createRend(
                                    context = context,
                                    videoUri = uri,
                                    title = rendTitle.ifEmpty { "Mi Rend" },
                                    description = rendDescription.ifEmpty { null },
                                    productTitle = finalProductTitle,
                                    productPrice = finalProductPrice,
                                    productImage = productImageUrl,
                                    productId = linkedPost?.productId,
                                    duration = 15,
                                    visibility = rendMeta.visibility,
                                    allowOpinions = rendMeta.allowOpinions,
                                    allowConsults = rendMeta.allowConsults,
                                    allowDownloads = rendMeta.allowDownloads,
                                    allowShares = rendMeta.allowShares,
                                    hashtags = rendMeta.hashtags,
                                    category = rendMeta.category,
                                    location = rendMeta.location
                                )
                                if (result.isSuccess) {
                                    android.widget.Toast.makeText(context, "¡Rend publicado!", android.widget.Toast.LENGTH_SHORT).show()
                                    RendRepository.loadRends() // Recargar rends
                                    onClose()
                                } else {
                                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                                    android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
private fun RendCameraView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    lensFacing: Int,
    isRecording: Boolean,
    hasPermission: Boolean,
    galleryVideos: List<GalleryVideo>,
    onClose: () -> Unit,
    onFlipCamera: () -> Unit,
    onRecordingChange: (Boolean) -> Unit,
    onVideoSelected: (Uri) -> Unit,
    onShowGallery: () -> Unit,
    currentModeIndex: Int,
    onModeSelected: (Int) -> Unit
) {
    val previewView = remember { PreviewView(context) }
    val scope = rememberCoroutineScope()
    
    // Dimensiones del preview - iguales que HISTORIA
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val previewHeight = screenHeight * 0.86f
    val previewCornerRadius = 24.dp
    val previewTopPadding = 8.dp
    
    // Permiso de audio para grabación de video
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Estados de grabación profesional
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingDuration by remember { mutableStateOf(0L) }
    val maxDurationMs = 60_000L // 60 segundos máximo
    
    // Estado del flash/linterna
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<Camera?>(null) }
    
    // Estado del temporizador (countdown antes de grabar)
    var showTimerModal by remember { mutableStateOf(false) }
    var selectedTimerSeconds by remember { mutableStateOf(0) } // 0 = sin temporizador
    var countdownActive by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableStateOf(0) }
    
    // Timer para duración de grabación
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0L
            while (isRecording && recordingDuration < maxDurationMs) {
                kotlinx.coroutines.delay(100)
                recordingDuration += 100
            }
            // Auto-detener al llegar al máximo
            if (recordingDuration >= maxDurationMs) {
                activeRecording?.stop()
                onRecordingChange(false)
            }
        } else {
            recordingDuration = 0L
        }
    }
    
    // Configurar cámara con VideoCapture
    LaunchedEffect(lensFacing, hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        try {
            val cameraProvider = context.getCameraProviderRend()
            cameraProvider.unbindAll()
            
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
            
            // Configurar grabador de video de alta calidad
            val qualitySelector = QualitySelector.fromOrderedList(
                listOf(Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )
            
            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build()
            
            videoCapture = VideoCapture.withOutput(recorder)
            
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
            cameraControl = camera
        } catch (e: Exception) {
            Log.e("RendScreen", "Camera binding failed", e)
        }
    }
    
    // Función para iniciar grabación
    fun startRecording() {
        val vc = videoCapture ?: return
        
        // Verificar permiso de audio
        if (!audioPermission.status.isGranted) {
            audioPermission.launchPermissionRequest()
            return
        }
        
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "REND_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Vinzay")
            }
        }
        
        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        
        activeRecording = vc.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d("RendScreen", "Grabación iniciada")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val uri = recordEvent.outputResults.outputUri
                            Log.d("RendScreen", "Video guardado: $uri")
                            onVideoSelected(uri)
                        } else {
                            Log.e("RendScreen", "Error de grabación: ${recordEvent.error}")
                            activeRecording?.close()
                            activeRecording = null
                        }
                        onRecordingChange(false)
                    }
                }
            }
        
        onRecordingChange(true)
    }
    
    // Función para detener grabación
    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }
    
    // Función para manejar el botón de grabar con temporizador
    fun handleRecordButton() {
        if (isRecording) {
            stopRecording()
        } else if (selectedTimerSeconds > 0) {
            countdownValue = selectedTimerSeconds
            countdownActive = true
        } else {
            startRecording()
        }
    }
    
    // Variable para detectar cuando el countdown termina
    var shouldStartAfterCountdown by remember { mutableStateOf(false) }
    
    // Countdown del temporizador
    LaunchedEffect(countdownActive) {
        if (countdownActive && countdownValue > 0) {
            while (countdownValue > 0) {
                kotlinx.coroutines.delay(1000)
                countdownValue--
            }
            countdownActive = false
            shouldStartAfterCountdown = true
        }
    }
    
    // Iniciar grabación cuando countdown termina
    LaunchedEffect(shouldStartAfterCountdown) {
        if (shouldStartAfterCountdown) {
            shouldStartAfterCountdown = false
            startRecording()
        }
    }
    
    // Animación del botón de grabar
    val recordButtonScale by animateFloatAsState(
        targetValue = if (isRecording) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "recordScale"
    )
    
    val innerButtonSize by animateDpAsState(
        targetValue = if (isRecording) 28.dp else 64.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "innerSize"
    )
    
    val innerButtonShape by animateFloatAsState(
        targetValue = if (isRecording) 8f else 32f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "innerShape"
    )
    
    // Pulso animado cuando graba
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Preview de cámara con border radius - igual que HISTORIA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .background(Color.Black)
                .padding(top = previewTopPadding, start = 4.dp, end = 4.dp)
                .clip(RoundedCornerShape(previewCornerRadius))
                .align(Alignment.TopCenter)
        ) {
            if (hasPermission) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0A0F)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Videocam, null, tint = AccentGold, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Permiso de cámara requerido", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Top bar - Volver izquierda, Timer centro, Temporizador derecha (todo alineado)
        // Mismo padding que HISTORIA: horizontal = 16dp, vertical = 16dp + previewTopPadding (8dp) = 24dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp + previewTopPadding)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón volver - mismo tamaño y padding que linterna/temporizador (40dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        if (isRecording) stopRecording()
                        if (countdownActive) { countdownActive = false; countdownValue = 0 }
                        onClose()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            
            // Timer de grabación O Countdown - Centro del header
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isRecording || countdownActive,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (countdownActive) {
                            // Countdown antes de grabar
                            Text(
                                text = countdownValue.toString(),
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        } else {
                            // Timer de grabación
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .graphicsLayer { alpha = pulseAlpha }
                                    .background(Color.Red, CircleShape)
                            )
                            Text(
                                text = formatRecordingTime(recordingDuration),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            // Flash toggle - Derecha
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de flash/linterna
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFlashOn) AccentGold.copy(alpha = 0.3f)
                            else Color.Black.copy(alpha = 0.5f)
                        )
                        .clickable(enabled = lensFacing == CameraSelector.LENS_FACING_BACK) {
                            cameraControl?.let { cam ->
                                if (cam.cameraInfo.hasFlashUnit()) {
                                    isFlashOn = !isFlashOn
                                    cam.cameraControl.enableTorch(isFlashOn)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isFlashOn) Icons.Filled.FlashOn else Icons.Outlined.FlashOff,
                        "Flash",
                        tint = if (isFlashOn) AccentGold else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Temporizador
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedTimerSeconds > 0) AccentGold.copy(alpha = 0.3f)
                            else Color.Black.copy(alpha = 0.5f)
                        )
                        .clickable(enabled = !isRecording && !countdownActive) { showTimerModal = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedTimerSeconds > 0) {
                        Text(
                            text = "${selectedTimerSeconds}s",
                            color = AccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Timer,
                            "Temporizador",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
        
        // Modal de selección de temporizador
        if (showTimerModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showTimerModal = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Temporizador",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tiempo antes de empezar a grabar",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf(0 to "OFF", 3 to "3s", 5 to "5s", 10 to "10s").forEach { (seconds, label) ->
                            val isSelected = selectedTimerSeconds == seconds
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) AccentGold else Color.White.copy(alpha = 0.1f)
                                    )
                                    .then(
                                        if (isSelected) Modifier
                                        else Modifier.border(1.dp, TextMuted.copy(alpha = 0.3f), CircleShape)
                                    )
                                    .clickable {
                                        selectedTimerSeconds = seconds
                                        showTimerModal = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Botón de grabar - POSICIÓN FIJA (no se mueve cuando desaparece la fila inferior)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp) // Posición fija sobre la fila de controles
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp) // Mismo tamaño que HISTORIA
                    .graphicsLayer {
                        scaleX = recordButtonScale
                        scaleY = recordButtonScale
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .then(
                        if (isRecording) Modifier.border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                0f to AccentPink,
                                0.5f to AccentGold,
                                1f to AccentPink
                            ),
                            shape = CircleShape
                        ) else Modifier
                    )
                    .clickable(enabled = !countdownActive) {
                        handleRecordButton()
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(innerButtonSize)
                        .clip(RoundedCornerShape(innerButtonShape.dp))
                        .then(
                            if (isRecording) 
                                Modifier.background(Color(0xFFEF4444))
                            else 
                                Modifier.background(
                                    Brush.linearGradient(listOf(AccentPink, AccentGold))
                                )
                        )
                )
            }
        }
        
        // Row con [Galería] [Carrusel de modos] [Girar] - POSICIÓN FIJA en la parte inferior
        AnimatedVisibility(
            visible = !isRecording,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón de galería (izquierda) - SIN BORDE, igual que HISTORIA
                IconButton(
                    onClick = { onShowGallery() },
                    enabled = !countdownActive,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .graphicsLayer { alpha = if (countdownActive) 0.4f else 1f }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "Galería",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Carrusel de modos (centro) - minimalista estilo Instagram
                Box(modifier = Modifier.weight(1f)) {
                    com.vinzay.app.ui.components.ModeCarousel(
                        currentIndex = currentModeIndex,
                        onModeSelected = onModeSelected,
                        style = com.vinzay.app.ui.components.CarouselStyle.CENTERED_SINGLE
                    )
                }
                
                // Botón de girar cámara (derecha)
                IconButton(
                    onClick = { if (!isRecording) onFlipCamera() },
                    enabled = !isRecording,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .graphicsLayer { alpha = if (isRecording) 0.4f else 1f }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cameraswitch,
                        contentDescription = "Voltear cámara",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun formatRecordingTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun RendGalleryView(
    videos: List<GalleryVideo>,
    selectedUri: Uri?,
    onVideoSelect: (Uri) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
            }
            Text("Seleccionar video", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onNext, enabled = selectedUri != null) {
                Text("Siguiente", color = if (selectedUri != null) AccentGold else TextMuted, fontWeight = FontWeight.Bold)
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(videos) { video ->
                VideoThumbnailItem(
                    video = video,
                    isSelected = video.uri == selectedUri,
                    onClick = { onVideoSelect(video.uri) }
                )
            }
        }
    }
}

@Composable
private fun VideoThumbnailItem(video: GalleryVideo, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(video.uri) {
        thumbnail = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(video.uri, android.util.Size(300, 400), null)
            } else null
        } catch (e: Exception) { null }
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A2E))
            .border(if (isSelected) 3.dp else 0.dp, if (isSelected) AccentGold else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        if (thumbnail != null) {
            AsyncImage(model = thumbnail, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PlayCircle, null, tint = AccentGold.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
            }
        }
        
        Box(
            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                .clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(formatVideoDuration(video.duration), color = Color.White, fontSize = 10.sp)
        }
        
        if (isSelected) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                    .clip(CircleShape).background(AccentGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// -------------------------------------------------------------------------------
// REND CATEGORIES
// -------------------------------------------------------------------------------
private data class RendCategory(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

private val REND_CATEGORIES = listOf(
    RendCategory("entertainment", "Entretenimiento", Icons.Outlined.TheaterComedy, Color(0xFFFF6B35)),
    RendCategory("fashion", "Moda", Icons.Outlined.Checkroom, Color(0xFFE91E63)),
    RendCategory("tech", "Tecnología", Icons.Outlined.Devices, Color(0xFF2196F3)),
    RendCategory("food", "Comida", Icons.Outlined.Restaurant, Color(0xFFFF9800)),
    RendCategory("beauty", "Belleza", Icons.Outlined.Face, Color(0xFFE040FB)),
    RendCategory("sports", "Deportes", Icons.Outlined.FitnessCenter, Color(0xFF4CAF50)),
    RendCategory("education", "Educación", Icons.Outlined.School, Color(0xFF00BCD4)),
    RendCategory("music", "Música", Icons.Outlined.MusicNote, Color(0xFF9C27B0)),
    RendCategory("travel", "Viajes", Icons.Outlined.Flight, Color(0xFF009688)),
    RendCategory("gaming", "Gaming", Icons.Outlined.SportsEsports, Color(0xFF673AB7)),
    RendCategory("art", "Arte", Icons.Outlined.Palette, Color(0xFFFF5722)),
    RendCategory("other", "Otro", Icons.Outlined.MoreHoriz, Color(0xFF607D8B))
)

// -------------------------------------------------------------------------------
// REND DETAILS VIEW - Pantalla profesional de configuración de Rend
// -------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RendDetailsView(
    videoUri: Uri?,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    productTitle: String,
    onProductTitleChange: (String) -> Unit,
    productPrice: String,
    onProductPriceChange: (String) -> Unit,
    isPublishing: Boolean,
    onBack: () -> Unit,
    onPublish: (linkedPost: com.vinzay.app.data.model.Post?, meta: RendMeta) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Estado para enlazar post
    var showPostLinkModal by remember { mutableStateOf(false) }
    var linkedPost by remember { mutableStateOf<com.vinzay.app.data.model.Post?>(null) }
    val userPosts by com.vinzay.app.data.repository.PostRepository.userPosts.collectAsState()
    
    // Nuevos estados profesionales
    var hashtags by remember { mutableStateOf(listOf<String>()) }
    var hashtagInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<RendCategory?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf("public") } // public, followers, private
    var allowOpinions by remember { mutableStateOf(true) }
    var allowShares by remember { mutableStateOf(true) }
    var allowDownloads by remember { mutableStateOf(false) }
    var allowConsults by remember { mutableStateOf(true) }
    
    // Trending hashtags y categorías populares
    var trendingHashtags by remember { mutableStateOf<List<com.vinzay.app.data.repository.TrendingHashtag>>(emptyList()) }
    var popularCategories by remember { mutableStateOf<List<com.vinzay.app.data.repository.PopularCategory>>(emptyList()) }
    var locationTag by remember { mutableStateOf("") }
    var showLocationInput by remember { mutableStateOf(false) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    
    // Upload progress animation
    val uploadProgress by RendRepository.uploadState.collectAsState()
    val progressAnim by animateFloatAsState(
        targetValue = if (isPublishing) uploadProgress.progress else 0f,
        animationSpec = tween(300),
        label = "uploadProgress"
    )
    
    // Cargar posts del usuario y tendencias
    LaunchedEffect(Unit) {
        com.vinzay.app.data.repository.PostRepository.loadUserPosts()
        trendingHashtags = RendRepository.getTrendingHashtags()
        popularCategories = RendRepository.getPopularCategories()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ---------------------------------------------------------------
            // HEADER PROFESIONAL
            // ---------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF12121A), Color(0xFF0A0A0F))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            enabled = !isPublishing,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Nuevo Clip",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Configura tu video",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Button(
                        onClick = { 
                            onPublish(linkedPost, RendMeta(
                                visibility = visibility,
                                allowOpinions = allowOpinions,
                                allowConsults = allowConsults,
                                allowDownloads = allowDownloads,
                                allowShares = allowShares,
                                hashtags = hashtags,
                                category = selectedCategory?.id,
                                location = locationTag.ifBlank { null }
                            ))
                        },
                        enabled = !isPublishing && title.isNotBlank(),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            disabledContainerColor = AccentGold.copy(alpha = 0.3f)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Publicando...",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Publicar",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                // Barra de progreso de upload
                if (isPublishing) {
                    LinearProgressIndicator(
                        progress = progressAnim,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = AccentGold,
                        trackColor = Color.Transparent
                    )
                }
            }
            
            // ---------------------------------------------------------------
            // CONTENIDO SCROLLEABLE
            // ---------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 40.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // --- VIDEO PREVIEW + TÍTULO INLINE ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Thumbnail del video
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface)
                    ) {
                        if (videoUri != null) {
                            // Generar thumbnail desde el video usando MediaMetadataRetriever
                            val thumbnailBitmap = remember(videoUri) {
                                try {
                                    val retriever = android.media.MediaMetadataRetriever()
                                    retriever.setDataSource(context, videoUri)
                                    retriever.getFrameAtTime(0L, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            
                            if (thumbnailBitmap != null) {
                                Image(
                                    bitmap = thumbnailBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        // Overlay gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )
                        // Badge CLIP
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AccentGold, Color(0xFFFF8C00))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("CLIP", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        // Play icon center
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    // Título y Descripción al lado
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { if (it.length <= 80) onTitleChange(it) },
                            placeholder = { Text("Título de tu Clip...", color = TextMuted, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = rendFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        )
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 300) onDescriptionChange(it) },
                            placeholder = { Text("Describe tu video...", color = TextMuted, fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            colors = rendFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 3,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                        
                        // Contador de caracteres
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${title.length}/80",
                                color = if (title.length > 70) Color(0xFFFF6B6B) else TextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                "${description.length}/300",
                                color = if (description.length > 270) Color(0xFFFF6B6B) else TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // --- HASHTAGS ---
                RendDetailCard(
                    icon = Icons.Outlined.Tag,
                    title = "Hashtags",
                    subtitle = "Agrega hashtags para que más personas encuentren tu Clip",
                    accentColor = Color(0xFF1DA1F2)
                ) {
                    // Input de hashtags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = hashtagInput,
                            onValueChange = { input ->
                                // Si termina en espacio o coma, agregar hashtag
                                if (input.endsWith(" ") || input.endsWith(",")) {
                                    val tag = input.trimEnd(' ', ',', '#').trim()
                                    if (tag.isNotBlank() && hashtags.size < 15 && !hashtags.contains(tag)) {
                                        hashtags = hashtags + tag
                                    }
                                    hashtagInput = ""
                                } else {
                                    hashtagInput = input
                                }
                            },
                            placeholder = { Text("Escribe y presiona espacio...", color = TextMuted, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            colors = rendFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                            leadingIcon = {
                                Text("#", color = Color(0xFF1DA1F2), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 4.dp))
                            }
                        )
                    }
                    
                    if (hashtags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            hashtags.forEach { tag ->
                                RendHashtagChip(
                                    tag = tag,
                                    onRemove = { hashtags = hashtags.filter { it != tag } }
                                )
                            }
                        }
                    }
                    
                    if (hashtags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${hashtags.size}/15 hashtags",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    
                    // Trending hashtags suggestions
                    if (trendingHashtags.isNotEmpty() && hashtags.size < 15) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.TrendingUp, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tendencias", color = Color(0xFF1DA1F2), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            trendingHashtags.filter { it.tag !in hashtags }.take(8).forEach { trend ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            if (hashtags.size < 15 && trend.tag !in hashtags) {
                                                hashtags = hashtags + trend.tag
                                            }
                                        },
                                    color = Color.White.copy(alpha = 0.06f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("#${trend.tag}", color = TextSecondary, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${trend.usageCount}", color = TextMuted, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Outlined.Add, null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // --- CATEGORÍA ---
                RendDetailCard(
                    icon = Icons.Outlined.Category,
                    title = "Categoría",
                    subtitle = "Clasifica tu contenido para llegar al público correcto",
                    accentColor = Color(0xFFFF6B35)
                ) {
                    // Categoría seleccionada o botón para elegir
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCategoryPicker = !showCategoryPicker },
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedCategory != null) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(selectedCategory!!.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        selectedCategory!!.icon,
                                        null,
                                        tint = selectedCategory!!.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    selectedCategory!!.label,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Add,
                                    null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Seleccionar categoría",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                if (showCategoryPicker) Icons.Default.KeyboardArrowUp 
                                else Icons.Default.KeyboardArrowDown,
                                null,
                                tint = TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    // Grid de categorías expandible
                    AnimatedVisibility(
                        visible = showCategoryPicker,
                        enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                REND_CATEGORIES.forEach { cat ->
                                    val isSelected = selectedCategory?.id == cat.id
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedCategory = if (isSelected) null else cat
                                                showCategoryPicker = false
                                            },
                                        color = if (isSelected) cat.color.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isSelected) BorderStroke(
                                            1.5.dp, cat.color.copy(alpha = 0.5f)
                                        ) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                cat.icon,
                                                null,
                                                tint = if (isSelected) cat.color else TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                cat.label,
                                                color = if (isSelected) cat.color else TextSecondary,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // --- PRIVACIDAD Y VISIBILIDAD ---
                RendDetailCard(
                    icon = Icons.Outlined.Shield,
                    title = "Privacidad",
                    subtitle = "Controla quién puede ver tu Clip",
                    accentColor = Color(0xFF4CAF50)
                ) {
                    // Opciones de visibilidad
                    val visibilityOptions = listOf(
                        Triple("public", "Público", Icons.Outlined.Public),
                        Triple("followers", "Seguidores", Icons.Outlined.People),
                        Triple("private", "Privado", Icons.Outlined.Lock)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        visibilityOptions.forEach { (id, label, icon) ->
                            val isSelected = visibility == id
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { visibility = id },
                                color = if (isSelected) AccentGold.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) BorderStroke(
                                    1.5.dp, AccentGold.copy(alpha = 0.5f)
                                ) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        icon,
                                        null,
                                        tint = if (isSelected) AccentGold else TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        label,
                                        color = if (isSelected) AccentGold else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // --- INTERACCIONES ---
                RendDetailCard(
                    icon = Icons.Outlined.TouchApp,
                    title = "Interacciones",
                    subtitle = "Gestiona cómo otros interactúan con tu Clip",
                    accentColor = Color(0xFFE040FB)
                ) {
                    RendToggleRow(
                        icon = Icons.Outlined.RateReview,
                        title = "Permitir opiniones",
                        subtitle = "Los usuarios podrán dejar opiniones",
                        isChecked = allowOpinions,
                        onCheckedChange = { allowOpinions = it }
                    )
                    
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    RendToggleRow(
                        icon = Icons.Outlined.Send,
                        title = "Permitir reenvíos",
                        subtitle = "Los usuarios pueden reenviar tu Clip",
                        isChecked = allowShares,
                        onCheckedChange = { allowShares = it }
                    )
                    
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    RendToggleRow(
                        icon = Icons.Outlined.ContactMail,
                        title = "Permitir consultas",
                        subtitle = "Los usuarios pueden enviarte mensajes",
                        isChecked = allowConsults,
                        onCheckedChange = { allowConsults = it }
                    )
                    
                    Divider(
                        color = Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    RendToggleRow(
                        icon = Icons.Outlined.Download,
                        title = "Permitir descargas",
                        subtitle = "Los usuarios podrán guardar el video",
                        isChecked = allowDownloads,
                        onCheckedChange = { allowDownloads = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // --- UBICACIÓN ---
                RendDetailCard(
                    icon = Icons.Outlined.LocationOn,
                    title = "Ubicación",
                    subtitle = "Agrega una ubicación a tu Clip",
                    accentColor = Color(0xFFFF5252)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLocationInput = !showLocationInput },
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (locationTag.isNotBlank()) Icons.Filled.LocationOn 
                                else Icons.Outlined.AddLocation,
                                null,
                                tint = if (locationTag.isNotBlank()) Color(0xFFFF5252) else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                if (locationTag.isNotBlank()) locationTag else "Agregar ubicación",
                                color = if (locationTag.isNotBlank()) Color.White else TextMuted,
                                fontSize = 14.sp
                            )
                            if (locationTag.isNotBlank()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Close,
                                    "Quitar",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { locationTag = ""; showLocationInput = false }
                                )
                            }
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = showLocationInput && locationTag.isBlank(),
                        enter = expandVertically(tween(200)) + fadeIn(),
                        exit = shrinkVertically(tween(200)) + fadeOut()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            var locationInput by remember { mutableStateOf("") }
                            
                            OutlinedTextField(
                                value = locationInput,
                                onValueChange = { locationInput = it },
                                placeholder = { Text("Ej: Buenos Aires, Argentina", color = TextMuted, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = rendFieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                                trailingIcon = {
                                    if (locationInput.isNotBlank()) {
                                        IconButton(onClick = { 
                                            locationTag = locationInput
                                            showLocationInput = false 
                                        }) {
                                            Icon(Icons.Default.Check, null, tint = AccentGold, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Botón de ubicación actual
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch {
                                            try {
                                                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                                                if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                        location?.let {
                                                            val geocoder = android.location.Geocoder(context)
                                                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                                            if (!addresses.isNullOrEmpty()) {
                                                                val address = addresses[0]
                                                                val locality = address.locality ?: ""
                                                                val country = address.countryName ?: ""
                                                                locationTag = "$locality, $country".trim().trim(',')
                                                                showLocationInput = false
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    android.widget.Toast.makeText(context, "Permiso de ubicación requerido", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Error obteniendo ubicación", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                color = Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.MyLocation,
                                        null,
                                        tint = Color(0xFF1DA1F2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Usar ubicación actual",
                                        color = Color(0xFF1DA1F2),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // --- PRODUCTO (OPCIONAL) ---
                RendDetailCard(
                    icon = Icons.Outlined.ShoppingBag,
                    title = "Producto",
                    subtitle = "Vincula un producto o ingresa los datos manualmente",
                    accentColor = AccentGold
                ) {
                    // Enlazar publicación existente
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showPostLinkModal = true },
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (linkedPost != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = linkedPost?.images?.firstOrNull(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Surface)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = linkedPost?.title ?: "",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    linkedPost?.price?.let { price ->
                                        Text(
                                            text = "$$price",
                                            color = AccentGold,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { linkedPost = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Quitar", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Link, null, tint = AccentGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Enlazar artículo publicado",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "Selecciona uno de tus artículos",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                    
                    // Divider con "o"
                    if (linkedPost == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.08f)
                            )
                            Text(
                                "  o  ",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.08f)
                            )
                        }
                        
                        // Campos manuales
                        OutlinedTextField(
                            value = productTitle,
                            onValueChange = onProductTitleChange,
                            placeholder = { Text("Nombre del producto", color = TextMuted, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Inventory2, null, tint = AccentGold, modifier = Modifier.size(20.dp))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = rendFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.White)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = productPrice,
                            onValueChange = onProductPriceChange,
                            placeholder = { Text("Precio (ej: 99.99)", color = TextMuted, fontSize = 13.sp) },
                            leadingIcon = {
                                Text(
                                    "$",
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = rendFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.White)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // --- RESUMEN VISUAL ---
                if (title.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        AccentGold.copy(alpha = 0.08f),
                                        Color(0xFFFF8C00).copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                AccentGold.copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Receipt,
                                    null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Resumen",
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            
                            RendSummaryItem("Título", title)
                            if (hashtags.isNotEmpty()) {
                                RendSummaryItem("Hashtags", hashtags.joinToString(" ") { "#$it" })
                            }
                            if (selectedCategory != null) {
                                RendSummaryItem("Categoría", selectedCategory!!.label)
                            }
                            RendSummaryItem("Visibilidad", when(visibility) {
                                "public" -> "Público"
                                "followers" -> "Solo seguidores"
                                else -> "Privado"
                            })
                            if (locationTag.isNotBlank()) {
                                RendSummaryItem("Ubicación", locationTag)
                            }
                            if (linkedPost != null) {
                                RendSummaryItem("Producto", linkedPost?.title ?: "")
                            } else if (productTitle.isNotBlank()) {
                                RendSummaryItem("Producto", productTitle)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    // Modal de selección de post
    PostLinkModal(
        isVisible = showPostLinkModal,
        posts = userPosts,
        onDismiss = { showPostLinkModal = false },
        onPostSelected = { post ->
            linkedPost = post
            showPostLinkModal = false
        }
    )
}

// -------------------------------------------------------------------------------
// COMPONENTES AUXILIARES PROFESIONALES
// -------------------------------------------------------------------------------

@Composable
private fun RendDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp, Color.White.copy(alpha = 0.06f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(19.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        subtitle,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun RendHashtagChip(tag: String, onRemove: () -> Unit) {
    Surface(
        color = Color(0xFF1DA1F2).copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp, Color(0xFF1DA1F2).copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$tag",
                color = Color(0xFF1DA1F2),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    "Quitar",
                    tint = Color(0xFF1DA1F2).copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun RendToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentGold,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun RendSummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(
            value,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun RendSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AccentGold, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PostLinkModal(
    isVisible: Boolean,
    posts: List<com.vinzay.app.data.model.Post>,
    onDismiss: () -> Unit,
    onPostSelected: (com.vinzay.app.data.model.Post) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    
    // Scroll offset para animación del header
    val scrollOffset = remember {
        androidx.compose.runtime.derivedStateOf {
            listState.firstVisibleItemScrollOffset
        }
    }
    
    // Header debe desaparecer al hacer scroll
    val headerAlpha = remember {
        androidx.compose.runtime.derivedStateOf {
            (1f - (scrollOffset.value / 100f).coerceIn(0f, 1f))
        }
    }
    
    val filteredPosts = remember(posts, searchQuery) {
        if (searchQuery.isBlank()) posts
        else posts.filter { 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description?.contains(searchQuery, ignoreCase = true) == true
        }
    }
    
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { }
                        ),
                    shape = RectangleShape,
                    color = HomeBg
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header animado
                            androidx.compose.animation.AnimatedVisibility(
                                visible = headerAlpha.value > 0.1f,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .graphicsLayer { alpha = headerAlpha.value },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Enlazar artículo",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    IconButton(onClick = onDismiss) {
                                        Icon(Icons.Default.Close, "Cerrar", tint = TextMuted)
                                    }
                                }
                            }
                            
                            // Search bar (siempre visible)
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar artículo...", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                                trailingIcon = {
                                    if (headerAlpha.value <= 0.1f) {
                                        IconButton(onClick = onDismiss) {
                                            Icon(Icons.Default.Close, "Cerrar", tint = TextMuted)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = Surface,
                                    focusedContainerColor = Surface.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Surface.copy(alpha = 0.3f),
                                    cursorColor = PrimaryPurple,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        
                            // Posts list con UnifiedProductCard
                            if (filteredPosts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.Inventory2,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "No se encontraron artículos" else "No tienes artículos publicados",
                                            color = TextMuted,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    items(filteredPosts.size) { index ->
                                        val post = filteredPosts[index]
                                        com.vinzay.app.ui.components.UnifiedProductCard(
                                            data = post.toProductCardData(),
                                            onClick = { onPostSelected(post) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rendFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentGold,
    unfocusedBorderColor = Surface,
    focusedContainerColor = Surface.copy(alpha = 0.3f),
    unfocusedContainerColor = Surface.copy(alpha = 0.3f),
    cursorColor = AccentGold,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

private fun formatVideoDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return if (minutes > 0) "%d:%02d".format(minutes, seconds) else "0:%02d".format(seconds)
}

private suspend fun Context.getCameraProviderRend(): ProcessCameraProvider {
    return suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener({ continuation.resume(future.get()) }, ContextCompat.getMainExecutor(this))
        }
    }
}

private suspend fun loadGalleryVideos(context: Context): List<GalleryVideo> = withContext(Dispatchers.IO) {
    val videos = mutableListOf<GalleryVideo>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    
    val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED)
    val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
    
    try {
        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            
            while (cursor.moveToNext() && videos.size < 100) {
                val id = cursor.getLong(idColumn)
                val duration = cursor.getLong(durationColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                videos.add(GalleryVideo(id, uri, duration, dateAdded))
            }
        }
    } catch (e: Exception) {
        Log.e("RendScreen", "Error loading videos", e)
    }
    videos
}
