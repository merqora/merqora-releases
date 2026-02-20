package com.rendly.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rendly.app.ui.theme.*

private data class ShopItem(
    val id: String,
    val title: String,
    val description: String,
    val impactTag: String,
    val icon: ImageVector,
    val price: Int,
    val gradient: List<Color>,
    val isPopular: Boolean = false
)

private data class ShopSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val items: List<ShopItem>
)

private val SHOP_SECTIONS = listOf(
    ShopSection(
        id = "growth",
        title = "Crecimiento & Visibilidad",
        subtitle = "Aumenta tu alcance y llega a más compradores",
        icon = Icons.Outlined.TrendingUp,
        items = listOf(
            ShopItem(
                id = "boost_post",
                title = "Impulsar Publicación",
                description = "Tu producto destacado en el feed principal durante 24h",
                impactTag = "5x más vistas",
                icon = Icons.Outlined.Rocket,
                price = 150,
                gradient = listOf(Color(0xFFFF6B35), Color(0xFF1565A0)),
                isPopular = true
            ),
            ShopItem(
                id = "smart_exposure",
                title = "Exposición Inteligente",
                description = "Algoritmo optimizado para mostrar tu producto a compradores interesados",
                impactTag = "Mayor conversión",
                icon = Icons.Outlined.AutoAwesome,
                price = 300,
                gradient = listOf(Color(0xFF6366F1), Color(0xFF818CF8))
            ),
            ShopItem(
                id = "local_reach",
                title = "Alcance Local",
                description = "Prioridad en búsquedas de tu zona geográfica",
                impactTag = "Ventas rápidas",
                icon = Icons.Outlined.LocationOn,
                price = 100,
                gradient = listOf(Color(0xFF2E8B57), Color(0xFF34D399))
            )
        )
    ),
    ShopSection(
        id = "profile",
        title = "Perfil & Autoridad",
        subtitle = "Construye confianza y profesionalismo",
        icon = Icons.Outlined.Verified,
        items = listOf(
            ShopItem(
                id = "verified_badge",
                title = "Insignia Verificado",
                description = "Distintivo de vendedor confiable visible en todas tus publicaciones",
                impactTag = "Mayor confianza",
                icon = Icons.Filled.Verified,
                price = 500,
                gradient = listOf(Color(0xFF1565A0), Color(0xFF60A5FA)),
                isPopular = true
            ),
            ShopItem(
                id = "profile_highlight",
                title = "Perfil Destacado",
                description = "Aparece en la sección de vendedores recomendados",
                impactTag = "Más seguidores",
                icon = Icons.Outlined.Star,
                price = 250,
                gradient = listOf(Color(0xFFFF6B35), Color(0xFFFF6B35))
            ),
            ShopItem(
                id = "custom_theme",
                title = "Tema Personalizado",
                description = "Colores y estilo único para tu perfil de vendedor",
                impactTag = "Marca personal",
                icon = Icons.Outlined.Palette,
                price = 200,
                gradient = listOf(Color(0xFF2E8B57), Color(0xFFF472B6))
            )
        )
    ),
    ShopSection(
        id = "sales",
        title = "Optimización de Ventas",
        subtitle = "Herramientas para cerrar más ventas",
        icon = Icons.Outlined.ShoppingCart,
        items = listOf(
            ShopItem(
                id = "priority_messages",
                title = "Mensajes Prioritarios",
                description = "Tus mensajes aparecen primero en la bandeja del comprador",
                impactTag = "Respuesta rápida",
                icon = Icons.Outlined.Email,
                price = 120,
                gradient = listOf(Color(0xFFFF6B35), Color(0xFFC084FC))
            ),
            ShopItem(
                id = "quick_response",
                title = "Respuesta Rápida",
                description = "Plantillas inteligentes y respuestas automáticas",
                impactTag = "Ahorra tiempo",
                icon = Icons.Outlined.FlashOn,
                price = 180,
                gradient = listOf(Color(0xFFFF6B35), Color(0xFFFCD34D))
            ),
            ShopItem(
                id = "conversion_boost",
                title = "Potenciador de Conversión",
                description = "Ofertas flash y descuentos exclusivos para tus visitantes",
                impactTag = "Más ventas",
                icon = Icons.Outlined.Percent,
                price = 220,
                gradient = listOf(Color(0xFF2E8B57), Color(0xFF6EE7B7)),
                isPopular = true
            )
        )
    ),
    ShopSection(
        id = "insights",
        title = "Insights & Herramientas Pro",
        subtitle = "Datos y análisis para decisiones inteligentes",
        icon = Icons.Outlined.Analytics,
        items = listOf(
            ShopItem(
                id = "advanced_analytics",
                title = "Analíticas Avanzadas",
                description = "Métricas detalladas de rendimiento, visitas y conversiones",
                impactTag = "Datos precisos",
                icon = Icons.Outlined.BarChart,
                price = 350,
                gradient = listOf(Color(0xFF6366F1), Color(0xFFA5B4FC))
            ),
            ShopItem(
                id = "buyer_insights",
                title = "Comportamiento de Compradores",
                description = "Entiende qué buscan y cómo interactúan con tus productos",
                impactTag = "Decisiones inteligentes",
                icon = Icons.Outlined.Psychology,
                price = 280,
                gradient = listOf(Color(0xFFFF6B35), Color(0xFFDDD6FE))
            ),
            ShopItem(
                id = "performance_reports",
                title = "Reportes de Rendimiento",
                description = "Informes semanales con recomendaciones personalizadas",
                impactTag = "Mejora continua",
                icon = Icons.Outlined.Assessment,
                price = 200,
                gradient = listOf(Color(0xFF1565A0), Color(0xFF93C5FD))
            )
        )
    )
)

