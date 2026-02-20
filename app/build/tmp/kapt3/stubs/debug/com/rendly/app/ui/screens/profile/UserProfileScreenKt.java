package com.rendly.app.ui.screens.profile;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.pager.PagerDefaults;
import androidx.compose.foundation.pager.PagerSnapDistance;
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
import com.rendly.app.data.model.Post;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.ui.components.HighlightedStories;
import com.rendly.app.ui.components.HighlightedStory;
import com.rendly.app.ui.components.HighlightCategory;
import com.rendly.app.ui.theme.*;
import android.util.Log;
import android.widget.Toast;
import com.rendly.app.data.model.PostDB;
import com.rendly.app.data.repository.ChatRepository;
import com.rendly.app.data.repository.FollowersRepository;
import com.rendly.app.data.repository.FollowType;
import com.rendly.app.data.repository.StoryRepository;
import com.rendly.app.data.repository.UserPreferencesRepository;
import com.rendly.app.ui.components.FollowBottomSheet;
import com.rendly.app.ui.components.NotificationsSettingsModal;
import com.rendly.app.ui.components.ProfileScreenSkeleton;
import com.rendly.app.ui.components.ProfileGridSkeleton;
import com.rendly.app.ui.components.UserProfileSettingsModal;
import com.rendly.app.ui.components.ReportModal;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000`\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0010\u001a>\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00040\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\fH\u0003\u001a\u0010\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0010H\u0003\u001a:\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\bH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0019\u0010\u001a\u001a*\u0010\u001b\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\u0015H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001a*\u0010\u001f\u001a\u00020\u00042\f\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u00012\u0012\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\u00040#H\u0003\u001a:\u0010$\u001a\u00020\u00042\u0006\u0010%\u001a\u00020&2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00040\f2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00040\f2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00040\fH\u0003\u001a>\u0010*\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010+\u001a\u00020\n2\u0006\u0010,\u001a\u00020\n2\u0006\u0010-\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n2\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00040\fH\u0003\u001aT\u0010/\u001a\u00020\u00042\u0006\u00100\u001a\u00020\u00062\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00040\f2\u0014\b\u0002\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\u00040#2\u0014\b\u0002\u00101\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00040#2\b\b\u0002\u00102\u001a\u000203H\u0007\u001a\u0018\u00104\u001a\u00020\u00042\u0006\u00105\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\u0006H\u0003\u001a\u0018\u00106\u001a\u00020\u00042\u0006\u00107\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\u0006H\u0003\u001a<\u00108\u001a\u00020\u00042\f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u0006\u0010:\u001a\u00020\n2\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00040#2\b\b\u0002\u0010<\u001a\u00020\bH\u0003\u001a\u0016\u0010=\u001a\u00020\u00042\f\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u0001H\u0003\u001a2\u0010>\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\u0015H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b?\u0010@\u001a\b\u0010A\u001a\u00020\u0004H\u0003\u001a\u0010\u0010B\u001a\u00020\u00062\u0006\u00105\u001a\u00020\nH\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006C"}, d2 = {"userProfileTabs", "", "Lcom/rendly/app/ui/screens/profile/UserProfileTab;", "ProfileCompactHeader", "", "username", "", "isVerified", "", "reputacion", "", "onBackClick", "Lkotlin/Function0;", "onNotificationsClick", "UserDetailsSection", "user", "Lcom/rendly/app/data/model/Usuario;", "UserInfoRow", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "iconColor", "Landroidx/compose/ui/graphics/Color;", "label", "value", "showDivider", "UserInfoRow-iJQMabo", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;Z)V", "UserPaymentChip", "color", "UserPaymentChip-mxwnekA", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;J)V", "UserPostsGrid", "posts", "Lcom/rendly/app/data/model/Post;", "onPostClick", "Lkotlin/Function1;", "UserProfileActions", "followType", "Lcom/rendly/app/data/repository/FollowType;", "onFollow", "onMessage", "onMore", "UserProfileHeader", "postCount", "seguidores", "clientes", "onBack", "UserProfileScreen", "userId", "onOpenChat", "modifier", "Landroidx/compose/ui/Modifier;", "UserProfileStat", "count", "UserProfileStatPercent", "percent", "UserProfileTabs", "tabs", "selectedIndex", "onTabSelected", "showTopSeparator", "UserRendsGrid", "UserStatColumn", "UserStatColumn-g2O1Hgs", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;J)V", "UserStatDivider", "formatCount", "app_debug"})
public final class UserProfileScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.profile.UserProfileTab> userProfileTabs = null;
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void UserProfileScreen(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Usuario, kotlin.Unit> onOpenChat, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserProfileHeader(com.rendly.app.data.model.Usuario user, int postCount, int seguidores, int clientes, int reputacion, kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserProfileActions(com.rendly.app.data.repository.FollowType followType, kotlin.jvm.functions.Function0<kotlin.Unit> onFollow, kotlin.jvm.functions.Function0<kotlin.Unit> onMessage, kotlin.jvm.functions.Function0<kotlin.Unit> onMore) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserProfileTabs(java.util.List<com.rendly.app.ui.screens.profile.UserProfileTab> tabs, int selectedIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onTabSelected, boolean showTopSeparator) {
    }
    
    private static final java.lang.String formatCount(int count) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserProfileStat(int count, java.lang.String label) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserProfileStatPercent(int percent, java.lang.String label) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserPostsGrid(java.util.List<com.rendly.app.data.model.Post> posts, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserRendsGrid(java.util.List<com.rendly.app.data.model.Post> posts) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserDetailsSection(com.rendly.app.data.model.Usuario user) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void UserStatDivider() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ProfileCompactHeader(java.lang.String username, boolean isVerified, int reputacion, kotlin.jvm.functions.Function0<kotlin.Unit> onBackClick, kotlin.jvm.functions.Function0<kotlin.Unit> onNotificationsClick) {
    }
}