package com.rendly.app;

import android.app.Application;
import coil.disk.DiskCache;
import coil.memory.MemoryCache;
import coil.request.CachePolicy;
import com.rendly.app.data.cache.CacheOrchestrator;
import com.rendly.app.data.cache.sync.CacheSyncWorker;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.NotificationRepository;
import com.rendly.app.data.repository.StoryRepository;
import com.rendly.app.startup.StartupOptimizer;
import dagger.hilt.android.HiltAndroidApp;

@dagger.hilt.android.HiltAndroidApp
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0007\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0002J\b\u0010\u0005\u001a\u00020\u0004H\u0016J\b\u0010\u0006\u001a\u00020\u0004H\u0016\u00a8\u0006\b"}, d2 = {"Lcom/rendly/app/MerqoraApplication;", "Landroid/app/Application;", "()V", "initializeCacheSystem", "", "onCreate", "onTerminate", "Companion", "app_debug"})
public final class MerqoraApplication extends android.app.Application {
    @kotlin.jvm.Volatile
    @org.jetbrains.annotations.Nullable
    private static volatile coil.ImageLoader imageLoader;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.MerqoraApplication.Companion Companion = null;
    
    public MerqoraApplication() {
        super();
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    /**
     * Initialize the cache-first architecture system.
     * - Warms caches from disk into memory
     * - Schedules background sync with WorkManager
     * - Sets up cache maintenance
     */
    private final void initializeCacheSystem() {
    }
    
    @java.lang.Override
    public void onTerminate() {
    }
    
    /**
     * COLD START OPTIMIZATION: ImageLoader se crea lazy cuando Coil lo necesita.
     * Movido a companion object para evitar que ImageLoaderFactory bloquee startup.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/rendly/app/MerqoraApplication$Companion;", "", "()V", "imageLoader", "Lcoil/ImageLoader;", "getImageLoader", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final coil.ImageLoader getImageLoader(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
            return null;
        }
    }
}