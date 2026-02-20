package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.HandshakeTransaction;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.ui.theme.*;

/**
 * Estado del banner de handshake
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rendly/app/ui/components/HandshakeBannerState;", "", "(Ljava/lang/String;I)V", "WAITING", "ACCEPTED", "COMPLETED", "CANCELLED", "app_debug"})
public enum HandshakeBannerState {
    /*public static final*/ WAITING /* = new WAITING() */,
    /*public static final*/ ACCEPTED /* = new ACCEPTED() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */;
    
    HandshakeBannerState() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.components.HandshakeBannerState> getEntries() {
        return null;
    }
}