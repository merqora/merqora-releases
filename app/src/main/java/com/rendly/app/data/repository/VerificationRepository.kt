package com.rendly.app.data.repository

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.SupabaseClient
import java.text.SimpleDateFormat
import java.util.*

object VerificationRepository {
    private const val TAG = "VerificationRepository"
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Estado de verificación del otro usuario (para tiempo real)
    private val _otherUserVerified = MutableStateFlow(false)
    val otherUserVerified: StateFlow<Boolean> = _otherUserVerified.asStateFlow()
    
    private var verificationChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var subscribedUserId: String? = null

    @Serializable
    data class VerificationRequest(
        val id: String? = null,
        val user_id: String,
        val verification_type: String = "personal",
        val status: String = "pending",
        val username: String? = null,
        val display_name: String? = null,
        val email: String? = null,
        val avatar_url: String? = null,
        val bio: String? = null,
        val website_url: String? = null,
        val followers_count: Int = 0,
        val following_count: Int = 0,
        val posts_count: Int = 0,
        val full_legal_name: String? = null,
        val reason_for_verification: String? = null,
        val notable_presence: String? = null,
        val rejection_reason: String? = null,
        val created_at: String? = null,
        val updated_at: String? = null
    )

    @Serializable
    data class UserVerificationStatus(
        val is_verified: Boolean = false,
        val verified_at: String? = null,
        val verification_type: String? = null
    )

    // Datos de la cuenta para el formulario de verificación
    data class AccountDataForVerification(
        val username: String?,
        val email: String?,
        val phone: String?,
        val followersCount: Int,
        val clientsCount: Int,
        val postsCount: Int,
        val reputation: Int,
        val memberSince: String?,
        val hasStore: Boolean,
        val salesCount: Int
    )

