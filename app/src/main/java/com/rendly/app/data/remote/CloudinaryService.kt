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
import java.util.concurrent.TimeUnit

object CloudinaryService {
    private const val TAG = "CloudinaryService"
    private const val CLOUD_NAME = "dz0clge3s" // Tu cloud name de Cloudinary
    private const val UPLOAD_PRESET = "Merqora_unsigned" // Upload preset unsigned
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    suspend fun uploadImage(
        bitmap: Bitmap,
        folder: String = "stories",
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)
            
            // Convertir bitmap a bytes
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()
            
            onProgress(0.3f)
            
            // Crear request multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart(
                    "file",
                    "story_${System.currentTimeMillis()}.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
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
}
