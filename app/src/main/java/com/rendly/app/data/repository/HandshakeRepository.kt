package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.model.CreateHandshakeRequest
import com.rendly.app.data.model.HandshakeTransaction
import com.rendly.app.data.model.HandshakeEvent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.UUID

@Serializable
data class ConfirmHandshakeParams(
    @SerialName("p_handshake_id") val handshakeId: String,
    @SerialName("p_field") val field: String
)

@Serializable
data class UpdateHandshakeStatusParams(
    @SerialName("p_handshake_id") val handshakeId: String,
    @SerialName("p_status") val status: String
)

/**
 * Repositorio para gestionar transacciones Handshake con Supabase Realtime
 * Funciona igual que los mensajes del chat - en tiempo real
 */
object HandshakeRepository {
    private const val TAG = "HandshakeRepository"
    private const val TABLE_NAME = "handshake_transactions"
    
    private lateinit var supabase: SupabaseClient
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }
    
    // Estado del handshake activo en la conversación actual
    private val _activeHandshake = MutableStateFlow<HandshakeTransaction?>(null)
    val activeHandshake: StateFlow<HandshakeTransaction?> = _activeHandshake.asStateFlow()
    
    // Propuestas pendientes para el usuario actual (para mostrar modal)
    private val _pendingProposals = MutableStateFlow<List<HandshakeTransaction>>(emptyList())
    val pendingProposals: StateFlow<List<HandshakeTransaction>> = _pendingProposals.asStateFlow()
    
    // Eventos de handshake en tiempo real
    private val _handshakeEvents = MutableSharedFlow<HandshakeEvent>()
    val handshakeEvents: Flow<HandshakeEvent> = _handshakeEvents.asSharedFlow()
    
    // Canal de Realtime activo
    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var currentUserId: String? = null
    private var currentConversationId: String? = null
    private var isSubscribed = false
    
    fun initialize(client: SupabaseClient) {
        supabase = client
        Log.d(TAG, "HandshakeRepository initialized")
    }
    
    /**
     * Suscribirse a cambios de handshake para un usuario
     */
    suspend fun subscribeToHandshakes(userId: String) {
        currentUserId = userId
        Log.d(TAG, ">>> subscribeToHandshakes START for userId=$userId")
        
        try {
            // Cargar propuestas pendientes
            loadPendingProposals(userId)
            
            // Cancelar suscripción anterior si existe
            if (realtimeChannel != null) {
                try {
                    realtimeChannel?.unsubscribe()
                    supabase.realtime.removeChannel(realtimeChannel!!)
                } catch (e: Exception) {
                    Log.w(TAG, "Error removing old channel: ${e.message}")
                }
                realtimeChannel = null
                isSubscribed = false
            }
            
            // 1. Conectar al websocket de Realtime (como hace ChatRepository)
            Log.d(TAG, ">>> Step 1: Connecting to Realtime websocket...")
            val realtime = supabase.realtime
            try {
                realtime.connect()
                delay(500) // Esperar a que se establezca la conexión
            } catch (e: Exception) {
                if (!e.message.orEmpty().contains("already connected", ignoreCase = true)) {
                    Log.e(TAG, ">>> WS connection error: ${e.message}")
                }
            }
            Log.d(TAG, ">>> Step 1 DONE: Realtime websocket ready")
            
            // 2. Crear canal de Realtime
            val channelId = "handshake_$userId"
            Log.d(TAG, ">>> Step 2: Creating channel '$channelId'...")
            val channel = supabase.channel(channelId)
            
            // 3. Configurar listener de postgres changes para INSERT (nuevas propuestas)
            Log.d(TAG, ">>> Step 3: Setting up INSERT flow...")
            val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = TABLE_NAME
            }
            
            // 4. Configurar listener de postgres changes para UPDATE (cambios de estado)
            Log.d(TAG, ">>> Step 4: Setting up UPDATE flow...")
            val updateFlow = channel.postgresChangeFlow<PostgresAction.Update>(
                schema = "public"
            ) {
                table = TABLE_NAME
            }
            
            // 5. Configurar listener de postgres changes para DELETE
            val deleteFlow = channel.postgresChangeFlow<PostgresAction.Delete>(
                schema = "public"
            ) {
                table = TABLE_NAME
            }
            
            // Procesar INSERT events
            insertFlow.onEach { action ->
                Log.d(TAG, ">>> REALTIME INSERT received!")
                try {
                    val handshake = json.decodeFromString<HandshakeTransaction>(action.record.toString())
                    Log.d(TAG, ">>> INSERT parsed: id=${handshake.id} status=${handshake.status} initiator=${handshake.initiatorId} receiver=${handshake.receiverId}")
                    
                    if (handshake.initiatorId == userId || handshake.receiverId == userId) {
                        Log.d(TAG, ">>> INSERT is for us, processing...")
                        
                        // Si soy el receptor y está en PROPOSED, agregar a pendientes
                        if (handshake.receiverId == userId && handshake.status == "PROPOSED") {
                            _pendingProposals.value = _pendingProposals.value + handshake
                            Log.d(TAG, ">>> Added to pendingProposals (now ${_pendingProposals.value.size})")
                        }
                        
                        _handshakeEvents.emit(HandshakeEvent.Created(handshake))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, ">>> Error parsing INSERT: ${e.message}")
                    Log.e(TAG, ">>> Raw INSERT record: ${action.record}")
                }
            }.launchIn(scope)
            
            // Procesar UPDATE events (CRITICAL: This is what updates PROPOSED → ACCEPTED etc.)
            updateFlow.onEach { action ->
                Log.d(TAG, ">>> REALTIME UPDATE received!")
                try {
                    val handshake = json.decodeFromString<HandshakeTransaction>(action.record.toString())
                    Log.d(TAG, ">>> UPDATE parsed: id=${handshake.id} status=${handshake.status} initiator=${handshake.initiatorId} receiver=${handshake.receiverId}")
                    
                    if (handshake.initiatorId == userId || handshake.receiverId == userId) {
                        Log.d(TAG, ">>> UPDATE is for us, processing status=${handshake.status}...")
                        
                        // Actualizar lista de pendientes
                        if (handshake.status != "PROPOSED") {
                            _pendingProposals.value = _pendingProposals.value.filter { it.id != handshake.id }
                        }
                        
                        // SIEMPRE actualizar activeHandshake para que el UI refleje el cambio
                        val previousStatus = _activeHandshake.value?.status
                        _activeHandshake.value = handshake
                        Log.d(TAG, ">>> _activeHandshake UPDATED: $previousStatus → ${handshake.status}")
                        
                        // Si el handshake se canceló o rechazó, limpiar después de emitir evento
                        if (handshake.status in listOf("CANCELLED", "REJECTED")) {
                            Log.d(TAG, ">>> Clearing _activeHandshake due to status: ${handshake.status}")
                            // Delay breve para que el UI procese el cambio antes de limpiar
                            delay(100)
                            _activeHandshake.value = null
                        }
                        
                        _handshakeEvents.emit(HandshakeEvent.Updated(handshake))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, ">>> Error parsing UPDATE: ${e.message}")
                    Log.e(TAG, ">>> Raw UPDATE record: ${action.record}")
                }
            }.launchIn(scope)
            
            // Procesar DELETE events
            deleteFlow.onEach { action ->
                Log.d(TAG, ">>> REALTIME DELETE received!")
                try {
                    val oldRecord = action.oldRecord
                    val handshakeId = oldRecord["id"]?.toString()?.trim('"')
                    if (handshakeId != null) {
                        Log.d(TAG, ">>> DELETE: handshakeId=$handshakeId")
                        _pendingProposals.value = _pendingProposals.value.filter { it.id != handshakeId }
                        if (_activeHandshake.value?.id == handshakeId) {
                            _activeHandshake.value = null
                        }
                        _handshakeEvents.emit(HandshakeEvent.Deleted(handshakeId))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, ">>> Error processing DELETE: ${e.message}")
                }
            }.launchIn(scope)
            
            // Monitorear estado del canal
            channel.status.onEach { status ->
                Log.d(TAG, ">>> Channel status: $status")
            }.launchIn(scope)
            
            // 6. Guardar referencia ANTES de suscribir
            realtimeChannel = channel
            
            // 7. Suscribirse al canal (bloquear hasta confirmar suscripción)
            Log.d(TAG, ">>> Step 7: Subscribing to channel (blockUntilSubscribed)...")
            channel.subscribe(blockUntilSubscribed = true)
            isSubscribed = true
            Log.d(TAG, ">>> SUBSCRIBED SUCCESSFULLY to handshakes for user: $userId")
            
        } catch (e: Exception) {
            Log.e(TAG, ">>> ERROR subscribing to handshakes: ${e.message}")
            e.printStackTrace()
            isSubscribed = false
        }
    }
    
    /**
     * Refrescar el handshake activo desde la DB (polling fallback)
     * Llamar periódicamente desde ChatScreen como safety net
     */
    suspend fun refreshActiveHandshake(conversationId: String): Boolean {
        return try {
            val handshake = supabase.postgrest[TABLE_NAME]
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                        or {
                            eq("status", "PROPOSED")
                            eq("status", "ACCEPTED")
                            eq("status", "IN_PROGRESS")
                            eq("status", "RENEGOTIATING")
                        }
                    }
                }
                .decodeSingleOrNull<HandshakeTransaction>()
            
            val previousStatus = _activeHandshake.value?.status
            val newStatus = handshake?.status
            
            if (previousStatus != newStatus) {
                Log.d(TAG, ">>> POLL detected change: $previousStatus → $newStatus")
                _activeHandshake.value = handshake
                return true // Changed
            }
            false // No change
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing handshake: ${e.message}")
            false
        }
    }
    
    /**
     * Cargar propuestas pendientes para el usuario
     */
    private suspend fun loadPendingProposals(userId: String) {
        try {
            val proposals = supabase.postgrest[TABLE_NAME]
                .select {
                    filter {
                        eq("receiver_id", userId)
                        eq("status", "PROPOSED")
                    }
                }
                .decodeList<HandshakeTransaction>()
            
            _pendingProposals.value = proposals
            Log.d(TAG, "Loaded ${proposals.size} pending proposals")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading pending proposals: ${e.message}")
        }
    }
    
    /**
     * Crear un nuevo handshake (iniciar propuesta de compra/venta)
     */
    suspend fun createHandshake(
        conversationId: String,
        initiatorId: String,
        receiverId: String,
        productDescription: String,
        agreedPrice: Double
    ): HandshakeTransaction? {
        return try {
            // Generar secret para QR del iniciador
            val qrSecret = UUID.randomUUID().toString()
            
            val request = CreateHandshakeRequest(
                conversationId = conversationId,
                initiatorId = initiatorId,
                receiverId = receiverId,
                productDescription = productDescription,
                agreedPrice = agreedPrice,
                qrSecretInitiator = qrSecret
            )
            
            val result = supabase.postgrest[TABLE_NAME]
                .insert(request) {
                    select()
                }
                .decodeSingle<HandshakeTransaction>()
            
            Log.d(TAG, "Created handshake: ${result.id}")
            _activeHandshake.value = result
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating handshake: ${e.message}")
            null
        }
    }
    
    /**
     * Aceptar una propuesta de handshake
     */
    suspend fun acceptHandshake(handshakeId: String): Boolean {
        return try {
            val qrSecret = UUID.randomUUID().toString()
            
            supabase.postgrest[TABLE_NAME]
                .update({
                    set("status", "ACCEPTED")
                    set("accepted_at", "now()")
                    set("qr_secret_receiver", qrSecret)
                }) {
                    filter {
                        eq("id", handshakeId)
                    }
                }
            
            Log.d(TAG, "Accepted handshake: $handshakeId")
            
            // Recargar desde DB para actualizar estado local inmediatamente
            reloadHandshake(handshakeId)
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting handshake: ${e.message}")
            false
        }
    }
    
    /**
     * Rechazar una propuesta de handshake
     */
    suspend fun rejectHandshake(handshakeId: String): Boolean {
        return try {
            supabase.postgrest[TABLE_NAME]
                .update({
                    set("status", "REJECTED")
                }) {
                    filter {
                        eq("id", handshakeId)
                    }
                }
            
            Log.d(TAG, "Rejected handshake: $handshakeId")
            _pendingProposals.value = _pendingProposals.value.filter { it.id != handshakeId }
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting handshake: ${e.message}")
            false
        }
    }
    
    /**
     * Renegociar una propuesta
     */
    suspend fun renegotiateHandshake(
        handshakeId: String,
        counterPrice: Double,
        counterMessage: String
    ): Boolean {
        return try {
            supabase.postgrest[TABLE_NAME]
                .update({
                    set("status", "RENEGOTIATING")
                    set("counter_price", counterPrice)
                    set("counter_message", counterMessage)
                }) {
                    filter {
                        eq("id", handshakeId)
                    }
                }
            
            Log.d(TAG, "Renegotiating handshake: $handshakeId")
            reloadHandshake(handshakeId)
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error renegotiating handshake: ${e.message}")
            false
        }
    }
    
    /**
     * Confirmar la transacción (cada parte confirma)
     */
    suspend fun confirmTransaction(handshakeId: String, userId: String): Boolean {
        return try {
            // Obtener el handshake actual para determinar si es initiator o receiver
            val handshake = _activeHandshake.value
            
            if (handshake == null) {
                Log.e(TAG, "No active handshake found")
                return false
            }
            
            // Determinar qué campo actualizar
            val isInitiator = handshake.initiatorId == userId
            val fieldToUpdate = if (isInitiator) "initiator_confirmed" else "receiver_confirmed"
            
            // Verificar si AMBOS confirman → COMPLETED
            val otherConfirmed = if (isInitiator) handshake.receiverConfirmed else handshake.initiatorConfirmed
            val newStatus = if (otherConfirmed == true) "COMPLETED" else "IN_PROGRESS"
            
            supabase.postgrest[TABLE_NAME]
                .update({
                    set(fieldToUpdate, true)
                    set("status", newStatus)
                    if (newStatus == "COMPLETED") {
                        set("completed_at", "now()")
                    }
                }) {
                    filter {
                        eq("id", handshakeId)
                    }
                }
            
            Log.d(TAG, "Confirmed transaction: $handshakeId by $userId (isInitiator: $isInitiator, newStatus: $newStatus)")
            
            // Recargar desde DB para actualizar estado local inmediatamente
            reloadHandshake(handshakeId)
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error confirming transaction: ${e.message}")
            false
        }
    }
    
    /**
     * Cancelar un handshake
     */
    suspend fun cancelHandshake(handshakeId: String): Boolean {
        Log.d(TAG, ">>> cancelHandshake CALLED with id: $handshakeId")
        Log.d(TAG, ">>> Current _activeHandshake.value?.id = ${_activeHandshake.value?.id}")
        
        return try {
            supabase.postgrest[TABLE_NAME]
                .update({
                    set("status", "CANCELLED")
                }) {
                    filter {
                        eq("id", handshakeId)
                    }
                }
            
            Log.d(TAG, ">>> Supabase update SUCCESS for handshake: $handshakeId")
            
            if (_activeHandshake.value?.id == handshakeId) {
                Log.d(TAG, ">>> Setting _activeHandshake to NULL")
                _activeHandshake.value = null
            } else {
                Log.d(TAG, ">>> ID mismatch, not clearing _activeHandshake")
            }
            true
            
        } catch (e: Exception) {
            Log.e(TAG, ">>> ERROR cancelling handshake: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Recargar un handshake desde DB para actualizar estado local
     */
    private suspend fun reloadHandshake(handshakeId: String) {
        try {
            val updated = supabase.postgrest[TABLE_NAME]
                .select {
                    filter { eq("id", handshakeId) }
                }
                .decodeSingleOrNull<HandshakeTransaction>()
            
            if (updated != null) {
                _activeHandshake.value = updated
                Log.d(TAG, ">>> Reloaded handshake from DB: ${updated.id} status=${updated.status}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reloading handshake: ${e.message}")
        }
    }
    
    /**
     * Obtener handshake activo para una conversación
     */
    suspend fun getActiveHandshakeForConversation(conversationId: String): HandshakeTransaction? {
        currentConversationId = conversationId
        return try {
            val handshake = supabase.postgrest[TABLE_NAME]
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                        or {
                            eq("status", "PROPOSED")
                            eq("status", "ACCEPTED")
                            eq("status", "IN_PROGRESS")
                            eq("status", "RENEGOTIATING")
                        }
                    }
                }
                .decodeSingleOrNull<HandshakeTransaction>()
            
            _activeHandshake.value = handshake
            Log.d(TAG, ">>> getActiveHandshakeForConversation: ${handshake?.id} status=${handshake?.status}")
            handshake
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active handshake: ${e.message}")
            null
        }
    }
    
    /**
     * Desuscribirse de Realtime
     */
    suspend fun unsubscribe() {
        try {
            realtimeChannel?.let {
                try { it.unsubscribe() } catch (_: Exception) {}
                supabase.realtime.removeChannel(it)
            }
            realtimeChannel = null
            currentUserId = null
            currentConversationId = null
            isSubscribed = false
            _activeHandshake.value = null
            _pendingProposals.value = emptyList()
            Log.d(TAG, "Unsubscribed from handshakes")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing: ${e.message}")
        }
    }
    
    /**
     * Verificar si la suscripción está activa
     */
    fun isRealtimeActive(): Boolean = isSubscribed && realtimeChannel != null
}
