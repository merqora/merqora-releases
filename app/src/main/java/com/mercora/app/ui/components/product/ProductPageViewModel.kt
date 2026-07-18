package com.mercora.app.ui.components.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.model.Post
import com.mercora.app.data.model.SellerStats
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.CartRepository
import com.mercora.app.data.repository.CommentRepository
import com.mercora.app.data.repository.NotificationRepository
import com.mercora.app.data.repository.OrderRepository
import com.mercora.app.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductPageUiState(
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val selectedColorIndex: Int = 0,
    val sellerStats: SellerStats? = null,
    val isSellerVerified: Boolean = false,
    val localReviewsCount: Int = 0,
    val realRatingDistribution: RatingDistribution = RatingDistribution(),
    val reviewsRefreshKey: Int = 0,
    val currentUserAvatar: String? = null,
    val currentUsername: String = "Usuario",
    val currentUserIsVerified: Boolean = false,
    val showCommentsSheet: Boolean = false,
    val showForwardModal: Boolean = false,
    val showCartModal: Boolean = false,
    val showReportDialog: Boolean = false,
    val showConsultModal: Boolean = false,
    val showShareModal: Boolean = false,
    val showStatsScreen: Boolean = false,
    val showEditPostModal: Boolean = false,
    val productData: ProductDataV2? = null
)

