package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TeamLogo
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: FanArenaViewModel,
    onNavigateToMatch: (Long) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToBadges: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.loggedInUser.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val missions by viewModel.userMissions.collectAsState()
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    val apiSyncState by viewModel.apiSyncState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedSportFilter by remember { mutableStateOf("All") }
    val sports = listOf("All", "Football", "NBA", "Basketball", "Volleyball", "Formula 1", "Tennis")

    val filteredMatches = if (selectedSportFilter == "All") {
        matches
    } else {
        matches.filter { it.sport.equals(selectedSportFilter, ignoreCase = true) }
    }

    val hasCheckedInToday = user?.let { t1 -> 
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1.lastCheckIn }
        val cal2 = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    } ?: false

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundNavy
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLowest)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            name = user?.fullName ?: "Fan Legend",
                            avatarUrl = user?.avatarUrl.orEmpty(),
                            size = 52.dp,
                            borderColor = PrimaryElectricBlue
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Welcome back,", fontSize = 12.sp, color = OnSurfaceVariant)
                            Text(user?.fullName ?: "Fan Legend", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = OnSurface)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            BadgePill(text = "${user?.tokenBalance ?: 0} ⚡", color = PrimaryElectricBlue)
                            Spacer(Modifier.height(4.dp))
                            Text("Level ${user?.level ?: 1}", fontSize = 10.sp, color = SecondaryNeonGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // AI Spotlight
            DataSourceBanner(
                source = apiSyncState?.source ?: "DEMO",
                message = apiSyncState?.message ?: "Preparing sports data...",
                isLoading = apiSyncState?.isLoading == true,
                onRefresh = {
                    viewModel.refreshSports(force = true)
                    coroutineScope.launch { snackbarHostState.showSnackbar("Refreshing sports data...") }
                }
            )

            // AI Spotlight
            if (aiRecommendation.recommendedMatchId != null) {
                val match = matches.find { it.id == aiRecommendation.recommendedMatchId }
                if (match != null) {
                    DashboardAiSpotlight(
                        match = match,
                        reason = aiRecommendation.recommendationReason,
                        onClick = { onNavigateToMatch(match.id) }
                    )
                }
            }

            // Missions Preview
            if (missions.isNotEmpty()) {
                DashboardMissionSection(missions = missions.take(2)) { onNavigateToMissions() }
            }

            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(Modifier.weight(1f), "XP", "${user?.xp ?: 0}", PrimaryElectricBlue, Icons.AutoMirrored.Filled.TrendingUp)
                StatCard(Modifier.weight(1f), "Streak", "${user?.currentStreak ?: 0} 🔥", PremiumRed, Icons.Default.Whatshot)
            }

            // Arena List
            Text("Live Arenas", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface, modifier = Modifier.padding(horizontal = 24.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sports) { sport ->
                    FilterChip(
                        selected = selectedSportFilter == sport,
                        onClick = { selectedSportFilter = sport },
                        label = { Text(sport, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryElectricBlue,
                            selectedLabelColor = BackgroundNavy,
                            containerColor = SurfaceLowest,
                            labelColor = OnSurfaceVariant
                        ),
                        border = null
                    )
                }
            }

            if (filteredMatches.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                    Text("No matches currently available", color = OnSurfaceVariant, fontSize = 14.sp)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredMatches) { match ->
                        DashboardMatchCard(match) { onNavigateToMatch(match.id) }
                    }
                }
            }

            // Daily Check-in Card
            DashboardDailyCard(hasCheckedInToday) {
                viewModel.triggerDailyCheckIn(
                    onAlreadyCheckedIn = { coroutineScope.launch { snackbarHostState.showSnackbar("Already checked in!") } },
                    onBonusClaimed = { bonus -> coroutineScope.launch { snackbarHostState.showSnackbar("Received $bonus token! ⚡") } }
                )
            }

            // Social Feed Preview
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Fan Feed", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface)
                TextButton(onClick = onNavigateToFeed) { Text("See All", color = PrimaryElectricBlue) }
            }
            
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                posts.take(3).forEach { post ->
                    PostMiniCard(post) { onNavigateToFeed() }
                }
            }
        }
    }
}

