package com.mercora.app.media

import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream

/**
 * Mercora Media Optimizer - Smart compression & resize system.
 * 
 * Uses C++ native engine (Lanczos3 resize + perceptual quality analysis)
 * with per-media-type optimization profiles:
 * 
 * - AVATAR:    256×256,  WebP, quality 78-90 (higher: old gets deleted)
 * - BANNER:    1080×400, WebP, quality 72-85 (higher: old gets deleted)
 * - POST:      1080px,   WebP, quality 65-80 (smart: can't delete old)
 * - STORY:     1080×1920,WebP, quality 60-75 (aggressive: ephemeral 24h)
 * - HIGHLIGHT: 400×400,  WebP, quality 65-78 (cover only)
 * - REND_THUMB:720×1280, WebP, quality 60-75 (video thumbnail)
 */
object MediaOptimizer {
    
    private const val TAG = "MediaOptimizer"
    
    /**
     * Optimization profile per media type.
     * Each profile defines target dimensions, quality range, and format.
     */
    enum class MediaType(
        val maxWidth: Int,
        val maxHeight: Int,
        val baseQuality: Int,
        val minQuality: Int,
        val maxQuality: Int,
        val useWebP: Boolean,
        val description: String
    ) {
        AVATAR(
            maxWidth = 256, maxHeight = 256,
            baseQuality = 84, minQuality = 78, maxQuality = 90,
            useWebP = true,
            description = "Avatar (replaceable, higher quality)"
        ),
        BANNER(
            maxWidth = 1080, maxHeight = 400,
            baseQuality = 78, minQuality = 72, maxQuality = 85,
            useWebP = true,
            description = "Banner (replaceable, higher quality)"
        ),
        POST(
            maxWidth = 1080, maxHeight = 1440,
            baseQuality = 72, minQuality = 65, maxQuality = 80,
            useWebP = true,
            description = "Post image (permanent, smart compression)"
        ),
        STORY(
            maxWidth = 1080, maxHeight = 1920,
            baseQuality = 68, minQuality = 60, maxQuality = 75,
            useWebP = true,
            description = "Story (ephemeral 24h, aggressive compression)"
        ),
        HIGHLIGHT(
            maxWidth = 400, maxHeight = 400,
            baseQuality = 72, minQuality = 65, maxQuality = 78,
            useWebP = true,
            description = "Highlight cover (small, moderate compression)"
        ),
        REND_THUMBNAIL(
            maxWidth = 720, maxHeight = 1280,
            baseQuality = 68, minQuality = 60, maxQuality = 75,
            useWebP = true,
            description = "Rend thumbnail (video preview)"
        )
    }
    
    /**
     * Result of optimization with stats for logging.
     */
    data class OptimizationResult(
        val bitmap: Bitmap,
        val bytes: ByteArray,
        val contentType: String,
        val extension: String,
        val originalWidth: Int,
        val originalHeight: Int,
        val finalWidth: Int,
        val finalHeight: Int,
        val quality: Int,
        val originalSizeEstimate: Long,
        val finalSize: Long,
        val compressionRatio: Double,
        val complexityScore: Float
    ) {
        val savedPercentage: Int
            get() = if (originalSizeEstimate > 0)
                ((1.0 - finalSize.toDouble() / originalSizeEstimate) * 100).toInt()
            else 0
    }
    
    /**
     * Optimize a bitmap for the given media type.
     * Returns optimized bytes ready for upload + metadata.
     */
    fun optimize(bitmap: Bitmap, mediaType: MediaType): OptimizationResult {
        val startTime = System.currentTimeMillis()
        val originalW = bitmap.width
        val originalH = bitmap.height
        
        Log.i(TAG, "â•â•â• Optimizing ${mediaType.description} â•â•â•")
        Log.i(TAG, "  Input: ${originalW}×${originalH} (${bitmap.config})")
        
        // Estimate original uncompressed size (JPEG q85)
        val originalSizeEstimate = estimateJpegSize(bitmap, 85)
        
        // Step 1: Compute target dimensions (maintain aspect ratio)
        val (targetW, targetH) = computeTargetSize(
            originalW, originalH,
            mediaType.maxWidth, mediaType.maxHeight
        )
        
        // Step 2: Resize using Lanczos3 (C++ engine) if needed
        val resized = if (targetW < originalW || targetH < originalH) {
            Log.i(TAG, "  Resize: ${originalW}×${originalH} â†’ ${targetW}×${targetH} (Lanczos3)")
            NativeMediaEngine.resize(bitmap, targetW, targetH)
        } else {
            Log.i(TAG, "  No resize needed (${originalW}×${originalH} within limits)")
            bitmap
        }
        
        // Step 3: Analyze complexity and determine optimal quality (C++ engine)
        val complexity = NativeMediaEngine.complexity(resized)
        val optimalQuality = NativeMediaEngine.getOptimalQuality(
            resized,
            mediaType.baseQuality,
            mediaType.minQuality,
            mediaType.maxQuality
        )
        
        Log.i(TAG, "  Complexity: %.2f â†’ Quality: %d (range %d-%d)".format(
            complexity, optimalQuality, mediaType.minQuality, mediaType.maxQuality
        ))
        
        // Step 4: Compress to WebP (lossy) or JPEG
        val baos = ByteArrayOutputStream()
        val (format, contentType, extension) = if (mediaType.useWebP) {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                Triple(Bitmap.CompressFormat.WEBP_LOSSY, "image/webp", "webp")
            } else {
                Triple(Bitmap.CompressFormat.JPEG, "image/jpeg", "jpg")
            }
        } else {
            Triple(Bitmap.CompressFormat.JPEG, "image/jpeg", "jpg")
        }
        
