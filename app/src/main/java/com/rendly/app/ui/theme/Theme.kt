package com.rendly.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rendly.app.data.preferences.AppPreferences

// ═══════════════════════════════════════════════════════════════
// COLORES TEMA OSCURO (Original)
// ═══════════════════════════════════════════════════════════════
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = AccentGreen,
    tertiary = AccentGold,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

// ═══════════════════════════════════════════════════════════════
// COLORES TEMA CLARO - Versión clara con la misma identidad
// ═══════════════════════════════════════════════════════════════
private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = AccentGreen,
    tertiary = AccentGold,
    background = Color(0xFFF5F7FA),        // Fondo suave con tinte azul
    surface = Color(0xFFFFFFFF),            // Superficies blancas
    surfaceVariant = Color(0xFFEDF1F5),     // Superficie variante
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),
    onBackground = Color(0xFF1A1A2E),       // Texto oscuro
    onSurface = Color(0xFF1A1A2E),
    outline = Color(0xFFDCE3EB),            // Bordes suaves
    outlineVariant = Color(0xFFCDD5DE),
)

// ═══════════════════════════════════════════════════════════════
// COLORES DINÁMICOS TEMA CLARO
// ═══════════════════════════════════════════════════════════════
val LightHomeBg = Color(0xFFF5F7FA)           // Fondo principal claro
val LightNavBarBg = Color(0xFFE8ECF1)         // NavBar diferenciado
val LightSurface = Color(0xFFFFFFFF)          // Superficies blancas
val LightSurfaceElevated = Color(0xFFEDF1F5)  // Superficie elevada
val LightTextPrimary = Color(0xFF1A1A2E)      // Texto principal oscuro
val LightTextSecondary = Color(0xFF5C5C6E)    // Texto secundario
val LightTextMuted = Color(0xFF8E8E9A)        // Texto tenue
val LightBorderSubtle = Color(0x1A0A3D62)     // Bordes sutiles azulados
val LightIconColor = Color(0xFF1A1A2E)        // Iconos oscuros
val LightInactiveColor = Color(0xB35C5C6E)    // Iconos inactivos (70% opacity)

// ═══════════════════════════════════════════════════════════════
// COMPOSITION LOCALS
// ═══════════════════════════════════════════════════════════════
val LocalIsDarkTheme = compositionLocalOf { true }
val LocalAccentColor = compositionLocalOf { PrimaryPurple }

@Composable
fun MerqoraTheme(
    themeMode: String = AppPreferences.THEME_DARK,
    accentColor: String = AppPreferences.ACCENT_PURPLE,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        AppPreferences.THEME_LIGHT -> false
        AppPreferences.THEME_DARK -> true
        AppPreferences.THEME_SYSTEM -> systemDark
        else -> true
    }
    
    // Color primario dinámico basado en el acento seleccionado
    val primaryColor = when (accentColor) {
        AppPreferences.ACCENT_PURPLE -> Color(0xFF0A3D62)  // Azul marino
        AppPreferences.ACCENT_PINK -> Color(0xFFFF6B35)    // Naranja acento
        AppPreferences.ACCENT_BLUE -> Color(0xFF0A3D62)    // Azul marino
        AppPreferences.ACCENT_GREEN -> Color(0xFF2E8B57)   // Verde mar
        AppPreferences.ACCENT_ORANGE -> Color(0xFFFF6B35)  // Naranja acento
        else -> Color(0xFF0A3D62)
    }
    
    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(primary = primaryColor)
    } else {
        LightColorScheme.copy(primary = primaryColor)
    }
    
    // Actualizar barra de estado
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) HomeBg.toArgb() else LightHomeBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalAccentColor provides primaryColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// FUNCIONES HELPER PARA COLORES DINÁMICOS
// ═══════════════════════════════════════════════════════════════

// Fondos
@Composable
fun themedHomeBg(): Color = if (LocalIsDarkTheme.current) HomeBg else LightHomeBg

@Composable
fun themedNavBarBg(): Color = if (LocalIsDarkTheme.current) TabBarBg else LightNavBarBg

@Composable
fun themedSurface(): Color = if (LocalIsDarkTheme.current) Surface else LightSurface

@Composable
fun themedSurfaceElevated(): Color = if (LocalIsDarkTheme.current) SurfaceElevated else LightSurfaceElevated

// Textos
@Composable
fun themedTextPrimary(): Color = if (LocalIsDarkTheme.current) TextPrimary else LightTextPrimary

@Composable
fun themedTextSecondary(): Color = if (LocalIsDarkTheme.current) TextSecondary else LightTextSecondary

@Composable
fun themedTextMuted(): Color = if (LocalIsDarkTheme.current) TextMuted else LightTextMuted

// Iconos
@Composable
fun themedIconColor(): Color = if (LocalIsDarkTheme.current) IconColor else LightIconColor

@Composable
fun themedInactiveColor(): Color = if (LocalIsDarkTheme.current) InactiveColor else LightInactiveColor

// Bordes
@Composable
fun themedBorderSubtle(): Color = if (LocalIsDarkTheme.current) BorderSubtle else LightBorderSubtle

// Color de acento (dinámico según preferencias del usuario)
@Composable
fun accentColor(): Color = LocalAccentColor.current
