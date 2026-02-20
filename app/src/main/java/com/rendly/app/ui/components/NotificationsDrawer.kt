package com.rendly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, SALE, MENTION, SYSTEM
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val userAvatar: String?,
    val userName: String?,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val productImage: String? = null
)

private val DEMO_NOTIFICATIONS = listOf(
    AppNotification("1", NotificationType.LIKE, "https://i.pravatar.cc/150?img=1", "María García", "le gustó tu Publicación", "2m", false, "https://picsum.photos/100"),
    AppNotification("2", NotificationType.COMMENT, "https://i.pravatar.cc/150?img=2", "Juan Pérez", "comentó: \"¡Me encanta!\"", "15m", false),
    AppNotification("3", NotificationType.FOLLOW, "https://i.pravatar.cc/150?img=3", "Ana López", "comenzó a seguirte", "1h", false),
    AppNotification("4", NotificationType.SALE, "https://i.pravatar.cc/150?img=4", "Carlos Ruiz", "compró tu producto", "3h", true, "https://picsum.photos/101"),
    AppNotification("5", NotificationType.MENTION, "https://i.pravatar.cc/150?img=5", "Laura Martín", "te mencionó en un comentario", "5h", true),
    AppNotification("6", NotificationType.LIKE, "https://i.pravatar.cc/150?img=6", "Diego Sánchez", "y 5 Más les gustó tu Publicación", "1d", true, "https://picsum.photos/102"),
    AppNotification("7", NotificationType.SYSTEM, null, "Merqora", "Tu cuenta ha sido verificada ?", "2d", true)
)

@Composable
fun NotificationsDrawer(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val notifications = remember { DEMO_NOTIFICATIONS }
    val unreadCount = notifications.count { !it.isRead }
    
    // Control interno de visibilidad para animación fluida
    var internalVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            internalVisible = true
        }
    }
    
    // Backdrop con fade
    AnimatedVisibility(
        visible = internalVisible && isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )
    }
    
    // Drawer desde la derecha - animación Rápida sin rebote
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(250)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(200)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.88f),
                color = HomeBg,
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Cerrar",
                                    tint = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notificaciones",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(PrimaryPurple)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$unreadCount",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        TextButton(onClick = { /* Mark all as read */ }) {
                            Text(
                                text = "Leer todo",
                                color = PrimaryPurple,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Filter tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = "Todas",
                            color = PrimaryPurple
                        )
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = "Ventas",
                            color = AccentGreen
                        )
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = "Menciones",
                            color = AccentPink
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Notifications list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        // Today section
                        item {
                            Text(
                                text = "HOY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(notifications.take(3)) { notification ->
                            NotificationRow(
                                notification = notification,
                                onClick = { onNotificationClick(notification) }
                            )
                        }
                        
                        // Earlier section
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ANTERIORES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(notifications.drop(3)) { notification ->
                            NotificationRow(
                                notification = notification,
                                onClick = { onNotificationClick(notification) }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    color: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) color.copy(alpha = 0.15f) else Color.Transparent,
        border = if (!selected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) color else TextMuted,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        NotificationType.LIKE -> Icons.Default.Favorite
        NotificationType.COMMENT -> Icons.Default.MailOutline
        NotificationType.FOLLOW -> Icons.Default.Person
        NotificationType.SALE -> Icons.Default.ShoppingCart
        NotificationType.MENTION -> Icons.Default.Email
        NotificationType.SYSTEM -> Icons.Default.Notifications
    }
    
    val iconColor = when (notification.type) {
        NotificationType.LIKE -> AccentPink
        NotificationType.COMMENT -> PrimaryPurple
        NotificationType.FOLLOW -> Color(0xFF1565A0)
        NotificationType.SALE -> AccentGreen
        NotificationType.MENTION -> Color(0xFFFF6B35)
        NotificationType.SYSTEM -> PrimaryPurple
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!notification.isRead) {
                    Modifier.background(
                        PrimaryPurple.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar or icon
        Box(modifier = Modifier.size(48.dp)) {
            if (notification.userAvatar != null) {
                AsyncImage(
                    model = notification.userAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryPurple, AccentPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Type indicator
            if (notification.userAvatar != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(HomeBg)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(iconColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    notification.userName?.let {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(it)
                        }
                        append(" ")
                    }
                    append(notification.message)
                },
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = notification.timestamp,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
        
        // Product image if exists
        notification.productImage?.let { imageUrl ->
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        // Unread indicator
        if (!notification.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple)
            )
        }
    }
}
