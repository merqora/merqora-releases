package com.rendly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*
import com.rendly.app.data.repository.ExploreRepository
import kotlinx.coroutines.launch

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
        CategoryItem("vestidos", "Vestidos", Icons.Outlined.FavoriteBorder, listOf(Color(0xFF2E8B57), Color(0xFFF472B6))),
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

@Composable
fun rememberCategorySections(): List<Pair<String, List<CategoryItem>>> {
    val exploreItems by ExploreRepository.exploreItems.collectAsState()

    return remember(exploreItems) {
        val counts = ExploreRepository.getCategoryCounts()
        val totalItems = exploreItems.size

        CATEGORY_DEFINITIONS.map { (sectionName, categories) ->
            sectionName to categories.map { cat ->
                val count = counts[cat.id] ?: 0
                val isTrending = count > 0 && count >= (totalItems * 0.1)
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
    val categorySections = rememberCategorySections()
    val listState = rememberLazyListState()

    val offsetX = remember { Animatable(if (isVisible) 0f else -1f) }
    val velocityTracker = remember { VelocityTracker() }
    var committedToClose by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            committedToClose = false
            ExploreRepository.preloadCategoryStats()
            if (!ExploreRepository.hasStatsLoaded()) {
                ExploreRepository.loadExploreItems(forceRefresh = !ExploreRepository.hasCache)
            }
            scope.launch {
                kotlinx.coroutines.delay(100)
                ExploreRepository.preloadNextCategories()
            }
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 280f)
            )
        } else if (offsetX.value > -1f) {
            committedToClose = true
            offsetX.animateTo(
                targetValue = -1f,
                animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f)
            )
        }
    }

    LaunchedEffect(offsetX.value) {
        if (offsetX.value >= 0f && isVisible) {
            committedToClose = false
        }
    }

    val backdropAlpha = ((1f + offsetX.value).coerceIn(0f, 1f) * 0.5f).coerceIn(0f, 0.5f)

    val filteredSections = remember(searchQuery, categorySections) {
        if (searchQuery.isBlank()) {
            categorySections
        } else {
            val query = searchQuery.lowercase()
            categorySections.mapNotNull { (section, cats) ->
                val filtered = cats.filter {
                    it.name.lowercase().contains(query) || section.lowercase().contains(query)
                }
                if (filtered.isNotEmpty()) section to filtered else null
            }
        }
    }

    if (isVisible || offsetX.value > -1f) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backdropAlpha > 0.001f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = backdropAlpha }
                        .background(Color.Black)
                        .clickable(enabled = !committedToClose) {
                            committedToClose = true
                            scope.launch {
                                offsetX.animateTo(
                                    -1f,
                                    spring(dampingRatio = 0.85f, stiffness = 500f)
                                )
                                onDismiss()
                            }
                        }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = (offsetX.value * size.width).coerceIn(-size.width, 0f)
                    }
                    .pointerInput(Unit) {
                        if (committedToClose) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (!committedToClose) {
                                    val velocity = velocityTracker.calculateVelocity().x
                                    if (velocity < -400f || offsetX.value < -0.3f) {
                                        scope.launch {
                                            committedToClose = true
                                            offsetX.animateTo(
                                                -1f,
                                                spring(dampingRatio = 0.85f, stiffness = 500f)
                                            )
                                            onDismiss()
                                        }
                                    } else if (offsetX.value > 0.05f) {
                                        scope.launch {
                                            offsetX.animateTo(
                                                0f,
                                                spring(dampingRatio = 0.75f, stiffness = 400f)
                                            )
                                        }
                                    } else {
                                        scope.launch {
                                            offsetX.animateTo(
                                                0f,
                                                spring(dampingRatio = 0.7f, stiffness = 300f)
                                            )
                                        }
                                    }
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                if (!committedToClose) {
                                    velocityTracker.addPosition(
                                        change.uptimeMillis,
                                        Offset(change.position.x, change.position.y)
                                    )
                                    scope.launch {
                                        offsetX.stop()
                                        val newValue = (offsetX.value + dragAmount / size.width)
                                            .coerceIn(-1.2f, 0.1f)
                                        offsetX.snapTo(newValue)
                                    }
                                }
                            },
                            onDragCancel = {
                                if (!committedToClose) {
                                    scope.launch {
                                        offsetX.animateTo(
                                            0f,
                                            spring(dampingRatio = 0.7f, stiffness = 300f)
                                        )
                                    }
                                }
                            }
                        )
                    }
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.88f),
                    color = HomeBg,
                    shadowElevation = 32.dp,
                    shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        DrawerHeader(onDismiss = onDismiss)

                        DrawerSearchBar(
                            searchQuery = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (filteredSections.isEmpty() && searchQuery.isNotBlank()) {
                                item(key = "no_results") {
                                    NoResultsPlaceholder(query = searchQuery)
                                }
                            }

                            filteredSections.forEach { (sectionName, categories) ->
                                item(key = "header_$sectionName") {
                                    SectionHeader(
                                        title = sectionName,
                                        count = categories.size
                                    )
                                }

                                items(categories, key = { it.id }) { category ->
                                    CategoryRow(
                                        category = category,
                                        onClick = {
                                            onCategorySelected(category.id)
                                        }
                                    )
                                }

                                item(key = "spacer_$sectionName") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            item(key = "footer_divider") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp, horizontal = 8.dp)
                                        .height(1.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Surface.copy(alpha = 0.5f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            item(key = "footer_links") {
                                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                    FooterLink("Ayuda y Soporte") { onHelpCenter(); onDismiss() }
                                    FooterLink("Política de Privacidad") { onPrivacyPolicy(); onDismiss() }
                                    FooterLink("Términos y Condiciones") { onTermsAndConditions(); onDismiss() }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = "Merqora © 2024",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted.copy(alpha = 0.5f)
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
private fun DrawerHeader(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryPurple.copy(alpha = 0.06f),
                        HomeBg
                    )
                )
            )
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Explorar",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Descubre todas nuestras categorías",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    letterSpacing = 0.2.sp
                )
            }

            Surface(
                onClick = onDismiss,
                modifier = Modifier.size(34.dp),
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
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerSearchBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit
) {
    val textColor = TextPrimary
    val placeholderColor = TextMuted
    val cursorColor = PrimaryBright

    TextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        placeholder = {
            Text(
                text = "Buscar categorías...",
                color = placeholderColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        textStyle = LocalTextStyle.current.copy(
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            cursorColor = cursorColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        ),
        shape = RoundedCornerShape(14.dp),
        singleLine = true
    )
}

@Composable
private fun NoResultsPlaceholder(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = TextMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sin resultados para \"$query\"",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Prueba con otros términos",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextMuted
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryBright.copy(alpha = 0.6f))
            )
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.3.sp
            )
        }
        Text(
            text = "$count",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceElevated.copy(alpha = 0.6f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(0.dp, 2.dp, 0.dp, 2.dp))
                    .background(category.gradient[0].copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.gradient[0],
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    category.badge?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (badge) {
                                "HOT" -> AccentPink.copy(alpha = 0.9f)
                                "NEW" -> AccentGreen.copy(alpha = 0.9f)
                                else -> PrimaryPurple.copy(alpha = 0.9f)
                            }
                        ) {
                            Text(
                                text = badge,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
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
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }
                    if (category.trending) {
                        Text(
                            text = "·",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Trending",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentGreen
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FooterLink(text: String, onClick: () -> Unit = {}) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = TextMuted,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
    )
}

private fun formatItemCount(count: Int): String {
    return when {
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
}
