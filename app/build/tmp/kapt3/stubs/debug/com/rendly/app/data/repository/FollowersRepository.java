package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J$\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\u0010J$\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0013\u0010\u0010J\u0016\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u001c\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\u001b2\u0006\u0010\u0016\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u001c\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0010J\u001c\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00040\u001b2\u0006\u0010\u0016\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u001e\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u0010J$\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b \u0010\u0010J$\u0010!\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\"\u0010\u0010J$\u0010#\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u0012\u001a\u00020\u0004H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b$\u0010\u0010R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\n\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006%"}, d2 = {"Lcom/rendly/app/data/repository/FollowersRepository;", "", "()V", "TAG", "", "_isLoading", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "isLoading", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "acceptClientRequest", "Lkotlin/Result;", "", "followerId", "acceptClientRequest-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "follow", "followedId", "follow-gIAlu-s", "getClientsCount", "", "userId", "getFollowType", "Lcom/rendly/app/data/repository/FollowType;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFollowers", "", "getFollowersCount", "getFollowing", "getReputation", "rejectClientRequest", "rejectClientRequest-gIAlu-s", "requestClient", "requestClient-gIAlu-s", "unfollow", "unfollow-gIAlu-s", "app_debug"})
public final class FollowersRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "FollowersRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.FollowersRepository INSTANCE = null;
    
    private FollowersRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    /**
     * Obtiene el conteo de SEGUIDORES NORMALES de un usuario
     * (NO incluye clientes - solo is_client=false)
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getFollowersCount(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Obtiene el conteo de clientes ACEPTADOS de un usuario
     * (is_client=true AND is_pending=false)
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getClientsCount(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Calcula la reputación basada en clientes
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getReputation(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Verifica el tipo de relación entre dos usuarios
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getFollowType(@org.jetbrains.annotations.NotNull
    java.lang.String followerId, @org.jetbrains.annotations.NotNull
    java.lang.String followedId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.repository.FollowType> $completion) {
        return null;
    }
    
    /**
     * Obtiene la lista de seguidores de un usuario
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getFollowers(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    /**
     * Obtiene la lista de usuarios que sigue
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getFollowing(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
}