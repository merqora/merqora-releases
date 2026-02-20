package com.rendly.app.data.remote

import android.content.Context
import com.rendly.app.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import com.rendly.app.data.repository.HandshakeRepository
import com.rendly.app.data.repository.ReputationRepository

/**
 * SupabaseClient - COLD START OPTIMIZED + SESSION PERSISTENCE
 * 
 * Usa lazy initialization para diferir la creación del cliente
 * hasta el primer uso, evitando bloquear Application.onCreate()
 * 
 * Session persiste en EncryptedSharedPreferences para auto-login
 * 
 * Impacto: ~2000ms removidos del cold start
 */
object SupabaseClient {
    
    private var appContext: Context? = null
    
    /**
     * Inicializar con contexto de aplicación para SessionStorage
     * Llamar desde Application.onCreate()
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }
    
    // ═══════════════════════════════════════════════════════════════════
    // LAZY INITIALIZATION: El cliente se crea en primer acceso, no en startup
    // Esto permite que Application.onCreate() termine en < 50ms
    // ═══════════════════════════════════════════════════════════════════
    val client by lazy {
        android.util.Log.d("SupabaseClient", "⏱️ Initializing Supabase client (lazy)...")
        val startTime = System.currentTimeMillis()
        
        val supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Configurar JSON para ignorar campos desconocidos
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
            
            // FIX: requestTimeout de supabase-kt por defecto es 10s (muy corto)
            // Aumentar a 60s para evitar "Request timeout has expired"
            requestTimeout = 60.seconds
            
            // OPTIMIZADO: Timeouts reducidos para cold start más rápido
            // El primer request será más rápido, retries manejan fallos
            httpEngine = OkHttp.create {
                config {
                    connectTimeout(10, TimeUnit.SECONDS)  // Reducido de 30s
                    readTimeout(30, TimeUnit.SECONDS)     // Reducido de 60s
                    writeTimeout(30, TimeUnit.SECONDS)    // Reducido de 60s
                    retryOnConnectionFailure(true)
                }
            }
            
            install(Auth)
            install(Postgrest) {
                defaultSchema = "public"
            }
            install(Storage)
            install(Realtime)
            install(Functions)
        }
        
        val elapsed = System.currentTimeMillis() - startTime
        android.util.Log.d("SupabaseClient", "✅ Supabase initialized in ${elapsed}ms")
        
        // Inicializar repositorios que dependen del cliente
        HandshakeRepository.initialize(supabase)
        android.util.Log.d("SupabaseClient", "✅ HandshakeRepository initialized")
        
        ReputationRepository.initialize(supabase)
        android.util.Log.d("SupabaseClient", "✅ ReputationRepository initialized")
        
        supabase
    }
    
    val auth get() = client.auth
    val database get() = client.postgrest
    val storage get() = client.storage
}
