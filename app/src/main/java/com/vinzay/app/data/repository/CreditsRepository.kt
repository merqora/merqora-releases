package com.vinzay.app.data.repository

import android.util.Log
import com.vinzay.app.data.model.CreditPack
import com.vinzay.app.data.model.CreditTransaction
import com.vinzay.app.data.model.UserCreditsRow
import com.vinzay.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

object CreditsRepository {
    private const val TAG = "CreditsRepository"

    private val _balance = MutableStateFlow(0)
    val balance: StateFlow<Int> = _balance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _transactions = MutableStateFlow<List<CreditTransaction>>(emptyList())
    val transactions: StateFlow<List<CreditTransaction>> = _transactions.asStateFlow()

    fun getCurrentBalance(): Int = _balance.value

    suspend fun fetchCredits(): Int = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext 0

            val row = SupabaseClient.database
                .from("user_credits")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<UserCreditsRow>()

            val balance = row?.balance ?: 0
            _balance.value = balance
            Log.d(TAG, "Credits fetched: $balance")
            balance
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching credits: ${e.message}")
            0
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun addCredits(amount: Int, description: String, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext false

            val now = OffsetDateTime.now().toString()

            val existing = SupabaseClient.database
                .from("user_credits")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<UserCreditsRow>()

            if (existing != null) {
                SupabaseClient.database
                    .from("user_credits")
                    .update(CreditsUpdate(
                        balance = existing.balance + amount,
                        totalEarned = existing.totalEarned + amount,
                        updatedAt = now
                    )) { filter { eq("user_id", userId) } }
            } else {
                SupabaseClient.database
                    .from("user_credits")
                    .insert(CreditsInsert(
                        userId = userId,
                        balance = amount,
                        totalEarned = amount,
                        totalSpent = 0,
                        updatedAt = now
                    ))
            }

            SupabaseClient.database
                .from("credit_transactions")
                .insert(CreditTxInsert(
                    userId = userId,
                    amount = amount,
                    type = "purchase",
                    referenceId = referenceId,
                    description = description,
                    createdAt = now
                ))

            _balance.value += amount
            Log.d(TAG, "Added $amount credits: $description")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding credits: ${e.message}")
            false
        }
    }

    suspend fun deductCredits(amount: Int, description: String, referenceId: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext false

            val existing = SupabaseClient.database
                .from("user_credits")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<UserCreditsRow>()
                ?: return@withContext false

            if (existing.balance < amount) return@withContext false

            val now = OffsetDateTime.now().toString()

            SupabaseClient.database
                .from("user_credits")
                .update(CreditsUpdate(
                    balance = existing.balance - amount,
                    totalSpent = existing.totalSpent + amount,
                    updatedAt = now
                )) { filter { eq("user_id", userId) } }

            SupabaseClient.database
                .from("credit_transactions")
                .insert(CreditTxInsert(
                    userId = userId,
                    amount = -amount,
                    type = "redeem",
                    referenceId = referenceId,
                    description = description,
                    createdAt = now
                ))

            _balance.value -= amount
            Log.d(TAG, "Deducted $amount credits: $description")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deducting credits: ${e.message}")
            false
        }
    }

    suspend fun getTransactionHistory(): List<CreditTransaction> = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()

            val rows = SupabaseClient.database
                .from("credit_transactions")
                .select { filter { eq("user_id", userId) }; order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING); limit(50) }
                .decodeList<CreditTransaction>()

            _transactions.value = rows
            rows
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching transactions: ${e.message}")
            emptyList()
        }
    }

    suspend fun createCreditOrder(creditPack: CreditPack): Result<CreditOrderResult> {
        val userId = SupabaseClient.auth.currentUserOrNull()?.id
            ?: return Result.failure(Exception("Usuario no autenticado"))

        val totalCredits = creditPack.credits + creditPack.bonusCredits
        val orderId = UUID.randomUUID().toString()

        Log.d(TAG, "Credit order reference: $orderId for $totalCredits credits")
        return Result.success(CreditOrderResult(orderId, creditPack.priceUyu, totalCredits, creditPack.name))
    }

    suspend fun createSingleItemOrder(itemTitle: String, priceUyu: Double, itemId: String): Result<SingleItemOrderResult> {
        val userId = SupabaseClient.auth.currentUserOrNull()?.id
            ?: return Result.failure(Exception("Usuario no autenticado"))

        val orderId = UUID.randomUUID().toString()

        Log.d(TAG, "Single item order reference: $orderId for $itemTitle")
        return Result.success(SingleItemOrderResult(orderId, priceUyu, itemTitle, itemId))
    }

    // ---- Serializable DTOs ----

    @Serializable
    private data class CreditsUpdate(
        val balance: Int,
        @SerialName("total_earned") val totalEarned: Int = 0,
        @SerialName("total_spent") val totalSpent: Int = 0,
        @SerialName("updated_at") val updatedAt: String
    )

    @Serializable
    private data class CreditsInsert(
        @SerialName("user_id") val userId: String,
        val balance: Int,
        @SerialName("total_earned") val totalEarned: Int,
        @SerialName("total_spent") val totalSpent: Int,
        @SerialName("updated_at") val updatedAt: String
    )

    @Serializable
    private data class CreditTxInsert(
        @SerialName("user_id") val userId: String,
        val amount: Int,
        val type: String,
        @SerialName("reference_id") val referenceId: String? = null,
        val description: String? = null,
        @SerialName("created_at") val createdAt: String
    )

    // ---- Public result classes ----

    data class CreditOrderResult(
        val orderId: String,
        val totalAmount: Double,
        val creditsToAdd: Int,
        val creditPackName: String
    )

    data class SingleItemOrderResult(
        val orderId: String,
        val totalAmount: Double,
        val itemTitle: String,
        val itemId: String
    )
}
