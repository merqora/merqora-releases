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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

/**
 * Modal para configurar notificaciones de un usuario específico
 * Abre desde abajo con animación fluida
 */
@Composable
fun NotificationsSettingsModal(
    isVisible: Boolean,
    username: String,
    onDismiss: () -> Unit,
    onEnableAll: () -> Unit = {},
    onEnablePosts: () -> Unit = {},
    onEnableStories: () -> Unit = {},
    onEnableLive: () -> Unit = {},
    onDisableAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Estado de las notificaciones
    var postsEnabled by remember { mutableStateOf(true) }
    var storiesEnabled by remember { mutableStateOf(true) }
    var liveEnabled by remember { mutableStateOf(true) }
    var offersEnabled by remember { mutableStateOf(false) }
    
    // Backdrop
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }
    
    // Modal con animación desde abajo
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
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
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 24.dp)
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
                    
                    // Header con icono
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                PrimaryPurple.copy(alpha = 0.2f),
                                                AccentPink.copy(alpha = 0.2f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = "Notificaciones",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@$username",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        
                        // Botón cerrar
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TextMuted.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Elige qué notificaciones recibir de este usuario",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Opciones de notificación
                    NotificationOption(
                        icon = Icons.Outlined.Image,
                        title = "Publicaciones",
                        description = "Cuando sube nuevos productos",
                        isEnabled = postsEnabled,
                        onToggle = { postsEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    NotificationOption(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "Historias",
                        description = "Cuando comparte historias",
                        isEnabled = storiesEnabled,
                        onToggle = { storiesEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    NotificationOption(
                        icon = Icons.Outlined.Videocam,
                        title = "Transmisiones en vivo",
                        description = "Cuando inicia un directo",
                        isEnabled = liveEnabled,
                        accentColor = Color(0xFFEF4444),
                        onToggle = { liveEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    NotificationOption(
                        icon = Icons.Outlined.LocalOffer,
                        title = "Ofertas exclusivas",
                        description = "Descuentos y promociones especiales",
                        isEnabled = offersEnabled,
                        accentColor = Color(0xFF2E8B57),
                        isPremium = true,
                        onToggle = { offersEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botón guardar
                    Button(
                        onClick = {
                            // TODO: Guardar preferencias
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Guardar preferencias",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationOption(
    icon: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    accentColor: Color = PrimaryPurple,
    isPremium: Boolean = false,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle(!isEnabled) },
        shape = RoundedCornerShape(14.dp),
        color = if (isEnabled) accentColor.copy(alpha = 0.08f) else HomeBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = if (isEnabled) 0.15f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) accentColor else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Text
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isPremium) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "VIP",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            
            // Switch
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = TextMuted.copy(alpha = 0.2f)
                )
            )
        }
    }
}
