package com.mercora.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.model.Post
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.AvatarShapeRepository
import com.mercora.app.data.repository.FollowersRepository
import com.mercora.app.data.repository.HighlightRepository
import com.mercora.app.data.repository.NotificationRepository
import com.mercora.app.data.repository.PostRepository
import com.mercora.app.data.repository.ProfileRepository
import com.mercora.app.data.repository.RendRepository
import com.mercora.app.data.repository.StoryRepository
import com.mercora.app.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
private data class PostSaveDB(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("post_id") val postId: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    val currentProfile = ProfileRepository.currentProfile
    val isLoading = ProfileRepository.isLoading
    val highlights = HighlightRepository.highlights
    val userPosts = PostRepository.userPosts
    val isLoadingUserPosts = PostRepository.isLoadingUserPosts
    val myStories = StoryRepository.myStories
    val userRends = RendRepository.rends

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())
    val savedPosts: StateFlow<List<Post>> = _savedPosts.asStateFlow()

    private val _isLoadingSaved = MutableStateFlow(false)
    val isLoadingSaved: StateFlow<Boolean> = _isLoadingSaved.asStateFlow()

    private val _shouldLogout = MutableStateFlow(false)
    val shouldLogout: StateFlow<Boolean> = _shouldLogout.asStateFlow()

    init {
        loadProfileData()
        subscribeToFollowChanges()
        listenToRefreshTriggers()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            ProfileRepository.loadCurrentProfile()
            HighlightRepository.loadHighlights()
            PostRepository.loadUserPosts()
            RendRepository.loadRends()
            AnalyticsHelper.logScreenView("profile")
        }
    }

    private fun subscribeToFollowChanges() {
        viewModelScope.launch {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            FollowersRepository.subscribeToFollowChanges(userId)
            FollowersRepository.followChangeTrigger.collect {
                ProfileRepository.loadCurrentProfile()
                PostRepository.loadUserPosts(forceRefresh = true)
            }
        }
    }

    private fun listenToRefreshTriggers() {
        viewModelScope.launch {
            NotificationRepository.profileRefreshTrigger.collect {
                ProfileRepository.loadCurrentProfile()
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        if (index == 4 && _savedPosts.value.isEmpty()) {
            loadSavedPosts()
        }
    }

    private fun loadSavedPosts() {
        viewModelScope.launch {
            _isLoadingSaved.value = true
            try {
                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                val postIds = SupabaseClient.database
                    .from("post_saves")
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<PostSaveDB>()
                    .map { it.postId }
                _savedPosts.value = PostRepository.getPostsByIds(postIds)
            } catch (e: Exception) {
                _savedPosts.value = emptyList()
            } finally {
                _isLoadingSaved.value = false
            }
        }
    }

    fun refreshAll() {
        loadProfileData()
    }

    fun requestLogout() {
        _shouldLogout.value = true
    }

    fun cancelLogout() {
        _shouldLogout.value = false
    }
}
