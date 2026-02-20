package com.rendly.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * STORE CONFIG SCREEN - Configuración de tienda profesional
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Esta pantalla permite al usuario configurar si tiene una tienda física/online
 * o si es un vendedor casual. Dependiendo de la selección, se mostrarán
 * diferentes opciones en ProductPage y tarjetas de artículos.
 * 
 * - CON TIENDA: Más colores, condición, categoría, disponibilidad, garantía, etc.
 * - SIN TIENDA: Versión reducida pero igualmente profesional
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

data class StoreTypeOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val benefits: List<String>,
    val accentColor: Color,
    val isPro: Boolean = false
)

private val STORE_TYPE_OPTIONS = listOf(
    StoreTypeOption(
        id = "full_store",
        title = "Tienda Completa",
        description = "Tengo una tienda física u online con inventario de productos",
        icon = Icons.Outlined.Storefront,
        benefits = listOf(
            "Múltiples variantes de color y talle",
            "Gestión de stock e inventario",
            "Categorías y subcategorías",
            "Garantía y políticas de devolución",
            "Estadísticas avanzadas de ventas",
            "Badge de tienda verificada"
        ),
        accentColor = AccentPink,
        isPro = true
    ),
    StoreTypeOption(
        id = "casual_seller",
        title = "Vendedor Casual",
        description = "Vendo artículos ocasionalmente sin tienda formal",
        icon = Icons.Outlined.Person,
        benefits = listOf(
            "Publicación rápida y sencilla",
            "Perfil de vendedor confiable",
            "Chat directo con compradores",
            "Sistema de reputación",
            "Sin comisiones adicionales"
        ),
        accentColor = PrimaryPurple,
        isPro = false
    )
)

@Composable
fun StoreConfigScreen(
    onClose: () -> Unit,
    onStoreConfigured: (hasStore: Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedStoreType by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableIntStateOf(0) }
    var storeName by remember { mutableStateOf("") }
    var storeDescription by remember { mutableStateOf("") }
    var storeCategory by remember { mutableStateOf("") }
    var hasPhysicalLocation by remember { mutableStateOf(false) }
    var acceptsReturns by remember { mutableStateOf(true) }
    var offersWarranty by remember { mutableStateOf(false) }
    var warrantyDays by remember { mutableStateOf("30") }
    var shipsNationwide by remember { mutableStateOf(true) }
    var acceptsMercadoPago by remember { mutableStateOf(true) }
    var acceptsCash by remember { mutableStateOf(true) }
    var acceptsTransfer by remember { mutableStateOf(true) }
    
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            StoreConfigHeader(
                currentStep = currentStep,
                totalSteps = if (selectedStoreType == "full_store") 4 else 2,
                onBack = {
                    if (currentStep > 0) {
                        currentStep--
                    } else {
                        onClose()
                    }
                },
                onClose = onClose
            )
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        slideInHorizontally { it } + fadeIn() togetherWith 
                        slideOutHorizontally { -it } + fadeOut()
                    },
                    label = "stepAnimation"
                ) { step ->
                    when (step) {
                        0 -> StoreTypeSelection(
                            selectedType = selectedStoreType,
                            onTypeSelected = { selectedStoreType = it }
                        )
                        1 -> if (selectedStoreType == "full_store") {
                            StoreBasicInfo(
                                storeName = storeName,
                                onStoreNameChange = { storeName = it },
                                storeDescription = storeDescription,
                                onStoreDescriptionChange = { storeDescription = it },
                                storeCategory = storeCategory,
                                onStoreCategoryChange = { storeCategory = it }
                            )
                        } else {
                            CasualSellerInfo()
                        }
                        2 -> StoreLocationAndShipping(
                            hasPhysicalLocation = hasPhysicalLocation,
                            onPhysicalLocationChange = { hasPhysicalLocation = it },
                            shipsNationwide = shipsNationwide,
                            onShipsNationwideChange = { shipsNationwide = it }
                        )
                        3 -> StorePoliciesAndPayments(
                            acceptsReturns = acceptsReturns,
                            onAcceptsReturnsChange = { acceptsReturns = it },
                            offersWarranty = offersWarranty,
                            onOffersWarrantyChange = { offersWarranty = it },
                            warrantyDays = warrantyDays,
                            onWarrantyDaysChange = { warrantyDays = it },
                            acceptsMercadoPago = acceptsMercadoPago,
                            onAcceptsMercadoPagoChange = { acceptsMercadoPago = it },
                            acceptsCash = acceptsCash,
                            onAcceptsCashChange = { acceptsCash = it },
                            acceptsTransfer = acceptsTransfer,
                            onAcceptsTransferChange = { acceptsTransfer = it }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            // Bottom action button
            StoreConfigBottomAction(
                currentStep = currentStep,
                totalSteps = if (selectedStoreType == "full_store") 4 else 2,
                canContinue = when (currentStep) {
                    0 -> selectedStoreType != null
                    1 -> if (selectedStoreType == "full_store") storeName.isNotBlank() else true
                    else -> true
                },
                onContinue = {
                    val totalSteps = if (selectedStoreType == "full_store") 4 else 2
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        onStoreConfigured(selectedStoreType == "full_store")
                        onClose()
                    }
                },
                isLastStep = currentStep == (if (selectedStoreType == "full_store") 3 else 1)
            )
        }
    }
}

