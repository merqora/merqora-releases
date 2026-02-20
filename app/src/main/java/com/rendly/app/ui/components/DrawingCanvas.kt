package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════
// SISTEMA DE DIBUJO ULTRA-OPTIMIZADO
// - Sin recomposiciones durante el dibujo
// - Usa Path nativo para máximo rendimiento
// - Canvas con drawIntoCanvas para acceso directo a Android Canvas
// ═══════════════════════════════════════════════════════════════

// Representa un trazo individual - CADA trazo guarda su propia herramienta
data class DrawingStroke(
    val points: MutableList<Offset> = mutableListOf(),
    val color: Color,
    val strokeWidth: Float,
    val tool: DrawingTool = DrawingTool.PEN,
    val isEraser: Boolean = false
)

// Colores disponibles para dibujar
val DRAWING_COLORS = listOf(
    Color.White,
    Color.Black,
    Color(0xFFEF4444), // Rojo
    Color(0xFFF97316), // Naranja
    Color(0xFFEAB308), // Amarillo
    Color(0xFF22C55E), // Verde
    Color(0xFF1565A0), // Cyan
    Color(0xFF1565A0), // Azul
    Color(0xFFFF6B35), // Violeta
    Color(0xFF2E8B57), // Rosa
    Color(0xFFF472B6), // Rosa claro
    Color(0xFF14B8A6), // Teal
    Color(0xFF84CC16), // Lima
    Color(0xFF6366F1), // Índigo
    Color(0xFFA855F7), // Púrpura
    Color(0xFFD946EF), // Fucsia
    Color(0xFF78716C), // Gris
    Color(0xFF92400E), // Marrón
)

// Herramientas de dibujo
enum class DrawingTool {
    PEN,
    MARKER,
    NEON,
    ERASER
}

