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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MemberViewModel(private val repo: WardrobeRepository) : ViewModel() {

    // Member list & admin mode
    val members: StateFlow<List<Member>> =
        repo.getAllMembers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isAdminMode: StateFlow<Boolean> =
        repo.settings.isAdminMode
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Outdated clothing counts (per member)
    private val _outdatedCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val outdatedCounts: StateFlow<Map<Long, Int>> = _outdatedCounts.asStateFlow()

    // Current selected member
    private val _currentMemberId = MutableStateFlow<Long?>(null)
    val currentMemberId: StateFlow<Long?> = _currentMemberId.asStateFlow()

    val currentMemberName: StateFlow<String> =
        members
            .combine(currentMemberId) { list, id ->
                list.firstOrNull { it.memberId == id }?.name ?: ""
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // Initial observers
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

    // Member CRUD operations
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

    fun updateMember(
        memberId: Long,
        name: String,
        gender: String,
        age: Int,
        birthDate: Long?
    ) {
        viewModelScope.launch {
            repo.updateMember(memberId, name, gender, age, birthDate)
        }
    }

    fun deleteMember(memberId: Long) {
        viewModelScope.launch {
            repo.deleteMember(memberId)
        }
    }

    // Current member selection helpers
    fun setCurrentMember(id: Long?) {
        _currentMemberId.value = id
    }

    fun clearCurrentMember() {
        _currentMemberId.value = null
    }
}