package com.rendly.app.ui.components.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Order
import com.rendly.app.data.model.OrderStatus
import com.rendly.app.data.repository.OrderRepository
import com.rendly.app.data.repository.TransactionsSummary
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MIS TRANSACCIONES - Pantalla completa de historial de compras y ventas
 * ═══════════════════════════════════════════════════════════════════════════════
 */
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
    
    // Estados desde cache
    val purchases by OrderRepository.cachedPurchases.collectAsState()
    val sales by OrderRepository.cachedSales.collectAsState()
    val summary by OrderRepository.cachedSummary.collectAsState()
    val isCacheLoaded by OrderRepository.isCacheLoaded.collectAsState()
    val isRefreshing by OrderRepository.isRefreshing.collectAsState()
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    
    // Loading = primera carga sin cache
    val isLoading = !isCacheLoaded && isRefreshing
    
    // Pager state para tabs
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    // Cargar datos con cache
    LaunchedEffect(isVisible) {
        if (isVisible) {
            OrderRepository.loadTransactionsWithCache()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
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
                SettingsScreenHeader(
                    title = "Mis Transacciones",
                    subtitle = "Historial de compras y ventas",
                    icon = Icons.Outlined.Receipt,
                    iconColor = Color(0xFFFF6B35),
                    onBack = onDismiss
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else {
                    // Resumen de estadísticas
                    TransactionsSummaryCard(summary = summary)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tabs
                    TransactionsTabs(
                        selectedTab = pagerState.currentPage,
                        purchasesCount = purchases.size,
                        salesCount = sales.size,
                        onTabSelected = { tab ->
                            scope.launch { pagerState.animateScrollToPage(tab) }
                        }
                    )
                    
                    // Contenido con pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> TransactionsList(
                                orders = purchases,
                                emptyIcon = Icons.Outlined.ShoppingCart,
                                emptyTitle = "Sin compras aún",
                                emptySubtitle = "Tus compras aparecerán aquí",
                                isSales = false,
                                onOrderClick = { selectedOrder = it }
                            )
                            1 -> TransactionsList(
                                orders = sales,
                                emptyIcon = Icons.Outlined.Storefront,
                                emptyTitle = "Sin ventas aún",
                                emptySubtitle = "Tus ventas aparecerán aquí",
                                isSales = true,
                                onOrderClick = { selectedOrder = it }
                            )
                        }
                    }
                }
            }
        }
        
        // Modal de detalle de orden
        selectedOrder?.let { order ->
            OrderDetailModal(
                order = order,
                isSale = sales.any { it.id == order.id },
                onDismiss = { selectedOrder = null },
                onMarkAsShipped = { trackingNumber ->
                    scope.launch {
                        OrderRepository.markAsShipped(order.id, trackingNumber)
                        OrderRepository.invalidateCache()
                        OrderRepository.loadTransactionsWithCache(forceRefresh = true)
                        selectedOrder = null
                    }
                },
                onMarkAsDelivered = {
                    scope.launch {
                        OrderRepository.markAsDelivered(order.id)
                        OrderRepository.invalidateCache()
                        OrderRepository.loadTransactionsWithCache(forceRefresh = true)
                        selectedOrder = null
                    }
                }
            )
        }
    }
}

