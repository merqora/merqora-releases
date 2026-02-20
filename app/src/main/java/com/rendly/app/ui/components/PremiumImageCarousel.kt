package com.rendly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import androidx.compose.ui.platform.LocalContext
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Optimiza URLs de Cloudinary para máximo rendimiento
 * Aplica: width, quality auto, format auto
 */
private fun optimizeCloudinaryUrl(url: String, width: Int = 800): String {
    if (!url.contains("cloudinary.com")) return url
    
    // Cloudinary transformation: w_800,q_auto,f_auto
    val transformation = "w_$width,q_auto,f_auto"
    
    return if (url.contains("/upload/")) {
        url.replace("/upload/", "/upload/$transformation/")
    } else {
        url
    }
}

/**
 * PremiumImageCarousel - Carrusel de imágenes optimizado estilo Instagram
 * 
 * Características:
 * - Snap instantáneo con física natural
 * - Detección inteligente de intención del gesto
 * - Animaciones en GPU (graphicsLayer)
 * - Umbral de velocidad bajo para cambio rápido
 * - Pre-carga de imágenes adyacentes
 * - Sin conflicto con scroll vertical del feed
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    onImageTap: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    showIndicators: Boolean = true,
    showCounter: Boolean = true
) {
    if (images.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { images.size })
    val scope = rememberCoroutineScope()
    
    // Configuración de fling optimizada para swipes cortos y rápidos
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        snapVelocityThreshold = 80.dp // Umbral bajo para respuesta rápida
    )
    
    Box(modifier = modifier) {
        val context = LocalContext.current
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = flingBehavior,
            beyondBoundsPageCount = 1,
            key = { images.getOrNull(it) ?: it }
        ) { page ->
            // OPTIMIZADO: Sin animaciones de parallax/escala para mejor rendimiento
            val optimizedUrl = remember(images[page]) { optimizeCloudinaryUrl(images[page]) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { onDoubleTap() },
                            onTap = { onImageTap() }
                        )
                    }
            ) {
                AsyncImage(
                    model = remember(optimizedUrl) {
                        ImageRequest.Builder(context)
                            .data(optimizedUrl)
                            .crossfade(100)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .size(1080, 1350) // OPTIMIZADO: Limitar decodificación
                            .build()
                    },
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // Indicadores de página (dots) - OPTIMIZADO sin animaciones
        if (showIndicators && images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                images.forEachIndexed { index, _ ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) IconAccentBlue
                                else IconAccentBlue.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
        
        // Contador de imágenes
        if (showCounter && images.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${images.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Versión simplificada para uso en PostItem con callbacks existentes
 * 
 * OPTIMIZACIÓN CRÍTICA: Este carrusel consume eventos horizontales para evitar
 * conflictos con el HorizontalPager principal de navegación entre secciones.
 * Cuando hay múltiples imágenes, el carrusel "roba" los gestos horizontales.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostImageCarousel(
    images: List<String>,
    contentDescription: String,
    onTap: () -> Unit,
    onDoubleTap: (androidx.compose.ui.geometry.Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty() || images[0].isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { images.size })
    
    // Fling behavior ultra-responsive
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        snapVelocityThreshold = 80.dp
    )
    
    // SIMPLIFICADO: El HorizontalPager maneja swipes directamente
    // Solo necesitamos asegurar que userScrollEnabled = true (default)
    Box(
        modifier = modifier
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = flingBehavior,
            beyondBoundsPageCount = 1,
            key = { images.getOrNull(it) ?: it }
        ) { page ->
            val context = LocalContext.current
            val optimizedUrl = remember(images[page]) { optimizeCloudinaryUrl(images[page]) }
            
            // OPTIMIZADO: Memoizar ImageRequest con SIZE CONSTRAINT crítico
            val imageRequest = remember(optimizedUrl) {
                ImageRequest.Builder(context)
                    .data(optimizedUrl)
                    .crossfade(150)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .size(1080, 1350)
                    .build()
            }
            
            // combinedClickable coopera con el scroll del HorizontalPager
            // permitiendo swipe desde cualquier parte de la imagen
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTap() },
                        onDoubleClick = { onDoubleTap(Offset.Zero) }
                    ),
                contentScale = ContentScale.Crop
            )
        }
        
        // Dots
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                images.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) IconAccentBlue
                                else IconAccentBlue.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            
            // Counter
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.65f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${images.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
