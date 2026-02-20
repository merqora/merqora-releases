package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlinx.serialization.Serializable
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0087\u0081\u0002\u0018\u0000 \u00072\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u0007B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\b"}, d2 = {"Lcom/rendly/app/data/model/AddressStatus;", "", "(Ljava/lang/String;I)V", "VALID", "SUSPICIOUS", "INVALID", "PENDING", "Companion", "app_debug"})
public enum AddressStatus {
    @kotlinx.serialization.SerialName(value = "valid")
    /*public static final*/ VALID /* = new VALID() */,
    @kotlinx.serialization.SerialName(value = "suspicious")
    /*public static final*/ SUSPICIOUS /* = new SUSPICIOUS() */,
    @kotlinx.serialization.SerialName(value = "invalid")
    /*public static final*/ INVALID /* = new INVALID() */,
    @kotlinx.serialization.SerialName(value = "pending")
    /*public static final*/ PENDING /* = new PENDING() */;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.AddressStatus.Companion Companion = null;
    
    AddressStatus() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.AddressStatus> getEntries() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/model/AddressStatus$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/model/AddressStatus;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final kotlinx.serialization.KSerializer<com.rendly.app.data.model.AddressStatus> serializer() {
            return null;
        }
    }
}