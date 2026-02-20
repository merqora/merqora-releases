package com.rendly.app.data.cache.network

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Deduplicates concurrent network requests for the same resource.
 * If multiple callers request the same data simultaneously,
 * only one network call is made and all callers receive the result.
 * 
 * Inspired by SWR (stale-while-revalidate) libraries and how
 * Instagram handles concurrent feed requests.
 */
class RequestDeduplicator(
    private val tag: String = "RequestDeduplicator"
) {
    
    private data class InFlightRequest<T>(
        val deferred: CompletableDeferred<T?>,
        val startedAt: Long = System.currentTimeMillis()
    )
    
    private val inFlightRequests = ConcurrentHashMap<String, InFlightRequest<*>>()
    private val mutex = Mutex()
    
    /**
     * Execute a request with deduplication.
     * If a request with the same key is already in flight, wait for its result.
     * Otherwise, execute the request and share the result with any concurrent callers.
     * 
     * @param key Unique identifier for this request
     * @param timeoutMs Maximum time to wait for in-flight request
     * @param request The actual network request to execute
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> dedupe(
        key: String,
        timeoutMs: Long = 30_000L,
        request: suspend () -> T?
    ): T? {
        // Check if request is already in flight
        val existingRequest = inFlightRequests[key] as? InFlightRequest<T>
        if (existingRequest != null) {
            val elapsed = System.currentTimeMillis() - existingRequest.startedAt
            if (elapsed < timeoutMs) {
                Log.d(tag, "[$key] Waiting for in-flight request (elapsed=${elapsed}ms)")
                return try {
                    withTimeout(timeoutMs - elapsed) {
                        existingRequest.deferred.await()
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.w(tag, "[$key] Timeout waiting for in-flight request")
                    // Remove stale request and proceed with new one
                    inFlightRequests.remove(key)
                    executeRequest(key, request)
                }
            } else {
                // Stale in-flight request, remove it
                Log.w(tag, "[$key] Removing stale in-flight request")
                inFlightRequests.remove(key)
            }
        }
        
        return executeRequest(key, request)
    }
    
    private suspend fun <T> executeRequest(
        key: String,
        request: suspend () -> T?
    ): T? {
        val deferred = CompletableDeferred<T?>()
        val inFlight = InFlightRequest(deferred)
        
        // Register this request as in-flight
        inFlightRequests[key] = inFlight
        
        return try {
            Log.d(tag, "[$key] Executing request")
            val result = request()
            deferred.complete(result)
            Log.d(tag, "[$key] Request completed successfully")
            result
        } catch (e: Exception) {
            Log.e(tag, "[$key] Request failed: ${e.message}")
            deferred.completeExceptionally(e)
            throw e
        } finally {
            // Clean up after request completes
            inFlightRequests.remove(key)
        }
    }
    
    /**
     * Cancel an in-flight request
     */
    fun cancel(key: String) {
        val request = inFlightRequests.remove(key)
        request?.deferred?.cancel()
        Log.d(tag, "[$key] Request cancelled")
    }
    
    /**
     * Cancel all in-flight requests
     */
    fun cancelAll() {
        inFlightRequests.forEach { (key, request) ->
            request.deferred.cancel()
            Log.d(tag, "[$key] Request cancelled (cancelAll)")
        }
        inFlightRequests.clear()
    }
    
    /**
     * Get count of in-flight requests
     */
    fun inFlightCount(): Int = inFlightRequests.size
    
    /**
     * Check if a specific request is in flight
     */
    fun isInFlight(key: String): Boolean = inFlightRequests.containsKey(key)
}

/**
 * Global request deduplicator instance
 */
object GlobalRequestDeduplicator {
    private val deduplicator = RequestDeduplicator("GlobalDedup")
    
    suspend fun <T> dedupe(
        key: String,
        timeoutMs: Long = 30_000L,
        request: suspend () -> T?
    ): T? = deduplicator.dedupe(key, timeoutMs, request)
    
    fun cancel(key: String) = deduplicator.cancel(key)
    fun cancelAll() = deduplicator.cancelAll()
    fun inFlightCount() = deduplicator.inFlightCount()
    fun isInFlight(key: String) = deduplicator.isInFlight(key)
}
