package com.rendly.app.ui.components

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RenderEffect
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

// ═══════════════════════════════════════════════════════════════
// DEFINICIÓN DE FILTROS - Matrices de color optimizadas para GPU
// ═══════════════════════════════════════════════════════════════
data class ImageFilter(
    val id: String,
    val name: String,
    val colorMatrix: FloatArray,
    val previewColor: Color // Color representativo para el círculo
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ImageFilter
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
}

// Filtros predefinidos con matrices de color (procesamiento GPU)
val STORY_FILTERS = listOf(
    ImageFilter(
        id = "original",
        name = "Original",
        colorMatrix = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFFFFFFF)
    ),
    ImageFilter(
        id = "clarendon",
        name = "Clarendon",
        colorMatrix = floatArrayOf(
            1.2f, 0f, 0f, 0f, 10f,
            0f, 1.1f, 0f, 0f, 10f,
            0f, 0f, 1.3f, 0f, 20f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFF6B8DD6)
    ),
    ImageFilter(
        id = "gingham",
        name = "Gingham",
        colorMatrix = floatArrayOf(
            1.05f, 0.1f, 0f, 0f, 15f,
            0f, 1.05f, 0.1f, 0f, 15f,
            0.05f, 0f, 1f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFE8D5B7)
    ),
    ImageFilter(
        id = "moon",
        name = "Moon",
        colorMatrix = floatArrayOf(
            0.33f, 0.33f, 0.33f, 0f, 20f,
            0.33f, 0.33f, 0.33f, 0f, 20f,
            0.33f, 0.33f, 0.33f, 0f, 20f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFB0B0B0)
    ),
    ImageFilter(
        id = "lark",
        name = "Lark",
        colorMatrix = floatArrayOf(
            1.2f, 0f, 0f, 0f, 20f,
            0f, 1.1f, 0f, 0f, 15f,
            0f, 0f, 0.9f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFFFF5E6)
    ),
    ImageFilter(
        id = "reyes",
        name = "Reyes",
        colorMatrix = floatArrayOf(
            1.1f, 0.05f, 0f, 0f, 25f,
            0.05f, 1.05f, 0.05f, 0f, 25f,
            0f, 0.05f, 1f, 0f, 20f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFFFE4D6)
    ),
    ImageFilter(
        id = "juno",
        name = "Juno",
        colorMatrix = floatArrayOf(
            1.2f, 0f, 0f, 0f, 0f,
            0f, 1.1f, 0f, 0f, 0f,
            0f, 0f, 0.8f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFFFD93D)
    ),
    ImageFilter(
        id = "slumber",
        name = "Slumber",
        colorMatrix = floatArrayOf(
            0.9f, 0.1f, 0f, 0f, 10f,
            0f, 0.9f, 0.1f, 0f, 10f,
            0.1f, 0f, 0.85f, 0f, 15f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFF9B8AA5)
    ),
    ImageFilter(
        id = "crema",
        name = "Crema",
        colorMatrix = floatArrayOf(
            1.1f, 0.05f, 0.02f, 0f, 15f,
            0.02f, 1.05f, 0.02f, 0f, 15f,
            0f, 0.02f, 0.95f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFF5E6D3)
    ),
    ImageFilter(
        id = "ludwig",
        name = "Ludwig",
        colorMatrix = floatArrayOf(
            1.15f, 0f, 0f, 0f, 5f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 0.95f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFFFE8CC)
    ),
    ImageFilter(
        id = "aden",
        name = "Aden",
        colorMatrix = floatArrayOf(
            0.95f, 0.05f, 0f, 0f, 20f,
            0f, 1f, 0.05f, 0f, 15f,
            0.05f, 0f, 0.9f, 0f, 10f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFE6D5C3)
    ),
    ImageFilter(
        id = "perpetua",
        name = "Perpetua",
        colorMatrix = floatArrayOf(
            1f, 0.05f, 0.1f, 0f, 0f,
            0.05f, 1f, 0.05f, 0f, 10f,
            0f, 0.1f, 1.1f, 0f, 15f,
            0f, 0f, 0f, 1f, 0f
        ),
        previewColor = Color(0xFFB8D4E3)
    )
)

