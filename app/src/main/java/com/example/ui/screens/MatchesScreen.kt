package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TeamLogo
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel
import com.example.data.MatchEntity

@Composable
fun MatchesScreen(
    viewModel: FanArenaViewModel,
    onNavigateToMatch: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.matches.collectAsState()
    val apiSyncState by viewModel.apiSyncState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedSportTab by remember { mutableStateOf("Football") }
    var selectedStatusTab by remember { mutableStateOf("ALL") }

    val sportsTab = listOf("Football", "NBA", "Basketball", "Volleyball", "Formula 1", "Tennis")
    val categoriesTab = listOf("ALL", "LIVE", "UPCOMING", "FINISHED")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Search Header
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            placeholder = { Text("Search teams or leagues...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryElectricBlue,
                unfocusedBorderColor = OutlineVariant,
                focusedContainerColor = SurfaceLowest,
                unfocusedContainerColor = SurfaceLowest
            )
        )

        DataSourceBanner(
            source = apiSyncState?.source ?: "DEMO",
            message = apiSyncState?.message ?: "Preparing sports data...",
            isLoading = apiSyncState?.isLoading == true,
            onRefresh = { viewModel.refreshSports(force = true) },
            modifier = Modifier.padding(top = 0.dp)
        )

        // Sport Tabs
        ScrollableTabRow(
            selectedTabIndex = sportsTab.indexOf(selectedSportTab).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = PrimaryElectricBlue,
            edgePadding = 24.dp,
            divider = {}
        ) {
            sportsTab.forEach { sport ->
                Tab(
                    selected = selectedSportTab == sport,
                    onClick = { selectedSportTab = sport },
                    text = { Text(sportTabLabel(sport), fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Tabs
        TabRow(
            selectedTabIndex = categoriesTab.indexOf(selectedStatusTab).coerceAtLeast(0),
            containerColor = SurfaceLowest,
            contentColor = PrimaryElectricBlue,
            indicator = { tabPositions ->
                val index = categoriesTab.indexOf(selectedStatusTab).coerceAtLeast(0)
                if (index < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = PrimaryElectricBlue
                    )
                }
            }
        ) {
            categoriesTab.forEach { currentTab ->
                Tab(
                    selected = selectedStatusTab == currentTab,
                    onClick = { selectedStatusTab = currentTab },
                    text = { Text(currentTab, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val fanFilteredMatches = matches
            .filter { match ->
                val matchesSport = match.sport.equals(selectedSportTab, ignoreCase = true)
                val matchesSearch = match.homeTeam.contains(searchText, ignoreCase = true) || match.awayTeam.contains(searchText, ignoreCase = true)
                val s = statusOf(match)
                val matchesStatus = if (selectedStatusTab == "ALL") true else s == selectedStatusTab
                matchesSport && matchesSearch && matchesStatus
            }

        if (fanFilteredMatches.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No arena fixtures found", color = OnSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(fanFilteredMatches) { match ->
                    MatchListItemRow(match) { onNavigateToMatch(match.id) }
                }
            }
        }
    }
}

private fun statusOf(match: MatchEntity): String {
    return when {
        match.statusText.contains("LIVE", true) || match.statusText.contains("'") -> "LIVE"
        match.statusText.contains("FT", true) || match.statusText.contains("Finished", true) -> "FINISHED"
        else -> "UPCOMING"
    }
}

private fun sportTabLabel(sport: String): String = when (sport) {
    "Football" -> "⚽ Football"
    "NBA" -> "🏀 NBA"
    "Basketball" -> "🏀 Basketball"
    "Volleyball" -> "🏐 Volleyball"
    "Formula 1" -> "🏎 Formula 1"
    "Tennis" -> "🎾 Tennis"
    else -> sport
}

@Composable
fun MatchListItemRow(match: MatchEntity, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onItemClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(match.league, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                val isLive = statusOf(match) == "LIVE"
                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(if (isLive) PremiumRed.copy(0.1f) else SurfaceContainer).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(match.statusText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isLive) PremiumRed else OnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    TeamLogo(
                        name = match.homeTeam,
                        sport = match.sport,
                        logoUrl = match.homeLogoUrl,
                        size = 38.dp,
                        borderColor = PrimaryElectricBlue
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(match.homeTeam, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Text("${match.homeScore} - ${match.awayScore}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    TeamLogo(
                        name = match.awayTeam,
                        sport = match.sport,
                        logoUrl = match.awayLogoUrl,
                        size = 38.dp,
                        borderColor = SecondaryNeonGreen
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(match.awayTeam, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}
