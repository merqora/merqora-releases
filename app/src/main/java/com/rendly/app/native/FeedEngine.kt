package com.rendly.app.native

import android.util.Log

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * FEED ENGINE - Motor de scroll nativo C++ para 60+ FPS
 * ══════════════════════════════════════════════════════════════════════════════
 * 
 * ARQUITECTURA:
 * - C++ (FeedEngine.cpp): 100% de física de scroll, inercia, prefetch
 * - Kotlin: Solo consulta valores y los aplica al LazyColumn
 * 
 * USO:
 * - Sincronizar con LazyListState para obtener información de scroll
 * - Usar shouldPrefetch() para cargar items anticipadamente
 * - Usar getPrefetchCount() para determinar cuántos items cargar
 * 
 * ══════════════════════════════════════════════════════════════════════════════
 */
object FeedEngine {
    
    private const val TAG = "FeedEngine"
    private var isLoaded = false
    
    init {
        try {
            System.loadLibrary("Merqora-native")
            isLoaded = true
            Log.i(TAG, "✓ Native feed engine loaded")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "✗ Failed to load native library", e)
            isLoaded = false
        }
    }
    
    /** Inicializa el motor con dimensiones del viewport y contenido */
    fun init(viewportHeight: Float, contentHeight: Float) {
        if (!isLoaded) return
        nativeInit(viewportHeight, contentHeight)
    }
    
    /** Notifica inicio de drag */
    fun onDragStart() {
        if (!isLoaded) return
        nativeOnDragStart()
    }
    
    /** Actualiza durante drag */
    fun onDrag(delta: Float, velocity: Float) {
        if (!isLoaded) return
        nativeOnDrag(delta, velocity)
    }
    
    /** Notifica fin de drag con velocidad final */
    fun onDragEnd(velocity: Float) {
        if (!isLoaded) return
        nativeOnDragEnd(velocity)
    }
    
    /** Actualiza física cada frame */
    fun update() {
        if (!isLoaded) return
        nativeUpdate()
    }
    
    /** Obtiene offset actual de scroll */
    fun getScrollOffset(): Float {
        if (!isLoaded) return 0f
        return nativeGetScrollOffset()
    }
    
    /** Obtiene velocidad actual */
    fun getVelocity(): Float {
        if (!isLoaded) return 0f
        return nativeGetVelocity()
    }
    
    /** Verifica si el scroll está en reposo */
    fun isSettled(): Boolean {
        if (!isLoaded) return true
        return nativeIsSettled()
    }
    
    /** Indica si debemos prefetch items */
    fun shouldPrefetch(): Boolean {
        if (!isLoaded) return false
        return nativeShouldPrefetch()
    }
    
    /** Cantidad de items a prefetch basado en velocidad */
    fun getPrefetchCount(): Int {
        if (!isLoaded) return 2
        return nativeGetPrefetchCount()
    }
    
    /** Dirección del prefetch: 1=down, -1=up, 0=none */
    fun getPrefetchDirection(): Int {
        if (!isLoaded) return 0
        return nativeGetPrefetchDirection()
    }
    
    /** Actualiza altura del contenido cuando cambia */
    fun setContentHeight(height: Float) {
        if (!isLoaded) return
        nativeSetContentHeight(height)
    }
    
    /** Establece offset de scroll directo */
    fun setScrollOffset(offset: Float) {
        if (!isLoaded) return
        nativeSetScrollOffset(offset)
    }
    
    // ══════════════════════════════════════════════════════════════════
    // JNI NATIVES - Implementados en FeedEngine.cpp
    // ══════════════════════════════════════════════════════════════════
    
    private external fun nativeInit(viewportHeight: Float, contentHeight: Float)
    private external fun nativeOnDragStart()
    private external fun nativeOnDrag(delta: Float, velocity: Float)
    private external fun nativeOnDragEnd(velocity: Float)
    private external fun nativeUpdate()
    private external fun nativeGetScrollOffset(): Float
    private external fun nativeGetVelocity(): Float
    private external fun nativeIsSettled(): Boolean
    private external fun nativeShouldPrefetch(): Boolean
    private external fun nativeGetPrefetchCount(): Int
    private external fun nativeGetPrefetchDirection(): Int
    private external fun nativeSetContentHeight(height: Float)
    private external fun nativeSetScrollOffset(offset: Float)
}
