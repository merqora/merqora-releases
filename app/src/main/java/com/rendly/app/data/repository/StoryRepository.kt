package com.rendly.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.rendly.app.data.model.Story
import com.rendly.app.data.model.StoryUploadState
import com.rendly.app.data.remote.CloudflareService
import com.rendly.app.data.remote.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
private data class StoryHiddenEntry(
    @SerialName("user_id") val userId: String = "",
    @SerialName("hidden_user_id") val hiddenUserId: String = ""
)

object StoryRepository {
    private const val TAG = "StoryRepository"
    private const val PREFS_NAME = "story_preferences"
    private const val KEY_VIEWED_STORIES = "viewed_story_ids"
    
    private var sharedPreferences: SharedPreferences? = null
    
    private val _uploadState = MutableStateFlow(StoryUploadState())
    val uploadState: StateFlow<StoryUploadState> = _uploadState.asStateFlow()
    
    private val _myStories = MutableStateFlow<List<Story>>(emptyList())
    val myStories: StateFlow<List<Story>> = _myStories.asStateFlow()
    
    private val _otherUsersStories = MutableStateFlow<List<StoryWithUser>>(emptyList())
    val otherUsersStories: StateFlow<List<StoryWithUser>> = _otherUsersStories.asStateFlow()
    
    // Set de IDs de stories vistas por el usuario actual (PERSISTIDO en SharedPreferences)
    private val _viewedStoryIds = MutableStateFlow<Set<String>>(emptySet())
    val viewedStoryIds: StateFlow<Set<String>> = _viewedStoryIds.asStateFlow()
    
