package com.rendly.app.data.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.remote.CloudflareService;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.ui.screens.profile.EditProfileData;
import com.rendly.app.ui.screens.profile.ProfileData;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import java.io.InputStream;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0013J\b\u0010\u0015\u001a\u00020\u000bH\u0002J\u001a\u0010\u0016\u001a\u0004\u0018\u00010\t2\b\b\u0002\u0010\u0017\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\u0018J@\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\t0\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010 2\b\u0010!\u001a\u0004\u0018\u00010 H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\"\u0010#J\u001a\u0010$\u001a\u0004\u0018\u00010%2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010&\u001a\u00020 H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fR\u000e\u0010\u0011\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\'"}, d2 = {"Lcom/rendly/app/data/repository/ProfileRepository;", "", "()V", "CACHE_DURATION_MS", "", "TAG", "", "_currentProfile", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/rendly/app/ui/screens/profile/ProfileData;", "_isLoading", "", "currentProfile", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentProfile", "()Lkotlinx/coroutines/flow/StateFlow;", "isLoading", "lastFetchTime", "clearProfile", "", "invalidateCache", "isCacheValid", "loadCurrentProfile", "forceRefresh", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProfile", "Lkotlin/Result;", "context", "Landroid/content/Context;", "data", "Lcom/rendly/app/ui/screens/profile/EditProfileData;", "avatarUri", "Landroid/net/Uri;", "bannerUri", "updateProfile-yxL6bBk", "(Landroid/content/Context;Lcom/rendly/app/ui/screens/profile/EditProfileData;Landroid/net/Uri;Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uriToBitmap", "Landroid/graphics/Bitmap;", "uri", "app_debug"})
public final class ProfileRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "ProfileRepository";
    private static final long CACHE_DURATION_MS = 300000L;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.ui.screens.profile.ProfileData> _currentProfile = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.ui.screens.profile.ProfileData> currentProfile = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    private static long lastFetchTime = 0L;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.ProfileRepository INSTANCE = null;
    
    private ProfileRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.ui.screens.profile.ProfileData> getCurrentProfile() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    private final boolean isCacheValid() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadCurrentProfile(boolean forceRefresh, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.ui.screens.profile.ProfileData> $completion) {
        return null;
    }
    
    private final android.graphics.Bitmap uriToBitmap(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    public final void clearProfile() {
    }
    
    public final void invalidateCache() {
    }
}