package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.repository.CartRepository
import com.rendly.app.ui.theme.*

@Composable
fun CartModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCheckout: () -> Unit = {},
    onContinueShopping: () -> Unit = {},
    onOpenCategories: () -> Unit = {}, // Abrir drawer de categorías
    onOpenExplore: () -> Unit = {}, // Nuevo: abrir modal de explorar productos directamente
    onVisibilityChange: (Boolean) -> Unit = {},
    onNavigateToCheckout: () -> Unit = {} // Navegar a pantalla de checkout con Mercado Pago
) {
    // Notificar cambio de visibilidad para ocultar NavBar
    LaunchedEffect(isVisible) {
        onVisibilityChange(isVisible)
    }
    val cartItems by CartRepository.cartItems.collectAsState()
    val itemCount = cartItems.sumOf { it.quantity }
    val subtotal = cartItems.sumOf { it.totalPrice }
    val totalSavings = cartItems.sumOf { it.savings }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
                .systemBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                CartHeader(
                    itemCount = itemCount,
                    onClose = onDismiss,
                    onClear = { CartRepository.clearCart() }
                )
                
                if (cartItems.isEmpty()) {
                    // Estado vacío
                    EmptyCartState(
                        onContinueShopping = {
                            onDismiss()
                            onOpenExplore() // Abrir SearchResultsScreen directamente
                        }
                    )
                } else {
                    // Lista de items
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = cartItems,
                            key = { "${it.post.id}_${it.selectedColor}_${it.selectedSize}" }
                        ) { item ->
                            CartItemCard(
                                item = item,
                                onQuantityChange = { newQty ->
                                    CartRepository.updateQuantity(
                                        item.post.id,
                                        newQty,
                                        item.selectedColor,
                                        item.selectedSize
                                    )
                                },
                                onRemove = {
                                    CartRepository.removeFromCart(
                                        item.post.id,
                                        item.selectedColor,
                                        item.selectedSize
                                    )
                                }
                            )
                        }
                        
                        // Espacio para el footer
                        item { Spacer(modifier = Modifier.height(140.dp)) }
                    }
                    
                    // Footer con totales y botón de checkout
                    CartFooter(
                        subtotal = subtotal,
                        savings = totalSavings,
                        itemCount = itemCount,
                        onCheckout = {
                            onDismiss()
                            onNavigateToCheckout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartHeader(
    itemCount: Int,
    onClose: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Cerrar",
                    tint = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mi Carrito",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (itemCount > 0) {
                    Text(
                        text = "$itemCount ${if (itemCount == 1) "artículo" else "artículos"}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
            
            if (itemCount > 0) {
                TextButton(onClick = onClear) {
                    Text(
                        text = "Vaciar",
                        fontSize = 13.sp,
                        color = AccentPink
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCartState(onContinueShopping: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carrito vacío - más arriba
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(50.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Tu carrito está vacío",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = "Explora productos y agrega tus favoritos al carrito",
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = onContinueShopping,
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonAddCart
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Explorar productos",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Sección de conversión atractiva
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡Ofertas del momento!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Descubre productos con hasta 50% OFF y envío gratis en tu primera compra",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OfferBadge(
                        icon = Icons.Outlined.LocalShipping,
                        text = "Envío gratis",
                        color = SavingsColor
                    )
                    OfferBadge(
                        icon = Icons.Outlined.Percent,
                        text = "Hasta 50% OFF",
                        color = AccentPink
                    )
                    OfferBadge(
                        icon = Icons.Outlined.Security,
                        text = "Pago seguro",
                        color = PrimaryPurple
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tendencias
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Brush.linearGradient(
                colors = listOf(PrimaryPurple.copy(alpha = 0.15f), AccentPink.copy(alpha = 0.1f))
            ).let { Surface },
            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onContinueShopping() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryPurple, AccentPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lo más vendido",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Mira qué están comprando otros",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
    }
}

@Composable
private fun FeaturedProductsGrid(
    onProductClick: () -> Unit
) {
    var featuredPosts by remember { mutableStateOf<List<com.rendly.app.data.model.Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            featuredPosts = com.rendly.app.data.repository.PostRepository.getPostsByCategory(
                category = null,
                excludePostId = "",
                limit = 6
            )
        } catch (e: Exception) {
            android.util.Log.e("CartModal", "Error loading featured: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Productos destacados",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = "Ver todo",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryPurple,
                modifier = Modifier.clickable { onProductClick() }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                    )
                }
            }
        } else {
            val chunkedPosts = featuredPosts.chunked(2)
            chunkedPosts.forEach { rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowPosts.forEach { post ->
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumProductCard(
                                post = post,
                                onClick = onProductClick
                            )
                        }
                    }
                    if (rowPosts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TARJETA DE PRODUCTO PREMIUM - Estilo Amazon/MercadoLibre
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumProductCard(
    post: com.rendly.app.data.model.Post,
    onClick: () -> Unit
) {
    val imageUrl = post.images.firstOrNull() ?: post.producto.imagenUrl.firstOrNull() ?: ""
    val title = post.title.ifEmpty { post.producto.titulo }
    val price = if (post.price > 0) post.price else post.producto.precio
    val previousPrice = post.previousPrice ?: (price * 1.20)
    val discount = ((previousPrice - price) / previousPrice * 100).toInt()
    val hasDiscount = discount > 5
    val rating = if (post.reviewsCount > 0) 4.2f + (post.reviewsCount.coerceAtMost(30) / 60f) else 0f
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = SurfaceElevated,
        shadowElevation = 3.dp
    ) {
        Column {
            // Imagen con badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                )
                
                // Badge de descuento estilo MercadoLibre
                if (hasDiscount) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFF4757)
                    ) {
                        Text(
                            text = "$discount% OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Badge envío gratis
                if (post.freeShipping) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = SavingsColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "Envío gratis",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Info del producto
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                // Título
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Precio anterior tachado
                if (hasDiscount) {
                    Text(
                        text = "$${String.format("%,.0f", previousPrice)}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                
                // Precio actual
                Text(
                    text = "$${String.format("%,.0f", price)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = PriceColor
                )
                
                // Cuotas
                Text(
                    text = "en 12x $${String.format("%,.0f", price / 12)}",
                    fontSize = 10.sp,
                    color = SavingsColor,
                    fontWeight = FontWeight.Medium
                )
                
                // Rating
                if (rating > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "(${post.reviewsCount})",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartRepository.CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val post = item.post
    val imageUrl = post.images.firstOrNull() ?: ""
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Imagen del producto
            AsyncImage(
                model = imageUrl,
                contentDescription = post.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info del producto
            Column(modifier = Modifier.weight(1f)) {
                // Título
                Text(
                    text = post.title.ifEmpty { "Producto" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Opciones seleccionadas
                if (item.selectedColor != null || item.selectedSize != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.selectedColor?.let { color ->
                            Text(
                                text = "Color: $color",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        item.selectedSize?.let { size ->
                            Text(
                                text = "Talla: $size",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Vendedor
                Text(
                    text = "Vendido por ${post.username}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Precios
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${String.format("%,.2f", item.totalPrice)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PriceColor
                    )
                    
                    if (item.savings > 0) {
                        Text(
                            text = "$${String.format("%,.2f", item.originalTotalPrice)}",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Controles de cantidad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Selector de cantidad
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Surface,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onQuantityChange(item.quantity - 1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.quantity == 1) Icons.Outlined.Delete else Icons.Default.Remove,
                                    contentDescription = "Reducir",
                                    tint = if (item.quantity == 1) AccentPink else TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            Text(
                                text = "${item.quantity}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            IconButton(
                                onClick = { onQuantityChange(item.quantity + 1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Aumentar",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    
                    // Botón eliminar
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Eliminar",
                            tint = AccentPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartFooter(
    subtotal: Double,
    savings: Double,
    itemCount: Int,
    onCheckout: () -> Unit
) {
    val shippingThreshold = 50000.0
    val freeShipping = subtotal >= shippingThreshold
    val remainingForFreeShipping = (shippingThreshold - subtotal).coerceAtLeast(0.0)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Barra de progreso para envío gratis
            if (!freeShipping && subtotal > 0) {
                val progress = (subtotal / shippingThreshold).toFloat().coerceIn(0f, 1f)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = SavingsColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡Agrega $${String.format("%,.0f", remainingForFreeShipping)} más para envío GRATIS!",
                        fontSize = 12.sp,
                        color = SavingsColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SavingsColor,
                    trackColor = SurfaceElevated
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Ahorro total
            if (savings > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Estás ahorrando",
                        fontSize = 13.sp,
                        color = SavingsColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$${String.format("%,.2f", savings)}",
                        fontSize = 13.sp,
                        color = SavingsColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subtotal",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "$${String.format("%,.2f", subtotal)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
                
                // Botón de checkout
                Button(
                    onClick = onCheckout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonBuyNow
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .widthIn(min = 160.dp)
                ) {
                    Text(
                        text = "Comprar ($itemCount)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Envío gratis badge
            if (freeShipping) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = SavingsColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "¡Envío GRATIS incluido!",
                        fontSize = 13.sp,
                        color = SavingsColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
