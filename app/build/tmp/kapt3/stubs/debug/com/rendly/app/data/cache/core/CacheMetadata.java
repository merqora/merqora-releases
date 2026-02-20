package com.rendly.app.data.cache.core;

import kotlin.time.Duration;

/**
 * Cache entry metadata stored alongside data
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\b\u0018\u00002\u00020\u0001B/\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0013\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\bH\u00c6\u0003J3\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001J\u000e\u0010\u001b\u001a\u00020\u00172\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010\u001e\u001a\u00020\u00172\u0006\u0010\u001c\u001a\u00020\u001dJ\t\u0010\u001f\u001a\u00020\u0006H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000b\u00a8\u0006 "}, d2 = {"Lcom/rendly/app/data/cache/core/CacheMetadata;", "", "cachedAt", "", "version", "etag", "", "source", "Lcom/rendly/app/data/cache/core/CacheSource;", "(JJLjava/lang/String;Lcom/rendly/app/data/cache/core/CacheSource;)V", "getCachedAt", "()J", "getEtag", "()Ljava/lang/String;", "getSource", "()Lcom/rendly/app/data/cache/core/CacheSource;", "getVersion", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "isDiskStale", "policy", "Lcom/rendly/app/data/cache/core/CachePolicy;", "isMemoryStale", "toString", "app_debug"})
public final class CacheMetadata {
    private final long cachedAt = 0L;
    private final long version = 0L;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String etag = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.cache.core.CacheSource source = null;
    
    public CacheMetadata(long cachedAt, long version, @org.jetbrains.annotations.Nullable
    java.lang.String etag, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CacheSource source) {
        super();
    }
    
    public final long getCachedAt() {
        return 0L;
    }
    
    public final long getVersion() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getEtag() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheSource getSource() {
        return null;
    }
    
    public final boolean isMemoryStale(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CachePolicy policy) {
        return false;
    }
    
    public final boolean isDiskStale(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CachePolicy policy) {
        return false;
    }
    
    public CacheMetadata() {
        super();
    }
    
    public final long component1() {
        return 0L;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheSource component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.core.CacheMetadata copy(long cachedAt, long version, @org.jetbrains.annotations.Nullable
    java.lang.String etag, @org.jetbrains.annotations.NotNull
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