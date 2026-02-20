package com.rendly.app.di;

import com.rendly.app.data.cache.db.CachedConversationDao;
import com.rendly.app.data.cache.db.MerqoraDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class CacheModule_ProvideCachedConversationDaoFactory implements Factory<CachedConversationDao> {
  private final Provider<MerqoraDatabase> databaseProvider;

  public CacheModule_ProvideCachedConversationDaoFactory(
      Provider<MerqoraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CachedConversationDao get() {
    return provideCachedConversationDao(databaseProvider.get());
  }

  public static CacheModule_ProvideCachedConversationDaoFactory create(
      Provider<MerqoraDatabase> databaseProvider) {
    return new CacheModule_ProvideCachedConversationDaoFactory(databaseProvider);
  }

  public static CachedConversationDao provideCachedConversationDao(MerqoraDatabase database) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.provideCachedConversationDao(database));
  }
}
