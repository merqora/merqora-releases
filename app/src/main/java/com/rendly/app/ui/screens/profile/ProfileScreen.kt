package com.rendly.app.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Highlight
import com.rendly.app.data.repository.ProfileRepository
import com.rendly.app.data.repository.HighlightRepository
import com.rendly.app.ui.components.HighlightedStories
import com.rendly.app.ui.components.HighlightedStory
import com.rendly.app.ui.components.HighlightCategory
import com.rendly.app.ui.components.HighlightFrameStyle
import com.rendly.app.ui.components.HighlightFrameColor
import com.rendly.app.ui.components.HighlightBackgroundColor
import com.rendly.app.ui.components.ProfileStatsBadges
import com.rendly.app.ui.components.SellerTrustIndicator
import com.rendly.app.ui.components.AddHighlightModal
import com.rendly.app.ui.components.StoriesViewer
import com.rendly.app.ui.components.Story
import com.rendly.app.ui.components.UserStories
import com.rendly.app.ui.components.HighlightOptionsModal
import com.rendly.app.ui.components.ProductPage
import com.rendly.app.ui.components.PublishOptionsModal
import com.rendly.app.ui.components.ProfileSettingsModal
import com.rendly.app.ui.components.BottomNavBar
import com.rendly.app.ui.components.EditPostModal
import com.rendly.app.ui.screens.publish.PublishScreen
import androidx.compose.foundation.combinedClickable
import com.rendly.app.data.model.Post
import com.rendly.app.data.model.Rend
import com.rendly.app.data.repository.PostRepository
import com.rendly.app.data.repository.RendRepository
import com.rendly.app.data.repository.StoryRepository
import com.rendly.app.ui.components.ProfileScreenSkeleton
import com.rendly.app.ui.components.ProfileGridSkeleton
import com.rendly.app.ui.theme.*
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
    val isVerified: Boolean = false
)

private data class ProfileTab(
    val id: String,
    val icon: ImageVector,
    val label: String
)

