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
fun TrustedDevicesScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var devices by remember { mutableStateOf<List<SecurityRepository.TrustedDevice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var deviceToRemove by remember { mutableStateOf<SecurityRepository.TrustedDevice?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                // Registrar dispositivo actual si no existe
                SecurityRepository.registerDevice(userId, context, isCurrent = true)
                devices = SecurityRepository.getTrustedDevices(userId)
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
    
    // Dialog de confirmación para eliminar
    if (deviceToRemove != null) {
        AlertDialog(
            onDismissRequest = { deviceToRemove = null },
            title = { 
                Text(
                    "Eliminar dispositivo",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ) 
            },
            text = { 
                Text(
                    "¿estás seguro de que quieres eliminar \"${deviceToRemove?.device_name}\" de la lista de dispositivos de confianza?",
                    color = TextSecondary
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                            deviceToRemove?.id?.let { deviceId ->
                                if (userId != null) {
                                    SecurityRepository.removeDevice(deviceId, userId)
                                    devices = SecurityRepository.getTrustedDevices(userId)
                                }
                            }
                            deviceToRemove = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToRemove = null }) {
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
                    title = "Dispositivos de confianza",
                    subtitle = "Dispositivos que pueden acceder a tu cuenta",
                    icon = Icons.Outlined.Devices,
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
                } else if (devices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Devices,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay dispositivos registrados",
                                color = TextMuted,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dispositivo actual primero
                        val currentDevice = devices.find { it.is_current }
                        val otherDevices = devices.filter { !it.is_current }
                        
                        if (currentDevice != null) {
                            item {
                                Text(
                                    text = "ESTE DISPOSITIVO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                )
                            }
                            
                            item {
                                DeviceItem(
                                    device = currentDevice,
                                    isCurrent = true,
                                    onRemove = null // No se puede eliminar el actual
                                )
                            }
                        }
                        
                        if (otherDevices.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "OTROS DISPOSITIVOS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                )
                            }
                            
                            items(otherDevices) { device ->
                                DeviceItem(
                                    device = device,
                                    isCurrent = false,
                                    onRemove = { deviceToRemove = device }
                                )
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Info
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF1565A0).copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF1565A0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Los dispositivos de confianza pueden acceder a tu cuenta sin Verificación adicional. Elimina los que ya no uses para mayor seguridad.",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: SecurityRepository.TrustedDevice,
    isCurrent: Boolean,
    onRemove: (() -> Unit)?
) {
    val deviceIcon = when (device.device_type) {
        "android" -> Icons.Outlined.PhoneAndroid
        "ios" -> Icons.Outlined.PhoneIphone
        "web" -> Icons.Outlined.Language
        "desktop" -> Icons.Outlined.Computer
        else -> Icons.Outlined.Devices
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrent) PrimaryPurple.copy(alpha = 0.08f) else Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isCurrent) PrimaryPurple.copy(alpha = 0.15f) 
                        else Color(0xFF1565A0).copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = deviceIcon,
                    contentDescription = null,
                    tint = if (isCurrent) PrimaryPurple else Color(0xFF1565A0),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.device_name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryPurple.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ACTUAL",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${device.os_version ?: device.device_type} \u00b7 ${device.app_version ?: "Merqora"}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCurrent) "Ahora" else formatDate(device.last_used_at),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    
                    device.last_location?.let { location ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
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
            
            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar dispositivo",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "Desconocido"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = inputFormat.parse(dateString.take(19)) ?: return "Desconocido"
        
        val now = Date()
        val diffMs = now.time - date.time
        val diffDays = diffMs / (1000 * 60 * 60 * 24)
        
        when {
            diffDays == 0L -> "Hoy"
            diffDays == 1L -> "Ayer"
            diffDays < 7 -> "Hace $diffDays Días"
            else -> {
                val outputFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "Desconocido"
    }
}
