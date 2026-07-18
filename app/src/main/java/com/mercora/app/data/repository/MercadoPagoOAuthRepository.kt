package com.mercora.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.mercora.app.BuildConfig
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object MercadoPagoOAuthRepository {
    private const val TAG = "MercadoPagoOAuth"

    private val _connectionState = MutableStateFlow<MpConnectionState>(MpConnectionState.Loading)
    val connectionState: StateFlow<MpConnectionState> = _connectionState.asStateFlow()

    private val _oauthResult = MutableStateFlow<OAuthResult?>(null)
    val oauthResult: StateFlow<OAuthResult?> = _oauthResult.asStateFlow()

    fun loadConnection() {
        _connectionState.value = MpConnectionState.Loading
    }

    suspend fun fetchConnection(userId: String): MpConnectionState = withContext(Dispatchers.IO) {
        try {
            val conn = SupabaseClient.database
                .from("mercadopago_connections")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<MercadoPagoConnectionDTO>()

            if (conn != null && conn.conexionEstado == "activa") {
                val state = MpConnectionState.Connected(
                    mpUserId = conn.mercadopagoUserId,
                    connectedSince = conn.conexionIniciadaEn
                )
                _connectionState.value = state
                state
            } else {
                val state = MpConnectionState.Disconnected
                _connectionState.value = state
                state
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching MP connection: ${e.message}")
            val state = MpConnectionState.Error(e.message ?: "Error al verificar conexiÃ³n")
            _connectionState.value = state
            state
        }
    }

    suspend fun disconnectMercadoPago(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("mercadopago_connections")
                .update(buildJsonObject {
                    put("conexion_estado", "revocada")
                    put("conexion_actualizada_en", java.time.Instant.now().toString())
                }) {
                    filter { eq("user_id", userId) }
                }
            _connectionState.value = MpConnectionState.Disconnected
            Log.d(TAG, "âœ… ConexiÃ³n MP revocada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error revocando conexiÃ³n MP: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun exchangeOAuthCode(code: String): Result<OAuthExchangeResponse> = withContext(Dispatchers.IO) {
        try {
            val functionUrl = "${BuildConfig.SUPABASE_URL}/functions/v1/mercadopago-oauth-exchange"

            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val requestBody = buildJsonObject {
                put("code", code)
            }.toString()
                .toRequestBody("application/json".toMediaType())

            val request = okhttp3.Request.Builder()
                .url(functionUrl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: throw Exception("Respuesta vacÃ­a")
            Log.d(TAG, "OAuth exchange response: $responseText")

            val result = Json { ignoreUnknownKeys = true }
                .decodeFromString<OAuthExchangeResponse>(responseText)

            if (result.success) {
                _connectionState.value = MpConnectionState.Connected(
                    mpUserId = result.mercadopagoUserId,
                    connectedSince = java.time.Instant.now().toString()
                )
                _oauthResult.value = OAuthResult.Success(result.mercadopagoUserId)
                Result.success(result)
            } else {
                _oauthResult.value = OAuthResult.Error(result.error ?: "Error desconocido")
                Result.failure(Exception(result.error ?: "Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en OAuth exchange: ${e.message}")
            _oauthResult.value = OAuthResult.Error(e.message ?: "Error de conexiÃ³n")
            Result.failure(e)
        }
    }

    fun buildOAuthUrl(clientId: String, state: String): String {
        val redirectUri = "vinzay://mp-oauth/callback"
        return "https://auth.mercadopago.com.uy/authorization" +
            "?client_id=$clientId" +
            "&response_type=code" +
            "&platform_id=mp" +
            "&redirect_uri=${Uri.encode(redirectUri)}" +
            "&state=$state"
    }

    fun handleOAuthRedirect(intent: Intent): String? {
        val data = intent.data ?: return null
        if (data.scheme != "vinzay" || data.host != "mp-oauth") return null
        return data.getQueryParameter("code")
    }

    fun resetOAuthResult() {
        _oauthResult.value = null
    }

    fun resetState() {
        _connectionState.value = MpConnectionState.Loading
        _oauthResult.value = null
    }

    private fun String.toRequestBody(contentType: String, mediaType: okhttp3.MediaType): okhttp3.RequestBody {
        return this.toByteArray().toRequestBody(mediaType)
    }
}

@Serializable
data class OAuthExchangeResponse(
    val success: Boolean = false,
    @SerialName("mercadopago_user_id") val mercadopagoUserId: String = "",
    @SerialName("public_key") val publicKey: String? = null,
    @SerialName("live_mode") val liveMode: Boolean = false,
    @SerialName("expires_at") val expiresAt: String? = null,
    val error: String? = null
)

sealed class OAuthResult {
    data class Success(val mpUserId: String) : OAuthResult()
    data class Error(val message: String) : OAuthResult()
}

sealed class MpConnectionState {
    object Loading : MpConnectionState()
    object Disconnected : MpConnectionState()
    data class Connected(val mpUserId: String, val connectedSince: String) : MpConnectionState()
    data class Error(val message: String) : MpConnectionState()
}

@Serializable
data class MercadoPagoConnectionDTO(
    @SerialName("user_id") val userId: String = "",
    @SerialName("mercadopago_user_id") val mercadopagoUserId: String = "",
    @SerialName("conexion_estado") val conexionEstado: String = "",
    @SerialName("conexion_iniciada_en") val conexionIniciadaEn: String = "",
    @SerialName("public_key") val publicKey: String? = null
)
