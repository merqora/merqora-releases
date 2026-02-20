package com.rendly.app.data.cache.db;

import androidx.room.*;
import kotlinx.coroutines.flow.Flow;

/**
 * Data Access Objects for cache entities.
 * Optimized queries with Flow support for reactive updates.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\n\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\"\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00100\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\b0\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u0018\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u00162\u0006\u0010\u0007\u001a\u00020\bH\'J&\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00100\u00122\u0006\u0010\u0018\u001a\u00020\b2\b\b\u0002\u0010\u0019\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u0016\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u001c\u0010\u001e\u001a\u00020\u00062\f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00100\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0014\u00a8\u0006 "}, d2 = {"Lcom/rendly/app/data/cache/db/CachedUserDao;", "", "count", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "userId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "deleteExpired", "expiredBefore", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getById", "Lcom/rendly/app/data/cache/db/CachedUserEntity;", "getByIds", "", "userIds", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observeById", "Lkotlinx/coroutines/flow/Flow;", "searchByUsername", "query", "limit", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsert", "user", "(Lcom/rendly/app/data/cache/db/CachedUserEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertAll", "users", "app_debug"})
@androidx.room.Dao
public abstract interface CachedUserDao {
    
    @androidx.room.Query(value = "SELECT * FROM cached_users WHERE user_id = :userId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getById(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.cache.db.CachedUserEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM cached_users WHERE user_id = :userId")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<com.rendly.app.data.cache.db.CachedUserEntity> observeById(@org.jetbrains.annotations.NotNull
    java.lang.String userId);
    
    @androidx.room.Query(value = "SELECT * FROM cached_users WHERE user_id IN (:userIds)")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getByIds(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> userIds, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.cache.db.CachedUserEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM cached_users WHERE username LIKE \'%\' || :query || \'%\' LIMIT :limit")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object searchByUsername(@org.jetbrains.annotations.NotNull
    java.lang.String query, int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.cache.db.CachedUserEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.CachedUserEntity user, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object upsertAll(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.cache.db.CachedUserEntity> users, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_users WHERE user_id = :userId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_users WHERE cached_at < :expiredBefore")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteExpired(long expiredBefore, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_users")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM cached_users")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object count(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    /**
     * Data Access Objects for cache entities.
     * Optimized queries with Flow support for reactive updates.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}