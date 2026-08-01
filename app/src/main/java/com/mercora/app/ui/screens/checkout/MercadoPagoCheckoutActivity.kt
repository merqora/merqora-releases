package com.mercora.app.ui.screens.checkout

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
import com.mercora.app.BuildConfig

/**
 * Activity con WebView embebido para checkout de Mercado Pago.
 * Se abre dentro de la app cuando no se puede abrir la app de MP directamente.
 */
class MercadoPagoCheckoutActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MPCheckoutActivity"

        const val EXTRA_CHECKOUT_URL = "checkout_url"
        const val EXTRA_ORDER_ID = "order_id"

        const val RESULT_PAYMENT_ID = "payment_id"
        const val RESULT_PAYMENT_STATUS = "payment_status"
        const val RESULT_ORDER_ID = "order_id"
        const val RESULT_ERROR_MESSAGE = "error_message"

        private const val DEEP_LINK_SCHEME = "Mercora"

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
    private lateinit var container: FrameLayout
    private var orderId: String = ""
    private var mpAppOpened = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val checkoutUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL)
        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""

        if (checkoutUrl.isNullOrEmpty()) {
            Log.e(TAG, "checkout_url es requerido")
            finishWithError("URL de checkout no proporcionada")
            return
        }

        Log.d(TAG, "Iniciando checkout WebView: $checkoutUrl")

        container = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFFFFFFFF.toInt())
            visibility = View.GONE
        }

        progressBar = ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = android.view.Gravity.CENTER }
            isIndeterminate = true
        }

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
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
                userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.230 Mobile Safari/537.36"
            }

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = MPWebViewClient()
            webChromeClient = MPWebChromeClient()
        }

        container.addView(webView)
        container.addView(progressBar)
        setContentView(container)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finishWithCancel()
                }
            }
        })

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
            // Si la página terminó de cargar y no redirigió a MP app, mostrar WebView
            if (!mpAppOpened && url?.startsWith("https://") == true) {
                container.visibility = View.VISIBLE
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            Log.d(TAG, "URL interceptada: $url")

            // mercadopago://, meli://, o pay-preference ? redirect de MP
            if (url.startsWith("mercadopago://") || url.startsWith("meli://") || url.contains("pay-preference")) {
                Log.d(TAG, "Redirect MP detectado: $url")
                val mpIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.mercadopago.wallet")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return try {
                    startActivity(mpIntent)
                    Log.d(TAG, "Abriendo app MP: $url")
                    mpAppOpened = true
                    finish()
                    true
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo abrir MP app: ${e.message}")
                    // Sin MP app instalada: extraer URL real del init_point original
                    val originalUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL) ?: url
                    Log.d(TAG, "Cargando init_point original en WebView: $originalUrl")
                    container.visibility = View.VISIBLE
                    webView.loadUrl(originalUrl)
                    true
                }
            }

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
                url.contains("mercadopago") || url.contains("mercadolibre") || url.contains("mlstatic") -> {
                    // MP está cargando en WebView (sin app MP instalada) — mostrar contenido
                    container.visibility = View.VISIBLE
                    false
                }
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

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "SSL error ignorado en DEBUG: ${error?.url}")
                handler?.proceed()
                return
            }
            super.onReceivedSslError(view, handler, error)
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame == true) {
                val url = request.url.toString()
                Log.e(TAG, "Error cargando página: ${error?.errorCode} - ${error?.description} - $url")
                progressBar.visibility = View.GONE
                // Si es schema desconocido (mercadopago://), recargar init_point original
                val unknownScheme = error?.errorCode == -10 || error?.errorCode == -6
                if (unknownScheme || url.startsWith("mercadopago://") || url.contains("pay-preference")) {
                    val originalUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL) ?: return
                    Log.d(TAG, "Recargando init_point en WebView: $originalUrl")
                    container.visibility = View.VISIBLE
                    webView.post { webView.loadUrl(originalUrl) }
                }
            }
        }
    }

    private inner class MPWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (newProgress >= 100) {
                progressBar.visibility = View.GONE
            }
        }

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: android.os.Message?
        ): Boolean {
            val childView = WebView(this@MercadoPagoCheckoutActivity).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportMultipleWindows(true)
                }
                webViewClient = MPWebViewClient()
                webChromeClient = MPWebChromeClient()
            }
            webView.addView(childView)
            resultMsg?.obj = childView
            resultMsg?.sendToTarget()
            return true
        }

        override fun onCloseWindow(window: WebView?) {
            webView.removeView(window)
        }

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            result?.confirm()
            return true
        }

        override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            result?.confirm()
            return true
        }
    }

    private fun handleDeepLink(uri: Uri) {
        val path = "${uri.host}${uri.path ?: ""}"
        val paymentId = uri.getQueryParameter("payment_id")

        Log.d(TAG, "Deep link recibido: $path, payment_id=$paymentId")

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
        setResult(1002, Intent().apply {
            putExtra(RESULT_PAYMENT_ID, paymentId)
            putExtra(RESULT_PAYMENT_STATUS, "pending")
            putExtra(RESULT_ORDER_ID, orderId)
        })
        finish()
    }

    private fun finishWithFailure(reason: String?) {
        Log.d(TAG, "Pago fallido: $reason")
        setResult(1003, Intent().apply {
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
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }
}
