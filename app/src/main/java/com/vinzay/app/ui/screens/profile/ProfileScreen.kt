package com.vinzay.app.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vinzay.app.data.model.Highlight
import com.vinzay.app.data.repository.FollowersRepository
import com.vinzay.app.data.repository.NotificationRepository
import com.vinzay.app.data.repository.ProfileRepository
import com.vinzay.app.data.repository.HighlightRepository
import com.vinzay.app.ui.components.HighlightedStories
import com.vinzay.app.ui.components.HighlightedStory
import com.vinzay.app.ui.components.HighlightCategory
import com.vinzay.app.ui.components.HighlightFrameStyle
import com.vinzay.app.ui.components.HighlightFrameColor
import com.vinzay.app.ui.components.HighlightBackgroundColor
import com.vinzay.app.ui.components.ProfileStatsBadges
import com.vinzay.app.ui.components.SellerTrustIndicator
import com.vinzay.app.ui.components.AddHighlightModal
import com.vinzay.app.ui.components.StoriesViewer
import com.vinzay.app.ui.components.Story
import com.vinzay.app.ui.components.UserStories
import com.vinzay.app.ui.components.HighlightOptionsModal
import com.vinzay.app.ui.components.PublishOptionsModal
import com.vinzay.app.ui.components.LocalOpenProductPreview
import com.vinzay.app.ui.components.ProductPreviewConfig
import com.vinzay.app.ui.components.ProfileSettingsModal
import com.vinzay.app.ui.components.BottomNavBar
import com.vinzay.app.ui.components.EditPostModal
import com.vinzay.app.ui.screens.publish.PublishScreen
import androidx.compose.foundation.combinedClickable
import com.vinzay.app.data.model.Post
import com.vinzay.app.data.model.Rend
import com.vinzay.app.data.repository.PostRepository
import com.vinzay.app.data.repository.RendRepository
import com.vinzay.app.data.repository.StoryRepository
import com.vinzay.app.ui.components.FollowersListSheet
import com.vinzay.app.ui.components.ProfileScreenSkeleton
import com.vinzay.app.ui.components.ProfileGridSkeleton
import com.vinzay.app.ui.theme.*
import android.util.Log
import com.vinzay.app.data.remote.SupabaseClient
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke

data class ProfileData(
    val userId: String = "",
    val username: String = "",
    val nombre: String? = null,
    val nombreTienda: String? = null,
    val descripcion: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val ubicacion: String? = null,
    val telefono: String? = null,
    val sexo: String? = null,
    val publicaciones: Int = 0,
    val seguidores: Int = 0,
    val clientes: Int = 0,
    val reputacion: Int = 0,
    val tieneTienda: Boolean = false,
    val miembroDesde: String = "2024",
    val isVerified: Boolean = false,
    val avatarShape: String? = null
)

private data class ProfileTab(
    val id: String,
    val icon: ImageVector,
    val label: String
)

