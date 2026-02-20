package com.rendly.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.data.repository.ExploreItem
import com.rendly.app.ui.theme.*
import com.rendly.app.ui.components.VerifiedBadge

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * UNIFIED PRODUCT CARD
 * ═══════════════════════════════════════════════════════════════════════════════
 * Componente único de tarjeta de producto usado en toda la app.
 * Diseño profesional inspirado en marketplaces pero con estilo original Merqora.
 * 
 * Estructura:
 * - Imagen con swipe (si hay múltiples)
 * - Título (máximo 2 líneas)
 * - Username del vendedor
 * - Rating resumido + ventas (si hay)
 * - Precio tachado (anterior)
 * - Precio actual + badge descuento
 * - Cuotas sin interés
 * - Envío gratis (si aplica)
 */

data class ProductCardData(
    val id: String,
    val title: String,
    val username: String,
    val avatarUrl: String? = null,
    val images: List<String>,
    val currentPrice: Double,
    val originalPrice: Double,
    val rating: Double,
    val reviewsCount: Int,
    val salesCount: Int,
    val hasFreeShipping: Boolean,
    val isSaved: Boolean = false,
    val isVerified: Boolean = false
)

// Extensión para convertir Post a ProductCardData
fun Post.toProductCardData(): ProductCardData {
    val precio = this.price.takeIf { it > 0 } ?: this.producto.precio
    val precioOriginal = this.previousPrice?.takeIf { it > 0 } ?: (precio * 1.25)
    val imgs = this.images.ifEmpty { this.producto.imagenUrl }.ifEmpty { listOf("") }
    
    // Simular ventas basadas en likes y reviews
    val ventas = when {
        this.likesCount > 50 -> (this.likesCount * 0.3).toInt()
        this.likesCount > 20 -> (this.likesCount * 0.5).toInt()
        this.likesCount > 5 -> this.likesCount / 2
        else -> 0
    }
    
    // Rating basado en reviews reales o simulado
    val ratingValue = when {
        this.reviewsCount > 0 -> (3.5 + (this.reviewsCount % 15) * 0.1).coerceIn(3.5, 5.0)
        else -> 0.0
    }
    
    return ProductCardData(
        id = this.id,
        title = this.title.ifEmpty { this.producto.titulo }.ifEmpty { "Sin título" },
        username = this.username.ifEmpty { "vendedor" },
        avatarUrl = this.userAvatar.ifEmpty { null },
        images = imgs,
        currentPrice = precio,
        originalPrice = precioOriginal,
        rating = ratingValue,
        reviewsCount = this.reviewsCount,
        salesCount = ventas,
        hasFreeShipping = precio > 15000,
        isSaved = this.isSaved,
        isVerified = this.isUserVerified
    )
}

