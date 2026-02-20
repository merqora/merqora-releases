package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class CommentDB(
    val id: String = "",
    @SerialName("post_id") val postId: String = "",
    @SerialName("user_id") val userId: String = "",
    val text: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val likes: Int = 0,
    val rating: Int = 5,
    @SerialName("parent_id") val parentId: String? = null
)

@Serializable
data class RendCommentDB(
    val id: String = "",
    @SerialName("rend_id") val rendId: String = "",
    @SerialName("user_id") val userId: String = "",
    val text: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val likes: Int = 0,
    val rating: Int = 5,
    @SerialName("parent_id") val parentId: String? = null
)

// Modelo para la tabla unificada product_reviews
@Serializable
data class ProductReviewDB(
    val id: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("source_type") val sourceType: String = "",
    @SerialName("source_id") val sourceId: String = "",
    val text: String = "",
    val rating: Int = 5,
    val likes: Int = 0,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("parent_id") val parentId: String? = null
)

@Serializable
data class CommentUserDB(
    @SerialName("user_id") val userId: String = "",
    val username: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false
)

data class CommentWithUser(
    val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val text: String,
    val createdAt: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val rating: Int = 5,
    val parentId: String? = null,
    val replies: List<CommentWithUser> = emptyList(),
    val replyCount: Int = 0,
    val isVerified: Boolean = false
)

@Serializable
private data class PostReviewsCount(
    @SerialName("reviews_count") val reviewsCount: Int = 0
)

object CommentRepository {
    
    private const val TAG = "CommentRepository"
    
    private val _comments = MutableStateFlow<List<CommentWithUser>>(emptyList())
    val comments: StateFlow<List<CommentWithUser>> = _comments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    private var currentPostId: String? = null
    private var currentIsRend: Boolean = false
    
    fun clearError() {
        _lastError.value = null
    }
    
    private suspend fun getCurrentUserId(): String? {
        return SupabaseClient.auth.currentUserOrNull()?.id
    }
    
    // Cache de datos del usuario actual para optimistic updates
    private var cachedUserName: String? = null
    private var cachedUserAvatar: String? = null
    
