package com.rendly.app.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Repository para gestionar feedback de usuarios y reportes de errores
 */
object FeedbackRepository {
    private const val TAG = "FeedbackRepository"
    
    @Serializable
    data class FeedbackRequest(
        val user_id: String,
        val category: String,
        val title: String,
        val description: String,
        val rating: Int? = null,
        val user_name: String? = null,
        val user_email: String? = null,
        val device_info: JsonObject? = null,
        val app_version: String? = null
    )
    
    @Serializable
    data class BugReportRequest(
        val user_id: String,
        val title: String,
        val description: String,
        val steps_to_reproduce: String? = null,
        val expected_behavior: String? = null,
        val actual_behavior: String? = null,
        val severity: String = "medium",
        val category: String? = null,
        val user_name: String? = null,
        val user_email: String? = null,
        val device_info: JsonObject? = null,
        val app_version: String = "1.0.0",
        val os_version: String? = null,
        val app_logs: String? = null,
        val include_device_info: Boolean = false,
        val include_logs: Boolean = false
    )
    
    sealed class SubmissionResult {
        data class Success(val id: String) : SubmissionResult()
        data class Error(val message: String) : SubmissionResult()
    }
    
    /**
     * Enviar feedback de usuario
     */
    suspend fun submitFeedback(
        userId: String,
        category: String,
        title: String,
        description: String,
        rating: Int? = null,
        userName: String? = null,
        userEmail: String? = null,
        context: Context? = null
    ): SubmissionResult {
        return try {
            val deviceInfo = context?.let { getDeviceInfo(it) }
            val appVersion = context?.let { getAppVersion(it) }
            
            val request = FeedbackRequest(
                user_id = userId,
                category = category,
                title = title,
                description = description,
                rating = rating,
                user_name = userName,
                user_email = userEmail,
                device_info = deviceInfo,
                app_version = appVersion
            )
            
            SupabaseClient.client.from("app_feedback")
                .insert(request)
            
            Log.d(TAG, "✓ Feedback enviado exitosamente")
            SubmissionResult.Success("success")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando feedback: ${e.message}", e)
            SubmissionResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Enviar reporte de error
     */
    suspend fun submitBugReport(
        userId: String,
        title: String,
        description: String,
        stepsToReproduce: String? = null,
        expectedBehavior: String? = null,
        actualBehavior: String? = null,
        severity: String = "medium",
        category: String? = null,
        userName: String? = null,
        userEmail: String? = null,
        includeDeviceInfo: Boolean = false,
        includeLogs: Boolean = false,
        context: Context? = null
    ): SubmissionResult {
        return try {
            val deviceInfo = if (includeDeviceInfo && context != null) {
                getDeviceInfo(context)
            } else null
            
            val appLogs = if (includeLogs && context != null) {
                getAppLogs()
            } else null
            
            val appVersion = context?.let { getAppVersion(it) }
            val osVersion = Build.VERSION.RELEASE
            
            val request = BugReportRequest(
                user_id = userId,
                title = title,
                description = description,
                steps_to_reproduce = stepsToReproduce,
                expected_behavior = expectedBehavior,
                actual_behavior = actualBehavior,
                severity = severity,
                category = category,
                user_name = userName,
                user_email = userEmail,
                device_info = deviceInfo,
                app_version = appVersion ?: "1.0.0",
                os_version = osVersion,
                app_logs = appLogs,
                include_device_info = includeDeviceInfo,
                include_logs = includeLogs
            )
            
            SupabaseClient.client.from("bug_reports")
                .insert(request)
            
            Log.d(TAG, "✓ Reporte de error enviado exitosamente")
            SubmissionResult.Success("success")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando reporte: ${e.message}", e)
            SubmissionResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Obtener información del dispositivo
     */
    private fun getDeviceInfo(context: Context): JsonObject {
        return buildJsonObject {
            put("manufacturer", Build.MANUFACTURER)
            put("model", Build.MODEL)
            put("device", Build.DEVICE)
            put("product", Build.PRODUCT)
            put("brand", Build.BRAND)
            put("android_version", Build.VERSION.RELEASE)
            put("sdk_int", Build.VERSION.SDK_INT)
            put("display", Build.DISPLAY)
            
            // Screen info
            val displayMetrics = context.resources.displayMetrics
            put("screen_width", displayMetrics.widthPixels)
            put("screen_height", displayMetrics.heightPixels)
            put("screen_density", displayMetrics.density)
            
            // Memory info
            val runtime = Runtime.getRuntime()
            put("total_memory_mb", runtime.totalMemory() / (1024 * 1024))
            put("free_memory_mb", runtime.freeMemory() / (1024 * 1024))
            put("max_memory_mb", runtime.maxMemory() / (1024 * 1024))
        }
    }
    
    /**
     * Obtener versión de la app
     */
    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    /**
     * Obtener logs de la aplicación (últimas 500 líneas)
     */
    private fun getAppLogs(): String {
        return try {
            val process = Runtime.getRuntime().exec("logcat -d -t 500")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val log = StringBuilder()
            
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                // Filtrar solo logs de nuestra app
                if (line?.contains("com.rendly.app") == true || 
                    line?.contains("Merqora") == true ||
                    line?.contains("E/") == true || 
                    line?.contains("W/") == true) {
                    log.append(line).append("\n")
                }
            }
            
            bufferedReader.close()
            log.toString().takeLast(10000) // Limitar a 10KB
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo logs: ${e.message}")
            "No se pudieron obtener los logs: ${e.message}"
        }
    }
}
