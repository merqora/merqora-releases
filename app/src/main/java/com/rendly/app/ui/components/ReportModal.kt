package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

data class ReportReason(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

private val reportReasons = listOf(
    ReportReason(
        "spam",
        Icons.Outlined.Report,
        "Spam",
        "Contenido repetitivo o no deseado",
        Color(0xFF6B7280)
    ),
    ReportReason(
        "inappropriate",
        Icons.Outlined.RemoveCircleOutline,
        "Contenido inapropiado",
        "Contenido para adultos o explícito",
        Color(0xFFEF4444)
    ),
    ReportReason(
        "harassment",
        Icons.Outlined.PersonOff,
        "Acoso o bullying",
        "Comportamiento abusivo hacia otros",
        Color(0xFFFF6B35)
    ),
    ReportReason(
        "fake",
        Icons.Outlined.Warning,
        "Información falsa",
        "Producto o información engañosa",
        Color(0xFF1565A0)
    ),
    ReportReason(
        "scam",
        Icons.Outlined.MoneyOff,
        "Estafa o fraude",
        "Intento de engañar para obtener dinero",
        Color(0xFFDC2626)
    ),
    ReportReason(
        "hate_speech",
        Icons.Outlined.SentimentVeryDissatisfied,
        "Discurso de odio",
        "Contenido que promueve odio o discriminación",
        Color(0xFF0A3D62)
    ),
    ReportReason(
        "other",
        Icons.Outlined.MoreHoriz,
        "Otro motivo",
        "Describe el problema en detalle",
        Color(0xFF6B7280)
    )
)

@Composable
fun ReportModal(
    isVisible: Boolean,
    contentType: String = "post", // post, rend, user, comment, message, story
    contentId: String = "",
    username: String = "",
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String, description: String?) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var additionalDetails by remember { mutableStateOf("") }
    var showDetailsInput by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Reset state when modal opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            selectedReason = null
            additionalDetails = ""
            showDetailsInput = false
            isSubmitting = false
            showSuccess = false
        }
    }
    
    // Backdrop
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )
    }
    
    // Modal
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Handle y Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TextMuted.copy(alpha = 0.3f))
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (showSuccess) {
                            // Success state
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "¡Gracias por tu reporte!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Revisaremos el contenido y tomaremos las medidas necesarias",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Text("Cerrar", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            // Normal state
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { 
                                    if (showDetailsInput) {
                                        showDetailsInput = false
                                    } else {
                                        onDismiss()
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (showDetailsInput) Icons.Default.ArrowBack else Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = TextPrimary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (showDetailsInput) "Detalles adicionales" else "Reportar ${
                                            when(contentType) {
                                                "post" -> "publicación"
                                                "rend" -> "rend"
                                                "user" -> "usuario"
                                                "comment" -> "comentario"
                                                "message" -> "mensaje"
                                                "story" -> "historia"
                                                else -> "contenido"
                                            }
                                        }",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (username.isNotEmpty() && !showDetailsInput) {
                                        Text(
                                            text = "de @$username",
                                            fontSize = 13.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    if (!showSuccess) {
                        Divider(color = BorderSubtle)
                        
                        if (showDetailsInput && selectedReason != null) {
                            // Input de detalles adicionales
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {
                                // Razón seleccionada
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = selectedReason!!.color.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = selectedReason!!.icon,
                                            contentDescription = null,
                                            tint = selectedReason!!.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = selectedReason!!.title,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = selectedReason!!.description,
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text(
                                    text = "¿Puedes darnos más detalles? (opcional)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = additionalDetails,
                                    onValueChange = { additionalDetails = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    placeholder = { 
                                        Text(
                                            "Describe lo que viste...",
                                            color = TextMuted
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedContainerColor = SurfaceElevated,
                                        unfocusedContainerColor = SurfaceElevated
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Button(
                                    onClick = {
                                        isSubmitting = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSubmitReport(
                                            selectedReason!!.id,
                                            additionalDetails.takeIf { it.isNotBlank() }
                                        )
                                        showSuccess = true
                                        isSubmitting = false
                                    },
                                    enabled = !isSubmitting,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Flag,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Enviar reporte",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        } else {
                            // Lista de razones
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                item {
                                    Text(
                                        text = "¿Por qué reportas este contenido?",
                                        fontSize = 14.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                                    )
                                }
                                
                                items(reportReasons) { reason ->
                                    ReportReasonItem(
                                        reason = reason,
                                        isSelected = selectedReason?.id == reason.id,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedReason = reason
                                            showDetailsInput = true
                                        }
                                    )
                                }
                                
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))
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
private fun ReportReasonItem(
    reason: ReportReason,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) reason.color.copy(alpha = 0.12f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(reason.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reason.icon,
                    contentDescription = null,
                    tint = reason.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reason.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = reason.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
