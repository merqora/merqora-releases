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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000f\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0019\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000ej\u0002\b\u000fj\u0002\b\u0010j\u0002\b\u0011j\u0002\b\u0012j\u0002\b\u0013\u00a8\u0006\u0014"}, d2 = {"Lcom/rendly/app/ui/components/HighlightFrameStyle;", "", "displayName", "", "isPremium", "", "(Ljava/lang/String;ILjava/lang/String;Z)V", "getDisplayName", "()Ljava/lang/String;", "()Z", "CLASSIC", "THIN", "BOLD", "DOUBLE", "DASHED", "GLOW", "NEON", "RAINBOW", "GOLDEN", "DIAMOND", "app_debug"})
public enum HighlightFrameStyle {
    /*public static final*/ CLASSIC /* = new CLASSIC(null, false) */,
    /*public static final*/ THIN /* = new THIN(null, false) */,
    /*public static final*/ BOLD /* = new BOLD(null, false) */,
    /*public static final*/ DOUBLE /* = new DOUBLE(null, false) */,
    /*public static final*/ DASHED /* = new DASHED(null, false) */,
    /*public static final*/ GLOW /* = new GLOW(null, false) */,
    /*public static final*/ NEON /* = new NEON(null, false) */,
    /*public static final*/ RAINBOW /* = new RAINBOW(null, false) */,
    /*public static final*/ GOLDEN /* = new GOLDEN(null, false) */,
    /*public static final*/ DIAMOND /* = new DIAMOND(null, false) */;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String displayName = null;
    private final boolean isPremium = false;
    
    HighlightFrameStyle(java.lang.String displayName, boolean isPremium) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    public final boolean isPremium() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.components.HighlightFrameStyle> getEntries() {
        return null;
    }
}