package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

@Composable
fun UserProfileSettingsModal(
    isVisible: Boolean,
    username: String,
    isBlocked: Boolean = false,
    isMuted: Boolean = false,
    onDismiss: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit = {},
    onMute: () -> Unit = {},
    onUnmute: () -> Unit = {},
    onReport: () -> Unit,
    onShareProfile: () -> Unit = {},
    onCopyProfileLink: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    
    // Backdrop
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )
    }
    
    // Modal
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp, bottom = 40.dp)
                ) {
                    // Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Título
                    Text(
                        text = "Opciones de @$username",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Opciones
                    ProfileSettingOption(
                        icon = Icons.Outlined.Share,
                        title = "Compartir perfil",
                        subtitle = "Enviar enlace del perfil",
                        iconColor = PrimaryPurple,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onShareProfile()
                        }
                    )
                    
                    ProfileSettingOption(
                        icon = Icons.Outlined.Link,
                        title = "Copiar enlace",
                        subtitle = "Copiar enlace al portapapeles",
                        iconColor = Color(0xFF6B7280),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCopyProfileLink()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ProfileSettingOption(
                        icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Outlined.VolumeOff,
                        title = if (isMuted) "Dejar de silenciar" else "Silenciar a @$username",
                        subtitle = if (isMuted) "Volver a ver publicaciones" else "No verás sus publicaciones en tu feed",
                        iconColor = Color(0xFFFF6B35),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (isMuted) onUnmute() else onMute()
                        }
                    )
                    
                    ProfileSettingOption(
                        icon = if (isBlocked) Icons.Filled.Block else Icons.Outlined.Block,
                        title = if (isBlocked) "Desbloquear a @$username" else "Bloquear a @$username",
                        subtitle = if (isBlocked) "Podrá ver tu perfil de nuevo" else "No podrá ver tu perfil ni contactarte",
                        iconColor = Color(0xFFEF4444),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isBlocked) onUnblock() else onBlock()
                        }
                    )
                    
                    ProfileSettingOption(
                        icon = Icons.Outlined.Flag,
                        title = "Reportar a @$username",
                        subtitle = "Denunciar comportamiento inadecuado",
                        iconColor = Color(0xFFEF4444),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReport()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSettingOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
