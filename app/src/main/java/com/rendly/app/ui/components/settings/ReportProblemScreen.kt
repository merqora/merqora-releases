package com.rendly.app.ui.components.settings

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.rendly.app.ui.theme.*
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.FeedbackRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReportProblemScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedProblemType by remember { mutableStateOf<String?>(null) }
    var problemDescription by remember { mutableStateOf("") }
    var stepsToReproduce by remember { mutableStateOf("") }
    var includeDeviceInfo by remember { mutableStateOf(true) }
    var includeLogs by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var attachedScreenshots by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Get current user info
    val currentUser = remember { SupabaseClient.client.auth.currentUserOrNull() }
    val userId = currentUser?.id ?: "anonymous"
    val userEmail = currentUser?.email
    
    // Load user name from usuarios table
    var userName by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(userId) {
        if (userId != "anonymous") {
            try {
                withContext(Dispatchers.IO) {
                    val result = SupabaseClient.client.from("usuarios")
                        .select() {
                            filter { eq("user_id", userId) }
                            limit(1)
                        }
                        .decodeSingleOrNull<Map<String, Any?>>()
                    
                    userName = result?.get("nombre")?.toString()
                        ?: result?.get("username")?.toString()
                }
            } catch (e: Exception) {
                android.util.Log.e("ReportProblemScreen", "Error loading user name: ${e.message}")
            }
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
            ) {
                // Header
                SettingsScreenHeader(
                    title = "Reportar un problema",
                    subtitle = "Ayúdanos a mejorar Merqora",
                    icon = Icons.Outlined.BugReport,
                    iconColor = Color(0xFFEF4444),
                    onBack = onDismiss
                )
                
                if (showSuccess) {
                    // Pantalla de éxito
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFF2E8B57).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Reporte enviado",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Nuestro equipo técnico revisará el problema reportado. Te contactaremos si necesitamos Más Información.",
                            fontSize = 15.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Surface
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Ticket de soporte",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "#BUG-${System.currentTimeMillis().toString().takeLast(8)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Volver",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Intro
                        Text(
                            text = "¿Qué problema encontraste?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Describe el problema con el mayor detalle posible para que podamos solucionarlo Rápidamente.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Tipo de problema
                        Text(
                            text = "Tipo de problema",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val problemTypes = listOf(
                            Triple("crash", "La app se cierra", Icons.Outlined.ErrorOutline),
                            Triple("ui", "Error visual/UI", Icons.Outlined.BrokenImage),
                            Triple("performance", "Lentitud", Icons.Outlined.SlowMotionVideo),
                            Triple("data", "Datos incorrectos", Icons.Outlined.Storage),
                            Triple("network", "Problema de Conexión", Icons.Outlined.WifiOff),
                            Triple("security", "Problema de seguridad", Icons.Outlined.Security),
                            Triple("other", "Otro problema", Icons.Outlined.MoreHoriz)
                        )
                        
                        // Grid 2x4
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            problemTypes.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { (id, label, icon) ->
                                        ProblemTypeChip(
                                            label = label,
                                            icon = icon,
                                            isSelected = selectedProblemType == id,
                                            onClick = { selectedProblemType = id },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Descripción
                        Text(
                            text = "Describe el problema",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = problemDescription,
                            onValueChange = { problemDescription = it },
                            placeholder = {
                                Text(
                                    "¿Qué estaba pasando cuando ocurrió el problema?",
                                    color = TextMuted.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Pasos para reproducir
                        Text(
                            text = "Pasos para reproducir (opcional)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = stepsToReproduce,
                            onValueChange = { stepsToReproduce = it },
                            placeholder = {
                                Text(
                                    "1. Abrir la app\n2. Ir a...\n3. Pulsar en...",
                                    color = TextMuted.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Capturas de pantalla
                        Text(
                            text = "Capturas de pantalla",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { /* TODO: Abrir galería */ },
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Agregar capturas",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryPurple
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Información adicional
                        Text(
                            text = "Información adicional",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { includeDeviceInfo = !includeDeviceInfo }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PhoneAndroid,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Incluir info del dispositivo",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Modelo, Versión de Android, etc.",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Switch(
                                        checked = includeDeviceInfo,
                                        onCheckedChange = { includeDeviceInfo = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PrimaryPurple
                                        )
                                    )
                                }
                                
                                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { includeLogs = !includeLogs }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Code,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Incluir logs de la app",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Ayuda a diagnosticar el problema",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Switch(
                                        checked = includeLogs,
                                        onCheckedChange = { includeLogs = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PrimaryPurple
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // botón enviar
                        Button(
                            onClick = {
                                isSubmitting = true
                                scope.launch {
                                    val result = FeedbackRepository.submitBugReport(
                                        userId = userId,
                                        title = selectedProblemType?.let { type ->
                                            problemTypes.find { it.first == type }?.second
                                        } ?: "Problema reportado",
                                        description = problemDescription,
                                        stepsToReproduce = stepsToReproduce.ifBlank { null },
                                        severity = "medium",
                                        category = selectedProblemType,
                                        userName = userName,
                                        userEmail = userEmail,
                                        includeDeviceInfo = includeDeviceInfo,
                                        includeLogs = includeLogs,
                                        context = context
                                    )
                                    
                                    isSubmitting = false
                                    when (result) {
                                        is FeedbackRepository.SubmissionResult.Success -> {
                                            showSuccess = true
                                        }
                                        is FeedbackRepository.SubmissionResult.Error -> {
                                            showError = true
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = problemDescription.isNotBlank() && selectedProblemType != null && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                disabledContainerColor = Color(0xFFEF4444).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(
                                imageVector = Icons.Outlined.BugReport,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSubmitting) "Enviando..." else "Enviar reporte",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        
                        // Mostrar error si hay
                        if (showError) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Error: $errorMessage",
                                        fontSize = 13.sp,
                                        color = Color(0xFFEF4444),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProblemTypeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.1f) else Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFEF4444) else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFFEF4444) else TextMuted
            )
        }
    }
}
