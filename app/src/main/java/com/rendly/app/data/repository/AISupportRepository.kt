package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.BuildConfig
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Repository for AI Support communication
 * 
 * Connects the Android app to the Python AI Support backend
 */
object AISupportRepository {
    
    private const val TAG = "AISupportRepository"
    
    // AI Support API base URL - loaded from BuildConfig (set in build.gradle.kts / gradle.properties)
    // For local dev: add AI_SUPPORT_URL=http://192.168.1.X:8000 to gradle.properties
    // For production: deploy to Railway/Render and use the public URL
    private val AI_API_BASE_URL = BuildConfig.AI_SUPPORT_URL
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private var currentSessionId: String? = null
    private var currentConversationId: String? = null
    
    /**
     * Get current session ID for realtime subscription
     */
    fun getCurrentSessionId(): String? = currentSessionId
    
    /**
     * Get current conversation ID for realtime subscription
     * This is the real UUID from support_conversations table
     */
    fun getCurrentConversationId(): String? = currentConversationId
    
    @Serializable
    data class SupportMessageRequest(
        val user_id: String,
        val message: String,
        val session_id: String? = null,
        val context: Map<String, String>? = null
    )
    
    @Serializable
    data class AnalysisDetails(
        val confidence_score: Int = 0,
        val detected_intent: String = "",
        val category: String = "",
        val clarity_score: Float = 0f,
        val completeness_score: Float = 0f,
        val is_aggressive: Boolean = false,
        val is_confused: Boolean = false,
        val matched_keywords: List<String> = emptyList()
    )
    
    @Serializable
    data class ActionButton(
        val id: String,
        val label: String,
        val action: String, // "navigate", "open_url", "call_function"
        val target: String,
        val icon: String? = null
    )
    
    @Serializable
    data class SupportMessageResponse(
        val message_id: String,
        val response_type: String, // "ai_response", "escalated", "blocked", "error"
        val content: String,
        val analysis: AnalysisDetails? = null,
        val session_id: String,
        val conversation_id: String? = null, // Real UUID for realtime subscription
        val timestamp: String = "",
        val escalated: Boolean = false,
        val escalation_reason: String? = null,
        val suggested_actions: List<String> = emptyList(),
        val action_buttons: List<ActionButton> = emptyList()
    )
    
    @Serializable
    data class FeedbackRequest(
        val message_id: String,
        val user_id: String,
        val helpful: Boolean,
        val feedback_text: String? = null
    )
    
    sealed class AIResponse {
        data class Success(val response: SupportMessageResponse) : AIResponse()
        data class Error(val message: String) : AIResponse()
        data class Offline(val fallbackResponse: String) : AIResponse()
    }
    
