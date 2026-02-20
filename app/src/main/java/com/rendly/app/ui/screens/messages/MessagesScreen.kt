package com.rendly.app.ui.screens.messages

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*

data class Conversation(
    val id: String,
    val name: String,
    val avatar: String?,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val isVerified: Boolean = false
)

// Datos de ejemplo
private val DEMO_CONVERSATIONS = listOf(
    Conversation(
        id = "1",
        name = "María González",
        avatar = "https://i.pravatar.cc/150?img=1",
        lastMessage = "¡Me encantó el producto! 😍",
        timestamp = "Ahora",
        unreadCount = 3,
        isOnline = true,
        isVerified = true
    ),
    Conversation(
        id = "2",
        name = "Carlos Rodríguez",
        avatar = "https://i.pravatar.cc/150?img=12",
        lastMessage = "Nos vemos mañana entonces 👍",
        timestamp = "5m",
        unreadCount = 0,
        isOnline = true
    ),
    Conversation(
        id = "3",
        name = "Ana Martínez",
        avatar = "https://i.pravatar.cc/150?img=5",
        lastMessage = "",
        timestamp = "10m",
        unreadCount = 0,
        isOnline = true,
        isTyping = true
    ),
    Conversation(
        id = "4",
        name = "Luis Fernández",
        avatar = "https://i.pravatar.cc/150?img=33",
        lastMessage = "Perfecto, gracias por la info",
        timestamp = "1h",
        unreadCount = 1,
        isOnline = false,
        isVerified = true
    ),
    Conversation(
        id = "5",
        name = "Isabel Torres",
        avatar = "https://i.pravatar.cc/150?img=9",
        lastMessage = "¿Está disponible en talle M?",
        timestamp = "2h",
        unreadCount = 0,
        isOnline = false
    ),
    Conversation(
        id = "6",
        name = "Pedro Sánchez",
        avatar = "https://i.pravatar.cc/150?img=15",
        lastMessage = "¿Vienes a la reunión de hoy?",
        timestamp = "3h",
        unreadCount = 2,
        isOnline = true
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    onConversationClick: (Conversation) -> Unit = {},
    onNewMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val conversations = remember { DEMO_CONVERSATIONS }
    
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
            modifier = modifier
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
            
            // Online users row
            if (conversations.any { it.isOnline }) {
                Text(
                    text = "ACTIVOS AHORA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    conversations.filter { it.isOnline }.take(5).forEach { conversation ->
                        OnlineUserAvatar(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Messages section header
            Text(
                text = "MENSAJES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Conversations list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlineUserAvatar(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box {
            AsyncImage(
                model = conversation.avatar,
                contentDescription = conversation.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            // Online indicator
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(HomeBg)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(AccentGreen)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = conversation.name.split(" ").first(),
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val hasUnread = conversation.unreadCount > 0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with online indicator
        Box {
            AsyncImage(
                model = conversation.avatar,
                contentDescription = conversation.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            if (conversation.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(HomeBg)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.name,
                    fontSize = 15.sp,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                if (conversation.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificado",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Formatear mensaje de preview - detectar posts compartidos
            val displayMessage = when {
                conversation.isTyping -> "Escribiendo..."
                conversation.lastMessage.startsWith("[SHARED_POST]") -> {
                    // Parsear para obtener el username del dueño del post
                    try {
                        val jsonStr = conversation.lastMessage.removePrefix("[SHARED_POST]")
                        val json = org.json.JSONObject(jsonStr)
                        val ownerUsername = json.optString("ownerUsername", "")
                        if (ownerUsername.isNotEmpty()) {
                            "Envió un post de ${ownerUsername.take(15)}${if (ownerUsername.length > 15) "..." else ""}"
                        } else {
                            "Envió un post compartido"
                        }
                    } catch (e: Exception) {
                        "Envió un post compartido"
                    }
                }
                conversation.lastMessage.startsWith("[IMG]") -> "📷 Imagen"
                conversation.lastMessage.startsWith("[VIDEO]") -> "🎬 Video"
                conversation.lastMessage.startsWith("[AUDIO]") -> "🎤 Audio"
                else -> conversation.lastMessage
            }
            
            Text(
                text = displayMessage,
                fontSize = 13.sp,
                color = if (conversation.isTyping) AccentGreen else if (hasUnread) TextPrimary else TextMuted,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Right side
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = conversation.timestamp,
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
}
