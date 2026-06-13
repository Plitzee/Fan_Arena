package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FanArenaRepository(private val appDao: AppDao) {
    companion object {
        private const val SPORTS_SYNC_ID = "sports"
        private const val SPORTS_CACHE_WINDOW_MS = 30L * 60L * 1000L
    }

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance("https://sportbay-3f80a-default-rtdb.asia-southeast1.firebasedatabase.app/")
    
    private val usersRef = database.getReference("users")
    private val postsRef = database.getReference("posts")
    private val commentsRef = database.getReference("comments")
    private val reportsRef = database.getReference("reports")
    private val favoriteTeamsRef = database.getReference("favorite_teams")
    private val favoriteLeaguesRef = database.getReference("favorite_leagues")
    private val followsRef = database.getReference("follows")
    private val notificationsRef = database.getReference("notifications")
    private val userMissionsRef = database.getReference("user_missions")
    private val userStatsRef = database.getReference("user_stats")
    private val matchRoomsRef = database.getReference("match_rooms")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val matchApi = Retrofit.Builder()
        .baseUrl("https://v3.football.api-sports.io/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(MatchApiService::class.java)

    private val nbaApi = Retrofit.Builder()
        .baseUrl("https://v2.nba.api-sports.io/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(NbaApiService::class.java)

    private val basketballApi = Retrofit.Builder()
        .baseUrl("https://v1.basketball.api-sports.io/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(BasketballApiService::class.java)

    private val volleyballApi = Retrofit.Builder()
        .baseUrl("https://v1.volleyball.api-sports.io/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(VolleyballApiService::class.java)

    private val formulaOneApi = Retrofit.Builder()
        .baseUrl("https://v1.formula-1.api-sports.io/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(FormulaOneApiService::class.java)

    val loggedInUser: Flow<UserProfile?> = appDao.getLoggedInUserFlow()
    val predictions: Flow<List<Prediction>> = appDao.getAllPredictionsFlow()
    val matches: Flow<List<MatchEntity>> = appDao.getAllMatchesFlow()
    val transactions: Flow<List<Transaction>> = appDao.getAllTransactionsFlow()
    val apiSyncState: Flow<ApiSyncState?> = appDao.getApiSyncStateFlow(SPORTS_SYNC_ID)

    val posts: Flow<List<Post>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Post::class.java) }
                    .sortedByDescending { it.timestamp }
                trySend(items)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        postsRef.addValueEventListener(listener)
        awaitClose { postsRef.removeEventListener(listener) }
    }

    val pendingReports: Flow<List<Report>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                trySend(s.children.mapNotNull { it.getValue(Report::class.java) }.filter { it.status == "PENDING" })
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        reportsRef.addValueEventListener(listener)
        awaitClose { reportsRef.removeEventListener(listener) }
    }

    suspend fun seedSampleData() {
        try {
            val defaultAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=HuyPlitzee"
            if (appDao.getUserCount() == 0) {
                val testUser = UserProfile(
                    email = "plitzee@gmail.com",
                    fullName = "Huy Plitzee",
                    tokenBalance = 5100,
                    xp = 1200,
                    level = 5,
                    favoriteSports = "Football, Basketball",
                    avatarUrl = defaultAvatar,
                    bio = "Trading predictions, debating lineups, and stacking tokens.",
                    unlockedBadges = listOf("rookie", "predictor")
                )
                appDao.insertUserProfile(testUser.copy(isLoggedIn = true))
            } else {
                val currentUser = appDao.getLoggedInUser()
                if (currentUser != null && currentUser.avatarUrl.contains("ui-avatars.com")) {
                    appDao.insertUserProfile(currentUser.copy(avatarUrl = defaultAvatar))
                }
            }

            val seededMatches = listOf(
                MatchEntity(
                    id = 901000001,
                    league = "Premier League",
                    homeTeam = "Arsenal",
                    awayTeam = "Manchester City",
                    homeScore = 1,
                    awayScore = 2,
                    statusText = "LIVE 67'",
                    stadium = "Emirates Stadium",
                    scheduleTime = "Tonight 20:00",
                    sport = "Football",
                    homeLogoUrl = "https://media.api-sports.io/football/teams/42.png",
                    awayLogoUrl = "https://media.api-sports.io/football/teams/50.png",
                    homeOdds = "2.40",
                    drawOdds = "3.40",
                    awayOdds = "2.65",
                    isActivePrediction = true
                ),
                MatchEntity(
                    id = 901000002,
                    league = "UEFA Champions League",
                    homeTeam = "Inter",
                    awayTeam = "Milan",
                    homeScore = 0,
                    awayScore = 0,
                    statusText = "UPCOMING 23:45",
                    stadium = "San Siro",
                    scheduleTime = "Tomorrow 23:45",
                    sport = "Football",
                    homeLogoUrl = "https://media.api-sports.io/football/teams/505.png",
                    awayLogoUrl = "https://media.api-sports.io/football/teams/489.png",
                    homeOdds = "2.10",
                    drawOdds = "3.10",
                    awayOdds = "3.25",
                    isActivePrediction = true
                ),
                MatchEntity(
                    id = 901000007,
                    league = "ATP Finals",
                    homeTeam = "Novak Djokovic",
                    awayTeam = "Carlos Alcaraz",
                    homeScore = 1,
                    awayScore = 0,
                    statusText = "SET 2",
                    stadium = "Turin Arena",
                    scheduleTime = "Today 19:00",
                    sport = "Tennis",
                    homeLogoUrl = "https://api.dicebear.com/7.x/initials/svg?seed=ND",
                    awayLogoUrl = "https://api.dicebear.com/7.x/initials/svg?seed=CA",
                    homeOdds = "1.85",
                    drawOdds = "N/A",
                    awayOdds = "1.95",
                    isActivePrediction = true
                ),
                MatchEntity(
                    id = 901000008,
                    league = "NBA Regular Season",
                    homeTeam = "LA Lakers",
                    awayTeam = "GS Warriors",
                    homeScore = 102,
                    awayScore = 98,
                    statusText = "Q4 02:30",
                    stadium = "Crypto Arena",
                    scheduleTime = "Tonight 02:00",
                    sport = "NBA",
                    homeLogoUrl = "https://api.dicebear.com/7.x/initials/svg?seed=LAL",
                    awayLogoUrl = "https://api.dicebear.com/7.x/initials/svg?seed=GSW",
                    homeOdds = "1.70",
                    drawOdds = "N/A",
                    awayOdds = "2.20",
                    isActivePrediction = true
                ),
                MatchEntity(
                    id = 901000009,
                    league = "Grand Slam",
                    homeTeam = "Iga Swiatek",
                    awayTeam = "Aryna Sabalenka",
                    homeScore = 0,
                    awayScore = 0,
                    statusText = "UPCOMING",
                    stadium = "Court Central",
                    scheduleTime = "Tomorrow 15:00",
                    sport = "Tennis",
                    homeLogoUrl = "https://api.dicebear.com/7.x/initials/svg?seed=IS",
                    awayLogoUrl = "https://api.dicebear.com/7.x/initials/svg?seed=AS",
                    homeOdds = "1.65",
                    drawOdds = "N/A",
                    awayOdds = "2.25",
                    isActivePrediction = true
                )
            )
            storeMatchesAndResolve(seededMatches)

            if (appDao.getTransactionCount() == 0) {
                appDao.insertTransaction(Transaction(transactionId = "seed-topup-1", title = "Welcome bonus", amount = 500, type = "EARN"))
            }

            val seedPosts = listOf(
                Post(
                    firestoreId = "seed-post-1",
                    authorEmail = "huy.fan@fanarena.app",
                    authorName = "Huy Fan",
                    authorAvatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=HuyFan",
                    postText = "Champions League night is heating up. Arsenal fans, are you backing a late comeback?",
                    imageUrl = "https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?auto=format&fit=crop&w=1200&q=80",
                    sportCategory = "Football",
                    likesCount = 45,
                    commentsCount = 5,
                    rewardedTokens = 10,
                    hashtags = "#PremierLeague #Derby"
                ),
                Post(
                    firestoreId = "seed-post-2",
                    authorEmail = "minh.nba@fanarena.app",
                    authorName = "Minh Courtside",
                    authorAvatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=MinhNBA",
                    postText = "The Lakers vs Warriors matchup still feels like playoff basketball. Pace and turnovers will decide it.",
                    imageUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=1200&q=80",
                    sportCategory = "NBA",
                    likesCount = 31,
                    commentsCount = 3,
                    hashtags = "#NBA #Lakers #Warriors"
                ),
                Post(
                    firestoreId = "seed-post-3",
                    authorEmail = "linh.f1@fanarena.app",
                    authorName = "Linh GP",
                    authorAvatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=LinhGP",
                    postText = "Formula 1 qualifying is where the real pressure starts. Track position could matter more than raw race pace.",
                    imageUrl = "https://images.unsplash.com/photo-1503736334956-4c8f8e92946d?auto=format&fit=crop&w=1200&q=80",
                    sportCategory = "Formula 1",
                    likesCount = 27,
                    commentsCount = 2,
                    hashtags = "#F1 #Qualifying"
                ),
                Post(
                    firestoreId = "seed-post-4",
                    authorEmail = "thao.volley@fanarena.app",
                    authorName = "Thao Volley",
                    authorAvatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=ThaoVolley",
                    postText = "Volleyball momentum can flip in one rotation. I am watching serve receive and block timing today.",
                    imageUrl = "https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?auto=format&fit=crop&w=1200&q=80",
                    sportCategory = "Volleyball",
                    likesCount = 18,
                    commentsCount = 1,
                    hashtags = "#Volleyball"
                )
            )
            seedPosts.forEach { post ->
                postsRef.child(post.firestoreId).setValue(post).await()
            }
        } catch (e: Exception) { Log.e("FanArena", "Seed error", e) }
    }

    suspend fun loginWithPassword(email: String, pass: String): UserProfile? {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            val safeEmail = email.replace(".", "_")
            val snapshot = usersRef.child(safeEmail).get().await()
            val user = snapshot.getValue(UserProfile::class.java)
            if (user != null) {
                appDao.logoutAllUsers()
                appDao.insertUserProfile(user.copy(isLoggedIn = true))
            }
            user
        } catch (e: Exception) {
            if (email == "plitzee@gmail.com" && pass == "huy123") {
                val fallbackUser = UserProfile(
                    email = email,
                    fullName = "Huy Plitzee",
                    isLoggedIn = true,
                    tokenBalance = 5100,
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=HuyPlitzee",
                    unlockedBadges = listOf("rookie", "predictor")
                )
                appDao.insertUserProfile(fallbackUser)
                return fallbackUser
            }
            null
        }
    }

    suspend fun registerUser(email: String, password: String, fullName: String, sports: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
        val newUser = UserProfile(
            email = email,
            fullName = fullName,
            favoriteSports = sports,
            tokenBalance = 500,
            avatarUrl = generatedUserAvatarUrl(fullName)
        )
        usersRef.child(email.replace(".", "_")).setValue(newUser).await()
        appDao.insertUserProfile(newUser)
    }

    suspend fun verifyOtpAndLogin(verificationId: String, code: String): UserProfile? {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential).await()
        return restoreUserSession()
    }

    suspend fun restoreUserSession(): UserProfile? {
        val current = auth.currentUser ?: return null
        val safeEmail = current.email?.replace(".", "_") ?: (current.phoneNumber?.replace("+", "p") ?: "unknown_user")
        val user = usersRef.child(safeEmail).get().await().getValue(UserProfile::class.java)
        if (user != null) {
            appDao.insertUserProfile(user.copy(isLoggedIn = true))
        } else {
            // Create a basic profile for first-time phone login
            val newUser = UserProfile(
                email = current.email ?: (current.phoneNumber ?: ""),
                fullName = current.displayName ?: "Fan User",
                tokenBalance = 500,
                avatarUrl = generatedUserAvatarUrl(current.displayName ?: current.email ?: current.phoneNumber ?: "Fan User"),
                isLoggedIn = true
            )
            usersRef.child(safeEmail).setValue(newUser).await()
            appDao.insertUserProfile(newUser)
            return newUser
        }
        return user
    }

    suspend fun updateProfile(profile: UserProfile) {
        appDao.insertUserProfile(profile)
        usersRef.child(profile.email.replace(".", "_")).setValue(profile).await()
    }

    suspend fun createPost(post: Post) {
        val key = postsRef.push().key ?: return
        postsRef.child(key).setValue(post.copy(firestoreId = key)).await()
    }

    suspend fun toggleLike(postId: String, userEmail: String) {
        if (postId.isBlank() || userEmail.isBlank()) return
        try {
            val snapshot = postsRef.child(postId).get().await()
            val post = snapshot.getValue(Post::class.java) ?: return
            val updatedLikedBy = post.likedBy.toMutableList()
            val liked = updatedLikedBy.contains(userEmail)
            if (liked) {
                updatedLikedBy.remove(userEmail)
            } else {
                updatedLikedBy.add(userEmail)
            }
            val updatedPost = post.copy(
                likedBy = updatedLikedBy,
                likesCount = updatedLikedBy.size
            )
            postsRef.child(postId).setValue(updatedPost).await()
            if (!liked && post.authorEmail.isNotBlank() && post.authorEmail != userEmail) {
                createNotification(
                    post.authorEmail,
                    NotificationType.SOCIAL_INTERACTION,
                    "New like",
                    "$userEmail liked your post.",
                    postId
                )
            }
        } catch (e: Exception) {
            Log.e("FanArena", "Toggle like failed", e)
        }
    }

    suspend fun sharePost(postId: String) {
        if (postId.isBlank()) return
        try {
            val snapshot = postsRef.child(postId).get().await()
            val post = snapshot.getValue(Post::class.java) ?: return
            postsRef.child(postId).child("shareCount").setValue(post.shareCount + 1).await()
        } catch (e: Exception) {
            Log.e("FanArena", "Share post failed", e)
        }
    }

    suspend fun followUser(myEmail: String, targetEmail: String) {
        followsRef.child(myEmail.replace(".", "_")).child(targetEmail.replace(".", "_")).setValue(true).await()
        updateMissionProgress(myEmail, "follow", 1)
    }

    suspend fun unfollowUser(myEmail: String, targetEmail: String) {
        followsRef.child(myEmail.replace(".", "_")).child(targetEmail.replace(".", "_")).removeValue().await()
    }

    fun getFollowingList(myEmail: String): Flow<List<String>> = callbackFlow {
        val ref = followsRef.child(myEmail.replace(".", "_"))
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { trySend(s.children.mapNotNull { it.key?.replace("_", ".") }) }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getAllUsers(): List<UserProfile> = usersRef.get().await().children.mapNotNull { it.getValue(UserProfile::class.java) }

    suspend fun refreshSportsFromConfiguredApi(force: Boolean = false) {
        val now = System.currentTimeMillis()
        val cachedState = appDao.getApiSyncState(SPORTS_SYNC_ID)
        if (!force && cachedState?.source == "LIVE" && now - cachedState.lastSuccessAt < SPORTS_CACHE_WINDOW_MS) {
            appDao.upsertApiSyncState(
                cachedState.copy(
                    isLoading = false,
                    message = "Live data cached. Next refresh is available in ${minutesUntilNextRefresh(cachedState.lastSuccessAt)} min."
                )
            )
            return
        }

        val apiKey = readBuildConfigString("APISPORTS_API_KEY")
        if (apiKey.isBlank() || apiKey.contains("YOUR_API", ignoreCase = true)) {
            Log.i("FanArena", "APISPORTS_API_KEY is not configured; using seeded fixtures.")
            appDao.upsertApiSyncState(
                ApiSyncState(
                    id = SPORTS_SYNC_ID,
                    source = "DEMO",
                    isLoading = false,
                    lastRefreshAt = now,
                    message = "API key is not configured. Showing demo fixtures."
                )
            )
            return
        }
        appDao.upsertApiSyncState(
            (cachedState ?: ApiSyncState(id = SPORTS_SYNC_ID)).copy(
                isLoading = true,
                lastRefreshAt = now,
                message = "Refreshing live sports data..."
            )
        )

        val totalInserted = try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            fetchFootballFixtures(apiKey, today) +
                fetchNbaGames(apiKey, today) +
                fetchBasketballGames(apiKey, today) +
                fetchVolleyballGames(apiKey, today) +
                fetchFormulaOneRaces(apiKey)
        } catch (e: Exception) {
            Log.e("FanArena", "Sports API refresh failed", e)
            0
        }

        val success = totalInserted > 0
        if (success) {
            appDao.deleteSeededDemoMatches()
        }
        appDao.upsertApiSyncState(
            ApiSyncState(
                id = SPORTS_SYNC_ID,
                source = if (success) "LIVE" else "DEMO",
                isLoading = false,
                lastRefreshAt = now,
                lastSuccessAt = if (success) now else (cachedState?.lastSuccessAt ?: 0),
                insertedCount = totalInserted,
                message = if (success) {
                    "Live data updated: $totalInserted fixtures loaded."
                } else {
                    "API returned no live fixtures now. Showing demo fixtures."
                }
            )
        )
    }

    private fun minutesUntilNextRefresh(lastSuccessAt: Long): Long {
        val remaining = (SPORTS_CACHE_WINDOW_MS - (System.currentTimeMillis() - lastSuccessAt)).coerceAtLeast(0)
        return ((remaining + 59_999L) / 60_000L).coerceAtLeast(1)
    }

    private fun readBuildConfigString(name: String): String {
        return try {
            val field = Class.forName("com.example.BuildConfig").getField(name)
            (field.get(null) as? String).orEmpty().trim().removeSurrounding("\"")
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun fetchFootballFixtures(apiKey: String, date: String): Int {
        return try {
            val liveResponse = runCatching { matchApi.getLiveMatches(apiKey) }.getOrNull()
            val dateResponse = matchApi.getFixturesByDate(apiKey, date)
            val remotes = ((liveResponse?.response ?: emptyList()) + dateResponse.response)
                .distinctBy { it.fixture.id }
                .take(30)
            val response = MatchResponse(remotes)
            val entities = response.response.map { remote ->
                MatchEntity(
                    id = remote.fixture.id, league = remote.league.name,
                    homeTeam = remote.teams.home.name, awayTeam = remote.teams.away.name,
                    homeScore = remote.goals.home ?: 0, awayScore = remote.goals.away ?: 0,
                    statusText = remote.fixture.status.short,
                    homeLogoUrl = logoOrFallback(remote.teams.home.logo, remote.teams.home.name, "Football"),
                    awayLogoUrl = logoOrFallback(remote.teams.away.logo, remote.teams.away.name, "Football"),
                    sport = "Football"
                )
            }
            storeMatchesAndResolve(entities)
            entities.size
        } catch (e: Exception) {
            Log.e("FanArena", "Football API refresh failed", e)
            0
        }
    }

    suspend fun fetchNbaGames(apiKey: String, date: String): Int {
        return try {
            val liveResponse = runCatching { nbaApi.getLiveGames(apiKey) }.getOrNull()
            val dateResponse = nbaApi.getGamesByDate(apiKey, date)
            val games = ((liveResponse?.response ?: emptyList()) + dateResponse.response)
                .distinctBy { it.id }
                .take(30)
            val entities = games.mapNotNull { game ->
                val home = game.teams?.home
                val away = game.teams?.visitors
                if (home?.name.isNullOrBlank() || away?.name.isNullOrBlank()) return@mapNotNull null
                val scoreHome = game.scores?.home?.points ?: 0
                val scoreAway = game.scores?.visitors?.points ?: 0
                val statusText = when {
                    !game.status?.clock.isNullOrBlank() -> game.status?.clock ?: "LIVE"
                    !game.status?.long.isNullOrBlank() -> game.status?.long ?: "LIVE"
                    else -> "LIVE"
                }
                MatchEntity(
                    id = 2_000_000_000L + game.id,
                    league = game.league ?: "NBA",
                    homeTeam = home?.name ?: "Home",
                    awayTeam = away?.name ?: "Away",
                    homeScore = scoreHome,
                    awayScore = scoreAway,
                    statusText = statusText,
                    scheduleTime = game.date?.start ?: "",
                    sport = "NBA",
                    homeLogoUrl = logoOrFallback(home?.logo, home?.name ?: "Home", "NBA"),
                    awayLogoUrl = logoOrFallback(away?.logo, away?.name ?: "Away", "NBA"),
                    drawOdds = "N/A",
                    homeOdds = "1.90",
                    awayOdds = "1.90"
                )
            }
            storeMatchesAndResolve(entities)
            entities.size
        } catch (e: Exception) {
            Log.e("FanArena", "NBA API refresh failed", e)
            0
        }
    }

    suspend fun fetchBasketballGames(apiKey: String, date: String): Int {
        return try {
            val response = basketballApi.getGamesByDate(apiKey, date)
            val entities = response.response
                .distinctBy { it.id }
                .take(30)
                .mapNotNull { it.toMatchEntity("Basketball", 3_000_000_000L) }
            storeMatchesAndResolve(entities)
            entities.size
        } catch (e: Exception) {
            Log.e("FanArena", "Basketball API refresh failed", e)
            0
        }
    }

    suspend fun fetchVolleyballGames(apiKey: String, date: String): Int {
        return try {
            val response = volleyballApi.getGamesByDate(apiKey, date)
            val entities = response.response
                .distinctBy { it.id }
                .take(30)
                .mapNotNull { it.toMatchEntity("Volleyball", 4_000_000_000L) }
            storeMatchesAndResolve(entities)
            entities.size
        } catch (e: Exception) {
            Log.e("FanArena", "Volleyball API refresh failed", e)
            0
        }
    }

    suspend fun fetchFormulaOneRaces(apiKey: String): Int {
        return try {
            val season = latestFormulaOneFreeSeason()
            val response = formulaOneApi.getRacesBySeason(apiKey, season)
            val raceEvents = response.response.filter { it.type.equals("Race", ignoreCase = true) }
                .ifEmpty { response.response }
            val entities = raceEvents
                .distinctBy { it.id }
                .take(10)
                .map { race ->
                    val competition = "${race.competition?.name ?: "Formula 1"} $season"
                    val circuit = race.circuit?.name ?: "Grand Prix"
                    val location = listOfNotNull(
                        race.competition?.location?.city,
                        race.competition?.location?.country
                    ).joinToString(", ")
                    MatchEntity(
                        id = 5_000_000_000L + race.id,
                        league = competition,
                        homeTeam = race.type ?: "Race",
                        awayTeam = circuit,
                        homeScore = 0,
                        awayScore = 0,
                        statusText = race.status ?: "Scheduled",
                        stadium = location,
                        scheduleTime = race.date ?: "",
                        sport = "Formula 1",
                        homeLogoUrl = generatedInitialLogoUrl("F1 ${race.type ?: "Race"}", "Formula 1"),
                        awayLogoUrl = logoOrFallback(race.circuit?.image, circuit, "Formula 1"),
                        drawOdds = "N/A",
                        homeOdds = "1.00",
                        awayOdds = "1.00"
                    )
                }
            storeMatchesAndResolve(entities)
            entities.size
        } catch (e: Exception) {
            Log.e("FanArena", "Formula 1 API refresh failed", e)
            0
        }
    }

    private fun latestFormulaOneFreeSeason(): Int {
        val currentYear = SimpleDateFormat("yyyy", Locale.US).format(Date()).toIntOrNull() ?: 2024
        return currentYear.coerceAtMost(2024).coerceAtLeast(2022)
    }

    private fun ApiSportsGame.toMatchEntity(sportName: String, idOffset: Long): MatchEntity? {
        val home = teams?.home
        val away = teams?.away
        if (home?.name.isNullOrBlank() || away?.name.isNullOrBlank()) return null
        val statusText = status?.short ?: status?.long ?: "Scheduled"
        return MatchEntity(
            id = idOffset + id,
            league = league?.name ?: sportName,
            leagueId = league?.id ?: 0,
            homeTeam = home?.name ?: "Home",
            homeTeamId = home?.id ?: 0,
            awayTeam = away?.name ?: "Away",
            awayTeamId = away?.id ?: 0,
            homeScore = scores?.home?.total ?: scores?.home?.points ?: 0,
            awayScore = scores?.away?.total ?: scores?.away?.points ?: 0,
            statusText = statusText,
            stadium = country?.name ?: "",
            scheduleTime = date ?: time ?: "",
            sport = sportName,
            homeLogoUrl = logoOrFallback(home?.logo, home?.name ?: "Home", sportName),
            awayLogoUrl = logoOrFallback(away?.logo, away?.name ?: "Away", sportName),
            drawOdds = "N/A",
            homeOdds = "1.90",
            awayOdds = "1.90"
        )
    }

    private fun logoOrFallback(logoUrl: String?, name: String, sport: String): String {
        return logoUrl?.takeIf { it.isNotBlank() } ?: generatedInitialLogoUrl(name, sport)
    }

    private fun generatedInitialLogoUrl(name: String, sport: String): String {
        val seed = URLEncoder.encode("${sport.ifBlank { "Sport" }}-${name.ifBlank { "Team" }}", "UTF-8")
        return "https://api.dicebear.com/7.x/initials/svg?seed=$seed&backgroundColor=1d4ed8,0f766e,f59e0b&textColor=ffffff&fontWeight=700"
    }

    private fun generatedUserAvatarUrl(name: String): String {
        val seed = URLEncoder.encode(name.ifBlank { "FanArena User" }, "UTF-8")
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=$seed&backgroundColor=1d4ed8,0f766e,f59e0b"
    }

    private suspend fun storeMatchesAndResolve(matches: List<MatchEntity>) {
        if (matches.isEmpty()) return
        appDao.insertMatches(matches)
        autoResolveFinishedMatches(matches)
    }

    private suspend fun autoResolveFinishedMatches(matches: List<MatchEntity>) {
        matches.filter { it.isFinishedForResolution() }.forEach { match ->
            resolveFinishedMatchPredictions(match)
        }
    }

    suspend fun placePrediction(prediction: Prediction) { appDao.insertPrediction(prediction) }
    suspend fun getLoggedInUserDirect() = appDao.getLoggedInUser()
    suspend fun getMatchById(id: Long) = appDao.getMatchById(id)
    suspend fun getPendingPredictionsForMatch(matchId: Long) = appDao.getPendingPredictionsForMatch(matchId)
    
    fun getCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val ref = commentsRef.child(postId)
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { trySend(s.children.mapNotNull { it.getValue(Comment::class.java) }) }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getNotificationsFlow(email: String): Flow<List<NotificationItem>> = callbackFlow {
        val ref = notificationsRef.child(email.replace(".", "_"))
        val listener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) { trySend(s.children.mapNotNull { it.getValue(NotificationItem::class.java) }) }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun markNotificationAsRead(email: String, id: String) {
        notificationsRef.child(email.replace(".", "_")).child(id).child("isRead").setValue(true).await()
    }

    suspend fun createNotification(
        email: String,
        type: NotificationType,
        title: String,
        message: String,
        referenceId: String = ""
    ) {
        val userNotificationsRef = notificationsRef.child(email.replace(".", "_"))
        val notificationId = userNotificationsRef.push().key ?: return
        val notification = NotificationItem(
            notificationId = notificationId,
            type = type,
            title = title,
            message = message,
            referenceId = referenceId
        )
        userNotificationsRef.child(notificationId).setValue(notification).await()
    }

    suspend fun addCommentOnDetail(postId: String, text: String, imageUrl: String = "") {
        val user = appDao.getLoggedInUser()
        val comment = Comment(
            postId = postId,
            authorName = user?.fullName ?: "Fan",
            authorEmail = user?.email ?: "",
            authorAvatarUrl = user?.avatarUrl ?: generatedUserAvatarUrl(user?.fullName ?: "Fan"),
            text = text,
            imageUrl = imageUrl
        )
        commentsRef.child(postId).push().setValue(comment).await()
        try {
            val snapshot = postsRef.child(postId).get().await()
            val post = snapshot.getValue(Post::class.java)
            if (post != null) {
                postsRef.child(postId).child("commentsCount").setValue(post.commentsCount + 1).await()
            }
        } catch (e: Exception) {
            Log.e("FanArena", "Comment count update failed", e)
        }
    }

    fun getCommentsForMatch(matchId: String): Flow<List<Comment>> = getCommentsForPost(matchId)

    fun getMatchRoomMessages(matchId: String): Flow<List<MatchRoomMessage>> {
        return callbackFlow {
            val ref = matchRoomsRef.child(matchId).child("messages").limitToLast(50)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull { it.getValue(MatchRoomMessage::class.java) }
                        .sortedBy { it.createdAt }
                    trySend(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FanArena", "Match room messages cancelled", error.toException())
                    trySend(emptyList())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    fun getMatchRoomPollSummary(matchId: String): Flow<MatchRoomPollSummary> {
        return callbackFlow {
            val ref = matchRoomsRef.child(matchId).child("polls")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val choices = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    trySend(buildPollSummary(choices))
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FanArena", "Match room poll cancelled", error.toException())
                    trySend(MatchRoomPollSummary())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    suspend fun sendMatchRoomMessage(matchId: String, text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        val user = appDao.getLoggedInUser()
        val ref = matchRoomsRef.child(matchId).child("messages").push()
        val message = MatchRoomMessage(
            messageId = ref.key ?: "local-${System.currentTimeMillis()}",
            matchId = matchId,
            authorEmail = user?.email.orEmpty(),
            authorName = user?.fullName?.ifBlank { "Fan" } ?: "Fan",
            authorAvatarUrl = user?.avatarUrl?.takeIf { it.isNotBlank() }
                ?: generatedUserAvatarUrl(user?.fullName ?: "Fan"),
            text = cleanText
        )
        try {
            ref.setValue(message).await()
        } catch (e: Exception) {
            Log.e("FanArena", "Match room send failed", e)
            throw e
        }
    }

    suspend fun voteMatchRoom(matchId: String, choice: String) {
        val user = appDao.getLoggedInUser()
        val voterId = safeKey(user?.email?.takeIf { it.isNotBlank() } ?: "demo_user")
        val normalizedChoice = choice.uppercase(Locale.US)
        try {
            matchRoomsRef.child(matchId).child("polls").child(voterId).setValue(normalizedChoice).await()
        } catch (e: Exception) {
            Log.e("FanArena", "Match room vote failed", e)
            throw e
        }
    }

    suspend fun updateReportStatus(id: String, status: String) { reportsRef.child(id).child("status").setValue(status).await() }

    suspend fun resolveFinishedMatchPredictions(match: MatchEntity): Int {
        if (!match.isFinishedForResolution()) return 0
        val actualWinner = match.actualWinnerLabel() ?: return 0
        val resultLabel = "$actualWinner ${match.homeScore}-${match.awayScore}"
        val pending = appDao.getPendingPredictionsForMatch(match.id)
        pending.forEach { prediction ->
            // Real API resolution uses final score/status from MatchEntity. Demo Win/Lose buttons call the same resolver with a manual outcome.
            val isWinner = prediction.yourChoice.normalizedChoice() == actualWinner.normalizedChoice()
            resolvePredictionOutcome(prediction, resultLabel, isWinner, source = "AUTO")
        }
        return pending.size
    }

    suspend fun resolvePrediction(id: Long, isWinner: Boolean) {
        val prediction = appDao.getPredictionById(id) ?: return
        val demoResult = if (isWinner) {
            "Demo resolved: ${prediction.yourChoice}"
        } else {
            "Demo resolved: not ${prediction.yourChoice}"
        }
        resolvePredictionOutcome(prediction, demoResult, isWinner, source = "DEMO")
    }

    private suspend fun resolvePredictionOutcome(
        prediction: Prediction,
        matchResult: String,
        isWinner: Boolean,
        source: String
    ) {
        val latest = appDao.getPredictionById(prediction.id) ?: return
        if (latest.status != "PENDING") return

        val resolvedStatus = if (isWinner) "WON" else "LOST"
        appDao.updatePrediction(latest.copy(status = resolvedStatus, matchResult = matchResult))

        val user = appDao.getLoggedInUser()
        if (user != null && user.email == latest.userEmail) {
            if (isWinner) {
                val xpReward = latest.possibleXpReward.takeIf { it > 0 } ?: 25
                val nextStreak = user.currentStreak + 1
                runCatching {
                    updateProfile(
                        user.copy(
                            tokenBalance = user.tokenBalance + latest.potentialReturn,
                            xp = user.xp + xpReward,
                            currentStreak = nextStreak,
                            bestStreak = maxOf(user.bestStreak, nextStreak)
                        )
                    )
                }.onFailure { Log.e("FanArena", "Winner profile update failed", it) }
                runCatching {
                    recordTransaction(
                        "#WIN-${latest.id}-${System.currentTimeMillis()}",
                        "Prediction Win",
                        latest.potentialReturn,
                        "EARN"
                    )
                }
            } else {
                runCatching { updateProfile(user.copy(currentStreak = 0)) }
                    .onFailure { Log.e("FanArena", "Loser profile update failed", it) }
                runCatching {
                    recordTransaction(
                        "#LOSS-${latest.id}-${System.currentTimeMillis()}",
                        "Prediction Lost",
                        0,
                        "RESULT"
                    )
                }
            }
        }

        val title = if (isWinner) "Prediction won" else "Prediction lost"
        val message = if (isWinner) {
            "Your ${latest.matchTitle} pick paid ${latest.potentialReturn} tokens. Source: $source."
        } else {
            "Your ${latest.matchTitle} pick was settled as lost. Result: $matchResult. Source: $source."
        }
        runCatching {
            createNotification(latest.userEmail, NotificationType.PREDICTION_RESULT, title, message, latest.id.toString())
        }.onFailure { Log.e("FanArena", "Prediction notification failed", it) }
    }

    private fun buildPollSummary(choices: Iterable<String>): MatchRoomPollSummary {
        var home = 0
        var away = 0
        var draw = 0
        var participants = 0
        choices.forEach { rawChoice ->
            when (rawChoice.uppercase(Locale.US)) {
                "HOME" -> {
                    home++
                    participants++
                }
                "AWAY" -> {
                    away++
                    participants++
                }
                "DRAW" -> {
                    draw++
                    participants++
                }
            }
        }
        return MatchRoomPollSummary(home, away, draw, participants)
    }

    private fun MatchEntity.isFinishedForResolution(): Boolean {
        val status = statusText.uppercase(Locale.US)
        return status == "FT" ||
            status == "AET" ||
            status == "PEN" ||
            status.contains("FINISHED") ||
            status.contains("ENDED") ||
            status.contains("FULL TIME") ||
            status.contains("GAME OVER")
    }

    private fun MatchEntity.actualWinnerLabel(): String? {
        return when {
            homeScore > awayScore -> homeTeam
            awayScore > homeScore -> awayTeam
            homeScore == awayScore -> "Draw"
            else -> null
        }
    }

    private fun String.normalizedChoice(): String = trim().lowercase(Locale.US)

    private fun safeKey(value: String): String = value
        .replace(".", "_")
        .replace("#", "_")
        .replace("\$", "_")
        .replace("[", "_")
        .replace("]", "_")
        .replace("/", "_")

    suspend fun recordTransaction(id: String, title: String, amount: Int, type: String) = 
        appDao.insertTransaction(Transaction(transactionId = id, title = title, amount = amount, type = type))

    suspend fun redeemShopItem(itemName: String, cost: Int, avatarUrl: String? = null): Boolean {
        val user = appDao.getLoggedInUser() ?: return false
        if (user.tokenBalance < cost) return false

        val updatedUser = user.copy(
            tokenBalance = user.tokenBalance - cost,
            avatarUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: user.avatarUrl
        )
        updateProfile(updatedUser)
        recordTransaction(
            "#SHOP-${System.currentTimeMillis()}",
            "Redeemed $itemName",
            -cost,
            "SPEND"
        )
        return true
    }

    fun getFavoriteTeams(): Flow<List<FavoriteTeam>> = appDao.getFavoriteTeamsFlow()
    fun getFavoriteLeagues(): Flow<List<FavoriteLeague>> = appDao.getFavoriteLeaguesFlow()

    suspend fun toggleFavoriteTeam(team: FavoriteTeam) {
        val existing = appDao.getFavoriteTeam(team.teamId)
        if (existing == null) {
            appDao.insertFavoriteTeam(team)
            runCatching { favoriteTeamsRef.child(team.teamId.toString()).setValue(team).await() }
        } else {
            appDao.deleteFavoriteTeam(existing)
            runCatching { favoriteTeamsRef.child(team.teamId.toString()).removeValue().await() }
        }
    }

    suspend fun toggleFavoriteLeague(league: FavoriteLeague) {
        val existing = appDao.getFavoriteLeague(league.leagueId)
        if (existing == null) {
            appDao.insertFavoriteLeague(league)
            runCatching { favoriteLeaguesRef.child(league.leagueId.toString()).setValue(league).await() }
        } else {
            appDao.deleteFavoriteLeague(existing)
            runCatching { favoriteLeaguesRef.child(league.leagueId.toString()).removeValue().await() }
        }
    }

    fun getUserStats(userEmail: String): Flow<UserStats> {
        return combine(posts, getFollowingList(userEmail)) { postList, following ->
            UserStats(
                followersCount = 0,
                followingCount = following.size,
                postsCount = postList.count { it.authorEmail == userEmail }
            )
        }
    }
    fun getUserMissionsFlow(email: String): Flow<List<UserMission>> {
        return combine(predictions, posts, getFollowingList(email)) { predictionList, postList, following ->
            val userPredictions = predictionList.filter { it.userEmail == email }
            val userPosts = postList.filter { it.authorEmail == email }
            listOf(
                UserMission("daily-checkin", 0, 1, false),
                UserMission("predict-2", userPredictions.size.coerceAtMost(2), 2, userPredictions.size >= 2),
                UserMission("post-1", userPosts.size.coerceAtMost(1), 1, userPosts.isNotEmpty()),
                UserMission("follow-3", following.size.coerceAtMost(3), 3, following.size >= 3),
                UserMission("win-5", userPredictions.count { it.status == "WON" }.coerceAtMost(5), 5, userPredictions.count { it.status == "WON" } >= 5)
            )
        }
    }
    suspend fun updateMissionProgress(email: String, id: String, inc: Int) {
        val current = userStatsRef.child(email.replace(".", "_")).child(id).get().await()
            .getValue(Int::class.java) ?: 0
        userStatsRef.child(email.replace(".", "_")).child(id).setValue(current + inc).await()
    }

    suspend fun fetchAiMatchInsight(home: String, away: String, league: String) = com.example.ai.GeminiService.getMatchInsight(home, away, league)
    suspend fun fetchAiPredictionExplanation(home: String, away: String, league: String, choice: String) = com.example.ai.GeminiService.getPredictionExplanation(home, away, league, choice)
    suspend fun fetchAiRiskAssessment(match: MatchEntity) = com.example.ai.GeminiService.getRiskAssessment(
        home = match.homeTeam,
        away = match.awayTeam,
        league = match.league,
        sport = match.sport,
        statusText = match.statusText,
        homeOdds = match.homeOdds,
        drawOdds = match.drawOdds,
        awayOdds = match.awayOdds
    )

    suspend fun logoutAll() {
        appDao.logoutAllUsers()
        auth.signOut()
    }
}