@Composable
fun RendshopScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf<String?>(null) }
    var userCredits by remember { mutableIntStateOf(1250) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Modal states
    var selectedItem by remember { mutableStateOf<ShopItem?>(null) }
    var showItemModal by remember { mutableStateOf(false) }
    var showProBenefitsModal by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header
            item(key = "header") {
                RendshopHeader(
                    credits = userCredits,
                    onClose = onClose
                )
            }
            
            // Hero Banner
            item(key = "hero") {
                HeroBanner(onProClick = { showProBenefitsModal = true })
            }
            
            // Quick Actions
            item(key = "quick_actions") {
                QuickActionsRow()
            }
            
            // Search Bar
            item(key = "search") {
                SearchBarSection(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }
            
            // Sections
            SHOP_SECTIONS.forEach { section ->
                item(key = "section_header_${section.id}") {
                    SectionHeader(
                        section = section,
                        isExpanded = selectedSection == section.id,
                        onClick = {
                            selectedSection = if (selectedSection == section.id) null else section.id
                        }
                    )
                }
                
                item(key = "section_items_${section.id}") {
                    SectionItems(
                        items = section.items,
                        onItemClick = { item ->
                            selectedItem = item
                            showItemModal = true
                        }
                    )
                }
            }
            
            // Footer
            item(key = "footer") {
                RendshopFooter()
            }
        }
        
        // Item Detail Modal
        if (showItemModal && selectedItem != null) {
            ItemDetailModal(
                item = selectedItem!!,
                userCredits = userCredits,
                onDismiss = { showItemModal = false },
                onRedeem = { item ->
                    if (userCredits >= item.price) {
                        userCredits -= item.price
                    }
                    showItemModal = false
                },
                onBuy = { item ->
                    // Simulated purchase
                    showItemModal = false
                }
            )
        }
        
        // Pro Benefits Modal
        if (showProBenefitsModal) {
            ProBenefitsModal(
                onDismiss = { showProBenefitsModal = false }
            )
        }
    }
}

