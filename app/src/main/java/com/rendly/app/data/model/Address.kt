package com.rendly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * ADDRESS MODEL - Modelo de dirección con validación y scoring
 * ══════════════════════════════════════════════════════════════════════════════
 */

@Serializable
enum class AddressType {
    @SerialName("home") HOME,
    @SerialName("work") WORK,
    @SerialName("other") OTHER
}

@Serializable
enum class AddressSource {
    @SerialName("gps") GPS,
    @SerialName("manual") MANUAL,
    @SerialName("autocomplete") AUTOCOMPLETE,
    @SerialName("map") MAP
}

@Serializable
enum class AddressStatus {
    @SerialName("valid") VALID,
    @SerialName("suspicious") SUSPICIOUS,
    @SerialName("invalid") INVALID,
    @SerialName("pending") PENDING
}

@Serializable
data class Address(
    val id: String = "",
    
    @SerialName("user_id")
    val userId: String = "",
    
    val label: String = "Mi dirección",
    
    @SerialName("address_type")
    val addressType: AddressType = AddressType.HOME,
    
    @SerialName("formatted_address")
    val formattedAddress: String = "",
    
    @SerialName("street_address")
    val streetAddress: String? = null,
    
    @SerialName("street_number")
    val streetNumber: String? = null,
    
    val apartment: String? = null,
    
    val floor: String? = null,
    
    val neighborhood: String? = null,
    
    val city: String = "",
    
    @SerialName("state_province")
    val stateProvince: String? = null,
    
    @SerialName("postal_code")
    val postalCode: String? = null,
    
    val country: String = "Argentina",
    
    @SerialName("country_code")
    val countryCode: String = "AR",
    
    val latitude: Double = 0.0,
    
    val longitude: Double = 0.0,
    
    @SerialName("confidence_score")
    val confidenceScore: Int = 0,
    
    val status: AddressStatus = AddressStatus.PENDING,
    
    @SerialName("is_verified")
    val isVerified: Boolean = false,
    
    @SerialName("text_coord_consistency")
    val textCoordConsistency: Float? = null,
    
    @SerialName("gps_geocode_distance")
    val gpsGeocodeDistance: Float? = null,
    
    @SerialName("address_completeness")
    val addressCompleteness: Float? = null,
    
    val source: AddressSource = AddressSource.MANUAL,
    
    @SerialName("raw_input")
    val rawInput: String? = null,
    
    @SerialName("geocode_provider")
    val geocodeProvider: String? = null,
    
    @SerialName("geocode_place_id")
    val geocodePlaceId: String? = null,
    
    @SerialName("is_default")
    val isDefault: Boolean = false,
    
    @SerialName("is_active")
    val isActive: Boolean = true,
    
    @SerialName("delivery_instructions")
    val deliveryInstructions: String? = null,
    
    @SerialName("reference_point")
    val referencePoint: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null,
    
    @SerialName("verified_at")
    val verifiedAt: String? = null,
    
    @SerialName("last_used_at")
    val lastUsedAt: String? = null
) {
    val isValid: Boolean get() = status == AddressStatus.VALID
    val isSuspicious: Boolean get() = status == AddressStatus.SUSPICIOUS
    val isInvalid: Boolean get() = status == AddressStatus.INVALID
    
    val displayAddress: String get() = buildString {
        append(formattedAddress)
        if (!apartment.isNullOrBlank()) {
            append(", Dpto $apartment")
        }
        if (!floor.isNullOrBlank()) {
            append(", Piso $floor")
        }
    }
    
    val shortAddress: String get() = buildString {
        streetAddress?.let { append(it) }
        streetNumber?.let { append(" $it") }
        if (isEmpty()) append(formattedAddress.take(50))
    }
    
    val cityWithPostalCode: String get() = buildString {
        append(city)
        postalCode?.let { append(" - $it") }
    }
    
    fun getTypeIcon(): String = when (addressType) {
        AddressType.HOME -> "🏠"
        AddressType.WORK -> "🏢"
        AddressType.OTHER -> "📍"
    }
    
    fun getStatusText(): String = when (status) {
        AddressStatus.VALID -> "Verificada"
        AddressStatus.SUSPICIOUS -> "Pendiente de verificación"
        AddressStatus.INVALID -> "Dirección inválida"
        AddressStatus.PENDING -> "Pendiente"
    }
}

/**
 * DTO para crear/actualizar una dirección
 */
@Serializable
data class AddressCreateRequest(
    @SerialName("user_id")
    val userId: String,
    
    val label: String,
    
    @SerialName("address_type")
    val addressType: AddressType,
    
    @SerialName("formatted_address")
    val formattedAddress: String,
    
    @SerialName("street_address")
    val streetAddress: String? = null,
    
    @SerialName("street_number")
    val streetNumber: String? = null,
    
    val apartment: String? = null,
    
    val floor: String? = null,
    
    val neighborhood: String? = null,
    
    val city: String,
    
    @SerialName("state_province")
    val stateProvince: String? = null,
    
    @SerialName("postal_code")
    val postalCode: String? = null,
    
    val country: String = "Argentina",
    
    @SerialName("country_code")
    val countryCode: String = "AR",
    
    val latitude: Double,
    
    val longitude: Double,
    
    @SerialName("confidence_score")
    val confidenceScore: Int,
    
    val status: AddressStatus,
    
    @SerialName("text_coord_consistency")
    val textCoordConsistency: Float? = null,
    
    @SerialName("gps_geocode_distance")
    val gpsGeocodeDistance: Float? = null,
    
    @SerialName("address_completeness")
    val addressCompleteness: Float? = null,
    
    val source: AddressSource,
    
    @SerialName("raw_input")
    val rawInput: String? = null,
    
    @SerialName("geocode_provider")
    val geocodeProvider: String? = null,
    
    @SerialName("geocode_place_id")
    val geocodePlaceId: String? = null,
    
    @SerialName("is_default")
    val isDefault: Boolean = false,
    
    @SerialName("delivery_instructions")
    val deliveryInstructions: String? = null,
    
    @SerialName("reference_point")
    val referencePoint: String? = null
)

/**
 * Resultado de geocodificación
 */
data class GeocodingResult(
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double,
    val streetAddress: String? = null,
    val streetNumber: String? = null,
    val neighborhood: String? = null,
    val city: String,
    val stateProvince: String? = null,
    val postalCode: String? = null,
    val country: String,
    val countryCode: String,
    val placeId: String? = null,
    val provider: String = "google"
)

/**
 * Predicción de autocompletado
 */
data class AddressPrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullText: String,
    val types: List<String> = emptyList()
)