// ═══════════════════════════════════════════════════════════════
// APLICACIÓN DE FILTROS - Optimizado para GPU
// ═══════════════════════════════════════════════════════════════
object FilterProcessor {
    
    // Dispatcher limitado para evitar saturar CPU en procesamiento pesado
    private val singleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    
    // Caché de thumbnails: clave = "filterId_bitmapHashCode"
    private val thumbnailCache = mutableMapOf<String, Bitmap>()
    
    /**
     * Obtiene ColorMatrixColorFilter para aplicar en GPU via drawWithContent/RenderEffect
     * Uso: Para preview en tiempo real SIN crear nuevos Bitmaps
     */
    fun getColorFilter(filter: ImageFilter): ColorMatrixColorFilter? {
        if (filter.id == "original") return null
        return ColorMatrixColorFilter(ColorMatrix(filter.colorMatrix))
    }
    
    /**
     * Crea RenderEffect para Android 12+ (GPU puro)
     * Preparado para migración futura a shaders
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun createRenderEffect(filter: ImageFilter): RenderEffect? {
        if (filter.id == "original") return null
        return RenderEffect.createColorFilterEffect(
            ColorMatrixColorFilter(ColorMatrix(filter.colorMatrix))
        )
    }
    
    /**
     * SOLO para captura/export final - Aplica filtro creando nuevo Bitmap
     * Usa dispatcher limitado para no saturar CPU
     */
    suspend fun applyFilterForExport(source: Bitmap, filter: ImageFilter): Bitmap = 
        withContext(singleThreadDispatcher) {
            if (filter.id == "original") return@withContext source
            
            val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint().apply {
                isAntiAlias = true
                colorFilter = ColorMatrixColorFilter(ColorMatrix(filter.colorMatrix))
            }
            canvas.drawBitmap(source, 0f, 0f, paint)
            result
        }
    