@Composable
private fun RendshopHeader(
    credits: Int,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Rendshop",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Herramientas para vendedores",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Credits pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$credits",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroBanner(onProClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF312E81),
                            Color(0xFF1E1B4B)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Diamond,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Pro Seller",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Maximiza tus ventas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Accede a herramientas exclusivas diseñadas para vendedores ambiciosos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    onClick = onProClick,
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGold
                ) {
                    Text(
                        text = "Ver beneficios Pro",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
            if (query.isEmpty()) {
                Text(
                    text = "Buscar herramientas...",
                    color = TextMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = query,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow() {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Acciones rápidas",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionCard(
                    icon = Icons.Outlined.Rocket,
                    title = "Impulsar",
                    subtitle = "Post rápido",
                    gradient = listOf(Color(0xFFFF6B35), Color(0xFF1565A0))
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Outlined.AddChart,
                    title = "Obtener",
                    subtitle = "Créditos",
                    gradient = listOf(Color(0xFFFF6B35), Color(0xFFFF6B35))
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Outlined.Analytics,
                    title = "Ver",
                    subtitle = "Estadísticas",
                    gradient = listOf(Color(0xFF1565A0), Color(0xFF60A5FA))
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Centro de",
                    subtitle = "Ayuda",
                    gradient = listOf(Color(0xFF2E8B57), Color(0xFF34D399))
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: List<Color>,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .width(150.dp)
                .height(68.dp)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$title $subtitle",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    section: ShopSection,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column {
                Text(
                    text = section.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = section.subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SectionItems(
    items: List<ShopItem>,
    onItemClick: (ShopItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ShopItemCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(18.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with icon and popular badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(item.gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                if (item.isPopular) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AccentGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "POPULAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentGold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Title
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Description
            Text(
                text = item.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Impact tag
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = item.gradient.first().copy(alpha = 0.1f)
            ) {
                Text(
                    text = item.impactTag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = item.gradient.first(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Price and action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${item.price}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
                
                Surface(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryPurple
                ) {
                    Text(
                        text = "Activar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RendshopFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            color = Surface,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        Text(
            text = "¿Necesitas ayuda?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Contáctanos para consultas sobre herramientas Pro",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            onClick = { },
            shape = RoundedCornerShape(12.dp),
            color = Surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Support,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Centro de Soporte",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Rendshop © 2024 · Todos los derechos reservados",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ItemDetailModal(
    item: ShopItem,
    userCredits: Int,
    onDismiss: () -> Unit,
    onRedeem: (ShopItem) -> Unit,
    onBuy: (ShopItem) -> Unit
) {
    val canRedeem = userCredits >= item.price
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(item.gradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = item.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Impact tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = item.gradient.first().copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = item.gradient.first(),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.impactTag,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = item.gradient.first()
                        )
                    }
                }
                
                if (item.isPopular) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentGold.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Herramienta popular",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentGold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider(color = Surface)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Price section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Precio",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stars,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "${item.price}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "créditos",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    }
                    
                    Text(
                        text = "o \$${String.format("%.2f", item.price * 0.01)} USD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGreen
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Redeem button
                    OutlinedButton(
                        onClick = { onRedeem(item) },
                        enabled = canRedeem,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (canRedeem) PrimaryPurple else TextMuted
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Canjear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Buy button
                    Button(
                        onClick = { onBuy(item) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Comprar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                
                if (!canRedeem) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Necesitas ${item.price - userCredits} créditos más para canjear",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProBenefitsModal(onDismiss: () -> Unit) {
    val benefits = listOf(
        Triple(Icons.Filled.Verified, "Insignia Verificado", "Destaca como vendedor confiable"),
        Triple(Icons.Filled.TrendingUp, "Alcance Premium", "5x más visibilidad en búsquedas"),
        Triple(Icons.Filled.Analytics, "Estadísticas Pro", "Analytics detallado de tus ventas"),
        Triple(Icons.Filled.Support, "Soporte Prioritario", "Atención 24/7 exclusiva"),
        Triple(Icons.Filled.Bolt, "Impulsos Gratis", "3 impulsos mensuales incluidos"),
        Triple(Icons.Filled.Palette, "Personalización", "Temas exclusivos para tu tienda")
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(AccentGold, Color(0xFFFF6B35))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Pro Seller",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Beneficios exclusivos",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Benefits list
                benefits.forEach { (icon, title, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryPurple.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = description,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Pricing
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Plan Pro Mensual",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "\$9.99",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "/mes",
                                fontSize = 14.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // CTA Button
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text(
                        text = "Activar Pro Seller",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Cancela cuando quieras · Sin compromisos",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
