package com.rendly.app.data.cache.core;

import kotlin.time.Duration;

/**
 * Wrapper for cached data with metadata
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\r\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u0000*\u0004\b\u0000\u0010\u00012\u00020\u0002B\u001d\u0012\u0006\u0010\u0003\u001a\u00028\u0000\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0017\u001a\u00028\u0000H\u00c6\u0003\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0007H\u00c6\u0003J2\u0010\u001a\u001a\b\u0012\u0004\u0012\u00028\u00000\u00002\b\b\u0002\u0010\u0003\u001a\u00028\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001\u00a2\u0006\u0002\u0010\u001bJ\u0013\u0010\u001c\u001a\u00020\u00112\b\u0010\u001d\u001a\u0004\u0018\u00010\u0002H\u00d6\u0003J\t\u0010\u001e\u001a\u00020\u001fH\u00d6\u0001J\t\u0010 \u001a\u00020!H\u00d6\u0001R\u0011\u0010\t\u001a\u00020\n8F\u00a2\u0006\u0006\u001a\u0004\b\u000b\u0010\fR\u0013\u0010\u0003\u001a\u00028\u0000\u00a2\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0010\u001a\u00020\u00118F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0012R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006\""}, d2 = {"Lcom/rendly/app/data/cache/core/CachedData;", "T", "", "data", "metadata", "Lcom/rendly/app/data/cache/core/CacheMetadata;", "source", "Lcom/rendly/app/data/cache/core/CacheSource;", "(Ljava/lang/Object;Lcom/rendly/app/data/cache/core/CacheMetadata;Lcom/rendly/app/data/cache/core/CacheSource;)V", "age", "", "getAge", "()J", "getData", "()Ljava/lang/Object;", "Ljava/lang/Object;", "isStale", "", "()Z", "getMetadata", "()Lcom/rendly/app/data/cache/core/CacheMetadata;", "getSource", "()Lcom/rendly/app/data/cache/core/CacheSource;", "component1", "component2", "component3", "copy", "(Ljava/lang/Object;Lcom/rendly/app/data/cache/core/CacheMetadata;Lcom/rendly/app/data/cache/core/CacheSource;)Lcom/rendly/app/data/cache/core/CachedData;", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
public final class CachedData<T extends java.lang.Object> {
    private final T data = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.cache.core.CacheMetadata metadata = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.cache.core.CacheSource source = null;
    
    public CachedData(T data, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CacheMetadata metadata, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CacheSource source) {
        super();
    }
    
    public final T getData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheMetadata getMetadata() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheSource getSource() {
        return null;
    }
    
    public final boolean isStale() {
        return false;
    }
    
    public final long getAge() {
        return 0L;
    }
    
    public final T component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheMetadata component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheSource component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CachedData<T> copy(T data, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CacheMetadata metadata, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CacheSource source) {
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