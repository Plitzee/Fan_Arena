package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.MatchEntity
import com.example.ui.theme.*
import com.example.viewmodel.MatchDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.AiRiskAssessment
import com.example.data.AppDatabase
import com.example.data.FanArenaRepository
import com.example.data.Comment
import com.example.viewmodel.MatchDetailUiState
import com.example.data.MatchRoomMessage
import com.example.data.MatchRoomPollSummary
import com.example.ui.components.TeamLogo
import com.example.ui.components.UserAvatar
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun MatchDetailScreen(
    matchId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { FanArenaRepository(AppDatabase.getDatabase(context).appDao()) }
    val viewModel: MatchDetailViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MatchDetailViewModel(repository) as T
        }
    })

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val match = state.match
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var predictionSuccessMessage by remember { mutableStateOf("") }

    LaunchedEffect(matchId) {
        viewModel.loadMatchDetails(matchId)
    }

    LaunchedEffect(state.predictionSuccess) {
        if (state.predictionSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = predictionSuccessMessage,
                    duration = SnackbarDuration.Long
                )
            }
            viewModel.resetPredictionSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundNavy
    ) { scaffoldPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundNavy).padding(scaffoldPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryElectricBlue, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Syncing arena data...", color = OnSurfaceVariant, fontSize = 14.sp)
                }
            }
            return@Scaffold
        }

        if (match == null) {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundNavy).padding(scaffoldPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, null, tint = PremiumRed, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Match data unavailable", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onBack) { Text("Go Back", color = PrimaryElectricBlue) }
                }
            }
            return@Scaffold
        }

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("Overview", "Predict", "AI Tactical", "Room", "Stats")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundNavy)
                .padding(scaffoldPadding)
        ) {
            MatchHeaderSection(match, onBack)

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = SurfaceLowest,
                contentColor = PrimaryElectricBlue,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PrimaryElectricBlue
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTabIndex == index) PrimaryElectricBlue else OnSurfaceVariant
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> OverviewTab(match)
                    1 -> PredictionTab(
                        match = match,
                        state = state,
                        onSelectChoice = viewModel::selectChoice,
                        onSetBid = viewModel::setBidAmount,
                        onPlacePrediction = { choice, tokens ->
                            viewModel.placePrediction(
                                onSuccess = { potentialReturn ->
                                    predictionSuccessMessage = "🎉 Predict '$choice' Success! Potential: $potentialReturn ⚡"
                                },
                                onError = { errMsg ->
                                    scope.launch { snackbarHostState.showSnackbar(errMsg) }
                                }
                            )
                        }
                    )
                    2 -> AiInsightTab(state) {
                        viewModel.fetchAiInsight()
                        viewModel.fetchAiRiskAssessment()
                    }
                    3 -> MatchRoomTab(match, state, viewModel)
                    4 -> StatsTab(match)
                }
            }
        }
    }
}

