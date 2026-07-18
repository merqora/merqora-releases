package com.mercora.app.ui.components.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.mercora.app.ui.components.ExoPlayerPool
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProductImageGalleryV2(
    images: List<String>,
    title: String,
    selectedIndex: Int = 0,
    onPageChange: (Int) -> Unit = {},
    onShowFullscreen: () -> Unit = {}
) {
    val displayImages = images.ifEmpty { listOf("") }
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { displayImages.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex) {
        if (pagerState.currentPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A24))
            .clickable { onShowFullscreen() }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val mediaUrl = displayImages[page]
            if (isVideoUrl(mediaUrl)) {
                ProductVideoPlayer(
                    videoUrl = mediaUrl,
                    isCurrentPage = pagerState.currentPage == page
                )
            } else {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (displayImages.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isVideoUrl(displayImages[pagerState.currentPage])) {
                        Icon(
                            Icons.Filled.PlayCircle,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "${pagerState.currentPage + 1}/${displayImages.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProductVideoPlayer(
    videoUrl: String,
    isCurrentPage: Boolean
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showPlayButton by remember { mutableStateOf(true) }
    var isVideoReady by remember { mutableStateOf(false) }

    val exoPlayer = remember(videoUrl) {
        ExoPlayerPool.acquire(context).apply {
            val mediaItem = androidx.media3.common.MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            volume = 1f
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onRenderedFirstFrame() {
                    isVideoReady = true
                }
            })
            prepare()
        }
    }

    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage) {
            exoPlayer.pause()
            isPlaying = false
            showPlayButton = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.pause()
            ExoPlayerPool.release(exoPlayer)
        }
    }

    val thumbnailUrl = remember(videoUrl) { videoUrlToThumbnail(videoUrl) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                if (isPlaying) {
                    exoPlayer.pause()
                    isPlaying = false
                    showPlayButton = true
                } else {
                    exoPlayer.play()
                    isPlaying = true
                    showPlayButton = false
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                androidx.media3.ui.PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    controllerAutoShow = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShowBuffering(androidx.media3.ui.PlayerView.SHOW_BUFFERING_NEVER)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isVideoReady || !isPlaying) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        AnimatedVisibility(
            visible = showPlayButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 12.dp, start = 12.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Filled.Videocam,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Video",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
