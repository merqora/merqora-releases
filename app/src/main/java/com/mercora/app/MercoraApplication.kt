package com.mercora.app

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.mercora.app.data.cache.CacheOrchestrator
import com.mercora.app.data.cache.sync.CacheSyncWorker
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.NotificationRepository
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import com.mercora.app.startup.StartupOptimizer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MercoraApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // -------------------------------------------------------------------
        // COLD START OPTIMIZATION: Todas las inicializaciones pesadas movidas
        // a StartupOptimizer para ejecutar DESPUï¿½S del primer frame
        // 
        // ANTES: ~3000ms bloqueando Main Thread
        // DESPUï¿½S: ~50ms (solo setup crï¿½tico)
        // -------------------------------------------------------------------
        
        // FASE 1: Solo inicializaciï¿½n crï¿½tica (< 50ms)
        SentryAndroid.init(this) { options ->
            options.dsn = "https://cf9d2a01688263e58b44e36419d224ea@o4511757401915392.ingest.us.sentry.io/4511757409452032"
            options.tracesSampleRate = 0.2
            options.profilesSampleRate = 0.2
        }
        StartupOptimizer.initCritical(this)
        
        // SESSION PERSISTENCE: Inicializar helpers (operaciï¿½n instantï¿½nea)
        SupabaseClient.init(this)
        com.mercora.app.data.remote.SessionPersistence.init(this)
        
        // Inicializar NotificationRepository para sonidos de notificaciï¿½n
        NotificationRepository.init(this)
        
        // FASE 2: Inicialización diferida post-first-frame
        // La inicialización pesada se maneja via StartupOptimizer desde MainActivity

        // Inicializar cache system (async - no bloquea main thread)
        initializeCacheSystem()

        // Registrar observer de conectividad para sync automático
        registerNetworkSyncTrigger()
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
                            .maxSizeBytes(1024 * 1024 * 1024) // 1 GB
                            .build()
                    }
                    .respectCacheHeaders(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .crossfade(false)
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
        android.util.Log.i("MercoraApp", "Initializing cache system...")
        
        // Initialize cache orchestrator (warms caches)
        CacheOrchestrator.getInstance(this).initialize()
        
        // Schedule periodic background sync
        CacheSyncWorker.schedulePeriodicSync(this)
        
        android.util.Log.i("MercoraApp", "Cache system initialized")
    }

    /**
     * Register network connectivity callback to trigger sync on reconnection.
     */
    private fun registerNetworkSyncTrigger() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        connectivityManager?.registerDefaultNetworkCallback(
            object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    super.onAvailable(network)
                    com.mercora.app.data.cache.sync.NetworkSyncTrigger.onNetworkAvailable(this@MercoraApplication)
                }
            }
        )
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // Cleanup cache orchestrator
        CacheOrchestrator.getInstance(this).shutdown()
    }
}
