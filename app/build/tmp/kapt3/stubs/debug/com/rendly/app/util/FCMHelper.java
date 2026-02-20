package com.rendly.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rendly.app.data.remote.SupabaseClient;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * FCM HELPER
 * ═══════════════════════════════════════════════════════════════════════════════
 * Helper para manejar Firebase Cloud Messaging
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0016\u0010\r\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u0010\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0012\u0010\u0010\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\u0013\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u001e\u0010\u0014\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u0016\u0010\u0017\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u000e\u0010\u0018\u001a\u00020\u00122\u0006\u0010\u0019\u001a\u00020\u001aJ\u0018\u0010\u001b\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001c\u001a\u00020\u0004H\u0002J&\u0010\u001d\u001a\u00020\n2\u0006\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010\u001eJ\u000e\u0010\u001f\u001a\u00020\n2\u0006\u0010 \u001a\u00020\u0004J\u000e\u0010!\u001a\u00020\n2\u0006\u0010 \u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/rendly/app/util/FCMHelper;", "", "()V", "KEY_PENDING_TOKEN", "", "NOTIFICATION_PERMISSION_REQUEST_CODE", "", "PREFS_NAME", "TAG", "clearPendingToken", "", "context", "Landroid/content/Context;", "forceTokenRefresh", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAppVersion", "getPendingToken", "hasNotificationPermission", "", "initialize", "onUserLogin", "userId", "(Landroid/content/Context;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "onUserLogout", "requestNotificationPermission", "activity", "Landroid/app/Activity;", "savePendingToken", "token", "saveTokenToSupabase", "(Ljava/lang/String;Ljava/lang/String;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "subscribeToTopic", "topic", "unsubscribeFromTopic", "app_debug"})
public final class FCMHelper {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "FCMHelper";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PREFS_NAME = "Merqora_fcm";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_PENDING_TOKEN = "pending_fcm_token";
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.util.FCMHelper INSTANCE = null;
    
    private FCMHelper() {
        super();
    }
    
    /**
     * Inicializa FCM y obtiene el token actual.
     * Llamar después del login del usuario.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object initialize(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Fuerza la regeneración del token FCM.
     * Útil cuando el token anterior es inválido.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object forceTokenRefresh(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Llamar después del login para registrar cualquier token pendiente.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object onUserLogin(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Llamar en logout para desactivar el token del dispositivo.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object onUserLogout(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Suscribirse a un tema para notificaciones grupales.
     */
    public final void subscribeToTopic(@org.jetbrains.annotations.NotNull
    java.lang.String topic) {
    }
    
    /**
     * Desuscribirse de un tema.
     */
    public final void unsubscribeFromTopic(@org.jetbrains.annotations.NotNull
    java.lang.String topic) {
    }
    
    /**
     * Verificar y solicitar permiso de notificaciones (Android 13+).
     * Llamar desde una Activity.
     */
    public final boolean requestNotificationPermission(@org.jetbrains.annotations.NotNull
    android.app.Activity activity) {
        return false;
    }
    
    /**
     * Verificar si tiene permiso de notificaciones.
     */
    public final boolean hasNotificationPermission(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    private final java.lang.Object saveTokenToSupabase(java.lang.String userId, java.lang.String token, android.content.Context context, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void savePendingToken(android.content.Context context, java.lang.String token) {
    }
    
    private final java.lang.String getPendingToken(android.content.Context context) {
        return null;
    }
    
    private final void clearPendingToken(android.content.Context context) {
    }
    
    private final java.lang.String getAppVersion(android.content.Context context) {
        return null;
    }
}