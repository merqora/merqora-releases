package com.rendly.app.data.cache.db;

import androidx.room.*;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u000b\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\n\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\r\u001a\u00020\u00062\u0006\u0010\u000e\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\"\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\b0\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0016J0\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\u0006\u0010\f\u001a\u00020\b2\b\b\u0002\u0010\u0018\u001a\u00020\u00032\b\b\u0002\u0010\u0019\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ(\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0018\u001a\u00020\u00032\b\b\u0002\u0010\u0019\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u001e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u001f2\u0006\u0010\u0007\u001a\u00020\bH\'J\u001c\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00140\u001f2\u0006\u0010\f\u001a\u00020\bH\'J\u0014\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00140\u001fH\'J\u001e\u0010\"\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010#J\u001e\u0010$\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010#J\u0016\u0010%\u001a\u00020\u00062\u0006\u0010&\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\'J\u001c\u0010(\u001a\u00020\u00062\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0016\u00a8\u0006*"}, d2 = {"Lcom/rendly/app/data/cache/db/CachedPostDao;", "", "count", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "delete", "", "postId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "deleteByUserId", "userId", "deleteExpired", "expiredBefore", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getById", "Lcom/rendly/app/data/cache/db/CachedPostEntity;", "getByIds", "", "postIds", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getByUserId", "limit", "offset", "(Ljava/lang/String;IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFeed", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLatestVersion", "observeById", "Lkotlinx/coroutines/flow/Flow;", "observeByUserId", "observeFeed", "updateLikesCount", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateSavesCount", "upsert", "post", "(Lcom/rendly/app/data/cache/db/CachedPostEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertAll", "posts", "app_debug"})
@androidx.room.Dao
public abstract interface CachedPostDao {
    
    @androidx.room.Query(value = "SELECT * FROM cached_posts WHERE id = :postId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getById(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.rendly.app.data.cache.db.CachedPostEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM cached_posts WHERE id = :postId")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<com.rendly.app.data.cache.db.CachedPostEntity> observeById(@org.jetbrains.annotations.NotNull
    java.lang.String postId);
    
    @androidx.room.Query(value = "\n        SELECT * FROM cached_posts \n        WHERE status = \'active\' \n        ORDER BY created_at DESC \n        LIMIT :limit OFFSET :offset\n    ")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getFeed(int limit, int offset, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.cache.db.CachedPostEntity>> $completion);
    
    @androidx.room.Query(value = "\n        SELECT * FROM cached_posts \n        WHERE status = \'active\' \n        ORDER BY created_at DESC\n    ")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rendly.app.data.cache.db.CachedPostEntity>> observeFeed();
    
    @androidx.room.Query(value = "\n        SELECT * FROM cached_posts \n        WHERE user_id = :userId AND status = \'active\'\n        ORDER BY created_at DESC \n        LIMIT :limit OFFSET :offset\n    ")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getByUserId(@org.jetbrains.annotations.NotNull
    java.lang.String userId, int limit, int offset, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.cache.db.CachedPostEntity>> $completion);
    
    @androidx.room.Query(value = "\n        SELECT * FROM cached_posts \n        WHERE user_id = :userId AND status = \'active\'\n        ORDER BY created_at DESC\n    ")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rendly.app.data.cache.db.CachedPostEntity>> observeByUserId(@org.jetbrains.annotations.NotNull
    java.lang.String userId);
    
    @androidx.room.Query(value = "SELECT * FROM cached_posts WHERE id IN (:postIds)")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getByIds(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> postIds, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.cache.db.CachedPostEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.CachedPostEntity post, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object upsertAll(@org.jetbrains.annotations.NotNull
    java.util.List<com.rendly.app.data.cache.db.CachedPostEntity> posts, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE cached_posts SET likes_count = :count WHERE id = :postId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateLikesCount(@org.jetbrains.annotations.NotNull
    java.lang.String postId, int count, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE cached_posts SET saves_count = :count WHERE id = :postId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateSavesCount(@org.jetbrains.annotations.NotNull
    java.lang.String postId, int count, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_posts WHERE id = :postId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_posts WHERE user_id = :userId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteByUserId(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_posts WHERE cached_at < :expiredBefore")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteExpired(long expiredBefore, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_posts")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT MAX(version) FROM cached_posts")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getLatestVersion(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM cached_posts")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object count(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}