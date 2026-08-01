package com.mercora.app.data.repository

import android.util.Log
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * Preferencias de medios de pago que un vendedor acepta en sus ventas.
 * - Cache en memoria por vendedor (ConcurrentHashMap) para pintado instantáneo en ProductPage
 * - StateFlow para las prefs propias (reactivo en tiempo real al editarlas)
 * - Upsert atómico contra Supabase (onConflict user_id)
 */
object SellerPaymentPrefsRepository {
    private const val TAG = "SellerPaymentPrefs"
    private const val TABLE = "seller_payment_prefs"

    @Serializable
    data class SellerPaymentPrefs(
        @SerialName("user_id") val userId: String = "",
        @SerialName("accepts_mercado_pago") val acceptsMercadoPago: Boolean = true,
        @SerialName("accepts_card") val acceptsCard: Boolean = true,
        @SerialName("accepts_bank_transfer") val acceptsBankTransfer: Boolean = true,
        @SerialName("accepts_cash") val acceptsCash: Boolean = false,
        @SerialName("accepts_prex") val acceptsPrex: Boolean = false,
        @SerialName("max_installments") val maxInstallments: Int = 12
    )

    /** Defaults cuando el vendedor nunca configuró sus medios de pago */
    val DEFAULTS = SellerPaymentPrefs()

    // Cache por vendedor: ProductPage pinta al instante en visitas repetidas
    private val sellerCache = ConcurrentHashMap<String, SellerPaymentPrefs>()

    // Prefs del usuario actual, reactivas para la pantalla de configuración
    private val _myPrefs = MutableStateFlow<SellerPaymentPrefs?>(null)
    val myPrefs: StateFlow<SellerPaymentPrefs?> = _myPrefs.asStateFlow()

    /** Lectura instantánea (sin red) del cache en memoria */
    fun peekCached(sellerId: String): SellerPaymentPrefs? = sellerCache[sellerId]

    /**
     * Prefs de un vendedor. Cache-first: devuelve memoria si existe,
     * sino consulta Supabase y cachea. forceRefresh salta el cache.
     */
    suspend fun getPrefs(sellerId: String, forceRefresh: Boolean = false): SellerPaymentPrefs =
        withContext(Dispatchers.IO) {
            if (!forceRefresh) {
                sellerCache[sellerId]?.let { return@withContext it }
            }
            try {
                val remote = SupabaseClient.database
                    .from(TABLE)
                    .select { filter { eq("user_id", sellerId) } }
                    .decodeSingleOrNull<SellerPaymentPrefs>()
                val prefs = remote ?: DEFAULTS.copy(userId = sellerId)
                sellerCache[sellerId] = prefs
                prefs
            } catch (e: Exception) {
                Log.e(TAG, "Error loading prefs for $sellerId: ${e.message}")
                sellerCache[sellerId] ?: DEFAULTS.copy(userId = sellerId)
            }
        }

    /** Carga las prefs del usuario actual en el StateFlow */
    suspend fun loadMyPrefs(): SellerPaymentPrefs? {
        val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return null
        val prefs = getPrefs(userId, forceRefresh = true)
        _myPrefs.value = prefs
        return prefs
    }

    /**
     * Guarda las prefs propias: optimistic update (flow + cache al instante)
     * y upsert en Supabase en background.
     */
    suspend fun saveMyPrefs(prefs: SellerPaymentPrefs): Boolean = withContext(Dispatchers.IO) {
        val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
        val toSave = prefs.copy(userId = userId)
        // Optimistic: la UI (config + ProductPage propio) refleja el cambio YA
        _myPrefs.value = toSave
        sellerCache[userId] = toSave
        try {
            SupabaseClient.database
                .from(TABLE)
                .upsert(toSave, onConflict = "user_id")
            Log.d(TAG, "Payment prefs saved for $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving payment prefs: ${e.message}")
            false
        }
    }
}
