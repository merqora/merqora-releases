package com.mercora.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.NavType
import coil.compose.AsyncImage
import com.mercora.app.data.model.Post
import com.mercora.app.data.model.Notification
import com.mercora.app.data.model.NotificationType
import com.mercora.app.data.model.WelcomeState
import com.mercora.app.data.repository.PostRepository
import com.mercora.app.data.repository.ProfileRepository
import com.mercora.app.ui.components.LocalOpenProductPreview
import com.mercora.app.ui.components.ProductPage
import com.mercora.app.ui.components.ProductPreviewConfig
import com.mercora.app.ui.components.WelcomeOverlay
import com.mercora.app.ui.screens.auth.LoginScreen
import com.mercora.app.ui.screens.auth.RegisterScreen
import com.mercora.app.ui.screens.main.MainScreen
import com.mercora.app.ui.screens.messages.MessagesScreen
import com.mercora.app.ui.screens.notifications.NotificationsScreen
import com.mercora.app.ui.screens.live.LiveStreamsListScreen
import com.mercora.app.ui.screens.live.LiveViewerScreen
import com.mercora.app.ui.screens.checkout.CheckoutScreen
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch

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
fun MercoraNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    var showProductPreview by remember { mutableStateOf(false) }
    var previewPost by remember { mutableStateOf<Post?>(null) }
    var previewOnContactSeller by remember { mutableStateOf<((Post) -> Unit)?>(null) }

    val openProductPreview: (ProductPreviewConfig) -> Unit = { config ->
        previewPost = config.post
        previewOnContactSeller = config.onContactSeller
        showProductPreview = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CompositionLocalProvider(LocalOpenProductPreview provides openProductPreview) {
            NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(
                Screen.Login.route,
                enterTransition = { fadeIn(animationSpec = tween(280)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
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
        
        composable(
            Screen.Register.route,
            enterTransition = { fadeIn(animationSpec = tween(280)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
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
        
        composable(
            Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }
        ) {
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
                onBack = { navController.popBackStack() }
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
                onNotificationClick = { notification ->
                    when (notification.type) {
                        NotificationType.FOLLOW,
                        NotificationType.CLIENT_REQUEST,
                        NotificationType.CLIENT_ACCEPTED,
                        NotificationType.CLIENT_REJECTED,
                        NotificationType.CLIENT_PENDING,
                        NotificationType.FOLLOW_REQUEST,
                        NotificationType.FOLLOW_ACCEPTED,
                        NotificationType.FOLLOW_REJECTED -> {
                            navController.navigate(Screen.Profile.createRoute(notification.senderId))
                        }
                        NotificationType.LIKE,
                        NotificationType.SAVE,
                        NotificationType.COMMENT,
                        NotificationType.MENTION -> {
                            val postId = notification.postId
                            if (postId != null) {
                                navController.navigate(Screen.ProductDetail.createRoute(postId))
                            }
                        }
                        NotificationType.UNKNOWN -> {}
                    }
                }
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
                onClose = { navController.popBackStack() },
                onNavigateToProduct = { postId ->
                    navController.navigate(Screen.ProductDetail.createRoute(postId))
                }
            )
        }
        
        // Profile Viewer (other user)
        composable(
            Screen.Profile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            ),
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
            }
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            val scope = rememberCoroutineScope()
            val profile by ProfileRepository.selectedProfile.collectAsState()
            val isLoading by ProfileRepository.selectedProfileIsLoading.collectAsState()
            
            LaunchedEffect(userId) {
                ProfileRepository.loadProfileByUserId(userId)
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = profile?.username ?: "Perfil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else if (profile == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Usuario no encontrado", color = TextMuted, fontSize = 16.sp)
                    }
                } else {
                    val p = profile!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (p.avatarUrl != null) {
                            AsyncImage(
                                model = p.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = p.username,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        if (p.nombre != null) {
                            Text(
                                text = p.nombre,
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        
                        if (p.descripcion != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = p.descripcion,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(value = p.publicaciones.toString(), label = "Publicaciones")
                            StatItem(value = p.seguidores.toString(), label = "Seguidores")
                            StatItem(value = p.clientes.toString(), label = "Clientes")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar mensaje", color = Color.White)
                        }
                    }
                }
            }
        }
        
        // Product Detail
        composable(
            Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://vinzay.app/p/{postId}" },
                navDeepLink { uriPattern = "https://vinzay.netlify.app/p/{postId}" }
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(200)
                )
            }
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val openDetailPreview = LocalOpenProductPreview.current
            val detailScope = rememberCoroutineScope()
            
            var isLoading by remember { mutableStateOf(true) }
            var notFound by remember { mutableStateOf(false) }
            
            LaunchedEffect(postId) {
                isLoading = true
                val posts = PostRepository.getPostsByIds(listOf(postId))
                val loadedPost = posts.firstOrNull()
                isLoading = false
                if (loadedPost != null) {
                    openDetailPreview(ProductPreviewConfig(post = loadedPost))
                    navController.popBackStack()
                } else {
                    notFound = true
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HomeBg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else if (notFound) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HomeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Producto no encontrado", color = TextMuted, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text("Volver", color = PrimaryPurple)
                        }
                    }
                }
            }
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

        // Welcome overlay SIEMPRE en composiciÃ³n, invisible hasta activarse
        // Prevenir jank del primer frame evitando que entre/salga del Ã¡rbol de composiciÃ³n
        val welcomeData by WelcomeState.welcome.collectAsState()
        WelcomeOverlay(
            username = welcomeData.username,
            isVisible = welcomeData.show,
            onAnimationEnd = {
                WelcomeState.consume()
            }
        )

        // Product preview overlay (centralized, used by all tab screens)
        val context = LocalContext.current
        AnimatedVisibility(
            visible = showProductPreview,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(200)
            )
        ) {
            previewPost?.let { post ->
                ProductPage(
                    post = post,
                    isVisible = true,
                    onDismiss = {
                        showProductPreview = false
                    },
                    onBuyNow = { p ->
                        showProductPreview = false
                        navController.navigate(Screen.Checkout.route)
                    },
                    onAddToCart = { p ->
                        android.widget.Toast.makeText(
                            context,
                            "Agregado al carrito",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onContactSeller = previewOnContactSeller ?: {},
                    onViewProfile = { p ->
                        showProductPreview = false
                        navController.navigate(Screen.Profile.createRoute(p.userId))
                    },
                    onRelatedPostClick = { p ->
                        previewPost = p
                    },
                    onNavigateToCheckout = {
                        showProductPreview = false
                        navController.navigate(Screen.Checkout.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}
