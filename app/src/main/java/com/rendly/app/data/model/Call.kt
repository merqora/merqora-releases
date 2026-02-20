package com.rendly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* ═══════════════════════════════════════════════════════════════
   MODELOS DE LLAMADAS - WebRTC + Supabase Realtime
═══════════════════════════════════════════════════════════════ */

@Serializable
data class CallDB(
    val id: String = "",
    @SerialName("caller_id") val callerId: String = "",
    @SerialName("callee_id") val calleeId: String = "",
    val status: String = "ringing",
    @SerialName("call_type") val callType: String = "voice",
    @SerialName("offer_sdp") val offerSdp: String? = null,
    @SerialName("answer_sdp") val answerSdp: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("answered_at") val answeredAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int = 0,
    @SerialName("end_reason") val endReason: String = "normal"
)

@Serializable
data class IceCandidateDB(
    val id: String = "",
    @SerialName("call_id") val callId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    val candidate: String = "",
    @SerialName("sdp_mid") val sdpMid: String? = null,
    @SerialName("sdp_m_line_index") val sdpMLineIndex: Int? = null,
    @SerialName("created_at") val createdAt: String = ""
)

/** Estado de la llamada en la UI */
enum class CallStatus {
    IDLE,           // Sin llamada activa
    OUTGOING,       // Llamada saliente (esperando que contesten)
    INCOMING,       // Llamada entrante (sonando)
    CONNECTED,      // Llamada activa/conectada
    RECONNECTING,   // Reconectando (pérdida temporal de red)
    ENDED           // Llamada finalizada
}

/** Razón de finalización */
enum class CallEndReason {
    NORMAL,         // Colgó normalmente
    MISSED,         // No contestaron
    REJECTED,       // Rechazaron la llamada
    BUSY,           // Ocupado
    NETWORK_ERROR,  // Error de red
    TIMEOUT         // Timeout (60s sin respuesta)
}

/** Estado completo de la llamada para la UI */
data class CallState(
    val callId: String? = null,
    val status: CallStatus = CallStatus.IDLE,
    val callType: String = "voice",
    val isOutgoing: Boolean = true,
    
    // Info del otro usuario
    val otherUserId: String = "",
    val otherUsername: String = "",
    val otherAvatarUrl: String = "",
    
    // Controles de audio
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    
    // Duración
    val durationSeconds: Int = 0,
    
    // Fin
    val endReason: CallEndReason? = null
)
