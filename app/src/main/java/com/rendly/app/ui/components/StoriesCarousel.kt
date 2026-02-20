package com.rendly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import androidx.compose.ui.platform.LocalContext
import com.rendly.app.data.repository.StoryRepository
import com.rendly.app.ui.theme.*

/**
 * Carrusel horizontal de stories de otros usuarios
 * Se muestra debajo de MyStoryBanner cuando hay stories disponibles
 * Si no hay stories, el componente NO se muestra
 */
@Composable
fun StoriesCarousel(
    stories: List<StoryRepository.StoryWithUser>,
    viewedStoryIds: Set<String> = emptySet(), // IDs de stories ya vistas
    onStoryClick: (String, Int) -> Unit, // userId, index
    modifier: Modifier = Modifier
) {
    // Si no hay stories, no mostrar nada
    if (stories.isEmpty()) return
    
    // OPTIMIZADO: Memoizar groupBy para evitar recálculo en cada recomposición
    val storiesByUser = remember(stories) { stories.groupBy { it.userId } }
    val usersList = remember(storiesByUser) { storiesByUser.keys.toList() }
    
    // OPTIMIZADO: Pre-calcular estado de visto por usuario para evitar .all{} en cada frame
    val viewedStatusByUser = remember(storiesByUser, viewedStoryIds) {
        storiesByUser.mapValues { (_, userStories) ->
            userStories.all { it.story.id in viewedStoryIds }
        }
    }
    
    // OPTIMIZADO: Pre-calcular datos de cada usuario para evitar lookups en LazyRow
    val usersData = remember(storiesByUser, viewedStatusByUser) {
        usersList.mapIndexed { index, userId ->
            val userStories = storiesByUser[userId] ?: emptyList()
            val firstStory = userStories.firstOrNull()
            if (firstStory != null) {
                StoryUserData(
                    userId = userId,
                    index = index,
                    username = firstStory.username,
                    avatarUrl = firstStory.avatarUrl,
                    storiesCount = userStories.size,
                    isViewed = viewedStatusByUser[userId] ?: false
                )
            } else null
        }.filterNotNull()
    }
    
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(
            items = usersData,
            key = { it.userId },
            contentType = { "story_avatar" }
        ) { userData ->
            // OPTIMIZADO: Callback memoizado con datos pre-calculados
            val onClickCallback = remember(userData.userId, userData.index) { 
                { onStoryClick(userData.userId, userData.index) } 
            }
            
            StoryAvatarItem(
                username = userData.username,
                avatarUrl = userData.avatarUrl,
                storiesCount = userData.storiesCount,
                isViewed = userData.isViewed,
                onClick = onClickCallback
            )
        }
    }
}

// Data class para pre-calcular datos de usuario
private data class StoryUserData(
    val userId: String,
    val index: Int,
    val username: String,
    val avatarUrl: String?,
    val storiesCount: Int,
    val isViewed: Boolean
)

@Composable
private fun StoryAvatarItem(
    username: String,
    avatarUrl: String?,
    storiesCount: Int,
    isViewed: Boolean = false,
    onClick: () -> Unit
) {
    // OPTIMIZADO: Memoizar gradientes para evitar recreación en cada frame
    val storyGradient = remember(isViewed) {
        if (isViewed) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4B5563),
                    Color(0xFF6B7280),
                    Color(0xFF4B5563)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00D4FF),
                    Color(0xFF7B2FFF),
                    Color(0xFFFF00E5)
                )
            )
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        // Avatar con borde gradiente
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Borde gradiente
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(storyGradient)
            )
            
            // Espaciador blanco/oscuro
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(HomeBg)
            )
            
            // Avatar optimizado con caché
            val context = LocalContext.current
            val finalAvatarUrl = remember(avatarUrl, username) {
                avatarUrl ?: "https://ui-avatars.com/api/?name=$username&background=A78BFA&color=fff&size=200"
            }
            AsyncImage(
                model = remember(finalAvatarUrl) {
                    ImageRequest.Builder(context)
                        .data(finalAvatarUrl)
                        .crossfade(100)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(128)
                        .build()
                },
                contentDescription = "Avatar de $username",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            
            // Badge de cantidad removido - ya no se muestra el número de stories
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Username
        Text(
            text = username,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
