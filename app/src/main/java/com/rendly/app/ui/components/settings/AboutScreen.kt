package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

@Composable
fun AboutScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
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
            ) {
                // Header
                SettingsScreenHeader(
                    title = "Acerca de Merqora",
                    subtitle = "Información de la Aplicación",
                    icon = Icons.Outlined.Info,
                    iconColor = Color(0xFFFF6B35),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Logo y Versión
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryPurple,
                                            Color(0xFFFF6B35)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Merqora",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Versión 1.0.0 (Build 2024.01.25)",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF2E8B57).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "? Actualizado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E8B57),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Descripción
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Surface
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tu marketplace social favorito",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Merqora combina lo mejor de las redes sociales con un marketplace moderno. Compra, vende y conecta con personas que comparten tus intereses.",
                                fontSize = 14.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 21.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // estadísticas
                    Text(
                        text = "Merqora en Números",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            value = "1M+",
                            label = "Usuarios",
                            icon = Icons.Outlined.People,
                            color = Color(0xFF1565A0),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = "500K+",
                            label = "Productos",
                            icon = Icons.Outlined.ShoppingBag,
                            color = Color(0xFF2E8B57),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = "50+",
                            label = "Países",
                            icon = Icons.Outlined.Public,
                            color = Color(0xFFFF6B35),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Links
                    Text(
                        text = "más Información",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Surface
                    ) {
                        Column {
                            AboutLinkItem(
                                icon = Icons.Outlined.Language,
                                title = "Sitio web",
                                subtitle = "www.Merqora.com",
                                onClick = { }
                            )
                            
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            
                            AboutLinkItem(
                                icon = Icons.Outlined.Description,
                                title = "términos y condiciones",
                                subtitle = "Lee nuestros Términos de uso",
                                onClick = { }
                            )
                            
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            
                            AboutLinkItem(
                                icon = Icons.Outlined.Policy,
                                title = "política de privacidad",
                                subtitle = "cómo manejamos tus datos",
                                onClick = { }
                            )
                            
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            
                            AboutLinkItem(
                                icon = Icons.Outlined.Code,
                                title = "Licencias de Código abierto",
                                subtitle = "Bibliotecas que usamos",
                                onClick = { }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Redes sociales
                    Text(
                        text = "Síguenos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialButton(
                            icon = Icons.Default.Facebook,
                            color = Color(0xFF1877F2),
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        SocialButton(
                            icon = Icons.Default.Camera,
                            color = Color(0xFFE4405F),
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        SocialButton(
                            icon = Icons.Default.Close,
                            color = TextPrimary,
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        SocialButton(
                            icon = Icons.Default.PlayArrow,
                            color = Color(0xFFFF0000),
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Equipo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryPurple.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Favorite,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Hecho con ?? en Argentina",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Por un equipo apasionado por crear experiencias increíbles para compradores y vendedores.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Copyright
                    Text(
                        text = "© 2024 Merqora. Todos los derechos reservados.",
                        fontSize = 12.sp,
                        color = TextMuted.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun AboutLinkItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextMuted
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted
        )
    }
}

@Composable
private fun SocialButton(
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
