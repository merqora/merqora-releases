package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.model.*
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ORDER REPOSITORY - Gestión de órdenes y pagos
 * ═══════════════════════════════════════════════════════════════════════════════
 */
object OrderRepository {
    private const val TAG = "OrderRepository"
    
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    // Cache para transacciones - carga instantánea
    private val _cachedPurchases = MutableStateFlow<List<Order>>(emptyList())
    val cachedPurchases: StateFlow<List<Order>> = _cachedPurchases
    
    private val _cachedSales = MutableStateFlow<List<Order>>(emptyList())
    val cachedSales: StateFlow<List<Order>> = _cachedSales
    
    private val _cachedSummary = MutableStateFlow(TransactionsSummary.empty())
    val cachedSummary: StateFlow<TransactionsSummary> = _cachedSummary
    
    private val _isCacheLoaded = MutableStateFlow(false)
    val isCacheLoaded: StateFlow<Boolean> = _isCacheLoaded
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    
    /**
     * Cargar transacciones con cache - muestra datos previos inmediatamente,
     * refresca en background si es necesario
     */
    suspend fun loadTransactionsWithCache(forceRefresh: Boolean = false) {
        // Si ya hay cache y no se fuerza refresh, no mostrar loading
        if (_isCacheLoaded.value && !forceRefresh) {
            // Refrescar en background silenciosamente
            _isRefreshing.value = true
            try {
                val purchases = getMyOrders()
                val sales = getMySales()
                _cachedPurchases.value = purchases
                _cachedSales.value = sales
                _cachedSummary.value = buildSummary(purchases, sales)
            } catch (_: Exception) {}
            _isRefreshing.value = false
            return
        }
        
        // Primera carga o force refresh
        _isRefreshing.value = true
        try {
            val purchases = getMyOrders()
            val sales = getMySales()
            _cachedPurchases.value = purchases
            _cachedSales.value = sales
            _cachedSummary.value = buildSummary(purchases, sales)
            _isCacheLoaded.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando transacciones: ${e.message}")
        }
        _isRefreshing.value = false
    }
    
