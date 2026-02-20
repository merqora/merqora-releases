package com.rendly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.repository.ChatRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

enum class ClientRequestType {
    REQUEST,    // Solicitud enviada
    ACCEPTED,   // Solicitud aceptada
    REJECTED,   // Solicitud rechazada
    PENDING     // Esperando respuesta (para el solicitante)
}

@Composable
fun ClientRequestMessageBubble(
    content: String,
    isFromMe: Boolean,
    senderId: String,
    senderUsername: String,
    onAccept: () -> Unit = {},
    onReject: (String) -> Unit = {},
    onViewHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    
    val requestType = when {
        ChatRepository.isClientRequestMessage(content) -> ClientRequestType.REQUEST
        ChatRepository.isClientAcceptedMessage(content) -> ClientRequestType.ACCEPTED
        ChatRepository.isClientRejectedMessage(content) -> ClientRequestType.REJECTED
        ChatRepository.isClientPendingMessage(content) -> ClientRequestType.PENDING
        else -> ClientRequestType.REQUEST
    }
    
    val cleanContent = ChatRepository.getCleanMessageContent(content)
    
    val (backgroundColor, borderColor, iconTint) = when (requestType) {
        ClientRequestType.REQUEST -> Triple(
            Color(0xFF1565A0).copy(alpha = 0.1f),
            Color(0xFF1565A0).copy(alpha = 0.3f),
            Color(0xFF1565A0)
        )
        ClientRequestType.ACCEPTED -> Triple(
            Color(0xFF2E8B57).copy(alpha = 0.1f),
            Color(0xFF2E8B57).copy(alpha = 0.3f),
            Color(0xFF2E8B57)
        )
        ClientRequestType.REJECTED -> Triple(
            Color(0xFFEF4444).copy(alpha = 0.1f),
            Color(0xFFEF4444).copy(alpha = 0.3f),
            Color(0xFFEF4444)
        )
        ClientRequestType.PENDING -> Triple(
            Color(0xFFFFA726).copy(alpha = 0.1f),
            Color(0xFFFFA726).copy(alpha = 0.3f),
            Color(0xFFFFA726)
        )
    }
    
    val icon = when (requestType) {
        ClientRequestType.REQUEST -> Icons.Outlined.PersonAdd
        ClientRequestType.ACCEPTED -> Icons.Filled.CheckCircle
        ClientRequestType.REJECTED -> Icons.Filled.Cancel
        ClientRequestType.PENDING -> Icons.Outlined.Schedule
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card de solicitud
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
            color = backgroundColor
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Título según tipo
                Text(
                    text = when (requestType) {
                        ClientRequestType.REQUEST -> "Solicitud de Cliente"
                        ClientRequestType.ACCEPTED -> "¡Solicitud Aceptada!"
                        ClientRequestType.REJECTED -> "Solicitud Rechazada"
                        ClientRequestType.PENDING -> "Esperando Respuesta"
                    },
                    color = iconTint,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Contenido del mensaje
                Text(
                    text = cleanContent,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                
                // Botones de acción solo para REQUEST y si NO es mi mensaje (soy el vendedor)
                if (requestType == ClientRequestType.REQUEST && !isFromMe) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón Ver Historial
                    TextButton(
                        onClick = onViewHistory,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verificar historial",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botones Aceptar / Rechazar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rechazar
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFEF4444), Color(0xFFEF4444))
                                )
                            )
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFEF4444)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rechazar", fontSize = 13.sp)
                            }
                        }
                        
                        // Aceptar
                        Button(
                            onClick = {
                                isProcessing = true
                                scope.launch {
                                    onAccept()
                                    isProcessing = false
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E8B57)
                            )
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aceptar", fontSize = 13.sp)
                            }
                        }
                    }
                }
                
                // Si es mi solicitud pendiente, mostrar estado
                if (requestType == ClientRequestType.REQUEST && isFromMe) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFFFA726),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Esperando respuesta del vendedor",
                            color = Color(0xFFFFA726),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de rechazo
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            containerColor = Surface,
            title = {
                Text(
                    text = "Rechazar solicitud",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "¿Deseas indicar un motivo? (opcional)",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = {
                            Text("Ej: No acepto nuevos clientes por ahora")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            onReject(rejectReason)
                            showRejectDialog = false
                            rejectReason = ""
                            isProcessing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}