@Composable
private fun StoreConfigHeader(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HomeBg)
            ) {
                Icon(
                    imageVector = if (currentStep == 0) Icons.Default.Close else Icons.Default.ArrowBack,
                    contentDescription = if (currentStep == 0) "Cerrar" else "Volver",
                    tint = TextPrimary
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Configura tu tienda",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Paso ${currentStep + 1} de $totalSteps",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HomeBg)
            ) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "Ayuda",
                    tint = TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = (currentStep + 1).toFloat() / totalSteps,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = AccentPink,
            trackColor = HomeBg
        )
    }
}

@Composable
private fun StoreTypeSelection(
    selectedType: String?,
    onTypeSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "¿Cómo vendes?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Elige el tipo de vendedor que mejor te represente. Esto determinará las opciones disponibles en tus publicaciones.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        STORE_TYPE_OPTIONS.forEach { option ->
            StoreTypeCard(
                option = option,
                isSelected = selectedType == option.id,
                onClick = { onTypeSelected(option.id) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StoreTypeCard(
    option: StoreTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) option.accentColor else Color.Transparent,
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) option.accentColor.copy(alpha = 0.08f) else Surface,
        animationSpec = tween(200),
        label = "backgroundColor"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        option.accentColor,
                                        option.accentColor.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (option.isPro) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = option.accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "PRO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = option.accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = option.description,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
                
                // Radio indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) option.accentColor else TextMuted,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(option.accentColor)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = HomeBg, thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Benefits
            Text(
                text = "BENEFICIOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            option.benefits.forEach { benefit ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = option.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = benefit,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreBasicInfo(
    storeName: String,
    onStoreNameChange: (String) -> Unit,
    storeDescription: String,
    onStoreDescriptionChange: (String) -> Unit,
    storeCategory: String,
    onStoreCategoryChange: (String) -> Unit
) {
    val categories = listOf(
        "Ropa y Accesorios", "Calzado", "Tecnología", "Hogar y Decoración",
        "Belleza y Cuidado Personal", "Deportes", "Joyería y Bijouterie",
        "Arte y Manualidades", "Libros y Papelería", "Otros"
    )
    var expandedCategory by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Información de tu tienda",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Esta información será visible para tus compradores y ayudará a generar confianza.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Store name
        Text(
            text = "NOMBRE DE LA TIENDA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = storeName,
            onValueChange = onStoreNameChange,
            placeholder = { Text("Ej: Fashion Store", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPink,
                unfocusedBorderColor = Surface,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = AccentPink,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Store,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Store description
        Text(
            text = "DESCRIPCIÓN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = storeDescription,
            onValueChange = onStoreDescriptionChange,
            placeholder = { Text("Cuéntale a tus clientes sobre tu tienda...", color = TextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPink,
                unfocusedBorderColor = Surface,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = AccentPink,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Category dropdown
        Text(
            text = "CATEGORÍA PRINCIPAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = it }
        ) {
            OutlinedTextField(
                value = storeCategory,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Selecciona una categoría", color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPink,
                    unfocusedBorderColor = Surface,
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Category,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            )
            
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false },
                modifier = Modifier.background(Surface)
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category, color = TextPrimary) },
                        onClick = {
                            onStoreCategoryChange(category)
                            expandedCategory = false
                        },
                        leadingIcon = {
                            if (storeCategory == category) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentPink
                                )
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pro tip card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = AccentPink.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = AccentPink,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Consejo PRO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Un nombre memorable y una descripción clara aumentan la confianza de tus compradores hasta un 40%.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CasualSellerInfo() {
    Column {
        Text(
            text = "¡Perfecto!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Como vendedor casual, tendrás acceso a todas las herramientas esenciales para vender de forma rápida y segura.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Features
        val features = listOf(
            Triple(Icons.Outlined.CameraAlt, "Publica en segundos", "Sube fotos y describe tu producto rápidamente"),
            Triple(Icons.Outlined.Chat, "Chat directo", "Comunícate con compradores interesados"),
            Triple(Icons.Outlined.Shield, "Transacciones seguras", "Protegemos tu dinero y tus datos"),
            Triple(Icons.Outlined.Star, "Sistema de reputación", "Construye confianza con cada venta")
        )
        
        features.forEach { (icon, title, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Upgrade notice
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = PrimaryPurple.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¿Empezaste a vender más?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Actualiza a Tienda Completa cuando quieras",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreLocationAndShipping(
    hasPhysicalLocation: Boolean,
    onPhysicalLocationChange: (Boolean) -> Unit,
    shipsNationwide: Boolean,
    onShipsNationwideChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Ubicación y envíos",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Configura dónde se encuentra tu tienda y cómo realizas los envíos.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Physical location toggle
        SettingToggleCard(
            icon = Icons.Outlined.LocationOn,
            iconColor = Color(0xFF1565A0),
            title = "Tengo local físico",
            description = "Los compradores podrán visitarte o retirar en persona",
            isEnabled = hasPhysicalLocation,
            onToggle = onPhysicalLocationChange
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Nationwide shipping toggle
        SettingToggleCard(
            icon = Icons.Outlined.LocalShipping,
            iconColor = Color(0xFF2E8B57),
            title = "Envíos a todo el país",
            description = "Realizo envíos a cualquier parte del país",
            isEnabled = shipsNationwide,
            onToggle = onShipsNationwideChange
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Shipping methods
        Text(
            text = "MÉTODOS DE ENVÍO DISPONIBLES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val shippingMethods = listOf(
            Triple("Correo Argentino", Icons.Outlined.LocalPostOffice, true),
            Triple("Mercado Envíos", Icons.Outlined.Inventory2, true),
            Triple("Moto / Flex", Icons.Outlined.TwoWheeler, false),
            Triple("Retiro en sucursal", Icons.Outlined.Store, hasPhysicalLocation)
        )
        
        shippingMethods.forEach { (name, icon, defaultEnabled) ->
            var enabled by remember { mutableStateOf(defaultEnabled) }
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { enabled = !enabled },
                shape = RoundedCornerShape(12.dp),
                color = if (enabled) AccentPink.copy(alpha = 0.08f) else Surface
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) AccentPink else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentPink,
                            uncheckedColor = TextMuted
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StorePoliciesAndPayments(
    acceptsReturns: Boolean,
    onAcceptsReturnsChange: (Boolean) -> Unit,
    offersWarranty: Boolean,
    onOffersWarrantyChange: (Boolean) -> Unit,
    warrantyDays: String,
    onWarrantyDaysChange: (String) -> Unit,
    acceptsMercadoPago: Boolean,
    onAcceptsMercadoPagoChange: (Boolean) -> Unit,
    acceptsCash: Boolean,
    onAcceptsCashChange: (Boolean) -> Unit,
    acceptsTransfer: Boolean,
    onAcceptsTransferChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Políticas y pagos",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Define tus políticas de devolución, garantía y métodos de pago aceptados.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // Returns policy
        SettingToggleCard(
            icon = Icons.Outlined.Replay,
            iconColor = Color(0xFFFF6B35),
            title = "Acepto devoluciones",
            description = "Los compradores pueden devolver productos",
            isEnabled = acceptsReturns,
            onToggle = onAcceptsReturnsChange
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Warranty
        SettingToggleCard(
            icon = Icons.Outlined.Verified,
            iconColor = Color(0xFF2E8B57),
            title = "Ofrezco garantía",
            description = "Garantía por defectos de fabricación",
            isEnabled = offersWarranty,
            onToggle = onOffersWarrantyChange
        )
        
        AnimatedVisibility(visible = offersWarranty) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = warrantyDays,
                    onValueChange = { if (it.all { c -> c.isDigit() }) onWarrantyDaysChange(it) },
                    label = { Text("Días de garantía", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPink,
                        unfocusedBorderColor = Surface,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        cursorColor = AccentPink,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    suffix = { Text("días", color = TextMuted) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Payment methods
        Text(
            text = "MÉTODOS DE PAGO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PaymentMethodToggle(
            name = "Mercado Pago",
            icon = Icons.Outlined.AccountBalanceWallet,
            color = Color(0xFF00B1EA),
            isEnabled = acceptsMercadoPago,
            onToggle = onAcceptsMercadoPagoChange,
            badge = "Recomendado"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        PaymentMethodToggle(
            name = "Efectivo",
            icon = Icons.Outlined.Money,
            color = Color(0xFF2E8B57),
            isEnabled = acceptsCash,
            onToggle = onAcceptsCashChange
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        PaymentMethodToggle(
            name = "Transferencia bancaria",
            icon = Icons.Outlined.AccountBalance,
            color = Color(0xFF6366F1),
            isEnabled = acceptsTransfer,
            onToggle = onAcceptsTransferChange
        )
    }
}

@Composable
private fun SettingToggleCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) },
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentPink,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = HomeBg
                )
            )
        }
    }
}

@Composable
private fun PaymentMethodToggle(
    name: String,
    icon: ImageVector,
    color: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    badge: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) },
        shape = RoundedCornerShape(12.dp),
        color = if (isEnabled) color.copy(alpha = 0.08f) else Surface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = color,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = HomeBg
                )
            )
        }
    }
}

@Composable
private fun StoreConfigBottomAction(
    currentStep: Int,
    totalSteps: Int,
    canContinue: Boolean,
    onContinue: () -> Unit,
    isLastStep: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Button(
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPink,
                    disabledContainerColor = AccentPink.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (isLastStep) "Guardar configuración" else "Continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (!isLastStep) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
