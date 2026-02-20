package com.rendly.app.native

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.rendly.app.ui.components.ImageAdjustState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Motor de ajuste de imagen ULTRA-OPTIMIZADO
 * - LUTs precalculadas para conversiones sRGB (evita pow() costoso)
 * - Procesamiento en resolución reducida para imágenes grandes
 * - Procesamiento paralelo por chunks
 */
object ImageAdjustEngine {
    
    // Número de threads para procesamiento paralelo
    private val NUM_THREADS = Runtime.getRuntime().availableProcessors().coerceIn(4, 8)
    
    // Tamaño máximo para procesamiento directo (más grande = procesar en resolución reducida)
    private const val MAX_DIRECT_PIXELS = 1_000_000 // ~1000x1000
    
    // ═══════════════════════════════════════════════════════════════
    // LUTs PRECALCULADAS - Evita cálculos pow() costosos
    // ═══════════════════════════════════════════════════════════════
    private val srgbToLinearLUT = FloatArray(256) { i ->
        val v = i / 255f
        if (v <= 0.04045f) v / 12.92f
        else ((v + 0.055f) / 1.055f).pow(2.4f)
    }
    
    private val linearToSrgbLUT = FloatArray(4096) { i ->
        val v = i / 4095f
        val result = if (v <= 0.0031308f) v * 12.92f
        else 1.055f * v.pow(1f / 2.4f) - 0.055f
        (result * 255f).coerceIn(0f, 255f)
    }
    
    // Conversiones usando LUTs (MUCHO más rápido)
    private fun srgbToLinearFast(byteVal: Int): Float = srgbToLinearLUT[byteVal]
    
    private fun linearToSrgbFast(linear: Float): Int {
        val idx = (linear.coerceIn(0f, 1f) * 4095f).toInt()
        return linearToSrgbLUT[idx].toInt()
    }
    
