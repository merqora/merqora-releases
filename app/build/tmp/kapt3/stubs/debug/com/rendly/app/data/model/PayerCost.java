package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b.\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 P2\u00020\u0001:\u0002OPB\u00a9\u0001\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0005\u001a\u00020\u0006\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\u0006\u0012\u000e\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n\u0012\u0010\b\u0001\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n\u0012\n\b\u0001\u0010\r\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\u000e\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0001\u0010\u000f\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0001\u0010\u0010\u001a\u00020\u0006\u0012\b\b\u0001\u0010\u0011\u001a\u00020\u0006\u0012\n\b\u0001\u0010\u0012\u001a\u0004\u0018\u00010\u000b\u0012\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014\u00a2\u0006\u0002\u0010\u0015B\u0091\u0001\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0006\u0012\u0010\b\u0002\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n\u0012\u0010\b\u0002\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u000b\u0012\u0006\u0010\u0010\u001a\u00020\u0006\u0012\u0006\u0010\u0011\u001a\u00020\u0006\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\u0016J\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\t\u00105\u001a\u00020\u0006H\u00c6\u0003J\t\u00106\u001a\u00020\u0006H\u00c6\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\t\u00108\u001a\u00020\u0006H\u00c6\u0003J\u0010\u00109\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001aJ\u0010\u0010:\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001aJ\u0011\u0010;\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\nH\u00c6\u0003J\u0011\u0010<\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\nH\u00c6\u0003J\u0010\u0010=\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001aJ\u0010\u0010>\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003\u00a2\u0006\u0002\u0010\u001aJ\u000b\u0010?\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\u00a2\u0001\u0010@\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00062\u0010\b\u0002\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n2\u0010\b\u0002\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\u0010\u001a\u00020\u00062\b\b\u0002\u0010\u0011\u001a\u00020\u00062\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u000bH\u00c6\u0001\u00a2\u0006\u0002\u0010AJ\u0013\u0010B\u001a\u00020C2\b\u0010D\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010E\u001a\u00020\u0003H\u00d6\u0001J\t\u0010F\u001a\u00020\u000bH\u00d6\u0001J&\u0010G\u001a\u00020H2\u0006\u0010I\u001a\u00020\u00002\u0006\u0010J\u001a\u00020K2\u0006\u0010L\u001a\u00020MH\u00c1\u0001\u00a2\u0006\u0002\bNR \u0010\u0007\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010\u001b\u0012\u0004\b\u0017\u0010\u0018\u001a\u0004\b\u0019\u0010\u001aR\u001c\u0010\u0010\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u001c\u0010\u0018\u001a\u0004\b\u001d\u0010\u001eR\u001c\u0010\u0005\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u001f\u0010\u0018\u001a\u0004\b \u0010\u001eR$\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b!\u0010\u0018\u001a\u0004\b\"\u0010#R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0019\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010#R \u0010\u000e\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010\u001b\u0012\u0004\b\'\u0010\u0018\u001a\u0004\b(\u0010\u001aR \u0010\r\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010\u001b\u0012\u0004\b)\u0010\u0018\u001a\u0004\b*\u0010\u001aR\u001e\u0010\u0012\u001a\u0004\u0018\u00010\u000b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b+\u0010\u0018\u001a\u0004\b,\u0010-R\u001e\u0010\u000f\u001a\u0004\u0018\u00010\u000b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b.\u0010\u0018\u001a\u0004\b/\u0010-R \u0010\b\u001a\u0004\u0018\u00010\u00068\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010\u001b\u0012\u0004\b0\u0010\u0018\u001a\u0004\b1\u0010\u001aR\u001c\u0010\u0011\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b2\u0010\u0018\u001a\u0004\b3\u0010\u001e\u00a8\u0006Q"}, d2 = {"Lcom/rendly/app/data/model/PayerCost;", "", "seen1", "", "installments", "installmentRate", "", "discountRate", "reimbursementRate", "labels", "", "", "installmentRateCollector", "minAllowedAmount", "maxAllowedAmount", "recommendedMessage", "installmentAmount", "totalAmount", "paymentMethodOptionId", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(IIDLjava/lang/Double;Ljava/lang/Double;Ljava/util/List;Ljava/util/List;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/String;DDLjava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(IDLjava/lang/Double;Ljava/lang/Double;Ljava/util/List;Ljava/util/List;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/String;DDLjava/lang/String;)V", "getDiscountRate$annotations", "()V", "getDiscountRate", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getInstallmentAmount$annotations", "getInstallmentAmount", "()D", "getInstallmentRate$annotations", "getInstallmentRate", "getInstallmentRateCollector$annotations", "getInstallmentRateCollector", "()Ljava/util/List;", "getInstallments", "()I", "getLabels", "getMaxAllowedAmount$annotations", "getMaxAllowedAmount", "getMinAllowedAmount$annotations", "getMinAllowedAmount", "getPaymentMethodOptionId$annotations", "getPaymentMethodOptionId", "()Ljava/lang/String;", "getRecommendedMessage$annotations", "getRecommendedMessage", "getReimbursementRate$annotations", "getReimbursementRate", "getTotalAmount$annotations", "getTotalAmount", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(IDLjava/lang/Double;Ljava/lang/Double;Ljava/util/List;Ljava/util/List;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/String;DDLjava/lang/String;)Lcom/rendly/app/data/model/PayerCost;", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
public final class PayerCost {
    private final int installments = 0;
    private final double installmentRate = 0.0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double discountRate = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double reimbursementRate = null;
    @org.jetbrains.annotations.Nullable
    private final java.util.List<java.lang.String> labels = null;
    @org.jetbrains.annotations.Nullable
    private final java.util.List<java.lang.String> installmentRateCollector = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double minAllowedAmount = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double maxAllowedAmount = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String recommendedMessage = null;
    private final double installmentAmount = 0.0;
    private final double totalAmount = 0.0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String paymentMethodOptionId = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.PayerCost.Companion Companion = null;
    
    public PayerCost(int installments, double installmentRate, @org.jetbrains.annotations.Nullable
    java.lang.Double discountRate, @org.jetbrains.annotations.Nullable
    java.lang.Double reimbursementRate, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> labels, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> installmentRateCollector, @org.jetbrains.annotations.Nullable
    java.lang.Double minAllowedAmount, @org.jetbrains.annotations.Nullable
    java.lang.Double maxAllowedAmount, @org.jetbrains.annotations.Nullable
    java.lang.String recommendedMessage, double installmentAmount, double totalAmount, @org.jetbrains.annotations.Nullable
    java.lang.String paymentMethodOptionId) {
        super();
    }
    
    public final int getInstallments() {
        return 0;
    }
    
    public final double getInstallmentRate() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "installment_rate")
    @java.lang.Deprecated
    public static void getInstallmentRate$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getDiscountRate() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "discount_rate")
    @java.lang.Deprecated
    public static void getDiscountRate$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getReimbursementRate() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "reimbursement_rate")
    @java.lang.Deprecated
    public static void getReimbursementRate$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> getLabels() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> getInstallmentRateCollector() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "installment_rate_collector")
    @java.lang.Deprecated
    public static void getInstallmentRateCollector$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getMinAllowedAmount() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "min_allowed_amount")
    @java.lang.Deprecated
    public static void getMinAllowedAmount$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getMaxAllowedAmount() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "max_allowed_amount")
    @java.lang.Deprecated
    public static void getMaxAllowedAmount$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRecommendedMessage() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "recommended_message")
    @java.lang.Deprecated
    public static void getRecommendedMessage$annotations() {
    }
    
    public final double getInstallmentAmount() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "installment_amount")
    @java.lang.Deprecated
    public static void getInstallmentAmount$annotations() {
    }
    
    public final double getTotalAmount() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "total_amount")
    @java.lang.Deprecated
    public static void getTotalAmount$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPaymentMethodOptionId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "payment_method_option_id")
    @java.lang.Deprecated
    public static void getPaymentMethodOptionId$annotations() {
    }
    
    public final int component1() {
        return 0;
    }
    
    public final double component10() {
        return 0.0;
    }
    
    public final double component11() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component12() {
        return null;
    }
    
    public final double component2() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.List<java.lang.String> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.PayerCost copy(int installments, double installmentRate, @org.jetbrains.annotations.Nullable
    java.lang.Double discountRate, @org.jetbrains.annotations.Nullable
    java.lang.Double reimbursementRate, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> labels, @org.jetbrains.annotations.Nullable
    java.util.List<java.lang.String> installmentRateCollector, @org.jetbrains.annotations.Nullable
    java.lang.Double minAllowedAmount, @org.jetbrains.annotations.Nullable
    java.lang.Double maxAllowedAmount, @org.jetbrains.annotations.Nullable
    java.lang.String recommendedMessage, double installmentAmount, double totalAmount, @org.jetbrains.annotations.Nullable
    java.lang.String paymentMethodOptionId) {
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
    com.rendly.app.data.model.PayerCost self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/PayerCost.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/PayerCost;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.PayerCost> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.PayerCost.$serializer INSTANCE = null;
        
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
        public com.rendly.app.data.model.PayerCost deserialize(@org.jetbrains.annotations.NotNull
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
        com.rendly.app.data.model.PayerCost value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/PayerCost$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/PayerCost;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.PayerCost> serializer() {
            return null;
        }
    }
}