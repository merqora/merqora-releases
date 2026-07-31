package com.mercora.app.startup

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.Coil
import com.mercora.app.MercoraApplication
import java.util.concurrent.atomic.AtomicBoolean

/**
 * StartupOptimizer - Orquestador de inicialización diferida
 * 
 * Divide el startup en 3 fases:
 * 1. CRITICAL: Solo lo necesario para mostrar primer frame (< 100ms)
 * 2. DEFERRED: Inicializaciones que pueden esperar post-first-frame (< 500ms)  
 * 3. LAZY: Inicializaciones bajo demanda cuando se usen
 *
 * Target: Cold start < 1000ms
 */
object StartupOptimizer {
    
    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val isInitialized = AtomicBoolean(false)
    private val isDeferredComplete = AtomicBoolean(false)
    
    // Callbacks para notificar cuando startup diferido complete
    private var onDeferredComplete: (() -> Unit)? = null
    
    /**
     * FASE 1: Inicialización crítica - SOLO lo mínimo para UI
     * Ejecutar ANTES de setContent()
     * Target: < 100ms
     */
    fun initCritical(context: Context) {
        if (isInitialized.getAndSet(true)) return
        
        // COLD START: Solo configurar Coil singleton (lazy - no crea ImageLoader aún)
        // El ImageLoader real se crea cuando se use la primera imagen
        Coil.setImageLoader {
            MercoraApplication.getImageLoader(context)
        }
    }
    
    /**
     * FASE 2: Inicialización diferida - post first frame
     * Ejecutar DESPUÉS de que el primer frame se dibuje
     * Target: < 500ms en background
     */
    fun initDeferred(context: Context, onComplete: (() -> Unit)? = null) {
        if (isDeferredComplete.get()) {
            onComplete?.invoke()
            return
        }
        
        onDeferredComplete = onComplete
        
        startupScope.launch {
            // Todas las inicializaciones pesadas en paralelo
            val supabaseJob = async { initSupabaseLazy() }
            val storyJob = async { initStoryRepository(context) }
            
            // Esperar todas las inicializaciones
            supabaseJob.await()
            storyJob.await()
            
            isDeferredComplete.set(true)
            
            withContext(Dispatchers.Main) {
                onDeferredComplete?.invoke()
            }
        }
    }
    
    /**
     * Inicializar Supabase de forma lazy en background thread
     */
    private suspend fun initSupabaseLazy() = withContext(Dispatchers.IO) {
        try {
            // Forzar inicialización del cliente Supabase
            com.mercora.app.data.remote.SupabaseClient.client
            android.util.Log.d("StartupOptimizer", "? Supabase initialized")
        } catch (e: Exception) {
            android.util.Log.e("StartupOptimizer", "Supabase init error: ${e.message}")
        }
    }
    
    /**
     * Inicializar StoryRepository para persistencia
     */
    private suspend fun initStoryRepository(context: Context) = withContext(Dispatchers.IO) {
        try {
            com.mercora.app.data.repository.StoryRepository.initialize(context)
            android.util.Log.d("StartupOptimizer", "? StoryRepository initialized")
        } catch (e: Exception) {
            android.util.Log.e("StartupOptimizer", "StoryRepository error: ${e.message}")
        }
    }
    
    /**
     * Check if deferred initialization is complete
     */
    fun isDeferredInitComplete(): Boolean = isDeferredComplete.get()
    
    /**
     * FASE 3: Inicializaciones lazy - solo cuando se necesiten
     * Cada feature inicializa sus dependencias bajo demanda
     */
    object LazyInit {
        
        private val chatInitialized = AtomicBoolean(false)
        private val notificationsInitialized = AtomicBoolean(false)
        
        /**
         * Inicializar ChatRepository solo cuando se abra el chat
         */
        fun initChatIfNeeded(context: Context) {
            if (chatInitialized.getAndSet(true)) return
            
            startupScope.launch(Dispatchers.IO) {
                com.mercora.app.data.repository.ChatRepository.init(context)
                com.mercora.app.data.repository.CallRepository.initialize(context)
                android.util.Log.d("StartupOptimizer", "? ChatRepository + CallRepository lazy initialized")
            }
        }
        
        /**
         * Inicializar NotificationRepository solo cuando se necesite
         */
        suspend fun initNotificationsIfNeeded() {
            if (notificationsInitialized.getAndSet(true)) return
            
            withContext(Dispatchers.IO) {
                com.mercora.app.data.repository.NotificationRepository.loadNotifications()
                com.mercora.app.data.repository.NotificationRepository.subscribeToRealtime()
                android.util.Log.d("StartupOptimizer", "? Notifications lazy initialized")
            }
        }
    }
}
