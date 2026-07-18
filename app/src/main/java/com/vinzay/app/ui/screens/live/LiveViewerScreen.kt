package com.vinzay.app.ui.screens.live

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vinzay.app.data.livekit.LiveInteractionManager
import com.vinzay.app.data.livekit.LiveKitManager
import com.vinzay.app.data.livekit.LiveKitState
import com.vinzay.app.data.model.Post
import com.vinzay.app.data.repository.LiveStream
import com.vinzay.app.data.repository.LiveStreamRepository
import com.vinzay.app.data.repository.PostRepository
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sin

private val ViewerDarkBg = Color(0xFF0D0B14)
private val ViewerSurfaceDark = Color(0xFF1C1828)
private val ViewerPurple = Color(0xFF7C3AED)
private val ViewerPink = Color(0xFFEF4444)
private val ViewerBlue = Color(0xFF3B82F6)
private val ViewerGlass = Color(0x55000000)

@Composable
fun LiveViewerScreen(
    streamId: String,
    onClose: () -> Unit,
    onNavigateToProduct: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var stream by remember { mutableStateOf<LiveStream?>(null) }
    var isLoadingStream by remember { mutableStateOf(true) }
    var broadcasterPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var featuredPost by remember { mutableStateOf<Post?>(null) }

    val liveKitManager = remember { LiveKitManager(context) }
    val liveKitState by liveKitManager.state.collectAsState()
    val viewerCount by liveKitManager.viewerCount.collectAsState()

    var interactionManager by remember { mutableStateOf<LiveInteractionManager?>(null) }
    val comments by interactionManager?.comments?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    var commentInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()

    val pendingReactions = remember { mutableStateListOf<Triple<String, String, Long>>() }

    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(streamId) {
        isLoadingStream = true
        val cached = LiveStreamRepository.activeStreams.value.find { it.id == streamId }
        stream = cached ?: LiveStreamRepository.getStreamById(streamId)
        isLoadingStream = false
    }

    LaunchedEffect(stream) {
        val roomName = stream?.roomName ?: return@LaunchedEffect
        liveKitManager.connectAsViewer(roomName)
        val mgr = LiveInteractionManager(roomName)
        interactionManager = mgr
        mgr.connect()
        scope.launch { LiveStreamRepository.incrementViewerCount(streamId) }

        // Cargar productos reales del broadcaster
        val broadcasterId = stream?.broadcasterId
        if (broadcasterId != null) {
            val posts = PostRepository.getPostsByUserId(broadcasterId)
            broadcasterPosts = posts
            featuredPost = posts.firstOrNull()
        }
    }

    LaunchedEffect(interactionManager) {
        interactionManager?.reactions?.collect { reaction ->
            val id = UUID.randomUUID().toString()
            pendingReactions.add(Triple(id, reaction.type, System.currentTimeMillis()))
            if (pendingReactions.size > 30) pendingReactions.removeAt(0)
        }
    }

    LaunchedEffect(comments) {
        if (comments.isNotEmpty()) chatListState.animateScrollToItem(comments.size - 1)
    }

    DisposableEffect(Unit) {
        onDispose {
            liveKitManager.disconnect()
            interactionManager?.disconnect()
            scope.launch { LiveStreamRepository.decrementViewerCount(streamId) }
        }
    }

    fun sendComment() {
        val text = commentInput.trim()
        if (text.isEmpty()) return
        interactionManager?.let { mgr ->
            scope.launch {
                val session = com.vinzay.app.data.remote.SupabaseClient.auth.currentSessionOrNull()
                val uid = session?.user?.id ?: "local"
                val uname = session?.user?.userMetadata?.get("username")?.toString()?.removeSurrounding("\"") ?: "Espectador"
                mgr.sendComment(uid, uname, text)
            }
        }
        commentInput = ""
    }

    fun sendLike() {
        interactionManager?.let { mgr ->
            scope.launch {
                val session = com.vinzay.app.data.remote.SupabaseClient.auth.currentSessionOrNull()
                val uid = session?.user?.id ?: "local"
                val uname = session?.user?.userMetadata?.get("username")?.toString()?.removeSurrounding("\"") ?: "Espectador"
                mgr.sendLike(uid, uname)
            }
        }
        val id = UUID.randomUUID().toString()
        pendingReactions.add(Triple(id, "❤️", System.currentTimeMillis()))
        if (pendingReactions.size > 30) pendingReactions.removeAt(0)
    }

    if (isLoadingStream) {
        Box(modifier.fillMaxSize().background(ViewerDarkBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = ViewerPink, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(14.dp))
                Text("Cargando transmisión...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
        return
    }

    Box(modifier = modifier.fillMaxSize().background(ViewerDarkBg)) {

        // ── Video background ──
        val remoteTrack by liveKitManager.remoteVideoTrack.collectAsState()
        val room = liveKitManager.roomRef
        if (room != null && remoteTrack != null) {
            io.livekit.android.compose.VideoRenderer(
                room = room,
                videoTrack = remoteTrack!!,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.radialGradient(listOf(Color(0xFF2D1B69), ViewerDarkBg))
                ),
                contentAlignment = Alignment.Center
            ) {
                if (liveKitState !is LiveKitState.Error) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ViewerPink, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(14.dp))
                        Text("Conectando al live...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }
        }

        // Gradient overlays
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp).align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(320.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))))
        )

        // ── Main content: two columns ──
        Row(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {

            // ═══════════════════════════════════════
            // LEFT COLUMN
            // ═══════════════════════════════════════
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {

                // ── TOP BAR ──
                Column(
                    modifier = Modifier.align(Alignment.TopStart)
                        .padding(top = 8.dp, start = 12.dp, end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "vinzay",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            // LIVE badge
                            Surface(shape = RoundedCornerShape(4.dp), color = ViewerPink) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(5.dp)
                                            .graphicsLayer { alpha = pulseAlpha }
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Text("LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            // Viewer count
                            Surface(shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.5f)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Filled.Visibility, null, tint = Color.White, modifier = Modifier.size(11.dp))
                                    Text("$viewerCount", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        // Close
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Streamer info row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (stream?.broadcasterAvatar != null) {
                            AsyncImage(
                                model = stream!!.broadcasterAvatar,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                                    .border(1.5.dp, ViewerPurple, CircleShape)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(36.dp)
                                    .border(1.5.dp, ViewerPurple, CircleShape)
                                    .clip(CircleShape)
                                    .background(ViewerPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stream?.broadcasterUsername?.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                stream?.broadcasterUsername ?: "...",
                                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            if (stream?.broadcasterStoreName != null) {
                                Text(
                                    stream!!.broadcasterStoreName!!,
                                    fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ViewerPurple,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                        ) {
                            Text(
                                "Seguir",
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // ── BOTTOM SECTION ──
                Column(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Featured product card
                    if (featuredPost != null) {
                        ViewerFeaturedProductCard(
                            post = featuredPost!!,
                            onNavigate = { onNavigateToProduct(featuredPost!!.id) },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Chat messages
                    if (comments.isNotEmpty()) {
                        val visible = comments.takeLast(6)
                        Box(
                            modifier = Modifier.fillMaxWidth().height(124.dp).padding(start = 8.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            LazyColumn(
                                state = chatListState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                contentPadding = PaddingValues(bottom = 2.dp)
                            ) {
                                itemsIndexed(visible) { idx, comment ->
                                    val fadeAlpha = ((idx + 1).toFloat() / visible.size).coerceIn(0.35f, 1f)
                                    ViewerChatBubble(username = comment.username, text = comment.text, alpha = fadeAlpha)
                                }
                            }
                        }
                    }

                    // Comment input row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = {
                                Text("Escribe algo...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedContainerColor = ViewerGlass,
                                unfocusedContainerColor = ViewerGlass
                            ),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendComment() })
                        )
                        // Heart button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { sendLike() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("❤️", fontSize = 16.sp)
                            }
                        }
                        // Send button
                        Surface(
                            shape = CircleShape,
                            color = ViewerPurple,
                            modifier = Modifier.size(38.dp).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { sendComment() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Código rápido bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(ViewerGlass)
                            .padding(start = 12.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Código rápido",
                            fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { codeInput = it },
                            placeholder = {
                                Text("ingresá código...", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f).height(38.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ViewerPurple,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* TODO: buscar producto por código */ }
                        ) {
                            Text(
                                "Buscar",
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }

            // ═══════════════════════════════════════
            // RIGHT COLUMN: Products panel
            // ═══════════════════════════════════════
            Column(
                modifier = Modifier
                    .width(158.dp)
                    .fillMaxHeight()
                    .padding(top = 6.dp, end = 6.dp, bottom = 8.dp)
            ) {
                Text(
                    "Productos en vivo (${broadcasterPosts.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(broadcasterPosts) { post ->
                        ViewerProductCard(
                            post = post,
                            isActive = post.id == featuredPost?.id,
                            onBuy = { onNavigateToProduct(post.id) }
                        )
                    }
                }
            }
        }

        // ── Floating hearts overlay ──
        Box(modifier = Modifier.fillMaxSize()) {
            pendingReactions.forEachIndexed { _, (id, emoji, createdAt) ->
                key(id) {
                    ViewerFloatingHeart(emoji = emoji, createdAt = createdAt)
                }
            }
        }

        // ── Error overlay ──
        if (liveKitState is LiveKitState.Error) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WifiOff, null, tint = ViewerPink, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Error de conexión", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (liveKitState as LiveKitState.Error).message,
                        color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) { Text("Cerrar", color = Color.White) }
                }
            }
        }
    }
}

// ═══════════════════════ COMPONENTS ═══════════════════════

@Composable
private fun ViewerFeaturedProductCard(
    post: Post,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = post.images.firstOrNull()
    val priceText = if (post.price > 0) "$${post.price.toLong()}" else ""

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ViewerSurfaceDark.copy(alpha = 0.9f),
        modifier = modifier.width(210.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF2D1B69)) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ShoppingBag, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Surface(shape = RoundedCornerShape(3.dp), color = ViewerPink) {
                    Text(
                        "DESTACADO",
                        fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(post.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, lineHeight = 13.sp)
                if (priceText.isNotEmpty()) {
                    Text(priceText, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ViewerBlue,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigate() }
                ) {
                    Text(
                        "Ver producto",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerChatBubble(username: String, text: String, alpha: Float = 1f) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .widthIn(max = 280.dp)
            .graphicsLayer { this.alpha = alpha.coerceIn(0f, 1f) }
    ) {
        Surface(shape = CircleShape, color = ViewerPurple) {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Text(username.take(1).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Surface(shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.55f)) {
            Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)) {
                Text("$username ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB97FFC))
                Text(text, fontSize = 12.sp, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ViewerProductCard(
    post: Post,
    isActive: Boolean = false,
    onBuy: () -> Unit
) {
    val imageUrl = post.images.firstOrNull()
    val priceText = if (post.price > 0) "$${post.price.toLong()}" else ""

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) ViewerPurple.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.45f),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, ViewerPurple) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                Surface(shape = RoundedCornerShape(8.dp), color = ViewerSurfaceDark) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingBag, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        post.title,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        maxLines = 2, lineHeight = 12.sp, overflow = TextOverflow.Ellipsis
                    )
                    if (post.category.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(3.dp), color = ViewerBlue.copy(alpha = 0.2f)) {
                            Text(
                                post.category,
                                fontSize = 7.sp, fontWeight = FontWeight.Medium, color = ViewerBlue,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (priceText.isNotEmpty()) {
                        Text(priceText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ViewerPurple,
                modifier = Modifier.fillMaxWidth().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBuy() }
            ) {
                Text(
                    "Comprar",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ViewerFloatingHeart(emoji: String, createdAt: Long) {
    val alpha = remember { Animatable(1f) }
    val offsetY = remember { Animatable(0f) }
    val swayX = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { offsetY.animateTo(-260f, tween(2800, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(0f, tween(2800, easing = FastOutSlowInEasing)) }
        launch { swayX.animateTo(360f, tween(2200, easing = LinearEasing)) }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(end = 170.dp, bottom = 180.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp,
            modifier = Modifier
                .offset(
                    x = (sin(swayX.value * (Math.PI.toFloat() / 180f)) * 18f).dp,
                    y = offsetY.value.dp
                )
                .graphicsLayer { this.alpha = alpha.value.coerceIn(0f, 1f) }
        )
    }
}
