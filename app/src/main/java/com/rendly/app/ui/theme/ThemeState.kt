package com.rendly.app.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.rendly.app.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Estado global del tema de la app
 * Maneja tema oscuro/claro, color de acento, tamaño de fuente y accesibilidad
 */
class ThemeState(private val preferences: AppPreferences, private val scope: CoroutineScope) {
    
    // Estados observables
    var theme by mutableStateOf(AppPreferences.THEME_DARK)
        private set
    
    var accentColor by mutableStateOf(AppPreferences.ACCENT_PURPLE)
        private set
    
    var fontSize by mutableStateOf(AppPreferences.FONT_MEDIUM)
        private set
    
    var reduceMotion by mutableStateOf(false)
        private set
    
    var highContrast by mutableStateOf(false)
        private set
    
    init {
        // Cargar preferencias iniciales
        scope.launch {
            preferences.themeFlow.collect { theme = it }
        }
        scope.launch {
            preferences.accentColorFlow.collect { accentColor = it }
        }
        scope.launch {
            preferences.fontSizeFlow.collect { fontSize = it }
        }
        scope.launch {
            preferences.reduceMotionFlow.collect { reduceMotion = it }
        }
        scope.launch {
            preferences.highContrastFlow.collect { highContrast = it }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ACTUALIZADORES (evitar conflicto con setters de propiedades)
    // ═══════════════════════════════════════════════════════════════
    fun updateTheme(newTheme: String) {
        theme = newTheme
        scope.launch { preferences.setTheme(newTheme) }
    }
    
    fun updateAccentColor(color: String) {
        accentColor = color
        scope.launch { preferences.setAccentColor(color) }
    }
    
    fun updateFontSize(size: String) {
        fontSize = size
        scope.launch { preferences.setFontSize(size) }
    }
    
    fun updateReduceMotion(reduce: Boolean) {
        reduceMotion = reduce
        scope.launch { preferences.setReduceMotion(reduce) }
    }
    
    fun updateHighContrast(contrast: Boolean) {
        highContrast = contrast
        scope.launch { preferences.setHighContrast(contrast) }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // COLORES DINÁMICOS
    // ═══════════════════════════════════════════════════════════════
    val primaryColor: Color
        get() = when (accentColor) {
            AppPreferences.ACCENT_PURPLE -> Color(0xFF0A3D62)
            AppPreferences.ACCENT_PINK -> Color(0xFFFF6B35)
            AppPreferences.ACCENT_BLUE -> Color(0xFF0A3D62)
            AppPreferences.ACCENT_GREEN -> Color(0xFF2E8B57)
            AppPreferences.ACCENT_ORANGE -> Color(0xFFFF6B35)
            else -> Color(0xFF0A3D62)
        }
    
    val isDarkTheme: Boolean
        get() = theme == AppPreferences.THEME_DARK
    
    val isLightTheme: Boolean
        get() = theme == AppPreferences.THEME_LIGHT
    
    // Factor de escala de fuente
    val fontScale: Float
        get() = when (fontSize) {
            AppPreferences.FONT_SMALL -> 0.85f
            AppPreferences.FONT_MEDIUM -> 1f
            AppPreferences.FONT_LARGE -> 1.15f
            else -> 1f
        }
}

// CompositionLocal para acceder al estado del tema
val LocalThemeState = staticCompositionLocalOf<ThemeState> { 
    error("ThemeState not provided") 
}

/**
 * Provider del estado del tema
 */
@Composable
fun ProvideThemeState(
    context: Context,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val preferences = remember { AppPreferences(context) }
    val themeState = remember { ThemeState(preferences, scope) }
    
    CompositionLocalProvider(LocalThemeState provides themeState) {
        content()
    }
}
