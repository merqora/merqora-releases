package com.rendly.app.data.remote;

import android.util.Log;
import com.rendly.app.BuildConfig;
import com.rendly.app.data.model.AddressPrediction;
import com.rendly.app.data.model.GeocodingResult;
import io.ktor.client.*;
import io.ktor.client.call.*;
import io.ktor.client.engine.okhttp.*;
import io.ktor.client.plugins.contentnegotiation.*;
import io.ktor.client.request.*;
import io.ktor.serialization.kotlinx.json.*;
import kotlinx.coroutines.Dispatchers;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;
import java.net.URLEncoder;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * MAPBOX SERVICE - Geocoding, Reverse Geocoding, and Address Search
 * ══════════════════════════════════════════════════════════════════════════════
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 &2\u00020\u0001:\u0001&B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\n\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u000bJ>\u0010\f\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000e2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00112\b\b\u0002\u0010\u0013\u001a\u00020\u00112\b\b\u0002\u0010\u0014\u001a\u00020\u0004J-\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\u00172\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u000e2\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u000eH\u0002\u00a2\u0006\u0002\u0010\u001aJ \u0010\u001b\u001a\u0004\u0018\u00010\t2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u001cJH\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001e2\u0006\u0010 \u001a\u00020\u00042\u0016\b\u0002\u0010!\u001a\u0010\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000e\u0018\u00010\"2\b\b\u0002\u0010#\u001a\u00020\u00042\b\b\u0002\u0010$\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010%R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/rendly/app/data/remote/MapboxService;", "", "()V", "TAG", "", "accessToken", "client", "Lio/ktor/client/HttpClient;", "geocodeAddress", "Lcom/rendly/app/data/model/GeocodingResult;", "address", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getStaticMapUrl", "latitude", "", "longitude", "zoom", "", "width", "height", "markerColor", "parseFeatureToGeocodingResult", "feature", "Lcom/rendly/app/data/remote/MapboxFeature;", "overrideLat", "overrideLon", "(Lcom/rendly/app/data/remote/MapboxFeature;Ljava/lang/Double;Ljava/lang/Double;)Lcom/rendly/app/data/model/GeocodingResult;", "reverseGeocode", "(DDLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchAddresses", "", "Lcom/rendly/app/data/model/AddressPrediction;", "query", "proximity", "Lkotlin/Pair;", "country", "limit", "(Ljava/lang/String;Lkotlin/Pair;Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class MapboxService {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String TAG = "MapboxService";
    @org.jetbrains.annotations.NotNull
    private final java.lang.String accessToken = "pk.eyJ1Ijoicm9kbmljb2xhcyIsImEiOiJjbWt1azgzdHcxaHZmM2RvZDR4MXpieGZpIn0.MMBQhnz8GfmLutTwamo5pQ";
    @org.jetbrains.annotations.NotNull
    private final io.ktor.client.HttpClient client = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String GEOCODING_BASE_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String SEARCH_BASE_URL = "https://api.mapbox.com/search/searchbox/v1";
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.remote.MapboxService.Companion Companion = null;
    
    @javax.inject.Inject
    public MapboxService() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object searchAddresses(@org.jetbrains.annotations.NotNull
    java.lang.String query, @org.jetbrains.annotations.Nullable
    kotlin.Pair<java.lang.Double, java.lang.Double> proximity, @org.jetbrains.annotations.NotNull
    java.lang.String country, int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.model.AddressPrediction>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object geocodeAddress(@org.jetbrains.annotations.NotNull
    java.lang.String address, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.model.GeocodingResult> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object reverseGeocode(double latitude, double longitude, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.model.GeocodingResult> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStaticMapUrl(double latitude, double longitude, int zoom, int width, int height, @org.jetbrains.annotations.NotNull
    java.lang.String markerColor) {
        return null;
    }
    
    private final com.rendly.app.data.model.GeocodingResult parseFeatureToGeocodingResult(com.rendly.app.data.remote.MapboxFeature feature, java.lang.Double overrideLat, java.lang.Double overrideLon) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/remote/MapboxService$Companion;", "", "()V", "GEOCODING_BASE_URL", "", "SEARCH_BASE_URL", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}