package com.mercora.app.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mercora.app.ui.theme.*

@Composable
fun ProductActionsRow(
    images: List<String>,
    isLiked: Boolean,
    likesCount: Int,
    isSaved: Boolean,
    savesCount: Int = 0,
    sharesCount: Int = 0,
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Row(
                modifier = Modifier
                    .clickable(onClick = onSave)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Guardar",
                    tint = if (isSaved) AccentGold else IconColor,
                    modifier = Modifier.size(24.dp)
                )
                if (savesCount > 0) {
                    Text(
                        text = formatCountV2(savesCount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSaved) AccentGold else TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clickable(onClick = onForward)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = "Reenviar",
                    tint = IconColor,
                    modifier = Modifier.size(24.dp)
                )
                if (sharesCount > 0) {
                    Text(
                        text = formatCountV2(sharesCount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }

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
                        val displayUrl = if (isVideoUrl(imageUrl)) videoUrlToThumbnail(imageUrl) else imageUrl
                        AsyncImage(
                            model = displayUrl,
                            contentDescription = "Variante ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isVideoUrl(imageUrl)) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.PlayCircle,
                                    contentDescription = "Video",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

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

@Composable
fun ProductActionButtons(
    onBuyNow: () -> Unit,
    onAddToCart: () -> Unit = {},
    onContact: () -> Unit,
    isOwnProduct: Boolean = false,
    onEditPost: () -> Unit = {},
    onSharePost: () -> Unit = {},
    onViewStats: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isOwnProduct) {
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            // OWNER VIEW â€” Herramientas de vendedor
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditPost()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A3D62)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Editar publicaciÃ³n",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSharePost()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E8B57)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Compartir",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onViewStats()
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = Color(0xFFFF6B35).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = "EstadÃ­sticas",
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            // BUYER VIEW â€” Botones de compra originales
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            Button(
                onClick = { onBuyNow() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBuyNow
                )
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onAddToCart() },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonAddCart
                    )
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

                OutlinedButton(
                    onClick = onContact,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ButtonConsult)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = ButtonConsult
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Consultar",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ButtonConsult
                    )
                }
            }
        }
    }
}