private val profileTabs = listOf(
    ProfileTab("posts", Icons.Default.Menu, "Catálogo"),
    ProfileTab("videos", Icons.Outlined.PlayArrow, "Rends"),
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
    modifier: Modifier = Modifier,
    // NavBar embebido
    showNavBar: Boolean = true,
    currentNavRoute: String = "profile",
    onNavNavigate: (String) -> Unit = {},
    onNavHomeReclick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Estado del modal de agregar highlight
    var showAddHighlightModal by remember { mutableStateOf(false) }
    var isCreatingHighlight by remember { mutableStateOf(false) }
    
    // Estado para StoriesViewer de highlights
    var showHighlightViewer by remember { mutableStateOf(false) }
    var selectedHighlight by remember { mutableStateOf<HighlightedStory?>(null) }
    
    // Estado para modal de opciones de highlight (long press)
    var showHighlightOptionsModal by remember { mutableStateOf(false) }
    var highlightForOptions by remember { mutableStateOf<HighlightedStory?>(null) }
    var isDeletingHighlight by remember { mutableStateOf(false) }
    
    // Estado para Rendshop
    var showRendshop by remember { mutableStateOf(false) }
    
    // Estado para modal de ajustes
    var showSettingsModal by remember { mutableStateOf(false) }
    
    // Estado para ProductPage
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showProductPage by remember { mutableStateOf(false) }
    var showPublishModal by remember { mutableStateOf(false) }
    var showPublishScreen by remember { mutableStateOf(false) }
    var publishModeIndex by remember { mutableIntStateOf(1) } // Default: Historia
    
    // Estado para EditPostModal (long press en posts)
    var postForEdit by remember { mutableStateOf<Post?>(null) }
    var showEditPostModal by remember { mutableStateOf(false) }
    
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
    
    // Estado de tab seleccionado (declarado antes de usarse en LaunchedEffect)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Cargar perfil, highlights, posts y rends del usuario al iniciar
    LaunchedEffect(Unit) {
        ProfileRepository.loadCurrentProfile()
        HighlightRepository.loadHighlights()
        PostRepository.loadUserPosts()
        RendRepository.loadRends()
    }
    
    // Cargar posts guardados cuando se selecciona el tab
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 4 && savedPosts.isEmpty()) {
            isLoadingSaved = true
            try {
                val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                if (currentUserId != null) {
                    val saves = com.rendly.app.data.remote.SupabaseClient.database
                        .from("post_saves")
                        .select { filter { eq("user_id", currentUserId) } }
                        .decodeList<com.rendly.app.ui.screens.home.PostSaveDB>()
                    
                    val savedPostIds = saves.map { it.postId }.toSet()
                    
                    if (savedPostIds.isNotEmpty()) {
                        val posts = com.rendly.app.data.remote.SupabaseClient.database
                            .from("posts")
                            .select()
                            .decodeList<com.rendly.app.data.model.PostDB>()
                            .filter { it.id in savedPostIds }
                        
                        val userIds = posts.map { it.userId }.distinct()
                        val usersMap = mutableMapOf<String, com.rendly.app.data.repository.ExploreUserProfile>()
                        
                        for (userId in userIds) {
                            try {
                                val user = com.rendly.app.data.remote.SupabaseClient.database
                                    .from("usuarios")
                                    .select { filter { eq("user_id", userId) } }
                                    .decodeSingleOrNull<com.rendly.app.data.repository.ExploreUserProfile>()
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
                                isSaved = true
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
        // LazyColumn para scroll unificado de toda la pantalla
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
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
                ProfileBanner(bannerUrl = profile.bannerUrl)
            }
            
            // Header con avatar y stats
            item {
                ProfileHeader(
                    profile = profile,
                    hasStories = hasProfileStories,
                    isLoadingStories = isLoadingMyStories,
                    onAvatarClick = {
                        if (hasProfileStories) {
                            isLoadingMyStories = true
                            showMyStoriesViewer = true
                        }
                    }
                )
            }
            
            // Botones de acción
            item {
                ProfileActions(
                    onEditProfile = onEditProfile,
                    onRendshop = { showRendshop = true },
                    onMoreOptions = { showSettingsModal = true }
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
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Tabs STICKY - se pegan arriba al hacer scroll
            stickyHeader {
                ProfileTabs(
                    tabs = profileTabs,
                    selectedIndex = selectedTabIndex,
                    onTabSelected = { index -> selectedTabIndex = index }
                )
            }
            
            // Contenido de tabs
            item {
                when (selectedTabIndex) {
                    0 -> if (isLoadingUserPosts && userPosts.isEmpty()) {
                        ProfileGridSkeleton(itemCount = 9)
                    } else {
                        PostsGrid(
                            posts = userPosts,
                            onPostClick = { post ->
                                selectedPost = post
                                showProductPage = true
                            },
                            onPostLongPress = { post ->
                                postForEdit = post
                                showEditPostModal = true
                            }
                        )
                    }
                    1 -> RendsGrid(rends = userRends)
                    2 -> DetailsSection(profile = profile)
                    3 -> PointsSection()
                    4 -> SavedPostsGrid(
                        posts = savedPosts,
                        isLoading = isLoadingSaved,
                        onPostClick = { post ->
                            selectedPost = post
                            showProductPage = true
                        },
                        onRequestUnsave = { post ->
                            // Mostrar modal de confirmación (fuera del LazyColumn)
                            postToUnsave = post
                            showUnsaveModal = true
                        }
                    )
                }
                
                // Spacer para NavBar bottom
                Spacer(modifier = Modifier.height(80.dp))
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
            
            // Cargar stories del highlight desde Supabase
            LaunchedEffect(selectedHighlight?.id) {
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
                        android.util.Log.d("ProfileScreen", "Agregando ${uris.size} imágenes al highlight ${highlight.id}")
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
                                        android.util.Log.d("ProfileScreen", "✅ Imagen agregada al highlight")
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
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // ProductPage para ver detalles del post
        ProductPage(
            post = selectedPost,
            isVisible = showProductPage,
            onDismiss = {
                showProductPage = false
                selectedPost = null
            }
        )
        
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
            onPrivacySettings = { /* TODO */ },
            onNotificationSettings = { /* TODO */ },
            onSecuritySettings = { /* TODO */ },
            onHelpCenter = { /* TODO */ },
            onAbout = { /* TODO */ },
            onLogout = {
                scope.launch {
                    try {
                        // PRIMERO: Limpiar sesión persistida (CRÍTICO)
                        com.rendly.app.data.remote.SessionPersistence.clearSession()
                        // Cerrar sesión en Supabase
                        com.rendly.app.data.remote.SupabaseClient.auth.signOut()
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
            UnsaveConfirmationModal(
                post = postToUnsave!!,
                onConfirm = {
                    // Quitar de guardados en Supabase
                    scope.launch {
                        try {
                            val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                            if (currentUserId != null) {
                                com.rendly.app.data.remote.SupabaseClient.database
                                    .from("post_saves")
                                    .delete {
                                        filter {
                                            eq("user_id", currentUserId)
                                            eq("post_id", postToUnsave!!.id)
                                        }
                                    }
                            }
                            // Actualizar lista local
                            savedPosts = savedPosts.filter { it.id != postToUnsave!!.id }
                            android.widget.Toast.makeText(
                                context,
                                "Publicación eliminada de guardados",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            android.util.Log.e("ProfileScreen", "Error al quitar de guardados: ${e.message}")
                        }
                    }
                    showUnsaveModal = false
                    postToUnsave = null
                },
                onDismiss = {
                    showUnsaveModal = false
                    postToUnsave = null
                }
            )
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
private fun ProfileBanner(bannerUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 8.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        if (bannerUrl != null) {
            AsyncImage(
                model = bannerUrl,
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
    onAvatarClick: () -> Unit = {}
) {
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
                        .clip(CircleShape)
                        .background(HomeBg)
                ) {
                    AsyncImage(
                        model = profile.avatarUrl ?: "https://ui-avatars.com/api/?name=${profile.username}&background=A78BFA&color=fff",
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
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
                ProfileStat(count = profile.seguidores, label = "Seguidores")
                ProfileStat(count = profile.clientes, label = "Clientes")
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
                com.rendly.app.ui.components.VerifiedBadge(size = 14.dp)
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
private fun ProfileStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.Start) { // Alineado al inicio
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
    onMoreOptions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-12).dp) // Ajustado para balance perfecto
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Editar perfil
        Button(
            onClick = onEditProfile,
            modifier = Modifier.weight(1f),
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
            .background(HomeBg) // Fondo sólido para sticky header
    ) {
        // Línea separadora sutil sobre las tabs
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
        
        // Indicador animado
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
private fun RendsGrid(rends: List<Rend>) {
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
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sin Rends aún",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tus videos cortos aparecerán aquí",
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
            rends.chunked(3).forEach { rowRends ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    rowRends.forEach { rend ->
                        // Generar thumbnail del video si no hay uno
                        val thumbnailToShow = rend.thumbnailUrl?.takeIf { it.isNotBlank() } 
                            ?: rend.videoUrl.takeIf { it.isNotBlank() }?.let { videoUrl ->
                                // Para ImageKit, agregar transformación de thumbnail
                                if (videoUrl.contains("ik.imagekit.io")) {
                                    videoUrl.replace("/rends/", "/rends/tr:so-1/") // Frame at 1 second
                                } else videoUrl
                            }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0A3D62).copy(alpha = 0.3f),
                                            Color(0xFFFF6B35).copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        ) {
                            AsyncImage(
                                model = thumbnailToShow,
                                contentDescription = rend.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Play icon overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Title at bottom
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = rend.title,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
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

@Composable
private fun DetailsSection(profile: ProfileData) {
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
                        value = "4.8",
                        label = "Valoración",
                        color = Color(0xFFFF6B35)
                    )
                    StatDivider()
                    StatColumn(
                        icon = Icons.Outlined.Speed,
                        value = "< 1h",
                        label = "Respuesta",
                        color = Color(0xFF1565A0)
                    )
                }
            }
        }
        
        // Indicador de confianza mejorado
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2E8B57).copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8B57).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = Color(0xFF2E8B57),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vendedor Confiable",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "92% de confianza • Verificado",
                        color = Color(0xFF2E8B57),
                        fontSize = 13.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E8B57)
                ) {
                    Text(
                        text = "TOP",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
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
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444) // Rojo
                        )
                    ) {
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
