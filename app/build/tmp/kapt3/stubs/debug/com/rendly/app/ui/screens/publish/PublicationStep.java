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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0003\u0004\u0005\u0006B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0004\u0007\b\t\n\u00a8\u0006\u000b"}, d2 = {"Lcom/rendly/app/ui/screens/publish/PublicationStep;", "", "()V", "Details", "Gallery", "Preview", "Publishing", "Lcom/rendly/app/ui/screens/publish/PublicationStep$Details;", "Lcom/rendly/app/ui/screens/publish/PublicationStep$Gallery;", "Lcom/rendly/app/ui/screens/publish/PublicationStep$Preview;", "Lcom/rendly/app/ui/screens/publish/PublicationStep$Publishing;", "app_debug"})
public abstract class PublicationStep {
    
    private PublicationStep() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/PublicationStep$Details;", "Lcom/rendly/app/ui/screens/publish/PublicationStep;", "()V", "app_debug"})
    public static final class Details extends com.rendly.app.ui.screens.publish.PublicationStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.PublicationStep.Details INSTANCE = null;
        
        private Details() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/PublicationStep$Gallery;", "Lcom/rendly/app/ui/screens/publish/PublicationStep;", "()V", "app_debug"})
    public static final class Gallery extends com.rendly.app.ui.screens.publish.PublicationStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.PublicationStep.Gallery INSTANCE = null;
        
        private Gallery() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/PublicationStep$Preview;", "Lcom/rendly/app/ui/screens/publish/PublicationStep;", "()V", "app_debug"})
    public static final class Preview extends com.rendly.app.ui.screens.publish.PublicationStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.PublicationStep.Preview INSTANCE = null;
        
        private Preview() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/PublicationStep$Publishing;", "Lcom/rendly/app/ui/screens/publish/PublicationStep;", "()V", "app_debug"})
    public static final class Publishing extends com.rendly.app.ui.screens.publish.PublicationStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.PublicationStep.Publishing INSTANCE = null;
        
        private Publishing() {
        }
    }
}