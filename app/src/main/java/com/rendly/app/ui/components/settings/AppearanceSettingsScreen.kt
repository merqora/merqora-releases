package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.preferences.AppPreferences
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AppearanceSettingsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { AppPreferences(context) }
    
    // Estados conectados a las preferencias reales
    var selectedTheme by remember { mutableStateOf(AppPreferences.THEME_DARK) }
    var selectedAccentColor by remember { mutableStateOf(AppPreferences.ACCENT_PURPLE) }
    var compactMode by remember { mutableStateOf(false) }
    var showAnimations by remember { mutableStateOf(true) }
    
    // Cargar preferencias al iniciar
    LaunchedEffect(Unit) {
        launch { preferences.themeFlow.collect { selectedTheme = it } }
        launch { preferences.accentColorFlow.collect { selectedAccentColor = it } }
        launch { preferences.compactModeFlow.collect { compactMode = it } }
        launch { preferences.showAnimationsFlow.collect { showAnimations = it } }
    }
    
    // Colores dinámicos
    val bgColor = themedHomeBg()
    val surfaceColor = themedSurface()
    val textColor = themedTextPrimary()
    val mutedColor = themedTextMuted()
    val accent = accentColor()
    
    // Funciones para guardar preferencias
    fun updateTheme(theme: String) {
        selectedTheme = theme
        scope.launch { preferences.setTheme(theme) }
    }
    
    fun updateAccentColor(color: String) {
        selectedAccentColor = color
        scope.launch { preferences.setAccentColor(color) }
    }
    
    fun updateCompactMode(compact: Boolean) {
        compactMode = compact
        scope.launch { preferences.setCompactMode(compact) }
    }
    
    fun updateShowAnimations(show: Boolean) {
        showAnimations = show
        scope.launch { preferences.setShowAnimations(show) }
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
            color = bgColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Apariencia",
                    subtitle = "Personaliza el aspecto de la app",
                    icon = Icons.Outlined.Palette,
                    iconColor = Color(0xFFFF6B35),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsSectionTitle("Tema")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ThemeOption(
                                    icon = Icons.Outlined.LightMode,
                                    label = "Claro",
                                    isSelected = selectedTheme == AppPreferences.THEME_LIGHT,
                                    onClick = { updateTheme(AppPreferences.THEME_LIGHT) },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeOption(
                                    icon = Icons.Outlined.DarkMode,
                                    label = "Oscuro",
                                    isSelected = selectedTheme == AppPreferences.THEME_DARK,
                                    onClick = { updateTheme(AppPreferences.THEME_DARK) },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemeOption(
                                    icon = Icons.Outlined.Contrast,
                                    label = "Sistema",
                                    isSelected = selectedTheme == AppPreferences.THEME_SYSTEM,
                                    onClick = { updateTheme(AppPreferences.THEME_SYSTEM) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsSectionTitle("Color de acento")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ColorOption(
                                    color = PrimaryPurple,
                                    label = "Púrpura",
                                    isSelected = selectedAccentColor == AppPreferences.ACCENT_PURPLE,
                                    onClick = { updateAccentColor(AppPreferences.ACCENT_PURPLE) }
                                )
                                ColorOption(
                                    color = AccentPink,
                                    label = "Rosa",
                                    isSelected = selectedAccentColor == AppPreferences.ACCENT_PINK,
                                    onClick = { updateAccentColor(AppPreferences.ACCENT_PINK) }
                                )
                                ColorOption(
                                    color = Color(0xFF1565A0),
                                    label = "Azul",
                                    isSelected = selectedAccentColor == AppPreferences.ACCENT_BLUE,
                                    onClick = { updateAccentColor(AppPreferences.ACCENT_BLUE) }
                                )
                                ColorOption(
                                    color = Color(0xFF2E8B57),
                                    label = "Verde",
                                    isSelected = selectedAccentColor == AppPreferences.ACCENT_GREEN,
                                    onClick = { updateAccentColor(AppPreferences.ACCENT_GREEN) }
                                )
                                ColorOption(
                                    color = Color(0xFFFF6B35),
                                    label = "Naranja",
                                    isSelected = selectedAccentColor == AppPreferences.ACCENT_ORANGE,
                                    onClick = { updateAccentColor(AppPreferences.ACCENT_ORANGE) }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsSectionTitle("Preferencias de visualización")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = surfaceColor
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { updateCompactMode(!compactMode) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ViewCompact,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Modo compacto",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                    Text(
                                        text = "Muestra más contenido en pantalla",
                                        fontSize = 12.sp,
                                        color = mutedColor
                                    )
                                }
                                Switch(
                                    checked = compactMode,
                                    onCheckedChange = { updateCompactMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accent
                                    )
                                )
                            }
                            
                            Divider(color = themedBorderSubtle(), modifier = Modifier.padding(horizontal = 16.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { updateShowAnimations(!showAnimations) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Animation,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Animaciones",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                    Text(
                                        text = "Transiciones y efectos visuales",
                                        fontSize = 12.sp,
                                        color = mutedColor
                                    )
                                }
                                Switch(
                                    checked = showAnimations,
                                    onCheckedChange = { updateShowAnimations(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accent
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else SurfaceElevated,
        border = if (isSelected) BorderStroke(2.dp, PrimaryPurple) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryPurple else TextMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryPurple else TextSecondary
            )
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) color else TextMuted
        )
    }
}

