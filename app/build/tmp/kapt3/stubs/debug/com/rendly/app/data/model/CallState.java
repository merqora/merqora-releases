package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Estado completo de la llamada para la UI
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001f\b\u0086\b\u0018\u00002\u00020\u0001Bw\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\b\u0012\b\b\u0002\u0010\r\u001a\u00020\b\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\u0002\u0010\u0012J\u000b\u0010 \u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\u000fH\u00c6\u0003J\u000b\u0010\"\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003J\t\u0010#\u001a\u00020\u0005H\u00c6\u0003J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\bH\u00c6\u0003J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\bH\u00c6\u0003J\t\u0010*\u001a\u00020\bH\u00c6\u0003J{\u0010+\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\b2\b\b\u0002\u0010\r\u001a\u00020\b2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u00c6\u0001J\u0013\u0010,\u001a\u00020\b2\b\u0010-\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010.\u001a\u00020\u000fH\u00d6\u0001J\t\u0010/\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0014R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\f\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u001aR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u001aR\u0011\u0010\r\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u001aR\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0014R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0014R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0014R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001f\u00a8\u00060"}, d2 = {"Lcom/rendly/app/data/model/CallState;", "", "callId", "", "status", "Lcom/rendly/app/data/model/CallStatus;", "callType", "isOutgoing", "", "otherUserId", "otherUsername", "otherAvatarUrl", "isMuted", "isSpeakerOn", "durationSeconds", "", "endReason", "Lcom/rendly/app/data/model/CallEndReason;", "(Ljava/lang/String;Lcom/rendly/app/data/model/CallStatus;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZILcom/rendly/app/data/model/CallEndReason;)V", "getCallId", "()Ljava/lang/String;", "getCallType", "getDurationSeconds", "()I", "getEndReason", "()Lcom/rendly/app/data/model/CallEndReason;", "()Z", "getOtherAvatarUrl", "getOtherUserId", "getOtherUsername", "getStatus", "()Lcom/rendly/app/data/model/CallStatus;", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class CallState {
    @org.jetbrains.annotations.Nullable
    private final java.lang.String callId = null;
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.model.CallStatus status = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String callType = null;
    private final boolean isOutgoing = false;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String otherUserId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String otherUsername = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String otherAvatarUrl = null;
    private final boolean isMuted = false;
    private final boolean isSpeakerOn = false;
    private final int durationSeconds = 0;
    @org.jetbrains.annotations.Nullable
    private final com.rendly.app.data.model.CallEndReason endReason = null;
    
    public CallState(@org.jetbrains.annotations.Nullable
    java.lang.String callId, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.CallStatus status, @org.jetbrains.annotations.NotNull
    java.lang.String callType, boolean isOutgoing, @org.jetbrains.annotations.NotNull
    java.lang.String otherUserId, @org.jetbrains.annotations.NotNull
    java.lang.String otherUsername, @org.jetbrains.annotations.NotNull
    java.lang.String otherAvatarUrl, boolean isMuted, boolean isSpeakerOn, int durationSeconds, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.CallEndReason endReason) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCallId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.CallStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCallType() {
        return null;
    }
    
    public final boolean isOutgoing() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getOtherUserId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getOtherUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getOtherAvatarUrl() {
        return null;
    }
    
    public final boolean isMuted() {
        return false;
    }
    
    public final boolean isSpeakerOn() {
        return false;
    }
    
    public final int getDurationSeconds() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.CallEndReason getEndReason() {
        return null;
    }
    
    public CallState() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component1() {
        return null;
    }
    
    public final int component10() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.rendly.app.data.model.CallEndReason component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.CallStatus component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component6() {
        return null;
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
    public final com.rendly.app.data.model.CallState copy(@org.jetbrains.annotations.Nullable
    java.lang.String callId, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.CallStatus status, @org.jetbrains.annotations.NotNull
    java.lang.String callType, boolean isOutgoing, @org.jetbrains.annotations.NotNull
    java.lang.String otherUserId, @org.jetbrains.annotations.NotNull
    java.lang.String otherUsername, @org.jetbrains.annotations.NotNull
    java.lang.String otherAvatarUrl, boolean isMuted, boolean isSpeakerOn, int durationSeconds, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.CallEndReason endReason) {
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