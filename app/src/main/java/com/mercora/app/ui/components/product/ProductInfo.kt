package com.mercora.app.ui.components.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import com.mercora.app.ui.theme.*

@Composable
fun ProductTopInfo(
    isNew: Boolean,
    reviewsCount: Int = 0,
    avgRating: Float = 0f
) {
    val rating = if (reviewsCount > 0 && avgRating > 0f) avgRating else 0f
    val hasReviews = reviewsCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            Text(
                text = "Sin opiniones AÃºn",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun ProductBadgesRow(category: String) {
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
                    text = "mÃ¡s visto",
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

@Composable
fun ProductPriceSection(
    price: Double,
    originalPrice: Double? = null
) {
    val animatedPrice by animateFloatAsState(
        targetValue = price.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "price"
    )

    val precioAnterior = originalPrice ?: (price * 1.25)
    val animatedPrecioAnterior by animateFloatAsState(
        targetValue = precioAnterior.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "originalPrice"
    )

    val descuento = if (precioAnterior > 0) ((precioAnterior - price) / precioAnterior * 100).toInt() else 0
    val ahorro = precioAnterior - price
    val animatedAhorro by animateFloatAsState(
        targetValue = ahorro.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "savings"
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (descuento > 0) {
            Text(
                text = "$${String.format("%,.2f", animatedPrecioAnterior.toDouble())}",
                fontSize = 14.sp,
                color = TextMuted,
                textDecoration = TextDecoration.LineThrough
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

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

@Composable
fun ProductDetailsSection(
    condition: String,
    category: String,
    warranty: String = "Sin GarantÃ­a",
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
        DetailRow("CondiciÃ³n", condition)
        DetailRow("categorÃ­a", displayCategory)
        DetailRow("Disponibilidad", "En stock")
        DetailRow("garantÃ­a", warranty)
        DetailRow("DevoluciÃ³n", if (returnsAccepted) "Aceptada" else "No aceptada")
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
