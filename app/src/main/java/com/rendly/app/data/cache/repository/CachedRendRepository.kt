package com.rendly.app.data.cache.repository

import android.content.Context
import android.util.Log
import com.rendly.app.data.cache.core.*
import com.rendly.app.data.cache.db.*
import com.rendly.app.data.cache.network.SupabaseDataSource
import com.rendly.app.data.model.Rend
import com.rendly.app.data.model.RendDB
import com.rendly.app.data.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Cache-first repository for Rends (short videos).
 * Implements stale-while-revalidate pattern with three cache layers.
 * 
 * Usage:
 * ```kotlin
 * cachedRendRepository.getFeed().collect { result ->
 *     when (result) {
 *         is DataResult.Success -> updateUI(result.data, result.source)
 *         is DataResult.Loading -> showSkeleton()
 *         is DataResult.Empty -> showEmpty()
 *         is DataResult.Error -> showError(result.exception)
 *     }
 * }
 * ```
 */
class CachedRendRepository(
    private val context: Context
) : CacheFirstRepository<List<Rend>>("CachedRendRepo") {
    
    private val database: MerqoraDatabase by lazy { MerqoraDatabase.getInstance(context) }
    private val rendDao: CachedRendDao by lazy { database.cachedRendDao() }
    private val userDao: CachedUserDao by lazy { database.cachedUserDao() }
    private val syncMetadataDao: CacheSyncMetadataDao by lazy { database.cacheSyncMetadataDao() }
    
    // Memory caches
    private val rendListCache = ListMemoryCache<Rend>(
        maxSize = 20,
        defaultTtlMs = CachePolicy.RENDS.memoryTtlMs,
        tag = "RendListCache"
    )
    
    private val rendCache = MemoryCache<String, Rend>(
        maxSize = 100,
        defaultTtlMs = CachePolicy.RENDS.memoryTtlMs,
        tag = "RendCache"
    )
    
    private val userCache = MemoryCache<String, Usuario>(
        maxSize = 200,
        defaultTtlMs = CachePolicy.USER_DATA.memoryTtlMs,
        tag = "UserCache"
    )
    
    // Background scope for cache operations
    private val cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Get rends feed with cache-first strategy.
     * Emits immediately from cache, then updates from network.
     */
    fun getFeed(
        page: Int = 0,
        pageSize: Int = 20,
        forceRefresh: Boolean = false
    ): Flow<DataResult<List<Rend>>> {
        val cacheKey = CacheKey.rends(page)
        val offset = page * pageSize
        
        return cacheFirst(
            cacheKey = cacheKey,
            policy = CachePolicy.RENDS,
            forceRefresh = forceRefresh,
            
            fetchFromNetwork = {
                val rendsWithUsers = SupabaseDataSource.fetchRendsWithUsers(pageSize, offset)
                rendsWithUsers.map { (rendDB, user) ->
                    // Cache user data
                    user?.let { cacheUser(it) }
                    
                    Rend.fromDB(
                        db = rendDB,
                        username = user?.username ?: "usuario",
                        avatarUrl = user?.avatarUrl ?: "",
                        storeName = user?.nombreTienda
                    )
                }
            },
            
            getFromMemory = {
                rendListCache.getWithVersion(cacheKey)?.let { (data, version) ->
                    CachedData(
                        data = data,
                        metadata = CacheMetadata(cachedAt = version, version = version),
                        source = CacheSource.MEMORY
                    )
                }
            },
            
            getFromDisk = {
                getRendsFromDisk(offset, pageSize)
            },
            
            saveToMemory = { data, version ->
                rendListCache.put(cacheKey, data, CachePolicy.RENDS.memoryTtlMs, version)
                // Also cache individual rends
                data.forEach { rend ->
                    rendCache.put(rend.id, rend, CachePolicy.RENDS.memoryTtlMs)
                }
            },
            
            saveToDisk = { data, version ->
                saveRendsToDisk(data, cacheKey, version)
            },
            
            getVersion = { data ->
                // Use the newest rend's timestamp as version
                data.maxOfOrNull { parseTimestamp(it.createdAt) } ?: System.currentTimeMillis()
            }
        )
    }
    
    /**
     * Get rends by user ID with cache-first strategy.
     */
    fun getByUserId(
        userId: String,
        page: Int = 0,
        pageSize: Int = 20,
        forceRefresh: Boolean = false
    ): Flow<DataResult<List<Rend>>> {
        val cacheKey = CacheKey.rendsByUser(userId, page)
        val offset = page * pageSize
        
        return cacheFirst(
            cacheKey = cacheKey,
            policy = CachePolicy.RENDS,
            forceRefresh = forceRefresh,
            
            fetchFromNetwork = {
                val rendsWithUsers = SupabaseDataSource.fetchRendsWithUsers(pageSize, offset, userId)
                rendsWithUsers.map { (rendDB, user) ->
                    user?.let { cacheUser(it) }
                    Rend.fromDB(
                        db = rendDB,
                        username = user?.username ?: "usuario",
                        avatarUrl = user?.avatarUrl ?: "",
                        storeName = user?.nombreTienda
                    )
                }
            },
            
            getFromMemory = {
                rendListCache.getWithVersion(cacheKey)?.let { (data, version) ->
                    CachedData(data, CacheMetadata(cachedAt = version, version = version), CacheSource.MEMORY)
                }
            },
            
            getFromDisk = {
                getRendsByUserFromDisk(userId, offset, pageSize)
            },
            
            saveToMemory = { data, version ->
                rendListCache.put(cacheKey, data, CachePolicy.RENDS.memoryTtlMs, version)
            },
            
            saveToDisk = { data, version ->
                saveRendsToDisk(data, cacheKey, version)
            },
            
            getVersion = { data ->
                data.maxOfOrNull { parseTimestamp(it.createdAt) } ?: System.currentTimeMillis()
            }
        )
    }
    
    /**
     * Get single rend by ID with cache-first strategy.
     */
    suspend fun getById(rendId: String, forceRefresh: Boolean = false): Rend? {
        return cacheFirstSingle(
            cacheKey = "rend:$rendId",
            policy = CachePolicy.RENDS,
            forceRefresh = forceRefresh,
            
            fetchFromNetwork = {
                val rendDB = SupabaseDataSource.fetchRend(rendId) ?: return@cacheFirstSingle null
                val user = SupabaseDataSource.fetchUser(rendDB.userId)
                user?.let { cacheUser(it) }
                
                Rend.fromDB(
                    db = rendDB,
                    username = user?.username ?: "usuario",
                    avatarUrl = user?.avatarUrl ?: "",
                    storeName = user?.nombreTienda
                )
            },
            
            getFromMemory = { rendCache.get(rendId) },
            
            getFromDisk = { getRendFromDisk(rendId) },
            
            saveToMemory = { rend -> rendCache.put(rendId, rend) },
            
            saveToDisk = { rend -> saveRendToDisk(rend) }
        )
    }
    
    /**
     * Observe rends feed reactively from Room.
     * Automatically updates when database changes.
     */
    fun observeFeed(): Flow<List<Rend>> {
        return rendDao.observeFeed()
            .map { entities -> 
                entities.mapNotNull { entity -> entityToRend(entity) }
            }
            .distinctUntilChanged()
    }
    
    /**
     * Observe rends by user ID reactively from Room.
     */
    fun observeByUserId(userId: String): Flow<List<Rend>> {
        return rendDao.observeByUserId(userId)
            .map { entities ->
                entities.mapNotNull { entity -> entityToRend(entity) }
            }
            .distinctUntilChanged()
    }
    
    /**
     * Update like count locally (optimistic update).
     */
    suspend fun updateLikeCount(rendId: String, newCount: Int) {
        // Update memory cache
        rendCache.get(rendId)?.let { rend ->
            rendCache.put(rendId, rend.copy(likesCount = newCount))
        }
        
        // Update disk cache
        rendDao.updateLikesCount(rendId, newCount)
        
        // Invalidate list caches to reflect change
        rendListCache.removeMatching { it.contains("rends:") }
        
        Log.d(tag, "Updated like count for $rendId to $newCount")
    }
    
    /**
     * Prefetch next page of rends in background.
     */
    fun prefetchNextPage(currentPage: Int, pageSize: Int = 20) {
        cacheScope.launch {
            val nextPage = currentPage + 1
            val cacheKey = CacheKey.rends(nextPage)
            
            // Only prefetch if not already cached
            if (rendListCache.get(cacheKey) == null) {
                Log.d(tag, "Prefetching page $nextPage")
                try {
                    val offset = nextPage * pageSize
                    val rendsWithUsers = SupabaseDataSource.fetchRendsWithUsers(pageSize, offset)
                    val rends = rendsWithUsers.map { (rendDB, user) ->
                        user?.let { cacheUser(it) }
                        Rend.fromDB(
                            db = rendDB,
                            username = user?.username ?: "usuario",
                            avatarUrl = user?.avatarUrl ?: "",
                            storeName = user?.nombreTienda
                        )
                    }
                    
                    if (rends.isNotEmpty()) {
                        rendListCache.put(cacheKey, rends, CachePolicy.RENDS.memoryTtlMs)
                        saveRendsToDisk(rends, cacheKey, System.currentTimeMillis())
                        Log.d(tag, "Prefetched ${rends.size} rends for page $nextPage")
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Prefetch failed: ${e.message}")
                }
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // CACHE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════
    
    override suspend fun invalidateCache(cacheKey: String) {
        rendListCache.remove(cacheKey)
        syncMetadataDao.delete(cacheKey)
        Log.d(tag, "Invalidated cache: $cacheKey")
    }
    
    override suspend fun invalidateAllCaches() {
        rendListCache.clear()
        rendCache.clear()
        rendDao.deleteAll()
        syncMetadataDao.deleteMatching("rends:%")
        Log.d(tag, "Invalidated all caches")
    }
    
    /**
     * Warm cache with initial data (call on app start).
     */
    suspend fun warmCache() {
        Log.d(tag, "Warming cache...")
        try {
            // Load from disk first
            val diskRends = rendDao.getFeed(limit = 20, offset = 0)
            if (diskRends.isNotEmpty()) {
                val rends = diskRends.mapNotNull { entityToRend(it) }
                rendListCache.put(CacheKey.rends(0), rends, CachePolicy.RENDS.memoryTtlMs)
                rends.forEach { rend ->
                    rendCache.put(rend.id, rend, CachePolicy.RENDS.memoryTtlMs)
                }
                Log.d(tag, "Warmed cache with ${rends.size} rends from disk")
            }
            
            // Then refresh from network in background
            cacheScope.launch {
                try {
                    getFeed(forceRefresh = false).first()
                } catch (e: Exception) {
                    Log.w(tag, "Background refresh failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Cache warming failed: ${e.message}")
        }
    }
    
    /**
     * Evict expired entries from caches.
     */
    suspend fun evictExpired() {
        val cutoff = System.currentTimeMillis() - CachePolicy.RENDS.diskTtlMs
        rendDao.deleteExpired(cutoff)
        rendListCache.clear() // Simple approach: clear all list caches
        Log.d(tag, "Evicted expired entries")
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    
    private suspend fun getRendsFromDisk(offset: Int, limit: Int): CachedData<List<Rend>>? {
        val entities = rendDao.getFeed(limit, offset)
        if (entities.isEmpty()) return null
        
        val rends = entities.mapNotNull { entityToRend(it) }
        if (rends.isEmpty()) return null
        
        val oldestCachedAt = entities.minOf { it.cachedAt }
        val latestVersion = entities.maxOf { it.version }
        
        return CachedData(
            data = rends,
            metadata = CacheMetadata(cachedAt = oldestCachedAt, version = latestVersion),
            source = CacheSource.DISK
        )
    }
    
    private suspend fun getRendsByUserFromDisk(userId: String, offset: Int, limit: Int): CachedData<List<Rend>>? {
        val entities = rendDao.getByUserId(userId, limit, offset)
        if (entities.isEmpty()) return null
        
        val rends = entities.mapNotNull { entityToRend(it) }
        if (rends.isEmpty()) return null
        
        val oldestCachedAt = entities.minOf { it.cachedAt }
        val latestVersion = entities.maxOf { it.version }
        
        return CachedData(
            data = rends,
            metadata = CacheMetadata(cachedAt = oldestCachedAt, version = latestVersion),
            source = CacheSource.DISK
        )
    }
    
    private suspend fun getRendFromDisk(rendId: String): Rend? {
        val entity = rendDao.getById(rendId) ?: return null
        return entityToRend(entity)
    }
    
    private suspend fun saveRendsToDisk(rends: List<Rend>, cacheKey: String, version: Long) {
        val now = System.currentTimeMillis()
        val entities = rends.map { rend -> rendToEntity(rend, now, version) }
        rendDao.upsertAll(entities)
        
        syncMetadataDao.upsert(CacheSyncMetadataEntity(
            cacheKey = cacheKey,
            lastSyncAt = now,
            lastVersion = version
        ))
    }
    
    private suspend fun saveRendToDisk(rend: Rend) {
        val now = System.currentTimeMillis()
        rendDao.upsert(rendToEntity(rend, now, now))
    }
    
    private suspend fun entityToRend(entity: CachedRendEntity): Rend? {
        // Get user data from cache or database
        val user = userCache.get(entity.userId)
            ?: userDao.getById(entity.userId)?.let { userEntity ->
                Usuario(
                    userId = userEntity.userId,
                    username = userEntity.username,
                    nombreTienda = userEntity.nombreTienda,
                    avatarUrl = userEntity.avatarUrl
                ).also { userCache.put(entity.userId, it) }
            }
        
        return Rend(
            id = entity.id,
            userId = entity.userId,
            title = entity.title,
            description = entity.description,
            videoUrl = entity.videoUrl,
            thumbnailUrl = entity.thumbnailUrl,
            productTitle = entity.productTitle,
            productPrice = entity.productPrice,
            productLink = entity.productLink,
            productImage = entity.productImage,
            productId = entity.productId,
            duration = entity.duration,
            likesCount = entity.likesCount,
            reviewsCount = entity.reviewsCount,
            viewsCount = entity.viewsCount,
            sharesCount = entity.sharesCount,
            savesCount = entity.savesCount,
            username = user?.username ?: "usuario",
            userAvatar = user?.avatarUrl ?: "",
            userStoreName = user?.nombreTienda
        )
    }
    
    private fun rendToEntity(rend: Rend, cachedAt: Long, version: Long): CachedRendEntity {
        return CachedRendEntity(
            id = rend.id,
            userId = rend.userId,
            title = rend.title,
            description = rend.description,
            videoUrl = rend.videoUrl,
            thumbnailUrl = rend.thumbnailUrl,
            productTitle = rend.productTitle,
            productPrice = rend.productPrice,
            productLink = rend.productLink,
            productImage = rend.productImage,
            productId = rend.productId,
            duration = rend.duration,
            likesCount = rend.likesCount,
            reviewsCount = rend.reviewsCount,
            viewsCount = rend.viewsCount,
            sharesCount = rend.sharesCount,
            savesCount = rend.savesCount,
            createdAt = rend.createdAt,
            cachedAt = cachedAt,
            version = version
        )
    }
    
    private suspend fun cacheUser(user: Usuario) {
        userCache.put(user.userId, user, CachePolicy.USER_DATA.memoryTtlMs)
        userDao.upsert(CachedUserEntity(
            userId = user.userId,
            username = user.username,
            nombreTienda = user.nombreTienda,
            avatarUrl = user.avatarUrl,
            isVerified = user.isVerified,
            tieneTienda = user.tieneTienda
        ))
    }
    
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: CachedRendRepository? = null
        
        fun getInstance(context: Context): CachedRendRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CachedRendRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
