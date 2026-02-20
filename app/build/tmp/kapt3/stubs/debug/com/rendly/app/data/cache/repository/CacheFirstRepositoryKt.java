package com.rendly.app.data.cache.repository;

import android.util.Log;
import com.rendly.app.data.cache.core.*;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0016\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\u001a\"\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00030\u0001\u001a&\u0010\u0004\u001a\u0004\u0018\u0001H\u0002\"\u0004\b\u0000\u0010\u0002*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00030\u0001H\u0086@\u00a2\u0006\u0002\u0010\u0005\u001aB\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00070\u00030\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0007*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00030\u00012\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u0002H\u0002\u0012\u0004\u0012\u0002H\u00070\t\u00a8\u0006\n"}, d2 = {"filterSuccess", "Lkotlinx/coroutines/flow/Flow;", "T", "Lcom/rendly/app/data/cache/repository/CacheFirstRepository$DataResult;", "firstSuccess", "(Lkotlinx/coroutines/flow/Flow;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "mapSuccess", "R", "transform", "Lkotlin/Function1;", "app_debug"})
public final class CacheFirstRepositoryKt {
    
    /**
     * Extension to collect only successful results
     */
    @org.jetbrains.annotations.NotNull
    public static final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<T> filterSuccess(@org.jetbrains.annotations.NotNull
    kotlinx.coroutines.flow.Flow<? extends com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult<? extends T>> $this$filterSuccess) {
        return null;
    }
    
    /**
     * Extension to get the first successful result
     */
    @org.jetbrains.annotations.Nullable
    public static final <T extends java.lang.Object>java.lang.Object firstSuccess(@org.jetbrains.annotations.NotNull
    kotlinx.coroutines.flow.Flow<? extends com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult<? extends T>> $this$firstSuccess, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super T> $completion) {
        return null;
    }
    
    /**
     * Extension to map successful results
     */
    @org.jetbrains.annotations.NotNull
    public static final <T extends java.lang.Object, R extends java.lang.Object>kotlinx.coroutines.flow.Flow<com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult<R>> mapSuccess(@org.jetbrains.annotations.NotNull
    kotlinx.coroutines.flow.Flow<? extends com.rendly.app.data.cache.repository.CacheFirstRepository.DataResult<? extends T>> $this$mapSuccess, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super T, ? extends R> transform) {
        return null;
    }
}