package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.HandshakeTransaction
import com.rendly.app.data.model.Usuario
import com.rendly.app.ui.theme.*

/**
 * Modal que aparece al RECEPTOR cuando alguien inicia un handshake
 * Opciones: Aceptar, Rechazar
 */
@Composable
fun HandshakeProposalModal(
    handshake: HandshakeTransaction,
    initiatorUser: Usuario?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { if (!isLoading) onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .widthIn(max = 320.dp)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(20.dp),
            color = HomeBg,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar + icono
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = initiatorUser?.avatarUrl 
                            ?: "https://ui-avatars.com/api/?name=User&background=2D3748&color=fff",
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF22C55E).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Propuesta de @${initiatorUser?.username ?: "usuario"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Detalles compactos
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = handshake.productDescription,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$${String.format("%.0f", handshake.agreedPrice)}",
                            fontSize = 22.sp,
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isLoading = true
                            onReject()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF5350)
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", fontSize = 14.sp)
                    }
                    
                    Button(
                        onClick = {
                            isLoading = true
                            onAccept()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aceptar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Estado del banner de handshake
 */
enum class HandshakeBannerState {
    WAITING,    // Esperando que el otro acepte
    ACCEPTED,   // Ambos aceptaron, esperando confirmaciones
    COMPLETED,  // Transacción completada
    CANCELLED   // Acuerdo cancelado (muestra penalización)
}

/**
 * Banner dinámico que muestra diferentes estados del handshake
 * Estados: WAITING → ACCEPTED → COMPLETED
 */
@Composable
fun HandshakeActiveBanner(
    handshake: HandshakeTransaction?,
    currentUserId: String,
    otherUserName: String,
    bannerState: HandshakeBannerState,
    currentUserReputation: Double = 50.0, // Tu reputación actual
    reputationChange: Int = 0, // +3, +4, -1, -5, etc.
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (handshake == null) return
    
    val isInitiator = handshake.initiatorId == currentUserId
    val canConfirm = handshake.canConfirm(currentUserId)
    val myConfirmed = if (isInitiator) handshake.initiatorConfirmed else handshake.receiverConfirmed
    val otherConfirmed = if (isInitiator) handshake.receiverConfirmed else handshake.initiatorConfirmed
    
    // Colores según estado
    val bannerColor = when (bannerState) {
        HandshakeBannerState.WAITING -> PrimaryPurple.copy(alpha = 0.12f)
        HandshakeBannerState.ACCEPTED -> Color(0xFF22C55E).copy(alpha = 0.12f)
        HandshakeBannerState.COMPLETED -> Color(0xFF22C55E).copy(alpha = 0.18f)
        HandshakeBannerState.CANCELLED -> Color(0xFFEF5350).copy(alpha = 0.12f)
    }
    val accentColor = when (bannerState) {
        HandshakeBannerState.WAITING -> PrimaryPurple
        else -> Color(0xFF22C55E)
    }
    
    // Animación de rotación para el estado WAITING
    val infiniteTransition = rememberInfiniteTransition(label = "waiting")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    AnimatedContent(
        targetState = bannerState,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
            fadeOut(animationSpec = tween(200))
        },
        label = "bannerStateTransition"
    ) { state ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = bannerColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state) {
                    HandshakeBannerState.WAITING -> {
                        // Estado: Esperando aceptación
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotation),
                                color = PrimaryPurple,
                                strokeWidth = 2.dp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Esperando a @$otherUserName",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = handshake.productDescription,
                                fontSize = 12.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Text(
                            text = "$${String.format("%.0f", handshake.agreedPrice)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Botón cancelar
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onCancel() },
                            color = Color(0xFFEF5350).copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    
                    HandshakeBannerState.ACCEPTED -> {
                        // Estado: Aceptado - diseño limpio y profesional
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            // Primera línea: [Aceptado] [Tú] | [User]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Aceptado",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF22C55E)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Confirmaciones: Tú | User
                                if (myConfirmed) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = "Tú",
                                    fontSize = 11.sp,
                                    color = if (myConfirmed) Color(0xFF22C55E) else TextMuted,
                                    fontWeight = if (myConfirmed) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(" | ", fontSize = 11.sp, color = TextMuted)
                                if (otherConfirmed) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF22C55E), modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = otherUserName,
                                    fontSize = 11.sp,
                                    color = if (otherConfirmed) Color(0xFF22C55E) else TextMuted,
                                    fontWeight = if (otherConfirmed) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            // Segunda línea: artículo
                            Text(
                                text = handshake.productDescription,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        // Botón cancelar
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onCancel() },
                            color = Color(0xFFEF5350).copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Botón de confirmación
                        Surface(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .then(
                                    if (canConfirm) Modifier.clickable { onConfirm() } else Modifier
                                ),
                            color = if (canConfirm) Color(0xFF22C55E) else Color(0xFF22C55E).copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Confirmar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (canConfirm) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Confirmar",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    HandshakeBannerState.COMPLETED -> {
                        // Estado: Completado - título a la izquierda, badge reputación a la derecha
                        
                        // Animación del número de reputación
                        val targetReputation = (currentUserReputation + reputationChange).toInt().coerceIn(0, 100)
                        val animatedReputation by animateIntAsState(
                            targetValue = targetReputation,
                            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                            label = "reputationAnim"
                        )
                        
                        // Animación de escala para el badge
                        val badgeScale by animateFloatAsState(
                            targetValue = if (reputationChange != 0) 1.15f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "badgeScale"
                        )
                        
                        val reputationColor = when {
                            animatedReputation >= 90 -> Color(0xFF22C55E)
                            animatedReputation >= 70 -> Color(0xFFFFA726)
                            else -> Color(0xFFEF5350)
                        }
                        
                        // Icono de check
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF22C55E).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Contenido central con título y detalles
                        Column(modifier = Modifier.weight(1f)) {
                            // Título a la izquierda
                            Text(
                                text = "¡Transacción completada!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Username + artículo
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@$otherUserName",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(" · ", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    text = handshake.productDescription,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Badge de reputación alineado a la derecha con animación
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = reputationColor.copy(alpha = 0.15f),
                            modifier = Modifier
                                .align(Alignment.Top)
                                .graphicsLayer {
                                    scaleX = badgeScale
                                    scaleY = badgeScale
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Tu reputación",
                                    tint = reputationColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$animatedReputation%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = reputationColor
                                )
                            }
                        }
                    }
                    
                    HandshakeBannerState.CANCELLED -> {
                        // Estado: Cancelado - muestra penalización de reputación
                        val penaltyAmount = if (reputationChange < 0) reputationChange else -2
                        val targetReputation = (currentUserReputation + penaltyAmount).toInt().coerceIn(0, 100)
                        val animatedReputation by animateIntAsState(
                            targetValue = targetReputation,
                            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                            label = "cancelledReputationAnim"
                        )
                        
                        // Icono de cancelación
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFEF5350).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Texto de cancelación
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acuerdo cancelado",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF5350)
                            )
                            Text(
                                text = handshake.productDescription,
                                fontSize = 12.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Badge de reputación disminuyendo
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF5350).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Tu reputación",
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$animatedReputation%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF5350)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mensaje especial de handshake en el chat
 */
