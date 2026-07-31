package com.mercora.app.ui.screens.notifications

import com.mercora.app.data.repository.NotificationRepository
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(NotificationRepository)
        every { NotificationRepository.notifications } returns MutableStateFlow(emptyList())
        every { NotificationRepository.isLoading } returns MutableStateFlow(false)
        every { NotificationRepository.unreadCount } returns MutableStateFlow(0)
        coEvery { NotificationRepository.loadNotifications() } returns Unit
        coEvery { NotificationRepository.subscribeToRealtime() } returns Unit
        coEvery { NotificationRepository.markAllAsRead() } returns Unit

        viewModel = NotificationsViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial selectedFilter is Todas`() {
        assertEquals("Todas", viewModel.selectedFilter.value)
    }

    @Test
    fun `selectFilter updates filter`() {
        viewModel.selectFilter("Compras")
        assertEquals("Compras", viewModel.selectedFilter.value)
    }

    @Test
    fun `selectFilter can change multiple times`() {
        viewModel.selectFilter("Compras")
        assertEquals("Compras", viewModel.selectedFilter.value)
        viewModel.selectFilter("Ventas")
        assertEquals("Ventas", viewModel.selectedFilter.value)
        viewModel.selectFilter("Todas")
        assertEquals("Todas", viewModel.selectedFilter.value)
    }

    @Test
    fun `selectFilter with empty string`() {
        viewModel.selectFilter("")
        assertEquals("", viewModel.selectedFilter.value)
    }
}
