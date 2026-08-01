package com.mercora.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Form state
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var generoExpanded by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Dirty flags for validation
    var emailDirty by remember { mutableStateOf(false) }
    var usernameDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }
    var confirmDirty by remember { mutableStateOf(false) }
    var dateDirty by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AuthBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = SurfaceElevated.copy(alpha = 0.5f)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.ArrowBack, "Volver", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Crea tu cuenta",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Form Content
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Surface.copy(alpha = 0.8f),
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it; emailDirty = true; viewModel.validateEmail(it) },
                        label = "Correo electrónico",
                        placeholder = "tu@email.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = emailDirty && uiState.emailError != null,
                        errorMessage = if (emailDirty) uiState.emailError else null
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = username,
                        onValueChange = { username = it; usernameDirty = true; viewModel.validateUsername(it) },
                        label = "Nombre de usuario",
                        placeholder = "@usuario",
                        leadingIcon = Icons.Outlined.AlternateEmail,
                        trailingIcon = {
                             if (uiState.isCheckingUsername) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                             else if (uiState.isUsernameAvailable == true && username.length > 2) Icon(Icons.Outlined.CheckCircle, null, tint = AccentGreen)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = usernameDirty && uiState.usernameError != null,
                        errorMessage = if (usernameDirty) uiState.usernameError else null
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; passwordDirty = true; viewModel.validatePassword(it); if(confirmDirty) viewModel.validateConfirmPassword(it, confirmPassword) },
                        label = "Contraseña",
                        placeholder = "········",
                        leadingIcon = Icons.Outlined.Lock,
                        trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = TextMuted) } },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = passwordDirty && uiState.passwordError != null,
                        errorMessage = if (passwordDirty) uiState.passwordError else null
                    )
                     Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmDirty = true; viewModel.validateConfirmPassword(password, it) },
                        label = "Confirmar Contraseña",
                        placeholder = "········",
                        leadingIcon = Icons.Outlined.Lock,
                        trailingIcon = { IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) { Icon(if (showConfirmPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = TextMuted) } },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        isError = confirmDirty && uiState.confirmPasswordError != null,
                        errorMessage = if (confirmDirty) uiState.confirmPasswordError else null
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { acceptedTerms = !acceptedTerms }
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                             colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple, uncheckedColor = TextMuted)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Acepto los Términos y Política de Privacidad", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 AnimatedVisibility(visible = uiState.errorMessage.isNotEmpty()) {
                    Text(uiState.errorMessage, color = AccentPink, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                }
                GradientButton(
                    text = "Crear Cuenta",
                    icon = Icons.Filled.ArrowForward,
                    isLoading = uiState.isLoading,
                    enabled = acceptedTerms && !uiState.isLoading,
                    onClick = {
                        emailDirty = true
                        usernameDirty = true
                        passwordDirty = true
                        confirmDirty = true
                        focusManager.clearFocus()
                        viewModel.register(email, username, password, confirmPassword, genero, fechaNacimiento)
                    }
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                     modifier = Modifier.clickable { onNavigateToLogin() }
                ) {
                    Text("¿Ya tienes cuenta?", color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("Inicia sesión", color = PrimaryPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // Overlays
        if (showDatePicker) {
            // Date picker implementation...
        }
        AnimatedVisibility(visible = uiState.isSuccess, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize().background(HomeBg.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
                // Success content...
            }
        }
    }
}
