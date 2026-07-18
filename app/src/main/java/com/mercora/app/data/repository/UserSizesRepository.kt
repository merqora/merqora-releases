package com.mercora.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class UserSizesDB(
    @SerialName("user_id") val userId: String = "",
    @SerialName("gender") val gender: String = "Hombre",
    @SerialName("fit_preference") val fitPreference: String = "Regular",
    @SerialName("height_cm") val heightCm: String? = null,
    @SerialName("weight_kg") val weightKg: String? = null,
    @SerialName("chest_cm") val chestCm: String? = null,
    @SerialName("waist_cm") val waistCm: String? = null,
    @SerialName("hip_cm") val hipCm: String? = null,
    @SerialName("inseam_cm") val inseamCm: String? = null,
    @SerialName("tops_size") val topsSize: String? = null,
    @SerialName("bottoms_size") val bottomsSize: String? = null,
    @SerialName("dresses_size") val dressesSize: String? = null,
    @SerialName("outerwear_size") val outerwearSize: String? = null,
    @SerialName("shoes_size") val shoesSize: String? = null,
    @SerialName("underwear_size") val underwearSize: String? = null,
    @SerialName("swimwear_size") val swimwearSize: String? = null,
    @SerialName("accessories_size") val accessoriesSize: String? = null
)

object UserSizesRepository {
    private const val TAG = "UserSizesRepository"
    private const val TABLE = "user_sizes"
    private const val PREFS_NAME = "user_sizes_cache"
    private const val KEY_CACHED_SIZES = "cached_sizes"
    private const val KEY_CACHED_USER_ID = "cached_user_id"

    private var prefs: SharedPreferences? = null
    private val json = Json { ignoreUnknownKeys = true }

    // Cache en memoria para acceso instantÃ¡neo dentro de la misma sesiÃ³n
    @Volatile
    private var memoryCache: UserSizesDB? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Devuelve las tallas cacheadas (memoria â†’ disco) de forma instantÃ¡nea.
     * Retorna null si no hay cache para este usuario.
     */
    fun getCachedSizes(userId: String): UserSizesDB? {
        memoryCache?.let { if (it.userId == userId) return it }
        val p = prefs ?: return null
        if (p.getString(KEY_CACHED_USER_ID, null) != userId) return null
        val raw = p.getString(KEY_CACHED_SIZES, null) ?: return null
        return try {
            json.decodeFromString<UserSizesDB>(raw).also { memoryCache = it }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding cached sizes: ${e.message}")
            null
        }
    }

    private fun cacheSizes(sizes: UserSizesDB) {
        memoryCache = sizes
        try {
            prefs?.edit()
                ?.putString(KEY_CACHED_USER_ID, sizes.userId)
                ?.putString(KEY_CACHED_SIZES, json.encodeToString(UserSizesDB.serializer(), sizes))
                ?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error caching sizes: ${e.message}")
        }
    }

    fun clearCache() {
        memoryCache = null
        prefs?.edit()?.remove(KEY_CACHED_SIZES)?.remove(KEY_CACHED_USER_ID)?.apply()
    }

    suspend fun loadUserSizes(userId: String): UserSizesDB? = withContext(Dispatchers.IO) {
        try {
            val remote = SupabaseClient.database
                .from(TABLE)
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<UserSizesDB>()
            // Actualizar cache con lo Ãºltimo del servidor
            if (remote != null) cacheSizes(remote)
            remote
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user sizes: ${e.message}")
            // Sin red o error: devolver cache local si existe
            getCachedSizes(userId)
        }
    }

    suspend fun saveUserSizes(
        userId: String,
        gender: String,
        fitPreference: String,
        heightCm: String?,
        weightKg: String?,
        chestCm: String?,
        waistCm: String?,
        hipCm: String?,
        inseamCm: String?,
        sizes: Map<String, String>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = buildJsonObject {
                put("user_id", userId)
                put("gender", gender)
                put("fit_preference", fitPreference)
                if (!heightCm.isNullOrBlank()) put("height_cm", heightCm)
                if (!weightKg.isNullOrBlank()) put("weight_kg", weightKg)
                if (!chestCm.isNullOrBlank()) put("chest_cm", chestCm)
                if (!waistCm.isNullOrBlank()) put("waist_cm", waistCm)
                if (!hipCm.isNullOrBlank()) put("hip_cm", hipCm)
                if (!inseamCm.isNullOrBlank()) put("inseam_cm", inseamCm)
                sizes.forEach { (category, size) ->
                    val col = when (category) {
                        "tops" -> "tops_size"
                        "bottoms" -> "bottoms_size"
                        "dresses" -> "dresses_size"
                        "outerwear" -> "outerwear_size"
                        "shoes" -> "shoes_size"
                        "underwear" -> "underwear_size"
                        "swimwear" -> "swimwear_size"
                        "accessories" -> "accessories_size"
                        else -> null
                    }
                    if (col != null) put(col, size)
                }
                put("updated_at", java.time.Instant.now().toString())
            }

            // Upsert atÃ³mico: inserta o actualiza segÃºn user_id
            SupabaseClient.database
                .from(TABLE)
                .upsert(body, onConflict = "user_id")

            // Actualizar cache local inmediatamente para que al reabrir estÃ© todo listo
            cacheSizes(
                UserSizesDB(
                    userId = userId,
                    gender = gender,
                    fitPreference = fitPreference,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    chestCm = chestCm,
                    waistCm = waistCm,
                    hipCm = hipCm,
                    inseamCm = inseamCm,
                    topsSize = sizes["tops"],
                    bottomsSize = sizes["bottoms"],
                    dressesSize = sizes["dresses"],
                    outerwearSize = sizes["outerwear"],
                    shoesSize = sizes["shoes"],
                    underwearSize = sizes["underwear"],
                    swimwearSize = sizes["swimwear"],
                    accessoriesSize = sizes["accessories"]
                )
            )
            Log.d(TAG, "User sizes saved successfully for userId: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user sizes: ${e.message}")
            false
        }
    }
}
