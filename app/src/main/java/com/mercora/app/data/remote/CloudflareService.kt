package com.mercora.app.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.mercora.app.BuildConfig
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Cloudflare R2: Highlights, stories, avatars, banners, portadas y contenido de "menor calidad"
 * - StoryRepository: stories
 * - HighlightRepository: highlights y covers
 * - ProfileRepository: avatares y banners
 * No usar para: posts, rends, chat media
 *
 * La firma SigV4 se hace server-side en la edge function "media-services"
 * (las credenciales R2 ya no viajan dentro del APK).
 */
object CloudflareService {
    private const val TAG = "CloudflareR2"

    val PUBLIC_DOMAIN = BuildConfig.CLOUDFLARE_PUBLIC_DOMAIN

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private suspend fun invokeMediaAction(
        action: String,
        extra: JsonObjectBuilder.() -> Unit = {}
    ): JsonObject? = try {
        val bodyJson = buildJsonObject {
            put("action", action)
            extra()
        }
        val response = SupabaseClient.client.functions.invoke(
            function = "media-services",
            body = bodyJson
        )
        val bodyText = response.body<String>()
        Json.decodeFromString<JsonObject>(bodyText)
    } catch (e: Exception) {
        Log.e(TAG, "media-services($action) error: ${e.message}", e)
        null
    }

    /**
     * Upload a bitmap image to R2. Returns the public URL on success.
     * Same signature as before so all callers (ProfileRepository, StoryRepository,
     * HighlightRepository) work without changes.
     */
    suspend fun uploadImage(
        bitmap: Bitmap,
        folder: String = "avatars",
        mediaType: com.mercora.app.media.MediaOptimizer.MediaType? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)

            // Optimize with C++ engine if mediaType specified
            val imageBytes: ByteArray
            val contentType: String
            val fileExtension: String
            
            if (mediaType != null) {
                val result = com.mercora.app.media.MediaOptimizer.optimize(bitmap, mediaType)
                imageBytes = result.bytes
                contentType = result.contentType
                fileExtension = result.extension
                Log.d(TAG, "Optimized: ${result.originalWidth}x${result.originalHeight} → " +
                    "${result.finalWidth}x${result.finalHeight}, saved ${result.savedPercentage}%")
            } else {
                // Legacy: JPEG q85 (backward compatible)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                imageBytes = baos.toByteArray()
                contentType = "image/jpeg"
                fileExtension = "jpg"
            }

            onProgress(0.3f)

            // Object key: folder/uuid.ext
            val objectKey = "$folder/${UUID.randomUUID()}.$fileExtension"
            val bodyHash = sha256Hex(imageBytes)

            // Firmar el PUT server-side (edge function) y subir directo a R2
            val signed = invokeMediaAction("r2-upload-sign") {
                put("objectKey", objectKey)
                put("contentType", contentType)
                put("bodyHash", bodyHash)
            }
            if (signed == null) {
                return@withContext Result.failure(Exception("No se pudo firmar el upload R2"))
            }

            val url = signed["url"]?.jsonPrimitive?.content ?: run {
                return@withContext Result.failure(Exception("Respuesta R2 sin url"))
            }
            val headersObj = signed["headers"]?.jsonObject ?: run {
                return@withContext Result.failure(Exception("Respuesta R2 sin headers"))
            }
            val publicUrl = signed["publicUrl"]?.jsonPrimitive?.content
                ?: "$PUBLIC_DOMAIN/$objectKey"

            onProgress(0.5f)

            Log.d(TAG, "Uploading to R2: $url (${imageBytes.size} bytes)")

            val request = Request.Builder()
                .url(url)
                .put(imageBytes.toRequestBody(contentType.toMediaType()))
                .header("Content-Type", contentType)
                .header("x-amz-date", headersObj["x-amz-date"]?.jsonPrimitive?.content ?: "")
                .header("x-amz-content-sha256", headersObj["x-amz-content-sha256"]?.jsonPrimitive?.content ?: "")
                .header("Authorization", headersObj["Authorization"]?.jsonPrimitive?.content ?: "")
                .build()

            val response = client.newCall(request).execute()

            onProgress(0.85f)

            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                Log.e(TAG, "R2 upload failed: ${response.code} - $errorBody")
                return@withContext Result.failure(
                    Exception("Error R2 (${response.code}): $errorBody")
                )
            }

            onProgress(1f)
            Log.d(TAG, "R2 upload successful: $publicUrl")
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "R2 upload exception", e)
            Result.failure(e)
        }
    }

    /**
     * Delete an image from R2 given its public URL (server-side, edge function).
     * Silently ignores errors (best-effort cleanup).
     */
    suspend fun deleteImage(publicUrl: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Extract object key from public URL: https://pub-xxx.r2.dev/avatars/userId/uuid.jpg → avatars/userId/uuid.jpg
            val objectKey = publicUrl.removePrefix(PUBLIC_DOMAIN).removePrefix("/")
            if (objectKey.isBlank() || objectKey == publicUrl) {
                Log.w(TAG, "Cannot extract object key from URL: $publicUrl")
                return@withContext
            }

            Log.d(TAG, "Deleting from R2: $objectKey")
            invokeMediaAction("r2-delete") {
                put("objectKey", objectKey)
            }
        } catch (e: Exception) {
            Log.w(TAG, "R2 delete exception (non-fatal): ${e.message}")
        }
    }

    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }
}
