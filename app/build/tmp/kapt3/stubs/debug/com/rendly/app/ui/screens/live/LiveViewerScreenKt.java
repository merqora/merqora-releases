package com.rendly.app.ui.screens.live;

import android.view.ViewGroup;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import com.rendly.app.data.repository.LiveStream;
import com.rendly.app.data.repository.LiveStreamRepository;
import com.rendly.app.data.webrtc.SignalingClient;
import com.rendly.app.ui.theme.*;
import org.webrtc.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u001a(\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001a\"\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0003\u001a.\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aj\u0010\u0014\u001a\u00020\u00012\u0006\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u000e\u0010\u001c\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001e0\u001d2\u000e\u0010\u001f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\n0\u001d2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\"H\u0002\u00a8\u0006#"}, d2 = {"LiveViewerScreen", "", "streamId", "", "onClose", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "RemoteVideoView", "videoTrack", "Lorg/webrtc/VideoTrack;", "eglBase", "Lorg/webrtc/EglBase;", "ViewerHeader", "stream", "Lcom/rendly/app/data/repository/LiveStream;", "viewerCount", "", "pulseAlpha", "", "handleOfferFromBroadcaster", "senderId", "sdp", "Lorg/webrtc/SessionDescription;", "factory", "Lorg/webrtc/PeerConnectionFactory;", "signalingClient", "Lcom/rendly/app/data/webrtc/SignalingClient;", "peerConnection", "Landroidx/compose/runtime/MutableState;", "Lorg/webrtc/PeerConnection;", "remoteVideoTrack", "onConnected", "onError", "Lkotlin/Function1;", "app_debug"})
public final class LiveViewerScreenKt {
    
    /**
     * Pantalla para ver una transmisión en vivo como espectador (Viewer)
     * Conecta via WebRTC P2P al broadcaster
     */
    @androidx.compose.runtime.Composable
    public static final void LiveViewerScreen(@org.jetbrains.annotations.NotNull
    java.lang.String streamId, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClose, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Manejar offer SDP del broadcaster
     */
    private static final void handleOfferFromBroadcaster(java.lang.String senderId, org.webrtc.SessionDescription sdp, org.webrtc.PeerConnectionFactory factory, com.rendly.app.data.webrtc.SignalingClient signalingClient, androidx.compose.runtime.MutableState<org.webrtc.PeerConnection> peerConnection, androidx.compose.runtime.MutableState<org.webrtc.VideoTrack> remoteVideoTrack, kotlin.jvm.functions.Function0<kotlin.Unit> onConnected, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError) {
    }
    
    /**
     * Vista de video remoto usando SurfaceViewRenderer
     */
    @androidx.compose.runtime.Composable
    private static final void RemoteVideoView(org.webrtc.VideoTrack videoTrack, org.webrtc.EglBase eglBase, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Header del viewer
     */
    @androidx.compose.runtime.Composable
    private static final void ViewerHeader(com.rendly.app.data.repository.LiveStream stream, int viewerCount, float pulseAlpha, kotlin.jvm.functions.Function0<kotlin.Unit> onClose) {
    }
}