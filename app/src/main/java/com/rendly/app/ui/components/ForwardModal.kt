package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.repository.ChatRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForwardModal(
    isVisible: Boolean,
    post: Post?,
    onDismiss: () -> Unit,
    onForwardToUser: (Usuario, Post, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    val messageFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    
    // Estados
    var searchResults by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<Usuario?>(null) }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Buscar usuarios cuando cambia la query
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2 && selectedUser == null) {
            isSearching = true
            delay(300)
            searchResults = ChatRepository.searchUsers(searchQuery)
            isSearching = false
        } else if (selectedUser == null) {
            searchResults = emptyList()
        }
    }
    
    // Reset al abrir/cerrar
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(100)
            searchFocusRequester.requestFocus()
        } else {
            searchQuery = ""
            searchResults = emptyList()
            selectedUser = null
            message = ""
            showSuccess = false
            isSending = false
        }
    }
    
    // Focus en input de mensaje cuando se selecciona usuario
    LaunchedEffect(selectedUser) {
        if (selectedUser != null) {
            delay(100)
            messageFocusRequester.requestFocus()
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
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .imePadding()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedUser != null) {
                                    selectedUser = null
                                    message = ""
                                } else {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedUser != null) Icons.Default.ArrowBack else Icons.Default.Close,
                                contentDescription = if (selectedUser != null) "Volver" else "Cerrar",
                                tint = TextPrimary
                            )
                        }
                        
                        Text(
                            text = if (selectedUser != null) "Enviar a @${selectedUser?.username}" else "Reenviar a",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                    
                    // Preview del post
                    if (post != null) {
                        PostPreviewCard(post = post)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Contenido principal
                    AnimatedContent(
                        targetState = selectedUser,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                        },
                        label = "forward_content",
                        modifier = Modifier.weight(1f)
                    ) { user ->
                        if (user == null) {
                            // Búsqueda de usuarios
                            UserSearchContent(
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                searchResults = searchResults,
                                isSearching = isSearching,
                                focusRequester = searchFocusRequester,
                                onUserSelect = { selectedUser = it }
                            )
                        } else {
                            // Usuario seleccionado - mostrar área de mensaje
                            SelectedUserContent(
                                user = user,
                                showSuccess = showSuccess
                            )
                        }
                    }
                    
                    // Input de mensaje cuando hay usuario seleccionado
                    AnimatedVisibility(
                        visible = selectedUser != null && !showSuccess,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        MessageInputBar(
                            message = message,
                            onMessageChange = { message = it },
                            isSending = isSending,
                            focusRequester = messageFocusRequester,
                            onSend = {
                                if (selectedUser != null && post != null && !isSending) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSending = true
                                    scope.launch {
                                        onForwardToUser(selectedUser!!, post, message.trim())
                                        delay(500)
                                        showSuccess = true
                                        delay(1500)
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostPreviewCard(post: Post) {
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.producto.imagenUrl.firstOrNull() ?: "",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.producto.titulo.ifEmpty { "Sin título" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${String.format("%.2f", post.producto.precio)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D26A)
                )
            }
            
            Icon(
                imageVector = Icons.Outlined.Send,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UserSearchContent(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    searchResults: List<Usuario>,
    isSearching: Boolean,
    focusRequester: FocusRequester,
    onUserSelect: (Usuario) -> Unit
) {
    Column {
        // Campo de búsqueda
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
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
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
        
        Text(
            text = if (searchQuery.isEmpty()) "Escribe para buscar" else "Resultados",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Lista de usuarios
        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                    UserSelectItem(
                        user = user,
                        onClick = { onUserSelect(user) }
                    )
                }
                
                if (searchResults.isEmpty() && searchQuery.length >= 2) {
                    item {
                        EmptySearchState()
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

@Composable
private fun UserSelectItem(
    user: Usuario,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            
            // Indicador de selección
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SelectedUserContent(
    user: Usuario,
    showSuccess: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Avatar grande del usuario seleccionado
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            shadowElevation = 8.dp
        ) {
            AsyncImage(
                model = user.avatarUrl 
                    ?: "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff&size=128",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = user.nombreTienda ?: user.nombre ?: user.username,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Text(
            text = "@${user.username}",
            fontSize = 14.sp,
            color = TextMuted
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Success message
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AccentGreen.copy(alpha = 0.15f),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "¡Enviado con éxito!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                }
            }
        }
        
        if (!showSuccess) {
            Text(
                text = "Escribe un mensaje personalizado (opcional)",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    isSending: Boolean,
    focusRequester: FocusRequester,
    onSend: () -> Unit
) {
    // Input sin fondo, solo el campo con botón
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Input field
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = Surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (message.isNotEmpty()) PrimaryPurple.copy(alpha = 0.5f) else BorderSubtle
            )
        ) {
            BasicTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp
                ),
                singleLine = false,
                maxLines = 3,
                cursorBrush = SolidColor(PrimaryPurple),
                decorationBox = { innerTextField ->
                    Box {
                        if (message.isEmpty()) {
                            Text(
                                text = "Escribe un mensaje...",
                                color = TextMuted,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        
        // Botón de enviar centrado
        IconButton(
            onClick = onSend,
            enabled = !isSending,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PrimaryPurple)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.PersonSearch,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No se encontraron usuarios",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}
