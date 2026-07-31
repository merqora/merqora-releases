package com.mercora.app.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.ChatRepository
import com.mercora.app.data.repository.HandshakeRepository
import com.mercora.app.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor() : ViewModel() {

    val conversations = ChatRepository.conversations
    val isLoading = ChatRepository.isLoading

    private val _selectedChatUser = MutableStateFlow<Usuario?>(null)
    val selectedChatUser: StateFlow<Usuario?> = _selectedChatUser.asStateFlow()

    private val _selectedChatConversationId = MutableStateFlow<String?>(null)
    val selectedChatConversationId: StateFlow<String?> = _selectedChatConversationId.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        loadConversations()
        listenToHandshakes()
        startPolling()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            _currentUserId.value = userId
            ChatRepository.loadConversations()
            AnalyticsHelper.logScreenView("messages")
        }
    }

    private fun listenToHandshakes() {
        viewModelScope.launch {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            HandshakeRepository.subscribeToHandshakes(userId)
            HandshakeRepository.handshakeEvents.collectLatest {
                ChatRepository.loadConversations()
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5_000)
                ChatRepository.loadConversations()
            }
        }
    }

    fun selectChat(user: Usuario, conversationId: String?) {
        _selectedChatUser.value = user
        _selectedChatConversationId.value = conversationId
    }

    fun clearSelection() {
        _selectedChatUser.value = null
        _selectedChatConversationId.value = null
    }

    fun togglePinConversation(conversationId: String, isPinned: Boolean) {
        viewModelScope.launch {
            ChatRepository.togglePinConversation(conversationId, isPinned)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            ChatRepository.deleteConversation(conversationId)
        }
    }

    fun refreshConversations() {
        viewModelScope.launch {
            ChatRepository.loadConversations()
        }
    }
}
