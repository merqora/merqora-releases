package com.rendly.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════════
// ENUMS Y DATA CLASSES
// ═══════════════════════════════════════════════════════════════════════════════

enum class HighlightCategory(
    val displayName: String,
    val icon: ImageVector,
    val gradient: List<Color>
) {
    FASHION("Moda", Icons.Outlined.Checkroom, listOf(Color(0xFF2E8B57), Color(0xFFF472B6))),
    BEAUTY("Belleza", Icons.Outlined.Spa, listOf(Color(0xFFF472B6), Color(0xFFFDA4AF))),
    TECH("Tech", Icons.Outlined.Devices, listOf(Color(0xFF1565A0), Color(0xFF60A5FA))),
    HOME("Hogar", Icons.Outlined.Home, listOf(Color(0xFF2E8B57), Color(0xFF34D399))),
    SPORTS("Deportes", Icons.Outlined.FitnessCenter, listOf(Color(0xFFFF6B35), Color(0xFFFF6B35))),
    FOOD("Comida", Icons.Outlined.Restaurant, listOf(Color(0xFFEF4444), Color(0xFFF87171))),
    TRAVEL("Viajes", Icons.Outlined.Flight, listOf(Color(0xFFFF6B35), Color(0xFF1565A0))),
    MUSIC("Música", Icons.Outlined.MusicNote, listOf(Color(0xFF1565A0), Color(0xFF22D3EE))),
    ART("Arte", Icons.Outlined.Palette, listOf(Color(0xFFD946EF), Color(0xFFE879F9))),
    PETS("Mascotas", Icons.Outlined.Pets, listOf(Color(0xFF84CC16), Color(0xFFA3E635))),
    OFFERS("Ofertas", Icons.Outlined.LocalOffer, listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))),
    NEW("Nuevo", Icons.Outlined.NewReleases, listOf(Color(0xFF4ECDC4), Color(0xFF45B7D1))),
    CUSTOM("Personalizado", Icons.Outlined.AutoAwesome, listOf(Color(0xFF0A3D62), Color(0xFF2E8B57)))
}

enum class HighlightFrameStyle(
    val displayName: String,
    val isPremium: Boolean = false
) {
    CLASSIC("Clásico"),
    THIN("Delgado"),
    BOLD("Grueso"),
    DOUBLE("Doble"),
    DASHED("Punteado"),
    GLOW("Resplandor", true),
    NEON("Neón", true),
    RAINBOW("Arcoíris", true),
    GOLDEN("Dorado", true),
    DIAMOND("Diamante", true)
}

enum class HighlightFrameColor(
    val displayName: String,
    val colors: List<Color>
) {
    CATEGORY("Categoría", emptyList()), // Usa el gradiente de la categoría seleccionada
    PURPLE("Púrpura", listOf(Color(0xFF7C3AED), Color(0xFFA855F7))),
    BLUE("Azul", listOf(Color(0xFF2563EB), Color(0xFF60A5FA))),
    CYAN("Cian", listOf(Color(0xFF0891B2), Color(0xFF22D3EE))),
    GREEN("Verde", listOf(Color(0xFF059669), Color(0xFF34D399))),
    ORANGE("Naranja", listOf(Color(0xFFEA580C), Color(0xFFFB923C))),
    PINK("Rosa", listOf(Color(0xFFDB2777), Color(0xFFF472B6))),
    RED("Rojo", listOf(Color(0xFFDC2626), Color(0xFFF87171))),
    GOLD("Dorado", listOf(Color(0xFFD97706), Color(0xFFFBBF24))),
    WHITE("Blanco", listOf(Color(0xFFE5E7EB), Color(0xFFFFFFFF)))
}

enum class HighlightBackgroundColor(
    val displayName: String,
    val colors: List<Color>
) {
    DEFAULT("Por defecto", listOf(Color(0xFF1A1A2E), Color(0xFF2A2A3E))),
    PURPLE("Púrpura", listOf(Color(0xFF4C1D95), Color(0xFF0A3D62))),
    PINK("Rosa", listOf(Color(0xFF831843), Color(0xFF2E8B57))),
    BLUE("Azul", listOf(Color(0xFF1E3A8A), Color(0xFF1565A0))),
    GREEN("Verde", listOf(Color(0xFF064E3B), Color(0xFF2E8B57))),
    ORANGE("Naranja", listOf(Color(0xFF9A3412), Color(0xFFF97316))),
    CYAN("Cian", listOf(Color(0xFF164E63), Color(0xFF1565A0))),
    GOLD("Dorado", listOf(Color(0xFF78350F), Color(0xFFFF6B35)))
}

