package com.rendly.app.data.cache.core;

import kotlin.time.Duration;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0010\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u001e\u0010\u0000\u001a\n\u0012\u0004\u0012\u0002H\u0002\u0018\u00010\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0003\u001a\u001d\u0010\u0004\u001a\u0004\u0018\u0001H\u0002\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"dataOrNull", "Lcom/rendly/app/data/cache/core/CachedData;", "T", "Lcom/rendly/app/data/cache/core/CacheResult;", "getOrNull", "(Lcom/rendly/app/data/cache/core/CacheResult;)Ljava/lang/Object;", "app_debug"})
public final class CachePolicyKt {
    
    /**
     * Extension to convert CacheResult to nullable value
     */
    @org.jetbrains.annotations.Nullable
    public static final <T extends java.lang.Object>T getOrNull(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CacheResult<? extends T> $this$getOrNull) {
        return null;
    }
    
    /**
     * Extension to get data regardless of staleness
     */
    @org.jetbrains.annotations.Nullable
    public static final <T extends java.lang.Object>com.rendly.app.data.cache.core.CachedData<T> dataOrNull(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.core.CacheResult<? extends T> $this$dataOrNull) {
        return null;
    }
}