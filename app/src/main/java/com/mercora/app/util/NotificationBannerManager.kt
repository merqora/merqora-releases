package com.mercora.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class NotificationBannerEvent(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val body: String,
    val type: String = "general",
    val targetId: String? = null,
    val userId: String? = null
)

object NotificationBannerManager {
    private val _events = MutableSharedFlow<NotificationBannerEvent>(extraBufferCapacity = 5)
    val events: SharedFlow<NotificationBannerEvent> = _events.asSharedFlow()

    fun emit(event: NotificationBannerEvent) {
        _events.tryEmit(event)
    }
}
