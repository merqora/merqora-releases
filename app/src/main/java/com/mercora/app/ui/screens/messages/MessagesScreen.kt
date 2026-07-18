package com.mercora.app.ui.screens.messages

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.repository.ChatRepository
import com.mercora.app.data.repository.Conversation
import com.mercora.app.data.repository.HandshakeRepository
import com.mercora.app.data.model.HandshakeEvent
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.screens.chat.ChatScreen
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    onConversationClick: (Usuario) -> Unit = {},
    onNewMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val conversations by ChatRepository.conversations.collectAsState()
    val isLoading by ChatRepository.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Estado para chat activo dentro de MessagesScreen
    var selectedChatUser by remember { mutableStateOf<Usuario?>(null) }
    var selectedChatConversationId by remember { mutableStateOf<String?>(null) }
    
    // Cargar conversaciones + suscribirse a handshakes al inicio
    LaunchedEffect(Unit) {
        ChatRepository.loadConversations()
        
        val userId = try {
            SupabaseClient.auth.currentUserOrNull()?.id
        } catch (_: Exception) { null }
        
        if (userId != null) {
            launch {
                try {
                    HandshakeRepository.subscribeToHandshakes(userId)
                    android.util.Log.d("MessagesScreen", ">>> Subscribed to handshakes for userId=$userId")
                } catch (e: Exception) {
                    android.util.Log.e("MessagesScreen", "Error subscribing: ${e.message}")
                }
            }
        }
    }
    
    // Polling: recargar conversaciones cada 5 segundos para reflejar cambios
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(5000)
            try {
                ChatRepository.loadConversations()
            } catch (_: Exception) {}
        }
    }
    
    // Escuchar eventos de handshake para recargar conversaciones inmediatamente
    LaunchedEffect(Unit) {
        HandshakeRepository.handshakeEvents.collectLatest { event ->
            android.util.Log.d("MessagesScreen", ">>> Handshake event received: $event")
            ChatRepository.loadConversations()
        }
    }
    
    // Pantalla principal de lista de conversaciones
    Box(modifier = modifier.fillMaxSize().background(HomeBg)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mensajes",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNewMessage) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Nuevo mensaje",
                                tint = PrimaryPurple
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HomeBg
                    )
                )
            },
            containerColor = HomeBg
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buscar conversaciones...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Text(
                    text = "MENSAJES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                if (isLoading && conversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else if (conversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sin conversaciones",
                                color = TextMuted,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(conversations, key = { it.id }) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = {
                                    selectedChatUser = conversation.otherUser
                                    selectedChatConversationId = conversation.id
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // ChatScreen overlay cuando se selecciona una conversaciÃ³n
        AnimatedVisibility(
            visible = selectedChatUser != null,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            )
        ) {
            selectedChatUser?.let { user ->
                ChatScreen(
                    otherUser = user,
                    conversationId = selectedChatConversationId,
                    onBack = {
                        selectedChatUser = null
                        selectedChatConversationId = null
                    },
                    onOpenChatList = {
                        selectedChatUser = null
                        selectedChatConversationId = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val hasUnread = conversation.unreadCount > 0
    val user = conversation.otherUser
    val displayName = user.nombreTienda ?: user.nombre ?: user.username
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Estado del menÃº contextual
    var showContextMenu by remember { mutableStateOf(false) }
    
    // Estado local para actualizaciÃ³n inmediata de la UI
    var localIsPinned by remember { mutableStateOf(conversation.isPinned) }
    
    // Sincronizar con datos del servidor cuando cambian
    LaunchedEffect(conversation.isPinned) { localIsPinned = conversation.isPinned }
    
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showContextMenu = true
                    }
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = displayName,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        fontSize = 15.sp,
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    
                    // Icono de fijado
                    if (localIsPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Fijado",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Formatear mensaje de preview
                val displayMessage = formatLastMessagePreview(conversation.lastMessage)
                
                // Detectar color especial para handshake states
                val messageColor = getHandshakeMessageColor(conversation.lastMessage)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayMessage,
                        fontSize = 13.sp,
                        color = messageColor ?: if (hasUnread) TextPrimary else TextMuted,
                        fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // Badge +N mensajes cuando hay mÃ¡s de 1 no leÃ­do
                    if (hasUnread && conversation.unreadCount > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+${conversation.unreadCount} mensajes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Right side
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatMessageTimeAgo(conversation.lastMessageAt),
                    fontSize = 12.sp,
                    color = if (hasUnread) PrimaryPurple else TextMuted
                )
                
                if (hasUnread) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${conversation.unreadCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        // MenÃº contextual con animaciÃ³n fluida
        ConversationContextMenu(
            isVisible = showContextMenu,
            isPinned = localIsPinned,
            onDismiss = { showContextMenu = false },
            onTogglePin = {
                val newPinned = !localIsPinned
                localIsPinned = newPinned
                showContextMenu = false
                scope.launch {
                    ChatRepository.togglePinConversation(conversation.id, newPinned)
                }
            },
            onDelete = {
                scope.launch {
                    ChatRepository.deleteConversation(conversation.id)
                }
                showContextMenu = false
            }
        )
    }
}

@Composable
private fun ConversationContextMenu(
    isVisible: Boolean,
    isPinned: Boolean,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    // Backdrop
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(120))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() }
        )
    }
    
    // Menu con slide-up fluido
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
        ) + fadeIn(tween(100)),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(150)
        ) + fadeOut(tween(100))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = Surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Fijar chat
                ContextMenuItem(
                    icon = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    text = if (isPinned) "Desfijar chat" else "Fijar chat",
                    iconTint = PrimaryPurple,
                    onClick = onTogglePin
                )
                // Eliminar
                ContextMenuItem(
                    icon = Icons.Outlined.Delete,
                    text = "Eliminar chat",
                    iconTint = Color(0xFFEF4444),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

// Formatear preview del Ãºltimo mensaje para la lista de conversaciones
private fun formatLastMessagePreview(message: String?): String {
    if (message.isNullOrBlank()) return "Sin mensajes"
    return when {
        message.startsWith("[SHARED_POST]") -> {
            try {
                val jsonStr = message.removePrefix("[SHARED_POST]")
                val json = org.json.JSONObject(jsonStr)
                val ownerUsername = json.optString("ownerUsername", "")
                if (ownerUsername.isNotEmpty()) {
                    "EnviÃ³ un post de ${ownerUsername.take(15)}${if (ownerUsername.length > 15) "..." else ""}"
                } else {
                    "EnviÃ³ un post compartido"
                }
            } catch (_: Exception) {
                "EnviÃ³ un post compartido"
            }
        }
        message.startsWith("[CONSULT_POST]") -> {
            try {
                val jsonStr = message.removePrefix("[CONSULT_POST]")
                val json = org.json.JSONObject(jsonStr)
                val type = json.optString("type", "inquiry")
                val productTitle = json.optString("productTitle", "")
                if (type == "offer") {
                    "ðŸ’° Oferta: ${productTitle.take(20)}${if (productTitle.length > 20) "..." else ""}"
                } else {
                    "â“ Consulta: ${productTitle.take(20)}${if (productTitle.length > 20) "..." else ""}"
                }
            } catch (_: Exception) {
                "â“ Consulta de producto"
            }
        }
        message.startsWith("[SHARED_REND]") -> {
            try {
                val jsonStr = message.removePrefix("[SHARED_REND]")
                val json = org.json.JSONObject(jsonStr)
                val owner = json.optString("ownerUsername", "")
                if (owner.isNotEmpty()) "ðŸŽ¬ Video de @$owner" else "ðŸŽ¬ Video compartido"
            } catch (_: Exception) { "ðŸŽ¬ Video compartido" }
        }
        message.startsWith("[SHARED_USER]") -> {
            try {
                val jsonStr = message.removePrefix("[SHARED_USER]")
                val json = org.json.JSONObject(jsonStr)
                val username = json.optString("username", "")
                if (username.isNotEmpty()) "ðŸ‘¤ CompartiÃ³ a @$username" else "ðŸ‘¤ CompartiÃ³ un usuario"
            } catch (_: Exception) { "ðŸ‘¤ CompartiÃ³ un usuario" }
        }
        message.startsWith("[IMG]") -> "ðŸ“· Imagen"
        message.startsWith("[VIDEO]") -> "ðŸŽ¬ Video"
        message.startsWith("[AUDIO]") -> "ðŸŽ¤ Audio"
        message.startsWith("[HANDSHAKE_STATUS]") -> {
            try {
                val jsonStr = message.removePrefix("[HANDSHAKE_STATUS]")
                val json = org.json.JSONObject(jsonStr)
                val type = json.optString("type", "")
                val price = json.optDouble("agreedPrice", 0.0)
                val priceStr = if (price > 0) " Â· \$${String.format("%.0f", price)}" else ""
                when {
                    type.contains("COMPLETED") || type.contains("TRANSACTION_COMPLETED") -> "âœ… TransacciÃ³n finalizada$priceStr"
                    type.contains("CANCELLED") || type.contains("AGREEMENT_CANCELLED") -> "âŒ Acuerdo cancelado"
                    type.contains("REJECTED") -> "âŒ Propuesta rechazada"
                    type.contains("PROPOSED") -> "ðŸ¤ Nueva propuesta$priceStr"
                    type.contains("ACCEPTED") -> "âœ… Acuerdo aceptado$priceStr"
                    type.contains("CONFIRMED") -> "â³ Esperando confirmaciÃ³n$priceStr"
                    else -> "ðŸ¤ ActualizaciÃ³n de acuerdo"
                }
            } catch (_: Exception) { "ðŸ¤ ActualizaciÃ³n de transacciÃ³n" }
        }
        message.startsWith("[HANDSHAKE_INITIATED]") || message.startsWith("[HANDSHAKE]") -> "ðŸ¤ Propuesta de transacciÃ³n"
        message.startsWith("[LOCATION]") -> "ðŸ“ UbicaciÃ³n"
        message.startsWith("[FILE]") -> {
            try {
                val json = org.json.JSONObject(message.removePrefix("[FILE]"))
                "ðŸ“Ž ${json.optString("name", "Archivo")}"
            } catch (_: Exception) { "ðŸ“Ž Archivo" }
        }
        else -> message
    }
}

// Obtener color especial para mensajes de handshake
private fun getHandshakeMessageColor(message: String?): Color? {
    if (message == null || !message.startsWith("[HANDSHAKE_STATUS]")) return null
    return try {
        val jsonStr = message.removePrefix("[HANDSHAKE_STATUS]")
        val json = org.json.JSONObject(jsonStr)
        val type = json.optString("type", "")
        when {
            type.contains("COMPLETED") || type.contains("TRANSACTION_COMPLETED") -> Color(0xFF22C55E) // green
            type.contains("CANCELLED") || type.contains("AGREEMENT_CANCELLED") -> Color(0xFFEF4444) // red
            type.contains("REJECTED") -> Color(0xFFEF4444) // red
            type.contains("PROPOSED") -> Color(0xFFFF6B35) // orange
            type.contains("ACCEPTED") -> Color(0xFF3B82F6) // blue
            type.contains("CONFIRMED") -> Color(0xFFFF6B35) // orange
            else -> null
        }
    } catch (_: Exception) { null }
}

// Formatear timestamp de mensaje como "ahora", "5m", "1h", "2d", etc.
private fun formatMessageTimeAgo(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""
    return try {
        val cleanTimestamp = timestamp
            .replace(" ", "T")
            .replace("+00:00", "Z")
            .replace("+00", "Z")
            .let { if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it }
        
        val instant = try {
            java.time.Instant.parse(cleanTimestamp)
        } catch (_: Exception) {
            java.time.OffsetDateTime.parse(cleanTimestamp).toInstant()
        }
        
        val now = java.time.Instant.now()
        val minutes = java.time.Duration.between(instant, now).toMinutes()
        when {
            minutes < 1 -> "ahora"
            minutes < 60 -> "${minutes}m"
            minutes < 1440 -> "${minutes / 60}h"
            minutes < 10080 -> "${minutes / 1440}d"
            else -> "${minutes / 10080}sem"
        }
    } catch (_: Exception) { "" }
}
