package com.rendly.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rendly.app.data.model.*
import com.rendly.app.data.remote.MapboxService
import com.rendly.app.data.repository.AddressRepository
import com.rendly.app.engine.AddressEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.URLEncoder
import javax.inject.Inject

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * ADDRESS VIEWMODEL - Gestión de direcciones con geocoding y validación
 * ══════════════════════════════════════════════════════════════════════════════
 */
@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val mapboxService: MapboxService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val TAG = "AddressViewModel"
    
    // ══════════════════════════════════════════════════════════════════════════
    // STATE
    // ══════════════════════════════════════════════════════════════════════════
    
    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()
    
    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()
    
    private val _predictions = MutableStateFlow<List<AddressPrediction>>(emptyList())
    val predictions: StateFlow<List<AddressPrediction>> = _predictions.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    private val _validationResult = MutableStateFlow<AddressEngine.ValidationResult?>(null)
    val validationResult: StateFlow<AddressEngine.ValidationResult?> = _validationResult.asStateFlow()
    
    private val _events = MutableSharedFlow<AddressEvent>()
    val events: SharedFlow<AddressEvent> = _events.asSharedFlow()
    
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var cancellationTokenSource: CancellationTokenSource? = null
    
    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        AddressEngine.init()
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // PUBLIC METHODS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Carga las direcciones de un usuario
     */
    fun loadAddresses(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            addressRepository.getAddresses(userId).fold(
                onSuccess = { addresses ->
                    _addresses.value = addresses
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    _events.emit(AddressEvent.Error(error.message ?: "Error al cargar direcciones"))
                }
            )
        }
    }
    
    /**
     * Busca predicciones de direcciones (autocomplete)
     */
    fun searchAddresses(query: String) {
        if (query.length < 3) {
            _predictions.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            
            try {
                val results = performAddressSearch(query)
                _predictions.value = results
            } catch (e: Exception) {
                Log.e(TAG, "Error searching addresses", e)
                _predictions.value = emptyList()
            } finally {
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }
    
    /**
     * Geocodifica una dirección (texto → coordenadas)
     */
    fun geocodeAddress(address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeocoding = true) }
            
            try {
                val result = performGeocode(address)
                if (result != null) {
                    _uiState.update { state ->
                        state.copy(
                            isGeocoding = false,
                            selectedGeocodingResult = result,
                            formattedAddress = result.formattedAddress,
                            latitude = result.latitude,
                            longitude = result.longitude,
                            city = result.city ?: "",
                            postalCode = result.postalCode ?: "",
                            country = result.country ?: ""
                        )
                    }
                    
                    // Validar con motor C++
                    validateCurrentAddress()
                } else {
                    _uiState.update { it.copy(isGeocoding = false) }
                    _events.emit(AddressEvent.Error("No se pudo geocodificar la dirección"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error geocoding address", e)
                _uiState.update { it.copy(isGeocoding = false) }
                _events.emit(AddressEvent.Error("Error de geocodificación: ${e.message}"))
            }
        }
    }
    
    /**
     * Reverse geocoding (coordenadas → texto)
     */
    fun reverseGeocode(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeocoding = true) }
            
            try {
                val result = performReverseGeocode(latitude, longitude)
                if (result != null) {
                    Log.d(TAG, "Reverse geocode success: ${result.formattedAddress}")
                    _uiState.update { state ->
                        state.copy(
                            isGeocoding = false,
                            selectedGeocodingResult = result,
                            formattedAddress = result.formattedAddress,
                            rawInput = result.formattedAddress, // Actualizar también el campo de búsqueda
                            latitude = latitude,
                            longitude = longitude,
                            city = result.city ?: "",
                            postalCode = result.postalCode ?: "",
                            country = result.country ?: "",
                            addressSource = AddressSource.GPS
                        )
                    }
                    
                    validateCurrentAddress()
                } else {
                    Log.w(TAG, "Reverse geocode returned null")
                    _uiState.update { it.copy(isGeocoding = false) }
                    _events.emit(AddressEvent.Error("No se pudo obtener la dirección de tu ubicación"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reverse geocoding", e)
                _uiState.update { it.copy(isGeocoding = false) }
                _events.emit(AddressEvent.Error("Error de geocodificación inversa"))
            }
        }
    }
    
    /**
     * Obtiene la ubicación actual del usuario
     * Este método verifica permisos y solicita si no los tiene
     */
    fun getCurrentLocation() {
        viewModelScope.launch {
            if (!hasLocationPermission()) {
                _events.emit(AddressEvent.RequestLocationPermission)
                return@launch
            }
            
            // Ya tiene permisos, obtener ubicación directamente
            fetchCurrentLocationInternal()
        }
    }
    
    /**
     * Obtiene la ubicación después de que los permisos fueron concedidos
     * NO verifica permisos porque ya sabemos que fueron concedidos por el launcher
     */
    fun getCurrentLocationAfterPermission() {
        viewModelScope.launch {
            fetchCurrentLocationInternal()
        }
    }
    
    /**
     * Lógica interna para obtener ubicación (sin verificar permisos)
     */
    @Suppress("MissingPermission")
    private suspend fun fetchCurrentLocationInternal() {
        _uiState.update { it.copy(isGettingLocation = true) }
        
        try {
            cancellationTokenSource?.cancel()
            cancellationTokenSource = CancellationTokenSource()
            
            Log.d(TAG, "Fetching current location...")
            
            val location = fusedLocationClient?.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource!!.token
            )?.await()
            
            if (location != null) {
                Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
                _currentLocation.value = location
                _uiState.update { 
                    it.copy(
                        isGettingLocation = false,
                        gpsLatitude = location.latitude,
                        gpsLongitude = location.longitude
                    )
                }
                
                // Reverse geocode automático para obtener la dirección
                reverseGeocode(location.latitude, location.longitude)
            } else {
                Log.w(TAG, "Location is null")
                _uiState.update { it.copy(isGettingLocation = false) }
                _events.emit(AddressEvent.Error("No se pudo obtener la ubicación. Asegúrate de tener el GPS activado."))
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting location", e)
            _uiState.update { it.copy(isGettingLocation = false) }
            _events.emit(AddressEvent.Error("Permiso de ubicación denegado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            _uiState.update { it.copy(isGettingLocation = false) }
            _events.emit(AddressEvent.Error("Error al obtener ubicación: ${e.message ?: "Error desconocido"}"))
        }
    }
    
    /**
     * Selecciona una predicción de autocompletado
     */
    fun selectPrediction(prediction: AddressPrediction) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    addressSource = AddressSource.AUTOCOMPLETE,
                    rawInput = prediction.fullText
                )
            }
            _predictions.value = emptyList()
            
            // Geocodificar la dirección seleccionada
            geocodeAddress(prediction.fullText)
        }
    }
    
    /**
     * Actualiza la ubicación desde el mapa interactivo
     */
    fun updateLocationFromMap(latitude: Double, longitude: Double, address: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    latitude = latitude,
                    longitude = longitude,
                    formattedAddress = address,
                    rawInput = address,
                    addressSource = AddressSource.MAP
                )
            }
            
            // Validar la dirección con el motor C++
            validateCurrentAddress()
        }
    }
    
    /**
     * Valida la dirección actual con el motor C++
     */
    fun validateCurrentAddress() {
        viewModelScope.launch {
            val state = _uiState.value
            
            if (state.formattedAddress.isBlank() || state.latitude == 0.0) {
                return@launch
            }
            
            _uiState.update { it.copy(isValidating = true) }
            
            val result = addressRepository.validateAddressWithEngine(
                formattedAddress = state.formattedAddress,
                latitude = state.latitude,
                longitude = state.longitude,
                gpsLatitude = state.gpsLatitude,
                gpsLongitude = state.gpsLongitude,
                source = state.addressSource
            )
            
            _validationResult.value = result
            _uiState.update { 
                it.copy(
                    isValidating = false,
                    confidenceScore = result.confidenceScore,
                    addressStatus = when (result.status) {
                        AddressEngine.AddressStatus.VALID -> AddressStatus.VALID
                        AddressEngine.AddressStatus.SUSPICIOUS -> AddressStatus.SUSPICIOUS
                        AddressEngine.AddressStatus.INVALID -> AddressStatus.INVALID
                        AddressEngine.AddressStatus.PENDING -> AddressStatus.PENDING
                    }
                )
            }
        }
    }
    
    /**
     * Guarda la dirección actual
     */
    fun saveAddress(userId: String) {
        viewModelScope.launch {
            Log.d(TAG, "saveAddress called with userId: '$userId'")
            
            if (userId.isBlank()) {
                Log.e(TAG, "userId is empty!")
                _events.emit(AddressEvent.Error("Error: No se pudo identificar tu usuario. Intenta cerrar sesión y volver a entrar."))
                return@launch
            }
            
            val state = _uiState.value
            
            // Allow saving with either geocoded address or manual text input
            val addressText = state.formattedAddress.ifBlank { state.rawInput }
            if (addressText.isBlank()) {
                _events.emit(AddressEvent.Error("Ingresa una dirección válida"))
                return@launch
            }
            
            _uiState.update { it.copy(isSaving = true) }
            
            // Determine source - if no coordinates, mark as MANUAL
            val effectiveSource = if (state.latitude == 0.0 || state.longitude == 0.0) AddressSource.MANUAL else state.addressSource
            val effectiveStatus = if (state.latitude == 0.0 || state.longitude == 0.0) AddressStatus.PENDING else state.addressStatus
            
            val request = AddressCreateRequest(
                userId = userId,
                label = state.label.ifBlank { "Mi dirección" },
                addressType = state.addressType,
                formattedAddress = addressText,
                streetAddress = state.streetAddress.takeIf { it.isNotBlank() },
                streetNumber = state.streetNumber.takeIf { it.isNotBlank() },
                apartment = state.apartment.takeIf { it.isNotBlank() },
                floor = state.floor.takeIf { it.isNotBlank() },
                neighborhood = state.neighborhood.takeIf { it.isNotBlank() },
                city = state.city,
                stateProvince = state.stateProvince.takeIf { it.isNotBlank() },
                postalCode = state.postalCode.takeIf { it.isNotBlank() },
                country = state.country.ifBlank { "Uruguay" },
                countryCode = state.countryCode.ifBlank { "UY" },
                latitude = state.latitude,
                longitude = state.longitude,
                confidenceScore = state.confidenceScore,
                status = effectiveStatus,
                source = effectiveSource,
                rawInput = state.rawInput.takeIf { it.isNotBlank() },
                geocodeProvider = state.selectedGeocodingResult?.provider,
                geocodePlaceId = state.selectedGeocodingResult?.placeId,
                isDefault = state.isDefault,
                deliveryInstructions = state.deliveryInstructions.takeIf { it.isNotBlank() },
                referencePoint = state.referencePoint.takeIf { it.isNotBlank() }
            )
            
            Log.d(TAG, "Calling addressRepository.createAddress...")
            addressRepository.createAddress(
                request = request,
                gpsLatitude = state.gpsLatitude,
                gpsLongitude = state.gpsLongitude
            ).fold(
                onSuccess = { address ->
                    Log.d(TAG, "✓ Address saved successfully: id=${address.id}")
                    // Always reload fresh list from Supabase to ensure consistency
                    addressRepository.getAddresses(userId).fold(
                        onSuccess = { freshAddresses ->
                            _addresses.value = freshAddresses
                            Log.d(TAG, "✓ Addresses reloaded: ${freshAddresses.size} addresses")
                        },
                        onFailure = { e ->
                            Log.w(TAG, "Reload failed, using local: ${e.message}")
                            _addresses.value = listOf(address) + _addresses.value
                        }
                    )
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(AddressEvent.AddressSaved(address))
                    resetForm()
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Error desconocido al guardar la dirección"
                    Log.e(TAG, "❌ Error saving address: $errorMsg", error)
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(AddressEvent.Error(errorMsg))
                }
            )
        }
    }
    
    /**
     * Establece una dirección como default
     */
    fun setDefaultAddress(addressId: String, userId: String) {
        viewModelScope.launch {
            addressRepository.setDefaultAddress(addressId, userId).fold(
                onSuccess = {
                    _addresses.value = _addresses.value.map { addr ->
                        addr.copy(isDefault = addr.id == addressId)
                    }.sortedByDescending { it.isDefault }
                    _events.emit(AddressEvent.DefaultAddressSet)
                },
                onFailure = { error ->
                    _events.emit(AddressEvent.Error(error.message ?: "Error"))
                }
            )
        }
    }
    
    /**
     * Elimina una dirección
     */
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            addressRepository.deleteAddress(addressId).fold(
                onSuccess = {
                    _addresses.value = _addresses.value.filter { it.id != addressId }
                    _events.emit(AddressEvent.AddressDeleted)
                },
                onFailure = { error ->
                    _events.emit(AddressEvent.Error(error.message ?: "Error al eliminar"))
                }
            )
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // FORM UPDATES
    // ══════════════════════════════════════════════════════════════════════════
    
    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }
    
    fun updateAddressType(type: AddressType) {
        _uiState.update { it.copy(addressType = type) }
    }
    
    fun updateStreetAddress(street: String) {
        _uiState.update { it.copy(streetAddress = street, rawInput = street) }
    }
    
    fun updateStreetNumber(number: String) {
        _uiState.update { it.copy(streetNumber = number) }
    }
    
    fun updateApartment(apartment: String) {
        _uiState.update { it.copy(apartment = apartment) }
    }
    
    fun updateFloor(floor: String) {
        _uiState.update { it.copy(floor = floor) }
    }
    
    fun updateCity(city: String) {
        _uiState.update { it.copy(city = city) }
    }
    
    fun updatePostalCode(postalCode: String) {
        _uiState.update { it.copy(postalCode = postalCode) }
    }
    
    fun updateDeliveryInstructions(instructions: String) {
        _uiState.update { it.copy(deliveryInstructions = instructions) }
    }
    
    fun updateReferencePoint(reference: String) {
        _uiState.update { it.copy(referencePoint = reference) }
    }
    
    fun updateIsDefault(isDefault: Boolean) {
        _uiState.update { it.copy(isDefault = isDefault) }
    }
    
    fun resetForm() {
        _uiState.value = AddressUiState()
        _validationResult.value = null
        _predictions.value = emptyList()
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // PRIVATE METHODS
    // ══════════════════════════════════════════════════════════════════════════
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private suspend fun performAddressSearch(query: String): List<AddressPrediction> {
        // Use current location for proximity search if available
        val proximity = _currentLocation.value?.let { 
            Pair(it.longitude, it.latitude) 
        }
        return mapboxService.searchAddresses(query, proximity)
    }
    
    private suspend fun performGeocode(address: String): GeocodingResult? {
        // Try Mapbox first, fallback to Android Geocoder
        return mapboxService.geocodeAddress(address) ?: withContext(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(address, 1)
                
                addresses?.firstOrNull()?.let { addr ->
                    GeocodingResult(
                        formattedAddress = addr.getAddressLine(0) ?: address,
                        latitude = addr.latitude,
                        longitude = addr.longitude,
                        streetAddress = addr.thoroughfare,
                        streetNumber = addr.subThoroughfare,
                        neighborhood = addr.subLocality,
                        city = addr.locality ?: addr.subAdminArea ?: "",
                        stateProvince = addr.adminArea,
                        postalCode = addr.postalCode,
                        country = addr.countryName ?: "Argentina",
                        countryCode = addr.countryCode ?: "AR",
                        provider = "android_geocoder"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Android Geocoder fallback error", e)
                null
            }
        }
    }
    
    private suspend fun performReverseGeocode(lat: Double, lon: Double): GeocodingResult? {
        // Try Mapbox first, fallback to Android Geocoder
        return mapboxService.reverseGeocode(lat, lon) ?: withContext(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(context)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                
                addresses?.firstOrNull()?.let { addr ->
                    GeocodingResult(
                        formattedAddress = addr.getAddressLine(0) ?: "",
                        latitude = lat,
                        longitude = lon,
                        streetAddress = addr.thoroughfare,
                        streetNumber = addr.subThoroughfare,
                        neighborhood = addr.subLocality,
                        city = addr.locality ?: addr.subAdminArea ?: "",
                        stateProvince = addr.adminArea,
                        postalCode = addr.postalCode,
                        country = addr.countryName ?: "Argentina",
                        countryCode = addr.countryCode ?: "AR",
                        provider = "android_geocoder"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Android Geocoder fallback error", e)
                null
            }
        }
    }
    
    /**
     * Get static map URL for preview
     */
    fun getStaticMapUrl(latitude: Double, longitude: Double): String {
        return mapboxService.getStaticMapUrl(latitude, longitude)
    }
    
    override fun onCleared() {
        super.onCleared()
        cancellationTokenSource?.cancel()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// UI STATE
// ══════════════════════════════════════════════════════════════════════════════

data class AddressUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isGeocoding: Boolean = false,
    val isGettingLocation: Boolean = false,
    val isValidating: Boolean = false,
    val isSaving: Boolean = false,
    
    // Error
    val error: String? = null,
    
    // Form data
    val label: String = "",
    val addressType: AddressType = AddressType.HOME,
    val rawInput: String = "",
    val formattedAddress: String = "",
    val streetAddress: String = "",
    val streetNumber: String = "",
    val apartment: String = "",
    val floor: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val stateProvince: String = "",
    val postalCode: String = "",
    val country: String = "Uruguay",
    val countryCode: String = "UY",
    
    // Coordinates
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val gpsLatitude: Double = 0.0,
    val gpsLongitude: Double = 0.0,
    
    // Validation
    val confidenceScore: Int = 0,
    val addressStatus: AddressStatus = AddressStatus.PENDING,
    
    // Metadata
    val addressSource: AddressSource = AddressSource.MANUAL,
    val selectedGeocodingResult: GeocodingResult? = null,
    val isDefault: Boolean = false,
    val deliveryInstructions: String = "",
    val referencePoint: String = ""
) {
    val hasValidLocation: Boolean get() = latitude != 0.0 && longitude != 0.0
    val isProcessing: Boolean get() = isLoading || isSearching || isGeocoding || 
                                       isGettingLocation || isValidating || isSaving
}

// ══════════════════════════════════════════════════════════════════════════════
// EVENTS
// ══════════════════════════════════════════════════════════════════════════════

sealed class AddressEvent {
    data class AddressSaved(val address: Address) : AddressEvent()
    data object AddressDeleted : AddressEvent()
    data object DefaultAddressSet : AddressEvent()
    data class Error(val message: String) : AddressEvent()
    data object RequestLocationPermission : AddressEvent()
}
