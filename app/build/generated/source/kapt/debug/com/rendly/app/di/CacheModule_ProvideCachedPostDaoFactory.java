package com.rendly.app.di;

import com.rendly.app.data.cache.db.CachedPostDao;
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
public final class CacheModule_ProvideCachedPostDaoFactory implements Factory<CachedPostDao> {
  private final Provider<MerqoraDatabase> databaseProvider;

  public CacheModule_ProvideCachedPostDaoFactory(Provider<MerqoraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CachedPostDao get() {
    return provideCachedPostDao(databaseProvider.get());
  }

  public static CacheModule_ProvideCachedPostDaoFactory create(
      Provider<MerqoraDatabase> databaseProvider) {
    return new CacheModule_ProvideCachedPostDaoFactory(databaseProvider);
  }

  public static CachedPostDao provideCachedPostDao(MerqoraDatabase database) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.provideCachedPostDao(database));
  }
}
