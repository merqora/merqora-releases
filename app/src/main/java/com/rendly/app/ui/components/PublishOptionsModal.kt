package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

data class PublishOption(
    val id: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val modeIndex: Int
)

private val PUBLISH_OPTIONS = listOf(
    PublishOption(
        id = "post",
        label = "Publicación",
        description = "Artículo permanente en tu perfil",
        icon = Icons.Outlined.AddBox,
        accentColor = Color(0xFFFF6B35),
        modeIndex = 0
    ),
    PublishOption(
        id = "story",
        label = "Historia",
        description = "Contenido temporal de 24 horas",
        icon = Icons.Outlined.AutoAwesome,
        accentColor = Color(0xFF2E8B57),
        modeIndex = 1
    ),
    PublishOption(
        id = "rend",
        label = "Rend",
        description = "Video corto en formato vertical",
        icon = Icons.Outlined.PlayCircle,
        accentColor = Color(0xFFFF6B35),
        modeIndex = 2
    ),
    PublishOption(
        id = "live",
        label = "En Vivo",
        description = "Transmisión en tiempo real",
        icon = Icons.Outlined.Videocam,
        accentColor = Color(0xFFEF4444),
        modeIndex = 3
    )
)

@Composable
fun PublishOptionsModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (modeIndex: Int) -> Unit
) {
    // Backdrop separado (como ConsultModal)
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }
    
    // Modal con animación slide desde abajo
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .padding(bottom = 80.dp) // Espacio amplio para el NavBar bottom
                        .navigationBarsPadding()
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Crear nuevo",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "¿Qué te gustaría publicar?",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Options en columna vertical
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PUBLISH_OPTIONS.forEach { option ->
                            PublishOptionCard(
                                option = option,
                                onClick = {
                                    onOptionSelected(option.modeIndex)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PublishOptionCard(
    option: PublishOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = option.accentColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                option.accentColor,
                                option.accentColor.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = option.description,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
