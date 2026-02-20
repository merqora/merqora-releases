package com.rendly.app.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* ---------------------------------------------------
   MODELO DB (Supabase)
--------------------------------------------------- */

@Serializable
data class PostDB(
    val id: String = "",
    @SerialName("user_id") val userId: String,

    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val category: String? = null,
    val condition: String? = null,

    val tags: List<String>? = null,
    val images: List<String> = emptyList(),

    val status: String = "active",

    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    @SerialName("views_count") val viewsCount: Int = 0,
    @SerialName("shares_count") val sharesCount: Int = 0,
    @SerialName("saves_count") val savesCount: Int = 0,

    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",

    @SerialName("is_collection") val isCollection: Boolean = false,
    @SerialName("cover_type") val coverType: String? = null,
    @SerialName("cover_image_index") val coverImageIndex: Int? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    
    // New columns
    @SerialName("allow_offers") val allowOffers: Boolean? = null,
    @SerialName("free_shipping") val freeShipping: Boolean? = null,
    @SerialName("previous_price") val previousPrice: Double? = null,
    val stock: Int? = null,
    
    // Additional product details
    val warranty: String? = null,
    @SerialName("returns_accepted") val returnsAccepted: Boolean? = null,
    val colors: List<String>? = null,
    
    // Identificador unificado de producto (para vincular opiniones de Posts y Rends)
    @SerialName("product_id") val productId: String? = null
)

// Modelo para INSERT - sin campos autogenerados por Supabase
@Serializable
data class PostInsert(
    @SerialName("user_id") val userId: String,
    val title: String = "",
    val description: String? = null,
    val images: List<String> = emptyList(),
    val status: String = "active",
    @SerialName("cover_url") val coverUrl: String? = null
)

/* ---------------------------------------------------
   MODELO UI (POST + USUARIO + ESTADOS LOCALES)
--------------------------------------------------- */

@Immutable
data class Post(
    val id: String,
    val userId: String,

    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val previousPrice: Double? = null,
    val category: String = "",
    val condition: String = "",

    val images: List<String> = emptyList(),

    val likesCount: Int = 0,
    val reviewsCount: Int = 0,
    val savesCount: Int = 0,
    val sharesCount: Int = 0,
    val createdAt: String = "",

    // Datos del usuario
    val username: String = "",
    val userAvatar: String = "",
    val userStoreName: String? = null,
    val isUserVerified: Boolean = false,

    // Estados locales (NO DB)
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val showStats: Boolean = false,
    
    // Additional product details
    val warranty: String? = null,
    val returnsAccepted: Boolean = false,
    val colors: List<String> = emptyList(),
    
    // Configuraciones de venta
    val allowOffers: Boolean = false,
    val freeShipping: Boolean = false,
    
    // Identificador unificado de producto
    val productId: String? = null
) {

    /* -------- Compatibilidad con UI vieja -------- */

    val likes: Int get() = likesCount
    val comments: Int get() = reviewsCount

    val producto: Producto
        get() = Producto(
            id = id,
            titulo = title,
            precio = price,
            imagenUrl = images,
            estado = condition,
            categoria = category,
            descripcion = description
        )

    companion object {
        fun fromDB(
            postDB: PostDB,
            username: String,
            avatarUrl: String,
            storeName: String? = null,
            overrideReviewsCount: Int? = null,
            isUserVerified: Boolean = false
        ): Post {
            return Post(
                id = postDB.id,
                userId = postDB.userId,
                title = postDB.title,
                description = postDB.description,
                price = postDB.price,
                previousPrice = postDB.previousPrice,
                category = postDB.category ?: "",
                condition = postDB.condition ?: "",
                images = postDB.images,
                likesCount = postDB.likesCount,
                reviewsCount = overrideReviewsCount ?: postDB.reviewsCount,
                savesCount = postDB.savesCount,
                sharesCount = postDB.sharesCount,
                createdAt = postDB.createdAt,
                username = username,
                userAvatar = avatarUrl,
                userStoreName = storeName,
                isUserVerified = isUserVerified,
                warranty = postDB.warranty,
                returnsAccepted = postDB.returnsAccepted ?: false,
                colors = postDB.colors ?: emptyList(),
                allowOffers = postDB.allowOffers ?: false,
                freeShipping = postDB.freeShipping ?: false,
                productId = postDB.productId
            )
        }
    }
}

/* ---------------------------------------------------
   MODELO PRODUCTO (LEGACY / COMPATIBILIDAD)
--------------------------------------------------- */

@Immutable
@Serializable
data class Producto(
    val id: String = "",
    val titulo: String = "",
    val precio: Double = 0.0,

    @SerialName("imagen_url")
    val imagenUrl: List<String> = emptyList(),

    val estado: String = "",
    val talle: String = "",
    val categoria: String = "",
    val condicion: String = "",
    val stock: Int = 0,
    val descripcion: String? = null
)

/* ---------------------------------------------------
   MODELO USUARIO
--------------------------------------------------- */

@Immutable
@Serializable
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,

    val bio: String? = null,
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,

    val isFollowing: Boolean = false
)
