package com.rendly.app.data.cache.db;

import androidx.room.*;

/**
 * Room entities for disk cache layer.
 * Normalized structure with proper indices for query performance.
 *
 * Design principles:
 * - Each entity has cached_at for TTL checks
 * - version field for change detection
 * - Indices on frequently queried columns
 * - Relationships via foreign keys where appropriate
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b4\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u00fb\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0014\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0018\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0018\u0012\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u001bJ\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\u000b\u00104\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00105\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00106\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00108\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00109\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\u0014H\u00c6\u0003J\t\u0010<\u001a\u00020\u0014H\u00c6\u0003J\t\u0010=\u001a\u00020\u0014H\u00c6\u0003J\t\u0010>\u001a\u00020\u0003H\u00c6\u0003J\t\u0010?\u001a\u00020\u0018H\u00c6\u0003J\t\u0010@\u001a\u00020\u0018H\u00c6\u0003J\u000b\u0010A\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010B\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010D\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010E\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010G\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0083\u0002\u0010I\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00142\b\b\u0002\u0010\u0016\u001a\u00020\u00142\b\b\u0002\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u00182\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010J\u001a\u00020\u00142\b\u0010K\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010L\u001a\u00020MH\u00d6\u0001J\t\u0010N\u001a\u00020\u0003H\u00d6\u0001R\u0018\u0010\u0007\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0018\u0010\b\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001dR\u0016\u0010\u0017\u001a\u00020\u00188\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001dR\u0013\u0010\t\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001dR\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001dR\u0013\u0010\n\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001dR\u0013\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001dR\u0016\u0010\u0013\u001a\u00020\u00148\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010&R\u0016\u0010\u0015\u001a\u00020\u00148\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010&R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001dR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001dR\u0018\u0010\u0005\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001dR\u0016\u0010\u0016\u001a\u00020\u00148\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010&R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001dR\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001dR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001dR\u0018\u0010\u001a\u001a\u0004\u0018\u00010\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001dR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010\u001dR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\u001dR\u0011\u0010\u0019\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010 R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010\u001d\u00a8\u0006O"}, d2 = {"Lcom/rendly/app/data/cache/db/CachedUserEntity;", "", "userId", "", "username", "nombreTienda", "descripcion", "avatarUrl", "bannerUrl", "email", "genero", "ubicacion", "nombre", "facebook", "whatsapp", "twitter", "instagram", "linkedin", "tiktok", "isOnline", "", "isVerified", "tieneTienda", "cachedAt", "", "version", "updatedAt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZZJJLjava/lang/String;)V", "getAvatarUrl", "()Ljava/lang/String;", "getBannerUrl", "getCachedAt", "()J", "getDescripcion", "getEmail", "getFacebook", "getGenero", "getInstagram", "()Z", "getLinkedin", "getNombre", "getNombreTienda", "getTieneTienda", "getTiktok", "getTwitter", "getUbicacion", "getUpdatedAt", "getUserId", "getUsername", "getVersion", "getWhatsapp", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
@androidx.room.Entity(tableName = "cached_users", indices = {@androidx.room.Index(value = {"username"}), @androidx.room.Index(value = {"cached_at"})})
public final class CachedUserEntity {
    @androidx.room.PrimaryKey
    @androidx.room.ColumnInfo(name = "user_id")
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String username = null;
    @androidx.room.ColumnInfo(name = "nombre_tienda")
    @org.jetbrains.annotations.Nullable
    private final java.lang.String nombreTienda = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String descripcion = null;
    @androidx.room.ColumnInfo(name = "avatar_url")
    @org.jetbrains.annotations.Nullable
    private final java.lang.String avatarUrl = null;
    @androidx.room.ColumnInfo(name = "banner_url")
    @org.jetbrains.annotations.Nullable
    private final java.lang.String bannerUrl = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String email = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String genero = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String ubicacion = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String nombre = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String facebook = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String whatsapp = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String twitter = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String instagram = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String linkedin = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String tiktok = null;
    @androidx.room.ColumnInfo(name = "is_online")
    private final boolean isOnline = false;
    @androidx.room.ColumnInfo(name = "is_verified")
    private final boolean isVerified = false;
    @androidx.room.ColumnInfo(name = "tiene_tienda")
    private final boolean tieneTienda = false;
    @androidx.room.ColumnInfo(name = "cached_at")
    private final long cachedAt = 0L;
    private final long version = 0L;
    @androidx.room.ColumnInfo(name = "updated_at")
    @org.jetbrains.annotations.Nullable
    private final java.lang.String updatedAt = null;
    
    public CachedUserEntity(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.Nullable
    java.lang.String nombreTienda, @org.jetbrains.annotations.Nullable
    java.lang.String descripcion, @org.jetbrains.annotations.Nullable
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable
    java.lang.String bannerUrl, @org.jetbrains.annotations.Nullable
    java.lang.String email, @org.jetbrains.annotations.Nullable
    java.lang.String genero, @org.jetbrains.annotations.Nullable
    java.lang.String ubicacion, @org.jetbrains.annotations.Nullable
    java.lang.String nombre, @org.jetbrains.annotations.Nullable
    java.lang.String facebook, @org.jetbrains.annotations.Nullable
    java.lang.String whatsapp, @org.jetbrains.annotations.Nullable
    java.lang.String twitter, @org.jetbrains.annotations.Nullable
    java.lang.String instagram, @org.jetbrains.annotations.Nullable
    java.lang.String linkedin, @org.jetbrains.annotations.Nullable
    java.lang.String tiktok, boolean isOnline, boolean isVerified, boolean tieneTienda, long cachedAt, long version, @org.jetbrains.annotations.Nullable
    java.lang.String updatedAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNombreTienda() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDescripcion() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getAvatarUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getBannerUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getGenero() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUbicacion() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNombre() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getFacebook() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getWhatsapp() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getTwitter() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getInstagram() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getLinkedin() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getTiktok() {
        return null;
    }
    
    public final boolean isOnline() {
        return false;
    }
    
    public final boolean isVerified() {
        return false;
    }
    
    public final boolean getTieneTienda() {
        return false;
    }
    
    public final long getCachedAt() {
        return 0L;
    }
    
    public final long getVersion() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUpdatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component16() {
        return null;
    }
    
    public final boolean component17() {
        return false;
    }
    
    public final boolean component18() {
        return false;
    }
    
    public final boolean component19() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    public final long component20() {
        return 0L;
    }
    
    public final long component21() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component22() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.cache.db.CachedUserEntity copy(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.Nullable
    java.lang.String nombreTienda, @org.jetbrains.annotations.Nullable
    java.lang.String descripcion, @org.jetbrains.annotations.Nullable
    java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable
    java.lang.String bannerUrl, @org.jetbrains.annotations.Nullable
    java.lang.String email, @org.jetbrains.annotations.Nullable
    java.lang.String genero, @org.jetbrains.annotations.Nullable
    java.lang.String ubicacion, @org.jetbrains.annotations.Nullable
    java.lang.String nombre, @org.jetbrains.annotations.Nullable
    java.lang.String facebook, @org.jetbrains.annotations.Nullable
    java.lang.String whatsapp, @org.jetbrains.annotations.Nullable
    java.lang.String twitter, @org.jetbrains.annotations.Nullable
    java.lang.String instagram, @org.jetbrains.annotations.Nullable
    java.lang.String linkedin, @org.jetbrains.annotations.Nullable
    java.lang.String tiktok, boolean isOnline, boolean isVerified, boolean tieneTienda, long cachedAt, long version, @org.jetbrains.annotations.Nullable
    java.lang.String updatedAt) {
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