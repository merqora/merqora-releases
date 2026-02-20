package com.rendly.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.AccentPink
import com.rendly.app.ui.theme.PrimaryPurple
import com.rendly.app.ui.theme.TextMuted
import com.rendly.app.ui.theme.TextPrimary
import kotlinx.coroutines.launch

/**
 * Carrusel de modos de publicación - Diseño Ruleta Profesional
 * 
 * Comportamientos:
 * - Historia (index 1): Ancho completo, pegado al TabBar del dispositivo
 * - Publicación (index 0): Flotante, pegado a la derecha, 75% del ancho, borderRadius izquierdo
 * - En Vivo (index 2) y Rend (index 3): Centrado en la parte inferior
 * 
 * Características:
 * - Ruleta horizontal con máximo 3 modos visibles
 * - Modo actual siempre centrado
 * - Animaciones suaves entre estados
 */

// Enum para los diferentes layouts del carrusel
enum class CarouselLayout {
    FULL_WIDTH,      // Historia - ancho completo
    FLOATING_RIGHT,  // Publicación - flotante a la derecha
    CENTERED         // En Vivo & Rend - centrado
}

@Composable
fun ModesCarousel(
    currentIndex: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf("PUBLICACIÓN", "HISTORIA", "EN VIVO", "REND")
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    
    // Determinar el layout según el modo actual
    val layout = when (currentIndex) {
        1 -> CarouselLayout.FULL_WIDTH      // Historia
        0 -> CarouselLayout.FLOATING_RIGHT  // Publicación  
        else -> CarouselLayout.CENTERED     // En Vivo, Rend
    }
    
    // Animaciones de transición
    val animDuration = 300
    
    // Ancho del carrusel
    val carouselWidth by animateDpAsState(
        targetValue = when (layout) {
            CarouselLayout.FULL_WIDTH -> screenWidth
            CarouselLayout.FLOATING_RIGHT -> screenWidth * 0.75f
            CarouselLayout.CENTERED -> screenWidth * 0.7f
        },
        animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
        label = "carouselWidth"
    )
    
    // Offset horizontal (para posicionamiento)
    val horizontalOffset by animateDpAsState(
        targetValue = when (layout) {
            CarouselLayout.FULL_WIDTH -> 0.dp
            CarouselLayout.FLOATING_RIGHT -> screenWidth * 0.25f
            CarouselLayout.CENTERED -> (screenWidth - screenWidth * 0.7f) / 2
        },
        animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
        label = "horizontalOffset"
    )
    
    // Border radius izquierdo
    val leftRadius by animateDpAsState(
        targetValue = when (layout) {
            CarouselLayout.FULL_WIDTH -> 0.dp
            CarouselLayout.FLOATING_RIGHT -> 28.dp
            CarouselLayout.CENTERED -> 28.dp
        },
        animationSpec = tween(animDuration),
        label = "leftRadius"
    )
    
    // Border radius derecho
    val rightRadius by animateDpAsState(
        targetValue = when (layout) {
            CarouselLayout.FULL_WIDTH -> 0.dp
            CarouselLayout.FLOATING_RIGHT -> 0.dp
            CarouselLayout.CENTERED -> 28.dp
        },
        animationSpec = tween(animDuration),
        label = "rightRadius"
    )
    
    // Color de fondo
    val bgColor by animateColorAsState(
        targetValue = when (layout) {
            CarouselLayout.FULL_WIDTH -> Color(0xFF0D0D14)
            else -> Color(0xFF151520).copy(alpha = 0.98f)
        },
        animationSpec = tween(animDuration),
        label = "bgColor"
    )
    
    // Auto-scroll para centrar el modo seleccionado
    LaunchedEffect(currentIndex) {
        // Pequeño delay para que la animación del layout ocurra primero
        kotlinx.coroutines.delay(50)
        listState.animateScrollToItem(
            index = maxOf(0, currentIndex),
            scrollOffset = 0
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = horizontalOffset)
    ) {
        Box(
            modifier = Modifier
                .width(carouselWidth)
                .height(54.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = leftRadius,
                        bottomStart = leftRadius,
                        topEnd = rightRadius,
                        bottomEnd = rightRadius
                    )
                )
                .background(bgColor)
        ) {
            // Contenido del carrusel - Ruleta
            CarouselContent(
                modes = modes,
                currentIndex = currentIndex,
                onModeSelected = { index ->
                    onModeSelected(index)
                    scope.launch {
                        listState.animateScrollToItem(index)
                    }
                },
                listState = listState,
                carouselWidth = carouselWidth
            )
        }
    }
}

@Composable
private fun CarouselContent(
    modes: List<String>,
    currentIndex: Int,
    onModeSelected: (Int) -> Unit,
    listState: LazyListState,
    carouselWidth: Dp
) {
    val density = LocalDensity.current
    val itemWidth = 85.dp
    val itemSpacing = 8.dp
    
    // Calcular el padding horizontal para centrar los items
    val itemWidthPx = with(density) { itemWidth.toPx() }
    val carouselWidthPx = with(density) { carouselWidth.toPx() }
    
    // El padding debe centrar el item seleccionado
    val horizontalPadding = with(density) { 
        ((carouselWidthPx - itemWidthPx) / 2).toDp() 
    }
    
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    ) {
        itemsIndexed(modes) { index, mode ->
            // Calcular la distancia al centro para efectos visuales
            val centerOffset = remember { derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                val itemInfo = visibleItems.find { it.index == index }
                if (itemInfo != null) {
                    val center = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2
                    val itemCenter = itemInfo.offset + itemInfo.size / 2
                    (itemCenter - center).toFloat() / (layoutInfo.viewportSize.width / 2)
                } else {
                    1f
                }
            }}
            
            ModeItemRoulette(
                text = mode,
                isSelected = index == currentIndex,
                modeIndex = index,
                centerOffset = centerOffset.value,
                onClick = { onModeSelected(index) }
            )
        }
    }
}

@Composable
private fun ModeItemRoulette(
    text: String,
    isSelected: Boolean,
    modeIndex: Int,
    centerOffset: Float,
    onClick: () -> Unit
) {
    // Escala basada en la distancia al centro (efecto ruleta)
    val scale = (1f - minOf(kotlin.math.abs(centerOffset) * 0.15f, 0.2f))
    
    // Alpha basada en la distancia al centro
    val alpha = (1f - minOf(kotlin.math.abs(centerOffset) * 0.4f, 0.5f))
    
    // Color según el modo
    val activeColor = when (modeIndex) {
        0 -> PrimaryPurple        // Publicación
        1 -> PrimaryPurple        // Historia
        2 -> AccentPink           // En Vivo
        3 -> AccentPink           // Rend
        else -> PrimaryPurple
    }
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else TextMuted.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "textColor"
    )
    
    Box(
        modifier = Modifier
            .width(85.dp)
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = if (isSelected) 13.sp else 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
            
            // Indicador de selección animado
            AnimatedIndicator(
                isVisible = isSelected,
                color = activeColor
            )
        }
    }
}

@Composable
private fun AnimatedIndicator(
    isVisible: Boolean,
    color: Color
) {
    val indicatorWidth by animateDpAsState(
        targetValue = if (isVisible) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorWidth"
    )
    
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200),
        label = "indicatorAlpha"
    )
    
    Spacer(modifier = Modifier.height(5.dp))
    
    Box(
        modifier = Modifier
            .width(indicatorWidth)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color.copy(alpha = indicatorAlpha))
    )
}
