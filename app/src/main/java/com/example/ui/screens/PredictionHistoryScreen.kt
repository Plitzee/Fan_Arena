package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.data.AppDatabase
import com.example.data.FanArenaRepository
import com.example.data.Prediction
import com.example.ui.theme.*
import com.example.viewmodel.PredictionHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionHistoryScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FanArenaRepository(AppDatabase.getDatabase(context).appDao()) }
    val viewModel: PredictionHistoryViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PredictionHistoryViewModel(repository) as T
        }
    })

    val predictions by viewModel.predictions.collectAsState()
    val stats by viewModel.stats.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "WON", "LOST", "PENDING")
    val filteredPredictions = when (selectedFilter) {
        "WON" -> predictions.filter { it.status == "WON" }
        "LOST" -> predictions.filter { it.status == "LOST" }
        "PENDING" -> predictions.filter { it.status == "PENDING" }
        else -> predictions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Prediction History", fontFamily = SoraFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${predictions.size} total predictions", fontSize = 11.sp, color = OnSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLowest,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
                )
            )
        },
        containerColor = BackgroundNavy
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Win Rate Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryElectricBlue.copy(0.15f), SecondaryNeonGreen.copy(0.15f))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.horizontalGradient(colors = listOf(PrimaryElectricBlue.copy(0.5f), SecondaryNeonGreen.copy(0.5f))),
                            RoundedCornerShape(0.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Win rate circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(PrimaryElectricBlue.copy(0.2f))
                                .border(3.dp, PrimaryElectricBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${stats.winRate}%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryElectricBlue, fontFamily = SoraFamily)
                                Text("Win", fontSize = 9.sp, color = OnSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.width(20.dp))

                        // Stats grid
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MiniStatItem("Total", "${stats.total}", OnSurface, Modifier.weight(1f))
                                MiniStatItem("Won", "${stats.correct}", SecondaryNeonGreen, Modifier.weight(1f))
                                MiniStatItem("Lost", "${stats.wrong}", PremiumRed, Modifier.weight(1f))
                                MiniStatItem("Pending", "${stats.pending}", TertiaryOrange, Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MiniStatItem("Streak", "${stats.currentStreak}🔥", PremiumRed, Modifier.weight(1f))
                                MiniStatItem("Best", "${stats.longestStreak}🏆", TertiaryOrange, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Win rate progress bar
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Performance", fontSize = 12.sp, color = OnSurfaceVariant)
                        Text("${stats.winRate}% accuracy", fontSize = 12.sp, color = PrimaryElectricBlue, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stats.winRate / 100f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(PrimaryElectricBlue, SecondaryNeonGreen)
                                    )
                                )
                        )
                    }
                }
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val chipColor = when (filter) {
                            "WON" -> SecondaryNeonGreen
                            "LOST" -> PremiumRed
                            "PENDING" -> TertiaryOrange
                            else -> PrimaryElectricBlue
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) chipColor.copy(0.2f) else SurfaceLowest)
                                .border(1.dp, if (isSelected) chipColor else OutlineVariant, RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            val count = when (filter) {
                                "WON" -> stats.correct
                                "LOST" -> stats.wrong
                                "PENDING" -> stats.pending
                                else -> stats.total
                            }
                            Text(
                                "$filter ($count)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) chipColor else OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Predictions list
            if (filteredPredictions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, null, tint = OnSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No $selectedFilter predictions found", color = OnSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(filteredPredictions, key = { it.id }) { prediction ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        PredictionItem(
                            prediction = prediction,
                            onResolveWin = { viewModel.resolvePredictionDemo(prediction.id, true) },
                            onResolveLose = { viewModel.resolvePredictionDemo(prediction.id, false) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 9.sp, color = OnSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = OnSurfaceVariant)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun PredictionItem(
    prediction: Prediction,
    onResolveWin: () -> Unit = {},
    onResolveLose: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(prediction.timestamp))

    val statusColor = when (prediction.status) {
        "WON" -> SecondaryNeonGreen
        "LOST" -> PremiumRed
        "PENDING" -> TertiaryOrange
        else -> OnSurfaceVariant
    }
    val statusIcon = when (prediction.status) {
        "WON" -> Icons.Default.CheckCircle
        "LOST" -> Icons.Default.Cancel
        "PENDING" -> Icons.Default.Schedule
        else -> Icons.AutoMirrored.Filled.Help
    }
    val statusBg = statusColor.copy(0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (prediction.status == "WON") SecondaryNeonGreen.copy(0.3f) else OutlineVariant
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        prediction.matchTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OnSurface
                    )
                    Text(prediction.league, fontSize = 11.sp, color = OnSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(13.dp))
                        Text(prediction.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = OutlineVariant.copy(0.5f))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Flag, null, tint = PrimaryElectricBlue, modifier = Modifier.size(14.dp))
                        Text("Your pick:", fontSize = 11.sp, color = OnSurfaceVariant)
                        Text(prediction.yourChoice, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryElectricBlue)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Token, null, tint = TertiaryOrange, modifier = Modifier.size(14.dp))
                        Text("Stake:", fontSize = 11.sp, color = OnSurfaceVariant)
                        Text("${prediction.tokensPlaced} ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TertiaryOrange)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (prediction.status == "WON") {
                        Text("+${prediction.potentialReturn} ⚡", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SecondaryNeonGreen)
                        Text("Reward earned!", fontSize = 10.sp, color = SecondaryNeonGreen)
                    } else if (prediction.status == "PENDING") {
                        Text("~${prediction.potentialReturn} ⚡", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TertiaryOrange)
                        Text("Potential win", fontSize = 10.sp, color = OnSurfaceVariant)
                    } else {
                        Text("-${prediction.tokensPlaced} ⚡", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PremiumRed)
                        Text("Better luck next time!", fontSize = 10.sp, color = OnSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(dateString, fontSize = 10.sp, color = OnSurfaceVariant.copy(0.7f))
                }
            }

            if (prediction.status == "PENDING") {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onResolveWin,
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryNeonGreen,
                            contentColor = BackgroundNavy
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Demo Win", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onResolveLose,
                        modifier = Modifier.weight(1f).height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumRed),
                        border = BorderStroke(1.dp, PremiumRed.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Demo Lose", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
