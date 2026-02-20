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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.SecurityRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SuspiciousActivityScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var activities by remember { mutableStateOf<List<SecurityRepository.SuspiciousActivity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showResolved by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(isVisible, showResolved) {
        if (isVisible) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                activities = SecurityRepository.getSuspiciousActivities(userId, !showResolved)
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
                    title = "Actividad sospechosa",
                    subtitle = "Revisa intentos de acceso inusuales",
                    icon = Icons.Outlined.Report,
                    iconColor = Color(0xFFEF4444),
                    onBack = onDismiss
                )
                
                // Toggle mostrar resueltos
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mostrar resueltos",
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Switch(
                            checked = showResolved,
                            onCheckedChange = { showResolved = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Surface
                            )
                        )
                    }
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else if (activities.isEmpty()) {
                    // Estado vacío - sin actividad sospechosa
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFF2E8B57).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "¡Todo seguro!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "No se ha detectado actividad sospechosa en tu cuenta. Seguimos monitoreando para mantenerte protegido.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Tips de seguridad
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
                                            text = "Consejos de seguridad",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1565A0)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    SecurityTip("Activa la autenticación de dos factores")
                                    SecurityTip("Usa una contraseña única y segura")
                                    SecurityTip("Revisa tus sesiones activas regularmente")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activities) { activity ->
                            SuspiciousActivityItem(
                                activity = activity,
                                onResolve = {
                                    scope.launch {
                                        val userId = SupabaseClient.auth.currentUserOrNull()?.id
                                        if (userId != null && activity.id != null) {
                                            SecurityRepository.resolveSuspiciousActivity(
                                                activity.id,
                                                userId,
                                                "Marcado como seguro por el usuario"
                                            )
                                            // Recargar lista
                                            activities = SecurityRepository.getSuspiciousActivities(userId, !showResolved)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityTip(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF1565A0).copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun SuspiciousActivityItem(
    activity: SecurityRepository.SuspiciousActivity,
    onResolve: () -> Unit
) {
    val riskColor = when (activity.risk_level) {
        "critical" -> Color(0xFFDC2626)
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFFF6B35)
        else -> Color(0xFF6B7280)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (activity.is_resolved) Surface else riskColor.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activity.is_resolved) 
                                Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (activity.is_resolved) Color(0xFF2E8B57) else riskColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = getSuspiciousActivityTitle(activity.activity_type),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = riskColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = getRiskLevelText(activity.risk_level),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                            
                            if (activity.is_resolved) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF2E8B57).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "RESUELTO",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E8B57)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = activity.description,
                fontSize = 13.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                activity.location?.let { location ->
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(activity.created_at),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
            
            // Botón de acción si no está resuelto
            if (!activity.is_resolved) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onResolve,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2E8B57)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fui yo", fontSize = 13.sp)
                    }
                    
                    Button(
                        onClick = { /* TODO: Reportar y cambiar contraseña */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Proteger", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun getSuspiciousActivityTitle(type: String): String {
    return when (type) {
        "unusual_location" -> "Ubicación inusual"
        "multiple_failed_logins" -> "Múltiples intentos fallidos"
        "new_device" -> "Nuevo dispositivo"
        "unusual_time" -> "Horario inusual"
        "rapid_requests" -> "Actividad sospechosa"
        "suspicious_login" -> "Inicio de sesión sospechoso"
        else -> "Actividad detectada"
    }
}

private fun getRiskLevelText(level: String): String {
    return when (level) {
        "critical" -> "CRÍTICO"
        "high" -> "ALTO"
        "medium" -> "MEDIO"
        else -> "BAJO"
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
