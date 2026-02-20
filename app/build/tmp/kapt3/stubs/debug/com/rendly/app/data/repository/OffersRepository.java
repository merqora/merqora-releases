package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.data.model.PostDB;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0007\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0014\u001a\u00020\u0015J\b\u0010\u0016\u001a\u00020\u0015H\u0002J\u0018\u0010\u0017\u001a\u00020\u00152\b\b\u0002\u0010\u0018\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u0019J\u000e\u0010\u001a\u001a\u00020\u00152\u0006\u0010\u001b\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0010\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\n0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u0019\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u000f\u00a8\u0006\u001c"}, d2 = {"Lcom/rendly/app/data/repository/OffersRepository;", "", "()V", "TAG", "", "_campaigns", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/repository/OfferCampaign;", "_isLoading", "", "_selectedCampaign", "campaigns", "Lkotlinx/coroutines/flow/StateFlow;", "getCampaigns", "()Lkotlinx/coroutines/flow/StateFlow;", "isDataLoaded", "isLoading", "selectedCampaign", "getSelectedCampaign", "clearCache", "", "generateFallbackOffers", "loadOffers", "forceRefresh", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "selectCampaign", "campaign", "app_debug"})
public final class OffersRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "OffersRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.OfferCampaign>> _campaigns = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.OfferCampaign>> campaigns = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.repository.OfferCampaign> _selectedCampaign = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.OfferCampaign> selectedCampaign = null;
    private static boolean isDataLoaded = false;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.OffersRepository INSTANCE = null;
    
    private OffersRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.OfferCampaign>> getCampaigns() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.OfferCampaign> getSelectedCampaign() {
        return null;
    }
    
    public final void selectCampaign(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.repository.OfferCampaign campaign) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadOffers(boolean forceRefresh, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Fallback: genera ofertas a partir de los items del ExploreRepository
     * cuando no hay tabla offer_campaigns o está vacía.
     */
    private final void generateFallbackOffers() {
    }
    
    public final void clearCache() {
    }
}