package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODELOS DE ÓRDENES Y PAGOS - Sistema de compras Merqora
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u000b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000b\u00a8\u0006\f"}, d2 = {"Lcom/rendly/app/data/model/OrderStatus;", "", "(Ljava/lang/String;I)V", "PENDING", "PAYMENT_PROCESSING", "PAID", "PREPARING", "SHIPPED", "DELIVERED", "COMPLETED", "CANCELLED", "REFUNDED", "app_debug"})
public enum OrderStatus {
    /*public static final*/ PENDING /* = new PENDING() */,
    /*public static final*/ PAYMENT_PROCESSING /* = new PAYMENT_PROCESSING() */,
    /*public static final*/ PAID /* = new PAID() */,
    /*public static final*/ PREPARING /* = new PREPARING() */,
    /*public static final*/ SHIPPED /* = new SHIPPED() */,
    /*public static final*/ DELIVERED /* = new DELIVERED() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */,
    /*public static final*/ REFUNDED /* = new REFUNDED() */;
    
    OrderStatus() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.OrderStatus> getEntries() {
        return null;
    }
}