package com.rendly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import androidx.compose.ui.platform.LocalContext
import com.rendly.app.data.model.Post
import com.rendly.app.ui.theme.*
import com.rendly.app.ui.components.VerifiedBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onPostClick: () -> Unit,
    onInfoClick: () -> Unit = {},
    onConsultClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Nueva animación optimizada estilo Instagram
    val animationState = rememberDoubleTapAnimationState()
    
    // Estado para descripción expandida
    var descriptionExpanded by remember { mutableStateOf(false) }
    
    // OPTIMIZADO: Memoizar valores derivados del post
    val description = remember(post.description, post.producto.descripcion) {
        post.description.orEmpty().ifEmpty { post.producto.descripcion }
    }
    
    // Imágenes del producto - memoizado
    val images = remember(post.producto.imagenUrl) {
        post.producto.imagenUrl.ifEmpty { listOf("") }
    }
    
    // Tiempo formateado
    val timeAgo = remember(post.createdAt) { formatTimeAgo(post.createdAt) }
    
    // Avatar URL
    val avatarUrl = remember(post.userAvatar) {
        if (post.userAvatar.startsWith("http")) post.userAvatar
        else "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${post.userAvatar}"
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HomeBg)
            .padding(top = 8.dp) // Espaciado entre posts
    ) {
        // ═══════════════════════════════════════════════════════════════
        // HEADER SOBRE LA IMAGEN (Avatar + Username | Opciones)
        // ═══════════════════════════════════════════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Izquierda: Avatar + Username
            Row(
                modifier = Modifier.clickable(onClick = onProfileClick),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar optimizado con caché
                AsyncImage(
                    model = remember(avatarUrl) {
                        ImageRequest.Builder(context)
                            .data(avatarUrl)
                            .crossfade(100)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .size(96)
                            .build()
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = post.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (post.isUserVerified) {
                        VerifiedBadge(size = 14.dp)
                    }
                }
            }
            
            // Derecha: Icono de opciones
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Opciones",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // IMAGE CAROUSEL con Double-Tap Like
        // ═══════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(HomeBg)
        ) {
            if (images.isNotEmpty() && images[0].isNotEmpty()) {
                // Carrusel premium con gestos optimizados
                PostImageCarousel(
                    images = images,
                    contentDescription = post.producto.titulo,
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPostClick()
                    },
                    onDoubleTap = { offset ->
                        // Dar like SOLO si no está likeado (nunca quitar)
                        if (!post.isLiked) {
                            onLikeClick()
                        }
                        
                        // Haptic feedback
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        
                        // Disparar animación optimizada
                        scope.launch {
                            animationState.trigger(Offset(offset.x, offset.y))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay de animación optimizado estilo Instagram
                DoubleTapAnimationOverlay(
                    state = animationState,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Product Info Panel (posicionado arriba de los dots) - passthrough para swipe
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 32.dp) // Espacio para los dots
                ) {
                    ProductInfoPanel(
                        producto = post.producto,
                        visible = post.showStats
                    )
                }
            } else {
                // Placeholder cuando no hay imagen
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin imagen disponible",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // SECCIÓN ECOMMERCE + RED SOCIAL REDISEÑADA - MÁS COMPACTA
        // ═══════════════════════════════════════════════════════════════
        
        // OPTIMIZADO: Memoizar cálculos derivados del post
        val hasTitle = remember(post.producto.titulo) { post.producto.titulo.isNotBlank() }
        val hasPrice = remember(post.producto.precio) { post.producto.precio > 0.0 }
        val hasReviews = remember(post.comments) { post.comments > 0 }
        val averageRating = remember(post.comments) {
            if (post.comments > 0) {
                val base = 3.5 + (post.comments.coerceAtMost(10) * 0.15)
                (base * 10).toInt() / 10.0
            } else 0.0
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp) // Más compacto
        ) {
            // Row 1: Acciones sociales (Like + Save + Reenviar) + Consultar a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Izquierda: Like + Save + Reenviar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        count = post.likes,
                        isActive = post.isLiked,
                        activeColor = AccentGreen,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLikeClick()
                        }
                    )
                    
                    ActionButton(
                        icon = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        count = post.savesCount,
                        isActive = post.isSaved,
                        activeColor = AccentGold,  // Naranja
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSaveClick()
                        }
                    )
                    
                    // Botón Reenviar con contador
                    ActionButton(
                        icon = Icons.Outlined.Send,
                        count = if (post.sharesCount > 0) post.sharesCount else null,
                        isActive = false,
                        activeColor = IconAccentBlue,
                        inactiveColor = IconAccentBlue,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onShareClick()
                        }
                    )
                }
                
                // Derecha: Botón Consultar - Diseño minimalista con acento naranja
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConsultClick()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF333333),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                AccentGold.copy(alpha = 0.4f),
                                Color(0xFF444444),
                                AccentGold.copy(alpha = 0.2f)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Consultar",
                            modifier = Modifier.size(13.dp),
                            tint = AccentGold
                        )
                        Text(
                            text = "Consultar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp)) // Más compacto
            
            // Row 2: Solo título del producto (username ya está en header sobre imagen)
            if (hasTitle) {
                Text(
                    text = post.producto.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Row 3: NUEVO DISEÑO DE PRECIOS - Profesional para maximizar ventas
            if (hasPrice) {
                // OPTIMIZADO: Memoizar cálculos y strings formateados
                val precioActual = post.producto.precio
                val priceStrings = remember(precioActual) {
                    val precioAnterior = precioActual * 1.25
                    val ahorro = precioAnterior - precioActual
                    val porcentajeAhorro = ((ahorro / precioAnterior) * 100).toInt()
                    PriceStrings(
                        precioAnteriorText = "$${String.format("%.2f", precioAnterior)}",
                        precioActualText = "$${String.format("%.2f", precioActual)}",
                        ahorroText = "Ahorras $${String.format("%.2f", ahorro)}",
                        descuentoText = "-$porcentajeAhorro%"
                    )
                }
                
                // Línea 1: Precio tachado arriba (más pequeño)
                Text(
                    text = priceStrings.precioAnteriorText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Línea 2: Precio actual + Badge descuento + Ahorro alineado derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Izquierda: Precio actual + Badge descuento
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Precio actual - Grande y llamativo (mismo verde que "Ahorras")
                        Text(
                            text = priceStrings.precioActualText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGreen
                        )
                        
                        // Badge de descuento - Naranja acento
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF6B35) // Naranja acento
                        ) {
                            Text(
                                text = priceStrings.descuentoText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    // Derecha: Texto de ahorro
                    Text(
                        text = priceStrings.ahorroText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Row 4: Rating con estrellas (SOLO si hay opiniones) o texto "Ver opiniones" debajo
            if (hasReviews) {
                // Mostrar rating con estrellas + Promedio | Ver opiniones alineado derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Izquierda: Estrellas + Promedio
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Estrellas
                        repeat(5) { index ->
                            Icon(
                                imageVector = when {
                                    index < averageRating.toInt() -> Icons.Filled.Star
                                    index < averageRating -> Icons.Filled.Star // Parcial como llena
                                    else -> Icons.Outlined.StarOutline
                                },
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        val ratingText = remember(averageRating) { String.format("%.1f", averageRating) }
                        Text(
                            text = ratingText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "(${post.comments})",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    
                    // Derecha: Ver opiniones clickeable
                    Text(
                        text = "Ver opiniones",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = IconAccentBlue,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCommentClick()
                        }
                    )
                }
            } else {
                // Sin opiniones: Mostrar texto "Sé el primero en opinar" debajo del precio
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCommentClick()
                        }
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextMuted
                    )
                    Text(
                        text = "Sé el primero en opinar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextMuted
                    )
                }
            }
        }
        
        // Espacio extra antes del separador
        Spacer(modifier = Modifier.height(12.dp))
        
        // Separador sutil
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(BorderSubtle)
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int?,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    inactiveColor: Color = IconColor
) {
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = if (isActive) activeColor else inactiveColor
        )
        
        if (count != null) {
            Text(
                text = formatCount(count),
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isActive) activeColor else TextSecondary,
                letterSpacing = 0.2.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PREMIUM SHARE BUTTON - Botón Reenviar profesional estilo Instagram
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PremiumShareButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "share_scale"
    )
    
    // OPTIMIZADO: Memoizar gradiente estático
    val shareButtonGradient = remember {
        Brush.linearGradient(colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
    }
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(shareButtonGradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Reenviar",
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = -25f },
                tint = Color.White
            )
            Text(
                text = "Reenviar",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.3.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PRODUCT INFO PANEL - Con animación staggered como Vinzary
// ═══════════════════════════════════════════════════════════════
private data class InfoDataItem(
    val id: String,
    val icon: String?,
    val color: Color,
    val label: String,
    val value: String,
    val vectorIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

@Composable
private fun ProductInfoPanel(
    producto: com.rendly.app.data.model.Producto,
    visible: Boolean = true
) {
    val stockText = when {
        producto.stock == 0 -> "Agotado"
        producto.stock == 1 -> "¡Último!"
        producto.stock <= 3 -> "Solo ${producto.stock}"
        else -> "${producto.stock} unidades"
    }
    
    val stockColor = when {
        producto.stock == 0 -> AccentPink
        producto.stock <= 3 -> AccentYellow
        else -> AccentGreen
    }
    
    val dataItems = remember(producto) {
        listOf(
            InfoDataItem("price", null, PrimaryPurple, "Precio", "$${producto.precio}", Icons.Outlined.LocalOffer),
            InfoDataItem("condition", null, AccentGreen, "Estado", producto.condicion.ifEmpty { "Nuevo" }, Icons.Outlined.StarOutline),
            InfoDataItem("size", null, AccentPink, "Talle", producto.talle.ifEmpty { "Único" }, Icons.Outlined.Checkroom),
            InfoDataItem("stock", null, stockColor, "Disponible", stockText, Icons.Outlined.Inventory)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedDataItem(
                    dataItem = dataItems[0],
                    index = 0,
                    visible = visible,
                    modifier = Modifier.weight(1f)
                )
                AnimatedDataItem(
                    dataItem = dataItems[1],
                    index = 1,
                    visible = visible,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedDataItem(
                    dataItem = dataItems[2],
                    index = 2,
                    visible = visible,
                    modifier = Modifier.weight(1f)
                )
                AnimatedDataItem(
                    dataItem = dataItems[3],
                    index = 3,
                    visible = visible,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ANIMATED DATA ITEM - OPTIMIZADO sin animaciones pesadas
// ═══════════════════════════════════════════════════════════════
@Composable
private fun AnimatedDataItem(
    dataItem: InfoDataItem,
    index: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    // OPTIMIZADO: Solo usar alpha simple, sin animaciones de escala/offset
    val alpha = if (visible) 1f else 0f
    
    Surface(
        modifier = modifier.graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xF20F0F17),
        border = BorderStroke(1.dp, Color(0x26A78BFA))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(dataItem.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (dataItem.vectorIcon != null) {
                    Icon(
                        imageVector = dataItem.vectorIcon,
                        contentDescription = null,
                        tint = dataItem.color,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(text = dataItem.icon ?: "", fontSize = 16.sp)
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dataItem.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dataItem.value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = dataItem.color,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

// OPTIMIZADO: Data class para strings de precio memoizados
private data class PriceStrings(
    val precioAnteriorText: String,
    val precioActualText: String,
    val ahorroText: String,
    val descuentoText: String
)

private fun formatTimeAgo(dateString: String): String {
    if (dateString.isEmpty()) return "Ahora"
    
    return try {
        val instant = java.time.Instant.parse(dateString)
        val now = java.time.Instant.now()
        val duration = java.time.Duration.between(instant, now)
        
        when {
            duration.toMinutes() < 1 -> "Ahora"
            duration.toMinutes() < 60 -> "Hace ${duration.toMinutes()}m"
            duration.toHours() < 24 -> "Hace ${duration.toHours()}h"
            duration.toDays() < 7 -> "Hace ${duration.toDays()}d"
            duration.toDays() < 30 -> "Hace ${duration.toDays() / 7}sem"
            else -> "Hace ${duration.toDays() / 30}mes"
        }
    } catch (e: Exception) {
        "Ahora"
    }
}
