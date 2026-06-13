package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.FanArenaRepository
import com.example.viewmodel.MissionUiItem
import com.example.ui.theme.*
import com.example.viewmodel.MissionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { FanArenaRepository(AppDatabase.getDatabase(context).appDao()) }
    val viewModel: MissionViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MissionViewModel(repository) as T
        }
    })

    val missions by viewModel.missions.collectAsState()
    val canCheckIn by viewModel.canCheckIn.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily", "Weekly", "Achievements")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceLowest, BackgroundNavy)
                    )
                )
                .padding(top = 12.dp, bottom = 16.dp, start = 16.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryElectricBlue.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Explore, null, tint = PrimaryElectricBlue)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Missions",
                            fontFamily = SoraFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = OnSurface
                        )
                        Text("Complete tasks, earn tokens", fontSize = 11.sp, color = OnSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Daily Check-in Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
                    border = BorderStroke(1.dp, if (canCheckIn) SecondaryNeonGreen else OutlineVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (canCheckIn) SecondaryNeonGreen.copy(0.15f) else SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                null,
                                tint = if (canCheckIn) SecondaryNeonGreen else OnSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Daily Check-in", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                            Text(if (canCheckIn) "+50 ⚡, +10 XP" else "Come back tomorrow!", fontSize = 12.sp, color = OnSurfaceVariant)
                        }
                        Button(
                            onClick = { viewModel.claimDailyCheckIn() },
                            enabled = canCheckIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryNeonGreen,
                                contentColor = BackgroundNavy,
                                disabledContainerColor = SurfaceContainer,
                                disabledContentColor = OnSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(if (canCheckIn) "Claim" else "Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundNavy,
            contentColor = PrimaryElectricBlue,
            divider = { HorizontalDivider(color = OutlineVariant) },
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryElectricBlue
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == index) PrimaryElectricBlue else OnSurfaceVariant
                        )
                    }
                )
            }
        }

        val filteredMissions = when (selectedTab) {
            0 -> missions.filter { it.type == "DAILY" }
            1 -> missions.filter { it.type == "WEEKLY" }
            else -> missions.filter { it.type == "ACHIEVEMENT" }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredMissions, key = { it.id }) { mission ->
                MissionItemCard(mission = mission, onClaim = { viewModel.claimMissionReward(mission.id) })
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MissionItemCard(mission: MissionUiItem, onClaim: () -> Unit) {
    val progressPercent = (mission.currentProgress.toFloat() / mission.maxProgress.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val isCompleted = mission.currentProgress >= mission.maxProgress
    val isClaimable = isCompleted && !mission.isClaimed

    val cardColor = if (isClaimable) PrimaryElectricBlue.copy(0.05f) else SurfaceLowest
    val borderColor = if (isClaimable) PrimaryElectricBlue.copy(0.3f) else OutlineVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            mission.title.contains("Login") -> Icons.AutoMirrored.Filled.Login
                            mission.title.contains("Predict") -> Icons.Default.Stars
                            mission.title.contains("Follow") -> Icons.Default.PersonAdd
                            else -> Icons.Default.EmojiEvents
                        },
                        contentDescription = null,
                        tint = if (isCompleted) SecondaryNeonGreen else PrimaryElectricBlue
                    )
                }
                
                Spacer(Modifier.width(14.dp))
                
                // Title & Rewards
                Column(modifier = Modifier.weight(1f)) {
                    Text(mission.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Bolt, null, tint = PrimaryElectricBlue, modifier = Modifier.size(12.dp))
                            Text("${mission.tokenReward} Token", fontSize = 11.sp, color = PrimaryElectricBlue, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.StarRate, null, tint = TertiaryOrange, modifier = Modifier.size(12.dp))
                            Text("${mission.xpReward} XP", fontSize = 11.sp, color = TertiaryOrange, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Claim Button
                if (mission.isClaimed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Claimed", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                } else if (isClaimable) {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryElectricBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Claim", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BackgroundNavy)
                    }
                } else {
                    Text(
                        "${mission.currentProgress}/${mission.maxProgress}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                }
            }

            if (!mission.isClaimed) {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercent)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) SecondaryNeonGreen else PrimaryElectricBlue)
                    )
                }
            }
        }
    }
}
