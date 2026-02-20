package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.unit.IntOffset

// ═══════════════════════════════════════════════════════════════
// DEFINICIONES DE HERRAMIENTAS DE TEXTO
// ═══════════════════════════════════════════════════════════════

enum class TextToolType {
    FONT, COLOR, ANIMATION, EFFECTS, ALIGNMENT, BACKGROUND
}

data class TextTool(
    val type: TextToolType,
    val icon: ImageVector,
    val hasCarousel: Boolean = true // false para toggles (alignment, background)
)

val TEXT_TOOLS = listOf(
    TextTool(TextToolType.FONT, Icons.Outlined.TextFields),
    TextTool(TextToolType.COLOR, Icons.Outlined.Palette),
    TextTool(TextToolType.ANIMATION, Icons.Outlined.Animation),
    TextTool(TextToolType.EFFECTS, Icons.Outlined.AutoAwesome),
    TextTool(TextToolType.ALIGNMENT, Icons.Outlined.FormatAlignCenter, hasCarousel = false),
    TextTool(TextToolType.BACKGROUND, Icons.Outlined.FormatColorFill, hasCarousel = false)
)

// Fuentes disponibles
data class FontOption(
    val id: String,
    val name: String,
    val fontFamily: FontFamily,
    val fontWeight: FontWeight = FontWeight.Normal
)

val FONT_OPTIONS = listOf(
    FontOption("default", "Normal", FontFamily.Default),
    FontOption("serif", "Serif", FontFamily.Serif),
    FontOption("mono", "Mono", FontFamily.Monospace),
    FontOption("bold", "Bold", FontFamily.Default, FontWeight.Bold),
    FontOption("light", "Light", FontFamily.Default, FontWeight.Light),
    FontOption("cursive", "Cursive", FontFamily.Cursive)
)

// Colores de texto disponibles
val TEXT_COLORS = listOf(
    Color.White,
    Color.Black,
    Color(0xFFFF6B6B), // Rojo
    Color(0xFFFFE66D), // Amarillo
    Color(0xFF4ECDC4), // Turquesa
    Color(0xFF95E1D3), // Verde menta
    Color(0xFFF38181), // Coral
    Color(0xFFAA96DA), // Lavanda
    Color(0xFFFF9F43), // Naranja
    Color(0xFF74B9FF), // Azul claro
    Color(0xFFE056FD), // Magenta
    Color(0xFF00D2D3)  // Cyan
)

// Animaciones de texto
data class TextAnimation(
    val id: String,
    val name: String,
    val icon: ImageVector
)

val TEXT_ANIMATIONS = listOf(
    TextAnimation("none", "Sin", Icons.Outlined.Block),
    TextAnimation("fade", "Fade", Icons.Outlined.Visibility),
    TextAnimation("slide", "Slide", Icons.Outlined.SwipeRight),
    TextAnimation("bounce", "Bounce", Icons.Outlined.KeyboardDoubleArrowUp),
    TextAnimation("typewriter", "Type", Icons.Outlined.Keyboard),
    TextAnimation("glow", "Glow", Icons.Outlined.LightMode)
)

// Efectos de texto
data class TextEffect(
    val id: String,
    val name: String,
    val icon: ImageVector
)

val TEXT_EFFECTS = listOf(
    TextEffect("none", "Sin", Icons.Outlined.Block),
    TextEffect("shadow", "Sombra", Icons.Outlined.Layers),
    TextEffect("outline", "Borde", Icons.Outlined.BorderStyle),
    TextEffect("neon", "Neon", Icons.Outlined.FlashOn),
    TextEffect("gradient", "Grad", Icons.Outlined.Gradient)
)

// Alineaciones
enum class TextAlignOption {
    LEFT, CENTER, RIGHT
}

// ═══════════════════════════════════════════════════════════════
// ESTADO DEL EDITOR DE TEXTO
// ═══════════════════════════════════════════════════════════════

// Estados de fondo de texto
enum class TextBackgroundState {
    NONE,      // Sin fondo
    BLACK,     // Fondo negro, texto del color seleccionado
    WHITE      // Fondo blanco, texto negro
}

