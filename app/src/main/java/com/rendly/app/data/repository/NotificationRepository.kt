package com.rendly.app.data.repository

import android.content.Context
import android.util.Log
import com.rendly.app.data.model.Notification
import com.rendly.app.util.SoundManager
import com.rendly.app.data.model.NotificationDB
import com.rendly.app.data.model.NotificationType
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import com.rendly.app.data.cache.BadgeCountCache

object NotificationRepository {
    
    private const val TAG = "NotificationRepository"
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var appContext: Context? = null
    
    fun init(context: Context) {
        appContext = context.applicationContext
        SoundManager.init(context)
        // Cargar conteo cacheado inmediatamente para mostrar badge sin delay
        val cachedCount = BadgeCountCache.getNotificationCount()
        _unreadCount.value = cachedCount
        Log.d(TAG, "✅ Init with cached notification count: $cachedCount")
    }
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _newNotification = MutableStateFlow<Notification?>(null)
    val newNotification: StateFlow<Notification?> = _newNotification.asStateFlow()
    
    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var isSubscribed = false
    
    private suspend fun getCurrentUserId(): String? {
        return SupabaseClient.auth.currentUserOrNull()?.id
    }
    
    private suspend fun getCurrentUserProfile(): Usuario? {
        val userId = getCurrentUserId() ?: return null
        return try {
            SupabaseClient.database
                .from("usuarios")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<Usuario>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }
    
    // Cargar notificaciones del usuario
    suspend fun loadNotifications() = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val userId = getCurrentUserId() ?: return@withContext
            
            val notificationsList = SupabaseClient.database
                .from("notifications")
                .select {
                    filter { eq("recipient_id", userId) }
                }
                .decodeList<NotificationDB>()
                .sortedByDescending { it.createdAt }
            
            _notifications.value = notificationsList.map { Notification.fromDB(it) }
            _unreadCount.value = notificationsList.count { !it.isRead }
            BadgeCountCache.setNotificationCount(_unreadCount.value)
            
            Log.d(TAG, "Loaded ${notificationsList.size} notifications, ${_unreadCount.value} unread")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notifications", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    // Suscribirse a notificaciones en tiempo real
    suspend fun subscribeToRealtime() {
        if (isSubscribed) return
        
        val userId = getCurrentUserId() ?: return
        
        try {
            realtimeChannel = SupabaseClient.client.channel("notifications:$userId")
            
            realtimeChannel?.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = "notifications"
                filter = "recipient_id=eq.$userId"
            }?.onEach { _ ->
                // Cuando llega una nueva notificación, recargar la lista
                Log.d(TAG, "🔔 Nueva notificación detectada, recargando...")
                loadNotifications()
                // Reproducir sonido de notificación
                SoundManager.playNotificationSound()
            }?.launchIn(scope)
            
            realtimeChannel?.subscribe()
            isSubscribed = true
            
            Log.d(TAG, "✅ Suscrito a notificaciones en tiempo real")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to realtime", e)
        }
    }
    
    // Desuscribirse
    suspend fun unsubscribeFromRealtime() {
        try {
            realtimeChannel?.unsubscribe()
            isSubscribed = false
            Log.d(TAG, "Desuscrito de notificaciones en tiempo real")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing", e)
        }
    }
    
    // Limpiar notificación nueva (después de mostrar toast)
    fun clearNewNotification() {
        _newNotification.value = null
    }
    
