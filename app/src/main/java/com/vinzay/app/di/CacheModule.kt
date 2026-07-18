package com.vinzay.app.di

import android.content.Context
import com.vinzay.app.data.cache.CacheOrchestrator
import com.vinzay.app.data.cache.db.*
import com.vinzay.app.data.cache.repository.CachedRendRepository
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
    
    // -----------------------------------------------------------------------
    // DATABASE
    // -----------------------------------------------------------------------
    
    /**
     * COLD START OPTIMIZATION: Database se crea lazy cuando se accede por primera vez.
     * NO usar @Singleton aquí - el singleton está en VinzayDatabase.getInstance()
     * Esto evita que Hilt fuerce la creación durante el startup.
     */
    @Provides
    fun provideVinzayDatabase(
        @ApplicationContext context: Context
    ): VinzayDatabase {
        return VinzayDatabase.getInstance(context)
    }
    
    // -----------------------------------------------------------------------
    // DAOs
    // -----------------------------------------------------------------------
    
    @Provides
    fun provideCachedUserDao(database: VinzayDatabase): CachedUserDao {
        return database.cachedUserDao()
    }
    
    @Provides
    fun provideCachedPostDao(database: VinzayDatabase): CachedPostDao {
        return database.cachedPostDao()
    }
    
    @Provides
    fun provideCachedRendDao(database: VinzayDatabase): CachedRendDao {
        return database.cachedRendDao()
    }
    
    @Provides
    fun provideCachedStoryDao(database: VinzayDatabase): CachedStoryDao {
        return database.cachedStoryDao()
    }
    
    @Provides
    fun provideCachedMessageDao(database: VinzayDatabase): CachedMessageDao {
        return database.cachedMessageDao()
    }
    
    @Provides
    fun provideCachedConversationDao(database: VinzayDatabase): CachedConversationDao {
        return database.cachedConversationDao()
    }
    
    @Provides
    fun provideCachedNotificationDao(database: VinzayDatabase): CachedNotificationDao {
        return database.cachedNotificationDao()
    }
    
    @Provides
    fun provideCachedFollowDao(database: VinzayDatabase): CachedFollowDao {
        return database.cachedFollowDao()
    }
    
    @Provides
    fun provideCacheSyncMetadataDao(database: VinzayDatabase): CacheSyncMetadataDao {
        return database.cacheSyncMetadataDao()
    }
    
    @Provides
    fun providePendingOperationDao(database: VinzayDatabase): PendingOperationDao {
        return database.pendingOperationDao()
    }
    
    // -----------------------------------------------------------------------
    // REPOSITORIES
    // -----------------------------------------------------------------------
    
    /**
     * COLD START OPTIMIZATION: Repository lazy
     */
    @Provides
    fun provideCachedRendRepository(
        @ApplicationContext context: Context
    ): CachedRendRepository {
        return CachedRendRepository.getInstance(context)
    }
    
    // -----------------------------------------------------------------------
    // ADDRESS REPOSITORY
    // -----------------------------------------------------------------------
    
    // AddressRepository uses @Inject constructor with @Singleton annotation
    // No provider needed here - Hilt will create it automatically
    
    // -----------------------------------------------------------------------
    // ORCHESTRATOR
    // -----------------------------------------------------------------------
    
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
