package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApiSyncState
import com.example.data.AppDatabase
import com.example.data.Badge
import com.example.data.Comment
import com.example.data.FanArenaRepository
import com.example.data.MatchEntity
import com.example.data.Post
import com.example.data.Prediction
import com.example.data.Report
import com.example.data.Transaction
import com.example.data.UserMission
import com.example.data.UserProfile
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DashboardAiState(
    val recommendedMatchId: Long? = null,
    val recommendationReason: String = "AI đang phân tích dữ liệu trận đấu...",
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class FanArenaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FanArenaRepository(database.appDao())

    val loggedInUser: StateFlow<UserProfile?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val matches: StateFlow<List<MatchEntity>> = repository.matches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val posts: StateFlow<List<Post>> = repository.posts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apiSyncState: StateFlow<ApiSyncState?> = repository.apiSyncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userMissions: StateFlow<List<UserMission>> = loggedInUser
        .flatMapLatest { user ->
            user?.let { repository.getUserMissionsFlow(it.email) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val predictions: StateFlow<List<Prediction>> = repository.predictions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followingList: StateFlow<List<String>> = loggedInUser
        .flatMapLatest { user ->
            user?.let { repository.getFollowingList(it.email) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReports: StateFlow<List<Report>> = repository.pendingReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBadges = listOf(
        Badge("rookie", "Rookie Predictor", "Đặt 5 dự đoán đầu tiên", "🎯"),
        Badge("streak3", "Winning Streak", "Thắng 3 trận liên tiếp", "🔥"),
        Badge("loyal", "Daily Legend", "Điểm danh 7 ngày liên tục", "📅"),
        Badge("social", "Social Butterfly", "Tạo 10 bài viết cộng đồng", "💬"),
        Badge("predictor", "Match Analyst", "Sử dụng AI phân tích 5 lần", "🧠"),
        Badge("whale", "Token Whale", "Sở hữu hơn 10,000 Tokens", "💰")
    )

    private val _aiRecommendation = MutableStateFlow(DashboardAiState())
    val aiRecommendation = _aiRecommendation.asStateFlow()

    private var verificationId: String? = null

    init {
        viewModelScope.launch {
            repository.seedSampleData()
            repository.refreshSportsFromConfiguredApi()
            matches.filter { it.isNotEmpty() }.first().let { list ->
                generateDashboardRecommendation(list.first())
            }
        }
    }

    fun refreshSports(force: Boolean = true) {
        viewModelScope.launch {
            repository.refreshSportsFromConfiguredApi(force = force)
        }
    }

    private fun generateDashboardRecommendation(match: MatchEntity) {
        viewModelScope.launch {
            _aiRecommendation.update {
                it.copy(
                    recommendedMatchId = match.id,
                    isLoading = true,
                    recommendationReason = "AI đang phân tích: ${match.homeTeam} vs ${match.awayTeam}..."
                )
            }
            try {
                val insight = com.example.ai.GeminiService.getMatchInsight(
                    match.homeTeam,
                    match.awayTeam,
                    match.league
                )
                _aiRecommendation.update { it.copy(recommendationReason = insight, isLoading = false) }
            } catch (_: Exception) {
                _aiRecommendation.update {
                    it.copy(
                        recommendationReason = "AI Spotlight: Trận cầu tâm điểm với nhiều điểm nóng chiến thuật.",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleLikeOnPost(post: Post) {
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect() ?: return@launch
            repository.toggleLike(post.firestoreId, user.email)
        }
    }

    fun sharePost(post: Post) {
        viewModelScope.launch {
            repository.sharePost(post.firestoreId)
        }
    }

    fun followUser(targetEmail: String) {
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect() ?: return@launch
            repository.followUser(user.email, targetEmail)
        }
    }

    fun unfollowUser(targetEmail: String) {
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect() ?: return@launch
            repository.unfollowUser(user.email, targetEmail)
        }
    }

    fun triggerDailyCheckIn(onAlreadyCheckedIn: () -> Unit, onBonusClaimed: (Int) -> Unit) {
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect() ?: return@launch
            val current = System.currentTimeMillis()
            val alreadyCheckedIn = user.lastCheckIn > 0 &&
                Calendar.getInstance().apply { timeInMillis = user.lastCheckIn }.let { last ->
                    Calendar.getInstance().let { now ->
                        last.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            last.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                    }
                }
            if (alreadyCheckedIn) {
                onAlreadyCheckedIn()
                return@launch
            }
            repository.updateProfile(user.copy(tokenBalance = user.tokenBalance + 100, lastCheckIn = current))
            repository.recordTransaction("daily-checkin-$current", "Daily Reward", 100, "EARN")
            onBonusClaimed(100)
        }
    }

    fun submitPost(text: String, category: String, imageUrl: String = "", hashtags: String = "", visibility: String = "Public") {
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect() ?: return@launch
            repository.createPost(
                Post(
                    authorName = user.fullName,
                    authorEmail = user.email,
                    authorAvatarUrl = user.avatarUrl,
                    postText = text,
                    sportCategory = category,
                    hashtags = hashtags,
                    imageUrl = imageUrl,
                    visibility = visibility
                )
            )
        }
    }

    fun addCommentOnPost(postId: String, text: String) {
        viewModelScope.launch { repository.addCommentOnDetail(postId, text) }
    }

    fun getCommentsForPost(postId: String): Flow<List<Comment>> = repository.getCommentsForPost(postId)

    fun addCommentOnMatch(matchId: String, text: String) {
        viewModelScope.launch { repository.addCommentOnDetail(matchId, text) }
    }

    fun getCommentsForMatch(matchId: String): Flow<List<Comment>> = repository.getCommentsForMatch(matchId)

    fun updateReportStatus(reportId: String, status: String) {
        viewModelScope.launch { repository.updateReportStatus(reportId, status) }
    }

    fun redeemShopItem(itemName: String, cost: Int, avatarUrl: String? = null, onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(repository.redeemShopItem(itemName, cost, avatarUrl)) }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch { repository.updateProfile(profile) }
    }

    fun resolvePredictionDemo(predictionId: Long, isWinner: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.resolvePrediction(predictionId, isWinner)
            onDone()
        }
    }

    suspend fun getImprovedPost(text: String, category: String): String {
        return com.example.ai.GeminiService.getImprovedPost(text, category)
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.loginWithPassword(email, pass)
            if (user != null) onSuccess() else onError("Đăng nhập thất bại")
        }
    }

    fun register(fullName: String, email: String, pass: String, sports: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.registerUser(email, pass, fullName, sports)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Đăng ký thất bại")
            }
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity, onCodeSent: () -> Unit, onError: (String) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(com.google.firebase.auth.FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) = Unit
                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.message ?: "Xác thực thất bại")
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@FanArenaViewModel.verificationId = verificationId
                    onCodeSent()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val id = verificationId
        if (id == null) {
            onError("Mã xác thực không hợp lệ")
            return
        }
        viewModelScope.launch {
            try {
                repository.verifyOtpAndLogin(id, code)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Mã OTP không đúng")
            }
        }
    }

    fun restoreSession() {
        viewModelScope.launch { repository.restoreUserSession() }
    }

    fun logout() {
        viewModelScope.launch { repository.logoutAll() }
    }
}