@Composable
fun MatchHeaderSection(match: MatchEntity, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceLowest, BackgroundNavy)
                )
            )
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(match.league, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryElectricBlue)
                    Text(match.sport, fontSize = 10.sp, color = OnSurfaceVariant)
                }
                Icon(Icons.Default.Share, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    TeamLogo(
                        name = match.homeTeam,
                        sport = match.sport,
                        logoUrl = match.homeLogoUrl,
                        size = 64.dp,
                        borderColor = PrimaryElectricBlue
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(match.homeTeam, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurface, textAlign = TextAlign.Center)
                    Text("${match.homeOdds}x", fontSize = 11.sp, color = PrimaryElectricBlue, fontWeight = FontWeight.Bold)
                }

                // Score
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.9f)) {
                    Text(text = "${match.homeScore} : ${match.awayScore}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface, fontFamily = SoraFamily)
                    Spacer(Modifier.height(4.dp))
                    val isLive = match.statusText.contains("'", true) || match.statusText.contains("SET", true) || match.statusText.contains("Q", true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLive) PremiumRed.copy(alpha = 0.2f) else SurfaceContainer)
                            .border(1.dp, if (isLive) PremiumRed.copy(0.5f) else OutlineVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(match.statusText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLive) PremiumRed else OnSurfaceVariant)
                    }
                }

                // Away Team
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    TeamLogo(
                        name = match.awayTeam,
                        sport = match.sport,
                        logoUrl = match.awayLogoUrl,
                        size = 64.dp,
                        borderColor = SecondaryNeonGreen
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(match.awayTeam, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurface, textAlign = TextAlign.Center)
                    Text("${match.awayOdds}x", fontSize = 11.sp, color = SecondaryNeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OverviewTab(match: MatchEntity) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Text("Match Info", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OnSurface, modifier = Modifier.padding(bottom = 12.dp))
        InfoCard {
            InfoRow("League", match.league)
            HorizontalDivider(color = OutlineVariant.copy(0.5f))
            InfoRow("Venue", match.stadium.ifBlank { "TBA" })
            HorizontalDivider(color = OutlineVariant.copy(0.5f))
            InfoRow("Kickoff", match.scheduleTime.take(16).replace("T", " "))
            HorizontalDivider(color = OutlineVariant.copy(0.5f))
            InfoRow("Sport", match.sport)
        }
        Spacer(Modifier.height(20.dp))
        Text("Odds Analysis", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OnSurface, modifier = Modifier.padding(bottom = 12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OddsCard("🏠 Home", match.homeOdds + "x", PrimaryElectricBlue, Modifier.weight(1f))
            if (match.drawOdds != "N/A") {
                OddsCard("🤝 Draw", match.drawOdds + "x", TertiaryOrange, Modifier.weight(1f))
            }
            OddsCard("✈️ Away", match.awayOdds + "x", SecondaryNeonGreen, Modifier.weight(1f))
        }
    }
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceLowest), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(4.dp), content = content)
    }
}

@Composable
fun OddsCard(label: String, odds: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, color = OnSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(odds, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = OnSurfaceVariant, fontSize = 13.sp)
        Text(value, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun isFinishedStatus(status: String): Boolean {
    return status.equals("FT", ignoreCase = true) ||
        status.equals("AET", ignoreCase = true) ||
        status.equals("PEN", ignoreCase = true) ||
        status.contains("Finished", ignoreCase = true) ||
        status.contains("Ended", ignoreCase = true) ||
        status.contains("Full Time", ignoreCase = true) ||
        status.contains("Game Over", ignoreCase = true)
}

@Composable
fun PredictionTab(
    match: MatchEntity,
    state: MatchDetailUiState,
    onSelectChoice: (String) -> Unit,
    onSetBid: (Float) -> Unit,
    onPlacePrediction: (String, Int) -> Unit
) {
    val isFinished = isFinishedStatus(match.statusText)
    val options = mutableListOf(
        Triple(match.homeTeam, match.homeOdds, PrimaryElectricBlue)
    )
    if (match.drawOdds != "N/A") {
        options.add(Triple("Draw", match.drawOdds, TertiaryOrange))
    }
    options.add(Triple(match.awayTeam, match.awayOdds, SecondaryNeonGreen))

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Default.Star, null, tint = TertiaryOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Arena Prediction", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface, fontFamily = SoraFamily)
        }
        Text(
            if (isFinished) "This match is finished. Pending predictions will auto-resolve." else "Predict the outcome to earn Tokens & XP.",
            fontSize = 12.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { (label, odds, color) ->
                val isSelected = state.selectedChoice == label
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) color.copy(alpha = 0.2f) else SurfaceLowest)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) color else OutlineVariant, RoundedCornerShape(12.dp))
                        .clickable(enabled = !state.alreadyPredicted && !isFinished) { onSelectChoice(label) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) color else OnSurface, textAlign = TextAlign.Center)
                        Text("${odds}x", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
                    }
                }
            }
        }

        // AI Prediction Explanation Card
        AnimatedVisibility(
            visible = state.selectedChoice != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryElectricBlue.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, PrimaryElectricBlue.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = PrimaryElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("AI STRATEGIC VIEW", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = PrimaryElectricBlue, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (state.isAiExplanationLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = PrimaryElectricBlue, trackColor = SurfaceContainer)
                    } else {
                        Text(state.predictionAiExplanation ?: "Analyzing market trends...", fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 18.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text("Stake Amount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurface)
            Text("${state.bidAmount.toInt()} ⚡", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryElectricBlue)
        }
        Slider(
            value = state.bidAmount, onValueChange = onSetBid, valueRange = 10f..500f, steps = 48, enabled = !state.alreadyPredicted && !isFinished,
            colors = SliderDefaults.colors(thumbColor = PrimaryElectricBlue, activeTrackColor = PrimaryElectricBlue)
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onPlacePrediction(state.selectedChoice ?: "", state.bidAmount.toInt()) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.alreadyPredicted) SurfaceContainer else SecondaryNeonGreen,
                contentColor = if (state.alreadyPredicted) OnSurfaceVariant else BackgroundNavy
            ),
            shape = RoundedCornerShape(14.dp),
            enabled = state.selectedChoice != null && !state.isPredicting && !state.alreadyPredicted && !isFinished
        ) {
            if (state.isPredicting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundNavy)
            else Text(
                when {
                    isFinished -> "MATCH FINISHED"
                    state.alreadyPredicted -> "PREDICTION PLACED"
                    else -> "CONFIRM PREDICTION"
                },
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun AiInsightTab(state: MatchDetailUiState, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryElectricBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Psychology, null, tint = PrimaryElectricBlue, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Tactical Insight", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface, fontFamily = SoraFamily)
                    Text("Real-time AI Analysis", fontSize = 10.sp, color = OnSurfaceVariant)
                }
            }
            IconButton(onClick = onRefresh, enabled = !state.isAiLoading && !state.isRiskLoading) {
                if (state.isAiLoading || state.isRiskLoading) CircularProgressIndicator(Modifier.size(18.dp), color = PrimaryElectricBlue)
                else Icon(Icons.Default.Refresh, null, tint = PrimaryElectricBlue)
            }
        }
        Spacer(Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceLowest), border = BorderStroke(1.dp, PrimaryElectricBlue.copy(0.15f)), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (state.isAiLoading) repeat(4) { Box(modifier = Modifier.fillMaxWidth().height(14.dp).padding(vertical = 4.dp).clip(RoundedCornerShape(4.dp)).background(SurfaceContainer)) }
                else Text(state.aiInsight, fontSize = 14.sp, color = OnSurface, lineHeight = 24.sp, fontFamily = SoraFamily)
            }
        }
        Spacer(Modifier.height(16.dp))
        AiRiskScoreCard(state.aiRiskAssessment, state.isRiskLoading)
    }
}

