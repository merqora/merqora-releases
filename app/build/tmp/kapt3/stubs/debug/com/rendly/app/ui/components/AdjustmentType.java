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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0015\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B/\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u0012\u0006\u0010\t\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\nR\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\fj\u0002\b\u0013j\u0002\b\u0014j\u0002\b\u0015j\u0002\b\u0016j\u0002\b\u0017j\u0002\b\u0018j\u0002\b\u0019j\u0002\b\u001aj\u0002\b\u001b\u00a8\u0006\u001c"}, d2 = {"Lcom/rendly/app/ui/components/AdjustmentType;", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "label", "", "min", "", "max", "default", "(Ljava/lang/String;ILandroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;FFF)V", "getDefault", "()F", "getIcon", "()Landroidx/compose/ui/graphics/vector/ImageVector;", "getLabel", "()Ljava/lang/String;", "getMax", "getMin", "BRIGHTNESS", "CONTRAST", "SATURATION", "EXPOSURE", "HIGHLIGHTS", "SHADOWS", "TEMPERATURE", "TINT", "GRAIN", "app_debug"})
public enum AdjustmentType {
    /*public static final*/ BRIGHTNESS /* = new BRIGHTNESS(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ CONTRAST /* = new CONTRAST(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ SATURATION /* = new SATURATION(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ EXPOSURE /* = new EXPOSURE(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ HIGHLIGHTS /* = new HIGHLIGHTS(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ SHADOWS /* = new SHADOWS(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ TEMPERATURE /* = new TEMPERATURE(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ TINT /* = new TINT(null, null, 0.0F, 0.0F, 0.0F) */,
    /*public static final*/ GRAIN /* = new GRAIN(null, null, 0.0F, 0.0F, 0.0F) */;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.ui.graphics.vector.ImageVector icon = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String label = null;
    private final float min = 0.0F;
    private final float max = 0.0F;
    
    AdjustmentType(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, float min, float max, float p4_772401952) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.compose.ui.graphics.vector.ImageVector getIcon() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLabel() {
        return null;
    }
    
    public final float getMin() {
        return 0.0F;
    }
    
    public final float getMax() {
        return 0.0F;
    }
    
    public final float getDefault() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.components.AdjustmentType> getEntries() {
        return null;
    }
}