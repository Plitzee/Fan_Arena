package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.AppDatabase
import com.example.data.FanArenaRepository
import com.example.data.UserProfile
import com.example.ui.theme.*
import com.example.viewmodel.LeaderboardViewModel

@Composable
fun LeaderboardScreen() {
    val context = LocalContext.current
    val repository = remember { FanArenaRepository(AppDatabase.getDatabase(context).appDao()) }
    val viewModel: LeaderboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LeaderboardViewModel(repository) as T
        }
    })

    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Token Leaders", "XP Masters", "Streak Kings")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(colors = listOf(SurfaceLowest, BackgroundNavy))
                )
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, null, tint = TertiaryOrange, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Leaderboard",
                        fontFamily = SoraFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = OnSurface
                    )
                }
                Text(
                    "Compete with the best fans worldwide!",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Tabs
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
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            label.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == index) PrimaryElectricBlue else OnSurfaceVariant
                        )
                    }
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryElectricBlue)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading rankings...", color = OnSurfaceVariant)
                }
            }
        } else {
            val displayList = when (selectedTab) {
                0 -> users.sortedByDescending { it.tokenBalance }
                1 -> users.sortedByDescending { it.xp }
                else -> users.sortedByDescending { it.currentStreak }
            }

            if (displayList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.People, null, tint = OnSurfaceVariant, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No players yet!", color = OnSurfaceVariant, fontSize = 14.sp)
                        Text("Be the first to join the arena.", color = OnSurfaceVariant, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Top 3 Podium
                    if (displayList.size >= 3) {
                        item {
                            TopThreePodium(
                                first = displayList[0],
                                second = displayList[1],
                                third = displayList[2],
                                selectedTab = selectedTab
                            )
                        }
                    }

                    // Remaining ranks
                    itemsIndexed(displayList.drop(3)) { index, user ->
                        LeaderboardItem(
                            rank = index + 4,
                            user = user,
                            selectedTab = selectedTab
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopThreePodium(
    first: UserProfile,
    second: UserProfile,
    third: UserProfile,
    selectedTab: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(0.05f),
                        BackgroundNavy
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2nd place
            PodiumItem(
                rank = 2,
                user = second,
                podiumHeight = 80.dp,
                medalColor = Color(0xFFC0C0C0),
                medalEmoji = "🥈",
                selectedTab = selectedTab,
                modifier = Modifier.weight(1f)
            )

            // 1st place (tallest)
            PodiumItem(
                rank = 1,
                user = first,
                podiumHeight = 110.dp,
                medalColor = Color(0xFFFFD700),
                medalEmoji = "🥇",
                selectedTab = selectedTab,
                modifier = Modifier.weight(1f)
            )

            // 3rd place
            PodiumItem(
                rank = 3,
                user = third,
                podiumHeight = 60.dp,
                medalColor = Color(0xFFCD7F32),
                medalEmoji = "🥉",
                selectedTab = selectedTab,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PodiumItem(
    rank: Int,
    user: UserProfile,
    podiumHeight: androidx.compose.ui.unit.Dp,
    medalColor: Color,
    medalEmoji: String,
    selectedTab: Int,
    modifier: Modifier = Modifier
) {
    val valueText = when (selectedTab) {
        0 -> "${user.tokenBalance} ⚡"
        1 -> "${user.xp} XP"
        else -> "${user.currentStreak} 🔥"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(medalEmoji, fontSize = 24.sp)
        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(medalColor.copy(0.15f))
                .border(2.dp, medalColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier.size(52.dp).clip(CircleShape)
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            user.fullName.split(" ").firstOrNull() ?: user.fullName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            valueText,
            fontSize = 10.sp,
            color = medalColor,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        // Podium block
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(medalColor.copy(0.4f), medalColor.copy(0.15f))
                    )
                )
                .border(1.dp, medalColor.copy(0.5f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#$rank",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = medalColor
            )
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, user: UserProfile, selectedTab: Int) {
    val valueText = when (selectedTab) {
        0 -> "${user.tokenBalance} ⚡"
        1 -> "${user.xp} XP"
        else -> "${user.currentStreak} 🔥"
    }
    val valueColor = when (selectedTab) {
        0 -> PrimaryElectricBlue
        1 -> SecondaryNeonGreen
        else -> PremiumRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                modifier = Modifier.width(36.dp)
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer)
                    .border(1.dp, OutlineVariant, CircleShape)
            ) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Lv.${user.level}", fontSize = 11.sp, color = TertiaryOrange, fontWeight = FontWeight.Bold)
                    Text("·", fontSize = 11.sp, color = OnSurfaceVariant)
                    Text("${user.currentStreak}🔥", fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }

            Text(
                text = valueText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = valueColor
            )
        }
    }
}
