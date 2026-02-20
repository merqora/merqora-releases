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
fun ActiveSessionsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var sessions by remember { mutableStateOf<List<SecurityRepository.UserSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showEndAllDialog by remember { mutableStateOf(false) }
    var sessionToEnd by remember { mutableStateOf<SecurityRepository.UserSession?>(null) }
    var isEndingAll by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Obtener el ID del dispositivo actual para identificar la sesión actual
    val currentDeviceId = remember {
        android.provider.Settings.Secure.getString(
            context.contentResolver, 
            android.provider.Settings.Secure.ANDROID_ID
        )
    }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                sessions = SecurityRepository.getActiveSessions(userId)
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
    if (showEndAllDialog) {
        AlertDialog(
            onDismissRequest = { showEndAllDialog = false },
            title = { 
                Text(
                    "Cerrar todas las sesiones",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ) 
            },
            text = { 
                Text(
                    "Se cerrarán todas las sesiones excepto la actual. Tendrás que volver a iniciar sesión en otros dispositivos.",
                    color = TextSecondary
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isEndingAll = true
                            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                            if (userId != null) {
                                val currentSession = sessions.find { it.device_id == currentDeviceId }
                                SecurityRepository.endAllSessions(userId, currentSession?.id)
                                sessions = SecurityRepository.getActiveSessions(userId)
                            }
                            isEndingAll = false
                            showEndAllDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    enabled = !isEndingAll
                ) {
                    if (isEndingAll) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cerrar todas")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndAllDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = Surface
        )
    }
    
    // Dialog para cerrar sesión individual
    if (sessionToEnd != null) {
        AlertDialog(
            onDismissRequest = { sessionToEnd = null },
            title = { 
                Text(
                    "Cerrar sesión",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ) 
            },
            text = { 
                Text(
                    "¿Estás seguro de que quieres cerrar esta sesión en \"${sessionToEnd?.device_name}\"?",
                    color = TextSecondary
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                            sessionToEnd?.id?.let { sessionId ->
                                if (userId != null) {
                                    SecurityRepository.endSession(sessionId, userId)
                                    sessions = SecurityRepository.getActiveSessions(userId)
                                }
                            }
                            sessionToEnd = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToEnd = null }) {
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
                    title = "Sesiones activas",
                    subtitle = "Sesiones iniciadas en otros dispositivos",
                    icon = Icons.Outlined.Laptop,
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Contador de sesiones
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = PrimaryPurple.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${sessions.size} sesión${if (sessions.size != 1) "es" else ""} activa${if (sessions.size != 1) "s" else ""}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "En todos tus dispositivos",
                                            fontSize = 13.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    
                                    if (sessions.size > 1) {
                                        TextButton(
                                            onClick = { showEndAllDialog = true },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = Color(0xFFEF4444)
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ExitToApp,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Cerrar todas", fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (sessions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.Laptop,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No hay sesiones activas",
                                            color = TextMuted,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // Separar sesión actual de otras
                            val currentSession = sessions.find { it.device_id == currentDeviceId }
                            val otherSessions = sessions.filter { it.device_id != currentDeviceId }
                            
                            if (currentSession != null) {
                                item {
                                    Text(
                                        text = "SESIÓN ACTUAL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                    )
                                }
                                
                                item {
                                    SessionItem(
                                        session = currentSession,
                                        isCurrent = true,
                                        onEnd = null
                                    )
                                }
                            }
                            
                            if (otherSessions.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "OTRAS SESIONES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                                    )
                                }
                                
                                items(otherSessions) { session ->
                                    SessionItem(
                                        session = session,
                                        isCurrent = false,
                                        onEnd = { sessionToEnd = session }
                                    )
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Info de seguridad
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFF6B35).copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Security,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Si ves una sesión que no reconoces, ciérrala inmediatamente y cambia tu contraseña para proteger tu cuenta.",
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
private fun SessionItem(
    session: SecurityRepository.UserSession,
    isCurrent: Boolean,
    onEnd: (() -> Unit)?
) {
    val deviceIcon = when (session.device_type) {
        "android" -> Icons.Outlined.PhoneAndroid
        "ios" -> Icons.Outlined.PhoneIphone
        "web" -> Icons.Outlined.Language
        "desktop" -> Icons.Outlined.Computer
        else -> Icons.Outlined.Devices
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrent) Color(0xFF2E8B57).copy(alpha = 0.08f) else Surface
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
                        if (isCurrent) Color(0xFF2E8B57).copy(alpha = 0.15f) 
                        else Color(0xFF1565A0).copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = deviceIcon,
                    contentDescription = null,
                    tint = if (isCurrent) Color(0xFF2E8B57) else Color(0xFF1565A0),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.device_name ?: "Dispositivo desconocido",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E8B57))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (isCurrent) "Sesión activa ahora" else "Última actividad: ${formatDate(session.last_activity_at)}",
                    fontSize = 12.sp,
                    color = if (isCurrent) Color(0xFF2E8B57) else TextSecondary
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    session.location?.let { location ->
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
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Iniciada ${formatDate(session.started_at)}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
            
            if (onEnd != null) {
                IconButton(
                    onClick = onEnd,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = "Cerrar sesión",
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
        val diffMinutes = diffMs / (1000 * 60)
        val diffHours = diffMs / (1000 * 60 * 60)
        val diffDays = diffMs / (1000 * 60 * 60 * 24)
        
        when {
            diffMinutes < 1 -> "Ahora"
            diffMinutes < 60 -> "Hace $diffMinutes min"
            diffHours < 24 -> "Hace $diffHours h"
            diffDays == 1L -> "Ayer"
            diffDays < 7 -> "Hace $diffDays días"
            else -> {
                val outputFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "Desconocido"
    }
}
