package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: FanArenaViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.loggedInUser.collectAsState()
    val allBadges = viewModel.allBadges
    val unlockedIds = user?.unlockedBadges ?: emptyList()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hall of Fame 🥇", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLowest)
            )
        },
        containerColor = BackgroundNavy
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
                border = BorderStroke(1.dp, PrimaryElectricBlue.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Arena Master Progress", fontWeight = FontWeight.Bold, color = OnSurfaceVariant, fontSize = 12.sp)
                    Text("${unlockedIds.size} / ${allBadges.size} Badges", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = OnSurface)
                    
                    Spacer(Modifier.height(16.dp))
                    
                    val progress = if (allBadges.isNotEmpty()) unlockedIds.size.toFloat() / allBadges.size else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = SecondaryNeonGreen,
                        trackColor = SurfaceContainer
                    )
                }
            }

            // Grid of badges
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allBadges) { badge ->
                    val isUnlocked = unlockedIds.contains(badge.id)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
                        border = BorderStroke(1.dp, if (isUnlocked) PrimaryElectricBlue.copy(0.5f) else OutlineVariant)
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape).background(if (isUnlocked) PrimaryElectricBlue.copy(0.1f) else SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(badge.iconName, fontSize = 28.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(badge.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center, color = if (isUnlocked) OnSurface else OnSurfaceVariant)
                            Text(badge.description, fontSize = 10.sp, textAlign = TextAlign.Center, color = OnSurfaceVariant, lineHeight = 14.sp)
                            
                            if (isUnlocked) {
                                Spacer(Modifier.height(8.dp))
                                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(SecondaryNeonGreen.copy(0.2f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("UNLOCKED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SecondaryNeonGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
