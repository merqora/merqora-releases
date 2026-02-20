package com.rendly.app.di;

import com.rendly.app.data.cache.db.CacheSyncMetadataDao;
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
public final class CacheModule_ProvideCacheSyncMetadataDaoFactory implements Factory<CacheSyncMetadataDao> {
  private final Provider<MerqoraDatabase> databaseProvider;

  public CacheModule_ProvideCacheSyncMetadataDaoFactory(
      Provider<MerqoraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CacheSyncMetadataDao get() {
    return provideCacheSyncMetadataDao(databaseProvider.get());
  }

  public static CacheModule_ProvideCacheSyncMetadataDaoFactory create(
      Provider<MerqoraDatabase> databaseProvider) {
    return new CacheModule_ProvideCacheSyncMetadataDaoFactory(databaseProvider);
  }

  public static CacheSyncMetadataDao provideCacheSyncMetadataDao(MerqoraDatabase database) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.provideCacheSyncMetadataDao(database));
  }
}
