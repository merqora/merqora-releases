package com.mercora.app.native

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.withFrameMillis
import kotlin.math.abs
import kotlin.math.exp

/**
 * Fling behavior impulsado por el motor de física nativo C++ (scroll_physics.cpp).
 *
 * - Al soltar el dedo, la física C++ (decaimiento exponencial de fricción)
 *   calcula la trayectoria completa del fling.
 * - Cada frame el motor avanza su simulación y el resultado se aplica al
 *   LazyColumn vía scrollBy(), hasta que el motor queda en reposo o se
 *   alcanza un extremo del contenido.
 * - Durante el fling se consulta el prefetch predictivo del motor
 *   (dirección + cantidad de items) para precargar contenido adelante.
 * - Si la librería nativa no está disponible, usa un fallback Kotlin
 *   con la misma física.
 */
class NativeFlingBehavior(
    private val onNativePrefetch: (direction: Int, count: Int) -> Unit = { _, _ -> }
) : FlingBehavior {

    private var lastPrefetchHintMs = 0L

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val engine = FeedEngine
        if (!engine.isAvailable()) return kotlinFallbackFling(initialVelocity)

        engine.onDragEnd(initialVelocity)
        if (engine.isSettled()) return 0f

        var lastOffset = engine.getScrollOffset()

        while (!engine.isSettled()) {
            engine.update()
            val target = engine.getScrollOffset()
            val delta = target - lastOffset
            lastOffset = target
            if (delta == 0f) break

            val consumed = scrollBy(delta)
            if (consumed != delta) break

            val nowMs = withFrameMillis { it }
            if (nowMs - lastPrefetchHintMs >= 150L) {
                lastPrefetchHintMs = nowMs
                if (engine.shouldPrefetch()) {
                    onNativePrefetch(engine.getPrefetchDirection(), engine.getPrefetchCount())
                }
            }
        }

        engine.setScrollOffset(0f)
        return 0f
    }

    private suspend fun ScrollScope.kotlinFallbackFling(initialVelocity: Float): Float {
        if (abs(initialVelocity) <= 8f) return 0f
        var velocity = initialVelocity
        var lastFrameMs = 0L
        while (abs(velocity) > 8f) {
            val nowMs = withFrameMillis { it }
            val dt = ((nowMs - lastFrameMs).coerceAtMost(32L)).toFloat() / 1000f
            if (lastFrameMs == 0L) {
                lastFrameMs = nowMs
                continue
            }
            lastFrameMs = nowMs
            val delta = velocity * dt
            velocity *= exp(-6f * dt)
            val consumed = scrollBy(delta)
            if (consumed != delta) return 0f
        }
        return 0f
    }
}
