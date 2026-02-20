package com.rendly.app.ui.components;

import android.net.Uri;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.window.DialogProperties;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0015\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B%\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0002\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fj\u0002\b\u0010j\u0002\b\u0011j\u0002\b\u0012j\u0002\b\u0013j\u0002\b\u0014j\u0002\b\u0015j\u0002\b\u0016j\u0002\b\u0017j\u0002\b\u0018j\u0002\b\u0019j\u0002\b\u001aj\u0002\b\u001bj\u0002\b\u001c\u00a8\u0006\u001d"}, d2 = {"Lcom/rendly/app/ui/components/HighlightCategory;", "", "displayName", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "gradient", "", "Landroidx/compose/ui/graphics/Color;", "(Ljava/lang/String;ILjava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/util/List;)V", "getDisplayName", "()Ljava/lang/String;", "getGradient", "()Ljava/util/List;", "getIcon", "()Landroidx/compose/ui/graphics/vector/ImageVector;", "FASHION", "BEAUTY", "TECH", "HOME", "SPORTS", "FOOD", "TRAVEL", "MUSIC", "ART", "PETS", "OFFERS", "NEW", "CUSTOM", "app_debug"})
public enum HighlightCategory {
    /*public static final*/ FASHION /* = new FASHION(null, null, null) */,
    /*public static final*/ BEAUTY /* = new BEAUTY(null, null, null) */,
    /*public static final*/ TECH /* = new TECH(null, null, null) */,
    /*public static final*/ HOME /* = new HOME(null, null, null) */,
    /*public static final*/ SPORTS /* = new SPORTS(null, null, null) */,
    /*public static final*/ FOOD /* = new FOOD(null, null, null) */,
    /*public static final*/ TRAVEL /* = new TRAVEL(null, null, null) */,
    /*public static final*/ MUSIC /* = new MUSIC(null, null, null) */,
    /*public static final*/ ART /* = new ART(null, null, null) */,
    /*public static final*/ PETS /* = new PETS(null, null, null) */,
    /*public static final*/ OFFERS /* = new OFFERS(null, null, null) */,
    /*public static final*/ NEW /* = new NEW(null, null, null) */,
    /*public static final*/ CUSTOM /* = new CUSTOM(null, null, null) */;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String displayName = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.ui.graphics.vector.ImageVector icon = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<androidx.compose.ui.graphics.Color> gradient = null;
    
    HighlightCategory(java.lang.String displayName, androidx.compose.ui.graphics.vector.ImageVector icon, java.util.List<androidx.compose.ui.graphics.Color> gradient) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.compose.ui.graphics.vector.ImageVector getIcon() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<androidx.compose.ui.graphics.Color> getGradient() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.components.HighlightCategory> getEntries() {
        return null;
    }
}