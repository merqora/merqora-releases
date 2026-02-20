package com.rendly.app.ui.screens.benefits

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*

data class ClientBenefit(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val highlight: String? = null,
    val accentColor: Color = Color(0xFF2E8B57)
)

@Composable
fun ClientBenefitsScreen(
    isVisible: Boolean,
    sellerUsername: String,
    sellerAvatar: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Beneficios simulados (luego el vendedor los configurará)
    val benefits = remember {
        listOf(
            ClientBenefit(
                id = "discount",
                icon = Icons.Outlined.LocalOffer,
                title = "Descuentos Exclusivos",
                description = "Accede a descuentos especiales de hasta 30% en todos los productos de la tienda.",
                highlight = "Hasta 30% OFF",
                accentColor = Color(0xFF2E8B57)
            ),
            ClientBenefit(
                id = "early_access",
                icon = Icons.Outlined.Bolt,
                title = "Acceso Anticipado",
                description = "Sé el primero en ver y comprar nuevos productos antes que nadie.",
                highlight = "24h antes",
                accentColor = Color(0xFFFF6B35)
            ),
            ClientBenefit(
                id = "free_shipping",
                icon = Icons.Outlined.LocalShipping,
                title = "Envío Gratis",
                description = "Disfruta de envío gratuito en todas tus compras sin monto mínimo.",
                highlight = "Sin mínimo",
                accentColor = Color(0xFF1565A0)
            ),
            ClientBenefit(
                id = "priority_support",
                icon = Icons.Outlined.SupportAgent,
                title = "Atención Prioritaria",
                description = "Tus consultas y mensajes serán respondidos con prioridad máxima.",
                highlight = "Respuesta rápida",
                accentColor = Color(0xFFFF6B35)
            ),
            ClientBenefit(
                id = "exclusive_content",
                icon = Icons.Outlined.Stars,
                title = "Contenido Exclusivo",
                description = "Accede a historias, rends y contenido solo disponible para clientes VIP.",
                highlight = "Solo VIP",
                accentColor = Color(0xFF2E8B57)
            ),
            ClientBenefit(
                id = "rewards",
                icon = Icons.Outlined.CardGiftcard,
                title = "Programa de Recompensas",
                description = "Acumula puntos con cada compra y canjéalos por productos o descuentos.",
                highlight = "Gana puntos",
                accentColor = Color(0xFFEF4444)
            )
        )
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = fadeOut(tween(150)) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(HomeBg)
                .systemBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header con confetti effect
                item {
                    BenefitsHeader(
                        sellerUsername = sellerUsername,
                        sellerAvatar = sellerAvatar,
                        onBack = onDismiss
                    )
                }
                
                // Título de sección
                item {
                    Text(
                        text = "Tus Beneficios VIP",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
                
                // Lista de beneficios
                items(benefits, key = { it.id }) { benefit ->
                    BenefitCard(
                        benefit = benefit,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                
                // Nota final
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = PrimaryPurple.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Los beneficios pueden variar según las políticas del vendedor. Mantente atento a nuevas ofertas exclusivas.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
            
            // Botón flotante
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E8B57)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Celebration,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "¡Empezar a disfrutar!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BenefitsHeader(
    sellerUsername: String,
    sellerAvatar: String?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E8B57).copy(alpha = 0.2f),
                        Color(0xFF2E8B57).copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Surface.copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Success icon with animation
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            
            Box(
                modifier = Modifier
                    .size((80 * scale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2E8B57),
                                Color(0xFF2E8B57).copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Congratulations text
            Text(
                text = "¡Felicidades!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E8B57)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ahora eres cliente VIP de",
                fontSize = 16.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Seller info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF2E8B57), CircleShape)
                ) {
                    AsyncImage(
                        model = sellerAvatar ?: "https://ui-avatars.com/api/?name=$sellerUsername&background=10B981&color=fff",
                        contentDescription = "Avatar de $sellerUsername",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Text(
                    text = "@$sellerUsername",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF2E8B57).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "VIP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E8B57),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BenefitCard(
    benefit: ClientBenefit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = benefit.accentColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(benefit.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = benefit.icon,
                    contentDescription = null,
                    tint = benefit.accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = benefit.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    if (benefit.highlight != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = benefit.accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = benefit.highlight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = benefit.accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = benefit.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            
            // Check indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(benefit.accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Incluido",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
