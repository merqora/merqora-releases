package com.rendly.app.ui.screens.auth;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.ViewModel;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.util.FCMHelper;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.github.jan.supabase.exceptions.RestException;
import io.github.jan.supabase.gotrue.user.UserInfo;
import kotlinx.coroutines.flow.StateFlow;
import java.time.Instant;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\f\u001a\u00020\rH\u0002J\u001e\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\rJ\u0006\u0010\u0015\u001a\u00020\rR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/rendly/app/ui/screens/auth/LoginViewModel;", "Landroidx/lifecycle/ViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/rendly/app/ui/screens/auth/LoginUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "checkExistingSession", "", "login", "emailOrUsername", "", "password", "recibirNovedades", "", "loginAsGuest", "signOut", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class LoginViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final android.app.Application application = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.ui.screens.auth.LoginUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.rendly.app.ui.screens.auth.LoginUiState> uiState = null;
    
    @javax.inject.Inject
    public LoginViewModel(@org.jetbrains.annotations.NotNull
    android.app.Application application) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.ui.screens.auth.LoginUiState> getUiState() {
        return null;
    }
    
    private final void checkExistingSession() {
    }
    
    public final void login(@org.jetbrains.annotations.NotNull
    java.lang.String emailOrUsername, @org.jetbrains.annotations.NotNull
    java.lang.String password, boolean recibirNovedades) {
    }
    
    public final void loginAsGuest() {
    }
    
    public final void signOut() {
    }
}