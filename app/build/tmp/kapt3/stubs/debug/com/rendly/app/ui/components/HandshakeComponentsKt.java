package com.rendly.app.ui.components;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.SolidColor;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.HandshakeTransaction;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000B\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a,\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001ah\u0010\u0007\u001a\u00020\u00012\b\u0010\b\u001a\u0004\u0018\u00010\t2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a&\u0010\u0014\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\u00162\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001aD\u0010\u0018\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\b\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a.\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u00a8\u0006\u001f"}, d2 = {"CancelConfirmationModal", "", "productDescription", "", "onConfirmCancel", "Lkotlin/Function0;", "onDismiss", "HandshakeActiveBanner", "handshake", "Lcom/rendly/app/data/model/HandshakeTransaction;", "currentUserId", "otherUserName", "bannerState", "Lcom/rendly/app/ui/components/HandshakeBannerState;", "currentUserReputation", "", "reputationChange", "", "onConfirm", "onCancel", "HandshakeMessageBubble", "isFromMe", "", "onViewDetails", "HandshakeProposalModal", "initiatorUser", "Lcom/rendly/app/data/model/Usuario;", "onAccept", "onReject", "TransactionCompletedModal", "price", "app_debug"})
public final class HandshakeComponentsKt {
    
    /**
     * Modal que aparece al RECEPTOR cuando alguien inicia un handshake
     * Opciones: Aceptar, Rechazar
     */
    @androidx.compose.runtime.Composable
    public static final void HandshakeProposalModal(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.HandshakeTransaction handshake, @org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.Usuario initiatorUser, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onAccept, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onReject, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * Banner dinámico que muestra diferentes estados del handshake
     * Estados: WAITING → ACCEPTED → COMPLETED
     */
    @androidx.compose.runtime.Composable
    public static final void HandshakeActiveBanner(@org.jetbrains.annotations.Nullable
    com.rendly.app.data.model.HandshakeTransaction handshake, @org.jetbrains.annotations.NotNull
    java.lang.String currentUserId, @org.jetbrains.annotations.NotNull
    java.lang.String otherUserName, @org.jetbrains.annotations.NotNull
    com.rendly.app.ui.components.HandshakeBannerState bannerState, double currentUserReputation, int reputationChange, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onCancel, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * Mensaje especial de handshake en el chat
     */
    @androidx.compose.runtime.Composable
    public static final void HandshakeMessageBubble(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.HandshakeTransaction handshake, boolean isFromMe, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onViewDetails) {
    }
    
    /**
     * Modal de confirmación al cancelar una transacción
     */
    @androidx.compose.runtime.Composable
    public static final void CancelConfirmationModal(@org.jetbrains.annotations.NotNull
    java.lang.String productDescription, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirmCancel, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    /**
     * Modal de transacción completada - aparece cuando ambos confirman
     */
    @androidx.compose.runtime.Composable
    public static final void TransactionCompletedModal(@org.jetbrains.annotations.NotNull
    java.lang.String productDescription, double price, @org.jetbrains.annotations.NotNull
    java.lang.String otherUserName, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
}