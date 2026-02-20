package com.rendly.app.ui.screens.checkout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowCompat
import com.rendly.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MERCADO PAGO CHECKOUT ACTIVITY - WebView Embebido
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Activity con WebView embebido para el checkout de Mercado Pago.
 * Se ve como parte de la app (sin abrir navegador externo).
 * 
 * Usa OkHttp como proxy de red via shouldInterceptRequest para resolver
 * el error ERR_SSL_PROTOCOL_ERROR del sandbox de MP Uruguay (.com.uy).
 * El WebView nunca hace conexiones de red directamente - OkHttp maneja
 * todo el TLS correctamente.
 * 
 * Flujo:
 * 1. Se crea preferencia en backend → se obtiene init_point URL
 * 2. Se abre esta Activity con la URL
 * 3. WebView carga el checkout de MP (via OkHttp proxy)
 * 4. Usuario completa el pago
 * 5. MP redirige a deep link → interceptamos y retornamos resultado
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
class MercadoPagoCheckoutActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MPCheckoutActivity"
        
        // Extras
        const val EXTRA_CHECKOUT_URL = "checkout_url"
        const val EXTRA_ORDER_ID = "order_id"
        
        // Result extras
        const val RESULT_PAYMENT_ID = "payment_id"
        const val RESULT_PAYMENT_STATUS = "payment_status"
        const val RESULT_ORDER_ID = "order_id"
        const val RESULT_ERROR_MESSAGE = "error_message"
        
        // Deep link patterns para detectar resultado
        private const val DEEP_LINK_SCHEME = "Merqora"
        
        /**
         * Crear intent para iniciar el checkout
         */
        fun createIntent(
            context: Context,
            checkoutUrl: String,
            orderId: String
        ): Intent {
            return Intent(context, MercadoPagoCheckoutActivity::class.java).apply {
                putExtra(EXTRA_CHECKOUT_URL, checkoutUrl)
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
    }
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var okHttpClient: OkHttpClient
    private var orderId: String = ""
    
    /**
     * Crear OkHttpClient que puede manejar SSL del sandbox de MP
     * En DEBUG: acepta todos los certificados (solo para sandbox)
     * En RELEASE: usa certificados del sistema normalmente
     */
    private fun createOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
        
        if (BuildConfig.DEBUG) {
            // En debug, aceptar cualquier certificado para sandbox
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
        }
        
        return builder.build()
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar OkHttp
        okHttpClient = createOkHttpClient()
        
        // Configurar ventana edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = 0xFF009EE3.toInt() // Azul de Mercado Pago
        
        val checkoutUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL)
        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        
        if (checkoutUrl.isNullOrEmpty()) {
            Log.e(TAG, "checkout_url es requerido")
            finishWithError("URL de checkout no proporcionada")
            return
        }
        
        Log.d(TAG, "Iniciando checkout WebView: $checkoutUrl")
        
        // Crear layout
        val container = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        
        // Progress bar
        progressBar = ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            isIndeterminate = true
        }
        
        // WebView
        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = false
                setSupportZoom(false)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                allowContentAccess = true
                allowFileAccess = true
                // User agent de Chrome moderno para que MP no bloquee
                userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.230 Mobile Safari/537.36"
            }
            
            // Sincronizar cookies entre OkHttp y WebView
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            
            webViewClient = MPWebViewClient()
            webChromeClient = MPWebChromeClient()
        }
        
        container.addView(webView)
        container.addView(progressBar)
        setContentView(container)
        
        // Manejar botón atrás
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finishWithCancel()
                }
            }
        })
        
        // Cargar URL de checkout
        webView.loadUrl(checkoutUrl)
    }
    
    private inner class MPWebViewClient : WebViewClient() {
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar.visibility = View.VISIBLE
            Log.d(TAG, "Cargando: $url")
        }
        
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
        }
        
        /**
         * Interceptar TODAS las requests de red del WebView y rutearlas
         * a través de OkHttp que maneja TLS/SSL correctamente.
         * Esto soluciona ERR_SSL_PROTOCOL_ERROR del sandbox de MP Uruguay.
         */
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val url = request?.url?.toString() ?: return null
            
            // Solo interceptar requests HTTPS de dominios de MP
            if (!url.startsWith("https://") || 
                !(url.contains("mercadopago") || url.contains("mercadolibre") || url.contains("mlstatic"))) {
                return super.shouldInterceptRequest(view, request)
            }
            
            return try {
                val okRequest = Request.Builder()
                    .url(url)
                    .apply {
                        // Copiar headers del WebView request
                        request?.requestHeaders?.forEach { (key, value) ->
                            addHeader(key, value)
                        }
                    }
                    .build()
                
                val response = okHttpClient.newCall(okRequest).execute()
                
                val contentType = response.header("Content-Type", "text/html")
                val mimeType = contentType?.split(";")?.firstOrNull()?.trim() ?: "text/html"
                val encoding = if (contentType?.contains("charset=") == true) {
                    contentType.substringAfter("charset=").trim()
                } else {
                    "UTF-8"
                }
                
                val responseBody = response.body
                val inputStream = responseBody?.byteStream() ?: ByteArrayInputStream(ByteArray(0))
                
                // Copiar headers de respuesta relevantes
                val responseHeaders = mutableMapOf<String, String>()
                response.headers.forEach { (name, value) ->
                    responseHeaders[name] = value
                }
                
                WebResourceResponse(
                    mimeType,
                    encoding,
                    response.code,
                    if (response.isSuccessful) "OK" else "Error",
                    responseHeaders,
                    inputStream
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error interceptando request: $url - ${e.message}")
                null // Fallback al WebView normal
            }
        }
        
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            Log.d(TAG, "URL interceptada: $url")
            
            // Detectar deep links de resultado
            return when {
                url.startsWith("$DEEP_LINK_SCHEME://") -> {
                    handleDeepLink(Uri.parse(url))
                    true
                }
                url.contains("payment/success") || url.contains("congrats") -> {
                    extractPaymentInfoAndFinish(url, "approved")
                    true
                }
                url.contains("payment/failure") || url.contains("failure") -> {
                    extractPaymentInfoAndFinish(url, "rejected")
                    true
                }
                url.contains("payment/pending") || url.contains("pending") -> {
                    extractPaymentInfoAndFinish(url, "pending")
                    true
                }
                // Permitir otros links de MP
                url.contains("mercadopago") || url.contains("mercadolibre") -> {
                    false
                }
                // Links externos - abrir en navegador
                else -> {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        }
        
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame == true) {
                Log.e(TAG, "Error cargando página: ${error?.description}")
            }
        }
        
        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            // Fallback: si alguna request no pasó por OkHttp, aceptar SSL en debug
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "SSL fallback en DEBUG: ${error?.url}")
                handler?.proceed()
                return
            }
            super.onReceivedSslError(view, handler, error)
        }
    }
    
    private inner class MPWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (newProgress >= 100) {
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun handleDeepLink(uri: Uri) {
        val path = "${uri.host}${uri.path ?: ""}"
        val paymentId = uri.getQueryParameter("payment_id")
        val status = uri.getQueryParameter("status")
        
        Log.d(TAG, "Deep link recibido: $path, payment_id=$paymentId, status=$status")
        
        when {
            path.contains("success") -> finishWithSuccess(paymentId)
            path.contains("pending") -> finishWithPending(paymentId)
            path.contains("failure") -> finishWithFailure(uri.getQueryParameter("reason"))
            else -> finishWithCancel()
        }
    }
    
    private fun extractPaymentInfoAndFinish(url: String, status: String) {
        val uri = Uri.parse(url)
        val paymentId = uri.getQueryParameter("payment_id") 
            ?: uri.getQueryParameter("collection_id")
            ?: uri.getQueryParameter("preference_id")
        
        Log.d(TAG, "Extrayendo info de URL: status=$status, paymentId=$paymentId")
        
        when (status) {
            "approved" -> finishWithSuccess(paymentId)
            "pending" -> finishWithPending(paymentId)
            else -> finishWithFailure(uri.getQueryParameter("status_detail"))
        }
    }
    
    private fun finishWithSuccess(paymentId: String?) {
        Log.d(TAG, "Pago exitoso: $paymentId")
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(RESULT_PAYMENT_ID, paymentId)
            putExtra(RESULT_PAYMENT_STATUS, "approved")
            putExtra(RESULT_ORDER_ID, orderId)
        })
        finish()
    }
    
    private fun finishWithPending(paymentId: String?) {
        Log.d(TAG, "Pago pendiente: $paymentId")
        setResult(RESULT_PAYMENT_PENDING, Intent().apply {
            putExtra(RESULT_PAYMENT_ID, paymentId)
            putExtra(RESULT_PAYMENT_STATUS, "pending")
            putExtra(RESULT_ORDER_ID, orderId)
        })
        finish()
    }
    
    private fun finishWithFailure(reason: String?) {
        Log.d(TAG, "Pago fallido: $reason")
        setResult(RESULT_PAYMENT_REJECTED, Intent().apply {
            putExtra(RESULT_PAYMENT_STATUS, "rejected")
            putExtra(RESULT_ERROR_MESSAGE, reason ?: "Pago rechazado")
            putExtra(RESULT_ORDER_ID, orderId)
        })
        finish()
    }
    
    private fun finishWithCancel() {
        Log.d(TAG, "Pago cancelado")
        setResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(RESULT_ERROR_MESSAGE, "Pago cancelado")
            putExtra(RESULT_ORDER_ID, orderId)
        })
        finish()
    }
    
    private fun finishWithError(message: String) {
        Log.e(TAG, "Error: $message")
        setResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(RESULT_ERROR_MESSAGE, message)
            putExtra(RESULT_ORDER_ID, orderId)
        })
        finish()
    }
    
    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}

// Códigos de resultado personalizados
const val RESULT_PAYMENT_PENDING = 1002
const val RESULT_PAYMENT_REJECTED = 1003
