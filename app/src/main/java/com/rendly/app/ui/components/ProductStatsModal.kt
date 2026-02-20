package com.rendly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

data class ProductStats(
    val precio: String,
    val talle: String,
    val condicion: String,
    val stock: Int,
    val categoria: String,
    val vistas: Int = 0,
    val favoritos: Int = 0
)

@Composable
fun ProductStatsModal(
    visible: Boolean,
    stats: ProductStats,
    modifier: Modifier = Modifier
) {
    val stockColor = when {
        stats.stock > 5 -> AccentGreen
        stats.stock > 0 -> Color(0xFFFF6B35)
        else -> Color(0xFFEF4444)
    }
    
    val stockText = when {
        stats.stock > 5 -> "Disponible"
        stats.stock > 0 -> "Últimas unidades"
        else -> "Agotado"
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(animationSpec = tween(100)) + scaleOut(targetScale = 0.95f)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = Surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Información",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        // Stock badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = stockColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(stockColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stockText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = stockColor
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Stats grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Row 1: Precio y Talle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.Star,
                                label = "Precio",
                                value = stats.precio,
                                color = PrimaryPurple,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.Person,
                                label = "Talle",
                                value = stats.talle,
                                color = AccentPink,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Row 2: Condición y Stock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.CheckCircle,
                                label = "Condición",
                                value = stats.condicion,
                                color = AccentGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.ShoppingCart,
                                label = "Stock",
                                value = "${stats.stock} unid.",
                                color = stockColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Row 3: Categoría
                        StatCard(
                            icon = Icons.Default.Menu,
                            label = "Categoría",
                            value = stats.categoria,
                            color = Color(0xFFFF6B35),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Row 4: Vistas y Favoritos (si hay datos)
                        if (stats.vistas > 0 || stats.favoritos > 0) {
                            Divider(color = TextMuted.copy(alpha = 0.2f))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MiniStat(
                                    icon = Icons.Default.Person,
                                    value = "${stats.vistas}",
                                    label = "vistas"
                                )
                                MiniStat(
                                    icon = Icons.Default.Favorite,
                                    value = "${stats.favoritos}",
                                    label = "favoritos"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun MiniStat(
    icon: ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}
