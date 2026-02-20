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
import androidx.compose.foundation.layout.navigationBarsPadding
import com.rendly.app.data.repository.FollowType
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FollowBottomSheet(
    isVisible: Boolean,
    username: String,
    currentFollowType: FollowType,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onFollow: () -> Unit,
    onRequestClient: () -> Unit,
    onUnfollow: () -> Unit
) {
    // Backdrop - igual que ConsultModal
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }
    
    // Modal con animación simple igual que ConsultModal
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
                        .navigationBarsPadding()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { }
                        ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 16.dp, bottom = 12.dp)
                    ) {
                        // X close button top-right
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
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
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Handle bar
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(TextMuted.copy(alpha = 0.3f))
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Header icon
                            Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF2E8B57).copy(alpha = 0.2f),
                                            Color(0xFF1565A0).copy(alpha = 0.2f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Title
                        Text(
                            text = "Conectar con @$username",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Elige cómo quieres interactuar",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Options based on current follow status
                        when (currentFollowType) {
                            FollowType.NONE -> {
                                // Option 1: Follow (sin spinner - acción rápida)
                                FollowOptionItem(
                                    icon = Icons.Outlined.PersonAdd,
                                    title = "Seguir",
                                    description = "Recibe actualizaciones de sus productos y novedades",
                                    accentColor = Color(0xFF2E8B57),
                                    showSpinner = false,
                                    onClick = onFollow
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Option 2: Request Client (con spinner)
                                FollowOptionItem(
                                    icon = Icons.Outlined.LocalOffer,
                                    title = "Solicitar ser Cliente",
                                    description = "Accede a ofertas exclusivas, descuentos y beneficios VIP. El vendedor debe aceptarte.",
                                    accentColor = Color(0xFF1565A0),
                                    isPremium = true,
                                    showSpinner = isLoading,
                                    onClick = onRequestClient
                                )
                            }
                            
                            FollowType.FOLLOWER -> {
                                // Already following - show upgrade to client option
                                FollowOptionItem(
                                    icon = Icons.Outlined.LocalOffer,
                                    title = "Solicitar ser Cliente",
                                    description = "Accede a ofertas exclusivas, descuentos y beneficios VIP. El vendedor debe aceptarte.",
                                    accentColor = Color(0xFF1565A0),
                                    isPremium = true,
                                    showSpinner = isLoading,
                                    onClick = onRequestClient
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Unfollow option
                                FollowOptionItem(
                                    icon = Icons.Outlined.PersonRemove,
                                    title = "Dejar de seguir",
                                    description = "Ya no recibirás actualizaciones de este vendedor",
                                    accentColor = Color(0xFFEF4444),
                                    isDestructive = true,
                                    showSpinner = false,
                                    onClick = onUnfollow
                                )
                            }
                            
                            FollowType.CLIENT_PENDING -> {
                                // Pending request info
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFFFA726).copy(alpha = 0.1f))
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = Color(0xFFFFA726),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Solicitud pendiente",
                                                color = Color(0xFFFFA726),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "El vendedor aún no ha aceptado tu solicitud de cliente",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Cancel request / Unfollow
                                FollowOptionItem(
                                    icon = Icons.Outlined.PersonRemove,
                                    title = "Cancelar solicitud",
                                    description = "Deja de seguir y cancela la solicitud de cliente",
                                    accentColor = Color(0xFFEF4444),
                                    isDestructive = true,
                                    showSpinner = isLoading,
                                    onClick = onUnfollow
                                )
                            }
                            
                            FollowType.CLIENT -> {
                                // Already a client
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF2E8B57).copy(alpha = 0.15f),
                                                    Color(0xFF1565A0).copy(alpha = 0.15f)
                                                )
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = Color(0xFF2E8B57),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Eres Cliente VIP",
                                                color = Color(0xFF2E8B57),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Disfrutas de ofertas exclusivas y beneficios especiales",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Unfollow option
                                FollowOptionItem(
                                    icon = Icons.Outlined.PersonRemove,
                                    title = "Dejar de ser cliente",
                                    description = "Perderás acceso a ofertas exclusivas",
                                    accentColor = Color(0xFFEF4444),
                                    isDestructive = true,
                                    showSpinner = false,
                                    onClick = onUnfollow
                                )
                            }
                        }
                        
                            Spacer(modifier = Modifier.height(8.dp))
                        } // End Column
                    } // End Box
                }
        }
    }
}

@Composable
private fun FollowOptionItem(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    isPremium: Boolean = false,
    isDestructive: Boolean = false,
    showSpinner: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !showSpinner, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isPremium) {
            Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.1f),
                    accentColor.copy(alpha = 0.05f)
                )
            ).let { Color.Transparent }
        } else {
            accentColor.copy(alpha = 0.08f)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isPremium) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.1f),
                                    accentColor.copy(alpha = 0.05f)
                                )
                            )
                        )
                    } else {
                        Modifier.background(accentColor.copy(alpha = 0.08f))
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (showSpinner) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = accentColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = if (isDestructive) accentColor else TextPrimary,
                            fontSize = 15.sp,
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                
                // Arrow
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
