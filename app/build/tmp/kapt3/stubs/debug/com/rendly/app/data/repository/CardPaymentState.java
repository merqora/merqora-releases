package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.BuildConfig;
import com.rendly.app.data.model.*;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.util.concurrent.TimeUnit;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * CARD PAYMENT REPOSITORY - Checkout API de Mercado Pago
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Gestiona el flujo de pago con tarjeta usando Checkout API:
 * 1. Obtener métodos de pago disponibles
 * 2. Obtener emisores (bancos) para un método de pago
 * 3. Obtener cuotas disponibles
 * 4. Tokenizar tarjeta (crear card_token)
 * 5. Procesar pago con el token
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\b\u0003\u0004\u0005\u0006\u0007\b\t\nB\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\b\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState;", "", "()V", "Error", "Idle", "LoadingPaymentMethods", "PaymentPending", "PaymentRejected", "PaymentSuccess", "ProcessingPayment", "TokenizingCard", "Lcom/rendly/app/data/repository/CardPaymentState$Error;", "Lcom/rendly/app/data/repository/CardPaymentState$Idle;", "Lcom/rendly/app/data/repository/CardPaymentState$LoadingPaymentMethods;", "Lcom/rendly/app/data/repository/CardPaymentState$PaymentPending;", "Lcom/rendly/app/data/repository/CardPaymentState$PaymentRejected;", "Lcom/rendly/app/data/repository/CardPaymentState$PaymentSuccess;", "Lcom/rendly/app/data/repository/CardPaymentState$ProcessingPayment;", "Lcom/rendly/app/data/repository/CardPaymentState$TokenizingCard;", "app_debug"})
public abstract class CardPaymentState {
    
    private CardPaymentState() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$Error;", "Lcom/rendly/app/data/repository/CardPaymentState;", "message", "", "(Ljava/lang/String;)V", "getMessage", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class Error extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String message = null;
        
        public Error(@org.jetbrains.annotations.NotNull
        java.lang.String message) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CardPaymentState.Error copy(@org.jetbrains.annotations.NotNull
        java.lang.String message) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$Idle;", "Lcom/rendly/app/data/repository/CardPaymentState;", "()V", "app_debug"})
    public static final class Idle extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CardPaymentState.Idle INSTANCE = null;
        
        private Idle() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$LoadingPaymentMethods;", "Lcom/rendly/app/data/repository/CardPaymentState;", "()V", "app_debug"})
    public static final class LoadingPaymentMethods extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CardPaymentState.LoadingPaymentMethods INSTANCE = null;
        
        private LoadingPaymentMethods() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$PaymentPending;", "Lcom/rendly/app/data/repository/CardPaymentState;", "response", "Lcom/rendly/app/data/model/PaymentResponse;", "(Lcom/rendly/app/data/model/PaymentResponse;)V", "getResponse", "()Lcom/rendly/app/data/model/PaymentResponse;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class PaymentPending extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.PaymentResponse response = null;
        
        public PaymentPending(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.PaymentResponse response) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.PaymentResponse getResponse() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.PaymentResponse component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CardPaymentState.PaymentPending copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.PaymentResponse response) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\f\u001a\u00020\u0005H\u00c6\u0003J\u001d\u0010\r\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u00d6\u0003J\t\u0010\u0012\u001a\u00020\u0013H\u00d6\u0001J\t\u0010\u0014\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0015"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$PaymentRejected;", "Lcom/rendly/app/data/repository/CardPaymentState;", "response", "Lcom/rendly/app/data/model/PaymentResponse;", "reason", "", "(Lcom/rendly/app/data/model/PaymentResponse;Ljava/lang/String;)V", "getReason", "()Ljava/lang/String;", "getResponse", "()Lcom/rendly/app/data/model/PaymentResponse;", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class PaymentRejected extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.PaymentResponse response = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String reason = null;
        
        public PaymentRejected(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.PaymentResponse response, @org.jetbrains.annotations.NotNull
        java.lang.String reason) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.PaymentResponse getResponse() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getReason() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.PaymentResponse component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CardPaymentState.PaymentRejected copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.PaymentResponse response, @org.jetbrains.annotations.NotNull
        java.lang.String reason) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$PaymentSuccess;", "Lcom/rendly/app/data/repository/CardPaymentState;", "response", "Lcom/rendly/app/data/model/PaymentResponse;", "(Lcom/rendly/app/data/model/PaymentResponse;)V", "getResponse", "()Lcom/rendly/app/data/model/PaymentResponse;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class PaymentSuccess extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.PaymentResponse response = null;
        
        public PaymentSuccess(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.PaymentResponse response) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.PaymentResponse getResponse() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.PaymentResponse component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CardPaymentState.PaymentSuccess copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.PaymentResponse response) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$ProcessingPayment;", "Lcom/rendly/app/data/repository/CardPaymentState;", "()V", "app_debug"})
    public static final class ProcessingPayment extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CardPaymentState.ProcessingPayment INSTANCE = null;
        
        private ProcessingPayment() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentState$TokenizingCard;", "Lcom/rendly/app/data/repository/CardPaymentState;", "()V", "app_debug"})
    public static final class TokenizingCard extends com.rendly.app.data.repository.CardPaymentState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CardPaymentState.TokenizingCard INSTANCE = null;
        
        private TokenizingCard() {
        }
    }
}