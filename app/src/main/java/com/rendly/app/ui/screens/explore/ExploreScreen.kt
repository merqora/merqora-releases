package com.rendly.app.ui.screens.explore

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.rendly.app.data.repository.ExploreItem
import com.rendly.app.data.repository.ExploreRepository
import com.rendly.app.data.repository.OffersRepository
import com.rendly.app.data.repository.OfferCampaign
import com.rendly.app.data.repository.OfferProduct
import com.rendly.app.data.repository.ZoneRepository
import com.rendly.app.data.repository.ZoneLocationState
import com.rendly.app.data.repository.PopularSearch
import com.rendly.app.data.repository.RecentSearch
import com.rendly.app.data.model.Post
import com.rendly.app.ui.components.ProductPage
import com.rendly.app.ui.components.BottomNavBar
import com.rendly.app.ui.components.ExploreScreenSkeleton
import com.rendly.app.ui.components.ProductCardSkeleton
import com.rendly.app.ui.components.SectionHeaderSkeleton
import com.rendly.app.ui.components.CarouselSkeleton
import com.rendly.app.ui.components.UnifiedProductCard
import com.rendly.app.ui.components.toProductCardData
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

// Quick Actions
private data class QuickAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private val QUICK_ACTIONS = listOf(
    QuickAction("live", "En Vivo", Icons.Filled.Videocam, Color(0xFFEF4444)),
    QuickAction("offers", "Ofertas", Icons.Outlined.LocalOffer, AccentYellow),
    QuickAction("ranking", "Ranking", Icons.Outlined.EmojiEvents, Color(0xFFFFD700)),
    QuickAction("mysize", "Mi Talla", Icons.Outlined.Straighten, PrimaryPurple),
    QuickAction("zones", "Zonas", Icons.Outlined.LocationOn, AccentBlue)
)

// categorías
private data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector
)

private val CATEGORIES = listOf(
    Category("ropa", "Ropa", Icons.Outlined.Checkroom),
    Category("zapatos", "Zapatos", Icons.Outlined.Hiking),
    Category("accesorios", "Accesorios", Icons.Outlined.Watch),
    Category("electronica", "Electrúnica", Icons.Outlined.Devices),
    Category("hogar", "Hogar", Icons.Outlined.Home),
    Category("deportes", "Deportes", Icons.Outlined.FitnessCenter)
)

// Helper function a nivel de archivo para convertir ExploreItem a Post
private fun exploreItemToPost(item: ExploreItem): Post {
    return Post(
        id = item.id,
        userId = item.userId,
        username = item.username,
        userAvatar = item.userAvatar,
        userStoreName = item.storeName,
        title = item.title,
        description = null,
        images = item.images,
        price = item.price,
        condition = "",
        category = item.category,
        likesCount = item.likesCount,
        reviewsCount = item.reviewsCount,
        createdAt = "",
        isLiked = false,
        isSaved = false
    )
}

