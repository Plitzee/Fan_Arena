package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.database.PropertyName
import com.google.firebase.database.IgnoreExtraProperties
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@IgnoreExtraProperties
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String = "",
    val fullName: String = "",
    val password: String = "huy123", 
    val tokenBalance: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val favoriteSports: String = "",
    val avatarUrl: String = "https://api.dicebear.com/7.x/avataaars/svg?seed=FanArenaUser",
    val bio: String = "FanArena Legend",
    val lastCheckIn: Long = 0,
    
    // Sửa lỗi mapping Firebase: Tránh lỗi "no field loggedIn found"
    @get:PropertyName("isLoggedIn")
    @set:PropertyName("isLoggedIn")
    var isLoggedIn: Boolean = false,
    
    val unlockedBadges: List<String> = emptyList()
) {
    // Constructor rỗng bắt buộc cho Firebase
    constructor() : this("")
}

data class Badge(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconName: String = "",
    val requirement: String = ""
)

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: Long = 0,
    val matchTitle: String = "",
    val league: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val predictionType: String = "",
    val yourChoice: String = "",
    val tokensPlaced: Int = 0,
    val possibleXpReward: Int = 0,
    val potentialReturn: Int = 0,
    val status: String = "PENDING", 
    val matchResult: String = "",
    val userEmail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String = "", 
    val authorEmail: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val authorTitle: String = "FanArena Fan",
    val postText: String = "",
    val imageUrl: String = "", 
    val sportCategory: String = "General",
    val visibility: String = "Public",
    val likesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val shareCount: Int = 0,
    val rewardedTokens: Int = 0,
    val hashtags: String = "",
    val qualityPercent: Int = 50,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String = "",
    val postId: String = "", 
    val authorEmail: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "https://ui-avatars.com/api/?background=random", 
    val text: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Long = 0,
    val league: String = "",
    val leagueId: Int = 0,
    val homeTeam: String = "",
    val homeTeamId: Int = 0,
    val awayTeam: String = "",
    val awayTeamId: Int = 0,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val statusText: String = "",
    val stadium: String = "",
    val scheduleTime: String = "",
    val sport: String = "Football",
    val homeLogoUrl: String = "",
    val awayLogoUrl: String = "",
    val isActivePrediction: Boolean = true,
    val homeOdds: String = "2.10",
    val drawOdds: String = "3.40",
    val awayOdds: String = "3.20"
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String = "",
    val title: String = "",
    val amount: Int = 0,
    val type: String = "EARN", 
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firestoreId: String = "",
    val authorName: String = "",
    val text: String = "",
    val category: String = "",
    val aiConfidence: Int = 0,
    val status: String = "PENDING",
    val isPost: Boolean = true,
    val originalId: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_sync_state")
data class ApiSyncState(
    @PrimaryKey val id: String = "sports",
    val source: String = "DEMO",
    val isLoading: Boolean = false,
    val lastRefreshAt: Long = 0,
    val lastSuccessAt: Long = 0,
    val insertedCount: Int = 0,
    val message: String = "Using demo fixtures"
)

@Entity(tableName = "favorite_teams")
data class FavoriteTeam(
    @PrimaryKey val teamId: Int = 0,
    val teamName: String = "",
    val teamLogo: String = ""
)

@Entity(tableName = "favorite_leagues")
data class FavoriteLeague(
    @PrimaryKey val leagueId: Int = 0,
    val leagueName: String = "",
    val leagueLogo: String = ""
)

enum class NotificationType { MATCH_REMINDER, PREDICTION_RESULT, SOCIAL_INTERACTION, LEVEL_UP, DAILY_CHECKIN, MISSION_COMPLETED }

data class NotificationItem(
    val notificationId: String = "",
    val type: NotificationType = NotificationType.SOCIAL_INTERACTION,
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val referenceId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class MatchRoomMessage(
    val messageId: String = "",
    val matchId: String = "",
    val authorEmail: String = "",
    val authorName: String = "Fan",
    val authorAvatarUrl: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class MatchRoomPollSummary(
    val homeVotes: Int = 0,
    val awayVotes: Int = 0,
    val drawVotes: Int = 0,
    val participants: Int = 0
) {
    val totalVotes: Int
        get() = homeVotes + awayVotes + drawVotes

    fun percentFor(votes: Int): Int = if (totalVotes == 0) 0 else (votes * 100 / totalVotes)
}

data class UserMission(val missionId: String = "", val currentProgress: Int = 0, val targetValue: Int = 1, val isCompleted: Boolean = false)

data class UserStats(val followersCount: Int = 0, val followingCount: Int = 0, val postsCount: Int = 0)

class FanArenaConverters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String = adapter.toJson(value ?: emptyList())

    @TypeConverter
    fun toStringList(value: String): List<String>? = adapter.fromJson(value)
}
