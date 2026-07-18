package com.vinzay.app.data.repository

import android.util.Log
import com.vinzay.app.data.cache.core.MemoryCache
import com.vinzay.app.data.model.*
import com.vinzay.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import java.util.UUID

/**
 * -------------------------------------------------------------------------------
 * ORDER REPOSITORY - Gestión de órdenes y pagos
 * -------------------------------------------------------------------------------
 */
object OrderRepository {
    private const val TAG = "OrderRepository"
    
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    // Cache para transacciones - carga instantánea
    private val _cachedPurchases = MutableStateFlow<List<Order>>(emptyList())
    
    // Cache para estadísticas de vendedor - TTL 2 minutos (carga inmediata al reabrir)
    private val sellerStatsCache = MemoryCache<String, SellerStats>(
        maxSize = 200,
        defaultTtlMs = 120_000L,
        tag = "SellerStatsCache"
    )
    val cachedPurchases: StateFlow<List<Order>> = _cachedPurchases
    
    private val _cachedSales = MutableStateFlow<List<Order>>(emptyList())
    val cachedSales: StateFlow<List<Order>> = _cachedSales
    
    private val _cachedSummary = MutableStateFlow(TransactionsSummary.empty())
    val cachedSummary: StateFlow<TransactionsSummary> = _cachedSummary
    
