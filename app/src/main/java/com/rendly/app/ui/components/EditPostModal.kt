package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.ui.theme.*

// ═══════════════════════════════════════════════════════════════════════════════
// MODAL PARA EDITAR PUBLICACIONES - Long Press en posts del perfil
// ═══════════════════════════════════════════════════════════════════════════════

// Data class para variante con precio individual
data class VariantPriceData(
    val imageUrl: String,
    val colorName: String? = null,
    val price: Double? = null, // null = usa precio base
    val originalPrice: Double? = null
)

data class EditPostData(
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val originalPrice: Double? = null,
    val showOriginalPrice: Boolean = false,
    val category: String = "",
    val condition: String = "Nuevo",
    val isAvailable: Boolean = true,
    val allowOffers: Boolean = false,
    val freeShipping: Boolean = false,
    // New fields
    val warranty: String = "Sin garantía",
    val returnsAccepted: Boolean = false,
    val colors: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    // Precios por variante - mapa de índice de imagen a precio
    val variantPrices: Map<Int, VariantPriceData> = emptyMap(),
    // Colores asignados a imágenes - mapa de índice de imagen a color
    val imageColors: Map<Int, String> = emptyMap()
)

@Composable
fun EditPostModal(
    isVisible: Boolean,
    post: Post?,
    onDismiss: () -> Unit,
    onSave: (EditPostData) -> Unit,
    onDelete: () -> Unit,
    onPromote: () -> Unit = {},
    onViewStats: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    
    // Guardar el último post válido para evitar crash durante animación de salida
    var lastValidPost by remember { mutableStateOf<Post?>(null) }
    LaunchedEffect(post) {
        if (post != null) {
            lastValidPost = post
        }
    }
    
    // Usar el post actual o el último válido durante la animación
    val currentPost = post ?: lastValidPost
    
    // Estado de edición
    var editData by remember(currentPost?.id) {
        val prevPrice = currentPost?.previousPrice
        mutableStateOf(
            EditPostData(
                title = currentPost?.title ?: currentPost?.producto?.titulo ?: "",
                description = currentPost?.description ?: currentPost?.producto?.descripcion ?: "",
                price = currentPost?.price?.takeIf { it > 0 } ?: currentPost?.producto?.precio ?: 0.0,
                originalPrice = prevPrice,
                showOriginalPrice = prevPrice != null && prevPrice > 0,
                category = currentPost?.category ?: currentPost?.producto?.categoria ?: "",
                condition = currentPost?.condition ?: currentPost?.producto?.condicion ?: "Nuevo",
                warranty = currentPost?.warranty ?: "Sin garantía",
                returnsAccepted = currentPost?.returnsAccepted ?: false,
                allowOffers = currentPost?.allowOffers ?: false,
                freeShipping = currentPost?.freeShipping ?: false,
                colors = currentPost?.colors ?: emptyList(),
                images = currentPost?.images?.ifEmpty { currentPost?.producto?.imagenUrl } ?: emptyList()
            )
        )
    }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPriceSettings by remember { mutableStateOf(false) }
    var showImageEditor by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Reset al cerrar
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            showDeleteConfirm = false
            showPriceSettings = false
            showImageEditor = false
            showCategorySelector = false
            isSaving = false
        }
    }
    
    // Lista de categorías - sincronizado con CategoryDrawer
    val categories = listOf(
        // Moda Mujer
        "vestidos" to Icons.Outlined.Favorite,
        "blusas" to Icons.Outlined.ShoppingBag,
        "pantalones" to Icons.Outlined.Checkroom,
        "faldas" to Icons.Outlined.Face,
        "abrigos" to Icons.Outlined.AcUnit,
        "zapatos_m" to Icons.Outlined.RunCircle,
        // Moda Hombre
        "camisas" to Icons.Outlined.PersonOutline,
        "pantalones_h" to Icons.Outlined.Checkroom,
        "chaquetas" to Icons.Outlined.AcUnit,
        "trajes" to Icons.Outlined.BusinessCenter,
        "zapatos_h" to Icons.Outlined.RunCircle,
        // Accesorios
        "bolsos" to Icons.Outlined.ShoppingBag,
        "joyeria" to Icons.Outlined.Diamond,
        "relojes" to Icons.Outlined.Watch,
        "gafas" to Icons.Outlined.Visibility,
        "sombreros" to Icons.Outlined.Face,
        // Hogar y Deco
        "muebles" to Icons.Outlined.Chair,
        "decoracion" to Icons.Outlined.Palette,
        "plantas" to Icons.Outlined.Spa,
        "iluminacion" to Icons.Outlined.LightMode,
        // Electrónica
        "smartphones" to Icons.Outlined.PhoneAndroid,
        "laptops" to Icons.Outlined.Laptop,
        "audio" to Icons.Outlined.Headphones,
        "gaming" to Icons.Outlined.SportsEsports
    )
    
    // Map category IDs to display names
    val categoryDisplayNames = mapOf(
        "vestidos" to "Vestidos",
        "blusas" to "Blusas y Tops",
        "pantalones" to "Pantalones",
        "faldas" to "Faldas",
        "abrigos" to "Abrigos y Chaquetas",
        "zapatos_m" to "Zapatos Mujer",
        "camisas" to "Camisas",
        "pantalones_h" to "Pantalones Hombre",
        "chaquetas" to "Chaquetas",
        "trajes" to "Trajes",
        "zapatos_h" to "Zapatos Hombre",
        "bolsos" to "Bolsos y Carteras",
        "joyeria" to "Joyería",
        "relojes" to "Relojes",
        "gafas" to "Gafas de Sol",
        "sombreros" to "Sombreros",
        "muebles" to "Muebles",
        "decoracion" to "Decoración",
        "plantas" to "Plantas",
        "iluminacion" to "Iluminación",
        "smartphones" to "Smartphones",
        "laptops" to "Laptops",
        "audio" to "Audio",
        "gaming" to "Gaming"
    )
    
    // Backdrop con animación
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )
    }
    
    AnimatedVisibility(
        visible = isVisible && currentPost != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { }
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = HomeBg
            ) {
                // Estado del contenido: 0 = edición, 1 = eliminar, 2 = editor de imágenes
                val contentState = when {
                    showImageEditor -> 2
                    showDeleteConfirm -> 1
                    else -> 0
                }
                
                AnimatedContent(
                    targetState = contentState,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "edit_content"
                ) { state ->
                    when (state) {
                        0 -> {
                        // Contenido principal de edición
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                        ) {
                            // Handle
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(TextMuted.copy(alpha = 0.3f))
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Header con preview
                            EditPostHeader(
                                post = currentPost!!,
                                currentImages = editData.images,
                                onClose = onDismiss,
                                onEditImages = { showImageEditor = true }
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Contenido scrolleable
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // ══════════════════════════════════════════════
                                // ACCIONES RÁPIDAS
                                // ══════════════════════════════════════════════
                                QuickActionsRow(
                                    onPromote = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onPromote()
                                    },
                                    onStats = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onViewStats()
                                    },
                                    onShare = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // ══════════════════════════════════════════════
                                // INFORMACIÓN BÁSICA
                                // ══════════════════════════════════════════════
                                EditSectionHeader(
                                    icon = Icons.Outlined.Edit,
                                    title = "Información básica",
                                    iconColor = PrimaryPurple
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                EditPostField(
                                    label = "Título del producto",
                                    value = editData.title,
                                    onValueChange = { editData = editData.copy(title = it) },
                                    placeholder = "Ej: Remera algodón premium",
                                    icon = Icons.Outlined.ShoppingBag,
                                    maxChars = 80
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                EditPostField(
                                    label = "Descripción",
                                    value = editData.description,
                                    onValueChange = { editData = editData.copy(description = it) },
                                    placeholder = "Describe tu producto...",
                                    icon = Icons.Outlined.Description,
                                    multiline = true,
                                    maxLines = 4,
                                    maxChars = 500
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                // Selector de categoría
                                CategorySelector(
                                    selectedCategory = editData.category,
                                    categories = categories,
                                    categoryDisplayNames = categoryDisplayNames,
                                    onCategorySelected = { editData = editData.copy(category = it) }
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // ══════════════════════════════════════════════
                                // PRECIO Y OFERTAS
                                // ══════════════════════════════════════════════
                                EditSectionHeader(
                                    icon = Icons.Outlined.LocalOffer,
                                    title = "Precio y ofertas",
                                    iconColor = Color(0xFF2E8B57)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Card de precio con opciones avanzadas
                                PriceEditCard(
                                    price = editData.price,
                                    originalPrice = editData.originalPrice,
                                    showOriginalPrice = editData.showOriginalPrice,
                                    onPriceChange = { editData = editData.copy(price = it) },
                                    onOriginalPriceChange = { editData = editData.copy(originalPrice = it) },
                                    onShowOriginalPriceChange = { editData = editData.copy(showOriginalPrice = it) },
                                    onOpenSettings = { showPriceSettings = true }
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                // Toggle de ofertas
                                ToggleOption(
                                    icon = Icons.Outlined.Handshake,
                                    title = "Permitir ofertas",
                                    subtitle = "Los compradores pueden proponer precios",
                                    isEnabled = editData.allowOffers,
                                    onToggle = { editData = editData.copy(allowOffers = it) },
                                    iconColor = Color(0xFFFF6B35)
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Toggle de envío gratis
                                ToggleOption(
                                    icon = Icons.Outlined.LocalShipping,
                                    title = "Envío gratis",
                                    subtitle = "Ofrecer envío sin costo adicional",
                                    isEnabled = editData.freeShipping,
                                    onToggle = { editData = editData.copy(freeShipping = it) },
                                    iconColor = Color(0xFF1565A0)
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // ══════════════════════════════════════════════
                                // DISPONIBILIDAD
                                // ══════════════════════════════════════════════
                                EditSectionHeader(
                                    icon = Icons.Outlined.Inventory2,
                                    title = "Disponibilidad",
                                    iconColor = Color(0xFFFF6B35)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Estado de disponibilidad
                                AvailabilityCard(
                                    isAvailable = editData.isAvailable,
                                    onAvailabilityChange = { editData = editData.copy(isAvailable = it) }
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // ══════════════════════════════════════════════
                                // DETALLES ADICIONALES
                                // ══════════════════════════════════════════════
                                EditSectionHeader(
                                    icon = Icons.Outlined.Info,
                                    title = "Detalles adicionales",
                                    iconColor = Color(0xFF1565A0)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Selector de garantía
                                WarrantySelector(
                                    selectedWarranty = editData.warranty,
                                    onWarrantySelected = { editData = editData.copy(warranty = it) }
                                )
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                // Toggle de devoluciones
                                ToggleOption(
                                    icon = Icons.Outlined.Autorenew,
                                    title = "Acepta devoluciones",
                                    subtitle = "Los compradores pueden devolver el producto",
                                    isEnabled = editData.returnsAccepted,
                                    onToggle = { editData = editData.copy(returnsAccepted = it) },
                                    iconColor = Color(0xFF2E8B57)
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // ══════════════════════════════════════════════
                                // ZONA DE PELIGRO
                                // ══════════════════════════════════════════════
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { showDeleteConfirm = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.08f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444).copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Eliminar publicación",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFEF4444)
                                            )
                                            Text(
                                                text = "Esta acción no se puede deshacer",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                        
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            
                            // Botón guardar - Se oculta cuando el modal de precios está abierto
                            if (!showPriceSettings) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isSaving = true
                                        onSave(editData)
                                    },
                                    enabled = !isSaving && editData.title.isNotEmpty(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryPurple,
                                        disabledContainerColor = PrimaryPurple.copy(alpha = 0.3f)
                                    )
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Guardar cambios",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        }
                        1 -> {
                            // Pantalla de confirmación de eliminación
                            DeleteConfirmationContent(
                                postTitle = currentPost?.title ?: currentPost?.producto?.titulo ?: "esta publicación",
                                postImage = currentPost?.images?.firstOrNull() ?: currentPost?.producto?.imagenUrl?.firstOrNull(),
                                onCancel = { showDeleteConfirm = false },
                                onConfirm = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDelete()
                                }
                            )
                        }
                        2 -> {
                            // Editor de imágenes con asignación de colores
                            ImageEditorContent(
                                post = currentPost!!,
                                initialImageColors = editData.imageColors,
                                onBack = { showImageEditor = false },
                                onSaveImages = { newImages, newImageColors ->
                                    editData = editData.copy(
                                        images = newImages,
                                        imageColors = newImageColors
                                    )
                                    showImageEditor = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Modal de configuración de precio - Pantalla completa
    if (showPriceSettings) {
        PriceSettingsModal(
            currentPrice = editData.price,
            originalPrice = editData.originalPrice,
            showOriginalPrice = editData.showOriginalPrice,
            images = editData.images,
            variantPrices = editData.variantPrices,
            imageColors = editData.imageColors,
            onDismiss = { showPriceSettings = false },
            onSave = { newPrice, origPrice, showOrig, variantPrices ->
                editData = editData.copy(
                    price = newPrice,
                    originalPrice = origPrice,
                    showOriginalPrice = showOrig,
                    variantPrices = variantPrices
                )
                showPriceSettings = false
            }
        )
    }
}

@Composable
private fun EditPostHeader(
    post: Post,
    currentImages: List<String> = emptyList(),
    onClose: () -> Unit,
    onEditImages: () -> Unit = {}
) {
    // Use currentImages if available, otherwise fall back to post images
    val displayImage = currentImages.firstOrNull() 
        ?: post.images.firstOrNull() 
        ?: post.producto.imagenUrl.firstOrNull()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail del producto con botón de editar
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
                .clickable(onClick = onEditImages)
        ) {
            AsyncImage(
                model = displayImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Overlay con icono de editar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "Editar imágenes",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Editar publicación",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = post.title.ifEmpty { post.producto.titulo },
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
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

@Composable
private fun QuickActionsRow(
    onPromote: () -> Unit,
    onStats: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            icon = Icons.Outlined.TrendingUp,
            label = "Promocionar",
            color = Color(0xFFFF6B35),
            modifier = Modifier.weight(1f),
            onClick = onPromote
        )
        
        QuickActionButton(
            icon = Icons.Outlined.Analytics,
            label = "Estadísticas",
            color = Color(0xFF1565A0),
            modifier = Modifier.weight(1f),
            onClick = onStats
        )
        
        QuickActionButton(
            icon = Icons.Outlined.Share,
            label = "Compartir",
            color = Color(0xFF2E8B57),
            modifier = Modifier.weight(1f),
            onClick = onShare
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun EditSectionHeader(
    icon: ImageVector,
    title: String,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun EditPostField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    multiline: Boolean = false,
    maxLines: Int = 1,
    maxChars: Int? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
            if (maxChars != null) {
                Text(
                    text = "${value.length}/$maxChars",
                    color = if (value.length > maxChars) Color(0xFFEF4444) else TextMuted,
                    fontSize = 11.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = if (multiline) 12.dp else 0.dp)
                    .then(if (!multiline) Modifier.height(48.dp) else Modifier.heightIn(min = 80.dp)),
                verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(18.dp)
                        .then(if (multiline) Modifier.padding(top = 2.dp) else Modifier)
                )
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextMuted.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (maxChars == null || newValue.length <= maxChars) {
                                onValueChange(newValue)
                            }
                        },
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(PrimaryPurple),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = maxLines,
                        singleLine = !multiline
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceEditCard(
    price: Double,
    originalPrice: Double?,
    showOriginalPrice: Boolean,
    onPriceChange: (Double) -> Unit,
    onOriginalPriceChange: (Double?) -> Unit,
    onShowOriginalPriceChange: (Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    var priceText by remember(price) { mutableStateOf(if (price > 0) price.toLong().toString() else "") }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Campo de precio
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Precio actual",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E8B57)
                        )
                        
                        BasicTextField(
                            value = priceText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                                    priceText = newValue
                                    onPriceChange(newValue.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            cursorBrush = SolidColor(PrimaryPurple),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (priceText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted.copy(alpha = 0.3f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
                
                // Botón de configuración avanzada
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configuración de precio",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Mostrar precio original si está configurado
            if (showOriginalPrice && originalPrice != null && originalPrice > price) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalOffer,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Antes: ",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "$${originalPrice.toLong()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEF4444),
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val discount = ((originalPrice - price) / originalPrice * 100).toInt()
                            Text(
                                text = "-$discount%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E8B57)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    iconColor: Color
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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = iconColor,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Surface
                )
            )
        }
    }
}

@Composable
private fun AvailabilityCard(
    isAvailable: Boolean,
    onAvailabilityChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isAvailable) Color(0xFF2E8B57).copy(alpha = 0.08f) else Color(0xFFEF4444).copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAvailable) Color(0xFF2E8B57).copy(alpha = 0.15f)
                        else Color(0xFFEF4444).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAvailable) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                    contentDescription = null,
                    tint = if (isAvailable) Color(0xFF2E8B57) else Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAvailable) "Disponible" else "No disponible",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAvailable) Color(0xFF2E8B57) else Color(0xFFEF4444)
                )
                Text(
                    text = if (isAvailable) "Tu producto está visible para compradores"
                           else "Tu producto está oculto temporalmente",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            
            Switch(
                checked = isAvailable,
                onCheckedChange = onAvailabilityChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF2E8B57),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFEF4444)
                )
            )
        }
    }
}

@Composable
private fun DeleteConfirmationContent(
    postTitle: String,
    postImage: String?,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Icono de advertencia
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(44.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "¿Eliminar publicación?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Preview del post
        if (postImage != null) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = postImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Overlay con X
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        Text(
            text = "\"$postTitle\"",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Esta acción eliminará permanentemente tu publicación, incluyendo todas las imágenes, estadísticas y comentarios asociados.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = "Cancelar",
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Eliminar",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PriceSettingsModal(
    currentPrice: Double,
    originalPrice: Double?,
    showOriginalPrice: Boolean,
    images: List<String> = emptyList(),
    variantPrices: Map<Int, VariantPriceData> = emptyMap(),
    imageColors: Map<Int, String> = emptyMap(),
    onDismiss: () -> Unit,
    onSave: (newPrice: Double, originalPrice: Double?, showOriginal: Boolean, variantPrices: Map<Int, VariantPriceData>) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var priceText by remember { mutableStateOf(if (currentPrice > 0) currentPrice.toLong().toString() else "") }
    var originalPriceText by remember { mutableStateOf(originalPrice?.toLong()?.toString() ?: "") }
    var showOriginal by remember { mutableStateOf(showOriginalPrice) }
    
    // Estado para precios por variante
    val localVariantPrices = remember(variantPrices) { 
        variantPrices.toMutableMap().toMutableStateMap() 
    }
    
    // Estado para la variante seleccionada para editar
    var selectedVariantIndex by remember { mutableIntStateOf(-1) }
    var variantPriceText by remember { mutableStateOf("") }
    var variantOriginalPriceText by remember { mutableStateOf("") }
    
    // Actualizar campos cuando se selecciona una variante
    LaunchedEffect(selectedVariantIndex) {
        if (selectedVariantIndex >= 0) {
            val variantData = localVariantPrices[selectedVariantIndex]
            variantPriceText = variantData?.price?.toLong()?.toString() ?: ""
            variantOriginalPriceText = variantData?.originalPrice?.toLong()?.toString() ?: ""
        }
    }
    
    // Modal de pantalla completa
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header fijo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HomeBg,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Configuración de precios",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Configura precios base y por variante",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            // Contenido scrolleable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ══════════════════════════════════════════════
                // PRECIO BASE
                // ══════════════════════════════════════════════
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E8B57).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Precio base",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Aplica a todas las variantes sin precio específico",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Input de precio base
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = HomeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E8B57)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                BasicTextField(
                                    value = priceText,
                                    onValueChange = { newValue ->
                                        if (newValue.all { c -> c.isDigit() } && newValue.length <= 10) {
                                            priceText = newValue
                                        }
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    cursorBrush = SolidColor(PrimaryPurple),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (priceText.isEmpty()) {
                                                Text(
                                                    text = "0",
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextMuted.copy(alpha = 0.3f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Toggle precio anterior
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(HomeBg)
                                .clickable { showOriginal = !showOriginal }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mostrar precio anterior",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Muestra descuento tachado",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            
                            Switch(
                                checked = showOriginal,
                                onCheckedChange = { showOriginal = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2E8B57)
                                )
                            )
                        }
                        
                        // Campo precio anterior
                        AnimatedVisibility(
                            visible = showOriginal,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = HomeBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "$",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444),
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        BasicTextField(
                                            value = originalPriceText,
                                            onValueChange = { newValue ->
                                                if (newValue.all { c -> c.isDigit() } && newValue.length <= 10) {
                                                    originalPriceText = newValue
                                                }
                                            },
                                            textStyle = TextStyle(
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted,
                                                textDecoration = TextDecoration.LineThrough
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            cursorBrush = SolidColor(PrimaryPurple),
                                            decorationBox = { innerTextField ->
                                                Box {
                                                    if (originalPriceText.isEmpty()) {
                                                        Text(
                                                            text = "Precio anterior",
                                                            fontSize = 24.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextMuted.copy(alpha = 0.3f),
                                                            textDecoration = TextDecoration.LineThrough
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                
                                // Preview descuento
                                val newPrice = priceText.toDoubleOrNull() ?: 0.0
                                val oldPrice = originalPriceText.toDoubleOrNull()
                                if (oldPrice != null && oldPrice > newPrice && newPrice > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val discount = ((oldPrice - newPrice) / oldPrice * 100).toInt()
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF2E8B57).copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.TrendingDown,
                                                contentDescription = null,
                                                tint = Color(0xFF2E8B57),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Descuento del $discount%",
                                                fontSize = 12.sp,
                                                color = Color(0xFF2E8B57),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // ══════════════════════════════════════════════
                // PRECIOS POR VARIANTE
                // ══════════════════════════════════════════════
                if (images.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Precios por variante",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Opcional: asigna precios diferentes a cada variante",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Grid de variantes
                    images.forEachIndexed { index, imageUrl ->
                        val variantData = localVariantPrices[index]
                        val hasCustomPrice = variantData?.price != null
                        val colorName = imageColors[index]
                        val basePrice = priceText.toDoubleOrNull() ?: 0.0
                        val displayPrice = variantData?.price ?: basePrice
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedVariantIndex = if (selectedVariantIndex == index) -1 else index
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = Surface,
                            border = if (selectedVariantIndex == index) 
                                androidx.compose.foundation.BorderStroke(2.dp, PrimaryPurple)
                            else null
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail más alto que ancho
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        
                                        // Número de variante
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(4.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.Black.copy(alpha = 0.7f)
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                        
                                        // Indicador de color si tiene
                                        if (colorName != null) {
                                            val color = availableProductColors.find { it.first == colorName }?.second
                                            if (color != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(4.dp)
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                        .border(1.dp, Color.White, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Variante ${index + 1}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        if (colorName != null) {
                                            Text(
                                                text = colorName,
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "$${displayPrice.toLong()}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hasCustomPrice) PrimaryPurple else Color(0xFF2E8B57)
                                            )
                                            if (hasCustomPrice) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = PrimaryPurple.copy(alpha = 0.1f)
                                                ) {
                                                    Text(
                                                        text = "Personalizado",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = PrimaryPurple,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Precio base",
                                                    fontSize = 11.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Botón editar/eliminar precio
                                    if (hasCustomPrice) {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                localVariantPrices.remove(index)
                                                if (selectedVariantIndex == index) {
                                                    selectedVariantIndex = -1
                                                }
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Eliminar precio",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selectedVariantIndex == index) 
                                                Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                
                                // Panel de edición expandido
                                AnimatedVisibility(
                                    visible = selectedVariantIndex == index,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(HomeBg)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Precio para esta variante",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextMuted
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Input precio variante
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp),
                                                color = Surface
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "$",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryPurple
                                                    )
                                                    BasicTextField(
                                                        value = variantPriceText,
                                                        onValueChange = { newValue ->
                                                            if (newValue.all { c -> c.isDigit() } && newValue.length <= 10) {
                                                                variantPriceText = newValue
                                                            }
                                                        },
                                                        textStyle = TextStyle(
                                                            fontSize = 18.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary
                                                        ),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        cursorBrush = SolidColor(PrimaryPurple),
                                                        decorationBox = { innerTextField ->
                                                            Box {
                                                                if (variantPriceText.isEmpty()) {
                                                                    Text(
                                                                        text = "Precio",
                                                                        fontSize = 18.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = TextMuted.copy(alpha = 0.3f)
                                                                    )
                                                                }
                                                                innerTextField()
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            
                                            // Botón aplicar
                                            Button(
                                                onClick = {
                                                    val price = variantPriceText.toDoubleOrNull()
                                                    if (price != null && price > 0) {
                                                        localVariantPrices[index] = VariantPriceData(
                                                            imageUrl = imageUrl,
                                                            colorName = colorName,
                                                            price = price,
                                                            originalPrice = variantOriginalPriceText.toDoubleOrNull()
                                                        )
                                                        selectedVariantIndex = -1
                                                        variantPriceText = ""
                                                    }
                                                },
                                                enabled = variantPriceText.isNotEmpty(),
                                                modifier = Modifier.height(48.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PrimaryPurple,
                                                    disabledContainerColor = PrimaryPurple.copy(alpha = 0.3f)
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Info
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1565A0).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color(0xFF1565A0),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Las variantes sin precio personalizado usarán el precio base",
                                fontSize = 12.sp,
                                color = Color(0xFF1565A0),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            // Botón guardar fijo abajo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HomeBg,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        val origPrice = if (showOriginal) originalPriceText.toDoubleOrNull() else null
                        onSave(price, origPrice, showOriginal, localVariantPrices.toMap())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar configuración",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Extension para mutableStateMap (NO debe ser @Composable)
private fun <K, V> Map<K, V>.toMutableStateMap(): androidx.compose.runtime.snapshots.SnapshotStateMap<K, V> {
    return androidx.compose.runtime.mutableStateMapOf<K, V>().also { it.putAll(this) }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    categories: List<Pair<String, ImageVector>>,
    categoryDisplayNames: Map<String, String> = emptyMap(),
    onCategorySelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // Get display name for selected category
    val selectedDisplayName = categoryDisplayNames[selectedCategory] ?: selectedCategory
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Categoría",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(12.dp),
            color = Surface
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = null,
                        tint = PrimaryPurple.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = if (selectedCategory.isEmpty()) "Seleccionar categoría" else selectedDisplayName,
                        fontSize = 14.sp,
                        color = if (selectedCategory.isEmpty()) TextMuted.copy(alpha = 0.5f) else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                        
                        categories.forEach { (categoryId, icon) ->
                            val isSelected = selectedCategory == categoryId
                            val displayName = categoryDisplayNames[categoryId] ?: categoryId
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onCategorySelected(categoryId)
                                        isExpanded = false
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryPurple.copy(alpha = 0.12f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) PrimaryPurple else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                    
                                    Text(
                                        text = displayName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) PrimaryPurple else TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// WARRANTY SELECTOR
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun WarrantySelector(
    selectedWarranty: String,
    onWarrantySelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val warrantyOptions = listOf(
        "Sin garantía",
        "7 días",
        "15 días",
        "30 días",
        "3 meses",
        "6 meses",
        "12 meses",
        "24 meses",
        "Garantía de por vida"
    )
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Garantía",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(12.dp),
            color = Surface
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = Color(0xFF1565A0),
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Text(
                        text = selectedWarranty,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                        
                        warrantyOptions.forEach { option ->
                            val isSelected = selectedWarranty == option
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onWarrantySelected(option)
                                        isExpanded = false
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF1565A0).copy(alpha = 0.12f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF1565A0) else TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF1565A0),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COLOR SELECTOR
// ═══════════════════════════════════════════════════════════════════════════════
private val availableProductColors = listOf(
    "Negro" to Color(0xFF1A1A1A),
    "Blanco" to Color(0xFFF5F5F5),
    "Gris" to Color(0xFF6B7280),
    "Rojo" to Color(0xFFEF4444),
    "Azul" to Color(0xFF1565A0),
    "Verde" to Color(0xFF2E8B57),
    "Amarillo" to Color(0xFFFF6B35),
    "Naranja" to Color(0xFFF97316),
    "Rosa" to Color(0xFF2E8B57),
    "Morado" to Color(0xFFFF6B35),
    "Marrón" to Color(0xFF92400E),
    "Beige" to Color(0xFFD4C4A8)
)

@Composable
private fun ColorSelector(
    selectedColors: List<String>,
    onColorsChanged: (List<String>) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Colores disponibles",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(12.dp),
            color = Surface
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = Color(0xFF2E8B57),
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    if (selectedColors.isEmpty()) {
                        Text(
                            text = "Seleccionar colores",
                            fontSize = 14.sp,
                            color = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedColors.take(6).forEach { colorName ->
                                val color = availableProductColors.find { it.first == colorName }?.second ?: Color.Gray
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(1.dp, BorderSubtle, CircleShape)
                                )
                            }
                            if (selectedColors.size > 6) {
                                Text(
                                    text = "+${selectedColors.size - 6}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
                        
                        // Grid de colores 4x3
                        val chunkedColors = availableProductColors.chunked(4)
                        chunkedColors.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { (colorName, color) ->
                                    val isSelected = selectedColors.contains(colorName)
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val newColors = if (isSelected) {
                                                    selectedColors - colorName
                                                } else {
                                                    selectedColors + colorName
                                                }
                                                onColorsChanged(newColors)
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) PrimaryPurple else BorderSubtle,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (colorName == "Negro" || colorName == "Azul" || colorName == "Verde" || colorName == "Morado" || colorName == "Marrón") Color.White else Color.Black,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = colorName,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) PrimaryPurple else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// IMAGE EDITOR CONTENT - With numbered reorder, full functionality and color assignment
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ImageEditorContent(
    post: Post,
    initialImageColors: Map<Int, String> = emptyMap(),
    onBack: () -> Unit,
    onSaveImages: (List<String>, Map<Int, String>) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    val images = remember(post) { 
        (post.images.ifEmpty { post.producto.imagenUrl }).toMutableStateList()
    }
    
    // Estado para colores asignados a imágenes
    val imageColors = remember(initialImageColors) {
        initialImageColors.toMutableMap().toMutableStateMap()
    }
    
    // Estado para mostrar selector de color
    var showColorPickerForIndex by remember { mutableIntStateOf(-1) }
    
    // State for reordering
    var showReorderDialog by remember { mutableStateOf(false) }
    var reorderingImageIndex by remember { mutableIntStateOf(-1) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            if (images.size < 10) {
                images.add(uri.toString())
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TextMuted.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header con botón atrás
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Surface)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Editar imágenes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${images.size}/10 imágenes",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            
            // Botón agregar
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar imagen",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Grid de imágenes con números
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (images.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .border(2.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Agregar imágenes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Máximo 10 imágenes",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                // Imagen principal grande
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = images.first(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Gradient overlay for badges
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                    
                    // Badge de principal con número
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryPurple
                        ) {
                            Text(
                                text = "1",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E8B57)
                        ) {
                            Text(
                                text = "Principal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    // Botón eliminar (solo si hay más de 1 imagen)
                    if (images.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    images.removeAt(0)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    // Botón para asignar color a imagen principal
                    val assignedColor = imageColors[0]
                    val colorValue = assignedColor?.let { colorName ->
                        availableProductColors.find { it.first == colorName }?.second
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showColorPickerForIndex = 0
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (colorValue != null) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(colorValue)
                                        .border(1.dp, Color.White, CircleShape)
                                )
                                Text(
                                    text = assignedColor,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Color",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Grid de imágenes secundarias
                if (images.size > 1) {
                    val secondaryImages = images.drop(1)
                    val rows = secondaryImages.chunked(3)
                    
                    rows.forEachIndexed { rowIndex, rowImages ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowImages.forEachIndexed { colIndex, imageUrl ->
                                val actualIndex = 1 + (rowIndex * 3) + colIndex
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    // Número de posición - clickable para reordenar
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                reorderingImageIndex = actualIndex
                                                showReorderDialog = true
                                            },
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.Black.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = "${actualIndex + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    
                                    // Botón eliminar
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                images.removeAt(actualIndex)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    
                                    // Botón para hacer principal
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryPurple.copy(alpha = 0.9f))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val img = images.removeAt(actualIndex)
                                                images.add(0, img)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Text(
                                                text = "Principal",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    
                                    // Botón para asignar color a imagen secundaria
                                    val secAssignedColor = imageColors[actualIndex]
                                    val secColorValue = secAssignedColor?.let { colorName ->
                                        availableProductColors.find { it.first == colorName }?.second
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (secColorValue != null) secColorValue 
                                                else Color.Black.copy(alpha = 0.6f)
                                            )
                                            .border(
                                                width = if (secColorValue != null) 2.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showColorPickerForIndex = actualIndex
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (secColorValue == null) {
                                            Icon(
                                                imageVector = Icons.Outlined.Palette,
                                                contentDescription = "Asignar color",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Fill empty spaces in the row
                            repeat(3 - rowImages.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                // Botón agregar más imágenes
                if (images.size < 10) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        color = Surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agregar más imágenes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryPurple
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1565A0).copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color(0xFF1565A0),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Consejos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1565A0)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• Toca el número para cambiar el orden\n• Toca ★ para hacer una imagen principal",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón guardar
        Button(
            onClick = { onSaveImages(images.toList(), imageColors.toMap()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Guardar cambios",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    // Modal de selector de color para imagen
    if (showColorPickerForIndex >= 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showColorPickerForIndex = -1 },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Asignar color a imagen ${showColorPickerForIndex + 1}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Este color representará esta variante",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Grid de colores disponibles
                    val rows = availableProductColors.chunked(5)
                    rows.forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowColors.forEach { (colorName, color) ->
                                val isSelected = imageColors[showColorPickerForIndex] == colorName
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            imageColors[showColorPickerForIndex] = colorName
                                            showColorPickerForIndex = -1
                                        }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.5.dp,
                                                color = if (isSelected) PrimaryPurple else BorderSubtle,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = colorName,
                                        fontSize = 10.sp,
                                        color = if (isSelected) PrimaryPurple else TextMuted,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                            repeat(5 - rowColors.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Opción para quitar color
                    if (imageColors.containsKey(showColorPickerForIndex)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    imageColors.remove(showColorPickerForIndex)
                                    showColorPickerForIndex = -1
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Quitar color asignado",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    
    // Reorder dialog
    if (showReorderDialog && reorderingImageIndex >= 0) {
        AlertDialog(
            onDismissRequest = { 
                showReorderDialog = false
                reorderingImageIndex = -1
            },
            title = {
                Text(
                    text = "Cambiar posición",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Selecciona la nueva posición para esta imagen:",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Grid de posiciones
                    val positions = (1..images.size).toList()
                    val rows = positions.chunked(5)
                    
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { position ->
                                val isCurrentPosition = position == reorderingImageIndex + 1
                                
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (!isCurrentPosition) {
                                                val newIndex = position - 1
                                                val img = images.removeAt(reorderingImageIndex)
                                                images.add(newIndex, img)
                                            }
                                            showReorderDialog = false
                                            reorderingImageIndex = -1
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrentPosition) PrimaryPurple else Surface,
                                    border = if (!isCurrentPosition) 
                                        androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle) 
                                    else null
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$position",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentPosition) Color.White else TextPrimary
                                        )
                                    }
                                }
                            }
                            
                            // Fill empty spaces
                            repeat(5 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReorderDialog = false
                    reorderingImageIndex = -1
                }) {
                    Text("Cancelar", color = PrimaryPurple)
                }
            }
        )
    }
}
