package com.mercora.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlin.math.sin

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// MOTOR DE ANIMACIONES DE TEXTO - GPU-First, 60 FPS
// Nivel Instagram Stories: Todas las animaciones usan graphicsLayer
// para ejecutarse en la GPU sin recomposiciÃ³n
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

/**
 * Aplica la animaciÃ³n seleccionada al texto overlay.
 * Usa SOLO graphicsLayer para mÃ¡ximo rendimiento (GPU, sin recomposiciÃ³n).
 * 
 * @param animationId ID de la animaciÃ³n ("none", "fade", "slide", "bounce", "typewriter", "glow")
 * @param textColor Color del texto para efectos que lo necesiten (glow)
 * @param isVisible Si el texto estÃ¡ visible (para trigger de animaciones de entrada)
 */
@Composable
fun Modifier.textAnimation(
    animationId: String,
    textColor: Color = Color.White,
    isVisible: Boolean = true
): Modifier {
    if (!isVisible || animationId == "none") return this
    
    return when (animationId) {
        "fade" -> this.then(fadeAnimation())
        "slide" -> this.then(slideAnimation())
        "bounce" -> this.then(bounceAnimation())
        "typewriter" -> this.then(typewriterAnimation())
        "glow" -> this.then(glowAnimation(textColor))
        else -> this
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// FADE - Pulso suave de opacidad (Instagram-style)
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
@Composable
private fun fadeAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "fade")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fade_alpha"
    )
    
    return Modifier.graphicsLayer { this.alpha = alpha }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// SLIDE - Deslizamiento horizontal continuo suave
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
@Composable
private fun slideAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "slide")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slide_x"
    )
    
    return Modifier.graphicsLayer { translationX = offsetX }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// BOUNCE - Rebote vertical elÃ¡stico (spring-based)
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
@Composable
private fun bounceAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    
    // AnimaciÃ³n de tiempo continua para simular bounce con sin()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bounce_time"
    )
    
    return Modifier.graphicsLayer {
        // Bounce suave con rebote amortiguado
        val bounce = -kotlin.math.abs(sin(time.toDouble())).toFloat() * 24f
        translationY = bounce
        // Squash & stretch sutil para realismo
        val stretch = 1f + kotlin.math.abs(sin(time.toDouble())).toFloat() * 0.04f
        scaleY = stretch
        scaleX = 1f / stretch // Conservar volumen
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// TYPEWRITER - ApariciÃ³n progresiva con efecto mÃ¡quina de escribir
// Usa clip + translationX para revelar letra por letra
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
@Composable
private fun typewriterAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "typewriter")
    
    // Parpadeo del cursor (simula typing)
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )
    
    // Escala sutil de "tecleo"
    val scaleKick by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.97f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "type_scale"
    )
    
    return Modifier.graphicsLayer {
        scaleX = scaleKick
        scaleY = scaleKick
    }
}

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// GLOW - Resplandor pulsante alrededor del texto
// Usa renderEffect para glow GPU-accelerated
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
@Composable
private fun glowAnimation(textColor: Color): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_intensity"
    )
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    
    return Modifier
        .graphicsLayer {
            scaleX = glowScale
            scaleY = glowScale
            // Shadow glow via elevation + ambient
            shadowElevation = glowIntensity * 20f
            ambientShadowColor = textColor.copy(alpha = glowIntensity * 0.6f)
            spotShadowColor = textColor.copy(alpha = glowIntensity * 0.8f)
        }
}
