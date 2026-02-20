package com.rendly.app.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.rendly.app.BuildConfig
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

object ImageKitService {
    private const val TAG = "ImageKitService"
    
    // ImageKit configuration
    private const val UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload"
    private const val PUBLIC_KEY = "public_kk09G2vc9jjsVDVRz6D35/YavwE="
    private const val PRIVATE_KEY = "private_jB3PhowAiQtz/Uyq8OuQX7itPTs="
    val URL_ENDPOINT = BuildConfig.IMAGEKIT_URL_ENDPOINT
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()
    
    /**
     * Upload video to ImageKit
     */
    suspend fun uploadVideo(
        context: Context,
        videoUri: Uri,
        folder: String = "rends",
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f)
            Log.d(TAG, "Starting video upload to ImageKit...")
            
            // Read video bytes from URI
            val inputStream = context.contentResolver.openInputStream(videoUri)
                ?: return@withContext Result.failure(Exception("Cannot open video file"))
            
            val videoBytes = inputStream.use { it.readBytes() }
            Log.d(TAG, "Video size: ${videoBytes.size / 1024}KB")
            
            onProgress(0.15f)
            
            // Generate unique filename
            val fileName = "rend_${UUID.randomUUID()}.mp4"
            
            // Create multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    videoBytes.toRequestBody("video/mp4".toMediaType())
                )
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("folder", "/$folder")
                .addFormDataPart("publicKey", PUBLIC_KEY)
                .build()
            
            onProgress(0.25f)
            
            // Create auth header (Basic auth with private key)
            val credentials = "$PRIVATE_KEY:"
            val basicAuth = "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .header("Authorization", basicAuth)
                .post(requestBody)
                .build()
            
            onProgress(0.35f)
            Log.d(TAG, "Sending upload request...")
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            onProgress(0.85f)
            
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val videoUrl = json.getString("url")
                Log.d(TAG, "Upload successful: $videoUrl")
                onProgress(1f)
                Result.success(videoUrl)
            } else {
                Log.e(TAG, "Upload failed: ${response.code} - $responseBody")
                val errorMsg = when (response.code) {
                    403 -> "Error de autenticación ImageKit (403). Verifica las credenciales."
                    401 -> "No autorizado (401). API key inválida."
                    413 -> "Video demasiado grande. Máximo 25MB."
                    else -> "Error de subida: ${response.code}"
                }
                Result.failure(Exception(errorMsg))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload video from byte array
     */
    suspend fun uploadVideoBytes(
        videoBytes: ByteArray,
        folder: String = "rends",
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f)
            Log.d(TAG, "Starting video bytes upload to ImageKit...")
            Log.d(TAG, "Video size: ${videoBytes.size / 1024}KB")
            
            // Generate unique filename
            val fileName = "rend_${UUID.randomUUID()}.mp4"
            
            // Create multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    videoBytes.toRequestBody("video/mp4".toMediaType())
                )
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("folder", "/$folder")
                .addFormDataPart("publicKey", PUBLIC_KEY)
                .build()
            
            onProgress(0.25f)
            
            // Create auth header
            val credentials = "$PRIVATE_KEY:"
            val basicAuth = "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .header("Authorization", basicAuth)
                .post(requestBody)
                .build()
            
            onProgress(0.35f)
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            onProgress(0.85f)
            
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val videoUrl = json.getString("url")
                Log.d(TAG, "Upload successful: $videoUrl")
                onProgress(1f)
                Result.success(videoUrl)
            } else {
                Log.e(TAG, "Upload failed: ${response.code} - $responseBody")
                Result.failure(Exception("Upload failed: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload audio from byte array
     */
    suspend fun uploadAudioBytes(
        audioBytes: ByteArray,
        folder: String = "chat_audio",
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)
            Log.d(TAG, "Starting audio upload to ImageKit...")
            Log.d(TAG, "Audio size: ${audioBytes.size / 1024}KB")
            
            val fileName = "audio_${UUID.randomUUID()}.m4a"
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    audioBytes.toRequestBody("audio/mp4".toMediaType())
                )
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("folder", "/$folder")
                .addFormDataPart("publicKey", PUBLIC_KEY)
                .build()
            
            onProgress(0.3f)
            
            val credentials = "$PRIVATE_KEY:"
            val basicAuth = "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .header("Authorization", basicAuth)
                .post(requestBody)
                .build()
            
            onProgress(0.5f)
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            onProgress(0.9f)
            
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val audioUrl = json.getString("url")
                Log.d(TAG, "Audio upload successful: $audioUrl")
                onProgress(1f)
                Result.success(audioUrl)
            } else {
                Log.e(TAG, "Audio upload failed: ${response.code} - $responseBody")
                Result.failure(Exception("Upload failed: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Audio upload exception", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload image from byte array
     */
    suspend fun uploadImageBytes(
        imageBytes: ByteArray,
        folder: String = "chat_media",
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f)
            Log.d(TAG, "Starting image upload to ImageKit...")
            Log.d(TAG, "Image size: ${imageBytes.size / 1024}KB")
            
            val fileName = "img_${UUID.randomUUID()}.jpg"
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    imageBytes.toRequestBody("image/jpeg".toMediaType())
                )
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("folder", "/$folder")
                .addFormDataPart("publicKey", PUBLIC_KEY)
                .build()
            
            onProgress(0.3f)
            
            val credentials = "$PRIVATE_KEY:"
            val basicAuth = "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"
            
            val request = Request.Builder()
                .url(UPLOAD_URL)
                .header("Authorization", basicAuth)
                .post(requestBody)
                .build()
            
            onProgress(0.5f)
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            onProgress(0.9f)
            
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val imageUrl = json.getString("url")
                Log.d(TAG, "Image upload successful: $imageUrl")
                onProgress(1f)
                Result.success(imageUrl)
            } else {
                Log.e(TAG, "Image upload failed: ${response.code} - $responseBody")
                Result.failure(Exception("Upload failed: ${response.code}"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Image upload exception", e)
            Result.failure(e)
        }
    }
}
