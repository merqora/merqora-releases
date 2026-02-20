package com.rendly.app.di;

import com.rendly.app.data.cache.db.CachedRendDao;
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
public final class CacheModule_ProvideCachedRendDaoFactory implements Factory<CachedRendDao> {
  private final Provider<MerqoraDatabase> databaseProvider;

  public CacheModule_ProvideCachedRendDaoFactory(Provider<MerqoraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CachedRendDao get() {
    return provideCachedRendDao(databaseProvider.get());
  }

  public static CacheModule_ProvideCachedRendDaoFactory create(
      Provider<MerqoraDatabase> databaseProvider) {
    return new CacheModule_ProvideCachedRendDaoFactory(databaseProvider);
  }

  public static CachedRendDao provideCachedRendDao(MerqoraDatabase database) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.provideCachedRendDao(database));
  }
}
