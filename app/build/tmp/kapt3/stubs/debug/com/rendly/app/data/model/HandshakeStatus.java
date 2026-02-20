package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Estados posibles de una transacción handshake
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\u000b"}, d2 = {"Lcom/rendly/app/data/model/HandshakeStatus;", "", "(Ljava/lang/String;I)V", "PROPOSED", "ACCEPTED", "RENEGOTIATING", "REJECTED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "DISPUTED", "app_debug"})
public enum HandshakeStatus {
    /*public static final*/ PROPOSED /* = new PROPOSED() */,
    /*public static final*/ ACCEPTED /* = new ACCEPTED() */,
    /*public static final*/ RENEGOTIATING /* = new RENEGOTIATING() */,
    /*public static final*/ REJECTED /* = new REJECTED() */,
    /*public static final*/ IN_PROGRESS /* = new IN_PROGRESS() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */,
    /*public static final*/ DISPUTED /* = new DISPUTED() */;
    
    HandshakeStatus() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.HandshakeStatus> getEntries() {
        return null;
    }
}