package com.rendly.app.data.cache.db

import androidx.room.*

/**
 * Room entities for disk cache layer.
 * Normalized structure with proper indices for query performance.
 * 
 * Design principles:
 * - Each entity has cached_at for TTL checks
 * - version field for change detection
 * - Indices on frequently queried columns
 * - Relationships via foreign keys where appropriate
 */

// ═══════════════════════════════════════════════════════════════════════════
// USER ENTITY
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_users",
    indices = [
        Index(value = ["username"]),
        Index(value = ["cached_at"])
    ]
)
data class CachedUserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val username: String,
    
    @ColumnInfo(name = "nombre_tienda")
    val nombreTienda: String? = null,
    
    val descripcion: String? = null,
    
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,
    
    @ColumnInfo(name = "banner_url")
    val bannerUrl: String? = null,
    
    val email: String? = null,
    val genero: String? = null,
    val ubicacion: String? = null,
    val nombre: String? = null,
    
    // Social links
    val facebook: String? = null,
    val whatsapp: String? = null,
    val twitter: String? = null,
    val instagram: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    
    // Status
    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = false,
    
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    
    @ColumnInfo(name = "tiene_tienda")
    val tieneTienda: Boolean = false,
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
    
    val version: Long = 0,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════
// POST ENTITY
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_posts",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["created_at"]),
        Index(value = ["cached_at"]),
        Index(value = ["status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CachedUserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CachedPostEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val category: String? = null,
    val condition: String? = null,
    
    // Stored as JSON string
    val images: String = "[]",
    val tags: String? = null,
    
    val status: String = "active",
    
    // Counts
    @ColumnInfo(name = "likes_count")
    val likesCount: Int = 0,
    
    @ColumnInfo(name = "reviews_count")
    val reviewsCount: Int = 0,
    
    @ColumnInfo(name = "views_count")
    val viewsCount: Int = 0,
    
    @ColumnInfo(name = "shares_count")
    val sharesCount: Int = 0,
    
    @ColumnInfo(name = "saves_count")
    val savesCount: Int = 0,
    
    // Product linking
    @ColumnInfo(name = "product_id")
    val productId: String? = null,
    
    // Timestamps
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String = "",
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
    
    val version: Long = 0
)

// ═══════════════════════════════════════════════════════════════════════════
// REND ENTITY (Short Videos)
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_rends",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["created_at"]),
        Index(value = ["cached_at"]),
        Index(value = ["status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CachedUserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CachedRendEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val title: String = "",
    val description: String? = null,
    
    @ColumnInfo(name = "video_url")
    val videoUrl: String,
    
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @ColumnInfo(name = "product_title")
    val productTitle: String? = null,
    
    @ColumnInfo(name = "product_price")
    val productPrice: Double? = null,
    
    @ColumnInfo(name = "product_link")
    val productLink: String? = null,
    
    @ColumnInfo(name = "product_image")
    val productImage: String? = null,
    
    @ColumnInfo(name = "product_id")
    val productId: String? = null,
    
    val duration: Int = 0,
    val status: String = "active",
    
    // Counts
    @ColumnInfo(name = "likes_count")
    val likesCount: Int = 0,
    
    @ColumnInfo(name = "reviews_count")
    val reviewsCount: Int = 0,
    
    @ColumnInfo(name = "views_count")
    val viewsCount: Int = 0,
    
    @ColumnInfo(name = "shares_count")
    val sharesCount: Int = 0,
    
    @ColumnInfo(name = "saves_count")
    val savesCount: Int = 0,
    
    // Timestamps
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String = "",
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
    
    val version: Long = 0
)

// ═══════════════════════════════════════════════════════════════════════════
// STORY ENTITY
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_stories",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["created_at"]),
        Index(value = ["expires_at"]),
        Index(value = ["cached_at"])
    ]
)
data class CachedStoryEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "media_url")
    val mediaUrl: String,
    
    @ColumnInfo(name = "media_type")
    val mediaType: String = "image",
    
    val caption: String? = null,
    
    @ColumnInfo(name = "views_count")
    val viewsCount: Int = 0,
    
    @ColumnInfo(name = "likes_count")
    val likesCount: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: String? = null,
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
    
    val version: Long = 0
)

// ═══════════════════════════════════════════════════════════════════════════
// MESSAGE ENTITY
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_messages",
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["sender_id"]),
        Index(value = ["created_at"]),
        Index(value = ["cached_at"])
    ]
)
data class CachedMessageEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    
    @ColumnInfo(name = "sender_id")
    val senderId: String,
    
    @ColumnInfo(name = "receiver_id")
    val receiverId: String,
    
    val content: String,
    
    @ColumnInfo(name = "message_type")
    val messageType: String = "text",
    
    @ColumnInfo(name = "media_url")
    val mediaUrl: String? = null,
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
    
    val version: Long = 0
)

// ═══════════════════════════════════════════════════════════════════════════
// CONVERSATION ENTITY
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_conversations",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["other_user_id"]),
        Index(value = ["last_message_at"]),
        Index(value = ["cached_at"])
    ]
)
data class CachedConversationEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "other_user_id")
    val otherUserId: String,
    
    @ColumnInfo(name = "last_message")
    val lastMessage: String? = null,
    
    @ColumnInfo(name = "last_message_at")
    val lastMessageAt: String? = null,
    
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
    
    val version: Long = 0
)

// ═══════════════════════════════════════════════════════════════════════════
// NOTIFICATION ENTITY
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_notifications",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["created_at"]),
        Index(value = ["is_read"]),
        Index(value = ["cached_at"])
    ]
)
data class CachedNotificationEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    val type: String,
    val title: String,
    val body: String,
    
    @ColumnInfo(name = "action_url")
    val actionUrl: String? = null,
    
    @ColumnInfo(name = "actor_id")
    val actorId: String? = null,
    
    @ColumnInfo(name = "entity_id")
    val entityId: String? = null,
    
    @ColumnInfo(name = "entity_type")
    val entityType: String? = null,
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
    
    val version: Long = 0
)

// ═══════════════════════════════════════════════════════════════════════════
// FOLLOW RELATIONSHIP ENTITY
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_follows",
    primaryKeys = ["follower_id", "following_id"],
    indices = [
        Index(value = ["follower_id"]),
        Index(value = ["following_id"]),
        Index(value = ["cached_at"])
    ]
)
data class CachedFollowEntity(
    @ColumnInfo(name = "follower_id")
    val followerId: String,
    
    @ColumnInfo(name = "following_id")
    val followingId: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",
    
    // Cache metadata
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════════════════════════════════════
// CACHE SYNC METADATA - Tracks last sync times per cache key
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cache_sync_metadata",
    indices = [
        Index(value = ["last_sync_at"])
    ]
)
data class CacheSyncMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    val cacheKey: String,
    
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_version")
    val lastVersion: Long = 0,
    
    val etag: String? = null,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "success" // success, pending, error
)

// ═══════════════════════════════════════════════════════════════════════════
// PENDING OPERATIONS - For offline-first write support
// ═══════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "pending_operations",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["status"])
    ]
)
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String, // create, update, delete
    
    @ColumnInfo(name = "entity_type")
    val entityType: String, // post, rend, message, etc.
    
    @ColumnInfo(name = "entity_id")
    val entityId: String? = null,
    
    // JSON payload of the operation
    val payload: String,
    
    val status: String = "pending", // pending, syncing, success, error
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null
)
