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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Cloudinary: Publicaciones (posts)
 * - PostRepository: imï¿½genes de productos/publicaciones
 * No usar para: avatares, banners, stories, highlights, rends
 */
object CloudinaryService {
    private const val TAG = "CloudinaryService"
    private val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME
    private const val UPLOAD_PRESET = "Mercora_unsigned" // Upload preset unsigned
    private val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    suspend fun uploadImage(
        bitmap: Bitmap,
        folder: String = "stories",
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
                Log.d(TAG, "Optimized: ${result.originalWidth}x${result.originalHeight} ? " +
                    "${result.finalWidth}x${result.finalHeight}, " +
                    "saved ${result.savedPercentage}%")
            } else {
                // Legacy: JPEG q85 (backward compatible)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                imageBytes = outputStream.toByteArray()
                contentType = "image/jpeg"
                fileExtension = "jpg"
            }
            
            onProgress(0.3f)
            
            // Crear request multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart(
                    "file",
                    "media_${System.currentTimeMillis()}.$fileExtension",
                    imageBytes.toRequestBody(contentType.toMediaType())
                )
                .build()
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()
            
            onProgress(0.5f)
            
            Log.d(TAG, "Uploading to Cloudinary...")
            val response = client.newCall(request).execute()
            
            onProgress(0.8f)
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "{}")
                val secureUrl = json.optString("secure_url", "")
                
                if (secureUrl.isNotEmpty()) {
                    Log.d(TAG, "Upload successful: $secureUrl")
                    onProgress(1f)
                    Result.success(secureUrl)
                } else {
                    Log.e(TAG, "No secure_url in response")
                    Result.failure(Exception("No se pudo obtener la URL de la imagen"))
                }
            } else {
                val error = response.body?.string() ?: "Error desconocido"
                Log.e(TAG, "Upload failed: $error")
                Result.failure(Exception("Error al subir: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }
    
    // Extraer public_id de una URL de Cloudinary para poder eliminarla
    fun extractPublicIdFromUrl(url: String): String? {
        return try {
            // URL format: https://res.cloudinary.com/CLOUD_NAME/image/upload/v123456/folder/filename.ext
            val regex = Regex("""/upload/(?:v\d+/)?(.+)\.\w+$""")
            regex.find(url)?.groupValues?.get(1)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting public_id from URL: $url", e)
            null
        }
    }
    
    // ---------------------------------------------------------------
    // DELETE - Eliminar imagen de Cloudinary (server-side, edge function)
    // ---------------------------------------------------------------
    
    /**
     * Elimina una imagen de Cloudinary usando su public_id
     * El destroy se ejecuta en la edge function "media-services"
     * (la API_SECRET no viaja dentro del APK).
     */
    suspend fun deleteImage(publicId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (publicId.isBlank()) {
                Log.w(TAG, "publicId vacío, no se puede eliminar")
                return@withContext Result.success(false)
            }
            
            val response = try {
                SupabaseClient.client.functions.invoke(
                    function = "media-services",
                    body = buildJsonObject {
                        put("action", "cloudinary-delete")
                        put("publicId", publicId)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "media-services(cloudinary-delete) error: ${e.message}", e)
                return@withContext Result.failure(e)
            }
            
            val result = Json.decodeFromString<JsonObject>(response.body<String>())
            val ok = result["ok"]?.jsonPrimitive?.content == "true"
            Log.d(TAG, "Delete result for $publicId: ok=$ok")
            Result.success(ok)
        } catch (e: Exception) {
            Log.e(TAG, "Delete exception for $publicId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Elimina mï¿½ltiples imï¿½genes de Cloudinary desde sus URLs
     */
    suspend fun deleteImagesFromUrls(urls: List<String>) {
        urls.forEach { url ->
            val publicId = extractPublicIdFromUrl(url)
            if (publicId != null) {
                deleteImage(publicId)
            }
        }
    }
}
