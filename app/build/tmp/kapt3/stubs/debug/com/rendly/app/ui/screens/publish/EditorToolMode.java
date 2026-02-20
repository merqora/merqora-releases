package com.rendly.app.ui.screens.publish;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.gestures.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.*;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.input.pointer.awaitFirstDown;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import com.rendly.app.ui.theme.*;
import kotlinx.coroutines.Dispatchers;
import android.util.LruCache;
import androidx.compose.ui.graphics.PathEffect;
import androidx.compose.ui.graphics.drawscope.Stroke;

/**
 * Modos de herramientas activas
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\f\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\f\u00a8\u0006\r"}, d2 = {"Lcom/rendly/app/ui/screens/publish/EditorToolMode;", "", "(Ljava/lang/String;I)V", "MAIN", "AUDIO", "TEXT", "VOICE", "STICKERS", "CAPTIONS", "ADJUST", "FILTERS", "EFFECTS", "CONTEXT", "app_debug"})
public enum EditorToolMode {
    /*public static final*/ MAIN /* = new MAIN() */,
    /*public static final*/ AUDIO /* = new AUDIO() */,
    /*public static final*/ TEXT /* = new TEXT() */,
    /*public static final*/ VOICE /* = new VOICE() */,
    /*public static final*/ STICKERS /* = new STICKERS() */,
    /*public static final*/ CAPTIONS /* = new CAPTIONS() */,
    /*public static final*/ ADJUST /* = new ADJUST() */,
    /*public static final*/ FILTERS /* = new FILTERS() */,
    /*public static final*/ EFFECTS /* = new EFFECTS() */,
    /*public static final*/ CONTEXT /* = new CONTEXT() */;
    
    EditorToolMode() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.screens.publish.EditorToolMode> getEntries() {
        return null;
    }
}