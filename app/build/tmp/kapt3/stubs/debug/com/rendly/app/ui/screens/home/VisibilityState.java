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

/**
 * OPTIMIZACIÓN: Data class para consolidar estados de visibility en snapshotFlow.
 * Evita múltiples LaunchedEffects separados que compiten por el UI thread.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0011\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J1\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00032\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\t\u00a8\u0006\u0018"}, d2 = {"Lcom/rendly/app/ui/screens/home/VisibilityState;", "", "userProfile", "", "forward", "comments", "product", "(ZZZZ)V", "getComments", "()Z", "getForward", "getProduct", "getUserProfile", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
final class VisibilityState {
    private final boolean userProfile = false;
    private final boolean forward = false;
    private final boolean comments = false;
    private final boolean product = false;
    
    public VisibilityState(boolean userProfile, boolean forward, boolean comments, boolean product) {
        super();
    }
    
    public final boolean getUserProfile() {
        return false;
    }
    
    public final boolean getForward() {
        return false;
    }
    
    public final boolean getComments() {
        return false;
    }
    
    public final boolean getProduct() {
        return false;
    }
    
    public final boolean component1() {
        return false;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.screens.home.VisibilityState copy(boolean userProfile, boolean forward, boolean comments, boolean product) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}