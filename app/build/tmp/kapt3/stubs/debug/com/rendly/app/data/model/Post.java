package com.rendly.app.data.model;

import androidx.compose.runtime.Immutable;
import androidx.compose.runtime.Stable;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0004\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b#\n\u0002\u0018\u0002\n\u0002\b0\b\u0087\b\u0018\u0000 k2\u00020\u0001:\u0001kB\u00a5\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00030\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0018\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0018\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u0018\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u0018\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u001d\u001a\u00020\u0018\u0012\u000e\b\u0002\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00030\r\u0012\b\b\u0002\u0010\u001f\u001a\u00020\u0018\u0012\b\b\u0002\u0010 \u001a\u00020\u0018\u0012\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\"J\t\u0010J\u001a\u00020\u0003H\u00c6\u0003J\t\u0010K\u001a\u00020\u000fH\u00c6\u0003J\t\u0010L\u001a\u00020\u000fH\u00c6\u0003J\t\u0010M\u001a\u00020\u000fH\u00c6\u0003J\t\u0010N\u001a\u00020\u000fH\u00c6\u0003J\t\u0010O\u001a\u00020\u0003H\u00c6\u0003J\t\u0010P\u001a\u00020\u0003H\u00c6\u0003J\t\u0010Q\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010R\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010S\u001a\u00020\u0018H\u00c6\u0003J\t\u0010T\u001a\u00020\u0018H\u00c6\u0003J\t\u0010U\u001a\u00020\u0003H\u00c6\u0003J\t\u0010V\u001a\u00020\u0018H\u00c6\u0003J\t\u0010W\u001a\u00020\u0018H\u00c6\u0003J\u000b\u0010X\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010Y\u001a\u00020\u0018H\u00c6\u0003J\u000f\u0010Z\u001a\b\u0012\u0004\u0012\u00020\u00030\rH\u00c6\u0003J\t\u0010[\u001a\u00020\u0018H\u00c6\u0003J\t\u0010\\\u001a\u00020\u0018H\u00c6\u0003J\u000b\u0010]\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010^\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010_\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010`\u001a\u00020\bH\u00c6\u0003J\u0010\u0010a\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u00106J\t\u0010b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010c\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010d\u001a\b\u0012\u0004\u0012\u00020\u00030\rH\u00c6\u0003J\u00b2\u0002\u0010e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u000f2\b\b\u0002\u0010\u0011\u001a\u00020\u000f2\b\b\u0002\u0010\u0012\u001a\u00020\u000f2\b\b\u0002\u0010\u0013\u001a\u00020\u00032\b\b\u0002\u0010\u0014\u001a\u00020\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u00032\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0017\u001a\u00020\u00182\b\b\u0002\u0010\u0019\u001a\u00020\u00182\b\b\u0002\u0010\u001a\u001a\u00020\u00182\b\b\u0002\u0010\u001b\u001a\u00020\u00182\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u001d\u001a\u00020\u00182\u000e\b\u0002\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\b\b\u0002\u0010\u001f\u001a\u00020\u00182\b\b\u0002\u0010 \u001a\u00020\u00182\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010fJ\u0013\u0010g\u001a\u00020\u00182\b\u0010h\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010i\u001a\u00020\u000fH\u00d6\u0001J\t\u0010j\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u001f\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00030\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0011\u0010)\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b*\u0010+R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010&R\u0011\u0010\u0013\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010&R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010&R\u0011\u0010 \u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010$R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010&R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00030\r\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010(R\u0011\u0010\u0019\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010$R\u0011\u0010\u001a\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010$R\u0011\u0010\u0017\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010$R\u0011\u00102\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b3\u0010+R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010+R\u0015\u0010\t\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u00107\u001a\u0004\b5\u00106R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u00109R\u0013\u0010!\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010&R\u0011\u0010;\u001a\u00020<8F\u00a2\u0006\u0006\u001a\u0004\b=\u0010>R\u0011\u0010\u001d\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010$R\u0011\u0010\u0010\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010+R\u0011\u0010\u0011\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010+R\u0011\u0010\u0012\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010+R\u0011\u0010\u001b\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010$R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010&R\u0011\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010&R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u0010&R\u0013\u0010\u0016\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010&R\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u0010&R\u0013\u0010\u001c\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u0010&\u00a8\u0006l"}, d2 = {"Lcom/rendly/app/data/model/Post;", "", "id", "", "userId", "title", "description", "price", "", "previousPrice", "category", "condition", "images", "", "likesCount", "", "reviewsCount", "savesCount", "sharesCount", "createdAt", "username", "userAvatar", "userStoreName", "isUserVerified", "", "isLiked", "isSaved", "showStats", "warranty", "returnsAccepted", "colors", "allowOffers", "freeShipping", "productId", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;IIIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZZZLjava/lang/String;ZLjava/util/List;ZZLjava/lang/String;)V", "getAllowOffers", "()Z", "getCategory", "()Ljava/lang/String;", "getColors", "()Ljava/util/List;", "comments", "getComments", "()I", "getCondition", "getCreatedAt", "getDescription", "getFreeShipping", "getId", "getImages", "likes", "getLikes", "getLikesCount", "getPreviousPrice", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getPrice", "()D", "getProductId", "producto", "Lcom/rendly/app/data/model/Producto;", "getProducto", "()Lcom/rendly/app/data/model/Producto;", "getReturnsAccepted", "getReviewsCount", "getSavesCount", "getSharesCount", "getShowStats", "getTitle", "getUserAvatar", "getUserId", "getUserStoreName", "getUsername", "getWarranty", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;IIIILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZZZLjava/lang/String;ZLjava/util/List;ZZLjava/lang/String;)Lcom/rendly/app/data/model/Post;", "equals", "other", "hashCode", "toString", "Companion", "app_debug"})
@androidx.compose.runtime.Immutable
public final class Post {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String title = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String description = null;
    private final double price = 0.0;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double previousPrice = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String category = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String condition = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> images = null;
    private final int likesCount = 0;
    private final int reviewsCount = 0;
    private final int savesCount = 0;
    private final int sharesCount = 0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String username = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String userAvatar = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String userStoreName = null;
    private final boolean isUserVerified = false;
    private final boolean isLiked = false;
    private final boolean isSaved = false;
    private final boolean showStats = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String warranty = null;
    private final boolean returnsAccepted = false;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.String> colors = null;
    private final boolean allowOffers = false;
    private final boolean freeShipping = false;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String productId = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.model.Post.Companion Companion = null;
    
