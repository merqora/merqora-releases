package com.rendly.app.data.cache.network;

import android.util.Log;
import kotlinx.coroutines.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global request deduplicator instance
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0006\u0010\t\u001a\u00020\u0006JH\u0010\n\u001a\u0004\u0018\u0001H\u000b\"\u0004\b\u0000\u0010\u000b2\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\f\u001a\u00020\r2\u001e\u0010\u000e\u001a\u001a\b\u0001\u0012\f\u0012\n\u0012\u0006\u0012\u0004\u0018\u0001H\u000b0\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0011J\u0006\u0010\u0012\u001a\u00020\u0013J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/rendly/app/data/cache/network/GlobalRequestDeduplicator;", "", "()V", "deduplicator", "Lcom/rendly/app/data/cache/network/RequestDeduplicator;", "cancel", "", "key", "", "cancelAll", "dedupe", "T", "timeoutMs", "", "request", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "(Ljava/lang/String;JLkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "inFlightCount", "", "isInFlight", "", "app_debug"})
public final class GlobalRequestDeduplicator {
    @org.jetbrains.annotations.NotNull
    private static final com.rendly.app.data.cache.network.RequestDeduplicator deduplicator = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.cache.network.GlobalRequestDeduplicator INSTANCE = null;
    
    private GlobalRequestDeduplicator() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final <T extends java.lang.Object>java.lang.Object dedupe(@org.jetbrains.annotations.NotNull
    java.lang.String key, long timeoutMs, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super T>, ? extends java.lang.Object> request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super T> $completion) {
        return null;
    }
    
    public final void cancel(@org.jetbrains.annotations.NotNull
    java.lang.String key) {
    }
    
    public final void cancelAll() {
    }
    
    public final int inFlightCount() {
        return 0;
    }
    
    public final boolean isInFlight(@org.jetbrains.annotations.NotNull
    java.lang.String key) {
        return false;
    }
}