    /**
     * Send a message to the AI support system
     */
    suspend fun sendMessage(
        userId: String,
        message: String,
        context: Map<String, String>? = null
    ): AIResponse = withContext(Dispatchers.IO) {
        try {
            val request = SupportMessageRequest(
                user_id = userId,
                message = message,
                session_id = currentSessionId,
                context = context
            )
            
            val url = URL("$AI_API_BASE_URL/ai/support/message")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 60000
            }
            
            val requestBody = json.encodeToString(SupportMessageRequest.serializer(), request)
            Log.d(TAG, "Sending request: $requestBody")
            
            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray())
            }
            
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Response: $responseBody")
                
                val response = json.decodeFromString(SupportMessageResponse.serializer(), responseBody)
                currentSessionId = response.session_id
                currentConversationId = response.conversation_id
                
                AIResponse.Success(response)
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e(TAG, "Error response: $responseCode - $errorBody")
                AIResponse.Error("Error del servidor: $responseCode")
            }
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "❌ ConnectException to $AI_API_BASE_URL: ${e.message}", e)
            AIResponse.Offline(getFallbackResponse(message))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "❌ SocketTimeout to $AI_API_BASE_URL: ${e.message}", e)
            AIResponse.Offline(getFallbackResponse(message))
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "❌ UnknownHost $AI_API_BASE_URL: ${e.message}", e)
            AIResponse.Offline(getFallbackResponse(message))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception ${e.javaClass.simpleName}: ${e.message}", e)
            AIResponse.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Submit feedback on an AI response
     */
    suspend fun submitFeedback(
        messageId: String,
        userId: String,
        helpful: Boolean,
        feedbackText: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = FeedbackRequest(
                message_id = messageId,
                user_id = userId,
                helpful = helpful,
                feedback_text = feedbackText
            )
            
            val url = URL("$AI_API_BASE_URL/ai/support/feedback")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 10000
            }
            
            val requestBody = json.encodeToString(FeedbackRequest.serializer(), request)
            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray())
            }
            
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting feedback", e)
            false
        }
    }
    
    /**
     * Check if AI server is available
     */
    suspend fun isServerAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$AI_API_BASE_URL/health")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get a new session ID
     */
    fun startNewSession(): String {
        currentSessionId = UUID.randomUUID().toString()
        return currentSessionId!!
    }
    
    /**
     * End current session
     */
    fun endSession() {
        currentSessionId = null
        currentConversationId = null
    }
    
    /**
     * Send a message directly to Supabase (for escalated conversations)
     * This bypasses the AI and sends the user message directly to the conversation
     * so the human agent can see it in real-time
     */
    suspend fun sendDirectMessage(
        conversationId: String,
        content: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📤 Enviando mensaje directo a conversación escalada: $conversationId")
            
            val messageData = mapOf(
                "conversation_id" to conversationId,
                "role" to "user",
                "content" to content
            )
            
            SupabaseClient.client
                .from("support_messages")
                .insert(messageData)
            
            Log.d(TAG, "✓ Mensaje directo enviado exitosamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando mensaje directo: ${e.message}", e)
            false
        }
    }
    
    @Serializable
    data class SupportMessageRecord(
        val id: String = "",
        val conversation_id: String = "",
        val role: String = "",
        val content: String = "",
        val created_at: String = ""
    )
    
    /**
     * Guardar calificación del usuario en ai_feedback
     * Solo guarda si la calificación es positiva (4-5 estrellas) para aprendizaje
     */
    suspend fun saveUserRating(
        conversationId: String,
        userId: String,
        rating: Int,
        feedbackText: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val helpful = rating >= 4
            
            Log.d(TAG, "📝 Guardando calificación: $rating estrellas (${if (helpful) "útil" else "no útil"})")
            Log.d(TAG, "   → conversationId: $conversationId")
            
            // Obtener mensajes de la conversación con filtro directo en la query
            var userMessage = ""
            var agentResponse = ""
            var lastMessageId = ""
            
            if (conversationId.isNotEmpty()) {
                try {
                    val messages = SupabaseClient.client
                        .from("support_messages")
                        .select() {
                            filter { eq("conversation_id", conversationId) }
                            order("created_at", Order.ASCENDING)
                            limit(20)
                        }
                        .decodeList<SupportMessageRecord>()
                    
                    Log.d(TAG, "   → Encontrados ${messages.size} mensajes en la conversación")
                    
                    // Recorrer mensajes para obtener el último del usuario y la última respuesta
                    for (msg in messages) {
                        when (msg.role) {
                            "user" -> {
                                userMessage = msg.content
                                lastMessageId = msg.id
                            }
                            "assistant", "human_support" -> {
                                agentResponse = msg.content
                            }
                        }
                    }
                    
                    Log.d(TAG, "   → userMessage encontrado: ${userMessage.isNotEmpty()}")
                    Log.d(TAG, "   → agentResponse encontrado: ${agentResponse.isNotEmpty()}")
                    Log.d(TAG, "   → lastMessageId: $lastMessageId")
                    
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudieron obtener mensajes: ${e.message}")
                }
            }
            
            // Construir el objeto de feedback - SIEMPRE incluir los campos aunque estén vacíos
            val feedbackData = kotlinx.serialization.json.buildJsonObject {
                put("conversation_id", kotlinx.serialization.json.JsonPrimitive(conversationId))
                put("user_id", kotlinx.serialization.json.JsonPrimitive(userId))
                put("helpful", kotlinx.serialization.json.JsonPrimitive(helpful))
                put("rating", kotlinx.serialization.json.JsonPrimitive(rating))
                put("feedback_type", kotlinx.serialization.json.JsonPrimitive("resolution_feedback"))
                put("feedback_text", kotlinx.serialization.json.JsonPrimitive(feedbackText ?: "Calificación: $rating/5 estrellas"))
                // Siempre incluir estos campos para evitar nulls
                put("message_id", kotlinx.serialization.json.JsonPrimitive(lastMessageId.ifEmpty { null }))
                put("user_message", kotlinx.serialization.json.JsonPrimitive(userMessage.ifEmpty { null }))
                put("agent_response", kotlinx.serialization.json.JsonPrimitive(agentResponse.ifEmpty { null }))
            }
            
            SupabaseClient.client
                .from("ai_feedback")
                .insert(feedbackData)
            
            Log.d(TAG, "✅ Calificación guardada en Supabase: $rating⭐ (helpful=$helpful)")
            Log.d(TAG, "   → user_message: ${userMessage.take(50)}...")
            Log.d(TAG, "   → agent_response: ${agentResponse.take(50)}...")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando calificación: ${e.message}", e)
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Fallback responses when AI server is offline
     */
    private fun getFallbackResponse(message: String): String {
        val lowerMessage = message.lowercase()
        
        return when {
            // Saludos
            lowerMessage.contains("hola") || lowerMessage.contains("buenos") || 
            lowerMessage.contains("buenas") -> {
                """¡Hola! 👋 Bienvenido al soporte de Merqora.
                
En este momento estoy funcionando en modo offline. Puedo ayudarte con información básica sobre:

• 🛒 Compras y pedidos
• 💰 Pagos y reembolsos  
• 👤 Tu cuenta
• 📦 Envíos
• 🏪 Ventas
• 🔒 Seguridad

¿En qué puedo ayudarte?"""
            }
            
            // Pedidos
            lowerMessage.contains("pedido") || lowerMessage.contains("compra") || 
            lowerMessage.contains("orden") -> {
                """Para ver el estado de tus pedidos:

1. Ve a **Perfil** → **Historial de pedidos**
2. Selecciona el pedido que quieres revisar
3. Verás el estado actual y opciones disponibles

Si tienes un problema específico con tu pedido, por favor intenta más tarde cuando el servicio esté disponible."""
            }
            
            // Contraseña
            lowerMessage.contains("contraseña") || lowerMessage.contains("password") ||
            lowerMessage.contains("acceso") -> {
                """Para recuperar tu contraseña:

1. En la pantalla de login, toca **"¿Olvidaste tu contraseña?"**
2. Ingresa tu email registrado
3. Revisa tu bandeja (y spam) para el enlace de recuperación

Para cambiar tu contraseña actual:
**Perfil** → **Configuración** → **Seguridad** → **Cambiar contraseña**"""
            }
            
            // Pagos
            lowerMessage.contains("pago") || lowerMessage.contains("tarjeta") ||
            lowerMessage.contains("reembolso") -> {
                """Para gestionar métodos de pago:
**Perfil** → **Métodos de pago**

Los reembolsos tardan 3-10 días hábiles dependiendo del método de pago original.

Para problemas específicos de pago, intenta más tarde cuando el servicio esté disponible."""
            }
            
            // Ventas
            lowerMessage.contains("vender") || lowerMessage.contains("public") ||
            lowerMessage.contains("producto") -> {
                """Para vender en Merqora:

1. Toca el botón **"+"** en la barra inferior
2. Selecciona **"Publicación"**
3. Sube fotos de tu producto
4. Completa los detalles (título, precio, descripción)
5. Toca **"Publicar"**

La comisión por venta es del 10%."""
            }
            
            // Default
            else -> {
                """Gracias por contactarnos.

En este momento el servicio de IA está funcionando en modo limitado. Para obtener ayuda más detallada:

• Revisa el **Centro de ayuda** para preguntas frecuentes
• Explora **Perfil** → **Configuración** para opciones
• Intenta de nuevo más tarde

¡Disculpa las molestias!"""
            }
        }
    }
}
