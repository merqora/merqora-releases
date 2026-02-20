package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\u001a\u000e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0018\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0001\u001a\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0001\u001a\u000e\u0010\n\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0003\u001a\u0016\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r\u00a8\u0006\u000f"}, d2 = {"detectCardType", "Lcom/rendly/app/data/model/CardType;", "cardNumber", "", "formatCardNumber", "number", "cardType", "isValidCVV", "", "cvv", "isValidCardNumber", "isValidExpirationDate", "month", "", "year", "app_debug"})
public final class CardPaymentKt {
    
    /**
     * Detectar tipo de tarjeta por los primeros dígitos (BIN)
     */
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.CardType detectCardType(@org.jetbrains.annotations.NotNull
    java.lang.String cardNumber) {
        return null;
    }
    
    /**
     * Formatear número de tarjeta con espacios
     */
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String formatCardNumber(@org.jetbrains.annotations.NotNull
    java.lang.String number, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.CardType cardType) {
        return null;
    }
    
    /**
     * Validar número de tarjeta con algoritmo de Luhn
     */
    public static final boolean isValidCardNumber(@org.jetbrains.annotations.NotNull
    java.lang.String number) {
        return false;
    }
    
    /**
     * Validar fecha de expiración
     */
    public static final boolean isValidExpirationDate(int month, int year) {
        return false;
    }
    
    /**
     * Validar CVV
     */
    public static final boolean isValidCVV(@org.jetbrains.annotations.NotNull
    java.lang.String cvv, @org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.CardType cardType) {
        return false;
    }
}