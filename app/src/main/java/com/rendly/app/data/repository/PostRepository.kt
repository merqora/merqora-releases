package com.rendly.app.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.rendly.app.data.model.Post
import com.rendly.app.data.model.PostDB
import com.rendly.app.data.model.PostUploadState
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.CloudinaryService
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// Minimal class for counting reviews by product_id
@Serializable
private data class ReviewCountDB(
    val id: String = "",
    @SerialName("product_id") val productId: String? = null
)

object PostRepository {

    private const val TAG = "PostRepository"
    private const val CACHE_DURATION_MS = 3 * 60 * 1000L // 3 minutos

    private val _uploadState = MutableStateFlow(PostUploadState())
    val uploadState: StateFlow<PostUploadState> = _uploadState.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    private val _isLoadingUserPosts = MutableStateFlow(false)
    val isLoadingUserPosts: StateFlow<Boolean> = _isLoadingUserPosts.asStateFlow()

    private var feedLastFetchTime: Long = 0L
    private var userPostsLastFetchTime: Long = 0L

    private fun isFeedCacheValid(): Boolean {
        return _posts.value.isNotEmpty() &&
               (System.currentTimeMillis() - feedLastFetchTime) < CACHE_DURATION_MS
    }

    private fun isUserPostsCacheValid(): Boolean {
        return _userPosts.value.isNotEmpty() &&
               (System.currentTimeMillis() - userPostsLastFetchTime) < CACHE_DURATION_MS
    }

    fun invalidateCache() {
        feedLastFetchTime = 0L
        userPostsLastFetchTime = 0L
    }

    private suspend fun getCurrentUserId(): String {
        return SupabaseClient.auth.currentUserOrNull()?.id
            ?: throw Exception("Usuario no autenticado")
    }

    // ===============================
    // FEED - Optimizado con batch query
    // ===============================
    suspend fun loadFeedPosts(forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!forceRefresh && isFeedCacheValid()) {
            Log.d(TAG, "Retornando feed desde cache (${_posts.value.size} posts)")
            return@withContext
        }
        try {
            _isLoading.value = true

            val postsList = SupabaseClient.database
                .from("posts")
                .select()
                .decodeList<PostDB>()
                .sortedByDescending { it.createdAt }

            Log.d(TAG, "Posts cargados: ${postsList.size}")

            // Cargar TODOS los usuarios de una sola vez (batch query)
            val userIds = postsList.map { it.userId }.distinct()
            Log.d(TAG, "User IDs únicos: $userIds")

            val usersMap = mutableMapOf<String, Usuario>()
            
            // Cargar usuarios uno por uno para asegurar datos correctos
            if (userIds.isNotEmpty()) {
                for (uId in userIds) {
                    try {
                        val userProfile = SupabaseClient.database
                            .from("usuarios")
                            .select {
                                filter { eq("user_id", uId) }
                            }
                            .decodeSingleOrNull<Usuario>()
                        
                        if (userProfile != null) {
                            usersMap[uId] = userProfile
                            Log.d(TAG, "✓ Usuario cargado: ${userProfile.username} para $uId")
                        } else {
                            Log.w(TAG, "⚠ Usuario NO encontrado para user_id: $uId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error cargando usuario $uId: ${e.message}")
                    }
                }
                Log.d(TAG, "Usuarios cargados: ${usersMap.size} de ${userIds.size}")
            }

            // Obtener conteo real de reviews por product_id
            val reviewsCountMap = mutableMapOf<String, Int>()
            
            try {
                val productIds = postsList.mapNotNull { it.productId }.distinct()
                if (productIds.isNotEmpty()) {
                    val allReviews = SupabaseClient.database
                        .from("product_reviews")
                        .select()
                        .decodeList<ReviewCountDB>()
                    
                    // Contar reviews por product_id
                    allReviews.forEach { review ->
                        review.productId?.let { pid ->
                            reviewsCountMap[pid] = (reviewsCountMap[pid] ?: 0) + 1
                        }
                    }
                    Log.d(TAG, "Real review counts loaded for ${reviewsCountMap.size} products")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading review counts: ${e.message}")
            }

            val postsWithUser = postsList.map { postDB ->
                val usuario = usersMap[postDB.userId]
                val realReviewsCount = postDB.productId?.let { reviewsCountMap[it] } ?: 0

                Post.fromDB(
                    postDB = postDB,
                    username = usuario?.username ?: "usuario",
                    avatarUrl = usuario?.avatarUrl ?: "",
                    storeName = usuario?.nombreTienda,
                    overrideReviewsCount = realReviewsCount,
                    isUserVerified = usuario?.isVerified ?: false
                )
            }

            _posts.value = postsWithUser
            Log.d(TAG, "Loaded ${postsWithUser.size} posts for feed")

            feedLastFetchTime = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading feed posts", e)
        } finally {
            _isLoading.value = false
        }
    }

    // ===============================
    // POSTS DEL USUARIO
    // ===============================
    
    /**
     * Obtiene el conteo de posts de un usuario específico
     */
    suspend fun getUserPostsCount(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val postsList = SupabaseClient.database
                .from("posts")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<PostDB>()
            
            Log.d(TAG, "Posts count for $userId: ${postsList.size}")
            postsList.size
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user posts count", e)
            0
        }
    }
    
