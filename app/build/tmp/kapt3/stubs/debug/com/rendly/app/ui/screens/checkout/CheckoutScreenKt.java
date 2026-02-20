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

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000Z\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0016\u001a0\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u000fH\u0003\u001a&\u0010\u0010\u001a\u00020\u00072\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u000fH\u0003\u001a\b\u0010\u0015\u001a\u00020\u0007H\u0003\u001aB\u0010\u0016\u001a\u00020\u00072\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f2\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00070\u00182\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f2\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0007\u001a,\u0010\u001d\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u001e\u001a\u00020\u001f2\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\u00070\u0018H\u0003\u001a,\u0010!\u001a\u00020\u00072\u0006\u0010\"\u001a\u00020\u00192\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00070\u000fH\u0003\u001a.\u0010#\u001a\u00020\u00072\f\u0010$\u001a\b\u0012\u0004\u0012\u00020&0%2\u0006\u0010\'\u001a\u00020\t2\u0006\u0010(\u001a\u00020\t2\u0006\u0010\b\u001a\u00020\tH\u0003\u001a,\u0010)\u001a\u00020\u00072\u0006\u0010*\u001a\u00020\u00122\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u000fH\u0003\u001a&\u0010,\u001a\u00020\u00072\u0006\u0010-\u001a\u00020\r2\u0006\u0010.\u001a\u00020\u000b2\f\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00070\u000fH\u0003\u001a&\u00100\u001a\u00020\u00072\b\u0010\f\u001a\u0004\u0018\u00010\r2\u0012\u00101\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00070\u0018H\u0003\u001a\u0012\u00102\u001a\u00020\u00072\b\u00103\u001a\u0004\u0018\u00010\rH\u0003\u001a,\u00104\u001a\u00020\u00072\u0006\u0010\"\u001a\u00020\u00192\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f2\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00070\u000fH\u0003\u001a\u0018\u00106\u001a\u00020\u00072\u0006\u00107\u001a\u00020\u00122\u0006\u00108\u001a\u00020\u0012H\u0003\u001a\b\u00109\u001a\u00020\u0007H\u0003\u001a\"\u0010:\u001a\u00020\u00072\u0006\u00107\u001a\u00020\u00122\u0006\u00108\u001a\u00020\t2\b\b\u0002\u0010;\u001a\u00020\u000bH\u0003\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u00a8\u0006<"}, d2 = {"MercadoPagoBlue", "Landroidx/compose/ui/graphics/Color;", "J", "MercadoPagoDark", "MercadoPagoYellow", "PriceColor", "CheckoutBottomBar", "", "totalAmount", "", "isLoading", "", "selectedMethod", "Lcom/rendly/app/ui/screens/checkout/PaymentMethod;", "onPay", "Lkotlin/Function0;", "CheckoutHeader", "title", "", "subtitle", "onBack", "CheckoutLoadingState", "CheckoutScreen", "onPaymentComplete", "Lkotlin/Function1;", "Lcom/rendly/app/data/model/Order;", "onContinueShopping", "modifier", "Landroidx/compose/ui/Modifier;", "InstallmentsSection", "selectedInstallments", "", "onInstallmentsSelected", "OrderReceiptScreen", "order", "OrderSummaryCard", "cartItems", "", "Lcom/rendly/app/data/repository/CartRepository$CartItem;", "subtotal", "shippingCost", "PaymentFailedState", "error", "onRetry", "PaymentMethodCard", "method", "isSelected", "onClick", "PaymentMethodsSection", "onMethodSelected", "PaymentProcessingState", "paymentMethod", "PaymentSuccessState", "onViewOrder", "ReceiptInfoRow", "label", "value", "SecurityInfoCard", "SummaryRow", "isFree", "app_debug"})
public final class CheckoutScreenKt {
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * CHECKOUT SCREEN - Pantalla de pago con Mercado Pago
     * ═══════════════════════════════════════════════════════════════════════════════
     *
     * Flujo de checkout profesional con soporte para Mercado Pago (Uruguay).
     * Incluye modo sandbox/test para pagos simulados.
     *
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    private static final long MercadoPagoBlue = 0L;
    private static final long MercadoPagoYellow = 0L;
    private static final long MercadoPagoDark = 0L;
    private static final long PriceColor = 0L;
    
    @androidx.compose.runtime.Composable
    public static final void CheckoutScreen(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.Order, kotlin.Unit> onPaymentComplete, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onContinueShopping, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CheckoutHeader(java.lang.String title, java.lang.String subtitle, kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OrderSummaryCard(java.util.List<com.rendly.app.data.repository.CartRepository.CartItem> cartItems, double subtotal, double shippingCost, double totalAmount) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SummaryRow(java.lang.String label, double value, boolean isFree) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentMethodsSection(com.rendly.app.ui.screens.checkout.PaymentMethod selectedMethod, kotlin.jvm.functions.Function1<? super com.rendly.app.ui.screens.checkout.PaymentMethod, kotlin.Unit> onMethodSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentMethodCard(com.rendly.app.ui.screens.checkout.PaymentMethod method, boolean isSelected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void InstallmentsSection(double totalAmount, int selectedInstallments, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onInstallmentsSelected) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SecurityInfoCard() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CheckoutBottomBar(double totalAmount, boolean isLoading, com.rendly.app.ui.screens.checkout.PaymentMethod selectedMethod, kotlin.jvm.functions.Function0<kotlin.Unit> onPay) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CheckoutLoadingState() {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentProcessingState(com.rendly.app.ui.screens.checkout.PaymentMethod paymentMethod) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentSuccessState(com.rendly.app.data.model.Order order, kotlin.jvm.functions.Function0<kotlin.Unit> onViewOrder, kotlin.jvm.functions.Function0<kotlin.Unit> onContinueShopping) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void OrderReceiptScreen(com.rendly.app.data.model.Order order, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onContinueShopping) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void ReceiptInfoRow(java.lang.String label, java.lang.String value) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void PaymentFailedState(java.lang.String error, kotlin.jvm.functions.Function0<kotlin.Unit> onRetry, kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
}