    private fun buildSummary(purchases: List<Order>, sales: List<Order>): TransactionsSummary {
        val purchasesTotal = purchases.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.COMPLETED || it.status == OrderStatus.DELIVERED }
            .sumOf { it.totalAmount }
        val salesTotal = sales.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.COMPLETED || it.status == OrderStatus.DELIVERED }
            .sumOf { order -> order.items.sumOf { it.totalPrice } }
        return TransactionsSummary(
            totalPurchases = purchases.size,
            totalPurchasesAmount = purchasesTotal,
            totalSales = sales.size,
            totalSalesAmount = salesTotal,
            pendingPurchases = purchases.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAYMENT_PROCESSING },
            pendingSales = sales.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAYMENT_PROCESSING }
        )
    }
    
    /**
     * Invalidar cache (llamar después de crear orden, marcar enviado, etc.)
     */
    fun invalidateCache() {
        _isCacheLoaded.value = false
    }
    
    /**
     * Crear una orden desde los items del carrito
     */
    suspend fun createOrderFromCart(
        cartItems: List<CartRepository.CartItem>,
        shippingAddressId: String? = null,
        buyerNotes: String? = null
    ): Result<Order> = withContext(Dispatchers.IO) {
        try {
            _isProcessing.value = true
            
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))
            
            if (cartItems.isEmpty()) {
                return@withContext Result.failure(Exception("El carrito está vacío"))
            }
            
            // Calcular totales
            val subtotal = cartItems.sumOf { it.totalPrice }
            val shippingCost = if (subtotal >= 50000) 0.0 else 500.0 // Envío gratis > $50,000
            val totalAmount = subtotal + shippingCost
            
            // Crear orden
            val orderInsert = OrderInsert(
                buyerId = currentUserId,
                subtotal = subtotal,
                shippingCost = shippingCost,
                totalAmount = totalAmount,
                shippingAddressId = shippingAddressId,
                buyerNotes = buyerNotes
            )
            
            val orderResult = SupabaseClient.database
                .from("orders")
                .insert(orderInsert) {
                    select()
                }
                .decodeSingle<OrderDB>()
            
            Log.d(TAG, "✅ Orden creada: ${orderResult.orderNumber}")
            
            // Crear items de la orden
            val orderItems = mutableListOf<OrderItem>()
            for (cartItem in cartItems) {
                val itemInsert = OrderItemInsert(
                    orderId = orderResult.id,
                    postId = cartItem.post.id,
                    productId = cartItem.post.productId,
                    sellerId = cartItem.post.userId,
                    title = cartItem.post.title.ifEmpty { cartItem.post.producto.titulo },
                    imageUrl = cartItem.post.images.firstOrNull(),
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.post.price,
                    totalPrice = cartItem.totalPrice,
                    selectedColor = cartItem.selectedColor,
                    selectedSize = cartItem.selectedSize
                )
                
                val itemResult = SupabaseClient.database
                    .from("order_items")
                    .insert(itemInsert) {
                        select()
                    }
                    .decodeSingle<OrderItemDB>()
                
                orderItems.add(OrderItem.fromDB(itemResult))
            }
            
            // Crear registro de pago pendiente
            val externalRef = "Merqora-${orderResult.id.take(8).uppercase()}-${System.currentTimeMillis()}"
            val paymentInsert = PaymentInsert(
                orderId = orderResult.id,
                amount = totalAmount,
                mpExternalReference = externalRef
            )
            
            val paymentResult = SupabaseClient.database
                .from("payments")
                .insert(paymentInsert) {
                    select()
                }
                .decodeSingle<PaymentDB>()
            
            val order = Order.fromDB(
                db = orderResult,
                items = orderItems,
                payment = Payment.fromDB(paymentResult)
            )
            
            _currentOrder.value = order
            
            Log.d(TAG, "✅ Orden completa con ${orderItems.size} items")
            Result.success(order)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando orden: ${e.message}", e)
            Result.failure(e)
        } finally {
            _isProcessing.value = false
        }
    }
    
    /**
     * Obtener orden por ID
     */
    suspend fun getOrderById(orderId: String): Order? = withContext(Dispatchers.IO) {
        try {
            val orderDB = SupabaseClient.database
                .from("orders")
                .select {
                    filter { eq("id", orderId) }
                }
                .decodeSingleOrNull<OrderDB>()
                ?: return@withContext null
            
            val itemsDB = SupabaseClient.database
                .from("order_items")
                .select {
                    filter { eq("order_id", orderId) }
                }
                .decodeList<OrderItemDB>()
            
            val paymentDB = SupabaseClient.database
                .from("payments")
                .select {
                    filter { eq("order_id", orderId) }
                }
                .decodeSingleOrNull<PaymentDB>()
            
            Order.fromDB(
                db = orderDB,
                items = itemsDB.map { OrderItem.fromDB(it) },
                payment = paymentDB?.let { Payment.fromDB(it) }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo orden: ${e.message}")
            null
        }
    }
    
    /**
     * Obtener órdenes del usuario actual
     */
    suspend fun getMyOrders(): List<Order> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()
            
            val ordersDB = SupabaseClient.database
                .from("orders")
                .select {
                    filter { eq("buyer_id", currentUserId) }
                }
                .decodeList<OrderDB>()
            
            if (ordersDB.isEmpty()) return@withContext emptyList()
            
            // Batch: traer TODOS los items de todas las órdenes en UNA sola query
            val orderIds = ordersDB.map { it.id }
            val allItemsDB = SupabaseClient.database
                .from("order_items")
                .select {
                    filter { isIn("order_id", orderIds) }
                }
                .decodeList<OrderItemDB>()
            
            // Agrupar items por order_id
            val itemsByOrder = allItemsDB.groupBy { it.orderId }
            
            ordersDB.map { orderDB ->
                val items = itemsByOrder[orderDB.id]?.map { OrderItem.fromDB(it) } ?: emptyList()
                Order.fromDB(db = orderDB, items = items)
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo órdenes: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Actualizar estado del pago (llamado después de callback de Mercado Pago)
     */
    suspend fun updatePaymentStatus(
        orderId: String,
        mpPaymentId: String,
        status: PaymentStatus,
        statusDetail: String? = null,
        paymentMethodId: String? = null,
        installments: Int = 1
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = java.time.OffsetDateTime.now().toString()
            
            // Actualizar pago con timestamp real
            val paymentUpdate = mutableMapOf<String, Any?>(
                "mp_payment_id" to mpPaymentId,
                "status" to status.name.lowercase(),
                "status_detail" to statusDetail,
                "payment_method_id" to paymentMethodId,
                "installments" to installments,
                "updated_at" to now
            )
            
            if (status == PaymentStatus.APPROVED) {
                paymentUpdate["approved_at"] = now
            }
            
            SupabaseClient.database
                .from("payments")
                .update(paymentUpdate) {
                    filter { eq("order_id", orderId) }
                }
            
            // Si el pago fue aprobado, actualizar estado de la orden
            if (status == PaymentStatus.APPROVED) {
                SupabaseClient.database
                    .from("orders")
                    .update(mapOf(
                        "status" to "paid",
                        "paid_at" to now,
                        "updated_at" to now
                    )) {
                        filter { eq("id", orderId) }
                    }
                
                // Actualizar stats del vendedor
                updateSellerStatsForOrder(orderId)
                
                Log.d(TAG, "✅ Orden $orderId marcada como pagada")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando pago: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar stats del vendedor después de una venta
     */
    private suspend fun updateSellerStatsForOrder(orderId: String) {
        try {
            // Obtener items de la orden para saber los vendedores
            val items = SupabaseClient.database
                .from("order_items")
                .select {
                    filter { eq("order_id", orderId) }
                }
                .decodeList<OrderItemDB>()
            
            // Agrupar por vendedor
            val sellerIds = items.map { it.sellerId }.distinct()
            
            for (sellerId in sellerIds) {
                val sellerItems = items.filter { it.sellerId == sellerId }
                val sellerRevenue = sellerItems.sumOf { it.totalPrice }
                
                // Intentar actualizar o insertar stats
                try {
                    SupabaseClient.database
                        .from("seller_stats")
                        .upsert(mapOf(
                            "user_id" to sellerId,
                            "total_sales" to 1,
                            "total_revenue" to sellerRevenue,
                            "completed_orders" to 1,
                            "updated_at" to java.time.OffsetDateTime.now().toString()
                        ))
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo actualizar stats del vendedor: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando stats de vendedores: ${e.message}")
        }
    }
    
    /**
     * Obtener mis ventas (como vendedor)
     */
    suspend fun getMySales(): List<Order> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()
            
            // Obtener order_items donde soy el vendedor
            val myItems = SupabaseClient.database
                .from("order_items")
                .select {
                    filter { eq("seller_id", currentUserId) }
                }
                .decodeList<OrderItemDB>()
            
            if (myItems.isEmpty()) return@withContext emptyList()
            
            // Obtener las órdenes únicas - batch en UNA query
            val orderIds = myItems.map { it.orderId }.distinct()
            val allOrdersDB = SupabaseClient.database
                .from("orders")
                .select {
                    filter { isIn("id", orderIds) }
                }
                .decodeList<OrderDB>()
            
            if (allOrdersDB.isEmpty()) return@withContext emptyList()
            
            // Batch: obtener info de compradores en UNA query
            val buyerIds = allOrdersDB.map { it.buyerId }.distinct()
            val buyersMap = try {
                SupabaseClient.database
                    .from("usuarios")
                    .select(columns = Columns.list("id", "username", "avatar_url")) {
                        filter { isIn("id", buyerIds) }
                    }
                    .decodeList<BuyerInfoDB>()
                    .associateBy { it.id }
            } catch (_: Exception) { emptyMap() }
            
            // Batch: obtener pagos en UNA query
            val allPayments = try {
                SupabaseClient.database
                    .from("payments")
                    .select {
                        filter { isIn("order_id", orderIds) }
                    }
                    .decodeList<PaymentDB>()
                    .associateBy { it.orderId }
            } catch (_: Exception) { emptyMap<String, PaymentDB>() }
            
            // Agrupar items por orderId
            val itemsByOrder = myItems.groupBy { it.orderId }
            
            allOrdersDB.map { orderDB ->
                val orderItems = itemsByOrder[orderDB.id]?.map { OrderItem.fromDB(it) } ?: emptyList()
                val buyerInfo = buyersMap[orderDB.buyerId]
                val paymentDB = allPayments[orderDB.id]
                
                Order.fromDB(
                    db = orderDB,
                    items = orderItems,
                    payment = paymentDB?.let { Payment.fromDB(it) }
                ).copy(
                    buyerUsername = buyerInfo?.username,
                    buyerAvatarUrl = buyerInfo?.avatarUrl
                )
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo ventas: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtener estadísticas de un vendedor
     */
    suspend fun getSellerStats(sellerId: String): SellerStats = withContext(Dispatchers.IO) {
        try {
            val statsDB = SupabaseClient.database
                .from("seller_stats")
                .select {
                    filter { eq("user_id", sellerId) }
                }
                .decodeSingleOrNull<SellerStatsDB>()
            
            statsDB?.let { SellerStats.fromDB(it) } ?: SellerStats.default(sellerId)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo stats del vendedor: ${e.message}")
            SellerStats.default(sellerId)
        }
    }
    
    /**
     * Cancelar orden (solo si está pendiente)
     */
    suspend fun cancelOrder(orderId: String, reason: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val order = getOrderById(orderId)
            if (order == null) {
                return@withContext Result.failure(Exception("Orden no encontrada"))
            }
            
            if (order.status != OrderStatus.PENDING && order.status != OrderStatus.PAYMENT_PROCESSING) {
                return@withContext Result.failure(Exception("No se puede cancelar una orden ${order.status}"))
            }
            
            SupabaseClient.database
                .from("orders")
                .update(mapOf(
                    "status" to "cancelled",
                    "cancelled_at" to "now()"
                )) {
                    filter { eq("id", orderId) }
                }
            
            // Actualizar pago también
            SupabaseClient.database
                .from("payments")
                .update(mapOf("status" to "cancelled")) {
                    filter { eq("order_id", orderId) }
                }
            
            Log.d(TAG, "✅ Orden $orderId cancelada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelando orden: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Limpiar orden actual
     */
    fun clearCurrentOrder() {
        _currentOrder.value = null
    }
    
    /**
     * Obtener resumen de transacciones del usuario
     */
    suspend fun getTransactionsSummary(): TransactionsSummary = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext TransactionsSummary.empty()
            
            // Obtener compras
            val purchases = getMyOrders()
            val purchasesTotal = purchases.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.COMPLETED || it.status == OrderStatus.DELIVERED }
                .sumOf { it.totalAmount }
            
            // Obtener ventas
            val sales = getMySales()
            val salesTotal = sales.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.COMPLETED || it.status == OrderStatus.DELIVERED }
                .sumOf { order -> order.items.sumOf { it.totalPrice } }
            
            TransactionsSummary(
                totalPurchases = purchases.size,
                totalPurchasesAmount = purchasesTotal,
                totalSales = sales.size,
                totalSalesAmount = salesTotal,
                pendingPurchases = purchases.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAYMENT_PROCESSING },
                pendingSales = sales.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAYMENT_PROCESSING }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo resumen: ${e.message}")
            TransactionsSummary.empty()
        }
    }
    
    /**
     * Marcar orden como enviada (para vendedores)
     */
    suspend fun markAsShipped(
        orderId: String,
        trackingNumber: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = java.time.OffsetDateTime.now().toString()
            
            SupabaseClient.database
                .from("orders")
                .update(mapOf(
                    "status" to "shipped",
                    "shipped_at" to now,
                    "tracking_number" to trackingNumber,
                    "updated_at" to now
                )) {
                    filter { eq("id", orderId) }
                }
            
            Log.d(TAG, "✅ Orden $orderId marcada como enviada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marcando como enviada: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Marcar orden como entregada
     */
    suspend fun markAsDelivered(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val now = java.time.OffsetDateTime.now().toString()
            
            SupabaseClient.database
                .from("orders")
                .update(mapOf(
                    "status" to "delivered",
                    "delivered_at" to now,
                    "updated_at" to now
                )) {
                    filter { eq("id", orderId) }
                }
            
            Log.d(TAG, "✅ Orden $orderId marcada como entregada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marcando como entregada: ${e.message}")
            Result.failure(e)
        }
    }
}

// Modelo auxiliar para info del comprador
@kotlinx.serialization.Serializable
data class BuyerInfoDB(
    val id: String,
    val username: String? = null,
    @kotlinx.serialization.SerialName("avatar_url") val avatarUrl: String? = null
)

// Resumen de transacciones
data class TransactionsSummary(
    val totalPurchases: Int,
    val totalPurchasesAmount: Double,
    val totalSales: Int,
    val totalSalesAmount: Double,
    val pendingPurchases: Int,
    val pendingSales: Int
) {
    companion object {
        fun empty() = TransactionsSummary(0, 0.0, 0, 0.0, 0, 0)
    }
}
