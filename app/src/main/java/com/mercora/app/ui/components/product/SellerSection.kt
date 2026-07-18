package com.mercora.app.ui.components.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mercora.app.data.model.SellerStats
import com.mercora.app.ui.components.VerifiedBadge
import com.mercora.app.ui.theme.*

@Composable
fun SellerSectionV2(
    username: String,
    avatarUrl: String,
    storeName: String?,
    isVerified: Boolean = false,
    sellerStats: SellerStats? = null,
    onViewProfile: () -> Unit
) {
    val displayAvatar = remember(avatarUrl) {
        if (avatarUrl.startsWith("http")) avatarUrl
        else "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/$avatarUrl"
    }

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
            AsyncImage(
                model = displayAvatar,
                contentDescription = username,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onViewProfile() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = storeName ?: "@$username",
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
            }

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SellerStatItem(
                value = reputationValue,
                label = "ReputaciÃ³n",
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
fun SellerStatItem(value: String, label: String, color: Color) {
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
