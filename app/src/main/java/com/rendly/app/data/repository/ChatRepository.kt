package com.rendly.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.ImageKitService
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.service.ChatNotificationService
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.presenceChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import com.rendly.app.data.cache.BadgeCountCache

@Serializable
data class ConversationDB(
    val id: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("last_message") val lastMessage: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null
)

@Serializable
data class ConversationParticipantDB(
    val id: String = "",
    @SerialName("conversation_id") val conversationId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("unread_count") val unreadCount: Int = 0
)

@Serializable
data class MessageDB(
    val id: String = "",
    @SerialName("conversation_id") val conversationId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    val content: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("client_temp_id") val clientTempId: String? = null,
    val status: String = "sent",
    @SerialName("delivered_at") val deliveredAt: String? = null,
    @SerialName("read_at") val readAt: String? = null,
    val reactions: String? = null // JSON: {"emoji": ["user_id1", "user_id2"]}
)

data class Conversation(
    val id: String,
    val otherUser: Usuario,
    val lastMessage: String?,
    val lastMessageAt: String?,
    val unreadCount: Int,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val labels: List<ChatLabel> = emptyList()
)

@Serializable
data class ChatLabelDB(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val name: String = "",
    val color: String = "#7C3AED",
    val icon: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class ChatLabelAssignmentDB(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("conversation_id") val conversationId: String = "",
    @SerialName("label_id") val labelId: String = "",
    @SerialName("assigned_at") val assignedAt: String? = null
)

@Serializable
data class PinnedChatDB(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("conversation_id") val conversationId: String = "",
    @SerialName("pinned_at") val pinnedAt: String? = null
)

data class ChatLabel(
    val id: String,
    val name: String,
    val color: String,
    val icon: String? = null,
    val sortOrder: Int = 0
)

// Estados de mensaje para los ticks
enum class MessageStatus {
    SENDING,   // Reloj o spinner (aún no confirmado)
    SENT,      // 1 tick gris - enviado al servidor
    DELIVERED, // 2 ticks grises - entregado al dispositivo
    READ       // 2 ticks azules - leído por el usuario
}

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val createdAt: String,
    val isRead: Boolean,
    val isFromMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
    val reactions: Map<String, List<String>> = emptyMap() // emoji -> list of user ids
)

@Serializable
data class BlockedUserDB(
    val id: String = "",
    @SerialName("blocker_id") val blockerId: String = "",
    @SerialName("blocked_id") val blockedId: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    val reason: String? = null
)

data class BlockedUserInfo(
    val id: String,
    val username: String,
    val avatarUrl: String?,
    val blockedAt: String,
    val reason: String? = null
)

object ChatRepository {
    private val TAG = "ChatRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Contexto para notificaciones
    private var appContext: Context? = null
    private var currentOtherUserName: String? = null
    
    fun init(context: Context) {
        appContext = context.applicationContext
        ChatNotificationService.createNotificationChannel(context)
        // Cargar conteo cacheado inmediatamente para mostrar badge sin delay
        val cachedCount = BadgeCountCache.getMessageCount()
        _totalUnreadCount.value = cachedCount
        Log.d(TAG, "✅ Init with cached message count: $cachedCount")
    }
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _currentMessages = MutableStateFlow<List<Message>>(emptyList())
    val currentMessages: StateFlow<List<Message>> = _currentMessages.asStateFlow()
    
    // Total de mensajes no leídos (suma de todos los chats)
    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()
    
    // ═══════════════════════════════════════════════════════════════
    // CLEARED_AT - Persistir timestamps de chats vaciados
    // ═══════════════════════════════════════════════════════════════
    private const val PREFS_NAME = "rendly_chat_prefs"
    private const val KEY_PREFIX_CLEARED = "cleared_at_"
    
    private fun getClearedAt(conversationId: String): String? {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs?.getString("$KEY_PREFIX_CLEARED$conversationId", null)
    }
    
    private fun setClearedAt(conversationId: String, timestamp: String) {
        val prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs?.edit()?.putString("$KEY_PREFIX_CLEARED$conversationId", timestamp)?.apply()
    }
    
    private fun filterByClearedAt(messages: List<Message>, conversationId: String): List<Message> {
        val clearedAt = getClearedAt(conversationId) ?: return messages
        return messages.filter { it.createdAt > clearedAt }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CACHE DE MENSAJES EN MEMORIA
    // ═══════════════════════════════════════════════════════════════
    private val messagesCache = mutableMapOf<String, List<Message>>()
    private val MAX_CACHED_CONVERSATIONS = 20 // Límite de conversaciones en cache
    
    /**
     * Obtiene mensajes del cache si existen
     */
    fun getCachedMessages(conversationId: String): List<Message>? {
        return messagesCache[conversationId]
    }
    
    /**
     * Guarda mensajes en cache
     */
    private fun cacheMessages(conversationId: String, messages: List<Message>) {
        // Limitar tamaño del cache
        if (messagesCache.size >= MAX_CACHED_CONVERSATIONS && !messagesCache.containsKey(conversationId)) {
            // Eliminar la conversación más antigua
            messagesCache.keys.firstOrNull()?.let { messagesCache.remove(it) }
        }
        messagesCache[conversationId] = messages
    }
    
    /**
     * Actualiza cache con nuevo mensaje
     */
    private fun updateCacheWithNewMessage(conversationId: String, message: Message) {
        val cached = messagesCache[conversationId]?.toMutableList() ?: return
        if (cached.none { it.id == message.id }) {
            cached.add(message)
            messagesCache[conversationId] = cached
        }
    }
    
    /**
     * Indica si la conversación tiene cache disponible
     */
    fun hasCachedMessages(conversationId: String): Boolean {
        return messagesCache.containsKey(conversationId) && messagesCache[conversationId]?.isNotEmpty() == true
    }
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Estados para typing y online
    private val _isOtherUserTyping = MutableStateFlow(false)
    val isOtherUserTyping: StateFlow<Boolean> = _isOtherUserTyping.asStateFlow()
    
    private val _isOtherUserOnline = MutableStateFlow(false)
    val isOtherUserOnline: StateFlow<Boolean> = _isOtherUserOnline.asStateFlow()
    
    private var currentConversationId: String? = null
    private var currentOtherUserId: String? = null
    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var presenceChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    
    // Control de typing para evitar spam
    private var lastTypingBroadcast = 0L
    private val TYPING_THROTTLE_MS = 500L // Reducido para respuesta más rápida
    
    // Enviar estado de typing via Broadcast
    fun setTyping(conversationId: String, isTyping: Boolean) {
        val now = System.currentTimeMillis()
        if (now - lastTypingBroadcast < TYPING_THROTTLE_MS && isTyping) return
        lastTypingBroadcast = now
        
        scope.launch {
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                realtimeChannel?.broadcast(
                    event = "typing",
                    message = buildJsonObject {
                        put("user_id", currentUserId)
                        put("is_typing", isTyping)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error broadcasting typing: ${e.message}")
            }
        }
    }
    
    // Simular actualización de estado online del otro usuario
    fun setOtherUserOnline(isOnline: Boolean) {
        _isOtherUserOnline.value = isOnline
    }
    
    fun setOtherUserTyping(isTyping: Boolean) {
        _isOtherUserTyping.value = isTyping
    }
    
    // Buscar usuarios por username
    suspend fun searchUsers(query: String): List<Usuario> {
        if (query.isBlank()) return emptyList()
        
        return try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return emptyList()
            
            SupabaseClient.database
                .from("usuarios")
                .select {
                    filter { 
                        ilike("username", "%$query%")
                        neq("user_id", currentUserId)
                    }
                }
                .decodeList<Usuario>()
                .take(10)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users: ${e.message}")
            emptyList()
        }
    }
    
    // Cargar conversaciones del usuario actual
    suspend fun loadConversations() {
        _isLoading.value = true
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return
            
            // Obtener participaciones del usuario
            val myParticipations = SupabaseClient.database
                .from("conversation_participants")
                .select { filter { eq("user_id", currentUserId) } }
                .decodeList<ConversationParticipantDB>()
            
            val conversationIds = myParticipations.map { it.conversationId }
            if (conversationIds.isEmpty()) {
                _conversations.value = emptyList()
                return
            }
            
            // Obtener conversaciones
            val conversationsDB = SupabaseClient.database
                .from("conversations")
                .select { filter { isIn("id", conversationIds) } }
                .decodeList<ConversationDB>()
            
            // Obtener todos los participantes
            val allParticipants = SupabaseClient.database
                .from("conversation_participants")
                .select { filter { isIn("conversation_id", conversationIds) } }
                .decodeList<ConversationParticipantDB>()
            
            // Obtener usuarios únicos (excluyendo al usuario actual)
            val otherUserIds = allParticipants
                .filter { it.userId != currentUserId }
                .map { it.userId }
                .distinct()
            
            val users = if (otherUserIds.isNotEmpty()) {
                SupabaseClient.database
                    .from("usuarios")
                    .select { filter { isIn("user_id", otherUserIds) } }
                    .decodeList<Usuario>()
            } else emptyList()
            
            val usersMap = users.associateBy { it.userId }
            val participationsMap = myParticipations.associateBy { it.conversationId }
            
            // Cargar chats fijados y etiquetas en paralelo
            val pinnedChats = try {
                SupabaseClient.database
                    .from("pinned_chats")
                    .select { filter { eq("user_id", currentUserId) } }
                    .decodeList<PinnedChatDB>()
            } catch (e: Exception) { emptyList() }
            val pinnedIds = pinnedChats.map { it.conversationId }.toSet()
            
            val allAssignments = try {
                SupabaseClient.database
                    .from("chat_label_assignments")
                    .select { filter { eq("user_id", currentUserId) } }
                    .decodeList<ChatLabelAssignmentDB>()
            } catch (e: Exception) { emptyList() }
            val assignmentsByConv = allAssignments.groupBy { it.conversationId }
            
            val mutedChats = try {
                SupabaseClient.database
                    .from("muted_chats")
                    .select { filter { eq("user_id", currentUserId) } }
                    .decodeList<kotlinx.serialization.json.JsonObject>()
                    .mapNotNull { it["conversation_id"]?.jsonPrimitive?.content }
                    .toSet()
            } catch (e: Exception) { emptySet() }
            
            // Cargar etiquetas del usuario si aún no se cargaron
            if (_userLabels.value.isEmpty()) {
                try {
                    val labels = SupabaseClient.database
                        .from("chat_labels")
                        .select { filter { eq("user_id", currentUserId) }; order("sort_order", Order.ASCENDING) }
                        .decodeList<ChatLabelDB>()
                        .map { ChatLabel(it.id, it.name, it.color, it.icon, it.sortOrder) }
                    _userLabels.value = labels
                } catch (e: Exception) { /* ignorar */ }
            }
            val labelsMap = _userLabels.value.associateBy { it.id }
            
            _conversations.value = conversationsDB.mapNotNull { conv ->
                val otherParticipant = allParticipants.find { 
                    it.conversationId == conv.id && it.userId != currentUserId 
                }
                val otherUser = otherParticipant?.let { usersMap[it.userId] }
                val myParticipation = participationsMap[conv.id]
                
                // Resolver etiquetas de esta conversación
                val convLabels = assignmentsByConv[conv.id]
                    ?.mapNotNull { labelsMap[it.labelId] }
                    ?: emptyList()
                
                if (otherUser != null) {
                    Conversation(
                        id = conv.id,
                        otherUser = otherUser,
                        lastMessage = conv.lastMessage,
                        lastMessageAt = conv.lastMessageAt,
                        unreadCount = myParticipation?.unreadCount ?: 0,
                        isMuted = conv.id in mutedChats,
                        isPinned = conv.id in pinnedIds,
                        labels = convLabels
                    )
                } else null
            }.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.lastMessageAt })
            
