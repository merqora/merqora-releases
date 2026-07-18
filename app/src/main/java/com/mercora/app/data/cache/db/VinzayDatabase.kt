package com.mercora.app.data.cache.db

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
abstract class VinzayDatabase : RoomDatabase() {
    
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
        private const val DATABASE_NAME = "Vinzay_cache.db"
        
        @Volatile
        private var INSTANCE: VinzayDatabase? = null
        
        fun getInstance(context: Context): VinzayDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): VinzayDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VinzayDatabase::class.java,
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
