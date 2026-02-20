package com.rendly.app.data.cache.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room Database for local caching.
 * Single source of truth for cached data.
 *
 * Migration strategy: destructive for cache (data is ephemeral)
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00172\u00020\u0001:\u0001\u0017B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\fH&J\b\u0010\r\u001a\u00020\u000eH&J\b\u0010\u000f\u001a\u00020\u0010H&J\b\u0010\u0011\u001a\u00020\u0012H&J\b\u0010\u0013\u001a\u00020\u0014H&J\b\u0010\u0015\u001a\u00020\u0016H&\u00a8\u0006\u0018"}, d2 = {"Lcom/rendly/app/data/cache/db/MerqoraDatabase;", "Landroidx/room/RoomDatabase;", "()V", "cacheSyncMetadataDao", "Lcom/rendly/app/data/cache/db/CacheSyncMetadataDao;", "cachedConversationDao", "Lcom/rendly/app/data/cache/db/CachedConversationDao;", "cachedFollowDao", "Lcom/rendly/app/data/cache/db/CachedFollowDao;", "cachedMessageDao", "Lcom/rendly/app/data/cache/db/CachedMessageDao;", "cachedNotificationDao", "Lcom/rendly/app/data/cache/db/CachedNotificationDao;", "cachedPostDao", "Lcom/rendly/app/data/cache/db/CachedPostDao;", "cachedRendDao", "Lcom/rendly/app/data/cache/db/CachedRendDao;", "cachedStoryDao", "Lcom/rendly/app/data/cache/db/CachedStoryDao;", "cachedUserDao", "Lcom/rendly/app/data/cache/db/CachedUserDao;", "pendingOperationDao", "Lcom/rendly/app/data/cache/db/PendingOperationDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {com.rendly.app.data.cache.db.CachedUserEntity.class, com.rendly.app.data.cache.db.CachedPostEntity.class, com.rendly.app.data.cache.db.CachedRendEntity.class, com.rendly.app.data.cache.db.CachedStoryEntity.class, com.rendly.app.data.cache.db.CachedMessageEntity.class, com.rendly.app.data.cache.db.CachedConversationEntity.class, com.rendly.app.data.cache.db.CachedNotificationEntity.class, com.rendly.app.data.cache.db.CachedFollowEntity.class, com.rendly.app.data.cache.db.CacheSyncMetadataEntity.class, com.rendly.app.data.cache.db.PendingOperationEntity.class}, version = 1, exportSchema = false)
public abstract class MerqoraDatabase extends androidx.room.RoomDatabase {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String DATABASE_NAME = "Merqora_cache.db";
    @kotlin.jvm.Volatile
    @org.jetbrains.annotations.Nullable
    private static volatile com.rendly.app.data.cache.db.MerqoraDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.cache.db.MerqoraDatabase.Companion Companion = null;
    
    public MerqoraDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedUserDao cachedUserDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedPostDao cachedPostDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedRendDao cachedRendDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedStoryDao cachedStoryDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedMessageDao cachedMessageDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedConversationDao cachedConversationDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedNotificationDao cachedNotificationDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CachedFollowDao cachedFollowDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.CacheSyncMetadataDao cacheSyncMetadataDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.rendly.app.data.cache.db.PendingOperationDao pendingOperationDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tH\u0002J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\fJ\u000e\u0010\r\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/rendly/app/data/cache/db/MerqoraDatabase$Companion;", "", "()V", "DATABASE_NAME", "", "INSTANCE", "Lcom/rendly/app/data/cache/db/MerqoraDatabase;", "buildDatabase", "context", "Landroid/content/Context;", "clearAllCaches", "", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getInstance", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.cache.db.MerqoraDatabase getInstance(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
            return null;
        }
        
        private final com.rendly.app.data.cache.db.MerqoraDatabase buildDatabase(android.content.Context context) {
            return null;
        }
        
        /**
         * Clear all cached data (for logout or cache invalidation)
         */
        @org.jetbrains.annotations.Nullable
        public final java.lang.Object clearAllCaches(@org.jetbrains.annotations.NotNull
        android.content.Context context, @org.jetbrains.annotations.NotNull
        kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
            return null;
        }
    }
}