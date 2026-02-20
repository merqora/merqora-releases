package com.rendly.app.ui.viewmodel;

import android.content.Context;
import com.rendly.app.data.remote.MapboxService;
import com.rendly.app.data.repository.AddressRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AddressViewModel_Factory implements Factory<AddressViewModel> {
  private final Provider<AddressRepository> addressRepositoryProvider;

  private final Provider<MapboxService> mapboxServiceProvider;

  private final Provider<Context> contextProvider;

  public AddressViewModel_Factory(Provider<AddressRepository> addressRepositoryProvider,
      Provider<MapboxService> mapboxServiceProvider, Provider<Context> contextProvider) {
    this.addressRepositoryProvider = addressRepositoryProvider;
    this.mapboxServiceProvider = mapboxServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AddressViewModel get() {
    return newInstance(addressRepositoryProvider.get(), mapboxServiceProvider.get(), contextProvider.get());
  }

  public static AddressViewModel_Factory create(
      Provider<AddressRepository> addressRepositoryProvider,
      Provider<MapboxService> mapboxServiceProvider, Provider<Context> contextProvider) {
    return new AddressViewModel_Factory(addressRepositoryProvider, mapboxServiceProvider, contextProvider);
  }

  public static AddressViewModel newInstance(AddressRepository addressRepository,
      MapboxService mapboxService, Context context) {
    return new AddressViewModel(addressRepository, mapboxService, context);
  }
}
