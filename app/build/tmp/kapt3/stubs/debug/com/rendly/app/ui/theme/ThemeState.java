package com.rendly.app.ui.theme;

import android.content.Context;
import androidx.compose.runtime.*;
import com.rendly.app.data.preferences.AppPreferences;

/**
 * Estado global del tema de la app
 * Maneja tema oscuro/claro, color de acento, tamaño de fuente y accesibilidad
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0002\b\n\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\bJ\u000e\u00100\u001a\u00020.2\u0006\u00101\u001a\u00020\bJ\u000e\u00102\u001a\u00020.2\u0006\u00103\u001a\u00020\u0018J\u000e\u00104\u001a\u00020.2\u0006\u00105\u001a\u00020\u0018J\u000e\u00106\u001a\u00020.2\u0006\u00107\u001a\u00020\bR+\u0010\t\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u0011\u0010\u0010\u001a\u00020\u00118F\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R+\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0017\u0010\u000f\u001a\u0004\b\u0015\u0010\u000b\"\u0004\b\u0016\u0010\rR+\u0010\u0019\u001a\u00020\u00182\u0006\u0010\u0007\u001a\u00020\u00188F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u001e\u0010\u000f\u001a\u0004\b\u001a\u0010\u001b\"\u0004\b\u001c\u0010\u001dR\u0011\u0010\u001f\u001a\u00020\u00188F\u00a2\u0006\u0006\u001a\u0004\b\u001f\u0010\u001bR\u0011\u0010 \u001a\u00020\u00188F\u00a2\u0006\u0006\u001a\u0004\b \u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010!\u001a\u00020\"8F\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0006\u001a\u0004\b#\u0010$R+\u0010%\u001a\u00020\u00182\u0006\u0010\u0007\u001a\u00020\u00188F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b(\u0010\u000f\u001a\u0004\b&\u0010\u001b\"\u0004\b\'\u0010\u001dR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010)\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b8F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b,\u0010\u000f\u001a\u0004\b*\u0010\u000b\"\u0004\b+\u0010\r\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u00068"}, d2 = {"Lcom/rendly/app/ui/theme/ThemeState;", "", "preferences", "Lcom/rendly/app/data/preferences/AppPreferences;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "(Lcom/rendly/app/data/preferences/AppPreferences;Lkotlinx/coroutines/CoroutineScope;)V", "<set-?>", "", "accentColor", "getAccentColor", "()Ljava/lang/String;", "setAccentColor", "(Ljava/lang/String;)V", "accentColor$delegate", "Landroidx/compose/runtime/MutableState;", "fontScale", "", "getFontScale", "()F", "fontSize", "getFontSize", "setFontSize", "fontSize$delegate", "", "highContrast", "getHighContrast", "()Z", "setHighContrast", "(Z)V", "highContrast$delegate", "isDarkTheme", "isLightTheme", "primaryColor", "Landroidx/compose/ui/graphics/Color;", "getPrimaryColor-0d7_KjU", "()J", "reduceMotion", "getReduceMotion", "setReduceMotion", "reduceMotion$delegate", "theme", "getTheme", "setTheme", "theme$delegate", "updateAccentColor", "", "color", "updateFontSize", "size", "updateHighContrast", "contrast", "updateReduceMotion", "reduce", "updateTheme", "newTheme", "app_debug"})
public final class ThemeState {
    @org.jetbrains.annotations.NotNull
    private final com.rendly.app.data.preferences.AppPreferences preferences = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState theme$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState accentColor$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState fontSize$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState reduceMotion$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.compose.runtime.MutableState highContrast$delegate = null;
    
    public ThemeState(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.preferences.AppPreferences preferences, @org.jetbrains.annotations.NotNull
    kotlinx.coroutines.CoroutineScope scope) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTheme() {
        return null;
    }
    
    private final void setTheme(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getAccentColor() {
        return null;
    }
    
    private final void setAccentColor(java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFontSize() {
        return null;
    }
    
    private final void setFontSize(java.lang.String p0) {
    }
    
    public final boolean getReduceMotion() {
        return false;
    }
    
    private final void setReduceMotion(boolean p0) {
    }
    
    public final boolean getHighContrast() {
        return false;
    }
    
    private final void setHighContrast(boolean p0) {
    }
    
    public final void updateTheme(@org.jetbrains.annotations.NotNull
    java.lang.String newTheme) {
    }
    
    public final void updateAccentColor(@org.jetbrains.annotations.NotNull
    java.lang.String color) {
    }
    
    public final void updateFontSize(@org.jetbrains.annotations.NotNull
    java.lang.String size) {
    }
    
    public final void updateReduceMotion(boolean reduce) {
    }
    
    public final void updateHighContrast(boolean contrast) {
    }
    
    public final boolean isDarkTheme() {
        return false;
    }
    
    public final boolean isLightTheme() {
        return false;
    }
    
    public final float getFontScale() {
        return 0.0F;
    }
}