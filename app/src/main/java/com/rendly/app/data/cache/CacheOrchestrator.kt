package com.rendly.app.data.cache

import android.content.Context
import android.util.Log
import com.rendly.app.data.cache.core.CachePolicy
import com.rendly.app.data.cache.db.MerqoraDatabase
import com.rendly.app.data.cache.repository.CachedRendRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Central orchestrator for cache management across the app.
 * 
 * Responsibilities:
 * - Cache warming on app start
 * - Background sync coordination
 * - Prefetch based on navigation patterns
 * - Global cache invalidation
 * - Monitoring and logging
 * 
 * Design inspired by Instagram's cache management layer.
 */
class CacheOrchestrator private constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "CacheOrchestrator"
        
        @Volatile
        private var INSTANCE: CacheOrchestrator? = null
        
        fun getInstance(context: Context): CacheOrchestrator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CacheOrchestrator(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isWarmingUp = AtomicBoolean(false)
    private val isInitialized = AtomicBoolean(false)
    
    // Repositories
    private val rendRepository by lazy { CachedRendRepository.getInstance(context) }
    
    // Navigation tracking for smart prefetch
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Unknown)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    // Cache health monitoring
    private val _cacheStats = MutableStateFlow(CacheHealthStats())
    val cacheStats: StateFlow<CacheHealthStats> = _cacheStats.asStateFlow()
    
    /**
     * Initialize cache system. Call once at app startup.
     */
    fun initialize() {
        if (isInitialized.getAndSet(true)) {
            Log.d(TAG, "Already initialized")
            return
        }
        
        Log.i(TAG, "Initializing cache orchestrator...")
        
        scope.launch {
            // Warm caches in parallel
            warmAllCaches()
            
            // Start periodic maintenance
            startCacheMaintenance()
            
            // Monitor cache health
            startHealthMonitoring()
        }
    }
    
    /**
     * Warm all caches on app startup.
     * Loads critical data from disk into memory.
     */
    private suspend fun warmAllCaches() {
        if (isWarmingUp.getAndSet(true)) return
        
        Log.i(TAG, "Warming caches...")
        val startTime = System.currentTimeMillis()
        
        try {
            supervisorScope {
                // Warm rends cache (main feed)
                launch { rendRepository.warmCache() }
                
                // Add more cache warming here as repositories are implemented
                // launch { postRepository.warmCache() }
                // launch { storyRepository.warmCache() }
            }
            
            val elapsed = System.currentTimeMillis() - startTime
            Log.i(TAG, "Cache warming completed in ${elapsed}ms")
            
        } catch (e: Exception) {
            Log.e(TAG, "Cache warming failed: ${e.message}")
        } finally {
            isWarmingUp.set(false)
        }
    }
    
    /**
     * Notify screen change for smart prefetching.
     */
    fun onScreenChanged(screen: Screen) {
        _currentScreen.value = screen
        
        scope.launch {
            when (screen) {
                is Screen.RendsFeed -> {
                    // Prefetch next page when entering rends feed
                    rendRepository.prefetchNextPage(0)
                }
                is Screen.Profile -> {
                    // Prefetch user's rends when viewing profile
                    // rendRepository.prefetchUserRends(screen.userId)
                }
                is Screen.Home -> {
                    // Prefetch feed and stories
                }
                else -> { /* No prefetch */ }
            }
        }
    }
    
    /**
     * Handle scroll-based prefetching.
     */
    fun onScrollApproachingEnd(screen: Screen, currentPage: Int) {
        scope.launch {
            when (screen) {
                is Screen.RendsFeed -> {
                    rendRepository.prefetchNextPage(currentPage)
                }
                else -> { /* No prefetch */ }
            }
        }
    }
    
    /**
     * Invalidate all caches. Use on logout or data corruption.
     */
    suspend fun invalidateAllCaches() {
        Log.w(TAG, "Invalidating all caches")
        
        supervisorScope {
            launch { rendRepository.invalidateAllCaches() }
            // Add more repositories as implemented
        }
        
        // Clear database
        MerqoraDatabase.clearAllCaches(context)
        
        Log.i(TAG, "All caches invalidated")
    }
    
    /**
     * Invalidate caches for a specific user.
     * Use when user data changes (profile update, logout of another user, etc.)
     */
    suspend fun invalidateUserCaches(userId: String) {
        Log.d(TAG, "Invalidating caches for user: $userId")
        
        // Invalidate memory caches
        rendRepository.invalidateCache("rends:user:$userId:0")
    }
    
    /**
     * Force refresh all visible data.
     * Use on pull-to-refresh.
     */
    fun forceRefreshCurrentScreen() {
        scope.launch {
            when (val screen = _currentScreen.value) {
                is Screen.RendsFeed -> {
                    rendRepository.getFeed(forceRefresh = true).first()
                }
                is Screen.Profile -> {
                    // Refresh profile data
                }
                else -> { /* No refresh */ }
            }
        }
    }
    
    /**
     * Periodic cache maintenance.
     * Runs every 15 minutes to evict expired entries.
     */
    private fun startCacheMaintenance() {
        scope.launch {
            while (isActive) {
                delay(15 * 60 * 1000L) // 15 minutes
                
                Log.d(TAG, "Running cache maintenance...")
                try {
                    rendRepository.evictExpired()
                    updateCacheStats()
                } catch (e: Exception) {
                    Log.e(TAG, "Cache maintenance failed: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Monitor cache health and log stats.
     */
    private fun startHealthMonitoring() {
        scope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L) // Every 5 minutes
                updateCacheStats()
            }
        }
    }
    
    private suspend fun updateCacheStats() {
        try {
            val database = MerqoraDatabase.getInstance(context)
            val rendCount = database.cachedRendDao().count()
            val postCount = database.cachedPostDao().count()
            val userCount = database.cachedUserDao().count()
            
            _cacheStats.value = CacheHealthStats(
                rendsCached = rendCount,
                postsCached = postCount,
                usersCached = userCount,
                lastUpdated = System.currentTimeMillis()
            )
            
            Log.d(TAG, "Cache stats: rends=$rendCount, posts=$postCount, users=$userCount")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update cache stats: ${e.message}")
        }
    }
    
    /**
     * Cleanup on app termination.
     */
    fun shutdown() {
        Log.i(TAG, "Shutting down cache orchestrator")
        scope.cancel()
    }
}

/**
 * Screen identifiers for navigation-aware prefetching.
 */
sealed class Screen {
    data object Unknown : Screen()
    data object Home : Screen()
    data object RendsFeed : Screen()
    data class Profile(val userId: String) : Screen()
    data class PostDetail(val postId: String) : Screen()
    data class RendDetail(val rendId: String) : Screen()
    data object Messages : Screen()
    data object Notifications : Screen()
    data object Search : Screen()
}

/**
 * Cache health statistics for monitoring.
 */
data class CacheHealthStats(
    val rendsCached: Int = 0,
    val postsCached: Int = 0,
    val usersCached: Int = 0,
    val messagesCached: Int = 0,
    val lastUpdated: Long = 0
) {
    val totalCached: Int get() = rendsCached + postsCached + usersCached + messagesCached
}
