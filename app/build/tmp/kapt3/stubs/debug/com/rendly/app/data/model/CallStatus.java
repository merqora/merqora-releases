package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Estado de la llamada en la UI
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/rendly/app/data/model/CallStatus;", "", "(Ljava/lang/String;I)V", "IDLE", "OUTGOING", "INCOMING", "CONNECTED", "RECONNECTING", "ENDED", "app_debug"})
public enum CallStatus {
    /*public static final*/ IDLE /* = new IDLE() */,
    /*public static final*/ OUTGOING /* = new OUTGOING() */,
    /*public static final*/ INCOMING /* = new INCOMING() */,
    /*public static final*/ CONNECTED /* = new CONNECTED() */,
    /*public static final*/ RECONNECTING /* = new RECONNECTING() */,
    /*public static final*/ ENDED /* = new ENDED() */;
    
    CallStatus() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.CallStatus> getEntries() {
        return null;
    }
}