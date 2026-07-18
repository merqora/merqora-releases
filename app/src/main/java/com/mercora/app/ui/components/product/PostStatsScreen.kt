package com.mercora.app.ui.components.product

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.model.Post
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PostStatsScreen(
    isVisible: Boolean,
    post: Post?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible && post != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        if (post != null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { } // Prevent clicks from passing through
                    ),
                color = HomeBg
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    StatsHeader(title = post.title.ifEmpty { post.producto.titulo }, onClose = onDismiss)

                    // Content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            TimeSincePublishSection(createdAt = post.createdAt)
                        }

                        item {
                            Text(
                                text = "MÃ©tricas principales",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid of stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Vistas",
                                    value = post.viewsCount.toString(),
                                    icon = Icons.Outlined.Visibility,
                                    color = Color(0xFF1565A0)
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Me gusta",
                                    value = post.likesCount.toString(),
                                    icon = Icons.Outlined.FavoriteBorder,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Guardados",
                                    value = post.savesCount.toString(),
                                    icon = Icons.Outlined.BookmarkBorder,
                                    color = Color(0xFFFF6B35)
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Compartidos",
                                    value = post.sharesCount.toString(),
                                    icon = Icons.Outlined.Share,
                                    color = Color(0xFF2E8B57)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            EngagementChart(
                                likes = post.likesCount,
                                saves = post.savesCount,
                                shares = post.sharesCount,
                                reviews = post.reviewsCount
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsHeader(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "EstadÃ­sticas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceElevated)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TimeSincePublishSection(createdAt: String) {
    val dateText = remember(createdAt) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(createdAt) ?: return@remember "Fecha desconocida"
            
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val formattedDate = outputFormat.format(date)
            
            val diffMs = System.currentTimeMillis() - date.time
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            
            if (diffDays == 0L) {
                "Publicado hoy ($formattedDate)"
            } else {
                "Publicado hace $diffDays dÃ­as ($formattedDate)"
            }
        } catch (e: Exception) {
            "Fecha de publicaciÃ³n desconocida"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = PrimaryBright,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "AntigÃ¼edad",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = dateText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    var animated by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        animated = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (animated) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "scale"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                color.copy(alpha = 0.8f),
                                color
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            
            Text(
                text = title,
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EngagementChart(likes: Int, saves: Int, shares: Int, reviews: Int) {
    val total = (likes + saves + shares + reviews).coerceAtLeast(1)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Interacciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$total interacciones en total",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ChartRow("Me gusta", likes, total, Color(0xFFEF4444))
            Spacer(modifier = Modifier.height(16.dp))
            ChartRow("Guardados", saves, total, Color(0xFFFF6B35))
            Spacer(modifier = Modifier.height(16.dp))
            ChartRow("Compartidos", shares, total, Color(0xFF2E8B57))
            Spacer(modifier = Modifier.height(16.dp))
            ChartRow("ReseÃ±as", reviews, total, Color(0xFF8B5CF6))
        }
    }
}

@Composable
private fun ChartRow(label: String, value: Int, total: Int, color: Color) {
    var animated by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        animated = true
    }
    
    val targetPercentage = if (total > 0) value.toFloat() / total else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = if (animated) targetPercentage else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "barChart"
    )
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = value.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