            // Actualizar contador total de mensajes no leídos
            _totalUnreadCount.value = _conversations.value.sumOf { it.unreadCount }
            BadgeCountCache.setMessageCount(_totalUnreadCount.value)
            
            Log.d(TAG, "Loaded ${_conversations.value.size} conversations, total unread: ${_totalUnreadCount.value}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversations: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    // Obtener o crear conversación con un usuario
    suspend fun getOrCreateConversation(otherUserId: String): String? {
        _lastError.value = null
        
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
            if (currentUserId == null) {
                _lastError.value = "GET/CREATE CONV: No hay usuario autenticado"
                return null
            }
            
            Log.d(TAG, "=== BUSCANDO/CREANDO CONVERSACIÓN ===")
            Log.d(TAG, "CurrentUser: $currentUserId, OtherUser: $otherUserId")
            
            // Buscar conversación existente
            val myParticipations = SupabaseClient.database
                .from("conversation_participants")
                .select { filter { eq("user_id", currentUserId) } }
                .decodeList<ConversationParticipantDB>()
            
            Log.d(TAG, "Mis participaciones: ${myParticipations.size}")
            
            for (participation in myParticipations) {
                val otherParticipant = SupabaseClient.database
                    .from("conversation_participants")
                    .select { 
                        filter { 
                            eq("conversation_id", participation.conversationId)
                            eq("user_id", otherUserId)
                        } 
                    }
                    .decodeList<ConversationParticipantDB>()
                
                if (otherParticipant.isNotEmpty()) {
                    Log.d(TAG, "Conversación existente encontrada: ${participation.conversationId}")
                    return participation.conversationId
                }
            }
            
            // Crear nueva conversación usando el patrón de StoryRepository
            Log.d(TAG, "Creando nueva conversación...")
            
            // 1. Insertar conversación y obtener el ID con select()
            val convJson = buildJsonObject {
                put("last_message", JsonNull)
                put("last_message_at", JsonNull)
            }
            
            val conversationResult = SupabaseClient.database
                .from("conversations")
                .insert(convJson) {
                    select()
                }
                .decodeSingleOrNull<ConversationDB>()
            
            if (conversationResult == null) {
                _lastError.value = "CREATE CONV: No se pudo crear la conversación"
                return null
            }
            
            val conversationId = conversationResult.id
            Log.d(TAG, "Conversación creada: $conversationId")
            
            // 2. Agregar participantes usando buildJsonObject
            val participant1 = buildJsonObject {
                put("conversation_id", conversationId)
                put("user_id", currentUserId)
                put("unread_count", 0)
            }
            val participant2 = buildJsonObject {
                put("conversation_id", conversationId)
                put("user_id", otherUserId)
                put("unread_count", 0)
            }
            
            val insertParticipantsResult = runCatching {
                SupabaseClient.database
                    .from("conversation_participants")
                    .insert(listOf(participant1, participant2))
            }
            
            if (insertParticipantsResult.isFailure) {
                _lastError.value = "INSERT PARTICIPANTS: ${insertParticipantsResult.exceptionOrNull()?.message}"
                return null
            }
            
            Log.d(TAG, "✓ Participantes agregados exitosamente")
            Log.d(TAG, "=== CONVERSACIÓN CREADA: $conversationId ===")
            
            return conversationId
            
        } catch (e: Exception) {
            _lastError.value = "EXCEPTION: ${e.message}"
            e.printStackTrace()
            return null
        }
    }
    
    private var hasMoreMessages = true
    private val INITIAL_MESSAGE_LIMIT = 15 // Mensajes iniciales (los que caben en pantalla + 2-3)
    private val LOAD_MORE_LIMIT = 12 // Mensajes adicionales al hacer scroll arriba
    private var oldestLoadedMessageDate: String? = null // Cursor para paginación
    
    // Estado para indicar si hay más mensajes por cargar
    private val _hasMoreMessages = MutableStateFlow(true)
    val hasMoreMessagesFlow: StateFlow<Boolean> = _hasMoreMessages.asStateFlow()
    
    // Estado para indicar si está cargando más mensajes
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    // Cargar mensajes INICIALES de una conversación (últimos N mensajes)
    suspend fun loadMessages(conversationId: String) {
        Log.d(TAG, "╔════════════════════════════════════════════╗")
        Log.d(TAG, "║       LOAD MESSAGES INICIADO               ║")
        Log.d(TAG, "╚════════════════════════════════════════════╝")
        Log.d(TAG, "ConversationId: $conversationId")
        
        try {
            currentConversationId = conversationId
            oldestLoadedMessageDate = null // Reset cursor
            _hasMoreMessages.value = true
            
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
            if (currentUserId == null) {
                Log.e(TAG, "ERROR: No hay usuario autenticado en loadMessages")
                _lastError.value = "LOAD: No hay usuario autenticado"
                return
            }
            
            Log.d(TAG, "CurrentUserId: $currentUserId")
            
            // Cargar ÚLTIMOS N mensajes (ORDER BY DESC + LIMIT, luego invertir)
            Log.d(TAG, "Consultando últimos $INITIAL_MESSAGE_LIMIT mensajes...")
            
            val clearedAt = getClearedAt(conversationId)
            
            val messagesDB = try {
                SupabaseClient.database
                    .from("messages")
                    .select {
                        filter { 
                            eq("conversation_id", conversationId)
                            if (clearedAt != null) {
                                gt("created_at", clearedAt)
                            }
                        }
                        order("created_at", Order.DESCENDING)
                        limit(INITIAL_MESSAGE_LIMIT.toLong())
                    }
                    .decodeList<MessageDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error en consulta de mensajes: ${e.message}", e)
                _lastError.value = "QUERY ERROR: ${e.message}"
                emptyList()
            }
            
            Log.d(TAG, "Mensajes obtenidos de DB: ${messagesDB.size}")
            
            // Si obtenemos menos del límite, no hay más mensajes
            _hasMoreMessages.value = messagesDB.size >= INITIAL_MESSAGE_LIMIT
            
            // Invertir para orden cronológico (más antiguos primero)
            val sortedMessages = messagesDB.reversed()
            
            // Guardar fecha del mensaje más antiguo para paginación
            if (sortedMessages.isNotEmpty()) {
                oldestLoadedMessageDate = sortedMessages.first().createdAt
            }
            
            val newMessages = sortedMessages.map { msg ->
                val isFromMe = msg.senderId == currentUserId
                Message(
                    id = msg.id,
                    conversationId = msg.conversationId,
                    senderId = msg.senderId,
                    content = msg.content,
                    createdAt = msg.createdAt ?: "",
                    isRead = msg.isRead,
                    isFromMe = isFromMe,
                    status = when {
                        !isFromMe -> MessageStatus.READ
                        msg.status == "read" -> MessageStatus.READ
                        msg.status == "delivered" -> MessageStatus.DELIVERED
                        else -> MessageStatus.SENT
                    }
                )
            }
            
            _currentMessages.value = filterByClearedAt(newMessages, conversationId)
            
            // Guardar en cache
            cacheMessages(conversationId, _currentMessages.value)
            Log.d(TAG, "✓ Mensajes cargados y cacheados: ${newMessages.size}, hasMore: ${_hasMoreMessages.value}")
            
            // Marcar mensajes como leídos
            markMessagesAsRead(conversationId)
            
        } catch (e: Exception) {
            Log.e(TAG, "ERROR en loadMessages: ${e.message}", e)
            _lastError.value = "LOAD ERROR: ${e.message}"
        }
    }
    
