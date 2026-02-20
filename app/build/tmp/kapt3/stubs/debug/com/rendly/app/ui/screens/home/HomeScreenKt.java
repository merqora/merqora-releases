package com.rendly.app.ui.screens.home;

import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.navigation.NavController;
import com.rendly.app.ui.components.CategoryDrawer;
import com.rendly.app.ui.components.settings.HelpCenterScreen;
import com.rendly.app.ui.components.settings.TermsAndConditionsScreen;
import com.rendly.app.ui.components.settings.PrivacyPolicyScreen;
import com.rendly.app.ui.components.Comment;
import com.rendly.app.ui.components.ConsultModal;
import com.rendly.app.ui.components.ForwardModal;
import com.rendly.app.ui.components.HomeHeader;
import com.rendly.app.ui.components.OptimizedNotificationsDrawer;
import com.rendly.app.ui.components.PostOptionsModal;
import com.rendly.app.ui.components.SearchBar;
import com.rendly.app.ui.components.StoriesViewer;
import com.rendly.app.ui.components.Story;
import com.rendly.app.ui.components.StoriesCarousel;
import com.rendly.app.ui.components.StoryViewersModal;
import com.rendly.app.ui.components.UploadProgressBanner;
import com.rendly.app.ui.components.UserStories;
import com.rendly.app.ui.components.CartModal;
import com.rendly.app.ui.components.ReportModal;
import com.rendly.app.ui.components.HiddenPostPlaceholder;
import com.rendly.app.ui.components.PostQrCodeModal;
import com.rendly.app.ui.components.FeaturedPostsSection;
import com.rendly.app.data.repository.CartRepository;
import com.rendly.app.util.FCMHelper;
import com.rendly.app.data.repository.ChatRepository;
import com.rendly.app.data.repository.ExploreRepository;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.model.StoryUploadState;
import com.rendly.app.data.repository.CommentRepository;
import com.rendly.app.data.repository.NotificationRepository;
import com.rendly.app.data.repository.PostRepository;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.data.repository.StoryRepository;
import com.rendly.app.data.repository.ViewTracker;
import com.rendly.app.data.cache.BadgeCountCache;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000d\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\t\n\u0000\u001a\u00f8\u0002\u0010\u0003\u001a\u00020\u00042\b\b\u0002\u0010\u0005\u001a\u00020\u00062\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u000e\b\u0002\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u0014\b\u0002\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u000b2\u0014\b\u0002\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u000b2\u0014\b\u0002\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u000b2\u0014\b\u0002\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u000b2\u0014\b\u0002\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u000b2\u0014\b\u0002\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u000b2\u0014\b\u0002\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u000b2 \b\u0002\u0010\u0013\u001a\u001a\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u0015\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00040\u00142\u0014\b\u0002\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00040\u000b2\u000e\b\u0002\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u000e\b\u0002\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\b\b\u0002\u0010\u001d\u001a\u00020\u001e2\b\b\u0002\u0010\u001f\u001a\u00020\f2\b\b\u0002\u0010 \u001a\u00020\u00172\u0014\b\u0002\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00040\u000b2\u000e\b\u0002\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00040\bH\u0007\u001a\u001a\u0010#\u001a\u00020\u00042\u0006\u0010$\u001a\u00020%2\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u0007\u001a\u00ae\u0001\u0010&\u001a\u00020\u00042\u0006\u0010\'\u001a\u00020(2\b\u0010)\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010*\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00040\u000b2\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00040\u000b2\u0012\u0010,\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00040\u000b2\u0012\u0010-\u001a\u000e\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00040\u000b2\u0012\u0010.\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00040\u000b2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00040\b2\u0018\u0010/\u001a\u0014\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00040\u0014H\u0003\u001a\u0010\u00100\u001a\u00020\u00172\u0006\u00101\u001a\u00020\u0017H\u0002\u001a\u0010\u00102\u001a\u0002032\u0006\u00101\u001a\u00020\u0017H\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00064"}, d2 = {"dateFormatCache", "Ljava/lang/ThreadLocal;", "Ljava/text/SimpleDateFormat;", "HomeContent", "", "viewModel", "Lcom/rendly/app/ui/screens/home/HomeViewModel;", "onMessagesClick", "Lkotlin/Function0;", "onNotificationsClick", "onStoriesViewerVisibilityChange", "Lkotlin/Function1;", "", "onUserProfileVisibilityChange", "onForwardModalVisibilityChange", "onCommentsSheetVisibilityChange", "onProductPageVisibilityChange", "onCartModalVisibilityChange", "onSearchResultsVisibilityChange", "onOpenStoriesViewer", "Lkotlin/Function2;", "", "Lcom/rendly/app/ui/components/UserStories;", "", "onOpenChatFromProfile", "Lcom/rendly/app/data/model/Usuario;", "onHomeClick", "onNavigateToProfile", "onNavigateToCheckout", "homeReclickTrigger", "", "showNavBar", "currentNavRoute", "onNavNavigate", "onNavHomeReclick", "HomeScreen", "navController", "Landroidx/navigation/NavController;", "StablePostItem", "post", "Lcom/rendly/app/data/model/Post;", "currentUserId", "onSelectForComments", "onSelectForProduct", "onSelectForConsult", "onSelectForForward", "onSelectUserId", "onSelectForOptions", "formatCommentTime", "createdAt", "parseStoryTimestamp", "", "app_debug"})
public final class HomeScreenKt {
    
