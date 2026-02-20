package com.rendly.app.data.cache.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database for local caching.
 * Single source of truth for cached data.
 * 
 * Migration strategy: destructive for cache (data is ephemeral)
 */
@Database(
    entities = [
        CachedUserEntity::class,
        CachedPostEntity::class,
        CachedRendEntity::class,
        CachedStoryEntity::class,
        CachedMessageEntity::class,
        CachedConversationEntity::class,
        CachedNotificationEntity::class,
        CachedFollowEntity::class,
        CacheSyncMetadataEntity::class,
        PendingOperationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MerqoraDatabase : RoomDatabase() {
    
    abstract fun cachedUserDao(): CachedUserDao
    abstract fun cachedPostDao(): CachedPostDao
    abstract fun cachedRendDao(): CachedRendDao
    abstract fun cachedStoryDao(): CachedStoryDao
    abstract fun cachedMessageDao(): CachedMessageDao
    abstract fun cachedConversationDao(): CachedConversationDao
    abstract fun cachedNotificationDao(): CachedNotificationDao
    abstract fun cachedFollowDao(): CachedFollowDao
    abstract fun cacheSyncMetadataDao(): CacheSyncMetadataDao
    abstract fun pendingOperationDao(): PendingOperationDao
    
    companion object {
        private const val DATABASE_NAME = "Merqora_cache.db"
        
        @Volatile
        private var INSTANCE: MerqoraDatabase? = null
        
        fun getInstance(context: Context): MerqoraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): MerqoraDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MerqoraDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
        
        /**
         * Clear all cached data (for logout or cache invalidation)
         */
        suspend fun clearAllCaches(context: Context) {
            getInstance(context).apply {
                cachedUserDao().deleteAll()
                cachedPostDao().deleteAll()
                cachedRendDao().deleteAll()
                cachedStoryDao().deleteAll()
                cachedMessageDao().deleteAll()
                cachedConversationDao().deleteAll()
                cachedNotificationDao().deleteAll()
                cachedFollowDao().deleteAll()
                cacheSyncMetadataDao().deleteAll()
            }
        }
    }
}