@Composable
fun DrawingOverlay(
    visible: Boolean,
    selectedColor: Color,
    selectedTool: DrawingTool,
    strokeWidth: Float,
    strokes: MutableList<DrawingStroke>,
    modifier: Modifier = Modifier,
    onToolChange: (DrawingTool) -> Unit,
    onUndo: () -> Unit,
    onApply: () -> Unit,
    onStrokesChanged: (List<DrawingStroke>) -> Unit
) {
    var currentStroke by remember { mutableStateOf<DrawingStroke?>(null) }
    
    // Trigger para forzar redibujado sin recomposición
    var drawTrigger by remember { mutableIntStateOf(0) }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(150)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ═══════════════════════════════════════════════════════════════
            // CANVAS DE DIBUJO - Ultra optimizado
            // ═══════════════════════════════════════════════════════════════
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .pointerInput(selectedTool, selectedColor, strokeWidth) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            
                            // Crear nuevo trazo CON la herramienta actual guardada
                            val newStroke = DrawingStroke(
                                points = mutableListOf(down.position),
                                color = if (selectedTool == DrawingTool.ERASER) Color.Transparent else selectedColor,
                                strokeWidth = if (selectedTool == DrawingTool.ERASER) strokeWidth * 3 else strokeWidth,
                                tool = selectedTool,
                                isEraser = selectedTool == DrawingTool.ERASER
                            )
                            currentStroke = newStroke
                            drawTrigger++
                            
                            down.consume()
                            
                            var anyPressed = true
                            while (anyPressed) {
                                val event = awaitPointerEvent()
                                
                                event.changes.forEach { change ->
                                    if (change.pressed && change.positionChanged()) {
                                        currentStroke?.points?.add(change.position)
                                        drawTrigger++
                                        change.consume()
                                    }
                                }
                                
                                anyPressed = event.changes.any { it.pressed }
                            }
                            
                            // Trazo terminado - agregar a la lista
                            currentStroke?.let { stroke ->
                                if (stroke.points.size > 1) {
                                    strokes.add(stroke)
                                    onStrokesChanged(strokes.toList())
                                }
                            }
                            currentStroke = null
                        }
                    }
            ) {
                // Forzar lectura del trigger para actualizar
                drawTrigger.let { _ ->
                    // Dibujar todos los trazos - CADA UNO con su propia herramienta
                    strokes.forEach { stroke ->
                        drawStrokeWithTool(stroke)
                    }
                    
                    // Dibujar trazo actual
                    currentStroke?.let { stroke ->
                        drawStrokeWithTool(stroke)
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // BOTÓN DESHACER - Esquina superior IZQUIERDA
            // ═══════════════════════════════════════════════════════════════
            if (strokes.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onUndo() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Undo,
                        contentDescription = "Deshacer",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // HERRAMIENTAS - Esquina superior DERECHA
            // ═══════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pluma
                DrawingToolButtonIndividual(
                    icon = Icons.Outlined.Edit,
                    selected = selectedTool == DrawingTool.PEN,
                    onClick = { onToolChange(DrawingTool.PEN) }
                )
                
                // Marcador
                DrawingToolButtonIndividual(
                    icon = Icons.Outlined.Brush,
                    selected = selectedTool == DrawingTool.MARKER,
                    onClick = { onToolChange(DrawingTool.MARKER) }
                )
                
                // Neón
                DrawingToolButtonIndividual(
                    icon = Icons.Outlined.AutoAwesome,
                    selected = selectedTool == DrawingTool.NEON,
                    onClick = { onToolChange(DrawingTool.NEON) }
                )
                
                // Borrador
                DrawingToolButtonIndividual(
                    icon = Icons.Outlined.Delete,
                    selected = selectedTool == DrawingTool.ERASER,
                    onClick = { onToolChange(DrawingTool.ERASER) }
                )
                
                // Botón Aplicar (Tick) - Mismo color que los demás
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onApply() },
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

// Función para dibujar trazo con su herramienta guardada
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrokeWithTool(
    stroke: DrawingStroke
) {
    if (stroke.points.size < 2) return
    
    val path = Path().apply {
        moveTo(stroke.points.first().x, stroke.points.first().y)
        
        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val current = stroke.points[i]
            val mid = Offset(
                (prev.x + current.x) / 2f,
                (prev.y + current.y) / 2f
            )
            quadraticBezierTo(prev.x, prev.y, mid.x, mid.y)
        }
        
        lineTo(stroke.points.last().x, stroke.points.last().y)
    }
    
    if (stroke.isEraser) {
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(
                width = stroke.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            blendMode = BlendMode.Clear
        )
    } else {
        // Usar la herramienta GUARDADA en el trazo
        when (stroke.tool) {
            DrawingTool.NEON -> {
                drawPath(
                    path = path,
                    color = stroke.color.copy(alpha = 0.3f),
                    style = Stroke(width = stroke.strokeWidth * 4, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = stroke.color.copy(alpha = 0.5f),
                    style = Stroke(width = stroke.strokeWidth * 2, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = stroke.strokeWidth * 0.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            DrawingTool.MARKER -> {
                drawPath(
                    path = path,
                    color = stroke.color.copy(alpha = 0.7f),
                    style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Square, join = StrokeJoin.Round)
                )
            }
            else -> {
                drawPath(
                    path = path,
                    color = stroke.color,
                    style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@Composable
private fun DrawingToolButtonIndividual(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// CARRUSEL DE COLORES - Componente exportable para HistoryScreen
// ═══════════════════════════════════════════════════════════════
@Composable
fun DrawingColorCarousel(
    selectedColor: Color,
    selectedTool: DrawingTool,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
    ) {
        items(DRAWING_COLORS) { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (selectedColor == color && selectedTool != DrawingTool.ERASER) {
                            Modifier.border(3.dp, Color.White, CircleShape)
                        } else {
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        }
                    )
                    .clickable { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == color && selectedTool != DrawingTool.ERASER) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (color == Color.White) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SLIDER DE GROSOR VERTICAL PROFESIONAL - Estilo iOS/Instagram
// Diseño minimalista con indicador visual del tamaño actual
// ═══════════════════════════════════════════════════════════════
@Composable
fun DrawingStrokeSliderVertical(
    strokeWidth: Float,
    selectedColor: Color,
    onStrokeWidthChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val minStroke = 2f
    val maxStroke = 30f
    val normalizedValue = (strokeWidth - minStroke) / (maxStroke - minStroke)
    
    // Animación suave del indicador
    val animatedSize by animateFloatAsState(
        targetValue = strokeWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "strokeSize"
    )
    
    // Sin fondo - diseño limpio
    Column(
        modifier = modifier
            .width(50.dp)
            .height(220.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Indicador visual del tamaño actual (separado arriba del slider)
        Box(
            modifier = Modifier
                .size(animatedSize.dp.coerceIn(8.dp, 32.dp))
                .clip(CircleShape)
                .background(selectedColor)
                .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
        )
        
        // Track vertical personalizado
        var currentY by remember { mutableFloatStateOf(0f) }
        var trackHeight by remember { mutableFloatStateOf(1f) }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .width(40.dp)
                .pointerInput(Unit) {
                    trackHeight = size.height.toFloat()
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            currentY = offset.y
                            val newValue = 1f - (currentY / trackHeight).coerceIn(0f, 1f)
                            onStrokeWidthChange(minStroke + newValue * (maxStroke - minStroke))
                        },
                        onVerticalDrag = { change, dragAmount ->
                            currentY = change.position.y
                            val dragValue = 1f - (currentY / trackHeight).coerceIn(0f, 1f)
                            onStrokeWidthChange(minStroke + dragValue * (maxStroke - minStroke))
                            change.consume()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Track de fondo
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
            
            // Track activo (desde abajo hasta el valor actual)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(normalizedValue)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(2.dp))
                    .background(selectedColor)
            )
            
            // Thumb/Indicador de posición - posicionado según el valor
            val thumbOffsetY = ((1f - normalizedValue) * 150).dp - 75.dp
            Box(
                modifier = Modifier
                    .offset(y = thumbOffsetY)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, selectedColor, CircleShape)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CANVAS DE DIBUJO ESTÁTICO - Para renderizar sobre la imagen
// ═══════════════════════════════════════════════════════════════
@Composable
fun DrawingCanvasStatic(
    strokes: List<DrawingStroke>,
    modifier: Modifier = Modifier
) {
    if (strokes.isEmpty()) return
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        strokes.forEach { stroke ->
            drawStrokeWithTool(stroke)
        }
    }
}
