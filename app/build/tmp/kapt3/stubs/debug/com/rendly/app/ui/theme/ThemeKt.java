package com.rendly.app.ui.theme;

import android.app.Activity;
import androidx.compose.runtime.Composable;
import androidx.core.view.WindowCompat;
import com.rendly.app.data.preferences.AppPreferences;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000:\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0017\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\u001a/\u0010!\u001a\u00020\"2\b\b\u0002\u0010#\u001a\u00020$2\b\b\u0002\u0010%\u001a\u00020$2\u0011\u0010&\u001a\r\u0012\u0004\u0012\u00020\"0\'\u00a2\u0006\u0002\b(H\u0007\u001a\r\u0010%\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u0010)\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u0010*\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u0010+\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u0010,\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u0010-\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u0010.\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u0010/\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u00100\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u00101\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\u001a\r\u00102\u001a\u00020\u0003H\u0007\u00a2\u0006\u0002\u0010\u0005\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0013\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u0004\u0010\u0005\"\u000e\u0010\u0007\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0013\u0010\b\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\t\u0010\u0005\"\u0013\u0010\n\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u000b\u0010\u0005\"\u0013\u0010\f\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\r\u0010\u0005\"\u0013\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u000f\u0010\u0005\"\u0013\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u0011\u0010\u0005\"\u0013\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u0013\u0010\u0005\"\u0013\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u0015\u0010\u0005\"\u0013\u0010\u0016\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u0017\u0010\u0005\"\u0013\u0010\u0018\u001a\u00020\u0003\u00a2\u0006\n\n\u0002\u0010\u0006\u001a\u0004\b\u0019\u0010\u0005\"\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00030\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001d\u00a8\u00063"}, d2 = {"DarkColorScheme", "Landroidx/compose/material3/ColorScheme;", "LightBorderSubtle", "Landroidx/compose/ui/graphics/Color;", "getLightBorderSubtle", "()J", "J", "LightColorScheme", "LightHomeBg", "getLightHomeBg", "LightIconColor", "getLightIconColor", "LightInactiveColor", "getLightInactiveColor", "LightNavBarBg", "getLightNavBarBg", "LightSurface", "getLightSurface", "LightSurfaceElevated", "getLightSurfaceElevated", "LightTextMuted", "getLightTextMuted", "LightTextPrimary", "getLightTextPrimary", "LightTextSecondary", "getLightTextSecondary", "LocalAccentColor", "Landroidx/compose/runtime/ProvidableCompositionLocal;", "getLocalAccentColor", "()Landroidx/compose/runtime/ProvidableCompositionLocal;", "LocalIsDarkTheme", "", "getLocalIsDarkTheme", "MerqoraTheme", "", "themeMode", "", "accentColor", "content", "Lkotlin/Function0;", "Landroidx/compose/runtime/Composable;", "themedBorderSubtle", "themedHomeBg", "themedIconColor", "themedInactiveColor", "themedNavBarBg", "themedSurface", "themedSurfaceElevated", "themedTextMuted", "themedTextPrimary", "themedTextSecondary", "app_debug"})
public final class ThemeKt {
    @org.jetbrains.annotations.NotNull
    private static final androidx.compose.material3.ColorScheme DarkColorScheme = null;
    @org.jetbrains.annotations.NotNull
    private static final androidx.compose.material3.ColorScheme LightColorScheme = null;
    private static final long LightHomeBg = 0L;
    private static final long LightNavBarBg = 0L;
    private static final long LightSurface = 0L;
    private static final long LightSurfaceElevated = 0L;
    private static final long LightTextPrimary = 0L;
    private static final long LightTextSecondary = 0L;
    private static final long LightTextMuted = 0L;
    private static final long LightBorderSubtle = 0L;
    private static final long LightIconColor = 0L;
    private static final long LightInactiveColor = 0L;
    @org.jetbrains.annotations.NotNull
    private static final androidx.compose.runtime.ProvidableCompositionLocal<java.lang.Boolean> LocalIsDarkTheme = null;
    @org.jetbrains.annotations.NotNull
    private static final androidx.compose.runtime.ProvidableCompositionLocal<androidx.compose.ui.graphics.Color> LocalAccentColor = null;
    
    public static final long getLightHomeBg() {
        return 0L;
    }
    
    public static final long getLightNavBarBg() {
        return 0L;
    }
    
    public static final long getLightSurface() {
        return 0L;
    }
    
    public static final long getLightSurfaceElevated() {
        return 0L;
    }
    
    public static final long getLightTextPrimary() {
        return 0L;
    }
    
    public static final long getLightTextSecondary() {
        return 0L;
    }
    
    public static final long getLightTextMuted() {
        return 0L;
    }
    
    public static final long getLightBorderSubtle() {
        return 0L;
    }
    
    public static final long getLightIconColor() {
        return 0L;
    }
    
    public static final long getLightInactiveColor() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final androidx.compose.runtime.ProvidableCompositionLocal<java.lang.Boolean> getLocalIsDarkTheme() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final androidx.compose.runtime.ProvidableCompositionLocal<androidx.compose.ui.graphics.Color> getLocalAccentColor() {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    public static final void MerqoraTheme(@org.jetbrains.annotations.NotNull
    java.lang.String themeMode, @org.jetbrains.annotations.NotNull
    java.lang.String accentColor, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> content) {
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedHomeBg() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedNavBarBg() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedSurface() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedSurfaceElevated() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedTextPrimary() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedTextSecondary() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedTextMuted() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedIconColor() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedInactiveColor() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long themedBorderSubtle() {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable
    public static final long accentColor() {
        return 0L;
    }
}