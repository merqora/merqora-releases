package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.BuildConfig
import com.rendly.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * CARD PAYMENT REPOSITORY - Checkout API de Mercado Pago
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Gestiona el flujo de pago con tarjeta usando Checkout API:
 * 1. Obtener métodos de pago disponibles
 * 2. Obtener emisores (bancos) para un método de pago
 * 3. Obtener cuotas disponibles
 * 4. Tokenizar tarjeta (crear card_token)
 * 5. Procesar pago con el token
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

sealed class CardPaymentState {
    object Idle : CardPaymentState()
    object LoadingPaymentMethods : CardPaymentState()
    object TokenizingCard : CardPaymentState()
    object ProcessingPayment : CardPaymentState()
    data class PaymentSuccess(val response: PaymentResponse) : CardPaymentState()
    data class PaymentRejected(val response: PaymentResponse, val reason: String) : CardPaymentState()
    data class PaymentPending(val response: PaymentResponse) : CardPaymentState()
    data class Error(val message: String) : CardPaymentState()
}

object CardPaymentRepository {
    private const val TAG = "CardPaymentRepository"
    
    // URLs de la API de Mercado Pago
    private const val MP_API_BASE = "https://api.mercadopago.com/v1"
    
    // Public Key de Mercado Pago (para tokenización del lado del cliente)
    // IMPORTANTE: Para tarjetas de prueba, usar credenciales TEST- (no APP_USR-)
    // Obtener desde: https://www.mercadopago.com.uy/developers/panel/app -> Credenciales de prueba
    private val MP_PUBLIC_KEY = BuildConfig.MP_PUBLIC_KEY.ifEmpty { "TEST-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" }
    
    // Access Token para operaciones del servidor (usar Edge Function en producción)
    // IMPORTANTE: Para tarjetas de prueba, usar credenciales TEST- (no APP_USR-)
    private val MP_ACCESS_TOKEN = BuildConfig.MP_ACCESS_TOKEN.ifEmpty { "TEST-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" }
    
    // Validar si estamos usando credenciales de test
    private val isUsingTestCredentials: Boolean
        get() = MP_PUBLIC_KEY.startsWith("TEST-") || MP_ACCESS_TOKEN.startsWith("TEST-")
    
    private val isUsingProductionCredentials: Boolean
        get() = MP_PUBLIC_KEY.startsWith("APP_USR-") || MP_ACCESS_TOKEN.startsWith("APP_USR-")
    
    // Modo sandbox: simular pagos cuando las credenciales son placeholder o de test
    private val isSandboxMode: Boolean
        get() = !isUsingProductionCredentials
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Estado del pago
    private val _paymentState = MutableStateFlow<CardPaymentState>(CardPaymentState.Idle)
    val paymentState: StateFlow<CardPaymentState> = _paymentState.asStateFlow()
    
    // Métodos de pago disponibles
    private val _paymentMethods = MutableStateFlow<List<PaymentMethodInfo>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethodInfo>> = _paymentMethods.asStateFlow()
    
    // Cuotas disponibles
    private val _installments = MutableStateFlow<List<PayerCost>>(emptyList())
    val installments: StateFlow<List<PayerCost>> = _installments.asStateFlow()
    
    /**
     * Obtener métodos de pago disponibles
     */
    suspend fun getPaymentMethods(): Result<List<PaymentMethodInfo>> = withContext(Dispatchers.IO) {
        try {
            _paymentState.value = CardPaymentState.LoadingPaymentMethods
            
            val request = Request.Builder()
                .url("$MP_API_BASE/payment_methods")
                .addHeader("Authorization", "Bearer $MP_ACCESS_TOKEN")
                .get()
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Respuesta vacía")
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Error obteniendo métodos de pago: ${response.code} - $responseBody")
                throw Exception("Error del servidor: ${response.code}")
            }
            
            val methods = json.decodeFromString<List<PaymentMethodInfo>>(responseBody)
            
            // Filtrar solo tarjetas de crédito/débito
            val cardMethods = methods.filter { 
                it.paymentTypeId in listOf("credit_card", "debit_card") && 
                it.status == "active" 
            }
            
            _paymentMethods.value = cardMethods
            _paymentState.value = CardPaymentState.Idle
            
            Log.d(TAG, "Métodos de pago obtenidos: ${cardMethods.size}")
            Result.success(cardMethods)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo métodos de pago: ${e.message}", e)
            _paymentState.value = CardPaymentState.Error(e.message ?: "Error desconocido")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener emisores (bancos) para un método de pago
     */
    suspend fun getIssuers(paymentMethodId: String): Result<List<Issuer>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$MP_API_BASE/payment_methods/issuers?payment_method_id=$paymentMethodId")
                .addHeader("Authorization", "Bearer $MP_ACCESS_TOKEN")
                .get()
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Respuesta vacía")
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Error obteniendo emisores: ${response.code} - $responseBody")
                throw Exception("Error del servidor: ${response.code}")
            }
            
