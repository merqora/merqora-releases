package com.mercora.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WelcomeOverlay(
    username: String,
    isVisible: Boolean,
    onAnimationEnd: () -> Unit
) {
    val portalProgress = remember { Animatable(0f) }
    val welcomeProgress = remember { Animatable(0f) }
    val revealProgress = remember { Animatable(0f) }
    val isComplete = remember { mutableStateOf(true) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            isComplete.value = false
            portalProgress.snapTo(0f)
            welcomeProgress.snapTo(0f)
            revealProgress.snapTo(0f)

            portalProgress.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 260f))
            delay(80)
            welcomeProgress.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 200f))
            delay(500)
            revealProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            delay(150)
            isComplete.value = true
            onAnimationEnd()
        }
    }

    // Contenido visible SOLO durante la animaciÃ³n. Al completarse,
    // no se renderiza nada para no interceptar eventos tÃ¡ctiles del Home.
    if (!isComplete.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (isComplete.value) 0f else 1f },
            contentAlignment = Alignment.Center
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E).copy(alpha = (0.85f + portalProgress.value * 0.15f).coerceIn(0f, 1f)),
                            AccentGold.copy(alpha = 0.02f),
                            HomeBg
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha((1f - revealProgress.value).coerceIn(0f, 1f))
                .scale((1f - revealProgress.value * 0.12f).coerceAtLeast(0f))
        ) {
            val logoScale = portalProgress.value.let { p ->
                when {
                    p < 0.35f -> p / 0.35f * 0.3f
                    p < 0.6f -> 0.3f + (p - 0.35f) / 0.25f * 0.7f
                    else -> 1f - (p - 0.6f) / 0.4f * 0.12f
                }
            }

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(logoScale.coerceAtLeast(0.01f))
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(AccentGold, Color(0xFF1565A0), PrimaryPurple, AccentGreen, AccentGold)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D1117).copy(alpha = 0.35f))
                )
                Text("M", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.alpha(0.7f))
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .width(((welcomeProgress.value * 160f).coerceIn(0.1f, 200f)).dp)
                    .height(2.dp)
                    .background(AccentGold.copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Â¡Bienvenido,",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(welcomeProgress.value.coerceIn(0f, 1f))
                    .offset(y = ((1f - welcomeProgress.value) * 18).dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = username,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(welcomeProgress.value.coerceIn(0f, 1f))
                    .offset(y = ((1f - welcomeProgress.value) * 12).dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tu tienda, tu comunidad, tu mundo",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha((welcomeProgress.value * 0.8f).coerceIn(0f, 1f))
                    .offset(y = ((1f - welcomeProgress.value) * 8).dp)
            )
        }
    }
    }
}
