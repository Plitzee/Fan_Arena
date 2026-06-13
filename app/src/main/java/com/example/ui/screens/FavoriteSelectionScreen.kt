package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.FavoriteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteSelectionScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FanArenaRepository(AppDatabase.getDatabase(context).appDao()) }
    val viewModel: FavoriteViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(repository) as T
        }
    })

    val favoriteTeams by viewModel.favoriteTeams.collectAsState()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", fontFamily = SoraFamily, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundNavy,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
                )
            )
        },
        containerColor = BackgroundNavy
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search teams or leagues...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryElectricBlue,
                    unfocusedBorderColor = OutlineVariant
                )
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceLowest,
                contentColor = PrimaryElectricBlue,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryElectricBlue
                        )
                    }
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("TEAMS", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("LEAGUES", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            val availableTeams = listOf(
                FavoriteTeam(1, "Arsenal", "https://media.api-sports.io/football/teams/42.png"),
                FavoriteTeam(2, "Man City", "https://media.api-sports.io/football/teams/50.png"),
                FavoriteTeam(3, "Liverpool", "https://media.api-sports.io/football/teams/40.png"),
                FavoriteTeam(4, "Real Madrid", "https://media.api-sports.io/football/teams/541.png"),
                FavoriteTeam(5, "Barcelona", "https://media.api-sports.io/football/teams/529.png"),
                FavoriteTeam(101, "LA Lakers", "https://api.dicebear.com/7.x/initials/svg?seed=LAL"),
                FavoriteTeam(102, "Golden State Warriors", "https://api.dicebear.com/7.x/initials/svg?seed=GSW"),
                FavoriteTeam(201, "Red Bull Racing", "https://api.dicebear.com/7.x/initials/svg?seed=RBR"),
                FavoriteTeam(301, "Vietnam Volleyball", "https://api.dicebear.com/7.x/initials/svg?seed=VVB")
            )

            val availableLeagues = listOf(
                FavoriteLeague(39, "Premier League", "https://media.api-sports.io/football/leagues/39.png"),
                FavoriteLeague(140, "La Liga", "https://media.api-sports.io/football/leagues/140.png"),
                FavoriteLeague(61, "Ligue 1", "https://media.api-sports.io/football/leagues/61.png"),
                FavoriteLeague(78, "Bundesliga", "https://media.api-sports.io/football/leagues/78.png"),
                FavoriteLeague(1001, "NBA", "https://api.dicebear.com/7.x/initials/svg?seed=NBA"),
                FavoriteLeague(1002, "Formula 1", "https://api.dicebear.com/7.x/initials/svg?seed=F1"),
                FavoriteLeague(1003, "Volleyball Nations League", "https://api.dicebear.com/7.x/initials/svg?seed=VNL")
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    items(availableTeams.filter { it.teamName.contains(searchQuery, ignoreCase = true) }) { team ->
                        val isFav = favoriteTeams.any { it.teamId == team.teamId }
                        FavoriteItemRow(
                            name = team.teamName,
                            logo = team.teamLogo,
                            isFavorite = isFav,
                            onToggle = { viewModel.toggleFavoriteTeam(team) }
                        )
                    }
                } else {
                    items(availableLeagues.filter { it.leagueName.contains(searchQuery, ignoreCase = true) }) { league ->
                        val isFav = favoriteLeagues.any { it.leagueId == league.leagueId }
                        FavoriteItemRow(
                            name = league.leagueName,
                            logo = league.leagueLogo,
                            isFavorite = isFav,
                            onToggle = { viewModel.toggleFavoriteLeague(league) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemRow(
    name: String,
    logo: String,
    isFavorite: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = logo,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(SurfaceContainer)
            )
            Spacer(Modifier.width(16.dp))
            Text(name, fontWeight = FontWeight.Bold, color = OnSurface, modifier = Modifier.weight(1f))
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) PremiumRed else OnSurfaceVariant
                )
            }
        }
    }
}
