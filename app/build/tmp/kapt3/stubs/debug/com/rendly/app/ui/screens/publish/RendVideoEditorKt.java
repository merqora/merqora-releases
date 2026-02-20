package com.rendly.app.ui.screens.publish;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.gestures.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.*;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.input.pointer.awaitFirstDown;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import com.rendly.app.ui.theme.*;
import kotlinx.coroutines.Dispatchers;
import android.util.LruCache;
import androidx.compose.ui.graphics.PathEffect;
import androidx.compose.ui.graphics.drawscope.Stroke;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0092\u0001\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0016\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0014\n\u0002\b\b\u001aD\u0010\u000f\u001a\u00020\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00140\u00122\u0018\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00100\u00162\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001aD\u0010\u0019\u001a\u00020\u00102\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00140\u00122\u0018\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00100\u00162\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001aT\u0010\u001a\u001a\u00020\u00102\u0006\u0010\u001b\u001a\u00020\u00132\u0006\u0010\u001c\u001a\u00020\u00142\u0006\u0010\u001d\u001a\u00020\u00142\u0006\u0010\u001e\u001a\u00020\u00142\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\b\b\u0002\u0010!\u001a\u00020\"H\u0003\u001a\u008c\u0001\u0010#\u001a\u00020\u00102\b\b\u0002\u0010$\u001a\u00020%2\u000e\b\u0002\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\u0016\b\u0002\u0010\'\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010(\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010)\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010*\u001a\u0010\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\b\b\u0002\u0010!\u001a\u00020\"H\u0003\u001a,\u0010+\u001a\u00020\u00102\u0006\u0010,\u001a\u00020\u00132\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001a>\u0010/\u001a\u00020\u00102\f\u00100\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\b\u00101\u001a\u0004\u0018\u00010\u00132\b\b\u0002\u00102\u001a\u00020%2\u0012\u00103\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00100\u001fH\u0003\u001a\u0088\u0001\u00104\u001a\u00020\u00102\b\b\u0002\u00105\u001a\u00020\u00132\b\b\u0002\u0010$\u001a\u00020%2\b\b\u0002\u00106\u001a\u00020\u00142\u000e\b\u0002\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\u0016\b\u0002\u0010\'\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010(\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010*\u001a\u0010\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\b\b\u0002\u0010!\u001a\u00020\"H\u0003\u001a*\u00107\u001a\u00020\u00102\u0012\u00108\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001a*\u00109\u001a\u00020\u00102\u0012\u00108\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001a2\u0010:\u001a\u00020\u00102\u0006\u0010;\u001a\u00020<2\u0012\u0010=\u001a\u000e\u0012\u0004\u0012\u00020<\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001a2\u0010>\u001a\u00020\u00102\u0006\u0010;\u001a\u00020<2\u0012\u0010=\u001a\u000e\u0012\u0004\u0012\u00020<\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001aJ\u0010?\u001a\u00020\u00102\u0006\u0010@\u001a\u00020%2\u0006\u0010A\u001a\u00020B2\u0006\u0010C\u001a\u00020B2\f\u0010D\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\f\u0010E\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\f\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001a\u0012\u0010G\u001a\u00020\u00102\b\b\u0002\u0010!\u001a\u00020\"H\u0003\u001a6\u0010H\u001a\u00020\u00102\u0006\u0010I\u001a\u00020J2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\b\b\u0002\u0010!\u001a\u00020\"H\u0007\u001a2\u0010K\u001a\u00020\u00102\u0006\u0010L\u001a\u00020\u00142\u0012\u0010M\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001a\u00b6\u0002\u0010N\u001a\u00020\u00102\f\u0010O\u001a\b\u0012\u0004\u0012\u00020P0\u00012\f\u0010Q\u001a\b\u0012\u0004\u0012\u00020R0\u00012\u0006\u0010S\u001a\u00020T2\f\u0010U\u001a\b\u0012\u0004\u0012\u00020V0\u00012\u0006\u0010W\u001a\u00020B2\u0006\u0010A\u001a\u00020B2\u0006\u0010X\u001a\u00020\u00142\u0006\u0010Y\u001a\u00020%2\u0006\u0010Z\u001a\u00020%2\b\u0010[\u001a\u0004\u0018\u00010R2\u0006\u0010\\\u001a\u00020%2\u0012\u0010]\u001a\u000e\u0012\u0004\u0012\u00020B\u0012\u0004\u0012\u00020\u00100\u001f2\u0012\u0010^\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00100\u001f2\u0012\u0010_\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00100\u001f2\u0006\u0010`\u001a\u00020%2\u0012\u0010a\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00100\u001f2\u0014\u0010b\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010R\u0012\u0004\u0012\u00020\u00100\u001f2\u0012\u0010c\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010d\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\f\u0010e\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\"\b\u0002\u0010f\u001a\u001c\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020B\u0012\u0004\u0012\u00020B\u0012\u0004\u0012\u00020\u0010\u0018\u00010gH\u0003\u001a\u00a2\u0001\u0010h\u001a\u00020\u00102\f\u0010i\u001a\b\u0012\u0004\u0012\u00020V0\u00012\u0006\u0010j\u001a\u00020%2\b\b\u0002\u0010$\u001a\u00020%2\u000e\b\u0002\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\u0016\b\u0002\u0010\'\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010(\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010)\u001a\u0010\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\u0016\b\u0002\u0010*\u001a\u0010\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u001f2\b\b\u0002\u0010!\u001a\u00020\"H\u0003\u001aH\u0010k\u001a\u00020\u00102\u0006\u0010l\u001a\u00020m2\u0006\u0010@\u001a\u00020%2\f\u0010n\u001a\b\u0012\u0004\u0012\u00020\u00100\u00182\u0014\b\u0002\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00140\u00122\n\b\u0002\u0010o\u001a\u0004\u0018\u00010pH\u0003\u001a2\u0010q\u001a\u00020\u00102\u0006\u0010r\u001a\u00020\u00142\u0012\u0010s\u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00100\u001f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u0018H\u0003\u001a(\u0010t\u001a\u00020p2\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00140\u00122\n\b\u0002\u0010o\u001a\u0004\u0018\u00010pH\u0002\u001a\u0010\u0010u\u001a\u00020\u00132\u0006\u0010v\u001a\u00020BH\u0002\u001a\u0010\u0010w\u001a\u00020\u00132\u0006\u0010v\u001a\u00020BH\u0002\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006x"}, d2 = {"ADJUST_TOOLS", "", "Lcom/rendly/app/ui/screens/publish/EditorTool;", "AUDIO_CONTEXT_TOOLS", "AUDIO_TOOLS", "CAPTIONS_TOOLS", "EDITOR_TOOLS", "EFFECTS_TOOLS", "EFFECT_CONTEXT_TOOLS", "FILTER_TOOLS", "STICKER_TOOLS", "TEXT_CONTEXT_TOOLS", "TEXT_TOOLS", "VIDEO_CONTEXT_TOOLS", "VOICE_TOOLS", "AdjustModal", "", "adjustValues", "", "", "", "onValueChange", "Lkotlin/Function2;", "onDismiss", "Lkotlin/Function0;", "AdjustModalCompact", "AdjustSliderPanel", "label", "value", "minValue", "maxValue", "Lkotlin/Function1;", "onReset", "modifier", "Landroidx/compose/ui/Modifier;", "AudioLayerTrack", "isSelected", "", "onClick", "onTrimStart", "onTrimEnd", "onLongPressDrag", "onTrimmingStateChange", "EditorHeader", "projectName", "onBack", "onNext", "EditorToolbar", "tools", "selectedToolId", "isContextMode", "onToolClick", "EffectsLayerTrack", "effectName", "widthFraction", "EffectsModal", "onEffectSelected", "EffectsModalCompact", "FiltersModal", "selectedFilter", "Lcom/rendly/app/ui/components/ImageFilter;", "onFilterSelected", "FiltersModalCompact", "PlaybackControls", "isPlaying", "currentPositionMs", "", "durationMs", "onPlayPause", "onUndo", "onRedo", "Playhead", "RendVideoEditor", "videoUri", "Landroid/net/Uri;", "SpeedModalCompact", "currentSpeed", "onSpeedChange", "TimelineArea", "layers", "Lcom/rendly/app/ui/screens/publish/TimelineLayer;", "timelineItems", "Lcom/rendly/app/ui/screens/publish/TimelineItem;", "selectedLayerIndex", "", "videoThumbnails", "Landroid/graphics/Bitmap;", "videoDurationMs", "timelineZoom", "isLoadingThumbnails", "isUserScrubbing", "selectedTimelineItem", "showAddClipMenu", "onSeek", "onZoomChange", "onUserScrubbingChange", "isProgrammaticScroll", "onProgrammaticScrollChange", "onItemSelected", "onAddClipMenuToggle", "onAddFromGallery", "onAddFromCamera", "onTrimVideo", "Lkotlin/Function3;", "VideoLayerTrack", "thumbnails", "isLoading", "VideoPreview", "exoPlayer", "Landroidx/media3/exoplayer/ExoPlayer;", "onTogglePlay", "filterColorMatrix", "", "VolumeModalCompact", "currentVolume", "onVolumeChange", "buildVideoColorMatrix", "formatDurationShort", "ms", "formatTimeMs", "app_debug"})
public final class RendVideoEditorKt {
    
