package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.SecurityRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PasswordRequirementsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var settings by remember { mutableStateOf<SecurityRepository.SecuritySettings?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Estados locales para los toggles
    var requireStrongPassword by remember { mutableStateOf(true) }
    var passwordMinLength by remember { mutableStateOf(8) }
    var requireUppercase by remember { mutableStateOf(true) }
    var requireNumber by remember { mutableStateOf(true) }
    var requireSpecialChar by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                settings = SecurityRepository.getSecuritySettings(userId)
                settings?.let { s ->
                    requireStrongPassword = s.require_strong_password
                    passwordMinLength = s.password_min_length
                    requireUppercase = s.require_uppercase
                    requireNumber = s.require_number
                    requireSpecialChar = s.require_special_char
                }
            }
            isLoading = false
            hasChanges = false
        }
    }
    
    // Detectar cambios
    LaunchedEffect(requireStrongPassword, passwordMinLength, requireUppercase, requireNumber, requireSpecialChar) {
        settings?.let { s ->
            hasChanges = requireStrongPassword != s.require_strong_password ||
                        passwordMinLength != s.password_min_length ||
                        requireUppercase != s.require_uppercase ||
                        requireNumber != s.require_number ||
                        requireSpecialChar != s.require_special_char
        }
    }
    
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
                    title = "Requisitos de contraseña",
                    subtitle = "Configura la complejidad requerida",
                    icon = Icons.Outlined.Password,
                    iconColor = Color(0xFF1565A0),
                    onBack = onDismiss
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Toggle principal
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { requireStrongPassword = !requireStrongPassword }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Requerir contraseña segura",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Aplicar requisitos de complejidad",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = requireStrongPassword,
                                    onCheckedChange = { requireStrongPassword = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryPurple,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Surface
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Opciones de complejidad (solo si está activado)
                        if (requireStrongPassword) {
                            Text(
                                text = "LONGITUD MÍNIMA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            )
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Surface
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Caracteres mínimos",
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "$passwordMinLength caracteres",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPurple
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Slider(
                                        value = passwordMinLength.toFloat(),
                                        onValueChange = { passwordMinLength = it.toInt() },
                                        valueRange = 6f..20f,
                                        steps = 13,
                                        colors = SliderDefaults.colors(
                                            thumbColor = PrimaryPurple,
                                            activeTrackColor = PrimaryPurple,
                                            inactiveTrackColor = PrimaryPurple.copy(alpha = 0.2f)
                                        )
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("6", fontSize = 11.sp, color = TextMuted)
                                        Text("20", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "REQUISITOS DE CARACTERES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            )
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Surface
                            ) {
                                Column {
                                    RequirementToggle(
                                        title = "Al menos una mayúscula",
                                        subtitle = "A-Z",
                                        isEnabled = requireUppercase,
                                        onToggle = { requireUppercase = it },
                                        icon = Icons.Outlined.TextFields
                                    )
                                    
                                    Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                    
                                    RequirementToggle(
                                        title = "Al menos un número",
                                        subtitle = "0-9",
                                        isEnabled = requireNumber,
                                        onToggle = { requireNumber = it },
                                        icon = Icons.Outlined.Numbers
                                    )
                                    
                                    Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                    
                                    RequirementToggle(
                                        title = "Al menos un carácter especial",
                                        subtitle = "!@#\$%^&*()",
                                        isEnabled = requireSpecialChar,
                                        onToggle = { requireSpecialChar = it },
                                        icon = Icons.Outlined.Code
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Vista previa de ejemplo
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1565A0).copy(alpha = 0.08f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFF1565A0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ejemplo de contraseña válida",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1565A0)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val examplePassword = buildString {
                                    append("M")
                                    append("i")
                                    if (requireUppercase) append("C")
                                    append("lave")
                                    if (requireNumber) append("123")
                                    if (requireSpecialChar) append("!")
                                    while (length < passwordMinLength) append("x")
                                }
                                
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = HomeBg
                                ) {
                                    Text(
                                        text = examplePassword,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Botón guardar
                        if (hasChanges) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isSaving = true
                                        val userId = SupabaseClient.auth.currentUserOrNull()?.id
                                        if (userId != null) {
                                            SecurityRepository.updateSecuritySettings(
                                                userId = userId,
                                                requireStrongPassword = requireStrongPassword,
                                                passwordMinLength = passwordMinLength,
                                                requireUppercase = requireUppercase,
                                                requireNumber = requireNumber,
                                                requireSpecialChar = requireSpecialChar
                                            )
                                            // Recargar settings
                                            settings = SecurityRepository.getSecuritySettings(userId)
                                            hasChanges = false
                                        }
                                        isSaving = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !isSaving,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                )
                            ) {
                                if (isSaving) {
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
                                        text = "Guardar cambios",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementToggle(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
        
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryPurple,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Surface
            )
        )
    }
}
