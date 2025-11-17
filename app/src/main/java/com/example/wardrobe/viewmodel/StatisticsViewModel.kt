package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.CategoryCount
import com.example.wardrobe.data.NameCount
import com.example.wardrobe.data.SeasonCount
import com.example.wardrobe.data.WardrobeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(repo: WardrobeRepository) : ViewModel() {
    val countByMember: StateFlow<List<NameCount>> = repo.getCountByMember()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val countBySeason: StateFlow<List<SeasonCount>> = repo.getCountBySeason()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val countByCategory: StateFlow<List<CategoryCount>> = repo.getCountByCategory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class StatisticsViewModelFactory(private val repo: WardrobeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}