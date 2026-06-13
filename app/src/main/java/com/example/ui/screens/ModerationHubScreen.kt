package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationHubScreen(
    viewModel: FanArenaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.pendingReports.collectAsState()
    val userProfile by viewModel.loggedInUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moderation Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
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
            // Metrics
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("PENDING", "${reports.size}", modifier = Modifier.weight(1f), color = TertiaryOrange)
                MetricCard("BALANCE", "${userProfile?.tokenBalance ?: 0} ⚡", modifier = Modifier.weight(1f), color = PrimaryElectricBlue)
            }

            Text("Queue for Review", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp), color = OnSurface)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reports) { rep ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
                        border = BorderStroke(1.dp, OutlineVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(rep.authorName, fontWeight = FontWeight.Bold, color = PrimaryElectricBlue)
                            Spacer(Modifier.height(4.dp))
                            Text(rep.text, color = OnSurface, fontSize = 14.sp)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.updateReportStatus(rep.firestoreId, "KEEP") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeonGreen)
                                ) { Text("KEEP", fontSize = 11.sp, color = BackgroundNavy) }
                                Button(
                                    onClick = { viewModel.updateReportStatus(rep.firestoreId, "DELETE") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumRed)
                                ) { Text("DELETE", fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceLowest), border = BorderStroke(1.dp, OutlineVariant)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = OnSurfaceVariant)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
