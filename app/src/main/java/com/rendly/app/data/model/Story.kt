package com.rendly.app.data.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class Story(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("media_url")
    val mediaUrl: String = "",
    @SerialName("media_type")
    val mediaType: String = "photo",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("expires_at")
    val expiresAt: String = "",
    val views: Int = 0,
    // Columnas opcionales - pueden no existir en la tabla hasta ejecutar SUPABASE_STORIES_LIKES_FORWARDS.sql
    val likes: Int? = 0,
    val forwarded: Int? = 0,
    @SerialName("is_highlight")
    val isHighlight: Boolean = false
) {
    // Propiedades calculadas para compatibilidad con UI
    val shares: Int get() = forwarded ?: 0
    val newFollowers: Int get() = 0 // Será implementado cuando se agregue tracking de seguidores
}

data class StoryUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val isComplete: Boolean = false,
    val error: String? = null
)

data class PostUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val isComplete: Boolean = false,
    val error: String? = null
)
