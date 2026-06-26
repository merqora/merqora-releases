package com.rendly.app.feed

import android.util.Log
import com.rendly.app.data.model.Post
import com.rendly.app.data.model.Rend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.OffsetDateTime

/**
 * ═══════════════════════════════════════════════════════════════
 * FEED MANAGER - Orchestrates between data layer and C++ engine
 * ═══════════════════════════════════════════════════════════════
 * 
 * Responsibilities:
 * - Receives raw posts/rends from Supabase
 * - Converts to engine-compatible arrays (minimal JNI overhead)
 * - Calls C++ engine for batch generation
 * - Maps engine output indices back to typed FeedItem objects
 * - Manages feed state with Kotlin Flow
 * - Handles fallback when native engine unavailable
 * 
 * Architecture:
 * [Supabase Data] → [FeedManager] → [C++ Engine] → [Ordered Indices] → [FeedItem list]
 */
object FeedManager {

    private const val TAG = "FeedManager"
    private const val BATCH_SIZE = 4

    // ═══════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════

    private val _feedItems = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems: StateFlow<List<FeedItem>> = _feedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    // Content pools (source data)
    private var posts: List<Post> = emptyList()
    private var rends: List<Rend> = emptyList()

    // Mapping: sourceIndex → (type, dataIndex)
    // sourceIndex is the global index passed to C++ engine
    // dataIndex is the index into posts[] or rends[] arrays
    private data class SourceMapping(val type: FeedItemType, val dataIndex: Int)
    private var sourceMap: MutableList<SourceMapping> = mutableListOf()

    // Suggested accounts slot counter
    private var suggestedSlotIndex = 0

    private var isInitialized = false

    // ═══════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════

    fun initialize() {
        NativeFeedEngine.initialize()
        isInitialized = true
        Log.i(TAG, "FeedManager initialized (native=${NativeFeedEngine.isAvailable()})")
    }

    // ═══════════════════════════════════════════════════════════
    // CONTENT LOADING
    // ═══════════════════════════════════════════════════════════

    /**
     * Load content into the engine pool.
     * Call this after fetching posts and rends from Supabase.
     */
    suspend fun loadContent(newPosts: List<Post>, newRends: List<Rend>) = withContext(Dispatchers.Default) {
        _isLoading.value = true

        posts = newPosts
        rends = newRends
        sourceMap.clear()
        suggestedSlotIndex = 0

        Log.d(TAG, "Loading content: ${posts.size} posts, ${rends.size} rends")

        if (NativeFeedEngine.isAvailable()) {
            loadIntoNativeEngine()
        }

        _isLoading.value = false
    }

    private fun loadIntoNativeEngine() {
        NativeFeedEngine.resetSession()
        NativeFeedEngine.clearPool()
        sourceMap.clear()

        // Build source map and v2 arrays for JNI
        val totalItems = posts.size + rends.size + 2 // +2 for suggested/special slots
        val indices        = IntArray(totalItems)
        val types          = IntArray(totalItems)
        val timestamps     = LongArray(totalItems)
        val likes          = IntArray(totalItems)
        val views          = IntArray(totalItems)
        val shares         = IntArray(totalItems)
        val saves          = IntArray(totalItems)
        val comments       = IntArray(totalItems)
        val authorIds      = IntArray(totalItems)
        val categoryIds    = IntArray(totalItems)
        val quality        = FloatArray(totalItems)
        val credibility    = FloatArray(totalItems) { 1.0f } // Default: trusted
        val completionRate = FloatArray(totalItems)

        var idx = 0

        // Add posts
        for (i in posts.indices) {
            val post = posts[i]
            val hasVideo = post.images.any { 
                it.contains(".mp4", ignoreCase = true) || 
                it.contains(".webm", ignoreCase = true) ||
                it.contains("video", ignoreCase = true)
            }
            
            sourceMap.add(SourceMapping(
                type = if (hasVideo) FeedItemType.POST_VIDEO else FeedItemType.POST_IMAGE,
                dataIndex = i
            ))
            
            indices[idx]        = idx
            types[idx]          = if (hasVideo) FeedItemType.POST_VIDEO.ordinal else FeedItemType.POST_IMAGE.ordinal
            timestamps[idx]     = parseTimestamp(post.createdAt)
            likes[idx]          = post.likesCount
            views[idx]          = 0 // Post UI model doesn't track views
            shares[idx]         = post.sharesCount
            saves[idx]          = post.savesCount
            comments[idx]       = post.reviewsCount
            authorIds[idx]      = post.userId.hashCode()
            categoryIds[idx]    = 0 // Default category
            quality[idx]        = 0.0f // Let C++ compute from engagement
            credibility[idx]    = 1.0f
            completionRate[idx] = 0.0f
            idx++
        }

        // Add rends
        for (i in rends.indices) {
            val rend = rends[i]
            
            sourceMap.add(SourceMapping(
                type = FeedItemType.REND_VIDEO,
                dataIndex = i
            ))
            
            indices[idx]        = idx
            types[idx]          = FeedItemType.REND_VIDEO.ordinal
            timestamps[idx]     = parseTimestamp(rend.createdAt)
            likes[idx]          = rend.likesCount
            views[idx]          = rend.viewsCount
            shares[idx]         = rend.sharesCount
            saves[idx]          = rend.savesCount
            comments[idx]       = rend.reviewsCount
            authorIds[idx]      = rend.userId.hashCode()
            categoryIds[idx]    = 1 // Video category
            quality[idx]        = 0.0f // Let C++ compute
            credibility[idx]    = 1.0f
            completionRate[idx] = 0.5f // Default 50% completion
            idx++
        }

        // Add suggested accounts placeholder
        sourceMap.add(SourceMapping(FeedItemType.SUGGESTED_ACCOUNTS, 0))
        indices[idx] = idx; types[idx] = FeedItemType.SUGGESTED_ACCOUNTS.ordinal
        timestamps[idx] = System.currentTimeMillis()
        likes[idx] = 0; views[idx] = 0; shares[idx] = 0; saves[idx] = 0; comments[idx] = 0
        authorIds[idx] = 0; categoryIds[idx] = 0; quality[idx] = 0f; credibility[idx] = 1f; completionRate[idx] = 0f
        idx++

        // Add special module placeholder
        sourceMap.add(SourceMapping(FeedItemType.SPECIAL_MODULE, 0))
        indices[idx] = idx; types[idx] = FeedItemType.SPECIAL_MODULE.ordinal
        timestamps[idx] = System.currentTimeMillis()
        likes[idx] = 0; views[idx] = 0; shares[idx] = 0; saves[idx] = 0; comments[idx] = 0
        authorIds[idx] = 0; categoryIds[idx] = 0; quality[idx] = 0f; credibility[idx] = 1f; completionRate[idx] = 0f
        idx++

        // Pass to native engine v2
        NativeFeedEngine.addItems(
            indices.copyOf(idx), types.copyOf(idx), timestamps.copyOf(idx),
            likes.copyOf(idx), views.copyOf(idx), shares.copyOf(idx),
            saves.copyOf(idx), comments.copyOf(idx), authorIds.copyOf(idx),
            categoryIds.copyOf(idx), quality.copyOf(idx), credibility.copyOf(idx),
            completionRate.copyOf(idx)
        )

        // Start a fresh session for scoring
        NativeFeedEngine.startSession()

        Log.d(TAG, "Loaded $idx items into native engine v2")
    }

