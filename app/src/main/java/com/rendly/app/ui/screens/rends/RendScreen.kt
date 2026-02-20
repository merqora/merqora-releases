package com.rendly.app.ui.screens.rends

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.data.model.Rend
import com.rendly.app.ui.components.CommentsSheet
import com.rendly.app.ui.components.Comment
import com.rendly.app.data.repository.RendRepository
import com.rendly.app.data.repository.FollowersRepository
import com.rendly.app.data.repository.FollowType
import com.rendly.app.data.repository.CommentRepository
import com.rendly.app.data.repository.NotificationRepository
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.rendly.app.ui.components.ConsultModal
import com.rendly.app.ui.components.ForwardModal
import com.rendly.app.ui.components.ProductPage
import com.rendly.app.ui.components.BottomNavBar
import com.rendly.app.data.repository.ViewTracker
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * -------------------------------------------------------------------------------
 * REND SCREEN - Pantalla de videos cortos con CLIPPING PERFECTO
 * -------------------------------------------------------------------------------
 * 
 * IMPORTANTE: Esta pantalla usa Múltiples capas de clipping para evitar que
 * el contenido de video se vea desde otras secciones del pager horizontal.
 * 
 * Capas de clipping:
 * 1. Box raíz con clipToBounds + graphicsLayer clip
 * 2. Contenedor interno con clip estricto
 * 3. AndroidView del video con clip en LayoutParams
 * 
 * -------------------------------------------------------------------------------
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RendScreen(
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToTendencias: () -> Unit = {},
    isScreenVisible: Boolean = true,
    showNavBar: Boolean = true,
    currentNavRoute: String = "videos",
    onNavNavigate: (String) -> Unit = {},
    onNavHomeReclick: () -> Unit = {},
    initialRendId: String? = null // ID del rend para abrir directamente
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val rends by RendRepository.rends.collectAsState()
    val isLoading by RendRepository.isLoading.collectAsState()
    val errorMessage by RendRepository.errorMessage.collectAsState()
    
    // -------------------------------------------------------------------
    // ESTADOS LEVANTADOS - para que modales y ProductPage están FUERA del pager
    // -------------------------------------------------------------------
    var isProductPageOpen by remember { mutableStateOf(false) }
    var showForwardModal by remember { mutableStateOf(false) }
    var showConsultModal by remember { mutableStateOf(false) }
    var showCommentChoiceModal by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    
    // Estado de error para mostrar en el modal de opiniones
    var commentError by remember { mutableStateOf<String?>(null) }
    
    // Rend actual para modales y ProductPage
    var currentRendForModals by remember { mutableStateOf<Rend?>(null) }
    
    // Datos del usuario actual para comentarios
    val currentAuthUser = SupabaseClient.auth.currentUserOrNull()
    var currentUserAvatar by remember { mutableStateOf<String?>(null) }
    var currentUsername by remember { mutableStateOf("Usuario") }
    var currentUserIsVerified by remember { mutableStateOf(false) }
    
    // Cargar datos del usuario actual
    LaunchedEffect(currentAuthUser?.id) {
        currentAuthUser?.id?.let { userId ->
            try {
                val userResult = SupabaseClient.database
                    .from("usuarios")
                    .select(columns = Columns.list("username", "avatar_url", "is_verified")) {
                        filter { eq("user_id", userId) }
                    }
                    .decodeSingleOrNull<UserDataForComments>()
                
                userResult?.let {
                    currentUsername = it.username.takeIf { u -> u.isNotBlank() } ?: "Usuario"
                    currentUserAvatar = it.avatarUrl
                    currentUserIsVerified = it.isVerified
                }
            } catch (e: Exception) {
                android.util.Log.e("RendScreen", "Error cargando usuario: ${e.message}")
            }
        }
    }
    
    // Cargar comentarios usando CommentRepository
    val commentsFromRepo by CommentRepository.comments.collectAsState()
    val isCommentsLoading by CommentRepository.isLoading.collectAsState()
    val repoError by CommentRepository.lastError.collectAsState()
    
    // Sincronizar error del repositorio con el estado local
    LaunchedEffect(repoError) {
        repoError?.let { error ->
            commentError = error
            CommentRepository.clearError()
        }
    }
    
    // Estado para almacenar el product_id a usar (del Post enlazado o del propio Rend)
    var activeProductId by remember { mutableStateOf<String?>(null) }
    // Indica si el productId viene del Post enlazado (true) o del Rend (false)
    var isLinkedToPost by remember { mutableStateOf(false) }
    
    // Cargar comentarios cuando se abre el sheet
    // SIEMPRE usar product_reviews - con product_id del Post enlazado o del Rend
    LaunchedEffect(showCommentsSheet, currentRendForModals?.id, currentRendForModals?.productLink) {
        if (showCommentsSheet && currentRendForModals != null) {
            val rend = currentRendForModals!!
            val postId = rend.productLink // El productLink es el ID del Post enlazado
            
            if (!postId.isNullOrBlank()) {
                // Rend tiene Post enlazado - usar product_id del Post
                val productId = CommentRepository.getProductIdFromPostId(postId)
                if (productId != null) {
                    activeProductId = productId
                    isLinkedToPost = true
                    CommentRepository.loadProductReviews(productId)
                } else {
                    // El Post no tiene product_id, usar el del Rend
                    val rendProductId = rend.productId ?: CommentRepository.getProductIdFromRendId(rend.id)
                    activeProductId = rendProductId
                    isLinkedToPost = false
                    if (rendProductId != null) {
                        CommentRepository.loadProductReviews(rendProductId)
                    }
                }
            } else {
                // El Rend no tiene Post enlazado - usar product_id del propio Rend
                val rendProductId = rend.productId ?: CommentRepository.getProductIdFromRendId(rend.id)
                activeProductId = rendProductId
                isLinkedToPost = false
                if (rendProductId != null) {
                    CommentRepository.loadProductReviews(rendProductId)
                }
            }
        }
    }
    
    // Detectar si cualquier modal está abierto
    val isAnyModalOpen = showForwardModal || showConsultModal || showCommentsSheet || showCommentChoiceModal
    val isAnyOverlayOpen = isProductPageOpen || isAnyModalOpen
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            RendRepository.clearError()
        }
    }
    
    LaunchedEffect(Unit) {
        RendRepository.loadRends()
    }
    
    // Calcular initialPage basado en initialRendId
    val initialPage = remember(initialRendId, rends) {
        if (initialRendId != null && rends.isNotEmpty()) {
            rends.indexOfFirst { it.id == initialRendId }.takeIf { it >= 0 } ?: 0
        } else 0
    }
    
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { rends.size.coerceAtLeast(1) }
    )
    
    // Scroll to initialPage when rends load and initialRendId is set
    LaunchedEffect(initialRendId, rends) {
        if (initialRendId != null && rends.isNotEmpty()) {
            val targetIndex = rends.indexOfFirst { it.id == initialRendId }
            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                pagerState.scrollToPage(targetIndex)
            }
        }
    }
    
    // Actualizar currentRendForModals cuando cambia la Página
    LaunchedEffect(pagerState.currentPage, rends) {
        if (rends.isNotEmpty() && pagerState.currentPage < rends.size) {
            currentRendForModals = rends[pagerState.currentPage]
            // VIEW TRACKING: Registrar vista del rend actual
            ViewTracker.trackRendView(rends[pagerState.currentPage].id)
        }
    }
    
    // Crear dummyPost para ProductPage y modales (fuera del pager)
    val dummyPostForModals = remember(currentRendForModals) {
        currentRendForModals?.let { rend ->
            val imagesList = mutableListOf<String>()
            rend.productImage?.takeIf { it.isNotBlank() }?.let { imagesList.add(it) }
            if (imagesList.isEmpty()) {
                rend.thumbnailUrl?.takeIf { it.isNotBlank() }?.let { imagesList.add(it) }
            }
            val finalImages = imagesList.ifEmpty { listOf("https://via.placeholder.com/400x400?text=Sin+imagen") }
            
            Post(
                id = rend.id,
                userId = rend.userId,
                username = rend.username,
                userAvatar = rend.userAvatar,
                userStoreName = rend.userStoreName,
                title = rend.productTitle ?: rend.title,
                description = rend.description,
                images = finalImages,
                price = rend.productPrice ?: 0.0,
                condition = "Nuevo",
                category = "",
                likesCount = rend.likesCount,
                reviewsCount = rend.reviewsCount,
                createdAt = System.currentTimeMillis().toString(),
                isLiked = false,
                isSaved = false,
                productId = rend.productId
            )
        }
    }
    
    // -------------------------------------------------------------------
    // CONTENEDOR PRINCIPAL CON CLIPPING máximO
    // -------------------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .graphicsLayer {
                clip = true
                shape = RectangleShape
            }
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        // Contenedor interno con clip adicional
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            // Mostrar skeleton solo si está cargando Y no hay rends Aún
            if (isLoading && rends.isEmpty()) {
                RendSkeletonLoader()
            } else if (!isLoading && rends.isEmpty()) {
                EmptyRendsState(onReload = { scope.launch { RendRepository.loadRends() } })
            } else {
                // Altura del NavBar para calcular el espacio disponible
                val navBarHeight = 64.dp
                
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(bottom = navBarHeight) // Solo el video respeta el NavBar
                        .clipToBounds(),
                    userScrollEnabled = !isAnyOverlayOpen
                ) { page ->
                    val isCurrentPage = pagerState.currentPage == page
                    RendPageContent(
                        rend = rends[page],
                        isPlaying = isCurrentPage && !isProductPageOpen && isScreenVisible && !isAnyModalOpen,
                        onUserClick = { userId -> onNavigateToProfile(userId) },
                        onOpenProductPage = { 
                            currentRendForModals = rends[page]
                            isProductPageOpen = true 
                        },
                        onOpenCommentChoice = { 
                            currentRendForModals = rends[page]
                            showCommentChoiceModal = true 
                        },
                        onOpenForward = { 
                            currentRendForModals = rends[page]
                            showForwardModal = true 
                        }
                    )
                }
            }
            
            // Header tabs
            if (!isProductPageOpen && !isAnyModalOpen) {
                RendHeader(onNavigateToTendencias = onNavigateToTendencias)
            }
            
            // NavBar embebido
            if (showNavBar) {
                BottomNavBar(
                    currentRoute = currentNavRoute,
                    onNavigate = onNavNavigate,
                    onHomeReclick = onNavHomeReclick,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        
        // -------------------------------------------------------------------
        // MODALES Y PRODUCTPAGE - FUERA del pager, usan toda la pantalla
        // -------------------------------------------------------------------
        
        ForwardModal(
            isVisible = showForwardModal,
            post = dummyPostForModals,
            onDismiss = { showForwardModal = false },
            onForwardToUser = { _, _, _ -> showForwardModal = false }
        )
        
        ConsultModal(
            isVisible = showConsultModal,
            post = dummyPostForModals,
            onDismiss = { showConsultModal = false },
            onSendConsult = { showConsultModal = false }
        )
        
        CommentChoiceModal(
            isVisible = showCommentChoiceModal,
            onDismiss = { showCommentChoiceModal = false },
            onViewComments = {
                showCommentChoiceModal = false
                showCommentsSheet = true
            },
            onMakeConsult = {
                showCommentChoiceModal = false
                showConsultModal = true
            }
        )
        
        CommentsSheet(
            isVisible = showCommentsSheet,
            comments = commentsFromRepo.map { comment ->
                Comment(
                    id = comment.id,
                    userId = comment.userId,
                    username = comment.username,
                    avatarUrl = comment.avatarUrl ?: "",
                    text = comment.text,
                    timeAgo = formatCommentTimeRend(comment.createdAt),
                    likes = comment.likes,
                    isLiked = comment.isLiked,
                    rating = comment.rating,
                    replies = comment.replies.map { r ->
                        Comment(
                            id = r.id,
                            userId = r.userId,
                            username = r.username,
                            avatarUrl = r.avatarUrl ?: "",
                            text = r.text,
                            timeAgo = formatCommentTimeRend(r.createdAt),
                            likes = r.likes,
                            isLiked = r.isLiked,
                            rating = r.rating,
                            isVerified = r.isVerified
                        )
                    },
                    replyCount = comment.replyCount,
                    isVerified = comment.isVerified
                )
            },
            onDismiss = { 
                showCommentsSheet = false 
                CommentRepository.clearComments()
            },
            onSendComment = { text, rating ->
                currentRendForModals?.let { rend ->
                    scope.launch {
                        commentError = null // Limpiar error previo
                        
                        val productId = activeProductId
                        if (productId != null) {
                            // SIEMPRE usar sistema unificado product_reviews
                            val success = CommentRepository.addProductReview(
                                productId = productId,
                                sourceId = if (isLinkedToPost) rend.productLink!! else rend.id,
                                sourceType = if (isLinkedToPost) "post" else "rend",
                                text = text,
                                userAvatar = currentUserAvatar,
                                userName = currentUsername,
                                rating = rating,
                                isVerified = currentUserIsVerified
                            )
                            
                            if (success) {
                                // Notificar al dueño del rend
                                NotificationRepository.createCommentNotification(
                                    recipientId = rend.userId,
                                    postId = rend.id,
                                    postImage = rend.thumbnailUrl,
                                    commentText = text
                                )
                            }
                        }
                    }
                }
            },
            onLikeComment = { commentId ->
                scope.launch {
                    CommentRepository.likeProductReview(commentId)
                }
            },
            onReplyComment = { parentId, text ->
                currentRendForModals?.let { rend ->
                    scope.launch {
                        val productId = activeProductId
                        if (productId != null) {
                            CommentRepository.addProductReviewReply(
                                productId = productId,
                                parentId = parentId,
                                sourceId = if (isLinkedToPost) rend.productLink!! else rend.id,
                                sourceType = if (isLinkedToPost) "post" else "rend",
                                text = text,
                                userAvatar = currentUserAvatar,
                                userName = currentUsername
                            )
                        }
                    }
                }
            },
            onDeleteComment = { commentId ->
                scope.launch {
                    CommentRepository.deleteProductReview(commentId)
                }
            },
            isLoading = isCommentsLoading,
            currentUserAvatar = currentUserAvatar,
            currentUsername = currentUsername,
            currentUserId = currentAuthUser?.id,
            errorMessage = commentError,
            onDismissError = { commentError = null }
        )
        
        ProductPage(
            post = dummyPostForModals,
            isVisible = isProductPageOpen,
            isFromRend = true,
            onDismiss = { isProductPageOpen = false }
        )
    }
}

