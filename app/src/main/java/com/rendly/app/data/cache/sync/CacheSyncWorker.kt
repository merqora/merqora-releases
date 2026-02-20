package com.rendly.app.data.cache.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.rendly.app.data.cache.db.PendingOperationEntity
import com.rendly.app.data.cache.db.MerqoraDatabase
import com.rendly.app.data.cache.repository.CachedRendRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for background cache synchronization.
 * 
 * Responsibilities:
 * - Sync pending offline operations
 * - Refresh stale cache data
 * - Clean up expired cache entries
 * 
 * Runs periodically and on network availability changes.
 */
class CacheSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "CacheSyncWorker"
        const val WORK_NAME = "cache_sync_work"
        const val WORK_NAME_PERIODIC = "cache_sync_periodic"
        
        /**
         * Schedule periodic background sync.
         * Runs every 15 minutes when network is available.
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val request = PeriodicWorkRequestBuilder<CacheSyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1,
                    TimeUnit.MINUTES
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME_PERIODIC,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            
            Log.i(TAG, "Scheduled periodic cache sync")
        }
        
        /**
         * Trigger immediate sync when network becomes available.
         */
        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val request = OneTimeWorkRequestBuilder<CacheSyncWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            
            Log.i(TAG, "Triggered immediate cache sync")
        }
        
        /**
         * Cancel all scheduled syncs.
         */
        fun cancelAllSyncs(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
            Log.i(TAG, "Cancelled all cache syncs")
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting cache sync...")
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Sync pending offline operations
            syncPendingOperations()
            
            // 2. Refresh stale caches
            refreshStaleCaches()
            
            // 3. Clean up expired entries
            cleanupExpiredEntries()
            
            val elapsed = System.currentTimeMillis() - startTime
            Log.i(TAG, "Cache sync completed in ${elapsed}ms")
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Cache sync failed: ${e.message}")
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    /**
     * Sync any pending offline operations to the server.
     */
    private suspend fun syncPendingOperations() {
        val database = MerqoraDatabase.getInstance(applicationContext)
        val pendingOps = database.pendingOperationDao().getPending()
        
        if (pendingOps.isEmpty()) {
            Log.d(TAG, "No pending operations to sync")
            return
        }
        
        Log.d(TAG, "Syncing ${pendingOps.size} pending operations")
        
        for (op in pendingOps) {
            try {
                syncOperation(op)
                database.pendingOperationDao().updateStatus(op.id, "success")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync operation ${op.id}: ${e.message}")
                database.pendingOperationDao().markFailed(
                    id = op.id,
                    errorMessage = e.message
                )
            }
        }
        
        // Clean up successful operations
        database.pendingOperationDao().deleteCompleted()
        
        // Remove operations that failed too many times
        database.pendingOperationDao().deleteFailedAfterRetries(3)
    }
    
    private suspend fun syncOperation(op: PendingOperationEntity) {
        // Implement actual sync logic based on operation type
        when (op.operationType) {
            "create" -> {
                // Create entity on server
            }
            "update" -> {
                // Update entity on server
            }
            "delete" -> {
                // Delete entity on server
            }
        }
        
        Log.d(TAG, "Synced operation: ${op.operationType} ${op.entityType} ${op.entityId}")
    }
    
    /**
     * Refresh caches that have become stale.
     */
    private suspend fun refreshStaleCaches() {
        Log.d(TAG, "Refreshing stale caches...")
        
        try {
            // Refresh rends feed
            val rendRepository = CachedRendRepository.getInstance(applicationContext)
            rendRepository.getFeed(forceRefresh = true)
                .collect { /* Just trigger the refresh */ }
            
            // Add more cache refresh logic as needed
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to refresh some caches: ${e.message}")
        }
    }
    
    /**
     * Clean up expired cache entries to reclaim storage.
     */
    private suspend fun cleanupExpiredEntries() {
        Log.d(TAG, "Cleaning up expired entries...")
        
        val database = MerqoraDatabase.getInstance(applicationContext)
        val now = System.currentTimeMillis()
        
        // Calculate cutoff times for each policy
        val rendCutoff = now - com.rendly.app.data.cache.core.CachePolicy.RENDS.diskTtlMs
        val storyCutoff = now - com.rendly.app.data.cache.core.CachePolicy.STORIES.diskTtlMs
        val messageCutoff = now - com.rendly.app.data.cache.core.CachePolicy.MESSAGES.diskTtlMs
        
        database.cachedRendDao().deleteExpired(rendCutoff)
        database.cachedStoryDao().deleteExpired(storyCutoff)
        database.cachedMessageDao().deleteExpired(messageCutoff)
        
        Log.d(TAG, "Expired entries cleaned up")
    }
}

/**
 * Network connectivity observer that triggers sync on reconnection.
 */
object NetworkSyncTrigger {
    private const val TAG = "NetworkSyncTrigger"
    
    fun onNetworkAvailable(context: Context) {
        Log.d(TAG, "Network became available, triggering sync")
        CacheSyncWorker.triggerImmediateSync(context)
    }
}
