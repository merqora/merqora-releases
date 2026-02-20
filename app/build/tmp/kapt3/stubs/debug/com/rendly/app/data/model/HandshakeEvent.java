package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Evento de Realtime para cambios en handshake
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0003\u0003\u0004\u0005B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0003\u0006\u0007\b\u00a8\u0006\t"}, d2 = {"Lcom/rendly/app/data/model/HandshakeEvent;", "", "()V", "Created", "Deleted", "Updated", "Lcom/rendly/app/data/model/HandshakeEvent$Created;", "Lcom/rendly/app/data/model/HandshakeEvent$Deleted;", "Lcom/rendly/app/data/model/HandshakeEvent$Updated;", "app_debug"})
public abstract class HandshakeEvent {
    
    private HandshakeEvent() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/data/model/HandshakeEvent$Created;", "Lcom/rendly/app/data/model/HandshakeEvent;", "handshake", "Lcom/rendly/app/data/model/HandshakeTransaction;", "(Lcom/rendly/app/data/model/HandshakeTransaction;)V", "getHandshake", "()Lcom/rendly/app/data/model/HandshakeTransaction;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Created extends com.rendly.app.data.model.HandshakeEvent {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.HandshakeTransaction handshake = null;
        
        public Created(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.HandshakeTransaction handshake) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.HandshakeTransaction getHandshake() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.HandshakeTransaction component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.HandshakeEvent.Created copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.HandshakeTransaction handshake) {
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
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/rendly/app/data/model/HandshakeEvent$Deleted;", "Lcom/rendly/app/data/model/HandshakeEvent;", "handshakeId", "", "(Ljava/lang/String;)V", "getHandshakeId", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class Deleted extends com.rendly.app.data.model.HandshakeEvent {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String handshakeId = null;
        
        public Deleted(@org.jetbrains.annotations.NotNull
        java.lang.String handshakeId) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getHandshakeId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.HandshakeEvent.Deleted copy(@org.jetbrains.annotations.NotNull
        java.lang.String handshakeId) {
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
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/data/model/HandshakeEvent$Updated;", "Lcom/rendly/app/data/model/HandshakeEvent;", "handshake", "Lcom/rendly/app/data/model/HandshakeTransaction;", "(Lcom/rendly/app/data/model/HandshakeTransaction;)V", "getHandshake", "()Lcom/rendly/app/data/model/HandshakeTransaction;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Updated extends com.rendly.app.data.model.HandshakeEvent {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.HandshakeTransaction handshake = null;
        
        public Updated(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.HandshakeTransaction handshake) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.HandshakeTransaction getHandshake() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.HandshakeTransaction component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.HandshakeEvent.Updated copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.HandshakeTransaction handshake) {
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
    }
}