package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*
import com.rendly.app.data.repository.ExploreRepository
import kotlinx.coroutines.launch
import kotlin.math.abs

data class CategoryItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val badge: String? = null,
    val trending: Boolean = false,
    val itemCount: Int = 0
)

private val CATEGORY_DEFINITIONS = listOf(
    "Moda Mujer" to listOf(
        CategoryItem("vestidos", "Vestidos", Icons.Outlined.Favorite, listOf(Color(0xFF2E8B57), Color(0xFFF472B6))),
        CategoryItem("blusas", "Blusas y Tops", Icons.Outlined.ShoppingBag, listOf(Color(0xFFFF6B35), Color(0xFFC4B5FD))),
        CategoryItem("pantalones", "Pantalones", Icons.Outlined.Checkroom, listOf(Color(0xFF1565A0), Color(0xFF93C5FD))),
        CategoryItem("faldas", "Faldas", Icons.Outlined.Woman, listOf(Color(0xFFF472B6), Color(0xFFFDA4AF))),
        CategoryItem("abrigos", "Abrigos y Chaquetas", Icons.Outlined.AcUnit, listOf(Color(0xFF6366F1), Color(0xFFA5B4FC))),
        CategoryItem("zapatos_m", "Zapatos", Icons.Outlined.RunCircle, listOf(Color(0xFFFF6B35), Color(0xFFFCD34D)))
    ),
    "Moda Hombre" to listOf(
        CategoryItem("camisas", "Camisas", Icons.Outlined.PersonOutline, listOf(Color(0xFF1565A0), Color(0xFF93C5FD))),
        CategoryItem("pantalones_h", "Pantalones", Icons.Outlined.Checkroom, listOf(Color(0xFF6366F1), Color(0xFFA5B4FC))),
        CategoryItem("chaquetas", "Chaquetas", Icons.Outlined.AcUnit, listOf(Color(0xFF2E8B57), Color(0xFF6EE7B7))),
        CategoryItem("trajes", "Trajes", Icons.Outlined.BusinessCenter, listOf(Color(0xFFFF6B35), Color(0xFFC4B5FD))),
        CategoryItem("zapatos_h", "Zapatos", Icons.Outlined.RunCircle, listOf(Color(0xFFFF6B35), Color(0xFFFCD34D)))
    ),
    "Accesorios" to listOf(
        CategoryItem("bolsos", "Bolsos y Carteras", Icons.Outlined.ShoppingBag, listOf(Color(0xFF2E8B57), Color(0xFFF9A8D4))),
        CategoryItem("joyeria", "Joyería", Icons.Outlined.Diamond, listOf(Color(0xFFFF6B35), Color(0xFFFDE68A))),
        CategoryItem("relojes", "Relojes", Icons.Outlined.Watch, listOf(Color(0xFF6366F1), Color(0xFFA5B4FC))),
        CategoryItem("gafas", "Gafas de Sol", Icons.Outlined.Visibility, listOf(Color(0xFF0EA5E9), Color(0xFF7DD3FC))),
        CategoryItem("sombreros", "Sombreros", Icons.Outlined.Face, listOf(Color(0xFF2E8B57), Color(0xFF6EE7B7)))
    ),
    "Hogar y Deco" to listOf(
        CategoryItem("muebles", "Muebles", Icons.Outlined.Chair, listOf(Color(0xFFFF6B35), Color(0xFFC4B5FD))),
        CategoryItem("decoracion", "Decoración", Icons.Outlined.Palette, listOf(Color(0xFF2E8B57), Color(0xFFF9A8D4))),
        CategoryItem("plantas", "Plantas", Icons.Outlined.Spa, listOf(Color(0xFF2E8B57), Color(0xFF6EE7B7))),
        CategoryItem("iluminacion", "iluminación", Icons.Outlined.LightMode, listOf(Color(0xFFFF6B35), Color(0xFFFDE68A)))
    ),
    "Electrónica" to listOf(
        CategoryItem("smartphones", "Smartphones", Icons.Outlined.PhoneAndroid, listOf(Color(0xFF1565A0), Color(0xFF93C5FD))),
        CategoryItem("laptops", "Laptops", Icons.Outlined.Laptop, listOf(Color(0xFF6366F1), Color(0xFFA5B4FC))),
        CategoryItem("audio", "Audio", Icons.Outlined.Headphones, listOf(Color(0xFF2E8B57), Color(0xFFF9A8D4))),
        CategoryItem("gaming", "Gaming", Icons.Outlined.SportsEsports, listOf(Color(0xFF2E8B57), Color(0xFF6EE7B7)))
    )
)

/**
 * Returns category sections with real item counts from the database
 */
