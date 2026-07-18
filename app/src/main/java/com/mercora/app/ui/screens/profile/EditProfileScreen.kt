package com.mercora.app.ui.screens.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var accountType: String = "casual", // "casual", "brand", "community", "product_service", etc.
    var avatarShape: String = "circle"
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var profileData by remember { mutableStateOf(initialData) }
    var hasImageChanges by remember { mutableStateOf(false) }
    
    // Estado para modal de tipo de cuenta
    var showAccountTypeModal by remember { mutableStateOf(false) }
    
    // Estado para modal de informaciÃ³n personal
    var showPersonalInfoModal by remember { mutableStateOf(false) }
    
    // Estado para modal de posiciÃ³n de imagen
    var showImagePositionModal by remember { mutableStateOf(false) }
    var imagePositionMode by remember { mutableStateOf("avatar") } // "avatar" o "banner"
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
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
            profileData.accountType != initialData.accountType ||
            profileData.avatarShape != initialData.avatarShape
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
            pendingImageUri = it
            imagePositionMode = "avatar"
            scope.launch {
                pendingImageBitmap = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(it)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (e: Exception) { null }
                }
                showImagePositionModal = true
            }
        }
    }
    
    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pendingImageUri = it
            imagePositionMode = "banner"
            scope.launch {
                pendingImageBitmap = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(it)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (e: Exception) { null }
                }
                showImagePositionModal = true
            }
        }
    }
    
    // Inicializar repositorio de formas de avatar
    LaunchedEffect(Unit) {
        com.vinzay.app.data.repository.AvatarShapeRepository.init(context)
    }
    
    // Auto-sync shape al servidor cuando cambia (sin esperar "Guardar")
    LaunchedEffect(profileData.avatarShape) {
        if (profileData.avatarShape != initialData.avatarShape) {
            com.vinzay.app.data.repository.AvatarShapeRepository.syncSelectedShapeToServer()
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
                onSave = {
                    onSave(profileData, selectedAvatarUri, selectedBannerUri)
                    onBack()
                }
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
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // INFORMACIÃ“N BÃSICA
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                SectionHeader(
                    icon = Icons.Outlined.Person,
                    title = "InformaciÃ³n bÃ¡sica"
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
                
                // Selector de GÃ©nero inclusivo
                GenderSelector(
                    selectedGender = profileData.sexo,
                    onGenderSelected = { profileData = profileData.copy(sexo = it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                EditFieldPro(
                    label = "BiografÃ­a",
                    value = profileData.descripcion,
                    onValueChange = { profileData = profileData.copy(descripcion = it) },
                    placeholder = "CuÃ©ntanos sobre ti o tu negocio...",
                    icon = Icons.Outlined.Edit,
                    multiline = true,
                    maxLines = 4,
                    maxChars = 150
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // BotÃ³n Configurar informaciÃ³n personal
                ConfigButton(
                    icon = Icons.Outlined.ManageAccounts,
                    title = "Configurar informaciÃ³n personal",
                    subtitle = "Fecha de nacimiento, paÃ­s, idioma",
                    onClick = { showPersonalInfoModal = true }
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // NEGOCIO / TIENDA
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
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
                
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // REDES Y WEB
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
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
                
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // TIPO DE CUENTA - Al final
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                SectionHeader(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Tipo de cuenta"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                AccountTypeCard(
                    currentType = profileData.accountType,
                    onClick = { showAccountTypeModal = true }
                )
                
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // FORMA DEL AVATAR (solo si tiene formas desbloqueadas)
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                val ownedShapes = com.vinzay.app.data.repository.AvatarShapeRepository.getAllOwnedShapes()
                if (ownedShapes.size > 1) {
                    Spacer(modifier = Modifier.height(28.dp))
                    SectionHeader(
                        icon = Icons.Outlined.Category,
                        title = "Forma del avatar"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val currentShapeType = com.vinzay.app.data.repository.AvatarShapeRepository.getSelectedShape()
                    
                    // Grid de formas disponibles (4 columnas)
                    val columns = 4
                    val rows = ownedShapes.chunked(columns)
                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { shapeType ->
                                val isSelected = shapeType == currentShapeType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Surface)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) PrimaryPurple else TextMuted.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            com.vinzay.app.data.repository.AvatarShapeRepository.setSelectedShape(shapeType)
                                            profileData = profileData.copy(avatarShape = shapeType.dbValue)
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Preview visual de la forma real
                                        val shapeForPreview = shapeType.toShape()
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(shapeForPreview)
                                                .background(PrimaryPurple.copy(alpha = if (isSelected) 0.7f else 0.35f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // PequeÃ±o brillo interno para simular avatar
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(6.dp)
                                                    .clip(shapeForPreview)
                                                    .background(if (isSelected) PrimaryPurple.copy(alpha = 0.3f) else TextMuted.copy(alpha = 0.1f))
                                            )
                                        }
                                        Text(
                                            text = shapeType.displayName,
                                            fontSize = 9.sp,
                                            color = if (isSelected) PrimaryPurple else TextMuted,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            // Fill empty slots
                            repeat(columns - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // BotÃ³n Guardar
                Button(
                    onClick = {
                        onSave(profileData, selectedAvatarUri, selectedBannerUri)
                        onBack()
                    },
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
        
        // Modal de informaciÃ³n personal
        PersonalInfoModal(
            isVisible = showPersonalInfoModal,
            onDismiss = { showPersonalInfoModal = false },
            ubicacion = profileData.ubicacion,
            onUbicacionChange = { profileData = profileData.copy(ubicacion = it) },
            telefono = profileData.telefono,
            onTelefonoChange = { profileData = profileData.copy(telefono = it) },
            fechaNacimiento = "",
            onFechaNacimientoChange = { }
        )
        
        // Modal de posiciÃ³n de imagen (avatar o banner)
        ImagePositionModal(
            isVisible = showImagePositionModal,
            bitmap = pendingImageBitmap,
            mode = imagePositionMode,
            onConfirm = { uri ->
                if (imagePositionMode == "avatar") {
                    selectedAvatarUri = uri
                } else {
                    selectedBannerUri = uri
                }
                hasImageChanges = true
                showImagePositionModal = false
            },
            onDismiss = { showImagePositionModal = false },
            imageUri = pendingImageUri
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
            description = "Tienda o marca con catÃ¡logo de productos",
            icon = Icons.Outlined.Storefront,
            color = AccentPink,
            features = listOf("CatÃ¡logo completo", "MÃºltiples variantes", "EstadÃ­sticas PRO", "Badge verificado")
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
            description = "Ofreces productos o servicios especÃ­ficos",
            icon = Icons.Outlined.Inventory2,
            color = Color(0xFF2E8B57),
            features = listOf("Servicios destacados", "Cotizaciones", "Portfolio", "Badge verificado")
        )
        "restaurant" -> AccountTypeInfo(
            id = "restaurant",
            label = "Restaurante / GastronomÃ­a",
            description = "Restaurante, cafÃ© o servicio gastronÃ³mico",
            icon = Icons.Outlined.Restaurant,
            color = Color(0xFFFF6B35),
            features = listOf("MenÃº digital", "Reservas", "Delivery", "Badge verificado")
        )
        "shopping" -> AccountTypeInfo(
            id = "shopping",
            label = "Shopping / Centro comercial",
            description = "Centro comercial o galerÃ­a de tiendas",
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
            features = listOf("PublicaciÃ³n rÃ¡pida", "Chat directo", "ReputaciÃ³n", "Badge verificado")
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
                        
                        // Icon y tÃ­tulo
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
                        
                        // BotÃ³n de selecciÃ³n
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

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// MODAL DE INFORMACIÃ“N PERSONAL - Profesional y completo
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

private val COUNTRIES = listOf(
    "Argentina", "Bolivia", "Brasil", "Chile", "Colombia", "Costa Rica",
    "Cuba", "Ecuador", "El Salvador", "EspaÃ±a", "Estados Unidos", "Guatemala",
    "Honduras", "MÃ©xico", "Nicaragua", "PanamÃ¡", "Paraguay", "PerÃº",
    "Puerto Rico", "RepÃºblica Dominicana", "Uruguay", "Venezuela"
)

private val LANGUAGES = listOf(
    "EspaÃ±ol" to "es",
    "InglÃ©s" to "en",
    "PortuguÃ©s" to "pt",
    "FrancÃ©s" to "fr",
    "Italiano" to "it",
    "AlemÃ¡n" to "de"
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estados locales para los campos
    var birthDate by remember(fechaNacimiento) { mutableStateOf(fechaNacimiento) }
    var localUbicacion by remember(ubicacion) { mutableStateOf(ubicacion) }
    var localTelefono by remember(telefono) { mutableStateOf(telefono) }
    var selectedCountry by remember { mutableStateOf("Argentina") }
    var selectedLanguage by remember { mutableStateOf("EspaÃ±ol") }
    var showCountryPicker by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    
    // Material3 DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            birthDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = PrimaryPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = HomeBg
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = HomeBg,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextMuted,
                    dayContentColor = TextPrimary,
                    selectedDayContainerColor = PrimaryPurple,
                    selectedDayContentColor = Color.White,
                    todayContentColor = PrimaryPurple,
                    todayDateBorderColor = PrimaryPurple,
                    yearContentColor = TextPrimary,
                    selectedYearContainerColor = PrimaryPurple,
                    selectedYearContentColor = Color.White
                )
            )
        }
    }
    
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
                                    text = "InformaciÃ³n personal",
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
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        // FECHA DE NACIMIENTO
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
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
                                text = "Tu edad no serÃ¡ visible pÃºblicamente",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        // UBICACIÃ“N
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        PersonalInfoSection(
                            icon = Icons.Outlined.LocationOn,
                            title = "UbicaciÃ³n",
                            iconColor = Color(0xFF2E8B57)
                        ) {
                            OutlinedTextField(
                                value = localUbicacion,
                                onValueChange = { localUbicacion = it },
                                placeholder = { Text("Ciudad, PaÃ­s", color = TextMuted) },
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
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // BotÃ³n "Usar ubicaciÃ³n actual" (mismo que en RendScreen)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isLoadingLocation) {
                                        isLoadingLocation = true
                                        scope.launch {
                                            try {
                                                val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                                                if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                        location?.let {
                                                            val geocoder = android.location.Geocoder(context)
                                                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                                            if (!addresses.isNullOrEmpty()) {
                                                                val address = addresses[0]
                                                                val locality = address.locality ?: ""
                                                                val country = address.countryName ?: ""
                                                                localUbicacion = "$locality, $country".trim().trim(',')
                                                            }
                                                        }
                                                        isLoadingLocation = false
                                                    }.addOnFailureListener {
                                                        isLoadingLocation = false
                                                    }
                                                } else {
                                                    android.widget.Toast.makeText(context, "Permiso de ubicaciÃ³n requerido", android.widget.Toast.LENGTH_SHORT).show()
                                                    isLoadingLocation = false
                                                }
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Error obteniendo ubicaciÃ³n", android.widget.Toast.LENGTH_SHORT).show()
                                                isLoadingLocation = false
                                            }
                                        }
                                    },
                                color = Color(0xFF1DA1F2).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isLoadingLocation) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color(0xFF1DA1F2),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.MyLocation,
                                            null,
                                            tint = Color(0xFF1DA1F2),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Usar ubicaciÃ³n actual",
                                        color = Color(0xFF1DA1F2),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Text(
                                text = "Se mostrarÃ¡ en tu perfil pÃºblico",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        // TELÃ‰FONO
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        PersonalInfoSection(
                            icon = Icons.Outlined.Phone,
                            title = "TelÃ©fono",
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
                        
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        // PAÃS
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        PersonalInfoSection(
                            icon = Icons.Outlined.Public,
                            title = "PaÃ­s de residencia",
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
                        
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        // IDIOMA
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
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
                        
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                        // NOTA DE PRIVACIDAD
                        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
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
                                        text = "Tu informaciÃ³n estÃ¡ protegida",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Esta informaciÃ³n es privada y nunca se compartirÃ¡ con terceros sin tu consentimiento explÃ­cito.",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // BotÃ³n guardar
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
                            text = "Guardar informaciÃ³n",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Picker de paÃ­s
    if (showCountryPicker) {
        PickerModal(
            title = "Seleccionar paÃ­s",
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

// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// IMAGE POSITION MODAL - Pan/Zoom para avatar y banner (reutiliza sistema de PublicationScreen)
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
@Composable
private fun ImagePositionModal(
    isVisible: Boolean,
    bitmap: Bitmap?,
    mode: String, // "avatar" o "banner"
    imageUri: Uri?,
    onConfirm: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    
    // Pan/zoom state
    var imageOffsetX by remember(isVisible) { mutableStateOf(0f) }
    var imageOffsetY by remember(isVisible) { mutableStateOf(0f) }
    var imageScale by remember(isVisible) { mutableStateOf(1f) }
    
    // Dimensiones para clamping
    var containerWidthPx by remember { mutableStateOf(0f) }
    var containerHeightPx by remember { mutableStateOf(0f) }
    var imageWidthPx by remember { mutableStateOf(0f) }
    var imageHeightPx by remember { mutableStateOf(0f) }
    var baseCoverScaleState by remember { mutableStateOf(1f) }
    
    fun clampOffsets(newOffsetX: Float, newOffsetY: Float, scale: Float): Pair<Float, Float> {
        if (containerWidthPx <= 0 || containerHeightPx <= 0 || imageWidthPx <= 0 || imageHeightPx <= 0) {
            return Pair(newOffsetX, newOffsetY)
        }
        val finalScale = baseCoverScaleState * scale
        val scaledImageW = imageWidthPx * finalScale
        val scaledImageH = imageHeightPx * finalScale
        val maxOffsetX = maxOf(0f, (scaledImageW - containerWidthPx) / 2f)
        val maxOffsetY = maxOf(0f, (scaledImageH - containerHeightPx) / 2f)
        return Pair(
            newOffsetX.coerceIn(-maxOffsetX, maxOffsetX),
            newOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
        )
    }
    
    AnimatedVisibility(
        visible = isVisible && bitmap != null,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .systemBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextMuted, fontSize = 14.sp)
                    }
                    
                    Text(
                        text = if (mode == "avatar") "Ajustar foto de perfil" else "Ajustar portada",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(
                        onClick = { imageUri?.let { onConfirm(it) } }
                    ) {
                        Text(
                            "Aplicar",
                            color = PrimaryPurple,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Preview area - diferente forma segÃºn modo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (mode == "avatar") 80.dp else 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val previewModifier = if (mode == "avatar") {
                        Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                    }
                    
                    BoxWithConstraints(
                        modifier = previewModifier
                            .clipToBounds()
                            .background(Color(0xFF1A1A2E))
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        val pan = event.calculatePan()
                                        val zoom = event.calculateZoom()
                                        val newScale = (imageScale * zoom).coerceIn(1f, 3f)
                                        imageScale = newScale
                                        val newOffsetX = imageOffsetX + pan.x
                                        val newOffsetY = imageOffsetY + pan.y
                                        val (cx, cy) = clampOffsets(newOffsetX, newOffsetY, newScale)
                                        imageOffsetX = cx
                                        imageOffsetY = cy
                                        event.changes.forEach { it.consume() }
                                    } while (event.changes.any { it.pressed })
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val containerWidth = maxWidth
                        val containerHeight = maxHeight
                        
                        LaunchedEffect(containerWidth, containerHeight) {
                            with(density) {
                                containerWidthPx = containerWidth.toPx()
                                containerHeightPx = containerHeight.toPx()
                            }
                        }
                        
                        bitmap?.let { bmp ->
                            val imgWidth = bmp.width.toFloat()
                            val imgHeight = bmp.height.toFloat()
                            
                            LaunchedEffect(bmp) {
                                imageWidthPx = imgWidth
                                imageHeightPx = imgHeight
                            }
                            
                            val containerW = with(density) { containerWidth.toPx() }
                            val containerH = with(density) { containerHeight.toPx() }
                            
                            val scaleToFillWidth = containerW / imgWidth
                            val scaleToFillHeight = containerH / imgHeight
                            val baseCoverScale = maxOf(scaleToFillWidth, scaleToFillHeight)
                            
                            LaunchedEffect(baseCoverScale) {
                                baseCoverScaleState = baseCoverScale
                            }
                            
                            val finalScale = baseCoverScale * imageScale
                            val (clampedX, clampedY) = clampOffsets(imageOffsetX, imageOffsetY, imageScale)
                            val scaledWidth = with(density) { (imgWidth * finalScale).toDp() }
                            val scaledHeight = with(density) { (imgHeight * finalScale).toDp() }
                            
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Posicionar imagen",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .requiredSize(scaledWidth, scaledHeight)
                                    .graphicsLayer {
                                        translationX = clampedX
                                        translationY = clampedY
                                    }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Hint text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TouchApp,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Arrastra para mover â€¢ Pellizca para zoom",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reset button
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            imageOffsetX = 0f
                            imageOffsetY = 0f
                            imageScale = 1f
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CenterFocusStrong,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Centrar",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
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
                .fillMaxHeight()
                .statusBarsPadding()
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
