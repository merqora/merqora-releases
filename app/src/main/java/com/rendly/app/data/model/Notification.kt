package com.rendly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDB(
    val id: String = "",
    @SerialName("recipient_id")
    val recipientId: String = "",
    @SerialName("sender_id")
    val senderId: String = "",
    @SerialName("sender_username")
    val senderUsername: String = "",
    @SerialName("sender_avatar")
    val senderAvatar: String? = null,
    val type: String = "",
    @SerialName("post_id")
    val postId: String? = null,
    @SerialName("post_image")
    val postImage: String? = null,
    val message: String? = null,
    @SerialName("extra_data")
    val extraData: String? = null,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = ""
)

data class Notification(
    val id: String,
    val recipientId: String,
    val senderId: String,
    val senderUsername: String,
    val senderAvatar: String?,
    val type: NotificationType,
    val postId: String?,
    val postImage: String?,
    val message: String?,
    val extraData: String?,
    val isRead: Boolean,
    val createdAt: String
) {
    companion object {
        fun fromDB(db: NotificationDB): Notification {
            return Notification(
                id = db.id,
                recipientId = db.recipientId,
                senderId = db.senderId,
                senderUsername = db.senderUsername,
                senderAvatar = db.senderAvatar,
                type = NotificationType.fromString(db.type),
                postId = db.postId,
                postImage = db.postImage,
                message = db.message,
                extraData = db.extraData,
                isRead = db.isRead,
                createdAt = db.createdAt
            )
        }
    }
}

enum class NotificationType(val value: String) {
    LIKE("like"),
    SAVE("save"),
    FOLLOW("follow"),
    COMMENT("comment"),
    MENTION("mention"),
    CLIENT_REQUEST("client_request"),
    CLIENT_ACCEPTED("client_accepted"),
    CLIENT_REJECTED("client_rejected"),
    CLIENT_PENDING("client_pending"),
    UNKNOWN("unknown");
    
    companion object {
        fun fromString(value: String): NotificationType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}
