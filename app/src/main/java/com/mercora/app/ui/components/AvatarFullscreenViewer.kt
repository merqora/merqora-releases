package com.mercora.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.roundToInt

@Composable
fun AvatarFullscreenViewer(
    avatarUrl: String?,
    username: String,
    shape: Shape = CircleShape,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = tween(300),
        label = "avatarScale"
    )

    var rotation by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val displayRotation by animateFloatAsState(
        targetValue = if (isDragging) rotation else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "avatarRotation"
    )

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Cerrar",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(28.dp)
                .clickable(onClick = onDismiss)
                .zIndex(1f)
        )

        val avatarToLoad = if (!avatarUrl.isNullOrBlank()) {
            if (avatarUrl.startsWith("http")) avatarUrl
            else "https://xyrpmmnegzjkbysoocpc.supabase.co/storage/v1/object/public/avatars_new/$avatarUrl"
        } else {
            "https://ui-avatars.com/api/?name=$username&background=A78BFA&color=fff&size=256"
        }

        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale)
                .graphicsLayer {
                    rotationY = displayRotation
                    cameraDistance = 12f * density.density
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onHorizontalDrag = { _, dragAmount ->
                            rotation = (rotation + dragAmount * 0.4f).coerceIn(-180f, 180f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarToLoad)
                    .crossfade(true)
                    .size(512)
                    .build(),
                contentDescription = "Avatar de $username",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            )
        }
    }
}