    // ═══════════════════════════════════════════════════════════
    // FEED GENERATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Generate the initial feed batch (1 item, always image/video).
     */
    suspend fun generateInitialFeed() = withContext(Dispatchers.Default) {
        _isLoading.value = true

        val items = if (NativeFeedEngine.isAvailable() && sourceMap.isNotEmpty()) {
            generateFromNative(initial = true)
        } else {
            generateFallback(initial = true)
        }

        _feedItems.value = items
        _hasMore.value = getAvailableCount() > 0
        _isLoading.value = false

        Log.d(TAG, "Initial feed: ${items.size} items")
    }

    /**
     * Generate the next batch (exactly 4 items).
     */
    suspend fun loadMoreFeed() = withContext(Dispatchers.Default) {
        if (_isLoadingMore.value || !_hasMore.value) return@withContext

        _isLoadingMore.value = true

        val newItems = if (NativeFeedEngine.isAvailable() && sourceMap.isNotEmpty()) {
            generateFromNative(initial = false)
        } else {
            generateFallback(initial = false)
        }

        if (newItems.isNotEmpty()) {
            val currentFeed = _feedItems.value
            val startIndex = currentFeed.size
            // Re-index new items
            val reindexed = newItems.mapIndexed { i, item ->
                when (item) {
                    is FeedItem.PostItem -> item.copy(feedIndex = startIndex + i)
                    is FeedItem.RendItem -> item.copy(feedIndex = startIndex + i)
                    is FeedItem.SuggestedAccountsItem -> item.copy(feedIndex = startIndex + i)
                    is FeedItem.SpecialModuleItem -> item.copy(feedIndex = startIndex + i)
                }
            }
            _feedItems.value = currentFeed + reindexed
        }

        _hasMore.value = getAvailableCount() > 0
        _isLoadingMore.value = false

        Log.d(TAG, "Loaded more: +${newItems.size} items (total: ${_feedItems.value.size})")
    }

    // ═══════════════════════════════════════════════════════════
    // NATIVE ENGINE PATH
    // ═══════════════════════════════════════════════════════════

    private fun generateFromNative(initial: Boolean): List<FeedItem> {
        val nativeIndices = if (initial) {
            NativeFeedEngine.generateInitialBatch()
        } else {
            NativeFeedEngine.generateNextBatch()
        }

        return nativeIndices.toList().mapIndexedNotNull { feedIdx, sourceIdx ->
            mapSourceToFeedItem(sourceIdx, feedIdx)
        }
    }

