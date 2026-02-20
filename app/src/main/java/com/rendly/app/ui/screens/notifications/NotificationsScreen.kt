package com.rendly.app.ui.screens.notifications

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, SALE, MENTION, SYSTEM
}

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val avatar: String? = null,
    val time: String,
    val isRead: Boolean = false
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (Notification) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val notifications = remember {
        listOf(
            Notification("1", NotificationType.LIKE, "María García", "le gustó tu publicación", null, "Hace 2 min"),
            Notification("2", NotificationType.COMMENT, "Carlos López", "comentó: \"¡Me encanta!\"", null, "Hace 15 min"),
            Notification("3", NotificationType.FOLLOW, "Ana Martínez", "comenzó a seguirte", null, "Hace 1 hora"),
            Notification("4", NotificationType.SALE, "Nueva venta", "Vendiste \"Remera Vintage\"", null, "Hace 2 horas", true),
            Notification("5", NotificationType.MENTION, "Pedro Ruiz", "te mencionó en un comentario", null, "Hace 3 horas", true),
            Notification("6", NotificationType.SYSTEM, "Merqora", "Tu cuenta fue verificada ✓", null, "Ayer", true)
        )
    }
    
    var selectedFilter by remember { mutableStateOf("Todas") }
    val filters = listOf("Todas", "Likes", "Comentarios", "Seguidores", "Ventas")
    
    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(200),
        label = "alpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
            .alpha(alpha)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            
            Text(
                text = "Notificaciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            IconButton(onClick = { /* Mark all as read */ }) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Marcar todo como leído",
                    tint = PrimaryPurple
                )
            }
        }
        
        // Filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple.copy(alpha = 0.2f),
                        selectedLabelColor = PrimaryPurple,
                        containerColor = Surface,
                        labelColor = TextSecondary
                    ),
                    border = null
                )
            }
        }
        
        // Lista de notificaciones
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(notifications, key = { it.id }) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = { onNotificationClick(notification) }
                )
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
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
        NotificationType.COMMENT -> AccentBlue
        NotificationType.FOLLOW -> PrimaryPurple
        NotificationType.SALE -> AccentGreen
        NotificationType.MENTION -> AccentGold
        NotificationType.SYSTEM -> TextSecondary
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (!notification.isRead) PrimaryPurple.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar o icono
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (notification.avatar != null) {
                AsyncImage(
                    model = notification.avatar,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = " ${notification.message}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = notification.time,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
        
        // Indicador de no leído
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple)
            )
        }
    }
}
