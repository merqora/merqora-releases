package com.rendly.app.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*

data class EditProfileData(
    var nombre: String = "",
    var username: String = "",
    var descripcion: String = "",
    var ubicacion: String = "",
    var telefono: String = "",
    var sitioWeb: String = "",
    var nombreTienda: String = "",
    var sexo: String = "", // "masculino", "femenino", "otro", ""
    var avatarUrl: String? = null,
    var bannerUrl: String? = null,
    var accountType: String = "casual" // "casual", "brand", "community", "product_service", etc.
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    initialData: EditProfileData = EditProfileData(
        nombre = "Tu Nombre",
        username = "tu_usuario",
        descripcion = "Amante de la moda y el estilo",
        ubicacion = "Buenos Aires, Argentina"
    ),
    isSaving: Boolean = false,
    onSave: (EditProfileData, Uri?, Uri?) -> Unit = { _, _, _ -> },
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var profileData by remember { mutableStateOf(initialData) }
    var hasImageChanges by remember { mutableStateOf(false) }
    
    // Estado para modal de tipo de cuenta
    var showAccountTypeModal by remember { mutableStateOf(false) }
    
    // Estado para modal de información personal
    var showPersonalInfoModal by remember { mutableStateOf(false) }
    
    // Detectar cambios usando derivedStateOf para reactividad correcta
    val hasChanges by remember {
        derivedStateOf {
            profileData.nombre != initialData.nombre ||
            profileData.username != initialData.username ||
            profileData.descripcion != initialData.descripcion ||
            profileData.ubicacion != initialData.ubicacion ||
            profileData.telefono != initialData.telefono ||
            profileData.sitioWeb != initialData.sitioWeb ||
            profileData.nombreTienda != initialData.nombreTienda ||
            profileData.sexo != initialData.sexo ||
            profileData.accountType != initialData.accountType
        }
    }
    val canSave = hasChanges || hasImageChanges
    
    // Image pickers
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBannerUri by remember { mutableStateOf<Uri?>(null) }
    
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedAvatarUri = it
            hasImageChanges = true
        }
    }
    
    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedBannerUri = it
            hasImageChanges = true
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Custom Header
            EditProfileHeader(
                canSave = canSave,
                isSaving = isSaving,
                onBack = onBack,
                onSave = { onSave(profileData, selectedAvatarUri, selectedBannerUri) }
            )
            
            // Banner + Avatar Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { bannerLauncher.launch("image/*") }
                ) {
                    if (selectedBannerUri != null) {
                        AsyncImage(
                            model = selectedBannerUri,
                            contentDescription = "Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (profileData.bannerUrl != null) {
                        AsyncImage(
                            model = profileData.bannerUrl,
                            contentDescription = "Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryPurple.copy(alpha = 0.5f),
                                            AccentPink.copy(alpha = 0.4f),
                                            Color(0xFF1A1A2E)
                                        )
                                    )
                                )
                        )
                    }
                    
                    // Edit banner overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cambiar portada",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Avatar superpuesto
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 16.dp, y = 45.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .border(
                                width = 4.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryPurple, AccentPink)
                                ),
                                shape = CircleShape
                            )
                            .background(HomeBg)
                            .padding(4.dp)
                            .clickable { avatarLauncher.launch("image/*") }
                    ) {
                        if (selectedAvatarUri != null) {
                            AsyncImage(
                                model = selectedAvatarUri,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            AsyncImage(
                                model = profileData.avatarUrl
                                    ?: "https://ui-avatars.com/api/?name=${profileData.username}&background=A78BFA&color=fff",
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        
                        // Camera overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Form Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // ═══════════════════════════════════════════════════════════
                // INFORMACIÓN BÁSICA
                // ═══════════════════════════════════════════════════════════
                SectionHeader(
                    icon = Icons.Outlined.Person,
                    title = "Información básica"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                EditFieldPro(
                    label = "Nombre",
                    value = profileData.nombre,
                    onValueChange = { profileData = profileData.copy(nombre = it) },
                    placeholder = "Tu nombre",
                    icon = Icons.Outlined.Badge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                EditFieldPro(
                    label = "Nombre de usuario",
                    value = profileData.username,
                    onValueChange = { profileData = profileData.copy(username = it) },
                    placeholder = "usuario",
                    icon = Icons.Outlined.AlternateEmail,
                    prefix = "@"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de Género inclusivo
                GenderSelector(
                    selectedGender = profileData.sexo,
                    onGenderSelected = { profileData = profileData.copy(sexo = it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                EditFieldPro(
                    label = "Biografía",
                    value = profileData.descripcion,
                    onValueChange = { profileData = profileData.copy(descripcion = it) },
                    placeholder = "Cuéntanos sobre ti o tu negocio...",
                    icon = Icons.Outlined.Edit,
                    multiline = true,
                    maxLines = 4,
                    maxChars = 150
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Botón Configurar información personal
                ConfigButton(
                    icon = Icons.Outlined.ManageAccounts,
                    title = "Configurar información personal",
                    subtitle = "Fecha de nacimiento, país, idioma",
                    onClick = { showPersonalInfoModal = true }
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // ═══════════════════════════════════════════════════════════
                // NEGOCIO / TIENDA
                // ═══════════════════════════════════════════════════════════
                SectionHeader(
                    icon = Icons.Outlined.Storefront,
                    title = "Tu negocio"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                EditFieldPro(
                    label = "Nombre de tienda / marca",
                    value = profileData.nombreTienda,
                    onValueChange = { profileData = profileData.copy(nombreTienda = it) },
                    placeholder = "Ej: Fashion Store",
                    icon = Icons.Outlined.Store
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // ═══════════════════════════════════════════════════════════
                // REDES Y WEB
                // ═══════════════════════════════════════════════════════════
                SectionHeader(
                    icon = Icons.Outlined.Language,
                    title = "Web y redes"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                EditFieldPro(
                    label = "Sitio web",
                    value = profileData.sitioWeb,
                    onValueChange = { profileData = profileData.copy(sitioWeb = it) },
                    placeholder = "www.tutienda.com",
                    icon = Icons.Outlined.Language
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // ═══════════════════════════════════════════════════════════
                // TIPO DE CUENTA - Al final
                // ═══════════════════════════════════════════════════════════
                SectionHeader(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Tipo de cuenta"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                AccountTypeCard(
                    currentType = profileData.accountType,
                    onClick = { showAccountTypeModal = true }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón Guardar
                Button(
                    onClick = { onSave(profileData, selectedAvatarUri, selectedBannerUri) },
                    enabled = canSave && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        disabledContainerColor = PrimaryPurple.copy(alpha = 0.3f)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Guardar cambios",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Modal de tipo de cuenta
        AccountTypeSelectionModal(
            isVisible = showAccountTypeModal,
            currentType = profileData.accountType,
            onDismiss = { showAccountTypeModal = false },
            onTypeSelected = { type ->
                profileData = profileData.copy(accountType = type)
                showAccountTypeModal = false
            }
        )
        
        // Modal de información personal
        PersonalInfoModal(
            isVisible = showPersonalInfoModal,
            onDismiss = { showPersonalInfoModal = false },
            ubicacion = profileData.ubicacion,
            onUbicacionChange = { profileData = profileData.copy(ubicacion = it) },
            telefono = profileData.telefono,
            onTelefonoChange = { profileData = profileData.copy(telefono = it) },
            fechaNacimiento = "", // TODO: Cargar desde datos del usuario
            onFechaNacimientoChange = { /* TODO: Guardar fecha de nacimiento */ }
        )
    }
}

@Composable
private fun EditProfileHeader(
    canSave: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary
            )
        }
        
        Text(
            text = "Editar perfil",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        TextButton(
            onClick = onSave,
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PrimaryPurple,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Guardar",
                    color = if (canSave) PrimaryPurple else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun AccountTypeCard(
    currentType: String,
    onClick: () -> Unit
) {
    val typeInfo = getAccountTypeInfo(currentType)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = typeInfo.color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, typeInfo.color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(typeInfo.color, typeInfo.color.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeInfo.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tipo de cuenta",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeInfo.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "CAMBIAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeInfo.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = typeInfo.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}

data class AccountTypeInfo(
    val id: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val features: List<String>
)

private fun getAccountTypeInfo(type: String): AccountTypeInfo {
    return when (type) {
        "brand" -> AccountTypeInfo(
            id = "brand",
            label = "Marca de ropa",
            description = "Tienda o marca con catálogo de productos",
            icon = Icons.Outlined.Storefront,
            color = AccentPink,
            features = listOf("Catálogo completo", "Múltiples variantes", "Estadísticas PRO", "Badge verificado")
        )
        "community" -> AccountTypeInfo(
            id = "community",
            label = "Comunidad",
            description = "Grupo o comunidad de intereses",
            icon = Icons.Outlined.Groups,
            color = Color(0xFF1565A0),
            features = listOf("Eventos", "Miembros", "Contenido exclusivo", "Badge verificado")
        )
        "product_service" -> AccountTypeInfo(
            id = "product_service",
            label = "Producto o Servicio",
            description = "Ofreces productos o servicios específicos",
            icon = Icons.Outlined.Inventory2,
            color = Color(0xFF2E8B57),
            features = listOf("Servicios destacados", "Cotizaciones", "Portfolio", "Badge verificado")
        )
        "restaurant" -> AccountTypeInfo(
            id = "restaurant",
            label = "Restaurante / Gastronomía",
            description = "Restaurante, café o servicio gastronómico",
            icon = Icons.Outlined.Restaurant,
            color = Color(0xFFFF6B35),
            features = listOf("Menú digital", "Reservas", "Delivery", "Badge verificado")
        )
        "shopping" -> AccountTypeInfo(
            id = "shopping",
            label = "Shopping / Centro comercial",
            description = "Centro comercial o galería de tiendas",
            icon = Icons.Outlined.ShoppingBag,
            color = Color(0xFFFF6B35),
            features = listOf("Directorio de tiendas", "Promociones", "Eventos", "Badge verificado")
        )
        else -> AccountTypeInfo(
            id = "casual",
            label = "Vendedor casual",
            description = "Vendes ocasionalmente sin tienda formal",
            icon = Icons.Outlined.Person,
            color = PrimaryPurple,
            features = listOf("Publicación rápida", "Chat directo", "Reputación", "Badge verificado")
        )
    }
}

private val ACCOUNT_TYPES = listOf(
    "casual" to "Vendedor casual",
    "brand" to "Marca de ropa",
    "community" to "Comunidad",
    "product_service" to "Producto o Servicio",
    "restaurant" to "Restaurante",
    "shopping" to "Shopping"
)

@Composable
private fun AccountTypeSelectionModal(
    isVisible: Boolean,
    currentType: String,
    onDismiss: () -> Unit,
    onTypeSelected: (String) -> Unit
) {
    var selectedTypeForDetail by remember { mutableStateOf<String?>(null) }
    
    // Backdrop
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }
    
    // Modal principal
    AnimatedVisibility(
        visible = isVisible && selectedTypeForDetail == null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
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
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Tipo de cuenta",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = "Elige el tipo que mejor represente tu actividad",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Lista de tipos
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ACCOUNT_TYPES.forEach { (id, _) ->
                            val info = getAccountTypeInfo(id)
                            val isSelected = currentType == id
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedTypeForDetail = id },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) info.color.copy(alpha = 0.1f) else Surface,
                                border = if (isSelected) 
                                    androidx.compose.foundation.BorderStroke(2.dp, info.color) 
                                else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(info.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = info.icon,
                                            contentDescription = null,
                                            tint = info.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = info.label,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = info.description,
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                    
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = info.color,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = TextMuted
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
    
    // Modal de detalle del tipo seleccionado
    AnimatedVisibility(
        visible = isVisible && selectedTypeForDetail != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        selectedTypeForDetail?.let { typeId ->
            val info = getAccountTypeInfo(typeId)
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = HomeBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Back button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { selectedTypeForDetail = null }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = TextPrimary
                                )
                            }
                            Text(
                                text = "Detalles",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Icon y título
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(info.color, info.color.copy(alpha = 0.7f))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = info.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = info.label,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = info.description,
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Features
                        Text(
                            text = "INCLUYE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        info.features.forEach { feature ->
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = info.color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = feature,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Botón de selección
                        Button(
                            onClick = { onTypeSelected(typeId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = info.color)
                        ) {
                            Text(
                                text = if (currentType == typeId) "Tipo actual" else "Cambiar a ${info.label}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryPurple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EditFieldPro(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    prefix: String? = null,
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = if (multiline) 14.dp else 0.dp)
                    .then(if (!multiline) Modifier.height(54.dp) else Modifier.heightIn(min = 90.dp)),
                verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(20.dp)
                        .then(if (multiline) Modifier.padding(top = 2.dp) else Modifier)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                if (prefix != null) {
                    Text(
                        text = prefix,
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextMuted.copy(alpha = 0.5f),
                            fontSize = 15.sp
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
                            fontSize = 15.sp
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

// Opciones de sexo
private val GENDER_OPTIONS = listOf(
    "masculino" to "Masculino",
    "femenino" to "Femenino",
    "no_binario" to "No binario",
    "prefiero_no_decir" to "Prefiero no decir",
    "otro" to "Otro"
)

@Composable
private fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedLabel = GENDER_OPTIONS.find { it.first == selectedGender }?.second ?: "Seleccionar"
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Sexo",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(14.dp),
            color = Surface
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(54.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = PrimaryPurple.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = if (selectedGender.isEmpty()) "Seleccionar" else selectedLabel,
                        fontSize = 15.sp,
                        color = if (selectedGender.isEmpty()) TextMuted.copy(alpha = 0.5f) else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Lista expandible
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
                        Divider(
                            color = BorderSubtle,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        
                        GENDER_OPTIONS.forEach { (value, label) ->
                            val isSelected = selectedGender == value
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onGenderSelected(value)
                                        isExpanded = false
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PrimaryPurple.copy(alpha = 0.12f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
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
                                            modifier = Modifier.size(20.dp)
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

@Composable
private fun ConfigButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(22.dp)
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
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MODAL DE INFORMACIÓN PERSONAL - Profesional y completo
// ═══════════════════════════════════════════════════════════════════════════════

private val COUNTRIES = listOf(
    "Argentina", "Bolivia", "Brasil", "Chile", "Colombia", "Costa Rica",
    "Cuba", "Ecuador", "El Salvador", "España", "Estados Unidos", "Guatemala",
    "Honduras", "México", "Nicaragua", "Panamá", "Paraguay", "Perú",
    "Puerto Rico", "República Dominicana", "Uruguay", "Venezuela"
)

private val LANGUAGES = listOf(
    "Español" to "es",
    "Inglés" to "en",
    "Portugués" to "pt",
    "Francés" to "fr",
    "Italiano" to "it",
    "Alemán" to "de"
)

@Composable
private fun PersonalInfoModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    ubicacion: String = "",
    onUbicacionChange: (String) -> Unit = {},
    telefono: String = "",
    onTelefonoChange: (String) -> Unit = {},
    fechaNacimiento: String = "",
    onFechaNacimientoChange: (String) -> Unit = {}
) {
    // Estados locales para los campos
    var birthDate by remember(fechaNacimiento) { mutableStateOf(fechaNacimiento) }
    var localUbicacion by remember(ubicacion) { mutableStateOf(ubicacion) }
    var localTelefono by remember(telefono) { mutableStateOf(telefono) }
    var selectedCountry by remember { mutableStateOf("Argentina") }
    var selectedLanguage by remember { mutableStateOf("Español") }
    var showCountryPicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Backdrop
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
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
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
                    .fillMaxHeight(), // Altura completa
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = HomeBg
            ) {
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
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(PrimaryPurple, AccentPink)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ManageAccounts,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Column {
                                Text(
                                    text = "Información personal",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Datos privados de tu cuenta",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = onDismiss,
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Contenido scrolleable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ══════════════════════════════════════════════
                        // FECHA DE NACIMIENTO
                        // ══════════════════════════════════════════════
                        PersonalInfoSection(
                            icon = Icons.Outlined.Cake,
                            title = "Fecha de nacimiento",
                            iconColor = Color(0xFF2E8B57)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showDatePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (birthDate.isEmpty()) "Seleccionar fecha" else birthDate,
                                        fontSize = 15.sp,
                                        color = if (birthDate.isEmpty()) TextMuted else TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Text(
                                text = "Tu edad no será visible públicamente",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // ══════════════════════════════════════════════
                        // UBICACIÓN
                        // ══════════════════════════════════════════════
                        PersonalInfoSection(
                            icon = Icons.Outlined.LocationOn,
                            title = "Ubicación",
                            iconColor = Color(0xFF2E8B57)
                        ) {
                            OutlinedTextField(
                                value = localUbicacion,
                                onValueChange = { localUbicacion = it },
                                placeholder = { Text("Ciudad, País", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2E8B57),
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            Text(
                                text = "Se mostrará en tu perfil público",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // ══════════════════════════════════════════════
                        // TELÉFONO
                        // ══════════════════════════════════════════════
                        PersonalInfoSection(
                            icon = Icons.Outlined.Phone,
                            title = "Teléfono",
                            iconColor = Color(0xFFFF6B35)
                        ) {
                            OutlinedTextField(
                                value = localTelefono,
                                onValueChange = { localTelefono = it },
                                placeholder = { Text("+54 11 1234-5678", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF6B35),
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            Text(
                                text = "Para contacto de ventas y soporte",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // ══════════════════════════════════════════════
                        // PAÍS
                        // ══════════════════════════════════════════════
                        PersonalInfoSection(
                            icon = Icons.Outlined.Public,
                            title = "País de residencia",
                            iconColor = Color(0xFF1565A0)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showCountryPicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCountry,
                                        fontSize = 15.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Text(
                                text = "Usado para mostrar precios en tu moneda local",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // ══════════════════════════════════════════════
                        // IDIOMA
                        // ══════════════════════════════════════════════
                        PersonalInfoSection(
                            icon = Icons.Outlined.Language,
                            title = "Idioma preferido",
                            iconColor = Color(0xFF2E8B57)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showLanguagePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedLanguage,
                                        fontSize = 15.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // ══════════════════════════════════════════════
                        // NOTA DE PRIVACIDAD
                        // ══════════════════════════════════════════════
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFF6B35).copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B35),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Tu información está protegida",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Esta información es privada y nunca se compartirá con terceros sin tu consentimiento explícito.",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón guardar
                    Button(
                        onClick = { 
                            // Guardar los valores actualizados
                            onUbicacionChange(localUbicacion)
                            onTelefonoChange(localTelefono)
                            onFechaNacimientoChange(birthDate)
                            onDismiss() 
                        },
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
                            text = "Guardar información",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Picker de país
    if (showCountryPicker) {
        PickerModal(
            title = "Seleccionar país",
            options = COUNTRIES,
            selectedOption = selectedCountry,
            onOptionSelected = { 
                selectedCountry = it
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }
    
    // Picker de idioma
    if (showLanguagePicker) {
        PickerModal(
            title = "Seleccionar idioma",
            options = LANGUAGES.map { it.first },
            selectedOption = selectedLanguage,
            onOptionSelected = { 
                selectedLanguage = it
                showLanguagePicker = false
            },
            onDismiss = { showLanguagePicker = false }
        )
    }
}

@Composable
private fun PersonalInfoSection(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
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
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        
        content()
    }
}

@Composable
private fun PickerModal(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = HomeBg
        ) {
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
                
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = option == selectedOption
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOptionSelected(option) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryPurple.copy(alpha = 0.12f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 15.sp,
                                    color = if (isSelected) PrimaryPurple else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(20.dp)
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
