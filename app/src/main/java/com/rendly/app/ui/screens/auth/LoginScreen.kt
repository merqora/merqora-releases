package com.rendly.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rendly.app.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {}
) {
    // -------------------------------------------------------------------
    // COLD START ULTRA: SIN hiltViewModel() hasta que se necesite
    // El ViewModel solo se carga cuando el usuario pulsa "Iniciar Sesión"
    // Esto evita ~500ms de Hilt DI en el primer frame
    // -------------------------------------------------------------------
    
    // -------------------------------------------------------------------
    // SESSION PERSISTENCE: Verificar Sesión guardada (operación instantánea)
    // Si hay Sesión Válida, navegar directamente al Home
    // -------------------------------------------------------------------
    var checkingSession by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (com.rendly.app.data.remote.SessionPersistence.isLoggedIn()) {
            // Sesión guardada - navegar al Home directamente
            onNavigateToHome()
            return@LaunchedEffect
        }
        checkingSession = false
    }
    
    // Estado local - UI pura sin ViewModel
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var recibirNovedades by remember { mutableStateOf(false) }
    
    // Estado de auth - solo se activa cuando usuario pulsa Botón
    var authTriggered by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    // Si estamos verificando Sesión, no mostrar nada (evita flash)
    if (checkingSession) return
    
    // ViewModel solo se carga cuando authTriggered = true
    if (authTriggered) {
        val viewModel: LoginViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()
        
        // Sincronizar estado
        LaunchedEffect(uiState) {
            isLoading = uiState.isLoading
            errorMessage = uiState.errorMessage
            successMessage = uiState.successMessage
            if (uiState.isAuthenticated) {
                onNavigateToHome()
            }
        }
        
        // Ejecutar login cuando se active
        LaunchedEffect(Unit) {
            viewModel.login(emailOrUsername, password, recibirNovedades)
        }
    }
    
    // SIN animaciones en el primer frame - diferir
    var animationsReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        animationsReady = true
    }
    
    val pulseScale = if (animationsReady) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        ).value
    } else 1f
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Fondo simple - sin Canvas pesado en primer frame
        if (animationsReady) {
            AuthBackgroundDesign(gradientOffset = 0f)
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            
            // Header: Logo + título compacto
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = com.rendly.app.R.mipmap.ic_launcher),
                    contentDescription = "Merqora",
                    modifier = Modifier
                        .size(70.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(20.dp))
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Bienvenido",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "Descubre, conecta y compra",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            // Card de formulario con glassmorphism
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Surface.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = 0.3f),
                            AccentPink.copy(alpha = 0.1f),
                            PrimaryPurple.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Notificación de éxito
                    AnimatedVisibility(
                        visible = successMessage.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2E8B57).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = successMessage,
                                    color = Color(0xFF2E8B57),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    
                    // Campo Email/Username
                    PremiumTextField(
                        value = emailOrUsername,
                        onValueChange = { emailOrUsername = it },
                        label = "Correo o usuario",
                        placeholder = "tu@email.com",
                        leadingIcon = Icons.Outlined.Person,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo Password
                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "contraseña",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Outlined.Lock,
                        trailingIcon = {
                            IconButton(
                                onClick = { showPassword = !showPassword },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Checkbox + Olvidaste Contraseña
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { recibirNovedades = !recibirNovedades }
                        ) {
                            Checkbox(
                                checked = recibirNovedades,
                                onCheckedChange = { recibirNovedades = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryPurple,
                                    uncheckedColor = TextMuted
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recordarme",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        
                        Text(
                            text = "¿Olvidaste?",
                            color = PrimaryPurple,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToForgotPassword() }
                        )
                    }
                    
                    // Error message
                    AnimatedVisibility(
                        visible = errorMessage.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = AccentPink.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = AccentPink,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage,
                                    color = AccentPink,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // botón Login Premium - activa authTriggered para cargar ViewModel
                    Button(
                        onClick = { authTriggered = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading && emailOrUsername.isNotEmpty() && password.isNotEmpty(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = SurfaceElevated
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (!isLoading && emailOrUsername.isNotEmpty() && password.isNotEmpty())
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B35),
                                                Color(0xFF2E8B57)
                                            )
                                        )
                                    else Brush.horizontalGradient(
                                        colors = listOf(SurfaceElevated, SurfaceElevated)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Iniciar Sesión",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Footer compacto
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // botón Guest - navega directamente sin auth
                Surface(
                    onClick = { onNavigateToHome() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Explore, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explorar sin cuenta",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Nuevo en Merqora?", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Crear cuenta",
                        color = PrimaryPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// COMPONENTES PREMIUM COMPARTIDOS
// ----------------------------------------------------------------

@Composable
fun AuthBackgroundDesign(gradientOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Orbe superior derecho - Círculo con gradiente radial
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    PrimaryPurple.copy(alpha = 0.35f),
                    PrimaryPurple.copy(alpha = 0.15f),
                    PrimaryPurple.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(width * 0.85f, height * 0.08f),
                radius = width * 0.6f
            ),
            center = Offset(width * 0.85f, height * 0.08f),
            radius = width * 0.6f
        )
        
        // Orbe inferior izquierdo - Círculo con gradiente radial
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AccentPink.copy(alpha = 0.3f),
                    AccentPink.copy(alpha = 0.12f),
                    AccentPink.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(width * 0.15f, height * 0.92f),
                radius = width * 0.55f
            ),
            center = Offset(width * 0.15f, height * 0.92f),
            radius = width * 0.55f
        )
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextMuted,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = HomeBg.copy(alpha = 0.5f),
                unfocusedContainerColor = HomeBg.copy(alpha = 0.3f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = PrimaryPurple
            )
        )
    }
}
