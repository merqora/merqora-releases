package com.rendly.app.data.cache.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MerqoraDatabase_Impl extends MerqoraDatabase {
  private volatile CachedUserDao _cachedUserDao;

  private volatile CachedPostDao _cachedPostDao;

  private volatile CachedRendDao _cachedRendDao;

  private volatile CachedStoryDao _cachedStoryDao;

  private volatile CachedMessageDao _cachedMessageDao;

  private volatile CachedConversationDao _cachedConversationDao;

  private volatile CachedNotificationDao _cachedNotificationDao;

  private volatile CachedFollowDao _cachedFollowDao;

  private volatile CacheSyncMetadataDao _cacheSyncMetadataDao;

  private volatile PendingOperationDao _pendingOperationDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_users` (`user_id` TEXT NOT NULL, `username` TEXT NOT NULL, `nombre_tienda` TEXT, `descripcion` TEXT, `avatar_url` TEXT, `banner_url` TEXT, `email` TEXT, `genero` TEXT, `ubicacion` TEXT, `nombre` TEXT, `facebook` TEXT, `whatsapp` TEXT, `twitter` TEXT, `instagram` TEXT, `linkedin` TEXT, `tiktok` TEXT, `is_online` INTEGER NOT NULL, `is_verified` INTEGER NOT NULL, `tiene_tienda` INTEGER NOT NULL, `cached_at` INTEGER NOT NULL, `version` INTEGER NOT NULL, `updated_at` TEXT, PRIMARY KEY(`user_id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_users_username` ON `cached_users` (`username`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_users_cached_at` ON `cached_users` (`cached_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_posts` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `price` REAL NOT NULL, `category` TEXT, `condition` TEXT, `images` TEXT NOT NULL, `tags` TEXT, `status` TEXT NOT NULL, `likes_count` INTEGER NOT NULL, `reviews_count` INTEGER NOT NULL, `views_count` INTEGER NOT NULL, `shares_count` INTEGER NOT NULL, `saves_count` INTEGER NOT NULL, `product_id` TEXT, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, `cached_at` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`user_id`) REFERENCES `cached_users`(`user_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_posts_user_id` ON `cached_posts` (`user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_posts_created_at` ON `cached_posts` (`created_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_posts_cached_at` ON `cached_posts` (`cached_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_posts_status` ON `cached_posts` (`status`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_rends` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `video_url` TEXT NOT NULL, `thumbnail_url` TEXT, `product_title` TEXT, `product_price` REAL, `product_link` TEXT, `product_image` TEXT, `product_id` TEXT, `duration` INTEGER NOT NULL, `status` TEXT NOT NULL, `likes_count` INTEGER NOT NULL, `reviews_count` INTEGER NOT NULL, `views_count` INTEGER NOT NULL, `shares_count` INTEGER NOT NULL, `saves_count` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `updated_at` TEXT NOT NULL, `cached_at` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`user_id`) REFERENCES `cached_users`(`user_id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_rends_user_id` ON `cached_rends` (`user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_rends_created_at` ON `cached_rends` (`created_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_rends_cached_at` ON `cached_rends` (`cached_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_rends_status` ON `cached_rends` (`status`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_stories` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `media_url` TEXT NOT NULL, `media_type` TEXT NOT NULL, `caption` TEXT, `views_count` INTEGER NOT NULL, `likes_count` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `expires_at` TEXT, `cached_at` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_stories_user_id` ON `cached_stories` (`user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_stories_created_at` ON `cached_stories` (`created_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_stories_expires_at` ON `cached_stories` (`expires_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_stories_cached_at` ON `cached_stories` (`cached_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_messages` (`id` TEXT NOT NULL, `conversation_id` TEXT NOT NULL, `sender_id` TEXT NOT NULL, `receiver_id` TEXT NOT NULL, `content` TEXT NOT NULL, `message_type` TEXT NOT NULL, `media_url` TEXT, `is_read` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `cached_at` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_messages_conversation_id` ON `cached_messages` (`conversation_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_messages_sender_id` ON `cached_messages` (`sender_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_messages_created_at` ON `cached_messages` (`created_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_messages_cached_at` ON `cached_messages` (`cached_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_conversations` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `other_user_id` TEXT NOT NULL, `last_message` TEXT, `last_message_at` TEXT, `unread_count` INTEGER NOT NULL, `cached_at` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_conversations_user_id` ON `cached_conversations` (`user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_conversations_other_user_id` ON `cached_conversations` (`other_user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_conversations_last_message_at` ON `cached_conversations` (`last_message_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_conversations_cached_at` ON `cached_conversations` (`cached_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_notifications` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `action_url` TEXT, `actor_id` TEXT, `entity_id` TEXT, `entity_type` TEXT, `is_read` INTEGER NOT NULL, `created_at` TEXT NOT NULL, `cached_at` INTEGER NOT NULL, `version` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_notifications_user_id` ON `cached_notifications` (`user_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_notifications_created_at` ON `cached_notifications` (`created_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_notifications_is_read` ON `cached_notifications` (`is_read`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_notifications_cached_at` ON `cached_notifications` (`cached_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cached_follows` (`follower_id` TEXT NOT NULL, `following_id` TEXT NOT NULL, `created_at` TEXT NOT NULL, `cached_at` INTEGER NOT NULL, PRIMARY KEY(`follower_id`, `following_id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_follows_follower_id` ON `cached_follows` (`follower_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_follows_following_id` ON `cached_follows` (`following_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cached_follows_cached_at` ON `cached_follows` (`cached_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cache_sync_metadata` (`cache_key` TEXT NOT NULL, `last_sync_at` INTEGER NOT NULL, `last_version` INTEGER NOT NULL, `etag` TEXT, `sync_status` TEXT NOT NULL, PRIMARY KEY(`cache_key`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cache_sync_metadata_last_sync_at` ON `cache_sync_metadata` (`last_sync_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_operations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `operation_type` TEXT NOT NULL, `entity_type` TEXT NOT NULL, `entity_id` TEXT, `payload` TEXT NOT NULL, `status` TEXT NOT NULL, `retry_count` INTEGER NOT NULL, `error_message` TEXT, `created_at` INTEGER NOT NULL, `last_attempt_at` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_operations_created_at` ON `pending_operations` (`created_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_operations_status` ON `pending_operations` (`status`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '5c7123ab011500e909d23c96a47afe45')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `cached_users`");
        db.execSQL("DROP TABLE IF EXISTS `cached_posts`");
        db.execSQL("DROP TABLE IF EXISTS `cached_rends`");
        db.execSQL("DROP TABLE IF EXISTS `cached_stories`");
        db.execSQL("DROP TABLE IF EXISTS `cached_messages`");
        db.execSQL("DROP TABLE IF EXISTS `cached_conversations`");
        db.execSQL("DROP TABLE IF EXISTS `cached_notifications`");
        db.execSQL("DROP TABLE IF EXISTS `cached_follows`");
        db.execSQL("DROP TABLE IF EXISTS `cache_sync_metadata`");
        db.execSQL("DROP TABLE IF EXISTS `pending_operations`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCachedUsers = new HashMap<String, TableInfo.Column>(22);
        _columnsCachedUsers.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("nombre_tienda", new TableInfo.Column("nombre_tienda", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("descripcion", new TableInfo.Column("descripcion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("avatar_url", new TableInfo.Column("avatar_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("banner_url", new TableInfo.Column("banner_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("genero", new TableInfo.Column("genero", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("ubicacion", new TableInfo.Column("ubicacion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("nombre", new TableInfo.Column("nombre", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("facebook", new TableInfo.Column("facebook", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("whatsapp", new TableInfo.Column("whatsapp", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("twitter", new TableInfo.Column("twitter", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("instagram", new TableInfo.Column("instagram", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("linkedin", new TableInfo.Column("linkedin", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("tiktok", new TableInfo.Column("tiktok", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("is_online", new TableInfo.Column("is_online", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("is_verified", new TableInfo.Column("is_verified", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("tiene_tienda", new TableInfo.Column("tiene_tienda", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("version", new TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedUsers.put("updated_at", new TableInfo.Column("updated_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedUsers = new HashSet<TableInfo.Index>(2);
        _indicesCachedUsers.add(new TableInfo.Index("index_cached_users_username", false, Arrays.asList("username"), Arrays.asList("ASC")));
        _indicesCachedUsers.add(new TableInfo.Index("index_cached_users_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        final TableInfo _infoCachedUsers = new TableInfo("cached_users", _columnsCachedUsers, _foreignKeysCachedUsers, _indicesCachedUsers);
        final TableInfo _existingCachedUsers = TableInfo.read(db, "cached_users");
        if (!_infoCachedUsers.equals(_existingCachedUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_users(com.rendly.app.data.cache.db.CachedUserEntity).\n"
                  + " Expected:\n" + _infoCachedUsers + "\n"
                  + " Found:\n" + _existingCachedUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedPosts = new HashMap<String, TableInfo.Column>(20);
        _columnsCachedPosts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("price", new TableInfo.Column("price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("condition", new TableInfo.Column("condition", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("images", new TableInfo.Column("images", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("tags", new TableInfo.Column("tags", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("likes_count", new TableInfo.Column("likes_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("reviews_count", new TableInfo.Column("reviews_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("views_count", new TableInfo.Column("views_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("shares_count", new TableInfo.Column("shares_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("saves_count", new TableInfo.Column("saves_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("product_id", new TableInfo.Column("product_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("updated_at", new TableInfo.Column("updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedPosts.put("version", new TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedPosts = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCachedPosts.add(new TableInfo.ForeignKey("cached_users", "CASCADE", "NO ACTION", Arrays.asList("user_id"), Arrays.asList("user_id")));
        final HashSet<TableInfo.Index> _indicesCachedPosts = new HashSet<TableInfo.Index>(4);
        _indicesCachedPosts.add(new TableInfo.Index("index_cached_posts_user_id", false, Arrays.asList("user_id"), Arrays.asList("ASC")));
        _indicesCachedPosts.add(new TableInfo.Index("index_cached_posts_created_at", false, Arrays.asList("created_at"), Arrays.asList("ASC")));
        _indicesCachedPosts.add(new TableInfo.Index("index_cached_posts_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        _indicesCachedPosts.add(new TableInfo.Index("index_cached_posts_status", false, Arrays.asList("status"), Arrays.asList("ASC")));
        final TableInfo _infoCachedPosts = new TableInfo("cached_posts", _columnsCachedPosts, _foreignKeysCachedPosts, _indicesCachedPosts);
        final TableInfo _existingCachedPosts = TableInfo.read(db, "cached_posts");
        if (!_infoCachedPosts.equals(_existingCachedPosts)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_posts(com.rendly.app.data.cache.db.CachedPostEntity).\n"
                  + " Expected:\n" + _infoCachedPosts + "\n"
                  + " Found:\n" + _existingCachedPosts);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedRends = new HashMap<String, TableInfo.Column>(22);
        _columnsCachedRends.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("video_url", new TableInfo.Column("video_url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("thumbnail_url", new TableInfo.Column("thumbnail_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("product_title", new TableInfo.Column("product_title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("product_price", new TableInfo.Column("product_price", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("product_link", new TableInfo.Column("product_link", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("product_image", new TableInfo.Column("product_image", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("product_id", new TableInfo.Column("product_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("duration", new TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("likes_count", new TableInfo.Column("likes_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("reviews_count", new TableInfo.Column("reviews_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("views_count", new TableInfo.Column("views_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("shares_count", new TableInfo.Column("shares_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("saves_count", new TableInfo.Column("saves_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("updated_at", new TableInfo.Column("updated_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedRends.put("version", new TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedRends = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCachedRends.add(new TableInfo.ForeignKey("cached_users", "CASCADE", "NO ACTION", Arrays.asList("user_id"), Arrays.asList("user_id")));
        final HashSet<TableInfo.Index> _indicesCachedRends = new HashSet<TableInfo.Index>(4);
        _indicesCachedRends.add(new TableInfo.Index("index_cached_rends_user_id", false, Arrays.asList("user_id"), Arrays.asList("ASC")));
        _indicesCachedRends.add(new TableInfo.Index("index_cached_rends_created_at", false, Arrays.asList("created_at"), Arrays.asList("ASC")));
        _indicesCachedRends.add(new TableInfo.Index("index_cached_rends_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        _indicesCachedRends.add(new TableInfo.Index("index_cached_rends_status", false, Arrays.asList("status"), Arrays.asList("ASC")));
        final TableInfo _infoCachedRends = new TableInfo("cached_rends", _columnsCachedRends, _foreignKeysCachedRends, _indicesCachedRends);
        final TableInfo _existingCachedRends = TableInfo.read(db, "cached_rends");
        if (!_infoCachedRends.equals(_existingCachedRends)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_rends(com.rendly.app.data.cache.db.CachedRendEntity).\n"
                  + " Expected:\n" + _infoCachedRends + "\n"
                  + " Found:\n" + _existingCachedRends);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedStories = new HashMap<String, TableInfo.Column>(11);
        _columnsCachedStories.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("media_url", new TableInfo.Column("media_url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("media_type", new TableInfo.Column("media_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("caption", new TableInfo.Column("caption", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("views_count", new TableInfo.Column("views_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("likes_count", new TableInfo.Column("likes_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("expires_at", new TableInfo.Column("expires_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedStories.put("version", new TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedStories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedStories = new HashSet<TableInfo.Index>(4);
        _indicesCachedStories.add(new TableInfo.Index("index_cached_stories_user_id", false, Arrays.asList("user_id"), Arrays.asList("ASC")));
        _indicesCachedStories.add(new TableInfo.Index("index_cached_stories_created_at", false, Arrays.asList("created_at"), Arrays.asList("ASC")));
        _indicesCachedStories.add(new TableInfo.Index("index_cached_stories_expires_at", false, Arrays.asList("expires_at"), Arrays.asList("ASC")));
        _indicesCachedStories.add(new TableInfo.Index("index_cached_stories_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        final TableInfo _infoCachedStories = new TableInfo("cached_stories", _columnsCachedStories, _foreignKeysCachedStories, _indicesCachedStories);
        final TableInfo _existingCachedStories = TableInfo.read(db, "cached_stories");
        if (!_infoCachedStories.equals(_existingCachedStories)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_stories(com.rendly.app.data.cache.db.CachedStoryEntity).\n"
                  + " Expected:\n" + _infoCachedStories + "\n"
                  + " Found:\n" + _existingCachedStories);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedMessages = new HashMap<String, TableInfo.Column>(11);
        _columnsCachedMessages.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("conversation_id", new TableInfo.Column("conversation_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("sender_id", new TableInfo.Column("sender_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("receiver_id", new TableInfo.Column("receiver_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("message_type", new TableInfo.Column("message_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("media_url", new TableInfo.Column("media_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("is_read", new TableInfo.Column("is_read", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedMessages.put("version", new TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedMessages = new HashSet<TableInfo.Index>(4);
        _indicesCachedMessages.add(new TableInfo.Index("index_cached_messages_conversation_id", false, Arrays.asList("conversation_id"), Arrays.asList("ASC")));
        _indicesCachedMessages.add(new TableInfo.Index("index_cached_messages_sender_id", false, Arrays.asList("sender_id"), Arrays.asList("ASC")));
        _indicesCachedMessages.add(new TableInfo.Index("index_cached_messages_created_at", false, Arrays.asList("created_at"), Arrays.asList("ASC")));
        _indicesCachedMessages.add(new TableInfo.Index("index_cached_messages_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        final TableInfo _infoCachedMessages = new TableInfo("cached_messages", _columnsCachedMessages, _foreignKeysCachedMessages, _indicesCachedMessages);
        final TableInfo _existingCachedMessages = TableInfo.read(db, "cached_messages");
        if (!_infoCachedMessages.equals(_existingCachedMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_messages(com.rendly.app.data.cache.db.CachedMessageEntity).\n"
                  + " Expected:\n" + _infoCachedMessages + "\n"
                  + " Found:\n" + _existingCachedMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedConversations = new HashMap<String, TableInfo.Column>(8);
        _columnsCachedConversations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedConversations.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedConversations.put("other_user_id", new TableInfo.Column("other_user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedConversations.put("last_message", new TableInfo.Column("last_message", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedConversations.put("last_message_at", new TableInfo.Column("last_message_at", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedConversations.put("unread_count", new TableInfo.Column("unread_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedConversations.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedConversations.put("version", new TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedConversations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedConversations = new HashSet<TableInfo.Index>(4);
        _indicesCachedConversations.add(new TableInfo.Index("index_cached_conversations_user_id", false, Arrays.asList("user_id"), Arrays.asList("ASC")));
        _indicesCachedConversations.add(new TableInfo.Index("index_cached_conversations_other_user_id", false, Arrays.asList("other_user_id"), Arrays.asList("ASC")));
        _indicesCachedConversations.add(new TableInfo.Index("index_cached_conversations_last_message_at", false, Arrays.asList("last_message_at"), Arrays.asList("ASC")));
        _indicesCachedConversations.add(new TableInfo.Index("index_cached_conversations_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        final TableInfo _infoCachedConversations = new TableInfo("cached_conversations", _columnsCachedConversations, _foreignKeysCachedConversations, _indicesCachedConversations);
        final TableInfo _existingCachedConversations = TableInfo.read(db, "cached_conversations");
        if (!_infoCachedConversations.equals(_existingCachedConversations)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_conversations(com.rendly.app.data.cache.db.CachedConversationEntity).\n"
                  + " Expected:\n" + _infoCachedConversations + "\n"
                  + " Found:\n" + _existingCachedConversations);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedNotifications = new HashMap<String, TableInfo.Column>(13);
        _columnsCachedNotifications.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("user_id", new TableInfo.Column("user_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("body", new TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("action_url", new TableInfo.Column("action_url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("actor_id", new TableInfo.Column("actor_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("entity_id", new TableInfo.Column("entity_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("entity_type", new TableInfo.Column("entity_type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("is_read", new TableInfo.Column("is_read", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedNotifications.put("version", new TableInfo.Column("version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedNotifications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedNotifications = new HashSet<TableInfo.Index>(4);
        _indicesCachedNotifications.add(new TableInfo.Index("index_cached_notifications_user_id", false, Arrays.asList("user_id"), Arrays.asList("ASC")));
        _indicesCachedNotifications.add(new TableInfo.Index("index_cached_notifications_created_at", false, Arrays.asList("created_at"), Arrays.asList("ASC")));
        _indicesCachedNotifications.add(new TableInfo.Index("index_cached_notifications_is_read", false, Arrays.asList("is_read"), Arrays.asList("ASC")));
        _indicesCachedNotifications.add(new TableInfo.Index("index_cached_notifications_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        final TableInfo _infoCachedNotifications = new TableInfo("cached_notifications", _columnsCachedNotifications, _foreignKeysCachedNotifications, _indicesCachedNotifications);
        final TableInfo _existingCachedNotifications = TableInfo.read(db, "cached_notifications");
        if (!_infoCachedNotifications.equals(_existingCachedNotifications)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_notifications(com.rendly.app.data.cache.db.CachedNotificationEntity).\n"
                  + " Expected:\n" + _infoCachedNotifications + "\n"
                  + " Found:\n" + _existingCachedNotifications);
        }
        final HashMap<String, TableInfo.Column> _columnsCachedFollows = new HashMap<String, TableInfo.Column>(4);
        _columnsCachedFollows.put("follower_id", new TableInfo.Column("follower_id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedFollows.put("following_id", new TableInfo.Column("following_id", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedFollows.put("created_at", new TableInfo.Column("created_at", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCachedFollows.put("cached_at", new TableInfo.Column("cached_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCachedFollows = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCachedFollows = new HashSet<TableInfo.Index>(3);
        _indicesCachedFollows.add(new TableInfo.Index("index_cached_follows_follower_id", false, Arrays.asList("follower_id"), Arrays.asList("ASC")));
        _indicesCachedFollows.add(new TableInfo.Index("index_cached_follows_following_id", false, Arrays.asList("following_id"), Arrays.asList("ASC")));
        _indicesCachedFollows.add(new TableInfo.Index("index_cached_follows_cached_at", false, Arrays.asList("cached_at"), Arrays.asList("ASC")));
        final TableInfo _infoCachedFollows = new TableInfo("cached_follows", _columnsCachedFollows, _foreignKeysCachedFollows, _indicesCachedFollows);
        final TableInfo _existingCachedFollows = TableInfo.read(db, "cached_follows");
        if (!_infoCachedFollows.equals(_existingCachedFollows)) {
          return new RoomOpenHelper.ValidationResult(false, "cached_follows(com.rendly.app.data.cache.db.CachedFollowEntity).\n"
                  + " Expected:\n" + _infoCachedFollows + "\n"
                  + " Found:\n" + _existingCachedFollows);
        }
        final HashMap<String, TableInfo.Column> _columnsCacheSyncMetadata = new HashMap<String, TableInfo.Column>(5);
        _columnsCacheSyncMetadata.put("cache_key", new TableInfo.Column("cache_key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCacheSyncMetadata.put("last_sync_at", new TableInfo.Column("last_sync_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCacheSyncMetadata.put("last_version", new TableInfo.Column("last_version", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCacheSyncMetadata.put("etag", new TableInfo.Column("etag", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCacheSyncMetadata.put("sync_status", new TableInfo.Column("sync_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCacheSyncMetadata = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCacheSyncMetadata = new HashSet<TableInfo.Index>(1);
        _indicesCacheSyncMetadata.add(new TableInfo.Index("index_cache_sync_metadata_last_sync_at", false, Arrays.asList("last_sync_at"), Arrays.asList("ASC")));
        final TableInfo _infoCacheSyncMetadata = new TableInfo("cache_sync_metadata", _columnsCacheSyncMetadata, _foreignKeysCacheSyncMetadata, _indicesCacheSyncMetadata);
        final TableInfo _existingCacheSyncMetadata = TableInfo.read(db, "cache_sync_metadata");
        if (!_infoCacheSyncMetadata.equals(_existingCacheSyncMetadata)) {
          return new RoomOpenHelper.ValidationResult(false, "cache_sync_metadata(com.rendly.app.data.cache.db.CacheSyncMetadataEntity).\n"
                  + " Expected:\n" + _infoCacheSyncMetadata + "\n"
                  + " Found:\n" + _existingCacheSyncMetadata);
        }
        final HashMap<String, TableInfo.Column> _columnsPendingOperations = new HashMap<String, TableInfo.Column>(10);
        _columnsPendingOperations.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("operation_type", new TableInfo.Column("operation_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("entity_type", new TableInfo.Column("entity_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("entity_id", new TableInfo.Column("entity_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("payload", new TableInfo.Column("payload", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("retry_count", new TableInfo.Column("retry_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("error_message", new TableInfo.Column("error_message", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingOperations.put("last_attempt_at", new TableInfo.Column("last_attempt_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingOperations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingOperations = new HashSet<TableInfo.Index>(2);
        _indicesPendingOperations.add(new TableInfo.Index("index_pending_operations_created_at", false, Arrays.asList("created_at"), Arrays.asList("ASC")));
        _indicesPendingOperations.add(new TableInfo.Index("index_pending_operations_status", false, Arrays.asList("status"), Arrays.asList("ASC")));
        final TableInfo _infoPendingOperations = new TableInfo("pending_operations", _columnsPendingOperations, _foreignKeysPendingOperations, _indicesPendingOperations);
        final TableInfo _existingPendingOperations = TableInfo.read(db, "pending_operations");
        if (!_infoPendingOperations.equals(_existingPendingOperations)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_operations(com.rendly.app.data.cache.db.PendingOperationEntity).\n"
                  + " Expected:\n" + _infoPendingOperations + "\n"
                  + " Found:\n" + _existingPendingOperations);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "5c7123ab011500e909d23c96a47afe45", "71d38002c6e94bd3ec7aa429ef6fb6d6");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "cached_users","cached_posts","cached_rends","cached_stories","cached_messages","cached_conversations","cached_notifications","cached_follows","cache_sync_metadata","pending_operations");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `cached_users`");
      _db.execSQL("DELETE FROM `cached_posts`");
      _db.execSQL("DELETE FROM `cached_rends`");
      _db.execSQL("DELETE FROM `cached_stories`");
      _db.execSQL("DELETE FROM `cached_messages`");
      _db.execSQL("DELETE FROM `cached_conversations`");
      _db.execSQL("DELETE FROM `cached_notifications`");
      _db.execSQL("DELETE FROM `cached_follows`");
      _db.execSQL("DELETE FROM `cache_sync_metadata`");
      _db.execSQL("DELETE FROM `pending_operations`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CachedUserDao.class, CachedUserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CachedPostDao.class, CachedPostDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CachedRendDao.class, CachedRendDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CachedStoryDao.class, CachedStoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CachedMessageDao.class, CachedMessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CachedConversationDao.class, CachedConversationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CachedNotificationDao.class, CachedNotificationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CachedFollowDao.class, CachedFollowDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CacheSyncMetadataDao.class, CacheSyncMetadataDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PendingOperationDao.class, PendingOperationDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CachedUserDao cachedUserDao() {
    if (_cachedUserDao != null) {
      return _cachedUserDao;
    } else {
      synchronized(this) {
        if(_cachedUserDao == null) {
          _cachedUserDao = new CachedUserDao_Impl(this);
        }
        return _cachedUserDao;
      }
    }
  }

  @Override
  public CachedPostDao cachedPostDao() {
    if (_cachedPostDao != null) {
      return _cachedPostDao;
    } else {
      synchronized(this) {
        if(_cachedPostDao == null) {
          _cachedPostDao = new CachedPostDao_Impl(this);
        }
        return _cachedPostDao;
      }
    }
  }

  @Override
  public CachedRendDao cachedRendDao() {
    if (_cachedRendDao != null) {
      return _cachedRendDao;
    } else {
      synchronized(this) {
        if(_cachedRendDao == null) {
          _cachedRendDao = new CachedRendDao_Impl(this);
        }
        return _cachedRendDao;
      }
    }
  }

  @Override
  public CachedStoryDao cachedStoryDao() {
    if (_cachedStoryDao != null) {
      return _cachedStoryDao;
    } else {
      synchronized(this) {
        if(_cachedStoryDao == null) {
          _cachedStoryDao = new CachedStoryDao_Impl(this);
        }
        return _cachedStoryDao;
      }
    }
  }

  @Override
  public CachedMessageDao cachedMessageDao() {
    if (_cachedMessageDao != null) {
      return _cachedMessageDao;
    } else {
      synchronized(this) {
        if(_cachedMessageDao == null) {
          _cachedMessageDao = new CachedMessageDao_Impl(this);
        }
        return _cachedMessageDao;
      }
    }
  }

  @Override
  public CachedConversationDao cachedConversationDao() {
    if (_cachedConversationDao != null) {
      return _cachedConversationDao;
    } else {
      synchronized(this) {
        if(_cachedConversationDao == null) {
          _cachedConversationDao = new CachedConversationDao_Impl(this);
        }
        return _cachedConversationDao;
      }
    }
  }

  @Override
  public CachedNotificationDao cachedNotificationDao() {
    if (_cachedNotificationDao != null) {
      return _cachedNotificationDao;
    } else {
      synchronized(this) {
        if(_cachedNotificationDao == null) {
          _cachedNotificationDao = new CachedNotificationDao_Impl(this);
        }
        return _cachedNotificationDao;
      }
    }
  }

  @Override
  public CachedFollowDao cachedFollowDao() {
    if (_cachedFollowDao != null) {
      return _cachedFollowDao;
    } else {
      synchronized(this) {
        if(_cachedFollowDao == null) {
          _cachedFollowDao = new CachedFollowDao_Impl(this);
        }
        return _cachedFollowDao;
      }
    }
  }

  @Override
  public CacheSyncMetadataDao cacheSyncMetadataDao() {
    if (_cacheSyncMetadataDao != null) {
      return _cacheSyncMetadataDao;
    } else {
      synchronized(this) {
        if(_cacheSyncMetadataDao == null) {
          _cacheSyncMetadataDao = new CacheSyncMetadataDao_Impl(this);
        }
        return _cacheSyncMetadataDao;
      }
    }
  }

  @Override
  public PendingOperationDao pendingOperationDao() {
    if (_pendingOperationDao != null) {
      return _pendingOperationDao;
    } else {
      synchronized(this) {
        if(_pendingOperationDao == null) {
          _pendingOperationDao = new PendingOperationDao_Impl(this);
        }
        return _pendingOperationDao;
      }
    }
  }
}
