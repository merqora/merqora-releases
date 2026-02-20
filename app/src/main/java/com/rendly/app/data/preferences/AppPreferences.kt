package com.rendly.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

/**
 * Gestor de preferencias de la app usando DataStore
 * Maneja tema, color de acento, tamaño de fuente y accesibilidad
 */
class AppPreferences(private val context: Context) {
    
    companion object {
        // Claves de preferencias
        private val THEME_KEY = stringPreferencesKey("theme")
        private val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        private val REDUCE_MOTION_KEY = booleanPreferencesKey("reduce_motion")
        private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast")
        private val COMPACT_MODE_KEY = booleanPreferencesKey("compact_mode")
        private val SHOW_ANIMATIONS_KEY = booleanPreferencesKey("show_animations")
        
        // Valores por defecto
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_SYSTEM = "system"
        
        const val ACCENT_PURPLE = "purple"
        const val ACCENT_PINK = "pink"
        const val ACCENT_BLUE = "blue"
        const val ACCENT_GREEN = "green"
        const val ACCENT_ORANGE = "orange"
        
        const val FONT_SMALL = "small"
        const val FONT_MEDIUM = "medium"
        const val FONT_LARGE = "large"
    }
    
    // ═══════════════════════════════════════════════════════════════
    // TEMA
    // ═══════════════════════════════════════════════════════════════
    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_DARK
    }
    
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // COLOR DE ACENTO
    // ═══════════════════════════════════════════════════════════════
    val accentColorFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ACCENT_COLOR_KEY] ?: ACCENT_PURPLE
    }
    
    suspend fun setAccentColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = color
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // TAMAÑO DE FUENTE
    // ═══════════════════════════════════════════════════════════════
    val fontSizeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE_KEY] ?: FONT_MEDIUM
    }
    
    suspend fun setFontSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = size
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ACCESIBILIDAD
    // ═══════════════════════════════════════════════════════════════
    val reduceMotionFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REDUCE_MOTION_KEY] ?: false
    }
    
    suspend fun setReduceMotion(reduce: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REDUCE_MOTION_KEY] = reduce
        }
    }
    
    val highContrastFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIGH_CONTRAST_KEY] ?: false
    }
    
    suspend fun setHighContrast(highContrast: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST_KEY] = highContrast
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // MODO COMPACTO
    // ═══════════════════════════════════════════════════════════════
    val compactModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[COMPACT_MODE_KEY] ?: false
    }
    
    suspend fun setCompactMode(compact: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[COMPACT_MODE_KEY] = compact
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // ANIMACIONES
    // ═══════════════════════════════════════════════════════════════
    val showAnimationsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_ANIMATIONS_KEY] ?: true
    }
    
    suspend fun setShowAnimations(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_ANIMATIONS_KEY] = show
        }
    }
}
