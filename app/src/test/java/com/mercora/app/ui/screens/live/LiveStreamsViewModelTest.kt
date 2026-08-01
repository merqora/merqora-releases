package com.mercora.app.ui.screens.live

import com.mercora.app.data.repository.LiveStreamRepository
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LiveStreamsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LiveStreamsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(LiveStreamRepository)
        every { LiveStreamRepository.activeStreams } returns MutableStateFlow(emptyList())
        every { LiveStreamRepository.lastError } returns MutableStateFlow(null)
        coEvery { LiveStreamRepository.loadActiveStreams() } returns Unit
        every { LiveStreamRepository.clearError() } returns Unit

        viewModel = LiveStreamsViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial isRefreshing is false`() {
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `initial showErrorDialog is false`() {
        assertFalse(viewModel.showErrorDialog.value)
    }

    @Test
    fun `initial errorMessage is empty`() {
        assertEquals("", viewModel.errorMessage.value)
    }

    @Test
    fun `dismissError resets error states`() {
        viewModel.dismissError()
        assertFalse(viewModel.showErrorDialog.value)
        assertEquals("", viewModel.errorMessage.value)
    }

    @Test
    fun `dismissError after multiple calls`() {
        viewModel.dismissError()
        viewModel.dismissError()
        assertFalse(viewModel.showErrorDialog.value)
        assertEquals("", viewModel.errorMessage.value)
    }
}
