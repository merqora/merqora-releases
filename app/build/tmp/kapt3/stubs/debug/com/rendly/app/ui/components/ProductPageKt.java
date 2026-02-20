package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextDecoration;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.repository.CartRepository;
import com.rendly.app.data.repository.CommentRepository;
import com.rendly.app.data.repository.NotificationRepository;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0084\u0001\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001b\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0007\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\n\u001a\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u000bH\u0003\u001a4\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0006H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0017\u0010\u0018\u001a,\u0010\u0019\u001a\u00020\r2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b2\u0006\u0010\u001c\u001a\u00020\u000b2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\r0\u001eH\u0003\u001a\u0010\u0010\u001f\u001a\u00020\r2\u0006\u0010\u0013\u001a\u00020\u000bH\u0003\u001a\b\u0010 \u001a\u00020\rH\u0003\u001a4\u0010!\u001a\u00020\r2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\u000e\b\u0002\u0010#\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\r0\u001eH\u0003\u001a\u00ba\u0001\u0010%\u001a\u00020\r2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b2\u0006\u0010&\u001a\u00020\u00152\u0006\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020\u00152\b\b\u0002\u0010*\u001a\u00020(2\b\b\u0002\u0010+\u001a\u00020,2\u0014\b\u0002\u0010-\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020,0\n2 \b\u0002\u0010.\u001a\u001a\u0012\u0004\u0012\u00020(\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00060/0\n2\u0014\b\u0002\u00100\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\r012\f\u00102\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\f\u00103\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\f\u00104\u001a\b\u0012\u0004\u0012\u00020\r0\u001eH\u0003\u001a\u0010\u00105\u001a\u00020\r2\u0006\u00106\u001a\u00020\u000bH\u0003\u001ad\u00107\u001a\u00020\r2\u0018\u00108\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00060/0\u001b2\u0006\u00109\u001a\u00020(2\u0012\u0010:\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\r012\u000e\b\u0002\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b2\u0014\b\u0002\u0010;\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\r01H\u0003\u001a\u0010\u0010<\u001a\u00020\r2\u0006\u0010=\u001a\u00020\u000bH\u0003\u001a,\u0010>\u001a\u00020\r2\u0006\u0010?\u001a\u00020\u000b2\u0006\u00106\u001a\u00020\u000b2\b\b\u0002\u0010@\u001a\u00020\u000b2\b\b\u0002\u0010A\u001a\u00020\u0015H\u0003\u001a&\u0010B\u001a\u00020\r2\b\b\u0002\u0010C\u001a\u00020\u00152\b\b\u0002\u0010D\u001a\u00020\u00152\b\b\u0002\u0010A\u001a\u00020\u0015H\u0003\u001a<\u0010E\u001a\u00020\r2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b2\u0006\u0010F\u001a\u00020\u000b2\f\u0010G\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\u000e\b\u0002\u0010H\u001a\b\u0012\u0004\u0012\u00020\r0\u001eH\u0003\u001a>\u0010I\u001a\u00020\r2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001b2\u0006\u0010\u001c\u001a\u00020\u000b2\b\b\u0002\u00109\u001a\u00020(2\u0014\b\u0002\u0010J\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\r01H\u0003\u001a\u00c8\u0002\u0010K\u001a\u00020\r2\b\u0010L\u001a\u0004\u0018\u00010M2\u0006\u0010N\u001a\u00020\u00152\b\b\u0002\u0010O\u001a\u00020\u00152\u000e\b\u0002\u0010P\u001a\b\u0012\u0004\u0012\u00020M0\u001b2\u000e\b\u0002\u0010Q\u001a\b\u0012\u0004\u0012\u00020M0\u001b2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\u0014\b\u0002\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u0010#\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u0010R\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u0010S\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u0010T\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u00102\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u00104\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u00103\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u0010U\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u0014\b\u0002\u0010V\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r012\u000e\b\u0002\u0010W\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\b\b\u0002\u0010X\u001a\u00020YH\u0007\u001a!\u0010Z\u001a\u00020\r2\u0006\u0010[\u001a\u00020,2\n\b\u0002\u0010\\\u001a\u0004\u0018\u00010,H\u0003\u00a2\u0006\u0002\u0010]\u001aB\u0010^\u001a\u00020\r2\u0006\u0010_\u001a\u00020\u000b2\u0012\u0010`\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\r012\f\u0010a\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\u000e\b\u0002\u0010b\u001a\b\u0012\u0004\u0012\u00020\r0\u001eH\u0003\u001a\u001a\u0010c\u001a\u00020\r2\u0006\u0010d\u001a\u00020\u00152\b\b\u0002\u0010e\u001a\u00020(H\u0003\u001a\u0018\u0010f\u001a\u00020\r2\u0006\u0010g\u001a\u00020(2\u0006\u0010h\u001a\u00020iH\u0003\u001a*\u0010j\u001a\u00020\r2\f\u0010k\u001a\b\u0012\u0004\u0012\u00020M0\u001b2\u0012\u0010l\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r01H\u0003\u001aP\u0010m\u001a\u00020\r2\u0006\u0010n\u001a\u00020\u000b2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\r0\u001e2\u0012\u0010H\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\r012\b\b\u0002\u0010F\u001a\u00020\u000b2\b\b\u0002\u0010o\u001a\u00020\u000b2\b\b\u0002\u0010p\u001a\u00020\u000bH\u0003\u001a\"\u0010q\u001a\u00020\r2\b\b\u0002\u0010r\u001a\u00020s2\u000e\b\u0002\u0010t\u001a\b\u0012\u0004\u0012\u00020\r0\u001eH\u0003\u001a\b\u0010u\u001a\u00020\rH\u0003\u001aF\u0010v\u001a\u00020\r2\u0006\u0010w\u001a\u00020\u000b2\u0006\u0010x\u001a\u00020\u000b2\b\u0010y\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010D\u001a\u00020\u00152\n\b\u0002\u0010z\u001a\u0004\u0018\u00010{2\f\u0010|\u001a\b\u0012\u0004\u0012\u00020\r0\u001eH\u0003\u001a+\u0010}\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u000b2\u0006\u0010~\u001a\u00020\u0006H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0005\b\u007f\u0010\u0080\u0001\u001a&\u0010\u0081\u0001\u001a\u00020\r2\u0007\u0010\u0082\u0001\u001a\u00020\u000b2\u0012\u0010l\u001a\u000e\u0012\u0004\u0012\u00020M\u0012\u0004\u0012\u00020\r01H\u0003\u001a\u0012\u0010\u0083\u0001\u001a\u00020\u000b2\u0007\u0010\u0084\u0001\u001a\u00020(H\u0002\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0007\"\u0010\u0010\b\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0007\"\u001a\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0085\u0001"}, d2 = {"AddToCartColor", "error/NonExistentClass", "Lerror/NonExistentClass;", "BuyNowColor", "ConsultColor", "ReturnGreen", "Landroidx/compose/ui/graphics/Color;", "J", "VerifiedBlue", "categoryDisplayNames", "", "", "DetailRow", "", "label", "value", "FeatureChip", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "text", "isActive", "", "activeColor", "FeatureChip-g2O1Hgs", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;ZJ)V", "FullscreenGalleryModal", "images", "", "title", "onDismiss", "Lkotlin/Function0;", "PaymentMethodChip", "PaymentMethodsSection", "ProductActionButtons", "onBuyNow", "onAddToCart", "onContact", "ProductActionsRow", "isLiked", "likesCount", "", "isSaved", "selectedImageIndex", "basePrice", "", "variantPrices", "variantColors", "Lkotlin/Pair;", "onImageSelect", "Lkotlin/Function1;", "onLike", "onForward", "onSave", "ProductBadgesRow", "category", "ProductColorsSection", "colors", "selectedIndex", "onColorSelect", "onImageChange", "ProductDescriptionSection", "description", "ProductDetailsSection", "condition", "warranty", "returnsAccepted", "ProductFeaturesCompact", "freeShipping", "isVerified", "ProductGalleryWithSKU", "postId", "onViewAllImages", "onReport", "ProductImageGalleryV2", "onPageChange", "ProductPage", "post", "Lcom/rendly/app/data/model/Post;", "isVisible", "isFromRend", "relatedPosts", "suggestedPosts", "onContactSeller", "onShare", "onFavorite", "onViewAllReviews", "onRelatedPostClick", "onNavigateToCheckout", "modifier", "Landroidx/compose/ui/Modifier;", "ProductPriceSection", "price", "originalPrice", "(DLjava/lang/Double;)V", "ProductSearchHeader", "searchQuery", "onSearchChange", "onBack", "onCartClick", "ProductTopInfo", "isNew", "reviewsCount", "RatingBar", "stars", "percent", "", "RelatedProductsSection", "posts", "onPostClick", "ReportProductDialog", "sku", "postTitle", "postImage", "ReviewsSection", "ratingDistribution", "Lcom/rendly/app/ui/components/RatingDistribution;", "onViewAll", "SectionDivider", "SellerSectionV2", "username", "avatarUrl", "storeName", "sellerStats", "Lcom/rendly/app/data/model/SellerStats;", "onViewProfile", "SellerStatItem", "color", "SellerStatItem-mxwnekA", "(Ljava/lang/String;Ljava/lang/String;J)V", "YouMightLikeSectionInfinite", "currentPostId", "formatCountV2", "count", "app_debug"})
public final class ProductPageKt {
    private static final long VerifiedBlue = 0L;
    private static final long ReturnGreen = 0L;
    @org.jetbrains.annotations.NotNull
    private static final java.util.Map<java.lang.String, java.lang.String> categoryDisplayNames = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.Object BuyNowColor = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.Object AddToCartColor = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.Object ConsultColor = null;
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void ProductPage(@org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.Post post, boolean isVisible, boolean isFromRend, @org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.model.Post> relatedPosts, @org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.model.Post> suggestedPosts, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onBuyNow, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onAddToCart, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onContactSeller, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onShare, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onFavorite, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onLike, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onSave, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onForward, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onViewAllReviews, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onRelatedPostClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToCheckout, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductSearchHeader(java.lang.String searchQuery, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSearchChange, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onCartClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductTopInfo(boolean isNew, int reviewsCount) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductBadgesRow(java.lang.String category) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void ProductImageGalleryV2(java.util.List<java.lang.String> images, java.lang.String title, int selectedIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onPageChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductActionsRow(java.util.List<java.lang.String> images, boolean isLiked, int likesCount, boolean isSaved, int selectedImageIndex, double basePrice, java.util.Map<java.lang.Integer, java.lang.Double> variantPrices, java.util.Map<java.lang.Integer, kotlin.Pair<java.lang.String, androidx.compose.ui.graphics.Color>> variantColors, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onImageSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onLike, kotlin.jvm.functions.Function0<kotlin.Unit> onForward, kotlin.jvm.functions.Function0<kotlin.Unit> onSave) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductColorsSection(java.util.List<kotlin.Pair<java.lang.String, androidx.compose.ui.graphics.Color>> colors, int selectedIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onColorSelect, java.util.List<java.lang.String> images, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onImageChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductPriceSection(double price, java.lang.Double originalPrice) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SellerSectionV2(java.lang.String username, java.lang.String avatarUrl, java.lang.String storeName, boolean isVerified, com.rendly.app.data.model.SellerStats sellerStats, kotlin.jvm.functions.Function0<kotlin.Unit> onViewProfile) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductFeaturesCompact(boolean freeShipping, boolean isVerified, boolean returnsAccepted) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductDetailsSection(java.lang.String condition, java.lang.String category, java.lang.String warranty, boolean returnsAccepted) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void DetailRow(java.lang.String label, java.lang.String value) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductDescriptionSection(java.lang.String description) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentMethodsSection() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentMethodChip(java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ReviewsSection(com.rendly.app.ui.components.RatingDistribution ratingDistribution, kotlin.jvm.functions.Function0<kotlin.Unit> onViewAll) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RatingBar(int stars, float percent) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RelatedProductsSection(java.util.List<com.rendly.app.data.model.Post> posts, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void YouMightLikeSectionInfinite(java.lang.String currentPostId, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductActionButtons(kotlin.jvm.functions.Function0<kotlin.Unit> onBuyNow, kotlin.jvm.functions.Function0<kotlin.Unit> onAddToCart, kotlin.jvm.functions.Function0<kotlin.Unit> onContact) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SectionDivider() {
    }
    
    private static final java.lang.String formatCountV2(int count) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProductGalleryWithSKU(java.util.List<java.lang.String> images, java.lang.String postId, kotlin.jvm.functions.Function0<kotlin.Unit> onViewAllImages, kotlin.jvm.functions.Function0<kotlin.Unit> onReport) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void FullscreenGalleryModal(java.util.List<java.lang.String> images, java.lang.String title, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ReportProductDialog(java.lang.String sku, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onReport, java.lang.String postId, java.lang.String postTitle, java.lang.String postImage) {
    }
}