    /**
     * Inicializar el repositorio con Context para persistencia
     * Llamar esto desde Application o MainActivity
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadViewedStoriesFromPrefs()
        Log.d(TAG, "StoryRepository inicializado con ${_viewedStoryIds.value.size} stories vistas persistidas")
    }
    
    // Cargar stories vistas desde SharedPreferences
    private fun loadViewedStoriesFromPrefs() {
        val savedIds = sharedPreferences?.getStringSet(KEY_VIEWED_STORIES, emptySet()) ?: emptySet()
        _viewedStoryIds.value = savedIds.toSet()
    }
    
    // Guardar stories vistas en SharedPreferences
    private fun saveViewedStoriesToPrefs() {
        sharedPreferences?.edit()?.apply {
            putStringSet(KEY_VIEWED_STORIES, _viewedStoryIds.value)
            apply()
        }
    }
    
    // Marcar una story como vista (CON PERSISTENCIA)
    fun markStoryAsViewed(storyId: String) {
        _viewedStoryIds.value = _viewedStoryIds.value + storyId
        saveViewedStoriesToPrefs()
        Log.d(TAG, "Story marcada como vista y persistida: $storyId")
    }
    
    // Limpiar stories vistas antiguas (llamar periódicamente)
    fun cleanOldViewedStories(activeStoryIds: Set<String>) {
        // Solo mantener IDs de stories que aún existen
        val cleanedIds = _viewedStoryIds.value.intersect(activeStoryIds)
        if (cleanedIds.size != _viewedStoryIds.value.size) {
            _viewedStoryIds.value = cleanedIds
            saveViewedStoriesToPrefs()
            Log.d(TAG, "Stories vistas limpiadas: ${_viewedStoryIds.value.size} restantes")
        }
    }
    
    // Data class para stories con info del usuario
    data class StoryWithUser(
        val story: Story,
        val userId: String,
        val username: String,
        val avatarUrl: String?
    )
    
    private suspend fun getCurrentUserId(): String {
        // Siempre obtener el usuario actual fresco, no cachear
        val userId = SupabaseClient.auth.currentUserOrNull()?.id
        Log.d(TAG, "getCurrentUserId: $userId")
        return userId ?: throw Exception("Usuario no autenticado")
    }
    
    suspend fun uploadStory(
        bitmap: Bitmap,
        onProgress: (Float) -> Unit = {}
    ): Result<Story> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== INICIANDO SUBIDA DE STORY ===")
            _uploadState.value = StoryUploadState(isUploading = true, progress = 0f)
            onProgress(0.1f)
            
            // Obtener usuario actual
            val userId = getCurrentUserId()
            Log.d(TAG, "User ID: $userId")
            
            // 1. Subir imagen a Cloudflare
            Log.d(TAG, "Subiendo a Cloudflare...")
            val uploadResult = CloudflareService.uploadImage(
                bitmap = bitmap,
                folder = "stories/$userId",
                onProgress = { progress ->
                    val adjustedProgress = progress * 0.7f // 0-70% para upload
                    _uploadState.value = _uploadState.value.copy(progress = adjustedProgress)
                    onProgress(adjustedProgress)
                }
            )
            
            val publicUrl = uploadResult.getOrElse { error ->
                Log.e(TAG, "Error en Cloudflare: ${error.message}")
                throw error
            }
            Log.d(TAG, "Cloudflare URL: $publicUrl")
            
            _uploadState.value = _uploadState.value.copy(progress = 0.8f)
            onProgress(0.8f)
            
            // 5. Crear registro en tabla stories
            // Usar formato ISO 8601 compatible con PostgreSQL TIMESTAMPTZ
            val now = Date()
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val expiresAt = Date(now.time + 24 * 60 * 60 * 1000) // +24 horas
            
            // Solo enviar los campos requeridos - dejar que Supabase genere id, created_at
            val storyData = mapOf(
                "user_id" to userId,
                "media_url" to publicUrl,
                "media_type" to "photo",
                "expires_at" to isoFormat.format(expiresAt)
            )
            
            Log.d(TAG, "=== INSERTANDO STORY EN SUPABASE ===")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Media URL: $publicUrl")
            Log.d(TAG, "Expires At: ${isoFormat.format(expiresAt)}")
            Log.d(TAG, "Data completa: $storyData")
            
            // Insertar en Supabase - CRÍTICO: no silenciar errores
            val insertResult = runCatching {
                SupabaseClient.database
                    .from("stories")
                    .insert(storyData)
            }
            
            if (insertResult.isFailure) {
                val dbError = insertResult.exceptionOrNull()
                Log.e(TAG, "═══════════════════════════════════════════")
                Log.e(TAG, "ERROR CRÍTICO insertando en Supabase")
                Log.e(TAG, "Mensaje: ${dbError?.message}")
                Log.e(TAG, "Causa: ${dbError?.cause?.message}")
                Log.e(TAG, "═══════════════════════════════════════════")
                dbError?.printStackTrace()
                // Re-lanzar el error para que el usuario sepa que falló
                throw Exception("Error guardando story: ${dbError?.message}")
            }
            
            Log.d(TAG, "✓ Story insertada exitosamente en Supabase")
            
            // Pequeño delay para asegurar que Supabase haya procesado la inserción
            kotlinx.coroutines.delay(500)
            
            // Refrescar lista de stories ANTES de marcar como completado
            // para que la UI tenga los datos cuando detecte el cambio
            loadMyStories()
            
            Log.d(TAG, "✓ Stories recargadas después de inserción")
            
            _uploadState.value = StoryUploadState(
                isUploading = false,
                progress = 1f,
                isComplete = true
            )
            onProgress(1f)
            
            // Crear objeto Story para retorno
            val uploadedStory = Story(
                id = UUID.randomUUID().toString(),
                userId = userId,
                mediaUrl = publicUrl,
                mediaType = "photo",
                createdAt = isoFormat.format(now),
                expiresAt = isoFormat.format(expiresAt)
            )
            
            Log.d(TAG, "=== STORY PUBLICADA EXITOSAMENTE ===")
            Result.success(uploadedStory)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading story", e)
            _uploadState.value = StoryUploadState(
                isUploading = false,
                error = e.message
            )
            Result.failure(e)
        }
    }
    
    suspend fun loadMyStories() = withContext(Dispatchers.IO) {
        try {
            // Primero limpiar stories expiradas
            cleanExpiredStories()
            
            val userId = getCurrentUserId()
            Log.d(TAG, "=== CARGANDO MIS STORIES ===")
            Log.d(TAG, "User ID: $userId")
            
            val allStories = SupabaseClient.database
                .from("stories")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Story>()
            
            Log.d(TAG, "Stories encontradas en DB: ${allStories.size}")
            allStories.forEach { story ->
                Log.d(TAG, "  - ID: ${story.id}, URL: ${story.mediaUrl.take(50)}..., Expires: ${story.expiresAt}")
            }
            
            // Filtrar solo stories que no han expirado (24 horas)
            val now = Date()
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val activeStories = allStories.filter { story ->
                try {
                    val expiresAt = isoFormat.parse(story.expiresAt)
                    expiresAt != null && expiresAt.after(now)
                } catch (e: Exception) {
                    true // Si hay error de parsing, mantener la story
                }
            }.sortedByDescending { it.createdAt }
            
            _myStories.value = activeStories
            Log.d(TAG, "✓ Stories activas (filtradas por 24h): ${activeStories.size}")
            activeStories.forEach { story ->
                Log.d(TAG, "  - Story activa: ${story.id}, URL: ${story.mediaUrl.take(50)}...")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading stories: ${e.message}", e)
        }
    }
    
    // Limpiar stories que han expirado (más de 24 horas)
    suspend fun cleanExpiredStories() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== LIMPIANDO STORIES EXPIRADAS ===")
            
            val now = Date()
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val nowFormatted = isoFormat.format(now)
            
            // Obtener todas las stories expiradas
            val expiredStories = SupabaseClient.database
                .from("stories")
                .select {
                    filter {
                        lt("expires_at", nowFormatted)
                    }
                }
                .decodeList<Story>()
            
            Log.d(TAG, "Stories expiradas encontradas: ${expiredStories.size}")
            
            if (expiredStories.isEmpty()) {
                Log.d(TAG, "No hay stories expiradas para eliminar")
                return@withContext
            }
            
            // Eliminar cada story expirada de Supabase
            expiredStories.forEach { story ->
                try {
                    Log.d(TAG, "Eliminando story expirada: ${story.id}")
                    
                    // Eliminar de Supabase
                    SupabaseClient.database
                        .from("stories")
                        .delete {
                            filter {
                                eq("id", story.id)
                            }
                        }
                    
                    // El archivo remoto puede limpiarse luego con una tarea backend si se requiere.
                    Log.d(TAG, "✓ Story ${story.id} eliminada de Supabase")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error eliminando story ${story.id}: ${e.message}")
                }
            }
            
            Log.d(TAG, "=== LIMPIEZA COMPLETADA: ${expiredStories.size} stories eliminadas ===")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando stories expiradas: ${e.message}", e)
        }
    }
    
    fun resetUploadState() {
        _uploadState.value = StoryUploadState()
    }
    
    /**
     * Cargar stories de otros usuarios (no del usuario actual)
     */
    suspend fun loadOtherUsersStories() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== CARGANDO STORIES DE OTROS USUARIOS ===")
            
