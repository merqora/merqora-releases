package com.rendly.app.ui.screens.publish

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.rendly.app.data.repository.RendRepository
import com.rendly.app.ui.theme.*
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RendScreen(
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
                        currentStep = RendStep.Edit
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
                        if (selectedVideoUri != null) currentStep = RendStep.Edit
                    }
                )
            }
            
            is RendStep.Edit -> {
                selectedVideoUri?.let { uri ->
                    RendVideoEditor(
                        videoUri = uri,
                        onBack = { currentStep = RendStep.Camera },
                        onNext = { currentStep = RendStep.Details }
                    )
                }
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
                    onBack = { currentStep = RendStep.Edit },
                    onPublish = { linkedPost ->
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
                                    productImage = productImageUrl, // Imagen de Cloudinary del producto enlazado
                                    productId = linkedPost?.productId, // Mismo product_id para unificar reviews
                                    duration = 15
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
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Merqora")
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
                    com.rendly.app.ui.components.ModeCarousel(
                        currentIndex = currentModeIndex,
                        onModeSelected = onModeSelected,
                        style = com.rendly.app.ui.components.CarouselStyle.CENTERED_SINGLE
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
    onPublish: (linkedPost: com.rendly.app.data.model.Post?) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Estado para enlazar post
    var showPostLinkModal by remember { mutableStateOf(false) }
    var linkedPost by remember { mutableStateOf<com.rendly.app.data.model.Post?>(null) }
    val userPosts by com.rendly.app.data.repository.PostRepository.userPosts.collectAsState()
    
    // Cargar posts del usuario
    LaunchedEffect(Unit) {
        com.rendly.app.data.repository.PostRepository.loadUserPosts()
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header con botón Publicar a la derecha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    enabled = !isPublishing,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(OverlayMedium)
                ) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Detalles del Rend", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            // Botón Publicar en header
            Button(
                onClick = { onPublish(linkedPost) },
                enabled = !isPublishing && title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    disabledContainerColor = AccentGold.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Publish, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publicar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        
        // Contenido scrolleable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            // Preview del video
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
            ) {
                if (videoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(videoUri).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentGold)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("REND", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Título
            RendSectionHeader(Icons.Default.Title, "Título")
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Dale un título a tu Rend...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = rendFieldColors(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Descripción
            RendSectionHeader(Icons.Default.Description, "Descripción")
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("Describe tu video...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(100.dp),
                colors = rendFieldColors(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Surface, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(20.dp))
            
            // Producto (opcional)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ShoppingBag, null, tint = AccentGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Producto (opcional)", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = productTitle,
                onValueChange = onProductTitleChange,
                placeholder = { Text("Nombre del producto", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Inventory, null, tint = AccentGold) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = rendFieldColors(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = productPrice,
                onValueChange = onProductPriceChange,
                placeholder = { Text("Precio (ej: 99.99)", color = TextMuted) },
                leadingIcon = { 
                    Text(
                        "$", 
                        color = AccentGold, 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.padding(start = 12.dp)
                    ) 
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = rendFieldColors(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Surface, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(20.dp))
            
            // Enlazar publicación (opcional)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Link, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enlazar publicación (opcional)", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón para enlazar post o mostrar post enlazado
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showPostLinkModal = true },
                color = Surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (linkedPost != null) {
                    // Mostrar post enlazado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail del post
                        AsyncImage(
                            model = linkedPost?.images?.firstOrNull(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Surface)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = linkedPost?.title ?: "",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = linkedPost?.price?.let { "$$it" } ?: "Sin precio",
                                color = AccentGold,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = { linkedPost = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, "Quitar", tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    // Botón para seleccionar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.AddLink, null, tint = PrimaryPurple, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar artículo", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
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
    posts: List<com.rendly.app.data.model.Post>,
    onDismiss: () -> Unit,
    onPostSelected: (com.rendly.app.data.model.Post) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
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
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .navigationBarsPadding()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { }
                        ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = HomeBg
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 12.dp)
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TextMuted.copy(alpha = 0.3f))
                        )
                        
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                        
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar artículo...", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
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
                        
                        // Posts list
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredPosts) { post ->
                                    PostLinkItem(
                                        post = post,
                                        onClick = { onPostSelected(post) }
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

@Composable
private fun PostLinkItem(
    post: com.rendly.app.data.model.Post,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Imagen del post
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = post.images.firstOrNull(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Badge de precio
                post.price?.let { price ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentGold)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$$price",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Info del post
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = post.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.likesCount}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
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
