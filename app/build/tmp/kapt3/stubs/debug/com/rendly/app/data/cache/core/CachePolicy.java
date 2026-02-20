package com.rendly.app.data.cache.core;

import kotlin.time.Duration;

/**
 * Cache policy configuration for different data types.
 * Defines TTL for memory and disk cache layers.
 *
 * Design principles:
 * - Frequently changing data (messages, notifications) → short TTL
 * - Semi-static data (profiles, user info) → medium TTL
 * - Static data (categories, config) → long TTL
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0017\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B!\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u000e\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\fJ\u000e\u0010\u0015\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\fR\u0019\u0010\u0004\u001a\u00020\u0003\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\n\u001a\u0004\b\b\u0010\tR\u0011\u0010\u000b\u001a\u00020\f8F\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\tR\u0019\u0010\u0002\u001a\u00020\u0003\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\n\u001a\u0004\b\u000e\u0010\tR\u0011\u0010\u000f\u001a\u00020\f8F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012j\u0002\b\u0016j\u0002\b\u0017j\u0002\b\u0018j\u0002\b\u0019j\u0002\b\u001aj\u0002\b\u001bj\u0002\b\u001cj\u0002\b\u001dj\u0002\b\u001ej\u0002\b\u001fj\u0002\b j\u0002\b!j\u0002\b\"\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u0006#"}, d2 = {"Lcom/rendly/app/data/cache/core/CachePolicy;", "", "memoryTtl", "Lkotlin/time/Duration;", "diskTtl", "staleWhileRevalidate", "", "(Ljava/lang/String;IJJZ)V", "getDiskTtl-UwyO8pc", "()J", "J", "diskTtlMs", "", "getDiskTtlMs", "getMemoryTtl-UwyO8pc", "memoryTtlMs", "getMemoryTtlMs", "getStaleWhileRevalidate", "()Z", "isDiskExpired", "cachedAtMs", "isMemoryExpired", "FEED", "PROFILE", "MESSAGES", "RENDS", "STORIES", "USER_DATA", "COMMENTS", "NOTIFICATIONS", "SEARCH", "SOCIAL", "STATIC", "NO_CACHE", "OFFLINE_FIRST", "app_debug"})
public enum CachePolicy {
    /*public static final*/ FEED /* = new FEED(0L, 0L, false) */,
    /*public static final*/ PROFILE /* = new PROFILE(0L, 0L, false) */,
    /*public static final*/ MESSAGES /* = new MESSAGES(0L, 0L, false) */,
    /*public static final*/ RENDS /* = new RENDS(0L, 0L, false) */,
    /*public static final*/ STORIES /* = new STORIES(0L, 0L, false) */,
    /*public static final*/ USER_DATA /* = new USER_DATA(0L, 0L, false) */,
    /*public static final*/ COMMENTS /* = new COMMENTS(0L, 0L, false) */,
    /*public static final*/ NOTIFICATIONS /* = new NOTIFICATIONS(0L, 0L, false) */,
    /*public static final*/ SEARCH /* = new SEARCH(0L, 0L, false) */,
    /*public static final*/ SOCIAL /* = new SOCIAL(0L, 0L, false) */,
    /*public static final*/ STATIC /* = new STATIC(0L, 0L, false) */,
    /*public static final*/ NO_CACHE /* = new NO_CACHE(0L, 0L, false) */,
    /*public static final*/ OFFLINE_FIRST /* = new OFFLINE_FIRST(0L, 0L, false) */;
    private final long memoryTtl = 0L;
    private final long diskTtl = 0L;
    private final boolean staleWhileRevalidate = false;
    
    CachePolicy(long memoryTtl, long diskTtl, boolean staleWhileRevalidate) {
    }
    
    public final boolean getStaleWhileRevalidate() {
        return false;
    }
    
    public final long getMemoryTtlMs() {
        return 0L;
    }
    
    public final long getDiskTtlMs() {
        return 0L;
    }
    
    public final boolean isMemoryExpired(long cachedAtMs) {
        return false;
    }
    
    public final boolean isDiskExpired(long cachedAtMs) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.cache.core.CachePolicy> getEntries() {
        return null;
    }
}