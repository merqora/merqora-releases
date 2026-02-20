package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.SecurityRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var securitySettings by remember { mutableStateOf<SecurityRepository.SecuritySettings?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Cargar configuración de seguridad
    LaunchedEffect(isVisible) {
        if (isVisible) {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                securitySettings = SecurityRepository.getSecuritySettings(userId)
            }
        }
    }
    
    // Validación en tiempo real
    val passwordValidation = remember(newPassword, securitySettings) {
        securitySettings?.let { settings ->
            SecurityRepository.validatePassword(newPassword, settings)
        } ?: Pair(true, emptyList())
    }
    
    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
    val canSubmit = currentPassword.isNotEmpty() && 
                   passwordValidation.first && 
                   passwordsMatch && 
                   !isLoading
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp)
                .clickable(enabled = false) { },
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Cambiar contraseña",
                    subtitle = "Actualiza tu contraseña de acceso",
                    icon = Icons.Outlined.Key,
                    iconColor = PrimaryPurple,
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mensajes de error/éxito
                    errorMessage?.let { error ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFEF4444),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    successMessage?.let { success ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2E8B57).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = success,
                                    color = Color(0xFF2E8B57),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Contraseña actual
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Contraseña actual",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { 
                                    currentPassword = it
                                    errorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Ingresa tu contraseña actual") },
                                visualTransformation = if (showCurrentPassword) 
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                        Icon(
                                            imageVector = if (showCurrentPassword) 
                                                Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nueva contraseña
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Nueva contraseña",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { 
                                    newPassword = it
                                    errorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Crea una nueva contraseña") },
                                visualTransformation = if (showNewPassword) 
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                        Icon(
                                            imageVector = if (showNewPassword) 
                                                Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            // Requisitos de contraseña
                            if (newPassword.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                securitySettings?.let { settings ->
                                    PasswordRequirement(
                                        text = "Mínimo ${settings.password_min_length} caracteres",
                                        isMet = newPassword.length >= settings.password_min_length
                                    )
                                    if (settings.require_uppercase) {
                                        PasswordRequirement(
                                            text = "Al menos una mayúscula",
                                            isMet = newPassword.any { it.isUpperCase() }
                                        )
                                    }
                                    if (settings.require_number) {
                                        PasswordRequirement(
                                            text = "Al menos un número",
                                            isMet = newPassword.any { it.isDigit() }
                                        )
                                    }
                                    if (settings.require_special_char) {
                                        PasswordRequirement(
                                            text = "Al menos un carácter especial",
                                            isMet = newPassword.any { !it.isLetterOrDigit() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Confirmar contraseña
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Confirmar contraseña",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { 
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Repite la nueva contraseña") },
                                visualTransformation = if (showConfirmPassword) 
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            imageVector = if (showConfirmPassword) 
                                                Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = TextMuted
                                        )
                                    }
                                },
                                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (passwordsMatch && confirmPassword.isNotEmpty()) 
                                        Color(0xFF2E8B57) else PrimaryPurple,
                                    unfocusedBorderColor = BorderSubtle,
                                    errorBorderColor = Color(0xFFEF4444),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            if (confirmPassword.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (passwordsMatch) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = if (passwordsMatch) Color(0xFF2E8B57) else Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (passwordsMatch) "Las contraseñas coinciden" else "Las contraseñas no coinciden",
                                        fontSize = 12.sp,
                                        color = if (passwordsMatch) Color(0xFF2E8B57) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Botón de guardar
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                
                                try {
                                    // Verificar contraseña actual (re-autenticar)
                                    val email = SupabaseClient.auth.currentUserOrNull()?.email
                                    if (email != null) {
                                        // Intentar cambiar la contraseña
                                        val success = SecurityRepository.changePassword(newPassword)
                                        
                                        if (success) {
                                            successMessage = "¡Contraseña actualizada correctamente!"
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                        } else {
                                            errorMessage = "No se pudo cambiar la contraseña. Intenta de nuevo."
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = canSubmit,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple,
                            disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cambiar contraseña",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirement(
    text: String,
    isMet: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isMet) Color(0xFF2E8B57) else TextMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) Color(0xFF2E8B57) else TextMuted
        )
    }
}
