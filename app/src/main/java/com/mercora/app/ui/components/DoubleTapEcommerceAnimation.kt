package com.mercora.app.ui.components

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
import com.mercora.app.ui.theme.*

/**
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 * DoubleTapEcommerceAnimation - AnimaciÃ³n Ultra Optimizada Estilo Instagram
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 * 
 * Arquitectura:
 * - MÃ³dulo independiente que NO depende de recomposiciones del post
 * - Renderizado como overlay usando graphicsLayer (GPU-accelerated)
 * - Pre-cÃ¡lculo de valores para evitar allocations durante animaciÃ³n
 * - Animaciones con SpringSpec fÃ­sico para fluidez natural
 * 
 * Performance:
 * - Uso de Animatable para animaciones state-less
 * - graphicsLayer para transformaciones en GPU
 * - Sin allocations en hot path (remember + pre-computed values)
 * - Preparado para 60-120Hz refresh rates
 * 
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 */

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// CONFIGURACIÃ“N - FÃ¡cilmente ajustable
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

object DoubleTapAnimationConfig {
    // DuraciÃ³n total de la animaciÃ³n
    const val TOTAL_DURATION_MS = 900
    
    // Escala del icono
    const val ICON_SIZE_DP = 90
    const val INITIAL_SCALE = 0f
    const val PEAK_SCALE = 1.15f
    const val FINAL_SCALE = 1f
    
    // Spring specs para fÃ­sica natural
    val SCALE_IN_SPRING = SpringSpec<Float>(
        dampingRatio = 0.55f,  // Rebote moderado
        stiffness = 800f       // Respuesta rÃ¡pida
    )
    
    val SCALE_OUT_SPRING = SpringSpec<Float>(
        dampingRatio = 0.8f,
        stiffness = 400f
    )
    
    // Fade specs
    const val FADE_IN_DURATION = 100
    const val HOLD_DURATION = 400
    const val FADE_OUT_DURATION = 400
    
    // Anti-spam: tiempo mÃ­nimo entre animaciones
    const val MIN_TIME_BETWEEN_TAPS_MS = 350L
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ICONOS ECOMMERCE - Lista personalizable
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

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

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// ESTADO DE LA ANIMACIÃ“N - Encapsulado y reutilizable
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

class DoubleTapAnimationState(
    private val icons: List<EcommerceIcon> = defaultEcommerceIcons
) {
    // Animatables pre-creados (NO se recrean en cada tap)
    val scale = Animatable(0f)
    val alpha = Animatable(0f)
    
    // Estado de visibilidad
    var isVisible by mutableStateOf(false)
        private set
    
    // PosiciÃ³n del tap
    var tapOffset by mutableStateOf(Offset.Zero)
        private set
    
    // Icono actual (rotativo)
    private var currentIconIndex by mutableIntStateOf(0)
    val currentIcon: EcommerceIcon
        get() = icons[currentIconIndex]
    
    // Anti-spam
    private var lastTapTime = 0L
    
    /**
     * Dispara la animaciÃ³n en la posiciÃ³n indicada.
     * Retorna true si se ejecutÃ³, false si fue bloqueado por anti-spam.
     */
    suspend fun trigger(offset: Offset): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < DoubleTapAnimationConfig.MIN_TIME_BETWEEN_TAPS_MS) {
            return false
        }
        lastTapTime = now
        
        // Rotar al siguiente icono
        currentIconIndex = (currentIconIndex + 1) % icons.size
        
        // Guardar posiciÃ³n
        tapOffset = offset
        
        // Reset instantÃ¡neo
        scale.snapTo(DoubleTapAnimationConfig.INITIAL_SCALE)
        alpha.snapTo(1f)
        isVisible = true
        
        // AnimaciÃ³n de entrada (scale up con spring fÃ­sico)
        scale.animateTo(
            targetValue = DoubleTapAnimationConfig.PEAK_SCALE,
            animationSpec = DoubleTapAnimationConfig.SCALE_IN_SPRING
        )
        
        // PequeÃ±o settle
        scale.animateTo(
            targetValue = DoubleTapAnimationConfig.FINAL_SCALE,
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = 500f
            )
        )
        
        // Hold breve
        kotlinx.coroutines.delay(DoubleTapAnimationConfig.HOLD_DURATION.toLong())
        
        // AnimaciÃ³n de salida (fade + scale out)
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

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// COMPOSABLE PRINCIPAL - Overlay de animaciÃ³n
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

/**
 * Overlay de animaciÃ³n para doble tap.
 * 
 * USO:
 * ```kotlin
 * val animState = rememberDoubleTapAnimationState()
 * 
 * Box {
 *     // Tu contenido (imagen del post)
 *     AsyncImage(...)
 *     
 *     // Overlay de animaciÃ³n
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
                    
                    // PequeÃ±a rotaciÃ³n para mÃ¡s dinamismo
                    rotationZ = (1f - state.scale.value) * 15f
                },
            tint = icon.tint
        )
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// VERSIÃ“N SIMPLIFICADA - Para integraciÃ³n rÃ¡pida
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

/**
 * VersiÃ³n todo-en-uno que maneja su propio estado.
 * Ideal para integraciÃ³n rÃ¡pida cuando no necesitas control externo.
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
    
    // Disparar animaciÃ³n cuando show cambia a true
    LaunchedEffect(show, tapOffset) {
        if (show) {
            // Rotar icono
            iconIndex++
            
            // Reset
            scale.snapTo(0f)
            alpha.snapTo(1f)
            
            // AnimaciÃ³n entrada
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
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 * Â¿POR QUÃ‰ ESTA ARQUITECTURA LOGRA FLUIDEZ TIPO INSTAGRAM?
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 * 
 * 1. ANIMACIONES GPU-FIRST:
 *    - Usamos `graphicsLayer` para scale, alpha y rotation
 *    - Estas propiedades se animan en GPU sin recomposiciÃ³n del Ã¡rbol UI
 *    - No hay layout invalidation durante la animaciÃ³n
 * 
 * 2. FÃSICA REAL CON SPRING:
 *    - SpringSpec simula fÃ­sica real (masa, rigidez, amortiguamiento)
 *    - El rebote sutil hace que se sienta "fÃ­sico" y "orgÃ¡nico"
 *    - Instagram usa exactamente este tipo de animaciones
 * 
 * 3. ZERO ALLOCATIONS EN HOT PATH:
 *    - Los Animatable se crean UNA vez con remember
 *    - No hay creaciÃ³n de objetos durante la animaciÃ³n
 *    - snapTo() y animateTo() no allocan
 * 
 * 4. DESACOPLAMIENTO DEL UI TREE:
 *    - El overlay es independiente del contenido del post
 *    - No importa si el LazyColumn estÃ¡ scrolleando
 *    - La animaciÃ³n corre en su propio "layer"
 * 
 * 5. ANTI-JANK:
 *    - Anti-spam previene mÃºltiples animaciones simultÃ¡neas
 *    - Las coroutines estÃ¡n bien estructuradas para no competir
 *    - El estado es minimal y atÃ³mico
 * 
 * 6. 120Hz READY:
 *    - Animatable usa el frame clock del sistema
 *    - Se adapta automÃ¡ticamente a 60/90/120Hz
 *    - No hay hardcoded frame times
 * 
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 */
