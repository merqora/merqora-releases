package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Respuesta del procesamiento de pago
 */
@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\bE\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 k2\u00020\u0001:\u0002jkB\u00e5\u0001\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\t\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\n\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\u000b\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\f\u001a\u0004\u0018\u00010\u0007\u0012\b\u0010\r\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\u000e\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\u000f\u001a\u0004\u0018\u00010\u0007\u0012\b\u0010\u0010\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u0012\n\b\u0001\u0010\u0013\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0001\u0010\u0014\u001a\u0004\u0018\u00010\u0015\u0012\b\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0001\u0010\u0017\u001a\u0004\u0018\u00010\u0018\u0012\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u0012\n\b\u0001\u0010\u001b\u001a\u0004\u0018\u00010\u0007\u0012\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d\u00a2\u0006\u0002\u0010\u001eB\u00d5\u0001\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0007\u0012\u0006\u0010\r\u001a\u00020\u0007\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0015\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0018\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010\u001fJ\t\u0010J\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010K\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010L\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u0010\u0010M\u001a\u0004\u0018\u00010\u0012H\u00c6\u0003\u00a2\u0006\u0002\u00106J\u000b\u0010N\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u0010\u0010O\u001a\u0004\u0018\u00010\u0015H\u00c6\u0003\u00a2\u0006\u0002\u0010EJ\u0010\u0010P\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u00102J\u000b\u0010Q\u001a\u0004\u0018\u00010\u0018H\u00c6\u0003J\u000b\u0010R\u001a\u0004\u0018\u00010\u001aH\u00c6\u0003J\u000b\u0010S\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010T\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010U\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010V\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010W\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010X\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010Y\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010Z\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010[\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u00e2\u0001\u0010\\\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\r\u001a\u00020\u00072\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00182\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0007H\u00c6\u0001\u00a2\u0006\u0002\u0010]J\u0013\u0010^\u001a\u00020\u00122\b\u0010_\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010`\u001a\u00020\u0003H\u00d6\u0001J\t\u0010a\u001a\u00020\u0007H\u00d6\u0001J&\u0010b\u001a\u00020c2\u0006\u0010d\u001a\u00020\u00002\u0006\u0010e\u001a\u00020f2\u0006\u0010g\u001a\u00020hH\u00c1\u0001\u00a2\u0006\u0002\biR\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u001e\u0010\u000f\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\"\u0010#\u001a\u0004\b$\u0010%R\u001e\u0010\b\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b&\u0010#\u001a\u0004\b\'\u0010%R\u001e\u0010\u0006\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b(\u0010#\u001a\u0004\b)\u0010%R\u001e\u0010\t\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b*\u0010#\u001a\u0004\b+\u0010%R\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010%R\u001e\u0010\u0013\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b-\u0010#\u001a\u0004\b.\u0010%R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0015\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u00103\u001a\u0004\b1\u00102R \u0010\u0011\u001a\u0004\u0018\u00010\u00128\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u00107\u0012\u0004\b4\u0010#\u001a\u0004\b5\u00106R\u001e\u0010\n\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b8\u0010#\u001a\u0004\b9\u0010%R\u001e\u0010\u000b\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b:\u0010#\u001a\u0004\b;\u0010%R\u001e\u0010\f\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b<\u0010#\u001a\u0004\b=\u0010%R\u001e\u0010\u001b\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b>\u0010#\u001a\u0004\b?\u0010%R\u0011\u0010\r\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010%R\u001e\u0010\u000e\u001a\u0004\u0018\u00010\u00078\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bA\u0010#\u001a\u0004\bB\u0010%R \u0010\u0014\u001a\u0004\u0018\u00010\u00158\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010F\u0012\u0004\bC\u0010#\u001a\u0004\bD\u0010ER\u001e\u0010\u0017\u001a\u0004\u0018\u00010\u00188\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bG\u0010#\u001a\u0004\bH\u0010I\u00a8\u0006l"}, d2 = {"Lcom/rendly/app/data/model/PaymentResponse;", "", "seen1", "", "id", "", "dateCreated", "", "dateApproved", "dateLastUpdated", "operationType", "paymentMethodId", "paymentTypeId", "status", "statusDetail", "currencyId", "description", "liveMode", "", "externalReference", "transactionAmount", "", "installments", "transactionDetails", "Lcom/rendly/app/data/model/TransactionDetails;", "card", "Lcom/rendly/app/data/model/CardResponse;", "statementDescriptor", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(IJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Lcom/rendly/app/data/model/TransactionDetails;Lcom/rendly/app/data/model/CardResponse;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Lcom/rendly/app/data/model/TransactionDetails;Lcom/rendly/app/data/model/CardResponse;Ljava/lang/String;)V", "getCard", "()Lcom/rendly/app/data/model/CardResponse;", "getCurrencyId$annotations", "()V", "getCurrencyId", "()Ljava/lang/String;", "getDateApproved$annotations", "getDateApproved", "getDateCreated$annotations", "getDateCreated", "getDateLastUpdated$annotations", "getDateLastUpdated", "getDescription", "getExternalReference$annotations", "getExternalReference", "getId", "()J", "getInstallments", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getLiveMode$annotations", "getLiveMode", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getOperationType$annotations", "getOperationType", "getPaymentMethodId$annotations", "getPaymentMethodId", "getPaymentTypeId$annotations", "getPaymentTypeId", "getStatementDescriptor$annotations", "getStatementDescriptor", "getStatus", "getStatusDetail$annotations", "getStatusDetail", "getTransactionAmount$annotations", "getTransactionAmount", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getTransactionDetails$annotations", "getTransactionDetails", "()Lcom/rendly/app/data/model/TransactionDetails;", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Integer;Lcom/rendly/app/data/model/TransactionDetails;Lcom/rendly/app/data/model/CardResponse;Ljava/lang/String;)Lcom/rendly/app/data/model/PaymentResponse;", "equals", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
public final class PaymentResponse {
    private final long id = 0L;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String dateCreated = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String dateApproved = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String dateLastUpdated = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String operationType = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String paymentMethodId = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String paymentTypeId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String status = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String statusDetail = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String currencyId = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String description = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Boolean liveMode = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String externalReference = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double transactionAmount = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Integer installments = null;
    @org.jetbrains.annotations.Nullable
    private final com.rendly.app.data.model.TransactionDetails transactionDetails = null;
    @org.jetbrains.annotations.Nullable
    private final com.rendly.app.data.model.CardResponse card = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String statementDescriptor = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.PaymentResponse.Companion Companion = null;
    
    public PaymentResponse(long id, @org.jetbrains.annotations.Nullable
    java.lang.String dateCreated, @org.jetbrains.annotations.Nullable
    java.lang.String dateApproved, @org.jetbrains.annotations.Nullable
    java.lang.String dateLastUpdated, @org.jetbrains.annotations.Nullable
    java.lang.String operationType, @org.jetbrains.annotations.Nullable
    java.lang.String paymentMethodId, @org.jetbrains.annotations.Nullable
    java.lang.String paymentTypeId, @org.jetbrains.annotations.NotNull
    java.lang.String status, @org.jetbrains.annotations.Nullable
    java.lang.String statusDetail, @org.jetbrains.annotations.Nullable
    java.lang.String currencyId, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.Nullable
    java.lang.Boolean liveMode, @org.jetbrains.annotations.Nullable
    java.lang.String externalReference, @org.jetbrains.annotations.Nullable
    java.lang.Double transactionAmount, @org.jetbrains.annotations.Nullable
    java.lang.Integer installments, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.TransactionDetails transactionDetails, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.CardResponse card, @org.jetbrains.annotations.Nullable
    java.lang.String statementDescriptor) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDateCreated() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "date_created")
    @java.lang.Deprecated
    public static void getDateCreated$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDateApproved() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "date_approved")
    @java.lang.Deprecated
    public static void getDateApproved$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDateLastUpdated() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "date_last_updated")
    @java.lang.Deprecated
    public static void getDateLastUpdated$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getOperationType() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "operation_type")
    @java.lang.Deprecated
    public static void getOperationType$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPaymentMethodId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "payment_method_id")
    @java.lang.Deprecated
    public static void getPaymentMethodId$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPaymentTypeId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "payment_type_id")
    @java.lang.Deprecated
    public static void getPaymentTypeId$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getStatusDetail() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "status_detail")
    @java.lang.Deprecated
    public static void getStatusDetail$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCurrencyId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "currency_id")
    @java.lang.Deprecated
    public static void getCurrencyId$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean getLiveMode() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "live_mode")
    @java.lang.Deprecated
    public static void getLiveMode$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getExternalReference() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "external_reference")
    @java.lang.Deprecated
    public static void getExternalReference$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getTransactionAmount() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "transaction_amount")
    @java.lang.Deprecated
    public static void getTransactionAmount$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer getInstallments() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.TransactionDetails getTransactionDetails() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "transaction_details")
    @java.lang.Deprecated
    public static void getTransactionDetails$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.CardResponse getCard() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getStatementDescriptor() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "statement_descriptor")
    @java.lang.Deprecated
    public static void getStatementDescriptor$annotations() {
    }
    
    public final long component1() {
        return 0L;
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
    public final java.lang.Boolean component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Integer component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.TransactionDetails component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.CardResponse component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.PaymentResponse copy(long id, @org.jetbrains.annotations.Nullable
    java.lang.String dateCreated, @org.jetbrains.annotations.Nullable
    java.lang.String dateApproved, @org.jetbrains.annotations.Nullable
    java.lang.String dateLastUpdated, @org.jetbrains.annotations.Nullable
    java.lang.String operationType, @org.jetbrains.annotations.Nullable
    java.lang.String paymentMethodId, @org.jetbrains.annotations.Nullable
    java.lang.String paymentTypeId, @org.jetbrains.annotations.NotNull
    java.lang.String status, @org.jetbrains.annotations.Nullable
    java.lang.String statusDetail, @org.jetbrains.annotations.Nullable
    java.lang.String currencyId, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.Nullable
    java.lang.Boolean liveMode, @org.jetbrains.annotations.Nullable
    java.lang.String externalReference, @org.jetbrains.annotations.Nullable
    java.lang.Double transactionAmount, @org.jetbrains.annotations.Nullable
    java.lang.Integer installments, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.TransactionDetails transactionDetails, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.CardResponse card, @org.jetbrains.annotations.Nullable
    java.lang.String statementDescriptor) {
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
    com.rendly.app.data.model.PaymentResponse self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    /**
     * Respuesta del procesamiento de pago
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/PaymentResponse.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/PaymentResponse;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.PaymentResponse> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.PaymentResponse.$serializer INSTANCE = null;
        
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
        public com.rendly.app.data.model.PaymentResponse deserialize(@org.jetbrains.annotations.NotNull
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
        com.rendly.app.data.model.PaymentResponse value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    /**
     * Respuesta del procesamiento de pago
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/PaymentResponse$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/PaymentResponse;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.PaymentResponse> serializer() {
            return null;
        }
    }
}