package com.rendly.app.ui.screens.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.*;
import androidx.camera.video.VideoCapture;
import androidx.camera.view.PreviewView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executor;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import coil.request.ImageRequest;
import com.google.accompanist.permissions.ExperimentalPermissionsApi;
import com.rendly.app.data.repository.RendRepository;
import com.rendly.app.ui.theme.*;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0084\u0001\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a@\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001a\u00ac\u0001\u0010\u000e\u001a\u00020\u00012\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\b2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\n2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\r2\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u001d\u0012\u0004\u0012\u00020\u00010\r2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u0010\u001f\u001a\u00020\u00142\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001a\u00bd\u0001\u0010!\u001a\u00020\u00012\b\u0010\"\u001a\u0004\u0018\u00010\u001d2\u0006\u0010#\u001a\u00020$2\u0012\u0010%\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u00010\r2\u0006\u0010&\u001a\u00020$2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u00010\r2\u0006\u0010(\u001a\u00020$2\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u00010\r2\u0006\u0010*\u001a\u00020$2\u0012\u0010+\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u00010\r2\u0006\u0010,\u001a\u00020\b2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052#\u0010.\u001a\u001f\u0012\u0015\u0012\u0013\u0018\u00010\u0003\u00a2\u0006\f\b/\u0012\b\b0\u0012\u0004\b\b(1\u0012\u0004\u0012\u00020\u00010\rH\u0003\u001aP\u00102\u001a\u00020\u00012\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u00180\n2\b\u00104\u001a\u0004\u0018\u00010\u001d2\u0012\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u001d\u0012\u0004\u0012\u00020\u00010\r2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a<\u00107\u001a\u00020\u00012\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00010\r2\u0006\u0010\u001f\u001a\u00020\u00142\b\b\u0002\u00108\u001a\u000209H\u0007\u001a\u0018\u0010:\u001a\u00020\u00012\u0006\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020$H\u0003\u001a&\u0010>\u001a\u00020\u00012\u0006\u0010?\u001a\u00020\u00182\u0006\u0010@\u001a\u00020\b2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0010\u0010A\u001a\u00020$2\u0006\u0010B\u001a\u00020CH\u0002\u001a\u0010\u0010D\u001a\u00020$2\u0006\u0010B\u001a\u00020CH\u0002\u001a\u001c\u0010E\u001a\b\u0012\u0004\u0012\u00020\u00180\n2\u0006\u0010\u000f\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010F\u001a\b\u0010G\u001a\u00020HH\u0003\u001a\u0012\u0010I\u001a\u00020J*\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010F\u00a8\u0006K"}, d2 = {"PostLinkItem", "", "post", "Lcom/rendly/app/data/model/Post;", "onClick", "Lkotlin/Function0;", "PostLinkModal", "isVisible", "", "posts", "", "onDismiss", "onPostSelected", "Lkotlin/Function1;", "RendCameraView", "context", "Landroid/content/Context;", "lifecycleOwner", "Landroidx/lifecycle/LifecycleOwner;", "lensFacing", "", "isRecording", "hasPermission", "galleryVideos", "Lcom/rendly/app/ui/screens/publish/GalleryVideo;", "onClose", "onFlipCamera", "onRecordingChange", "onVideoSelected", "Landroid/net/Uri;", "onShowGallery", "currentModeIndex", "onModeSelected", "RendDetailsView", "videoUri", "title", "", "onTitleChange", "description", "onDescriptionChange", "productTitle", "onProductTitleChange", "productPrice", "onProductPriceChange", "isPublishing", "onBack", "onPublish", "Lkotlin/ParameterName;", "name", "linkedPost", "RendGalleryView", "videos", "selectedUri", "onVideoSelect", "onNext", "RendScreen", "modifier", "Landroidx/compose/ui/Modifier;", "RendSectionHeader", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "text", "VideoThumbnailItem", "video", "isSelected", "formatRecordingTime", "millis", "", "formatVideoDuration", "loadGalleryVideos", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "rendFieldColors", "Landroidx/compose/material3/TextFieldColors;", "getCameraProviderRend", "Landroidx/camera/lifecycle/ProcessCameraProvider;", "app_debug"})
public final class RendScreenKt {
    
    @kotlin.OptIn(markerClass = {com.google.accompanist.permissions.ExperimentalPermissionsApi.class})
    @androidx.compose.runtime.Composable
    public static final void RendScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, int currentModeIndex, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @kotlin.OptIn(markerClass = {com.google.accompanist.permissions.ExperimentalPermissionsApi.class})
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    @androidx.compose.runtime.Composable
    private static final void RendCameraView(android.content.Context context, androidx.lifecycle.LifecycleOwner lifecycleOwner, int lensFacing, boolean isRecording, boolean hasPermission, java.util.List<com.rendly.app.ui.screens.publish.GalleryVideo> galleryVideos, kotlin.jvm.functions.Function0<kotlin.Unit> onClose, kotlin.jvm.functions.Function0<kotlin.Unit> onFlipCamera, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onRecordingChange, kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onVideoSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onShowGallery, int currentModeIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected) {
    }
    
    private static final java.lang.String formatRecordingTime(long millis) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendGalleryView(java.util.List<com.rendly.app.ui.screens.publish.GalleryVideo> videos, android.net.Uri selectedUri, kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onVideoSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onNext) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void VideoThumbnailItem(com.rendly.app.ui.screens.publish.GalleryVideo video, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendDetailsView(android.net.Uri videoUri, java.lang.String title, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTitleChange, java.lang.String description, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onDescriptionChange, java.lang.String productTitle, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onProductTitleChange, java.lang.String productPrice, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onProductPriceChange, boolean isPublishing, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPublish) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RendSectionHeader(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String text) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PostLinkModal(boolean isVisible, java.util.List<com.rendly.app.data.model.Post> posts, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Post, kotlin.Unit> onPostSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PostLinkItem(com.rendly.app.data.model.Post post, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final androidx.compose.material3.TextFieldColors rendFieldColors() {
        return null;
    }
    
    private static final java.lang.String formatVideoDuration(long millis) {
        return null;
    }
    
    private static final java.lang.Object getCameraProviderRend(android.content.Context $this$getCameraProviderRend, kotlin.coroutines.Continuation<? super androidx.camera.lifecycle.ProcessCameraProvider> $completion) {
        return null;
    }
    
    private static final java.lang.Object loadGalleryVideos(android.content.Context context, kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.ui.screens.publish.GalleryVideo>> $completion) {
        return null;
    }
}