    private suspend fun ensureCurrentUserData(passedName: String?, passedAvatar: String?): Pair<String, String?> {
        // Si ya tenemos datos válidos del caller, usarlos
        if (!passedName.isNullOrBlank() && passedName != "Usuario" && passedName != "Tú") {
            cachedUserName = passedName
            cachedUserAvatar = passedAvatar
            return Pair(passedName, passedAvatar)
        }
        // Si tenemos cache válido, usarlo
        if (!cachedUserName.isNullOrBlank()) {
            return Pair(cachedUserName!!, cachedUserAvatar)
        }
        // Fetch desde DB como último recurso
        try {
            val userId = getCurrentUserId() ?: return Pair(passedName ?: "Usuario", passedAvatar)
            val user = SupabaseClient.database
                .from("usuarios")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("username", "avatar_url")) {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<CommentUserDB>()
            if (user != null) {
                cachedUserName = user.username.takeIf { it.isNotBlank() } ?: "Usuario"
                cachedUserAvatar = user.avatarUrl
                return Pair(cachedUserName!!, cachedUserAvatar)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user data for optimistic update: ${e.message}")
        }
        return Pair(passedName ?: "Usuario", passedAvatar)
    }
    
    suspend fun loadComments(postId: String, isRend: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            currentPostId = postId
            currentIsRend = isRend
            
            // Usar tabla correcta según si es Rend o Post
            val tableName = if (isRend) "rend_comments" else "comments"
            val idColumn = if (isRend) "rend_id" else "post_id"
            
            val commentsList = SupabaseClient.database
                .from(tableName)
                .select {
                    filter { eq(idColumn, postId) }
                }
                .decodeList<CommentDB>()
                .sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Comentarios cargados: ${commentsList.size}")
            
            // Cargar usuarios de los comentarios
            val userIds = commentsList.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, CommentUserDB>()
            
            for (uid in userIds) {
                try {
                    val user = SupabaseClient.database
                        .from("usuarios")
                        .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("user_id", "username", "avatar_url", "is_verified")) {
                            filter { eq("user_id", uid) }
                        }
                        .decodeSingleOrNull<CommentUserDB>()
                    
                    Log.d(TAG, "Usuario $uid cargado: username=${user?.username}, avatar=${user?.avatarUrl?.take(50)}")
                    
                    if (user != null) {
                        usersMap[uid] = user
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error cargando usuario $uid: ${e.message}")
                }
            }
            
            val allCommentsWithUser = commentsList.map { comment ->
                val user = usersMap[comment.userId]
                CommentWithUser(
                    id = comment.id,
                    postId = comment.postId,
                    userId = comment.userId,
                    username = user?.username?.takeIf { it.isNotBlank() } ?: "Usuario",
                    avatarUrl = user?.avatarUrl,
                    text = comment.text,
                    createdAt = comment.createdAt,
                    likes = comment.likes,
                    isLiked = false,
                    rating = comment.rating,
                    parentId = comment.parentId,
                    isVerified = user?.isVerified ?: false
                )
            }
            
            // Construir árbol: agrupar respuestas bajo sus padres
            _comments.value = buildCommentTree(allCommentsWithUser)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando comentarios: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Carga comentarios de AMBAS tablas (comments + rend_comments) para mostrar
     * todas las opiniones de un producto en ProductPage.
     * Busca por post_id en comments Y por rend_id en rend_comments.
     */
    suspend fun loadAllCommentsForProduct(postId: String) = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            currentPostId = postId
            currentIsRend = false
            
            val allComments = mutableListOf<CommentWithUser>()
            
            // 1. Cargar comentarios de la tabla 'comments' (posts del Home)
            try {
                val postComments = SupabaseClient.database
                    .from("comments")
                    .select {
                        filter { eq("post_id", postId) }
                    }
                    .decodeList<CommentDB>()
                
                Log.d(TAG, "Comentarios de posts: ${postComments.size}")
                
                // Obtener usuarios para estos comentarios
                val userIds = postComments.map { it.userId }.distinct()
                val usersMap = mutableMapOf<String, CommentUserDB>()
                
                for (userId in userIds) {
                    try {
                        val user = SupabaseClient.database
                            .from("usuarios")
                            .select {
                                filter { eq("user_id", userId) }
                            }
                            .decodeSingleOrNull<CommentUserDB>()
                        user?.let { usersMap[userId] = it }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error obteniendo usuario $userId: ${e.message}")
                    }
                }
                
                postComments.forEach { comment ->
                    val user = usersMap[comment.userId]
                    allComments.add(
                        CommentWithUser(
                            id = comment.id,
                            postId = comment.postId,
                            userId = comment.userId,
                            username = user?.username ?: "Usuario",
                            avatarUrl = user?.avatarUrl,
                            text = comment.text,
                            createdAt = comment.createdAt,
                            likes = comment.likes,
                            isLiked = false,
                            rating = comment.rating,
                            isVerified = user?.isVerified ?: false
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando comentarios de posts: ${e.message}")
            }
            
            // 2. Cargar comentarios de la tabla 'rend_comments' (Rends)
            // Buscar rends que tengan el mismo product_link que el post
            try {
                val rendComments = SupabaseClient.database
                    .from("rend_comments")
                    .select {
                        filter { eq("rend_id", postId) }
                    }
                    .decodeList<RendCommentDB>()
                
                Log.d(TAG, "Comentarios de rends (mismo ID): ${rendComments.size}")
                
                // Obtener usuarios para estos comentarios
                val userIds = rendComments.map { it.userId }.distinct()
                val usersMap = mutableMapOf<String, CommentUserDB>()
                
                for (userId in userIds) {
                    try {
                        val user = SupabaseClient.database
                            .from("usuarios")
                            .select {
                                filter { eq("user_id", userId) }
                            }
                            .decodeSingleOrNull<CommentUserDB>()
                        user?.let { usersMap[userId] = it }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error obteniendo usuario $userId: ${e.message}")
                    }
                }
                
                rendComments.forEach { comment ->
                    val user = usersMap[comment.userId]
                    allComments.add(
                        CommentWithUser(
                            id = comment.id,
                            postId = comment.rendId,
                            userId = comment.userId,
                            username = user?.username ?: "Usuario",
                            avatarUrl = user?.avatarUrl,
                            text = comment.text,
                            createdAt = comment.createdAt,
                            likes = comment.likes,
                            isLiked = false,
                            rating = comment.rating,
                            isVerified = user?.isVerified ?: false
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando comentarios de rends: ${e.message}")
            }
            
            // Ordenar todos los comentarios por fecha (más recientes primero)
            val sortedComments = allComments.sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Total comentarios combinados: ${sortedComments.size}")
            _comments.value = sortedComments
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando todos los comentarios: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun addComment(
        postId: String, 
        text: String,
        userAvatar: String? = null,
        userName: String = "Usuario",
        rating: Int = 5,
        isRend: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: return@withContext false
            
            val commentId = UUID.randomUUID().toString()
            
            // Usar tabla y columna correcta según si es Rend o Post
            val tableName = if (isRend) "rend_comments" else "comments"
            val idColumn = if (isRend) "rend_id" else "post_id"
            
            val newComment = buildJsonObject {
                put("id", commentId)
                put(idColumn, postId)
                put("user_id", userId)
                put("text", text)
                put("likes", 0)
                put("rating", rating)
            }
            
            Log.d(TAG, "Insertando comentario en $tableName: $newComment")
            
            SupabaseClient.database
                .from(tableName)
                .insert(newComment)
            
            Log.d(TAG, "Comentario agregado exitosamente: $commentId")
            
            // El trigger de Supabase se encarga de incrementar reviews_count automáticamente
            
            // Usar directamente el avatar y username pasados desde la UI
            val localComment = CommentWithUser(
                id = commentId,
                postId = postId,
                userId = userId,
                username = userName.takeIf { it.isNotBlank() } ?: "Usuario",
                avatarUrl = userAvatar,
                text = text,
                createdAt = java.time.Instant.now().toString(),
                likes = 0,
                isLiked = false,
                rating = rating,
                isVerified = false
            )
            
            // Agregar al inicio de la lista
            val updatedComments = listOf(localComment) + _comments.value
            _comments.value = updatedComments
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error agregando comentario: ${e.message}", e)
            _lastError.value = "Error: ${e.message ?: "Error desconocido al publicar"}"
            false
        }
    }
    
    suspend fun likeComment(commentId: String) = withContext(Dispatchers.IO) {
        try {
            // Toggle like en el comentario
            val currentComments = _comments.value.toMutableList()
            val index = currentComments.indexOfFirst { it.id == commentId }
            
            if (index >= 0) {
                val comment = currentComments[index]
                val newLikes = if (comment.isLiked) comment.likes - 1 else comment.likes + 1
                currentComments[index] = comment.copy(
                    likes = newLikes,
                    isLiked = !comment.isLiked
                )
                _comments.value = currentComments
                
                // Usar tabla correcta según contexto actual
                val tableName = if (currentIsRend) "rend_comments" else "comments"
                SupabaseClient.database
                    .from(tableName)
                    .update(mapOf("likes" to newLikes)) {
                        filter { eq("id", commentId) }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error likeando comentario: ${e.message}", e)
        }
        Unit // Retornar Unit explícitamente
    }
    
    fun clearComments() {
        _comments.value = emptyList()
        currentPostId = null
        currentIsRend = false
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // SISTEMA UNIFICADO DE REVIEWS (usa tabla product_reviews)
    // ═══════════════════════════════════════════════════════════════════════════════
    
    @Serializable
    private data class ProductIdResponse(
        @SerialName("product_id") val productId: String? = null
    )
    
    /**
     * Obtiene el product_id de un Post dado su ID.
     * Útil cuando un Rend tiene product_link apuntando a un Post.
     */
    suspend fun getProductIdFromPostId(postId: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Buscando product_id para post: $postId")
            val result = SupabaseClient.database
                .from("posts")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("product_id")) {
                    filter { eq("id", postId) }
                }
                .decodeSingleOrNull<ProductIdResponse>()
            
            Log.d(TAG, "📦 product_id encontrado: ${result?.productId}")
            result?.productId
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo product_id: ${e.message}", e)
            null
        }
    }
    
    /**
     * Obtiene el product_id de un Rend dado su ID.
     */
    suspend fun getProductIdFromRendId(rendId: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Buscando product_id para rend: $rendId")
            val result = SupabaseClient.database
                .from("rends")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("product_id")) {
                    filter { eq("id", rendId) }
                }
                .decodeSingleOrNull<ProductIdResponse>()
            
            Log.d(TAG, "📦 product_id de rend encontrado: ${result?.productId}")
            result?.productId
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo product_id de rend: ${e.message}", e)
            null
        }
    }
    
    /**
     * Carga todas las reviews de un producto usando su product_id.
     * Esto muestra opiniones de Posts y Rends del mismo producto juntas.
     */
    suspend fun loadProductReviews(productId: String) = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            currentProductId = productId
            
            Log.d(TAG, "📦 Cargando reviews para product_id: $productId")
            
            val reviews = SupabaseClient.database
                .from("product_reviews")
                .select {
                    filter { eq("product_id", productId) }
                }
                .decodeList<ProductReviewDB>()
                .sortedByDescending { it.createdAt }
            
            Log.d(TAG, "📦 Reviews encontradas: ${reviews.size}")
            
            // Obtener usuarios
            val userIds = reviews.map { it.userId }.distinct()
            val usersMap = mutableMapOf<String, CommentUserDB>()
            
            for (userId in userIds) {
                try {
                    val user = SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeSingleOrNull<CommentUserDB>()
                    user?.let { usersMap[userId] = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Error obteniendo usuario $userId: ${e.message}")
                }
            }
            
            val commentsWithUser = reviews.map { review ->
                val user = usersMap[review.userId]
                CommentWithUser(
                    id = review.id,
                    postId = review.sourceId,
                    userId = review.userId,
                    username = user?.username ?: "Usuario",
                    avatarUrl = user?.avatarUrl,
                    text = review.text,
                    createdAt = review.createdAt,
                    likes = review.likes,
                    isLiked = false,
                    rating = review.rating,
                    parentId = review.parentId,
                    isVerified = user?.isVerified ?: false
                )
            }
            
            // Construir árbol de comentarios con respuestas anidadas
            _comments.value = buildCommentTree(commentsWithUser)
            Log.d(TAG, "✅ Reviews cargadas: ${commentsWithUser.size} (${_comments.value.size} raíz)")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando product reviews: ${e.message}", e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Agrega una review a la tabla unificada product_reviews.
     */
    suspend fun addProductReview(
        productId: String,
        sourceId: String,
        sourceType: String, // "post" o "rend"
        text: String,
        userAvatar: String? = null,
        userName: String = "Usuario",
        rating: Int = 5,
        isVerified: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: return@withContext false
            
            // Asegurar datos de usuario para optimistic update
            val (resolvedName, resolvedAvatar) = ensureCurrentUserData(userName, userAvatar)
            
            val reviewId = UUID.randomUUID().toString()
            
            val newReview = buildJsonObject {
                put("id", reviewId)
                put("product_id", productId)
                put("user_id", userId)
                put("source_type", sourceType)
                put("source_id", sourceId)
                put("text", text)
                put("rating", rating)
                put("likes", 0)
            }
            
            Log.d(TAG, "📝 Insertando review en product_reviews: $newReview")
            
            SupabaseClient.database
                .from("product_reviews")
                .insert(newReview)
            
            Log.d(TAG, "✅ Review agregada exitosamente: $reviewId (user=$resolvedName)")
            
            // El trigger de Supabase se encarga de incrementar reviews_count automáticamente
            
            // Agregar localmente con datos de usuario resueltos
            // Usar ProfileRepository como fallback para isVerified (evita race condition)
            val resolvedIsVerified = isVerified || (ProfileRepository.currentProfile.value?.isVerified == true)
            val localComment = CommentWithUser(
                id = reviewId,
                postId = sourceId,
                userId = userId,
                username = resolvedName,
                avatarUrl = resolvedAvatar,
                text = text,
                createdAt = java.time.Instant.now().toString(),
                likes = 0,
                isLiked = false,
                rating = rating,
                isVerified = resolvedIsVerified
            )
            
            val updatedComments = listOf(localComment) + _comments.value
            _comments.value = updatedComments
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error agregando review: ${e.message}", e)
            _lastError.value = "Error: ${e.message ?: "Error desconocido al publicar"}"
            false
        }
    }
    
    /**
     * Like en review de la tabla unificada
     */
    suspend fun likeProductReview(reviewId: String) = withContext(Dispatchers.IO) {
        try {
            val currentComments = _comments.value.toMutableList()
            val index = currentComments.indexOfFirst { it.id == reviewId }
            
            if (index >= 0) {
                // Like en comentario raíz
                val comment = currentComments[index]
                val newLikes = if (comment.isLiked) comment.likes - 1 else comment.likes + 1
                currentComments[index] = comment.copy(
                    likes = newLikes,
                    isLiked = !comment.isLiked
                )
                _comments.value = currentComments
                
                SupabaseClient.database
                    .from("product_reviews")
                    .update(mapOf("likes" to newLikes)) {
                        filter { eq("id", reviewId) }
                    }
            } else {
                // Buscar en respuestas anidadas
                var newLikesValue = 0
                val updatedComments = currentComments.map { comment ->
                    val replyIndex = comment.replies.indexOfFirst { it.id == reviewId }
                    if (replyIndex >= 0) {
                        val reply = comment.replies[replyIndex]
                        val newLikes = if (reply.isLiked) reply.likes - 1 else reply.likes + 1
                        newLikesValue = newLikes
                        val updatedReplies = comment.replies.toMutableList()
                        updatedReplies[replyIndex] = reply.copy(
                            likes = newLikes,
                            isLiked = !reply.isLiked
                        )
                        comment.copy(replies = updatedReplies)
                    } else comment
                }
                _comments.value = updatedComments
                
                if (newLikesValue >= 0) {
                    SupabaseClient.database
                        .from("product_reviews")
                        .update(mapOf("likes" to newLikesValue)) {
                            filter { eq("id", reviewId) }
                        }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error likeando review: ${e.message}", e)
        }
        Unit
    }
    
    /**
     * Eliminar una review/respuesta de la tabla unificada
     */
    suspend fun deleteProductReview(reviewId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("product_reviews")
                .delete {
                    filter { eq("id", reviewId) }
                }
            
            // Eliminar localmente - buscar en raíz y en respuestas
            val currentComments = _comments.value.toMutableList()
            val rootIndex = currentComments.indexOfFirst { it.id == reviewId }
            if (rootIndex >= 0) {
                currentComments.removeAt(rootIndex)
            } else {
                // Buscar en respuestas
                for (i in currentComments.indices) {
                    val comment = currentComments[i]
                    val replyIndex = comment.replies.indexOfFirst { it.id == reviewId }
                    if (replyIndex >= 0) {
                        val updatedReplies = comment.replies.toMutableList()
                        updatedReplies.removeAt(replyIndex)
                        currentComments[i] = comment.copy(
                            replies = updatedReplies,
                            replyCount = updatedReplies.size
                        )
                        break
                    }
                }
            }
            _comments.value = currentComments
            
            Log.d(TAG, "Review eliminada: $reviewId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando review: ${e.message}", e)
            false
        }
    }
    
    private var currentProductId: String? = null
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // SISTEMA DE RESPUESTAS A OPINIONES
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * Construye un árbol de comentarios agrupando respuestas bajo sus padres.
     * Los comentarios raíz (parentId == null) se muestran como principales,
     * y sus respuestas se anidan dentro del campo `replies`.
     */
    private fun buildCommentTree(allComments: List<CommentWithUser>): List<CommentWithUser> {
        val rootComments = allComments.filter { it.parentId == null }
        val repliesByParent = allComments.filter { it.parentId != null }.groupBy { it.parentId }
        
        return rootComments.map { root ->
            val replies = repliesByParent[root.id]?.sortedBy { it.createdAt } ?: emptyList()
            root.copy(
                replies = replies,
                replyCount = replies.size
            )
        }.sortedByDescending { it.createdAt }
    }
    
    /**
     * Agrega una respuesta a una review en la tabla unificada product_reviews.
     */
    suspend fun addProductReviewReply(
        productId: String,
        parentId: String,
        sourceId: String,
        sourceType: String,
        text: String,
        userAvatar: String? = null,
        userName: String = "Usuario"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = getCurrentUserId() ?: return@withContext false
            
            // Asegurar datos de usuario para optimistic update
            val (resolvedName, resolvedAvatar) = ensureCurrentUserData(userName, userAvatar)
            
            val replyId = UUID.randomUUID().toString()
            
            val newReply = buildJsonObject {
                put("id", replyId)
                put("product_id", productId)
                put("user_id", userId)
                put("source_type", sourceType)
                put("source_id", sourceId)
                put("text", text)
                put("rating", 5)
                put("likes", 0)
                put("parent_id", parentId)
            }
            
            Log.d(TAG, "💬 Insertando respuesta en product_reviews: parent=$parentId")
            
            SupabaseClient.database
                .from("product_reviews")
                .insert(newReply)
            
            Log.d(TAG, "✅ Respuesta a review agregada: $replyId (user=$resolvedName)")
            
            val localReply = CommentWithUser(
                id = replyId,
                postId = sourceId,
                userId = userId,
                username = resolvedName,
                avatarUrl = resolvedAvatar,
                text = text,
                createdAt = java.time.Instant.now().toString(),
                likes = 0,
                isLiked = false,
                rating = 5,
                parentId = parentId,
                isVerified = ProfileRepository.currentProfile.value?.isVerified == true
            )
            
            _comments.value = _comments.value.map { comment ->
                if (comment.id == parentId) {
                    comment.copy(
                        replies = comment.replies + localReply,
                        replyCount = comment.replyCount + 1
                    )
                } else comment
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error agregando respuesta a review: ${e.message}", e)
            _lastError.value = "Error: ${e.message ?: "Error desconocido al responder"}"
            false
        }
    }
}
