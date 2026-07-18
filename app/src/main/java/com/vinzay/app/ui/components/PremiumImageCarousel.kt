package com.vinzay.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
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
import com.vinzay.app.ui.theme.*
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
    
    // Configuración de fling optimizada para swipes cortos y rápidos
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    Box(modifier = modifier) {
        val context = LocalContext.current
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = flingBehavior,
            beyondViewportPageCount = 1,
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
                            // Sin .size() fijo: Coil decodifica al tamaño MEDIDO del
                            // composable (menos memoria y decode que 1080x1350 fijo)
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
 * PostImageCarousel V2 - TAP INSTANTÁNEO sin delay
 * 
 * OPTIMIZACIÓN CRÍTICA: 
 * - Se eliminó combinedClickable con onDoubleClick que causaba delay de 300-500ms
 *   en el onClick (esperando a determinar si era doble tap)
 * - Ahora usa clickable + pointerInput separados para respuesta táctil INMEDIATA
 * - Sin conflicto con HorizontalPager: los taps se registran AL INSTANTE
 * - Doble tap redirigido solo a onDoubleTap sin bloquear el single tap
 *
 * @param images Lista de URLs de imágenes
 * @param contentDescription Descripción para accesibilidad
 * @param onTap Callback que se dispara INSTANTÁNEAMENTE al pulsar (sin esperar doble tap)
 * @param onDoubleTap Callback para doble tap (se dispara ADEMÁS del single tap)
 * @param modifier Modificador
 */
@Suppress("DEPRECATION")
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
    
    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    Box(
        modifier = modifier
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = flingBehavior,
            beyondViewportPageCount = 1,
            key = { images.getOrNull(it) ?: it }
        ) { page ->
            val context = LocalContext.current
            val optimizedUrl = remember(images[page]) { optimizeCloudinaryUrl(images[page]) }
            
            val imageRequest = remember(optimizedUrl) {
                ImageRequest.Builder(context)
                    .data(optimizedUrl)
                    .crossfade(100)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    // Sin .size() fijo: Coil decodifica al tamaño MEDIDO del
                    // composable (menos memoria y decode que 1080x1350 fijo)
                    .build()
            }
            
            // CUSTOM GESTURE DETECTOR: Tap instantáneo + doble tap
            // No usa clickable ni detectTapGestures para evitar conflictos con HorizontalPager
            // onTap se dispara INMEDIATAMENTE al levantar el dedo (sin esperar doble tap timeout)
            // Si llega un segundo tap dentro de 300ms, dispara ADEMÁS onDoubleTap (like)
            var lastTapTimeMs by remember { mutableLongStateOf(0L) }
            
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val firstDown = awaitFirstDown()
                            val firstUp = waitForUpOrCancellation()
                            if (firstUp != null) {
                                val now = System.currentTimeMillis()
                                if (now - lastTapTimeMs < 300L && lastTapTimeMs != 0L) {
                                    // DOUBLE TAP: dispara like, onTap ya se disparó en el primer tap
                                    lastTapTimeMs = 0L
                                    onDoubleTap(Offset(firstDown.position.x, firstDown.position.y))
                                } else {
                                    // SINGLE TAP: dispara INMEDIATAMENTE (navegación a ProductPage)
                                    lastTapTimeMs = now
                                    onTap()
                                }
                            }
                        }
                    },
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
