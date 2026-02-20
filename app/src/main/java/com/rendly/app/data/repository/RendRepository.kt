package com.rendly.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.rendly.app.data.model.Rend
import com.rendly.app.data.model.RendDB
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.ImageKitService
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
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
            Log.d(TAG, "Supabase URL: ${com.rendly.app.BuildConfig.SUPABASE_URL}")
            
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
            
            // Map to Rend with user data
            val rendsList = rendsDB.map { db ->
                val user = usersMap[db.userId]
                // DEBUG: Log datos de imagen
                Log.d(TAG, "═══ Rend ${db.id.take(8)}... ═══")
                Log.d(TAG, "  thumbnailUrl: ${db.thumbnailUrl?.take(60) ?: "NULL"}")
                Log.d(TAG, "  productImage: ${db.productImage?.take(60) ?: "NULL"}")
                
                Rend.fromDB(
                    db = db,
                    username = user?.username ?: "usuario",
                    avatarUrl = user?.avatarUrl ?: "",
                    storeName = user?.nombreTienda
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
                _errorMessage.value = "Error de conexión: Verifica tu conexión a internet y reinicia la app."
            } else {
                _errorMessage.value = "Error cargando Rends: $errorMsg"
            }
            _isLoading.value = false
        }
    }
    
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
        productImage: String? = null, // Imagen del producto enlazado (Cloudinary URL)
        productId: String? = null, // product_id del Post enlazado para unificar reviews
        duration: Int = 0,
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
                put("product_image", finalProductImage) // Imagen del producto (Cloudinary si enlazado, sino thumbnail)
                put("duration", duration)
                put("status", "active")
                if (!description.isNullOrBlank()) put("description", description)
                if (!productTitle.isNullOrBlank()) put("product_title", productTitle)
                if (productPrice != null) put("product_price", productPrice)
                if (!productId.isNullOrBlank()) put("product_id", productId) // Mismo product_id que el Post enlazado
            }
            
            Log.d(TAG, "=== INSERTANDO REND EN SUPABASE ===")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Video URL: $videoUrl")
            Log.d(TAG, "Data completa: $rendData")
            
            _uploadState.value = _uploadState.value.copy(progress = 0.90f)
            
            // Insertar con manejo de errores explícito
            val insertResult = runCatching {
                SupabaseClient.database
                    .from("rends")
                    .insert(rendData)
            }
            
            if (insertResult.isFailure) {
                val dbError = insertResult.exceptionOrNull()
                Log.e(TAG, "═══════════════════════════════════════════")
                Log.e(TAG, "ERROR CRÍTICO insertando Rend en Supabase")
                Log.e(TAG, "Mensaje: ${dbError?.message}")
                Log.e(TAG, "Causa: ${dbError?.cause?.message}")
                Log.e(TAG, "═══════════════════════════════════════════")
                dbError?.printStackTrace()
                throw Exception("Error guardando Rend: ${dbError?.message}")
            }
            
            Log.d(TAG, "✓ Rend insertado exitosamente en Supabase")
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
            
            Log.d(TAG, "Like updated: $rendId -> $newLikeCount")
            Result.success(newLikeCount.coerceAtLeast(0))
        } catch (e: Exception) {
            Log.e(TAG, "Error updating like", e)
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
}
