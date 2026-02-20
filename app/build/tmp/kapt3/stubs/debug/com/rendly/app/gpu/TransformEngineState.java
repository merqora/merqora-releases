package com.rendly.app.gpu;

import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0018\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0014\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010*\u001a\u00020+J\u001a\u0010,\u001a\u00020-2\b\b\u0002\u0010.\u001a\u00020/\u00f8\u0001\u0000\u00a2\u0006\u0004\b0\u00101J\u0006\u00102\u001a\u00020-J0\u00103\u001a\u00020-2\u0006\u00104\u001a\u00020/2\u0006\u00105\u001a\u00020\u00122\u0006\u00106\u001a\u00020\u00122\u0006\u00107\u001a\u00020/\u00f8\u0001\u0000\u00a2\u0006\u0004\b8\u00109J\u0006\u0010:\u001a\u00020-J\u0006\u0010;\u001a\u00020-J\u0016\u0010<\u001a\u00020-2\u0006\u0010=\u001a\u00020\u00122\u0006\u0010>\u001a\u00020\u0012J\u0016\u0010?\u001a\u00020-2\u0006\u0010=\u001a\u00020\u00122\u0006\u0010>\u001a\u00020\u0012J\b\u0010@\u001a\u00020-H\u0002J\u000e\u0010A\u001a\u00020\u00062\u0006\u0010B\u001a\u00020\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u000b\u0010\f\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u000e\u0010\r\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R+\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u00128F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001c\u0010\u001d\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001bR+\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u00128F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b!\u0010\u001d\u001a\u0004\b\u001f\u0010\u0019\"\u0004\b \u0010\u001bR+\u0010\"\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u00128F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b%\u0010\u001d\u001a\u0004\b#\u0010\u0019\"\u0004\b$\u0010\u001bR+\u0010&\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u00128F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b)\u0010\u001d\u001a\u0004\b\'\u0010\u0019\"\u0004\b(\u0010\u001b\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006C"}, d2 = {"Lcom/rendly/app/gpu/TransformEngineState;", "", "engine", "Lcom/rendly/app/gpu/TransformBridge;", "(Lcom/rendly/app/gpu/TransformBridge;)V", "<set-?>", "", "isAnimating", "()Z", "setAnimating", "(Z)V", "isAnimating$delegate", "Landroidx/compose/runtime/MutableState;", "isGestureActive", "isResettingAnimated", "lastFrameTime", "", "resetProgress", "", "resetStartRotation", "resetStartScale", "resetStartTranslateX", "resetStartTranslateY", "rotation", "getRotation", "()F", "setRotation", "(F)V", "rotation$delegate", "Landroidx/compose/runtime/MutableFloatState;", "scale", "getScale", "setScale", "scale$delegate", "translateX", "getTranslateX", "setTranslateX", "translateX$delegate", "translateY", "getTranslateY", "setTranslateY", "translateY$delegate", "getSnapshot", "Lcom/rendly/app/gpu/TransformStateSnapshot;", "onGestureEnd", "", "velocity", "Landroidx/compose/ui/geometry/Offset;", "onGestureEnd-k-4lQ0M", "(J)V", "onGestureStart", "onGestureUpdate", "pan", "zoom", "rotationDelta", "centroid", "onGestureUpdate-PZAlG8E", "(JFFJ)V", "reset", "resetAnimated", "setImageSize", "width", "height", "setViewportSize", "syncState", "update", "currentTimeMillis", "app_debug"})
public final class TransformEngineState {
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.gpu.TransformBridge engine = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableFloatState translateX$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableFloatState translateY$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableFloatState scale$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableFloatState rotation$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState isAnimating$delegate = null;
    private long lastFrameTime = 0L;
    private boolean isGestureActive = false;
    private boolean isResettingAnimated = false;
    private float resetStartTranslateX = 0.0F;
    private float resetStartTranslateY = 0.0F;
    private float resetStartScale = 1.0F;
    private float resetStartRotation = 0.0F;
    private float resetProgress = 0.0F;
    
    public TransformEngineState(@org.jetbrains.annotations.NotNull
    com.rendly.app.gpu.TransformBridge engine) {
        super();
    }
    
    public final float getTranslateX() {
        return 0.0F;
    }
    
    private final void setTranslateX(float p0) {
    }
    
    public final float getTranslateY() {
        return 0.0F;
    }
    
    private final void setTranslateY(float p0) {
    }
    
    public final float getScale() {
        return 0.0F;
    }
    
    private final void setScale(float p0) {
    }
    
    public final float getRotation() {
        return 0.0F;
    }
    
    private final void setRotation(float p0) {
    }
    
    public final boolean isAnimating() {
        return false;
    }
    
    private final void setAnimating(boolean p0) {
    }
    
    public final void setViewportSize(float width, float height) {
    }
    
    public final void setImageSize(float width, float height) {
    }
    
    public final void reset() {
    }
    
    /**
     * Reset animado - transición fluida hacia el estado inicial
     */
    public final void resetAnimated() {
    }
    
    public final void onGestureStart() {
    }
    
    public final boolean update(long currentTimeMillis) {
        return false;
    }
    
    private final void syncState() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.gpu.TransformStateSnapshot getSnapshot() {
        return null;
    }
}