            val issuers = json.decodeFromString<List<Issuer>>(responseBody)
            Log.d(TAG, "Emisores obtenidos para $paymentMethodId: ${issuers.size}")
            
            Result.success(issuers)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo emisores: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtener cuotas disponibles para un monto y BIN de tarjeta
     */
    suspend fun getInstallments(
        amount: Double,
        bin: String, // Primeros 6 dígitos de la tarjeta
        paymentMethodId: String? = null,
        issuerId: String? = null
    ): Result<List<PayerCost>> = withContext(Dispatchers.IO) {
        try {
            val url = buildString {
                append("$MP_API_BASE/payment_methods/installments?")
                append("amount=$amount")
                append("&bin=$bin")
                paymentMethodId?.let { append("&payment_method_id=$it") }
                issuerId?.let { append("&issuer.id=$it") }
            }
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $MP_ACCESS_TOKEN")
                .get()
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Respuesta vacía")
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Error obteniendo cuotas: ${response.code} - $responseBody")
                throw Exception("Error del servidor: ${response.code}")
            }
            
            val installmentsResponse = json.decodeFromString<List<InstallmentsResponse>>(responseBody)
            val payerCosts = installmentsResponse.firstOrNull()?.payerCosts ?: emptyList()
            
            _installments.value = payerCosts
            Log.d(TAG, "Cuotas obtenidas: ${payerCosts.size}")
            
            Result.success(payerCosts)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo cuotas: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Crear token de tarjeta (tokenizar)
     * Esto se hace del lado del cliente usando la Public Key
     */
    suspend fun createCardToken(
        cardNumber: String,
        expirationMonth: Int,
        expirationYear: Int,
        securityCode: String,
        cardholderName: String,
        identificationType: String = "CI",
        identificationNumber: String
    ): Result<CardToken> = withContext(Dispatchers.IO) {
        try {
            _paymentState.value = CardPaymentState.TokenizingCard
            
            val cleanCardNumber = cardNumber.replace(" ", "").replace("-", "")
            
            // Validaciones
            if (!isValidCardNumber(cleanCardNumber)) {
                throw Exception("Número de tarjeta inválido")
            }
            
            if (!isValidExpirationDate(expirationMonth, expirationYear)) {
                throw Exception("Fecha de expiración inválida")
            }
            
            val cardType = detectCardType(cleanCardNumber)
            if (!isValidCVV(securityCode, cardType)) {
                throw Exception("Código de seguridad inválido")
            }
            
            // Modo sandbox: simular tokenización
            if (isSandboxMode) {
                Log.d(TAG, "🧪 Modo sandbox: simulando tokenización")
                kotlinx.coroutines.delay(800)
                val simulatedToken = CardToken(
                    id = "sandbox_tok_${System.currentTimeMillis()}",
                    publicKey = MP_PUBLIC_KEY,
                    firstSixDigits = cleanCardNumber.take(6),
                    lastFourDigits = cleanCardNumber.takeLast(4),
                    expirationMonth = expirationMonth,
                    expirationYear = expirationYear,
                    status = "active",
                    liveMode = false
                )
                _paymentState.value = CardPaymentState.Idle
                return@withContext Result.success(simulatedToken)
            }
            
            // Construir request body
            val requestBody = buildString {
                append("{")
                append("\"card_number\": \"$cleanCardNumber\",")
                append("\"expiration_month\": $expirationMonth,")
                append("\"expiration_year\": $expirationYear,")
                append("\"security_code\": \"$securityCode\",")
                append("\"cardholder\": {")
                append("\"name\": \"$cardholderName\",")
                append("\"identification\": {")
                append("\"type\": \"$identificationType\",")
                append("\"number\": \"$identificationNumber\"")
                append("}")
                append("}")
                append("}")
            }
            
            Log.d(TAG, "Tokenizando tarjeta... (Public Key: ${MP_PUBLIC_KEY.take(15)}...)")
            
            // Advertencia si se usan credenciales de producción con posibles tarjetas de prueba
            if (isUsingProductionCredentials) {
                Log.w(TAG, "⚠️ Usando credenciales de PRODUCCIÓN (APP_USR-). Las tarjetas de prueba de MP NO funcionarán. Usa credenciales TEST- para testing.")
            }
            
            val request = Request.Builder()
                .url("$MP_API_BASE/card_tokens?public_key=$MP_PUBLIC_KEY")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseBodyStr = response.body?.string() ?: throw Exception("Respuesta vacía")
            
            Log.d(TAG, "Respuesta tokenización: $responseBodyStr")
            
            if (!response.isSuccessful) {
                val errorMessage = try {
                    val errorJson = json.parseToJsonElement(responseBodyStr)
                    val cause = errorJson.jsonObject["cause"]?.jsonArray?.firstOrNull()
                        ?.jsonObject?.get("description")?.jsonPrimitive?.content
                    val message = errorJson.jsonObject["message"]?.jsonPrimitive?.content
                    val error = errorJson.jsonObject["error"]?.jsonPrimitive?.content
                    cause ?: message ?: error ?: responseBodyStr
                } catch (e: Exception) {
                    responseBodyStr
                }
                Log.e(TAG, "Error tokenizando tarjeta: ${response.code} - $errorMessage")
                
                // Mensaje más amigable según el tipo de error
                val friendlyMessage = when {
                    response.code == 400 && isUsingProductionCredentials -> 
                        "Las tarjetas de prueba no funcionan con credenciales de producción. Configurá credenciales TEST- en gradle.properties"
                    response.code == 401 -> "Credenciales de Mercado Pago inválidas"
                    response.code == 400 -> "Datos de tarjeta incorrectos: $errorMessage"
                    else -> "Error al procesar tarjeta ($errorMessage)"
                }
                throw Exception(friendlyMessage)
            }
            
            val cardToken = json.decodeFromString<CardToken>(responseBodyStr)
            Log.d(TAG, "Token creado: ${cardToken.id}")
            
            _paymentState.value = CardPaymentState.Idle
            Result.success(cardToken)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error tokenizando tarjeta: ${e.message}", e)
            _paymentState.value = CardPaymentState.Error(e.message ?: "Error desconocido")
            Result.failure(e)
        }
    }
    
    /**
     * Procesar pago con token
     * 
     * En modo TEST: llama directamente a la API de MP con el Access Token
     * En PRODUCCIÓN: debe usar la Edge Function para no exponer el Access Token
     * 
     * TODO: Cambiar USE_EDGE_FUNCTION a true cuando se deploye la Edge Function
     */
    private val USE_EDGE_FUNCTION = false // Cambiar a true en producción
    
    suspend fun processPayment(
        orderId: String,
        token: String,
        amount: Double,
        description: String,
        installments: Int,
        paymentMethodId: String,
        issuerId: String?,
        payerEmail: String,
        identificationType: String = "CI",
        identificationNumber: String
    ): Result<PaymentResponse> = withContext(Dispatchers.IO) {
        try {
            _paymentState.value = CardPaymentState.ProcessingPayment
            
            Log.d(TAG, "Procesando pago - Orden: $orderId, Monto: $amount, Cuotas: $installments, Método: $paymentMethodId")
            
            // Modo sandbox: simular pago aprobado
            if (isSandboxMode) {
                Log.d(TAG, "🧪 Modo sandbox: simulando pago aprobado")
                kotlinx.coroutines.delay(1500)
                val simulatedResponse = PaymentResponse(
                    id = System.currentTimeMillis(),
                    status = "approved",
                    statusDetail = "accredited",
                    paymentMethodId = paymentMethodId,
                    paymentTypeId = "credit_card",
                    transactionAmount = amount,
                    installments = installments,
                    currencyId = "UYU",
                    description = description,
                    externalReference = orderId,
                    liveMode = false,
                    dateCreated = java.time.Instant.now().toString(),
                    dateApproved = java.time.Instant.now().toString()
                )
                Log.d(TAG, "✅ Pago sandbox aprobado: ${simulatedResponse.id}")
                _paymentState.value = CardPaymentState.PaymentSuccess(simulatedResponse)
                return@withContext Result.success(simulatedResponse)
            }
            
            val responseBodyStr: String
            
            if (USE_EDGE_FUNCTION) {
                // ── Modo PRODUCCIÓN: Edge Function ──
                responseBodyStr = processViaEdgeFunction(
                    orderId, token, amount, description, installments,
                    paymentMethodId, issuerId, payerEmail, identificationType, identificationNumber
                )
            } else {
                // ── Modo TEST: API directa de MP ──
                responseBodyStr = processDirectly(
                    orderId, token, amount, description, installments,
                    paymentMethodId, issuerId, payerEmail, identificationType, identificationNumber
                )
            }
            
            Log.d(TAG, "Respuesta pago: $responseBodyStr")
            
            val paymentResponse = json.decodeFromString<PaymentResponse>(responseBodyStr)
            
            // Actualizar estado según resultado
            when (paymentResponse.status) {
                "approved" -> {
                    Log.d(TAG, "✅ Pago aprobado: ${paymentResponse.id}")
                    _paymentState.value = CardPaymentState.PaymentSuccess(paymentResponse)
                }
                "rejected" -> {
                    val reason = getRejectReason(paymentResponse.statusDetail ?: "")
                    Log.d(TAG, "❌ Pago rechazado: ${paymentResponse.statusDetail} - $reason")
                    _paymentState.value = CardPaymentState.PaymentRejected(paymentResponse, reason)
                }
                "pending", "in_process" -> {
                    Log.d(TAG, "⏳ Pago pendiente: ${paymentResponse.status}")
                    _paymentState.value = CardPaymentState.PaymentPending(paymentResponse)
                }
                else -> {
                    Log.w(TAG, "⚠️ Estado desconocido: ${paymentResponse.status}")
                    _paymentState.value = CardPaymentState.Error("Estado de pago desconocido: ${paymentResponse.status}")
                }
            }
            
            Result.success(paymentResponse)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando pago: ${e.message}", e)
            _paymentState.value = CardPaymentState.Error(e.message ?: "Error desconocido")
            Result.failure(e)
        }
    }
    
    /**
     * Procesar pago directamente contra la API de MP (para testing)
     */
    private fun processDirectly(
        orderId: String,
        token: String,
        amount: Double,
        description: String,
        installments: Int,
        paymentMethodId: String,
        issuerId: String?,
        payerEmail: String,
        identificationType: String,
        identificationNumber: String
    ): String {
        val requestBody = buildString {
            append("{")
            append("\"token\": \"$token\",")
            append("\"transaction_amount\": $amount,")
            append("\"description\": \"$description\",")
            append("\"installments\": $installments,")
            append("\"payment_method_id\": \"$paymentMethodId\",")
            issuerId?.let { append("\"issuer_id\": $it,") }
            append("\"statement_descriptor\": \"Merqora\",")
            append("\"external_reference\": \"$orderId\",")
            append("\"payer\": {")
            append("\"email\": \"$payerEmail\",")
            append("\"identification\": {")
            append("\"type\": \"$identificationType\",")
            append("\"number\": \"$identificationNumber\"")
            append("}")
            append("}")
            append("}")
        }
        
        Log.d(TAG, "Request body (directo): $requestBody")
        
        val request = Request.Builder()
            .url("$MP_API_BASE/payments")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $MP_ACCESS_TOKEN")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Idempotency-Key", "$orderId-${System.currentTimeMillis()}")
            .build()
        
        val response = okHttpClient.newCall(request).execute()
        val responseStr = response.body?.string() ?: throw Exception("Respuesta vacía del servidor")
        
        Log.d(TAG, "Respuesta MP (${response.code}): $responseStr")
        
        if (!response.isSuccessful) {
            // Intentar extraer mensaje de error amigable
            val errorMsg = try {
                val errorJson = json.parseToJsonElement(responseStr)
                val message = errorJson.jsonObject["message"]?.jsonPrimitive?.content
                val cause = errorJson.jsonObject["cause"]?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("description")?.jsonPrimitive?.content
                cause ?: message ?: "Error ${response.code}"
            } catch (e: Exception) {
                "Error ${response.code}: $responseStr"
            }
            throw Exception(errorMsg)
        }
        
        return responseStr
    }
    