    /**
     * OPTIMIZACIÓN: ThreadLocal cache para SimpleDateFormat
     * SimpleDateFormat NO es thread-safe y crear instancias es costoso (~0.5ms cada una)
     * Con ThreadLocal, cada thread reutiliza su propia instancia
     */
    @org.jetbrains.annotations.NotNull
    private static final java.lang.ThreadLocal<java.text.SimpleDateFormat> dateFormatCache = null;
    
    @androidx.compose.runtime.Composable
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.screens.home.HomeViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void HomeContent(@org.jetbrains.annotations.NotNull
    com.rendly.app.ui.screens.home.HomeViewModel viewModel, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onMessagesClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNotificationsClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onStoriesViewerVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onUserProfileVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onForwardModalVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onCommentsSheetVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onProductPageVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onCartModalVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onSearchResultsVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.util.List<com.rendly.app.ui.components.UserStories>, ? super java.lang.String, kotlin.Unit> onOpenStoriesViewer, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Usuario, kotlin.Unit> onOpenChatFromProfile, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onHomeClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToProfile, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToCheckout, int homeReclickTrigger, boolean showNavBar, @org.jetbrains.annotations.NotNull
    java.lang.String currentNavRoute, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavNavigate, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavHomeReclick) {
    }
    
    private static final java.lang.String formatCommentTime(java.lang.String createdAt) {
        return null;
    }
    
    /**
     * OPTIMIZACIÓN: Parser de timestamp para stories usando cache ThreadLocal
     * Evita crear SimpleDateFormat en cada frame durante scroll.
     */
    private static final long parseStoryTimestamp(java.lang.String createdAt) {
        return 0L;
    }
    
    /**
     * OPTIMIZACIÓN CRÍTICA: Subcomposable estable para PostItem
     *
     * Elimina jank causado por:
     * 1. Lambdas inestables que capturan objetos mutables
     * 2. Recomposiciones en cascada cuando cambia cualquier estado del padre
     *
     * Al extraer a un composable separado con parámetros estables,
     * Compose puede skipear recomposiciones cuando el post no cambia.
     */
    @androidx.compose.runtime.Composable
    private static final void StablePostItem(com.rendly.app.data.model.Post post, java.lang.String currentUserId, com.rendly.app.ui.screens.home.HomeViewModel viewModel, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onSelectForComments, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onSelectForProduct, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onSelectForConsult, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onSelectForForward, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSelectUserId, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToProfile, kotlin.jvm.functions.Function2<? super com.rendly.app.data.model.Post, ? super java.lang.Boolean, kotlin.Unit> onSelectForOptions) {
    }
}