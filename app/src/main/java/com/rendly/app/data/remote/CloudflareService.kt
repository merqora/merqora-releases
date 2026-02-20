package com.rendly.app.data.remote

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

object CloudflareService {
    private const val TAG = "CloudflareService"

    private const val ACCOUNT_ID = "016e77c65134e8e7acc93c412d73ebaf"
    private const val API_TOKEN = "24NUf6QZGMydABaYso5uPiOrm7jpvIjLXwYPnk1z"
    private const val UPLOAD_URL = "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/images/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(
        bitmap: Bitmap,
        folder: String = "avatars",
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()

            onProgress(0.3f)

            val fileName = "${folder.replace('/', '_')}_${UUID.randomUUID()}.jpg"
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .addFormDataPart("metadata", "{\"folder\":\"$folder\"}")
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .header("Authorization", "Bearer $API_TOKEN")
                .post(requestBody)
                .build()

            onProgress(0.55f)

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            onProgress(0.85f)

            if (!response.isSuccessful) {
                Log.e(TAG, "Cloudflare upload failed: ${response.code} - $responseBody")
                return@withContext Result.failure(Exception("Error Cloudflare: ${response.code}"))
            }

            val json = JSONObject(responseBody)
            val success = json.optBoolean("success", false)
            if (!success) {
                val errors = json.optJSONArray("errors")?.toString() ?: "[]"
                Log.e(TAG, "Cloudflare API returned success=false: $errors")
                return@withContext Result.failure(Exception("Cloudflare rechazó la subida"))
            }

            val result = json.optJSONObject("result")
            val variants = result?.optJSONArray("variants")
            val publicUrl = if (variants != null && variants.length() > 0) {
                variants.optString(0, "")
            } else {
                ""
            }

            if (publicUrl.isBlank()) {
                Log.e(TAG, "Cloudflare response without variants URL: $responseBody")
                return@withContext Result.failure(Exception("Cloudflare no devolvió URL pública"))
            }

            onProgress(1f)
            Log.d(TAG, "Cloudflare upload successful: $publicUrl")
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }
}
