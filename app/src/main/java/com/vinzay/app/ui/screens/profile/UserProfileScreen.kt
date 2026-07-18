package com.vinzay.app.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
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
import com.vinzay.app.data.model.Post
import com.vinzay.app.data.model.Usuario
import com.vinzay.app.data.remote.SupabaseClient
import com.vinzay.app.ui.components.HighlightedStories
import com.vinzay.app.ui.components.HighlightedStory
import com.vinzay.app.ui.components.HighlightCategory
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.vinzay.app.data.model.PostDB
import com.vinzay.app.data.repository.ChatRepository
import com.vinzay.app.data.repository.FollowersRepository
import com.vinzay.app.data.repository.FollowType
import com.vinzay.app.data.repository.NotificationRepository
import com.vinzay.app.data.repository.StoryRepository
import com.vinzay.app.data.repository.UserPreferencesRepository
import com.vinzay.app.ui.components.FollowBottomSheet
import com.vinzay.app.ui.components.FollowersListSheet
import com.vinzay.app.ui.components.NotificationsSettingsModal
import com.vinzay.app.ui.components.ProfileScreenSkeleton
import com.vinzay.app.ui.components.ProfileGridSkeleton
import com.vinzay.app.ui.components.UserProfileSettingsModal
import com.vinzay.app.ui.components.ReportModal
import io.github.jan.supabase.postgrest.from

// Tabs para el perfil de otros usuarios (sin "Puntos")
private data class UserProfileTab(
    val id: String,
    val icon: ImageVector,
    val label: String
)

