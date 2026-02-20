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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rendly/app/ui/screens/publish/PreviewAspectRatio;", "", "(Ljava/lang/String;I)V", "PORTRAIT", "LANDSCAPE", "app_debug"})
public enum PreviewAspectRatio {
    /*public static final*/ PORTRAIT /* = new PORTRAIT() */,
    /*public static final*/ LANDSCAPE /* = new LANDSCAPE() */;
    
    PreviewAspectRatio() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.screens.publish.PreviewAspectRatio> getEntries() {
        return null;
    }
}