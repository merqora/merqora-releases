package com.rendly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.data.repository.StoryRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class Story(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatar: String?,
    val imageUrl: String,
    val timestamp: Long,
    val duration: Long = 5000L,
    val isViewed: Boolean = false
)

data class UserStories(
    val userId: String,
    val username: String,
    val userAvatar: String?,
    val stories: List<Story>,
    val hasUnviewed: Boolean = true
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoriesViewer(
    userStories: List<UserStories>,
    initialUserIndex: Int = 0,
    currentUserId: String = "", // ID del usuario actual para saber si es nuestra story
    onClose: () -> Unit,
    onStoryViewed: (String) -> Unit = {},
    onReply: (String, String) -> Unit = { _, _ -> }, // storyId, message
    onLike: (String) -> Unit = {},
    onShare: (String) -> Unit = {},
    onDeleteStory: (String) -> Unit = {}, // Callback para eliminar story
    modifier: Modifier = Modifier
) {
    var currentUserIndex by remember { mutableIntStateOf(initialUserIndex) }
    var currentStoryIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var showControls by remember { mutableStateOf(true) }
    
    // Estado para ForwardModal
    var showForwardModal by remember { mutableStateOf(false) }
    
    // Estado para StoryOptionsModal
    var showStoryOptionsModal by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    // Detectar si el teclado está visible
    val isImeVisible = WindowInsets.isImeVisible
    
    val currentUser = userStories.getOrNull(currentUserIndex)
    val currentStory = currentUser?.stories?.getOrNull(currentStoryIndex)
    val isOwnStory = currentUser?.userId == currentUserId
    
    // Likes persistentes desde StoryRepository
    val likedStoryIds by StoryRepository.likedStoryIds.collectAsState()
    val isLiked = currentStory?.id?.let { it in likedStoryIds } ?: false
    val scope = rememberCoroutineScope()
    
    // Progress animation
    val progress = remember { Animatable(0f) }
    
    // Animación de like
    var showLikeAnimation by remember { mutableStateOf(false) }
    
    // Progress animation - handles story progression
    LaunchedEffect(currentUserIndex, currentStoryIndex) {
        // Reset progress immediately when story changes (sin animación)
        progress.snapTo(0f)
        
        // Small delay to ensure UI is ready
        delay(50)
        
        // Start animation from 0
        if (currentStory != null) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = currentStory.duration.toInt(),
                    easing = LinearEasing
                )
            )
            
            // Story finished - go to next
            if (progress.value >= 0.99f) {
                goToNextStory(
                    currentUserIndex = currentUserIndex,
                    currentStoryIndex = currentStoryIndex,
                    userStories = userStories,
                    onUserIndexChange = { currentUserIndex = it },
                    onStoryIndexChange = { currentStoryIndex = it },
                    onClose = onClose
                )
            }
        }
    }
    
    // Handle pause/resume
    LaunchedEffect(isPaused) {
        if (isPaused) {
            progress.stop()
        } else if (currentStory != null && progress.value < 1f) {
            val remainingProgress = 1f - progress.value
            val remainingDuration = (currentStory.duration * remainingProgress).toInt()
            
            if (remainingDuration > 0) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = remainingDuration,
                        easing = LinearEasing
                    )
                )
                
                if (progress.value >= 0.99f) {
                    goToNextStory(
                        currentUserIndex = currentUserIndex,
                        currentStoryIndex = currentStoryIndex,
                        userStories = userStories,
                        onUserIndexChange = { currentUserIndex = it },
                        onStoryIndexChange = { currentStoryIndex = it },
                        onClose = onClose
                    )
                }
            }
        }
    }
    
    // Marcar story como vista cuando cambia
    LaunchedEffect(currentStory?.id) {
        currentStory?.id?.let { onStoryViewed(it) }
    }
    
    // Like animation
    LaunchedEffect(showLikeAnimation) {
        if (showLikeAnimation) {
            delay(800)
            showLikeAnimation = false
        }
    }
    
    // Pausar/reanudar automáticamente según estado del teclado
    LaunchedEffect(isImeVisible) {
        isPaused = isImeVisible
    }
    
    // Pausar cuando se abre el ForwardModal
    LaunchedEffect(showForwardModal) {
        if (showForwardModal) {
            isPaused = true
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets(0)) // Ignorar safe areas - overlay completo
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // Long press para pausar
                        isPaused = true
                        showControls = false
                    },
                    onPress = {
                        // Al presionar, esperar release
                        val released = tryAwaitRelease()
                        if (released) {
                            // Al soltar, despausar
                            isPaused = false
                            showControls = true
                        }
                    },
                    onTap = { offset ->
                        // Navegación por tap en los lados
                        val screenWidth = size.width
                        when {
                            offset.x < screenWidth / 3 -> {
                                // Tap izquierda - story anterior
                                goToPreviousStory(
                                    currentUserIndex = currentUserIndex,
                                    currentStoryIndex = currentStoryIndex,
                                    userStories = userStories,
                                    onUserIndexChange = { currentUserIndex = it },
                                    onStoryIndexChange = { currentStoryIndex = it }
                                )
                            }
                            offset.x > screenWidth * 2 / 3 -> {
                                // Tap derecha - siguiente story
                                goToNextStory(
                                    currentUserIndex = currentUserIndex,
                                    currentStoryIndex = currentStoryIndex,
                                    userStories = userStories,
                                    onUserIndexChange = { currentUserIndex = it },
                                    onStoryIndexChange = { currentStoryIndex = it },
                                    onClose = onClose
                                )
                            }
                        }
                    }
                    // Double tap DESHABILITADO - reservado para futuros gestos de navegación entre usuarios
                )
            }
    ) {
        // Story image
        if (currentStory != null) {
            AsyncImage(
                model = currentStory.imageUrl,
                contentDescription = "Story",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Like animation overlay
        AnimatedVisibility(
            visible = showLikeAnimation,
            enter = fadeIn() + scaleIn(initialScale = 0.5f),
            exit = fadeOut() + scaleOut(targetScale = 1.5f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(120.dp)
            )
        }
        
        // Paused indicator - REMOVIDO, solo pausamos sin icono
        
        // Gradient overlay top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Gradient overlay bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        
        // Top bar with progress and user info
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Progress bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentUser?.stories?.forEachIndexed { index, _ ->
                        val storyProgress = when {
                            index < currentStoryIndex -> 1f
                            index == currentStoryIndex -> progress.value
                            else -> 0f
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(storyProgress)
                                    .background(Color.White)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // User info and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User avatar with gradient border
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryPurple, AccentPink)
                                    ),
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                        ) {
                            val avatarUrl = remember(currentUser?.userAvatar, currentUser?.username) {
                                currentUser?.userAvatar?.takeIf { it.isNotBlank() }
                                    ?: "https://ui-avatars.com/api/?name=${currentUser?.username ?: "U"}&background=A78BFA&color=fff&size=200"
                            }
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column {
                            Text(
                                text = currentUser?.username ?: "",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = getTimeAgo(currentStory?.timestamp ?: 0),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Row {
                        IconButton(onClick = {
                            showStoryOptionsModal = true
                            isPaused = true
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Más",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom bar - Input o Stats según sea nuestra story o no
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (isOwnStory) {
                // Vista para nuestra propia story - mostrar stats
                OwnStoryBottomBar(
                    viewsCount = 0, // TODO: Implementar conteo de vistas
                    onViewsClick = { /* TODO: Mostrar lista de viewers */ },
                    onDeleteClick = { showDeleteConfirm = true; isPaused = true },
                    onShareClick = { currentStory?.id?.let { onShare(it) } }
                )
            } else {
                // Vista para story de otros - mostrar input y acciones
                OtherStoryBottomBar(
                    replyText = replyText,
                    onReplyChange = { replyText = it },
                    onSendReply = {
                        if (replyText.isNotBlank()) {
                            currentStory?.id?.let { onReply(it, replyText) }
                            replyText = ""
                        }
                    },
                    isLiked = isLiked,
                    onLikeClick = {
                        // Toggle like - persistir en Supabase y actualizar UI inmediatamente
                        currentStory?.id?.let { storyId ->
                            scope.launch {
                                val wasLiked = storyId in likedStoryIds
                                val newLikeState = StoryRepository.toggleStoryLike(storyId)
                                // Mostrar animación solo si se está dando like (no unlike)
                                if (newLikeState && !wasLiked) {
                                    showLikeAnimation = true
                                }
                            }
                            onLike(storyId)
                        }
                    },
                    onForwardClick = { showForwardModal = true }
                )
            }
        }
        
        // Story Options Modal
        if (showStoryOptionsModal) {
            StoryOptionsModal(
                isOwnStory = isOwnStory,
                onDismiss = { showStoryOptionsModal = false; isPaused = false },
                onDelete = {
                    showStoryOptionsModal = false
                    showDeleteConfirm = true
                },
                onShare = {
                    showStoryOptionsModal = false
                    isPaused = false
                    currentStory?.id?.let { onShare(it) }
                },
                onReport = {
                    showStoryOptionsModal = false
                    isPaused = false
                    // TODO: Report story
                }
            )
        }
        
        // Delete confirmation dialog
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false; isPaused = false },
                title = { Text("Eliminar historia", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Estás seguro de que quieres eliminar esta historia?", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = {
                        currentStory?.id?.let { onDeleteStory(it) }
                        showDeleteConfirm = false
                        isPaused = false
                        // Go to next story or close
                        goToNextStory(
                            currentUserIndex = currentUserIndex,
                            currentStoryIndex = currentStoryIndex,
                            userStories = userStories,
                            onUserIndexChange = { currentUserIndex = it },
                            onStoryIndexChange = { currentStoryIndex = it },
                            onClose = onClose
                        )
                    }) {
                        Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false; isPaused = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = HomeBg
            )
        }
        
        // Forward Modal para reenviar story
        ForwardModal(
            isVisible = showForwardModal,
            post = currentStory?.let { story ->
                // Crear un Post temporal para el ForwardModal con la imagen de la story
                Post(
                    id = story.id,
                    userId = currentUser?.userId ?: "",
                    title = "Story de ${currentUser?.username ?: ""}",
                    images = listOf(story.imageUrl),
                    username = currentUser?.username ?: "",
                    userAvatar = currentUser?.userAvatar ?: ""
                )
            },
            onDismiss = { 
                showForwardModal = false
                // NO reanudar automáticamente, el teclado lo controla
            },
            onForwardToUser = { _, _, _ -> 
                showForwardModal = false 
            }
        )
    }
}

@Composable
private fun OwnStoryBottomBar(
    viewsCount: Int,
    onViewsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Viewers count
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clickable { onViewsClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$viewsCount vistas",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Compartir",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun OtherStoryBottomBar(
    replyText: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onForwardClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reply input
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (replyText.isEmpty()) {
                Text(
                    text = "Enviar mensaje...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = replyText,
                onValueChange = onReplyChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Send button (only show if there's text)
        if (replyText.isNotBlank()) {
            IconButton(
                onClick = onSendReply,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            // Like button
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Me gusta",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Forward button (Reenviar)
            IconButton(
                onClick = onForwardClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = "Reenviar",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun goToNextStory(
    currentUserIndex: Int,
    currentStoryIndex: Int,
    userStories: List<UserStories>,
    onUserIndexChange: (Int) -> Unit,
    onStoryIndexChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    val currentUser = userStories.getOrNull(currentUserIndex)
    if (currentUser != null && currentStoryIndex < currentUser.stories.size - 1) {
        // Next story of same user
        onStoryIndexChange(currentStoryIndex + 1)
    } else if (currentUserIndex < userStories.size - 1) {
        // Next user
        onUserIndexChange(currentUserIndex + 1)
        onStoryIndexChange(0)
    } else {
        // End of all stories
        onClose()
    }
}

private fun goToPreviousStory(
    currentUserIndex: Int,
    currentStoryIndex: Int,
    userStories: List<UserStories>,
    onUserIndexChange: (Int) -> Unit,
    onStoryIndexChange: (Int) -> Unit
) {
    if (currentStoryIndex > 0) {
        // Previous story of same user
        onStoryIndexChange(currentStoryIndex - 1)
    } else if (currentUserIndex > 0) {
        // Previous user, last story
        val prevUser = userStories[currentUserIndex - 1]
        onUserIndexChange(currentUserIndex - 1)
        onStoryIndexChange(prevUser.stories.size - 1)
    }
}

@Composable
private fun StoryOptionsModal(
    isOwnStory: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Share option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onShare() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = Color(0xFF3A8FD4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Compartir", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                
                if (isOwnStory) {
                    // Delete option (own story)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDelete() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Eliminar historia", color = Color(0xFFEF4444), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    // Report option (other's story)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onReport() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Reportar", color = Color(0xFFEF4444), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Ahora"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> "${diff / 86_400_000}d"
    }
}
