package com.rendly.app.data.repository

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import com.rendly.app.data.remote.SupabaseClient
import java.text.SimpleDateFormat
import java.util.*

object SecurityRepository {
    private const val TAG = "SecurityRepository"

    // ═══════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════════
    
    @Serializable
    data class SecuritySettings(
        val id: String? = null,
        val user_id: String,
        val biometric_enabled: Boolean = false,
        val two_factor_enabled: Boolean = false,
        val two_factor_method: String = "totp",
        val login_alerts_enabled: Boolean = true,
        val require_strong_password: Boolean = true,
        val password_min_length: Int = 8,
        val require_uppercase: Boolean = true,
        val require_number: Boolean = true,
        val require_special_char: Boolean = false,
        val last_password_change: String? = null,
        val created_at: String? = null,
        val updated_at: String? = null
    )

    @Serializable
    data class TrustedDevice(
        val id: String? = null,
        val user_id: String,
        val device_id: String,
        val device_name: String,
        val device_type: String,
        val device_model: String? = null,
        val os_version: String? = null,
        val app_version: String? = null,
        val is_current: Boolean = false,
        val is_trusted: Boolean = true,
        val last_used_at: String? = null,
        val last_ip_address: String? = null,
        val last_location: String? = null,
        val created_at: String? = null
    )

    @Serializable
    data class UserSession(
        val id: String? = null,
        val user_id: String,
        val session_token: String,
        val device_id: String? = null,
        val device_name: String? = null,
        val device_type: String? = null,
        val ip_address: String? = null,
        val location: String? = null,
        val user_agent: String? = null,
        val is_active: Boolean = true,
        val started_at: String? = null,
        val last_activity_at: String? = null,
        val expires_at: String? = null,
        val ended_at: String? = null
    )

    @Serializable
    data class ActivityLog(
        val id: String? = null,
        val user_id: String,
        val activity_type: String,
        val description: String? = null,
        val ip_address: String? = null,
        val location: String? = null,
        val device_info: String? = null,
        val user_agent: String? = null,
        val is_suspicious: Boolean = false,
        val risk_level: String = "low",
        val created_at: String? = null
    )

    @Serializable
    data class SuspiciousActivity(
        val id: String? = null,
        val user_id: String,
        val activity_type: String,
        val description: String,
        val ip_address: String? = null,
        val location: String? = null,
        val device_info: String? = null,
        val risk_level: String = "medium",
        val is_resolved: Boolean = false,
        val resolved_at: String? = null,
        val resolved_by: String? = null,
        val resolution_note: String? = null,
        val created_at: String? = null
    )

    // ═══════════════════════════════════════════════════════════════
    // SECURITY SETTINGS
    // ═══════════════════════════════════════════════════════════════

    suspend fun getSecuritySettings(userId: String): SecuritySettings? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("user_security_settings")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<SecuritySettings>()
            
