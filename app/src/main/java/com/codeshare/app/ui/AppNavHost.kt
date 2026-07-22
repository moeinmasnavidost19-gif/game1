package com.codeshare.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codeshare.app.ui.screens.AdminPanelScreen
import com.codeshare.app.ui.screens.AuthScreen
import com.codeshare.app.ui.screens.BookmarksScreen
import com.codeshare.app.ui.screens.DashboardScreen
import com.codeshare.app.ui.screens.FileDetailScreen
import com.codeshare.app.ui.screens.HomeScreen
import com.codeshare.app.ui.screens.LeaderboardScreen
import com.codeshare.app.ui.screens.NotificationsScreen
import com.codeshare.app.ui.screens.ProfileScreen
import com.codeshare.app.ui.screens.SettingsScreen
import com.codeshare.app.ui.screens.SplashScreen
import com.codeshare.app.ui.screens.UploadScreen

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
    const val DETAIL = "detail/{fileId}"
    const val UPLOAD = "upload?editId={editId}"
    const val PROFILE = "profile/{userId}"
    const val ADMIN = "admin"
    const val DASHBOARD = "dashboard"
    const val LEADERBOARD = "leaderboard"
    const val BOOKMARKS = "bookmarks"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"

    fun detail(fileId: String) = "detail/$fileId"
    fun upload(editId: String? = null) = if (editId == null) "upload?editId=" else "upload?editId=$editId"
    fun profile(userId: String) = "profile/$userId"
}

@Composable
fun AppNavHost(vm: AppViewModel, nav: NavHostController = rememberNavController()) {
    val snackbar = remember { SnackbarHostState() }
    val message by vm.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { pad ->
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(pad),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(350))
            },
        ) {
            composable(Routes.SPLASH) { SplashScreen(vm, nav) }
            composable(Routes.AUTH) { AuthScreen(vm, nav) }
            composable(Routes.HOME) { HomeScreen(vm, nav) }
            composable(Routes.DETAIL) { back ->
                FileDetailScreen(vm, nav, back.arguments?.getString("fileId") ?: "")
            }
            composable(Routes.UPLOAD) { back ->
                UploadScreen(vm, nav, back.arguments?.getString("editId").takeUnless { it.isNullOrBlank() })
            }
            composable(Routes.PROFILE) { back ->
                ProfileScreen(vm, nav, back.arguments?.getString("userId") ?: "")
            }
            composable(Routes.ADMIN) { AdminPanelScreen(vm, nav) }
            composable(Routes.DASHBOARD) { DashboardScreen(vm, nav) }
            composable(Routes.LEADERBOARD) { LeaderboardScreen(vm, nav) }
            composable(Routes.BOOKMARKS) { BookmarksScreen(vm, nav) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(vm, nav) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
        }
    }
}
