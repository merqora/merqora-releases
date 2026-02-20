package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Modelo de notificación del sistema
 */
@Serializable
data class SystemNotification(
    val id: String,
    val user_id: String,
    val type: String, // CONFIRMATION_REMINDER, AUTO_COMPLETED, REPUTATION_PENALTY
    val title: String,
    val message: String,
    val data: JsonObject? = null,
    val read: Boolean = false,
    val created_at: String
)

/**
 * Repositorio para manejar notificaciones del sistema en tiempo real.
 * Diseñado para ser escalable con miles de usuarios simultáneos.
 * 
 * Características:
 * - Suscripción Realtime a nuevas notificaciones
 * - Cache local de notificaciones no leídas
 * - Marcar como leídas en batch
 * - Sin timers locales (todo el procesamiento es server-side)
 */
object SystemNotificationRepository {
    private const val TAG = "SystemNotificationRepo"
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Estado de notificaciones
    private val _notifications = MutableStateFlow<List<SystemNotification>>(emptyList())
    val notifications: StateFlow<List<SystemNotification>> = _notifications.asStateFlow()
    
    // Contador de no leídas
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    // Usuario actual
    private var currentUserId: String? = null
    
    
    /**
     * Inicializa el repositorio y comienza a escuchar notificaciones
     */
    fun initialize(userId: String) {
        if (currentUserId == userId) return
        
        currentUserId = userId
        Log.d(TAG, "Initializing for user: $userId")
        
        scope.launch {
            try {
                loadNotifications()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing: ${e.message}", e)
            }
        }
    }
    
    /**
     * Carga las notificaciones no leídas del usuario
     */
    private suspend fun loadNotifications() {
        val userId = currentUserId ?: return
        
        try {
            val result = SupabaseClient.database.from("system_notifications")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("read", false)
                    }
                    order("created_at", Order.DESCENDING)
                    limit(50)
                }
                .decodeList<SystemNotification>()
            
            _notifications.value = result
            _unreadCount.value = result.size
            
            Log.d(TAG, "Loaded ${result.size} unread notifications")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notifications: ${e.message}", e)
        }
    }
    
    
    /**
     * Marca notificaciones como leídas
     */
    suspend fun markAsRead(notificationIds: List<String>) = withContext(Dispatchers.IO) {
        if (notificationIds.isEmpty()) return@withContext
        
        try {
            // Actualizar en Supabase
            for (id in notificationIds) {
                SupabaseClient.database.from("system_notifications")
                    .update(buildJsonObject {
                        put("read", true)
                    }) {
                        filter { eq("id", id) }
                    }
            }
            
            // Actualizar estado local
            val updatedList = _notifications.value.filterNot { notificationIds.contains(it.id) }
            _notifications.value = updatedList
            _unreadCount.value = updatedList.size
            
            Log.d(TAG, "Marked ${notificationIds.size} notifications as read")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as read: ${e.message}", e)
        }
    }
    
    /**
     * Marca todas las notificaciones como leídas
     */
    suspend fun markAllAsRead() {
        val ids = _notifications.value.map { it.id }
        markAsRead(ids)
    }
    
    /**
     * Refresca las notificaciones
     */
    suspend fun refresh() {
        loadNotifications()
    }
    
    /**
     * Limpia el repositorio al cerrar sesión
     */
    fun cleanup() {
        currentUserId = null
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}
