package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.repository.ChatLabel
import com.rendly.app.data.repository.ChatRepository
import com.rendly.app.data.repository.Conversation
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MessagePreview(
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val iconColor: Color = Color(0xFF8B98A5),
    val text: String
)

data class ChatPreview(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessagePreview: MessagePreview? = null,
    val timeAgo: String,
    val isUnread: Boolean = false,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val isMuted: Boolean = false,
    val lastMessageIsFromMe: Boolean = false,
    val isPinned: Boolean = false,
    val labels: List<ChatLabel> = emptyList()
)

data class UserToMessage(
    val id: String,
    val username: String,
    val displayName: String? = null,
    val avatarUrl: String?,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false
)

@Composable
fun OptimizedMessagesDrawer(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpenChat: (Usuario) -> Unit = {}, // Solo el usuario
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(1f) }
    val backdropAlpha = (1f - offsetX.value).coerceIn(0f, 1f)

    LaunchedEffect(isVisible) {
        offsetX.animateTo(
            targetValue = if (isVisible) 0f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    if (isVisible || offsetX.value < 1f) {
        Box(modifier = modifier.fillMaxSize()) {
            // Backdrop
            if (backdropAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = backdropAlpha * 0.5f }
                        .background(Color.Black)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDismiss() }
                )
            }

            // Drawer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX.value * size.width
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                if (dragAmount > 0) {
                                    scope.launch {
                                        offsetX.stop()
                                        val newValue = (offsetX.value + dragAmount / size.width).coerceIn(0f, 1f)
                                        offsetX.snapTo(newValue)
                                    }
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value > 0.25f) {
                                        offsetX.animateTo(1f, tween(120, easing = LinearOutSlowInEasing))
                                        onDismiss()
                                    } else {
                                        offsetX.animateTo(0f)
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    if (offsetX.value > 0.25f) {
                                        offsetX.animateTo(1f, tween(120, easing = LinearOutSlowInEasing))
                                        onDismiss()
                                    } else {
                                        offsetX.animateTo(0f)
                                    }
                                }
                            }
                        )
                    }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HomeBg,
                    shadowElevation = 24.dp
                ) {
                    MessagesContent(
                        onClose = onDismiss,
                        onOpenChat = onOpenChat
                    )
                }
            }
        }
    }
}

@Composable
private fun MessagesContent(
    onClose: () -> Unit,
    onOpenChat: (Usuario) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNewMessageModal by remember { mutableStateOf(false) }
    var showLabelsManager by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Cargar conversaciones reales desde Supabase
    val conversations by ChatRepository.conversations.collectAsState()
    val isLoading by ChatRepository.isLoading.collectAsState()
    val userLabels by ChatRepository.userLabels.collectAsState()
    
    // Filtro de etiqueta activa
    var selectedLabelFilter by remember { mutableStateOf<String?>(null) }
    
    // Cargar al inicio
    LaunchedEffect(Unit) {
        ChatRepository.loadConversations()
        ChatRepository.loadUserLabels()
    }
    
    // Convertir conversaciones a ChatPreview para mantener compatibilidad UI
    val chats = remember(conversations) {
        conversations.map { conv ->
            val preview = getMessagePreview(conv.lastMessage)
            ChatPreview(
                id = conv.id,
                username = conv.otherUser.username,
                displayName = conv.otherUser.nombreTienda ?: conv.otherUser.nombre,
                avatarUrl = conv.otherUser.avatarUrl,
                lastMessage = formatLastMessagePreview(conv.lastMessage),
                lastMessagePreview = preview,
                timeAgo = formatMessageTimeAgo(conv.lastMessageAt),
                isUnread = conv.unreadCount > 0,
                unreadCount = conv.unreadCount,
                isOnline = false,
                isVerified = conv.otherUser.isVerified,
                isMuted = conv.isMuted,
                isPinned = conv.isPinned,
                labels = conv.labels
            )
        }
    }
    
    // Map para obtener Usuario por conversationId
    val conversationUserMap = remember(conversations) {
        conversations.associate { it.id to it.otherUser }
    }
    
    // Estado para modal de opciones al dejar presionado un chat
    var selectedChatForOptions by remember { mutableStateOf<ChatPreview?>(null) }
    
    // Filtrar chats por búsqueda y etiqueta
    val filteredChats = remember(searchQuery, chats, selectedLabelFilter) {
        var result = chats
        if (searchQuery.isNotEmpty()) {
            result = result.filter { 
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.displayName?.contains(searchQuery, ignoreCase = true) == true ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
        if (selectedLabelFilter != null) {
            result = result.filter { chat -> chat.labels.any { it.id == selectedLabelFilter } }
        }
        result
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
                .statusBarsPadding()
        ) {
            // Header estilo Instagram
            MessagesHeader(
                onClose = onClose,
                onNewMessage = { showNewMessageModal = true },
                onManageLabels = { showLabelsManager = true }
            )
            
            // Buscador
            SearchBarMessages(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { 
                    searchQuery = ""
                    focusManager.clearFocus()
                }
            )
            
            // Filtros de etiquetas (chips horizontales)
            if (userLabels.isNotEmpty()) {
                LabelFilterChips(
                    labels = userLabels,
                    selectedLabelId = selectedLabelFilter,
                    onLabelSelected = { labelId ->
                        selectedLabelFilter = if (selectedLabelFilter == labelId) null else labelId
                    }
                )
            }
            
            // Lista de chats
            if (isLoading) {
                // Skeleton loading profesional en lugar de spinner
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(6) {
                        ChatItemSkeleton()
                    }
                }
            } else if (filteredChats.isEmpty()) {
                EmptyChatsState(
                    isSearching = searchQuery.isNotEmpty() || selectedLabelFilter != null,
                    onNewMessage = { showNewMessageModal = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredChats, key = { it.id }) { chat ->
                        ChatItemImproved(
                            chat = chat,
                            onClick = { 
                                conversationUserMap[chat.id]?.let { user ->
                                    onOpenChat(user)
                                }
                            },
                            onLongClick = {
                                selectedChatForOptions = chat
                            }
                        )
                    }
                }
            }
        }
        
        // Modal de opciones al dejar presionado un chat
        ChatOptionsModal(
            chat = selectedChatForOptions,
            onDismiss = { selectedChatForOptions = null }
        )
        
        // Modal de gestión de etiquetas
        LabelsManagerModal(
            isVisible = showLabelsManager,
            onDismiss = { showLabelsManager = false }
        )
        
        // Modal de nuevo mensaje
        NewMessageModal(
            isVisible = showNewMessageModal,
            onDismiss = { showNewMessageModal = false },
            onSelectUser = { user ->
                showNewMessageModal = false
                onOpenChat(user)
            }
        )
    }
}

