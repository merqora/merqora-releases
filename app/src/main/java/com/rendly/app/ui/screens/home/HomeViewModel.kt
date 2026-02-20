package com.rendly.app.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendly.app.data.model.Post
import com.rendly.app.data.model.PostDB
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.NotificationRepository
import com.rendly.app.data.repository.PostRepository
import com.rendly.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Función genérica para actualizar contadores en posts
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
                
                Log.d("HomeViewModel", "$countField actualizado: $currentCount → $newCount")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error actualizando $countField: ${e.message}", e)
            }
        }
    }
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _hasMorePosts = MutableStateFlow(true)
    val hasMorePosts: StateFlow<Boolean> = _hasMorePosts.asStateFlow()
    
    // Configuración de paginación
    private val INITIAL_LOAD_COUNT = 1
    private val LOAD_MORE_COUNT = 3
    
    private val _currentRoute = MutableStateFlow("home")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()
    
    // Usuario de sesión actual
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()
    
    // Estado de error visible para el usuario
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    // Cache de usuarios para evitar queries repetidas
    private val usersCache = mutableMapOf<String, Usuario>()
    
    init {
        loadCurrentUser()
        loadInitialPosts()
        
        // Escuchar cambios del PostRepository (nuevos posts)
        viewModelScope.launch {
            PostRepository.posts.collect { repoPosts ->
                if (repoPosts.isNotEmpty()) {
                    Log.d("HomeViewModel", "PostRepository actualizó: ${repoPosts.size} posts")
                    
                    // Buscar nuevos userIds que no tengamos en cache
                    val newUserIds = repoPosts.map { it.userId }.distinct().filter { it !in usersCache }
                    if (newUserIds.isNotEmpty()) {
                        try {
                            val newUsers = SupabaseClient.database
                                .from("usuarios")
                                .select()
                                .decodeList<Usuario>()
                                .filter { it.userId in newUserIds }
                            newUsers.forEach { usersCache[it.userId] = it }
                            Log.d("HomeViewModel", "Cargados ${newUsers.size} usuarios nuevos para posts")
                        } catch (e: Exception) {
                            Log.e("HomeViewModel", "Error cargando usuarios nuevos: ${e.message}")
                            _errorMessage.value = "Error cargando datos de usuarios: ${e.message}"
                        }
                    }
                    
                    // OPTIMIZADO: Mover transformación a Default dispatcher
                    val postsWithUserData = withContext(Dispatchers.Default) {
                        repoPosts.map { post ->
                            val cachedUser = usersCache[post.userId]
                            if (cachedUser != null && (post.username == "usuario" || post.userAvatar.isEmpty())) {
                                post.copy(
                                    username = cachedUser.username,
                                    userAvatar = cachedUser.avatarUrl ?: "",
                                    userStoreName = cachedUser.nombreTienda
                                )
                            } else {
                                post
                            }
                        }.sortedByDescending { it.createdAt }
                    }
                    
                    _allPosts.value = postsWithUserData
                    val currentVisible = _visiblePosts.value.size.coerceAtLeast(INITIAL_LOAD_COUNT)
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
                // OPTIMIZADO: Eliminado delay(300) innecesario - Supabase client maneja su propio estado
                
                // Cargar posts desde Supabase
                val postsDB = SupabaseClient.database
                    .from("posts")
                    .select()
                    .decodeList<PostDB>()
                
                Log.d("HomeViewModel", "PostsDB loaded: ${postsDB.size}")
                
                // Obtener usuarios únicos - con retry
                val userIds = postsDB.map { it.userId }.distinct()
                var users = emptyList<Usuario>()
                var retryCount = 0
                val maxRetries = 3
                
                while (users.isEmpty() && userIds.isNotEmpty() && retryCount < maxRetries) {
                    try {
                        users = SupabaseClient.database
                            .from("usuarios")
                            .select()
                            .decodeList<Usuario>()
                            .filter { it.userId in userIds }
                        
                        if (users.isEmpty() && retryCount < maxRetries - 1) {
                            Log.w("HomeViewModel", "Users empty, retry ${retryCount + 1}/$maxRetries")
                            kotlinx.coroutines.delay(500)
                        }
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error loading users (retry $retryCount): ${e.message}")
                        kotlinx.coroutines.delay(500)
                    }
                    retryCount++
                }
                
                Log.d("HomeViewModel", "Loaded ${users.size} users for ${userIds.size} unique IDs")
                
                // Cachear usuarios
                users.forEach { usersCache[it.userId] = it }
                
                // Cargar likes y saves del usuario actual
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                val userLikes = mutableSetOf<String>()
                val userSaves = mutableSetOf<String>()
                
                if (currentUserId != null) {
                    try {
                        val likes = SupabaseClient.database
                            .from("post_likes")
                            .select { filter { eq("user_id", currentUserId) } }
                            .decodeList<PostLikeDB>()
                        userLikes.addAll(likes.map { it.postId })
                        
                        val saves = SupabaseClient.database
                            .from("post_saves")
                            .select { filter { eq("user_id", currentUserId) } }
                            .decodeList<PostSaveDB>()
                        userSaves.addAll(saves.map { it.postId })
                        
                                Log.d("HomeViewModel", "Loaded ${userLikes.size} likes, ${userSaves.size} saves")
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error loading likes/saves: ${e.message}")
                    }
                }
                
                // Cargar conteo real de reviews por product_id
                val reviewsCountMap = mutableMapOf<String, Int>()
                try {
                    val productIds = postsDB.mapNotNull { it.productId }.distinct()
                    if (productIds.isNotEmpty()) {
                        val filteredReviews = SupabaseClient.database
                            .from("product_reviews")
                            .select {
                                filter {
                                    isIn("product_id", productIds)
                                    exact("parent_id", null)
                                }
                            }
                            .decodeList<CommentForCount>()
                        
                        // Contar comentarios por product_id (solo reviews top-level, no replies)
                        filteredReviews.forEach { comment ->
                            comment.productId?.let { pid ->
                                reviewsCountMap[pid] = (reviewsCountMap[pid] ?: 0) + 1
                            }
                        }
                        Log.d("HomeViewModel", "Loaded real review counts for ${reviewsCountMap.size} products")
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error loading review counts: ${e.message}")
                }
                
                // OPTIMIZADO: Mover transformación pesada a Default dispatcher
                // map() + sortedByDescending() son O(n) y O(n log n) - no deben correr en Main
                val allPostsList = withContext(Dispatchers.Default) {
                    postsDB.map { postDB ->
                        val user = usersCache[postDB.userId]
                        // Usar conteo real de reviews si disponible, sino 0
                        val realReviewCount = postDB.productId?.let { reviewsCountMap[it] } ?: 0
                        Post.fromDB(
                            postDB = postDB,
                            username = user?.username ?: "usuario",
                            avatarUrl = user?.avatarUrl ?: "",
                            storeName = user?.nombreTienda,
                            overrideReviewsCount = realReviewCount,
                            isUserVerified = user?.isVerified ?: false
                        ).copy(
                            isLiked = postDB.id in userLikes,
                            isSaved = postDB.id in userSaves
                        )
                    }.sortedByDescending { it.createdAt }
                }
                
                _allPosts.value = allPostsList
                // Mostrar solo 1 post inicial
                _visiblePosts.value = allPostsList.take(INITIAL_LOAD_COUNT)
                _hasMorePosts.value = allPostsList.size > INITIAL_LOAD_COUNT
                
                Log.d("HomeViewModel", "Loaded ${allPostsList.size} posts, showing ${_visiblePosts.value.size}")
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading posts: ${e.message}", e)
                // Mostrar error visible al usuario
                _errorMessage.value = "Error cargando posts: ${e.message}"
                _visiblePosts.value = emptyList()
            }
            _isLoading.value = false
        }
    }
    
    fun loadMorePosts() {
        if (_isLoadingMore.value || !_hasMorePosts.value) return
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            
            // OPTIMIZADO: Reducido delay de 800ms a 50ms - suficiente para feedback visual sin lag
            kotlinx.coroutines.delay(50)
            
            val currentCount = _visiblePosts.value.size
            val newCount = (currentCount + LOAD_MORE_COUNT).coerceAtMost(_allPosts.value.size)
            
            _visiblePosts.value = _allPosts.value.take(newCount)
            _hasMorePosts.value = newCount < _allPosts.value.size
            
            Log.d("HomeViewModel", "Loaded more: now showing ${_visiblePosts.value.size}/${_allPosts.value.size}")
            
            _isLoadingMore.value = false
        }
    }
    
    fun toggleLike(postId: String) {
        val currentList = _visiblePosts.value
        val postIndex = currentList.indexOfFirst { it.id == postId }
        if (postIndex == -1) return
        
        val post = currentList[postIndex]
        val wasLiked = post.isLiked
        
        // OPTIMIZADO: Actualizar solo el item específico usando toMutableList()
        // Esto es más eficiente que .map{} sobre toda la lista
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
                    
                    // Crear notificación
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
        
        // OPTIMIZADO: Actualizar solo el item específico
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
                    
                    // Crear notificación
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
        // OPTIMIZADO: Actualizar solo el item específico
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
    
    fun refreshPosts() {
        loadInitialPosts()
    }
}
