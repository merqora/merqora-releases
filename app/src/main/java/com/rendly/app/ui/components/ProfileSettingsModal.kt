package com.rendly.app.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rendly.app.ui.components.settings.*
import com.rendly.app.ui.theme.*
import com.rendly.app.data.repository.VerificationRepository
import com.rendly.app.data.remote.SupabaseClient
import kotlinx.coroutines.launch

/**
 * Modal de Configuración del perfil
 * Accesible desde el Botón de 3 puntos en ProfileScreen
 * Abre desde la derecha con animación fluida
 */
@Composable
fun ProfileSettingsModal(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPrivacySettings: () -> Unit = {},
    onNotificationSettings: () -> Unit = {},
    onSecuritySettings: () -> Unit = {},
    onHelpCenter: () -> Unit = {},
    onAbout: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Estados para pantallas de Configuración
    var showOrderHistory by remember { mutableStateOf(false) }
    var showAddresses by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showSecurity by remember { mutableStateOf(false) }
    var showBlockedUsers by remember { mutableStateOf(false) }
    var showVerification by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf("") }
    
    // Estado de Verificación del usuario actual
    var isCurrentUserVerified by remember { mutableStateOf(false) }
    var verificationPending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Cargar estado de Verificación al abrir
    LaunchedEffect(isVisible) {
        if (isVisible) {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                currentUserId = userId
                // Verificar si está verificado
                isCurrentUserVerified = VerificationRepository.isUserVerified(userId)
                // Verificar si tiene solicitud pendiente
                val pending = VerificationRepository.getPendingRequest(userId)
                verificationPending = pending != null
            }
        }
    }
    var showHelpCenter by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var showReportProblem by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showTermsAndConditions by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    
    // Animación de slide desde la derecha
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "slideOffset"
    )
    
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundAlpha"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha))
            .clickable(onClick = onDismiss)
    ) {
        // Modal content - slide desde la derecha
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
                // Header con Botón X
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Configuración",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Administra tu cuenta y preferencias",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    
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
                            tint = TextPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ----------------------------------------------
                    // SEcción: CUENTA
                    // ----------------------------------------------
                    SettingsSectionHeader(title = "Cuenta")
                    
                    SettingsItem(
                        icon = Icons.Outlined.Receipt,
                        iconColor = Color(0xFFFF6B35),
                        title = "Mis Transacciones",
                        subtitle = "Historial de compras y ventas",
                        onClick = { showOrderHistory = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.LocationOn,
                        iconColor = Color(0xFF1565A0),
                        title = "Direcciones",
                        subtitle = "Direcciones de Envío guardadas",
                        onClick = { showAddresses = true }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ----------------------------------------------
                    // SEcción: PREFERENCIAS
                    // ----------------------------------------------
                    SettingsSectionHeader(title = "Preferencias")
                    
                    SettingsItem(
                        icon = Icons.Outlined.Notifications,
                        iconColor = Color(0xFFFFA726),
                        title = "Notificaciones",
                        subtitle = "Push, email y preferencias",
                        onClick = { showNotifications = true }
                    )
                    
                    
                    SettingsItem(
                        icon = Icons.Outlined.Language,
                        iconColor = Color(0xFF1565A0),
                        title = "Idioma",
                        subtitle = "Español (Latinoamérica)",
                        onClick = { showLanguage = true }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ----------------------------------------------
                    // SEcción: PRIVACIDAD Y SEGURIDAD
                    // ----------------------------------------------
                    SettingsSectionHeader(title = "Privacidad y seguridad")
                    
                    SettingsItem(
                        icon = Icons.Outlined.Lock,
                        iconColor = Color(0xFFEF4444),
                        title = "Privacidad",
                        subtitle = "Controla quién ve tu contenido",
                        onClick = { showPrivacy = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Security,
                        iconColor = Color(0xFF2E8B57),
                        title = "Seguridad",
                        subtitle = "contraseña y autenticación",
                        onClick = { showSecurity = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Block,
                        iconColor = Color(0xFF6B7280),
                        title = "Usuarios bloqueados",
                        subtitle = "Gestiona tu lista de bloqueos",
                        onClick = { showBlockedUsers = true }
                    )
                    
                    SettingsItem(
                        icon = if (isCurrentUserVerified) Icons.Filled.Verified else Icons.Outlined.VerifiedUser,
                        iconColor = if (isCurrentUserVerified) Color(0xFF22C55E) else Color(0xFF1565A0),
                        title = "Verificación",
                        subtitle = when {
                            isCurrentUserVerified -> "Cuenta verificada"
                            verificationPending -> "Solicitud en Revisión..."
                            else -> "Verifica tu cuenta"
                        },
                        badge = if (isCurrentUserVerified) "VERIFICADO" else if (!verificationPending) "NUEVO" else null,
                        badgeColor = if (isCurrentUserVerified) Color(0xFF22C55E) else PrimaryPurple,
                        onClick = { showVerification = true }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ----------------------------------------------
                    // SEcción: SOPORTE
                    // ----------------------------------------------
                    SettingsSectionHeader(title = "Soporte")
                    
                    SettingsItem(
                        icon = Icons.Outlined.HelpOutline,
                        iconColor = Color(0xFF1565A0),
                        title = "Centro de ayuda",
                        subtitle = "Preguntas frecuentes y soporte",
                        onClick = { showHelpCenter = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Feedback,
                        iconColor = Color(0xFFFF6B35),
                        title = "Enviar comentarios",
                        subtitle = "Ayúdanos a mejorar Merqora",
                        onClick = { showFeedback = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.BugReport,
                        iconColor = Color(0xFFEF4444),
                        title = "Reportar un problema",
                        subtitle = "Reporta bugs o errores",
                        onClick = { showReportProblem = true }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ----------------------------------------------
                    // SEcción: INFORMación
                    // ----------------------------------------------
                    SettingsSectionHeader(title = "Información")
                    
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        iconColor = Color(0xFF6366F1),
                        title = "Acerca de Merqora",
                        subtitle = "Versión 1.0.0",
                        onClick = { showAbout = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Description,
                        iconColor = Color(0xFF444444),
                        title = "términos y condiciones",
                        subtitle = "Lee nuestros Términos de uso",
                        onClick = { showTermsAndConditions = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Outlined.Policy,
                        iconColor = Color(0xFF444444),
                        title = "política de privacidad",
                        subtitle = "cómo manejamos tus datos",
                        onClick = { showPrivacyPolicy = true }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ----------------------------------------------
                    // botón DE CERRAR SESIÓN
                    // ----------------------------------------------
                    Button(
                        onClick = {
                            onDismiss()
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar Sesión",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // Pantalla de Historial de Pedidos
        OrderHistoryScreen(
            isVisible = showOrderHistory,
            onDismiss = { showOrderHistory = false }
        )
        
        // Pantalla de Direcciones
        AddressesScreen(
            isVisible = showAddresses,
            onDismiss = { showAddresses = false }
        )
        
        // Pantalla de Notificaciones
        NotificationsSettingsScreen(
            isVisible = showNotifications,
            userId = currentUserId,
            onDismiss = { showNotifications = false }
        )
        
        
        // Pantalla de Idioma
        LanguageSettingsScreen(
            isVisible = showLanguage,
            onDismiss = { showLanguage = false }
        )
        
        // Pantalla de Privacidad
        PrivacySettingsScreen(
            isVisible = showPrivacy,
            onDismiss = { showPrivacy = false }
        )
        
        // Pantalla de Seguridad
        SecuritySettingsScreen(
            isVisible = showSecurity,
            onDismiss = { showSecurity = false },
            onLogout = {
                onDismiss()
                onLogout()
            }
        )
        
        // Pantalla de Usuarios bloqueados
        BlockedUsersScreen(
            isVisible = showBlockedUsers,
            onDismiss = { showBlockedUsers = false }
        )
        
        // Pantalla de Verificación
        VerificationScreen(
            isVisible = showVerification,
            onDismiss = { showVerification = false }
        )
        
        // Pantalla de Centro de ayuda
        HelpCenterScreen(
            isVisible = showHelpCenter,
            onDismiss = { showHelpCenter = false }
        )
        
        // Pantalla de Enviar comentarios
        FeedbackScreen(
            isVisible = showFeedback,
            onDismiss = { showFeedback = false }
        )
        
        // Pantalla de Reportar un problema
        ReportProblemScreen(
            isVisible = showReportProblem,
            onDismiss = { showReportProblem = false }
        )
        
        // Pantalla de Acerca de Merqora
        AboutScreen(
            isVisible = showAbout,
            onDismiss = { showAbout = false }
        )
        
        // Pantalla de términos y Condiciones
        TermsAndConditionsScreen(
            isVisible = showTermsAndConditions,
            onDismiss = { showTermsAndConditions = false }
        )
        
        // Pantalla de política de Privacidad
        PrivacyPolicyScreen(
            isVisible = showPrivacyPolicy,
            onDismiss = { showPrivacyPolicy = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextMuted,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = PrimaryPurple,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
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
            
            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    // Badge opcional
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