    suspend fun loadUserPosts(forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!forceRefresh && isUserPostsCacheValid()) {
            Log.d(TAG, "Retornando user posts desde cache (${_userPosts.value.size} posts)")
            return@withContext
        }
        try {
            _isLoadingUserPosts.value = true
            val userId = getCurrentUserId()

            val postsList = SupabaseClient.database
                .from("posts")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<PostDB>()
                .sortedByDescending { it.createdAt }

            val userProfile = ProfileRepository.currentProfile.value

            val postsWithUser = postsList.map { postDB ->
                Post.fromDB(
                    postDB = postDB,
                    username = userProfile?.username ?: "usuario",
                    avatarUrl = userProfile?.avatarUrl ?: "",
                    storeName = userProfile?.nombreTienda,
                    overrideReviewsCount = 0,
                    isUserVerified = userProfile?.isVerified ?: false
                )
            }

            _userPosts.value = postsWithUser
            userPostsLastFetchTime = System.currentTimeMillis()
            Log.d(TAG, "Loaded ${postsWithUser.size} user posts")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user posts", e)
        } finally {
            _isLoadingUserPosts.value = false
        }
    }

    // ===============================
    // CREAR POST (🔥 ARREGLADO - Soporte múltiples imágenes)
    // ===============================
    suspend fun createPost(
        bitmaps: List<Bitmap>,
        caption: String? = null,
        title: String? = null,
        price: Double? = null,
        condition: String? = null,
        category: String? = null,
        allowOffers: Boolean = true,
        freeShipping: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): Result<Post> = withContext(Dispatchers.IO) {

        try {
            Log.d(TAG, "=== INICIANDO CREACIÓN DE POST ===")
            Log.d(TAG, "Datos: title=$title, price=$price, condition=$condition, category=$category")
            Log.d(TAG, "Imágenes a subir: ${bitmaps.size}")

            val userId = getCurrentUserId()
            Log.d(TAG, "User ID: $userId")

            // Progreso inicial visible
            _uploadState.value = PostUploadState(isUploading = true, progress = 0.05f)
            kotlinx.coroutines.delay(200)
            _uploadState.value = _uploadState.value.copy(progress = 0.10f)
            kotlinx.coroutines.delay(200)
            _uploadState.value = _uploadState.value.copy(progress = 0.15f)

            // 1️⃣ Subir TODAS las imágenes (15% - 75%)
            val imageUrls = mutableListOf<String>()
            val progressPerImage = 0.60f / bitmaps.size
            
            bitmaps.forEachIndexed { index, bitmap ->
                Log.d(TAG, "Subiendo imagen ${index + 1}/${bitmaps.size} a Cloudinary...")
                val imageUrl = CloudinaryService.uploadImage(
                    bitmap = bitmap,
                    folder = "posts/$userId",
                    onProgress = { progress ->
                        val baseProgress = 0.15f + (index * progressPerImage)
                        val adjusted = baseProgress + (progress * progressPerImage)
                        _uploadState.value = _uploadState.value.copy(progress = adjusted)
                    }
                ).getOrThrow()
                
                imageUrls.add(imageUrl)
                Log.d(TAG, "Imagen ${index + 1} subida: $imageUrl")
            }

            Log.d(TAG, "Todas las imágenes subidas: ${imageUrls.size}")
            
            // Progreso después de subir imágenes
            _uploadState.value = _uploadState.value.copy(progress = 0.80f)
            kotlinx.coroutines.delay(300)

            // Calcular precio anterior simulado (25% más que el actual)
            val actualPrice = price ?: 0.0
            val previousPrice = if (actualPrice > 0) actualPrice * 1.25 else 0.0

            // 2️⃣ JSON para INSERT con TODAS las imágenes
            val imagesArray = buildJsonArray { 
                imageUrls.forEach { url -> add(JsonPrimitive(url)) }
            }
            val coverUrl = imageUrls.firstOrNull() ?: ""
            val postJson = buildJsonObject {
                put("user_id", userId)
                put("title", title ?: caption ?: "Producto sin título")
                put("description", caption ?: "")
                put("images", imagesArray)
                put("status", "active")
                put("cover_url", coverUrl)
                put("price", actualPrice)
                put("previous_price", previousPrice)
                put("condition", condition ?: "Nuevo")
                put("category", category ?: "General")
                put("allow_offers", allowOffers)
                put("free_shipping", freeShipping)
                put("stock", 1)
            }

            _uploadState.value = _uploadState.value.copy(progress = 0.85f)

            // 3️⃣ INSERT directo (sin decodificar respuesta)
            SupabaseClient.database
                .from("posts")
                .insert(postJson)
            
            // Progreso final visible y fluido
            _uploadState.value = _uploadState.value.copy(progress = 0.90f)
            kotlinx.coroutines.delay(200)
            _uploadState.value = _uploadState.value.copy(progress = 0.95f)
            kotlinx.coroutines.delay(200)
            _uploadState.value = _uploadState.value.copy(progress = 1f)
            kotlinx.coroutines.delay(300)

            // Marcar como completado
            _uploadState.value = PostUploadState(
                isUploading = false,
                isComplete = true,
                progress = 1f
            )

            // Recargar posts desde Supabase (forzar refresh del cache)
            Log.d(TAG, "Recargando posts desde Supabase...")
            loadFeedPosts(forceRefresh = true)
            loadUserPosts(forceRefresh = true)
            
            Log.d(TAG, "=== POST CREADO EXITOSAMENTE ===")

            // Retornar el primer post recargado (el más reciente)
            val latestPost = _posts.value.firstOrNull()
            if (latestPost != null) {
                Result.success(latestPost)
            } else {
                Result.success(
                    Post(
                        id = "",
                        userId = userId,
                        title = caption ?: "",
                        images = imageUrls
                    )
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "ERROR CRÍTICO AL CREAR POST", e)
            _uploadState.value = PostUploadState(
                isUploading = false,
                error = e.message
            )
            Result.failure(e)
        }
    }

    fun resetUploadState() {
        _uploadState.value = PostUploadState()
    }

    // ===============================
    // UPDATE POST
    // ===============================
    suspend fun updatePost(
        postId: String,
        title: String? = null,
        description: String? = null,
        price: Double? = null,
        previousPrice: Double? = null,
        category: String? = null,
        condition: String? = null,
        allowOffers: Boolean? = null,
        freeShipping: Boolean? = null,
        status: String? = null,
        warranty: String? = null,
        returnsAccepted: Boolean? = null,
        colors: List<String>? = null,
        images: List<String>? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating post $postId")
            
            val updateJson = buildJsonObject {
                title?.let { put("title", it) }
                description?.let { put("description", it) }
                price?.let { put("price", it) }
                previousPrice?.let { put("previous_price", it) }
                category?.let { put("category", it) }
                condition?.let { put("condition", it) }
                allowOffers?.let { put("allow_offers", it) }
                freeShipping?.let { put("free_shipping", it) }
                status?.let { put("status", it) }
                warranty?.let { put("warranty", it) }
                returnsAccepted?.let { put("returns_accepted", it) }
                colors?.let { put("colors", buildJsonArray { colors.forEach { add(JsonPrimitive(it)) } }) }
                images?.let { put("images", buildJsonArray { images.forEach { add(JsonPrimitive(it)) } }) }
            }
            
            SupabaseClient.database
                .from("posts")
                .update(updateJson) {
                    filter { eq("id", postId) }
                }
            
            Log.d(TAG, "Post updated successfully")
            
            // Reload posts to reflect changes
            loadFeedPosts(forceRefresh = true)
            loadUserPosts(forceRefresh = true)
            
            // Clear ExploreRepository cache to refresh category counts
            ExploreRepository.clearCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating post", e)
            Result.failure(e)
        }
    }

    // ===============================
    // DELETE
    // ===============================
    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("posts")
                .delete {
                    filter { eq("id", postId) }
                }

            loadFeedPosts(forceRefresh = true)
            loadUserPosts(forceRefresh = true)
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post", e)
            Result.failure(e)
        }
    }

    // ===============================
    // GET POSTS BY CATEGORY - Para explorar productos relacionados con paginación
    // ===============================
    suspend fun getPostsByCategory(
        category: String? = null,
        excludePostId: String = "",
        limit: Int = 20,
        offset: Int = 0
    ): List<Post> = withContext(Dispatchers.IO) {
        try {
            val postsList = if (category != null && category.isNotBlank()) {
                SupabaseClient.database
                    .from("posts")
                    .select {
                        filter { 
                            eq("category", category)
                            if (excludePostId.isNotBlank()) {
                                neq("id", excludePostId)
                            }
                        }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(limit.toLong())
                        range(offset.toLong(), (offset + limit - 1).toLong())
                    }
                    .decodeList<PostDB>()
            } else {
                // Sin categoría, cargar posts aleatorios
                SupabaseClient.database
                    .from("posts")
                    .select {
                        if (excludePostId.isNotBlank()) {
                            filter { neq("id", excludePostId) }
                        }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(limit.toLong())
                        range(offset.toLong(), (offset + limit - 1).toLong())
                    }
                    .decodeList<PostDB>()
            }
            
            // Cargar usuarios
            val userIds = postsList.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, Usuario>()
            
            for (uId in userIds) {
                try {
                    val userProfile = SupabaseClient.database
                        .from("usuarios")
                        .select { filter { eq("user_id", uId) } }
                        .decodeSingleOrNull<Usuario>()
                    if (userProfile != null) {
                        usersMap[uId] = userProfile
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error cargando usuario $uId: ${e.message}")
                }
            }
            
            // Convertir a modelo UI
            postsList.mapNotNull { postDB ->
                val user = usersMap[postDB.userId]
                if (user != null) {
                    Post.fromDB(
                        postDB = postDB,
                        username = user.username,
                        avatarUrl = user.avatarUrl ?: "",
                        storeName = user.nombreTienda,
                        isUserVerified = user.isVerified
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading category posts: ${e.message}")
            emptyList()
        }
    }

    // ===============================
    // PROFILE AUX
    // ===============================
    @kotlinx.serialization.Serializable
    private data class ProfileData(
        @kotlinx.serialization.SerialName("user_id")
        val userId: String = "",
        val username: String = "",
        @kotlinx.serialization.SerialName("avatar_url")
        val avatarUrl: String? = null,
        @kotlinx.serialization.SerialName("nombre_tienda")
        val nombreTienda: String? = null
    )
    
    // ===============================
    // GET POSTS BY IDS - Para cargar posts del carrito
    // ===============================
    suspend fun getPostsByIds(postIds: List<String>): List<Post> = withContext(Dispatchers.IO) {
        if (postIds.isEmpty()) return@withContext emptyList()
        
        try {
            val postsList = SupabaseClient.database
                .from("posts")
                .select {
                    filter {
                        isIn("id", postIds)
                    }
                }
                .decodeList<PostDB>()
            
            // Obtener IDs de usuarios únicos
            val userIds = postsList.map { it.userId }.distinct()
            
            // Cargar datos de usuarios en batch
            val usersMap = if (userIds.isNotEmpty()) {
                try {
                    SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { isIn("user_id", userIds) }
                        }
                        .decodeList<Usuario>()
                        .associateBy { it.userId }
                } catch (e: Exception) {
                    Log.e(TAG, "Error cargando usuarios: ${e.message}")
                    emptyMap()
                }
            } else emptyMap()
            
            postsList.map { postDB ->
                val user = usersMap[postDB.userId]
                Post.fromDB(
                    postDB = postDB,
                    username = user?.username ?: "Usuario",
                    avatarUrl = user?.avatarUrl ?: "",
                    storeName = user?.nombreTienda,
                    isUserVerified = user?.isVerified ?: false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo posts por IDs: ${e.message}")
            emptyList()
        }
    }
}

