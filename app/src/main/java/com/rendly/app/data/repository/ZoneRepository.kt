package com.rendly.app.data.repository

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Locale

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ZONE REPOSITORY - Geolocalización + búsquedas por zona
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Reutiliza el mismo patrón de FusedLocationProviderClient de AddressViewModel
 * pero de forma ligera (sin ViewModel, singleton para toda la app).
 * 
 * Funcionalidades:
 * ✓ Auto-detección de ubicación del usuario (GPS + Geocoder)
 * ✓ Stats de zona desde Supabase (posts/vendedores cercanos)
 * ✓ Búsquedas populares por zona desde Supabase
 * ✓ Historial de búsquedas recientes (SharedPreferences)
 * ✓ Cache de ubicación (evita re-detectar en cada apertura)
 */
object ZoneRepository {
    private const val TAG = "ZoneRepository"
    private const val PREFS_NAME = "zone_prefs"
    private const val KEY_RECENT_SEARCHES = "recent_searches"
    private const val KEY_CACHED_CITY = "cached_city"
    private const val KEY_CACHED_COUNTRY = "cached_country"
    private const val KEY_CACHED_STATE = "cached_state"
    private const val KEY_CACHED_LAT = "cached_lat"
    private const val KEY_CACHED_LON = "cached_lon"
    private const val KEY_CACHED_ADDRESS = "cached_formatted_address"
    private const val KEY_CACHE_TIMESTAMP = "cache_timestamp"
    private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutos
    private const val MAX_RECENT_SEARCHES = 10
    
    private lateinit var prefs: SharedPreferences
    private var cancellationTokenSource: CancellationTokenSource? = null
    
    // Estado de ubicación actual
    private val _locationState = MutableStateFlow(ZoneLocationState())
    val locationState: StateFlow<ZoneLocationState> = _locationState.asStateFlow()
    
    // Stats de zona
    private val _zoneStats = MutableStateFlow(ZoneStats())
    val zoneStats: StateFlow<ZoneStats> = _zoneStats.asStateFlow()
    
    // Búsquedas populares
    private val _popularSearches = MutableStateFlow<List<PopularSearch>>(emptyList())
    val popularSearches: StateFlow<List<PopularSearch>> = _popularSearches.asStateFlow()
    