@Composable
fun AiRiskScoreCard(assessment: AiRiskAssessment, isLoading: Boolean) {
    val riskColor = when (assessment.riskLabel) {
        "Low Risk" -> SecondaryNeonGreen
        "High Risk" -> PremiumRed
        else -> TertiaryOrange
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        border = BorderStroke(1.dp, riskColor.copy(0.35f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AI Risk Score", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = OnSurface, fontFamily = SoraFamily)
                    Text("Confidence: ${assessment.confidenceLabel} · Source: ${assessment.sourceLabel}", fontSize = 11.sp, color = OnSurfaceVariant)
                }
                Box(
                    modifier = Modifier.size(74.dp).clip(CircleShape).background(riskColor.copy(0.12f)).border(2.dp, riskColor.copy(0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(28.dp), color = riskColor, strokeWidth = 3.dp)
                    } else {
                        Text("${assessment.score}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = riskColor)
                    }
                }
            }
            LinearProgressIndicator(
                progress = { assessment.score / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = riskColor,
                trackColor = SurfaceContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                RiskChip(assessment.riskLabel, riskColor, Modifier.weight(1f))
                RiskChip(assessment.stakeStyle, PrimaryElectricBlue, Modifier.weight(1f))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                assessment.reasons.take(3).forEach { reason ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.AutoAwesome, null, tint = riskColor, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(reason, fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 17.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RiskChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun MatchRoomTab(match: MatchEntity, state: MatchDetailUiState, viewModel: MatchDetailViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MatchRoomPollCard(
                    match = match,
                    summary = state.roomPollSummary,
                    onVote = { choice -> viewModel.voteMatchRoom(match.id.toString(), choice) }
                )
            }
            if (state.roomError != null) {
                item {
                    Text(
                        state.roomError,
                        color = PremiumRed,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(PremiumRed.copy(0.08f)).padding(12.dp)
                    )
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = PrimaryElectricBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Live Match Room", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OnSurface)
                    Spacer(Modifier.width(8.dp))
                    Text("${state.roomPollSummary.participants} participants", fontSize = 11.sp, color = OnSurfaceVariant)
                }
            }
            if (state.roomMessages.isEmpty()) {
                item {
                    Text(
                        "No live room messages yet. Start the match talk.",
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceLowest).padding(16.dp)
                    )
                }
            } else {
                items(state.roomMessages) { message -> MatchRoomMessageCard(message, state.currentUserEmail) }
            }
            if (state.comments.isNotEmpty()) {
                item {
                    Text("Older comments", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurfaceVariant)
                }
                items(state.comments) { comment -> MatchCommentCard(comment) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().background(SurfaceLowest).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.roomInput,
                onValueChange = viewModel::updateRoomInput,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message match room...") },
                shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryElectricBlue, unfocusedContainerColor = SurfaceContainer, focusedContainerColor = SurfaceContainer)
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.sendMatchRoomMessage(match.id.toString()) },
                modifier = Modifier.size(44.dp).clip(CircleShape).background(PrimaryElectricBlue)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = BackgroundNavy)
            }
        }
    }
}

@Composable
fun MatchRoomPollCard(
    match: MatchEntity,
    summary: MatchRoomPollSummary,
    onVote: (String) -> Unit
) {
    val showDraw = match.sport.equals("Football", true) || match.drawOdds != "N/A"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        border = BorderStroke(1.dp, PrimaryElectricBlue.copy(0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BarChart, null, tint = PrimaryElectricBlue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Room Poll", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OnSurface)
                }
                Text("${summary.participants} voters", fontSize = 11.sp, color = OnSurfaceVariant)
            }
            PollOptionButton(
                label = match.homeTeam,
                percent = summary.percentFor(summary.homeVotes),
                color = PrimaryElectricBlue,
                onClick = { onVote("HOME") }
            )
            PollOptionButton(
                label = match.awayTeam,
                percent = summary.percentFor(summary.awayVotes),
                color = SecondaryNeonGreen,
                onClick = { onVote("AWAY") }
            )
            if (showDraw) {
                PollOptionButton(
                    label = "Draw",
                    percent = summary.percentFor(summary.drawVotes),
                    color = TertiaryOrange,
                    onClick = { onVote("DRAW") }
                )
            }
        }
    }
}

