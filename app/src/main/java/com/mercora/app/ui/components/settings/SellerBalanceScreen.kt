package com.mercora.app.ui.components.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.model.Order
import com.mercora.app.data.model.OrderStatus
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.OrderRepository
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SellerBalanceScreen(
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
    var sales by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var summary by remember { mutableStateOf<SellerFinancialSummary?>(null) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            try {
                val result = SupabaseClient.database
                    .from("seller_financial_summary")
                    .select {
                        filter { eq("seller_id", SupabaseClient.auth.currentUserOrNull()?.id ?: "") }
                    }
                    .decodeSingleOrNull<SellerFinancialSummary>()
                if (result != null) {
                    summary = result
                }
            } catch (_: Exception) {}

            val mySales = OrderRepository.getMySales()
            sales = mySales
                .filter { it.status == OrderStatus.PAID || it.status == OrderStatus.SHIPPED || it.status == OrderStatus.DELIVERED }
                .sortedByDescending { it.createdAt }
            isLoading = false
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
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            "Balance de Ventas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Tu dinero está en Mercado Pago",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBright, modifier = Modifier.size(32.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        summary?.let { BalanceCard(it) }

                        Spacer(modifier = Modifier.height(20.dp))

                        InfoBanner()

                        Spacer(modifier = Modifier.height(24.dp))

                        SectionTitle("VENTAS RECIENTES")

                        if (sales.isEmpty()) {
                            EmptySales()
                        } else {
                            sales.take(20).forEach { sale ->
                                SaleItem(
                                    order = sale,
                                    formatAmount = { formatCurrency(it) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(summary: SellerFinancialSummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0A3D62), Color(0xFF1A5276))
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text("Total en ventas", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatCurrency(summary.grossSales),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tus cobros se acreditan en Mercado Pago automáticamente",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    label = "Órdenes",
                    value = summary.totalOrders.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Comisiones",
                    value = formatCurrency(summary.totalCommission),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Neto",
                    value = formatCurrency(summary.netEarnings),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Dinero en tu cuenta de Mercado Pago",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            "Revisá tu app de Mercado Pago para ver el saldo disponible",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun InfoBanner() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1565A0).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF1565A0),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Con Split Payments de Mercado Pago, el dinero de tus ventas va directo a tu cuenta de Mercado Pago. Mercora nunca retiene tu dinero. Los montos mostrados acá son informativos.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
private fun EmptySales() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = TextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Sin ventas aún",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SaleItem(
    order: Order,
    formatAmount: (Double) -> String
) {
    val commission = order.items.sumOf { it.totalPrice } * 0.10
    val sellerNet = order.items.sumOf { it.totalPrice } - commission

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        order.items.firstOrNull()?.title ?: "Pedido",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        "#${order.orderNumber}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatAmount(order.totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E)
                    )
                    Text(
                        "Neto: ${formatAmount(sellerNet)}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(order.status)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    order.createdAt.take(10),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (order.splitInfo != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1565A0).copy(alpha = 0.08f)
                ) {
                    Text(
                        "Comisión Mercora: ${formatCurrency(commission)} (10%) - MP ya desembolsó",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = Color(0xFF1565A0)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: OrderStatus) {
    val (text, color) = when (status) {
        OrderStatus.PAID -> "Pagado" to Color(0xFF22C55E)
        OrderStatus.SHIPPED -> "Enviado" to Color(0xFF1565A0)
        OrderStatus.DELIVERED -> "Entregado" to Color(0xFF22C55E)
        OrderStatus.PENDING -> "Pendiente" to Color(0xFFFFA726)
        OrderStatus.PAYMENT_PROCESSING -> "Procesando" to Color(0xFFFFA726)
        OrderStatus.CANCELLED -> "Cancelado" to Color(0xFFEF4444)
        OrderStatus.REFUNDED -> "Reembolsado" to Color(0xFFEF4444)
        else -> "â€”" to TextMuted
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "UY"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

data class SellerFinancialSummary(
    val sellerId: String = "",
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val grossSales: Double = 0.0,
    val totalCommission: Double = 0.0,
    val netEarnings: Double = 0.0,
    val pendingClearance: Double = 0.0,
    val clearedForPayment: Double = 0.0
)