private val profileTabs = listOf(
    ProfileTab("posts", Icons.Default.Menu, "Catálogo"),
    ProfileTab("videos", Icons.Outlined.PlayCircle, "Clips"),
    ProfileTab("details", Icons.Outlined.Info, "Detalles"),
    ProfileTab("points", Icons.Outlined.Star, "Puntos"),
    ProfileTab("saved", Icons.Outlined.BookmarkBorder, "Guardados")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit = {},
    onStoriesViewerVisibilityChange: (Boolean) -> Unit = {},
    onSettingsModalVisibilityChange: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    modifier: Modifier = Modifier,
    // NavBar embebido
    showNavBar: Boolean = true,
    currentNavRoute: String = "profile",
    onNavNavigate: (String) -> Unit = {},
    onNavHomeReclick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Estado del modal de agregar highlight
    var showAddHighlightModal by remember { mutableStateOf(false) }
    var isCreatingHighlight by remember { mutableStateOf(false) }
    
    // Estado para StoriesViewer de highlights
    var showHighlightViewer by remember { mutableStateOf(false) }
    var selectedHighlight by remember { mutableStateOf<HighlightedStory?>(null) }
    var highlightRefreshKey by remember { mutableIntStateOf(0) }
    
    // Estado para modal de opciones de highlight (long press)
    var showHighlightOptionsModal by remember { mutableStateOf(false) }
    var highlightForOptions by remember { mutableStateOf<HighlightedStory?>(null) }
    var isDeletingHighlight by remember { mutableStateOf(false) }
    var uploadingHighlightId by remember { mutableStateOf<String?>(null) }
    
    // Estado para Rendshop
    var showRendshop by remember { mutableStateOf(false) }
    
    // Estado para visor de avatar a pantalla completa
    var showAvatarViewer by remember { mutableStateOf(false) }
    
    // Inicializar repo de formas de avatar
    LaunchedEffect(Unit) {
        com.vinzay.app.data.repository.AvatarShapeRepository.init(context)
    }
    
    // Estado para modal de ajustes
    var showSettingsModal by remember { mutableStateOf(false) }
    var showPrivacySettings by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }
    var showHelpCenter by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    
    // Product preview — delegated to centralized overlay
    val openPreview = LocalOpenProductPreview.current
    val openProduct: (Post) -> Unit = { post ->
        openPreview(ProductPreviewConfig(
            post = post,
            onContactSeller = {
                android.widget.Toast.makeText(context, "Esta publicación es tuya", android.widget.Toast.LENGTH_SHORT).show()
            }
        ))
    }
    var showPublishModal by remember { mutableStateOf(false) }
    var showPublishScreen by remember { mutableStateOf(false) }
    var publishModeIndex by remember { mutableIntStateOf(1) } // Default: Historia
    
    // Estado para EditPostModal (long press en posts)
    var postForEdit by remember { mutableStateOf<Post?>(null) }
    var showEditPostModal by remember { mutableStateOf(false) }
    
    // Estado para modal de seguidores/clientes
    var showFollowersList by remember { mutableStateOf(false) }
    
    // Estado para modal de quitar de guardados (movido aquí para que se renderice fuera del LazyColumn)
    var showUnsaveModal by remember { mutableStateOf(false) }
    var postToUnsave by remember { mutableStateOf<Post?>(null) }
    
    // Estado para logout - se activa después de limpiar sesión
    var shouldLogout by remember { mutableStateOf(false) }
    
    // Ejecutar logout fuera de coroutines para evitar problemas de contexto
    LaunchedEffect(shouldLogout) {
        if (shouldLogout) {
            onLogout()
        }
    }
    
    // Notificar al padre cuando cambia la visibilidad del StoriesViewer
    LaunchedEffect(showHighlightViewer) {
        onStoriesViewerVisibilityChange(showHighlightViewer)
    }
    
    // Notificar al padre cuando cambia la visibilidad del modal de ajustes
    LaunchedEffect(showSettingsModal) {
        onSettingsModalVisibilityChange(showSettingsModal)
    }
    
    // Estado del perfil desde el repositorio
    val profileFromRepo by ProfileRepository.currentProfile.collectAsState()
    val isLoading by ProfileRepository.isLoading.collectAsState()
    
    // Estado de highlights desde Supabase
    val highlightsFromRepo by HighlightRepository.highlights.collectAsState()
    
    // Posts del usuario desde el repositorio
    val userPosts by PostRepository.userPosts.collectAsState()
    val isLoadingUserPosts by PostRepository.isLoadingUserPosts.collectAsState()
    var hasInitiallyLoadedPosts by remember { mutableStateOf(PostRepository.userPosts.value.isNotEmpty()) }
    
    // Stories del usuario para halo del avatar
    val myStories by StoryRepository.myStories.collectAsState()
    val hasProfileStories = myStories.isNotEmpty()
    var showMyStoriesViewer by remember { mutableStateOf(false) }
    var isLoadingMyStories by remember { mutableStateOf(false) }
    
    // Rends del usuario
    val userRends by RendRepository.rends.collectAsState()
    
    // Posts guardados del usuario
    var savedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoadingSaved by remember { mutableStateOf(false) }
    
    // Pager state for swipeable tabs
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val screenConfig = androidx.compose.ui.platform.LocalConfiguration.current
    
    // Marcar cuando los posts terminan de cargar por primera vez
    LaunchedEffect(isLoadingUserPosts) {
        if (!isLoadingUserPosts && !hasInitiallyLoadedPosts && profileFromRepo != null) {
            hasInitiallyLoadedPosts = true
        }
    }
    // También marcar si ya hay posts disponibles
    LaunchedEffect(userPosts) {
        if (userPosts.isNotEmpty()) {
            hasInitiallyLoadedPosts = true
        }
    }

    // Estado del dot de nuevas formas - actualizado al entrar a editar perfil
    var hasNewShapesInProfile by remember { mutableStateOf(com.vinzay.app.data.repository.AvatarShapeRepository.hasUnseenShapes()) }
    
    // Cargar perfil, highlights, posts y rends del usuario al iniciar
    LaunchedEffect(Unit) {
        ProfileRepository.loadCurrentProfile()
        HighlightRepository.loadHighlights()
        PostRepository.loadUserPosts()
        RendRepository.loadRends()
    }

    // Reaccionar a cambios en seguidores (aceptación de solicitudes)
    // para actualizar contadores en el propio perfil
    LaunchedEffect(Unit) {
        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
        if (currentUserId != null) {
            FollowersRepository.subscribeToFollowChanges(currentUserId)
        }
    }

    LaunchedEffect(Unit) {
        FollowersRepository.followChangeTrigger.collect { (followerId, followedId) ->
            if (followedId == profileFromRepo?.userId) {
                Log.d("ProfileScreen", "🔄 Follow change detected, refrescando perfil...")
                ProfileRepository.loadCurrentProfile(forceRefresh = true)
                PostRepository.loadUserPosts(forceRefresh = true)
            }
        }
    }

    // También reaccionar al trigger vía notificaciones (fallback)
    LaunchedEffect(Unit) {
        NotificationRepository.profileRefreshTrigger.collect { _ ->
            if (profileFromRepo != null) {
                ProfileRepository.loadCurrentProfile(forceRefresh = true)
            }
        }
    }

    // Cargar posts guardados cuando se selecciona el tab Guardados
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 4 && savedPosts.isEmpty()) {
            isLoadingSaved = true
            try {
                val currentUserId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                if (currentUserId != null) {
                    val saves = com.vinzay.app.data.remote.SupabaseClient.database
                        .from("post_saves")
                        .select { filter { eq("user_id", currentUserId) } }
                        .decodeList<com.vinzay.app.ui.screens.home.PostSaveDB>()
                    
                    val savedPostIds = saves.map { it.postId }.toSet()
                    
                    if (savedPostIds.isNotEmpty()) {
                        val posts = com.vinzay.app.data.remote.SupabaseClient.database
                            .from("posts")
                            .select()
                            .decodeList<com.vinzay.app.data.model.PostDB>()
                            .filter { it.id in savedPostIds }
                        
                        val userIds = posts.map { it.userId }.distinct()
                        val usersMap = mutableMapOf<String, com.vinzay.app.data.repository.ExploreUserProfile>()
                        
                        for (userId in userIds) {
                            try {
                                val user = com.vinzay.app.data.remote.SupabaseClient.database
                                    .from("usuarios")
                                    .select { filter { eq("user_id", userId) } }
                                    .decodeSingleOrNull<com.vinzay.app.data.repository.ExploreUserProfile>()
                                if (user != null) usersMap[userId] = user
                            } catch (_: Exception) {}
                        }
                        
                        savedPosts = posts.map { post ->
                            val user = usersMap[post.userId]
                            Post(
                                id = post.id,
                                userId = post.userId,
                                title = post.title,
                                description = post.description,
                                price = post.price,
                                previousPrice = post.previousPrice,
                                category = post.category ?: "",
                                condition = post.condition ?: "",
                                images = post.images,
                                likesCount = post.likesCount,
                                reviewsCount = post.reviewsCount,
                                savesCount = post.savesCount,
                                sharesCount = post.sharesCount,
                                createdAt = post.createdAt,
                                username = user?.username ?: "usuario",
                                userAvatar = user?.avatarUrl ?: "",
                                userStoreName = user?.nombreTienda,
                                isSaved = true,
                                freeShipping = post.freeShipping ?: false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Error cargando guardados: ${e.message}")
            }
            isLoadingSaved = false
        }
    }
    
    // Usar datos del repositorio o valores por defecto
    val profile = profileFromRepo ?: ProfileData(
        username = "tu_usuario",
        nombre = "Tu Nombre",
        descripcion = "Amante de la moda y el estilo.",
        avatarUrl = null,
        ubicacion = "Buenos Aires, Argentina",
        publicaciones = 0,
        seguidores = 0,
        clientes = 0,
        reputacion = 0
    )
    
    // Historias destacadas desde Supabase
    val highlightedStories = highlightsFromRepo.map { highlight ->
        HighlightedStory(
            id = highlight.id,
            title = highlight.title,
            thumbnail = highlight.coverUrl,
            category = try {
                HighlightCategory.valueOf(highlight.category)
            } catch (e: Exception) {
                HighlightCategory.CUSTOM
            },
            frameStyle = try {
                HighlightFrameStyle.valueOf(highlight.frameStyle)
            } catch (e: Exception) {
                HighlightFrameStyle.CLASSIC
            },
            frameColor = try {
                HighlightFrameColor.valueOf(highlight.frameColor)
            } catch (e: Exception) {
                HighlightFrameColor.CATEGORY
            },
            backgroundColor = try {
                HighlightBackgroundColor.valueOf(highlight.backgroundColor)
            } catch (e: Exception) {
                HighlightBackgroundColor.DEFAULT
            },
            icon = highlight.icon,
            count = highlight.storiesCount
        )
    }
    
    Box(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        if (profileFromRepo == null) {
            // Solo skeleton completo si no hay datos de perfil aún
            ProfileScreenSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg)
            )
        } else {
        // Alturas medidas para calcular el espacio exacto del contenido de tabs
        var lazyColumnHeightPx by remember { mutableIntStateOf(0) }
        var tabBarHeightPx by remember { mutableIntStateOf(0) }
        val density = androidx.compose.ui.platform.LocalDensity.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
                .onSizeChanged { lazyColumnHeightPx = it.height }
        ) {
            // Header compacto con username y botón de publicar
            item {
                OwnProfileHeader(
                    username = profile.username,
                    isVerified = profile.isVerified,
                    reputacion = profile.reputacion,
                    onPublishClick = { showPublishModal = true }
                )
            }
            
            // Banner con borderRadius
            item {
                ProfileBanner(bannerUrl = profile.bannerUrl, username = profile.username)
            }
            
            // Header con avatar y stats
            item(key = "profileHeader") {
                // Observa el flow del repo: cambia EN TIEMPO REAL al elegir otra forma
                val repoShape by com.vinzay.app.data.repository.AvatarShapeRepository.selectedShapeFlow.collectAsState()
                val avatarShapeType = try {
                    // Si el repo tiene un valor distinto al default (circle), úsalo.
                    // Si no, lee del perfil (DB).
                    if (repoShape != com.vinzay.app.data.model.AvatarShapeType.CIRCLE || profile.avatarShape.isNullOrBlank()) {
                        repoShape
                    } else {
                        com.vinzay.app.data.model.AvatarShapeType.fromDbValue(profile.avatarShape)
                    }
                } catch (_: Exception) {
                    com.vinzay.app.data.model.AvatarShapeType.CIRCLE
                }
                ProfileHeader(
                    profile = profile,
                    hasStories = hasProfileStories,
                    isLoadingStories = isLoadingMyStories,
                    avatarShape = avatarShapeType.toShape(),
                    onAvatarClick = {
                        showAvatarViewer = true
                    },
                    onFollowersClick = { showFollowersList = true }
                )
            }
            
            // Botones de acción
            item(key = "profileActions_${hasNewShapesInProfile}") {
                ProfileActions(
                    onEditProfile = {
                        hasNewShapesInProfile = false
                        com.vinzay.app.data.repository.AvatarShapeRepository.markShapesAsSeen()
                        onEditProfile()
                    },
                    onRendshop = { showRendshop = true },
                    onMoreOptions = { showSettingsModal = true },
                    showNewShapeIndicator = hasNewShapesInProfile
                )
            }
            
            // Historias destacadas
            item {
                HighlightedStories(
                    stories = highlightedStories,
                    onStoryPress = { highlight ->
                        selectedHighlight = highlight
                        showHighlightViewer = true
                    },
                    onStoryLongPress = { highlight ->
                        highlightForOptions = highlight
                        showHighlightOptionsModal = true
                    },
                    onAddStory = { showAddHighlightModal = true },
                    canAddStories = true,
                    uploadingHighlightId = uploadingHighlightId,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Tabs pegajosas - suben con el contenido y se quedan arriba
            stickyHeader {
                Box(modifier = Modifier.onSizeChanged { tabBarHeightPx = it.height }) {
                    ProfileTabs(
                        tabs = profileTabs,
                        selectedIndex = selectedTabIndex,
                        onTabSelected = { index ->
                            selectedTabIndex = index
                        }
                    )
                }
            }

            // Contenido del tab seleccionado
            item(key = "tabContent_${selectedTabIndex}") {
                // Altura EXACTA del área visible bajo el TabBar sticky:
                // así el contenido corto queda contra el borde inferior sin
                // sobrepasar ni esconderse detrás del TabBar al hacer scroll.
                val visibleContentHeight = with(density) {
                    (lazyColumnHeightPx - tabBarHeightPx).coerceAtLeast(0).toDp()
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (visibleContentHeight > 0.dp) Modifier.heightIn(min = visibleContentHeight)
                            else Modifier
                        )
                        .padding(top = 6.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> if (!hasInitiallyLoadedPosts || (isLoadingUserPosts && userPosts.isEmpty())) {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp))
                        } else {
                            PostsGrid(
                                posts = userPosts,
                                onPostClick = { post ->
                                    openProduct(post)
                                },
                                onPostLongPress = { post ->
                                    postForEdit = post
                                    showEditPostModal = true
                                }
                            )
                        }
                        1 -> RendsGrid(
                            rends = userRends.filter { it.userId == profile.userId },
                            onRendClick = { rend ->
                                val rendAsPost = Post(
                                    id = rend.id,
                                    userId = rend.userId,
                                    username = rend.username,
                                    userAvatar = rend.userAvatar,
                                    userStoreName = rend.userStoreName,
                                    title = rend.productTitle ?: rend.title,
                                    description = rend.description,
                                    images = listOfNotNull(
                                        rend.productImage,
                                        rend.thumbnailUrl
                                    ).ifEmpty { listOf("https://via.placeholder.com/400") },
                                    price = rend.productPrice ?: 0.0,
                                    condition = "Nuevo",
                                    category = "",
                                    likesCount = rend.likesCount,
                                    reviewsCount = rend.reviewsCount,
                                    createdAt = System.currentTimeMillis().toString(),
                                    isLiked = false,
                                    isSaved = false,
                                    freeShipping = false,
                                    productId = rend.productId
                                )
                                openProduct(rendAsPost)
                            }
                        )
                        2 -> DetailsSection(profile = profile)
                        3 -> PointsSection()
                        4 -> SavedPostsGrid(
                            posts = savedPosts,
                            isLoading = isLoadingSaved,
                            onPostClick = { post ->
                                openProduct(post)
                            },
                            onRequestUnsave = { post ->
                                postToUnsave = post
                                showUnsaveModal = true
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        } // End of else block for skeleton
        
        // NavBar embebido - ANTES de todos los modales para que queden SOBRE él
        if (showNavBar) {
            BottomNavBar(
                currentRoute = currentNavRoute,
                onNavigate = onNavNavigate,
                onHomeReclick = onNavHomeReclick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // StoriesViewer para highlights - cargar stories reales
        if (showHighlightViewer && selectedHighlight != null) {
            var highlightStoriesList by remember { mutableStateOf<List<Story>>(emptyList()) }
            var isLoadingStories by remember { mutableStateOf(true) }
            
            // Cargar stories del highlight desde Supabase (refreshKey forces reload after adding stories)
            LaunchedEffect(selectedHighlight?.id, highlightRefreshKey) {
                isLoadingStories = true
                val stories = HighlightRepository.getHighlightStories(selectedHighlight!!.id)
                highlightStoriesList = if (stories.isNotEmpty()) {
                    stories.map { hs ->
                        Story(
                            id = hs.id,
                            userId = profile.userId,
                            username = profile.username,
                            userAvatar = profile.avatarUrl,
                            imageUrl = hs.mediaUrl,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                } else {
                    // Fallback al thumbnail si no hay stories
                    listOf(
                        Story(
                            id = selectedHighlight!!.id,
                            userId = profile.userId,
                            username = profile.username,
                            userAvatar = profile.avatarUrl,
                            imageUrl = selectedHighlight!!.thumbnail ?: "",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                isLoadingStories = false
            }
            
            if (!isLoadingStories && highlightStoriesList.isNotEmpty()) {
                val highlightUserStories = listOf(
                    UserStories(
                        userId = profile.userId,
                        username = profile.username,
                        userAvatar = profile.avatarUrl,
                        stories = highlightStoriesList
                    )
                )
                
                StoriesViewer(
                    userStories = highlightUserStories,
                    currentUserId = profile.userId,
                    onClose = {
                        showHighlightViewer = false
                        selectedHighlight = null
                    },
                    onStoryViewed = { },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (isLoadingStories) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = IconAccentBlue)
                }
            }
        }
        
        // Stories viewer del avatar (mis stories)
        if (showMyStoriesViewer && myStories.isNotEmpty()) {
            // Detener spinning cuando se muestra el viewer
            LaunchedEffect(Unit) { isLoadingMyStories = false }
            
            val myUserStories = listOf(
                UserStories(
                    userId = profile.userId,
                    username = profile.username,
                    userAvatar = profile.avatarUrl,
                    stories = myStories.map { story ->
                        Story(
                            id = story.id,
                            userId = profile.userId,
                            username = profile.username,
                            userAvatar = profile.avatarUrl,
                            imageUrl = story.mediaUrl,
                            timestamp = try { java.time.Instant.parse(story.createdAt).toEpochMilli() } catch (_: Exception) { System.currentTimeMillis() }
                        )
                    }
                )
            )
            
            StoriesViewer(
                userStories = myUserStories,
                currentUserId = profile.userId,
                onClose = {
                    showMyStoriesViewer = false
                    isLoadingMyStories = false
                },
                onStoryViewed = { },
                onDeleteStory = { storyId ->
                    scope.launch {
                        StoryRepository.deleteStory(storyId)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Modal de opciones de highlight (long press)
        HighlightOptionsModal(
            isVisible = showHighlightOptionsModal,
            highlightTitle = highlightForOptions?.title ?: "",
            highlightThumbnail = highlightForOptions?.thumbnail,
            onDismiss = { 
                showHighlightOptionsModal = false
                highlightForOptions = null
            },
            onAddImages = { uris ->
                highlightForOptions?.let { highlight ->
                    scope.launch {
                        uploadingHighlightId = highlight.id
                        android.util.Log.d("ProfileScreen", "Agregando ${uris.size} historias al highlight ${highlight.id}")
                        uris.forEach { uri ->
                            try {
                                // Cargar bitmap desde URI
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                inputStream?.close()
                                
                                if (bitmap != null) {
                                    val result = HighlightRepository.addStoryToHighlight(
                                        highlightId = highlight.id,
                                        storyId = null,
                                        mediaBitmap = bitmap,
                                        mediaUrl = null
                                    )
                                    if (result.isSuccess) {
                                        android.util.Log.d("ProfileScreen", "✅ Historia agregada al highlight")
                                    } else {
                                        android.util.Log.e("ProfileScreen", "❌ Error: ${result.exceptionOrNull()?.message}")
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileScreen", "Error procesando imagen", e)
                            }
                        }
                        // Recargar highlights después de agregar todas las imágenes
                        HighlightRepository.loadHighlights()
                        uploadingHighlightId = null
                        // Incrementar refresh key para que el viewer recargue
                        highlightRefreshKey++
                    }
                }
            },
            onDeleteHighlight = {
                highlightForOptions?.let { highlight ->
                    scope.launch {
                        isDeletingHighlight = true
                        try {
                            val result = HighlightRepository.deleteHighlight(highlight.id)
                            if (result.isSuccess) {
                                android.util.Log.d("ProfileScreen", "✅ Highlight eliminado")
                                showHighlightOptionsModal = false
                                highlightForOptions = null
                            }
                        } finally {
                            isDeletingHighlight = false
                        }
                    }
                }
            },
            isDeleting = isDeletingHighlight
        )
        
        // Modal para agregar highlight - fuera del Column pero dentro del Box
        AddHighlightModal(
            isVisible = showAddHighlightModal,
            onDismiss = { showAddHighlightModal = false },
            onCreateHighlight = { title, category, coverUri, frameStyle, frameColor, backgroundColor, icon ->
                scope.launch {
                    isCreatingHighlight = true
                    try {
                        android.util.Log.d("ProfileScreen", "=== CREANDO HIGHLIGHT ===")
                        android.util.Log.d("ProfileScreen", "Title: $title, Category: $category, Frame: $frameStyle, FrameColor: $frameColor, Bg: $backgroundColor, Icon: $icon")
                        
                        // Convertir Uri a Bitmap si existe
                        val coverBitmap = coverUri?.let { uri ->
                            android.util.Log.d("ProfileScreen", "Cargando imagen desde URI: $uri")
                            val inputStream = context.contentResolver.openInputStream(uri)
                            android.graphics.BitmapFactory.decodeStream(inputStream)
                        }
                        
                        android.util.Log.d("ProfileScreen", "Llamando a HighlightRepository.createHighlight()")
                        val result = HighlightRepository.createHighlight(
                            title = title,
                            category = category,
                            coverBitmap = coverBitmap,
                            frameStyle = frameStyle,
                            frameColor = frameColor,
                            backgroundColor = backgroundColor,
                            icon = icon
                        )
                        
                        if (result.isSuccess) {
                            android.util.Log.d("ProfileScreen", "✅ Highlight creado exitosamente en Supabase")
                            // El repositorio ya recarga automáticamente, pero esperamos un poco
                            kotlinx.coroutines.delay(300)
                            showAddHighlightModal = false
                        } else {
                            android.util.Log.e("ProfileScreen", "❌ Error al crear highlight: ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "❌ Excepción al crear highlight", e)
                    } finally {
                        isCreatingHighlight = false
                    }
                }
            },
            isLoading = isCreatingHighlight
        )
        
        // Rendshop Screen
        if (showRendshop) {
            RendshopScreen(
                onClose = { showRendshop = false },
                userId = profileFromRepo?.userId ?: "",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Modal de opciones de publicación
        PublishOptionsModal(
            isVisible = showPublishModal,
            onDismiss = { showPublishModal = false },
            onOptionSelected = { modeIndex ->
                publishModeIndex = modeIndex
                showPublishScreen = true
            }
        )
        
        // Pantalla de publicación
        if (showPublishScreen) {
            PublishScreen(
                onClose = { showPublishScreen = false },
                onStoryPublished = { showPublishScreen = false },
                onNavigateToHome = { showPublishScreen = false },
                initialMode = publishModeIndex,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Modal de configuración/ajustes
        ProfileSettingsModal(
            isVisible = showSettingsModal,
            onDismiss = { showSettingsModal = false },
            onPrivacySettings = { showSettingsModal = false; showPrivacySettings = true },
            onNotificationSettings = { showSettingsModal = false; showNotificationSettings = true },
            onSecuritySettings = { showSettingsModal = false; showSecuritySettings = true },
            onHelpCenter = { showSettingsModal = false; showHelpCenter = true },
            onAbout = { showSettingsModal = false; showAbout = true },
            onLogout = {
                scope.launch {
                    try {
                        // PRIMERO: Limpiar sesión persistida (CRÍTICO)
                        com.vinzay.app.data.remote.SessionPersistence.clearSession()
                        // Cerrar sesión en Supabase
                        com.vinzay.app.data.remote.SupabaseClient.auth.signOut()
                        // Limpiar perfil cargado
                        ProfileRepository.clearProfile()
                        android.widget.Toast.makeText(context, "Sesión cerrada", android.widget.Toast.LENGTH_SHORT).show()
                        // Navegar al login DESPUÉS de limpiar todo
                        shouldLogout = true
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Error al cerrar sesión: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        
        // Modal para editar publicaciones (long press)
        EditPostModal(
            isVisible = showEditPostModal,
            post = postForEdit,
            onDismiss = {
                showEditPostModal = false
                postForEdit = null
            },
            onSave = { editData ->
                scope.launch {
                    try {
                        val postId = postForEdit?.id ?: return@launch
                        
                        // Actualizar post en Supabase
                        val result = PostRepository.updatePost(
                            postId = postId,
                            title = editData.title,
                            description = editData.description,
                            price = editData.price,
                            previousPrice = if (editData.showOriginalPrice) editData.originalPrice else null,
                            category = editData.category,
                            condition = editData.condition,
                            allowOffers = editData.allowOffers,
                            freeShipping = editData.freeShipping,
                            status = if (editData.isAvailable) "active" else "paused",
                            warranty = editData.warranty,
                            returnsAccepted = editData.returnsAccepted,
                            colors = editData.colors,
                            images = editData.images.ifEmpty { null }
                        )
                        
                        if (result.isSuccess) {
                            android.widget.Toast.makeText(context, "Cambios guardados", android.widget.Toast.LENGTH_SHORT).show()
                            showEditPostModal = false
                            postForEdit = null
                        } else {
                            android.widget.Toast.makeText(context, "Error al guardar", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDelete = {
                scope.launch {
                    try {
                        val postId = postForEdit?.id ?: return@launch
                        
                        val result = PostRepository.deletePost(postId)
                        
                        if (result.isSuccess) {
                            android.widget.Toast.makeText(context, "Publicación eliminada", android.widget.Toast.LENGTH_SHORT).show()
                            showEditPostModal = false
                            postForEdit = null
                        } else {
                            android.widget.Toast.makeText(context, "Error al eliminar", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onPromote = {
                android.widget.Toast.makeText(context, "Promocionar publicación", android.widget.Toast.LENGTH_SHORT).show()
            },
            onViewStats = {
                android.widget.Toast.makeText(context, "Ver estadísticas", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
        
        // Modal de confirmación para quitar de guardados (FUERA del LazyColumn para posicionarse correctamente)
        if (showUnsaveModal && postToUnsave != null) {
            var isUnsaving by remember { mutableStateOf(false) }
            UnsaveConfirmationModal(
                post = postToUnsave!!,
                isLoading = isUnsaving,
                onConfirm = {
                    if (isUnsaving) return@UnsaveConfirmationModal
                    // Capturar ID antes de nullear
                    val postId = postToUnsave?.id ?: return@UnsaveConfirmationModal
                    isUnsaving = true
                    scope.launch {
                        try {
                            val currentUserId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.vinzay.app.data.remote.SupabaseClient.database
                                    .from("post_saves")
                                    .delete {
                                        filter {
                                            eq("user_id", currentUserId)
                                            eq("post_id", postId)
                                        }
                                    }
                            }
                            // Spinner breve para feedback visual
                            kotlinx.coroutines.delay(1200)
                            // Actualizar lista local
                            savedPosts = savedPosts.filter { it.id != postId }
                            android.widget.Toast.makeText(
                                context,
                                "Publicación eliminada de guardados",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            android.util.Log.e("ProfileScreen", "Error al quitar de guardados: ${e.message}")
                        }
                        isUnsaving = false
                        showUnsaveModal = false
                        postToUnsave = null
                    }
                },
                onDismiss = {
                    if (!isUnsaving) {
                        showUnsaveModal = false
                        postToUnsave = null
                    }
                }
            )
        }
        
        // Modal de seguidores/clientes
        FollowersListSheet(
            isVisible = showFollowersList,
            userId = profile.userId,
            seguidoresCount = profile.seguidores,
            clientesCount = profile.clientes,
            onDismiss = { showFollowersList = false },
            onUserClick = { userId ->
                showFollowersList = false
                // Navegar al perfil del usuario
            }
        )

        // Settings sub-screens
        com.vinzay.app.ui.components.settings.PrivacySettingsScreen(
            isVisible = showPrivacySettings,
            onDismiss = { showPrivacySettings = false }
        )
        
        com.vinzay.app.ui.components.settings.NotificationsSettingsScreen(
            isVisible = showNotificationSettings,
            userId = profileFromRepo?.userId ?: "",
            onDismiss = { showNotificationSettings = false }
        )
        
        com.vinzay.app.ui.components.settings.SecuritySettingsScreen(
            isVisible = showSecuritySettings,
            onDismiss = { showSecuritySettings = false },
            onLogout = {
                showSecuritySettings = false
                scope.launch {
                    try {
                        com.vinzay.app.data.remote.SessionPersistence.clearSession()
                        com.vinzay.app.data.remote.SupabaseClient.auth.signOut()
                        ProfileRepository.clearProfile()
                        shouldLogout = true
                    } catch (_: Exception) {}
                }
            }
        )
        
        com.vinzay.app.ui.components.settings.HelpCenterScreen(
            isVisible = showHelpCenter,
            onDismiss = { showHelpCenter = false }
        )
        
        com.vinzay.app.ui.components.settings.AboutScreen(
            isVisible = showAbout,
            onDismiss = { showAbout = false }
        )
        
        // Visor de avatar a pantalla completa
        if (showAvatarViewer) {
            Box(modifier = Modifier.fillMaxSize()) {
                com.vinzay.app.ui.components.AvatarFullscreenViewer(
                    avatarUrl = profile.avatarUrl,
                    username = profile.username,
                    shape = com.vinzay.app.data.repository.AvatarShapeRepository.getSelectedShape().toShape(),
                    onDismiss = { showAvatarViewer = false }
                )
            }
        }
    }
}

@Composable
private fun ProfileTopHeader(
    username: String,
    isVerified: Boolean,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Botón volver
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        // Username con badge de verificado
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "@$username",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Botón notificaciones
        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notificaciones",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ProfileBanner(bannerUrl: String?, username: String = "") {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Construir URL completa si es necesario
    val finalBannerUrl = remember(bannerUrl) {
        if (bannerUrl.isNullOrBlank()) null
        else if (bannerUrl.startsWith("http")) bannerUrl
        else "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/banners/$bannerUrl"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        if (finalBannerUrl != null) {
            android.util.Log.d("ProfileScreen", "Intentando cargar Banner: $finalBannerUrl")
            AsyncImage(
                model = remember(finalBannerUrl) {
                    coil.request.ImageRequest.Builder(context)
                        .data(finalBannerUrl)
                        .crossfade(true)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build()
                },
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { android.util.Log.d("ProfileScreen", "✅ Banner cargado con éxito") },
                onError = {
                    android.util.Log.e("ProfileScreen", "❌ Error cargando banner de $username: $finalBannerUrl")
                    android.util.Log.e("ProfileScreen", "Causa: ${it.result.throwable.message}")
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0A3D62).copy(alpha = 0.4f),
                                Color(0xFF2E8B57).copy(alpha = 0.3f),
                                Color(0xFF1A1A2E)
                            )
                        )
                    )
            )
        }
        
        // Gradient overlay sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, HomeBg.copy(alpha = 0.8f)),
                        startY = 100f,
                        endY = 400f
                    )
                )
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: ProfileData,
    hasStories: Boolean = false,
    isLoadingStories: Boolean = false,
    avatarShape: androidx.compose.ui.graphics.Shape = CircleShape,
    onAvatarClick: () -> Unit = {},
    onFollowersClick: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Animación de giro del halo (solo gira al cargar stories)
    val infiniteTransition = rememberInfiniteTransition(label = "profileHalo")
    val rotateRing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateRing"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-28).dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            
            // Avatar con halo condicional (cercano, sin giro por defecto)
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                // Halo (solo cuando hay stories, gira solo al cargar)
                if (hasStories) {
                    Canvas(
                        modifier = Modifier
                            .size(84.dp)
                            .then(if (isLoadingStories) Modifier.rotate(rotateRing) else Modifier)
                    ) {
                        val strokeWidth = 2.5.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFF0A3D62),
                                    Color(0xFFFF6B35).copy(alpha = 0.2f),
                                    Color(0xFF0A3D62),
                                    Color(0xFFFF6B35)
                                ),
                                center = Offset(size.width / 2, size.height / 2)
                            ),
                            radius = radius,
                            center = Offset(size.width / 2, size.height / 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
                
                // Avatar centrado (gap ~4dp al halo)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(avatarShape)
                        .background(HomeBg)
                ) {
                    val avatarToLoad = remember(profile.avatarUrl) {
                        if (profile.avatarUrl.isNullOrBlank()) {
                            "https://ui-avatars.com/api/?name=${profile.username}&background=A78BFA&color=fff"
                        } else if (profile.avatarUrl.startsWith("http")) {
                            profile.avatarUrl
                        } else {
                            "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${profile.avatarUrl}"
                        }
                    }
                    
                    android.util.Log.d("ProfileScreen", "Intentando cargar Avatar: $avatarToLoad")
                    AsyncImage(
                        model = remember(avatarToLoad) {
                            coil.request.ImageRequest.Builder(context)
                                .data(avatarToLoad)
                                .crossfade(true)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .size(128) // Un poco más grande para perfil
                                .build()
                        },
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(avatarShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(17.dp))
            
            // Stats - Publicaciones, Seguidores, Clientes
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileStat(count = profile.publicaciones, label = "Publicaciones")
                ProfileStat(count = profile.seguidores, label = "Seguidores", onClick = onFollowersClick)
                ProfileStat(count = profile.clientes, label = "Clientes", onClick = onFollowersClick)
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Nombre + Badge de verificación (sin badge de reputación - ahora está en el header)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = profile.nombreTienda ?: profile.nombre ?: profile.username,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Badge de verificación junto al nombre
            if (profile.isVerified) {
                Spacer(modifier = Modifier.width(3.dp))
                com.vinzay.app.ui.components.VerifiedBadge(size = 14.dp)
            }
        }
        
        // Bio - sin @username (ahora está en el header)
        if (!profile.descripcion.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = profile.descripcion!!,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ProfileStat(count: Int, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text = formatCount(count),
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ProfileStatPercent(percent: Int, label: String) {
    Column(horizontalAlignment = Alignment.Start) { // Alineado al inicio
        Text(
            text = "$percent%",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ProfileActions(
    onEditProfile: () -> Unit,
    onRendshop: () -> Unit,
    onMoreOptions: () -> Unit,
    showNewShapeIndicator: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-12).dp) // Ajustado para balance perfecto
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Editar perfil
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Surface),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = IconAccentBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Editar perfil",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            // Indicador de nuevos shapes desbloqueados
            if (showNewShapeIndicator) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                        .align(Alignment.TopEnd)
                )
            }
        }
        
        // Rendshop
        Button(
            onClick = onRendshop,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Surface),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                tint = IconAccentBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Rendshop",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
        
        // Más opciones - misma altura que los otros botones
        Button(
            onClick = onMoreOptions,
            colors = ButtonDefaults.buttonColors(containerColor = Surface),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más",
                tint = IconAccentBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileTabs(
    tabs: List<ProfileTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(0.5.dp)
                .background(TextMuted.copy(alpha = 0.2f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) IconAccentBlue else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        color = if (isSelected) IconAccentBlue else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f / tabs.size)
                    .offset(x = (selectedIndex * (1f / tabs.size) * 360).dp)
                    .padding(horizontal = 20.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(IconAccentBlue)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PostsGrid(
    posts: List<Post>,
    onPostClick: (Post) -> Unit = {},
    onPostLongPress: (Post) -> Unit = {}
) {
    // Grilla NO scrollable - el scroll viene del LazyColumn padre
    // Esto permite scroll unificado de toda la pantalla
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sin publicaciones aún",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tus productos aparecerán aquí",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        // Grilla manual de 3 columnas - SIN scroll propio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Dividir posts en filas de 3
            posts.chunked(3).forEach { rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowPosts.forEach { post ->
                        val imageUrl = post.images.firstOrNull() ?: ""
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = post.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .clip(RoundedCornerShape(6.dp))
                                .combinedClickable(
                                    onClick = { onPostClick(post) },
                                    onLongClick = { onPostLongPress(post) }
                                )
                        )
                    }
                    // Rellenar espacios vacíos si la fila no está completa
                    repeat(3 - rowPosts.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RendsGrid(rends: List<Rend>, onRendClick: (Rend) -> Unit = {}) {
    // Grilla NO scrollable - el scroll viene del LazyColumn padre
    if (rends.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.VideoLibrary,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sin clips aún",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tus clips aparecerán aquí",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        // Grilla manual de 3 columnas - SIN scroll propio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            rends.chunked(3).forEach { rowRends ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowRends.forEach { rend ->
                        // Generar thumbnail con ImageKit transformation (igual que TrendenciasScreen)
                        val thumbnailToShow = if (rend.videoUrl.contains("ik.imagekit.io")) {
                            "${rend.videoUrl}/ik-thumbnail.jpg"
                        } else {
                            rend.thumbnailUrl ?: rend.productImage ?: rend.videoUrl
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1A1A2E))
                                .clickable { onRendClick(rend) }
                        ) {
                            // Thumbnail
                            AsyncImage(
                                model = thumbnailToShow,
                                contentDescription = rend.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Video icon en esquina superior derecha
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            
                            // View count en esquina inferior izquierda
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = formatViewCount(rend.viewsCount),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    // Rellenar espacios vacíos
                    repeat(3 - rowRends.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun formatViewCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
private fun DetailsSection(profile: ProfileData) {
    // Load real seller stats
    var sellerStats by remember { mutableStateOf<com.vinzay.app.data.model.SellerStats?>(null) }
    LaunchedEffect(profile.userId) {
        if (profile.userId.isNotEmpty()) {
            sellerStats = com.vinzay.app.data.repository.OrderRepository.getSellerStats(profile.userId)
        }
    }
    
    val reputation = profile.reputacion
    val ratingValue = sellerStats?.avgRating?.let { String.format("%.1f", it) } ?: "N/A"
    val responseValue = sellerStats?.formattedResponseTime ?: "N/A"
    
    // Trust level based on reputation
    val trustLabel = when {
        reputation >= 90 -> "Vendedor Excelente"
        reputation >= 80 -> "Vendedor Confiable"
        reputation >= 70 -> "Vendedor Activo"
        else -> "Vendedor Nuevo"
    }
    val trustColor = when {
        reputation >= 80 -> Color(0xFF2E8B57)
        reputation >= 70 -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
    }
    val trustBadge = when {
        reputation >= 90 -> "TOP"
        reputation >= 80 -> "PRO"
        else -> null
    }
    val verifiedText = if (profile.isVerified) " • Verificado" else ""
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con título elegante
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(IconAccentBlue, Color(0xFF2E8B57))
                        )
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sobre mí",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Tarjeta de estadísticas principales
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(
                        icon = Icons.Outlined.People,
                        value = "${profile.clientes}",
                        label = "Clientes",
                        color = Color(0xFF2E8B57)
                    )
                    StatDivider()
                    StatColumn(
                        icon = Icons.Outlined.Star,
                        value = ratingValue,
                        label = "Valoración",
                        color = Color(0xFFFF6B35)
                    )
                    StatDivider()
                    StatColumn(
                        icon = Icons.Outlined.Speed,
                        value = responseValue,
                        label = "Respuesta",
                        color = Color(0xFF1565A0)
                    )
                }
            }
        }
        
        // Indicador de confianza mejorado (dinámico)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = trustColor.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(trustColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = trustColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trustLabel,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${reputation}% de confianza$verifiedText",
                        color = trustColor,
                        fontSize = 13.sp
                    )
                }
                if (trustBadge != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = trustColor
                    ) {
                        Text(
                            text = trustBadge,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Sección de información personal
        Text(
            text = "Información",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Grid de información
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Surface
        ) {
            Column {
                InfoRow(
                    icon = Icons.Outlined.LocationOn,
                    iconColor = IconAccentBlue,
                    label = "Ubicación",
                    value = profile.ubicacion ?: "No especificada",
                    showDivider = true
                )
                InfoRow(
                    icon = Icons.Outlined.CalendarMonth,
                    iconColor = Color(0xFF60A5FA),
                    label = "Miembro desde",
                    value = profile.miembroDesde,
                    showDivider = true
                )
                InfoRow(
                    icon = Icons.Outlined.Language,
                    iconColor = Color(0xFFFF6B35),
                    label = "Idioma",
                    value = "Español",
                    showDivider = true
                )
                InfoRow(
                    icon = Icons.Outlined.LocalShipping,
                    iconColor = Color(0xFFFF6B35),
                    label = "Envíos",
                    value = "A todo el país",
                    showDivider = false
                )
            }
        }
        
        // Métodos de pago aceptados
        Text(
            text = "Métodos de pago",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PaymentMethodChip(
                icon = Icons.Outlined.CreditCard,
                label = "Tarjeta",
                color = Color(0xFF1565A0)
            )
            PaymentMethodChip(
                icon = Icons.Outlined.AccountBalance,
                label = "Transferencia",
                color = Color(0xFF2E8B57)
            )
            PaymentMethodChip(
                icon = Icons.Outlined.Payments,
                label = "Efectivo",
                color = Color(0xFFFF6B35)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatColumn(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(50.dp)
            .background(TextMuted.copy(alpha = 0.15f))
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(TextMuted.copy(alpha = 0.08f))
            )
        }
    }
}

@Composable
private fun PaymentMethodChip(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = iconColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PointsSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = IconAccentBlue.copy(alpha = 0.1f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tu saldo",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Text(
                    text = "1,250 pts",
                    color = IconAccentBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nivel: Bronce",
                    color = Color(0xFFCD7F32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = 0.65f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = IconAccentBlue,
                    trackColor = Surface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "350 pts para Plata",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
        
        // Misiones diarias
        Text(
            text = "Misiones Diarias",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        QuestItem("Publica 3 posts hoy", 100, false)
        QuestItem("Responde 5 comentarios", 50, true)
        QuestItem("Comparte tu perfil", 75, false)
    }
}

@Composable
private fun QuestItem(title: String, reward: Int, completed: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (completed) Color(0xFF2E8B57).copy(alpha = 0.1f) else Surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$reward pts",
                color = if (completed) Color(0xFF2E8B57) else IconAccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (completed) Color(0xFF2E8B57) else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (completed) Color(0xFF2E8B57) else TextMuted,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
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
private fun OwnProfileHeader(
    username: String,
    isVerified: Boolean = false,
    reputacion: Int = 0,
    onPublishClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Username alineado a la izquierda con badge de verificación y reputación
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "@$username",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            // Badge de reputación junto al username
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    reputacion >= 90 -> AccentGreen.copy(alpha = 0.15f)
                    reputacion >= 70 -> Color(0xFFFFA726).copy(alpha = 0.15f)
                    else -> Color(0xFFEF5350).copy(alpha = 0.15f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Reputación",
                        tint = when {
                            reputacion >= 90 -> AccentGreen
                            reputacion >= 70 -> Color(0xFFFFA726)
                            else -> Color(0xFFEF5350)
                        },
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$reputacion%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            reputacion >= 90 -> AccentGreen
                            reputacion >= 70 -> Color(0xFFFFA726)
                            else -> Color(0xFFEF5350)
                        }
                    )
                }
            }
        }
        
        // Botón para abrir modal de publicación (solo icono, sin fondo)
        IconButton(
            onClick = onPublishClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AddBox,
                contentDescription = "Publicar",
                tint = IconAccentBlue,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun SavedPostsGrid(
    posts: List<Post>,
    isLoading: Boolean,
    onPostClick: (Post) -> Unit,
    onRequestUnsave: (Post) -> Unit = {} // Callback para solicitar quitar de guardados (modal se maneja en ProfileScreen)
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = IconAccentBlue)
            }
        }
        posts.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sin publicaciones guardadas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Las publicaciones que guardes aparecerán aquí",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            // Grid de 3 columnas igual que PostsGrid
            val chunkedPosts = posts.chunked(3)
            Column(
                modifier = Modifier.padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                chunkedPosts.forEach { rowPosts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        rowPosts.forEach { post ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.8f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onPostClick(post) }
                            ) {
                                AsyncImage(
                                    model = post.images.firstOrNull() ?: "",
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Badge de guardado - clickable para quitar de guardados
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .clickable { onRequestUnsave(post) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bookmark,
                                        contentDescription = "Quitar de guardados",
                                        tint = AccentYellow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                // Precio en la esquina inferior
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$${String.format("%.0f", post.price)}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // Espacios vacíos si la fila no está completa
                        repeat(3 - rowPosts.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// Modal de confirmación para quitar de guardados - aparece desde abajo con animación
@Composable
private fun UnsaveConfirmationModal(
    post: Post,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Animación de entrada desde abajo
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val offsetY by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isVisible) 0.dp else 300.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "offsetY"
    )
    
    val backdropAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 0.6f else 0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "backdropAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backdropAlpha))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consumir clicks
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextMuted.copy(alpha = 0.3f))
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Preview de la imagen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = post.images.firstOrNull() ?: "",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Quitar de guardados?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Esta publicación se eliminará de tu colección",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Cancelar
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Cancelar",
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                    
                    // Botón Quitar
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444) // Rojo
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Eliminando...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkRemove,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quitar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
