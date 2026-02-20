package com.rendly.app.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import com.rendly.app.R;

/**
 * Gestor de sonidos para notificaciones y mensajes
 * Usa los archivos de sonido en res/raw/
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0017B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\bJ\u0006\u0010\u0011\u001a\u00020\u000fJ\u0006\u0010\u0012\u001a\u00020\u000fJ\u0010\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u0015H\u0002J\u0006\u0010\u0016\u001a\u00020\u000fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/rendly/app/util/SoundManager;", "", "()V", "MIN_SOUND_INTERVAL", "", "TAG", "", "appContext", "Landroid/content/Context;", "isInitialized", "", "lastSoundTime", "mediaPlayer", "Landroid/media/MediaPlayer;", "init", "", "context", "playMessageSound", "playNotificationSound", "playSound", "type", "Lcom/rendly/app/util/SoundManager$SoundType;", "release", "SoundType", "app_debug"})
public final class SoundManager {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "SoundManager";
    @org.jetbrains.annotations.Nullable
    private static android.media.MediaPlayer mediaPlayer;
    private static boolean isInitialized = false;
    @org.jetbrains.annotations.Nullable
    private static android.content.Context appContext;
    private static long lastSoundTime = 0L;
    private static final long MIN_SOUND_INTERVAL = 500L;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.util.SoundManager INSTANCE = null;
    
    private SoundManager() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    /**
     * Reproduce sonido de notificación
     */
    public final void playNotificationSound() {
    }
    
    /**
     * Reproduce sonido de mensaje nuevo
     */
    public final void playMessageSound() {
    }
    
    private final void playSound(com.rendly.app.util.SoundManager.SoundType type) {
    }
    
    public final void release() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rendly/app/util/SoundManager$SoundType;", "", "(Ljava/lang/String;I)V", "NOTIFICATION", "MESSAGE", "app_debug"})
    public static enum SoundType {
        /*public static final*/ NOTIFICATION /* = new NOTIFICATION() */,
        /*public static final*/ MESSAGE /* = new MESSAGE() */;
        
        SoundType() {
        }
        
        @org.jetbrains.annotations.NotNull
        public static kotlin.enums.EnumEntries<com.rendly.app.util.SoundManager.SoundType> getEntries() {
            return null;
        }
    }
}