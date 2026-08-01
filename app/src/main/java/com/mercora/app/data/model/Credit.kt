package com.mercora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreditsRow(
    @SerialName("user_id") val userId: String = "",
    val balance: Int = 0,
    @SerialName("total_earned") val totalEarned: Int = 0,
    @SerialName("total_spent") val totalSpent: Int = 0,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class CreditTransaction(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val amount: Int = 0,
    val type: String = "",
    @SerialName("reference_id") val referenceId: String? = null,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

data class CreditPack(
    val id: String,
    val name: String,
    val credits: Int,
    val bonusCredits: Int = 0,
    val priceUyu: Double,
    val priceUsd: Double,
    val isPopular: Boolean = false
)

val CREDIT_PACKS = listOf(
    CreditPack(
        id = "starter", name = "Starter", credits = 100,
        priceUyu = 1.0, priceUsd = 0.03
    ),
    CreditPack(
        id = "basic", name = "Básico", credits = 300,
        bonusCredits = 20, priceUyu = 99.0, priceUsd = 2.99,
        isPopular = true
    ),
    CreditPack(
        id = "pro", name = "Pro", credits = 500,
        bonusCredits = 50, priceUyu = 149.0, priceUsd = 4.99
    ),
    CreditPack(
        id = "premium", name = "Premium", credits = 1000,
        bonusCredits = 150, priceUyu = 279.0, priceUsd = 8.99
    ),
    CreditPack(
        id = "elite", name = "Elite", credits = 2500,
        bonusCredits = 500, priceUyu = 599.0, priceUsd = 19.99
    )
)
