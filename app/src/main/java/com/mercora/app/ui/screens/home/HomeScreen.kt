package com.mercora.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mercora.app.ui.components.BottomNavBar
import com.mercora.app.ui.components.CategoryDrawer
import com.mercora.app.ui.components.settings.HelpCenterScreen
import com.mercora.app.ui.components.settings.TermsAndConditionsScreen
import com.mercora.app.ui.components.settings.PrivacyPolicyScreen
import com.mercora.app.ui.components.Comment
import com.mercora.app.ui.components.CommentsSheet
import com.mercora.app.ui.components.ConsultModal
import com.mercora.app.ui.components.ForwardModal
import com.mercora.app.ui.components.HomeHeader
import com.mercora.app.ui.components.MyStoryBanner
import com.mercora.app.ui.components.OptimizedMessagesDrawer
import com.mercora.app.ui.components.OptimizedNotificationsDrawer
import com.mercora.app.ui.components.PostItem
import com.mercora.app.ui.components.PostOptionsModal
import com.mercora.app.ui.components.SearchBar
import com.mercora.app.ui.components.StoriesViewer
import com.mercora.app.ui.components.Story
import com.mercora.app.ui.components.StoriesCarousel
import com.mercora.app.ui.components.StoryViewersModal
import com.mercora.app.ui.components.UploadProgressBanner
import com.mercora.app.ui.components.UserStories
import com.mercora.app.ui.components.CartModal
import com.mercora.app.ui.components.LocalOpenProductPreview
import com.mercora.app.ui.components.ProductPreviewConfig
import com.mercora.app.ui.components.RendsCarousel
import com.mercora.app.ui.components.ReportModal
import com.mercora.app.ui.components.HiddenPostPlaceholder
import com.mercora.app.ui.components.PostQrCodeModal
import com.mercora.app.ui.components.FeaturedPostsSection
import com.mercora.app.ui.components.VideoPostItem
import com.mercora.app.ui.components.SuggestedAccountsCarousel
import com.mercora.app.ui.components.HomeFeedSkeleton
// RendViewerScreen removed - navigation goes to videos section directly
import com.mercora.app.data.repository.CartRepository
import com.mercora.app.util.FCMHelper
import com.mercora.app.data.repository.ChatRepository
import com.mercora.app.data.repository.ExploreRepository
import com.mercora.app.ui.screens.profile.UserProfileScreen
import com.mercora.app.ui.screens.search.SearchResultsScreen
// RendScreen removed from HomeScreen - rendered in MainScreen's videos route
import com.mercora.app.data.model.Post
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.model.StoryUploadState
import com.mercora.app.data.repository.CommentRepository
import com.mercora.app.data.repository.NotificationRepository
import com.mercora.app.data.repository.PostRepository
import com.mercora.app.data.repository.ProfileRepository
import com.mercora.app.data.repository.StoryRepository
import com.mercora.app.data.repository.ViewTracker
import com.mercora.app.data.cache.BadgeCountCache
import com.mercora.app.data.repository.AppUpdateRepository
import com.mercora.app.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
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
    homeListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onMessagesClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onStoriesViewerVisibilityChange: (Boolean) -> Unit = {},
    onUserProfileVisibilityChange: (Boolean) -> Unit = {},
    onForwardModalVisibilityChange: (Boolean) -> Unit = {},
    onCommentsSheetVisibilityChange: (Boolean) -> Unit = {},
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
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // UI STATE COMBINADO: 1 solo collectAsState en lugar de 6+ separados
    // Reduce overhead de subscriptions por frame durante scroll
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    val feedState by viewModel.feedState.collectAsState()
    val posts = feedState.posts
    val isLoading = feedState.isLoading
    val isLoadingMore = feedState.isLoadingMore
    val hasMorePosts = feedState.hasMorePosts
    val currentUser = feedState.currentUser
    val errorMessage = feedState.errorMessage
    val listState = homeListState
    
    // Avatar desde ProfileRepository (mÃ¡s confiable)
    val profileFromRepo by ProfileRepository.currentProfile.collectAsState()
    val userAvatarUrl = profileFromRepo?.avatarUrl ?: currentUser?.avatarUrl
    
    // Asegurar que el perfil se carga si no existe para que se vean los avatares
    LaunchedEffect(currentUser) {
        if (profileFromRepo == null) {
            ProfileRepository.loadCurrentProfile()
        }
    }
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // COLD START OPTIMIZATION V2: Carga ultra-diferida post-first-frame
    // 
    // Estrategia: Mostrar UI instantÃ¡neamente, cargar datos en 2 fases
    // FASE 1: Datos crÃ­ticos para primer scroll (posts) - ya manejado por ViewModel
    // FASE 2: Datos secundarios diferidos 300ms (stories, rends, notificaciones)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    val initScope = rememberCoroutineScope()
    
    // Variable para controlar si ya se ejecutÃ³ la inicializaciÃ³n diferida
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
        com.vinzay.app.data.repository.AvatarShapeRepository.init(context)
    }
    
    LaunchedEffect(Unit) {
        // Diferir carga de datos secundarios 300ms post-first-frame
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
                com.vinzay.app.data.repository.RendRepository.loadRends() 
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
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // VIDEO GATING (patrÃ³n Instagram): solo el video con >50% visible en
    // viewport reproduce. derivedStateOf devuelve la KEY del item -> solo
    // recompone cuando cambia el video elegido, no en cada frame de scroll.
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    val mostVisibleVideoKey by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportStart = layoutInfo.viewportStartOffset
            val viewportEnd = layoutInfo.viewportEndOffset
            layoutInfo.visibleItemsInfo
                .filter { (it.key as? String)?.startsWith("feed_rend_slot_") == true }
                .maxByOrNull { item ->
                    val visibleTop = maxOf(item.offset, viewportStart)
                    val visibleBottom = minOf(item.offset + item.size, viewportEnd)
                    (visibleBottom - visibleTop).coerceAtLeast(0)
                }
                ?.takeIf { item ->
                    // Requiere >50% del item visible para autoplay
                    val visibleTop = maxOf(item.offset, viewportStart)
                    val visibleBottom = minOf(item.offset + item.size, viewportEnd)
                    (visibleBottom - visibleTop) > item.size / 2
                }
                ?.key as? String
        }
    }

    // VIEW TRACKING: Registrar vistas de posts visibles en pantalla
    // distinctUntilChanged: solo emite cuando cambia el SET de keys visibles,
    // no en cada frame de scroll (visibleItemsInfo cambia por offsets)
    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.map { it.key } }
            .distinctUntilChanged()
            .collect { visibleKeys ->
                for (key in visibleKeys) {
                    if (key is String && key != "header" && key != "search" && key != "story_banner"
                        && key != "stories_carousel" && key != "rends_carousel"
                        && key != "featured_posts" && key != "refresh_spinner"
                        && key != "upload_banner" && key != "loading_more") {
                        ViewTracker.trackPostView(key)
                    }
                }
            }
    }
    
    // PREFETCH DE AVATARES: Cuando cambian los posts visibles, encolar carga de avatares
    // de los posts que estÃ¡n cerca pero aÃºn no visibles. Reduce perceived loading a ~0ms.
    LaunchedEffect(posts.size) {
        if (posts.isNotEmpty()) {
            val imageLoader = com.vinzay.app.MercoraApplication.getImageLoader(context)
            val visibleKeys = listState.layoutInfo.visibleItemsInfo.map { it.key }.toSet()
            // Prefetch avatares de posts que estÃ¡n en el feed pero no visibles aÃºn (max 10)
            posts.take(15).forEach { post ->
                val avatarUrl = if (post.userAvatar.startsWith("http")) post.userAvatar
                    else "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${post.userAvatar}"
                if (avatarUrl.isNotBlank()) {
                    val request = coil.request.ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .size(96)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build()
                    imageLoader.enqueue(request)
                }
            }
        }
    }
    
    // Prefetch inteligente basado en posiciÃ³n de scroll
    // Optimizado: prefetch un poco antes (5 \u00edtems del final) para que los datos lleguen sin lag
    val shouldPrefetchMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            // Prefetch cuando estamos a 5 items del final
            lastVisibleItem >= totalItems - 5 && hasMorePosts && !isLoadingMore && posts.isNotEmpty()
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
    
    // Product preview â€” delegated to centralized overlay via CompositionLocal
    val openPreview = LocalOpenProductPreview.current
    
    val scope = rememberCoroutineScope()
    
    val openProduct: (Post) -> Unit = { post ->
        openPreview(ProductPreviewConfig(
            post = post,
            onContactSeller = { p ->
                scope.launch {
                    try {
                        val conversationId = ChatRepository.getOrCreateConversation(p.userId)
                        if (conversationId != null) {
                            val users = com.vinzay.app.data.cache.network.SupabaseDataSource.fetchUsers(listOf(p.userId))
                            val seller = users.firstOrNull()
                            if (seller != null) {
                                onOpenChatFromProfile(seller)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error contactando vendedor: ${e.message}")
                    }
                }
            }
        ))
    }
    
    // Cart Modal state
    var showCartModal by remember { mutableStateOf(false) }
    
    // UserProfile state
    var showUserProfile by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    
    // Rend navigation - navigate to videos section with specific rend ID
    // (handled via onNavNavigate callback to MainScreen)
    
    // WelcomeOverlay state â€” ocultar contenido del Home mientras la animaciÃ³n de bienvenida se reproduce
    val welcomeData by com.vinzay.app.data.model.WelcomeState.welcome.collectAsState()
    
    // PostOptionsModal state
    var showPostOptionsModal by remember { mutableStateOf(false) }
    var selectedPostForOptions by remember { mutableStateOf<Post?>(null) }
    var isSelectedPostOwn by remember { mutableStateOf(false) }
    
    // EditPostModal state
    var showEditPostModal by remember { mutableStateOf(false) }
    var editingPost by remember { mutableStateOf<Post?>(null) }
    
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
    
    // Posts que ya procesaron su acciÃ³n y deben desaparecer completamente
    var fullyHiddenPostIds by remember { mutableStateOf(setOf<String>()) }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // CALLBACKS DE MODERACIÃ“N: creados UNA vez para todo el feed.
    // Antes cada item hidden inflaba lambdas gigantes con queries Supabase
    // inline, invalidando el items block en cada recomposiciÃ³n.
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    val onHiddenReport: (Post) -> Unit = remember {
        { post ->
            reportPostId = post.id
            reportUsername = post.username
            showReportModal = true
            fullyHiddenPostIds = fullyHiddenPostIds + post.id
            postWithHideOptions = null
        }
    }
    val onHiddenMute: (Post) -> Unit = remember {
        { post ->
            viewModel.muteUser(post.userId, post.username)
            fullyHiddenPostIds = fullyHiddenPostIds + post.id
            postWithHideOptions = null
        }
    }
    val onHiddenUnfollow: (Post) -> Unit = remember {
        { post ->
            viewModel.unfollowUser(post.userId, post.username)
            fullyHiddenPostIds = fullyHiddenPostIds + post.id
            postWithHideOptions = null
        }
    }
    val onHiddenCancel: (Post) -> Unit = remember {
        { post ->
            hiddenPostIds = hiddenPostIds - post.id
            viewModel.unhidePost(post.id)
            postWithHideOptions = null
        }
    }
    
    // OPTIMIZADO: Consolidar notificaciones de visibility en un solo snapshotFlow
    // Reduce de 4 LaunchedEffects a 1, eliminando overhead de coroutines
    LaunchedEffect(Unit) {
        snapshotFlow { 
            VisibilityState(showUserProfile, showForwardModal, showCommentsSheet) 
        }.collect { state ->
            onUserProfileVisibilityChange(state.userProfile)
            onForwardModalVisibilityChange(state.forward)
            onCommentsSheetVisibilityChange(state.comments)
        }
    }
    
    // Stories state
    val storyUploadState by StoryRepository.uploadState.collectAsState()
    val myStories by StoryRepository.myStories.collectAsState()
    val otherUsersStories by StoryRepository.otherUsersStories.collectAsState()
    val viewedStoryIds by StoryRepository.viewedStoryIds.collectAsState()
    
    // OPTIMIZADO: Rends state en nivel superior (evita collectAsState dentro del LazyColumn item)
    val rendsData by com.vinzay.app.data.repository.RendRepository.rends.collectAsState()
    val rendsLoading by com.vinzay.app.data.repository.RendRepository.isLoading.collectAsState()
    var showStoriesViewer by remember { mutableStateOf(false) }
    var showOtherStoriesViewer by remember { mutableStateOf(false) }
    var selectedStoryUserIndex by remember { mutableIntStateOf(0) }
    var isLoadingStory by remember { mutableStateOf(false) }
    var showStoryViewersModal by remember { mutableStateOf(false) }
    var selectedStoryForViewers by remember { mutableStateOf("") }
    val storyScope = rememberCoroutineScope()
    
    // OPTIMIZADO: Memoizar cÃ¡lculo de vistas totales
    val myStoriesViewsCount = remember(myStories) { myStories.sumOf { it.views } }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // FEED PRE-COMPUTADO EN VIEWMODEL (Dispatchers.Default):
    // distinctBy/drop/interleaving ya vienen resueltos - la composiciÃ³n
    // no recalcula NADA al emitirse la lista (p.ej. en un like)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    val feedUi by viewModel.feedUi.collectAsState()
    val firstThreePosts = feedUi.firstThreePosts
    val remainingPosts = feedUi.remainingPosts
    val stableFeed = feedUi.entries
    
    // Post upload state
    val postUploadState by PostRepository.uploadState.collectAsState()
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
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
            // Esperar 2 segundos despuÃ©s de que el banner desaparezca
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

    var updateInfo by remember { mutableStateOf<AppUpdateRepository.UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val info = AppUpdateRepository.checkForUpdate()
        if (info != null && info.hasUpdate) {
            updateInfo = info
            showUpdateDialog = true
        }
    }

    val updateInfoValue = updateInfo

    if (showUpdateDialog && updateInfoValue != null) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            containerColor = Color(0xFF1A1A2E),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0B0B0),
            icon = {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF2E8B57))
            },
            title = {
                Text("Actualizacion disponible", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Vinzay v${updateInfoValue.latest.version_name} disponible")
                    if (!updateInfoValue.latest.changelog.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Cambios:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(updateInfoValue.latest.changelog, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Version actual: ${updateInfoValue.currentVersion}", fontSize = 12.sp, color = Color(0xFF808080))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showUpdateDialog = false
                    AppUpdateRepository.downloadAndInstall(context, updateInfoValue.latest)
                }) {
                    Text("Descargar", color = Color(0xFF2E8B57))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Ahora no", color = Color(0xFF808080))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            .systemBarsPadding()
    ) {
        // Premium refresh spinner overlay (hidden until refresh)
        // Handled inside LazyColumn for smooth animation
        
        // Sin spinner de carga â€” el overlay HomeBg cubre hasta que posts estÃ©n listos
        // Scroll con fÃ­sica personalizada: exponencial decay + pre-composiciÃ³n de items fuera de vista
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 72.dp),
            userScrollEnabled = true,
        ) {
                // Header como parte del scroll
                item(key = "header", contentType = "header") {
                    // OPTIMIZADO: Callbacks memoizados para HomeHeader
                    val onMenuClickCallback = remember { { showCategoryDrawer = true } }
                    val onCartClickCallback = remember { { showCartModal = true } }
                    HomeHeader(
                        onMenuClick = onMenuClickCallback,
                        onNotificationsClick = onNotificationsClick,
                        onMessagesClick = onMessagesClick,
                        onCartClick = onCartClickCallback
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
                
                // Banner de progreso de publicaciÃ³n (arriba del search)
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
                    // OPTIMIZADO: Calcular estadÃ­sticas con derivedStateOf para evitar recÃ¡lculos
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
                        // OPTIMIZADO: Callbacks memoizados para evitar recomposiciÃ³n
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
                        // contentType PURO: no lee estado (leer hiddenPostIds aquÃ­
                        // invalidaba el item provider completo al ocultar un post)
                        contentType = { "post" }
                    ) { post ->
                        if (post.id in fullyHiddenPostIds) {
                            return@items
                        }
                        if (post.id in hiddenPostIds) {
                            HiddenPostPlaceholder(
                                username = post.username,
                                onReport = { onHiddenReport(post) },
                                onMuteUser = { onHiddenMute(post) },
                                onUnfollow = { onHiddenUnfollow(post) },
                                onCancel = { onHiddenCancel(post) }
                            )
                        } else {
                            StablePostItem(
                                post = post,
                                currentUserId = currentUser?.id,
                                viewModel = viewModel,
                                onSelectForComments = { selectedPostForComments = it; showCommentsSheet = true },
                                onSelectForProduct = { openProduct(it) },
                                onSelectForConsult = { selectedPostForConsult = it; showConsultModal = true },
                                onSelectForForward = { selectedPostForForward = it; showForwardModal = true },
                                onSelectUserId = { selectedUserId = it; showUserProfile = true },
                                onNavigateToProfile = onNavigateToProfile,
                                onSelectForOptions = { p, isOwn -> selectedPostForOptions = p; isSelectedPostOwn = isOwn; showPostOptionsModal = true }
                            )
                        }
                    }
                    
                    // Carrusel de Rends - aparece despuÃ©s de 3 posts
                    item(key = "rends_carousel", contentType = "rends_carousel") {
                        // Callbacks para navegar a la secciÃ³n de videos con el rend pulsado
                        val onRendClickCallback = remember { 
                            { rend: com.vinzay.app.data.model.Rend -> 
                                com.vinzay.app.data.repository.RendRepository.setPendingRendId(rend.id)
                                onNavNavigate("videos")
                            } 
                        }
                        val onViewAllCallback = remember { 
                            { 
                                onNavNavigate("videos")
                            } 
                        }
                        RendsCarousel(
                            rends = rendsData,
                            isLoading = rendsLoading,
                            onRendClick = onRendClickCallback,
                            onViewAll = onViewAllCallback
                        )
                    }
                    
                    // Suggested Accounts Carousel - "Personas que quizÃ¡s conozcas"
                    item(key = "suggested_accounts", contentType = "suggested_accounts") {
                        SuggestedAccountsCarousel(
                            onProfileClick = { userId ->
                                selectedUserId = userId
                                showUserProfile = true
                            }
                        )
                    }
                    
                    // Featured Posts Section - 6 publicaciones destacadas (despuÃ©s de 3 posts y rends)
                    if (posts.size >= 6) {
                        item(key = "featured_posts", contentType = "featured_posts") {
                            FeaturedPostsSection(
                                posts = posts.take(6), // Mostrar siempre 6 tarjetas
                                onPostClick = { post ->
                                    openProduct(post)
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
                
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // FEED ESTABLE: Usa lista pre-computada (no recomputa en scroll)
                // Previene crashes por items shifting durante recomposiciÃ³n
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                items(
                    items = stableFeed,
                    key = { it.id },
                    // contentType PURO: no lee estado (leer hiddenPostIds aquÃ­
                    // invalidaba el item provider completo al ocultar un post)
                    contentType = { entry ->
                        if (entry.type == "rend") "video_rend" else "post"
                    }
                ) { entry ->
                    if (entry.type == "rend") {
                        // Render Rend video - slot siempre existe, contenido se llena cuando rendsData carga
                        val rend = rendsData.getOrNull(entry.rendIndex)
                        if (rend != null) {
                            StableVideoPostItem(
                                rend = rend,
                                currentUserId = currentUser?.id,
                                onSelectUserId = { selectedUserId = it; showUserProfile = true },
                                onNavigateToProfile = onNavigateToProfile,
                                onRendClick = {
                                    com.vinzay.app.data.repository.RendRepository.setPendingRendId(rend.id)
                                    onNavNavigate("videos")
                                },
                                onSelectForConsult = { selectedPostForConsult = it; showConsultModal = true },
                                onSelectForComments = { selectedPostForComments = it; showCommentsSheet = true },
                                onSelectForForward = { selectedPostForForward = it; showForwardModal = true },
                                onSelectForOptions = { p, isOwn -> selectedPostForOptions = p; isSelectedPostOwn = isOwn; showPostOptionsModal = true },
                                isVisible = entry.id == mostVisibleVideoKey
                            )
                        } else {
                            // Slot pre-allocado pero rend aÃºn no cargado - no renderizar nada
                            return@items
                        }
                    } else {
                        // Render Post
                        val post = remainingPosts.getOrNull(entry.postIndex) ?: return@items
                        
                        if (post.id in fullyHiddenPostIds) {
                            return@items
                        }
                        if (post.id in hiddenPostIds) {
                            HiddenPostPlaceholder(
                                username = post.username,
                                onReport = { onHiddenReport(post) },
                                onMuteUser = { onHiddenMute(post) },
                                onUnfollow = { onHiddenUnfollow(post) },
                                onCancel = { onHiddenCancel(post) }
                            )
                        } else {
                            StablePostItem(
                                post = post,
                                currentUserId = currentUser?.id,
                                viewModel = viewModel,
                                onSelectForComments = { selectedPostForComments = it; showCommentsSheet = true },
                                onSelectForProduct = { openProduct(it) },
                                onSelectForConsult = { selectedPostForConsult = it; showConsultModal = true },
                                onSelectForForward = { selectedPostForForward = it; showForwardModal = true },
                                onSelectUserId = { selectedUserId = it; showUserProfile = true },
                                onNavigateToProfile = onNavigateToProfile,
                                onSelectForOptions = { p, isOwn -> selectedPostForOptions = p; isSelectedPostOwn = isOwn; showPostOptionsModal = true }
                            )
                        }
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
                                        text = "Cargando mÃ¡s...",
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
                                text = "No hay mÃ¡s publicaciones",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
        }
        
        // ReseÃ±as Sheet con reseÃ±as reales
        val commentsFromRepo by CommentRepository.comments.collectAsState()
        val isCommentsLoading by CommentRepository.isLoading.collectAsState()
        
        // Cargar reseÃ±as cuando se selecciona un post
        // SIEMPRE usar product_reviews
        LaunchedEffect(selectedPostForComments) {
            selectedPostForComments?.let { post ->
                val productId = post.productId ?: CommentRepository.getProductIdFromPostId(post.id)
                if (productId != null) {
                    CommentRepository.loadProductReviews(productId)
                }
            }
        }
        
        // OPTIMIZADO: Memoizar transformaciÃ³n de comentarios
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
        
        // NavBar embebido - ANTES de todos los modales para que queden SOBRE Ã©l
        if (showNavBar) {
                BottomNavBar(
                    currentRoute = currentNavRoute,
                    onNavigate = onNavNavigate,
                    onHomeReclick = onNavHomeReclick,
                    userAvatarUrl = userAvatarUrl, // Pasar el avatar calculado
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
        }
        
        // Category Drawer - DESPUÃ‰S del NavBar para overlay correcto (queda SOBRE la navbar)
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
                                
                                // Notificar al dueÃ±o del post
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
            currentUsername = currentUser?.username ?: "TÃº",
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
                            // Obtener o crear conversaciÃ³n con el vendedor del producto
                            val conversationId = ChatRepository.getOrCreateConversation(post.userId)
                            if (conversationId != null) {
                                // Formato especial para consultas: [CONSULT_POST]JSON
                                val consultImagesArray = org.json.JSONArray(post.producto.imagenUrl)
                                val consultData = org.json.JSONObject().apply {
                                    put("postId", post.id)
                                    put("productTitle", post.producto.titulo)
                                    put("productPrice", post.producto.precio)
                                    put("productImage", post.producto.imagenUrl.firstOrNull() ?: "")
                                    put("images", consultImagesArray)
                                    put("ownerUsername", post.username)
                                    put("ownerAvatar", post.userAvatar)
                                    put("isOwnerVerified", post.isUserVerified)
                                    put("message", consultMessage)
                                    put("type", if (consultMessage.contains("ðŸ’° OFERTA")) "offer" else "inquiry")
                                }
                                val consultPostMessage = "[CONSULT_POST]${consultData}"
                                
                                // Enviar mensaje en tiempo real
                                val success = ChatRepository.sendMessage(conversationId, consultPostMessage)
                                if (success) {
                                    android.widget.Toast.makeText(
                                        context, 
                                        if (consultMessage.contains("ðŸ’° OFERTA")) "Â¡Oferta enviada!" else "Â¡Consulta enviada!",
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
                            put("isOwnerVerified", post.isUserVerified)
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
        val currentUsername = currentUser?.username ?: "TÃº"
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
                            timestamp = parseStoryTimestamp(story.createdAt),
                            views = story.views
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
                                timestamp = parseStoryTimestamp(storyWithUser.story.createdAt),
                                views = storyWithUser.story.views
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
                    openProduct(post)
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
                // Abrir drawer de categorÃ­as
                showCategoryDrawer = true
            },
            onOpenExplore = {
                // Abrir SearchResultsScreen directamente (igual que al pulsar una categorÃ­a)
                searchResultsQuery = "" // Query vacÃ­a muestra todos los productos
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
                        val currentUserId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                        if (currentUserId != null && reportPostId.isNotEmpty()) {
                            com.vinzay.app.data.remote.SupabaseClient.database
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
                        searchQuery = "" // Limpiar bÃºsqueda del Home
                    },
                    onProductClick = { post ->
                        openProduct(post)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Post Options Modal - DESPUÃ‰S del NavBar para que quede SOBRE Ã©l
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
                selectedPostForOptions?.let { post ->
                    editingPost = post
                    showEditPostModal = true
                }
                showPostOptionsModal = false
            },
            onDelete = {
                selectedPostForOptions?.let { post ->
                    scope.launch {
                        try {
                            com.vinzay.app.data.remote.SupabaseClient.database
                                .from("posts")
                                .delete {
                                    filter { eq("id", post.id) }
                                }
                            viewModel.removePost(post.id)
                            android.widget.Toast.makeText(context, "PublicaciÃ³n eliminada", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error eliminando post: ${e.message}")
                            android.widget.Toast.makeText(context, "Error al eliminar", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showPostOptionsModal = false
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
                            val currentUserId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.vinzay.app.data.remote.SupabaseClient.database
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
                            val currentUserId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.vinzay.app.data.remote.SupabaseClient.database
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
                            val currentUserId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.vinzay.app.data.remote.SupabaseClient.database
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
        
        // EditPostModal
        com.vinzay.app.ui.components.EditPostModal(
            isVisible = showEditPostModal,
            post = editingPost,
            onDismiss = {
                showEditPostModal = false
                editingPost = null
            },
            onSave = { editData ->
                editingPost?.let { post ->
                    scope.launch {
                        try {
                            com.vinzay.app.data.remote.SupabaseClient.database
                                .from("posts")
                                .update({
                                    set("title", editData.title)
                                    set("description", editData.description)
                                    set("price", editData.price)
                                    set("previous_price", editData.originalPrice)
                                    set("category", editData.category)
                                    set("condition", editData.condition)
                                }) {
                                    filter { eq("id", post.id) }
                                }
                            viewModel.refreshPosts()
                            android.widget.Toast.makeText(context, "PublicaciÃ³n actualizada", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error actualizando post: ${e.message}")
                            android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showEditPostModal = false
                editingPost = null
            },
            onDelete = {
                editingPost?.let { post ->
                    scope.launch {
                        try {
                            com.vinzay.app.data.remote.SupabaseClient.database
                                .from("posts")
                                .delete {
                                    filter { eq("id", post.id) }
                                }
                            viewModel.removePost(post.id)
                            android.widget.Toast.makeText(context, "PublicaciÃ³n eliminada", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            android.util.Log.e("HomeScreen", "Error eliminando post: ${e.message}")
                        }
                    }
                }
                showEditPostModal = false
                editingPost = null
            }
        )
        
        // OVERLAY: Cubre el Home mientras carga â€” HomeFeedSkeleton + loading hasta que posts lleguen
        // El usuario ve un skeleton profesional en lugar de pantalla vacÃ­a â†’ percepciÃ³n de velocidad
        if (welcomeData.show || (isLoading && posts.isEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg)
            ) {
                if (!welcomeData.show) {
                    HomeFeedSkeleton()
                }
            }
        }
    }
}

/**
 * OPTIMIZACIÃ“N: ThreadLocal cache para SimpleDateFormat
 * SimpleDateFormat NO es thread-safe y crear instancias es costoso (~0.5ms cada una)
 * Con ThreadLocal, cada thread reutiliza su propia instancia
 */
private val dateFormatCache: java.lang.ThreadLocal<java.text.SimpleDateFormat> = 
    java.lang.ThreadLocal.withInitial {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
    }

// FunciÃ³n para formatear el tiempo de las reseÃ±as - OPTIMIZADA con cache
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
 * OPTIMIZACIÃ“N: Parser de timestamp para stories usando cache ThreadLocal
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
 * OPTIMIZACIÃ“N CRÃTICA: Subcomposable estable para PostItem
 * 
 * Elimina jank causado por:
 * 1. Lambdas inestables que capturan objetos mutables
 * 2. Recomposiciones en cascada cuando cambia cualquier estado del padre
 * 
 * Al extraer a un composable separado con parÃ¡metros estables,
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
    // CRÃTICO: Extraer IDs para callbacks estables que no dependan del objeto post
    val postId = post.id
    val postUserId = post.userId
    
    // Callbacks 100% estables - solo capturan primitivos inmutables
    val onLike = remember(postId) { { viewModel.toggleLike(postId) } }
    val onSave = remember(postId) { { viewModel.toggleSave(postId) } }
    val onInfo = remember(postId) { { viewModel.toggleStats(postId) } }
    
    // Para callbacks que necesitan el post, usamos rememberUpdatedState
    // Esto garantiza que siempre tengamos la versiÃ³n mÃ¡s reciente sin invalidar el remember
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
 * Stable wrapper for VideoPostItem (Rend) - same pattern as StablePostItem
 */
@Composable
private fun StableVideoPostItem(
    rend: com.vinzay.app.data.model.Rend,
    currentUserId: String?,
    onSelectUserId: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onRendClick: () -> Unit,
    onSelectForConsult: (Post) -> Unit = {},
    onSelectForComments: (Post) -> Unit = {},
    onSelectForForward: (Post) -> Unit = {},
    onSelectForOptions: (Post, Boolean) -> Unit = { _, _ -> },
    isVisible: Boolean = false
) {
    val rendId = rend.id
    val rendUserId = rend.userId
    
    val currentRend by rememberUpdatedState(rend)
    
    // Convert Rend to Post for reuse with existing modals
    val rendAsPost = remember(rendId) {
        Post(
            id = rendId,
            userId = rend.userId,
            username = rend.username,
            userAvatar = rend.userAvatar,
            userStoreName = rend.userStoreName,
            title = rend.productTitle ?: rend.title,
            description = rend.description,
            images = listOfNotNull(rend.productImage, rend.thumbnailUrl).ifEmpty { listOf("") },
            price = rend.productPrice ?: 0.0,
            condition = "Nuevo",
            category = "",
            likesCount = rend.likesCount,
            reviewsCount = rend.reviewsCount,
            createdAt = rend.createdAt,
            isLiked = rend.isLiked,
            isSaved = rend.isSaved,
            freeShipping = false,
            productId = rend.productId
        )
    }
    
    // Check if user is verified (cacheado: no re-consulta Supabase al reciclar el item)
    var isUserVerified by remember { mutableStateOf(false) }
    LaunchedEffect(rend.userId) {
        isUserVerified = com.vinzay.app.data.repository.VerificationRepository.isUserVerifiedCached(rend.userId)
    }
    
    val rendScope = rememberCoroutineScope()
    val onLike = remember(rendId) {
        {
            rendScope.launch {
                com.vinzay.app.data.repository.RendRepository.toggleLike(
                    rendId,
                    currentRend.likesCount,
                    currentRend.isLiked
                )
            }
            Unit
        }
    }
    val onSave = remember(rendId) {
        {
            rendScope.launch {
                com.vinzay.app.data.repository.RendRepository.toggleSave(
                    rendId,
                    currentRend.savesCount,
                    currentRend.isSaved
                )
            }
            Unit
        }
    }
    
    val onProfile = remember(rendId, rendUserId) {
        {
            if (rendUserId == currentUserId) {
                onNavigateToProfile()
            } else {
                onSelectUserId(rendUserId)
            }
        }
    }
    
    VideoPostItem(
        rend = rend,
        onLikeClick = onLike,
        onSaveClick = onSave,
        onRendClick = onRendClick,
        onProfileClick = onProfile,
        onShareClick = { onSelectForForward(rendAsPost) },
        onConsultClick = { onSelectForConsult(rendAsPost) },
        onCommentClick = { onSelectForComments(rendAsPost) },
        onOptionsClick = { onSelectForOptions(rendAsPost, rendUserId == currentUserId) },
        isUserVerified = isUserVerified,
        isVisible = isVisible
    )
}

/**
 * OPTIMIZACIÃ“N: Data class para consolidar estados de visibility en snapshotFlow.
 * Evita mÃºltiples LaunchedEffects separados que compiten por el UI thread.
 */
private data class VisibilityState(
    val userProfile: Boolean,
    val forward: Boolean,
    val comments: Boolean
)
