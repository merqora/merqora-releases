package com.vinzay.app.ui.components.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinzay.app.ui.theme.*

@Composable
fun ReviewsSection(
    ratingDistribution: RatingDistribution = RatingDistribution(),
    onViewAll: () -> Unit = {}
) {
    val reviewsCount = ratingDistribution.total
    val rating = ratingDistribution.averageRating
    val hasReviews = reviewsCount > 0

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Opiniones",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasReviews) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", rating),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Row {
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
                    }
                    Text(
                        text = "$reviewsCount opiniones",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    RatingBar(stars = 5, percent = ratingDistribution.percentFor(5))
                    RatingBar(stars = 4, percent = ratingDistribution.percentFor(4))
                    RatingBar(stars = 3, percent = ratingDistribution.percentFor(3))
                    RatingBar(stars = 2, percent = ratingDistribution.percentFor(2))
                    RatingBar(stars = 1, percent = ratingDistribution.percentFor(1))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewAll() },
                shape = RoundedCornerShape(10.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver todas las opiniones",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IconAccentBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = IconAccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewAll() },
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = null,
                        tint = PrimaryPurple.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aún no hay opiniones",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Text(
                        text = "Sé el primero en opinar",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onViewAll() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Escribir una opinión",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(stars: Int, percent: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "$stars",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.width(12.dp)
        )
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = AccentYellow,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SurfaceElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent)
                    .background(AccentYellow)
            )
        }
    }
}
