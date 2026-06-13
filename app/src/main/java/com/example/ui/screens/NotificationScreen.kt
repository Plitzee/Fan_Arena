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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FanArenaRepository(AppDatabase.getDatabase(context).appDao()) }
    val viewModel: NotificationViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
    })

    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredNotifications = when (selectedFilter) {
        "Unread" -> notifications.filter { !it.isRead }
        "Social" -> notifications.filter { it.type == NotificationType.SOCIAL_INTERACTION }
        "Prediction" -> notifications.filter { it.type == NotificationType.PREDICTION_RESULT }
        else -> notifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Notifications",
                            fontFamily = SoraFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PremiumRed)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$unreadCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            notifications.filter { !it.isRead }.forEach {
                                viewModel.markAsRead(it.notificationId)
                            }
                        }) {
                            Text("Mark all read", fontSize = 11.sp, color = PrimaryElectricBlue)
                        }
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
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SurfaceLowest)
                            .border(2.dp, OutlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = OnSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("No notifications yet", color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Predictions results and interactions will appear here.",
                        color = OnSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Unread", "Social", "Prediction").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            AssistChip(
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) PrimaryElectricBlue.copy(0.2f) else SurfaceLowest,
                                    labelColor = if (isSelected) PrimaryElectricBlue else OnSurfaceVariant
                                )
                            )
                        }
                    }
                }

                items(filteredNotifications, key = { it.notificationId }) { notification ->
                    NotificationItemRow(notification) {
                        viewModel.markAsRead(notification.notificationId)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(notification: NotificationItem, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm, MMM dd", Locale.getDefault())
    val dateString = dateFormat.format(Date(notification.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) SurfaceLowest else SurfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(
                    if (notification.isRead) Color.Transparent else PrimaryElectricBlue
                )
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(notification.message, fontSize = 12.sp, color = OnSurfaceVariant)
                Text(dateString, fontSize = 10.sp, color = OnSurfaceVariant.copy(0.6f))
            }
        }
    }
}
