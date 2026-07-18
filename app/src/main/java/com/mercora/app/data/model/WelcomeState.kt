package com.mercora.app.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WelcomeData(
    val show: Boolean = false,
    val username: String = ""
)

object WelcomeState {
    private val _welcome = MutableStateFlow(WelcomeData())
    val welcome: StateFlow<WelcomeData> = _welcome.asStateFlow()

    fun trigger(username: String) {
        _welcome.value = WelcomeData(show = true, username = username)
    }

    fun consume() {
        _welcome.value = WelcomeData()
    }
}
