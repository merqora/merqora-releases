package com.rendly.app.di;

import com.rendly.app.data.cache.db.CachedNotificationDao;
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
public final class CacheModule_ProvideCachedNotificationDaoFactory implements Factory<CachedNotificationDao> {
  private final Provider<MerqoraDatabase> databaseProvider;

  public CacheModule_ProvideCachedNotificationDaoFactory(
      Provider<MerqoraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CachedNotificationDao get() {
    return provideCachedNotificationDao(databaseProvider.get());
  }

  public static CacheModule_ProvideCachedNotificationDaoFactory create(
      Provider<MerqoraDatabase> databaseProvider) {
    return new CacheModule_ProvideCachedNotificationDaoFactory(databaseProvider);
  }

  public static CachedNotificationDao provideCachedNotificationDao(MerqoraDatabase database) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.provideCachedNotificationDao(database));
  }
}
