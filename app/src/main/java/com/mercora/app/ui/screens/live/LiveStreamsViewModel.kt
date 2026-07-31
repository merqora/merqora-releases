package com.mercora.app.ui.screens.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.repository.LiveStreamRepository
import com.mercora.app.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveStreamsViewModel @Inject constructor() : ViewModel() {

    val activeStreams = LiveStreamRepository.activeStreams
    val lastError = LiveStreamRepository.lastError

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog: StateFlow<Boolean> = _showErrorDialog.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    init {
        loadActiveStreams()
    }

    fun loadActiveStreams() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                LiveStreamRepository.loadActiveStreams()
                AnalyticsHelper.logScreenView("live_streams")
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar streams"
                _showErrorDialog.value = true
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun dismissError() {
        _showErrorDialog.value = false
        _errorMessage.value = ""
        LiveStreamRepository.clearError()
    }

    fun pullToRefresh() {
        loadActiveStreams()
    }
}
