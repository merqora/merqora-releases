package com.mercora.app

import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
// installSplashScreen REMOVIDO - solo usamos XML splash para máxima velocidad
import androidx.navigation.compose.rememberNavController
import com.mercora.app.data.preferences.AppPreferences
import com.mercora.app.data.repository.AppUpdateRepository
import com.mercora.app.data.repository.MercadoPagoRepository
import com.mercora.app.service.MercoraFirebaseMessagingService
import com.mercora.app.startup.StartupOptimizer
import com.mercora.app.ui.navigation.MercoraNavHost
import com.mercora.app.ui.navigation.Screen
import com.mercora.app.ui.components.ErrorBoundary
import com.mercora.app.ui.components.NotificationBanner
import com.mercora.app.ui.components.UpdateDialog
import com.mercora.app.ui.theme.MercoraTheme
import com.mercora.app.util.NotificationBannerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

    @AndroidEntryPoint
    class MainActivity : FragmentActivity() {
        
        private var pendingUpdate: AppUpdateRepository.UpdateInfo? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permiso de notificaciones concedido o denegado
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // -------------------------------------------------------------------
        // COLD START ULTRA - Target: < 500ms hasta LoginScreen visible
        // SIN installSplashScreen() - evitamos overhead del SplashScreen API
        // -------------------------------------------------------------------
        
        super.onCreate(savedInstanceState)
        
        // ? REMOVIDO del Main Thread - ahora es lazy
        // ChatRepository.init(this)
        
        // Tracker de foreground/background para FCM
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                MercoraFirebaseMessagingService.setAppInForeground(true)
            }
            override fun onStop(owner: LifecycleOwner) {
                MercoraFirebaseMessagingService.setAppInForeground(false)
            }
        })
        
        // StatusBar = HomeBg, NavigationBar/TabBar = mismo color que BottomNavBar
        val homeBgColor = Color.parseColor("#0A0A0F")    // StatusBar
        val navbarBgColor = Color.parseColor("#080C12")  // NavigationBar = TabBarBg/NavbarBg
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(homeBgColor),
            navigationBarStyle = SystemBarStyle.dark(navbarBgColor)
        )
        
        // Solicitar permiso DESPUÉS de setContent (no bloquea UI)
        requestNotificationPermission()

        // -------------------------------------------------------------------
        // COLD START OPTIMIZATION V5: View XML splash (NO Compose overhead)
        //
        // Estrategia: Mostrar layout XML nativo ANTES de inicializar Compose.
        // Esto evita el class loading de Compose (~1.3s) en el primer frame.
        // -------------------------------------------------------------------
        
        // Iniciar Compose directamente - el system splash (blanco con ic_launcher_round)
        // se muestra automáticamente durante el cold start via Theme.Mercora
        initializeComposeUI()
        
        // Inicialización diferida post-first-frame
        StartupOptimizer.initDeferred(this) {
            // Después de init diferido: verificar actualizaciones
            kotlinx.coroutines.MainScope().launch {
                val updateInfo = AppUpdateRepository.checkForUpdate()
                if (updateInfo?.hasUpdate == true) {
                    Log.d("MainActivity", "Actualización disponible: ${updateInfo.latest.version_name}")
                    pendingUpdate = updateInfo
                }
            }
        }
        
        // Manejar deep link desde notificación push
        handleNotificationDeepLink(intent)
        
        // Reportar fully drawn
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            reportFullyDrawn()
        }
    }
    
    private fun initializeComposeUI() {
        setContent {
            
            // -------------------------------------------------------------------
            // COLD START ULTRA: NO cargar NADA aquí
            // - Sin Supabase
            // - Sin verificación de sesión  
            // - Sin SDKs
            // Todo se carga LAZY cuando el usuario interactúa
            // -------------------------------------------------------------------
            
            // Cargar preferencias de tema
            val context = LocalContext.current
            val preferences = remember { AppPreferences(context) }
            var themeMode by remember { mutableStateOf(AppPreferences.THEME_DARK) }
            var accentColor by remember { mutableStateOf(AppPreferences.ACCENT_PURPLE) }
            
            LaunchedEffect(Unit) {
                combine(
                    preferences.themeFlow,
                    preferences.accentColorFlow
                ) { theme, accent -> Pair(theme, accent) }
                    .collect { (theme, accent) ->
                        themeMode = theme
                        accentColor = accent
                    }
            }
            
            val navController = rememberNavController()
            MercoraTheme(
                navController = navController,
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    ErrorBoundary {
                        // Splash dismiss callback — LoginScreen lo llama cuando auth
                        // se completa, para que el enrollment no quede detrás.
                        var splashVisible by remember { mutableStateOf(true) }
                        var splashFinished by remember { mutableStateOf(false) }
                        val dismissSplash: () -> Unit = {
                            splashFinished = true
                            splashVisible = false
                        }

                        MercoraNavHost(
                            navController = navController,
                            startDestination = Screen.Login.route,
                            dismissSplash = dismissSplash
                        )

                        // Banner de notificaciones in-app
                        var currentEvent by remember { mutableStateOf<com.mercora.app.util.NotificationBannerEvent?>(null) }
                        LaunchedEffect(Unit) {
                            NotificationBannerManager.events.collect { event ->
                                currentEvent = event
                            }
                        }
                        currentEvent?.let { event ->
                            Box(
                                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                NotificationBanner(
                                    event = event,
                                    onDismiss = { currentEvent = null },
                                    onClick = {
                                        currentEvent = null
                                        when (event.type) {
                                            "message" -> navController.navigate(Screen.Messages.route)
                                            "like", "save", "comment", "mention" -> {
                                                event.targetId?.let { postId ->
                                                    navController.navigate("product/$postId")
                                                }
                                            }
                                            "follow" -> {
                                                event.userId?.let { userId ->
                                                    navController.navigate("profile/$userId")
                                                }
                                            }
                                            "sale", "handshake" -> navController.navigate("checkout")
                                        }
                                    }
                                )
                            }
                        }

                        // Diálogo de actualización disponible
                        pendingUpdate?.let { updateInfo ->
                            UpdateDialog(
                                updateInfo = updateInfo,
                                onDismiss = { pendingUpdate = null },
                                onUpdate = {
                                    pendingUpdate = null
                                    AppUpdateRepository.downloadAndInstall(
                                        this@MainActivity,
                                        updateInfo.latest
                                    )
                                }
                            )
                        }

                        if (splashVisible) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = !splashFinished,
                                enter = androidx.compose.animation.EnterTransition.None,
                                exit = androidx.compose.animation.fadeOut(
                                    animationSpec = androidx.compose.animation.core.tween(200)
                                )
                            ) {
                                com.mercora.app.ui.MercoraSplashExact(
                                    onFinished = { splashFinished = true }
                                )
                            }
                            LaunchedEffect(splashFinished) {
                                if (splashFinished) {
                                    kotlinx.coroutines.delay(80)
                                    splashVisible = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permiso ya concedido
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    
    // -------------------------------------------------------------------------------
    // MERCADO PAGO DEEP LINK HANDLING
    // Maneja el retorno desde el checkout de Mercado Pago
    // Deep links: mercora://payment/success, mercora://payment/failure, mercora://payment/pending
    // -------------------------------------------------------------------------------
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleMercadoPagoDeepLink(intent)
        handleNotificationDeepLink(intent)
    }
    
    private fun handleNotificationDeepLink(intent: Intent?) {
        val openScreen = intent?.getStringExtra("open_screen") ?: return
        Log.d("MainActivity", "Notification deep link: $openScreen")
        // La navegación se maneja cuando el usuario está autenticado
        // Guardamos el intent para procesarlo post-login
        getSharedPreferences("mercora_nav", Context.MODE_PRIVATE)
            .edit()
            .putString("pending_open_screen", openScreen)
            .putString("pending_chat_id", intent.getStringExtra("chat_id"))
            .putString("pending_post_id", intent.getStringExtra("post_id"))
            .putString("pending_user_id", intent.getStringExtra("user_id"))
            .putString("pending_transaction_id", intent.getStringExtra("transaction_id"))
            .putString("pending_call_id", intent.getStringExtra("call_id"))
            .apply()
    }
    
    private fun handleMercadoPagoDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        
        // Verificar si es un deep link de Mercado Pago
        if (data.scheme == MercadoPagoRepository.DEEP_LINK_SCHEME && data.host == "payment") {
            Log.d("MainActivity", "Mercado Pago deep link recibido: $data")
            
            // Delegar al repository para procesar el resultado
            MercadoPagoRepository.handlePaymentDeepLink(intent)
            
            // El CheckoutScreen observará el estado de MercadoPagoRepository.paymentState
            // y actualizará la UI según el resultado del pago
        }
    }
}