@Composable
fun rememberCategorySections(): List<Pair<String, List<CategoryItem>>> {
    val exploreItems by ExploreRepository.exploreItems.collectAsState()
    
    return remember(exploreItems) {
        val counts = ExploreRepository.getCategoryCounts()
        val totalItems = exploreItems.size
        
        CATEGORY_DEFINITIONS.map { (sectionName, categories) ->
            sectionName to categories.map { cat ->
                val count = counts[cat.id] ?: 0
                val isTrending = count > 0 && count >= (totalItems * 0.1) // 10%+ of items
                val badge = when {
                    count >= (totalItems * 0.2) -> "HOT"
                    count > 0 && categories.indexOf(cat) == 0 -> "NEW"
                    else -> null
                }
                cat.copy(
                    itemCount = count,
                    trending = isTrending,
                    badge = badge
                )
            }
        }
    }
}

@Composable
fun CategoryDrawer(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onHelpCenter: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onTermsAndConditions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    
    // Get real category data from repository
    val categorySections = rememberCategorySections()
    
    // Load explore items when drawer becomes visible
    LaunchedEffect(isVisible) {
        if (isVisible) {
            ExploreRepository.loadExploreItems()
        }
    }
    
    // LazyListState para detectar scroll
    val listState = rememberLazyListState()
    
    // animación del drawer - usar key para resetear en cada apertura
    val offsetX = remember { Animatable(-1f) }
    val velocityTracker = remember { VelocityTracker() }
    val backdropAlpha = (1f + offsetX.value).coerceIn(0f, 1f)
    
    // Efecto para animar cuando cambia isVisible - fluido desde el primer toque
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Asegurar que empiece desde -1f para animación fluida
            if (offsetX.value == -1f) {
                offsetX.snapTo(-1f)
            }
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = tween(280, easing = FastOutSlowInEasing)
            )
        } else {
            offsetX.animateTo(
                targetValue = -1f,
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            )
        }
    }
    
    if (isVisible || offsetX.value > -1f) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Backdrop mejorado
            if (backdropAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = backdropAlpha * 0.6f }
                        .background(Color.Black)
                        .clickable { onDismiss() }
                )
            }
            
            // Drawer con gesto de arrastre
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX.value * size.width
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                if (dragAmount < 0) {
                                    velocityTracker.addPosition(
                                        change.uptimeMillis,
                                        Offset(change.position.x, change.position.y)
                                    )
                                    scope.launch {
                                        offsetX.stop()
                                        val newValue = (offsetX.value + dragAmount / size.width).coerceIn(-1f, 0f)
                                        offsetX.snapTo(newValue)
                                    }
                                }
                            },
                            onDragEnd = {
                                val velocity = velocityTracker.calculateVelocity().x
                                val shouldDismiss = velocity < -1200f || offsetX.value < -0.5f
                                scope.launch {
                                    if (shouldDismiss) {
                                        offsetX.animateTo(-1f, tween(200, easing = FastOutSlowInEasing))
                                        onDismiss()
                                    } else {
                                        offsetX.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                                    }
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    offsetX.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                                }
                            }
                        )
                    }
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.90f),
                    color = HomeBg,
                    shadowElevation = 24.dp,
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        // Header premium con gradiente y branding
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            PrimaryPurple.copy(alpha = 0.08f),
                                            HomeBg
                                        )
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Logo mini
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(PrimaryPurple, AccentPink)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "R",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "Explorar",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                    
                                    Surface(
                                        onClick = onDismiss,
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = Surface
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cerrar",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                
                        // Search bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Surface,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Buscar categorías...",
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                
                        // Divider sutil
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Categories list
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            categorySections.forEach { (sectionName, categories) ->
                                item(key = "section_$sectionName") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = sectionName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${categories.size}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted
                                        )
                                    }
                                }
                                
                                items(categories, key = { it.id }) { category ->
                                    CategoryRowPremium(
                                        category = category,
                                        onClick = {
                                            onCategorySelected(category.id)
                                            onDismiss()
                                        }
                                    )
                                }
                                
                                item(key = "spacer_$sectionName") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                    
                            // Footer mejorado
                            item(key = "footer_divider") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp)
                                        .height(1.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.White.copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                            
                            item(key = "footer_links") {
                                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                    FooterLink("Ayuda y Soporte") { onHelpCenter(); onDismiss() }
                                    FooterLink("política de Privacidad") { onPrivacyPolicy(); onDismiss() }
                                    FooterLink("términos y Condiciones") { onTermsAndConditions(); onDismiss() }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Text(
                                        text = "Merqora © 2024",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted.copy(alpha = 0.6f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRowPremium(
    category: CategoryItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono circular con gradiente
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                category.gradient[0],
                                category.gradient[1]
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    // Badge inline
                    category.badge?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (badge) {
                                "HOT" -> AccentPink
                                "NEW" -> AccentGreen
                                "PREMIUM" -> Color(0xFFFF6B35)
                                else -> PrimaryPurple
                            }
                        ) {
                            Text(
                                text = badge,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (category.itemCount > 0) {
                        Text(
                            text = "${formatItemCount(category.itemCount)} productos",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                    if (category.trending) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(TextMuted.copy(alpha = 0.5f))
                            )
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Trending",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        }
                    }
                }
            }
            
            // Arrow con Círculo sutil
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatItemCount(count: Int): String {
    return when {
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
}

@Composable
private fun FooterLink(text: String, onClick: () -> Unit = {}) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = TextMuted,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    )
}