            val currentUserId = try { getCurrentUserId() } catch (e: Exception) {
                Log.w(TAG, "No se pudo obtener userId, no se cargan stories de otros")
                null
            }
            
            // Sin userId no podemos filtrar nuestras stories — salir
            if (currentUserId == null) {
                _otherUsersStories.value = emptyList()
                return@withContext
            }
            
            val now = Date()
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val nowFormatted = isoFormat.format(now)
            
            // Obtener todas las stories activas (no expiradas)
            val allStories = SupabaseClient.database
                .from("stories")
                .select {
                    filter {
                        gt("expires_at", nowFormatted)
                    }
                }
                .decodeList<Story>()
            
            Log.d(TAG, "Stories activas encontradas: ${allStories.size}")
            
            // Filtrar stories que NO son del usuario actual
            val otherStories = allStories.filter { it.userId != currentUserId }
            Log.d(TAG, "Filtradas stories propias (userId=$currentUserId), quedan: ${otherStories.size}")
            
            // Filter out stories from users who have hidden us
            val hiddenByUsers = try {
                SupabaseClient.database
                    .from("story_hidden_users")
                    .select { filter { eq("hidden_user_id", currentUserId) } }
                    .decodeList<StoryHiddenEntry>()
                    .map { it.userId }
                    .toSet()
            } catch (_: Exception) { emptySet() }
            
            val visibleStories = if (hiddenByUsers.isNotEmpty()) {
                otherStories.filter { it.userId !in hiddenByUsers }
            } else otherStories
            
            Log.d(TAG, "Stories visibles (tras filtro ocultos): ${visibleStories.size}")
            
            // Obtener info de usuarios para cada story
            val storiesWithUsers = mutableListOf<StoryWithUser>()
            val userIds = visibleStories.map { it.userId }.distinct()
            
