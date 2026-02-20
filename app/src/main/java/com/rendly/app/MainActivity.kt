package com.rendly.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
// installSplashScreen REMOVIDO - solo usamos XML splash para máxima velocidad
import androidx.navigation.compose.rememberNavController
import com.rendly.app.data.preferences.AppPreferences
import com.rendly.app.data.repository.MercadoPagoRepository
import com.rendly.app.ui.navigation.MerqoraNavHost
import com.rendly.app.ui.navigation.Screen
import com.rendly.app.ui.theme.MerqoraTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // Estado para controlar el splash screen - marcar ready INMEDIATAMENTE
    // para mostrar UI instantáneamente mientras cargamos datos en background
    private var isReady = false
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permiso de notificaciones concedido o denegado
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // ═══════════════════════════════════════════════════════════════════
        // COLD START ULTRA - Target: < 500ms hasta LoginScreen visible
        // SIN installSplashScreen() - evitamos overhead del SplashScreen API
        // ═══════════════════════════════════════════════════════════════════
        
        super.onCreate(savedInstanceState)
        
        // ❌ REMOVIDO del Main Thread - ahora es lazy
        // ChatRepository.init(this)
        
        // StatusBar = HomeBg, NavigationBar/TabBar = mismo color que BottomNavBar
        val homeBgColor = Color.parseColor("#0A0A0F")    // StatusBar
        val navbarBgColor = Color.parseColor("#080C12")  // NavigationBar = TabBarBg/NavbarBg
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(homeBgColor),
            navigationBarStyle = SystemBarStyle.dark(navbarBgColor)
        )
        
        // Solicitar permiso DESPUÉS de setContent (no bloquea UI)
        
        // ═══════════════════════════════════════════════════════════════════
        // COLD START OPTIMIZATION V5: View XML splash (NO Compose overhead)
        //
        // Estrategia: Mostrar layout XML nativo ANTES de inicializar Compose.
        // Esto evita el class loading de Compose (~1.3s) en el primer frame.
        // ═══════════════════════════════════════════════════════════════════
        
        // Iniciar Compose directamente - el system splash (blanco con ic_launcher_round)
        // se muestra automáticamente durante el cold start via Theme.Merqora
        isReady = true
        initializeComposeUI()
        
        // Reportar fully drawn
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            reportFullyDrawn()
        }
    }
    
    private fun initializeComposeUI() {
        setContent {
            
            // ═══════════════════════════════════════════════════════════════════
            // COLD START ULTRA: NO cargar NADA aquí
            // - Sin Supabase
            // - Sin verificación de sesión  
            // - Sin SDKs
            // Todo se carga LAZY cuando el usuario interactúa
            // ═══════════════════════════════════════════════════════════════════
            
            // Cargar preferencias de tema
            val context = LocalContext.current
            val preferences = remember { AppPreferences(context) }
            var themeMode by remember { mutableStateOf(AppPreferences.THEME_DARK) }
            var accentColor by remember { mutableStateOf(AppPreferences.ACCENT_PURPLE) }
            
            LaunchedEffect(Unit) {
                launch { preferences.themeFlow.collect { themeMode = it } }
                launch { preferences.accentColorFlow.collect { accentColor = it } }
            }
            
            MerqoraTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val navController = rememberNavController()
                    
                    MerqoraNavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    )
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
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // MERCADO PAGO DEEP LINK HANDLING
    // Maneja el retorno desde el checkout de Mercado Pago
    // Deep links: Merqora://payment/success, Merqora://payment/failure, Merqora://payment/pending
    // ═══════════════════════════════════════════════════════════════════════════════
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleMercadoPagoDeepLink(intent)
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
