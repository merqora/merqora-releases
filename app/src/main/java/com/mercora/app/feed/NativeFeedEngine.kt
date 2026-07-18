package com.mercora.app.feed

import android.util.Log

/**
 * JNI bridge to the C++ Feed Engine v2.
 *
 * v2 capabilities:
 *   - 6-factor multiplicative scoring (affinity Ã— quality Ã— recency Ã— intent Ã— diversity Ã— exploration)
 *   - User profile with 64-dim embeddings + type affinities
 *   - Session tracking: dwell time, scroll speed, video completion, fatigue
 *   - Thompson Sampling bandit for explore/exploit
 *   - Anti-manipulation: spike detection, credibility scoring
 *   - Author dedup, category spread, anti-clustering
 *
 * All heavy computation in C++. Kotlin only passes primitive arrays.
 */
object NativeFeedEngine {

    private const val TAG = "NativeFeedEngine"
    private var isLoaded = false

    init {
        try {
            System.loadLibrary("vinzay_media_optimizer")
            isLoaded = true
            Log.i(TAG, "Native feed engine v2 library loaded")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library: ${e.message}")
            isLoaded = false
        }
    }

    fun isAvailable(): Boolean = isLoaded

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // LIFECYCLE
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    fun initialize() {
        if (!isLoaded) return
        try { nativeInit() } catch (e: Exception) { Log.e(TAG, "Init: ${e.message}") }
    }

    fun resetSession() {
        if (!isLoaded) return
        try { nativeResetSession() } catch (e: Exception) { Log.e(TAG, "Reset: ${e.message}") }
    }

    fun destroy() {
        if (!isLoaded) return
        try { nativeDestroy() } catch (e: Exception) { Log.e(TAG, "Destroy: ${e.message}") }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // POOL MANAGEMENT (v2: expanded with quality, credibility, author)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    fun clearPool() {
        if (!isLoaded) return
        try { nativeClearPool() } catch (e: Exception) { Log.e(TAG, "ClearPool: ${e.message}") }
    }

