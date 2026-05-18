package com.gramasethu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gramasethu.app.data.model.Bridge
import com.gramasethu.app.ui.alerts.AlertsScreen
import com.gramasethu.app.ui.auth.AuthViewModel
import com.gramasethu.app.ui.auth.LoginScreen
import com.gramasethu.app.ui.map.MapScreen
import com.gramasethu.app.ui.profile.ProfileScreen
import com.gramasethu.app.ui.report.ReportScreen
import com.gramasethu.app.ui.splash.SplashScreen
import com.gramasethu.app.ui.theme.GramaSethuTheme
import com.gramasethu.app.utils.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAP = "map"
    const val REPORT = "report"
    const val ALERTS = "alerts"
    const val PROFILE = "profile"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup notification channel
        NotificationUtils.createNotificationChannel(this)

        setContent {
            GramaSethuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GramaSethuNavigation()
                }
            }
        }
    }
}

@Composable
fun GramaSethuNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    // Store selected bridge for report screen
    var selectedBridge by remember { mutableStateOf<Bridge?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH  // Always show splash first
    ) {

        // 1. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    val destination = if (authViewModel.isLoggedIn)
                        Routes.MAP else Routes.LOGIN
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 3. Map Screen (Main Screen)
        composable(Routes.MAP) {
            MapScreen(
                onNavigateToReport = { bridge ->
                    selectedBridge = bridge
                    navController.navigate(Routes.REPORT)
                },
                onNavigateToAlerts = {
                    navController.navigate(Routes.ALERTS)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAP) { inclusive = true }
                    }
                }
            )
        }

        // 4. Report Screen
        composable(Routes.REPORT) {
            selectedBridge?.let { bridge ->
                ReportScreen(
                    bridge = bridge,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // 5. Alerts Screen
        composable(Routes.ALERTS) {
            AlertsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Profile Screen
        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAP) { inclusive = true }
                    }
                }
            )
        }
    }
}