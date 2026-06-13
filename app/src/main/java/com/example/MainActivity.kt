package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
                val fanArenaViewModel: FanArenaViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(context))
                LaunchedEffect(Unit) {
                    fanArenaViewModel.restoreSession()
                }
                MainAppOrchestrator(viewModel = fanArenaViewModel)
            }
        }
    }
}

object ScreenRoute {
    const val Login = "login"
    const val Dashboard = "dashboard"
    const val Matches = "matches"
    const val MatchDetail = "match_detail"
    const val Feed = "feed"
    const val PostDetail = "post_detail"
    const val CreatePost = "create_post"
    const val Profile = "profile"
    const val Shop = "shop"
    const val Mission = "missions"
    const val Favorites = "favorites"
    const val PredictionHistory = "prediction_history"
    const val AiAssistant = "ai_assistant"
    const val Leaderboard = "leaderboard"
    const val Notifications = "notifications"
}

@Composable
fun MainAppOrchestrator(viewModel: FanArenaViewModel) {
    val navController = rememberNavController()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(loggedInUser, currentRoute) {
        if (loggedInUser != null && currentRoute == ScreenRoute.Login) {
            navController.navigate(ScreenRoute.Dashboard) {
                popUpTo(ScreenRoute.Login) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val mainTabs = listOf(ScreenRoute.Dashboard, ScreenRoute.Matches, ScreenRoute.Feed, ScreenRoute.Leaderboard, ScreenRoute.Profile)
            if (loggedInUser != null && mainTabs.any { currentRoute?.startsWith(it) == true }) {
                NavigationBar(containerColor = SurfaceLowest) {
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Dashboard,
                        onClick = { navController.navigate(ScreenRoute.Dashboard) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Matches,
                        onClick = { navController.navigate(ScreenRoute.Matches) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.PlayArrow, null) }, label = { Text("Live", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Feed,
                        onClick = { navController.navigate(ScreenRoute.Feed) { launchSingleTop = true } },
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, null) }, label = { Text("Feed", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Leaderboard,
                        onClick = { navController.navigate(ScreenRoute.Leaderboard) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Star, null) }, label = { Text("Leader", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Profile,
                        onClick = { navController.navigate(ScreenRoute.Profile) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Person, null) }, label = { Text("Vault", fontSize = 10.sp) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (loggedInUser != null && currentRoute == ScreenRoute.Dashboard) {
                FloatingActionButton(
                    onClick = { navController.navigate(ScreenRoute.AiAssistant) },
                    containerColor = PrimaryElectricBlue,
                    contentColor = OnPrimary
                ) { Icon(Icons.Default.Psychology, "AI Assistant") }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Login,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenRoute.Login) { LoginScreen(viewModel, onLoginSuccess = { navController.navigate(ScreenRoute.Dashboard) }) }
            composable(ScreenRoute.Dashboard) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMatch = { id -> navController.navigate("${ScreenRoute.MatchDetail}/$id") },
                    onNavigateToCreatePost = { navController.navigate(ScreenRoute.CreatePost) },
                    onNavigateToFeed = { navController.navigate(ScreenRoute.Feed) },
                    onNavigateToBadges = { navController.navigate(ScreenRoute.Profile) },
                    onNavigateToNotifications = { navController.navigate(ScreenRoute.Notifications) },
                    onNavigateToMissions = { navController.navigate(ScreenRoute.Mission) }
                )
            }
            composable(ScreenRoute.Matches) { MatchesScreen(viewModel, onNavigateToMatch = { id -> navController.navigate("${ScreenRoute.MatchDetail}/$id") }) }
            composable(
                route = "${ScreenRoute.MatchDetail}/{matchId}",
                arguments = listOf(navArgument("matchId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("matchId") ?: 0L
                MatchDetailScreen(id, onBack = { navController.popBackStack() })
            }
            composable(ScreenRoute.Feed) { CommunityFeedScreen(viewModel, { navController.navigate(ScreenRoute.CreatePost) }, { id -> navController.navigate("${ScreenRoute.PostDetail}/$id") }) }
            composable(ScreenRoute.CreatePost) { CreatePostScreen(viewModel, onBack = { navController.popBackStack() }, onPublishSuccess = { navController.navigate(ScreenRoute.Feed) }) }
            composable("${ScreenRoute.PostDetail}/{postId}", listOf(navArgument("postId") { type = NavType.StringType })) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("postId") ?: ""
                PostDetailScreen(id, viewModel, onBack = { navController.popBackStack() })
            }
            composable(ScreenRoute.Leaderboard) { LeaderboardScreen() }
            composable(ScreenRoute.Profile) {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = { navController.navigate(ScreenRoute.Login) { popUpTo(0) } },
                    onOpenShop = { navController.navigate(ScreenRoute.Shop) },
                    onNavigateToMissions = { navController.navigate(ScreenRoute.Mission) },
                    onNavigateToPredictionHistory = { navController.navigate(ScreenRoute.PredictionHistory) },
                    onNavigateToFavoriteSelection = { navController.navigate(ScreenRoute.Favorites) }
                )
            }
            composable(ScreenRoute.Shop) { ShopScreen(viewModel, onBack = { navController.popBackStack() }) }
            composable(ScreenRoute.Favorites) { FavoriteSelectionScreen(onBack = { navController.popBackStack() }) }
            composable(ScreenRoute.PredictionHistory) { PredictionHistoryScreen(onBack = { navController.popBackStack() }) }
            composable(ScreenRoute.AiAssistant) { AiAssistantScreen(onBack = { navController.popBackStack() }) }
            composable(ScreenRoute.Mission) { MissionScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(ScreenRoute.Notifications) { NotificationScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
