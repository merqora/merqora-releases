package com.rendly.app.ui.components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.UserPreferencesRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PrivacySettingsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val userId = remember { SupabaseClient.auth.currentUserOrNull()?.id ?: "" }
    
    var isLoading by remember { mutableStateOf(true) }
    var rowExists by remember { mutableStateOf(false) }
    
    // Privacy state
    var profileVisibility by remember { mutableStateOf("public") }
    var showOnlineStatus by remember { mutableStateOf(true) }
    var showLastSeen by remember { mutableStateOf(true) }
    var allowTagging by remember { mutableStateOf(true) }
    var allowMentions by remember { mutableStateOf(true) }
    var showActivityStatus by remember { mutableStateOf(true) }
    var showLikes by remember { mutableStateOf(true) }
    var showPurchaseActivity by remember { mutableStateOf(true) }
    var hideStoryEnabled by remember { mutableStateOf(false) }
    
    // Story hidden users modal
    var showHiddenUsersModal by remember { mutableStateOf(false) }
    var hiddenUsersCount by remember { mutableIntStateOf(0) }
    
    // Load from Supabase
    LaunchedEffect(isVisible) {
        if (isVisible && userId.isNotBlank()) {
            isLoading = true
            // Ensure default row exists first, returns loaded or default settings
            val settings = UserPreferencesRepository.ensureDefaultPrivacySettings(userId)
            rowExists = true
            profileVisibility = settings.profileVisibility
            showOnlineStatus = settings.showOnlineStatus
            showLastSeen = settings.showLastSeen
            showActivityStatus = settings.showActivityStatus
            allowTagging = settings.allowTagging
            allowMentions = settings.allowMentions
            showLikes = settings.showLikes
            showPurchaseActivity = settings.showPurchaseActivity
            hideStoryEnabled = settings.hideStoryEnabled
            // Load hidden users count
            val hiddenUsers = UserPreferencesRepository.loadStoryHiddenUsers(userId)
            hiddenUsersCount = hiddenUsers.size
            isLoading = false
        }
    }
    
    // Save helper
    fun saveField(field: String, value: Any) {
        if (userId.isBlank()) return
        scope.launch {
            val success = UserPreferencesRepository.savePrivacyField(userId, field, value, rowExists)
            if (success && !rowExists) rowExists = true
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
                    title = "Privacidad",
                    subtitle = "Controla quién ve tu contenido",
                    icon = Icons.Outlined.Lock,
                    iconColor = Color(0xFFEF4444),
                    onBack = onDismiss
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // ── Visibilidad del perfil ──
                    SettingsSectionTitle("Visibilidad del perfil")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            PrivacyOption(
                                icon = Icons.Outlined.Public,
                                title = "Perfil público",
                                subtitle = "Cualquiera puede ver tu perfil y publicaciones",
                                isSelected = profileVisibility == "public",
                                onClick = { 
                                    profileVisibility = "public"
                                    saveField("profile_visibility", "public")
                                },
                                iconColor = Color(0xFF2E8B57)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            PrivacyOption(
                                icon = Icons.Outlined.People,
                                title = "Solo seguidores",
                                subtitle = "Solo quienes te siguen ven tus publicaciones",
                                isSelected = profileVisibility == "followers",
                                onClick = { 
                                    profileVisibility = "followers"
                                    saveField("profile_visibility", "followers")
                                },
                                iconColor = Color(0xFF1565A0)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            PrivacyOption(
                                icon = Icons.Outlined.LockPerson,
                                title = "Perfil privado",
                                subtitle = "Debes aprobar cada seguidor para que vea tu contenido",
                                isSelected = profileVisibility == "private",
                                onClick = { 
                                    profileVisibility = "private"
                                    saveField("profile_visibility", "private")
                                },
                                iconColor = Color(0xFFEF4444)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ── Estado de actividad (chat presence) ──
                    SettingsSectionTitle("Estado de actividad")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            PrivacyToggle(
                                icon = Icons.Outlined.Circle,
                                title = "Mostrar estado en línea",
                                subtitle = "Otros ven si estás conectado en los chats",
                                isEnabled = showOnlineStatus,
                                onToggle = { 
                                    showOnlineStatus = it
                                    saveField("show_online_status", it)
                                },
                                iconColor = Color(0xFF2E8B57)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            PrivacyToggle(
                                icon = Icons.Outlined.AccessTime,
                                title = "Mostrar última conexión",
                                subtitle = "Muestra cuándo estuviste activo por última vez",
                                isEnabled = showLastSeen,
                                onToggle = { 
                                    showLastSeen = it
                                    saveField("show_last_seen", it)
                                },
                                iconColor = Color(0xFF1565A0)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            PrivacyToggle(
                                icon = Icons.Outlined.TrendingUp,
                                title = "Estado de actividad",
                                subtitle = "Muestra tu actividad reciente en la app",
                                isEnabled = showActivityStatus,
                                onToggle = { 
                                    showActivityStatus = it
                                    saveField("show_activity_status", it)
                                },
                                iconColor = Color(0xFFFF6B35)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ── Interacciones ──
                    SettingsSectionTitle("Interacciones")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            PrivacyToggle(
                                icon = Icons.Outlined.LocalOffer,
                                title = "Permitir etiquetado",
                                subtitle = "Otros pueden etiquetarte en publicaciones",
                                isEnabled = allowTagging,
                                onToggle = { 
                                    allowTagging = it
                                    saveField("allow_tagging", it)
                                },
                                iconColor = PrimaryPurple
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            PrivacyToggle(
                                icon = Icons.Outlined.AlternateEmail,
                                title = "Permitir menciones",
                                subtitle = "Otros pueden mencionarte con @ en comentarios y chats",
                                isEnabled = allowMentions,
                                onToggle = { 
                                    allowMentions = it
                                    saveField("allow_mentions", it)
                                },
                                iconColor = Color(0xFF1565A0)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ── Contenido visible ──
                    SettingsSectionTitle("Contenido visible")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            PrivacyToggle(
                                icon = Icons.Outlined.Favorite,
                                title = "Mostrar mis likes",
                                subtitle = "Los demás pueden ver las publicaciones que te gustan",
                                isEnabled = showLikes,
                                onToggle = { 
                                    showLikes = it
                                    saveField("show_likes", it)
                                },
                                iconColor = AccentPink
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            PrivacyToggle(
                                icon = Icons.Outlined.ShoppingBag,
                                title = "Mostrar actividad de compras",
                                subtitle = "Los demás pueden ver tus compras recientes",
                                isEnabled = showPurchaseActivity,
                                onToggle = { 
                                    showPurchaseActivity = it
                                    saveField("show_purchase_activity", it)
                                },
                                iconColor = Color(0xFFFF6B35)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ── Historias ──
                    SettingsSectionTitle("Historias")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            PrivacyToggle(
                                icon = Icons.Outlined.VisibilityOff,
                                title = "Ocultar historias de...",
                                subtitle = if (hiddenUsersCount > 0) "$hiddenUsersCount usuarios ocultos" 
                                           else "Selecciona personas que no verán tus historias",
                                isEnabled = hideStoryEnabled,
                                onToggle = { 
                                    hideStoryEnabled = it
                                    saveField("hide_story_enabled", it)
                                },
                                iconColor = Color(0xFF6B7280)
                            )
                            
                            // Show "Manage list" button when enabled
                            AnimatedVisibility(visible = hideStoryEnabled) {
                                Column {
                                    Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showHiddenUsersModal = true }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PersonAdd,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Administrar lista de ocultos",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryPurple
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Info card
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
                                text = "Estas configuraciones controlan tu privacidad en la plataforma. Los cambios se aplican inmediatamente.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                } // end if (!isLoading)
            }
        }
        
        // Story Hidden Users Modal
        if (showHiddenUsersModal) {
            StoryHiddenUsersModal(
                userId = userId,
                onDismiss = { showHiddenUsersModal = false },
                onCountChanged = { hiddenUsersCount = it }
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// STORY HIDDEN USERS MODAL
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun StoryHiddenUsersModal(
    userId: String,
    onDismiss: () -> Unit,
    onCountChanged: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserPreferencesRepository.MentionUserDB>>(emptyList()) }
    var hiddenUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isSearching by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load current hidden users
    LaunchedEffect(Unit) {
        val hidden = UserPreferencesRepository.loadStoryHiddenUsers(userId)
        hiddenUserIds = hidden.map { it.hiddenUserId }.toSet()
        isLoading = false
    }
    
    // Search users when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 1) {
            isSearching = true
            searchResults = UserPreferencesRepository.searchUsersForMention(searchQuery)
            isSearching = false
        } else {
            searchResults = UserPreferencesRepository.searchUsersForMention("")
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(20.dp),
            color = HomeBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ocultar historias de...",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${hiddenUserIds.size} usuarios ocultos",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Cerrar", tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
                
                // Search field
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(PrimaryPurple),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Buscar usuario...", color = TextMuted, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = BorderSubtle)
                
                // User list
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(32.dp))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(searchResults, key = { it.userId }) { user ->
                            val isHidden = hiddenUserIds.contains(user.userId)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            if (isHidden) {
                                                UserPreferencesRepository.removeStoryHiddenUser(userId, user.userId)
                                                hiddenUserIds = hiddenUserIds - user.userId
                                            } else {
                                                UserPreferencesRepository.addStoryHiddenUser(userId, user.userId)
                                                hiddenUserIds = hiddenUserIds + user.userId
                                            }
                                            onCountChanged(hiddenUserIds.size)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                if (user.avatarUrl != null) {
                                    AsyncImage(
                                        model = user.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryPurple.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.username.firstOrNull()?.uppercase() ?: "?",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryPurple
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.username,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Verified,
                                                contentDescription = null,
                                                tint = Color(0xFF1D9BF0),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    if (user.nombreTienda != null) {
                                        Text(
                                            text = user.nombreTienda,
                                            fontSize = 12.sp,
                                            color = TextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                
                                // Toggle button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isHidden) Color(0xFFEF4444).copy(alpha = 0.15f) 
                                            else PrimaryPurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isHidden) "Oculto" else "Ocultar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isHidden) Color(0xFFEF4444) else PrimaryPurple,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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

// ════════════════════════════════════════════════════════════════════════════
// REUSABLE COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrivacyOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                text = subtitle,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = PrimaryPurple,
                unselectedColor = TextMuted
            )
        )
    }
}

@Composable
private fun PrivacyToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) }
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
                text = subtitle,
                fontSize = 12.sp,
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
