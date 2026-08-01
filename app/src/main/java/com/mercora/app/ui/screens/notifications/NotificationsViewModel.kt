package com.mercora.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.repository.NotificationRepository
import com.mercora.app.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {

    val notifications = NotificationRepository.notifications
    val isLoading = NotificationRepository.isLoading

    private val _selectedFilter = MutableStateFlow("Todas")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        loadNotifications()
        subscribeToRealtime()
    }

    fun loadNotifications(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            NotificationRepository.loadNotifications()
            AnalyticsHelper.logScreenView("notifications")
        }
    }

    fun selectFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            NotificationRepository.markAllAsRead()
        }
    }

    private fun subscribeToRealtime() {
        viewModelScope.launch {
            NotificationRepository.subscribeToRealtime()
        }
    }
}
