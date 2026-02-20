package com.rendly.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.repository.UserPreferencesRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Professional mention popup with 4-column grid layout.
 * Each item shows avatar on top + @username below.
 * Appears above the text input when user types '@'.
 */
@Composable
fun MentionSuggestionPopup(
    isVisible: Boolean,
    query: String,
    onUserSelected: (username: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var users by remember { mutableStateOf<List<UserPreferencesRepository.MentionUserDB>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    
    // Search with debounce
    LaunchedEffect(query, isVisible) {
        if (!isVisible) return@LaunchedEffect
        
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(200) // debounce
            isLoading = true
            users = UserPreferencesRepository.searchUsersForMention(query)
            isLoading = false
        }
    }
    
    AnimatedVisibility(
        visible = isVisible && (users.isNotEmpty() || isLoading),
        enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
        exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = Surface,
            shadowElevation = 12.dp
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AlternateEmail,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (query.isEmpty()) "Mencionar usuario" else "Resultados para \"$query\"",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPurple
                    )
                    if (isLoading) {
                        Spacer(modifier = Modifier.weight(1f))
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = PrimaryPurple,
                            strokeWidth = 1.5.dp
                        )
                    }
                }
                
                Divider(color = BorderSubtle, thickness = 0.5.dp)
                
                // 4-column grid of users
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users, key = { it.userId }) { user ->
                        MentionUserGridItem(
                            user = user,
                            onClick = { onUserSelected(user.username) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MentionUserGridItem(
    user: UserPreferencesRepository.MentionUserDB,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            if (user.avatarUrl != null) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.username.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }
            }
            // Verified badge on avatar
            if (user.isVerified) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = Color(0xFF1D9BF0),
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // @username only
        Text(
            text = "@${user.username}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Helper to detect '@' mention trigger in text.
 * Returns the query after '@' if user is currently typing a mention, null otherwise.
 */
fun extractMentionQuery(text: String, cursorPosition: Int = text.length): String? {
    if (text.isEmpty() || cursorPosition <= 0) return null
    
    val beforeCursor = text.substring(0, minOf(cursorPosition, text.length))
    
    // Find the last '@' before cursor
    val atIndex = beforeCursor.lastIndexOf('@')
    if (atIndex < 0) return null
    
    // '@' must be at start or preceded by a space
    if (atIndex > 0 && beforeCursor[atIndex - 1] != ' ') return null
    
    // Extract query after '@'
    val query = beforeCursor.substring(atIndex + 1)
    
    // If query contains a space, mention is complete
    if (query.contains(' ')) return null
    
    return query
}

/**
 * Replaces the current @query with the selected username in the text.
 */
fun insertMention(text: String, username: String, cursorPosition: Int = text.length): String {
    val beforeCursor = text.substring(0, minOf(cursorPosition, text.length))
    val afterCursor = if (cursorPosition < text.length) text.substring(cursorPosition) else ""
    
    val atIndex = beforeCursor.lastIndexOf('@')
    if (atIndex < 0) return text
    
    val beforeAt = text.substring(0, atIndex)
    return "$beforeAt@$username $afterCursor"
}
