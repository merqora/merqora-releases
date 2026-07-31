package com.mercora.app.ui.screens.messages

import com.mercora.app.data.model.HandshakeEvent
import com.mercora.app.data.model.HandshakeTransaction
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.repository.ChatRepository
import com.mercora.app.data.repository.HandshakeRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MessagesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MessagesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(ChatRepository)
        every { ChatRepository.conversations } returns MutableStateFlow(emptyList())
        every { ChatRepository.isLoading } returns MutableStateFlow(false)
        coEvery { ChatRepository.loadConversations() } returns Unit
        coEvery { ChatRepository.togglePinConversation(any(), any()) } returns true
        coEvery { ChatRepository.deleteConversation(any()) } returns true

        mockkObject(HandshakeRepository)
        coEvery { HandshakeRepository.subscribeToHandshakes(any()) } returns Unit
        every { HandshakeRepository.handshakeEvents } returns MutableStateFlow(HandshakeEvent.Deleted(""))

        viewModel = MessagesViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial selectedChatUser is null`() {
        assertNull(viewModel.selectedChatUser.value)
    }

    @Test
    fun `initial selectedChatConversationId is null`() {
        assertNull(viewModel.selectedChatConversationId.value)
    }

    @Test
    fun `initial currentUserId is null`() {
        assertNull(viewModel.currentUserId.value)
    }

    @Test
    fun `selectChat sets selected user and conversation`() {
        val user = Usuario(userId = "123", username = "test")
        viewModel.selectChat(user, "conv1")
        assertEquals(user, viewModel.selectedChatUser.value)
        assertEquals("conv1", viewModel.selectedChatConversationId.value)
    }

    @Test
    fun `selectChat with null conversationId`() {
        val user = Usuario(userId = "123", username = "test")
        viewModel.selectChat(user, null)
        assertEquals(user, viewModel.selectedChatUser.value)
        assertNull(viewModel.selectedChatConversationId.value)
    }

    @Test
    fun `clearSelection resets both values`() {
        val user = Usuario(userId = "123", username = "test")
        viewModel.selectChat(user, "conv1")
        viewModel.clearSelection()
        assertNull(viewModel.selectedChatUser.value)
        assertNull(viewModel.selectedChatConversationId.value)
    }

    @Test
    fun `clearSelection when already null`() {
        viewModel.clearSelection()
        assertNull(viewModel.selectedChatUser.value)
        assertNull(viewModel.selectedChatConversationId.value)
    }
}