    public Post(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.String description, double price, @org.jetbrains.annotations.Nullable
    java.lang.Double previousPrice, @org.jetbrains.annotations.NotNull
    java.lang.String category, @org.jetbrains.annotations.NotNull
    java.lang.String condition, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, int likesCount, int reviewsCount, int savesCount, int sharesCount, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.NotNull
    java.lang.String userAvatar, @org.jetbrains.annotations.Nullable
    java.lang.String userStoreName, boolean isUserVerified, boolean isLiked, boolean isSaved, boolean showStats, @org.jetbrains.annotations.Nullable
    java.lang.String warranty, boolean returnsAccepted, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> colors, boolean allowOffers, boolean freeShipping, @org.jetbrains.annotations.Nullable
    java.lang.String productId) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDescription() {
        return null;
    }
    
    public final double getPrice() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getPreviousPrice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCategory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCondition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getImages() {
        return null;
    }
    
    public final int getLikesCount() {
        return 0;
    }
    
    public final int getReviewsCount() {
        return 0;
    }
    
    public final int getSavesCount() {
        return 0;
    }
    
    public final int getSharesCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCreatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getUserAvatar() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getUserStoreName() {
        return null;
    }
    
    public final boolean isUserVerified() {
        return false;
    }
    
    public final boolean isLiked() {
        return false;
    }
    
    public final boolean isSaved() {
        return false;
    }
    
    public final boolean getShowStats() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getWarranty() {
        return null;
    }
    
    public final boolean getReturnsAccepted() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> getColors() {
        return null;
    }
    
    public final boolean getAllowOffers() {
        return false;
    }
    
    public final boolean getFreeShipping() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getProductId() {
        return null;
    }
    
    public final int getLikes() {
        return 0;
    }
    
    public final int getComments() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.Producto getProducto() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final int component11() {
        return 0;
    }
    
    public final int component12() {
        return 0;
    }
    
    public final int component13() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component17() {
        return null;
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
    
    public final boolean component20() {
        return false;
    }
    
    public final boolean component21() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component22() {
        return null;
    }
    
    public final boolean component23() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component24() {
        return null;
    }
    
    public final boolean component25() {
        return false;
    }
    
    public final boolean component26() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component27() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    public final double component5() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.String> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.rendly.app.data.model.Post copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.String description, double price, @org.jetbrains.annotations.Nullable
    java.lang.Double previousPrice, @org.jetbrains.annotations.NotNull
    java.lang.String category, @org.jetbrains.annotations.NotNull
    java.lang.String condition, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> images, int likesCount, int reviewsCount, int savesCount, int sharesCount, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.NotNull
    java.lang.String userAvatar, @org.jetbrains.annotations.Nullable
    java.lang.String userStoreName, boolean isUserVerified, boolean isLiked, boolean isSaved, boolean showStats, @org.jetbrains.annotations.Nullable
    java.lang.String warranty, boolean returnsAccepted, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.String> colors, boolean allowOffers, boolean freeShipping, @org.jetbrains.annotations.Nullable
    java.lang.String productId) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002JE\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\b\u0002\u0010\r\u001a\u00020\u000e\u00a2\u0006\u0002\u0010\u000f\u00a8\u0006\u0010"}, d2 = {"Lcom/rendly/app/data/model/Post$Companion;", "", "()V", "fromDB", "Lcom/rendly/app/data/model/Post;", "postDB", "Lcom/rendly/app/data/model/PostDB;", "username", "", "avatarUrl", "storeName", "overrideReviewsCount", "", "isUserVerified", "", "(Lcom/rendly/app/data/model/PostDB;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Z)Lcom/rendly/app/data/model/Post;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Post fromDB(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.PostDB postDB, @org.jetbrains.annotations.NotNull
        java.lang.String username, @org.jetbrains.annotations.NotNull
        java.lang.String avatarUrl, @org.jetbrains.annotations.Nullable
        java.lang.String storeName, @org.jetbrains.annotations.Nullable
        java.lang.Integer overrideReviewsCount, boolean isUserVerified) {
            return null;
        }
    }
}