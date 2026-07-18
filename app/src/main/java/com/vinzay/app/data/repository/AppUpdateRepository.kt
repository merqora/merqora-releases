package com.vinzay.app.data.repository

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.vinzay.app.BuildConfig
import com.vinzay.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

object AppUpdateRepository {
    private const val TAG = "AppUpdateRepository"
    private const val BUCKET = "app-releases"

    @Serializable
    data class AppVersion(
        val id: String? = null,
        val version_name: String = "",
        val version_code: Int = 0,
        val file_url: String = "",
        val file_size: Long = 0,
        val min_sdk: Int? = null,
        val changelog: String? = null,
        val created_at: String? = null,
    )

    data class UpdateInfo(
        val latest: AppVersion,
        val currentVersion: String,
        val currentCode: Int,
        val hasUpdate: Boolean,
    )

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val currentVersion = BuildConfig.VERSION_NAME
            val currentCode = BuildConfig.VERSION_CODE

            val response = SupabaseClient.client.from("app_versions")
                .select {
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingle<AppVersion>()

            if (response == null) {
                Log.d(TAG, "No versions found in app_versions table")
                return@withContext null
            }

            val latestCode = response.version_code
            val hasUpdate = latestCode > currentCode

            Log.d(TAG, "Current: v$currentVersion ($currentCode) | Latest: v${response.version_name} ($latestCode) | Update: $hasUpdate")

            UpdateInfo(
                latest = response,
                currentVersion = currentVersion,
                currentCode = currentCode,
                hasUpdate = hasUpdate,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for update: ${e.message}", e)
            null
        }
    }

    fun downloadAndInstall(context: Context, version: AppVersion) {
        try {
            val destination = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Vinzay-${version.version_name}.apk"
            )

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(version.file_url))
                .setTitle("Vinzay ${version.version_name}")
                .setDescription("Descargando actualizacion...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destination))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                request.setRequiresCharging(false)
            }

            downloadManager.enqueue(request)

            Log.d(TAG, "Download started: ${version.file_url} -> $destination")
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update: ${e.message}", e)
            fallbackInstall(context, version)
        }
    }

    private fun fallbackInstall(context: Context, version: AppVersion) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(version.file_url), "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in fallback install: ${e.message}", e)
        }
    }
}
