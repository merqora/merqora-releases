package com.vinzay.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vinzay.app.ui.theme.*

// Usar HighlightCategory de AddHighlightModal.kt
// (La definición está en AddHighlightModal.kt para evitar duplicación)

data class HighlightedStory(
    val id: String,
    val title: String,
    val thumbnail: String?,
    val category: HighlightCategory = HighlightCategory.CUSTOM,
    val frameStyle: HighlightFrameStyle = HighlightFrameStyle.CLASSIC,
    val frameColor: HighlightFrameColor = HighlightFrameColor.CATEGORY,
    val backgroundColor: HighlightBackgroundColor = HighlightBackgroundColor.DEFAULT,
    val icon: String = "Star",
    val count: Int = 0
)

@Composable
fun HighlightedStories(
    stories: List<HighlightedStory>,
    onStoryPress: (HighlightedStory) -> Unit,
    onStoryLongPress: (HighlightedStory) -> Unit = {},
    onAddStory: () -> Unit = {},
    canAddStories: Boolean = true,
    uploadingHighlightId: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón agregar highlight
        if (canAddStories) {
            AddHighlightButton(onClick = onAddStory)
        }
        
        // Highlights existentes
        stories.forEach { story ->
            HighlightItem(
                story = story,
                onClick = { onStoryPress(story) },
                onLongClick = { onStoryLongPress(story) },
                isUploading = story.id == uploadingHighlightId
            )
        }
    }
}

@Composable
private fun AddHighlightButton(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SurfaceElevated)
                .border(
                    width = 2.dp,
                    brush = RiverBrushSweep,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar highlight",
                tint = PrimaryPurple,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = "Nuevo",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HighlightItem(
    story: HighlightedStory,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isUploading: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    
    // Animación de rotación para loading
    val infiniteTransition = rememberInfiniteTransition(label = "upload_spin")
    val uploadRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "upload_rotation"
    )
    // Animación de pulso para el borde al subir
    val uploadAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "upload_alpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onLongClickLabel = "Opciones"
                ),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            
            // Obtener gradiente según el frame style y color personalizado
            val frameGradient = when (story.frameStyle) {
                HighlightFrameStyle.GOLDEN -> listOf(Color(0xFFFFD700), Color(0xFFDAA520), Color(0xFFFFA500), Color(0xFFFFD700))
                HighlightFrameStyle.RAINBOW -> listOf(
                    Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
                    Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082),
                    Color(0xFF8B00FF), Color(0xFFFF0000)
                )
                HighlightFrameStyle.NEON -> listOf(Color(0xFF00FF41), Color(0xFF39FF14), Color(0xFF00FF41))
                HighlightFrameStyle.DIAMOND -> listOf(Color(0xFFB0C4DE), Color(0xFFFFFFFF), Color(0xFFE0E0E0), Color(0xFFB0C4DE))
                else -> {
                    if (story.frameColor != HighlightFrameColor.CATEGORY && story.frameColor.colors.isNotEmpty()) {
                        story.frameColor.colors
                    } else {
                        story.category.gradient
                    }
                }
            }
            
            // Canvas para dibujar frame con estilos distintos (o loading animation)
            Canvas(modifier = Modifier.fillMaxSize().then(
                if (isUploading) Modifier.graphicsLayer { rotationZ = uploadRotation } else Modifier
            )) {
                val radius = size.minDimension / 2f
                val ctr = center
                
                if (isUploading) {
                    // Loading animation: rotating dashed gradient arc
                    val strokeW = with(density) { 3.dp.toPx() }
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                PrimaryPurple.copy(alpha = uploadAlpha),
                                AccentPink.copy(alpha = uploadAlpha),
                                PrimaryPurple.copy(alpha = 0.1f)
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(strokeW / 2, strokeW / 2),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeW, size.height - strokeW),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                } else
                when (story.frameStyle) {
                    HighlightFrameStyle.GLOW -> {
                        val neonColor = frameGradient.first()
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(neonColor.copy(alpha = 0.5f), Color.Transparent),
                                center = ctr, radius = radius * 1.1f
                            ),
                            radius = radius, center = ctr
                        )
                        drawCircle(brush = Brush.sweepGradient(frameGradient), radius = radius, center = ctr)
                    }
                    HighlightFrameStyle.DOUBLE -> {
                        val outerW = with(density) { 2.5.dp.toPx() }
                        val innerW = with(density) { 2.dp.toPx() }
                        val gap = with(density) { 2.dp.toPx() }
                        val brush = Brush.linearGradient(frameGradient)
                        drawCircle(brush = brush, radius = radius - outerW / 2, center = ctr, style = Stroke(width = outerW))
                        drawCircle(brush = brush, radius = radius - outerW - gap - innerW / 2, center = ctr, style = Stroke(width = innerW))
                    }
                    HighlightFrameStyle.DASHED -> {
                        val dashW = with(density) { 2.5.dp.toPx() }
                        drawCircle(
                            brush = Brush.linearGradient(frameGradient),
                            radius = radius - dashW / 2,
                            center = ctr,
                            style = Stroke(
                                width = dashW,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(with(density) { 6.dp.toPx() }, with(density) { 4.dp.toPx() }), 0f
                                ),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    HighlightFrameStyle.RAINBOW -> {
                        drawCircle(brush = Brush.sweepGradient(frameGradient), radius = radius, center = ctr)
                    }
                    HighlightFrameStyle.NEON -> {
                        val neonColor = frameGradient.first()
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(neonColor.copy(alpha = 0.4f), Color.Transparent),
                                center = ctr, radius = radius * 1.1f
                            ),
                            radius = radius * 1.08f, center = ctr
                        )
                        drawCircle(brush = Brush.sweepGradient(frameGradient), radius = radius, center = ctr)
                    }
                    else -> {
                        drawCircle(brush = Brush.linearGradient(frameGradient), radius = radius, center = ctr)
                    }
                }
            }
            
            // Tamaño interior según estilo
            val innerSize = when (story.frameStyle) {
                HighlightFrameStyle.THIN -> 60.dp
                HighlightFrameStyle.BOLD -> 54.dp
                HighlightFrameStyle.DOUBLE -> 52.dp
                HighlightFrameStyle.DASHED -> 56.dp
                HighlightFrameStyle.GLOW -> 54.dp
                HighlightFrameStyle.NEON -> 54.dp
                else -> 58.dp
            }
            
            // Contenido interno
            Box(
                modifier = Modifier
                    .size(innerSize)
                    .clip(CircleShape)
                    .background(HomeBg),
                contentAlignment = Alignment.Center
            ) {
                if (story.thumbnail != null) {
                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    AsyncImage(
                        model = remember(story.thumbnail) {
                            coil.request.ImageRequest.Builder(ctx)
                                .data(story.thumbnail)
                                .crossfade(100)
                                .size(128) // Low quality for small thumbnail
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build()
                        },
                        contentDescription = story.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Icono de categoría si no hay thumbnail
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = story.category.icon,
                            contentDescription = null,
                            tint = story.category.gradient.first(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = story.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