private val HIGHLIGHT_ICONS = listOf(
    Icons.Outlined.Star to "Estrella",
    Icons.Outlined.Favorite to "Corazón",
    Icons.Outlined.AutoAwesome to "Magia",
    Icons.Outlined.Diamond to "Diamante",
    Icons.Outlined.EmojiEvents to "Trofeo",
    Icons.Outlined.LocalFireDepartment to "Fuego",
    Icons.Outlined.Bolt to "Rayo",
    Icons.Outlined.Celebration to "Celebración",
    Icons.Outlined.Stars to "Destello",
    Icons.Outlined.Whatshot to "Trending",
    Icons.Outlined.ShoppingBag to "Compras",
    Icons.Outlined.CardGiftcard to "Regalo"
)

// ═══════════════════════════════════════════════════════════════════════════════
// MAIN MODAL - Nuevo diseño con vista previa izquierda + resumen derecha
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHighlightModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCreateHighlight: (title: String, category: String, coverUri: Uri?, frameStyle: String, frameColor: String, backgroundColor: String, icon: String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Estados
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HighlightCategory.CUSTOM) }
    var selectedFrame by remember { mutableStateOf(HighlightFrameStyle.CLASSIC) }
    var selectedFrameColor by remember { mutableStateOf(HighlightFrameColor.CATEGORY) }
    var selectedBackground by remember { mutableStateOf(HighlightBackgroundColor.DEFAULT) }
    var selectedIcon by remember { mutableStateOf(HIGHLIGHT_ICONS[0]) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    
    // Modal activo para personalización
    var activeBottomSheet by remember { mutableStateOf<String?>(null) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { coverUri = it } }
    
    // Reset al abrir
    LaunchedEffect(isVisible) {
        if (isVisible) {
            title = ""
            selectedCategory = HighlightCategory.CUSTOM
            selectedFrame = HighlightFrameStyle.CLASSIC
            selectedFrameColor = HighlightFrameColor.CATEGORY
            selectedBackground = HighlightBackgroundColor.DEFAULT
            selectedIcon = HIGHLIGHT_ICONS[0]
            coverUri = null
            activeBottomSheet = null
        }
    }
    
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = HomeBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header (NO fijo, sigue scroll)
                        ModalHeaderV2(
                            onClose = onDismiss,
                            onSave = {
                                if (title.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCreateHighlight(
                                        title.trim(),
                                        selectedCategory.name,
                                        coverUri,
                                        selectedFrame.name,
                                        selectedFrameColor.name,
                                        selectedBackground.name,
                                        selectedIcon.second
                                    )
                                }
                            },
                            canSave = title.isNotBlank() && !isLoading,
                            isLoading = isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Vista previa centrada (sin resumen)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HighlightPreviewCompact(
                                title = title.ifBlank { "Mi Highlight" },
                                coverUri = coverUri,
                                category = selectedCategory,
                                frameStyle = selectedFrame,
                                frameColor = selectedFrameColor,
                                backgroundColor = selectedBackground,
                                icon = selectedIcon.first
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Campo de título
                        TitleInputCompact(
                            title = title,
                            onTitleChange = { title = it }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Opciones en grid 2x3
                        Text(
                            text = "Personalizar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Grid de opciones 2x3
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Fila 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OptionButton(
                                    icon = Icons.Outlined.Image,
                                    label = "Portada",
                                    value = if (coverUri != null) "✓" else "—",
                                    onClick = { activeBottomSheet = "cover" },
                                    modifier = Modifier.weight(1f)
                                )
                                OptionButton(
                                    icon = Icons.Outlined.Category,
                                    label = "Categoría",
                                    value = selectedCategory.displayName,
                                    onClick = { activeBottomSheet = "category" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // Fila 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OptionButton(
                                    icon = Icons.Outlined.BorderStyle,
                                    label = "Marco",
                                    value = selectedFrame.displayName,
                                    onClick = { activeBottomSheet = "frame" },
                                    modifier = Modifier.weight(1f)
                                )
                                OptionButton(
                                    icon = Icons.Outlined.Palette,
                                    label = "Fondo",
                                    value = selectedBackground.displayName,
                                    onClick = { activeBottomSheet = "background" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // Fila 3
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OptionButton(
                                    icon = Icons.Outlined.EmojiEmotions,
                                    label = "Icono",
                                    value = selectedIcon.second,
                                    onClick = { activeBottomSheet = "icon" },
                                    modifier = Modifier.weight(1f)
                                )
                                OptionButton(
                                    icon = Icons.Outlined.ColorLens,
                                    label = "Color marco",
                                    value = selectedFrameColor.displayName,
                                    onClick = { activeBottomSheet = "frameColor" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
                
                // Bottom Sheets para cada opción
                AnimatedVisibility(
                    visible = activeBottomSheet != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    when (activeBottomSheet) {
                        "cover" -> CoverBottomSheet(
                            coverUri = coverUri,
                            onSelectCover = { imagePicker.launch("image/*") },
                            onRemoveCover = { coverUri = null },
                            onDone = { activeBottomSheet = null }
                        )
                        "category" -> CategoryBottomSheet(
                            selected = selectedCategory,
                            onSelect = { 
                                selectedCategory = it
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDone = { activeBottomSheet = null }
                        )
                        "frame" -> FrameBottomSheet(
                            selected = selectedFrame,
                            category = selectedCategory,
                            frameColor = selectedFrameColor,
                            onSelect = { 
                                selectedFrame = it
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDone = { activeBottomSheet = null }
                        )
                        "frameColor" -> FrameColorBottomSheet(
                            selected = selectedFrameColor,
                            onSelect = {
                                selectedFrameColor = it
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDone = { activeBottomSheet = null }
                        )
                        "background" -> BackgroundBottomSheet(
                            selected = selectedBackground,
                            onSelect = { 
                                selectedBackground = it
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDone = { activeBottomSheet = null }
                        )
                        "icon" -> IconBottomSheet(
                            selected = selectedIcon,
                            category = selectedCategory,
                            onSelect = { 
                                selectedIcon = it
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDone = { activeBottomSheet = null }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HEADER V2
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ModalHeaderV2(
    onClose: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = TextPrimary
            )
        }
        
        Text(
            text = "Nuevo Highlight",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        TextButton(
            onClick = onSave,
            enabled = canSave
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = PrimaryPurple
                )
            } else {
                Text(
                    text = "Crear",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) PrimaryPurple else TextMuted
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW COMPACTO
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Resuelve el gradiente del marco según el estilo, color personalizado y categoría
 */
private fun resolveFrameGradient(
    frameStyle: HighlightFrameStyle,
    frameColor: HighlightFrameColor,
    category: HighlightCategory
): List<Color> {
    // Estilos especiales tienen su propio gradiente
    return when (frameStyle) {
        HighlightFrameStyle.GOLDEN -> listOf(Color(0xFFFFD700), Color(0xFFDAA520), Color(0xFFFFA500), Color(0xFFFFD700))
        HighlightFrameStyle.RAINBOW -> listOf(
            Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
            Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082),
            Color(0xFF8B00FF), Color(0xFFFF0000) // Cierra el ciclo rojo→violeta→rojo
        )
        HighlightFrameStyle.NEON -> listOf(Color(0xFF00FF41), Color(0xFF39FF14), Color(0xFF00FF41))
        HighlightFrameStyle.DIAMOND -> listOf(Color(0xFFB0C4DE), Color(0xFFFFFFFF), Color(0xFFE0E0E0), Color(0xFFB0C4DE))
        else -> {
            // Usar color personalizado si no es CATEGORY
            if (frameColor != HighlightFrameColor.CATEGORY && frameColor.colors.isNotEmpty()) {
                frameColor.colors
            } else {
                category.gradient
            }
        }
    }
}

@Composable
private fun HighlightPreviewCompact(
    title: String,
    coverUri: Uri?,
    category: HighlightCategory,
    frameStyle: HighlightFrameStyle,
    frameColor: HighlightFrameColor,
    backgroundColor: HighlightBackgroundColor,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Vista previa",
            fontSize = 12.sp,
            color = TextMuted
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val previewSize = 100.dp
        val frameGradient = resolveFrameGradient(frameStyle, frameColor, category)
        
        // Preview circle con Canvas para estilos especiales
        Box(
            modifier = Modifier.size(previewSize),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            // Glow animation para Resplandor
            val glowAlpha by if (frameStyle == HighlightFrameStyle.GLOW) {
                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                infiniteTransition.animateFloat(
                    initialValue = 0.4f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
                    label = "glowAlpha"
                )
            } else {
                remember { mutableStateOf(1f) }
            }
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = this.center
                val radius = size.minDimension / 2f
                
                when (frameStyle) {
                    HighlightFrameStyle.GLOW -> {
                        // Resplandor: halo difuminado exterior
                        val glowBrush = Brush.radialGradient(
                            colors = listOf(
                                frameGradient.first().copy(alpha = 0.6f * glowAlpha),
                                frameGradient.last().copy(alpha = 0.3f * glowAlpha),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 1.15f
                        )
                        drawCircle(brush = glowBrush, radius = radius * 1.1f, center = center)
                        // Anillo sólido interior
                        drawCircle(
                            brush = Brush.sweepGradient(frameGradient),
                            radius = radius,
                            center = center
                        )
                    }
                    HighlightFrameStyle.DOUBLE -> {
                        // Doble: dos anillos concéntricos con espacio entre ellos
                        val outerWidth = with(density) { 3.dp.toPx() }
                        val innerRingWidth = with(density) { 2.dp.toPx() }
                        val gap = with(density) { 2.dp.toPx() }
                        val brush = Brush.linearGradient(frameGradient)
                        // Anillo exterior
                        drawCircle(
                            brush = brush,
                            radius = radius,
                            center = center,
                            style = Stroke(width = outerWidth)
                        )
                        // Anillo interior
                        drawCircle(
                            brush = brush,
                            radius = radius - outerWidth - gap,
                            center = center,
                            style = Stroke(width = innerRingWidth)
                        )
                    }
                    HighlightFrameStyle.DASHED -> {
                        // Punteado: patrón de línea discontinua
                        val dashWidth = with(density) { 2.5.dp.toPx() }
                        val brush = Brush.linearGradient(frameGradient)
                        drawCircle(
                            brush = brush,
                            radius = radius - dashWidth / 2,
                            center = center,
                            style = Stroke(
                                width = dashWidth,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(with(density) { 6.dp.toPx() }, with(density) { 4.dp.toPx() }),
                                    0f
                                ),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    HighlightFrameStyle.RAINBOW -> {
                        // Rainbow: sweep gradient completo (cierra el ciclo)
                        drawCircle(
                            brush = Brush.sweepGradient(frameGradient),
                            radius = radius,
                            center = center
                        )
                    }
                    HighlightFrameStyle.NEON -> {
                        // Neón: brillo exterior + anillo nítido
                        val neonColor = frameGradient.first()
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(neonColor.copy(alpha = 0.4f), Color.Transparent),
                                center = center,
                                radius = radius * 1.1f
                            ),
                            radius = radius * 1.08f,
                            center = center
                        )
                        drawCircle(
                            brush = Brush.sweepGradient(frameGradient),
                            radius = radius,
                            center = center
                        )
                    }
                    else -> {
                        // Classic, Thin, Bold: anillo sólido con gradiente
                        drawCircle(
                            brush = Brush.linearGradient(frameGradient),
                            radius = radius,
                            center = center
                        )
                    }
                }
            }
            
            // Tamaño interior según estilo
            val innerSize = when (frameStyle) {
                HighlightFrameStyle.THIN -> 94.dp
                HighlightFrameStyle.BOLD -> 82.dp
                HighlightFrameStyle.DOUBLE -> 82.dp
                HighlightFrameStyle.DASHED -> 88.dp
                HighlightFrameStyle.GLOW -> 84.dp
                HighlightFrameStyle.NEON -> 84.dp
                else -> 88.dp
            }
            
            // Inner content
            Box(
                modifier = Modifier
                    .size(innerSize)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(backgroundColor.colors)),
                contentAlignment = Alignment.Center
            ) {
                if (coverUri != null) {
                    AsyncImage(
                        model = coverUri,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SUMMARY ITEM
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TITLE INPUT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TitleInputCompact(
    title: String,
    onTitleChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Nombre",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 20) onTitleChange(it) },
            placeholder = { Text("Ej: Mis favoritos", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated
            ),
            singleLine = true,
            trailingIcon = {
                Text(
                    text = "${title.length}/20",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// OPTION BUTTON
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun OptionButton(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(22.dp)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = value,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BOTTOM SHEETS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun BottomSheetContainer(
    title: String,
    onDone: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Surface,
        shadowElevation = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                TextButton(onClick = onDone) {
                    Text(
                        text = "Listo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
            }
            
            Divider(color = BorderSubtle)
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }
}

// Cover Bottom Sheet
@Composable
private fun CoverBottomSheet(
    coverUri: Uri?,
    onSelectCover: () -> Unit,
    onRemoveCover: () -> Unit,
    onDone: () -> Unit
) {
    BottomSheetContainer(title = "Portada", onDone = onDone) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .clickable(onClick = onSelectCover),
                contentAlignment = Alignment.Center
            ) {
                if (coverUri != null) {
                    AsyncImage(
                        model = coverUri,
                        contentDescription = "Portada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = "Agregar",
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Text(
                text = if (coverUri != null) "Toca para cambiar" else "Toca para seleccionar",
                fontSize = 13.sp,
                color = TextMuted
            )
            
            if (coverUri != null) {
                TextButton(onClick = onRemoveCover) {
                    Text(
                        text = "Eliminar portada",
                        color = AccentPink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Category Bottom Sheet
@Composable
private fun CategoryBottomSheet(
    selected: HighlightCategory,
    onSelect: (HighlightCategory) -> Unit,
    onDone: () -> Unit
) {
    BottomSheetContainer(title = "Categoría", onDone = onDone) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HighlightCategory.entries) { category ->
                val isSelected = category == selected
                
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) category.gradient.first().copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            brush = Brush.linearGradient(category.gradient),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(category) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(brush = Brush.linearGradient(category.gradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.displayName,
                        fontSize = 9.sp,
                        color = if (isSelected) category.gradient.first() else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Frame Bottom Sheet
@Composable
private fun FrameBottomSheet(
    selected: HighlightFrameStyle,
    category: HighlightCategory,
    frameColor: HighlightFrameColor,
    onSelect: (HighlightFrameStyle) -> Unit,
    onDone: () -> Unit
) {
    BottomSheetContainer(title = "Marco", onDone = onDone) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(HighlightFrameStyle.entries) { frame ->
                val isSelected = frame == selected
                val frameGradient = resolveFrameGradient(frame, frameColor, category)
                
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF0A3D62).copy(alpha = 0.1f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color(0xFF0A3D62) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(frame) }
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Miniatura del marco con Canvas
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val density = LocalDensity.current
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val ctr = this.center
                            val r = size.minDimension / 2f
                            
                            when (frame) {
                                HighlightFrameStyle.GLOW -> {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            listOf(frameGradient.first().copy(alpha = 0.5f), Color.Transparent),
                                            center = ctr, radius = r * 1.15f
                                        ),
                                        radius = r * 1.1f, center = ctr
                                    )
                                    drawCircle(brush = Brush.sweepGradient(frameGradient), radius = r, center = ctr)
                                }
                                HighlightFrameStyle.DOUBLE -> {
                                    val w1 = with(density) { 2.dp.toPx() }
                                    val w2 = with(density) { 1.5.dp.toPx() }
                                    val gap = with(density) { 2.dp.toPx() }
                                    val brush = Brush.linearGradient(frameGradient)
                                    drawCircle(brush = brush, radius = r - w1 / 2, center = ctr, style = Stroke(width = w1))
                                    drawCircle(brush = brush, radius = r - w1 - gap - w2 / 2, center = ctr, style = Stroke(width = w2))
                                }
                                HighlightFrameStyle.DASHED -> {
                                    val w = with(density) { 2.dp.toPx() }
                                    drawCircle(
                                        brush = Brush.linearGradient(frameGradient),
                                        radius = r - w / 2, center = ctr,
                                        style = Stroke(
                                            width = w,
                                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                floatArrayOf(with(density) { 4.dp.toPx() }, with(density) { 3.dp.toPx() }), 0f
                                            ),
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                                HighlightFrameStyle.RAINBOW -> {
                                    drawCircle(brush = Brush.sweepGradient(frameGradient), radius = r, center = ctr)
                                }
                                HighlightFrameStyle.NEON -> {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            listOf(frameGradient.first().copy(alpha = 0.3f), Color.Transparent),
                                            center = ctr, radius = r * 1.1f
                                        ),
                                        radius = r * 1.05f, center = ctr
                                    )
                                    drawCircle(brush = Brush.sweepGradient(frameGradient), radius = r, center = ctr)
                                }
                                else -> {
                                    drawCircle(brush = Brush.linearGradient(frameGradient), radius = r, center = ctr)
                                }
                            }
                        }
                        // Centro interior
                        val innerSz = when (frame) {
                            HighlightFrameStyle.THIN -> 32.dp
                            HighlightFrameStyle.BOLD -> 24.dp
                            HighlightFrameStyle.DOUBLE -> 24.dp
                            HighlightFrameStyle.DASHED -> 28.dp
                            HighlightFrameStyle.GLOW -> 26.dp
                            HighlightFrameStyle.NEON -> 26.dp
                            else -> 28.dp
                        }
                        Box(
                            modifier = Modifier
                                .size(innerSz)
                                .clip(CircleShape)
                                .background(HomeBg)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = frame.displayName,
                        fontSize = 9.sp,
                        color = if (isSelected) Color(0xFF0A3D62) else TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    if (frame.isPremium) {
                        Text(
                            text = "PRO",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                }
            }
        }
    }
}

// Frame Color Bottom Sheet
@Composable
private fun FrameColorBottomSheet(
    selected: HighlightFrameColor,
    onSelect: (HighlightFrameColor) -> Unit,
    onDone: () -> Unit
) {
    BottomSheetContainer(title = "Color del marco", onDone = onDone) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(HighlightFrameColor.entries) { color ->
                val isSelected = color == selected
                val displayColors = if (color == HighlightFrameColor.CATEGORY) {
                    listOf(Color(0xFF0A3D62), Color(0xFF2E8B57)) // Placeholder para "Categoría"
                } else {
                    color.colors
                }
                
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(color) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(brush = Brush.linearGradient(displayColors))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) PrimaryPurple else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (color == HighlightFrameColor.CATEGORY) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = color.displayName,
                        fontSize = 9.sp,
                        color = if (isSelected) PrimaryPurple else TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Background Bottom Sheet
@Composable
private fun BackgroundBottomSheet(
    selected: HighlightBackgroundColor,
    onSelect: (HighlightBackgroundColor) -> Unit,
    onDone: () -> Unit
) {
    BottomSheetContainer(title = "Color de fondo", onDone = onDone) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(HighlightBackgroundColor.entries) { bg ->
                val isSelected = bg == selected
                
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(bg) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(brush = Brush.linearGradient(bg.colors))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) PrimaryPurple else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bg.displayName,
                        fontSize = 10.sp,
                        color = if (isSelected) PrimaryPurple else TextSecondary
                    )
                }
            }
        }
    }
}

// Icon Bottom Sheet
@Composable
private fun IconBottomSheet(
    selected: Pair<ImageVector, String>,
    category: HighlightCategory,
    onSelect: (Pair<ImageVector, String>) -> Unit,
    onDone: () -> Unit
) {
    BottomSheetContainer(title = "Icono", onDone = onDone) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HIGHLIGHT_ICONS) { iconPair ->
                val isSelected = iconPair == selected
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Brush.linearGradient(category.gradient)
                            else Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated))
                        )
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = BorderSubtle,
                            shape = CircleShape
                        )
                        .clickable { onSelect(iconPair) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconPair.first,
                        contentDescription = iconPair.second,
                        tint = if (isSelected) Color.White else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
