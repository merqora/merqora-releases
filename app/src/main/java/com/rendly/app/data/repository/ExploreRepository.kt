package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.model.PostDB
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExploreUserProfile(
    @SerialName("user_id") val userId: String = "",
    val username: String = "",
    @SerialName("nombre_tienda") val nombreTienda: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val publicaciones: Int = 0,
    val seguidores: Int = 0,
    val clientes: Int = 0,
    val reputacion: Int = 0,
    @SerialName("is_verified") val isVerified: Boolean = false
)

data class ExploreItem(
    val id: String,
    val userId: String,
    val title: String,
    val price: Double,
    val images: List<String>,
    val category: String,
    val likesCount: Int,
    val reviewsCount: Int,
    val username: String,
    val userAvatar: String,
    val storeName: String?,
    val isVerified: Boolean,
    val reputationPercent: Int
)

object ExploreRepository {
    private const val TAG = "ExploreRepository"
    
    private val _exploreItems = MutableStateFlow<List<ExploreItem>>(emptyList())
    val exploreItems: StateFlow<List<ExploreItem>> = _exploreItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var isDataLoaded = false
    
    suspend fun loadExploreItems(forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        // Skip if already loaded and not forcing refresh
        if (isDataLoaded && !forceRefresh && _exploreItems.value.isNotEmpty()) {
            Log.d(TAG, "Using cached explore items: ${_exploreItems.value.size}")
            return@withContext
        }
        
        try {
            _isLoading.value = true
            Log.d(TAG, "Loading explore items from Supabase...")
            
            // 1. Load all posts
            val posts = SupabaseClient.database
                .from("posts")
                .select()
                .decodeList<PostDB>()
                .sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Loaded ${posts.size} posts")
            
            // 2. Get unique user IDs
            val userIds = posts.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, ExploreUserProfile>()
            
            // 3. Load user data for each unique user
            for (userId in userIds) {
                try {
                    val user = SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeSingleOrNull<ExploreUserProfile>()
                    
                    if (user != null) {
                        usersMap[userId] = user
                        Log.d(TAG, "✓ Usuario: ${user.username} para $userId")
                    } else {
                        Log.w(TAG, "⚠ Usuario NO encontrado: $userId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error cargando usuario $userId: ${e.message}")
                }
            }
            
            // 4. Combine posts with user data
            val items = posts.map { post ->
                val user = usersMap[post.userId]
                val reputation = ((post.likesCount.coerceAtMost(100) + post.reviewsCount.coerceAtMost(50)) / 1.5).toInt().coerceIn(50, 100)
                
                ExploreItem(
                    id = post.id,
                    userId = post.userId,
                    title = post.title.ifBlank { "Producto" },
                    price = post.price,
                    images = post.images,
                    category = post.category?.lowercase() ?: "",
                    likesCount = post.likesCount,
                    reviewsCount = post.reviewsCount,
                    username = user?.username ?: "usuario",
                    userAvatar = user?.avatarUrl ?: "",
                    storeName = user?.nombreTienda,
                    isVerified = user?.isVerified ?: false,
                    reputationPercent = reputation
                )
            }
            
            _exploreItems.value = items
            isDataLoaded = true
            Log.d(TAG, "Explore items loaded: ${items.size}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading explore data", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearCache() {
        isDataLoaded = false
        _exploreItems.value = emptyList()
    }
    
    /**
     * Returns a map of category ID -> count of posts for that category
     * Uses the loaded explore items to calculate counts
     */
    fun getCategoryCounts(): Map<String, Int> {
        val items = _exploreItems.value
        return items.groupBy { normalizeCategory(it.category) }
            .mapValues { it.value.size }
    }
    
    /**
     * Returns items filtered by category
     */
    fun getItemsByCategory(categoryId: String): List<ExploreItem> {
        return _exploreItems.value.filter { 
            normalizeCategory(it.category) == categoryId 
        }
    }
    
    /**
     * Normalizes category names to match CategoryDrawer IDs
     * Maps variations of category names to standard IDs
     */
    private fun normalizeCategory(category: String): String {
        val cat = category.lowercase().trim()
        return when {
            // Moda Mujer
            cat.contains("vestido") -> "vestidos"
            cat.contains("blusa") || cat.contains("top") -> "blusas"
            cat.contains("pantalon") && !cat.contains("hombre") -> "pantalones"
            cat.contains("falda") -> "faldas"
            cat.contains("abrigo") || cat.contains("chaqueta") -> "abrigos"
            cat.contains("zapato") && (cat.contains("mujer") || !cat.contains("hombre")) -> "zapatos_m"
            
            // Moda Hombre
            cat.contains("camisa") -> "camisas"
            cat.contains("pantalon") && cat.contains("hombre") -> "pantalones_h"
            cat.contains("chaqueta") && cat.contains("hombre") -> "chaquetas"
            cat.contains("traje") -> "trajes"
            cat.contains("zapato") && cat.contains("hombre") -> "zapatos_h"
            
            // Accesorios
            cat.contains("bolso") || cat.contains("cartera") -> "bolsos"
            cat.contains("joya") || cat.contains("joyeria") || cat.contains("collar") || cat.contains("anillo") -> "joyeria"
            cat.contains("reloj") -> "relojes"
            cat.contains("gafa") || cat.contains("lente") -> "gafas"
            cat.contains("sombrero") || cat.contains("gorra") -> "sombreros"
            
            // Hogar y Deco
            cat.contains("mueble") || cat.contains("silla") || cat.contains("mesa") -> "muebles"
            cat.contains("decoracion") || cat.contains("deco") -> "decoracion"
            cat.contains("planta") -> "plantas"
            cat.contains("iluminacion") || cat.contains("lampara") -> "iluminacion"
            
            // Electrónica
            cat.contains("smartphone") || cat.contains("celular") || cat.contains("telefono") -> "smartphones"
            cat.contains("laptop") || cat.contains("computador") || cat.contains("notebook") -> "laptops"
            cat.contains("audio") || cat.contains("auricular") || cat.contains("parlante") -> "audio"
            cat.contains("gaming") || cat.contains("consola") || cat.contains("videojuego") -> "gaming"
            
            // Ropa genérica
            cat.contains("ropa") -> "vestidos"
            
            // Default
            else -> cat.ifEmpty { "general" }
        }
    }
}
