package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiRiskAssessment
import com.example.data.Comment
import com.example.data.FanArenaRepository
import com.example.data.MatchEntity
import com.example.data.MatchRoomMessage
import com.example.data.MatchRoomPollSummary
import com.example.data.NotificationType
import com.example.data.Prediction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchDetailUiState(
    val match: MatchEntity? = null,
    val aiInsight: String = "AI đang chuẩn bị phân tích trận đấu...",
    val isAiLoading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val comments: List<Comment> = emptyList(),
    val roomMessages: List<MatchRoomMessage> = emptyList(),
    val roomPollSummary: MatchRoomPollSummary = MatchRoomPollSummary(),
    val roomInput: String = "",
    val roomError: String? = null,
    val selectedChoice: String? = null,
    val predictionAiExplanation: String? = null,
    val isAiExplanationLoading: Boolean = false,
    val aiRiskAssessment: AiRiskAssessment = AiRiskAssessment(),
    val isRiskLoading: Boolean = false,
    val bidAmount: Float = 50f,
    val isPredicting: Boolean = false,
    val predictionSuccess: Boolean = false,
    val predictionError: String? = null,
    val alreadyPredicted: Boolean = false,
    val currentUserEmail: String = ""
)

class MatchDetailViewModel(private val repository: FanArenaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    private var commentsJob: Job? = null
    private var roomMessagesJob: Job? = null
    private var roomPollJob: Job? = null

    fun loadMatchDetails(matchId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val match = repository.getMatchById(matchId)
            val user = repository.getLoggedInUserDirect()
            if (match != null) {
                repository.resolveFinishedMatchPredictions(match)
                val userEmail = user?.email ?: ""
                val predictions = repository.getPendingPredictionsForMatch(matchId)
                val hasUserPredicted = predictions.any { it.userEmail == userEmail }

                _uiState.update {
                    it.copy(
                        match = match,
                        isLoading = false,
                        currentUserEmail = userEmail,
                        alreadyPredicted = hasUserPredicted
                    )
                }
                fetchAiInsight()
                fetchAiRiskAssessment()
                observeComments(matchId.toString())
                observeMatchRoom(matchId.toString())
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy trận đấu") }
            }
        }
    }

    private fun observeComments(matchId: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            repository.getCommentsForMatch(matchId).collect { list ->
                _uiState.update { it.copy(comments = list) }
            }
        }
    }

    private fun observeMatchRoom(matchId: String) {
        roomMessagesJob?.cancel()
        roomPollJob?.cancel()
        roomMessagesJob = viewModelScope.launch {
            repository.getMatchRoomMessages(matchId).collect { messages ->
                _uiState.update { it.copy(roomMessages = messages, roomError = null) }
            }
        }
        roomPollJob = viewModelScope.launch {
            repository.getMatchRoomPollSummary(matchId).collect { summary ->
                _uiState.update { it.copy(roomPollSummary = summary) }
            }
        }
    }

    fun fetchAiInsight() {
        val match = _uiState.value.match ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val insight = repository.fetchAiMatchInsight(match.homeTeam, match.awayTeam, match.league)
            _uiState.update { it.copy(aiInsight = insight, isAiLoading = false) }
        }
    }

    fun fetchAiRiskAssessment() {
        val match = _uiState.value.match ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRiskLoading = true) }
            val assessment = runCatching { repository.fetchAiRiskAssessment(match) }
                .getOrElse { AiRiskAssessment() }
            _uiState.update { it.copy(aiRiskAssessment = assessment, isRiskLoading = false) }
        }
    }

    fun selectChoice(choice: String) {
        _uiState.update {
            it.copy(
                selectedChoice = choice,
                predictionError = null,
                predictionAiExplanation = null
            )
        }
        fetchAiPredictionExplanation(choice)
    }

    private fun fetchAiPredictionExplanation(choice: String) {
        val match = _uiState.value.match ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiExplanationLoading = true) }
            val explanation = repository.fetchAiPredictionExplanation(
                match.homeTeam,
                match.awayTeam,
                match.league,
                choice
            )
            _uiState.update {
                it.copy(
                    predictionAiExplanation = explanation,
                    isAiExplanationLoading = false
                )
            }
        }
    }

    fun setBidAmount(amount: Float) {
        _uiState.update { it.copy(bidAmount = amount) }
    }

    fun postComment(matchId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.addCommentOnDetail(matchId, text) }
    }

    fun updateRoomInput(text: String) {
        _uiState.update { it.copy(roomInput = text, roomError = null) }
    }

    fun sendMatchRoomMessage(matchId: String) {
        val text = _uiState.value.roomInput.trim()
        if (text.isBlank()) return
        _uiState.update { it.copy(roomInput = "", roomError = null) }
        viewModelScope.launch {
            runCatching { repository.sendMatchRoomMessage(matchId, text) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(roomError = error.localizedMessage ?: "Unable to send room message")
                    }
                }
        }
    }

    fun voteMatchRoom(matchId: String, choice: String) {
        viewModelScope.launch {
            runCatching { repository.voteMatchRoom(matchId, choice) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(roomError = error.localizedMessage ?: "Unable to submit room vote")
                    }
                }
        }
    }

    fun placePrediction(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        val match = state.match ?: return onError("Không tìm thấy trận đấu")
        val choice = state.selectedChoice ?: return onError("Vui lòng chọn kết quả dự đoán")
        val tokens = state.bidAmount.toInt()
        if (match.statusText.isFinishedStatus()) {
            return onError("Trận đã kết thúc, không thể đặt dự đoán mới.")
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPredicting = true, predictionError = null) }
            try {
                val user = repository.getLoggedInUserDirect()
                    ?: return@launch run {
                        _uiState.update { it.copy(isPredicting = false) }
                        onError("Bạn chưa đăng nhập")
                    }

                if (user.tokenBalance < tokens) {
                    _uiState.update { it.copy(isPredicting = false) }
                    return@launch onError("Không đủ Token. Cần $tokens, hiện có ${user.tokenBalance}.")
                }

                val odds = when (choice) {
                    match.homeTeam -> match.homeOdds.toDoubleOrNull() ?: 2.0
                    match.awayTeam -> match.awayOdds.toDoubleOrNull() ?: 3.2
                    else -> match.drawOdds.toDoubleOrNull() ?: 3.4
                }
                val potentialReturn = (tokens * odds).toInt()

                val newPrediction = Prediction(
                    matchId = match.id,
                    matchTitle = "${match.homeTeam} vs ${match.awayTeam}",
                    league = match.league,
                    homeTeam = match.homeTeam,
                    awayTeam = match.awayTeam,
                    predictionType = "Match Winner",
                    yourChoice = choice,
                    tokensPlaced = tokens,
                    potentialReturn = potentialReturn,
                    status = "PENDING",
                    userEmail = user.email
                )

                repository.placePrediction(newPrediction)
                repository.updateProfile(user.copy(tokenBalance = user.tokenBalance - tokens))
                repository.recordTransaction(
                    "#PRED-${match.id}-${System.currentTimeMillis()}",
                    "Prediction Stake",
                    -tokens,
                    "SPEND"
                )
                repository.updateMissionProgress(user.email, "predict", 1)
                repository.createNotification(
                    user.email,
                    NotificationType.PREDICTION_RESULT,
                    "Dự đoán đã đặt",
                    "Bạn đã đặt $tokens token cho '$choice'."
                )

                _uiState.update {
                    it.copy(
                        isPredicting = false,
                        predictionSuccess = true,
                        alreadyPredicted = true
                    )
                }
                onSuccess(potentialReturn)
            } catch (e: Exception) {
                _uiState.update { it.copy(isPredicting = false) }
                onError("Lỗi: ${e.localizedMessage ?: "không xác định"}")
            }
        }
    }

    fun resetPredictionSuccess() {
        _uiState.update { it.copy(predictionSuccess = false) }
    }

    private fun String.isFinishedStatus(): Boolean {
        return equals("FT", ignoreCase = true) ||
            equals("AET", ignoreCase = true) ||
            equals("PEN", ignoreCase = true) ||
            contains("Finished", ignoreCase = true) ||
            contains("Ended", ignoreCase = true) ||
            contains("Full Time", ignoreCase = true) ||
            contains("Game Over", ignoreCase = true)
    }
}
