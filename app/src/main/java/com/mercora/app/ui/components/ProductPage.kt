package com.mercora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mercora.app.data.model.Post
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.CartRepository
import com.mercora.app.data.repository.CommentRepository
import com.mercora.app.ui.components.product.*
import com.mercora.app.ui.theme.*
import io.github.jan.supabase.gotrue.auth

@Composable
fun ProductPage(
    post: Post?,
    isVisible: Boolean,
    isFromRend: Boolean = false,
    relatedPosts: List<Post> = emptyList(),
    suggestedPosts: List<Post> = emptyList(),
    onDismiss: () -> Unit,
    onBuyNow: (Post) -> Unit = {},
    onAddToCart: (Post) -> Unit = {},
    onContactSeller: (Post) -> Unit = {},
    onShare: (Post) -> Unit = {},
    onFavorite: (Post) -> Unit = {},
    onLike: (Post) -> Unit = {},
    onSave: (Post) -> Unit = {},
    onForward: (Post) -> Unit = {},
    onViewAllReviews: (Post) -> Unit = {},
    onRelatedPostClick: (Post) -> Unit = {},
    onViewProfile: (Post) -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val viewModel: ProductPageViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var showFullscreenGallery by remember { mutableStateOf(false) }

    LaunchedEffect(post) {
        post?.let { p ->
            viewModel.init(p, isFromRend)
            try {
                scrollState.scrollTo(0)
            } catch (_: Exception) {
                // Layout might not be ready yet, animate instead
                scrollState.animateScrollTo(0)
            }
        }
    }

    val commentsFromRepo by CommentRepository.comments.collectAsState()
    val isCommentsLoading by CommentRepository.isLoading.collectAsState()

    val productData = uiState.productData
    val availableColors = remember(productData?.colors) {
        ProductPageViewModel.getAvailableColors(productData?.colors)
    }

    if (isVisible && post != null) {
        productData?.let { data ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(HomeBg)
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProductSearchHeader(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onBack = onDismiss,
                        onCartClick = { viewModel.setShowCartModal(true) }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        ProductTopInfo(
                            isNew = data.isNew,
                            reviewsCount = uiState.localReviewsCount,
                            avgRating = uiState.realRatingDistribution.averageRating
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = data.title.ifEmpty { "Producto Premium" },
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProductBadgesRow(category = data.category)

                        Spacer(modifier = Modifier.height(12.dp))

                        ProductImageGalleryV2(
                            images = data.images,
                            title = data.title,
                            selectedIndex = selectedImageIndex,
                            onPageChange = { selectedImageIndex = it },
                            onShowFullscreen = { showFullscreenGallery = true }
                        )

                        ProductActionsRow(
                            images = data.images,
                            isLiked = uiState.isLiked,
                            likesCount = data.likesCount,
                            isSaved = uiState.isSaved,
                            savesCount = post.savesCount,
                            sharesCount = post.sharesCount,
                            selectedImageIndex = selectedImageIndex,
                            onImageSelect = { selectedImageIndex = it },
                            onLike = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleLike()
                                onLike(post)
                            },
                            onForward = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setShowForwardModal(true)
                            },
                            onSave = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleSaved()
                                onSave(post)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProductColorsSection(
                            colors = availableColors,
                            selectedIndex = uiState.selectedColorIndex,
                            onColorSelect = { viewModel.selectColor(it) },
                            images = data.images,
                            onImageChange = { imageIndex ->
                                if (imageIndex < data.images.size) {
                                    selectedImageIndex = imageIndex
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ProductPriceSection(
                            price = data.price,
                            originalPrice = post.previousPrice
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ProductActionButtons(
                            onBuyNow = {
                                val p = post ?: return@ProductActionButtons
                                CartRepository.clearCart()
                                CartRepository.addToCart(p)
                                onBuyNow(p)
                            },
                            onAddToCart = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.addToCart()
                                onAddToCart(post)
                            },
                            onContact = { viewModel.setShowConsultModal(true) },
                            isOwnProduct = post?.userId == SupabaseClient.auth.currentUserOrNull()?.id,
                            onEditPost = { viewModel.setShowEditPostModal(true) },
                            onSharePost = { viewModel.setShowShareModal(true) },
                            onViewStats = { viewModel.setShowStatsScreen(true) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SectionDivider()

                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = BorderSubtle
                        )

                        SellerSectionV2(
                            username = data.username,
                            avatarUrl = data.userAvatar,
                            storeName = data.storeName,
                            isVerified = uiState.isSellerVerified,
                            sellerStats = uiState.sellerStats,
                            onViewProfile = { onViewProfile(post) }
                        )

                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = BorderSubtle
                        )

                        SectionDivider()

                        ProductFeaturesCompact(
                            freeShipping = data.freeShipping,
                            isVerified = uiState.isSellerVerified,
                            returnsAccepted = data.returnsAccepted
                        )

                        SectionDivider()

                        ProductDetailsSection(
                            condition = data.condition,
                            category = data.category,
                            warranty = data.warranty,
                            returnsAccepted = data.returnsAccepted
                        )

                        SectionDivider()

                        ProductGalleryWithSKU(
                            images = data.images,
                            postId = post.id,
                            onViewAllImages = { showFullscreenGallery = true },
                            onReport = { viewModel.setShowReportDialog(true) }
                        )

                        SectionDivider()

                        if (showFullscreenGallery) {
                            FullscreenGalleryModal(
                                images = data.images,
                                title = data.title,
                                onDismiss = { showFullscreenGallery = false }
                            )
                        }

                        PaymentMethodsSection(sellerId = post.userId)

                        SectionDivider()

                        ReviewsSection(
                            ratingDistribution = uiState.realRatingDistribution,
                            onViewAll = { viewModel.setShowCommentsSheet(true) }
                        )

                        SectionDivider()

                        if (relatedPosts.isNotEmpty()) {
                            RelatedProductsSection(
                                posts = relatedPosts,
                                onPostClick = onRelatedPostClick
                            )
                            SectionDivider()
                        }

                        YouMightLikeSectionInfinite(
                            currentPostId = post.id,
                            onPostClick = onRelatedPostClick
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // MODALS Y SCREENS
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

                ShareModal(
                    isVisible = uiState.showShareModal,
                    post = post,
                    onDismiss = { viewModel.setShowShareModal(false) },
                    onInternalShare = { viewModel.setShowForwardModal(true) }
                )

                PostStatsScreen(
                    isVisible = uiState.showStatsScreen,
                    post = post,
                    onDismiss = { viewModel.setShowStatsScreen(false) }
                )

                EditPostModal(
                    isVisible = uiState.showEditPostModal,
                    post = post,
                    onDismiss = { viewModel.setShowEditPostModal(false) },
                    onSave = { _ ->
                        viewModel.setShowEditPostModal(false)
                    },
                    onDelete = {
                        viewModel.setShowEditPostModal(false)
                        onDismiss()
                    }
                )

                ForwardModal(
                    isVisible = uiState.showForwardModal,
                    post = post,
                    onDismiss = { viewModel.setShowForwardModal(false) },
                    onForwardToUser = { _, _, _ -> viewModel.setShowForwardModal(false) }
                )

                CommentsSheet(
                    isVisible = uiState.showCommentsSheet,
                    comments = commentsFromRepo.map { c ->
                        Comment(
                            id = c.id,
                            userId = c.userId,
                            username = c.username,
                            avatarUrl = c.avatarUrl,
                            text = c.text,
                            timeAgo = c.createdAt.take(10),
                            likes = c.likes,
                            isLiked = c.isLiked,
                            rating = c.rating,
                            replies = c.replies.map { r ->
                                Comment(
                                    id = r.id,
                                    userId = r.userId,
                                    username = r.username,
                                    avatarUrl = r.avatarUrl,
                                    text = r.text,
                                    timeAgo = r.createdAt.take(10),
                                    likes = r.likes,
                                    isLiked = r.isLiked,
                                    rating = r.rating,
                                    isVerified = r.isVerified
                                )
                            },
                            replyCount = c.replyCount,
                            isVerified = c.isVerified
                        )
                    },
                    onDismiss = { viewModel.dismissComments() },
                    onSendComment = { text, rating ->
                        viewModel.sendReview(text, rating)
                    },
                    onLikeComment = { commentId ->
                        viewModel.likeComment(commentId)
                    },
                    onReplyComment = { parentId, text ->
                        viewModel.replyToComment(parentId, text)
                    },
                    onDeleteComment = { commentId ->
                        viewModel.deleteComment(commentId)
                    },
                    isLoading = isCommentsLoading,
                    currentUserAvatar = uiState.currentUserAvatar,
                    currentUsername = uiState.currentUsername,
                    currentUserId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                )

                CartModal(
                    isVisible = uiState.showCartModal,
                    onDismiss = { viewModel.setShowCartModal(false) },
                    onCheckout = { viewModel.setShowCartModal(false) },
                    onContinueShopping = { viewModel.setShowCartModal(false) },
                    onNavigateToCheckout = {
                        viewModel.setShowCartModal(false)
                        onNavigateToCheckout()
                    }
                )

                ConsultModal(
                    isVisible = uiState.showConsultModal,
                    post = post,
                    onDismiss = { viewModel.setShowConsultModal(false) },
                    onSendConsult = { message ->
                        viewModel.sendConsult(message)
                    }
                )

                if (uiState.showReportDialog) {
                    ReportProductDialog(
                        sku = (post.id).take(8).uppercase(),
                        postId = post.id,
                        postTitle = data.title,
                        postImage = data.images.firstOrNull() ?: "",
                        onDismiss = { viewModel.setShowReportDialog(false) },
                        onReport = { reportDescription ->
                            viewModel.submitReport(reportDescription)
                            viewModel.setShowReportDialog(false)
                        }
                    )
                }
            }
        }
    }
}
