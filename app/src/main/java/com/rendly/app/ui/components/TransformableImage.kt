package com.rendly.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.rendly.app.gpu.TransformBridge
import kotlinx.coroutines.isActive

/**
 * Imagen transformable con motor C++ profesional
 * 
 * Características:
 * - Pan/Scale/Rotate con gestos multitouch
 * - Inercia física al soltar
 * - Rubber-band en límites de zoom
 * - Zoom logarítmico para UX natural
 * - 60+ FPS garantizado
 */
@Composable
fun TransformableImage(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    minScale: Float = 0.5f,
    maxScale: Float = 8.0f,
    enableRotation: Boolean = true,
    snapToAngles: Boolean = false,
    contentScale: ContentScale = ContentScale.Fit,
    onTransformChanged: ((translateX: Float, translateY: Float, scale: Float, rotation: Float) -> Unit)? = null
) {
    if (bitmap == null) return
    
    val engine = remember { TransformBridge() }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Estado observable desde C++
    var translateX by remember { mutableFloatStateOf(0f) }
    var translateY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    
    // Configurar engine
    DisposableEffect(minScale, maxScale, snapToAngles) {
        engine.setConfig(
            minScale = minScale,
            maxScale = maxScale,
            friction = 0.92f,
            snapAngle = if (snapToAngles) 90f else 0f,
            snapThreshold = 0.15f
        )
        onDispose {
            engine.destroy()
        }
    }
    
    // Actualizar tamaños
    LaunchedEffect(viewportSize, bitmap) {
        if (viewportSize.width > 0 && viewportSize.height > 0) {
            engine.setViewportSize(viewportSize.width.toFloat(), viewportSize.height.toFloat())
            engine.setImageSize(bitmap.width.toFloat(), bitmap.height.toFloat())
        }
    }
    
    // Animation loop para inercia
    LaunchedEffect(isAnimating) {
        if (!isAnimating) return@LaunchedEffect
        
        while (isActive && isAnimating) {
            withFrameMillis { frameTimeMillis ->
                if (lastFrameTime == 0L) {
                    lastFrameTime = frameTimeMillis
                    return@withFrameMillis
                }
                
                val deltaTime = (frameTimeMillis - lastFrameTime) / 1000f
                lastFrameTime = frameTimeMillis
                
                val needsUpdate = engine.update(deltaTime.coerceAtMost(0.1f))
                if (needsUpdate) {
                    translateX = engine.translateX
                    translateY = engine.translateY
                    scale = engine.scale
                    rotation = engine.rotation
                    onTransformChanged?.invoke(translateX, translateY, scale, rotation)
                } else {
                    isAnimating = false
                }
            }
        }
    }
    
    Box(
        modifier = modifier
            .onSizeChanged { viewportSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    engine.onGestureStart()
                    lastFrameTime = 0L
                    
                    var anyPressed = true
                    while (anyPressed) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        
                        if (pressed.isNotEmpty()) {
                            val pan = event.calculatePan()
                            val zoom = event.calculateZoom()
                            val rot = if (enableRotation) event.calculateRotation() else 0f
                            val centroid = event.calculateCentroid(useCurrent = true)
                            
                            engine.onGestureUpdate(
                                pan.x, pan.y,
                                zoom, rot,
                                centroid.x, centroid.y
                            )
                            
                            // Sync state
                            translateX = engine.translateX
                            translateY = engine.translateY
                            scale = engine.scale
                            rotation = engine.rotation
                            onTransformChanged?.invoke(translateX, translateY, scale, rotation)
                            
                            event.changes.forEach { it.consume() }
                        }
                        
                        anyPressed = pressed.isNotEmpty()
                    }
                    
                    // Gesture ended - start inertia
                    engine.onGestureEnd(0f, 0f, 0f, 0f)
                    isAnimating = engine.isAnimating
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = translateX
                    translationY = translateY
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
        )
    }
}

/**
 * Estado de transformación para uso externo
 */
data class ImageTransformState(
    val translateX: Float = 0f,
    val translateY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
) {
    fun isDefault(): Boolean = 
        translateX == 0f && translateY == 0f && scale == 1f && rotation == 0f
}
