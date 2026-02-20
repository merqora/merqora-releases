package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Modelo principal de transacción Handshake
 */
@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\bG\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 q2\u00020\u0001:\u0002pqB\u00e7\u0001\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\n\u001a\u00020\u000b\u0012\b\u0010\f\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0001\u0010\r\u001a\u00020\u000e\u0012\b\b\u0001\u0010\u000f\u001a\u00020\u000e\u0012\n\b\u0001\u0010\u0010\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0001\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a\u00a2\u0006\u0002\u0010\u001bB\u00c3\u0001\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\u0006\u0010\t\u001a\u00020\u0005\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u0005\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u000e\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u000b\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u001cJ\u000e\u0010E\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\u000e\u0010G\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\u000e\u0010H\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\u000e\u0010I\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\u000e\u0010J\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\u000b\u0010K\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010L\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010,J\u000b\u0010M\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010N\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010O\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010P\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010Q\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010R\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010S\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010T\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010U\u001a\u00020\u0005H\u00c6\u0003J\t\u0010V\u001a\u00020\u0005H\u00c6\u0003J\t\u0010W\u001a\u00020\u0005H\u00c6\u0003J\t\u0010X\u001a\u00020\u0005H\u00c6\u0003J\t\u0010Y\u001a\u00020\u000bH\u00c6\u0003J\t\u0010Z\u001a\u00020\u0005H\u00c6\u0003J\t\u0010[\u001a\u00020\u000eH\u00c6\u0003J\t\u0010\\\u001a\u00020\u000eH\u00c6\u0003J\u00d6\u0001\u0010]\u001a\u00020\u00002\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00052\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u000e2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u000b2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010^J\u0013\u0010_\u001a\u00020\u000e2\b\u0010`\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0006\u0010a\u001a\u00020bJ\t\u0010c\u001a\u00020\u0003H\u00d6\u0001J\u000e\u0010d\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\u000e\u0010e\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\u000e\u0010f\u001a\u00020\u000e2\u0006\u0010F\u001a\u00020\u0005J\t\u0010g\u001a\u00020\u0005H\u00d6\u0001J&\u0010h\u001a\u00020i2\u0006\u0010j\u001a\u00020\u00002\u0006\u0010k\u001a\u00020l2\u0006\u0010m\u001a\u00020nH\u00c1\u0001\u00a2\u0006\u0002\boR\u001e\u0010\u0017\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u001d\u0010\u001e\u001a\u0004\b\u001f\u0010 R\u001c\u0010\n\u001a\u00020\u000b8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b!\u0010\u001e\u001a\u0004\b\"\u0010#R\u001e\u0010\u0018\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b$\u0010\u001e\u001a\u0004\b%\u0010 R\u001c\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b&\u0010\u001e\u001a\u0004\b\'\u0010 R\u001e\u0010\u0011\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b(\u0010\u001e\u001a\u0004\b)\u0010 R \u0010\u0010\u001a\u0004\u0018\u00010\u000b8\u0006X\u0087\u0004\u00a2\u0006\u0010\n\u0002\u0010-\u0012\u0004\b*\u0010\u001e\u001a\u0004\b+\u0010,R\u001e\u0010\u0015\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b.\u0010\u001e\u001a\u0004\b/\u0010 R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010 R\u001c\u0010\r\u001a\u00020\u000e8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b1\u0010\u001e\u001a\u0004\b2\u00103R\u001c\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b4\u0010\u001e\u001a\u0004\b5\u0010 R\u001c\u0010\t\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b6\u0010\u001e\u001a\u0004\b7\u0010 R\u001e\u0010\u0014\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b8\u0010\u001e\u001a\u0004\b9\u0010 R\u001e\u0010\u0012\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b:\u0010\u001e\u001a\u0004\b;\u0010 R\u001e\u0010\u0013\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b<\u0010\u001e\u001a\u0004\b=\u0010 R\u001c\u0010\u000f\u001a\u00020\u000e8\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b>\u0010\u001e\u001a\u0004\b?\u00103R\u001c\u0010\b\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b@\u0010\u001e\u001a\u0004\bA\u0010 R\u0011\u0010\f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010 R\u001e\u0010\u0016\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\bC\u0010\u001e\u001a\u0004\bD\u0010 \u00a8\u0006r"}, d2 = {"Lcom/rendly/app/data/model/HandshakeTransaction;", "", "seen1", "", "id", "", "conversationId", "initiatorId", "receiverId", "productDescription", "agreedPrice", "", "status", "initiatorConfirmed", "", "receiverConfirmed", "counterPrice", "counterMessage", "qrSecretInitiator", "qrSecretReceiver", "qrScannedAt", "createdAt", "updatedAt", "acceptedAt", "completedAt", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;ZZLjava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;ZZLjava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAcceptedAt$annotations", "()V", "getAcceptedAt", "()Ljava/lang/String;", "getAgreedPrice$annotations", "getAgreedPrice", "()D", "getCompletedAt$annotations", "getCompletedAt", "getConversationId$annotations", "getConversationId", "getCounterMessage$annotations", "getCounterMessage", "getCounterPrice$annotations", "getCounterPrice", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getCreatedAt$annotations", "getCreatedAt", "getId", "getInitiatorConfirmed$annotations", "getInitiatorConfirmed", "()Z", "getInitiatorId$annotations", "getInitiatorId", "getProductDescription$annotations", "getProductDescription", "getQrScannedAt$annotations", "getQrScannedAt", "getQrSecretInitiator$annotations", "getQrSecretInitiator", "getQrSecretReceiver$annotations", "getQrSecretReceiver", "getReceiverConfirmed$annotations", "getReceiverConfirmed", "getReceiverId$annotations", "getReceiverId", "getStatus", "getUpdatedAt$annotations", "getUpdatedAt", "canAccept", "userId", "canCancel", "canConfirm", "canReject", "canRenegotiate", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;ZZLjava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/rendly/app/data/model/HandshakeTransaction;", "equals", "other", "getStatusEnum", "Lcom/rendly/app/data/model/HandshakeStatus;", "hashCode", "isInitiator", "isParticipant", "isReceiver", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
public final class HandshakeTransaction {
    @org.jetbrains.annotations.Nullable
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String conversationId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String initiatorId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String receiverId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String productDescription = null;
    private final double agreedPrice = 0.0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String status = null;
    private final boolean initiatorConfirmed = false;
    private final boolean receiverConfirmed = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double counterPrice = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String counterMessage = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String qrSecretInitiator = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String qrSecretReceiver = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String qrScannedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String updatedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String acceptedAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String completedAt = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.HandshakeTransaction.Companion Companion = null;
    
    public HandshakeTransaction(@org.jetbrains.annotations.Nullable
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String initiatorId, @org.jetbrains.annotations.NotNull
    java.lang.String receiverId, @org.jetbrains.annotations.NotNull
    java.lang.String productDescription, double agreedPrice, @org.jetbrains.annotations.NotNull
    java.lang.String status, boolean initiatorConfirmed, boolean receiverConfirmed, @org.jetbrains.annotations.Nullable
    java.lang.Double counterPrice, @org.jetbrains.annotations.Nullable
    java.lang.String counterMessage, @org.jetbrains.annotations.Nullable
    java.lang.String qrSecretInitiator, @org.jetbrains.annotations.Nullable
    java.lang.String qrSecretReceiver, @org.jetbrains.annotations.Nullable
    java.lang.String qrScannedAt, @org.jetbrains.annotations.Nullable
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String updatedAt, @org.jetbrains.annotations.Nullable
    java.lang.String acceptedAt, @org.jetbrains.annotations.Nullable
    java.lang.String completedAt) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getConversationId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "conversation_id")
    @java.lang.Deprecated
    public static void getConversationId$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getInitiatorId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "initiator_id")
    @java.lang.Deprecated
    public static void getInitiatorId$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getReceiverId() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "receiver_id")
    @java.lang.Deprecated
    public static void getReceiverId$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getProductDescription() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "product_description")
    @java.lang.Deprecated
    public static void getProductDescription$annotations() {
    }
    
    public final double getAgreedPrice() {
        return 0.0;
    }
    
    @kotlinx.serialization.SerialName(value = "agreed_price")
    @java.lang.Deprecated
    public static void getAgreedPrice$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStatus() {
        return null;
    }
    
    public final boolean getInitiatorConfirmed() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "initiator_confirmed")
    @java.lang.Deprecated
    public static void getInitiatorConfirmed$annotations() {
    }
    
    public final boolean getReceiverConfirmed() {
        return false;
    }
    
    @kotlinx.serialization.SerialName(value = "receiver_confirmed")
    @java.lang.Deprecated
    public static void getReceiverConfirmed$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getCounterPrice() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "counter_price")
    @java.lang.Deprecated
    public static void getCounterPrice$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCounterMessage() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "counter_message")
    @java.lang.Deprecated
    public static void getCounterMessage$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getQrSecretInitiator() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "qr_secret_initiator")
    @java.lang.Deprecated
    public static void getQrSecretInitiator$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getQrSecretReceiver() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "qr_secret_receiver")
    @java.lang.Deprecated
    public static void getQrSecretReceiver$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getQrScannedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "qr_scanned_at")
    @java.lang.Deprecated
    public static void getQrScannedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCreatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "created_at")
    @java.lang.Deprecated
    public static void getCreatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUpdatedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "updated_at")
    @java.lang.Deprecated
    public static void getUpdatedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getAcceptedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "accepted_at")
    @java.lang.Deprecated
    public static void getAcceptedAt$annotations() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCompletedAt() {
        return null;
    }
    
    @kotlinx.serialization.SerialName(value = "completed_at")
    @java.lang.Deprecated
    public static void getCompletedAt$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.HandshakeStatus getStatusEnum() {
        return null;
    }
    
    public final boolean isParticipant(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    public final boolean isInitiator(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    public final boolean isReceiver(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    public final boolean canAccept(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    public final boolean canRenegotiate(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    public final boolean canReject(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    public final boolean canConfirm(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    public final boolean canCancel(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component10() {
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
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    public final double component6() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.HandshakeTransaction copy(@org.jetbrains.annotations.Nullable
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    java.lang.String initiatorId, @org.jetbrains.annotations.NotNull
    java.lang.String receiverId, @org.jetbrains.annotations.NotNull
    java.lang.String productDescription, double agreedPrice, @org.jetbrains.annotations.NotNull
    java.lang.String status, boolean initiatorConfirmed, boolean receiverConfirmed, @org.jetbrains.annotations.Nullable
    java.lang.Double counterPrice, @org.jetbrains.annotations.Nullable
    java.lang.String counterMessage, @org.jetbrains.annotations.Nullable
    java.lang.String qrSecretInitiator, @org.jetbrains.annotations.Nullable
    java.lang.String qrSecretReceiver, @org.jetbrains.annotations.Nullable
    java.lang.String qrScannedAt, @org.jetbrains.annotations.Nullable
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String updatedAt, @org.jetbrains.annotations.Nullable
    java.lang.String acceptedAt, @org.jetbrains.annotations.Nullable
    java.lang.String completedAt) {
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
    com.rendly.app.data.model.HandshakeTransaction self, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
    kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
    }
    
    /**
     * Modelo principal de transacción Handshake
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/model/HandshakeTransaction.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/model/HandshakeTransaction;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
    @java.lang.Deprecated
    public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.model.HandshakeTransaction> {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.model.HandshakeTransaction.$serializer INSTANCE = null;
        
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
        public com.rendly.app.data.model.HandshakeTransaction deserialize(@org.jetbrains.annotations.NotNull
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
        com.rendly.app.data.model.HandshakeTransaction value) {
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
            return null;
        }
    }
    
    /**
     * Modelo principal de transacción Handshake
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/HandshakeTransaction$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/HandshakeTransaction;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.HandshakeTransaction> serializer() {
            return null;
        }
    }
}