@Composable
private fun RendHeader(onNavigateToTendencias: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tendencias",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onNavigateToTendencias() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
            
            Text(text = "|", color = Color.White.copy(alpha = 0.3f), fontSize = 15.sp)
            
            Text(
                text = "Recomendados",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
            
            Text(text = "|", color = Color.White.copy(alpha = 0.3f), fontSize = 15.sp)
            
            Text(
                text = "Siguiendo",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun EmptyRendsState(onReload: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.VideoLibrary,
                    null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No hay Rends Aún",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Sé el primero en compartir un video corto",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                onClick = onReload,
                shape = RoundedCornerShape(24.dp),
                color = PrimaryPurple
            ) {
                Text(
                    "Recargar",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun RendPageContent(
    rend: Rend,
    isPlaying: Boolean,
    onUserClick: (String) -> Unit,
    onOpenProductPage: () -> Unit,
    onOpenCommentChoice: () -> Unit,
    onOpenForward: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isLiked by remember { mutableStateOf(rend.isLiked) }
    var likeCount by remember { mutableIntStateOf(rend.likesCount) }
    var isSaved by remember { mutableStateOf(rend.isSaved) }
    var isPaused by remember { mutableStateOf(false) }
    var showPlayIcon by remember { mutableStateOf(false) }
    
    var isFollowing by remember { mutableStateOf(false) }
    var isFollowLoading by remember { mutableStateOf(false) }
    var showOptionsModal by remember { mutableStateOf(false) }
    
    LaunchedEffect(rend.userId) {
        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
        if (currentUserId != null && currentUserId != rend.userId) {
            val followType = FollowersRepository.getFollowType(currentUserId, rend.userId)
            isFollowing = followType != FollowType.NONE
        }
    }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 1f
        }
    }
    
    LaunchedEffect(rend.videoUrl) {
        val mediaItem = MediaItem.fromUri(Uri.parse(rend.videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }
    
    LaunchedEffect(isPlaying, isPaused) {
        if (isPlaying && !isPaused) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying && !isPaused) exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }
    
    // -------------------------------------------------------------------
    // CONTENEDOR DEL VIDEO CON CLIPPING ESTRICTO
    // -------------------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPaused = !isPaused
                showPlayIcon = true
                scope.launch {
                    kotlinx.coroutines.delay(800)
                    showPlayIcon = false
                }
            }
    ) {
        // Video Player con clip Máximo
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    clipToOutline = true
                    clipChildren = true
                    clipToPadding = true
                    setOutlineProvider(android.view.ViewOutlineProvider.BOUNDS)
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(0, 0, 0, 0)
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .graphicsLayer {
                    clip = true
                    shape = RectangleShape
                }
        )
        
        // Play/Pause icon overlay
        if (showPlayIcon) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(72.dp)
                )
            }
        }
        
        // Gradients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        
        // Bottom content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 70.dp, bottom = 16.dp)
        ) {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
            val isOwnVideo = currentUserId == rend.userId
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onUserClick(rend.userId) }
            ) {
                Text(
                    text = "@${rend.username}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!isOwnVideo) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryPurple
                    ) {
                        Text(
                            text = "Seguir",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = rend.description ?: rend.title,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            
            if (rend.productTitle != null || rend.productPrice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenProductPage() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryPurple.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Usar getProductImageUrl() que prioriza productImage > thumbnailUrl
                            val productImageUrl = rend.getProductImageUrl()
                            
                            if (productImageUrl != null) {
                                AsyncImage(
                                    model = productImageUrl,
                                    contentDescription = "Producto",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(6.dp))
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rend.productTitle ?: "Ver producto",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (rend.productPrice != null) {
                                Text(
                                    text = "$${String.format("%.0f", rend.productPrice)}",
                                    color = AccentGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Right side actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User avatar with follow
            Box(modifier = Modifier.size(48.dp)) {
                AsyncImage(
                    model = rend.userAvatar.ifEmpty { 
                        "https://ui-avatars.com/api/?name=${rend.username}&background=A78BFA&color=fff" 
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                        .clickable { onUserClick(rend.userId) }
                )
                
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                if (currentUserId != rend.userId && !isFollowing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 5.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AccentPink)
                            .clickable {
                                if (!isFollowLoading) {
                                    scope.launch {
                                        isFollowLoading = true
                                        val result = FollowersRepository.follow(rend.userId)
                                        if (result.isSuccess) {
                                            isFollowing = true
                                            Toast.makeText(context, "Ahora sigues a @${rend.username}", Toast.LENGTH_SHORT).show()
                                        }
                                        isFollowLoading = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFollowLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                color = Color.White,
                                strokeWidth = 1.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Seguir",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else if (isFollowing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 5.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E8B57)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Siguiendo",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            
            // Action buttons
            RendActionButton(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = likeCount,
                tint = if (isLiked) AccentPink else Color.White,
                onClick = {
                    val wasLiked = isLiked
                    isLiked = !isLiked
                    likeCount = if (isLiked) likeCount + 1 else (likeCount - 1).coerceAtLeast(0)
                    scope.launch {
                        RendRepository.toggleLike(rend.id, rend.likesCount, wasLiked)
                    }
                }
            )
            
            RendActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = rend.reviewsCount,
                onClick = { onOpenCommentChoice() }
            )
            
            RendActionButton(
                icon = Icons.Outlined.Send,
                count = rend.sharesCount,
                onClick = { onOpenForward() }
            )
            
            RendActionButton(
                icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                count = null,
                tint = if (isSaved) AccentYellow else Color.White,
                onClick = { isSaved = !isSaved }
            )
            
            RendActionButton(
                icon = Icons.Outlined.MoreHoriz,
                count = null,
                onClick = { showOptionsModal = true }
            )
        }
        
        // Modal de opciones del video
        if (showOptionsModal) {
            RendOptionsModal(
                rend = rend,
                onDismiss = { showOptionsModal = false },
                onReport = {
                    showOptionsModal = false
                    Toast.makeText(context, "Reporte enviado", Toast.LENGTH_SHORT).show()
                },
                onNotInterested = {
                    showOptionsModal = false
                    Toast.makeText(context, "No te mostraremos Más este tipo de contenido", Toast.LENGTH_SHORT).show()
                },
                onCopyLink = {
                    showOptionsModal = false
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Rend Link", "https://Merqora.app/rend/${rend.id}")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Enlace copiado", Toast.LENGTH_SHORT).show()
                },
                onDownload = {
                    showOptionsModal = false
                    Toast.makeText(context, "Descargando video...", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun RendActionButton(
    icon: ImageVector,
    count: Int?,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(30.dp)
        )
        if (count != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatCount(count),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
private fun RendSkeletonLoader() {
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A2E).copy(alpha = shimmerAlpha))
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = shimmerAlpha * 0.3f))
                )
                if (it < 2) Spacer(modifier = Modifier.width(16.dp))
            }
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = shimmerAlpha * 0.4f))
            )
            
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = shimmerAlpha * 0.3f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = shimmerAlpha * 0.2f))
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 80.dp, end = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha * 0.4f))
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha * 0.3f))
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha * 0.25f))
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = shimmerAlpha * 0.15f))
            )
        }
    }
}

