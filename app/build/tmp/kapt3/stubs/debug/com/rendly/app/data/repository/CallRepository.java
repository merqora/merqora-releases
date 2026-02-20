package com.rendly.app.data.repository;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import com.rendly.app.data.model.CallDB;
import com.rendly.app.data.model.CallEndReason;
import com.rendly.app.data.model.CallState;
import com.rendly.app.data.model.CallStatus;
import com.rendly.app.data.model.IceCandidateDB;
import com.rendly.app.data.remote.SupabaseClient;
import io.github.jan.supabase.realtime.PostgresAction;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.json.JsonNull;
import org.webrtc.*;

/**
 * ═══════════════════════════════════════════════════════════════
 * CALL REPOSITORY - Motor de llamadas VoIP
 * ═══════════════════════════════════════════════════════════════
 *
 * Arquitectura:
 * - WebRTC para audio peer-to-peer (baja latencia)
 * - Supabase Realtime para señalización (offer/answer/ICE)
 * - STUN/TURN servers para NAT traversal
 * - AudioManager para gestión de audio del dispositivo
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00aa\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b&\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001nB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u00107\u001a\u0002082\u0006\u00109\u001a\u00020 H\u0002J\u0016\u0010:\u001a\u0002082\u0006\u0010;\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010<J\b\u0010=\u001a\u000208H\u0002J\u0010\u0010>\u001a\u0002082\u0006\u0010;\u001a\u00020\u0004H\u0002J\u0010\u0010?\u001a\u0002082\u0006\u0010;\u001a\u00020\u0004H\u0002J\u0018\u0010@\u001a\u0002082\u0006\u0010;\u001a\u00020\u00042\u0006\u0010A\u001a\u00020\u0004H\u0002J\u0018\u0010B\u001a\u0002082\b\b\u0002\u0010C\u001a\u00020DH\u0086@\u00a2\u0006\u0002\u0010EJ\b\u0010F\u001a\u000208H\u0002J\u000e\u0010G\u001a\u00020\u00042\u0006\u0010H\u001a\u00020IJ\u000e\u0010J\u001a\u0002082\u0006\u0010K\u001a\u00020\nJ\u0010\u0010L\u001a\u0002082\u0006\u0010K\u001a\u00020\nH\u0002J\b\u0010M\u001a\u000208H\u0002J\b\u0010N\u001a\u000208H\u0002J)\u0010O\u001a\u0002082\u0006\u0010P\u001a\u00020\u00042\b\u0010Q\u001a\u0004\u0018\u00010\u00042\b\u0010R\u001a\u0004\u0018\u00010IH\u0002\u00a2\u0006\u0002\u0010SJ\u0016\u0010T\u001a\u0002082\u0006\u0010;\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010<J\b\u0010U\u001a\u000208H\u0002J\b\u0010V\u001a\u000208H\u0002J.\u0010W\u001a\u0002082\u0006\u0010X\u001a\u00020\u00042\u0006\u0010Y\u001a\u00020\u00042\u0006\u0010;\u001a\u00020\u00042\u0006\u0010Z\u001a\u00020\u0004H\u0082@\u00a2\u0006\u0002\u0010[J&\u0010\\\u001a\u0002082\u0006\u0010;\u001a\u00020\u00042\u0006\u0010]\u001a\u00020\u00042\u0006\u0010^\u001a\u00020.H\u0082@\u00a2\u0006\u0002\u0010_J\u0010\u0010`\u001a\u0002082\u0006\u0010a\u001a\u00020 H\u0002J0\u0010b\u001a\u00020 2\u0006\u0010X\u001a\u00020\u00042\u0006\u0010Y\u001a\u00020\u00042\u0006\u0010c\u001a\u00020\u00042\b\b\u0002\u0010Z\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010[J\b\u0010d\u001a\u000208H\u0002J\b\u0010e\u001a\u000208H\u0002J\b\u0010f\u001a\u000208H\u0002J\b\u0010g\u001a\u000208H\u0002J\b\u0010h\u001a\u000208H\u0002J\u0018\u0010i\u001a\u0002082\u0006\u0010;\u001a\u00020\u00042\u0006\u0010A\u001a\u00020\u0004H\u0002J\b\u0010j\u001a\u000208H\u0002J\b\u0010k\u001a\u000208H\u0002J\u0006\u0010l\u001a\u000208J\u0006\u0010m\u001a\u000208R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00070\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0019\u001a\u0010\u0012\f\u0012\n \u001c*\u0004\u0018\u00010\u001b0\u001b0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001d\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0014R\u000e\u0010\u001f\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010$\u001a\u0004\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010&\u001a\u0004\u0018\u00010\'X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010(\u001a\u0004\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010*\u001a\u0004\u0018\u00010+X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010,\u001a\b\u0012\u0004\u0012\u00020.0-X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010/\u001a\u0004\u0018\u000100X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u00101\u001a\u0004\u0018\u000100X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00102\u001a\u000203X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u00104\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u00105\u001a\u0004\u0018\u000106X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006o"}, d2 = {"Lcom/rendly/app/data/repository/CallRepository;", "", "()V", "TAG", "", "_callState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/rendly/app/data/model/CallState;", "_incomingCall", "appContext", "Landroid/content/Context;", "audioFocusRequest", "Landroid/media/AudioFocusRequest;", "audioManager", "Landroid/media/AudioManager;", "callChannel", "Lio/github/jan/supabase/realtime/RealtimeChannel;", "callState", "Lkotlinx/coroutines/flow/StateFlow;", "getCallState", "()Lkotlinx/coroutines/flow/StateFlow;", "durationJob", "Lkotlinx/coroutines/Job;", "globalCallChannel", "iceChannel", "iceServers", "", "Lorg/webrtc/PeerConnection$IceServer;", "kotlin.jvm.PlatformType", "incomingCall", "getIncomingCall", "initialized", "", "isEnding", "isRemoteDescriptionSet", "isResetting", "localAudioSource", "Lorg/webrtc/AudioSource;", "localAudioTrack", "Lorg/webrtc/AudioTrack;", "peerConnection", "Lorg/webrtc/PeerConnection;", "peerConnectionFactory", "Lorg/webrtc/PeerConnectionFactory;", "pendingIceCandidates", "", "Lorg/webrtc/IceCandidate;", "ringbackPlayer", "Landroid/media/MediaPlayer;", "ringtonePlayer", "scope", "Lkotlinx/coroutines/CoroutineScope;", "timeoutJob", "vibrator", "Landroid/os/Vibrator;", "adjustVolumeForMode", "", "isSpeaker", "answerCall", "callId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "configureAudioForCall", "createAnswer", "createOffer", "createPeerConnection", "currentUserId", "endCall", "reason", "Lcom/rendly/app/data/model/CallEndReason;", "(Lcom/rendly/app/data/model/CallEndReason;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureInitialized", "formatDuration", "seconds", "", "initialize", "context", "initializeWebRTC", "playRingback", "playRingtone", "processRemoteIceCandidate", "candidateStr", "sdpMid", "sdpMLineIndex", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;)V", "rejectCall", "requestAudioFocus", "resetState", "sendCallPushNotification", "calleeId", "calleeUsername", "callType", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendIceCandidate", "senderId", "candidate", "(Ljava/lang/String;Ljava/lang/String;Lorg/webrtc/IceCandidate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setSpeakerOutput", "enabled", "startCall", "calleeAvatarUrl", "startDurationTimer", "startVibration", "stopRingback", "stopRingtone", "stopVibration", "subscribeToCallUpdates", "subscribeToIncomingCalls", "subscribeToIncomingCallsWithRetry", "toggleMute", "toggleSpeaker", "FcmTokenDB", "app_debug"})
public final class CallRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "CallRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.model.CallState> _callState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.model.CallState> callState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.model.CallState> _incomingCall = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.model.CallState> incomingCall = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable
    private static android.content.Context appContext;
    @org.jetbrains.annotations.Nullable
    private static android.media.AudioManager audioManager;
    @org.jetbrains.annotations.Nullable
    private static org.webrtc.PeerConnectionFactory peerConnectionFactory;
    @org.jetbrains.annotations.Nullable
    private static org.webrtc.PeerConnection peerConnection;
    @org.jetbrains.annotations.Nullable
    private static org.webrtc.AudioTrack localAudioTrack;
    @org.jetbrains.annotations.Nullable
    private static org.webrtc.AudioSource localAudioSource;
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel callChannel;
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel iceChannel;
    @org.jetbrains.annotations.Nullable
    private static io.github.jan.supabase.realtime.RealtimeChannel globalCallChannel;
    @org.jetbrains.annotations.Nullable
    private static android.media.MediaPlayer ringtonePlayer;
    @org.jetbrains.annotations.Nullable
    private static android.media.MediaPlayer ringbackPlayer;
    @org.jetbrains.annotations.Nullable
    private static android.os.Vibrator vibrator;
    @org.jetbrains.annotations.Nullable
    private static android.media.AudioFocusRequest audioFocusRequest;
    @org.jetbrains.annotations.Nullable
    private static kotlinx.coroutines.Job durationJob;
    @org.jetbrains.annotations.Nullable
    private static kotlinx.coroutines.Job timeoutJob;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<org.webrtc.IceCandidate> pendingIceCandidates = null;
    private static boolean isRemoteDescriptionSet = false;
    private static boolean isResetting = false;
    private static boolean isEnding = false;
    @org.jetbrains.annotations.NotNull
    private static final java.util.List<org.webrtc.PeerConnection.IceServer> iceServers = null;
    private static boolean initialized = false;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.CallRepository INSTANCE = null;
    
    private CallRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.model.CallState> getCallState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.model.CallState> getIncomingCall() {
        return null;
    }
    
    public final void initialize(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    private final void ensureInitialized() {
    }
    
    private final void subscribeToIncomingCallsWithRetry() {
    }
    
    private final void initializeWebRTC(android.content.Context context) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object startCall(@org.jetbrains.annotations.NotNull
    java.lang.String calleeId, @org.jetbrains.annotations.NotNull
    java.lang.String calleeUsername, @org.jetbrains.annotations.NotNull
    java.lang.String calleeAvatarUrl, @org.jetbrains.annotations.NotNull
    java.lang.String callType, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object answerCall(@org.jetbrains.annotations.NotNull
    java.lang.String callId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object rejectCall(@org.jetbrains.annotations.NotNull
    java.lang.String callId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object endCall(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.CallEndReason reason, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void toggleMute() {
    }
    
    public final void toggleSpeaker() {
    }
    
    /**
     * Adjust STREAM_VOICE_CALL volume based on speaker/earpiece mode.
     * Speaker mode uses lower volume (~50%) to prevent acoustic echo feedback.
     * Earpiece mode uses higher volume (~80%) for comfortable listening.
     */
    private final void adjustVolumeForMode(boolean isSpeaker) {
    }
    
    private final void setSpeakerOutput(boolean enabled) {
    }
    
    private final void createPeerConnection(java.lang.String callId, java.lang.String currentUserId) {
    }
    
    private final void createOffer(java.lang.String callId) {
    }
    
    private final void createAnswer(java.lang.String callId) {
    }
    
    private final java.lang.Object sendIceCandidate(java.lang.String callId, java.lang.String senderId, org.webrtc.IceCandidate candidate, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void processRemoteIceCandidate(java.lang.String candidateStr, java.lang.String sdpMid, java.lang.Integer sdpMLineIndex) {
    }
    
    private final void subscribeToIncomingCalls() {
    }
    
    private final void subscribeToCallUpdates(java.lang.String callId, java.lang.String currentUserId) {
    }
    
    private final void requestAudioFocus() {
    }
    
    private final void configureAudioForCall() {
    }
    
    private final void playRingtone() {
    }
    
    private final void stopRingtone() {
    }
    
    private final void playRingback() {
    }
    
    private final void stopRingback() {
    }
    
    private final void startVibration() {
    }
    
    private final void stopVibration() {
    }
    
    private final void startDurationTimer() {
    }
    
    private final void resetState() {
    }
    
    private final java.lang.Object sendCallPushNotification(java.lang.String calleeId, java.lang.String calleeUsername, java.lang.String callId, java.lang.String callType, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Formato de duración mm:ss
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String formatDuration(int seconds) {
        return null;
    }
    
    @kotlinx.serialization.Serializable
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0083\b\u0018\u0000 $2\u00020\u0001:\u0002#$B9\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\nB#\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0012\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0005H\u00c6\u0003J\'\u0010\u0015\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\u0005H\u00d6\u0001J&\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u00002\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!H\u00c1\u0001\u00a2\u0006\u0002\b\"R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rR\u001c\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u000f\u0010\u0010\u001a\u0004\b\u0011\u0010\r\u00a8\u0006%"}, d2 = {"Lcom/rendly/app/data/repository/CallRepository$FcmTokenDB;", "", "seen1", "", "id", "", "token", "userId", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getId", "()Ljava/lang/String;", "getToken", "getUserId$annotations", "()V", "getUserId", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
    static final class FcmTokenDB {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String id = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String token = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String userId = null;
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CallRepository.FcmTokenDB.Companion Companion = null;
        
        public FcmTokenDB(@org.jetbrains.annotations.NotNull
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String token, @org.jetbrains.annotations.NotNull
        java.lang.String userId) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getToken() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getUserId() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "user_id")
        @java.lang.Deprecated
        public static void getUserId$annotations() {
        }
        
        public FcmTokenDB() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
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
        public final com.rendly.app.data.repository.CallRepository.FcmTokenDB copy(@org.jetbrains.annotations.NotNull
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String token, @org.jetbrains.annotations.NotNull
        java.lang.String userId) {
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
        com.rendly.app.data.repository.CallRepository.FcmTokenDB self, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/repository/CallRepository.FcmTokenDB.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/repository/CallRepository$FcmTokenDB;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
        @java.lang.Deprecated
        public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.repository.CallRepository.FcmTokenDB> {
            @org.jetbrains.annotations.NotNull
            public static final com.rendly.app.data.repository.CallRepository.FcmTokenDB.$serializer INSTANCE = null;
            
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
            public com.rendly.app.data.repository.CallRepository.FcmTokenDB deserialize(@org.jetbrains.annotations.NotNull
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
            com.rendly.app.data.repository.CallRepository.FcmTokenDB value) {
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/repository/CallRepository$FcmTokenDB$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/repository/CallRepository$FcmTokenDB;", "app_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull
            public final kotlinx.serialization.KSerializer<com.rendly.app.data.repository.CallRepository.FcmTokenDB> serializer() {
                return null;
            }
        }
    }
}