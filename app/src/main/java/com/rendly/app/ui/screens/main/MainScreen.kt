package com.rendly.app.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rendly.app.ui.components.BottomNavBar
import com.rendly.app.ui.components.OptimizedMessagesDrawer
import com.rendly.app.ui.components.OptimizedNotificationsDrawer
import com.rendly.app.ui.screens.explore.ExploreScreen
import com.rendly.app.ui.screens.home.HomeContent
import com.rendly.app.ui.screens.profile.EditProfileScreen
import com.rendly.app.ui.screens.profile.ProfileScreen
import com.rendly.app.ui.screens.profile.UserProfileScreen
import com.rendly.app.ui.screens.publish.PublishScreen
import com.rendly.app.ui.screens.rends.RendScreen
import com.rendly.app.ui.screens.videos.TendenciasScreen
import com.rendly.app.ui.theme.HomeBg
import com.rendly.app.ui.theme.TextMuted
import com.rendly.app.ui.theme.TextPrimary
import com.rendly.app.data.repository.ProfileRepository
import com.rendly.app.data.repository.StoryRepository
import com.rendly.app.ui.screens.profile.EditProfileData
import com.rendly.app.ui.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import com.rendly.app.data.model.Usuario
import com.rendly.app.ui.screens.chat.ChatScreen
import com.rendly.app.ui.components.StoriesViewer
import com.rendly.app.ui.components.Story
import com.rendly.app.ui.components.UserStories
import com.rendly.app.ui.components.ProductPage
import com.rendly.app.data.model.Post
import com.rendly.app.data.cache.network.SupabaseDataSource
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var showEditProfile by remember { mutableStateOf(false) }
    var isSavingProfile by remember { mutableStateOf(false) }
    
    // URIs de imágenes seleccionadas en EditProfile
    var selectedAvatarUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedBannerUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    // Drawers optimizados
    var showMessagesDrawer by remember { mutableStateOf(false) }
    var showNotificationsDrawer by remember { mutableStateOf(false) }
    
    // Estado para chat activo - solo necesitamos el usuario
    var activeChatUser by remember { mutableStateOf<Usuario?>(null) }
    
    // Estado para ocultar navbar cuando se ve perfil de otro usuario, ForwardModal, CommentsSheet, ProductPage, CartModal, SettingsModal o SearchResults
    var isStoriesViewerVisible by remember { mutableStateOf(false) }
    var isUserProfileVisible by remember { mutableStateOf(false) }
    var isForwardModalVisible by remember { mutableStateOf(false) }
    var isCommentsSheetVisible by remember { mutableStateOf(false) }
    var isProductPageVisible by remember { mutableStateOf(false) }
    var isCartModalVisible by remember { mutableStateOf(false) }
    var isSettingsModalVisible by remember { mutableStateOf(false) }
    var isExploreSubScreenVisible by remember { mutableStateOf(false) }
    var isSearchResultsVisible by remember { mutableStateOf(false) }
    
    // Estado para StoriesViewer a nivel de MainScreen (fuera de systemBarsPadding)
    var showStoriesViewerFullscreen by remember { mutableStateOf(false) }
    var storiesViewerData by remember { mutableStateOf<List<UserStories>>(emptyList()) }
    var storiesCurrentUserId by remember { mutableStateOf("") }
    
    // Estado para TendenciasScreen
    var showTendenciasScreen by remember { mutableStateOf(false) }
    
    // Estado para abrir RendScreen desde Tendencias con un rend específico
    var showRendFromTendencias by remember { mutableStateOf(false) }
    var rendIdFromTendencias by remember { mutableStateOf<String?>(null) }
    
    // Estado para HashtagDetailScreen
    var showHashtagDetail by remember { mutableStateOf(false) }
    var selectedHashtagItem by remember { mutableStateOf<com.rendly.app.data.repository.TrendingTagItem?>(null) }
    
    // Estado para ProductPage desde chat
    var showProductPageFromChat by remember { mutableStateOf(false) }
    var selectedPostIdFromChat by remember { mutableStateOf<String?>(null) }
    var selectedPostFromChat by remember { mutableStateOf<Post?>(null) }
    
    // Estado para UserProfile desde chat
    var showUserProfileFromChat by remember { mutableStateOf(false) }
    var selectedUserIdFromChat by remember { mutableStateOf<String?>(null) }
    
    // Cargar post cuando se selecciona desde chat
    LaunchedEffect(selectedPostIdFromChat) {
        selectedPostIdFromChat?.let { postId ->
            scope.launch {
                try {
                    // Fetch post y usuario en paralelo
                    val postDB = SupabaseDataSource.fetchPost(postId)
                    if (postDB != null) {
                        val users = SupabaseDataSource.fetchUsers(listOf(postDB.userId))
                        val user = users.firstOrNull()
                        
                        // Construir Post manualmente desde PostDB + Usuario
                        selectedPostFromChat = Post(
                            id = postDB.id,
                            userId = postDB.userId,
                            title = postDB.title,
                            description = postDB.description,
                            price = postDB.price,
                            previousPrice = postDB.previousPrice,
                            category = postDB.category ?: "",
                            condition = postDB.condition ?: "",
                            images = postDB.images,
                            likesCount = postDB.likesCount,
                            reviewsCount = postDB.reviewsCount,
                            savesCount = postDB.savesCount,
                            sharesCount = postDB.sharesCount,
                            createdAt = postDB.createdAt,
                            username = user?.username ?: "",
                            userAvatar = user?.avatarUrl ?: "",
                            userStoreName = user?.nombreTienda,
                            isUserVerified = user?.isVerified ?: false,
                            isLiked = false,
                            isSaved = false
                        )
                    } else {
                        selectedPostFromChat = null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainScreen", "Error cargando post: ${e.message}")
                    selectedPostFromChat = null
                }
            }
        }
    }
    
    // Estado para recargar Home al pulsar icono Home estando en Home
    var homeReclickTrigger by remember { mutableIntStateOf(0) }
    
    // Estado de navegación - navegación simple por estado
    var currentRoute by remember { mutableStateOf("home") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            // systemBarsPadding movido a cada pantalla individual para evitar saltos durante el deslizamiento
    ) {
        // Navegación simple por estado - sin deslizamiento horizontal
        when (currentRoute) {
            "home" -> HomeContent(
                onMessagesClick = { showMessagesDrawer = true },
                onNotificationsClick = { showNotificationsDrawer = true },
                onStoriesViewerVisibilityChange = { isVisible ->
                    isStoriesViewerVisible = isVisible
                },
                onUserProfileVisibilityChange = { isVisible ->
                    isUserProfileVisible = isVisible
                },
                onForwardModalVisibilityChange = { isVisible ->
                    isForwardModalVisible = isVisible
                },
                onCommentsSheetVisibilityChange = { isVisible ->
                    isCommentsSheetVisible = isVisible
                },
                onProductPageVisibilityChange = { isVisible ->
                    isProductPageVisible = isVisible
                },
                onCartModalVisibilityChange = { isVisible ->
                    isCartModalVisible = isVisible
                },
                onSearchResultsVisibilityChange = { isVisible ->
                    isSearchResultsVisible = isVisible
                },
                onOpenStoriesViewer = { stories, userId ->
                    storiesViewerData = stories
                    storiesCurrentUserId = userId
                    showStoriesViewerFullscreen = true
                },
                onOpenChatFromProfile = { user ->
                    activeChatUser = user
                },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                },
                onNavigateToProfile = { currentRoute = "profile" },
                homeReclickTrigger = homeReclickTrigger,
                showNavBar = true,
                currentNavRoute = currentRoute,
                onNavNavigate = { route -> currentRoute = route },
                onNavHomeReclick = { homeReclickTrigger++ }
            )
            "explore" -> ExploreScreen(
                onNavigateToLiveStreams = {
                    navController.navigate(Screen.LiveStreams.route)
                },
                onSubScreenVisibilityChange = { isVisible ->
                    isExploreSubScreenVisible = isVisible
                },
                showNavBar = true,
                currentNavRoute = currentRoute,
                onNavNavigate = { route -> currentRoute = route },
                onNavHomeReclick = { homeReclickTrigger++ }
            )
            "sell" -> PublishScreen(
                onClose = { currentRoute = "home" },
                onStoryPublished = {
                    scope.launch {
                        kotlinx.coroutines.delay(1500)
                        StoryRepository.loadMyStories()
                    }
                },
                onNavigateToHome = { currentRoute = "home" },
                onEditingStateChange = { _ -> },
                initialMode = 1
            )
            "videos" -> RendScreen(
                onNavigateToProfile = { userId ->
                    selectedUserIdFromChat = userId
                    showUserProfileFromChat = true
                },
                onNavigateToTendencias = { showTendenciasScreen = true },
                isScreenVisible = currentRoute == "videos",
                showNavBar = true,
                currentNavRoute = currentRoute,
                onNavNavigate = { route -> currentRoute = route },
                onNavHomeReclick = { homeReclickTrigger++ }
            )
            "profile" -> ProfileScreen(
                onEditProfile = { showEditProfile = true },
                onStoriesViewerVisibilityChange = { isVisible ->
                    isStoriesViewerVisible = isVisible
                },
                onSettingsModalVisibilityChange = { isVisible ->
                    isSettingsModalVisible = isVisible
                },
                onLogout = {
                    // Navegar a la pantalla de login limpiando toda la pila
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                showNavBar = true,
                currentNavRoute = currentRoute,
                onNavNavigate = { route -> currentRoute = route },
                onNavHomeReclick = { homeReclickTrigger++ }
            )
        }
        
        // NavBar REMOVIDO de aquí - ahora está embebido en cada pantalla individual
        // Esto evita el salto visual al deslizar hacia/desde Publish
        
        // Modal de Editar Perfil - ocupa toda la pantalla sin navbar
        if (showEditProfile) {
            val currentProfile by ProfileRepository.currentProfile.collectAsState()
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg)
            ) {
                EditProfileScreen(
                    initialData = EditProfileData(
                        nombre = currentProfile?.nombre ?: "",
                        username = currentProfile?.username ?: "",
                        descripcion = currentProfile?.descripcion ?: "",
                        ubicacion = currentProfile?.ubicacion ?: "",
                        telefono = currentProfile?.telefono ?: "",
                        sexo = currentProfile?.sexo ?: "",
                        nombreTienda = currentProfile?.nombreTienda ?: "",
                        avatarUrl = currentProfile?.avatarUrl,
                        bannerUrl = currentProfile?.bannerUrl
                    ),
                    isSaving = isSavingProfile,
                    onBack = { 
                        selectedAvatarUri = null
                        selectedBannerUri = null
                        showEditProfile = false 
                    },
                    onSave = { data, avatarUri, bannerUri ->
                        android.util.Log.d("MainScreen", "=== onSave LLAMADO ===")
                        android.util.Log.d("MainScreen", "Data: $data")
                        scope.launch {
                            android.util.Log.d("MainScreen", "Iniciando guardado...")
                            isSavingProfile = true
                            try {
                                val result = ProfileRepository.updateProfile(
                                    context = context,
                                    data = data,
                                    avatarUri = avatarUri,
                                    bannerUri = bannerUri
                                )
                                android.util.Log.d("MainScreen", "Resultado: ${result.isSuccess}")
                                if (result.isFailure) {
                                    android.util.Log.e("MainScreen", "Error: ${result.exceptionOrNull()?.message}")
                                }
                                isSavingProfile = false
                                if (result.isSuccess) {
                                    android.util.Log.d("MainScreen", "Guardado exitoso, cerrando modal...")
                                    selectedAvatarUri = null
                                    selectedBannerUri = null
                                    showEditProfile = false
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MainScreen", "Excepción: ${e.message}", e)
                                isSavingProfile = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                )
            }
        }
        
        // Drawers optimizados con GPU acceleration
        // El drawer se mantiene visible (debajo) cuando hay un chat activo para evitar el efecto de cierre/apertura
        OptimizedMessagesDrawer(
            isVisible = showMessagesDrawer || activeChatUser != null,
            onDismiss = { 
                if (activeChatUser == null) {
                    showMessagesDrawer = false 
                }
            },
            onOpenChat = { user ->
                activeChatUser = user
                // NO cerramos el drawer, queda debajo del chat
            }
        )
        
        OptimizedNotificationsDrawer(
            isVisible = showNotificationsDrawer,
            onDismiss = { showNotificationsDrawer = false }
        )
    }
    
    // Chat Screen con animación de slide fluido hacia la derecha
    val chatUser = activeChatUser
    val chatOffsetX = remember { Animatable(if (chatUser != null) 0f else 1f) }
    
    LaunchedEffect(activeChatUser) {
        chatOffsetX.animateTo(
            targetValue = if (activeChatUser != null) 0f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }
    
    if (activeChatUser != null || chatOffsetX.value < 1f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .graphicsLayer {
                    translationX = chatOffsetX.value * size.width
                }
                .background(HomeBg)
                .systemBarsPadding()
        ) {
            // Mantener referencia al usuario actual para evitar null durante animación
            val currentChatUser = activeChatUser ?: chatUser
            currentChatUser?.let { user ->
                ChatScreen(
                    otherUser = user,
                    onBack = {
                        activeChatUser = null
                    },
                    onOpenChatList = {
                        // Solo cerrar el chat - el drawer ya está visible debajo
                        activeChatUser = null
                    },
                    onOpenProduct = { postId ->
                        selectedPostIdFromChat = postId
                        showProductPageFromChat = true
                    },
                    onNavigateToUserProfile = { userId ->
                        selectedUserIdFromChat = userId
                        showUserProfileFromChat = true
                    }
                )
            }
        }
    }
    
    // StoriesViewer a nivel raíz - FUERA del Box con systemBarsPadding para ocupar toda la pantalla
    if (showStoriesViewerFullscreen && storiesViewerData.isNotEmpty()) {
        StoriesViewer(
            userStories = storiesViewerData,
            currentUserId = storiesCurrentUserId,
            onClose = {
                showStoriesViewerFullscreen = false
                storiesViewerData = emptyList()
            },
            onStoryViewed = { storyId ->
                // Marcar story como vista para cambiar el color del borde en el carrusel
                StoryRepository.markStoryAsViewed(storyId)
                // Registrar vista en Supabase
                scope.launch {
                    StoryRepository.recordStoryView(storyId)
                }
            },
            onDeleteStory = { storyId ->
                scope.launch {
                    StoryRepository.deleteStory(storyId)
                }
            },
            onReply = { storyId, message ->
                // Enviar respuesta de story al chat del usuario
                scope.launch {
                    // Buscar a qué usuario pertenece esta story
                    val storyOwner = storiesViewerData.flatMap { it.stories }
                        .find { it.id == storyId }
                    
                    storyOwner?.let { story ->
                        val conversationId = com.rendly.app.data.repository.ChatRepository.getOrCreateConversation(story.userId)
                        if (conversationId != null) {
                            // Formato profesional estilo Instagram
                            val storyReplyMessage = "📷 Respondió a tu historia\n\n\"$message\""
                            com.rendly.app.data.repository.ChatRepository.sendMessage(conversationId, storyReplyMessage)
                            android.widget.Toast.makeText(context, "Mensaje enviado", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    // TendenciasScreen - pantalla completa cuando se navega desde VideosScreen
    if (showTendenciasScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
                .background(HomeBg)
                .systemBarsPadding()
        ) {
            TendenciasScreen(
                onBack = { showTendenciasScreen = false },
                onTrendClick = { trendItem ->
                    // Navigate to RendScreen with this specific rend
                    rendIdFromTendencias = trendItem.id
                    showRendFromTendencias = true
                },
                onHashtagClick = { hashtagItem ->
                    selectedHashtagItem = hashtagItem
                    showHashtagDetail = true
                }
            )
        }
    }
    
    // RendScreen desde Tendencias - abrir video específico
    if (showRendFromTendencias) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(150f)
                .background(HomeBg)
        ) {
            RendScreen(
                onNavigateToProfile = { userId ->
                    selectedUserIdFromChat = userId
                    showUserProfileFromChat = true
                },
                onNavigateToTendencias = {},
                isScreenVisible = true,
                showNavBar = false,
                initialRendId = rendIdFromTendencias
            )
            // Back button overlay
            IconButton(
                onClick = {
                    showRendFromTendencias = false
                    rendIdFromTendencias = null
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
    
    // HashtagDetailScreen
    if (showHashtagDetail && selectedHashtagItem != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(150f)
                .background(HomeBg)
        ) {
            com.rendly.app.ui.screens.videos.HashtagDetailScreen(
                hashtag = selectedHashtagItem!!,
                onBack = {
                    showHashtagDetail = false
                    selectedHashtagItem = null
                },
                onVideoClick = { trendItem ->
                    rendIdFromTendencias = trendItem.id
                    showRendFromTendencias = true
                }
            )
        }
    }
    
    // ProductPage desde chat - sobre todas las pantallas
    if (showProductPageFromChat && selectedPostFromChat != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(200f) // Mayor que TendenciasScreen (100f) para estar encima de todo
        ) {
            ProductPage(
                post = selectedPostFromChat,
                isVisible = true,
                onDismiss = {
                    showProductPageFromChat = false
                    selectedPostIdFromChat = null
                    selectedPostFromChat = null
                },
                onBuyNow = { /* TODO */ },
                onAddToCart = { /* TODO */ },
                onContactSeller = { /* TODO */ },
                onShare = { /* TODO */ },
                onFavorite = { /* TODO */ },
                onLike = { /* TODO */ },
                onSave = { /* TODO */ },
                onForward = { /* TODO */ },
                onViewAllReviews = { /* TODO */ },
                onRelatedPostClick = { /* TODO */ }
            )
        }
    }
    
    // UserProfile desde chat - sobre todas las pantallas
    if (showUserProfileFromChat && selectedUserIdFromChat != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(200f)
                .background(HomeBg)
                .systemBarsPadding()
        ) {
            UserProfileScreen(
                userId = selectedUserIdFromChat!!,
                onBack = {
                    showUserProfileFromChat = false
                    selectedUserIdFromChat = null
                },
                onPostClick = { post ->
                    // Abrir producto desde el perfil
                    selectedPostFromChat = post
                    showProductPageFromChat = true
                },
                onOpenChat = { user ->
                    // Cerrar perfil y abrir chat con este usuario
                    showUserProfileFromChat = false
                    selectedUserIdFromChat = null
                    activeChatUser = user
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, emoji: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Próximamente",
                fontSize = 14.sp,
                color = TextMuted
            )
        }
    }
}
