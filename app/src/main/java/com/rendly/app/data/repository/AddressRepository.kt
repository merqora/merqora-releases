package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.model.*
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.engine.AddressEngine
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * ADDRESS REPOSITORY - Gestión de direcciones con Supabase y motor C++
 * ══════════════════════════════════════════════════════════════════════════════
 */
@Singleton
class AddressRepository @Inject constructor() {
    
    private val supabaseClient get() = SupabaseClient.client
    private val TAG = "AddressRepository"
    
    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: Flow<List<Address>> = _addresses.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Obtiene todas las direcciones de un usuario
     */
    suspend fun getAddresses(userId: String): Result<List<Address>> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            
            val result = supabaseClient.postgrest["addresses"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_active", true)
                    }
                    order("is_default", Order.DESCENDING)
                    order("last_used_at", Order.DESCENDING)
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Address>()
            
            _addresses.value = result
            Log.i(TAG, "Loaded ${result.size} addresses for user $userId")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading addresses", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Obtiene una dirección por ID
     */
    suspend fun getAddressById(addressId: String): Result<Address?> = withContext(Dispatchers.IO) {
        try {
            val result = supabaseClient.postgrest["addresses"]
                .select {
                    filter {
                        eq("id", addressId)
                    }
                }
                .decodeSingleOrNull<Address>()
            
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting address by ID", e)
            Result.failure(e)
        }
    }
    
    /**
     * Crea una nueva dirección con validación del motor C++
     */
    suspend fun createAddress(
        request: AddressCreateRequest,
        gpsLatitude: Double = 0.0,
        gpsLongitude: Double = 0.0
    ): Result<Address> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            Log.d(TAG, "createAddress: Starting for userId=${request.userId}, address=${request.formattedAddress}")
            
            // Validar con motor C++
            val validation = AddressEngine.validateAddress(
                formattedAddress = request.formattedAddress,
                latitude = request.latitude,
                longitude = request.longitude,
                gpsLatitude = gpsLatitude,
                gpsLongitude = gpsLongitude,
                source = when (request.source) {
                    AddressSource.GPS -> AddressEngine.AddressSource.GPS
                    AddressSource.MANUAL -> AddressEngine.AddressSource.MANUAL
                    AddressSource.AUTOCOMPLETE -> AddressEngine.AddressSource.AUTOCOMPLETE
                    AddressSource.MAP -> AddressEngine.AddressSource.MAP
                }
            )
            
            // Map source for DB compatibility (DB enum may not have 'map' yet)
            val dbSafeSource = if (request.source == AddressSource.MAP) AddressSource.AUTOCOMPLETE else request.source
            
            // Crear request con métricas de validación
            val enrichedRequest = request.copy(
                source = dbSafeSource,
                confidenceScore = validation.confidenceScore,
                status = when (validation.status) {
                    AddressEngine.AddressStatus.VALID -> AddressStatus.VALID
                    AddressEngine.AddressStatus.SUSPICIOUS -> AddressStatus.SUSPICIOUS
                    AddressEngine.AddressStatus.INVALID -> AddressStatus.INVALID
                    AddressEngine.AddressStatus.PENDING -> AddressStatus.PENDING
                },
                textCoordConsistency = validation.textCoordConsistency,
                gpsGeocodeDistance = validation.gpsGeocodeDistance,
                addressCompleteness = validation.addressCompleteness
            )
            
            // Insertar en Supabase
            Log.d(TAG, "Inserting address into Supabase for user ${request.userId}...")
            
            // Step 1: Insert - let errors propagate (don't catch insert failures)
            val insertResult = supabaseClient.postgrest["addresses"]
                .insert(enrichedRequest) {
                    select()
                }
            Log.d(TAG, "✓ Insert completed successfully")
            
            // Step 2: Decode - this can fail if response schema differs, but data IS saved
            val result = try {
                insertResult.decodeSingle<Address>()
            } catch (decodeError: Exception) {
                Log.w(TAG, "Insert succeeded but decode failed: ${decodeError.message}")
                // Data IS in Supabase, reload fresh list
                null
            }
            
