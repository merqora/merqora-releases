package com.rendly.app.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.rendly.app.data.remote.SupabaseClient
import kotlinx.coroutines.tasks.await

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * FCM HELPER
 * ═══════════════════════════════════════════════════════════════════════════════
 * Helper para manejar Firebase Cloud Messaging
 */
object FCMHelper {
    
    private const val TAG = "FCMHelper"
    private const val PREFS_NAME = "Merqora_fcm"
    private const val KEY_PENDING_TOKEN = "pending_fcm_token"
    
    /**
     * Inicializa FCM y obtiene el token actual.
     * Llamar después del login del usuario.
     */
    suspend fun initialize(context: Context) {
        try {
            // Obtener token actual
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "🔔 FCM Token obtenido: ${token.take(20)}...")
            
            // Guardar en Supabase si el usuario está logueado
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                saveTokenToSupabase(userId, token, context)
            } else {
                // Guardar localmente para después
                savePendingToken(context, token)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando FCM: ${e.message}")
        }
    }
    
    /**
     * Fuerza la regeneración del token FCM.
     * Útil cuando el token anterior es inválido.
     */
    suspend fun forceTokenRefresh(context: Context) {
        try {
            Log.d(TAG, "🔄 Forzando regeneración de token FCM...")
            
            // Eliminar token actual
            FirebaseMessaging.getInstance().deleteToken().await()
            Log.d(TAG, "🗑️ Token anterior eliminado")
            
            // Obtener nuevo token
            val newToken = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "✅ Nuevo FCM Token: ${newToken.take(20)}...")
            
            // Guardar en Supabase
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                saveTokenToSupabase(userId, newToken, context)
                Log.d(TAG, "✅ Nuevo token guardado en Supabase")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error regenerando token: ${e.message}")
        }
    }
    
    /**
     * Llamar después del login para registrar cualquier token pendiente.
     */
    suspend fun onUserLogin(context: Context, userId: String) {
        try {
            // Verificar si hay token pendiente
            val pendingToken = getPendingToken(context)
            if (pendingToken != null) {
                saveTokenToSupabase(userId, pendingToken, context)
                clearPendingToken(context)
            } else {
                // Obtener token actual y guardarlo
                val token = FirebaseMessaging.getInstance().token.await()
                saveTokenToSupabase(userId, token, context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en onUserLogin: ${e.message}")
        }
    }
    
    /**
     * Llamar en logout para desactivar el token del dispositivo.
     */
    suspend fun onUserLogout(context: Context) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            
            // Marcar token como inactivo en Supabase
            SupabaseClient.database
                .from("fcm_tokens")
                .update(mapOf("is_active" to false)) {
                    filter {
                        eq("token", token)
                    }
                }
            
            Log.d(TAG, "✅ Token marcado como inactivo")
        } catch (e: Exception) {
            Log.e(TAG, "Error en logout: ${e.message}")
        }
    }
    
    /**
     * Suscribirse a un tema para notificaciones grupales.
     */
    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Suscrito a topic: $topic")
                } else {
                    Log.e(TAG, "Error suscribiendo a topic: $topic")
                }
            }
    }
    
    /**
     * Desuscribirse de un tema.
     */
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Desuscrito de topic: $topic")
                }
            }
    }
    
    /**
     * Verificar y solicitar permiso de notificaciones (Android 13+).
     * Llamar desde una Activity.
     */
    fun requestNotificationPermission(activity: Activity): Boolean {
        // Solo requerido en Android 13 (API 33) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            
            // Verificar si ya tiene el permiso
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permiso de notificaciones ya otorgado")
                return true
            }
            
            // Solicitar permiso
            Log.d(TAG, "📢 Solicitando permiso de notificaciones...")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
            return false
        }
        
        // En versiones anteriores a Android 13, no se requiere permiso explícito
        return true
    }
    
    /**
     * Verificar si tiene permiso de notificaciones.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════
    
    private suspend fun saveTokenToSupabase(userId: String, token: String, context: Context) {
        try {
            val deviceInfo = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            
            SupabaseClient.database
                .from("fcm_tokens")
                .upsert(mapOf(
                    "user_id" to userId,
                    "token" to token,
                    "device_info" to deviceInfo,
                    "platform" to "android",
                    "app_version" to getAppVersion(context),
                    "is_active" to true
                ))
            
            Log.d(TAG, "✅ Token FCM guardado en Supabase")
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando token: ${e.message}")
        }
    }
    
    private fun savePendingToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_TOKEN, token)
            .apply()
    }
    
    private fun getPendingToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PENDING_TOKEN, null)
    }
    
    private fun clearPendingToken(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PENDING_TOKEN)
            .apply()
    }
    
    private fun getAppVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
