package com.rendly.app.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.rendly.app.data.model.Rend
import com.rendly.app.data.repository.RendRepository
import com.rendly.app.ui.theme.*
import com.rendly.app.ui.components.VideoThumbnail

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun RendsCarousel(
    rends: List<Rend> = emptyList(), // OPTIMIZADO: Recibir como parámetro
    isLoading: Boolean = false, // OPTIMIZADO: Recibir como parámetro
    onRendClick: (Rend) -> Unit = {},
    onViewAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // OPTIMIZADO: Memoizar lista limitada para evitar take() en cada frame
    val limitedRends = remember(rends) { rends.take(10) }
    
    // Autoplay: track visible items in the LazyRow
    val lazyListState = rememberLazyListState()
    
    // Detect if this carousel section is visible on screen
    var isSectionVisible by remember { mutableStateOf(false) }
    
    // Derive which rend indices are visible (max 2 for autoplay)
    val visibleRendIndices by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            layoutInfo.visibleItemsInfo
                .map { it.index }
                .take(2)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val parentBounds = coordinates.parentLayoutCoordinates?.size
                val yInParent = try {
                    coordinates.localToRoot(androidx.compose.ui.geometry.Offset.Zero).y
                } catch (_: Exception) { 0f }
                val height = coordinates.size.height.toFloat()
                val screenHeight = parentBounds?.height?.toFloat() ?: 2000f
                // Visible if any part is on screen
                isSectionVisible = yInParent + height > 0f && yInParent < screenHeight
            }
    ) {
        // Línea separadora superior
        Divider(
            color = Surface,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icono de Rends - OPTIMIZADO: Gradiente estático fuera del modifier
                val rendsIconGradient = remember {
                    Brush.linearGradient(colors = listOf(AccentPink, PrimaryPurple))
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(rendsIconGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                Text(
                    text = "Rends",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            TextButton(onClick = onViewAll) {
                Text(
                    text = "Ver todos",
                    fontSize = 13.sp,
                    color = Color(0xFF3A8FD4),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF3A8FD4),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        if (isLoading && rends.isEmpty()) {
            // Skeleton loading
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4) {
                    RendCardSkeleton()
                }
            }
        } else if (rends.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VideoLibrary,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay Rends disponibles",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            // Lista de Rends con autoplay
            LazyRow(
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = limitedRends.size,
                    key = { limitedRends[it].id },
                    contentType = { "rend_card" }
                ) { index ->
                    val rend = limitedRends[index]
                    val shouldAutoplay = isSectionVisible && index in visibleRendIndices
                    
                    AutoplayRendCard(
                        rend = rend,
                        isAutoplayEnabled = shouldAutoplay,
                        onClick = { onRendClick(rend) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Línea separadora inferior
        Divider(
            color = Surface,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun AutoplayRendCard(
    rend: Rend,
    isAutoplayEnabled: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // ExoPlayer - created lazily, only when autoplay is first enabled
    var playerCreated by remember { mutableStateOf(false) }
    val exoPlayer = remember {
        lazy {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f // Muted autoplay
                val mediaItem = MediaItem.fromUri(Uri.parse(rend.videoUrl))
                setMediaItem(mediaItem)
                prepare()
            }
        }
    }
    
    // Create player only when autoplay is first enabled
    LaunchedEffect(isAutoplayEnabled) {
        if (isAutoplayEnabled && !playerCreated) {
            playerCreated = true
            exoPlayer.value // Force lazy init
        }
    }
    
    // Control play/pause
    LaunchedEffect(isAutoplayEnabled, playerCreated) {
        if (playerCreated) {
            if (isAutoplayEnabled) {
                exoPlayer.value.play()
            } else {
                exoPlayer.value.pause()
            }
        }
    }
    
    // Lifecycle observer
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (playerCreated) {
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> exoPlayer.value.pause()
                    Lifecycle.Event.ON_RESUME -> if (isAutoplayEnabled) exoPlayer.value.play()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (playerCreated) {
                exoPlayer.value.release()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .clickable(onClick = onClick)
    ) {
        // Video layer or thumbnail
        if (isAutoplayEnabled && playerCreated) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer.value
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        clipToOutline = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            )
        } else {
            // Static thumbnail
            VideoThumbnail(
                videoUrl = rend.videoUrl,
                thumbnailUrl = rend.thumbnailUrl,
                contentDescription = rend.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Gradiente oscuro inferior
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        
        // Play icon - only show when NOT autoplaying
        if (!isAutoplayEnabled || !playerCreated) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Avatar y username en esquina SUPERIOR izquierda
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val avatarUrl = remember(rend.userId) {
                rend.userAvatar.ifEmpty { "https://ui-avatars.com/api/?name=${rend.username}&background=A78BFA&color=fff" }
            }
            AsyncImage(
                model = remember(avatarUrl) {
                    ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .crossfade(100)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(80)
                        .build()
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
            )
            
            Text(
                text = rend.username,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Stats en la parte inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Likes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatCount(rend.likesCount),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Guardados
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bookmark,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatCount(rend.reviewsCount),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Reenviados
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formatCount(rend.sharesCount),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RendCardSkeleton() {
    Box(
        modifier = Modifier
            .width(180.dp)
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(shimmerBrush())
    )
}

@Composable
private fun shimmerBrush(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            SurfaceElevated,
            Surface,
            SurfaceElevated
        )
    )
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