    /**
     * Procesar pago via Edge Function de Supabase (para producción)
     */
    private fun processViaEdgeFunction(
        orderId: String,
        token: String,
        amount: Double,
        description: String,
        installments: Int,
        paymentMethodId: String,
        issuerId: String?,
        payerEmail: String,
        identificationType: String,
        identificationNumber: String
    ): String {
        val functionUrl = "${BuildConfig.SUPABASE_URL}/functions/v1/process-card-payment"
        
        val requestBody = buildString {
            append("{")
            append("\"order_id\": \"$orderId\",")
            append("\"token\": \"$token\",")
            append("\"transaction_amount\": $amount,")
            append("\"description\": \"$description\",")
            append("\"installments\": $installments,")
            append("\"payment_method_id\": \"$paymentMethodId\",")
            issuerId?.let { append("\"issuer_id\": $it,") }
            append("\"payer_email\": \"$payerEmail\",")
            append("\"payer_identification\": {")
            append("\"type\": \"$identificationType\",")
            append("\"number\": \"$identificationNumber\"")
            append("}")
            append("}")
        }
        
        Log.d(TAG, "Request body (Edge Function): $requestBody")
        
        val request = Request.Builder()
            .url(functionUrl)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        val response = okHttpClient.newCall(request).execute()
        val responseStr = response.body?.string() ?: throw Exception("Respuesta vacía del servidor")
        
        Log.d(TAG, "Respuesta Edge Function (${response.code}): $responseStr")
        
        if (!response.isSuccessful) {
            val errorMsg = try {
                val errorJson = json.parseToJsonElement(responseStr)
                errorJson.jsonObject["error"]?.jsonPrimitive?.content ?: "Error ${response.code}"
            } catch (e: Exception) {
                "Error ${response.code}: $responseStr"
            }
            throw Exception(errorMsg)
        }
        
        return responseStr
    }
    
