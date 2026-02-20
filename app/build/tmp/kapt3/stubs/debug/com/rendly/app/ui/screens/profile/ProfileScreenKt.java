package com.rendly.app.ui.screens.profile;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Highlight;
import com.rendly.app.data.repository.ProfileRepository;
import com.rendly.app.data.repository.HighlightRepository;
import com.rendly.app.ui.components.HighlightedStories;
import com.rendly.app.ui.components.HighlightedStory;
import com.rendly.app.ui.components.HighlightCategory;
import com.rendly.app.ui.components.HighlightFrameStyle;
import com.rendly.app.ui.components.HighlightFrameColor;
import com.rendly.app.ui.components.HighlightBackgroundColor;
import com.rendly.app.ui.components.ProfileStatsBadges;
import com.rendly.app.ui.components.SellerTrustIndicator;
import com.rendly.app.ui.components.AddHighlightModal;
import com.rendly.app.ui.components.StoriesViewer;
import com.rendly.app.ui.components.Story;
import com.rendly.app.ui.components.UserStories;
import com.rendly.app.ui.components.HighlightOptionsModal;
import com.rendly.app.ui.components.PublishOptionsModal;
import com.rendly.app.ui.components.EditPostModal;
import com.rendly.app.ui.screens.publish.PublishScreen;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.model.Rend;
import com.rendly.app.data.repository.PostRepository;
import com.rendly.app.data.repository.RendRepository;
import com.rendly.app.data.repository.StoryRepository;
import com.rendly.app.ui.components.ProfileScreenSkeleton;
import com.rendly.app.ui.components.ProfileGridSkeleton;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0018\u0002\n\u0002\b\r\u001a2\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\nH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\f\u0010\r\u001a\u0010\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0010H\u0003\u001a:\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0012\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u0014H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0015\u0010\u0016\u001a2\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\n2\b\b\u0002\u0010\u0019\u001a\u00020\u00142\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00040\u001dH\u0003\u001a*\u0010\u001e\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b \u0010!\u001a\b\u0010\"\u001a\u00020\u0004H\u0003\u001aB\u0010#\u001a\u00020\u00042\f\u0010$\u001a\b\u0012\u0004\u0012\u00020%0\u00012\u0014\b\u0002\u0010&\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00040\'2\u0014\b\u0002\u0010(\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00040\'H\u0003\u001a2\u0010)\u001a\u00020\u00042\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00040\u001d2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00040\u001d2\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00040\u001dH\u0003\u001a\u0012\u0010-\u001a\u00020\u00042\b\u0010.\u001a\u0004\u0018\u00010\nH\u0003\u001a4\u0010/\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u00100\u001a\u00020\u00142\b\b\u0002\u00101\u001a\u00020\u00142\u000e\b\u0002\u00102\u001a\b\u0012\u0004\u0012\u00020\u00040\u001dH\u0003\u001a\u0098\u0001\u00103\u001a\u00020\u00042\u000e\b\u0002\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00040\u001d2\u0014\b\u0002\u00104\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00040\'2\u0014\b\u0002\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00040\'2\u000e\b\u0002\u00106\u001a\b\u0012\u0004\u0012\u00020\u00040\u001d2\b\b\u0002\u00107\u001a\u0002082\b\b\u0002\u00109\u001a\u00020\u00142\b\b\u0002\u0010:\u001a\u00020\n2\u0014\b\u0002\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00040\'2\u000e\b\u0002\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00040\u001dH\u0007\u001a\u0018\u0010=\u001a\u00020\u00042\u0006\u0010>\u001a\u00020\u001b2\u0006\u0010\u0012\u001a\u00020\nH\u0003\u001a\u0018\u0010?\u001a\u00020\u00042\u0006\u0010@\u001a\u00020\u001b2\u0006\u0010\u0012\u001a\u00020\nH\u0003\u001a2\u0010A\u001a\u00020\u00042\f\u0010B\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u0006\u0010C\u001a\u00020\u001b2\u0012\u0010D\u001a\u000e\u0012\u0004\u0012\u00020\u001b\u0012\u0004\u0012\u00020\u00040\'H\u0003\u001a4\u0010E\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\u00142\f\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00040\u001d2\f\u0010G\u001a\b\u0012\u0004\u0012\u00020\u00040\u001dH\u0003\u001a \u0010H\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\n2\u0006\u0010I\u001a\u00020\u001b2\u0006\u0010J\u001a\u00020\u0014H\u0003\u001a\u0016\u0010K\u001a\u00020\u00042\f\u0010L\u001a\b\u0012\u0004\u0012\u00020M0\u0001H\u0003\u001aH\u0010N\u001a\u00020\u00042\f\u0010$\u001a\b\u0012\u0004\u0012\u00020%0\u00012\u0006\u0010O\u001a\u00020\u00142\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00040\'2\u0014\b\u0002\u0010P\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00040\'H\u0003\u001a2\u0010Q\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\bR\u0010S\u001a\b\u0010T\u001a\u00020\u0004H\u0003\u001a,\u0010U\u001a\u00020\u00042\u0006\u0010V\u001a\u00020%2\f\u0010W\u001a\b\u0012\u0004\u0012\u00020\u00040\u001d2\f\u0010X\u001a\b\u0012\u0004\u0012\u00020\u00040\u001dH\u0003\u001a\u0010\u0010Y\u001a\u00020\n2\u0006\u0010>\u001a\u00020\u001bH\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006Z"}, d2 = {"profileTabs", "", "Lcom/rendly/app/ui/screens/profile/ProfileTab;", "DetailCard", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "iconColor", "Landroidx/compose/ui/graphics/Color;", "title", "", "value", "DetailCard-RPmYEkk", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;)V", "DetailsSection", "profile", "Lcom/rendly/app/ui/screens/profile/ProfileData;", "InfoRow", "label", "showDivider", "", "InfoRow-iJQMabo", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;Z)V", "OwnProfileHeader", "username", "isVerified", "reputacion", "", "onPublishClick", "Lkotlin/Function0;", "PaymentMethodChip", "color", "PaymentMethodChip-mxwnekA", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;J)V", "PointsSection", "PostsGrid", "posts", "Lcom/rendly/app/data/model/Post;", "onPostClick", "Lkotlin/Function1;", "onPostLongPress", "ProfileActions", "onEditProfile", "onRendshop", "onMoreOptions", "ProfileBanner", "bannerUrl", "ProfileHeader", "hasStories", "isLoadingStories", "onAvatarClick", "ProfileScreen", "onStoriesViewerVisibilityChange", "onSettingsModalVisibilityChange", "onLogout", "modifier", "Landroidx/compose/ui/Modifier;", "showNavBar", "currentNavRoute", "onNavNavigate", "onNavHomeReclick", "ProfileStat", "count", "ProfileStatPercent", "percent", "ProfileTabs", "tabs", "selectedIndex", "onTabSelected", "ProfileTopHeader", "onBackClick", "onNotificationsClick", "QuestItem", "reward", "completed", "RendsGrid", "rends", "Lcom/rendly/app/data/model/Rend;", "SavedPostsGrid", "isLoading", "onRequestUnsave", "StatColumn", "StatColumn-g2O1Hgs", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;J)V", "StatDivider", "UnsaveConfirmationModal", "post", "onConfirm", "onDismiss", "formatCount", "app_debug"})
public final class ProfileScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.profile.ProfileTab> profileTabs = null;
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void ProfileScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onEditProfile, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onStoriesViewerVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onSettingsModalVisibilityChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onLogout, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, boolean showNavBar, @org.jetbrains.annotations.NotNull
    java.lang.String currentNavRoute, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavNavigate, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavHomeReclick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileTopHeader(java.lang.String username, boolean isVerified, kotlin.jvm.functions.Function0<kotlin.Unit> onBackClick, kotlin.jvm.functions.Function0<kotlin.Unit> onNotificationsClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileBanner(java.lang.String bannerUrl) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileHeader(com.rendly.app.ui.screens.profile.ProfileData profile, boolean hasStories, boolean isLoadingStories, kotlin.jvm.functions.Function0<kotlin.Unit> onAvatarClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileStat(int count, java.lang.String label) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileStatPercent(int percent, java.lang.String label) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileActions(kotlin.jvm.functions.Function0<kotlin.Unit> onEditProfile, kotlin.jvm.functions.Function0<kotlin.Unit> onRendshop, kotlin.jvm.functions.Function0<kotlin.Unit> onMoreOptions) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileTabs(java.util.List<com.rendly.app.ui.screens.profile.ProfileTab> tabs, int selectedIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onTabSelected) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void PostsGrid(java.util.List<com.rendly.app.data.model.Post> posts, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostClick, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostLongPress) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendsGrid(java.util.List<com.rendly.app.data.model.Rend> rends) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void DetailsSection(com.rendly.app.ui.screens.profile.ProfileData profile) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StatDivider() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PointsSection() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void QuestItem(java.lang.String title, int reward, boolean completed) {
    }
    
    private static final java.lang.String formatCount(int count) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void OwnProfileHeader(java.lang.String username, boolean isVerified, int reputacion, kotlin.jvm.functions.Function0<kotlin.Unit> onPublishClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SavedPostsGrid(java.util.List<com.rendly.app.data.model.Post> posts, boolean isLoading, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostClick, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onRequestUnsave) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UnsaveConfirmationModal(com.rendly.app.data.model.Post post, kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}