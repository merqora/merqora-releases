package com.rendly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SubdirectoryArrowLeft
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

data class Comment(
    val id: String,
    val userId: String = "",
    val username: String,
    val avatarUrl: String?,
    val text: String,
    val timeAgo: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val rating: Int = 5,
    val replies: List<Comment> = emptyList(),
    val replyCount: Int = 0,
    val isVerified: Boolean = false
)

@Composable
fun CommentsSheet(
    isVisible: Boolean,
    comments: List<Comment>,
    onDismiss: () -> Unit,
    onSendComment: (String, Int) -> Unit,
    onLikeComment: (String) -> Unit,
    onReplyComment: (String, String) -> Unit = { _, _ -> },
    onDeleteComment: (String) -> Unit = {},
    isLoading: Boolean = false,
    currentUserAvatar: String? = null,
    currentUsername: String = "Tú",
    currentUserId: String? = null,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Fallback: si currentUserId es null, obtenerlo directamente de auth
    val resolvedCurrentUserId = currentUserId 
        ?: com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
    
    var commentText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(5) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Estado para respuestas
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    // ID del comentario raíz cuando se responde a una respuesta (nested reply)
    var replyingToRootId by remember { mutableStateOf<String?>(null) }
    
    // Estado para modal de opciones (3 puntitos)
    var showOptionsForComment by remember { mutableStateOf<Comment?>(null) }
    
    // Estado para ReportModal
    var showReportForComment by remember { mutableStateOf<Comment?>(null) }
    
    // Backdrop animado
    AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(animationSpec = tween(300)),
        exit = androidx.compose.animation.fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )
    }
    
    // Bottom Sheet
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight() // Usar TODA la altura de la pantalla
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset > 100) {
                                    onDismiss()
                                }
                                dragOffset = 0f
                            },
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount > 0) {
                                    dragOffset += dragAmount
                                }
                            }
                        )
                    },
                color = HomeBg,
                shape = RoundedCornerShape(0.dp) // Sin border radius en pantalla completa
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Handle bar (draggable indicator)
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                    )
                    
                    // Header with close button and count
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Opiniones",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (comments.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PrimaryPurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${comments.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Divider(color = TextMuted.copy(alpha = 0.1f))
                    
                    // Error banner
                    androidx.compose.animation.AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = errorMessage ?: "",
                                        fontSize = 13.sp,
                                        color = Color(0xFFDC2626),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(
                                    onClick = onDismissError,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar error",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Comments list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isLoading) {
                            // Skeleton loading state
                            items(4) { index ->
                                CommentSkeletonItem()
                            }
                        } else if (comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.StarOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = TextMuted.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Sin opiniones aún",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Sé el primero en compartir tu experiencia",
                                            fontSize = 13.sp,
                                            color = TextMuted.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(comments, key = { it.id }) { comment ->
                                OpinionItem(
                                    comment = comment,
                                    onLike = { onLikeComment(comment.id) },
                                    onReply = {
                                        replyingTo = comment
                                        replyingToRootId = comment.id
                                        scope.launch {
                                            kotlinx.coroutines.delay(100)
                                            try { focusRequester.requestFocus() } catch (_: Exception) {}
                                        }
                                    },
                                    onOptions = { showOptionsForComment = comment },
                                    isOwnComment = resolvedCurrentUserId != null && comment.userId == resolvedCurrentUserId
                                )
                                
                                // Mostrar respuestas anidadas con línea vertical conectora (estilo Facebook)
                                if (comment.replies.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 18.dp, top = 4.dp)
                                    ) {
                                        comment.replies.forEachIndexed { index, reply ->
                                            val isLast = index == comment.replies.lastIndex
                                            ReplyItemConnected(
                                                reply = reply,
                                                onLike = { onLikeComment(reply.id) },
                                                onReply = {
                                                    replyingTo = reply
                                                    replyingToRootId = comment.id // Root parent
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(100)
                                                        try { focusRequester.requestFocus() } catch (_: Exception) {}
                                                    }
                                                },
                                                onOptions = { showOptionsForComment = reply },
                                                isOwnComment = resolvedCurrentUserId != null && reply.userId == resolvedCurrentUserId,
                                                isLast = isLast
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // ════════════════════════════════════════════════════════════
                    // INPUT FOOTER - Pegado al TabBar de Android (3 botones)
                    // ════════════════════════════════════════════════════════════
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TabBarBg)
                            .imePadding()
                            .navigationBarsPadding() // Padding para NavBar del sistema (3 botones Android)
                    ) {
                        Divider(color = TextMuted.copy(alpha = 0.1f))
                        
                        // Reply indicator banner
                        androidx.compose.animation.AnimatedVisibility(
                            visible = replyingTo != null,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = PrimaryPurple.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Respondiendo a @${replyingTo?.username ?: ""}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryPurple,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { replyingTo = null; replyingToRootId = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancelar respuesta",
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // @ Mention query - computed before rating row to control visibility
                        val mentionQuery = remember(commentText) { extractMentionQuery(commentText) }
                        
                        // Star Rating Row (ocultar cuando se responde O cuando mention popup está activo)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = replyingTo == null && mentionQuery == null
                        ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tu calificación",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                            
                            // Star selector
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Estrella $i",
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clickable { selectedRating = i },
                                        tint = if (i <= selectedRating) AccentYellow else TextMuted.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                        } // Cierre AnimatedVisibility rating
                        
                        // @ Mention Popup (aparece cuando el rating desaparece)
                        MentionSuggestionPopup(
                            isVisible = mentionQuery != null,
                            query = mentionQuery ?: "",
                            onUserSelected = { username ->
                                commentText = insertMention(commentText, username)
                            },
                            onDismiss = { /* popup hides when mentionQuery becomes null */ }
                        )
                        
                        // Input Row - Pegado al teclado
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            if (currentUserAvatar != null && currentUserAvatar.isNotEmpty()) {
                                AsyncImage(
                                    model = currentUserAvatar,
                                    contentDescription = "Tu avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUsername.firstOrNull()?.uppercase() ?: "T",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            // Text input
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                color = HomeBg
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    if (commentText.isEmpty()) {
                                        Text(
                                            text = if (replyingTo != null) "Escribe tu respuesta..." else "Escribe tu opinión...",
                                            color = TextMuted,
                                            fontSize = 14.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        textStyle = TextStyle(
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        ),
                                        cursorBrush = SolidColor(PrimaryPurple),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester),
                                        singleLine = false,
                                        maxLines = 3,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (commentText.isNotBlank() && !isSending) {
                                                    isSending = true
                                                    if (replyingTo != null) {
                                                        val parentId = replyingToRootId ?: replyingTo!!.id
                                                        val isNestedReply = replyingToRootId != null && replyingToRootId != replyingTo!!.id
                                                        val textToSend = if (isNestedReply) "@${replyingTo!!.username} $commentText" else commentText
                                                        onReplyComment(parentId, textToSend)
                                                        replyingTo = null
                                                        replyingToRootId = null
                                                    } else {
                                                        onSendComment(commentText, selectedRating)
                                                    }
                                                    commentText = ""
                                                    selectedRating = 5
                                                    focusManager.clearFocus()
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(300)
                                                        isSending = false
                                                    }
                                                }
                                            }
                                        )
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            // Send button
                            Surface(
                                onClick = {
                                    if (commentText.isNotBlank() && !isSending) {
                                        isSending = true
                                        if (replyingTo != null) {
                                            val parentId = replyingToRootId ?: replyingTo!!.id
                                            val isNestedReply = replyingToRootId != null && replyingToRootId != replyingTo!!.id
                                            val textToSend = if (isNestedReply) "@${replyingTo!!.username} $commentText" else commentText
                                            onReplyComment(parentId, textToSend)
                                            replyingTo = null
                                            replyingToRootId = null
                                        } else {
                                            onSendComment(commentText, selectedRating)
                                        }
                                        commentText = ""
                                        selectedRating = 5
                                        focusManager.clearFocus()
                                        scope.launch {
                                            kotlinx.coroutines.delay(300)
                                            isSending = false
                                        }
                                    }
                                },
                                shape = CircleShape,
                                color = if (commentText.isNotBlank()) PrimaryPurple else PrimaryPurple.copy(alpha = 0.3f),
                                enabled = commentText.isNotBlank() && !isSending
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Enviar",
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.White
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
    
    // Modal de opciones de opinión (3 puntitos)
    CommentOptionsSheet(
        isVisible = showOptionsForComment != null,
        isOwnComment = showOptionsForComment != null && resolvedCurrentUserId != null && showOptionsForComment!!.userId == resolvedCurrentUserId,
        onDismiss = { showOptionsForComment = null },
        onDelete = {
            showOptionsForComment?.let { comment ->
                onDeleteComment(comment.id)
            }
            showOptionsForComment = null
        },
        onReport = {
            showReportForComment = showOptionsForComment
            showOptionsForComment = null
        }
    )
    
    // ReportModal para reportar opiniones de otros usuarios
    ReportModal(
        isVisible = showReportForComment != null,
        contentType = "comment",
        contentId = showReportForComment?.id ?: "",
        username = showReportForComment?.username ?: "",
        onDismiss = { showReportForComment = null },
        onSubmitReport = { reason, description ->
            showReportForComment?.let { comment ->
                // Enviar reporte a Supabase
                scope.launch {
                    try {
                        val reporterId = com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id
                        if (reporterId != null) {
                            com.rendly.app.data.remote.SupabaseClient.database
                                .from("content_reports")
                                .insert(kotlinx.serialization.json.buildJsonObject {
                                    put("reporter_id", kotlinx.serialization.json.JsonPrimitive(reporterId))
                                    put("content_type", kotlinx.serialization.json.JsonPrimitive("comment"))
                                    put("content_id", kotlinx.serialization.json.JsonPrimitive(comment.id))
                                    put("reported_user_id", kotlinx.serialization.json.JsonPrimitive(comment.userId))
                                    put("reason", kotlinx.serialization.json.JsonPrimitive(reason))
                                    if (description != null) {
                                        put("description", kotlinx.serialization.json.JsonPrimitive(description))
                                    }
                                })
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CommentsSheet", "Error enviando reporte: ${e.message}")
                    }
                }
            }
            showReportForComment = null
        }
    )
}

@Composable
private fun OpinionItem(
    comment: Comment,
    onLike: () -> Unit,
    onReply: () -> Unit = {},
    onOptions: () -> Unit = {},
    isOwnComment: Boolean = false
) {
    val avatarUrl = remember(comment.avatarUrl, comment.username) {
        when {
            comment.avatarUrl.isNullOrEmpty() -> "https://ui-avatars.com/api/?name=${comment.username}&background=A78BFA&color=fff"
            comment.avatarUrl.startsWith("http") -> comment.avatarUrl
            else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${comment.avatarUrl}"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        // Header: Avatar + Name + Time + Stars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (comment.username.isNotEmpty()) comment.username else "Usuario",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (comment.isVerified) {
                            VerifiedBadge(size = 14.dp)
                        }
                    }
                    Text(
                        text = comment.timeAgo,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Star Rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= comment.rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (i <= comment.rating) AccentYellow else TextMuted.copy(alpha = 0.3f)
                        )
                    }
                }
                
                // 3-dot options menu
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onOptions),
                    tint = TextMuted.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Opinion text
        Text(
            text = comment.text,
            fontSize = 14.sp,
            color = TextPrimary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Actions: Helpful + Reply
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onLike),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Útil",
                    modifier = Modifier.size(16.dp),
                    tint = if (comment.isLiked) AccentPink else TextMuted
                )
                Text(
                    text = if (comment.likes > 0) "Útil (${comment.likes})" else "Útil",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (comment.isLiked) AccentPink else TextMuted
                )
            }
            
            // Reply button
            Row(
                modifier = Modifier.clickable(onClick = onReply),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.SubdirectoryArrowLeft,
                    contentDescription = "Responder",
                    modifier = Modifier.size(16.dp),
                    tint = TextMuted
                )
                Text(
                    text = if (comment.replyCount > 0) "Responder (${comment.replyCount})" else "Responder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ReplyItemConnected(
    reply: Comment,
    onLike: () -> Unit,
    onReply: () -> Unit = {},
    onOptions: () -> Unit = {},
    isOwnComment: Boolean = false,
    isLast: Boolean = false
) {
    val avatarUrl = remember(reply.avatarUrl, reply.username) {
        when {
            reply.avatarUrl.isNullOrEmpty() -> "https://ui-avatars.com/api/?name=${reply.username}&background=A78BFA&color=fff"
            reply.avatarUrl.startsWith("http") -> reply.avatarUrl
            else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${reply.avatarUrl}"
        }
    }
    
    val lineColor = PrimaryPurple.copy(alpha = 0.25f)
    val density = LocalDensity.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical + branch line connector (Canvas)
        Canvas(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
        ) {
            val strokeWidth = with(density) { 1.5.dp.toPx() }
            val lineX = with(density) { 0.dp.toPx() }
            val branchY = with(density) { 20.dp.toPx() }
            val branchEndX = with(density) { 20.dp.toPx() }
            
            // Vertical line (from top to branch point, or full height if not last)
            drawLine(
                color = lineColor,
                start = Offset(lineX, 0f),
                end = Offset(lineX, if (isLast) branchY else size.height),
                strokeWidth = strokeWidth
            )
            
            // Horizontal branch line (connects to reply card)
            drawLine(
                color = lineColor,
                start = Offset(lineX, branchY),
                end = Offset(branchEndX, branchY),
                strokeWidth = strokeWidth
            )
        }
        
        // Reply content card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface.copy(alpha = 0.4f))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                    )
                    
                    Text(
                        text = reply.username.ifEmpty { "Usuario" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (reply.isVerified) {
                        VerifiedBadge(size = 12.dp)
                    }
                    Text(
                        text = "·",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = reply.timeAgo,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                
                // 3-dot options menu
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onOptions),
                    tint = TextMuted.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Style @username mention with distinct color
            val styledText = remember(reply.text) {
                if (reply.text.startsWith("@")) {
                    val spaceIndex = reply.text.indexOf(' ')
                    if (spaceIndex > 0) {
                        val mention = reply.text.substring(0, spaceIndex)
                        val rest = reply.text.substring(spaceIndex)
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = PrimaryPurple, fontWeight = FontWeight.SemiBold)) {
                                append(mention)
                            }
                            withStyle(SpanStyle(color = TextPrimary.copy(alpha = 0.9f))) {
                                append(rest)
                            }
                        }
                    } else {
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = TextPrimary.copy(alpha = 0.9f))) {
                                append(reply.text)
                            }
                        }
                    }
                } else {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = TextPrimary.copy(alpha = 0.9f))) {
                            append(reply.text)
                        }
                    }
                }
            }
            
            Text(
                text = styledText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Actions: Like + Reply
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onLike),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (reply.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Útil",
                        modifier = Modifier.size(13.dp),
                        tint = if (reply.isLiked) AccentPink else TextMuted
                    )
                    Text(
                        text = if (reply.likes > 0) "Útil (${reply.likes})" else "Útil",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (reply.isLiked) AccentPink else TextMuted
                    )
                }
                
                // Reply button
                Row(
                    modifier = Modifier.clickable(onClick = onReply),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.SubdirectoryArrowLeft,
                        contentDescription = "Responder",
                        modifier = Modifier.size(13.dp),
                        tint = TextMuted
                    )
                    Text(
                        text = "Responder",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SKELETON LOADING ITEM
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun CommentSkeletonItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            SurfaceElevated,
            Surface.copy(alpha = 0.5f),
            SurfaceElevated
        ),
        start = Offset(shimmerOffset.value - 500f, 0f),
        end = Offset(shimmerOffset.value, 0f)
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Header: Avatar + Name + Stars
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar skeleton
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(shimmerBrush)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Username skeleton
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Time skeleton
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
            
            // Stars skeleton
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Comment text skeleton - multiple lines
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Helpful button skeleton
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMMENT OPTIONS BOTTOM SHEET
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun CommentOptionsSheet(
    isVisible: Boolean,
    isOwnComment: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    // Backdrop
    AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(animationSpec = tween(200)),
        exit = androidx.compose.animation.fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() }
        )
    }
    
    // Bottom sheet
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(250)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = SurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                    )
                    
                    if (isOwnComment) {
                        // Opciones para opiniones propias
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDelete() }
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Eliminar opinión",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFEF5350)
                                )
                            }
                        }
                    } else {
                        // Opciones para opiniones de otros
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onReport() }
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Flag,
                                    contentDescription = null,
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Reportar opinión",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFEF5350)
                                )
                            }
                        }
                    }
                    
                    // Cancelar button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Cancelar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
