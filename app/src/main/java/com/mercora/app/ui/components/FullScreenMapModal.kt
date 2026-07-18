package com.mercora.app.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mercora.app.BuildConfig
import com.mercora.app.ui.theme.HomeBg
import com.mercora.app.ui.theme.PrimaryPurple
import com.mercora.app.ui.theme.TextMuted
import com.mercora.app.ui.theme.TextPrimary
import com.mercora.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun FullScreenMapModal(
    isVisible: Boolean,
    latitude: Double,
    longitude: Double,
    onDismiss: () -> Unit
) {
    val latStr = String.format(Locale.US, "%.6f", latitude)
    val lngStr = String.format(Locale.US, "%.6f", longitude)
    val context = LocalContext.current
    var isMapReady by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }

    val animationProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "modalAnim"
    )

    val scaleValue = 0.90f + (0.10f * animationProgress)
    val offsetY = (1f - animationProgress) * 80

    if (!isVisible && animationProgress == 0f) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.7f * animationProgress).coerceIn(0f, 1f)))
            .clickable(enabled = false) {}
            .graphicsLayer {
                scaleX = scaleValue
                scaleY = scaleValue
                translationY = offsetY
                alpha = animationProgress
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (mapError == null) {
                FullScreenMapWebView(
                    latitude = latitude,
                    longitude = longitude,
                    onMapReady = { isMapReady = true },
                    onError = { mapError = it }
                )
            }

            if (!isMapReady && mapError == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HomeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = PrimaryPurple,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando mapa...",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            mapError?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HomeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Error al cargar el mapa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(onClick = onDismiss) {
                            Text("Volver")
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "UbicaciÃ³n",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$latStr, $lngStr",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(44.dp))
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = HomeBg,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "UbicaciÃ³n compartida",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "$latStr, $lngStr",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val gmmUri = android.net.Uri.parse("geo:$latStr,$lngStr?q=$latStr,$lngStr")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmUri)
                            intent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallbackUri = android.net.Uri.parse("https://maps.google.com/maps?q=$latStr,$lngStr")
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, fallbackUri))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Navegar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            val shareUri = android.net.Uri.parse("https://maps.google.com/maps?q=$latStr,$lngStr")
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "MirÃ¡ mi ubicaciÃ³n: https://maps.google.com/maps?q=$latStr,$lngStr")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir ubicaciÃ³n"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF151B23))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compartir",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun FullScreenMapWebView(
    latitude: Double,
    longitude: Double,
    onMapReady: () -> Unit,
    onError: (String) -> Unit
) {
    val accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    val htmlContent = remember(latitude, longitude, accessToken) {
        generateFullScreenMapHtml(latitude, longitude, accessToken)
    }

    var webView: WebView? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                try {
                    stopLoading()
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    clearCache(true)
                    clearHistory()
                    loadUrl("about:blank")
                    onPause()
                    removeAllViews()
                    destroy()
                } catch (_: Exception) {}
            }
            webView = null
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = false
                    displayZoomControls = false
                    setSupportZoom(false)
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onMapReady()
                    }
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            onError("Error al cargar el mapa")
                        }
                    }
                }
                webChromeClient = WebChromeClient()
                loadDataWithBaseURL(
                    "https://api.mapbox.com",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
                webView = this
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view -> webView = view }
    )
}

private fun generateFullScreenMapHtml(lat: Double, lng: Double, accessToken: String): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no">
    <script src="https://api.mapbox.com/mapbox-gl-js/v3.0.1/mapbox-gl.js"></script>
    <link href="https://api.mapbox.com/mapbox-gl-js/v3.0.1/mapbox-gl.css" rel="stylesheet">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { overflow: hidden; background: #0D1117; }
        #map { position: absolute; top: 0; bottom: 0; width: 100%; height: 100%; }
        .mapboxgl-ctrl-attrib { display: none !important; }
        .mapboxgl-ctrl-logo { display: none !important; }
        .marker {
            width: 32px; height: 32px;
            background: #FF6B35;
            border: 3px solid white;
            border-radius: 50% 50% 50% 0;
            transform: rotate(-45deg);
            box-shadow: 0 2px 8px rgba(0,0,0,0.4);
            cursor: default;
        }
        .marker::after {
            content: '';
            width: 10px; height: 10px;
            background: white;
            border-radius: 50%;
            position: absolute;
            top: 50%; left: 50%;
            transform: translate(-50%, -50%);
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        mapboxgl.accessToken = '$accessToken';
        const map = new mapboxgl.Map({
            container: 'map',
            style: 'mapbox://styles/mapbox/streets-v12',
            center: [$lng, $lat],
            zoom: 15,
            attributionControl: false
        });
        map.addControl(new mapboxgl.NavigationControl({ showCompass: false }), 'top-right');
        const el = document.createElement('div');
        el.className = 'marker';
        new mapboxgl.Marker({ element: el, anchor: 'bottom' })
            .setLngLat([$lng, $lat])
            .addTo(map);
    </script>
</body>
</html>
    """.trimIndent()
}
