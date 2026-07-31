package com.mercora.app.ui.screens.explore

import com.mercora.app.data.repository.ExploreRepository
import com.mercora.app.data.repository.OffersRepository
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
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ExploreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(ExploreRepository)
        every { ExploreRepository.exploreItems } returns MutableStateFlow(emptyList())
        every { ExploreRepository.isLoading } returns MutableStateFlow(false)
        coEvery { ExploreRepository.loadExploreItems(any()) } returns Unit

        mockkObject(OffersRepository)
        every { OffersRepository.campaigns } returns MutableStateFlow(emptyList())
        every { OffersRepository.selectedCampaign } returns MutableStateFlow(null)
        every { OffersRepository.isLoading } returns MutableStateFlow(false)
        coEvery { OffersRepository.loadOffers(any()) } returns Unit
        every { OffersRepository.selectCampaign(any()) } returns Unit

        viewModel = ExploreViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial searchQuery is empty`() {
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `initial selectedCategory is null`() {
        assertNull(viewModel.selectedCategory.value)
    }

    @Test
    fun `updateSearchQuery updates searchQuery`() {
        viewModel.updateSearchQuery("ropa")
        assertEquals("ropa", viewModel.searchQuery.value)
    }

    @Test
    fun `updateSearchQuery with empty string`() {
        viewModel.updateSearchQuery("test")
        viewModel.updateSearchQuery("")
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `selectCategory sets category`() {
        viewModel.selectCategory("ropa")
        assertEquals("ropa", viewModel.selectedCategory.value)
    }

    @Test
    fun `selectCategory with null clears category`() {
        viewModel.selectCategory("ropa")
        viewModel.selectCategory(null)
        assertNull(viewModel.selectedCategory.value)
    }

    @Test
    fun `selectCategory toggles back to null`() {
        viewModel.selectCategory("ropa")
        assertEquals("ropa", viewModel.selectedCategory.value)
        viewModel.selectCategory(null)
        assertNull(viewModel.selectedCategory.value)
    }
}
