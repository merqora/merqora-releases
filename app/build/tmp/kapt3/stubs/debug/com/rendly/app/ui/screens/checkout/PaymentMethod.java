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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0011\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B9\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fR\u0019\u0010\b\u001a\u00020\t\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u0016j\u0002\b\u0017j\u0002\b\u0018j\u0002\b\u0019j\u0002\b\u001aj\u0002\b\u001b\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u0006\u001c"}, d2 = {"Lcom/rendly/app/ui/screens/checkout/PaymentMethod;", "", "id", "", "displayName", "description", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "color", "Landroidx/compose/ui/graphics/Color;", "isRecommended", "", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JZ)V", "getColor-0d7_KjU", "()J", "J", "getDescription", "()Ljava/lang/String;", "getDisplayName", "getIcon", "()Landroidx/compose/ui/graphics/vector/ImageVector;", "getId", "()Z", "MERCADO_PAGO", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "PREX", "app_debug"})
public enum PaymentMethod {
    /*public static final*/ MERCADO_PAGO /* = new MERCADO_PAGO(null, null, null, null, 0L, false) */,
    /*public static final*/ CREDIT_CARD /* = new CREDIT_CARD(null, null, null, null, 0L, false) */,
    /*public static final*/ DEBIT_CARD /* = new DEBIT_CARD(null, null, null, null, 0L, false) */,
    /*public static final*/ BANK_TRANSFER /* = new BANK_TRANSFER(null, null, null, null, 0L, false) */,
    /*public static final*/ PREX /* = new PREX(null, null, null, null, 0L, false) */;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String displayName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String description = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.ui.graphics.vector.ImageVector icon = null;
    private final long color = 0L;
    private final boolean isRecommended = false;
    
    PaymentMethod(java.lang.String id, java.lang.String displayName, java.lang.String description, androidx.compose.ui.graphics.vector.ImageVector icon, long color, boolean isRecommended) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.compose.ui.graphics.vector.ImageVector getIcon() {
        return null;
    }
    
    public final boolean isRecommended() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.ui.screens.checkout.PaymentMethod> getEntries() {
        return null;
    }
}