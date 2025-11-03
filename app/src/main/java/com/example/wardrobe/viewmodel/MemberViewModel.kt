package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.Member
import com.example.wardrobe.data.WardrobeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemberViewModel(private val repo: WardrobeRepository) : ViewModel() {

    val members: StateFlow<List<Member>> = repo.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createMember(name: String) {
        viewModelScope.launch {
            repo.createMember(name)
        }
    }
}