package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\t\u00a8\u0006\n"}, d2 = {"Lcom/rendly/app/data/model/PaymentStatus;", "", "(Ljava/lang/String;I)V", "PENDING", "IN_PROCESS", "APPROVED", "REJECTED", "CANCELLED", "REFUNDED", "CHARGED_BACK", "app_debug"})
public enum PaymentStatus {
    /*public static final*/ PENDING /* = new PENDING() */,
    /*public static final*/ IN_PROCESS /* = new IN_PROCESS() */,
    /*public static final*/ APPROVED /* = new APPROVED() */,
    /*public static final*/ REJECTED /* = new REJECTED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */,
    /*public static final*/ REFUNDED /* = new REFUNDED() */,
    /*public static final*/ CHARGED_BACK /* = new CHARGED_BACK() */;
    
    PaymentStatus() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.PaymentStatus> getEntries() {
        return null;
    }
}