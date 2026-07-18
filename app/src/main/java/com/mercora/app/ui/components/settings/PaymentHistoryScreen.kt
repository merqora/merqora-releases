package com.mercora.app.ui.components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

@Serializable
private data class DisbursementRow(
    val id: String = "",
    val order_id: String = "",
    val seller_id: String = "",
    val amount: Double = 0.0,
    val mp_payment_id: String? = null,
    val status: String = "pending",
    val seller_mp_email: String? = null,
    val created_at: String = ""
)

private data class PayoutItem(
    val id: String,
    val date: String,
    val amount: Double,
    val status: String,
    val method: String,
    val reference: String
)

private data class PayoutGroup(
    val title: String,
    val items: List<PayoutItem>
)

private fun groupPayouts(payouts: List<PayoutItem>): List<PayoutGroup> {
    if (payouts.isEmpty()) return emptyList()
    val today = payouts.first().date.substring(0, 10)
    val groups = mutableListOf<PayoutGroup>()

    val hoy = payouts.filter { it.date.startsWith(today) }
    if (hoy.isNotEmpty()) groups.add(PayoutGroup("Hoy", hoy))

    val estaSemana = payouts.filter {
        val d = it.date.substring(0, 10)
        d >= getDateDaysAgo(today, 7) && d < today
    }
    if (estaSemana.isNotEmpty()) groups.add(PayoutGroup("Esta semana", estaSemana))

    val esteMes = payouts.filter {
        val d = it.date.substring(0, 10)
        d >= getDateDaysAgo(today, 30) && d < getDateDaysAgo(today, 7)
    }
    if (esteMes.isNotEmpty()) groups.add(PayoutGroup("Este mes", esteMes))

    val anteriores = payouts.filter {
        it.date.substring(0, 10) < getDateDaysAgo(today, 30)
    }
    if (anteriores.isNotEmpty()) groups.add(PayoutGroup("Anteriores", anteriores))

    return groups
}

private fun getDateDaysAgo(from: String, days: Int): String {
    val parts = from.split("-")
    if (parts.size != 3) return from
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val day = parts[2].toInt()
    val cal = java.util.Calendar.getInstance()
    cal.set(year, month - 1, day)
    cal.add(java.util.Calendar.DAY_OF_MONTH, -days)
    return String.format("%04d-%02d-%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
}

private fun mapStatus(status: String): String = when (status.lowercase()) {
    "completed", "approved", "paid" -> "Completado"
    "pending", "in_process" -> "Pendiente"
    "processing" -> "Procesando"
    "rejected", "cancelled", "failed" -> "Rechazado"
    else -> status
}

private fun statusColor(status: String): Color = when (mapStatus(status)) {
    "Completado" -> AccentGreen
    "Pendiente" -> AccentGold
    "Procesando" -> Color(0xFF6C63FF)
    "Rechazado" -> Color(0xFFEF4444)
    else -> TextMuted
}

@Composable
fun PaymentHistoryScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    if (!isVisible && slideOffset == 1f) return

    val scope = rememberCoroutineScope()
    var payouts by remember { mutableStateOf<List<PayoutItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            try {
                val userId = com.vinzay.app.data.remote.SessionPersistence.getUserId()
                if (userId != null) {
                    val response = SupabaseClient.database
                        .from("disbursements")
                        .select {
                            filter { eq("seller_id", userId) }
                            limit(50)
                        }
                        .decodeList<DisbursementRow>()

                    payouts = response.map { row ->
                        PayoutItem(
                            id = row.mp_payment_id ?: row.id,
                            date = row.created_at,
                            amount = row.amount,
                            status = mapStatus(row.status),
                            method = "Mercado Pago",
                            reference = "Venta #${row.order_id.take(8)}"
                        )
                    }
                }
            } catch (e: Exception) {
                payouts = emptyList()
            }
            isLoading = false
        }
    }

    val groups = remember(payouts) { groupPayouts(payouts) }
    val totalAmount = remember(payouts) { payouts.sumOf { it.amount } }
    val completedAmount = remember(payouts) { payouts.filter { it.status == "Completado" }.sumOf { it.amount } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(enabled = slideOffset == 0f) { onDismiss() }
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
                SettingsScreenHeader(
                    title = "Historial de pagos",
                    subtitle = "Todos los pagos y cobros recibidos",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    iconColor = AccentGreen,
                    onBack = onDismiss
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentGreen, modifier = Modifier.size(32.dp))
                    }
                } else if (payouts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Sin pagos aÃºn", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                            Text("Los pagos que recibas aparecerÃ¡n aquÃ­", fontSize = 12.sp, color = TextMuted.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Surface
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Resumen de cobros", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("$${String.format("%,.0f", completedAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("Total cobrado", fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(BorderSubtle.copy(alpha = 0.5f)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(AccentGold.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Outlined.Receipt, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("${payouts.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("Pagos recibidos", fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(BorderSubtle.copy(alpha = 0.5f)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(if (totalAmount - completedAmount > 0) AccentGold.copy(alpha = 0.12f) else AccentGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = if (totalAmount - completedAmount > 0) AccentGold else AccentGreen, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("$${String.format("%,.0f", totalAmount - completedAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (totalAmount - completedAmount > 0) AccentGold else TextPrimary)
                                            Text("Pendiente", fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center)
                                        }
                                    }
                                    if (totalAmount - completedAmount > 0) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(shape = RoundedCornerShape(10.dp), color = AccentGold.copy(alpha = 0.08f)) {
                                            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Outlined.Info, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("$${String.format("%,.0f", totalAmount - completedAmount)} pendientes de cobro", fontSize = 11.sp, color = AccentGold, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "MOVIMIENTOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }

                        groups.forEach { group ->
                            item {
                                Text(
                                    text = group.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            items(group.items, key = { it.id }) { payout ->
                                PayoutCard(payout = payout)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayoutCard(payout: PayoutItem) {
    val sc = statusColor(payout.status)
    val statusIcon = when (payout.status) {
        "Completado" -> Icons.Filled.CheckCircle
        "Pendiente" -> Icons.Outlined.HourglassEmpty
        "Procesando" -> Icons.Outlined.Sync
        "Rechazado" -> Icons.Outlined.Cancel
        else -> Icons.Outlined.HelpOutline
    }
    val methodIcon = when {
        payout.method.contains("Mercado Pago") -> Icons.Outlined.AccountBalance
        payout.method.contains("Transferencia") -> Icons.Outlined.AccountBalance
        payout.method.contains("Tarjeta") -> Icons.Outlined.CreditCard
        else -> Icons.Outlined.Payment
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(sc.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = methodIcon, contentDescription = null, tint = sc, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = payout.reference, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = payout.method, fontSize = 11.sp, color = TextMuted)
                        Text(text = " Â· ", fontSize = 11.sp, color = TextMuted)
                        Text(text = payout.date.substring(0, 10), fontSize = 11.sp, color = TextMuted)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%,.0f", payout.amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (payout.status == "Completado") AccentGreen else TextPrimary
                    )
                    Surface(shape = RoundedCornerShape(8.dp), color = sc.copy(alpha = 0.12f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(imageVector = statusIcon, contentDescription = null, tint = sc, modifier = Modifier.size(10.dp))
                            Text(text = payout.status, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = sc)
                        }
                    }
                }
            }
        }
    }
}
