package com.rendly.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class CarouselMode(
    val id: String,
    val label: String,
    val accentColor: Color
)

val CAROUSEL_MODES = listOf(
    CarouselMode("post", "PUBLICACIÓN", Color(0xFFFF6B35)),
    CarouselMode("story", "HISTORIA", Color(0xFF2E8B57)),
    CarouselMode("rend", "REND", Color(0xFFFF6B35)),
    CarouselMode("live", "EN VIVO", Color(0xFFEF4444))
)

// Estilos de carrusel
enum class CarouselStyle {
    FULL,           // Muestra todos los modos (default)
    CENTERED_SINGLE, // Solo muestra el modo actual centrado (HISTORIA, REND)
    FLOATING_RIGHT   // Flotante a la derecha con fondo (PUBLICACIÓN)
}

@Composable
fun ModeCarousel(
    currentIndex: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    style: CarouselStyle = CarouselStyle.FULL
) {
    if (!visible) return
    
    when (style) {
        CarouselStyle.CENTERED_SINGLE -> CenteredSingleMode(
            currentIndex = currentIndex,
            onModeSelected = onModeSelected,
            modifier = modifier
        )
        CarouselStyle.FLOATING_RIGHT -> FloatingRightMode(
            currentIndex = currentIndex,
            onModeSelected = onModeSelected,
            modifier = modifier
        )
        CarouselStyle.FULL -> FullModeCarousel(
            currentIndex = currentIndex,
            onModeSelected = onModeSelected,
            modifier = modifier
        )
    }
}

/**
 * Modo centrado simple - Solo muestra el modo actual con swipe para cambiar
 * Usado en HISTORIA y REND debajo del botón central
 */
@Composable
private fun CenteredSingleMode(
    currentIndex: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMode = CAROUSEL_MODES.getOrNull(currentIndex) ?: return
    
    var dragOffset by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = 50f
                        val newIndex = when {
                            dragOffset < -threshold && currentIndex < CAROUSEL_MODES.lastIndex -> currentIndex + 1
                            dragOffset > threshold && currentIndex > 0 -> currentIndex - 1
                            else -> currentIndex
                        }
                        if (newIndex != currentIndex) {
                            onModeSelected(newIndex)
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Flechas indicadoras sutiles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flecha izquierda (si hay modo anterior)
            Text(
                text = "‹",
                fontSize = 18.sp,
                color = if (currentIndex > 0) Color.White.copy(alpha = 0.4f) else Color.Transparent,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // Texto del modo actual
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentMode.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Línea indicadora
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(currentMode.accentColor)
                )
            }
            
            // Flecha derecha (si hay modo siguiente)
            Text(
                text = "›",
                fontSize = 18.sp,
                color = if (currentIndex < CAROUSEL_MODES.lastIndex) Color.White.copy(alpha = 0.4f) else Color.Transparent,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

/**
 * Modo flotante a la derecha - Para PUBLICACIÓN
 * Fondo azul oscuro, pegado a la derecha, border radius solo a la izquierda
 */
@Composable
private fun FloatingRightMode(
    currentIndex: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMode = CAROUSEL_MODES.getOrNull(currentIndex) ?: return
    
    var dragOffset by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.60f) // 60% del ancho - más compacto
                .height(44.dp)
                .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp, topEnd = 0.dp, bottomEnd = 0.dp))
                .background(Color(0xFF0F2942)) // Azul más oscuro
                .pointerInput(currentIndex) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = 50f
                            val newIndex = when {
                                dragOffset < -threshold && currentIndex < CAROUSEL_MODES.lastIndex -> currentIndex + 1
                                dragOffset > threshold && currentIndex > 0 -> currentIndex - 1
                                else -> currentIndex
                            }
                            if (newIndex != currentIndex) {
                                onModeSelected(newIndex)
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f },
                        onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flecha izquierda
                Text(
                    text = "‹",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = if (currentIndex > 0) Color.White.copy(alpha = 0.6f) else Color.Transparent
                )
                
                // Texto del modo con indicador
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentMode.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(currentMode.accentColor)
                    )
                }
                
                // Flecha derecha
                Text(
                    text = "›",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = if (currentIndex < CAROUSEL_MODES.lastIndex) Color.White.copy(alpha = 0.6f) else Color.Transparent
                )
            }
        }
    }
}

/**
 * Carrusel completo - Muestra todos los modos
 * OPTIMIZADO: Animaciones simplificadas para mejor rendimiento
 */
@Composable
private fun FullModeCarousel(
    currentIndex: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = 50f
                        val newIndex = when {
                            dragOffset < -threshold && currentIndex < CAROUSEL_MODES.lastIndex -> currentIndex + 1
                            dragOffset > threshold && currentIndex > 0 -> currentIndex - 1
                            else -> currentIndex
                        }
                        if (newIndex != currentIndex) {
                            onModeSelected(newIndex)
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CAROUSEL_MODES.forEachIndexed { index, mode ->
                val isSelected = index == currentIndex
                
                // Animación simplificada - solo escala y alpha
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 0.85f,
                    animationSpec = tween(150),
                    label = "scale_$index"
                )
                
                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.4f,
                    animationSpec = tween(150),
                    label = "alpha_$index"
                )
                
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) onModeSelected(index)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = mode.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextMuted.copy(alpha = 0.6f),
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Indicador sin animación de ancho
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(mode.accentColor)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}
