package com.rendly.app.ui.screens.rends;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
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
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.model.Rend;
import com.rendly.app.ui.components.Comment;
import com.rendly.app.data.repository.RendRepository;
import com.rendly.app.data.repository.FollowersRepository;
import com.rendly.app.data.repository.FollowType;
import com.rendly.app.data.repository.CommentRepository;
import com.rendly.app.data.repository.NotificationRepository;
import com.rendly.app.data.remote.SupabaseClient;
import io.github.jan.supabase.postgrest.query.Columns;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;
import com.rendly.app.ui.components.ConsultModal;
import com.rendly.app.ui.components.ForwardModal;
import com.rendly.app.data.repository.ViewTracker;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0010\u001a:\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0016\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a&\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aJ\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0015\u001a\u00020\u0003H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0016\u0010\u0017\u001a<\u0010\u0018\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u00122\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001c\u0010\u001d\u001a\u0016\u0010\u001e\u001a\u00020\u00012\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aV\u0010 \u001a\u00020\u00012\u0006\u0010!\u001a\u00020\"2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aV\u0010\'\u001a\u00020\u00012\u0006\u0010!\u001a\u00020\"2\u0006\u0010(\u001a\u00020\u00032\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010*2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a~\u0010.\u001a\u00020\u00012\u0014\b\u0002\u0010/\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010*2\u000e\b\u0002\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u00100\u001a\u00020\u00032\b\b\u0002\u00101\u001a\u00020\u00032\b\b\u0002\u00102\u001a\u00020\u000e2\u0014\b\u0002\u00103\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010*2\u000e\b\u0002\u00104\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\n\b\u0002\u00105\u001a\u0004\u0018\u00010\u000eH\u0007\u001a\b\u00106\u001a\u00020\u0001H\u0003\u001a\u0010\u00107\u001a\u00020\u000e2\u0006\u00108\u001a\u00020\u000eH\u0002\u001a\u0010\u00109\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006:"}, d2 = {"CommentChoiceModal", "", "isVisible", "", "onDismiss", "Lkotlin/Function0;", "onViewComments", "onMakeConsult", "EmptyRendsState", "onReload", "OptionGridItem", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "label", "", "onClick", "OptionListItem", "iconTint", "Landroidx/compose/ui/graphics/Color;", "text", "subtitle", "isDestructive", "OptionListItem-3IgeMak", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLjava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function0;Z)V", "RendActionButton", "count", "", "tint", "RendActionButton-9LQNqLg", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/Integer;JLkotlin/jvm/functions/Function0;)V", "RendHeader", "onNavigateToTendencias", "RendOptionsModal", "rend", "Lcom/rendly/app/data/model/Rend;", "onReport", "onNotInterested", "onCopyLink", "onDownload", "RendPageContent", "isPlaying", "onUserClick", "Lkotlin/Function1;", "onOpenProductPage", "onOpenCommentChoice", "onOpenForward", "RendScreen", "onNavigateToProfile", "isScreenVisible", "showNavBar", "currentNavRoute", "onNavNavigate", "onNavHomeReclick", "initialRendId", "RendSkeletonLoader", "formatCommentTimeRend", "createdAt", "formatCount", "app_debug"})
public final class RendScreenKt {
    
    /**
     * -------------------------------------------------------------------------------
     * REND SCREEN - Pantalla de videos cortos con CLIPPING PERFECTO
     * -------------------------------------------------------------------------------
     *
     * IMPORTANTE: Esta pantalla usa Múltiples capas de clipping para evitar que
     * el contenido de video se vea desde otras secciones del pager horizontal.
     *
     * Capas de clipping:
     * 1. Box raíz con clipToBounds + graphicsLayer clip
     * 2. Contenedor interno con clip estricto
     * 3. AndroidView del video con clip en LayoutParams
     *
     * -------------------------------------------------------------------------------
     */
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void RendScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToProfile, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToTendencias, boolean isScreenVisible, boolean showNavBar, @org.jetbrains.annotations.NotNull
    java.lang.String currentNavRoute, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavNavigate, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavHomeReclick, @org.jetbrains.annotations.Nullable
    java.lang.String initialRendId) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendHeader(kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToTendencias) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EmptyRendsState(kotlin.jvm.functions.Function0<kotlin.Unit> onReload) {
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @androidx.compose.runtime.Composable
    private static final void RendPageContent(com.rendly.app.data.model.Rend rend, boolean isPlaying, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onUserClick, kotlin.jvm.functions.Function0<kotlin.Unit> onOpenProductPage, kotlin.jvm.functions.Function0<kotlin.Unit> onOpenCommentChoice, kotlin.jvm.functions.Function0<kotlin.Unit> onOpenForward) {
    }
    
    private static final java.lang.String formatCount(int count) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendSkeletonLoader() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CommentChoiceModal(boolean isVisible, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onViewComments, kotlin.jvm.functions.Function0<kotlin.Unit> onMakeConsult) {
    }
    
    private static final java.lang.String formatCommentTimeRend(java.lang.String createdAt) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendOptionsModal(com.rendly.app.data.model.Rend rend, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onReport, kotlin.jvm.functions.Function0<kotlin.Unit> onNotInterested, kotlin.jvm.functions.Function0<kotlin.Unit> onCopyLink, kotlin.jvm.functions.Function0<kotlin.Unit> onDownload) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OptionGridItem(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}