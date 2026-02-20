package com.rendly.app.data.repository;

import android.util.Log;
import com.rendly.app.data.model.Post;
import com.rendly.app.data.remote.SupabaseClient;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;
import com.rendly.app.data.cache.BadgeCountCache;

/**
 * Repositorio para manejar el carrito de compras
 * Sincroniza automáticamente con Supabase
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001a\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u00039:;B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J0\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\b\b\u0002\u0010#\u001a\u00020\u00072\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0004J\u0006\u0010&\u001a\u00020 J\u0006\u0010\'\u001a\u00020 J\u000e\u0010(\u001a\u00020\u00072\u0006\u0010)\u001a\u00020\u0004J\u0006\u0010*\u001a\u00020 J\u000e\u0010+\u001a\u00020\f2\u0006\u0010)\u001a\u00020\u0004J\u000e\u0010,\u001a\u00020 H\u0086@\u00a2\u0006\u0002\u0010-J\u0006\u0010.\u001a\u00020 J&\u0010/\u001a\u00020 2\u0006\u0010)\u001a\u00020\u00042\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0004J2\u00100\u001a\u00020 2\u0006\u0010)\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u00072\b\u0010$\u001a\u0004\u0018\u00010\u00042\b\u0010%\u001a\u0004\u0018\u00010\u0004H\u0082@\u00a2\u0006\u0002\u00101J\u000e\u00102\u001a\u00020 H\u0082@\u00a2\u0006\u0002\u0010-J*\u00103\u001a\u00020 2\u0006\u0010)\u001a\u00020\u00042\b\u0010$\u001a\u0004\u0018\u00010\u00042\b\u0010%\u001a\u0004\u0018\u00010\u0004H\u0082@\u00a2\u0006\u0002\u00104J2\u00105\u001a\u00020 2\u0006\u0010)\u001a\u00020\u00042\u0006\u00106\u001a\u00020\u00072\b\u0010$\u001a\u0004\u0018\u00010\u00042\b\u0010%\u001a\u0004\u0018\u00010\u0004H\u0082@\u00a2\u0006\u0002\u00101J\b\u00107\u001a\u00020 H\u0002J.\u00108\u001a\u00020 2\u0006\u0010)\u001a\u00020\u00042\u0006\u00106\u001a\u00020\u00072\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001d\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0010R\u0011\u0010\u0014\u001a\u00020\u00078F\u00a2\u0006\u0006\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0019\u001a\u00020\u001a8F\u00a2\u0006\u0006\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u001d\u001a\u00020\u001a8F\u00a2\u0006\u0006\u001a\u0004\b\u001e\u0010\u001c\u00a8\u0006<"}, d2 = {"Lcom/rendly/app/data/repository/CartRepository;", "", "()V", "TAG", "", "_cachedItemCount", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_cartItems", "", "Lcom/rendly/app/data/repository/CartRepository$CartItem;", "_isCartOpen", "", "cachedItemCount", "Lkotlinx/coroutines/flow/StateFlow;", "getCachedItemCount", "()Lkotlinx/coroutines/flow/StateFlow;", "cartItems", "getCartItems", "isCartOpen", "itemCount", "getItemCount", "()I", "scope", "Lkotlinx/coroutines/CoroutineScope;", "subtotal", "", "getSubtotal", "()D", "totalSavings", "getTotalSavings", "addToCart", "", "post", "Lcom/rendly/app/data/model/Post;", "quantity", "selectedColor", "selectedSize", "clearCart", "closeCart", "getItemQuantity", "postId", "initCache", "isInCart", "loadCartFromSupabase", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "openCart", "removeFromCart", "syncAddToCart", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncClearCart", "syncRemoveFromCart", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncUpdateQuantity", "newQuantity", "updateCachedCount", "updateQuantity", "CartItem", "CartItemDB", "CartItemInsert", "app_debug"})
public final class CartRepository {
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TAG = "CartRepository";
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.rendly.app.data.repository.CartRepository.CartItem>> _cartItems = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.CartRepository.CartItem>> cartItems = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _cachedItemCount = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> cachedItemCount = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isCartOpen = null;
    @org.jetbrains.annotations.NotNull
    private static final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isCartOpen = null;
    @org.jetbrains.annotations.NotNull
    public static final com.rendly.app.data.repository.CartRepository INSTANCE = null;
    
    private CartRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.rendly.app.data.repository.CartRepository.CartItem>> getCartItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getCachedItemCount() {
        return null;
    }
    
    public final void initCache() {
    }
    
    private final void updateCachedCount() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isCartOpen() {
        return null;
    }
    
    public final int getItemCount() {
        return 0;
    }
    
    public final double getSubtotal() {
        return 0.0;
    }
    
    public final double getTotalSavings() {
        return 0.0;
    }
    
    public final void openCart() {
    }
    
    public final void closeCart() {
    }
    
    public final void addToCart(@org.jetbrains.annotations.NotNull
    com.rendly.app.data.model.Post post, int quantity, @org.jetbrains.annotations.Nullable
    java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
    java.lang.String selectedSize) {
    }
    
    public final void removeFromCart(@org.jetbrains.annotations.NotNull
    java.lang.String postId, @org.jetbrains.annotations.Nullable
    java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
    java.lang.String selectedSize) {
    }
    
    public final void updateQuantity(@org.jetbrains.annotations.NotNull
    java.lang.String postId, int newQuantity, @org.jetbrains.annotations.Nullable
    java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
    java.lang.String selectedSize) {
    }
    
    public final void clearCart() {
    }
    
    public final boolean isInCart(@org.jetbrains.annotations.NotNull
    java.lang.String postId) {
        return false;
    }
    
    public final int getItemQuantity(@org.jetbrains.annotations.NotNull
    java.lang.String postId) {
        return 0;
    }
    
    /**
     * Cargar carrito desde Supabase al iniciar sesión
     */
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object loadCartFromSupabase(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncAddToCart(java.lang.String postId, int quantity, java.lang.String selectedColor, java.lang.String selectedSize, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncUpdateQuantity(java.lang.String postId, int newQuantity, java.lang.String selectedColor, java.lang.String selectedSize, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncRemoveFromCart(java.lang.String postId, java.lang.String selectedColor, java.lang.String selectedSize, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncClearCart(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B9\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010 \u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010!\u001a\u00020\nH\u00c6\u0003J?\u0010\"\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\t\u001a\u00020\nH\u00c6\u0001J\u0013\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010&\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\'\u001a\u00020\u0007H\u00d6\u0001R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u000e\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0016\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\u0011R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0011\u0010\u001b\u001a\u00020\u000f8F\u00a2\u0006\u0006\u001a\u0004\b\u001c\u0010\u0011\u00a8\u0006("}, d2 = {"Lcom/rendly/app/data/repository/CartRepository$CartItem;", "", "post", "Lcom/rendly/app/data/model/Post;", "quantity", "", "selectedColor", "", "selectedSize", "addedAt", "", "(Lcom/rendly/app/data/model/Post;ILjava/lang/String;Ljava/lang/String;J)V", "getAddedAt", "()J", "originalTotalPrice", "", "getOriginalTotalPrice", "()D", "getPost", "()Lcom/rendly/app/data/model/Post;", "getQuantity", "()I", "savings", "getSavings", "getSelectedColor", "()Ljava/lang/String;", "getSelectedSize", "totalPrice", "getTotalPrice", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class CartItem {
        @org.jetbrains.annotations.NotNull
        private final com.rendly.app.data.model.Post post = null;
        private final int quantity = 0;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String selectedColor = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String selectedSize = null;
        private final long addedAt = 0L;
        
        public CartItem(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.Post post, int quantity, @org.jetbrains.annotations.Nullable
        java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
        java.lang.String selectedSize, long addedAt) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Post getPost() {
            return null;
        }
        
        public final int getQuantity() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSelectedColor() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSelectedSize() {
            return null;
        }
        
        public final long getAddedAt() {
            return 0L;
        }
        
        public final double getTotalPrice() {
            return 0.0;
        }
        
        public final double getOriginalTotalPrice() {
            return 0.0;
        }
        
        public final double getSavings() {
            return 0.0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.model.Post component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component4() {
            return null;
        }
        
        public final long component5() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CartRepository.CartItem copy(@org.jetbrains.annotations.NotNull
        com.rendly.app.data.model.Post post, int quantity, @org.jetbrains.annotations.Nullable
        java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
        java.lang.String selectedSize, long addedAt) {
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
    
    @kotlinx.serialization.Serializable
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u001a\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 52\u00020\u0001:\u000245Bg\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0007\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\n\b\u0001\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\u0002\u0010\u000eBO\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001f\u001a\u00020\u0005H\u00c6\u0003J\t\u0010 \u001a\u00020\u0005H\u00c6\u0003J\t\u0010!\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010%\u001a\u00020\u0005H\u00c6\u0003JS\u0010&\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u000b\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010\'\u001a\u00020(2\b\u0010)\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010*\u001a\u00020\u0003H\u00d6\u0001J\t\u0010+\u001a\u00020\u0005H\u00d6\u0001J&\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020\u00002\u0006\u0010/\u001a\u0002002\u0006\u00101\u001a\u000202H\u00c1\u0001\u00a2\u0006\u0002\b3R\u001c\u0010\u000b\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0013R\u001c\u0010\u0007\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0015\u0010\u0011\u001a\u0004\b\u0016\u0010\u0013R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u001e\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0019\u0010\u0011\u001a\u0004\b\u001a\u0010\u0013R\u001e\u0010\n\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u001b\u0010\u0011\u001a\u0004\b\u001c\u0010\u0013R\u001c\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u001d\u0010\u0011\u001a\u0004\b\u001e\u0010\u0013\u00a8\u00066"}, d2 = {"Lcom/rendly/app/data/repository/CartRepository$CartItemDB;", "", "seen1", "", "id", "", "userId", "postId", "quantity", "selectedColor", "selectedSize", "createdAt", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getCreatedAt$annotations", "()V", "getCreatedAt", "()Ljava/lang/String;", "getId", "getPostId$annotations", "getPostId", "getQuantity", "()I", "getSelectedColor$annotations", "getSelectedColor", "getSelectedSize$annotations", "getSelectedSize", "getUserId$annotations", "getUserId", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
    public static final class CartItemDB {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String id = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String userId = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String postId = null;
        private final int quantity = 0;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String selectedColor = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String selectedSize = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String createdAt = null;
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CartRepository.CartItemDB.Companion Companion = null;
        
        public CartItemDB(@org.jetbrains.annotations.NotNull
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String userId, @org.jetbrains.annotations.NotNull
        java.lang.String postId, int quantity, @org.jetbrains.annotations.Nullable
        java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
        java.lang.String selectedSize, @org.jetbrains.annotations.NotNull
        java.lang.String createdAt) {
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
        
        @kotlinx.serialization.SerialName(value = "user_id")
        @java.lang.Deprecated
        public static void getUserId$annotations() {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getPostId() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "post_id")
        @java.lang.Deprecated
        public static void getPostId$annotations() {
        }
        
        public final int getQuantity() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSelectedColor() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "selected_color")
        @java.lang.Deprecated
        public static void getSelectedColor$annotations() {
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSelectedSize() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "selected_size")
        @java.lang.Deprecated
        public static void getSelectedSize$annotations() {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getCreatedAt() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "created_at")
        @java.lang.Deprecated
        public static void getCreatedAt$annotations() {
        }
        
        public CartItemDB() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component3() {
            return null;
        }
        
        public final int component4() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CartRepository.CartItemDB copy(@org.jetbrains.annotations.NotNull
        java.lang.String id, @org.jetbrains.annotations.NotNull
        java.lang.String userId, @org.jetbrains.annotations.NotNull
        java.lang.String postId, int quantity, @org.jetbrains.annotations.Nullable
        java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
        java.lang.String selectedSize, @org.jetbrains.annotations.NotNull
        java.lang.String createdAt) {
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
        com.rendly.app.data.repository.CartRepository.CartItemDB self, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/repository/CartRepository.CartItemDB.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/repository/CartRepository$CartItemDB;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
        @java.lang.Deprecated
        public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.repository.CartRepository.CartItemDB> {
            @org.jetbrains.annotations.NotNull
            public static final com.rendly.app.data.repository.CartRepository.CartItemDB.$serializer INSTANCE = null;
            
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
            public com.rendly.app.data.repository.CartRepository.CartItemDB deserialize(@org.jetbrains.annotations.NotNull
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
            com.rendly.app.data.repository.CartRepository.CartItemDB value) {
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/repository/CartRepository$CartItemDB$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/repository/CartRepository$CartItemDB;", "app_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull
            public final kotlinx.serialization.KSerializer<com.rendly.app.data.repository.CartRepository.CartItemDB> serializer() {
                return null;
            }
        }
    }
    
    @kotlinx.serialization.Serializable
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0087\b\u0018\u0000 .2\u00020\u0001:\u0002-.BQ\b\u0011\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0001\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\n\b\u0001\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0001\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\fB7\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\rJ\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J?\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001J\u0013\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010#\u001a\u00020\u0003H\u00d6\u0001J\t\u0010$\u001a\u00020\u0005H\u00d6\u0001J&\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u00002\u0006\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020+H\u00c1\u0001\u00a2\u0006\u0002\b,R\u001c\u0010\u0006\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u000e\u0010\u000f\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001e\u0010\b\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0014\u0010\u000f\u001a\u0004\b\u0015\u0010\u0011R\u001e\u0010\t\u001a\u0004\u0018\u00010\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0016\u0010\u000f\u001a\u0004\b\u0017\u0010\u0011R\u001c\u0010\u0004\u001a\u00020\u00058\u0006X\u0087\u0004\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0018\u0010\u000f\u001a\u0004\b\u0019\u0010\u0011\u00a8\u0006/"}, d2 = {"Lcom/rendly/app/data/repository/CartRepository$CartItemInsert;", "", "seen1", "", "userId", "", "postId", "quantity", "selectedColor", "selectedSize", "serializationConstructorMarker", "Lkotlinx/serialization/internal/SerializationConstructorMarker;", "(ILjava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Lkotlinx/serialization/internal/SerializationConstructorMarker;)V", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V", "getPostId$annotations", "()V", "getPostId", "()Ljava/lang/String;", "getQuantity", "()I", "getSelectedColor$annotations", "getSelectedColor", "getSelectedSize$annotations", "getSelectedSize", "getUserId$annotations", "getUserId", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "write$Self", "", "self", "output", "Lkotlinx/serialization/encoding/CompositeEncoder;", "serialDesc", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "write$Self$app_debug", "$serializer", "Companion", "app_debug"})
    public static final class CartItemInsert {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String userId = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String postId = null;
        private final int quantity = 0;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String selectedColor = null;
        @org.jetbrains.annotations.Nullable
        private final java.lang.String selectedSize = null;
        @org.jetbrains.annotations.NotNull
        public static final com.rendly.app.data.repository.CartRepository.CartItemInsert.Companion Companion = null;
        
        public CartItemInsert(@org.jetbrains.annotations.NotNull
        java.lang.String userId, @org.jetbrains.annotations.NotNull
        java.lang.String postId, int quantity, @org.jetbrains.annotations.Nullable
        java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
        java.lang.String selectedSize) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getUserId() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "user_id")
        @java.lang.Deprecated
        public static void getUserId$annotations() {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getPostId() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "post_id")
        @java.lang.Deprecated
        public static void getPostId$annotations() {
        }
        
        public final int getQuantity() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSelectedColor() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "selected_color")
        @java.lang.Deprecated
        public static void getSelectedColor$annotations() {
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getSelectedSize() {
            return null;
        }
        
        @kotlinx.serialization.SerialName(value = "selected_size")
        @java.lang.Deprecated
        public static void getSelectedSize$annotations() {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component2() {
            return null;
        }
        
        public final int component3() {
            return 0;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.rendly.app.data.repository.CartRepository.CartItemInsert copy(@org.jetbrains.annotations.NotNull
        java.lang.String userId, @org.jetbrains.annotations.NotNull
        java.lang.String postId, int quantity, @org.jetbrains.annotations.Nullable
        java.lang.String selectedColor, @org.jetbrains.annotations.Nullable
        java.lang.String selectedSize) {
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
        com.rendly.app.data.repository.CartRepository.CartItemInsert self, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.encoding.CompositeEncoder output, @org.jetbrains.annotations.NotNull
        kotlinx.serialization.descriptors.SerialDescriptor serialDesc) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\b\u001a\f\u0012\b\u0012\u0006\u0012\u0002\b\u00030\n0\tH\u00d6\u0001\u00a2\u0006\u0002\u0010\u000bJ\u0011\u0010\f\u001a\u00020\u00022\u0006\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\u0019\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0002H\u00d6\u0001R\u0014\u0010\u0004\u001a\u00020\u00058VX\u00d6\u0005\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"com/rendly/app/data/repository/CartRepository.CartItemInsert.$serializer", "Lkotlinx/serialization/internal/GeneratedSerializer;", "Lcom/rendly/app/data/repository/CartRepository$CartItemInsert;", "()V", "descriptor", "Lkotlinx/serialization/descriptors/SerialDescriptor;", "getDescriptor", "()Lkotlinx/serialization/descriptors/SerialDescriptor;", "childSerializers", "", "Lkotlinx/serialization/KSerializer;", "()[Lkotlinx/serialization/KSerializer;", "deserialize", "decoder", "Lkotlinx/serialization/encoding/Decoder;", "serialize", "", "encoder", "Lkotlinx/serialization/encoding/Encoder;", "value", "app_debug"})
        @java.lang.Deprecated
        public static final class $serializer implements kotlinx.serialization.internal.GeneratedSerializer<com.rendly.app.data.repository.CartRepository.CartItemInsert> {
            @org.jetbrains.annotations.NotNull
            public static final com.rendly.app.data.repository.CartRepository.CartItemInsert.$serializer INSTANCE = null;
            
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
            public com.rendly.app.data.repository.CartRepository.CartItemInsert deserialize(@org.jetbrains.annotations.NotNull
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
            com.rendly.app.data.repository.CartRepository.CartItemInsert value) {
            }
            
            @java.lang.Override
            @org.jetbrains.annotations.NotNull
            public kotlinx.serialization.KSerializer<?>[] typeParametersSerializers() {
                return null;
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u00c6\u0001\u00a8\u0006\u0006"}, d2 = {"Lcom/rendly/app/data/repository/CartRepository$CartItemInsert$Companion;", "", "()V", "serializer", "Lkotlinx/serialization/KSerializer;", "Lcom/rendly/app/data/repository/CartRepository$CartItemInsert;", "app_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull
            public final kotlinx.serialization.KSerializer<com.rendly.app.data.repository.CartRepository.CartItemInsert> serializer() {
                return null;
            }
        }
    }
}