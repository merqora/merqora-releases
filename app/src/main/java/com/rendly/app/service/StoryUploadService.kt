package com.rendly.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rendly.app.MainActivity
import com.rendly.app.R
import com.rendly.app.data.repository.StoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StoryUploadService {
    private const val CHANNEL_ID = "story_upload_channel"
    private const val NOTIFICATION_ID = 1001
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Publicación de Historia"
            val descriptionText = "Muestra el progreso de publicación de historias"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun uploadStoryWithNotification(
        context: Context,
        bitmap: Bitmap,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        createNotificationChannel(context)
        
        val notificationManager = NotificationManagerCompat.from(context)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle("Publicando historia")
            .setContentText("Preparando... 0%")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setProgress(100, 0, false)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
        
        // Mostrar notificación inicial
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // No permission for notifications, continue without
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            // Mostrar progreso detallado con delays para que sea visible
            val progressSteps = listOf(
                0 to "Preparando imagen...",
                5 to "Comprimiendo...",
                10 to "Conectando al servidor..."
            )
            
            for ((progress, text) in progressSteps) {
                try {
                    builder.setProgress(100, progress, false)
                        .setContentText("$text $progress%")
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                    kotlinx.coroutines.delay(400)
                } catch (e: SecurityException) { }
            }
            
            val result = StoryRepository.uploadStory(bitmap) { progress ->
                // El progreso de Cloudinary va de 0-1, ajustamos para mostrar 15-75%
                val adjustedProgress = 15 + (progress * 60).toInt()
                val progressInt = adjustedProgress.coerceIn(15, 75)
                
                CoroutineScope(Dispatchers.Main).launch {
                    val statusText = when {
                        progressInt < 30 -> "Subiendo imagen..."
                        progressInt < 50 -> "Procesando..."
                        progressInt < 70 -> "Casi listo..."
                        else -> "Finalizando subida..."
                    }
                    builder.setProgress(100, progressInt, false)
                        .setContentText("$statusText $progressInt%")
                    
                    try {
                        notificationManager.notify(NOTIFICATION_ID, builder.build())
                    } catch (e: SecurityException) {
                        // Ignore
                    }
                }
            }
            
            result.fold(
                onSuccess = {
                    // Mostrar progreso final detallado (~2.5 segundos total)
                    val finalSteps = listOf(
                        80 to "Guardando en servidor...",
                        90 to "Verificando...",
                        95 to "Casi listo...",
                        100 to "¡Completado!"
                    )
                    
                    for ((progress, text) in finalSteps) {
                        try {
                            builder.setProgress(100, progress, false)
                                .setContentText("$text $progress%")
                            notificationManager.notify(NOTIFICATION_ID, builder.build())
                            kotlinx.coroutines.delay(500)
                        } catch (e: SecurityException) { }
                    }
                    
                    builder.setContentTitle("¡Historia publicada!")
                        .setContentText("Tu historia está visible por 24 horas ✨")
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setSmallIcon(android.R.drawable.ic_menu_send)
                    
                    try {
                        notificationManager.notify(NOTIFICATION_ID, builder.build())
                    } catch (e: SecurityException) {
                        // Ignore
                    }
                    
                    // Refrescar stories para que aparezcan en el home
                    CoroutineScope(Dispatchers.Main).launch {
                        StoryRepository.loadMyStories()
                    }
                    
                    onComplete()
                },
                onFailure = { error ->
                    builder.setContentTitle("Error al publicar")
                        .setContentText(error.message ?: "Intenta de nuevo")
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                    
                    try {
                        notificationManager.notify(NOTIFICATION_ID, builder.build())
                    } catch (e: SecurityException) {
                        // Ignore
                    }
                    
                    onError(error.message ?: "Error desconocido")
                }
            )
        }
    }
}
