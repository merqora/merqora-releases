package com.mercora.app.ui.screens.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.biometric.BiometricEnrollmentManager
import com.mercora.app.ui.theme.*

private const val REQUIRED_SCANS = 5

@Composable
fun BiometricEnrollmentScreen(
    userId: String,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var scanCount by remember { mutableIntStateOf(0) }
    var showSuccess by remember { mutableStateOf(false) }

    val progress = scanCount.toFloat() / REQUIRED_SCANS
    val fillColor by animateColorAsState(
        targetValue = if (progress >= 1f) PrimaryBright else if (progress > 0f) PrimaryPurple else TextMuted,
        label = "fillColor"
    )

    fun doBiometric() {
        val activity = context as? androidx.fragment.app.FragmentActivity ?: return
        BiometricPrompt(
            activity,
            androidx.core.content.ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val next = scanCount + 1
                    if (next >= REQUIRED_SCANS) {
                        BiometricEnrollmentManager.markEnrolled(context, userId)
                        showSuccess = true
                    }
                    scanCount = next
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        ).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Configurar huella digital")
                .setSubtitle(subtitleForStep(scanCount))
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }

    // Auto-trigger: starts on mount, continues after each success
    LaunchedEffect(scanCount) {
        if (!showSuccess && scanCount < REQUIRED_SCANS) {
            if (scanCount > 0) kotlinx.coroutines.delay(400)
            doBiometric()
        }
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            BiometricEnrollmentManager.syncToSupabase(userId)
            kotlinx.coroutines.delay(1200)
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = "Configurar huella",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = subtitleForStep(scanCount),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(48.dp))

            // Circular progress ring + icon
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeW = 10.dp.toPx()
                    val outerRadius = size.minDimension / 2 - strokeW / 2
                    val rect = androidx.compose.ui.geometry.Rect(
                        offset = Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
                        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                    )
                    drawArc(
                        color = BorderSubtle,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PrimaryPurple,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = null,
                    tint = fillColor,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            if (!showSuccess) {
                Text(
                    text = messageForStep(scanCount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (scanCount > 0) "$scanCount de $REQUIRED_SCANS" else "",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                if (scanCount > 0) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(REQUIRED_SCANS) { i ->
                            val dotColor by animateColorAsState(
                                targetValue = if (i < scanCount) PrimaryPurple else BorderSubtle,
                                label = "dot$i"
                            )
                            Box(
                                modifier = Modifier
                                    .size(if (i < scanCount) 14.dp else 10.dp)
                                    .background(dotColor, RoundedCornerShape(7.dp))
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                // Fallback button (in case user closes the system prompt)
                Surface(
                    onClick = { doBiometric() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryPurple
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.Fingerprint, null,
                            tint = Color.White, modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            buttonTextForStep(scanCount),
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Omitir",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onSkip() }
                )
            } else {
                Text(
                    text = "Â¡ConfiguraciÃ³n completa!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ahora puedes iniciar sesiÃ³n con tu huella",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun subtitleForStep(step: Int): String = when (step) {
    0 -> "Coloca tu dedo en el sensor para empezar"
    1 -> "Coloca el dedo nuevamente"
    2 -> "Sigue colocando el dedo"
    3 -> "Â¡Casi listo!"
    4 -> "Una vez mÃ¡s"
    else -> "ConfiguraciÃ³n completada"
}

private fun messageForStep(step: Int): String = when (step) {
    0 -> "Coloca tu dedo"
    1, 2 -> "Coloca el dedo nuevamente"
    3 -> "Â¡Casi listo!"
    4 -> "Una vez mÃ¡s"
    else -> ""
}

private fun buttonTextForStep(step: Int): String = when (step) {
    0 -> "Comenzar configuraciÃ³n"
    in 1..3 -> "Coloca el dedo nuevamente"
    4 -> "Una vez mÃ¡s"
    else -> "Completado"
}

fun isBiometricEnrolled(context: Context, userId: String): Boolean {
    return BiometricEnrollmentManager.isEnrolled(context, userId)
}
