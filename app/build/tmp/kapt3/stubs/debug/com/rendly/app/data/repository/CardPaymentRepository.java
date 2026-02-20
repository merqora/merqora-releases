package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.BuildConfig;
import com.rendly.app.data.model.*;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000|\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u0006\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002JV\u0010\"\u001a\b\u0012\u0004\u0012\u00020$0#2\u0006\u0010%\u001a\u00020\u00042\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\'2\u0006\u0010)\u001a\u00020\u00042\u0006\u0010*\u001a\u00020\u00042\b\b\u0002\u0010+\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b-\u0010.JJ\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0#2\u0006\u0010/\u001a\u0002002\u0006\u00101\u001a\u00020\u00042\n\b\u0002\u00102\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u00103\u001a\u0004\u0018\u00010\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b4\u00105J*\u00106\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002070\f0#2\u0006\u00102\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b8\u00109J\"\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\f0#H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b:\u0010;J\u0010\u0010<\u001a\u00020\u00042\u0006\u0010=\u001a\u00020\u0004H\u0002J&\u0010>\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0#2\u0006\u00101\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b?\u00109JZ\u0010@\u001a\u00020\u00042\u0006\u0010A\u001a\u00020\u00042\u0006\u0010B\u001a\u00020\u00042\u0006\u0010/\u001a\u0002002\u0006\u0010C\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\'2\u0006\u00102\u001a\u00020\u00042\b\u00103\u001a\u0004\u0018\u00010\u00042\u0006\u0010D\u001a\u00020\u00042\u0006\u0010+\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0002Jp\u0010E\u001a\b\u0012\u0004\u0012\u00020F0#2\u0006\u0010A\u001a\u00020\u00042\u0006\u0010B\u001a\u00020\u00042\u0006\u0010/\u001a\u0002002\u0006\u0010C\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\'2\u0006\u00102\u001a\u00020\u00042\b\u00103\u001a\u0004\u0018\u00010\u00042\u0006\u0010D\u001a\u00020\u00042\b\b\u0002\u0010+\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bG\u0010HJZ\u0010I\u001a\u00020\u00042\u0006\u0010A\u001a\u00020\u00042\u0006\u0010B\u001a\u00020\u00042\u0006\u0010/\u001a\u0002002\u0006\u0010C\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\'2\u0006\u00102\u001a\u00020\u00042\b\u00103\u001a\u0004\u0018\u00010\u00042\u0006\u0010D\u001a\u00020\u00042\u0006\u0010+\u001a\u00020\u00042\u0006\u0010,\u001a\u00020\u0004H\u0002J\u0006\u0010J\u001a\u00020KR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0014\u0010\u0016\u001a\u00020\t8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0017R\u0014\u0010\u0018\u001a\u00020\t8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0017R\u0014\u0010\u0019\u001a\u00020\t8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0019\u0010\u0017R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\f0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0015R\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00110\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0015\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006L"}, d2 = {"Lcom/rendly/app/data/repository/CardPaymentRepository;", "", "()V", "MP_ACCESS_TOKEN", "", "MP_API_BASE", "MP_PUBLIC_KEY", "TAG", "USE_EDGE_FUNCTION", "", "_installments", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/model/PayerCost;", "_paymentMethods", "Lcom/rendly/app/data/model/PaymentMethodInfo;", "_paymentState", "Lcom/rendly/app/data/repository/CardPaymentState;", "installments", "Lkotlinx/coroutines/flow/StateFlow;", "getInstallments", "()Lkotlinx/coroutines/flow/StateFlow;", "isSandboxMode", "()Z", "isUsingProductionCredentials", "isUsingTestCredentials", "json", "Lkotlinx/serialization/json/Json;", "okHttpClient", "Lokhttp3/OkHttpClient;", "paymentMethods", "getPaymentMethods", "paymentState", "getPaymentState", "createCardToken", "Lkotlin/Result;", "Lcom/rendly/app/data/model/CardToken;", "cardNumber", "expirationMonth", "", "expirationYear", "securityCode", "cardholderName", "identificationType", "identificationNumber", "createCardToken-eH_QyT8", "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "amount", "", "bin", "paymentMethodId", "issuerId", "getInstallments-yxL6bBk", "(DLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getIssuers", "Lcom/rendly/app/data/model/Issuer;", "getIssuers-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPaymentMethods-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRejectReason", "statusDetail", "guessPaymentMethod", "guessPaymentMethod-gIAlu-s", "processDirectly", "orderId", "token", "description", "payerEmail", "processPayment", "Lcom/rendly/app/data/model/PaymentResponse;", "processPayment-5p_uFSQ", "(Ljava/lang/String;Ljava/lang/String;DLjava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "processViaEdgeFunction", "resetState", "", "app_debug"})
public final class CardPaymentRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "CardPaymentRepository";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String MP_API_BASE = "https://api.mercadopago.com/v1";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String MP_PUBLIC_KEY = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String MP_ACCESS_TOKEN = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.serialization.json.Json json = null;
    @org.jetbrains.annotations.NotNull
    private static final okhttp3.OkHttpClient okHttpClient = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.repository.CardPaymentState> _paymentState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.CardPaymentState> paymentState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.PaymentMethodInfo>> _paymentMethods = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.PaymentMethodInfo>> paymentMethods = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.PayerCost>> _installments = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.PayerCost>> installments = null;
    
    /**
     * Procesar pago con token
     *
     * En modo TEST: llama directamente a la API de MP con el Access Token
     * En PRODUCCIÓN: debe usar la Edge Function para no exponer el Access Token
     *
     * TODO: Cambiar USE_EDGE_FUNCTION a true cuando se deploye la Edge Function
     */
    private static final boolean USE_EDGE_FUNCTION = false;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.CardPaymentRepository INSTANCE = null;
    
    private CardPaymentRepository() {
        super();
    }
    
    private final boolean isUsingTestCredentials() {
        return false;
    }
    
    private final boolean isUsingProductionCredentials() {
        return false;
    }
    
    private final boolean isSandboxMode() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.CardPaymentState> getPaymentState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.PaymentMethodInfo>> getPaymentMethods() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.PayerCost>> getInstallments() {
        return null;
    }
    
    /**
     * Procesar pago directamente contra la API de MP (para testing)
     */
    private final java.lang.String processDirectly(java.lang.String orderId, java.lang.String token, double amount, java.lang.String description, int installments, java.lang.String paymentMethodId, java.lang.String issuerId, java.lang.String payerEmail, java.lang.String identificationType, java.lang.String identificationNumber) {
        return null;
    }
    
    /**
     * Procesar pago via Edge Function de Supabase (para producción)
     */
    private final java.lang.String processViaEdgeFunction(java.lang.String orderId, java.lang.String token, double amount, java.lang.String description, int installments, java.lang.String paymentMethodId, java.lang.String issuerId, java.lang.String payerEmail, java.lang.String identificationType, java.lang.String identificationNumber) {
        return null;
    }
    
    /**
     * Obtener mensaje amigable para el motivo de rechazo
     */
    private final java.lang.String getRejectReason(java.lang.String statusDetail) {
        return null;
    }
    
    /**
     * Resetear estado
     */
    public final void resetState() {
    }
}