// Extensión para convertir ExploreItem a ProductCardData
fun ExploreItem.toProductCardData(): ProductCardData {
    val imgs = this.images.ifEmpty { listOf("") }
    
    // Simular ventas basadas en likes
    val ventas = when {
        this.likesCount > 50 -> (this.likesCount * 0.3).toInt()
        this.likesCount > 20 -> (this.likesCount * 0.5).toInt()
        this.likesCount > 5 -> this.likesCount / 2
        else -> 0
    }
    
    // Rating basado en reviews reales o simulado
    val ratingValue = when {
        this.reviewsCount > 0 -> (3.5 + (this.reviewsCount % 15) * 0.1).coerceIn(3.5, 5.0)
        else -> 0.0
    }
    
    // Calcular precio original (simulado si no hay descuento)
    val precioOriginal = this.price * 1.25
    
    return ProductCardData(
        id = this.id,
        title = this.title.ifEmpty { "Sin título" },
        username = this.username.ifEmpty { "vendedor" },
        avatarUrl = this.userAvatar.ifEmpty { null },
        images = imgs,
        currentPrice = this.price,
        originalPrice = precioOriginal,
        rating = ratingValue,
        reviewsCount = this.reviewsCount,
        salesCount = ventas,
        hasFreeShipping = this.price > 15000,
        isSaved = false,
        isVerified = this.isVerified
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifiedProductCard(
    data: ProductCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 160.dp
) {
    var isSaved by remember { mutableStateOf(data.isSaved) }
    
    // Calcular descuento
    val hasDiscount = data.originalPrice > data.currentPrice
    val discountPercent = if (hasDiscount) {
        ((data.originalPrice - data.currentPrice) / data.originalPrice * 100).toInt()
    } else 0
    val savings = data.originalPrice - data.currentPrice
    
    // Cuotas sin interés (simulado: 12 cuotas para productos > $5000)
    val installments = if (data.currentPrice > 5000) 12 else if (data.currentPrice > 2000) 6 else 3
    val installmentPrice = data.currentPrice / installments
    
    val pagerState = rememberPagerState(pageCount = { data.images.size })
    
    Surface(
        modifier = modifier.fillMaxWidth().fillMaxHeight().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Surface,
        shadowElevation = 3.dp,
        tonalElevation = 1.dp
    ) {
        Column {
            // ═══════════════════════════════════════════════════════════════
            // IMAGEN CON SWIPE
            // ═══════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) { page ->
                    AsyncImage(
                        model = data.images.getOrNull(page) ?: "",
                        contentDescription = data.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Gradiente inferior sutil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.25f)
                                )
                            )
                        )
                )
                
                // Indicador de páginas (dots)
                if (data.images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(data.images.size.coerceAtMost(5)) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == index) 6.dp else 4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) 
                                            Color.White 
                                        else 
                                            Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
                
                // Botón guardar - esquina superior derecha (dark semi-transparent)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { isSaved = !isSaved },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Guardar",
                        tint = if (isSaved) Color(0xFF3A8FD4) else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // INFORMACIÓN DEL PRODUCTO
            // ═══════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Título (máximo 2 líneas, altura fija para uniformidad)
                Text(
                    text = data.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    lineHeight = 17.sp,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Username del vendedor con avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (data.avatarUrl != null) {
                        AsyncImage(
                            model = data.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0A3D62), Color(0xFF2E8B57))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = data.username.take(1).uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "@${data.username}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (data.isVerified) {
                        VerifiedBadge(size = 12.dp)
                    }
                }
                
                // Rating + Ventas
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(16.dp)
                ) {
                    if (data.rating > 0) {
                        // Rating compacto: ⭐ 4.5
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFD4A853), // Dorado sobrio
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", data.rating),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        
                        // Ventas (si hay)
                        if (data.salesCount > 0) {
                            Text(
                                text = " · ",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "+${data.salesCount} vendidos",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        } else if (data.reviewsCount > 0) {
                            Text(
                                text = " (${data.reviewsCount})",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    } else {
                        // Sin rating: mostrar 5 estrellas vacías
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Outlined.StarBorder,
                                contentDescription = null,
                                tint = Color(0xFFD4A853).copy(alpha = 0.5f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sin opiniones",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
                
                // Precio anterior (tachado)
                if (hasDiscount) {
                    Text(
                        text = "$${String.format("%,.0f", data.originalPrice)}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                
                // Precio actual + badge descuento en la misma fila
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$${String.format("%,.0f", data.currentPrice)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (hasDiscount && discountPercent >= 5) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF6B35) // Naranja paleta
                        ) {
                            Text(
                                text = "-$discountPercent%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                
                // Ahorras (debajo del precio)
                if (hasDiscount && discountPercent >= 5) {
                    Text(
                        text = "Ahorras $${String.format("%,.0f", savings)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E8B57) // Verde paleta
                    )
                }
                
                // Cuotas sin interés
                if (data.currentPrice > 2000) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = Color(0xFF0A3D62),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${installments}x $${String.format("%,.0f", installmentPrice)} sin interés",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0A3D62)
                        )
                    }
                }
                
                // Envío gratis
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.height(16.dp)
                ) {
                    if (data.hasFreeShipping) {
                        Icon(
                            imageVector = Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = Color(0xFF00A650), // Verde MercadoLibre style
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Envío gratis",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00A650)
                        )
                    }
                }
            }
        }
    }
}

// Versión compacta para grids más densos
@Composable
fun UnifiedProductCardCompact(
    data: ProductCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedProductCard(
        data = data,
        onClick = onClick,
        modifier = modifier,
        imageHeight = 140.dp
    )
}
