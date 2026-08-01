package com.mercora.app.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.ui.theme.*

private val VerifiedBlue = Color(0xFF1D9BF0)
private val ReturnGreen = Color(0xFF2E8B57)

@Composable
fun ProductFeaturesCompact(
    freeShipping: Boolean = false,
    isVerified: Boolean = false,
    returnsAccepted: Boolean = false
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Beneficios",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureChip(
                icon = Icons.Outlined.LocalShipping,
                text = "envío gratis",
                isActive = freeShipping,
                activeColor = AccentGreen
            )
            FeatureChip(
                icon = Icons.Outlined.Verified,
                text = "Verificado",
                isActive = isVerified,
                activeColor = VerifiedBlue
            )
            FeatureChip(
                icon = Icons.Outlined.Autorenew,
                text = "Devolución",
                isActive = returnsAccepted,
                activeColor = ReturnGreen
            )
        }
    }
}

@Composable
fun FeatureChip(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    activeColor: Color = AccentGreen
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) activeColor.copy(alpha = 0.1f) else SurfaceElevated
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) activeColor else TextSecondary
            )
        }
    }
}
