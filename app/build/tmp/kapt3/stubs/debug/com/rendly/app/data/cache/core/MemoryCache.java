package com.rendly.app.data.cache.core;

import android.util.Log;
import android.util.LruCache;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance in-memory cache with TTL support.
 * Thread-safe implementation using LruCache + ConcurrentHashMap for metadata.
 *
 * Design inspired by Instagram/TikTok caching strategies:
 * - LruCache for automatic eviction based on memory pressure
 * - Per-entry TTL for fine-grained expiration control
 * - Pattern-based invalidation for related entities
 * - Namespace support for context isolation (feed, profile, messages, etc.)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 +*\b\b\u0000\u0010\u0001*\u00020\u0002*\b\b\u0001\u0010\u0003*\u00020\u00022\u00020\u0002:\u0002*+B#\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u0012\u001a\u00020\u0005H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00028\u0000H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u000e\u0010\u0018\u001a\u00020\u0019H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0018\u0010\u001a\u001a\u0004\u0018\u00018\u00012\u0006\u0010\u0016\u001a\u00028\u0000H\u0086@\u00a2\u0006\u0002\u0010\u0017J$\u0010\u001b\u001a\u0010\u0012\u0004\u0012\u00028\u0001\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u001c2\u0006\u0010\u0016\u001a\u00028\u0000H\u0086@\u00a2\u0006\u0002\u0010\u0017J2\u0010\u001d\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00028\u00002\u0006\u0010\u001e\u001a\u00028\u00012\b\b\u0002\u0010\u001f\u001a\u00020\u00072\b\b\u0002\u0010 \u001a\u00020\u0007H\u0086@\u00a2\u0006\u0002\u0010!J\u0016\u0010\"\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00028\u0000H\u0086@\u00a2\u0006\u0002\u0010\u0017J\"\u0010#\u001a\u00020\u00052\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00020\u00150%H\u0086@\u00a2\u0006\u0002\u0010&J\u0006\u0010\'\u001a\u00020\u0005J\u0006\u0010(\u001a\u00020)R \u0010\u000b\u001a\u0014\u0012\u0004\u0012\u00028\u0000\u0012\n\u0012\b\u0012\u0004\u0012\u00028\u00010\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00028\u0000\u0012\u0004\u0012\u00020\u00070\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/rendly/app/data/cache/core/MemoryCache;", "K", "", "V", "maxSize", "", "defaultTtlMs", "", "tag", "", "(IJLjava/lang/String;)V", "cache", "Landroid/util/LruCache;", "Lcom/rendly/app/data/cache/core/MemoryCache$CacheEntry;", "metadata", "Ljava/util/concurrent/ConcurrentHashMap;", "mutex", "Lkotlinx/coroutines/sync/Mutex;", "clear", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "contains", "", "key", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "evictExpired", "", "get", "getWithVersion", "Lkotlin/Pair;", "put", "value", "ttlMs", "version", "(Ljava/lang/Object;Ljava/lang/Object;JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "remove", "removeMatching", "predicate", "Lkotlin/Function1;", "(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "size", "stats", "Lcom/rendly/app/data/cache/core/CacheStats;", "CacheEntry", "Companion", "app_debug"})
public final class MemoryCache<K extends java.lang.Object, V extends java.lang.Object> {
    private final int maxSize = 0;
    private final long defaultTtlMs = 0L;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String tag = null;
    private static final int DEFAULT_MAX_SIZE = 100;
    private static final long DEFAULT_TTL_MS = 30000L;
    @org.jetbrains.annotations.NotNull
    private final android.util.LruCache<K, com.rendly.app.data.cache.core.MemoryCache.CacheEntry<V>> cache = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.concurrent.ConcurrentHashMap<K, java.lang.Long> metadata = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.sync.Mutex mutex = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.cache.core.MemoryCache.Companion Companion = null;
    
    public MemoryCache(int maxSize, long defaultTtlMs, @org.jetbrains.annotations.NotNull
    java.lang.String tag) {
        super();
    }
    
    /**
     * Store a value in cache with optional custom TTL
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object put(@org.jetbrains.annotations.NotNull
    K key, @org.jetbrains.annotations.NotNull
    V value, long ttlMs, long version, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Get value from cache. Returns null if not found or expired.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object get(@org.jetbrains.annotations.NotNull
    K key, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super V> $completion) {
        return null;
    }
    
    /**
     * Get value with version info for comparison
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getWithVersion(@org.jetbrains.annotations.NotNull
    K key, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Pair<? extends V, java.lang.Long>> $completion) {
        return null;
    }
    
    /**
     * Check if key exists and is not expired
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object contains(@org.jetbrains.annotations.NotNull
    K key, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Remove specific key from cache
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object remove(@org.jetbrains.annotations.NotNull
    K key, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Remove all keys matching a predicate (pattern-based invalidation)
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object removeMatching(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super K, java.lang.Boolean> predicate, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Clear all entries
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object clear(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Get current cache size
     */
    public final int size() {
        return 0;
    }
    
    /**
     * Get cache statistics for debugging
     */
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheStats stats() {
        return null;
    }
    
    /**
     * Evict expired entries proactively
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object evictExpired(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public MemoryCache() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u0000*\u0004\b\u0002\u0010\u00012\u00020\u0002B\u001f\u0012\u0006\u0010\u0003\u001a\u00028\u0002\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0007J\u000e\u0010\u000e\u001a\u00028\u0002H\u00c6\u0003\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u000f\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0005H\u00c6\u0003J2\u0010\u0011\u001a\b\u0012\u0004\u0012\u00028\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00028\u00022\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010\u0012J\u0013\u0010\u0013\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\u0002H\u00d6\u0003J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001J\u0006\u0010\u0018\u001a\u00020\u0014J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0013\u0010\u0003\u001a\u00028\u0002\u00a2\u0006\n\n\u0002\u0010\f\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\t\u00a8\u0006\u001b"}, d2 = {"Lcom/rendly/app/data/cache/core/MemoryCache$CacheEntry;", "V", "", "value", "expiresAt", "", "version", "(Ljava/lang/Object;JJ)V", "getExpiresAt", "()J", "getValue", "()Ljava/lang/Object;", "Ljava/lang/Object;", "getVersion", "component1", "component2", "component3", "copy", "(Ljava/lang/Object;JJ)Lcom/rendly/app/data/cache/core/MemoryCache$CacheEntry;", "equals", "", "other", "hashCode", "", "isExpired", "toString", "", "app_debug"})
    static final class CacheEntry<V extends java.lang.Object> {
        private final V value = null;
        private final long expiresAt = 0L;
        private final long version = 0L;
        
        public CacheEntry(V value, long expiresAt, long version) {
            super();
        }
        
        public final V getValue() {
            return null;
        }
        
        public final long getExpiresAt() {
            return 0L;
        }
        
        public final long getVersion() {
            return 0L;
        }
        
        public final boolean isExpired() {
            return false;
        }
        
        public final V component1() {
            return null;
        }
        
        public final long component2() {
            return 0L;
        }
        
        public final long component3() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.cache.core.MemoryCache.CacheEntry<V> copy(V value, long expiresAt, long version) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/rendly/app/data/cache/core/MemoryCache$Companion;", "", "()V", "DEFAULT_MAX_SIZE", "", "DEFAULT_TTL_MS", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}