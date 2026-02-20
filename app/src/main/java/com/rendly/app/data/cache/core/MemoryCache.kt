package com.rendly.app.data.cache.core

import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

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
class MemoryCache<K : Any, V : Any>(
    private val maxSize: Int = DEFAULT_MAX_SIZE,
    private val defaultTtlMs: Long = DEFAULT_TTL_MS,
    private val tag: String = "MemoryCache"
) {
    companion object {
        private const val DEFAULT_MAX_SIZE = 100
        private const val DEFAULT_TTL_MS = 30_000L // 30 seconds
    }

    private data class CacheEntry<V>(
        val value: V,
        val expiresAt: Long,
        val version: Long = 0
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
    }

    private val cache = object : LruCache<K, CacheEntry<V>>(maxSize) {
        override fun sizeOf(key: K, value: CacheEntry<V>): Int = 1
    }

    private val metadata = ConcurrentHashMap<K, Long>() // key -> insertedAt
    private val mutex = Mutex()

    /**
     * Store a value in cache with optional custom TTL
     */
    suspend fun put(
        key: K,
        value: V,
        ttlMs: Long = defaultTtlMs,
        version: Long = System.currentTimeMillis()
    ) = mutex.withLock {
        val entry = CacheEntry(
            value = value,
            expiresAt = System.currentTimeMillis() + ttlMs,
            version = version
        )
        cache.put(key, entry)
        metadata[key] = System.currentTimeMillis()
        Log.d(tag, "PUT: $key (TTL: ${ttlMs}ms, version: $version)")
    }

    /**
     * Get value from cache. Returns null if not found or expired.
     */
    suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache.get(key) ?: return@withLock null
        
        if (entry.isExpired()) {
            cache.remove(key)
            metadata.remove(key)
            Log.d(tag, "GET: $key - EXPIRED")
            return@withLock null
        }
        
        Log.d(tag, "GET: $key - HIT")
        entry.value
    }

    /**
     * Get value with version info for comparison
     */
    suspend fun getWithVersion(key: K): Pair<V, Long>? = mutex.withLock {
        val entry = cache.get(key) ?: return@withLock null
        
        if (entry.isExpired()) {
            cache.remove(key)
            metadata.remove(key)
            return@withLock null
        }
        
        Pair(entry.value, entry.version)
    }

    /**
     * Check if key exists and is not expired
     */
    suspend fun contains(key: K): Boolean = get(key) != null

    /**
     * Remove specific key from cache
     */
    suspend fun remove(key: K) = mutex.withLock {
        cache.remove(key)
        metadata.remove(key)
        Log.d(tag, "REMOVE: $key")
    }

    /**
     * Remove all keys matching a predicate (pattern-based invalidation)
     */
    suspend fun removeMatching(predicate: (K) -> Boolean) = mutex.withLock {
        val keysToRemove = cache.snapshot().keys.filter(predicate)
        keysToRemove.forEach { key ->
            cache.remove(key)
            metadata.remove(key)
        }
        Log.d(tag, "REMOVE_MATCHING: ${keysToRemove.size} entries")
    }

    /**
     * Clear all entries
     */
    suspend fun clear() = mutex.withLock {
        cache.evictAll()
        metadata.clear()
        Log.d(tag, "CLEARED")
    }

    /**
     * Get current cache size
     */
    fun size(): Int = cache.size()

    /**
     * Get cache statistics for debugging
     */
    fun stats(): CacheStats {
        return CacheStats(
            size = cache.size(),
            maxSize = maxSize,
            hitCount = cache.hitCount(),
            missCount = cache.missCount(),
            evictionCount = cache.evictionCount()
        )
    }

    /**
     * Evict expired entries proactively
     */
    suspend fun evictExpired() = mutex.withLock {
        val now = System.currentTimeMillis()
        val expiredKeys = cache.snapshot().filter { it.value.isExpired() }.keys
        expiredKeys.forEach { key ->
            cache.remove(key)
            metadata.remove(key)
        }
        if (expiredKeys.isNotEmpty()) {
            Log.d(tag, "EVICT_EXPIRED: ${expiredKeys.size} entries")
        }
    }
}

data class CacheStats(
    val size: Int,
    val maxSize: Int,
    val hitCount: Int,
    val missCount: Int,
    val evictionCount: Int
) {
    val hitRate: Float get() = if (hitCount + missCount > 0) {
        hitCount.toFloat() / (hitCount + missCount)
    } else 0f
}

/**
 * Specialized MemoryCache for list data with pagination support
 */
class ListMemoryCache<T : Any>(
    maxSize: Int = 50,
    defaultTtlMs: Long = 30_000L,
    tag: String = "ListMemoryCache"
) {
    private val cache = MemoryCache<String, List<T>>(maxSize, defaultTtlMs, tag)

    suspend fun put(
        cacheKey: String,
        items: List<T>,
        ttlMs: Long = 30_000L,
        version: Long = System.currentTimeMillis()
    ) = cache.put(cacheKey, items, ttlMs, version)

    suspend fun get(cacheKey: String): List<T>? = cache.get(cacheKey)

    suspend fun getWithVersion(cacheKey: String): Pair<List<T>, Long>? = 
        cache.getWithVersion(cacheKey)

    suspend fun append(cacheKey: String, newItems: List<T>, ttlMs: Long = 30_000L) {
        val existing = cache.get(cacheKey) ?: emptyList()
        cache.put(cacheKey, existing + newItems, ttlMs)
    }

    suspend fun prepend(cacheKey: String, newItems: List<T>, ttlMs: Long = 30_000L) {
        val existing = cache.get(cacheKey) ?: emptyList()
        cache.put(cacheKey, newItems + existing, ttlMs)
    }

    suspend fun updateItem(cacheKey: String, predicate: (T) -> Boolean, transform: (T) -> T) {
        val existing = cache.get(cacheKey) ?: return
        val updated = existing.map { if (predicate(it)) transform(it) else it }
        cache.put(cacheKey, updated)
    }

    suspend fun removeItem(cacheKey: String, predicate: (T) -> Boolean) {
        val existing = cache.get(cacheKey) ?: return
        cache.put(cacheKey, existing.filterNot(predicate))
    }

    suspend fun clear() = cache.clear()
    suspend fun remove(cacheKey: String) = cache.remove(cacheKey)
    suspend fun removeMatching(predicate: (String) -> Boolean) = cache.removeMatching(predicate)
}

/**
 * Namespace-aware cache key builder
 */
object CacheKey {
    fun feed(userId: String? = null, page: Int = 0) = 
        "feed:${userId ?: "global"}:$page"
    
    fun profile(userId: String) = "profile:$userId"
    
    fun posts(userId: String, page: Int = 0) = "posts:$userId:$page"
    
    fun rends(page: Int = 0) = "rends:$page"
    
    fun rendsByUser(userId: String, page: Int = 0) = "rends:user:$userId:$page"
    
    fun stories(userId: String? = null) = "stories:${userId ?: "feed"}"
    
    fun messages(conversationId: String, page: Int = 0) = 
        "messages:$conversationId:$page"
    
    fun conversations(userId: String) = "conversations:$userId"
    
    fun notifications(userId: String, page: Int = 0) = 
        "notifications:$userId:$page"
    
    fun user(userId: String) = "user:$userId"
    
    fun followers(userId: String, page: Int = 0) = "followers:$userId:$page"
    
    fun following(userId: String, page: Int = 0) = "following:$userId:$page"
    
    fun search(query: String, type: String = "all") = "search:$type:$query"
    
    fun comments(entityId: String, entityType: String, page: Int = 0) = 
        "comments:$entityType:$entityId:$page"
}
