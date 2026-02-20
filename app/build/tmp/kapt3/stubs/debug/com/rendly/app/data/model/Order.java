package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b7\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\b\u0018\u0000 O2\u00020\u0001:\u0001OB\u00b9\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0007\u0012\u0006\u0010\t\u001a\u00020\u0007\u0012\u0006\u0010\n\u001a\u00020\u0007\u0012\u0006\u0010\u000b\u001a\u00020\u0003\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u0012\u0006\u0010\u0013\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u001aJ\t\u00106\u001a\u00020\u0003H\u00c6\u0003J\u000f\u00107\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fH\u00c6\u0003J\u000b\u00108\u001a\u0004\u0018\u00010\u0012H\u00c6\u0003J\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010<\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010>\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010?\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010@\u001a\u00020\u0003H\u00c6\u0003J\t\u0010A\u001a\u00020\u0003H\u00c6\u0003J\t\u0010B\u001a\u00020\u0007H\u00c6\u0003J\t\u0010C\u001a\u00020\u0007H\u00c6\u0003J\t\u0010D\u001a\u00020\u0007H\u00c6\u0003J\t\u0010E\u001a\u00020\u0007H\u00c6\u0003J\t\u0010F\u001a\u00020\u0003H\u00c6\u0003J\t\u0010G\u001a\u00020\rH\u00c6\u0003J\u00d1\u0001\u0010H\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\u00072\b\b\u0002\u0010\n\u001a\u00020\u00072\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00032\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010I\u001a\u00020J2\b\u0010K\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010L\u001a\u00020MH\u00d6\u0001J\t\u0010N\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001cR\u0013\u0010\u0018\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001cR\u0011\u0010\u0013\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001cR\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001cR\u0013\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001cR\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010$\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b%\u0010\u001cR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001cR\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001cR\u0013\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001cR\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001cR\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010#R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0011\u00101\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b2\u0010\u001cR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010#R\u0011\u0010\n\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010#R\u0013\u0010\u0017\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010\u001c\u00a8\u0006P"}, d2 = {"Lcom/rendly/app/data/model/Order;", "", "id", "", "orderNumber", "buyerId", "subtotal", "", "shippingCost", "discountAmount", "totalAmount", "currency", "status", "Lcom/rendly/app/data/model/OrderStatus;", "items", "", "Lcom/rendly/app/data/model/OrderItem;", "payment", "Lcom/rendly/app/data/model/Payment;", "createdAt", "paidAt", "shippedAt", "deliveredAt", "trackingNumber", "buyerUsername", "buyerAvatarUrl", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DDDDLjava/lang/String;Lcom/rendly/app/data/model/OrderStatus;Ljava/util/List;Lcom/rendly/app/data/model/Payment;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getBuyerAvatarUrl", "()Ljava/lang/String;", "getBuyerId", "getBuyerUsername", "getCreatedAt", "getCurrency", "getDeliveredAt", "getDiscountAmount", "()D", "formattedDate", "getFormattedDate", "getId", "getItems", "()Ljava/util/List;", "getOrderNumber", "getPaidAt", "getPayment", "()Lcom/rendly/app/data/model/Payment;", "getShippedAt", "getShippingCost", "getStatus", "()Lcom/rendly/app/data/model/OrderStatus;", "statusDisplayName", "getStatusDisplayName", "getSubtotal", "getTotalAmount", "getTrackingNumber", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "", "toString", "Companion", "app_debug"})
public final class Order {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String orderNumber = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String buyerId = null;
    private final double subtotal = 0.0;
    private final double shippingCost = 0.0;
    private final double discountAmount = 0.0;
    private final double totalAmount = 0.0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String currency = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.OrderStatus status = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.rendly.app.data.model.OrderItem> items = null;
    @org.jetbrains.annotations.Nullable
    private final com.rendly.app.data.model.Payment payment = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String paidAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String shippedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String deliveredAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String trackingNumber = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String buyerUsername = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String buyerAvatarUrl = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.Order.Companion Companion = null;
    
    public Order(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String orderNumber, @org.jetbrains.annotations.NotNull
    java.lang.String buyerId, double subtotal, double shippingCost, double discountAmount, double totalAmount, @org.jetbrains.annotations.NotNull
    java.lang.String currency, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.OrderStatus status, @org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.model.OrderItem> items, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.Payment payment, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String paidAt, @org.jetbrains.annotations.Nullable
    java.lang.String shippedAt, @org.jetbrains.annotations.Nullable
    java.lang.String deliveredAt, @org.jetbrains.annotations.Nullable
    java.lang.String trackingNumber, @org.jetbrains.annotations.Nullable
    java.lang.String buyerUsername, @org.jetbrains.annotations.Nullable
    java.lang.String buyerAvatarUrl) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getOrderNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getBuyerId() {
        return null;
    }
    
    public final double getSubtotal() {
        return 0.0;
    }
    
    public final double getShippingCost() {
        return 0.0;
    }
    
    public final double getDiscountAmount() {
        return 0.0;
    }
    
    public final double getTotalAmount() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCurrency() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.OrderStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.rendly.app.data.model.OrderItem> getItems() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.Payment getPayment() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCreatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPaidAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getShippedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDeliveredAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getTrackingNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getBuyerUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getBuyerAvatarUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStatusDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.rendly.app.data.model.OrderItem> component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.Payment component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    public final double component4() {
        return 0.0;
    }
    
    public final double component5() {
        return 0.0;
    }
    
    public final double component6() {
        return 0.0;
    }
    
    public final double component7() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.OrderStatus component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.Order copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String orderNumber, @org.jetbrains.annotations.NotNull
    java.lang.String buyerId, double subtotal, double shippingCost, double discountAmount, double totalAmount, @org.jetbrains.annotations.NotNull
    java.lang.String currency, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.OrderStatus status, @org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.model.OrderItem> items, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.Payment payment, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String paidAt, @org.jetbrains.annotations.Nullable
    java.lang.String shippedAt, @org.jetbrains.annotations.Nullable
    java.lang.String deliveredAt, @org.jetbrains.annotations.Nullable
    java.lang.String trackingNumber, @org.jetbrains.annotations.Nullable
    java.lang.String buyerUsername, @org.jetbrains.annotations.Nullable
    java.lang.String buyerAvatarUrl) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J*\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/rendly/app/data/model/Order$Companion;", "", "()V", "fromDB", "Lcom/rendly/app/data/model/Order;", "db", "Lcom/rendly/app/data/model/OrderDB;", "items", "", "Lcom/rendly/app/data/model/OrderItem;", "payment", "Lcom/rendly/app/data/model/Payment;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Order fromDB(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.OrderDB db, @org.jetbrains.annotations.NotNull
        java.util.List<com.rendly.app.data.model.OrderItem> items, @org.jetbrains.annotations.Nullable
        com.rendly.app.data.model.Payment payment) {
            return null;
        }
    }
}