private val userProfileTabs = listOf(
    UserProfileTab("posts", Icons.Default.Menu, "Catálogo"),
    UserProfileTab("videos", Icons.Outlined.PlayArrow, "Rends"),
    UserProfileTab("details", Icons.Outlined.Info, "Detalles")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onPostClick: (Post) -> Unit = {},
    onOpenChat: (Usuario) -> Unit = {}, // Solo el usuario
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Estado del usuario
    var userProfile by remember { mutableStateOf<Usuario?>(null) }
    var userPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingPosts by remember { mutableStateOf(true) }
    
    // Estado de seguimiento
    var currentFollowType by remember { mutableStateOf(FollowType.NONE) }
    var showFollowSheet by remember { mutableStateOf(false) }
    var isClientLoading by remember { mutableStateOf(false) } // Solo para solicitar cliente
    
    // Estado para modal de notificaciones
    var showNotificationsModal by remember { mutableStateOf(false) }
    
    // Estado para modal de opciones del perfil
    var showProfileSettingsModal by remember { mutableStateOf(false) }
    var isUserBlocked by remember { mutableStateOf(false) }
    var isUserMuted by remember { mutableStateOf(false) }
    
    // Estado para modal de reporte
    var showReportModal by remember { mutableStateOf(false) }
    
    // Privacy visibility
    var profileVisibility by remember { mutableStateOf("public") }
    var isContentRestricted by remember { mutableStateOf(false) }
    
    // Estado para modal de seguidores/clientes
    var showFollowersList by remember { mutableStateOf(false) }

    // Stats dinámicos desde FollowersRepository
    var seguidoresCount by remember { mutableStateOf(0) }
    var clientesCount by remember { mutableStateOf(0) }
    var reputacionCalc by remember { mutableStateOf(70) }
    
    val pagerState = rememberPagerState(pageCount = { userProfileTabs.size })
    
    // Clave para forzar recarga al volver a la pantalla
    var reloadKey by remember { mutableStateOf(0) }

    // Suscribirse a cambios en followers para este usuario
    LaunchedEffect(userId) {
        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
        if (currentUserId != null) {
            FollowersRepository.subscribeToFollowChanges(currentUserId)
        }
    }

    // Reaccionar a cambios directos en la tabla followers
    // (se activa cuando is_pending cambia → solicitud aceptada/rechazada)
    LaunchedEffect(userId) {
        FollowersRepository.followChangeTrigger.collect { (_, followedId) ->
            if (followedId == userId) {
                Log.d("UserProfileScreen", "🔄 Follow change match: followed=$followedId, refrescando...")
                reloadKey++
            }
        }
    }

    // Reaccionar a aceptación/rechazo de solicitudes via notificaciones Realtime
    LaunchedEffect(userId) {
        NotificationRepository.profileRefreshTrigger.collect { _ ->
            reloadKey++
        }
    }

    // Cargar perfil del usuario: USER primero (secuencial), luego todo en PARALELO
    LaunchedEffect(userId, reloadKey) {
        try {
            isLoading = true
            isLoadingPosts = true

            // 1. Cargar usuario primero (determina si existe)
            val user = SupabaseClient.database
                .from("usuarios")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<Usuario>()

            userProfile = user
            isLoading = false

            if (user == null) {
                isLoadingPosts = false
                return@LaunchedEffect
            }

            // 2. Posts, stats, privacidad y follow type en PARALELO
            coroutineScope {
                val postsDeferred = async {
                    SupabaseClient.database
                        .from("posts")
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<PostDB>()
                }

                val statsDeferred = async {
                    val followers = FollowersRepository.getFollowersCount(userId)
                    val clients = FollowersRepository.getClientsCount(userId)
                    val rep = FollowersRepository.getReputation(userId)
                    Triple(followers, clients, rep)
                }

                val privacyDeferred = async {
                    try { UserPreferencesRepository.loadPrivacySettings(userId) }
                    catch (_: Exception) { null }
                }

                val followTypeDeferred = async {
                    val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                    if (currentUserId != null) {
                        FollowersRepository.getFollowType(currentUserId, userId)
                    } else FollowType.NONE
                }

                val postsDB = postsDeferred.await()
                val (followers, clients, rep) = statsDeferred.await()
                val privacy = privacyDeferred.await()
                val followTypeResult = followTypeDeferred.await()

                userPosts = postsDB.filter { postDB ->
                    val matches = postDB.userId == userId
                    if (!matches) {
                        Log.w("UserProfileScreen", "⚠️ Post ${postDB.id} tiene user_id=${postDB.userId}, pero el perfil es $userId. FILTRADO.")
                    }
                    matches
                }.map { postDB ->
                    Post.fromDB(
                        postDB = postDB,
                        username = user.username,
                        avatarUrl = user.avatarUrl ?: "",
                        storeName = user.nombreTienda
                    )
                }.sortedByDescending { it.createdAt }
                isLoadingPosts = false

                seguidoresCount = followers
                clientesCount = clients
                reputacionCalc = rep

                profileVisibility = privacy?.profileVisibility ?: "public"
                currentFollowType = followTypeResult

                isContentRestricted = when (profileVisibility) {
                    "private" -> followTypeResult == FollowType.NONE || followTypeResult == FollowType.FOLLOWER_PENDING || followTypeResult == FollowType.CLIENT_PENDING
                    "followers" -> followTypeResult == FollowType.NONE
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e("UserProfileScreen", "Error loading profile: ${e.message}", e)
            // No mostrar toast de error - solo log
        } finally {
            isLoading = false
        }
    }
    
    Box(modifier = modifier.fillMaxSize().background(HomeBg).statusBarsPadding()) {
        when {
            // Estado de carga - mostrar skeleton
            isLoading -> {
                ProfileScreenSkeleton(
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Usuario no encontrado
            userProfile == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Usuario no encontrado",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onBack) {
                            Text("Volver", color = IconAccentBlue)
                        }
                    }
                }
            }
            // Usuario encontrado - mostrar perfil con LazyColumn para scroll fluido y tabs sticky
            else -> {
            val userHighlights = emptyList<HighlightedStory>()
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg)
            ) {
                // Header compacto arriba del banner [← @username 🔔]
                item {
                    ProfileCompactHeader(
                        username = userProfile!!.username,
                        isVerified = userProfile!!.isVerified,
                        reputacion = reputacionCalc,
                        onBackClick = onBack,
                        onNotificationsClick = { showNotificationsModal = true }
                    )
                }
                
                // Header con avatar y stats
                item {
                    val otherUserShape = com.vinzay.app.data.model.AvatarShapeType.fromDbValue(userProfile!!.avatarShape).toShape()
                    UserProfileHeader(
                        user = userProfile!!,
                        postCount = userPosts.size,
                        seguidores = seguidoresCount,
                        clientes = clientesCount,
                        reputacion = reputacionCalc,
                        onBack = onBack,
                        avatarShape = otherUserShape,
                        onFollowersClick = { showFollowersList = true }
                    )
                }
                
                // Botones de acción: Seguir + Mensaje
                item {
                    UserProfileActions(
                        followType = currentFollowType,
                        isPrivate = profileVisibility == "private",
                        onFollow = { showFollowSheet = true },
                        onMessage = {
                            userProfile?.let { user ->
                                onOpenChat(user)
                            }
                        },
                        onMore = { showProfileSettingsModal = true }
                    )
                }
                
                // Highlights del usuario (sin botón de agregar)
                if (userHighlights.isNotEmpty()) {
                    item {
                        HighlightedStories(
                            stories = userHighlights,
                            onStoryPress = { /* TODO: Ver highlight */ },
                            onAddStory = { },
                            canAddStories = false,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                
                // Tabs STICKY - se pegan arriba al hacer scroll
                stickyHeader {
                    UserProfileTabs(
                        tabs = userProfileTabs,
                        selectedIndex = pagerState.currentPage,
                        onTabSelected = { index ->
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        showTopSeparator = userHighlights.isNotEmpty()
                    )
                }
                
                // Contenido de tabs - altura dinámica basada en contenido
                item {
                    val flingBehavior = PagerDefaults.flingBehavior(
                        state = pagerState,
                        pagerSnapDistance = PagerSnapDistance.atMost(1),
                        snapAnimationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    
                    // Calcular altura basada en número de posts (3 columnas, ~120dp por fila)
                    val gridRowHeight = 130.dp
                    val rowCount = ((userPosts.size + 2) / 3).coerceAtLeast(3)
                    val contentHeight = (gridRowHeight * rowCount).coerceAtLeast(400.dp)
                    
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(contentHeight),
                        beyondViewportPageCount = 1,
                        flingBehavior = flingBehavior,
                        key = { it }
                    ) { page ->
                        // Cada página tiene su propio scroll vertical
                        val tabScrollState = rememberScrollState()
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(tabScrollState)
                        ) {
                            if (isContentRestricted) {
                                // Locked profile content
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        val isPending = currentFollowType == FollowType.FOLLOWER_PENDING
                                        Text(
                                            text = if (profileVisibility == "private") "Perfil privado" else "Solo seguidores",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = when {
                                                profileVisibility == "private" && isPending -> "Solicitud enviada. Espera a que el usuario acepte tu solicitud."
                                                profileVisibility == "private" -> "Sigue a este usuario y espera a ser aceptado para ver su contenido."
                                                else -> "Sigue a este usuario para ver sus publicaciones."
                                            },
                                            fontSize = 14.sp,
                                            color = TextMuted,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            } else {
                            when (page) {
                                0 -> if (isLoadingPosts && userPosts.isEmpty()) {
                                    ProfileGridSkeleton(itemCount = 9)
                                } else {
                                    UserPostsGrid(posts = userPosts, onPostClick = onPostClick)
                                }
                                1 -> UserRendsGrid(posts = userPosts)
                                2 -> UserDetailsSection(user = userProfile!!)
                            }
                            }
                            
                            // Spacer para NavBar bottom
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            }
        }
        
        // Follow Bottom Sheet Modal
        FollowBottomSheet(
            isVisible = showFollowSheet,
            username = userProfile?.username ?: "",
            currentFollowType = currentFollowType,
            isLoading = isClientLoading,
            onDismiss = { showFollowSheet = false },
            onFollow = {
                // Acción rápida sin spinner - cerrar sheet inmediatamente
                showFollowSheet = false
                scope.launch {
                    val isPrivate = profileVisibility == "private"
                    val result = if (isPrivate) {
                        FollowersRepository.requestFollow(userId)
                    } else {
                        FollowersRepository.follow(userId)
                    }
                    if (result.isSuccess) {
                        currentFollowType = if (isPrivate) FollowType.FOLLOWER_PENDING else FollowType.FOLLOWER
                        if (!isPrivate) {
                            seguidoresCount++
                            NotificationRepository.createFollowNotification(recipientId = userId)
                        }
                        if (isPrivate) {
                            NotificationRepository.createFollowRequestNotification(
                                profileOwnerId = userId
                            )
                            Toast.makeText(context, "Solicitud de seguimiento enviada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ahora sigues a @${userProfile?.username}", Toast.LENGTH_SHORT).show()
                        }
                        // Recargar estado de restricción
                        isContentRestricted = when (profileVisibility) {
                            "private" -> currentFollowType == FollowType.NONE || currentFollowType == FollowType.FOLLOWER_PENDING || currentFollowType == FollowType.CLIENT_PENDING
                            "followers" -> currentFollowType == FollowType.NONE
                            else -> false
                        }
                    } else {
                        Toast.makeText(context, if (isPrivate) "Error al enviar solicitud" else "Error al seguir", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onRequestClient = {
                scope.launch {
                    isClientLoading = true
                    val wasFollower = currentFollowType == FollowType.FOLLOWER
                    val wasNone = currentFollowType == FollowType.NONE
                    
                    // 1. Registrar en followers como solicitud de cliente
                    val result = FollowersRepository.requestClient(userId)
                    if (result.isSuccess) {
                        // 2. Enviar solicitud via chat
                        ChatRepository.sendClientRequest(
                            sellerId = userId,
                            sellerUsername = userProfile?.username ?: ""
                        )
                        // 3. Actualizar UI
                        currentFollowType = FollowType.CLIENT_PENDING
                        // Si era NONE, ahora estoy en la relación (pero como cliente pendiente, no seguidor)
                        // Los clientes pendientes NO cuentan como seguidores
                        if (wasNone) {
                            // No incrementar seguidores - los clientes pendientes no van a seguidores
                        }
                        if (wasFollower) {
                            // Ya no soy seguidor normal, ahora soy cliente pendiente
                            seguidoresCount = maxOf(0, seguidoresCount - 1)
                        }
                        Toast.makeText(context, "Solicitud de cliente enviada ⏳", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
                    }
                    isClientLoading = false
                    showFollowSheet = false
                }
            },
            onUnfollow = {
                // Acción rápida sin spinner
                showFollowSheet = false
                scope.launch {
                    val result = FollowersRepository.unfollow(userId)
                    if (result.isSuccess) {
                        val wasClient = currentFollowType == FollowType.CLIENT || currentFollowType == FollowType.CLIENT_PENDING
                        val wasFollower = currentFollowType == FollowType.FOLLOWER
                        currentFollowType = FollowType.NONE
                        if (wasFollower) seguidoresCount = maxOf(0, seguidoresCount - 1)
                        if (wasClient) clientesCount = maxOf(0, clientesCount - 1)
                        // Recargar restricción de contenido
                        isContentRestricted = when (profileVisibility) {
                            "private" -> true
                            "followers" -> true
                            else -> false
                        }
                        Toast.makeText(context, "Solicitud cancelada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al cancelar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        
        // Modal de notificaciones
        NotificationsSettingsModal(
            isVisible = showNotificationsModal,
            username = userProfile?.username ?: "",
            onDismiss = { showNotificationsModal = false }
        )
        
        // Modal de opciones del perfil
        UserProfileSettingsModal(
            isVisible = showProfileSettingsModal,
            username = userProfile?.username ?: "",
            isBlocked = isUserBlocked,
            isMuted = isUserMuted,
            onDismiss = { showProfileSettingsModal = false },
            onBlock = {
                scope.launch {
                    try {
                        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                        if (currentUserId != null) {
                            SupabaseClient.database
                                .from("blocked_users")
                                .insert(mapOf(
                                    "blocker_id" to currentUserId,
                                    "blocked_id" to userId
                                ))
                            isUserBlocked = true
                            Toast.makeText(context, "@${userProfile?.username} bloqueado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("UserProfileScreen", "Error al bloquear: ${e.message}")
                    }
                }
                showProfileSettingsModal = false
            },
            onUnblock = {
                scope.launch {
                    try {
                        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                        if (currentUserId != null) {
                            SupabaseClient.database
                                .from("blocked_users")
                                .delete {
                                    filter {
                                        eq("blocker_id", currentUserId)
                                        eq("blocked_id", userId)
                                    }
                                }
                            isUserBlocked = false
                            Toast.makeText(context, "@${userProfile?.username} desbloqueado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("UserProfileScreen", "Error al desbloquear: ${e.message}")
                    }
                }
                showProfileSettingsModal = false
            },
            onMute = {
                scope.launch {
                    try {
                        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                        if (currentUserId != null) {
                            SupabaseClient.database
                                .from("muted_users")
                                .insert(mapOf(
                                    "muter_id" to currentUserId,
                                    "muted_id" to userId
                                ))
                            isUserMuted = true
                            Toast.makeText(context, "@${userProfile?.username} silenciado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("UserProfileScreen", "Error al silenciar: ${e.message}")
                    }
                }
                showProfileSettingsModal = false
            },
            onUnmute = {
                scope.launch {
                    try {
                        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                        if (currentUserId != null) {
                            SupabaseClient.database
                                .from("muted_users")
                                .delete {
                                    filter {
                                        eq("muter_id", currentUserId)
                                        eq("muted_id", userId)
                                    }
                                }
                            isUserMuted = false
                            Toast.makeText(context, "@${userProfile?.username} ya no está silenciado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("UserProfileScreen", "Error al dejar de silenciar: ${e.message}")
                    }
                }
                showProfileSettingsModal = false
            },
            onReport = {
                showProfileSettingsModal = false
                showReportModal = true
            },
            onShareProfile = {
                Toast.makeText(context, "Compartir perfil de @${userProfile?.username}", Toast.LENGTH_SHORT).show()
                showProfileSettingsModal = false
            },
            onCopyProfileLink = {
                Toast.makeText(context, "Enlace copiado", Toast.LENGTH_SHORT).show()
                showProfileSettingsModal = false
            }
        )
        
        // Modal de reporte de usuario
        ReportModal(
            isVisible = showReportModal,
            contentType = "user",
            contentId = userId,
            username = userProfile?.username ?: "",
            onDismiss = { showReportModal = false },
            onSubmitReport = { reason, description ->
                scope.launch {
                    try {
                        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                        if (currentUserId != null) {
                            SupabaseClient.database
                                .from("content_reports")
                                .insert(mapOf(
                                    "reporter_id" to currentUserId,
                                    "content_type" to "user",
                                    "content_id" to userId,
                                    "reported_user_id" to userId,
                                    "reason" to reason,
                                    "description" to description
                                ))
                        }
                    } catch (e: Exception) {
                        Log.e("UserProfileScreen", "Error al reportar: ${e.message}")
                    }
                }
            }
        )
        
        // Modal de seguidores/clientes
        FollowersListSheet(
            isVisible = showFollowersList,
            userId = userId,
            seguidoresCount = seguidoresCount,
            clientesCount = clientesCount,
            onDismiss = { showFollowersList = false },
            onUserClick = { clickedUserId ->
                showFollowersList = false
            }
        )
    }
}

@Composable
private fun UserProfileHeader(
    user: Usuario,
    postCount: Int,
    seguidores: Int,
    clientes: Int,
    reputacion: Int,
    onBack: () -> Unit,
    avatarShape: androidx.compose.ui.graphics.Shape = CircleShape,
    onFollowersClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeBg)
    ) {
        // Banner - sin botón de volver duplicado (ya está en el header compacto)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(130.dp) // Sin padding top extra
                .clip(RoundedCornerShape(20.dp))
        ) {
            // Mostrar imagen del banner si existe, sino mostrar gradiente
            if (!user.bannerUrl.isNullOrBlank()) {
                android.util.Log.d("UserProfileScreen", "Intentando cargar Banner: ${user.bannerUrl}")
                AsyncImage(
                    model = user.bannerUrl,
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onSuccess = { android.util.Log.d("UserProfileScreen", "✅ Banner cargado con éxito") },
                    onError = {
                        android.util.Log.e("UserProfileScreen", "Error cargando banner de ${user.username}: ${user.bannerUrl}")
                        android.util.Log.e("UserProfileScreen", "Causa: ${it.result.throwable.message}")
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
            
            // Gradient overlay (sin botón de volver - ya está en header)
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
        
        // Avatar y stats
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
                
                // Avatar con halo condicional (cercano)
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Cargar stories del usuario para mostrar halo
                    
                    // Avatar centrado (gap ~4dp al halo)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(avatarShape)
                            .background(HomeBg)
                    ) {
                        val avatarToLoad = if (!user.avatarUrl.isNullOrBlank()) {
                            user.avatarUrl
                        } else {
                            "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff"
                        }
                        
                        android.util.Log.d("UserProfileScreen", "Intentando cargar Avatar: $avatarToLoad")
                        AsyncImage(
                            model = avatarToLoad,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(avatarShape),
                            onSuccess = { android.util.Log.d("UserProfileScreen", "✅ Avatar cargado con éxito") },
                            onError = {
                                android.util.Log.e("UserProfileScreen", "Error cargando avatar de ${user.username}: $avatarToLoad")
                                android.util.Log.e("UserProfileScreen", "Causa: ${it.result.throwable.message}")
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(17.dp))
                
                // Stats - Publicaciones, Seguidores, Clientes (igual que ProfileScreen)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        UserProfileStat(count = postCount, label = "Publicaciones")
                        UserProfileStat(count = seguidores, label = "Seguidores", onClick = onFollowersClick)
                        UserProfileStat(count = clientes, label = "Clientes", onClick = onFollowersClick)
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Nombre + Badge de verificación (sin badge de reputación - ahora está en el header)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.nombreTienda ?: user.nombre ?: user.username,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Badge verificado si aplica
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    com.vinzay.app.ui.components.VerifiedBadge(size = 14.dp)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Username
            Text(
                text = "@${user.username}",
                color = TextMuted,
                fontSize = 14.sp
            )
            
            // Bio
            user.descripcion?.let { bio ->
                if (bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp)) // Igual que ProfileScreen
                    Text(
                        text = bio,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileActions(
    followType: FollowType,
    isPrivate: Boolean = false,
    onFollow: () -> Unit,
    onMessage: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-12).dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Botón Seguir/Estado - ocupa todo el ancho si es privado y no siguiendo
        Button(
            onClick = onFollow,
            modifier = Modifier.weight(if (isPrivate && followType == FollowType.NONE) 2f else 1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (followType) {
                    FollowType.NONE -> Color(0xFF2E8B57)
                    FollowType.FOLLOWER -> Surface
                    FollowType.FOLLOWER_PENDING -> Color(0xFFFFA726).copy(alpha = 0.15f)
                    FollowType.CLIENT_PENDING -> Color(0xFFFFA726).copy(alpha = 0.15f)
                    FollowType.CLIENT -> Color(0xFF2E8B57).copy(alpha = 0.15f)
                }
            ),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(
                imageVector = when (followType) {
                    FollowType.NONE -> Icons.Outlined.PersonAdd
                    FollowType.FOLLOWER -> Icons.Filled.Check
                    FollowType.FOLLOWER_PENDING -> Icons.Outlined.Schedule
                    FollowType.CLIENT_PENDING -> Icons.Outlined.Schedule
                    FollowType.CLIENT -> Icons.Filled.Verified
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when (followType) {
                    FollowType.NONE -> Color.White
                    FollowType.FOLLOWER -> TextPrimary
                    FollowType.FOLLOWER_PENDING -> Color(0xFFFFA726)
                    FollowType.CLIENT_PENDING -> Color(0xFFFFA726)
                    FollowType.CLIENT -> Color(0xFF2E8B57)
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (followType) {
                    FollowType.NONE -> "Seguir"
                    FollowType.FOLLOWER -> "Siguiendo"
                    FollowType.FOLLOWER_PENDING -> "Solicitado"
                    FollowType.CLIENT_PENDING -> "Pendiente"
                    FollowType.CLIENT -> "Cliente"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (followType) {
                    FollowType.NONE -> Color.White
                    FollowType.FOLLOWER -> TextPrimary
                    FollowType.FOLLOWER_PENDING -> Color(0xFFFFA726)
                    FollowType.CLIENT_PENDING -> Color(0xFFFFA726)
                    FollowType.CLIENT -> Color(0xFF2E8B57)
                }
            )
        }

        // Botón Mensaje - SOLO visible si no es perfil privado
        if (!isPrivate) {
            Button(
                onClick = onMessage,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Surface),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mensaje",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
        
        // Botón Más opciones
        Button(
            onClick = onMore,
            colors = ButtonDefaults.buttonColors(containerColor = Surface),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Más opciones",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UserProfileTabs(
    tabs: List<UserProfileTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    showTopSeparator: Boolean = true
) {
    Column {
        // Línea separadora sutil sobre las tabs - solo si hay highlights
        if (showTopSeparator) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(0.5.dp)
                    .background(TextMuted.copy(alpha = 0.2f))
            )
        }
        
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

// Función para formatear números igual que en ProfileScreen
private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

@Composable
private fun UserProfileStat(count: Int, label: String, onClick: (() -> Unit)? = null) {
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
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun UserProfileStatPercent(percent: Int, label: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "$percent%",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun UserPostsGrid(
    posts: List<Post>,
    onPostClick: (Post) -> Unit
) {
    // Grilla NO scrollable - el scroll viene del Column padre con verticalScroll
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sin publicaciones",
                    color = TextMuted,
                    fontSize = 16.sp
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
            posts.chunked(3).forEach { rowPosts ->
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
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            if (post.images.size > 1) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                    // Rellenar espacios vacíos
                    repeat(3 - rowPosts.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRendsGrid(posts: List<Post>) {
    // Grilla NO scrollable
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
                text = "Sin Rends",
                color = TextMuted,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun UserDetailsSection(user: Usuario) {
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
                text = "Sobre ${user.nombre ?: user.username}",
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
                    UserStatColumn(
                        icon = Icons.Outlined.People,
                        value = "0",
                        label = "Clientes",
                        color = Color(0xFF2E8B57)
                    )
                    UserStatDivider()
                    UserStatColumn(
                        icon = Icons.Outlined.Star,
                        value = "4.8",
                        label = "Valoración",
                        color = Color(0xFFFF6B35)
                    )
                    UserStatDivider()
                    UserStatColumn(
                        icon = Icons.Outlined.Speed,
                        value = "< 1h",
                        label = "Respuesta",
                        color = Color(0xFF1565A0)
                    )
                }
            }
        }
        
        // Indicador de confianza mejorado
        if (user.isVerified) {
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
                            text = "Verificado • Transacciones seguras",
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
                UserInfoRow(
                    icon = Icons.Outlined.Person,
                    iconColor = IconAccentBlue,
                    label = "Usuario",
                    value = "@${user.username}",
                    showDivider = true
                )
                user.ubicacion?.let { ubicacion ->
                    if (ubicacion.isNotEmpty()) {
                        UserInfoRow(
                            icon = Icons.Outlined.LocationOn,
                            iconColor = Color(0xFF2E8B57),
                            label = "Ubicación",
                            value = ubicacion,
                            showDivider = true
                        )
                    }
                }
                UserInfoRow(
                    icon = Icons.Outlined.CalendarMonth,
                    iconColor = Color(0xFF60A5FA),
                    label = "Miembro desde",
                    value = user.fechaRegistro?.take(10) ?: "2024",
                    showDivider = user.tieneTienda == true
                )
                if (user.tieneTienda == true) {
                    UserInfoRow(
                        icon = Icons.Outlined.Storefront,
                        iconColor = Color(0xFFFF6B35),
                        label = "Tienda",
                        value = user.nombreTienda ?: "Tienda verificada",
                        showDivider = false
                    )
                }
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
            UserPaymentChip(
                icon = Icons.Outlined.CreditCard,
                label = "Tarjeta",
                color = Color(0xFF1565A0)
            )
            UserPaymentChip(
                icon = Icons.Outlined.AccountBalance,
                label = "Transferencia",
                color = Color(0xFF2E8B57)
            )
            UserPaymentChip(
                icon = Icons.Outlined.Payments,
                label = "Efectivo",
                color = Color(0xFFFF6B35)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UserStatColumn(
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
private fun UserStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(50.dp)
            .background(TextMuted.copy(alpha = 0.15f))
    )
}

@Composable
private fun UserInfoRow(
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
private fun UserPaymentChip(
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
private fun ProfileCompactHeader(
    username: String,
    isVerified: Boolean,
    reputacion: Int = 0,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back button + Username + Reputación
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "@$username",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
        }
        
        // Notifications button
        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notificaciones",
                tint = TextPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
