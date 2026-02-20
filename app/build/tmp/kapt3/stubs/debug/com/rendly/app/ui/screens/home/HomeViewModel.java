package com.rendly.app.ui.screens.home;

import android.util.Log;
import androidx.lifecycle.ViewModel;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.model.PostDB;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.remote.SupabaseClient;
import com.rendly.app.data.repository.NotificationRepository;
import com.rendly.app.data.repository.PostRepository;
import com.rendly.app.data.repository.ProfileRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.github.jan.supabase.postgrest.query.Order;
import kotlinx.coroutines.Dispatchers;
import kotlinx.serialization.Serializable;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010%\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0012\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010$\u001a\u00020%J\u000e\u0010&\u001a\u00020%2\u0006\u0010\'\u001a\u00020\u000bJ\u000e\u0010(\u001a\u00020%2\u0006\u0010\'\u001a\u00020\u000bJ\b\u0010)\u001a\u00020%H\u0002J\b\u0010*\u001a\u00020%H\u0002J\u0006\u0010+\u001a\u00020%J\u000e\u0010,\u001a\u00020%2\u0006\u0010-\u001a\u00020\u000bJ\u0006\u0010.\u001a\u00020%J\u000e\u0010/\u001a\u00020%2\u0006\u0010\'\u001a\u00020\u000bJ\u000e\u00100\u001a\u00020%2\u0006\u0010\'\u001a\u00020\u000bJ\u000e\u00101\u001a\u00020%2\u0006\u0010\'\u001a\u00020\u000bJ&\u00102\u001a\u00020%2\u0006\u0010\'\u001a\u00020\u000b2\u0006\u00103\u001a\u00020\u000b2\u0006\u00104\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u00105J\u0016\u00106\u001a\u00020%2\u0006\u0010\'\u001a\u00020\u000b2\u0006\u00104\u001a\u00020\u0010R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00100\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00100\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0019\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0017R\u0019\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0017R\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00100\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0017R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00100\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0017R\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00100\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0017R\u001d\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0017R\u001a\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\r0#X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00067"}, d2 = {"Lcom/rendly/app/ui/screens/home/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "INITIAL_LOAD_COUNT", "", "LOAD_MORE_COUNT", "_allPosts", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/model/Post;", "_currentRoute", "", "_currentUser", "Lcom/rendly/app/data/model/Usuario;", "_errorMessage", "_hasMorePosts", "", "_isLoading", "_isLoadingMore", "_visiblePosts", "currentRoute", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentRoute", "()Lkotlinx/coroutines/flow/StateFlow;", "currentUser", "getCurrentUser", "errorMessage", "getErrorMessage", "hasMorePosts", "getHasMorePosts", "isLoading", "isLoadingMore", "posts", "getPosts", "usersCache", "", "clearError", "", "incrementShareCount", "postId", "incrementViewCount", "loadCurrentUser", "loadInitialPosts", "loadMorePosts", "navigateTo", "route", "refreshPosts", "toggleLike", "toggleSave", "toggleStats", "updatePostCount", "countField", "increment", "(Ljava/lang/String;Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateReviewsCount", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.Post>> _allPosts = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.Post>> _visiblePosts = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.Post>> posts = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoadingMore = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoadingMore = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _hasMorePosts = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> hasMorePosts = null;
    private final int INITIAL_LOAD_COUNT = 1;
    private final int LOAD_MORE_COUNT = 3;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _currentRoute = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> currentRoute = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.model.Usuario> _currentUser = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.model.Usuario> currentUser = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _errorMessage = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> errorMessage = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.Map<java.lang.String, com.rendly.app.data.model.Usuario> usersCache = null;
    
    @javax.inject.Inject
    public HomeViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.Post>> getPosts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    private final java.lang.Object updatePostCount(java.lang.String postId, java.lang.String countField, boolean increment, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoadingMore() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHasMorePosts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getCurrentRoute() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.model.Usuario> getCurrentUser() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getErrorMessage() {
        return null;
    }
    
    public final void clearError() {
    }
    
    private final void loadCurrentUser() {
    }
    
    private final void loadInitialPosts() {
    }
    
    public final void loadMorePosts() {
    }
    
    public final void toggleLike(@org.jetbrains.annotations.NotNull
    java.lang.String postId) {
    }
    
    public final void incrementShareCount(@org.jetbrains.annotations.NotNull
    java.lang.String postId) {
    }
    
    public final void incrementViewCount(@org.jetbrains.annotations.NotNull
    java.lang.String postId) {
    }
    
    public final void updateReviewsCount(@org.jetbrains.annotations.NotNull
    java.lang.String postId, boolean increment) {
    }
    
    public final void toggleSave(@org.jetbrains.annotations.NotNull
    java.lang.String postId) {
    }
    
    public final void toggleStats(@org.jetbrains.annotations.NotNull
    java.lang.String postId) {
    }
    
    public final void navigateTo(@org.jetbrains.annotations.NotNull
    java.lang.String route) {
    }
    
    public final void refreshPosts() {
    }
}