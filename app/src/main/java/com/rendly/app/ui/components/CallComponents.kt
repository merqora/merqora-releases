package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.CallEndReason
import com.rendly.app.data.model.CallState
import com.rendly.app.data.model.CallStatus
import com.rendly.app.data.repository.CallRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

/* ═══════════════════════════════════════════════════════════════
   PANTALLA DE LLAMADA COMPLETA (Full-screen overlay)
   - Llamada saliente (esperando)
   - Llamada conectada (en curso)
   - Llamada finalizada
═══════════════════════════════════════════════════════════════ */

@Composable
fun CallScreen(
    callState: CallState,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Animación de pulso para el avatar cuando está sonando/conectando
    val infiniteTransition = rememberInfiniteTransition(label = "callPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Gradiente de fondo según estado
    val bgGradient = when (callState.status) {
        CallStatus.OUTGOING -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
        )
        CallStatus.CONNECTED -> Brush.verticalGradient(
            colors = listOf(Color(0xFF0D1117), Color(0xFF161B22), Color(0xFF1A2332))
        )
        CallStatus.RECONNECTING -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1A0E), Color(0xFF2E2E16), Color(0xFF3D3D0F))
        )
        CallStatus.ENDED -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A0D0D), Color(0xFF2E1616), Color(0xFF3D1A1A))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF0D1117), Color(0xFF161B22))
        )
    }
    
    val avatarUrl = remember(callState.otherAvatarUrl, callState.otherUsername) {
        when {
            callState.otherAvatarUrl.isEmpty() -> 
                "https://ui-avatars.com/api/?name=${callState.otherUsername}&background=A78BFA&color=fff&size=256"
            callState.otherAvatarUrl.startsWith("http") -> callState.otherAvatarUrl
            else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${callState.otherAvatarUrl}"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // ═══ Estado de la llamada ═══
            Text(
                text = when (callState.status) {
                    CallStatus.OUTGOING -> "Llamando..."
                    CallStatus.CONNECTED -> "En llamada"
                    CallStatus.RECONNECTING -> "Reconectando..."
                    CallStatus.ENDED -> when (callState.endReason) {
                        CallEndReason.REJECTED -> "Llamada rechazada"
                        CallEndReason.BUSY -> "Ocupado"
                        CallEndReason.MISSED, CallEndReason.TIMEOUT -> "Sin respuesta"
                        CallEndReason.NETWORK_ERROR -> "Error de conexión"
                        else -> "Llamada finalizada"
                    }
                    else -> ""
                },
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ═══ Nombre del usuario ═══
            Text(
                text = callState.otherUsername,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ═══ Duración / Estado secundario ═══
            Text(
                text = when (callState.status) {
                    CallStatus.CONNECTED -> CallRepository.formatDuration(callState.durationSeconds)
                    CallStatus.OUTGOING -> "Esperando respuesta..."
                    CallStatus.RECONNECTING -> "Verificando conexión..."
                    CallStatus.ENDED -> if (callState.durationSeconds > 0) 
                        "Duración: ${CallRepository.formatDuration(callState.durationSeconds)}" else ""
                    else -> ""
                },
                fontSize = 16.sp,
                color = when (callState.status) {
                    CallStatus.CONNECTED -> Color(0xFF4ADE80)
                    CallStatus.RECONNECTING -> Color(0xFFFBBF24)
                    else -> Color.White.copy(alpha = 0.5f)
                },
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // ═══ Avatar grande ═══
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Anillos de pulso
                if (callState.status == CallStatus.OUTGOING || callState.status == CallStatus.CONNECTED) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                if (callState.status == CallStatus.CONNECTED)
                                    Color(0xFF4ADE80).copy(alpha = 0.08f)
                                else
                                    PrimaryPurple.copy(alpha = 0.1f)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(if (callState.status == CallStatus.OUTGOING) pulseScale * 0.95f else 1f)
                            .clip(CircleShape)
                            .background(
                                if (callState.status == CallStatus.CONNECTED)
                                    Color(0xFF4ADE80).copy(alpha = 0.12f)
                                else
                                    PrimaryPurple.copy(alpha = 0.15f)
                            )
                    )
                }
                
                // Avatar
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = callState.otherUsername,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                )
                
                // Indicador de estado en la esquina
                if (callState.status == CallStatus.CONNECTED) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // ═══ Controles de llamada ═══
            if (callState.status == CallStatus.CONNECTED || callState.status == CallStatus.RECONNECTING) {
                // En llamada: Mute, Speaker, Colgar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute
                    CallControlButton(
                        icon = if (callState.isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (callState.isMuted) "Activar" else "Silenciar",
                        isActive = callState.isMuted,
                        activeColor = Color(0xFFEF4444),
                        onClick = onToggleMute
                    )
                    
                    // Colgar (grande)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .clickable {
                                scope.launch { onEndCall() }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "Colgar",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Speaker
                    CallControlButton(
                        icon = if (callState.isSpeakerOn) Icons.Filled.VolumeUp else Icons.Outlined.VolumeUp,
                        label = if (callState.isSpeakerOn) "Auricular" else "Altavoz",
                        isActive = callState.isSpeakerOn,
                        activeColor = PrimaryPurple,
                        onClick = onToggleSpeaker
                    )
                }
            } else if (callState.status == CallStatus.OUTGOING) {
                // Llamada saliente: solo Colgar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .clickable {
                            scope.launch { onEndCall() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = "Cancelar",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Cancelar llamada",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // ═══ Cifrado / Seguridad ═══
            if (callState.status == CallStatus.CONNECTED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Llamada cifrada de extremo a extremo",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   BOTÓN DE CONTROL DE LLAMADA
═══════════════════════════════════════════════════════════════ */

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) activeColor.copy(alpha = 0.2f)
                    else Color.White.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   MODAL DE LLAMADA ENTRANTE (overlay sobre cualquier pantalla)
═══════════════════════════════════════════════════════════════ */

@Composable
fun IncomingCallOverlay(
    callState: CallState,
    onAnswer: () -> Unit,
    onReject: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "incomingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "incomingPulse"
    )
    
    val scope = rememberCoroutineScope()
    
    val avatarUrl = remember(callState.otherAvatarUrl, callState.otherUsername) {
        when {
            callState.otherAvatarUrl.isEmpty() -> 
                "https://ui-avatars.com/api/?name=${callState.otherUsername}&background=A78BFA&color=fff&size=256"
            callState.otherAvatarUrl.startsWith("http") -> callState.otherAvatarUrl
            else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${callState.otherAvatarUrl}"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117).copy(alpha = 0.97f),
                        Color(0xFF161B22).copy(alpha = 0.98f),
                        Color(0xFF1A2332)
                    )
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Icono de llamada
            Icon(
                imageVector = Icons.Filled.PhoneInTalk,
                contentDescription = null,
                tint = Color(0xFF4ADE80),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Llamada entrante",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Avatar con pulso
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80).copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(135.dp)
                        .scale(pulseScale * 0.97f)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80).copy(alpha = 0.12f))
                )
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = callState.otherUsername,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Nombre
            Text(
                text = callState.otherUsername,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (callState.callType == "video") "Videollamada" else "Llamada de voz",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Botones: Rechazar | Contestar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Rechazar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .clickable {
                                scope.launch { onReject() }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "Rechazar",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Rechazar",
                        fontSize = 13.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Contestar
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                            .clickable {
                                scope.launch { onAnswer() }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Contestar",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Contestar",
                        fontSize = 13.sp,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   BANNER COMPACTO DE LLAMADA EN CURSO
   (se muestra en la parte superior cuando navegas a otra pantalla)
═══════════════════════════════════════════════════════════════ */

@Composable
fun ActiveCallBanner(
    callState: CallState,
    onClick: () -> Unit
) {
    if (callState.status != CallStatus.CONNECTED && callState.status != CallStatus.RECONNECTING) return
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color(0xFF22C55E)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "En llamada con ${callState.otherUsername}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = CallRepository.formatDuration(callState.durationSeconds),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Toca para volver",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
