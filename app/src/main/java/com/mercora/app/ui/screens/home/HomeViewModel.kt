package com.mercora.app.ui.screens.home

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.model.Post
import com.mercora.app.data.model.PostDB
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.NotificationRepository
import com.mercora.app.data.repository.PostRepository
import com.mercora.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Entrada del feed interleaved (posts + slots de rends en posiciones fijas).
 * Pre-computada en el ViewModel para que la composiciÃ³n no recalcule nada.
 * @Immutable: Compose puede skipear recomposiciones cuando la lista no cambia.
 */
@Immutable
data class FeedEntry(val type: String, val id: String, val postIndex: Int = -1, val rendIndex: Int = -1)

/**
 * Modelo de UI del feed listo para renderizar: se computa en Dispatchers.Default
 * una sola vez por emisiÃ³n de posts (no en cada recomposiciÃ³n del Home).
 * @Immutable: Compose puede skipear recomposiciones cuando el feedUi no cambia.
 */
@Immutable
data class HomeFeedUi(
    val firstThreePosts: List<Post> = emptyList(),
    val remainingPosts: List<Post> = emptyList(),
    val entries: List<FeedEntry> = emptyList()
)

@Serializable
data class PostLikeDB(
    val id: String = "",
    @kotlinx.serialization.SerialName("user_id") val userId: String = "",
    @kotlinx.serialization.SerialName("post_id") val postId: String = "",
    @kotlinx.serialization.SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PostSaveDB(
    val id: String = "",
    @kotlinx.serialization.SerialName("user_id") val userId: String = "",
    @kotlinx.serialization.SerialName("post_id") val postId: String = "",
    @kotlinx.serialization.SerialName("created_at") val createdAt: String? = null
)

@Serializable
private data class PostCountData(
    @kotlinx.serialization.SerialName("likes_count") val likesCount: Int = 0,
    @kotlinx.serialization.SerialName("reviews_count") val reviewsCount: Int = 0,
    @kotlinx.serialization.SerialName("views_count") val viewsCount: Int = 0,
    @kotlinx.serialization.SerialName("shares_count") val sharesCount: Int = 0
)

@Serializable
private data class CommentForCount(
    val id: String = "",
    @kotlinx.serialization.SerialName("product_id") val productId: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    
    private val _allPosts = MutableStateFlow<List<Post>>(emptyList()) // Todos los posts cacheados
    private val _visiblePosts = MutableStateFlow<List<Post>>(emptyList()) // Posts visibles (paginados)
    val posts: StateFlow<List<Post>> = _visiblePosts.asStateFlow()

    // Feed pre-computado (distinctBy + interleaving) fuera del Main thread:
    // la composiciÃ³n del Home consume esto directamente sin recalcular
    val feedUi: StateFlow<HomeFeedUi> = _visiblePosts
        .map { posts -> buildFeedUi(posts) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeFeedUi())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // UI STATE COMBINADO: Reduce de 6+ collectAsState a 1 solo.
    // Cada collectAsState crea una subscription independiente â†’ overhead por frame.
    // Con combine, solo 1 subscription emite cuando CUALQUIER campo cambia.
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    data class HomeFeedState(
        val posts: List<Post> = emptyList(),
        val isLoading: Boolean = true,
        val isLoadingMore: Boolean = false,
        val hasMorePosts: Boolean = true,
        val currentUser: Usuario? = null,
        val errorMessage: String? = null
    )
    
    // FunciÃ³n genÃ©rica para actualizar contadores en posts
    private suspend fun updatePostCount(postId: String, countField: String, increment: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                val postData = SupabaseClient.database
                    .from("posts")
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.list(countField)) {
                        filter { eq("id", postId) }
                    }
                    .decodeSingleOrNull<Map<String, Int>>()
                
                val currentCount = postData?.get(countField) ?: 0
                val newCount = if (increment) currentCount + 1 else maxOf(0, currentCount - 1)
                
                SupabaseClient.database
                    .from("posts")
                    .update(kotlinx.serialization.json.buildJsonObject {
                        put(countField, kotlinx.serialization.json.JsonPrimitive(newCount))
                    }) {
                        filter { eq("id", postId) }
                    }
                
                Log.d("HomeViewModel", "$countField actualizado: $currentCount â†’ $newCount")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error actualizando $countField: ${e.message}", e)
            }
        }
    }
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _hasMorePosts = MutableStateFlow(true)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()
    
    // PaginaciÃ³n REAL server-side: cada pÃ¡gina trae posts + datos asociados
    // en batch desde Supabase (antes se descargaba TODO al inicio)
    private val PAGE_SIZE = 10
    
    private val _currentRoute = MutableStateFlow("home")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()
    
    // Usuario de sesiÃ³n actual
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()
    
    // Estado de error visible para el usuario
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // FeedState combinado: 1 solo StateFlow que emite cuando cualquier campo cambia
    val feedState: StateFlow<HomeFeedState> = combine(
        _visiblePosts,
        _isLoading,
        _isLoadingMore,
        _hasMorePosts,
        _currentUser,
        _errorMessage
    ) { values ->
        HomeFeedState(
            posts = values[0] as List<Post>,
            isLoading = values[1] as Boolean,
            isLoadingMore = values[2] as Boolean,
            hasMorePosts = values[3] as Boolean,
            currentUser = values[4] as Usuario?,
            errorMessage = values[5] as String?
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeFeedState())
    
    fun clearError() {
        _errorMessage.value = null
    }

    // RÃ©plica exacta del layout que antes computaba HomeScreen en composiciÃ³n:
    // primeros 3 posts + feed interleaved con slots de rends fijos (3, 7, 11...)
    private fun buildFeedUi(posts: List<Post>): HomeFeedUi {
        val uniquePosts = posts.distinctBy { it.id }
        val firstThree = uniquePosts.take(3)
        val remaining = if (uniquePosts.size > 3) uniquePosts.drop(3) else emptyList()

        val entries = mutableListOf<FeedEntry>()
        val maxRendSlots = 5
        var rendSlotIdx = 0
        remaining.forEachIndexed { index, post ->
            if (index > 0 && index % 4 == 3 && rendSlotIdx < maxRendSlots) {
                entries.add(FeedEntry("rend", "feed_rend_slot_$rendSlotIdx", rendIndex = rendSlotIdx))
                rendSlotIdx++
            }
            entries.add(FeedEntry("post", post.id, postIndex = index))
        }
        return HomeFeedUi(firstThree, remaining, entries.toList())
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // ACCIONES DE MODERACIÃ“N: extraÃ­das de HomeScreen para que los items
    // del feed no capturen scope/context en lambdas inline gigantes
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    fun muteUser(mutedUserId: String, username: String) {
        viewModelScope.launch {
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                SupabaseClient.database
                    .from("muted_users")
                    .insert(mapOf("muter_id" to currentUserId, "muted_id" to mutedUserId))
                _errorMessage.value = "@$username silenciado"
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al silenciar: ${e.message}")
            }
        }
    }

    fun unfollowUser(followedUserId: String, username: String) {
        viewModelScope.launch {
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                SupabaseClient.database
                    .from("followers")
                    .delete {
                        filter {
                            eq("follower_id", currentUserId)
                            eq("followed_id", followedUserId)
                        }
                    }
                _errorMessage.value = "Dejaste de seguir a @$username"
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al dejar de seguir: ${e.message}")
            }
        }
    }

    fun unhidePost(postId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                SupabaseClient.database
                    .from("hidden_posts")
                    .delete {
                        filter {
                            eq("user_id", currentUserId)
                            eq("post_id", postId)
                        }
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al deshacer: ${e.message}")
            }
        }
    }
    
    // Cache de usuarios para evitar queries repetidas
    private val usersCache = mutableMapOf<String, Usuario>()

    // Likes/saves del usuario actual: se cargan UNA vez y se reusan por pÃ¡gina
    private var userLikes: Set<String>? = null
    private var userSaves: Set<String>? = null

    // CachÃ© de privacy settings (show_likes) por dueÃ±o de post
    private val privacyCache = mutableMapOf<String, Boolean>()

    init {
        loadCurrentUser()
        loadInitialPosts()
        
        // Escuchar cambios del PostRepository (nuevos posts)
        viewModelScope.launch {
            PostRepository.posts.collect { repoPosts ->
                if (repoPosts.isNotEmpty()) {
                    Log.d("HomeViewModel", "PostRepository actualizÃ³: ${repoPosts.size} posts")
                    
                    // Buscar nuevos userIds que no tengamos en cache
                    val newUserIds = repoPosts.map { it.userId }.distinct().filter { it !in usersCache }
                    if (newUserIds.isNotEmpty()) {
                        try {
                            // Batch server-side: solo los usuarios faltantes (antes
                            // se descargaba la tabla entera y se filtraba en cliente)
                            val newUsers = SupabaseClient.database
                                .from("usuarios")
                                .select { filter { isIn("user_id", newUserIds) } }
                                .decodeList<Usuario>()
                            newUsers.forEach { usersCache[it.userId] = it }
                            Log.d("HomeViewModel", "Cargados ${newUsers.size} usuarios nuevos para posts")
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error cargando usuarios nuevos: ${e.message}")
                            _errorMessage.value = "Error cargando datos de usuarios: ${e.message}"
                        }
                    }

                    // OPTIMIZADO: Mover transformaciÃ³n a Default dispatcher
                    // Preserva isLiked/isSaved de la lista actual (el repo no los trae)
                    val currentById = _visiblePosts.value.associateBy { it.id }
                    val postsWithUserData = withContext(Dispatchers.Default) {
                        repoPosts.map { post ->
                            val cachedUser = usersCache[post.userId]
                            val enriched = if (cachedUser != null && (post.username == "usuario" || post.userAvatar.isEmpty())) {
                                post.copy(
                                    username = cachedUser.username,
                                    userAvatar = cachedUser.avatarUrl ?: "",
                                    userStoreName = cachedUser.nombreTienda
                                )
                            } else {
                                post
                            }
                            val existing = currentById[enriched.id]
                            if (existing != null) {
                                enriched.copy(isLiked = existing.isLiked, isSaved = existing.isSaved)
                            } else {
                                enriched
                            }
                        }.sortedByDescending { it.createdAt }
                    }

                    _allPosts.value = postsWithUserData
                    val currentVisible = _visiblePosts.value.size.coerceAtLeast(PAGE_SIZE)
                    _visiblePosts.value = postsWithUserData.take(currentVisible)
                    _hasMorePosts.value = _visiblePosts.value.size < _allPosts.value.size
                }
            }
        }
        
        // Escuchar cambios del ProfileRepository para sincronizar avatar
        viewModelScope.launch {
            ProfileRepository.currentProfile.collect { profile ->
                if (profile != null) {
                    // Actualizar currentUser con el avatar del perfil
                    _currentUser.value = _currentUser.value?.copy(
                        avatarUrl = profile.avatarUrl,
                        username = profile.username,
                        nombre = profile.nombre,
                        nombreTienda = profile.nombreTienda
                    )
                    Log.d("HomeViewModel", "Perfil sincronizado: avatar=${profile.avatarUrl}")
                }
            }
        }
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val authUser = SupabaseClient.auth.currentUserOrNull()
                if (authUser != null) {
                    val usuario = SupabaseClient.database
                        .from("usuarios")
                        .select()
                        .decodeList<Usuario>()
                        .firstOrNull { it.userId == authUser.id }
                    _currentUser.value = usuario
                    Log.d("HomeViewModel", "Current user: ${usuario?.username}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading current user", e)
            }
        }
    }
    
    private fun loadInitialPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Primera pÃ¡gina REAL desde el servidor (posts + datos en batch)
                val firstPage = fetchPostsPage(0)
                _allPosts.value = firstPage
                _visiblePosts.value = firstPage
                _hasMorePosts.value = firstPage.size == PAGE_SIZE
                Log.d("HomeViewModel", "PÃ¡gina inicial: ${firstPage.size} posts")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading posts: ${e.message}", e)
                _errorMessage.value = "Error cargando posts: ${e.message}"
                _visiblePosts.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    // Likes/saves del usuario actual: una sola query cada uno por sesiÃ³n,
    // reusadas por todas las pÃ¡ginas siguientes
    private suspend fun ensureUserInteractionsLoaded() {
        if (userLikes != null && userSaves != null) return
        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
        if (currentUserId == null) {
            userLikes = emptySet()
            userSaves = emptySet()
            return
        }
        coroutineScope {
            val likesDeferred = async {
                try {
                    SupabaseClient.database
                        .from("post_likes")
                        .select { filter { eq("user_id", currentUserId) } }
                        .decodeList<PostLikeDB>()
                        .map { it.postId }
                        .toSet()
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading likes: ${e.message}")
                    emptySet()
                }
            }
            val savesDeferred = async {
                try {
                    SupabaseClient.database
                        .from("post_saves")
                        .select { filter { eq("user_id", currentUserId) } }
                        .decodeList<PostSaveDB>()
                        .map { it.postId }
                        .toSet()
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading saves: ${e.message}")
                    emptySet()
                }
            }
            userLikes = likesDeferred.await()
            userSaves = savesDeferred.await()
        }
    }

    /**
     * PAGINACIÃ“N REAL: trae UNA pÃ¡gina de posts del servidor (order + range)
     * junto con sus datos asociados en batch: usuarios faltantes (isIn),
     * reviews de la pÃ¡gina (isIn) y privacy settings cacheados por usuario.
     * Antes se descargaba la tabla completa de posts/usuarios al inicio.
     */
    private suspend fun fetchPostsPage(offset: Int): List<Post> = withContext(Dispatchers.IO) {
        val postsDB = SupabaseClient.database
            .from("posts")
            .select(columns = io.github.jan.supabase.postgrest.query.Columns.list(
                "id", "user_id", "title", "description", "price", "previous_price",
                "category", "condition", "images", "likes_count", "reviews_count",
                "saves_count", "shares_count", "created_at", "product_id",
                "warranty", "returns_accepted", "colors", "allow_offers",
                "free_shipping", "stock"
            )) {
                order("created_at", Order.DESCENDING)
                range(offset.toLong(), (offset + PAGE_SIZE - 1).toLong())
            }
            .decodeList<PostDB>()
        if (postsDB.isEmpty()) return@withContext emptyList()

        ensureUserInteractionsLoaded()

        // Usuarios de la pÃ¡gina que no estÃ©n cacheados (batch server-side)
        val userIds = postsDB.map { it.userId }.distinct()
        val missingUserIds = userIds.filter { it !in usersCache }
        if (missingUserIds.isNotEmpty()) {
            try {
                SupabaseClient.database
                    .from("usuarios")
                    .select { filter { isIn("user_id", missingUserIds) } }
                    .decodeList<Usuario>()
                    .forEach { usersCache[it.userId] = it }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading users: ${e.message}")
            }
        }

        // Conteo de reviews de los productos de esta pÃ¡gina
        val reviewsCountMap = mutableMapOf<String, Int>()
        try {
            val productIds = postsDB.mapNotNull { it.productId }.distinct()
            if (productIds.isNotEmpty()) {
                SupabaseClient.database
                    .from("product_reviews")
                    .select {
                        filter {
                            isIn("product_id", productIds)
                            exact("parent_id", null)
                        }
                    }
                    .decodeList<CommentForCount>()
                    .forEach { comment ->
                        comment.productId?.let { pid ->
                            reviewsCountMap[pid] = (reviewsCountMap[pid] ?: 0) + 1
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error loading review counts: ${e.message}")
        }

        // Privacy settings (show_likes) de los dueÃ±os de la pÃ¡gina, batch loading
        val newUserIds = userIds.filter { it !in privacyCache }
        if (newUserIds.isNotEmpty()) {
            try {
                val prefsList = com.vinzay.app.data.repository.UserPreferencesRepository
                    .loadPrivacySettingsBatch(newUserIds)
                prefsList.forEach { (uid, settings) ->
                    privacyCache[uid] = settings?.showLikes ?: true
                }
            } catch (_: Exception) {
                // En caso de error, asumir showLikes = true para todos los nuevos
                newUserIds.forEach { uid -> privacyCache[uid] = true }
            }
        }

        val likes = userLikes ?: emptySet()
        val saves = userSaves ?: emptySet()
        postsDB.map { postDB ->
            val user = usersCache[postDB.userId]
            val realReviewCount = postDB.productId?.let { reviewsCountMap[it] } ?: 0
            val showLikes = privacyCache[postDB.userId] ?: true
            Post.fromDB(
                postDB = postDB,
                username = user?.username ?: "usuario",
                avatarUrl = user?.avatarUrl ?: "",
                storeName = user?.nombreTienda,
                overrideReviewsCount = realReviewCount,
                isUserVerified = user?.isVerified ?: false
            ).copy(
                isLiked = postDB.id in likes,
                isSaved = postDB.id in saves,
                likesCount = if (showLikes) postDB.likesCount else 0
            )
        }
    }

    fun loadMorePosts() {
        if (_isLoadingMore.value || !_hasMorePosts.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                // Siguiente pÃ¡gina real (offset = posts ya cargados)
                val nextPage = fetchPostsPage(_visiblePosts.value.size)
                if (nextPage.isEmpty()) {
                    _hasMorePosts.value = false
                } else {
                    // distinctBy protege contra corrimiento de offset si se
                    // insertaron posts nuevos entre pÃ¡ginas
                    val merged = (_visiblePosts.value + nextPage).distinctBy { it.id }
                    _allPosts.value = merged
                    _visiblePosts.value = merged
                    _hasMorePosts.value = nextPage.size == PAGE_SIZE
                }
                Log.d("HomeViewModel", "Loaded more: ${_visiblePosts.value.size} posts")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error cargando mÃ¡s posts: ${e.message}")
            }
            _isLoadingMore.value = false
        }
    }
    
    fun toggleLike(postId: String) {
        val currentList = _visiblePosts.value
        val postIndex = currentList.indexOfFirst { it.id == postId }
        if (postIndex == -1) return
        
        val post = currentList[postIndex]
        val wasLiked = post.isLiked
        
        // OPTIMIZADO: Actualizar solo el item especÃ­fico usando toMutableList()
        // Esto es mÃ¡s eficiente que .map{} sobre toda la lista
        val updatedPost = post.copy(
            isLiked = !post.isLiked,
            likesCount = if (post.isLiked) post.likesCount - 1 else post.likesCount + 1
        )
        _visiblePosts.value = currentList.toMutableList().apply {
            set(postIndex, updatedPost)
        }
        
        // Persistir en Supabase
        viewModelScope.launch {
            try {
                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                
                if (!wasLiked) {
                    // Agregar like
                    SupabaseClient.database
                        .from("post_likes")
                        .insert(mapOf("user_id" to userId, "post_id" to postId))
                    
                    // Actualizar likes_count en posts
                    updatePostCount(postId, "likes_count", increment = true)
                    
                    // Crear notificaciÃ³n
                    NotificationRepository.createLikeNotification(
                        recipientId = post.userId,
                        postId = postId,
                        postImage = post.images.firstOrNull()
                    )
                } else {
                    // Quitar like
                    SupabaseClient.database
                        .from("post_likes")
                        .delete {
                            filter {
                                eq("user_id", userId)
                                eq("post_id", postId)
                            }
                        }
                    
                    // Decrementar likes_count en posts
                    updatePostCount(postId, "likes_count", increment = false)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error toggling like: ${e.message}")
            }
        }
    }
    
    fun incrementShareCount(postId: String) {
        viewModelScope.launch {
            updatePostCount(postId, "shares_count", increment = true)
        }
    }
    
    fun incrementViewCount(postId: String) {
        viewModelScope.launch {
            updatePostCount(postId, "views_count", increment = true)
        }
    }
    
    // Actualizar el contador de reviews cuando se agregan o eliminan comentarios
    fun updateReviewsCount(postId: String, increment: Boolean) {
        // Actualizar UI inmediatamente
        val currentList = _visiblePosts.value
        val postIndex = currentList.indexOfFirst { it.id == postId }
        if (postIndex != -1) {
            val post = currentList[postIndex]
            val updatedPost = post.copy(
                reviewsCount = if (increment) post.reviewsCount + 1 else maxOf(0, post.reviewsCount - 1)
            )
            _visiblePosts.value = currentList.toMutableList().apply {
                set(postIndex, updatedPost)
            }
        }
        
        // Actualizar en Supabase
        viewModelScope.launch {
            updatePostCount(postId, "reviews_count", increment)
        }
    }
    
    fun toggleSave(postId: String) {
        val currentList = _visiblePosts.value
        val postIndex = currentList.indexOfFirst { it.id == postId }
        if (postIndex == -1) return
        
        val post = currentList[postIndex]
        val wasSaved = post.isSaved
        
        // OPTIMIZADO: Actualizar solo el item especÃ­fico
        val updatedPost = post.copy(
            isSaved = !post.isSaved,
            savesCount = if (post.isSaved) post.savesCount - 1 else post.savesCount + 1
        )
        _visiblePosts.value = currentList.toMutableList().apply {
            set(postIndex, updatedPost)
        }
        
        // Persistir en Supabase
        viewModelScope.launch {
            try {
                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                
                if (!wasSaved) {
                    // Agregar save
                    SupabaseClient.database
                        .from("post_saves")
                        .insert(mapOf("user_id" to userId, "post_id" to postId))
                    
                    // Actualizar saves_count en posts
                    updatePostCount(postId, "saves_count", increment = true)
                    
                    // Crear notificaciÃ³n
                    NotificationRepository.createSaveNotification(
                        recipientId = post.userId,
                        postId = postId,
                        postImage = post.images.firstOrNull()
                    )
                } else {
                    // Quitar save
                    SupabaseClient.database
                        .from("post_saves")
                        .delete {
                            filter {
                                eq("user_id", userId)
                                eq("post_id", postId)
                            }
                        }
                    
                    // Decrementar saves_count en posts
                    updatePostCount(postId, "saves_count", increment = false)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error toggling save: ${e.message}")
            }
        }
    }
    
    fun toggleStats(postId: String) {
        // OPTIMIZADO: Actualizar solo el item especÃ­fico
        val currentList = _visiblePosts.value
        val postIndex = currentList.indexOfFirst { it.id == postId }
        if (postIndex == -1) return
        
        val post = currentList[postIndex]
        val updatedPost = post.copy(showStats = !post.showStats)
        _visiblePosts.value = currentList.toMutableList().apply {
            set(postIndex, updatedPost)
        }
    }
    
    fun navigateTo(route: String) {
        _currentRoute.value = route
    }
    
    fun removePost(postId: String) {
        _visiblePosts.value = _visiblePosts.value.filter { it.id != postId }
    }
    
    fun refreshPosts() {
        loadInitialPosts()
    }
}
