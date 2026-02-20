package com.rendly.app.data.cache.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Objects for cache entities.
 * Optimized queries with Flow support for reactive updates.
 */

// ═══════════════════════════════════════════════════════════════════════════
// USER DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedUserDao {
    
    @Query("SELECT * FROM cached_users WHERE user_id = :userId")
    suspend fun getById(userId: String): CachedUserEntity?
    
    @Query("SELECT * FROM cached_users WHERE user_id = :userId")
    fun observeById(userId: String): Flow<CachedUserEntity?>
    
    @Query("SELECT * FROM cached_users WHERE user_id IN (:userIds)")
    suspend fun getByIds(userIds: List<String>): List<CachedUserEntity>
    
    @Query("SELECT * FROM cached_users WHERE username LIKE '%' || :query || '%' LIMIT :limit")
    suspend fun searchByUsername(query: String, limit: Int = 20): List<CachedUserEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: CachedUserEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<CachedUserEntity>)
    
    @Query("DELETE FROM cached_users WHERE user_id = :userId")
    suspend fun delete(userId: String)
    
    @Query("DELETE FROM cached_users WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)
    
    @Query("DELETE FROM cached_users")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM cached_users")
    suspend fun count(): Int
}

// ═══════════════════════════════════════════════════════════════════════════
// POST DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedPostDao {
    
    @Query("SELECT * FROM cached_posts WHERE id = :postId")
    suspend fun getById(postId: String): CachedPostEntity?
    
    @Query("SELECT * FROM cached_posts WHERE id = :postId")
    fun observeById(postId: String): Flow<CachedPostEntity?>
    
    @Query("""
        SELECT * FROM cached_posts 
        WHERE status = 'active' 
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFeed(limit: Int = 20, offset: Int = 0): List<CachedPostEntity>
    
    @Query("""
        SELECT * FROM cached_posts 
        WHERE status = 'active' 
        ORDER BY created_at DESC
    """)
    fun observeFeed(): Flow<List<CachedPostEntity>>
    
    @Query("""
        SELECT * FROM cached_posts 
        WHERE user_id = :userId AND status = 'active'
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getByUserId(userId: String, limit: Int = 20, offset: Int = 0): List<CachedPostEntity>
    
    @Query("""
        SELECT * FROM cached_posts 
        WHERE user_id = :userId AND status = 'active'
        ORDER BY created_at DESC
    """)
    fun observeByUserId(userId: String): Flow<List<CachedPostEntity>>
    
    @Query("SELECT * FROM cached_posts WHERE id IN (:postIds)")
    suspend fun getByIds(postIds: List<String>): List<CachedPostEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: CachedPostEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(posts: List<CachedPostEntity>)
    
    @Query("UPDATE cached_posts SET likes_count = :count WHERE id = :postId")
    suspend fun updateLikesCount(postId: String, count: Int)
    
    @Query("UPDATE cached_posts SET saves_count = :count WHERE id = :postId")
    suspend fun updateSavesCount(postId: String, count: Int)
    
    @Query("DELETE FROM cached_posts WHERE id = :postId")
    suspend fun delete(postId: String)
    
    @Query("DELETE FROM cached_posts WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: String)
    
    @Query("DELETE FROM cached_posts WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)
    
    @Query("DELETE FROM cached_posts")
    suspend fun deleteAll()
    
    @Query("SELECT MAX(version) FROM cached_posts")
    suspend fun getLatestVersion(): Long?
    
    @Query("SELECT COUNT(*) FROM cached_posts")
    suspend fun count(): Int
}

// ═══════════════════════════════════════════════════════════════════════════
// REND DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedRendDao {
    
    @Query("SELECT * FROM cached_rends WHERE id = :rendId")
    suspend fun getById(rendId: String): CachedRendEntity?
    
    @Query("SELECT * FROM cached_rends WHERE id = :rendId")
    fun observeById(rendId: String): Flow<CachedRendEntity?>
    
    @Query("""
        SELECT * FROM cached_rends 
        WHERE status = 'active' 
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFeed(limit: Int = 20, offset: Int = 0): List<CachedRendEntity>
    
    @Query("""
        SELECT * FROM cached_rends 
        WHERE status = 'active' 
        ORDER BY created_at DESC
    """)
    fun observeFeed(): Flow<List<CachedRendEntity>>
    
    @Query("""
        SELECT * FROM cached_rends 
        WHERE user_id = :userId AND status = 'active'
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getByUserId(userId: String, limit: Int = 20, offset: Int = 0): List<CachedRendEntity>
    
    @Query("""
        SELECT * FROM cached_rends 
        WHERE user_id = :userId AND status = 'active'
        ORDER BY created_at DESC
    """)
    fun observeByUserId(userId: String): Flow<List<CachedRendEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rend: CachedRendEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rends: List<CachedRendEntity>)
    
    @Query("UPDATE cached_rends SET likes_count = :count WHERE id = :rendId")
    suspend fun updateLikesCount(rendId: String, count: Int)
    
    @Query("UPDATE cached_rends SET views_count = :count WHERE id = :rendId")
    suspend fun updateViewsCount(rendId: String, count: Int)
    
    @Query("DELETE FROM cached_rends WHERE id = :rendId")
    suspend fun delete(rendId: String)
    
    @Query("DELETE FROM cached_rends WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)
    
    @Query("DELETE FROM cached_rends")
    suspend fun deleteAll()
    
    @Query("SELECT MAX(version) FROM cached_rends")
    suspend fun getLatestVersion(): Long?
    
    @Query("SELECT COUNT(*) FROM cached_rends")
    suspend fun count(): Int
}

// ═══════════════════════════════════════════════════════════════════════════
// STORY DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedStoryDao {
    
    @Query("SELECT * FROM cached_stories WHERE id = :storyId")
    suspend fun getById(storyId: String): CachedStoryEntity?
    
    @Query("""
        SELECT * FROM cached_stories 
        WHERE user_id = :userId 
        ORDER BY created_at DESC
    """)
    suspend fun getByUserId(userId: String): List<CachedStoryEntity>
    
    @Query("""
        SELECT * FROM cached_stories 
        ORDER BY created_at DESC
    """)
    fun observeAll(): Flow<List<CachedStoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(story: CachedStoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stories: List<CachedStoryEntity>)
    
    @Query("DELETE FROM cached_stories WHERE id = :storyId")
    suspend fun delete(storyId: String)
    
    @Query("DELETE FROM cached_stories WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)
    
    @Query("DELETE FROM cached_stories")
    suspend fun deleteAll()
}

// ═══════════════════════════════════════════════════════════════════════════
// MESSAGE DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedMessageDao {
    
    @Query("SELECT * FROM cached_messages WHERE id = :messageId")
    suspend fun getById(messageId: String): CachedMessageEntity?
    
    @Query("""
        SELECT * FROM cached_messages 
        WHERE conversation_id = :conversationId 
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getByConversation(
        conversationId: String, 
        limit: Int = 50, 
        offset: Int = 0
    ): List<CachedMessageEntity>
    
    @Query("""
        SELECT * FROM cached_messages 
        WHERE conversation_id = :conversationId 
        ORDER BY created_at ASC
    """)
    fun observeByConversation(conversationId: String): Flow<List<CachedMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: CachedMessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<CachedMessageEntity>)
    
    @Query("UPDATE cached_messages SET is_read = 1 WHERE conversation_id = :conversationId")
    suspend fun markAllRead(conversationId: String)
    
    @Query("DELETE FROM cached_messages WHERE id = :messageId")
    suspend fun delete(messageId: String)
    
    @Query("DELETE FROM cached_messages WHERE conversation_id = :conversationId")
    suspend fun deleteByConversation(conversationId: String)
    
    @Query("DELETE FROM cached_messages WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)
    
    @Query("DELETE FROM cached_messages")
    suspend fun deleteAll()
}

// ═══════════════════════════════════════════════════════════════════════════
// CONVERSATION DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedConversationDao {
    
    @Query("SELECT * FROM cached_conversations WHERE id = :conversationId")
    suspend fun getById(conversationId: String): CachedConversationEntity?
    
    @Query("""
        SELECT * FROM cached_conversations 
        WHERE user_id = :userId 
        ORDER BY last_message_at DESC
    """)
    suspend fun getByUserId(userId: String): List<CachedConversationEntity>
    
    @Query("""
        SELECT * FROM cached_conversations 
        WHERE user_id = :userId 
        ORDER BY last_message_at DESC
    """)
    fun observeByUserId(userId: String): Flow<List<CachedConversationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(conversation: CachedConversationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(conversations: List<CachedConversationEntity>)
    
    @Query("UPDATE cached_conversations SET unread_count = 0 WHERE id = :conversationId")
    suspend fun markAsRead(conversationId: String)
    
    @Query("DELETE FROM cached_conversations WHERE id = :conversationId")
    suspend fun delete(conversationId: String)
    
    @Query("DELETE FROM cached_conversations")
    suspend fun deleteAll()
}

// ═══════════════════════════════════════════════════════════════════════════
// NOTIFICATION DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedNotificationDao {
    
    @Query("""
        SELECT * FROM cached_notifications 
        WHERE user_id = :userId 
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getByUserId(userId: String, limit: Int = 50, offset: Int = 0): List<CachedNotificationEntity>
    
    @Query("""
        SELECT * FROM cached_notifications 
        WHERE user_id = :userId 
        ORDER BY created_at DESC
    """)
    fun observeByUserId(userId: String): Flow<List<CachedNotificationEntity>>
    
    @Query("SELECT COUNT(*) FROM cached_notifications WHERE user_id = :userId AND is_read = 0")
    suspend fun getUnreadCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM cached_notifications WHERE user_id = :userId AND is_read = 0")
    fun observeUnreadCount(userId: String): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(notification: CachedNotificationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(notifications: List<CachedNotificationEntity>)
    
    @Query("UPDATE cached_notifications SET is_read = 1 WHERE user_id = :userId")
    suspend fun markAllRead(userId: String)
    
    @Query("UPDATE cached_notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)
    
    @Query("DELETE FROM cached_notifications WHERE id = :notificationId")
    suspend fun delete(notificationId: String)
    
    @Query("DELETE FROM cached_notifications WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)
    
    @Query("DELETE FROM cached_notifications")
    suspend fun deleteAll()
}

// ═══════════════════════════════════════════════════════════════════════════
// FOLLOW DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CachedFollowDao {
    
    @Query("SELECT * FROM cached_follows WHERE follower_id = :userId")
    suspend fun getFollowing(userId: String): List<CachedFollowEntity>
    
    @Query("SELECT * FROM cached_follows WHERE following_id = :userId")
    suspend fun getFollowers(userId: String): List<CachedFollowEntity>
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM cached_follows 
            WHERE follower_id = :followerId AND following_id = :followingId
        )
    """)
    suspend fun isFollowing(followerId: String, followingId: String): Boolean
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM cached_follows 
            WHERE follower_id = :followerId AND following_id = :followingId
        )
    """)
    fun observeIsFollowing(followerId: String, followingId: String): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(follow: CachedFollowEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(follows: List<CachedFollowEntity>)
    
    @Query("DELETE FROM cached_follows WHERE follower_id = :followerId AND following_id = :followingId")
    suspend fun delete(followerId: String, followingId: String)
    
    @Query("DELETE FROM cached_follows WHERE follower_id = :userId OR following_id = :userId")
    suspend fun deleteByUser(userId: String)
    
    @Query("DELETE FROM cached_follows")
    suspend fun deleteAll()
}

// ═══════════════════════════════════════════════════════════════════════════
// CACHE SYNC METADATA DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface CacheSyncMetadataDao {
    
    @Query("SELECT * FROM cache_sync_metadata WHERE cache_key = :cacheKey")
    suspend fun get(cacheKey: String): CacheSyncMetadataEntity?
    
    @Query("SELECT last_sync_at FROM cache_sync_metadata WHERE cache_key = :cacheKey")
    suspend fun getLastSyncTime(cacheKey: String): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: CacheSyncMetadataEntity)
    
    @Query("DELETE FROM cache_sync_metadata WHERE cache_key = :cacheKey")
    suspend fun delete(cacheKey: String)
    
    @Query("DELETE FROM cache_sync_metadata WHERE cache_key LIKE :pattern")
    suspend fun deleteMatching(pattern: String)
    
    @Query("DELETE FROM cache_sync_metadata")
    suspend fun deleteAll()
}

// ═══════════════════════════════════════════════════════════════════════════
// PENDING OPERATIONS DAO
// ═══════════════════════════════════════════════════════════════════════════

@Dao
interface PendingOperationDao {
    
    @Query("SELECT * FROM pending_operations WHERE status = 'pending' ORDER BY created_at ASC")
    suspend fun getPending(): List<PendingOperationEntity>
    
    @Query("SELECT * FROM pending_operations WHERE status = 'pending' ORDER BY created_at ASC")
    fun observePending(): Flow<List<PendingOperationEntity>>
    
    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'pending'")
    suspend fun getPendingCount(): Int
    
    @Insert
    suspend fun insert(operation: PendingOperationEntity): Long
    
    @Update
    suspend fun update(operation: PendingOperationEntity)
    
    @Query("UPDATE pending_operations SET status = :status, last_attempt_at = :attemptAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, attemptAt: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE pending_operations 
        SET status = :status, retry_count = retry_count + 1, 
            error_message = :errorMessage, last_attempt_at = :attemptAt 
        WHERE id = :id
    """)
    suspend fun markFailed(
        id: Long, 
        status: String = "error", 
        errorMessage: String?, 
        attemptAt: Long = System.currentTimeMillis()
    )
    
    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun delete(id: Long)
    
    @Query("DELETE FROM pending_operations WHERE status = 'success'")
    suspend fun deleteCompleted()
    
    @Query("DELETE FROM pending_operations WHERE status = 'error' AND retry_count >= :maxRetries")
    suspend fun deleteFailedAfterRetries(maxRetries: Int = 3)
    
    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()
}
