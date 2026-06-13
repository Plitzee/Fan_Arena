package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FanArenaRepository
import com.example.data.UserMission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class MissionUiItem(
    val id: String = "",
    val title: String = "",
    val type: String = "DAILY",
    val currentProgress: Int = 0,
    val maxProgress: Int = 1,
    val tokenReward: Int = 50,
    val xpReward: Int = 10,
    val isClaimed: Boolean = false
)

class MissionViewModel(private val repository: FanArenaRepository) : ViewModel() {

    private val _canCheckIn = MutableStateFlow(true)
    val canCheckIn: StateFlow<Boolean> = _canCheckIn.asStateFlow()

    private val _missions = MutableStateFlow<List<MissionUiItem>>(emptyList())
    val missions: StateFlow<List<MissionUiItem>> = _missions.asStateFlow()

    private val claimedMissionIds = mutableSetOf<String>()

    init {
        observeMissions()
        checkCheckInStatus()
    }

    private fun checkCheckInStatus() {
        viewModelScope.launch {
            repository.loggedInUser.collect { user ->
                if (user != null) {
                    val lastCheckIn = user.lastCheckIn
                    val last = Calendar.getInstance().apply { timeInMillis = lastCheckIn }
                    val now = Calendar.getInstance()
                    val isSameDay = last.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                        last.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                    _canCheckIn.value = !(isSameDay && lastCheckIn != 0L)
                }
            }
        }
    }

    private fun observeMissions() {
        viewModelScope.launch {
            repository.loggedInUser.collect { user ->
                if (user == null) {
                    _missions.value = emptyList()
                } else {
                    repository.getUserMissionsFlow(user.email).collect { list ->
                        _missions.value = list.map(::toUiMission)
                    }
                }
            }
        }
    }

    private fun toUiMission(mission: UserMission): MissionUiItem {
        return when (mission.missionId) {
            "daily-checkin" -> MissionUiItem(mission.missionId, "Daily check-in", "DAILY", if (_canCheckIn.value) 0 else 1, 1, 50, 10, !_canCheckIn.value)
            "predict-2" -> MissionUiItem(mission.missionId, "Predict 2 matches", "DAILY", mission.currentProgress, mission.targetValue, 50, 20, claimedMissionIds.contains(mission.missionId))
            "post-1" -> MissionUiItem(mission.missionId, "Create 1 fan post", "DAILY", mission.currentProgress, mission.targetValue, 30, 15, claimedMissionIds.contains(mission.missionId))
            "follow-3" -> MissionUiItem(mission.missionId, "Follow 3 users", "WEEKLY", mission.currentProgress, mission.targetValue, 100, 50, claimedMissionIds.contains(mission.missionId))
            "win-5" -> MissionUiItem(mission.missionId, "Win 5 predictions", "ACHIEVEMENT", mission.currentProgress, mission.targetValue, 300, 150, claimedMissionIds.contains(mission.missionId))
            else -> MissionUiItem(mission.missionId, mission.missionId, "DAILY", mission.currentProgress, mission.targetValue, 50, 10, claimedMissionIds.contains(mission.missionId))
        }
    }

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect() ?: return@launch
            val bonus = 100
            val current = System.currentTimeMillis()
            repository.updateProfile(
                user.copy(
                    tokenBalance = user.tokenBalance + bonus,
                    xp = user.xp + 10,
                    lastCheckIn = current,
                    currentStreak = user.currentStreak + 1,
                    bestStreak = maxOf(user.bestStreak, user.currentStreak + 1)
                )
            )
            repository.recordTransaction("daily-checkin-$current", "Daily Reward", bonus, "EARN")
            _canCheckIn.value = false
            _missions.update { currentList ->
                currentList.map {
                    if (it.id == "daily-checkin") it.copy(currentProgress = 1, isClaimed = true) else it
                }
            }
        }
    }

    fun claimMissionReward(id: String) {
        viewModelScope.launch {
            val mission = _missions.value.find { it.id == id }
            if (mission != null && mission.currentProgress >= mission.maxProgress && !mission.isClaimed) {
                val user = repository.getLoggedInUserDirect() ?: return@launch
                repository.updateProfile(
                    user.copy(
                        tokenBalance = user.tokenBalance + mission.tokenReward,
                        xp = user.xp + mission.xpReward
                    )
                )
                repository.recordTransaction("mission-reward-$id", "Mission: ${mission.title}", mission.tokenReward, "EARN")
                claimedMissionIds.add(id)
                _missions.update { current ->
                    current.map { if (it.id == id) it.copy(isClaimed = true) else it }
                }
            }
        }
    }
}
