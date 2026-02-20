package com.rendly.app.data.cache.core;

import android.util.Log;
import android.util.LruCache;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Namespace-aware cache key builder
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0012\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u0004J\u001c\u0010\u000b\u001a\u00020\u00042\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\f\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\r\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u0010\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u0011\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u000e\u0010\u0012\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u0004J\u0010\u0010\u0013\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u0014\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\u0007\u001a\u00020\bJ\u0018\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u00042\b\b\u0002\u0010\u0017\u001a\u00020\u0004J\u0012\u0010\u0018\u001a\u00020\u00042\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0004J\u000e\u0010\u0019\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u0004\u00a8\u0006\u001a"}, d2 = {"Lcom/rendly/app/data/cache/core/CacheKey;", "", "()V", "comments", "", "entityId", "entityType", "page", "", "conversations", "userId", "feed", "followers", "following", "messages", "conversationId", "notifications", "posts", "profile", "rends", "rendsByUser", "search", "query", "type", "stories", "user", "app_debug"})
public final class CacheKey {
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.cache.core.CacheKey INSTANCE = null;
    
    private CacheKey() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String feed(@org.jetbrains.annotations.Nullable
    java.lang.String userId, int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String profile(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String posts(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String rends(int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String rendsByUser(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String stories(@org.jetbrains.annotations.Nullable
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String messages(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String conversations(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String notifications(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String user(@org.jetbrains.annotations.NotNull
    java.lang.String userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String followers(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String following(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String search(@org.jetbrains.annotations.NotNull
    java.lang.String query, @org.jetbrains.annotations.NotNull
    java.lang.String type) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String comments(@org.jetbrains.annotations.NotNull
    java.lang.String entityId, @org.jetbrains.annotations.NotNull
    java.lang.String entityType, int page) {
        return null;
    }
}