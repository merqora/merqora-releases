package com.rendly.app.ui.components;

import android.graphics.Bitmap;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import com.rendly.app.gpu.TransformBridge;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a\u00b4\u0001\u0010\u0000\u001a\u00020\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\b\b\u0002\u0010\f\u001a\u00020\r2d\b\u0002\u0010\u000e\u001a^\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0012\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0013\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0014\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b\u0010\u0012\b\b\u0011\u0012\u0004\b\b(\u0015\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u000fH\u0007\u00a8\u0006\u0016"}, d2 = {"TransformableImage", "", "bitmap", "Landroid/graphics/Bitmap;", "modifier", "Landroidx/compose/ui/Modifier;", "minScale", "", "maxScale", "enableRotation", "", "snapToAngles", "contentScale", "Landroidx/compose/ui/layout/ContentScale;", "onTransformChanged", "Lkotlin/Function4;", "Lkotlin/ParameterName;", "name", "translateX", "translateY", "scale", "rotation", "app_debug"})
public final class TransformableImageKt {
    
    /**
     * Imagen transformable con motor C++ profesional
     *
     * Características:
     * - Pan/Scale/Rotate con gestos multitouch
     * - Inercia física al soltar
     * - Rubber-band en límites de zoom
     * - Zoom logarítmico para UX natural
     * - 60+ FPS garantizado
     */
    @androidx.compose.runtime.Composable
    public static final void TransformableImage(@org.jetbrains.annotations.Nullable
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, float minScale, float maxScale, boolean enableRotation, boolean snapToAngles, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.layout.ContentScale contentScale, @org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function4<? super java.lang.Float, ? super java.lang.Float, ? super java.lang.Float, ? super java.lang.Float, kotlin.Unit> onTransformChanged) {
    }
}