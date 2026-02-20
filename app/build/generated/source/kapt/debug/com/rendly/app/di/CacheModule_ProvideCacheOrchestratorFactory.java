package com.rendly.app.di;

import android.content.Context;
import com.rendly.app.data.cache.CacheOrchestrator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CacheModule_ProvideCacheOrchestratorFactory implements Factory<CacheOrchestrator> {
  private final Provider<Context> contextProvider;

  public CacheModule_ProvideCacheOrchestratorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CacheOrchestrator get() {
    return provideCacheOrchestrator(contextProvider.get());
  }

  public static CacheModule_ProvideCacheOrchestratorFactory create(
      Provider<Context> contextProvider) {
    return new CacheModule_ProvideCacheOrchestratorFactory(contextProvider);
  }

  public static CacheOrchestrator provideCacheOrchestrator(Context context) {
    return Preconditions.checkNotNullFromProvides(CacheModule.INSTANCE.provideCacheOrchestrator(context));
  }
}
