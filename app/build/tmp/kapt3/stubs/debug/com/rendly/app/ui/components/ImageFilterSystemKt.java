package com.rendly.app.ui.components;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.RenderEffect;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import kotlinx.coroutines.Dispatchers;
import java.util.concurrent.Executors;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a6\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00022\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u000b2\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u001a8\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00022\u0006\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00060\u00142\b\b\u0002\u0010\f\u001a\u00020\rH\u0003\u001a,\u0010\u0015\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u00022\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0007\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\u00a8\u0006\u0018"}, d2 = {"STORY_FILTERS", "", "Lcom/rendly/app/ui/components/ImageFilter;", "getSTORY_FILTERS", "()Ljava/util/List;", "FilterCarousel", "", "bitmap", "Landroid/graphics/Bitmap;", "currentFilter", "onFilterSelected", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "FilterItem", "filter", "thumbnail", "isSelected", "", "onClick", "Lkotlin/Function0;", "FilteredImage", "contentScale", "Landroidx/compose/ui/layout/ContentScale;", "app_debug"})
public final class ImageFilterSystemKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.ImageFilter> STORY_FILTERS = null;
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.ImageFilter> getSTORY_FILTERS() {
        return null;
    }
    
    /**
     * Composable que aplica filtro en GPU sin crear Bitmaps
     * Usa drawWithContent con ColorMatrixColorFilter (GPU-accelerated en Android)
     * Preparado para migración futura a OpenGL ES / shaders
     */
    @androidx.compose.runtime.Composable
    public static final void FilteredImage(@org.jetbrains.annotations.NotNull
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.components.ImageFilter filter, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.layout.ContentScale contentScale) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void FilterCarousel(@org.jetbrains.annotations.NotNull
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.components.ImageFilter currentFilter, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.ui.components.ImageFilter, kotlin.Unit> onFilterSelected, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void FilterItem(com.rendly.app.ui.components.ImageFilter filter, android.graphics.Bitmap thumbnail, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
}