            // Step 3: If decode failed, reload from Supabase to get the saved address
            if (result != null) {
                _addresses.value = listOf(result) + _addresses.value
                Log.i(TAG, "✓ Address created and decoded: id=${result.id}, score=${enrichedRequest.confidenceScore}")
                Result.success(result)
            } else {
                // Reload from Supabase since the insert succeeded
                val freshAddresses = try {
                    supabaseClient.postgrest["addresses"]
                        .select {
                            filter {
                                eq("user_id", request.userId)
                                eq("is_active", true)
                            }
                            order("created_at", Order.DESCENDING)
                        }
                        .decodeList<Address>()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reload addresses after insert: ${e.message}")
                    emptyList()
                }
                _addresses.value = freshAddresses
                val savedAddress = freshAddresses.firstOrNull()
                if (savedAddress != null) {
                    Log.i(TAG, "✓ Address created (reloaded): id=${savedAddress.id}")
                    Result.success(savedAddress)
                } else {
                    Log.e(TAG, "❌ Insert returned success but address not found on reload")
                    Result.failure(Exception("La dirección se guardó pero no se pudo verificar. Recarga la lista."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating address: ${e.message}", e)
            // Ensure we have a meaningful error message
            val errorMessage = when {
                e.message?.contains("401") == true -> "Sesión expirada. Por favor, vuelve a iniciar sesión."
                e.message?.contains("403") == true -> "No tienes permiso para realizar esta acción."
                e.message?.contains("404") == true -> "Recurso no encontrado."
                e.message?.contains("409") == true -> "Ya existe una dirección similar."
                e.message?.contains("network") == true || e.message?.contains("connect") == true -> 
                    "Error de conexión. Verifica tu internet."
                e.message.isNullOrBlank() -> "Error al guardar la dirección. Inténtalo de nuevo."
                else -> e.message ?: "Error desconocido al guardar"
            }
            Result.failure(Exception(errorMessage, e))
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Actualiza una dirección existente
     */
    suspend fun updateAddress(
        addressId: String,
        updates: Map<String, Any>
    ): Result<Address> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            
            val result = supabaseClient.postgrest["addresses"]
                .update(updates) {
                    filter {
                        eq("id", addressId)
                    }
                }
                .decodeSingle<Address>()
            
            // Actualizar lista local
            _addresses.value = _addresses.value.map { 
                if (it.id == addressId) result else it 
            }
            
            Log.i(TAG, "Updated address: $addressId")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating address", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Establece una dirección como default
     */
    suspend fun setDefaultAddress(addressId: String, userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Quitar default de otras direcciones
            supabaseClient.postgrest["addresses"]
                .update(mapOf("is_default" to false)) {
                    filter {
                        eq("user_id", userId)
                        eq("is_default", true)
                        neq("id", addressId)
                    }
                }
            
            // Establecer nueva default
            supabaseClient.postgrest["addresses"]
                .update(mapOf(
                    "is_default" to true,
                    "last_used_at" to java.time.Instant.now().toString()
                )) {
                    filter {
                        eq("id", addressId)
                    }
                }
            
            // Actualizar lista local
            _addresses.value = _addresses.value.map { addr ->
                when {
                    addr.id == addressId -> addr.copy(isDefault = true)
                    addr.isDefault -> addr.copy(isDefault = false)
                    else -> addr
                }
            }.sortedByDescending { it.isDefault }
            
            Log.i(TAG, "Set default address: $addressId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting default address", e)
            Result.failure(e)
        }
    }
    
    /**
     * Elimina (desactiva) una dirección
     */
    suspend fun deleteAddress(addressId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            
            supabaseClient.postgrest["addresses"]
                .update(mapOf(
                    "is_active" to false,
                    "is_default" to false
                )) {
                    filter {
                        eq("id", addressId)
                    }
                }
            
            // Actualizar lista local
            _addresses.value = _addresses.value.filter { it.id != addressId }
            
            Log.i(TAG, "Deleted address: $addressId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting address", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Valida una dirección usando el motor C++
     */
    suspend fun validateAddressWithEngine(
        formattedAddress: String,
        latitude: Double,
        longitude: Double,
        gpsLatitude: Double = 0.0,
        gpsLongitude: Double = 0.0,
        source: AddressSource = AddressSource.MANUAL
    ): AddressEngine.ValidationResult {
        return AddressEngine.validateAddress(
            formattedAddress = formattedAddress,
            latitude = latitude,
            longitude = longitude,
            gpsLatitude = gpsLatitude,
            gpsLongitude = gpsLongitude,
            source = when (source) {
                AddressSource.GPS -> AddressEngine.AddressSource.GPS
                AddressSource.MANUAL -> AddressEngine.AddressSource.MANUAL
                AddressSource.AUTOCOMPLETE -> AddressEngine.AddressSource.AUTOCOMPLETE
                AddressSource.MAP -> AddressEngine.AddressSource.MAP
            }
        )
    }
    
    /**
     * Normaliza una dirección usando el motor C++
     */
    suspend fun normalizeAddress(rawInput: String): String {
        return AddressEngine.normalizeAddress(rawInput)
    }
    
    /**
     * Compara dos direcciones para detectar duplicados
     */
    suspend fun areAddressesSimilar(addr1: String, addr2: String): Boolean {
        val similarity = AddressEngine.compareAddresses(addr1, addr2)
        return similarity > 0.8f // 80% similitud = probable duplicado
    }
    
    /**
     * Obtiene la dirección default de un usuario
     */
    suspend fun getDefaultAddress(userId: String): Address? {
        return _addresses.value.find { it.isDefault } 
            ?: _addresses.value.firstOrNull()
    }
    
    /**
     * Limpia el cache local
     */
    fun clearLocalCache() {
        _addresses.value = emptyList()
        AddressEngine.clearCache()
    }
}
