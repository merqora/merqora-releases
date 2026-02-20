package com.rendly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estados posibles de una transacción handshake
 */
enum class HandshakeStatus {
    PROPOSED,       // Esperando respuesta del receptor
    ACCEPTED,       // Receptor aceptó, deben encontrarse
    RENEGOTIATING,  // Receptor propuso cambios
    REJECTED,       // Receptor rechazó
    IN_PROGRESS,    // En proceso de encuentro
    COMPLETED,      // Transacción completada exitosamente
    CANCELLED,      // Cancelado
    DISPUTED        // Hay una disputa
}

/**
 * Modelo principal de transacción Handshake
 */
@Serializable
data class HandshakeTransaction(
    val id: String? = null,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("initiator_id") val initiatorId: String,
    @SerialName("receiver_id") val receiverId: String,
    @SerialName("product_description") val productDescription: String,
    @SerialName("agreed_price") val agreedPrice: Double,
    val status: String = "PROPOSED",
    @SerialName("initiator_confirmed") val initiatorConfirmed: Boolean = false,
    @SerialName("receiver_confirmed") val receiverConfirmed: Boolean = false,
    @SerialName("counter_price") val counterPrice: Double? = null,
    @SerialName("counter_message") val counterMessage: String? = null,
    @SerialName("qr_secret_initiator") val qrSecretInitiator: String? = null,
    @SerialName("qr_secret_receiver") val qrSecretReceiver: String? = null,
    @SerialName("qr_scanned_at") val qrScannedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("accepted_at") val acceptedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null
) {
    fun getStatusEnum(): HandshakeStatus = try {
        HandshakeStatus.valueOf(status)
    } catch (e: Exception) {
        HandshakeStatus.PROPOSED
    }
    
    fun isParticipant(userId: String): Boolean = 
        userId == initiatorId || userId == receiverId
    
    fun isInitiator(userId: String): Boolean = userId == initiatorId
    
    fun isReceiver(userId: String): Boolean = userId == receiverId
    
    fun canAccept(userId: String): Boolean = 
        isReceiver(userId) && status == "PROPOSED"
    
    fun canRenegotiate(userId: String): Boolean = 
        isReceiver(userId) && status == "PROPOSED"
    
    fun canReject(userId: String): Boolean = 
        isReceiver(userId) && status == "PROPOSED"
    
    fun canConfirm(userId: String): Boolean {
        if (status != "ACCEPTED" && status != "IN_PROGRESS") return false
        return if (isInitiator(userId)) !initiatorConfirmed else !receiverConfirmed
    }
    
    fun canCancel(userId: String): Boolean = 
        isParticipant(userId) && status in listOf("PROPOSED", "ACCEPTED", "IN_PROGRESS")
}

/**
 * DTO para crear un nuevo handshake
 */
@Serializable
data class CreateHandshakeRequest(
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("initiator_id") val initiatorId: String,
    @SerialName("receiver_id") val receiverId: String,
    @SerialName("product_description") val productDescription: String,
    @SerialName("agreed_price") val agreedPrice: Double,
    @SerialName("qr_secret_initiator") val qrSecretInitiator: String
)

/**
 * DTO para responder a un handshake
 */
@Serializable
data class HandshakeResponse(
    val action: String, // "accept", "reject", "renegotiate"
    @SerialName("counter_price") val counterPrice: Double? = null,
    @SerialName("counter_message") val counterMessage: String? = null,
    @SerialName("qr_secret_receiver") val qrSecretReceiver: String? = null
)

/**
 * Evento de Realtime para cambios en handshake
 */
sealed class HandshakeEvent {
    data class Created(val handshake: HandshakeTransaction) : HandshakeEvent()
    data class Updated(val handshake: HandshakeTransaction) : HandshakeEvent()
    data class Deleted(val handshakeId: String) : HandshakeEvent()
}
