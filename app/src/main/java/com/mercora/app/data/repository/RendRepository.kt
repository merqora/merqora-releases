package com.mercora.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mercora.app.data.model.Rend
import com.mercora.app.data.model.RendDB
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.remote.ImageKitService
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class RendUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val isComplete: Boolean = false,
    val error: String? = null
)

object RendRepository {
    private const val TAG = "RendRepository"
    
    private val _uploadState = MutableStateFlow(RendUploadState())
    val uploadState: StateFlow<RendUploadState> = _uploadState.asStateFlow()
    
    private val _rends = MutableStateFlow<List<Rend>>(emptyList())
    val rends: StateFlow<List<Rend>> = _rends.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _pendingRendId = MutableStateFlow<String?>(null)
    val pendingRendId: StateFlow<String?> = _pendingRendId.asStateFlow()

    // Infinite scroll state
    private const val PAGE_SIZE = 10
    private var hasMoreRends = true
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    fun setPendingRendId(id: String?) {
        _pendingRendId.value = id
    }
    
    fun consumePendingRendId(): String? {
        val id = _pendingRendId.value
        _pendingRendId.value = null
        return id
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private suspend fun getCurrentUserId(): String {
        return SupabaseClient.auth.currentUserOrNull()?.id
            ?: throw Exception("Usuario no autenticado")
    }
    
    /**
     * Load all rends from Supabase
     */
    suspend fun loadRends() = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Loading rends from Supabase...")
            Log.d(TAG, "Supabase URL: ${com.vinzay.app.BuildConfig.SUPABASE_URL}")
            
            val rendsDB = SupabaseClient.database
                .from("rends")
                .select()
                .decodeList<RendDB>()
                .sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Loaded ${rendsDB.size} rends")
            
            // Load user data
            val userIds = rendsDB.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, Usuario>()
            
            if (userIds.isNotEmpty()) {
                try {
                    val users = SupabaseClient.database
                        .from("usuarios")
                        .select()
                        .decodeList<Usuario>()
                        .filter { it.userId in userIds }
                    
                    users.forEach { usersMap[it.userId] = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading users: ${e.message}")
                }
            }
            
            // Load privacy settings for show_likes
            val privacyMap = mutableMapOf<String, Boolean>()
            for (uid in userIds) {
                try {
                    val settings = UserPreferencesRepository.loadPrivacySettings(uid)
                    privacyMap[uid] = settings?.showLikes ?: true
                } catch (_: Exception) { privacyMap[uid] = true }
            }

            // Map to Rend with user data
            val rendsList = rendsDB.map { db ->
                val user = usersMap[db.userId]
                val showLikes = privacyMap[db.userId] ?: true
                // DEBUG: Log datos de imagen
                Log.d(TAG, "â•â•â• Rend ${db.id.take(8)}... â•â•â•")
                Log.d(TAG, "  thumbnailUrl: ${db.thumbnailUrl?.take(60) ?: "NULL"}")
                Log.d(TAG, "  productImage: ${db.productImage?.take(60) ?: "NULL"}")
                
                Rend.fromDB(
                    db = db,
                    username = user?.username ?: "usuario",
                    avatarUrl = user?.avatarUrl ?: "",
                    storeName = user?.nombreTienda
                ).copy(
                    likesCount = if (showLikes) db.likesCount else 0
                )
            }
            
            _rends.value = rendsList
            _isLoading.value = false
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading rends", e)
            val errorMsg = e.message ?: "Error desconocido"
            // Check if table doesn't exist
            if (errorMsg.contains("relation") && errorMsg.contains("does not exist")) {
                _errorMessage.value = "La tabla 'rends' no existe. Ejecuta SUPABASE_RENDS_TABLE.sql en Supabase."
            } else if (errorMsg.contains("localhost")) {
                _errorMessage.value = "Error de conexiÃ³n: Verifica tu conexiÃ³n a internet y reinicia la app."
            } else {
                _errorMessage.value = "Error cargando Rends: $errorMsg"
            }
            _isLoading.value = false
        }
    }
    
