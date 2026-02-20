package com.rendly.app.ui.screens.publish

import android.Manifest
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rendly.app.data.repository.PostRepository
import com.rendly.app.ui.components.GalleryFilterModal
import com.rendly.app.ui.components.GalleryFilterState
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset

// State machine para el flujo de publicación
sealed class PublicationStep {
    object Gallery : PublicationStep()
    object Preview : PublicationStep()
    object Details : PublicationStep() // Nueva pantalla final
    object Publishing : PublicationStep()
}

// Aspect ratio para el preview de imagen (como Instagram)
enum class PreviewAspectRatio {
    PORTRAIT,   // 4:5 - Más alto que ancho (ideal para fotos verticales)
    LANDSCAPE   // 1.91:1 - Más ancho que alto (ideal para fotos horizontales)
}

// Modelo para imágenes de galería
data class GalleryImage(
    val id: Long,
    val uri: Uri,
    val dateAdded: Long
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PublicationScreen(
    onClose: () -> Unit,
    onNavigateToHome: () -> Unit,
    onModeSelected: (Int) -> Unit = {},
    currentModeIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State machine
    var currentStep by remember { mutableStateOf<PublicationStep>(PublicationStep.Gallery) }
    
    // Galería
    var galleryImages by remember { mutableStateOf<List<GalleryImage>>(emptyList()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Aspect ratio del preview (se detecta automáticamente pero puede cambiarse)
    var previewAspectRatio by remember { mutableStateOf(PreviewAspectRatio.PORTRAIT) }
    
    // Auto-detectar aspect ratio cuando cambia la imagen
    LaunchedEffect(selectedBitmap) {
        selectedBitmap?.let { bmp ->
            // Si la imagen es más ancha que alta, usar landscape; sino portrait
            previewAspectRatio = if (bmp.width > bmp.height) {
                PreviewAspectRatio.LANDSCAPE
            } else {
                PreviewAspectRatio.PORTRAIT
            }
        }
    }
    
    // Bitmap con filtro aplicado (persiste entre pantallas)
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Publishing state
    var isPublishing by remember { mutableStateOf(false) }
    var publishingProgress by remember { mutableStateOf(0f) }
    var caption by remember { mutableStateOf("") }
    
    // Permiso de almacenamiento
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionState = rememberPermissionState(storagePermission)
    
    // Cargar galería al obtener permiso
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            galleryImages = loadGalleryImages(context)
            // Seleccionar primera imagen por defecto
            galleryImages.firstOrNull()?.let { firstImage ->
                selectedImageUri = firstImage.uri
                selectedBitmap = loadBitmapFromUri(context, firstImage.uri)
            }
        }
    }
    
    // Solicitar permiso al iniciar
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                when {
                    targetState is PublicationStep.Preview && initialState is PublicationStep.Gallery -> {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    }
                    targetState is PublicationStep.Gallery && initialState is PublicationStep.Preview -> {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                    else -> fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                }
            },
            label = "publication_step"
        ) { step ->
            when (step) {
                is PublicationStep.Gallery -> {
                    GallerySelectionContent(
                        galleryImages = galleryImages,
                        selectedImageUri = selectedImageUri,
                        selectedBitmap = selectedBitmap,
                        aspectRatio = previewAspectRatio,
                        onAspectRatioToggle = {
                            previewAspectRatio = if (previewAspectRatio == PreviewAspectRatio.PORTRAIT) {
                                PreviewAspectRatio.LANDSCAPE
                            } else {
                                PreviewAspectRatio.PORTRAIT
                            }
                        },
                        onImageSelected = { uri ->
                            selectedImageUri = uri
                            scope.launch {
                                selectedBitmap = loadBitmapFromUri(context, uri)
                            }
                        },
                        onNextClick = {
                            if (selectedImageUri != null) {
                                currentStep = PublicationStep.Preview
                            }
                        },
                        onClose = onClose,
                        hasPermission = permissionState.status.isGranted,
                        onRequestPermission = { permissionState.launchPermissionRequest() },
                        currentModeIndex = currentModeIndex,
                        onModeSelected = onModeSelected,
                        onMultiImagesSelected = { uris ->
                            selectedImageUris = uris
                            if (uris.isNotEmpty()) {
                                selectedImageUri = uris.first()
                                scope.launch {
                                    selectedBitmap = loadBitmapFromUri(context, uris.first())
                                }
                            }
                        }
                    )
                }
                
                is PublicationStep.Preview -> {
                    PreviewConfirmContent(
                        selectedBitmap = selectedBitmap,
                        selectedUri = selectedImageUri,
                        selectedUris = selectedImageUris,
                        aspectRatio = previewAspectRatio,
                        onBackClick = {
                            currentStep = PublicationStep.Gallery
                        },
                        onNextClick = { finalBitmap ->
                            // Guardar el bitmap editado para usar en la siguiente pantalla
                            editedBitmap = finalBitmap
                            currentStep = PublicationStep.Details
                        }
                    )
                }
                
                is PublicationStep.Details -> {
                    FinalPublishContent(
                        selectedUri = selectedImageUri,
                        selectedUris = selectedImageUris,
                        editedBitmap = editedBitmap, // Pasar el bitmap editado con filtro
                        caption = caption,
                        onCaptionChange = { caption = it },
                        isPublishing = isPublishing,
                        onBackClick = {
                            currentStep = PublicationStep.Preview
                        },
                        onPublishClick = { productTitle, productPrice, productCondition, productCategory, allowOffers, freeShipping ->
                            isPublishing = true
                            scope.launch {
                                kotlinx.coroutines.delay(1500)
                                scope.launch {
                                    try {
                                        // Usar el bitmap editado si existe, sino cargar de URI
                                        val bitmapsToUpload = if (editedBitmap != null && selectedImageUris.size <= 1) {
                                            // Si hay bitmap editado y es una sola imagen, usar el editado
                                            listOf(editedBitmap!!)
                                        } else if (selectedImageUris.isNotEmpty()) {
                                            selectedImageUris.mapNotNull { uri ->
                                                loadBitmapFromUri(context, uri)
                                            }
                                        } else {
                                            // Fallback: usar el bitmap único si no hay múltiples
                                            listOfNotNull(editedBitmap ?: selectedBitmap)
                                        }
                                        
                                        if (bitmapsToUpload.isNotEmpty()) {
                                            val priceValue = productPrice.toDoubleOrNull()
                                            PostRepository.createPost(
                                                bitmaps = bitmapsToUpload,
                                                caption = caption.ifEmpty { null },
                                                title = productTitle.ifEmpty { null },
                                                price = priceValue,
                                                condition = productCondition.ifEmpty { null },
                                                category = productCategory.ifEmpty { null },
                                                allowOffers = allowOffers,
                                                freeShipping = freeShipping
                                            )
                                            Log.d("PublicationScreen", "Post creado con ${bitmapsToUpload.size} imágenes, precio=$priceValue, titulo=$productTitle")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("PublicationScreen", "Error: ${e.message}", e)
                                    }
                                }
                                onNavigateToHome()
                            }
                        }
                    )
                }
                
                is PublicationStep.Publishing -> {
                    PublishingContent(
                        progress = publishingProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun GallerySelectionContent(
    galleryImages: List<GalleryImage>,
    selectedImageUri: Uri?,
    selectedBitmap: Bitmap?,
    aspectRatio: PreviewAspectRatio,
    onAspectRatioToggle: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onNextClick: () -> Unit,
    onClose: () -> Unit,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    currentModeIndex: Int = 0,
    onModeSelected: (Int) -> Unit = {},
    onMultiImagesSelected: (List<Uri>) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    
    // Calcular dimensiones del preview según aspect ratio
    // PORTRAIT: 4:5 (más alto que ancho) - Instagram style  
    // LANDSCAPE: 4:3 (más ancho que alto) - Perfecto para home feed
    val previewWidth = screenWidth - 32.dp // Padding horizontal
    val targetHeight = when (aspectRatio) {
        PreviewAspectRatio.PORTRAIT -> minOf(previewWidth * 1.25f, screenHeight * 0.5f) // 4:5 ratio
        PreviewAspectRatio.LANDSCAPE -> previewWidth * 0.75f // 4:3 ratio (mejor para feed)
    }
    // Animación fluida del cambio de altura
    val previewHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "previewHeight"
    )
    
    // Filter modal state
    var showFilterModal by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(GalleryFilterState()) }
    
    // Multi-select state
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf(setOf<Uri>()) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar fijo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HomeBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OverlayMedium)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                
                Text(
                    text = "Nueva publicación",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(
                    onClick = onNextClick,
                    enabled = selectedImageUri != null
                ) {
                    Text(
                        text = "Siguiente",
                        color = if (selectedImageUri != null) PrimaryPurple else TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = if (selectedImageUri != null) PrimaryPurple else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            if (!hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Permiso de galería requerido",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("Permitir acceso")
                        }
                    }
                }
            } else {
                // Todo es parte de un solo scroll - Preview se empuja arriba
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
                ) {
                    // Preview de imagen (se empuja hacia arriba al hacer scroll)
                    item(key = "preview") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .requiredHeight(previewHeight)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A1A2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                // Imagen que SIEMPRE llena todo el preview (Crop)
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(selectedImageUri)
                                        .crossfade(200)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop, // SIEMPRE llena todo el espacio
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                // Botón de toggle aspect ratio - esquina inferior izquierda
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        .clickable { onAspectRatioToggle() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (aspectRatio == PreviewAspectRatio.PORTRAIT) {
                                            Icons.Outlined.CropPortrait
                                        } else {
                                            Icons.Outlined.CropLandscape
                                        },
                                        contentDescription = "Cambiar proporción",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Selecciona una imagen",
                                    color = TextMuted,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    // Selector de álbumes - STICKY debajo del header
                    stickyHeader(key = "album_selector") {
                        AlbumSelector(
                            albumName = filterState.selectedAlbum,
                            onAlbumClick = { showFilterModal = true },
                            onMultiSelectClick = { 
                                isMultiSelectMode = !isMultiSelectMode
                                if (!isMultiSelectMode) {
                                    selectedImages = emptySet()
                                }
                            },
                            isMultiSelectMode = isMultiSelectMode
                        )
                    }
                    
                    // Grilla de galería optimizada - como items individuales
                    val chunkedImages = galleryImages.chunked(4)
                    items(
                        count = chunkedImages.size,
                        key = { "row_$it" }
                    ) { rowIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            chunkedImages[rowIndex].forEach { image ->
                                GalleryThumbnail(
                                    image = image,
                                    isSelected = if (isMultiSelectMode) image.uri in selectedImages else image.uri == selectedImageUri,
                                    isMultiSelectMode = isMultiSelectMode,
                                    selectionIndex = if (isMultiSelectMode) selectedImages.indexOf(image.uri).takeIf { it >= 0 }?.plus(1) else null,
                                    onClick = { 
                                        if (isMultiSelectMode) {
                                            val newSelectedImages = if (image.uri in selectedImages) {
                                                selectedImages - image.uri
                                            } else {
                                                selectedImages + image.uri
                                            }
                                            selectedImages = newSelectedImages
                                            // Update the multi-images list
                                            onMultiImagesSelected(newSelectedImages.toList())
                                            // Also update the main selection for preview
                                            if (newSelectedImages.isNotEmpty()) {
                                                onImageSelected(newSelectedImages.last())
                                            }
                                        } else {
                                            onImageSelected(image.uri)
                                            onMultiImagesSelected(listOf(image.uri))
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Rellenar espacios vacíos si la fila no está completa
                            repeat(4 - chunkedImages[rowIndex].size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    
                    // Espacio para el carrusel flotante
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
        
        // Carrusel de modos profesional flotante
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            com.rendly.app.ui.components.ModeCarousel(
                currentIndex = currentModeIndex,
                onModeSelected = onModeSelected,
                style = com.rendly.app.ui.components.CarouselStyle.FLOATING_RIGHT
            )
        }
        
        // Gallery filter modal
        GalleryFilterModal(
            isVisible = showFilterModal,
            currentFilter = filterState,
            onDismiss = { showFilterModal = false },
            onFilterChange = { newFilter ->
                filterState = newFilter
            }
        )
    }
}

@Composable
private fun GalleryThumbnail(
    image: GalleryImage,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMultiSelectMode: Boolean = false,
    selectionIndex: Int? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.92f else 1f,
        animationSpec = tween(100),
        label = "thumbnail_scale"
    )
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = PrimaryPurple,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else Modifier
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.uri)
                .crossfade(true)
                .size(200) // Thumbnail size
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Indicador de selección - círculo en esquina superior derecha
        if (isMultiSelectMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) PrimaryPurple else Color.Black.copy(alpha = 0.4f)
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected && selectionIndex != null) {
                    Text(
                        text = selectionIndex.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Selector de álbumes/tipos - entre Preview y Galería
@Composable
private fun AlbumSelector(
    albumName: String = "Recientes",
    onAlbumClick: () -> Unit,
    onMultiSelectClick: () -> Unit,
    isMultiSelectMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de álbum
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAlbumClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = albumName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Seleccionar álbum",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Botón selección múltiple
        IconButton(
            onClick = onMultiSelectClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isMultiSelectMode) PrimaryPurple.copy(alpha = 0.2f) else Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Selección múltiple",
                tint = if (isMultiSelectMode) PrimaryPurple else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreviewConfirmContent(
    selectedBitmap: Bitmap?,
    selectedUri: Uri?,
    selectedUris: List<Uri> = emptyList(),
    aspectRatio: PreviewAspectRatio = PreviewAspectRatio.PORTRAIT,
    onBackClick: () -> Unit,
    onNextClick: (Bitmap?) -> Unit // Ahora pasa el bitmap editado
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // ═══════════════════════════════════════════════════════════════
    // ESTADOS DE HERRAMIENTAS DE EDICIÓN
    // ═══════════════════════════════════════════════════════════════
    var showFilterCarousel by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(com.rendly.app.ui.components.STORY_FILTERS.first()) }
    var showAdjustMode by remember { mutableStateOf(false) }
    var adjustState by remember { mutableStateOf(com.rendly.app.ui.components.ImageAdjustState()) }
    var filteredBitmapForAdjust by remember { mutableStateOf<Bitmap?>(null) }
    var selectedAdjustment by remember { mutableStateOf<com.rendly.app.ui.components.AdjustmentType?>(null) }
    val gpuAdjustState by remember(adjustState) {
        derivedStateOf { adjustState.toGPUState() }
    }
    
    // Estado para mostrar la grilla al interactuar
    var isInteractingWithImage by remember { mutableStateOf(false) }
    
    // Calcular dimensiones del preview según aspect ratio (como Instagram)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val previewWidth = screenWidth - 32.dp // Padding horizontal
    val normalPreviewHeight = when (aspectRatio) {
        PreviewAspectRatio.PORTRAIT -> minOf(previewWidth * 1.25f, screenHeight * 0.5f) // 4:5 ratio
        PreviewAspectRatio.LANDSCAPE -> previewWidth * 0.75f // 4:3 ratio (mejor para feed)
    }
    // En modo Ajustar, expandir preview para mostrar imagen completa
    val targetPreviewHeight = if (showAdjustMode) screenHeight * 0.62f else normalPreviewHeight
    // Animación fluida del cambio de altura
    val maxPreviewHeight by animateDpAsState(
        targetValue = targetPreviewHeight,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "previewHeight"
    )
    val previewCornerRadius = 12.dp
    
    // Estado para el offset de posición de la imagen (para herramienta Posición)
    // Usamos Animatable para animaciones fluidas de retorno
    val imageOffsetXAnimatable = remember { androidx.compose.animation.core.Animatable(0f) }
    val imageOffsetYAnimatable = remember { androidx.compose.animation.core.Animatable(0f) }
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    // Escala de la imagen para zoom (inicia en 1.0 = tamaño original que llena el preview)
    var imageScale by remember { mutableStateOf(1f) }
    
    // Dimensiones para calcular límites de paneo
    var containerWidthPx by remember { mutableStateOf(0f) }
    var containerHeightPx by remember { mutableStateOf(0f) }
    var imageWidthPx by remember { mutableStateOf(0f) }
    var imageHeightPx by remember { mutableStateOf(0f) }
    var baseCoverScaleState by remember { mutableStateOf(1f) }
    
    // Función para calcular y clampar offsets dentro de los límites
    fun clampOffsets(newOffsetX: Float, newOffsetY: Float, scale: Float): Pair<Float, Float> {
        if (containerWidthPx <= 0 || containerHeightPx <= 0 || imageWidthPx <= 0 || imageHeightPx <= 0) {
            return Pair(newOffsetX, newOffsetY)
        }
        
        // Calcular dimensiones escaladas de la imagen
        val finalScale = baseCoverScaleState * scale
        val scaledImageW = imageWidthPx * finalScale
        val scaledImageH = imageHeightPx * finalScale
        
        // Calcular límites máximos de paneo
        // El límite es la cantidad que la imagen sobresale del contenedor por cada lado
        val maxOffsetX = maxOf(0f, (scaledImageW - containerWidthPx) / 2f)
        val maxOffsetY = maxOf(0f, (scaledImageH - containerHeightPx) / 2f)
        
        // Clampar los offsets para que los bordes de la imagen nunca salgan del preview
        val clampedX = newOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
        val clampedY = newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
        
        return Pair(clampedX, clampedY)
    }
    
    
    // Bitmap con filtro aplicado para preview en tiempo real
    var displayBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Actualizar displayBitmap cuando cambie el filtro o el bitmap original
    LaunchedEffect(selectedBitmap, selectedFilter) {
        selectedBitmap?.let { bmp ->
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                val filtered = com.rendly.app.ui.components.FilterProcessor
                    .applyFilterForExport(bmp, selectedFilter)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    displayBitmap = filtered
                }
            }
        }
    }
    
    // Herramientas de edición para posts (optimizadas para e-commerce)
    data class EditTool(val id: String, val icon: ImageVector, val label: String, val isLocked: Boolean = false)
    val editTools = listOf(
        EditTool("filter", Icons.Outlined.AutoAwesome, "Filtros"),
        EditTool("adjust", Icons.Outlined.Tune, "Ajustar"),
        EditTool("crop", Icons.Outlined.Crop, "Recortar", isLocked = true),
        EditTool("rotate", Icons.Outlined.RotateRight, "Rotar", isLocked = true),
        EditTool("text", Icons.Outlined.TextFields, "Texto", isLocked = true),
        EditTool("stickers", Icons.Outlined.EmojiEmotions, "Stickers", isLocked = true),
        EditTool("draw", Icons.Outlined.Brush, "Dibujar", isLocked = true),
        EditTool("blur", Icons.Outlined.BlurOn, "Desenfocar", isLocked = true)
    )
    
    // Determinar si hay alguna herramienta activa
    val isAnyToolActive = showFilterCarousel || showAdjustMode
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar - SIEMPRE VISIBLE, solo cambia el contenido
            // Altura FIJA de 56dp para ambos steps (evita salto visual)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isAnyToolActive) {
                    // Contenido normal del header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OverlayMedium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Editar",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    TextButton(
                        onClick = { onNextClick(displayBitmap) },
                        enabled = selectedUri != null || selectedUris.isNotEmpty()
                    ) {
                        Text(
                            text = "Siguiente",
                            color = if (selectedUri != null || selectedUris.isNotEmpty()) PrimaryPurple else TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = if (selectedUri != null || selectedUris.isNotEmpty()) PrimaryPurple else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (showAdjustMode) {
                    // Header de modo Ajustar - Cancel / Título / Tick(aplicar)
                    TextButton(
                        onClick = {
                            adjustState = com.rendly.app.ui.components.ImageAdjustState()
                            selectedAdjustment = null
                            showAdjustMode = false
                        }
                    ) {
                        Text("Cancelar", color = TextMuted, fontSize = 14.sp)
                    }
                    Text(
                        text = "Ajustar",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Botón tick para aplicar ajustes
                    var isApplyingAdjust by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            if (adjustState.hasChanges() && !isApplyingAdjust) {
                                isApplyingAdjust = true
                                scope.launch {
                                    val adjusted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                        (filteredBitmapForAdjust ?: selectedBitmap)?.let { bmp ->
                                            com.rendly.app.ui.components.ImageAdjustProcessor.applyForExport(bmp, adjustState)
                                        }
                                    }
                                    adjusted?.let { displayBitmap = it }
                                    adjustState = com.rendly.app.ui.components.ImageAdjustState()
                                    selectedAdjustment = null
                                    isApplyingAdjust = false
                                    showAdjustMode = false
                                }
                            } else {
                                selectedAdjustment = null
                                showAdjustMode = false
                            }
                        },
                        enabled = !isApplyingAdjust,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.15f))
                    ) {
                        if (isApplyingAdjust) {
                            CircularProgressIndicator(
                                color = PrimaryPurple,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Aplicar ajustes",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    // Header de herramienta activa (Filtros)
                    TextButton(
                        onClick = {
                            showFilterCarousel = false
                        }
                    ) {
                        Text("Cancelar", color = TextMuted, fontSize = 14.sp)
                    }
                    Text(
                        text = if (showFilterCarousel) "Filtros" else "",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            showFilterCarousel = false
                        }
                    ) {
                        Text("Listo", color = PrimaryPurple, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // PREVIEW DE IMAGEN - Instagram-style con pan/zoom siempre activo
            // La imagen cubre todo el preview (Crop), las partes ocultas se
            // revelan arrastrando. Bordes de imagen SIEMPRE pegados al preview.
            // ═══════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(maxPreviewHeight)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(previewCornerRadius))
                    .background(Color(0xFF1A1A2E)),
                contentAlignment = Alignment.Center
            ) {
                val imagesToShow = selectedUris.ifEmpty { listOfNotNull(selectedUri) }
                
                if (showAdjustMode) {
                    // ═══════════════════════════════════════════════════════════════
                    // MODO AJUSTAR - Imagen completa con ajustes GPU en tiempo real
                    // Muestra la imagen entera (Fit) para ver los cambios claramente
                    // ═══════════════════════════════════════════════════════════════
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (filteredBitmapForAdjust != null) {
                            com.rendly.app.gpu.GPUAdjustedImage(
                                bitmap = filteredBitmapForAdjust,
                                adjustments = gpuAdjustState,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            displayBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Preview ajuste",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                } else if (imagesToShow.size > 1) {
                    val pagerState = rememberPagerState(pageCount = { imagesToShow.size })
                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationX = imageOffsetX
                                        translationY = imageOffsetY
                                        scaleX = imageScale
                                        scaleY = imageScale
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imagesToShow[page])
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Preview ${page + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        
                        // Indicadores de página
                        if (!isAnyToolActive) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .height(10.dp)
                            ) {
                                repeat(imagesToShow.size) { index ->
                                    val isActive = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isActive) PrimaryPurple 
                                                else Color.White.copy(alpha = 0.4f)
                                            )
                                            .then(
                                                if (isActive) Modifier.border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                                else Modifier
                                            )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ═══════════════════════════════════════════════════════════════
                    // IMAGEN ÚNICA - Instagram-style: Cover + pan/zoom siempre activo
                    // ═══════════════════════════════════════════════════════════════
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        // Doble tap: centrar imagen y resetear zoom con animación
                                        scope.launch {
                                            imageOffsetXAnimatable.snapTo(imageOffsetX)
                                            imageOffsetXAnimatable.animateTo(
                                                targetValue = 0f,
                                                animationSpec = androidx.compose.animation.core.spring(
                                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                                )
                                            ) { imageOffsetX = value }
                                        }
                                        scope.launch {
                                            imageOffsetYAnimatable.snapTo(imageOffsetY)
                                            imageOffsetYAnimatable.animateTo(
                                                targetValue = 0f,
                                                animationSpec = androidx.compose.animation.core.spring(
                                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                                )
                                            ) { imageOffsetY = value }
                                        }
                                        imageScale = 1f
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    isInteractingWithImage = true
                                    do {
                                        val event = awaitPointerEvent()
                                        val pan = event.calculatePan()
                                        val zoom = event.calculateZoom()
                                        // Zoom: clamp entre 1x y 3x
                                        val newScale = (imageScale * zoom).coerceIn(1f, 3f)
                                        imageScale = newScale
                                        // Pan: aplicar y clampar
                                        val newOffsetX = imageOffsetX + pan.x
                                        val newOffsetY = imageOffsetY + pan.y
                                        val (cx, cy) = clampOffsets(newOffsetX, newOffsetY, newScale)
                                        imageOffsetX = cx
                                        imageOffsetY = cy
                                        event.changes.forEach { it.consume() }
                                    } while (event.changes.any { it.pressed })
                                    isInteractingWithImage = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val containerWidth = maxWidth
                        val containerHeight = maxHeight
                        
                        // Actualizar dimensiones del contenedor en px
                        LaunchedEffect(containerWidth, containerHeight) {
                            with(density) {
                                containerWidthPx = containerWidth.toPx()
                                containerHeightPx = containerHeight.toPx()
                            }
                        }
                        
                        // Renderizar imagen con escala cover + offset
                        displayBitmap?.let { bmp ->
                            val imgWidth = bmp.width.toFloat()
                            val imgHeight = bmp.height.toFloat()
                            
                            LaunchedEffect(bmp) {
                                imageWidthPx = imgWidth
                                imageHeightPx = imgHeight
                            }
                            
                            val containerW = with(density) { containerWidth.toPx() }
                            val containerH = with(density) { containerHeight.toPx() }
                            
                            // Escala base: cubrir TODO el preview (comportamiento Crop)
                            val scaleToFillWidth = containerW / imgWidth
                            val scaleToFillHeight = containerH / imgHeight
                            val baseCoverScale = maxOf(scaleToFillWidth, scaleToFillHeight)
                            
                            LaunchedEffect(baseCoverScale) {
                                baseCoverScaleState = baseCoverScale
                            }
                            
                            val finalScale = baseCoverScale * imageScale
                            val (clampedX, clampedY) = clampOffsets(imageOffsetX, imageOffsetY, imageScale)
                            val scaledWidth = with(density) { (imgWidth * finalScale).toDp() }
                            val scaledHeight = with(density) { (imgHeight * finalScale).toDp() }
                            
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Preview con filtro",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .requiredSize(scaledWidth, scaledHeight)
                                    .graphicsLayer {
                                        translationX = clampedX
                                        translationY = clampedY
                                    }
                            )
                        } ?: selectedUri?.let { uri ->
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = imageScale
                                        scaleY = imageScale
                                        translationX = imageOffsetX
                                        translationY = imageOffsetY
                                    }
                            )
                        }
                        
                        // Grilla 3x3 Instagram - aparece al interactuar o en modo posición
                        val gridAlpha by animateFloatAsState(
                            targetValue = if (isInteractingWithImage) 0.6f else 0f,
                            animationSpec = tween(if (isInteractingWithImage) 100 else 300),
                            label = "grid_alpha"
                        )
                        if (gridAlpha > 0f) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 0.8f.dp.toPx()
                                val gridColor = Color.White.copy(alpha = gridAlpha)
                                val thirdW = size.width / 3
                                val thirdH = size.height / 3
                                // Líneas verticales
                                drawLine(gridColor, Offset(thirdW, 0f), Offset(thirdW, size.height), strokeWidth)
                                drawLine(gridColor, Offset(thirdW * 2, 0f), Offset(thirdW * 2, size.height), strokeWidth)
                                // Líneas horizontales
                                drawLine(gridColor, Offset(0f, thirdH), Offset(size.width, thirdH), strokeWidth)
                                drawLine(gridColor, Offset(0f, thirdH * 2), Offset(size.width, thirdH * 2), strokeWidth)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(if (showAdjustMode && selectedAdjustment != null) 8.dp else 16.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // SLIDER DE AJUSTE - DEBAJO del preview, visible solo cuando hay
            // una herramienta de ajuste seleccionada
            // ═══════════════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = showAdjustMode && selectedAdjustment != null,
                enter = fadeIn(tween(150)) + expandVertically(tween(200)),
                exit = fadeOut(tween(100)) + shrinkVertically(tween(150))
            ) {
                selectedAdjustment?.let { selected ->
                    val currentValue = adjustState.getValue(selected)
                    val displayValue = when (selected) {
                        com.rendly.app.ui.components.AdjustmentType.EXPOSURE -> String.format("%+.1f EV", currentValue)
                        com.rendly.app.ui.components.AdjustmentType.GRAIN -> String.format("%.0f%%", currentValue * 100)
                        else -> String.format("%+.0f", currentValue * 100)
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge con valor actual
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = displayValue,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Slider profesional
                        Slider(
                            value = currentValue,
                            onValueChange = { value ->
                                val newState = adjustState.withValue(selected, value)
                                adjustState = newState
                            },
                            valueRange = selected.min..selected.max,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        
                        // Reset link
                        AnimatedVisibility(
                            visible = currentValue != selected.default,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "Toca para restablecer",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clickable {
                                        adjustState = adjustState.withValue(selected, selected.default)
                                    }
                            )
                        }
                    }
                }
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // HERRAMIENTAS DE EDICIÓN - Condicional: AdjustTools o Carrusel principal
        // ═══════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            AnimatedContent(
                targetState = showAdjustMode,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically { it / 3 }) togetherWith
                    (fadeOut(tween(150)) + slideOutVertically { -it / 3 })
                },
                label = "tools_switch"
            ) { isAdjustMode ->
                if (isAdjustMode) {
                    // ═══════════════════════════════════════════════════════════
                    // MODO AJUSTAR: Carrusel de herramientas de ajuste
                    // Reemplaza las herramientas principales con transición fluida
                    // ═══════════════════════════════════════════════════════════
                    com.rendly.app.ui.components.AdjustmentToolsCarousel(
                        adjustState = adjustState,
                        selectedAdjustment = selectedAdjustment,
                        onAdjustmentSelected = { type -> selectedAdjustment = type },
                        onApply = { },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // ═══════════════════════════════════════════════════════════
                    // HERRAMIENTAS PRINCIPALES: Filtros, Posición, Ajustar, etc.
                    // ═══════════════════════════════════════════════════════════
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(editTools.size) { index ->
                            val tool = editTools[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable {
                                    if (tool.isLocked) return@clickable
                                    
                                    showFilterCarousel = false
                                    showAdjustMode = false
                                    selectedAdjustment = null
                                    
                                    when (tool.id) {
                                        "filter" -> showFilterCarousel = true
                                        "adjust" -> {
                                            selectedBitmap?.let { bmp ->
                                                scope.launch(kotlinx.coroutines.Dispatchers.Default) {
                                                    val filtered = com.rendly.app.ui.components.FilterProcessor
                                                        .applyFilterForExport(bmp, selectedFilter)
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        filteredBitmapForAdjust = filtered
                                                        showAdjustMode = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier.size(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = if (tool.isLocked) 0.2f else 0.4f))
                                            .border(1.dp, Color.White.copy(alpha = if (tool.isLocked) 0.1f else 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = tool.label,
                                            tint = Color.White.copy(alpha = if (tool.isLocked) 0.4f else 1f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    if (tool.isLocked) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(Surface)
                                                .border(1.dp, HomeBg, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Lock,
                                                contentDescription = "Próximamente",
                                                tint = TextMuted,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = tool.label,
                                    color = if (tool.isLocked) TextMuted else Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // CARRUSEL DE FILTROS - Justo debajo de la imagen
        // ═══════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showFilterCarousel,
            enter = fadeIn(tween(200)) + slideInVertically { -it },
            exit = fadeOut(tween(150)) + slideOutVertically { -it },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = maxPreviewHeight + 85.dp) // Debajo del header + preview + 5dp extra
        ) {
            // Carrusel de filtros debajo de la imagen
            selectedBitmap?.let { bitmap ->
                com.rendly.app.ui.components.FilterCarousel(
                    bitmap = bitmap,
                    currentFilter = selectedFilter,
                    onFilterSelected = { filter -> selectedFilter = filter }
                )
            }
        }
        
        // ImageAdjustOverlay removido - slider ahora está inline debajo del preview
    }
}

// ============================================================================
// PANTALLA FINAL DE PUBLICACIÓN - Estilo Ecommerce Creativo
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FinalPublishContent(
    selectedUri: Uri?,
    selectedUris: List<Uri> = emptyList(),
    editedBitmap: Bitmap? = null, // Bitmap con filtro aplicado
    caption: String,
    onCaptionChange: (String) -> Unit,
    isPublishing: Boolean,
    onBackClick: () -> Unit,
    onPublishClick: (title: String, price: String, condition: String, category: String, allowOffers: Boolean, freeShipping: Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    var productTitle by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productCategory by remember { mutableStateOf("") }
    var productCondition by remember { mutableStateOf("Nuevo") }
    var allowOffers by remember { mutableStateOf(true) }
    var freeShipping by remember { mutableStateOf(false) }
    
    // Nuevos campos de producto
    var availability by remember { mutableStateOf("Disponible") }
    var warranty by remember { mutableStateOf("Sin garantía") }
    var acceptedPayments by remember { mutableStateOf(setOf("Efectivo", "Transferencia")) }
    var maxInstallments by remember { mutableStateOf(0) }
    var hasReturn by remember { mutableStateOf(false) }
    
    // ═══════════════════════════════════════════════════════════════
    // PALETA DE COLORES ECOMMERCE PROFESIONAL
    // ═══════════════════════════════════════════════════════════════
    val EcommercePrimary = Color(0xFF48586f)    // Azul grisáceo (principal)
    val EcommerceAccent = Color(0xFFd6c496)     // Dorado/beige (acentos)
    val EcommerceHighlight = Color(0xFFffffc0)  // Amarillo crema (highlights)
    val EcommerceAlert = Color(0xFFd62e2e)      // Rojo (alertas/ofertas)
    val EcommerceDark = Color(0xFF283d3e)       // Verde oscuro (fondos oscuros)
    
    // Altura fija del preview (igual que step 2 - 400.dp)
    val previewHeight = 400.dp
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {
            // Top bar con botón volver y botón publicar
            // Altura FIJA de 56dp igual que step 1 (evita salto visual)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    enabled = !isPublishing,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OverlayMedium)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Nuevo producto",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                // Botón Publicar en esquina superior derecha
                Button(
                    onClick = { onPublishClick(productTitle, productPrice, productCondition, productCategory, allowOffers, freeShipping) },
                    enabled = !isPublishing && productTitle.isNotBlank(),
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcommercePrimary,
                        disabledContainerColor = EcommercePrimary.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Publicar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // ========== IMAGEN CON DIMENSIÓN AJUSTABLE ==========
            val imagesToShow = selectedUris.ifEmpty { listOfNotNull(selectedUri) }
            
            if (imagesToShow.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp) // Mismo padding que step 2
                        .height(previewHeight) // Misma altura que step 2
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                ) {
                    if (imagesToShow.size > 1) {
                        // Multi-image carousel
                        val pagerState = rememberPagerState(pageCount = { imagesToShow.size })
                        
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imagesToShow[page])
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Preview ${page + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Page indicators at bottom
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(imagesToShow.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == index) Color.White 
                                            else Color.White.copy(alpha = 0.5f)
                                        )
                                )
                            }
                        }
                    } else {
                        // Single image - usar bitmap editado si existe (con filtro aplicado)
                        if (editedBitmap != null) {
                            Image(
                                bitmap = editedBitmap.asImageBitmap(),
                                contentDescription = "Preview con filtro",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imagesToShow.first())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    
                    // Badge de foto (esquina superior derecha)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${imagesToShow.size} foto${if (imagesToShow.size > 1) "s" else ""}", 
                                color = Color.White, 
                                fontSize = 11.sp
                            )
                        }
                    }
                    
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ========== DESCRIPCIÓN (sin título, sin borde) ==========
            TextField(
                value = caption,
                onValueChange = onCaptionChange,
                placeholder = { Text("Describe tu producto de forma atractiva...", color = TextMuted, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = EcommerceDark.copy(alpha = 0.15f),
                    unfocusedContainerColor = EcommerceDark.copy(alpha = 0.1f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = EcommerceAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ========== INFORMACIÓN DEL PRODUCTO ==========
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Inventory, null, tint = EcommerceAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Información del producto",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Título del producto
            OutlinedTextField(
                value = productTitle,
                onValueChange = { productTitle = it },
                placeholder = { Text("Título del producto", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.ShoppingBag, null, tint = EcommerceAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcommerceAccent,
                    unfocusedBorderColor = EcommerceDark.copy(alpha = 0.3f),
                    focusedContainerColor = EcommerceDark.copy(alpha = 0.1f),
                    unfocusedContainerColor = EcommerceDark.copy(alpha = 0.05f),
                    cursorColor = EcommerceAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Precio
            OutlinedTextField(
                value = productPrice,
                onValueChange = { productPrice = it },
                placeholder = { Text("Precio (ej: 150.00)", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Text("$", color = EcommerceAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcommerceAccent,
                    unfocusedBorderColor = EcommerceDark.copy(alpha = 0.3f),
                    focusedContainerColor = EcommerceDark.copy(alpha = 0.1f),
                    unfocusedContainerColor = EcommerceDark.copy(alpha = 0.05f),
                    cursorColor = EcommerceAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Categoría
            OutlinedTextField(
                value = productCategory,
                onValueChange = { productCategory = it },
                placeholder = { Text("Categoría (ej: Ropa, Tech, Hogar)", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Category, null, tint = EcommerceAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcommerceAccent,
                    unfocusedBorderColor = EcommerceDark.copy(alpha = 0.3f),
                    focusedContainerColor = EcommerceDark.copy(alpha = 0.1f),
                    unfocusedContainerColor = EcommerceDark.copy(alpha = 0.05f),
                    cursorColor = EcommerceAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Divider(color = Surface, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // TARJETA: ESTADO DEL PRODUCTO
            // ═══════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                // Header de la tarjeta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EcommercePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory2, 
                            null, 
                            tint = EcommercePrimary, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Estado del producto",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Condición - Grid 2x2
                Text(
                    text = "Condición",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Primera fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Nuevo", "Como nuevo").forEach { condition ->
                        val isSelected = productCondition == condition
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EcommercePrimary else HomeBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) EcommercePrimary else Surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { productCondition = condition }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = condition,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Segunda fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Buen estado", "Usado").forEach { condition ->
                        val isSelected = productCondition == condition
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EcommercePrimary else HomeBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) EcommercePrimary else Surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { productCondition = condition }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = condition,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Surface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Disponibilidad
                Text(
                    text = "Disponibilidad",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Disponible" to Icons.Outlined.CheckCircle,
                        "Por encargo" to Icons.Outlined.Schedule,
                        "Último" to Icons.Outlined.LocalFireDepartment
                    ).forEach { (option, icon) ->
                        val isSelected = availability == option || (option == "Último" && availability == "Último disponible")
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) EcommercePrimary.copy(alpha = 0.15f) else HomeBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) EcommercePrimary else Surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    availability = if (option == "Último") "Último disponible" else option 
                                }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                icon,
                                null,
                                tint = if (isSelected) EcommercePrimary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = option,
                                color = if (isSelected) EcommercePrimary else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // TARJETA: GARANTÍA Y PAGOS
            // ═══════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Verified, 
                            null, 
                            tint = AccentPink, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Garantía y pagos",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Garantía - chips horizontales con scroll
                Text(
                    text = "Período de garantía",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Sin garantía", "7 días", "15 días", "30 días", "90 días", "1 año").forEach { option ->
                        val isSelected = warranty == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AccentPink else HomeBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AccentPink else Surface,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { warranty = option }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Surface, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Métodos de pago - iconos con checkboxes visuales
                Text(
                    text = "Métodos de pago aceptados",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Grid de métodos de pago
                val paymentMethods = listOf(
                    Triple("Efectivo", Icons.Outlined.Money, "cash"),
                    Triple("Transferencia", Icons.Outlined.AccountBalance, "transfer"),
                    Triple("Débito", Icons.Outlined.CreditCard, "debit"),
                    Triple("Crédito", Icons.Outlined.CreditScore, "credit"),
                    Triple("PayPal", Icons.Outlined.Wallet, "paypal")
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentMethods.forEach { (name, icon, _) ->
                        val isSelected = name in acceptedPayments || 
                            (name == "Débito" && "Tarjeta débito" in acceptedPayments) ||
                            (name == "Crédito" && "Tarjeta crédito" in acceptedPayments)
                        
                        val fullName = when(name) {
                            "Débito" -> "Tarjeta débito"
                            "Crédito" -> "Tarjeta crédito"
                            else -> name
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentPink.copy(alpha = 0.1f) else HomeBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AccentPink else Surface,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    acceptedPayments = if (isSelected) {
                                        acceptedPayments - fullName
                                    } else {
                                        acceptedPayments + fullName
                                    }
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                icon,
                                null,
                                tint = if (isSelected) AccentPink else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = name,
                                color = if (isSelected) AccentPink else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // Cuotas sin interés (solo si acepta tarjeta crédito)
                AnimatedVisibility(visible = "Tarjeta crédito" in acceptedPayments) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cuotas sin interés",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0, 3, 6, 12).forEach { num ->
                                    val isSelected = maxInstallments == num
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentPink else HomeBg)
                                            .clickable { maxInstallments = num }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (num == 0) "No" else "${num}x",
                                            color = if (isSelected) Color.White else TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // TARJETA: BENEFICIOS
            // ═══════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Stars, 
                            null, 
                            tint = PrimaryPurple, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Beneficios",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Beneficios como switches elegantes
                BenefitToggleRow(
                    icon = Icons.Default.LocalShipping,
                    iconBgColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    iconTint = Color(0xFF4CAF50),
                    title = "Envío gratis",
                    subtitle = "Sin costo adicional de envío",
                    isEnabled = freeShipping,
                    onToggle = { freeShipping = it }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BenefitToggleRow(
                    icon = Icons.Default.LocalOffer,
                    iconBgColor = Color(0xFFFF9800).copy(alpha = 0.15f),
                    iconTint = Color(0xFFFF9800),
                    title = "Acepta ofertas",
                    subtitle = "Negociación de precio",
                    isEnabled = allowOffers,
                    onToggle = { allowOffers = it }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BenefitToggleRow(
                    icon = Icons.Default.Autorenew,
                    iconBgColor = Color(0xFF2196F3).copy(alpha = 0.15f),
                    iconTint = Color(0xFF2196F3),
                    title = "Devolución gratis",
                    subtitle = "30 días sin costo",
                    isEnabled = hasReturn,
                    onToggle = { hasReturn = it }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Componente de toggle para beneficios con diseño elegante
@Composable
private fun BenefitToggleRow(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isEnabled) iconBgColor.copy(alpha = 0.3f) else HomeBg)
            .clickable { onToggle(!isEnabled) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iconTint,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Surface
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

// Componente de opción de engagement
@Composable
private fun EngagementOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Componente de opción de publicación
@Composable
private fun PublishOption(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Componente de opción con toggle
@Composable
private fun ToggleOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    initialValue: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var isEnabled by remember { mutableStateOf(initialValue) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                isEnabled = !isEnabled
                onToggle(isEnabled)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        
        Switch(
            checked = isEnabled,
            onCheckedChange = { 
                isEnabled = it
                onToggle(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryPurple,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Surface
            )
        )
    }
}

@Composable
private fun PublishingContent(
    progress: Float
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                progress = progress,
                color = PrimaryPurple,
                modifier = Modifier.size(64.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Publicando...",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

// Funciones de utilidad para cargar galería
private suspend fun loadGalleryImages(context: android.content.Context): List<GalleryImage> {
    return withContext(Dispatchers.IO) {
        val images = mutableListOf<GalleryImage>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        
        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                
                while (cursor.moveToNext() && images.size < 500) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded = cursor.getLong(dateColumn)
                    val contentUri = ContentUris.withAppendedId(uri, id)
                    
                    images.add(GalleryImage(id, contentUri, dateAdded))
                }
            }
        } catch (e: Exception) {
            Log.e("PublicationScreen", "Error loading gallery", e)
        }
        
        images
    }
}

private suspend fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("PublicationScreen", "Error loading bitmap", e)
            null
        }
    }
}

// Componente de encabezado de sección
@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Componente de chip seleccionable
@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    multiSelect: Boolean = false
) {
    val backgroundColor = if (isSelected) PrimaryPurple else Surface
    val contentColor = if (isSelected) Color.White else TextMuted
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (multiSelect && isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
