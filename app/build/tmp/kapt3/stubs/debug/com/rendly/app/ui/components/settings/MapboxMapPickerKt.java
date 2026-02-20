package com.rendly.app.ui.components.settings;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.webkit.*;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.BuildConfig;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000<\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\u001ah\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\u0018\u0010\u0007\u001a\u0014\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\n2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\nH\u0003\u001a\u0083\u0001\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u000b2K\u0010\u0013\u001aG\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0002\u0012\u0013\u0012\u00110\u0003\u00a2\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0004\u0012\u0013\u0012\u00110\u000b\u00a2\u0006\f\b\u0015\u0012\b\b\u0016\u0012\u0004\b\b(\u0017\u0012\u0004\u0012\u00020\u00010\u00142\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u001a \u0010\u0019\u001a\u00020\u000b2\u0006\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u001c\u001a\u00020\u000bH\u0002\u00a8\u0006\u001d"}, d2 = {"MapWebView", "", "latitude", "", "longitude", "onMapReady", "Lkotlin/Function0;", "onLocationChanged", "Lkotlin/Function2;", "onAddressResolved", "Lkotlin/Function1;", "", "onError", "MapboxMapPicker", "isVisible", "", "initialLatitude", "initialLongitude", "initialAddress", "onLocationSelected", "Lkotlin/Function3;", "Lkotlin/ParameterName;", "name", "address", "onDismiss", "generateMapHtml", "lat", "lng", "accessToken", "app_debug"})
public final class MapboxMapPickerKt {
    
    /**
     * ══════════════════════════════════════════════════════════════════════════════
     * MAPBOX MAP PICKER - Full-screen interactive map for location selection
     * ══════════════════════════════════════════════════════════════════════════════
     */
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void MapboxMapPicker(boolean isVisible, double initialLatitude, double initialLongitude, @org.jetbrains.annotations.NotNull
    java.lang.String initialAddress, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function3<? super java.lang.Double, ? super java.lang.Double, ? super java.lang.String, kotlin.Unit> onLocationSelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @android.annotation.SuppressLint(value = {"SetJavaScriptEnabled"})
    @androidx.compose.runtime.Composable
    private static final void MapWebView(double latitude, double longitude, kotlin.jvm.functions.Function0<kotlin.Unit> onMapReady, kotlin.jvm.functions.Function2<? super java.lang.Double, ? super java.lang.Double, kotlin.Unit> onLocationChanged, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onAddressResolved, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError) {
    }
    
    private static final java.lang.String generateMapHtml(double lat, double lng, java.lang.String accessToken) {
        return null;
    }
}