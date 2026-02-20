package com.rendly.app.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rendly.app.ui.theme.*

enum class GalleryFilterType {
    ALL,
    IMAGES_ONLY,
    VIDEOS_ONLY
}

enum class GallerySortOrder {
    RECENT_FIRST,
    OLDEST_FIRST,
    LARGEST_FIRST,
    SMALLEST_FIRST
}

data class GalleryFilterState(
    val filterType: GalleryFilterType = GalleryFilterType.ALL,
    val sortOrder: GallerySortOrder = GallerySortOrder.RECENT_FIRST,
    val selectedAlbum: String = "Recientes"
)

data class AlbumOption(
    val name: String,
    val count: Int,
    val icon: ImageVector
)

@Composable
fun GalleryFilterModal(
    isVisible: Boolean,
    currentFilter: GalleryFilterState,
    albums: List<AlbumOption> = listOf(
        AlbumOption("Recientes", 0, Icons.Outlined.AccessTime),
        AlbumOption("Cámara", 0, Icons.Outlined.CameraAlt),
        AlbumOption("Capturas", 0, Icons.Outlined.Screenshot),
        AlbumOption("Descargas", 0, Icons.Outlined.Download),
        AlbumOption("WhatsApp", 0, Icons.Outlined.Chat)
    ),
    onDismiss: () -> Unit,
    onFilterChange: (GalleryFilterState) -> Unit
) {
    var localFilter by remember(currentFilter) { mutableStateOf(currentFilter) }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(tween(150)) + slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(200)
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(TextMuted.copy(alpha = 0.3f))
                            )
                        }
                        
                        // Title
                        Text(
                            text = "Filtrar galería",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            // Tipo de archivo section
                            item {
                                FilterSection(title = "Tipo de archivo")
                            }
                            
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    FilterChipItem(
                                        label = "Todo",
                                        icon = Icons.Outlined.Folder,
                                        isSelected = localFilter.filterType == GalleryFilterType.ALL,
                                        accentColor = PrimaryPurple,
                                        onClick = {
                                            localFilter = localFilter.copy(filterType = GalleryFilterType.ALL)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChipItem(
                                        label = "Imágenes",
                                        icon = Icons.Outlined.Image,
                                        isSelected = localFilter.filterType == GalleryFilterType.IMAGES_ONLY,
                                        accentColor = AccentPink,
                                        onClick = {
                                            localFilter = localFilter.copy(filterType = GalleryFilterType.IMAGES_ONLY)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChipItem(
                                        label = "Videos",
                                        icon = Icons.Outlined.Videocam,
                                        isSelected = localFilter.filterType == GalleryFilterType.VIDEOS_ONLY,
                                        accentColor = AccentGold,
                                        onClick = {
                                            localFilter = localFilter.copy(filterType = GalleryFilterType.VIDEOS_ONLY)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            
                            // Ordenar por section
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                FilterSection(title = "Ordenar por")
                            }
                            
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        SortOptionChip(
                                            label = "Más recientes",
                                            icon = Icons.Outlined.Schedule,
                                            isSelected = localFilter.sortOrder == GallerySortOrder.RECENT_FIRST,
                                            onClick = {
                                                localFilter = localFilter.copy(sortOrder = GallerySortOrder.RECENT_FIRST)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        SortOptionChip(
                                            label = "Más antiguos",
                                            icon = Icons.Outlined.History,
                                            isSelected = localFilter.sortOrder == GallerySortOrder.OLDEST_FIRST,
                                            onClick = {
                                                localFilter = localFilter.copy(sortOrder = GallerySortOrder.OLDEST_FIRST)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        SortOptionChip(
                                            label = "Mayor tamaño",
                                            icon = Icons.Outlined.PhotoSizeSelectLarge,
                                            isSelected = localFilter.sortOrder == GallerySortOrder.LARGEST_FIRST,
                                            onClick = {
                                                localFilter = localFilter.copy(sortOrder = GallerySortOrder.LARGEST_FIRST)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        SortOptionChip(
                                            label = "Menor tamaño",
                                            icon = Icons.Outlined.PhotoSizeSelectSmall,
                                            isSelected = localFilter.sortOrder == GallerySortOrder.SMALLEST_FIRST,
                                            onClick = {
                                                localFilter = localFilter.copy(sortOrder = GallerySortOrder.SMALLEST_FIRST)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                            
                            // Álbumes section
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                FilterSection(title = "Álbumes")
                            }
                            
                            items(albums) { album ->
                                AlbumItem(
                                    album = album,
                                    isSelected = localFilter.selectedAlbum == album.name,
                                    onClick = {
                                        localFilter = localFilter.copy(selectedAlbum = album.name)
                                    }
                                )
                            }
                        }
                        
                        // Bottom buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    localFilter = GalleryFilterState()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextSecondary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(TextMuted.copy(alpha = 0.3f), TextMuted.copy(alpha = 0.3f))
                                    )
                                )
                            ) {
                                Text("Restablecer")
                            }
                            
                            Button(
                                onClick = {
                                    onFilterChange(localFilter)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                )
                            ) {
                                Text("Aplicar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String) {
    Text(
        text = title,
        color = TextMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
    )
}

@Composable
private fun FilterChipItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(150),
        label = "bgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else TextMuted.copy(alpha = 0.2f),
        animationSpec = tween(150),
        label = "borderColor"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else TextMuted,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = if (isSelected) accentColor else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SortOptionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(150),
        label = "bgColor"
    )
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isSelected) PrimaryPurple.copy(alpha = 0.5f) else TextMuted.copy(alpha = 0.15f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PrimaryPurple else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun AlbumItem(
    album: AlbumOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) PrimaryPurple.copy(alpha = 0.2f)
                    else TextMuted.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = album.icon,
                contentDescription = album.name,
                tint = if (isSelected) PrimaryPurple else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.name,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (album.count > 0) {
                Text(
                    text = "${album.count} elementos",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Seleccionado",
                tint = PrimaryPurple,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
