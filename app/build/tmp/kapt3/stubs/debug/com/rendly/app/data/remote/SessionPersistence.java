package com.rendly.app.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper para persistir estado de sesión
 * Simple y rápido - no afecta cold start
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\t\u001a\u00020\nJ\b\u0010\u000b\u001a\u0004\u0018\u00010\u0004J\u000e\u0010\f\u001a\u00020\n2\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/rendly/app/data/remote/SessionPersistence;", "", "()V", "KEY_IS_LOGGED_IN", "", "KEY_USER_ID", "PREFS_NAME", "prefs", "Landroid/content/SharedPreferences;", "clearSession", "", "getUserId", "init", "context", "Landroid/content/Context;", "isLoggedIn", "", "saveSession", "userId", "app_debug"})
public final class SessionPersistence {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PREFS_NAME = "Merqora_session";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_USER_ID = "user_id";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_IS_LOGGED_IN = "is_logged_in";
    @org.jetbrains.annotations.Nullable
    private static android.content.SharedPreferences prefs;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.remote.SessionPersistence INSTANCE = null;
    
    private SessionPersistence() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    public final void saveSession(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
    }
    
    public final boolean isLoggedIn() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUserId() {
        return null;
    }
    
    public final void clearSession() {
    }
}