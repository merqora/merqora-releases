package com.rendly.app.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.gpu.GPUAdjustedImage
import com.rendly.app.gpu.GPUImageSurfaceView
import com.rendly.app.gpu.ImageAdjustmentState
import com.rendly.app.native.ImageAdjustEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ═══════════════════════════════════════════════════════════════
// SISTEMA DE AJUSTES DE IMAGEN - Calidad Instagram/Lightroom
// Motor nativo C++ con UI Compose ultra-fluida
// ═══════════════════════════════════════════════════════════════

// Tipos de ajuste disponibles - Nivel Instagram/Lightroom
enum class AdjustmentType(
    val icon: ImageVector,
    val label: String,
    val min: Float,
    val max: Float,
    val default: Float
) {
    BRIGHTNESS(Icons.Outlined.WbSunny, "Brillo", -1f, 1f, 0f),
    CONTRAST(Icons.Outlined.Contrast, "Contraste", -1f, 1f, 0f),
    SATURATION(Icons.Outlined.Palette, "Saturación", -1f, 1f, 0f),
    EXPOSURE(Icons.Outlined.Exposure, "Exposición", -2f, 2f, 0f),
    HIGHLIGHTS(Icons.Outlined.LightMode, "Luces", -1f, 1f, 0f),
    SHADOWS(Icons.Outlined.DarkMode, "Sombras", -1f, 1f, 0f),
    TEMPERATURE(Icons.Outlined.Thermostat, "Temperatura", -1f, 1f, 0f),
    TINT(Icons.Outlined.ColorLens, "Tinte", -1f, 1f, 0f),
    GRAIN(Icons.Outlined.Grain, "Grano", 0f, 1f, 0f)
}

// Estado de ajustes - Nivel Instagram/Lightroom completo
data class ImageAdjustState(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val exposure: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val grain: Float = 0f
) {
    fun getValue(type: AdjustmentType): Float = when (type) {
        AdjustmentType.BRIGHTNESS -> brightness
        AdjustmentType.CONTRAST -> contrast
        AdjustmentType.SATURATION -> saturation
        AdjustmentType.EXPOSURE -> exposure
        AdjustmentType.HIGHLIGHTS -> highlights
        AdjustmentType.SHADOWS -> shadows
        AdjustmentType.TEMPERATURE -> temperature
        AdjustmentType.TINT -> tint
        AdjustmentType.GRAIN -> grain
    }
    
    fun withValue(type: AdjustmentType, value: Float): ImageAdjustState = when (type) {
        AdjustmentType.BRIGHTNESS -> copy(brightness = value)
        AdjustmentType.CONTRAST -> copy(contrast = value)
        AdjustmentType.SATURATION -> copy(saturation = value)
        AdjustmentType.EXPOSURE -> copy(exposure = value)
        AdjustmentType.HIGHLIGHTS -> copy(highlights = value)
        AdjustmentType.SHADOWS -> copy(shadows = value)
        AdjustmentType.TEMPERATURE -> copy(temperature = value)
        AdjustmentType.TINT -> copy(tint = value)
        AdjustmentType.GRAIN -> copy(grain = value)
    }
    
    fun hasChanges(): Boolean = brightness != 0f || contrast != 0f || 
                                 saturation != 0f || exposure != 0f || 
                                 highlights != 0f || shadows != 0f ||
                                 temperature != 0f || tint != 0f || grain != 0f
    
    // Convertir a estado GPU
    fun toGPUState(): ImageAdjustmentState = ImageAdjustmentState(
        brightness = brightness,
        contrast = contrast,
        saturation = saturation,
        exposure = exposure,
        highlights = highlights,
        shadows = shadows,
        temperature = temperature,
        tint = tint,
        grain = grain
    )
}

/**
 * Overlay de ajustes de imagen GPU-first
 * Preview en tiempo real a 60+ FPS
 * UI nivel Instagram/Lightroom
 * 
 * @param visible Si el overlay está visible
 * @param bitmap Bitmap original a ajustar
 * @param adjustState Estado actual de ajustes (para preview en tiempo real)
 * @param selectedAdjustment Ajuste actualmente seleccionado
 * @param onAdjustmentSelected Callback cuando se selecciona un ajuste
 * @param onValueChange Callback cuando cambia el valor del slider (tiempo real)
 * @param onApply Callback cuando se aplican los cambios
 * @param onDismiss Callback cuando se cierra el overlay
 * @param previewHeight Altura del preview para posicionar el slider
 */
