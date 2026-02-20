package com.rendly.app.ui.viewmodel;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.rendly.app.data.model.*;
import com.rendly.app.data.remote.MapboxService;
import com.rendly.app.data.repository.AddressRepository;
import com.rendly.app.engine.AddressEngine;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.*;
import kotlinx.serialization.json.*;
import java.net.URLEncoder;
import javax.inject.Inject;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * ADDRESS VIEWMODEL - Gestión de direcciones con geocoding y validación
 * ══════════════════════════════════════════════════════════════════════════════
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u009c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0017\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010.\u001a\u00020/2\u0006\u00100\u001a\u00020\nJ\u000e\u00101\u001a\u00020/H\u0082@\u00a2\u0006\u0002\u00102J\u000e\u00103\u001a\u00020/2\u0006\u00104\u001a\u00020\nJ\u0006\u0010!\u001a\u00020/J\u0006\u00105\u001a\u00020/J\u0016\u00106\u001a\u00020\n2\u0006\u00107\u001a\u0002082\u0006\u00109\u001a\u000208J\b\u0010:\u001a\u00020;H\u0002J\u000e\u0010<\u001a\u00020/2\u0006\u0010=\u001a\u00020\nJ\b\u0010>\u001a\u00020/H\u0014J\u001c\u0010?\u001a\b\u0012\u0004\u0012\u00020\u00150\r2\u0006\u0010@\u001a\u00020\nH\u0082@\u00a2\u0006\u0002\u0010AJ\u0018\u0010B\u001a\u0004\u0018\u00010C2\u0006\u00104\u001a\u00020\nH\u0082@\u00a2\u0006\u0002\u0010AJ \u0010D\u001a\u0004\u0018\u00010C2\u0006\u0010E\u001a\u0002082\u0006\u0010F\u001a\u000208H\u0082@\u00a2\u0006\u0002\u0010GJ\u0006\u0010H\u001a\u00020/J\u0016\u0010I\u001a\u00020/2\u0006\u00107\u001a\u0002082\u0006\u00109\u001a\u000208J\u000e\u0010J\u001a\u00020/2\u0006\u0010=\u001a\u00020\nJ\u000e\u0010K\u001a\u00020/2\u0006\u0010@\u001a\u00020\nJ\u000e\u0010L\u001a\u00020/2\u0006\u0010M\u001a\u00020\u0015J\u0016\u0010N\u001a\u00020/2\u0006\u00100\u001a\u00020\n2\u0006\u0010=\u001a\u00020\nJ\u000e\u0010O\u001a\u00020/2\u0006\u0010P\u001a\u00020QJ\u000e\u0010R\u001a\u00020/2\u0006\u0010S\u001a\u00020\nJ\u000e\u0010T\u001a\u00020/2\u0006\u0010U\u001a\u00020\nJ\u000e\u0010V\u001a\u00020/2\u0006\u0010W\u001a\u00020\nJ\u000e\u0010X\u001a\u00020/2\u0006\u0010Y\u001a\u00020\nJ\u000e\u0010Z\u001a\u00020/2\u0006\u0010[\u001a\u00020;J\u000e\u0010\\\u001a\u00020/2\u0006\u0010]\u001a\u00020\nJ\u001e\u0010^\u001a\u00020/2\u0006\u00107\u001a\u0002082\u0006\u00109\u001a\u0002082\u0006\u00104\u001a\u00020\nJ\u000e\u0010_\u001a\u00020/2\u0006\u0010`\u001a\u00020\nJ\u000e\u0010a\u001a\u00020/2\u0006\u0010b\u001a\u00020\nJ\u000e\u0010c\u001a\u00020/2\u0006\u0010d\u001a\u00020\nJ\u000e\u0010e\u001a\u00020/2\u0006\u0010f\u001a\u00020\nJ\u0006\u0010g\u001a\u00020/R\u000e\u0010\t\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00190\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010 \u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001dR\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00130#\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0010\u0010&\u001a\u0004\u0018\u00010\'X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\r0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001dR\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00170\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001dR\u0019\u0010,\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00190\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001d\u00a8\u0006h"}, d2 = {"Lcom/rendly/app/ui/viewmodel/AddressViewModel;", "Landroidx/lifecycle/ViewModel;", "addressRepository", "Lcom/rendly/app/data/repository/AddressRepository;", "mapboxService", "Lcom/rendly/app/data/remote/MapboxService;", "context", "Landroid/content/Context;", "(Lcom/rendly/app/data/repository/AddressRepository;Lcom/rendly/app/data/remote/MapboxService;Landroid/content/Context;)V", "TAG", "", "_addresses", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/model/Address;", "_currentLocation", "Landroid/location/Location;", "_events", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/rendly/app/ui/viewmodel/AddressEvent;", "_predictions", "Lcom/rendly/app/data/model/AddressPrediction;", "_uiState", "Lcom/rendly/app/ui/viewmodel/AddressUiState;", "_validationResult", "Lcom/rendly/app/engine/AddressEngine$ValidationResult;", "addresses", "Lkotlinx/coroutines/flow/StateFlow;", "getAddresses", "()Lkotlinx/coroutines/flow/StateFlow;", "cancellationTokenSource", "Lcom/google/android/gms/tasks/CancellationTokenSource;", "currentLocation", "getCurrentLocation", "events", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvents", "()Lkotlinx/coroutines/flow/SharedFlow;", "fusedLocationClient", "Lcom/google/android/gms/location/FusedLocationProviderClient;", "predictions", "getPredictions", "uiState", "getUiState", "validationResult", "getValidationResult", "deleteAddress", "", "addressId", "fetchCurrentLocationInternal", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "geocodeAddress", "address", "getCurrentLocationAfterPermission", "getStaticMapUrl", "latitude", "", "longitude", "hasLocationPermission", "", "loadAddresses", "userId", "onCleared", "performAddressSearch", "query", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "performGeocode", "Lcom/rendly/app/data/model/GeocodingResult;", "performReverseGeocode", "lat", "lon", "(DDLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetForm", "reverseGeocode", "saveAddress", "searchAddresses", "selectPrediction", "prediction", "setDefaultAddress", "updateAddressType", "type", "Lcom/rendly/app/data/model/AddressType;", "updateApartment", "apartment", "updateCity", "city", "updateDeliveryInstructions", "instructions", "updateFloor", "floor", "updateIsDefault", "isDefault", "updateLabel", "label", "updateLocationFromMap", "updatePostalCode", "postalCode", "updateReferencePoint", "reference", "updateStreetAddress", "street", "updateStreetNumber", "number", "validateCurrentAddress", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class AddressViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.repository.AddressRepository addressRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.remote.MapboxService mapboxService = null;
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String TAG = "AddressViewModel";
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.ui.viewmodel.AddressUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.rendly.app.ui.viewmodel.AddressUiState> uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.Address>> _addresses = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.Address>> addresses = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.AddressPrediction>> _predictions = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.AddressPrediction>> predictions = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<android.location.Location> _currentLocation = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<android.location.Location> currentLocation = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.engine.AddressEngine.ValidationResult> _validationResult = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.rendly.app.engine.AddressEngine.ValidationResult> validationResult = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.rendly.app.ui.viewmodel.AddressEvent> _events = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.SharedFlow<com.rendly.app.ui.viewmodel.AddressEvent> events = null;
    @org.jetbrains.annotations.Nullable
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    @org.jetbrains.annotations.Nullable
    private com.google.android.gms.tasks.CancellationTokenSource cancellationTokenSource;
    
    @javax.inject.Inject
    public AddressViewModel(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.repository.AddressRepository addressRepository, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.remote.MapboxService mapboxService, @dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.ui.viewmodel.AddressUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.Address>> getAddresses() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.AddressPrediction>> getPredictions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<android.location.Location> getCurrentLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.engine.AddressEngine.ValidationResult> getValidationResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.SharedFlow<com.rendly.app.ui.viewmodel.AddressEvent> getEvents() {
        return null;
    }
    
    /**
     * Carga las direcciones de un usuario
     */
    public final void loadAddresses(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
    }
    
    /**
     * Busca predicciones de direcciones (autocomplete)
     */
    public final void searchAddresses(@org.jetbrains.annotations.NotNull
    java.lang.String query) {
    }
    
    /**
     * Geocodifica una dirección (texto → coordenadas)
     */
    public final void geocodeAddress(@org.jetbrains.annotations.NotNull
    java.lang.String address) {
    }
    
    /**
     * Reverse geocoding (coordenadas → texto)
     */
    public final void reverseGeocode(double latitude, double longitude) {
    }
    
    /**
     * Obtiene la ubicación actual del usuario
     * Este método verifica permisos y solicita si no los tiene
     */
    public final void getCurrentLocation() {
    }
    
    /**
     * Obtiene la ubicación después de que los permisos fueron concedidos
     * NO verifica permisos porque ya sabemos que fueron concedidos por el launcher
     */
    public final void getCurrentLocationAfterPermission() {
    }
    
    /**
     * Lógica interna para obtener ubicación (sin verificar permisos)
     */
    @kotlin.Suppress(names = {"MissingPermission"})
    private final java.lang.Object fetchCurrentLocationInternal(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Selecciona una predicción de autocompletado
     */
    public final void selectPrediction(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressPrediction prediction) {
    }
    
    /**
     * Actualiza la ubicación desde el mapa interactivo
     */
    public final void updateLocationFromMap(double latitude, double longitude, @org.jetbrains.annotations.NotNull
    java.lang.String address) {
    }
    
    /**
     * Valida la dirección actual con el motor C++
     */
    public final void validateCurrentAddress() {
    }
    
    /**
     * Guarda la dirección actual
     */
    public final void saveAddress(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
    }
    
    /**
     * Establece una dirección como default
     */
    public final void setDefaultAddress(@org.jetbrains.annotations.NotNull
    java.lang.String addressId, @org.jetbrains.annotations.NotNull
    java.lang.String userId) {
    }
    
    /**
     * Elimina una dirección
     */
    public final void deleteAddress(@org.jetbrains.annotations.NotNull
    java.lang.String addressId) {
    }
    
    public final void updateLabel(@org.jetbrains.annotations.NotNull
    java.lang.String label) {
    }
    
    public final void updateAddressType(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressType type) {
    }
    
    public final void updateStreetAddress(@org.jetbrains.annotations.NotNull
    java.lang.String street) {
    }
    
    public final void updateStreetNumber(@org.jetbrains.annotations.NotNull
    java.lang.String number) {
    }
    
    public final void updateApartment(@org.jetbrains.annotations.NotNull
    java.lang.String apartment) {
    }
    
    public final void updateFloor(@org.jetbrains.annotations.NotNull
    java.lang.String floor) {
    }
    
    public final void updateCity(@org.jetbrains.annotations.NotNull
    java.lang.String city) {
    }
    
    public final void updatePostalCode(@org.jetbrains.annotations.NotNull
    java.lang.String postalCode) {
    }
    
    public final void updateDeliveryInstructions(@org.jetbrains.annotations.NotNull
    java.lang.String instructions) {
    }
    
    public final void updateReferencePoint(@org.jetbrains.annotations.NotNull
    java.lang.String reference) {
    }
    
    public final void updateIsDefault(boolean isDefault) {
    }
    
    public final void resetForm() {
    }
    
    private final boolean hasLocationPermission() {
        return false;
    }
    
    private final java.lang.Object performAddressSearch(java.lang.String query, kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.model.AddressPrediction>> $completion) {
        return null;
    }
    
    private final java.lang.Object performGeocode(java.lang.String address, kotlin.coroutines.Continuation<? super com.rendly.app.data.model.GeocodingResult> $completion) {
        return null;
    }
    
    private final java.lang.Object performReverseGeocode(double lat, double lon, kotlin.coroutines.Continuation<? super com.rendly.app.data.model.GeocodingResult> $completion) {
        return null;
    }
    
    /**
     * Get static map URL for preview
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStaticMapUrl(double latitude, double longitude) {
        return null;
    }
    
    @java.lang.Override
    protected void onCleared() {
    }
}