@Composable
fun PollOptionButton(label: String, percent: Int, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainer)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurface, maxLines = 1)
            Text("$percent%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = SurfaceLowest
        )
    }
}

@Composable
fun MatchRoomMessageCard(message: MatchRoomMessage, currentUserEmail: String) {
    val isMine = currentUserEmail.isNotBlank() && message.authorEmail == currentUserEmail
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMine) {
            UserAvatar(
                name = message.authorName,
                avatarUrl = message.authorAvatarUrl,
                size = 34.dp,
                borderColor = PrimaryElectricBlue
            )
            Spacer(Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isMine) PrimaryElectricBlue.copy(0.18f) else SurfaceLowest)
                .border(1.dp, if (isMine) PrimaryElectricBlue.copy(0.35f) else OutlineVariant, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Text(
                if (isMine) "You" else message.authorName.ifBlank { "Fan" },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMine) PrimaryElectricBlue else SecondaryNeonGreen
            )
            Spacer(Modifier.height(4.dp))
            Text(message.text, fontSize = 13.sp, color = OnSurface, lineHeight = 18.sp)
        }
    }
}

@Composable
fun MatchCommentCard(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceLowest).padding(12.dp)) {
        UserAvatar(
            name = comment.authorName,
            avatarUrl = comment.authorAvatarUrl,
            size = 38.dp,
            borderColor = SecondaryNeonGreen
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(comment.authorName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryElectricBlue)
            Text(comment.text, fontSize = 13.sp, color = OnSurface)
        }
    }
}

@Composable
fun StatsTab(match: MatchEntity) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(Icons.Default.BarChart, null, tint = PrimaryElectricBlue)
            Spacer(Modifier.width(8.dp))
            Text("Match Stats", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceLowest), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MatchStatBar("Possession", 55, 45, PrimaryElectricBlue)
                MatchStatBar("Shots", 14, 9, SecondaryNeonGreen)
                MatchStatBar("Accuracy", 88, 79, TertiaryOrange)
            }
        }
    }
}

@Composable
fun MatchStatBar(label: String, home: Int, away: Int, color: Color) {
    val total = home + away
    val progress = home.toFloat() / total.toFloat()
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("$home", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(label, color = OnSurfaceVariant, fontSize = 11.sp)
            Text("$away", color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = color,
            trackColor = SurfaceContainer
        )
    }
}
