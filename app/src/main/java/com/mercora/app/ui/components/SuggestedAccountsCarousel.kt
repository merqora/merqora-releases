package com.mercora.app.ui.components

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.FollowersRepository
import com.mercora.app.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 * SUGGESTED ACCOUNTS CAROUSEL - "Personas que quizÃ¡s conozcas"
 * â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
 * Carrusel horizontal profesional con cuentas sugeridas.
 * Cada tarjeta muestra avatar, username, nombre de tienda y botÃ³n de seguir.
 * DiseÃ±o Ãºnico tipo tarjeta con gradiente sutil, no copia Instagram.
 */

data class SuggestedAccount(
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val storeName: String?,
    val isVerified: Boolean = false,
    val followersCount: Int = 0,
    val postsCount: Int = 0
)

// CachÃ© de sesiÃ³n: el fetch de sugerencias corre UNA vez por proceso, no cada
// vez que el LazyColumn del Home recicla este item durante scroll
private object SuggestedAccountsCache {
    @Volatile
    var accounts: List<SuggestedAccount>? = null
}

@Composable
fun SuggestedAccountsCarousel(
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var accounts by remember { mutableStateOf(SuggestedAccountsCache.accounts ?: emptyList()) }
    var followedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(SuggestedAccountsCache.accounts == null) }
    var dismissedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Load suggested accounts
    LaunchedEffect(Unit) {
        if (SuggestedAccountsCache.accounts != null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
                
                // Get users the current user already follows
                val followedRelations = SupabaseClient.database
                    .from("followers")
                    .select {
                        filter { eq("follower_id", currentUserId) }
                    }
                    .decodeList<com.vinzay.app.data.repository.FollowerRelation>()
                val alreadyFollowedIds = followedRelations.map { it.followedId }.toSet()
                
                // Get suggested users (not self, not already followed, active accounts)
                val users = SupabaseClient.database
                    .from("usuarios")
                    .select()
                    .decodeList<Usuario>()
                    .filter { user ->
                        user.userId != currentUserId &&
                        user.userId !in alreadyFollowedIds &&
                        !user.baneado &&
                        !user.isAnonymous &&
                        user.username.isNotBlank()
                    }
                    .sortedByDescending { user ->
                        // Priorizar: verificados > con tienda > con avatar > resto
                        var score = 0
                        if (user.isVerified) score += 100
                        if (user.tieneTienda) score += 50
                        if (!user.avatarUrl.isNullOrBlank()) score += 25
                        if ((user.reputationScore ?: 0.0) >= 50.0) score += 10
                        score
                    }
                    .take(20)
                    .shuffled()
                    .take(12)
                
                accounts = users.map { user ->
                    SuggestedAccount(
                        userId = user.userId,
                        username = user.username,
                        avatarUrl = user.avatarUrl,
                        storeName = user.nombreTienda,
                        isVerified = user.isVerified,
                        followersCount = 0,
                        postsCount = 0
                    )
                }.also { SuggestedAccountsCache.accounts = it }
            } catch (e: Exception) {
                Log.e("SuggestedAccounts", "Error loading: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    val visibleAccounts = accounts.filter { it.userId !in dismissedIds }
    
    // No mostrar el componente hasta que haya al menos 5 cuentas recomendadas
    if (visibleAccounts.size < 5 && !isLoading) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
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
                    text = "Cuentas recomendadas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
        
        if (isLoading) {
            // Skeleton loading
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4) {
                    SuggestedAccountSkeleton()
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = visibleAccounts,
                    key = { it.userId }
                ) { account ->
                    SuggestedAccountCard(
                        account = account,
                        isFollowed = account.userId in followedIds,
                        onFollowClick = {
                            scope.launch {
                                val result = FollowersRepository.follow(account.userId)
                                if (result.isSuccess) {
                                    followedIds = followedIds + account.userId
                                }
                            }
                        },
                        onDismiss = {
                            dismissedIds = dismissedIds + account.userId
                        },
                        onProfileClick = { onProfileClick(account.userId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestedAccountCard(
    account: SuggestedAccount,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    onDismiss: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val avatarUrl = remember(account.avatarUrl) {
        val url = account.avatarUrl ?: ""
        if (url.startsWith("http")) url
        else if (url.isNotBlank()) "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/$url"
        else ""
    }
    
    val cardGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                SurfaceElevated,
                Surface
            )
        )
    }
    
    Box(
        modifier = Modifier.width(160.dp)
    ) {
        // Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onProfileClick),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .background(cardGradient, RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BorderSubtle.copy(alpha = 0.6f),
                                BorderSubtle.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Dismiss button (top right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Descartar",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    
                    // Avatar with gradient ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(68.dp)
                    ) {
                        // Gradient ring
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryPurple,
                                            AccentPink,
                                            AccentGold
                                        )
                                    )
                                )
                        )
                        // White gap
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Surface)
                        )
                        // Avatar
                        if (avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = remember(avatarUrl) {
                                    ImageRequest.Builder(context)
                                        .data(avatarUrl)
                                        .crossfade(100)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .size(128)
                                        .build()
                                },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        
                        // Verified badge overlay
                        if (account.isVerified) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-2).dp, y = (-2).dp)
                            ) {
                                VerifiedBadge(size = 18.dp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Username
                    Text(
                        text = account.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Store name
                    if (!account.storeName.isNullOrBlank()) {
                        Text(
                            text = account.storeName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Follow button
                    if (isFollowed) {
                        // Already following state
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                BorderSubtle
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Siguiendo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        // Follow button with gradient
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onFollowClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Seguir",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedAccountSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    
    Surface(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // Avatar skeleton
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(TextMuted.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Username skeleton
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TextMuted.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Store name skeleton
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TextMuted.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Button skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TextMuted.copy(alpha = alpha))
            )
        }
    }
}
