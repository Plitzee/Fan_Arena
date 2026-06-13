package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.PrimaryElectricBlue
import com.example.ui.theme.SecondaryNeonGreen
import com.example.ui.theme.SurfaceLowest
import com.example.viewmodel.FanArenaViewModel
import kotlinx.coroutines.launch

@Composable
fun CreatePostScreen(
    viewModel: FanArenaViewModel,
    onPublishSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rawText by remember { mutableStateOf("") }
    var hashtagInput by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Football") }
    var selectedVisibility by remember { mutableStateOf("Public") }
    var isImprovingByAi by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = OnSurface)
                }
                Text(
                    text = "Discuss Match Moment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Button(
                    onClick = {
                        if (rawText.isNotBlank()) {
                            viewModel.submitPost(
                                text = rawText.trim(),
                                category = selectedCategory,
                                imageUrl = imageUrl.trim(),
                                hashtags = hashtagInput.trim(),
                                visibility = selectedVisibility
                            )
                            onPublishSuccess()
                        }
                    },
                    enabled = rawText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("POST", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = BackgroundNavy)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceLowest.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = PrimaryElectricBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Match Highlight Image", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        placeholder = { Text("Paste image URL, optional") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                placeholder = { Text("What's on your mind? Share stats or predictions...", fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryElectricBlue,
                    unfocusedBorderColor = OutlineVariant,
                    cursorColor = PrimaryElectricBlue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryElectricBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceLowest)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Magic ✨", fontWeight = FontWeight.Bold, color = OnSurface)
                        Text("Improve your post style", fontSize = 11.sp, color = OnSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            if (rawText.isNotBlank()) {
                                isImprovingByAi = true
                                coroutineScope.launch {
                                    rawText = viewModel.getImprovedPost(rawText, selectedCategory)
                                    isImprovingByAi = false
                                }
                            }
                        },
                        enabled = !isImprovingByAi && rawText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryElectricBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isImprovingByAi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BackgroundNavy)
                        } else {
                            Text("ENHANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BackgroundNavy)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val score = remember(rawText, hashtagInput, imageUrl) {
                (rawText.length + hashtagInput.length * 2 + if (imageUrl.isNotBlank()) 20 else 0).coerceIn(5, 100)
            }
            Text("Post Quality: $score%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (score > 70) SecondaryNeonGreen else PrimaryElectricBlue,
                trackColor = SurfaceLowest
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = hashtagInput,
                onValueChange = { hashtagInput = it },
                label = { Text("Hashtags") },
                placeholder = { Text("#PremierLeague #Football") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Football", "NBA", "Formula 1", "Volleyball").forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryElectricBlue,
                            selectedLabelColor = BackgroundNavy,
                            containerColor = SurfaceLowest,
                            labelColor = OnSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Visibility", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Public", "Followers").forEach { visibility ->
                    FilterChip(
                        selected = selectedVisibility == visibility,
                        onClick = { selectedVisibility = visibility },
                        label = { Text(visibility, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryNeonGreen,
                            selectedLabelColor = BackgroundNavy,
                            containerColor = SurfaceLowest,
                            labelColor = OnSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