data class StoryTextState(
    val text: String = "",
    val fontSize: Float = 24f, // Controlado por el slider vertical
    val fontOption: FontOption = FONT_OPTIONS.first(),
    val color: Color = Color.White,
    val animation: TextAnimation = TEXT_ANIMATIONS.first(),
    val effect: TextEffect = TEXT_EFFECTS.first(),
    val alignment: TextAlignOption = TextAlignOption.CENTER,
    val backgroundState: TextBackgroundState = TextBackgroundState.NONE
)

// ═══════════════════════════════════════════════════════════════
// EDITOR DE TEXTO PRINCIPAL
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StoryTextEditor(
    visible: Boolean,
    keyboardHeight: Dp = 0.dp, // Fallback, se detecta internamente
    previewHeight: Dp,
    onTextStateChanged: (StoryTextState) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textState by remember { mutableStateOf(StoryTextState()) }
    var selectedTool by remember { mutableStateOf<TextToolType?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    
    // Detectar altura real del teclado usando WindowInsets
    val imeInsets = WindowInsets.ime
    val imeHeight = with(density) { imeInsets.getBottom(density).toDp() }
    val actualKeyboardHeight = if (imeHeight > 0.dp) imeHeight else keyboardHeight
    val isKeyboardVisible = imeHeight > 50.dp // Considerar visible si > 50dp
    
    // Abrir teclado cuando se hace visible
    LaunchedEffect(visible) {
        if (visible) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
    
    // Notificar cambios de estado
    LaunchedEffect(textState) {
        onTextStateChanged(textState)
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Altura de la barra de herramientas + carrusel de opciones
            val toolbarHeight = 52.dp
            val carouselHeight = if (selectedTool != null && TEXT_TOOLS.find { it.type == selectedTool }?.hasCarousel == true) 70.dp else 0.dp
            val totalToolsHeight = toolbarHeight + carouselHeight
            
            // ═══════════════════════════════════════════════════════════════
            // CÁLCULO DE CENTRO PERFECTO - FIJO
            // El texto y slider deben estar centrados entre:
            // - Top: status bar (~40dp)
            // - Bottom: borde superior del teclado
            // Usamos altura FIJA sin contar el carrusel para evitar movimiento
            // ═══════════════════════════════════════════════════════════════
            // Altura fija para el cálculo - NO incluye el carrusel para evitar que suba el texto
            val fixedToolbarHeight = 52.dp
            val availableHeight = (previewHeight - actualKeyboardHeight - fixedToolbarHeight).coerceAtLeast(100.dp)
            // Centrar un poco más abajo para compensar el status bar
            val centerY = (availableHeight / 2).coerceAtLeast(60.dp) + 20.dp
            
            // Campo de texto centrado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = centerY) // Posición fija centrada
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = textState.text,
                    onValueChange = { textState = textState.copy(text = it) },
                    textStyle = TextStyle(
                        color = if (textState.backgroundState == TextBackgroundState.WHITE) Color.Black else textState.color,
                        fontSize = textState.fontSize.sp,
                        fontFamily = textState.fontOption.fontFamily,
                        fontWeight = textState.fontOption.fontWeight,
                        textAlign = when (textState.alignment) {
                            TextAlignOption.LEFT -> TextAlign.Start
                            TextAlignOption.CENTER -> TextAlign.Center
                            TextAlignOption.RIGHT -> TextAlign.End
                        }
                    ),
                    cursorBrush = SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onDismiss() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .then(
                            when (textState.backgroundState) {
                                TextBackgroundState.BLACK -> Modifier
                                    .background(
                                        Color.Black.copy(alpha = 0.85f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                TextBackgroundState.WHITE -> Modifier
                                    .background(
                                        Color.White,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                TextBackgroundState.NONE -> Modifier
                            }
                        ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = when (textState.alignment) {
                                TextAlignOption.LEFT -> Alignment.CenterStart
                                TextAlignOption.CENTER -> Alignment.Center
                                TextAlignOption.RIGHT -> Alignment.CenterEnd
                            }
                        ) {
                            // Mostrar placeholder solo cuando está vacío
                            if (textState.text.isEmpty()) {
                                Text(
                                    text = "Escribe algo...",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = minOf(textState.fontSize, 28f).sp,
                                    fontFamily = textState.fontOption.fontFamily,
                                    fontWeight = textState.fontOption.fontWeight
                                )
                            }
                            // El campo de texto real - cursor aparece al principio
                            innerTextField()
                        }
                    }
                )
            }
            
            // Slider vertical de tamaño - lado izquierdo, centrado junto al texto
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp)
                    .padding(top = centerY - 40.dp) // Alineado con el centro del texto, posición fija
                    .height(140.dp)
            ) {
                VerticalFontSizeSlider(
                    value = textState.fontSize,
                    onValueChange = { textState = textState.copy(fontSize = it) },
                    valueRange = 16f..48f
                )
            }
            
            // Botón "Tick" - esquina superior derecha
            // Mismo estilo que botón arrow left (fondo negro 0.5 alpha, 40dp)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Confirmar",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // Contenedor de herramientas - solo visible cuando el teclado está abierto
            // PEGADO AL TECLADO SIN NINGUNA SEPARACIÓN
            AnimatedVisibility(
                visible = isKeyboardVisible,
                enter = fadeIn(tween(150)) + slideInVertically { it },
                exit = fadeOut(tween(100)) + slideOutVertically { it },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .imePadding() // Usa imePadding para pegarse perfectamente al teclado
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // SIN padding bottom adicional - imePadding maneja todo
                ) {
                // Carrusel de opciones (si hay herramienta seleccionada con carrusel)
                AnimatedVisibility(
                    visible = selectedTool != null && TEXT_TOOLS.find { it.type == selectedTool }?.hasCarousel == true,
                    enter = fadeIn(tween(150)) + slideInVertically { it },
                    exit = fadeOut(tween(100)) + slideOutVertically { it }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp)
                    ) {
                        when (selectedTool) {
                            TextToolType.FONT -> FontCarousel(
                                selected = textState.fontOption,
                                onSelect = { textState = textState.copy(fontOption = it) }
                            )
                            TextToolType.COLOR -> ColorCarousel(
                                selected = textState.color,
                                onSelect = { textState = textState.copy(color = it) }
                            )
                            TextToolType.ANIMATION -> AnimationCarousel(
                                selected = textState.animation,
                                onSelect = { textState = textState.copy(animation = it) }
                            )
                            TextToolType.EFFECTS -> EffectsCarousel(
                                selected = textState.effect,
                                onSelect = { textState = textState.copy(effect = it) }
                            )
                            else -> {}
                        }
                    }
                }
                
                // Barra de herramientas principal
                TextToolbar(
                    selectedTool = selectedTool,
                    alignment = textState.alignment,
                    backgroundState = textState.backgroundState,
                    onToolSelected = { tool ->
                        when {
                            tool.type == TextToolType.ALIGNMENT -> {
                                // Toggle alignment
                                val newAlign = when (textState.alignment) {
                                    TextAlignOption.LEFT -> TextAlignOption.CENTER
                                    TextAlignOption.CENTER -> TextAlignOption.RIGHT
                                    TextAlignOption.RIGHT -> TextAlignOption.LEFT
                                }
                                textState = textState.copy(alignment = newAlign)
                            }
                            tool.type == TextToolType.BACKGROUND -> {
                                // Ciclo de fondo: NONE -> BLACK -> WHITE -> NONE
                                val newBg = when (textState.backgroundState) {
                                    TextBackgroundState.NONE -> TextBackgroundState.BLACK
                                    TextBackgroundState.BLACK -> TextBackgroundState.WHITE
                                    TextBackgroundState.WHITE -> TextBackgroundState.NONE
                                }
                                textState = textState.copy(backgroundState = newBg)
                            }
                            else -> {
                                // Toggle carrusel
                                selectedTool = if (selectedTool == tool.type) null else tool.type
                            }
                        }
                    }
                )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SLIDER VERTICAL DE TAMAÑO DE FUENTE
// Mismo estilo que DrawingStrokeSliderVertical para consistencia
// ═══════════════════════════════════════════════════════════════

@Composable
private fun VerticalFontSizeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val minValue = valueRange.start
    val maxValue = valueRange.endInclusive
    val normalizedValue = (value - minValue) / (maxValue - minValue)
    
    // Animación suave del indicador
    val animatedSize by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fontSize"
    )
    
    // Diseño profesional con indicador arriba y track debajo (sin superposición)
    Column(
        modifier = modifier
            .width(44.dp)
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicador visual del tamaño actual - pequeño y elegante
        val indicatorSize = (animatedSize * 0.45f).dp.coerceIn(10.dp, 20.dp)
        Box(
            modifier = Modifier
                .size(indicatorSize)
                .clip(CircleShape)
                .background(Color.White)
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Track vertical personalizado - DEBAJO del indicador, sin superposición
        var currentY by remember { mutableFloatStateOf(0f) }
        var trackHeight by remember { mutableFloatStateOf(1f) }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp)
                .pointerInput(Unit) {
                    trackHeight = size.height.toFloat()
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentY = offset.y
                            val newNormalized = 1f - (currentY / trackHeight).coerceIn(0f, 1f)
                            onValueChange(minValue + newNormalized * (maxValue - minValue))
                        },
                        onDrag = { change, _ ->
                            currentY = change.position.y
                            val dragValue = 1f - (currentY / trackHeight).coerceIn(0f, 1f)
                            onValueChange(minValue + dragValue * (maxValue - minValue))
                            change.consume()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Track de fondo - más delgado y elegante
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            )
            
            // Track activo (desde abajo hasta el valor actual)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(normalizedValue)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color.White.copy(alpha = 0.9f))
            )
            
            // Thumb/Handle - posicionado según el valor, pequeño y preciso
            val trackFraction = 1f - normalizedValue
            Box(
                modifier = Modifier
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = with(androidx.compose.ui.platform.LocalDensity.current) {
                            // Posicionar el thumb en la fracción correcta del track
                            val totalHeight = trackHeight
                            (totalHeight * trackFraction).toDp() - 7.dp
                        }.coerceAtLeast(0.dp))
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// BARRA DE HERRAMIENTAS DE TEXTO
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TextToolbar(
    selectedTool: TextToolType?,
    alignment: TextAlignOption,
    backgroundState: TextBackgroundState,
    onToolSelected: (TextTool) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBackground = backgroundState != TextBackgroundState.NONE
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A)) // Fondo gris oscuro
            .padding(horizontal = 24.dp, vertical = 8.dp), // Padding reducido
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TEXT_TOOLS.forEach { tool ->
            val isSelected = selectedTool == tool.type
            val isBackgroundActive = tool.type == TextToolType.BACKGROUND && hasBackground
            
            // Animaciones GPU fluidas
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "tool_scale_${tool.type}"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isSelected || isBackgroundActive) 1f else 0.7f,
                animationSpec = tween(150),
                label = "tool_alpha_${tool.type}"
            )
            
            val icon = when (tool.type) {
                TextToolType.ALIGNMENT -> when (alignment) {
                    TextAlignOption.LEFT -> Icons.Outlined.FormatAlignLeft
                    TextAlignOption.CENTER -> Icons.Outlined.FormatAlignCenter
                    TextAlignOption.RIGHT -> Icons.Outlined.FormatAlignRight
                }
                TextToolType.BACKGROUND -> when (backgroundState) {
                    TextBackgroundState.NONE -> Icons.Outlined.FormatColorFill
                    TextBackgroundState.BLACK -> Icons.Filled.FormatColorFill
                    TextBackgroundState.WHITE -> Icons.Filled.FormatColorFill
                }
                else -> tool.icon
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f) 
                        else Color.Transparent
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToolSelected(tool) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { this.alpha = alpha }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CARRUSELES DE OPCIONES
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FontCarousel(
    selected: FontOption,
    onSelect: (FontOption) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(FONT_OPTIONS, key = { it.id }) { font ->
            val isSelected = font.id == selected.id
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else Color.Black.copy(alpha = 0.3f)
                    )
                    .clickable { onSelect(font) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aa",
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontFamily = font.fontFamily,
                    fontWeight = font.fontWeight
                )
            }
        }
    }
}

@Composable
private fun ColorCarousel(
    selected: Color,
    onSelect: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(TEXT_COLORS) { color ->
            val isSelected = color == selected
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                        else Modifier
                    )
                    .background(color)
                    .clickable { onSelect(color) }
            )
        }
    }
}

@Composable
private fun AnimationCarousel(
    selected: TextAnimation,
    onSelect: (TextAnimation) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(TEXT_ANIMATIONS, key = { it.id }) { anim ->
            val isSelected = anim.id == selected.id
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else Color.Black.copy(alpha = 0.3f)
                    )
                    .clickable { onSelect(anim) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = anim.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EffectsCarousel(
    selected: TextEffect,
    onSelect: (TextEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(TEXT_EFFECTS, key = { it.id }) { effect ->
            val isSelected = effect.id == selected.id
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else Color.Black.copy(alpha = 0.3f)
                    )
                    .clickable { onSelect(effect) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = effect.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