    // Luminancia perceptual Rec. 709
    private inline fun getLuminance(r: Float, g: Float, b: Float): Float {
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
    
    // Smoothstep inline para velocidad
    private inline fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
    
    /**
     * Aplica todos los ajustes profesionales en un solo paso
     * ULTRA-OPTIMIZADO: LUTs + paralelo + resolución reducida para imágenes grandes
     */
    fun applyAdjustmentsFull(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        exposure: Float,
        highlights: Float,
        shadows: Float,
        temperature: Float,
        tint: Float,
        grain: Float
    ) {
        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        
        // Para imágenes muy grandes, procesar en resolución reducida
        if (totalPixels > MAX_DIRECT_PIXELS) {
            applyAdjustmentsScaled(bitmap, brightness, contrast, saturation, 
                exposure, highlights, shadows, temperature, tint, grain)
            return
        }
        
        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // Precalcular factores una sola vez
        val expFactor = 2f.pow(exposure)
        val brightnessFactor = 1f + brightness * 0.5f
        val satFactor = 1f + saturation
        val tempScale = 1f + temperature * 0.15f
        val invTempScale = 1f / tempScale
        val tintScale = 1f + tint * 0.1f
        val contrastSlope = 1f + contrast * 2f
        
        // Flags para evitar checks innecesarios en el loop
        val hasExposure = exposure != 0f
        val hasFilmic = exposure > 0.1f
        val hasHighShadow = highlights != 0f || shadows != 0f
        val hasTemp = temperature != 0f || tint != 0f
        val hasContrast = contrast != 0f
        val hasBrightness = brightness != 0f
        val hasSaturation = saturation != 0f
        val hasGrain = grain > 0f
        
        val chunkSize = (totalPixels + NUM_THREADS - 1) / NUM_THREADS
        
        runBlocking(Dispatchers.Default) {
            (0 until NUM_THREADS).map { threadIdx ->
                async {
                    val start = threadIdx * chunkSize
                    val end = minOf(start + chunkSize, totalPixels)
                    val random = if (hasGrain) Random(threadIdx * 12345L) else null
                    
                    for (i in start until end) {
                        val pixel = pixels[i]
                        val a = (pixel shr 24) and 0xFF
                        
                        // 1. sRGB → Linear usando LUT (RÁPIDO)
                        var r = srgbToLinearFast((pixel shr 16) and 0xFF)
                        var g = srgbToLinearFast((pixel shr 8) and 0xFF)
                        var b = srgbToLinearFast(pixel and 0xFF)
                        
                        // 2. Exposición
                        if (hasExposure) {
                            r *= expFactor
                            g *= expFactor
                            b *= expFactor
                        }
                        
                        // 3. Filmic tonemap (ACES simplificado)
                        if (hasFilmic) {
                            r = acesTonemap(r)
                            g = acesTonemap(g)
                            b = acesTonemap(b)
                        }
                        
                        // 4. Highlights & Shadows
                        if (hasHighShadow) {
                            val lum = getLuminance(r, g, b)
                            val highlightMask = smoothstep(0.35f, 0.85f, lum)
                            val shadowMask = 1f - smoothstep(0.15f, 0.65f, lum)
                            
                            if (highlights != 0f) {
                                val hFactor = if (highlights < 0f) 
                                    1f - (-highlights * highlightMask * 0.4f)
                                else 
                                    1f + highlights * highlightMask * 0.3f
                                r *= hFactor; g *= hFactor; b *= hFactor
                            }
                            
                            if (shadows != 0f) {
                                if (shadows > 0f) {
                                    val lift = shadows * shadowMask * shadowMask * 0.4f
                                    r += (1f - r) * lift
                                    g += (1f - g) * lift
                                    b += (1f - b) * lift
                                } else {
                                    val sFactor = 1f + shadows * shadowMask * 0.5f
                                    r *= sFactor; g *= sFactor; b *= sFactor
                                }
                            }
                        }
                        
                        // 5. Temperatura y Tinte
                        if (hasTemp) {
                            r *= tempScale
                            b *= invTempScale
                            g *= tintScale
                        }
                        
                        // 6. Contraste con curva S inline
                        if (hasContrast) {
                            r = sCurveInline(r.coerceIn(0f, 1f), contrastSlope)
                            g = sCurveInline(g.coerceIn(0f, 1f), contrastSlope)
                            b = sCurveInline(b.coerceIn(0f, 1f), contrastSlope)
                        }
                        
                        // 7. Brillo
                        if (hasBrightness) {
                            r *= brightnessFactor
                            g *= brightnessFactor
                            b *= brightnessFactor
                        }
                        
                        // 8. Saturación perceptual
                        if (hasSaturation) {
                            val lum = getLuminance(r, g, b)
                            r = lum + (r - lum) * satFactor
                            g = lum + (g - lum) * satFactor
                            b = lum + (b - lum) * satFactor
                        }
                        
                        // 9-10. Clamp + Linear → sRGB usando LUT (RÁPIDO)
                        val rOut = linearToSrgbFast(r)
                        val gOut = linearToSrgbFast(g)
                        val bOut = linearToSrgbFast(b)
                        
                        // 11. Grain (solo si necesario)
                        if (hasGrain && random != null) {
                            val lum = (rOut + gOut + bOut) / 765f // Aprox luminance
                            val grainStrength = (grain * 38f * (1f - lum * 0.7f)).toInt()
                            val noise = random.nextInt(-grainStrength, grainStrength + 1)
                            pixels[i] = (a shl 24) or 
                                ((rOut + noise).coerceIn(0, 255) shl 16) or 
                                ((gOut + noise).coerceIn(0, 255) shl 8) or 
                                (bOut + noise).coerceIn(0, 255)
                        } else {
                            pixels[i] = (a shl 24) or (rOut shl 16) or (gOut shl 8) or bOut
                        }
                    }
                }
            }.awaitAll()
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
    
    // ACES tonemap inline
    private inline fun acesTonemap(x: Float): Float {
        val a = 2.51f; val b = 0.03f; val c = 2.43f; val d = 0.59f; val e = 0.14f
        return ((x * (a * x + b)) / (x * (c * x + d) + e)).coerceIn(0f, 1f)
    }
    
    // Curva S inline con slope precalculado
    private inline fun sCurveInline(x: Float, slope: Float): Float {
        return if (x < 0.5f) 0.5f * (x / 0.5f).pow(slope)
        else 1f - 0.5f * ((1f - x) / 0.5f).pow(slope)
    }
    
    /**
     * Procesa imagen grande en resolución reducida (MUCHO más rápido)
     */
    private fun applyAdjustmentsScaled(
        bitmap: Bitmap,
        brightness: Float, contrast: Float, saturation: Float, exposure: Float,
        highlights: Float, shadows: Float, temperature: Float, tint: Float, grain: Float
    ) {
        val width = bitmap.width
        val height = bitmap.height
        
        // Calcular escala para ~800k pixels
        val scale = sqrt(800_000f / (width * height))
        val scaledW = (width * scale).toInt().coerceAtLeast(100)
        val scaledH = (height * scale).toInt().coerceAtLeast(100)
        
        // Crear bitmap escalado
        val scaled = Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true)
        
        // Aplicar ajustes al bitmap pequeño (rápido)
        val pixels = IntArray(scaledW * scaledH)
        scaled.getPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)
        
        // Procesar directamente sin más recursión
        processPixelsDirectly(pixels, brightness, contrast, saturation, exposure,
            highlights, shadows, temperature, tint, grain)
        
        scaled.setPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)
        
        // Escalar de vuelta al tamaño original
        val result = Bitmap.createScaledBitmap(scaled, width, height, true)
        
        // Copiar resultado al bitmap original
        val finalPixels = IntArray(width * height)
        result.getPixels(finalPixels, 0, width, 0, 0, width, height)
        bitmap.setPixels(finalPixels, 0, width, 0, 0, width, height)
        
        // Limpiar
        scaled.recycle()
        result.recycle()
    }
    
