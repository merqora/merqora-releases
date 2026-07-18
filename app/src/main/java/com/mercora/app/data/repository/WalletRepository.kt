package com.mercora.app.data.repository

import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object WalletRepository {

    // Estado observable del saldo
    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _transactions = MutableStateFlow<List<JsonObject>>(emptyList())
    val transactions: StateFlow<List<JsonObject>> = _transactions.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<JsonObject>>(emptyList())
    val withdrawals: StateFlow<List<JsonObject>> = _withdrawals.asStateFlow()

    /** Obtener o crear la wallet del usuario */
    suspend fun getWallet(userId: String): JsonObject? = withContext(Dispatchers.IO) {
        try {
            var wallet = SupabaseClient.database
                .from("wallets")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<JsonObject>()

            if (wallet == null) {
                wallet = SupabaseClient.database
                    .from("wallets")
                    .insert(buildJsonObject { put("user_id", userId) }) {
                        select()
                    }
                    .decodeSingleOrNull<JsonObject>()
            }

            wallet?.let { safeDouble(it["balance"]) }?.let { _walletBalance.value = it }
            wallet
        } catch (_: Exception) { null }
    }

    /** Obtener saldo actual */
    suspend fun refreshBalance(userId: String): Double = withContext(Dispatchers.IO) {
        try {
            val wallet = SupabaseClient.database
                .from("wallets")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<JsonObject>()

            val balance = safeDouble(wallet?.get("balance")) ?: 0.0
            _walletBalance.value = balance
            balance
        } catch (_: Exception) { _walletBalance.value }
    }

    /**
     * âš ï¸ OBSOLETA: Ya NO se usa para pagos reales.
     * Con Split Payments de Mercado Pago, el dinero va directo a la cuenta
     * del vendedor. Merqora nunca custodia dinero.
     * 
     * Esta funciÃ³n se conserva solo como registro histÃ³rico (wallet local)
     * pero NO afecta el flujo real de fondos.
     */
    @Deprecated("Usar Split Payments de MP. El dinero va directo al vendedor.")
    suspend fun creditSeller(orderId: String, sellerId: String, amount: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            getWallet(sellerId)
            val current = SupabaseClient.database
                .from("wallets")
                .select(Columns.list("balance")) { filter { eq("user_id", sellerId) } }
                .decodeSingleOrNull<JsonObject>()
            val oldBalance = safeDouble(current?.get("balance")) ?: 0.0
            val newBalance = oldBalance + amount
            SupabaseClient.database
                .from("wallets")
                .update(buildJsonObject { put("balance", newBalance) }) {
                    filter { eq("user_id", sellerId) }
                }
            SupabaseClient.database
                .from("wallet_transactions")
                .insert(buildJsonObject {
                    put("wallet_user_id", sellerId)
                    put("type", "credit")
                    put("amount", amount)
                    put("balance_after", newBalance)
                    put("reference_type", "order")
                    put("reference_id", orderId)
                    put("description", "Pago recibido por orden $orderId")
                })
            _walletBalance.value = newBalance
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Obtener historial de transacciones */
    suspend fun loadTransactions(userId: String): List<JsonObject> = withContext(Dispatchers.IO) {
        try {
            val items = SupabaseClient.database
                .from("wallet_transactions")
                .select {
                    filter { eq("wallet_user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(50)
                }
                .decodeList<JsonObject>()
            _transactions.value = items
            items
        } catch (_: Exception) { emptyList() }
    }

    /** Obtener solicitudes de retiro */
    suspend fun loadWithdrawals(userId: String): List<JsonObject> = withContext(Dispatchers.IO) {
        try {
            val items = SupabaseClient.database
                .from("withdrawals")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(20)
                }
                .decodeList<JsonObject>()
            _withdrawals.value = items
            items
        } catch (_: Exception) { emptyList() }
    }

    /** Solicitar retiro */
    suspend fun requestWithdrawal(
        userId: String,
        amount: Double,
        method: String,
        destination: JsonObject
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Verificar saldo suficiente
            val wallet = SupabaseClient.database
                .from("wallets")
                .select(Columns.list("balance")) { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<JsonObject>()
            val balance = safeDouble(wallet?.get("balance")) ?: 0.0

            if (amount > balance) {
                return@withContext Result.failure(Exception("Saldo insuficiente"))
            }
            if (amount <= 0) {
                return@withContext Result.failure(Exception("Monto invÃ¡lido"))
            }

            val newBalance = balance - amount

            // Crear solicitud de retiro
            val result = SupabaseClient.database
                .from("withdrawals")
                .insert(buildJsonObject {
                    put("user_id", userId)
                    put("amount", amount)
                    put("method", method)
                    put("destination", destination)
                }) {
                    select()
                }
                .decodeSingleOrNull<JsonObject>()

            val withdrawalId = result?.get("id")?.toString()?.trim('"') ?: ""

            if (withdrawalId.isNotBlank()) {
                // Debitar de la wallet
                SupabaseClient.database
                    .from("wallets")
                    .update(buildJsonObject { put("balance", newBalance) }) {
                        filter { eq("user_id", userId) }
                    }

                // Registrar transacciÃ³n de dÃ©bito
                SupabaseClient.database
                    .from("wallet_transactions")
                    .insert(buildJsonObject {
                        put("wallet_user_id", userId)
                        put("type", "withdrawal")
                        put("amount", amount)
                        put("balance_after", newBalance)
                        put("reference_type", "withdrawal")
                        put("reference_id", withdrawalId)
                        put("description", "Retiro solicitado - \$${String.format("%.0f", amount)}")
                        put("status", "pending")
                    })

                _walletBalance.value = newBalance
                Result.success(withdrawalId)
            } else {
                Result.failure(Exception("Error al crear solicitud de retiro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun safeDouble(value: kotlinx.serialization.json.JsonElement?): Double? {
        val s = value?.toString()?.trim('"') ?: return null
        return s.toDoubleOrNull()
    }
}
