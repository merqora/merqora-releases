package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.animation.core.CubicBezierEasing;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.SpanStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.unit.Dp;
import coil.request.ImageRequest;
import coil.request.CachePolicy;
import com.rendly.app.data.model.Post;
import com.rendly.app.ui.theme.*;
import com.rendly.app.ui.components.VerifiedBadge;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J1\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\t\u00a8\u0006\u0018"}, d2 = {"Lcom/rendly/app/ui/components/PriceStrings;", "", "precioAnteriorText", "", "precioActualText", "ahorroText", "descuentoText", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAhorroText", "()Ljava/lang/String;", "getDescuentoText", "getPrecioActualText", "getPrecioAnteriorText", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
final class PriceStrings {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String precioAnteriorText = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String precioActualText = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String ahorroText = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String descuentoText = null;
    
    public PriceStrings(@org.jetbrains.annotations.NotNull
    java.lang.String precioAnteriorText, @org.jetbrains.annotations.NotNull
    java.lang.String precioActualText, @org.jetbrains.annotations.NotNull
    java.lang.String ahorroText, @org.jetbrains.annotations.NotNull
    java.lang.String descuentoText) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPrecioAnteriorText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPrecioActualText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getAhorroText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDescuentoText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.components.PriceStrings copy(@org.jetbrains.annotations.NotNull
    java.lang.String precioAnteriorText, @org.jetbrains.annotations.NotNull
    java.lang.String precioActualText, @org.jetbrains.annotations.NotNull
    java.lang.String ahorroText, @org.jetbrains.annotations.NotNull
    java.lang.String descuentoText) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}