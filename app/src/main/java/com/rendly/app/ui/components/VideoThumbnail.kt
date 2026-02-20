package com.rendly.app.ui.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.rendly.app.ui.theme.SurfaceElevated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Composable that displays a video thumbnail.
 * First tries to use thumbnailUrl if available.
 * If not, extracts a frame from the video URL.
 */
@Composable
fun VideoThumbnail(
    videoUrl: String,
    thumbnailUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    
    // If thumbnailUrl is available and not empty, use it directly
    val effectiveThumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() }
    
    if (effectiveThumbnailUrl != null) {
        // Use provided thumbnail
        AsyncImage(
            model = remember(effectiveThumbnailUrl) {
                ImageRequest.Builder(context)
                    .data(effectiveThumbnailUrl)
                    .crossfade(150)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            },
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        // Try Cloudinary thumbnail first, then extract frame as fallback
        val cloudinaryThumbUrl = remember(videoUrl) { getCloudinaryVideoThumbnail(videoUrl) }
        
        if (cloudinaryThumbUrl != null) {
            // Use Cloudinary-generated thumbnail from video
            AsyncImage(
                model = remember(cloudinaryThumbUrl) {
                    ImageRequest.Builder(context)
                        .data(cloudinaryThumbUrl)
                        .crossfade(150)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                },
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            // Non-Cloudinary: extract frame from video
            var bitmap by remember(videoUrl) { mutableStateOf<Bitmap?>(null) }
            var isLoading by remember(videoUrl) { mutableStateOf(true) }
            
            LaunchedEffect(videoUrl) {
                isLoading = true
                bitmap = extractVideoFrame(videoUrl)
                isLoading = false
            }
            
            Box(modifier = modifier.background(SurfaceElevated)) {
                when {
                    bitmap != null -> {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = contentDescription,
                            contentScale = contentScale,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(shimmerBrush())
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceElevated)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Generate a thumbnail URL from a Cloudinary video URL.
 * Replaces the video extension with .jpg and adds thumbnail transformation.
 * Returns null if the URL is not a Cloudinary video URL.
 */
private fun getCloudinaryVideoThumbnail(videoUrl: String): String? {
    if (!videoUrl.contains("cloudinary.com")) return null
    
    // Replace video extension with .jpg for thumbnail
    val thumbUrl = videoUrl
        .replace(".mp4", ".jpg")
        .replace(".mov", ".jpg")
        .replace(".webm", ".jpg")
        .replace(".avi", ".jpg")
    
    // Add transformation for quality and size
    return if (thumbUrl.contains("/upload/")) {
        thumbUrl.replace("/upload/", "/upload/w_400,h_600,c_fill,q_auto,f_auto,so_1/")
    } else {
        thumbUrl
    }
}

/**
 * Extract a frame from video URL using MediaMetadataRetriever
 */
private suspend fun extractVideoFrame(videoUrl: String): Bitmap? = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(videoUrl, HashMap())
        // Get frame at 1 second (1,000,000 microseconds)
        retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (e: Exception) {
        android.util.Log.e("VideoThumbnail", "Failed to extract frame: ${e.message}")
        null
    } finally {
        try {
            retriever.release()
        } catch (e: Exception) {
            // Ignore release errors
        }
    }
}
