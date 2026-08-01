package com.mercora.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mercora.app.R
import kotlinx.coroutines.delay

@Composable
fun MercoraSplashScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {}
) {
    var phase by remember { mutableIntStateOf(0) }

    val springBouncy = spring<Float>(
        dampingRatio = 0.5f,
        stiffness = 300f
    )

    val scaleX by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0f
            1 -> 0.35f
            2 -> 0.33f
            3 -> 0.88f
            else -> 1f
        },
        animationSpec = springBouncy,
        label = "scaleX"
    )

    val scaleY by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0f
            1 -> 0.35f
            2 -> 0.99f
            3 -> 0.33f
            else -> 1f
        },
        animationSpec = springBouncy,
        label = "scaleY"
    )

    val rotation by animateFloatAsState(
        targetValue = when (phase) {
            2 -> -4f
            3 -> 4f
            else -> 0f
        },
        animationSpec = tween(180),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(150L)  // 0-0.15s negro
        phase = 1    // 0.15-0.4s gota aparece
        delay(250L)
        phase = 2    // 0.4-0.65s estira alta flacida
        delay(250L)
        phase = 3    // 0.65-0.9s aplasta ancha + pancitas
        delay(250L)
        phase = 4    // 0.9-1.1s morph a M exacta
        delay(200L)
        phase = 5    // 1.1-1.3s pulso
        delay(200L)
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.logo_mercora),
            contentDescription = "Mercora",
            modifier = Modifier
                .size(400.dp)
                .graphicsLayer {
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                    this.rotationZ = rotation
                    this.alpha = if (phase == 0) 0f else 1f
                },
            contentScale = ContentScale.Fit
        )
    }
}