            for (userId in userIds) {
                try {
                    val user = SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeSingleOrNull<com.rendly.app.data.model.Usuario>()
                    
                    if (user != null) {
                        val userStories = visibleStories.filter { it.userId == userId }
                        userStories.forEach { story ->
                            storiesWithUsers.add(
                                StoryWithUser(
                                    story = story,
                                    userId = userId,
                                    username = user.username,
                                    avatarUrl = user.avatarUrl
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error obteniendo usuario $userId: ${e.message}")
                }
            }
            
            // Ordenar por fecha de creación (más recientes primero)
            val sortedStories = storiesWithUsers.sortedByDescending { it.story.createdAt }
            
            _otherUsersStories.value = sortedStories
            Log.d(TAG, "✓ Stories de otros usuarios cargadas: ${sortedStories.size}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando stories de otros usuarios: ${e.message}", e)
            _otherUsersStories.value = emptyList()
        }
    }
    
    // Data class para viewers de una story
    data class StoryViewer(
        val viewerId: String,
        val username: String,
        val avatarUrl: String?,
        val viewedAt: String
    )
    
    /**
     * Registrar una vista de story en Supabase
     */
    suspend fun recordStoryView(storyId: String) = withContext(Dispatchers.IO) {
        try {
            val viewerId = getCurrentUserId()
            
            // Insertar vista usando buildJsonObject
            val viewData = kotlinx.serialization.json.buildJsonObject {
                put("story_id", kotlinx.serialization.json.JsonPrimitive(storyId))
                put("viewer_id", kotlinx.serialization.json.JsonPrimitive(viewerId))
            }
            
            // Intentar insertar (ignorar si ya existe)
            try {
                SupabaseClient.database
                    .from("story_views")
                    .insert(viewData)
                
                Log.d(TAG, "✓ Vista registrada para story: $storyId")
            } catch (e: Exception) {
                // Probablemente ya existe la vista, ignorar
                Log.d(TAG, "Vista ya existente o error: ${e.message}")
            }
            
            // Actualizar contador de views en la story
            val viewsCount = SupabaseClient.database
                .from("story_views")
                .select {
                    filter { eq("story_id", storyId) }
                }
                .decodeList<Map<String, String>>()
                .size
            
            val updateData = kotlinx.serialization.json.buildJsonObject {
                put("views", kotlinx.serialization.json.JsonPrimitive(viewsCount))
            }
            
            SupabaseClient.database
                .from("stories")
                .update(updateData) {
                    filter { eq("id", storyId) }
                }
            
            Log.d(TAG, "✓ Contador de vistas actualizado: $viewsCount")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando vista: ${e.message}", e)
        }
    }
    
    /**
     * Obtener lista de viewers de una story
     */
    suspend fun getStoryViewers(storyId: String): List<StoryViewer> = withContext(Dispatchers.IO) {
        try {
            // Obtener vistas de la story
            val views = SupabaseClient.database
                .from("story_views")
                .select {
                    filter { eq("story_id", storyId) }
                }
                .decodeList<StoryViewDB>()
            
            // Obtener info de cada viewer
            val viewers = mutableListOf<StoryViewer>()
            for (view in views) {
                try {
                    val user = SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { eq("user_id", view.viewerId) }
                        }
                        .decodeSingleOrNull<com.rendly.app.data.model.Usuario>()
                    
                    if (user != null) {
                        viewers.add(
                            StoryViewer(
                                viewerId = view.viewerId,
                                username = user.username,
                                avatarUrl = user.avatarUrl,
                                viewedAt = view.viewedAt
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error obteniendo viewer ${view.viewerId}: ${e.message}")
                }
            }
            
            viewers.sortedByDescending { it.viewedAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo viewers: ${e.message}", e)
            emptyList()
        }
    }
    
    @kotlinx.serialization.Serializable
    private data class StoryViewDB(
        val id: String = "",
        @kotlinx.serialization.SerialName("story_id")
        val storyId: String = "",
        @kotlinx.serialization.SerialName("viewer_id")
        val viewerId: String = "",
        @kotlinx.serialization.SerialName("viewed_at")
        val viewedAt: String = ""
    )
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // LIKES DE STORIES - Persistencia en Supabase
    // ═══════════════════════════════════════════════════════════════════════════════
    
    // Cache local de likes del usuario actual
    private val _likedStoryIds = MutableStateFlow<Set<String>>(emptySet())
    val likedStoryIds: StateFlow<Set<String>> = _likedStoryIds.asStateFlow()
    
    /**
     * Cargar los likes del usuario actual desde Supabase
     */
    suspend fun loadMyLikes() = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()
            
            val likes = SupabaseClient.database
                .from("story_likes")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<StoryLikeDB>()
            
            _likedStoryIds.value = likes.map { it.storyId }.toSet()
            Log.d(TAG, "✓ Likes cargados: ${_likedStoryIds.value.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando likes: ${e.message}", e)
        }
    }
    
    /**
     * Toggle like en una story - persiste en Supabase
     */
    suspend fun toggleStoryLike(storyId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId()
            val isCurrentlyLiked = storyId in _likedStoryIds.value
            
            if (isCurrentlyLiked) {
                // Quitar like
                SupabaseClient.database
                    .from("story_likes")
                    .delete {
                        filter {
                            eq("story_id", storyId)
                            eq("user_id", userId)
                        }
                    }
                
                _likedStoryIds.value = _likedStoryIds.value - storyId
                
                // Decrementar contador en la story
                updateStoryLikesCount(storyId, -1)
                
                Log.d(TAG, "✓ Like removido de story: $storyId")
                false
            } else {
                // Agregar like
                val likeData = kotlinx.serialization.json.buildJsonObject {
                    put("story_id", kotlinx.serialization.json.JsonPrimitive(storyId))
                    put("user_id", kotlinx.serialization.json.JsonPrimitive(userId))
                }
                
                SupabaseClient.database
                    .from("story_likes")
                    .insert(likeData)
                
                _likedStoryIds.value = _likedStoryIds.value + storyId
                
                // Incrementar contador en la story
                updateStoryLikesCount(storyId, 1)
                
                Log.d(TAG, "✓ Like agregado a story: $storyId")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like: ${e.message}", e)
            storyId in _likedStoryIds.value // Retornar estado actual
        }
    }
    
    /**
     * Verificar si una story está liked por el usuario actual
     */
    fun isStoryLiked(storyId: String): Boolean {
        return storyId in _likedStoryIds.value
    }
    
    /**
     * Actualizar contador de likes en la story
     */
    private suspend fun updateStoryLikesCount(storyId: String, delta: Int) {
        try {
            // Obtener likes actuales
            val story = SupabaseClient.database
                .from("stories")
                .select {
                    filter { eq("id", storyId) }
                }
                .decodeSingleOrNull<Story>()
            
            val currentLikes = story?.likes ?: 0
            val newLikes = (currentLikes + delta).coerceAtLeast(0)
            
            val updateData = kotlinx.serialization.json.buildJsonObject {
                put("likes", kotlinx.serialization.json.JsonPrimitive(newLikes))
            }
            
            SupabaseClient.database
                .from("stories")
                .update(updateData) {
                    filter { eq("id", storyId) }
                }
            
            Log.d(TAG, "✓ Contador de likes actualizado: $newLikes")
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando contador de likes: ${e.message}", e)
        }
    }
    
    /**
     * Registrar un forward de story
     */
    suspend fun recordStoryForward(storyId: String) = withContext(Dispatchers.IO) {
        try {
            // Obtener forwards actuales
            val story = SupabaseClient.database
                .from("stories")
                .select {
                    filter { eq("id", storyId) }
                }
                .decodeSingleOrNull<Story>()
            
            val currentForwards = story?.forwarded ?: 0
            val newForwards = currentForwards + 1
            
            val updateData = kotlinx.serialization.json.buildJsonObject {
                put("forwarded", kotlinx.serialization.json.JsonPrimitive(newForwards))
            }
            
            SupabaseClient.database
                .from("stories")
                .update(updateData) {
                    filter { eq("id", storyId) }
                }
            
            Log.d(TAG, "✓ Forward registrado para story: $storyId (total: $newForwards)")
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando forward: ${e.message}", e)
        }
    }
    
    /**
     * Eliminar una story de Supabase
     */
    suspend fun deleteStory(storyId: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("stories")
                .delete {
                    filter { eq("id", storyId) }
                }
            
            // Actualizar lista local
            _myStories.value = _myStories.value.filter { it.id != storyId }
            
            Log.d(TAG, "✓ Story eliminada: $storyId")
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando story: ${e.message}", e)
        }
    }
    
    @kotlinx.serialization.Serializable
    private data class StoryLikeDB(
        val id: String = "",
        @kotlinx.serialization.SerialName("story_id")
        val storyId: String = "",
        @kotlinx.serialization.SerialName("user_id")
        val userId: String = ""
    )
}

