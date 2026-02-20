package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000f\b\u0086\u0081\u0002\u0018\u0000 \u00112\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u0011B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\rj\u0002\b\u000ej\u0002\b\u000fj\u0002\b\u0010\u00a8\u0006\u0012"}, d2 = {"Lcom/rendly/app/data/model/NotificationType;", "", "value", "", "(Ljava/lang/String;ILjava/lang/String;)V", "getValue", "()Ljava/lang/String;", "LIKE", "SAVE", "FOLLOW", "COMMENT", "MENTION", "CLIENT_REQUEST", "CLIENT_ACCEPTED", "CLIENT_REJECTED", "CLIENT_PENDING", "UNKNOWN", "Companion", "app_debug"})
public enum NotificationType {
    /*public static final*/ LIKE /* = new LIKE(null) */,
    /*public static final*/ SAVE /* = new SAVE(null) */,
    /*public static final*/ FOLLOW /* = new FOLLOW(null) */,
    /*public static final*/ COMMENT /* = new COMMENT(null) */,
    /*public static final*/ MENTION /* = new MENTION(null) */,
    /*public static final*/ CLIENT_REQUEST /* = new CLIENT_REQUEST(null) */,
    /*public static final*/ CLIENT_ACCEPTED /* = new CLIENT_ACCEPTED(null) */,
    /*public static final*/ CLIENT_REJECTED /* = new CLIENT_REJECTED(null) */,
    /*public static final*/ CLIENT_PENDING /* = new CLIENT_PENDING(null) */,
    /*public static final*/ UNKNOWN /* = new UNKNOWN(null) */;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String value = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.NotificationType.Companion Companion = null;
    
    NotificationType(java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getValue() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.NotificationType> getEntries() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rendly/app/data/model/NotificationType$Companion;", "", "()V", "fromString", "Lcom/rendly/app/data/model/NotificationType;", "value", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.NotificationType fromString(@org.jetbrains.annotations.NotNull
        java.lang.String value) {
            return null;
        }
    }
}