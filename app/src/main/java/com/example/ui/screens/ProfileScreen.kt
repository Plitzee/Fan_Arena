package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel
import com.example.data.Transaction
import com.example.data.Prediction
import com.example.data.Badge

@Composable
fun ProfileScreen(
    viewModel: FanArenaViewModel,
    onLogout: () -> Unit,
    onOpenShop: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToPredictionHistory: () -> Unit,
    onNavigateToFavoriteSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.loggedInUser.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val displayName = user?.fullName ?: "Fan Legend"

    var activeSection by remember { mutableStateOf("badges") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenShop, modifier = Modifier.background(SurfaceLowest, CircleShape)) {
                Icon(Icons.Default.ShoppingCart, "Shop", tint = PrimaryElectricBlue)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onNavigateToMissions, modifier = Modifier.background(SurfaceLowest, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.Assignment, "Missions", tint = SecondaryNeonGreen)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onNavigateToPredictionHistory, modifier = Modifier.background(SurfaceLowest, CircleShape)) {
                Icon(Icons.Default.History, "Prediction history", tint = TertiaryOrange)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onNavigateToFavoriteSelection, modifier = Modifier.background(SurfaceLowest, CircleShape)) {
                Icon(Icons.Default.Favorite, "Favorites", tint = PremiumRed)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onLogout, modifier = Modifier.background(SurfaceLowest, CircleShape)) {
                Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = PremiumRed)
            }
        }

        // Fan Identity Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, PrimaryElectricBlue.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    UserAvatar(
                        name = displayName,
                        avatarUrl = user?.avatarUrl.orEmpty(),
                        size = 80.dp,
                        borderColor = PrimaryElectricBlue
                    )
                    Box(Modifier.size(24.dp).clip(CircleShape).background(SecondaryNeonGreen).border(2.dp, SurfaceLowest, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = BackgroundNavy)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(displayName, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = OnSurface)
                Text("LEVEL ${user?.level ?: 1} SPORTS EXPERT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryElectricBlue, letterSpacing = 1.sp)
                
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    ProfileStatItem("${user?.tokenBalance ?: 0}", "Tokens", PrimaryElectricBlue)
                    ProfileStatItem("${user?.currentStreak ?: 0}🔥", "Streak", PremiumRed)
                    ProfileStatItem("${user?.unlockedBadges?.size ?: 0}", "Badges", TertiaryOrange)
                }
            }
        }

        // Section Tabs
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), Arrangement.spacedBy(12.dp)) {
            TabPill("Badges", activeSection == "badges") { activeSection = "badges" }
            TabPill("History", activeSection == "history") { activeSection = "history" }
            TabPill("Ledger", activeSection == "ledger") { activeSection = "ledger" }
        }

        // Content Area
        Box(Modifier.weight(1f).padding(horizontal = 24.dp)) {
            when(activeSection) {
                "badges" -> BadgeGrid(viewModel.allBadges, user?.unlockedBadges ?: emptyList())
                "history" -> PredictionsList(predictions)
                "ledger" -> TransactionsList(transactions)
            }
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TabPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryElectricBlue else SurfaceLowest,
        border = if (!isSelected) BorderStroke(1.dp, OutlineVariant) else null
    ) {
        Text(label, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
            color = if (isSelected) BackgroundNavy else OnSurfaceVariant, 
            fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BadgeGrid(allBadges: List<Badge>, unlockedIds: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(allBadges) { badge ->
            val isUnlocked = unlockedIds.contains(badge.id)
            Card(
                modifier = Modifier.aspectRatio(0.8f),
                colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
                border = BorderStroke(1.dp, if (isUnlocked) PrimaryElectricBlue.copy(alpha = 0.5f) else OutlineVariant)
            ) {
                Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(badge.iconName, fontSize = 32.sp, modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Text(badge.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        color = if (isUnlocked) OnSurface else OnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun PredictionsList(predictions: List<Prediction>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(predictions) { pred ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceLowest), border = BorderStroke(1.dp, OutlineVariant)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(pred.matchTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Choice: ${pred.yourChoice} (${pred.tokensPlaced}⚡)", fontSize = 11.sp, color = OnSurfaceVariant)
                    }
                    StatusBadge(pred.status)
                }
            }
        }
    }
}

@Composable
fun TransactionsList(transactions: List<Transaction>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(transactions) { tx ->
            Row(Modifier.fillMaxWidth().background(SurfaceLowest, RoundedCornerShape(12.dp)).padding(12.dp), Arrangement.SpaceBetween) {
                Text(tx.title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("${if (tx.amount >= 0) "+" else ""}${tx.amount} ⚡", fontWeight = FontWeight.Bold, color = if (tx.amount >= 0) SecondaryNeonGreen else PremiumRed)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "WON" -> SecondaryNeonGreen
        "PENDING" -> PrimaryElectricBlue
        else -> PremiumRed
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(status, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

