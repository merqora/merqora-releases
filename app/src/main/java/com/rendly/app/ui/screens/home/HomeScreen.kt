package com.rendly.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rendly.app.ui.components.BottomNavBar
import com.rendly.app.ui.components.CategoryDrawer
import com.rendly.app.ui.components.settings.HelpCenterScreen
import com.rendly.app.ui.components.settings.TermsAndConditionsScreen
import com.rendly.app.ui.components.settings.PrivacyPolicyScreen
import com.rendly.app.ui.components.Comment
import com.rendly.app.ui.components.CommentsSheet
import com.rendly.app.ui.components.ConsultModal
import com.rendly.app.ui.components.ForwardModal
import com.rendly.app.ui.components.HomeHeader
import com.rendly.app.ui.components.MyStoryBanner
import com.rendly.app.ui.components.OptimizedMessagesDrawer
import com.rendly.app.ui.components.OptimizedNotificationsDrawer
import com.rendly.app.ui.components.PostItem
import com.rendly.app.ui.components.PostOptionsModal
import com.rendly.app.ui.components.SearchBar
import com.rendly.app.ui.components.StoriesViewer
import com.rendly.app.ui.components.Story
import com.rendly.app.ui.components.StoriesCarousel
import com.rendly.app.ui.components.StoryViewersModal
import com.rendly.app.ui.components.UploadProgressBanner
import com.rendly.app.ui.components.UserStories
import com.rendly.app.ui.components.ProductPage
import com.rendly.app.ui.components.CartModal
import com.rendly.app.ui.components.RendsCarousel
import com.rendly.app.ui.components.ReportModal
import com.rendly.app.ui.components.HiddenPostPlaceholder
import com.rendly.app.ui.components.PostQrCodeModal
import com.rendly.app.ui.components.FeaturedPostsSection
import com.rendly.app.data.repository.CartRepository
import com.rendly.app.util.FCMHelper
import com.rendly.app.data.repository.ChatRepository
import com.rendly.app.data.repository.ExploreRepository
import com.rendly.app.ui.screens.profile.UserProfileScreen
import com.rendly.app.ui.screens.search.SearchResultsScreen
import com.rendly.app.ui.screens.rends.RendScreen
import com.rendly.app.data.model.Post
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.model.StoryUploadState
import com.rendly.app.data.repository.CommentRepository
import com.rendly.app.data.repository.NotificationRepository
import com.rendly.app.data.repository.PostRepository
import com.rendly.app.data.repository.ProfileRepository
import com.rendly.app.data.repository.StoryRepository
import com.rendly.app.data.repository.ViewTracker
import com.rendly.app.data.cache.BadgeCountCache
import com.rendly.app.ui.theme.*
import com.rendly.app.native.FeedEngine
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val currentRoute by viewModel.currentRoute.collectAsState()
    
    val homeBg = themedHomeBg()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(homeBg)
            .systemBarsPadding()
    ) {
        HomeContent(viewModel = viewModel)
        
        BottomNavBar(
            currentRoute = currentRoute,
            onNavigate = { route -> viewModel.navigateTo(route) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HomeContent(
    viewModel: HomeViewModel = hiltViewModel(),
    onMessagesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onStoriesViewerVisibilityChange: (Boolean) -> Unit = {},
    onUserProfileVisibilityChange: (Boolean) -> Unit = {},
    onForwardModalVisibilityChange: (Boolean) -> Unit = {},
    onCommentsSheetVisibilityChange: (Boolean) -> Unit = {},
    onProductPageVisibilityChange: (Boolean) -> Unit = {},
    onCartModalVisibilityChange: (Boolean) -> Unit = {},
    onSearchResultsVisibilityChange: (Boolean) -> Unit = {},
    onOpenStoriesViewer: (List<UserStories>, String) -> Unit = { _, _ -> },
    onOpenChatFromProfile: (Usuario) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    homeReclickTrigger: Int = 0,
    // NavBar embebido
    showNavBar: Boolean = true,
    currentNavRoute: String = "home",
    onNavNavigate: (String) -> Unit = {},
    onNavHomeReclick: () -> Unit = {}
) {
    val context = LocalContext.current
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMorePosts by viewModel.hasMorePosts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val listState = rememberLazyListState()
    
    // Avatar desde ProfileRepository (más confiable)
    val profileFromRepo by ProfileRepository.currentProfile.collectAsState()
    val userAvatarUrl = profileFromRepo?.avatarUrl ?: currentUser?.avatarUrl
    
    // ═══════════════════════════════════════════════════════════════════
    // COLD START OPTIMIZATION V2: Carga ultra-diferida post-first-frame
    // 
    // Estrategia: Mostrar UI instantáneamente, cargar datos en 2 fases
    // FASE 1: Datos críticos para primer scroll (posts) - ya manejado por ViewModel
    // FASE 2: Datos secundarios diferidos 300ms (stories, rends, notificaciones)
    // ═══════════════════════════════════════════════════════════════════
    val initScope = rememberCoroutineScope()
    
    // Variable para controlar si ya se ejecutó la inicialización diferida
    var deferredInitDone by remember { mutableStateOf(false) }
    
    // Solicitar permiso de notificaciones (Android 13+)
    LaunchedEffect(Unit) {
        // Solicitar permiso de notificaciones si es necesario
        val activity = context as? android.app.Activity
        activity?.let {
            if (!FCMHelper.hasNotificationPermission(context)) {
                FCMHelper.requestNotificationPermission(it)
            }
        }
    }
    
    // Inicializar cache de badges INMEDIATAMENTE (sincrono, sin I/O de red)
    LaunchedEffect(Unit) {
        BadgeCountCache.init(context)
        CartRepository.initCache()
        ViewTracker.init(context)
    }
    
    LaunchedEffect(Unit) {
        // FASE 1: FeedEngine en background (no bloquea composición)
        initScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            FeedEngine.init(2000f, 50000f)
        }
        
        // FASE 2: Diferir carga de datos secundarios 300ms post-first-frame
        // Esto permite que el primer frame se dibuje ANTES de cualquier I/O
        kotlinx.coroutines.delay(300)
        
        if (!deferredInitDone) {
            deferredInitDone = true
            // Cargar perfil del usuario actual PRIMERO (para MyStoryBanner avatar)
            initScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                ProfileRepository.loadCurrentProfile()
            }
            // Cargar en paralelo TODOS los datos secundarios
            initScope.launch(kotlinx.coroutines.Dispatchers.IO) { 
                StoryRepository.loadMyStories() 
            }
            initScope.launch(kotlinx.coroutines.Dispatchers.IO) { 
                StoryRepository.loadOtherUsersStories() 
            }
            initScope.launch(kotlinx.coroutines.Dispatchers.IO) { 
                com.rendly.app.data.repository.RendRepository.loadRends() 
            }
            // Carrito desde Supabase (badges ya visibles por cache)
            initScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                CartRepository.loadCartFromSupabase()
            }
            // Notificaciones y mensajes en paralelo (badges ya visibles por cache)
            initScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                NotificationRepository.loadNotifications()
                NotificationRepository.subscribeToRealtime()
            }
            initScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                ChatRepository.loadConversations()
                ChatRepository.subscribeToGlobalMessages()
            }
        }
    }
    
    // OPTIMIZADO: Solo actualizar FeedEngine cuando cambia el índice visible (no en cada frame)
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(firstVisibleIndex) {
        val totalItems = listState.layoutInfo.totalItemsCount
        if (totalItems > 0) {
            FeedEngine.setContentHeight(totalItems * 500f)
        }
    }
    
    // VIEW TRACKING: Registrar vistas de posts visibles en pantalla
    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                for (item in visibleItems) {
                    val key = item.key
                    if (key is String && key != "header" && key != "search" && key != "story_banner" 
                        && key != "stories_carousel" && key != "rends_carousel" 
                        && key != "featured_posts" && key != "refresh_spinner"
                        && key != "upload_banner" && key != "loading_more") {
                        ViewTracker.trackPostView(key)
                    }
                }
            }
    }
    
    // Prefetch inteligente basado en posición de scroll
    val shouldPrefetchMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            // Prefetch cuando estamos a 3 items del final
            lastVisibleItem >= totalItems - 3 && hasMorePosts && !isLoadingMore && posts.isNotEmpty()
        }
    }
    
    LaunchedEffect(shouldPrefetchMore) {
        if (shouldPrefetchMore) {
            viewModel.loadMorePosts()
        }
    }
    
    // Scroll to top y recargar cuando se pulsa Home estando en Home
    LaunchedEffect(homeReclickTrigger) {
        if (homeReclickTrigger > 0) {
            listState.animateScrollToItem(0)
            viewModel.refreshPosts()
        }
    }
    
    // Mostrar Toast con errores
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var showCategoryDrawer by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    
    // States para pantallas de ayuda y legal desde CategoryDrawer
    var showHelpCenter by remember { mutableStateOf(false) }
    var showTermsAndConditions by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    
    // SearchResultsScreen state
    var showSearchResults by remember { mutableStateOf(false) }
    var searchResultsQuery by remember { mutableStateOf("") }
    
    // Notificar cambio de visibilidad de SearchResultsScreen para ocultar NavBar
    LaunchedEffect(showSearchResults) {
        onSearchResultsVisibilityChange(showSearchResults)
    }
    
    var selectedPostForComments by remember { mutableStateOf<Post?>(null) }
    var showConsultModal by remember { mutableStateOf(false) }
    var selectedPostForConsult by remember { mutableStateOf<Post?>(null) }
    
    // Forward Modal state
    var showForwardModal by remember { mutableStateOf(false) }
    var selectedPostForForward by remember { mutableStateOf<Post?>(null) }
    
    // ProductPage state
    var showProductPage by remember { mutableStateOf(false) }
    var selectedPostForProduct by remember { mutableStateOf<Post?>(null) }
    
    // Cart Modal state
    var showCartModal by remember { mutableStateOf(false) }
    
    // UserProfile state
    var showUserProfile by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    
    // RendScreen state - para abrir RendScreen al pulsar un rend
    var showRendScreen by remember { mutableStateOf(false) }
    var selectedRendId by remember { mutableStateOf<String?>(null) }
    
    // PostOptionsModal state
    var showPostOptionsModal by remember { mutableStateOf(false) }
    var selectedPostForOptions by remember { mutableStateOf<Post?>(null) }
    var isSelectedPostOwn by remember { mutableStateOf(false) }
    
    // ReportModal state
    var showReportModal by remember { mutableStateOf(false) }
    var reportPostId by remember { mutableStateOf("") }
    var reportUsername by remember { mutableStateOf("") }
    
    // QR Code Modal state
    var showQrCodeModal by remember { mutableStateOf(false) }
    var qrCodePost by remember { mutableStateOf<Post?>(null) }
    
    // Hidden posts state - tracks locally hidden posts
    var hiddenPostIds by remember { mutableStateOf(setOf<String>()) }
    
    // Post pending hide actions (for showing HiddenPostPlaceholder)
    var postWithHideOptions by remember { mutableStateOf<Post?>(null) }
    
    // Posts que ya procesaron su acción y deben desaparecer completamente
    var fullyHiddenPostIds by remember { mutableStateOf(setOf<String>()) }
    
    // OPTIMIZADO: Consolidar notificaciones de visibility en un solo snapshotFlow
    // Reduce de 4 LaunchedEffects a 1, eliminando overhead de coroutines
    LaunchedEffect(Unit) {
        snapshotFlow { 
            VisibilityState(showUserProfile, showForwardModal, showCommentsSheet, showProductPage) 
        }.collect { state ->
            onUserProfileVisibilityChange(state.userProfile)
            onForwardModalVisibilityChange(state.forward)
            onCommentsSheetVisibilityChange(state.comments)
            onProductPageVisibilityChange(state.product)
        }
    }
    
    // Stories state
    val storyUploadState by StoryRepository.uploadState.collectAsState()
    val myStories by StoryRepository.myStories.collectAsState()
    val otherUsersStories by StoryRepository.otherUsersStories.collectAsState()
    val viewedStoryIds by StoryRepository.viewedStoryIds.collectAsState()
    
    // OPTIMIZADO: Rends state en nivel superior (evita collectAsState dentro del LazyColumn item)
    val rendsData by com.rendly.app.data.repository.RendRepository.rends.collectAsState()
    val rendsLoading by com.rendly.app.data.repository.RendRepository.isLoading.collectAsState()
    var showStoriesViewer by remember { mutableStateOf(false) }
    var showOtherStoriesViewer by remember { mutableStateOf(false) }
    var selectedStoryUserIndex by remember { mutableIntStateOf(0) }
    var isLoadingStory by remember { mutableStateOf(false) }
    var showStoryViewersModal by remember { mutableStateOf(false) }
    var selectedStoryForViewers by remember { mutableStateOf("") }
    val storyScope = rememberCoroutineScope()
    
    // OPTIMIZADO: Memoizar cálculo de vistas totales
    val myStoriesViewsCount = remember(myStories) { myStories.sumOf { it.views } }
    
    // OPTIMIZADO CRÍTICO: Memoizar listas de posts para evitar recreación en cada frame
    // NO filtrar posts ocultos de las listas - el renderer maneja placeholder/hidden inline
    // Esto evita duplicate keys entre firstThreePosts y allRemainingPosts
    val firstThreePosts = remember(posts) { posts.take(3) }
    val remainingPosts = remember(posts) { if (posts.size >= 3) posts.drop(3) else posts }
    
    // Post upload state
    val postUploadState by PostRepository.uploadState.collectAsState()
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Recargar stories cuando termine una subida
    LaunchedEffect(storyUploadState.isUploading) {
        if (!storyUploadState.isUploading && storyUploadState.progress >= 1f) {
            kotlinx.coroutines.delay(500)
            StoryRepository.loadMyStories()
        }
    }
    
    // Recargar posts cuando termine la subida de un post
    LaunchedEffect(postUploadState.isComplete) {
        if (postUploadState.isComplete) {
            // Esperar 2 segundos después de que el banner desaparezca
            kotlinx.coroutines.delay(2000)
            viewModel.refreshPosts()
        }
    }
    
    // Resetear isLoadingStory cuando se cierre el viewer
    LaunchedEffect(showStoriesViewer) {
        if (!showStoriesViewer) {
            isLoadingStory = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            .systemBarsPadding()
    ) {
        // Premium refresh spinner overlay (hidden until refresh)
        // Handled inside LazyColumn for smooth animation
        
        if (posts.isEmpty() && isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PrimaryPurple
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 72.dp), // Espacio para NavBar embebido
                // Optimizaciones para scroll fluido
                userScrollEnabled = true
            ) {
                // Header como parte del scroll
                item(key = "header", contentType = "header") {
                    // OPTIMIZADO: Callbacks memoizados para HomeHeader
                    val onMenuClickCallback = remember { { showCategoryDrawer = true } }
                    val onCartClickCallback = remember { { showCartModal = true } }
                    val onLogoClickCallback = remember {
                        {
                            scope.launch {
                                listState.animateScrollToItem(0)
                                kotlinx.coroutines.delay(300)
                                isRefreshing = true
                                viewModel.refreshPosts()
                                kotlinx.coroutines.delay(1000)
                                isRefreshing = false
                            }
                            Unit
                        }
                    }
                    HomeHeader(
                        onMenuClick = onMenuClickCallback,
                        onNotificationsClick = onNotificationsClick,
                        onMessagesClick = onMessagesClick,
                        onCartClick = onCartClickCallback,
                        onLogoClick = onLogoClickCallback
                    )
                }
                
                // Premium Refresh Spinner - OPTIMIZADO: Usar if simple en lugar de AnimatedVisibility
                if (isRefreshing) {
                    item(key = "refresh_spinner", contentType = "spinner") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Actualizando...",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
                
                // Banner de progreso de publicación (arriba del search)
                item(key = "upload_banner", contentType = "banner_upload") {
                    UploadProgressBanner(
                        isVisible = postUploadState.isUploading || postUploadState.isComplete || postUploadState.error != null,
                        progress = postUploadState.progress,
                        isComplete = postUploadState.isComplete,
                        error = postUploadState.error,
                        type = "post",
                        onComplete = {
                            PostRepository.resetUploadState()
                        },
                        onDismissError = {
                            PostRepository.resetUploadState()
                        }
                    )
                }
                
                // Search Bar - contentType para mejor reciclaje
                item(key = "search", contentType = "search") {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { query ->
                            searchResultsQuery = query
                            showSearchResults = true
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // My Story Banner
                item(key = "story_banner", contentType = "banner") {
                    // OPTIMIZADO: Calcular estadísticas con derivedStateOf para evitar recálculos
                    val myStoriesLikesCount = remember(myStories) { myStories.sumOf { it.likes ?: 0 } }
                    val myStoriesSharesCount = remember(myStories) { myStories.sumOf { it.shares } }
                    val myStoriesFollowersCount = remember(myStories) { myStories.sumOf { it.newFollowers } }
                    
                    // OPTIMIZADO: Callbacks con keys correctas para estados capturados
                    val storiesNotEmpty = myStories.isNotEmpty()
                    val firstStoryId = myStories.firstOrNull()?.id
                    val onPressCallback = remember(storiesNotEmpty, isLoadingStory) {
                        {
                            if (storiesNotEmpty && !isLoadingStory) {
                                isLoadingStory = true
                                storyScope.launch {
                                    kotlinx.coroutines.delay(1500)
                                    showStoriesViewer = true
                                }
                            }
                        }
                    }
                    val onAddPressCallback = remember(onNavNavigate) { { onNavNavigate("sell") } }
                    val onViewsClickCallback = remember(firstStoryId) {
                        {
                            firstStoryId?.let { id ->
                                selectedStoryForViewers = id
                                showStoryViewersModal = true
                            }
                            Unit
                        }
                    }
                    
                    MyStoryBanner(
                        username = currentUser?.username ?: "Usuario",
                        userAvatar = userAvatarUrl,
                        storiesCount = myStories.size,
                        viewsCount = myStoriesViewsCount,
                        followersCount = myStoriesFollowersCount,
                        likesCount = myStoriesLikesCount,
                        sharesCount = myStoriesSharesCount,
                        isUploading = storyUploadState.isUploading,
                        isLoading = isLoadingStory,
                        onPress = onPressCallback,
                        onAddPress = onAddPressCallback,
                        onViewsClick = onViewsClickCallback,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Stories de otros usuarios (carrusel horizontal)
                if (otherUsersStories.isNotEmpty()) {
                    item(key = "stories_carousel", contentType = "stories_carousel") {
                        // OPTIMIZADO: Callbacks memoizados para evitar recomposición
                        val onStoryClickCallback = remember {
                            { _: String, index: Int ->
                                selectedStoryUserIndex = index
                                showOtherStoriesViewer = true
                            }
                        }
                        StoriesCarousel(
                            stories = otherUsersStories,
                            viewedStoryIds = viewedStoryIds,
                            onStoryClick = onStoryClickCallback
                        )
                    }
                }
                
                // Primeros 3 posts - OPTIMIZADO: Usar lista memoizada
                if (firstThreePosts.isNotEmpty()) {
                    items(
                        items = firstThreePosts,
                        key = { it.id },
                        contentType = { 
                            when {
                                it.id in fullyHiddenPostIds -> "fully_hidden"
                                it.id in hiddenPostIds -> "hidden"
                                else -> "post"
                            }
                        }
                    ) { post ->
                        if (post.id in fullyHiddenPostIds) {
                            Spacer(modifier = Modifier.height(1.dp))
                            return@items
                        }
                        if (post.id in hiddenPostIds) {
                            HiddenPostPlaceholder(
                                username = post.username,
                                onReport = {
                                    reportPostId = post.id
                                    reportUsername = post.username
                                    showReportModal = true
                                    fullyHiddenPostIds = fullyHiddenPostIds + post.id
                                    postWithHideOptions = null
                                },
                                onMuteUser = {
                                    scope.launch {
                                        try {
                                            val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                                            if (currentUserId != null) {
                                                com.rendly.app.data.remote.SupabaseClient.database
                                                    .from("muted_users")
                                                    .insert(mapOf("muter_id" to currentUserId, "muted_id" to post.userId))
                                                android.widget.Toast.makeText(context, "@${post.username} silenciado", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("HomeScreen", "Error al silenciar: ${e.message}")
                                        }
                                    }
                                    fullyHiddenPostIds = fullyHiddenPostIds + post.id
                                    postWithHideOptions = null
                                },
                                onUnfollow = {
                                    scope.launch {
                                        try {
                                            val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                                            if (currentUserId != null) {
                                                com.rendly.app.data.remote.SupabaseClient.database
                                                    .from("followers")
                                                    .delete {
                                                        filter {
                                                            eq("follower_id", currentUserId)
                                                            eq("followed_id", post.userId)
                                                        }
                                                    }
                                                android.widget.Toast.makeText(context, "Dejaste de seguir a @${post.username}", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("HomeScreen", "Error al dejar de seguir: ${e.message}")
                                        }
                                    }
                                    fullyHiddenPostIds = fullyHiddenPostIds + post.id
                                    postWithHideOptions = null
                                },
                                onCancel = {
                                    hiddenPostIds = hiddenPostIds - post.id
                                    scope.launch {
                                        try {
                                            val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                                            if (currentUserId != null) {
                                                com.rendly.app.data.remote.SupabaseClient.database
                                                    .from("hidden_posts")
                                                    .delete {
                                                        filter {
                                                            eq("user_id", currentUserId)
                                                            eq("post_id", post.id)
                                                        }
                                                    }
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("HomeScreen", "Error al deshacer: ${e.message}")
                                        }
                                    }
                                    postWithHideOptions = null
                                }
                            )
                        } else {
                            StablePostItem(
                                post = post,
                                currentUserId = currentUser?.id,
                                viewModel = viewModel,
                                onSelectForComments = { selectedPostForComments = it; showCommentsSheet = true },
                                onSelectForProduct = { selectedPostForProduct = it; showProductPage = true },
                                onSelectForConsult = { selectedPostForConsult = it; showConsultModal = true },
                                onSelectForForward = { selectedPostForForward = it; showForwardModal = true },
                                onSelectUserId = { selectedUserId = it; showUserProfile = true },
                                onNavigateToProfile = onNavigateToProfile,
                                onSelectForOptions = { p, isOwn -> selectedPostForOptions = p; isSelectedPostOwn = isOwn; showPostOptionsModal = true }
                            )
                        }
                    }
                    
                    // Carrusel de Rends - aparece después de 3 posts
                    item(key = "rends_carousel", contentType = "rends_carousel") {
                        // Callbacks para abrir RendScreen con el rend pulsado
                        val onRendClickCallback = remember { 
                            { rend: com.rendly.app.data.model.Rend -> 
                                selectedRendId = rend.id
                                showRendScreen = true
                            } 
                        }
                        val onViewAllCallback = remember { 
                            { 
                                selectedRendId = null
                                showRendScreen = true
                            } 
                        }
                        RendsCarousel(
                            rends = rendsData,
                            isLoading = rendsLoading,
                            onRendClick = onRendClickCallback,
                            onViewAll = onViewAllCallback
                        )
                    }
                    
                    // Featured Posts Section - 6 publicaciones destacadas (después de 3 posts y rends)
                    if (posts.size >= 6) {
                        item(key = "featured_posts", contentType = "featured_posts") {
                            FeaturedPostsSection(
                                posts = posts.take(6), // Mostrar siempre 6 tarjetas
                                onPostClick = { post ->
                                    selectedPostForProduct = post
                                    showProductPage = true
                                },
                                onViewMoreClick = {
                                    // Abrir SearchResultsScreen mostrando todas las publicaciones
                                    searchResultsQuery = ""
                                    showSearchResults = true
                                },
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
                
                // Posts restantes - Incluir posts ocultos para mostrar placeholder en su lugar
                // Nota: La lista se calcula directamente ya que remember no puede usarse en LazyListScope
                val allRemainingPosts = if (posts.size >= 3) posts.drop(3) else posts
                items(
                    items = allRemainingPosts,
                    key = { it.id },
                    contentType = { 
                        when {
                            it.id in fullyHiddenPostIds -> "fully_hidden"
                            it.id in hiddenPostIds -> "hidden" 
                            else -> "post" 
                        }
                    }
                ) { post ->
                    // Si el post está completamente oculto (ya eligió una opción), no mostrar nada
                    if (post.id in fullyHiddenPostIds) {
                        // Espacio mínimo para evitar saltos - el post desapareció
                        Spacer(modifier = Modifier.height(1.dp))
                        return@items
                    }
                    // Si el post está oculto pero aún no eligió opción, mostrar placeholder
                    if (post.id in hiddenPostIds) {
                        HiddenPostPlaceholder(
                            username = post.username,
                            onReport = {
                                reportPostId = post.id
                                reportUsername = post.username
                                showReportModal = true
                                // Marcar como completamente oculto después de elegir opción
                                fullyHiddenPostIds = fullyHiddenPostIds + post.id
                                postWithHideOptions = null
                            },
                            onMuteUser = {
                                scope.launch {
                                    try {
                                        val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                                        if (currentUserId != null) {
                                            com.rendly.app.data.remote.SupabaseClient.database
                                                .from("muted_users")
                                                .insert(mapOf(
                                                    "muter_id" to currentUserId,
                                                    "muted_id" to post.userId
                                                ))
                                            android.widget.Toast.makeText(context, "@${post.username} silenciado", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("HomeScreen", "Error al silenciar: ${e.message}")
                                    }
                                }
                                // Marcar como completamente oculto
                                fullyHiddenPostIds = fullyHiddenPostIds + post.id
                                postWithHideOptions = null
                            },
                            onUnfollow = {
                                scope.launch {
                                    try {
                                        val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                                        if (currentUserId != null) {
                                            com.rendly.app.data.remote.SupabaseClient.database
                                                .from("followers")
                                                .delete {
                                                    filter {
                                                        eq("follower_id", currentUserId)
                                                        eq("followed_id", post.userId)
                                                    }
                                                }
                                            android.widget.Toast.makeText(context, "Dejaste de seguir a @${post.username}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("HomeScreen", "Error al dejar de seguir: ${e.message}")
                                    }
                                }
                                // Marcar como completamente oculto
                                fullyHiddenPostIds = fullyHiddenPostIds + post.id
                                postWithHideOptions = null
                            },
                            onCancel = {
                                // Deshacer - quitar de posts ocultos y mostrar post nuevamente
                                hiddenPostIds = hiddenPostIds - post.id
                                scope.launch {
                                    try {
                                        val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                                        if (currentUserId != null) {
                                            com.rendly.app.data.remote.SupabaseClient.database
                                                .from("hidden_posts")
                                                .delete {
                                                    filter {
                                                        eq("user_id", currentUserId)
                                                        eq("post_id", post.id)
                                                    }
                                                }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("HomeScreen", "Error al deshacer: ${e.message}")
                                    }
                                }
                                postWithHideOptions = null
                            }
                        )
                    } else {
                        // Post normal visible
                        StablePostItem(
                            post = post,
                            currentUserId = currentUser?.id,
                            viewModel = viewModel,
                            onSelectForComments = { selectedPostForComments = it; showCommentsSheet = true },
                            onSelectForProduct = { selectedPostForProduct = it; showProductPage = true },
                            onSelectForConsult = { selectedPostForConsult = it; showConsultModal = true },
                            onSelectForForward = { selectedPostForForward = it; showForwardModal = true },
                            onSelectUserId = { selectedUserId = it; showUserProfile = true },
                            onNavigateToProfile = onNavigateToProfile,
                            onSelectForOptions = { p, isOwn -> selectedPostForOptions = p; isSelectedPostOwn = isOwn; showPostOptionsModal = true }
                        )
                    }
                }
                
                // Loading more indicator
                if (isLoadingMore || hasMorePosts) {
                    item(key = "load_more", contentType = "load_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingMore) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = PrimaryPurple,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Cargando más...",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            } else if (hasMorePosts) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = TextMuted.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
                
                // No more posts indicator
                if (!hasMorePosts && posts.isNotEmpty()) {
                    item(key = "end", contentType = "end") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay más publicaciones",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Reseñas Sheet con reseñas reales
        val commentsFromRepo by CommentRepository.comments.collectAsState()
        val isCommentsLoading by CommentRepository.isLoading.collectAsState()
        
        // Cargar reseñas cuando se selecciona un post
        // SIEMPRE usar product_reviews
        LaunchedEffect(selectedPostForComments) {
            selectedPostForComments?.let { post ->
                val productId = post.productId ?: CommentRepository.getProductIdFromPostId(post.id)
                if (productId != null) {
                    CommentRepository.loadProductReviews(productId)
                }
            }
        }
        
        // OPTIMIZADO: Memoizar transformación de comentarios
        val mappedComments = remember(commentsFromRepo) {
            commentsFromRepo.map { c ->
                Comment(
                    id = c.id,
                    userId = c.userId,
                    username = c.username,
                    avatarUrl = c.avatarUrl,
                    text = c.text,
                    timeAgo = formatCommentTime(c.createdAt),
                    likes = c.likes,
                    isLiked = c.isLiked,
                    rating = c.rating,
                    replies = c.replies.map { r ->
                        Comment(
                            id = r.id,
                            userId = r.userId,
                            username = r.username,
                            avatarUrl = r.avatarUrl,
                            text = r.text,
                            timeAgo = formatCommentTime(r.createdAt),
                            likes = r.likes,
                            isLiked = r.isLiked,
                            rating = r.rating,
                            isVerified = r.isVerified
                        )
                    },
                    replyCount = c.replyCount,
                    isVerified = c.isVerified
                )
            }
        }
        
        // NavBar embebido - ANTES de todos los modales para que queden SOBRE él
        if (showNavBar) {
            BottomNavBar(
                currentRoute = currentNavRoute,
                onNavigate = onNavNavigate,
                onHomeReclick = onNavHomeReclick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // Category Drawer - DESPUÉS del NavBar para overlay correcto (queda SOBRE la navbar)
        CategoryDrawer(
            isVisible = showCategoryDrawer,
            onDismiss = { showCategoryDrawer = false },
            onCategorySelected = { categoryId ->
                showCategoryDrawer = false
                searchResultsQuery = categoryId
                showSearchResults = true
            },
            onHelpCenter = { showHelpCenter = true },
            onPrivacyPolicy = { showPrivacyPolicy = true },
            onTermsAndConditions = { showTermsAndConditions = true }
        )
        
        // Pantallas de ayuda y legal desde CategoryDrawer
        HelpCenterScreen(
            isVisible = showHelpCenter,
            onDismiss = { showHelpCenter = false }
        )
        
        TermsAndConditionsScreen(
            isVisible = showTermsAndConditions,
            onDismiss = { showTermsAndConditions = false }
        )
        
        PrivacyPolicyScreen(
            isVisible = showPrivacyPolicy,
            onDismiss = { showPrivacyPolicy = false }
        )
        
        CommentsSheet(
            isVisible = showCommentsSheet,
            comments = mappedComments,
            onDismiss = { 
                showCommentsSheet = false
                selectedPostForComments = null
                CommentRepository.clearComments()
            },
            onSendComment = { text, rating ->
                selectedPostForComments?.let { post ->
                    scope.launch {
                        val productId = post.productId ?: CommentRepository.getProductIdFromPostId(post.id)
                        if (productId != null) {
                            // SIEMPRE usar sistema unificado product_reviews
                            val success = CommentRepository.addProductReview(
                                productId = productId,
                                sourceId = post.id,
                                sourceType = "post",
                                text = text,
                                userAvatar = currentUser?.avatarUrl,
                                userName = currentUser?.username ?: "Usuario",
                                rating = rating,
                                isVerified = currentUser?.isVerified ?: false
                            )
                            if (success) {
                                // Actualizar contador de reviews en el post
                                viewModel.updateReviewsCount(post.id, increment = true)
                                
                                // Notificar al dueño del post
                                NotificationRepository.createCommentNotification(
                                    recipientId = post.userId,
                                    postId = post.id,
                                    postImage = post.images.firstOrNull(),
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
                selectedPostForComments?.let { post ->
                    scope.launch {
                        val productId = post.productId ?: CommentRepository.getProductIdFromPostId(post.id)
                        if (productId != null) {
                            CommentRepository.addProductReviewReply(
                                productId = productId,
                                parentId = parentId,
                                sourceId = post.id,
                                sourceType = "post",
                                text = text,
                                userAvatar = currentUser?.avatarUrl,
                                userName = currentUser?.username ?: "Usuario"
                            )
                        }
                    }
                }
            },
            onDeleteComment = { commentId ->
                selectedPostForComments?.let { post ->
                    scope.launch {
                        val success = CommentRepository.deleteProductReview(commentId)
                        if (success) {
                            // Actualizar contador de reviews en el post
                            viewModel.updateReviewsCount(post.id, increment = false)
                        }
                    }
                }
            },
            isLoading = isCommentsLoading,
            currentUserAvatar = userAvatarUrl,
            currentUsername = currentUser?.username ?: "Tú",
            currentUserId = currentUser?.userId
        )
        
        // Consult Modal
        ConsultModal(
            isVisible = showConsultModal,
            post = selectedPostForConsult,
            onDismiss = {
                showConsultModal = false
                selectedPostForConsult = null
            },
            onSendConsult = { consultMessage ->
                // Enviar consulta/oferta en tiempo real al vendedor
                selectedPostForConsult?.let { post ->
                    scope.launch {
                        try {
                            // Obtener o crear conversación con el vendedor del producto
                            val conversationId = ChatRepository.getOrCreateConversation(post.userId)
                            if (conversationId != null) {
                                // Formato especial para consultas: [CONSULT_POST]JSON
                                val imagesArray = org.json.JSONArray(post.producto.imagenUrl)
                                val consultData = org.json.JSONObject().apply {
                                    put("postId", post.id)
                                    put("productTitle", post.producto.titulo)
                                    put("productPrice", post.producto.precio)
                                    put("productImage", post.producto.imagenUrl.firstOrNull() ?: "")
                                    put("message", consultMessage)
                                    put("type", if (consultMessage.contains("💰 OFERTA")) "offer" else "inquiry")
                                }
                                val consultPostMessage = "[CONSULT_POST]${consultData}"
                                
                                // Enviar mensaje en tiempo real
                                val success = ChatRepository.sendMessage(conversationId, consultPostMessage)
                                if (success) {
                                    android.widget.Toast.makeText(
                                        context, 
                                        if (consultMessage.contains("💰 OFERTA")) "¡Oferta enviada!" else "¡Consulta enviada!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error enviando consulta: ${e.message}")
                            android.widget.Toast.makeText(context, "Error al enviar", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showConsultModal = false
                selectedPostForConsult = null
            }
        )
        
        // Forward Modal
        ForwardModal(
            isVisible = showForwardModal,
            post = selectedPostForForward,
            onDismiss = {
                showForwardModal = false
                selectedPostForForward = null
            },
            onForwardToUser = { user, post, customMessage ->
                // Enviar el post al usuario seleccionado via chat con formato especial
                scope.launch {
                    val conversationId = ChatRepository.getOrCreateConversation(user.userId)
                    if (conversationId != null) {
                        // Formato especial para posts compartidos: [SHARED_POST]JSON
                        val imagesArray = org.json.JSONArray(post.producto.imagenUrl)
                        val sharedPostData = org.json.JSONObject().apply {
                            put("postId", post.id)
                            put("images", imagesArray)
                            put("title", post.producto.titulo)
                            put("price", post.producto.precio)
                            put("ownerUsername", post.username)
                            put("ownerAvatar", post.userAvatar)
                            put("customMessage", customMessage.trim())
                        }
                        val sharedPostMessage = "[SHARED_POST]${sharedPostData}"
                        ChatRepository.sendMessage(conversationId, sharedPostMessage)
                        // Incrementar shares_count en Supabase
                        viewModel.incrementShareCount(post.id)
                    }
                }
            }
        )
        
        // OPTIMIZADO: Pre-calcular myStoriesData con remember para evitar parsing en UI thread
        val currentUserId = currentUser?.id ?: ""
        val currentUsername = currentUser?.username ?: "Tú"
        val currentUserAvatar = currentUser?.avatarUrl
        val preparedMyStoriesData = remember(myStories, currentUserId, currentUsername, currentUserAvatar) {
            if (myStories.isEmpty()) emptyList()
            else listOf(
                UserStories(
                    userId = currentUserId,
                    username = currentUsername,
                    userAvatar = currentUserAvatar,
                    stories = myStories.map { story ->
                        Story(
                            id = story.id,
                            userId = story.userId,
                            username = currentUsername,
                            userAvatar = currentUserAvatar,
                            imageUrl = story.mediaUrl,
                            timestamp = parseStoryTimestamp(story.createdAt)
                        )
                    }
                )
            )
        }
        
        // Stories Viewer - OPTIMIZADO: Solo leer datos pre-calculados
        LaunchedEffect(showStoriesViewer) {
            if (showStoriesViewer && preparedMyStoriesData.isNotEmpty()) {
                onOpenStoriesViewer(preparedMyStoriesData, currentUserId)
                showStoriesViewer = false
                isLoadingStory = false
            }
        }
        
        // OPTIMIZADO: Pre-calcular userStoriesData con remember para evitar groupBy en UI thread
        // El groupBy ahora solo se ejecuta cuando otherUsersStories cambia, no en cada apertura del viewer
        val preparedUserStoriesData = remember(otherUsersStories) {
            if (otherUsersStories.isEmpty()) emptyList()
            else {
                val storiesByUser = otherUsersStories.groupBy { it.userId }
                storiesByUser.map { (userId, userStories) ->
                    val firstStory = userStories.first()
                    UserStories(
                        userId = userId,
                        username = firstStory.username,
                        userAvatar = firstStory.avatarUrl,
                        stories = userStories.map { storyWithUser ->
                            Story(
                                id = storyWithUser.story.id,
                                userId = storyWithUser.userId,
                                username = storyWithUser.username,
                                userAvatar = storyWithUser.avatarUrl,
                                imageUrl = storyWithUser.story.mediaUrl,
                                timestamp = parseStoryTimestamp(storyWithUser.story.createdAt)
                            )
                        }
                    )
                }
            }
        }
        
        // Stories Viewer para stories de otros usuarios - OPTIMIZADO: Solo leer datos pre-calculados
        LaunchedEffect(showOtherStoriesViewer) {
            if (showOtherStoriesViewer && preparedUserStoriesData.isNotEmpty()) {
                onOpenStoriesViewer(preparedUserStoriesData, currentUser?.id ?: "")
                showOtherStoriesViewer = false
            }
        }
        
        // User Profile Screen - overlay completo
        if (showUserProfile && selectedUserId != null) {
            UserProfileScreen(
                userId = selectedUserId!!,
                onBack = {
                    showUserProfile = false
                    selectedUserId = null
                },
                onPostClick = { post ->
                    selectedPostForProduct = post
                    showProductPage = true
                },
                onOpenChat = { user ->
                    // Cerrar perfil y abrir chat
                    showUserProfile = false
                    selectedUserId = null
                    onOpenChatFromProfile(user)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Story Viewers Modal
        StoryViewersModal(
            isVisible = showStoryViewersModal,
            storyId = selectedStoryForViewers,
            onDismiss = { showStoryViewersModal = false },
            onViewerClick = { viewerId ->
                // Abrir perfil del viewer
                selectedUserId = viewerId
                showUserProfile = true
                showStoryViewersModal = false
            }
        )
        
        // Cart Modal
        CartModal(
            isVisible = showCartModal,
            onDismiss = { showCartModal = false },
            onCheckout = {
                showCartModal = false
            },
            onContinueShopping = {
                showCartModal = false
            },
            onOpenCategories = {
                // Abrir drawer de categorías
                showCategoryDrawer = true
            },
            onOpenExplore = {
                // Abrir SearchResultsScreen directamente (igual que al pulsar una categoría)
                searchResultsQuery = "" // Query vacía muestra todos los productos
                showSearchResults = true
            },
            onVisibilityChange = onCartModalVisibilityChange,
            onNavigateToCheckout = {
                showCartModal = false
                onNavigateToCheckout()
            }
        )
        
        // Report Modal
        ReportModal(
            isVisible = showReportModal,
            contentType = "post",
            contentId = reportPostId,
            username = reportUsername,
            onDismiss = {
                showReportModal = false
                reportPostId = ""
                reportUsername = ""
            },
            onSubmitReport = { reason, description ->
                scope.launch {
                    try {
                        val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                        if (currentUserId != null && reportPostId.isNotEmpty()) {
                            com.rendly.app.data.remote.SupabaseClient.database
                                .from("content_reports")
                                .insert(mapOf(
                                    "reporter_id" to currentUserId,
                                    "content_type" to "post",
                                    "content_id" to reportPostId,
                                    "reason" to reason,
                                    "description" to description
                                ))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error al reportar: ${e.message}")
                    }
                }
            }
        )
        
        // Post QR Code Modal
        PostQrCodeModal(
            isVisible = showQrCodeModal,
            post = qrCodePost,
            onDismiss = {
                showQrCodeModal = false
                qrCodePost = null
            }
        )
        
        // Search Results Screen - overlay completo
        if (showSearchResults) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg)
            ) {
                SearchResultsScreen(
                    initialQuery = searchResultsQuery,
                    onBack = { 
                        showSearchResults = false
                        searchQuery = "" // Limpiar búsqueda del Home
                    },
                    onProductClick = { post ->
                        selectedPostForProduct = post
                        showProductPage = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // RendScreen - overlay completo para reproducir Rends
        if (showRendScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black)
            ) {
                RendScreen(
                    initialRendId = selectedRendId,
                    isScreenVisible = true,
                    showNavBar = false,
                    onNavigateToProfile = { userId ->
                        showRendScreen = false
                        selectedRendId = null
                        selectedUserId = userId
                        showUserProfile = true
                    }
                )
                
                // Botón de cerrar
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .systemBarsPadding()
                        .size(40.dp)
                        .background(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable {
                            showRendScreen = false
                            selectedRendId = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
        
        // Product Page - AL FINAL para que esté por encima de todos los overlays
        val selectedProductId = selectedPostForProduct?.id
        val relatedPosts = remember(posts, selectedProductId) {
            posts.filter { it.id != selectedProductId }.take(8)
        }
        val suggestedPosts = remember(posts, selectedProductId) {
            posts.filter { it.id != selectedProductId }.takeLast(6)
        }
        ProductPage(
            post = selectedPostForProduct,
            isVisible = showProductPage,
            relatedPosts = relatedPosts,
            suggestedPosts = suggestedPosts,
            onDismiss = {
                showProductPage = false
                selectedPostForProduct = null
            },
            onBuyNow = { post ->
                // ProductPage ya agrega al carrito internamente, solo navegar
                onNavigateToCheckout()
            },
            onAddToCart = { post ->
                // ProductPage ya agrega al carrito internamente
                // Este callback es solo para notificaciones/feedback adicional
            },
            onContactSeller = { post ->
                // TODO: Implementar contactar vendedor
            },
            onShare = { post ->
                viewModel.incrementShareCount(post.id)
            },
            onFavorite = { post ->
                // TODO: Implementar favorito
            },
            onViewAllReviews = { post ->
                selectedPostForComments = post
                showCommentsSheet = true
            },
            onRelatedPostClick = { post ->
                selectedPostForProduct = post
            },
            onNavigateToCheckout = onNavigateToCheckout
        )
        
        // Post Options Modal - DESPUÉS del NavBar para que quede SOBRE él
        PostOptionsModal(
            isVisible = showPostOptionsModal,
            post = selectedPostForOptions,
            isOwnPost = isSelectedPostOwn,
            isSaved = selectedPostForOptions?.isSaved ?: false,
            onDismiss = {
                showPostOptionsModal = false
                selectedPostForOptions = null
            },
            onEdit = {
                showPostOptionsModal = false
                // TODO: Abrir EditPostModal
            },
            onDelete = {
                showPostOptionsModal = false
                // TODO: Eliminar post
            },
            onShare = {
                selectedPostForOptions?.let {
                    selectedPostForForward = it
                    showForwardModal = true
                }
                showPostOptionsModal = false
            },
            onSavePost = {
                selectedPostForOptions?.let {
                    viewModel.toggleSave(it.id)
                }
                showPostOptionsModal = false
            },
            onShowQrCode = {
                selectedPostForOptions?.let {
                    qrCodePost = it
                    showQrCodeModal = true
                }
                showPostOptionsModal = false
            },
            onReport = {
                selectedPostForOptions?.let {
                    reportPostId = it.id
                    reportUsername = it.username
                    showReportModal = true
                }
                showPostOptionsModal = false
            },
            onBlock = {
                selectedPostForOptions?.let { post ->
                    scope.launch {
                        try {
                            val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.rendly.app.data.remote.SupabaseClient.database
                                    .from("blocked_users")
                                    .insert(mapOf(
                                        "blocker_id" to currentUserId,
                                        "blocked_id" to post.userId
                                    ))
                                android.widget.Toast.makeText(context, "@${post.username} bloqueado", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error al bloquear: ${e.message}")
                        }
                    }
                }
                showPostOptionsModal = false
            },
            onHide = {
                selectedPostForOptions?.let { post ->
                    // Agregar a posts ocultos localmente
                    hiddenPostIds = hiddenPostIds + post.id
                    postWithHideOptions = post
                    // Guardar en Supabase
                    scope.launch {
                        try {
                            val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.rendly.app.data.remote.SupabaseClient.database
                                    .from("hidden_posts")
                                    .insert(mapOf(
                                        "user_id" to currentUserId,
                                        "post_id" to post.id
                                    ))
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error al ocultar: ${e.message}")
                        }
                    }
                }
                showPostOptionsModal = false
            },
            onUnfollow = {
                selectedPostForOptions?.let { post ->
                    scope.launch {
                        try {
                            val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.rendly.app.data.remote.SupabaseClient.database
                                    .from("followers")
                                    .delete {
                                        filter {
                                            eq("follower_id", currentUserId)
                                            eq("followed_id", post.userId)
                                        }
                                    }
                                android.widget.Toast.makeText(context, "Dejaste de seguir a @${post.username}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error al dejar de seguir: ${e.message}")
                        }
                    }
                }
                showPostOptionsModal = false
            }
        )
    }
}

/**
 * OPTIMIZACIÓN: ThreadLocal cache para SimpleDateFormat
 * SimpleDateFormat NO es thread-safe y crear instancias es costoso (~0.5ms cada una)
 * Con ThreadLocal, cada thread reutiliza su propia instancia
 */
private val dateFormatCache: java.lang.ThreadLocal<java.text.SimpleDateFormat> = 
    java.lang.ThreadLocal.withInitial {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
    }

// Función para formatear el tiempo de las reseñas - OPTIMIZADA con cache
private fun formatCommentTime(createdAt: String): String {
    return try {
        val date = dateFormatCache.get()?.parse(createdAt.substringBefore("+").substringBefore("."))
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

/**
 * OPTIMIZACIÓN: Parser de timestamp para stories usando cache ThreadLocal
 * Evita crear SimpleDateFormat en cada frame durante scroll.
 */
private fun parseStoryTimestamp(createdAt: String): Long {
    return try {
        dateFormatCache.get()?.parse(createdAt.substringBefore("+").substringBefore("."))?.time ?: 0L
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

/**
 * OPTIMIZACIÓN CRÍTICA: Subcomposable estable para PostItem
 * 
 * Elimina jank causado por:
 * 1. Lambdas inestables que capturan objetos mutables
 * 2. Recomposiciones en cascada cuando cambia cualquier estado del padre
 * 
 * Al extraer a un composable separado con parámetros estables,
 * Compose puede skipear recomposiciones cuando el post no cambia.
 */
@Composable
private fun StablePostItem(
    post: Post,
    currentUserId: String?,
    viewModel: HomeViewModel,
    onSelectForComments: (Post) -> Unit,
    onSelectForProduct: (Post) -> Unit,
    onSelectForConsult: (Post) -> Unit,
    onSelectForForward: (Post) -> Unit,
    onSelectUserId: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectForOptions: (Post, Boolean) -> Unit
) {
    // CRÍTICO: Extraer IDs para callbacks estables que no dependan del objeto post
    val postId = post.id
    val postUserId = post.userId
    
    // Callbacks 100% estables - solo capturan primitivos inmutables
    val onLike = remember(postId) { { viewModel.toggleLike(postId) } }
    val onSave = remember(postId) { { viewModel.toggleSave(postId) } }
    val onInfo = remember(postId) { { viewModel.toggleStats(postId) } }
    
    // Para callbacks que necesitan el post, usamos rememberUpdatedState
    // Esto garantiza que siempre tengamos la versión más reciente sin invalidar el remember
    val currentPost by rememberUpdatedState(post)
    
    val onComment = remember(postId) { { onSelectForComments(currentPost) } }
    val onProduct = remember(postId) { { onSelectForProduct(currentPost) } }
    val onConsult = remember(postId) { { onSelectForConsult(currentPost) } }
    val onShare = remember(postId) { { onSelectForForward(currentPost) } }
    
    val onProfile = remember(postId, postUserId) {
        {
            if (postUserId == currentUserId) {
                onNavigateToProfile()
            } else {
                onSelectUserId(postUserId)
            }
        }
    }
    
    val isOwnPost = postUserId == currentUserId
    val onOptions = remember(postId, isOwnPost) { { onSelectForOptions(currentPost, isOwnPost) } }
    
    PostItem(
        post = post,
        onLikeClick = onLike,
        onSaveClick = onSave,
        onCommentClick = onComment,
        onPostClick = onProduct,
        onInfoClick = onInfo,
        onConsultClick = onConsult,
        onProfileClick = onProfile,
        onOptionsClick = onOptions,
        onShareClick = onShare
    )
}

/**
 * OPTIMIZACIÓN: Data class para consolidar estados de visibility en snapshotFlow.
 * Evita múltiples LaunchedEffects separados que compiten por el UI thread.
 */
private data class VisibilityState(
    val userProfile: Boolean,
    val forward: Boolean,
    val comments: Boolean,
    val product: Boolean
)
