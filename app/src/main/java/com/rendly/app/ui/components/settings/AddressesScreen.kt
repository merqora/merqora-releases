package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rendly.app.data.model.Address
import com.rendly.app.data.model.AddressStatus
import com.rendly.app.data.model.AddressType
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.ProfileRepository
import com.rendly.app.ui.theme.*
import com.rendly.app.ui.viewmodel.AddressViewModel

@Composable
fun AddressesScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    viewModel: AddressViewModel = hiltViewModel()
) {
    val addresses by viewModel.addresses.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showAddAddressModal by remember { mutableStateOf(false) }
    
    // Obtener userId del usuario autenticado de forma reactiva y confiable
    val profileFromRepo by ProfileRepository.currentProfile.collectAsState()
    val currentUserId = remember(profileFromRepo) {
        SupabaseClient.auth.currentUserOrNull()?.id 
            ?: profileFromRepo?.userId 
            ?: ""
    }
    
    // Log para debug
    LaunchedEffect(currentUserId) {
        android.util.Log.d("AddressesScreen", "currentUserId: $currentUserId")
    }
    
    // Cargar perfil si no está disponible y luego cargar direcciones
    LaunchedEffect(isVisible) {
        if (isVisible) {
            if (profileFromRepo == null) {
                android.util.Log.d("AddressesScreen", "Profile not loaded, loading now...")
                ProfileRepository.loadCurrentProfile()
            }
            
            val userId = SupabaseClient.auth.currentUserOrNull()?.id 
                ?: ProfileRepository.currentProfile.value?.userId
            
            if (!userId.isNullOrEmpty()) {
                viewModel.loadAddresses(userId)
            } else {
                android.util.Log.e("AddressesScreen", "Failed to get userId")
            }
        }
    }
    
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
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp)
                .clickable(enabled = false) { },
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Direcciones",
                    subtitle = "Gestiona tus direcciones de envío",
                    icon = Icons.Outlined.LocationOn,
                    iconColor = Color(0xFF1565A0),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.isLoading) {
                        // Loading state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryPurple,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else if (addresses.isNotEmpty()) {
                        addresses.forEach { address ->
                            AddressCardNew(
                                address = address,
                                onSetDefault = {
                                    viewModel.setDefaultAddress(address.id, currentUserId)
                                },
                                onEdit = { /* TODO: Implementar edición */ },
                                onDelete = {
                                    viewModel.deleteAddress(address.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else {
                        EmptyStateCard(
                            icon = Icons.Outlined.LocationOff,
                            title = "Sin direcciones guardadas",
                            subtitle = "Agrega una dirección para facilitar tus compras"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showAddAddressModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agregar dirección",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1565A0).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color(0xFF1565A0),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tus direcciones se usan para calcular costos de envío y facilitar el proceso de compra.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // Reload addresses when modal closes (ensures fresh data)
        LaunchedEffect(showAddAddressModal) {
            if (!showAddAddressModal && currentUserId.isNotEmpty()) {
                kotlinx.coroutines.delay(300) // Wait for modal animation
                viewModel.loadAddresses(currentUserId)
            }
        }
        
        // Modal de agregar dirección
        if (showAddAddressModal) {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id 
                ?: ProfileRepository.currentProfile.value?.userId 
                ?: ""
            
            AddAddressModal(
                isVisible = showAddAddressModal,
                userId = userId,
                onDismiss = { showAddAddressModal = false },
                onAddressAdded = { address ->
                    showAddAddressModal = false
                }
            )
        }
    }
}

@Composable
private fun AddressCardNew(
    address: Address,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val typeIcon = when (address.addressType) {
        AddressType.HOME -> Icons.Outlined.Home
        AddressType.WORK -> Icons.Outlined.Work
        AddressType.OTHER -> Icons.Outlined.Place
    }
    
    val typeColor = when (address.addressType) {
        AddressType.HOME -> Color(0xFF2E8B57)
        AddressType.WORK -> Color(0xFF1565A0)
        AddressType.OTHER -> Color(0xFFFF6B35)
    }
    
    val statusColor = when (address.status) {
        AddressStatus.VALID -> Color(0xFF2E8B57)
        AddressStatus.SUSPICIOUS -> Color(0xFFFF6B35)
        AddressStatus.INVALID -> Color(0xFFEF4444)
        AddressStatus.PENDING -> Color(0xFF6B7280)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = address.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (address.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AccentGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Principal",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        // Validation status badge
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${address.confidenceScore}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = address.formattedAddress,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = address.cityWithPostalCode,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = BorderSubtle)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!address.isDefault) {
                    ActionChip(
                        text = "Principal",
                        icon = Icons.Outlined.Star,
                        color = PrimaryPurple,
                        onClick = onSetDefault,
                        modifier = Modifier.weight(1f)
                    )
                }
                ActionChip(
                    text = "Editar",
                    icon = Icons.Outlined.Edit,
                    color = Color(0xFF1565A0),
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    text = "Eliminar",
                    icon = Icons.Outlined.Delete,
                    color = Color(0xFFEF4444),
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
