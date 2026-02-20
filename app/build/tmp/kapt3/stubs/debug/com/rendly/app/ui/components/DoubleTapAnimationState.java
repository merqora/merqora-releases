package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u00002\u00020\u0001B\u0015\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0002\u0010\u0005J\u001b\u0010*\u001a\u00020\u00182\u0006\u0010+\u001a\u00020#H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0004\b,\u0010-R\u001d\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\f\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000eR+\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u000f\u001a\u00020\u00108B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0016\u0010\u0017\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\u0019\u001a\u00020\u00182\u0006\u0010\u000f\u001a\u00020\u00188F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001d\u0010\u001e\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u000e\u0010\u001f\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u000bR1\u0010$\u001a\u00020#2\u0006\u0010\u000f\u001a\u00020#8F@BX\u0086\u008e\u0002\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0012\n\u0004\b)\u0010\u001e\u001a\u0004\b%\u0010&\"\u0004\b\'\u0010(\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u0006."}, d2 = {"Lcom/rendly/app/ui/components/DoubleTapAnimationState;", "", "icons", "", "Lcom/rendly/app/ui/components/EcommerceIcon;", "(Ljava/util/List;)V", "alpha", "Landroidx/compose/animation/core/Animatable;", "", "Landroidx/compose/animation/core/AnimationVector1D;", "getAlpha", "()Landroidx/compose/animation/core/Animatable;", "currentIcon", "getCurrentIcon", "()Lcom/rendly/app/ui/components/EcommerceIcon;", "<set-?>", "", "currentIconIndex", "getCurrentIconIndex", "()I", "setCurrentIconIndex", "(I)V", "currentIconIndex$delegate", "Landroidx/compose/runtime/MutableIntState;", "", "isVisible", "()Z", "setVisible", "(Z)V", "isVisible$delegate", "Landroidx/compose/runtime/MutableState;", "lastTapTime", "", "scale", "getScale", "Landroidx/compose/ui/geometry/Offset;", "tapOffset", "getTapOffset-F1C5BW0", "()J", "setTapOffset-k-4lQ0M", "(J)V", "tapOffset$delegate", "trigger", "offset", "trigger-3MmeM6k", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class DoubleTapAnimationState {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.rendly.app.ui.components.EcommerceIcon> icons = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.animation.core.Animatable<java.lang.Float, androidx.compose.animation.core.AnimationVector1D> scale = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.animation.core.Animatable<java.lang.Float, androidx.compose.animation.core.AnimationVector1D> alpha = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState isVisible$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState tapOffset$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableIntState currentIconIndex$delegate = null;
    private long lastTapTime = 0L;
    
    public DoubleTapAnimationState(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.ui.components.EcommerceIcon> icons) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.compose.animation.core.Animatable<java.lang.Float, androidx.compose.animation.core.AnimationVector1D> getScale() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.compose.animation.core.Animatable<java.lang.Float, androidx.compose.animation.core.AnimationVector1D> getAlpha() {
        return null;
    }
    
    public final boolean isVisible() {
        return false;
    }
    
    private final void setVisible(boolean p0) {
    }
    
    private final int getCurrentIconIndex() {
        return 0;
    }
    
    private final void setCurrentIconIndex(int p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.EcommerceIcon getCurrentIcon() {
        return null;
    }
    
    public DoubleTapAnimationState() {
        super();
    }
}