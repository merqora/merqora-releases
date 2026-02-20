package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.repository.BlockedUserInfo
import com.rendly.app.data.repository.ChatRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

data class BlockedUser(
    val id: String,
    val username: String,
    val avatarUrl: String?,
    val blockedAt: String,
    val reason: String? = null
)

@Composable
fun BlockedUsersScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var blockedUsers by remember { mutableStateOf<List<BlockedUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load real blocked users from Supabase
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val users = ChatRepository.getBlockedUsers()
            blockedUsers = users.map { info ->
                val timeAgo = try {
                    val blocked = java.time.OffsetDateTime.parse(info.blockedAt)
                    val now = java.time.OffsetDateTime.now()
                    val days = java.time.Duration.between(blocked, now).toDays()
                    when {
                        days == 0L -> "Hoy"
                        days == 1L -> "Hace 1 día"
                        days < 7 -> "Hace $days días"
                        days < 30 -> "Hace ${days / 7} semana${if (days / 7 > 1) "s" else ""}"
                        else -> "Hace ${days / 30} mes${if (days / 30 > 1) "es" else ""}"
                    }
                } catch (e: Exception) { "" }
                
                BlockedUser(
                    id = info.id,
                    username = info.username,
                    avatarUrl = info.avatarUrl,
                    blockedAt = timeAgo,
                    reason = info.reason
                )
            }
            isLoading = false
        }
    }
    
    var showUnblockDialog by remember { mutableStateOf<BlockedUser?>(null) }
    
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
                    title = "Usuarios bloqueados",
                    subtitle = "Gestiona tu lista de bloqueos",
                    icon = Icons.Outlined.Block,
                    iconColor = Color(0xFF6B7280),
                    onBack = onDismiss
                )
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6B7280),
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else if (blockedUsers.isEmpty()) {
                    // Estado vacío
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E8B57).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Sin usuarios bloqueados",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "No has bloqueado a ningún usuario.\nCuando bloquees a alguien, aparecerá aquí.",
                                fontSize = 14.sp,
                                color = TextMuted,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            // Info banner
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
                                        text = "Los usuarios bloqueados no pueden ver tu perfil, publicaciones ni contactarte.",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "${blockedUsers.size} usuario${if (blockedUsers.size > 1) "s" else ""} bloqueado${if (blockedUsers.size > 1) "s" else ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                            )
                        }
                        
                        items(blockedUsers, key = { it.id }) { user ->
                            BlockedUserCard(
                                user = user,
                                onUnblock = { showUnblockDialog = user }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para desbloquear
    showUnblockDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showUnblockDialog = null },
            containerColor = HomeBg,
            title = {
                Text(
                    text = "Desbloquear usuario",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres desbloquear a @${user.username}? Podrá volver a ver tu perfil y contactarte.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = ChatRepository.unblockUser(user.id)
                            if (success) {
                                blockedUsers = blockedUsers.filter { it.id != user.id }
                            }
                        }
                        showUnblockDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Desbloquear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockDialog = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun BlockedUserCard(
    user: BlockedUser,
    onUnblock: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = user.username.take(2).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "@${user.username}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bloqueado ${user.blockedAt}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    if (user.reason != null) {
                        Text(
                            text = " · ",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = user.reason,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Botón desbloquear
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onUnblock),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E8B57).copy(alpha = 0.12f)
            ) {
                Text(
                    text = "Desbloquear",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E8B57),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
