package com.rendly.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.rendly.app.MainActivity;
import com.rendly.app.R;
import com.rendly.app.util.SoundManager;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010#\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0004J\u000e\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ.\u0010\u000f\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/rendly/app/service/ChatNotificationService;", "", "()V", "CHANNEL_ID", "", "NOTIFICATION_ID_BASE", "", "shownNotifications", "", "cancelChatNotifications", "", "context", "Landroid/content/Context;", "conversationId", "createNotificationChannel", "showMessageNotification", "senderName", "messageContent", "messageId", "app_debug"})
public final class ChatNotificationService {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String CHANNEL_ID = "chat_messages_channel";
    private static final int NOTIFICATION_ID_BASE = 2000;
    @org.jetbrains.annotations.NotNull
    private static final java.util.Set<java.lang.String> shownNotifications = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.service.ChatNotificationService INSTANCE = null;
    
    private ChatNotificationService() {
        super();
    }
    
    public final void createNotificationChannel(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    public final void showMessageNotification(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String senderName, @org.jetbrains.annotations.NotNull
    java.lang.String messageContent, @org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String messageId) {
    }
    
    public final void cancelChatNotifications(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String conversationId) {
    }
}