package com.rendly.app.data.cache.core;

import android.util.Log;
import android.util.LruCache;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Specialized MemoryCache for list data with pagination support
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\u00020\u0002B#\u0012\b\b\u0002\u0010\u0003\u001a\u00020\u0004\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ.\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\b2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00028\u00000\f2\b\b\u0002\u0010\u0011\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u0012J\u000e\u0010\u0013\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0015\u001a\n\u0012\u0004\u0012\u00028\u0000\u0018\u00010\f2\u0006\u0010\u000f\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u0016J*\u0010\u0017\u001a\u0016\u0012\n\u0012\b\u0012\u0004\u0012\u00028\u00000\f\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u00182\u0006\u0010\u000f\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u0016J.\u0010\u0019\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\b2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00028\u00000\f2\b\b\u0002\u0010\u0011\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u0012J8\u0010\u001a\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\b2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00028\u00000\f2\b\b\u0002\u0010\u0011\u001a\u00020\u00062\b\b\u0002\u0010\u001c\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0016\u0010\u001e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u0016J*\u0010\u001f\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\b2\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00020\"0!H\u0086@\u00a2\u0006\u0002\u0010#J\"\u0010$\u001a\u00020\u00042\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\"0!H\u0086@\u00a2\u0006\u0002\u0010%J>\u0010&\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\b2\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00020\"0!2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00028\u00000!H\u0086@\u00a2\u0006\u0002\u0010(R \u0010\n\u001a\u0014\u0012\u0004\u0012\u00020\b\u0012\n\u0012\b\u0012\u0004\u0012\u00028\u00000\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/rendly/app/data/cache/core/ListMemoryCache;", "T", "", "maxSize", "", "defaultTtlMs", "", "tag", "", "(IJLjava/lang/String;)V", "cache", "Lcom/rendly/app/data/cache/core/MemoryCache;", "", "append", "", "cacheKey", "newItems", "ttlMs", "(Ljava/lang/String;Ljava/util/List;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clear", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "get", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getWithVersion", "Lkotlin/Pair;", "prepend", "put", "items", "version", "(Ljava/lang/String;Ljava/util/List;JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "remove", "removeItem", "predicate", "Lkotlin/Function1;", "", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "removeMatching", "(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateItem", "transform", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ListMemoryCache<T extends java.lang.Object> {
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.cache.core.MemoryCache<java.lang.String, java.util.List<T>> cache = null;
    
    public ListMemoryCache(int maxSize, long defaultTtlMs, @org.jetbrains.annotations.NotNull
    java.lang.String tag) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object put(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    java.util.List<? extends T> items, long ttlMs, long version, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object get(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<? extends T>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getWithVersion(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Pair<? extends java.util.List<? extends T>, java.lang.Long>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object append(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    java.util.List<? extends T> newItems, long ttlMs, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object prepend(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    java.util.List<? extends T> newItems, long ttlMs, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateItem(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super T, java.lang.Boolean> predicate, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super T, ? extends T> transform, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object removeItem(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super T, java.lang.Boolean> predicate, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object clear(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object remove(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object removeMatching(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, java.lang.Boolean> predicate, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    public ListMemoryCache() {
        super();
    }
}