package com.mercora.app.ui.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mercora.app.ui.components.BottomNavBar
import com.mercora.app.ui.components.OptimizedMessagesDrawer
import com.mercora.app.ui.components.OptimizedNotificationsDrawer
import com.mercora.app.ui.screens.explore.ExploreScreen
import com.mercora.app.ui.screens.home.HomeContent
import com.mercora.app.ui.screens.profile.EditProfileScreen
import com.mercora.app.ui.screens.profile.ProfileScreen
import com.mercora.app.ui.screens.profile.UserProfileScreen
import com.mercora.app.ui.screens.publish.PublishScreen
import com.mercora.app.ui.screens.rends.RendScreen
import com.mercora.app.ui.screens.videos.TendenciasScreen
import com.mercora.app.ui.theme.HomeBg
import com.mercora.app.ui.theme.TextMuted
import com.mercora.app.ui.theme.TextPrimary
import com.mercora.app.data.repository.ProfileRepository
import com.mercora.app.data.repository.StoryRepository
import com.mercora.app.ui.screens.profile.EditProfileData
import com.mercora.app.ui.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import com.mercora.app.data.model.Usuario
import com.mercora.app.ui.screens.chat.ChatScreen
import com.mercora.app.ui.components.StoriesViewer
import com.mercora.app.ui.components.Story
import com.mercora.app.ui.components.UserStories
import com.mercora.app.data.model.Post
import com.mercora.app.ui.components.LocalOpenProductPreview
import com.mercora.app.ui.components.ProductPreviewConfig
import com.mercora.app.data.cache.network.SupabaseDataSource
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
    // ID de conversación del chat activo
    var activeChatConversationId by remember { mutableStateOf<String?>(null) }
    
    // Estado para ocultar navbar cuando se ven ciertos overlays
    var isStoriesViewerVisible by remember { mutableStateOf(false) }
    var isUserProfileVisible by remember { mutableStateOf(false) }
    var isForwardModalVisible by remember { mutableStateOf(false) }
    var isCommentsSheetVisible by remember { mutableStateOf(false) }
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
    
    // Navigation to videos section with specific rend is handled via RendRepository.setPendingRendId()
    
    // Estado para HashtagDetailScreen
    var showHashtagDetail by remember { mutableStateOf(false) }
    var selectedHashtagItem by remember { mutableStateOf<com.mercora.app.data.repository.TrendingTagItem?>(null) }
    
    // Product preview â€” delegated to centralized overlay via CompositionLocal
    val openPreview = LocalOpenProductPreview.current
    
    // Estado para UserProfile desde chat
    var showUserProfileFromChat by remember { mutableStateOf(false) }
    var selectedUserIdFromChat by remember { mutableStateOf<String?>(null) }
    
    // Estado para recargar Home al pulsar icono Home estando en Home
    var homeReclickTrigger by remember { mutableIntStateOf(0) }
    
    // Estado de navegación - navegación simple por estado
    var currentRoute by remember { mutableStateOf("home") }
    
    // LazyListState del Home preservado en MainScreen para que sobreviva
    // a la destrucción/recreación de HomeContent al cambiar de tab
    val homeListState = rememberLazyListState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            // systemBarsPadding movido a cada pantalla individual para evitar saltos durante el deslizamiento
    ) {
        // Navegación simple por estado - sin deslizamiento horizontal
        when (currentRoute) {
            "home" -> HomeContent(
                homeListState = homeListState,
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
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                },
                isScreenVisible = currentRoute == "videos",
                showNavBar = true,
                currentNavRoute = currentRoute,
                onNavNavigate = { route -> currentRoute = route },
                onNavHomeReclick = { homeReclickTrigger++ }
            )
        }
        
        // ProfileScreen: SOLO se renderiza cuando está activa
        if (currentRoute == "profile") {
            ProfileScreen(
                onEditProfile = { showEditProfile = true },
                onStoriesViewerVisibilityChange = { isVisible ->
                    isStoriesViewerVisible = isVisible
                },
                onSettingsModalVisibilityChange = { isVisible ->
                    isSettingsModalVisible = isVisible
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                },
                showNavBar = true,
                currentNavRoute = currentRoute,
                onNavNavigate = { route ->
                    currentRoute = route
                },
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
            onOpenChat = { user, convId ->
                activeChatUser = user
                activeChatConversationId = convId
                // NO cerramos el drawer, queda debajo del chat
            }
        )
        
        OptimizedNotificationsDrawer(
            isVisible = showNotificationsDrawer,
            onDismiss = { showNotificationsDrawer = false },
            onNavigateToProfile = { userId ->
                navController.navigate(Screen.Profile.createRoute(userId))
            }
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
                    conversationId = activeChatConversationId,
                    onBack = {
                        activeChatUser = null
                        activeChatConversationId = null
                    },
                    onOpenChatList = {
                        // Solo cerrar el chat - el drawer ya está visible debajo
                        activeChatUser = null
                        activeChatConversationId = null
                    },
                    onOpenProduct = { postId ->
                        scope.launch {
                            try {
                                val postDB = SupabaseDataSource.fetchPost(postId)
                                if (postDB != null) {
                                    val users = SupabaseDataSource.fetchUsers(listOf(postDB.userId))
                                    val user = users.firstOrNull()
                                    val post = Post(
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
                                        freeShipping = postDB.freeShipping ?: false
                                    )
                                    openPreview(ProductPreviewConfig(
                                        post = post,
                                        onContactSeller = { p ->
                                            scope.launch {
                                                try {
                                                    val sellerUsers = SupabaseDataSource.fetchUsers(listOf(p.userId))
                                                    val seller = sellerUsers.firstOrNull()
                                                    if (seller != null) activeChatUser = seller
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    ))
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MainScreen", "Error cargando post: ${e.message}")
                            }
                        }
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
                        val conversationId = com.mercora.app.data.repository.ChatRepository.getOrCreateConversation(story.userId)
                        if (conversationId != null) {
                            // Formato profesional estilo Instagram
                            val storyReplyMessage = "📷 Respondió a tu historia\n\n\"$message\""
                            com.mercora.app.data.repository.ChatRepository.sendMessage(conversationId, storyReplyMessage)
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
                    // Navigate to videos section with this specific rend
                    showTendenciasScreen = false
                    com.mercora.app.data.repository.RendRepository.setPendingRendId(trendItem.id)
                    currentRoute = "videos"
                },
                onHashtagClick = { hashtagItem ->
                    selectedHashtagItem = hashtagItem
                    showHashtagDetail = true
                }
            )
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
            com.mercora.app.ui.screens.videos.HashtagDetailScreen(
                hashtag = selectedHashtagItem!!,
                onBack = {
                    showHashtagDetail = false
                    selectedHashtagItem = null
                },
                onVideoClick = { trendItem ->
                    showHashtagDetail = false
                    selectedHashtagItem = null
                    showTendenciasScreen = false
                    com.mercora.app.data.repository.RendRepository.setPendingRendId(trendItem.id)
                    currentRoute = "videos"
                }
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
                    openPreview(ProductPreviewConfig(
                        post = post,
                        onContactSeller = { p ->
                            scope.launch {
                                try {
                                    val sellerUsers = SupabaseDataSource.fetchUsers(listOf(p.userId))
                                    val seller = sellerUsers.firstOrNull()
                                    if (seller != null) activeChatUser = seller
                                } catch (_: Exception) {}
                            }
                        }
                    ))
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
