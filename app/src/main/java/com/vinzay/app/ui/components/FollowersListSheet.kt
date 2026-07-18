package com.vinzay.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vinzay.app.data.repository.FollowerWithInfo
import com.vinzay.app.data.repository.FollowersRepository
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.launch

enum class FollowersTab { SEGUIDORES, CLIENTES }

@Composable
fun FollowersListSheet(
    isVisible: Boolean,
    userId: String,
    seguidoresCount: Int,
    clientesCount: Int,
    onDismiss: () -> Unit,
    onUserClick: (String) -> Unit = {}
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = androidx.compose.animation.core.tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = androidx.compose.animation.core.tween(300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Surface
            ) {
                FollowersListContent(
                    userId = userId,
                    seguidoresCount = seguidoresCount,
                    clientesCount = clientesCount,
                    onClose = onDismiss,
                    onUserClick = onUserClick
                )
            }
        }
    }
}

@Composable
private fun FollowersListContent(
    userId: String,
    seguidoresCount: Int,
    clientesCount: Int,
    onClose: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(FollowersTab.SEGUIDORES) }
    var searchQuery by remember { mutableStateOf("") }

    var followers by remember { mutableStateOf<List<FollowerWithInfo>>(emptyList()) }
    var clients by remember { mutableStateOf<List<FollowerWithInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentList = when (selectedTab) {
        FollowersTab.SEGUIDORES -> followers
        FollowersTab.CLIENTES -> clients
    }

    val filteredList = remember(currentList, searchQuery) {
        if (searchQuery.isBlank()) currentList
        else currentList.filter {
            it.username.contains(searchQuery, ignoreCase = true) ||
            it.nombreTienda?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                followers = FollowersRepository.getFollowersWithInfo(userId)
                clients = FollowersRepository.getClientsWithInfo(userId)
            } catch (e: Exception) {
                errorMessage = "Error al cargar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        loadData()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seguidores",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(TextMuted.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Close, "Cerrar", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Segmented toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OverlayMedium),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == FollowersTab.SEGUIDORES,
                label = "Seguidores",
                count = seguidoresCount,
                onClick = { selectedTab = FollowersTab.SEGUIDORES },
                modifier = Modifier.weight(1f)
            )
            SegmentedButton(
                selected = selectedTab == FollowersTab.CLIENTES,
                label = "Clientes",
                count = clientesCount,
                onClick = { selectedTab = FollowersTab.CLIENTES },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = OverlayMedium
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(PrimaryPurple),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) Text("Buscar...", color = TextMuted, fontSize = 14.sp)
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, "Limpiar", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando...", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { loadData() }) {
                            Text("Reintentar", color = PrimaryPurple)
                        }
                    }
                }
            }
            filteredList.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(
                            if (selectedTab == FollowersTab.SEGUIDORES) Icons.Outlined.People else Icons.Outlined.ShoppingCart,
                            null, tint = TextMuted, modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Sin resultados para \"$searchQuery\""
                                   else if (selectedTab == FollowersTab.SEGUIDORES) "Sin seguidores aún"
                                   else "Sin clientes aún",
                            color = TextMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredList, key = { it.userId }) { user ->
                        FollowerRow(
                            user = user,
                            onClick = { onUserClick(user.userId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedButton(
    selected: Boolean,
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) PrimaryPurple else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$label $count",
            color = if (selected) Color.White else TextMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun FollowerRow(
    user: FollowerWithInfo,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(modifier = Modifier.size(44.dp)) {
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(user.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + store
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${user.username}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 14.dp)
                    }
                }
                if (!user.nombreTienda.isNullOrBlank()) {
                    Text(
                        text = user.nombreTienda,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Follow/Following badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (user.isFollowingBack) AccentGreen.copy(alpha = 0.15f) else PrimaryPurple.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (user.isFollowingBack) "Te sigue" else "Siguiendo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (user.isFollowingBack) AccentGreen else PrimaryPurple,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
