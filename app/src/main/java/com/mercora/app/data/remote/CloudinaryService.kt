package com.mercora.app.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.mercora.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private const val UPLOAD_PRESET = "Vinzay_unsigned" // Upload preset unsigned
    private val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    suspend fun uploadImage(
        bitmap: Bitmap,
        folder: String = "stories",
        mediaType: com.vinzay.app.media.MediaOptimizer.MediaType? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)
            
            // Optimize with C++ engine if mediaType specified
            val imageBytes: ByteArray
            val contentType: String
            val fileExtension: String
            
            if (mediaType != null) {
                val result = com.vinzay.app.media.MediaOptimizer.optimize(bitmap, mediaType)
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
    // DELETE - Eliminar imagen de Cloudinary (requiere API key + secret)
    // ---------------------------------------------------------------
    private val API_KEY = BuildConfig.CLOUDINARY_API_KEY
    private val API_SECRET = BuildConfig.CLOUDINARY_API_SECRET
    
    /**
     * Elimina una imagen de Cloudinary usando su public_id
     * Requiere API_KEY y API_SECRET configurados
     */
    suspend fun deleteImage(publicId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (API_KEY.isBlank() || API_SECRET.isBlank()) {
                Log.w(TAG, "Cloudinary API_KEY o API_SECRET no configurados, no se puede eliminar")
                return@withContext Result.success(false)
            }
            
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            val toSign = "public_id=${publicId}&timestamp=${timestamp}${API_SECRET}"
            val signature = java.security.MessageDigest.getInstance("SHA-1")
                .digest(toSign.toByteArray())
                .joinToString("") { "%02x".format(it) }
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("public_id", publicId)
                .addFormDataPart("signature", signature)
                .addFormDataPart("api_key", API_KEY)
                .addFormDataPart("timestamp", timestamp)
                .build()
            
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/destroy")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val result = json.optString("result", "")
                Log.d(TAG, "Delete result for $publicId: $result")
                Result.success(result == "ok")
            } else {
                Log.e(TAG, "Delete failed for $publicId: $body")
                Result.success(false)
            }
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
