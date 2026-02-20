package com.rendly.app.ui.components;

import android.graphics.Bitmap;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.unit.Dp;
import com.rendly.app.gpu.GPUImageSurfaceView;
import com.rendly.app.gpu.ImageAdjustmentState;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a.\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0003\u001aH\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u00032\u0014\u0010\r\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\b\b\u0002\u0010\u0010\u001a\u00020\u0011H\u0007\u001a\u009e\u0001\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00052\b\u0010\u0014\u001a\u0004\u0018\u00010\u00152\b\b\u0002\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\u0016\b\u0002\u0010\r\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u0003\u0012\u0004\u0012\u00020\u00010\u000e2\u0014\b\u0002\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0017\u001a\u00020\u00182\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001a\u0010\u001b\u001aF\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00052\b\u0010\u0014\u001a\u0004\u0018\u00010\u00152\b\b\u0002\u0010\u0010\u001a\u00020\u00112\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001d"}, d2 = {"AdjustmentToolItem", "", "type", "Lcom/rendly/app/ui/components/AdjustmentType;", "isSelected", "", "hasValue", "onClick", "Lkotlin/Function0;", "AdjustmentToolsCarousel", "adjustState", "Lcom/rendly/app/ui/components/ImageAdjustState;", "selectedAdjustment", "onAdjustmentSelected", "Lkotlin/Function1;", "onApply", "modifier", "Landroidx/compose/ui/Modifier;", "ImageAdjustOverlay", "visible", "bitmap", "Landroid/graphics/Bitmap;", "onValueChange", "previewHeight", "Landroidx/compose/ui/unit/Dp;", "onDismiss", "ImageAdjustOverlay-1sJa4KU", "(ZLandroid/graphics/Bitmap;Lcom/rendly/app/ui/components/ImageAdjustState;Lcom/rendly/app/ui/components/AdjustmentType;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;FLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;)V", "ImageAdjustOverlayLegacy", "app_debug"})
public final class ImageAdjustOverlayKt {
    
    /**
     * Carrusel de herramientas de ajuste - Reemplaza botones Tu Vitrina/Frecuentes
     * Diseño horizontal con botones circulares uniformes, estilo Instagram
     * El botón Tick se mueve a la esquina superior derecha del preview
     */
    @androidx.compose.runtime.Composable
    public static final void AdjustmentToolsCarousel(@org.jetbrains.annotations.NotNull
    com.rendly.app.ui.components.ImageAdjustState adjustState, @org.jetbrains.annotations.Nullable
    com.rendly.app.ui.components.AdjustmentType selectedAdjustment, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.AdjustmentType, kotlin.Unit> onAdjustmentSelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onApply, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AdjustmentToolItem(com.rendly.app.ui.components.AdjustmentType type, boolean isSelected, boolean hasValue, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void ImageAdjustOverlayLegacy(boolean visible, @org.jetbrains.annotations.Nullable
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.ImageAdjustState, kotlin.Unit> onApply, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}