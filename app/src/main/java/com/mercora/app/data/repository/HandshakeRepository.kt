package com.mercora.app.data.repository

import android.util.Log
import com.mercora.app.data.model.CreateHandshakeRequest
import com.mercora.app.data.model.HandshakeTransaction
import com.mercora.app.data.model.HandshakeEvent
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
import java.util.concurrent.ConcurrentHashMap

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
    
    // Estado del handshake activo en la conversaciÃ³n actual
    private val _activeHandshake = MutableStateFlow<HandshakeTransaction?>(null)
    val activeHandshake: StateFlow<HandshakeTransaction?> = _activeHandshake.asStateFlow()
    
    // Propuestas pendientes para el usuario actual (para mostrar modal)
    private val _pendingProposals = MutableStateFlow<List<HandshakeTransaction>>(emptyList())
    val pendingProposals: StateFlow<List<HandshakeTransaction>> = _pendingProposals.asStateFlow()
    
    // Eventos de handshake en tiempo real
    private val _handshakeEvents = MutableSharedFlow<HandshakeEvent>()
    val handshakeEvents: Flow<HandshakeEvent> = _handshakeEvents.asSharedFlow()
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // CACHÃ‰ IN-MEMORY: Persiste handshakes activos entre entradas/salidas
    // del chat. Key = conversationId, Value = HandshakeTransaction
    // MÃ¡ximo ~1M entradas = ~200MB worst case, pero en la prÃ¡ctica serÃ¡n
    // decenas. Se limpia automÃ¡ticamente cuando un handshake se completa/cancela.
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    private val handshakeCache = ConcurrentHashMap<String, HandshakeTransaction>()
    
    /**
     * Enviar mensaje de estado del handshake al chat.
     * Se llama SOLO desde los mÃ©todos de acciÃ³n (accept, reject, confirm, cancel)
     * para que solo el usuario que ejecuta la acciÃ³n envÃ­e el mensaje (evita duplicados).
     */
    private suspend fun sendHandshakeStatusMessage(handshake: HandshakeTransaction, type: String) {
        try {
            val convId = handshake.conversationId
            val statusJson = org.json.JSONObject().apply {
                put("type", type)
                put("productDescription", handshake.productDescription)
                put("agreedPrice", handshake.agreedPrice)
                put("initiatorConfirmed", handshake.initiatorConfirmed)
                put("receiverConfirmed", handshake.receiverConfirmed)
            }
            ChatRepository.sendMessage(convId, "[HANDSHAKE_STATUS]$statusJson")
            Log.d(TAG, ">>> Sent status message: $type for conv=$convId")
            // Recargar conversaciones para que MessagesScreen refleje el cambio
            try { ChatRepository.loadConversations() } catch (_: Exception) {}
        } catch (e: Exception) {
            Log.e(TAG, ">>> Error sending status message: ${e.message}")
        }
    }
    
    /**
     * Obtener handshake cacheado para una conversaciÃ³n (instantÃ¡neo, sin red)
     */
    fun getCachedHandshake(conversationId: String): HandshakeTransaction? {
        return handshakeCache[conversationId]
    }
    
    /**
     * Guardar handshake en cachÃ© (llamar al salir del chat o cuando cambia el estado)
     */
    private fun cacheHandshake(handshake: HandshakeTransaction?) {
        val convId = handshake?.conversationId ?: return
        val status = handshake.status
        if (status in listOf("COMPLETED", "CANCELLED", "REJECTED")) {
            // Limpiar del cachÃ© si estÃ¡ en estado terminal
            handshakeCache.remove(convId)
            Log.d(TAG, ">>> Cache REMOVED for conv=$convId (status=$status)")
        } else {
            // Limitar tamaÃ±o del cachÃ©
            if (handshakeCache.size >= MAX_CACHE_SIZE && !handshakeCache.containsKey(convId)) {
                handshakeCache.keys.firstOrNull()?.let { handshakeCache.remove(it) }
            }
            handshakeCache[convId] = handshake
            Log.d(TAG, ">>> Cache SAVED for conv=$convId (status=$status)")
        }
    }
    
    /**
     * Limpiar cachÃ© de una conversaciÃ³n especÃ­fica
     */
    fun clearCacheForConversation(conversationId: String) {
        handshakeCache.remove(conversationId)
    }
    
    // Dedup: track which status messages THIS device has already sent
    // Key = "${handshakeId}_${status}"
    private val sentStatusKeys = ConcurrentHashMap.newKeySet<String>()
    private const val MAX_SENT_KEYS = 500
    private const val MAX_CACHE_SIZE = 100
    
    private fun trackSentKey(key: String) {
        sentStatusKeys.add(key)
        // Evitar crecimiento infinito: si supera el lÃ­mite, limpiar las mÃ¡s antiguas
        if (sentStatusKeys.size > MAX_SENT_KEYS) {
            val toRemove = sentStatusKeys.take(sentStatusKeys.size - MAX_SENT_KEYS / 2)
            toRemove.forEach { sentStatusKeys.remove(it) }
            Log.d(TAG, ">>> Cleaned sentStatusKeys: removed ${toRemove.size}, remaining ${sentStatusKeys.size}")
        }
    }
    
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
     * Suscribirse a cambios de handshake para un usuario.
     * IDEMPOTENTE: si ya estÃ¡ suscrito para el mismo usuario, solo recarga propuestas pendientes.
     * Esto evita gaps en la cobertura Realtime al navegar entre ChatScreen y MessagesScreen.
     */
    suspend fun subscribeToHandshakes(userId: String) {
        // Si ya estamos suscritos para este usuario, solo refrescar propuestas
        if (currentUserId == userId && isSubscribed && realtimeChannel != null) {
            Log.d(TAG, ">>> subscribeToHandshakes: Already subscribed for userId=$userId, refreshing proposals only")
            loadPendingProposals(userId)
            return
        }
        
        currentUserId = userId
        Log.d(TAG, ">>> subscribeToHandshakes START for userId=$userId")
        
        try {
            // Cargar propuestas pendientes
            loadPendingProposals(userId)
            
            // Cancelar suscripciÃ³n anterior si existe (solo si es para otro usuario)
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
                delay(500) // Esperar a que se establezca la conexiÃ³n
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
                        
                        // Guardar en cachÃ©
                        cacheHandshake(handshake)
                        
                        // Si soy el receptor y estÃ¡ en PROPOSED, agregar a pendientes
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
            
            // Procesar UPDATE events (CRITICAL: This is what updates PROPOSED â†’ ACCEPTED etc.)
            updateFlow.onEach { action ->
                Log.d(TAG, ">>> REALTIME UPDATE received!")
                try {
                    val handshake = json.decodeFromString<HandshakeTransaction>(action.record.toString())
                    Log.d(TAG, ">>> UPDATE parsed: id=${handshake.id} status=${handshake.status} initiator=${handshake.initiatorId} receiver=${handshake.receiverId}")
                    
                    if (handshake.initiatorId == userId || handshake.receiverId == userId) {
                        Log.d(TAG, ">>> UPDATE is for us, processing status=${handshake.status}...")
                        
                        // Actualizar cachÃ©
                        cacheHandshake(handshake)
                        
                        // Actualizar lista de pendientes
                        if (handshake.status != "PROPOSED") {
                            _pendingProposals.value = _pendingProposals.value.filter { it.id != handshake.id }
                        }
                        
                        // SIEMPRE actualizar activeHandshake para que el UI refleje el cambio
                        val previousStatus = _activeHandshake.value?.status
                        _activeHandshake.value = handshake
                        Log.d(TAG, ">>> _activeHandshake UPDATED: $previousStatus â†’ ${handshake.status}")
                        
                        // â•â•â• ENVIAR MENSAJE AL CHAT SI NO FUE ENVIADO POR ESTE DISPOSITIVO â•â•â•
                        // Esto cubre el caso donde admin-web u otro dispositivo cambiÃ³ el estado
                        // Funciona tanto si el usuario estÃ¡ en el chat como si estÃ¡ en MessagesScreen
                        val messageType = when (handshake.status) {
                            "ACCEPTED" -> "ACCEPTED"
                            "IN_PROGRESS" -> "CONFIRMED"
                            "COMPLETED" -> "TRANSACTION_COMPLETED"
                            "CANCELLED" -> "AGREEMENT_CANCELLED"
                            "REJECTED" -> "REJECTED"
                            else -> null
                        }
                        if (messageType != null) {
                            val dedupKey = "${handshake.id}_${messageType}"
                            if (!sentStatusKeys.contains(dedupKey)) {
                                Log.d(TAG, ">>> REALTIME: Sending missing chat message for $messageType (not sent by this device)")
                                trackSentKey(dedupKey)
                                sendHandshakeStatusMessage(handshake, messageType)
                            }
                        }
                        
                        // Si el handshake se cancelÃ³ o rechazÃ³, limpiar despuÃ©s de emitir evento
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
            
            // 7. Suscribirse al canal (bloquear hasta confirmar suscripciÃ³n)
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
     * Llamar periÃ³dicamente desde ChatScreen como safety net
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
            
            val previous = _activeHandshake.value
            // Compare full state: status, confirmations, id (covers all meaningful changes)
            val changed = previous?.id != handshake?.id ||
                          previous?.status != handshake?.status ||
                          previous?.initiatorConfirmed != handshake?.initiatorConfirmed ||
                          previous?.receiverConfirmed != handshake?.receiverConfirmed ||
                          previous?.counterPrice != handshake?.counterPrice
            
            if (changed) {
                Log.d(TAG, ">>> POLL detected change: ${previous?.status}â†’${handshake?.status} (initConf=${handshake?.initiatorConfirmed}, recvConf=${handshake?.receiverConfirmed})")
                _activeHandshake.value = handshake
                if (handshake != null) {
                    cacheHandshake(handshake)
                } else if (previous != null) {
                    handshakeCache.remove(previous.conversationId)
                }
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
            cacheHandshake(result)
            
            // Enviar mensaje de propuesta al chat para que quede registrado
            trackSentKey("${result.id}_PROPOSED")
            sendHandshakeStatusMessage(result, "PROPOSED")
            
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
            
            // Enviar mensaje de estado al chat
            trackSentKey("${handshakeId}_ACCEPTED")
            _activeHandshake.value?.let { sendHandshakeStatusMessage(it, "ACCEPTED") }
            
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
            
            // Enviar mensaje de estado al chat (fetch from DB since _activeHandshake may be null for proposals)
            try {
                val rejected = supabase.postgrest[TABLE_NAME]
                    .select { filter { eq("id", handshakeId) } }
                    .decodeSingleOrNull<HandshakeTransaction>()
                if (rejected != null) {
                    trackSentKey("${handshakeId}_REJECTED")
                    sendHandshakeStatusMessage(rejected, "REJECTED")
                }
            } catch (_: Exception) {}
            
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
     * Confirmar la transacciÃ³n (cada parte confirma)
     */
    suspend fun confirmTransaction(handshakeId: String, userId: String): Boolean {
        return try {
            // Obtener el handshake actual para determinar si es initiator o receiver
            val handshake = _activeHandshake.value
            
            if (handshake == null) {
                Log.e(TAG, "No active handshake found")
                return false
            }
            
            // Determinar quÃ© campo actualizar
            val isInitiator = handshake.initiatorId == userId
            val fieldToUpdate = if (isInitiator) "initiator_confirmed" else "receiver_confirmed"
            
            // Verificar si AMBOS confirman â†’ COMPLETED
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
            
            // Enviar mensaje de estado al chat
            val messageType = if (newStatus == "COMPLETED") "TRANSACTION_COMPLETED" else "CONFIRMED"
            trackSentKey("${handshakeId}_${messageType}")
            _activeHandshake.value?.let { sendHandshakeStatusMessage(it, messageType) }
            
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
            
            // Enviar mensaje de cancelaciÃ³n al chat ANTES de limpiar el estado
            val cancelledHandshake = _activeHandshake.value
            if (cancelledHandshake?.id == handshakeId && cancelledHandshake != null) {
                trackSentKey("${handshakeId}_AGREEMENT_CANCELLED")
                sendHandshakeStatusMessage(cancelledHandshake.copy(status = "CANCELLED"), "AGREEMENT_CANCELLED")
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
                cacheHandshake(updated)
                Log.d(TAG, ">>> Reloaded handshake from DB: ${updated.id} status=${updated.status}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reloading handshake: ${e.message}")
        }
    }
    
    /**
     * Obtener handshake activo para una conversaciÃ³n
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
            // Actualizar cachÃ©
            if (handshake != null) {
                cacheHandshake(handshake)
            }
            Log.d(TAG, ">>> getActiveHandshakeForConversation: ${handshake?.id} status=${handshake?.status}")
            handshake
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active handshake: ${e.message}")
            null
        }
    }
    
    /**
     * Obtener el handshake MÃS RECIENTE de una conversaciÃ³n, INCLUYENDO estados terminales
     * (COMPLETED, CANCELLED, REJECTED). Se usa al re-entrar al chat para reconciliar.
     */
    suspend fun getLatestHandshakeForConversation(conversationId: String): HandshakeTransaction? {
        return try {
            val handshake = supabase.postgrest[TABLE_NAME]
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                    }
                    order("updated_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(1L)
                }
                .decodeSingleOrNull<HandshakeTransaction>()
            Log.d(TAG, ">>> getLatestHandshake: ${handshake?.id} status=${handshake?.status}")
            handshake
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latest handshake: ${e.message}", e)
            null
        }
    }
    
    /**
     * Reconciliar mensajes de handshake al re-entrar al chat.
     * Busca mensajes DIRECTAMENTE desde la DB (no depende de estado en memoria)
     * y envÃ­a los que faltan. Esto cubre el caso donde admin-web u otro
     * dispositivo cambiÃ³ el estado sin enviar mensajes al chat.
     */
    suspend fun reconcileHandshakeMessages(conversationId: String) {
        try {
            val latest = getLatestHandshakeForConversation(conversationId)
            if (latest == null) {
                Log.d(TAG, ">>> RECONCILE: No handshake found for conv=$conversationId")
                return
            }
            Log.d(TAG, ">>> RECONCILE START: handshake=${latest.id} status=${latest.status}")
            
            // Mapear status actual a los tipos de mensaje que DEBERÃAN existir en el chat
            val expectedTypes = mutableListOf<String>()
            when (latest.status) {
                "COMPLETED" -> {
                    expectedTypes.add("ACCEPTED")
                    expectedTypes.add("TRANSACTION_COMPLETED")
                }
                "CANCELLED" -> {
                    expectedTypes.add("AGREEMENT_CANCELLED")
                }
                "REJECTED" -> {
                    expectedTypes.add("REJECTED")
                }
                "ACCEPTED", "IN_PROGRESS" -> {
                    expectedTypes.add("ACCEPTED")
                    if (latest.initiatorConfirmed || latest.receiverConfirmed) {
                        expectedTypes.add("CONFIRMED")
                    }
                }
            }
            
            if (expectedTypes.isEmpty()) {
                Log.d(TAG, ">>> RECONCILE: No expected messages for status=${latest.status}")
                return
            }
            
            // Buscar mensajes HANDSHAKE_STATUS directamente en la DB (no depender de memoria)
            // Esto es mÃ¡s robusto que leer ChatRepository.currentMessages que puede estar vacÃ­o
            // Buscamos los Ãºltimos 50 mensajes de la conversaciÃ³n y filtramos client-side
            val existingHandshakeMessages = try {
                com.vinzay.app.data.remote.SupabaseClient.database
                    .from("messages")
                    .select {
                        filter {
                            eq("conversation_id", conversationId)
                        }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(50)
                    }
                    .decodeList<MessageDB>()
                    .map { it.content }
                    .filter { it.startsWith("[HANDSHAKE_STATUS]") }
            } catch (e: Exception) {
                Log.e(TAG, ">>> RECONCILE: Error fetching messages from DB: ${e.message}")
                // Fallback a mensajes en memoria
                ChatRepository.currentMessages.value
                    .map { it.content }
                    .filter { it.startsWith("[HANDSHAKE_STATUS]") }
            }
            Log.d(TAG, ">>> RECONCILE: Found ${existingHandshakeMessages.size} handshake msgs in DB")
            
            var sentCount = 0
            for (messageType in expectedTypes) {
                val key = "${latest.id}_${messageType}"
                
                // Verificar si ya existe en la DB
                val alreadyExists = existingHandshakeMessages.any { content ->
                    try {
                        val jsonStr = content.removePrefix("[HANDSHAKE_STATUS]")
                        val json = org.json.JSONObject(jsonStr)
                        json.optString("type", "").contains(messageType)
                    } catch (_: Exception) { false }
                }
                
                if (!alreadyExists && !sentStatusKeys.contains(key)) {
                    Log.d(TAG, ">>> RECONCILE: SENDING missing message: $messageType")
                    trackSentKey(key)
                    sendHandshakeStatusMessage(latest, messageType)
                    sentCount++
                } else {
                    Log.d(TAG, ">>> RECONCILE: $messageType already exists (inDB=$alreadyExists, inSent=${sentStatusKeys.contains(key)})")
                }
            }
            
            Log.d(TAG, ">>> RECONCILE DONE: sent $sentCount missing messages")
            
            // Si se enviaron mensajes, recargar la lista de mensajes del chat para mostrarlos
            if (sentCount > 0) {
                delay(300) // Breve delay para que Supabase procese los inserts
                try {
                    ChatRepository.loadMessages(conversationId)
                    Log.d(TAG, ">>> RECONCILE: Messages reloaded after sending $sentCount")
                } catch (e: Exception) {
                    Log.e(TAG, ">>> RECONCILE: Error reloading messages: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reconciling handshake messages: ${e.message}", e)
        }
    }
    
    /**
     * Suspender la suscripciÃ³n al salir del chat.
     * Guarda el handshake activo en cachÃ© ANTES de limpiar el estado.
     */
    suspend fun suspendForConversation(conversationId: String) {
        // Guardar en cachÃ© antes de limpiar
        val current = _activeHandshake.value
        if (current != null && current.conversationId == conversationId) {
            cacheHandshake(current)
            Log.d(TAG, ">>> Suspended handshake for conv=$conversationId (status=${current.status})")
        }
        // Limpiar estado activo sin borrar cachÃ©
        _activeHandshake.value = null
    }
    
    suspend fun unsubscribe() {
        try {
            // Guardar handshake activo en cachÃ© antes de desuscribirse
            val current = _activeHandshake.value
            if (current != null) {
                cacheHandshake(current)
            }
            
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
     * Verificar si la suscripciÃ³n estÃ¡ activa
     */
    fun isRealtimeActive(): Boolean = isSubscribed && realtimeChannel != null
}
