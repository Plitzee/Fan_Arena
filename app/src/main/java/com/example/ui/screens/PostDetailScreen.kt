package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Comment
import com.example.data.Post
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    viewModel: FanArenaViewModel,
    onBack: () -> Unit
) {
    val posts by viewModel.posts.collectAsState()
    val post = posts.find { it.firestoreId == postId }
    val comments by viewModel.getCommentsForPost(postId).collectAsState(emptyList())
    var commentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discussion", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLowest)
            )
        },
        containerColor = BackgroundNavy
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (post != null) {
                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    item {
                        PostHeader(post)
                        Spacer(Modifier.height(16.dp))
                        Text("Comments (${comments.size})", fontWeight = FontWeight.Bold, color = OnSurface)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                }
            }

            // Input Area
            Surface(Modifier.fillMaxWidth(), color = SurfaceLowest) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Write a comment...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainer,
                            unfocusedContainerColor = SurfaceContainer
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addCommentOnPost(postId, commentText)
                                commentText = ""
                            }
                        },
                        modifier = Modifier.background(PrimaryElectricBlue, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PostHeader(post: Post) {
    Column(Modifier.padding(vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.authorAvatarUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceContainer)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(post.authorName, fontWeight = FontWeight.ExtraBold, color = OnSurface)
                Text(post.sportCategory, fontSize = 11.sp, color = OnSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(post.postText, fontSize = 16.sp, color = OnSurface, lineHeight = 24.sp)
        if (post.imageUrl.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(Modifier.padding(vertical = 8.dp)) {
        AsyncImage(
            model = comment.authorAvatarUrl,
            contentDescription = null,
            modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceContainer)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.background(SurfaceLowest, RoundedCornerShape(12.dp)).padding(12.dp)) {
            Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryElectricBlue)
            Text(comment.text, fontSize = 13.sp, color = OnSurface)
        }
    }
}
