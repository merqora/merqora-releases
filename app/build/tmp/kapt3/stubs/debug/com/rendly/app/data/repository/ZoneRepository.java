package com.rendly.app.data.repository;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;
import java.util.Locale;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ZONE REPOSITORY - Geolocalización + búsquedas por zona
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Reutiliza el mismo patrón de FusedLocationProviderClient de AddressViewModel
 * pero de forma ligera (sin ViewModel, singleton para toda la app).
 *
 * Funcionalidades:
 * ✓ Auto-detección de ubicación del usuario (GPS + Geocoder)
 * ✓ Stats de zona desde Supabase (posts/vendedores cercanos)
 * ✓ Búsquedas populares por zona desde Supabase
 * ✓ Historial de búsquedas recientes (SharedPreferences)
 * ✓ Cache de ubicación (evita re-detectar en cada apertura)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\"\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0010\u0006\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\tJ,\u0010.\u001a\u00020\t2\u0006\u0010/\u001a\u00020\t2\u0006\u00100\u001a\u00020\t2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\t022\u0006\u00103\u001a\u000204J\u0010\u00105\u001a\u00020,2\u0006\u00106\u001a\u00020\u0017H\u0002J\u0006\u00107\u001a\u00020,J\u0016\u00108\u001a\u00020,2\u0006\u00109\u001a\u00020:H\u0086@\u00a2\u0006\u0002\u0010;J\u000e\u0010<\u001a\u00020=2\u0006\u00109\u001a\u00020:J\u000e\u0010>\u001a\u00020,2\u0006\u00109\u001a\u00020:J\b\u0010?\u001a\u00020,H\u0002J\u001a\u0010@\u001a\u00020,2\n\b\u0002\u0010A\u001a\u0004\u0018\u00010\tH\u0086@\u00a2\u0006\u0002\u0010BJ\b\u0010C\u001a\u00020,H\u0002J\u001e\u0010D\u001a\u00020,2\u0006\u0010E\u001a\u00020F2\u0006\u0010G\u001a\u00020FH\u0082@\u00a2\u0006\u0002\u0010HJ\u000e\u0010I\u001a\u00020,2\u0006\u0010-\u001a\u00020\tJ&\u0010J\u001a\u00020,2\u0006\u00109\u001a\u00020:2\u0006\u0010E\u001a\u00020F2\u0006\u0010G\u001a\u00020FH\u0082@\u00a2\u0006\u0002\u0010KJ\u0016\u0010L\u001a\u00020,2\f\u0010M\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00060\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00170 \u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u001d\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u00060 \u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\"R\u000e\u0010%\u001a\u00020&X\u0082.\u00a2\u0006\u0002\n\u0000R\u001d\u0010\'\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00060 \u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\"R\u0017\u0010)\u001a\b\u0012\u0004\u0012\u00020\u001c0 \u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\"\u00a8\u0006N"}, d2 = {"Lcom/rendly/app/data/repository/ZoneRepository;", "", "()V", "CACHE_DURATION_MS", "", "DEFAULT_POPULAR_SEARCHES", "", "Lcom/rendly/app/data/repository/PopularSearch;", "KEY_CACHED_ADDRESS", "", "KEY_CACHED_CITY", "KEY_CACHED_COUNTRY", "KEY_CACHED_LAT", "KEY_CACHED_LON", "KEY_CACHED_STATE", "KEY_CACHE_TIMESTAMP", "KEY_RECENT_SEARCHES", "MAX_RECENT_SEARCHES", "", "PREFS_NAME", "TAG", "_locationState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/rendly/app/data/repository/ZoneLocationState;", "_popularSearches", "_recentSearches", "Lcom/rendly/app/data/repository/RecentSearch;", "_zoneStats", "Lcom/rendly/app/data/repository/ZoneStats;", "cancellationTokenSource", "Lcom/google/android/gms/tasks/CancellationTokenSource;", "locationState", "Lkotlinx/coroutines/flow/StateFlow;", "getLocationState", "()Lkotlinx/coroutines/flow/StateFlow;", "popularSearches", "getPopularSearches", "prefs", "Landroid/content/SharedPreferences;", "recentSearches", "getRecentSearches", "zoneStats", "getZoneStats", "addRecentSearch", "", "query", "buildSearchQuery", "baseQuery", "zoneType", "filters", "", "distanceKm", "", "cacheLocation", "state", "clearRecentSearches", "detectLocation", "context", "Landroid/content/Context;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "hasLocationPermission", "", "init", "loadCachedLocation", "loadPopularSearches", "city", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadRecentSearches", "loadZoneStats", "lat", "", "lon", "(DDLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "removeRecentSearch", "reverseGeocodeAndUpdate", "(Landroid/content/Context;DDLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveRecentSearches", "searches", "app_debug"})
public final class ZoneRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ZoneRepository";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String PREFS_NAME = "zone_prefs";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_RECENT_SEARCHES = "recent_searches";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CACHED_CITY = "cached_city";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CACHED_COUNTRY = "cached_country";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CACHED_STATE = "cached_state";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CACHED_LAT = "cached_lat";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CACHED_LON = "cached_lon";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CACHED_ADDRESS = "cached_formatted_address";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEY_CACHE_TIMESTAMP = "cache_timestamp";
    private static final long CACHE_DURATION_MS = 1800000L;
    private static final int MAX_RECENT_SEARCHES = 10;
    private static android.content.SharedPreferences prefs;
    @org.jetbrains.annotations.Nullable
    private static com.google.android.gms.tasks.CancellationTokenSource cancellationTokenSource;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.repository.ZoneLocationState> _locationState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.ZoneLocationState> locationState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.repository.ZoneStats> _zoneStats = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.ZoneStats> zoneStats = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.PopularSearch>> _popularSearches = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.PopularSearch>> popularSearches = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.RecentSearch>> _recentSearches = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.RecentSearch>> recentSearches = null;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<com.rendly.app.data.repository.PopularSearch> DEFAULT_POPULAR_SEARCHES = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.ZoneRepository INSTANCE = null;
    
    private ZoneRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.ZoneLocationState> getLocationState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.ZoneStats> getZoneStats() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.PopularSearch>> getPopularSearches() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.RecentSearch>> getRecentSearches() {
        return null;
    }
    
    public final void init(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    public final boolean hasLocationPermission(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return false;
    }
    
    /**
     * Detectar ubicación del usuario. Llama DESPUÉS de verificar/obtener permisos.
     */
    @kotlin.Suppress(names = {"MissingPermission"})
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object detectLocation(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object reverseGeocodeAndUpdate(android.content.Context context, double lat, double lon, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object loadZoneStats(double lat, double lon, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadPopularSearches(@org.jetbrains.annotations.Nullable
    java.lang.String city, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void addRecentSearch(@org.jetbrains.annotations.NotNull
    java.lang.String query) {
    }
    
    public final void clearRecentSearches() {
    }
    
    public final void removeRecentSearch(@org.jetbrains.annotations.NotNull
    java.lang.String query) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String buildSearchQuery(@org.jetbrains.annotations.NotNull
    java.lang.String baseQuery, @org.jetbrains.annotations.NotNull
    java.lang.String zoneType, @org.jetbrains.annotations.NotNull
    java.util.Set<java.lang.String> filters, float distanceKm) {
        return null;
    }
    
    private final void cacheLocation(com.rendly.app.data.repository.ZoneLocationState state) {
    }
    
    private final void loadCachedLocation() {
    }
    
    private final void saveRecentSearches(java.util.List<com.rendly.app.data.repository.RecentSearch> searches) {
    }
    
    private final void loadRecentSearches() {
    }
}