    // Obtener estado de verificación del usuario actual
    suspend fun getVerificationStatus(userId: String): UserVerificationStatus? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("usuarios")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("is_verified", "verified_at", "verification_type")) {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<UserVerificationStatus>()
            
            Log.d(TAG, "✅ Estado de verificación obtenido: ${result?.is_verified}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo estado de verificación: ${e.message}")
            null
        }
    }

    // Obtener solicitud de verificación pendiente
    suspend fun getPendingRequest(userId: String): VerificationRequest? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("verification_requests")
                .select {
                    filter { 
                        eq("user_id", userId)
                        eq("status", "pending")
                    }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<VerificationRequest>()
            
            Log.d(TAG, "✅ Solicitud pendiente: ${result?.id}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo solicitud pendiente: ${e.message}")
            null
        }
    }

    // Obtener última solicitud (cualquier estado)
    suspend fun getLatestRequest(userId: String): VerificationRequest? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("verification_requests")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<VerificationRequest>()
            
            Log.d(TAG, "✅ Última solicitud: ${result?.status}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo última solicitud: ${e.message}")
            null
        }
    }

    // Crear nueva solicitud de verificación
    suspend fun submitVerificationRequest(
        userId: String,
        verificationType: String,
        fullLegalName: String,
        reasonForVerification: String,
        notablePresence: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Obtener datos del perfil del usuario directamente de Supabase
            val userResult = SupabaseClient.client
                .from("usuarios")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<Usuario>()
            
            // Contar seguidores
            val followersCount = try {
                SupabaseClient.client
                    .from("followers")
                    .select {
                        filter { eq("followed_id", userId) }
                    }
                    .decodeList<JsonObject>().size
            } catch (e: Exception) { 0 }
            
            // Contar siguiendo
            val followingCount = try {
                SupabaseClient.client
                    .from("followers")
                    .select {
                        filter { eq("follower_id", userId) }
                    }
                    .decodeList<JsonObject>().size
            } catch (e: Exception) { 0 }
            
            // Contar posts
            val postsCount = try {
                SupabaseClient.client
                    .from("posts")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<JsonObject>().size
            } catch (e: Exception) { 0 }
            
            val request = buildJsonObject {
                put("user_id", JsonPrimitive(userId))
                put("verification_type", JsonPrimitive(verificationType))
                put("status", JsonPrimitive("pending"))
                put("username", JsonPrimitive(userResult?.username ?: ""))
                put("display_name", JsonPrimitive(userResult?.nombre ?: userResult?.nombreTienda ?: ""))
                put("email", JsonPrimitive(SupabaseClient.auth.currentUserOrNull()?.email ?: ""))
                put("avatar_url", JsonPrimitive(userResult?.avatarUrl ?: ""))
                put("bio", JsonPrimitive(userResult?.descripcion ?: ""))
                put("website_url", JsonPrimitive(""))
                put("followers_count", JsonPrimitive(followersCount))
                put("following_count", JsonPrimitive(followingCount))
                put("posts_count", JsonPrimitive(postsCount))
                put("full_legal_name", JsonPrimitive(fullLegalName))
                put("reason_for_verification", JsonPrimitive(reasonForVerification))
                notablePresence?.let { put("notable_presence", JsonPrimitive(it)) }
            }

            SupabaseClient.client
                .from("verification_requests")
                .insert(request)

            Log.d(TAG, "✅ Solicitud de verificación enviada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error enviando solicitud: ${e.message}")
            false
        }
    }

    // Cancelar solicitud pendiente
    suspend fun cancelPendingRequest(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("verification_requests")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("status", "pending")
                    }
                }

            Log.d(TAG, "✅ Solicitud cancelada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cancelando solicitud: ${e.message}")
            false
        }
    }

    // Obtener datos de la cuenta para el formulario de verificación
    suspend fun getAccountDataForVerification(): AccountDataForVerification? = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext null
            val email = SupabaseClient.auth.currentUserOrNull()?.email
            
            // Obtener datos del usuario
            val userResult = SupabaseClient.client
                .from("usuarios")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<Usuario>()
            
            // Contar seguidores
            val followersCount = try {
                SupabaseClient.client
                    .from("followers")
                    .select {
                        filter { eq("followed_id", userId) }
                    }
                    .decodeList<JsonObject>().size
            } catch (e: Exception) { 0 }
            
            // Contar posts
            val postsCount = try {
                SupabaseClient.client
                    .from("posts")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeList<JsonObject>().size
            } catch (e: Exception) { 0 }
            
            // Calcular "miembro desde" desde created_at del usuario
            val memberSince = try {
                userResult?.createdAt?.let { dateStr ->
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    val outputFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                    val date = inputFormat.parse(dateStr.take(19))
                    date?.let { outputFormat.format(it).capitalize() } ?: "---"
                } ?: "---"
            } catch (e: Exception) { "---" }
            
            AccountDataForVerification(
                username = userResult?.username,
                email = email,
                phone = userResult?.whatsapp,
                followersCount = followersCount,
                clientsCount = 0, // TODO: Implementar cuando exista el campo
                postsCount = postsCount,
                reputation = 100, // TODO: Implementar sistema de reputación
                memberSince = memberSince,
                hasStore = userResult?.tieneTienda ?: false,
                salesCount = 0 // TODO: Implementar conteo de ventas
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo datos de cuenta: ${e.message}")
            null
        }
    }

    // Verificar si un usuario está verificado (para mostrar badge)
    suspend fun isUserVerified(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, ">>> Consultando is_verified para usuario: $userId")
            val result = SupabaseClient.client
                .from("usuarios")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("is_verified", "verified_at")) {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<UserVerificationStatus>()
            
            Log.d(TAG, ">>> Resultado de is_verified: ${result?.is_verified}, verified_at: ${result?.verified_at}")
            result?.is_verified ?: false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando usuario: ${e.message}", e)
            false
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
    
    // Suscribirse a cambios de verificación de un usuario en tiempo real
    suspend fun subscribeToVerification(userId: String) {
        // Si ya estamos suscritos al mismo usuario, no hacer nada
        if (subscribedUserId == userId && verificationChannel != null) {
            Log.d(TAG, "Ya suscrito a verificación de $userId")
            return
        }
        
        // Limpiar suscripción anterior
        unsubscribeFromVerification()
        
        subscribedUserId = userId
        
        // Cargar estado inicial
        val initialStatus = isUserVerified(userId)
        _otherUserVerified.value = initialStatus
        Log.d(TAG, ">>> Estado inicial de verificación para $userId: $initialStatus")
        
        try {
            // Crear canal para escuchar cambios en la tabla usuarios
            val channelId = "verification-$userId-${System.currentTimeMillis()}"
            verificationChannel = SupabaseClient.client.realtime.channel(channelId)
            
            // Escuchar cambios en el usuario específico
            verificationChannel?.postgresChangeFlow<PostgresAction>(
                schema = "public"
            ) {
                table = "usuarios"
                filter = "user_id=eq.$userId"
            }?.onEach { action ->
                Log.d(TAG, ">>> Realtime verificación recibido: ${action::class.simpleName}")
                when (action) {
                    is PostgresAction.Update -> {
                        val record = action.record
                        val isVerified = record["is_verified"]?.jsonPrimitive?.boolean ?: false
                        Log.d(TAG, ">>> Usuario $userId is_verified actualizado a: $isVerified")
                        _otherUserVerified.value = isVerified
                    }
                    else -> {}
                }
            }?.launchIn(scope)
            
            verificationChannel?.subscribe()
            Log.d(TAG, "✅ Suscrito a cambios de verificación de $userId")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error suscribiendo a verificación: ${e.message}")
        }
    }
    
    // Cancelar suscripción
    fun unsubscribeFromVerification() {
        scope.launch {
            try {
                verificationChannel?.unsubscribe()
                verificationChannel = null
                subscribedUserId = null
                Log.d(TAG, "Desuscrito de verificación")
            } catch (e: Exception) {
                Log.e(TAG, "Error desuscribiendo: ${e.message}")
            }
        }
    }
}
