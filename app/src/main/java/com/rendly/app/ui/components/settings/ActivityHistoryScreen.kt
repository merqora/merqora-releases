package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun ActivityHistoryScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var activityLogs by remember { mutableStateOf<List<SecurityRepository.ActivityLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("all") }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                activityLogs = SecurityRepository.getActivityLogs(userId, 100)
            }
            isLoading = false
        }
    }
    
    val filteredLogs = remember(activityLogs, selectedFilter) {
        when (selectedFilter) {
            "login" -> activityLogs.filter { it.activity_type in listOf("login", "logout") }
            "security" -> activityLogs.filter { 
                it.activity_type in listOf("password_change", "2fa_enabled", "2fa_disabled", "settings_change") 
            }
            "devices" -> activityLogs.filter { 
                it.activity_type in listOf("device_added", "device_removed", "session_ended") 
            }
            "suspicious" -> activityLogs.filter { it.is_suspicious }
            else -> activityLogs
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
                    title = "Historial de actividad",
                    subtitle = "Registro de acciones en tu cuenta",
                    icon = Icons.Outlined.History,
                    iconColor = Color(0xFF6B7280),
                    onBack = onDismiss
                )
                
                // Filtros
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipButton(
                        text = "Todo",
                        isSelected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" }
                    )
                    FilterChipButton(
                        text = "Sesiones",
                        isSelected = selectedFilter == "login",
                        onClick = { selectedFilter = "login" }
                    )
                    FilterChipButton(
                        text = "Seguridad",
                        isSelected = selectedFilter == "security",
                        onClick = { selectedFilter = "security" }
                    )
                    FilterChipButton(
                        text = "Sospechoso",
                        isSelected = selectedFilter == "suspicious",
                        onClick = { selectedFilter = "suspicious" },
                        color = Color(0xFFEF4444)
                    )
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay actividad registrada",
                                color = TextMuted,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs) { log ->
                            ActivityLogItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color = PrimaryPurple
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color else Surface
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun ActivityLogItem(log: SecurityRepository.ActivityLog) {
    val (icon, iconColor, bgColor) = getActivityIcon(log.activity_type, log.is_suspicious)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (log.is_suspicious) Color(0xFFEF4444).copy(alpha = 0.08f) else Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getActivityTitle(log.activity_type),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    if (log.is_suspicious) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "SOSPECHOSO",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = log.description ?: getActivityDescription(log.activity_type),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fecha
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(log.created_at),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    
                    // Dispositivo
                    log.device_info?.let { device ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.PhoneAndroid,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = device.take(20),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    
                    // Ubicación
                    log.location?.let { location ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getActivityIcon(type: String, isSuspicious: Boolean): Triple<ImageVector, Color, Color> {
    return when {
        isSuspicious -> Triple(
            Icons.Filled.Warning,
            Color(0xFFEF4444),
            Color(0xFFEF4444).copy(alpha = 0.12f)
        )
        type == "login" -> Triple(
            Icons.Outlined.Login,
            Color(0xFF2E8B57),
            Color(0xFF2E8B57).copy(alpha = 0.12f)
        )
        type == "logout" -> Triple(
            Icons.Outlined.Logout,
            Color(0xFF6B7280),
            Color(0xFF6B7280).copy(alpha = 0.12f)
        )
        type == "password_change" -> Triple(
            Icons.Outlined.Key,
            Color(0xFFFF6B35),
            Color(0xFFFF6B35).copy(alpha = 0.12f)
        )
        type == "2fa_enabled" -> Triple(
            Icons.Outlined.Security,
            Color(0xFF2E8B57),
            Color(0xFF2E8B57).copy(alpha = 0.12f)
        )
        type == "2fa_disabled" -> Triple(
            Icons.Outlined.SecurityUpdateWarning,
            Color(0xFFFF6B35),
            Color(0xFFFF6B35).copy(alpha = 0.12f)
        )
        type == "device_added" -> Triple(
            Icons.Outlined.PhonelinkSetup,
            Color(0xFF1565A0),
            Color(0xFF1565A0).copy(alpha = 0.12f)
        )
        type == "device_removed" -> Triple(
            Icons.Outlined.PhonelinkErase,
            Color(0xFFEF4444),
            Color(0xFFEF4444).copy(alpha = 0.12f)
        )
        type == "session_ended" -> Triple(
            Icons.Outlined.ExitToApp,
            Color(0xFF6B7280),
            Color(0xFF6B7280).copy(alpha = 0.12f)
        )
        type == "settings_change" -> Triple(
            Icons.Outlined.Settings,
            Color(0xFF1565A0),
            Color(0xFF1565A0).copy(alpha = 0.12f)
        )
        else -> Triple(
            Icons.Outlined.History,
            Color(0xFF6B7280),
            Color(0xFF6B7280).copy(alpha = 0.12f)
        )
    }
}

private fun getActivityTitle(type: String): String {
    return when (type) {
        "login" -> "Inicio de sesión"
        "logout" -> "Cierre de sesión"
        "password_change" -> "Cambio de contraseña"
        "2fa_enabled" -> "2FA activado"
        "2fa_disabled" -> "2FA desactivado"
        "device_added" -> "Dispositivo agregado"
        "device_removed" -> "Dispositivo eliminado"
        "session_ended" -> "Sesión cerrada"
        "settings_change" -> "Configuración modificada"
        "suspicious_login" -> "Inicio sospechoso"
        else -> "Actividad"
    }
}

private fun getActivityDescription(type: String): String {
    return when (type) {
        "login" -> "Iniciaste sesión en tu cuenta"
        "logout" -> "Cerraste sesión de tu cuenta"
        "password_change" -> "Tu contraseña fue actualizada"
        "2fa_enabled" -> "Autenticación de dos factores activada"
        "2fa_disabled" -> "Autenticación de dos factores desactivada"
        "device_added" -> "Nuevo dispositivo de confianza"
        "device_removed" -> "Dispositivo eliminado de confianza"
        "session_ended" -> "Sesión cerrada remotamente"
        "settings_change" -> "Configuración de seguridad modificada"
        else -> "Actividad en tu cuenta"
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "Desconocido"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
        val date = inputFormat.parse(dateString.take(19))
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        "Reciente"
    }
}
