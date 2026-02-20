package com.rendly.app.ui.components;

import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.window.DialogProperties;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000p\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0000\u001a\u00d7\u0001\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\u00a2\u0001\u0010\u000b\u001a\u009d\u0001\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u000f\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0010\u0012\u0015\u0012\u0013\u0018\u00010\u0011\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0013\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0014\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0015\u0012\u0013\u0012\u00110\u0004\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0016\u0012\u0004\u0012\u00020\u00060\f2\b\b\u0002\u0010\u0017\u001a\u00020\b2\b\b\u0002\u0010\u0018\u001a\u00020\u0019H\u0007\u001a2\u0010\u001a\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020\u001c2\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u001c\u0012\u0004\u0012\u00020\u00060\u001e2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a1\u0010 \u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00042\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\u0011\u0010!\u001a\r\u0012\u0004\u0012\u00020\u00060\n\u00a2\u0006\u0002\b\"H\u0003\u001a2\u0010#\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020$2\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u00060\u001e2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a<\u0010%\u001a\u00020\u00062\b\u0010\u0012\u001a\u0004\u0018\u00010\u00112\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001aB\u0010(\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020)2\u0006\u0010\u0010\u001a\u00020$2\u0006\u0010\u0014\u001a\u00020*2\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00060\u001e2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a2\u0010+\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020*2\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020*\u0012\u0004\u0012\u00020\u00060\u001e2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001aB\u0010,\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00042\b\u0010\u0012\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0010\u001a\u00020$2\u0006\u0010\u0013\u001a\u00020)2\u0006\u0010\u0014\u001a\u00020*2\u0006\u0010\u0015\u001a\u00020\u001c2\u0006\u0010\u0016\u001a\u00020\u0003H\u0003\u001aR\u0010-\u001a\u00020\u00062\u0012\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u00022\u0006\u0010\u0010\u001a\u00020$2\u001e\u0010\u001d\u001a\u001a\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u0002\u0012\u0004\u0012\u00020\u00060\u001e2\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\nH\u0003\u001a4\u0010.\u001a\u00020\u00062\f\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\f\u00100\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\u0006\u00101\u001a\u00020\b2\u0006\u0010\u0017\u001a\u00020\bH\u0003\u001a8\u00102\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\u00032\u0006\u00103\u001a\u00020\u00042\u0006\u00104\u001a\u00020\u00042\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00060\n2\b\b\u0002\u0010\u0018\u001a\u00020\u0019H\u0003\u001a\u0018\u00106\u001a\u00020\u00062\u0006\u00103\u001a\u00020\u00042\u0006\u00104\u001a\u00020\u0004H\u0003\u001a$\u00107\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00042\u0012\u00108\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00060\u001eH\u0003\u001a&\u00109\u001a\b\u0012\u0004\u0012\u00020:0\u00012\u0006\u0010\u0013\u001a\u00020)2\u0006\u0010\u0014\u001a\u00020*2\u0006\u0010\u0010\u001a\u00020$H\u0002\" \u0010\u0000\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00040\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006;"}, d2 = {"HIGHLIGHT_ICONS", "", "Lkotlin/Pair;", "Landroidx/compose/ui/graphics/vector/ImageVector;", "", "AddHighlightModal", "", "isVisible", "", "onDismiss", "Lkotlin/Function0;", "onCreateHighlight", "Lkotlin/Function7;", "Lkotlin/ParameterName;", "name", "title", "category", "Landroid/net/Uri;", "coverUri", "frameStyle", "frameColor", "backgroundColor", "icon", "isLoading", "modifier", "Landroidx/compose/ui/Modifier;", "BackgroundBottomSheet", "selected", "Lcom/rendly/app/ui/components/HighlightBackgroundColor;", "onSelect", "Lkotlin/Function1;", "onDone", "BottomSheetContainer", "content", "Landroidx/compose/runtime/Composable;", "CategoryBottomSheet", "Lcom/rendly/app/ui/components/HighlightCategory;", "CoverBottomSheet", "onSelectCover", "onRemoveCover", "FrameBottomSheet", "Lcom/rendly/app/ui/components/HighlightFrameStyle;", "Lcom/rendly/app/ui/components/HighlightFrameColor;", "FrameColorBottomSheet", "HighlightPreviewCompact", "IconBottomSheet", "ModalHeaderV2", "onClose", "onSave", "canSave", "OptionButton", "label", "value", "onClick", "SummaryItem", "TitleInputCompact", "onTitleChange", "resolveFrameGradient", "Landroidx/compose/ui/graphics/Color;", "app_debug"})
public final class AddHighlightModalKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<kotlin.Pair<androidx.compose.ui.graphics.vector.ImageVector, java.lang.String>> HIGHLIGHT_ICONS = null;
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void AddHighlightModal(boolean isVisible, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function7<? super java.lang.String, ? super java.lang.String, ? super android.net.Uri, ? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onCreateHighlight, boolean isLoading, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ModalHeaderV2(kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function0<kotlin.Unit> onSave, boolean canSave, boolean isLoading) {
    }
    
    /**
     * Resuelve el gradiente del marco según el estilo, color personalizado y categoría
     */
    private static final java.util.List<androidx.compose.ui.graphics.Color> resolveFrameGradient(com.rendly.app.ui.components.HighlightFrameStyle frameStyle, com.rendly.app.ui.components.HighlightFrameColor frameColor, com.rendly.app.ui.components.HighlightCategory category) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void HighlightPreviewCompact(java.lang.String title, android.net.Uri coverUri, com.rendly.app.ui.components.HighlightCategory category, com.rendly.app.ui.components.HighlightFrameStyle frameStyle, com.rendly.app.ui.components.HighlightFrameColor frameColor, com.rendly.app.ui.components.HighlightBackgroundColor backgroundColor, androidx.compose.ui.graphics.vector.ImageVector icon) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SummaryItem(java.lang.String label, java.lang.String value) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void TitleInputCompact(java.lang.String title, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTitleChange) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OptionButton(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, java.lang.String value, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void BottomSheetContainer(java.lang.String title, kotlin.jvm.functions.Function0<kotlin.Unit> onDone, kotlin.jvm.functions.Function0<kotlin.Unit> content) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CoverBottomSheet(android.net.Uri coverUri, kotlin.jvm.functions.Function0<kotlin.Unit> onSelectCover, kotlin.jvm.functions.Function0<kotlin.Unit> onRemoveCover, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CategoryBottomSheet(com.rendly.app.ui.components.HighlightCategory selected, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.HighlightCategory, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void FrameBottomSheet(com.rendly.app.ui.components.HighlightFrameStyle selected, com.rendly.app.ui.components.HighlightCategory category, com.rendly.app.ui.components.HighlightFrameColor frameColor, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.HighlightFrameStyle, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void FrameColorBottomSheet(com.rendly.app.ui.components.HighlightFrameColor selected, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.HighlightFrameColor, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void BackgroundBottomSheet(com.rendly.app.ui.components.HighlightBackgroundColor selected, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.HighlightBackgroundColor, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void IconBottomSheet(kotlin.Pair<androidx.compose.ui.graphics.vector.ImageVector, java.lang.String> selected, com.rendly.app.ui.components.HighlightCategory category, kotlin.jvm.functions.Function1<? super kotlin.Pair<androidx.compose.ui.graphics.vector.ImageVector, java.lang.String>, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
}