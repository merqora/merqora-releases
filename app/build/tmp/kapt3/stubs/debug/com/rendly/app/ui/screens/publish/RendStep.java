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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0003\u0004\u0005\u0006B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0004\u0007\b\t\n\u00a8\u0006\u000b"}, d2 = {"Lcom/rendly/app/ui/screens/publish/RendStep;", "", "()V", "Camera", "Details", "Edit", "Gallery", "Lcom/rendly/app/ui/screens/publish/RendStep$Camera;", "Lcom/rendly/app/ui/screens/publish/RendStep$Details;", "Lcom/rendly/app/ui/screens/publish/RendStep$Edit;", "Lcom/rendly/app/ui/screens/publish/RendStep$Gallery;", "app_debug"})
public abstract class RendStep {
    
    private RendStep() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/RendStep$Camera;", "Lcom/rendly/app/ui/screens/publish/RendStep;", "()V", "app_debug"})
    public static final class Camera extends com.rendly.app.ui.screens.publish.RendStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.RendStep.Camera INSTANCE = null;
        
        private Camera() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/RendStep$Details;", "Lcom/rendly/app/ui/screens/publish/RendStep;", "()V", "app_debug"})
    public static final class Details extends com.rendly.app.ui.screens.publish.RendStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.RendStep.Details INSTANCE = null;
        
        private Details() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/RendStep$Edit;", "Lcom/rendly/app/ui/screens/publish/RendStep;", "()V", "app_debug"})
    public static final class Edit extends com.rendly.app.ui.screens.publish.RendStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.RendStep.Edit INSTANCE = null;
        
        private Edit() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/publish/RendStep$Gallery;", "Lcom/rendly/app/ui/screens/publish/RendStep;", "()V", "app_debug"})
    public static final class Gallery extends com.rendly.app.ui.screens.publish.RendStep {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.publish.RendStep.Gallery INSTANCE = null;
        
        private Gallery() {
        }
    }
}