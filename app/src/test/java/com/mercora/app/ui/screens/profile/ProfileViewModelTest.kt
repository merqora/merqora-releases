package com.mercora.app.ui.screens.profile

import com.mercora.app.data.repository.AvatarShapeRepository
import com.mercora.app.data.repository.FollowersRepository
import com.mercora.app.data.repository.HighlightRepository
import com.mercora.app.data.repository.NotificationRepository
import com.mercora.app.data.repository.PostRepository
import com.mercora.app.data.repository.ProfileRepository
import com.mercora.app.data.repository.RendRepository
import com.mercora.app.data.repository.StoryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(ProfileRepository)
        every { ProfileRepository.currentProfile } returns MutableStateFlow(null)
        every { ProfileRepository.isLoading } returns MutableStateFlow(false)
        coEvery { ProfileRepository.loadCurrentProfile(any()) } returns null
        every { ProfileRepository.clearProfile() } returns Unit

        mockkObject(HighlightRepository)
        every { HighlightRepository.highlights } returns MutableStateFlow(emptyList())
        coEvery { HighlightRepository.loadHighlights() } returns 0

        mockkObject(PostRepository)
        every { PostRepository.userPosts } returns MutableStateFlow(emptyList())
        every { PostRepository.isLoadingUserPosts } returns MutableStateFlow(false)
        coEvery { PostRepository.loadUserPosts(any()) } returns Unit
        coEvery { PostRepository.getPostsByIds(any()) } returns emptyList()

        mockkObject(RendRepository)
        every { RendRepository.rends } returns MutableStateFlow(emptyList())
        coEvery { RendRepository.loadRends() } returns Unit

        mockkObject(StoryRepository)
        every { StoryRepository.myStories } returns MutableStateFlow(emptyList())

        mockkObject(AvatarShapeRepository)
        every { AvatarShapeRepository.init(any()) } returns Unit
        every { AvatarShapeRepository.selectedShapeFlow } returns MutableStateFlow(com.mercora.app.data.model.AvatarShapeType.CIRCLE)
        every { AvatarShapeRepository.hasUnseenShapes() } returns false
        every { AvatarShapeRepository.markShapesAsSeen() } returns Unit
        every { AvatarShapeRepository.getSelectedShape() } returns com.mercora.app.data.model.AvatarShapeType.CIRCLE

        mockkObject(FollowersRepository)
        coEvery { FollowersRepository.subscribeToFollowChanges(any()) } returns Unit
        every { FollowersRepository.followChangeTrigger } returns MutableStateFlow(Pair("", ""))

        mockkObject(NotificationRepository)
        every { NotificationRepository.profileRefreshTrigger } returns MutableStateFlow(0)
        coEvery { NotificationRepository.loadNotifications() } returns Unit
        coEvery { NotificationRepository.subscribeToRealtime() } returns Unit

        viewModel = ProfileViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial selectedTabIndex is 0`() {
        assertEquals(0, viewModel.selectedTabIndex.value)
    }

    @Test
    fun `selectTab updates index`() {
        viewModel.selectTab(2)
        assertEquals(2, viewModel.selectedTabIndex.value)
    }

    @Test
    fun `selectTab cycles through tabs`() {
        viewModel.selectTab(0)
        assertEquals(0, viewModel.selectedTabIndex.value)
        viewModel.selectTab(1)
        assertEquals(1, viewModel.selectedTabIndex.value)
        viewModel.selectTab(4)
        assertEquals(4, viewModel.selectedTabIndex.value)
    }

    @Test
    fun `initial shouldLogout is false`() {
        assertFalse(viewModel.shouldLogout.value)
    }

    @Test
    fun `requestLogout sets shouldLogout to true`() {
        viewModel.requestLogout()
        assertTrue(viewModel.shouldLogout.value)
    }

    @Test
    fun `cancelLogout resets shouldLogout to false`() {
        viewModel.requestLogout()
        assertTrue(viewModel.shouldLogout.value)
        viewModel.cancelLogout()
        assertFalse(viewModel.shouldLogout.value)
    }

    @Test
    fun `cancelLogout when already false`() {
        viewModel.cancelLogout()
        assertFalse(viewModel.shouldLogout.value)
    }

    @Test
    fun `initial isLoadingSaved is false`() {
        assertFalse(viewModel.isLoadingSaved.value)
    }

    @Test
    fun `initial savedPosts is empty`() {
        assertTrue(viewModel.savedPosts.value.isEmpty())
    }
}
