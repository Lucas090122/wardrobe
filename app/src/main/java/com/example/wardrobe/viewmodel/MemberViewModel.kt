package com.example.wardrobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardrobe.data.Member
import com.example.wardrobe.data.WardrobeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MemberViewModel(private val repo: WardrobeRepository) : ViewModel() {

    val members: StateFlow<List<Member>> = repo.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _outdatedCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val outdatedCounts: StateFlow<Map<Long, Int>> = _outdatedCounts.asStateFlow()

    init {
        viewModelScope.launch {
            members.collectLatest { list ->
                val map = mutableMapOf<Long, Int>()
                for (m in list) {
                    val count = repo.countOutdatedItems(m.memberId)
                    map[m.memberId] = count
                }
                _outdatedCounts.value = map
            }
        }
    }

    fun createMember(
        name: String,
        gender: String,
        age: Int,
        birthDate: Long?
    ) {
        viewModelScope.launch {
            repo.createMember(name, gender, age, birthDate)
        }
    }
}