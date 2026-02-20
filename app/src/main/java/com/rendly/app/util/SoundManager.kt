package com.rendly.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.rendly.app.R

/**
 * Gestor de sonidos para notificaciones y mensajes
 * Usa los archivos de sonido en res/raw/
 */
object SoundManager {
    private const val TAG = "SoundManager"
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    private var appContext: Context? = null
    
    // Control de frecuencia para evitar sonidos repetitivos
    private var lastSoundTime = 0L
    private const val MIN_SOUND_INTERVAL = 500L // 500ms entre sonidos
    
    fun init(context: Context) {
        appContext = context.applicationContext
        isInitialized = true
    }
    
    /**
     * Reproduce sonido de notificación
     */
    fun playNotificationSound() {
        playSound(SoundType.NOTIFICATION)
    }
    
    /**
     * Reproduce sonido de mensaje nuevo
     */
    fun playMessageSound() {
        playSound(SoundType.MESSAGE)
    }
    
    private fun playSound(type: SoundType) {
        val context = appContext ?: return
        
        // Evitar sonidos muy seguidos
        val now = System.currentTimeMillis()
        if (now - lastSoundTime < MIN_SOUND_INTERVAL) {
            return
        }
        lastSoundTime = now
        
        try {
            // Liberar reproductor anterior si existe
            mediaPlayer?.release()
            
            val soundResId = when (type) {
                SoundType.NOTIFICATION -> R.raw.notification_sound
                SoundType.MESSAGE -> R.raw.message_sound
            }
            
            mediaPlayer = MediaPlayer.create(context, soundResId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setVolume(0.7f, 0.7f) // Volumen moderado
                setOnCompletionListener { mp ->
                    mp.release()
                }
                start()
            }
            
            Log.d(TAG, "Playing ${type.name} sound")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
            // Fallback al sonido del sistema
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(context, notification)?.play()
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback sound also failed: ${e2.message}")
            }
        }
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    enum class SoundType {
        NOTIFICATION,
        MESSAGE
    }
}
