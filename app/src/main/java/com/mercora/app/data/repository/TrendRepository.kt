package com.mercora.app.data.repository

import android.util.Log
import com.mercora.app.data.model.PostDB
import com.mercora.app.data.model.RendDB
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class TrendingPostItem(
    val id: String,
    val rank: Int,
    val title: String,
    val hashtag: String,
    val viewCount: Int,
    val likesCount: Int,
    val growthPercent: Int,
    val thumbnailUrl: String,
    val category: String,
    val username: String,
    val userAvatar: String,
    val isHot: Boolean = false
)

data class TrendingTagItem(
    val id: String,
    val hashtag: String,
    val totalViews: Int,
    val videoCount: Int,
    val thumbnails: List<String>
)

object TrendRepository {
    private const val TAG = "TrendRepository"

    private val _trendingPosts = MutableStateFlow<List<TrendingPostItem>>(emptyList())
    val trendingPosts: StateFlow<List<TrendingPostItem>> = _trendingPosts.asStateFlow()

    private val _trendingTags = MutableStateFlow<List<TrendingTagItem>>(emptyList())
    val trendingTags: StateFlow<List<TrendingTagItem>> = _trendingTags.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mapa de categorías de posts a categorías de tendencias
    private val categoryMap = mapOf(
        "Ropa" to "fashion",
        "Calzado" to "fashion",
        "Accesorios" to "fashion",
        "Electrónica" to "tech",
        "Tecnología" to "tech",
        "Belleza" to "beauty",
        "Salud" to "beauty",
        "Hogar" to "home",
        "Muebles" to "home",
        "Deportes" to "sports",
        "Fitness" to "sports"
    )

    suspend fun loadTrending() = withContext(Dispatchers.IO) {
        if (_isLoading.value) return@withContext
        try {
            _isLoading.value = true

            // 1. Cargar RENDS más populares (por views + likes) - NO posts
            val popularRends = SupabaseClient.database
                .from("rends")
                .select {
                    order("views_count", Order.DESCENDING)
                    limit(20)
                }
                .decodeList<RendDB>()

            // 2. Cargar datos de usuarios
            val userIds = popularRends.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, Usuario>()
            for (uid in userIds) {
                try {
                    val user = SupabaseClient.database
                        .from("usuarios")
                        .select { filter { eq("user_id", uid) } }
                        .decodeSingleOrNull<Usuario>()
                    if (user != null) usersMap[uid] = user
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading user $uid: ${e.message}")
                }
            }

            // 3. Mapear a TrendingPostItem con hashtags reales de cada rend
            val trendItems = popularRends.mapIndexed { index, rend ->
                val user = usersMap[rend.userId]
                val trendCategory = categoryMap[rend.category] ?: "fashion"
                // Usar el primer hashtag real del rend, o generar uno basado en descripción
                val firstTag = if (rend.hashtags.isNotEmpty()) {
                    val tag = rend.hashtags.first()
                    if (tag.startsWith("#")) tag else "#$tag"
                } else {
                    "#trending"
                }

                val finalThumbnail = if (rend.videoUrl.contains("ik.imagekit.io")) {
                    "${rend.videoUrl}/ik-thumbnail.jpg"
                } else {
                    rend.productImage?.takeIf { it.isNotBlank() && !it.endsWith("/") }
                        ?: rend.videoUrl
                }
                
                TrendingPostItem(
                    id = rend.id,
                    rank = index + 1,
                    title = rend.description?.take(50) ?: "Rend",
                    hashtag = firstTag,
                    viewCount = rend.viewsCount,
                    likesCount = rend.likesCount,
                    growthPercent = if (rend.viewsCount > 0) {
                        ((rend.likesCount.toDouble() / rend.viewsCount.coerceAtLeast(1)) * 100).toInt().coerceIn(10, 500)
                    } else 10,
                    thumbnailUrl = finalThumbnail,
                    category = trendCategory,
                    username = user?.username ?: "usuario",
                    userAvatar = user?.avatarUrl ?: "",
                    isHot = index < 3
                )
            }
            _trendingPosts.value = trendItems

            // 4. Crear trending hashtags reales agrupando rends por sus hashtags
            val tagMap = mutableMapOf<String, MutableList<RendDB>>()
            for (rend in popularRends) {
                if (rend.hashtags.isNotEmpty()) {
                    // Agrupar por cada hashtag real del rend
                    for (hashtag in rend.hashtags) {
                        val cleanTag = hashtag.removePrefix("#").trim().lowercase()
                        if (cleanTag.isNotBlank()) {
                            tagMap.getOrPut(cleanTag) { mutableListOf() }.add(rend)
                        }
                    }
                }
            }

            val trendTags = tagMap.entries
                .filter { it.value.size >= 1 } // Al menos 1 video con ese hashtag
                .sortedByDescending { entry -> entry.value.sumOf { it.viewsCount } }
                .take(8)
                .mapIndexed { index, (tag, rends) ->
                    TrendingTagItem(
                        id = "tag_$index",
                        hashtag = "#$tag",
                        totalViews = rends.sumOf { it.viewsCount },
                        videoCount = rends.size,
                        thumbnails = rends.distinctBy { it.id }.take(3).map { rend ->
                            if (rend.videoUrl.contains("ik.imagekit.io")) {
                                "${rend.videoUrl}/ik-thumbnail.jpg"
                            } else rend.videoUrl
                        }
                    )
                }
            _trendingTags.value = trendTags

            Log.d(TAG, "Loaded ${trendItems.size} trending rends, ${trendTags.size} trending tags")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading trending: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
}
