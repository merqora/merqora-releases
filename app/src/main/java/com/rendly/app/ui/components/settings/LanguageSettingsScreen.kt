package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.UserPreferencesRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String
)

private val AVAILABLE_LANGUAGES = listOf(
    LanguageOption("es", "Español", "Español (Latinoamérica)"),
    LanguageOption("en", "English", "English (US)")
)

@Composable
fun LanguageSettingsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("es") }
    var autoDetect by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var rowExists by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val userId = remember { SupabaseClient.auth.currentUserOrNull()?.id ?: "" }
    
    // Load preference from Supabase
    LaunchedEffect(isVisible) {
        if (isVisible && userId.isNotBlank()) {
            isLoading = true
            // Ensure default row exists first
            val pref = UserPreferencesRepository.ensureDefaultLanguagePreference(userId)
            rowExists = true
            selectedLanguage = pref.languageCode
            autoDetect = pref.autoDetect
            isLoading = false
        }
    }
    
    // Save to Supabase when changed
    fun saveLanguage(code: String, auto: Boolean) {
        if (userId.isBlank()) return
        scope.launch {
            val success = UserPreferencesRepository.saveLanguagePreference(userId, code, auto, rowExists)
            if (success && !rowExists) rowExists = true
        }
    }
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp)
                .clickable(enabled = false) { },
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Idioma",
                    subtitle = "Selecciona tu idioma preferido",
                    icon = Icons.Outlined.Language,
                    iconColor = Color(0xFF2E8B57),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryPurple)
                        }
                    } else {
                    
                    // Auto-detect toggle
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { 
                                val newValue = !autoDetect
                                autoDetect = newValue
                                saveLanguage(selectedLanguage, newValue)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF2E8B57).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Detectar automáticamente",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Usar el idioma de tu dispositivo",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            
                            Switch(
                                checked = autoDetect,
                                onCheckedChange = { 
                                    autoDetect = it
                                    saveLanguage(selectedLanguage, it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2E8B57)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsSectionTitle("Idiomas disponibles")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            AVAILABLE_LANGUAGES.forEachIndexed { index, language ->
                                val isSelected = selectedLanguage == language.code && !autoDetect
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !autoDetect) {
                                            selectedLanguage = language.code
                                            saveLanguage(language.code, autoDetect)
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Professional Canvas-drawn flag
                                    LanguageFlagIcon(languageCode = language.code)
                                    
                                    Spacer(modifier = Modifier.width(14.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = language.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (autoDetect) TextMuted else TextPrimary
                                        )
                                        Text(
                                            text = language.nativeName,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                
                                if (index < AVAILABLE_LANGUAGES.size - 1) {
                                    Divider(
                                        color = BorderSubtle,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1565A0).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color(0xFF1565A0),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "El cambio de idioma puede requerir reiniciar la aplicación para aplicarse completamente.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    } // end if (!isLoading)
                }
            }
        }
    }
}

/**
 * Professional Canvas-drawn flag icons for language selection
 */
@Composable
private fun LanguageFlagIcon(languageCode: String) {
    Canvas(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
    ) {
        val w = size.width
        val h = size.height
        
        // Draw rounded rect clip path
        val clipRR = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f, top = 0f, right = w, bottom = h,
                    cornerRadius = CornerRadius(8f, 8f)
                )
            )
        }
        
        clipPath(clipRR) {
            when (languageCode) {
                "es" -> drawSpainFlag(w, h)
                "en" -> drawUSAFlag(w, h)
            }
        }
    }
}

private fun DrawScope.drawSpainFlag(w: Float, h: Float) {
    val redColor = Color(0xFFC60B1E)
    val yellowColor = Color(0xFFFFC400)
    
    // Top red stripe (1/4)
    drawRect(color = redColor, topLeft = Offset.Zero, size = Size(w, h * 0.25f))
    // Yellow stripe (2/4)
    drawRect(color = yellowColor, topLeft = Offset(0f, h * 0.25f), size = Size(w, h * 0.5f))
    // Bottom red stripe (1/4)
    drawRect(color = redColor, topLeft = Offset(0f, h * 0.75f), size = Size(w, h * 0.25f))
    
    // Coat of arms hint (small shield shape in center)
    val shieldX = w * 0.3f
    val shieldY = h * 0.35f
    val shieldW = w * 0.08f
    val shieldH = h * 0.3f
    drawRoundRect(
        color = Color(0xFFAD1519),
        topLeft = Offset(shieldX, shieldY),
        size = Size(shieldW, shieldH),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = Color(0xFFFABD00),
        topLeft = Offset(shieldX + shieldW + 1f, shieldY),
        size = Size(shieldW, shieldH),
        cornerRadius = CornerRadius(2f, 2f)
    )
}

private fun DrawScope.drawUSAFlag(w: Float, h: Float) {
    val redColor = Color(0xFFB22234)
    val whiteColor = Color(0xFFFFFFFF)
    val blueColor = Color(0xFF3C3B6E)
    
    // Draw 13 alternating red/white stripes
    val stripeHeight = h / 13f
    for (i in 0 until 13) {
        drawRect(
            color = if (i % 2 == 0) redColor else whiteColor,
            topLeft = Offset(0f, i * stripeHeight),
            size = Size(w, stripeHeight)
        )
    }
    
    // Blue canton (top-left corner covering 7 stripes)
    val cantonW = w * 0.4f
    val cantonH = stripeHeight * 7f
    drawRect(color = blueColor, topLeft = Offset.Zero, size = Size(cantonW, cantonH))
    
    // Simplified stars (3x3 grid of dots)
    val starColor = Color.White
    val dotR = cantonW * 0.04f
    val cols = 5
    val rows = 4
    val spacingX = cantonW / (cols + 1)
    val spacingY = cantonH / (rows + 1)
    for (row in 1..rows) {
        for (col in 1..cols) {
            drawCircle(
                color = starColor,
                radius = dotR,
                center = Offset(col * spacingX, row * spacingY)
            )
        }
    }
}
