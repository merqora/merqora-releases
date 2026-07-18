package com.mercora.app.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.mercora.app.data.model.Highlight
import com.mercora.app.data.model.HighlightStory
import com.mercora.app.data.remote.CloudflareService
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

object HighlightRepository {
    private const val TAG = "HighlightRepository"
    
    private val _highlights = MutableStateFlow<List<Highlight>>(emptyList())
    val highlights: StateFlow<List<Highlight>> = _highlights.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private suspend fun getCurrentUserId(): String {
        return SupabaseClient.auth.currentUserOrNull()?.id 
            ?: throw Exception("Usuario no autenticado")
    }
    
    suspend fun loadHighlights() = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val userId = getCurrentUserId()
            
            val highlightsList = SupabaseClient.database
                .from("highlights")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Highlight>()
                .sortedByDescending { it.createdAt }
            
            _highlights.value = highlightsList
            Log.d(TAG, "Loaded ${highlightsList.size} highlights")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading highlights", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createHighlight(
        title: String,
        category: String,
        coverBitmap: Bitmap?,
        frameStyle: String = "CLASSIC",
        frameColor: String = "CATEGORY",
        backgroundColor: String = "DEFAULT",
        icon: String = "Star",
        onProgress: (Float) -> Unit = {}
    ): Result<Highlight> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "=== INICIANDO CREACIÃ“N DE HIGHLIGHT ===")
            val userId = getCurrentUserId()
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Title: $title, Category: $category, FrameStyle: $frameStyle")
            
            // Subir cover a Cloudinary si existe
            var coverUrl: String? = null
            if (coverBitmap != null) {
                Log.d(TAG, "Subiendo imagen de portada a Cloudflare...")
                onProgress(0.2f)
                val result = CloudflareService.uploadImage(
                    bitmap = coverBitmap,
                    folder = "highlights/$userId",
                    mediaType = com.vinzay.app.media.MediaOptimizer.MediaType.HIGHLIGHT,
                    onProgress = { progress -> onProgress(0.2f + progress * 0.6f) }
                )
                
                if (result.isSuccess) {
                    coverUrl = result.getOrNull()
                    Log.d(TAG, "âœ… Portada subida exitosamente: $coverUrl")
                } else {
                    Log.e(TAG, "âŒ Error subiendo portada: ${result.exceptionOrNull()?.message}")
                }
            } else {
                Log.d(TAG, "No hay imagen de portada para subir")
            }
            
            onProgress(0.8f)
            
            // Crear highlight en Supabase
            val highlightId = UUID.randomUUID().toString()
            val now = java.time.Instant.now().toString()
            
            val highlight = Highlight(
                id = highlightId,
                userId = userId,
                title = title,
                coverUrl = coverUrl,
                category = category,
                frameStyle = frameStyle,
                frameColor = frameColor,
                backgroundColor = backgroundColor,
                icon = icon,
                storiesCount = 0,
                isNew = true,
                createdAt = now,
                updatedAt = now
            )
            
            Log.d(TAG, "Insertando highlight en Supabase...")
            Log.d(TAG, "Highlight data: id=$highlightId, userId=$userId, title=$title, category=$category")
            
            val insertResult = runCatching {
                SupabaseClient.database
                    .from("highlights")
                    .insert(highlight)
            }
            
            if (insertResult.isFailure) {
                val error = insertResult.exceptionOrNull()
                Log.e(TAG, "âŒ ERROR AL INSERTAR HIGHLIGHT EN SUPABASE:", error)
                Log.e(TAG, "Error message: ${error?.message}")
                Log.e(TAG, "Error type: ${error?.javaClass?.simpleName}")
                throw error ?: Exception("Error desconocido al insertar highlight")
            }
            
            Log.d(TAG, "âœ… Highlight insertado exitosamente en Supabase!")
            
            // Also insert the cover as the first highlight_story so it shows in StoriesViewer
            if (coverUrl != null) {
                val firstStory = HighlightStory(
                    id = UUID.randomUUID().toString(),
                    highlightId = highlightId,
                    storyId = null,
                    mediaUrl = coverUrl,
                    position = 0,
                    createdAt = now
                )
                SupabaseClient.database
                    .from("highlight_stories")
                    .insert(firstStory)
                
                // Update stories_count to 1
                val updateCount = buildJsonObject {
                    put("stories_count", 1)
                }
                SupabaseClient.database
                    .from("highlights")
                    .update(updateCount) {
                        filter { eq("id", highlightId) }
                    }
                Log.d(TAG, "âœ… Cover insertada como primera highlight_story")
            }
            
            onProgress(1f)
            
            // Recargar lista
            Log.d(TAG, "Recargando lista de highlights...")
            loadHighlights()
            
            Log.d(TAG, "âœ… HIGHLIGHT CREADO COMPLETAMENTE: $highlightId")
            Result.success(highlight)
        } catch (e: Exception) {
            Log.e(TAG, "âŒ ERROR CRÃTICO AL CREAR HIGHLIGHT", e)
            Log.e(TAG, "Stack trace:", e)
            Result.failure(e)
        }
    }
    
    suspend fun addStoryToHighlight(
        highlightId: String,
        storyId: String? = null,
        mediaBitmap: Bitmap?,
        mediaUrl: String? = null
    ): Result<HighlightStory> = withContext(Dispatchers.IO) {
        try {
            // Subir imagen si es nueva
            val finalMediaUrl = mediaUrl ?: if (mediaBitmap != null) {
                CloudflareService.uploadImage(
                    bitmap = mediaBitmap,
                    folder = "highlights/stories",
                    mediaType = com.vinzay.app.media.MediaOptimizer.MediaType.HIGHLIGHT
                ).getOrThrow()
            } else {
                throw Exception("Se requiere imagen o URL")
            }
            
            // Obtener posiciÃ³n actual
            val existingStories = SupabaseClient.database
                .from("highlight_stories")
                .select {
                    filter {
                        eq("highlight_id", highlightId)
                    }
                }
                .decodeList<HighlightStory>()
            
            val position = existingStories.size
            
            // Crear story en highlight
            val highlightStory = HighlightStory(
                id = UUID.randomUUID().toString(),
                highlightId = highlightId,
                storyId = storyId,
                mediaUrl = finalMediaUrl,
                position = position,
                createdAt = java.time.Instant.now().toString()
            )
            
            SupabaseClient.database
                .from("highlight_stories")
                .insert(highlightStory)
            
            // Actualizar contador del highlight
            SupabaseClient.database
                .from("highlights")
                .update({
                    set("stories_count", position + 1)
                    set("updated_at", java.time.Instant.now().toString())
                }) {
                    filter {
                        eq("id", highlightId)
                    }
                }
            
            loadHighlights()
            
            Result.success(highlightStory)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding story to highlight", e)
            Result.failure(e)
        }
    }
    
    suspend fun getHighlightStories(highlightId: String): List<HighlightStory> = withContext(Dispatchers.IO) {
        try {
            val stories = SupabaseClient.database
                .from("highlight_stories")
                .select {
                    filter {
                        eq("highlight_id", highlightId)
                    }
                }
                .decodeList<HighlightStory>()
                .sortedBy { it.position }
            
            Log.d(TAG, "Loaded ${stories.size} stories for highlight $highlightId")
            stories
        } catch (e: Exception) {
            Log.e(TAG, "Error loading highlight stories", e)
            emptyList()
        }
    }
    
    suspend fun deleteHighlight(highlightId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener todas las stories para borrar media de Cloudflare R2
            val stories = SupabaseClient.database
                .from("highlight_stories")
                .select {
                    filter {
                        eq("highlight_id", highlightId)
                    }
                }
                .decodeList<HighlightStory>()
            
            // 2. Obtener el highlight para borrar su cover
            val highlight = SupabaseClient.database
                .from("highlights")
                .select {
                    filter {
                        eq("id", highlightId)
                    }
                }
                .decodeSingleOrNull<Highlight>()
            
            // 3. Borrar media de Cloudflare R2 (best-effort)
            for (story in stories) {
                if (story.mediaUrl.isNotBlank()) {
                    try {
                        CloudflareService.deleteImage(story.mediaUrl)
                        Log.d(TAG, "âœ… Deleted story media from R2: ${story.mediaUrl}")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete story media from R2: ${e.message}")
                    }
                }
            }
            if (highlight?.coverUrl != null && highlight.coverUrl.isNotBlank()) {
                try {
                    CloudflareService.deleteImage(highlight.coverUrl)
                    Log.d(TAG, "âœ… Deleted cover from R2: ${highlight.coverUrl}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete cover from R2: ${e.message}")
                }
            }
            
            // 4. Eliminar stories del highlight de Supabase
            SupabaseClient.database
                .from("highlight_stories")
                .delete {
                    filter {
                        eq("highlight_id", highlightId)
                    }
                }
            
            // 5. Eliminar highlight de Supabase
            SupabaseClient.database
                .from("highlights")
                .delete {
                    filter {
                        eq("id", highlightId)
                    }
                }
            
            loadHighlights()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting highlight", e)
            Result.failure(e)
        }
    }
}