@Composable
fun ExploreScreen(
    onNavigateToLiveStreams: () -> Unit = {},
    onSubScreenVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    // NavBar embebido
    showNavBar: Boolean = true,
    currentNavRoute: String = "explore",
    onNavNavigate: (String) -> Unit = {},
    onNavHomeReclick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Estado para pantalla de "próximamente" de En Vivo
    var showLiveComingSoon by remember { mutableStateOf(false) }
    
    // Estado para pantalla de Ofertas
    var showOffersScreen by remember { mutableStateOf(false) }
    
    // Estado para pantalla de Ranking
    var showRankingScreen by remember { mutableStateOf(false) }
    
    // Estado para pantalla de Mi Talle
    var showMySizeScreen by remember { mutableStateOf(false) }
    
    // Estado para pantalla de Zonas
    var showZonesScreen by remember { mutableStateOf(false) }
    
    // Estado para pantalla de búsqueda/Ecommerce
    var showSearchScreen by remember { mutableStateOf(false) }
    var searchScreenQuery by remember { mutableStateOf("") }
    
    // Notificar visibilidad de sub-pantallas al padre
    val anySubScreenVisible = showLiveComingSoon || showOffersScreen || showRankingScreen || showMySizeScreen || showZonesScreen || showSearchScreen
    LaunchedEffect(anySubScreenVisible) {
        onSubScreenVisibilityChange(anySubScreenVisible)
    }
    
    // ProductPage state
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showProductPage by remember { mutableStateOf(false) }
    
    // Use cached data from ExploreRepository
    val exploreItems by ExploreRepository.exploreItems.collectAsState()
    val isLoading by ExploreRepository.isLoading.collectAsState()
    val listState = rememberLazyListState()
    
    // Load data only if not already loaded (caching)
    LaunchedEffect(Unit) {
        ExploreRepository.loadExploreItems(forceRefresh = false)
    }
    
    // Filtrar por Búsqueda y Categoría
    val filteredItems = remember(searchQuery, selectedCategory, exploreItems) {
        exploreItems.filter { item ->
            val matchesSearch = searchQuery.isEmpty() || 
                item.title.lowercase().contains(searchQuery.lowercase()) ||
                item.username.lowercase().contains(searchQuery.lowercase())
            
            val matchesCategory = selectedCategory == null || 
                item.category.contains(selectedCategory!!, ignoreCase = true)
            
            matchesSearch && matchesCategory
        }
    }
    
    // Separar productos para carruseles
    val discoverItems = filteredItems.take(10)
    val popularItems = filteredItems.filter { it.reviewsCount > 0 }.take(8)
    val gridItems = filteredItems
    
    Box(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
        // Search Bar
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { query ->
                    searchScreenQuery = query
                    showSearchScreen = true
                }
            )
        }
        
        // Quick Actions
        item {
            QuickActionsRow(
                onLiveClick = { showLiveComingSoon = true },
                onOffersClick = { showOffersScreen = true },
                onRankingClick = { showRankingScreen = true },
                onMySizeClick = { showMySizeScreen = true },
                onZonesClick = { showZonesScreen = true }
            )
        }
        
        // categorías Específicas
        item {
            CategoriesRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { 
                    selectedCategory = if (selectedCategory == it) null else it 
                }
            )
        }
        
        // Sección: Descubre lo que tenemos para ti
        if (discoverItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Descubre lo que tenemos para ti",
                    icon = Icons.Outlined.Explore
                )
            }
            
            item {
                ItemCarousel(
                    items = discoverItems,
                    onItemClick = { item ->
                        selectedPost = exploreItemToPost(item)
                        showProductPage = true
                    }
                )
            }
        }
        
        // Sección: Populares
        if (popularItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "más populares",
                    icon = Icons.Outlined.TrendingUp,
                    showSeeAll = false
                )
            }
            
            item {
                ItemCarousel(
                    items = popularItems,
                    onItemClick = { item ->
                        selectedPost = exploreItemToPost(item)
                        showProductPage = true
                    }
                )
            }
        }
        
        // Sección: Todas las publicaciones
        item {
            SectionHeader(
                title = "Explora todo",
                icon = Icons.Outlined.GridView,
                showSeeAll = true,
                seeAllText = "Ver Más",
                onSeeAllClick = {
                    searchScreenQuery = ""
                    showSearchScreen = true
                }
            )
        }
        
        // Grid de productos (2 columnas simuladas con filas)
        val chunkedItems = gridItems.chunked(2)
        items(chunkedItems) { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f).height(355.dp)) {
                        UnifiedProductCard(
                            data = item.toProductCardData(),
                            onClick = {
                                selectedPost = exploreItemToPost(item)
                                showProductPage = true
                            },
                            imageHeight = 170.dp
                        )
                    }
                }
                // Si solo hay 1 producto en la fila, agregar espacio vacío
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Skeleton loading - se muestra mientras no hay datos
        if (isLoading && exploreItems.isEmpty()) {
            item {
                SectionHeaderSkeleton()
            }
            item {
                CarouselSkeleton(itemCount = 3)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeaderSkeleton()
            }
            // Grid skeleton
            items(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ProductCardSkeleton()
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ProductCardSkeleton()
                    }
                }
            }
        }
    }
    
    // NavBar embebido - ANTES de todos los modales para que queden SOBRE él
    if (showNavBar) {
        BottomNavBar(
            currentRoute = currentNavRoute,
            onNavigate = onNavNavigate,
            onHomeReclick = onNavHomeReclick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    // ProductPage overlay
    ProductPage(
        post = selectedPost,
        isVisible = showProductPage,
        onDismiss = {
            showProductPage = false
            selectedPost = null
        }
    )
    
    // Pantalla de "próximamente" para En Vivo (overlay completo)
    if (showLiveComingSoon) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
        ) {
            LiveComingSoonScreen(
                onBack = { showLiveComingSoon = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Pantalla de Ofertas
    if (showOffersScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
        ) {
            OffersScreen(
                onBack = { showOffersScreen = false },
                onProductClick = { item ->
                    selectedPost = exploreItemToPost(item)
                    showProductPage = true
                    showOffersScreen = false
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Pantalla de Ranking
    if (showRankingScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
        ) {
            RankingScreen(
                onBack = { showRankingScreen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Pantalla de Mi Talle
    if (showMySizeScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
        ) {
            MySizeScreen(
                onBack = { showMySizeScreen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Pantalla de Zonas
    if (showZonesScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
        ) {
            ZonesScreen(
                onBack = { showZonesScreen = false },
                onSearch = { query ->
                    searchScreenQuery = query
                    showSearchScreen = true
                    showZonesScreen = false
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Pantalla de búsqueda/Ecommerce
    if (showSearchScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBg)
        ) {
            SearchResultsScreen(
                initialQuery = searchScreenQuery,
                onBack = { showSearchScreen = false },
                onProductClick = { post ->
                    selectedPost = post
                    showProductPage = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    } // End Box
}

// ---------------------------------------------------------------
// COMPONENTES DE UI
// ---------------------------------------------------------------

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Buscar productos, tiendas...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    cursorBrush = if (isFocused) SolidColor(PrimaryPurple) else SolidColor(Color.Transparent),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = {
                            if (query.isNotBlank()) {
                                onSearch(query)
                            }
                        }
                    )
                )
            }
            
            // Botón de búsqueda visible cuando hay texto
            androidx.compose.animation.AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                IconButton(
                    onClick = { if (query.isNotBlank()) onSearch(query) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onLiveClick: () -> Unit = {},
    onOffersClick: () -> Unit = {},
    onRankingClick: () -> Unit = {},
    onMySizeClick: () -> Unit = {},
    onZonesClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QUICK_ACTIONS.forEach { action ->
            // Determinar si esta Sección está bloqueada
            val isLocked = action.id == "live" || action.id == "ranking"
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { 
                    when (action.id) {
                        "live" -> onLiveClick()
                        "offers" -> onOffersClick()
                        "ranking" -> onRankingClick()
                        "mysize" -> onMySizeClick()
                        "zones" -> onZonesClick()
                    }
                }
            ) {
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // círculo de fondo con opacidad reducida si está bloqueado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(action.color.copy(alpha = if (isLocked) 0.08f else 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            tint = action.color.copy(alpha = if (isLocked) 0.5f else 1f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Overlay de candado para secciones bloqueadas
                    if (isLocked) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Surface)
                                .border(1.dp, HomeBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "próximamente",
                                tint = TextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = action.label,
                    fontSize = 12.sp,
                    color = if (isLocked) TextMuted else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CategoriesRow(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CATEGORIES) { category ->
            val isSelected = selectedCategory == category.id
            
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) PrimaryPurple else Surface,
                label = "categoryBg"
            )
            
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category.id) },
                shape = RoundedCornerShape(20.dp),
                color = backgroundColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = category.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    showSeeAll: Boolean = false,
    seeAllText: String = "Ver todo",
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        if (showSeeAll) {
            Text(
                text = seeAllText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryPurple,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

@Composable
private fun ItemCarousel(
    items: List<ExploreItem>,
    onItemClick: (ExploreItem) -> Unit = {}
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.id }) { item ->
            Box(modifier = Modifier.width(170.dp).height(355.dp)) {
                UnifiedProductCard(
                    data = item.toProductCardData(),
                    onClick = { onItemClick(item) },
                    imageHeight = 170.dp
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE OFERTAS - Dinámica con Supabase
// ---------------------------------------------------------------

private fun campaignIcon(slug: String): ImageVector = when (slug) {
    "flash" -> Icons.Filled.FlashOn
    "today" -> Icons.Filled.Today
    "week" -> Icons.Outlined.DateRange
    "clearance" -> Icons.Filled.LocalFireDepartment
    else -> Icons.Outlined.LocalOffer
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFFFF6B35)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OffersScreen(
    onBack: () -> Unit,
    onProductClick: (ExploreItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val campaigns by OffersRepository.campaigns.collectAsState()
    val selectedCampaign by OffersRepository.selectedCampaign.collectAsState()
    val isLoading by OffersRepository.isLoading.collectAsState()
    val listState = rememberLazyListState()

    // Cargar ofertas desde Supabase
    LaunchedEffect(Unit) {
        OffersRepository.loadOffers(forceRefresh = false)
    }

    // Productos de la campaña seleccionada
    val currentProducts = selectedCampaign?.items ?: emptyList()
    val featuredProducts = currentProducts.filter { it.isFeatured }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Header
        OffersHeader(
            onBack = onBack,
            totalOffers = campaigns.sumOf { it.items.size }
        )

        if (isLoading && campaigns.isEmpty()) {
            // Loading skeleton
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentYellow)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando ofertas...", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else if (campaigns.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.LocalOffer,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay ofertas activas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Vuelve pronto para nuevos descuentos", fontSize = 14.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Banner dinámico
                item(key = "banner") {
                    selectedCampaign?.let { campaign ->
                        OffersBannerDynamic(campaign = campaign)
                    }
                }

                // Carrusel de destacados (si hay)
                if (featuredProducts.isNotEmpty()) {
                    item(key = "featured_header") {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Star, null, tint = AccentYellow, modifier = Modifier.size(20.dp))
                            Text("Destacados", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    item(key = "featured_carousel") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(featuredProducts.size) { index ->
                                val product = featuredProducts[index]
                                Box(modifier = Modifier.width(170.dp).height(355.dp)) {
                                    UnifiedProductCard(
                                        data = product.exploreItem.toProductCardData().copy(
                                            originalPrice = product.originalPrice,
                                            currentPrice = product.offerPrice
                                        ),
                                        onClick = { onProductClick(product.exploreItem) },
                                        imageHeight = 170.dp
                                    )
                                }
                            }
                        }
                    }
                }

                // Categorías STICKY
                stickyHeader(key = "categories") {
                    OfferCampaignTabs(
                        campaigns = campaigns,
                        selectedCampaign = selectedCampaign,
                        onCampaignSelected = { OffersRepository.selectCampaign(it) }
                    )
                }

                // Stats de la campaña
                item(key = "stats") {
                    selectedCampaign?.let { campaign ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Total productos
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.Inventory2, null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                    Text("${campaign.items.size} productos", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                }
                            }
                            // Descuento máximo
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF6B35).copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.TrendingDown, null, tint = Color(0xFFFF6B35), modifier = Modifier.size(16.dp))
                                    Text("Hasta -${campaign.maxDiscount}%", fontSize = 12.sp, color = Color(0xFFFF6B35), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Grid de productos 2 columnas
                val chunkedProducts = currentProducts.chunked(2)
                items(chunkedProducts.size) { rowIndex ->
                    val rowItems = chunkedProducts[rowIndex]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(top = if (rowIndex == 0) 4.dp else 0.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { product ->
                            Box(modifier = Modifier.weight(1f).height(355.dp)) {
                                UnifiedProductCard(
                                    data = product.exploreItem.toProductCardData().copy(
                                        originalPrice = product.originalPrice,
                                        currentPrice = product.offerPrice
                                    ),
                                    onClick = { onProductClick(product.exploreItem) },
                                    imageHeight = 160.dp
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Empty products state
                if (currentProducts.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.SearchOff, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Sin productos en esta campaña", fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun OffersHeader(onBack: () -> Unit, totalOffers: Int = 0) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalOffer,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ofertas Especiales",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (totalOffers > 0) "$totalOffers productos en oferta" else "Los mejores descuentos para ti",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFF6B35).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HOT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B35)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun OffersBannerDynamic(campaign: OfferCampaign) {
    val gradStart = parseColor(campaign.bannerGradientStart)
    val gradEnd = parseColor(campaign.bannerGradientEnd)
    val bannerBrush = Brush.horizontalGradient(listOf(gradStart, gradEnd))
    val icon = campaignIcon(campaign.slug)

    // Countdown real
    var remainingSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(campaign.endsAt) {
        if (campaign.endsAt.isNotBlank()) {
            try {
                val endInstant = java.time.Instant.parse(campaign.endsAt)
                while (true) {
                    val now = java.time.Instant.now()
                    val diff = java.time.Duration.between(now, endInstant).seconds
                    remainingSeconds = if (diff > 0) diff else 0
                    kotlinx.coroutines.delay(1000L)
                }
            } catch (_: Exception) {
                remainingSeconds = 0
            }
        }
    }

    val hours = (remainingSeconds / 3600).toString().padStart(2, '0')
    val minutes = ((remainingSeconds % 3600) / 60).toString().padStart(2, '0')
    val seconds = (remainingSeconds % 60).toString().padStart(2, '0')

    val shortName = when (campaign.slug) {
        "week" -> "SEMANA"
        "clearance" -> "LIQUIDA"
        else -> campaign.name.uppercase()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bannerBrush)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = shortName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hasta ${campaign.maxDiscount}% OFF",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                campaign.description?.let { desc ->
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Countdown real
            if (remainingSeconds > 0) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(min = 110.dp)
                ) {
                    Text(
                        text = "Termina en",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf(hours, minutes, seconds).forEach { time ->
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = Color.Black.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = time,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferCampaignTabs(
    campaigns: List<OfferCampaign>,
    selectedCampaign: OfferCampaign?,
    onCampaignSelected: (OfferCampaign) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeBg),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(campaigns.size) { index ->
            val campaign = campaigns[index]
            val isSelected = selectedCampaign?.id == campaign.id
            val icon = campaignIcon(campaign.slug)

            Surface(
                modifier = Modifier.clickable { onCampaignSelected(campaign) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) AccentYellow else Surface,
                shadowElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = campaign.name,
                        tint = if (isSelected) Color.Black else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = campaign.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.Black else TextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) Color.Black.copy(alpha = 0.2f) else Color(0xFFFF6B35).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "-${campaign.maxDiscount}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color(0xFFFF6B35),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    // Items count badge
                    if (campaign.items.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.Black.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${campaign.items.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE RANKING - Top usuarios por Categorías
// ---------------------------------------------------------------

private data class RankingCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

private val RANKING_CATEGORIES = listOf(
    RankingCategory("sellers", "Mejores Vendedores", "Los que Más venden", Icons.Outlined.Storefront, Color(0xFF22C55E)),
    RankingCategory("buyers", "más Compradores", "Los que Más compran", Icons.Outlined.ShoppingCart, Color(0xFF0A3D62)),
    RankingCategory("posts", "más Publicaciones", "Los Más activos", Icons.Outlined.GridView, Color(0xFFFF6B35)),
    RankingCategory("likes", "más Likes", "Los Más populares", Icons.Outlined.Favorite, Color(0xFF2E8B57)),
    RankingCategory("rends", "más Rends", "Creadores de video", Icons.Outlined.PlayCircle, Color(0xFFEF4444))
)

private data class RankedUser(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val storeName: String?,
    val count: Int,
    val isVerified: Boolean,
    val reputation: Int
)

@Composable
private fun RankingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación del anillo giratorio (como LiveComingSoonScreen)
    val infiniteTransition = rememberInfiniteTransition(label = "rankingComingSoon")
    
    val rotateRing by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateRing"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Fondo decorativo con gradiente dorado sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.08f),
                            Color(0xFFFFA500).copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top bar con Botón de volver
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Surface.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                
                // Badge PróximAMENTE
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "PróximAMENTE",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.size(40.dp))
            }
            
            // Contenido central
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    // Icono principal con borde animado
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                    ) {
                        // Anillo exterior rotando
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .rotate(rotateRing)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700).copy(alpha = 0f),
                                            Color(0xFFFFD700).copy(alpha = 0.9f),
                                            Color(0xFFFFA500).copy(alpha = 0.9f),
                                            Color(0xFFFFD700).copy(alpha = 0f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // círculo principal dorado
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFFFA500)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // título principal
                    Text(
                        text = "¡El Ranking está llegando!",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // SubTítulo emotivo
                    Text(
                        text = "Prepárate para descubrir quiénes son\nlos mejores vendedores, compradores\ny creadores de contenido en Merqora.",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // categorías preview
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Surface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Próximas Categorías",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                RankingPreviewCategoryIcon(Icons.Outlined.Storefront, Color(0xFF22C55E), "Top Vendedores")
                                RankingPreviewCategoryIcon(Icons.Outlined.ShoppingCart, Color(0xFF0A3D62), "Top Compradores")
                                RankingPreviewCategoryIcon(Icons.Outlined.GridView, Color(0xFFFF6B35), "más Publicados")
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                RankingPreviewCategoryIcon(Icons.Outlined.Favorite, Color(0xFF2E8B57), "más Likes")
                                RankingPreviewCategoryIcon(Icons.Outlined.PlayCircle, Color(0xFFEF4444), "más Rends")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mensaje de expectativa
                    Text(
                        text = "¡Serás tú el Próximo #1?",
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingPreviewCategoryIcon(
    icon: ImageVector,
    color: Color,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

private suspend fun loadTopSellers(): List<RankedUser> {
    return try {
        val users = com.rendly.app.data.remote.SupabaseClient.database
            .from("usuarios")
            .select()
            .decodeList<com.rendly.app.data.repository.ExploreUserProfile>()
            .sortedByDescending { it.clientes }
            .take(20)
        
        users.map { user ->
            RankedUser(
                userId = user.userId,
                username = user.username,
                avatarUrl = user.avatarUrl,
                storeName = user.nombreTienda,
                count = user.clientes,
                isVerified = user.isVerified,
                reputation = user.reputacion
            )
        }
    } catch (e: Exception) { emptyList() }
}

private suspend fun loadTopBuyers(): List<RankedUser> {
    return try {
        val users = com.rendly.app.data.remote.SupabaseClient.database
            .from("usuarios")
            .select()
            .decodeList<com.rendly.app.data.repository.ExploreUserProfile>()
            .sortedByDescending { it.clientes }
            .take(20)
        
        users.map { user ->
            RankedUser(
                userId = user.userId,
                username = user.username,
                avatarUrl = user.avatarUrl,
                storeName = user.nombreTienda,
                count = user.clientes,
                isVerified = user.isVerified,
                reputation = user.reputacion
            )
        }
    } catch (e: Exception) { emptyList() }
}

private suspend fun loadTopPosters(): List<RankedUser> {
    return try {
        val users = com.rendly.app.data.remote.SupabaseClient.database
            .from("usuarios")
            .select()
            .decodeList<com.rendly.app.data.repository.ExploreUserProfile>()
            .sortedByDescending { it.publicaciones }
            .take(20)
        
        users.map { user ->
            RankedUser(
                userId = user.userId,
                username = user.username,
                avatarUrl = user.avatarUrl,
                storeName = user.nombreTienda,
                count = user.publicaciones,
                isVerified = user.isVerified,
                reputation = user.reputacion
            )
        }
    } catch (e: Exception) { emptyList() }
}

private suspend fun loadTopLiked(): List<RankedUser> {
    return try {
        val users = com.rendly.app.data.remote.SupabaseClient.database
            .from("usuarios")
            .select()
            .decodeList<com.rendly.app.data.repository.ExploreUserProfile>()
            .sortedByDescending { it.seguidores }
            .take(20)
        
        users.map { user ->
            RankedUser(
                userId = user.userId,
                username = user.username,
                avatarUrl = user.avatarUrl,
                storeName = user.nombreTienda,
                count = user.seguidores,
                isVerified = user.isVerified,
                reputation = user.reputacion
            )
        }
    } catch (e: Exception) { emptyList() }
}

private suspend fun loadTopRenders(): List<RankedUser> {
    return try {
        val users = com.rendly.app.data.remote.SupabaseClient.database
            .from("usuarios")
            .select()
            .decodeList<com.rendly.app.data.repository.ExploreUserProfile>()
            .sortedByDescending { it.publicaciones }
            .take(20)
        
        users.map { user ->
            RankedUser(
                userId = user.userId,
                username = user.username,
                avatarUrl = user.avatarUrl,
                storeName = user.nombreTienda,
                count = user.publicaciones,
                isVerified = user.isVerified,
                reputation = user.reputacion
            )
        }
    } catch (e: Exception) { emptyList() }
}

@Composable
private fun RankingHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ranking",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Los mejores de Merqora",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingCategoryTabs(
    categories: List<RankingCategory>,
    selectedCategory: RankingCategory,
    onCategorySelected: (RankingCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeBg),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory.id == category.id
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) category.color else Surface,
                border = if (!isSelected) BorderStroke(1.dp, category.color.copy(alpha = 0.3f)) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else category.color,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = category.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TopThreeSection(
    users: List<RankedUser>,
    category: RankingCategory
) {
    if (users.isEmpty()) return
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Top 3 ${category.title}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Segundo lugar (izquierda)
            if (users.size > 1) {
                TopUserPodium(
                    user = users[1],
                    position = 2,
                    color = Color(0xFFC0C0C0), // Plata
                    height = 80.dp
                )
            }
            
            // Primer lugar (centro, Más alto)
            if (users.isNotEmpty()) {
                TopUserPodium(
                    user = users[0],
                    position = 1,
                    color = Color(0xFFFFD700), // Oro
                    height = 100.dp
                )
            }
            
            // Tercer lugar (derecha)
            if (users.size > 2) {
                TopUserPodium(
                    user = users[2],
                    position = 3,
                    color = Color(0xFFCD7F32), // Bronce
                    height = 60.dp
                )
            }
        }
    }
}

@Composable
private fun TopUserPodium(
    user: RankedUser,
    position: Int,
    color: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(if (position == 1) 72.dp else 60.dp)
                .border(3.dp, color, CircleShape)
                .padding(3.dp)
        ) {
            AsyncImage(
                model = user.avatarUrl ?: "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff",
                contentDescription = user.username,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Username
        Text(
            text = "@${user.username}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Count
        Text(
            text = "${user.count}",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Podium
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(height)
                .background(
                    color.copy(alpha = 0.2f),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(32.dp)
                    .background(color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$position",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun RankingUserCard(
    position: Int,
    user: RankedUser,
    category: RankingCategory
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición
            Text(
                text = "#$position",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.width(40.dp)
            )
            
            // Avatar
            AsyncImage(
                model = user.avatarUrl ?: "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff",
                contentDescription = user.username,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${user.username}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verificado",
                            tint = Color(0xFF0A3D62),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                user.storeName?.let { store ->
                    Text(
                        text = store,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Count
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${user.count}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
                Text(
                    text = "${user.reputation}%",
                    fontSize = 11.sp,
                    color = AccentGreen
                )
            }
        }
    }
}

@Composable
private fun EmptyRankingState(category: RankingCategory) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sin datos Aún",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "aún no hay suficientes datos para mostrar el ranking",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------
// PANTALLA MI TALLE - Super profesional y creativa
// ---------------------------------------------------------------

private data class SizeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val sizes: List<String>
)

private val SIZE_CATEGORIES = listOf(
    SizeCategory("tops", "Partes de Arriba", "Camisetas, camisas, sweaters, chaquetas", Icons.Outlined.Checkroom, Color(0xFF0A3D62), listOf("XS", "S", "M", "L", "XL", "XXL", "XXXL")),
    SizeCategory("bottoms", "Partes de Abajo", "Pantalones, jeans, shorts, faldas", Icons.Outlined.Straighten, Color(0xFF2E8B57), listOf("24", "26", "28", "30", "32", "34", "36", "38", "40", "42")),
    SizeCategory("shoes", "Calzado", "Zapatos, zapatillas, botas, sandalias", Icons.Outlined.Hiking, Color(0xFFFF6B35), listOf("35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45")),
    SizeCategory("accessories", "Accesorios", "Sombreros, gorras, guantes, cinturones", Icons.Outlined.Watch, Color(0xFFFF6B35), listOf("XS", "S", "M", "L", "XL", "único")),
    SizeCategory("underwear", "Ropa Interior", "Boxers, calzoncillos, brasieres, medias", Icons.Outlined.Favorite, Color(0xFF2E8B57), listOf("XS", "S", "M", "L", "XL", "XXL"))
)

@Composable
private fun MySizeScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val selectedSizes = remember { mutableStateMapOf<String, String>() }
    val isComplete = selectedSizes.size >= 3
    
    Column(modifier = modifier.fillMaxSize().background(HomeBg)) {
        MySizeHeader(onBack = onBack, isComplete = isComplete)
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            item { MySizeBanner() }
            items(SIZE_CATEGORIES) { category ->
                SizeCategoryCard(category, selectedSizes[category.id]) { selectedSizes[category.id] = it }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SaveSizesButton(isComplete) { onBack() }
            }
        }
    }
}

@Composable
private fun MySizeHeader(onBack: () -> Unit, isComplete: Boolean) {
    Surface(modifier = Modifier.fillMaxWidth(), color = HomeBg, shadowElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary) }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(PrimaryPurple, AccentPink))),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Straighten, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Mi Talla", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Personaliza tu experiencia", fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (isComplete) {
                Surface(shape = RoundedCornerShape(12.dp), color = AccentGreen.copy(alpha = 0.15f)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Listo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun MySizeBanner() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
            .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(PrimaryPurple, Color(0xFF0A3D62), AccentPink)), shape = RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("¡Encuentra tu moda perfecta!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Ingresa tus tallas y descubre ropa que te quede perfecta. Nuestra IA te mostrará productos de TU talla primero.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(Icons.Filled.Speed to "más Rápido", Icons.Filled.Verified to "Tu talla", Icons.Filled.Favorite to "Personalizado").forEach { (icon, text) ->
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SizeCategoryCard(category: SizeCategory, selectedSize: String?, onSizeSelected: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(16.dp), color = Surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(category.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(category.icon, null, tint = category.color, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(category.subtitle, fontSize = 12.sp, color = TextSecondary)
                }
                if (selectedSize != null) {
                    Surface(shape = CircleShape, color = category.color) {
                        Text(selectedSize, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(category.sizes) { size ->
                    val isSelected = selectedSize == size
                    Surface(
                        modifier = Modifier.clickable { onSizeSelected(size) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) category.color else HomeBg,
                        border = if (!isSelected) BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f)) else null
                    ) {
                        Text(size, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else TextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveSizesButton(isComplete: Boolean, onSave: () -> Unit) {
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isComplete) PrimaryPurple else PrimaryPurple.copy(alpha = 0.5f))
    ) {
        Icon(if (isComplete) Icons.Filled.CheckCircle else Icons.Outlined.Save, null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isComplete) "¡Guardar y descubrir moda!" else "Selecciona al menos 3 Categorías", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------
// PANTALLA DE ZONAS - Búsqueda por ubicación FUNCIONAL
// ---------------------------------------------------------------

private data class ZoneCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

private val ZONE_CATEGORIES = listOf(
    ZoneCategory("nearby", "Cerca de ti", Icons.Filled.NearMe, Color(0xFF2E8B57), "Productos a menos de 5km"),
    ZoneCategory("city", "Tu Ciudad", Icons.Outlined.LocationCity, Color(0xFF0A3D62), "Todo en tu ciudad"),
    ZoneCategory("region", "Tu Región", Icons.Outlined.Map, Color(0xFFFF6B35), "Explora tu región"),
    ZoneCategory("national", "Nacional", Icons.Outlined.Public, Color(0xFFFF6B35), "Todo el país"),
    ZoneCategory("pickup", "Retiro en persona", Icons.Outlined.Handshake, Color(0xFF2E8B57), "Sin envío, retiras tú")
)

private data class SearchFilter(
    val id: String,
    val name: String,
    val icon: ImageVector
)

private val ZONE_FILTERS = listOf(
    SearchFilter("price", "Precio", Icons.Outlined.AttachMoney),
    SearchFilter("distance", "Distancia", Icons.Outlined.SocialDistance),
    SearchFilter("rating", "Valoración", Icons.Outlined.Star),
    SearchFilter("new", "Más nuevos", Icons.Outlined.NewReleases),
    SearchFilter("verified", "Verificados", Icons.Outlined.Verified)
)

@Composable
private fun ZonesScreen(
    onBack: () -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf("nearby") }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    var distanceRadius by remember { mutableStateOf(10f) }
    
    // ZoneRepository states
    val locationState by ZoneRepository.locationState.collectAsState()
    val zoneStats by ZoneRepository.zoneStats.collectAsState()
    val popularSearches by ZoneRepository.popularSearches.collectAsState()
    val recentSearches by ZoneRepository.recentSearches.collectAsState()
    
    // Permission launcher (reutiliza patrón de AddAddressModal)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch { ZoneRepository.detectLocation(context) }
        }
    }
    
    // Helper para ejecutar búsqueda
    fun executeSearch(query: String) {
        val finalQuery = ZoneRepository.buildSearchQuery(query, selectedZone, selectedFilters, distanceRadius)
        if (query.isNotBlank()) {
            ZoneRepository.addRecentSearch(query)
        }
        onSearch(finalQuery)
    }
    
    // Init: detectar ubicación automáticamente al abrir
    LaunchedEffect(Unit) {
        ZoneRepository.init(context)
        
        if (ZoneRepository.hasLocationPermission(context)) {
            ZoneRepository.detectLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        
        // Cargar búsquedas populares
        ZoneRepository.loadPopularSearches(locationState.city.takeIf { it.isNotBlank() })
    }
    
    // Recargar popular searches cuando se resuelve la ciudad
    LaunchedEffect(locationState.city) {
        if (locationState.city.isNotBlank()) {
            ZoneRepository.loadPopularSearches(locationState.city)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Header
        ZonesHeader(onBack = onBack, locationState = locationState)
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Banner de ubicación REAL
            item {
                ZonesLocationBanner(
                    locationState = locationState,
                    zoneStats = zoneStats
                )
            }
            
            // Buscador principal
            item {
                ZonesSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { executeSearch(searchQuery) }
                )
            }
            
            // Categorías de zona
            item {
                Text(
                    text = "¿Dónde quieres buscar?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            item {
                ZoneCategoriesGrid(
                    selectedZone = selectedZone,
                    onZoneSelected = { selectedZone = it }
                )
            }
            
            // Control de distancia (solo para "nearby")
            if (selectedZone == "nearby") {
                item {
                    DistanceSlider(
                        distance = distanceRadius,
                        onDistanceChange = { distanceRadius = it }
                    )
                }
            }
            
            // Filtros de búsqueda
            item {
                Text(
                    text = "Ajusta tu búsqueda",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            item {
                ZoneFiltersRow(
                    selectedFilters = selectedFilters,
                    onFilterToggle = { filter ->
                        selectedFilters = if (filter in selectedFilters) {
                            selectedFilters - filter
                        } else {
                            selectedFilters + filter
                        }
                    }
                )
            }
            
            // Búsquedas populares (desde Supabase)
            if (popularSearches.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ZonesPopularSection(
                        searches = popularSearches,
                        onSearchClick = { query -> executeSearch(query) }
                    )
                }
            }
            
            // Búsquedas recientes (desde SharedPreferences)
            if (recentSearches.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ZonesRecentSearches(
                        searches = recentSearches,
                        onSearchClick = { query -> executeSearch(query) },
                        onClearAll = { ZoneRepository.clearRecentSearches() },
                        onRemoveSearch = { query -> ZoneRepository.removeRecentSearch(query) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZonesHeader(
    onBack: () -> Unit,
    locationState: ZoneLocationState
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(AccentBlue, Color(0xFF0A3D62))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Zonas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (locationState.isLoaded && locationState.city.isNotBlank())
                        locationState.displayLocation
                    else "Detectando tu ubicación...",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ZonesLocationBanner(
    locationState: ZoneLocationState,
    zoneStats: com.rendly.app.data.repository.ZoneStats
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(AccentBlue, Color(0xFF0A3D62), Color(0xFF2E8B57))
                )
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (locationState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        locationState.isLoading -> "Detectando ubicación..."
                        locationState.error != null -> "Ubicación no disponible"
                        locationState.isLoaded -> locationState.displayLocation
                        else -> "Activá tu ubicación"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (locationState.error != null)
                    "Activa el GPS para descubrir productos cerca de ti."
                else
                    "Descubre productos increíbles en tu zona. Ahorra en envíos y recoge en persona.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
            
            if (zoneStats.isLoaded && (zoneStats.sellerCount > 0 || zoneStats.postCount > 0)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (zoneStats.sellerCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Store, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val sellerText = if (zoneStats.sellerCount >= 1000) 
                                    "${String.format("%.1f", zoneStats.sellerCount / 1000f)}k" 
                                else "${zoneStats.sellerCount}"
                                Text("$sellerText vendedores", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                    if (zoneStats.postCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Inventory2, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val postText = if (zoneStats.postCount >= 1000) 
                                    "${String.format("%.1f", zoneStats.postCount / 1000f)}k" 
                                else "${zoneStats.postCount}"
                                Text("$postText productos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZonesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = AccentBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "¿Qué estás buscando?",
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp
                    ),
                    singleLine = true
                )
            }
            
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSearch() },
                color = AccentBlue
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Buscar",
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp).size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ZoneCategoriesGrid(
    selectedZone: String,
    onZoneSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ZONE_CATEGORIES.take(2).forEach { zone ->
                ZoneCategoryCard(
                    zone = zone,
                    isSelected = selectedZone == zone.id,
                    onClick = { onZoneSelected(zone.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ZONE_CATEGORIES.drop(2).forEach { zone ->
                ZoneCategoryCard(
                    zone = zone,
                    isSelected = selectedZone == zone.id,
                    onClick = { onZoneSelected(zone.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ZoneCategoryCard(
    zone: ZoneCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) zone.color.copy(alpha = 0.15f) else Surface,
        border = if (isSelected) BorderStroke(2.dp, zone.color) else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(zone.color.copy(alpha = if (isSelected) 0.3f else 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = zone.icon,
                    contentDescription = zone.name,
                    tint = zone.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = zone.name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) zone.color else TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DistanceSlider(
    distance: Float,
    onDistanceChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.SocialDistance,
                        contentDescription = null,
                        tint = Color(0xFF2E8B57),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Radio de búsqueda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E8B57).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${distance.toInt()} km",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E8B57),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = distance,
                onValueChange = onDistanceChange,
                valueRange = 1f..50f,
                steps = 48,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF2E8B57),
                    activeTrackColor = Color(0xFF2E8B57),
                    inactiveTrackColor = TextMuted.copy(alpha = 0.2f)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 km", fontSize = 11.sp, color = TextMuted)
                Text("50 km", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun ZoneFiltersRow(
    selectedFilters: Set<String>,
    onFilterToggle: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ZONE_FILTERS) { filter ->
            val isSelected = filter.id in selectedFilters
            Surface(
                modifier = Modifier.clickable { onFilterToggle(filter.id) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PrimaryPurple else Surface,
                border = if (!isSelected) BorderStroke(1.dp, TextMuted.copy(alpha = 0.2f)) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = filter.name,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = filter.name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ZonesPopularSection(
    searches: List<PopularSearch>,
    onSearchClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.TrendingUp,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Búsquedas populares en tu zona",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(searches) { search ->
                val icon = when (search.iconName) {
                    "PhoneIphone" -> Icons.Outlined.PhoneIphone
                    "Hiking" -> Icons.Outlined.Hiking
                    "SportsEsports" -> Icons.Outlined.SportsEsports
                    "DirectionsBike" -> Icons.Outlined.DirectionsBike
                    "Laptop" -> Icons.Outlined.Laptop
                    "Headphones" -> Icons.Outlined.Headphones
                    "Monitor" -> Icons.Outlined.Monitor
                    "Chair" -> Icons.Outlined.Chair
                    else -> Icons.Outlined.Search
                }
                Surface(
                    modifier = Modifier.clickable { onSearchClick(search.query) },
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = IconAccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = search.query,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZonesRecentSearches(
    searches: List<RecentSearch>,
    onSearchClick: (String) -> Unit,
    onClearAll: () -> Unit,
    onRemoveSearch: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Búsquedas recientes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Text(
                text = "Limpiar",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = IconAccentBlue,
                modifier = Modifier.clickable { onClearAll() }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            searches.forEach { search ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSearchClick(search.query) },
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = search.query,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = search.timeAgo,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = TextMuted.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onRemoveSearch(search.query) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE RESULTADOS DE búsqueda - Ecommerce Profesional
// ---------------------------------------------------------------

private data class FilterOption(
    val id: String,
    val name: String,
    val icon: ImageVector
)

private val SEARCH_SORT_OPTIONS = listOf(
    FilterOption("relevance", "Relevancia", Icons.Outlined.Sort),
    FilterOption("price_low", "Menor precio", Icons.Outlined.ArrowDownward),
    FilterOption("price_high", "Mayor precio", Icons.Outlined.ArrowUpward),
    FilterOption("newest", "más recientes", Icons.Outlined.NewReleases),
    FilterOption("popular", "más populares", Icons.Outlined.TrendingUp)
)

private val SEARCH_CATEGORIES = listOf(
    "Todos", "Ropa", "Electrúnica", "Hogar", "Deportes", "Belleza", "Juguetes", "Libros"
)

// List of known category IDs from CategoryDrawer
private val KNOWN_CATEGORY_IDS = setOf(
    "vestidos", "blusas", "pantalones", "faldas", "abrigos", "zapatos_m",
    "camisas", "pantalones_h", "chaquetas", "trajes", "zapatos_h",
    "bolsos", "joyeria", "relojes", "gafas", "sombreros",
    "muebles", "decoracion", "plantas", "iluminacion",
    "smartphones", "laptops", "audio", "gaming"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultsScreen(
    initialQuery: String,
    onBack: () -> Unit,
    onProductClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    // Detect if initialQuery is a category ID
    val isDirectCategoryFilter = initialQuery in KNOWN_CATEGORY_IDS
    
    var searchQuery by remember { mutableStateOf(if (isDirectCategoryFilter) "" else initialQuery) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var filterByCategoryId by remember { mutableStateOf(if (isDirectCategoryFilter) initialQuery else null) }
    var selectedSort by remember { mutableStateOf("relevance") }
    var showFiltersSheet by remember { mutableStateOf(false) }
    var priceRange by remember { mutableStateOf(0f..10000f) }
    var onlyWithDiscount by remember { mutableStateOf(false) }
    var onlyFreeShipping by remember { mutableStateOf(false) }
    
    // Usar los items del repositorio
    val allItems by ExploreRepository.exploreItems.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // Detectar scroll para ocultar/mostrar carrusel de Categorías
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }
    
    // Cargar productos automáticamente al abrir la pantalla
    LaunchedEffect(Unit) {
        if (allItems.isEmpty()) {
            ExploreRepository.loadExploreItems()
        }
    }
    
    // Filtrar items Según Búsqueda y filtros
    val filteredItems = remember(searchQuery, selectedCategory, filterByCategoryId, allItems, selectedSort) {
        val baseItems = if (filterByCategoryId != null) {
            ExploreRepository.getItemsByCategory(filterByCategoryId!!)
        } else {
            allItems
        }
        
        baseItems
            .filter { item ->
                (searchQuery.isEmpty() || 
                 item.title.contains(searchQuery, ignoreCase = true) ||
                 (item.storeName?.contains(searchQuery, ignoreCase = true) == true))
            }
            .filter { item ->
                selectedCategory == "Todos" || item.category == selectedCategory
            }
            .let { list ->
                when (selectedSort) {
                    "price_low" -> list.sortedBy { it.price }
                    "price_high" -> list.sortedByDescending { it.price }
                    "newest" -> list.sortedByDescending { it.id }
                    "popular" -> list.sortedByDescending { it.likesCount }
                    else -> list
                }
            }
    }
    
    // Get category name for display
    val categoryDisplayName = remember(filterByCategoryId) {
        when (filterByCategoryId) {
            "vestidos" -> "Vestidos"
            "blusas" -> "Blusas y Tops"
            "pantalones" -> "Pantalones"
            "faldas" -> "Faldas"
            "abrigos" -> "Abrigos y Chaquetas"
            "zapatos_m" -> "Zapatos Mujer"
            "camisas" -> "Camisas"
            "pantalones_h" -> "Pantalones Hombre"
            "chaquetas" -> "Chaquetas"
            "trajes" -> "Trajes"
            "zapatos_h" -> "Zapatos Hombre"
            "bolsos" -> "Bolsos y Carteras"
            "joyeria" -> "Joyería"
            "relojes" -> "Relojes"
            "gafas" -> "Gafas de Sol"
            "sombreros" -> "Sombreros"
            "muebles" -> "Muebles"
            "decoracion" -> "Decoración"
            "plantas" -> "Plantas"
            "iluminacion" -> "Iluminación"
            "smartphones" -> "Smartphones"
            "laptops" -> "Laptops"
            "audio" -> "Audio"
            "gaming" -> "Gaming"
            else -> null
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Contenido principal con LazyColumn
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = if (categoryDisplayName == null && !isScrolled) 160.dp else 110.dp)
        ) {
            // Grid de productos
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No encontramos resultados",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Intenta con otros Términos o filtros",
                                fontSize = 14.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                val chunkedItems = filteredItems.chunked(2)
                items(chunkedItems) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                UnifiedProductCard(
                                    data = item.toProductCardData(),
                                    onClick = {
                                        onProductClick(exploreItemToPost(item))
                                    },
                                    imageHeight = 170.dp
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Header fijo con buscador, carrusel animado y filtros
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HomeBg)
                .align(Alignment.TopCenter)
        ) {
            // Buscador (siempre visible)
            SearchResultsHeader(
                query = if (categoryDisplayName != null) categoryDisplayName else searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    if (it.isNotEmpty() && filterByCategoryId != null) {
                        filterByCategoryId = null
                    }
                },
                onBack = onBack,
                resultCount = filteredItems.size,
                isCategory = categoryDisplayName != null
            )
            
            // Carrusel de Categorías con animación de ocultar al scroll
            androidx.compose.animation.AnimatedVisibility(
                visible = categoryDisplayName == null && !isScrolled,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                SearchCategoriesRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }
            
            // Chip de Categoría filtrada
            if (categoryDisplayName != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryPurple.copy(alpha = 0.1f),
                        modifier = Modifier.clickable { filterByCategoryId = null }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = categoryDisplayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryPurple
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Quitar filtro",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${filteredItems.size} productos",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
            
            // Barra de ordenar y filtros (siempre visible)
            SearchToolbar(
                selectedSort = selectedSort,
                onSortChange = { selectedSort = it },
                onFilterClick = { showFiltersSheet = true },
                hasActiveFilters = onlyWithDiscount || onlyFreeShipping
            )
        }
    }
}

@Composable
private fun SearchResultsHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    resultCount: Int,
    isCategory: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                
                // Campo de Búsqueda
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCategory) Icons.Default.Category else Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isCategory) PrimaryPurple else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Buscar productos...",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            if (isCategory) {
                                Text(
                                    text = query,
                                    color = PrimaryPurple,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = query,
                                    onValueChange = onQueryChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                        if (query.isNotEmpty() && !isCategory) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Contador de resultados
            Text(
                text = "$resultCount productos encontrados",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SearchCategoriesRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SEARCH_CATEGORIES) { category ->
            val isSelected = selectedCategory == category
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) PrimaryPurple else Surface
            ) {
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchToolbar(
    selectedSort: String,
    onSortChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean
) {
    var showSortMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // botón de ordenar
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable { showSortMenu = true },
            shape = RoundedCornerShape(10.dp),
            color = Surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sort,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = SEARCH_SORT_OPTIONS.find { it.id == selectedSort }?.name ?: "Ordenar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SEARCH_SORT_OPTIONS.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (selectedSort == option.id) PrimaryPurple else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option.name,
                                    fontWeight = if (selectedSort == option.id) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSort == option.id) PrimaryPurple else TextPrimary
                                )
                            }
                        },
                        onClick = {
                            onSortChange(option.id)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
        
        // botón de filtros
        Surface(
            modifier = Modifier.clickable { onFilterClick() },
            shape = RoundedCornerShape(10.dp),
            color = if (hasActiveFilters) PrimaryPurple.copy(alpha = 0.15f) else Surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filtros",
                    tint = if (hasActiveFilters) PrimaryPurple else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filtros",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasActiveFilters) PrimaryPurple else TextPrimary
                )
                if (hasActiveFilters) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(PrimaryPurple, CircleShape)
                    )
                }
            }
        }
    }
}

// SearchProductCard eliminado - ahora usamos UnifiedProductCard desde ui/components/



