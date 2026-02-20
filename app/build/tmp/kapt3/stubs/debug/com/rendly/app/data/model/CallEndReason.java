package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Razón de finalización
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/rendly/app/data/model/CallEndReason;", "", "(Ljava/lang/String;I)V", "NORMAL", "MISSED", "REJECTED", "BUSY", "NETWORK_ERROR", "TIMEOUT", "app_debug"})
public enum CallEndReason {
    /*public static final*/ NORMAL /* = new NORMAL() */,
    /*public static final*/ MISSED /* = new MISSED() */,
    /*public static final*/ REJECTED /* = new REJECTED() */,
    /*public static final*/ BUSY /* = new BUSY() */,
    /*public static final*/ NETWORK_ERROR /* = new NETWORK_ERROR() */,
    /*public static final*/ TIMEOUT /* = new TIMEOUT() */;
    
    CallEndReason() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.CallEndReason> getEntries() {
        return null;
    }
}