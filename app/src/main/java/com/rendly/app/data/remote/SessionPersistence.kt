package com.rendly.app.data.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper para persistir estado de sesión
 * Simple y rápido - no afecta cold start
 */
object SessionPersistence {
    
    private const val PREFS_NAME = "Merqora_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveSession(userId: String) {
        prefs?.edit()?.apply {
            putString(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun isLoggedIn(): Boolean {
        return prefs?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
    }
    
    fun getUserId(): String? {
        return prefs?.getString(KEY_USER_ID, null)
    }
    
    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }
}
