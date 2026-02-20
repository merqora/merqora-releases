package com.rendly.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Badge de verificación profesional estilo Instagram/Twitter.
 * Diseño: Estrella de 8 puntas con checkmark blanco.
 * Color: Azul vibrante (#1D9BF0) - el mismo de Twitter/X
 * 
 * @param size Tamaño del badge (default: 18.dp)
 * @param modifier Modificador opcional
 */
@Composable
fun VerifiedBadge(
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(size)
    ) {
        val centerX = size.toPx() / 2
        val centerY = size.toPx() / 2
        val outerRadius = size.toPx() / 2
        val innerRadius = outerRadius * 0.75f
        
        // Crear estrella de 8 puntas
        val starPath = Path().apply {
            val points = 8
            val angleStep = PI / points
            
            for (i in 0 until points * 2) {
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val angle = i * angleStep - PI / 2
                val x = centerX + (radius * cos(angle)).toFloat()
                val y = centerY + (radius * sin(angle)).toFloat()
                
                if (i == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
            close()
        }
        
        // Dibujar estrella azul (color Twitter/Instagram verified)
        drawPath(
            path = starPath,
            color = Color(0xFF1D9BF0),
            style = Fill
        )
        
        // Dibujar checkmark blanco
        val checkPath = Path().apply {
            val checkSize = size.toPx() * 0.35f
            val startX = centerX - checkSize * 0.5f
            val startY = centerY + checkSize * 0.1f
            
            moveTo(startX, startY)
            lineTo(centerX - checkSize * 0.1f, centerY + checkSize * 0.45f)
            lineTo(centerX + checkSize * 0.55f, centerY - checkSize * 0.35f)
        }
        
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(
                width = size.toPx() * 0.12f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

/**
 * Row helper que muestra username + badge de verificación si aplica
 */
@Composable
fun UsernameWithBadge(
    username: String,
    isVerified: Boolean,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 14.dp,
    content: @Composable (String) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        content(username)
        if (isVerified) {
            VerifiedBadge(size = badgeSize)
        }
    }
}
