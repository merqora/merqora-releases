package com.rendly.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.rendly.app.ui.screens.auth.LoginScreen
import com.rendly.app.ui.screens.auth.RegisterScreen
import com.rendly.app.ui.screens.main.MainScreen
import com.rendly.app.ui.screens.messages.MessagesScreen
import com.rendly.app.ui.screens.notifications.NotificationsScreen
import com.rendly.app.ui.screens.live.LiveStreamsListScreen
import com.rendly.app.ui.screens.live.LiveViewerScreen
import com.rendly.app.ui.screens.checkout.CheckoutScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Messages : Screen("messages")
    object Notifications : Screen("notifications")
    object LiveStreams : Screen("live_streams")
    object LiveViewer : Screen("live_viewer/{streamId}") {
        fun createRoute(streamId: String): String = "live_viewer/$streamId"
    }
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object ProductDetail : Screen("product/{postId}") {
        fun createRoute(postId: String) = "product/$postId"
    }
    object Checkout : Screen("checkout")
}

@Composable
fun MerqoraNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            MainScreen(navController = navController)
        }
        
        composable(
            Screen.Messages.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            MessagesScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { /* TODO: Open conversation */ }
            )
        }
        
        composable(
            Screen.Notifications.route,
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(200)
                )
            }
        ) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onNotificationClick = { /* TODO: Handle notification */ }
            )
        }
        
        // Live Streams List
        composable(
            Screen.LiveStreams.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            LiveStreamsListScreen(
                onBack = { navController.popBackStack() },
                onStreamClick = { stream ->
                    navController.navigate(Screen.LiveViewer.createRoute(stream.id))
                }
            )
        }
        
        // Live Viewer
        composable(
            Screen.LiveViewer.route,
            arguments = listOf(
                navArgument("streamId") { type = NavType.StringType }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            
            LiveViewerScreen(
                streamId = streamId,
                onClose = { navController.popBackStack() }
            )
        }
        
        // Checkout Screen
        composable(
            Screen.Checkout.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onPaymentComplete = { order ->
                    navController.popBackStack()
                },
                onContinueShopping = {
                    navController.popBackStack()
                }
            )
        }
    }
}
