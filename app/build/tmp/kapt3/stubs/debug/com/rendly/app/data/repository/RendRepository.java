package com.rendly.app.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.rendly.app.data.model.Rend;
import com.rendly.app.data.model.RendDB;
import com.rendly.app.data.model.Usuario;
import com.rendly.app.data.remote.ImageKitService;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0007\n\u0002\b\u0013\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0017\u001a\u00020\u0018J\u0090\u0001\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u00042\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010#2\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u00042\b\b\u0002\u0010&\u001a\u00020\'2\u0014\b\u0002\u0010(\u001a\u000e\u0012\u0004\u0012\u00020*\u0012\u0004\u0012\u00020\u00180)H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b+\u0010,J\u000e\u0010-\u001a\u00020\u0004H\u0082@\u00a2\u0006\u0002\u0010.J,\u0010/\u001a\b\u0012\u0004\u0012\u00020\'0\u001a2\u0006\u00100\u001a\u00020\u00042\u0006\u00101\u001a\u00020\'H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b2\u00103J,\u00104\u001a\b\u0012\u0004\u0012\u00020\'0\u001a2\u0006\u00100\u001a\u00020\u00042\u0006\u00101\u001a\u00020\'H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b5\u00103J\u000e\u00106\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010.J\u0006\u00107\u001a\u00020\u0018J4\u00108\u001a\b\u0012\u0004\u0012\u00020\'0\u001a2\u0006\u00100\u001a\u00020\u00042\u0006\u00109\u001a\u00020\'2\u0006\u0010:\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b;\u0010<R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u001d\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0011\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006="}, d2 = {"Lcom/rendly/app/data/repository/RendRepository;", "", "()V", "TAG", "", "_errorMessage", "Lkotlinx/coroutines/flow/MutableStateFlow;", "_isLoading", "", "_rends", "", "Lcom/rendly/app/data/model/Rend;", "_uploadState", "Lcom/rendly/app/data/repository/RendUploadState;", "errorMessage", "Lkotlinx/coroutines/flow/StateFlow;", "getErrorMessage", "()Lkotlinx/coroutines/flow/StateFlow;", "isLoading", "rends", "getRends", "uploadState", "getUploadState", "clearError", "", "createRend", "Lkotlin/Result;", "context", "Landroid/content/Context;", "videoUri", "Landroid/net/Uri;", "title", "description", "productTitle", "productPrice", "", "productImage", "productId", "duration", "", "onProgress", "Lkotlin/Function1;", "", "createRend-5p_uFSQ", "(Landroid/content/Context;Landroid/net/Uri;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;ILkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCurrentUserId", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "incrementCommentCount", "rendId", "currentCount", "incrementCommentCount-0E7RQCE", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "incrementShareCount", "incrementShareCount-0E7RQCE", "loadRends", "resetUploadState", "toggleLike", "currentLikeCount", "isLiked", "toggleLike-BWLJW6A", "(Ljava/lang/String;IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class RendRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "RendRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.rendly.app.data.repository.RendUploadState> _uploadState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.RendUploadState> uploadState = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.model.Rend>> _rends = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.Rend>> rends = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _errorMessage = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.String> errorMessage = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.RendRepository INSTANCE = null;
    
    private RendRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<com.rendly.app.data.repository.RendUploadState> getUploadState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.model.Rend>> getRends() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getErrorMessage() {
        return null;
    }
    
    public final void clearError() {
    }
    
    private final java.lang.Object getCurrentUserId(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Load all rends from Supabase
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadRends(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void resetUploadState() {
    }
}