package com.rendly.app.engine

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * ADDRESS ENGINE - Motor nativo C++ para validación y scoring de direcciones
 * ══════════════════════════════════════════════════════════════════════════════
 * 
 * ARQUITECTURA:
 * - C++ (AddressEngine.cpp): Normalización, validación y scoring
 * - Kotlin: API de alto nivel y ejecución en background
 * 
 * CARACTERÍSTICAS:
 * - Normalización avanzada de direcciones (limpieza, tokenización, estandarización)
 * - Scoring de confianza (0-100) basado en múltiples factores
 * - Detección de direcciones dudosas o inválidas
 * - Cálculo de distancia Haversine optimizado
 * - Cache de validaciones
 * 
 * ══════════════════════════════════════════════════════════════════════════════
 */
object AddressEngine {
    
    private const val TAG = "AddressEngine"
    private var isLoaded = false
    
    init {
        try {
            System.loadLibrary("Merqora-native")
            isLoaded = true
            Log.i(TAG, "✓ Native address engine loaded")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "✗ Failed to load native library", e)
            isLoaded = false
        }
    }
    
    /**
     * Estado de validación de una dirección
     */
    enum class AddressStatus(val value: Int) {
        VALID(0),
        SUSPICIOUS(1),
        INVALID(2),
        PENDING(3);
        
        companion object {
            fun fromValue(value: Int): AddressStatus {
                return entries.find { it.value == value } ?: PENDING
            }
        }
    }
    
    /**
     * Fuente de la dirección
     */
    enum class AddressSource(val value: Int) {
        GPS(0),
        MANUAL(1),
        AUTOCOMPLETE(2),
        MAP(3)
    }
    
    /**
     * Resultado de validación de una dirección
     */
    data class ValidationResult(
        val confidenceScore: Int,
        val status: AddressStatus,
        val textCoordConsistency: Float,
        val gpsGeocodeDistance: Float,
        val addressCompleteness: Float,
        val warningsCount: Int
    ) {
        val isValid: Boolean get() = status == AddressStatus.VALID
        val isSuspicious: Boolean get() = status == AddressStatus.SUSPICIOUS
        val isInvalid: Boolean get() = status == AddressStatus.INVALID
        
        fun getStatusText(): String = when (status) {
            AddressStatus.VALID -> "Válida"
            AddressStatus.SUSPICIOUS -> "Dudosa"
            AddressStatus.INVALID -> "Inválida"
            AddressStatus.PENDING -> "Pendiente"
        }
        
        fun getStatusColor(): Long = when (status) {
            AddressStatus.VALID -> 0xFF10B981      // Green
            AddressStatus.SUSPICIOUS -> 0xFFF59E0B // Amber
            AddressStatus.INVALID -> 0xFFEF4444    // Red
            AddressStatus.PENDING -> 0xFF6B7280    // Gray
        }
    }
    
    /**
     * Inicializa el motor nativo. Llamar al inicio de la app.
     */
    fun init() {
        if (!isLoaded) {
            Log.w(TAG, "Cannot init: native library not loaded")
            return
        }
        nativeInit()
        Log.i(TAG, "✓ Address engine initialized")
    }
    
    /**
     * Normaliza una dirección (limpieza, expansión de abreviaturas, formateo)
     * Ejecuta en background para no bloquear UI
     */
    suspend fun normalizeAddress(rawInput: String): String = withContext(Dispatchers.Default) {
        if (!isLoaded) {
            Log.w(TAG, "Cannot normalize: native library not loaded")
            return@withContext rawInput
        }
        
        if (rawInput.isBlank()) {
            return@withContext ""
        }
        
        try {
            nativeNormalizeAddress(rawInput)
        } catch (e: Exception) {
            Log.e(TAG, "Error normalizing address", e)
            rawInput
        }
    }
    
    /**
     * Normaliza una dirección de forma síncrona (usar solo fuera del main thread)
     */
    fun normalizeAddressSync(rawInput: String): String {
        if (!isLoaded || rawInput.isBlank()) return rawInput
        return try {
            nativeNormalizeAddress(rawInput)
        } catch (e: Exception) {
            Log.e(TAG, "Error normalizing address", e)
            rawInput
        }
    }
    
    /**
     * Valida una dirección y calcula su score de confianza
     * Ejecuta en background para no bloquear UI
     * 
     * @param formattedAddress Dirección formateada
     * @param latitude Latitud de la dirección geocodificada
     * @param longitude Longitud de la dirección geocodificada
     * @param gpsLatitude Latitud del GPS del usuario (0 si no disponible)
     * @param gpsLongitude Longitud del GPS del usuario (0 si no disponible)
     * @param source Fuente de la dirección (GPS, MANUAL, AUTOCOMPLETE)
     */
    suspend fun validateAddress(
        formattedAddress: String,
        latitude: Double,
        longitude: Double,
        gpsLatitude: Double = 0.0,
        gpsLongitude: Double = 0.0,
        source: AddressSource = AddressSource.MANUAL
    ): ValidationResult = withContext(Dispatchers.Default) {
        if (!isLoaded) {
            Log.w(TAG, "Cannot validate: native library not loaded")
            return@withContext ValidationResult(
                confidenceScore = 50,
                status = AddressStatus.PENDING,
                textCoordConsistency = 0.5f,
                gpsGeocodeDistance = 0f,
                addressCompleteness = 0.5f,
                warningsCount = 0
            )
        }
        
        try {
            val result = nativeValidateAddress(
                formattedAddress,
                latitude,
                longitude,
                gpsLatitude,
                gpsLongitude,
                source.value
            )
            
            ValidationResult(
                confidenceScore = result[0],
                status = AddressStatus.fromValue(result[1]),
                textCoordConsistency = result[2] / 100f,
                gpsGeocodeDistance = result[3].toFloat(),
                addressCompleteness = result[4] / 100f,
                warningsCount = result[5]
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error validating address", e)
            // Fallback: si la validación nativa falla, NO marcar como inválida
            // GPS y autocomplete son fuentes confiables
            val fallbackScore = when (source) {
                AddressSource.GPS -> 85
                AddressSource.AUTOCOMPLETE -> 80
                AddressSource.MAP -> 75
                AddressSource.MANUAL -> 60
            }
            val fallbackStatus = if (fallbackScore >= 70) AddressStatus.VALID else AddressStatus.PENDING
            ValidationResult(
                confidenceScore = fallbackScore,
                status = fallbackStatus,
                textCoordConsistency = 0.7f,
                gpsGeocodeDistance = 0f,
                addressCompleteness = 0.7f,
                warningsCount = 0
            )
        }
    }
    
    /**
     * Calcula la distancia en metros entre dos puntos geográficos
     * Usa la fórmula Haversine (precisión ~0.5%)
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        if (!isLoaded) {
            // Fallback a cálculo Kotlin si native no está disponible
            return calculateDistanceKotlin(lat1, lon1, lat2, lon2)
        }
        return try {
            nativeCalculateDistance(lat1, lon1, lat2, lon2)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating distance", e)
            calculateDistanceKotlin(lat1, lon1, lat2, lon2)
        }
    }
    
    /**
     * Verifica si una dirección parece sospechosa (patrones de entrada falsa)
     */
    suspend fun isSuspiciousAddress(address: String): Boolean = withContext(Dispatchers.Default) {
        if (!isLoaded || address.isBlank()) return@withContext false
        
        try {
            nativeIsSuspiciousAddress(address)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking suspicious address", e)
            false
        }
    }
    
    /**
     * Compara dos direcciones y retorna un score de similitud (0-1)
     */
    suspend fun compareAddresses(addr1: String, addr2: String): Float = withContext(Dispatchers.Default) {
        if (!isLoaded || addr1.isBlank() || addr2.isBlank()) return@withContext 0f
        
        try {
            nativeCompareAddresses(addr1, addr2)
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing addresses", e)
            0f
        }
    }
    
    /**
     * Limpia el cache de validaciones
     */
    fun clearCache() {
        if (!isLoaded) return
        try {
            nativeClearCache()
            Log.i(TAG, "Validation cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
    
    /**
     * Habilita o deshabilita el cache de validaciones
     */
    fun setCacheEnabled(enabled: Boolean) {
        if (!isLoaded) return
        try {
            nativeSetCacheEnabled(enabled)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting cache enabled", e)
        }
    }
    
    /**
     * Verifica si el motor nativo está disponible
     */
    fun isAvailable(): Boolean = isLoaded
    
    // ══════════════════════════════════════════════════════════════════════════
    // FALLBACK KOTLIN IMPLEMENTATIONS
    // ══════════════════════════════════════════════════════════════════════════
    
    private fun calculateDistanceKotlin(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // JNI NATIVES - Implementados en AddressEngine.cpp
    // ══════════════════════════════════════════════════════════════════════════
    
    private external fun nativeInit()
    private external fun nativeNormalizeAddress(rawInput: String): String
    private external fun nativeValidateAddress(
        formattedAddress: String,
        latitude: Double,
        longitude: Double,
        gpsLatitude: Double,
        gpsLongitude: Double,
        source: Int
    ): IntArray
    private external fun nativeCalculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double
    private external fun nativeIsSuspiciousAddress(address: String): Boolean
    private external fun nativeCompareAddresses(addr1: String, addr2: String): Float
    private external fun nativeClearCache()
    private external fun nativeSetCacheEnabled(enabled: Boolean)
}