    /**
     * Procesa pixels directamente sin crear coroutines (para bitmaps pequeños)
     */
    private fun processPixelsDirectly(
        pixels: IntArray,
        brightness: Float, contrast: Float, saturation: Float, exposure: Float,
        highlights: Float, shadows: Float, temperature: Float, tint: Float, grain: Float
    ) {
        val expFactor = 2f.pow(exposure)
        val brightnessFactor = 1f + brightness * 0.5f
        val satFactor = 1f + saturation
        val tempScale = 1f + temperature * 0.15f
        val invTempScale = 1f / tempScale
        val tintScale = 1f + tint * 0.1f
        val contrastSlope = 1f + contrast * 2f
        
        val hasExposure = exposure != 0f
        val hasFilmic = exposure > 0.1f
        val hasHighShadow = highlights != 0f || shadows != 0f
        val hasTemp = temperature != 0f || tint != 0f
        val hasContrast = contrast != 0f
        val hasBrightness = brightness != 0f
        val hasSaturation = saturation != 0f
        val hasGrain = grain > 0f
        val random = if (hasGrain) Random(42) else null
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel shr 24) and 0xFF
            
            var r = srgbToLinearFast((pixel shr 16) and 0xFF)
            var g = srgbToLinearFast((pixel shr 8) and 0xFF)
            var b = srgbToLinearFast(pixel and 0xFF)
            
            if (hasExposure) { r *= expFactor; g *= expFactor; b *= expFactor }
            if (hasFilmic) { r = acesTonemap(r); g = acesTonemap(g); b = acesTonemap(b) }
            
            if (hasHighShadow) {
                val lum = getLuminance(r, g, b)
                if (highlights != 0f) {
                    val hMask = smoothstep(0.35f, 0.85f, lum)
                    val hFactor = if (highlights < 0f) 1f - (-highlights * hMask * 0.4f) else 1f + highlights * hMask * 0.3f
                    r *= hFactor; g *= hFactor; b *= hFactor
                }
                if (shadows != 0f) {
                    val sMask = 1f - smoothstep(0.15f, 0.65f, lum)
                    if (shadows > 0f) {
                        val lift = shadows * sMask * sMask * 0.4f
                        r += (1f-r)*lift; g += (1f-g)*lift; b += (1f-b)*lift
                    } else {
                        val sf = 1f + shadows * sMask * 0.5f
                        r *= sf; g *= sf; b *= sf
                    }
                }
            }
            
