package com.rendly.app.data.remote;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class MapboxService_Factory implements Factory<MapboxService> {
  @Override
  public MapboxService get() {
    return newInstance();
  }

  public static MapboxService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MapboxService newInstance() {
    return new MapboxService();
  }

  private static final class InstanceHolder {
    private static final MapboxService_Factory INSTANCE = new MapboxService_Factory();
  }
}
