package com.mercora.app.data.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper para persistir estado de sesión
 * Simple y rápido - no afecta cold start
 */
object SessionPersistence {
    
    private const val PREFS_NAME = "vinzay_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USERNAME = "username"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(userId: String, username: String = "") {
        prefs?.edit()?.apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
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

    fun getUsername(): String? {
        return prefs?.getString(KEY_USERNAME, null)
    }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }
}
