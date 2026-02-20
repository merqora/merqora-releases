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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.ui.theme.*

@Composable
fun PostOptionsModal(
    isVisible: Boolean,
    post: Post?,
    isOwnPost: Boolean,
    isSaved: Boolean = false,
    onDismiss: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onPromote: () -> Unit = {},
    onViewStats: () -> Unit = {},
    onShare: () -> Unit = {},
    onCopyLink: () -> Unit = {},
    onSavePost: () -> Unit = {},
    onShowQrCode: () -> Unit = {},
    onReport: () -> Unit = {},
    onBlock: () -> Unit = {},
    onHide: () -> Unit = {},
    onUnfollow: () -> Unit = {},
    onAboutAccount: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    
    // Guardar último post válido para animación
    var lastValidPost by remember { mutableStateOf<Post?>(null) }
    LaunchedEffect(post) {
        if (post != null) lastValidPost = post
    }
    val currentPost = post ?: lastValidPost
    
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
        visible = isVisible && currentPost != null,
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
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 16.dp)
                        .navigationBarsPadding()
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Info del post
                    currentPost?.let { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Imagen del post
                            val imageUrl = p.images.firstOrNull() ?: p.producto.imagenUrl.firstOrNull() ?: ""
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.title ?: p.producto.titulo,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "@${p.username}",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                            
                            if (isOwnPost) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0A3D62).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Tu post",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0A3D62),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Divider(color = BorderSubtle)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isOwnPost) {
                        // Opciones para TU publicación
                        OptionItem(
                            icon = Icons.Outlined.Edit,
                            title = "Editar publicación",
                            subtitle = "Modifica título, descripción o precio",
                            iconColor = Color(0xFF0A3D62),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onEdit()
                            }
                        )
                        
                        OptionItem(
                            icon = Icons.Outlined.TrendingUp,
                            title = "Promocionar",
                            subtitle = "Aumenta la visibilidad de tu post",
                            iconColor = Color(0xFF2E8B57),
                            badge = "PRO",
                            badgeColor = Color(0xFFFF6B35),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPromote()
                            }
                        )
                        
                        OptionItem(
                            icon = Icons.Outlined.BarChart,
                            title = "Ver estadísticas",
                            subtitle = "Visualizaciones, likes y más",
                            iconColor = Color(0xFF0A3D62),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onViewStats()
                            }
                        )
                        
                        OptionItem(
                            icon = Icons.Outlined.Share,
                            title = "Compartir",
                            subtitle = "Comparte en redes sociales",
                            iconColor = Color(0xFFFF6B35),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onShare()
                            }
                        )
                        
                        OptionItem(
                            icon = Icons.Outlined.Link,
                            title = "Copiar enlace",
                            subtitle = "Copia el link de la publicación",
                            iconColor = Color(0xFF2E8B57),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCopyLink()
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OptionItem(
                            icon = Icons.Outlined.Delete,
                            title = "Eliminar publicación",
                            subtitle = "Esta acción no se puede deshacer",
                            iconColor = Color(0xFFEF4444),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDelete()
                            }
                        )
                    } else {
                        // Opciones para publicación de OTROS
                        // Acciones rápidas horizontales - Solo Guardar y Código QR
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionButton(
                                icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                label = if (isSaved) "Guardado" else "Guardar",
                                color = Color(0xFFFF6B35),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSavePost()
                                }
                            )
                            
                            QuickActionButton(
                                icon = Icons.Outlined.QrCode2,
                                label = "Código QR",
                                color = IconAccentBlue,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onShowQrCode()
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OptionItem(
                            icon = Icons.Outlined.VisibilityOff,
                            title = "Ocultar publicación",
                            subtitle = "No verás esta publicación de nuevo",
                            iconColor = TextMuted,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onHide()
                            }
                        )
                        
                        OptionItem(
                            icon = Icons.Outlined.PersonOff,
                            title = "Dejar de seguir",
                            subtitle = "No verás más publicaciones de @${currentPost?.username}",
                            iconColor = TextMuted,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onUnfollow()
                            }
                        )
                        
                        OptionItem(
                            icon = Icons.Outlined.Flag,
                            title = "Reportar",
                            subtitle = "Denunciar contenido inapropiado",
                            iconColor = Color(0xFFEF4444),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onReport()
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    badge: String? = null,
    badgeColor: Color = Color(0xFFFF6B35),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