    private fun mapSourceToFeedItem(sourceIndex: Int, feedIndex: Int): FeedItem? {
        if (sourceIndex < 0 || sourceIndex >= sourceMap.size) {
            Log.w(TAG, "Invalid source index: $sourceIndex")
            return null
        }

        val mapping = sourceMap[sourceIndex]
        return when (mapping.type) {
            FeedItemType.POST_IMAGE, FeedItemType.POST_VIDEO -> {
                if (mapping.dataIndex < posts.size) {
                    FeedItem.PostItem(feedIndex = feedIndex, postIndex = mapping.dataIndex)
                } else null
            }
            FeedItemType.REND_VIDEO -> {
                if (mapping.dataIndex < rends.size) {
                    FeedItem.RendItem(feedIndex = feedIndex, rendIndex = mapping.dataIndex)
                } else null
            }
            FeedItemType.SUGGESTED_ACCOUNTS -> {
                FeedItem.SuggestedAccountsItem(feedIndex = feedIndex)
            }
            FeedItemType.SPECIAL_MODULE -> {
                FeedItem.SpecialModuleItem(feedIndex = feedIndex)
            }
            FeedItemType.AD_SLOT -> null
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FALLBACK (when native engine unavailable)
    // ═══════════════════════════════════════════════════════════

    private var fallbackPostIdx = 0
    private var fallbackRendIdx = 0
    private var fallbackTotalServed = 0

    private fun generateFallback(initial: Boolean): List<FeedItem> {
        if (initial) {
            fallbackPostIdx = 0
            fallbackRendIdx = 0
            fallbackTotalServed = 0
        }

        val batchSize = if (initial) 1 else BATCH_SIZE
        val result = mutableListOf<FeedItem>()

        for (i in 0 until batchSize) {
            val feedIdx = fallbackTotalServed

            // Inject suggested accounts every 8 items (after initial)
            if (!initial && fallbackTotalServed > 0 && fallbackTotalServed % 8 == 0) {
                result.add(FeedItem.SuggestedAccountsItem(feedIndex = feedIdx))
                fallbackTotalServed++
                continue
            }

            // Alternate between posts and rends (3:1 ratio)
            val useRend = fallbackTotalServed % 4 == 3 && fallbackRendIdx < rends.size

            if (useRend && fallbackRendIdx < rends.size) {
                result.add(FeedItem.RendItem(feedIndex = feedIdx, rendIndex = fallbackRendIdx))
                fallbackRendIdx++
            } else if (fallbackPostIdx < posts.size) {
                result.add(FeedItem.PostItem(feedIndex = feedIdx, postIndex = fallbackPostIdx))
                fallbackPostIdx++
            } else if (fallbackRendIdx < rends.size) {
                result.add(FeedItem.RendItem(feedIndex = feedIdx, rendIndex = fallbackRendIdx))
                fallbackRendIdx++
            } else {
                break // No more content
            }

            fallbackTotalServed++
        }

        return result
    }

    // ═══════════════════════════════════════════════════════════
    // PREFETCH
    // ═══════════════════════════════════════════════════════════

    /**
     * Get prefetch hints for preloading images/videos.
     * Returns list of (type, dataIndex) pairs.
     */
    fun getPrefetchHints(): List<Pair<FeedItemType, Int>> {
        if (!NativeFeedEngine.isAvailable()) return emptyList()

        val hints = NativeFeedEngine.getPrefetchHints()
        return hints.toList().mapNotNull { sourceIdx ->
            if (sourceIdx in sourceMap.indices) {
                val mapping = sourceMap[sourceIdx]
                Pair(mapping.type, mapping.dataIndex)
            } else null
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ACCESSORS
    // ═══════════════════════════════════════════════════════════

    fun getPost(index: Int): Post? = posts.getOrNull(index)
    fun getRend(index: Int): Rend? = rends.getOrNull(index)
    fun getAllPosts(): List<Post> = posts
    fun getAllRends(): List<Rend> = rends

    fun getAvailableCount(): Int {
        return if (NativeFeedEngine.isAvailable()) {
            NativeFeedEngine.getAvailableCount()
        } else {
            (posts.size - fallbackPostIdx) + (rends.size - fallbackRendIdx)
        }
    }

    // ═══════════════════════════════════════════════════════════
    // RESET
    // ═══════════════════════════════════════════════════════════

    fun reset() {
        _feedItems.value = emptyList()
        _hasMore.value = true
        posts = emptyList()
        rends = emptyList()
        sourceMap.clear()
        fallbackPostIdx = 0
        fallbackRendIdx = 0
        fallbackTotalServed = 0
        NativeFeedEngine.resetSession()
    }

    // ═══════════════════════════════════════════════════════════
    // UTILS
    // ═══════════════════════════════════════════════════════════

    private fun parseTimestamp(ts: String): Long {
        if (ts.isBlank()) return System.currentTimeMillis()
        return try {
            val clean = ts.replace(" ", "T")
                .let { if (!it.contains("+") && !it.endsWith("Z")) "${it}Z" else it }
            Instant.parse(clean).toEpochMilli()
        } catch (e: Exception) {
            try {
                OffsetDateTime.parse(ts.replace(" ", "T")).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }
}