        resized.compress(format, optimalQuality, baos)
        val bytes = baos.toByteArray()
        
        // Recycle resized if it's a different bitmap
        if (resized !== bitmap) {
            resized.recycle()
        }
        
        val elapsed = System.currentTimeMillis() - startTime
        val ratio = if (originalSizeEstimate > 0) bytes.size.toDouble() / originalSizeEstimate else 1.0
        
        val result = OptimizationResult(
            bitmap = bitmap, // original bitmap reference (not recycled)
            bytes = bytes,
            contentType = contentType,
            extension = extension,
            originalWidth = originalW,
            originalHeight = originalH,
            finalWidth = targetW,
            finalHeight = targetH,
            quality = optimalQuality,
            originalSizeEstimate = originalSizeEstimate,
            finalSize = bytes.size.toLong(),
            compressionRatio = ratio,
            complexityScore = complexity
        )
        
        Log.i(TAG, "  Output: ${targetW}×${targetH}, $format q$optimalQuality")
        Log.i(TAG, "  Size: ${formatBytes(originalSizeEstimate)} â†’ ${formatBytes(bytes.size.toLong())} " +
                "(saved ${result.savedPercentage}%)")
        Log.i(TAG, "  Time: ${elapsed}ms")
        Log.i(TAG, "â•â•â• Done â•â•â•")
        
        return result
    }
    
    /**
     * Quick optimize: returns just the compressed bytes (for drop-in replacement).
     */
    fun optimizeToBytes(bitmap: Bitmap, mediaType: MediaType): ByteArray {
        return optimize(bitmap, mediaType).bytes
    }
    
    /**
     * Quick optimize: returns resized+compressed Bitmap (for preview/display).
     */
    fun optimizeBitmap(bitmap: Bitmap, mediaType: MediaType): Bitmap {
        val (targetW, targetH) = computeTargetSize(
            bitmap.width, bitmap.height,
            mediaType.maxWidth, mediaType.maxHeight
        )
        return if (targetW < bitmap.width || targetH < bitmap.height) {
            NativeMediaEngine.resize(bitmap, targetW, targetH)
        } else {
            bitmap
        }
    }
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // Private helpers
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    
    private fun computeTargetSize(
        srcW: Int, srcH: Int,
        maxW: Int, maxH: Int
    ): Pair<Int, Int> {
        if (srcW <= maxW && srcH <= maxH) return Pair(srcW, srcH)
        
        val ratioW = maxW.toDouble() / srcW
        val ratioH = maxH.toDouble() / srcH
        val ratio = minOf(ratioW, ratioH)
        
        val targetW = (srcW * ratio).toInt().coerceAtLeast(1)
        val targetH = (srcH * ratio).toInt().coerceAtLeast(1)
        
        return Pair(targetW, targetH)
    }
    
    private fun estimateJpegSize(bitmap: Bitmap, quality: Int): Long {
        // Quick estimate: compress a small sample
        val sampleW = minOf(bitmap.width, 256)
        val sampleH = minOf(bitmap.height, 256)
        val sample = if (sampleW < bitmap.width || sampleH < bitmap.height) {
            Bitmap.createScaledBitmap(bitmap, sampleW, sampleH, true)
        } else bitmap
        
        val baos = ByteArrayOutputStream()
        sample.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val sampleBytes = baos.size()
        
        if (sample !== bitmap) sample.recycle()
        
        // Scale up to full image size
        val pixelRatio = (bitmap.width.toLong() * bitmap.height) / (sampleW.toLong() * sampleH)
        return sampleBytes * pixelRatio
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            else -> "%.1fMB".format(bytes / (1024.0 * 1024.0))
        }
    }
}
