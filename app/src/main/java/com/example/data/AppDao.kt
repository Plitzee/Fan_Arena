package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getUserCount(): Int

    @Query("UPDATE user_profiles SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    // Matches
    @Query("SELECT * FROM matches")
    fun getAllMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: Long): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("DELETE FROM matches WHERE id BETWEEN 901000000 AND 901000999")
    suspend fun deleteSeededDemoMatches()

    @Query("SELECT COUNT(*) FROM matches")
    suspend fun getMatchCount(): Int

    @Update
    suspend fun updateMatch(match: MatchEntity)

    // Predictions - Cập nhật để lọc theo User
    @Query("SELECT * FROM predictions WHERE matchId = :matchId AND userEmail = :email AND status = 'PENDING'")
    suspend fun getPendingPredictionForUser(matchId: Long, email: String): Prediction?

    @Query("SELECT * FROM predictions WHERE matchId = :matchId AND status = 'PENDING'")
    suspend fun getPendingPredictionsForMatch(matchId: Long): List<Prediction>

    @Query("SELECT * FROM predictions WHERE id = :id LIMIT 1")
    suspend fun getPredictionById(id: Long): Prediction?

    @Query("SELECT * FROM predictions")
    fun getAllPredictionsFlow(): Flow<List<Prediction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction)

    @Update
    suspend fun updatePrediction(prediction: Prediction)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT * FROM api_sync_state WHERE id = :id LIMIT 1")
    fun getApiSyncStateFlow(id: String = "sports"): Flow<ApiSyncState?>

    @Query("SELECT * FROM api_sync_state WHERE id = :id LIMIT 1")
    suspend fun getApiSyncState(id: String = "sports"): ApiSyncState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApiSyncState(state: ApiSyncState)

    // Favorites
    @Query("SELECT * FROM favorite_teams ORDER BY teamName")
    fun getFavoriteTeamsFlow(): Flow<List<FavoriteTeam>>

    @Query("SELECT * FROM favorite_leagues ORDER BY leagueName")
    fun getFavoriteLeaguesFlow(): Flow<List<FavoriteLeague>>

    @Query("SELECT * FROM favorite_teams WHERE teamId = :teamId LIMIT 1")
    suspend fun getFavoriteTeam(teamId: Int): FavoriteTeam?

    @Query("SELECT * FROM favorite_leagues WHERE leagueId = :leagueId LIMIT 1")
    suspend fun getFavoriteLeague(leagueId: Int): FavoriteLeague?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteTeam(team: FavoriteTeam)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteLeague(league: FavoriteLeague)

    @Delete
    suspend fun deleteFavoriteTeam(team: FavoriteTeam)

    @Delete
    suspend fun deleteFavoriteLeague(league: FavoriteLeague)

    // Comments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("SELECT * FROM reports WHERE status = 'PENDING'")
    fun getPendingReportsFlow(): Flow<List<Report>>
}
