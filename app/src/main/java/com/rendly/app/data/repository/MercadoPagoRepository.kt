package com.rendly.app.data.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.rendly.app.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MERCADO PAGO REPOSITORY
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Gestiona la integración con Mercado Pago para pagos en Uruguay.
 * Usa Checkout Pro con Custom Tabs para el flujo de pago.
 * 
 * Flujo:
 * 1. Crear orden en nuestra DB
 * 2. Llamar a Edge Function para crear preferencia en MP
 * 3. Abrir Custom Tab con la URL de pago
 * 4. Manejar retorno via Deep Link
 * 5. Verificar estado del pago
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

// Respuesta de la Edge Function
@Serializable
data class PreferenceResponse(
    @SerialName("preference_id") val preferenceId: String,
    @SerialName("init_point") val initPoint: String,
    @SerialName("sandbox_init_point") val sandboxInitPoint: String? = null,
    @SerialName("production_init_point") val productionInitPoint: String? = null,
    val error: String? = null
)

// Item para enviar a Mercado Pago
@Serializable
data class MPItem(
    val id: String,
    val title: String,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("currency_id") val currencyId: String = "UYU",
    @SerialName("picture_url") val pictureUrl: String? = null,
    val description: String? = null
)

// Estado del pago
sealed class PaymentState {
    object Idle : PaymentState()
    object CreatingPreference : PaymentState()
    data class ReadyToPay(val initPoint: String, val preferenceId: String) : PaymentState()
    object WaitingForPayment : PaymentState()
    data class PaymentSuccess(val orderId: String, val paymentId: String?) : PaymentState()
    data class PaymentPending(val orderId: String) : PaymentState()
    data class PaymentFailed(val orderId: String, val reason: String?) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

object MercadoPagoRepository {
    private const val TAG = "MercadoPagoRepository"
    
    // Configuración - cambiar a false para producción
    private const val USE_SANDBOX = true
    
    // Ya no se necesita fix de URL - OkHttp maneja SSL de .com.uy correctamente
    private fun fixSandboxUrl(url: String): String = url
    
    // Deep link scheme (debe coincidir con AndroidManifest)
    const val DEEP_LINK_SCHEME = "Merqora"
    const val DEEP_LINK_HOST_SUCCESS = "payment/success"
    const val DEEP_LINK_HOST_FAILURE = "payment/failure"
    const val DEEP_LINK_HOST_PENDING = "payment/pending"
    
    // Estado del pago
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()
    
    // Orden actual en proceso de pago
    private var currentOrderId: String? = null
    
    /**
     * Crear preferencia de pago en Mercado Pago
     * @param orderId ID de la orden en nuestra DB
     * @param items Lista de items a pagar
     * @param payerEmail Email del comprador (opcional)
     * @return URL de pago (init_point) o null si falla
     */
    suspend fun createPaymentPreference(
        orderId: String,
        items: List<MPItem>,
        payerEmail: String? = null
    ): Result<PreferenceResponse> = withContext(Dispatchers.IO) {
        try {
            _paymentState.value = PaymentState.CreatingPreference
            currentOrderId = orderId
            
            Log.d(TAG, "Creando preferencia para orden: $orderId con ${items.size} items")
            
            // Construir JSON para la Edge Function
            val requestBody = buildJsonObject {
                put("order_id", orderId)
                put("items", buildJsonArray {
                    items.forEach { item ->
                        add(buildJsonObject {
                            put("id", item.id)
                            put("title", item.title)
                            put("quantity", item.quantity)
                            put("unit_price", item.unitPrice)
                            put("currency_id", item.currencyId)
                            item.pictureUrl?.let { put("picture_url", it) }
                            item.description?.let { put("description", it) }
                        })
                    }
                })
                payerEmail?.let { put("payer_email", it) }
            }
            
            // Llamar a la Edge Function de Supabase usando OkHttp (maneja SSL correctamente)
            Log.d(TAG, "Llamando Edge Function create-mp-preference...")
            val functionUrl = "${BuildConfig.SUPABASE_URL}/functions/v1/create-mp-preference"
            
            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            val requestBodyOk = requestBody.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = okhttp3.Request.Builder()
                .url(functionUrl)
                .post(requestBodyOk)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: throw Exception("Respuesta vacía")
            
            Log.d(TAG, "Respuesta de Edge Function: $responseText")
            
            // Parsear respuesta
            val preferenceResponse = kotlinx.serialization.json.Json { 
                ignoreUnknownKeys = true 
            }.decodeFromString<PreferenceResponse>(responseText)
            
            if (preferenceResponse.error != null) {
                Log.e(TAG, "Error de MP: ${preferenceResponse.error}")
                _paymentState.value = PaymentState.Error(preferenceResponse.error)
                return@withContext Result.failure(Exception(preferenceResponse.error))
            }
            
            // Determinar URL según entorno
            val rawInitPoint = if (USE_SANDBOX) {
                preferenceResponse.sandboxInitPoint ?: preferenceResponse.initPoint
            } else {
                preferenceResponse.productionInitPoint ?: preferenceResponse.initPoint
            }
            
            // Aplicar fix para URLs del sandbox de Uruguay que tienen problemas SSL
            val initPoint = if (USE_SANDBOX) fixSandboxUrl(rawInitPoint) else rawInitPoint
            
            Log.d(TAG, "Preferencia creada: ${preferenceResponse.preferenceId}")
            Log.d(TAG, "URL de pago (fixed): $initPoint")
            _paymentState.value = PaymentState.ReadyToPay(initPoint, preferenceResponse.preferenceId)
            
            Result.success(preferenceResponse)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creando preferencia: ${e.message}", e)
            _paymentState.value = PaymentState.Error(e.message ?: "Error desconocido")
            Result.failure(e)
        }
    }
    
