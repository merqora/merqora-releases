package com.rendly.app.ui.components.settings

import android.content.Context
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.SecurityRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SecuritySettingsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onLogout: () -> Unit = {}
) {
    var settings by remember { mutableStateOf<SecurityRepository.SecuritySettings?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var trustedDevicesCount by remember { mutableStateOf(0) }
    var activeSessionsCount by remember { mutableStateOf(0) }
    var suspiciousCount by remember { mutableStateOf(0) }
    
    // Estados para sub-pantallas
    var showChangePassword by remember { mutableStateOf(false) }
    var showPasswordRequirements by remember { mutableStateOf(false) }
    var showTrustedDevices by remember { mutableStateOf(false) }
    var showActiveSessions by remember { mutableStateOf(false) }
    var showActivityHistory by remember { mutableStateOf(false) }
    var showSuspiciousActivity by remember { mutableStateOf(false) }
    var showEndAllSessionsDialog by remember { mutableStateOf(false) }
    var showTwoFactorSetup by remember { mutableStateOf(false) }
    var showDeactivateAccountDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }
    var isProcessingAccount by remember { mutableStateOf(false) }
    var shouldLogout by remember { mutableStateOf(false) }
    
    // Ejecutar logout fuera de coroutines para evitar problemas de contexto
    LaunchedEffect(shouldLogout) {
        if (shouldLogout) {
            onLogout()
        }
    }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Cargar datos
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                // Crear configuración si no existe
                val existingSettings = SecurityRepository.getSecuritySettings(userId)
                if (existingSettings == null) {
                    SecurityRepository.createSecuritySettings(userId)
                }
                settings = SecurityRepository.getSecuritySettings(userId)
                
                // Contar dispositivos y sesiones
                trustedDevicesCount = SecurityRepository.getTrustedDevices(userId).size
                activeSessionsCount = SecurityRepository.getActiveSessions(userId).size
                suspiciousCount = SecurityRepository.getSuspiciousActivities(userId, onlyUnresolved = true).size
            }
            isLoading = false
        }
    }
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    // Dialog para cerrar todas las sesiones
    if (showEndAllSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showEndAllSessionsDialog = false },
            title = { 
                Text(
                    "Cerrar todas las sesiones",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ) 
            },
            text = { 
                Text(
                    "Se cerrarán todas las sesiones en todos los dispositivos. Tendrás que volver a iniciar sesión.",
                    color = TextSecondary
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                            if (userId != null) {
                                val closed = SecurityRepository.endAllSessions(userId)
                                activeSessionsCount = 0
                                Toast.makeText(context, "$closed sesiones cerradas. Cerrando sesión...", Toast.LENGTH_SHORT).show()
                                // PRIMERO limpiar sesión persistida
                                com.rendly.app.data.remote.SessionPersistence.clearSession()
                                // Luego cerrar sesión en Supabase
                                kotlinx.coroutines.delay(500)
                                SupabaseClient.auth.signOut()
                                shouldLogout = true
                            }
                            showEndAllSessionsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Cerrar todas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndAllSessionsDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = Surface
        )
    }
    
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
                    title = "Seguridad",
                    subtitle = "Protege tu cuenta",
                    icon = Icons.Outlined.Security,
                    iconColor = Color(0xFF2E8B57),
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
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Contraseña
                        SettingsSectionTitle("Contraseña")
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column {
                                SecurityItem(
                                    icon = Icons.Outlined.Key,
                                    title = "Cambiar contraseña",
                                    subtitle = formatLastPasswordChange(settings?.last_password_change),
                                    onClick = { showChangePassword = true },
                                    iconColor = PrimaryPurple
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Autenticación
                        SettingsSectionTitle("Autenticación")
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column {
                                SecurityToggle(
                                    icon = Icons.Outlined.PhonelinkLock,
                                    title = "Autenticación de dos factores",
                                    subtitle = if (settings?.two_factor_enabled == true) "Activada - TOTP" else "Añade una capa extra de seguridad",
                                    isEnabled = settings?.two_factor_enabled ?: false,
                                    onToggle = { enabled ->
                                        scope.launch {
                                            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                                            if (userId != null) {
                                                if (enabled) {
                                                    showTwoFactorSetup = true
                                                } else {
                                                    val success = SecurityRepository.disable2FA(userId)
                                                    if (success) {
                                                        settings = SecurityRepository.getSecuritySettings(userId)
                                                        Toast.makeText(context, "2FA desactivado", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    iconColor = Color(0xFF2E8B57),
                                )
                                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                SecurityToggle(
                                    icon = Icons.Outlined.NotificationsActive,
                                    title = "Alertas de inicio de sesión",
                                    subtitle = "Notificar cuando alguien inicie sesión",
                                    isEnabled = settings?.login_alerts_enabled ?: true,
                                    onToggle = { enabled ->
                                        scope.launch {
                                            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                                            if (userId != null) {
                                                SecurityRepository.updateSecuritySettings(userId, loginAlertsEnabled = enabled)
                                                settings = SecurityRepository.getSecuritySettings(userId)
                                            }
                                        }
                                    },
                                    iconColor = Color(0xFFFF6B35)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Dispositivos y sesiones
                        SettingsSectionTitle("Dispositivos y sesiones")
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column {
                                SecurityItem(
                                    icon = Icons.Outlined.Devices,
                                    title = "Dispositivos de confianza",
                                    subtitle = "$trustedDevicesCount dispositivo${if (trustedDevicesCount != 1) "s" else ""} registrado${if (trustedDevicesCount != 1) "s" else ""}",
                                    onClick = { showTrustedDevices = true },
                                    iconColor = Color(0xFF1565A0),
                                    showArrow = true
                                )
                                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                SecurityItem(
                                    icon = Icons.Outlined.Laptop,
                                    title = "Sesiones activas",
                                    subtitle = "$activeSessionsCount sesión${if (activeSessionsCount != 1) "es" else ""} activa${if (activeSessionsCount != 1) "s" else ""}",
                                    onClick = { showActiveSessions = true },
                                    iconColor = Color(0xFF2E8B57),
                                    showArrow = true
                                )
                                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                SecurityItem(
                                    icon = Icons.Outlined.ExitToApp,
                                    title = "Cerrar todas las sesiones",
                                    subtitle = "Cierra sesión en todos los dispositivos",
                                    onClick = { showEndAllSessionsDialog = true },
                                    iconColor = Color(0xFFEF4444)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Actividad de seguridad
                        SettingsSectionTitle("Actividad de seguridad")
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column {
                                SecurityItem(
                                    icon = Icons.Outlined.History,
                                    title = "Historial de actividad",
                                    subtitle = "Ver inicios de sesión recientes",
                                    onClick = { showActivityHistory = true },
                                    iconColor = Color(0xFF6B7280),
                                    showArrow = true
                                )
                                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                SecurityItem(
                                    icon = Icons.Outlined.Report,
                                    title = "Actividad sospechosa",
                                    subtitle = if (suspiciousCount > 0) "$suspiciousCount alerta${if (suspiciousCount != 1) "s" else ""} sin revisar" else "No se detectó actividad inusual",
                                    onClick = { showSuspiciousActivity = true },
                                    iconColor = if (suspiciousCount > 0) Color(0xFFEF4444) else Color(0xFF2E8B57),
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Zona de peligro
                        SettingsSectionTitle("Zona de peligro")
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.08f)
                        ) {
                            Column {
                                SecurityItem(
                                    icon = Icons.Outlined.PersonOff,
                                    title = "Desactivar cuenta",
                                    subtitle = "Tu cuenta se ocultará temporalmente",
                                    onClick = { showDeactivateAccountDialog = true },
                                    iconColor = Color(0xFFFF6B35)
                                )
                                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                SecurityItem(
                                    icon = Icons.Outlined.DeleteForever,
                                    title = "Eliminar cuenta",
                                    subtitle = "Esta acción es irreversible",
                                    onClick = { showDeleteAccountDialog = true },
                                    iconColor = Color(0xFFEF4444)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
        
        // Sub-pantallas
        ChangePasswordScreen(
            isVisible = showChangePassword,
            onDismiss = { showChangePassword = false }
        )
        
        PasswordRequirementsScreen(
            isVisible = showPasswordRequirements,
            onDismiss = { showPasswordRequirements = false }
        )
        
        TrustedDevicesScreen(
            isVisible = showTrustedDevices,
            onDismiss = { 
                showTrustedDevices = false
                // Recargar conteo
                scope.launch {
                    val userId = SupabaseClient.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        trustedDevicesCount = SecurityRepository.getTrustedDevices(userId).size
                    }
                }
            }
        )
        
        ActiveSessionsScreen(
            isVisible = showActiveSessions,
            onDismiss = { 
                showActiveSessions = false
                // Recargar conteo
                scope.launch {
                    val userId = SupabaseClient.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        activeSessionsCount = SecurityRepository.getActiveSessions(userId).size
                    }
                }
            }
        )
        
        ActivityHistoryScreen(
            isVisible = showActivityHistory,
            onDismiss = { showActivityHistory = false }
        )
        
        SuspiciousActivityScreen(
            isVisible = showSuspiciousActivity,
            onDismiss = { 
                showSuspiciousActivity = false
                // Recargar conteo
                scope.launch {
                    val userId = SupabaseClient.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        suspiciousCount = SecurityRepository.getSuspiciousActivities(userId, onlyUnresolved = true).size
                    }
                }
            }
        )
        
        TwoFactorSetupScreen(
            isVisible = showTwoFactorSetup,
            onDismiss = { 
                showTwoFactorSetup = false
                // Recargar settings
                scope.launch {
                    val userId = SupabaseClient.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        settings = SecurityRepository.getSecuritySettings(userId)
                    }
                }
            }
        )
        
        // Dialog para desactivar cuenta
        if (showDeactivateAccountDialog) {
            AlertDialog(
                onDismissRequest = { if (!isProcessingAccount) showDeactivateAccountDialog = false },
                title = { 
                    Text(
                        "Desactivar cuenta",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                text = { 
                    Column {
                        Text(
                            "Tu cuenta será desactivada temporalmente:",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("• Tu perfil no será visible para otros usuarios", color = TextMuted, fontSize = 13.sp)
                        Text("• Tus posts y contenido se ocultarán", color = TextMuted, fontSize = 13.sp)
                        Text("• Podrás reactivar tu cuenta en cualquier momento", color = TextMuted, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Para reactivar, solo inicia sesión nuevamente.",
                            color = Color(0xFF2E8B57),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessingAccount = true
                                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                                if (userId != null) {
                                    val success = SecurityRepository.deactivateAccount(userId)
                                    if (success) {
                                        Toast.makeText(context, "Cuenta desactivada", Toast.LENGTH_SHORT).show()
                                        // PRIMERO limpiar sesión persistida
                                        com.rendly.app.data.remote.SessionPersistence.clearSession()
                                        SupabaseClient.auth.signOut()
                                        shouldLogout = true
                                    } else {
                                        Toast.makeText(context, "Error al desactivar cuenta", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                isProcessingAccount = false
                                showDeactivateAccountDialog = false
                            }
                        },
                        enabled = !isProcessingAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                    ) {
                        if (isProcessingAccount) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Desactivar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeactivateAccountDialog = false },
                        enabled = !isProcessingAccount
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = Surface
            )
        }
        
        // Dialog para eliminar cuenta
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { if (!isProcessingAccount) showDeleteAccountDialog = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Eliminar cuenta",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                text = { 
                    Column {
                        Text(
                            "⚠️ Esta acción es IRREVERSIBLE",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Se eliminarán permanentemente:", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Tu perfil y toda tu información", color = TextMuted, fontSize = 13.sp)
                        Text("• Todos tus posts, rends y stories", color = TextMuted, fontSize = 13.sp)
                        Text("• Tus mensajes y conversaciones", color = TextMuted, fontSize = 13.sp)
                        Text("• Tus seguidores y siguiendo", color = TextMuted, fontSize = 13.sp)
                        Text("• Tu historial de pedidos", color = TextMuted, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Escribe \"ELIMINAR\" para confirmar:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deleteConfirmText,
                            onValueChange = { deleteConfirmText = it },
                            placeholder = { Text("ELIMINAR", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFEF4444),
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessingAccount = true
                                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                                if (userId != null) {
                                    val success = SecurityRepository.deleteAccount(userId)
                                    if (success) {
                                        Toast.makeText(context, "Cuenta eliminada permanentemente", Toast.LENGTH_LONG).show()
                                        // PRIMERO limpiar sesión persistida
                                        com.rendly.app.data.remote.SessionPersistence.clearSession()
                                        SupabaseClient.auth.signOut()
                                        shouldLogout = true
                                    } else {
                                        Toast.makeText(context, "Error al eliminar cuenta", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                isProcessingAccount = false
                                showDeleteAccountDialog = false
                                deleteConfirmText = ""
                            }
                        },
                        enabled = !isProcessingAccount && deleteConfirmText == "ELIMINAR",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            disabledContainerColor = Color(0xFFEF4444).copy(alpha = 0.3f)
                        )
                    ) {
                        if (isProcessingAccount) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Eliminar permanentemente")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteAccountDialog = false
                            deleteConfirmText = ""
                        },
                        enabled = !isProcessingAccount
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = Surface
            )
        }
    }
}

private fun formatLastPasswordChange(dateString: String?): String {
    if (dateString == null) return "Nunca actualizada"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = inputFormat.parse(dateString.take(19)) ?: return "Desconocido"
        
        val now = Date()
        val diffMs = now.time - date.time
        val diffDays = diffMs / (1000 * 60 * 60 * 24)
        
        when {
            diffDays == 0L -> "Actualizada hoy"
            diffDays == 1L -> "Actualizada ayer"
            diffDays < 7 -> "Hace $diffDays días"
            diffDays < 30 -> "Hace ${diffDays / 7} semana${if (diffDays / 7 > 1) "s" else ""}"
            diffDays < 365 -> "Hace ${diffDays / 30} mes${if (diffDays / 30 > 1) "es" else ""}"
            else -> "Hace más de un año"
        }
    } catch (e: Exception) {
        "Desconocido"
    }
}


@Composable
private fun SecurityItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
        
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SecurityToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    iconColor: Color,
    badge: String? = null,
    badgeColor: Color = PrimaryPurple
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
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
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
                checkedTrackColor = iconColor,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Surface
            )
        )
    }
}
