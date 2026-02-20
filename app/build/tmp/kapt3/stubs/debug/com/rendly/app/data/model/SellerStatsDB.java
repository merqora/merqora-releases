package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b1\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 R2\u00020\u0001:\u0002QRB\u009b\u0001\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0007\u001a\u00020\b\u0012\b\b\u0001\u0010\t\u001a\u00020\u0003\u0012\b\b\u0001\u0010\n\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0001\u0010\r\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u000e\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u000f\u001a\u0004\u0018\u00010\b\u0012\b\b\u0001\u0010\u0010\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\u0002\u0010\u0014B\u0081\u0001\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\r\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0015J\t\u00106\u001a\u00020\u0005H\u00c6\u0003J\u0010\u00107\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0019J\t\u00108\u001a\u00020\u0003H\u00c6\u0003J\t\u00109\u001a\u00020\u0005H\u00c6\u0003J\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\bH\u00c6\u0003J\t\u0010<\u001a\u00020\u0003H\u00c6\u0003J\t\u0010=\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010>\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001dJ\u0010\u0010?\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001dJ\t\u0010@\u001a\u00020\u0003H\u00c6\u0003J\t\u0010A\u001a\u00020\u0003H\u00c6\u0003J\u008c\u0001\u0010B\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\r\u001a\u00020\u00032\b\b\u0002\u0010\u000e\u001a\u00020\u00032\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010CJ\u0013\u0010D\u001a\u00020E2\b\u0010F\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010G\u001a\u00020\u0003H\u00d6\u0001J\t\u0010H\u001a\u00020\u0005H\u00d6\u0001J&\u0010I\u001a\u00020J2\u0006\u0010K\u001a\u00020\u00002\u0006\u0010L\u001a\u00020M2\u0006\u0010N\u001a\u00020OH\u00c1\u0001\u00a2\u0006\u0002\bPR \u0010\u000f\u001a\u0004\u0018\u00010\b8\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010\u001a\u0012\u0004\b\u0016\u0010\u0017\u001a\u0004\b\u0018\u0010\u0019R \u0010\u000b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010\u001e\u0012\u0004\b\u001b\u0010\u0017\u001a\u0004\b\u001c\u0010\u001dR \u0010\f\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010\u001e\u0012\u0004\b\u001f\u0010\u0017\u001a\u0004\b \u0010\u001dR\u001c\u0010\n\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b!\u0010\u0017\u001a\u0004\b\"\u0010#R\u001c\u0010\t\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b$\u0010\u0017\u001a\u0004\b%\u0010#R\u001c\u0010\u0010\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b&\u0010\u0017\u001a\u0004\b\'\u0010#R\u001c\u0010\u000e\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b(\u0010\u0017\u001a\u0004\b)\u0010#R\u001c\u0010\r\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b*\u0010\u0017\u001a\u0004\b+\u0010#R\u001c\u0010\u0007\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b,\u0010\u0017\u001a\u0004\b-\u0010.R\u001c\u0010\u0006\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b/\u0010\u0017\u001a\u0004\b0\u0010#R\u001c\u0010\u0011\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b1\u0010\u0017\u001a\u0004\b2\u00103R\u001c\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b4\u0010\u0017\u001a\u0004\b5\u00103\u00a8\u0006S"}, d2 = {"Lcom/rendly/app/data/model/SellerStatsDB;", "", "seen1", "", "userId", "", "totalSales", "totalRevenue", "", "completedOrders", "cancelledOrders", "avgResponseTimeMinutes", "avgShippingTimeHours", "totalRatings", "sumRatings", "avgRating", "reputationScore", "updatedAt", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;IDIILjava/lang/Integer;Ljava/lang/Integer;IILjava/lang/Double;ILjava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;IDIILjava/lang/Integer;Ljava/lang/Integer;IILjava/lang/Double;ILjava/lang/String;)V", "getAvgRating$annotations", "()V", "getAvgRating", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getAvgResponseTimeMinutes$annotations", "getAvgResponseTimeMinutes", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getAvgShippingTimeHours$annotations", "getAvgShippingTimeHours", "getCancelledOrders$annotations", "getCancelledOrders", "()I", "getCompletedOrders$annotations", "getCompletedOrders", "getReputationScore$annotations", "getReputationScore", "getSumRatings$annotations", "getSumRatings", "getTotalRatings$annotations", "getTotalRatings", "getTotalRevenue$annotations", "getTotalRevenue", "()D", "getTotalSales$annotations", "getTotalSales", "getUpdatedAt$annotations", "getUpdatedAt", "()Ljava/lang/String;", "getUserId$annotations", "getUserId", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;IDIILjava/lang/Integer;Ljava/lang/Integer;IILjava/lang/Double;ILjava/lang/String;)Lcom/rendly/app/data/model/SellerStatsDB;", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
public final class SellerStatsDB {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    private final int totalSales = 0;
    private final double totalRevenue = 0.0;
    private final int completedOrders = 0;
    private final int cancelledOrders = 0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer avgResponseTimeMinutes = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer avgShippingTimeHours = null;
    private final int totalRatings = 0;
    private final int sumRatings = 0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double avgRating = null;
    private final int reputationScore = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String updatedAt = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.SellerStatsDB.Companion Companion = null;
    
