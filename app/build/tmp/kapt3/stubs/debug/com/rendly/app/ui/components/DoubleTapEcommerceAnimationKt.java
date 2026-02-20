package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a\u001a\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\nH\u0007\u001aJ\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00060\u00112\b\b\u0002\u0010\t\u001a\u00020\n2\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0013\u0010\u0014\u001a\u0018\u0010\u0015\u001a\u00020\b2\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001H\u0007\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0016"}, d2 = {"defaultEcommerceIcons", "", "Lcom/rendly/app/ui/components/EcommerceIcon;", "getDefaultEcommerceIcons", "()Ljava/util/List;", "DoubleTapAnimationOverlay", "", "state", "Lcom/rendly/app/ui/components/DoubleTapAnimationState;", "modifier", "Landroidx/compose/ui/Modifier;", "DoubleTapEcommerceAnimation", "show", "", "tapOffset", "Landroidx/compose/ui/geometry/Offset;", "onAnimationEnd", "Lkotlin/Function0;", "icons", "DoubleTapEcommerceAnimation-YqVAtuI", "(ZJLkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;Ljava/util/List;)V", "rememberDoubleTapAnimationState", "app_debug"})
public final class DoubleTapEcommerceAnimationKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.components.EcommerceIcon> defaultEcommerceIcons = null;
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<com.rendly.app.ui.components.EcommerceIcon> getDefaultEcommerceIcons() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.ui.components.DoubleTapAnimationState rememberDoubleTapAnimationState(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.ui.components.EcommerceIcon> icons) {
        return null;
    }
    
    /**
     * Overlay de animación para doble tap.
     *
     * USO:
     * ```kotlin
     * val animState = rememberDoubleTapAnimationState()
     *
     * Box {
     *    // Tu contenido (imagen del post)
     *    AsyncImage(...)
     *    
     *    // Overlay de animación
     *    DoubleTapAnimationOverlay(
     *        state = animState,
     *        modifier = Modifier.fillMaxSize()
     *    )
     * }
     *
     * // En el detector de gestos:
     * pointerInput(Unit) {
     *    detectTapGestures(
     *        onDoubleTap = { offset ->
     *            scope.launch {
     *                if (animState.trigger(offset)) {
     *                    onLikeClick()
     *                }
     *            }
     *        }
     *    )
     * }
     * ```
     */
    @androidx.compose.runtime.Composable
    public static final void DoubleTapAnimationOverlay(@org.jetbrains.annotations.NotNull
    com.rendly.app.ui.components.DoubleTapAnimationState state, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
}