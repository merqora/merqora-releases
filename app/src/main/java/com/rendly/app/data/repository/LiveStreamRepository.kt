package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Modelo de transmisión en vivo
 */
@Serializable
data class LiveStream(
    val id: String = "",
    @SerialName("broadcaster_id") val broadcasterId: String = "",
    @SerialName("broadcaster_username") val broadcasterUsername: String = "",
    @SerialName("broadcaster_avatar") val broadcasterAvatar: String? = null,
    @SerialName("broadcaster_store_name") val broadcasterStoreName: String? = null,
    val title: String = "",
    @SerialName("viewer_count") val viewerCount: Int = 0,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Repositorio para gestionar transmisiones en vivo
 */
object LiveStreamRepository {
    private const val TAG = "LiveStreamRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _activeStreams = MutableStateFlow<List<LiveStream>>(emptyList())
    val activeStreams: StateFlow<List<LiveStream>> = _activeStreams.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    private var realtimeChannel: RealtimeChannel? = null
    
    /**
     * Cargar todas las transmisiones activas
     * Como eliminamos los registros al terminar, todos los que existen están activos
     */
    suspend fun loadActiveStreams() {
        _isLoading.value = true
        _lastError.value = null
        
        try {
            // Obtener TODOS los registros (los eliminamos al terminar, así que todos son activos)
            val streams = SupabaseClient.database
                .from("live_streams")
                .select()
                .decodeList<LiveStream>()
            
            _activeStreams.value = streams
            
        } catch (e: Exception) {
            val errorMsg = "Error: ${e.javaClass.simpleName} - ${e.message ?: "Sin mensaje"}"
            _lastError.value = errorMsg
            _activeStreams.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _lastError.value = null
    }
    
    /**
     * Iniciar una nueva transmisión
     */
    suspend fun startStream(streamId: String, title: String = "En vivo"): Boolean {
        Log.d(TAG, "Iniciando nueva transmisión: $streamId")
        
        try {
            val currentUser = SupabaseClient.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.e(TAG, "No hay usuario autenticado")
                return false
            }
            
            val profile = ProfileRepository.currentProfile.value
            Log.d(TAG, "Usuario: ${currentUser.id}, Profile: ${profile?.username}")
            
            val streamData = buildJsonObject {
                put("id", streamId)
                put("broadcaster_id", currentUser.id)
                put("broadcaster_username", profile?.username ?: "usuario")
                put("broadcaster_avatar", profile?.avatarUrl)
                put("broadcaster_store_name", profile?.nombreTienda)
                put("title", title)
                put("viewer_count", 0)
                put("is_active", true)
            }
            
            Log.d(TAG, "Insertando stream en Supabase: $streamData")
            
            SupabaseClient.database
                .from("live_streams")
                .insert(streamData)
            
            Log.d(TAG, "✓ Transmisión registrada en Supabase: $streamId")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando stream: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Obtener un stream específico por ID desde Supabase
     */
    suspend fun getStreamById(streamId: String): LiveStream? {
        return try {
            Log.d(TAG, "Buscando stream: $streamId")
            val streams = SupabaseClient.database
                .from("live_streams")
                .select {
                    filter {
                        eq("id", streamId)
                    }
                }
                .decodeList<LiveStream>()
            
            val stream = streams.firstOrNull()
            if (stream != null) {
                Log.d(TAG, "✓ Stream encontrado: ${stream.broadcasterUsername}")
            } else {
                Log.w(TAG, "Stream no encontrado: $streamId")
            }
            stream
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo stream: ${e.message}", e)
            null
        }
    }
    
    /**
     * Terminar una transmisión - ELIMINA el registro de Supabase
     */
    suspend fun endStream(streamId: String) {
        Log.d(TAG, "=== TERMINANDO TRANSMISIÓN ===")
        Log.d(TAG, "Stream ID a eliminar: $streamId")
        
        if (streamId.isBlank()) {
            Log.e(TAG, "ERROR: streamId está vacío")
            return
        }
        
        try {
            // Usar delete con el filtro correcto
            val result = SupabaseClient.database
                .from("live_streams")
                .delete {
                    filter {
                        eq("id", streamId)
                    }
                }
            
            Log.d(TAG, "✓ DELETE ejecutado para stream: $streamId")
            Log.d(TAG, "Resultado: $result")
            
            // Actualizar lista local removiendo el stream
            _activeStreams.value = _activeStreams.value.filter { it.id != streamId }
            Log.d(TAG, "Lista local actualizada. Streams activos: ${_activeStreams.value.size}")
            
        } catch (e: Exception) {
            Log.e(TAG, "ERROR eliminando stream: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Incrementar contador de viewers
     */
    suspend fun incrementViewerCount(streamId: String) {
        Log.d(TAG, "=== INCREMENTANDO VIEWER COUNT ===")
        Log.d(TAG, "Stream ID: $streamId")
        
        try {
            // Primero obtener el count actual desde Supabase
            val streams = SupabaseClient.database
                .from("live_streams")
                .select {
                    filter {
                        eq("id", streamId)
                    }
                }
                .decodeList<LiveStream>()
            
            val currentCount = streams.firstOrNull()?.viewerCount ?: 0
            val newCount = currentCount + 1
            
            Log.d(TAG, "Count actual: $currentCount, nuevo: $newCount")
            
            // Actualizar con el nuevo valor
            SupabaseClient.database
                .from("live_streams")
                .update(buildJsonObject {
                    put("viewer_count", newCount)
                }) {
                    filter {
                        eq("id", streamId)
                    }
                }
            
            Log.d(TAG, "✓ viewer_count actualizado a $newCount")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementando viewers: ${e.message}", e)
        }
    }
    
    /**
     * Decrementar contador de viewers
     */
    suspend fun decrementViewerCount(streamId: String) {
        Log.d(TAG, "=== DECREMENTANDO VIEWER COUNT ===")
        Log.d(TAG, "Stream ID: $streamId")
        
        try {
            // Primero obtener el count actual desde Supabase
            val streams = SupabaseClient.database
                .from("live_streams")
                .select {
                    filter {
                        eq("id", streamId)
                    }
                }
                .decodeList<LiveStream>()
            
            val currentCount = streams.firstOrNull()?.viewerCount ?: 1
            val newCount = maxOf(0, currentCount - 1)
            
            Log.d(TAG, "Count actual: $currentCount, nuevo: $newCount")
            
            SupabaseClient.database
                .from("live_streams")
                .update(buildJsonObject {
                    put("viewer_count", newCount)
                }) {
                    filter {
                        eq("id", streamId)
                    }
                }
            
            Log.d(TAG, "✓ viewer_count decrementado a $newCount")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error decrementando viewers: ${e.message}", e)
        }
    }
    
    /**
     * Suscribirse a cambios en tiempo real de streams activos
     */
    fun subscribeToStreams() {
        scope.launch {
            try {
                val realtime = SupabaseClient.client.realtime
                try {
                    realtime.connect()
                    kotlinx.coroutines.delay(500)
                } catch (e: Exception) {
                    if (!e.message.orEmpty().contains("already connected", ignoreCase = true)) {
                        throw e
                    }
                }
                
                realtimeChannel = SupabaseClient.client.channel("live-streams-updates")
                
                // Escuchar cambios en la tabla live_streams
                realtimeChannel?.postgresChangeFlow<PostgresAction>(
                    schema = "public"
                ) {
                    table = "live_streams"
                }?.onEach { action ->
                    Log.d(TAG, "Cambio en streams: $action")
                    loadActiveStreams()
                }?.launchIn(scope)
                
                realtimeChannel?.subscribe(blockUntilSubscribed = true)
                Log.d(TAG, "✓ Suscrito a cambios de streams")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error suscribiendo a streams: ${e.message}")
            }
        }
    }
    
    /**
     * Desuscribirse de cambios
     */
    fun unsubscribeFromStreams() {
        scope.launch {
            try {
                realtimeChannel?.unsubscribe()
                realtimeChannel = null
                Log.d(TAG, "✓ Desuscrito de cambios de streams")
            } catch (e: Exception) {
                Log.e(TAG, "Error desuscribiendo: ${e.message}")
            }
        }
    }
}
