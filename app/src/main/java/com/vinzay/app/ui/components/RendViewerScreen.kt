package com.vinzay.app.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.vinzay.app.data.model.Rend
import com.vinzay.app.data.repository.RendRepository
import com.vinzay.app.ui.screens.rends.RendPageContent
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen Rend viewer opened from home feed video tap.
 * Vertical pager with all rends, fluid seeking, and playback controls.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RendViewerScreen(
    rends: List<Rend>,
    initialRendId: String?,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit = {},
    onLikeClick: (Rend) -> Unit = {},
    onSaveClick: (Rend) -> Unit = {},
    onShareClick: (Rend) -> Unit = {},
    onCommentClick: (Rend) -> Unit = {},
    onOpenProductPage: ((Rend) -> Unit)? = null,
    onOpenCommentChoice: ((Rend) -> Unit)? = null,
    onOpenForward: ((Rend) -> Unit)? = null,
    onLoadMore: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val initialPage = remember(initialRendId, rends) {
        if (initialRendId != null && rends.isNotEmpty()) {
            rends.indexOfFirst { it.id == initialRendId }.takeIf { it >= 0 } ?: 0
        } else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { rends.size.coerceAtLeast(1) }
    )

    // Scroll to initialPage when rends load
    LaunchedEffect(initialRendId, rends) {
        if (initialRendId != null && rends.isNotEmpty()) {
            val targetIndex = rends.indexOfFirst { it.id == initialRendId }
            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                pagerState.scrollToPage(targetIndex)
            }
        }
    }

    // Infinite scroll: load more when near last 3 pages
    LaunchedEffect(pagerState.currentPage, rends.size) {
        val totalPages = rends.size
        if (totalPages > 0 && pagerState.currentPage >= totalPages - 3 && onLoadMore != null) {
            onLoadMore?.invoke()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .graphicsLayer { clip = true; shape = RectangleShape }
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        if (rends.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().clipToBounds(),
                beyondViewportPageCount = 1,
                key = { if (it < rends.size) rends[it].id else "empty_$it" }
            ) { page ->
                if (page < rends.size) {
                    val isCurrentPage = pagerState.currentPage == page
                    RendPageContent(
                        rend = rends[page],
                        isPlaying = isCurrentPage,
                        onUserClick = { userId -> onProfileClick(userId) },
                        onOpenProductPage = { onOpenProductPage?.invoke(rends[page]) },
                        onOpenCommentChoice = { onOpenCommentChoice?.invoke(rends[page]) ?: onCommentClick(rends[page]) },
                        onOpenForward = { onOpenForward?.invoke(rends[page]) },
                        onOpenFullScreenFeed = { }
                    )
                }
            }
        }

        // Close button (top-left)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun RendViewerPage(
    rend: Rend,
    isPlaying: Boolean,
    onProfileClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isPaused by remember { mutableStateOf(false) }
    var showPauseIcon by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }
    var isVideoReady by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 1f
        }
    }

    // Prepare media
    DisposableEffect(rend.videoUrl) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    isVideoReady = true
                    totalDuration = exoPlayer.duration.coerceAtLeast(0)
                }
            }
        }
        exoPlayer.addListener(listener)
        val mediaItem = MediaItem.fromUri(rend.videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Play/pause based on visibility
    LaunchedEffect(isPlaying, isPaused) {
        if (isPlaying && !isPaused) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Track playback position for seek bar
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            if (!isSeeking) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0)
                totalDuration = exoPlayer.duration.coerceAtLeast(0)
            }
            delay(200)
        }
    }

    // Fade out pause icon
    LaunchedEffect(showPauseIcon) {
        if (showPauseIcon) {
            delay(600)
            showPauseIcon = false
        }
    }

    val avatarUrl = remember(rend.userAvatar) {
        if (rend.userAvatar.startsWith("http")) rend.userAvatar
        else "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${rend.userAvatar}"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPaused = !isPaused
                showPauseIcon = true
            }
    ) {
        // Video player - full screen
        if (isVideoReady) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        controllerAutoShow = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Thumbnail while loading
            val thumbUrl = rend.thumbnailUrl?.takeIf { it.isNotBlank() }
                ?: "${rend.videoUrl}/ik-thumbnail.jpg"
            AsyncImage(
                model = thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
            }
        }

        // Pause/Play icon overlay
        AnimatedVisibility(
            visible = showPauseIcon,
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        // Right side action buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .offset(y = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Like
            RendViewerActionButton(
                icon = if (rend.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = rend.likesCount,
                tint = if (rend.isLiked) AccentGreen else Color.White,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLikeClick()
                }
            )
            // Comment
            RendViewerActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = rend.reviewsCount,
                tint = Color.White,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCommentClick()
                }
            )
            // Save
            RendViewerActionButton(
                icon = if (rend.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                count = rend.savesCount,
                tint = if (rend.isSaved) AccentGold else Color.White,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaveClick()
                }
            )
            // Share
            RendViewerActionButton(
                icon = Icons.Outlined.Send,
                count = null,
                tint = Color.White,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShareClick()
                }
            )
        }

        // Bottom info + seek bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // User info
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(onClick = onProfileClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = remember(avatarUrl) {
                        ImageRequest.Builder(context)
                            .data(avatarUrl)
                            .crossfade(100)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .size(96)
                            .build()
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Column {
                    Text(
                        text = rend.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (rend.title.isNotBlank()) {
                        Text(
                            text = rend.title,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Product card mini (if linked)
            if (!rend.productTitle.isNullOrBlank() || (rend.productPrice != null && rend.productPrice > 0)) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(0.7f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!rend.productTitle.isNullOrBlank()) {
                            Text(
                                text = rend.productTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rend.productPrice != null && rend.productPrice > 0) {
                            Text(
                                text = "$${String.format("%.2f", rend.productPrice)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seek bar - fluid scrubbing
            if (totalDuration > 0) {
                val progress = if (isSeeking) seekPosition
                    else if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat()
                    else 0f

                Slider(
                    value = progress.coerceIn(0f, 1f),
                    onValueChange = { value ->
                        isSeeking = true
                        seekPosition = value
                    },
                    onValueChangeFinished = {
                        val newPos = (seekPosition * totalDuration).toLong()
                        exoPlayer.seekTo(newPos)
                        currentPosition = newPos
                        isSeeking = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
private fun RendViewerActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int?,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
        if (count != null && count > 0) {
            Text(
                text = if (count >= 1000) "${count / 1000}k" else "$count",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
