package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.gestures.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.pager.PagerDefaults;
import androidx.compose.foundation.pager.PagerSnapDistance;
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection;
import androidx.compose.ui.input.nestedscroll.NestedScrollSource;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.input.pointer.util.VelocityTracker;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import coil.request.ImageRequest;
import coil.request.CachePolicy;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000:\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\u001aJ\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0005\u001a\u00020\u00042\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00010\t2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0007\u001a^\u0010\r\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\u0005\u001a\u00020\u00042\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u0010H\u0007\u001a\u001a\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\u00042\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u0002\u00a8\u0006\u0016"}, d2 = {"PostImageCarousel", "", "images", "", "", "contentDescription", "onTap", "Lkotlin/Function0;", "onDoubleTap", "Lkotlin/Function1;", "Landroidx/compose/ui/geometry/Offset;", "modifier", "Landroidx/compose/ui/Modifier;", "PremiumImageCarousel", "onImageTap", "showIndicators", "", "showCounter", "optimizeCloudinaryUrl", "url", "width", "", "app_debug"})
public final class PremiumImageCarouselKt {
    
    /**
     * Optimiza URLs de Cloudinary para máximo rendimiento
     * Aplica: width, quality auto, format auto
     */
    private static final java.lang.String optimizeCloudinaryUrl(java.lang.String url, int width) {
        return null;
    }
    
    /**
     * PremiumImageCarousel - Carrusel de imágenes optimizado estilo Instagram
     *
     * Características:
     * - Snap instantáneo con física natural
     * - Detección inteligente de intención del gesto
     * - Animaciones en GPU (graphicsLayer)
     * - Umbral de velocidad bajo para cambio rápido
     * - Pre-carga de imágenes adyacentes
     * - Sin conflicto con scroll vertical del feed
     */
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void PremiumImageCarousel(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull
    java.lang.String contentDescription, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onImageTap, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDoubleTap, boolean showIndicators, boolean showCounter) {
    }
    
    /**
     * Versión simplificada para uso en PostItem con callbacks existentes
     *
     * OPTIMIZACIÓN CRÍTICA: Este carrusel consume eventos horizontales para evitar
     * conflictos con el HorizontalPager principal de navegación entre secciones.
     * Cuando hay múltiples imágenes, el carrusel "roba" los gestos horizontales.
     */
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void PostImageCarousel(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, @org.jetbrains.annotations.NotNull
    java.lang.String contentDescription, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onTap, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super androidx.compose.ui.geometry.Offset, kotlin.Unit> onDoubleTap, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
}