    /**
     * Iniciar el checkout NATIVO de Mercado Pago (WebView embebido)
     * @param activity Activity que recibirá el resultado
     * @param checkoutUrl URL del checkout (init_point)
     * @param orderId ID de la orden en nuestra DB
     * @param requestCode Código para identificar el resultado
     */
    fun startNativeCheckout(
        activity: Activity,
        checkoutUrl: String,
        orderId: String,
        requestCode: Int = 1001
    ) {
        try {
            Log.d(TAG, "Iniciando checkout WebView embebido")
            _paymentState.value = PaymentState.WaitingForPayment
            currentOrderId = orderId
            
            // Crear intent para la Activity con WebView
            val intent = Intent(activity, Class.forName("com.rendly.app.ui.screens.checkout.MercadoPagoCheckoutActivity")).apply {
                putExtra("checkout_url", checkoutUrl)
                putExtra("order_id", orderId)
            }
            
            activity.startActivityForResult(intent, requestCode)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando checkout WebView: ${e.message}", e)
            _paymentState.value = PaymentState.Error("No se pudo iniciar el checkout")
        }
    }
    
    /**
     * Procesar el resultado del checkout nativo
     * @param resultCode Código de resultado de la Activity
     * @param data Intent con los datos del resultado
     */
    fun handleNativeCheckoutResult(resultCode: Int, data: Intent?) {
        val orderId = data?.getStringExtra("order_id") ?: currentOrderId ?: ""
        val paymentId = data?.getStringExtra("payment_id")
        val paymentStatus = data?.getStringExtra("payment_status")
        val errorMessage = data?.getStringExtra("error_message")
        
        Log.d(TAG, "Resultado checkout nativo - resultCode: $resultCode, status: $paymentStatus")
        
        when (resultCode) {
            Activity.RESULT_OK -> {
                // Pago aprobado
                Log.d(TAG, "✅ Pago aprobado via SDK nativo")
                _paymentState.value = PaymentState.PaymentSuccess(orderId, paymentId)
            }
            1002 -> { // RESULT_PAYMENT_PENDING
                Log.d(TAG, "⏳ Pago pendiente via SDK nativo")
                _paymentState.value = PaymentState.PaymentPending(orderId)
            }
            1003 -> { // RESULT_PAYMENT_REJECTED
                Log.d(TAG, "❌ Pago rechazado via SDK nativo: $errorMessage")
                _paymentState.value = PaymentState.PaymentFailed(orderId, errorMessage)
            }
            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "⚠️ Pago cancelado via SDK nativo")
                _paymentState.value = PaymentState.PaymentFailed(orderId, errorMessage ?: "Pago cancelado")
            }
            else -> {
                Log.w(TAG, "⚠️ Resultado desconocido: $resultCode")
                _paymentState.value = PaymentState.Error(errorMessage ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Abrir el checkout de Mercado Pago en Custom Tab (FALLBACK)
     * @param context Context de Android (debe ser Activity context)
     * @param initPoint URL de pago obtenida de createPaymentPreference
     */
    fun openMercadoPagoCheckout(context: Context, initPoint: String) {
        try {
            // Aplicar fix de URL del sandbox (com.uy → com)
            val fixedUrl = if (USE_SANDBOX) fixSandboxUrl(initPoint) else initPoint
            Log.d(TAG, "Abriendo checkout: $fixedUrl (original: $initPoint)")
            _paymentState.value = PaymentState.WaitingForPayment
            
            // Colores de Mercado Pago
            val mpBlue = android.graphics.Color.parseColor("#009EE3")
            
            val colorScheme = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(mpBlue)
                .setNavigationBarColor(mpBlue)
                .build()
            
            // Crear Custom Tab con configuración completa
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(false)
                .setDefaultColorSchemeParams(colorScheme)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .build()
            
            // Forzar que abra en Custom Tab, no en navegador externo
            // Buscar paquete de Chrome o navegador con Custom Tabs
            val chromePackage = findCustomTabsPackage(context)
            if (chromePackage != null) {
                customTabsIntent.intent.setPackage(chromePackage)
                Log.d(TAG, "Usando Custom Tabs con: $chromePackage")
            } else {
                Log.w(TAG, "No se encontró navegador con Custom Tabs, usando default")
            }
            
            // Abrir la URL de pago (con fix aplicado)
            customTabsIntent.launchUrl(context, Uri.parse(fixedUrl))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo checkout: ${e.message}", e)
            _paymentState.value = PaymentState.Error("No se pudo abrir el checkout")
        }
    }
    
    /**
     * Buscar un navegador que soporte Custom Tabs
     */
    private fun findCustomTabsPackage(context: Context): String? {
        val pm = context.packageManager
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
        
        // Lista de navegadores preferidos con soporte Custom Tabs
        val preferredPackages = listOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.google.android.apps.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.brave.browser"
        )
        
        val resolvedActivities = pm.queryIntentActivities(activityIntent, PackageManager.MATCH_DEFAULT_ONLY)
        
        for (preferred in preferredPackages) {
            for (info in resolvedActivities) {
                if (info.activityInfo.packageName == preferred) {
                    return preferred
                }
            }
        }
        
        // Si no encontró preferido, usar el primero disponible
        return resolvedActivities.firstOrNull()?.activityInfo?.packageName
    }
    
    /**
     * Procesar el Deep Link de retorno de Mercado Pago
     * Llamar desde la Activity que maneja el deep link
     * @param intent Intent recibido
     */
    fun handlePaymentDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val scheme = data.scheme
        val host = data.host
        val path = data.path
        
        Log.d(TAG, "Deep link recibido: $scheme://$host$path")
        
        if (scheme != DEEP_LINK_SCHEME) return
        
        val orderId = data.getQueryParameter("order_id") ?: currentOrderId
        
        when {
            host == "payment" && path == "/success" -> {
                val paymentId = data.getQueryParameter("payment_id")
                Log.d(TAG, "✅ Pago exitoso - Order: $orderId, Payment: $paymentId")
                _paymentState.value = PaymentState.PaymentSuccess(
                    orderId = orderId ?: "",
                    paymentId = paymentId
                )
            }
            host == "payment" && path == "/pending" -> {
                Log.d(TAG, "⏳ Pago pendiente - Order: $orderId")
                _paymentState.value = PaymentState.PaymentPending(orderId = orderId ?: "")
            }
            host == "payment" && path == "/failure" -> {
                val reason = data.getQueryParameter("reason")
                Log.d(TAG, "❌ Pago fallido - Order: $orderId, Reason: $reason")
                _paymentState.value = PaymentState.PaymentFailed(
                    orderId = orderId ?: "",
                    reason = reason
                )
            }
        }
    }
    