@Composable
private fun TransactionsSummaryCard(summary: TransactionsSummary) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Compras
            SummaryStatItem(
                icon = Icons.Outlined.ShoppingBag,
                iconColor = Color(0xFF1565A0),
                label = "Compras",
                value = summary.totalPurchases.toString(),
                subValue = "$${String.format("%,.0f", summary.totalPurchasesAmount)}",
                pendingCount = summary.pendingPurchases
            )
            
            // Divider vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(BorderSubtle)
            )
            
            // Ventas / Ganancias
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8B57).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF2E8B57),
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Ganancias",
                    fontSize = 12.sp,
                    color = Color(0xFF2E8B57)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+$${String.format("%,.0f", summary.totalSalesAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E8B57)
                    )
                    if (summary.pendingSales > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFA726)
                        ) {
                            Text(
                                text = "${summary.pendingSales}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = "${summary.totalSales} ventas",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    subValue: String,
    pendingCount: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (pendingCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFA726)
                ) {
                    Text(
                        text = "$pendingCount",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
        
        Text(
            text = subValue,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun TransactionsTabs(
    selectedTab: Int,
    purchasesCount: Int,
    salesCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionTab(
            title = "Compras",
            count = purchasesCount,
            isSelected = selectedTab == 0,
            color = Color(0xFF1565A0),
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        
        TransactionTab(
            title = "Ventas",
            count = salesCount,
            isSelected = selectedTab == 1,
            color = Color(0xFF2E8B57),
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TransactionTab(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.15f) else SurfaceElevated,
        animationSpec = tween(200),
        label = "tabBg"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBorder"
    )
    
    Surface(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else TextSecondary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) color.copy(alpha = 0.2f) else BorderSubtle
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TransactionsList(
    orders: List<Order>,
    emptyIcon: ImageVector,
    emptyTitle: String,
    emptySubtitle: String,
    isSales: Boolean,
    onOrderClick: (Order) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                icon = emptyIcon,
                title = emptyTitle,
                subtitle = emptySubtitle
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                TransactionCard(
                    order = order,
                    isSale = isSales,
                    onClick = { onOrderClick(order) }
                )
            }
        }
    }
}

@Composable
private fun TransactionCard(
    order: Order,
    isSale: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (order.status) {
        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
        OrderStatus.PAID, OrderStatus.PREPARING -> Color(0xFF1565A0)
        OrderStatus.SHIPPED -> Color(0xFFFF6B35)
        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> Color(0xFF2E8B57)
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Color(0xFFEF4444)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con número de orden y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Text(
                        text = order.formattedDate,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = order.statusDisplayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Items preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imágenes de productos (máximo 3)
                Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                    order.items.take(3).forEachIndexed { index, item ->
                        AsyncImage(
                            model = item.imageUrl ?: "",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, HomeBg, RoundedCornerShape(8.dp))
                        )
                    }
                    
                    if (order.items.size > 3) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BorderSubtle)
                                .border(2.dp, HomeBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${order.items.size - 3}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Info de items
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.items.firstOrNull()?.title ?: "Producto",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (order.items.size > 1) {
                        Text(
                            text = "+${order.items.size - 1} más",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                
                // Total / Ganancia
                Column(horizontalAlignment = Alignment.End) {
                    if (isSale) {
                        // Badge de ganancia destacado para ventas
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E8B57).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "+$${String.format("%,.0f", order.items.sumOf { it.totalPrice })}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E8B57)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ganancia",
                            fontSize = 10.sp,
                            color = Color(0xFF2E8B57).copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "$${String.format("%,.0f", order.totalAmount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = order.currency,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
            
            // Info del comprador (solo para ventas)
            if (isSale && order.buyerUsername != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Comprador: @${order.buyerUsername}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Tracking number si existe
            if (order.trackingNumber != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tracking: ${order.trackingNumber}",
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B35),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderDetailModal(
    order: Order,
    isSale: Boolean,
    onDismiss: () -> Unit,
    onMarkAsShipped: (String?) -> Unit,
    onMarkAsDelivered: () -> Unit
) {
    var trackingNumber by remember { mutableStateOf("") }
    var showShippingDialog by remember { mutableStateOf(false) }
    
    val statusColor = when (order.status) {
        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
        OrderStatus.PAID, OrderStatus.PREPARING -> Color(0xFF1565A0)
        OrderStatus.SHIPPED -> Color(0xFFFF6B35)
        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> Color(0xFF2E8B57)
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Color(0xFFEF4444)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = HomeBg
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderSubtle)
                )
                
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Detalle de ${if (isSale) "venta" else "compra"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = order.orderNumber,
                            fontSize = 14.sp,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = order.statusDisplayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Divider(color = BorderSubtle)
                
                // Contenido scrolleable
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Timeline de estados
                    item {
                        OrderTimeline(order = order)
                    }
                    
                    // Items
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceElevated
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Productos (${order.items.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                order.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = item.imageUrl ?: "",
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row {
                                                Text(
                                                    text = "Cant: ${item.quantity}",
                                                    fontSize = 12.sp,
                                                    color = TextMuted
                                                )
                                                item.selectedColor?.let {
                                                    Text(
                                                        text = " • $it",
                                                        fontSize = 12.sp,
                                                        color = TextMuted
                                                    )
                                                }
                                                item.selectedSize?.let {
                                                    Text(
                                                        text = " • $it",
                                                        fontSize = 12.sp,
                                                        color = TextMuted
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Text(
                                            text = "$${String.format("%,.0f", item.totalPrice)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Resumen de pagos
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceElevated
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Resumen",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                SummaryRow("Subtotal", "$${String.format("%,.0f", order.subtotal)}")
                                SummaryRow(
                                    "Envío",
                                    if (order.shippingCost == 0.0) "GRATIS" else "$${String.format("%,.0f", order.shippingCost)}",
                                    isHighlighted = order.shippingCost == 0.0
                                )
                                if (order.discountAmount > 0) {
                                    SummaryRow(
                                        "Descuento",
                                        "-$${String.format("%,.0f", order.discountAmount)}",
                                        isHighlighted = true
                                    )
                                }
                                
                                Divider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = BorderSubtle
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$${String.format("%,.0f", order.totalAmount)} ${order.currency}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF2E8B57)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Info del comprador (para ventas)
                    if (isSale && order.buyerUsername != null) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = order.buyerAvatarUrl ?: "",
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(BorderSubtle)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = "Comprador",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "@${order.buyerUsername}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    IconButton(onClick = { /* Abrir chat */ }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Chat,
                                            contentDescription = "Contactar",
                                            tint = PrimaryPurple
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Espacio para botones
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                
                // Botones de acción
                if (isSale && order.status == OrderStatus.PAID) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = { showShippingDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Marcar como enviado",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (!isSale && order.status == OrderStatus.SHIPPED) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = onMarkAsDelivered,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirmar recepción",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialog para número de tracking
    if (showShippingDialog) {
        AlertDialog(
            onDismissRequest = { showShippingDialog = false },
            containerColor = SurfaceElevated,
            title = {
                Text(
                    text = "Marcar como enviado",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Ingresa el número de seguimiento (opcional)",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = trackingNumber,
                        onValueChange = { trackingNumber = it },
                        placeholder = { Text("Ej: UY123456789") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkAsShipped(trackingNumber.ifEmpty { null })
                        showShippingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                ) {
                    Text("Confirmar envío")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShippingDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun OrderTimeline(order: Order) {
    val steps = listOf(
        TimelineStep(
            title = "Pedido creado",
            subtitle = order.formattedDate,
            isCompleted = true,
            icon = Icons.Outlined.ShoppingCart
        ),
        TimelineStep(
            title = "Pago confirmado",
            subtitle = order.paidAt?.let { formatTimestamp(it) } ?: "Pendiente",
            isCompleted = order.status.ordinal >= OrderStatus.PAID.ordinal,
            icon = Icons.Outlined.Payment
        ),
        TimelineStep(
            title = "Enviado",
            subtitle = order.shippedAt?.let { formatTimestamp(it) } 
                ?: order.trackingNumber?.let { "Tracking: $it" } 
                ?: "Pendiente",
            isCompleted = order.status.ordinal >= OrderStatus.SHIPPED.ordinal,
            icon = Icons.Outlined.LocalShipping
        ),
        TimelineStep(
            title = "Entregado",
            subtitle = order.deliveredAt?.let { formatTimestamp(it) } ?: "Pendiente",
            isCompleted = order.status.ordinal >= OrderStatus.DELIVERED.ordinal,
            icon = Icons.Outlined.CheckCircle
        )
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estado del pedido",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            steps.forEachIndexed { index, step ->
                TimelineItem(
                    step = step,
                    isLast = index == steps.lastIndex
                )
            }
        }
    }
}

private data class TimelineStep(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val icon: ImageVector
)

@Composable
private fun TimelineItem(
    step: TimelineStep,
    isLast: Boolean
) {
    val color = if (step.isCompleted) Color(0xFF2E8B57) else TextMuted
    
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (step.isCompleted) 0.15f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (step.isCompleted) Icons.Filled.Check else step.icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(if (step.isCompleted) color.copy(alpha = 0.3f) else BorderSubtle)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(
                text = step.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (step.isCompleted) TextPrimary else TextMuted
            )
            Text(
                text = step.subtitle,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) Color(0xFF2E8B57) else TextPrimary
        )
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(timestamp)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM, HH:mm", java.util.Locale("es", "ES"))
        zoned.format(formatter)
    } catch (e: Exception) {
        timestamp.take(16).replace("T", " ")
    }
}
