package com.rendly.app.ui.components;

import androidx.compose.animation.core.*;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import com.rendly.app.data.repository.FollowersRepository;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.input.pointer.util.VelocityTracker;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Notification;
import com.rendly.app.data.model.NotificationType;
import com.rendly.app.data.repository.NotificationRepository;
import com.rendly.app.data.repository.SystemNotificationRepository;
import com.rendly.app.data.repository.SystemNotification;
import com.rendly.app.ui.theme.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u000e\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/rendly/app/ui/components/OptimizedNotificationType;", "", "(Ljava/lang/String;I)V", "LIKE", "SAVE", "COMMENT", "FOLLOW", "PURCHASE", "SALE", "MENTION", "SYSTEM", "CLIENT_REQUEST", "CLIENT_ACCEPTED", "CLIENT_REJECTED", "CLIENT_PENDING", "app_debug"})
public enum OptimizedNotificationType {
    /*public static final*/ LIKE /* = new LIKE() */,
    /*public static final*/ SAVE /* = new SAVE() */,
    /*public static final*/ COMMENT /* = new COMMENT() */,
    /*public static final*/ FOLLOW /* = new FOLLOW() */,
    /*public static final*/ PURCHASE /* = new PURCHASE() */,
    /*public static final*/ SALE /* = new SALE() */,
    /*public static final*/ MENTION /* = new MENTION() */,
    /*public static final*/ SYSTEM /* = new SYSTEM() */,
    /*public static final*/ CLIENT_REQUEST /* = new CLIENT_REQUEST() */,
    /*public static final*/ CLIENT_ACCEPTED /* = new CLIENT_ACCEPTED() */,
    /*public static final*/ CLIENT_REJECTED /* = new CLIENT_REJECTED() */,
    /*public static final*/ CLIENT_PENDING /* = new CLIENT_PENDING() */;
    
    OptimizedNotificationType() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.components.OptimizedNotificationType> getEntries() {
        return null;
    }
}