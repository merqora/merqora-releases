package com.vinzay.app.data.biometric

import android.content.Context
import com.vinzay.app.data.remote.SessionPersistence
import com.vinzay.app.data.repository.SecurityRepository
import com.vinzay.app.data.remote.SupabaseClient

object BiometricEnrollmentManager {

    private const val PREFS_NAME = "biometric_enrollment"
    private const val KEY_ENROLLED_USERS = "enrolled_user_ids"
    private const val KEY_LAST_EMAIL = "last_email"
    private const val KEY_LAST_PASSWORD = "last_password"
    private const val KEY_HAS_SAVED_CREDENTIALS = "has_saved_creds"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnrolled(context: Context, userId: String): Boolean {
        val set = prefs(context).getStringSet(KEY_ENROLLED_USERS, emptySet()) ?: emptySet()
        return set.contains(userId)
    }

    fun markEnrolled(context: Context, userId: String) {
        val set = prefs(context).getStringSet(KEY_ENROLLED_USERS, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        set.add(userId)
        prefs(context).edit().putStringSet(KEY_ENROLLED_USERS, set).apply()
    }

    fun clearEnrollment(context: Context, userId: String) {
        val set = prefs(context).getStringSet(KEY_ENROLLED_USERS, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        set.remove(userId)
        prefs(context).edit().putStringSet(KEY_ENROLLED_USERS, set).apply()
    }

    fun isEnrolledForCurrentSession(context: Context): Boolean {
        val userId = SessionPersistence.getUserId() ?: return false
        return isEnrolled(context, userId)
    }

    suspend fun syncToSupabase(userId: String) {
        try {
            val existing = SecurityRepository.getSecuritySettings(userId)
            if (existing == null) {
                SecurityRepository.createSecuritySettings(userId)
            }
            SecurityRepository.updateSecuritySettings(userId, biometricEnabled = true)
        } catch (_: Exception) { }
    }

    suspend fun disableOnSupabase(userId: String) {
        try {
            SecurityRepository.updateSecuritySettings(userId, biometricEnabled = false)
        } catch (_: Exception) { }
    }

    fun saveLoginCredentials(context: Context, emailOrUsername: String, password: String) {
        prefs(context).edit()
            .putString(KEY_LAST_EMAIL, emailOrUsername)
            .putString(KEY_LAST_PASSWORD, password)
            .putBoolean(KEY_HAS_SAVED_CREDENTIALS, true)
            .apply()
    }

    fun getSavedEmail(context: Context): String? =
        prefs(context).getString(KEY_LAST_EMAIL, null)

    fun getSavedPassword(context: Context): String? =
        prefs(context).getString(KEY_LAST_PASSWORD, null)

    fun hasSavedCredentials(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAS_SAVED_CREDENTIALS, false)

    fun clearCredentials(context: Context) {
        prefs(context).edit()
            .remove(KEY_LAST_EMAIL)
            .remove(KEY_LAST_PASSWORD)
            .putBoolean(KEY_HAS_SAVED_CREDENTIALS, false)
            .apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
