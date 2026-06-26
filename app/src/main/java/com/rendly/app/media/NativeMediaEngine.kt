package com.rendly.app.media

import android.graphics.Bitmap
import android.util.Log

/**
 * JNI bridge to the C++ media optimizer engine.
 * Provides hardware-accelerated image analysis and Lanczos3 resampling.
 */
object NativeMediaEngine {
    
    private const val TAG = "NativeMediaEngine"
    private var isLoaded = false
    
    init {
        try {
            System.loadLibrary("rendly_media_optimizer")
            isLoaded = true
            Log.i(TAG, "C++ media optimizer engine loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library: ${e.message}")
            isLoaded = false
        }
    }
    
    fun isAvailable(): Boolean = isLoaded
    
    /**
     * Analyze image complexity and return optimal JPEG/WebP quality.
     * Uses perceptual analysis (spatial frequency, edge density, color variance).
     * Falls back to baseQuality if native engine is unavailable.
     */
    fun getOptimalQuality(bitmap: Bitmap, baseQuality: Int, minQuality: Int, maxQuality: Int): Int {
        if (!isLoaded) return baseQuality
        return try {
            // Ensure ARGB_8888 for native code
            val argbBitmap = if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
                else bitmap.copy(Bitmap.Config.ARGB_8888, false)
            val quality = analyzeAndGetQuality(argbBitmap, baseQuality, minQuality, maxQuality)
            if (argbBitmap !== bitmap) argbBitmap.recycle()
            quality
        } catch (e: Exception) {
            Log.e(TAG, "getOptimalQuality failed: ${e.message}")
            baseQuality
        }
    }
    
    /**
     * High-quality Lanczos3 resize. Returns new bitmap (caller must recycle).
     * Falls back to Android's createScaledBitmap if native unavailable.
     */
    fun resize(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (targetWidth == bitmap.width && targetHeight == bitmap.height) return bitmap
        if (!isLoaded) {
            Log.w(TAG, "Native unavailable, falling back to bilinear resize")
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        }
        return try {
            val argbBitmap = if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
                else bitmap.copy(Bitmap.Config.ARGB_8888, false)
            val result = lanczosResize(argbBitmap, targetWidth, targetHeight)
            if (argbBitmap !== bitmap) argbBitmap.recycle()
            result ?: Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        } catch (e: Exception) {
            Log.e(TAG, "lanczosResize failed: ${e.message}")
            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        }
    }
    
    /**
     * Get complexity score (0.0 = flat/simple, 1.0 = highly detailed).
     */
    fun complexity(bitmap: Bitmap): Float {
        if (!isLoaded) return 0.5f
        return try {
            val argbBitmap = if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
                else bitmap.copy(Bitmap.Config.ARGB_8888, false)
            val score = getComplexityScore(argbBitmap)
            if (argbBitmap !== bitmap) argbBitmap.recycle()
            score
        } catch (e: Exception) {
            Log.e(TAG, "complexity failed: ${e.message}")
            0.5f
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Native methods (implemented in media_optimizer_jni.cpp)
    // ═══════════════════════════════════════════════════════════════
    
    private external fun analyzeAndGetQuality(
        bitmap: Bitmap,
        baseQuality: Int,
        minQuality: Int,
        maxQuality: Int
    ): Int
    
    private external fun lanczosResize(
        srcBitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap?
    
    private external fun getComplexityScore(bitmap: Bitmap): Float
    
    private external fun estimateCompressedSize(bitmap: Bitmap, quality: Int): Long
}
