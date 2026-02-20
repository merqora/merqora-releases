package com.rendly.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.rendly.app.MainActivity;
import com.rendly.app.R;
import com.rendly.app.data.repository.StoryRepository;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ8\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\b0\u000f2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\b0\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/rendly/app/service/StoryUploadService;", "", "()V", "CHANNEL_ID", "", "NOTIFICATION_ID", "", "createNotificationChannel", "", "context", "Landroid/content/Context;", "uploadStoryWithNotification", "bitmap", "Landroid/graphics/Bitmap;", "onComplete", "Lkotlin/Function0;", "onError", "Lkotlin/Function1;", "app_debug"})
public final class StoryUploadService {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String CHANNEL_ID = "story_upload_channel";
    private static final int NOTIFICATION_ID = 1001;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.service.StoryUploadService INSTANCE = null;
    
    private StoryUploadService() {
        super();
    }
    
    public final void createNotificationChannel(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    public final void uploadStoryWithNotification(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onComplete, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError) {
    }
}