    /**
     * Obtener mensaje amigable para el motivo de rechazo
     */
    private fun getRejectReason(statusDetail: String): String {
        return when (statusDetail) {
            "cc_rejected_bad_filled_card_number" -> "Revisá el número de tarjeta"
            "cc_rejected_bad_filled_date" -> "Revisá la fecha de vencimiento"
            "cc_rejected_bad_filled_other" -> "Revisá los datos de la tarjeta"
            "cc_rejected_bad_filled_security_code" -> "Revisá el código de seguridad"
            "cc_rejected_blacklist" -> "Tu tarjeta no puede ser utilizada"
            "cc_rejected_call_for_authorize" -> "Contactá a tu banco para autorizar el pago"
            "cc_rejected_card_disabled" -> "Contactá a tu banco para activar tu tarjeta"
            "cc_rejected_card_error" -> "No se pudo procesar tu tarjeta"
            "cc_rejected_duplicated_payment" -> "Ya realizaste un pago similar"
            "cc_rejected_high_risk" -> "Tu pago fue rechazado por seguridad"
            "cc_rejected_insufficient_amount" -> "Fondos insuficientes"
            "cc_rejected_invalid_installments" -> "No se puede pagar en cuotas"
            "cc_rejected_max_attempts" -> "Alcanzaste el límite de intentos"
            "cc_rejected_other_reason" -> "Tu banco rechazó el pago"
            else -> "El pago fue rechazado. Probá con otra tarjeta"
        }
    }
    
    /**
     * Detectar método de pago por BIN de tarjeta
     */
    suspend fun guessPaymentMethod(bin: String): Result<PaymentMethodInfo?> = withContext(Dispatchers.IO) {
        try {
            if (bin.length < 6) return@withContext Result.success(null)
            
            val request = Request.Builder()
                .url("$MP_API_BASE/payment_methods/search?bins=$bin")
                .addHeader("Authorization", "Bearer $MP_ACCESS_TOKEN")
                .get()
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Respuesta vacía")
            
            if (!response.isSuccessful) {
                return@withContext Result.success(null)
            }
            
            val methods = json.decodeFromString<List<PaymentMethodInfo>>(responseBody)
            Result.success(methods.firstOrNull())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detectando método de pago: ${e.message}")
            Result.success(null)
        }
    }
    
    /**
     * Resetear estado
     */
    fun resetState() {
        _paymentState.value = CardPaymentState.Idle
        _installments.value = emptyList()
    }
}
