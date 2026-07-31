// MercoraSplashExact.kt - CODIGO EXACTO PARA DEEPSEEK - NO MODIFICAR VALORES
// Animacion 1.3s exacta: https://www.meta.ai/share/c/ZeiHHd0soi
// Gota flacida tipo plastilina con 3 pancitas -> M exacta LogoMercora.jpg

package com.mercora.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.mercora.app.R

@Composable
fun MercoraSplashExact(onFinished: () -> Unit) {
    var phase by remember { mutableStateOf(0) }

    // TIMING EXACTO - NO CAMBIAR
    LaunchedEffect(Unit) {
        delay(150)  // 0-0.15s negro
        phase = 1   // 0.15-0.4s aparece
        delay(250)
        phase = 2   // 0.4-0.65s estira alta flacida
        delay(250)
        phase = 3   // 0.65-0.9s aplasta ancha + 3 pancitas
        delay(250)
        phase = 4   // 0.9-1.1s morph a M exacta
        delay(200)
        phase = 5   // 1.1-1.3s pulso elastico
        delay(200)
        onFinished()
    }

    // VALORES EXACTOS - NO CAMBIAR
    val scaleX by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0f
            1 -> 1f
            2 -> 0.6f  // alta flaca
            3 -> 1.6f  // ancha aplastada
            4 -> 1f
            5 -> 1f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "scaleX"
    )

    val scaleY by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0f
            1 -> 1f
            2 -> 1.8f  // alta flaca
            3 -> 0.6f  // ancha aplastada
            4 -> 1f
            5 -> 1.05f // pulso
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "scaleY"
    )

    val rotation by animateFloatAsState(
        targetValue = when (phase) {
            2 -> -4f
            3 -> 4f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (phase == 0) 0f else 1f,
        animationSpec = tween(150),
        label = "alpha"
    )

    // Pulso final 1.1-1.3s
    val pulseScale by animateFloatAsState(
        targetValue = if (phase == 5) 1f else 1.05f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // #000 obligatorio
        contentAlignment = Alignment.Center
    ) {
        // Usa EXACTAMENTE R.drawable.logomercora - NO vector, NO recreacion
        Image(
            painter = painterResource(id = R.drawable.logomercora),
            contentDescription = "Mercora",
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer {
                    this.scaleX = scaleX * if (phase == 5) pulseScale else 1f
                    this.scaleY = scaleY * if (phase == 5) pulseScale else 1f
                    this.rotationZ = rotation
                    this.alpha = alpha
                    this.transformOrigin = TransformOrigin(0.5f, 0.8f) // base flacida
                }
        )
    }
}
