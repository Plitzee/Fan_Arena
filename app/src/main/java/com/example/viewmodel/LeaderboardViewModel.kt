package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FanArenaRepository
import com.example.data.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LeaderboardViewModel(private val repository: FanArenaRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            val userList = repository.getAllUsers()
            _users.value = userList
            _isLoading.value = false
        }
    }

    fun getTopPredictors() = _users.value.sortedByDescending { it.tokenBalance }
    fun getRisingFans() = _users.value.sortedByDescending { it.xp }
}
