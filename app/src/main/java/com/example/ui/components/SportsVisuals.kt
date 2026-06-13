package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.OnSurface
import com.example.ui.theme.PrimaryElectricBlue
import com.example.ui.theme.SecondaryNeonGreen
import com.example.ui.theme.SurfaceContainer
import java.net.URLEncoder

@Composable
fun TeamLogo(
    name: String,
    sport: String,
    logoUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    borderColor: Color = PrimaryElectricBlue
) {
    val fallbackUrl = generatedLogoUrl(name, sport)
    var useFallback by remember(name, sport, logoUrl) { mutableStateOf(logoUrl.isBlank()) }
    VisualBubble(
        label = initialsFor(if (sport.equals("Formula 1", true) && name.equals("Race", true)) "F1" else name),
        imageUrl = if (useFallback) fallbackUrl else logoUrl,
        modifier = modifier,
        size = size,
        borderColor = borderColor,
        onError = { useFallback = true }
    )
}

@Composable
fun UserAvatar(
    name: String,
    avatarUrl: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    borderColor: Color = SecondaryNeonGreen
) {
    val fallbackUrl = generatedAvatarUrl(name)
    var useFallback by remember(name, avatarUrl) { mutableStateOf(avatarUrl.isBlank()) }
    VisualBubble(
        label = initialsFor(name),
        imageUrl = if (useFallback) fallbackUrl else avatarUrl,
        modifier = modifier,
        size = size,
        borderColor = borderColor,
        onError = { useFallback = true }
    )
}

@Composable
private fun VisualBubble(
    label: String,
    imageUrl: String,
    modifier: Modifier,
    size: Dp,
    borderColor: Color,
    onError: () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceContainer)
            .border(1.5.dp, borderColor.copy(alpha = 0.55f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = OnSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value / 3f).sp
        )
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop,
            onError = { onError() }
        )
    }
}

fun generatedLogoUrl(name: String, sport: String): String {
    val seed = urlEncode("${sport.ifBlank { "Sport" }}-${name.ifBlank { "Team" }}")
    return "https://api.dicebear.com/7.x/initials/svg?seed=$seed&backgroundColor=1d4ed8,0f766e,f59e0b&textColor=ffffff&fontWeight=700"
}

fun generatedAvatarUrl(name: String): String {
    val seed = urlEncode(name.ifBlank { "FanArena User" })
    return "https://api.dicebear.com/7.x/avataaars/svg?seed=$seed&backgroundColor=1d4ed8,0f766e,f59e0b"
}

private fun initialsFor(name: String): String {
    val clean = name.trim().ifBlank { "FA" }
    val parts = clean.split(" ", "-", "_").filter { it.isNotBlank() }
    return parts.mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifBlank {
        clean.take(2).uppercase()
    }
}

private fun urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")
