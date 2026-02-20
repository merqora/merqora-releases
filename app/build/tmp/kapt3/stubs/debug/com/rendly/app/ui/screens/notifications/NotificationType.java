package com.rendly.app.ui.screens.notifications;

import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/rendly/app/ui/screens/notifications/NotificationType;", "", "(Ljava/lang/String;I)V", "LIKE", "COMMENT", "FOLLOW", "SALE", "MENTION", "SYSTEM", "app_debug"})
public enum NotificationType {
    /*public static final*/ LIKE /* = new LIKE() */,
    /*public static final*/ COMMENT /* = new COMMENT() */,
    /*public static final*/ FOLLOW /* = new FOLLOW() */,
    /*public static final*/ SALE /* = new SALE() */,
    /*public static final*/ MENTION /* = new MENTION() */,
    /*public static final*/ SYSTEM /* = new SYSTEM() */;
    
    NotificationType() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.screens.notifications.NotificationType> getEntries() {
        return null;
    }
}