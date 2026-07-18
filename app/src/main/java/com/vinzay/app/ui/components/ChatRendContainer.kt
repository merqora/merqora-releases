package com.vinzay.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.vinzay.app.ui.theme.*

data class SharedRendData(
    val rendId: String,
    val videoUrl: String,
    val thumbnailUrl: String = "",
    val productTitle: String? = null,
    val productPrice: Double? = null,
    val productImage: String? = null,
    val ownerUsername: String = "",
    val ownerAvatar: String = "",
    val isOwnerVerified: Boolean = false,
    val customMessage: String = ""
)

fun parseSharedRendJson(content: String): SharedRendData? {
    return try {
        val jsonStr = content.removePrefix("[SHARED_REND]")
        val json = org.json.JSONObject(jsonStr)
        SharedRendData(
            rendId = json.optString("rendId", ""),
            videoUrl = json.optString("videoUrl", ""),
            thumbnailUrl = json.optString("thumbnailUrl", ""),
            productTitle = json.optString("productTitle", "").ifEmpty { null },
            productPrice = if (json.has("productPrice") && !json.isNull("productPrice")) json.optDouble("productPrice", 0.0) else null,
            productImage = json.optString("productImage", "").ifEmpty { null },
            ownerUsername = json.optString("ownerUsername", ""),
            ownerAvatar = json.optString("ownerAvatar", ""),
            isOwnerVerified = json.optBoolean("isOwnerVerified", false),
            customMessage = json.optString("customMessage", "")
        )
    } catch (e: Exception) {
        android.util.Log.e("ChatRendContainer", "Error parsing SharedRend: ${e.message}")
        null
    }
}

@Composable
fun ChatRendContainer(
    data: SharedRendData,
    isFromMe: Boolean,
    onClickRend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isFromMe) Color.White else Color(0xFFE7E9EA)
    val mutedColor = if (isFromMe) Color.White.copy(alpha = 0.6f) else Color(0xFF8B98A5)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClickRend(data.rendId) }
    ) {
        // Thumbnail — full width, taller, black background for video
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color.Black)
        ) {
            val thumbUrl = data.thumbnailUrl.takeIf { it.isNotBlank() }
                ?: "${data.videoUrl}/ik-thumbnail.jpg"
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbUrl)
                    .crossfade(100)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )

            // Play button overlay
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Price badge
            if (data.productPrice != null && data.productPrice > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            AccentGreen.copy(alpha = 0.9f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$${String.format("%.0f", data.productPrice)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Owner info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (data.ownerAvatar.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data.ownerAvatar)
                        .crossfade(100)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .size(64)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = data.ownerUsername,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (data.isOwnerVerified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verificado",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Product title
        if (!data.productTitle.isNullOrBlank()) {
            Text(
                text = data.productTitle,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Separator line
        if (data.customMessage.isNotBlank() && !data.productTitle.isNullOrBlank()) {
            Divider(
                color = textColor.copy(alpha = 0.12f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Custom message
        if (data.customMessage.isNotBlank()) {
            Text(
                text = data.customMessage,
                color = mutedColor,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(6.dp))
    }
}
