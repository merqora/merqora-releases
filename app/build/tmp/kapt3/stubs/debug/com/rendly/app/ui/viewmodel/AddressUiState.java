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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u0006\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\bN\b\u0086\b\u0018\u00002\u00020\u0001B\u00c9\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\n\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\n\u0012\b\b\u0002\u0010\u000f\u001a\u00020\n\u0012\b\b\u0002\u0010\u0010\u001a\u00020\n\u0012\b\b\u0002\u0010\u0011\u001a\u00020\n\u0012\b\b\u0002\u0010\u0012\u001a\u00020\n\u0012\b\b\u0002\u0010\u0013\u001a\u00020\n\u0012\b\b\u0002\u0010\u0014\u001a\u00020\n\u0012\b\b\u0002\u0010\u0015\u001a\u00020\n\u0012\b\b\u0002\u0010\u0016\u001a\u00020\n\u0012\b\b\u0002\u0010\u0017\u001a\u00020\n\u0012\b\b\u0002\u0010\u0018\u001a\u00020\n\u0012\b\b\u0002\u0010\u0019\u001a\u00020\n\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001d\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001f\u001a\u00020 \u0012\b\b\u0002\u0010!\u001a\u00020\"\u0012\b\b\u0002\u0010#\u001a\u00020$\u0012\n\b\u0002\u0010%\u001a\u0004\u0018\u00010&\u0012\b\b\u0002\u0010\'\u001a\u00020\u0003\u0012\b\b\u0002\u0010(\u001a\u00020\n\u0012\b\b\u0002\u0010)\u001a\u00020\n\u00a2\u0006\u0002\u0010*J\t\u0010O\u001a\u00020\u0003H\u00c6\u0003J\t\u0010P\u001a\u00020\nH\u00c6\u0003J\t\u0010Q\u001a\u00020\nH\u00c6\u0003J\t\u0010R\u001a\u00020\nH\u00c6\u0003J\t\u0010S\u001a\u00020\nH\u00c6\u0003J\t\u0010T\u001a\u00020\nH\u00c6\u0003J\t\u0010U\u001a\u00020\nH\u00c6\u0003J\t\u0010V\u001a\u00020\nH\u00c6\u0003J\t\u0010W\u001a\u00020\nH\u00c6\u0003J\t\u0010X\u001a\u00020\nH\u00c6\u0003J\t\u0010Y\u001a\u00020\nH\u00c6\u0003J\t\u0010Z\u001a\u00020\u0003H\u00c6\u0003J\t\u0010[\u001a\u00020\nH\u00c6\u0003J\t\u0010\\\u001a\u00020\nH\u00c6\u0003J\t\u0010]\u001a\u00020\u001bH\u00c6\u0003J\t\u0010^\u001a\u00020\u001bH\u00c6\u0003J\t\u0010_\u001a\u00020\u001bH\u00c6\u0003J\t\u0010`\u001a\u00020\u001bH\u00c6\u0003J\t\u0010a\u001a\u00020 H\u00c6\u0003J\t\u0010b\u001a\u00020\"H\u00c6\u0003J\t\u0010c\u001a\u00020$H\u00c6\u0003J\u000b\u0010d\u001a\u0004\u0018\u00010&H\u00c6\u0003J\t\u0010e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010g\u001a\u00020\nH\u00c6\u0003J\t\u0010h\u001a\u00020\nH\u00c6\u0003J\t\u0010i\u001a\u00020\u0003H\u00c6\u0003J\t\u0010j\u001a\u00020\u0003H\u00c6\u0003J\t\u0010k\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010l\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\t\u0010m\u001a\u00020\nH\u00c6\u0003J\t\u0010n\u001a\u00020\rH\u00c6\u0003J\u00cd\u0002\u0010o\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\b\b\u0002\u0010\u000b\u001a\u00020\n2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\n2\b\b\u0002\u0010\u000f\u001a\u00020\n2\b\b\u0002\u0010\u0010\u001a\u00020\n2\b\b\u0002\u0010\u0011\u001a\u00020\n2\b\b\u0002\u0010\u0012\u001a\u00020\n2\b\b\u0002\u0010\u0013\u001a\u00020\n2\b\b\u0002\u0010\u0014\u001a\u00020\n2\b\b\u0002\u0010\u0015\u001a\u00020\n2\b\b\u0002\u0010\u0016\u001a\u00020\n2\b\b\u0002\u0010\u0017\u001a\u00020\n2\b\b\u0002\u0010\u0018\u001a\u00020\n2\b\b\u0002\u0010\u0019\u001a\u00020\n2\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\b\b\u0002\u0010\u001c\u001a\u00020\u001b2\b\b\u0002\u0010\u001d\u001a\u00020\u001b2\b\b\u0002\u0010\u001e\u001a\u00020\u001b2\b\b\u0002\u0010\u001f\u001a\u00020 2\b\b\u0002\u0010!\u001a\u00020\"2\b\b\u0002\u0010#\u001a\u00020$2\n\b\u0002\u0010%\u001a\u0004\u0018\u00010&2\b\b\u0002\u0010\'\u001a\u00020\u00032\b\b\u0002\u0010(\u001a\u00020\n2\b\b\u0002\u0010)\u001a\u00020\nH\u00c6\u0001J\u0013\u0010p\u001a\u00020\u00032\b\u0010q\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010r\u001a\u00020 H\u00d6\u0001J\t\u0010s\u001a\u00020\nH\u00d6\u0001R\u0011\u0010#\u001a\u00020$\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0011\u0010!\u001a\u00020\"\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010.R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0011\u0010\u0012\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0011\u0010\u0015\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u00102R\u0011\u0010\u001f\u001a\u00020 \u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0011\u0010\u0018\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u00102R\u0011\u0010\u0019\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u00102R\u0011\u0010(\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00102R\u0013\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u00102R\u0011\u0010\u0013\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u00102R\u0011\u0010\u000f\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u00102R\u0011\u0010\u001d\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010=R\u0011\u0010\u001e\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010=R\u0011\u0010?\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b@\u0010AR\u0011\u0010\'\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010AR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010AR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010AR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010AR\u0011\u0010B\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\bB\u0010AR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010AR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010AR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010AR\u0011\u0010\u000b\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u00102R\u0011\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010=R\u0011\u0010\u001c\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010=R\u0011\u0010\u0014\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u00102R\u0011\u0010\u0017\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u00102R\u0011\u0010\u000e\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u00102R\u0011\u0010)\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u00102R\u0013\u0010%\u001a\u0004\u0018\u00010&\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u0010KR\u0011\u0010\u0016\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bL\u00102R\u0011\u0010\u0010\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u00102R\u0011\u0010\u0011\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\bN\u00102\u00a8\u0006t"}, d2 = {"Lcom/rendly/app/ui/viewmodel/AddressUiState;", "", "isLoading", "", "isSearching", "isGeocoding", "isGettingLocation", "isValidating", "isSaving", "error", "", "label", "addressType", "Lcom/rendly/app/data/model/AddressType;", "rawInput", "formattedAddress", "streetAddress", "streetNumber", "apartment", "floor", "neighborhood", "city", "stateProvince", "postalCode", "country", "countryCode", "latitude", "", "longitude", "gpsLatitude", "gpsLongitude", "confidenceScore", "", "addressStatus", "Lcom/rendly/app/data/model/AddressStatus;", "addressSource", "Lcom/rendly/app/data/model/AddressSource;", "selectedGeocodingResult", "Lcom/rendly/app/data/model/GeocodingResult;", "isDefault", "deliveryInstructions", "referencePoint", "(ZZZZZZLjava/lang/String;Ljava/lang/String;Lcom/rendly/app/data/model/AddressType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DDDDILcom/rendly/app/data/model/AddressStatus;Lcom/rendly/app/data/model/AddressSource;Lcom/rendly/app/data/model/GeocodingResult;ZLjava/lang/String;Ljava/lang/String;)V", "getAddressSource", "()Lcom/rendly/app/data/model/AddressSource;", "getAddressStatus", "()Lcom/rendly/app/data/model/AddressStatus;", "getAddressType", "()Lcom/rendly/app/data/model/AddressType;", "getApartment", "()Ljava/lang/String;", "getCity", "getConfidenceScore", "()I", "getCountry", "getCountryCode", "getDeliveryInstructions", "getError", "getFloor", "getFormattedAddress", "getGpsLatitude", "()D", "getGpsLongitude", "hasValidLocation", "getHasValidLocation", "()Z", "isProcessing", "getLabel", "getLatitude", "getLongitude", "getNeighborhood", "getPostalCode", "getRawInput", "getReferencePoint", "getSelectedGeocodingResult", "()Lcom/rendly/app/data/model/GeocodingResult;", "getStateProvince", "getStreetAddress", "getStreetNumber", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component32", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class AddressUiState {
    private final boolean isLoading = false;
    private final boolean isSearching = false;
    private final boolean isGeocoding = false;
    private final boolean isGettingLocation = false;
    private final boolean isValidating = false;
    private final boolean isSaving = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String error = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String label = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.AddressType addressType = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String rawInput = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String formattedAddress = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String streetAddress = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String streetNumber = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String apartment = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String floor = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String neighborhood = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String city = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String stateProvince = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String postalCode = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String country = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String countryCode = null;
    private final double latitude = 0.0;
    private final double longitude = 0.0;
    private final double gpsLatitude = 0.0;
    private final double gpsLongitude = 0.0;
    private final int confidenceScore = 0;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.AddressStatus addressStatus = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.AddressSource addressSource = null;
    @org.jetbrains.annotations.Nullable
    private final com.rendly.app.data.model.GeocodingResult selectedGeocodingResult = null;
    private final boolean isDefault = false;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String deliveryInstructions = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String referencePoint = null;
    
    public AddressUiState(boolean isLoading, boolean isSearching, boolean isGeocoding, boolean isGettingLocation, boolean isValidating, boolean isSaving, @org.jetbrains.annotations.Nullable
    java.lang.String error, @org.jetbrains.annotations.NotNull
    java.lang.String label, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressType addressType, @org.jetbrains.annotations.NotNull
    java.lang.String rawInput, @org.jetbrains.annotations.NotNull
    java.lang.String formattedAddress, @org.jetbrains.annotations.NotNull
    java.lang.String streetAddress, @org.jetbrains.annotations.NotNull
    java.lang.String streetNumber, @org.jetbrains.annotations.NotNull
    java.lang.String apartment, @org.jetbrains.annotations.NotNull
    java.lang.String floor, @org.jetbrains.annotations.NotNull
    java.lang.String neighborhood, @org.jetbrains.annotations.NotNull
    java.lang.String city, @org.jetbrains.annotations.NotNull
    java.lang.String stateProvince, @org.jetbrains.annotations.NotNull
    java.lang.String postalCode, @org.jetbrains.annotations.NotNull
    java.lang.String country, @org.jetbrains.annotations.NotNull
    java.lang.String countryCode, double latitude, double longitude, double gpsLatitude, double gpsLongitude, int confidenceScore, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressStatus addressStatus, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressSource addressSource, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.GeocodingResult selectedGeocodingResult, boolean isDefault, @org.jetbrains.annotations.NotNull
    java.lang.String deliveryInstructions, @org.jetbrains.annotations.NotNull
    java.lang.String referencePoint) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final boolean isSearching() {
        return false;
    }
    
    public final boolean isGeocoding() {
        return false;
    }
    
    public final boolean isGettingLocation() {
        return false;
    }
    
    public final boolean isValidating() {
        return false;
    }
    
    public final boolean isSaving() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLabel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressType getAddressType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getRawInput() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStreetAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStreetNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getApartment() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFloor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getNeighborhood() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCity() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStateProvince() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPostalCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCountry() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCountryCode() {
        return null;
    }
    
    public final double getLatitude() {
        return 0.0;
    }
    
    public final double getLongitude() {
        return 0.0;
    }
    
    public final double getGpsLatitude() {
        return 0.0;
    }
    
    public final double getGpsLongitude() {
        return 0.0;
    }
    
    public final int getConfidenceScore() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressStatus getAddressStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressSource getAddressSource() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.GeocodingResult getSelectedGeocodingResult() {
        return null;
    }
    
    public final boolean isDefault() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDeliveryInstructions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getReferencePoint() {
        return null;
    }
    
    public final boolean getHasValidLocation() {
        return false;
    }
    
    public final boolean isProcessing() {
        return false;
    }
    
    public AddressUiState() {
        super();
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component17() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component19() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component20() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component21() {
        return null;
    }
    
    public final double component22() {
        return 0.0;
    }
    
    public final double component23() {
        return 0.0;
    }
    
    public final double component24() {
        return 0.0;
    }
    
    public final double component25() {
        return 0.0;
    }
    
    public final int component26() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressStatus component27() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressSource component28() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.GeocodingResult component29() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean component30() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component31() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component32() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.AddressType component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.ui.viewmodel.AddressUiState copy(boolean isLoading, boolean isSearching, boolean isGeocoding, boolean isGettingLocation, boolean isValidating, boolean isSaving, @org.jetbrains.annotations.Nullable
    java.lang.String error, @org.jetbrains.annotations.NotNull
    java.lang.String label, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressType addressType, @org.jetbrains.annotations.NotNull
    java.lang.String rawInput, @org.jetbrains.annotations.NotNull
    java.lang.String formattedAddress, @org.jetbrains.annotations.NotNull
    java.lang.String streetAddress, @org.jetbrains.annotations.NotNull
    java.lang.String streetNumber, @org.jetbrains.annotations.NotNull
    java.lang.String apartment, @org.jetbrains.annotations.NotNull
    java.lang.String floor, @org.jetbrains.annotations.NotNull
    java.lang.String neighborhood, @org.jetbrains.annotations.NotNull
    java.lang.String city, @org.jetbrains.annotations.NotNull
    java.lang.String stateProvince, @org.jetbrains.annotations.NotNull
    java.lang.String postalCode, @org.jetbrains.annotations.NotNull
    java.lang.String country, @org.jetbrains.annotations.NotNull
    java.lang.String countryCode, double latitude, double longitude, double gpsLatitude, double gpsLongitude, int confidenceScore, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressStatus addressStatus, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressSource addressSource, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.GeocodingResult selectedGeocodingResult, boolean isDefault, @org.jetbrains.annotations.NotNull
    java.lang.String deliveryInstructions, @org.jetbrains.annotations.NotNull
    java.lang.String referencePoint) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}