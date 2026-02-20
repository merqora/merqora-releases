package com.rendly.app.data.cache.db;

import androidx.room.*;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\t\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fH\u00a7@\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0011\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\u0012\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0014J4\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u00172\b\b\u0002\u0010\u0019\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u0014\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\u001cH\'J\u0016\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u0013\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0014J(\u0010\u001e\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00020\u00172\b\b\u0002\u0010\u0019\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u001f\u00a8\u0006 "}, d2 = {"Lcom/rendly/app/data/cache/db/PendingOperationDao;", "", "delete", "", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteCompleted", "deleteFailedAfterRetries", "maxRetries", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPending", "", "Lcom/rendly/app/data/cache/db/PendingOperationEntity;", "getPendingCount", "insert", "operation", "(Lcom/rendly/app/data/cache/db/PendingOperationEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markFailed", "status", "", "errorMessage", "attemptAt", "(JLjava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observePending", "Lkotlinx/coroutines/flow/Flow;", "update", "updateStatus", "(JLjava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao
public abstract interface PendingOperationDao {
    
    @androidx.room.Query(value = "SELECT * FROM pending_operations WHERE status = \'pending\' ORDER BY created_at ASC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getPending(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.rendly.app.data.cache.db.PendingOperationEntity>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM pending_operations WHERE status = \'pending\' ORDER BY created_at ASC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.rendly.app.data.cache.db.PendingOperationEntity>> observePending();
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM pending_operations WHERE status = \'pending\'")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getPendingCount(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Insert
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.PendingOperationEntity operation, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.cache.db.PendingOperationEntity operation, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE pending_operations SET status = :status, last_attempt_at = :attemptAt WHERE id = :id")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateStatus(long id, @org.jetbrains.annotations.NotNull
    java.lang.String status, long attemptAt, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "\n        UPDATE pending_operations \n        SET status = :status, retry_count = retry_count + 1, \n            error_message = :errorMessage, last_attempt_at = :attemptAt \n        WHERE id = :id\n    ")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object markFailed(long id, @org.jetbrains.annotations.NotNull
    java.lang.String status, @org.jetbrains.annotations.Nullable
    java.lang.String errorMessage, long attemptAt, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM pending_operations WHERE id = :id")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object delete(long id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM pending_operations WHERE status = \'success\'")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteCompleted(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM pending_operations WHERE status = \'error\' AND retry_count >= :maxRetries")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteFailedAfterRetries(int maxRetries, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM pending_operations")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}