package com.rendly.app.di

import android.content.Context
import com.rendly.app.data.cache.CacheOrchestrator
import com.rendly.app.data.cache.db.*
import com.rendly.app.data.cache.repository.CachedRendRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for cache-related dependencies.
 * 
 * Provides:
 * - Room Database instance
 * - All DAOs
 * - Cache repositories
 * - Cache orchestrator
 */
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {
    
    // ═══════════════════════════════════════════════════════════════════════
    // DATABASE
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * COLD START OPTIMIZATION: Database se crea lazy cuando se accede por primera vez.
     * NO usar @Singleton aquí - el singleton está en MerqoraDatabase.getInstance()
     * Esto evita que Hilt fuerce la creación durante el startup.
     */
    @Provides
    fun provideMerqoraDatabase(
        @ApplicationContext context: Context
    ): MerqoraDatabase {
        return MerqoraDatabase.getInstance(context)
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // DAOs
    // ═══════════════════════════════════════════════════════════════════════
    
    @Provides
    fun provideCachedUserDao(database: MerqoraDatabase): CachedUserDao {
        return database.cachedUserDao()
    }
    
    @Provides
    fun provideCachedPostDao(database: MerqoraDatabase): CachedPostDao {
        return database.cachedPostDao()
    }
    
    @Provides
    fun provideCachedRendDao(database: MerqoraDatabase): CachedRendDao {
        return database.cachedRendDao()
    }
    
    @Provides
    fun provideCachedStoryDao(database: MerqoraDatabase): CachedStoryDao {
        return database.cachedStoryDao()
    }
    
    @Provides
    fun provideCachedMessageDao(database: MerqoraDatabase): CachedMessageDao {
        return database.cachedMessageDao()
    }
    
    @Provides
    fun provideCachedConversationDao(database: MerqoraDatabase): CachedConversationDao {
        return database.cachedConversationDao()
    }
    
    @Provides
    fun provideCachedNotificationDao(database: MerqoraDatabase): CachedNotificationDao {
        return database.cachedNotificationDao()
    }
    
    @Provides
    fun provideCachedFollowDao(database: MerqoraDatabase): CachedFollowDao {
        return database.cachedFollowDao()
    }
    
    @Provides
    fun provideCacheSyncMetadataDao(database: MerqoraDatabase): CacheSyncMetadataDao {
        return database.cacheSyncMetadataDao()
    }
    
    @Provides
    fun providePendingOperationDao(database: MerqoraDatabase): PendingOperationDao {
        return database.pendingOperationDao()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // REPOSITORIES
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * COLD START OPTIMIZATION: Repository lazy
     */
    @Provides
    fun provideCachedRendRepository(
        @ApplicationContext context: Context
    ): CachedRendRepository {
        return CachedRendRepository.getInstance(context)
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // ADDRESS REPOSITORY
    // ═══════════════════════════════════════════════════════════════════════
    
    // AddressRepository uses @Inject constructor with @Singleton annotation
    // No provider needed here - Hilt will create it automatically
    
    // ═══════════════════════════════════════════════════════════════════════
    // ORCHESTRATOR
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * COLD START OPTIMIZATION: CacheOrchestrator se crea lazy.
     * NO usar @Singleton - el singleton está en getInstance()
     */
    @Provides
    fun provideCacheOrchestrator(
        @ApplicationContext context: Context
    ): CacheOrchestrator {
        return CacheOrchestrator.getInstance(context)
    }
}