    /**
     * Verificar el estado actual del pago en nuestra DB
     * Útil para sincronizar si el webhook ya actualizó el estado
     */
    suspend fun checkPaymentStatus(orderId: String): String? = withContext(Dispatchers.IO) {
        try {
            @Serializable
            data class PaymentStatus(val status: String?)
            
            val payment = SupabaseClient.database
                .from("payments")
                .select { filter { eq("order_id", orderId) } }
                .decodeSingleOrNull<PaymentStatus>()
            
            payment?.status
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando estado: ${e.message}")
            null
        }
    }
    
    /**
     * Resetear el estado del pago
     */
    fun resetState() {
        _paymentState.value = PaymentState.Idle
        currentOrderId = null
    }
    
    /**
     * Convertir items del carrito a items de Mercado Pago
     */
    fun cartItemsToMPItems(cartItems: List<CartRepository.CartItem>): List<MPItem> {
        return cartItems.map { cartItem ->
            MPItem(
                id = cartItem.post.id,
                title = cartItem.post.title.ifEmpty { 
                    cartItem.post.producto.titulo.ifEmpty { "Producto" }
                },
                quantity = cartItem.quantity,
                unitPrice = cartItem.post.producto.precio,
                currencyId = "UYU",
                pictureUrl = cartItem.post.images.firstOrNull(),
                description = cartItem.selectedColor?.let { "Color: $it" }
            )
        }
    }
}
