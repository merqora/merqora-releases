package com.vinzay.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinzay.app.data.repository.WalletRepository
import com.vinzay.app.data.remote.SupabaseClient
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

@Composable
fun WalletScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onRequestWithdraw: () -> Unit = {}
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    if (!isVisible && slideOffset == 1f) return

    val scope = rememberCoroutineScope()
    val balance by WalletRepository.walletBalance.collectAsState()
    val transactions by WalletRepository.transactions.collectAsState()
    val isRefreshing by WalletRepository.isRefreshing.collectAsState()

    var showWithdrawScreen by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
            WalletRepository.getWallet(userId)
            WalletRepository.loadTransactions(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp),
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Mi Billetera", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Saldo disponible para retirar", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Saldo card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0A3D62), Color(0xFF1A5276))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Saldo disponible",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "$${String.format("%,.0f", balance)}",
                                    color = Color.White,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "UYU",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = onRequestWithdraw,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF0A3D62),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Retirar dinero",
                                        color = Color(0xFF0A3D62),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Historial de transacciones
                    Text(
                        text = "Historial de movimientos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Sin movimientos aún",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Los pagos que recibas aparecerán aquí",
                                    color = TextMuted.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        transactions.forEach { tx ->
                            TransactionItem(tx)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        WithdrawScreen(
            isVisible = showWithdrawScreen,
            onDismiss = { showWithdrawScreen = false },
            onSuccess = { showWithdrawScreen = false }
        )
    }
}

@Composable
private fun TransactionItem(tx: JsonObject) {
    val type = tx["type"]?.toString()?.trim('"') ?: ""
    val amountStr = tx["amount"]?.toString()?.trim('"') ?: "0"
    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val description = tx["description"]?.toString()?.trim('"') ?: ""
    val createdAt = tx["created_at"]?.toString()?.trim('"') ?: ""
    val status = tx["status"]?.toString()?.trim('"') ?: "completed"

    val isCredit = type == "credit"
    val icon = if (isCredit) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward
    val iconColor = if (isCredit) Color(0xFF22C55E) else Color(0xFFEF4444)
    val sign = if (isCredit) "+" else "-"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = description.ifEmpty { if (isCredit) "Pago recibido" else "Retiro" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = createdAt.take(10),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign$${String.format("%,.0f", amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) Color(0xFF22C55E) else Color(0xFFEF4444)
                )
                if (status != "completed") {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFA726).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (status) { "pending" -> "Pendiente" else -> status },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFA726),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
