package com.rendly.app.ui.components

import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*

@Composable
fun MyStoryBanner(
    userAvatar: String? = null,
    username: String = "Tu Historia",
    storiesCount: Int = 0,
    viewsCount: Int = 0,
    followersCount: Int = 0,
    likesCount: Int = 0,
    sharesCount: Int = 0,
    isUploading: Boolean = false,
    isLoading: Boolean = false,
    isUploadingPost: Boolean = false,
    onPress: () -> Unit = {},
    onAddPress: () -> Unit = {},
    onViewsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasStories = storiesCount > 0
    val showLoadingAnimation = isLoading
    
    // Cargar avatar automáticamente usando StateFlow del ProfileRepository si no se proporciona
    val currentProfile by com.rendly.app.data.repository.ProfileRepository.currentProfile.collectAsState()
    
    // Cargar perfil al inicio si no está cargado
    LaunchedEffect(Unit) {
        if (currentProfile == null && userAvatar == null) {
            try {
                com.rendly.app.data.repository.ProfileRepository.loadCurrentProfile()
            } catch (e: Exception) { /* Silently fail */ }
        }
    }
    
    // Usar avatar del parámetro o del perfil cargado
    val effectiveAvatar = userAvatar ?: currentProfile?.avatarUrl
    
    // Estado para efecto lámpara de lava - SOLO al pulsar
    var isLavaActive by remember { mutableStateOf(false) }
    
    // Animación de pulso al pulsar
    var isPulsing by remember { mutableStateOf(false) }
    val pulseScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = 0.35f,
            stiffness = 350f
        ),
        label = "pulseScale"
    )
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isPulsing) 1f else 0f,
        animationSpec = tween(200),
        label = "pulseAlpha"
    )
    
    // OPTIMIZADO: Solo crear animación infinita cuando showLoadingAnimation o isLavaActive están activos
    // Esto evita ~60 recomposiciones/segundo cuando no se necesita la animación
    val needsAnimation = showLoadingAnimation || isLavaActive
    
    // Rotación continua solo cuando se necesita - usando LaunchedEffect en lugar de infiniteTransition
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(needsAnimation) {
        if (needsAnimation) {
            while (true) {
                rotationDegrees = (rotationDegrees + 2f) % 360f
                kotlinx.coroutines.delay(16) // ~60fps solo cuando activo
            }
        } else {
            rotationDegrees = 0f
        }
    }
    
    val lavaColors = listOf(
        Color(0xFFFF6B35), // Naranja acento
        Color(0xFF2E8B57), // Verde río
        Color(0xFF0A3D62), // Azul marino
        Color(0xFFFF6B35)  // Cierra con naranja
    )
    
    val scope = rememberCoroutineScope()
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                if (hasStories) {
                    // Activar efecto lámpara de lava y pulso antes de abrir
                    scope.launch {
                        isLavaActive = true
                        isPulsing = true
                        kotlinx.coroutines.delay(400)
                        isPulsing = false
                        kotlinx.coroutines.delay(200)
                        isLavaActive = false
                        onPress()
                    }
                }
            },
        shape = RoundedCornerShape(20.dp),
        color = Surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (hasStories) {
                            listOf(
                                Color(0xFF0C1E2E),
                                Color(0xFF0A1924),
                                Color(0xFF081520)
                            )
                        } else {
                            listOf(
                                Color(0xFF0B1A28),
                                Color(0xFF091420)
                            )
                        }
                    )
                )
        ) {
            // Fondo profesional con detalles decorativos de la paleta
            Canvas(
                modifier = Modifier.matchParentSize()
            ) {
                val gridSize = 36.dp.toPx()
                
                // Líneas diagonales sutiles en azul
                val lineColorBlue = Color(0xFF0A3D62).copy(alpha = 0.06f)
                for (i in -10..20) {
                    drawLine(
                        color = lineColorBlue,
                        start = Offset(i * gridSize, 0f),
                        end = Offset(i * gridSize + size.height, size.height),
                        strokeWidth = 1f
                    )
                }
                
                // Líneas cruzadas sutiles en naranja (menor frecuencia)
                val lineColorOrange = Color(0xFFFF6B35).copy(alpha = 0.03f)
                for (i in -5..15) {
                    drawLine(
                        color = lineColorOrange,
                        start = Offset(i * gridSize * 2, size.height),
                        end = Offset(i * gridSize * 2 + size.height, 0f),
                        strokeWidth = 0.8f
                    )
                }
                
                // Brillo azul sutil en la esquina superior derecha
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A3D62).copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.2f),
                        radius = size.width * 0.4f
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.2f),
                    radius = size.width * 0.4f
                )
                
                // Acento naranja sutil en la esquina inferior izquierda
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.1f, size.height * 0.8f),
                        radius = size.width * 0.3f
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.8f),
                    radius = size.width * 0.3f
                )
                
                // Acento verde sutil en el centro-abajo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2E8B57).copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.9f),
                        radius = size.width * 0.25f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.9f),
                    radius = size.width * 0.25f
                )
            }
            
            // Borde superior con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF0A3D62).copy(alpha = 0.25f),
                                Color(0xFFFF6B35).copy(alpha = 0.15f),
                                Color(0xFF2E8B57).copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Contenido principal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con efecto lámpara de lava SOLO al pulsar
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Efecto LÁMPARA DE LAVA - SOLO cuando isLavaActive es true
                    if (isLavaActive) {
                        Canvas(
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center)
                                .rotate(rotationDegrees)
                        ) {
                            val strokeWidth = 4.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = lavaColors,
                                    center = Offset(size.width / 2, size.height / 2)
                                ),
                                radius = radius,
                                center = Offset(size.width / 2, size.height / 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    
                    // Efecto de PULSO al presionar - halo que se expande
                    if (isPulsing) {
                        Canvas(
                            modifier = Modifier
                                .size(76.dp * pulseScale)
                                .align(Alignment.Center)
                        ) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B35).copy(alpha = 0.6f),
                                        Color(0xFF0A3D62).copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2, size.height / 2)
                                ),
                                radius = size.minDimension / 2,
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        }
                    }
                    
                    // Animación de carga (spinner)
                    if (showLoadingAnimation) {
                        Canvas(
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center)
                                .rotate(rotationDegrees)
                        ) {
                            val strokeWidth = 3.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        PrimaryPurple,
                                        AccentPink,
                                        PrimaryPurple.copy(alpha = 0.2f),
                                        AccentPink,
                                        PrimaryPurple
                                    ),
                                    center = Offset(size.width / 2, size.height / 2)
                                ),
                                radius = radius,
                                center = Offset(size.width / 2, size.height / 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    
                    // Borde estático cuando hay stories (sin animación)
                    if (hasStories && !isLavaActive && !showLoadingAnimation) {
                        Canvas(
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center)
                        ) {
                            val strokeWidth = 3.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = lavaColors,
                                    center = Offset(size.width / 2, size.height / 2)
                                ),
                                radius = radius,
                                center = Offset(size.width / 2, size.height / 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                    
                    // Avatar - perfectamente centrado
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Estado para manejar error de carga
                        var imageLoadFailed by remember { mutableStateOf(false) }
                        
                        // Mostrar avatar real si existe y no falló la carga
                        if (!effectiveAvatar.isNullOrBlank() && !imageLoadFailed) {
                            AsyncImage(
                                model = effectiveAvatar,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                onError = { imageLoadFailed = true },
                                onSuccess = { imageLoadFailed = false },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            // Fallback: Círculo con iniciales del usuario
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(PrimaryPurple, AccentGreen)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = username.take(2).uppercase(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                // Info - Panel de estadísticas profesional VERTICAL
                Column(modifier = Modifier.weight(1f)) {
                    if (hasStories) {
                        // Estadísticas en formato vertical: Vistas, Likes, Reenviados
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StoryStatItem(
                                icon = Icons.Outlined.Visibility,
                                value = viewsCount,
                                label = "Vistas",
                                onClick = onViewsClick
                            )
                            StoryStatItem(
                                icon = Icons.Outlined.Favorite,
                                value = likesCount,
                                label = "Likes"
                            )
                            StoryStatItem(
                                icon = Icons.Outlined.Reply,
                                value = sharesCount,
                                label = "Reenviados"
                            )
                        }
                    } else {
                        Text(
                            text = "Crear Historia",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Comparte tu momento",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
                
                // Botón de acción
                if (hasStories) {
                    Surface(
                        modifier = Modifier.clickable { onPress() },
                        shape = RoundedCornerShape(12.dp),
                        color = IconAccentBlue.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            IconAccentBlue.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "Ver",
                                tint = IconAccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ver",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IconAccentBlue
                            )
                        }
                    }
                } else {
                    // Botón de crear historia - Diseño premium con gradiente azul-esmeralda
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glow effect sutil
                        Canvas(modifier = Modifier.size(50.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF2E8B57).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 1.6f
                            )
                        }
                        
                        // Botón principal con gradiente premium
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(8.dp, CircleShape, clip = false, ambientColor = Color(0xFF2E8B57).copy(alpha = 0.4f))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0A3D62), // Azul marino profundo
                                            Color(0xFF1E6F5C), // Verde esmeralda oscuro
                                            Color(0xFF2E8B57)  // Verde mar
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(100f, 100f)
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.3f),
                                            Color(0xFF2E8B57).copy(alpha = 0.5f),
                                            Color.White.copy(alpha = 0.15f),
                                            Color(0xFF0A3D62).copy(alpha = 0.4f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { onAddPress() },
                            contentAlignment = Alignment.Center
                        ) {
                            // Icono de cámara elegante
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = "Crear Historia",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Componente para mostrar estadísticas individuales de stories
@Composable
private fun StoryStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatStatNumber(value),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}

// Formatear números grandes (1000 -> 1K, 1000000 -> 1M)
private fun formatStatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}
