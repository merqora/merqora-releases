package com.rendly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import com.rendly.app.data.repository.FollowersRepository
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Notification
import com.rendly.app.data.model.NotificationType
import com.rendly.app.data.repository.NotificationRepository
import com.rendly.app.data.repository.SystemNotificationRepository
import com.rendly.app.data.repository.SystemNotification
import com.rendly.app.ui.screens.benefits.ClientBenefitsScreen
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

/* -------------------- MODELOS -------------------- */

enum class OptimizedNotificationType {
    LIKE,
    SAVE,
    COMMENT,
    FOLLOW,
    PURCHASE,
    SALE,
    MENTION,
    SYSTEM,
    CLIENT_REQUEST,
    CLIENT_ACCEPTED,
    CLIENT_REJECTED,
    CLIENT_PENDING
}

data class NotificationItem(
    val id: String,
    val type: OptimizedNotificationType,
    val username: String,
    val avatarUrl: String?,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val postImage: String? = null,
    val dayGroup: String = "Hoy",
    val senderId: String = "",
    val customMessage: String? = null,
    val extraData: String? = null // Para motivo de rechazo, etc.
)

// Helper function to format time ago
private fun formatTimeAgo(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val days = ChronoUnit.DAYS.between(instant, now)
        
        when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace ${minutes}m"
            hours < 24 -> "Hace ${hours}h"
            days == 1L -> "Ayer"
            days < 7 -> "Hace ${days} días"
            else -> "Hace ${days / 7} sem"
        }
    } catch (e: Exception) {
        "Reciente"
    }
}

// Helper function to get day group
private fun getDayGroup(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val now = Instant.now()
        val days = ChronoUnit.DAYS.between(instant, now)
        
        when {
            days == 0L -> "Hoy"
            days == 1L -> "Ayer"
            days < 7 -> "Esta semana"
            days < 30 -> "Este mes"
            else -> "Anteriores"
        }
    } catch (e: Exception) {
        "Reciente"
    }
}

/* -------------------- DRAWER -------------------- */

@Composable
fun OptimizedNotificationsDrawer(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(1f, visibilityThreshold = 0.001f) }
    val velocityTracker = remember { VelocityTracker() }

    val backdropAlpha = (1f - offsetX.value).coerceIn(0f, 1f)

    // Entrada inmediata tipo Instagram
    LaunchedEffect(isVisible) {
        if (isVisible) {
            offsetX.snapTo(1f)
            offsetX.animateTo(0f, tween(140, easing = LinearOutSlowInEasing))
        } else {
            offsetX.animateTo(1f, tween(120, easing = LinearOutSlowInEasing))
        }
    }

    if (isVisible || offsetX.value < 1f) {
        Box(modifier = modifier.fillMaxSize()) {

            /* ---------- BACKDROP ---------- */
            if (backdropAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = backdropAlpha * 0.5f }
                        .background(Color.Black)
                        .clickable { onDismiss() }
                )
            }

            /* ---------- DRAWER ---------- */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX.value * size.width
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                if (dragAmount > 0f) {
                                    velocityTracker.addPosition(
                                        change.uptimeMillis,
                                        Offset(change.position.x, change.position.y)
                                    )

                                    scope.launch {
                                        offsetX.stop()
                                        val newValue =
                                            (offsetX.value + dragAmount / size.width)
                                                .coerceIn(0f, 1f)
                                        offsetX.snapTo(newValue)
                                    }
                                }
                            },
                            onDragEnd = {
                                val velocity = velocityTracker.calculateVelocity().x
                                val shouldDismiss =
                                    velocity > 1200f ||
                                    (velocity > 600f && offsetX.value > 0.18f) ||
                                    offsetX.value > 0.42f

                                scope.launch {
                                    if (shouldDismiss) {
                                        offsetX.animateTo(1f, tween(110, easing = FastOutLinearInEasing))
                                        onDismiss()
                                    } else {
                                        offsetX.animateTo(0f, tween(150, easing = LinearOutSlowInEasing))
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    offsetX.animateTo(0f, tween(160))
                                }
                            }
                        )
                    }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = HomeBg,
                    shadowElevation = 24.dp
                ) {
                    NotificationsContent(onClose = onDismiss)
                }
            }
        }
    }
}

