package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.example.viewmodel.AiAssistantViewModel
import com.example.viewmodel.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(onBack: () -> Unit) {
    val viewModel: AiAssistantViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FanArena AI Assistant", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLowest)
            )
        },
        containerColor = BackgroundNavy
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages) { message ->
                    ChatBubble(message)
                }
                if (state.isLoading) {
                    item { LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp), color = PrimaryElectricBlue) }
                }
            }

            Surface(Modifier.fillMaxWidth(), color = SurfaceLowest) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = state.inputText,
                        onValueChange = viewModel::onInputChanged,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about matches, rules, or stats...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainer,
                            unfocusedContainerColor = SurfaceContainer
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = viewModel::sendMessage,
                        containerColor = PrimaryElectricBlue,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) PrimaryElectricBlue else SurfaceLowest
    val textColor = if (message.isUser) Color.White else OnSurface

    Column(Modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp, 
                    bottomStart = if (message.isUser) 16.dp else 0.dp,
                    bottomEnd = if (message.isUser) 0.dp else 16.dp
                ))
                .background(color)
                .padding(12.dp)
        ) {
            Text(message.text, color = textColor, fontSize = 14.sp)
        }
    }
}
