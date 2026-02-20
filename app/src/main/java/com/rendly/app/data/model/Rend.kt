package com.rendly.app.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RendDB(
    val id: String = "",
    @SerialName("user_id") val userId: String,
    val title: String = "",
    val description: String? = null,
    @SerialName("video_url") val videoUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("product_title") val productTitle: String? = null,
    @SerialName("product_price") val productPrice: Double? = null,
    @SerialName("product_link") val productLink: String? = null,
    @SerialName("product_image") val productImage: String? = null,
    val duration: Int = 0, // seconds
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    @SerialName("views_count") val viewsCount: Int = 0,
    @SerialName("shares_count") val sharesCount: Int = 0,
    @SerialName("saves_count") val savesCount: Int? = 0,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("product_id") val productId: String? = null
)

@Immutable
data class Rend(
    val id: String,
    val userId: String,
    val title: String = "",
    val description: String? = null,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val productTitle: String? = null,
    val productPrice: Double? = null,
    val productLink: String? = null,
    val productImage: String? = null,
    val duration: Int = 0,
    val likesCount: Int = 0,
    val reviewsCount: Int = 0,
    val viewsCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    
    val createdAt: String = "",
    
    // User data
    val username: String = "",
    val userAvatar: String = "",
    val userStoreName: String? = null,
    
    // Local state
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    
    // Identificador unificado de producto
    val productId: String? = null
) {
    /**
     * Obtiene la mejor imagen disponible para el producto anclado.
     * Prioridad: productImage > thumbnailUrl
     */
    fun getProductImageUrl(): String? {
        return productImage?.takeIf { it.isNotEmpty() }
            ?: thumbnailUrl?.takeIf { it.isNotEmpty() }
    }
    
    companion object {
        fun fromDB(
            db: RendDB,
            username: String = "usuario",
            avatarUrl: String = "",
            storeName: String? = null
        ): Rend {
            return Rend(
                id = db.id,
                userId = db.userId,
                title = db.title,
                description = db.description,
                videoUrl = db.videoUrl,
                thumbnailUrl = db.thumbnailUrl,
                productTitle = db.productTitle,
                productPrice = db.productPrice,
                productLink = db.productLink,
                productImage = db.productImage,
                duration = db.duration,
                likesCount = db.likesCount,
                reviewsCount = db.reviewsCount,
                viewsCount = db.viewsCount,
                sharesCount = db.sharesCount,
                savesCount = db.savesCount ?: 0,
                username = username,
                userAvatar = avatarUrl,
                userStoreName = storeName,
                productId = db.productId
            )
        }
    }
}
