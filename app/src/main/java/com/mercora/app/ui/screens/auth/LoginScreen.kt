package com.mercora.app.ui.screens.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.mercora.app.data.biometric.BiometricEnrollmentManager
import com.mercora.app.data.remote.SessionPersistence
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    dismissSplash: () -> Unit = {}
) {
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // COLD START: Check session immediately â€” instant redirect if logged in
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // Check for saved session
    val context = LocalContext.current
    var checkingSession by remember { mutableStateOf(true) }
    var hasSession by remember { mutableStateOf(false) }
    var sessionUserId by remember { mutableStateOf("") }
    var biometricEnrolledInApp by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val loggedIn = com.mercora.app.data.remote.SessionPersistence.isLoggedIn()
        val userId = com.mercora.app.data.remote.SessionPersistence.getUserId() ?: ""
        hasSession = loggedIn
        sessionUserId = userId
        if (loggedIn && userId.isNotEmpty()) {
            biometricEnrolledInApp = BiometricEnrollmentManager.isEnrolled(context, userId)
        }
        checkingSession = false
    }
    if (checkingSession) return

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // STATE
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    val focusManager = LocalFocusManager.current
    val prefs = remember { context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE) }
    var emailOrUsername by remember { mutableStateOf(prefs.getString("last_username", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(prefs.getBoolean("remember_me", false)) }
    var showForgotPasswordSheet by remember { mutableStateOf(false) }

    // Validation state
    var emailDirty by remember { mutableStateOf(emailOrUsername.isNotEmpty()) }
    var passwordDirty by remember { mutableStateOf(false) }

    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Inline biometric enrollment (tapped from fingerprint button)
    var showInlineEnrollment by remember { mutableStateOf(false) }
    
    // Auto-trigger biometric prompt if enrolled
    val activity = context as? FragmentActivity
    LaunchedEffect(hasSession, biometricEnrolledInApp) {
        if (hasSession && biometricEnrolledInApp && activity != null && !uiState.isAuthenticated) {
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                val prompt = BiometricPrompt(
                    activity, androidx.core.content.ContextCompat.getMainExecutor(activity),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            onNavigateToHome()
                        }
                    }
                )
                prompt.authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Inicio de sesión biométrico")
                        .setSubtitle("Usa tu huella para acceder")
                        .setAllowedAuthenticators(
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                        .build()
                )
            }
        }
    }
    
    // React to auth success — navegar a Home directamente, sin auto-enrollment
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            dismissSplash()
            prefs.edit()
                .putBoolean("remember_me", rememberMe)
                .putString("last_username", if (rememberMe) emailOrUsername else "")
                .apply()
            onNavigateToHome()
        }
    }

    // Show inline enrollment when user taps "iniciar con huella" without being enrolled
    if (showInlineEnrollment) {
        val inlineUserId = SessionPersistence.getUserId() ?: ""
        if (inlineUserId.isNotEmpty()) {
            BiometricEnrollmentScreen(
                userId = inlineUserId,
                onComplete = {
                    showInlineEnrollment = false
                },
                onSkip = {
                    showInlineEnrollment = false
                }
            )
            return
        } else {
            showInlineEnrollment = false
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // LAYOUT
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        AuthBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Mercora",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryPurple,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Descubre, conecta y compra",
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Surface.copy(alpha = 0.85f),
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // â•â•â• SUCCESS MESSAGE â•â•â•
                    AnimatedVisibility(
                        visible = uiState.successMessage.isNotEmpty(),
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = AccentGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(uiState.successMessage, color = AccentGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // â•â•â• EMAIL/USERNAME FIELD â•â•â•
                    AuthTextField(
                        value = emailOrUsername,
                        onValueChange = {
                            emailOrUsername = it
                            emailDirty = true
                            viewModel.validateEmail(it)
                        },
                        label = "Correo o usuario",
                        placeholder = "tu@email.com",
                        leadingIcon = Icons.Outlined.Person,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = emailDirty && uiState.emailError != null,
                        errorMessage = if (emailDirty) uiState.emailError else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // â•â•â• PASSWORD FIELD â•â•â•
                    AuthTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordDirty = true
                            viewModel.validatePassword(it)
                        },
                        label = "Contraseña",
                        placeholder = "········",
                        leadingIcon = Icons.Outlined.Lock,
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(40.dp)) {
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
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        isError = passwordDirty && uiState.passwordError != null,
                        errorMessage = if (passwordDirty) uiState.passwordError else null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // â•â•â• REMEMBER ME + FORGOT PASSWORD â•â•â•
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryPurple,
                                    uncheckedColor = TextMuted.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Recordarme", color = TextSecondary, fontSize = 13.sp)
                        }

                        Text(
                            text = "¿Olvidaste?",
                            color = PrimaryPurple,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showForgotPasswordSheet = true }
                        )
                    }

                    // â•â•â• ERROR MESSAGE â•â•â•
                    AnimatedVisibility(
                        visible = uiState.errorMessage.isNotEmpty(),
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = AccentPink.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = AccentPink, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(uiState.errorMessage, color = AccentPink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // â•â•â• LOGIN BUTTON â•â•â•
                    GradientButton(
                        text = "Iniciar Sesión",
                        icon = Icons.Filled.ArrowForward,
                        isLoading = uiState.isLoading,
                        enabled = emailOrUsername.isNotEmpty() && password.isNotEmpty() && !uiState.isLoading,
                        gradientColors = listOf(Color(0xFFFF6B35), Color(0xFF1565A0)),
                        onClick = {
                            emailDirty = true
                            passwordDirty = true
                            focusManager.clearFocus()
                            viewModel.login(emailOrUsername, password, rememberMe)
                        }
                    )

                    // BIOMETRIC BUTTON — siempre visible
                    Spacer(modifier = Modifier.height(12.dp))
                    BiometricLoginButton(
                        context = context,
                        hasSession = hasSession && BiometricEnrollmentManager.isEnrolledForCurrentSession(context),
                        hasSavedCredentials = BiometricEnrollmentManager.hasSavedCredentials(context),
                        onEnrollRequest = { showInlineEnrollment = true },
                        onSuccess = {
                            if (hasSession) {
                                onNavigateToHome()
                            } else {
                                val savedEmail = BiometricEnrollmentManager.getSavedEmail(context)
                                val savedPass = BiometricEnrollmentManager.getSavedPassword(context)
                                if (savedEmail != null && savedPass != null) {
                                    emailOrUsername = savedEmail
                                    password = savedPass
                                    viewModel.login(savedEmail, savedPass, rememberMe)
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• FOOTER â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Guest mode button
                Surface(
                    onClick = { viewModel.loginAsGuest() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
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
                        Spacer(Modifier.width(8.dp))
                        Text("Explorar sin cuenta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Nuevo en Mercora?", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Crear cuenta",
                        color = PrimaryPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â• FORGOT PASSWORD BOTTOM SHEET â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        if (showForgotPasswordSheet) {
            ForgotPasswordSheet(
                onDismiss = { showForgotPasswordSheet = false }
            )
        }
    }
}

// ========================================================================
// FORGOT PASSWORD BOTTOM SHEET
// ========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordSheet(
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Recuperar contraseña",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Te enviaremos un enlace para restablecer tu contraseña",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = !sent,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column {
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        placeholder = "tu@email.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (email.isNotEmpty() && email.contains("@")) {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            SupabaseClient.auth.resetPasswordForEmail(email.trim())
                                            sent = true
                                        } catch (_: Exception) { }
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    GradientButton(
                        text = "Enviar enlace",
                        icon = Icons.Filled.Send,
                        isLoading = isLoading,
                        enabled = email.isNotEmpty() && email.contains("@") && !isLoading,
                        gradientColors = listOf(PrimaryPurple, PrimaryBright),
                        onClick = {
                            isLoading = true
                            scope.launch {
                                try {
                                    SupabaseClient.auth.resetPasswordForEmail(email.trim())
                                    sent = true
                                } catch (_: Exception) { }
                                isLoading = false
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = sent,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(8.dp))
                    Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("¡Enlace enviado!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Revisa tu correo electrónico", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Cerrar",
                        color = PrimaryPurple,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }
            }
        }
    }
}

// ========================================================================
// BIOMETRIC LOGIN BUTTON
// ========================================================================
@Composable
private fun BiometricLoginButton(
    context: Context,
    hasSession: Boolean = false,
    hasSavedCredentials: Boolean = false,
    onEnrollRequest: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val activity = context as? FragmentActivity
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    val hasBiometricHardware = canAuthenticate != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    val isBiometricEnrolled = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS

    val buttonText = when {
        !hasBiometricHardware -> "Huella no disponible"
        isBiometricEnrolled -> "Iniciar con huella"
        else -> "Configurar huella"
    }
    val buttonEnabled = hasBiometricHardware

    Surface(
        onClick = {
            if (!buttonEnabled) {
                android.widget.Toast.makeText(
                    context, "Este dispositivo no tiene sensor de huella",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@Surface
            }

            if (activity == null) {
                android.widget.Toast.makeText(
                    context, "Error al iniciar sensor de huella",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@Surface
            }

            if (!isBiometricEnrolled) {
                if (hasSession || hasSavedCredentials) {
                    onEnrollRequest()
                } else {
                    android.widget.Toast.makeText(
                        context, "Inicia sesión primero para configurar la huella",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return@Surface
            }

            if (!hasSession && !hasSavedCredentials) {
                android.widget.Toast.makeText(
                    context, "Inicia sesión primero para guardar tus datos biométricos",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@Surface
            }

            // Hay huella configurada -> lanzar auth
            val prompt = BiometricPrompt(
                activity!!,
                androidx.core.content.ContextCompat.getMainExecutor(activity!!),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }
                }
            )
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Inicio de sesión biométrico")
                    .setSubtitle("Usa tu huella para acceder")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                    .build()
            )
        },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (buttonEnabled) SurfaceElevated else SurfaceElevated.copy(alpha = 0.4f),
        border = if (buttonEnabled)
            androidx.compose.foundation.BorderStroke(1.dp, BorderDefault)
        else
            androidx.compose.foundation.BorderStroke(1.dp, BorderDefault.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Fingerprint, null,
                tint = if (buttonEnabled) PrimaryPurple else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buttonText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (buttonEnabled) TextSecondary else TextMuted
            )
        }
    }
}

// ========================================================================
// REUSABLE COMPONENTS
// ========================================================================

/**
 * Premium text field for auth screens with label, icon, error state, and transitions.
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) AccentPink else TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 15.sp) },
            leadingIcon = {
                Icon(leadingIcon, null, tint = if (isError) AccentPink else TextMuted, modifier = Modifier.size(20.dp))
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) AccentPink else PrimaryPurple,
                unfocusedBorderColor = if (isError) AccentPink.copy(alpha = 0.5f) else BorderSubtle,
                focusedContainerColor = HomeBg.copy(alpha = 0.5f),
                unfocusedContainerColor = HomeBg.copy(alpha = 0.3f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = if (isError) AccentPink else PrimaryPurple
            ),
            supportingText = if (errorMessage != null) {
                { Text(errorMessage, color = AccentPink, fontSize = 11.sp) }
            } else null
        )
    }
}

/**
 * Full-width gradient button with loading spinner.
 */
@Composable
fun GradientButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(PrimaryPurple, PrimaryBright),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = enabled && !isLoading,
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
                    if (enabled && !isLoading)
                        Brush.horizontalGradient(gradientColors)
                    else
                        Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceElevated))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    if (icon != null) {
                        Spacer(Modifier.width(8.dp))
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

/**
 * Animated background design with glowing orbs.
 */
@Composable
fun AuthBackground(gradientOffset: Float = 0f) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Blob 1: Subtle purple glow
        val x1 = w * 0.3f + (w * 0.2f) * kotlin.math.cos(phase1)
        val y1 = h * 0.25f + (h * 0.15f) * kotlin.math.sin(phase1 * 0.7f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PrimaryPurple.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(x1, y1),
                radius = w * 0.6f
            ),
            center = Offset(x1, y1),
            radius = w * 0.6f
        )

        // Blob 2: Subtle blue glow
        val x2 = w * 0.7f + (w * 0.18f) * kotlin.math.cos(phase2 + 3f)
        val y2 = h * 0.75f + (h * 0.12f) * kotlin.math.sin(phase2 * 0.6f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.07f), Color.Transparent),
                center = Offset(x2, y2),
                radius = w * 0.6f
            ),
            center = Offset(x2, y2),
            radius = w * 0.6f
        )
    }
}
