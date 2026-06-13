package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FavoriteLeague
import com.example.data.FavoriteTeam
import com.example.data.FanArenaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repository: FanArenaRepository) : ViewModel() {

    val favoriteTeams: StateFlow<List<FavoriteTeam>> = repository.getFavoriteTeams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteLeagues: StateFlow<List<FavoriteLeague>> = repository.getFavoriteLeagues()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavoriteTeam(team: FavoriteTeam) {
        viewModelScope.launch {
            repository.toggleFavoriteTeam(team)
        }
    }

    fun toggleFavoriteLeague(league: FavoriteLeague) {
        viewModelScope.launch {
            repository.toggleFavoriteLeague(league)
        }
    }
}