    public SellerStatsDB(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int totalSales, double totalRevenue, int completedOrders, int cancelledOrders, @org.jetbrains.annotations.Nullable
    java.lang.Integer avgResponseTimeMinutes, @org.jetbrains.annotations.Nullable
    java.lang.Integer avgShippingTimeHours, int totalRatings, int sumRatings, @org.jetbrains.annotations.Nullable
    java.lang.Double avgRating, int reputationScore, @org.jetbrains.annotations.NotNull
    java.lang.String updatedAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "user_id")
    @java.lang.Deprecated
    public static void getUserId$annotations() {
    }
    
    public final int getTotalSales() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "total_sales")
    @java.lang.Deprecated
    public static void getTotalSales$annotations() {
    }
    
    public final double getTotalRevenue() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "total_revenue")
    @java.lang.Deprecated
    public static void getTotalRevenue$annotations() {
    }
    
    public final int getCompletedOrders() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "completed_orders")
    @java.lang.Deprecated
    public static void getCompletedOrders$annotations() {
    }
    
    public final int getCancelledOrders() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "cancelled_orders")
    @java.lang.Deprecated
    public static void getCancelledOrders$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getAvgResponseTimeMinutes() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "avg_response_time_minutes")
    @java.lang.Deprecated
    public static void getAvgResponseTimeMinutes$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getAvgShippingTimeHours() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "avg_shipping_time_hours")
    @java.lang.Deprecated
    public static void getAvgShippingTimeHours$annotations() {
    }
    
    public final int getTotalRatings() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "total_ratings")
    @java.lang.Deprecated
    public static void getTotalRatings$annotations() {
    }
    
    public final int getSumRatings() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "sum_ratings")
    @java.lang.Deprecated
    public static void getSumRatings$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getAvgRating() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "avg_rating")
    @java.lang.Deprecated
    public static void getAvgRating$annotations() {
    }
    
    public final int getReputationScore() {
        return 0;
    }
    
    @kotlinx.serialization.SerialName(value = "reputation_score")
    @java.lang.Deprecated
    public static void getReputationScore$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUpdatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "updated_at")
    @java.lang.Deprecated
    public static void getUpdatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component10() {
        return null;
    }
    
    public final int component11() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component12() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final double component3() {
        return 0.0;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component7() {
        return null;
    }
    
    public final int component8() {
        return 0;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.SellerStatsDB copy(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int totalSales, double totalRevenue, int completedOrders, int cancelledOrders, @org.jetbrains.annotations.Nullable
    java.lang.Integer avgResponseTimeMinutes, @org.jetbrains.annotations.Nullable
    java.lang.Integer avgShippingTimeHours, int totalRatings, int sumRatings, @org.jetbrains.annotations.Nullable
    java.lang.Double avgRating, int reputationScore, @org.jetbrains.annotations.NotNull
    java.lang.String updatedAt) {
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
    com.rendly.app.data.model.SellerStatsDB self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/SellerStatsDB.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/SellerStatsDB;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.SellerStatsDB> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.SellerStatsDB.$serializer INSTANCE = null;
        
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
        public com.rendly.app.data.model.SellerStatsDB deserialize(@org.jetbrains.annotations.NotNull
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
        com.rendly.app.data.model.SellerStatsDB value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/SellerStatsDB$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/SellerStatsDB;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.SellerStatsDB> serializer() {
            return null;
        }
    }
}