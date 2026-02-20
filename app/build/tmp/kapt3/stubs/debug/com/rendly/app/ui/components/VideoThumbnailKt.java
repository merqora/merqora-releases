package com.rendly.app.ui.components;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import androidx.compose.runtime.*;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import coil.request.CachePolicy;
import coil.request.ImageRequest;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a8\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u0007\u001a\u0018\u0010\n\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0002\u001a\u00020\u0003H\u0082@\u00a2\u0006\u0002\u0010\f\u001a\u0012\u0010\r\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0002\u001a\u00020\u0003H\u0002\u00a8\u0006\u000e"}, d2 = {"VideoThumbnail", "", "videoUrl", "", "thumbnailUrl", "contentDescription", "modifier", "Landroidx/compose/ui/Modifier;", "contentScale", "Landroidx/compose/ui/layout/ContentScale;", "extractVideoFrame", "Landroid/graphics/Bitmap;", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCloudinaryVideoThumbnail", "app_debug"})
public final class VideoThumbnailKt {
    
    /**
     * Composable that displays a video thumbnail.
     * First tries to use thumbnailUrl if available.
     * If not, extracts a frame from the video URL.
     */
    @androidx.compose.runtime.Composable
    public static final void VideoThumbnail(@org.jetbrains.annotations.NotNull
    java.lang.String videoUrl, @org.jetbrains.annotations.Nullable
    java.lang.String thumbnailUrl, @org.jetbrains.annotations.Nullable
    java.lang.String contentDescription, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.layout.ContentScale contentScale) {
    }
    
    /**
     * Generate a thumbnail URL from a Cloudinary video URL.
     * Replaces the video extension with .jpg and adds thumbnail transformation.
     * Returns null if the URL is not a Cloudinary video URL.
     */
    private static final java.lang.String getCloudinaryVideoThumbnail(java.lang.String videoUrl) {
        return null;
    }
    
    /**
     * Extract a frame from video URL using MediaMetadataRetriever
     */
    private static final java.lang.Object extractVideoFrame(java.lang.String videoUrl, kotlin.coroutines.Continuation<? super android.graphics.Bitmap> $completion) {
        return null;
    }
}