@Composable
fun BadgePill(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.15f)).border(1.dp, color, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface)
    }
}

@Composable
fun DataSourceBanner(
    source: String,
    message: String,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLive = source == "LIVE"
    val color = if (isLive) SecondaryNeonGreen else TertiaryOrange
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isLive) "LIVE DATA" else "DEMO DATA",
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh data", tint = color)
                }
            }
        }
    }
}

@Composable
fun DashboardAiSpotlight(match: com.example.data.MatchEntity, reason: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PrimaryElectricBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = PrimaryElectricBlue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("AI SPOTLIGHT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryElectricBlue)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(match.homeTeam, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("VS", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = OnSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
                Text(match.awayTeam, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            Spacer(Modifier.height(12.dp))
            Text(reason, fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 18.sp)
        }
    }
}

@Composable
fun DashboardMissionSection(missions: List<com.example.data.UserMission>, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).clickable { onClick() }) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Active Missions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
            Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
        }
        missions.forEach { mission ->
            val progress = mission.currentProgress.toFloat() / mission.targetValue.coerceAtLeast(1).toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(4.dp).clip(CircleShape),
                color = if (mission.isCompleted) SecondaryNeonGreen else PrimaryElectricBlue,
                trackColor = SurfaceContainer
            )
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceLowest), border = BorderStroke(1.dp, OutlineVariant)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 10.sp, color = OnSurfaceVariant)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface)
            }
        }
    }
}

@Composable
fun DashboardMatchCard(match: com.example.data.MatchEntity, onClick: () -> Unit) {
    Card(modifier = Modifier.width(260.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = SurfaceLowest), border = BorderStroke(1.dp, OutlineVariant)) {
        Column(Modifier.padding(16.dp)) {
            Text(match.league, fontSize = 10.sp, color = OnSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                TeamLogo(match.homeTeam, match.sport, match.homeLogoUrl, size = 30.dp, borderColor = PrimaryElectricBlue)
                Text("${match.homeScore} : ${match.awayScore}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                TeamLogo(match.awayTeam, match.sport, match.awayLogoUrl, size = 30.dp, borderColor = SecondaryNeonGreen)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(match.homeTeam, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(match.awayTeam, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }
    }
}

@Composable
fun DashboardDailyCard(hasCheckedInToday: Boolean, onClaim: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 24.dp), 
        colors = CardDefaults.cardColors(containerColor = if (hasCheckedInToday) SurfaceLowest.copy(0.5f) else SurfaceLowest), 
        border = BorderStroke(1.dp, if (hasCheckedInToday) OutlineVariant else PrimaryElectricBlue.copy(0.3f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Daily Reward", fontWeight = FontWeight.Bold, color = if (hasCheckedInToday) OnSurfaceVariant else OnSurface)
                Text(if (hasCheckedInToday) "Reward collected! ✅" else "Check-in for 100⚡", fontSize = 12.sp, color = OnSurfaceVariant)
            }
            Button(
                onClick = onClaim, 
                enabled = !hasCheckedInToday, 
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryElectricBlue,
                    disabledContainerColor = SurfaceContainer
                )
            ) {
                Text(if (hasCheckedInToday) "DONE" else "CLAIM", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PostMiniCard(post: com.example.data.Post, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = SurfaceLowest), border = BorderStroke(1.dp, OutlineVariant)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(post.authorName, post.authorAvatarUrl, size = 32.dp, borderColor = PrimaryElectricBlue)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("@${post.authorName}", fontWeight = FontWeight.Bold, color = PrimaryElectricBlue, fontSize = 12.sp)
                Text(post.postText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
