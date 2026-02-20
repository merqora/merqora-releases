package com.rendly.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rendly.app.data.repository.ProfileRepository
import com.rendly.app.ui.theme.*

data class NavItem(
    val route: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val contentDescription: String
)

val navItems = listOf(
    NavItem("home", Icons.Filled.Home, Icons.Outlined.Home, "Inicio"),
    NavItem("explore", Icons.Filled.Search, Icons.Outlined.Search, "Explorar"),
    NavItem("sell", Icons.Filled.AddBox, Icons.Outlined.AddBox, "Vender"), // Icono más creativo
    NavItem("videos", Icons.Filled.Movie, Icons.Outlined.Movie, "Rends"), // Claqueta de cine
    NavItem("profile", Icons.Filled.Person, Icons.Outlined.Person, "Perfil")
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onHomeReclick: () -> Unit = {},
    userAvatarUrl: String? = null, // Avatar del usuario para el tab de perfil (opcional, se carga automáticamente si es null)
    modifier: Modifier = Modifier
) {
    val navBarBg = themedNavBarBg()
    val activeColor = IconAccentBlue
    val inactiveIconColor = themedInactiveColor()
    
    // Cargar avatar automáticamente usando StateFlow del ProfileRepository
    // Esto hace que el avatar se actualice automáticamente cuando se carga el perfil
    val currentProfile by ProfileRepository.currentProfile.collectAsState()
    
    // Cargar perfil al inicio si no está cargado
    LaunchedEffect(Unit) {
        if (currentProfile == null) {
            try {
                ProfileRepository.loadCurrentProfile()
            } catch (e: Exception) {
                // Silently fail - just show icon instead
            }
        }
    }
    
    // Usar avatar del parámetro o del perfil cargado
    val effectiveAvatarUrl = userAvatarUrl ?: currentProfile?.avatarUrl
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(navBarBg)
            .padding(vertical = 4.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
            when {
                // Si es el tab de perfil y hay avatar, mostrar avatar en lugar de icono
                item.route == "profile" && effectiveAvatarUrl != null -> {
                    ProfileAvatarNavItem(
                        avatarUrl = effectiveAvatarUrl,
                        isSelected = currentRoute == item.route,
                        activeColor = activeColor,
                        onClick = { onNavigate(item.route) }
                    )
                }
                // Botón central CREATIVO para vender/publicar
                item.route == "sell" -> {
                    CreativeCenterButton(
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
                else -> {
                    NavBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        activeColor = activeColor,
                        inactiveColor = inactiveIconColor,
                        onClick = { 
                            if (item.route == "home" && currentRoute == "home") {
                                onHomeReclick()
                            } else {
                                onNavigate(item.route) 
                            }
                        }
                    )
                }
            }
        }
    }
}

// Botón central CREATIVO - Diseño premium con gradiente azul-esmeralda
@Composable
private fun CreativeCenterButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(200),
        label = "centerScale"
    )
    
    // Colores premium: azul profundo a esmeralda
    val gradientColors = listOf(
        Color(0xFF0A3D62), // Azul marino profundo
        Color(0xFF1E6F5C), // Verde esmeralda oscuro
        Color(0xFF2E8B57)  // Verde mar
    )
    
    Box(
        modifier = Modifier
            .size(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        // Glow effect sutil
        Canvas(modifier = Modifier.size(50.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2E8B57).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension / 1.6f
            )
        }
        
        // Botón principal con gradiente premium
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(8.dp, CircleShape, clip = false, ambientColor = Color(0xFF2E8B57).copy(alpha = 0.4f))
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(100f, 100f)
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color(0xFF2E8B57).copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.15f),
                            Color(0xFF0A3D62).copy(alpha = 0.4f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Icono de + elegante
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Publicar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    activeColor: androidx.compose.ui.graphics.Color,
    inactiveColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val activeAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200),
        label = "activeAlpha"
    )
    
    val inactiveAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0f else 1f,
        animationSpec = tween(200),
        label = "inactiveAlpha"
    )
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Icono inactivo (outline)
        Icon(
            imageVector = item.iconOutlined,
            contentDescription = item.contentDescription,
            tint = inactiveColor,
            modifier = Modifier
                .size(26.dp)
                .alpha(inactiveAlpha)
        )
        
        // Icono activo (filled) - superpuesto con opacidad animada
        Icon(
            imageVector = item.iconFilled,
            contentDescription = item.contentDescription,
            tint = activeColor,
            modifier = Modifier
                .size(26.dp)
                .alpha(activeAlpha)
        )
    }
}

@Composable
private fun ProfileAvatarNavItem(
    avatarUrl: String,
    isSelected: Boolean,
    activeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) Modifier.border(2.dp, activeColor, CircleShape)
                    else Modifier.border(1.5.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f), CircleShape)
                )
        )
    }
}
