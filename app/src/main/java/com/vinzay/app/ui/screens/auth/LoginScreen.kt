package com.vinzay.app.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import com.vinzay.app.data.biometric.BiometricEnrollmentManager
import com.vinzay.app.data.remote.SessionPersistence
import com.vinzay.app.data.remote.SupabaseClient
import com.vinzay.app.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {}
) {
    // ═══════════════════════════════════════════════════════════════════
    // COLD START: Check session immediately — instant redirect if logged in
    // ═══════════════════════════════════════════════════════════════════
    // Check for saved session
    val context = LocalContext.current
    var checkingSession by remember { mutableStateOf(true) }
    var hasSession by remember { mutableStateOf(false) }
    var sessionUserId by remember { mutableStateOf("") }
    var biometricEnrolledInApp by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val loggedIn = com.vinzay.app.data.remote.SessionPersistence.isLoggedIn()
        val userId = com.vinzay.app.data.remote.SessionPersistence.getUserId() ?: ""
        hasSession = loggedIn
        sessionUserId = userId
        if (loggedIn && userId.isNotEmpty()) {
            biometricEnrolledInApp = BiometricEnrollmentManager.isEnrolled(context, userId)
        }
        checkingSession = false
    }
    if (checkingSession) return

    // ═══════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════
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

    // Post-login biometric enrollment state
    var showPostLoginEnrollment by remember { mutableStateOf(false) }
    var postLoginUserId by remember { mutableStateOf("") }
    
    // Auto-trigger biometric prompt if enrolled
    val activity = context as? androidx.fragment.app.FragmentActivity
    LaunchedEffect(hasSession, biometricEnrolledInApp) {
        if (hasSession && biometricEnrolledInApp && activity != null && !uiState.isAuthenticated) {
            val biometricManager = BiometricManager.from(context)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                val prompt = BiometricPrompt(
                    activity, androidx.core.content.ContextCompat.getMainExecutor(activity),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            val uname = com.vinzay.app.data.remote.SessionPersistence.getUsername()
                            if (!uname.isNullOrEmpty()) com.vinzay.app.data.model.WelcomeState.trigger(uname)
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
    
    // React to auth success
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            val loggedUsername = com.vinzay.app.data.remote.SessionPersistence.getUsername()
            if (!loggedUsername.isNullOrEmpty()) {
                com.vinzay.app.data.model.WelcomeState.trigger(loggedUsername)
            }
            
            prefs.edit()
                .putBoolean("remember_me", rememberMe)
                .putString("last_username", if (rememberMe) emailOrUsername else "")
                .apply()
            val uid = SessionPersistence.getUserId()
            if (uiState.isBiometricAvailable && uid != null && !BiometricEnrollmentManager.isEnrolled(context, uid)) {
                postLoginUserId = uid
                showPostLoginEnrollment = true
            } else {
                onNavigateToHome()
            }
        }
    }

    // Show biometric enrollment after first login
    if (showPostLoginEnrollment && postLoginUserId.isNotEmpty()) {
        BiometricEnrollmentScreen(
            userId = postLoginUserId,
            onComplete = {
                showPostLoginEnrollment = false
                onNavigateToHome()
            },
            onSkip = {
                showPostLoginEnrollment = false
                onNavigateToHome()
            }
        )
        return
    }

    // ═══════════════════════════════════════════════════════════════════
    // LAYOUT
    // ═══════════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
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
                text = "Rendly",
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
                color = Surface.copy(alpha = 0.6f),
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // ═══ SUCCESS MESSAGE ═══
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

                    // ═══ EMAIL/USERNAME FIELD ═══
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

                    // ═══ PASSWORD FIELD ═══
                    AuthTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordDirty = true
                            viewModel.validatePassword(it)
                        },
                        label = "Contraseña",
                        placeholder = "••••••••",
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

                    // ═══ REMEMBER ME + FORGOT PASSWORD ═══
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

                    // ═══ ERROR MESSAGE ═══
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

                    // ═══ LOGIN BUTTON ═══
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

                    // ═══ BIOMETRIC BUTTON ═══
                    if (uiState.isBiometricAvailable) {
                        Spacer(modifier = Modifier.height(12.dp))
                        BiometricLoginButton(
                            context = context,
                            hasSession = hasSession && BiometricEnrollmentManager.isEnrolledForCurrentSession(context),
                            hasSavedCredentials = BiometricEnrollmentManager.hasSavedCredentials(context),
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ════════════════════ FOOTER ════════════════════
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
                    Text("¿Nuevo en Vinzay?", color = TextMuted, fontSize = 14.sp)
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

        // ════════════════════ FORGOT PASSWORD BOTTOM SHEET ════════════════════
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
// BIOMETRIC QUICK AUTH — shown when a session is already saved
// ========================================================================
@Composable
private fun BiometricQuickAuth(
    context: Context,
    onSuccess: () -> Unit,
    onUsePassword: () -> Unit
) {
    val activity = context as? FragmentActivity

    fun launchBiometric(onSuccess: () -> Unit) {
        activity?.let { act ->
            val prompt = BiometricPrompt(
                act, androidx.core.content.ContextCompat.getMainExecutor(act),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                }
            )
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Inicio de sesión biométrico")
                    .setSubtitle("Verifica tu identidad para acceder")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                    .build()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Rendly",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryPurple,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Bienvenido de vuelta",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(32.dp))

            Icon(
                Icons.Outlined.Fingerprint,
                contentDescription = "Huella digital",
                tint = PrimaryPurple,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Verifica tu identidad",
                fontSize = 16.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Usa tu huella digital para iniciar sesión",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                onClick = { launchBiometric(onSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryPurple
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.Fingerprint, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Verificar huella", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Usar contraseña",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    onUsePassword()
                }
            )
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
    onSuccess: () -> Unit = {}
) {
    val activity = context as? FragmentActivity ?: return
    val biometricManager = BiometricManager.from(context)
    val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    val hasBiometricHardware = canAuthenticate != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    val isBiometricEnrolled = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS

    if (!hasBiometricHardware) return

    Surface(
        onClick = {
            if (!hasSession && !hasSavedCredentials) {
                android.widget.Toast.makeText(
                    context, "Inicia sesión primero para guardar tus datos biométricos",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                return@Surface
            }

            if (!isBiometricEnrolled) {
                // No hay huella configurada → abrir settings para configurar
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                } else {
                    Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
                return@Surface
            }

            // Hay huella configurada → lanzar auth
            val prompt = BiometricPrompt(
                activity,
                androidx.core.content.ContextCompat.getMainExecutor(activity),
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
        color = SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDefault)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Fingerprint, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isBiometricEnrolled) "Iniciar con huella" else "Configurar huella",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
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
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PrimaryPurple.copy(alpha = 0.35f), PrimaryPurple.copy(alpha = 0.15f), PrimaryPurple.copy(alpha = 0.05f), Color.Transparent),
                center = Offset(w * 0.85f, h * 0.08f), radius = w * 0.6f
            ),
            center = Offset(w * 0.85f, h * 0.08f), radius = w * 0.6f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AccentPink.copy(alpha = 0.3f), AccentPink.copy(alpha = 0.12f), AccentPink.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(w * 0.15f, h * 0.92f), radius = w * 0.55f
            ),
            center = Offset(w * 0.15f, h * 0.92f), radius = w * 0.55f
        )
    }
}
