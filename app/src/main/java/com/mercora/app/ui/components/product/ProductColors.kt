package com.mercora.app.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.ui.theme.*

@Composable
fun ProductColorsSection(
    colors: List<Pair<String, Color>>,
    selectedIndex: Int,
    onColorSelect: (Int) -> Unit,
    images: List<String> = emptyList(),
    onImageChange: (Int) -> Unit = {}
) {
    val selectedColorName = colors.getOrNull(selectedIndex)?.first ?: ""

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Color: $selectedColorName",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (colors.size > 4) {
                Text(
                    text = "${colors.size} disponibles",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            itemsIndexed(colors) { index, (colorName, color) ->
                val hasImage = index < images.size

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onColorSelect(index)
                            if (hasImage) onImageChange(index)
                        }
                        .background(
                            if (index == selectedIndex) PrimaryPurple.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (index == selectedIndex) 3.dp else 1.5.dp,
                                color = if (index == selectedIndex) PrimaryPurple else BorderSubtle,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = colorName,
                        fontSize = 11.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) PrimaryPurple else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
