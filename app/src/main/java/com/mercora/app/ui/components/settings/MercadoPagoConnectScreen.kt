package com.mercora.app.ui.components.settings

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.BuildConfig
import com.mercora.app.data.repository.MercadoPagoOAuthRepository
import com.mercora.app.data.repository.MpConnectionState
import com.mercora.app.data.repository.OAuthResult
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MercadoPagoConnectScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConnectionChanged: () -> Unit = {}
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    if (!isVisible && slideOffset == 1f) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val connectionState by MercadoPagoOAuthRepository.connectionState.collectAsState()
    val oauthResult by MercadoPagoOAuthRepository.oauthResult.collectAsState()
    var isProcessing by remember { mutableStateOf(false) }
    var showDisconnectConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
            MercadoPagoOAuthRepository.fetchConnection(userId)
        }
    }

    LaunchedEffect(oauthResult) {
        when (oauthResult) {
            is OAuthResult.Success -> {
                isProcessing = false
                onConnectionChanged()
            }
            is OAuthResult.Error -> {
                isProcessing = false
                errorMessage = (oauthResult as OAuthResult.Error).message
            }
            null -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp),
            color = HomeBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            MercadoPagoOAuthRepository.resetOAuthResult()
                            onDismiss()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            "Mercado Pago",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Conectá tu cuenta para cobrar ventas",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                when (val state = connectionState) {
                    is MpConnectionState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBright, modifier = Modifier.size(32.dp))
                        }
                    }

                    is MpConnectionState.Disconnected -> {
                        DisconnectedContent(
                            onConnect = {
                                isProcessing = true
                                errorMessage = null
                                val clientId = BuildConfig.MERCADOPAGO_CLIENT_ID
                                if (clientId.isBlank()) {
                                    errorMessage = "Configuración de Mercado Pago no disponible"
                                    isProcessing = false
                                    return@DisconnectedContent
                                }
                                val state = java.util.UUID.randomUUID().toString()
                                val url = MercadoPagoOAuthRepository.buildOAuthUrl(clientId, state)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            isProcessing = isProcessing,
                            errorMessage = errorMessage
                        )
                    }

                    is MpConnectionState.Connected -> {
                        ConnectedContent(
                            mpUserId = state.mpUserId,
                            connectedSince = state.connectedSince,
                            onDisconnect = { showDisconnectConfirm = true },
                            errorMessage = errorMessage
                        )
                    }

                    is MpConnectionState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetry = {
                                scope.launch {
                                    val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                                    MercadoPagoOAuthRepository.fetchConnection(userId)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDisconnectConfirm) {
            DisconnectConfirmDialog(
                onConfirm = {
                    showDisconnectConfirm = false
                    scope.launch {
                        val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                        MercadoPagoOAuthRepository.disconnectMercadoPago(userId)
                        onConnectionChanged()
                    }
                },
                onDismiss = { showDisconnectConfirm = false }
            )
        }
    }
}

@Composable
private fun DisconnectedContent(
    onConnect: () -> Unit,
    isProcessing: Boolean,
    errorMessage: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF1565A0).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF1565A0)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Conectá tu cuenta de Mercado Pago",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Para recibir los pagos de tus ventas directamente en tu cuenta de Mercado Pago.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                BenefitRow(
                    icon = Icons.Default.FlashOn,
                    title = "Cobros automáticos",
                    subtitle = "Los pagos llegan directo a tu cuenta de MP",
                    color = Color(0xFF22C55E)
                )
                Spacer(modifier = Modifier.height(16.dp))
                BenefitRow(
                    icon = Icons.Default.Security,
                    title = "Seguridad",
                    subtitle = "Mercado Pago protege cada transacción",
                    color = Color(0xFF1565A0)
                )
                Spacer(modifier = Modifier.height(16.dp))
                BenefitRow(
                    icon = Icons.Default.TrendingUp,
                    title = "Sin retenciones",
                    subtitle = "Disponibilidad inmediata del dinero",
                    color = Color(0xFF22C55E)
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, fontSize = 13.sp, color = Color(0xFFEF4444))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConnect,
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1565A0),
                disabledContainerColor = Color(0xFF1565A0).copy(alpha = 0.5f)
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Conectar Mercado Pago", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Al conectarte, aceptás los términos de Mercado Pago.",
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Mercora usa Split Payments de Mercado Pago. Nunca retenemos tu dinero.",
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConnectedContent(
    mpUserId: String,
    connectedSince: String,
    onDisconnect: () -> Unit,
    errorMessage: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = Color(0xFF22C55E)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "¡Mercado Pago conectado!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        val formattedDate = try {
            val instant = Instant.parse(connectedSince)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            instant.atZone(ZoneId.of("America/Montevideo")).format(formatter)
        } catch (_: Exception) {
            connectedSince.take(10)
        }

        Text(
            "Conectado desde $formattedDate",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(
                    label = "ID de cuenta MP",
                    value = mpUserId.ifEmpty { "â€”" }
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    label = "Estado",
                    value = "Activa",
                    valueColor = Color(0xFF22C55E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(
                    label = "Comisión Mercora",
                    value = "10%"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF1565A0),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Los pagos de tus ventas se acreditan automáticamente en tu cuenta de Mercado Pago. Mercora nunca retiene tu dinero.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, fontSize = 13.sp, color = Color(0xFFEF4444))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onDisconnect,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
        ) {
            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Desconectar Mercado Pago", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Error de conexión",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBright)
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun DisconnectConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Desconectar Mercado Pago", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Si desconectás tu cuenta, no vas a poder recibir pagos de nuevas ventas. Las ventas existentes no se ven afectadas.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Desconectar", color = Color(0xFFEF4444))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = Surface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
