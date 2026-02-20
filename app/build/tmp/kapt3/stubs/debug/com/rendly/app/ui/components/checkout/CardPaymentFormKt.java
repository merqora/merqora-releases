package com.rendly.app.ui.components.checkout;

import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.focus.FocusDirection;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.SpanStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.*;
import com.rendly.app.data.model.*;
import com.rendly.app.data.repository.CardPaymentRepository;
import com.rendly.app.data.repository.CardPaymentState;
import com.rendly.app.ui.theme.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000~\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a`\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\r0\u00142\u0012\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\r0\u00142\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\r0\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u001aH\u0007\u001a(\u0010\u001b\u001a\u00020\r2\u0006\u0010\u001c\u001a\u00020\u00112\u0006\u0010\u001d\u001a\u00020\u00112\u0006\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u001f\u001a\u00020 H\u0003\u001a\u0096\u0001\u0010!\u001a\u00020\r2\u0006\u0010\"\u001a\u00020#2\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020#\u0012\u0004\u0012\u00020\r0\u00142\u0006\u0010%\u001a\u00020\u00112\u0006\u0010&\u001a\u00020\u00112\b\b\u0002\u0010\u0019\u001a\u00020\u001a2\u0015\b\u0002\u0010\'\u001a\u000f\u0012\u0004\u0012\u00020\r\u0018\u00010\u0018\u00a2\u0006\u0002\b(2\u0015\b\u0002\u0010)\u001a\u000f\u0012\u0004\u0012\u00020\r\u0018\u00010\u0018\u00a2\u0006\u0002\b(2\n\b\u0002\u0010*\u001a\u0004\u0018\u00010\u00112\b\b\u0002\u0010+\u001a\u00020,2\b\b\u0002\u0010-\u001a\u00020.2\b\b\u0002\u0010/\u001a\u000200H\u0003\u001a\u0010\u00101\u001a\u00020\r2\u0006\u0010\u001f\u001a\u00020 H\u0003\u001a\u0010\u00102\u001a\u00020\r2\u0006\u0010\u001f\u001a\u00020 H\u0003\u001a2\u00103\u001a\u00020\r2\f\u00104\u001a\b\u0012\u0004\u0012\u000206052\u0006\u00107\u001a\u0002082\u0012\u00109\u001a\u000e\u0012\u0004\u0012\u000208\u0012\u0004\u0012\u00020\r0\u0014H\u0003\u001a\b\u0010:\u001a\u00020\rH\u0003\u001a\u001c\u0010;\u001a\u000e\u0012\u0004\u0012\u000208\u0012\u0004\u0012\u0002080<2\u0006\u0010=\u001a\u00020\u0011H\u0002\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0007\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\t\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\n\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u000b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u00a8\u0006>"}, d2 = {"AmexBlue", "Landroidx/compose/ui/graphics/Color;", "J", "CardGradientEnd", "CardGradientStart", "ErrorRed", "FieldBg", "FieldBorder", "FieldBorderFocused", "MastercardOrange", "MastercardRed", "VisaBlue", "CardPaymentForm", "", "totalAmount", "", "orderId", "", "payerEmail", "onPaymentSuccess", "Lkotlin/Function1;", "Lcom/rendly/app/data/model/PaymentResponse;", "onPaymentFailed", "onCancel", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "CardPreview", "cardNumber", "cardholderName", "expirationDate", "cardType", "Lcom/rendly/app/data/model/CardType;", "CardTextField", "value", "Landroidx/compose/ui/text/input/TextFieldValue;", "onValueChange", "label", "placeholder", "leadingIcon", "Landroidx/compose/runtime/Composable;", "trailingIcon", "error", "visualTransformation", "Landroidx/compose/ui/text/input/VisualTransformation;", "keyboardOptions", "Landroidx/compose/foundation/text/KeyboardOptions;", "keyboardActions", "Landroidx/compose/foundation/text/KeyboardActions;", "CardTypeIcon", "CardTypeLogo", "InstallmentsSelector", "installments", "", "Lcom/rendly/app/data/model/PayerCost;", "selectedInstallments", "", "onSelect", "SecurityBadge", "parseExpirationDate", "Lkotlin/Pair;", "date", "app_debug"})
public final class CardPaymentFormKt {
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * CARD PAYMENT FORM - Formulario nativo de pago con tarjeta
     * ═══════════════════════════════════════════════════════════════════════════════
     *
     * UI nativa en Compose para ingresar datos de tarjeta.
     * Incluye detección automática de tipo de tarjeta, formateo, validación,
     * y selector de cuotas.
     *
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    private static final long CardGradientStart = 0L;
    private static final long CardGradientEnd = 0L;
    private static final long VisaBlue = 0L;
    private static final long MastercardRed = 0L;
    private static final long MastercardOrange = 0L;
    private static final long AmexBlue = 0L;
    private static final long FieldBg = 0L;
    private static final long FieldBorder = 0L;
    private static final long FieldBorderFocused = 0L;
    private static final long ErrorRed = 0L;
    
    @androidx.compose.runtime.Composable
    public static final void CardPaymentForm(double totalAmount, @org.jetbrains.annotations.NotNull
    java.lang.String orderId, @org.jetbrains.annotations.NotNull
    java.lang.String payerEmail, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super com.rendly.app.data.model.PaymentResponse, kotlin.Unit> onPaymentSuccess, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onPaymentFailed, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onCancel, @org.jetbrains.annotations.NotNull
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CardPreview(java.lang.String cardNumber, java.lang.String cardholderName, java.lang.String expirationDate, com.rendly.app.data.model.CardType cardType) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CardTypeLogo(com.rendly.app.data.model.CardType cardType) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void CardTypeIcon(com.rendly.app.data.model.CardType cardType) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    private static final void CardTextField(androidx.compose.ui.text.input.TextFieldValue value, kotlin.jvm.functions.Function1<? super androidx.compose.ui.text.input.TextFieldValue, kotlin.Unit> onValueChange, java.lang.String label, java.lang.String placeholder, androidx.compose.ui.Modifier modifier, kotlin.jvm.functions.Function0<kotlin.Unit> leadingIcon, kotlin.jvm.functions.Function0<kotlin.Unit> trailingIcon, java.lang.String error, androidx.compose.ui.text.input.VisualTransformation visualTransformation, androidx.compose.foundation.text.KeyboardOptions keyboardOptions, androidx.compose.foundation.text.KeyboardActions keyboardActions) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void InstallmentsSelector(java.util.List<com.rendly.app.data.model.PayerCost> installments, int selectedInstallments, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onSelect) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void SecurityBadge() {
    }
    
    /**
     * Parsear fecha de expiración MM/YY
     */
    private static final kotlin.Pair<java.lang.Integer, java.lang.Integer> parseExpirationDate(java.lang.String date) {
        return null;
    }
}