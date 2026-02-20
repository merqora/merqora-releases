package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\b\n\u0002\b\u000e\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0011\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001CB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002JH\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u00042\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00042\b\b\u0002\u0010\u001c\u001a\u00020\u00042\b\b\u0002\u0010\u001d\u001a\u00020\u001e2\b\b\u0002\u0010\u001f\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010 JX\u0010!\u001a\u00020\n2\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u00042\u0006\u0010$\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u00042\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00042\b\b\u0002\u0010\u001c\u001a\u00020\u00042\b\b\u0002\u0010\u001d\u001a\u00020\u001e2\b\b\u0002\u0010%\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010&JL\u0010\'\u001a\u00020\n2\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010(\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u00042\u0006\u0010$\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u00042\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00042\b\b\u0002\u0010\u001c\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010)J\u001c\u0010*\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u0002J\u0006\u0010,\u001a\u00020-J\u0006\u0010.\u001a\u00020-J\u0016\u0010/\u001a\u00020\n2\u0006\u00100\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101J0\u00102\u001a\u0010\u0012\u0004\u0012\u00020\u0004\u0012\u0006\u0012\u0004\u0018\u00010\u0004032\b\u00104\u001a\u0004\u0018\u00010\u00042\b\u00105\u001a\u0004\u0018\u00010\u0004H\u0082@\u00a2\u0006\u0002\u00106J\u0010\u00107\u001a\u0004\u0018\u00010\u0004H\u0082@\u00a2\u0006\u0002\u00108J\u0018\u00109\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0019\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101J\u0018\u0010:\u001a\u0004\u0018\u00010\u00042\u0006\u0010;\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101J\u0016\u0010<\u001a\u00020-2\u0006\u0010=\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101J\u0016\u0010>\u001a\u00020-2\u0006\u00100\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101J\u0016\u0010?\u001a\u00020\u00012\u0006\u0010\u0019\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101J \u0010@\u001a\u00020\u00012\u0006\u0010\u0019\u001a\u00020\u00042\b\b\u0002\u0010\u001f\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010AJ\u0016\u0010B\u001a\u00020\u001e2\u0006\u0010\"\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u00101R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0012\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\n0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011R\u0019\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0011\u00a8\u0006D"}, d2 = {"Lcom/rendly/app/data/repository/CommentRepository;", "", "()V", "TAG", "", "_comments", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/rendly/app/data/repository/CommentWithUser;", "_isLoading", "", "_lastError", "cachedUserAvatar", "cachedUserName", "comments", "Lkotlinx/coroutines/flow/StateFlow;", "getComments", "()Lkotlinx/coroutines/flow/StateFlow;", "currentIsRend", "currentPostId", "currentProductId", "isLoading", "lastError", "getLastError", "addComment", "postId", "text", "userAvatar", "userName", "rating", "", "isRend", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addProductReview", "productId", "sourceId", "sourceType", "isVerified", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addProductReviewReply", "parentId", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "buildCommentTree", "allComments", "clearComments", "", "clearError", "deleteProductReview", "reviewId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureCurrentUserData", "Lkotlin/Pair;", "passedName", "passedAvatar", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCurrentUserId", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getProductIdFromPostId", "getProductIdFromRendId", "rendId", "likeComment", "commentId", "likeProductReview", "loadAllCommentsForProduct", "loadComments", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadProductReviews", "ProductIdResponse", "app_debug"})
public final class CommentRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "CommentRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.CommentWithUser>> _comments = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.CommentWithUser>> comments = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _lastError = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.String> lastError = null;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String currentPostId;
    private static boolean currentIsRend = false;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String cachedUserName;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String cachedUserAvatar;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String currentProductId;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.CommentRepository INSTANCE = null;
    
    private CommentRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.CommentWithUser>> getComments() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getLastError() {
        return null;
    }
    
    public final void clearError() {
    }
    
    private final java.lang.Object getCurrentUserId(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object ensureCurrentUserData(java.lang.String passedName, java.lang.String passedAvatar, kotlin.coroutines.Continuation<? super kotlin.Pair<java.lang.String, java.lang.String>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadComments(@org.jetbrains.annotations.NotNull
    java.lang.String postId, boolean isRend, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<java.lang.Object> $completion) {
        return null;
    }
    
    /**
     * Carga comentarios de AMBAS tablas (comments + rend_comments) para mostrar
     * todas las opiniones de un producto en ProductPage.
     * Busca por post_id en comments Y por rend_id en rend_comments.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadAllCommentsForProduct(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<java.lang.Object> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object addComment(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    java.lang.String text, @org.jetbrains.annotations.Nullable
    java.lang.String userAvatar, @org.jetbrains.annotations.NotNull
    java.lang.String userName, int rating, boolean isRend, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object likeComment(@org.jetbrains.annotations.NotNull
    java.lang.String commentId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void clearComments() {
    }
    
    /**
     * Obtiene el product_id de un Post dado su ID.
     * Útil cuando un Rend tiene product_link apuntando a un Post.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getProductIdFromPostId(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Obtiene el product_id de un Rend dado su ID.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getProductIdFromRendId(@org.jetbrains.annotations.NotNull
    java.lang.String rendId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Carga todas las reviews de un producto usando su product_id.
     * Esto muestra opiniones de Posts y Rends del mismo producto juntas.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadProductReviews(@org.jetbrains.annotations.NotNull
    java.lang.String productId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    /**
     * Agrega una review a la tabla unificada product_reviews.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object addProductReview(@org.jetbrains.annotations.NotNull
    java.lang.String productId, @org.jetbrains.annotations.NotNull
    java.lang.String sourceId, @org.jetbrains.annotations.NotNull
    java.lang.String sourceType, @org.jetbrains.annotations.NotNull
    java.lang.String text, @org.jetbrains.annotations.Nullable
    java.lang.String userAvatar, @org.jetbrains.annotations.NotNull
    java.lang.String userName, int rating, boolean isVerified, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Like en review de la tabla unificada
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object likeProductReview(@org.jetbrains.annotations.NotNull
    java.lang.String reviewId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Eliminar una review/respuesta de la tabla unificada
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteProductReview(@org.jetbrains.annotations.NotNull
    java.lang.String reviewId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Construye un árbol de comentarios agrupando respuestas bajo sus padres.
     * Los comentarios raíz (parentId == null) se muestran como principales,
     * y sus respuestas se anidan dentro del campo `replies`.
     */
    private final java.util.List<com.rendly.app.data.repository.CommentWithUser> buildCommentTree(java.util.List<com.rendly.app.data.repository.CommentWithUser> allComments) {
        return null;
    }
    
    /**
     * Agrega una respuesta a una review en la tabla unificada product_reviews.
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object addProductReviewReply(@org.jetbrains.annotations.NotNull
    java.lang.String productId, @org.jetbrains.annotations.NotNull
    java.lang.String parentId, @org.jetbrains.annotations.NotNull
    java.lang.String sourceId, @org.jetbrains.annotations.NotNull
    java.lang.String sourceType, @org.jetbrains.annotations.NotNull
    java.lang.String text, @org.jetbrains.annotations.Nullable
    java.lang.String userAvatar, @org.jetbrains.annotations.NotNull
    java.lang.String userName, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @kotlinx.serialization.Serializable
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0083\b\u0018\u0000 \u001e2\u00020\u0001:\u0002\u001d\u001eB%\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010\bB\u0011\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\tJ\u000b\u0010\u000e\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0015\u0010\u000f\u001a\u00020\u00002\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0014\u001a\u00020\u0005H\u00d6\u0001J&\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u00002\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u00c1\u0001\u00a2\u0006\u0002\b\u001cR\u001e\u0010\u0004\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\n\u0010\u000b\u001a\u0004\b\f\u0010\r\u00a8\u0006\u001f"}, d2 = {"Lcom/rendly/app/data/repository/CommentRepository$ProductIdResponse;", "", "seen1", "", "productId", "", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;)V", "getProductId$annotations", "()V", "getProductId", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
    static final class ProductIdResponse {
        @org.jetbrains.annotations.Nullable
        private final java.lang.String productId = null;
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CommentRepository.ProductIdResponse.Companion Companion = null;
        
        public ProductIdResponse(@org.jetbrains.annotations.Nullable
        java.lang.String productId) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getProductId() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "product_id")
        @java.lang.Deprecated
        public static void getProductId$annotations() {
        }
        
        public ProductIdResponse() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CommentRepository.ProductIdResponse copy(@org.jetbrains.annotations.Nullable
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
        
        @kotlin.jvm.JvmStatic
        public static final void write$Self$app_debug(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.repository.CommentRepository.ProductIdResponse self, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/repository/CommentRepository.ProductIdResponse.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/repository/CommentRepository$ProductIdResponse;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
        @java.lang.Deprecated
        public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.repository.CommentRepository.ProductIdResponse> {
            @org.jetbrains.annotations.NotNull
            public static final com.rendly.app.data.repository.CommentRepository.ProductIdResponse.$serializer INSTANCE = null;
            
            private $serializer() {
                super();
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] childSerializers() {
                return null;
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public com.rendly.app.data.repository.CommentRepository.ProductIdResponse deserialize(@org.jetbrains.annotations.NotNull
            kotlinx.serialization.encoding.Decoder decoder) {
                return null;
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.descriptors.SerialDescriptor getDescriptor() {
                return null;
            }
            
            @java.lang.Override
            public void serialize(@org.jetbrains.annotations.NotNull
            kotlinx.serialization.encoding.Encoder encoder, @org.jetbrains.annotations.NotNull
            com.rendly.app.data.repository.CommentRepository.ProductIdResponse value) {
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/repository/CommentRepository$ProductIdResponse$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/repository/CommentRepository$ProductIdResponse;", "app_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull
            public final kotlinx.serialization.KSerializer<com.rendly.app.data.repository.CommentRepository.ProductIdResponse> serializer() {
                return null;
            }
        }
    }
}