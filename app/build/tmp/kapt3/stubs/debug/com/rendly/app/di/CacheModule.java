package com.rendly.app.di;

import android.content.Context;
import com.rendly.app.data.cache.CacheOrchestrator;
import com.rendly.app.data.cache.db.*;
import com.rendly.app.data.cache.repository.CachedRendRepository;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * Hilt module for cache-related dependencies.
 *
 * Provides:
 * - Room Database instance
 * - All DAOs
 * - Cache repositories
 * - Cache orchestrator
 */
@dagger.Module
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\t\u001a\u00020\nH\u0007J\u0012\u0010\u0017\u001a\u00020\u00182\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0012\u0010\u001d\u001a\u00020\n2\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\t\u001a\u00020\nH\u0007\u00a8\u0006 "}, d2 = {"Lcom/rendly/app/di/CacheModule;", "", "()V", "provideCacheOrchestrator", "Lcom/rendly/app/data/cache/CacheOrchestrator;", "context", "Landroid/content/Context;", "provideCacheSyncMetadataDao", "Lcom/rendly/app/data/cache/db/CacheSyncMetadataDao;", "database", "Lcom/rendly/app/data/cache/db/MerqoraDatabase;", "provideCachedConversationDao", "Lcom/rendly/app/data/cache/db/CachedConversationDao;", "provideCachedFollowDao", "Lcom/rendly/app/data/cache/db/CachedFollowDao;", "provideCachedMessageDao", "Lcom/rendly/app/data/cache/db/CachedMessageDao;", "provideCachedNotificationDao", "Lcom/rendly/app/data/cache/db/CachedNotificationDao;", "provideCachedPostDao", "Lcom/rendly/app/data/cache/db/CachedPostDao;", "provideCachedRendDao", "Lcom/rendly/app/data/cache/db/CachedRendDao;", "provideCachedRendRepository", "Lcom/rendly/app/data/cache/repository/CachedRendRepository;", "provideCachedStoryDao", "Lcom/rendly/app/data/cache/db/CachedStoryDao;", "provideCachedUserDao", "Lcom/rendly/app/data/cache/db/CachedUserDao;", "provideMerqoraDatabase", "providePendingOperationDao", "Lcom/rendly/app/data/cache/db/PendingOperationDao;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class CacheModule {
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.di.CacheModule INSTANCE = null;
    
    private CacheModule() {
        super();
    }
    
    /**
     * COLD START OPTIMIZATION: Database se crea lazy cuando se accede por primera vez.
     * NO usar @Singleton aquí - el singleton está en MerqoraDatabase.getInstance()
     * Esto evita que Hilt fuerce la creación durante el startup.
     */
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.MerqoraDatabase provideMerqoraDatabase(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedUserDao provideCachedUserDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedPostDao provideCachedPostDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedRendDao provideCachedRendDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedStoryDao provideCachedStoryDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedMessageDao provideCachedMessageDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedConversationDao provideCachedConversationDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedNotificationDao provideCachedNotificationDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedFollowDao provideCachedFollowDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CacheSyncMetadataDao provideCacheSyncMetadataDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.PendingOperationDao providePendingOperationDao(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.MerqoraDatabase database) {
        return null;
    }
    
    /**
     * COLD START OPTIMIZATION: Repository lazy
     */
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.repository.CachedRendRepository provideCachedRendRepository(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
    
    /**
     * COLD START OPTIMIZATION: CacheOrchestrator se crea lazy.
     * NO usar @Singleton - el singleton está en getInstance()
     */
    @dagger.Provides
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.CacheOrchestrator provideCacheOrchestrator(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        return null;
    }
}