package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.model.Post
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.rendly.app.data.cache.BadgeCountCache

/**
 * Repositorio para manejar el carrito de compras
 * Sincroniza automáticamente con Supabase
 */
object CartRepository {
    private const val TAG = "CartRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Modelo para Supabase
    @Serializable
    data class CartItemDB(
        val id: String = "",
        @SerialName("user_id") val userId: String = "",
        @SerialName("post_id") val postId: String = "",
        val quantity: Int = 1,
        @SerialName("selected_color") val selectedColor: String? = null,
        @SerialName("selected_size") val selectedSize: String? = null,
        @SerialName("created_at") val createdAt: String = ""
    )
    
    @Serializable
    data class CartItemInsert(
        @SerialName("user_id") val userId: String,
        @SerialName("post_id") val postId: String,
        val quantity: Int = 1,
        @SerialName("selected_color") val selectedColor: String? = null,
        @SerialName("selected_size") val selectedSize: String? = null
    )
    
    data class CartItem(
        val post: Post,
        val quantity: Int = 1,
        val selectedColor: String? = null,
        val selectedSize: String? = null,
        val addedAt: Long = System.currentTimeMillis()
    ) {
        val totalPrice: Double get() = post.price * quantity
        val originalTotalPrice: Double get() = post.price * quantity
        val savings: Double get() = originalTotalPrice - totalPrice
    }
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    // Conteo cacheado para mostrar badge inmediatamente
    private val _cachedItemCount = MutableStateFlow(0)
    val cachedItemCount: StateFlow<Int> = _cachedItemCount.asStateFlow()
    
    fun initCache() {
        _cachedItemCount.value = BadgeCountCache.getCartCount()
    }
    
    private fun updateCachedCount() {
        val count = _cartItems.value.sumOf { it.quantity }
        _cachedItemCount.value = count
        BadgeCountCache.setCartCount(count)
    }
    
    private val _isCartOpen = MutableStateFlow(false)
    val isCartOpen: StateFlow<Boolean> = _isCartOpen.asStateFlow()
    
    val itemCount: Int get() = _cartItems.value.sumOf { it.quantity }
    val subtotal: Double get() = _cartItems.value.sumOf { it.totalPrice }
    val totalSavings: Double get() = _cartItems.value.sumOf { it.savings }
    
    fun openCart() {
        _isCartOpen.value = true
    }
    
    fun closeCart() {
        _isCartOpen.value = false
    }
    
    fun addToCart(
        post: Post,
        quantity: Int = 1,
        selectedColor: String? = null,
        selectedSize: String? = null
    ) {
        val currentItems = _cartItems.value.toMutableList()
        
        // Buscar si ya existe el mismo producto con las mismas opciones
        val existingIndex = currentItems.indexOfFirst { 
            it.post.id == post.id && 
            it.selectedColor == selectedColor && 
            it.selectedSize == selectedSize 
        }
        
        if (existingIndex >= 0) {
            // Actualizar cantidad
            val existing = currentItems[existingIndex]
            val newQuantity = existing.quantity + quantity
            currentItems[existingIndex] = existing.copy(quantity = newQuantity)
            Log.d(TAG, "Cantidad actualizada para ${post.title}: $newQuantity")
            
            // Sincronizar con Supabase
            scope.launch { syncUpdateQuantity(post.id, newQuantity, selectedColor, selectedSize) }
        } else {
            // Agregar nuevo item
            currentItems.add(
                CartItem(
                    post = post,
                    quantity = quantity,
                    selectedColor = selectedColor,
                    selectedSize = selectedSize
                )
            )
            Log.d(TAG, "Nuevo item agregado al carrito: ${post.title}")
            
            // Sincronizar con Supabase
            scope.launch { syncAddToCart(post.id, quantity, selectedColor, selectedSize) }
        }
        
        _cartItems.value = currentItems
        updateCachedCount()
        Log.d(TAG, "Total items en carrito: ${itemCount}")
    }
    
    fun removeFromCart(postId: String, selectedColor: String? = null, selectedSize: String? = null) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { 
            it.post.id == postId && 
            it.selectedColor == selectedColor && 
            it.selectedSize == selectedSize 
        }
        _cartItems.value = currentItems
        updateCachedCount()
        Log.d(TAG, "Item removido del carrito. Total: ${itemCount}")
        
