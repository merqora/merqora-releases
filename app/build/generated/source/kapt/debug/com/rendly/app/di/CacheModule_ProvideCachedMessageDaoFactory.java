package com.rendly.app.di;

import com.rendly.app.data.cache.db.CachedMessageDao;
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
public final class CacheModule_ProvideCachedMessageDaoFactory implements Factory<CachedMessageDao> {
  private final Provider<MerqoraDatabase> databaseProvider;

  public CacheModule_ProvideCachedMessageDaoFactory(Provider<MerqoraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CachedMessageDao get() {
    return provideCachedMessageDao(databaseProvider.get());
  }

  public static CacheModule_ProvideCachedMessageDaoFactory create(
      Provider<MerqoraDatabase> databaseProvider) {
    return new CacheModule_ProvideCachedMessageDaoFactory(databaseProvider);
  }

  public static CachedMessageDao provideCachedMessageDao(MerqoraDatabase database) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.provideCachedMessageDao(database));
  }
}