    // Cargar más mensajes antiguos (al hacer scroll hacia arriba)
    suspend fun loadMoreMessages(): Boolean {
        val convId = currentConversationId ?: return false
        val oldestDate = oldestLoadedMessageDate ?: return false
        
        if (_isLoadingMore.value || !_hasMoreMessages.value) return false
        
        _isLoadingMore.value = true
        Log.d(TAG, "Cargando más mensajes anteriores a: $oldestDate")
        
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return false
            
            // Cargar mensajes ANTERIORES al más antiguo cargado (respetando cleared_at)
            val clearedAt = getClearedAt(convId)
            val olderMessagesDB = SupabaseClient.database
                .from("messages")
                .select {
                    filter { 
                        eq("conversation_id", convId)
                        lt("created_at", oldestDate) // Mensajes anteriores
                        if (clearedAt != null) {
                            gt("created_at", clearedAt)
                        }
                    }
                    order("created_at", Order.DESCENDING)
                    limit(LOAD_MORE_LIMIT.toLong())
                }
                .decodeList<MessageDB>()
            
            Log.d(TAG, "Mensajes antiguos obtenidos: ${olderMessagesDB.size}")
            
            if (olderMessagesDB.isEmpty()) {
                _hasMoreMessages.value = false
                _isLoadingMore.value = false
                return false
            }
            
            // Si obtenemos menos del límite, no hay más
            _hasMoreMessages.value = olderMessagesDB.size >= LOAD_MORE_LIMIT
            
            // Invertir para orden cronológico
            val sortedOlder = olderMessagesDB.reversed()
            
            // Actualizar cursor
            oldestLoadedMessageDate = sortedOlder.first().createdAt
            
            val olderMessages = sortedOlder.map { msg ->
                val isFromMe = msg.senderId == currentUserId
                Message(
                    id = msg.id,
                    conversationId = msg.conversationId,
                    senderId = msg.senderId,
                    content = msg.content,
                    createdAt = msg.createdAt ?: "",
                    isRead = msg.isRead,
                    isFromMe = isFromMe,
                    status = when {
                        !isFromMe -> MessageStatus.READ
                        msg.status == "read" -> MessageStatus.READ
                        msg.status == "delivered" -> MessageStatus.DELIVERED
                        else -> MessageStatus.SENT
                    }
                )
            }
            
            // PREPEND: agregar al inicio de la lista
            _currentMessages.value = olderMessages + _currentMessages.value
            
            // Actualizar cache
            currentConversationId?.let { cacheMessages(it, _currentMessages.value) }
            Log.d(TAG, "✓ Total mensajes ahora: ${_currentMessages.value.size}")
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando más mensajes: ${e.message}", e)
            return false
        } finally {
            _isLoadingMore.value = false
        }
    }
    
    /** Cargar mensajes hasta encontrar un mensaje específico por ID (para scroll desde búsqueda) */
    suspend fun loadMessagesUntilFound(messageId: String): Int {
        currentConversationId ?: return -1
        
        // Primero buscar en la lista actual
        val existingIndex = _currentMessages.value.indexOfFirst { it.id == messageId }
        if (existingIndex >= 0) return existingIndex
        
        // Cargar bloques de mensajes antiguos hasta encontrarlo (máximo 5 intentos)
        repeat(5) {
            if (!_hasMoreMessages.value) return -1
            val loaded = loadMoreMessages()
            if (!loaded) return -1
            val idx = _currentMessages.value.indexOfFirst { it.id == messageId }
            if (idx >= 0) return idx
        }
        return -1
    }
    
    // Último error para mostrar en UI
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    fun clearError() {
        _lastError.value = null
    }
    
    // Enviar mensaje
    suspend fun sendMessage(conversationId: String, content: String): Boolean {
        Log.d(TAG, "╔════════════════════════════════════════════╗")
        Log.d(TAG, "║       SEND MESSAGE INICIADO                ║")
        Log.d(TAG, "╚════════════════════════════════════════════╝")
        
        _lastError.value = null
        
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
            if (currentUserId == null) {
                Log.e(TAG, "ERROR: No hay usuario autenticado")
                _lastError.value = "No hay usuario autenticado"
                return false
            }
            
            Log.d(TAG, "=== ENVIANDO MENSAJE ===")
            Log.d(TAG, "ConversationId: $conversationId")
            Log.d(TAG, "Content: $content")
            Log.d(TAG, "SenderId: $currentUserId")
        
        // Formato de fecha compatible con PostgreSQL
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val now = isoFormat.format(java.util.Date())
        
        // Generar client_temp_id para sincronizar mensaje optimista con realtime
        val clientTempId = java.util.UUID.randomUUID().toString()
        
        // Agregar mensaje localmente primero (optimistic update)
        val optimisticMessage = Message(
            id = clientTempId, // Usar clientTempId como ID temporal
            conversationId = conversationId,
            senderId = currentUserId,
            content = content,
            createdAt = now,
            isRead = false,
            isFromMe = true
        )
        _currentMessages.value = _currentMessages.value + optimisticMessage
        Log.d(TAG, "Mensaje optimista agregado con clientTempId: $clientTempId")
        
        // 1. Insertar mensaje en Supabase CON client_temp_id para sincronización
        val messageJson = buildJsonObject {
            put("conversation_id", conversationId)
            put("sender_id", currentUserId)
            put("content", content)
            put("client_temp_id", clientTempId)
        }
        
        Log.d(TAG, "Insertando mensaje en Supabase...")
        val insertMessageResult = runCatching {
            SupabaseClient.database
                .from("messages")
                .insert(messageJson)
        }
        
        if (insertMessageResult.isFailure) {
            val errorMsg = insertMessageResult.exceptionOrNull()?.message ?: "Error desconocido"
            Log.e(TAG, "═══════════════════════════════════════════")
            Log.e(TAG, "ERROR insertando mensaje en Supabase")
            Log.e(TAG, "Error: $errorMsg")
            Log.e(TAG, "═══════════════════════════════════════════")
            _lastError.value = "INSERT MSG: $errorMsg"
            // Remover mensaje optimista si falla
            _currentMessages.value = _currentMessages.value.filter { it.id != clientTempId }
            return false
        }
        
        Log.d(TAG, "✓ Mensaje insertado en Supabase")
        
        // 2. Actualizar conversación con último mensaje
        Log.d(TAG, "Actualizando conversación con último mensaje...")
        val updateConvJson = buildJsonObject {
            put("last_message", content)
            put("last_message_at", now)
        }
        val updateConvResult = runCatching {
            SupabaseClient.database
                .from("conversations")
                .update(updateConvJson) {
                    filter { eq("id", conversationId) }
                }
        }
        
        if (updateConvResult.isFailure) {
            Log.e(TAG, "Error actualizando conversación: ${updateConvResult.exceptionOrNull()?.message}")
        } else {
            Log.d(TAG, "✓ Conversación actualizada")
        }
        
        // 3. Incrementar contador de no leídos para el otro usuario
        try {
            val participants = SupabaseClient.database
                .from("conversation_participants")
                .select { filter { eq("conversation_id", conversationId) } }
                .decodeList<ConversationParticipantDB>()
            
            val otherParticipant = participants.find { it.userId != currentUserId }
            if (otherParticipant != null) {
                Log.d(TAG, "Incrementando unread_count para: ${otherParticipant.userId}")
                val unreadJson = buildJsonObject {
                    put("unread_count", otherParticipant.unreadCount + 1)
                }
                SupabaseClient.database
                    .from("conversation_participants")
                    .update(unreadJson) {
                        filter { eq("id", otherParticipant.id) }
                    }
                
                // Crear notificación para el otro usuario
                val senderUsername = ProfileRepository.currentProfile.value?.username ?: "Usuario"
                val messagePreview = if (content.length > 50) content.take(50) + "..." else content
                
                NotificationRepository.createMessageNotification(
                    recipientId = otherParticipant.userId,
                    senderUsername = senderUsername,
                    messagePreview = messagePreview
                )
                
                // Enviar FCM push para que llegue al otro teléfono (phone-to-phone)
                NotificationRepository.sendFCMPush(
                    recipientId = otherParticipant.userId,
                    title = senderUsername,
                    body = messagePreview,
                    data = mapOf(
                        "type" to "message",
                        "sender_id" to currentUserId,
                        "sender_name" to senderUsername,
                        "sender_avatar" to (ProfileRepository.currentProfile.value?.avatarUrl ?: ""),
                        "target_id" to conversationId
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error con participantes: ${e.message}")
        }
        
        // 4. Actualizar lastMessage en la lista de conversaciones localmente (sin reload)
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(
                    lastMessage = content,
                    lastMessageAt = now
                )
            } else conv
        }
        
        // NO recargar mensajes ni conversaciones - el realtime se encarga de confirmar
        Log.d(TAG, "=== MENSAJE ENVIADO (esperando confirmación realtime) ===")
        return true
        
        } catch (e: Exception) {
            Log.e(TAG, "╔════════════════════════════════════════════╗")
            Log.e(TAG, "║       ERROR EN SEND MESSAGE                ║")
            Log.e(TAG, "╚════════════════════════════════════════════╝")
            Log.e(TAG, "Exception: ${e.message}", e)
            _lastError.value = "SEND ERROR: ${e.message}"
            return false
        }
    }
    
    // Subir y enviar imagen/video al chat
    suspend fun uploadAndSendMedia(context: Context, conversationId: String, mediaUri: Uri): String? {
        Log.d(TAG, "=== SUBIENDO MEDIA AL CHAT ===")
        _lastError.value = null
        
        try {
            // Leer bytes del archivo
            val inputStream = context.contentResolver.openInputStream(mediaUri)
                ?: run {
                    _lastError.value = "No se pudo abrir el archivo"
                    return null
                }
            
            val mediaBytes = inputStream.use { it.readBytes() }
            Log.d(TAG, "Media size: ${mediaBytes.size / 1024}KB")
            
            // Determinar tipo de media
            val mimeType = context.contentResolver.getType(mediaUri) ?: "image/jpeg"
            val isVideo = mimeType.startsWith("video")
            
            // Subir a ImageKit
            val uploadResult = if (isVideo) {
                ImageKitService.uploadVideoBytes(mediaBytes, folder = "chat_media")
            } else {
                ImageKitService.uploadImageBytes(mediaBytes, folder = "chat_media")
            }
            
            if (uploadResult.isFailure) {
                _lastError.value = "Error subiendo: ${uploadResult.exceptionOrNull()?.message}"
                return null
            }
            
            val mediaUrl = uploadResult.getOrNull() ?: return null
            Log.d(TAG, "Media subida: $mediaUrl")
            
            // Enviar mensaje con la URL de la imagen
            val mediaPrefix = if (isVideo) "[VIDEO]" else "[IMG]"
            val messageContent = "$mediaPrefix$mediaUrl"
            
            val sent = sendMessage(conversationId, messageContent)
            return if (sent) mediaUrl else null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en uploadAndSendMedia: ${e.message}", e)
            _lastError.value = "Error: ${e.message}"
            return null
        }
    }
    
    // Subir y enviar audio al chat
    suspend fun uploadAndSendAudio(context: Context, conversationId: String, audioUri: Uri): String? {
        Log.d(TAG, "=== SUBIENDO AUDIO AL CHAT ===")
        _lastError.value = null
        
        try {
            val inputStream = context.contentResolver.openInputStream(audioUri)
                ?: run {
                    _lastError.value = "No se pudo abrir el archivo de audio"
                    return null
                }
            
            val audioBytes = inputStream.use { it.readBytes() }
            Log.d(TAG, "Audio size: ${audioBytes.size / 1024}KB")
            
            // Subir a ImageKit
            val uploadResult = ImageKitService.uploadAudioBytes(audioBytes, folder = "chat_audio")
            
            if (uploadResult.isFailure) {
                _lastError.value = "Error subiendo audio: ${uploadResult.exceptionOrNull()?.message}"
                return null
            }
            
            val audioUrl = uploadResult.getOrNull() ?: return null
            Log.d(TAG, "Audio subido: $audioUrl")
            
            // Enviar mensaje con prefijo [AUDIO]
            val messageContent = "[AUDIO]$audioUrl"
            val sent = sendMessage(conversationId, messageContent)
            return if (sent) audioUrl else null
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en uploadAndSendAudio: ${e.message}", e)
            _lastError.value = "Error: ${e.message}"
            return null
        }
    }
    
    // Agregar o quitar reacción a un mensaje
    suspend fun toggleReaction(messageId: String, emoji: String): Boolean {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return false
            Log.d(TAG, "Toggle reaction $emoji en mensaje $messageId")
            
            // Obtener mensaje actual
            val message = _currentMessages.value.find { it.id == messageId } ?: return false
            val currentReactions = message.reactions.toMutableMap()
            
            // Toggle: agregar o quitar usuario de la lista
            val usersForEmoji = currentReactions[emoji]?.toMutableList() ?: mutableListOf()
            if (currentUserId in usersForEmoji) {
                usersForEmoji.remove(currentUserId)
                if (usersForEmoji.isEmpty()) {
                    currentReactions.remove(emoji)
                } else {
                    currentReactions[emoji] = usersForEmoji
                }
            } else {
                usersForEmoji.add(currentUserId)
                currentReactions[emoji] = usersForEmoji
            }
            
            // Convertir a JSON string
            val reactionsJson = if (currentReactions.isEmpty()) {
                null
            } else {
                val jsonObj = org.json.JSONObject()
                currentReactions.forEach { (emoji, users) ->
                    val jsonArray = org.json.JSONArray()
                    users.forEach { userId -> jsonArray.put(userId) }
                    jsonObj.put(emoji, jsonArray)
                }
                jsonObj.toString()
            }
            
            // Actualizar en Supabase
            val updateJson = buildJsonObject {
                if (reactionsJson != null) {
                    put("reactions", reactionsJson)
                } else {
                    put("reactions", JsonNull)
                }
            }
            
            SupabaseClient.database
                .from("messages")
                .update(updateJson) {
                    filter { eq("id", messageId) }
                }
            
            // Actualizar localmente
            _currentMessages.value = _currentMessages.value.map {
                if (it.id == messageId) it.copy(reactions = currentReactions) else it
            }
            
            Log.d(TAG, "✓ Reacción actualizada")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en toggleReaction: ${e.message}", e)
            return false
        }
    }
    
    // Editar un mensaje existente
    suspend fun updateMessage(messageId: String, newContent: String): Boolean {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return false
            Log.d(TAG, "Editando mensaje $messageId con nuevo contenido: $newContent")
            
            val updateJson = buildJsonObject {
                put("content", newContent)
            }
            
            SupabaseClient.database
                .from("messages")
                .update(updateJson) {
                    filter {
                        eq("id", messageId)
                        eq("sender_id", currentUserId) // Solo el remitente puede editar
                    }
                }
            
            // Actualizar localmente
            val currentMessages = _currentMessages.value.toMutableList()
            val index = currentMessages.indexOfFirst { it.id == messageId }
            if (index != -1) {
                currentMessages[index] = currentMessages[index].copy(content = newContent)
                _currentMessages.value = currentMessages
            }
            
            Log.d(TAG, "✓ Mensaje editado correctamente")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error editando mensaje: ${e.message}", e)
            return false
        }
    }
    
    // Eliminar un mensaje (optimista: elimina localmente primero, Supabase después)
    suspend fun deleteMessage(messageId: String): Boolean {
        return try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return false
            
            // ELIMINACIÓN OPTIMISTA: Quitar del chat inmediatamente
            _currentMessages.value = _currentMessages.value.filter { it.id != messageId }
            Log.d(TAG, "✓ Mensaje eliminado del chat localmente")
            
            // Eliminar de Supabase (ya no bloqueamos la UI porque la lista local ya se actualizó)
            try {
                SupabaseClient.database
                    .from("messages")
                    .delete {
                        filter { 
                            eq("id", messageId)
                            eq("sender_id", currentUserId)
                        }
                    }
                Log.d(TAG, "✓ Mensaje eliminado de Supabase")
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error eliminando de Supabase (pero ya se eliminó localmente): ${e.message}")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error eliminando mensaje: ${e.message}", e)
            false
        }
    }
    
    // Marcar mensajes como leídos (actualiza status a 'read' para los ticks azules)
    suspend fun markMessagesAsRead(conversationId: String) {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return
            
            Log.d(TAG, "Marcando mensajes como leídos - convId: $conversationId, userId: $currentUserId")
            
            // Marcar mensajes como leídos con status = 'read'
            val readJson = buildJsonObject { 
                put("is_read", true)
                put("status", "read")
                put("read_at", java.time.Instant.now().toString())
            }
            
            // Actualizar mensajes que NO son míos y que NO están leídos
            SupabaseClient.database
                .from("messages")
                .update(readJson) {
                    filter { 
                        eq("conversation_id", conversationId)
                        neq("sender_id", currentUserId)
                        eq("is_read", false)
                    }
                }
            
            Log.d(TAG, "✓ Mensajes marcados como leídos")
            
            // Resetear contador de no leídos
            val resetUnreadJson = buildJsonObject { put("unread_count", 0) }
            SupabaseClient.database
                .from("conversation_participants")
                .update(resetUnreadJson) {
                    filter { 
                        eq("conversation_id", conversationId)
                        eq("user_id", currentUserId)
                    }
                }
                
        } catch (e: Exception) {
            Log.e(TAG, "Error marking messages as read: ${e.message}", e)
            _realtimeStatus.value = "ERR read: ${e.message?.take(15)}"
        }
    }
    
    // Abrir chat: cargar cache INSTANTÁNEAMENTE, luego actualizar desde servidor
    suspend fun openChat(conversationId: String, otherUserName: String? = null) {
        Log.d(TAG, "=== ABRIENDO CHAT ===")
        Log.d(TAG, "ConversationId: $conversationId, otherUser: $otherUserName")
        
        // Guardar nombre del otro usuario para notificaciones
        if (otherUserName != null) {
            currentOtherUserName = otherUserName
        }
        
        // 1. Limpiar estado anterior si es otra conversación
        if (currentConversationId != conversationId) {
            // NO limpiar los mensajes si hay cache - mostrar cache instantáneamente
            currentConversationId = conversationId
            currentOtherUserId = null
            _isOtherUserOnline.value = false
            _isOtherUserTyping.value = false
        }
        
        // 2. CARGAR CACHE INSTANTÁNEAMENTE si existe
        val cachedMessages = getCachedMessages(conversationId)
        if (cachedMessages != null && cachedMessages.isNotEmpty()) {
            val filtered = filterByClearedAt(cachedMessages, conversationId)
            Log.d(TAG, "✓ Cache encontrado: ${cachedMessages.size} mensajes (${filtered.size} después de filtro cleared_at)")
            _currentMessages.value = filtered
        }
        
        // Cancelar notificaciones de este chat al abrirlo
        appContext?.let { ChatNotificationService.cancelChatNotifications(it, conversationId) }
        
        // 3. Suscribirse al canal realtime
        subscribeToMessages(conversationId)
        
        // 4. Cargar mensajes frescos del servidor (actualizará el cache)
        loadMessages(conversationId)
    }
    
    // Estado de debug para Realtime
    private val _realtimeStatus = MutableStateFlow<String>("No conectado")
    val realtimeStatus: StateFlow<String> = _realtimeStatus.asStateFlow()
    
    // Suscribirse a nuevos mensajes en tiempo real
    private fun subscribeToMessages(conversationId: String) {
        // No recrear si ya está suscrito a esta conversación
        if (realtimeChannel != null && currentConversationId == conversationId) {
            _realtimeStatus.value = "Ya suscrito"
            return
        }
        
        scope.launch {
            try {
                // Cancelar suscripción anterior si existe
                if (realtimeChannel != null) {
                    try {
                        realtimeChannel?.unsubscribe()
                    } catch (e: Exception) { /* ignorar */ }
                    realtimeChannel = null
                }
                
                _realtimeStatus.value = "Conectando..."
                
                // 1. Conectar al websocket de Realtime (ignorar error si ya conectado)
                val realtime = SupabaseClient.client.realtime
                try {
                    realtime.connect()
                    kotlinx.coroutines.delay(500)
                } catch (e: Exception) {
                    // Si ya está conectado, continuar sin problema
                    if (!e.message.orEmpty().contains("already connected", ignoreCase = true)) {
                        _realtimeStatus.value = "WS err: ${e.message?.take(15)}"
                    }
                }
                
                _realtimeStatus.value = "WS OK, creando canal..."
                
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                
                // 2. Crear canal para esta conversación
                val channelName = "chat-$conversationId"
                val channel = SupabaseClient.client.channel(channelName)
                
                // 3. Configurar listener de postgres changes para NUEVOS mensajes
                val messageFlow = channel.postgresChangeFlow<PostgresAction.Insert>(
                    schema = "public"
                ) {
                    table = "messages"
                }
                
                // 4. Configurar listener de postgres changes para UPDATES (status de mensajes)
                val messageUpdateFlow = channel.postgresChangeFlow<PostgresAction.Update>(
                    schema = "public"
                ) {
                    table = "messages"
                }
                
                // 5. Configurar listener de BROADCAST para typing
                val typingFlow = channel.broadcastFlow<Map<String, Any>>("typing")
                
                // 6. Configurar listener de PRESENCE para online/offline
                val presenceFlow = channel.presenceChangeFlow()
                
                // Procesar eventos de PRESENCE (online/offline)
                presenceFlow.onEach { presenceAction ->
                    try {
                        // Contar usuarios en el canal (excluyéndome)
                        val joins = presenceAction.joins
                        val leaves = presenceAction.leaves
                        
                        Log.d(TAG, "PRESENCE: joins=${joins.size} leaves=${leaves.size}")
                        
                        // Si alguien se unió que no soy yo
                        joins.forEach { (key, _) ->
                            if (key != currentUserId) {
                                _isOtherUserOnline.value = true
                                _realtimeStatus.value = "✓ Usuario conectado"
                            }
                        }
                        
                        // Si alguien salió que no soy yo
                        leaves.forEach { (key, _) ->
                            if (key != currentUserId) {
                                _isOtherUserOnline.value = false
                                _isOtherUserTyping.value = false
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando presence: ${e.message}")
                    }
                }.launchIn(scope)
                
                // Procesar eventos de typing
                typingFlow.onEach { payload ->
                    try {
                        val senderUserId = payload["user_id"]?.toString()
                        val isTyping = payload["is_typing"]?.toString()?.toBoolean() ?: false
                        
                        Log.d(TAG, "TYPING recibido: user=$senderUserId typing=$isTyping myId=$currentUserId")
                        
                        // Solo mostrar si es del OTRO usuario
                        if (senderUserId != null && senderUserId != currentUserId) {
                            _isOtherUserTyping.value = isTyping
                            _realtimeStatus.value = if (isTyping) "✍️ Escribiendo..." else "✓ LISTO"
                            // Auto-reset después de 3 segundos si no llega otro evento
                            if (isTyping) {
                                scope.launch {
                                    kotlinx.coroutines.delay(3000)
                                    if (_isOtherUserTyping.value) {
                                        _isOtherUserTyping.value = false
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando typing: ${e.message}")
                    }
                }.launchIn(scope)
                
                // Procesar updates de mensajes (para actualizar ticks Y reacciones)
                messageUpdateFlow.onEach { change ->
                    try {
                        val record = change.record
                        val msgId = record["id"]?.jsonPrimitive?.content ?: ""
                        val msgStatus = record["status"]?.jsonPrimitive?.content ?: "sent"
                        val msgConversationId = record["conversation_id"]?.jsonPrimitive?.content ?: ""
                        val reactionsRaw = record["reactions"]?.let { if (it is JsonNull) null else it.jsonPrimitive.content }
                        
                        if (msgConversationId != conversationId) return@onEach
                        
                        Log.d(TAG, "UPDATE recibido - msgId: $msgId, reactions: $reactionsRaw")
                        
                        // Parsear reacciones si existen
                        val reactions = parseReactionsJson(reactionsRaw)
                        
                        // Actualizar status Y reacciones del mensaje local
                        _currentMessages.value = _currentMessages.value.map { msg ->
                            if (msg.id == msgId) {
                                msg.copy(
                                    status = when(msgStatus) {
                                        "read" -> MessageStatus.READ
                                        "delivered" -> MessageStatus.DELIVERED
                                        else -> MessageStatus.SENT
                                    },
                                    reactions = reactions
                                )
                            } else msg
                        }
                        
                        Log.d(TAG, "✓ Mensaje actualizado con reacciones: $reactions")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando update: ${e.message}")
                    }
                }.launchIn(scope)
                
                // Procesar mensajes entrantes
                messageFlow.onEach { change ->
                    try {
                        val record = change.record
                        
                        val msgId = record["id"]?.jsonPrimitive?.content ?: ""
                        val msgConversationId = record["conversation_id"]?.jsonPrimitive?.content ?: ""
                        val msgSenderId = record["sender_id"]?.jsonPrimitive?.content ?: ""
                        val msgContent = record["content"]?.jsonPrimitive?.content ?: ""
                        val msgCreatedAt = record["created_at"]?.jsonPrimitive?.content ?: ""
                        val clientTempId = record["client_temp_id"]?.let { if (it is JsonNull) null else it.jsonPrimitive.content.takeIf { c -> c != "null" && c.isNotEmpty() } }
                        
                        // Solo mensajes de ESTA conversación
                        if (msgConversationId != conversationId) return@onEach
                        
                        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                        val isFromMe = msgSenderId == currentUserId
                        
                        // Evitar duplicados
                        if (_currentMessages.value.any { it.id == msgId }) return@onEach
                        
                        // Buscar mensaje optimista
                        val optimisticIndex = if (clientTempId != null) {
                            _currentMessages.value.indexOfFirst { it.id == clientTempId }
                        } else -1
                        
                        val newMessage = Message(
                            id = msgId,
                            conversationId = msgConversationId,
                            senderId = msgSenderId,
                            content = msgContent,
                            createdAt = msgCreatedAt,
                            isRead = isFromMe,
                            isFromMe = isFromMe,
                            status = if (isFromMe) MessageStatus.SENT else MessageStatus.READ
                        )
                        
                        // Verificar si el mensaje es posterior al cleared_at
                        val clearedAt = getClearedAt(conversationId)
                        val isAfterClear = clearedAt == null || msgCreatedAt > clearedAt
                        
                        if (optimisticIndex >= 0) {
                            _currentMessages.value = _currentMessages.value.toMutableList().apply {
                                set(optimisticIndex, newMessage)
                            }
                            _realtimeStatus.value = "✓ Confirmado"
                        } else if (isAfterClear) {
                            _currentMessages.value = _currentMessages.value + newMessage
                            _realtimeStatus.value = "✓ NUEVO: ${msgContent.take(10)}..."
                            
                            // El otro usuario dejó de escribir (acaba de enviar)
                            _isOtherUserTyping.value = false
                            
                            if (!isFromMe) {
                                // Marcar como leído
                                scope.launch { markMessagesAsRead(conversationId) }
                            }
                        }
                    } catch (e: Exception) {
                        _realtimeStatus.value = "ERR proc: ${e.message?.take(15)}"
                    }
                }.launchIn(scope)
                
                // 6. Escuchar estado del canal
                channel.status.onEach { status ->
                    _realtimeStatus.value = "Canal: $status"
                }.launchIn(scope)
                
                // 7. Guardar referencia
                realtimeChannel = channel
                
                // 8. Suscribir al canal
                _realtimeStatus.value = "Suscribiendo..."
                channel.subscribe(blockUntilSubscribed = true)
                
                // 9. Registrar presencia (respetando configuración de privacidad)
                try {
                    // Check if user has online status enabled
                    val privacySettings = try {
                        UserPreferencesRepository.loadPrivacySettings(currentUserId)
                    } catch (_: Exception) { null }
                    
                    val showOnline = privacySettings?.showOnlineStatus ?: true
                    
                    if (showOnline) {
                        channel.track(
                            buildJsonObject {
                                put("user_id", currentUserId)
                                put("online_at", java.time.Instant.now().toString())
                            }
                        )
                        Log.d(TAG, "✓ Presencia registrada (online visible)")
                    } else {
                        Log.d(TAG, "⊘ Presencia NO registrada (privacidad: online oculto)")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error tracking presence: ${e.message}")
                }
                
                _realtimeStatus.value = "✓ ESCUCHANDO"
                
            } catch (e: Exception) {
                _realtimeStatus.value = "ERR: ${e.message?.take(25)}"
            }
        }
    }
    
    // Limpiar al cerrar chat
    fun clearCurrentChat() {
        currentConversationId = null
        currentOtherUserId = null
        _currentMessages.value = emptyList()
        _isOtherUserOnline.value = false
        _isOtherUserTyping.value = false
        scope.launch {
            try {
                realtimeChannel?.untrack()
            } catch (e: Exception) { /* ignorar */ }
            realtimeChannel?.unsubscribe()
            realtimeChannel = null
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // SOLICITUDES DE CLIENTE VIA CHAT
    // ═══════════════════════════════════════════════════════════════
    
    // Prefijos especiales para mensajes de sistema
    const val CLIENT_REQUEST_PREFIX = "[[CLIENT_REQUEST]]"
    const val CLIENT_REQUEST_ACCEPTED = "[[CLIENT_ACCEPTED]]"
    const val CLIENT_REQUEST_REJECTED = "[[CLIENT_REJECTED]]"
    const val CLIENT_REQUEST_PENDING = "[[CLIENT_PENDING]]"
    
    /**
     * Enviar solicitud de cliente - solo via notificaciones (más escalable)
     */
    suspend fun sendClientRequest(sellerId: String, sellerUsername: String): Result<Unit> {
        return try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val currentUsername = ProfileRepository.currentProfile.value?.username ?: "Usuario"
            
            Log.d(TAG, "Enviando solicitud de cliente a: $sellerUsername ($sellerId)")
            
            // Solo crear notificaciones - más escalable que mensajes de chat
            // Notificación al vendedor con botones de aceptar/rechazar
            NotificationRepository.createClientRequestNotification(
                sellerId = sellerId,
                requesterUsername = currentUsername
            )
            
            // Notificación al solicitante de estado pendiente
            NotificationRepository.createClientPendingNotification(
                requesterId = currentUserId,
                sellerUsername = sellerUsername
            )
            
            Log.d(TAG, "✓ Solicitud de cliente enviada via notificaciones")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando solicitud de cliente: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Aceptar solicitud de cliente - solo actualiza followers y notifica
     */
    suspend fun acceptClientFromChat(requesterId: String, requesterUsername: String): Result<Unit> {
        return try {
            Log.d(TAG, "Aceptando cliente: $requesterUsername ($requesterId)")
            
            // Actualizar en followers
            val acceptResult = FollowersRepository.acceptClientRequest(requesterId)
            if (acceptResult.isFailure) {
                return acceptResult
            }
            
            // Solo crear notificación - no enviar mensaje al chat
            val sellerUsername = ProfileRepository.currentProfile.value?.username ?: "vendedor"
            NotificationRepository.createClientAcceptedNotification(
                requesterId = requesterId,
                sellerUsername = sellerUsername
            )
            
            Log.d(TAG, "✓ Cliente aceptado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error aceptando cliente: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Rechazar solicitud de cliente - solo actualiza followers y notifica
     */
    suspend fun rejectClientFromChat(requesterId: String, requesterUsername: String, reason: String = ""): Result<Unit> {
        return try {
            Log.d(TAG, "Rechazando cliente: $requesterUsername ($requesterId)")
            
            // Actualizar en followers (eliminar solicitud pendiente)
            val rejectResult = FollowersRepository.rejectClientRequest(requesterId)
            if (rejectResult.isFailure) {
                return rejectResult
            }
            
            // Solo crear notificación - no enviar mensaje al chat
            val sellerUsername = ProfileRepository.currentProfile.value?.username ?: "vendedor"
            NotificationRepository.createClientRejectedNotification(
                requesterId = requesterId,
                sellerUsername = sellerUsername,
                reason = reason
            )
            
            Log.d(TAG, "✓ Cliente rechazado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error rechazando cliente: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verificar si un mensaje es una solicitud de cliente
     */
    fun isClientRequestMessage(content: String): Boolean = content.startsWith(CLIENT_REQUEST_PREFIX)
    fun isClientAcceptedMessage(content: String): Boolean = content.startsWith(CLIENT_REQUEST_ACCEPTED)
    fun isClientRejectedMessage(content: String): Boolean = content.startsWith(CLIENT_REQUEST_REJECTED)
    fun isClientPendingMessage(content: String): Boolean = content.startsWith(CLIENT_REQUEST_PENDING)
    
    /**
     * Obtener el contenido limpio del mensaje (sin prefijo)
     */
    fun getCleanMessageContent(content: String): String {
        return when {
            content.startsWith(CLIENT_REQUEST_PREFIX) -> content.removePrefix(CLIENT_REQUEST_PREFIX)
            content.startsWith(CLIENT_REQUEST_ACCEPTED) -> content.removePrefix(CLIENT_REQUEST_ACCEPTED)
            content.startsWith(CLIENT_REQUEST_REJECTED) -> content.removePrefix(CLIENT_REQUEST_REJECTED)
            content.startsWith(CLIENT_REQUEST_PENDING) -> content.removePrefix(CLIENT_REQUEST_PENDING)
            else -> content
        }
    }
    
    /**
     * Parsear JSON de reacciones a Map<String, List<String>>
     */
    private fun parseReactionsJson(reactionsRaw: String?): Map<String, List<String>> {
        if (reactionsRaw.isNullOrBlank() || reactionsRaw == "null") {
            return emptyMap()
        }
        return try {
            val json = org.json.JSONObject(reactionsRaw)
            val result = mutableMapOf<String, List<String>>()
            json.keys().forEach { emoji ->
                val usersArray = json.getJSONArray(emoji)
                val users = mutableListOf<String>()
                for (i in 0 until usersArray.length()) {
                    users.add(usersArray.getString(i))
                }
                result[emoji] = users
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando reacciones: ${e.message}")
            emptyMap()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // SUSCRIPCIÓN GLOBAL PARA BADGE DE MENSAJES EN HOME
    // ═══════════════════════════════════════════════════════════════
    
    private var globalMessagesChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var globalParticipantsChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var isGlobalSubscribed = false
    
    /**
     * Suscribirse a nuevos mensajes para actualizar el badge en HomeHeader
     * Debe llamarse al iniciar la app (después del login)
     */
    suspend fun subscribeToGlobalMessages() {
        if (isGlobalSubscribed) return
        
        val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return
        
        try {
            // Canal para nuevos mensajes
            globalMessagesChannel = SupabaseClient.client.channel("global-messages-badge")
            
            globalMessagesChannel?.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = "messages"
            }?.onEach { change ->
                val record = change.record
                val msgConvId = record["conversation_id"]?.jsonPrimitive?.content ?: ""
                val msgContent = record["content"]?.jsonPrimitive?.content ?: ""
                val msgCreatedAt = record["created_at"]?.jsonPrimitive?.content ?: ""
                val senderId = record["sender_id"]?.jsonPrimitive?.content
                val isFromMe = senderId == currentUserId
                
                Log.d(TAG, "🔔 Nuevo mensaje INSERT detectado (conv=$msgConvId)")
                
                // Actualización dirigida: solo mover la conversación afectada al tope
                val currentList = _conversations.value.toMutableList()
                val idx = currentList.indexOfFirst { it.id == msgConvId }
                if (idx >= 0) {
                    val updated = currentList[idx].copy(
                        lastMessage = msgContent,
                        lastMessageAt = msgCreatedAt,
                        unreadCount = if (isFromMe) currentList[idx].unreadCount else currentList[idx].unreadCount + 1
                    )
                    currentList.removeAt(idx)
                    currentList.add(0, updated)
                    _conversations.value = currentList
                    _totalUnreadCount.value = currentList.sumOf { it.unreadCount }
                    BadgeCountCache.setMessageCount(_totalUnreadCount.value)
                } else {
                    // Conversación nueva, hacer reload completo solo esta vez
                    loadConversations()
                }
                
                // Reproducir sonido SOLO si el mensaje NO es nuestro
                if (senderId != null && !isFromMe) {
                    appContext?.let { com.rendly.app.util.SoundManager.init(it) }
                    com.rendly.app.util.SoundManager.playMessageSound()
                }
            }?.launchIn(scope)
            
            globalMessagesChannel?.subscribe()
            
            // Canal separado para cambios en unread_count de participants
            globalParticipantsChannel = SupabaseClient.client.channel("global-participants-badge")
            
            globalParticipantsChannel?.postgresChangeFlow<PostgresAction.Update>(
                schema = "public"
            ) {
                table = "conversation_participants"
                filter = "user_id=eq.$currentUserId"
            }?.onEach { change ->
                try {
                    val record = change.record
                    val convId = record["conversation_id"]?.jsonPrimitive?.content ?: ""
                    val newUnread = record["unread_count"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    
                    Log.d(TAG, "\uD83D\uDD14 Participant UPDATE: conv=$convId unread=$newUnread")
                    
                    // Actualización dirigida: solo cambiar el unread_count de ESA conversación
                    val currentList = _conversations.value.toMutableList()
                    val idx = currentList.indexOfFirst { it.id == convId }
                    if (idx >= 0) {
                        currentList[idx] = currentList[idx].copy(unreadCount = newUnread)
                        _conversations.value = currentList
                        _totalUnreadCount.value = currentList.sumOf { it.unreadCount }
                        BadgeCountCache.setMessageCount(_totalUnreadCount.value)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando participant update: ${e.message}")
                }
            }?.launchIn(scope)
            
            globalParticipantsChannel?.subscribe()
            
            isGlobalSubscribed = true
            Log.d(TAG, "✅ Suscrito a mensajes y participants para badge (userId: $currentUserId)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to global messages: ${e.message}", e)
        }
    }
    
    /**
     * Desuscribirse de mensajes globales
     */
    suspend fun unsubscribeFromGlobalMessages() {
        try {
            globalMessagesChannel?.unsubscribe()
            globalParticipantsChannel?.unsubscribe()
            isGlobalSubscribed = false
            Log.d(TAG, "Desuscrito de mensajes globales")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from global messages: ${e.message}", e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CHAT SETTINGS - Mute, Block, Report, Clear, Export, Search
    // ═══════════════════════════════════════════════════════════════
    
    /** Silenciar notificaciones de una conversación */
    suspend fun muteChat(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("muted_chats")
                .upsert(mapOf(
                    "user_id" to userId,
                    "conversation_id" to conversationId
                ))
            Log.d(TAG, "✅ Chat silenciado: $conversationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error silenciando chat: ${e.message}")
            false
        }
    }
    
    /** Reactivar notificaciones de una conversación */
    suspend fun unmuteChat(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("muted_chats")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("conversation_id", conversationId)
                    }
                }
            Log.d(TAG, "✅ Chat desilenciado: $conversationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error desilenciando chat: ${e.message}")
            false
        }
    }
    
    /** Verificar si una conversación está silenciada */
    suspend fun isChatMuted(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            val result = SupabaseClient.database
                .from("muted_chats")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("conversation_id", conversationId)
                    }
                }
                .decodeList<kotlinx.serialization.json.JsonObject>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /** Vaciar chat - solo local, persiste cleared_at en SharedPreferences */
    suspend fun clearChat(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Guardar timestamp en UTC para comparación fiable con timestamps de Supabase
            val now = java.time.Instant.now().toString()
            setClearedAt(conversationId, now)
            
            // Limpiar mensajes locales y cache
            _currentMessages.value = emptyList()
            messagesCache.remove(conversationId)
            
            Log.d(TAG, "✅ Chat vaciado localmente: $conversationId (cleared_at=$now)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error vaciando chat: ${e.message}")
            false
        }
    }
    
    /** Eliminar conversación del listado (ocultar localmente usando cleared_at + remover de lista) */
    suspend fun deleteConversation(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val now = java.time.OffsetDateTime.now().toString()
            setClearedAt(conversationId, now)
            messagesCache.remove(conversationId)
            _conversations.value = _conversations.value.filter { it.id != conversationId }
            _totalUnreadCount.value = _conversations.value.sumOf { it.unreadCount }
            BadgeCountCache.setMessageCount(_totalUnreadCount.value)
            Log.d(TAG, "✅ Conversación eliminada del listado: $conversationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando conversación: ${e.message}")
            false
        }
    }
    
    /** Buscar mensajes en una conversación */
    suspend fun searchMessages(conversationId: String, query: String): List<Message> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) return@withContext emptyList()
            
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            
            val results = SupabaseClient.database
                .from("messages")
                .select {
                    filter {
                        eq("conversation_id", conversationId)
                        ilike("content", "%$query%")
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(50)
                }
                .decodeList<MessageDB>()
            
            results.map { msg ->
                val isFromMe = msg.senderId == currentUserId
                Message(
                    id = msg.id,
                    conversationId = msg.conversationId,
                    senderId = msg.senderId,
                    content = msg.content,
                    createdAt = msg.createdAt ?: "",
                    isRead = msg.isRead,
                    isFromMe = isFromMe,
                    status = when {
                        !isFromMe -> MessageStatus.READ
                        msg.status == "read" -> MessageStatus.READ
                        msg.status == "delivered" -> MessageStatus.DELIVERED
                        else -> MessageStatus.SENT
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando mensajes: ${e.message}")
            emptyList()
        }
    }
    
    /** Bloquear usuario */
    suspend fun blockUser(blockedId: String, reason: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("blocked_users")
                .upsert(buildMap {
                    put("blocker_id", userId)
                    put("blocked_id", blockedId)
                    if (reason != null) put("reason", reason)
                })
            Log.d(TAG, "✅ Usuario bloqueado: $blockedId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error bloqueando usuario: ${e.message}")
            false
        }
    }
    
    /** Desbloquear usuario */
    suspend fun unblockUser(blockedId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("blocked_users")
                .delete {
                    filter {
                        eq("blocker_id", userId)
                        eq("blocked_id", blockedId)
                    }
                }
            Log.d(TAG, "✅ Usuario desbloqueado: $blockedId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error desbloqueando usuario: ${e.message}")
            false
        }
    }
    
    /** Verificar si un usuario está bloqueado */
    suspend fun isUserBlocked(otherUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            val result = SupabaseClient.database
                .from("blocked_users")
                .select {
                    filter {
                        eq("blocker_id", userId)
                        eq("blocked_id", otherUserId)
                    }
                }
                .decodeList<kotlinx.serialization.json.JsonObject>()
            result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /** Obtener lista de usuarios bloqueados con info de perfil */
    suspend fun getBlockedUsers(): List<BlockedUserInfo> = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            val results = SupabaseClient.database
                .from("blocked_users")
                .select {
                    filter { eq("blocker_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<BlockedUserDB>()
            
            // Fetch user profiles for each blocked user
            results.mapNotNull { blocked ->
                try {
                    val userResult = SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { eq("user_id", blocked.blockedId) }
                            limit(1)
                        }
                        .decodeList<kotlinx.serialization.json.JsonObject>()
                    
                    val userObj = userResult.firstOrNull()
                    val username = userObj?.get("username")?.jsonPrimitive?.content ?: "usuario"
                    val avatarUrl = userObj?.get("avatar_url")?.let {
                        if (it is kotlinx.serialization.json.JsonNull) null else it.jsonPrimitive.content
                    }
                    
                    BlockedUserInfo(
                        id = blocked.blockedId,
                        username = username,
                        avatarUrl = avatarUrl,
                        blockedAt = blocked.createdAt ?: "",
                        reason = blocked.reason
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching blocked user profile: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuarios bloqueados: ${e.message}")
            emptyList()
        }
    }
    
    /** Reportar usuario */
    suspend fun reportUser(
        reportedUserId: String,
        reason: String,
        description: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("content_reports")
                .insert(buildMap {
                    put("reporter_id", userId)
                    put("content_type", "user")
                    put("content_id", reportedUserId)
                    put("reported_user_id", reportedUserId)
                    put("reason", reason)
                    if (description != null) put("description", description)
                })
            Log.d(TAG, "✅ Usuario reportado: $reportedUserId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error reportando usuario: ${e.message}")
            false
        }
    }
    
    /** Limpiar contenido de mensaje para exportación legible */
    private fun cleanMessageContent(content: String): String {
        return when {
            content.startsWith("[SHARED_POST]") -> {
                try {
                    val json = kotlinx.serialization.json.Json.parseToJsonElement(
                        content.removePrefix("[SHARED_POST]")
                    ).jsonObject
                    val title = json["title"]?.jsonPrimitive?.content ?: "Producto"
                    val price = json["price"]?.jsonPrimitive?.content ?: ""
                    val customMsg = json["customMessage"]?.jsonPrimitive?.content ?: ""
                    if (customMsg.isNotBlank()) "Artículo compartido: $title (\$$price) - \"$customMsg\""
                    else "Artículo compartido: $title (\$$price)"
                } catch (e: Exception) { "Artículo compartido" }
            }
            content.startsWith("[CONSULT_POST]") -> {
                try {
                    val json = kotlinx.serialization.json.Json.parseToJsonElement(
                        content.removePrefix("[CONSULT_POST]")
                    ).jsonObject
                    val title = json["productTitle"]?.jsonPrimitive?.content ?: "Producto"
                    val msg = json["message"]?.jsonPrimitive?.content ?: ""
                    val type = json["type"]?.jsonPrimitive?.content ?: ""
                    if (type == "offer") "Oferta sobre $title: $msg"
                    else "Consulta sobre $title: $msg"
                } catch (e: Exception) { "Consulta de producto" }
            }
            content.startsWith("[HANDSHAKE_STATUS]") -> {
                try {
                    val json = kotlinx.serialization.json.Json.parseToJsonElement(
                        content.removePrefix("[HANDSHAKE_STATUS]")
                    ).jsonObject
                    val type = json["type"]?.jsonPrimitive?.content ?: ""
                    when (type) {
                        "TRANSACTION_COMPLETED" -> {
                            val desc = json["productDescription"]?.jsonPrimitive?.content ?: ""
                            val price = json["agreedPrice"]?.jsonPrimitive?.content ?: ""
                            "Transacción completada: $desc (\$$price)"
                        }
                        "AGREEMENT_CANCELLED" -> "Acuerdo cancelado"
                        else -> "Actualización de acuerdo: $type"
                    }
                } catch (e: Exception) { "Actualización de acuerdo" }
            }
            content.startsWith("[AUDIO]") -> "Mensaje de voz"
            content.startsWith("[LOCATION]") -> {
                val coords = content.removePrefix("[LOCATION]").split(",")
                if (coords.size == 2) "Ubicación: ${coords[0]}, ${coords[1]}"
                else "Ubicación compartida"
            }
            content.startsWith("[FILE]") -> {
                try {
                    val json = kotlinx.serialization.json.Json.parseToJsonElement(
                        content.removePrefix("[FILE]")
                    ).jsonObject
                    val name = json["name"]?.jsonPrimitive?.content ?: "archivo"
                    "Archivo: $name"
                } catch (e: Exception) { "Archivo adjunto" }
            }
            content.startsWith("[IMAGE]") -> "Imagen"
            content.startsWith("[SHARED_USER]") -> {
                try {
                    val json = kotlinx.serialization.json.Json.parseToJsonElement(
                        content.removePrefix("[SHARED_USER]")
                    ).jsonObject
                    val username = json["username"]?.jsonPrimitive?.content ?: "usuario"
                    "Contacto compartido: @$username"
                } catch (e: Exception) { "Contacto compartido" }
            }
            else -> content
        }
    }

    /** Exportar chat como PDF profesional */
    suspend fun exportChatAsPdf(
        context: android.content.Context,
        conversationId: String,
        otherUsername: String
    ): java.io.File? = withContext(Dispatchers.IO) {
        try {
            val messages = SupabaseClient.database
                .from("messages")
                .select {
                    filter { eq("conversation_id", conversationId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<MessageDB>()
            
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: ""
            val now = java.time.LocalDateTime.now()
            val dateStr = "${now.dayOfMonth}/${now.monthValue}/${now.year} ${now.hour}:${String.format("%02d", now.minute)}"
            
            // PDF generation with Android PdfDocument
            val pageWidth = 595  // A4
            val pageHeight = 842
            val margin = 40f
            val contentWidth = pageWidth - (margin * 2)
            
            val document = android.graphics.pdf.PdfDocument()
            var pageNumber = 1
            var currentY = margin
            
            val titlePaint = android.graphics.Paint().apply {
                textSize = 18f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#1A1A2E")
                isAntiAlias = true
            }
            val subtitlePaint = android.graphics.Paint().apply {
                textSize = 11f; color = android.graphics.Color.parseColor("#6B7280")
                isAntiAlias = true
            }
            val senderPaint = android.graphics.Paint().apply {
                textSize = 10f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#7C3AED")
                isAntiAlias = true
            }
            val otherSenderPaint = android.graphics.Paint().apply {
                textSize = 10f; isFakeBoldText = true; color = android.graphics.Color.parseColor("#2563EB")
                isAntiAlias = true
            }
            val messagePaint = android.graphics.Paint().apply {
                textSize = 11f; color = android.graphics.Color.parseColor("#1F2937")
                isAntiAlias = true
            }
            val timePaint = android.graphics.Paint().apply {
                textSize = 9f; color = android.graphics.Color.parseColor("#9CA3AF")
                isAntiAlias = true
            }
            val linePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#E5E7EB"); strokeWidth = 0.5f
            }
            val headerBgPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#F3F0FF")
            }
            
            var currentPage: android.graphics.pdf.PdfDocument.Page? = null
            
            fun newPage(): android.graphics.Canvas {
                currentPage?.let { document.finishPage(it) }
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = document.startPage(pageInfo)
                currentPage = page
                pageNumber++
                currentY = margin
                // Footer
                val footerPaint = android.graphics.Paint().apply {
                    textSize = 8f; color = android.graphics.Color.parseColor("#D1D5DB")
                    isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER
                }
                page.canvas.drawText("Rendly - Exportación de chat - Pág. ${pageNumber - 1}", pageWidth / 2f, pageHeight - 20f, footerPaint)
                return page.canvas
            }
            
            var canvas = newPage()
            
            // Header background
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, headerBgPaint)
            
            // Title
            canvas.drawText("Conversación con @$otherUsername", margin, margin + 20f, titlePaint)
            canvas.drawText("Exportado el $dateStr  |  ${messages.size} mensajes", margin, margin + 38f, subtitlePaint)
            canvas.drawText("Rendly - Registro oficial de conversación", margin, margin + 52f, subtitlePaint)
            
            currentY = 100f
            
            // Separator line
            canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
            currentY += 12f
            
            // Messages
            messages.forEach { msg ->
                val isFromMe = msg.senderId == currentUserId
                val sender = if (isFromMe) "Tú" else "@$otherUsername"
                val time = msg.createdAt?.take(16)?.replace("T", " ") ?: ""
                val cleanContent = cleanMessageContent(msg.content)
                
                // Wrap text to fit content width
                val words = cleanContent.split(" ")
                val lines = mutableListOf<String>()
                var currentLine = StringBuilder()
                words.forEach { word ->
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (messagePaint.measureText(testLine) > contentWidth - 20f) {
                        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                        currentLine = StringBuilder(word)
                    } else {
                        currentLine = StringBuilder(testLine)
                    }
                }
                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                
                val blockHeight = 16f + (lines.size * 15f) + 10f
                
                // Check if we need a new page
                if (currentY + blockHeight > pageHeight - 50f) {
                    canvas = newPage()
                }
                
                // Sender + time
                canvas.drawText(sender, margin + 4f, currentY + 12f, if (isFromMe) senderPaint else otherSenderPaint)
                canvas.drawText(time, pageWidth - margin - timePaint.measureText(time), currentY + 12f, timePaint)
                currentY += 16f
                
                // Content lines
                lines.forEach { line ->
                    canvas.drawText(line, margin + 4f, currentY + 12f, messagePaint)
                    currentY += 15f
                }
                
                currentY += 4f
                // Light separator
                canvas.drawLine(margin + 4f, currentY, pageWidth - margin, currentY, linePaint)
                currentY += 8f
            }
            
            currentPage?.let { document.finishPage(it) }
            
            // Save to cache
            val file = java.io.File(context.cacheDir, "chat_${otherUsername}_${System.currentTimeMillis()}.pdf")
            java.io.FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            
            Log.d(TAG, "✅ PDF exportado: ${file.absolutePath} (${file.length()} bytes)")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error exportando chat PDF: ${e.message}", e)
            null
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CHATS FIJADOS (PIN/UNPIN)
    // ═══════════════════════════════════════════════════════════════
    
    /** Fijar un chat */
    suspend fun pinChat(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("pinned_chats")
                .upsert(buildJsonObject {
                    put("user_id", userId)
                    put("conversation_id", conversationId)
                })
            // Actualizar localmente
            _conversations.value = _conversations.value.map {
                if (it.id == conversationId) it.copy(isPinned = true) else it
            }
            Log.d(TAG, "✅ Chat fijado: $conversationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error fijando chat: ${e.message}")
            false
        }
    }
    
    /** Desfijar un chat */
    suspend fun unpinChat(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("pinned_chats")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("conversation_id", conversationId)
                    }
                }
            // Actualizar localmente
            _conversations.value = _conversations.value.map {
                if (it.id == conversationId) it.copy(isPinned = false) else it
            }
            Log.d(TAG, "✅ Chat desfijado: $conversationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error desfijando chat: ${e.message}")
            false
        }
    }
    
    /** Verificar si un chat está fijado */
    suspend fun isChatPinned(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            val result = SupabaseClient.database
                .from("pinned_chats")
                .select { filter { eq("user_id", userId); eq("conversation_id", conversationId) } }
                .decodeList<PinnedChatDB>()
            result.isNotEmpty()
        } catch (e: Exception) { false }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ETIQUETAS DE CHAT (LABELS)
    // ═══════════════════════════════════════════════════════════════
    
    private val _userLabels = MutableStateFlow<List<ChatLabel>>(emptyList())
    val userLabels: StateFlow<List<ChatLabel>> = _userLabels.asStateFlow()
    
    /** Cargar etiquetas del usuario (crea las por defecto si no existen) */
    suspend fun loadUserLabels(): List<ChatLabel> = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            
            // Intentar crear etiquetas por defecto si no existen
            try {
                SupabaseClient.database.rpc("create_default_labels_if_needed", buildJsonObject {
                    put("p_user_id", userId)
                })
            } catch (e: Exception) {
                Log.d(TAG, "Default labels RPC: ${e.message}")
            }
            
            val labels = SupabaseClient.database
                .from("chat_labels")
                .select {
                    filter { eq("user_id", userId) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<ChatLabelDB>()
                .map { ChatLabel(it.id, it.name, it.color, it.icon, it.sortOrder) }
            
            _userLabels.value = labels
            Log.d(TAG, "✅ Etiquetas cargadas: ${labels.size}")
            labels
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando etiquetas: ${e.message}")
            emptyList()
        }
    }
    
    /** Crear nueva etiqueta */
    suspend fun createLabel(name: String, color: String, icon: String? = null): ChatLabel? = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext null
            val maxOrder = _userLabels.value.maxOfOrNull { it.sortOrder } ?: 0
            
            val result = SupabaseClient.database
                .from("chat_labels")
                .insert(buildJsonObject {
                    put("user_id", userId)
                    put("name", name)
                    put("color", color)
                    if (icon != null) put("icon", icon)
                    put("sort_order", maxOrder + 1)
                }) { select() }
                .decodeSingleOrNull<ChatLabelDB>()
            
            if (result != null) {
                val label = ChatLabel(result.id, result.name, result.color, result.icon, result.sortOrder)
                _userLabels.value = _userLabels.value + label
                Log.d(TAG, "✅ Etiqueta creada: ${label.name}")
                label
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error creando etiqueta: ${e.message}")
            null
        }
    }
    
    /** Actualizar etiqueta existente */
    suspend fun updateLabel(labelId: String, name: String, color: String, icon: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("chat_labels")
                .update(buildJsonObject {
                    put("name", name)
                    put("color", color)
                    if (icon != null) put("icon", icon) else put("icon", JsonNull)
                }) {
                    filter { eq("id", labelId); eq("user_id", userId) }
                }
            _userLabels.value = _userLabels.value.map {
                if (it.id == labelId) it.copy(name = name, color = color, icon = icon) else it
            }
            // Actualizar labels en conversaciones
            _conversations.value = _conversations.value.map { conv ->
                conv.copy(labels = conv.labels.map { 
                    if (it.id == labelId) it.copy(name = name, color = color, icon = icon) else it 
                })
            }
            Log.d(TAG, "✅ Etiqueta actualizada: $name")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando etiqueta: ${e.message}")
            false
        }
    }
    
    /** Eliminar etiqueta */
    suspend fun deleteLabel(labelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            // Eliminar asignaciones primero (cascade debería hacerlo pero por seguridad)
            SupabaseClient.database
                .from("chat_label_assignments")
                .delete { filter { eq("label_id", labelId); eq("user_id", userId) } }
            SupabaseClient.database
                .from("chat_labels")
                .delete { filter { eq("id", labelId); eq("user_id", userId) } }
            _userLabels.value = _userLabels.value.filter { it.id != labelId }
            // Remover de conversaciones
            _conversations.value = _conversations.value.map { conv ->
                conv.copy(labels = conv.labels.filter { it.id != labelId })
            }
            Log.d(TAG, "✅ Etiqueta eliminada: $labelId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando etiqueta: ${e.message}")
            false
        }
    }
    
    /** Asignar etiqueta a una conversación */
    suspend fun assignLabel(conversationId: String, labelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("chat_label_assignments")
                .upsert(buildJsonObject {
                    put("user_id", userId)
                    put("conversation_id", conversationId)
                    put("label_id", labelId)
                })
            // Actualizar localmente
            val label = _userLabels.value.find { it.id == labelId }
            if (label != null) {
                _conversations.value = _conversations.value.map { conv ->
                    if (conv.id == conversationId && conv.labels.none { it.id == labelId }) {
                        conv.copy(labels = conv.labels + label)
                    } else conv
                }
            }
            Log.d(TAG, "✅ Etiqueta asignada: $labelId → $conversationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error asignando etiqueta: ${e.message}")
            false
        }
    }
    
    /** Remover etiqueta de una conversación */
    suspend fun removeLabel(conversationId: String, labelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext false
            SupabaseClient.database
                .from("chat_label_assignments")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("conversation_id", conversationId)
                        eq("label_id", labelId)
                    }
                }
            // Actualizar localmente
            _conversations.value = _conversations.value.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(labels = conv.labels.filter { it.id != labelId })
                } else conv
            }
            Log.d(TAG, "✅ Etiqueta removida: $labelId de $conversationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removiendo etiqueta: ${e.message}")
            false
        }
    }
    
    /** Obtener etiquetas asignadas a una conversación */
    suspend fun getLabelsForConversation(conversationId: String): List<ChatLabel> = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            val assignments = SupabaseClient.database
                .from("chat_label_assignments")
                .select { filter { eq("user_id", userId); eq("conversation_id", conversationId) } }
                .decodeList<ChatLabelAssignmentDB>()
            
            val labelIds = assignments.map { it.labelId }
            _userLabels.value.filter { it.id in labelIds }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo etiquetas de conv: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun sendConsultMessage(
        sellerId: String,
        sellerUsername: String,
        postId: String,
        postTitle: String,
        message: String
    ): Boolean {
        Log.d(TAG, "=== ENVIANDO CONSULTA SOBRE PRODUCTO ===")
        Log.d(TAG, "Vendedor: $sellerUsername ($sellerId)")
        Log.d(TAG, "Producto: $postTitle ($postId)")
        
        try {
            // Obtener o crear conversación con el vendedor
            val conversationId = getOrCreateConversation(sellerId)
            if (conversationId == null) {
                Log.e(TAG, "No se pudo crear/obtener conversación con vendedor")
                return false
            }
            
            // Formatear mensaje de consulta con info del producto
            val formattedMessage = buildString {
                append("📦 *Consulta sobre producto*\n")
                append("━━━━━━━━━━━━━━━━━\n")
                append("🏷️ $postTitle\n")
                append("━━━━━━━━━━━━━━━━━\n\n")
                append(message)
            }
            
            // Enviar mensaje
            val sent = sendMessage(conversationId, formattedMessage)
            
            if (sent) {
                Log.d(TAG, "✅ Consulta enviada exitosamente")
            } else {
                Log.e(TAG, "❌ Error enviando consulta")
            }
            
            return sent
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en sendConsultMessage: ${e.message}", e)
            return false
        }
    }
}
