package com.rendly.app.ui.screens.publish;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import coil.request.ImageRequest;
import com.google.accompanist.permissions.ExperimentalPermissionsApi;
import com.rendly.app.service.StoryUploadService;
import com.rendly.app.ui.theme.*;
import kotlinx.coroutines.Dispatchers;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import com.rendly.app.gpu.TransformBridge;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000|\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a2\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0003\u001aR\u0010\u000f\u001a\u00020\u00042\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00040\u00132\u0006\u0010\u0014\u001a\u00020\n2\u0014\b\u0002\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00040\u00132\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0007\u001a\u00ce\u0001\u0010\u0017\u001a\u00020\u00042\b\u0010\u0018\u001a\u0004\u0018\u00010\u00192\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\b\b\u0002\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010 \u001a\u00020\u001f2\b\b\u0002\u0010!\u001a\u00020\u001f2\b\b\u0002\u0010\"\u001a\u00020\u001f2\b\b\u0002\u0010#\u001a\u00020\n2\b\b\u0002\u0010$\u001a\u00020\n2\u000e\b\u0002\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u000e\b\u0002\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u000e\b\u0002\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u000e\b\u0002\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u000e\b\u0002\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u000e\b\u0002\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0003\u001a \u0010+\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010,\u001a\u00020-H\u0082@\u00a2\u0006\u0002\u0010.\u001aD\u0010/\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\f2\u0012\u00100\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00040\u00132\u0016\u00101\u001a\u0012\u0012\b\u0012\u000602j\u0002`3\u0012\u0004\u0012\u00020\u00040\u0013H\u0003\u001a\u0012\u00104\u001a\u000205*\u00020\u0006H\u0082@\u00a2\u0006\u0002\u00106\"\u0014\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"STORY_EDIT_TOOLS", "", "Lcom/rendly/app/ui/screens/publish/EditTool;", "CameraPreview", "", "context", "Landroid/content/Context;", "lifecycleOwner", "Landroidx/lifecycle/LifecycleOwner;", "lensFacing", "", "imageCapture", "Landroidx/camera/core/ImageCapture;", "modifier", "Landroidx/compose/ui/Modifier;", "HistoryScreen", "onClose", "Lkotlin/Function0;", "onModeSelected", "Lkotlin/Function1;", "currentModeIndex", "onEditingStateChange", "", "StoryEditToolsCarousel", "bitmap", "Landroid/graphics/Bitmap;", "selectedFilter", "Lcom/rendly/app/ui/components/ImageFilter;", "textState", "Lcom/rendly/app/ui/components/StoryTextState;", "textOffsetX", "", "textOffsetY", "textRotation", "textScale", "previewWidth", "previewHeight", "onSaveSuccess", "onFilterClick", "onTextClick", "onGifClick", "onDrawClick", "onAdjustClick", "loadBitmapFromUri", "uri", "Landroid/net/Uri;", "(Landroid/content/Context;Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "takePhotoToMemoryFast", "onSuccess", "onError", "Ljava/lang/Exception;", "Lkotlin/Exception;", "getCameraProvider", "Landroidx/camera/lifecycle/ProcessCameraProvider;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class HistoryScreenKt {
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.ui.screens.publish.EditTool> STORY_EDIT_TOOLS = null;
    
    @kotlin.OptIn(markerClass = {com.google.accompanist.permissions.ExperimentalPermissionsApi.class, androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    public static final void HistoryScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, int currentModeIndex, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onEditingStateChange, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void StoryEditToolsCarousel(android.graphics.Bitmap bitmap, com.rendly.app.ui.components.ImageFilter selectedFilter, com.rendly.app.ui.components.StoryTextState textState, float textOffsetX, float textOffsetY, float textRotation, float textScale, int previewWidth, int previewHeight, kotlin.jvm.functions.Function0<kotlin.Unit> onSaveSuccess, kotlin.jvm.functions.Function0<kotlin.Unit> onFilterClick, kotlin.jvm.functions.Function0<kotlin.Unit> onTextClick, kotlin.jvm.functions.Function0<kotlin.Unit> onGifClick, kotlin.jvm.functions.Function0<kotlin.Unit> onDrawClick, kotlin.jvm.functions.Function0<kotlin.Unit> onAdjustClick, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CameraPreview(android.content.Context context, androidx.lifecycle.LifecycleOwner lifecycleOwner, int lensFacing, androidx.camera.core.ImageCapture imageCapture, androidx.compose.ui.Modifier modifier) {
    }
    
    private static final java.lang.Object getCameraProvider(android.content.Context $this$getCameraProvider, kotlin.coroutines.Continuation<? super androidx.camera.lifecycle.ProcessCameraProvider> $completion) {
        return null;
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.camera.core.ExperimentalGetImage.class})
    private static final void takePhotoToMemoryFast(android.content.Context context, androidx.camera.core.ImageCapture imageCapture, kotlin.jvm.functions.Function1<? super android.graphics.Bitmap, kotlin.Unit> onSuccess, kotlin.jvm.functions.Function1<? super java.lang.Exception, kotlin.Unit> onError) {
    }
    
    private static final java.lang.Object loadBitmapFromUri(android.content.Context context, android.net.Uri uri, kotlin.coroutines.Continuation<? super android.graphics.Bitmap> $completion) {
        return null;
    }
}