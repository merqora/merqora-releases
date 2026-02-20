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
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
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
public final class CachedRendDao_Impl implements CachedRendDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedRendEntity> __insertionAdapterOfCachedRendEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLikesCount;

  private final SharedSQLiteStatement __preparedStmtOfUpdateViewsCount;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpired;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public CachedRendDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedRendEntity = new EntityInsertionAdapter<CachedRendEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_rends` (`id`,`user_id`,`title`,`description`,`video_url`,`thumbnail_url`,`product_title`,`product_price`,`product_link`,`product_image`,`product_id`,`duration`,`status`,`likes_count`,`reviews_count`,`views_count`,`shares_count`,`saves_count`,`created_at`,`updated_at`,`cached_at`,`version`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedRendEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getUserId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUserId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        if (entity.getVideoUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVideoUrl());
        }
        if (entity.getThumbnailUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getThumbnailUrl());
        }
        if (entity.getProductTitle() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getProductTitle());
        }
        if (entity.getProductPrice() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getProductPrice());
        }
        if (entity.getProductLink() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getProductLink());
        }
        if (entity.getProductImage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getProductImage());
        }
        if (entity.getProductId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getProductId());
        }
        statement.bindLong(12, entity.getDuration());
        if (entity.getStatus() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getStatus());
        }
        statement.bindLong(14, entity.getLikesCount());
        statement.bindLong(15, entity.getReviewsCount());
        statement.bindLong(16, entity.getViewsCount());
        statement.bindLong(17, entity.getSharesCount());
        statement.bindLong(18, entity.getSavesCount());
        if (entity.getCreatedAt() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getUpdatedAt());
        }
        statement.bindLong(21, entity.getCachedAt());
        statement.bindLong(22, entity.getVersion());
      }
    };
    this.__preparedStmtOfUpdateLikesCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE cached_rends SET likes_count = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateViewsCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE cached_rends SET views_count = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_rends WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpired = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_rends WHERE cached_at < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_rends";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final CachedRendEntity rend, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedRendEntity.insert(rend);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<CachedRendEntity> rends,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedRendEntity.insert(rends);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLikesCount(final String rendId, final int count,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLikesCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, count);
        _argIndex = 2;
        if (rendId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, rendId);
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
          __preparedStmtOfUpdateLikesCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateViewsCount(final String rendId, final int count,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateViewsCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, count);
        _argIndex = 2;
        if (rendId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, rendId);
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
          __preparedStmtOfUpdateViewsCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String rendId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        if (rendId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, rendId);
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
  public Object deleteExpired(final long expiredBefore,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpired.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, expiredBefore);
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
          __preparedStmtOfDeleteExpired.release(_stmt);
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
  public Object getById(final String rendId,
      final Continuation<? super CachedRendEntity> $completion) {
    final String _sql = "SELECT * FROM cached_rends WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (rendId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, rendId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedRendEntity>() {
      @Override
      @Nullable
      public CachedRendEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "video_url");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_url");
          final int _cursorIndexOfProductTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "product_title");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "product_price");
          final int _cursorIndexOfProductLink = CursorUtil.getColumnIndexOrThrow(_cursor, "product_link");
          final int _cursorIndexOfProductImage = CursorUtil.getColumnIndexOrThrow(_cursor, "product_image");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likes_count");
          final int _cursorIndexOfReviewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviews_count");
          final int _cursorIndexOfViewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "views_count");
          final int _cursorIndexOfSharesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "shares_count");
          final int _cursorIndexOfSavesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "saves_count");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final CachedRendEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final String _tmpProductTitle;
            if (_cursor.isNull(_cursorIndexOfProductTitle)) {
              _tmpProductTitle = null;
            } else {
              _tmpProductTitle = _cursor.getString(_cursorIndexOfProductTitle);
            }
            final Double _tmpProductPrice;
            if (_cursor.isNull(_cursorIndexOfProductPrice)) {
              _tmpProductPrice = null;
            } else {
              _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            }
            final String _tmpProductLink;
            if (_cursor.isNull(_cursorIndexOfProductLink)) {
              _tmpProductLink = null;
            } else {
              _tmpProductLink = _cursor.getString(_cursorIndexOfProductLink);
            }
            final String _tmpProductImage;
            if (_cursor.isNull(_cursorIndexOfProductImage)) {
              _tmpProductImage = null;
            } else {
              _tmpProductImage = _cursor.getString(_cursorIndexOfProductImage);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final int _tmpReviewsCount;
            _tmpReviewsCount = _cursor.getInt(_cursorIndexOfReviewsCount);
            final int _tmpViewsCount;
            _tmpViewsCount = _cursor.getInt(_cursorIndexOfViewsCount);
            final int _tmpSharesCount;
            _tmpSharesCount = _cursor.getInt(_cursorIndexOfSharesCount);
            final int _tmpSavesCount;
            _tmpSavesCount = _cursor.getInt(_cursorIndexOfSavesCount);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            _result = new CachedRendEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDescription,_tmpVideoUrl,_tmpThumbnailUrl,_tmpProductTitle,_tmpProductPrice,_tmpProductLink,_tmpProductImage,_tmpProductId,_tmpDuration,_tmpStatus,_tmpLikesCount,_tmpReviewsCount,_tmpViewsCount,_tmpSharesCount,_tmpSavesCount,_tmpCreatedAt,_tmpUpdatedAt,_tmpCachedAt,_tmpVersion);
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
  public Flow<CachedRendEntity> observeById(final String rendId) {
    final String _sql = "SELECT * FROM cached_rends WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (rendId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, rendId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_rends"}, new Callable<CachedRendEntity>() {
      @Override
      @Nullable
      public CachedRendEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "video_url");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_url");
          final int _cursorIndexOfProductTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "product_title");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "product_price");
          final int _cursorIndexOfProductLink = CursorUtil.getColumnIndexOrThrow(_cursor, "product_link");
          final int _cursorIndexOfProductImage = CursorUtil.getColumnIndexOrThrow(_cursor, "product_image");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likes_count");
          final int _cursorIndexOfReviewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviews_count");
          final int _cursorIndexOfViewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "views_count");
          final int _cursorIndexOfSharesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "shares_count");
          final int _cursorIndexOfSavesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "saves_count");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final CachedRendEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final String _tmpProductTitle;
            if (_cursor.isNull(_cursorIndexOfProductTitle)) {
              _tmpProductTitle = null;
            } else {
              _tmpProductTitle = _cursor.getString(_cursorIndexOfProductTitle);
            }
            final Double _tmpProductPrice;
            if (_cursor.isNull(_cursorIndexOfProductPrice)) {
              _tmpProductPrice = null;
            } else {
              _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            }
            final String _tmpProductLink;
            if (_cursor.isNull(_cursorIndexOfProductLink)) {
              _tmpProductLink = null;
            } else {
              _tmpProductLink = _cursor.getString(_cursorIndexOfProductLink);
            }
            final String _tmpProductImage;
            if (_cursor.isNull(_cursorIndexOfProductImage)) {
              _tmpProductImage = null;
            } else {
              _tmpProductImage = _cursor.getString(_cursorIndexOfProductImage);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final int _tmpReviewsCount;
            _tmpReviewsCount = _cursor.getInt(_cursorIndexOfReviewsCount);
            final int _tmpViewsCount;
            _tmpViewsCount = _cursor.getInt(_cursorIndexOfViewsCount);
            final int _tmpSharesCount;
            _tmpSharesCount = _cursor.getInt(_cursorIndexOfSharesCount);
            final int _tmpSavesCount;
            _tmpSavesCount = _cursor.getInt(_cursorIndexOfSavesCount);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            _result = new CachedRendEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDescription,_tmpVideoUrl,_tmpThumbnailUrl,_tmpProductTitle,_tmpProductPrice,_tmpProductLink,_tmpProductImage,_tmpProductId,_tmpDuration,_tmpStatus,_tmpLikesCount,_tmpReviewsCount,_tmpViewsCount,_tmpSharesCount,_tmpSavesCount,_tmpCreatedAt,_tmpUpdatedAt,_tmpCachedAt,_tmpVersion);
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

  @Override
  public Object getFeed(final int limit, final int offset,
      final Continuation<? super List<CachedRendEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM cached_rends \n"
            + "        WHERE status = 'active' \n"
            + "        ORDER BY created_at DESC \n"
            + "        LIMIT ? OFFSET ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 2;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedRendEntity>>() {
      @Override
      @NonNull
      public List<CachedRendEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "video_url");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_url");
          final int _cursorIndexOfProductTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "product_title");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "product_price");
          final int _cursorIndexOfProductLink = CursorUtil.getColumnIndexOrThrow(_cursor, "product_link");
          final int _cursorIndexOfProductImage = CursorUtil.getColumnIndexOrThrow(_cursor, "product_image");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likes_count");
          final int _cursorIndexOfReviewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviews_count");
          final int _cursorIndexOfViewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "views_count");
          final int _cursorIndexOfSharesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "shares_count");
          final int _cursorIndexOfSavesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "saves_count");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final List<CachedRendEntity> _result = new ArrayList<CachedRendEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedRendEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final String _tmpProductTitle;
            if (_cursor.isNull(_cursorIndexOfProductTitle)) {
              _tmpProductTitle = null;
            } else {
              _tmpProductTitle = _cursor.getString(_cursorIndexOfProductTitle);
            }
            final Double _tmpProductPrice;
            if (_cursor.isNull(_cursorIndexOfProductPrice)) {
              _tmpProductPrice = null;
            } else {
              _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            }
            final String _tmpProductLink;
            if (_cursor.isNull(_cursorIndexOfProductLink)) {
              _tmpProductLink = null;
            } else {
              _tmpProductLink = _cursor.getString(_cursorIndexOfProductLink);
            }
            final String _tmpProductImage;
            if (_cursor.isNull(_cursorIndexOfProductImage)) {
              _tmpProductImage = null;
            } else {
              _tmpProductImage = _cursor.getString(_cursorIndexOfProductImage);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final int _tmpReviewsCount;
            _tmpReviewsCount = _cursor.getInt(_cursorIndexOfReviewsCount);
            final int _tmpViewsCount;
            _tmpViewsCount = _cursor.getInt(_cursorIndexOfViewsCount);
            final int _tmpSharesCount;
            _tmpSharesCount = _cursor.getInt(_cursorIndexOfSharesCount);
            final int _tmpSavesCount;
            _tmpSavesCount = _cursor.getInt(_cursorIndexOfSavesCount);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            _item = new CachedRendEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDescription,_tmpVideoUrl,_tmpThumbnailUrl,_tmpProductTitle,_tmpProductPrice,_tmpProductLink,_tmpProductImage,_tmpProductId,_tmpDuration,_tmpStatus,_tmpLikesCount,_tmpReviewsCount,_tmpViewsCount,_tmpSharesCount,_tmpSavesCount,_tmpCreatedAt,_tmpUpdatedAt,_tmpCachedAt,_tmpVersion);
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
  public Flow<List<CachedRendEntity>> observeFeed() {
    final String _sql = "\n"
            + "        SELECT * FROM cached_rends \n"
            + "        WHERE status = 'active' \n"
            + "        ORDER BY created_at DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_rends"}, new Callable<List<CachedRendEntity>>() {
      @Override
      @NonNull
      public List<CachedRendEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "video_url");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_url");
          final int _cursorIndexOfProductTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "product_title");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "product_price");
          final int _cursorIndexOfProductLink = CursorUtil.getColumnIndexOrThrow(_cursor, "product_link");
          final int _cursorIndexOfProductImage = CursorUtil.getColumnIndexOrThrow(_cursor, "product_image");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likes_count");
          final int _cursorIndexOfReviewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviews_count");
          final int _cursorIndexOfViewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "views_count");
          final int _cursorIndexOfSharesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "shares_count");
          final int _cursorIndexOfSavesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "saves_count");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final List<CachedRendEntity> _result = new ArrayList<CachedRendEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedRendEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final String _tmpProductTitle;
            if (_cursor.isNull(_cursorIndexOfProductTitle)) {
              _tmpProductTitle = null;
            } else {
              _tmpProductTitle = _cursor.getString(_cursorIndexOfProductTitle);
            }
            final Double _tmpProductPrice;
            if (_cursor.isNull(_cursorIndexOfProductPrice)) {
              _tmpProductPrice = null;
            } else {
              _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            }
            final String _tmpProductLink;
            if (_cursor.isNull(_cursorIndexOfProductLink)) {
              _tmpProductLink = null;
            } else {
              _tmpProductLink = _cursor.getString(_cursorIndexOfProductLink);
            }
            final String _tmpProductImage;
            if (_cursor.isNull(_cursorIndexOfProductImage)) {
              _tmpProductImage = null;
            } else {
              _tmpProductImage = _cursor.getString(_cursorIndexOfProductImage);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final int _tmpReviewsCount;
            _tmpReviewsCount = _cursor.getInt(_cursorIndexOfReviewsCount);
            final int _tmpViewsCount;
            _tmpViewsCount = _cursor.getInt(_cursorIndexOfViewsCount);
            final int _tmpSharesCount;
            _tmpSharesCount = _cursor.getInt(_cursorIndexOfSharesCount);
            final int _tmpSavesCount;
            _tmpSavesCount = _cursor.getInt(_cursorIndexOfSavesCount);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            _item = new CachedRendEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDescription,_tmpVideoUrl,_tmpThumbnailUrl,_tmpProductTitle,_tmpProductPrice,_tmpProductLink,_tmpProductImage,_tmpProductId,_tmpDuration,_tmpStatus,_tmpLikesCount,_tmpReviewsCount,_tmpViewsCount,_tmpSharesCount,_tmpSavesCount,_tmpCreatedAt,_tmpUpdatedAt,_tmpCachedAt,_tmpVersion);
            _result.add(_item);
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

  @Override
  public Object getByUserId(final String userId, final int limit, final int offset,
      final Continuation<? super List<CachedRendEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM cached_rends \n"
            + "        WHERE user_id = ? AND status = 'active'\n"
            + "        ORDER BY created_at DESC \n"
            + "        LIMIT ? OFFSET ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 3;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedRendEntity>>() {
      @Override
      @NonNull
      public List<CachedRendEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "video_url");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_url");
          final int _cursorIndexOfProductTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "product_title");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "product_price");
          final int _cursorIndexOfProductLink = CursorUtil.getColumnIndexOrThrow(_cursor, "product_link");
          final int _cursorIndexOfProductImage = CursorUtil.getColumnIndexOrThrow(_cursor, "product_image");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likes_count");
          final int _cursorIndexOfReviewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviews_count");
          final int _cursorIndexOfViewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "views_count");
          final int _cursorIndexOfSharesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "shares_count");
          final int _cursorIndexOfSavesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "saves_count");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final List<CachedRendEntity> _result = new ArrayList<CachedRendEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedRendEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final String _tmpProductTitle;
            if (_cursor.isNull(_cursorIndexOfProductTitle)) {
              _tmpProductTitle = null;
            } else {
              _tmpProductTitle = _cursor.getString(_cursorIndexOfProductTitle);
            }
            final Double _tmpProductPrice;
            if (_cursor.isNull(_cursorIndexOfProductPrice)) {
              _tmpProductPrice = null;
            } else {
              _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            }
            final String _tmpProductLink;
            if (_cursor.isNull(_cursorIndexOfProductLink)) {
              _tmpProductLink = null;
            } else {
              _tmpProductLink = _cursor.getString(_cursorIndexOfProductLink);
            }
            final String _tmpProductImage;
            if (_cursor.isNull(_cursorIndexOfProductImage)) {
              _tmpProductImage = null;
            } else {
              _tmpProductImage = _cursor.getString(_cursorIndexOfProductImage);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final int _tmpReviewsCount;
            _tmpReviewsCount = _cursor.getInt(_cursorIndexOfReviewsCount);
            final int _tmpViewsCount;
            _tmpViewsCount = _cursor.getInt(_cursorIndexOfViewsCount);
            final int _tmpSharesCount;
            _tmpSharesCount = _cursor.getInt(_cursorIndexOfSharesCount);
            final int _tmpSavesCount;
            _tmpSavesCount = _cursor.getInt(_cursorIndexOfSavesCount);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            _item = new CachedRendEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDescription,_tmpVideoUrl,_tmpThumbnailUrl,_tmpProductTitle,_tmpProductPrice,_tmpProductLink,_tmpProductImage,_tmpProductId,_tmpDuration,_tmpStatus,_tmpLikesCount,_tmpReviewsCount,_tmpViewsCount,_tmpSharesCount,_tmpSavesCount,_tmpCreatedAt,_tmpUpdatedAt,_tmpCachedAt,_tmpVersion);
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
  public Flow<List<CachedRendEntity>> observeByUserId(final String userId) {
    final String _sql = "\n"
            + "        SELECT * FROM cached_rends \n"
            + "        WHERE user_id = ? AND status = 'active'\n"
            + "        ORDER BY created_at DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_rends"}, new Callable<List<CachedRendEntity>>() {
      @Override
      @NonNull
      public List<CachedRendEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfVideoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "video_url");
          final int _cursorIndexOfThumbnailUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_url");
          final int _cursorIndexOfProductTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "product_title");
          final int _cursorIndexOfProductPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "product_price");
          final int _cursorIndexOfProductLink = CursorUtil.getColumnIndexOrThrow(_cursor, "product_link");
          final int _cursorIndexOfProductImage = CursorUtil.getColumnIndexOrThrow(_cursor, "product_image");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "product_id");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLikesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "likes_count");
          final int _cursorIndexOfReviewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "reviews_count");
          final int _cursorIndexOfViewsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "views_count");
          final int _cursorIndexOfSharesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "shares_count");
          final int _cursorIndexOfSavesCount = CursorUtil.getColumnIndexOrThrow(_cursor, "saves_count");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final List<CachedRendEntity> _result = new ArrayList<CachedRendEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedRendEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpVideoUrl;
            if (_cursor.isNull(_cursorIndexOfVideoUrl)) {
              _tmpVideoUrl = null;
            } else {
              _tmpVideoUrl = _cursor.getString(_cursorIndexOfVideoUrl);
            }
            final String _tmpThumbnailUrl;
            if (_cursor.isNull(_cursorIndexOfThumbnailUrl)) {
              _tmpThumbnailUrl = null;
            } else {
              _tmpThumbnailUrl = _cursor.getString(_cursorIndexOfThumbnailUrl);
            }
            final String _tmpProductTitle;
            if (_cursor.isNull(_cursorIndexOfProductTitle)) {
              _tmpProductTitle = null;
            } else {
              _tmpProductTitle = _cursor.getString(_cursorIndexOfProductTitle);
            }
            final Double _tmpProductPrice;
            if (_cursor.isNull(_cursorIndexOfProductPrice)) {
              _tmpProductPrice = null;
            } else {
              _tmpProductPrice = _cursor.getDouble(_cursorIndexOfProductPrice);
            }
            final String _tmpProductLink;
            if (_cursor.isNull(_cursorIndexOfProductLink)) {
              _tmpProductLink = null;
            } else {
              _tmpProductLink = _cursor.getString(_cursorIndexOfProductLink);
            }
            final String _tmpProductImage;
            if (_cursor.isNull(_cursorIndexOfProductImage)) {
              _tmpProductImage = null;
            } else {
              _tmpProductImage = _cursor.getString(_cursorIndexOfProductImage);
            }
            final String _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            }
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final int _tmpLikesCount;
            _tmpLikesCount = _cursor.getInt(_cursorIndexOfLikesCount);
            final int _tmpReviewsCount;
            _tmpReviewsCount = _cursor.getInt(_cursorIndexOfReviewsCount);
            final int _tmpViewsCount;
            _tmpViewsCount = _cursor.getInt(_cursorIndexOfViewsCount);
            final int _tmpSharesCount;
            _tmpSharesCount = _cursor.getInt(_cursorIndexOfSharesCount);
            final int _tmpSavesCount;
            _tmpSavesCount = _cursor.getInt(_cursorIndexOfSavesCount);
            final String _tmpCreatedAt;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmpCreatedAt = null;
            } else {
              _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            }
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            _item = new CachedRendEntity(_tmpId,_tmpUserId,_tmpTitle,_tmpDescription,_tmpVideoUrl,_tmpThumbnailUrl,_tmpProductTitle,_tmpProductPrice,_tmpProductLink,_tmpProductImage,_tmpProductId,_tmpDuration,_tmpStatus,_tmpLikesCount,_tmpReviewsCount,_tmpViewsCount,_tmpSharesCount,_tmpSavesCount,_tmpCreatedAt,_tmpUpdatedAt,_tmpCachedAt,_tmpVersion);
            _result.add(_item);
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

  @Override
  public Object getLatestVersion(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(version) FROM cached_rends";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM cached_rends";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
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