    // Búsquedas recientes
    private val _recentSearches = MutableStateFlow<List<RecentSearch>>(emptyList())
    val recentSearches: StateFlow<List<RecentSearch>> = _recentSearches.asStateFlow()
    
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadCachedLocation()
        loadRecentSearches()
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // LOCATION DETECTION
    // ══════════════════════════════════════════════════════════════════════════
    
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Detectar ubicación del usuario. Llama DESPUÉS de verificar/obtener permisos.
     */
    @Suppress("MissingPermission")
    suspend fun detectLocation(context: Context) {
        // Si hay cache válido, usar eso
        val cached = _locationState.value
        if (cached.isLoaded && !cached.isStale()) {
            Log.d(TAG, "Using cached location: ${cached.city}, ${cached.country}")
            // Cargar stats con ubicación cacheada
            loadZoneStats(cached.latitude, cached.longitude)
            return
        }
        
        _locationState.update { it.copy(isLoading = true, error = null) }
        
        try {
            cancellationTokenSource?.cancel()
            cancellationTokenSource = CancellationTokenSource()
            
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource!!.token
            ).await()
            
            if (location != null) {
                Log.d(TAG, "GPS location: ${location.latitude}, ${location.longitude}")
                reverseGeocodeAndUpdate(context, location.latitude, location.longitude)
            } else {
                // Intentar última ubicación conocida como fallback
                val lastLocation = fusedClient.lastLocation.await()
                if (lastLocation != null) {
                    Log.d(TAG, "Using last known location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    reverseGeocodeAndUpdate(context, lastLocation.latitude, lastLocation.longitude)
                } else {
                    _locationState.update { 
                        it.copy(isLoading = false, error = "No se pudo obtener tu ubicación. Activa el GPS.")
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied", e)
            _locationState.update { 
                it.copy(isLoading = false, error = "Permiso de ubicación denegado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting location", e)
            _locationState.update { 
                it.copy(isLoading = false, error = "Error al detectar ubicación: ${e.message}")
            }
        }
    }
    
    private suspend fun reverseGeocodeAndUpdate(context: Context, lat: Double, lon: Double) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                
                val addr = addresses?.firstOrNull()
                val city = addr?.locality ?: addr?.subAdminArea ?: ""
                val state = addr?.adminArea ?: ""
                val country = addr?.countryName ?: ""
                val formatted = addr?.getAddressLine(0) ?: "$city, $country"
                
                val newState = ZoneLocationState(
                    isLoading = false,
                    isLoaded = true,
                    latitude = lat,
                    longitude = lon,
                    city = city,
                    stateProvince = state,
                    country = country,
                    formattedAddress = formatted,
                    timestamp = System.currentTimeMillis()
                )
                
                _locationState.value = newState
                cacheLocation(newState)
                
                Log.d(TAG, "Location resolved: $city, $state, $country")
                
                // Cargar stats de zona
                loadZoneStats(lat, lon)
            } catch (e: Exception) {
                Log.e(TAG, "Reverse geocode error", e)
                // Aún así guardar las coordenadas
                _locationState.update { 
                    it.copy(
                        isLoading = false,
                        isLoaded = true,
                        latitude = lat,
                        longitude = lon,
                        city = "Tu ubicación",
                        error = null,
                        timestamp = System.currentTimeMillis()
                    )
                }
                loadZoneStats(lat, lon)
            }
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // ZONE STATS (desde Supabase)
    // ══════════════════════════════════════════════════════════════════════════
    
    private suspend fun loadZoneStats(lat: Double, lon: Double) {
        try {
            val result = SupabaseClient.database.rpc(
                "get_zone_stats",
                buildJsonObject {
                    put("user_lat", lat)
                    put("user_lon", lon)
                    put("radius_km", 50.0)
                }
            )
            
            val body = result.data
            // Parse JSON response
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val stats = json.decodeFromString<ZoneStatsResponse>(body)
            
            _zoneStats.value = ZoneStats(
                postCount = stats.postCount,
                sellerCount = stats.sellerCount,
                isLoaded = true
            )
            
            Log.d(TAG, "Zone stats: ${stats.postCount} posts, ${stats.sellerCount} sellers")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading zone stats: ${e.message}")
            // No es crítico, dejar los defaults
            _zoneStats.value = ZoneStats(isLoaded = true)
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // POPULAR SEARCHES (desde Supabase)
    // ══════════════════════════════════════════════════════════════════════════
    
    suspend fun loadPopularSearches(city: String? = null) {
        try {
            val results = SupabaseClient.database.from("zone_popular_searches")
                .select {
                    order("search_count", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(12)
                }
                .decodeList<PopularSearchDB>()
                .let { all ->
                    // Priorizar búsquedas de la ciudad del usuario, luego globales
                    if (!city.isNullOrBlank()) {
                        val local = all.filter { it.city.equals(city, ignoreCase = true) }
                        val global = all.filter { it.city.isNullOrBlank() }
                        (local + global).take(8)
                    } else {
                        all.take(8)
                    }
                }
            
            _popularSearches.value = results.map { 
                PopularSearch(query = it.query, iconName = it.iconName ?: "Search")
            }
            
            Log.d(TAG, "Loaded ${results.size} popular searches")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading popular searches: ${e.message}")
            // Fallback hardcoded si Supabase falla
            _popularSearches.value = DEFAULT_POPULAR_SEARCHES
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // RECENT SEARCHES (SharedPreferences)
    // ══════════════════════════════════════════════════════════════════════════
    
    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        
        val current = _recentSearches.value.toMutableList()
        // Remover si ya existe (para moverlo al inicio)
        current.removeAll { it.query.equals(query, ignoreCase = true) }
        // Agregar al inicio
        current.add(0, RecentSearch(query = query, timestamp = System.currentTimeMillis()))
        // Limitar tamaño
        val trimmed = current.take(MAX_RECENT_SEARCHES)
        _recentSearches.value = trimmed
        saveRecentSearches(trimmed)
    }
    
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }
    
    fun removeRecentSearch(query: String) {
        val updated = _recentSearches.value.filter { !it.query.equals(query, ignoreCase = true) }
        _recentSearches.value = updated
        saveRecentSearches(updated)
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // SEARCH QUERY BUILDER (combina zona + filtros en un query)
    // ══════════════════════════════════════════════════════════════════════════
    
    fun buildSearchQuery(
        baseQuery: String,
        zoneType: String,
        filters: Set<String>,
        distanceKm: Float
    ): String {
        val parts = mutableListOf<String>()
        if (baseQuery.isNotBlank()) parts.add(baseQuery)
        
        // Agregar contexto de zona al query
        val location = _locationState.value
        when (zoneType) {
            "nearby" -> {
                if (location.city.isNotBlank()) {
                    parts.add("cerca de ${location.city}")
                }
            }
            "city" -> {
                if (location.city.isNotBlank()) {
                    parts.add("en ${location.city}")
                }
            }
            "region" -> {
                if (location.stateProvince.isNotBlank()) {
                    parts.add("en ${location.stateProvince}")
                }
            }
            "pickup" -> {
                parts.add("retiro en persona")
            }
            // "national" no agrega filtro
        }
        
        return parts.joinToString(" ")
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // CACHE & PERSISTENCE
    // ══════════════════════════════════════════════════════════════════════════
    
    private fun cacheLocation(state: ZoneLocationState) {
        prefs.edit()
            .putString(KEY_CACHED_CITY, state.city)
            .putString(KEY_CACHED_COUNTRY, state.country)
            .putString(KEY_CACHED_STATE, state.stateProvince)
            .putFloat(KEY_CACHED_LAT, state.latitude.toFloat())
            .putFloat(KEY_CACHED_LON, state.longitude.toFloat())
            .putString(KEY_CACHED_ADDRESS, state.formattedAddress)
            .putLong(KEY_CACHE_TIMESTAMP, state.timestamp)
            .apply()
    }
    
    private fun loadCachedLocation() {
        val timestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0L)
        if (timestamp == 0L) return
        
        val city = prefs.getString(KEY_CACHED_CITY, "") ?: ""
        if (city.isBlank()) return
        
        _locationState.value = ZoneLocationState(
            isLoaded = true,
            latitude = prefs.getFloat(KEY_CACHED_LAT, 0f).toDouble(),
            longitude = prefs.getFloat(KEY_CACHED_LON, 0f).toDouble(),
            city = city,
            stateProvince = prefs.getString(KEY_CACHED_STATE, "") ?: "",
            country = prefs.getString(KEY_CACHED_COUNTRY, "") ?: "",
            formattedAddress = prefs.getString(KEY_CACHED_ADDRESS, "") ?: "",
            timestamp = timestamp
        )
        
        Log.d(TAG, "Loaded cached location: $city")
    }
    
    private fun saveRecentSearches(searches: List<RecentSearch>) {
        val serialized = searches.joinToString("|") { "${it.query}::${it.timestamp}" }
        prefs.edit().putString(KEY_RECENT_SEARCHES, serialized).apply()
    }
    
    private fun loadRecentSearches() {
        val serialized = prefs.getString(KEY_RECENT_SEARCHES, "") ?: ""
        if (serialized.isBlank()) return
        
        _recentSearches.value = serialized.split("|").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size == 2) {
                RecentSearch(
                    query = parts[0],
                    timestamp = parts[1].toLongOrNull() ?: 0L
                )
            } else null
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // DEFAULTS
    // ══════════════════════════════════════════════════════════════════════════
    
    private val DEFAULT_POPULAR_SEARCHES = listOf(
        PopularSearch("iPhone", "PhoneIphone"),
        PopularSearch("Zapatillas Nike", "Hiking"),
        PopularSearch("PlayStation", "SportsEsports"),
        PopularSearch("Bicicleta", "DirectionsBike"),
        PopularSearch("Laptop", "Laptop"),
        PopularSearch("Auriculares", "Headphones")
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// DATA CLASSES
// ══════════════════════════════════════════════════════════════════════════════

data class ZoneLocationState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val stateProvince: String = "",
    val country: String = "",
    val formattedAddress: String = "",
    val error: String? = null,
    val timestamp: Long = 0L
) {
    fun isStale(): Boolean = System.currentTimeMillis() - timestamp > 30 * 60 * 1000L
    
    val displayLocation: String get() = when {
        city.isNotBlank() && country.isNotBlank() -> "$city, $country"
        city.isNotBlank() -> city
        country.isNotBlank() -> country
        else -> "Detectando ubicación..."
    }
}

data class ZoneStats(
    val postCount: Int = 0,
    val sellerCount: Int = 0,
    val isLoaded: Boolean = false
)

data class PopularSearch(
    val query: String,
    val iconName: String = "Search"
)

data class RecentSearch(
    val query: String,
    val timestamp: Long = 0L
) {
    val timeAgo: String get() {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace ${minutes}min"
            hours < 24 -> "Hace ${hours}h"
            days < 7 -> "Hace ${days}d"
            else -> "Hace ${days / 7}sem"
        }
    }
}

@Serializable
private data class ZoneStatsResponse(
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("seller_count") val sellerCount: Int = 0
)

@Serializable
private data class PopularSearchDB(
    val query: String = "",
    val city: String? = null,
    @SerialName("icon_name") val iconName: String? = null,
    @SerialName("search_count") val searchCount: Int = 0
)
