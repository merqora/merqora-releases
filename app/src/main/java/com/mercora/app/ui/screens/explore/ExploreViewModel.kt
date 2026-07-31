package com.mercora.app.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.repository.ExploreRepository
import com.mercora.app.data.repository.OfferCampaign
import com.mercora.app.data.repository.OffersRepository
import com.mercora.app.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor() : ViewModel() {

    val exploreItems = ExploreRepository.exploreItems
    val isLoading = ExploreRepository.isLoading

    val campaigns = OffersRepository.campaigns
    val selectedCampaign = OffersRepository.selectedCampaign
    val offersLoading = OffersRepository.isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadExploreItems()
    }

    fun loadExploreItems(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            ExploreRepository.loadExploreItems(forceRefresh)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun loadOffers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            OffersRepository.loadOffers(forceRefresh)
        }
    }

    fun selectCampaign(campaign: OfferCampaign) {
        OffersRepository.selectCampaign(campaign)
        AnalyticsHelper.logEngagement("campaign", campaign.id ?: "")
    }
}
