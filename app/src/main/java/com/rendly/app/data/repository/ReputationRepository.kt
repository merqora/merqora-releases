package com.rendly.app.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
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
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Repositorio para gestionar la reputación de usuarios con Supabase Realtime
 * La reputación se actualiza automáticamente cuando:
 * - Se completa un handshake: +2% a +5%
 * - Se cancela en WAITING: -2%
 * - Se cancela en ACCEPTED: -5%
 */
object ReputationRepository {
    private const val TAG = "ReputationRepository"
    
    private lateinit var supabase: SupabaseClient
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }
    
    // Reputación del usuario actual (0-100)
    private val _currentUserReputation = MutableStateFlow(50.0)
    val currentUserReputation: StateFlow<Double> = _currentUserReputation.asStateFlow()
    
    // Reputación del otro usuario en el chat actual
    private val _otherUserReputation = MutableStateFlow(50.0)
    val otherUserReputation: StateFlow<Double> = _otherUserReputation.asStateFlow()
    
    // Canal de Realtime
    private var realtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var currentUserId: String? = null
    private var watchedOtherUserId: String? = null
    
    fun initialize(client: SupabaseClient) {
        supabase = client
        Log.d(TAG, "ReputationRepository initialized")
    }
    
    /**
     * Cargar reputación inicial de un usuario
     */
    suspend fun loadReputation(userId: String): Double {
        return try {
            val result = supabase.postgrest["usuarios"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserReputationResponse>()
            
            val reputation = result?.reputationScore ?: 50.0
            Log.d(TAG, "Loaded reputation for $userId: $reputation")
            reputation
        } catch (e: Exception) {
            Log.e(TAG, "Error loading reputation: ${e.message}")
            50.0
        }
    }
    
    /**
     * Suscribirse a cambios de reputación en tiempo real
     */
    suspend fun subscribeToReputation(currentUserId: String, otherUserId: String) {
        this.currentUserId = currentUserId
        this.watchedOtherUserId = otherUserId
        
        try {
            // Cargar reputaciones iniciales
            _currentUserReputation.value = loadReputation(currentUserId)
            _otherUserReputation.value = loadReputation(otherUserId)
            
            // Crear canal de Realtime para escuchar cambios en usuarios
            val channelId = "reputation_${currentUserId}_$otherUserId"
            realtimeChannel?.let {
                try {
                    supabase.realtime.removeChannel(it)
                } catch (e: Exception) {
                    Log.w(TAG, "Error removing old channel: ${e.message}")
                }
            }
            
            realtimeChannel = supabase.realtime.channel(channelId)
            
            // Escuchar cambios en la tabla usuarios
            val changesFlow = realtimeChannel?.postgresChangeFlow<PostgresAction>(
                schema = "public"
            ) {
                table = "usuarios"
            }
            
            // Colectar eventos
            scope.launch {
                changesFlow?.collect { action ->
                    when (action) {
                        is PostgresAction.Update -> {
                            try {
                                val userData = json.decodeFromString<UserReputationResponse>(action.record.toString())
                                
                                // Actualizar si es el usuario actual o el otro usuario
                                when (userData.userId) {
                                    currentUserId -> {
                                        val newRep = userData.reputationScore ?: 50.0
                                        Log.d(TAG, "Current user reputation updated: $newRep")
                                        _currentUserReputation.value = newRep
                                    }
                                    otherUserId -> {
                                        val newRep = userData.reputationScore ?: 50.0
                                        Log.d(TAG, "Other user reputation updated: $newRep")
                                        _otherUserReputation.value = newRep
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing user update: ${e.message}")
                            }
                        }
                        else -> {}
                    }
                }
            }
            
            realtimeChannel?.subscribe()
            Log.d(TAG, "Subscribed to reputation changes for $currentUserId and $otherUserId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to reputation: ${e.message}")
        }
    }
    
    /**
     * Forzar recarga de reputación (útil después de completar handshake)
     */
    suspend fun refreshReputation() {
        currentUserId?.let { 
            _currentUserReputation.value = loadReputation(it)
        }
        watchedOtherUserId?.let {
            _otherUserReputation.value = loadReputation(it)
        }
    }
    
    /**
     * Incrementar reputación del usuario actual (handshake completado)
     * @return El cambio aplicado (+3 a +4 random)
     */
    suspend fun incrementReputation(): Int {
        val userId = currentUserId
        if (userId == null) {
            Log.e(TAG, "incrementReputation: currentUserId is NULL!")
            return 0
        }
        
        val change = (3..4).random()
        val currentRep = _currentUserReputation.value
        val newRep = (currentRep + change).coerceIn(0.0, 100.0)
        
        Log.d(TAG, "incrementReputation: userId=$userId, currentRep=$currentRep, change=+$change, newRep=$newRep")
        
        return try {
            val result = supabase.postgrest["usuarios"]
                .update({
                    set("reputation_score", newRep)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            Log.d(TAG, "incrementReputation: Supabase update completed")
            _currentUserReputation.value = newRep
            Log.d(TAG, "Reputation incremented by +$change to $newRep for user $userId")
            change
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing reputation: ${e.message}", e)
            0
        }
    }
    
    /**
     * Decrementar reputación del usuario actual (cancelación)
     * @param amount Cantidad a decrementar (1 para WAITING, 5 para ACCEPTED)
     * @return El cambio aplicado (negativo)
     */
    suspend fun decrementReputation(amount: Int): Int {
        val userId = currentUserId ?: return 0
        
        return try {
            val currentRep = _currentUserReputation.value
            val newRep = (currentRep - amount).coerceIn(0.0, 100.0)
            
            supabase.postgrest["usuarios"]
                .update({
                    set("reputation_score", newRep)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            _currentUserReputation.value = newRep
            Log.d(TAG, "Reputation decremented by -$amount to $newRep")
            -amount
        } catch (e: Exception) {
            Log.e(TAG, "Error decrementing reputation: ${e.message}")
            0
        }
    }
    
    /**
     * Desuscribirse
     */
    suspend fun unsubscribe() {
        try {
            realtimeChannel?.let {
                supabase.realtime.removeChannel(it)
            }
            realtimeChannel = null
            currentUserId = null
            watchedOtherUserId = null
            Log.d(TAG, "Unsubscribed from reputation")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing: ${e.message}")
        }
    }
}

@Serializable
private data class UserReputationResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("reputation_score") val reputationScore: Double? = null
)
