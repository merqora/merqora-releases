package com.rendly.app.ui.screens.publish;

import android.Manifest;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import coil.request.ImageRequest;
import com.google.accompanist.permissions.ExperimentalPermissionsApi;
import com.rendly.app.data.repository.PostRepository;
import com.rendly.app.ui.components.GalleryFilterState;
import com.rendly.app.ui.theme.*;
import kotlinx.coroutines.Dispatchers;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.ui.graphics.drawscope.Stroke;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0084\u0001\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0007\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a6\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u0010\u0007\u001a\u00020\bH\u0003\u001aV\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\b2\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u0013H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0014\u0010\u0015\u001a.\u0010\u0016\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00032\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u00ee\u0001\u0010\u0018\u001a\u00020\u00012\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001a0\u001c2\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\u0006\u0010\u001f\u001a\u00020\u00032\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00132\u0006\u0010!\u001a\u00020\b2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u008b\u0001\u0010#\u001a\u0086\u0001\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\u000f\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(\'\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b((\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b()\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(*\u0012\u0013\u0012\u00110\b\u00a2\u0006\f\b%\u0012\b\b&\u0012\u0004\b\b(+\u0012\u0004\u0012\u00020\u00010$H\u0003\u001a\u00c2\u0001\u0010,\u001a\u00020\u00012\f\u0010-\u001a\b\u0012\u0004\u0012\u00020.0\u001c2\b\u0010/\u001a\u0004\u0018\u00010\u001a2\b\u00100\u001a\u0004\u0018\u00010\u001e2\u0006\u00101\u001a\u0002022\f\u00103\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u00104\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u00010\u00132\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u00107\u001a\u00020\b2\f\u00108\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u00109\u001a\u00020:2\u0014\b\u0002\u0010;\u001a\u000e\u0012\u0004\u0012\u00020:\u0012\u0004\u0012\u00020\u00010\u00132\u001a\b\u0002\u0010<\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u001c\u0012\u0004\u0012\u00020\u00010\u0013H\u0003\u001aK\u0010=\u001a\u00020\u00012\u0006\u0010>\u001a\u00020.2\u0006\u0010?\u001a\u00020\b2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010@\u001a\u00020A2\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010B\u001a\u0004\u0018\u00010:H\u0003\u00a2\u0006\u0002\u0010C\u001aZ\u0010D\u001a\u00020\u00012\b\u00100\u001a\u0004\u0018\u00010\u001e2\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001a0\u001c2\b\b\u0002\u00101\u001a\u0002022\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0014\u00105\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u001e\u0012\u0004\u0012\u00020\u00010\u0013H\u0003\u001aN\u0010E\u001a\u00020\u00012\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0014\b\u0002\u0010;\u001a\u000e\u0012\u0004\u0012\u00020:\u0012\u0004\u0012\u00020\u00010\u00132\b\b\u0002\u00109\u001a\u00020:2\b\b\u0002\u0010@\u001a\u00020AH\u0007\u001a2\u0010G\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u00032\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00032\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0010\u0010H\u001a\u00020\u00012\u0006\u0010I\u001a\u00020JH\u0003\u001a\u0018\u0010K\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u0003H\u0003\u001a0\u0010L\u001a\u00020\u00012\u0006\u0010M\u001a\u00020\u00032\u0006\u0010?\u001a\u00020\b2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010N\u001a\u00020\bH\u0003\u001a<\u0010O\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00032\u0006\u0010P\u001a\u00020\b2\u0012\u0010\u0012\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00010\u0013H\u0003\u001a \u0010Q\u001a\u0004\u0018\u00010\u001e2\u0006\u0010R\u001a\u00020S2\u0006\u0010T\u001a\u00020\u001aH\u0082@\u00a2\u0006\u0002\u0010U\u001a\u001c\u0010V\u001a\b\u0012\u0004\u0012\u00020.0\u001c2\u0006\u0010R\u001a\u00020SH\u0082@\u00a2\u0006\u0002\u0010W\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006X"}, d2 = {"AlbumSelector", "", "albumName", "", "onAlbumClick", "Lkotlin/Function0;", "onMultiSelectClick", "isMultiSelectMode", "", "BenefitToggleRow", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "iconBgColor", "Landroidx/compose/ui/graphics/Color;", "iconTint", "title", "subtitle", "isEnabled", "onToggle", "Lkotlin/Function1;", "BenefitToggleRow-eopBjH0", "(Landroidx/compose/ui/graphics/vector/ImageVector;JJLjava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function1;)V", "EngagementOption", "onClick", "FinalPublishContent", "selectedUri", "Landroid/net/Uri;", "selectedUris", "", "editedBitmap", "Landroid/graphics/Bitmap;", "caption", "onCaptionChange", "isPublishing", "onBackClick", "onPublishClick", "Lkotlin/Function6;", "Lkotlin/ParameterName;", "name", "price", "condition", "category", "allowOffers", "freeShipping", "GallerySelectionContent", "galleryImages", "Lcom/rendly/app/ui/screens/publish/GalleryImage;", "selectedImageUri", "selectedBitmap", "aspectRatio", "Lcom/rendly/app/ui/screens/publish/PreviewAspectRatio;", "onAspectRatioToggle", "onImageSelected", "onNextClick", "onClose", "hasPermission", "onRequestPermission", "currentModeIndex", "", "onModeSelected", "onMultiImagesSelected", "GalleryThumbnail", "image", "isSelected", "modifier", "Landroidx/compose/ui/Modifier;", "selectionIndex", "(Lcom/rendly/app/ui/screens/publish/GalleryImage;ZLkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;ZLjava/lang/Integer;)V", "PreviewConfirmContent", "PublicationScreen", "onNavigateToHome", "PublishOption", "PublishingContent", "progress", "", "SectionHeader", "SelectableChip", "text", "multiSelect", "ToggleOption", "initialValue", "loadBitmapFromUri", "context", "Landroid/content/Context;", "uri", "(Landroid/content/Context;Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadGalleryImages", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class PublicationScreenKt {
    
    @kotlin.OptIn(markerClass = {com.google.accompanist.permissions.ExperimentalPermissionsApi.class})
    @androidx.compose.runtime.Composable
    public static final void PublicationScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToHome, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, int currentModeIndex, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void GallerySelectionContent(java.util.List<com.rendly.app.ui.screens.publish.GalleryImage> galleryImages, android.net.Uri selectedImageUri, android.graphics.Bitmap selectedBitmap, com.rendly.app.ui.screens.publish.PreviewAspectRatio aspectRatio, kotlin.jvm.functions.Function0<kotlin.Unit> onAspectRatioToggle, kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onImageSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onNextClick, kotlin.jvm.functions.Function0<kotlin.Unit> onClose, boolean hasPermission, kotlin.jvm.functions.Function0<kotlin.Unit> onRequestPermission, int currentModeIndex, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onModeSelected, kotlin.jvm.functions.Function1<? super java.util.List<? extends android.net.Uri>, kotlin.Unit> onMultiImagesSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void GalleryThumbnail(com.rendly.app.ui.screens.publish.GalleryImage image, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier, boolean isMultiSelectMode, java.lang.Integer selectionIndex) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void AlbumSelector(java.lang.String albumName, kotlin.jvm.functions.Function0<kotlin.Unit> onAlbumClick, kotlin.jvm.functions.Function0<kotlin.Unit> onMultiSelectClick, boolean isMultiSelectMode) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void PreviewConfirmContent(android.graphics.Bitmap selectedBitmap, android.net.Uri selectedUri, java.util.List<? extends android.net.Uri> selectedUris, com.rendly.app.ui.screens.publish.PreviewAspectRatio aspectRatio, kotlin.jvm.functions.Function0<kotlin.Unit> onBackClick, kotlin.jvm.functions.Function1<? super android.graphics.Bitmap, kotlin.Unit> onNextClick) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable
    private static final void FinalPublishContent(android.net.Uri selectedUri, java.util.List<? extends android.net.Uri> selectedUris, android.graphics.Bitmap editedBitmap, java.lang.String caption, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onCaptionChange, boolean isPublishing, kotlin.jvm.functions.Function0<kotlin.Unit> onBackClick, kotlin.jvm.functions.Function6<? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.String, ? super java.lang.Boolean, ? super java.lang.Boolean, kotlin.Unit> onPublishClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void EngagementOption(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PublishOption(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ToggleOption(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle, boolean initialValue, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onToggle) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PublishingContent(float progress) {
    }
    
    private static final java.lang.Object loadGalleryImages(android.content.Context context, kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.ui.screens.publish.GalleryImage>> $completion) {
        return null;
    }
    
    private static final java.lang.Object loadBitmapFromUri(android.content.Context context, android.net.Uri uri, kotlin.coroutines.Continuation<? super android.graphics.Bitmap> $completion) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void SectionHeader(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SelectableChip(java.lang.String text, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, boolean multiSelect) {
    }
}