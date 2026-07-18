package com.mercora.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mercora.app.data.model.Post
import com.mercora.app.data.model.Usuario
import com.mercora.app.ui.components.*
import com.mercora.app.ui.components.settings.HelpCenterScreen
import com.mercora.app.ui.components.settings.TermsAndConditionsScreen
import com.mercora.app.ui.components.settings.PrivacyPolicyScreen
import com.mercora.app.ui.screens.profile.UserProfileScreen
import com.mercora.app.ui.screens.search.SearchResultsScreen

/**
 * ModalsOverlay - Todos los modales y sheets del Home, extraÃ­dos de HomeContent.
 * 
 * Al estar en un composable separado:
 * - Recompone solo cuando sus props cambian, no cuando scrollea el feed
 * - Reduce el Ã¡rbol de composiciÃ³n de HomeContent ~40%
 * - Mejora skippability del LazyColumn principal
 */
@Composable
fun HomeModalsOverlay(
    // Comments Sheet
    showCommentsSheet: Boolean,
    mappedComments: List<Comment>,
    isCommentsLoading: Boolean,
    selectedPostForComments: Post?,
    onDismissComments: () -> Unit,
    onSendComment: (String, Int) -> Unit,
    onLikeComment: (String) -> Unit,
    onReplyComment: (String, String) -> Unit,
    onDeleteComment: (String) -> Unit,
    currentUserAvatar: String?,
    currentUsername: String,
    currentUserId: String?,
    // Consult Modal
    showConsultModal: Boolean,
    selectedPostForConsult: Post?,
    onDismissConsult: () -> Unit,
    onSendConsult: (String) -> Unit,
    // Forward Modal
    showForwardModal: Boolean,
    selectedPostForForward: Post?,
    onDismissForward: () -> Unit,
    onForwardToUser: (Usuario, Post, String) -> Unit,
    // Product Page
    showProductPage: Boolean,
    selectedPostForProduct: Post?,
    onDismissProduct: () -> Unit,
    relatedPosts: List<Post>,
    suggestedPosts: List<Post>,
    onNavigateToCheckout: () -> Unit,
    onOpenChatFromProfile: (Usuario) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    // Post Options Modal
    showPostOptionsModal: Boolean,
    selectedPostForOptions: Post?,
    isSelectedPostOwn: Boolean,
    onDismissOptions: () -> Unit,
    onEditPost: () -> Unit,
    onDeletePost: () -> Unit,
    onSharePost: () -> Unit,
    onSavePost: () -> Unit,
    onShowQrCode: () -> Unit,
    onReportPost: () -> Unit,
    onBlockUser: () -> Unit,
    onHidePost: () -> Unit,
    onUnfollowUser: () -> Unit,
    // Report Modal
    showReportModal: Boolean,
    reportPostId: String,
    reportUsername: String,
    onDismissReport: () -> Unit,
    onSubmitReport: (String, String?) -> Unit,
    // QR Code Modal
    showQrCodeModal: Boolean,
    qrCodePost: Post?,
    onDismissQrCode: () -> Unit,
    // Edit Post Modal
    showEditPostModal: Boolean,
    editingPost: Post?,
    onDismissEdit: () -> Unit,
    onSaveEdit: (Any) -> Unit,
    onDeleteEdit: () -> Unit,
    // Category Drawer
    showCategoryDrawer: Boolean,
    onDismissCategory: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onOpenHelpCenter: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    // Help/Legal screens
    showHelpCenter: Boolean,
    onDismissHelpCenter: () -> Unit,
    showTermsAndConditions: Boolean,
    onDismissTerms: () -> Unit,
    showPrivacyPolicy: Boolean,
    onDismissPrivacy: () -> Unit,
    // Cart Modal
    showCartModal: Boolean,
    onDismissCart: () -> Unit,
    onNavigateToCheckoutCart: () -> Unit,
    onOpenCategoriesCart: () -> Unit,
    onOpenExploreCart: () -> Unit,
    onCartVisibilityChange: (Boolean) -> Unit,
    // Story Viewers Modal
    showStoryViewersModal: Boolean,
    selectedStoryForViewers: String,
    onDismissStoryViewers: () -> Unit,
    onViewerClick: (String) -> Unit,
    // User Profile
    showUserProfile: Boolean,
    selectedUserId: String?,
    onDismissUserProfile: () -> Unit,
    onPostClickProfile: (Post) -> Unit,
    onOpenChatFromUserProfile: (Usuario) -> Unit,
    // Search Results
    showSearchResults: Boolean,
    searchResultsQuery: String,
    onDismissSearchResults: () -> Unit,
    onProductClickSearch: (Post) -> Unit,
    onClearSearchQuery: () -> Unit
) {
    // Category Drawer
    CategoryDrawer(
        isVisible = showCategoryDrawer,
        onDismiss = onDismissCategory,
        onCategorySelected = onCategorySelected,
        onHelpCenter = onOpenHelpCenter,
        onPrivacyPolicy = onOpenPrivacyPolicy,
        onTermsAndConditions = onOpenTerms
    )
    
    // Help/Legal screens
    HelpCenterScreen(isVisible = showHelpCenter, onDismiss = onDismissHelpCenter)
    TermsAndConditionsScreen(isVisible = showTermsAndConditions, onDismiss = onDismissTerms)
    PrivacyPolicyScreen(isVisible = showPrivacyPolicy, onDismiss = onDismissPrivacy)
    
    // Comments Sheet
    CommentsSheet(
        isVisible = showCommentsSheet,
        comments = mappedComments,
        onDismiss = onDismissComments,
        onSendComment = onSendComment,
        onLikeComment = onLikeComment,
        onReplyComment = onReplyComment,
        onDeleteComment = onDeleteComment,
        isLoading = isCommentsLoading,
        currentUserAvatar = currentUserAvatar,
        currentUsername = currentUsername,
        currentUserId = currentUserId
    )
    
    // Consult Modal
    ConsultModal(
        isVisible = showConsultModal,
        post = selectedPostForConsult,
        onDismiss = onDismissConsult,
        onSendConsult = onSendConsult
    )
    
    // Forward Modal
    ForwardModal(
        isVisible = showForwardModal,
        post = selectedPostForForward,
        onDismiss = onDismissForward,
        onForwardToUser = onForwardToUser
    )
    
    // Cart Modal
    CartModal(
        isVisible = showCartModal,
        onDismiss = onDismissCart,
        onCheckout = onNavigateToCheckoutCart,
        onContinueShopping = onDismissCart,
        onOpenCategories = onOpenCategoriesCart,
        onOpenExplore = onOpenExploreCart,
        onVisibilityChange = onCartVisibilityChange,
        onNavigateToCheckout = onNavigateToCheckoutCart
    )
    
    // Report Modal
    ReportModal(
        isVisible = showReportModal,
        contentType = "post",
        contentId = reportPostId,
        username = reportUsername,
        onDismiss = onDismissReport,
        onSubmitReport = onSubmitReport
    )
    
    // QR Code Modal
    PostQrCodeModal(
        isVisible = showQrCodeModal,
        post = qrCodePost,
        onDismiss = onDismissQrCode
    )
    
    // Story Viewers Modal
    StoryViewersModal(
        isVisible = showStoryViewersModal,
        storyId = selectedStoryForViewers,
        onDismiss = onDismissStoryViewers,
        onViewerClick = onViewerClick
    )
    
    // User Profile Screen
    if (showUserProfile && selectedUserId != null) {
        UserProfileScreen(
            userId = selectedUserId,
            onBack = onDismissUserProfile,
            onPostClick = onPostClickProfile,
            onOpenChat = onOpenChatFromUserProfile,
            modifier = Modifier.fillMaxSize()
        )
    }
    
    // Search Results Screen
    if (showSearchResults) {
        Box(modifier = Modifier.fillMaxSize()) {
            SearchResultsScreen(
                initialQuery = searchResultsQuery,
                onBack = {
                    onDismissSearchResults()
                    onClearSearchQuery()
                },
                onProductClick = onProductClickSearch,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
