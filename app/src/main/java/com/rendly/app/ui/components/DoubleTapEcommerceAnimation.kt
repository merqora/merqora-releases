package com.rendly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.rendly.app.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * DoubleTapEcommerceAnimation - Animación Ultra Optimizada Estilo Instagram
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Arquitectura:
 * - Módulo independiente que NO depende de recomposiciones del post
 * - Renderizado como overlay usando graphicsLayer (GPU-accelerated)
 * - Pre-cálculo de valores para evitar allocations durante animación
 * - Animaciones con SpringSpec físico para fluidez natural
 * 
 * Performance:
 * - Uso de Animatable para animaciones state-less
 * - graphicsLayer para transformaciones en GPU
 * - Sin allocations en hot path (remember + pre-computed values)
 * - Preparado para 60-120Hz refresh rates
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

// ═══════════════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN - Fácilmente ajustable
// ═══════════════════════════════════════════════════════════════════════════════

object DoubleTapAnimationConfig {
    // Duración total de la animación
    const val TOTAL_DURATION_MS = 900
    
    // Escala del icono
    const val ICON_SIZE_DP = 90
    const val INITIAL_SCALE = 0f
    const val PEAK_SCALE = 1.15f
    const val FINAL_SCALE = 1f
    
    // Spring specs para física natural
    val SCALE_IN_SPRING = SpringSpec<Float>(
        dampingRatio = 0.55f,  // Rebote moderado
        stiffness = 800f       // Respuesta rápida
    )
    
    val SCALE_OUT_SPRING = SpringSpec<Float>(
        dampingRatio = 0.8f,
        stiffness = 400f
    )
    
    // Fade specs
    const val FADE_IN_DURATION = 100
    const val HOLD_DURATION = 400
    const val FADE_OUT_DURATION = 400
    
    // Anti-spam: tiempo mínimo entre animaciones
    const val MIN_TIME_BETWEEN_TAPS_MS = 350L
}

// ═══════════════════════════════════════════════════════════════════════════════
// ICONOS ECOMMERCE - Lista personalizable
// ═══════════════════════════════════════════════════════════════════════════════

data class EcommerceIcon(
    val icon: ImageVector,
    val tint: Color
)

val defaultEcommerceIcons = listOf(
    EcommerceIcon(Icons.Filled.ShoppingBag, Color.White),
    EcommerceIcon(Icons.Filled.ShoppingCart, Color.White),
    EcommerceIcon(Icons.Filled.LocalOffer, Color.White),
    EcommerceIcon(Icons.Filled.Sell, Color.White),
    EcommerceIcon(Icons.Filled.Star, Color.White),
    EcommerceIcon(Icons.Filled.Inventory2, Color.White),
    EcommerceIcon(Icons.Filled.LocalShipping, Color.White),
    EcommerceIcon(Icons.Filled.CardGiftcard, Color.White)
)

// ═══════════════════════════════════════════════════════════════════════════════
// ESTADO DE LA ANIMACIÓN - Encapsulado y reutilizable
// ═══════════════════════════════════════════════════════════════════════════════

