package com.rendly.app.gpu

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * GLSurfaceView customizada para ajustes de imagen GPU
 * Configurada para máximo rendimiento (60+ FPS)
 */
class GPUImageSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    val renderer = GPUImageRenderer()

    init {
        // OpenGL ES 3.0
        setEGLContextClientVersion(3)
        
        // Configuración de superficie para máximo rendimiento
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        
        // Renderer
        setRenderer(renderer)
        
        // Renderizar solo cuando se solicita (máximo control, mínimo consumo)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setBitmap(bitmap: Bitmap?) {
        renderer.setBitmap(bitmap)
        requestRender()
    }

    fun updateAdjustments(
        brightness: Float = renderer.brightness,
        contrast: Float = renderer.contrast,
        saturation: Float = renderer.saturation,
        exposure: Float = renderer.exposure,
        highlights: Float = renderer.highlights,
        shadows: Float = renderer.shadows,
        temperature: Float = renderer.temperature,
        tint: Float = renderer.tint,
        grain: Float = renderer.grain
    ) {
        renderer.brightness = brightness
        renderer.contrast = contrast
        renderer.saturation = saturation
        renderer.exposure = exposure
        renderer.highlights = highlights
        renderer.shadows = shadows
        renderer.temperature = temperature
        renderer.tint = tint
        renderer.grain = grain
        requestRender()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderer.release()
    }
}

/**
 * Estado de ajustes de imagen completo
 * Incluye todos los parámetros nivel Instagram/Lightroom
 */
@Stable
data class ImageAdjustmentState(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val exposure: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val grain: Float = 0f
) {
    fun hasChanges(): Boolean = 
        brightness != 0f || contrast != 0f || saturation != 0f || 
        exposure != 0f || highlights != 0f || shadows != 0f ||
        temperature != 0f || tint != 0f || grain != 0f
    
    companion object {
        val Default = ImageAdjustmentState()
    }
}

/**
 * Composable que envuelve GPUImageSurfaceView para uso en Compose
 * Preview GPU-accelerated con ajustes en tiempo real
 */
@Composable
fun GPUAdjustedImage(
    bitmap: Bitmap?,
    adjustments: ImageAdjustmentState,
    modifier: Modifier = Modifier,
    onViewReady: ((GPUImageSurfaceView) -> Unit)? = null
) {
    // Referencia a la vista para actualizaciones
    var surfaceView by remember { mutableStateOf<GPUImageSurfaceView?>(null) }
    
    // Actualizar bitmap cuando cambie
    LaunchedEffect(bitmap) {
        surfaceView?.setBitmap(bitmap)
    }
    
    // Actualizar ajustes cuando cambien (instantáneo, sin recomposición)
    LaunchedEffect(adjustments) {
        surfaceView?.updateAdjustments(
            brightness = adjustments.brightness,
            contrast = adjustments.contrast,
            saturation = adjustments.saturation,
            exposure = adjustments.exposure,
            highlights = adjustments.highlights,
            shadows = adjustments.shadows,
            temperature = adjustments.temperature,
            tint = adjustments.tint,
            grain = adjustments.grain
        )
    }
    
    AndroidView(
        factory = { context ->
            GPUImageSurfaceView(context).also { view ->
                surfaceView = view
                view.setBitmap(bitmap)
                view.updateAdjustments(
                    brightness = adjustments.brightness,
                    contrast = adjustments.contrast,
                    saturation = adjustments.saturation,
                    exposure = adjustments.exposure,
                    highlights = adjustments.highlights,
                    shadows = adjustments.shadows,
                    temperature = adjustments.temperature,
                    tint = adjustments.tint,
                    grain = adjustments.grain
                )
                onViewReady?.invoke(view)
            }
        },
        update = { view ->
            view.updateAdjustments(
                brightness = adjustments.brightness,
                contrast = adjustments.contrast,
                saturation = adjustments.saturation,
                exposure = adjustments.exposure,
                highlights = adjustments.highlights,
                shadows = adjustments.shadows,
                temperature = adjustments.temperature,
                tint = adjustments.tint,
                grain = adjustments.grain
            )
        },
        modifier = modifier
    )
}
