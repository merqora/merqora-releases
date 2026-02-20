package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.data.model.*;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.engine.AddressEngine;
import io.github.jan.supabase.postgrest.query.Order;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.Flow;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * ADDRESS REPOSITORY - Gestión de direcciones con Supabase y motor C++
 * ══════════════════════════════════════════════════════════════════════════════
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0012\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u0014\u001a\u00020\n2\u0006\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u0006\u0010\u0018\u001a\u00020\u0019J8\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\b0\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010 \u001a\u00020\u001fH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b!\u0010\"J$\u0010#\u001a\b\u0012\u0004\u0012\u00020\n0\u001b2\u0006\u0010$\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b%\u0010&J&\u0010\'\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u001b2\u0006\u0010$\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b(\u0010&J*\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u001b2\u0006\u0010)\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b*\u0010&J\u0018\u0010+\u001a\u0004\u0018\u00010\b2\u0006\u0010)\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010&J\u0016\u0010,\u001a\u00020\u00042\u0006\u0010-\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010&J,\u0010.\u001a\b\u0012\u0004\u0012\u00020\n0\u001b2\u0006\u0010$\u001a\u00020\u00042\u0006\u0010)\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b/\u0010\u0017J8\u00100\u001a\b\u0012\u0004\u0012\u00020\b0\u001b2\u0006\u0010$\u001a\u00020\u00042\u0012\u00101\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u000102H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b3\u00104JD\u00105\u001a\u0002062\u0006\u00107\u001a\u00020\u00042\u0006\u00108\u001a\u00020\u001f2\u0006\u00109\u001a\u00020\u001f2\b\b\u0002\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010 \u001a\u00020\u001f2\b\b\u0002\u0010:\u001a\u00020;H\u0086@\u00a2\u0006\u0002\u0010<R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0014\u0010\u0010\u001a\u00020\u00118BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006="}, d2 = {"Lcom/rendly/app/data/repository/AddressRepository;", "", "()V", "TAG", "", "_addresses", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/model/Address;", "_isLoading", "", "addresses", "Lkotlinx/coroutines/flow/Flow;", "getAddresses", "()Lkotlinx/coroutines/flow/Flow;", "isLoading", "supabaseClient", "Lio/github/jan/supabase/SupabaseClient;", "getSupabaseClient", "()Lio/github/jan/supabase/SupabaseClient;", "areAddressesSimilar", "addr1", "addr2", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearLocalCache", "", "createAddress", "Lkotlin/Result;", "request", "Lcom/rendly/app/data/model/AddressCreateRequest;", "gpsLatitude", "", "gpsLongitude", "createAddress-BWLJW6A", "(Lcom/rendly/app/data/model/AddressCreateRequest;DDLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAddress", "addressId", "deleteAddress-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAddressById", "getAddressById-gIAlu-s", "userId", "getAddresses-gIAlu-s", "getDefaultAddress", "normalizeAddress", "rawInput", "setDefaultAddress", "setDefaultAddress-0E7RQCE", "updateAddress", "updates", "", "updateAddress-0E7RQCE", "(Ljava/lang/String;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "validateAddressWithEngine", "Lcom/rendly/app/engine/AddressEngine$ValidationResult;", "formattedAddress", "latitude", "longitude", "source", "Lcom/rendly/app/data/model/AddressSource;", "(Ljava/lang/String;DDDDLcom/rendly/app/data/model/AddressSource;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class AddressRepository {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String TAG = "AddressRepository";
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.Address>> _addresses = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.rendly.app.data.model.Address>> addresses = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.Flow<java.lang.Boolean> isLoading = null;
    
    @javax.inject.Inject
    public AddressRepository() {
        super();
    }
    
    private final io.github.jan.supabase.SupabaseClient getSupabaseClient() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.rendly.app.data.model.Address>> getAddresses() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    /**
     * Valida una dirección usando el motor C++
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object validateAddressWithEngine(@org.jetbrains.annotations.NotNull
    java.lang.String formattedAddress, double latitude, double longitude, double gpsLatitude, double gpsLongitude, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.AddressSource source, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.engine.AddressEngine.ValidationResult> $completion) {
        return null;
    }
    
    /**
     * Normaliza una dirección usando el motor C++
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object normalizeAddress(@org.jetbrains.annotations.NotNull
    java.lang.String rawInput, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Compara dos direcciones para detectar duplicados
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object areAddressesSimilar(@org.jetbrains.annotations.NotNull
    java.lang.String addr1, @org.jetbrains.annotations.NotNull
    java.lang.String addr2, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Obtiene la dirección default de un usuario
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getDefaultAddress(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.model.Address> $completion) {
        return null;
    }
    
    /**
     * Limpia el cache local
     */
    public final void clearLocalCache() {
    }
}