@Composable
private fun CommentChoiceModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onViewComments: () -> Unit,
    onMakeConsult: () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(animationSpec = tween(200)),
        exit = androidx.compose.animation.fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )
    }
    
    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = androidx.compose.animation.slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .navigationBarsPadding(),
                color = HomeBg,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "¿Qué deseas hacer?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        onClick = onViewComments,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(PrimaryPurple.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RateReview,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ver opiniones",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Lee lo que otros usuarios opinan",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        onClick = onMakeConsult,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(AccentGold.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QuestionAnswer,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hacer consulta",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Pregunta directamente al vendedor",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// DATA CLASS PARA OBTENER DATOS DEL USUARIO ACTUAL
// -------------------------------------------------------------------------------
@Serializable
private data class UserDataForComments(
    val username: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false
)

// -------------------------------------------------------------------------------
// FUNCIÓN PARA FORMATEAR TIEMPO DE COMENTARIOS
// -------------------------------------------------------------------------------
private fun formatCommentTimeRend(createdAt: String): String {
    return try {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val date = dateFormat.parse(createdAt.substringBefore("+").substringBefore("."))
        val now = System.currentTimeMillis()
        val diff = now - (date?.time ?: now)
        
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)
        
        when {
            minutes < 1 -> "ahora"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> "${days / 7}sem"
        }
    } catch (e: Exception) {
        "ahora"
    }
}

