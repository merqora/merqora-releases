package com.rendly.app.data.remote

import android.util.Log
import com.rendly.app.BuildConfig
import com.rendly.app.data.model.AddressPrediction
import com.rendly.app.data.model.GeocodingResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * MAPBOX SERVICE - Geocoding, Reverse Geocoding, and Address Search
 * ══════════════════════════════════════════════════════════════════════════════
 */
@Singleton
class MapboxService @Inject constructor() {
    
    private val TAG = "MapboxService"
    private val accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    companion object {
        private const val GEOCODING_BASE_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places"
        private const val SEARCH_BASE_URL = "https://api.mapbox.com/search/searchbox/v1"
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // ADDRESS SEARCH (Autocomplete)
    // ══════════════════════════════════════════════════════════════════════════
    
    suspend fun searchAddresses(
        query: String,
        proximity: Pair<Double, Double>? = null, // lon, lat
        country: String = "uy", // Uruguay by default
        limit: Int = 5
    ): List<AddressPrediction> = withContext(Dispatchers.IO) {
        if (query.length < 3) return@withContext emptyList()
        
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val proximityParam = proximity?.let { "${it.first},${it.second}" } ?: "-56.1645,-34.9011" // Montevideo, Uruguay
            
            val url = "$GEOCODING_BASE_URL/$encodedQuery.json" +
                "?access_token=$accessToken" +
                "&country=$country" +
                "&proximity=$proximityParam" +
                "&limit=$limit" +
                "&types=address,poi,place" +
                "&language=es"
            
            Log.d(TAG, "Searching addresses: $query")
            
            val response: MapboxGeocodingResponse = client.get(url).body()
            
            response.features.map { feature ->
                AddressPrediction(
                    placeId = feature.id ?: "",
                    primaryText = feature.text ?: "",
                    secondaryText = feature.placeName?.replace("${feature.text}, ", "") ?: "",
                    fullText = feature.placeName ?: feature.text ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching addresses", e)
            emptyList()
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // GEOCODING (Address -> Coordinates)
    // ══════════════════════════════════════════════════════════════════════════
    
    suspend fun geocodeAddress(address: String): GeocodingResult? = withContext(Dispatchers.IO) {
        try {
            val encodedAddress = URLEncoder.encode(address, "UTF-8")
            
            val url = "$GEOCODING_BASE_URL/$encodedAddress.json" +
                "?access_token=$accessToken" +
                "&country=uy" +
                "&limit=1" +
                "&types=address,poi,place" +
                "&language=es"
            
            Log.d(TAG, "Geocoding address: $address")
            
            val response: MapboxGeocodingResponse = client.get(url).body()
            
            response.features.firstOrNull()?.let { feature ->
                parseFeatureToGeocodingResult(feature)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error geocoding address", e)
            null
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // REVERSE GEOCODING (Coordinates -> Address)
    // ══════════════════════════════════════════════════════════════════════════
    
    suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodingResult? = 
        withContext(Dispatchers.IO) {
            try {
                val url = "$GEOCODING_BASE_URL/$longitude,$latitude.json" +
                    "?access_token=$accessToken" +
                    "&types=address,poi,place" +
                    "&language=es" +
                    "&limit=1"
                
                Log.d(TAG, "Reverse geocoding: $latitude, $longitude")
                
                val response: MapboxGeocodingResponse = client.get(url).body()
                
                response.features.firstOrNull()?.let { feature ->
                    parseFeatureToGeocodingResult(feature, latitude, longitude)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reverse geocoding", e)
                null
            }
        }
    
    // ══════════════════════════════════════════════════════════════════════════
    // STATIC MAP URL
    // ══════════════════════════════════════════════════════════════════════════
    
    fun getStaticMapUrl(
        latitude: Double,
        longitude: Double,
        zoom: Int = 15,
        width: Int = 600,
        height: Int = 300,
        markerColor: String = "ff0000"
    ): String {
        return "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/" +
            "pin-l+$markerColor($longitude,$latitude)/" +
            "$longitude,$latitude,$zoom,0/" +
            "${width}x$height@2x" +
            "?access_token=$accessToken"
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════════
    
    private fun parseFeatureToGeocodingResult(
        feature: MapboxFeature,
        overrideLat: Double? = null,
        overrideLon: Double? = null
    ): GeocodingResult {
        val context = feature.context ?: emptyList()
        
        // Extract address components from context
        val neighborhood = context.find { it.id?.startsWith("neighborhood") == true }?.text
        val locality = context.find { it.id?.startsWith("locality") == true }?.text
        val place = context.find { it.id?.startsWith("place") == true }?.text
        val district = context.find { it.id?.startsWith("district") == true }?.text
        val region = context.find { it.id?.startsWith("region") == true }?.text
        val postcode = context.find { it.id?.startsWith("postcode") == true }?.text
        val country = context.find { it.id?.startsWith("country") == true }?.text
        val countryCode = context.find { it.id?.startsWith("country") == true }?.shortCode?.uppercase()
        
        // Parse address and street number from feature
        val addressText = feature.address ?: ""
        val streetName = feature.text ?: ""
        
        return GeocodingResult(
            formattedAddress = feature.placeName ?: "",
            latitude = overrideLat ?: (feature.center?.getOrNull(1) ?: 0.0),
            longitude = overrideLon ?: (feature.center?.getOrNull(0) ?: 0.0),
            streetAddress = streetName,
            streetNumber = addressText,
            neighborhood = neighborhood ?: locality,
            city = place ?: district ?: "",
            stateProvince = region,
            postalCode = postcode,
            country = country ?: "Uruguay",
            countryCode = countryCode ?: "UY",
            provider = "mapbox",
            placeId = feature.id
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// MAPBOX API RESPONSE MODELS
// ══════════════════════════════════════════════════════════════════════════════

@Serializable
data class MapboxGeocodingResponse(
    val type: String? = null,
    val features: List<MapboxFeature> = emptyList(),
    val attribution: String? = null
)

@Serializable
data class MapboxFeature(
    val id: String? = null,
    val type: String? = null,
    val text: String? = null,
    @SerialName("place_name")
    val placeName: String? = null,
    val address: String? = null,
    val center: List<Double>? = null,
    val geometry: MapboxGeometry? = null,
    val context: List<MapboxContext>? = null,
    val properties: MapboxProperties? = null
)

@Serializable
data class MapboxGeometry(
    val type: String? = null,
    val coordinates: List<Double>? = null
)

@Serializable
data class MapboxContext(
    val id: String? = null,
    val text: String? = null,
    @SerialName("short_code")
    val shortCode: String? = null,
    val wikidata: String? = null
)

@Serializable
data class MapboxProperties(
    val accuracy: String? = null,
    val address: String? = null,
    val category: String? = null,
    val maki: String? = null
)
