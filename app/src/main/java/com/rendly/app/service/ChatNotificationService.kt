package com.rendly.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rendly.app.MainActivity
import com.rendly.app.R
import com.rendly.app.util.SoundManager

object ChatNotificationService {
    private const val CHANNEL_ID = "chat_messages_channel"
    private const val NOTIFICATION_ID_BASE = 2000
    
    // Para evitar notificaciones duplicadas
    private val shownNotifications = mutableSetOf<String>()
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mensajes de Chat"
            val descriptionText = "Notificaciones de nuevos mensajes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageContent: String,
        conversationId: String,
        messageId: String
    ) {
        // Evitar mostrar la misma notificación dos veces
        if (shownNotifications.contains(messageId)) return
        shownNotifications.add(messageId)
        
        // Limpiar notificaciones antiguas (mantener solo las últimas 50)
        if (shownNotifications.size > 50) {
            shownNotifications.clear()
        }
        
        createNotificationChannel(context)
        
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Intent para abrir la app en el chat
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_chat", true)
            putExtra("conversation_id", conversationId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            conversationId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Truncar mensaje si es muy largo
        val truncatedMessage = if (messageContent.length > 100) {
            messageContent.take(100) + "..."
        } else {
            messageContent
        }
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(senderName)
            .setContentText(truncatedMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(truncatedMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        try {
            // Usar hash del conversationId para agrupar notificaciones del mismo chat
            val notificationId = NOTIFICATION_ID_BASE + conversationId.hashCode().mod(1000)
            notificationManager.notify(notificationId, builder.build())
            
            // Reproducir sonido de mensaje
            SoundManager.init(context)
            SoundManager.playMessageSound()
        } catch (e: SecurityException) {
            // No hay permiso de notificaciones
        }
    }
    
    fun cancelChatNotifications(context: Context, conversationId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val notificationId = NOTIFICATION_ID_BASE + conversationId.hashCode().mod(1000)
        notificationManager.cancel(notificationId)
    }
}
