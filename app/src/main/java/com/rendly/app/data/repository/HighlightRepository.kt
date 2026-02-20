package com.rendly.app.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.rendly.app.data.model.Highlight
import com.rendly.app.data.model.HighlightStory
import com.rendly.app.data.remote.CloudflareService
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
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
            Log.d(TAG, "=== INICIANDO CREACIÓN DE HIGHLIGHT ===")
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
                    onProgress = { progress -> onProgress(0.2f + progress * 0.6f) }
                )
                
                if (result.isSuccess) {
                    coverUrl = result.getOrNull()
                    Log.d(TAG, "✅ Portada subida exitosamente: $coverUrl")
                } else {
                    Log.e(TAG, "❌ Error subiendo portada: ${result.exceptionOrNull()?.message}")
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
                Log.e(TAG, "❌ ERROR AL INSERTAR HIGHLIGHT EN SUPABASE:", error)
                Log.e(TAG, "Error message: ${error?.message}")
                Log.e(TAG, "Error type: ${error?.javaClass?.simpleName}")
                throw error ?: Exception("Error desconocido al insertar highlight")
            }
            
            Log.d(TAG, "✅ Highlight insertado exitosamente en Supabase!")
            onProgress(1f)
            
            // Recargar lista
            Log.d(TAG, "Recargando lista de highlights...")
            loadHighlights()
            
            Log.d(TAG, "✅ HIGHLIGHT CREADO COMPLETAMENTE: $highlightId")
            Result.success(highlight)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR CRÍTICO AL CREAR HIGHLIGHT", e)
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
                    folder = "highlights/stories"
                ).getOrThrow()
            } else {
                throw Exception("Se requiere imagen o URL")
            }
            
            // Obtener posición actual
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
            // Eliminar stories del highlight primero
            SupabaseClient.database
                .from("highlight_stories")
                .delete {
                    filter {
                        eq("highlight_id", highlightId)
                    }
                }
            
            // Eliminar highlight
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
