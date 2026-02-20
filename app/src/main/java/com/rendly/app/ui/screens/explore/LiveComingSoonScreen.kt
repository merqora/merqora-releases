package com.rendly.app.ui.screens.explore

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

/**
 * Pantalla de "Próximamente" para la sección En Vivo del Explore
 * Accesible desde los accesos rápidos del header de ExploreScreen
 */
@Composable
fun LiveComingSoonScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Solo animación del borde giratorio
    val infiniteTransition = rememberInfiniteTransition(label = "liveComingSoon")
    
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
            .background(HomeBg)
    ) {
        // Fondo decorativo sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEF4444).copy(alpha = 0.08f),
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
            // Top bar con botón de volver
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
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
                
                // Badge EN VIVO
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f)
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
                                .background(Color(0xFFEF4444))
                        )
                        Text(
                            text = "PRÓXIMAMENTE",
                            color = Color(0xFFEF4444),
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
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    // Icono principal con borde animado
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
                                            Color(0xFFEF4444).copy(alpha = 0f),
                                            Color(0xFFEF4444).copy(alpha = 0.9f),
                                            Color(0xFFFF6B6B).copy(alpha = 0.9f),
                                            Color(0xFFEF4444).copy(alpha = 0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // Círculo principal (sin animación)
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFEF4444),
                                            Color(0xFFFF6B6B)
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
                        text = "Muy pronto podrás ver transmisiones en vivo\nde tus vendedores favoritos y descubrir\nproductos en tiempo real.",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Features que vendrán - compactos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LiveFeatureChip(
                            icon = Icons.Outlined.PlayCircle,
                            title = "En vivo",
                            modifier = Modifier.weight(1f)
                        )
                        LiveFeatureChip(
                            icon = Icons.Outlined.ShoppingBag,
                            title = "Compras",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LiveFeatureChip(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            title = "Chat",
                            modifier = Modifier.weight(1f)
                        )
                        LiveFeatureChip(
                            icon = Icons.Outlined.Notifications,
                            title = "Alertas",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveFeatureChip(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Surface.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