    /**
     * Load next page of rends for infinite scroll in full-screen feed
     */
    suspend fun loadMoreRends() = withContext(Dispatchers.IO) {
        if (_isLoadingMore.value || !hasMoreRends) return@withContext
        try {
            _isLoadingMore.value = true
            val offset = _rends.value.size

            val newRendsDB = SupabaseClient.database
                .from("rends")
                .select {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(PAGE_SIZE.toLong())
                }
                .decodeList<RendDB>()

            if (newRendsDB.isEmpty()) {
                hasMoreRends = false
                return@withContext
            }

            val userIds = newRendsDB.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, Usuario>()
            if (userIds.isNotEmpty()) {
                try {
                    val users = SupabaseClient.database
                        .from("usuarios")
                        .select()
                        .decodeList<Usuario>()
                        .filter { it.userId in userIds }
                    users.forEach { usersMap[it.userId] = it }
                } catch (_: Exception) { }
            }

            val privacyMapMore = mutableMapOf<String, Boolean>()
            for (uid in userIds) {
                try {
                    val settings = UserPreferencesRepository.loadPrivacySettings(uid)
                    privacyMapMore[uid] = settings?.showLikes ?: true
                } catch (_: Exception) { privacyMapMore[uid] = true }
            }

            val newRends = newRendsDB.map { db ->
                val user = usersMap[db.userId]
                val showLikes = privacyMapMore[db.userId] ?: true
                Rend.fromDB(
                    db = db,
                    username = user?.username ?: "usuario",
                    avatarUrl = user?.avatarUrl ?: "",
                    storeName = user?.nombreTienda
                ).copy(
                    likesCount = if (showLikes) db.likesCount else 0
                )
            }

            _rends.value = _rends.value + newRends
            hasMoreRends = newRendsDB.size >= PAGE_SIZE
        } catch (e: Exception) {
            Log.e(TAG, "Error loading more rends", e)
        } finally {
            _isLoadingMore.value = false
        }
    }

    // Rends de usuarios seguidos
    private val _followingRends = MutableStateFlow<List<Rend>>(emptyList())
    val followingRends: StateFlow<List<Rend>> = _followingRends.asStateFlow()
    
    private val _isLoadingFollowing = MutableStateFlow(false)
    val isLoadingFollowing: StateFlow<Boolean> = _isLoadingFollowing.asStateFlow()
    
    /**
     * Load rends only from users the current user follows
     */
    suspend fun loadFollowingRends() = withContext(Dispatchers.IO) {
        try {
            _isLoadingFollowing.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
            
            // Get list of followed user IDs
            val followedUsers = SupabaseClient.database
                .from("followers")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("followed_id")) {
                    filter { eq("follower_id", currentUserId) }
                }
                .decodeList<FollowedIdRow>()
                .map { it.followedId }
            
            if (followedUsers.isEmpty()) {
                _followingRends.value = emptyList()
                _isLoadingFollowing.value = false
                return@withContext
            }
            
            // Load rends from followed users
            val rendsDB = SupabaseClient.database
                .from("rends")
                .select {
                    filter { isIn("user_id", followedUsers) }
                }
                .decodeList<RendDB>()
                .sortedByDescending { it.createdAt }
            
            // Load user data
            val userIds = rendsDB.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, Usuario>()
            if (userIds.isNotEmpty()) {
                try {
                    val users = SupabaseClient.database
                        .from("usuarios")
                        .select()
                        .decodeList<Usuario>()
                        .filter { it.userId in userIds }
                    users.forEach { usersMap[it.userId] = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading users for following rends: ${e.message}")
                }
            }
            
            _followingRends.value = rendsDB.map { db ->
                val user = usersMap[db.userId]
                Rend.fromDB(
                    db = db,
                    username = user?.username ?: "usuario",
                    avatarUrl = user?.avatarUrl ?: "",
                    storeName = user?.nombreTienda
                )
            }
            
            Log.d(TAG, "Loaded ${_followingRends.value.size} following rends from ${followedUsers.size} followed users")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading following rends: ${e.message}", e)
        } finally {
            _isLoadingFollowing.value = false
        }
    }
    
    @Serializable
    private data class FollowedIdRow(
        @SerialName("followed_id") val followedId: String
    )
    
