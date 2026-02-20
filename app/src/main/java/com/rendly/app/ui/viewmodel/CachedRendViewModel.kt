package com.rendly.app.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rendly.app.data.cache.CacheOrchestrator
import com.rendly.app.data.cache.Screen
import com.rendly.app.data.cache.core.CacheSource
import com.rendly.app.data.cache.repository.CachedRendRepository
import com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult
import com.rendly.app.data.model.Rend
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Example ViewModel demonstrating cache-first pattern integration.
 * 
 * Key principles:
 * - ViewModel does NOT contain cache logic (delegated to repository)
 * - UI state reflects cache source for visual feedback
 * - Smooth transitions between cache and network data
 * - No blocking operations on UI thread
 * 
 * Usage in Compose:
 * ```kotlin
 * @Composable
 * fun RendsFeedScreen(viewModel: CachedRendViewModel = viewModel()) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     
 *     LaunchedEffect(Unit) {
 *         viewModel.loadFeed()
 *     }
 *     
 *     when (val state = uiState) {
 *         is RendsUiState.Loading -> LoadingSkeleton()
 *         is RendsUiState.Success -> {
 *             RendsList(
 *                 rends = state.rends,
 *                 isRefreshing = state.isRefreshing,
 *                 showStaleIndicator = state.isStale
 *             )
 *         }
 *         is RendsUiState.Empty -> EmptyState()
 *         is RendsUiState.Error -> ErrorState(state.message)
 *     }
 * }
 * ```
 */
class CachedRendViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "CachedRendViewModel"
        private const val PAGE_SIZE = 20
    }
    
    private val repository = CachedRendRepository.getInstance(application)
    private val cacheOrchestrator = CacheOrchestrator.getInstance(application)
    
    // UI State
    private val _uiState = MutableStateFlow<RendsUiState>(RendsUiState.Loading)
    val uiState: StateFlow<RendsUiState> = _uiState.asStateFlow()
    
    // Pagination state
    private var currentPage = 0
    private var isLoadingMore = false
    private var hasMorePages = true
    private var loadJob: Job? = null
    
    // All loaded rends (across pages)
    private val allRends = mutableListOf<Rend>()
    
    init {
        // Notify orchestrator of current screen
        cacheOrchestrator.onScreenChanged(Screen.RendsFeed)
    }
    
    /**
     * Load initial feed data.
     * Uses cache-first strategy: shows cache immediately, updates from network.
     */
    fun loadFeed(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            currentPage = 0
            allRends.clear()
            hasMorePages = true
        }
        
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getFeed(
                page = 0,
                pageSize = PAGE_SIZE,
                forceRefresh = forceRefresh
            ).collect { result ->
                handleResult(result, isInitialLoad = true)
            }
        }
    }
    
    /**
     * Load more rends (pagination).
     */
    fun loadMore() {
        if (isLoadingMore || !hasMorePages) return
        
        isLoadingMore = true
        val nextPage = currentPage + 1
        
        viewModelScope.launch {
            repository.getFeed(
                page = nextPage,
                pageSize = PAGE_SIZE,
                forceRefresh = false
            ).collect { result ->
                when (result) {
                    is DataResult.Success -> {
                        if (result.data.isEmpty()) {
                            hasMorePages = false
                        } else {
                            currentPage = nextPage
                            allRends.addAll(result.data)
                            
                            _uiState.value = RendsUiState.Success(
                                rends = allRends.toList(),
                                isRefreshing = false,
                                isStale = result.isStale,
                                source = result.source,
                                canLoadMore = hasMorePages
                            )
                        }
                    }
                    is DataResult.Empty -> {
                        hasMorePages = false
                    }
                    is DataResult.Error -> {
                        Log.e(TAG, "Load more error: ${result.exception.message}")
                        // Don't update UI state on pagination error, just log
                    }
                    is DataResult.Loading -> { /* Ignore */ }
                }
                isLoadingMore = false
            }
        }
        
        // Trigger prefetch for next page
        cacheOrchestrator.onScrollApproachingEnd(Screen.RendsFeed, nextPage)
    }
    
    /**
     * Pull-to-refresh handler.
     */
    fun refresh() {
        _uiState.update { current ->
            if (current is RendsUiState.Success) {
                current.copy(isRefreshing = true)
            } else {
                current
            }
        }
        loadFeed(forceRefresh = true)
    }
    
    /**
     * Toggle like on a rend (optimistic update).
     */
    fun toggleLike(rend: Rend) {
        viewModelScope.launch {
            val newLikeCount = if (rend.isLiked) rend.likesCount - 1 else rend.likesCount + 1
            val updatedRend = rend.copy(
                isLiked = !rend.isLiked,
                likesCount = newLikeCount.coerceAtLeast(0)
            )
            
            // Optimistic update: update UI immediately
            updateRendInList(updatedRend)
            
            // Persist to cache
            try {
                repository.updateLikeCount(rend.id, newLikeCount.coerceAtLeast(0))
            } catch (e: Exception) {
                // Revert on failure
                Log.e(TAG, "Failed to update like: ${e.message}")
                updateRendInList(rend)
            }
        }
    }
    
    /**
     * Handle scroll position for smart prefetching.
     */
    fun onVisibleItemsChanged(firstVisible: Int, lastVisible: Int) {
        val totalItems = allRends.size
        val threshold = 5 // Prefetch when 5 items from end
        
        if (lastVisible >= totalItems - threshold && hasMorePages && !isLoadingMore) {
            loadMore()
        }
    }
    
    private fun handleResult(result: DataResult<List<Rend>>, isInitialLoad: Boolean) {
        when (result) {
            is DataResult.Success -> {
                val rends = result.data
                
                if (isInitialLoad) {
                    allRends.clear()
                    allRends.addAll(rends)
                    currentPage = 0
                }
                
                val newState = RendsUiState.Success(
                    rends = allRends.toList(),
                    isRefreshing = false,
                    isStale = result.isStale,
                    source = result.source,
                    canLoadMore = hasMorePages
                )
                
                // Animate transition if data source changed
                val currentState = _uiState.value
                if (currentState is RendsUiState.Success && 
                    currentState.source != result.source &&
                    result.source == CacheSource.NETWORK) {
                    // Data updated from network - could trigger smooth animation
                    Log.d(TAG, "Data refreshed from network, animating update")
                }
                
                _uiState.value = newState
                
                Log.d(TAG, "Loaded ${rends.size} rends from ${result.source} (stale=${result.isStale})")
            }
            
            is DataResult.Loading -> {
                if (_uiState.value !is RendsUiState.Success) {
                    _uiState.value = RendsUiState.Loading
                }
            }
            
            is DataResult.Empty -> {
                if (allRends.isEmpty()) {
                    _uiState.value = RendsUiState.Empty
                }
            }
            
            is DataResult.Error -> {
                Log.e(TAG, "Error loading rends: ${result.exception.message}")
                
                if (allRends.isEmpty()) {
                    _uiState.value = RendsUiState.Error(
                        message = result.exception.message ?: "Error loading rends",
                        cachedData = result.cachedData as? List<Rend>
                    )
                } else {
                    // We have cached data, just log the error
                    _uiState.update { current ->
                        if (current is RendsUiState.Success) {
                            current.copy(isRefreshing = false, networkError = result.exception.message)
                        } else current
                    }
                }
            }
        }
    }
    
    private fun updateRendInList(rend: Rend) {
        val index = allRends.indexOfFirst { it.id == rend.id }
        if (index >= 0) {
            allRends[index] = rend
            _uiState.update { current ->
                if (current is RendsUiState.Success) {
                    current.copy(rends = allRends.toList())
                } else current
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        loadJob?.cancel()
    }
}

/**
 * UI state for rends screen.
 */
sealed class RendsUiState {
    data object Loading : RendsUiState()
    
    data class Success(
        val rends: List<Rend>,
        val isRefreshing: Boolean = false,
        val isStale: Boolean = false,
        val source: CacheSource = CacheSource.NETWORK,
        val canLoadMore: Boolean = true,
        val networkError: String? = null
    ) : RendsUiState()
    
    data object Empty : RendsUiState()
    
    data class Error(
        val message: String,
        val cachedData: List<Rend>? = null
    ) : RendsUiState()
}

/**
 * Extension to check if state has usable data.
 */
val RendsUiState.hasData: Boolean
    get() = this is RendsUiState.Success && rends.isNotEmpty()

/**
 * Extension to get rends from any state (for shimmer/skeleton).
 */
val RendsUiState.rendsList: List<Rend>
    get() = when (this) {
        is RendsUiState.Success -> rends
        is RendsUiState.Error -> cachedData ?: emptyList()
        else -> emptyList()
    }