@Composable
fun ImageAdjustOverlay(
    visible: Boolean,
    bitmap: Bitmap?,
    adjustState: ImageAdjustState = ImageAdjustState(),
    selectedAdjustment: AdjustmentType? = null,
    onAdjustmentSelected: (AdjustmentType?) -> Unit = {},
    onValueChange: (ImageAdjustState) -> Unit = {},
    modifier: Modifier = Modifier,
    previewHeight: Dp = 0.dp,
    onApply: (ImageAdjustState) -> Unit,
    onDismiss: () -> Unit
) {
    // Estado interno solo si no se proporciona externamente
    var internalAdjustState by remember { mutableStateOf(adjustState) }
    var internalSelectedAdjustment by remember { mutableStateOf<AdjustmentType?>(null) }
    
    // Usar estado externo si se proporciona
    val currentState = adjustState
    val currentSelected = selectedAdjustment ?: internalSelectedAdjustment
    
    // Reset al cerrar
    LaunchedEffect(visible) {
        if (!visible) {
            internalAdjustState = ImageAdjustState()
            internalSelectedAdjustment = null
            ImageAdjustEngine.resetCache()
        }
    }
    
    // El overlay ahora NO muestra el preview (se muestra en HistoryScreen con GPU)
    // Solo muestra el slider y el carrusel de herramientas
    
    AnimatedVisibility(
        visible = visible && currentSelected != null,
        enter = fadeIn(tween(150)) + slideInVertically { it / 2 },
        exit = fadeOut(tween(100)) + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        currentSelected?.let { selected ->
            // ═══════════════════════════════════════════════════════════════
            // SLIDER EN PARTE BAJA DEL PREVIEW - Diseño Instagram
            // ═══════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Slider container - posicionado en la parte baja del preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Valor actual con fondo semi-transparente
                    val currentValue = currentState.getValue(selected)
                    val displayValue = when (selected) {
                        AdjustmentType.EXPOSURE -> String.format("%+.1f EV", currentValue)
                        AdjustmentType.GRAIN -> String.format("%.0f%%", currentValue * 100)
                        else -> String.format("%+.0f", currentValue * 100)
                    }
                    
                    // Badge con valor
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = displayValue,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Slider profesional
                    Slider(
                        value = currentValue,
                        onValueChange = { value ->
                            val newState = currentState.withValue(selected, value)
                            internalAdjustState = newState
                            onValueChange(newState)
                        },
                        valueRange = selected.min..selected.max,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    
                    // Reset si hay cambio
                    AnimatedVisibility(
                        visible = currentValue != selected.default,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = "Toca para restablecer",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    val newState = currentState.withValue(selected, selected.default)
                                    internalAdjustState = newState
                                    onValueChange(newState)
                                }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Carrusel de herramientas de ajuste - Reemplaza botones Tu Vitrina/Frecuentes
 * Diseño horizontal con botones circulares uniformes, estilo Instagram
 * El botón Tick se mueve a la esquina superior derecha del preview
 */
@Composable
fun AdjustmentToolsCarousel(
    adjustState: ImageAdjustState,
    selectedAdjustment: AdjustmentType?,
    onAdjustmentSelected: (AdjustmentType?) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Carrusel de herramientas - ocupa TODO el ancho
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        contentPadding = PaddingValues(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(AdjustmentType.entries.toList()) { type ->
            val isSelected = selectedAdjustment == type
            val hasValue = adjustState.getValue(type) != type.default
            
            AdjustmentToolItem(
                type = type,
                isSelected = isSelected,
                hasValue = hasValue,
                onClick = {
                    if (isSelected) {
                        onAdjustmentSelected(null)
                    } else {
                        onAdjustmentSelected(type)
                    }
                }
            )
        }
    }
}

@Composable
private fun AdjustmentToolItem(
    type: AdjustmentType,
    isSelected: Boolean,
    hasValue: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "scale"
    )
    
    val bgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.35f else 0.15f,
        animationSpec = tween(150),
        label = "bgAlpha"
    )
    
    // Tamaño fijo para todos los botones
    val buttonSize = 48.dp
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp) // Ancho fijo para todos
    ) {
        // Container para el botón + puntito indicador
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(buttonSize + 8.dp) // Extra espacio para el puntito
        ) {
            // Puntito blanco indicador ARRIBA y FUERA del botón
            if (hasValue) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-2).dp) // Fuera del botón
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            
            // Botón circular
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(buttonSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = bgAlpha))
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onClick() }
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = type.label,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = type.label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// VERSIÓN LEGACY - Para compatibilidad con código existente
// ═══════════════════════════════════════════════════════════════
@Composable
fun ImageAdjustOverlayLegacy(
    visible: Boolean,
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    onApply: (ImageAdjustState) -> Unit,
    onDismiss: () -> Unit
) {
    var adjustState by remember { mutableStateOf(ImageAdjustState()) }
    var selectedAdjustment by remember { mutableStateOf(AdjustmentType.BRIGHTNESS) }
    
    LaunchedEffect(visible) {
        if (!visible) {
            adjustState = ImageAdjustState()
            ImageAdjustEngine.resetCache()
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Título del ajuste actual
            Text(
                text = selectedAdjustment.label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
            
            // Slider centrado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentValue = adjustState.getValue(selectedAdjustment)
                val displayValue = when (selectedAdjustment) {
                    AdjustmentType.EXPOSURE -> String.format("%.1f", currentValue)
                    AdjustmentType.GRAIN -> String.format("%.0f%%", currentValue * 100)
                    else -> String.format("%+.0f", currentValue * 100)
                }
                
                Text(
                    text = displayValue,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Slider(
                    value = currentValue,
                    onValueChange = { value ->
                        adjustState = adjustState.withValue(selectedAdjustment, value)
                    },
                    valueRange = selectedAdjustment.min..selectedAdjustment.max,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
            
            // Selector de ajustes y botón aplicar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(AdjustmentType.entries.toList()) { type ->
                        val isSelected = selectedAdjustment == type
                        val hasValue = adjustState.getValue(type) != type.default
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedAdjustment = type }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color.White.copy(alpha = 0.3f)
                                        else Color.Black.copy(alpha = 0.5f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = type.label,
                                    tint = if (hasValue) Color(0xFF22C55E) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Text(
                                text = type.label,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                // Botón aplicar sin fondo verde
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { 
                            onApply(adjustState)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Aplicar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PROCESADOR DE AJUSTES - Para exportación final
// Pipeline profesional en espacio lineal
// ═══════════════════════════════════════════════════════════════
object ImageAdjustProcessor {
    
    /**
     * Aplica los ajustes profesionales al bitmap para exportación
     * Usa el motor C++ con procesamiento en espacio lineal
     */
    fun applyForExport(bitmap: Bitmap, state: ImageAdjustState): Bitmap {
        if (!state.hasChanges()) return bitmap
        
        return ImageAdjustEngine.applyFromStateCopy(bitmap, state)
    }
}
