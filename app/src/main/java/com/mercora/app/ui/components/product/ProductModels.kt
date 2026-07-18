package com.mercora.app.ui.components.product

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class ProductPageUserData(
    val username: String = "",
    @kotlinx.serialization.SerialName("avatar_url") val avatarUrl: String? = null,
    @kotlinx.serialization.SerialName("is_verified") val isVerified: Boolean = false
)

@Serializable
data class ProductReviewRatingRow(
    val rating: Int = 0
)

data class ProductDataV2(
    val title: String,
    val price: Double,
    val description: String,
    val images: List<String>,
    val condition: String,
    val category: String,
    val username: String,
    val userAvatar: String,
    val storeName: String?,
    val likesCount: Int,
    val reviewsCount: Int,
    val isNew: Boolean,
    val warranty: String = "Sin GarantÃ­a",
    val returnsAccepted: Boolean = false,
    val freeShipping: Boolean = false,
    val colors: List<String> = emptyList()
)

data class RatingDistribution(
    val fiveStars: Int = 0,
    val fourStars: Int = 0,
    val threeStars: Int = 0,
    val twoStars: Int = 0,
    val oneStar: Int = 0
) {
    val total: Int get() = fiveStars + fourStars + threeStars + twoStars + oneStar

    val averageRating: Float get() {
        if (total == 0) return 0f
        val weightedSum = (fiveStars * 5 + fourStars * 4 + threeStars * 3 + twoStars * 2 + oneStar * 1).toFloat()
        return weightedSum / total
    }

    fun percentFor(stars: Int): Float {
        if (total == 0) return 0f
        val count = when (stars) {
            5 -> fiveStars
            4 -> fourStars
            3 -> threeStars
            2 -> twoStars
            1 -> oneStar
            else -> 0
        }
        return count.toFloat() / total
    }
}