@HiltViewModel
class ProductPageViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProductPageUiState())
    val uiState: StateFlow<ProductPageUiState> = _uiState.asStateFlow()

    private var currentPost: Post? = null
    private var isFromRend: Boolean = false

    fun init(post: Post?, isFromRend: Boolean) {
        if (post == null) return

        val isSamePost = post.id == currentPost?.id
        currentPost = post
        this.isFromRend = isFromRend

        _uiState.update {
            it.copy(
                isLiked = post.isLiked,
                isSaved = post.isSaved,
                localReviewsCount = post.reviewsCount,
                isSellerVerified = post.isUserVerified
            )
        }

        computeProductData(post)
        if (!isSamePost) {
            loadSellerStats(post.userId)
            loadCurrentUser()
            loadReviewDistribution(post)
            
            // Incrementar vistas
            viewModelScope.launch {
                PostRepository.incrementPostView(post.id)
            }
        }
    }

    fun forceRefresh(post: Post) {
        currentPost = post
        computeProductData(post)
        loadSellerStats(post.userId)
        loadCurrentUser()
        loadReviewDistribution(post)
        _uiState.update {
            it.copy(
                isLiked = post.isLiked,
                isSaved = post.isSaved,
                localReviewsCount = post.reviewsCount,
                isSellerVerified = post.isUserVerified
            )
        }
    }

    private fun computeProductData(post: Post) {
        val data = ProductDataV2(
            title = post.title.ifEmpty { post.producto.titulo },
            price = post.price.takeIf { p -> p > 0 } ?: post.producto.precio,
            description = post.description ?: post.producto.descripcion ?: "",
            images = post.images.ifEmpty { post.producto.imagenUrl },
            condition = post.condition.ifEmpty { post.producto.condicion.ifEmpty { "Nuevo" } },
            category = post.category.ifEmpty { post.producto.categoria },
            username = post.username,
            userAvatar = post.userAvatar,
            storeName = post.userStoreName,
            likesCount = post.likesCount,
            reviewsCount = post.reviewsCount,
            isNew = post.createdAt.isNotEmpty(),
            warranty = post.warranty ?: "Sin GarantÃ­a",
            returnsAccepted = post.returnsAccepted,
            freeShipping = post.freeShipping,
            colors = post.colors
        )
        _uiState.update { it.copy(productData = data) }
    }

    fun toggleLike() {
        val post = currentPost ?: return
        _uiState.update { it.copy(isLiked = !it.isLiked) }
    }

    fun toggleSaved() {
        val post = currentPost ?: return
        _uiState.update { it.copy(isSaved = !it.isSaved) }
    }

    fun selectColor(index: Int) {
        _uiState.update { it.copy(selectedColorIndex = index) }
    }

    fun addToCart() {
        val post = currentPost ?: return
        val colors = _uiState.value.productData?.colors
        val colorMap = getAvailableColors(colors)
        val selectedColor = colorMap.getOrNull(_uiState.value.selectedColorIndex)?.first
        CartRepository.addToCart(
            post = post,
            quantity = 1,
            selectedColor = selectedColor
        )
    }

    fun setShowCommentsSheet(show: Boolean) {
        _uiState.update { it.copy(showCommentsSheet = show) }
        if (show) loadComments()
    }

    fun setShowForwardModal(show: Boolean) {
        _uiState.update { it.copy(showForwardModal = show) }
    }

    fun setShowCartModal(show: Boolean) {
        _uiState.update { it.copy(showCartModal = show) }
    }

    fun setShowReportDialog(show: Boolean) {
        _uiState.update { it.copy(showReportDialog = show) }
    }

    fun setShowConsultModal(show: Boolean) {
        _uiState.update { it.copy(showConsultModal = show) }
    }

    fun setShowShareModal(show: Boolean) {
        _uiState.update { it.copy(showShareModal = show) }
    }

    fun setShowStatsScreen(show: Boolean) {
        _uiState.update { it.copy(showStatsScreen = show) }
    }

    fun setShowEditPostModal(show: Boolean) {
        _uiState.update { it.copy(showEditPostModal = show) }
    }

    fun dismissComments() {
        _uiState.update { it.copy(showCommentsSheet = false, reviewsRefreshKey = it.reviewsRefreshKey + 1) }
        CommentRepository.clearComments()
        currentPost?.let { loadReviewDistribution(it) }
    }

    fun sendConsult(message: String) {
        val post = currentPost ?: return
        viewModelScope.launch {
            try {
                com.vinzay.app.data.repository.ChatRepository.sendConsultMessage(
                    sellerId = post.userId,
                    sellerUsername = post.username,
                    postId = post.id,
                    postTitle = post.title.ifEmpty { post.producto.titulo },
                    message = message
                )
            } catch (e: Exception) {
                Log.e("ProductPage", "Error sending consult: ${e.message}")
            }
        }
    }

    fun sendReview(text: String, rating: Int) {
        val post = currentPost ?: return
        viewModelScope.launch {
            try {
                val productId = resolveProductId(post)
                if (productId != null) {
                    val success = CommentRepository.addProductReview(
                        productId = productId,
                        sourceId = post.id,
                        sourceType = if (isFromRend) "rend" else "post",
                        text = text,
                        userAvatar = _uiState.value.currentUserAvatar,
                        userName = _uiState.value.currentUsername,
                        rating = rating,
                        isVerified = _uiState.value.currentUserIsVerified
                    )
                    if (success) {
                        NotificationRepository.createCommentNotification(
                            recipientId = post.userId,
                            postId = post.id,
                            postImage = post.images.firstOrNull(),
                            commentText = text
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductPage", "Error sending review: ${e.message}")
            }
        }
    }

    fun likeComment(commentId: String) {
        viewModelScope.launch {
            try {
                CommentRepository.likeProductReview(commentId)
            } catch (e: Exception) {
                Log.e("ProductPage", "Error liking comment: ${e.message}")
            }
        }
    }

    fun replyToComment(parentId: String, text: String) {
        val post = currentPost ?: return
        viewModelScope.launch {
            try {
                val productId = resolveProductId(post)
                if (productId != null) {
                    CommentRepository.addProductReviewReply(
                        productId = productId,
                        parentId = parentId,
                        sourceId = post.id,
                        sourceType = if (isFromRend) "rend" else "post",
                        text = text,
                        userAvatar = _uiState.value.currentUserAvatar,
                        userName = _uiState.value.currentUsername
                    )
                }
            } catch (e: Exception) {
                Log.e("ProductPage", "Error replying to comment: ${e.message}")
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                CommentRepository.deleteProductReview(commentId)
            } catch (e: Exception) {
                Log.e("ProductPage", "Error deleting comment: ${e.message}")
            }
        }
    }

    fun submitReport(reportDescription: String) {
        val post = currentPost ?: return
        viewModelScope.launch {
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                if (currentUserId != null) {
                    SupabaseClient.database
                        .from("content_reports")
                        .insert(
                            mapOf(
                                "reporter_id" to currentUserId,
                                "content_type" to "post",
                                "content_id" to post.id,
                                "reported_user_id" to post.userId,
                                "reason" to "inappropriate",
                                "description" to reportDescription,
                                "status" to "pending"
                            )
                        )
                    Log.d("ProductPage", "Report submitted to Supabase")
                }
            } catch (e: Exception) {
                Log.e("ProductPage", "Error submitting report: ${e.message}")
            }
        }
    }

    private fun loadSellerStats(sellerId: String) {
        viewModelScope.launch {
            try {
                val stats = OrderRepository.getSellerStats(sellerId)
                _uiState.update { it.copy(sellerStats = stats) }
            } catch (e: Exception) {
                Log.e("ProductPage", "Error loading seller stats: ${e.message}")
            }
        }
    }

    private fun loadCurrentUser() {
        val currentAuthUser = SupabaseClient.auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val userResult = SupabaseClient.database
                    .from("usuarios")
                    .select(
                        columns = io.github.jan.supabase.postgrest.query.Columns.list(
                            "username", "avatar_url", "is_verified"
                        )
                    ) {
                        filter { eq("user_id", currentAuthUser.id) }
                    }
                    .decodeSingleOrNull<ProductPageUserData>()
                userResult?.let { data ->
                    _uiState.update {
                        it.copy(
                            currentUsername = data.username.takeIf { u -> u.isNotBlank() } ?: "Usuario",
                            currentUserAvatar = data.avatarUrl,
                            currentUserIsVerified = data.isVerified
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductPage", "Error loading user: ${e.message}")
            }
        }
    }

    private fun loadReviewDistribution(post: Post) {
        viewModelScope.launch {
            try {
                val productId = resolveProductId(post) ?: return@launch
                val reviews = SupabaseClient.database
                    .from("product_reviews")
                    .select(
                        columns = io.github.jan.supabase.postgrest.query.Columns.list("rating")
                    ) {
                        filter { eq("product_id", productId) }
                    }
                    .decodeList<ProductReviewRatingRow>()

                val distribution = if (reviews.isNotEmpty()) {
                    var s5 = 0; var s4 = 0; var s3 = 0; var s2 = 0; var s1 = 0
                    reviews.forEach { r ->
                        when (r.rating) {
                            5 -> s5++
                            4 -> s4++
                            3 -> s3++
                            2 -> s2++
                            1 -> s1++
                        }
                    }
                    RatingDistribution(s5, s4, s3, s2, s1)
                } else {
                    RatingDistribution()
                }

                _uiState.update {
                    it.copy(
                        localReviewsCount = reviews.size,
                        realRatingDistribution = distribution
                    )
                }
            } catch (e: Exception) {
                Log.e("ProductPage", "Error loading review distribution: ${e.message}")
            }
        }
    }

    private fun loadComments() {
        val post = currentPost ?: return
        viewModelScope.launch {
            try {
                val productId = resolveProductId(post)
                if (productId != null) {
                    CommentRepository.loadProductReviews(productId)
                }
            } catch (e: Exception) {
                Log.e("ProductPage", "Error loading comments: ${e.message}")
            }
        }
    }

    private suspend fun resolveProductId(post: Post): String? {
        return post.productId
            ?: if (isFromRend) CommentRepository.getProductIdFromRendId(post.id)
            else CommentRepository.getProductIdFromPostId(post.id)
    }

    companion object {
        fun getAvailableColors(colors: List<String>?): List<Pair<String, androidx.compose.ui.graphics.Color>> {
            val colorMap = mapOf(
                "Negro" to androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                "Blanco" to androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                "Gris" to androidx.compose.ui.graphics.Color(0xFF6B7280),
                "Rojo" to androidx.compose.ui.graphics.Color(0xFFEF4444),
                "Azul" to androidx.compose.ui.graphics.Color(0xFF0A3D62),
                "Verde" to androidx.compose.ui.graphics.Color(0xFF2E8B57),
                "Amarillo" to androidx.compose.ui.graphics.Color(0xFFFF6B35),
                "Naranja" to androidx.compose.ui.graphics.Color(0xFFF97316),
                "Rosa" to androidx.compose.ui.graphics.Color(0xFF2E8B57),
                "Morado" to androidx.compose.ui.graphics.Color(0xFFFF6B35),
                "MarrÃ³n" to androidx.compose.ui.graphics.Color(0xFF92400E),
                "Beige" to androidx.compose.ui.graphics.Color(0xFFD4C4A8)
            )
            return if (!colors.isNullOrEmpty()) {
                colors.mapNotNull { colorName ->
                    colorMap[colorName]?.let { colorName to it }
                }
            } else {
                listOf(
                    "Negro" to androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    "Blanco" to androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    "Azul" to androidx.compose.ui.graphics.Color(0xFF0A3D62),
                    "Rojo" to androidx.compose.ui.graphics.Color(0xFFEF4444)
                )
            }
        }
    }
}