// -------------------------------------------------------------------------------
// MODAL DE OPCIONES DEL VIDEO - Estilo profesional con animación fluida
// -------------------------------------------------------------------------------
@Composable
private fun RendOptionsModal(
    rend: Rend,
    onDismiss: () -> Unit,
    onReport: () -> Unit,
    onNotInterested: () -> Unit,
    onCopyLink: () -> Unit,
    onDownload: () -> Unit
) {
    val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
    val isOwnVideo = currentUserId == rend.userId
    
    // Animación de entrada/salida
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    val backdropAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 0.6f else 0f,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "backdrop"
    )
    
    val slideOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isVisible) 0.dp else 400.dp,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "slide"
    )
    
    // Función para cerrar con animación
    val dismissWithAnimation: () -> Unit = {
        isVisible = false
        kotlinx.coroutines.MainScope().launch {
            kotlinx.coroutines.delay(250)
            onDismiss()
        }
    }
    
    // Backdrop con animación de fade
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backdropAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { dismissWithAnimation() }
    )
    
    // Modal content con animación de slide desde abajo
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset(y = slideOffset)
                .navigationBarsPadding(),
            color = HomeBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextMuted.copy(alpha = 0.3f))
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Grid de opciones principales
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OptionGridItem(
                        icon = Icons.Outlined.Link,
                        label = "Copiar\nenlace",
                        onClick = onCopyLink
                    )
                    
                    OptionGridItem(
                        icon = Icons.Outlined.Download,
                        label = "Guardar\nvideo",
                        onClick = onDownload
                    )
                    
                    OptionGridItem(
                        icon = Icons.Outlined.QrCode2,
                        label = "código\nQR",
                        onClick = { /* TODO */ }
                    )
                    
                    OptionGridItem(
                        icon = Icons.Outlined.Share,
                        label = "Compartir\nen...",
                        onClick = { /* TODO */ }
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Divider(
                    color = TextMuted.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                // Lista de opciones adicionales
                if (!isOwnVideo) {
                    OptionListItem(
                        icon = Icons.Outlined.PersonOff,
                        iconTint = TextMuted,
                        text = "No me interesa",
                        subtitle = "Ver menos contenido como este",
                        onClick = onNotInterested
                    )
                    
                    OptionListItem(
                        icon = Icons.Outlined.Flag,
                        iconTint = AccentPink,
                        text = "Reportar",
                        subtitle = "El contenido es inapropiado",
                        onClick = onReport,
                        isDestructive = true
                    )
                } else {
                    OptionListItem(
                        icon = Icons.Outlined.Analytics,
                        iconTint = PrimaryPurple,
                        text = "Ver Estadísticas",
                        subtitle = "Analiza el rendimiento de tu video",
                        onClick = { /* TODO */ }
                    )
                    
                    OptionListItem(
                        icon = Icons.Outlined.Edit,
                        iconTint = AccentGold,
                        text = "Editar Descripción",
                        subtitle = "Modifica el Título o Descripción",
                        onClick = { /* TODO */ }
                    )
                    
                    OptionListItem(
                        icon = Icons.Outlined.Delete,
                        iconTint = AccentPink,
                        text = "Eliminar video",
                        subtitle = "Esta Acción no se puede deshacer",
                        onClick = { /* TODO */ },
                        isDestructive = true
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // botón cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun OptionGridItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun OptionListItem(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    color = if (isDestructive) AccentPink else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
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
