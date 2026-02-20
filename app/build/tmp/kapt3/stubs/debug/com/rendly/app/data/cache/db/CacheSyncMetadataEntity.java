package com.rendly.app.data.cache.db;

import androidx.room.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B7\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u0014\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J=\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\b\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u001bH\u00d6\u0001J\t\u0010\u001c\u001a\u00020\u0003H\u00d6\u0001R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0016\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0016\u0010\b\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000b\u00a8\u0006\u001d"}, d2 = {"Lcom/rendly/app/data/cache/db/CacheSyncMetadataEntity;", "", "cacheKey", "", "lastSyncAt", "", "lastVersion", "etag", "syncStatus", "(Ljava/lang/String;JJLjava/lang/String;Ljava/lang/String;)V", "getCacheKey", "()Ljava/lang/String;", "getEtag", "getLastSyncAt", "()J", "getLastVersion", "getSyncStatus", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
@androidx.room.Entity(tableName = "cache_sync_metadata", indices = {@androidx.room.Index(value = {"last_sync_at"})})
public final class CacheSyncMetadataEntity {
    @androidx.room.PrimaryKey
    @androidx.room.ColumnInfo(name = "cache_key")
    @org.jetbrains.annotations.NotNull
    private final java.lang.String cacheKey = null;
    @androidx.room.ColumnInfo(name = "last_sync_at")
    private final long lastSyncAt = 0L;
    @androidx.room.ColumnInfo(name = "last_version")
    private final long lastVersion = 0L;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String etag = null;
    @androidx.room.ColumnInfo(name = "sync_status")
    @org.jetbrains.annotations.NotNull
    private final java.lang.String syncStatus = null;
    
    public CacheSyncMetadataEntity(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, long lastSyncAt, long lastVersion, @org.jetbrains.annotations.Nullable
    java.lang.String etag, @org.jetbrains.annotations.NotNull
    java.lang.String syncStatus) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCacheKey() {
        return null;
    }
    
    public final long getLastSyncAt() {
        return 0L;
    }
    
    public final long getLastVersion() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getEtag() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getSyncStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final long component2() {
        return 0L;
    }
    
    public final long component3() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CacheSyncMetadataEntity copy(@org.jetbrains.annotations.NotNull
    java.lang.String cacheKey, long lastSyncAt, long lastVersion, @org.jetbrains.annotations.Nullable
    java.lang.String etag, @org.jetbrains.annotations.NotNull
    java.lang.String syncStatus) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}