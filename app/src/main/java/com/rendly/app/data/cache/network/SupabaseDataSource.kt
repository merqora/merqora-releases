package com.rendly.app.data.cache.network

import android.util.Log
import com.rendly.app.data.model.*
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Network data source wrapping Supabase client.
 * Provides clean async API with request deduplication.
 * 
 * All network calls go through RequestDeduplicator to prevent
 * redundant requests when multiple UI components request the same data.
 */
object SupabaseDataSource {
    private const val TAG = "SupabaseDataSource"
    private val deduplicator = RequestDeduplicator("SupabaseDS")
    
    // ═══════════════════════════════════════════════════════════════════════
    // USERS
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun fetchUser(userId: String): Usuario? = withContext(Dispatchers.IO) {
        deduplicator.dedupe("user:$userId") {
            try {
                Log.d(TAG, "Fetching user: $userId")
                SupabaseClient.database
                    .from("usuarios")
                    .select {
                        filter { eq("user_id", userId) }
                    }
                    .decodeSingleOrNull<Usuario>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user $userId: ${e.message}")
                throw e
            }
        }
    }
    
    suspend fun fetchUsers(userIds: List<String>): List<Usuario> = withContext(Dispatchers.IO) {
        if (userIds.isEmpty()) return@withContext emptyList()
        
        val key = "users:${userIds.sorted().hashCode()}"
        deduplicator.dedupe(key) {
            try {
                Log.d(TAG, "Fetching ${userIds.size} users")
                SupabaseClient.database
                    .from("usuarios")
                    .select {
                        filter { isIn("user_id", userIds) }
                    }
                    .decodeList<Usuario>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching users: ${e.message}")
                throw e
            }
        } ?: emptyList()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // POSTS
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun fetchPosts(
        limit: Int = 20,
        offset: Int = 0,
        userId: String? = null
    ): List<PostDB> = withContext(Dispatchers.IO) {
        val key = "posts:${userId ?: "feed"}:$offset:$limit"
        deduplicator.dedupe(key) {
            try {
                Log.d(TAG, "Fetching posts (userId=$userId, limit=$limit, offset=$offset)")
                val query = SupabaseClient.database
                    .from("posts")
                    .select {
                        filter { 
                            eq("status", "active")
                            if (userId != null) {
                                eq("user_id", userId)
                            }
                        }
                        order("created_at", Order.DESCENDING)
                        limit(limit.toLong())
                        // Note: Supabase uses range for offset
                        if (offset > 0) {
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                    }
                query.decodeList<PostDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching posts: ${e.message}")
                throw e
            }
        } ?: emptyList()
    }
    
    suspend fun fetchPost(postId: String): PostDB? = withContext(Dispatchers.IO) {
        deduplicator.dedupe("post:$postId") {
            try {
                Log.d(TAG, "Fetching post: $postId")
                SupabaseClient.database
                    .from("posts")
                    .select {
                        filter { eq("id", postId) }
                    }
                    .decodeSingleOrNull<PostDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching post $postId: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Fetch posts with user data in a single optimized query
     */
    suspend fun fetchPostsWithUsers(
        limit: Int = 20,
        offset: Int = 0,
        userId: String? = null
    ): List<Pair<PostDB, Usuario?>> = withContext(Dispatchers.IO) {
        try {
            val posts = fetchPosts(limit, offset, userId)
            if (posts.isEmpty()) return@withContext emptyList()
            
            val userIds = posts.map { it.userId }.distinct()
            val users = fetchUsers(userIds).associateBy { it.userId }
            
            posts.map { post -> post to users[post.userId] }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching posts with users: ${e.message}")
            throw e
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // RENDS (Short Videos)
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun fetchRends(
        limit: Int = 20,
        offset: Int = 0,
        userId: String? = null
    ): List<RendDB> = withContext(Dispatchers.IO) {
        val key = "rends:${userId ?: "feed"}:$offset:$limit"
        deduplicator.dedupe(key) {
            try {
                Log.d(TAG, "Fetching rends (userId=$userId, limit=$limit, offset=$offset)")
                val query = SupabaseClient.database
                    .from("rends")
                    .select {
                        filter { 
                            eq("status", "active")
                            if (userId != null) {
                                eq("user_id", userId)
                            }
                        }
                        order("created_at", Order.DESCENDING)
                        limit(limit.toLong())
                        if (offset > 0) {
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                    }
                query.decodeList<RendDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rends: ${e.message}")
                throw e
            }
        } ?: emptyList()
    }
    
    suspend fun fetchRend(rendId: String): RendDB? = withContext(Dispatchers.IO) {
        deduplicator.dedupe("rend:$rendId") {
            try {
                Log.d(TAG, "Fetching rend: $rendId")
                SupabaseClient.database
                    .from("rends")
                    .select {
                        filter { eq("id", rendId) }
                    }
                    .decodeSingleOrNull<RendDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rend $rendId: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Fetch rends with user data in a single optimized query
     */
    suspend fun fetchRendsWithUsers(
        limit: Int = 20,
        offset: Int = 0,
        userId: String? = null
    ): List<Pair<RendDB, Usuario?>> = withContext(Dispatchers.IO) {
        try {
            val rends = fetchRends(limit, offset, userId)
            if (rends.isEmpty()) return@withContext emptyList()
            
            val userIds = rends.map { it.userId }.distinct()
            val users = fetchUsers(userIds).associateBy { it.userId }
            
            rends.map { rend -> rend to users[rend.userId] }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching rends with users: ${e.message}")
            throw e
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // STORIES
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun fetchStories(userId: String? = null): List<StoryDB> = withContext(Dispatchers.IO) {
        val key = "stories:${userId ?: "feed"}"
        deduplicator.dedupe(key) {
            try {
                Log.d(TAG, "Fetching stories (userId=$userId)")
                SupabaseClient.database
                    .from("stories")
                    .select {
                        if (userId != null) {
                            filter { eq("user_id", userId) }
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<StoryDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching stories: ${e.message}")
                throw e
            }
        } ?: emptyList()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun fetchNotifications(
        userId: String,
        limit: Int = 50,
        offset: Int = 0
    ): List<NotificationDB> = withContext(Dispatchers.IO) {
        val key = "notifications:$userId:$offset:$limit"
        deduplicator.dedupe(key) {
            try {
                Log.d(TAG, "Fetching notifications for $userId")
                SupabaseClient.database
                    .from("notifications")
                    .select {
                        filter { eq("user_id", userId) }
                        order("created_at", Order.DESCENDING)
                        limit(limit.toLong())
                        if (offset > 0) {
                            range(offset.toLong(), (offset + limit - 1).toLong())
                        }
                    }
                    .decodeList<NotificationDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching notifications: ${e.message}")
                throw e
            }
        } ?: emptyList()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // FOLLOWERS
    // ═══════════════════════════════════════════════════════════════════════
    
    suspend fun fetchFollowers(userId: String): List<FollowerDB> = withContext(Dispatchers.IO) {
        deduplicator.dedupe("followers:$userId") {
            try {
                Log.d(TAG, "Fetching followers for $userId")
                SupabaseClient.database
                    .from("followers")
                    .select {
                        filter { eq("following_id", userId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<FollowerDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching followers: ${e.message}")
                throw e
            }
        } ?: emptyList()
    }
    
    suspend fun fetchFollowing(userId: String): List<FollowerDB> = withContext(Dispatchers.IO) {
        deduplicator.dedupe("following:$userId") {
            try {
                Log.d(TAG, "Fetching following for $userId")
                SupabaseClient.database
                    .from("followers")
                    .select {
                        filter { eq("follower_id", userId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<FollowerDB>()
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching following: ${e.message}")
                throw e
            }
        } ?: emptyList()
    }
    
    suspend fun isFollowing(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.database
                .from("followers")
                .select {
                    filter { 
                        eq("follower_id", followerId)
                        eq("following_id", followingId)
                    }
                }
                .decodeList<FollowerDB>()
            result.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking follow status: ${e.message}")
            false
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // UTILITY
    // ═══════════════════════════════════════════════════════════════════════
    
    fun cancelAllRequests() {
        deduplicator.cancelAll()
    }
    
    fun inFlightCount(): Int = deduplicator.inFlightCount()
}

// ═══════════════════════════════════════════════════════════════════════════
// NETWORK MODEL EXTENSIONS (for missing DB models)
// ═══════════════════════════════════════════════════════════════════════════

@kotlinx.serialization.Serializable
data class StoryDB(
    val id: String = "",
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("media_url") val mediaUrl: String,
    @kotlinx.serialization.SerialName("media_type") val mediaType: String = "image",
    val caption: String? = null,
    @kotlinx.serialization.SerialName("views_count") val viewsCount: Int = 0,
    @kotlinx.serialization.SerialName("likes_count") val likesCount: Int = 0,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String = "",
    @kotlinx.serialization.SerialName("expires_at") val expiresAt: String? = null,
    @kotlinx.serialization.SerialName("updated_at") val updatedAt: String = ""
)

@kotlinx.serialization.Serializable
data class NotificationDB(
    val id: String = "",
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    val type: String,
    val title: String,
    val body: String,
    @kotlinx.serialization.SerialName("action_url") val actionUrl: String? = null,
    @kotlinx.serialization.SerialName("actor_id") val actorId: String? = null,
    @kotlinx.serialization.SerialName("entity_id") val entityId: String? = null,
    @kotlinx.serialization.SerialName("entity_type") val entityType: String? = null,
    @kotlinx.serialization.SerialName("is_read") val isRead: Boolean = false,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String = "",
    @kotlinx.serialization.SerialName("updated_at") val updatedAt: String = ""
)

@kotlinx.serialization.Serializable
data class FollowerDB(
    val id: String? = null,
    @kotlinx.serialization.SerialName("follower_id") val followerId: String,
    @kotlinx.serialization.SerialName("following_id") val followingId: String,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String = ""
)