    fun addItems(
        indices: IntArray,
        types: IntArray,
        timestamps: LongArray,
        likes: IntArray,
        views: IntArray,
        shares: IntArray,
        saves: IntArray = IntArray(indices.size),
        comments: IntArray = IntArray(indices.size),
        authorIds: IntArray = IntArray(indices.size),
        categoryIds: IntArray = IntArray(indices.size),
        quality: FloatArray = FloatArray(indices.size),
        credibility: FloatArray = FloatArray(indices.size) { 1.0f },
        completionRate: FloatArray = FloatArray(indices.size)
    ) {
        if (!isLoaded || indices.isEmpty()) return
        try {
            nativeAddItems(
                indices, types, timestamps, likes, views, shares,
                saves, comments, authorIds, categoryIds,
                quality, credibility, completionRate, indices.size
            )
        } catch (e: Exception) { Log.e(TAG, "AddItems: ${e.message}") }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // FEED GENERATION
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    fun generateInitialBatch(): IntArray {
        if (!isLoaded) return intArrayOf()
        return try { nativeGenerateInitialBatch() } catch (e: Exception) {
            Log.e(TAG, "GenInitial: ${e.message}"); intArrayOf()
        }
    }

    fun generateNextBatch(): IntArray {
        if (!isLoaded) return intArrayOf()
        return try { nativeGenerateNextBatch() } catch (e: Exception) {
            Log.e(TAG, "GenNext: ${e.message}"); intArrayOf()
        }
    }

    fun getPrefetchHints(): IntArray {
        if (!isLoaded) return intArrayOf()
        return try { nativeGetPrefetchHints() } catch (e: Exception) {
            Log.e(TAG, "Prefetch: ${e.message}"); intArrayOf()
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // USER PROFILE
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    fun setUserEmbedding(embedding: FloatArray) {
        if (!isLoaded) return
        try { nativeSetUserEmbedding(embedding) } catch (e: Exception) {
            Log.e(TAG, "SetEmbed: ${e.message}")
        }
    }

    fun setUserTypeAffinity(affinities: FloatArray) {
        if (!isLoaded) return
        try { nativeSetUserTypeAffinity(affinities) } catch (e: Exception) {
            Log.e(TAG, "SetAffinity: ${e.message}")
        }
    }

    fun setUserStats(avgDwell: Float, avgCompletion: Float, sessionCount: Float, interactionRate: Float) {
        if (!isLoaded) return
        try { nativeSetUserStats(avgDwell, avgCompletion, sessionCount, interactionRate) } catch (e: Exception) {
            Log.e(TAG, "SetStats: ${e.message}")
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // SESSION TRACKING
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    fun startSession() {
        if (!isLoaded) return
        try { nativeStartSession() } catch (e: Exception) { Log.e(TAG, "StartSession: ${e.message}") }
    }

    fun reportDwell(sourceIndex: Int, dwellMs: Float, interacted: Boolean) {
        if (!isLoaded) return
        try { nativeReportDwell(sourceIndex, dwellMs, interacted) } catch (e: Exception) {
            Log.e(TAG, "Dwell: ${e.message}")
        }
    }

    fun reportScrollSpeed(pxPerSec: Float) {
        if (!isLoaded) return
        try { nativeReportScrollSpeed(pxPerSec) } catch (e: Exception) {
            Log.e(TAG, "Scroll: ${e.message}")
        }
    }

    fun reportVideoCompletion(sourceIndex: Int, completionRate: Float) {
        if (!isLoaded) return
        try { nativeReportVideoCompletion(sourceIndex, completionRate) } catch (e: Exception) {
            Log.e(TAG, "Completion: ${e.message}")
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // CONFIG (A/B testing, backend-driven)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    fun setTypeWeight(type: FeedItemType, weight: Float) {
        if (!isLoaded) return
        try { nativeSetTypeWeight(type.ordinal, weight) } catch (e: Exception) {
            Log.e(TAG, "TypeWeight: ${e.message}")
        }
    }

    fun setFactorPowers(
        affinity: Float, quality: Float, recency: Float,
        sessionIntent: Float, diversity: Float, exploration: Float
    ) {
        if (!isLoaded) return
        try { nativeSetFactorPowers(affinity, quality, recency, sessionIntent, diversity, exploration) } catch (e: Exception) {
            Log.e(TAG, "FactorPowers: ${e.message}")
        }
    }

    fun setIntervals(suggestedInterval: Int, specialInterval: Int) {
        if (!isLoaded) return
        try { nativeSetIntervals(suggestedInterval, specialInterval) } catch (e: Exception) {
            Log.e(TAG, "Intervals: ${e.message}")
        }
    }

    fun setExplorationRate(rate: Float) {
        if (!isLoaded) return
        try { nativeSetExplorationRate(rate) } catch (e: Exception) {
            Log.e(TAG, "ExplRate: ${e.message}")
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // STATE
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    fun getTotalServed(): Int {
        if (!isLoaded) return 0
        return try { nativeGetTotalServed() } catch (e: Exception) { 0 }
    }

    fun getAvailableCount(): Int {
        if (!isLoaded) return 0
        return try { nativeGetAvailableCount() } catch (e: Exception) { 0 }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // DEBUG
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    /**
     * Returns [affinity, quality, recency, sessionIntent, diversity, exploration, antiManip]
     */
    fun getScoreBreakdown(sourceIndex: Int): FloatArray {
        if (!isLoaded) return FloatArray(7)
        return try { nativeGetScoreBreakdown(sourceIndex) } catch (e: Exception) { FloatArray(7) }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // NATIVE METHODS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private external fun nativeInit()
    private external fun nativeResetSession()
    private external fun nativeDestroy()
    private external fun nativeClearPool()
    private external fun nativeAddItems(
        indices: IntArray, types: IntArray, timestamps: LongArray,
        likes: IntArray, views: IntArray, shares: IntArray,
        saves: IntArray, comments: IntArray, authorIds: IntArray,
        categoryIds: IntArray, quality: FloatArray, credibility: FloatArray,
        completionRate: FloatArray, count: Int
    )
    private external fun nativeGenerateInitialBatch(): IntArray
    private external fun nativeGenerateNextBatch(): IntArray
    private external fun nativeGetPrefetchHints(): IntArray
    private external fun nativeSetUserEmbedding(embedding: FloatArray)
    private external fun nativeSetUserTypeAffinity(affinities: FloatArray)
    private external fun nativeSetUserStats(avgDwell: Float, avgCompletion: Float, sessionCount: Float, interactionRate: Float)
    private external fun nativeStartSession()
    private external fun nativeReportDwell(sourceIndex: Int, dwellMs: Float, interacted: Boolean)
    private external fun nativeReportScrollSpeed(pxPerSec: Float)
    private external fun nativeReportVideoCompletion(sourceIndex: Int, completionRate: Float)
    private external fun nativeSetTypeWeight(type: Int, weight: Float)
    private external fun nativeSetFactorPowers(affinity: Float, quality: Float, recency: Float, sessionIntent: Float, diversity: Float, exploration: Float)
    private external fun nativeSetIntervals(suggestedInterval: Int, specialInterval: Int)
    private external fun nativeSetExplorationRate(rate: Float)
    private external fun nativeGetTotalServed(): Int
    private external fun nativeGetAvailableCount(): Int
    private external fun nativeGetScoreBreakdown(sourceIndex: Int): FloatArray
}
