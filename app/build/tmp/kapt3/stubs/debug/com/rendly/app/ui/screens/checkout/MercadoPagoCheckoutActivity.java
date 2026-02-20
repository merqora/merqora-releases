package com.rendly.app.ui.screens.checkout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.WindowCompat;
import com.rendly.app.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 \u001f2\u00020\u0001:\u0003\u001f !B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u000b\u001a\u00020\u0004H\u0002J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0006H\u0002J\b\u0010\u0010\u001a\u00020\rH\u0002J\u0010\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u0006H\u0002J\u0012\u0010\u0013\u001a\u00020\r2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0006H\u0002J\u0012\u0010\u0015\u001a\u00020\r2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0006H\u0002J\u0012\u0010\u0017\u001a\u00020\r2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0006H\u0002J\u0010\u0010\u0018\u001a\u00020\r2\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u0012\u0010\u001b\u001a\u00020\r2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0015J\b\u0010\u001e\u001a\u00020\rH\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/rendly/app/ui/screens/checkout/MercadoPagoCheckoutActivity;", "Landroidx/activity/ComponentActivity;", "()V", "okHttpClient", "Lokhttp3/OkHttpClient;", "orderId", "", "progressBar", "Landroid/widget/ProgressBar;", "webView", "Landroid/webkit/WebView;", "createOkHttpClient", "extractPaymentInfoAndFinish", "", "url", "status", "finishWithCancel", "finishWithError", "message", "finishWithFailure", "reason", "finishWithPending", "paymentId", "finishWithSuccess", "handleDeepLink", "uri", "Landroid/net/Uri;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "Companion", "MPWebChromeClient", "MPWebViewClient", "app_debug"})
public final class MercadoPagoCheckoutActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "MPCheckoutActivity";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String EXTRA_CHECKOUT_URL = "checkout_url";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String EXTRA_ORDER_ID = "order_id";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String RESULT_PAYMENT_ID = "payment_id";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String RESULT_PAYMENT_STATUS = "payment_status";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String RESULT_ORDER_ID = "order_id";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String RESULT_ERROR_MESSAGE = "error_message";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String DEEP_LINK_SCHEME = "Merqora";
    private android.webkit.WebView webView;
    private android.widget.ProgressBar progressBar;
    private okhttp3.OkHttpClient okHttpClient;
    @org.jetbrains.annotations.NotNull
    private java.lang.String orderId = "";
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.ui.screens.checkout.MercadoPagoCheckoutActivity.Companion Companion = null;
    
    public MercadoPagoCheckoutActivity() {
        super();
    }
    
    /**
     * Crear OkHttpClient que puede manejar SSL del sandbox de MP
     * En DEBUG: acepta todos los certificados (solo para sandbox)
     * En RELEASE: usa certificados del sistema normalmente
     */
    private final okhttp3.OkHttpClient createOkHttpClient() {
        return null;
    }
    
    @java.lang.Override
    @android.annotation.SuppressLint(value = {"SetJavaScriptEnabled"})
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void handleDeepLink(android.net.Uri uri) {
    }
    
    private final void extractPaymentInfoAndFinish(java.lang.String url, java.lang.String status) {
    }
    
    private final void finishWithSuccess(java.lang.String paymentId) {
    }
    
    private final void finishWithPending(java.lang.String paymentId) {
    }
    
    private final void finishWithFailure(java.lang.String reason) {
    }
    
    private final void finishWithCancel() {
    }
    
    private final void finishWithError(java.lang.String message) {
    }
    
    @java.lang.Override
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/MercadoPagoCheckoutActivity$Companion;", "", "()V", "DEEP_LINK_SCHEME", "", "EXTRA_CHECKOUT_URL", "EXTRA_ORDER_ID", "RESULT_ERROR_MESSAGE", "RESULT_ORDER_ID", "RESULT_PAYMENT_ID", "RESULT_PAYMENT_STATUS", "TAG", "createIntent", "Landroid/content/Intent;", "context", "Landroid/content/Context;", "checkoutUrl", "orderId", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Crear intent para iniciar el checkout
         */
        @org.jetbrains.annotations.NotNull
        public final android.content.Intent createIntent(@org.jetbrains.annotations.NotNull
        android.content.Context context, @org.jetbrains.annotations.NotNull
        java.lang.String checkoutUrl, @org.jetbrains.annotations.NotNull
        java.lang.String orderId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0082\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0016\u00a8\u0006\t"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/MercadoPagoCheckoutActivity$MPWebChromeClient;", "Landroid/webkit/WebChromeClient;", "(Lcom/rendly/app/ui/screens/checkout/MercadoPagoCheckoutActivity;)V", "onProgressChanged", "", "view", "Landroid/webkit/WebView;", "newProgress", "", "app_debug"})
    final class MPWebChromeClient extends android.webkit.WebChromeClient {
        
        public MPWebChromeClient() {
            super();
        }
        
        @java.lang.Override
        public void onProgressChanged(@org.jetbrains.annotations.Nullable
        android.webkit.WebView view, int newProgress) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u0082\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0016J&\u0010\t\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0016J&\u0010\f\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\r\u001a\u0004\u0018\u00010\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0016J&\u0010\u0011\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\b\u0010\u000f\u001a\u0004\u0018\u00010\u0014H\u0017J\u001e\u0010\u0015\u001a\u0004\u0018\u00010\u00162\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016J\u001c\u0010\u0017\u001a\u00020\u00182\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0016\u00a8\u0006\u0019"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/MercadoPagoCheckoutActivity$MPWebViewClient;", "Landroid/webkit/WebViewClient;", "(Lcom/rendly/app/ui/screens/checkout/MercadoPagoCheckoutActivity;)V", "onPageFinished", "", "view", "Landroid/webkit/WebView;", "url", "", "onPageStarted", "favicon", "Landroid/graphics/Bitmap;", "onReceivedError", "request", "Landroid/webkit/WebResourceRequest;", "error", "Landroid/webkit/WebResourceError;", "onReceivedSslError", "handler", "Landroid/webkit/SslErrorHandler;", "Landroid/net/http/SslError;", "shouldInterceptRequest", "Landroid/webkit/WebResourceResponse;", "shouldOverrideUrlLoading", "", "app_debug"})
    final class MPWebViewClient extends android.webkit.WebViewClient {
        
        public MPWebViewClient() {
            super();
        }
        
        @java.lang.Override
        public void onPageStarted(@org.jetbrains.annotations.Nullable
        android.webkit.WebView view, @org.jetbrains.annotations.Nullable
        java.lang.String url, @org.jetbrains.annotations.Nullable
        android.graphics.Bitmap favicon) {
        }
        
        @java.lang.Override
        public void onPageFinished(@org.jetbrains.annotations.Nullable
        android.webkit.WebView view, @org.jetbrains.annotations.Nullable
        java.lang.String url) {
        }
        
        /**
         * Interceptar TODAS las requests de red del WebView y rutearlas
         * a través de OkHttp que maneja TLS/SSL correctamente.
         * Esto soluciona ERR_SSL_PROTOCOL_ERROR del sandbox de MP Uruguay.
         */
        @java.lang.Override
        @org.jetbrains.annotations.Nullable
        public android.webkit.WebResourceResponse shouldInterceptRequest(@org.jetbrains.annotations.Nullable
        android.webkit.WebView view, @org.jetbrains.annotations.Nullable
        android.webkit.WebResourceRequest request) {
            return null;
        }
        
        @java.lang.Override
        public boolean shouldOverrideUrlLoading(@org.jetbrains.annotations.Nullable
        android.webkit.WebView view, @org.jetbrains.annotations.Nullable
        android.webkit.WebResourceRequest request) {
            return false;
        }
        
        @java.lang.Override
        public void onReceivedError(@org.jetbrains.annotations.Nullable
        android.webkit.WebView view, @org.jetbrains.annotations.Nullable
        android.webkit.WebResourceRequest request, @org.jetbrains.annotations.Nullable
        android.webkit.WebResourceError error) {
        }
        
        @java.lang.Override
        @android.annotation.SuppressLint(value = {"WebViewClientOnReceivedSslError"})
        public void onReceivedSslError(@org.jetbrains.annotations.Nullable
        android.webkit.WebView view, @org.jetbrains.annotations.Nullable
        android.webkit.SslErrorHandler handler, @org.jetbrains.annotations.Nullable
        android.net.http.SslError error) {
        }
    }
}