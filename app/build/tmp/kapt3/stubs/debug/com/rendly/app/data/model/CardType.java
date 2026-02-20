package com.rendly.app.data.model;

import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

/**
 * Tipo de tarjeta detectado por BIN
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0010\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\'\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\bR\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\rj\u0002\b\u000fj\u0002\b\u0010j\u0002\b\u0011j\u0002\b\u0012j\u0002\b\u0013j\u0002\b\u0014j\u0002\b\u0015\u00a8\u0006\u0016"}, d2 = {"Lcom/rendly/app/data/model/CardType;", "", "displayName", "", "icon", "cvvLength", "", "cardNumberLength", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;II)V", "getCardNumberLength", "()I", "getCvvLength", "getDisplayName", "()Ljava/lang/String;", "getIcon", "VISA", "MASTERCARD", "AMEX", "OCA", "DINERS", "CABAL", "UNKNOWN", "app_debug"})
public enum CardType {
    /*public static final*/ VISA /* = new VISA(null, null, 0, 0) */,
    /*public static final*/ MASTERCARD /* = new MASTERCARD(null, null, 0, 0) */,
    /*public static final*/ AMEX /* = new AMEX(null, null, 0, 0) */,
    /*public static final*/ OCA /* = new OCA(null, null, 0, 0) */,
    /*public static final*/ DINERS /* = new DINERS(null, null, 0, 0) */,
    /*public static final*/ CABAL /* = new CABAL(null, null, 0, 0) */,
    /*public static final*/ UNKNOWN /* = new UNKNOWN(null, null, 0, 0) */;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String displayName = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String icon = null;
    private final int cvvLength = 0;
    private final int cardNumberLength = 0;
    
    CardType(java.lang.String displayName, java.lang.String icon, int cvvLength, int cardNumberLength) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getIcon() {
        return null;
    }
    
    public final int getCvvLength() {
        return 0;
    }
    
    public final int getCardNumberLength() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<com.rendly.app.data.model.CardType> getEntries() {
        return null;
    }
}