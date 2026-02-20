package com.rendly.app.gpu;

import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a0\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\u0006H\u0007\u001a\u0012\u0010\n\u001a\u00020\u000b*\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a<\u0010\f\u001a\u00020\u000b*\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\u000e2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00102\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u0010\u001a,\u0010\u0012\u001a\u00020\u000b*\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\u000e2\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u0010\u00a8\u0006\u0014"}, d2 = {"TransformAnimationEffect", "", "state", "Lcom/rendly/app/gpu/TransformEngineState;", "rememberTransformEngine", "minScale", "", "maxScale", "friction", "snapAngle", "applyTransform", "Landroidx/compose/ui/Modifier;", "transformGestures", "enabled", "", "onTransformStart", "Lkotlin/Function0;", "onTransformEnd", "transformGesturesWithDoubleTap", "onDoubleTap", "app_debug"})
public final class TransformBridgeKt {
    
    @androidx.compose.runtime.Composable
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.gpu.TransformEngineState rememberTransformEngine(float minScale, float maxScale, float friction, float snapAngle) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final androidx.compose.ui.Modifier transformGestures(@org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier $this$transformGestures, @org.jetbrains.annotations.NotNull
    com.rendly.app.gpu.TransformEngineState state, boolean enabled, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onTransformStart, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onTransformEnd) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final androidx.compose.ui.Modifier applyTransform(@org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier $this$applyTransform, @org.jetbrains.annotations.NotNull
    com.rendly.app.gpu.TransformEngineState state) {
        return null;
    }
    
    /**
     * Extensión de transformGestures con soporte para doble tap para resetear (animado)
     */
    @org.jetbrains.annotations.NotNull
    public static final androidx.compose.ui.Modifier transformGesturesWithDoubleTap(@org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier $this$transformGesturesWithDoubleTap, @org.jetbrains.annotations.NotNull
    com.rendly.app.gpu.TransformEngineState state, boolean enabled, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDoubleTap) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    public static final void TransformAnimationEffect(@org.jetbrains.annotations.NotNull
    com.rendly.app.gpu.TransformEngineState state) {
    }
}