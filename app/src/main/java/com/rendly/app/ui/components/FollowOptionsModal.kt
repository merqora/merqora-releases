package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.rendly.app.ui.theme.*

enum class FollowOption {
    FOLLOW,     // Seguidor normal
    CLIENT      // Cliente
}

@Composable
fun FollowOptionsModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (FollowOption) -> Unit,
    isLoading: Boolean = false
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(150)) + 
                scaleIn(initialScale = 0.9f, animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(100)) + 
               scaleOut(targetScale = 0.9f, animationSpec = tween(100))
    ) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(300.dp)
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(20.dp),
                    color = Surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Título
                        Text(
                            text = "¿Cómo quieres seguir?",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "Elige el tipo de relación",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Opción: Seguir
                        FollowOptionCard(
                            icon = Icons.Outlined.PersonAdd,
                            title = "Seguir",
                            description = "Recibe actualizaciones y nuevos productos",
                            color = PrimaryPurple,
                            isLoading = isLoading,
                            onClick = { onOptionSelected(FollowOption.FOLLOW) }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Opción: Ser Cliente
                        FollowOptionCard(
                            icon = Icons.Filled.ShoppingBag,
                            title = "Ser Cliente",
                            description = "Compras frecuentes y beneficios exclusivos",
                            color = AccentGreen,
                            isLoading = isLoading,
                            onClick = { onOptionSelected(FollowOption.CLIENT) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Botón cancelar
                        TextButton(
                            onClick = onDismiss,
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Cancelar",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = color,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            
            // Flecha
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
