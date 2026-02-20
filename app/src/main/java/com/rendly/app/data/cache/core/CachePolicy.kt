package com.rendly.app.data.cache.core

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Cache policy configuration for different data types.
 * Defines TTL for memory and disk cache layers.
 * 
 * Design principles:
 * - Frequently changing data (messages, notifications) → short TTL
 * - Semi-static data (profiles, user info) → medium TTL
 * - Static data (categories, config) → long TTL
 */
enum class CachePolicy(
    val memoryTtl: Duration,
    val diskTtl: Duration,
    val staleWhileRevalidate: Boolean = true
) {
    /**
     * Feed posts - moderate refresh rate
     */
    FEED(
        memoryTtl = 30.seconds,
        diskTtl = 5.minutes
    ),

    /**
     * User profile data - cached longer
     */
    PROFILE(
        memoryTtl = 1.minutes,
        diskTtl = 10.minutes
    ),

    /**
     * Chat messages - very short TTL for real-time feel
     */
    MESSAGES(
        memoryTtl = 5.seconds,
        diskTtl = 1.minutes
    ),

    /**
     * Rends (short videos) - similar to feed
     */
    RENDS(
        memoryTtl = 30.seconds,
        diskTtl = 5.minutes
    ),

    /**
     * Stories - short lived content
     */
    STORIES(
        memoryTtl = 15.seconds,
        diskTtl = 2.minutes
    ),

    /**
     * User data - cached longer for performance
     */
    USER_DATA(
        memoryTtl = 2.minutes,
        diskTtl = 1.hours
    ),

    /**
     * Comments - moderate refresh
     */
    COMMENTS(
        memoryTtl = 30.seconds,
        diskTtl = 5.minutes
    ),

    /**
     * Notifications - short TTL
     */
    NOTIFICATIONS(
        memoryTtl = 10.seconds,
        diskTtl = 2.minutes
    ),

    /**
     * Search results - cache briefly
     */
    SEARCH(
        memoryTtl = 1.minutes,
        diskTtl = 5.minutes
    ),

    /**
     * Followers/Following lists
     */
    SOCIAL(
        memoryTtl = 1.minutes,
        diskTtl = 10.minutes
    ),

    /**
     * Static data (categories, configs, etc.)
     */
    STATIC(
        memoryTtl = 1.hours,
        diskTtl = 24.hours,
        staleWhileRevalidate = false
    ),

    /**
     * No caching - always fetch fresh
     */
    NO_CACHE(
        memoryTtl = Duration.ZERO,
        diskTtl = Duration.ZERO,
        staleWhileRevalidate = false
    ),

    /**
     * Aggressive caching - for offline-first features
     */
    OFFLINE_FIRST(
        memoryTtl = 5.minutes,
        diskTtl = (7 * 24).hours // 7 days
    );

    val memoryTtlMs: Long get() = memoryTtl.inWholeMilliseconds
    val diskTtlMs: Long get() = diskTtl.inWholeMilliseconds

    fun isMemoryExpired(cachedAtMs: Long): Boolean {
        return System.currentTimeMillis() - cachedAtMs > memoryTtlMs
    }

    fun isDiskExpired(cachedAtMs: Long): Boolean {
        return System.currentTimeMillis() - cachedAtMs > diskTtlMs
    }
}

/**
 * Cache entry metadata stored alongside data
 */
data class CacheMetadata(
    val cachedAt: Long = System.currentTimeMillis(),
    val version: Long = 0,
    val etag: String? = null,
    val source: CacheSource = CacheSource.NETWORK
) {
    fun isMemoryStale(policy: CachePolicy): Boolean = 
        policy.isMemoryExpired(cachedAt)
    
    fun isDiskStale(policy: CachePolicy): Boolean = 
        policy.isDiskExpired(cachedAt)
}

enum class CacheSource {
    MEMORY,
    DISK,
    NETWORK
}

/**
 * Wrapper for cached data with metadata
 */
data class CachedData<T>(
    val data: T,
    val metadata: CacheMetadata,
    val source: CacheSource
) {
    val isStale: Boolean get() = metadata.source != CacheSource.NETWORK
    val age: Long get() = System.currentTimeMillis() - metadata.cachedAt
}

/**
 * Result type for cache operations
 */
sealed class CacheResult<out T> {
    data class Hit<T>(val data: CachedData<T>) : CacheResult<T>()
    data object Miss : CacheResult<Nothing>()
    data class Stale<T>(val data: CachedData<T>) : CacheResult<T>()
    data class Error(val exception: Throwable) : CacheResult<Nothing>()
}

/**
 * Extension to convert CacheResult to nullable value
 */
fun <T> CacheResult<T>.getOrNull(): T? = when (this) {
    is CacheResult.Hit -> data.data
    is CacheResult.Stale -> data.data
    is CacheResult.Miss -> null
    is CacheResult.Error -> null
}

/**
 * Extension to get data regardless of staleness
 */
fun <T> CacheResult<T>.dataOrNull(): CachedData<T>? = when (this) {
    is CacheResult.Hit -> data
    is CacheResult.Stale -> data
    is CacheResult.Miss -> null
    is CacheResult.Error -> null
}
