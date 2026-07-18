package com.vinzay.app.ui.components.settings

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.vinzay.app.data.model.Order
import com.vinzay.app.data.model.OrderStatus
import com.vinzay.app.data.repository.OrderRepository
import com.vinzay.app.data.remote.SupabaseClient
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun EarningsScreen(
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
    val summary by OrderRepository.cachedSummary.collectAsState()
    val sales by OrderRepository.cachedSales.collectAsState()
    val isRefreshing by OrderRepository.isRefreshing.collectAsState()
    val isCacheLoaded by OrderRepository.isCacheLoaded.collectAsState()

    // Cargar datos al abrir
    LaunchedEffect(isVisible) {
        if (isVisible) {
            OrderRepository.loadTransactionsWithCache()
        }
    }

    var showPayoutConfig by remember { mutableStateOf(false) }
    var showPayoutMethods by remember { mutableStateOf(false) }
    var showPaymentHistory by remember { mutableStateOf(false) }
    var showBilling by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedPayoutMethod by remember { mutableStateOf("mercadopago") }
    var selectedSale by remember { mutableStateOf<Order?>(null) }

    // Compute earnings data
    val totalRevenue = remember(sales) { sales.sumOf { it.items.sumOf { it.totalPrice } } }
    val completedRevenue = remember(sales) {
        sales.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.COMPLETED || it.status == OrderStatus.DELIVERED }
            .sumOf { it.items.sumOf { it.totalPrice } }
    }
    val pendingCount = remember(sales) {
        sales.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAYMENT_PROCESSING }
    }

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
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Mis Ganancias",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Administra tus cobros y pagos",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (!isCacheLoaded && sales.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentGreen, modifier = Modifier.size(32.dp))
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
                            EarningsSummaryCard(
                                totalSales = sales.size,
                                totalRevenue = totalRevenue,
                                completedRevenue = completedRevenue,
                                pendingCount = pendingCount
                            )
                        }

                        // Botón de retirar
                        item {
                            WithdrawButton(
                                availableAmount = completedRevenue,
                                onWithdraw = { showWithdrawDialog = true }
                            )
                        }

                        // Acciones rápidas
                        item {
                            Text(
                                text = "ACCIONES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }

                        item {
                            EarningsActionCard(
                                icon = Icons.Outlined.AccountBalanceWallet,
                                iconColor = Color(0xFF2E8B57),
                                title = "Configurar método de cobro",
                                subtitle = if (selectedPayoutMethod == "mercadopago") "Mercado Pago" else "Transferencia bancaria",
                                onClick = { showPayoutMethods = true }
                            )
                        }

                        item {
                            EarningsActionCard(
                                icon = Icons.Outlined.History,
                                iconColor = Color(0xFF6C63FF),
                                title = "Historial de pagos",
                                subtitle = "Pagos recibidos y retiros realizados",
                                onClick = { showPaymentHistory = true }
                            )
                        }

                        item {
                            EarningsActionCard(
                                icon = Icons.Outlined.Receipt,
                                iconColor = Color(0xFFFF6B35),
                                title = "Facturación",
                                subtitle = "Descarga tus facturas y comprobantes",
                                onClick = { showBilling = true }
                            )
                        }

                        // Todas las ventas
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "VENTAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }

                        if (sales.isEmpty()) {
                            item {
                                EmptyStateCard(message = "Aún no tienes ventas registradas")
                            }
                        } else {
                            items(sales.sortedByDescending { it.createdAt }.take(20), key = { it.id }) { sale ->
                                SaleItemCard(
                                    sale = sale,
                                    onClick = { selectedSale = sale }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // Payout config overlay
        if (showPayoutConfig) {
            PayoutConfigOverlay(
                selectedMethod = selectedPayoutMethod,
                onSelect = { selectedPayoutMethod = it },
                onDismiss = { showPayoutConfig = false }
            )
        }

        // Withdraw dialog
        if (showWithdrawDialog) {
            WithdrawDialog(
                availableAmount = completedRevenue,
                onDismiss = { showWithdrawDialog = false },
                onConfirm = { amount ->
                    showWithdrawDialog = false
                    // TODO: implement withdrawal via MP API
                }
            )
        }

        // Payout methods screen
        PayoutMethodsScreen(
            isVisible = showPayoutMethods,
            onDismiss = { showPayoutMethods = false }
        )

        // Payment history screen
        PaymentHistoryScreen(
            isVisible = showPaymentHistory,
            onDismiss = { showPaymentHistory = false }
        )

        // Billing screen
        BillingScreen(
            isVisible = showBilling,
            onDismiss = { showBilling = false }
        )

        // Sale detail view
        selectedSale?.let { sale ->
            SaleDetailView(
                sale = sale,
                onDismiss = { selectedSale = null }
            )
        }
    }
}

@Composable
private fun EarningsSummaryCard(
    totalSales: Int,
    totalRevenue: Double,
    completedRevenue: Double,
    pendingCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Balance de ganancias",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$totalSales ventas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$${String.format("%.0f", totalRevenue)}",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Ingresos totales (incluye ventas pendientes)",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${String.format("%.0f", completedRevenue)} cobrados",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentGreen
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EarningBar(
                    modifier = Modifier.weight(1f),
                    label = "Cobrado",
                    amount = completedRevenue,
                    color = AccentGreen,
                    subtitle = "Disponible para retirar"
                )
                EarningBar(
                    modifier = Modifier.weight(1f),
                    label = "Pendiente",
                    amount = totalRevenue - completedRevenue,
                    color = Color(0xFFFFA726),
                    subtitle = "$pendingCount ventas por pagar"
                )
            }
        }
    }
}

