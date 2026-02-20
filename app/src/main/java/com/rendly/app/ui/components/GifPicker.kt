package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

// ═══════════════════════════════════════════════════════════════
// GIPHY API DATA CLASSES
// ═══════════════════════════════════════════════════════════════
@Serializable
data class GiphyResponse(
    val data: List<GiphyGif> = emptyList()
)

@Serializable
data class GiphyGif(
    val id: String = "",
    val images: GiphyImages = GiphyImages()
)

@Serializable
data class GiphyImages(
    val fixed_width: GiphyImage = GiphyImage(),
    val original: GiphyImage = GiphyImage()
)

@Serializable
data class GiphyImage(
    val url: String = "",
    val width: String = "100",
    val height: String = "100"
)

// ═══════════════════════════════════════════════════════════════
// GIF PICKER MODAL - Estilo Instagram profesional
// ═══════════════════════════════════════════════════════════════
@Composable
fun GifPickerModal(
    visible: Boolean,
    onDismiss: () -> Unit,
    onGifSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var gifs by remember { mutableStateOf<List<GiphyGif>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    // GIPHY API Key (usar la key pública de desarrollo)
    val giphyApiKey = "hlJ056DinwMhY9aPNSscrUi4R4Xo5ekI"
    
    // Cargar trending GIFs al abrir
    LaunchedEffect(visible) {
        if (visible) {
            showContent = true
            if (gifs.isEmpty()) {
                isLoading = true
                try {
                    val response = fetchGiphyTrending(giphyApiKey)
                    gifs = response
                } catch (e: Exception) {
                    // Error loading GIFs
                }
                isLoading = false
            }
        } else {
            delay(300)
            showContent = false
        }
    }
    
    // Buscar GIFs cuando cambia la query
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(500) // Debounce
            isLoading = true
            try {
                val response = fetchGiphySearch(giphyApiKey, searchQuery)
                gifs = response
            } catch (e: Exception) {
                // Error searching
            }
            isLoading = false
        } else if (visible) {
            // Volver a trending si se borra la búsqueda
            isLoading = true
            try {
                val response = fetchGiphyTrending(giphyApiKey)
                gifs = response
            } catch (e: Exception) {
                // Error loading
            }
            isLoading = false
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss)
        ) {
            // Modal container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF1A1A2E))
                    .clickable(enabled = false) {} // Prevent click through
            ) {
                // ═══════════════════════════════════════════════════════════════
                // HEADER
                // ═══════════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .align(Alignment.TopCenter)
                    )
                    
                    Text(
                        text = "GIFs",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 12.dp)
                    )
                    
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // ═══════════════════════════════════════════════════════════════
                // SEARCH BAR
                // ═══════════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFF2E8B57)),
                            singleLine = true,
                            enabled = true,
                            interactionSource = remember { MutableInteractionSource() },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Buscar GIFs...",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                // ═══════════════════════════════════════════════════════════════
                // GIF GRID (Staggered layout for variable heights)
                // ═══════════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    if (isLoading && gifs.isEmpty()) {
                        // Loading indicator
                        CircularProgressIndicator(
                            color = Color(0xFF2E8B57),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(gifs, key = { it.id }) { gif ->
                                GifItem(
                                    gif = gif,
                                    onClick = {
                                        onGifSelected(gif.images.original.url)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                    
                    // Loading overlay when searching
                    if (isLoading && gifs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF2E8B57),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                // ═══════════════════════════════════════════════════════════════
                // POWERED BY GIPHY
                // ═══════════════════════════════════════════════════════════════
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Powered by GIPHY",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GifItem(
    gif: GiphyGif,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val aspectRatio = try {
        val width = gif.images.fixed_width.width.toFloatOrNull() ?: 100f
        val height = gif.images.fixed_width.height.toFloatOrNull() ?: 100f
        width / height
    } catch (e: Exception) {
        1f
    }
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(gif.images.fixed_width.url)
            .decoderFactory(
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoderDecoder.Factory()
                } else {
                    GifDecoder.Factory()
                }
            )
            .crossfade(true)
            .build(),
        contentDescription = "GIF",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio.coerceIn(0.5f, 2f))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
    )
}

// ═══════════════════════════════════════════════════════════════
// GIPHY API FUNCTIONS
// ═══════════════════════════════════════════════════════════════
private val json = Json { ignoreUnknownKeys = true }

private suspend fun fetchGiphyTrending(apiKey: String): List<GiphyGif> {
    return try {
        val client = HttpClient()
        val response: HttpResponse = client.get("https://api.giphy.com/v1/gifs/trending") {
            parameter("api_key", apiKey)
            parameter("limit", 30)
            parameter("rating", "g")
        }
        val body = response.bodyAsText()
        client.close()
        val parsed = json.decodeFromString<GiphyResponse>(body)
        parsed.data
    } catch (e: Exception) {
        emptyList()
    }
}

private suspend fun fetchGiphySearch(apiKey: String, query: String): List<GiphyGif> {
    return try {
        val client = HttpClient()
        val response: HttpResponse = client.get("https://api.giphy.com/v1/gifs/search") {
            parameter("api_key", apiKey)
            parameter("q", query)
            parameter("limit", 30)
            parameter("rating", "g")
        }
        val body = response.bodyAsText()
        client.close()
        val parsed = json.decodeFromString<GiphyResponse>(body)
        parsed.data
    } catch (e: Exception) {
        emptyList()
    }
}
