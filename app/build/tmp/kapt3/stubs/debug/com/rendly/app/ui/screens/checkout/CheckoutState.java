package com.rendly.app.ui.screens.checkout;

import android.widget.Toast;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextDecoration;
import androidx.compose.ui.text.style.TextOverflow;
import com.rendly.app.data.model.Order;
import com.rendly.app.data.model.OrderStatus;
import com.rendly.app.data.model.PaymentStatus;
import com.rendly.app.data.model.PaymentResponse;
import com.rendly.app.data.repository.CartRepository;
import com.rendly.app.data.repository.CardPaymentRepository;
import com.rendly.app.data.repository.MercadoPagoRepository;
import com.rendly.app.data.repository.MPItem;
import com.rendly.app.data.repository.OrderRepository;
import com.rendly.app.ui.theme.*;
import com.rendly.app.data.remote.SupabaseClient;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0007\u0003\u0004\u0005\u0006\u0007\b\tB\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0007\n\u000b\f\r\u000e\u000f\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "", "()V", "EnteringCardDetails", "Loading", "PaymentFailed", "PaymentSuccess", "ProcessingPayment", "SelectingPayment", "ViewingReceipt", "Lcom/rendly/app/ui/screens/checkout/CheckoutState$EnteringCardDetails;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState$Loading;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState$PaymentFailed;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState$PaymentSuccess;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState$ProcessingPayment;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState$SelectingPayment;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState$ViewingReceipt;", "app_debug"})
public abstract class CheckoutState {
    
    private CheckoutState() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState$EnteringCardDetails;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "orderId", "", "(Ljava/lang/String;)V", "getOrderId", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class EnteringCardDetails extends com.rendly.app.ui.screens.checkout.CheckoutState {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String orderId = null;
        
        public EnteringCardDetails(@org.jetbrains.annotations.NotNull
        java.lang.String orderId) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getOrderId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.ui.screens.checkout.CheckoutState.EnteringCardDetails copy(@org.jetbrains.annotations.NotNull
        java.lang.String orderId) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState$Loading;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "()V", "app_debug"})
    public static final class Loading extends com.rendly.app.ui.screens.checkout.CheckoutState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.checkout.CheckoutState.Loading INSTANCE = null;
        
        private Loading() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\n\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u001f\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u00d6\u0003J\t\u0010\u0010\u001a\u00020\u0011H\u00d6\u0001J\t\u0010\u0012\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0013"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState$PaymentFailed;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "error", "", "orderId", "(Ljava/lang/String;Ljava/lang/String;)V", "getError", "()Ljava/lang/String;", "getOrderId", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class PaymentFailed extends com.rendly.app.ui.screens.checkout.CheckoutState {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String error = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String orderId = null;
        
        public PaymentFailed(@org.jetbrains.annotations.NotNull
        java.lang.String error, @org.jetbrains.annotations.Nullable
        java.lang.String orderId) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getError() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getOrderId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.ui.screens.checkout.CheckoutState.PaymentFailed copy(@org.jetbrains.annotations.NotNull
        java.lang.String error, @org.jetbrains.annotations.Nullable
        java.lang.String orderId) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState$PaymentSuccess;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "order", "Lcom/rendly/app/data/model/Order;", "(Lcom/rendly/app/data/model/Order;)V", "getOrder", "()Lcom/rendly/app/data/model/Order;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class PaymentSuccess extends com.rendly.app.ui.screens.checkout.CheckoutState {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.Order order = null;
        
        public PaymentSuccess(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.Order order) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Order getOrder() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Order component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.ui.screens.checkout.CheckoutState.PaymentSuccess copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.Order order) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState$ProcessingPayment;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "()V", "app_debug"})
    public static final class ProcessingPayment extends com.rendly.app.ui.screens.checkout.CheckoutState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.checkout.CheckoutState.ProcessingPayment INSTANCE = null;
        
        private ProcessingPayment() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState$SelectingPayment;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "()V", "app_debug"})
    public static final class SelectingPayment extends com.rendly.app.ui.screens.checkout.CheckoutState {
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.ui.screens.checkout.CheckoutState.SelectingPayment INSTANCE = null;
        
        private SelectingPayment() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/CheckoutState$ViewingReceipt;", "Lcom/rendly/app/ui/screens/checkout/CheckoutState;", "order", "Lcom/rendly/app/data/model/Order;", "(Lcom/rendly/app/data/model/Order;)V", "getOrder", "()Lcom/rendly/app/data/model/Order;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class ViewingReceipt extends com.rendly.app.ui.screens.checkout.CheckoutState {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.Order order = null;
        
        public ViewingReceipt(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.Order order) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Order getOrder() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Order component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.ui.screens.checkout.CheckoutState.ViewingReceipt copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.Order order) {
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