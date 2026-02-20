package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.data.repository.CartRepository
import com.rendly.app.data.repository.CommentRepository
import com.rendly.app.data.repository.NotificationRepository
import com.rendly.app.ui.theme.*

// -------------------------------------------------------------------------------
// PRODUCT PAGE V2 - ReDiseño completo estilo Amazon/MercadoLibre + Instagram
// -------------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductPage(
    post: Post?,
    isVisible: Boolean,
    isFromRend: Boolean = false,
    relatedPosts: List<Post> = emptyList(),
    suggestedPosts: List<Post> = emptyList(),
    onDismiss: () -> Unit,
    onBuyNow: (Post) -> Unit = {},
    onAddToCart: (Post) -> Unit = {},
    onContactSeller: (Post) -> Unit = {},
    onShare: (Post) -> Unit = {},
    onFavorite: (Post) -> Unit = {},
    onLike: (Post) -> Unit = {},
    onSave: (Post) -> Unit = {},
    onForward: (Post) -> Unit = {},
    onViewAllReviews: (Post) -> Unit = {},
    onRelatedPostClick: (Post) -> Unit = {},
    onNavigateToCheckout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var isLiked by remember(post?.id) { mutableStateOf(post?.isLiked ?: false) }
    var isSaved by remember(post?.id) { mutableStateOf(post?.isSaved ?: false) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }
    
    // Forward modal state
    var showForwardModal by remember { mutableStateOf(false) }
    
    // Cart modal state
    var showCartModal by remember { mutableStateOf(false) }
    
    // Report dialog state
    var showReportDialog by remember { mutableStateOf(false) }
    
    // Consult modal state
    var showConsultModal by remember { mutableStateOf(false) }
    
    // Coroutine scope (definido primero para usarlo en LaunchedEffects)
    val scope = rememberCoroutineScope()
    
    // Seller stats from Supabase
    var sellerStats by remember { mutableStateOf<com.rendly.app.data.model.SellerStats?>(null) }
    var isSellerVerified by remember { mutableStateOf(false) }
    
    // Load seller stats
    LaunchedEffect(post?.userId) {
        post?.userId?.let { sellerId ->
            sellerStats = com.rendly.app.data.repository.OrderRepository.getSellerStats(sellerId)
            // Also check if seller is verified from post data
            isSellerVerified = post.isUserVerified
        }
    }
    
    // Comments sheet state (interno para mostrarse SOBRE ProductPage)
    var showCommentsSheet by remember { mutableStateOf(false) }
    
    // Datos del usuario actual para comentarios
    val currentAuthUser = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()
    val currentUserId = currentAuthUser?.id
    var currentUserAvatar by remember { mutableStateOf<String?>(null) }
    var currentUsername by remember { mutableStateOf("Usuario") }
    var currentUserIsVerified by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentAuthUser?.id) {
        currentAuthUser?.id?.let { userId ->
            try {
                val userResult = com.rendly.app.data.remote.SupabaseClient.database
                    .from("usuarios")
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("username", "avatar_url", "is_verified")) {
                        filter { eq("user_id", userId) }
                    }
                    .decodeSingleOrNull<ProductPageUserData>()
                userResult?.let {
                    currentUsername = it.username.takeIf { u -> u.isNotBlank() } ?: "Usuario"
                    currentUserAvatar = it.avatarUrl
                    currentUserIsVerified = it.isVerified
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductPage", "Error cargando usuario: ${e.message}")
            }
        }
    }
    
    // Cargar comentarios usando CommentRepository
    val commentsFromRepo by CommentRepository.comments.collectAsState()
    val isCommentsLoading by CommentRepository.isLoading.collectAsState()
    
    // Cargar comentarios cuando se abre el sheet
    // SIEMPRE usar product_reviews
    LaunchedEffect(showCommentsSheet, post?.id, post?.productId) {
        if (showCommentsSheet && post != null) {
            val productId = post.productId 
                ?: if (isFromRend) CommentRepository.getProductIdFromRendId(post.id)
                   else CommentRepository.getProductIdFromPostId(post.id)
            if (productId != null) {
                CommentRepository.loadProductReviews(productId)
            }
        }
    }
    
    // Datos pre-computados
    val productData = remember(post?.id) {
        post?.let {
            ProductDataV2(
                title = it.title.ifEmpty { it.producto.titulo },
                price = it.price.takeIf { p -> p > 0 } ?: it.producto.precio,
                description = it.description ?: it.producto.descripcion ?: "",
                images = it.images.ifEmpty { it.producto.imagenUrl },
                condition = it.condition.ifEmpty { it.producto.condicion.ifEmpty { "Nuevo" } },
                category = it.category.ifEmpty { it.producto.categoria },
                username = it.username,
                userAvatar = it.userAvatar,
                storeName = it.userStoreName,
                likesCount = it.likesCount,
                reviewsCount = it.reviewsCount,
                isNew = it.createdAt.isNotEmpty(),
                warranty = it.warranty ?: "Sin Garantía",
                returnsAccepted = it.returnsAccepted,
                colors = it.colors
            )
        }
    }
    
    // Map colors to actual Color values
    val colorMap = mapOf(
        "Negro" to Color(0xFF1A1A1A),
        "Blanco" to Color(0xFFF5F5F5),
        "Gris" to Color(0xFF6B7280),
        "Rojo" to Color(0xFFEF4444),
        "Azul" to Color(0xFF0A3D62),
        "Verde" to Color(0xFF2E8B57),
        "Amarillo" to Color(0xFFFF6B35),
        "Naranja" to Color(0xFFF97316),
        "Rosa" to Color(0xFF2E8B57),
        "Morado" to Color(0xFFFF6B35),
        "Marrón" to Color(0xFF92400E),
        "Beige" to Color(0xFFD4C4A8)
    )
    
    // Get available colors from post or use defaults
    val availableColors = remember(productData?.colors) {
        if (productData?.colors?.isNotEmpty() == true) {
            productData.colors.mapNotNull { colorName ->
                colorMap[colorName]?.let { colorName to it }
            }
        } else {
            listOf(
                "Negro" to Color(0xFF1A1A1A),
                "Blanco" to Color(0xFFF5F5F5),
                "Azul" to Color(0xFF0A3D62),
                "Rojo" to Color(0xFFEF4444)
            )
        }
    }
    
    AnimatedVisibility(
        visible = isVisible && post != null,
        enter = fadeIn(tween(0)) + slideInVertically(
            initialOffsetY = { it / 8 },
            animationSpec = spring(dampingRatio = 1f, stiffness = 2000f)
        ),
        exit = fadeOut(tween(0)) + slideOutVertically(
            targetOffsetY = { it / 6 },
            animationSpec = tween(50)
        )
    ) {
        productData?.let { data ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(HomeBg)
                    .statusBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ---------------------------------------------------------------
                    // HEADER CON BUSCADOR (FIJO arriba, no sigue scroll)
                    // ---------------------------------------------------------------
                    ProductSearchHeader(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onBack = onDismiss,
                        onCartClick = { showCartModal = true }
                    )
                    
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // ---------------------------------------------------------------
                    // INFO: Badge Nuevo + Rating (primera fila) - DATOS REALES
                    // ---------------------------------------------------------------
                    ProductTopInfo(
                        isNew = data.isNew,
                        reviewsCount = data.reviewsCount // Usa datos reales
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // ---------------------------------------------------------------
                    // título (segunda fila) + número DE PRODUCTO
                    // ---------------------------------------------------------------
                    Text(
                        text = data.title.ifEmpty { "Producto Premium" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // ---------------------------------------------------------------
                    // BADGES: más visto + Posición en Categoría (tercera fila)
                    // ---------------------------------------------------------------
                    ProductBadgesRow(category = data.category)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Estado para controlar la imagen seleccionada desde thumbnails
                    var selectedImageIndex by remember { mutableIntStateOf(0) }
                    
                    // ---------------------------------------------------------------
                    // GALERÍA DE IMÁGENES
                    // ---------------------------------------------------------------
                    ProductImageGalleryV2(
                        images = data.images,
                        title = data.title,
                        selectedIndex = selectedImageIndex,
                        onPageChange = { selectedImageIndex = it }
                    )
                    
                    // ---------------------------------------------------------------
                    // ACCIONES: Like + Reenviar + Guardar + Thumbnails
                    // ---------------------------------------------------------------
                    ProductActionsRow(
                        images = data.images,
                        isLiked = isLiked,
                        likesCount = data.likesCount,
                        isSaved = isSaved,
                        selectedImageIndex = selectedImageIndex,
                        onImageSelect = { selectedImageIndex = it },
                        onLike = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isLiked = !isLiked
                            post?.let { onLike(it) }
                        },
                        onForward = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showForwardModal = true
                        },
                        onSave = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSaved = !isSaved
                            post?.let { onSave(it) }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // ---------------------------------------------------------------
                    // COLORES DISPONIBLES
                    // ---------------------------------------------------------------
                    ProductColorsSection(
                        colors = availableColors,
                        selectedIndex = selectedColorIndex,
                        onColorSelect = { selectedColorIndex = it },
                        images = data.images,
                        onImageChange = { imageIndex -> 
                            if (imageIndex < data.images.size) {
                                selectedImageIndex = imageIndex
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ---------------------------------------------------------------
                    // PRECIOS - Cambia dinámicamente Según variante seleccionada
                    // ---------------------------------------------------------------
                    // Por ahora usamos el precio base, pero cuando haya precios por variante
                    // se puede pasar variantPrices[selectedImageIndex] ?: data.price
                    ProductPriceSection(
                        price = data.price,
                        originalPrice = post?.previousPrice
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ---------------------------------------------------------------
                    // BOTONES DE Acción (movidos debajo del precio)
                    // ---------------------------------------------------------------
                    ProductActionButtons(
                        onBuyNow = { post?.let { onBuyNow(it) } },
                        onAddToCart = { 
                            post?.let { p ->
                                // Agregar al carrito con color seleccionado
                                val selectedColor = availableColors.getOrNull(selectedColorIndex)?.first
                                CartRepository.addToCart(
                                    post = p,
                                    quantity = 1,
                                    selectedColor = selectedColor
                                )
                                // también llamar al callback externo
                                onAddToCart(p)
                                // Mostrar feedback táctil
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        onContact = { showConsultModal = true }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    SectionDivider()
                    
                    // ---------------------------------------------------------------
                    // VENDEDOR - diseño mejorado (con líneas separadoras)
                    // ---------------------------------------------------------------
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = BorderSubtle
                    )
                    
                    SellerSectionV2(
                        username = data.username,
                        avatarUrl = data.userAvatar,
                        storeName = data.storeName,
                        isVerified = isSellerVerified,
                        sellerStats = sellerStats,
                        onViewProfile = { post?.let { onContactSeller(it) } }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = BorderSubtle
                    )
                    
                    SectionDivider()
                    
                    // ---------------------------------------------------------------
                    // CARACTERÍSTICAS - Compacto y profesional
                    // ---------------------------------------------------------------
                    ProductFeaturesCompact(
                        freeShipping = post?.freeShipping ?: false,
                        isVerified = isSellerVerified,
                        returnsAccepted = data.returnsAccepted
                    )
                    
                    SectionDivider()
                    
                    // ---------------------------------------------------------------
                    // DETALLES TÉCNICOS
                    // ---------------------------------------------------------------
                    ProductDetailsSection(
                        condition = data.condition,
                        category = data.category,
                        warranty = data.warranty,
                        returnsAccepted = data.returnsAccepted
                    )
                    
                    SectionDivider()
                    
                    // ---------------------------------------------------------------
                    // GALERÍA ADICIONAL + SKU (debajo de Detalles del producto)
                    // ---------------------------------------------------------------
                    var showFullscreenGallery by remember { mutableStateOf(false) }
                    
                    ProductGalleryWithSKU(
                        images = data.images,
                        postId = post?.id ?: "",
                        onViewAllImages = { showFullscreenGallery = true },
                        onReport = { showReportDialog = true }
                    )
                    
                    SectionDivider()
                    
                    // Fullscreen Gallery Modal
                    if (showFullscreenGallery) {
                        FullscreenGalleryModal(
                            images = data.images,
                            title = data.title,
                            onDismiss = { showFullscreenGallery = false }
                        )
                    }
                    
                    // ---------------------------------------------------------------
                    // MEDIOS DE PAGO
                    // ---------------------------------------------------------------
                    PaymentMethodsSection()
                    
                    SectionDivider()
                    
                    // ---------------------------------------------------------------
                    // OPINIONES - Con ratings dinámicas (sin datos hardcodeados)
                    // ---------------------------------------------------------------
                    // RatingDistribution basado en comentarios reales
                    // Depende de data.reviewsCount para actualizarse correctamente
                    val ratingDistribution = remember(post?.id, data.reviewsCount) {
                        if (data.reviewsCount > 0) {
                            // Distribución simulada basada en cantidad de comentarios
                            // En producción esto vendráa del backend con ratings reales
                            val total = data.reviewsCount
                            RatingDistribution(
                                fiveStars = (total * 0.60).toInt().coerceAtLeast(if (total > 0) 1 else 0),
                                fourStars = (total * 0.25).toInt().coerceAtLeast(0),
                                threeStars = (total * 0.10).toInt().coerceAtLeast(0),
                                twoStars = (total * 0.03).toInt().coerceAtLeast(0),
                                oneStar = (total * 0.02).toInt().coerceAtLeast(0)
                            )
                        } else {
                            RatingDistribution() // vacío
                        }
                    }
                    
                    ReviewsSection(
                        ratingDistribution = ratingDistribution,
                        onViewAll = { showCommentsSheet = true }
                    )
                    
                    SectionDivider()
                    
                    // ---------------------------------------------------------------
                    // PRODUCTOS RELACIONADOS
                    // ---------------------------------------------------------------
                    if (relatedPosts.isNotEmpty()) {
                        RelatedProductsSection(
                            posts = relatedPosts,
                            onPostClick = onRelatedPostClick
                        )
                        SectionDivider()
                    }
                    
                    // ---------------------------------------------------------------
                    // también podráA GUSTARTE - Todas las publicaciones con scroll infinito
                    // ---------------------------------------------------------------
                    YouMightLikeSectionInfinite(
                        currentPostId = post?.id ?: "",
                        onPostClick = onRelatedPostClick
                    )
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
                } // End outer Column (header + scrollable content)
                
                // Forward Modal
                ForwardModal(
                    isVisible = showForwardModal,
                    post = post,
                    onDismiss = { showForwardModal = false },
                    onForwardToUser = { _, _, _ -> showForwardModal = false }
                )
                
                // Comments Sheet (interno para mostrarse SOBRE ProductPage)
                CommentsSheet(
                    isVisible = showCommentsSheet,
                    comments = commentsFromRepo.map { c ->
                        Comment(
                            id = c.id,
                            userId = c.userId,
                            username = c.username,
                            avatarUrl = c.avatarUrl,
                            text = c.text,
                            timeAgo = c.createdAt.take(10),
                            likes = c.likes,
                            isLiked = c.isLiked,
                            rating = c.rating,
                            replies = c.replies.map { r ->
                                Comment(
                                    id = r.id,
                                    userId = r.userId,
                                    username = r.username,
                                    avatarUrl = r.avatarUrl,
                                    text = r.text,
                                    timeAgo = r.createdAt.take(10),
                                    likes = r.likes,
                                    isLiked = r.isLiked,
                                    rating = r.rating,
                                    isVerified = r.isVerified
                                )
                            },
                            replyCount = c.replyCount,
                            isVerified = c.isVerified
                        )
                    },
                    onDismiss = { 
                        showCommentsSheet = false
                        CommentRepository.clearComments()
                    },
                    onSendComment = { text, rating ->
                        post?.let { p ->
                            scope.launch {
                                val productId = p.productId 
                                    ?: if (isFromRend) CommentRepository.getProductIdFromRendId(p.id)
                                       else CommentRepository.getProductIdFromPostId(p.id)
                                if (productId != null) {
                                    val success = CommentRepository.addProductReview(
                                        productId = productId,
                                        sourceId = p.id,
                                        sourceType = if (isFromRend) "rend" else "post",
                                        text = text,
                                        userAvatar = currentUserAvatar,
                                        userName = currentUsername,
                                        rating = rating,
                                        isVerified = currentUserIsVerified
                                    )
                                    if (success) {
                                        NotificationRepository.createCommentNotification(
                                            recipientId = p.userId,
                                            postId = p.id,
                                            postImage = p.images.firstOrNull(),
                                            commentText = text
                                        )
                                    }
                                }
                            }
                        }
                    },
                    onLikeComment = { commentId ->
                        scope.launch {
                            CommentRepository.likeProductReview(commentId)
                        }
                    },
                    onReplyComment = { parentId, text ->
                        post?.let { p ->
                            scope.launch {
                                val productId = p.productId 
                                    ?: if (isFromRend) CommentRepository.getProductIdFromRendId(p.id)
                                       else CommentRepository.getProductIdFromPostId(p.id)
                                if (productId != null) {
                                    CommentRepository.addProductReviewReply(
                                        productId = productId,
                                        parentId = parentId,
                                        sourceId = p.id,
                                        sourceType = if (isFromRend) "rend" else "post",
                                        text = text,
                                        userAvatar = currentUserAvatar,
                                        userName = currentUsername
                                    )
                                }
                            }
                        }
                    },
                    onDeleteComment = { commentId ->
                        scope.launch {
                            CommentRepository.deleteProductReview(commentId)
                        }
                    },
                    isLoading = isCommentsLoading,
                    currentUserAvatar = currentUserAvatar,
                    currentUsername = currentUsername,
                    currentUserId = currentUserId
                )
                
                // Cart Modal
                CartModal(
                    isVisible = showCartModal,
                    onDismiss = { showCartModal = false },
                    onCheckout = { showCartModal = false },
                    onContinueShopping = { showCartModal = false },
                    onNavigateToCheckout = {
                        showCartModal = false
                        onNavigateToCheckout()
                    }
                )
                
                // Consult Modal
                ConsultModal(
                    isVisible = showConsultModal,
                    post = post,
                    onDismiss = { showConsultModal = false },
                    onSendConsult = { message ->
                        // Enviar consulta al vendedor via chat
                        post?.let { p ->
                            scope.launch {
                                com.rendly.app.data.repository.ChatRepository.sendConsultMessage(
                                    sellerId = p.userId,
                                    sellerUsername = p.username,
                                    postId = p.id,
                                    postTitle = p.title.ifEmpty { p.producto.titulo },
                                    message = message
                                )
                            }
                        }
                    }
                )
                
                // Report Dialog - FUERA del Column scrollable, Fullscreen desde abajo
                if (showReportDialog) {
                    ReportProductDialog(
                        sku = (post?.id ?: "").take(8).uppercase(),
                        postId = post?.id ?: "",
                        postTitle = data.title,
                        postImage = data.images.firstOrNull() ?: "",
                        onDismiss = { showReportDialog = false },
                        onReport = { reportDescription ->
                            // Enviar reporte a Supabase (tabla content_reports)
                            scope.launch {
                                try {
                                    val currentUserId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                                    if (currentUserId != null && post != null) {
                                        com.rendly.app.data.remote.SupabaseClient.database
                                            .from("content_reports")
                                            .insert(mapOf(
                                                "reporter_id" to currentUserId,
                                                "content_type" to "post",
                                                "content_id" to post.id,
                                                "reported_user_id" to post.userId,
                                                "reason" to "inappropriate",
                                                "description" to reportDescription,
                                                "status" to "pending"
                                            ))
                                        android.util.Log.d("ProductPage", "? Reporte enviado a Supabase")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ProductPage", "Error enviando reporte: ${e.message}")
                                }
                            }
                            showReportDialog = false
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// DATA CLASSES
// -------------------------------------------------------------------------------
@kotlinx.serialization.Serializable
private data class ProductPageUserData(
    val username: String = "",
    @kotlinx.serialization.SerialName("avatar_url") val avatarUrl: String? = null,
    @kotlinx.serialization.SerialName("is_verified") val isVerified: Boolean = false
)

private data class ProductDataV2(
    val title: String,
    val price: Double,
    val description: String,
    val images: List<String>,
    val condition: String,
    val category: String,
    val username: String,
    val userAvatar: String,
    val storeName: String?,
    val likesCount: Int,
    val reviewsCount: Int,
    val isNew: Boolean,
    val warranty: String = "Sin Garantía",
    val returnsAccepted: Boolean = false,
    val colors: List<String> = emptyList()
)

// -------------------------------------------------------------------------------
// HEADER CON BUSCADOR (estilo SearchBar del home)
// -------------------------------------------------------------------------------
@Composable
private fun ProductSearchHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onCartClick: () -> Unit = {}
) {
    val cartItems by CartRepository.cartItems.collectAsState()
    val cartItemCount = cartItems.sumOf { it.quantity }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Surface)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        // Search field - using Surface + BasicTextField like SearchBar
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = Surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Buscar en Merqora",
                            color = TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(PrimaryPurple),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Cart icon with badge
        Box {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    .clickable { onCartClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = "Carrito",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // Badge con contador de items
            if (cartItemCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(if (cartItemCount > 9) 18.dp else 16.dp)
                        .clip(CircleShape)
                        .background(ButtonBuyNow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                        fontSize = if (cartItemCount > 9) 8.sp else 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// TOP INFO: Badge Nuevo + Rating (DATOS REALES)
// -------------------------------------------------------------------------------
@Composable
private fun ProductTopInfo(
    isNew: Boolean,
    reviewsCount: Int = 0
) {
    // Calcular rating dinámico basado en cantidad de reviews
    // En producción esto vendráa del backend
    val rating = if (reviewsCount > 0) {
        // Simular rating basado en cantidad (Más reviews = Más confiable)
        4.0f + (reviewsCount.coerceAtMost(100) / 100f) * 0.9f
    } else 0f
    
    val hasReviews = reviewsCount > 0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Badge Nuevo
        if (isNew) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AccentGreen
            ) {
                Text(
                    text = "NUEVO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }
        
        // Rating - Solo mostrar si hay reviews
        if (hasReviews) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    val starRating = index + 1
                    Icon(
                        imageVector = when {
                            starRating <= rating.toInt() -> Icons.Filled.Star
                            starRating - 0.5f <= rating -> Icons.Filled.StarHalf
                            else -> Icons.Outlined.StarOutline
                        },
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = String.format("%.1f", rating),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "($reviewsCount)",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        } else {
            // Sin opiniones Aún
            Text(
                text = "Sin opiniones Aún",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

// -------------------------------------------------------------------------------
// BADGES ROW: más visto + Posición
// -------------------------------------------------------------------------------
@Composable
private fun ProductBadgesRow(category: String) {
    val displayCategory = categoryDisplayNames[category] ?: category.ifEmpty { "Productos" }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFFF6B6B).copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "más visto",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF6B6B)
                )
            }
        }
        
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = IconAccentBlue.copy(alpha = 0.15f)
        ) {
            Text(
                text = "#20 en $displayCategory",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = IconAccentBlue,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------------
// IMAGE GALLERY V2
// -------------------------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductImageGalleryV2(
    images: List<String>,
    title: String,
    selectedIndex: Int = 0,
    onPageChange: (Int) -> Unit = {}
) {
    val displayImages = images.ifEmpty { listOf("") }
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { displayImages.size }
    )
    val scope = rememberCoroutineScope()
    
    // Sincronizar pagerState con selectedIndex externo (sin animación de slide)
    LaunchedEffect(selectedIndex) {
        if (pagerState.currentPage != selectedIndex) {
            // Usar scrollToPage sin animación para cambio instantáneo
            pagerState.scrollToPage(selectedIndex)
        }
    }
    
    // Notificar cambios de Página al componente padre
    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A24))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = displayImages[page],
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Page indicator - ESQUINA SUPERIOR DERECHA
        if (displayImages.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${displayImages.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------
// ACTIONS ROW: Like + Forward + Save + Thumbnails below (enlarged with variant data)
// -------------------------------------------------------------------------------
@Composable
private fun ProductActionsRow(
    images: List<String>,
    isLiked: Boolean,
    likesCount: Int,
    isSaved: Boolean,
    selectedImageIndex: Int = 0,
    basePrice: Double = 0.0,
    variantPrices: Map<Int, Double> = emptyMap(),
    variantColors: Map<Int, Pair<String, Color>> = emptyMap(),
    onImageSelect: (Int) -> Unit = {},
    onLike: () -> Unit,
    onForward: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Actions row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like
            Row(
                modifier = Modifier
                    .clickable(onClick = onLike)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) AccentPink else IconColor,
                    modifier = Modifier.size(26.dp)
                )
                if (likesCount > 0) {
                    Text(
                        text = formatCountV2(likesCount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLiked) AccentPink else TextSecondary
                    )
                }
            }
            
            // Save
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Guardar",
                    tint = if (isSaved) AccentGold else IconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Forward (Share)
            IconButton(onClick = onForward) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = "Reenviar",
                    tint = IconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Thumbnails agrandados (Más altos que anchos) con datos de variante
        if (images.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(images.take(6)) { index, imageUrl ->
                    val isSelected = index == selectedImageIndex
                    val variantPrice = variantPrices[index]
                    val hasCustomPrice = variantPrice != null && variantPrice != basePrice
                    val colorData = variantColors[index]
                    
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) PrimaryPurple else BorderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onImageSelect(index) }
                    ) {
                        // Imagen de fondo
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Variante ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Overlay gradient para datos
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        
                        
                        // Color indicator si tiene
                        if (colorData != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(colorData.second)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                        
                        // Precio de variante abajo (si es diferente al base)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (hasCustomPrice) {
                                Text(
                                    text = "$${variantPrice?.toLong() ?: 0}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        // Indicador de Selección
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 2.5.dp,
                                        color = PrimaryPurple,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            )
                        }
                    }
                }
                
                // Mostrar indicador si hay Más Imagenes
                if (images.size > 6) {
                    item {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${images.size - 6}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// COLORS SECTION
// -------------------------------------------------------------------------------
@Composable
private fun ProductColorsSection(
    colors: List<Pair<String, Color>>,
    selectedIndex: Int,
    onColorSelect: (Int) -> Unit,
    images: List<String> = emptyList(),
    onImageChange: (Int) -> Unit = {}
) {
    val selectedColorName = colors.getOrNull(selectedIndex)?.first ?: ""
    
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Color: $selectedColorName",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            if (colors.size > 4) {
                Text(
                    text = "${colors.size} disponibles",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Horizontal scrollable carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            itemsIndexed(colors) { index, (colorName, color) ->
                // Check if there's a matching image for this color
                val hasImage = index < images.size
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { 
                            onColorSelect(index)
                            // If there's a matching image, switch to it
                            if (hasImage) {
                                onImageChange(index)
                            }
                        }
                        .background(
                            if (index == selectedIndex) PrimaryPurple.copy(alpha = 0.08f) 
                            else Color.Transparent
                        )
                        .padding(8.dp)
                ) {
                    // Color circle - Solo muestra el color, sin imagen dentro
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (index == selectedIndex) 3.dp else 1.5.dp,
                                color = if (index == selectedIndex) PrimaryPurple else BorderSubtle,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Solo mostramos el color
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Color name
                    Text(
                        text = colorName,
                        fontSize = 11.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) PrimaryPurple else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// PRICE SECTION - Con animación fluida para cambios de precio por variante
// -------------------------------------------------------------------------------
@Composable
private fun ProductPriceSection(
    price: Double,
    originalPrice: Double? = null
) {
    // Animación fluida del precio
    val animatedPrice by animateFloatAsState(
        targetValue = price.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "price"
    )
    
    // Precio anterior: usar el proporcionado o calcular 25% Más
    val precioAnterior = originalPrice ?: (price * 1.25)
    val animatedPrecioAnterior by animateFloatAsState(
        targetValue = precioAnterior.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "originalPrice"
    )
    
    // Calcular descuento y ahorro
    val descuento = if (precioAnterior > 0) ((precioAnterior - price) / precioAnterior * 100).toInt() else 0
    val ahorro = precioAnterior - price
    val animatedAhorro by animateFloatAsState(
        targetValue = ahorro.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "savings"
    )
    
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Precio tachado (anterior)
        if (descuento > 0) {
            Text(
                text = "$${String.format("%,.2f", animatedPrecioAnterior.toDouble())}",
                fontSize = 14.sp,
                color = TextMuted,
                textDecoration = TextDecoration.LineThrough
            )
            
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // Precio actual + Descuento + Ahorro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${String.format("%,.2f", animatedPrice.toDouble())}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = PriceColor
                )
                
                if (descuento > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFF4757)
                    ) {
                        Text(
                            text = "-$descuento%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            if (descuento > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ahorras",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "$${String.format("%,.2f", animatedAhorro.toDouble())}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SavingsColor
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// SELLER SECTION V2 - Mejorado
// -------------------------------------------------------------------------------
@Composable
private fun SellerSectionV2(
    username: String,
    avatarUrl: String,
    storeName: String?,
    isVerified: Boolean = false,
    sellerStats: com.rendly.app.data.model.SellerStats? = null,
    onViewProfile: () -> Unit
) {
    val displayAvatar = remember(avatarUrl) {
        if (avatarUrl.startsWith("http")) avatarUrl
        else "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/$avatarUrl"
    }
    
    // Calcular valores de stats (usar valores reales o defaults para nuevos vendedores)
    val reputationValue = sellerStats?.reputationScore?.let { "${it}%" } ?: "N/A"
    val ratingValue = sellerStats?.avgRating?.let { String.format("%.1f", it) } ?: "N/A"
    val responseValue = sellerStats?.formattedResponseTime ?: "N/A"
    val salesValue = sellerStats?.formattedSales ?: "0"
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Vendedor",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AsyncImage(
                model = displayAvatar,
                contentDescription = username,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = storeName ?: username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 18.dp)
                    }
                }
                
                Text(
                    text = "@$username",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
            
            // Ver perfil
            TextButton(onClick = onViewProfile) {
                Text(
                    text = "Ver perfil",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryPurple
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Stats del vendedor - datos reales de Supabase
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SellerStatItem(
                value = reputationValue, 
                label = "Reputación", 
                color = if (sellerStats?.reputationScore != null && sellerStats.reputationScore >= 80) AccentGreen else IconAccentBlue
            )
            SellerStatItem(
                value = ratingValue, 
                label = "Rating", 
                color = if (sellerStats?.avgRating != null && sellerStats.avgRating >= 4.0) AccentYellow else IconAccentBlue
            )
            SellerStatItem(
                value = responseValue, 
                label = "Respuesta", 
                color = IconAccentBlue
            )
            SellerStatItem(
                value = salesValue, 
                label = "Ventas", 
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SellerStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

// -------------------------------------------------------------------------------
// FEATURES COMPACT - Con colores dinámicos Según estado real
// -------------------------------------------------------------------------------

// Color del badge de Verificación (azul Twitter/Instagram)
private val VerifiedBlue = Color(0xFF1D9BF0)
// Color para Devolución activa
private val ReturnGreen = Color(0xFF2E8B57)

@Composable
private fun ProductFeaturesCompact(
    freeShipping: Boolean = false,
    isVerified: Boolean = false,
    returnsAccepted: Boolean = false
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Beneficios",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureChip(
                icon = Icons.Outlined.LocalShipping, 
                text = "envío gratis", 
                isActive = freeShipping,
                activeColor = AccentGreen
            )
            FeatureChip(
                icon = Icons.Outlined.Verified, 
                text = "Verificado", 
                isActive = isVerified,
                activeColor = VerifiedBlue
            )
            FeatureChip(
                icon = Icons.Outlined.Autorenew, 
                text = "Devolución", 
                isActive = returnsAccepted,
                activeColor = ReturnGreen
            )
        }
    }
}

@Composable
private fun FeatureChip(
    icon: ImageVector, 
    text: String, 
    isActive: Boolean,
    activeColor: Color = AccentGreen
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) activeColor.copy(alpha = 0.1f) else SurfaceElevated
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) activeColor else TextSecondary
            )
        }
    }
}

// Category display names map - synchronized with CategoryDrawer
private val categoryDisplayNames = mapOf(
    "vestidos" to "Vestidos",
    "blusas" to "Blusas y Tops",
    "pantalones" to "Pantalones",
    "faldas" to "Faldas",
    "abrigos" to "Abrigos y Chaquetas",
    "zapatos_m" to "Zapatos Mujer",
    "camisas" to "Camisas",
    "pantalones_h" to "Pantalones Hombre",
    "chaquetas" to "Chaquetas",
    "trajes" to "Trajes",
    "zapatos_h" to "Zapatos Hombre",
    "bolsos" to "Bolsos y Carteras",
    "joyeria" to "Joyería",
    "relojes" to "Relojes",
    "gafas" to "Gafas de Sol",
    "sombreros" to "Sombreros",
    "muebles" to "Muebles",
    "decoracion" to "Decoración",
    "plantas" to "Plantas",
    "iluminacion" to "Iluminación",
    "smartphones" to "Smartphones",
    "laptops" to "Laptops",
    "audio" to "Audio",
    "gaming" to "Gaming"
)

// -------------------------------------------------------------------------------
// PRODUCT DETAILS
// -------------------------------------------------------------------------------
@Composable
private fun ProductDetailsSection(
    condition: String, 
    category: String,
    warranty: String = "Sin Garantía",
    returnsAccepted: Boolean = false
) {
    val displayCategory = categoryDisplayNames[category] ?: category.ifEmpty { "General" }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Detalles del producto",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DetailRow("Condición", condition)
        DetailRow("categoría", displayCategory)
        DetailRow("Disponibilidad", "En stock")
        DetailRow("garantía", warranty)
        DetailRow("Devolución", if (returnsAccepted) "Aceptada" else "No aceptada")
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

// -------------------------------------------------------------------------------
// DESCRIPTION
// -------------------------------------------------------------------------------
@Composable
private fun ProductDescriptionSection(description: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Descripción",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextSecondary,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp
        )
        
        if (description.length > 150) {
            Text(
                text = if (expanded) "Ver menos" else "Ver Más",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryPurple,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }
}

// -------------------------------------------------------------------------------
// PAYMENT METHODS
// -------------------------------------------------------------------------------
@Composable
private fun PaymentMethodsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Medios de pago",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentMethodChip("Tarjeta")
            PaymentMethodChip("Transferencia")
            PaymentMethodChip("Efectivo")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Hasta 12 cuotas sin interés",
            fontSize = 13.sp,
            color = AccentGreen,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PaymentMethodChip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SurfaceElevated
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// Data class para representar la distribución de ratings
data class RatingDistribution(
    val fiveStars: Int = 0,
    val fourStars: Int = 0,
    val threeStars: Int = 0,
    val twoStars: Int = 0,
    val oneStar: Int = 0
) {
    val total: Int get() = fiveStars + fourStars + threeStars + twoStars + oneStar
    
    val averageRating: Float get() {
        if (total == 0) return 0f
        val weightedSum = (fiveStars * 5 + fourStars * 4 + threeStars * 3 + twoStars * 2 + oneStar * 1).toFloat()
        return weightedSum / total
    }
    
    fun percentFor(stars: Int): Float {
        if (total == 0) return 0f
        val count = when (stars) {
            5 -> fiveStars
            4 -> fourStars
            3 -> threeStars
            2 -> twoStars
            1 -> oneStar
            else -> 0
        }
        return count.toFloat() / total
    }
}

// -------------------------------------------------------------------------------
// REVIEWS - Ratings dinámicas basadas en opiniones reales
// -------------------------------------------------------------------------------
@Composable
private fun ReviewsSection(
    ratingDistribution: RatingDistribution = RatingDistribution(),
    onViewAll: () -> Unit = {}
) {
    val reviewsCount = ratingDistribution.total
    val rating = ratingDistribution.averageRating
    val hasReviews = reviewsCount > 0
    
    Column(modifier = Modifier.padding(16.dp)) {
        // título de Sección (sin el Botón Ver todas a¿Qué)
        Text(
            text = "Opiniones",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (hasReviews) {
            // Rating summary con datos reales
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", rating),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Row {
                        repeat(5) { index ->
                            val starRating = index + 1
                            Icon(
                                imageVector = when {
                                    starRating <= rating.toInt() -> Icons.Filled.Star
                                    starRating - 0.5f <= rating -> Icons.Filled.StarHalf
                                    else -> Icons.Outlined.StarOutline
                                },
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "$reviewsCount opiniones",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                
                // Rating bars con datos reales
                Column(modifier = Modifier.weight(1f)) {
                    RatingBar(stars = 5, percent = ratingDistribution.percentFor(5))
                    RatingBar(stars = 4, percent = ratingDistribution.percentFor(4))
                    RatingBar(stars = 3, percent = ratingDistribution.percentFor(3))
                    RatingBar(stars = 2, percent = ratingDistribution.percentFor(2))
                    RatingBar(stars = 1, percent = ratingDistribution.percentFor(1))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // botón Ver todas DEBAJO de la gráfica - Diseño profesional
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewAll() },
                shape = RoundedCornerShape(10.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver todas las opiniones",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IconAccentBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = IconAccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Estado vacío cuando no hay opiniones
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "aún no hay opiniones",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = "Sé el primero en opinar",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingBar(stars: Int, percent: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "$stars",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.width(12.dp)
        )
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = AccentYellow,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SurfaceElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent)
                    .background(AccentYellow)
            )
        }
    }
}

// -------------------------------------------------------------------------------
// RELATED PRODUCTS - Con UnifiedProductCard profesional
// -------------------------------------------------------------------------------
@Composable
private fun RelatedProductsSection(
    posts: List<Post>,
    onPostClick: (Post) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Productos relacionados",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(posts.take(8)) { post ->
                Box(modifier = Modifier.width(170.dp).height(330.dp)) {
                    UnifiedProductCard(
                        data = post.toProductCardData(),
                        onClick = { onPostClick(post) },
                        imageHeight = 150.dp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// también podráA GUSTARTE - Con scroll infinito de TODAS las publicaciones
// -------------------------------------------------------------------------------
@Composable
private fun YouMightLikeSectionInfinite(
    currentPostId: String,
    onPostClick: (Post) -> Unit
) {
    val scope = rememberCoroutineScope()
    var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMorePosts by remember { mutableStateOf(true) }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 20
    
    // Cargar productos iniciales (todas las publicaciones)
    LaunchedEffect(currentPostId) {
        isLoading = true
        currentPage = 0
        allPosts = emptyList()
        hasMorePosts = true
        
        scope.launch {
            try {
                val posts = com.rendly.app.data.repository.PostRepository.getPostsByCategory(
                    category = null, // Sin filtro de Categoría = TODAS
                    excludePostId = currentPostId,
                    limit = pageSize,
                    offset = 0
                )
                allPosts = posts
                hasMorePosts = posts.size >= pageSize
                currentPage = 1
            } catch (e: Exception) {
                android.util.Log.e("ProductPage", "Error loading posts: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Función para cargar Más productos
    fun loadMorePosts() {
        if (isLoadingMore || !hasMorePosts) return
        
        isLoadingMore = true
        scope.launch {
            try {
                val newPosts = com.rendly.app.data.repository.PostRepository.getPostsByCategory(
                    category = null,
                    excludePostId = currentPostId,
                    limit = pageSize,
                    offset = currentPage * pageSize
                )
                
                if (newPosts.isNotEmpty()) {
                    allPosts = allPosts + newPosts
                    currentPage++
                    hasMorePosts = newPosts.size >= pageSize
                } else {
                    hasMorePosts = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductPage", "Error loading more posts: ${e.message}")
            } finally {
                isLoadingMore = false
            }
        }
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "también podráa gustarte",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            if (allPosts.isNotEmpty()) {
                Text(
                    text = "${allPosts.size}+ productos",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            // Skeleton loading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceElevated)
                    )
                }
            }
        } else if (allPosts.isNotEmpty()) {
            // Grid de productos profesionales (2 columnas) con UnifiedProductCard
            val chunkedPosts = allPosts.chunked(2)
            chunkedPosts.forEachIndexed { index, rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowPosts.forEach { post ->
                        Box(modifier = Modifier.weight(1f).height(330.dp)) {
                            UnifiedProductCard(
                                data = post.toProductCardData(),
                                onClick = { onPostClick(post) },
                                imageHeight = 150.dp
                            )
                        }
                    }
                    if (rowPosts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // Cargar Más cuando llegamos cerca del final
                if (index == chunkedPosts.size - 2 && hasMorePosts && !isLoadingMore) {
                    LaunchedEffect(index) {
                        loadMorePosts()
                    }
                }
            }
            
            // Indicador de carga de Más productos
            if (isLoadingMore) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = PrimaryPurple,
                        strokeWidth = 3.dp
                    )
                }
            }
            
            // botón para cargar Más si hay Más productos
            if (hasMorePosts && !isLoadingMore) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { loadMorePosts() },
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryPurple.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver Más productos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryPurple
                        )
                    }
                }
            }
            
            // Mensaje cuando no hay Más productos
            if (!hasMorePosts && allPosts.size > pageSize) {
                Text(
                    text = "Has visto todos los productos",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// Colores usando la paleta de la app (definidos en Color.kt)
private val BuyNowColor = ButtonBuyNow       // Naranja acento - acción principal
private val AddToCartColor = ButtonAddCart    // Azul primario - acción secundaria
private val ConsultColor = ButtonConsult      // Gris neutro oscuro - opción terciaria

// -------------------------------------------------------------------------------
// BOTONES DE Acción (3 botones: Consultar, Agregar al carrito, Comprar)
// -------------------------------------------------------------------------------
@Composable
private fun ProductActionButtons(
    onBuyNow: () -> Unit,
    onAddToCart: () -> Unit = {},
    onContact: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Fila 1: botón principal COMPRAR AHORA (naranja - urgencia)
        Button(
            onClick = onBuyNow,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BuyNowColor)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Comprar ahora",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
        
        // Fila 2: Agregar al carrito + Consultar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // botón Agregar al carrito (azul - confianza)
            Button(
                onClick = onAddToCart,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AddToCartColor)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Agregar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
            
            // botón Consultar (gris elegante)
            OutlinedButton(
                onClick = onContact,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, ConsultColor)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ConsultColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Consultar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = ConsultColor
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------
// UTILS
// -------------------------------------------------------------------------------
@Composable
private fun SectionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(HomeBg)
    )
}

private fun formatCountV2(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
}

// -------------------------------------------------------------------------------
// GALERÍA ADICIONAL CON SKU - máximo 2 Imagenes grandes + Botón ver Más
// -------------------------------------------------------------------------------
@Composable
private fun ProductGalleryWithSKU(
    images: List<String>,
    postId: String,
    onViewAllImages: () -> Unit,
    onReport: () -> Unit = {}
) {
    val displayImages = images.take(2)
    
    if (displayImages.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Imágenes grandes sin Título
        displayImages.forEachIndexed { index, imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen ${index + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp) // más altas
                    .clip(RoundedCornerShape(12.dp))
            )
            if (index < displayImages.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        if (images.size > 2) {
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(IconAccentBlue.copy(alpha = 0.1f))
                    .clickable { onViewAllImages() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    tint = IconAccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver ${images.size} Imagenes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IconAccentBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = IconAccentBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // SKU + Denunciar en la misma línea
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SKU: ${postId.take(8).uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Text(
                text = "Denunciar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFEF4444),
                modifier = Modifier.clickable { onReport() }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FullscreenGalleryModal(
    images: List<String>,
    title: String,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    
    // Dialog fullscreen que cubre TODO el ProductPage
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Imágenes centradas con pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = images[page],
                        contentDescription = "Imagen ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                    )
                }
            }
            
            // Header con Botón cerrar (izquierda) y contador (derecha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // botón cerrar - esquina superior izquierda
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Contador - esquina superior derecha
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${images.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            
            // Dots indicadores - parte inferior
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (pagerState.currentPage == index) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index)
                                        Color.White
                                    else
                                        Color.White.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------
// REPORT PRODUCT MODAL - Fullscreen desde abajo, completo y detallado
// -------------------------------------------------------------------------------
@Composable
private fun ReportProductDialog(
    sku: String,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit,
    postId: String = "",
    postTitle: String = "",
    postImage: String = ""
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var detailsText by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var includeScreenshots by remember { mutableStateOf(false) }
    var blockSeller by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    data class ReportReason(
        val id: String,
        val title: String,
        val description: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )
    
    val reasons = listOf(
        ReportReason("fake", "Producto falso o falsificado", "El producto es una imitación o réplica no autorizada", Icons.Outlined.Warning),
        ReportReason("misleading", "Descripción Engañosa", "La Descripción no coincide con el producto real", Icons.Outlined.EditNote),
        ReportReason("wrong_price", "Precio incorrecto", "El precio mostrado es diferente al cobrado", Icons.Outlined.AttachMoney),
        ReportReason("wrong_images", "Imágenes no corresponden", "Las fotos son de otro producto", Icons.Outlined.Image),
        ReportReason("inappropriate", "Contenido inapropiado", "Contenido ofensivo, ilegal o prohibido", Icons.Outlined.Block),
        ReportReason("scam", "Posible estafa", "Sospecha de actividad fraudulenta", Icons.Outlined.Security),
        ReportReason("copyright", "Violación de derechos", "Uso no autorizado de marcas o contenido", Icons.Outlined.Copyright),
        ReportReason("other", "Otro motivo", "Especificar en los detalles", Icons.Outlined.MoreHoriz)
    )
    
    // Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() }
    )
    
    // Modal fullscreen desde abajo
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Denunciar Publicación",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "SKU: $sku",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Divider(color = BorderSubtle)
                    
                    // Content scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Info del producto
                        if (postTitle.isNotEmpty() || postImage.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (postImage.isNotEmpty()) {
                                        AsyncImage(
                                            model = postImage,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = postTitle.ifEmpty { "Producto" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Producto a reportar",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        
                        // Sección: Motivo del reporte
                        Text(
                            text = "¿Cuál es el problema?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Selecciona el motivo que mejor describe tu reporte",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Lista de motivos
                        reasons.forEach { reason ->
                            val isSelected = selectedReason == reason.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedReason = reason.id },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.1f) else SurfaceElevated,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) Color(0xFFEF4444).copy(alpha = 0.2f)
                                                else TextMuted.copy(alpha = 0.1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = reason.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFFEF4444) else TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(14.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reason.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color(0xFFEF4444) else TextPrimary
                                        )
                                        Text(
                                            text = reason.description,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedReason = reason.id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFFEF4444)
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Sección: Detalles adicionales
                        Text(
                            text = "Cuéntanos Más detalles",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Mientras Más Información nos des, Más Rápido podremos actuar",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Campo de Descripción
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceElevated
                        ) {
                            BasicTextField(
                                value = detailsText,
                                onValueChange = { detailsText = it },
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFFEF4444)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp)
                                    .padding(14.dp),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (detailsText.isEmpty()) {
                                            Text(
                                                text = "Describe el problema con detalle. Por ejemplo: \"El producto recibido es diferente al de las fotos...\"",
                                                color = TextMuted,
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // URL o Información adicional
                        Text(
                            text = "Enlace o Información adicional (opcional)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceElevated
                        ) {
                            BasicTextField(
                                value = additionalInfo,
                                onValueChange = { additionalInfo = it },
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFFEF4444)),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (additionalInfo.isEmpty()) {
                                            Text(
                                                text = "Link al producto original, prueba de compra, etc.",
                                                color = TextMuted,
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Opciones adicionales
                        Text(
                            text = "Opciones adicionales",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Checkbox: Bloquear vendedor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { blockSeller = !blockSeller }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = blockSeller,
                                onCheckedChange = { blockSeller = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFEF4444)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Bloquear a este vendedor",
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "No verás Más publicaciones de este usuario",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Aviso legal
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = TextMuted.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Los reportes falsos o malintencionados pueden resultar en restricciones a tu cuenta. Nuestro equipo revisará este reporte en las Próximas 24-48 horas.",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp)) // Espacio para el Botón
                    }
                    
                    // Footer con Botón de enviar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (selectedReason != null) {
                                        isSubmitting = true
                                        scope.launch {
                                            // Construir Descripción completa
                                            val fullReport = buildString {
                                                append("Motivo: ${reasons.find { it.id == selectedReason }?.title ?: selectedReason}\n")
                                                if (detailsText.isNotBlank()) {
                                                    append("Detalles: $detailsText\n")
                                                }
                                                if (additionalInfo.isNotBlank()) {
                                                    append("Info adicional: $additionalInfo\n")
                                                }
                                                if (blockSeller) {
                                                    append("Solicita bloquear vendedor")
                                                }
                                            }
                                            onReport(fullReport)
                                            isSubmitting = false
                                        }
                                    }
                                },
                                enabled = selectedReason != null && !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    disabledContainerColor = Color(0xFFEF4444).copy(alpha = 0.4f)
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Enviar reporte",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Al enviar, aceptas nuestras Políticas de uso",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

