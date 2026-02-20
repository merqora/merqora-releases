package com.rendly.app.ui.components.settings

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.rendly.app.BuildConfig
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * MAPBOX MAP PICKER - Full-screen interactive map for location selection
 * ══════════════════════════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapboxMapPicker(
    isVisible: Boolean,
    initialLatitude: Double,
    initialLongitude: Double,
    initialAddress: String,
    onLocationSelected: (latitude: Double, longitude: Double, address: String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // State
    var currentLatitude by remember { mutableStateOf(initialLatitude) }
    var currentLongitude by remember { mutableStateOf(initialLongitude) }
    var currentAddress by remember { mutableStateOf(initialAddress) }
    var isLoading by remember { mutableStateOf(true) }
    var isGeocoding by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }
    
    // Update initial values when they change
    LaunchedEffect(initialLatitude, initialLongitude) {
        if (initialLatitude != 0.0 && initialLongitude != 0.0) {
            currentLatitude = initialLatitude
            currentLongitude = initialLongitude
        }
    }
    
    LaunchedEffect(initialAddress) {
        if (initialAddress.isNotBlank()) {
            currentAddress = initialAddress
        }
    }
    
    // Back handler
    BackHandler(enabled = isVisible) {
        onDismiss()
    }
    
    // Animación más fluida con spring
    val animationProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "animationProgress"
    )
    
    val scaleValue = 0.92f + (0.08f * animationProgress)
    val offsetY = (1f - animationProgress) * 120
    
    if (!isVisible && animationProgress == 0f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.6f * animationProgress).coerceIn(0f, 1f)))
            .graphicsLayer {
                this.scaleX = scaleValue
                this.scaleY = scaleValue
                this.translationY = offsetY
                this.alpha = animationProgress
            }
    ) {
        // Map WebView
        MapWebView(
            latitude = if (currentLatitude != 0.0) currentLatitude else -34.9011,
            longitude = if (currentLongitude != 0.0) currentLongitude else -56.1645,
            onMapReady = { isLoading = false },
            onLocationChanged = { lat, lng ->
                currentLatitude = lat
                currentLongitude = lng
                isGeocoding = true
            },
            onAddressResolved = { address ->
                currentAddress = address
                isGeocoding = false
            },
            onError = { error ->
                mapError = error
                isLoading = false
            }
        )
        
        // Top Bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
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
                
                // Title
                Text(
                    text = "Seleccionar ubicación",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Placeholder for symmetry
                Spacer(modifier = Modifier.width(44.dp))
            }
        }
        
        // Center pin indicator
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Pin shadow
            Box(
                modifier = Modifier
                    .offset(y = 20.dp)
                    .size(12.dp)
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
            
            // Main pin
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier
                    .size(48.dp)
                    .offset(y = (-12).dp)
            )
        }
        
        // Bottom info card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            // Address card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ubicación seleccionada",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (isGeocoding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryPurple
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Address text
                    Text(
                        text = if (currentAddress.isNotBlank()) currentAddress else "Mueve el mapa para seleccionar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Coordinates
                    if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%.6f, %.6f".format(currentLatitude, currentLongitude),
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Confirm button
                    Button(
                        onClick = {
                            onLocationSelected(currentLatitude, currentLongitude, currentAddress)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        enabled = currentLatitude != 0.0 && currentLongitude != 0.0 && !isGeocoding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirmar ubicación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            
            // Help text
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Arrastra el mapa para mover el pin a la ubicación deseada",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
        
        // Error state
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
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun MapWebView(
    latitude: Double,
    longitude: Double,
    onMapReady: () -> Unit,
    onLocationChanged: (Double, Double) -> Unit,
    onAddressResolved: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    
    val htmlContent = remember(latitude, longitude, accessToken) {
        generateMapHtml(latitude, longitude, accessToken)
    }
    
    var webView: WebView? by remember { mutableStateOf(null) }
    
    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                try {
                    stopLoading()
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    removeJavascriptInterface("Android")
                    clearCache(true)
                    clearHistory()
                    loadUrl("about:blank")
                    onPause()
                    removeAllViews()
                    destroy()
                } catch (e: Exception) {
                    android.util.Log.e("MapWebView", "Error cleaning WebView", e)
                }
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
                            onError("Error de conexión")
                        }
                    }
                }
                
                webChromeClient = WebChromeClient()
                
                addJavascriptInterface(
                    MapJsInterface(onLocationChanged, onAddressResolved),
                    "Android"
                )
                
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
        update = { view ->
            webView = view
        }
    )
}

private class MapJsInterface(
    private val onLocationChanged: (Double, Double) -> Unit,
    private val onAddressResolved: (String) -> Unit
) {
    @JavascriptInterface
    fun onMapMoved(lat: Double, lng: Double) {
        onLocationChanged(lat, lng)
    }
    
    @JavascriptInterface
    fun onAddressFound(address: String) {
        onAddressResolved(address)
    }
}

private fun generateMapHtml(lat: Double, lng: Double, accessToken: String): String {
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
        body { overflow: hidden; }
        #map { position: absolute; top: 0; bottom: 0; width: 100%; height: 100%; }
        .mapboxgl-ctrl-attrib { display: none !important; }
        .mapboxgl-ctrl-logo { display: none !important; }
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
            zoom: 16,
            attributionControl: false
        });
        
        // Add zoom controls
        map.addControl(new mapboxgl.NavigationControl({ showCompass: false }), 'top-right');
        
        // Add geolocate control
        const geolocate = new mapboxgl.GeolocateControl({
            positionOptions: { enableHighAccuracy: true },
            trackUserLocation: false,
            showUserHeading: false
        });
        map.addControl(geolocate, 'top-right');
        
        let debounceTimer;
        
        function reverseGeocode(lat, lng) {
            fetch('https://api.mapbox.com/geocoding/v5/mapbox.places/' + lng + ',' + lat + '.json?access_token=' + mapboxgl.accessToken + '&language=es&limit=1')
                .then(response => response.json())
                .then(data => {
                    if (data.features && data.features.length > 0) {
                        Android.onAddressFound(data.features[0].place_name);
                    } else {
                        Android.onAddressFound('Ubicación: ' + lat.toFixed(6) + ', ' + lng.toFixed(6));
                    }
                })
                .catch(err => {
                    Android.onAddressFound('Ubicación: ' + lat.toFixed(6) + ', ' + lng.toFixed(6));
                });
        }
        
        map.on('moveend', function() {
            const center = map.getCenter();
            Android.onMapMoved(center.lat, center.lng);
            
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                reverseGeocode(center.lat, center.lng);
            }, 500);
        });
        
        map.on('load', function() {
            reverseGeocode($lat, $lng);
        });
        
        geolocate.on('geolocate', function(e) {
            Android.onMapMoved(e.coords.latitude, e.coords.longitude);
            setTimeout(() => {
                reverseGeocode(e.coords.latitude, e.coords.longitude);
            }, 500);
        });
    </script>
</body>
</html>
    """.trimIndent()
}