    /**
     * Crea miniatura filtrada con caché
     * Solo recalcula si el bitmap o filtro cambiaron
     */
    fun createFilteredThumbnail(source: Bitmap, filter: ImageFilter, size: Int = 80): Bitmap {
        val cacheKey = "${filter.id}_${source.generationId}_$size"
        
        thumbnailCache[cacheKey]?.let { return it }
        
        val aspectRatio = source.width.toFloat() / source.height.toFloat()
        val thumbWidth: Int
        val thumbHeight: Int
        
        if (aspectRatio > 1) {
            thumbWidth = size
            thumbHeight = (size / aspectRatio).toInt().coerceAtLeast(1)
        } else {
            thumbHeight = size
            thumbWidth = (size * aspectRatio).toInt().coerceAtLeast(1)
        }
        
        val scaled = Bitmap.createScaledBitmap(source, thumbWidth, thumbHeight, true)
        
        val result = if (filter.id == "original") {
            scaled
        } else {
            val output = Bitmap.createBitmap(thumbWidth, thumbHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint().apply {
                isAntiAlias = true
                colorFilter = ColorMatrixColorFilter(ColorMatrix(filter.colorMatrix))
            }
            canvas.drawBitmap(scaled, 0f, 0f, paint)
            output
        }
        
        // Limitar tamaño del caché
        if (thumbnailCache.size > 50) {
            thumbnailCache.clear()
        }
        thumbnailCache[cacheKey] = result
        
        return result
    }
    
    /**
     * Limpia caché de thumbnails (llamar cuando cambia el bitmap fuente)
     */
    fun clearCache() {
        thumbnailCache.clear()
    }
}

// ═══════════════════════════════════════════════════════════════
// PREVIEW CON GPU - Composable optimizado para filtros en tiempo real
// ═══════════════════════════════════════════════════════════════

/**
 * Composable que aplica filtro en GPU sin crear Bitmaps
 * Usa drawWithContent con ColorMatrixColorFilter (GPU-accelerated en Android)
 * Preparado para migración futura a OpenGL ES / shaders
 */
@Composable
fun FilteredImage(
    bitmap: Bitmap,
    filter: ImageFilter,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    // ColorFilter cacheado por filtro - no se recrea en cada recomposición
    val colorFilter = remember(filter.id) {
        FilterProcessor.getColorFilter(filter)
    }
    
    if (colorFilter != null) {
        // Aplicar filtro via drawWithContent (GPU-accelerated)
        // Calcular rect para ContentScale.Crop (mantener aspect ratio, llenar y recortar)
        Box(
            modifier = modifier.drawWithContent {
                val paint = Paint().apply {
                    this.colorFilter = colorFilter
                    isAntiAlias = true
                    isFilterBitmap = true
                }
                
                // Calcular source rect y dest rect para simular ContentScale.Crop
                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()
                val viewWidth = size.width
                val viewHeight = size.height
                
                val bitmapRatio = bitmapWidth / bitmapHeight
                val viewRatio = viewWidth / viewHeight
                
                val srcRect: android.graphics.Rect
                if (bitmapRatio > viewRatio) {
                    // Bitmap más ancho - recortar lados
                    val scaledWidth = bitmapHeight * viewRatio
                    val left = ((bitmapWidth - scaledWidth) / 2).toInt()
                    srcRect = android.graphics.Rect(left, 0, (left + scaledWidth).toInt(), bitmapHeight.toInt())
                } else {
                    // Bitmap más alto - recortar arriba/abajo
                    val scaledHeight = bitmapWidth / viewRatio
                    val top = ((bitmapHeight - scaledHeight) / 2).toInt()
                    srcRect = android.graphics.Rect(0, top, bitmapWidth.toInt(), (top + scaledHeight).toInt())
                }
                
                val destRect = android.graphics.RectF(0f, 0f, viewWidth, viewHeight)
                
                drawContext.canvas.nativeCanvas.drawBitmap(
                    bitmap,
                    srcRect,
                    destRect,
                    paint
                )
            }
        )
    } else {
        // Sin filtro - imagen original
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// CARRUSEL DE FILTROS - Estilo Instagram (Optimizado)
// ═══════════════════════════════════════════════════════════════
@Composable
fun FilterCarousel(
    bitmap: Bitmap,
    currentFilter: ImageFilter,
    onFilterSelected: (ImageFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    
    // Iniciar centrado en el primer filtro ("Original" / "Sin Efecto")
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    val itemSize = 80.dp // Aumentado para que quepan los títulos
    val itemSpacing = 12.dp
    
    // Padding simétrico: primer filtro en esquina izquierda, último en esquina derecha
    val startPadding = 16.dp
    val endPadding = 16.dp // Mismo padding que la izquierda para que el último quede a la derecha
    
    // Índice del filtro seleccionado actualmente (para highlight visual)
    val selectedIndex = remember(currentFilter) {
        STORY_FILTERS.indexOfFirst { it.id == currentFilter.id }.coerceAtLeast(0)
    }
    
    // NO cambiamos filtro al deslizar - solo al pulsar (click)
    
    // Thumbnails cacheados - solo se recalculan si cambia el bitmap
    val bitmapId = remember(bitmap) { bitmap.generationId }
    val thumbnails = remember(bitmapId) {
        STORY_FILTERS.map { filter ->
            filter to FilterProcessor.createFilteredThumbnail(bitmap, filter, 100)
        }
    }
    
    LazyRow(
        state = listState,
        flingBehavior = snapBehavior,
        contentPadding = PaddingValues(start = startPadding, end = endPadding),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(
            items = thumbnails,
            key = { _, (filter, _) -> filter.id } // Key estable para evitar recomposiciones
        ) { index, (filter, thumbnail) ->
            // Solo se selecciona al pulsar (click), no al deslizar
            val isSelected = index == selectedIndex
            
            FilterItem(
                filter = filter,
                thumbnail = thumbnail,
                isSelected = isSelected,
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.size(itemSize)
            )
        }
    }
}

@Composable
private fun FilterItem(
    filter: ImageFilter,
    thumbnail: Bitmap,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val borderAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "border"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.2f))
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = Color.White.copy(alpha = borderAlpha),
                            shape = CircleShape
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = filter.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = filter.name,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}

