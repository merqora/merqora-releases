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

// ─── Modelos DB ───────────────────────────────────────────────────

@Serializable
data class OfferCampaignDB(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String? = null,
    val icon: String = "local_offer",
    @SerialName("banner_gradient_start") val bannerGradientStart: String = "#FF6B35",
    @SerialName("banner_gradient_end") val bannerGradientEnd: String = "#0A3D62",
    @SerialName("max_discount") val maxDiscount: Int = 50,
    @SerialName("starts_at") val startsAt: String = "",
    @SerialName("ends_at") val endsAt: String = "",
    @SerialName("is_active") val isActive: Boolean = true,
    val priority: Int = 0
)

@Serializable
data class OfferItemDB(
    val id: String = "",
    @SerialName("campaign_id") val campaignId: String = "",
    @SerialName("post_id") val postId: String = "",
    @SerialName("discount_percent") val discountPercent: Int = 0,
    @SerialName("original_price") val originalPrice: Double? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false
)

// ─── Modelos UI ───────────────────────────────────────────────────

data class OfferCampaign(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val icon: String,
    val bannerGradientStart: String,
    val bannerGradientEnd: String,
    val maxDiscount: Int,
    val startsAt: String,
    val endsAt: String,
    val priority: Int,
    val items: List<OfferProduct> = emptyList()
)

data class OfferProduct(
    val id: String,
    val postId: String,
    val discountPercent: Int,
    val isFeatured: Boolean,
    val exploreItem: ExploreItem,
    val originalPrice: Double,
    val offerPrice: Double
)

// ─── Repositorio ──────────────────────────────────────────────────

object OffersRepository {
    private const val TAG = "OffersRepository"

    private val _campaigns = MutableStateFlow<List<OfferCampaign>>(emptyList())
    val campaigns: StateFlow<List<OfferCampaign>> = _campaigns.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCampaign = MutableStateFlow<OfferCampaign?>(null)
    val selectedCampaign: StateFlow<OfferCampaign?> = _selectedCampaign.asStateFlow()

    private var isDataLoaded = false

    fun selectCampaign(campaign: OfferCampaign) {
        _selectedCampaign.value = campaign
    }