@Composable
fun HandshakeMessageBubble(
    handshake: HandshakeTransaction,
    isFromMe: Boolean,
    onViewDetails: () -> Unit
) {
    val bubbleColor = if (isFromMe) Color(0xFF2D3748) else Color(0xFF1E2732)
    
    Surface(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(16.dp),
        color = bubbleColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF22C55E).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SwapHoriz,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFromMe) "Propuesta enviada" else "Nueva propuesta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22C55E)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Producto
            Text(
                text = handshake.productDescription,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Precio
            Text(
                text = "$${String.format("%.2f", handshake.agreedPrice)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22C55E)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Estado
            Text(
                text = when (handshake.status) {
                    "PROPOSED" -> "⏳ Esperando respuesta"
                    "ACCEPTED" -> "✅ Aceptado"
                    "REJECTED" -> "❌ Rechazado"
                    "COMPLETED" -> "🎉 Completado"
                    else -> handshake.status
                },
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Modal de confirmación al cancelar una transacción
 */
@Composable
fun CancelConfirmationModal(
    productDescription: String,
    onConfirmCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .widthIn(max = 300.dp)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(16.dp),
            color = HomeBg
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFEF5350).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "¿Cancelar transacción?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = productDescription,
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF5350).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "⚠️ Si cancelas, tu reputación podría verse afectada negativamente.",
                        fontSize = 12.sp,
                        color = Color(0xFFEF5350),
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Volver", fontSize = 14.sp)
                    }
                    
                    Button(
                        onClick = onConfirmCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF5350)
                        )
                    ) {
                        Text("Cancelar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Modal de transacción completada - aparece cuando ambos confirman
 */
@Composable
fun TransactionCompletedModal(
    productDescription: String,
    price: Double,
    otherUserName: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .widthIn(max = 300.dp)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(16.dp),
            color = HomeBg
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de éxito con animación
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF22C55E).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎉", fontSize = 28.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "¡Transacción completada!",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22C55E)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Resumen compacto
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = productDescription,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "con @$otherUserName",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = "$${String.format("%.0f", price)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Tu reputación se actualizará en breve",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón cerrar
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    color = Surface
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
