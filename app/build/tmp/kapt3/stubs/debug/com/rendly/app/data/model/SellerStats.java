package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u001c\n\u0002\u0010\u000b\n\u0002\b\u0005\b\u0086\b\u0018\u0000 )2\u00020\u0001:\u0001)B9\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\t\u001a\u00020\u0005\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010\u001f\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\rJ\t\u0010 \u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010!\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0010JN\u0010\"\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\t\u001a\u00020\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010#J\u0013\u0010$\u001a\u00020%2\b\u0010&\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\'\u001a\u00020\u0005H\u00d6\u0001J\t\u0010(\u001a\u00020\u0003H\u00d6\u0001R\u0015\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u000e\u001a\u0004\b\f\u0010\rR\u0015\u0010\n\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u0011\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0014\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0017\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0016R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0013R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0016\u00a8\u0006*"}, d2 = {"Lcom/rendly/app/data/model/SellerStats;", "", "userId", "", "totalSales", "", "completedOrders", "avgRating", "", "reputationScore", "avgResponseTimeMinutes", "(Ljava/lang/String;IILjava/lang/Double;ILjava/lang/Integer;)V", "getAvgRating", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getAvgResponseTimeMinutes", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getCompletedOrders", "()I", "formattedResponseTime", "getFormattedResponseTime", "()Ljava/lang/String;", "formattedSales", "getFormattedSales", "getReputationScore", "getTotalSales", "getUserId", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "(Ljava/lang/String;IILjava/lang/Double;ILjava/lang/Integer;)Lcom/rendly/app/data/model/SellerStats;", "equals", "", "other", "hashCode", "toString", "Companion", "app_debug"})
public final class SellerStats {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    private final int totalSales = 0;
    private final int completedOrders = 0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double avgRating = null;
    private final int reputationScore = 0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer avgResponseTimeMinutes = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.SellerStats.Companion Companion = null;
    
    public SellerStats(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int totalSales, int completedOrders, @org.jetbrains.annotations.Nullable
    java.lang.Double avgRating, int reputationScore, @org.jetbrains.annotations.Nullable
    java.lang.Integer avgResponseTimeMinutes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserId() {
        return null;
    }
    
    public final int getTotalSales() {
        return 0;
    }
    
    public final int getCompletedOrders() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getAvgRating() {
        return null;
    }
    
    public final int getReputationScore() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getAvgResponseTimeMinutes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedResponseTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedSales() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component4() {
        return null;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.SellerStats copy(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int totalSales, int completedOrders, @org.jetbrains.annotations.Nullable
    java.lang.Double avgRating, int reputationScore, @org.jetbrains.annotations.Nullable
    java.lang.Integer avgResponseTimeMinutes) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\b\u001a\u00020\t\u00a8\u0006\n"}, d2 = {"Lcom/rendly/app/data/model/SellerStats$Companion;", "", "()V", "default", "Lcom/rendly/app/data/model/SellerStats;", "userId", "", "fromDB", "db", "Lcom/rendly/app/data/model/SellerStatsDB;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.SellerStats fromDB(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.SellerStatsDB db) {
            return null;
        }
    }
}