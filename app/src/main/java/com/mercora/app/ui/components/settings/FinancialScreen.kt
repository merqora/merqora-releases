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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.model.Order
import com.mercora.app.data.model.OrderStatus
import com.mercora.app.data.repository.OrderRepository
import com.mercora.app.data.repository.PDFRepository
import com.mercora.app.data.repository.WalletRepository
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private data class FinancialTab(val id: String, val icon: ImageVector, val label: String)

private val tabs = listOf(
    FinancialTab("wallet", Icons.Outlined.AccountBalanceWallet, "Cobros"),
    FinancialTab("history", Icons.Outlined.ReceiptLong, "Historial"),
    FinancialTab("pdfs", Icons.Outlined.Description, "PDFs")
)

@Composable
fun FinancialScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    if (!isVisible && slideOffset == 1f) return

    var selectedTab by remember { mutableStateOf("wallet") }
    val scope = rememberCoroutineScope()
    val balance by WalletRepository.walletBalance.collectAsState()
    val transactions by WalletRepository.transactions.collectAsState()
    val sales by OrderRepository.cachedSales.collectAsState()
    val purchases by OrderRepository.cachedPurchases.collectAsState()
    var pdfs by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var showWithdraw by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
            WalletRepository.getWallet(userId)
            WalletRepository.loadTransactions(userId)
            OrderRepository.loadTransactionsWithCache()
            pdfs = PDFRepository.getUserPdfs(userId)
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
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Financiero", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOfFirst { it.id == selectedTab },
                    edgePadding = 8.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    contentColor = PrimaryPurple
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab.id == selectedTab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab.id },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(tab.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            selectedContentColor = PrimaryPurple,
                            unselectedContentColor = TextMuted
                        )
                    }
                }

                // Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        "wallet" -> WalletContent(balance, transactions, { showWithdraw = true })
                        "history" -> HistoryContent(transactions, purchases, sales)
                        "pdfs" -> PDFsContent(pdfs, onRefresh = {
                            scope.launch {
                                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                                pdfs = PDFRepository.getUserPdfs(userId)
                            }
                        })
                    }
                }
            }

            WithdrawScreen(
                isVisible = showWithdraw,
                onDismiss = { showWithdraw = false },
                onSuccess = { showWithdraw = false }
            )
        }
    }
}

@Composable
private fun WalletContent(
    balance: Double,
    transactions: List<JsonObject>,
    onWithdraw: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Balance card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF0A3D62), Color(0xFF1A5276))),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saldo disponible", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "$${String.format("%,.0f", balance)}",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("UYU", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onWithdraw,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF0A3D62), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retirar", color = Color(0xFF0A3D62), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Ãšltimos movimientos
        item {
            Text("Ãšltimos movimientos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }

        if (transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sin movimientos", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            transactions.take(20).forEach { tx ->
                item { TransactionMiniItem(tx) }
            }
        }
    }
}

@Composable
private fun HistoryContent(
    transactions: List<JsonObject>,
    purchases: List<Order>,
    sales: List<Order>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Wallet transactions
        item {
            Text("Movimientos de billetera", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (transactions.isEmpty()) {
            item { Text("Sin movimientos", color = TextMuted, fontSize = 12.sp) }
        } else {
            transactions.take(10).forEach { tx ->
                item { TransactionMiniItem(tx) }
            }
        }

        // Orders recent
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ã“rdenes recientes", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        val recentOrders = (purchases + sales).sortedByDescending { it.createdAt }.take(10)
        if (recentOrders.isEmpty()) {
            item { Text("Sin Ã³rdenes", color = TextMuted, fontSize = 12.sp) }
        } else {
            recentOrders.forEach { order ->
                item { OrderCard(order, if (order.buyerId == SupabaseClient.auth.currentUserOrNull()?.id) "Compra" else "Venta") }
            }
        }
    }
}

@Composable
private fun PDFsContent(pdfs: List<JsonObject>, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mis comprobantes", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            TextButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Actualizar", fontSize = 12.sp)
            }
        }

        if (pdfs.isEmpty()) {
            EmptyStateBox(Icons.Outlined.Description, "No hay comprobantes guardados")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pdfs) { pdf -> PDFItem(pdf) }
            }
        }
    }
}

@Composable
private fun TransactionMiniItem(tx: JsonObject) {
    val type = tx["type"]?.toString()?.trim('"') ?: ""
    val amount = (tx["amount"]?.toString()?.trim('"') ?: "0").toDoubleOrNull() ?: 0.0
    val desc = tx["description"]?.toString()?.trim('"') ?: ""
    val date = (tx["created_at"]?.toString()?.trim('"') ?: "").take(10)
    val isCredit = type == "credit"
    val iconColor = if (isCredit) Color(0xFF22C55E) else Color(0xFFEF4444)
    val sign = if (isCredit) "+" else "-"

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.ArrowUpward, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp)) }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(desc.ifEmpty { if (isCredit) "CrÃ©dito" else "DÃ©bito" }, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(date, fontSize = 10.sp, color = TextMuted)
            }
            Text("$sign$${String.format("%,.0f", amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = iconColor)
        }
    }
}

@Composable
private fun OrderCard(order: Order, label: String) {
    val statusColor = when (order.status) {
        OrderStatus.COMPLETED, OrderStatus.DELIVERED -> Color(0xFF22C55E)
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Color(0xFFEF4444)
        OrderStatus.PENDING, OrderStatus.PAYMENT_PROCESSING -> Color(0xFFFFA726)
        else -> TextMuted
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Surface) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 11.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("#${order.orderNumber}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
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
            Spacer(modifier = Modifier.height(8.dp))
            if (order.items.isNotEmpty()) {
                Text(order.items.first().title, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "$${String.format("%,.0f", order.totalAmount)} ${order.currency}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun PDFItem(
    pdf: JsonObject,
    context: android.content.Context = androidx.compose.ui.platform.LocalContext.current
) {
    val fileName = pdf["file_name"]?.toString()?.trim('"') ?: "documento.pdf"
    val createdAt = pdf["created_at"]?.toString()?.trim('"') ?: ""
    val url = pdf["file_url"]?.toString()?.trim('"') ?: ""
    val date = createdAt.take(10)
    val time = createdAt.substringAfter("T").take(8)

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = Surface) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(fileName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$date Â· $time", fontSize = 11.sp, color = TextMuted)
            }
            TextButton(onClick = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }) {
                Text("Abrir", fontSize = 12.sp, color = PrimaryPurple)
            }
        }
    }
}

@Composable
private fun EmptyStateBox(icon: ImageVector, text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text, color = TextMuted, fontSize = 14.sp)
        }
    }
}
