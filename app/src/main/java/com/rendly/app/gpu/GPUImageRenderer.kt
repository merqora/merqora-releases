package com.rendly.app.gpu

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Renderer OpenGL ES 3.0 para ajustes de imagen en tiempo real
 * Arquitectura GPU-first: todos los ajustes via uniforms = 0 dropped frames
 * Calidad nivel Instagram/Lightroom
 */
class GPUImageRenderer : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "GPUImageRenderer"
        
        // Coordenadas del quad (pantalla completa)
        private val VERTEX_DATA = floatArrayOf(
            -1f, -1f,  // Bottom left
             1f, -1f,  // Bottom right
            -1f,  1f,  // Top left
             1f,  1f   // Top right
        )
        
        // Coordenadas de textura
        private val TEXTURE_DATA = floatArrayOf(
            0f, 1f,  // Bottom left (flipped Y for Android)
            1f, 1f,  // Bottom right
            0f, 0f,  // Top left
            1f, 0f   // Top right
        )
    }

    // Buffers OpenGL
    private var vertexBuffer: FloatBuffer
    private var textureBuffer: FloatBuffer
    
    // Handles OpenGL
    private var programHandle = 0
    private var textureHandle = 0
    private var textureId = 0
    
    // Uniform locations (cacheados para máximo rendimiento)
    private var uTextureLocation = 0
    private var uBrightnessLocation = 0
    private var uContrastLocation = 0
    private var uSaturationLocation = 0
    private var uExposureLocation = 0
    private var uHighlightsLocation = 0
    private var uShadowsLocation = 0
    private var uTemperatureLocation = 0
    private var uTintLocation = 0
    private var uGrainLocation = 0
    private var uTimeLocation = 0
    private var uResolutionLocation = 0
    
    // Attribute locations
    private var aPositionLocation = 0
    private var aTexCoordLocation = 0
    
    // Parámetros de ajuste actuales (volátiles para thread-safety)
    @Volatile var brightness = 0f
    @Volatile var contrast = 0f
    @Volatile var saturation = 0f
    @Volatile var exposure = 0f
    @Volatile var highlights = 0f
    @Volatile var shadows = 0f
    @Volatile var temperature = 0f
    @Volatile var tint = 0f
    @Volatile var grain = 0f
    
    // Bitmap a renderizar
    @Volatile private var pendingBitmap: Bitmap? = null
    @Volatile private var bitmapUpdated = false
    
    // Dimensiones
    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var bitmapWidth = 0
    private var bitmapHeight = 0
    
    // Callback cuando el frame está listo
    var onFrameRendered: (() -> Unit)? = null

    init {
        // Crear buffers
        vertexBuffer = ByteBuffer.allocateDirect(VERTEX_DATA.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(VERTEX_DATA)
        vertexBuffer.position(0)
        
        textureBuffer = ByteBuffer.allocateDirect(TEXTURE_DATA.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(TEXTURE_DATA)
        textureBuffer.position(0)
    }

    fun setBitmap(bitmap: Bitmap?) {
        pendingBitmap = bitmap
        bitmap?.let {
            bitmapWidth = it.width
            bitmapHeight = it.height
            updateTextureCoordinates()
        }
        bitmapUpdated = true
    }
    
    /**
     * Actualiza las coordenadas de textura para ContentScale.Crop
     * Centra y recorta la imagen para llenar la superficie manteniendo aspect ratio
     */
    private fun updateTextureCoordinates() {
        if (surfaceWidth == 0 || surfaceHeight == 0 || bitmapWidth == 0 || bitmapHeight == 0) return
        
        val surfaceAspect = surfaceWidth.toFloat() / surfaceHeight.toFloat()
        val bitmapAspect = bitmapWidth.toFloat() / bitmapHeight.toFloat()
        
        val (left, right, top, bottom) = if (bitmapAspect > surfaceAspect) {
            // Bitmap es más ancho - recortar lados
            val scale = surfaceAspect / bitmapAspect
            val offset = (1f - scale) / 2f
            listOf(offset, 1f - offset, 0f, 1f)
        } else {
            // Bitmap es más alto - recortar arriba/abajo
            val scale = bitmapAspect / surfaceAspect
            val offset = (1f - scale) / 2f
            listOf(0f, 1f, offset, 1f - offset)
        }
        
        // Coordenadas de textura con crop centrado (Y invertido para Android)
        val croppedTextureData = floatArrayOf(
            left, 1f - top,    // Bottom left
            right, 1f - top,   // Bottom right  
            left, 1f - bottom, // Top left
            right, 1f - bottom // Top right
        )
        
        textureBuffer.clear()
        textureBuffer.put(croppedTextureData)
        textureBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        
        // Compilar shaders
        val vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, AdjustmentShaders.VERTEX_SHADER)
        val fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, AdjustmentShaders.FRAGMENT_SHADER)
        
        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e(TAG, "Failed to compile shaders")
            return
        }
        
        // Crear programa
        programHandle = GLES30.glCreateProgram()
        GLES30.glAttachShader(programHandle, vertexShader)
        GLES30.glAttachShader(programHandle, fragmentShader)
        GLES30.glLinkProgram(programHandle)
        
        // Verificar linkeo
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programHandle, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Program link failed: ${GLES30.glGetProgramInfoLog(programHandle)}")
            GLES30.glDeleteProgram(programHandle)
            return
        }
        
        // Obtener locations de uniforms
        uTextureLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.TEXTURE)
        uBrightnessLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.BRIGHTNESS)
        uContrastLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.CONTRAST)
        uSaturationLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.SATURATION)
        uExposureLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.EXPOSURE)
        uHighlightsLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.HIGHLIGHTS)
        uShadowsLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.SHADOWS)
        uTemperatureLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.TEMPERATURE)
        uTintLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.TINT)
        uGrainLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.GRAIN)
        uTimeLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.TIME)
        uResolutionLocation = GLES30.glGetUniformLocation(programHandle, AdjustmentShaders.Uniforms.RESOLUTION)
        
        // Obtener locations de attributes
        aPositionLocation = GLES30.glGetAttribLocation(programHandle, AdjustmentShaders.Attributes.POSITION)
        aTexCoordLocation = GLES30.glGetAttribLocation(programHandle, AdjustmentShaders.Attributes.TEX_COORD)
        
        // Crear textura
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        
        // Limpiar shaders (ya están en el programa)
        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        surfaceWidth = width
        surfaceHeight = height
        // Recalcular coordenadas de textura para ContentScale.Crop
        updateTextureCoordinates()
    }

    override fun onDrawFrame(gl: GL10?) {
        // Actualizar textura si hay nuevo bitmap
        if (bitmapUpdated) {
            pendingBitmap?.let { bitmap ->
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            }
            bitmapUpdated = false
        }
        
        // Limpiar
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        
        if (pendingBitmap == null) return
        
        // Usar programa
        GLES30.glUseProgram(programHandle)
        
        // Configurar textura
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(uTextureLocation, 0)
        
        // Configurar uniforms de ajuste (cambio instantáneo = 0 dropped frames)
        GLES30.glUniform1f(uBrightnessLocation, brightness)
        GLES30.glUniform1f(uContrastLocation, contrast)
        GLES30.glUniform1f(uSaturationLocation, saturation)
        GLES30.glUniform1f(uExposureLocation, exposure)
        GLES30.glUniform1f(uHighlightsLocation, highlights)
        GLES30.glUniform1f(uShadowsLocation, shadows)
        GLES30.glUniform1f(uTemperatureLocation, temperature)
        GLES30.glUniform1f(uTintLocation, tint)
        GLES30.glUniform1f(uGrainLocation, grain)
        GLES30.glUniform1f(uTimeLocation, System.currentTimeMillis() / 1000f)
        GLES30.glUniform2f(uResolutionLocation, surfaceWidth.toFloat(), surfaceHeight.toFloat())
        
        // Configurar vertices
        GLES30.glEnableVertexAttribArray(aPositionLocation)
        GLES30.glVertexAttribPointer(aPositionLocation, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        
        GLES30.glEnableVertexAttribArray(aTexCoordLocation)
        GLES30.glVertexAttribPointer(aTexCoordLocation, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        
        // Dibujar
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        
        // Limpiar
        GLES30.glDisableVertexAttribArray(aPositionLocation)
        GLES30.glDisableVertexAttribArray(aTexCoordLocation)
        
        onFrameRendered?.invoke()
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Shader compile failed: ${GLES30.glGetShaderInfoLog(shader)}")
            GLES30.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }

    /**
     * Renderiza el frame actual a un Bitmap para exportación
     * Usa framebuffer offscreen para captura exacta
     */
    fun renderToBitmap(width: Int, height: Int): Bitmap? {
        if (pendingBitmap == null) return null
        
        // Crear framebuffer offscreen
        val fbo = IntArray(1)
        val renderTexture = IntArray(1)
        
        GLES30.glGenFramebuffers(1, fbo, 0)
        GLES30.glGenTextures(1, renderTexture, 0)
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, renderTexture[0])
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, 
            GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo[0])
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, 
            GLES30.GL_TEXTURE_2D, renderTexture[0], 0)
        
        // Renderizar
        GLES30.glViewport(0, 0, width, height)
        onDrawFrame(null)
        
        // Leer píxeles
        val buffer = ByteBuffer.allocateDirect(width * height * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
        buffer.rewind()
        
        // Crear bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        
        // Limpiar
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glDeleteFramebuffers(1, fbo, 0)
        GLES30.glDeleteTextures(1, renderTexture, 0)
        
        // Restaurar viewport
        GLES30.glViewport(0, 0, surfaceWidth, surfaceHeight)
        
        return bitmap
    }

    fun release() {
        if (programHandle != 0) {
            GLES30.glDeleteProgram(programHandle)
            programHandle = 0
        }
        if (textureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = 0
        }
    }
}