/* -------------------- CONTENIDO -------------------- */

@Composable
private fun NotificationsContent(onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Cargar notificaciones reales desde Supabase
    val realNotifications by NotificationRepository.notifications.collectAsState()
    val isLoading by NotificationRepository.isLoading.collectAsState()
    val unreadCount by NotificationRepository.unreadCount.collectAsState()
    
    // Notificaciones del sistema (handshakes, penalizaciones, etc.)
    val systemNotifications by SystemNotificationRepository.notifications.collectAsState()
    val systemUnreadCount by SystemNotificationRepository.unreadCount.collectAsState()
    
    // Cargar notificaciones al abrir el drawer
    LaunchedEffect(Unit) {
        NotificationRepository.loadNotifications()
        NotificationRepository.subscribeToRealtime()
        SystemNotificationRepository.refresh()
    }
    
    // Convertir notificaciones reales a formato de UI con agrupación por día
    val notifications = remember(realNotifications) {
        realNotifications.map { notif ->
            NotificationItem(
                id = notif.id,
                type = when (notif.type) {
                    NotificationType.LIKE -> OptimizedNotificationType.LIKE
                    NotificationType.SAVE -> OptimizedNotificationType.SAVE
                    NotificationType.FOLLOW -> OptimizedNotificationType.FOLLOW
                    NotificationType.COMMENT -> OptimizedNotificationType.COMMENT
                    NotificationType.MENTION -> OptimizedNotificationType.MENTION
                    NotificationType.CLIENT_REQUEST -> OptimizedNotificationType.CLIENT_REQUEST
                    NotificationType.CLIENT_ACCEPTED -> OptimizedNotificationType.CLIENT_ACCEPTED
                    NotificationType.CLIENT_REJECTED -> OptimizedNotificationType.CLIENT_REJECTED
                    NotificationType.CLIENT_PENDING -> OptimizedNotificationType.CLIENT_PENDING
                    else -> OptimizedNotificationType.SYSTEM
                },
                username = notif.senderUsername,
                avatarUrl = notif.senderAvatar,
                message = when (notif.type) {
                    NotificationType.LIKE -> "le gustó tu publicación"
                    NotificationType.SAVE -> "guardó tu publicación"
                    NotificationType.FOLLOW -> "comenzó a seguirte"
                    NotificationType.COMMENT -> notif.message ?: "comentó tu publicación"
                    NotificationType.MENTION -> "te mencionó"
                    NotificationType.CLIENT_REQUEST -> "quiere ser tu cliente"
                    NotificationType.CLIENT_ACCEPTED -> notif.message ?: "te aceptó como cliente"
                    NotificationType.CLIENT_REJECTED -> notif.message ?: "no aceptó tu solicitud"
                    NotificationType.CLIENT_PENDING -> notif.message ?: "solicitud pendiente"
                    else -> "nueva actividad"
                },
                timestamp = formatTimeAgo(notif.createdAt),
                isRead = notif.isRead,
                postImage = notif.postImage,
                dayGroup = getDayGroup(notif.createdAt),
                senderId = notif.senderId,
                customMessage = notif.message,
                extraData = notif.extraData
            )
        }
    }
    
    // Convertir notificaciones del SISTEMA a formato de UI
    val systemNotificationItems = remember(systemNotifications) {
        systemNotifications.map { sysNotif ->
            NotificationItem(
                id = "sys_${sysNotif.id}",
                type = OptimizedNotificationType.SYSTEM,
                username = "Sistema",
                avatarUrl = null,
                message = sysNotif.message,
                timestamp = formatTimeAgo(sysNotif.created_at),
                isRead = sysNotif.read,
                postImage = null,
                dayGroup = getDayGroup(sysNotif.created_at),
                senderId = "",
                customMessage = sysNotif.title,
                extraData = sysNotif.type // CONFIRMATION_REMINDER, AUTO_COMPLETED, REPUTATION_PENALTY
            )
        }
    }
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    // Estado para selección múltiple
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedNotifications by remember { mutableStateOf(setOf<String>()) }
    var isDeleting by remember { mutableStateOf(false) }
    
    // Estado para pantalla de beneficios
    var showBenefitsScreen by remember { mutableStateOf(false) }
    var benefitsSellerUsername by remember { mutableStateOf("") }
    var benefitsSellerAvatar by remember { mutableStateOf<String?>(null) }
    
    // Estado para vista de solicitudes
    var showSolicitudesView by remember { mutableStateOf(false) }
    
    // Separar notificaciones de solicitudes de las normales
    val solicitudesTypes = listOf(
        OptimizedNotificationType.CLIENT_REQUEST,
        OptimizedNotificationType.CLIENT_ACCEPTED,
        OptimizedNotificationType.CLIENT_REJECTED,
        OptimizedNotificationType.CLIENT_PENDING
    )
    
    val solicitudesNotifications = remember(notifications) {
        notifications.filter { it.type in solicitudesTypes }
    }
    
    val normalNotifications = remember(notifications) {
        notifications.filter { it.type !in solicitudesTypes }
    }
    
    // Combinar notificaciones normales con las del sistema
    val allNormalNotifications = remember(normalNotifications, systemNotificationItems) {
        (normalNotifications + systemNotificationItems).sortedByDescending { it.timestamp }
    }
    
    // Agrupar notificaciones por día (normales + sistema)
    val groupedNotifications = remember(allNormalNotifications) {
        allNormalNotifications.groupBy { it.dayGroup }
    }
    
    // Agrupar solicitudes por día
    val groupedSolicitudes = remember(solicitudesNotifications) {
        solicitudesNotifications.groupBy { it.dayGroup }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            .statusBarsPadding()
    ) {
        /* ---------- VISTA DE SOLICITUDES (PANTALLA COMPLETA) ---------- */
        if (showSolicitudesView) {
            SolicitudesView(
                solicitudes = solicitudesNotifications,
                groupedSolicitudes = groupedSolicitudes,
                isSelectionMode = isSelectionMode,
                selectedNotifications = selectedNotifications,
                onBack = { showSolicitudesView = false },
                onClose = onClose,
                onSelectNotification = { id ->
                    selectedNotifications = if (selectedNotifications.contains(id)) {
                        selectedNotifications - id
                    } else {
                        selectedNotifications + id
                    }
                },
                onLongPress = { id ->
                    isSelectionMode = true
                    selectedNotifications = setOf(id)
                },
                onAccept = { notification ->
                    scope.launch {
                        android.widget.Toast.makeText(context, "DEBUG: Aceptando ${notification.senderId.take(8)}...", android.widget.Toast.LENGTH_SHORT).show()
                        val result = FollowersRepository.acceptClientRequest(notification.senderId)
                        if (result.isSuccess) {
                            android.widget.Toast.makeText(context, "✓ Cliente aceptado!", android.widget.Toast.LENGTH_SHORT).show()
                            NotificationRepository.createClientAcceptedNotification(
                                requesterId = notification.senderId,
                                sellerUsername = notification.username
                            )
                        } else {
                            android.widget.Toast.makeText(context, "✖ Error: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                        NotificationRepository.markAsRead(notification.id)
                        NotificationRepository.deleteNotification(notification.id)
                    }
                },
                onReject = { notification, reason ->
                    scope.launch {
                        FollowersRepository.rejectClientRequest(notification.senderId)
                        NotificationRepository.createClientRejectedNotification(
                            requesterId = notification.senderId,
                            sellerUsername = notification.username,
                            reason = reason
                        )
                        NotificationRepository.markAsRead(notification.id)
                        NotificationRepository.deleteNotification(notification.id)
                    }
                },
                onViewBenefits = { notification ->
                    benefitsSellerUsername = notification.username
                    benefitsSellerAvatar = notification.avatarUrl
                    showBenefitsScreen = true
                },
                onMarkAsRead = { id ->
                    scope.launch { NotificationRepository.markAsRead(id) }
                },
                onCancelSelection = {
                    isSelectionMode = false
                    selectedNotifications = emptySet()
                },
                onDeleteSelected = {
                    scope.launch {
                        isDeleting = true
                        selectedNotifications.forEach { id ->
                            NotificationRepository.deleteNotification(id)
                        }
                        isDeleting = false
                        isSelectionMode = false
                        selectedNotifications = emptySet()
                    }
                },
                isDeleting = isDeleting
            )
        } else {
        /* ---------- HEADER MEJORADO ---------- */
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HomeBg.copy(alpha = 0.95f),
            shadowElevation = 2.dp
        ) {
            if (isSelectionMode) {
                // Header de selección múltiple
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón cerrar selección
                        IconButton(
                            onClick = { 
                                isSelectionMode = false
                                selectedNotifications = emptySet()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = TextPrimary
                            )
                        }
                        
                        Text(
                            text = "${selectedNotifications.size} seleccionadas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Seleccionar todas
                        TextButton(
                            onClick = {
                                selectedNotifications = if (selectedNotifications.size == notifications.size) {
                                    emptySet()
                                } else {
                                    notifications.map { it.id }.toSet()
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedNotifications.size == notifications.size) "Ninguna" else "Todas",
                                color = PrimaryPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Eliminar seleccionadas
                        Button(
                            onClick = {
                                scope.launch {
                                    isDeleting = true
                                    selectedNotifications.forEach { id ->
                                        NotificationRepository.deleteNotification(id)
                                    }
                                    isDeleting = false
                                    isSelectionMode = false
                                    selectedNotifications = emptySet()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            ),
                            enabled = selectedNotifications.isNotEmpty() && !isDeleting,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Eliminar")
                            }
                        }
                    }
                }
            } else {
                // Header normal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Surface.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Cerrar",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Notificaciones",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isLoading) "Cargando..." else if (isRefreshing) "Actualizando..." else "$unreadCount nuevas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                        }
                    }
                    
                    // Botón de marcar todo como leído
                    IconButton(
                        onClick = { 
                            scope.launch { NotificationRepository.markAllAsRead() }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentPink.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Marcar como leído",
                            tint = AccentPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Divider sutil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        /* ---------- BOTÓN SOLICITUDES ---------- */
        if (!isSelectionMode && !showSolicitudesView) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSolicitudesView = true },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            tint = Color(0xFF1565A0),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Solicitudes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    // Contador de solicitudes pendientes
                    val pendingCount = solicitudesNotifications.count { 
                        it.type == OptimizedNotificationType.CLIENT_REQUEST && !it.isRead 
                    }
                    if (pendingCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1565A0)
                        ) {
                            Text(
                                text = if (pendingCount > 99) "99+" else pendingCount.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Divider después del botón
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = BorderSubtle
            )
        }
        
        /* ---------- LISTA CON PULL-TO-REFRESH MANUAL ---------- */
        var pullOffset by remember { mutableFloatStateOf(0f) }
        val canPullRefresh = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(canPullRefresh) {
                    if (canPullRefresh) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (pullOffset > 100f && !isRefreshing) {
                                    scope.launch {
                                        isRefreshing = true
                                        NotificationRepository.loadNotifications()
                                        kotlinx.coroutines.delay(500)
                                        isRefreshing = false
                                    }
                                }
                                pullOffset = 0f
                            },
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount > 0 && canPullRefresh) {
                                    pullOffset = (pullOffset + dragAmount).coerceIn(0f, 150f)
                                }
                            }
                        )
                    }
                }
        ) {
            // Indicador de pull-to-refresh
            if (pullOffset > 0f || isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryPurple,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (pullOffset > 100f) Icons.Default.Refresh else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (pullOffset > 100f) PrimaryPurple else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            if (notifications.isEmpty() && !isLoading) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin notificaciones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Desliza hacia abajo para actualizar",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    // Mostrar notificaciones agrupadas por día
                    val dayOrder = listOf("Hoy", "Ayer", "Esta semana", "Este mes", "Anteriores", "Reciente")
                    dayOrder.forEach { dayGroup ->
                        val dayNotifications = groupedNotifications[dayGroup] ?: emptyList()
                        if (dayNotifications.isNotEmpty()) {
                            item(key = "header_$dayGroup") {
                                DayHeader(title = dayGroup)
                            }
                            items(dayNotifications, key = { it.id }) { notification ->
                                val isSelected = selectedNotifications.contains(notification.id)
                                
                                // Usar vista especial para solicitudes de cliente
                                if (notification.type == OptimizedNotificationType.CLIENT_REQUEST) {
                                    ClientRequestNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = {
                                            selectedNotifications = if (isSelected) {
                                                selectedNotifications - notification.id
                                            } else {
                                                selectedNotifications + notification.id
                                            }
                                        },
                                        onAccept = {
                                            scope.launch {
                                                android.widget.Toast.makeText(context, "DEBUG: Aceptando ${notification.senderId.take(8)}...", android.widget.Toast.LENGTH_SHORT).show()
                                                val result = FollowersRepository.acceptClientRequest(notification.senderId)
                                                if (result.isSuccess) {
                                                    android.widget.Toast.makeText(context, "✓ Cliente aceptado!", android.widget.Toast.LENGTH_SHORT).show()
                                                    NotificationRepository.createClientAcceptedNotification(
                                                        requesterId = notification.senderId,
                                                        sellerUsername = notification.username
                                                    )
                                                } else {
                                                    android.widget.Toast.makeText(context, "✖ Error: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                                NotificationRepository.markAsRead(notification.id)
                                                NotificationRepository.deleteNotification(notification.id)
                                            }
                                        },
                                        onReject = { reason ->
                                            scope.launch {
                                                FollowersRepository.rejectClientRequest(notification.senderId)
                                                NotificationRepository.createClientRejectedNotification(
                                                    requesterId = notification.senderId,
                                                    sellerUsername = notification.username,
                                                    reason = reason
                                                )
                                                NotificationRepository.markAsRead(notification.id)
                                                NotificationRepository.deleteNotification(notification.id)
                                            }
                                        },
                                        onViewHistory = {
                                            // TODO: Navigate to user history
                                        },
                                        onLongPress = {
                                            isSelectionMode = true
                                            selectedNotifications = setOf(notification.id)
                                        }
                                    )
                                } else if (notification.type == OptimizedNotificationType.CLIENT_ACCEPTED) {
                                    ClientAcceptedNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = {
                                            selectedNotifications = if (isSelected) {
                                                selectedNotifications - notification.id
                                            } else {
                                                selectedNotifications + notification.id
                                            }
                                        },
                                        onMarkAsRead = {
                                            scope.launch { NotificationRepository.markAsRead(notification.id) }
                                        },
                                        onLongPress = {
                                            isSelectionMode = true
                                            selectedNotifications = setOf(notification.id)
                                        },
                                        onViewBenefits = {
                                            benefitsSellerUsername = notification.username
                                            benefitsSellerAvatar = notification.avatarUrl
                                            showBenefitsScreen = true
                                        }
                                    )
                                } else if (notification.type == OptimizedNotificationType.CLIENT_REJECTED) {
                                    ClientRejectedNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = {
                                            selectedNotifications = if (isSelected) {
                                                selectedNotifications - notification.id
                                            } else {
                                                selectedNotifications + notification.id
                                            }
                                        },
                                        onMarkAsRead = {
                                            scope.launch { NotificationRepository.markAsRead(notification.id) }
                                        },
                                        onLongPress = {
                                            isSelectionMode = true
                                            selectedNotifications = setOf(notification.id)
                                        }
                                    )
                                } else if (notification.type == OptimizedNotificationType.CLIENT_PENDING) {
                                    ClientPendingNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = {
                                            selectedNotifications = if (isSelected) {
                                                selectedNotifications - notification.id
                                            } else {
                                                selectedNotifications + notification.id
                                            }
                                        },
                                        onMarkAsRead = {
                                            scope.launch { NotificationRepository.markAsRead(notification.id) }
                                        },
                                        onLongPress = {
                                            isSelectionMode = true
                                            selectedNotifications = setOf(notification.id)
                                        }
                                    )
                                } else {
                                    NotificationItemView(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = {
                                            selectedNotifications = if (isSelected) {
                                                selectedNotifications - notification.id
                                            } else {
                                                selectedNotifications + notification.id
                                            }
                                        },
                                        onMarkAsRead = {
                                            scope.launch { NotificationRepository.markAsRead(notification.id) }
                                        },
                                        onLongPress = {
                                            isSelectionMode = true
                                            selectedNotifications = setOf(notification.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        } // Cierre del else de showSolicitudesView
        
        // Pantalla de beneficios VIP
        ClientBenefitsScreen(
            isVisible = showBenefitsScreen,
            sellerUsername = benefitsSellerUsername,
            sellerAvatar = benefitsSellerAvatar,
            onDismiss = { showBenefitsScreen = false }
        )
    }
}

/* -------------------- DAY HEADER -------------------- */

@Composable
private fun DayHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

/* -------------------- ITEM -------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationItemView(
    notification: NotificationItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onMarkAsRead: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { 
                    if (isSelectionMode) onSelect() else onMarkAsRead() 
                },
                onLongClick = { onLongPress() }
            ),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) 
                else if (!notification.isRead) Surface.copy(alpha = 0.3f) 
                else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox de selección
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryPurple,
                        uncheckedColor = TextMuted
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            // Avatar real o fallback
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            OptimizedNotificationType.LIKE -> AccentPink.copy(alpha = 0.15f)
                            OptimizedNotificationType.COMMENT -> PrimaryPurple.copy(alpha = 0.15f)
                            OptimizedNotificationType.FOLLOW -> AccentGreen.copy(alpha = 0.15f)
                            OptimizedNotificationType.PURCHASE -> AccentGreen.copy(alpha = 0.15f)
                            OptimizedNotificationType.SALE -> AccentGreen.copy(alpha = 0.15f)
                            OptimizedNotificationType.MENTION -> PrimaryPurple.copy(alpha = 0.15f)
                            OptimizedNotificationType.SYSTEM -> TextMuted.copy(alpha = 0.15f)
                            OptimizedNotificationType.SAVE -> Color(0xFFFF6B35).copy(alpha = 0.15f)
                            OptimizedNotificationType.CLIENT_REQUEST -> Color(0xFF1565A0).copy(alpha = 0.15f)
                            OptimizedNotificationType.CLIENT_ACCEPTED -> Color(0xFF2E8B57).copy(alpha = 0.15f)
                            OptimizedNotificationType.CLIENT_REJECTED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            OptimizedNotificationType.CLIENT_PENDING -> Color(0xFFFF6B35).copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!notification.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = notification.avatarUrl,
                        contentDescription = "Avatar de ${notification.username}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = if (notification.type == OptimizedNotificationType.SYSTEM) "R" 
                               else notification.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (notification.type) {
                            OptimizedNotificationType.LIKE -> AccentPink
                            OptimizedNotificationType.COMMENT -> PrimaryPurple
                            OptimizedNotificationType.FOLLOW -> AccentGreen
                            OptimizedNotificationType.PURCHASE -> AccentGreen
                            OptimizedNotificationType.SALE -> AccentGreen
                            OptimizedNotificationType.MENTION -> PrimaryPurple
                            OptimizedNotificationType.SYSTEM -> TextMuted
                            OptimizedNotificationType.SAVE -> Color(0xFFFF6B35)
                            OptimizedNotificationType.CLIENT_REQUEST -> Color(0xFF1565A0)
                            OptimizedNotificationType.CLIENT_ACCEPTED -> Color(0xFF2E8B57)
                            OptimizedNotificationType.CLIENT_REJECTED -> Color(0xFFEF4444)
                            OptimizedNotificationType.CLIENT_PENDING -> Color(0xFFFF6B35)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido central
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.username,
                        fontWeight = if (!notification.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = " ${notification.message}",
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = notification.timestamp,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Post preview (si existe)
            if (!notification.postImage.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface)
                ) {
                    AsyncImage(
                        model = notification.postImage,
                        contentDescription = "Preview del post",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Indicador de no leído (puntito)
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AccentPink)
                )
            }
        }
    }
}

/* -------------------- CLIENT REQUEST NOTIFICATION (COMPACTA) -------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientRequestNotificationItem(
    notification: NotificationItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onAccept: () -> Unit,
    onReject: (String) -> Unit,
    onViewHistory: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = { if (isSelectionMode) onSelect() },
                onLongClick = { onLongPress() }
            ),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color(0xFF1565A0).copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header compacto
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar pequeño
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1565A0).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!notification.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = notification.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = notification.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565A0)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${notification.username}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1565A0).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "CLIENTE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565A0),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Quiere ser tu cliente • ${notification.timestamp}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    // Mostrar mensaje completo si existe
                    if (!notification.customMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${notification.customMessage}\"",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Botones compactos en una fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Historial - solo icono
                OutlinedButton(
                    onClick = onViewHistory,
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextMuted
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Ver historial",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Rechazar
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    ),
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechazar", fontSize = 12.sp, maxLines = 1)
                }
                
                // Aceptar
                Button(
                    onClick = {
                        isProcessing = true
                        onAccept()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E8B57)
                    ),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aceptar", fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
            
            // Checkbox de selección en modo selección
            if (isSelectionMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PrimaryPurple,
                            uncheckedColor = TextMuted
                        )
                    )
                }
            }
        }
    }
    
    // Diálogo de rechazo
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            containerColor = Surface,
            title = {
                Text(
                    "Rechazar solicitud",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        "¿Por qué rechazas a @${notification.username}? (opcional)",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Escribe un motivo...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessing = true
                        showRejectDialog = false
                        onReject(rejectReason)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}

/* -------------------- CLIENT ACCEPTED NOTIFICATION -------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientAcceptedNotificationItem(
    notification: NotificationItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onMarkAsRead: () -> Unit,
    onLongPress: () -> Unit = {},
    onViewBenefits: () -> Unit = {}
) {
    var showBenefitsSheet by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = { if (isSelectionMode) onSelect() else onMarkAsRead() },
                onLongClick = { onLongPress() }
            ),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color(0xFF2E8B57).copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Success icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8B57).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = Color(0xFF2E8B57),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "¡Felicidades!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E8B57)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E8B57).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "VIP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E8B57),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "@${notification.username} ${notification.message}",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = notification.timestamp,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E8B57))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Benefits preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Ahora tienes acceso a:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BenefitChip(icon = Icons.Outlined.LocalOffer, text = "Descuentos")
                        BenefitChip(icon = Icons.Outlined.Star, text = "Exclusivos")
                        BenefitChip(icon = Icons.Outlined.Bolt, text = "Prioridad")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onViewBenefits() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E8B57)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver todos mis beneficios", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun BenefitChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2E8B57),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

/* -------------------- CLIENT REJECTED NOTIFICATION -------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientRejectedNotificationItem(
    notification: NotificationItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onMarkAsRead: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    var showReasonDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = { if (isSelectionMode) onSelect() else onMarkAsRead() },
                onLongClick = { onLongPress() }
            ),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rejected icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Solicitud no aceptada",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "@${notification.username} ${notification.message}",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = notification.timestamp,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                }
            }
            
            // Mostrar botón de motivo solo si hay extraData
            if (!notification.extraData.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = { showReasonDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver motivo del rechazo", fontSize = 14.sp)
                }
            }
        }
    }
    
    // Diálogo con el motivo
    if (showReasonDialog && !notification.extraData.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showReasonDialog = false },
            containerColor = Surface,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Motivo del rechazo",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "@${notification.username} indicó:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFEF4444).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "\"${notification.extraData}\"",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReasonDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

/* -------------------- CLIENT PENDING NOTIFICATION -------------------- */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientPendingNotificationItem(
    notification: NotificationItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onMarkAsRead: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = { if (isSelectionMode) onSelect() else onMarkAsRead() },
                onLongClick = { onLongPress() }
            ),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color(0xFFFF6B35).copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pending icon con animación
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B35).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Solicitud Enviada",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B35)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF6B35).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Pendiente",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B35),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notification.message,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.timestamp,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6B35))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Info box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFF6B35).copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "El vendedor revisará tu solicitud y te notificará cuando sea aceptada.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   VISTA DE SOLICITUDES - Componente separado
═══════════════════════════════════════════════════════════════ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SolicitudesView(
    solicitudes: List<NotificationItem>,
    groupedSolicitudes: Map<String, List<NotificationItem>>,
    isSelectionMode: Boolean,
    selectedNotifications: Set<String>,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onSelectNotification: (String) -> Unit,
    onLongPress: (String) -> Unit,
    onAccept: (NotificationItem) -> Unit,
    onReject: (NotificationItem, String) -> Unit,
    onViewBenefits: (NotificationItem) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    isDeleting: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header de solicitudes
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HomeBg.copy(alpha = 0.95f),
            shadowElevation = 2.dp
        ) {
            if (isSelectionMode) {
                // Header de selección múltiple
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = onCancelSelection,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = TextPrimary
                            )
                        }
                        
                        Text(
                            text = "${selectedNotifications.size} seleccionadas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    Button(
                        onClick = onDeleteSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        enabled = selectedNotifications.isNotEmpty() && !isDeleting,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Eliminar")
                        }
                    }
                }
            } else {
                // Header normal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Surface.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Solicitudes",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${solicitudes.count { !it.isRead }} pendientes",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                        }
                    }
                    
                    // Badge con total
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1565A0).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${solicitudes.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1565A0),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Divider sutil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        if (solicitudes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAdd,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sin solicitudes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Las solicitudes de clientes aparecerán aquí",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                val dayOrder = listOf("Hoy", "Ayer", "Esta semana", "Este mes", "Anteriores", "Reciente")
                dayOrder.forEach { dayGroup ->
                    val dayNotifications = groupedSolicitudes[dayGroup] ?: emptyList()
                    if (dayNotifications.isNotEmpty()) {
                        item(key = "solicitudes_header_$dayGroup") {
                            DayHeader(title = dayGroup)
                        }
                        items(dayNotifications, key = { "sol_${it.id}" }) { notification ->
                            val isSelected = selectedNotifications.contains(notification.id)
                            
                            when (notification.type) {
                                OptimizedNotificationType.CLIENT_REQUEST -> {
                                    ClientRequestNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = { onSelectNotification(notification.id) },
                                        onAccept = { onAccept(notification) },
                                        onReject = { reason -> onReject(notification, reason) },
                                        onViewHistory = { },
                                        onLongPress = { onLongPress(notification.id) }
                                    )
                                }
                                OptimizedNotificationType.CLIENT_ACCEPTED -> {
                                    ClientAcceptedNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = { onSelectNotification(notification.id) },
                                        onMarkAsRead = { onMarkAsRead(notification.id) },
                                        onLongPress = { onLongPress(notification.id) },
                                        onViewBenefits = { onViewBenefits(notification) }
                                    )
                                }
                                OptimizedNotificationType.CLIENT_REJECTED -> {
                                    ClientRejectedNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = { onSelectNotification(notification.id) },
                                        onMarkAsRead = { onMarkAsRead(notification.id) },
                                        onLongPress = { onLongPress(notification.id) }
                                    )
                                }
                                OptimizedNotificationType.CLIENT_PENDING -> {
                                    ClientPendingNotificationItem(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = { onSelectNotification(notification.id) },
                                        onMarkAsRead = { onMarkAsRead(notification.id) },
                                        onLongPress = { onLongPress(notification.id) }
                                    )
                                }
                                else -> {
                                    NotificationItemView(
                                        notification = notification,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelect = { onSelectNotification(notification.id) },
                                        onMarkAsRead = { onMarkAsRead(notification.id) },
                                        onLongPress = { onLongPress(notification.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
