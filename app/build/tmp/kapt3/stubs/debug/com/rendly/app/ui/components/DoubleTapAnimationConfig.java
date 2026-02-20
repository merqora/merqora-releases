package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import com.rendly.app.ui.theme.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * DoubleTapEcommerceAnimation - Animación Ultra Optimizada Estilo Instagram
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Arquitectura:
 * - Módulo independiente que NO depende de recomposiciones del post
 * - Renderizado como overlay usando graphicsLayer (GPU-accelerated)
 * - Pre-cálculo de valores para evitar allocations durante animación
 * - Animaciones con SpringSpec físico para fluidez natural
 *
 * Performance:
 * - Uso de Animatable para animaciones state-less
 * - graphicsLayer para transformaciones en GPU
 * - Sin allocations en hot path (remember + pre-computed values)
 * - Preparado para 60-120Hz refresh rates
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u000e\u0010\u0014\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/rendly/app/ui/components/DoubleTapAnimationConfig;", "", "()V", "FADE_IN_DURATION", "", "FADE_OUT_DURATION", "FINAL_SCALE", "", "HOLD_DURATION", "ICON_SIZE_DP", "INITIAL_SCALE", "MIN_TIME_BETWEEN_TAPS_MS", "", "PEAK_SCALE", "SCALE_IN_SPRING", "Landroidx/compose/animation/core/SpringSpec;", "getSCALE_IN_SPRING", "()Landroidx/compose/animation/core/SpringSpec;", "SCALE_OUT_SPRING", "getSCALE_OUT_SPRING", "TOTAL_DURATION_MS", "app_debug"})
public final class DoubleTapAnimationConfig {
    public static final int TOTAL_DURATION_MS = 900;
    public static final int ICON_SIZE_DP = 90;
    public static final float INITIAL_SCALE = 0.0F;
    public static final float PEAK_SCALE = 1.15F;
    public static final float FINAL_SCALE = 1.0F;
    @org.jetbrains.annotations.NotNull
    private static final androidx.compose.animation.core.SpringSpec<java.lang.Float> SCALE_IN_SPRING = null;
    @org.jetbrains.annotations.NotNull
    private static final androidx.compose.animation.core.SpringSpec<java.lang.Float> SCALE_OUT_SPRING = null;
    public static final int FADE_IN_DURATION = 100;
    public static final int HOLD_DURATION = 400;
    public static final int FADE_OUT_DURATION = 400;
    public static final long MIN_TIME_BETWEEN_TAPS_MS = 350L;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.ui.components.DoubleTapAnimationConfig INSTANCE = null;
    
    private DoubleTapAnimationConfig() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.compose.animation.core.SpringSpec<java.lang.Float> getSCALE_IN_SPRING() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.compose.animation.core.SpringSpec<java.lang.Float> getSCALE_OUT_SPRING() {
        return null;
    }
}