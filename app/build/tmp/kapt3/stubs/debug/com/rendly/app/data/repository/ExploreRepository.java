package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.data.model.PostDB;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\b\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0011\u001a\u00020\u0012J\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00150\u0014J\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\u0006\u0010\u0017\u001a\u00020\u0004J\u0018\u0010\u0018\u001a\u00020\u00122\b\b\u0002\u0010\u0019\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u001aJ\u0010\u0010\u001b\u001a\u00020\u00042\u0006\u0010\u001c\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u000f\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000e\u00a8\u0006\u001d"}, d2 = {"Lcom/rendly/app/data/repository/ExploreRepository;", "", "()V", "TAG", "", "_exploreItems", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/repository/ExploreItem;", "_isLoading", "", "exploreItems", "Lkotlinx/coroutines/flow/StateFlow;", "getExploreItems", "()Lkotlinx/coroutines/flow/StateFlow;", "isDataLoaded", "isLoading", "clearCache", "", "getCategoryCounts", "", "", "getItemsByCategory", "categoryId", "loadExploreItems", "forceRefresh", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "normalizeCategory", "category", "app_debug"})
public final class ExploreRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ExploreRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.ExploreItem>> _exploreItems = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.ExploreItem>> exploreItems = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    private static boolean isDataLoaded = false;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.ExploreRepository INSTANCE = null;
    
    private ExploreRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.ExploreItem>> getExploreItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadExploreItems(boolean forceRefresh, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void clearCache() {
    }
    
    /**
     * Returns a map of category ID -> count of posts for that category
     * Uses the loaded explore items to calculate counts
     */
    @org.jetbrains.annotations.NotNull
    public final java.util.Map<java.lang.String, java.lang.Integer> getCategoryCounts() {
        return null;
    }
    
    /**
     * Returns items filtered by category
     */
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.rendly.app.data.repository.ExploreItem> getItemsByCategory(@org.jetbrains.annotations.NotNull
    java.lang.String categoryId) {
        return null;
    }
    
    /**
     * Normalizes category names to match CategoryDrawer IDs
     * Maps variations of category names to standard IDs
     */
    private final java.lang.String normalizeCategory(java.lang.String category) {
        return null;
    }
}