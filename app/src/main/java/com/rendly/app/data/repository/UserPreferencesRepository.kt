package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository for user preferences: Privacy, Language, Story Hidden Users, Mentions
 */
object UserPreferencesRepository {
    
    private const val TAG = "UserPreferencesRepo"
    
    // ══════════════════════════════════════════════════════════════════════════
    // DATA MODELS
    // ══════════════════════════════════════════════════════════════════════════
    
    @Serializable
    data class PrivacySettingsDB(
        @SerialName("user_id") val userId: String = "",
        @SerialName("profile_visibility") val profileVisibility: String = "public",
        @SerialName("show_online_status") val showOnlineStatus: Boolean = true,
        @SerialName("show_last_seen") val showLastSeen: Boolean = true,
        @SerialName("show_activity_status") val showActivityStatus: Boolean = true,
        @SerialName("allow_tagging") val allowTagging: Boolean = true,
        @SerialName("allow_mentions") val allowMentions: Boolean = true,
        @SerialName("show_likes") val showLikes: Boolean = true,
        @SerialName("show_purchase_activity") val showPurchaseActivity: Boolean = true,
        @SerialName("hide_story_enabled") val hideStoryEnabled: Boolean = false
    )
    
    @Serializable
    data class LanguagePreferenceDB(
        @SerialName("user_id") val userId: String = "",
        @SerialName("language_code") val languageCode: String = "es",
        @SerialName("auto_detect") val autoDetect: Boolean = true
    )
    
    @Serializable
    data class StoryHiddenUserDB(
        val id: String = "",
        @SerialName("user_id") val userId: String = "",
        @SerialName("hidden_user_id") val hiddenUserId: String = ""
    )
    
    @Serializable
    data class MentionUserDB(
        @SerialName("mention_user_id") val userId: String = "",
        @SerialName("mention_username") val username: String = "",
        @SerialName("mention_avatar_url") val avatarUrl: String? = null,
        @SerialName("mention_nombre_tienda") val nombreTienda: String? = null,
        @SerialName("mention_is_verified") val isVerified: Boolean = false,
        @SerialName("mention_is_following") val isFollowing: Boolean = false
    )
    
    @Serializable
    data class HiddenUserWithInfo(
        val id: String = "",
        @SerialName("hidden_user_id") val hiddenUserId: String = "",
        @SerialName("username") val username: String = "",
        @SerialName("avatar_url") val avatarUrl: String? = null
    )
    
