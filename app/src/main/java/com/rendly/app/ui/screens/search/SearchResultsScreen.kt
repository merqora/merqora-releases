package com.rendly.app.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.model.Post
import com.rendly.app.data.repository.CartRepository
import com.rendly.app.data.repository.ExploreItem
import com.rendly.app.data.repository.ExploreRepository
import com.rendly.app.ui.components.AdvancedFilterModal
import com.rendly.app.ui.components.SearchFilterState
import com.rendly.app.ui.components.CartModal
import com.rendly.app.ui.components.UnifiedProductCard
import com.rendly.app.ui.components.toProductCardData
import com.rendly.app.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * SEARCH RESULTS SCREEN - Componente independiente
 * ═══════════════════════════════════════════════════════════════════════════════
 * Pantalla de resultados de búsqueda con diseño profesional.
 * - Header minimalista: solo buscador con flecha atrás
 * - Tarjetas de altura uniforme
 * - Imágenes deslizables con dots
 * - Avatar y username del vendedor
 */

// Categorías disponibles para filtrado
private data class SearchCategory(
    val id: String,
    val name: String,
    val icon: ImageVector
)

private val SEARCH_CATEGORIES = listOf(
    SearchCategory("all", "Todos", Icons.Outlined.GridView),
    SearchCategory("ropa", "Ropa", Icons.Outlined.Checkroom),
    SearchCategory("zapatos", "Zapatos", Icons.Outlined.Hiking),
    SearchCategory("accesorios", "Accesorios", Icons.Outlined.Watch),
    SearchCategory("electronica", "Electrónica", Icons.Outlined.Devices),
    SearchCategory("hogar", "Hogar", Icons.Outlined.Home),
    SearchCategory("deportes", "Deportes", Icons.Outlined.FitnessCenter)
)

private data class SortOption(
    val id: String,
    val name: String,
    val icon: ImageVector
)

private val SORT_OPTIONS = listOf(
    SortOption("relevance", "Relevancia", Icons.Outlined.Sort),
    SortOption("price_low", "Menor precio", Icons.Outlined.ArrowDownward),
    SortOption("price_high", "Mayor precio", Icons.Outlined.ArrowUpward),
    SortOption("newest", "Más recientes", Icons.Outlined.Schedule),
    SortOption("popular", "Más populares", Icons.Outlined.TrendingUp)
)

// Known category IDs from CategoryDrawer
private val KNOWN_CATEGORY_IDS = setOf(
    "vestidos", "blusas", "pantalones", "faldas", "abrigos", "zapatos_m",
    "camisas", "pantalones_h", "chaquetas", "trajes", "zapatos_h",
    "bolsos", "joyeria", "relojes", "gafas", "sombreros",
    "muebles", "decoracion", "plantas", "iluminacion",
    "smartphones", "laptops", "audio", "gaming"
)

