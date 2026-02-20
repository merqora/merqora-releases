package com.rendly.app.ui.screens.publish

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.rendly.app.service.StoryUploadService
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue
import kotlin.math.sqrt
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.geometry.Offset
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.rendly.app.ui.components.textAnimation
import com.rendly.app.ui.components.TransformableImage
import com.rendly.app.gpu.TransformBridge
import com.rendly.app.gpu.rememberTransformEngine
import com.rendly.app.gpu.TransformAnimationEffect
import com.rendly.app.gpu.transformGestures
import com.rendly.app.gpu.transformGesturesWithDoubleTap
import com.rendly.app.gpu.applyTransform

// Herramientas de edición para historias
private data class EditTool(
    val id: String,
    val icon: ImageVector,
    val label: String
)

private val STORY_EDIT_TOOLS = listOf(
    EditTool("save", Icons.Outlined.SaveAlt, "Guardar"),
    EditTool("filter", Icons.Outlined.AutoAwesome, "Filtros"),
    EditTool("text", Icons.Outlined.TextFields, "Texto"),
    EditTool("gif", Icons.Outlined.Gif, "GIF"),
    EditTool("image", Icons.Outlined.Image, "Imagen"),
    EditTool("draw", Icons.Outlined.Draw, "Dibujar"),
    EditTool("adjust", Icons.Outlined.Tune, "Ajustar")
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onClose: () -> Unit,
    onModeSelected: (Int) -> Unit,
    currentModeIndex: Int,
    onEditingStateChange: (Boolean) -> Unit = {}, // Para deshabilitar swipe cuando hay imagen capturada
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Permiso de Cámara - se solicita inmediatamente al entrar
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Permiso de galería
    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val galleryPermissionState = rememberPermissionState(galleryPermission)
    
    // Solicitar permiso de Cámara al entrar
    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }
    
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedGalleryUri by remember { mutableStateOf<Uri?>(null) }
    var isPublishing by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    
    // Estado para animación de transición fluida
    var showCapturedPreview by remember { mutableStateOf(false) }
    
    // ---------------------------------------------------------------
    // SISTEMA DE FILTROS - Optimizado para GPU (sin crear Bitmaps para preview)
    // ---------------------------------------------------------------
    var showFilterCarousel by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(com.rendly.app.ui.components.STORY_FILTERS.first()) }
    
    // ---------------------------------------------------------------
    // SISTEMA DE TEXTO - Editor de texto para historias
    // ---------------------------------------------------------------
    var showTextEditor by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf(com.rendly.app.ui.components.StoryTextState()) }
    var keyboardHeight by remember { mutableStateOf(0.dp) }
    
    // ---------------------------------------------------------------
    // SISTEMA DE GIF - Modal de Selección de GIFs y overlays
    // ---------------------------------------------------------------
    var showGifPicker by remember { mutableStateOf(false) }
    var gifOverlays by remember { mutableStateOf<List<GifOverlay>>(emptyList()) }
    
    // ---------------------------------------------------------------
    // SISTEMA DE IMÁGENES SUPERPUESTAS - Similar a GIF pero con Imagenes
    // ---------------------------------------------------------------
    var showImagePicker by remember { mutableStateOf(false) }
    var imageOverlays by remember { mutableStateOf<List<ImageOverlay>>(emptyList()) }
    
    // Launcher para seleccionar Imagenes de la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Agregar nueva imagen como overlay con zIndex Más alto
            val newZIndex = (imageOverlays.maxOfOrNull { it.zIndex } ?: -1) + 1
            imageOverlays = imageOverlays + ImageOverlay(uri = it, zIndex = newZIndex)
        }
        showImagePicker = false
    }
    
    // Abrir picker cuando showImagePicker cambia a true
    LaunchedEffect(showImagePicker) {
        if (showImagePicker) {
            imagePickerLauncher.launch("image/*")
        }
    }
    
    // ---------------------------------------------------------------
    // SISTEMA DE DIBUJO - Canvas ultra-optimizado
    // ---------------------------------------------------------------
    var showDrawingMode by remember { mutableStateOf(false) }
    val drawingStrokesList = remember { mutableStateListOf<com.rendly.app.ui.components.DrawingStroke>() }
    var drawingStrokes by remember { mutableStateOf<List<com.rendly.app.ui.components.DrawingStroke>>(emptyList()) }
    var drawingColor by remember { mutableStateOf(Color.White) }
    var drawingTool by remember { mutableStateOf(com.rendly.app.ui.components.DrawingTool.PEN) }
    val drawingStrokeWidth = remember { mutableStateOf(8f) }
    
    // ---------------------------------------------------------------
    // SISTEMA DE AJUSTES GPU-FIRST - Calidad Instagram/Lightroom
    // Preview en tiempo real a 60+ FPS sin recomposiciones
    // ---------------------------------------------------------------
    var showAdjustMode by remember { mutableStateOf(false) }
    var adjustState by remember { mutableStateOf(com.rendly.app.ui.components.ImageAdjustState()) }
    var selectedAdjustment by remember { mutableStateOf<com.rendly.app.ui.components.AdjustmentType?>(null) }
    // Bitmap con filtro pre-aplicado para modo ajustar (evita diferencia de color)
    var filteredBitmapForAdjust by remember { mutableStateOf<Bitmap?>(null) }
    
    // Estado de edición: true = mostrando herramientas, false = mostrando botones Tu Vitrina/Frecuentes
    var isEditingMode by remember { mutableStateOf(true) }
    // Para mostrar el texto educativo "Pulsa cuando termines" - SOLO UNA VEZ por Sesión
    var showEducationalText by remember { mutableStateOf(false) }
    var hasShownEducationalText by remember { mutableStateOf(false) }
    
    // ---------------------------------------------------------------
    // SISTEMA UNDO/REDO - Historial de trazos de dibujo
    // ---------------------------------------------------------------
    val undoStack = remember { mutableStateListOf<com.rendly.app.ui.components.DrawingStroke>() }
    val canUndo by remember { derivedStateOf { drawingStrokesList.isNotEmpty() } }
    val canRedo by remember { derivedStateOf { undoStack.isNotEmpty() } }
    
    // Estado GPU para preview en tiempo real (sin crear bitmaps)
    val gpuAdjustState by remember(adjustState) {
        derivedStateOf { adjustState.toGPUState() }
    }
    
    // ---------------------------------------------------------------
    // MOTOR DE TRANSFORMación C++ - Para mover/escalar/rotar imagen capturada
    // ---------------------------------------------------------------
    val imageTransformEngine = rememberTransformEngine(
        minScale = 0.5f,
        maxScale = 4.0f,
        friction = 0.92f,
        snapAngle = 0f
    )
    
    // Animation loop para inercia del motor C++
    TransformAnimationEffect(imageTransformEngine)
    
    // ---------------------------------------------------------------
    // SISTEMA DE GESTOS ULTRA FLUIDO - Nivel Instagram/TikTok
    // Usando mutableFloatStateOf para evitar recomposición por frame
    // Solo se lee en graphicsLayer = Máximo rendimiento
    // ---------------------------------------------------------------
    val textOffsetX = remember { mutableStateOf(0f) }
    val textOffsetY = remember { mutableStateOf(0f) }
    val textRotation = remember { mutableStateOf(0f) }
    val textScale = remember { mutableStateOf(1f) }
    var isDraggingText by remember { mutableStateOf(false) }
    var isDraggingGif by remember { mutableStateOf(false) }
    
    // Activar animación cuando hay imagen capturada
    LaunchedEffect(capturedBitmap) {
        showCapturedPreview = capturedBitmap != null
        // Notificar al padre para deshabilitar swipe horizontal
        onEditingStateChange(capturedBitmap != null)
        if (capturedBitmap == null) {
            // Reset al cerrar
            selectedFilter = com.rendly.app.ui.components.STORY_FILTERS.first()
            showFilterCarousel = false
            showTextEditor = false
            textState = com.rendly.app.ui.components.StoryTextState()
            textOffsetX.value = 0f
            textOffsetY.value = 0f
            textRotation.value = 0f
            textScale.value = 1f
            isEditingMode = true
            showEducationalText = false
            imageTransformEngine.reset() // Reset motor C++
            com.rendly.app.ui.components.FilterProcessor.clearCache()
        } else {
            // Resetear estado al capturar nueva imagen
            showTextEditor = false
            textState = com.rendly.app.ui.components.StoryTextState()
            textOffsetX.value = 0f
            textOffsetY.value = 0f
            textRotation.value = 0f
            textScale.value = 1f
            isEditingMode = true
            imageTransformEngine.reset() // Reset motor C++
            // RESET UNDO/REDO al capturar nueva imagen
            drawingStrokesList.clear()
            drawingStrokes = emptyList()
            undoStack.clear()
            // Mostrar texto educativo SOLO UNA VEZ por Sesión
            if (!hasShownEducationalText) {
                kotlinx.coroutines.delay(1500)
                showEducationalText = true
                hasShownEducationalText = true
                kotlinx.coroutines.delay(4000)
                showEducationalText = false
            }
        }
    }
    
    // Altura fija del preview - calculada una vez para consistencia
    val previewHeight = screenHeight * 0.86f // Llega hasta Más abajo de los botones
    val previewCornerRadius = 24.dp
    val previewTopPadding = 8.dp // pequeño padding para no tocar las curvas del Teléfono
    
    // Valores en píxeles para renderizado final (calculados en contexto Composable)
    val density = LocalDensity.current
    val previewHeightPx: Float = with(density) { previewHeight.toPx() }
    val previewWidthPx: Float = previewHeightPx * 0.9f
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedGalleryUri = it
            // Cargar bitmap desde URI
            scope.launch {
                val bitmap = loadBitmapFromUri(context, it)
                if (bitmap != null) {
                    capturedBitmap = bitmap
                }
            }
        }
    }
    
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(android.view.Surface.ROTATION_0)
            .setFlashMode(flashMode)
            .build()
    }
    
    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
            .statusBarsPadding()
    ) {
        if (capturedBitmap == null) {
            // ---------------------------------------------------------------
            // VISTA DE cámara - Preview con border radius y altura correcta
            // ---------------------------------------------------------------
            
            // Preview de Cámara con border radius - comienza arriba y llega hasta mitad de botones
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight)
                    .background(Color.Black) // Fondo negro sólido para evitar transparencia
                    .padding(top = previewTopPadding, start = 4.dp, end = 4.dp)
                    .clip(RoundedCornerShape(previewCornerRadius))
                    .clipToBounds() // Evitar fugas visuales del contenido
                    .align(Alignment.TopCenter)
            ) {
                if (cameraPermission.status.isGranted) {
                    CameraPreview(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        lensFacing = lensFacing,
                        imageCapture = imageCapture,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder mientras espera permiso
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0A0A0F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Permiso de Cámara requerido",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Para tomar fotos de tu historia",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // Top bar (sobre el preview)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp + previewTopPadding)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Filled.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Filled.FlashAuto
                            else -> Icons.Filled.FlashOff
                        },
                        contentDescription = "Flash",
                        tint = Color.White
                    )
                }
            }
            
            // ---------------------------------------------------------------
            // CONTROLES INFERIORES - Botones centrados + Carrusel con Galería y Girar a los lados
            // ---------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // botón de captura central grande
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(enabled = !isCapturing) {
                            if (!isCapturing) {
                                isCapturing = true
                                // Captura inmediata sin delay
                                takePhotoToMemoryFast(
                                    context = context,
                                    imageCapture = imageCapture,
                                    onSuccess = { bitmap ->
                                        capturedBitmap = bitmap
                                        isCapturing = false
                                    },
                                    onError = {
                                        isCapturing = false
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Row con [Galería] [Carrusel de modos] [Girar]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // botón de galería (izquierda)
                    IconButton(
                        onClick = {
                            if (galleryPermissionState.status.isGranted) {
                                galleryLauncher.launch("image/*")
                            } else {
                                galleryPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
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
                    
                    // botón de girar Cámara (derecha)
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cameraswitch,
                            contentDescription = "Voltear Cámara",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
        
        // ---------------------------------------------------------------
        // VISTA DE PREVIEW CON IMAGEN CAPTURADA + HERRAMIENTAS DE EDición
        // Transición fluida con animación de escala y fade
        // ---------------------------------------------------------------
        AnimatedVisibility(
            visible = capturedBitmap != null,
            enter = fadeIn(animationSpec = tween(250)) + 
                    scaleIn(initialScale = 0.95f, animationSpec = tween(300, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(200)) + 
                   scaleOut(targetScale = 0.95f, animationSpec = tween(200))
        ) {
            capturedBitmap?.let { bitmap ->
                // ---------------------------------------------------------------
                // ESTADO COMPARTIDO PARA OVERLAYS - Definido antes del Box
                // ---------------------------------------------------------------
                val hasTextOverlay = textState.text.isNotEmpty() && !showTextEditor
                val hasGifOverlay = gifOverlays.isNotEmpty()
                val hasImageOverlay = imageOverlays.isNotEmpty()
                val hasAnyOverlay = hasTextOverlay || hasGifOverlay || hasImageOverlay
                
                // Estado compartido para zona de eliminación
                var showDeleteZone by remember { mutableStateOf(false) }
                var isOverDeleteZone by remember { mutableStateOf(false) }
                var deletingOverlayType by remember { mutableStateOf<String?>(null) }
                
                // Estado para líneas Guía
                var showCenterLineH by remember { mutableStateOf(false) }
                var showCenterLineV by remember { mutableStateOf(false) }
                val guideThreshold = 25f
                val guideSnapForce = 0.35f
                val guideMagneticRange = 60f
                
                // Escala del elemento cuando está sobre zona de eliminación
                val deleteScale = remember { mutableStateOf(1f) }
                
                // Vibrador
                val vibrator = remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vibratorManager?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }
                }
                
                val vibrateSnap = {
                    vibrator?.let { v ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(8, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            v.vibrate(8)
                        }
                    }
                }
                
                val vibrateDelete = {
                    vibrator?.let { v ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            v.vibrate(25)
                        }
                    }
                }
                
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Preview de imagen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(previewHeight)
                            .padding(top = previewTopPadding, start = 4.dp, end = 4.dp)
                            .clip(RoundedCornerShape(previewCornerRadius))
                            .align(Alignment.TopCenter)
                            // ---------------------------------------------------------------
                            // GESTO GLOBAL - Mover texto/GIF desde CUALQUIER parte del preview
                            // ---------------------------------------------------------------
                            .pointerInput(hasTextOverlay, hasGifOverlay, showAdjustMode, showDrawingMode, showTextEditor) {
                                if (!hasAnyOverlay || showAdjustMode || showDrawingMode || showTextEditor) return@pointerInput
                                
                                val previewHeightPx = with(density) { previewHeight.toPx() }
                                val deleteZoneThreshold = with(density) { 50.dp.toPx() }
                                val deleteZoneStartY = (previewHeightPx / 2f) - deleteZoneThreshold
                                
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = true)
                                    
                                    val movingText = hasTextOverlay
                                    val movingGifId = if (!movingText && hasGifOverlay) gifOverlays.lastOrNull()?.id else null
                                    
                                    if (movingText) {
                                        isDraggingText = true
                                        deletingOverlayType = "text"
                                    } else if (movingGifId != null) {
                                        isDraggingGif = true
                                        deletingOverlayType = "gif:$movingGifId"
                                    }
                                    
                                    var anyPressed = true
                                    var hasMovedEnough = false
                                    var totalMovement = 0f
                                    var vibratedDelete = false
                                    
                                    while (anyPressed) {
                                        val event = awaitPointerEvent()
                                        val pressed = event.changes.filter { it.pressed }
                                        
                                        if (pressed.isNotEmpty()) {
                                            try {
                                                val pan = event.calculatePan()
                                                val zoom = event.calculateZoom()
                                                val rotation = event.calculateRotation()
                                                
                                                val currentScale = if (movingText) textScale.value else 
                                                    gifOverlays.find { it.id == movingGifId }?.scale ?: 1f
                                                val scale = currentScale.coerceAtLeast(0.3f)
                                                
                                                var newX: Float
                                                var newY: Float
                                                if (movingText) {
                                                    newX = textOffsetX.value + pan.x
                                                    newY = textOffsetY.value + pan.y
                                                } else {
                                                    val gif = gifOverlays.find { it.id == movingGifId }
                                                    if (gif != null) {
                                                        newX = gif.offsetX + pan.x
                                                        newY = gif.offsetY + pan.y
                                                    } else {
                                                        newX = 0f; newY = 0f
                                                    }
                                                }
                                                
                                                totalMovement += pan.x.absoluteValue + pan.y.absoluteValue
                                                if (totalMovement > 30f) hasMovedEnough = true
                                                
                                                if (hasMovedEnough && !showDeleteZone) showDeleteZone = true
                                                
                                                val isInDeleteZone = newY > deleteZoneStartY && hasMovedEnough
                                                if (isInDeleteZone) {
                                                    isOverDeleteZone = true
                                                    val proximity = ((newY - deleteZoneStartY) / deleteZoneThreshold).coerceIn(0f, 1f)
                                                    val deleteTargetX = 0f
                                                    val deleteTargetY = (previewHeightPx / 2f) - with(density) { 46.dp.toPx() }
                                                    val magnetForce = proximity * 0.85f
                                                    newX = newX + (deleteTargetX - newX) * magnetForce
                                                    newY = newY + (deleteTargetY - newY) * magnetForce
                                                    deleteScale.value = (1f - proximity * 0.92f).coerceIn(0.08f, 1f)
                                                    if (!vibratedDelete) { vibrateDelete(); vibratedDelete = true }
                                                } else {
                                                    isOverDeleteZone = false
                                                    deleteScale.value = 1f
                                                    vibratedDelete = false
                                                }
                                                
                                                if (!isOverDeleteZone) {
                                                    if (newX.absoluteValue < guideMagneticRange) {
                                                        val mag = 1f - (newX.absoluteValue / guideMagneticRange)
                                                        newX *= (1f - guideSnapForce * mag)
                                                        if (newX.absoluteValue < guideThreshold && !showCenterLineV) {
                                                            showCenterLineV = true; vibrateSnap()
                                                        }
                                                    } else showCenterLineV = false
                                                    
                                                    if (newY.absoluteValue < guideMagneticRange) {
                                                        val mag = 1f - (newY.absoluteValue / guideMagneticRange)
                                                        newY *= (1f - guideSnapForce * mag)
                                                        if (newY.absoluteValue < guideThreshold && !showCenterLineH) {
                                                            showCenterLineH = true; vibrateSnap()
                                                        }
                                                    } else showCenterLineH = false
                                                }
                                                
                                                if (movingText) {
                                                    textOffsetX.value = newX
                                                    textOffsetY.value = newY
                                                    textRotation.value += rotation
                                                    if (zoom != 1f) textScale.value = (textScale.value * zoom).coerceIn(0.3f, 3f)
                                                } else if (movingGifId != null) {
                                                    val gifIdx = gifOverlays.indexOfFirst { it.id == movingGifId }
                                                    if (gifIdx >= 0) {
                                                        val gif = gifOverlays[gifIdx]
                                                        gifOverlays = gifOverlays.toMutableList().apply {
                                                            this[gifIdx] = gif.copy(
                                                                offsetX = newX,
                                                                offsetY = newY,
                                                                rotation = gif.rotation + rotation,
                                                                scale = if (zoom != 1f) (gif.scale * zoom).coerceIn(0.3f, 3f) else gif.scale
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                                            } catch (_: Exception) { }
                                        }
                                        anyPressed = event.changes.any { it.pressed }
                                    }
                                    
                                    isDraggingText = false
                                    isDraggingGif = false
                                    showDeleteZone = false
                                    showCenterLineH = false
                                    showCenterLineV = false
                                    
                                    if (isOverDeleteZone) {
                                        if (movingText) {
                                            textState = com.rendly.app.ui.components.StoryTextState()
                                            textOffsetX.value = 0f
                                            textOffsetY.value = 0f
                                            textRotation.value = 0f
                                            textScale.value = 1f
                                        } else if (movingGifId != null) {
                                            gifOverlays = gifOverlays.filter { it.id != movingGifId }
                                        }
                                        isOverDeleteZone = false
                                    }
                                    deleteScale.value = 1f
                                    deletingOverlayType = null
                                }
                            }
                    ) {
                        // ---------------------------------------------------------------
                        // PREVIEW - Con gestos directos para transformación correcta
                        // Escala y rotación desde el centro, paneo fluido
                        // ---------------------------------------------------------------
                        // MODIFICADO: Bloquear transformación de imagen cuando hay overlay activo
                        // Los overlays tienen prioridad - mientras se arrastra un overlay, la imagen NO se mueve
                        val canTransformImage = !showAdjustMode && !showDrawingMode && !showTextEditor && !showFilterCarousel && !isDraggingText && !isDraggingGif
                        
                        // Estados locales para transformación de imagen principal
                        val mainImgOffsetX = remember { mutableStateOf(0f) }
                        val mainImgOffsetY = remember { mutableStateOf(0f) }
                        val mainImgScale = remember { mutableStateOf(1f) }
                        val mainImgRotation = remember { mutableStateOf(0f) }
                        
                        // Reset cuando cambia la imagen
                        LaunchedEffect(bitmap) {
                            mainImgOffsetX.value = 0f
                            mainImgOffsetY.value = 0f
                            mainImgScale.value = 1f
                            mainImgRotation.value = 0f
                        }
                        
                        // Variable para rastrear doble tap global (funciona incluso sobre texto)
                        var lastTapTime by remember { mutableStateOf(0L) }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(canTransformImage, textState.text, "globalDoubleTap") {
                                    // Doble tap GLOBAL - funciona en toda el área, incluso sobre texto
                                    detectTapGestures(
                                        onTap = {
                                            val now = System.currentTimeMillis()
                                            if (now - lastTapTime < 300L && canTransformImage) {
                                                // Doble tap detectado - resetear imagen
                                                mainImgOffsetX.value = 0f
                                                mainImgOffsetY.value = 0f
                                                mainImgScale.value = 1f
                                                mainImgRotation.value = 0f
                                            }
                                            lastTapTime = now
                                        }
                                    )
                                }
                                .pointerInput(canTransformImage, "transform") {
                                    if (!canTransformImage) return@pointerInput
                                    detectTransformGestures { centroid, pan, zoom, rotation ->
                                        // Paneo directo
                                        mainImgOffsetX.value += pan.x
                                        mainImgOffsetY.value += pan.y
                                        // Escala con límites
                                        mainImgScale.value = (mainImgScale.value * zoom).coerceIn(0.5f, 4f)
                                        // Rotación
                                        mainImgRotation.value += rotation
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                                        translationX = mainImgOffsetX.value
                                        translationY = mainImgOffsetY.value
                                        scaleX = mainImgScale.value
                                        scaleY = mainImgScale.value
                                        rotationZ = mainImgRotation.value
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (showAdjustMode && filteredBitmapForAdjust != null) {
                                    com.rendly.app.gpu.GPUAdjustedImage(
                                        bitmap = filteredBitmapForAdjust,
                                        adjustments = gpuAdjustState,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    com.rendly.app.ui.components.FilteredImage(
                                        bitmap = bitmap,
                                        filter = selectedFilter,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // CANVAS DE DIBUJO estáTICO - Muestra los trazos guardados
                        // ---------------------------------------------------------------
                        if (drawingStrokes.isNotEmpty() && !showDrawingMode) {
                            com.rendly.app.ui.components.DrawingCanvasStatic(
                                strokes = drawingStrokes,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // ---------------------------------------------------------------
                        // botón VOLVER A cámara (Arrow Left) - Solo visible cuando NO hay herramienta activa
                        // ---------------------------------------------------------------
                        AnimatedVisibility(
                            visible = !showFilterCarousel && !showTextEditor && !showDrawingMode && !showAdjustMode,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(100)),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 12.dp, top = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { 
                                        // RESET COMPLETO de todas las herramientas
                                        capturedBitmap = null
                                        showAdjustMode = false
                                        adjustState = com.rendly.app.ui.components.ImageAdjustState()
                                        selectedAdjustment = null
                                        showFilterCarousel = false
                                        selectedFilter = com.rendly.app.ui.components.STORY_FILTERS.first()
                                        showTextEditor = false
                                        textState = com.rendly.app.ui.components.StoryTextState()
                                        textOffsetX.value = 0f
                                        textOffsetY.value = 0f
                                        textRotation.value = 0f
                                        textScale.value = 1f
                                        showDrawingMode = false
                                        drawingStrokes = emptyList()
                                        drawingStrokesList.clear()
                                        drawingColor = Color.White
                                        showGifPicker = false
                                        gifOverlays = emptyList()
                                        isEditingMode = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // BOTONES UNDO/REDO - Esquina superior derecha
                        // Solo visibles cuando hay trazos de dibujo y NO hay herramienta activa
                        // ---------------------------------------------------------------
                        AnimatedVisibility(
                            visible = (canUndo || canRedo) && !showFilterCarousel && !showTextEditor && !showDrawingMode && !showAdjustMode,
                            enter = fadeIn(tween(150)) + slideInHorizontally { it },
                            exit = fadeOut(tween(100)) + slideOutHorizontally { it },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp, top = 12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // botón UNDO
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (canUndo) Color.Black.copy(alpha = 0.5f) 
                                            else Color.Black.copy(alpha = 0.2f)
                                        )
                                        .clickable(enabled = canUndo) {
                                            if (drawingStrokesList.isNotEmpty()) {
                                                val lastStroke = drawingStrokesList.removeAt(drawingStrokesList.lastIndex)
                                                undoStack.add(lastStroke)
                                                drawingStrokes = drawingStrokesList.toList()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Undo,
                                        contentDescription = "Deshacer",
                                        tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                // botón REDO
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (canRedo) Color.Black.copy(alpha = 0.5f) 
                                            else Color.Black.copy(alpha = 0.2f)
                                        )
                                        .clickable(enabled = canRedo) {
                                            if (undoStack.isNotEmpty()) {
                                                val strokeToRedo = undoStack.removeAt(undoStack.lastIndex)
                                                drawingStrokesList.add(strokeToRedo)
                                                drawingStrokes = drawingStrokesList.toList()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Redo,
                                        contentDescription = "Rehacer",
                                        tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // botón TICK (Aplicar Ajustes) - Esquina superior derecha
                        // Solo visible en modo ajustes
                        // ---------------------------------------------------------------
                        AnimatedVisibility(
                            visible = showAdjustMode,
                            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f),
                            exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.8f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp, top = 12.dp)
                        ) {
                            var isApplying by remember { mutableStateOf(false) }
                            
                            // Mismo estilo que Botón arrow left (fondo negro 0.5 alpha, 40dp)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable(enabled = !isApplying) { 
                                        // Aplicar ajustes PRIMERO y luego cerrar
                                        if (adjustState.hasChanges()) {
                                            isApplying = true
                                            scope.launch {
                                                // Aplicar en thread de IO para no bloquear UI
                                                val adjusted = kotlinx.coroutines.withContext(Dispatchers.Default) {
                                                    capturedBitmap?.let { bmp ->
                                                        com.rendly.app.ui.components.ImageAdjustProcessor.applyForExport(bmp, adjustState)
                                                    }
                                                }
                                                // Actualizar bitmap y cerrar herramienta
                                                adjusted?.let { capturedBitmap = it }
                                                adjustState = com.rendly.app.ui.components.ImageAdjustState()
                                                isApplying = false
                                                showAdjustMode = false
                                                selectedAdjustment = null
                                            }
                                        } else {
                                            // Sin cambios, solo cerrar
                                            showAdjustMode = false
                                            selectedAdjustment = null
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isApplying) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Aplicar",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // SLIDER DE AJUSTES - En parte baja del preview cuando hay ajuste seleccionado
                        // ---------------------------------------------------------------
                        com.rendly.app.ui.components.ImageAdjustOverlay(
                            visible = showAdjustMode,
                            bitmap = bitmap,
                            adjustState = adjustState,
                            selectedAdjustment = selectedAdjustment,
                            onAdjustmentSelected = { selectedAdjustment = it },
                            onValueChange = { newState -> 
                                adjustState = newState
                            },
                            previewHeight = previewHeight,
                            onApply = { state ->
                                adjustState = state
                                capturedBitmap?.let { bmp ->
                                    if (state.hasChanges()) {
                                        val adjusted = com.rendly.app.ui.components.ImageAdjustProcessor.applyForExport(bmp, state)
                                        capturedBitmap = adjusted
                                    }
                                }
                                showAdjustMode = false
                                selectedAdjustment = null
                                adjustState = com.rendly.app.ui.components.ImageAdjustState()
                            },
                            onDismiss = { 
                                showAdjustMode = false
                                selectedAdjustment = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(previewHeight)
                                .padding(top = previewTopPadding, start = 4.dp, end = 4.dp)
                                .clip(RoundedCornerShape(previewCornerRadius))
                                .align(Alignment.TopCenter)
                        )
                        
                        // Animaciones del icono de basura
                        val deleteIconScale by animateFloatAsState(
                            targetValue = if (isOverDeleteZone) 1.15f else 1f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
                            label = "deleteScale"
                        )
                        
                        // ---------------------------------------------------------------
                        // ICONO DE BASURA (zona de eliminación)
                        // ---------------------------------------------------------------
                        AnimatedVisibility(
                            visible = showDeleteZone,
                            enter = fadeIn(tween(100)) + scaleIn(initialScale = 0.8f),
                            exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.8f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = deleteIconScale
                                        scaleY = deleteIconScale
                                    }
                                    .size(44.dp) // más Pequeño
                                    .background(
                                        color = if (isOverDeleteZone) Color.Red.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.6f),
                                        shape = CircleShape
                                    )
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Eliminar",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // LÍNEAS guía DE SNAP
                        // ---------------------------------------------------------------
                        AnimatedVisibility(
                            visible = showCenterLineH,
                            enter = fadeIn(tween(50)),
                            exit = fadeOut(tween(100)),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.7f))
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = showCenterLineV,
                            enter = fadeIn(tween(50)),
                            exit = fadeOut(tween(100)),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .background(Color.White.copy(alpha = 0.7f))
                            )
                        }
                        
                        // ---------------------------------------------------------------
                        // TEXTO OVERLAY - Con gesture handler propio
                        // ---------------------------------------------------------------
                        if (textState.text.isNotEmpty() && !showTextEditor) {
                            var vibratedDelete by remember { mutableStateOf(false) }
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .graphicsLayer {
                                        translationX = textOffsetX.value
                                        translationY = textOffsetY.value
                                        rotationZ = textRotation.value
                                        val currentScale = textScale.value * (if (deletingOverlayType == "text") deleteScale.value else 1f)
                                        val activeScale = currentScale * (if (isDraggingText && !isOverDeleteZone) 1.02f else 1f)
                                        scaleX = activeScale
                                        scaleY = activeScale
                                    }
                                    .pointerInput(textState.text) {
                                        // Altura del preview en píxeles
                                        val previewHeightPx = with(density) { previewHeight.toPx() }
                                        // Zona de eliminación: SOLO los últimos 50dp del borde inferior
                                        val deleteZoneThreshold = with(density) { 50.dp.toPx() }
                                        val deleteZoneStartY = (previewHeightPx / 2f) - deleteZoneThreshold
                                        
                                        awaitEachGesture {
                                            awaitFirstDown(requireUnconsumed = false)
                                            isDraggingText = true
                                            deletingOverlayType = "text"
                                            
                                            var anyPressed = true
                                            var hasMovedEnough = false
                                            var totalMovement = 0f
                                            
                                            while (anyPressed) {
                                                val event = awaitPointerEvent()
                                                val pressed = event.changes.filter { it.pressed }
                                                
                                                if (pressed.isNotEmpty()) {
                                                    try {
                                                        val pan = event.calculatePan()
                                                        val zoom = event.calculateZoom()
                                                        val rotation = event.calculateRotation()
                                                        
                                                        // Velocidad de paneo CONSTANTE - NO depende del Tamaño/escala del texto
                                                        // Usamos pan directamente sin modificaciones para velocidad uniforme
                                                        var newX = textOffsetX.value + pan.x
                                                        var newY = textOffsetY.value + pan.y
                                                        
                                                        totalMovement += pan.x.absoluteValue + pan.y.absoluteValue
                                                        if (totalMovement > 30f) hasMovedEnough = true
                                                        
                                                        // Solo mostrar zona de eliminación después de mover un poco
                                                        if (hasMovedEnough && !showDeleteZone) {
                                                            showDeleteZone = true
                                                        }
                                                        
                                                        // ---------------------------------------------------------------
                                                        // ZONA DE ELIMINación - Efecto magnético FUERTE hacia el bote
                                                        // El elemento se mete DENTRO del Botón de basura
                                                        // ---------------------------------------------------------------
                                                        val isInDeleteZone = newY > deleteZoneStartY && hasMovedEnough
                                                        
                                                        if (isInDeleteZone) {
                                                            isOverDeleteZone = true
                                                            val proximity = ((newY - deleteZoneStartY) / deleteZoneThreshold).coerceIn(0f, 1f)
                                                            
                                                            // Posición exacta del bote de basura (centro inferior del preview)
                                                            val deleteTargetX = 0f // Centro horizontal
                                                            val deleteTargetY = (previewHeightPx / 2f) - with(density) { 46.dp.toPx() }
                                                            
                                                            // Fuerza magnética MUY fuerte para atraer al bote
                                                            val magnetForce = proximity * 0.85f
                                                            newX = newX + (deleteTargetX - newX) * magnetForce
                                                            newY = newY + (deleteTargetY - newY) * magnetForce
                                                            
                                                            // Escala: reducir drásticamente hasta 8% para que QUEPA DENTRO del bote
                                                            deleteScale.value = (1f - proximity * 0.92f).coerceIn(0.08f, 1f)
                                                            
                                                            if (!vibratedDelete) {
                                                                vibrateDelete()
                                                                vibratedDelete = true
                                                            }
                                                        } else {
                                                            isOverDeleteZone = false
                                                            deleteScale.value = 1f
                                                            vibratedDelete = false
                                                        }
                                                        
                                                        // ---------------------------------------------------------------
                                                        // guíaS DE CENTRADO - Efecto magnético suave
                                                        // Atrae hacia el centro pero es fácil de despegar
                                                        // ---------------------------------------------------------------
                                                        if (!isOverDeleteZone) {
                                                            val nearCenterX = newX.absoluteValue < guideMagneticRange
                                                            val nearCenterY = newY.absoluteValue < guideMagneticRange
                                                            
                                                            // Fuerza magnética proporcional a la cercanía (Más cerca = Más fuerte)
                                                            if (nearCenterX) {
                                                                val magnetStrength = 1f - (newX.absoluteValue / guideMagneticRange)
                                                                newX = newX * (1f - guideSnapForce * magnetStrength)
                                                                if (newX.absoluteValue < guideThreshold && !showCenterLineV) {
                                                                    showCenterLineV = true
                                                                    vibrateSnap()
                                                                }
                                                            } else {
                                                                showCenterLineV = false
                                                            }
                                                            
                                                            if (nearCenterY) {
                                                                val magnetStrength = 1f - (newY.absoluteValue / guideMagneticRange)
                                                                newY = newY * (1f - guideSnapForce * magnetStrength)
                                                                if (newY.absoluteValue < guideThreshold && !showCenterLineH) {
                                                                    showCenterLineH = true
                                                                    vibrateSnap()
                                                                }
                                                            } else {
                                                                showCenterLineH = false
                                                            }
                                                        } else {
                                                            showCenterLineV = false
                                                            showCenterLineH = false
                                                        }
                                                        
                                                        // Aplicar movimiento
                                                        textOffsetX.value = newX
                                                        textOffsetY.value = newY
                                                        
                                                        // Rotación: aplicar directamente (sin modificación por escala)
                                                        textRotation.value += rotation
                                                        
                                                        // Zoom: aplicar directamente
                                                        if (zoom != 1f) {
                                                            textScale.value = (textScale.value * zoom).coerceIn(0.3f, 3f)
                                                        }
                                                        
                                                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                                                    } catch (e: Exception) { }
                                                }
                                                
                                                anyPressed = event.changes.any { it.pressed }
                                            }
                                            
                                            // Gesto terminado
                                            isDraggingText = false
                                            showDeleteZone = false
                                            showCenterLineH = false
                                            showCenterLineV = false
                                            
                                            if (isOverDeleteZone) {
                                                textState = com.rendly.app.ui.components.StoryTextState()
                                                textOffsetX.value = 0f
                                                textOffsetY.value = 0f
                                                textRotation.value = 0f
                                                textScale.value = 1f
                                                isOverDeleteZone = false
                                            }
                                            
                                            deleteScale.value = 1f
                                            deletingOverlayType = null
                                        }
                                    }
                            ) {
                                val textAnimColor = when (textState.backgroundState) {
                                    com.rendly.app.ui.components.TextBackgroundState.WHITE -> Color.Black
                                    else -> textState.color
                                }
                                Text(
                                    text = textState.text,
                                    color = textAnimColor,
                                    fontSize = textState.fontSize.sp,
                                    fontFamily = textState.fontOption.fontFamily,
                                    fontWeight = textState.fontOption.fontWeight,
                                    textAlign = when (textState.alignment) {
                                        com.rendly.app.ui.components.TextAlignOption.LEFT -> TextAlign.Start
                                        com.rendly.app.ui.components.TextAlignOption.CENTER -> TextAlign.Center
                                        com.rendly.app.ui.components.TextAlignOption.RIGHT -> TextAlign.End
                                    },
                                    modifier = (when (textState.backgroundState) {
                                        com.rendly.app.ui.components.TextBackgroundState.BLACK -> Modifier
                                            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                        com.rendly.app.ui.components.TextBackgroundState.WHITE -> Modifier
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                        com.rendly.app.ui.components.TextBackgroundState.NONE -> Modifier
                                    }).textAnimation(
                                        animationId = textState.animation.id,
                                        textColor = textAnimColor,
                                        isVisible = true
                                    )
                                )
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // GIFs OVERLAYS - Cada uno con gesture handler propio
                        // ---------------------------------------------------------------
                        gifOverlays.forEachIndexed { index, gifOverlay ->
                            key(gifOverlay.id) {
                                val gifOffsetX = remember(gifOverlay.id) { mutableStateOf(gifOverlay.offsetX) }
                                val gifOffsetY = remember(gifOverlay.id) { mutableStateOf(gifOverlay.offsetY) }
                                val gifScale = remember(gifOverlay.id) { mutableStateOf(gifOverlay.scale) }
                                val gifRotation = remember(gifOverlay.id) { mutableStateOf(gifOverlay.rotation) }
                                var vibratedDelete by remember { mutableStateOf(false) }
                                
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .graphicsLayer {
                                            translationX = gifOffsetX.value
                                            translationY = gifOffsetY.value
                                            rotationZ = gifRotation.value
                                            val currentScale = gifScale.value * (if (deletingOverlayType == "gif:${gifOverlay.id}") deleteScale.value else 1f)
                                            scaleX = currentScale
                                            scaleY = currentScale
                                        }
                                        .pointerInput(gifOverlay.id) {
                                            // Altura del preview en píxeles
                                            val previewHeightPx = with(density) { previewHeight.toPx() }
                                            // Zona de eliminación: SOLO los últimos 50dp del borde inferior
                                            val deleteZoneThreshold = with(density) { 50.dp.toPx() }
                                            val deleteZoneStartY = (previewHeightPx / 2f) - deleteZoneThreshold
                                            
                                            awaitEachGesture {
                                                awaitFirstDown(requireUnconsumed = false)
                                                isDraggingGif = true
                                                deletingOverlayType = "gif:${gifOverlay.id}"
                                                
                                                var anyPressed = true
                                                var hasMovedEnough = false
                                                var totalMovement = 0f
                                                
                                                while (anyPressed) {
                                                    val event = awaitPointerEvent()
                                                    val pressed = event.changes.filter { it.pressed }
                                                    
                                                    if (pressed.isNotEmpty()) {
                                                        try {
                                                            val pan = event.calculatePan()
                                                            val zoom = event.calculateZoom()
                                                            val rotation = event.calculateRotation()
                                                            
                                                            // Velocidad de paneo UNIFORME - Compensar escala
                                                            // Multiplicar por escala para que el movimiento sea igual sin importar el Tamaño
                                                            val currentGifScale = gifScale.value.coerceAtLeast(0.5f)
                                                            var newX = gifOffsetX.value + (pan.x * currentGifScale)
                                                            var newY = gifOffsetY.value + (pan.y * currentGifScale)
                                                            
                                                            totalMovement += pan.x.absoluteValue + pan.y.absoluteValue
                                                            if (totalMovement > 30f) hasMovedEnough = true
                                                            
                                                            // Solo mostrar zona de eliminación después de mover un poco
                                                            if (hasMovedEnough && !showDeleteZone) {
                                                                showDeleteZone = true
                                                            }
                                                            
                                                            // ---------------------------------------------------------------
                                                            // ZONA DE ELIMINación - Efecto magnético FUERTE hacia el bote
                                                            // El elemento se mete DENTRO del Botón de basura
                                                            // ---------------------------------------------------------------
                                                            val isInDeleteZone = newY > deleteZoneStartY && hasMovedEnough
                                                            
                                                            if (isInDeleteZone) {
                                                                isOverDeleteZone = true
                                                                val proximity = ((newY - deleteZoneStartY) / deleteZoneThreshold).coerceIn(0f, 1f)
                                                                
                                                                // Posición exacta del bote de basura
                                                                val deleteTargetX = 0f
                                                                val deleteTargetY = (previewHeightPx / 2f) - with(density) { 46.dp.toPx() }
                                                                
                                                                // Fuerza magnética MUY fuerte
                                                                val magnetForce = proximity * 0.85f
                                                                newX = newX + (deleteTargetX - newX) * magnetForce
                                                                newY = newY + (deleteTargetY - newY) * magnetForce
                                                                
                                                                // Escala: reducir drásticamente hasta 8%
                                                                deleteScale.value = (1f - proximity * 0.92f).coerceIn(0.08f, 1f)
                                                                
                                                                if (!vibratedDelete) {
                                                                    vibrateDelete()
                                                                    vibratedDelete = true
                                                                }
                                                            } else {
                                                                isOverDeleteZone = false
                                                                deleteScale.value = 1f
                                                                vibratedDelete = false
                                                            }
                                                            
                                                            // ---------------------------------------------------------------
                                                            // guíaS DE CENTRADO - Efecto magnético suave
                                                            // ---------------------------------------------------------------
                                                            if (!isOverDeleteZone) {
                                                                val nearCenterX = newX.absoluteValue < guideMagneticRange
                                                                val nearCenterY = newY.absoluteValue < guideMagneticRange
                                                                
                                                                if (nearCenterX) {
                                                                    val magnetStrength = 1f - (newX.absoluteValue / guideMagneticRange)
                                                                    newX = newX * (1f - guideSnapForce * magnetStrength)
                                                                    if (newX.absoluteValue < guideThreshold && !showCenterLineV) {
                                                                        showCenterLineV = true
                                                                        vibrateSnap()
                                                                    }
                                                                } else {
                                                                    showCenterLineV = false
                                                                }
                                                                
                                                                if (nearCenterY) {
                                                                    val magnetStrength = 1f - (newY.absoluteValue / guideMagneticRange)
                                                                    newY = newY * (1f - guideSnapForce * magnetStrength)
                                                                    if (newY.absoluteValue < guideThreshold && !showCenterLineH) {
                                                                        showCenterLineH = true
                                                                        vibrateSnap()
                                                                    }
                                                                } else {
                                                                    showCenterLineH = false
                                                                }
                                                            } else {
                                                                showCenterLineV = false
                                                                showCenterLineH = false
                                                            }
                                                            
                                                            // Aplicar movimiento
                                                            gifOffsetX.value = newX
                                                            gifOffsetY.value = newY
                                                            
                                                            // Rotación y zoom: aplicar directamente
                                                            gifRotation.value += rotation
                                                            if (zoom != 1f) {
                                                                gifScale.value = (gifScale.value * zoom).coerceIn(0.3f, 3f)
                                                            }
                                                            
                                                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                                                        } catch (e: Exception) { }
                                                    }
                                                    
                                                    anyPressed = event.changes.any { it.pressed }
                                                }
                                                
                                                // Gesto terminado
                                                isDraggingGif = false
                                                showDeleteZone = false
                                                showCenterLineH = false
                                                showCenterLineV = false
                                                
                                                if (isOverDeleteZone) {
                                                    gifOverlays = gifOverlays.filter { it.id != gifOverlay.id }
                                                    isOverDeleteZone = false
                                                } else {
                                                    gifOverlays = gifOverlays.toMutableList().also {
                                                        if (index < it.size) {
                                                            it[index] = gifOverlay.copy(
                                                                offsetX = gifOffsetX.value,
                                                                offsetY = gifOffsetY.value,
                                                                scale = gifScale.value,
                                                                rotation = gifRotation.value
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                deleteScale.value = 1f
                                                deletingOverlayType = null
                                            }
                                        }
                                ) {
                                    coil.compose.AsyncImage(
                                        model = coil.request.ImageRequest.Builder(context)
                                            .data(gifOverlay.url)
                                            .decoderFactory(
                                                if (Build.VERSION.SDK_INT >= 28) {
                                                    coil.decode.ImageDecoderDecoder.Factory()
                                                } else {
                                                    coil.decode.GifDecoder.Factory()
                                                }
                                            )
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "GIF",
                                        modifier = Modifier.size(150.dp)
                                    )
                                }
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // IMÁGENES SUPERPUESTAS - Con gestos de transformación
                        // Incluye doble tap para centrar y velocidad de paneo normalizada
                        // ---------------------------------------------------------------
                        imageOverlays.sortedBy { it.zIndex }.forEach { imageOverlay ->
                            key(imageOverlay.id) {
                                val imgOffsetX = remember(imageOverlay.id) { mutableStateOf(imageOverlay.offsetX) }
                                val imgOffsetY = remember(imageOverlay.id) { mutableStateOf(imageOverlay.offsetY) }
                                val imgScale = remember(imageOverlay.id) { mutableStateOf(imageOverlay.scale) }
                                val imgRotation = remember(imageOverlay.id) { mutableStateOf(imageOverlay.rotation) }
                                var vibratedDeleteImg by remember { mutableStateOf(false) }
                                
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .graphicsLayer {
                                            // Usar centro como origen de transformación
                                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                                            translationX = imgOffsetX.value
                                            translationY = imgOffsetY.value
                                            rotationZ = imgRotation.value
                                            val currentScale = imgScale.value * (if (deletingOverlayType == "image:${imageOverlay.id}") deleteScale.value else 1f)
                                            scaleX = currentScale
                                            scaleY = currentScale
                                        }
                                        // Doble tap para centrar la imagen
                                        .pointerInput("doubleTap:${imageOverlay.id}") {
                                            detectTapGestures(
                                                onDoubleTap = {
                                                    // Reset a posición central con animación
                                                    imgOffsetX.value = 0f
                                                    imgOffsetY.value = 0f
                                                    imgScale.value = 1f
                                                    imgRotation.value = 0f
                                                    // Actualizar el estado
                                                    imageOverlays = imageOverlays.map {
                                                        if (it.id == imageOverlay.id) it.copy(
                                                            offsetX = 0f,
                                                            offsetY = 0f,
                                                            scale = 1f,
                                                            rotation = 0f
                                                        ) else it
                                                    }
                                                }
                                            )
                                        }
                                        .pointerInput(imageOverlay.id) {
                                            val previewHeightPx = with(density) { previewHeight.toPx() }
                                            val deleteZoneThreshold = with(density) { 50.dp.toPx() }
                                            val deleteZoneStartY = (previewHeightPx / 2f) - deleteZoneThreshold
                                            
                                            awaitEachGesture {
                                                awaitFirstDown(requireUnconsumed = false)
                                                deletingOverlayType = "image:${imageOverlay.id}"
                                                
                                                var anyPressed = true
                                                var hasMovedEnough = false
                                                var totalMovement = 0f
                                                
                                                while (anyPressed) {
                                                    val event = awaitPointerEvent()
                                                    val pressed = event.changes.filter { it.pressed }
                                                    
                                                    if (pressed.isNotEmpty()) {
                                                        try {
                                                            val pan = event.calculatePan()
                                                            val zoom = event.calculateZoom()
                                                            val rotation = event.calculateRotation()
                                                            
                                                            // Normalizar velocidad de paneo dividiendo por la escala actual
                                                            // Esto hace que la velocidad sea consistente sin importar el zoom
                                                            val normalizedPanX = pan.x / imgScale.value
                                                            val normalizedPanY = pan.y / imgScale.value
                                                            
                                                            var newX = imgOffsetX.value + normalizedPanX
                                                            var newY = imgOffsetY.value + normalizedPanY
                                                            
                                                            totalMovement += pan.x.absoluteValue + pan.y.absoluteValue
                                                            if (totalMovement > 30f) hasMovedEnough = true
                                                            
                                                            if (hasMovedEnough && !showDeleteZone) showDeleteZone = true
                                                            
                                                            val isInDeleteZone = newY > deleteZoneStartY && hasMovedEnough
                                                            if (isInDeleteZone) {
                                                                isOverDeleteZone = true
                                                                val proximity = ((newY - deleteZoneStartY) / deleteZoneThreshold).coerceIn(0f, 1f)
                                                                val deleteTargetX = 0f
                                                                val deleteTargetY = (previewHeightPx / 2f) - with(density) { 46.dp.toPx() }
                                                                val magnetForce = proximity * 0.85f
                                                                newX = newX + (deleteTargetX - newX) * magnetForce
                                                                newY = newY + (deleteTargetY - newY) * magnetForce
                                                                deleteScale.value = (1f - proximity * 0.92f).coerceIn(0.08f, 1f)
                                                                if (!vibratedDeleteImg) { vibrateDelete(); vibratedDeleteImg = true }
                                                            } else {
                                                                isOverDeleteZone = false
                                                                deleteScale.value = 1f
                                                                vibratedDeleteImg = false
                                                            }
                                                            
                                                            imgOffsetX.value = newX
                                                            imgOffsetY.value = newY
                                                            imgRotation.value += rotation
                                                            if (zoom != 1f) imgScale.value = (imgScale.value * zoom).coerceIn(0.2f, 3f)
                                                            
                                                            event.changes.forEach { it.consume() }
                                                        } catch (_: Exception) {}
                                                        anyPressed = pressed.isNotEmpty()
                                                    } else anyPressed = false
                                                }
                                                
                                                showDeleteZone = false
                                                showCenterLineH = false
                                                showCenterLineV = false
                                                
                                                if (isOverDeleteZone) {
                                                    imageOverlays = imageOverlays.filter { it.id != imageOverlay.id }
                                                    isOverDeleteZone = false
                                                } else {
                                                    imageOverlays = imageOverlays.map {
                                                        if (it.id == imageOverlay.id) it.copy(
                                                            offsetX = imgOffsetX.value,
                                                            offsetY = imgOffsetY.value,
                                                            scale = imgScale.value,
                                                            rotation = imgRotation.value
                                                        ) else it
                                                    }
                                                }
                                                
                                                deleteScale.value = 1f
                                                deletingOverlayType = null
                                            }
                                        }
                                ) {
                                    coil.compose.AsyncImage(
                                        model = coil.request.ImageRequest.Builder(context)
                                            .data(imageOverlay.uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Imagen superpuesta",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(180.dp)
                                    )
                                }
                            }
                        }
                        
                        // ---------------------------------------------------------------
                        // botón TICK - Esquina inferior derecha del PREVIEW
                        // Mismo Tamaño y color que el Botón Arrow left
                        // ---------------------------------------------------------------
                        AnimatedVisibility(
                            visible = !showFilterCarousel && !showTextEditor && !showDrawingMode && !showAdjustMode,
                            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f),
                            exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.8f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp) // Mismo Tamaño que Arrow left
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)) // Mismo color que Arrow left
                                    .clickable {
                                        isEditingMode = !isEditingMode
                                        if (!isEditingMode) showEducationalText = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isEditingMode) Icons.Filled.Check else Icons.Outlined.Edit,
                                    contentDescription = if (isEditingMode) "Listo" else "Editar",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp) // Mismo Tamaño de icono que Arrow left
                                )
                            }
                        }
                        
                        // Texto educativo "Pulsa cuando termines" - dentro del preview
                        AnimatedVisibility(
                            visible = showEducationalText && isEditingMode && !showFilterCarousel && !showTextEditor && !showDrawingMode && !showAdjustMode,
                            enter = fadeIn(tween(300)) + expandHorizontally(
                                animationSpec = tween(500),
                                expandFrom = Alignment.End
                            ),
                            exit = fadeOut(tween(200)) + shrinkHorizontally(
                                animationSpec = tween(300),
                                shrinkTowards = Alignment.End
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 68.dp, bottom = 20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Pulsa cuando termines ?",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // ---------------------------------------------------------------
                    // CARRUSEL DE FILTROS - Centrado en el preview, sobre el borde inferior
                    // ---------------------------------------------------------------
                    AnimatedVisibility(
                        visible = showFilterCarousel,
                        enter = fadeIn(animationSpec = tween(200)) + slideInVertically { it },
                        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically { it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = previewHeight - 94.dp) // Sobre el borde inferior del preview (bajado 6dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp), // Espacio extra para Títulos
                            contentAlignment = Alignment.Center
                        ) {
                            com.rendly.app.ui.components.FilterCarousel(
                                bitmap = bitmap,
                                currentFilter = selectedFilter,
                                onFilterSelected = { filter -> selectedFilter = filter }
                            )
                        }
                    }
                    
                    // ---------------------------------------------------------------
                    // EDITOR DE TEXTO - Aparece cuando se pulsa herramienta de texto
                    // ---------------------------------------------------------------
                    com.rendly.app.ui.components.StoryTextEditor(
                        visible = showTextEditor,
                        keyboardHeight = keyboardHeight,
                        previewHeight = previewHeight,
                        onTextStateChanged = { newState -> textState = newState },
                        onDismiss = { showTextEditor = false }
                    )
                    
                // Top bar con Botón Listo (cuando hay filtros)
                // Se oculta cuando el modo dibujo o ajustes están activos
                // NOTA: Eliminado el Botón X superior Según requerimiento
                AnimatedVisibility(
                    visible = !showDrawingMode && !showAdjustMode && showFilterCarousel,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(150)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp + previewTopPadding)
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        // botón "Tick" - solo visible cuando se muestran filtros
                        // Mismo estilo que Botón arrow left (fondo negro 0.5 alpha, 40dp)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable {
                                    // Aplicar filtro y cerrar carrusel
                                    showFilterCarousel = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Confirmar",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                
                // ---------------------------------------------------------------
                // ZONA INFERIOR - Carrusel de herramientas o botones de publicar
                // ---------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    // ---------------------------------------------------------------
                    // MODO DIBUJO: Solo carrusel de colores
                    // ---------------------------------------------------------------
                    if (showDrawingMode) {
                        com.rendly.app.ui.components.DrawingColorCarousel(
                            selectedColor = drawingColor,
                            selectedTool = drawingTool,
                            onColorSelected = { color ->
                                drawingColor = color
                                if (drawingTool == com.rendly.app.ui.components.DrawingTool.ERASER) {
                                    drawingTool = com.rendly.app.ui.components.DrawingTool.PEN
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (showAdjustMode) {
                        // ---------------------------------------------------------------
                        // MODO AJUSTES: Carrusel de herramientas de ajuste
                        // ---------------------------------------------------------------
                        com.rendly.app.ui.components.AdjustmentToolsCarousel(
                            adjustState = adjustState,
                            selectedAdjustment = selectedAdjustment,
                            onAdjustmentSelected = { type -> selectedAdjustment = type },
                            onApply = { },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (isEditingMode) {
                        // ---------------------------------------------------------------
                        // MODO EDición: Carrusel horizontal de herramientas (sin Botón a¿Qué)
                        // ---------------------------------------------------------------
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(end = 16.dp) // última herramienta contra borde derecho
                        ) {
                            val editTools = listOf(
                                EditTool("filter", Icons.Outlined.AutoAwesome, "Filtros"),
                                EditTool("text", Icons.Outlined.TextFields, "Texto"),
                                EditTool("gif", Icons.Outlined.Gif, "GIF"),
                                EditTool("image", Icons.Outlined.Image, "Imagen"),
                                EditTool("draw", Icons.Outlined.Draw, "Dibujar"),
                                EditTool("adjust", Icons.Outlined.Tune, "Ajustar")
                            )
                            items(editTools.size) { index ->
                                val tool = editTools[index]
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                            .clickable {
                                                // Cerrar TODAS las herramientas antes de abrir la nueva
                                                showFilterCarousel = false
                                                showTextEditor = false
                                                showGifPicker = false
                                                showImagePicker = false
                                                showDrawingMode = false
                                                showAdjustMode = false
                                                selectedAdjustment = null
                                                
                                                when (tool.id) {
                                                    "filter" -> showFilterCarousel = true
                                                    "text" -> showTextEditor = true
                                                    "gif" -> showGifPicker = true
                                                    "image" -> showImagePicker = true
                                                    "draw" -> showDrawingMode = true
                                                    "adjust" -> {
                                                        // Pre-aplicar filtro al bitmap para evitar diferencia de color
                                                        capturedBitmap?.let { bmp ->
                                                            scope.launch(Dispatchers.Default) {
                                                                val filtered = com.rendly.app.ui.components.FilterProcessor
                                                                    .applyFilterForExport(bmp, selectedFilter)
                                                                withContext(Dispatchers.Main) {
                                                                    filteredBitmapForAdjust = filtered
                                                                    showAdjustMode = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = tool.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Text(
                                        text = tool.label,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        // ---------------------------------------------------------------
                        // MODO PUBLICAR: Botones Tu Vitrina y Frecuentes - ANCHO COMPLETO
                        // ---------------------------------------------------------------
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // botón "Tu Vitrina"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF1E1E2E))
                                    .clickable(enabled = !isPublishing) {
                                        capturedBitmap?.let { bitmap ->
                                            isPublishing = true
                                            // Capturar valores actuales para la coroutine
                                            val currentPreviewWidthPx = previewWidthPx
                                            val currentPreviewHeightPx = previewHeightPx
                                            val currentTextOffsetX = textOffsetX.value
                                            val currentTextOffsetY = textOffsetY.value
                                            val currentTextRotation = textRotation.value
                                            val currentTextScale = textScale.value
                                            val currentTextState = textState.copy()
                                            val currentStrokes = drawingStrokesList.toList()
                                            val currentFilter = selectedFilter
                                            
                                            scope.launch(Dispatchers.Default) {
                                                // ---------------------------------------------------------------
                                                // RENDERIZADO COMPLETO - Filtro + Texto + Dibujos + Ajustes
                                                // ---------------------------------------------------------------
                                                
                                                // 1. Aplicar filtro
                                                var finalBitmap = com.rendly.app.ui.components.FilterProcessor
                                                    .applyFilterForExport(bitmap, currentFilter)
                                                
                                                val scaleX: Float = finalBitmap.width.toFloat() / currentPreviewWidthPx
                                                val scaleY: Float = finalBitmap.height.toFloat() / currentPreviewHeightPx
                                                
                                                // 2. Aplicar dibujos si hay
                                                if (currentStrokes.isNotEmpty()) {
                                                    val canvas = android.graphics.Canvas(finalBitmap)
                                                    
                                                    for (stroke in currentStrokes) {
                                                        if (stroke.points.size < 2) continue
                                                        val paint = android.graphics.Paint().apply {
                                                            isAntiAlias = true
                                                            strokeWidth = stroke.strokeWidth * scaleX
                                                            style = android.graphics.Paint.Style.STROKE
                                                            strokeCap = android.graphics.Paint.Cap.ROUND
                                                            strokeJoin = android.graphics.Paint.Join.ROUND
                                                            if (stroke.tool == com.rendly.app.ui.components.DrawingTool.ERASER) {
                                                                xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                                                            } else {
                                                                color = android.graphics.Color.argb(
                                                                    (stroke.color.alpha * 255).toInt(),
                                                                    (stroke.color.red * 255).toInt(),
                                                                    (stroke.color.green * 255).toInt(),
                                                                    (stroke.color.blue * 255).toInt()
                                                                )
                                                            }
                                                        }
                                                        val path = android.graphics.Path()
                                                        path.moveTo(stroke.points[0].x * scaleX, stroke.points[0].y * scaleY)
                                                        for (i in 1 until stroke.points.size) {
                                                            path.lineTo(stroke.points[i].x * scaleX, stroke.points[i].y * scaleY)
                                                        }
                                                        canvas.drawPath(path, paint)
                                                    }
                                                }
                                                
                                                // 3. Aplicar texto si hay
                                                if (currentTextState.text.isNotEmpty()) {
                                                    val canvas = android.graphics.Canvas(finalBitmap)
                                                    
                                                    val textPaint = android.graphics.Paint().apply {
                                                        isAntiAlias = true
                                                        textSize = currentTextState.fontSize * scaleX * 2.5f
                                                        color = android.graphics.Color.argb(
                                                            (currentTextState.color.alpha * 255).toInt(),
                                                            (currentTextState.color.red * 255).toInt(),
                                                            (currentTextState.color.green * 255).toInt(),
                                                            (currentTextState.color.blue * 255).toInt()
                                                        )
                                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                                        textAlign = when (currentTextState.alignment) {
                                                            com.rendly.app.ui.components.TextAlignOption.LEFT -> android.graphics.Paint.Align.LEFT
                                                            com.rendly.app.ui.components.TextAlignOption.CENTER -> android.graphics.Paint.Align.CENTER
                                                            com.rendly.app.ui.components.TextAlignOption.RIGHT -> android.graphics.Paint.Align.RIGHT
                                                        }
                                                    }
                                                    
                                                    val centerX = finalBitmap.width / 2f + (currentTextOffsetX * scaleX)
                                                    val centerY = finalBitmap.height / 2f + (currentTextOffsetY * scaleY)
                                                    
                                                    canvas.save()
                                                    canvas.translate(centerX, centerY)
                                                    canvas.rotate(currentTextRotation)
                                                    canvas.scale(currentTextScale, currentTextScale)
                                                    
                                                    // Fondo del texto si aplica
                                                    if (currentTextState.backgroundState != com.rendly.app.ui.components.TextBackgroundState.NONE) {
                                                        val textBounds = android.graphics.Rect()
                                                        textPaint.getTextBounds(currentTextState.text, 0, currentTextState.text.length, textBounds)
                                                        val padding = 20f * scaleX
                                                        val bgPaint = android.graphics.Paint().apply {
                                                            isAntiAlias = true
                                                            color = when (currentTextState.backgroundState) {
                                                                com.rendly.app.ui.components.TextBackgroundState.BLACK -> android.graphics.Color.argb(220, 0, 0, 0)
                                                                com.rendly.app.ui.components.TextBackgroundState.WHITE -> android.graphics.Color.WHITE
                                                                else -> android.graphics.Color.TRANSPARENT
                                                            }
                                                        }
                                                        if (currentTextState.backgroundState == com.rendly.app.ui.components.TextBackgroundState.WHITE) {
                                                            textPaint.color = android.graphics.Color.BLACK
                                                        }
                                                        val bgRect = android.graphics.RectF(
                                                            (-textBounds.width() / 2f - padding).toFloat(),
                                                            (-textBounds.height() / 2f - padding).toFloat(),
                                                            (textBounds.width() / 2f + padding).toFloat(),
                                                            (textBounds.height() / 2f + padding).toFloat()
                                                        )
                                                        canvas.drawRoundRect(bgRect, 16f, 16f, bgPaint)
                                                    }
                                                    
                                                    canvas.drawText(currentTextState.text, 0f, textPaint.textSize / 3f, textPaint)
                                                    canvas.restore()
                                                }
                                                
                                                withContext(Dispatchers.Main) {
                                                    StoryUploadService.uploadStoryWithNotification(
                                                        context = context,
                                                        bitmap = finalBitmap,
                                                        onComplete = { },
                                                        onError = { }
                                                    )
                                                }
                                                kotlinx.coroutines.delay(1500)
                                                withContext(Dispatchers.Main) {
                                                    isPublishing = false
                                                    onClose()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isPublishing) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF818CF8),
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Subiendo...",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color(0xFF818CF8)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Storefront,
                                            contentDescription = null,
                                            tint = Color(0xFF818CF8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tu Vitrina",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                            
                            // botón "Frecuentes"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF2D2D3A))
                                    .clickable(enabled = !isPublishing) { },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Groups,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Frecuentes",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    
                }
                }
                
                // ---------------------------------------------------------------
                // MODAL DE GIF PICKER - Al final para que quede sobre todo
                // ---------------------------------------------------------------
                com.rendly.app.ui.components.GifPickerModal(
                    visible = showGifPicker,
                    onDismiss = { showGifPicker = false },
                    onGifSelected = { gifUrl ->
                        // Agregar GIF a la lista de overlays
                        gifOverlays = gifOverlays + GifOverlay(url = gifUrl)
                    }
                )
                
                // ---------------------------------------------------------------
                // OVERLAY DE DIBUJO - Modo de dibujo completo
                // ---------------------------------------------------------------
                com.rendly.app.ui.components.DrawingOverlay(
                    visible = showDrawingMode,
                    selectedColor = drawingColor,
                    selectedTool = drawingTool,
                    strokeWidth = drawingStrokeWidth.value,
                    strokes = drawingStrokesList,
                    onToolChange = { tool -> drawingTool = tool },
                    onUndo = {
                        if (drawingStrokesList.isNotEmpty()) {
                            drawingStrokesList.removeAt(drawingStrokesList.lastIndex)
                            drawingStrokes = drawingStrokesList.toList()
                        }
                    },
                    onApply = { showDrawingMode = false },
                    onStrokesChanged = { strokes -> drawingStrokes = strokes },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(previewHeight)
                        .padding(top = previewTopPadding, start = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(previewCornerRadius))
                        .align(Alignment.TopCenter)
                )
                
                // ---------------------------------------------------------------
                // SLIDER VERTICAL DE GROSOR - Super centrado verticalmente en el preview
                // ---------------------------------------------------------------
                AnimatedVisibility(
                    visible = showDrawingMode,
                    enter = fadeIn(tween(200)) + slideInHorizontally { -it },
                    exit = fadeOut(tween(150)) + slideOutHorizontally { -it },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        // Sin offset - Alignment.CenterStart ya lo centra verticalmente en el preview
                ) {
                    com.rendly.app.ui.components.DrawingStrokeSliderVertical(
                        strokeWidth = drawingStrokeWidth.value,
                        selectedColor = drawingColor,
                        onStrokeWidthChange = { drawingStrokeWidth.value = it }
                    )
                }
                
            }
        }
    }
}

// Data class para GIF overlays
data class GifOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var scale: Float = 1f,
    var rotation: Float = 0f
)

// Data class para Imagenes superpuestas
data class ImageOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: android.net.Uri,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var scale: Float = 0.5f, // Escala inicial Más pequeña
    var rotation: Float = 0f,
    var zIndex: Int = 0 // Para controlar el orden de las capas
)

// ---------------------------------------------------------------
// COLUMNA VERTICAL DE HERRAMIENTAS DE EDición - Sin background
// ---------------------------------------------------------------
@Composable
private fun StoryEditToolsCarousel(
    bitmap: Bitmap?,
    selectedFilter: com.rendly.app.ui.components.ImageFilter? = null,
    textState: com.rendly.app.ui.components.StoryTextState = com.rendly.app.ui.components.StoryTextState(),
    textOffsetX: Float = 0f,
    textOffsetY: Float = 0f,
    textRotation: Float = 0f,
    textScale: Float = 1f,
    previewWidth: Int = 0,
    previewHeight: Int = 0,
    onSaveSuccess: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onTextClick: () -> Unit = {},
    onGifClick: () -> Unit = {},
    onDrawClick: () -> Unit = {},
    onAdjustClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var saveProgress by remember { mutableStateOf(0f) }
    
    // Animación de progreso vertical
    val animatedProgress by animateFloatAsState(
        targetValue = saveProgress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "saveProgress"
    )
    
    // Función para renderizar bitmap final con filtro y texto
    fun renderFinalBitmap(sourceBitmap: Bitmap): Bitmap {
        // Aplicar filtro primero
        val filteredBitmap = if (selectedFilter?.colorMatrix != null) {
            val result = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = android.graphics.Canvas(result)
            val paint = android.graphics.Paint().apply {
                colorFilter = android.graphics.ColorMatrixColorFilter(selectedFilter.colorMatrix)
            }
            canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)
            result
        } else {
            sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }
        
        // Si no hay texto, retornar solo con filtro
        if (textState.text.isEmpty()) return filteredBitmap
        
        // Renderizar texto sobre la imagen
        val canvas = android.graphics.Canvas(filteredBitmap)
        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = textState.fontSize * (filteredBitmap.width.toFloat() / previewWidth.coerceAtLeast(1))
            color = android.graphics.Color.argb(
                (textState.color.alpha * 255).toInt(),
                (textState.color.red * 255).toInt(),
                (textState.color.green * 255).toInt(),
                (textState.color.blue * 255).toInt()
            )
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = when (textState.alignment) {
                com.rendly.app.ui.components.TextAlignOption.LEFT -> android.graphics.Paint.Align.LEFT
                com.rendly.app.ui.components.TextAlignOption.CENTER -> android.graphics.Paint.Align.CENTER
                com.rendly.app.ui.components.TextAlignOption.RIGHT -> android.graphics.Paint.Align.RIGHT
            }
        }
        
        // Calcular posición del texto escalada al Tamaño del bitmap
        val scaleX = filteredBitmap.width.toFloat() / previewWidth.coerceAtLeast(1)
        val scaleY = filteredBitmap.height.toFloat() / previewHeight.coerceAtLeast(1)
        val centerX = filteredBitmap.width / 2f + (textOffsetX * scaleX)
        val centerY = filteredBitmap.height / 2f + (textOffsetY * scaleY)
        
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(textRotation)
        canvas.scale(textScale, textScale)
        
        // Dibujar fondo del texto si es necesario
        if (textState.backgroundState != com.rendly.app.ui.components.TextBackgroundState.NONE) {
            val textBounds = android.graphics.Rect()
            textPaint.getTextBounds(textState.text, 0, textState.text.length, textBounds)
            val padding = 20f * scaleX
            val bgPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                color = when (textState.backgroundState) {
                    com.rendly.app.ui.components.TextBackgroundState.BLACK -> android.graphics.Color.argb(220, 0, 0, 0)
                    com.rendly.app.ui.components.TextBackgroundState.WHITE -> android.graphics.Color.WHITE
                    else -> android.graphics.Color.TRANSPARENT
                }
            }
            
            // Cambiar color del texto si fondo es blanco
            if (textState.backgroundState == com.rendly.app.ui.components.TextBackgroundState.WHITE) {
                textPaint.color = android.graphics.Color.BLACK
            }
            
            val bgRect = android.graphics.RectF(
                -textBounds.width() / 2f - padding,
                -textBounds.height() / 2f - padding,
                textBounds.width() / 2f + padding,
                textBounds.height() / 2f + padding
            )
            canvas.drawRoundRect(bgRect, 16f, 16f, bgPaint)
        }
        
        // Dibujar texto
        canvas.drawText(textState.text, 0f, textPaint.textSize / 3f, textPaint)
        canvas.restore()
        
        return filteredBitmap
    }
    
    // Función para guardar imagen en galería (optimizada con coroutine)
    fun saveToGallery() {
        if (bitmap == null || isSaving) return
        isSaving = true
        saveProgress = 0.3f
        
        scope.launch(Dispatchers.IO) {
            try {
                // Renderizar imagen final con filtro y texto
                val finalBitmap = renderFinalBitmap(bitmap)
                
                withContext(Dispatchers.Main) { saveProgress = 0.6f }
                
                val filename = "Merqora_${System.currentTimeMillis()}.jpg"
                val contentValues = android.content.ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Merqora")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                withContext(Dispatchers.Main) { saveProgress = 0.85f }
                
                uri?.let { imageUri ->
                    context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(imageUri, contentValues, null, null)
                    }
                    
                    // Liberar bitmap si no es el original
                    if (finalBitmap != bitmap) {
                        finalBitmap.recycle()
                    }
                    
                    withContext(Dispatchers.Main) {
                        saveProgress = 1f
                        onSaveSuccess()
                        android.widget.Toast.makeText(context, "? Imagen guardada", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("StoryEditTools", "Error guardando imagen: ${e.message}")
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Error al guardar", android.widget.Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    // pequeño delay para mostrar animación completa
                    kotlinx.coroutines.delay(200)
                    isSaving = false
                    saveProgress = 0f
                }
            }
        }
    }
    
    // Columna simple sin background
    Column(
        modifier = modifier.width(52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        STORY_EDIT_TOOLS.forEach { tool ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { 
                        when (tool.id) {
                            "save" -> saveToGallery()
                            "filter" -> onFilterClick()
                            "text" -> onTextClick()
                            "gif" -> onGifClick()
                            "draw" -> onDrawClick()
                            "adjust" -> onAdjustClick()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (tool.id == "save") {
                    // ---------------------------------------------------------------
                    // ANIMación DE CARGA VERTICAL tipo "agua"
                    // El fondo blanco sube de abajo hacia arriba
                    // El icono cambia de blanco a negro mientras sube
                    // ---------------------------------------------------------------
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Fondo blanco que sube (efecto agua)
                        if (isSaving) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedProgress)
                                    .align(Alignment.BottomCenter)
                                    .background(Color.White)
                            )
                        }
                        
                        // Icono que cambia de color
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = tool.label,
                            tint = if (isSaving && animatedProgress > 0.5f) Color.Black else Color.White,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.Center)
                        )
                    }
                } else {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.label,
                        tint = Color.White,
                        modifier = Modifier.size(if (tool.id == "gif") 28.dp else 22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    lensFacing: Int,
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val previewView = remember { 
        PreviewView(context).apply {
            // Configurar clipping estricto en el View nativo
            clipToOutline = true
            clipChildren = true
            clipToPadding = true
            setBackgroundColor(android.graphics.Color.BLACK)
            // Modo de escala que respeta los límites
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("HistoryScreen", "Camera binding failed", e)
        }
    }
    
    // Contenedor con clip estricto para evitar fugas visuales
    Box(
        modifier = modifier
            .background(Color.Black)
            .clipToBounds()
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        )
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider {
    return suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener({
                continuation.resume(future.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }
}

// Función de captura rápidA - optimizada para Mínima latencia
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun takePhotoToMemoryFast(
    context: Context,
    imageCapture: ImageCapture,
    onSuccess: (Bitmap) -> Unit,
    onError: (Exception) -> Unit
) {
    // Captura directa sin Configuración adicional para Máxima velocidad
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val bitmap = image.toBitmap()
                    val rotationDegrees = image.imageInfo.rotationDegrees
                    val correctedBitmap = if (rotationDegrees != 0) {
                        val matrix = Matrix().apply {
                            postRotate(rotationDegrees.toFloat())
                        }
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    } else {
                        bitmap
                    }
                    onSuccess(correctedBitmap)
                } catch (e: Exception) {
                    onError(e)
                } finally {
                    image.close()
                }
            }
            
            override fun onError(exception: ImageCaptureException) {
                Log.e("HistoryScreen", "Photo capture failed", exception)
                onError(exception)
            }
        }
    )
}

// Función para cargar bitmap desde URI de galería
private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.e("HistoryScreen", "Error loading bitmap from URI", e)
            null
        }
    }
}
