package com.rendly.app.data.repository;

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
public final class AddressRepository_Factory implements Factory<AddressRepository> {
  @Override
  public AddressRepository get() {
    return newInstance();
  }

  public static AddressRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AddressRepository newInstance() {
    return new AddressRepository();
  }

  private static final class InstanceHolder {
    private static final AddressRepository_Factory INSTANCE = new AddressRepository_Factory();
  }
}