        // Sincronizar con Supabase
        scope.launch { syncRemoveFromCart(postId, selectedColor, selectedSize) }
    }
    
    fun updateQuantity(postId: String, newQuantity: Int, selectedColor: String? = null, selectedSize: String? = null) {
        if (newQuantity <= 0) {
            removeFromCart(postId, selectedColor, selectedSize)
            return
        }
        
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { 
            it.post.id == postId && 
            it.selectedColor == selectedColor && 
            it.selectedSize == selectedSize 
        }
        
        if (index >= 0) {
            currentItems[index] = currentItems[index].copy(quantity = newQuantity)
            _cartItems.value = currentItems
            updateCachedCount()
            
            // Sincronizar con Supabase
            scope.launch { syncUpdateQuantity(postId, newQuantity, selectedColor, selectedSize) }
        }
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
        updateCachedCount()
        Log.d(TAG, "Carrito vaciado")
        
        // Sincronizar con Supabase
        scope.launch { syncClearCart() }
    }
    
    fun isInCart(postId: String): Boolean {
        return _cartItems.value.any { it.post.id == postId }
    }
    
    fun getItemQuantity(postId: String): Int {
        return _cartItems.value.filter { it.post.id == postId }.sumOf { it.quantity }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // FUNCIONES DE SINCRONIZACIÓN CON SUPABASE
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * Cargar carrito desde Supabase al iniciar sesión
     */
    suspend fun loadCartFromSupabase() = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
            
            // Obtener items del carrito desde Supabase
            val cartItemsDB = SupabaseClient.database
                .from("cart_items")
                .select { filter { eq("user_id", userId) } }
                .decodeList<CartItemDB>()
            
            if (cartItemsDB.isEmpty()) {
                Log.d(TAG, "Carrito vacío en Supabase")
                return@withContext
            }
            
            // Obtener los posts correspondientes
            val postIds = cartItemsDB.map { it.postId }
            val posts = PostRepository.getPostsByIds(postIds)
            
            // Crear CartItems locales
            val loadedItems = cartItemsDB.mapNotNull { dbItem ->
                val post = posts.find { it.id == dbItem.postId }
                if (post != null) {
                    CartItem(
                        post = post,
                        quantity = dbItem.quantity,
                        selectedColor = dbItem.selectedColor,
                        selectedSize = dbItem.selectedSize,
                        addedAt = System.currentTimeMillis()
                    )
                } else null
            }
            
            _cartItems.value = loadedItems
            updateCachedCount()
            Log.d(TAG, "✅ Carrito cargado desde Supabase: ${loadedItems.size} items")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cargando carrito: ${e.message}")
        }
    }
    
    private suspend fun syncAddToCart(
        postId: String,
        quantity: Int,
        selectedColor: String?,
        selectedSize: String?
    ) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId == null) {
                Log.w(TAG, "⚠️ No hay usuario autenticado para sincronizar carrito")
                return
            }
            
            Log.d(TAG, "📦 Sincronizando carrito: postId=$postId, userId=$userId, qty=$quantity")
            
            // Primero eliminar si existe (para evitar conflicto de unique constraint)
            try {
                SupabaseClient.database
                    .from("cart_items")
                    .delete {
                        filter {
                            eq("user_id", userId)
                            eq("post_id", postId)
                            if (selectedColor != null) eq("selected_color", selectedColor)
                            if (selectedSize != null) eq("selected_size", selectedSize)
                        }
                    }
            } catch (deleteError: Exception) {
                Log.w(TAG, "Delete previo falló (puede ser normal): ${deleteError.message}")
            }
            
            // Insertar nuevo item
            val insert = CartItemInsert(
                userId = userId,
                postId = postId,
                quantity = quantity,
                selectedColor = selectedColor,
                selectedSize = selectedSize
            )
            
            SupabaseClient.database
                .from("cart_items")
                .insert(insert)
            
            Log.d(TAG, "✅ Item sincronizado con Supabase: $postId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sincronizando carrito: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    private suspend fun syncUpdateQuantity(
        postId: String,
        newQuantity: Int,
        selectedColor: String?,
        selectedSize: String?
    ) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return
            
            SupabaseClient.database
                .from("cart_items")
                .update(mapOf("quantity" to newQuantity)) {
                    filter {
                        eq("user_id", userId)
                        eq("post_id", postId)
                        if (selectedColor != null) eq("selected_color", selectedColor)
                        if (selectedSize != null) eq("selected_size", selectedSize)
                    }
                }
            
            Log.d(TAG, "✅ Cantidad actualizada en Supabase: $postId -> $newQuantity")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando cantidad: ${e.message}")
        }
    }
    
    private suspend fun syncRemoveFromCart(
        postId: String,
        selectedColor: String?,
        selectedSize: String?
    ) {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return
            
            SupabaseClient.database
                .from("cart_items")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("post_id", postId)
                        if (selectedColor != null) eq("selected_color", selectedColor)
                        if (selectedSize != null) eq("selected_size", selectedSize)
                    }
                }
            
            Log.d(TAG, "✅ Item eliminado de Supabase: $postId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando item: ${e.message}")
        }
    }
    
    private suspend fun syncClearCart() {
        try {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return
            
            SupabaseClient.database
                .from("cart_items")
                .delete {
                    filter { eq("user_id", userId) }
                }
            
            Log.d(TAG, "✅ Carrito vaciado en Supabase")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error vaciando carrito: ${e.message}")
        }
    }
}
