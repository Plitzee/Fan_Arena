package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UserAvatar
import com.example.ui.components.generatedAvatarUrl
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel
import kotlinx.coroutines.launch

data class ShopItem(
    val id: String,
    val name: String,
    val price: Int,
    val icon: String,
    val category: String,
    val description: String,
    val note: String,
    val previewAvatarUrl: String = "",
    val grantAvatarUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: FanArenaViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.loggedInUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val inventory = listOf(
        ShopItem("voucher_starbucks", "Starbucks Voucher", 1200, "☕", "Voucher", "Redeem a premium Starbucks e-voucher for your next coffee break.", "Popular drop"),
        ShopItem("voucher_highlands", "Highlands Voucher", 1000, "🧋", "Voucher", "Get a Highlands Coffee voucher for matchday hangouts.", "Fast moving"),
        ShopItem("voucher_crypto", "Crypto Booster Voucher", 1600, "₿", "Voucher", "Unlock a crypto-themed bonus bundle and premium badge skin.", "Limited edition"),
        ShopItem("avatar_neon_striker", "Neon Striker Avatar", 450, "NS", "Avatar Skin", "Apply a bright football-inspired profile avatar for feed and match rooms.", "New", generatedAvatarUrl("Neon Striker FanArena"), generatedAvatarUrl("Neon Striker FanArena")),
        ShopItem("avatar_courtside", "Courtside Analyst Avatar", 420, "CA", "Avatar Skin", "Switch your profile to a sharp basketball analyst identity.", "Hot", generatedAvatarUrl("Courtside Analyst FanArena"), generatedAvatarUrl("Courtside Analyst FanArena")),
        ShopItem("avatar_f1_racer", "F1 Racer Avatar", 480, "F1", "Avatar Skin", "Bring a racing paddock look to your FanArena profile.", "Fast drop", generatedAvatarUrl("F1 Racer FanArena"), generatedAvatarUrl("F1 Racer FanArena")),
        ShopItem("avatar_volley_captain", "Volley Captain Avatar", 400, "VC", "Avatar Skin", "A clean team-captain avatar for volleyball fans.", "Team pick", generatedAvatarUrl("Volley Captain FanArena"), generatedAvatarUrl("Volley Captain FanArena")),
        ShopItem("pass_vip", "VIP Match Pass", 900, "🎟️", "Fan Access", "Priority access to featured fixtures and AI spotlight picks.", "Best value"),
        ShopItem("boost_ai_pack", "AI Insight Boost Pack", 650, "🧠", "Matchday Boost", "A premium-looking pack for users who want stronger pre-match analysis moments.", "Demo perk"),
        ShopItem("badge_derby", "Derby Day Badge", 360, "🏟️", "Badge Skin", "Show a derby-day badge style beside your fan identity.", "Collectible"),
        ShopItem("theme_neon", "Neon Arena Theme", 700, "💡", "Theme", "A cosmetic neon theme drop for future profile customization.", "Cosmetic"),
        ShopItem("frame_gold", "Golden Frame", 500, "🖼️", "Avatar Custom", "Give your profile a premium gold border on fan cards.", "Cosmetic"),
        ShopItem("effect_fire", "Fire Comment Effect", 350, "🔥", "Comment Effect", "Make your comments stand out with a louder matchday aura.", "Popular")
    )

    val balance = user?.tokenBalance ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FanArena Token Shop", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 16.dp).clip(CircleShape).background(PrimaryElectricBlue.copy(0.1f)).padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${user?.tokenBalance ?: 0}", fontWeight = FontWeight.ExtraBold, color = PrimaryElectricBlue)
                        Spacer(Modifier.width(4.dp))
                        Text("⚡", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLowest)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundNavy
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
                    border = BorderStroke(1.dp, PrimaryElectricBlue.copy(alpha = 0.25f))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Redeem your tokens for real fan perks and brand vouchers.", fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Save tokens for Starbucks, Highlands, Crypto drops, and matchday bonuses.", color = OnSurfaceVariant, fontSize = 12.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PrimaryElectricBlue.copy(alpha = 0.12f))
                                    .border(1.dp, PrimaryElectricBlue.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("$balance ⚡", fontWeight = FontWeight.ExtraBold, color = PrimaryElectricBlue)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SecondaryNeonGreen.copy(alpha = 0.12f))
                                    .border(1.dp, SecondaryNeonGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Live drops", fontWeight = FontWeight.Bold, color = SecondaryNeonGreen)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Featured vouchers") }
            items(inventory.take(3), key = { it.id }) { item ->
                ShopCard(
                    item = item,
                    canAfford = balance >= item.price,
                    onRedeem = {
                        scope.launch {
                            viewModel.redeemShopItem(
                                item.name,
                                item.price,
                            ) { success ->
                                val message = if (success) {
                                    "Redeemed ${item.name}"
                                } else {
                                    "Not enough tokens"
                                }
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        }
                    }
                )
            }

            item { SectionHeader("Avatar skins") }
            items(inventory.filter { it.category == "Avatar Skin" }, key = { it.id }) { item ->
                ShopCard(
                    item = item,
                    canAfford = balance >= item.price,
                    onRedeem = {
                        scope.launch {
                            viewModel.redeemShopItem(
                                item.name,
                                item.price,
                                item.grantAvatarUrl
                            ) { success ->
                                val message = if (success) {
                                    "Redeemed ${item.name} and applied avatar"
                                } else {
                                    "Not enough tokens"
                                }
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        }
                    }
                )
            }

            item { SectionHeader("Fan upgrades") }
            items(inventory.filter { it.category != "Voucher" && it.category != "Avatar Skin" }, key = { it.id }) { item ->
                ShopCard(
                    item = item,
                    canAfford = balance >= item.price,
                    onRedeem = {
                        scope.launch {
                            viewModel.redeemShopItem(
                                item.name,
                                item.price,
                            ) { success ->
                                val message = if (success) {
                                    "Redeemed ${item.name}"
                                } else {
                                    "Not enough tokens"
                                }
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontWeight = FontWeight.ExtraBold, color = OnSurface, fontSize = 18.sp)
        Text("Limited stock", color = OnSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun ShopCard(
    item: ShopItem,
    canAfford: Boolean,
    onRedeem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        border = BorderStroke(1.dp, if (canAfford) PrimaryElectricBlue.copy(alpha = 0.25f) else OutlineVariant)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryElectricBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.previewAvatarUrl.isNotBlank()) {
                    UserAvatar(
                        name = item.name,
                        avatarUrl = item.previewAvatarUrl,
                        size = 52.dp,
                        borderColor = SecondaryNeonGreen
                    )
                } else {
                    Text(item.icon, fontSize = 28.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryElectricBlue)
                    Text(item.note, fontSize = 10.sp, color = OnSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                Spacer(Modifier.height(4.dp))
                Text(item.description, fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 16.sp)
                Spacer(Modifier.height(10.dp))
                Text("${item.price} ⚡", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SecondaryNeonGreen)
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onRedeem,
                enabled = canAfford,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) PrimaryElectricBlue else SurfaceContainer,
                    contentColor = OnPrimary,
                    disabledContainerColor = SurfaceContainer,
                    disabledContentColor = OnSurfaceVariant
                )
            ) {
                Text(if (canAfford) "Redeem" else "Need ${item.price}⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