    // ══════════════════════════════════════════════════════════════════════════
    // PRIVACY SETTINGS
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Ensures a default privacy settings row exists for the user.
     * Returns the loaded or newly created settings.
     */
    suspend fun ensureDefaultPrivacySettings(userId: String): PrivacySettingsDB = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "ensureDefaultPrivacySettings: checking for user $userId")
            val existing = SupabaseClient.database
                .from("user_privacy_settings")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<PrivacySettingsDB>()
            
            if (existing != null) {
                Log.d(TAG, "✓ Privacy settings already exist for $userId")
                return@withContext existing
            }
            
            // Create default row with ALL fields using buildJsonObject for proper serialization
            val defaults = buildJsonObject {
                put("user_id", userId)
                put("profile_visibility", "public")
                put("show_online_status", true)
                put("show_last_seen", true)
                put("show_activity_status", true)
                put("allow_tagging", true)
                put("allow_mentions", true)
                put("show_likes", true)
                put("show_purchase_activity", true)
                put("hide_story_enabled", false)
            }
            SupabaseClient.database
                .from("user_privacy_settings")
                .insert(defaults)
            Log.d(TAG, "✓ Created default privacy settings for $userId")
            PrivacySettingsDB(userId = userId)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error ensuring default privacy settings: ${e.message}", e)
            PrivacySettingsDB(userId = userId)
        }
    }
    
    suspend fun loadPrivacySettings(userId: String): PrivacySettingsDB? = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("user_privacy_settings")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<PrivacySettingsDB>()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading privacy settings", e)
            null
        }
    }
    
    suspend fun savePrivacyField(userId: String, field: String, value: Any, rowExists: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "savePrivacyField: $field = $value for user $userId")
            // Build proper JsonObject for reliable serialization
            val updateBody = buildJsonObject {
                when (value) {
                    is Boolean -> put(field, value)
                    is String -> put(field, value)
                    is Number -> put(field, value as Number)
                    else -> put(field, value.toString())
                }
            }
            SupabaseClient.database
                .from("user_privacy_settings")
                .update(updateBody) {
                    filter { eq("user_id", userId) }
                }
            Log.d(TAG, "✓ Updated privacy field: $field = $value")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating privacy field $field: ${e.message}", e)
            try {
                // Row might not exist yet - create full default + override field
                val insertBody = buildJsonObject {
                    put("user_id", userId)
                    put("profile_visibility", if (field == "profile_visibility" && value is String) value else "public")
                    put("show_online_status", if (field == "show_online_status" && value is Boolean) value else true)
                    put("show_last_seen", if (field == "show_last_seen" && value is Boolean) value else true)
                    put("show_activity_status", if (field == "show_activity_status" && value is Boolean) value else true)
                    put("allow_tagging", if (field == "allow_tagging" && value is Boolean) value else true)
                    put("allow_mentions", if (field == "allow_mentions" && value is Boolean) value else true)
                    put("show_likes", if (field == "show_likes" && value is Boolean) value else true)
                    put("show_purchase_activity", if (field == "show_purchase_activity" && value is Boolean) value else true)
                    put("hide_story_enabled", if (field == "hide_story_enabled" && value is Boolean) value else false)
                }
                SupabaseClient.database
                    .from("user_privacy_settings")
                    .insert(insertBody)
                Log.d(TAG, "✓ Inserted privacy row with $field = $value")
                true
            } catch (e2: Exception) {
                Log.e(TAG, "❌ Insert also failed for privacy $field: ${e2.message}", e2)
                false
            }
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // LANGUAGE PREFERENCES
    // ══════════════════════════════════════════════════════════════════════════
    
    suspend fun loadLanguagePreference(userId: String): LanguagePreferenceDB? = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("user_language_preferences")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<LanguagePreferenceDB>()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading language preference", e)
            null
        }
    }
    
    /**
     * Ensures a default language preference row exists for the user.
     */
    suspend fun ensureDefaultLanguagePreference(userId: String): LanguagePreferenceDB = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "ensureDefaultLanguagePreference: checking for user $userId")
            val existing = SupabaseClient.database
                .from("user_language_preferences")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<LanguagePreferenceDB>()
            
            if (existing != null) {
                Log.d(TAG, "✓ Language preference already exists for $userId")
                return@withContext existing
            }
            
            val defaults = buildJsonObject {
                put("user_id", userId)
                put("language_code", "es")
                put("auto_detect", true)
            }
            SupabaseClient.database
                .from("user_language_preferences")
                .insert(defaults)
            Log.d(TAG, "✓ Created default language preference for $userId")
            LanguagePreferenceDB(userId = userId)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error ensuring default language preference: ${e.message}", e)
            LanguagePreferenceDB(userId = userId)
        }
    }
    
    suspend fun saveLanguagePreference(userId: String, languageCode: String, autoDetect: Boolean, rowExists: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "saveLanguagePreference: $languageCode, autoDetect=$autoDetect for user $userId")
            val updateBody = buildJsonObject {
                put("language_code", languageCode)
                put("auto_detect", autoDetect)
            }
            SupabaseClient.database
                .from("user_language_preferences")
                .update(updateBody) {
                    filter { eq("user_id", userId) }
                }
            Log.d(TAG, "✓ Updated language preference")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating language preference: ${e.message}", e)
            try {
                val insertBody = buildJsonObject {
                    put("user_id", userId)
                    put("language_code", languageCode)
                    put("auto_detect", autoDetect)
                }
                SupabaseClient.database
                    .from("user_language_preferences")
                    .insert(insertBody)
                Log.d(TAG, "✓ Inserted language preference")
                true
            } catch (e2: Exception) {
                Log.e(TAG, "❌ Insert also failed for language: ${e2.message}", e2)
                false
            }
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // STORY HIDDEN USERS
    // ══════════════════════════════════════════════════════════════════════════
    
    suspend fun loadStoryHiddenUsers(userId: String): List<StoryHiddenUserDB> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("story_hidden_users")
                .select { filter { eq("user_id", userId) } }
                .decodeList<StoryHiddenUserDB>()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading story hidden users", e)
            emptyList()
        }
    }
    
    suspend fun addStoryHiddenUser(userId: String, hiddenUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("story_hidden_users")
                .insert(mapOf("user_id" to userId, "hidden_user_id" to hiddenUserId))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding story hidden user", e)
            false
        }
    }
    
    suspend fun removeStoryHiddenUser(userId: String, hiddenUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("story_hidden_users")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("hidden_user_id", hiddenUserId)
                    }
                }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing story hidden user", e)
            false
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // MENTION USER SEARCH
    // ══════════════════════════════════════════════════════════════════════════
    
    suspend fun searchUsersForMention(query: String): List<MentionUserDB> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
            
            SupabaseClient.database
                .rpc(
                    "search_users_for_mention",
                    buildJsonObject {
                        put("p_current_user_id", currentUserId)
                        put("p_query", query)
                        put("p_limit", 15)
                    }
                )
                .decodeList<MentionUserDB>()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users for mention", e)
            // Fallback: simple search on usuarios table
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
                searchUsersFallback(currentUserId, query)
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback search also failed", e2)
                emptyList()
            }
        }
    }
    
    @Serializable
    private data class SimpleUserDB(
        @SerialName("user_id") val userId: String = "",
        val username: String = "",
        @SerialName("avatar_url") val avatarUrl: String? = null,
        @SerialName("nombre_tienda") val nombreTienda: String? = null,
        @SerialName("is_verified") val isVerified: Boolean = false
    )
    
    private suspend fun searchUsersFallback(currentUserId: String, query: String): List<MentionUserDB> {
        val users = if (query.isEmpty()) {
            // Show followed users by default
            @Serializable
            data class FollowDB(@SerialName("following_id") val followingId: String = "")
            
            val following = SupabaseClient.database
                .from("followers")
                .select { 
                    filter { eq("follower_id", currentUserId) }
                    limit(15)
                }
                .decodeList<FollowDB>()
            
            val ids = following.map { it.followingId }
            if (ids.isEmpty()) return emptyList()
            
            SupabaseClient.database
                .from("usuarios")
                .select { filter { isIn("user_id", ids) } }
                .decodeList<SimpleUserDB>()
        } else {
            SupabaseClient.database
                .from("usuarios")
                .select {
                    filter {
                        neq("user_id", currentUserId)
                        ilike("username", "%$query%")
                    }
                    limit(15)
                }
                .decodeList<SimpleUserDB>()
        }
        
        return users.map {
            MentionUserDB(
                userId = it.userId,
                username = it.username,
                avatarUrl = it.avatarUrl,
                nombreTienda = it.nombreTienda,
                isVerified = it.isVerified,
                isFollowing = false
            )
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════════
    // NOTIFICATION PREFERENCES (fix for existing screen)
    // ══════════════════════════════════════════════════════════════════════════
    
    /**
     * Ensures a default notification preferences row exists for the user.
     */
    suspend fun ensureDefaultNotificationPreferences(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "ensureDefaultNotificationPreferences: checking for user $userId")
            @Serializable
            data class NotifCheck(@SerialName("user_id") val userId: String = "")
            
            val existing = SupabaseClient.database
                .from("notification_preferences")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<NotifCheck>()
            
            if (existing != null) {
                Log.d(TAG, "✓ Notification preferences already exist for $userId")
                return@withContext true
            }
            
            val defaults = buildJsonObject {
                put("user_id", userId)
                put("push_enabled", true)
                put("email_enabled", true)
                put("price_drops", true)
                put("stock_alerts", true)
                put("messages", true)
                put("new_followers", true)
                put("comments", true)
                put("likes", false)
                put("promotions", true)
            }
            SupabaseClient.database
                .from("notification_preferences")
                .insert(defaults)
            Log.d(TAG, "✓ Created default notification preferences for $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error ensuring default notification preferences: ${e.message}", e)
            false
        }
    }
    
    suspend fun saveNotificationField(userId: String, field: String, value: Boolean, rowExists: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "saveNotificationField: $field = $value for user $userId")
            val updateBody = buildJsonObject { put(field, value) }
            SupabaseClient.database
                .from("notification_preferences")
                .update(updateBody) {
                    filter { eq("user_id", userId) }
                }
            Log.d(TAG, "✓ Updated notification field: $field = $value")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating notification field $field: ${e.message}", e)
            try {
                // Full default row + override field
                val insertBody = buildJsonObject {
                    put("user_id", userId)
                    put("push_enabled", if (field == "push_enabled") value else true)
                    put("email_enabled", if (field == "email_enabled") value else true)
                    put("price_drops", if (field == "price_drops") value else true)
                    put("stock_alerts", if (field == "stock_alerts") value else true)
                    put("messages", if (field == "messages") value else true)
                    put("new_followers", if (field == "new_followers") value else true)
                    put("comments", if (field == "comments") value else true)
                    put("likes", if (field == "likes") value else false)
                    put("promotions", if (field == "promotions") value else true)
                }
                SupabaseClient.database
                    .from("notification_preferences")
                    .insert(insertBody)
                Log.d(TAG, "✓ Inserted notification row with $field = $value")
                true
            } catch (e2: Exception) {
                Log.e(TAG, "❌ Insert also failed for notification $field: ${e2.message}", e2)
                false
            }
        }
    }
}
