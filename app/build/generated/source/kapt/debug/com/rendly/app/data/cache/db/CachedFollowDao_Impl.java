package com.rendly.app.data.cache.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CachedFollowDao_Impl implements CachedFollowDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedFollowEntity> __insertionAdapterOfCachedFollowEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByUser;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public CachedFollowDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedFollowEntity = new EntityInsertionAdapter<CachedFollowEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_follows` (`follower_id`,`following_id`,`created_at`,`cached_at`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedFollowEntity entity) {
        if (entity.getFollowerId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getFollowerId());
        }
        if (entity.getFollowingId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getFollowingId());
        }
        if (entity.getCreatedAt() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getCreatedAt());
        }
        statement.bindLong(4, entity.getCachedAt());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_follows WHERE follower_id = ? AND following_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_follows WHERE follower_id = ? OR following_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_follows";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final CachedFollowEntity follow,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedFollowEntity.insert(follow);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<CachedFollowEntity> follows,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedFollowEntity.insert(follows);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String followerId, final String followingId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        if (followerId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, followerId);
        }
        _argIndex = 2;
        if (followingId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, followingId);
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
  public Object deleteByUser(final String userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByUser.acquire();
        int _argIndex = 1;
        if (userId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, userId);
        }
        _argIndex = 2;
        if (userId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfDeleteByUser.release(_stmt);
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
  public Object getFollowing(final String userId,
      final Continuation<? super List<CachedFollowEntity>> $completion) {
    final String _sql = "SELECT * FROM cached_follows WHERE follower_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedFollowEntity>>() {
      @Override
      @NonNull
      public List<CachedFollowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFollowerId = CursorUtil.getColumnIndexOrThrow(_cursor, "follower_id");
          final int _cursorIndexOfFollowingId = CursorUtil.getColumnIndexOrThrow(_cursor, "following_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final List<CachedFollowEntity> _result = new ArrayList<CachedFollowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedFollowEntity _item;
            final String _tmpFollowerId;
            if (_cursor.isNull(_cursorIndexOfFollowerId)) {
              _tmpFollowerId = null;
            } else {
              _tmpFollowerId = _cursor.getString(_cursorIndexOfFollowerId);
            }
            final String _tmpFollowingId;
            if (_cursor.isNull(_cursorIndexOfFollowingId)) {
              _tmpFollowingId = null;
            } else {
              _tmpFollowingId = _cursor.getString(_cursorIndexOfFollowingId);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new CachedFollowEntity(_tmpFollowerId,_tmpFollowingId,_tmpCreatedAt,_tmpCachedAt);
            _result.add(_item);
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
  public Object getFollowers(final String userId,
      final Continuation<? super List<CachedFollowEntity>> $completion) {
    final String _sql = "SELECT * FROM cached_follows WHERE following_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedFollowEntity>>() {
      @Override
      @NonNull
      public List<CachedFollowEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFollowerId = CursorUtil.getColumnIndexOrThrow(_cursor, "follower_id");
          final int _cursorIndexOfFollowingId = CursorUtil.getColumnIndexOrThrow(_cursor, "following_id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final List<CachedFollowEntity> _result = new ArrayList<CachedFollowEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedFollowEntity _item;
            final String _tmpFollowerId;
            if (_cursor.isNull(_cursorIndexOfFollowerId)) {
              _tmpFollowerId = null;
            } else {
              _tmpFollowerId = _cursor.getString(_cursorIndexOfFollowerId);
            }
            final String _tmpFollowingId;
            if (_cursor.isNull(_cursorIndexOfFollowingId)) {
              _tmpFollowingId = null;
            } else {
              _tmpFollowingId = _cursor.getString(_cursorIndexOfFollowingId);
            }
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new CachedFollowEntity(_tmpFollowerId,_tmpFollowingId,_tmpCreatedAt,_tmpCachedAt);
            _result.add(_item);
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
  public Object isFollowing(final String followerId, final String followingId,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "\n"
            + "        SELECT EXISTS(\n"
            + "            SELECT 1 FROM cached_follows \n"
            + "            WHERE follower_id = ? AND following_id = ?\n"
            + "        )\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (followerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, followerId);
    }
    _argIndex = 2;
    if (followingId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, followingId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp == null ? null : _tmp != 0;
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
  public Flow<Boolean> observeIsFollowing(final String followerId, final String followingId) {
    final String _sql = "\n"
            + "        SELECT EXISTS(\n"
            + "            SELECT 1 FROM cached_follows \n"
            + "            WHERE follower_id = ? AND following_id = ?\n"
            + "        )\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (followerId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, followerId);
    }
    _argIndex = 2;
    if (followingId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, followingId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_follows"}, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp == null ? null : _tmp != 0;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
