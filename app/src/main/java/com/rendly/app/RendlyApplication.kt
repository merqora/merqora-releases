package com.rendly.app

import android.app.Application
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.rendly.app.data.cache.CacheOrchestrator
import com.rendly.app.data.cache.sync.CacheSyncWorker
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.NotificationRepository
import com.rendly.app.data.repository.StoryRepository
import com.rendly.app.startup.StartupOptimizer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MerqoraApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // ═══════════════════════════════════════════════════════════════════
        // COLD START OPTIMIZATION: Todas las inicializaciones pesadas movidas
        // a StartupOptimizer para ejecutar DESPUÉS del primer frame
        // 
        // ANTES: ~3000ms bloqueando Main Thread
        // DESPUÉS: ~50ms (solo setup crítico)
        // ═══════════════════════════════════════════════════════════════════
        
        // FASE 1: Solo inicialización crítica (< 50ms)
        StartupOptimizer.initCritical(this)
        
        // SESSION PERSISTENCE: Inicializar helpers (operación instantánea)
        SupabaseClient.init(this)
        com.rendly.app.data.remote.SessionPersistence.init(this)
        
        // Inicializar NotificationRepository para sonidos de notificación
        NotificationRepository.init(this)
        
        // FASE 2: Diferir todo lo pesado post-first-frame
        // Se ejecuta desde MainActivity después de setContent()
        
        // ❌ REMOVIDO - Bloqueaba Main Thread ~2000ms
        // SupabaseClient.client
        
        // ❌ REMOVIDO - Bloqueaba Main Thread ~200ms  
        // StoryRepository.initialize(this)
        
        // ❌ REMOVIDO - Bloqueaba Main Thread ~500ms
        // System.loadLibrary("Merqora-native")
    }
    
    /**
     * COLD START OPTIMIZATION: ImageLoader se crea lazy cuando Coil lo necesita.
     * Movido a companion object para evitar que ImageLoaderFactory bloquee startup.
     */
    companion object {
        @Volatile
        private var imageLoader: ImageLoader? = null
        
        fun getImageLoader(context: android.content.Context): ImageLoader {
            return imageLoader ?: synchronized(this) {
                imageLoader ?: ImageLoader.Builder(context.applicationContext)
                    .memoryCache {
                        MemoryCache.Builder(context.applicationContext)
                            .maxSizePercent(0.20) // 20% de RAM (reducido para startup)
                            .build()
                    }
                    .diskCache {
                        DiskCache.Builder()
                            .directory(context.cacheDir.resolve("image_cache"))
                            .maxSizeBytes(256 * 1024 * 1024) // 256 MB (reducido)
                            .build()
                    }
                    .respectCacheHeaders(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(150) // Reducido de 300ms
                    .build()
                    .also { imageLoader = it }
            }
        }
    }
    
    /**
     * Initialize the cache-first architecture system.
     * - Warms caches from disk into memory
     * - Schedules background sync with WorkManager
     * - Sets up cache maintenance
     */
    private fun initializeCacheSystem() {
        android.util.Log.i("MerqoraApp", "Initializing cache system...")
        
        // Initialize cache orchestrator (warms caches)
        CacheOrchestrator.getInstance(this).initialize()
        
        // Schedule periodic background sync
        CacheSyncWorker.schedulePeriodicSync(this)
        
        android.util.Log.i("MerqoraApp", "Cache system initialized")
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // Cleanup cache orchestrator
        CacheOrchestrator.getInstance(this).shutdown()
    }
}