// Header del componente de mensajes
@Composable
private fun MessagesHeader(
    onClose: () -> Unit,
    onNewMessage: () -> Unit,
    onManageLabels: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg
    ) {
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
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Surface.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Cerrar",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Mensajes",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de etiquetas
                IconButton(
                    onClick = onManageLabels,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Surface.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Label,
                        contentDescription = "Etiquetas",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Botón de nuevo mensaje
                IconButton(
                    onClick = onNewMessage,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Surface)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Nuevo mensaje",
                        tint = TextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

// Barra de búsqueda
@Composable
private fun SearchBarMessages(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(PrimaryPurple),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Buscar conversaciones...",
                                color = TextMuted,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Item de chat mejorado estilo Instagram
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatItemImproved(
    chat: ChatPreview,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con indicador online
            Box {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    AsyncImage(
                        model = chat.avatarUrl 
                            ?: "https://ui-avatars.com/api/?name=${chat.username}&background=A78BFA&color=fff&size=128",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Indicador online
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .border(2.5.dp, HomeBg, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido del mensaje
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Username con verificación y pin
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = chat.username,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (chat.isVerified) {
                            Spacer(modifier = Modifier.width(3.dp))
                            VerifiedBadge(size = 14.dp)
                        }
                    }
                    
                    // Hora + pin icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (chat.timeAgo.isNotEmpty()) {
                            Text(
                                text = chat.timeAgo,
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        
                        if (chat.isPinned) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Fijado",
                                tint = PrimaryPurple.copy(alpha = 0.7f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        
                        if (chat.isMuted) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsOff,
                                contentDescription = "Silenciado",
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Último mensaje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (chat.isUnread && chat.unreadCount > 1) {
                        Text(
                            text = "+${chat.unreadCount} mensajes",
                            fontSize = 14.sp,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    } else {
                        if (chat.lastMessageIsFromMe) {
                            Text(
                                text = "Tú: ",
                                fontSize = 14.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        
                        // Icono profesional si hay preview con icono
                        val preview = chat.lastMessagePreview
                        if (preview?.icon != null) {
                            Icon(
                                imageVector = preview.icon,
                                contentDescription = null,
                                tint = preview.iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        Text(
                            text = chat.lastMessage,
                            fontSize = 14.sp,
                            color = if (chat.isUnread) TextPrimary else TextMuted,
                            fontWeight = if (chat.isUnread) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                
                // Etiquetas del chat (si tiene)
                if (chat.labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        chat.labels.take(3).forEach { label ->
                            val labelColor = try { Color(android.graphics.Color.parseColor(label.color)) } catch (e: Exception) { PrimaryPurple }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = labelColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(labelColor)
                                    )
                                    Text(
                                        text = label.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = labelColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        if (chat.labels.size > 3) {
                            Text(
                                text = "+${chat.labels.size - 3}",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// Estado vacío
@Composable
private fun EmptyChatsState(
    isSearching: Boolean,
    onNewMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Outlined.Search else Icons.Outlined.Email,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isSearching) "Sin resultados" else "Sin mensajes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isSearching) 
                "No encontramos conversaciones con ese término" 
                else "Inicia una conversación con alguien",
            fontSize = 14.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        if (!isSearching) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNewMessage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nuevo mensaje",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Modal de nuevo mensaje (100% ancho)
@Composable
private fun NewMessageModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSelectUser: (Usuario) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    
    // Búsqueda real de usuarios desde Supabase
    var searchResults by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    
    // Buscar usuarios cuando cambia la query
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            delay(300) // Debounce
            searchResults = ChatRepository.searchUsers(searchQuery)
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }
    
    // Solicitar foco cuando se abre
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(100)
            focusRequester.requestFocus()
        } else {
            searchQuery = ""
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header del modal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary
                        )
                    }
                    
                    Text(
                        text = "Nuevo mensaje",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // Placeholder para equilibrar el layout
                    Spacer(modifier = Modifier.size(36.dp))
                }
                
                // Campo de búsqueda "Para:"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Para:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 15.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(PrimaryPurple),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Buscar usuario...",
                                            color = TextMuted,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Título de sección
                Text(
                    text = if (searchQuery.isEmpty()) "Escribe para buscar" else "Resultados",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // Lista de usuarios
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isSearching) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    } else {
                        items(searchResults, key = { it.userId }) { user ->
                            RealUserItem(
                                user = user,
                                onClick = { onSelectUser(user) }
                            )
                        }
                        
                        if (searchResults.isEmpty() && searchQuery.length >= 2) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No se encontraron usuarios",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        
                        if (searchQuery.length < 2 && searchQuery.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Escribe al menos 2 caracteres",
                                        color = TextMuted,
                                        fontSize = 14.sp
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

// Skeleton para item de chat
@Composable
private fun ChatItemSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar skeleton
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Surface.copy(alpha = shimmerAlpha))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // Username skeleton
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Surface.copy(alpha = shimmerAlpha))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Surface.copy(alpha = shimmerAlpha * 0.7f))
            )
        }
    }
}

// Item de usuario real para nuevo mensaje
@Composable
private fun RealUserItem(
    user: Usuario,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape
            ) {
                AsyncImage(
                    model = user.avatarUrl 
                        ?: "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff&size=128",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nombreTienda ?: user.nombre ?: user.username,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = "@${user.username}",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// Modal de opciones al dejar presionado un chat
@Composable
private fun ChatOptionsModal(
    chat: ChatPreview?,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isMuted by remember(chat?.id) { mutableStateOf(chat?.isMuted ?: false) }
    var isPinned by remember(chat?.id) { mutableStateOf(chat?.isPinned ?: false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showLabelPicker by remember { mutableStateOf(false) }
    val userLabels by ChatRepository.userLabels.collectAsState()
    
    // Cargar estados reales al abrir
    LaunchedEffect(chat) {
        if (chat != null) {
            isMuted = ChatRepository.isChatMuted(chat.id)
            isPinned = ChatRepository.isChatPinned(chat.id)
            showDeleteConfirm = false
            showLabelPicker = false
        }
    }
    
    AnimatedVisibility(
        visible = chat != null,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = chat != null,
                enter = slideInVertically(
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetY = { it }
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {},
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = HomeBg,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        // Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(36.dp)
                                .height(4.dp)
                                .background(TextMuted.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Header con avatar y nombre
                        chat?.let { c ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape
                                ) {
                                    AsyncImage(
                                        model = c.avatarUrl 
                                            ?: "https://ui-avatars.com/api/?name=${c.username}&background=A78BFA&color=fff",
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = c.displayName ?: c.username,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        if (c.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            VerifiedBadge(size = 14.dp)
                                        }
                                    }
                                    Text("@${c.username}", fontSize = 13.sp, color = TextMuted)
                                }
                            }
                            
                            // Etiquetas actuales del chat
                            if (c.labels.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    c.labels.forEach { label ->
                                        val lc = try { Color(android.graphics.Color.parseColor(label.color)) } catch (e: Exception) { PrimaryPurple }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = lc.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(lc))
                                                Text(label.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = lc)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Separador sutil
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(TextMuted.copy(alpha = 0.15f))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Opciones
                        // 1. Fijar / Desfijar
                        ChatOptionItem(
                            icon = if (isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                            title = if (isPinned) "Desfijar chat" else "Fijar chat",
                            subtitle = if (isPinned) "Quitar de la parte superior" else "Mantener en la parte superior",
                            iconColor = if (isPinned) TextSecondary else PrimaryPurple
                        ) {
                            chat?.let { c ->
                                scope.launch {
                                    val success = if (isPinned) ChatRepository.unpinChat(c.id) else ChatRepository.pinChat(c.id)
                                    if (success) isPinned = !isPinned
                                    onDismiss()
                                }
                            }
                        }
                        
                        // 2. Etiquetas
                        ChatOptionItem(
                            icon = Icons.Outlined.Label,
                            title = "Etiquetar chat",
                            subtitle = if (chat?.labels.isNullOrEmpty()) "Agregar etiquetas para organizar" else "${chat?.labels?.size} etiqueta(s) asignada(s)",
                            iconColor = Color(0xFF8B5CF6)
                        ) {
                            showLabelPicker = true
                        }
                        
                        // 3. Silenciar / Activar
                        ChatOptionItem(
                            icon = if (isMuted) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                            title = if (isMuted) "Activar notificaciones" else "Silenciar notificaciones",
                            subtitle = if (isMuted) "Volver a recibir notificaciones" else "No recibir notificaciones de este chat",
                            iconColor = if (isMuted) Color(0xFF22C55E) else Color(0xFFFACC15)
                        ) {
                            chat?.let { c ->
                                scope.launch {
                                    val success = if (isMuted) ChatRepository.unmuteChat(c.id) else ChatRepository.muteChat(c.id)
                                    if (success) isMuted = !isMuted
                                    onDismiss()
                                }
                            }
                        }
                        
                        // 4. Marcar como leído
                        if (chat?.isUnread == true) {
                            ChatOptionItem(
                                icon = Icons.Outlined.DoneAll,
                                title = "Marcar como leído",
                                subtitle = "Quitar indicador de no leído",
                                iconColor = Color(0xFF3B82F6)
                            ) {
                                chat?.let { c ->
                                    scope.launch {
                                        ChatRepository.markMessagesAsRead(c.id)
                                        onDismiss()
                                    }
                                }
                            }
                        }
                        
                        // 5. Eliminar chat
                        ChatOptionItem(
                            icon = Icons.Outlined.Delete,
                            title = "Eliminar chat",
                            subtitle = "Quitar del listado de conversaciones",
                            iconColor = Color(0xFFEF4444)
                        ) {
                            showDeleteConfirm = true
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Botón cerrar
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Surface)
                        ) {
                            Text("Cerrar", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
    
    // Picker de etiquetas
    if (showLabelPicker && chat != null) {
        LabelPickerModal(
            conversationId = chat.id,
            currentLabels = chat.labels,
            allLabels = userLabels,
            onDismiss = { 
                showLabelPicker = false
                onDismiss()
            }
        )
    }
    
    // Confirmación de eliminación
    if (showDeleteConfirm && chat != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = HomeBg,
            title = {
                Text("Eliminar chat", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "Se eliminará esta conversación con @${chat.username} de tu listado. Los mensajes no se borrarán del servidor.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            ChatRepository.deleteConversation(chat.id)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun ChatOptionItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// Función auxiliar para obtener preview con icono profesional
private fun getMessagePreview(message: String?): MessagePreview? {
    if (message.isNullOrBlank()) return null
    return when {
        message.startsWith("[AUDIO]") -> MessagePreview(
            icon = Icons.Outlined.Mic,
            iconColor = Color(0xFF8B5CF6),
            text = "Mensaje de voz"
        )
        message.startsWith("[IMG]") -> MessagePreview(
            icon = Icons.Outlined.CameraAlt,
            iconColor = Color(0xFF3B82F6),
            text = "Foto"
        )
        message.startsWith("[VIDEO]") -> MessagePreview(
            icon = Icons.Outlined.Videocam,
            iconColor = Color(0xFFEF4444),
            text = "Video"
        )
        message.startsWith("[LOCATION]") -> MessagePreview(
            icon = Icons.Outlined.LocationOn,
            iconColor = Color(0xFF10B981),
            text = "Ubicación compartida"
        )
        message.startsWith("[SHARED_USER]") -> {
            val username = try {
                val json = org.json.JSONObject(message.removePrefix("[SHARED_USER]"))
                json.optString("username", "")
            } catch (_: Exception) { "" }
            MessagePreview(
                icon = Icons.Outlined.Person,
                iconColor = Color(0xFF6366F1),
                text = if (username.isNotEmpty()) "Contacto: @$username" else "Contacto compartido"
            )
        }
        message.startsWith("[SHARED_POST]") -> {
            val title = try {
                val json = org.json.JSONObject(message.removePrefix("[SHARED_POST]"))
                json.optString("title", "")
            } catch (_: Exception) { "" }
            MessagePreview(
                icon = Icons.Outlined.ShoppingBag,
                iconColor = Color(0xFFFF6B35),
                text = title.ifEmpty { "Artículo compartido" }
            )
        }
        message.startsWith("[ARTICLE_CARD]") -> MessagePreview(
            icon = Icons.Outlined.ShoppingBag,
            iconColor = Color(0xFFFF6B35),
            text = "Artículo compartido"
        )
        message.startsWith("[CONSULT_POST]") -> {
            val isOffer = try {
                val json = org.json.JSONObject(message.removePrefix("[CONSULT_POST]"))
                json.optString("type", "inquiry") == "offer"
            } catch (_: Exception) { false }
            if (isOffer) MessagePreview(
                icon = Icons.Outlined.LocalOffer,
                iconColor = Color(0xFF10B981),
                text = "Oferta enviada"
            ) else MessagePreview(
                icon = Icons.Outlined.HelpOutline,
                iconColor = Color(0xFF3B82F6),
                text = "Consulta sobre producto"
            )
        }
        message.startsWith("[FILE]") -> {
            val name = try {
                val json = org.json.JSONObject(message.removePrefix("[FILE]"))
                json.optString("name", "Archivo")
            } catch (_: Exception) { "Archivo" }
            MessagePreview(
                icon = Icons.Outlined.InsertDriveFile,
                iconColor = Color(0xFF6B7280),
                text = name
            )
        }
        message.startsWith("[HANDSHAKE_STATUS]") -> {
            val type = try {
                val json = org.json.JSONObject(message.removePrefix("[HANDSHAKE_STATUS]"))
                json.optString("type", "")
            } catch (_: Exception) { "" }
            when {
                type.contains("COMPLETED") -> MessagePreview(
                    icon = Icons.Outlined.CheckCircle,
                    iconColor = Color(0xFF10B981),
                    text = "Transacción completada"
                )
                type.contains("CANCELLED") -> MessagePreview(
                    icon = Icons.Outlined.Cancel,
                    iconColor = Color(0xFFEF4444),
                    text = "Transacción cancelada"
                )
                else -> MessagePreview(
                    icon = Icons.Outlined.Handshake,
                    iconColor = Color(0xFFFF6B35),
                    text = "Actualización de transacción"
                )
            }
        }
        message.startsWith("[HANDSHAKE_INITIATED]") || message.startsWith("[HANDSHAKE]") -> MessagePreview(
            icon = Icons.Outlined.Handshake,
            iconColor = Color(0xFFFF6B35),
            text = "Propuesta de transacción"
        )
        message.contains("Respondió a tu historia") -> MessagePreview(
            icon = Icons.Outlined.CameraAlt,
            iconColor = Color(0xFF8B5CF6),
            text = "Respondió a tu historia"
        )
        else -> null
    }
}

// Función auxiliar para formatear preview de último mensaje en chat list
private fun formatLastMessagePreview(message: String?): String {
    if (message.isNullOrBlank()) return "Sin mensajes"
    return when {
        message.startsWith("[AUDIO]") -> "Mensaje de voz"
        message.startsWith("[IMG]") -> "Foto"
        message.startsWith("[VIDEO]") -> "Video"
        message.startsWith("[LOCATION]") -> "Ubicación compartida"
        message.startsWith("[SHARED_USER]") -> {
            try {
                val json = org.json.JSONObject(message.removePrefix("[SHARED_USER]"))
                val username = json.optString("username", "")
                if (username.isNotEmpty()) "Contacto: @$username" else "Contacto compartido"
            } catch (_: Exception) { "Contacto compartido" }
        }
        message.startsWith("[SHARED_POST]") -> {
            try {
                val json = org.json.JSONObject(message.removePrefix("[SHARED_POST]"))
                val title = json.optString("title", "")
                title.ifEmpty { "Artículo compartido" }
            } catch (_: Exception) { "Artículo compartido" }
        }
        message.startsWith("[ARTICLE_CARD]") -> "Artículo compartido"
        message.startsWith("[CONSULT_POST]") -> {
            try {
                val json = org.json.JSONObject(message.removePrefix("[CONSULT_POST]"))
                val type = json.optString("type", "inquiry")
                if (type == "offer") "Oferta enviada" else "Consulta sobre producto"
            } catch (_: Exception) { "Consulta sobre producto" }
        }
        message.startsWith("[FILE]") -> {
            try {
                val json = org.json.JSONObject(message.removePrefix("[FILE]"))
                json.optString("name", "Archivo")
            } catch (_: Exception) { "Archivo" }
        }
        message.startsWith("[HANDSHAKE_STATUS]") -> {
            try {
                val json = org.json.JSONObject(message.removePrefix("[HANDSHAKE_STATUS]"))
                val type = json.optString("type", "")
                when {
                    type.contains("COMPLETED") -> "Transacción completada"
                    type.contains("CANCELLED") -> "Transacción cancelada"
                    else -> "Actualización de transacción"
                }
            } catch (_: Exception) { "Actualización de transacción" }
        }
        message.startsWith("[HANDSHAKE_INITIATED]") || message.startsWith("[HANDSHAKE]") -> "Propuesta de transacción"
        message.contains("Respondió a tu historia") -> "Respondió a tu historia"
        else -> message
    }
}

// Función auxiliar para formatear tiempo de mensajes
private fun formatMessageTimeAgo(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""
    return try {
        // Intentar varios formatos de Supabase
        val cleanTimestamp = timestamp
            .replace(" ", "T")
            .replace("+00:00", "Z")
            .replace("+00", "Z")
            .let { if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it }
        
        val instant = try {
            java.time.Instant.parse(cleanTimestamp)
        } catch (e: Exception) {
            // Intentar con OffsetDateTime si Instant falla
            java.time.OffsetDateTime.parse(cleanTimestamp).toInstant()
        }
        
        val now = java.time.Instant.now()
        val minutes = java.time.Duration.between(instant, now).toMinutes()
        when {
            minutes < 0 -> "ahora"
            minutes < 1 -> "ahora"
            minutes < 60 -> "${minutes}m"
            minutes < 1440 -> "${minutes / 60}h"
            minutes < 10080 -> "${minutes / 1440}d"
            else -> "${minutes / 10080}sem"
        }
    } catch (e: Exception) {
        android.util.Log.e("MessagesDrawer", "Error parsing timestamp: $timestamp - ${e.message}")
        ""
    }
}

// ═══════════════════════════════════════════════════════════════
// ETIQUETAS - Chips de filtro horizontal
// ═══════════════════════════════════════════════════════════════

@Composable
private fun LabelFilterChips(
    labels: List<ChatLabel>,
    selectedLabelId: String?,
    onLabelSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEach { label ->
            val isSelected = label.id == selectedLabelId
            val labelColor = try { Color(android.graphics.Color.parseColor(label.color)) } catch (e: Exception) { PrimaryPurple }
            
            Surface(
                modifier = Modifier.clickable { onLabelSelected(label.id) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) labelColor else labelColor.copy(alpha = 0.1f),
                border = if (!isSelected) BorderStroke(1.dp, labelColor.copy(alpha = 0.3f)) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val resolvedIcon = resolveIconByName(label.icon)
                    if (resolvedIcon != null) {
                        Icon(resolvedIcon, null, tint = if (isSelected) Color.White else labelColor, modifier = Modifier.size(14.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else labelColor)
                        )
                    }
                    Text(
                        text = label.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else labelColor
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ETIQUETAS - Picker para asignar/quitar etiquetas a un chat
// ═══════════════════════════════════════════════════════════════

@Composable
private fun LabelPickerModal(
    conversationId: String,
    currentLabels: List<ChatLabel>,
    allLabels: List<ChatLabel>,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var assignedIds by remember(currentLabels) { mutableStateOf(currentLabels.map { it.id }.toSet()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HomeBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Label, null, tint = PrimaryPurple, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Etiquetar chat", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
            }
        },
        text = {
            Column {
                Text(
                    "Selecciona las etiquetas para este chat",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (allLabels.isEmpty()) {
                    Text("No hay etiquetas creadas", color = TextMuted, fontSize = 14.sp)
                } else {
                    allLabels.forEach { label ->
                        val isAssigned = label.id in assignedIds
                        val labelColor = try { Color(android.graphics.Color.parseColor(label.color)) } catch (e: Exception) { PrimaryPurple }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    scope.launch {
                                        if (isAssigned) {
                                            ChatRepository.removeLabel(conversationId, label.id)
                                            assignedIds = assignedIds - label.id
                                        } else {
                                            ChatRepository.assignLabel(conversationId, label.id)
                                            assignedIds = assignedIds + label.id
                                        }
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color dot
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(labelColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val resolvedIcon = resolveIconByName(label.icon)
                                if (resolvedIcon != null) {
                                    Icon(resolvedIcon, null, tint = labelColor, modifier = Modifier.size(16.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(labelColor)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = label.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Checkbox visual
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isAssigned) labelColor else Color.Transparent
                                    )
                                    .border(
                                        2.dp,
                                        if (isAssigned) labelColor else TextMuted.copy(alpha = 0.3f),
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAssigned) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Listo", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════
// ETIQUETAS - Manager (crear, editar, eliminar etiquetas)
// ═══════════════════════════════════════════════════════════════

private val LABEL_COLORS = listOf(
    "#EF4444", "#F97316", "#F59E0B", "#22C55E",
    "#06B6D4", "#3B82F6", "#8B5CF6", "#EC4899",
    "#14B8A6", "#6366F1", "#A855F7", "#D946EF"
)

private data class LabelIconOption(val icon: ImageVector, val name: String)

@Composable
private fun resolveIconByName(name: String?): ImageVector? = when(name) {
    "person" -> Icons.Outlined.Person
    "cart" -> Icons.Outlined.ShoppingCart
    "package" -> Icons.Outlined.Inventory2
    "money" -> Icons.Outlined.Payments
    "star" -> Icons.Outlined.Star
    "chat" -> Icons.Outlined.ChatBubbleOutline
    "clock" -> Icons.Outlined.Schedule
    "check" -> Icons.Outlined.CheckCircle
    "shipping" -> Icons.Outlined.LocalShipping
    "heart" -> Icons.Outlined.Favorite
    "warning" -> Icons.Outlined.Warning
    "store" -> Icons.Outlined.Storefront
    "handshake" -> Icons.Outlined.Handshake
    "trending" -> Icons.Outlined.TrendingUp
    "bookmark" -> Icons.Outlined.Bookmark
    "verified" -> Icons.Outlined.Verified
    else -> null
}

@Composable
private fun getLabelIconOptions(): List<LabelIconOption> = listOf(
    LabelIconOption(Icons.Outlined.Person, "person"),
    LabelIconOption(Icons.Outlined.ShoppingCart, "cart"),
    LabelIconOption(Icons.Outlined.Inventory2, "package"),
    LabelIconOption(Icons.Outlined.Payments, "money"),
    LabelIconOption(Icons.Outlined.Star, "star"),
    LabelIconOption(Icons.Outlined.ChatBubbleOutline, "chat"),
    LabelIconOption(Icons.Outlined.Schedule, "clock"),
    LabelIconOption(Icons.Outlined.CheckCircle, "check"),
    LabelIconOption(Icons.Outlined.LocalShipping, "shipping"),
    LabelIconOption(Icons.Outlined.Favorite, "heart"),
    LabelIconOption(Icons.Outlined.Warning, "warning"),
    LabelIconOption(Icons.Outlined.Storefront, "store"),
    LabelIconOption(Icons.Outlined.Handshake, "handshake"),
    LabelIconOption(Icons.Outlined.TrendingUp, "trending"),
    LabelIconOption(Icons.Outlined.Bookmark, "bookmark"),
    LabelIconOption(Icons.Outlined.Verified, "verified")
)

@Composable
private fun LabelsManagerModal(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val labels by ChatRepository.userLabels.collectAsState()
    var showCreateForm by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<ChatLabel?>(null) }
    var deleteConfirmLabel by remember { mutableStateOf<ChatLabel?>(null) }
    
    // Form state
    var formName by remember { mutableStateOf("") }
    var formColor by remember { mutableStateOf(LABEL_COLORS[0]) }
    var formIcon by remember { mutableStateOf<String?>(null) }
    
    // Cargar etiquetas al abrir
    LaunchedEffect(isVisible) {
        if (isVisible) {
            ChatRepository.loadUserLabels()
            showCreateForm = false
            editingLabel = null
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HomeBg
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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Etiquetas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            formName = ""
                            formColor = LABEL_COLORS[0]
                            formIcon = null
                            editingLabel = null
                            showCreateForm = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple)
                    ) {
                        Icon(Icons.Default.Add, "Nueva", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                
                // Descripción
                Text(
                    text = "Organiza tus chats con etiquetas personalizadas para gestionar clientes, pedidos y más.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Formulario de crear/editar
                AnimatedVisibility(
                    visible = showCreateForm,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Surface,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (editingLabel != null) "Editar etiqueta" else "Nueva etiqueta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Nombre
                            BasicTextField(
                                value = formName,
                                onValueChange = { formName = it },
                                textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                                singleLine = true,
                                cursorBrush = SolidColor(PrimaryPurple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HomeBg, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (formName.isEmpty()) {
                                            Text("Nombre de la etiqueta", color = TextMuted, fontSize = 15.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Selector de color
                            Text("Color", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LABEL_COLORS.take(6).forEach { hex ->
                                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { PrimaryPurple }
                                    val isSelected = formColor == hex
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .then(
                                                if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                                    .border(4.dp, c, CircleShape)
                                                else Modifier
                                            )
                                            .clickable { formColor = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LABEL_COLORS.drop(6).forEach { hex ->
                                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { PrimaryPurple }
                                    val isSelected = formColor == hex
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .then(
                                                if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                                    .border(4.dp, c, CircleShape)
                                                else Modifier
                                            )
                                            .clickable { formColor = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Selector de icono
                            Text("Icono (opcional)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val iconOptions = getLabelIconOptions()
                            val iconScroll = rememberScrollState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(iconScroll),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Opción "sin icono"
                                Surface(
                                    modifier = Modifier.clickable { formIcon = null },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (formIcon == null) PrimaryPurple.copy(alpha = 0.15f) else HomeBg,
                                    border = if (formIcon == null) BorderStroke(1.5.dp, PrimaryPurple) else null
                                ) {
                                    Icon(
                                        Icons.Outlined.DoNotDisturb,
                                        contentDescription = "Sin icono",
                                        tint = if (formIcon == null) PrimaryPurple else TextMuted,
                                        modifier = Modifier.padding(8.dp).size(20.dp)
                                    )
                                }
                                
                                iconOptions.forEach { option ->
                                    val isSelected = formIcon == option.name
                                    Surface(
                                        modifier = Modifier.clickable { formIcon = option.name },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else HomeBg,
                                        border = if (isSelected) BorderStroke(1.5.dp, PrimaryPurple) else null
                                    ) {
                                        Icon(
                                            option.icon,
                                            contentDescription = option.name,
                                            tint = if (isSelected) PrimaryPurple else TextSecondary,
                                            modifier = Modifier.padding(8.dp).size(20.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Botones
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showCreateForm = false; editingLabel = null },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancelar", color = TextMuted)
                                }
                                
                                Button(
                                    onClick = {
                                        if (formName.isNotBlank()) {
                                            scope.launch {
                                                if (editingLabel != null) {
                                                    ChatRepository.updateLabel(editingLabel!!.id, formName.trim(), formColor, formIcon)
                                                } else {
                                                    ChatRepository.createLabel(formName.trim(), formColor, formIcon)
                                                }
                                                showCreateForm = false
                                                editingLabel = null
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    enabled = formName.isNotBlank()
                                ) {
                                    Text(if (editingLabel != null) "Guardar" else "Crear", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Lista de etiquetas existentes
                if (labels.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Label, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sin etiquetas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Crea tu primera etiqueta para organizar tus chats",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        items(labels, key = { it.id }) { label ->
                            val labelColor = try { Color(android.graphics.Color.parseColor(label.color)) } catch (e: Exception) { PrimaryPurple }
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = Surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Icono/Color
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(labelColor.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val resolvedIcon = resolveIconByName(label.icon)
                                        if (resolvedIcon != null) {
                                            Icon(resolvedIcon, null, tint = labelColor, modifier = Modifier.size(20.dp))
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(labelColor)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = label.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    
                                    // Botón editar
                                    IconButton(
                                        onClick = {
                                            editingLabel = label
                                            formName = label.name
                                            formColor = label.color
                                            formIcon = label.icon
                                            showCreateForm = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Outlined.Edit, "Editar", tint = TextMuted, modifier = Modifier.size(18.dp))
                                    }
                                    
                                    // Botón eliminar
                                    IconButton(
                                        onClick = { deleteConfirmLabel = label },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Outlined.Delete, "Eliminar", tint = Color(0xFFEF4444).copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Confirmación de eliminación de etiqueta
    deleteConfirmLabel?.let { label ->
        AlertDialog(
            onDismissRequest = { deleteConfirmLabel = null },
            containerColor = HomeBg,
            title = { Text("Eliminar etiqueta", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text(
                    "¿Eliminar \"${label.name}\"? Se quitará de todos los chats que la tengan asignada.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            ChatRepository.deleteLabel(label.id)
                            deleteConfirmLabel = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmLabel = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}