    // Crear notificación de LIKE
    suspend fun createLikeNotification(
        recipientId: String,
        postId: String,
        postImage: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val senderId = getCurrentUserId() ?: return@withContext Result.failure(Exception("No auth"))
            
            // No notificar si el usuario se da like a sí mismo
            if (senderId == recipientId) {
                return@withContext Result.success(Unit)
            }
            
            val profile = getCurrentUserProfile()
            
            SupabaseClient.database.from("notifications")
                .insert(buildJsonObject {
                    put("recipient_id", recipientId)
                    put("sender_id", senderId)
                    put("sender_username", profile?.username ?: "usuario")
                    put("sender_avatar", profile?.avatarUrl)
                    put("type", NotificationType.LIKE.value)
                    put("post_id", postId)
                    put("post_image", postImage)
                    put("created_at", Instant.now().toString())
                })
            
            Log.d(TAG, "✅ Notificación de LIKE creada para $recipientId")
            
            // FCM push phone-to-phone
            sendFCMPush(
                recipientId = recipientId,
                title = "Nuevo like",
                body = "${profile?.username ?: "Alguien"} le dio like a tu publicación",
                data = mapOf(
                    "type" to "like",
                    "sender_id" to senderId,
                    "sender_name" to (profile?.username ?: ""),
                    "target_id" to postId
                )
            )
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating like notification", e)
            Result.failure(e)
        }
    }
    
    // Crear notificación de SAVE (guardado)
    suspend fun createSaveNotification(
        recipientId: String,
        postId: String,
        postImage: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val senderId = getCurrentUserId() ?: return@withContext Result.failure(Exception("No auth"))
            
            // No notificar si el usuario guarda su propio post
            if (senderId == recipientId) {
                return@withContext Result.success(Unit)
            }
            
            val profile = getCurrentUserProfile()
            
            SupabaseClient.database.from("notifications")
                .insert(buildJsonObject {
                    put("recipient_id", recipientId)
                    put("sender_id", senderId)
                    put("sender_username", profile?.username ?: "usuario")
                    put("sender_avatar", profile?.avatarUrl)
                    put("type", NotificationType.SAVE.value)
                    put("post_id", postId)
                    put("post_image", postImage)
                    put("created_at", Instant.now().toString())
                })
            
            Log.d(TAG, "✅ Notificación de SAVE creada para $recipientId")
            
            // FCM push phone-to-phone
            sendFCMPush(
                recipientId = recipientId,
                title = "Guardaron tu publicación",
                body = "${profile?.username ?: "Alguien"} guardó tu publicación",
                data = mapOf(
                    "type" to "save",
                    "sender_id" to senderId,
                    "sender_name" to (profile?.username ?: ""),
                    "target_id" to postId
                )
            )
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating save notification", e)
            Result.failure(e)
        }
    }
    
    // Crear notificación de COMMENT (comentario/opinión)
    suspend fun createCommentNotification(
        recipientId: String,
        postId: String,
        postImage: String?,
        commentText: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val senderId = getCurrentUserId() ?: return@withContext Result.failure(Exception("No auth"))
            
            // No notificar si el usuario comenta su propio post
            if (senderId == recipientId) {
                return@withContext Result.success(Unit)
            }
            
            val profile = getCurrentUserProfile()
            
            SupabaseClient.database.from("notifications")
                .insert(buildJsonObject {
                    put("recipient_id", recipientId)
                    put("sender_id", senderId)
                    put("sender_username", profile?.username ?: "usuario")
                    put("sender_avatar", profile?.avatarUrl)
                    put("type", NotificationType.COMMENT.value)
                    put("post_id", postId)
                    put("post_image", postImage)
                    put("message", commentText.take(100))
                    put("created_at", Instant.now().toString())
                })
            
            Log.d(TAG, "✅ Notificación de COMMENT creada para $recipientId")
            
            // FCM push phone-to-phone
            sendFCMPush(
                recipientId = recipientId,
                title = "Nueva opinión",
                body = "${profile?.username ?: "Alguien"} comentó: ${commentText.take(80)}",
                data = mapOf(
                    "type" to "comment",
                    "sender_id" to senderId,
                    "sender_name" to (profile?.username ?: ""),
                    "target_id" to postId,
                    "body" to commentText.take(80)
                )
            )
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating comment notification", e)
            Result.failure(e)
        }
    }
    
    // Crear notificación de mensaje
    suspend fun createMessageNotification(
        recipientId: String,
        senderUsername: String,
        messagePreview: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUserProfile() ?: return@withContext Result.failure(Exception("No user"))
            
            // No notificar a uno mismo
            if (currentUser.userId == recipientId) {
                return@withContext Result.success(Unit)
            }
            
            SupabaseClient.database
                .from("notifications")
                .insert(buildJsonObject {
                    put("recipient_id", recipientId)
                    put("sender_id", currentUser.userId)
                    put("sender_username", currentUser.username)
                    put("sender_avatar", currentUser.avatarUrl)
                    put("type", "message")
                    put("message", "$senderUsername te envió un mensaje: $messagePreview")
                    put("created_at", Instant.now().toString())
                })
            
            Log.d(TAG, "✅ Notificación de mensaje creada para $recipientId")
            
            // FCM push phone-to-phone (para cuando la app está cerrada)
            sendFCMPush(
                recipientId = recipientId,
                title = senderUsername,
                body = messagePreview.take(100),
                data = mapOf(
                    "type" to "message",
                    "sender_id" to currentUser.userId,
                    "sender_name" to senderUsername,
                    "sender_avatar" to (currentUser.avatarUrl ?: "")
                )
            )
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating message notification", e)
            Result.failure(e)
        }
    }
    
    // Marcar notificación como leída
    suspend fun markAsRead(notificationId: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database.from("notifications")
                .update(buildJsonObject {
                    put("is_read", true)
                }) {
                    filter { eq("id", notificationId) }
                }
            
            // Actualizar estado local
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            _unreadCount.value = _notifications.value.count { !it.isRead }
            BadgeCountCache.setNotificationCount(_unreadCount.value)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read", e)
        }
    }
    
    // Marcar todas como leídas
    suspend fun markAllAsRead() = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: return@withContext
            
            SupabaseClient.database.from("notifications")
                .update(buildJsonObject {
                    put("is_read", true)
                }) {
                    filter { 
                        eq("recipient_id", userId)
                        eq("is_read", false)
                    }
                }
            
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            _unreadCount.value = 0
            BadgeCountCache.setNotificationCount(0)
            
            Log.d(TAG, "Todas las notificaciones marcadas como leídas")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all as read", e)
        }
    }
    
    // Eliminar notificación
    suspend fun deleteNotification(notificationId: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database.from("notifications")
                .delete {
                    filter { eq("id", notificationId) }
                }
            
            _notifications.value = _notifications.value.filter { it.id != notificationId }
            _unreadCount.value = _notifications.value.count { !it.isRead }
            BadgeCountCache.setNotificationCount(_unreadCount.value)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification", e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // NOTIFICACIONES DE SOLICITUD DE CLIENTE
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Notificar al vendedor que alguien quiere ser su cliente
     */
    suspend fun createClientRequestNotification(
        sellerId: String,
        requesterUsername: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUserProfile() ?: return@withContext Result.failure(Exception("No user"))
            
            // No notificar a uno mismo
            if (currentUser.userId == sellerId) {
                return@withContext Result.success(Unit)
            }
            
            SupabaseClient.database.from("notifications")
                .insert(buildJsonObject {
                    put("recipient_id", sellerId)
                    put("sender_id", currentUser.userId)
                    put("sender_username", currentUser.username)
                    put("sender_avatar", currentUser.avatarUrl)
                    put("type", NotificationType.CLIENT_REQUEST.value)
                    put("message", "@$requesterUsername quiere ser tu cliente")
                    put("created_at", Instant.now().toString())
                })
            
            Log.d(TAG, "✅ Notificación CLIENT_REQUEST creada para vendedor $sellerId")
            
            sendFCMPush(
                recipientId = sellerId,
                title = "Nueva solicitud de cliente",
                body = "@$requesterUsername quiere ser tu cliente",
                data = mapOf(
                    "type" to "client_request",
                    "sender_id" to currentUser.userId,
                    "sender_name" to currentUser.username
                )
            )
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating client request notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Notificar al solicitante que su solicitud está pendiente
     */
    suspend fun createClientPendingNotification(
        requesterId: String,
        sellerUsername: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUserProfile() ?: return@withContext Result.failure(Exception("No user"))
            
            SupabaseClient.database.from("notifications")
                .insert(buildJsonObject {
                    put("recipient_id", requesterId)
                    put("sender_id", currentUser.userId)
                    put("sender_username", currentUser.username)
                    put("sender_avatar", currentUser.avatarUrl)
                    put("type", NotificationType.CLIENT_PENDING.value)
                    put("message", "Tu solicitud para ser cliente de @$sellerUsername está pendiente")
                    put("created_at", Instant.now().toString())
                })
            
            Log.d(TAG, "✅ Notificación CLIENT_PENDING creada para solicitante $requesterId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating client pending notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Notificar al solicitante que fue aceptado como cliente
     * ACTUALIZA la notificación PENDING existente en lugar de crear una nueva
     */
    suspend fun createClientAcceptedNotification(
        requesterId: String,
        sellerUsername: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUserProfile() ?: return@withContext Result.failure(Exception("No user"))
            
            // Buscar la notificación PENDING existente para este usuario
            val existingNotifications = SupabaseClient.database
                .from("notifications")
                .select {
                    filter {
                        eq("recipient_id", requesterId)
                        eq("sender_id", currentUser.userId)
                        eq("type", NotificationType.CLIENT_PENDING.value)
                    }
                }
                .decodeList<NotificationDB>()
            
            if (existingNotifications.isNotEmpty()) {
                // Actualizar la notificación existente a CLIENT_ACCEPTED
                val notificationId = existingNotifications.first().id
                SupabaseClient.database.from("notifications")
                    .update(buildJsonObject {
                        put("type", NotificationType.CLIENT_ACCEPTED.value)
                        put("message", "te aceptó como cliente")
                        put("is_read", false) // Marcar como no leída para que el usuario la vea
                        put("created_at", Instant.now().toString()) // Actualizar timestamp
                    }) {
                        filter {
                            eq("id", notificationId)
                        }
                    }
                Log.d(TAG, "✅ Notificación PENDING actualizada a ACCEPTED para $requesterId")
            } else {
                // Si no existe PENDING (caso raro), crear nueva
                SupabaseClient.database.from("notifications")
                    .insert(buildJsonObject {
                        put("recipient_id", requesterId)
                        put("sender_id", currentUser.userId)
                        put("sender_username", currentUser.username)
                        put("sender_avatar", currentUser.avatarUrl)
                        put("type", NotificationType.CLIENT_ACCEPTED.value)
                        put("message", "te aceptó como cliente")
                        put("created_at", Instant.now().toString())
                    })
                Log.d(TAG, "✅ Notificación CLIENT_ACCEPTED creada (sin PENDING previo) para $requesterId")
            }
            
            sendFCMPush(
                recipientId = requesterId,
                title = "Solicitud aceptada",
                body = "@$sellerUsername te aceptó como cliente",
                data = mapOf(
                    "type" to "client_accepted",
                    "sender_id" to currentUser.userId,
                    "sender_name" to currentUser.username
                )
            )
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating/creating client accepted notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Notificar al solicitante que fue rechazado como cliente
     */
    suspend fun createClientRejectedNotification(
        requesterId: String,
        sellerUsername: String,
        reason: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUserProfile() ?: return@withContext Result.failure(Exception("No user"))
            
            // Mensaje base sin @ del solicitante, solo vendedor
            val message = "no aceptó tu solicitud de cliente"
            
            SupabaseClient.database.from("notifications")
                .insert(buildJsonObject {
                    put("recipient_id", requesterId)
                    put("sender_id", currentUser.userId)
                    put("sender_username", currentUser.username)
                    put("sender_avatar", currentUser.avatarUrl)
                    put("type", NotificationType.CLIENT_REJECTED.value)
                    put("message", message)
                    put("extra_data", reason) // Guardar motivo en campo separado
                    put("created_at", Instant.now().toString())
                })
            
            Log.d(TAG, "✅ Notificación CLIENT_REJECTED creada para $requesterId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating client rejected notification", e)
            Result.failure(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // FCM PUSH - Enviar push notifications via Supabase Edge Function
    // Para comunicación phone-to-phone en tiempo real
    // ═══════════════════════════════════════════════════════════════════════════════
    
    @kotlinx.serialization.Serializable
    private data class FcmTokenRow(
        val token: String = ""
    )
    
    @kotlinx.serialization.Serializable
    private data class FcmPushRequest(
        val tokens: List<String>,
        val title: String,
        val body: String,
        val data: Map<String, String> = emptyMap()
    )
    
    /**
     * Obtiene los tokens FCM activos de un usuario.
     */
    suspend fun getUserFCMTokens(userId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val tokens = SupabaseClient.database
                .from("fcm_tokens")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("token")) {
                    filter {
                        eq("user_id", userId)
                        eq("is_active", true)
                    }
                }
                .decodeList<FcmTokenRow>()
            
            val tokenList = tokens.map { it.token }
            Log.d(TAG, "📱 Tokens FCM para $userId: ${tokenList.size}")
            tokenList
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo tokens FCM: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Envía push notification via Supabase Edge Function (FCM v1 API).
     * Esto permite que las notificaciones lleguen al otro teléfono incluso
     * cuando la app está cerrada.
     */
    suspend fun sendFCMPush(
        recipientId: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        try {
            val tokens = getUserFCMTokens(recipientId)
            if (tokens.isEmpty()) {
                Log.d(TAG, "⚠️ No hay tokens FCM para usuario $recipientId")
                return@withContext
            }
            
            Log.d(TAG, "📤 Enviando FCM push a $recipientId (${tokens.size} tokens)")
            
            val request = FcmPushRequest(
                tokens = tokens,
                title = title,
                body = body,
                data = data
            )
            
            val response = SupabaseClient.client.functions.invoke("send-fcm-v1", body = request)
            val responseBody = response.body<String>()
            Log.d(TAG, "✅ FCM push response [${response.status}]: $responseBody")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando FCM push: ${e.message}", e)
        }
    }
}
