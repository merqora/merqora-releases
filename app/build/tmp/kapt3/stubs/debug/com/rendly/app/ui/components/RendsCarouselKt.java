package com.rendly.app.ui.components;

import android.net.Uri;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import coil.request.ImageRequest;
import coil.request.CachePolicy;
import com.rendly.app.data.model.Rend;
import com.rendly.app.data.repository.RendRepository;
import com.rendly.app.ui.theme.*;
import com.rendly.app.ui.components.VideoThumbnail;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\b\u0010\b\u001a\u00020\u0001H\u0003\u001aR\u0010\t\u001a\u00020\u00012\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00030\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00052\u0014\b\u0002\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u000e2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u0007\u001a\u0010\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0002\u001a\b\u0010\u0016\u001a\u00020\u0017H\u0003\u00a8\u0006\u0018"}, d2 = {"AutoplayRendCard", "", "rend", "Lcom/rendly/app/data/model/Rend;", "isAutoplayEnabled", "", "onClick", "Lkotlin/Function0;", "RendCardSkeleton", "RendsCarousel", "rends", "", "isLoading", "onRendClick", "Lkotlin/Function1;", "onViewAll", "modifier", "Landroidx/compose/ui/Modifier;", "formatCount", "", "count", "", "shimmerBrush", "Landroidx/compose/ui/graphics/Brush;", "app_debug"})
public final class RendsCarouselKt {
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @androidx.compose.runtime.Composable
    public static final void RendsCarousel(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.model.Rend> rends, boolean isLoading, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Rend, kotlin.Unit> onRendClick, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onViewAll, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @androidx.compose.runtime.Composable
    private static final void AutoplayRendCard(com.rendly.app.data.model.Rend rend, boolean isAutoplayEnabled, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendCardSkeleton() {
    }
    
    @androidx.compose.runtime.Composable
    private static final androidx.compose.ui.graphics.Brush shimmerBrush() {
        return null;
    }
    
    private static final java.lang.String formatCount(int count) {
        return null;
    }
}