    /**
     * Herramientas contextuales por tipo de item
     */
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> VIDEO_CONTEXT_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> AUDIO_CONTEXT_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> TEXT_CONTEXT_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> EFFECT_CONTEXT_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> EDITOR_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> AUDIO_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> TEXT_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> VOICE_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> STICKER_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> CAPTIONS_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> ADJUST_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> FILTER_TOOLS = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditorTool> EFFECTS_TOOLS = null;
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @androidx.compose.runtime.Composable
    public static final void RendVideoEditor(@org.jetbrains.annotations.NotNull
    android.net.Uri videoUri, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNext, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EditorHeader(java.lang.String projectName, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onNext) {
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @androidx.compose.runtime.Composable
    private static final void VideoPreview(androidx.media3.exoplayer.ExoPlayer exoPlayer, boolean isPlaying, kotlin.jvm.functions.Function0<kotlin.Unit> onTogglePlay, java.util.Map<java.lang.String, java.lang.Float> adjustValues, float[] filterColorMatrix) {
    }
    
    /**
     * Construye un ColorMatrix combinando los ajustes del usuario y el filtro seleccionado.
     * Retorna un FloatArray de 20 elementos (4x5 matrix).
     */
    private static final float[] buildVideoColorMatrix(java.util.Map<java.lang.String, java.lang.Float> adjustValues, float[] filterColorMatrix) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void PlaybackControls(boolean isPlaying, long currentPositionMs, long durationMs, kotlin.jvm.functions.Function0<kotlin.Unit> onPlayPause, kotlin.jvm.functions.Function0<kotlin.Unit> onUndo, kotlin.jvm.functions.Function0<kotlin.Unit> onRedo) {
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * TIMELINE AREA - Scroll Inteligente Anti "peleas"
     * ═══════════════════════════════════════════════════════════════════════════════
     *
     * Características:
     * - Detecta cuando el usuario está interactuando manualmente (drag/fling/scrub)
     * - animateScrollTo() solo cuando el usuario NO está tocando
     * - Scroll manual sin interferencia del sistema
     * - Experiencia idéntica a Instagram/CapCut
     */
    @androidx.compose.runtime.Composable
    private static final void TimelineArea(java.util.List<com.rendly.app.ui.screens.publish.TimelineLayer> layers, java.util.List<com.rendly.app.ui.screens.publish.TimelineItem> timelineItems, int selectedLayerIndex, java.util.List<android.graphics.Bitmap> videoThumbnails, long videoDurationMs, long currentPositionMs, float timelineZoom, boolean isLoadingThumbnails, boolean isUserScrubbing, com.rendly.app.ui.screens.publish.TimelineItem selectedTimelineItem, boolean showAddClipMenu, kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onSeek, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onZoomChange, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onUserScrubbingChange, boolean isProgrammaticScroll, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onProgrammaticScrollChange, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.screens.publish.TimelineItem, kotlin.Unit> onItemSelected, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onAddClipMenuToggle, kotlin.jvm.functions.Function0<kotlin.Unit> onAddFromGallery, kotlin.jvm.functions.Function0<kotlin.Unit> onAddFromCamera, kotlin.jvm.functions.Function3<? super java.lang.String, ? super java.lang.Long, ? super java.lang.Long, kotlin.Unit> onTrimVideo) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EffectsLayerTrack(java.lang.String effectName, boolean isSelected, float widthFraction, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onTrimStart, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onTrimEnd, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onTrimmingStateChange, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void VideoLayerTrack(java.util.List<android.graphics.Bitmap> thumbnails, boolean isLoading, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onTrimStart, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onTrimEnd, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onLongPressDrag, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onTrimmingStateChange, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AudioLayerTrack(boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onTrimStart, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onTrimEnd, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onLongPressDrag, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onTrimmingStateChange, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void Playhead(androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * EditorToolbar - Barra de herramientas con soporte para modo contextual
     *
     * Cuando isContextMode = true, muestra herramientas específicas del item
     * seleccionado con un botón de "back" a la izquierda para volver a las
     * herramientas principales.
     */
    @androidx.compose.runtime.Composable
    private static final void EditorToolbar(java.util.List<com.rendly.app.ui.screens.publish.EditorTool> tools, java.lang.String selectedToolId, boolean isContextMode, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onToolClick) {
    }
    
    private static final java.lang.String formatTimeMs(long ms) {
        return null;
    }
    
    private static final java.lang.String formatDurationShort(long ms) {
        return null;
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * PANEL DE AJUSTE - Slider profesional estilo Instagram/Lightroom
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    private static final void AdjustSliderPanel(java.lang.String label, float value, float minValue, float maxValue, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onValueChange, kotlin.jvm.functions.Function0<kotlin.Unit> onReset, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MODAL DE FILTROS - Carrusel profesional estilo Instagram
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    private static final void FiltersModal(com.rendly.app.ui.components.ImageFilter selectedFilter, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.ImageFilter, kotlin.Unit> onFilterSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MODAL DE EFECTOS - Panel profesional
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    private static final void EffectsModal(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onEffectSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MODAL DE AJUSTES - Panel completo profesional estilo Lightroom
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    private static final void AdjustModal(java.util.Map<java.lang.String, java.lang.Float> adjustValues, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Float, kotlin.Unit> onValueChange, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MODALES COMPACTOS - Preview visible sobre el modal (más altos, tapan controles)
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    private static final void FiltersModalCompact(com.rendly.app.ui.components.ImageFilter selectedFilter, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.ImageFilter, kotlin.Unit> onFilterSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EffectsModalCompact(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onEffectSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AdjustModalCompact(java.util.Map<java.lang.String, java.lang.Float> adjustValues, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Float, kotlin.Unit> onValueChange, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MODAL DE VELOCIDAD - Selector profesional de velocidad
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    private static final void SpeedModalCompact(float currentSpeed, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onSpeedChange, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MODAL DE VOLUMEN - Control profesional de volumen
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    @androidx.compose.runtime.Composable
    private static final void VolumeModalCompact(float currentVolume, kotlin.jvm.functions.Function1<? super java.lang.Float, kotlin.Unit> onVolumeChange, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}