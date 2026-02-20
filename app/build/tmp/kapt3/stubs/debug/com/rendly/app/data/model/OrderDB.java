package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b9\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 `2\u00020\u0001:\u0002_`B\u00e1\u0001\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0001\u0010\n\u001a\u00020\t\u0012\b\b\u0001\u0010\u000b\u001a\u00020\t\u0012\b\b\u0001\u0010\f\u001a\u00020\t\u0012\b\u0010\r\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019\u00a2\u0006\u0002\u0010\u001aB\u00c5\u0001\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\b\b\u0002\u0010\u000b\u001a\u00020\t\u0012\b\b\u0002\u0010\f\u001a\u00020\t\u0012\b\b\u0002\u0010\r\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u001bJ\t\u0010?\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010@\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010A\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010B\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010D\u001a\u00020\u0005H\u00c6\u0003J\t\u0010E\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010G\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010I\u001a\u00020\u0005H\u00c6\u0003J\t\u0010J\u001a\u00020\u0005H\u00c6\u0003J\t\u0010K\u001a\u00020\tH\u00c6\u0003J\t\u0010L\u001a\u00020\tH\u00c6\u0003J\t\u0010M\u001a\u00020\tH\u00c6\u0003J\t\u0010N\u001a\u00020\tH\u00c6\u0003J\t\u0010O\u001a\u00020\u0005H\u00c6\u0003J\t\u0010P\u001a\u00020\u0005H\u00c6\u0003J\u00cb\u0001\u0010Q\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\t2\b\b\u0002\u0010\f\u001a\u00020\t2\b\b\u0002\u0010\r\u001a\u00020\u00052\b\b\u0002\u0010\u000e\u001a\u00020\u00052\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0013\u001a\u00020\u00052\b\b\u0002\u0010\u0014\u001a\u00020\u00052\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010R\u001a\u00020S2\b\u0010T\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010U\u001a\u00020\u0003H\u00d6\u0001J\t\u0010V\u001a\u00020\u0005H\u00d6\u0001J&\u0010W\u001a\u00020X2\u0006\u0010Y\u001a\u00020\u00002\u0006\u0010Z\u001a\u00020[2\u0006\u0010\\\u001a\u00020]H\u00c1\u0001\u00a2\u0006\u0002\b^R\u001c\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u001c\u0010\u001d\u001a\u0004\b\u001e\u0010\u001fR\u001e\u0010\u0012\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b \u0010\u001d\u001a\u0004\b!\u0010\u001fR\u001c\u0010\u0013\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\"\u0010\u001d\u001a\u0004\b#\u0010\u001fR\u0011\u0010\r\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001fR\u001e\u0010\u0017\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b%\u0010\u001d\u001a\u0004\b&\u0010\u001fR\u001c\u0010\u000b\u001a\u00020\t8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\'\u0010\u001d\u001a\u0004\b(\u0010)R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001fR\u001c\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b+\u0010\u001d\u001a\u0004\b,\u0010\u001fR\u001e\u0010\u0015\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b-\u0010\u001d\u001a\u0004\b.\u0010\u001fR\u001e\u0010\u0016\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b/\u0010\u001d\u001a\u0004\b0\u0010\u001fR\u001e\u0010\u000f\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b1\u0010\u001d\u001a\u0004\b2\u0010\u001fR\u001c\u0010\n\u001a\u00020\t8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b3\u0010\u001d\u001a\u0004\b4\u0010)R\u001e\u0010\u0010\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b5\u0010\u001d\u001a\u0004\b6\u0010\u001fR\u0011\u0010\u000e\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010\u001fR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010)R\u001c\u0010\f\u001a\u00020\t8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b9\u0010\u001d\u001a\u0004\b:\u0010)R\u001e\u0010\u0011\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b;\u0010\u001d\u001a\u0004\b<\u0010\u001fR\u001c\u0010\u0014\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b=\u0010\u001d\u001a\u0004\b>\u0010\u001f\u00a8\u0006a"}, d2 = {"Lcom/rendly/app/data/model/OrderDB;", "", "seen1", "", "id", "", "orderNumber", "buyerId", "subtotal", "", "shippingCost", "discountAmount", "totalAmount", "currency", "status", "shippingAddressId", "shippingMethod", "trackingNumber", "buyerNotes", "createdAt", "updatedAt", "paidAt", "shippedAt", "deliveredAt", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;DDDDLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DDDDLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getBuyerId$annotations", "()V", "getBuyerId", "()Ljava/lang/String;", "getBuyerNotes$annotations", "getBuyerNotes", "getCreatedAt$annotations", "getCreatedAt", "getCurrency", "getDeliveredAt$annotations", "getDeliveredAt", "getDiscountAmount$annotations", "getDiscountAmount", "()D", "getId", "getOrderNumber$annotations", "getOrderNumber", "getPaidAt$annotations", "getPaidAt", "getShippedAt$annotations", "getShippedAt", "getShippingAddressId$annotations", "getShippingAddressId", "getShippingCost$annotations", "getShippingCost", "getShippingMethod$annotations", "getShippingMethod", "getStatus", "getSubtotal", "getTotalAmount$annotations", "getTotalAmount", "getTrackingNumber$annotations", "getTrackingNumber", "getUpdatedAt$annotations", "getUpdatedAt", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
public final class OrderDB {
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
    private final java.lang.String status = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String shippingAddressId = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String shippingMethod = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String trackingNumber = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String buyerNotes = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String updatedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String paidAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String shippedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String deliveredAt = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.OrderDB.Companion Companion = null;
    
    public OrderDB(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String orderNumber, @org.jetbrains.annotations.NotNull
    java.lang.String buyerId, double subtotal, double shippingCost, double discountAmount, double totalAmount, @org.jetbrains.annotations.NotNull
    java.lang.String currency, @org.jetbrains.annotations.NotNull
    java.lang.String status, @org.jetbrains.annotations.Nullable
    java.lang.String shippingAddressId, @org.jetbrains.annotations.Nullable
    java.lang.String shippingMethod, @org.jetbrains.annotations.Nullable
    java.lang.String trackingNumber, @org.jetbrains.annotations.Nullable
    java.lang.String buyerNotes, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.NotNull
    java.lang.String updatedAt, @org.jetbrains.annotations.Nullable
    java.lang.String paidAt, @org.jetbrains.annotations.Nullable
    java.lang.String shippedAt, @org.jetbrains.annotations.Nullable
    java.lang.String deliveredAt) {
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
    
    @kotlinx.serialization.SerialName(value = "order_number")
    @java.lang.Deprecated
    public static void getOrderNumber$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getBuyerId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "buyer_id")
    @java.lang.Deprecated
    public static void getBuyerId$annotations() {
    }
    
    public final double getSubtotal() {
        return 0.0;
    }
    
    public final double getShippingCost() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "shipping_cost")
    @java.lang.Deprecated
    public static void getShippingCost$annotations() {
    }
    
    public final double getDiscountAmount() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "discount_amount")
    @java.lang.Deprecated
    public static void getDiscountAmount$annotations() {
    }
    
    public final double getTotalAmount() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "total_amount")
    @java.lang.Deprecated
    public static void getTotalAmount$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCurrency() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getShippingAddressId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "shipping_address_id")
    @java.lang.Deprecated
    public static void getShippingAddressId$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getShippingMethod() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "shipping_method")
    @java.lang.Deprecated
    public static void getShippingMethod$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getTrackingNumber() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "tracking_number")
    @java.lang.Deprecated
    public static void getTrackingNumber$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getBuyerNotes() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "buyer_notes")
    @java.lang.Deprecated
    public static void getBuyerNotes$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCreatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "created_at")
    @java.lang.Deprecated
    public static void getCreatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUpdatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "updated_at")
    @java.lang.Deprecated
    public static void getUpdatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPaidAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "paid_at")
    @java.lang.Deprecated
    public static void getPaidAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getShippedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "shipped_at")
    @java.lang.Deprecated
    public static void getShippedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDeliveredAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "delivered_at")
    @java.lang.Deprecated
    public static void getDeliveredAt$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
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
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.OrderDB copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String orderNumber, @org.jetbrains.annotations.NotNull
    java.lang.String buyerId, double subtotal, double shippingCost, double discountAmount, double totalAmount, @org.jetbrains.annotations.NotNull
    java.lang.String currency, @org.jetbrains.annotations.NotNull
    java.lang.String status, @org.jetbrains.annotations.Nullable
    java.lang.String shippingAddressId, @org.jetbrains.annotations.Nullable
    java.lang.String shippingMethod, @org.jetbrains.annotations.Nullable
    java.lang.String trackingNumber, @org.jetbrains.annotations.Nullable
    java.lang.String buyerNotes, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.NotNull
    java.lang.String updatedAt, @org.jetbrains.annotations.Nullable
    java.lang.String paidAt, @org.jetbrains.annotations.Nullable
    java.lang.String shippedAt, @org.jetbrains.annotations.Nullable
    java.lang.String deliveredAt) {
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
    
    @kotlin.jvm.JvmStatic
    public static final void write$Self$app_debug(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.OrderDB self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/OrderDB.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/OrderDB;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.OrderDB> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.OrderDB.$serializer INSTANCE = null;
        
        private $serializer() {
            super();
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] childSerializers() {
            return null;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public com.rendly.app.data.model.OrderDB deserialize(@org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.Decoder decoder) {
            return null;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.descriptors.SerialDescriptor getDescriptor() {
            return null;
        }
        
        @java.lang.Override
        public void serialize(@org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.Encoder encoder, @org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.OrderDB value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/OrderDB$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/OrderDB;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.OrderDB> serializer() {
            return null;
        }
    }
}