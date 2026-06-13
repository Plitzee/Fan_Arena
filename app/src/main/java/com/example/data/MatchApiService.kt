package com.example.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MatchApiService {
    @GET("fixtures")
    suspend fun getLiveMatches(
        @Header("x-apisports-key") apiKey: String,
        @Query("live") live: String = "all"
    ): MatchResponse

    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Header("x-apisports-key") apiKey: String,
        @Query("date") date: String
    ): MatchResponse
}

interface NbaApiService {
    @GET("games")
    suspend fun getLiveGames(
        @Header("x-apisports-key") apiKey: String,
        @Query("live") live: String = "all"
    ): NbaGameResponse

    @GET("games")
    suspend fun getGamesByDate(
        @Header("x-apisports-key") apiKey: String,
        @Query("date") date: String
    ): NbaGameResponse
}

interface BasketballApiService {
    @GET("games")
    suspend fun getGamesByDate(
        @Header("x-apisports-key") apiKey: String,
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Asia/Ho_Chi_Minh"
    ): ApiSportsGameResponse
}

interface VolleyballApiService {
    @GET("games")
    suspend fun getGamesByDate(
        @Header("x-apisports-key") apiKey: String,
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Asia/Ho_Chi_Minh"
    ): ApiSportsGameResponse
}

interface FormulaOneApiService {
    @GET("races")
    suspend fun getRacesBySeason(
        @Header("x-apisports-key") apiKey: String,
        @Query("season") season: Int,
        @Query("timezone") timezone: String = "Asia/Ho_Chi_Minh"
    ): FormulaOneRaceResponse
}

data class MatchResponse(
    val response: List<RemoteMatch>
)

data class RemoteMatch(
    val fixture: Fixture,
    val league: RemoteLeague,
    val teams: RemoteTeams,
    val goals: RemoteGoals
)

data class Fixture(
    val id: Long,
    val status: FixtureStatus,
    val date: String
)

data class FixtureStatus(
    val long: String,
    val short: String,
    val elapsed: Int?
)

data class RemoteLeague(
    val id: Int? = null,
    val name: String,
    val logo: String? = null
)

data class RemoteTeams(
    val home: RemoteTeam,
    val away: RemoteTeam
)

data class RemoteTeam(
    val id: Int? = null,
    val name: String,
    val logo: String
)

data class RemoteGoals(
    val home: Int?,
    val away: Int?
)

data class NbaGameResponse(
    val response: List<NbaGame> = emptyList()
)

data class NbaGame(
    val id: Long = 0,
    val league: String? = null,
    val date: NbaDate? = null,
    val status: NbaStatus? = null,
    val teams: NbaTeams? = null,
    val scores: NbaScores? = null
)

data class NbaDate(
    val start: String? = null
)

data class NbaStatus(
    val long: String? = null,
    val short: Int? = null,
    val clock: String? = null
)

data class NbaTeams(
    val home: NbaTeam? = null,
    val visitors: NbaTeam? = null
)

data class NbaTeam(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null
)

data class NbaScores(
    val home: NbaScore? = null,
    val visitors: NbaScore? = null
)

data class NbaScore(
    val points: Int? = null
)

data class ApiSportsGameResponse(
    val response: List<ApiSportsGame> = emptyList()
)

data class ApiSportsGame(
    val id: Long = 0,
    val date: String? = null,
    val time: String? = null,
    val timestamp: Long? = null,
    val league: ApiSportsLeague? = null,
    val country: ApiSportsCountry? = null,
    val teams: ApiSportsGameTeams? = null,
    val scores: ApiSportsGameScores? = null,
    val status: ApiSportsGameStatus? = null
)

data class ApiSportsLeague(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null,
    val season: String? = null
)

data class ApiSportsCountry(
    val name: String? = null
)

data class ApiSportsGameTeams(
    val home: ApiSportsGameTeam? = null,
    val away: ApiSportsGameTeam? = null
)

data class ApiSportsGameTeam(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null
)

data class ApiSportsGameScores(
    val home: ApiSportsGameScore? = null,
    val away: ApiSportsGameScore? = null
)

data class ApiSportsGameScore(
    val total: Int? = null,
    val points: Int? = null
)

data class ApiSportsGameStatus(
    val long: String? = null,
    val short: String? = null,
    val timer: String? = null
)

data class FormulaOneRaceResponse(
    val response: List<FormulaOneRace> = emptyList()
)

data class FormulaOneRace(
    val id: Long = 0,
    val competition: FormulaOneCompetition? = null,
    val circuit: FormulaOneCircuit? = null,
    val season: Int? = null,
    val type: String? = null,
    val laps: FormulaOneLaps? = null,
    val fastest_lap: FormulaOneFastestLap? = null,
    val distance: String? = null,
    val timezone: String? = null,
    val date: String? = null,
    val weather: String? = null,
    val status: String? = null
)

data class FormulaOneCompetition(
    val id: Int? = null,
    val name: String? = null,
    val location: FormulaOneLocation? = null
)

data class FormulaOneLocation(
    val country: String? = null,
    val city: String? = null
)

data class FormulaOneCircuit(
    val id: Int? = null,
    val name: String? = null,
    val image: String? = null
)

data class FormulaOneLaps(
    val total: Int? = null
)

data class FormulaOneFastestLap(
    val driver: FormulaOneDriver? = null,
    val time: String? = null
)

data class FormulaOneDriver(
    val name: String? = null
)
