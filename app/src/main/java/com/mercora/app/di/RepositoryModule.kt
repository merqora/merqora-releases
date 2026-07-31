package com.mercora.app.di

import com.mercora.app.data.repository.AppUpdateRepository
import com.mercora.app.data.repository.CartRepository
import com.mercora.app.data.repository.ChatRepository
import com.mercora.app.data.repository.CommentRepository
import com.mercora.app.data.repository.ExploreRepository
import com.mercora.app.data.repository.FollowersRepository
import com.mercora.app.data.repository.LiveStreamRepository
import com.mercora.app.data.repository.MercadoPagoRepository
import com.mercora.app.data.repository.NotificationRepository
import com.mercora.app.data.repository.PostRepository
import com.mercora.app.data.repository.ProfileRepository
import com.mercora.app.data.repository.RendRepository
import com.mercora.app.data.repository.SecurityRepository
import com.mercora.app.data.repository.StoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePostRepository(): PostRepository = PostRepository

    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository = ProfileRepository

    @Provides
    @Singleton
    fun provideChatRepository(): ChatRepository = ChatRepository

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository = NotificationRepository

    @Provides
    @Singleton
    fun provideStoryRepository(): StoryRepository = StoryRepository

    @Provides
    @Singleton
    fun provideCommentRepository(): CommentRepository = CommentRepository

    @Provides
    @Singleton
    fun provideExploreRepository(): ExploreRepository = ExploreRepository

    @Provides
    @Singleton
    fun provideFollowersRepository(): FollowersRepository = FollowersRepository

    @Provides
    @Singleton
    fun provideCartRepository(): CartRepository = CartRepository

    @Provides
    @Singleton
    fun provideLiveStreamRepository(): LiveStreamRepository = LiveStreamRepository

    @Provides
    @Singleton
    fun provideSecurityRepository(): SecurityRepository = SecurityRepository

    @Provides
    @Singleton
    fun provideRendRepository(): RendRepository = RendRepository

    @Provides
    @Singleton
    fun provideMercadoPagoRepository(): MercadoPagoRepository = MercadoPagoRepository

    @Provides
    @Singleton
    fun provideAppUpdateRepository(): AppUpdateRepository = AppUpdateRepository

}
