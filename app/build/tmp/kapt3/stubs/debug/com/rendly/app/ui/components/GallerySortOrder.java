package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.window.DialogProperties;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rendly/app/ui/components/GallerySortOrder;", "", "(Ljava/lang/String;I)V", "RECENT_FIRST", "OLDEST_FIRST", "LARGEST_FIRST", "SMALLEST_FIRST", "app_debug"})
public enum GallerySortOrder {
    /*public static final*/ RECENT_FIRST /* = new RECENT_FIRST() */,
    /*public static final*/ OLDEST_FIRST /* = new OLDEST_FIRST() */,
    /*public static final*/ LARGEST_FIRST /* = new LARGEST_FIRST() */,
    /*public static final*/ SMALLEST_FIRST /* = new SMALLEST_FIRST() */;
    
    GallerySortOrder() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.components.GallerySortOrder> getEntries() {
        return null;
    }
}