    suspend fun loadOffers(forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (isDataLoaded && !forceRefresh && _campaigns.value.isNotEmpty()) {
            Log.d(TAG, "Using cached offers: ${_campaigns.value.size} campaigns")
            return@withContext
        }

        try {
            _isLoading.value = true
            Log.d(TAG, "Loading offers from Supabase...")

            // 1. Cargar campañas activas
            val campaignsDB = try {
                SupabaseClient.database
                    .from("offer_campaigns")
                    .select()
                    .decodeList<OfferCampaignDB>()
                    .filter { it.isActive }
                    .sortedByDescending { it.priority }
            } catch (e: Exception) {
                Log.w(TAG, "No offer_campaigns table or empty: ${e.message}")
                emptyList()
            }

            if (campaignsDB.isEmpty()) {
                Log.d(TAG, "No active campaigns, generating from explore items")
                generateFallbackOffers()
                return@withContext
            }

            Log.d(TAG, "Loaded ${campaignsDB.size} active campaigns")

            // 2. Cargar items de oferta
            val allOfferItems = try {
                SupabaseClient.database
                    .from("offer_items")
                    .select()
                    .decodeList<OfferItemDB>()
            } catch (e: Exception) {
                Log.w(TAG, "Error loading offer_items: ${e.message}")
                emptyList()
            }

            Log.d(TAG, "Loaded ${allOfferItems.size} offer items")

            // 3. Cargar posts y usuarios para los items
            val postIds = allOfferItems.map { it.postId }.distinct()
            val postsMap = mutableMapOf<String, PostDB>()
            val usersMap = mutableMapOf<String, ExploreUserProfile>()

            for (postId in postIds) {
                try {
                    val post = SupabaseClient.database
                        .from("posts")
                        .select { filter { eq("id", postId) } }
                        .decodeSingleOrNull<PostDB>()
                    if (post != null) {
                        postsMap[postId] = post
                    }
                } catch (_: Exception) {}
            }

            val userIds = postsMap.values.map { it.userId }.distinct()
            for (userId in userIds) {
                try {
                    val user = SupabaseClient.database
                        .from("usuarios")
                        .select { filter { eq("user_id", userId) } }
                        .decodeSingleOrNull<ExploreUserProfile>()
                    if (user != null) usersMap[userId] = user
                } catch (_: Exception) {}
            }

            // 4. Armar campañas con productos
            val campaigns = campaignsDB.map { campaign ->
                val campaignItems = allOfferItems.filter { it.campaignId == campaign.id }
                val products = campaignItems.mapNotNull { item ->
                    val post = postsMap[item.postId] ?: return@mapNotNull null
                    val user = usersMap[post.userId]
                    val reputation = ((post.likesCount.coerceAtMost(100) + post.reviewsCount.coerceAtMost(50)) / 1.5).toInt().coerceIn(50, 100)

                    val originalPrice = item.originalPrice ?: (post.price * 100.0 / (100 - item.discountPercent))
                    val offerPrice = originalPrice * (1 - item.discountPercent / 100.0)

                    OfferProduct(
                        id = item.id,
                        postId = item.postId,
                        discountPercent = item.discountPercent,
                        isFeatured = item.isFeatured,
                        exploreItem = ExploreItem(
                            id = post.id,
                            userId = post.userId,
                            title = post.title.ifBlank { "Producto" },
                            price = offerPrice,
                            images = post.images,
                            category = post.category?.lowercase() ?: "",
                            likesCount = post.likesCount,
                            reviewsCount = post.reviewsCount,
                            username = user?.username ?: "usuario",
                            userAvatar = user?.avatarUrl ?: "",
                            storeName = user?.nombreTienda,
                            isVerified = user?.isVerified ?: false,
                            reputationPercent = reputation
                        ),
                        originalPrice = originalPrice,
                        offerPrice = offerPrice
                    )
                }

                OfferCampaign(
                    id = campaign.id,
                    name = campaign.name,
                    slug = campaign.slug,
                    description = campaign.description,
                    icon = campaign.icon,
                    bannerGradientStart = campaign.bannerGradientStart,
                    bannerGradientEnd = campaign.bannerGradientEnd,
                    maxDiscount = campaign.maxDiscount,
                    startsAt = campaign.startsAt,
                    endsAt = campaign.endsAt,
                    priority = campaign.priority,
                    items = products
                )
            }

            _campaigns.value = campaigns
            if (_selectedCampaign.value == null && campaigns.isNotEmpty()) {
                _selectedCampaign.value = campaigns.first()
            }
            isDataLoaded = true
            Log.d(TAG, "Offers loaded: ${campaigns.size} campaigns, total items: ${campaigns.sumOf { it.items.size }}")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading offers", e)
            generateFallbackOffers()
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Fallback: genera ofertas a partir de los items del ExploreRepository
     * cuando no hay tabla offer_campaigns o está vacía.
     */
    private fun generateFallbackOffers() {
        val exploreItems = ExploreRepository.exploreItems.value
        if (exploreItems.isEmpty()) {
            _campaigns.value = emptyList()
            isDataLoaded = true
            return
        }

        val slugs = listOf(
            Triple("flash", "Flash Sale", 70),
            Triple("today", "Solo Hoy", 50),
            Triple("week", "Esta Semana", 40),
            Triple("clearance", "Liquidación", 60)
        )
        val gradients = listOf(
            "#FF6B35" to "#0A3D62",
            "#0A3D62" to "#2E8B57",
            "#11998E" to "#38EF7D",
            "#FF6B35" to "#2E8B57"
        )
        val icons = listOf("flash_on", "today", "date_range", "local_fire_department")

        val shuffled = exploreItems.shuffled()
        val chunks = shuffled.chunked((shuffled.size / 4).coerceAtLeast(2))

        val campaigns = slugs.mapIndexed { index, (slug, name, maxDiscount) ->
            val itemsChunk = chunks.getOrElse(index) { emptyList() }
            val (gradStart, gradEnd) = gradients[index]

            OfferCampaign(
                id = slug,
                name = name,
                slug = slug,
                description = null,
                icon = icons[index],
                bannerGradientStart = gradStart,
                bannerGradientEnd = gradEnd,
                maxDiscount = maxDiscount,
                startsAt = "",
                endsAt = "",
                priority = 4 - index,
                items = itemsChunk.mapIndexed { i, item ->
                    val discount = (maxDiscount - 20 + (0..20).random()).coerceIn(10, maxDiscount)
                    val origPrice = item.price * 100.0 / (100 - discount)
                    OfferProduct(
                        id = "${slug}_$i",
                        postId = item.id,
                        discountPercent = discount,
                        isFeatured = i < 3,
                        exploreItem = item.copy(price = item.price),
                        originalPrice = origPrice,
                        offerPrice = item.price
                    )
                }
            )
        }

        _campaigns.value = campaigns
        if (_selectedCampaign.value == null && campaigns.isNotEmpty()) {
            _selectedCampaign.value = campaigns.first()
        }
        isDataLoaded = true
        Log.d(TAG, "Fallback offers generated: ${campaigns.size} campaigns")
    }

    fun clearCache() {
        isDataLoaded = false
        _campaigns.value = emptyList()
        _selectedCampaign.value = null
    }
}