    /**
     * Create a new Rend with video upload to ImageKit
     */
    suspend fun createRend(
        context: Context,
        videoUri: Uri,
        title: String,
        description: String? = null,
        productTitle: String? = null,
        productPrice: Double? = null,
        productImage: String? = null,
        productId: String? = null,
        duration: Int = 0,
        visibility: String = "public",
        allowOpinions: Boolean = true,
        allowConsults: Boolean = true,
        allowDownloads: Boolean = false,
        allowShares: Boolean = true,
        hashtags: List<String> = emptyList(),
        category: String? = null,
        location: String? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<Rend> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== CREATING REND ===")
            
            val userId = getCurrentUserId()
            
            _uploadState.value = RendUploadState(isUploading = true, progress = 0.05f)
            
            // Upload video to ImageKit (5% - 80%)
            Log.d(TAG, "Uploading video to ImageKit...")
            val videoUrl = ImageKitService.uploadVideo(
                context = context,
                videoUri = videoUri,
                folder = "rends/$userId",
                onProgress = { progress ->
                    val adjusted = 0.05f + progress * 0.75f
                    _uploadState.value = _uploadState.value.copy(progress = adjusted)
                    onProgress(adjusted)
                }
            ).getOrThrow()
            
            Log.d(TAG, "Video uploaded: $videoUrl")
            _uploadState.value = _uploadState.value.copy(progress = 0.85f)
            
            // Create thumbnail URL (ImageKit auto-generates from video)
            // Format: https://ik.imagekit.io/.../video.mp4/ik-thumbnail.jpg
            val thumbnailUrl = "$videoUrl/ik-thumbnail.jpg"
            
            // Insert into Supabase - usar buildJsonObject como otros repositorios
            // Usar productImage de Cloudinary si existe, sino usar thumbnailUrl de ImageKit
            val finalProductImage = productImage?.takeIf { it.isNotBlank() } ?: thumbnailUrl
            
            val rendData = buildJsonObject {
                put("user_id", userId)
                put("title", title)
                put("video_url", videoUrl)
                put("thumbnail_url", thumbnailUrl)
                put("product_image", finalProductImage)
                put("duration", duration)
                put("status", "active")
                put("visibility", visibility)
                put("allow_opinions", allowOpinions)
                put("allow_consults", allowConsults)
                put("allow_downloads", allowDownloads)
                put("allow_shares", allowShares)
                if (!description.isNullOrBlank()) put("description", description)
                if (!productTitle.isNullOrBlank()) put("product_title", productTitle)
                if (productPrice != null) put("product_price", productPrice)
                if (!productId.isNullOrBlank()) put("product_id", productId)
                if (hashtags.isNotEmpty()) {
                    put("hashtags", kotlinx.serialization.json.JsonArray(hashtags.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                }
                if (!category.isNullOrBlank()) put("category", category)
                if (!location.isNullOrBlank()) put("location", location)
            }
            
            // Actualizar stats de hashtags y categorÃ­a
            if (hashtags.isNotEmpty()) {
                try {
                    SupabaseClient.database.rpc(
                        "increment_hashtag_stats",
                        buildJsonObject {
                            put("p_tags", kotlinx.serialization.json.JsonArray(hashtags.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                        }
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error updating hashtag stats: ${e.message}")
                }
            }
            if (!category.isNullOrBlank()) {
                try {
                    SupabaseClient.database.rpc(
                        "increment_category_stats",
                        buildJsonObject { put("p_category", category) }
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error updating category stats: ${e.message}")
                }
            }
            
            Log.d(TAG, "=== INSERTANDO REND EN SUPABASE ===")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Video URL: $videoUrl")
            Log.d(TAG, "Data completa: $rendData")
            
            _uploadState.value = _uploadState.value.copy(progress = 0.90f)
            
            // Insertar con manejo de errores explÃ­cito
            val insertResult = runCatching {
                SupabaseClient.database
                    .from("rends")
                    .insert(rendData)
            }
            
            if (insertResult.isFailure) {
                val dbError = insertResult.exceptionOrNull()
                Log.e(TAG, "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•")
                Log.e(TAG, "ERROR CRÃTICO insertando Rend en Supabase")
                Log.e(TAG, "Mensaje: ${dbError?.message}")
                Log.e(TAG, "Causa: ${dbError?.cause?.message}")
                Log.e(TAG, "â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•")
                dbError?.printStackTrace()
                throw Exception("Error guardando Rend: ${dbError?.message}")
            }
            
            Log.d(TAG, "âœ“ Rend insertado exitosamente en Supabase")
            _uploadState.value = _uploadState.value.copy(progress = 0.95f)
            
            // Reload rends
            loadRends()
            
            _uploadState.value = RendUploadState(
                isUploading = false,
                isComplete = true,
                progress = 1f
            )
            
            Log.d(TAG, "=== REND CREATED SUCCESSFULLY ===")
            
            // Return first rend (most recent)
            Result.success(_rends.value.firstOrNull() ?: throw Exception("Rend not found"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating rend", e)
            _uploadState.value = RendUploadState(
                isUploading = false,
                error = e.message
            )
            Result.failure(e)
        }
    }
    
    fun resetUploadState() {
        _uploadState.value = RendUploadState()
    }
    
    /**
     * Toggle like on a Rend and update like_count in Supabase
     */
    suspend fun toggleLike(rendId: String, currentLikeCount: Int, isLiked: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val newLikeCount = if (isLiked) currentLikeCount - 1 else currentLikeCount + 1
            
            val updateData = buildJsonObject {
                put("likes_count", newLikeCount.coerceAtLeast(0))
            }
            
            SupabaseClient.database
                .from("rends")
                .update(updateData) {
                    filter { eq("id", rendId) }
                }
            
            // Update local state immediately
            _rends.value = _rends.value.map { r ->
                if (r.id == rendId) r.copy(
                    isLiked = !isLiked,
                    likesCount = newLikeCount.coerceAtLeast(0)
                ) else r
            }
            
            Log.d(TAG, "Like updated: $rendId -> $newLikeCount")
            Result.success(newLikeCount.coerceAtLeast(0))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating like", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle save on a Rend and update local state
     */
    suspend fun toggleSave(rendId: String, currentSavesCount: Int, isSaved: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = SupabaseClient.auth.currentUserOrNull()?.id ?: throw Exception("No auth")
            if (isSaved) {
                // Remove save
                SupabaseClient.database
                    .from("user_interactions")
                    .delete {
                        filter {
                            eq("user_id", uid)
                            eq("target_id", rendId)
                            eq("target_type", "rend")
                            eq("interaction_type", "save")
                        }
                    }
            } else {
                // Add save
                val data = buildJsonObject {
                    put("user_id", uid)
                    put("target_id", rendId)
                    put("target_type", "rend")
                    put("interaction_type", "save")
                }
                SupabaseClient.database
                    .from("user_interactions")
                    .upsert(data)
            }
            
            // Update local state
            _rends.value = _rends.value.map { r ->
                if (r.id == rendId) r.copy(
                    isSaved = !isSaved,
                    savesCount = if (isSaved) (currentSavesCount - 1).coerceAtLeast(0) else currentSavesCount + 1
                ) else r
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling save", e)
            Result.failure(e)
        }
    }
    
    /**
     * Increment comment count
     */
    suspend fun incrementCommentCount(rendId: String, currentCount: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val newCount = currentCount + 1
            
            val updateData = buildJsonObject {
                put("comments_count", newCount)
            }
            
            SupabaseClient.database
                .from("rends")
                .update(updateData) {
                    filter { eq("id", rendId) }
                }
            
            Result.success(newCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating comment count", e)
            Result.failure(e)
        }
    }
    
    /**
     * Increment share count
     */
    suspend fun incrementShareCount(rendId: String, currentCount: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val newCount = currentCount + 1
            
            val updateData = buildJsonObject {
                put("shares_count", newCount)
            }
            
            SupabaseClient.database
                .from("rends")
                .update(updateData) {
                    filter { eq("id", rendId) }
                }
            
            Result.success(newCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating share count", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get trending hashtags sorted by usage count
     */
    suspend fun getTrendingHashtags(limit: Int = 20): List<TrendingHashtag> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.database
                .from("rend_hashtag_stats")
                .select {
                    order("usage_count", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<TrendingHashtagDB>()
            result.map { TrendingHashtag(it.tag, it.usageCount) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading trending hashtags", e)
            emptyList()
        }
    }
    
    /**
     * Get popular categories sorted by usage count
     */
    suspend fun getPopularCategories(limit: Int = 12): List<PopularCategory> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.database
                .from("rend_category_stats")
                .select {
                    order("usage_count", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<PopularCategoryDB>()
            result.map { PopularCategory(it.category, it.usageCount) }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading popular categories", e)
            emptyList()
        }
    }
}

@kotlinx.serialization.Serializable
data class TrendingHashtagDB(
    val tag: String = "",
    @kotlinx.serialization.SerialName("usage_count") val usageCount: Int = 0
)

data class TrendingHashtag(val tag: String, val usageCount: Int)

@kotlinx.serialization.Serializable
data class PopularCategoryDB(
    val category: String = "",
    @kotlinx.serialization.SerialName("usage_count") val usageCount: Int = 0
)

data class PopularCategory(val category: String, val usageCount: Int)
