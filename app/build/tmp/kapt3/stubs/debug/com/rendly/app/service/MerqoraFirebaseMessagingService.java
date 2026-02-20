package com.rendly.app.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.Lifecycle;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rendly.app.MainActivity;
import com.rendly.app.R;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import java.net.URL;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * Merqora FIREBASE MESSAGING SERVICE
 * ═══════════════════════════════════════════════════════════════════════════════
 * Maneja las notificaciones push incluso cuando la app está cerrada.
 *
 * Tipos de notificaciones soportadas:
 * - message: Nuevo mensaje de chat
 * - like: Alguien dio like a tu publicación
 * - comment: Nuevo comentario en tu publicación
 * - follow: Alguien te empezó a seguir
 * - sale: Nueva venta realizada
 * - handshake: Solicitud o confirmación de handshake
 * - mention: Te mencionaron en un comentario
 * - promotion: Promociones y ofertas especiales
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0011\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 -2\u00020\u0001:\u0001-B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0007\u001a\u00020\bH\u0002J\b\u0010\t\u001a\u00020\bH\u0016J\b\u0010\n\u001a\u00020\bH\u0016J\u0010\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rH\u0016J\u0010\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0016J\u0010\u0010\u0011\u001a\u00020\b2\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J\u001e\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\u00102\u0006\u0010\u000f\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010\u0016J,\u0010\u0017\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\u00102\b\u0010\u0019\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u001a\u001a\u00020\u00102\b\u0010\u001b\u001a\u0004\u0018\u00010\u0010H\u0002J\"\u0010\u001c\u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u00102\b\u0010\u001f\u001a\u0004\u0018\u00010\u0010H\u0002J,\u0010 \u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u00102\b\u0010!\u001a\u0004\u0018\u00010\u00102\b\u0010\"\u001a\u0004\u0018\u00010\u0010H\u0002J\u0018\u0010#\u001a\u00020\b2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'H\u0002J\"\u0010(\u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u00102\b\u0010\u001f\u001a\u0004\u0018\u00010\u0010H\u0002J0\u0010)\u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u00102\n\b\u0002\u0010*\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0010H\u0002J\"\u0010+\u001a\u00020\b2\u0006\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u00102\b\u0010,\u001a\u0004\u0018\u00010\u0010H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/rendly/app/service/MerqoraFirebaseMessagingService;", "Lcom/google/firebase/messaging/FirebaseMessagingService;", "()V", "serviceJob", "Lkotlinx/coroutines/CompletableJob;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "createNotificationChannels", "", "onCreate", "onDestroy", "onMessageReceived", "remoteMessage", "Lcom/google/firebase/messaging/RemoteMessage;", "onNewToken", "token", "", "playCustomSound", "isMessage", "", "saveFcmToken", "userId", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "showCallNotification", "callerName", "callId", "callType", "callerAvatar", "showGeneralNotification", "title", "body", "imageUrl", "showMessageNotification", "chatId", "senderAvatar", "showNotification", "id", "", "builder", "Landroidx/core/app/NotificationCompat$Builder;", "showPromotionNotification", "showSocialNotification", "postId", "showTransactionNotification", "transactionId", "Companion", "app_debug"})
public final class MerqoraFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CompletableJob serviceJob = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "MerqoraFCM";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CHANNEL_MESSAGES = "Merqora_messages";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CHANNEL_SOCIAL = "Merqora_social";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CHANNEL_TRANSACTIONS = "Merqora_transactions";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CHANNEL_PROMOTIONS = "Merqora_promotions";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CHANNEL_SYSTEM = "Merqora_system";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String CHANNEL_CALLS = "Merqora_calls";
    private static final int NOTIFICATION_ID_MESSAGE = 1000;
    private static final int NOTIFICATION_ID_SOCIAL = 2000;
    private static final int NOTIFICATION_ID_TRANSACTION = 3000;
    private static final int NOTIFICATION_ID_PROMOTION = 4000;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.service.MerqoraFirebaseMessagingService.Companion Companion = null;
    
    public MerqoraFirebaseMessagingService() {
        super();
    }
    
    private final void playCustomSound(boolean isMessage) {
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    @java.lang.Override
    public void onDestroy() {
    }
    
    /**
     * Llamado cuando se recibe un nuevo token FCM.
     * El token debe ser enviado a Supabase para poder enviar notificaciones a este dispositivo.
     */
    @java.lang.Override
    public void onNewToken(@org.jetbrains.annotations.NotNull
    java.lang.String token) {
    }
    
    /**
     * Llamado cuando se recibe un mensaje push.
     * Puede contener data y/o notification payload.
     */
    @java.lang.Override
    public void onMessageReceived(@org.jetbrains.annotations.NotNull
    com.google.firebase.messaging.RemoteMessage remoteMessage) {
    }
    
    private final void showMessageNotification(java.lang.String title, java.lang.String body, java.lang.String chatId, java.lang.String senderAvatar) {
    }
    
    private final void showSocialNotification(java.lang.String title, java.lang.String body, java.lang.String postId, java.lang.String userId) {
    }
    
    private final void showTransactionNotification(java.lang.String title, java.lang.String body, java.lang.String transactionId) {
    }
    
    private final void showPromotionNotification(java.lang.String title, java.lang.String body, java.lang.String imageUrl) {
    }
    
    private final void showCallNotification(java.lang.String callerName, java.lang.String callId, java.lang.String callType, java.lang.String callerAvatar) {
    }
    
    private final void showGeneralNotification(java.lang.String title, java.lang.String body, java.lang.String imageUrl) {
    }
    
    private final void showNotification(int id, androidx.core.app.NotificationCompat.Builder builder) {
    }
    
    private final void createNotificationChannels() {
    }
    
    private final java.lang.Object saveFcmToken(java.lang.String userId, java.lang.String token, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0010\u001a\u00020\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/rendly/app/service/MerqoraFirebaseMessagingService$Companion;", "", "()V", "CHANNEL_CALLS", "", "CHANNEL_MESSAGES", "CHANNEL_PROMOTIONS", "CHANNEL_SOCIAL", "CHANNEL_SYSTEM", "CHANNEL_TRANSACTIONS", "NOTIFICATION_ID_MESSAGE", "", "NOTIFICATION_ID_PROMOTION", "NOTIFICATION_ID_SOCIAL", "NOTIFICATION_ID_TRANSACTION", "TAG", "isAppInForeground", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isAppInForeground() {
            return false;
        }
    }
}