package com.rendly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODELOS DE ÓRDENES Y PAGOS - Sistema de compras Merqora
 * ═══════════════════════════════════════════════════════════════════════════════
 */

// Estados de orden
enum class OrderStatus {
    PENDING,
    PAYMENT_PROCESSING,
    PAID,
    PREPARING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REFUNDED
}

// Estados de pago
enum class PaymentStatus {
    PENDING,
    IN_PROCESS,
    APPROVED,
    REJECTED,
    CANCELLED,
    REFUNDED,
    CHARGED_BACK
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODELOS DB (Supabase)
// ═══════════════════════════════════════════════════════════════════════════════

@Serializable
data class OrderDB(
    val id: String = "",
    @SerialName("order_number") val orderNumber: String = "",
    @SerialName("buyer_id") val buyerId: String,
    val subtotal: Double = 0.0,
    @SerialName("shipping_cost") val shippingCost: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    val currency: String = "UYU",
    val status: String = "pending",
    @SerialName("shipping_address_id") val shippingAddressId: String? = null,
    @SerialName("shipping_method") val shippingMethod: String? = "standard",
    @SerialName("tracking_number") val trackingNumber: String? = null,
    @SerialName("buyer_notes") val buyerNotes: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("shipped_at") val shippedAt: String? = null,
    @SerialName("delivered_at") val deliveredAt: String? = null
)

@Serializable
data class OrderInsert(
    @SerialName("buyer_id") val buyerId: String,
    val subtotal: Double,
    @SerialName("shipping_cost") val shippingCost: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double,
    val currency: String = "UYU",
    @SerialName("shipping_address_id") val shippingAddressId: String? = null,
    @SerialName("shipping_method") val shippingMethod: String? = "standard",
    @SerialName("buyer_notes") val buyerNotes: String? = null
)

@Serializable
data class OrderItemDB(
    val id: String = "",
    @SerialName("order_id") val orderId: String,
    @SerialName("post_id") val postId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("seller_id") val sellerId: String,
    val title: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val quantity: Int = 1,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("selected_color") val selectedColor: String? = null,
    @SerialName("selected_size") val selectedSize: String? = null,
    @SerialName("item_status") val itemStatus: String = "pending",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class OrderItemInsert(
    @SerialName("order_id") val orderId: String,
    @SerialName("post_id") val postId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("seller_id") val sellerId: String,
    val title: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val quantity: Int = 1,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("selected_color") val selectedColor: String? = null,
    @SerialName("selected_size") val selectedSize: String? = null
)

@Serializable
data class PaymentDB(
    val id: String = "",
    @SerialName("order_id") val orderId: String,
    @SerialName("mp_payment_id") val mpPaymentId: String? = null,
    @SerialName("mp_preference_id") val mpPreferenceId: String? = null,
    @SerialName("mp_external_reference") val mpExternalReference: String? = null,
    val amount: Double,
    val currency: String = "UYU",
    val status: String = "pending",
    @SerialName("status_detail") val statusDetail: String? = null,
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
    @SerialName("payment_type") val paymentType: String? = null,
    val installments: Int = 1,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("approved_at") val approvedAt: String? = null
)

@Serializable
data class PaymentInsert(
    @SerialName("order_id") val orderId: String,
    val amount: Double,
    val currency: String = "UYU",
    @SerialName("mp_external_reference") val mpExternalReference: String? = null
)

@Serializable
data class SellerStatsDB(
    @SerialName("user_id") val userId: String,
    @SerialName("total_sales") val totalSales: Int = 0,
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("completed_orders") val completedOrders: Int = 0,
    @SerialName("cancelled_orders") val cancelledOrders: Int = 0,
    @SerialName("avg_response_time_minutes") val avgResponseTimeMinutes: Int? = null,
    @SerialName("avg_shipping_time_hours") val avgShippingTimeHours: Int? = null,
    @SerialName("total_ratings") val totalRatings: Int = 0,
    @SerialName("sum_ratings") val sumRatings: Int = 0,
    @SerialName("avg_rating") val avgRating: Double? = null,
    @SerialName("reputation_score") val reputationScore: Int = 70,
    @SerialName("updated_at") val updatedAt: String = ""
)

// ═══════════════════════════════════════════════════════════════════════════════
// MODELOS UI
// ═══════════════════════════════════════════════════════════════════════════════

data class Order(
    val id: String,
    val orderNumber: String,
    val buyerId: String,
    val subtotal: Double,
    val shippingCost: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val currency: String,
    val status: OrderStatus,
    val items: List<OrderItem> = emptyList(),
    val payment: Payment? = null,
    val createdAt: String,
    val paidAt: String? = null,
    val shippedAt: String? = null,
    val deliveredAt: String? = null,
    val trackingNumber: String? = null,
    val buyerUsername: String? = null,
    val buyerAvatarUrl: String? = null
) {
    val statusDisplayName: String
        get() = when (status) {
            OrderStatus.PENDING -> "Pendiente"
            OrderStatus.PAYMENT_PROCESSING -> "Procesando pago"
            OrderStatus.PAID -> "Pagado"
            OrderStatus.PREPARING -> "Preparando"
            OrderStatus.SHIPPED -> "Enviado"
            OrderStatus.DELIVERED -> "Entregado"
            OrderStatus.COMPLETED -> "Completado"
            OrderStatus.CANCELLED -> "Cancelado"
            OrderStatus.REFUNDED -> "Reembolsado"
        }
    
    val formattedDate: String
        get() {
            return try {
                val instant = java.time.Instant.parse(createdAt)
                val zoned = instant.atZone(java.time.ZoneId.systemDefault())
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", java.util.Locale("es", "ES"))
                zoned.format(formatter)
            } catch (e: Exception) {
                createdAt.take(10)
            }
        }
    
    companion object {
        fun fromDB(db: OrderDB, items: List<OrderItem> = emptyList(), payment: Payment? = null): Order {
            return Order(
                id = db.id,
                orderNumber = db.orderNumber,
                buyerId = db.buyerId,
                subtotal = db.subtotal,
                shippingCost = db.shippingCost,
                discountAmount = db.discountAmount,
                totalAmount = db.totalAmount,
                currency = db.currency,
                status = try { OrderStatus.valueOf(db.status.uppercase()) } catch (e: Exception) { OrderStatus.PENDING },
                items = items,
                payment = payment,
                createdAt = db.createdAt,
                paidAt = db.paidAt,
                shippedAt = db.shippedAt,
                deliveredAt = db.deliveredAt,
                trackingNumber = db.trackingNumber
            )
        }
    }
}

data class OrderItem(
    val id: String,
    val orderId: String,
    val postId: String,
    val sellerId: String,
    val title: String,
    val imageUrl: String?,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val selectedColor: String?,
    val selectedSize: String?,
    val sellerUsername: String? = null,
    val sellerAvatar: String? = null
) {
    companion object {
        fun fromDB(db: OrderItemDB): OrderItem {
            return OrderItem(
                id = db.id,
                orderId = db.orderId,
                postId = db.postId,
                sellerId = db.sellerId,
                title = db.title,
                imageUrl = db.imageUrl,
                quantity = db.quantity,
                unitPrice = db.unitPrice,
                totalPrice = db.totalPrice,
                selectedColor = db.selectedColor,
                selectedSize = db.selectedSize
            )
        }
    }
}

data class Payment(
    val id: String,
    val orderId: String,
    val mpPaymentId: String?,
    val mpPreferenceId: String?,
    val amount: Double,
    val currency: String,
    val status: PaymentStatus,
    val paymentMethodId: String?,
    val installments: Int
) {
    companion object {
        fun fromDB(db: PaymentDB): Payment {
            return Payment(
                id = db.id,
                orderId = db.orderId,
                mpPaymentId = db.mpPaymentId,
                mpPreferenceId = db.mpPreferenceId,
                amount = db.amount,
                currency = db.currency,
                status = try { PaymentStatus.valueOf(db.status.uppercase()) } catch (e: Exception) { PaymentStatus.PENDING },
                paymentMethodId = db.paymentMethodId,
                installments = db.installments
            )
        }
    }
}

data class SellerStats(
    val userId: String,
    val totalSales: Int,
    val completedOrders: Int,
    val avgRating: Double?,
    val reputationScore: Int,
    val avgResponseTimeMinutes: Int?
) {
    val formattedResponseTime: String
        get() = when {
            avgResponseTimeMinutes == null -> "N/A"
            avgResponseTimeMinutes < 60 -> "<1h"
            avgResponseTimeMinutes < 1440 -> "${avgResponseTimeMinutes / 60}h"
            else -> "${avgResponseTimeMinutes / 1440}d"
        }
    
    val formattedSales: String
        get() = when {
            totalSales >= 1000000 -> String.format("%.1fM", totalSales / 1000000.0)
            totalSales >= 1000 -> String.format("%.1fk", totalSales / 1000.0)
            else -> totalSales.toString()
        }
    
    companion object {
        fun fromDB(db: SellerStatsDB): SellerStats {
            return SellerStats(
                userId = db.userId,
                totalSales = db.totalSales,
                completedOrders = db.completedOrders,
                avgRating = db.avgRating,
                reputationScore = db.reputationScore,
                avgResponseTimeMinutes = db.avgResponseTimeMinutes
            )
        }
        
        // Default stats for new sellers
        fun default(userId: String) = SellerStats(
            userId = userId,
            totalSales = 0,
            completedOrders = 0,
            avgRating = null,
            reputationScore = 70,
            avgResponseTimeMinutes = null
        )
    }
}
