package com.rendly.app.data.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Cache ligero para contadores de badges (carrito, notificaciones, mensajes).
 * Usa SharedPreferences para lectura instantánea al abrir la app.
 * Se actualiza en background cada vez que cambian los datos reales.
 */
object BadgeCountCache {
    
    private const val TAG = "BadgeCountCache"
    private const val PREFS_NAME = "Merqora_badge_counts"
    
    private const val KEY_CART_COUNT = "cart_count"
    private const val KEY_NOTIFICATION_COUNT = "notification_unread_count"
    private const val KEY_MESSAGE_COUNT = "message_unread_count"
    
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d(TAG, "✅ BadgeCountCache initialized")
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CART
    // ═══════════════════════════════════════════════════════════════
    
    fun getCartCount(): Int = prefs?.getInt(KEY_CART_COUNT, 0) ?: 0
    
    fun setCartCount(count: Int) {
        prefs?.edit()?.putInt(KEY_CART_COUNT, count)?.apply()
    }
    
    // ═══════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════
    
    fun getNotificationCount(): Int = prefs?.getInt(KEY_NOTIFICATION_COUNT, 0) ?: 0
    
    fun setNotificationCount(count: Int) {
        prefs?.edit()?.putInt(KEY_NOTIFICATION_COUNT, count)?.apply()
    }
    
    // ═══════════════════════════════════════════════════════════════
    // MESSAGES
    // ═══════════════════════════════════════════════════════════════
    
    fun getMessageCount(): Int = prefs?.getInt(KEY_MESSAGE_COUNT, 0) ?: 0
    
    fun setMessageCount(count: Int) {
        prefs?.edit()?.putInt(KEY_MESSAGE_COUNT, count)?.apply()
    }
    
    // ═══════════════════════════════════════════════════════════════
    // CLEAR (logout)
    // ═══════════════════════════════════════════════════════════════
    
    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
        Log.d(TAG, "Badge counts cleared")
    }
}
