package com.rendly.app.ui.screens.publish

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

/**
 * Pantalla de transmisión en vivo - PRÓXIMAMENTE
 * Muestra una pantalla temporal indicando que la funcionalidad estará disponible pronto
 */
@Composable
fun LiveStreamScreen(
    onClose: () -> Unit,
    onModeSelected: (Int) -> Unit,
    currentModeIndex: Int,
    modifier: Modifier = Modifier
) {
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "comingSoon")
    
    // Solo animación del borde giratorio
    val rotateRing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateRing"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HomeBg,
                        Color(0xFF0D0D15),
                        HomeBg
                    )
                )
            )
    ) {
        // Fondo decorativo con círculos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentPink.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar simple
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Surface.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                
                // Badge PRÓXIMAMENTE
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentPink.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentPink)
                        )
                        Text(
                            text = "PRÓXIMAMENTE",
                            color = AccentPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.size(40.dp))
            }
            
            // Contenido central
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                ) {
                    // Icono principal con animación
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // Anillo exterior rotando (única animación)
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .rotate(rotateRing)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            AccentPink.copy(alpha = 0f),
                                            AccentPink.copy(alpha = 0.9f),
                                            PrimaryPurple.copy(alpha = 0.9f),
                                            AccentPink.copy(alpha = 0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // Círculo principal (sin animación de escala)
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            AccentPink,
                                            PrimaryPurple
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Título
                    Text(
                        text = "¡Transmisiones en Vivo!",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Subtítulo
                    Text(
                        text = "Estamos preparando algo increíble para ti.\nMuy pronto podrás transmitir en vivo y conectar\ncon tu audiencia en tiempo real.",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Features compactas en fila
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CompactFeatureItem(
                            icon = Icons.Outlined.Groups,
                            label = "Audiencia"
                        )
                        CompactFeatureItem(
                            icon = Icons.Outlined.ShoppingCart,
                            label = "Ventas"
                        )
                        CompactFeatureItem(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            label = "Chat"
                        )
                    }
                }
            }
            
            // Carrusel de modos en la parte inferior
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                
                com.rendly.app.ui.components.ModeCarousel(
                    currentIndex = currentModeIndex,
                    onModeSelected = onModeSelected,
                    style = com.rendly.app.ui.components.CarouselStyle.CENTERED_SINGLE
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CompactFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AccentPink.copy(alpha = 0.15f),
                            PrimaryPurple.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentPink,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
