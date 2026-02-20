package com.rendly.app.data.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class Usuario(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("created_at") val createdAt: String? = null,
    val email: String? = null,
    val username: String = "",
    @SerialName("nombre_tienda") val nombreTienda: String? = null,
    val descripcion: String? = null,
    val genero: String? = null,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    @SerialName("fecha_registro") val fechaRegistro: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    val facebook: String? = null,
    val whatsapp: String? = null,
    val twitter: String? = null,
    val instagram: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    val ubicacion: String? = null,
    @SerialName("ultima_actividad") val ultimaActividad: String? = null,
    val rol: String? = null,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("last_online") val lastOnline: String? = null,
    @SerialName("is_anonymous") val isAnonymous: Boolean = false,
    @SerialName("recibir_novedades") val recibirNovedades: Boolean = false,
    @SerialName("tiene_tienda") val tieneTienda: Boolean = false,
    val baneado: Boolean = false,
    @SerialName("motivo_baneo") val motivoBaneo: String? = null,
    val nombre: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("reputation_score") val reputationScore: Double? = null
)