    private val _cachedHandshakes = MutableStateFlow<List<HandshakeTransaction>>(emptyList())
    val cachedHandshakes: StateFlow<List<HandshakeTransaction>> = _cachedHandshakes
    
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
                val handshakes = loadUserHandshakes()
                _cachedPurchases.value = purchases
                _cachedSales.value = sales
                _cachedHandshakes.value = handshakes
                _cachedSummary.value = buildSummary(purchases, sales, handshakes)
            } catch (_: Exception) {}
            _isRefreshing.value = false
            return
        }
        
        // Primera carga o force refresh
        _isRefreshing.value = true
        try {
            val purchases = getMyOrders()
            val sales = getMySales()
            val handshakes = loadUserHandshakes()
            _cachedPurchases.value = purchases
            _cachedSales.value = sales
            _cachedHandshakes.value = handshakes
            _cachedSummary.value = buildSummary(purchases, sales, handshakes)
            _isCacheLoaded.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando transacciones: ${e.message}")
        }
        _isRefreshing.value = false
    }
    
    private fun buildSummary(purchases: List<Order>, sales: List<Order>, handshakes: List<HandshakeTransaction> = emptyList()): TransactionsSummary {
        val purchasesTotal = purchases.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.COMPLETED || it.status == OrderStatus.DELIVERED }
            .sumOf { it.totalAmount }
        val salesTotal = sales.filter { it.status == OrderStatus.PAID || it.status == OrderStatus.COMPLETED || it.status == OrderStatus.DELIVERED }
            .sumOf { order -> order.items.sumOf { it.totalPrice } }
        val handshakesTotal = handshakes.filter { it.getStatusEnum() == HandshakeStatus.COMPLETED }
            .sumOf { it.agreedPrice }
        val pendingHandshakes = handshakes.count { 
            it.getStatusEnum() in listOf(HandshakeStatus.PROPOSED, HandshakeStatus.ACCEPTED, HandshakeStatus.IN_PROGRESS, HandshakeStatus.RENEGOTIATING)
        }
        return TransactionsSummary(
            totalPurchases = purchases.size,
            totalPurchasesAmount = purchasesTotal,
            totalSales = sales.size,
            totalSalesAmount = salesTotal,
            pendingPurchases = purchases.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAYMENT_PROCESSING },
            pendingSales = sales.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAYMENT_PROCESSING },
            totalHandshakes = handshakes.size,
            totalHandshakesAmount = handshakesTotal,
            pendingHandshakes = pendingHandshakes
        )
    }
    
    /**
     * Cargar todos los handshake transactions del usuario
     */
    private suspend fun loadUserHandshakes(): List<HandshakeTransaction> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()
            
            val handshakes = SupabaseClient.database
                .from("handshake_transactions")
                .select {
                    filter {
                        or {
                            eq("initiator_id", currentUserId)
                            eq("receiver_id", currentUserId)
                        }
                    }
                }
                .decodeList<HandshakeTransaction>()
                .sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Loaded ${handshakes.size} handshake transactions")
            handshakes
        } catch (e: Exception) {
            Log.e(TAG, "Error loading handshakes: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Invalidar cache (llamar después de crear orden, marcar enviado, etc.)
     */
    fun invalidateCache() {
        _isCacheLoaded.value = false
    }
    
    /**
     * Cargar handshake vinculado a una orden desde Supabase
     */
    suspend fun loadHandshakeForOrder(handshakeId: String): HandshakeTransaction? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.database
                .from("handshake_transactions")
                .select {
                    filter { eq("id", handshakeId) }
                }
                .decodeSingleOrNull<HandshakeTransaction>()
            Log.d(TAG, "Loaded handshake for order: ${result?.id} status=${result?.status}")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error loading handshake: ${e.message}")
            null
        }
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
            val allItemsFreeShipping = cartItems.all { it.post.freeShipping }
            val shippingCost = if (allItemsFreeShipping) 0.0 else 500.0
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
            
            Log.d(TAG, "? Orden creada: ${orderResult.orderNumber}")
            
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
            val externalRef = "Vinzay-${orderResult.id.take(8).uppercase()}-${System.currentTimeMillis()}"
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
            
            Log.d(TAG, "? Orden completa con ${orderItems.size} items")
            Result.success(order)
            
        } catch (e: Exception) {
            Log.e(TAG, "? Error creando orden: ${e.message}", e)
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
                
                Log.d(TAG, "? Orden $orderId marcada como pagada")
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
     * Obtener estadísticas de un vendedor - combina seller_stats + rating real de product_reviews + response time de messages
     */
    suspend fun getSellerStats(sellerId: String): SellerStats = withContext(Dispatchers.IO) {
        val cached = sellerStatsCache.get(sellerId)
        if (cached != null) {
            Log.d(TAG, "getSellerStats: CACHE HIT for $sellerId")
            return@withContext cached
        }
        Log.d(TAG, "getSellerStats: CACHE MISS for $sellerId, fetching from network")
        val fresh = fetchSellerStats(sellerId)
        sellerStatsCache.put(sellerId, fresh)
        fresh
    }
    
    /**
     * Fetches fresh seller stats from Supabase (network only).
     */
    private suspend fun fetchSellerStats(sellerId: String): SellerStats = try {
        val statsDB = SupabaseClient.database
                .from("seller_stats")
                .select {
                    filter { eq("user_id", sellerId) }
                }
                .decodeSingleOrNull<SellerStatsDB>()
            
            val baseStats = statsDB?.let { SellerStats.fromDB(it) } ?: SellerStats.default(sellerId)
            
            // Compute real reputation: read usuarios.reputation_score first (same as ProfileRepository),
            // then fall back to FollowersRepository.getReputation()
            val realReputation = try {
                val userRow = SupabaseClient.database
                    .from("usuarios")
                    .select(columns = Columns.list("reputation_score")) {
                        filter { eq("user_id", sellerId) }
                    }
                    .decodeSingleOrNull<UserReputationRow>()
                userRow?.reputationScore?.toInt()
                    ?: FollowersRepository.getReputation(sellerId)
            } catch (e: Exception) {
                Log.e(TAG, "Error computing real reputation: ${e.message}")
                null
            }
            
            // Compute real avg rating from product_reviews for all seller's posts
            val realAvgRating = try {
                val sellerPosts = SupabaseClient.database
                    .from("posts")
                    .select(columns = Columns.list("id", "product_id")) {
                        filter { eq("user_id", sellerId) }
                    }
                    .decodeList<SellerPostRow>()
                
                val postIds = sellerPosts.map { it.id }
                val productIds = sellerPosts.mapNotNull { it.productId }
                
                val allReviews = mutableListOf<ReviewRatingRow>()
                if (postIds.isNotEmpty()) {
                    val bySource = SupabaseClient.database
                        .from("product_reviews")
                        .select(columns = Columns.list("id", "rating")) {
                            filter {
                                isIn("source_id", postIds)
                                neq("rating", 0)
                            }
                        }
                        .decodeList<ReviewRatingRow>()
                    allReviews.addAll(bySource)
                }
                if (productIds.isNotEmpty()) {
                    val byProduct = SupabaseClient.database
                        .from("product_reviews")
                        .select(columns = Columns.list("id", "rating")) {
                            filter {
                                isIn("product_id", productIds)
                                neq("rating", 0)
                            }
                        }
                        .decodeList<ReviewRatingRow>()
                    allReviews.addAll(byProduct)
                }
                val uniqueReviews = allReviews.distinctBy { it.id }
                if (uniqueReviews.isNotEmpty()) {
                    uniqueReviews.mapNotNull { it.rating?.toDouble() }.average()
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Error computing real avg rating: ${e.message}")
                null
            }
            
            val realResponseTimeMinutes = try {
                computeAvgResponseTime(sellerId)
            } catch (e: Exception) {
                Log.e(TAG, "Error computing response time: ${e.message}")
                null
            }
            
            baseStats.copy(
                reputationScore = realReputation ?: baseStats.reputationScore,
                avgRating = realAvgRating ?: baseStats.avgRating,
                avgResponseTimeMinutes = realResponseTimeMinutes ?: baseStats.avgResponseTimeMinutes
            )
    } catch (e: Exception) {
        Log.e(TAG, "Error obteniendo stats del vendedor: ${e.message}")
        SellerStats.default(sellerId)
    }
    
    @kotlinx.serialization.Serializable
    private data class ReviewRatingRow(
        val id: String? = null,
        val rating: Int? = null
    )
    
    @kotlinx.serialization.Serializable
    private data class SellerPostRow(
        val id: String = "",
        @kotlinx.serialization.SerialName("product_id") val productId: String? = null
    )
    
    @kotlinx.serialization.Serializable
    private data class UserReputationRow(
        @kotlinx.serialization.SerialName("reputation_score") val reputationScore: Double? = null
    )
    
    @kotlinx.serialization.Serializable
    private data class MessageTimestampRow(
        @kotlinx.serialization.SerialName("sender_id") val senderId: String = "",
        @kotlinx.serialization.SerialName("created_at") val createdAt: String = "",
        @kotlinx.serialization.SerialName("conversation_id") val conversationId: String = ""
    )
    
    /**
     * Compute average response time in minutes for a seller across their conversations
     */
    private suspend fun computeAvgResponseTime(sellerId: String): Int? {
        // Get recent messages where seller replied and compute avg gap to previous buyer message
        val recentReplies = SupabaseClient.database
            .from("messages")
            .select(columns = Columns.list("sender_id", "created_at", "conversation_id")) {
                filter { eq("sender_id", sellerId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(30)
            }
            .decodeList<MessageTimestampRow>()
        
        if (recentReplies.isEmpty()) return null
        
        val responseTimes = mutableListOf<Long>()
        
        for (reply in recentReplies.take(20)) {
            try {
                // Find the last message BEFORE this reply in the same conversation that wasn't from the seller
                val previousMsg = SupabaseClient.database
                    .from("messages")
                    .select(columns = Columns.list("sender_id", "created_at", "conversation_id")) {
                        filter {
                            eq("conversation_id", reply.conversationId)
                            neq("sender_id", sellerId)
                            lt("created_at", reply.createdAt)
                        }
                        order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(1)
                    }
                    .decodeSingleOrNull<MessageTimestampRow>()
                
                if (previousMsg != null) {
                    val replyTime = java.time.Instant.parse(reply.createdAt)
                    val msgTime = java.time.Instant.parse(previousMsg.createdAt)
                    val diffMinutes = java.time.Duration.between(msgTime, replyTime).toMinutes()
                    if (diffMinutes in 0..14400) { // Max 10 days
                        responseTimes.add(diffMinutes)
                    }
                }
            } catch (_: Exception) { }
        }
        
        return if (responseTimes.isNotEmpty()) {
            responseTimes.average().toInt()
        } else null
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
            
            Log.d(TAG, "? Orden $orderId cancelada")
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
            
            Log.d(TAG, "? Orden $orderId marcada como enviada")
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
            
            Log.d(TAG, "? Orden $orderId marcada como entregada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marcando como entregada: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Eliminar orden y sus registros asociados
     */
    suspend fun deleteOrder(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("order_items")
                .delete { filter { eq("order_id", orderId) } }

            SupabaseClient.database
                .from("payments")
                .delete { filter { eq("order_id", orderId) } }

            SupabaseClient.database
                .from("orders")
                .delete { filter { eq("id", orderId) } }

            _cachedPurchases.value = _cachedPurchases.value.filter { it.id != orderId }
            _cachedSales.value = _cachedSales.value.filter { it.id != orderId }

            Log.d(TAG, "??? Orden $orderId eliminada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando orden: ${e.message}")
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
    val pendingSales: Int,
    val totalHandshakes: Int = 0,
    val totalHandshakesAmount: Double = 0.0,
    val pendingHandshakes: Int = 0
) {
    companion object {
        fun empty() = TransactionsSummary(0, 0.0, 0, 0.0, 0, 0, 0, 0.0, 0)
    }
}
