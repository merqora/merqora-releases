package com.rendly.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rendly.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var generoExpanded by remember { mutableStateOf(false) }
    
    // Animaciones premium
    val infiniteTransition = rememberInfiniteTransition(label = "bg_animation")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            kotlinx.coroutines.delay(1000)
            onNavigateToHome()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Fondo premium
        AuthBackgroundDesign(gradientOffset = gradientOffset)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Header con botón volver
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = SurfaceElevated
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Logo Premium
            Image(
                painter = painterResource(id = com.rendly.app.R.mipmap.ic_launcher),
                contentDescription = "Merqora",
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(22.dp))
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Crea tu cuenta",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-1).sp
            )
            
            Text(
                text = "Comienza a descubrir y compartir",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
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
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Email
                    PremiumTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        placeholder = "tu@email.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Username
                    PremiumTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Nombre de usuario",
                        placeholder = "@usuario",
                        leadingIcon = Icons.Outlined.AlternateEmail,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Password
                    PremiumTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        placeholder = "Mínimo 6 caracteres",
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
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Género y Fecha en fila
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Género
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Género",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            
                            ExposedDropdownMenuBox(
                                expanded = generoExpanded,
                                onExpandedChange = { generoExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = when(genero) {
                                        "hombre" -> "Hombre"
                                        "mujer" -> "Mujer"
                                        "otro" -> "Otro"
                                        else -> ""
                                    },
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    readOnly = true,
                                    placeholder = { Text("Seleccionar", color = TextMuted, fontSize = 14.sp) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = generoExpanded) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedContainerColor = HomeBg.copy(alpha = 0.5f),
                                        unfocusedContainerColor = HomeBg.copy(alpha = 0.3f),
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = generoExpanded,
                                    onDismissRequest = { generoExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Hombre", color = TextPrimary) },
                                        onClick = {
                                            genero = "hombre"
                                            generoExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mujer", color = TextPrimary) },
                                        onClick = {
                                            genero = "mujer"
                                            generoExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Otro", color = TextPrimary) },
                                        onClick = {
                                            genero = "otro"
                                            generoExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Fecha de nacimiento
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nacimiento",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            
                            OutlinedTextField(
                                value = fechaNacimiento,
                                onValueChange = { text ->
                                    val cleanText = text.replace(Regex("[^0-9]"), "").take(8)
                                    fechaNacimiento = cleanText.let {
                                        when {
                                            it.length <= 2 -> it
                                            it.length <= 4 -> "${it.substring(0, 2)}/${it.substring(2)}"
                                            else -> "${it.substring(0, 2)}/${it.substring(2, 4)}/${it.substring(4)}"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("DD/MM/AA", color = TextMuted, fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
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
                    
                    // Error message
                    AnimatedVisibility(
                        visible = uiState.errorMessage.isNotEmpty(),
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
                                    text = uiState.errorMessage,
                                    color = AccentPink,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Botón Register Premium
                    Button(
                        onClick = {
                            viewModel.register(
                                email = email,
                                username = username,
                                password = password,
                                genero = genero,
                                fechaNacimiento = fechaNacimiento
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading && email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty(),
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
                                    if (!uiState.isLoading && email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty())
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
                            if (uiState.isLoading) {
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
                                        text = "Crear Cuenta",
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Términos y condiciones
            Text(
                text = "Al crear una cuenta, aceptas nuestros Términos de Servicio y Política de Privacidad",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Footer
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Inicia sesión",
                    color = PrimaryPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
