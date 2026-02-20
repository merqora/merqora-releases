package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

@Composable
fun UploadProgressBanner(
    isVisible: Boolean,
    progress: Float,
    isComplete: Boolean,
    error: String? = null,
    type: String = "post",
    onComplete: () -> Unit = {},
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasError = error != null
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300, easing = LinearEasing),
        label = "progress"
    )
    
    // Auto-ocultar cuando completa (solo si no hay error)
    LaunchedEffect(isComplete, hasError) {
        if (isComplete && !hasError) {
            kotlinx.coroutines.delay(1500)
            onComplete()
        }
    }
    
    // Auto-ocultar error después de 10 segundos
    LaunchedEffect(hasError) {
        if (hasError) {
            kotlinx.coroutines.delay(10000)
            onDismissError()
        }
    }
    
    AnimatedVisibility(
        visible = isVisible || hasError,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (hasError) Color(0xFF2D1F1F) else Surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icono
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        hasError -> Color.Red.copy(alpha = 0.2f)
                                        isComplete -> AccentGreen.copy(alpha = 0.2f)
                                        else -> PrimaryPurple.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    hasError -> Icons.Default.Close
                                    isComplete -> Icons.Default.Check
                                    else -> Icons.Default.KeyboardArrowUp
                                },
                                contentDescription = null,
                                tint = when {
                                    hasError -> Color.Red
                                    isComplete -> AccentGreen
                                    else -> PrimaryPurple
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = when {
                                    hasError -> "Error al publicar"
                                    isComplete -> if (type == "post") "¡Publicación lista!" else "¡Historia publicada!"
                                    else -> if (type == "post") "Subiendo publicación..." else "Subiendo historia..."
                                },
                                color = if (hasError) Color.Red else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    hasError -> error ?: "Error desconocido"
                                    isComplete -> "Tu contenido ya está visible"
                                    else -> "${(animatedProgress * 100).toInt()}% completado"
                                },
                                color = if (hasError) Color.Red.copy(alpha = 0.7f) else TextMuted,
                                fontSize = 12.sp,
                                maxLines = 3
                            )
                        }
                    }
                    
                    // Porcentaje o check
                    if (!isComplete && !hasError) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = PrimaryPurple,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Barra de progreso (solo si no hay error)
                if (!isComplete && !hasError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(PrimaryPurple, AccentPink)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
