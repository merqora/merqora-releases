package com.rendly.app.ui.screens.auth;

import android.app.Application;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<Application> applicationProvider;

  public LoginViewModel_Factory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(applicationProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<Application> applicationProvider) {
    return new LoginViewModel_Factory(applicationProvider);
  }

  public static LoginViewModel newInstance(Application application) {
    return new LoginViewModel(application);
  }
}