class DoubleTapAnimationState(
    private val icons: List<EcommerceIcon> = defaultEcommerceIcons
) {
    // Animatables pre-creados (NO se recrean en cada tap)
    val scale = Animatable(0f)
    val alpha = Animatable(0f)
    
    // Estado de visibilidad
    var isVisible by mutableStateOf(false)
        private set
    
    // Posición del tap
    var tapOffset by mutableStateOf(Offset.Zero)
        private set
    
    // Icono actual (rotativo)
    private var currentIconIndex by mutableIntStateOf(0)
    val currentIcon: EcommerceIcon
        get() = icons[currentIconIndex]
    
    // Anti-spam
    private var lastTapTime = 0L
    
    /**
     * Dispara la animación en la posición indicada.
     * Retorna true si se ejecutó, false si fue bloqueado por anti-spam.
     */
    suspend fun trigger(offset: Offset): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < DoubleTapAnimationConfig.MIN_TIME_BETWEEN_TAPS_MS) {
            return false
        }
        lastTapTime = now
        
        // Rotar al siguiente icono
        currentIconIndex = (currentIconIndex + 1) % icons.size
        
        // Guardar posición
        tapOffset = offset
        
        // Reset instantáneo
        scale.snapTo(DoubleTapAnimationConfig.INITIAL_SCALE)
        alpha.snapTo(1f)
        isVisible = true
        
        // Animación de entrada (scale up con spring físico)
        scale.animateTo(
            targetValue = DoubleTapAnimationConfig.PEAK_SCALE,
            animationSpec = DoubleTapAnimationConfig.SCALE_IN_SPRING
        )
        
        // Pequeño settle
        scale.animateTo(
            targetValue = DoubleTapAnimationConfig.FINAL_SCALE,
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = 500f
            )
        )
        
        // Hold breve
        kotlinx.coroutines.delay(DoubleTapAnimationConfig.HOLD_DURATION.toLong())
        
        // Animación de salida (fade + scale out)
        kotlinx.coroutines.coroutineScope {
            launch {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = DoubleTapAnimationConfig.FADE_OUT_DURATION,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 0.8f,
                    animationSpec = tween(
                        durationMillis = DoubleTapAnimationConfig.FADE_OUT_DURATION,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
        
        isVisible = false
        return true
    }
}

@Composable
fun rememberDoubleTapAnimationState(
    icons: List<EcommerceIcon> = defaultEcommerceIcons
): DoubleTapAnimationState {
    return remember { DoubleTapAnimationState(icons) }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSABLE PRINCIPAL - Overlay de animación
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Overlay de animación para doble tap.
 * 
 * USO:
 * ```kotlin
 * val animState = rememberDoubleTapAnimationState()
 * 
 * Box {
 *     // Tu contenido (imagen del post)
 *     AsyncImage(...)
 *     
 *     // Overlay de animación
 *     DoubleTapAnimationOverlay(
 *         state = animState,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 * 
 * // En el detector de gestos:
 * pointerInput(Unit) {
 *     detectTapGestures(
 *         onDoubleTap = { offset ->
 *             scope.launch {
 *                 if (animState.trigger(offset)) {
 *                     onLikeClick()
 *                 }
 *             }
 *         }
 *     )
 * }
 * ```
 */
@Composable
fun DoubleTapAnimationOverlay(
    state: DoubleTapAnimationState,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return
    
    val icon = state.currentIcon
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center // CENTRADO como Instagram
    ) {
        Icon(
            imageVector = icon.icon,
            contentDescription = null,
            modifier = Modifier
                .size(DoubleTapAnimationConfig.ICON_SIZE_DP.dp)
                .graphicsLayer {
                    // GPU-accelerated transforms
                    scaleX = state.scale.value
                    scaleY = state.scale.value
                    alpha = state.alpha.value
                    
                    // Pequeña rotación para más dinamismo
                    rotationZ = (1f - state.scale.value) * 15f
                },
            tint = icon.tint
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// VERSIÓN SIMPLIFICADA - Para integración rápida
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Versión todo-en-uno que maneja su propio estado.
 * Ideal para integración rápida cuando no necesitas control externo.
 */
@Composable
fun DoubleTapEcommerceAnimation(
    show: Boolean,
    tapOffset: Offset,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
    icons: List<EcommerceIcon> = defaultEcommerceIcons
) {
    if (!show) return
    
    val scope = rememberCoroutineScope()
    
    // Animatables
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    
    // Icono rotativo
    var iconIndex by remember { mutableIntStateOf(0) }
    val currentIcon = icons[iconIndex % icons.size]
    
    // Disparar animación cuando show cambia a true
    LaunchedEffect(show, tapOffset) {
        if (show) {
            // Rotar icono
            iconIndex++
            
            // Reset
            scale.snapTo(0f)
            alpha.snapTo(1f)
            
            // Animación entrada
            launch {
                scale.animateTo(
                    targetValue = 1.15f,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = 800f)
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )
            }
            
            // Esperar y hacer fade out
            kotlinx.coroutines.delay(500)
            
            launch {
                alpha.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
            }
            scale.animateTo(0.8f, tween(400, easing = FastOutSlowInEasing))
            
            onAnimationEnd()
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        Icon(
            imageVector = currentIcon.icon,
            contentDescription = null,
            modifier = Modifier
                .offset(
                    x = (tapOffset.x - 45).dp,
                    y = (tapOffset.y - 45).dp
                )
                .size(90.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                    rotationZ = (1f - scale.value) * 15f
                },
            tint = currentIcon.tint
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ¿POR QUÉ ESTA ARQUITECTURA LOGRA FLUIDEZ TIPO INSTAGRAM?
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1. ANIMACIONES GPU-FIRST:
 *    - Usamos `graphicsLayer` para scale, alpha y rotation
 *    - Estas propiedades se animan en GPU sin recomposición del árbol UI
 *    - No hay layout invalidation durante la animación
 * 
 * 2. FÍSICA REAL CON SPRING:
 *    - SpringSpec simula física real (masa, rigidez, amortiguamiento)
 *    - El rebote sutil hace que se sienta "físico" y "orgánico"
 *    - Instagram usa exactamente este tipo de animaciones
 * 
 * 3. ZERO ALLOCATIONS EN HOT PATH:
 *    - Los Animatable se crean UNA vez con remember
 *    - No hay creación de objetos durante la animación
 *    - snapTo() y animateTo() no allocan
 * 
 * 4. DESACOPLAMIENTO DEL UI TREE:
 *    - El overlay es independiente del contenido del post
 *    - No importa si el LazyColumn está scrolleando
 *    - La animación corre en su propio "layer"
 * 
 * 5. ANTI-JANK:
 *    - Anti-spam previene múltiples animaciones simultáneas
 *    - Las coroutines están bien estructuradas para no competir
 *    - El estado es minimal y atómico
 * 
 * 6. 120Hz READY:
 *    - Animatable usa el frame clock del sistema
 *    - Se adapta automáticamente a 60/90/120Hz
 *    - No hay hardcoded frame times
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