            if (hasTemp) { r *= tempScale; b *= invTempScale; g *= tintScale }
            if (hasContrast) {
                r = sCurveInline(r.coerceIn(0f,1f), contrastSlope)
                g = sCurveInline(g.coerceIn(0f,1f), contrastSlope)
                b = sCurveInline(b.coerceIn(0f,1f), contrastSlope)
            }
            if (hasBrightness) { r *= brightnessFactor; g *= brightnessFactor; b *= brightnessFactor }
            if (hasSaturation) {
                val lum = getLuminance(r, g, b)
                r = lum + (r - lum) * satFactor
                g = lum + (g - lum) * satFactor
                b = lum + (b - lum) * satFactor
            }
            
            val rOut = linearToSrgbFast(r)
            val gOut = linearToSrgbFast(g)
            val bOut = linearToSrgbFast(b)
            
            if (hasGrain && random != null) {
                val grainStrength = (grain * 38f).toInt()
                val noise = random.nextInt(-grainStrength, grainStrength + 1)
                pixels[i] = (a shl 24) or 
                    ((rOut + noise).coerceIn(0, 255) shl 16) or 
                    ((gOut + noise).coerceIn(0, 255) shl 8) or 
                    (bOut + noise).coerceIn(0, 255)
            } else {
                pixels[i] = (a shl 24) or (rOut shl 16) or (gOut shl 8) or bOut
            }
        }
    }
    
    /**
     * Versión legacy para compatibilidad
     */
    fun applyAdjustments(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        exposure: Float,
        grain: Float
    ) {
        applyAdjustmentsFull(bitmap, brightness, contrast, saturation, exposure, 0f, 0f, 0f, 0f, grain)
    }
    
    /**
     * Aplica solo brillo para preview rápido
     */
    fun applyBrightness(bitmap: Bitmap, value: Float) {
        val cm = ColorMatrix().apply {
            setScale(1f + value * 0.5f, 1f + value * 0.5f, 1f + value * 0.5f, 1f)
        }
        applyColorMatrix(bitmap, cm)
    }
    
    /**
     * Aplica solo contraste para preview rápido
     */
    fun applyContrast(bitmap: Bitmap, value: Float) {
        val scale = 1f + value
        val translate = (-0.5f * scale + 0.5f) * 255f
        val cm = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        applyColorMatrix(bitmap, cm)
    }
    
    private fun applyColorMatrix(bitmap: Bitmap, cm: ColorMatrix) {
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }
    
    /**
     * Resetea el cache de LUTs (no-op en implementación Kotlin)
     */
    fun resetCache() {
        // No-op - sin cache en implementación Kotlin
    }
    
    /**
     * Aplica ajustes desde un ImageAdjustState
     */
    fun applyFromState(bitmap: Bitmap, state: ImageAdjustState) {
        applyAdjustmentsFull(
            bitmap,
            state.brightness,
            state.contrast,
            state.saturation,
            state.exposure,
            state.highlights,
            state.shadows,
            state.temperature,
            state.tint,
            state.grain
        )
    }
    
    /**
     * Crea una copia del bitmap y aplica ajustes completos
     */
    fun applyAdjustmentsCopyFull(
        source: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        exposure: Float,
        highlights: Float,
        shadows: Float,
        temperature: Float,
        tint: Float,
        grain: Float
    ): Bitmap {
        val copy = source.copy(Bitmap.Config.ARGB_8888, true)
        applyAdjustmentsFull(
            copy, brightness, contrast, saturation, exposure,
            highlights, shadows, temperature, tint, grain
        )
        return copy
    }
    
    /**
     * Crea una copia del bitmap y aplica ajustes desde estado
     */
    fun applyFromStateCopy(source: Bitmap, state: ImageAdjustState): Bitmap {
        val copy = source.copy(Bitmap.Config.ARGB_8888, true)
        applyFromState(copy, state)
        return copy
    }
    
    /**
     * Versión legacy - Crea una copia del bitmap y aplica ajustes básicos
     */
    fun applyAdjustmentsCopy(
        source: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        exposure: Float,
        grain: Float
    ): Bitmap {
        val copy = source.copy(Bitmap.Config.ARGB_8888, true)
        applyAdjustments(copy, brightness, contrast, saturation, exposure, grain)
        return copy
    }
}