@Composable
private fun EarningBar(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    color: Color,
    subtitle: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.06f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${String.format("%.0f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun WithdrawButton(
    availableAmount: Double,
    onWithdraw: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AccentGreen.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onWithdraw)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CallMade,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Retirar ganancias",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen
                )
                Text(
                    text = "$${String.format("%.0f", availableAmount)} disponibles",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EarningsActionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SaleItemCard(sale: Order, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2E8B57).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    tint = Color(0xFF2E8B57),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sale.items.firstOrNull()?.title ?: "Producto #${sale.orderNumber.take(8)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = sale.formattedDate,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+$${String.format("%.0f", sale.totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (sale.status) {
                        OrderStatus.PAID, OrderStatus.COMPLETED, OrderStatus.DELIVERED -> AccentGreen
                        else -> Color(0xFFFFA726)
                    }
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (sale.status) {
                        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726).copy(alpha = 0.12f)
                        OrderStatus.PAID, OrderStatus.COMPLETED, OrderStatus.DELIVERED -> AccentGreen.copy(alpha = 0.12f)
                        else -> TextMuted.copy(alpha = 0.12f)
                    }
                ) {
                    Text(
                        text = sale.statusDisplayName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (sale.status) {
                            OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
                            OrderStatus.PAID, OrderStatus.COMPLETED, OrderStatus.DELIVERED -> AccentGreen
                            else -> TextMuted
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WithdrawDialog(
    availableAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var withdrawAmount by remember { mutableStateOf(availableAmount) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            color = Surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CallMade,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Retirar ganancias",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "El dinero se transferirá a tu método de cobro configurado",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Amount display
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = HomeBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Disponible",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "$${String.format("%.0f", availableAmount)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onConfirm(withdrawAmount) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(14.dp),
                    enabled = withdrawAmount > 0
                ) {
                    Text(
                        text = "Solicitar retiro",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun SaleDetailView(
    sale: Order,
    onDismiss: () -> Unit
) {
    val statusColor = when (sale.status) {
        OrderStatus.PAID, OrderStatus.COMPLETED, OrderStatus.DELIVERED -> AccentGreen
        OrderStatus.PREPARING -> Color(0xFF1565A0)
        OrderStatus.SHIPPED -> Color(0xFFFF6B35)
        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 0.dp),
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
                        Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Detalle de venta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(sale.orderNumber, fontSize = 12.sp, color = TextSecondary)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = sale.statusDisplayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Status banner
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = statusColor.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                        .background(statusColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("+$${String.format("%,.0f", sale.totalAmount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                    Text(sale.formattedDate, fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    // Productos
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Productos (${sale.items.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                sale.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = item.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("x${item.quantity} — $${String.format("%,.0f", item.totalPrice)}", fontSize = 12.sp, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Resumen
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Resumen de pago", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Subtotal", fontSize = 13.sp, color = TextSecondary)
                                    Text("$${String.format("%,.0f", sale.subtotal)}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Envío", fontSize = 13.sp, color = TextSecondary)
                                    Text(if (sale.shippingCost == 0.0) "GRATIS" else "$${String.format("%,.0f", sale.shippingCost)}",
                                        fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (sale.shippingCost == 0.0) AccentGreen else TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = BorderSubtle)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("$${String.format("%,.0f", sale.totalAmount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                                }
                            }
                        }
                    }

                    // Buyer info
                    if (sale.buyerUsername != null) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Surface
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Comprador", fontSize = 12.sp, color = TextMuted)
                                        Text("@${sale.buyerUsername}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PayoutConfigOverlay(
    selectedMethod: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextMuted.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Método de cobro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Seleccioná cómo querés recibir tus pagos",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))

                PayoutMethodOption(
                    icon = Icons.Outlined.AccountBalance,
                    name = "Mercado Pago",
                    description = "Recibí el dinero directo en tu cuenta de Mercado Pago",
                    isSelected = selectedMethod == "mercadopago",
                    onClick = { onSelect("mercadopago") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PayoutMethodOption(
                    icon = Icons.Outlined.AccountBalance,
                    name = "Transferencia bancaria",
                    description = "Recibí el dinero en tu cuenta bancaria (Próximamente)",
                    isSelected = selectedMethod == "bank",
                    onClick = { onSelect("bank") },
                    isDisabled = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Guardar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PayoutMethodOption(
    icon: ImageVector,
    name: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDisabled: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) AccentGreen.copy(alpha = 0.06f) else Surface,
        border = if (isSelected) BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AccentGreen.copy(alpha = 0.12f) else TextMuted.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) AccentGreen else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDisabled) TextMuted else TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isDisabled) TextMuted.copy(alpha = 0.5f) else TextSecondary
                )
            }
            if (isDisabled) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TextMuted.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "PRONTO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
