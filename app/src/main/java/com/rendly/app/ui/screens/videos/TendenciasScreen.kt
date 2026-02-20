package com.rendly.app.ui.screens.videos

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.repository.TrendRepository
import com.rendly.app.data.repository.TrendingPostItem
import com.rendly.app.data.repository.TrendingTagItem
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Categorías de tendencias
private data class TrendCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

private val TREND_CATEGORIES = listOf(
    TrendCategory("all", "Todo", Icons.Filled.Whatshot, Color(0xFFFF6B6B)),
    TrendCategory("fashion", "Moda", Icons.Outlined.Checkroom, Color(0xFF2E8B57)),
    TrendCategory("tech", "Tech", Icons.Outlined.Devices, Color(0xFF1565A0)),
    TrendCategory("beauty", "Belleza", Icons.Outlined.Face, Color(0xFFF472B6)),
    TrendCategory("home", "Hogar", Icons.Outlined.Home, Color(0xFF2E8B57)),
    TrendCategory("sports", "Deportes", Icons.Outlined.FitnessCenter, Color(0xFFFF6B35))
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TendenciasScreen(
    onBack: () -> Unit = {},
    onTrendClick: (TrendingPostItem) -> Unit = {},
    onHashtagClick: (TrendingTagItem) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("all") }
    val scope = rememberCoroutineScope()
    
    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                TrendRepository.loadTrending()
                delay(600)
                isRefreshing = false
            }
        }
    )
    
    // Datos reales desde TrendRepository
    val trendingItems by TrendRepository.trendingPosts.collectAsState()
    val trendingHashtags by TrendRepository.trendingTags.collectAsState()
    val isLoading by TrendRepository.isLoading.collectAsState()
    
    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        if (trendingItems.isEmpty()) {
            TrendRepository.loadTrending()
        }
    }
    
    // Focus search field when activated
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(300)
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }
    
    // Filter by category AND search query
    val filteredItems = remember(selectedCategory, searchQuery, trendingItems) {
        trendingItems.filter { item ->
            val matchesCategory = selectedCategory == "all" || item.category == selectedCategory
            val matchesSearch = searchQuery.isEmpty() ||
                item.title.lowercase().contains(searchQuery.lowercase()) ||
                item.hashtag.lowercase().contains(searchQuery.lowercase()) ||
                item.username.lowercase().contains(searchQuery.lowercase())
            matchesCategory && matchesSearch
        }
    }
    
    val filteredHashtags = remember(searchQuery, trendingHashtags) {
        if (searchQuery.isEmpty()) trendingHashtags
        else trendingHashtags.filter {
            it.hashtag.lowercase().contains(searchQuery.lowercase())
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            .statusBarsPadding()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header with search animation
            item(key = "header") {
                TendenciasHeaderScrollable(
                    onBack = onBack,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchActivate = { isSearchActive = true },
                    onSearchDeactivate = {
                        isSearchActive = false
                        searchQuery = ""
                    },
                    focusRequester = focusRequester
                )
            }
            
            // Loading indicator
            if (isLoading && trendingItems.isEmpty()) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF6B6B),
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
            
            // Hashtags en Tendencia - Carrusel horizontal
            if (filteredHashtags.isNotEmpty()) {
                item(key = "hashtags_section") {
                    TrendingHashtagsSection(
                        hashtags = filteredHashtags,
                        onHashtagClick = onHashtagClick
                    )
                }
            }
            
            // Categories
            item(key = "categories") {
                TrendCategoriesRow(
                    categories = TREND_CATEGORIES,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Sección "Videos en Tendencia"
            item(key = "videos_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Resultados" else "Videos en Tendencia",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${filteredItems.size})",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Trending List
            if (filteredItems.isEmpty() && searchQuery.isNotEmpty()) {
                item(key = "empty_search") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron resultados",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            itemsIndexed(filteredItems, key = { _, item -> item.id }) { index, item ->
                TrendingItemCard(
                    item = item,
                    onClick = { onTrendClick(item) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        
        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Surface,
            contentColor = Color(0xFFFF6B6B)
        )
    }
}

// Header scrollable with search animation
@Composable
private fun TendenciasHeaderScrollable(
    onBack: () -> Unit,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchActivate: () -> Unit,
    onSearchDeactivate: () -> Unit,
    focusRequester: FocusRequester
) {
    val headerAlpha by animateFloatAsState(
        targetValue = if (isSearchActive) 0f else 1f,
        animationSpec = tween(250),
        label = "headerAlpha"
    )
    val searchBarWeight by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = tween(300),
        label = "searchWeight"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button (always visible)
        IconButton(
            onClick = {
                if (isSearchActive) onSearchDeactivate()
                else onBack()
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isSearchActive) Icons.Default.ArrowBack else Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Animated: Header text fades out, search bar expands in
        Box(modifier = Modifier.weight(1f)) {
            // Header content - fades out
            if (headerAlpha > 0f) {
                Column(
                    modifier = Modifier.graphicsLayer { alpha = headerAlpha }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tendencias",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Videos verticales en tendencia",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
            
            // Search bar - slides in
            if (searchBarWeight > 0f) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .graphicsLayer { alpha = searchBarWeight },
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
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar tendencias, hashtags...",
                                    color = TextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFFFF6B6B)),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {})
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
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
                    }
                }
            }
        }
        
        // Search button - only when NOT searching
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            IconButton(
                onClick = onSearchActivate,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Surface)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Buscar",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Sección de hashtags en tendencia con carrusel horizontal
@Composable
private fun TrendingHashtagsSection(
    hashtags: List<TrendingTagItem>,
    onHashtagClick: (TrendingTagItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header de la sección
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Tag,
                contentDescription = null,
                tint = Color(0xFFFF6B35),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Hashtags en Tendencia",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Carrusel horizontal
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hashtags, key = { it.id }) { hashtag ->
                TrendingHashtagCard(
                    hashtag = hashtag,
                    onClick = { onHashtagClick(hashtag) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Card de hashtag con carrusel de thumbnails
@Composable
private fun TrendingHashtagCard(
    hashtag: TrendingTagItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Thumbnails en grid/overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // Mostrar hasta 3 thumbnails con overlay
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    hashtag.thumbnails.take(3).forEachIndexed { index, url ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.6f)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
                
                // Hashtag title overlay at bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = hashtag.hashtag,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatViewCount(hashtag.totalViews),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Info section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${hashtag.videoCount} videos",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF6B35).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Ver más",
                        color = Color(0xFFFF6B35),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopTrendsHero(
    items: List<TrendingPostItem>,
    onItemClick: (TrendingPostItem) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Glowing background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF6B6B).copy(alpha = glowAlpha * 0.3f),
                            Color(0xFF2E8B57).copy(alpha = glowAlpha * 0.2f),
                            Color(0xFFFF6B35).copy(alpha = glowAlpha * 0.3f)
                        )
                    )
                )
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TOP 3 AHORA",
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEachIndexed { index, item ->
                    TopTrendCard(
                        item = item,
                        rank = index + 1,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopTrendCard(
    item: TrendingPostItem,
    rank: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> TextMuted
    }
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Surface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.2f))
                    .border(1.5.dp, rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = rankColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.hashtag,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = formatViewCount(item.viewCount),
                color = TextMuted,
                fontSize = 10.sp
            )
            
            // Growth indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF2E8B57),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "+${item.growthPercent}%",
                    color = Color(0xFF2E8B57),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TrendCategoriesRow(
    categories: List<TrendCategory>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.id == selectedCategory
            
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategorySelected(category.id) },
                color = if (isSelected) category.color.copy(alpha = 0.15f) else Surface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = if (isSelected) category.color else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = category.name,
                        color = if (isSelected) category.color else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingItemCard(
    item: TrendingPostItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rankColor = when (item.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> TextMuted
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (item.rank <= 3) rankColor.copy(alpha = 0.15f)
                        else Surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.rank}",
                    color = if (item.rank <= 3) rankColor else TextMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Thumbnail
            if (item.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isHot) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = "Hot",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = item.hashtag,
                    color = Color(0xFFFF6B35),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatViewCount(item.viewCount),
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF2E8B57),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${item.growthPercent}%",
                            color = Color(0xFF2E8B57),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatViewCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
