package com.rendly.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.model.Post
import com.rendly.app.data.repository.CartRepository
import com.rendly.app.data.repository.ExploreItem
import com.rendly.app.data.repository.ExploreRepository
import com.rendly.app.ui.components.ProductCardData
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
    val icon: androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun SearchResultsScreen(
    initialQuery: String = "",
    isCategory: Boolean = false,
    onBack: () -> Unit,
    onProductClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val exploreItems by ExploreRepository.exploreItems.collectAsState()
    val isLoading by ExploreRepository.isLoading.collectAsState()
    
    // Filtrar items según búsqueda Y categoría
    val filteredItems = remember(exploreItems, searchQuery, selectedCategory) {
        var items = exploreItems
        
        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            items = items.filter { item ->
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true) ||
                item.username.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Filtrar por categoría
        if (selectedCategory != null && selectedCategory != "all") {
            items = items.filter { item ->
                item.category.contains(selectedCategory!!, ignoreCase = true)
            }
        }
        
        items
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
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header minimalista - solo buscador con flecha
            MinimalSearchHeader(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onBack = onBack,
                isCategory = isCategory
            )
            
            // Carrusel de categorías - SIEMPRE visible
            CategoryCarousel(
                categories = SEARCH_CATEGORIES,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = if (selectedCategory == it) null else it }
            )
            
            // Contenido
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
                // Grid de productos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val chunkedItems = filteredItems.chunked(2)
                    items(chunkedItems) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f).height(340.dp)) {
                                    UnifiedProductCard(
                                        data = item.toProductCardData(),
                                        onClick = {
                                            onProductClick(exploreItemToPost(item))
                                        },
                                        imageHeight = 150.dp
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
        }
    }
}

// Extensión toProductCardData ahora está en UnifiedProductCard.kt

@Composable
private fun MinimalSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
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
                        .clickable { /* TODO: open cart */ },
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
                            color = androidx.compose.ui.graphics.Color.White
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
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            val isSelected = selectedCategory == category.id || (selectedCategory == null && category.id == "all")
            
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category.id) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
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
                        tint = if (isSelected) androidx.compose.ui.graphics.Color.White else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = category.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) androidx.compose.ui.graphics.Color.White else TextSecondary
                    )
                }
            }
        }
    }
}
