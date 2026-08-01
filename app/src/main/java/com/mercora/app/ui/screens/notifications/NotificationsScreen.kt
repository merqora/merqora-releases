package com.mercora.app.ui.screens.notifications

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.mercora.app.data.model.Notification
import com.mercora.app.data.model.NotificationType
import com.mercora.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (Notification) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filters = listOf("Todas", "Likes", "Comentarios", "Seguidores", "Ventas")
    
    val filteredNotifications = when (selectedFilter) {
        "Likes" -> notifications.filter { it.type == NotificationType.LIKE || it.type == NotificationType.SAVE }
        "Comentarios" -> notifications.filter { it.type == NotificationType.COMMENT || it.type == NotificationType.MENTION }
        "Seguidores" -> notifications.filter { it.type == NotificationType.FOLLOW || it.type == NotificationType.CLIENT_REQUEST || it.type == NotificationType.CLIENT_ACCEPTED || it.type == NotificationType.CLIENT_REJECTED || it.type == NotificationType.CLIENT_PENDING }
        "Ventas" -> notifications.filter { it.type == NotificationType.CLIENT_ACCEPTED }
        else -> notifications
    }
    
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
            
            IconButton(onClick = { viewModel.markAllAsRead() }) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Marcar todo como leído",
                    tint = PrimaryPurple
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.selectFilter(filter) },
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
        
        if (isLoading && notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin notificaciones",
                    fontSize = 16.sp,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filteredNotifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = { onNotificationClick(notification) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val icon: ImageVector
    val iconColor: Color
    
    when (notification.type) {
        NotificationType.LIKE -> {
            icon = Icons.Default.Favorite
            iconColor = AccentPink
        }
        NotificationType.SAVE -> {
            icon = Icons.Default.Bookmark
            iconColor = AccentYellow
        }
        NotificationType.COMMENT -> {
            icon = Icons.Default.Chat
            iconColor = AccentBlue
        }
        NotificationType.MENTION -> {
            icon = Icons.Default.AlternateEmail
            iconColor = AccentGold
        }
        NotificationType.FOLLOW -> {
            icon = Icons.Default.Person
            iconColor = PrimaryPurple
        }
        NotificationType.CLIENT_REQUEST,
        NotificationType.CLIENT_PENDING -> {
            icon = Icons.Default.PersonAdd
            iconColor = AccentBlue
        }
        NotificationType.CLIENT_ACCEPTED -> {
            icon = Icons.Default.ShoppingCart
            iconColor = AccentGreen
        }
        NotificationType.CLIENT_REJECTED -> {
            icon = Icons.Default.Close
            iconColor = Color(0xFFEF4444)
        }
        NotificationType.FOLLOW_REQUEST -> {
            icon = Icons.Default.PersonAdd
            iconColor = AccentBlue
        }
        NotificationType.FOLLOW_ACCEPTED -> {
            icon = Icons.Default.Check
            iconColor = AccentGreen
        }
        NotificationType.FOLLOW_REJECTED -> {
            icon = Icons.Default.Close
            iconColor = Color(0xFFEF4444)
        }
        NotificationType.UNKNOWN -> {
            icon = Icons.Default.Notifications
            iconColor = TextMuted
        }
    }
    
    val displayMessage = notification.message ?: when (notification.type) {
        NotificationType.LIKE -> "le gustó tu publicación"
        NotificationType.SAVE -> "guardó tu publicación"
        NotificationType.COMMENT -> "comentó tu publicación"
        NotificationType.MENTION -> "te mencionó"
        NotificationType.FOLLOW -> "comenzó a seguirte"
        NotificationType.CLIENT_REQUEST -> "quiere ser tu cliente"
        NotificationType.CLIENT_ACCEPTED -> "aceptó ser tu cliente"
        NotificationType.CLIENT_REJECTED -> "rechazó ser tu cliente"
        NotificationType.CLIENT_PENDING -> "tiene solicitud pendiente"
        NotificationType.FOLLOW_REQUEST -> "quiere seguirte"
        NotificationType.FOLLOW_ACCEPTED -> "aceptó tu solicitud de seguimiento"
        NotificationType.FOLLOW_REJECTED -> "rechazó tu solicitud de seguimiento"
        NotificationType.UNKNOWN -> ""
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (!notification.isRead) PrimaryPurple.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (notification.senderAvatar != null) {
                AsyncImage(
                    model = notification.senderAvatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
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
                    text = notification.senderUsername,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = displayMessage,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = formatNotificationTime(notification.createdAt),
                fontSize = 12.sp,
                color = TextMuted
            )
        }
        
        if (notification.postImage != null) {
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = notification.postImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        
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

private fun formatNotificationTime(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(createdAt) ?: return createdAt
        val now = Date()
        val diff = now.time - date.time
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        when {
            seconds < 60 -> "Ahora"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours h"
            days < 7 -> "Hace $days d"
            else -> {
                val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.US)
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        createdAt.take(10)
    }
}