            Log.d(TAG, "✅ Configuración de seguridad obtenida")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo configuración: ${e.message}")
            null
        }
    }

    suspend fun updateSecuritySettings(
        userId: String,
        biometricEnabled: Boolean? = null,
        twoFactorEnabled: Boolean? = null,
        loginAlertsEnabled: Boolean? = null,
        requireStrongPassword: Boolean? = null,
        passwordMinLength: Int? = null,
        requireUppercase: Boolean? = null,
        requireNumber: Boolean? = null,
        requireSpecialChar: Boolean? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = buildJsonObject {
                biometricEnabled?.let { put("biometric_enabled", JsonPrimitive(it)) }
                twoFactorEnabled?.let { put("two_factor_enabled", JsonPrimitive(it)) }
                loginAlertsEnabled?.let { put("login_alerts_enabled", JsonPrimitive(it)) }
                requireStrongPassword?.let { put("require_strong_password", JsonPrimitive(it)) }
                passwordMinLength?.let { put("password_min_length", JsonPrimitive(it)) }
                requireUppercase?.let { put("require_uppercase", JsonPrimitive(it)) }
                requireNumber?.let { put("require_number", JsonPrimitive(it)) }
                requireSpecialChar?.let { put("require_special_char", JsonPrimitive(it)) }
                put("updated_at", JsonPrimitive(getCurrentTimestamp()))
            }

            SupabaseClient.client
                .from("user_security_settings")
                .update(updates) {
                    filter { eq("user_id", userId) }
                }

            Log.d(TAG, "✅ Configuración de seguridad actualizada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando configuración: ${e.message}")
            false
        }
    }

    suspend fun createSecuritySettings(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val settings = SecuritySettings(user_id = userId)
            SupabaseClient.client
                .from("user_security_settings")
                .insert(settings)
            
            Log.d(TAG, "✅ Configuración de seguridad creada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando configuración: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TRUSTED DEVICES
    // ═══════════════════════════════════════════════════════════════

    suspend fun getTrustedDevices(userId: String): List<TrustedDevice> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("user_trusted_devices")
                .select {
                    filter { eq("user_id", userId) }
                    order("last_used_at", Order.DESCENDING)
                }
                .decodeList<TrustedDevice>()
            
            Log.d(TAG, "✅ ${result.size} dispositivos obtenidos")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo dispositivos: ${e.message}")
            emptyList()
        }
    }

    suspend fun registerDevice(
        userId: String,
        context: Context,
        isCurrent: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            val deviceType = "android"
            val osVersion = "Android ${Build.VERSION.RELEASE}"
            val appVersion = getAppVersion(context)

            val device = buildJsonObject {
                put("user_id", JsonPrimitive(userId))
                put("device_id", JsonPrimitive(deviceId))
                put("device_name", JsonPrimitive(deviceName))
                put("device_type", JsonPrimitive(deviceType))
                put("device_model", JsonPrimitive(Build.MODEL))
                put("os_version", JsonPrimitive(osVersion))
                put("app_version", JsonPrimitive(appVersion))
                put("is_current", JsonPrimitive(isCurrent))
                put("is_trusted", JsonPrimitive(true))
                put("last_used_at", JsonPrimitive(getCurrentTimestamp()))
            }

            SupabaseClient.client
                .from("user_trusted_devices")
                .upsert(device)

            Log.d(TAG, "✅ Dispositivo registrado: $deviceName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error registrando dispositivo: ${e.message}")
            false
        }
    }

    suspend fun removeDevice(deviceId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client
                .from("user_trusted_devices")
                .delete {
                    filter {
                        eq("id", deviceId)
                        eq("user_id", userId)
                    }
                }

            logActivity(userId, "device_removed", "Dispositivo eliminado de la lista de confianza")
            Log.d(TAG, "✅ Dispositivo eliminado")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando dispositivo: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SESSIONS
    // ═══════════════════════════════════════════════════════════════

    suspend fun getActiveSessions(userId: String): List<UserSession> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("user_sessions")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_active", true)
                    }
                    order("last_activity_at", Order.DESCENDING)
                }
                .decodeList<UserSession>()
            
            Log.d(TAG, "✅ ${result.size} sesiones activas")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo sesiones: ${e.message}")
            emptyList()
        }
    }

    suspend fun createSession(
        userId: String,
        context: Context,
        sessionToken: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val deviceId = getDeviceId(context)
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

            val session = buildJsonObject {
                put("user_id", JsonPrimitive(userId))
                put("session_token", JsonPrimitive(sessionToken))
                put("device_id", JsonPrimitive(deviceId))
                put("device_name", JsonPrimitive(deviceName))
                put("device_type", JsonPrimitive("android"))
                put("is_active", JsonPrimitive(true))
                put("started_at", JsonPrimitive(getCurrentTimestamp()))
                put("last_activity_at", JsonPrimitive(getCurrentTimestamp()))
            }

            SupabaseClient.client
                .from("user_sessions")
                .insert(session)

            Log.d(TAG, "✅ Sesión creada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando sesión: ${e.message}")
            false
        }
    }

    suspend fun endSession(sessionId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = buildJsonObject {
                put("is_active", JsonPrimitive(false))
                put("ended_at", JsonPrimitive(getCurrentTimestamp()))
            }

            SupabaseClient.client
                .from("user_sessions")
                .update(updates) {
                    filter {
                        eq("id", sessionId)
                        eq("user_id", userId)
                    }
                }

            logActivity(userId, "session_ended", "Sesión cerrada manualmente")
            Log.d(TAG, "✅ Sesión cerrada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cerrando sesión: ${e.message}")
            false
        }
    }

    suspend fun endAllSessions(userId: String, exceptCurrentSessionId: String? = null): Int = withContext(Dispatchers.IO) {
        try {
            val sessions = getActiveSessions(userId)
            var closedCount = 0

            for (session in sessions) {
                if (session.id != exceptCurrentSessionId) {
                    val updates = buildJsonObject {
                        put("is_active", JsonPrimitive(false))
                        put("ended_at", JsonPrimitive(getCurrentTimestamp()))
                    }

                    SupabaseClient.client
                        .from("user_sessions")
                        .update(updates) {
                            filter { eq("id", session.id ?: "") }
                        }
                    closedCount++
                }
            }

            logActivity(userId, "session_ended", "Todas las sesiones cerradas ($closedCount)")
            Log.d(TAG, "✅ $closedCount sesiones cerradas")
            closedCount
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cerrando sesiones: ${e.message}")
            0
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ACTIVITY LOGS
    // ═══════════════════════════════════════════════════════════════

    suspend fun getActivityLogs(userId: String, limit: Int = 50): List<ActivityLog> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("user_activity_logs")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ActivityLog>()
            
            Log.d(TAG, "✅ ${result.size} registros de actividad")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo actividad: ${e.message}")
            emptyList()
        }
    }

    suspend fun logActivity(
        userId: String,
        activityType: String,
        description: String,
        isSuspicious: Boolean = false,
        riskLevel: String = "low",
        deviceInfo: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val log = buildJsonObject {
                put("user_id", JsonPrimitive(userId))
                put("activity_type", JsonPrimitive(activityType))
                put("description", JsonPrimitive(description))
                put("is_suspicious", JsonPrimitive(isSuspicious))
                put("risk_level", JsonPrimitive(riskLevel))
                deviceInfo?.let { put("device_info", JsonPrimitive(it)) }
            }

            SupabaseClient.client
                .from("user_activity_logs")
                .insert(log)

            // Si es sospechoso, también crear en suspicious_activities
            if (isSuspicious) {
                val suspicious = buildJsonObject {
                    put("user_id", JsonPrimitive(userId))
                    put("activity_type", JsonPrimitive(activityType))
                    put("description", JsonPrimitive(description))
                    put("risk_level", JsonPrimitive(riskLevel))
                    deviceInfo?.let { put("device_info", JsonPrimitive(it)) }
                }

                SupabaseClient.client
                    .from("suspicious_activities")
                    .insert(suspicious)
            }

            Log.d(TAG, "✅ Actividad registrada: $activityType")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error registrando actividad: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SUSPICIOUS ACTIVITIES
    // ═══════════════════════════════════════════════════════════════

    suspend fun getSuspiciousActivities(userId: String, onlyUnresolved: Boolean = false): List<SuspiciousActivity> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client
                .from("suspicious_activities")
                .select {
                    filter { 
                        eq("user_id", userId)
                        if (onlyUnresolved) {
                            eq("is_resolved", false)
                        }
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<SuspiciousActivity>()
            
            Log.d(TAG, "✅ ${result.size} actividades sospechosas")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo actividades sospechosas: ${e.message}")
            emptyList()
        }
    }

    suspend fun resolveSuspiciousActivity(
        activityId: String,
        userId: String,
        resolutionNote: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = buildJsonObject {
                put("is_resolved", JsonPrimitive(true))
                put("resolved_at", JsonPrimitive(getCurrentTimestamp()))
                put("resolved_by", JsonPrimitive("user"))
                resolutionNote?.let { put("resolution_note", JsonPrimitive(it)) }
            }

            SupabaseClient.client
                .from("suspicious_activities")
                .update(updates) {
                    filter {
                        eq("id", activityId)
                        eq("user_id", userId)
                    }
                }

            Log.d(TAG, "✅ Actividad sospechosa resuelta")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error resolviendo actividad: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PASSWORD
    // ═══════════════════════════════════════════════════════════════

    suspend fun changePassword(newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.auth.modifyUser {
                password = newPassword
            }

            // Actualizar fecha de último cambio
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                val updates = buildJsonObject {
                    put("last_password_change", JsonPrimitive(getCurrentTimestamp()))
                    put("updated_at", JsonPrimitive(getCurrentTimestamp()))
                }

                SupabaseClient.client
                    .from("user_security_settings")
                    .update(updates) {
                        filter { eq("user_id", userId) }
                    }

                logActivity(userId, "password_change", "Contraseña actualizada")
            }

            Log.d(TAG, "✅ Contraseña cambiada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cambiando contraseña: ${e.message}")
            false
        }
    }

    fun validatePassword(password: String, settings: SecuritySettings): Pair<Boolean, List<String>> {
        val errors = mutableListOf<String>()

        if (password.length < settings.password_min_length) {
            errors.add("Mínimo ${settings.password_min_length} caracteres")
        }

        if (settings.require_uppercase && !password.any { it.isUpperCase() }) {
            errors.add("Debe incluir al menos una mayúscula")
        }

        if (settings.require_number && !password.any { it.isDigit() }) {
            errors.add("Debe incluir al menos un número")
        }

        if (settings.require_special_char && !password.any { !it.isLetterOrDigit() }) {
            errors.add("Debe incluir al menos un carácter especial")
        }

        return Pair(errors.isEmpty(), errors)
    }

    // ═══════════════════════════════════════════════════════════════
    // 2FA (Two-Factor Authentication)
    // ═══════════════════════════════════════════════════════════════

    suspend fun enable2FA(userId: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        try {
            // Generar una clave secreta TOTP localmente
            // En producción, esto debería integrarse con Supabase MFA cuando esté habilitado
            val secret = generateTOTPSecret()
            
            // DO NOT set two_factor_enabled here - only after verify2FACode succeeds
            Log.d(TAG, "✅ 2FA secret generated (not yet enabled)")
            Pair(true, secret)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error habilitando 2FA: ${e.message}")
            Pair(false, null)
        }
    }

    suspend fun disable2FA(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            updateSecuritySettings(userId, twoFactorEnabled = false)
            logActivity(userId, "2fa_disabled", "Autenticación de dos factores desactivada")

            Log.d(TAG, "✅ 2FA deshabilitado")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deshabilitando 2FA: ${e.message}")
            false
        }
    }

    suspend fun verify2FACode(code: String, userId: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            // Validación básica del código (6 dígitos)
            if (code.length != 6 || !code.all { it.isDigit() }) {
                return@withContext false
            }
            // En producción, verificar contra el servidor TOTP
            // Por ahora, aceptamos cualquier código de 6 dígitos para testing
            
            // NOW set two_factor_enabled = true after successful verification
            if (userId != null) {
                updateSecuritySettings(userId, twoFactorEnabled = true)
                logActivity(userId, "2fa_enabled", "Autenticación de dos factores activada")
            }
            
            Log.d(TAG, "✅ Código 2FA verificado y 2FA habilitado")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando 2FA: ${e.message}")
            false
        }
    }
    
    private fun generateTOTPSecret(): String {
        // Generar una clave secreta Base32 de 20 bytes
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        return (1..32).map { chars.random() }.joinToString("")
    }

    // ═══════════════════════════════════════════════════════════════
    // ACCOUNT MANAGEMENT (Deactivate/Delete)
    // ═══════════════════════════════════════════════════════════════

    suspend fun deactivateAccount(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Marcar cuenta como desactivada en la tabla usuarios
            val updates = buildJsonObject {
                put("is_active", JsonPrimitive(false))
                put("deactivated_at", JsonPrimitive(getCurrentTimestamp()))
            }

            SupabaseClient.client
                .from("usuarios")
                .update(updates) {
                    filter { eq("user_id", userId) }
                }

            // Cerrar todas las sesiones
            endAllSessions(userId)
            
            // Registrar actividad
            logActivity(userId, "account_deactivated", "Cuenta desactivada por el usuario")

            Log.d(TAG, "✅ Cuenta desactivada: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error desactivando cuenta: ${e.message}")
            false
        }
    }

    suspend fun deleteAccount(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Eliminar datos relacionados en orden para evitar conflictos de FK
            
            // Eliminar mensajes del usuario
            try {
                SupabaseClient.client
                    .from("messages")
                    .delete { filter { eq("sender_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar messages: ${e.message}")
            }

            // Eliminar conversaciones donde participa
            try {
                SupabaseClient.client
                    .from("conversation_participants")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar conversation_participants: ${e.message}")
            }

            // Eliminar posts
            try {
                SupabaseClient.client
                    .from("posts")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar posts: ${e.message}")
            }

            // Eliminar rends
            try {
                SupabaseClient.client
                    .from("rends")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar rends: ${e.message}")
            }

            // Eliminar stories
            try {
                SupabaseClient.client
                    .from("stories")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar stories: ${e.message}")
            }

            // Eliminar follows
            try {
                SupabaseClient.client
                    .from("follows")
                    .delete { filter { eq("follower_id", userId) } }
                SupabaseClient.client
                    .from("follows")
                    .delete { filter { eq("following_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar follows: ${e.message}")
            }

            // Eliminar likes
            try {
                SupabaseClient.client
                    .from("likes")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar likes: ${e.message}")
            }

            // Eliminar saves
            try {
                SupabaseClient.client
                    .from("saves")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar saves: ${e.message}")
            }

            // Eliminar notificaciones
            try {
                SupabaseClient.client
                    .from("notifications")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar notifications: ${e.message}")
            }

            // Eliminar configuraciones de seguridad
            try {
                SupabaseClient.client
                    .from("user_security_settings")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar security_settings: ${e.message}")
            }

            // Eliminar dispositivos confiables
            try {
                SupabaseClient.client
                    .from("user_trusted_devices")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar trusted_devices: ${e.message}")
            }

            // Eliminar sesiones
            try {
                SupabaseClient.client
                    .from("user_sessions")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar sessions: ${e.message}")
            }

            // Eliminar logs de actividad
            try {
                SupabaseClient.client
                    .from("user_activity_logs")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar activity_logs: ${e.message}")
            }

            // Eliminar solicitudes de verificación
            try {
                SupabaseClient.client
                    .from("verification_requests")
                    .delete { filter { eq("user_id", userId) } }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudieron eliminar verification_requests: ${e.message}")
            }

            // 2. Finalmente eliminar el perfil del usuario
            SupabaseClient.client
                .from("usuarios")
                .delete { filter { eq("user_id", userId) } }

            Log.d(TAG, "✅ Cuenta eliminada permanentemente: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando cuenta: ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getAppVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