private fun getCategoryDisplayName(id: String?): String? {
    return when (id) {
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

@Composable
fun SearchResultsScreen(
    initialQuery: String = "",
    isCategory: Boolean = false,
    onBack: () -> Unit,
    onProductClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    // Detect if initialQuery is a direct category ID
    val isDirectCategoryFilter = initialQuery in KNOWN_CATEGORY_IDS
    
    var searchQuery by remember { mutableStateOf(if (isDirectCategoryFilter) "" else initialQuery) }
    var filterByCategoryId by remember { mutableStateOf(if (isDirectCategoryFilter) initialQuery else null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf("relevance") }
    
    // Estado de filtros avanzados y carrito
    var showFilterModal by remember { mutableStateOf(false) }
    var showCartModal by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(SearchFilterState()) }
    
    val exploreItems by ExploreRepository.exploreItems.collectAsState()
    val isLoading by ExploreRepository.isLoading.collectAsState()
    val listState = rememberLazyListState()
    
    // Detectar scroll para ocultar/mostrar carrusel de categorías
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }
    
    val categoryDisplayName = remember(filterByCategoryId) { getCategoryDisplayName(filterByCategoryId) }
    val effectiveIsCategory = isCategory || categoryDisplayName != null
    val headerQuery = categoryDisplayName ?: searchQuery
    
    // Filtrar items según búsqueda, categoría y ordenamiento
    val filteredItems = remember(exploreItems, searchQuery, selectedCategory, filterByCategoryId, selectedSort, filterState) {
        val baseItems = if (filterByCategoryId != null) {
            ExploreRepository.getItemsByCategory(filterByCategoryId!!)
        } else {
            exploreItems
        }
        
        var items = baseItems
        
        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            items = items.filter { item ->
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true) ||
                item.username.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Filtrar por categoría del carrusel
        if (selectedCategory != null && selectedCategory != "all") {
            items = items.filter { item ->
                item.category.contains(selectedCategory!!, ignoreCase = true)
            }
        }
        
        // --- FILTROS AVANZADOS ---
        // Precio mínimo
        filterState.minPrice?.let { min ->
            items = items.filter { it.price >= min }
        }
        // Precio máximo
        filterState.maxPrice?.let { max ->
            items = items.filter { it.price <= max }
        }
        // Estado (Nuevo/Usado)
        filterState.condition?.let { cond ->
            items = items.filter { it.condition?.contains(cond, ignoreCase = true) == true }
        }
        // Envío gratis
        if (filterState.freeShipping) {
            items = items.filter { it.freeShipping }
        }
        // Ubicación
        filterState.location?.let { loc ->
            // Simulación de filtro por ubicación (en un caso real vendría de la DB)
            if (loc == "Tu ciudad") {
                // Filtro placeholder
            }
        }
        
        // Ordenar
        when (selectedSort) {
            "price_low" -> items.sortedBy { it.price }
            "price_high" -> items.sortedByDescending { it.price }
            "newest" -> items.sortedByDescending { it.id }
            "popular" -> items.sortedByDescending { it.likesCount }
            else -> items
        }
    }
    
    // Cargar items al inicio
    LaunchedEffect(Unit) {
        if (exploreItems.isEmpty()) {
            ExploreRepository.loadExploreItems()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Contenido principal
        if (isLoading && filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (filteredItems.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
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
                        text = "Intenta con otros términos",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            // Grid de productos con padding superior para dejar espacio al header fijo
            // Cuando las categorías están ocultas (por scroll o por category filter), menos padding
            val topPadding = if (categoryDisplayName != null || isScrolled) 160.dp else 200.dp
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = topPadding, bottom = 16.dp)
            ) {
                val chunkedItems = filteredItems.chunked(2)
                items(chunkedItems) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f).height(355.dp)) {
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
                
                // Espacio inferior
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Header fijo en la parte superior (encima del contenido)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HomeBg)
                .align(Alignment.TopCenter)
        ) {
            // Buscador con flecha atrás
            MinimalSearchHeader(
                query = headerQuery,
                onQueryChange = { 
                    searchQuery = it
                    if (it.isNotEmpty() && filterByCategoryId != null) {
                        filterByCategoryId = null
                    }
                },
                onBack = onBack,
                onCartClick = { showCartModal = true },
                isCategory = effectiveIsCategory
            )
            
            // Carrusel de categorías con animación de ocultar al scroll
            AnimatedVisibility(
                visible = categoryDisplayName == null && !isScrolled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                CategoryCarousel(
                    categories = SEARCH_CATEGORIES,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = if (selectedCategory == it) null else it }
                )
            }
            
            // Chip de categoría filtrada
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
            
            // Barra de ordenar y filtros
            SortToolbar(
                selectedSort = selectedSort,
                onSortChange = { selectedSort = it },
                onFilterClick = { showFilterModal = true }
            )
        }
        
        // Modales
        AdvancedFilterModal(
            isVisible = showFilterModal,
            currentState = filterState,
            onDismiss = { showFilterModal = false },
            onApply = { filterState = it }
        )
        
        CartModal(
            isVisible = showCartModal,
            onDismiss = { showCartModal = false }
        )
    }
}

// Extensión toProductCardData ahora está en UnifiedProductCard.kt

@Composable
private fun MinimalSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    isCategory: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón volver
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            
            // Campo de búsqueda
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
                            BasicTextField(
                                value = query,
                                onValueChange = onQueryChange,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
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
            
            // Cart icon with badge
            val cartItems by CartRepository.cartItems.collectAsState()
            val cartItemCount = cartItems.sumOf { it.quantity }
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Surface)
                        .clickable { onCartClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Carrito",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(if (cartItemCount > 9) 18.dp else 16.dp)
                            .clip(CircleShape)
                            .background(ButtonBuyNow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                            fontSize = if (cartItemCount > 9) 8.sp else 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Helper para convertir ExploreItem a Post
private fun exploreItemToPost(item: ExploreItem): Post {
    return Post(
        id = item.id,
        userId = item.userId,
        title = item.title,
        price = item.price,
        images = item.images,
        category = item.category,
        condition = item.condition ?: "",
        freeShipping = item.freeShipping,
        username = item.username,
        userAvatar = item.userAvatar,
        userStoreName = item.storeName,
        likesCount = item.likesCount,
        reviewsCount = item.reviewsCount,
        isUserVerified = item.isVerified
    )
}

@Composable
private fun CategoryCarousel(
    categories: List<SearchCategory>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val isSelected = selectedCategory == category.id || (selectedCategory == null && category.id == "all")
            
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category.id) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) PrimaryPurple else Surface
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
private fun SortToolbar(
    selectedSort: String,
    onSortChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón de ordenar
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
                    text = SORT_OPTIONS.find { it.id == selectedSort }?.name ?: "Ordenar",
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
                SORT_OPTIONS.forEach { option ->
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
        
        // Botón de filtros
        Surface(
            modifier = Modifier.clickable { onFilterClick() },
            shape = RoundedCornerShape(10.dp),
            color = Surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filtros",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Filtros",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}
