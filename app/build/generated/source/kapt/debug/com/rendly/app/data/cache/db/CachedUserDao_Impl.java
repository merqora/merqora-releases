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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class CachedUserDao_Impl implements CachedUserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedUserEntity> __insertionAdapterOfCachedUserEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpired;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public CachedUserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedUserEntity = new EntityInsertionAdapter<CachedUserEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cached_users` (`user_id`,`username`,`nombre_tienda`,`descripcion`,`avatar_url`,`banner_url`,`email`,`genero`,`ubicacion`,`nombre`,`facebook`,`whatsapp`,`twitter`,`instagram`,`linkedin`,`tiktok`,`is_online`,`is_verified`,`tiene_tienda`,`cached_at`,`version`,`updated_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CachedUserEntity entity) {
        if (entity.getUserId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getUserId());
        }
        if (entity.getUsername() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUsername());
        }
        if (entity.getNombreTienda() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getNombreTienda());
        }
        if (entity.getDescripcion() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescripcion());
        }
        if (entity.getAvatarUrl() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAvatarUrl());
        }
        if (entity.getBannerUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBannerUrl());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEmail());
        }
        if (entity.getGenero() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getGenero());
        }
        if (entity.getUbicacion() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getUbicacion());
        }
        if (entity.getNombre() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNombre());
        }
        if (entity.getFacebook() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getFacebook());
        }
        if (entity.getWhatsapp() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getWhatsapp());
        }
        if (entity.getTwitter() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getTwitter());
        }
        if (entity.getInstagram() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getInstagram());
        }
        if (entity.getLinkedin() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getLinkedin());
        }
        if (entity.getTiktok() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getTiktok());
        }
        final int _tmp = entity.isOnline() ? 1 : 0;
        statement.bindLong(17, _tmp);
        final int _tmp_1 = entity.isVerified() ? 1 : 0;
        statement.bindLong(18, _tmp_1);
        final int _tmp_2 = entity.getTieneTienda() ? 1 : 0;
        statement.bindLong(19, _tmp_2);
        statement.bindLong(20, entity.getCachedAt());
        statement.bindLong(21, entity.getVersion());
        if (entity.getUpdatedAt() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getUpdatedAt());
        }
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_users WHERE user_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteExpired = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_users WHERE cached_at < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM cached_users";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final CachedUserEntity user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedUserEntity.insert(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<CachedUserEntity> users,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedUserEntity.insert(users);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
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
  public Object getById(final String userId,
      final Continuation<? super CachedUserEntity> $completion) {
    final String _sql = "SELECT * FROM cached_users WHERE user_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedUserEntity>() {
      @Override
      @Nullable
      public CachedUserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfNombreTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre_tienda");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatar_url");
          final int _cursorIndexOfBannerUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "banner_url");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfGenero = CursorUtil.getColumnIndexOrThrow(_cursor, "genero");
          final int _cursorIndexOfUbicacion = CursorUtil.getColumnIndexOrThrow(_cursor, "ubicacion");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfFacebook = CursorUtil.getColumnIndexOrThrow(_cursor, "facebook");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfTwitter = CursorUtil.getColumnIndexOrThrow(_cursor, "twitter");
          final int _cursorIndexOfInstagram = CursorUtil.getColumnIndexOrThrow(_cursor, "instagram");
          final int _cursorIndexOfLinkedin = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedin");
          final int _cursorIndexOfTiktok = CursorUtil.getColumnIndexOrThrow(_cursor, "tiktok");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfIsVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "is_verified");
          final int _cursorIndexOfTieneTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "tiene_tienda");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final CachedUserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpNombreTienda;
            if (_cursor.isNull(_cursorIndexOfNombreTienda)) {
              _tmpNombreTienda = null;
            } else {
              _tmpNombreTienda = _cursor.getString(_cursorIndexOfNombreTienda);
            }
            final String _tmpDescripcion;
            if (_cursor.isNull(_cursorIndexOfDescripcion)) {
              _tmpDescripcion = null;
            } else {
              _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final String _tmpBannerUrl;
            if (_cursor.isNull(_cursorIndexOfBannerUrl)) {
              _tmpBannerUrl = null;
            } else {
              _tmpBannerUrl = _cursor.getString(_cursorIndexOfBannerUrl);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpGenero;
            if (_cursor.isNull(_cursorIndexOfGenero)) {
              _tmpGenero = null;
            } else {
              _tmpGenero = _cursor.getString(_cursorIndexOfGenero);
            }
            final String _tmpUbicacion;
            if (_cursor.isNull(_cursorIndexOfUbicacion)) {
              _tmpUbicacion = null;
            } else {
              _tmpUbicacion = _cursor.getString(_cursorIndexOfUbicacion);
            }
            final String _tmpNombre;
            if (_cursor.isNull(_cursorIndexOfNombre)) {
              _tmpNombre = null;
            } else {
              _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            }
            final String _tmpFacebook;
            if (_cursor.isNull(_cursorIndexOfFacebook)) {
              _tmpFacebook = null;
            } else {
              _tmpFacebook = _cursor.getString(_cursorIndexOfFacebook);
            }
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpTwitter;
            if (_cursor.isNull(_cursorIndexOfTwitter)) {
              _tmpTwitter = null;
            } else {
              _tmpTwitter = _cursor.getString(_cursorIndexOfTwitter);
            }
            final String _tmpInstagram;
            if (_cursor.isNull(_cursorIndexOfInstagram)) {
              _tmpInstagram = null;
            } else {
              _tmpInstagram = _cursor.getString(_cursorIndexOfInstagram);
            }
            final String _tmpLinkedin;
            if (_cursor.isNull(_cursorIndexOfLinkedin)) {
              _tmpLinkedin = null;
            } else {
              _tmpLinkedin = _cursor.getString(_cursorIndexOfLinkedin);
            }
            final String _tmpTiktok;
            if (_cursor.isNull(_cursorIndexOfTiktok)) {
              _tmpTiktok = null;
            } else {
              _tmpTiktok = _cursor.getString(_cursorIndexOfTiktok);
            }
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final boolean _tmpIsVerified;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsVerified);
            _tmpIsVerified = _tmp_1 != 0;
            final boolean _tmpTieneTienda;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfTieneTienda);
            _tmpTieneTienda = _tmp_2 != 0;
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _result = new CachedUserEntity(_tmpUserId,_tmpUsername,_tmpNombreTienda,_tmpDescripcion,_tmpAvatarUrl,_tmpBannerUrl,_tmpEmail,_tmpGenero,_tmpUbicacion,_tmpNombre,_tmpFacebook,_tmpWhatsapp,_tmpTwitter,_tmpInstagram,_tmpLinkedin,_tmpTiktok,_tmpIsOnline,_tmpIsVerified,_tmpTieneTienda,_tmpCachedAt,_tmpVersion,_tmpUpdatedAt);
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
  public Flow<CachedUserEntity> observeById(final String userId) {
    final String _sql = "SELECT * FROM cached_users WHERE user_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cached_users"}, new Callable<CachedUserEntity>() {
      @Override
      @Nullable
      public CachedUserEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfNombreTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre_tienda");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatar_url");
          final int _cursorIndexOfBannerUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "banner_url");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfGenero = CursorUtil.getColumnIndexOrThrow(_cursor, "genero");
          final int _cursorIndexOfUbicacion = CursorUtil.getColumnIndexOrThrow(_cursor, "ubicacion");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfFacebook = CursorUtil.getColumnIndexOrThrow(_cursor, "facebook");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfTwitter = CursorUtil.getColumnIndexOrThrow(_cursor, "twitter");
          final int _cursorIndexOfInstagram = CursorUtil.getColumnIndexOrThrow(_cursor, "instagram");
          final int _cursorIndexOfLinkedin = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedin");
          final int _cursorIndexOfTiktok = CursorUtil.getColumnIndexOrThrow(_cursor, "tiktok");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfIsVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "is_verified");
          final int _cursorIndexOfTieneTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "tiene_tienda");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final CachedUserEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpNombreTienda;
            if (_cursor.isNull(_cursorIndexOfNombreTienda)) {
              _tmpNombreTienda = null;
            } else {
              _tmpNombreTienda = _cursor.getString(_cursorIndexOfNombreTienda);
            }
            final String _tmpDescripcion;
            if (_cursor.isNull(_cursorIndexOfDescripcion)) {
              _tmpDescripcion = null;
            } else {
              _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final String _tmpBannerUrl;
            if (_cursor.isNull(_cursorIndexOfBannerUrl)) {
              _tmpBannerUrl = null;
            } else {
              _tmpBannerUrl = _cursor.getString(_cursorIndexOfBannerUrl);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpGenero;
            if (_cursor.isNull(_cursorIndexOfGenero)) {
              _tmpGenero = null;
            } else {
              _tmpGenero = _cursor.getString(_cursorIndexOfGenero);
            }
            final String _tmpUbicacion;
            if (_cursor.isNull(_cursorIndexOfUbicacion)) {
              _tmpUbicacion = null;
            } else {
              _tmpUbicacion = _cursor.getString(_cursorIndexOfUbicacion);
            }
            final String _tmpNombre;
            if (_cursor.isNull(_cursorIndexOfNombre)) {
              _tmpNombre = null;
            } else {
              _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            }
            final String _tmpFacebook;
            if (_cursor.isNull(_cursorIndexOfFacebook)) {
              _tmpFacebook = null;
            } else {
              _tmpFacebook = _cursor.getString(_cursorIndexOfFacebook);
            }
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpTwitter;
            if (_cursor.isNull(_cursorIndexOfTwitter)) {
              _tmpTwitter = null;
            } else {
              _tmpTwitter = _cursor.getString(_cursorIndexOfTwitter);
            }
            final String _tmpInstagram;
            if (_cursor.isNull(_cursorIndexOfInstagram)) {
              _tmpInstagram = null;
            } else {
              _tmpInstagram = _cursor.getString(_cursorIndexOfInstagram);
            }
            final String _tmpLinkedin;
            if (_cursor.isNull(_cursorIndexOfLinkedin)) {
              _tmpLinkedin = null;
            } else {
              _tmpLinkedin = _cursor.getString(_cursorIndexOfLinkedin);
            }
            final String _tmpTiktok;
            if (_cursor.isNull(_cursorIndexOfTiktok)) {
              _tmpTiktok = null;
            } else {
              _tmpTiktok = _cursor.getString(_cursorIndexOfTiktok);
            }
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final boolean _tmpIsVerified;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsVerified);
            _tmpIsVerified = _tmp_1 != 0;
            final boolean _tmpTieneTienda;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfTieneTienda);
            _tmpTieneTienda = _tmp_2 != 0;
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _result = new CachedUserEntity(_tmpUserId,_tmpUsername,_tmpNombreTienda,_tmpDescripcion,_tmpAvatarUrl,_tmpBannerUrl,_tmpEmail,_tmpGenero,_tmpUbicacion,_tmpNombre,_tmpFacebook,_tmpWhatsapp,_tmpTwitter,_tmpInstagram,_tmpLinkedin,_tmpTiktok,_tmpIsOnline,_tmpIsVerified,_tmpTieneTienda,_tmpCachedAt,_tmpVersion,_tmpUpdatedAt);
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
  public Object getByIds(final List<String> userIds,
      final Continuation<? super List<CachedUserEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM cached_users WHERE user_id IN (");
    final int _inputSize = userIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : userIds) {
      if (_item == null) {
        _statement.bindNull(_argIndex);
      } else {
        _statement.bindString(_argIndex, _item);
      }
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedUserEntity>>() {
      @Override
      @NonNull
      public List<CachedUserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfNombreTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre_tienda");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatar_url");
          final int _cursorIndexOfBannerUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "banner_url");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfGenero = CursorUtil.getColumnIndexOrThrow(_cursor, "genero");
          final int _cursorIndexOfUbicacion = CursorUtil.getColumnIndexOrThrow(_cursor, "ubicacion");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfFacebook = CursorUtil.getColumnIndexOrThrow(_cursor, "facebook");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfTwitter = CursorUtil.getColumnIndexOrThrow(_cursor, "twitter");
          final int _cursorIndexOfInstagram = CursorUtil.getColumnIndexOrThrow(_cursor, "instagram");
          final int _cursorIndexOfLinkedin = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedin");
          final int _cursorIndexOfTiktok = CursorUtil.getColumnIndexOrThrow(_cursor, "tiktok");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfIsVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "is_verified");
          final int _cursorIndexOfTieneTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "tiene_tienda");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CachedUserEntity> _result = new ArrayList<CachedUserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedUserEntity _item_1;
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpNombreTienda;
            if (_cursor.isNull(_cursorIndexOfNombreTienda)) {
              _tmpNombreTienda = null;
            } else {
              _tmpNombreTienda = _cursor.getString(_cursorIndexOfNombreTienda);
            }
            final String _tmpDescripcion;
            if (_cursor.isNull(_cursorIndexOfDescripcion)) {
              _tmpDescripcion = null;
            } else {
              _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final String _tmpBannerUrl;
            if (_cursor.isNull(_cursorIndexOfBannerUrl)) {
              _tmpBannerUrl = null;
            } else {
              _tmpBannerUrl = _cursor.getString(_cursorIndexOfBannerUrl);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpGenero;
            if (_cursor.isNull(_cursorIndexOfGenero)) {
              _tmpGenero = null;
            } else {
              _tmpGenero = _cursor.getString(_cursorIndexOfGenero);
            }
            final String _tmpUbicacion;
            if (_cursor.isNull(_cursorIndexOfUbicacion)) {
              _tmpUbicacion = null;
            } else {
              _tmpUbicacion = _cursor.getString(_cursorIndexOfUbicacion);
            }
            final String _tmpNombre;
            if (_cursor.isNull(_cursorIndexOfNombre)) {
              _tmpNombre = null;
            } else {
              _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            }
            final String _tmpFacebook;
            if (_cursor.isNull(_cursorIndexOfFacebook)) {
              _tmpFacebook = null;
            } else {
              _tmpFacebook = _cursor.getString(_cursorIndexOfFacebook);
            }
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpTwitter;
            if (_cursor.isNull(_cursorIndexOfTwitter)) {
              _tmpTwitter = null;
            } else {
              _tmpTwitter = _cursor.getString(_cursorIndexOfTwitter);
            }
            final String _tmpInstagram;
            if (_cursor.isNull(_cursorIndexOfInstagram)) {
              _tmpInstagram = null;
            } else {
              _tmpInstagram = _cursor.getString(_cursorIndexOfInstagram);
            }
            final String _tmpLinkedin;
            if (_cursor.isNull(_cursorIndexOfLinkedin)) {
              _tmpLinkedin = null;
            } else {
              _tmpLinkedin = _cursor.getString(_cursorIndexOfLinkedin);
            }
            final String _tmpTiktok;
            if (_cursor.isNull(_cursorIndexOfTiktok)) {
              _tmpTiktok = null;
            } else {
              _tmpTiktok = _cursor.getString(_cursorIndexOfTiktok);
            }
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final boolean _tmpIsVerified;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsVerified);
            _tmpIsVerified = _tmp_1 != 0;
            final boolean _tmpTieneTienda;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfTieneTienda);
            _tmpTieneTienda = _tmp_2 != 0;
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _item_1 = new CachedUserEntity(_tmpUserId,_tmpUsername,_tmpNombreTienda,_tmpDescripcion,_tmpAvatarUrl,_tmpBannerUrl,_tmpEmail,_tmpGenero,_tmpUbicacion,_tmpNombre,_tmpFacebook,_tmpWhatsapp,_tmpTwitter,_tmpInstagram,_tmpLinkedin,_tmpTiktok,_tmpIsOnline,_tmpIsVerified,_tmpTieneTienda,_tmpCachedAt,_tmpVersion,_tmpUpdatedAt);
            _result.add(_item_1);
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
  public Object searchByUsername(final String query, final int limit,
      final Continuation<? super List<CachedUserEntity>> $completion) {
    final String _sql = "SELECT * FROM cached_users WHERE username LIKE '%' || ? || '%' LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CachedUserEntity>>() {
      @Override
      @NonNull
      public List<CachedUserEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "user_id");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfNombreTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre_tienda");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfAvatarUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "avatar_url");
          final int _cursorIndexOfBannerUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "banner_url");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfGenero = CursorUtil.getColumnIndexOrThrow(_cursor, "genero");
          final int _cursorIndexOfUbicacion = CursorUtil.getColumnIndexOrThrow(_cursor, "ubicacion");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfFacebook = CursorUtil.getColumnIndexOrThrow(_cursor, "facebook");
          final int _cursorIndexOfWhatsapp = CursorUtil.getColumnIndexOrThrow(_cursor, "whatsapp");
          final int _cursorIndexOfTwitter = CursorUtil.getColumnIndexOrThrow(_cursor, "twitter");
          final int _cursorIndexOfInstagram = CursorUtil.getColumnIndexOrThrow(_cursor, "instagram");
          final int _cursorIndexOfLinkedin = CursorUtil.getColumnIndexOrThrow(_cursor, "linkedin");
          final int _cursorIndexOfTiktok = CursorUtil.getColumnIndexOrThrow(_cursor, "tiktok");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "is_online");
          final int _cursorIndexOfIsVerified = CursorUtil.getColumnIndexOrThrow(_cursor, "is_verified");
          final int _cursorIndexOfTieneTienda = CursorUtil.getColumnIndexOrThrow(_cursor, "tiene_tienda");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cached_at");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<CachedUserEntity> _result = new ArrayList<CachedUserEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CachedUserEntity _item;
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpNombreTienda;
            if (_cursor.isNull(_cursorIndexOfNombreTienda)) {
              _tmpNombreTienda = null;
            } else {
              _tmpNombreTienda = _cursor.getString(_cursorIndexOfNombreTienda);
            }
            final String _tmpDescripcion;
            if (_cursor.isNull(_cursorIndexOfDescripcion)) {
              _tmpDescripcion = null;
            } else {
              _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            }
            final String _tmpAvatarUrl;
            if (_cursor.isNull(_cursorIndexOfAvatarUrl)) {
              _tmpAvatarUrl = null;
            } else {
              _tmpAvatarUrl = _cursor.getString(_cursorIndexOfAvatarUrl);
            }
            final String _tmpBannerUrl;
            if (_cursor.isNull(_cursorIndexOfBannerUrl)) {
              _tmpBannerUrl = null;
            } else {
              _tmpBannerUrl = _cursor.getString(_cursorIndexOfBannerUrl);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpGenero;
            if (_cursor.isNull(_cursorIndexOfGenero)) {
              _tmpGenero = null;
            } else {
              _tmpGenero = _cursor.getString(_cursorIndexOfGenero);
            }
            final String _tmpUbicacion;
            if (_cursor.isNull(_cursorIndexOfUbicacion)) {
              _tmpUbicacion = null;
            } else {
              _tmpUbicacion = _cursor.getString(_cursorIndexOfUbicacion);
            }
            final String _tmpNombre;
            if (_cursor.isNull(_cursorIndexOfNombre)) {
              _tmpNombre = null;
            } else {
              _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            }
            final String _tmpFacebook;
            if (_cursor.isNull(_cursorIndexOfFacebook)) {
              _tmpFacebook = null;
            } else {
              _tmpFacebook = _cursor.getString(_cursorIndexOfFacebook);
            }
            final String _tmpWhatsapp;
            if (_cursor.isNull(_cursorIndexOfWhatsapp)) {
              _tmpWhatsapp = null;
            } else {
              _tmpWhatsapp = _cursor.getString(_cursorIndexOfWhatsapp);
            }
            final String _tmpTwitter;
            if (_cursor.isNull(_cursorIndexOfTwitter)) {
              _tmpTwitter = null;
            } else {
              _tmpTwitter = _cursor.getString(_cursorIndexOfTwitter);
            }
            final String _tmpInstagram;
            if (_cursor.isNull(_cursorIndexOfInstagram)) {
              _tmpInstagram = null;
            } else {
              _tmpInstagram = _cursor.getString(_cursorIndexOfInstagram);
            }
            final String _tmpLinkedin;
            if (_cursor.isNull(_cursorIndexOfLinkedin)) {
              _tmpLinkedin = null;
            } else {
              _tmpLinkedin = _cursor.getString(_cursorIndexOfLinkedin);
            }
            final String _tmpTiktok;
            if (_cursor.isNull(_cursorIndexOfTiktok)) {
              _tmpTiktok = null;
            } else {
              _tmpTiktok = _cursor.getString(_cursorIndexOfTiktok);
            }
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            final boolean _tmpIsVerified;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsVerified);
            _tmpIsVerified = _tmp_1 != 0;
            final boolean _tmpTieneTienda;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfTieneTienda);
            _tmpTieneTienda = _tmp_2 != 0;
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            final long _tmpVersion;
            _tmpVersion = _cursor.getLong(_cursorIndexOfVersion);
            final String _tmpUpdatedAt;
            if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
              _tmpUpdatedAt = null;
            } else {
              _tmpUpdatedAt = _cursor.getString(_cursorIndexOfUpdatedAt);
            }
            _item = new CachedUserEntity(_tmpUserId,_tmpUsername,_tmpNombreTienda,_tmpDescripcion,_tmpAvatarUrl,_tmpBannerUrl,_tmpEmail,_tmpGenero,_tmpUbicacion,_tmpNombre,_tmpFacebook,_tmpWhatsapp,_tmpTwitter,_tmpInstagram,_tmpLinkedin,_tmpTiktok,_tmpIsOnline,_tmpIsVerified,_tmpTieneTienda,_tmpCachedAt,_tmpVersion,_tmpUpdatedAt);
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM cached_users";
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
