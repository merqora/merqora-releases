package com.rendly.app.data.cache.repository

import android.util.Log
import com.rendly.app.data.cache.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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
abstract class CacheFirstRepository<T : Any>(
    protected val tag: String = "CacheFirstRepository"
) {
    
    /**
     * Result wrapper for cache-first operations
     */
    sealed class DataResult<out T> {
        data class Success<T>(
            val data: T,
            val source: CacheSource,
            val isStale: Boolean = false
        ) : DataResult<T>()
        
        data class Loading(val fromCache: Boolean = false) : DataResult<Nothing>()
        data object Empty : DataResult<Nothing>()
        data class Error(val exception: Throwable, val cachedData: Any? = null) : DataResult<Nothing>()
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
    protected fun <T : Any> cacheFirst(
        cacheKey: String,
        policy: CachePolicy,
        forceRefresh: Boolean = false,
        fetchFromNetwork: suspend () -> T?,
        getFromMemory: suspend () -> CachedData<T>?,
        getFromDisk: suspend () -> CachedData<T>?,
        saveToMemory: suspend (T, Long) -> Unit,
        saveToDisk: suspend (T, Long) -> Unit,
        getVersion: (T) -> Long = { System.currentTimeMillis() }
    ): Flow<DataResult<T>> = flow {
        val startTime = System.currentTimeMillis()
        var emittedFromCache = false
        var cachedVersion: Long? = null
        
        // STEP 1: Try Memory Cache first (fastest)
        if (!forceRefresh) {
            try {
                val memoryCached = getFromMemory()
                if (memoryCached != null) {
                    val isStale = memoryCached.metadata.isMemoryStale(policy)
                    Log.d(tag, "[$cacheKey] MEMORY HIT (stale=$isStale, age=${memoryCached.age}ms)")
                    emit(DataResult.Success(memoryCached.data, CacheSource.MEMORY, isStale))
                    emittedFromCache = true
                    cachedVersion = memoryCached.metadata.version
                    
                    // If not stale and not forcing refresh, we might skip network
                    if (!isStale && !policy.staleWhileRevalidate) {
                        Log.d(tag, "[$cacheKey] Memory cache fresh, skipping network")
                        return@flow
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "[$cacheKey] Memory cache error: ${e.message}")
            }
        }
        
        // STEP 2: Try Disk Cache (Room)
        if (!emittedFromCache && !forceRefresh) {
            try {
                val diskCached = getFromDisk()
                if (diskCached != null) {
                    val isStale = diskCached.metadata.isDiskStale(policy)
                    Log.d(tag, "[$cacheKey] DISK HIT (stale=$isStale, age=${diskCached.age}ms)")
                    emit(DataResult.Success(diskCached.data, CacheSource.DISK, isStale))
                    emittedFromCache = true
                    cachedVersion = diskCached.metadata.version
                    
                    // Warm memory cache
                    try {
                        saveToMemory(diskCached.data, diskCached.metadata.version)
                    } catch (e: Exception) {
                        Log.w(tag, "[$cacheKey] Failed to warm memory cache: ${e.message}")
                    }
                    
                    // If not stale and not forcing refresh, we might skip network
                    if (!isStale && !policy.staleWhileRevalidate) {
                        Log.d(tag, "[$cacheKey] Disk cache fresh, skipping network")
                        return@flow
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "[$cacheKey] Disk cache error: ${e.message}")
            }
        }
        
        // STEP 3: Fetch from Network
        try {
            Log.d(tag, "[$cacheKey] NETWORK FETCH (forceRefresh=$forceRefresh)")
            val networkData = fetchFromNetwork()
            
            if (networkData != null) {
                val newVersion = getVersion(networkData)
                val hasChanged = cachedVersion == null || newVersion != cachedVersion
                
                Log.d(tag, "[$cacheKey] NETWORK SUCCESS (changed=$hasChanged, version=$newVersion)")
                
                // STEP 4: Update caches
                try {
                    saveToDisk(networkData, newVersion)
                    saveToMemory(networkData, newVersion)
                } catch (e: Exception) {
                    Log.w(tag, "[$cacheKey] Failed to update caches: ${e.message}")
                }
                
                // STEP 5: Emit if data changed or we haven't emitted yet
                if (hasChanged || !emittedFromCache) {
                    val elapsed = System.currentTimeMillis() - startTime
                    Log.d(tag, "[$cacheKey] Emitting network data (elapsed=${elapsed}ms)")
                    emit(DataResult.Success(networkData, CacheSource.NETWORK, isStale = false))
                }
            } else if (!emittedFromCache) {
                Log.d(tag, "[$cacheKey] Network returned null, no cache available")
                emit(DataResult.Empty)
            }
            
        } catch (e: Exception) {
            Log.e(tag, "[$cacheKey] NETWORK ERROR: ${e.message}")
            
            // Graceful degradation: if we already emitted from cache, just log
            if (!emittedFromCache) {
                // Try one more time from disk as fallback
                try {
                    val fallback = getFromDisk()
                    if (fallback != null) {
                        Log.d(tag, "[$cacheKey] Using disk fallback after network error")
                        emit(DataResult.Success(fallback.data, CacheSource.DISK, isStale = true))
                    } else {
                        emit(DataResult.Error(e))
                    }
                } catch (diskError: Exception) {
                    emit(DataResult.Error(e))
                }
            }
            // If we already emitted from cache, silently fail network (user has data)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Simplified cache-first for single items
     */
    protected suspend fun <T : Any> cacheFirstSingle(
        cacheKey: String,
        policy: CachePolicy,
        forceRefresh: Boolean = false,
        fetchFromNetwork: suspend () -> T?,
        getFromMemory: suspend () -> T?,
        getFromDisk: suspend () -> T?,
        saveToMemory: suspend (T) -> Unit,
        saveToDisk: suspend (T) -> Unit
    ): T? = withContext(Dispatchers.IO) {
        // Try memory first
        if (!forceRefresh) {
            getFromMemory()?.let { 
                Log.d(tag, "[$cacheKey] Single: MEMORY HIT")
                return@withContext it 
            }
            
            // Try disk
            getFromDisk()?.let { 
                Log.d(tag, "[$cacheKey] Single: DISK HIT")
                try { saveToMemory(it) } catch (_: Exception) {}
                return@withContext it 
            }
        }
        
        // Fetch from network
        try {
            fetchFromNetwork()?.also { data ->
                Log.d(tag, "[$cacheKey] Single: NETWORK SUCCESS")
                try {
                    saveToDisk(data)
                    saveToMemory(data)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(tag, "[$cacheKey] Single: NETWORK ERROR: ${e.message}")
            // Fallback to disk on network error
            getFromDisk()
        }
    }
    
    /**
     * Invalidate cache for a specific key
     */
    abstract suspend fun invalidateCache(cacheKey: String)
    
    /**
     * Invalidate all caches managed by this repository
     */
    abstract suspend fun invalidateAllCaches()
}

/**
 * Extension to collect only successful results
 */
fun <T> Flow<CacheFirstRepository.DataResult<T>>.filterSuccess(): Flow<T> = 
    filterIsInstance<CacheFirstRepository.DataResult.Success<T>>().map { it.data }

/**
 * Extension to get the first successful result
 */
suspend fun <T> Flow<CacheFirstRepository.DataResult<T>>.firstSuccess(): T? =
    filterSuccess().firstOrNull()

/**
 * Extension to map successful results
 */
fun <T, R> Flow<CacheFirstRepository.DataResult<T>>.mapSuccess(
    transform: (T) -> R
): Flow<CacheFirstRepository.DataResult<R>> = map { result ->
    when (result) {
        is CacheFirstRepository.DataResult.Success -> 
            CacheFirstRepository.DataResult.Success(transform(result.data), result.source, result.isStale)
        is CacheFirstRepository.DataResult.Loading -> result
        is CacheFirstRepository.DataResult.Empty -> result
        is CacheFirstRepository.DataResult.Error -> result
    }
}
