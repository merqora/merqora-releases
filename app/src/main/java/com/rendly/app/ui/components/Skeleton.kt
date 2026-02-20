package com.rendly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rendly.app.ui.theme.Surface
import com.rendly.app.ui.theme.SurfaceElevated

/**
 * Professional shimmer effect brush for skeleton loading
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    showShimmer: Boolean = true
): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color(0xFF2A2A2A),
            Color(0xFF3D3D3D),
            Color(0xFF2A2A2A)
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation.value - 200f, translateAnimation.value - 200f),
            end = Offset(translateAnimation.value, translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

/**
 * Generic skeleton placeholder box
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

/**
 * Circular skeleton (for avatars)
 */
@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush())
    )
}

/**
 * Text line skeleton
 */
@Composable
fun SkeletonText(
    width: Dp,
    height: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    SkeletonBox(
        modifier = modifier
            .width(width)
            .height(height),
        shape = RoundedCornerShape(4.dp)
    )
}

/**
 * Skeleton for a product card in explore/profile grid
 */
@Composable
fun ProductCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
    ) {
        // Image placeholder
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        )
        
        // Content
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonText(width = 100.dp)
            SkeletonText(width = 60.dp, height = 16.dp)
        }
    }
}

/**
 * Skeleton for a horizontal carousel item
 */
@Composable
fun CarouselItemSkeleton(
    width: Dp = 140.dp,
    height: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(width),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = RoundedCornerShape(12.dp)
        )
        SkeletonText(width = 80.dp)
        SkeletonText(width = 50.dp, height = 12.dp)
    }
}

/**
 * Skeleton for profile header - Alineado a la izquierda como el perfil real
 */
@Composable
fun ProfileHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = (-28).dp)
            .padding(horizontal = 16.dp)
    ) {
        // Avatar + Stats en la misma fila (como el perfil real)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            
            // Avatar
            SkeletonCircle(size = 80.dp)
            
            Spacer(modifier = Modifier.width(27.dp))
            
            // Stats - Publicaciones, Seguidores, Clientes
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.Start) {
                        SkeletonText(width = 35.dp, height = 17.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        SkeletonText(width = 55.dp, height = 11.dp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Nombre + Badge de reputación
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonText(width = 140.dp, height = 20.dp)
            Spacer(modifier = Modifier.width(8.dp))
            SkeletonBox(
                modifier = Modifier
                    .width(45.dp)
                    .height(20.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Bio
        SkeletonText(width = 220.dp, height = 14.dp)
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonText(width = 180.dp, height = 14.dp)
    }
}

/**
 * Skeleton for explore screen grid
 */
@Composable
fun ExploreGridSkeleton(
    itemCount: Int = 6,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = (itemCount + 1) / 2
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(2) { colIndex ->
                    val index = rowIndex * 2 + colIndex
                    if (index < itemCount) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCardSkeleton()
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Skeleton for horizontal carousel section
 */
@Composable
fun CarouselSkeleton(
    itemCount: Int = 4,
    itemWidth: Dp = 140.dp,
    itemHeight: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount.coerceAtMost(3)) {
            CarouselItemSkeleton(width = itemWidth, height = itemHeight)
        }
    }
}

/**
 * Skeleton for section header
 */
@Composable
fun SectionHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCircle(size = 24.dp)
            SkeletonText(width = 140.dp, height = 18.dp)
        }
        SkeletonText(width = 60.dp, height = 14.dp)
    }
}

/**
 * Full explore screen skeleton
 */
@Composable
fun ExploreScreenSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search bar skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        )
        
        // Quick actions skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(5) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SkeletonCircle(size = 48.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonText(width = 48.dp, height = 10.dp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Categories skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                SkeletonBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp),
                    shape = RoundedCornerShape(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section 1
        SectionHeaderSkeleton()
        CarouselSkeleton()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section 2
        SectionHeaderSkeleton()
        ExploreGridSkeleton(itemCount = 4)
    }
}

/**
 * Full profile screen skeleton - Alineado exactamente como el perfil real
 */
@Composable
fun ProfileScreenSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header compacto (username + botón publicar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SkeletonText(width = 100.dp, height = 18.dp)
            SkeletonBox(
                modifier = Modifier.size(36.dp),
                shape = CircleShape
            )
        }
        
        // Banner con borderRadius
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp)
                .height(130.dp),
            shape = RoundedCornerShape(20.dp)
        )
        
        // Profile header (avatar + stats alineados a la izquierda)
        ProfileHeaderSkeleton()
        
        // Action buttons - alineados con offset negativo como el perfil real
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-12).dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp)
            )
            SkeletonBox(
                modifier = Modifier
                    .width(44.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Highlights skeleton - alineados a la izquierda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón agregar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SkeletonBox(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonText(width = 40.dp, height = 10.dp)
            }
            // Highlights
            repeat(3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SkeletonCircle(size = 64.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonText(width = 48.dp, height = 10.dp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tabs skeleton - distribuidos uniformemente
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SkeletonBox(
                        modifier = Modifier.size(22.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonText(width = 45.dp, height = 11.dp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Grid skeleton - 3 columnas como el perfil real
        ProfileGridSkeleton(itemCount = 9)
    }
}

/**
 * Skeleton para grid de perfil (3 columnas)
 */
@Composable
fun ProfileGridSkeleton(
    itemCount: Int = 9,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val rows = (itemCount + 2) / 3
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(3) { colIndex ->
                    val index = rowIndex * 3 + colIndex
                    if (index < itemCount) {
                        SkeletonBox(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
