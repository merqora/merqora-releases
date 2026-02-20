package com.rendly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

enum class BadgeType(
    val icon: ImageVector,
    val label: String,
    val gradient: List<Color>,
    val description: String
) {
    VERIFIED(
        Icons.Default.CheckCircle,
        "Verificado",
        listOf(Color(0xFF1565A0), Color(0xFF1D4ED8)),
        "Identidad verificada"
    ),
    TOP_SELLER(
        Icons.Default.Star,
        "Top Seller",
        listOf(Color(0xFFFF6B35), Color(0xFFD97706)),
        "Vendedor destacado"
    ),
    FAST_SHIPPER(
        Icons.Default.Send,
        "Envío rápido",
        listOf(Color(0xFF2E8B57), Color(0xFF059669)),
        "Envía en 24h"
    ),
    TRUSTED(
        Icons.Default.Lock,
        "Confiable",
        listOf(Color(0xFF1565A0), Color(0xFF0A3D62)),
        "+100 ventas exitosas"
    ),
    NEW_SELLER(
        Icons.Default.Info,
        "Nuevo",
        listOf(Color(0xFF2E8B57), Color(0xFF2E8B57)),
        "Vendedor nuevo"
    ),
    PREMIUM(
        Icons.Default.Star,
        "Premium",
        listOf(Color(0xFFF472B6), Color(0xFF2E8B57)),
        "Cuenta premium"
    )
}

data class ProfileBadge(
    val type: BadgeType,
    val isActive: Boolean = true
)

@Composable
fun ProfileInfoBadges(
    badges: List<ProfileBadge>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        badges.filter { it.isActive }.forEach { badge ->
            BadgeItem(
                badge = badge,
                showLabel = showLabels
            )
        }
    }
}

@Composable
private fun BadgeItem(
    badge: ProfileBadge,
    showLabel: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(badge.type.gradient.map { it.copy(alpha = 0.15f) })
                )
                .padding(horizontal = if (showLabel) 10.dp else 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = badge.type.icon,
                    contentDescription = badge.type.label,
                    tint = badge.type.gradient.first(),
                    modifier = Modifier.size(14.dp)
                )
                
                if (showLabel) {
                    Text(
                        text = badge.type.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badge.type.gradient.first()
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStatsBadges(
    salesCount: Int,
    rating: Float,
    responseTime: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatBadge(
            icon = Icons.Default.ShoppingCart,
            value = "$salesCount",
            label = "Ventas",
            color = PrimaryPurple
        )
        
        StatBadge(
            icon = Icons.Default.Star,
            value = String.format("%.1f", rating),
            label = "Rating",
            color = Color(0xFFFF6B35)
        )
        
        StatBadge(
            icon = Icons.Default.Refresh,
            value = responseTime,
            label = "Respuesta",
            color = AccentGreen
        )
    }
}

@Composable
private fun StatBadge(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
fun SellerTrustIndicator(
    trustScore: Int, // 0-100
    modifier: Modifier = Modifier
) {
    val color = when {
        trustScore >= 90 -> AccentGreen
        trustScore >= 70 -> Color(0xFFFF6B35)
        trustScore >= 50 -> Color(0xFFEF4444)
        else -> TextMuted
    }
    
    val label = when {
        trustScore >= 90 -> "Muy confiable"
        trustScore >= 70 -> "Confiable"
        trustScore >= 50 -> "Regular"
        else -> "Nuevo"
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            
            Text(
                text = "$trustScore%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
