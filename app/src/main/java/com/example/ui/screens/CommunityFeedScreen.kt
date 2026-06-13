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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Delegate import cực kỳ quan trọng
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Post
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFeedScreen(
    viewModel: FanArenaViewModel,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sử dụng collectAsStateWithLifecycle kèm initialValue để fix lỗi Type Inference và Build
    val posts by viewModel.posts.collectAsStateWithLifecycle(initialValue = emptyList())
    val followingList by viewModel.followingList.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("ARENA FEED", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface,
                        letterSpacing = 2.sp
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundNavy,
                    titleContentColor = OnSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePost, 
                containerColor = PrimaryElectricBlue,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post", tint = BackgroundNavy, modifier = Modifier.size(28.dp))
            }
        },
        containerColor = BackgroundNavy
    ) { padding ->
        if (posts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Forum, null, tint = OnSurfaceVariant, modifier = Modifier.size(64.dp))
                    Text("The arena is quiet... be the first to post!", color = OnSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts) { post ->
                    val isFollowing = followingList.contains(post.authorEmail)
                    PostCard(
                        post = post,
                        isFollowing = isFollowing,
                        onLike = { viewModel.toggleLikeOnPost(post) },
                        onShare = { viewModel.sharePost(post) },
                        onFollow = { if (isFollowing) viewModel.unfollowUser(post.authorEmail) else viewModel.followUser(post.authorEmail) },
                        onClick = { onNavigateToPostDetail(post.firestoreId) }
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isFollowing: Boolean,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onFollow: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(0.5.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBubble(
                    name = post.authorName,
                    imageUrl = post.authorAvatarUrl,
                    size = 44
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(post.authorName, fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PrimaryElectricBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                post.sportCategory.uppercase(), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Black, 
                                color = PrimaryElectricBlue,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                // Follow Button
                TextButton(
                    onClick = onFollow,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isFollowing) OnSurfaceVariant else SecondaryNeonGreen
                    )
                ) {
                    Text(if (isFollowing) "FOLLOWING" else "+ FOLLOW", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(14.dp))
            
            // Content
            Text(
                text = post.postText, 
                color = OnSurface, 
                fontSize = 14.sp,
                lineHeight = 20.sp,
                style = MaterialTheme.typography.bodyLarge
            )

            if (post.imageUrl.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FeedImage(imageUrl = post.imageUrl, label = post.sportCategory)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            // Footer / Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }.padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (post.likesCount > 0) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        contentDescription = null, 
                        tint = if (post.likesCount > 0) TertiaryOrange else OnSurfaceVariant, 
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "${post.likesCount}", 
                        Modifier.padding(start = 6.dp), 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Bold,
                        color = if (post.likesCount > 0) TertiaryOrange else OnSurfaceVariant
                    )
                }
                
                Spacer(Modifier.width(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, Modifier.size(20.dp), tint = OnSurfaceVariant)
                    Text("${post.commentsCount}", Modifier.padding(start = 6.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                }
                
                Spacer(Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (post.shareCount > 0) {
                        Text("${post.shareCount}", fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    IconButton(onClick = onShare) {
                    Icon(Icons.Outlined.Share, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarBubble(name: String, imageUrl: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PrimaryElectricBlue.copy(alpha = 0.16f))
            .border(1.dp, PrimaryElectricBlue.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString(""),
            color = OnSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size / 3).sp
        )
        AsyncImage(
            model = imageUrl.ifBlank { "https://api.dicebear.com/7.x/avataaars/svg?seed=${name.replace(" ", "")}" },
            contentDescription = null,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun FeedImage(imageUrl: String, label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Image, null, tint = OnSurfaceVariant, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = OnSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
