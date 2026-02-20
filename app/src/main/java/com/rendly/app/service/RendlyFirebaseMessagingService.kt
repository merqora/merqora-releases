package com.rendly.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.Lifecycle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rendly.app.MainActivity
import com.rendly.app.R
import com.rendly.app.data.remote.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.URL

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * Merqora FIREBASE MESSAGING SERVICE
 * ═══════════════════════════════════════════════════════════════════════════════
 * Maneja las notificaciones push incluso cuando la app está cerrada.
 * 
 * Tipos de notificaciones soportadas:
 * - message: Nuevo mensaje de chat
 * - like: Alguien dio like a tu publicación
 * - comment: Nuevo comentario en tu publicación
 * - follow: Alguien te empezó a seguir
 * - sale: Nueva venta realizada
 * - handshake: Solicitud o confirmación de handshake
 * - mention: Te mencionaron en un comentario
 * - promotion: Promociones y ofertas especiales
 */
class MerqoraFirebaseMessagingService : FirebaseMessagingService() {
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    companion object {
        private const val TAG = "MerqoraFCM"
        
        // Channel IDs
        const val CHANNEL_MESSAGES = "Merqora_messages"
        const val CHANNEL_SOCIAL = "Merqora_social"
        const val CHANNEL_TRANSACTIONS = "Merqora_transactions"
        const val CHANNEL_PROMOTIONS = "Merqora_promotions"
        const val CHANNEL_SYSTEM = "Merqora_system"
        const val CHANNEL_CALLS = "Merqora_calls"
        
        // Notification IDs base
        private const val NOTIFICATION_ID_MESSAGE = 1000
        private const val NOTIFICATION_ID_SOCIAL = 2000
        private const val NOTIFICATION_ID_TRANSACTION = 3000
        private const val NOTIFICATION_ID_PROMOTION = 4000
        
        // Detectar si la app está en primer plano
        fun isAppInForeground(): Boolean {
            return try {
                ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Reproducir sonido personalizado manualmente (solo en primer plano)
    private fun playCustomSound(isMessage: Boolean) {
        if (!isAppInForeground()) return // Solo reproducir en primer plano
        
        try {
            val soundRes = if (isMessage) R.raw.message_sound else R.raw.notification_sound
            val mediaPlayer = MediaPlayer.create(this, soundRes)
            mediaPlayer?.setOnCompletionListener { it.release() }
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error reproduciendo sonido: ${e.message}")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
    
    /**
     * Llamado cuando se recibe un nuevo token FCM.
     * El token debe ser enviado a Supabase para poder enviar notificaciones a este dispositivo.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔔 Nuevo FCM Token: $token")
        
        // Guardar token en Supabase
        serviceScope.launch {
            try {
                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    saveFcmToken(userId, token)
                } else {
                    // Guardar token localmente para enviarlo después del login
                    getSharedPreferences("Merqora_fcm", Context.MODE_PRIVATE)
                        .edit()
                        .putString("pending_fcm_token", token)
                        .apply()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando FCM token: ${e.message}")
            }
        }
    }
    
    /**
     * Llamado cuando se recibe un mensaje push.
     * Puede contener data y/o notification payload.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "📬 Mensaje recibido de: ${remoteMessage.from}")
        
        // Extraer datos del mensaje
        // Soporta data-only messages (title/body en data) Y notification messages
        val data = remoteMessage.data
        val notification = remoteMessage.notification
        
        // Determinar tipo de notificación
        val notificationType = data["type"] ?: "general"
        val title = data["title"] ?: notification?.title ?: "Merqora"
        val body = data["body"] ?: notification?.body ?: ""
        val imageUrl = data["image_url"] ?: notification?.imageUrl?.toString()
        val clickAction = data["click_action"] ?: "OPEN_APP"
        val targetId = data["target_id"]
        val senderId = data["sender_id"]
        val senderName = data["sender_name"]
        val senderAvatar = data["sender_avatar"]
        
        Log.d(TAG, "📬 Tipo: $notificationType, title=$title, body=${body.take(30)}, sender=$senderName")
        
        // Mostrar notificación según tipo
        when (notificationType) {
            "message" -> {
                // Solo mostrar notificación push si la app está en background/cerrada
                // En foreground, el sonido de ChatRepository ya avisa al usuario
                if (!isAppInForeground()) {
                    showMessageNotification(
                        title = senderName ?: title,
                        body = body,
                        chatId = targetId,
                        senderAvatar = senderAvatar
                    )
                }
            }
            "like" -> showSocialNotification(
                title = "Nuevo like ❤️",
                body = "$senderName le dio like a tu publicación",
                postId = targetId
            )
            "save" -> showSocialNotification(
                title = "Guardaron tu publicación 🔖",
                body = "$senderName guardó tu publicación",
                postId = targetId
            )
            "comment" -> showSocialNotification(
                title = "Nueva opinión 💬",
                body = "$senderName: $body",
                postId = targetId
            )
            "follow" -> showSocialNotification(
                title = "Nuevo seguidor",
                body = "$senderName empezó a seguirte",
                userId = senderId
            )
            "sale" -> showTransactionNotification(
                title = "Nueva venta",
                body = body,
                transactionId = targetId
            )
            "handshake" -> showTransactionNotification(
                title = "Handshake",
                body = body,
                transactionId = targetId
            )
            "mention" -> showSocialNotification(
                title = "Te mencionaron",
                body = "$senderName te mencionó: $body",
                postId = targetId
            )
            "client_request" -> showSocialNotification(
                title = "Nueva solicitud de cliente",
                body = body,
                userId = senderId
            )
            "client_accepted" -> showSocialNotification(
                title = "Solicitud aceptada ✅",
                body = body,
                userId = senderId
            )
            "call" -> showCallNotification(
                callerName = data["caller_name"] ?: senderName ?: "Usuario",
                callId = data["call_id"],
                callType = data["call_type"] ?: "voice",
                callerAvatar = data["caller_avatar"]
            )
            "promotion" -> showPromotionNotification(
                title = title,
                body = body,
                imageUrl = imageUrl
            )
            else -> showGeneralNotification(title, body, imageUrl)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // NOTIFICATION BUILDERS
    // ═══════════════════════════════════════════════════════════════════════════════
    
    private fun showMessageNotification(
        title: String,
        body: String,
        chatId: String?,
        senderAvatar: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "chat")
            putExtra("chat_id", chatId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val isForeground = isAppInForeground()
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setContentIntent(pendingIntent)
        
        // En background: usar sonido del sistema. En foreground: sonido custom
        if (!isForeground) {
            notificationBuilder.setSound(defaultSound)
        }
        
        // Cargar avatar si está disponible
        senderAvatar?.let { url ->
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                notificationBuilder.setLargeIcon(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando avatar: ${e.message}")
            }
        }
        
        showNotification(NOTIFICATION_ID_MESSAGE + (chatId?.hashCode() ?: 0), notificationBuilder)
        playCustomSound(isMessage = true)
    }
    
    private fun showSocialNotification(
        title: String,
        body: String,
        postId: String? = null,
        userId: String? = null
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (postId != null) {
                putExtra("open_screen", "post")
                putExtra("post_id", postId)
            } else if (userId != null) {
                putExtra("open_screen", "profile")
                putExtra("user_id", userId)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val isForeground = isAppInForeground()
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_SOCIAL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setContentIntent(pendingIntent)
        
        if (!isForeground) {
            notificationBuilder.setSound(defaultSound)
            notificationBuilder.setVibrate(longArrayOf(0, 200, 100, 200))
        }
        
        val notificationId = NOTIFICATION_ID_SOCIAL + (postId?.hashCode() ?: userId?.hashCode() ?: 0)
        showNotification(notificationId, notificationBuilder)
        playCustomSound(isMessage = false)
    }
    
    private fun showTransactionNotification(
        title: String,
        body: String,
        transactionId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "transactions")
            putExtra("transaction_id", transactionId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_TRANSACTIONS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setSound(null) // Sonido manejado por el canal
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setContentIntent(pendingIntent)
        
        showNotification(NOTIFICATION_ID_TRANSACTION + (transactionId?.hashCode() ?: 0), notificationBuilder)
        playCustomSound(isMessage = false) // Sonido personalizado solo en primer plano
    }
    
    private fun showPromotionNotification(
        title: String,
        body: String,
        imageUrl: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "promotions")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_PROMOTIONS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROMO)
            .setContentIntent(pendingIntent)
        
        // Cargar imagen grande si está disponible
        imageUrl?.let { url ->
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as android.graphics.Bitmap?)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando imagen: ${e.message}")
            }
        }
        
        showNotification(NOTIFICATION_ID_PROMOTION + System.currentTimeMillis().toInt(), notificationBuilder)
    }
    
    private fun showCallNotification(
        callerName: String,
        callId: String?,
        callType: String,
        callerAvatar: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "call")
            putExtra("call_id", callId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val callTypeText = if (callType == "video") "Videollamada" else "Llamada de voz"
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("$callerName te está llamando")
            .setContentText(callTypeText)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setSound(ringtoneUri)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500))
            .setOngoing(true)
            .setTimeoutAfter(60_000)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
        
        callerAvatar?.let { url ->
            if (url.isNotEmpty()) {
                try {
                    val fullUrl = if (url.startsWith("http")) url 
                        else "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/$url"
                    val bitmap = BitmapFactory.decodeStream(URL(fullUrl).openStream())
                    notificationBuilder.setLargeIcon(bitmap)
                } catch (e: Exception) {
                    Log.e(TAG, "Error cargando avatar caller: ${e.message}")
                }
            }
        }
        
        val notificationId = 5000 + (callId?.hashCode() ?: 0)
        showNotification(notificationId, notificationBuilder)
    }
    
    private fun showGeneralNotification(
        title: String,
        body: String,
        imageUrl: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_SYSTEM)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
        
        showNotification(System.currentTimeMillis().toInt(), notificationBuilder)
    }
    
    private fun showNotification(id: Int, builder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // NOTIFICATION CHANNELS
    // ═══════════════════════════════════════════════════════════════════════════════
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                .build()
            
            // Canal de mensajes (alta prioridad)
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de mensajes de chat"
                setSound(defaultSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            
            // Canal social (prioridad alta para que lleguen en background)
            val socialChannel = NotificationChannel(
                CHANNEL_SOCIAL,
                "Social",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Likes, comentarios, seguidores y menciones"
                setSound(defaultSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }
            
            // Canal de transacciones (alta prioridad)
            val transactionsChannel = NotificationChannel(
                CHANNEL_TRANSACTIONS,
                "Ventas y Compras",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ventas, compras y handshakes"
                setSound(defaultSoundUri, audioAttributes)
                enableVibration(true)
            }
            
            // Canal de promociones (baja prioridad)
            val promotionsChannel = NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promociones",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ofertas especiales y promociones"
            }
            
            // Canal del sistema
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "Sistema",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones del sistema"
            }
            
            // Canal de llamadas (máxima prioridad - fullscreen intent)
            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Llamadas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Llamadas de voz y video entrantes"
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                setSound(ringtoneUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            
            notificationManager.createNotificationChannels(
                listOf(messagesChannel, socialChannel, transactionsChannel, promotionsChannel, systemChannel, callsChannel)
            )
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // SUPABASE TOKEN MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════════
    
    private suspend fun saveFcmToken(userId: String, token: String) {
        try {
            SupabaseClient.database
                .from("fcm_tokens")
                .upsert(mapOf(
                    "user_id" to userId,
                    "token" to token,
                    "device_info" to "${Build.MANUFACTURER} ${Build.MODEL}",
                    "platform" to "android",
                    "app_version" to "1.0.0",
                    "is_active" to true,
                    "updated_at" to java.time.Instant.now().toString()
                ))
            Log.d(TAG, "✅ FCM Token guardado en Supabase")
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando token en Supabase: ${e.message}")
        }
    }
}
