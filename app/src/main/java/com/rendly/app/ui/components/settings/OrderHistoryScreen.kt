package com.rendly.app.ui.components.settings

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Order
import com.rendly.app.data.model.OrderStatus
import com.rendly.app.data.model.HandshakeTransaction
import com.rendly.app.data.model.HandshakeStatus
import com.rendly.app.data.repository.OrderRepository
import com.rendly.app.data.repository.HandshakeRepository
import com.rendly.app.data.repository.TransactionsSummary
import com.rendly.app.data.remote.SupabaseClient
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
    val handshakes by OrderRepository.cachedHandshakes.collectAsState()
    val summary by OrderRepository.cachedSummary.collectAsState()
    val isCacheLoaded by OrderRepository.isCacheLoaded.collectAsState()
    val isRefreshing by OrderRepository.isRefreshing.collectAsState()
    val currentUserId = remember { SupabaseClient.auth.currentUserOrNull()?.id }
    
    // Estado para detalle de orden
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var detailOrder by remember { mutableStateOf<Order?>(null) }
    val showDetail = selectedOrder != null
    val detailSlideOffset by animateFloatAsState(
        targetValue = if (showDetail) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "detailSlide"
    )
    LaunchedEffect(selectedOrder) {
        if (selectedOrder != null) detailOrder = selectedOrder
    }
    LaunchedEffect(detailSlideOffset) {
        if (detailSlideOffset == 1f && selectedOrder == null) detailOrder = null
    }
    
    // Estado para detalle de handshake
    var selectedHandshake by remember { mutableStateOf<HandshakeTransaction?>(null) }
    var detailHandshake by remember { mutableStateOf<HandshakeTransaction?>(null) }
    val showHandshakeDetail = selectedHandshake != null
    val handshakeDetailSlideOffset by animateFloatAsState(
        targetValue = if (showHandshakeDetail) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "hsDetailSlide"
    )
    LaunchedEffect(selectedHandshake) {
        if (selectedHandshake != null) detailHandshake = selectedHandshake
    }
    LaunchedEffect(handshakeDetailSlideOffset) {
        if (handshakeDetailSlideOffset == 1f && selectedHandshake == null) detailHandshake = null
    }
    
    // Loading = primera carga sin cache
    val isLoading = !isCacheLoaded && isRefreshing
    
    // Pager state para 3 tabs
    val pagerState = rememberPagerState(pageCount = { 3 })
    
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
                    // ═══ Detectar scroll para ocultar/mostrar summary ═══
                    var isSummaryVisible by remember { mutableStateOf(true) }
                    val nestedScrollConnection = remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                // Solo detectar dirección, NO consumir scroll
                                if (available.y < -8f) isSummaryVisible = false  // scroll down
                                else if (available.y > 8f) isSummaryVisible = true  // scroll up
                                return Offset.Zero
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                    ) {
                        // Resumen de estadísticas - se oculta con AnimatedVisibility
                        AnimatedVisibility(
                            visible = isSummaryVisible,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                TransactionsSummaryCard(summary = summary)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                        
                        // Tabs - sticky, siempre visibles
                        TransactionsTabs(
                            selectedTab = pagerState.currentPage,
                            onTabSelected = { tab ->
                                scope.launch { pagerState.animateScrollToPage(tab) }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Contenido con pager
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            beyondBoundsPageCount = 1
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
                                2 -> HandshakesList(
                                    handshakes = handshakes,
                                    currentUserId = currentUserId ?: "",
                                    onHandshakeClick = { selectedHandshake = it }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Detalle de orden - PANTALLA COMPLETA con slide
        detailOrder?.let { order ->
            OrderDetailScreen(
                order = order,
                slideOffset = detailSlideOffset,
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
                },
                onHandshakeConfirm = { handshakeId ->
                    scope.launch {
                        val uid = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                        HandshakeRepository.confirmTransaction(handshakeId, uid)
                        OrderRepository.invalidateCache()
                        OrderRepository.loadTransactionsWithCache(forceRefresh = true)
                        selectedOrder = null
                    }
                }
            )
        }
        
        // Detalle de handshake - PANTALLA COMPLETA con slide
        detailHandshake?.let { hs ->
            HandshakeDetailScreen(
                handshake = hs,
                slideOffset = handshakeDetailSlideOffset,
                currentUserId = currentUserId ?: "",
                onDismiss = { selectedHandshake = null },
                onConfirm = { handshakeId ->
                    scope.launch {
                        val uid = currentUserId ?: return@launch
                        HandshakeRepository.confirmTransaction(handshakeId, uid)
                        OrderRepository.invalidateCache()
                        OrderRepository.loadTransactionsWithCache(forceRefresh = true)
                        selectedHandshake = null
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
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Compras + Ventas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    icon = Icons.Outlined.ShoppingBag,
                    iconColor = Color(0xFF1565A0),
                    label = "Compras",
                    value = summary.totalPurchases.toString(),
                    subValue = "$${String.format("%,.0f", summary.totalPurchasesAmount)}",
                    pendingCount = summary.pendingPurchases,
                    modifier = Modifier.weight(1f)
                )
                
                // Divider vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(70.dp)
                        .background(BorderSubtle.copy(alpha = 0.5f))
                )
                
                SummaryStatItem(
                    icon = Icons.Outlined.Storefront,
                    iconColor = Color(0xFF7C4DFF),
                    label = "Ventas",
                    value = summary.totalSales.toString(),
                    subValue = "$${String.format("%,.0f", summary.totalSalesAmount)}",
                    pendingCount = summary.pendingSales,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Divider horizontal
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(1.dp)
                    .background(BorderSubtle.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Fila inferior: Ganancias + Acuerdos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    icon = Icons.Filled.TrendingUp,
                    iconColor = Color(0xFF2E8B57),
                    label = "Ganancias",
                    value = "+$${String.format("%,.0f", summary.totalSalesAmount)}",
                    subValue = "neto de ventas",
                    pendingCount = 0,
                    modifier = Modifier.weight(1f)
                )
                
                // Divider vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(70.dp)
                        .background(BorderSubtle.copy(alpha = 0.5f))
                )
                
                SummaryStatItem(
                    icon = Icons.Outlined.Handshake,
                    iconColor = Color(0xFFFF6B35),
                    label = "Acuerdos",
                    value = summary.totalHandshakes.toString(),
                    subValue = "$${String.format("%,.0f", summary.totalHandshakesAmount)}",
                    pendingCount = summary.pendingHandshakes,
                    modifier = Modifier.weight(1f)
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
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 18.sp,
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
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
        
        Text(
            text = subValue,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun TransactionsTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TransactionTab(
            title = "Compras",
            icon = Icons.Outlined.ShoppingBag,
            isSelected = selectedTab == 0,
            color = Color(0xFF1565A0),
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        
        TransactionTab(
            title = "Ventas",
            icon = Icons.Outlined.Storefront,
            isSelected = selectedTab == 1,
            color = Color(0xFF7C4DFF),
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
        
        TransactionTab(
            title = "Acuerdos",
            icon = Icons.Outlined.Handshake,
            isSelected = selectedTab == 2,
            color = Color(0xFFFF6B35),
            onClick = { onTabSelected(2) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TransactionTab(
    title: String,
    icon: ImageVector,
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
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) borderColor else BorderSubtle.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else TextSecondary
            )
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
                        color = Color(0xFF8B5CF6)
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
private fun OrderDetailScreen(
    order: Order,
    slideOffset: Float,
    isSale: Boolean,
    onDismiss: () -> Unit,
    onMarkAsShipped: (String?) -> Unit,
    onMarkAsDelivered: () -> Unit,
    onHandshakeConfirm: (String) -> Unit = {}
) {
    var trackingNumber by remember { mutableStateOf("") }
    var showShippingDialog by remember { mutableStateOf(false) }
    
    // Cargar handshake vinculado si existe
    val scope = rememberCoroutineScope()
    var handshake by remember { mutableStateOf<HandshakeTransaction?>(order.handshake) }
    val currentUserId = remember { SupabaseClient.auth.currentUserOrNull()?.id }
    
    LaunchedEffect(order.handshakeId) {
        if (order.handshakeId != null && handshake == null) {
            handshake = OrderRepository.loadHandshakeForOrder(order.handshakeId)
        }
    }
    
    val statusColor = when (order.status) {
        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
        OrderStatus.PAID, OrderStatus.PREPARING -> Color(0xFF1565A0)
        OrderStatus.SHIPPED -> Color(0xFFFF6B35)
        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> Color(0xFF2E8B57)
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Color(0xFFEF4444)
    }
    
    val statusIcon = when (order.status) {
        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Icons.Outlined.HourglassEmpty
        OrderStatus.PAID, OrderStatus.PREPARING -> Icons.Outlined.Payment
        OrderStatus.SHIPPED -> Icons.Outlined.LocalShipping
        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> Icons.Outlined.CheckCircle
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Icons.Outlined.Cancel
    }
    
    // Full-screen surface con slide desde la derecha
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
            // ═══ HEADER ═══
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Detalle de ${if (isSale) "venta" else "compra"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = order.orderNumber,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = order.statusDisplayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            
            // ═══ CONTENIDO SCROLLEABLE ═══
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Status banner ──
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = statusColor.copy(alpha = 0.08f)
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
                                    .background(statusColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = order.statusDisplayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = order.formattedDate,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Text(
                                text = "$${String.format("%,.0f", order.totalAmount)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                    }
                }
                
                // ── Timeline de estados ──
                item {
                    OrderTimeline(order = order, handshake = handshake)
                }
                
                // ── Productos ──
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Productos (${order.items.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            order.items.forEachIndexed { index, item ->
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
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Surface)
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
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Surface
                                            ) {
                                                Text(
                                                    text = "x${item.quantity}",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            item.selectedColor?.let { colorName ->
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Surface
                                                ) {
                                                    Text(
                                                        text = colorName,
                                                        fontSize = 11.sp,
                                                        color = TextSecondary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            item.selectedSize?.let { sizeName ->
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Surface
                                                ) {
                                                    Text(
                                                        text = sizeName,
                                                        fontSize = 11.sp,
                                                        color = TextSecondary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
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
                                
                                if (index < order.items.size - 1) {
                                    Divider(
                                        modifier = Modifier.padding(start = 72.dp),
                                        color = BorderSubtle.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // ── Resumen de pagos ──
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Resumen de pago",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
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
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = BorderSubtle)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%,.0f", order.totalAmount)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSale) Color(0xFF2E8B57) else TextPrimary
                                    )
                                    Text(
                                        text = order.currency,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
                
                // ── Tracking info ──
                if (order.trackingNumber != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFF6B35).copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF6B35).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Número de seguimiento",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = order.trackingNumber ?: "",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF6B35)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // ── Info del comprador (para ventas) ──
                if (isSale && order.buyerUsername != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = order.buyerAvatarUrl ?: "",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Surface)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
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
                                
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = PrimaryBright.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { /* Abrir chat */ }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Chat,
                                            contentDescription = "Contactar",
                                            tint = PrimaryBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Chat",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryBright
                                        )
                                    }
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
            
            // ═══ BOTONES DE ACCIÓN ═══
            // Handshake: botón de confirmar entrega mutua
            val hs = handshake
            if (hs != null && order.handshakeId != null) {
                val hsStatus = hs.getStatusEnum()
                val canConfirm = currentUserId != null && hs.canConfirm(currentUserId)
                val alreadyConfirmed = currentUserId != null && (
                    (hs.isInitiator(currentUserId) && hs.initiatorConfirmed) ||
                    (hs.isReceiver(currentUserId) && hs.receiverConfirmed)
                )
                
                if (hsStatus == HandshakeStatus.ACCEPTED || hsStatus == HandshakeStatus.IN_PROGRESS) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        if (canConfirm) {
                            Button(
                                onClick = { onHandshakeConfirm(hs.id ?: "") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Handshake,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Confirmar que recibí mi parte",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (alreadyConfirmed) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ya confirmaste. Esperando a la otra parte.",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                // Flujo tradicional (envío)
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
                            focusedBorderColor = PrimaryBright,
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
private fun OrderTimeline(order: Order, handshake: HandshakeTransaction? = null) {
    val steps = if (handshake != null) {
        // Handshake-based timeline (in-person transaction)
        val hsStatus = handshake.getStatusEnum()
        listOf(
            TimelineStep(
                title = "Pedido creado",
                subtitle = order.formattedDate,
                isCompleted = true,
                icon = Icons.Outlined.ShoppingCart
            ),
            TimelineStep(
                title = "Acuerdo propuesto",
                subtitle = handshake.createdAt?.let { formatTimestamp(it) } ?: "Pendiente",
                isCompleted = hsStatus.ordinal >= HandshakeStatus.PROPOSED.ordinal,
                icon = Icons.Outlined.Send
            ),
            TimelineStep(
                title = "Acuerdo aceptado",
                subtitle = when (hsStatus) {
                    HandshakeStatus.ACCEPTED, HandshakeStatus.IN_PROGRESS, HandshakeStatus.COMPLETED ->
                        handshake.acceptedAt?.let { formatTimestamp(it) } ?: "Aceptado"
                    HandshakeStatus.REJECTED -> "Rechazado"
                    HandshakeStatus.RENEGOTIATING -> "En renegociación"
                    HandshakeStatus.CANCELLED -> "Cancelado"
                    else -> "Pendiente"
                },
                isCompleted = hsStatus in listOf(
                    HandshakeStatus.ACCEPTED, HandshakeStatus.IN_PROGRESS, HandshakeStatus.COMPLETED
                ),
                icon = Icons.Outlined.Handshake
            ),
            TimelineStep(
                title = "Confirmación mutua",
                subtitle = when {
                    hsStatus == HandshakeStatus.COMPLETED -> "Ambas partes confirmaron"
                    handshake.initiatorConfirmed && !handshake.receiverConfirmed -> "Esperando confirmación del receptor"
                    !handshake.initiatorConfirmed && handshake.receiverConfirmed -> "Esperando confirmación del iniciador"
                    else -> "Ambas partes deben confirmar"
                },
                isCompleted = hsStatus == HandshakeStatus.COMPLETED ||
                    (handshake.initiatorConfirmed && handshake.receiverConfirmed),
                icon = Icons.Outlined.VerifiedUser
            ),
            TimelineStep(
                title = "Transacción completada",
                subtitle = handshake.completedAt?.let { formatTimestamp(it) } ?: "Pendiente",
                isCompleted = hsStatus == HandshakeStatus.COMPLETED,
                icon = Icons.Outlined.CheckCircle
            )
        )
    } else {
        // Traditional shipping timeline
        listOf(
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
    }
    
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

// ═══════════════════════════════════════════════════════════════════════════════
// HANDSHAKES TAB - Lista y tarjetas de acuerdos
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HandshakesList(
    handshakes: List<HandshakeTransaction>,
    currentUserId: String,
    onHandshakeClick: (HandshakeTransaction) -> Unit
) {
    if (handshakes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                icon = Icons.Outlined.Handshake,
                title = "Sin acuerdos aún",
                subtitle = "Los acuerdos que hagas por chat aparecerán aquí"
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(handshakes, key = { it.id ?: "" }) { hs ->
                HandshakeTransactionCard(
                    handshake = hs,
                    currentUserId = currentUserId,
                    onClick = { onHandshakeClick(hs) }
                )
            }
        }
    }
}

@Composable
private fun HandshakeTransactionCard(
    handshake: HandshakeTransaction,
    currentUserId: String,
    onClick: () -> Unit
) {
    val hsStatus = handshake.getStatusEnum()
    val statusColor = when (hsStatus) {
        HandshakeStatus.PROPOSED -> Color(0xFFFFA726)
        HandshakeStatus.ACCEPTED -> Color(0xFF1565A0)
        HandshakeStatus.IN_PROGRESS -> Color(0xFF7C4DFF)
        HandshakeStatus.RENEGOTIATING -> Color(0xFFFF6B35)
        HandshakeStatus.COMPLETED -> Color(0xFF2E8B57)
        HandshakeStatus.REJECTED -> Color(0xFFEF4444)
        HandshakeStatus.CANCELLED -> Color(0xFFEF4444)
        HandshakeStatus.DISPUTED -> Color(0xFFD32F2F)
    }
    
    val statusText = when (hsStatus) {
        HandshakeStatus.PROPOSED -> "Propuesto"
        HandshakeStatus.ACCEPTED -> "Aceptado"
        HandshakeStatus.IN_PROGRESS -> "En progreso"
        HandshakeStatus.RENEGOTIATING -> "Renegociando"
        HandshakeStatus.COMPLETED -> "Completado"
        HandshakeStatus.REJECTED -> "Rechazado"
        HandshakeStatus.CANCELLED -> "Cancelado"
        HandshakeStatus.DISPUTED -> "En disputa"
    }
    
    val isInitiator = handshake.isInitiator(currentUserId)
    val roleText = if (isInitiator) "Iniciador" else "Receptor"
    val roleColor = if (isInitiator) Color(0xFF7C4DFF) else Color(0xFF1565A0)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con ID y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Handshake,
                            contentDescription = null,
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Acuerdo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B35)
                        )
                    }
                    Text(
                        text = handshake.createdAt?.let { formatTimestamp(it) } ?: "",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = roleColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = roleText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = roleColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Descripción del producto
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = HomeBg
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF6B35).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = handshake.productDescription,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "$${String.format("%,.0f", handshake.agreedPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            
            // Confirmaciones
            if (hsStatus == HandshakeStatus.ACCEPTED || hsStatus == HandshakeStatus.IN_PROGRESS || hsStatus == HandshakeStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConfirmationBadge(
                        label = "Iniciador",
                        confirmed = handshake.initiatorConfirmed,
                        modifier = Modifier.weight(1f)
                    )
                    ConfirmationBadge(
                        label = "Receptor",
                        confirmed = handshake.receiverConfirmed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Contra-oferta si existe
            if (hsStatus == HandshakeStatus.RENEGOTIATING && handshake.counterPrice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF6B35).copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contra-oferta: $${String.format("%,.0f", handshake.counterPrice)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF6B35)
                        )
                        if (handshake.counterMessage != null) {
                            Text(
                                text = " - ${handshake.counterMessage}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationBadge(
    label: String,
    confirmed: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (confirmed) Color(0xFF2E8B57).copy(alpha = 0.1f) else BorderSubtle.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (confirmed) Icons.Filled.CheckCircle else Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                tint = if (confirmed) Color(0xFF2E8B57) else TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (confirmed) Color(0xFF2E8B57) else TextMuted,
                fontWeight = if (confirmed) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HANDSHAKE DETAIL SCREEN - Pantalla completa de detalle de acuerdo
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HandshakeDetailScreen(
    handshake: HandshakeTransaction,
    slideOffset: Float,
    currentUserId: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val hsStatus = handshake.getStatusEnum()
    val statusColor = when (hsStatus) {
        HandshakeStatus.PROPOSED -> Color(0xFFFFA726)
        HandshakeStatus.ACCEPTED -> Color(0xFF1565A0)
        HandshakeStatus.IN_PROGRESS -> Color(0xFF7C4DFF)
        HandshakeStatus.RENEGOTIATING -> Color(0xFFFF6B35)
        HandshakeStatus.COMPLETED -> Color(0xFF2E8B57)
        HandshakeStatus.REJECTED -> Color(0xFFEF4444)
        HandshakeStatus.CANCELLED -> Color(0xFFEF4444)
        HandshakeStatus.DISPUTED -> Color(0xFFD32F2F)
    }
    val statusText = when (hsStatus) {
        HandshakeStatus.PROPOSED -> "Propuesto"
        HandshakeStatus.ACCEPTED -> "Aceptado"
        HandshakeStatus.IN_PROGRESS -> "En progreso"
        HandshakeStatus.RENEGOTIATING -> "Renegociando"
        HandshakeStatus.COMPLETED -> "Completado"
        HandshakeStatus.REJECTED -> "Rechazado"
        HandshakeStatus.CANCELLED -> "Cancelado"
        HandshakeStatus.DISPUTED -> "En disputa"
    }
    val statusIcon = when (hsStatus) {
        HandshakeStatus.PROPOSED -> Icons.Outlined.Send
        HandshakeStatus.ACCEPTED -> Icons.Outlined.Handshake
        HandshakeStatus.IN_PROGRESS -> Icons.Outlined.Sync
        HandshakeStatus.RENEGOTIATING -> Icons.Outlined.SwapHoriz
        HandshakeStatus.COMPLETED -> Icons.Outlined.CheckCircle
        HandshakeStatus.REJECTED -> Icons.Outlined.Cancel
        HandshakeStatus.CANCELLED -> Icons.Outlined.Cancel
        HandshakeStatus.DISPUTED -> Icons.Outlined.Warning
    }
    
    val isInitiator = handshake.isInitiator(currentUserId)
    val canConfirm = handshake.canConfirm(currentUserId)
    val alreadyConfirmed = if (isInitiator) handshake.initiatorConfirmed else handshake.receiverConfirmed
    
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
            // ═══ HEADER ═══
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Detalle del acuerdo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isInitiator) "Tú iniciaste este acuerdo" else "Recibiste este acuerdo",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            
            // ═══ CONTENIDO SCROLLEABLE ═══
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Status banner ──
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = statusColor.copy(alpha = 0.08f)
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
                                    .background(statusColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = statusText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = handshake.createdAt?.let { formatTimestamp(it) } ?: "",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            
                            Text(
                                text = "$${String.format("%,.0f", handshake.agreedPrice)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                    }
                }
                
                // ── Timeline de estados ──
                item {
                    HandshakeTimeline(handshake = handshake)
                }
                
                // ── Producto / Descripción ──
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Descripción del acuerdo",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = handshake.productDescription,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                
                // ── Resumen financiero ──
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Detalle financiero",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            SummaryRow(
                                "Precio acordado",
                                "$${String.format("%,.0f", handshake.agreedPrice)}"
                            )
                            
                            if (handshake.counterPrice != null) {
                                SummaryRow(
                                    "Contra-oferta",
                                    "$${String.format("%,.0f", handshake.counterPrice)}",
                                    isHighlighted = true
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = BorderSubtle)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$${String.format("%,.0f", handshake.counterPrice ?: handshake.agreedPrice)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
                
                // ── Participantes ──
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Participantes",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Iniciador
                            ParticipantRow(
                                label = "Iniciador",
                                userId = handshake.initiatorId,
                                isCurrentUser = isInitiator,
                                confirmed = handshake.initiatorConfirmed,
                                accentColor = Color(0xFF7C4DFF)
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Receptor
                            ParticipantRow(
                                label = "Receptor",
                                userId = handshake.receiverId,
                                isCurrentUser = !isInitiator,
                                confirmed = handshake.receiverConfirmed,
                                accentColor = Color(0xFF1565A0)
                            )
                        }
                    }
                }
                
                // ── Contra-mensaje si existe ──
                if (handshake.counterMessage != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFF6B35).copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF6B35).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Message,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B35),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mensaje de renegociación",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = handshake.counterMessage ?: "",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        lineHeight = 20.sp
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
            
            // ═══ BOTONES DE ACCIÓN ═══
            if (hsStatus == HandshakeStatus.ACCEPTED || hsStatus == HandshakeStatus.IN_PROGRESS) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HomeBg,
                    shadowElevation = 8.dp
                ) {
                    if (canConfirm) {
                        Button(
                            onClick = { onConfirm(handshake.id ?: "") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8B57))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Handshake,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirmar que recibí mi parte",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (alreadyConfirmed) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ya confirmaste. Esperando a la otra parte.",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            // Banner de completado
            if (hsStatus == HandshakeStatus.COMPLETED) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2E8B57).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E8B57),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acuerdo completado exitosamente",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E8B57)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    label: String,
    userId: String,
    isCurrentUser: Boolean,
    confirmed: Boolean,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (isCurrentUser) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryBright.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Tú",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = userId.take(8) + "...",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        
        Icon(
            imageVector = if (confirmed) Icons.Filled.CheckCircle else Icons.Outlined.HourglassEmpty,
            contentDescription = null,
            tint = if (confirmed) Color(0xFF2E8B57) else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (confirmed) "Confirmado" else "Pendiente",
            fontSize = 12.sp,
            fontWeight = if (confirmed) FontWeight.Bold else FontWeight.Normal,
            color = if (confirmed) Color(0xFF2E8B57) else TextMuted
        )
    }
}

@Composable
private fun HandshakeTimeline(handshake: HandshakeTransaction) {
    val hsStatus = handshake.getStatusEnum()
    val steps = listOf(
        TimelineStep(
            title = "Acuerdo propuesto",
            subtitle = handshake.createdAt?.let { formatTimestamp(it) } ?: "Pendiente",
            isCompleted = true,
            icon = Icons.Outlined.Send
        ),
        TimelineStep(
            title = when (hsStatus) {
                HandshakeStatus.REJECTED -> "Acuerdo rechazado"
                HandshakeStatus.RENEGOTIATING -> "En renegociación"
                HandshakeStatus.CANCELLED -> "Acuerdo cancelado"
                else -> "Acuerdo aceptado"
            },
            subtitle = when (hsStatus) {
                HandshakeStatus.ACCEPTED, HandshakeStatus.IN_PROGRESS, HandshakeStatus.COMPLETED ->
                    handshake.acceptedAt?.let { formatTimestamp(it) } ?: "Aceptado"
                HandshakeStatus.REJECTED -> "El receptor rechazó"
                HandshakeStatus.RENEGOTIATING -> "Propuesta de cambios"
                HandshakeStatus.CANCELLED -> "Cancelado por un participante"
                else -> "Pendiente"
            },
            isCompleted = hsStatus in listOf(
                HandshakeStatus.ACCEPTED, HandshakeStatus.IN_PROGRESS, HandshakeStatus.COMPLETED
            ),
            icon = Icons.Outlined.Handshake
        ),
        TimelineStep(
            title = "Confirmación mutua",
            subtitle = when {
                hsStatus == HandshakeStatus.COMPLETED -> "Ambas partes confirmaron"
                handshake.initiatorConfirmed && !handshake.receiverConfirmed -> "Esperando al receptor"
                !handshake.initiatorConfirmed && handshake.receiverConfirmed -> "Esperando al iniciador"
                else -> "Ambas partes deben confirmar"
            },
            isCompleted = hsStatus == HandshakeStatus.COMPLETED ||
                (handshake.initiatorConfirmed && handshake.receiverConfirmed),
            icon = Icons.Outlined.VerifiedUser
        ),
        TimelineStep(
            title = "Transacción completada",
            subtitle = handshake.completedAt?.let { formatTimestamp(it) } ?: "Pendiente",
            isCompleted = hsStatus == HandshakeStatus.COMPLETED,
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
                text = "Estado del acuerdo",
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
