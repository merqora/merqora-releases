package com.rendly.app.di;

import com.rendly.app.data.cache.db.MerqoraDatabase;
import com.rendly.app.data.cache.db.PendingOperationDao;
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
public final class CacheModule_ProvidePendingOperationDaoFactory implements Factory<PendingOperationDao> {
  private final Provider<MerqoraDatabase> databaseProvider;

  public CacheModule_ProvidePendingOperationDaoFactory(Provider<MerqoraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PendingOperationDao get() {
    return providePendingOperationDao(databaseProvider.get());
  }

  public static CacheModule_ProvidePendingOperationDaoFactory create(
      Provider<MerqoraDatabase> databaseProvider) {
    return new CacheModule_ProvidePendingOperationDaoFactory(databaseProvider);
  }

  public static PendingOperationDao providePendingOperationDao(MerqoraDatabase database) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.providePendingOperationDao(database));
  }
}
