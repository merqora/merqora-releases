package com.rendly.app.ui.components.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rendly.app.data.model.*
import com.rendly.app.engine.AddressEngine
import com.rendly.app.ui.theme.*
import com.rendly.app.ui.viewmodel.AddressEvent
import com.rendly.app.ui.viewmodel.AddressUiState
import com.rendly.app.ui.viewmodel.AddressViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * ADD ADDRESS MODAL - Modal full-screen para agregar direcciones
 * ══════════════════════════════════════════════════════════════════════════════
 */
@Composable
fun AddAddressModal(
    isVisible: Boolean,
    userId: String,
    onDismiss: () -> Unit,
    onAddressAdded: (Address) -> Unit,
    viewModel: AddressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val validationResult by viewModel.validationResult.collectAsState()
    val scope = rememberCoroutineScope()
    
    // State for map picker
    var showMapPicker by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    
    // Permission launcher - launches permissions and then gets location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Usar el método que NO verifica permisos porque ya sabemos que fueron concedidos
            viewModel.getCurrentLocationAfterPermission()
        } else {
            snackbarMessage = "Se necesitan permisos de ubicación para detectar tu dirección actual"
        }
    }
    
    // Function to request location with permission check
    fun requestCurrentLocation() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    // Event handling
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddressEvent.AddressSaved -> {
                    onAddressAdded(event.address)
                    onDismiss()
                }
                is AddressEvent.RequestLocationPermission -> {
                    // Solicitar permisos - el launcher se encargará de llamar 
                    // getCurrentLocationAfterPermission() cuando se concedan
                    requestCurrentLocation()
                }
                is AddressEvent.Error -> {
                    // Siempre mostrar un mensaje, nunca vacío
                    snackbarMessage = if (event.message.isNotBlank()) {
                        event.message
                    } else {
                        "Error al procesar la solicitud"
                    }
                }
                else -> {}
            }
        }
    }
    
    // Reset form when modal opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.resetForm()
        }
    }
    
    // Animation
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (slideOffset * 800).dp),
            color = HomeBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                AddAddressHeader(
                    onClose = onDismiss,
                    onSave = { viewModel.saveAddress(userId) },
                    isSaving = uiState.isSaving,
                    canSave = (uiState.hasValidLocation && uiState.formattedAddress.isNotBlank()) || uiState.rawInput.length >= 10
                )
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // GPS Button - Now properly requests permissions first
                    UseCurrentLocationButton(
                        onClick = { requestCurrentLocation() },
                        isLoading = uiState.isGettingLocation
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Address Search with Autocomplete
                    AddressSearchField(
                        value = uiState.rawInput,
                        onValueChange = { 
                            viewModel.updateStreetAddress(it)
                            viewModel.searchAddresses(it)
                        },
                        predictions = predictions,
                        onPredictionSelected = { viewModel.selectPrediction(it) },
                        isSearching = uiState.isSearching,
                        onSearch = { viewModel.geocodeAddress(uiState.rawInput) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Button to open interactive map picker
                    SelectOnMapButton(
                        onClick = { showMapPicker = true }
                    )
                    
                    // Validation Status
                    if (uiState.hasValidLocation) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ValidationStatusCard(
                            validationResult = validationResult,
                            isValidating = uiState.isValidating
                        )
                    }
                    
                    // Map Preview with Mapbox - Opens interactive map picker
                    if (uiState.hasValidLocation) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val mapUrl = viewModel.getStaticMapUrl(uiState.latitude, uiState.longitude)
                        
                        MapPreviewCard(
                            latitude = uiState.latitude,
                            longitude = uiState.longitude,
                            formattedAddress = uiState.formattedAddress,
                            mapUrl = mapUrl,
                            onMapClick = {
                                // Open interactive Mapbox map picker
                                showMapPicker = true
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Address Type Selection
                    AddressTypeSelector(
                        selectedType = uiState.addressType,
                        onTypeSelected = { viewModel.updateAddressType(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Label Input
                    AddressFormField(
                        label = "Nombre de la dirección",
                        value = uiState.label,
                        onValueChange = { viewModel.updateLabel(it) },
                        placeholder = "Ej: Casa, Trabajo, Casa de mamá",
                        leadingIcon = Icons.Outlined.Label
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Additional Details Section
                    Text(
                        text = "DETALLES ADICIONALES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Apartment & Floor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AddressFormField(
                            label = "Departamento",
                            value = uiState.apartment,
                            onValueChange = { viewModel.updateApartment(it) },
                            placeholder = "Ej: 4B",
                            leadingIcon = Icons.Outlined.MeetingRoom,
                            modifier = Modifier.weight(1f)
                        )
                        AddressFormField(
                            label = "Piso",
                            value = uiState.floor,
                            onValueChange = { viewModel.updateFloor(it) },
                            placeholder = "Ej: 3",
                            leadingIcon = Icons.Outlined.Layers,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Delivery Instructions
                    AddressFormField(
                        label = "Instrucciones de entrega",
                        value = uiState.deliveryInstructions,
                        onValueChange = { viewModel.updateDeliveryInstructions(it) },
                        placeholder = "Ej: Tocar timbre 2 veces",
                        leadingIcon = Icons.Outlined.Info,
                        singleLine = false,
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Reference Point
                    AddressFormField(
                        label = "Punto de referencia",
                        value = uiState.referencePoint,
                        onValueChange = { viewModel.updateReferencePoint(it) },
                        placeholder = "Ej: Frente a la plaza",
                        leadingIcon = Icons.Outlined.Place
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Set as Default Switch
                    SetAsDefaultSwitch(
                        isDefault = uiState.isDefault,
                        onToggle = { viewModel.updateIsDefault(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // Snackbar for messages
        snackbarMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { snackbarMessage = null }) {
                        Text("OK", color = Color.White)
                    }
                },
                containerColor = Color(0xFF323232)
            ) {
                Text(message)
            }
            
            LaunchedEffect(message) {
                delay(3000)
                snackbarMessage = null
            }
        }
    }
    
    // Mapbox Map Picker - Full screen interactive map
    MapboxMapPicker(
        isVisible = showMapPicker,
        initialLatitude = if (uiState.latitude != 0.0) uiState.latitude else -34.9011,
        initialLongitude = if (uiState.longitude != 0.0) uiState.longitude else -56.1645,
        initialAddress = uiState.formattedAddress,
        onLocationSelected = { lat, lng, address ->
            viewModel.updateLocationFromMap(lat, lng, address)
        },
        onDismiss = { showMapPicker = false }
    )
}

@Composable
private fun AddAddressHeader(
    onClose: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    canSave: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = TextPrimary
                )
            }
            
            Text(
                text = "Nueva dirección",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Button(
                onClick = onSave,
                enabled = canSave && !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UseCurrentLocationButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1565A0).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1565A0).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1565A0),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF1565A0),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Usar mi ubicación actual",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1565A0)
                )
                Text(
                    text = "Detectar dirección automáticamente por GPS",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF1565A0),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SelectOnMapButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFF6B35).copy(alpha = 0.1f)
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
                    .background(Color(0xFFFF6B35).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Seleccionar en el mapa",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF6B35)
                )
                Text(
                    text = "Marca la ubicación exacta con el pin",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFFF6B35),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AddressSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    predictions: List<AddressPrediction>,
    onPredictionSelected: (AddressPrediction) -> Unit,
    isSearching: Boolean,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Buscar dirección...",
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PrimaryPurple,
                        strokeWidth = 2.dp
                    )
                } else if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = TextMuted
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }
            ),
            singleLine = true
        )
        
        // Predictions dropdown
        AnimatedVisibility(
            visible = predictions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Surface,
                shadowElevation = 4.dp
            ) {
                Column {
                    predictions.forEachIndexed { index, prediction ->
                        PredictionItem(
                            prediction = prediction,
                            onClick = {
                                onPredictionSelected(prediction)
                                focusManager.clearFocus()
                            }
                        )
                        if (index < predictions.lastIndex) {
                            Divider(color = BorderSubtle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: AddressPrediction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = PrimaryPurple,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prediction.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = prediction.secondaryText,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ValidationStatusCard(
    validationResult: AddressEngine.ValidationResult?,
    isValidating: Boolean
) {
    val statusColor = when {
        isValidating -> Color(0xFF6B7280)
        validationResult == null -> Color(0xFF6B7280)
        validationResult.isValid -> Color(0xFF2E8B57)
        validationResult.isSuspicious -> Color(0xFFFF6B35)
        else -> Color(0xFFEF4444)
    }
    
    val statusText = when {
        isValidating -> "Validando dirección..."
        validationResult == null -> "Pendiente de validación"
        validationResult.isValid -> "Dirección válida"
        validationResult.isSuspicious -> "Dirección dudosa"
        else -> "Dirección inválida"
    }
    
    val statusIcon = when {
        isValidating -> Icons.Outlined.HourglassEmpty
        validationResult?.isValid == true -> Icons.Outlined.CheckCircle
        validationResult?.isSuspicious == true -> Icons.Outlined.Warning
        validationResult?.isInvalid == true -> Icons.Outlined.Error
        else -> Icons.Outlined.Info
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = statusColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isValidating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = statusColor,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                if (validationResult != null && !isValidating) {
                    Text(
                        text = "Confianza: ${validationResult.confidenceScore}%",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            if (validationResult != null && !isValidating) {
                // Score badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${validationResult.confidenceScore}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPreviewCard(
    latitude: Double,
    longitude: Double,
    formattedAddress: String,
    mapUrl: String,
    onMapClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMapClick() },
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column {
            // Mapbox Static Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                // Load static map from Mapbox
                AsyncImage(
                    model = mapUrl,
                    contentDescription = "Mapa de ubicación",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay with tap indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    // "Tap to expand" badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.OpenInFull,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ampliar",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Address info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedAddress,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Lat: %.5f, Lng: %.5f".format(latitude, longitude),
                        fontSize = 10.sp,
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
}

@Composable
private fun AddressTypeSelector(
    selectedType: AddressType,
    onTypeSelected: (AddressType) -> Unit
) {
    Column {
        Text(
            text = "TIPO DE DIRECCIÓN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AddressType.entries.forEach { type ->
                AddressTypeChip(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AddressTypeChip(
    type: AddressType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        AddressType.HOME -> Icons.Outlined.Home
        AddressType.WORK -> Icons.Outlined.Work
        AddressType.OTHER -> Icons.Outlined.Place
    }
    
    val label = when (type) {
        AddressType.HOME -> "Casa"
        AddressType.WORK -> "Trabajo"
        AddressType.OTHER -> "Otro"
    }
    
    val color = when (type) {
        AddressType.HOME -> Color(0xFF2E8B57)
        AddressType.WORK -> Color(0xFF1565A0)
        AddressType.OTHER -> Color(0xFFFF6B35)
    }
    
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, color, RoundedCornerShape(12.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else TextSecondary
            )
        }
    }
}

@Composable
private fun AddressFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = placeholder, color = TextMuted, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = keyboardType
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )
    }
}

@Composable
private fun SetAsDefaultSwitch(
    isDefault: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isDefault) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                tint = if (isDefault) Color(0xFFFF6B35) else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dirección principal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Usar como dirección predeterminada",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Switch(
                checked = isDefault,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryPurple,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = TextMuted.copy(alpha = 0.3f)
                )
            )
        }
    }
}
