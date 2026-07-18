package com.vinzay.app.ui.screens.auth

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
import com.vinzay.app.ui.theme.*
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

    // ═══ FORM STATE ═══
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

    // React to success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
        }
    }

    // Password strength colors
    val strengthColor = when {
        uiState.passwordStrength < 0.25f -> AccentPink
        uiState.passwordStrength < 0.50f -> AccentGold
        uiState.passwordStrength < 0.75f -> AccentGreen.copy(alpha = 0.7f)
        else -> AccentGreen
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
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

            // ═══ BACK BUTTON ═══
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
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.ArrowBack, "Volver", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═══ HEADER ═══
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Crea tu cuenta",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Comienza a descubrir y compartir",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ═══ FORM CARD ═══
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Surface.copy(alpha = 0.6f),
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // ═══ EMAIL ═══
                    AuthTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailDirty = true
                            viewModel.validateEmail(it)
                        },
                        label = "Correo electrónico",
                        placeholder = "tu@email.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = emailDirty && uiState.emailError != null,
                        errorMessage = if (emailDirty) uiState.emailError else null
                    )

                    Spacer(Modifier.height(14.dp))

                    // ═══ USERNAME ═══
                    AuthTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameDirty = true
                            viewModel.validateUsername(it)
                        },
                        label = "Nombre de usuario",
                        placeholder = "@usuario",
                        leadingIcon = Icons.Outlined.AlternateEmail,
                        trailingIcon = {
                            if (uiState.isCheckingUsername) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = TextMuted,
                                    strokeWidth = 2.dp
                                )
                            } else if (uiState.isUsernameAvailable == true && usernameDirty && username.length >= 3) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = usernameDirty && uiState.usernameError != null,
                        errorMessage = if (usernameDirty) uiState.usernameError else null
                    )

                    Spacer(Modifier.height(14.dp))

                    // ═══ PASSWORD ═══
                    AuthTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordDirty = true
                            viewModel.validatePassword(it)
                            if (confirmDirty) viewModel.validateConfirmPassword(it, confirmPassword)
                        },
                        label = "Contraseña",
                        placeholder = "Mínimo 6 caracteres",
                        leadingIcon = Icons.Outlined.Lock,
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    null, tint = TextMuted, modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = passwordDirty && uiState.passwordError != null,
                        errorMessage = if (passwordDirty) uiState.passwordError else null
                    )

                    // ═══ PASSWORD STRENGTH INDICATOR ═══
                    AnimatedVisibility(
                        visible = passwordDirty && password.isNotEmpty(),
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            // Strength bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SurfaceElevated)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = uiState.passwordStrength)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(strengthColor)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = uiState.passwordStrengthLabel,
                                fontSize = 11.sp,
                                color = strengthColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // ═══ CONFIRM PASSWORD ═══
                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmDirty = true
                            viewModel.validateConfirmPassword(password, it)
                        },
                        label = "Confirmar contraseña",
                        placeholder = "Repite la contraseña",
                        leadingIcon = Icons.Outlined.Lock,
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    null, tint = TextMuted, modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = confirmDirty && uiState.confirmPasswordError != null,
                        errorMessage = if (confirmDirty) uiState.confirmPasswordError else null
                    )

                    Spacer(Modifier.height(14.dp))

                    // ═══ GENDER + DATE ═══
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Gender dropdown
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Género",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.generoError != null) AccentPink else TextSecondary,
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
                                        focusedBorderColor = if (uiState.generoError != null) AccentPink else PrimaryPurple,
                                        unfocusedBorderColor = if (uiState.generoError != null) AccentPink.copy(alpha = 0.5f) else BorderSubtle,
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
                                        onClick = { genero = "hombre"; generoExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mujer", color = TextPrimary) },
                                        onClick = { genero = "mujer"; generoExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Otro", color = TextPrimary) },
                                        onClick = { genero = "otro"; generoExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Prefiero no decirlo", color = TextSecondary) },
                                        onClick = { genero = "otro"; generoExpanded = false }
                                    )
                                }
                            }
                            if (uiState.generoError != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(uiState.generoError!!, color = AccentPink, fontSize = 11.sp)
                            }
                        }

                        // Date of birth
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nacimiento",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.fechaError != null) AccentPink else TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                            OutlinedTextField(
                                value = fechaNacimiento,
                                onValueChange = { text ->
                                    val clean = text.replace(Regex("[^0-9]"), "").take(8)
                                    fechaNacimiento = clean.let {
                                        when {
                                            it.length <= 2 -> it
                                            it.length <= 4 -> "${it.substring(0, 2)}/${it.substring(2)}"
                                            else -> "${it.substring(0, 2)}/${it.substring(2, 4)}/${it.substring(4)}"
                                        }
                                    }
                                    dateDirty = true
                                    viewModel.validateDate(fechaNacimiento)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("DD/MM/AA", color = TextMuted, fontSize = 14.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                singleLine = true,
                                isError = dateDirty && uiState.fechaError != null,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (uiState.fechaError != null) AccentPink else PrimaryPurple,
                                    unfocusedBorderColor = if (uiState.fechaError != null) AccentPink.copy(alpha = 0.5f) else BorderSubtle,
                                    focusedContainerColor = HomeBg.copy(alpha = 0.5f),
                                    unfocusedContainerColor = HomeBg.copy(alpha = 0.3f),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = if (uiState.fechaError != null) AccentPink else PrimaryPurple
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }, modifier = Modifier.size(40.dp)) {
                                        Icon(
                                            Icons.Outlined.DateRange, null,
                                            tint = if (uiState.fechaError != null) AccentPink else TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                supportingText = if (dateDirty && uiState.fechaError != null) {
                                    { Text(uiState.fechaError!!, color = AccentPink, fontSize = 11.sp) }
                                } else null
                            )
                        }
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

                    Spacer(Modifier.height(16.dp))

                    // ═══ TERMS CHECKBOX ═══
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { acceptedTerms = !acceptedTerms }
                    ) {
                        Checkbox(
                            checked = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryPurple,
                                uncheckedColor = TextMuted.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Acepto los Términos y Política de Privacidad",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ═══ REGISTER BUTTON ═══
                    GradientButton(
                        text = "Crear Cuenta",
                        icon = Icons.Filled.ArrowForward,
                        isLoading = uiState.isLoading,
                        enabled = email.isNotEmpty() && username.length >= 3 && password.length >= 6 &&
                                 confirmPassword == password && acceptedTerms && !uiState.isLoading,
                        gradientColors = listOf(Color(0xFFFF6B35), Color(0xFF1565A0)),
                        onClick = {
                            emailDirty = true
                            usernameDirty = true
                            passwordDirty = true
                            confirmDirty = true
                            dateDirty = true
                            focusManager.clearFocus()
                            viewModel.register(
                                email = email,
                                username = username,
                                password = password,
                                confirmPassword = confirmPassword,
                                genero = genero,
                                fechaNacimiento = fechaNacimiento
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ TERMS TEXT ═══
            Text(
                text = "Al crear una cuenta, aceptas nuestros Términos de Servicio y Política de Privacidad",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ═══ FOOTER ═══
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes cuenta?", color = TextMuted, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Inicia sesión",
                    color = PrimaryPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        // ═══ DATE PICKER DIALOG ═══
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = run {
                    try {
                        val parts = fechaNacimiento.split("/")
                        if (parts.size == 3) {
                            val d = parts[0].toIntOrNull()
                            val m = parts[1].toIntOrNull()
                            val y = parts[2].toIntOrNull()
                            if (d != null && m != null && y != null) {
                                Date.from(LocalDate.of(y, m, d).atStartOfDay(ZoneId.systemDefault()).toInstant()).time
                            } else null
                        } else null
                    } catch (_: Exception) { null }
                } ?: System.currentTimeMillis() - 365L * 20 * 24 * 60 * 60 * 1000 // Default: 20 years ago
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            fechaNacimiento = String.format("%02d/%02d/%04d", date.dayOfMonth, date.monthValue, date.year)
                            dateDirty = true
                            viewModel.validateDate(fechaNacimiento)
                        }
                        showDatePicker = false
                    }) {
                        Text("Aceptar", color = PrimaryPurple)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar", color = TextMuted)
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = PrimaryPurple,
                    selectedYearContentColor = Color.White,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = PrimaryPurple,
                    todayContentColor = PrimaryPurple,
                    todayDateBorderColor = PrimaryPurple,
                    dayContentColor = TextPrimary,
                    dayInSelectionRangeContentColor = Color.White,
                    dayInSelectionRangeContainerColor = PrimaryPurple.copy(alpha = 0.3f)
                )
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // ═══ SUCCESS OVERLAY ═══
        AnimatedVisibility(
            visible = uiState.isSuccess,
            enter = fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HomeBg.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle, null,
                        tint = AccentGreen,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "¡Cuenta creada!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Bienvenido a Vinzay",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "@${username.lowercase().trim()}",
                        fontSize = 14.sp,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
