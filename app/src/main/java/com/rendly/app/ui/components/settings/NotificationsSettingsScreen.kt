package com.rendly.app.ui.components.settings

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.UserPreferencesRepository
import com.rendly.app.ui.theme.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class NotificationPreferencesDB(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("push_enabled") val pushEnabled: Boolean = true,
    @SerialName("email_enabled") val emailEnabled: Boolean = true,
    @SerialName("price_drops") val priceDrops: Boolean = true,
    @SerialName("stock_alerts") val stockAlerts: Boolean = true,
    val messages: Boolean = true,
    @SerialName("new_followers") val newFollowers: Boolean = true,
    val comments: Boolean = true,
    val likes: Boolean = false,
    val promotions: Boolean = true
)

@Composable
fun NotificationsSettingsScreen(
    isVisible: Boolean,
    userId: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var prefsRowExists by remember { mutableStateOf(false) }
    
    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(true) }
    var promotions by remember { mutableStateOf(true) }
    var newFollowers by remember { mutableStateOf(true) }
    var messages by remember { mutableStateOf(true) }
    var likes by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf(true) }
    var priceDrops by remember { mutableStateOf(true) }
    var stockAlerts by remember { mutableStateOf(true) }
    
    // Load preferences from Supabase
    LaunchedEffect(userId) {
        if (userId.isBlank()) return@LaunchedEffect
        try {
            // Ensure default row exists first
            UserPreferencesRepository.ensureDefaultNotificationPreferences(userId)
            
            val prefs = withContext(Dispatchers.IO) {
                SupabaseClient.database
                    .from("notification_preferences")
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<NotificationPreferencesDB>()
            }
            if (prefs != null) {
                prefsRowExists = true
                pushEnabled = prefs.pushEnabled
                emailEnabled = prefs.emailEnabled
                priceDrops = prefs.priceDrops
                stockAlerts = prefs.stockAlerts
                messages = prefs.messages
                newFollowers = prefs.newFollowers
                comments = prefs.comments
                likes = prefs.likes
                promotions = prefs.promotions
            }
        } catch (e: Exception) {
            Log.e("NotificationsSettings", "Error loading preferences", e)
        } finally {
            isLoading = false
        }
    }
    
    // Save preference helper - uses INSERT for new rows, UPDATE for existing
    fun savePreference(field: String, value: Boolean) {
        if (userId.isBlank()) return
        scope.launch {
            isSaving = true
            try {
                val success = UserPreferencesRepository.saveNotificationField(
                    userId = userId,
                    field = field,
                    value = value,
                    rowExists = prefsRowExists
                )
                if (success && !prefsRowExists) {
                    prefsRowExists = true
                }
            } catch (e: Exception) {
                Log.e("NotificationsSettings", "Error saving $field", e)
            } finally {
                isSaving = false
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
                    title = "Notificaciones",
                    subtitle = "Configura cómo te contactamos",
                    icon = Icons.Outlined.Notifications,
                    iconColor = Color(0xFFFF6B35),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryPurple)
                        }
                    } else {
                    
                    SettingsSectionTitle("Canales de notificación")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            NotificationToggle(
                                icon = Icons.Outlined.PhoneAndroid,
                                title = "Notificaciones push",
                                subtitle = "Recibe alertas en tu dispositivo",
                                isEnabled = pushEnabled,
                                onToggle = { 
                                    pushEnabled = it
                                    savePreference("push_enabled", it)
                                },
                                iconColor = Color(0xFFFF6B35)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            NotificationToggle(
                                icon = Icons.Outlined.Email,
                                title = "Correo electrónico",
                                subtitle = "Recibe notificaciones por email",
                                isEnabled = emailEnabled,
                                onToggle = { 
                                    emailEnabled = it
                                    savePreference("email_enabled", it)
                                },
                                iconColor = Color(0xFF1565A0)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsSectionTitle("Actividad y compras")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            NotificationToggle(
                                icon = Icons.Outlined.TrendingDown,
                                title = "Bajadas de precio",
                                subtitle = "Alertas de productos guardados",
                                isEnabled = priceDrops,
                                onToggle = { 
                                    priceDrops = it
                                    savePreference("price_drops", it)
                                },
                                iconColor = Color(0xFFEF4444)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            NotificationToggle(
                                icon = Icons.Outlined.Inventory2,
                                title = "Stock disponible",
                                subtitle = "Cuando un producto vuelve a estar disponible",
                                isEnabled = stockAlerts,
                                onToggle = { 
                                    stockAlerts = it
                                    savePreference("stock_alerts", it)
                                },
                                iconColor = Color(0xFFFF6B35)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsSectionTitle("Social")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column {
                            NotificationToggle(
                                icon = Icons.Outlined.Chat,
                                title = "Mensajes",
                                subtitle = "Nuevos mensajes de compradores/vendedores",
                                isEnabled = messages,
                                onToggle = { 
                                    messages = it
                                    savePreference("messages", it)
                                },
                                iconColor = PrimaryPurple
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            NotificationToggle(
                                icon = Icons.Outlined.PersonAdd,
                                title = "Nuevos seguidores",
                                subtitle = "Cuando alguien te sigue",
                                isEnabled = newFollowers,
                                onToggle = { 
                                    newFollowers = it
                                    savePreference("new_followers", it)
                                },
                                iconColor = Color(0xFF1565A0)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            NotificationToggle(
                                icon = Icons.Outlined.Comment,
                                title = "Comentarios",
                                subtitle = "Nuevos comentarios en tus publicaciones",
                                isEnabled = comments,
                                onToggle = { 
                                    comments = it
                                    savePreference("comments", it)
                                },
                                iconColor = Color(0xFFFF6B35)
                            )
                            Divider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
                            NotificationToggle(
                                icon = Icons.Outlined.FavoriteBorder,
                                title = "Me gusta",
                                subtitle = "Cuando dan like a tus publicaciones",
                                isEnabled = likes,
                                onToggle = { 
                                    likes = it
                                    savePreference("likes", it)
                                },
                                iconColor = AccentPink
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    SettingsSectionTitle("Marketing")
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        NotificationToggle(
                            icon = Icons.Outlined.Campaign,
                            title = "Promociones y ofertas",
                            subtitle = "Descuentos especiales y novedades",
                            isEnabled = promotions,
                            onToggle = { 
                                promotions = it
                                savePreference("promotions", it)
                            },
                            iconColor = Color(0xFFFF6B35)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    } // end if (!isLoading)
                }
            }
        }
    }
}

@Composable
private fun NotificationToggle(
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
