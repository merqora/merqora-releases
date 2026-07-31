package com.mercora.app.ui.screens.live

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mercora.app.data.repository.LiveStream
import com.mercora.app.ui.theme.*

/**
 * Pantalla que muestra todas las transmisiones en vivo activas
 * Con botón de refresh para actualizar manualmente
 */
@Composable
fun LiveStreamsListScreen(
    onBack: () -> Unit,
    onStreamClick: (LiveStream) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveStreamsViewModel = hiltViewModel()
) {
    val activeStreams by viewModel.activeStreams.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showErrorDialog by viewModel.showErrorDialog.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Dialog de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error cargando transmisiones") },
            text = { 
                Text(
                    text = errorMessage,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Animación de pulso para indicador LIVE
    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
            .statusBarsPadding()
    ) {
        // Header minimalista
        LiveStreamsHeader(onBack = onBack, onRefresh = { viewModel.pullToRefresh() })
        
        // Contenido
        if (activeStreams.isEmpty() && !isRefreshing) {
            EmptyStreamsContent()
        } else if (isRefreshing) {
            // Mostrar loading mientras refresca
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFEF4444),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
            }
        } else {
            // Grid de transmisiones
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activeStreams, key = { it.id }) { stream ->
                    LiveStreamItem(
                        stream = stream,
                        pulseAlpha = pulseAlpha,
                        onClick = { onStreamClick(stream) }
                    )
                }
            }
        }
    }
}

/**
 * Header de la pantalla con botón de refresh
 */
@Composable
private fun LiveStreamsHeader(onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Surface)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "En Vivo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Badge LIVE
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFEF4444)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Text(
                text = "Desliza hacia abajo para actualizar",
                fontSize = 13.sp,
                color = TextMuted
            )
        }
        
        // Botón de refresh
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Surface)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar",
                tint = TextPrimary
            )
        }
    }
}

/**
 * Item de stream en la grilla
 */
@Composable
private fun LiveStreamItem(
    stream: LiveStream,
    pulseAlpha: Float,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        // Avatar circular con borde animado
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Borde exterior animado (glow effect)
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer { alpha = pulseAlpha * 0.5f }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEF4444).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Borde gradient LIVE
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFEF4444),
                                Color(0xFF2E8B57),
                                Color(0xFFEF4444)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(3.dp)
            ) {
                // Avatar
                if (stream.broadcasterAvatar != null) {
                    AsyncImage(
                        model = stream.broadcasterAvatar,
                        contentDescription = stream.broadcasterUsername,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, AccentPink)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stream.broadcasterUsername.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Badges: LIVE + Viewers (Alineados a la izquierda)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Badge LIVE pequeño
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFEF4444)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .graphicsLayer { alpha = pulseAlpha }
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Text(
                            text = "LIVE",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                // Badge de Viewers
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(8.dp)
                        )
                        Text(
                            text = "${stream.viewerCount}",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Username
        Text(
            text = "@${stream.broadcasterUsername}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Nombre de tienda
        if (stream.broadcasterStoreName != null) {
            Text(
                text = stream.broadcasterStoreName,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Contenido cuando no hay streams
 */
@Composable
private fun EmptyStreamsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "No hay transmisiones en vivo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sé el primero en iniciar una transmisión\ny conecta con tu audiencia",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
