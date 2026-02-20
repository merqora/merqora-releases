package com.rendly.app.ui.components.settings

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.ProfileRepository
import com.rendly.app.data.repository.VerificationRepository
import com.rendly.app.data.repository.VerificationRepository.AccountDataForVerification
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VerificationScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var isVerified by remember { mutableStateOf(false) }
    var verificationStatus by remember { mutableStateOf("none") } // none, pending, approved, rejected
    var rejectionReason by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showRequestForm by remember { mutableStateOf(false) }
    
    // Form fields
    var selectedType by remember { mutableStateOf("personal") }
    var fullLegalName by remember { mutableStateOf("") }
    var reasonForVerification by remember { mutableStateOf("") }
    var notablePresence by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Datos para requisitos dinámicos
    var accountData by remember { mutableStateOf<AccountDataForVerification?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Cargar estado de Verificación y datos de cuenta
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                // Verificar si ya está verificado
                val status = VerificationRepository.getVerificationStatus(userId)
                isVerified = status?.is_verified ?: false
                
                // Si está verificado, recargar el perfil para actualizar el badge en toda la app
                if (isVerified) {
                    ProfileRepository.loadCurrentProfile()
                }
                
                // Cargar datos de cuenta para requisitos
                accountData = VerificationRepository.getAccountDataForVerification()
                
                if (!isVerified) {
                    // Verificar si hay solicitud pendiente
                    val latestRequest = VerificationRepository.getLatestRequest(userId)
                    verificationStatus = latestRequest?.status ?: "none"
                    rejectionReason = latestRequest?.rejection_reason
                }
            }
            isLoading = false
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
                    title = "Verificación",
                    subtitle = "Verifica tu cuenta",
                    icon = Icons.Outlined.VerifiedUser,
                    iconColor = Color(0xFF1565A0),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isVerified) {
                        // Pantalla de felicitación para usuarios verificados
                        VerifiedCelebrationContent()
                    } else {
                        // Pantalla normal para usuarios no verificados
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF1565A0),
                                                    Color(0xFFFF6B35)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Verificación de cuenta",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Obtén el badge de Verificación para mostrar autenticidad.",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                
                                if (verificationStatus == "none") {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    
                                    Button(
                                        onClick = { showRequestForm = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1565A0)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Solicitar Verificación",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            
                            if (verificationStatus == "pending" || verificationStatus == "under_review") {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFF6B35).copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = Color(0xFFFF6B35),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (verificationStatus == "under_review") "En Revisión por el equipo" else "Solicitud en Revisión",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFF6B35)
                                        )
                                    }
                                }
                            }
                            
                            if (verificationStatus == "rejected") {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.12f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Cancel,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Solicitud rechazada",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                        if (!rejectionReason.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = rejectionReason!!,
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        TextButton(
                                            onClick = { 
                                                verificationStatus = "none"
                                                showRequestForm = true
                                            }
                                        ) {
                                            Text("Volver a solicitar", color = Color(0xFF1565A0))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Beneficios
                    SettingsSectionTitle("Beneficios de la Verificación")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            BenefitItem(
                                icon = Icons.Outlined.Verified,
                                title = "Badge de Verificación",
                                description = "Distintivo azul junto a tu nombre",
                                iconColor = Color(0xFF1565A0)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            BenefitItem(
                                icon = Icons.Outlined.TrendingUp,
                                title = "Mayor visibilidad",
                                description = "Apareces primero en Búsquedas",
                                iconColor = Color(0xFF2E8B57)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            BenefitItem(
                                icon = Icons.Outlined.Security,
                                title = "Confianza",
                                description = "Los usuarios confían Más en cuentas verificadas",
                                iconColor = Color(0xFFFF6B35)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            BenefitItem(
                                icon = Icons.Outlined.Support,
                                title = "Soporte prioritario",
                                description = "Atención preferente del equipo de soporte",
                                iconColor = Color(0xFFFF6B35)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Requisitos dinámicos basados en métricas reales
                    SettingsSectionTitle("Requisitos para Verificación")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Requisito 1: mínimo 50 seguidores
                            val hasMinFollowers = (accountData?.followersCount ?: 0) >= 50
                            RequirementItemWithProgress(
                                text = "mínimo 50 seguidores",
                                current = accountData?.followersCount ?: 0,
                                target = 50,
                                isMet = hasMinFollowers
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Requisito 2: mínimo 10 publicaciones
                            val hasMinPosts = (accountData?.postsCount ?: 0) >= 10
                            RequirementItemWithProgress(
                                text = "mínimo 10 publicaciones",
                                current = accountData?.postsCount ?: 0,
                                target = 10,
                                isMet = hasMinPosts
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Requisito 3: Reputación Mínima 80%
                            val hasMinReputation = (accountData?.reputation ?: 0) >= 80
                            RequirementItemWithProgress(
                                text = "Reputación Mínima 80%",
                                current = accountData?.reputation ?: 0,
                                target = 80,
                                isMet = hasMinReputation,
                                isPercentage = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Requisito 4: Al menos 5 ventas realizadas
                            val hasMinSales = (accountData?.salesCount ?: 0) >= 5
                            RequirementItemWithProgress(
                                text = "mínimo 5 ventas realizadas",
                                current = accountData?.salesCount ?: 0,
                                target = 5,
                                isMet = hasMinSales
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Requisito 5: Perfil completo
                            val hasCompleteProfile = accountData?.username != null && 
                                (accountData?.email?.isNotBlank() == true)
                            RequirementItem(
                                text = "Perfil completo con foto y Biografía",
                                isMet = hasCompleteProfile
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Requisito 6: Cuenta activa (miembro desde hace tiempo)
                            val isActiveAccount = accountData?.memberSince != null && 
                                accountData?.memberSince != "---"
                            RequirementItem(
                                text = "Cuenta activa por Más de 30 Días",
                                isMet = isActiveAccount
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Tipos de Verificación
                    SettingsSectionTitle("Tipos de Verificación")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            VerificationTypeItem(
                                icon = Icons.Outlined.Person,
                                title = "Cuenta personal",
                                description = "Para creadores de contenido e influencers",
                                iconColor = PrimaryPurple
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            VerificationTypeItem(
                                icon = Icons.Outlined.Store,
                                title = "Negocio",
                                description = "Para tiendas y empresas",
                                iconColor = Color(0xFF2E8B57)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            VerificationTypeItem(
                                icon = Icons.Outlined.Campaign,
                                title = "Marca oficial",
                                description = "Para marcas reconocidas",
                                iconColor = Color(0xFF1565A0)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Info
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
                                text = "La Verificación es gratuita y el proceso puede tardar entre 1-7 Días hábiles.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    } // Cierre del else (usuarios no verificados)
                }
            }
        }
        
        // Modal de solicitud de Verificación - pantalla completa desde abajo
        VerificationRequestModal(
            isVisible = showRequestForm,
            onDismiss = { if (!isSubmitting) showRequestForm = false },
            selectedType = selectedType,
            onTypeChange = { selectedType = it },
            fullLegalName = fullLegalName,
            onFullLegalNameChange = { fullLegalName = it },
            reasonForVerification = reasonForVerification,
            onReasonChange = { reasonForVerification = it },
            notablePresence = notablePresence,
            onNotablePresenceChange = { notablePresence = it },
            isSubmitting = isSubmitting,
            onSubmit = {
                scope.launch {
                    isSubmitting = true
                    val userId = SupabaseClient.auth.currentUserOrNull()?.id
                    if (userId != null && fullLegalName.isNotBlank() && reasonForVerification.isNotBlank()) {
                        val success = VerificationRepository.submitVerificationRequest(
                            userId = userId,
                            verificationType = selectedType,
                            fullLegalName = fullLegalName,
                            reasonForVerification = reasonForVerification,
                            notablePresence = notablePresence.ifBlank { null }
                        )
                        if (success) {
                            Toast.makeText(context, "Solicitud enviada correctamente", Toast.LENGTH_SHORT).show()
                            verificationStatus = "pending"
                            showRequestForm = false
                            fullLegalName = ""
                            reasonForVerification = ""
                            notablePresence = ""
                        } else {
                            Toast.makeText(context, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Completa todos los campos requeridos", Toast.LENGTH_SHORT).show()
                    }
                    isSubmitting = false
                }
            }
        )
    }
}

// Componente de celebración para usuarios verificados
@Composable
private fun VerifiedCelebrationContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header de celebración
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF2E8B57).copy(alpha = 0.1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de Verificación grande con animación
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2E8B57),
                                    Color(0xFF1565A0)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "¡Felicidades!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E8B57)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tu cuenta está verificada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Ahora tienes el distintivo oficial de Merqora. Tu perfil es reconocido como auténtico.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Beneficios activos
        Text(
            text = "TUS BENEFICIOS ACTIVOS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Surface
        ) {
            Column {
                ActiveBenefitItem(
                    icon = Icons.Default.Verified,
                    title = "Badge de Verificación",
                    description = "Visible en tu perfil y publicaciones",
                    iconColor = Color(0xFF1565A0),
                    isActive = true
                )
                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                ActiveBenefitItem(
                    icon = Icons.Outlined.TrendingUp,
                    title = "Mayor visibilidad",
                    description = "Tu perfil aparece primero en Búsquedas",
                    iconColor = Color(0xFF2E8B57),
                    isActive = true
                )
                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                ActiveBenefitItem(
                    icon = Icons.Outlined.Security,
                    title = "Confianza aumentada",
                    description = "Los usuarios confían Más en tu cuenta",
                    iconColor = Color(0xFFFF6B35),
                    isActive = true
                )
                Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                ActiveBenefitItem(
                    icon = Icons.Outlined.Support,
                    title = "Soporte prioritario",
                    description = "Respuestas Más Rápidas del equipo",
                    iconColor = Color(0xFFFF6B35),
                    isActive = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info de Verificación permanente
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
                    text = "Tu Verificación es permanente mientras mantengas los estándares de la comunidad.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ActiveBenefitItem(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        
        if (isActive) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Activo",
                tint = Color(0xFF2E8B57),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Función auxiliar para calcular probabilidad de Verificación
private fun calculateVerificationProbability(accountData: AccountDataForVerification?): Int {
    val followersScore = ((accountData?.followersCount ?: 0).toFloat() / 50f).coerceIn(0f, 1f) * 20
    val postsScore = ((accountData?.postsCount ?: 0).toFloat() / 10f).coerceIn(0f, 1f) * 15
    val reputationScore = ((accountData?.reputation ?: 0).toFloat() / 80f).coerceIn(0f, 1f) * 25
    val salesScore = ((accountData?.salesCount ?: 0).toFloat() / 5f).coerceIn(0f, 1f) * 20
    val profileScore = if (accountData?.username != null && accountData.email?.isNotBlank() == true) 10f else 0f
    val activeScore = if (accountData?.memberSince != null && accountData.memberSince != "---") 10f else 0f
    return (followersScore + postsScore + reputationScore + salesScore + profileScore + activeScore).toInt()
}

// Componente de barra de probabilidad minimalista para el modal
@Composable
private fun VerificationProbabilityBar(
    accountData: AccountDataForVerification?,
    modifier: Modifier = Modifier
) {
    val probability = calculateVerificationProbability(accountData)
    
    val probabilityColor = when {
        probability >= 80 -> Color(0xFF2E8B57)
        probability >= 60 -> Color(0xFF1565A0)
        probability >= 40 -> Color(0xFFFF6B35)
        else -> Color(0xFFEF4444)
    }
    
    val probabilityLabel = when {
        probability >= 80 -> "Excelente"
        probability >= 60 -> "Buena"
        probability >= 40 -> "Moderada"
        else -> "Baja"
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = probabilityColor.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = probabilityColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Probabilidad de Verificación",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$probability%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = probabilityColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = probabilityLabel,
                        fontSize = 11.sp,
                        color = probabilityColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Barra de progreso
            LinearProgressIndicator(
                progress = probability / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = probabilityColor,
                trackColor = BorderSubtle
            )
        }
    }
}

@Composable
private fun VerificationRequestModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    selectedType: String,
    onTypeChange: (String) -> Unit,
    fullLegalName: String,
    onFullLegalNameChange: (String) -> Unit,
    reasonForVerification: String,
    onReasonChange: (String) -> Unit,
    notablePresence: String,
    onNotablePresenceChange: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    // Datos de la cuenta actual
    var accountData by remember { mutableStateOf<AccountDataForVerification?>(null) }
    var isLoadingData by remember { mutableStateOf(true) }
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "modalSlide"
    )
    
    // Cargar datos de la cuenta
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoadingData = true
            accountData = VerificationRepository.getAccountDataForVerification()
            isLoadingData = false
        }
    }
    
    if (!isVisible && slideOffset == 1f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f * (1f - slideOffset)))
    ) {
        // Modal desde abajo - pantalla completa
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (slideOffset * 1000).dp),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Solicitar Verificación",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Completa tu solicitud",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    
                    Button(
                        onClick = onSubmit,
                        enabled = !isSubmitting && fullLegalName.isNotBlank() && reasonForVerification.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565A0)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enviar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                
                Divider(color = BorderSubtle)
                
                // Barra de probabilidad minimalista
                if (!isLoadingData) {
                    VerificationProbabilityBar(
                        accountData = accountData,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                
                // Contenido scrolleable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Datos de tu cuenta (automáticos)
                    Text(
                        text = "DATOS DE TU CUENTA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        if (isLoadingData) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Username y Email
                                AccountDataRow(
                                    icon = Icons.Outlined.Person,
                                    label = "Usuario",
                                    value = "@${accountData?.username ?: "---"}",
                                    iconColor = PrimaryPurple
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AccountDataRow(
                                    icon = Icons.Outlined.Email,
                                    label = "Correo Electrónico",
                                    value = accountData?.email ?: "No disponible",
                                    iconColor = Color(0xFF1565A0)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AccountDataRow(
                                    icon = Icons.Outlined.Phone,
                                    label = "teléfono",
                                    value = accountData?.phone ?: "No registrado",
                                    iconColor = Color(0xFF2E8B57)
                                )
                                
                                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 12.dp))
                                
                                // estadísticas
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatColumn(
                                        value = "${accountData?.followersCount ?: 0}",
                                        label = "Seguidores",
                                        iconColor = Color(0xFFFF6B35)
                                    )
                                    StatColumn(
                                        value = "${accountData?.clientsCount ?: 0}",
                                        label = "Clientes",
                                        iconColor = Color(0xFF2E8B57)
                                    )
                                    StatColumn(
                                        value = "${accountData?.postsCount ?: 0}",
                                        label = "Publicaciones",
                                        iconColor = Color(0xFF1565A0)
                                    )
                                    StatColumn(
                                        value = "${accountData?.reputation ?: 0}%",
                                        label = "Reputación",
                                        iconColor = Color(0xFFFF6B35)
                                    )
                                }
                                
                                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 12.dp))
                                
                                // Historial de cuenta
                                AccountDataRow(
                                    icon = Icons.Outlined.CalendarMonth,
                                    label = "Miembro desde",
                                    value = accountData?.memberSince ?: "---",
                                    iconColor = Color(0xFF444444)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AccountDataRow(
                                    icon = Icons.Outlined.Store,
                                    label = "Tipo de cuenta",
                                    value = if (accountData?.hasStore == true) "Vendedor" else "Personal",
                                    iconColor = Color(0xFFFF6B35)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AccountDataRow(
                                    icon = Icons.Outlined.ShoppingBag,
                                    label = "Ventas realizadas",
                                    value = "${accountData?.salesCount ?: 0}",
                                    iconColor = Color(0xFF2E8B57)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Tipo de Verificación
                    Text(
                        text = "TIPO DE VERIFICación",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            listOf(
                                Triple("personal", "Cuenta personal", "Para creadores de contenido e influencers"),
                                Triple("business", "Negocio", "Para tiendas y empresas"),
                                Triple("brand", "Marca oficial", "Para marcas reconocidas")
                            ).forEachIndexed { index, (value, title, desc) ->
                                if (index > 0) {
                                    Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onTypeChange(value) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedType == value,
                                        onClick = { onTypeChange(value) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF1565A0),
                                            unselectedColor = TextMuted
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Información adicional
                    Text(
                        text = "INFORMación ADICIONAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = fullLegalName,
                                onValueChange = onFullLegalNameChange,
                                label = { Text("Nombre completo *") },
                                placeholder = { Text("Tu nombre legal completo") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1565A0),
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Badge,
                                        contentDescription = null,
                                        tint = TextMuted
                                    )
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = reasonForVerification,
                                onValueChange = onReasonChange,
                                label = { Text("¿Por ¿Qué quieres verificarte? *") },
                                placeholder = { Text("Explica por ¿Qué tu cuenta merece ser verificada...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1565A0),
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                minLines = 4,
                                maxLines = 6
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = notablePresence,
                                onValueChange = onNotablePresenceChange,
                                label = { Text("Presencia en otras plataformas (opcional)") },
                                placeholder = { Text("Links a tus perfiles verificados en otras redes...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1565A0),
                                    unfocusedBorderColor = BorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                minLines = 3,
                                maxLines = 5,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Link,
                                        contentDescription = null,
                                        tint = TextMuted
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nota informativa
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
                            Column {
                                Text(
                                    text = "Los datos de tu cuenta se enviarán automáticamente con tu solicitud.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "El proceso de Revisión tarda de 1 a 7 Días hábiles.",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountDataRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    iconColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = iconColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun BenefitItem(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
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
        
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun RequirementItem(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF2E8B57) else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isMet) TextPrimary else TextMuted
        )
    }
}

@Composable
private fun RequirementItemWithProgress(
    text: String,
    current: Int,
    target: Int,
    isMet: Boolean,
    isPercentage: Boolean = false
) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isMet) Color(0xFF2E8B57) else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = if (isMet) TextPrimary else TextMuted
                )
            }
            
            Text(
                text = if (isPercentage) "$current%" else "$current/$target",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isMet) Color(0xFF2E8B57) else Color(0xFFFF6B35)
            )
        }
        
        if (!isMet) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when {
                    progress >= 0.8f -> Color(0xFF2E8B57)
                    progress >= 0.5f -> Color(0xFFFF6B35)
                    else -> Color(0xFFEF4444)
                },
                trackColor = BorderSubtle
            )
        }
    }
}

@Composable
private fun VerificationTypeItem(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
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
