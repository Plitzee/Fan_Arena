package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FanArenaRepository
import com.example.data.Prediction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PredictionStats(
    val total: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val pending: Int = 0,
    val winRate: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

class PredictionHistoryViewModel(private val repository: FanArenaRepository) : ViewModel() {

    val predictions: StateFlow<List<Prediction>> = repository.predictions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<PredictionStats> = predictions.map { list ->
        val total = list.size
        val correct = list.count { it.status == "WON" }
        val wrong = list.count { it.status == "LOST" }
        val pending = list.count { it.status == "PENDING" }
        val winRate = if (total - pending > 0) (correct * 100 / (total - pending)) else 0
        
        // Calculate streaks (simplified)
        var currentStreak = 0
        var maxStreak = 0
        for (p in list.sortedBy { it.timestamp }) {
            if (p.status == "WON") {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else if (p.status == "LOST") {
                currentStreak = 0
            }
        }

        PredictionStats(total, correct, wrong, pending, winRate, currentStreak, maxStreak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictionStats())

    fun resolvePredictionDemo(predictionId: Long, isWinner: Boolean) {
        viewModelScope.launch {
            repository.resolvePrediction(predictionId, isWinner)
        }
    }
}
