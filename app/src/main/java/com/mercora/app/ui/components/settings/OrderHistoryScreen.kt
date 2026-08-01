package com.mercora.app.ui.components.settings

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mercora.app.data.model.Order
import com.mercora.app.data.model.OrderStatus
import com.mercora.app.data.repository.OrderRepository
import com.mercora.app.data.repository.TransactionsSummary
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch

private enum class OrderFilter(val label: String, val icon: ImageVector) {
    ALL("Todos", Icons.Outlined.ReceiptLong),
    PURCHASES("Compras", Icons.Outlined.ShoppingCart),
    SALES("Ventas", Icons.Outlined.TrendingUp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderHistoryScreen(
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
    val purchases by OrderRepository.cachedPurchases.collectAsState()
    val sales by OrderRepository.cachedSales.collectAsState()
    val summary by OrderRepository.cachedSummary.collectAsState()
    val isRefreshing by OrderRepository.isRefreshing.collectAsState()
    val currentUserId = remember { SupabaseClient.auth.currentUserOrNull()?.id }

    var filter by remember { mutableStateOf(OrderFilter.ALL) }
    var confirmDeleteOrder by remember { mutableStateOf<Order?>(null) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val allOrders = when (filter) {
        OrderFilter.ALL -> (purchases + sales).sortedByDescending { it.createdAt }
        OrderFilter.PURCHASES -> purchases.sortedByDescending { it.createdAt }
        OrderFilter.SALES -> sales.sortedByDescending { it.createdAt }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) OrderRepository.loadTransactionsWithCache()
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
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Mis Pedidos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Historial de compras y ventas", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryPurple)
                    } else {
                        IconButton(onClick = {
                            scope.launch { OrderRepository.loadTransactionsWithCache(forceRefresh = true) }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = TextPrimary)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary card
                    item { SummaryHeader(summary) }

                    // Filter tabs
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OrderFilter.entries.forEach { f ->
                                val selected = f == filter
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { filter = f },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) PrimaryPurple.copy(alpha = 0.12f) else Surface,
                                    border = if (selected) BorderStroke(1.5.dp, PrimaryPurple) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(f.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (selected) PrimaryPurple else TextMuted)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            f.label,
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) PrimaryPurple else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Empty state
                    if (allOrders.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        when (filter) {
                                            OrderFilter.ALL -> Icons.Outlined.ReceiptLong
                                            OrderFilter.PURCHASES -> Icons.Outlined.ShoppingCart
                                            OrderFilter.SALES -> Icons.Outlined.TrendingUp
                                        },
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        when (filter) {
                                            OrderFilter.ALL -> "No hay pedidos aún"
                                            OrderFilter.PURCHASES -> "No hay compras aún"
                                            OrderFilter.SALES -> "No hay ventas aún"
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted
                                    )
                                    Text(
                                        when (filter) {
                                            OrderFilter.ALL -> "Tus pedidos aparecerán aquí"
                                            OrderFilter.PURCHASES -> "Los productos que compres aparecerán aquí"
                                            OrderFilter.SALES -> "Cuando te compren aparecerá aquí"
                                        },
                                        fontSize = 12.sp,
                                        color = TextMuted.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Order list
                    items(
                        items = allOrders,
                        key = { it.id }
                    ) { order ->
                        val isSale = order.buyerId != currentUserId
                        EnhancedOrderCard(
                            order = order,
                            isSale = isSale,
                            onClick = { selectedOrder = order },
                            onLongClick = { confirmDeleteOrder = order }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Delete confirmation dialog
        if (confirmDeleteOrder != null) {
            val orderToDelete = confirmDeleteOrder!!
            AlertDialog(
                onDismissRequest = { confirmDeleteOrder = null },
                icon = { Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp)) },
                title = { Text("Eliminar pedido", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("¿Eliminar definitivamente el pedido #${orderToDelete.orderNumber}? Esta acción no se puede deshacer.")
                        if (deleteError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(deleteError!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isDeleting) return@Button
                            isDeleting = true
                            deleteError = null
                            scope.launch {
                                val result = OrderRepository.deleteOrder(orderToDelete.id)
                                isDeleting = false
                                result.fold(
                                    onSuccess = { confirmDeleteOrder = null },
                                    onFailure = { e ->
                                        deleteError = e.message ?: "Error al eliminar"
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        enabled = !isDeleting
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Eliminar")
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { if (!isDeleting) confirmDeleteOrder = null },
                        enabled = !isDeleting
                    ) { Text("Cancelar") }
                }
            )
        }

        // Order detail dialog
        if (selectedOrder != null) {
            val order = selectedOrder!!
            val isSale = order.buyerId != currentUserId
            AlertDialog(
                onDismissRequest = { selectedOrder = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = SurfaceElevated,
                modifier = Modifier.padding(16.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pedido #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                            Text(
                                if (isSale) "VENTA · ${order.buyerUsername ?: "Comprador"}" else "COMPRA",
                                fontSize = 12.sp,
                                color = if (isSale) Color(0xFF7C4DFF) else Color(0xFF1565A0),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Status
                        val statusColor = when (order.status) {
                            OrderStatus.COMPLETED, OrderStatus.DELIVERED -> Color(0xFF22C55E)
                            OrderStatus.PAID, OrderStatus.PREPARING -> Color(0xFF3B82F6)
                            OrderStatus.SHIPPED -> Color(0xFFFF6B35)
                            OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
                            OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Color(0xFFEF4444)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Estado: ", fontSize = 13.sp, color = TextMuted)
                            Text(order.statusDisplayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))

                        // Items
                        Text("Productos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        order.items.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (item.imageUrl != null) {
                                        AsyncImage(
                                            model = item.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text("Cant: ${item.quantity} · $${String.format("%,.0f", item.unitPrice)}", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Text("$${String.format("%,.0f", item.totalPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))

                        // Total
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("$${String.format("%,.0f", order.totalAmount)} ${order.currency}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }

                        // Details
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("N° de pedido: #${order.orderNumber}", fontSize = 12.sp, color = TextMuted)
                            Text("Fecha: ${order.formattedDate}", fontSize = 12.sp, color = TextMuted)
                            if (order.trackingNumber != null) {
                                Text("Tracking: ${order.trackingNumber}", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                },
                confirmButton = {
                    OutlinedButton(onClick = { selectedOrder = null }) {
                        Text("Cerrar", color = PrimaryPurple)
                    }
                }
            )
        }
    }
}

@Composable
private fun SummaryHeader(summary: TransactionsSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E))),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.ShoppingCart,
                    iconColor = Color(0xFF4FC3F7),
                    label = "Compras",
                    value = summary.totalPurchases.toString(),
                    amount = summary.totalPurchasesAmount,
                    pending = summary.pendingPurchases
                )
                StatDivider()
                StatItem(
                    icon = Icons.Outlined.TrendingUp,
                    iconColor = Color(0xFF81C784),
                    label = "Ventas",
                    value = summary.totalSales.toString(),
                    amount = summary.totalSalesAmount,
                    pending = summary.pendingSales
                )
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, iconColor: Color, label: String, value: String, amount: Double, pending: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp)) }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("$${String.format("%,.0f", amount)}", fontSize = 13.sp, color = iconColor)
        if (pending > 0) {
            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFFA726).copy(alpha = 0.2f)) {
                Text(
                    "$pending pendiente${if (pending != 1) "s" else ""}",
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA726),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(60.dp).background(Color.White.copy(alpha = 0.1f)))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EnhancedOrderCard(
    order: Order,
    isSale: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val statusColor = when (order.status) {
        OrderStatus.COMPLETED, OrderStatus.DELIVERED -> Color(0xFF22C55E)
        OrderStatus.PAID, OrderStatus.PREPARING -> Color(0xFF3B82F6)
        OrderStatus.SHIPPED -> Color(0xFFFF6B35)
        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Color(0xFFEF4444)
    }

    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: badge + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSale) Color(0xFF7C4DFF).copy(alpha = 0.12f) else Color(0xFF1565A0).copy(alpha = 0.12f)
                ) {
                    Text(
                        if (isSale) "VENTA" else "COMPRA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSale) Color(0xFF7C4DFF) else Color(0xFF1565A0),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.12f)) {
                    Text(
                        order.statusDisplayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Items row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product images
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    order.items.take(3).forEachIndexed { _, item ->
                        if (item.imageUrl != null) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, HomeBg, RoundedCornerShape(8.dp))
                            )
                        }
                    }
                    if (order.items.size > 3) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(BorderSubtle)
                                .border(2.dp, HomeBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("+${order.items.size - 3}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary) }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSale) "Para: ${order.buyerUsername ?: "Comprador"}" else order.items.firstOrNull()?.title ?: "Producto",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "#${order.orderNumber} · ${order.formattedDate}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: total + delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$${String.format("%,.0f", order.totalAmount)} ${order.currency}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Context menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Eliminar pedido", color = Color(0xFFEF4444))
                            } },
                            onClick = {
                                showMenu = false
                                onLongClick()
                            }
                        )
                    }
                }
            }

            // Expandable detail if toggled
            AnimatedVisibility(visible = showMenu) {
                // Actually we don't want to show expanded detail when menu is open
                // So we just do nothing here
            }
        }
    }
}
