package com.rendly.app.data.cache.repository;

import android.util.Log;
import com.rendly.app.data.cache.core.*;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.*;

/**
 * Base class for cache-first repositories implementing stale-while-revalidate pattern.
 *
 * Flow:
 * 1. Emit immediately from Memory Cache (if available)
 * 2. Emit from Disk Cache (Room) if memory miss
 * 3. Fetch from network in background
 * 4. Compare versions and update caches if changed
 * 5. Emit updated data with smooth animation hint
 *
 * Design inspired by Instagram/TikTok caching strategies.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\b&\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\u00020\u0002:\u0001$B\u000f\u0012\b\b\u0002\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\u0093\u0002\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00010\n0\t\"\b\b\u0001\u0010\u0001*\u00020\u00022\u0006\u0010\u000b\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\u001e\u0010\u0010\u001a\u001a\b\u0001\u0012\f\u0012\n\u0012\u0006\u0012\u0004\u0018\u0001H\u00010\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00112$\u0010\u0013\u001a \b\u0001\u0012\u0012\u0012\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u0002H\u0001\u0018\u00010\u00140\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00112$\u0010\u0015\u001a \b\u0001\u0012\u0012\u0012\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u0002H\u0001\u0018\u00010\u00140\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00112(\u0010\u0016\u001a$\b\u0001\u0012\u0004\u0012\u0002H\u0001\u0012\u0004\u0012\u00020\u0018\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00172(\u0010\u001a\u001a$\b\u0001\u0012\u0004\u0012\u0002H\u0001\u0012\u0004\u0012\u00020\u0018\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00172\u0014\b\u0002\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u0002H\u0001\u0012\u0004\u0012\u00020\u00180\u0011H\u0004\u00a2\u0006\u0002\u0010\u001cJ\u00dc\u0001\u0010\u001d\u001a\u0004\u0018\u0001H\u0001\"\b\b\u0001\u0010\u0001*\u00020\u00022\u0006\u0010\u000b\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\u001e\u0010\u0010\u001a\u001a\b\u0001\u0012\f\u0012\n\u0012\u0006\u0012\u0004\u0018\u0001H\u00010\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00112\u001e\u0010\u0013\u001a\u001a\b\u0001\u0012\f\u0012\n\u0012\u0006\u0012\u0004\u0018\u0001H\u00010\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00112\u001e\u0010\u0015\u001a\u001a\b\u0001\u0012\f\u0012\n\u0012\u0006\u0012\u0004\u0018\u0001H\u00010\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u00112\"\u0010\u0016\u001a\u001e\b\u0001\u0012\u0004\u0012\u0002H\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u001e2\"\u0010\u001a\u001a\u001e\b\u0001\u0012\u0004\u0012\u0002H\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u0012\u0012\u0006\u0012\u0004\u0018\u00010\u00020\u001eH\u0084@\u00a2\u0006\u0002\u0010\u001fJ\u000e\u0010 \u001a\u00020\u0019H\u00a6@\u00a2\u0006\u0002\u0010!J\u0016\u0010\"\u001a\u00020\u00192\u0006\u0010\u000b\u001a\u00020\u0004H\u00a6@\u00a2\u0006\u0002\u0010#R\u0014\u0010\u0003\u001a\u00020\u0004X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006%"}, d2 = {"Lcom/rendly/app/data/cache/repository/CacheFirstRepository;", "T", "", "tag", "", "(Ljava/lang/String;)V", "getTag", "()Ljava/lang/String;", "cacheFirst", "Lkotlinx/coroutines/flow/Flow;", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult;", "cacheKey", "policy", "Lcom/rendly/app/data/cache/core/CachePolicy;", "forceRefresh", "", "fetchFromNetwork", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "getFromMemory", "Lcom/rendly/app/data/cache/core/CachedData;", "getFromDisk", "saveToMemory", "Lkotlin/Function3;", "", "", "saveToDisk", "getVersion", "(Ljava/lang/String;Lcom/rendly/app/data/cache/core/CachePolicy;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function1;)Lkotlinx/coroutines/flow/Flow;", "cacheFirstSingle", "Lkotlin/Function2;", "(Ljava/lang/String;Lcom/rendly/app/data/cache/core/CachePolicy;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function2;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "invalidateAllCaches", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "invalidateCache", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "DataResult", "app_debug"})
public abstract class CacheFirstRepository<T extends java.lang.Object> {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String tag = null;
    
    public CacheFirstRepository(@org.jetbrains.annotations.NotNull
    java.lang.String tag) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    protected final java.lang.String getTag() {
        return null;
    }
    
    /**
     * Get data using cache-first strategy with stale-while-revalidate.
     *
     * @param cacheKey Unique key for this data
     * @param policy Cache policy to use
     * @param forceRefresh If true, always fetch from network
     * @param fetchFromNetwork Suspend function to fetch fresh data
     * @param getFromMemory Suspend function to get from memory cache
     * @param getFromDisk Suspend function to get from disk cache
     * @param saveToMemory Suspend function to save to memory cache
     * @param saveToDisk Suspend function to save to disk cache
     * @param getVersion Function to extract version from data for comparison
     */
    @org.jetbrains.annotations.NotNull
    protected final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult<T>> cacheFirst(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CachePolicy policy, boolean forceRefresh, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super T>, ? extends java.lang.Object> fetchFromNetwork, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super com.rendly.app.data.cache.core.CachedData<T>>, ? extends java.lang.Object> getFromMemory, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super com.rendly.app.data.cache.core.CachedData<T>>, ? extends java.lang.Object> getFromDisk, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function3<? super T, ? super java.lang.Long, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> saveToMemory, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function3<? super T, ? super java.lang.Long, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> saveToDisk, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super T, java.lang.Long> getVersion) {
        return null;
    }
    
    /**
     * Simplified cache-first for single items
     */
    @org.jetbrains.annotations.Nullable
    protected final <T extends java.lang.Object>java.lang.Object cacheFirstSingle(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CachePolicy policy, boolean forceRefresh, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super T>, ? extends java.lang.Object> fetchFromNetwork, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super T>, ? extends java.lang.Object> getFromMemory, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super T>, ? extends java.lang.Object> getFromDisk, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super T, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> saveToMemory, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super T, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> saveToDisk, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super T> $completion) {
        return null;
    }
    
    /**
     * Invalidate cache for a specific key
     */
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object invalidateCache(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Invalidate all caches managed by this repository
     */
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object invalidateAllCaches(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    public CacheFirstRepository() {
        super();
    }
    
    /**
     * Result wrapper for cache-first operations
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u0000*\u0006\b\u0001\u0010\u0001 \u00012\u00020\u0002:\u0004\u0004\u0005\u0006\u0007B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0003\u0082\u0001\u0004\b\t\n\u000b\u00a8\u0006\f"}, d2 = {"Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult;", "T", "", "()V", "Empty", "Error", "Loading", "Success", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Empty;", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Error;", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Loading;", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Success;", "app_debug"})
    public static abstract class DataResult<T extends java.lang.Object> {
        
        private DataResult() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0001\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0013\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u00d6\u0003J\t\u0010\b\u001a\u00020\tH\u00d6\u0001J\t\u0010\n\u001a\u00020\u000bH\u00d6\u0001\u00a8\u0006\f"}, d2 = {"Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Empty;", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult;", "", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
        public static final class Empty extends com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult {
            @org.jetbrains.annotations.NotNull
            public static final com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult.Empty INSTANCE = null;
            
            private Empty() {
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
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0001\n\u0000\n\u0002\u0010\u0003\n\u0000\n\u0002\u0010\u0000\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0019\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007J\t\u0010\f\u001a\u00020\u0004H\u00c6\u0003J\u000b\u0010\r\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u001f\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u00042\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0012\u001a\u00020\u0013H\u00d6\u0001J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Error;", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult;", "", "exception", "", "cachedData", "", "(Ljava/lang/Throwable;Ljava/lang/Object;)V", "getCachedData", "()Ljava/lang/Object;", "getException", "()Ljava/lang/Throwable;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
        public static final class Error extends com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult {
            @org.jetbrains.annotations.NotNull
            private final java.lang.Throwable exception = null;
            @org.jetbrains.annotations.Nullable
            private final java.lang.Object cachedData = null;
            
            public Error(@org.jetbrains.annotations.NotNull
            java.lang.Throwable exception, @org.jetbrains.annotations.Nullable
            java.lang.Object cachedData) {
            }
            
            @org.jetbrains.annotations.NotNull
            public final java.lang.Throwable getException() {
                return null;
            }
            
            @org.jetbrains.annotations.Nullable
            public final java.lang.Object getCachedData() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull
            public final java.lang.Throwable component1() {
                return null;
            }
            
            @org.jetbrains.annotations.Nullable
            public final java.lang.Object component2() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull
            public final com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult.Error copy(@org.jetbrains.annotations.NotNull
            java.lang.Throwable exception, @org.jetbrains.annotations.Nullable
            java.lang.Object cachedData) {
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
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0001\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\t\u0010\b\u001a\u00020\u0004H\u00c6\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u0004H\u00c6\u0001J\u0013\u0010\n\u001a\u00020\u00042\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Loading;", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult;", "", "fromCache", "", "(Z)V", "getFromCache", "()Z", "component1", "copy", "equals", "other", "", "hashCode", "", "toString", "", "app_debug"})
        public static final class Loading extends com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult {
            private final boolean fromCache = false;
            
            public Loading(boolean fromCache) {
            }
            
            public final boolean getFromCache() {
                return false;
            }
            
            public Loading() {
            }
            
            public final boolean component1() {
                return false;
            }
            
            @org.jetbrains.annotations.NotNull
            public final com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult.Loading copy(boolean fromCache) {
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
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000e\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u0000*\u0004\b\u0002\u0010\u00012\b\u0012\u0004\u0012\u0002H\u00010\u0002B\u001f\u0012\u0006\u0010\u0003\u001a\u00028\u0002\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u000f\u001a\u00028\u0002H\u00c6\u0003\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0007H\u00c6\u0003J2\u0010\u0012\u001a\b\u0012\u0004\u0012\u00028\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00028\u00022\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001\u00a2\u0006\u0002\u0010\u0013J\u0013\u0010\u0014\u001a\u00020\u00072\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001R\u0013\u0010\u0003\u001a\u00028\u0002\u00a2\u0006\n\n\u0002\u0010\u000b\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u001b"}, d2 = {"Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Success;", "T", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult;", "data", "source", "Lcom/rendly/app/data/cache/core/CacheSource;", "isStale", "", "(Ljava/lang/Object;Lcom/rendly/app/data/cache/core/CacheSource;Z)V", "getData", "()Ljava/lang/Object;", "Ljava/lang/Object;", "()Z", "getSource", "()Lcom/rendly/app/data/cache/core/CacheSource;", "component1", "component2", "component3", "copy", "(Ljava/lang/Object;Lcom/rendly/app/data/cache/core/CacheSource;Z)Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult$Success;", "equals", "other", "", "hashCode", "", "toString", "", "app_debug"})
        public static final class Success<T extends java.lang.Object> extends com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult<T> {
            private final T data = null;
            @org.jetbrains.annotations.NotNull
            private final com.rendly.app.data.cache.core.CacheSource source = null;
            private final boolean isStale = false;
            
            public Success(T data, @org.jetbrains.annotations.NotNull
            com.rendly.app.data.cache.core.CacheSource source, boolean isStale) {
            }
            
            public final T getData() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull
            public final com.rendly.app.data.cache.core.CacheSource getSource() {
                return null;
            }
            
            public final boolean isStale() {
                return false;
            }
            
            public final T component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull
            public final com.rendly.app.data.cache.core.CacheSource component2() {
                return null;
            }
            
            public final boolean component3() {
                return false;
            }
            
            @org.jetbrains.annotations.NotNull
            public final com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult.Success<T> copy(T data, @org.jetbrains.annotations.NotNull
            com.rendly.app.data.cache.core.CacheSource source, boolean isStale) {
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
    }
}