package com.mercora.app.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.mercora.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Cloudflare R2: Highlights, stories, avatars, banners, portadas y contenido de "menor calidad"
 * - StoryRepository: stories
 * - HighlightRepository: highlights y covers
 * - ProfileRepository: avatares y banners
 * No usar para: posts, rends, chat media
 */
object CloudflareService {
    private const val TAG = "CloudflareR2"

    // â”€â”€ R2 Configuration (from BuildConfig) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private val ACCOUNT_ID = BuildConfig.CLOUDFLARE_ACCOUNT_ID
    private val BUCKET_NAME = BuildConfig.CLOUDFLARE_BUCKET_NAME
    private val ACCESS_KEY_ID = BuildConfig.CLOUDFLARE_ACCESS_KEY_ID
    private val SECRET_ACCESS_KEY = BuildConfig.CLOUDFLARE_SECRET_ACCESS_KEY
    val PUBLIC_DOMAIN = BuildConfig.CLOUDFLARE_PUBLIC_DOMAIN
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private val ENDPOINT = "$ACCOUNT_ID.r2.cloudflarestorage.com"
    private const val REGION = "auto"
    private const val SERVICE = "s3"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Upload a bitmap image to R2. Returns the public URL on success.
     * Same signature as before so all callers (ProfileRepository, StoryRepository,
     * HighlightRepository) work without changes.
     */
    suspend fun uploadImage(
        bitmap: Bitmap,
        folder: String = "avatars",
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
                Log.d(TAG, "Optimized: ${result.originalWidth}x${result.originalHeight} â†’ " +
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

            // S3 V4 signing
            val now = Date()
            val amzDate = isoDateFormat().format(now)        // 20260221T040000Z
            val dateStamp = shortDateFormat().format(now)    // 20260221
            val bodyHash = sha256Hex(imageBytes)

            val headers = sortedMapOf(
                "content-type" to contentType,
                "host" to ENDPOINT,
                "x-amz-content-sha256" to bodyHash,
                "x-amz-date" to amzDate
            )

            val signedHeaders = headers.keys.joinToString(";")
            val canonicalHeaders = headers.entries.joinToString("") { "${it.key}:${it.value}\n" }

            val canonicalRequest = listOf(
                "PUT",
                "/$BUCKET_NAME/$objectKey",
                "",  // no query string
                canonicalHeaders,
                signedHeaders,
                bodyHash
            ).joinToString("\n")

            val credentialScope = "$dateStamp/$REGION/$SERVICE/aws4_request"
            val stringToSign = listOf(
                "AWS4-HMAC-SHA256",
                amzDate,
                credentialScope,
                sha256Hex(canonicalRequest.toByteArray(Charsets.UTF_8))
            ).joinToString("\n")

            val signingKey = deriveSigningKey(SECRET_ACCESS_KEY, dateStamp, REGION, SERVICE)
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authorization = "AWS4-HMAC-SHA256 " +
                "Credential=$ACCESS_KEY_ID/$credentialScope, " +
                "SignedHeaders=$signedHeaders, " +
                "Signature=$signature"

            onProgress(0.5f)

            // Execute PUT request
            val url = "https://$ENDPOINT/$BUCKET_NAME/$objectKey"
            Log.d(TAG, "Uploading to R2: $url (${imageBytes.size} bytes)")

            val request = Request.Builder()
                .url(url)
                .put(imageBytes.toRequestBody(contentType.toMediaType()))
                .header("Host", ENDPOINT)
                .header("Content-Type", contentType)
                .header("x-amz-date", amzDate)
                .header("x-amz-content-sha256", bodyHash)
                .header("Authorization", authorization)
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

            // Public URL
            val publicUrl = "$PUBLIC_DOMAIN/$objectKey"

            onProgress(1f)
            Log.d(TAG, "R2 upload successful: $publicUrl")
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "R2 upload exception", e)
            Result.failure(e)
        }
    }

    /**
     * Delete an image from R2 given its public URL.
     * Extracts the object key from the URL and sends a signed DELETE request.
     * Silently ignores errors (best-effort cleanup).
     */
    suspend fun deleteImage(publicUrl: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Extract object key from public URL: https://pub-xxx.r2.dev/avatars/userId/uuid.jpg â†’ avatars/userId/uuid.jpg
            val objectKey = publicUrl.removePrefix(PUBLIC_DOMAIN).removePrefix("/")
            if (objectKey.isBlank() || objectKey == publicUrl) {
                Log.w(TAG, "Cannot extract object key from URL: $publicUrl")
                return@withContext
            }

            val now = Date()
            val amzDate = isoDateFormat().format(now)
            val dateStamp = shortDateFormat().format(now)
            val bodyHash = sha256Hex(ByteArray(0)) // empty body for DELETE

            val headers = sortedMapOf(
                "host" to ENDPOINT,
                "x-amz-content-sha256" to bodyHash,
                "x-amz-date" to amzDate
            )

            val signedHeaders = headers.keys.joinToString(";")
            val canonicalHeaders = headers.entries.joinToString("") { "${it.key}:${it.value}\n" }

            val canonicalRequest = listOf(
                "DELETE",
                "/$BUCKET_NAME/$objectKey",
                "",
                canonicalHeaders,
                signedHeaders,
                bodyHash
            ).joinToString("\n")

            val credentialScope = "$dateStamp/$REGION/$SERVICE/aws4_request"
            val stringToSign = listOf(
                "AWS4-HMAC-SHA256",
                amzDate,
                credentialScope,
                sha256Hex(canonicalRequest.toByteArray(Charsets.UTF_8))
            ).joinToString("\n")

            val signingKey = deriveSigningKey(SECRET_ACCESS_KEY, dateStamp, REGION, SERVICE)
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authorization = "AWS4-HMAC-SHA256 " +
                "Credential=$ACCESS_KEY_ID/$credentialScope, " +
                "SignedHeaders=$signedHeaders, " +
                "Signature=$signature"

            val url = "https://$ENDPOINT/$BUCKET_NAME/$objectKey"
            Log.d(TAG, "Deleting from R2: $url")

            val request = Request.Builder()
                .url(url)
                .delete()
                .header("Host", ENDPOINT)
                .header("x-amz-date", amzDate)
                .header("x-amz-content-sha256", bodyHash)
                .header("Authorization", authorization)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "R2 delete successful: $objectKey")
            } else {
                Log.w(TAG, "R2 delete failed (${response.code}): ${response.body?.string()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "R2 delete exception (non-fatal): ${e.message}")
        }
    }

    // â”€â”€ S3 V4 Signing Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        return hmacSha256(key, data).joinToString("") { "%02x".format(it) }
    }

    private fun deriveSigningKey(
        secretKey: String, dateStamp: String, region: String, service: String
    ): ByteArray {
        val kDate = hmacSha256("AWS4$secretKey".toByteArray(Charsets.UTF_8), dateStamp)
        val kRegion = hmacSha256(kDate, region)
        val kService = hmacSha256(kRegion, service)
        return hmacSha256(kService, "aws4_request")
    }

    private fun isoDateFormat() = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun shortDateFormat() = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}
