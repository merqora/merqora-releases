package com.rendly.app.data.cache.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CacheSyncMetadataDao_Impl implements CacheSyncMetadataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CacheSyncMetadataEntity> __insertionAdapterOfCacheSyncMetadataEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMatching;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public CacheSyncMetadataDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCacheSyncMetadataEntity = new EntityInsertionAdapter<CacheSyncMetadataEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cache_sync_metadata` (`cache_key`,`last_sync_at`,`last_version`,`etag`,`sync_status`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CacheSyncMetadataEntity entity) {
        if (entity.getCacheKey() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getCacheKey());
        }
        statement.bindLong(2, entity.getLastSyncAt());
        statement.bindLong(3, entity.getLastVersion());
        if (entity.getEtag() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEtag());
        }
        if (entity.getSyncStatus() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getSyncStatus());
        }
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cache_sync_metadata WHERE cache_key = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMatching = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cache_sync_metadata WHERE cache_key LIKE ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cache_sync_metadata";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final CacheSyncMetadataEntity metadata,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCacheSyncMetadataEntity.insert(metadata);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String cacheKey, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        if (cacheKey == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, cacheKey);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMatching(final String pattern, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMatching.acquire();
        int _argIndex = 1;
        if (pattern == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, pattern);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMatching.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final String cacheKey,
      final Continuation<? super CacheSyncMetadataEntity> $completion) {
    final String _sql = "SELECT * FROM cache_sync_metadata WHERE cache_key = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (cacheKey == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cacheKey);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CacheSyncMetadataEntity>() {
      @Override
      @Nullable
      public CacheSyncMetadataEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCacheKey = CursorUtil.getColumnIndexOrThrow(_cursor, "cache_key");
          final int _cursorIndexOfLastSyncAt = CursorUtil.getColumnIndexOrThrow(_cursor, "last_sync_at");
          final int _cursorIndexOfLastVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "last_version");
          final int _cursorIndexOfEtag = CursorUtil.getColumnIndexOrThrow(_cursor, "etag");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_status");
          final CacheSyncMetadataEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpCacheKey;
            if (_cursor.isNull(_cursorIndexOfCacheKey)) {
              _tmpCacheKey = null;
            } else {
              _tmpCacheKey = _cursor.getString(_cursorIndexOfCacheKey);
            }
            final long _tmpLastSyncAt;
            _tmpLastSyncAt = _cursor.getLong(_cursorIndexOfLastSyncAt);
            final long _tmpLastVersion;
            _tmpLastVersion = _cursor.getLong(_cursorIndexOfLastVersion);
            final String _tmpEtag;
            if (_cursor.isNull(_cursorIndexOfEtag)) {
              _tmpEtag = null;
            } else {
              _tmpEtag = _cursor.getString(_cursorIndexOfEtag);
            }
            final String _tmpSyncStatus;
            if (_cursor.isNull(_cursorIndexOfSyncStatus)) {
              _tmpSyncStatus = null;
            } else {
              _tmpSyncStatus = _cursor.getString(_cursorIndexOfSyncStatus);
            }
            _result = new CacheSyncMetadataEntity(_tmpCacheKey,_tmpLastSyncAt,_tmpLastVersion,_tmpEtag,_tmpSyncStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLastSyncTime(final String cacheKey,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT last_sync_at FROM cache_sync_metadata WHERE cache_key = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (cacheKey == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cacheKey);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getLong(0);
            }
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
