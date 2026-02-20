package com.rendly.app.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.content.ContextCompat
import java.io.File
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.repository.ChatRepository
import com.rendly.app.data.repository.Message
import com.rendly.app.data.repository.MessageStatus
import com.rendly.app.data.repository.FollowersRepository
import com.rendly.app.data.repository.HandshakeRepository
import com.rendly.app.data.repository.ReputationRepository
import com.rendly.app.data.repository.VerificationRepository
import com.rendly.app.data.repository.ProfileRepository
import com.rendly.app.data.repository.PostRepository
import com.rendly.app.data.model.*
import com.rendly.app.ui.components.ClientRequestMessageBubble
import com.rendly.app.ui.components.VerifiedBadge
import com.rendly.app.ui.components.HandshakeProposalModal
import com.rendly.app.ui.components.HandshakeActiveBanner
import com.rendly.app.ui.components.HandshakeBannerState
import com.rendly.app.ui.components.CancelConfirmationModal
import com.rendly.app.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rendly.app.ui.components.MentionSuggestionPopup
import com.rendly.app.ui.components.extractMentionQuery
import com.rendly.app.ui.components.insertMention
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.rendly.app.ui.components.UnifiedProductCard
import com.rendly.app.ui.components.toProductCardData
import com.rendly.app.ui.components.CallScreen
import com.rendly.app.ui.components.IncomingCallOverlay
import com.rendly.app.data.repository.CallRepository
import com.rendly.app.data.model.CallStatus
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

// Data class para posts compartidos
data class SharedPostData(
    val postId: String,
    val images: List<String>,
    val title: String,
    val price: Double,
    val ownerUsername: String,
    val ownerAvatar: String,
    val customMessage: String
)

// Data class para usuarios compartidos
data class SharedUserData(
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val storeName: String,
    val reputation: Double,
    val isVerified: Boolean,
    val bannerUrl: String = "",
    val followers: Int = 0,
    val clients: Int = 0,
    val postsCount: Int = 0
)

// Data class para consultas/ofertas de productos
data class ConsultPostData(
    val postId: String,
    val productTitle: String,
    val productPrice: Double,
    val productImage: String,
    val message: String,
    val type: String // "offer" o "inquiry"
)

@Composable
fun ChatScreen(
    otherUser: Usuario,
    conversationId: String? = null,
    onBack: () -> Unit,
    onOpenChatList: () -> Unit = onBack, // Callback para ir al listado de chats
    onOpenProduct: ((String) -> Unit)? = null, // Callback para abrir ProductPage con postId
    onNavigateToUserProfile: ((String) -> Unit)? = null, // Callback para navegar al perfil del usuario
    modifier: Modifier = Modifier
) {
    val messages by ChatRepository.currentMessages.collectAsState()
    val isOtherUserTyping by ChatRepository.isOtherUserTyping.collectAsState()
    val isOtherUserOnline by ChatRepository.isOtherUserOnline.collectAsState()
    val lastError by ChatRepository.lastError.collectAsState()
    val realtimeStatus by ChatRepository.realtimeStatus.collectAsState()
    val hasMoreMessages by ChatRepository.hasMoreMessagesFlow.collectAsState()
    val isLoadingMoreFromRepo by ChatRepository.isLoadingMore.collectAsState()
    val scope = rememberCoroutineScope()
    val currentUserId = remember { com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id }
    val listState = rememberLazyListState()
    
    var messageText by remember { mutableStateOf("") }
    var currentConversationId by remember { mutableStateOf(conversationId) }
    
    // Estado para controlar si ya se hizo el scroll inicial
    var initialScrollDone by remember { mutableStateOf(false) }
    var messagesReady by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    
    // Estado para modal de opciones de mensaje
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var showMessageOptionsModal by remember { mutableStateOf(false) }
    
    // Estado para edición de mensaje
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    
    // Estado para reenvío de posts compartidos
    var sharedPostToForward by remember { mutableStateOf<SharedPostData?>(null) }
    var showForwardSharedPostModal by remember { mutableStateOf(false) }
    
    // Estado para envío de media
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingMedia by remember { mutableStateOf(false) }
    
    // Estado para modal de handshake (confirmación de compra)
    var showHandshakeModal by remember { mutableStateOf(false) }
    var pendingHandshakeId by remember { mutableStateOf<String?>(null) }
    var isWaitingForAcceptance by remember { mutableStateOf(false) }
    
    // Estado para modal de ajustes del chat (3 puntitos)
    var showChatSettingsModal by remember { mutableStateOf(false) }
    
    // Estado para búsqueda inline en el header
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Message>>(emptyList()) }
    var currentSearchIndex by remember { mutableStateOf(-1) }
    var isSearching by remember { mutableStateOf(false) }
    
    // Estado de bloqueo del otro usuario
    var isOtherUserBlocked by remember { mutableStateOf(false) }
    
    // Estados para sistema de handshake en tiempo real
    val pendingProposals by HandshakeRepository.pendingProposals.collectAsState()
    val activeHandshake by HandshakeRepository.activeHandshake.collectAsState()
    var showProposalModal by remember { mutableStateOf(false) }
    var currentProposal by remember { mutableStateOf<HandshakeTransaction?>(null) }
    
    // Estados para modales de cancelar y completado
    var showCancelConfirmModal by remember { mutableStateOf(false) }
    var completedHandshakeInfo by remember { mutableStateOf<HandshakeTransaction?>(null) }
    var cancelledHandshakeInfo by remember { mutableStateOf<HandshakeTransaction?>(null) } // Para mantener referencia durante animación de cancelación
    var animateReputationBadge by remember { mutableStateOf(false) }
    var lastReputationChange by remember { mutableIntStateOf(0) } // +3, +4, -1, -5, etc.
    var showHandshakeBanner by remember { mutableStateOf(false) }
    var handshakeBannerState by remember { mutableStateOf(HandshakeBannerState.WAITING) }
    var completedBannerDismissed by remember { mutableStateOf(false) }
    
    // Handshake creado localmente (para mostrar banner antes de que Realtime sincronice)
    var localCreatedHandshake by remember { mutableStateOf<HandshakeTransaction?>(null) }
    
    // Reputación en tiempo real desde Supabase
    val otherUserReputation by ReputationRepository.otherUserReputation.collectAsState()
    val currentUserReputation by ReputationRepository.currentUserReputation.collectAsState()
    
    // Verificación en tiempo real del otro usuario
    val isOtherUserVerified by VerificationRepository.otherUserVerified.collectAsState()
    
    // Estado de llamadas
    val callState by CallRepository.callState.collectAsState()
    val incomingCall by CallRepository.incomingCall.collectAsState()
    var showCallScreen by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Inicializar CallRepository al entrar al chat (idempotente)
    LaunchedEffect(Unit) {
        CallRepository.initialize(context)
    }
    
    // Estado para grabación de audio
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    
    // Estado para envío de ubicación
    var isGettingLocation by remember { mutableStateOf(false) }
    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }
    
    // Función para obtener y enviar ubicación
    @Suppress("MissingPermission")
    fun fetchAndSendLocation() {
        scope.launch {
            isGettingLocation = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val lng = location.longitude
                        scope.launch {
                            val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                            if (convId != null) {
                                currentConversationId = convId
                                val locationMessage = "[LOCATION]$lat,$lng"
                                ChatRepository.sendMessage(convId, locationMessage)
                            }
                            isGettingLocation = false
                        }
                    } else {
                        android.util.Log.e("ChatScreen", "No se pudo obtener ubicación")
                        isGettingLocation = false
                    }
                }.addOnFailureListener { e ->
                    android.util.Log.e("ChatScreen", "Error obteniendo ubicación: ${e.message}")
                    isGettingLocation = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Error obteniendo ubicación: ${e.message}")
                isGettingLocation = false
            }
        }
    }
    
    // Launcher para permiso de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchAndSendLocation()
        }
    }
    
    // Launcher para permiso de audio
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Iniciar grabación
            try {
                val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
                audioFile = file
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
                isRecording = true
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Error al iniciar grabación: ${e.message}")
            }
        }
    }
    
    // Launcher para seleccionar imagen/video de galería
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingMedia = true
                try {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        val mediaUrl = ChatRepository.uploadAndSendMedia(context, convId, selectedUri)
                        if (mediaUrl == null) {
                            android.util.Log.e("ChatScreen", "Error al subir media: ${ChatRepository.lastError.value}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error: ${e.message}")
                } finally {
                    isUploadingMedia = false
                }
            }
        }
    }
    
    // Estado para modal de compartir usuario
    var showShareUserModal by remember { mutableStateOf(false) }
    
    // Estado para modal de compartir artículo
    var showShareArticleModal by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar archivo
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingMedia = true
                try {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        // Obtener nombre y tamaño del archivo
                        val contentResolver = context.contentResolver
                        var fileName = "archivo"
                        var fileSize = 0L
                        contentResolver.query(selectedUri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                            if (cursor.moveToFirst()) {
                                if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: "archivo"
                                if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
                            }
                        }
                        val mediaUrl = ChatRepository.uploadAndSendMedia(context, convId, selectedUri)
                        if (mediaUrl != null) {
                            // Ya se envió como media, pero re-enviar como FILE con metadata
                            val fileMsg = "[FILE]{\"url\":\"$mediaUrl\",\"name\":\"$fileName\",\"size\":$fileSize}"
                            ChatRepository.sendMessage(convId, fileMsg)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error enviando archivo: ${e.message}")
                } finally {
                    isUploadingMedia = false
                }
            }
        }
    }
    
    // Verificar si el otro usuario está bloqueado
    LaunchedEffect(Unit) {
        isOtherUserBlocked = ChatRepository.isUserBlocked(otherUser.userId)
    }
    
    // Abrir chat: suscribir a realtime ANTES de cargar mensajes
    // Usar Unit como key para que SIEMPRE se ejecute al entrar a la pantalla
    LaunchedEffect(Unit) {
        try {
            // Buscar o crear conversación existente
            val existingConvId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
            if (existingConvId != null) {
                currentConversationId = existingConvId
                // Usar openChat que suscribe primero y luego carga
                val displayName = otherUser.nombreTienda ?: otherUser.nombre ?: otherUser.username
                ChatRepository.openChat(existingConvId, displayName)
                
                // Suscribirse a handshakes en tiempo real
                HandshakeRepository.subscribeToHandshakes(currentUserId ?: "")
                
                // Suscribirse a cambios de reputación en tiempo real
                ReputationRepository.subscribeToReputation(currentUserId ?: "", otherUser.userId)
                
                // Suscribirse a cambios de verificación del otro usuario en tiempo real
                VerificationRepository.subscribeToVerification(otherUser.userId)
                
                // Cargar handshake activo si existe
                HandshakeRepository.getActiveHandshakeForConversation(existingConvId)
                
            } else {
                android.util.Log.e("ChatScreen", "No se pudo obtener conversación: ${ChatRepository.lastError.value}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatScreen", "Exception al abrir chat: ${e.message}")
        }
    }
    
    // Detectar nuevas propuestas de handshake y mostrar modal
    LaunchedEffect(pendingProposals) {
        val proposalForThisConversation = pendingProposals.find { 
            it.conversationId == currentConversationId && it.receiverId == currentUserId
        }
        if (proposalForThisConversation != null && !showProposalModal) {
            currentProposal = proposalForThisConversation
            showProposalModal = true
        }
    }
    
    // *** UNIFIED REALTIME HANDLER: Actualizar el banner cuando activeHandshake cambia ***
    // Este es el ÚNICO LaunchedEffect que maneja todos los estados del handshake
    LaunchedEffect(activeHandshake) {
        val handshake = activeHandshake
        android.util.Log.d("ChatScreen", ">>> REALTIME: activeHandshake changed -> status=${handshake?.status} id=${handshake?.id}")
        
        // Si activeHandshake es null, ocultar banner (excepto si estamos en animación CANCELLED)
        if (handshake == null) {
            if (handshakeBannerState != HandshakeBannerState.CANCELLED) {
                showHandshakeBanner = false
                isWaitingForAcceptance = false
            }
            return@LaunchedEffect
        }
        
        // Solo procesar si es de esta conversación
        if (handshake.conversationId != currentConversationId) return@LaunchedEffect
        
        // Si estamos mostrando el banner CANCELLED, NO interferir con su animación
        if (handshakeBannerState == HandshakeBannerState.CANCELLED && handshake.status == "CANCELLED") {
            android.util.Log.d("ChatScreen", ">>> Ignorando update CANCELLED - banner ya en animación")
            return@LaunchedEffect
        }
        
        // Limpiar el handshake local ya que Realtime tiene el estado real
        if (localCreatedHandshake != null) {
            localCreatedHandshake = null
        }
        
        when (handshake.status) {
            "PROPOSED" -> {
                // Solo mostrar banner WAITING si soy el iniciador
                if (handshake.initiatorId == currentUserId) {
                    showHandshakeBanner = true
                    handshakeBannerState = HandshakeBannerState.WAITING
                    android.util.Log.d("ChatScreen", ">>> Banner WAITING mostrado para iniciador")
                }
            }
            "ACCEPTED", "IN_PROGRESS" -> {
                android.util.Log.d("ChatScreen", ">>> Handshake ${handshake.status}! Mostrando banner ACCEPTED")
                isWaitingForAcceptance = false
                showHandshakeModal = false
                showHandshakeBanner = true
                handshakeBannerState = HandshakeBannerState.ACCEPTED
            }
            "COMPLETED" -> {
                android.util.Log.d("ChatScreen", ">>> Handshake COMPLETED!")
                completedHandshakeInfo = handshake
                showHandshakeBanner = true
                handshakeBannerState = HandshakeBannerState.COMPLETED
                
                if (!completedBannerDismissed) {
                    // Enviar mensaje de transacción completada al chat
                    currentConversationId?.let { convId ->
                        val completedJson = org.json.JSONObject().apply {
                            put("type", "TRANSACTION_COMPLETED")
                            put("productDescription", handshake.productDescription)
                            put("agreedPrice", handshake.agreedPrice)
                        }
                        ChatRepository.sendMessage(convId, "[HANDSHAKE_STATUS]$completedJson")
                    }
                    
                    // Incrementar reputación
                    val change = ReputationRepository.incrementReputation()
                    lastReputationChange = change
                    android.util.Log.d("ChatScreen", ">>> Reputación incrementada: +$change%")
                    
                    delay(100)
                    delay(3500)
                    
                    // Cerrar banner automáticamente
                    showHandshakeBanner = false
                    completedBannerDismissed = true
                    lastReputationChange = 0
                    completedHandshakeInfo = null
                    android.util.Log.d("ChatScreen", ">>> Banner COMPLETED cerrado")
                }
            }
            "REJECTED", "CANCELLED" -> {
                // Solo ocultar si NO estamos mostrando el banner CANCELLED con animación
                if (handshakeBannerState != HandshakeBannerState.CANCELLED) {
                    android.util.Log.d("ChatScreen", ">>> Handshake rechazado/cancelado - ocultando banner")
                    isWaitingForAcceptance = false
                    pendingHandshakeId = null
                    showHandshakeBanner = false
                }
            }
        }
    }
    
    // *** POLLING FALLBACK: Verificar estado del handshake periódicamente ***
    // Si Realtime falla, esto detecta cambios cada 3 segundos
    LaunchedEffect(currentConversationId, showHandshakeBanner, activeHandshake) {
        val convId = currentConversationId ?: return@LaunchedEffect
        // Solo hacer polling si hay un handshake activo/pendiente
        if (activeHandshake != null || showHandshakeBanner || isWaitingForAcceptance) {
            while (true) {
                delay(3000) // Poll cada 3 segundos
                try {
                    val changed = HandshakeRepository.refreshActiveHandshake(convId)
                    if (changed) {
                        android.util.Log.d("ChatScreen", ">>> POLL: Handshake state changed via polling!")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", ">>> POLL error: ${e.message}")
                }
            }
        }
    }
    
    // Scroll INSTANTÁNEO al fondo cuando se cargan los mensajes iniciales
    // Usar snapshotFlow para evitar parpadeos y re-layouts
    LaunchedEffect(messages.size, initialScrollDone) {
        if (messages.isNotEmpty() && !initialScrollDone) {
            // Primera carga: scroll instantáneo al fondo SIN delay
            listState.scrollToItem(messages.size - 1)
            // Marcar como listo DESPUÉS del scroll
            initialScrollDone = true
            messagesReady = true
        }
    }
    
    // Scroll animado para mensajes nuevos (separado para evitar conflictos)
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && initialScrollDone && messagesReady) {
            // Mensajes nuevos (realtime): scroll animado solo si estamos cerca del fondo
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val isNearBottom = lastVisibleIndex >= messages.size - 3
            if (isNearBottom) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    // Detectar scroll hacia arriba para cargar más mensajes
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    
    // Guardar el conteo previo para detectar cuando se agregan mensajes antiguos
    var previousMessageCount by remember { mutableStateOf(0) }
    var wasLoadingMore by remember { mutableStateOf(false) }
    
    // Trigger de carga cuando llegamos arriba
    LaunchedEffect(firstVisibleItemIndex) {
        // Cargar más cuando estamos en los primeros 2 items y hay más por cargar
        // El +1 es por el item del spinner de carga
        if (firstVisibleItemIndex <= 2 && hasMoreMessages && !isLoadingMoreFromRepo && messagesReady && messages.isNotEmpty()) {
            previousMessageCount = messages.size
            wasLoadingMore = true
            ChatRepository.loadMoreMessages()
        }
    }
    
    // Mantener posición del scroll cuando se cargan mensajes antiguos (prepend)
    LaunchedEffect(messages.size, wasLoadingMore) {
        if (wasLoadingMore && messages.size > previousMessageCount && previousMessageCount > 0) {
            val addedCount = messages.size - previousMessageCount
            // Ajustar scroll para mantener la vista en el mismo mensaje
            val targetIndex = firstVisibleItemIndex + addedCount
            listState.scrollToItem(targetIndex)
            wasLoadingMore = false
        }
    }
    
    // Notificar cuando estoy escribiendo
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty() && currentConversationId != null) {
            ChatRepository.setTyping(currentConversationId!!, true)
            delay(2000)
            ChatRepository.setTyping(currentConversationId!!, false)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    // Solo aplicar imePadding cuando el modal de handshake NO está abierto
                    // para que el footer suba con el teclado solo al escribir mensajes
                    if (!showHandshakeModal) Modifier.imePadding() else Modifier
                )
        ) {
            // Header del chat
            if (isSearchMode) {
                ChatSearchHeader(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    resultCount = searchResults.size,
                    currentIndex = currentSearchIndex,
                    isSearching = isSearching,
                    onClose = {
                        isSearchMode = false
                        searchQuery = ""
                        searchResults = emptyList()
                        currentSearchIndex = -1
                    },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            scope.launch {
                                isSearching = true
                                searchResults = ChatRepository.searchMessages(
                                    currentConversationId ?: "", searchQuery
                                )
                                currentSearchIndex = if (searchResults.isNotEmpty()) 0 else -1
                                isSearching = false
                                // Scroll to first result
                                if (searchResults.isNotEmpty()) {
                                    val msgId = searchResults[0].id
                                    val idx = ChatRepository.loadMessagesUntilFound(msgId)
                                    if (idx >= 0) listState.scrollToItem(idx)
                                }
                            }
                        }
                    },
                    onPrevious = {
                        if (searchResults.isNotEmpty() && currentSearchIndex < searchResults.size - 1) {
                            currentSearchIndex++
                            scope.launch {
                                val msgId = searchResults[currentSearchIndex].id
                                val idx = ChatRepository.loadMessagesUntilFound(msgId)
                                if (idx >= 0) listState.animateScrollToItem(idx)
                            }
                        }
                    },
                    onNext = {
                        if (searchResults.isNotEmpty() && currentSearchIndex > 0) {
                            currentSearchIndex--
                            scope.launch {
                                val msgId = searchResults[currentSearchIndex].id
                                val idx = ChatRepository.loadMessagesUntilFound(msgId)
                                if (idx >= 0) listState.animateScrollToItem(idx)
                            }
                        }
                    }
                )
            } else {
                ChatHeader(
                    user = otherUser,
                    isOnline = isOtherUserOnline,
                    isTyping = isOtherUserTyping,
                    otherUserReputation = otherUserReputation,
                    isVerified = isOtherUserVerified || otherUser.isVerified,
                    onBack = onOpenChatList,
                    onCall = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            scope.launch {
                                val avatarUrl = otherUser.avatarUrl ?: ""
                                val success = CallRepository.startCall(
                                    calleeId = otherUser.userId,
                                    calleeUsername = otherUser.username,
                                    calleeAvatarUrl = avatarUrl
                                )
                                if (success) showCallScreen = true
                            }
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onMore = { showChatSettingsModal = true },
                    onNavigateToProfile = { onNavigateToUserProfile?.invoke(otherUser.userId) }
                )
            }
            
            // Banner dinámico de handshake - maneja todos los estados: WAITING, ACCEPTED, COMPLETED
            // Excluir COMPLETED si el usuario ya cerró el banner
            val shouldShowCompletedBanner = activeHandshake?.status == "COMPLETED" && !completedBannerDismissed
            // Usar activeHandshake si existe, sino usar el handshake creado localmente o el cancelado
            val effectiveHandshake = activeHandshake ?: localCreatedHandshake ?: cancelledHandshakeInfo ?: completedHandshakeInfo
            
            // Animación fluida de entrada/salida del banner
            androidx.compose.animation.AnimatedVisibility(
                visible = showHandshakeBanner || (activeHandshake?.conversationId == currentConversationId && 
                    (activeHandshake?.status in listOf("PROPOSED", "ACCEPTED", "IN_PROGRESS") || shouldShowCompletedBanner)),
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                        androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
                exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) +
                       androidx.compose.animation.slideOutVertically(targetOffsetY = { -it })
            ) {
                HandshakeActiveBanner(
                    handshake = effectiveHandshake,
                    currentUserId = currentUserId ?: "",
                    otherUserName = otherUser.username,
                    bannerState = handshakeBannerState,
                    currentUserReputation = currentUserReputation,
                    reputationChange = lastReputationChange,
                    onConfirm = {
                        scope.launch {
                            activeHandshake?.id?.let { id ->
                                HandshakeRepository.confirmTransaction(id, currentUserId ?: "")
                            }
                        }
                    },
                    onCancel = {
                        // Usar el estado real del handshake para decidir qué hacer
                        val isInWaitingState = activeHandshake?.status == "PROPOSED" || 
                                               handshakeBannerState == HandshakeBannerState.WAITING
                        
                        android.util.Log.d("ChatScreen", ">>> onCancel CALLED")
                        android.util.Log.d("ChatScreen", ">>> activeHandshake?.status = ${activeHandshake?.status}")
                        android.util.Log.d("ChatScreen", ">>> handshakeBannerState = $handshakeBannerState")
                        android.util.Log.d("ChatScreen", ">>> isInWaitingState = $isInWaitingState")
                        android.util.Log.d("ChatScreen", ">>> activeHandshake?.id = ${activeHandshake?.id}")
                        android.util.Log.d("ChatScreen", ">>> pendingHandshakeId = $pendingHandshakeId")
                        
                        if (isInWaitingState) {
                            // Cancelar propuesta pendiente - penalización -2%
                            val idToCancel = activeHandshake?.id ?: pendingHandshakeId
                            android.util.Log.d("ChatScreen", ">>> WAITING state - idToCancel = $idToCancel")
                            
                            // IMPORTANTE: Guardar referencia al handshake ANTES de cancelar
                            // para que el banner pueda mostrarlo durante la animación
                            cancelledHandshakeInfo = activeHandshake ?: localCreatedHandshake
                            android.util.Log.d("ChatScreen", ">>> Guardando cancelledHandshakeInfo: ${cancelledHandshakeInfo?.id}")
                            
                            // Establecer estado del banner ANTES de la coroutine
                            lastReputationChange = -2
                            handshakeBannerState = HandshakeBannerState.CANCELLED
                            showHandshakeBanner = true
                            android.util.Log.d("ChatScreen", ">>> Showing CANCELLED banner with -2%")
                            
                            // Limpiar estados locales inmediatamente
                            isWaitingForAcceptance = false
                            pendingHandshakeId = null
                            localCreatedHandshake = null
                            
                            scope.launch {
                                idToCancel?.let { id ->
                                    android.util.Log.d("ChatScreen", ">>> Calling cancelHandshake($id)")
                                    val success = HandshakeRepository.cancelHandshake(id)
                                    android.util.Log.d("ChatScreen", ">>> cancelHandshake result = $success")
                                    if (success) {
                                        currentConversationId?.let { convId ->
                                            ChatRepository.sendMessage(convId, "[HANDSHAKE_STATUS]{\"type\":\"AGREEMENT_CANCELLED\",\"message\":\"Acuerdo cancelado\"}")
                                        }
                                        // Penalización -2% por cancelar en estado WAITING
                                        android.util.Log.d("ChatScreen", ">>> Aplicando penalización -2% por cancelar en WAITING")
                                        ReputationRepository.decrementReputation(2)
                                        
                                        // Esperar a que se vea la animación y luego cerrar banner
                                        delay(2500)
                                        showHandshakeBanner = false
                                        lastReputationChange = 0
                                        cancelledHandshakeInfo = null
                                        android.util.Log.d("ChatScreen", ">>> Banner CANCELLED cerrado")
                                    }
                                }
                            }
                        } else {
                            // Mostrar modal de confirmación para cancelar transacción aceptada
                            android.util.Log.d("ChatScreen", ">>> ACCEPTED state - showing confirmation modal")
                            showCancelConfirmModal = true
                        }
                    },
                    onDismiss = {
                        // Cerrar banner manualmente (el usuario hizo clic en X)
                        // La animación de reputación ya se maneja en el LaunchedEffect de COMPLETED
                        completedBannerDismissed = true
                        showHandshakeBanner = false
                    }
                )
            }
        
        // Contenedor de mensajes - Oculto hasta que estén listos y posicionados
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .alpha(if (messagesReady) 1f else 0f), // Invisible hasta scroll inicial
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            reverseLayout = false
        ) {
            // Spinner de carga de mensajes antiguos (arriba)
            if (hasMoreMessages && messagesReady) {
                item(key = "loading_more_indicator") {
                    if (isLoadingMoreFromRepo) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryPurple,
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        // Espaciador invisible para trigger de carga
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }
            
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                // Detectar si es un mensaje especial de solicitud de cliente
                val isClientRequest = ChatRepository.isClientRequestMessage(message.content) ||
                    ChatRepository.isClientAcceptedMessage(message.content) ||
                    ChatRepository.isClientRejectedMessage(message.content)
                
                if (isClientRequest) {
                    ClientRequestMessageBubble(
                        content = message.content,
                        isFromMe = message.isFromMe,
                        senderId = message.senderId,
                        senderUsername = otherUser.username ?: "",
                        onAccept = {
                            scope.launch {
                                ChatRepository.acceptClientFromChat(
                                    requesterId = message.senderId,
                                    requesterUsername = otherUser.username ?: ""
                                )
                            }
                        },
                        onReject = { reason ->
                            scope.launch {
                                ChatRepository.rejectClientFromChat(
                                    requesterId = message.senderId,
                                    requesterUsername = otherUser.username ?: "",
                                    reason = reason
                                )
                            }
                        },
                        onViewHistory = {
                            // TODO: Navegar al historial del usuario
                        }
                    )
                } else {
                    MessageBubble(
                        message = message,
                        otherUserAvatar = otherUser.avatarUrl,
                        currentUserId = currentUserId,
                        onLongPress = { msg ->
                            selectedMessage = msg
                            showMessageOptionsModal = true
                        },
                        onForwardSharedPost = { postData ->
                            sharedPostToForward = postData
                            showForwardSharedPostModal = true
                        },
                        onSharedPostClick = { postId ->
                            onOpenProduct?.invoke(postId)
                        },
                        onNavigateToUserProfile = onNavigateToUserProfile
                    )
                }
            }
            
            // Mostrar burbuja de "Escribiendo..." cuando el otro usuario está escribiendo
            if (isOtherUserTyping) {
                item {
                    TypingIndicatorBubble(
                        userAvatar = otherUser.avatarUrl
                    )
                }
            }
        }
        
        // ═══ Banner de usuario bloqueado O input normal ═══
        if (isOtherUserBlocked) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = null,
                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Bloqueaste a este usuario",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444).copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Desbloquear",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                scope.launch {
                                    val success = ChatRepository.unblockUser(otherUser.userId)
                                    if (success) isOtherUserBlocked = false
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
        // Indicador de modo edición
        AnimatedVisibility(
            visible = isEditMode && editingMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryPurple.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Editando mensaje",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Text(
                            text = editingMessage?.content?.take(50) ?: "",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = {
                            isEditMode = false
                            editingMessage = null
                            messageText = ""
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar edición",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        // @ Mention Popup for chat
        val chatMentionQuery = remember(messageText) { extractMentionQuery(messageText) }
        
        MentionSuggestionPopup(
            isVisible = chatMentionQuery != null,
            query = chatMentionQuery ?: "",
            onUserSelected = { username ->
                messageText = insertMention(messageText, username)
            },
            onDismiss = { /* hides when query becomes null */ }
        )
        
        // Input mejorado estilo WhatsApp
        ChatInputV2(
            text = messageText,
            onTextChange = { messageText = it },
            onSend = {
                if (messageText.isNotBlank()) {
                    val textToSend = messageText
                    messageText = ""
                    
                    // Modo edición - actualizar mensaje existente
                    if (isEditMode && editingMessage != null) {
                        val msgToEdit = editingMessage!!
                        isEditMode = false
                        editingMessage = null
                        
                        scope.launch {
                            try {
                                ChatRepository.updateMessage(msgToEdit.id, textToSend)
                            } catch (e: Exception) {
                                android.util.Log.e("ChatScreen", "Error editando: ${e.message}")
                            }
                        }
                        return@ChatInputV2
                    }
                    
                    // Modo normal - enviar nuevo mensaje
                    scope.launch {
                        try {
                            val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                            if (convId == null) {
                                android.util.Log.e("ChatScreen", "No se pudo obtener/crear conversación: ${otherUser.userId}")
                                return@launch
                            }
                            
                            if (currentConversationId == null) {
                                currentConversationId = convId
                            }
                            
                            ChatRepository.sendMessage(convId, textToSend)
                        } catch (e: Exception) {
                            android.util.Log.e("ChatScreen", "Exception enviando mensaje: ${e.message}")
                        }
                    }
                }
            },
            onAttachmentClick = { showAttachmentMenu = !showAttachmentMenu },
            onCameraClick = { mediaPickerLauncher.launch("image/*") },
            onHandshakeClick = { showHandshakeModal = true },
            onVoiceRecord = {
                if (isRecording) {
                    // Detener grabación y enviar
                    try {
                        mediaRecorder?.apply {
                            stop()
                            release()
                        }
                        mediaRecorder = null
                        isRecording = false
                        
                        // Enviar audio
                        audioFile?.let { file ->
                            scope.launch {
                                isUploadingMedia = true
                                try {
                                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                                    if (convId != null) {
                                        currentConversationId = convId
                                        val audioUri = Uri.fromFile(file)
                                        ChatRepository.uploadAndSendAudio(context, convId, audioUri)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ChatScreen", "Error enviando audio: ${e.message}")
                                } finally {
                                    isUploadingMedia = false
                                    audioFile = null
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error al detener grabación: ${e.message}")
                        isRecording = false
                    }
                } else {
                    // Verificar permiso e iniciar grabación
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
                            audioFile = file
                            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                MediaRecorder(context)
                            } else {
                                @Suppress("DEPRECATION")
                                MediaRecorder()
                            }
                            recorder.apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                setOutputFile(file.absolutePath)
                                prepare()
                                start()
                            }
                            mediaRecorder = recorder
                            isRecording = true
                        } catch (e: Exception) {
                            android.util.Log.e("ChatScreen", "Error al iniciar grabación: ${e.message}")
                        }
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            onAudioRecorded = { file ->
                scope.launch {
                    isUploadingMedia = true
                    try {
                        val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                        if (convId != null) {
                            currentConversationId = convId
                            val audioUri = Uri.fromFile(file)
                            ChatRepository.uploadAndSendAudio(context, convId, audioUri)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error enviando audio: ${e.message}")
                    } finally {
                        isUploadingMedia = false
                    }
                }
            },
            isUploading = isUploadingMedia,
            isRecording = isRecording
        )
        } // end else (not blocked)
        } // end Column
        
        // Menú de adjuntos como OVERLAY - encima del footer (no sobre él)
        AnimatedVisibility(
            visible = showAttachmentMenu,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 62.dp) // Cerca del footer con separación de ~5dp
        ) {
            AttachmentMenu(
                onDismiss = { showAttachmentMenu = false },
                onSelectImage = { 
                    showAttachmentMenu = false
                    mediaPickerLauncher.launch("image/*")
                },
                onSelectFile = { 
                    showAttachmentMenu = false
                    filePickerLauncher.launch("*/*")
                },
                onSelectLocation = { 
                    showAttachmentMenu = false
                    // Verificar permisos de ubicación
                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        fetchAndSendLocation()
                    } else {
                        locationPermissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                },
                onSelectContact = { 
                    showAttachmentMenu = false
                    showShareUserModal = true
                },
                onSelectArticle = {
                    showAttachmentMenu = false
                    showShareArticleModal = true
                }
            )
        }
    }
    
    
    // Modal de opciones de mensaje (al mantener pulsado)
    if (showMessageOptionsModal && selectedMessage != null) {
        MessageOptionsModal(
            message = selectedMessage!!,
            onDismiss = { 
                showMessageOptionsModal = false
                selectedMessage = null
            },
            onReaction = { emoji ->
                selectedMessage?.let { msg ->
                    scope.launch {
                        ChatRepository.toggleReaction(msg.id, emoji)
                    }
                }
                showMessageOptionsModal = false
                selectedMessage = null
            },
            onEdit = {
                // Activar modo edición
                selectedMessage?.let { msg ->
                    if (msg.isFromMe) {
                        editingMessage = msg
                        isEditMode = true
                        messageText = msg.content
                    }
                }
                showMessageOptionsModal = false
                selectedMessage = null
            },
            onForward = {
                // TODO: Implementar reenvío
                showMessageOptionsModal = false
            },
            onCopy = {
                // Copiar al portapapeles
                selectedMessage?.content?.let { content ->
                    // El clipboard se implementa con ClipboardManager
                }
                showMessageOptionsModal = false
                selectedMessage = null
            },
            onDelete = {
                selectedMessage?.let { msg ->
                    // Eliminación optimista: quitar del chat inmediatamente
                    scope.launch {
                        ChatRepository.deleteMessage(msg.id)
                    }
                }
                showMessageOptionsModal = false
                selectedMessage = null
            }
        )
    }
    
    // Modal para reenviar posts compartidos - siempre montado para animación fluida
    ForwardSharedPostModal(
        isVisible = showForwardSharedPostModal && sharedPostToForward != null,
        postData = sharedPostToForward ?: SharedPostData("", emptyList(), "", 0.0, "", "", ""),
        onDismiss = {
            showForwardSharedPostModal = false
            sharedPostToForward = null
        },
        onForward = { targetUserId, customMessage ->
            scope.launch {
                val convId = ChatRepository.getOrCreateConversation(targetUserId)
                if (convId != null && sharedPostToForward != null) {
                    // Recrear el mensaje con formato [SHARED_POST]
                    val postData = sharedPostToForward!!
                    val imagesArray = org.json.JSONArray(postData.images)
                    val sharedPostJson = org.json.JSONObject().apply {
                        put("postId", postData.postId)
                        put("images", imagesArray)
                        put("title", postData.title)
                        put("price", postData.price)
                        put("ownerUsername", postData.ownerUsername)
                        put("ownerAvatar", postData.ownerAvatar)
                        put("customMessage", customMessage.trim())
                    }
                    ChatRepository.sendMessage(convId, "[SHARED_POST]$sharedPostJson")
                }
                showForwardSharedPostModal = false
                sharedPostToForward = null
            }
        }
    )
    
    // Modal de ajustes del chat (3 puntitos del header)
    ChatSettingsModal(
        isVisible = showChatSettingsModal,
        otherUser = otherUser,
        conversationId = currentConversationId ?: "",
        onDismiss = { showChatSettingsModal = false },
        onBack = onBack,
        onBlockStateChanged = { blocked -> isOtherUserBlocked = blocked },
        onReportSent = {
            showChatSettingsModal = false
            onBack()
        },
        onChatCleared = {
            completedHandshakeInfo = null
            cancelledHandshakeInfo = null
            localCreatedHandshake = null
            showHandshakeBanner = false
            completedBannerDismissed = true
        },
        onSearchInChat = {
            showChatSettingsModal = false
            isSearchMode = true
        },
        onScrollToMessage = { messageId ->
            showChatSettingsModal = false
            scope.launch {
                val index = ChatRepository.loadMessagesUntilFound(messageId)
                if (index >= 0) {
                    listState.scrollToItem(index)
                }
            }
        }
    )
    
    // Modal de Handshake para INICIAR propuesta de compra/venta (slide-up desde abajo)
    HandshakeConfirmationModal(
        isVisible = showHandshakeModal,
        otherUser = otherUser,
        onDismiss = { showHandshakeModal = false },
        onConfirm = { productDescription, agreedPrice ->
            scope.launch {
                try {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        
                        // Crear handshake en Supabase
                        val handshake = HandshakeRepository.createHandshake(
                            conversationId = convId,
                            initiatorId = currentUserId ?: "",
                            receiverId = otherUser.userId,
                            productDescription = productDescription,
                            agreedPrice = agreedPrice
                        )
                        
                        if (handshake != null) {
                            pendingHandshakeId = handshake.id
                            localCreatedHandshake = handshake // Guardar para el banner
                            isWaitingForAcceptance = true
                            showHandshakeModal = false
                            completedBannerDismissed = false // Reset para nuevo handshake
                            android.util.Log.d("ChatScreen", "Handshake creado: ${handshake.id}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error iniciando handshake: ${e.message}")
                }
            }
        }
    )
    
    // Modal para compartir usuario en el chat
    ShareUserModal(
        isVisible = showShareUserModal,
        currentUserId = currentUserId ?: "",
        onDismiss = { showShareUserModal = false },
        onShareUser = { sharedUser ->
            showShareUserModal = false
            scope.launch {
                try {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        // Fetch followers & posts count from Supabase
                        var followersCount = 0
                        var postsCount = 0
                        try {
                            followersCount = com.rendly.app.data.remote.SupabaseClient.database
                                .from("followers")
                                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("id")) {
                                    filter { eq("followed_id", sharedUser.userId) }
                                }.decodeList<kotlinx.serialization.json.JsonObject>().size
                        } catch (_: Exception) {}
                        try {
                            postsCount = com.rendly.app.data.remote.SupabaseClient.database
                                .from("posts")
                                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("id")) {
                                    filter { eq("user_id", sharedUser.userId); eq("status", "active") }
                                }.decodeList<kotlinx.serialization.json.JsonObject>().size
                        } catch (_: Exception) {}
                        val userJson = org.json.JSONObject().apply {
                            put("userId", sharedUser.userId)
                            put("username", sharedUser.username)
                            put("avatarUrl", sharedUser.avatarUrl ?: "")
                            put("storeName", sharedUser.nombreTienda ?: "")
                            put("reputation", sharedUser.reputationScore ?: 0.0)
                            put("isVerified", sharedUser.isVerified)
                            put("bannerUrl", sharedUser.bannerUrl ?: "")
                            put("followers", followersCount)
                            put("clients", 0)
                            put("postsCount", postsCount)
                        }
                        ChatRepository.sendMessage(convId, "[SHARED_USER]$userJson")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error compartiendo usuario: ${e.message}")
                }
            }
        }
    )
    
    // Modal para compartir artículo en el chat
    ShareArticleModal(
        isVisible = showShareArticleModal,
        currentUserId = currentUserId ?: "",
        otherUserId = otherUser.userId,
        otherUsername = otherUser.username,
        onDismiss = { showShareArticleModal = false },
        onSelectArticle = { selectedPost ->
            showShareArticleModal = false
            scope.launch {
                try {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        val articleJson = org.json.JSONObject().apply {
                            put("postId", selectedPost.id)
                        }
                        ChatRepository.sendMessage(convId, "[ARTICLE_CARD]$articleJson")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error compartiendo artículo: ${e.message}")
                }
            }
        }
    )
    
    // ═══ SISTEMA DE LLAMADAS ═══
    // Abrir pantalla de llamada cuando el estado cambia
    LaunchedEffect(callState.status) {
        when (callState.status) {
            CallStatus.OUTGOING, CallStatus.CONNECTED, CallStatus.RECONNECTING -> showCallScreen = true
            CallStatus.ENDED -> {
                // Mantener visible brevemente para mostrar "Llamada finalizada"
                kotlinx.coroutines.delay(1500)
                showCallScreen = false
            }
            CallStatus.IDLE -> showCallScreen = false
            else -> {}
        }
    }
    
    // Pantalla de llamada saliente / en curso (full-screen overlay)
    if (showCallScreen && callState.status != CallStatus.IDLE && callState.status != CallStatus.INCOMING) {
        CallScreen(
            callState = callState,
            onEndCall = {
                scope.launch {
                    CallRepository.endCall()
                    showCallScreen = false
                }
            },
            onToggleMute = { CallRepository.toggleMute() },
            onToggleSpeaker = { CallRepository.toggleSpeaker() }
        )
    }
    
    // Overlay de llamada entrante (full-screen)
    if (incomingCall != null && callState.status == CallStatus.INCOMING) {
        IncomingCallOverlay(
            callState = incomingCall!!,
            onAnswer = {
                scope.launch {
                    CallRepository.answerCall(incomingCall!!.callId!!)
                    showCallScreen = true
                }
            },
            onReject = {
                scope.launch {
                    CallRepository.rejectCall(incomingCall!!.callId!!)
                }
            }
        )
    }
    
    // Mostrar banner de ESPERA cuando se inicia handshake (reemplaza el modal central)
    LaunchedEffect(isWaitingForAcceptance, pendingHandshakeId) {
        if (isWaitingForAcceptance && pendingHandshakeId != null) {
            showHandshakeBanner = true
            handshakeBannerState = HandshakeBannerState.WAITING
        }
    }
    
    // Modal de PROPUESTA para el RECEPTOR (aparece en tiempo real)
    if (showProposalModal && currentProposal != null) {
        HandshakeProposalModal(
            handshake = currentProposal!!,
            initiatorUser = otherUser,
            onAccept = {
                scope.launch {
                    HandshakeRepository.acceptHandshake(currentProposal!!.id!!)
                    showProposalModal = false
                    currentProposal = null
                }
            },
            onReject = {
                scope.launch {
                    val success = HandshakeRepository.rejectHandshake(currentProposal!!.id!!)
                    if (success) {
                        currentConversationId?.let { convId ->
                            ChatRepository.sendMessage(convId, "❌ Propuesta rechazada")
                        }
                    }
                    showProposalModal = false
                    currentProposal = null
                }
            },
            onDismiss = {
                showProposalModal = false
                currentProposal = null
            }
        )
    }
    
    // Modal de confirmación al cancelar - guardar el ID antes de que activeHandshake sea null
    val handshakeToCancel = remember(showCancelConfirmModal) { 
        if (showCancelConfirmModal) activeHandshake else null 
    }
    
    if (showCancelConfirmModal && handshakeToCancel != null) {
        CancelConfirmationModal(
            productDescription = handshakeToCancel.productDescription,
            onConfirmCancel = {
                // IMPORTANTE: Guardar referencia al handshake ANTES de cerrar el modal
                // para que el banner pueda mostrarlo durante la animación
                cancelledHandshakeInfo = handshakeToCancel
                android.util.Log.d("ChatScreen", ">>> ACCEPTED cancel - Guardando cancelledHandshakeInfo: ${handshakeToCancel.id}")
                
                // Establecer estado del banner ANTES de la coroutine
                lastReputationChange = -4
                handshakeBannerState = HandshakeBannerState.CANCELLED
                showHandshakeBanner = true
                android.util.Log.d("ChatScreen", ">>> Showing CANCELLED banner with -4%")
                
                // Limpiar estados locales
                isWaitingForAcceptance = false
                pendingHandshakeId = null
                localCreatedHandshake = null
                
                // Cerrar modal primero
                showCancelConfirmModal = false
                
                scope.launch {
                    HandshakeRepository.cancelHandshake(handshakeToCancel.id!!)
                    currentConversationId?.let { convId ->
                        ChatRepository.sendMessage(convId, "[HANDSHAKE_STATUS]{\"type\":\"TRANSACTION_CANCELLED\",\"message\":\"Transacción cancelada\"}")
                    }
                    
                    // Penalización -4% por cancelar después de aceptar
                    android.util.Log.d("ChatScreen", ">>> Aplicando penalización -4% por cancelar después de aceptar")
                    ReputationRepository.decrementReputation(4)
                    
                    // Esperar a que se vea la animación y luego cerrar banner
                    delay(2500)
                    showHandshakeBanner = false
                    lastReputationChange = 0
                    cancelledHandshakeInfo = null
                    android.util.Log.d("ChatScreen", ">>> Banner CANCELLED (ACCEPTED) cerrado")
                }
            },
            onDismiss = { showCancelConfirmModal = false }
        )
    }
    
}

@Composable
private fun ChatHeader(
    user: Usuario,
    isOnline: Boolean,
    isTyping: Boolean,
    otherUserReputation: Double = 50.0, // Reputación del otro usuario (0-100) - ya no se muestra
    isVerified: Boolean = false, // Estado de verificación en tiempo real
    onBack: () -> Unit,
    onCall: () -> Unit,
    onMore: () -> Unit,
    onNavigateToProfile: () -> Unit = {} // Navegar al perfil del usuario
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón volver
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            
            // Avatar clickable para ir al perfil
            AsyncImage(
                model = user.avatarUrl ?: "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff",
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToProfile() }
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Info del usuario - clickable para ir al perfil
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToProfile() }
            ) {
                // Primera línea: nombre + verificado (pegados)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = user.nombreTienda ?: user.nombre ?: user.username,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Icono de verificado junto al nombre
                    if (isVerified) {
                        VerifiedBadge(size = 14.dp)
                    }
                }
                // Segunda línea: estado (en línea, escribiendo, etc.)
                Text(
                    text = when {
                        isTyping -> "escribiendo..."
                        isOnline -> "en línea"
                        else -> "últ. vez recientemente"
                    },
                    color = when {
                        isTyping -> Color(0xFF64B5F6)
                        isOnline -> Color(0xFF4CAF50)
                        else -> TextMuted
                    },
                    fontSize = 12.sp
                )
            }
            
            // Botón de llamada
            IconButton(onClick = onCall) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Llamar",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // Botón más opciones
            IconButton(onClick = onMore) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    resultCount: Int,
    currentIndex: Int,
    isSearching: Boolean,
    onClose: () -> Unit,
    onSearch: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón volver (cierra búsqueda)
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Cerrar búsqueda",
                    tint = TextPrimary
                )
            }
            
            // Campo de búsqueda
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text("Buscar mensajes...", color = TextMuted, fontSize = 14.sp)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            singleLine = true,
                            cursorBrush = SolidColor(PrimaryPurple),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onSearch() })
                        )
                    }
                    
                    // Indicador de resultados
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrimaryPurple,
                            strokeWidth = 2.dp
                        )
                    } else if (resultCount > 0) {
                        Text(
                            "${currentIndex + 1}/$resultCount",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Flechas de navegación
            IconButton(
                onClick = onPrevious,
                enabled = resultCount > 0 && currentIndex < resultCount - 1
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Anterior",
                    tint = if (resultCount > 0 && currentIndex < resultCount - 1) TextPrimary else TextMuted.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
            
            IconButton(
                onClick = onNext,
                enabled = resultCount > 0 && currentIndex > 0
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Siguiente",
                    tint = if (resultCount > 0 && currentIndex > 0) TextPrimary else TextMuted.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: Message,
    otherUserAvatar: String?,
    currentUserId: String? = null,
    onLongPress: (Message) -> Unit = {},
    onForwardSharedPost: ((SharedPostData) -> Unit)? = null,
    onSharedPostClick: ((String) -> Unit)? = null,
    onNavigateToUserProfile: ((String) -> Unit)? = null
) {
    // Paleta premium: grises elegantes
    val myMessageBg = Color(0xFF2D3748) // Gris oscuro elegante
    val otherMessageBg = Color(0xFF1E2732)
    val myMessageText = Color.White
    val otherMessageText = Color(0xFFE7E9EA)
    val timeColor = Color.White.copy(alpha = 0.6f)
    val otherTimeColor = Color(0xFF8B98A5)
    
    // Determinar si yo reaccioné a este mensaje
    val iReacted = currentUserId != null && message.reactions.any { (_, users) -> 
        currentUserId in users 
    }
    // Posición de reacciones: derecha si es MI mensaje y YO reaccioné, sino izquierda
    val reactionsOnRight = message.isFromMe && iReacted
    
    // Separación animada cuando hay reacciones
    val hasReactions = message.reactions.isNotEmpty()
    val bottomPadding by animateDpAsState(
        targetValue = if (hasReactions) 16.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "reactionPadding"
    )
    
    // Detectar tipo de mensaje
    val isImage = message.content.startsWith("[IMG]")
    val isVideo = message.content.startsWith("[VIDEO]")
    val isAudio = message.content.startsWith("[AUDIO]")
    val isSharedPost = message.content.startsWith("[SHARED_POST]")
    val isConsultPost = message.content.startsWith("[CONSULT_POST]")
    val isLocation = message.content.startsWith("[LOCATION]")
    val isHandshakeStatus = message.content.startsWith("[HANDSHAKE_STATUS]")
    val isSharedUser = message.content.startsWith("[SHARED_USER]")
    val isFile = message.content.startsWith("[FILE]")
    val isArticleCard = message.content.startsWith("[ARTICLE_CARD]")
    val isHandshakeInitiated = message.content.startsWith("[HANDSHAKE_INITIATED]") || message.content.startsWith("[HANDSHAKE]")
    val mediaUrl = when {
        isImage -> message.content.removePrefix("[IMG]")
        isVideo -> message.content.removePrefix("[VIDEO]")
        isAudio -> message.content.removePrefix("[AUDIO]")
        else -> null
    }
    
    // Parsear ubicación
    val locationData = if (isLocation) {
        try {
            val coords = message.content.removePrefix("[LOCATION]").split(",")
            if (coords.size == 2) {
                Pair(coords[0].toDouble(), coords[1].toDouble())
            } else null
        } catch (e: Exception) { null }
    } else null
    
    // Parsear datos del post compartido - usar remember para evitar re-parseos
    val sharedPostData = remember(message.content) {
        if (isSharedPost) {
            try {
                val jsonStr = message.content.removePrefix("[SHARED_POST]")
                val json = org.json.JSONObject(jsonStr)
                // Soportar ambos formatos: images array o imageUrl string
                val imagesList = mutableListOf<String>()
                if (json.has("images")) {
                    val imagesArray = json.optJSONArray("images")
                    if (imagesArray != null) {
                        for (i in 0 until imagesArray.length()) {
                            imagesArray.optString(i)?.let { if (it.isNotEmpty()) imagesList.add(it) }
                        }
                    }
                } else if (json.has("imageUrl")) {
                    json.optString("imageUrl", "").let { if (it.isNotEmpty()) imagesList.add(it) }
                }
                SharedPostData(
                    postId = json.optString("postId", ""),
                    images = imagesList,
                    title = json.optString("title", ""),
                    price = json.optDouble("price", 0.0),
                    ownerUsername = json.optString("ownerUsername", ""),
                    ownerAvatar = json.optString("ownerAvatar", ""),
                    customMessage = json.optString("customMessage", "")
                )
            } catch (e: Exception) { 
                android.util.Log.e("MessageBubble", "Error parsing SharedPost: ${e.message}")
                null 
            }
        } else null
    }
    
    // Parsear datos de consulta/oferta
    val consultPostData = remember(message.content) {
        if (isConsultPost) {
            try {
                val jsonStr = message.content.removePrefix("[CONSULT_POST]")
                val json = org.json.JSONObject(jsonStr)
                ConsultPostData(
                    postId = json.optString("postId", ""),
                    productTitle = json.optString("productTitle", ""),
                    productPrice = json.optDouble("productPrice", 0.0),
                    productImage = json.optString("productImage", ""),
                    message = json.optString("message", ""),
                    type = json.optString("type", "inquiry") // "offer" o "inquiry"
                )
            } catch (e: Exception) {
                android.util.Log.e("MessageBubble", "Error parsing ConsultPost: ${e.message}")
                null
            }
        } else null
    }
    
    // Parsear datos del usuario compartido
    val sharedUserData = remember(message.content) {
        if (isSharedUser) {
            try {
                val jsonStr = message.content.removePrefix("[SHARED_USER]")
                val json = org.json.JSONObject(jsonStr)
                SharedUserData(
                    userId = json.optString("userId", ""),
                    username = json.optString("username", ""),
                    avatarUrl = json.optString("avatarUrl", ""),
                    storeName = json.optString("storeName", ""),
                    reputation = json.optDouble("reputation", 0.0),
                    isVerified = json.optBoolean("isVerified", false),
                    bannerUrl = json.optString("bannerUrl", ""),
                    followers = json.optInt("followers", 0),
                    clients = json.optInt("clients", 0),
                    postsCount = json.optInt("postsCount", 0)
                )
            } catch (e: Exception) {
                android.util.Log.e("MessageBubble", "Error parsing SharedUser: ${e.message}")
                null
            }
        } else null
    }
    
    // Parsear postId del artículo compartido
    val articleCardPostId = remember(message.content) {
        if (isArticleCard) {
            try {
                val jsonStr = message.content.removePrefix("[ARTICLE_CARD]")
                val json = org.json.JSONObject(jsonStr)
                json.optString("postId", "")
            } catch (e: Exception) { null }
        } else null
    }
    
    // Parsear datos de archivo
    val fileData = remember(message.content) {
        if (isFile) {
            try {
                val jsonStr = message.content.removePrefix("[FILE]")
                val json = org.json.JSONObject(jsonStr)
                Triple(
                    json.optString("url", ""),
                    json.optString("name", "archivo"),
                    json.optLong("size", 0L)
                )
            } catch (e: Exception) { null }
        } else null
    }
    
    // Si es mensaje de handshake status, renderizar como banner centrado de ancho completo
    if (isHandshakeStatus) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Surface,
                tonalElevation = 1.dp
            ) {
                val statusData = remember(message.id, message.content) {
                    try {
                        val jsonStr = message.content.removePrefix("[HANDSHAKE_STATUS]")
                        val json = org.json.JSONObject(jsonStr)
                        Triple(
                            json.optString("type", ""),
                            json.optString("message", ""),
                            json.optString("productDescription", "") to json.optDouble("agreedPrice", 0.0)
                        )
                    } catch (e: Exception) {
                        val fallbackMsg = when {
                            message.content.contains("AGREEMENT_CANCELLED") -> "Acuerdo cancelado"
                            message.content.contains("TRANSACTION_CANCELLED") -> "Transacción cancelada"
                            message.content.contains("TRANSACTION_COMPLETED") -> "Transacción completada"
                            else -> "Estado desconocido"
                        }
                        val fallbackType = when {
                            message.content.contains("CANCELLED") -> "CANCELLED"
                            message.content.contains("COMPLETED") -> "TRANSACTION_COMPLETED"
                            else -> "UNKNOWN"
                        }
                        Triple(fallbackType, fallbackMsg, "" to 0.0)
                    }
                }
                
                val (statusType, statusMessage, productData) = statusData
                val (productDesc, price) = productData
                val isCancellation = statusType.contains("CANCELLED")
                val isCompleted = statusType == "TRANSACTION_COMPLETED"
                val statusColor = if (isCancellation) Color(0xFFEF5350) else Color(0xFF22C55E)
                val statusIcon = if (isCancellation) Icons.Default.Close else Icons.Default.Check
                val title = if (isCompleted) "Transacción completada" else statusMessage
                
                val msgTimestamp = message.createdAt
                val formattedTime = remember(msgTimestamp) {
                    try {
                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                        val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
                        val date = inputFormat.parse(msgTimestamp.take(19))
                        date?.let { outputFormat.format(it) } ?: ""
                    } catch (e: Exception) { "" }
                }
                
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    // Primera línea: [icono + título] ... [precio]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(statusColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                color = statusColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (price > 0) {
                            Text(
                                text = "$${String.format("%.0f", price)}",
                                color = statusColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Segunda línea: [artículo] ... [hora] - siempre mostrar para ambos banners
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = productDesc.ifEmpty { "Artículo" },
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formattedTime,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        // Fila del mensaje (solo burbuja, avatar solo en header)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
        ) {
            
            // Detectar si es respuesta a historia
            val isStoryReply = message.content.contains("📷 Respondió a tu historia")
            val storyReplyParts = if (isStoryReply) {
                val parts = message.content.split("\n\n")
                if (parts.size >= 2) parts else null
            } else null
            
            // Row para contener burbuja + botón de reenvío (para posts compartidos)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Burbuja del mensaje
            Box(
                modifier = Modifier
                    .widthIn(max = 240.dp) // Ancho profesional para mensajes
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                            bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                        )
                    )
                    .background(if (message.isFromMe) myMessageBg else otherMessageBg)
                    .combinedClickable(
                        onClick = { 
                            // Si es post compartido, abrir ProductPage
                            if (isSharedPost && sharedPostData != null) {
                                onSharedPostClick?.invoke(sharedPostData.postId)
                            }
                            // Si es tarjeta de artículo, abrir ProductPage
                            if (isArticleCard && !articleCardPostId.isNullOrEmpty()) {
                                onSharedPostClick?.invoke(articleCardPostId)
                            }
                        },
                        onLongClick = { onLongPress(message) }
                    )
            ) {
                val isFullWidthContent = mediaUrl != null || isSharedPost || isLocation || isSharedUser || isArticleCard
                Column(
                    modifier = Modifier.padding(
                        start = if (isFullWidthContent) 0.dp else 10.dp,
                        end = if (isFullWidthContent) 0.dp else 10.dp,
                        top = if (isFullWidthContent) 0.dp else 8.dp,
                        bottom = if (isSharedPost || isLocation || isSharedUser || isArticleCard) 0.dp else 6.dp
                    )
                ) {
                    when {
                        // Post compartido - tarjeta visual profesional
                        isSharedPost && sharedPostData != null -> {
                            SharedPostMessageContent(
                                data = sharedPostData,
                                isFromMe = message.isFromMe,
                                timestamp = message.createdAt,
                                senderAvatar = otherUserAvatar
                            )
                        }
                        // Consulta u oferta de producto - tarjeta profesional
                        isConsultPost && consultPostData != null -> {
                            ConsultPostMessageContent(
                                data = consultPostData,
                                isFromMe = message.isFromMe,
                                timestamp = message.createdAt
                            )
                        }
                        // Respuesta a historia con imagen
                        isStoryReply && storyReplyParts != null -> {
                            // Header de respuesta
                            Text(
                                text = "📷 Respondió a tu historia",
                                color = if (message.isFromMe) Color.White.copy(alpha = 0.7f) else Color(0xFF1565A0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Placeholder de imagen de story (gradiente)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B35).copy(alpha = 0.3f),
                                                Color(0xFF2E8B57).copy(alpha = 0.2f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Mensaje de respuesta
                            val replyMessage = storyReplyParts.getOrNull(1)?.trim('"') ?: ""
                            if (replyMessage.isNotEmpty()) {
                                Text(
                                    text = replyMessage,
                                    color = if (message.isFromMe) myMessageText else otherMessageText,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        isAudio && mediaUrl != null -> {
                            AudioMessagePlayer(
                                audioUrl = mediaUrl,
                                isFromMe = message.isFromMe,
                                accentColor = if (message.isFromMe) Color.White else Color(0xFF1565A0),
                                timeColor = if (message.isFromMe) timeColor else otherTimeColor,
                                senderAvatar = if (message.isFromMe) 
                                    ProfileRepository.currentProfile.value?.avatarUrl 
                                else 
                                    otherUserAvatar,
                                sentTime = formatMessageTimeWithAmPm(message.createdAt)
                            )
                        }
                        // Mensaje de ubicación con mapa
                        isLocation && locationData != null -> {
                            val (lat, lng) = locationData
                            val mapboxToken = com.rendly.app.BuildConfig.MAPBOX_ACCESS_TOKEN
                            val latStr = String.format(java.util.Locale.US, "%.6f", lat)
                            val lngStr = String.format(java.util.Locale.US, "%.6f", lng)
                            val mapUrl = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/pin-l+a855f7($lngStr,$latStr)/$lngStr,$latStr,15,0/600x300@2x?access_token=$mapboxToken"
                            
                            Column {
                                // Mapa estático
                                var mapLoadError by remember { mutableStateOf(false) }
                                
                                if (!mapLoadError) {
                                AsyncImage(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(mapUrl)
                                        .crossfade(true)
                                        .memoryCacheKey("map_${latStr}_${lngStr}")
                                        .build(),
                                    contentDescription = "Ubicación",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                    onError = { error ->
                                        android.util.Log.e("ChatScreen", "Error cargando mapa: ${error.result.throwable.message}")
                                        mapLoadError = true
                                    }
                                )
                                } else {
                                    // Fallback si el mapa no carga
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFF1A237E).copy(alpha = 0.3f),
                                                        Color(0xFF0D47A1).copy(alpha = 0.2f)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFF64B5F6),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$latStr, $lngStr",
                                                color = Color(0xFF90CAF9),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                // Footer con coordenadas
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = if (message.isFromMe) Color.White else Color(0xFF1565A0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ubicación compartida",
                                        color = if (message.isFromMe) myMessageText else otherMessageText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        (isImage || isVideo) && mediaUrl != null -> {
                            // Imagen sin border radius (solo el contenedor lo tiene)
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = if (isVideo) "Video" else "Imagen",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp, max = 220.dp)
                            )
                            if (isVideo) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Reproducir",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                        // Usuario compartido - tarjeta profesional
                        isSharedUser && sharedUserData != null -> {
                            SharedUserMessageContent(
                                data = sharedUserData,
                                isFromMe = message.isFromMe,
                                timestamp = message.createdAt,
                                messageStatus = message.status,
                                onViewProfile = {
                                    onNavigateToUserProfile?.invoke(sharedUserData.userId)
                                }
                            )
                        }
                        // Tarjeta de artículo compartido - datos dinámicos
                        isArticleCard && !articleCardPostId.isNullOrEmpty() -> {
                            ArticleCardMessageContent(
                                postId = articleCardPostId,
                                isFromMe = message.isFromMe,
                                timestamp = message.createdAt,
                                messageStatus = message.status,
                                onOpenArticle = {
                                    onSharedPostClick?.invoke(articleCardPostId)
                                }
                            )
                        }
                        // Archivo compartido
                        isFile && fileData != null -> {
                            val (fileUrl, fileName, fileSize) = fileData
                            val context = LocalContext.current
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(fileUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.util.Log.e("ChatScreen", "Error abriendo archivo: ${e.message}")
                                        }
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Color(0xFF2196F3).copy(alpha = 0.15f),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.InsertDriveFile,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fileName,
                                        color = if (message.isFromMe) myMessageText else otherMessageText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (fileSize > 0) {
                                        val sizeStr = when {
                                            fileSize < 1024 -> "$fileSize B"
                                            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
                                            else -> "${String.format("%.1f", fileSize / (1024.0 * 1024.0))} MB"
                                        }
                                        Text(
                                            text = sizeStr,
                                            color = if (message.isFromMe) timeColor else otherTimeColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = "Descargar",
                                    tint = if (message.isFromMe) Color.White.copy(alpha = 0.6f) else Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        // Mensaje de handshake iniciado (legacy - ya no se envía)
                        isHandshakeInitiated -> {
                            // Mantener por compatibilidad con mensajes antiguos
                            Text(
                                text = "Propuesta de transacción",
                                color = Color(0xFF22C55E),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        
                        else -> {
                            // Mensaje de texto normal - texto arriba izquierda
                            // Si es SharedPost pero falló el parsing, mostrar placeholder
                            if (isSharedPost) {
                                // Placeholder mientras carga o si parsing falló
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = PrimaryPurple,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cargando producto...",
                                        color = if (message.isFromMe) myMessageText else otherMessageText,
                                        fontSize = 13.sp
                                    )
                                }
                            } else if (isSharedUser) {
                                // SharedUser parsing failed - show compact fallback (no flash)
                                Text(
                                    text = "Perfil compartido",
                                    color = if (message.isFromMe) myMessageText else otherMessageText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else if (isArticleCard) {
                                // ArticleCard parsing fallback - show placeholder instead of raw JSON
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF00BFA5),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cargando artículo...",
                                        color = if (message.isFromMe) myMessageText else otherMessageText,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = message.content,
                                    color = if (message.isFromMe) myMessageText else otherMessageText,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    
                    // Hora y ticks - abajo derecha (NO mostrar para posts compartidos, audios, usuarios compartidos ni tarjetas de artículo)
                    if (!isSharedPost && !isAudio && !isSharedUser && !isArticleCard) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp, end = 6.dp, bottom = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = formatMessageTimeWithAmPm(message.createdAt),
                                color = if (message.isFromMe) timeColor else otherTimeColor,
                                fontSize = 10.sp
                            )
                            if (message.isFromMe) {
                                MessageStatusIcon(
                                    status = message.status,
                                    tintColor = if (message.status == MessageStatus.READ) 
                                        Color(0xFF53BDEB) else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Botón de reenvío FUERA de la burbuja para posts compartidos recibidos
            if (isSharedPost && !message.isFromMe && sharedPostData != null && onForwardSharedPost != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2F38))
                        .clickable { onForwardSharedPost(sharedPostData) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Reenviar",
                        tint = PrimaryPurple,
                        modifier = Modifier
                            .size(17.dp)
                            .rotate(-45f)
                    )
                }
            }
            } // Cierra Row de burbuja + botón
            
            if (message.isFromMe) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        // Reacciones FUERA de la burbuja
        if (hasReactions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = if (reactionsOnRight) Arrangement.End else Arrangement.Start
            ) {
                message.reactions.forEach { (emoji, users) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3A3F48),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                            if (users.size > 1) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "${users.size}", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Formato de hora con a. m. / p. m.
private fun formatMessageTimeWithAmPm(isoTimestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(isoTimestamp)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        val hour = zoned.hour
        val minute = zoned.minute
        val isPm = hour >= 12
        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (isPm) "p. m." else "a. m."
        "$hour12:${minute.toString().padStart(2, '0')} $amPm"
    } catch (e: Exception) {
        ""
    }
}

// Reproductor de mensajes de audio con WAVEFORM profesional y SEEK
@Composable
private fun AudioMessagePlayer(
    audioUrl: String,
    isFromMe: Boolean,
    accentColor: Color,
    timeColor: Color,
    senderAvatar: String? = null,
    sentTime: String = ""
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableStateOf("0:00") }
    var durationMs by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableStateOf("0:00") }
    var isSeeking by remember { mutableStateOf(false) }
    
    val mediaPlayer = remember { android.media.MediaPlayer() }
    
    // Generar waveform pseudo-aleatorio pero consistente
    val waveformBars = remember(audioUrl) {
        val seed = audioUrl.hashCode()
        val random = java.util.Random(seed.toLong())
        List(32) { 0.25f + random.nextFloat() * 0.75f }
    }
    
    // Preparar MediaPlayer
    DisposableEffect(audioUrl) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp ->
                durationMs = mp.duration
                val totalSecs = mp.duration / 1000
                duration = "${totalSecs / 60}:${(totalSecs % 60).toString().padStart(2, '0')}"
            }
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                progress = 0f
                currentTime = "0:00"
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayer", "Error: ${e.message}")
        }
        
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) { }
        }
    }
    
    // Actualizar progreso
    LaunchedEffect(isPlaying, isSeeking) {
        while (isPlaying && !isSeeking && mediaPlayer.isPlaying) {
            try {
                val current = mediaPlayer.currentPosition
                val total = mediaPlayer.duration.coerceAtLeast(1)
                progress = current.toFloat() / total
                val secs = current / 1000
                currentTime = "${secs / 60}:${(secs % 60).toString().padStart(2, '0')}"
            } catch (_: Exception) { }
            kotlinx.coroutines.delay(50)
        }
    }
    
    // Función para seek
    fun seekTo(newProgress: Float) {
        try {
            val seekPos = (newProgress * durationMs).toInt()
            mediaPlayer.seekTo(seekPos)
            progress = newProgress
            val secs = seekPos / 1000
            currentTime = "${secs / 60}:${(secs % 60).toString().padStart(2, '0')}"
        } catch (_: Exception) { }
    }
    
    // Layout profesional:
    // [Avatar] [Play] [--------Waveform--------]
    //                 [time]          [hr enviado]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar del usuario - alineado con Play/Waveform
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (senderAvatar != null) {
                AsyncImage(
                    model = senderAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Contenido: Play + Waveform arriba, Time + hr enviado abajo
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Primera línea: [Play] [Waveform] - altura 40dp para alinear con avatar
            Row(
                modifier = Modifier.height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Play/Pause
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                        .clickable {
                            try {
                                if (isPlaying) {
                                    mediaPlayer.pause()
                                    isPlaying = false
                                } else {
                                    mediaPlayer.start()
                                    isPlaying = true
                                }
                            } catch (_: Exception) { }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Waveform con SEEK
                SeekableWaveform(
                    waveformBars = waveformBars,
                    progress = progress,
                    accentColor = accentColor,
                    isPlaying = isPlaying,
                    onSeekStart = { isSeeking = true },
                    onSeek = { newProgress -> 
                        progress = newProgress
                        val secs = (newProgress * durationMs / 1000).toInt()
                        currentTime = "${secs / 60}:${(secs % 60).toString().padStart(2, '0')}"
                    },
                    onSeekEnd = { finalProgress ->
                        seekTo(finalProgress)
                        isSeeking = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                )
            }
            
            // Segunda línea: [time] debajo del waveform + [hr enviado] esquina inferior derecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, top = 2.dp), // 32dp play + 8dp spacer = 40dp
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPlaying) currentTime else duration,
                    color = timeColor.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (sentTime.isNotEmpty()) {
                    Text(
                        text = sentTime,
                        color = timeColor.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// Waveform con soporte de SEEK (deslizar para adelantar/atrasar)
@Composable
private fun SeekableWaveform(
    waveformBars: List<Float>,
    progress: Float,
    accentColor: Color,
    isPlaying: Boolean,
    onSeekStart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekEnd: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var isDragging by remember { mutableStateOf(false) }
    var localProgress by remember { mutableFloatStateOf(progress) }
    
    // Sincronizar con progreso externo cuando no está arrastrando
    LaunchedEffect(progress, isDragging) {
        if (!isDragging) {
            localProgress = progress
        }
    }
    
    // Animación suave del progreso
    val animatedProgress by animateFloatAsState(
        targetValue = localProgress,
        animationSpec = tween(if (isDragging) 0 else 50, easing = LinearEasing),
        label = "waveformProgress"
    )
    
    // Pulso cuando reproduce
    val infiniteTransition = rememberInfiniteTransition(label = "waveformPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying && !isDragging) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val barCount = waveformBars.size
    
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeekEnd(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        onSeekStart()
                        localProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(localProgress)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        localProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        onSeek(localProgress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeekEnd(localProgress)
                    },
                    onDragCancel = {
                        isDragging = false
                        onSeekEnd(localProgress)
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / barCount * 0.55f
        val spacing = canvasWidth / barCount * 0.45f
        val totalBarWidth = barWidth + spacing
        val maxHeight = canvasHeight * 0.85f
        val centerY = canvasHeight / 2
        
        waveformBars.forEachIndexed { index, heightFactor ->
            val x = index * totalBarWidth + barWidth / 2
            val barProgress = x / canvasWidth
            val isPast = barProgress <= animatedProgress
            
            val baseHeight = maxHeight * heightFactor
            val height = if (isPlaying && isPast && !isDragging) baseHeight * pulseScale else baseHeight
            
            val barColor = if (isPast) accentColor else accentColor.copy(alpha = 0.3f)
            
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x - barWidth / 2, centerY - height / 2),
                size = Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
        
        // Indicador de posición actual (círculo pequeño)
        if (isDragging || animatedProgress > 0.01f) {
            val indicatorX = animatedProgress * canvasWidth
            drawCircle(
                color = accentColor,
                radius = with(density) { 5.dp.toPx() },
                center = Offset(indicatorX, centerY)
            )
        }
    }
}

// Icono de estado del mensaje (ticks)
@Composable
private fun MessageStatusIcon(
    status: MessageStatus,
    tintColor: Color = Color.White.copy(alpha = 0.7f)
) {
    when (status) {
        MessageStatus.SENDING -> {
            // Reloj pequeño mientras se envía
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = "Enviando",
                tint = tintColor.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.SENT -> {
            // Un solo tick
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Enviado",
                tint = tintColor,
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.DELIVERED, MessageStatus.READ -> {
            // Doble tick
            Row(modifier = Modifier.width(18.dp)) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(14.dp)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (status == MessageStatus.READ) "Leído" else "Entregado",
                    tint = tintColor,
                    modifier = Modifier
                        .size(14.dp)
                        .offset(x = (-8).dp)
                )
            }
        }
    }
}

// Burbuja de "Escribiendo..." con animación de puntos
@Composable
private fun TypingIndicatorBubble(userAvatar: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = userAvatar ?: "https://ui-avatars.com/api/?name=U&background=A78BFA&color=fff",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .background(
                    color = Surface,
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TypingDotsAnimation()
        }
    }
}

// Animación de 3 puntos que aparecen y desaparecen
@Composable
private fun TypingDotsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    // Animación que va de 0 a 3 repetidamente
    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )
    
    // Mostrar puntos según el valor
    val dotsToShow = dotCount.toInt().coerceIn(0, 3)
    val dots = ".".repeat(dotsToShow)
    
    Text(
        text = dots.ifEmpty { " " }, // Espacio para mantener altura
        color = TextMuted,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.widthIn(min = 24.dp)
    )
}

/* ═══════════════════════════════════════════════════════════════
   CHAT INPUT V3 - Sistema de grabación profesional MEJORADO
   - Hold-to-record: mantener para grabar, soltar para enviar
   - Slide-up-to-lock: deslizar arriba para bloquear (manos libres)
   - Slide-left-to-cancel: deslizar izquierda para cancelar/eliminar
   - Animaciones fluidas y profesionales
   - Contenedor visual que sigue al dedo
═══════════════════════════════════════════════════════════════ */

@Composable
private fun ChatInputV2(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    onCameraClick: () -> Unit,
    onHandshakeClick: () -> Unit,
    onVoiceRecord: () -> Unit,
    onAudioRecorded: (File) -> Unit,
    isUploading: Boolean = false,
    isRecording: Boolean = false
) {
    val hasText = text.isNotBlank()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Estados de grabación profesional
    var isRecordingAudio by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableIntStateOf(0) }
    
    // Waveform data - amplitudes en tiempo real
    var waveformData by remember { mutableStateOf(listOf<Float>()) }
    
    // MediaRecorder
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    
    // Timer de grabación
    LaunchedEffect(isRecordingAudio, isPaused) {
        if (isRecordingAudio && !isPaused) {
            while (isRecordingAudio && !isPaused) {
                delay(1000)
                recordingTime++
            }
        }
    }
    
    // Simulador de waveform (actualiza cada 100ms mientras graba)
    LaunchedEffect(isRecordingAudio, isPaused) {
        if (isRecordingAudio && !isPaused) {
            while (isRecordingAudio && !isPaused) {
                delay(100)
                // Generar amplitud pseudo-aleatoria basada en el tiempo
                val newAmplitude = (0.3f + (kotlin.math.sin(System.currentTimeMillis() / 100.0) * 0.3f + Math.random() * 0.4f)).toFloat().coerceIn(0.1f, 1f)
                waveformData = (waveformData + newAmplitude).takeLast(50)
            }
        }
    }
    
    fun startRecording() {
        try {
            val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            audioFile = file
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecordingAudio = true
            isPaused = false
            recordingTime = 0
            waveformData = emptyList()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecord", "Error: ${e.message}")
        }
    }
    
    fun pauseRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                isPaused = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecord", "Error pause: ${e.message}")
        }
    }
    
    fun resumeRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                isPaused = false
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecord", "Error resume: ${e.message}")
        }
    }
    
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            audioFile?.delete()
            audioFile = null
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecord", "Error cancel: ${e.message}")
            audioFile?.delete()
        }
        isRecordingAudio = false
        isPaused = false
        recordingTime = 0
        waveformData = emptyList()
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun sendRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            if (audioFile?.exists() == true && recordingTime >= 1) {
                onAudioRecorded(audioFile!!)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else {
                audioFile?.delete()
            }
            audioFile = null
        } catch (e: Exception) {
            android.util.Log.e("VoiceRecord", "Error send: ${e.message}")
            audioFile?.delete()
        }
        isRecordingAudio = false
        isPaused = false
        recordingTime = 0
        waveformData = emptyList()
    }
    
    // ═══ FOOTER PRINCIPAL ═══
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 4.dp
    ) {
        AnimatedContent(
            targetState = isRecordingAudio,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "footerTransition"
        ) { recording ->
            if (recording) {
                // ═══ MODO GRABANDO ═══
                ProfessionalRecordingFooter(
                    recordingTime = recordingTime,
                    isPaused = isPaused,
                    waveformData = waveformData,
                    onDelete = { cancelRecording() },
                    onPauseResume = { 
                        if (isPaused) resumeRecording() else pauseRecording() 
                    },
                    onSend = { sendRecording() }
                )
            } else {
                // ═══ MODO NORMAL ═══
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Botón Handshake
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color(0xFF22C55E).copy(alpha = 0.15f),
                        onClick = onHandshakeClick
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Outlined.Handshake,
                                contentDescription = "Confirmar compra",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Campo de texto
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onAttachmentClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adjuntar",
                                    tint = TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (text.isEmpty()) {
                                    Text(
                                        text = "Mensaje",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = text,
                                    onValueChange = onTextChange,
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(PrimaryPurple),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 4
                                )
                            }
                            
                            IconButton(
                                onClick = onCameraClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = "Cámara",
                                    tint = TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    
                    // Botón enviar o grabar
                    if (hasText) {
                        IconButton(
                            onClick = onSend,
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF2D3748), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // Botón micrófono - toque simple para iniciar grabación
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = Color(0xFF2D3748),
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    startRecording()
                                } else {
                                    onVoiceRecord()
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Grabar audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══ FOOTER DE GRABACIÓN PROFESIONAL ═══
@Composable
private fun ProfessionalRecordingFooter(
    recordingTime: Int,
    isPaused: Boolean,
    waveformData: List<Float>,
    onDelete: () -> Unit,
    onPauseResume: () -> Unit,
    onSend: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1D21))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Primera fila: [time] [waveform] [indicador REC]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tiempo de grabación
            Text(
                text = String.format("%d:%02d", recordingTime / 60, recordingTime % 60),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(52.dp)
            )
            
            // Waveform en tiempo real
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2D3748))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val barWidth = 3.dp.toPx()
                    val barSpacing = 2.dp.toPx()
                    val maxBars = ((size.width + barSpacing) / (barWidth + barSpacing)).toInt()
                    val displayData = if (waveformData.size > maxBars) {
                        waveformData.takeLast(maxBars)
                    } else {
                        waveformData
                    }
                    
                    displayData.forEachIndexed { index, amplitude ->
                        val barHeight = (amplitude * size.height).coerceIn(4.dp.toPx(), size.height)
                        val x = index * (barWidth + barSpacing)
                        val y = (size.height - barHeight) / 2
                        
                        drawRoundRect(
                            color = if (isPaused) Color(0xFF6B7280) else Color(0xFF22C55E),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                        )
                    }
                }
            }
            
            // Indicador REC pulsante
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (isPaused) Color.Gray else Color.Red.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        )
                )
                Text(
                    text = if (isPaused) "PAUSE" else "REC",
                    color = if (isPaused) Color.Gray else Color.Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Segunda fila: [eliminar] [pausa] [enviar]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Eliminar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.Red.copy(alpha = 0.15f),
                onClick = onDelete
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar grabación",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Botón Pausa/Reanudar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color(0xFF3B4048),
                onClick = onPauseResume
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Reanudar" else "Pausar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Botón Enviar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFF22C55E),
                onClick = onSend
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar audio",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// Footer de grabación mejorado (legacy - mantener por compatibilidad)
@Composable
private fun VoiceRecordingFooter(
    recordingTime: Int,
    isLocked: Boolean,
    isCancelling: Boolean,
    cancelProgress: Float,
    alpha: Float,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Animación de slide para el texto de cancelar
    val slideOffset by animateFloatAsState(
        targetValue = if (isCancelling) -20f else 0f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "slideOffset"
    )
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(HomeBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLocked) {
            // Modo bloqueado: [Cancelar] [Tiempo + Grabando] [Enviar]
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.Red.copy(alpha = 0.15f),
                onClick = onCancel
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Cancelar",
                        tint = Color.Red,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            // Centro: indicador de grabación
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Punto pulsante
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Red.copy(alpha = pulseAlpha), CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = String.format("%d:%02d", recordingTime / 60, recordingTime % 60),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Grabando",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            
            // Botón enviar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFF22C55E),
                onClick = onSend
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar audio",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
            // Modo no bloqueado: indicadores de gestos
            // Punto pulsante
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Red.copy(alpha = pulseAlpha), CircleShape)
            )
            
            // Tiempo
            Text(
                text = String.format("%d:%02d", recordingTime / 60, recordingTime % 60),
                color = Color.Red,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            // Texto de instrucción que cambia según el gesto
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(x = slideOffset.dp)
            ) {
                AnimatedContent(
                    targetState = isCancelling,
                    transitionSpec = {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                    },
                    label = "instructionText"
                ) { cancelling ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (cancelling) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Suelta para cancelar",
                                color = Color.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "◀ Desliza para cancelar",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            
            // Espacio para el botón flotante
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   MENÚ DE ADJUNTOS EXPANDIBLE
═══════════════════════════════════════════════════════════════ */

@Composable
private fun AttachmentMenu(
    onDismiss: () -> Unit,
    onSelectImage: () -> Unit,
    onSelectFile: () -> Unit,
    onSelectLocation: () -> Unit,
    onSelectContact: () -> Unit,
    onSelectArticle: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachmentOption(
                icon = Icons.Outlined.InsertDriveFile,
                label = "Archivo",
                color = Color(0xFF2196F3),
                onClick = onSelectFile
            )
            AttachmentOption(
                icon = Icons.Outlined.LocationOn,
                label = "Ubicación",
                color = Color(0xFFFF5722),
                onClick = onSelectLocation
            )
            AttachmentOption(
                icon = Icons.Outlined.Person,
                label = "Usuario",
                color = Color(0xFF9C27B0),
                onClick = onSelectContact
            )
            AttachmentOption(
                icon = Icons.Outlined.Inventory2,
                label = "Artículo",
                color = Color(0xFF00BFA5),
                onClick = onSelectArticle
            )
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatMessageTime(timestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(timestamp.replace(" ", "T").let {
            if (!it.endsWith("Z")) "${it}Z" else it
        })
        val localTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        String.format("%02d:%02d", localTime.hour, localTime.minute)
    } catch (e: Exception) {
        ""
    }
}

/* ═══════════════════════════════════════════════════════════════
   MODAL DE OPCIONES DE MENSAJE (al mantener pulsado)
   - Fondo oscuro semi-transparente
   - Emojis arriba del menú de opciones
   - Menú de opciones alineado al lado del mensaje
═══════════════════════════════════════════════════════════════ */

@Composable
private fun MessageOptionsModal(
    message: Message,
    onDismiss: () -> Unit,
    onReaction: (String) -> Unit,
    onEdit: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val quickReactions = listOf("❤️", "😂", "😮", "😢", "😡", "👍")
    val scope = rememberCoroutineScope()
    var isDeleting by remember { mutableStateOf(false) }
    
    // Fondo oscuro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        // Contenedor principal centrado
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
        ) {
            // Barra de emojis
            AnimatedVisibility(
                visible = true,
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF2A2F38),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        quickReactions.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { onReaction(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 22.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3A3F48))
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Más",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Menú de opciones
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2A2F38),
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.width(180.dp)) {
                    if (message.isFromMe) {
                        MessageOptionItem(Icons.Outlined.Edit, "Editar", onEdit)
                    }
                    MessageOptionItem(Icons.Outlined.Reply, "Reenviar", onForward)
                    MessageOptionItem(Icons.Outlined.ContentCopy, "Copiar", onCopy)
                    // Solo mostrar eliminar para mensajes propios
                    if (message.isFromMe) {
                        if (isDeleting) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = false) {}
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            MessageOptionItem(Icons.Outlined.Delete, "Eliminar", {
                                isDeleting = true
                                scope.launch {
                                    kotlinx.coroutines.delay(800)
                                    onDelete()
                                }
                            }, true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageOptionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isDestructive) Color(0xFFEF4444) else Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            color = if (isDestructive) Color(0xFFEF4444) else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Componente visual para posts compartidos en el chat
 * Diseño: header (username izq + precio der), imagen, mensaje personalizado, hora
 */
@Composable
private fun SharedPostMessageContent(
    data: SharedPostData,
    isFromMe: Boolean,
    timestamp: String = "",
    senderAvatar: String? = null
) {
    val textColor = if (isFromMe) Color.White else Color(0xFFE7E9EA)
    val headerBg = Color.Black.copy(alpha = 0.4f)
    
    // Estado para el carrusel
    val pagerState = rememberPagerState(pageCount = { data.images.size.coerceAtLeast(1) })
    
    Column(
        modifier = Modifier.width(220.dp) // Ancho profesional reducido
    ) {
        // Header con avatar+username izquierda, precio derecha
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Usuario (izquierda)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = data.ownerAvatar.ifEmpty { 
                            "https://ui-avatars.com/api/?name=${data.ownerUsername}&background=A78BFA&color=fff&size=64" 
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = data.ownerUsername,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Precio (derecha en header)
                if (data.price > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${String.format("%.2f", data.price)}",
                        color = Color(0xFF00D26A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Carrusel de imágenes - SIN bordes, full width
        if (data.images.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = data.images[page],
                        contentDescription = data.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Indicador de páginas si hay más de 1 imagen
                if (data.images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(data.images.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) 
                                            Color.White 
                                        else 
                                            Color.White.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }
                }
            }
        }
        
        // Mensaje personalizado (sobre la hora)
        if (data.customMessage.isNotEmpty()) {
            Text(
                text = data.customMessage,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(top = 8.dp)
            )
        }
        
        // Footer: hora de envío alineada a la derecha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hora de envío - alineada a la derecha
            Text(
                text = formatMessageTimeWithAmPm(timestamp),
                color = textColor.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}


/**
 * Modal para reenviar un post compartido a otro usuario
 */
@Composable
private fun ForwardSharedPostModal(
    isVisible: Boolean,
    postData: SharedPostData,
    onDismiss: () -> Unit,
    onForward: (targetUserId: String, customMessage: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<Usuario?>(null) }
    var customMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Buscar usuarios
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2 && selectedUser == null) {
            isSearching = true
            kotlinx.coroutines.delay(300)
            searchResults = ChatRepository.searchUsers(searchQuery)
            isSearching = false
        } else if (selectedUser == null) {
            searchResults = emptyList()
        }
    }
    
    // Reset al cerrar
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            searchQuery = ""
            searchResults = emptyList()
            selectedUser = null
            customMessage = ""
            isSending = false
        }
    }
    
    // Fondo oscuro con fade
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { onDismiss() }
        )
    }
    
    // Panel deslizante desde abajo
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = HomeBg
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedUser != null) {
                                selectedUser = null
                                customMessage = ""
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedUser != null) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = if (selectedUser != null) "Volver" else "Cerrar",
                            tint = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = if (selectedUser != null) "Enviar a @${selectedUser?.username}" else "Reenviar producto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Preview del producto
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = postData.images.firstOrNull() ?: "https://via.placeholder.com/50",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = postData.title.ifEmpty { "Producto" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (postData.price > 0) {
                                Text(
                                    text = "$${String.format("%.2f", postData.price)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00D26A)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedUser == null) {
                    // Campo de búsqueda
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                                singleLine = true,
                                cursorBrush = SolidColor(PrimaryPurple),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text("Buscar usuario...", color = TextMuted, fontSize = 15.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lista de resultados
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSearching) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = PrimaryPurple,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        } else {
                            items(searchResults, key = { it.userId }) { user ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedUser = user },
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = user.avatarUrl ?: "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff",
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = user.nombreTienda ?: user.nombre ?: user.username,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "@${user.username}",
                                                fontSize = 13.sp,
                                                color = TextMuted
                                            )
                                        }
                                        
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Usuario seleccionado - input de mensaje
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AsyncImage(
                            model = selectedUser?.avatarUrl ?: "https://ui-avatars.com/api/?name=${selectedUser?.username}&background=A78BFA&color=fff",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = selectedUser?.nombreTienda ?: selectedUser?.nombre ?: selectedUser?.username ?: "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Text(
                            text = "@${selectedUser?.username}",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Mensaje opcional",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                    
                    // Input y botón de envío
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            color = Surface
                        ) {
                            BasicTextField(
                                value = customMessage,
                                onValueChange = { customMessage = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                                singleLine = false,
                                maxLines = 3,
                                cursorBrush = SolidColor(PrimaryPurple),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (customMessage.isEmpty()) {
                                            Text("Escribe un mensaje...", color = TextMuted, fontSize = 15.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = {
                                if (!isSending && selectedUser != null) {
                                    isSending = true
                                    onForward(selectedUser!!.userId, customMessage)
                                }
                            },
                            enabled = !isSending,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple)
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
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

/**
 * Modal de Handshake para confirmar compra/venta en persona
 * Shadow con fade independiente + Panel con slide-up/down fluido
 */
@Composable
private fun HandshakeConfirmationModal(
    isVisible: Boolean,
    otherUser: Usuario,
    onDismiss: () -> Unit,
    onConfirm: (productDescription: String, agreedPrice: Double) -> Unit
) {
    var productDescription by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    
    // Reset al cerrar (con delay para permitir animación de salida)
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            kotlinx.coroutines.delay(300) // Esperar que termine la animación de salida
            productDescription = ""
            priceText = ""
            isConfirming = false
        }
    }
    
    // Siempre renderizar el Box para que AnimatedVisibility pueda animar entrada/salida
    Box(modifier = Modifier.fillMaxSize()) {
        // Shadow con fade independiente (no sube con el panel)
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures { 
                            if (!isConfirming) onDismiss() 
                        }
                    }
            )
        }
        
        // Panel que sube/baja desde abajo (animación independiente)
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = tween(350, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetOffsetY = { it }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(), // Subir sobre el teclado
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = HomeBg,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Handle indicator
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(TextMuted.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Header compacto
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Handshake,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Confirmar transacción",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "con @${otherUser.username}",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Campo: Descripción del producto
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Surface
                        ) {
                            BasicTextField(
                                value = productDescription,
                                onValueChange = { productDescription = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                                singleLine = false,
                                maxLines = 2,
                                cursorBrush = SolidColor(PrimaryPurple),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (productDescription.isEmpty()) {
                                            Text("¿Qué estás comprando/vendiendo?", color = TextMuted, fontSize = 15.sp)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Campo: Precio acordado
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$",
                                    color = Color(0xFF22C55E),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = priceText,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                            priceText = newValue
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(PrimaryPurple),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (priceText.isEmpty()) {
                                                Text("0.00", color = TextMuted, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                            ) {
                                Text("Cancelar")
                            }
                            
                            Button(
                                onClick = {
                                    val price = priceText.toDoubleOrNull() ?: 0.0
                                    if (productDescription.isNotBlank() && price > 0) {
                                        isConfirming = true
                                        onConfirm(productDescription.trim(), price)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = productDescription.isNotBlank() && (priceText.toDoubleOrNull() ?: 0.0) > 0 && !isConfirming,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                if (isConfirming) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Iniciar", fontWeight = FontWeight.Bold)
                                        Icon(
                                            imageVector = Icons.Outlined.Handshake,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

/* ═══════════════════════════════════════════════════════════════
   MODAL DE AJUSTES DEL CHAT - Profesional y moderno
═══════════════════════════════════════════════════════════════ */

@Composable
private fun ChatSettingsModal(
    isVisible: Boolean,
    otherUser: Usuario,
    conversationId: String,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onBlockStateChanged: (Boolean) -> Unit = {},
    onReportSent: () -> Unit = {},
    onChatCleared: () -> Unit = {},
    onSearchInChat: () -> Unit = {},
    onScrollToMessage: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var isMuted by remember { mutableStateOf(false) }
    var isBlocked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Sub-screens
    var showSearchScreen by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    
    // Feedback messages
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    
    // Load initial states from Supabase
    LaunchedEffect(isVisible, conversationId) {
        if (isVisible && conversationId.isNotEmpty()) {
            isMuted = ChatRepository.isChatMuted(conversationId)
            isBlocked = ChatRepository.isUserBlocked(otherUser.userId)
        }
    }
    
    // Auto-dismiss feedback
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            kotlinx.coroutines.delay(2000)
            feedbackMessage = null
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    }
            )
        }
        
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = tween(350, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetOffsetY = { it }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = HomeBg,
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .background(TextMuted.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Header with avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = otherUser.avatarUrl ?: "https://ui-avatars.com/api/?name=${otherUser.username}&background=A78BFA&color=fff",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ajustes del chat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("con @${otherUser.username}", fontSize = 13.sp, color = TextMuted)
                        }
                    }
                    
                    // Feedback banner
                    AnimatedVisibility(visible = feedbackMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF22C55E).copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(feedbackMessage ?: "", fontSize = 13.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // ═══ Mute notifications ═══
                    ChatSettingsOption(
                        icon = if (isMuted) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications,
                        title = if (isMuted) "Activar notificaciones" else "Silenciar notificaciones",
                        subtitle = if (isMuted) "Recibirás notificaciones de este chat" else "No recibirás notificaciones de este chat",
                        iconColor = if (isMuted) AccentYellow else TextSecondary,
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val success = if (isMuted) {
                                    ChatRepository.unmuteChat(conversationId)
                                } else {
                                    ChatRepository.muteChat(conversationId)
                                }
                                if (success) {
                                    isMuted = !isMuted
                                }
                                isLoading = false
                            }
                        }
                    )
                    
                    Divider(color = Surface, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    
                    // ═══ Search ═══
                    ChatSettingsOption(Icons.Outlined.Search, "Buscar en el chat", "Encuentra mensajes específicos", PrimaryPurple) {
                        onSearchInChat()
                    }
                    
                    // ═══ Export ═══
                    ChatSettingsOption(Icons.Outlined.Download, "Exportar chat", "Descargar conversación como PDF", AccentBlue) {
                        scope.launch {
                            isLoading = true
                            val pdfFile = ChatRepository.exportChatAsPdf(context, conversationId, otherUser.username)
                            isLoading = false
                            
                            if (pdfFile != null) {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    pdfFile
                                )
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Chat con @${otherUser.username} - Rendly")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Exportar chat"))
                            } else {
                                feedbackMessage = "Error al generar PDF"
                            }
                        }
                    }
                    
                    Divider(color = Surface, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    
                    // ═══ Clear chat ═══
                    ChatSettingsOption(Icons.Outlined.DeleteSweep, "Vaciar chat", "Eliminar todos los mensajes y transacciones", AccentYellow) {
                        showClearConfirm = true
                    }
                    
                    // ═══ Block user ═══
                    ChatSettingsOption(
                        icon = if (isBlocked) Icons.Outlined.LockOpen else Icons.Outlined.Block,
                        title = if (isBlocked) "Desbloquear usuario" else "Bloquear usuario",
                        subtitle = if (isBlocked) "Permitir que te contacte" else "No podrá contactarte ni ver tu perfil",
                        iconColor = if (isBlocked) Color(0xFF22C55E) else Color(0xFFEF4444)
                    ) {
                        if (isBlocked) {
                            scope.launch {
                                val success = ChatRepository.unblockUser(otherUser.userId)
                                if (success) {
                                    isBlocked = false
                                    onBlockStateChanged(false)
                                    feedbackMessage = "Usuario desbloqueado"
                                }
                            }
                        } else {
                            showBlockConfirm = true
                        }
                    }
                    
                    // ═══ Report user ═══
                    ChatSettingsOption(Icons.Outlined.Flag, "Reportar usuario", "Denunciar comportamiento inadecuado", Color(0xFFEF4444)) {
                        showReportDialog = true
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Loading indicator
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = PrimaryPurple
                        )
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface)
                    ) {
                        Text("Cerrar", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
    
    // ═══ Clear chat confirmation dialog ═══
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = HomeBg,
            title = {
                Text("Vaciar chat", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "Se eliminarán todos los mensajes y las transacciones completadas o canceladas de esta conversación. Esta acción no se puede deshacer.",
                    color = TextSecondary, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        scope.launch {
                            isLoading = true
                            val success = ChatRepository.clearChat(conversationId)
                            isLoading = false
                            if (success) {
                                onChatCleared()
                                feedbackMessage = "Chat vaciado correctamente"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Vaciar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
    
    // ═══ Block user confirmation dialog ═══
    if (showBlockConfirm) {
        AlertDialog(
            onDismissRequest = { showBlockConfirm = false },
            containerColor = HomeBg,
            title = {
                Text("Bloquear a @${otherUser.username}", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "No podrá ver tu perfil, publicaciones ni contactarte. Tú tampoco verás su contenido. Puedes desbloquearlo en cualquier momento.",
                    color = TextSecondary, lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirm = false
                        scope.launch {
                            isLoading = true
                            val success = ChatRepository.blockUser(otherUser.userId)
                            isLoading = false
                            if (success) {
                                isBlocked = true
                                onBlockStateChanged(true)
                                feedbackMessage = "Usuario bloqueado"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Bloquear", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirm = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
    
    // ═══ Report user dialog with reason picker ═══
    if (showReportDialog) {
        var selectedReason by remember { mutableStateOf<String?>(null) }
        var reportDescription by remember { mutableStateOf("") }
        var reportSent by remember { mutableStateOf(false) }
        
        val reasons = listOf(
            "spam" to "Spam o publicidad",
            "harassment" to "Acoso o intimidación",
            "inappropriate" to "Contenido inapropiado",
            "scam" to "Estafa o fraude",
            "fake" to "Cuenta falsa",
            "hate_speech" to "Discurso de odio",
            "other" to "Otro motivo"
        )
        
        AlertDialog(
            onDismissRequest = { showReportDialog = false; reportSent = false },
            containerColor = HomeBg,
            title = {
                Text(
                    if (reportSent) "Reporte enviado" else "Reportar a @${otherUser.username}",
                    fontWeight = FontWeight.Bold, color = TextPrimary
                )
            },
            text = {
                if (reportSent) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tu reporte ha sido recibido. Lo revisaremos pronto.", color = TextSecondary, lineHeight = 20.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    Column {
                        Text("Selecciona el motivo:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        reasons.forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedReason = key }
                                    .background(if (selectedReason == key) PrimaryPurple.copy(alpha = 0.12f) else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (selectedReason == key) Icons.Filled.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (selectedReason == key) PrimaryPurple else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(label, fontSize = 14.sp, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = reportDescription,
                            onValueChange = { reportDescription = it },
                            label = { Text("Descripción adicional (opcional)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = Surface,
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (reportSent) {
                    Button(
                        onClick = {
                            showReportDialog = false
                            reportSent = false
                            onDismiss()
                            onReportSent()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) { Text("Entendido") }
                } else {
                    Button(
                        onClick = {
                            if (selectedReason != null) {
                                scope.launch {
                                    val success = ChatRepository.reportUser(
                                        reportedUserId = otherUser.userId,
                                        reason = selectedReason!!,
                                        description = reportDescription.ifBlank { null }
                                    )
                                    if (success) reportSent = true
                                }
                            }
                        },
                        enabled = selectedReason != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            disabledContainerColor = Color(0xFFEF4444).copy(alpha = 0.3f)
                        )
                    ) { Text("Enviar reporte", color = Color.White) }
                }
            },
            dismissButton = {
                if (!reportSent) {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancelar", color = TextMuted)
                    }
                }
            }
        )
    }
    
    // ═══ Search in chat screen ═══
    if (showSearchScreen) {
        ChatSearchScreen(
            conversationId = conversationId,
            otherUsername = otherUser.username,
            onDismiss = { showSearchScreen = false },
            onMessageSelected = { messageId ->
                showSearchScreen = false
                onDismiss()
                onScrollToMessage(messageId)
            }
        )
    }
}

@Composable
private fun ChatSearchScreen(
    conversationId: String,
    otherUsername: String,
    onDismiss: () -> Unit,
    onMessageSelected: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    val currentUserId = remember { com.rendly.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    
    fun executeSearch() {
        if (searchQuery.isNotBlank()) {
            scope.launch {
                isSearching = true
                hasSearched = true
                searchResults = ChatRepository.searchMessages(conversationId, searchQuery)
                isSearching = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search header - compact 44dp matching ExploreScreen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, "Buscar", tint = TextMuted, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Buscar mensajes...", color = TextMuted, fontSize = 14.sp)
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { isFocused = it.isFocused },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = TextPrimary, fontSize = 14.sp
                                ),
                                singleLine = true,
                                cursorBrush = if (isFocused) SolidColor(PrimaryPurple) else SolidColor(Color.Transparent),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { executeSearch() })
                            )
                        }
                        AnimatedVisibility(
                            visible = searchQuery.isNotBlank(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            IconButton(
                                onClick = { executeSearch() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Search, "Buscar", tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Divider(color = Surface)
            
            // Results
            if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(32.dp))
                }
            } else if (hasSearched && searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SearchOff, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sin resultados", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("No se encontraron mensajes con \"$searchQuery\"", fontSize = 13.sp, color = TextMuted)
                    }
                }
            } else if (searchResults.isNotEmpty()) {
                Text(
                    "${searchResults.size} resultado${if (searchResults.size > 1) "s" else ""}",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(searchResults.size, key = { searchResults[it].id }) { index ->
                        val msg = searchResults[index]
                        val isFromMe = msg.senderId == currentUserId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMessageSelected(msg.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = Surface
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isFromMe) "Tú" else "@$otherUsername",
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = if (isFromMe) PrimaryPurple else AccentBlue
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = msg.createdAt.take(16).replace("T", " "),
                                        fontSize = 10.sp, color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(msg.content, fontSize = 14.sp, color = TextPrimary, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        
                    }
                }
            } else {
                // Initial state - no search yet
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Search, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Buscar en el chat", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Escribe y presiona buscar", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatSettingsOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextMuted)
        }
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
    }
}

/**
 * Componente visual para consultas y ofertas de productos en el chat
 * Diseño profesional con imagen del producto, título, precio y mensaje
 */
@Composable
private fun ConsultPostMessageContent(
    data: ConsultPostData,
    isFromMe: Boolean,
    timestamp: String = ""
) {
    val isOffer = data.type == "offer"
    val accentColor = if (isOffer) Color(0xFF22C55E) else Color(0xFF1565A0)
    val headerIcon = if (isOffer) Icons.Outlined.LocalOffer else Icons.Outlined.QuestionAnswer
    val headerText = if (isOffer) "Oferta" else "Consulta"
    
    // Extraer precio de oferta del mensaje si es oferta
    val offerPrice = if (isOffer) {
        val priceMatch = Regex("\\$([\\d,.]+)").find(data.message)
        priceMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    } else null
    
    // Extraer mensaje personalizado - limpiar emojis y prefijos
    val personalMessage = data.message
        .replace(Regex("📝 Mensaje:|💰.*?\\n|Oferta:.*?\\n"), "")
        .replace(Regex("^[\\s\\n]+|[\\s\\n]+$"), "")
        .trim()
        .ifEmpty { null }
    
    // Mensaje unificado sin contenedor padre
    Column(
        modifier = Modifier.width(260.dp)
    ) {
        // Header con tipo e icono (sin emojis)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = headerIcon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = headerText,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (isOffer && offerPrice != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$${String.format("%.2f", offerPrice)}",
                    color = accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Imagen del producto
        if (data.productImage.isNotEmpty()) {
            AsyncImage(
                model = data.productImage,
                contentDescription = data.productTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Título del producto
        Text(
            text = data.productTitle,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Precio original con descuento si aplica
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$${String.format("%.2f", data.productPrice)}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            if (isOffer && offerPrice != null && data.productPrice > 0) {
                val discount = ((data.productPrice - offerPrice) / data.productPrice * 100).toInt()
                if (discount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "-$discount%",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Mensaje personalizado directo (sin contenedor adicional)
        if (!personalMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = personalMessage,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        
        // Hora
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatMessageTimeWithAmPm(timestamp),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   MODAL PARA COMPARTIR USUARIO
═══════════════════════════════════════════════════════════════ */

@Composable
private fun ShareUserModal(
    isVisible: Boolean,
    currentUserId: String,
    onDismiss: () -> Unit,
    onShareUser: (Usuario) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var suggestedUsers by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isLoadingSuggestions by remember { mutableStateOf(true) }
    var selectedUser by remember { mutableStateOf<Usuario?>(null) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    
    // Reset state when modal opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            searchQuery = ""
            searchResults = emptyList()
            selectedUser = null
            isLoadingSuggestions = true
            // Cargar usuarios sugeridos (seguidos, interacciones)
            try {
                val followedIds = com.rendly.app.data.remote.SupabaseClient.database
                    .from("followers")
                    .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("followed_id")) {
                        filter { eq("follower_id", currentUserId) }
                    }
                    .decodeList<FollowedIdDB>()
                    .map { it.followedId }
                
                if (followedIds.isNotEmpty()) {
                    val users = com.rendly.app.data.remote.SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { isIn("user_id", followedIds) }
                        }
                        .decodeList<Usuario>()
                    suggestedUsers = users.take(12)
                }
            } catch (e: Exception) {
                android.util.Log.e("ShareUserModal", "Error cargando sugeridos: ${e.message}")
            }
            isLoadingSuggestions = false
        }
    }
    
    // Buscar usuarios cuando cambia la query
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            kotlinx.coroutines.delay(300) // Debounce
            isSearching = true
            try {
                val results = com.rendly.app.data.remote.SupabaseClient.database
                    .from("usuarios")
                    .select {
                        filter { 
                            ilike("username", "%$searchQuery%")
                            neq("user_id", currentUserId)
                        }
                    }
                    .decodeList<Usuario>()
                searchResults = results.take(16)
            } catch (e: Exception) {
                android.util.Log.e("ShareUserModal", "Error buscando: ${e.message}")
            }
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }
    
    // Backdrop
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }
    
    // Modal
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(0.dp),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
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
                    
                    // Header with close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Compartir cuenta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
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
                    
                    // Search bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it; selectedUser = null },
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(PrimaryPurple),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Buscar usuario...",
                                            color = TextMuted,
                                            fontSize = 15.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = ""; selectedUser = null }
                                )
                            }
                        }
                    }
                    
                    // Selected user confirmation
                    AnimatedVisibility(visible = selectedUser != null) {
                        selectedUser?.let { user ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = PrimaryPurple.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val avatarUrl = when {
                                        user.avatarUrl.isNullOrEmpty() -> "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff"
                                        user.avatarUrl.startsWith("http") -> user.avatarUrl
                                        else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${user.avatarUrl}"
                                    }
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "@${user.username}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        if (!user.nombreTienda.isNullOrBlank()) {
                                            Text(
                                                text = user.nombreTienda,
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    Surface(
                                        onClick = { onShareUser(user) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = PrimaryPurple
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Compartir",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Content
                    val usersToShow = if (searchQuery.length >= 2) searchResults else suggestedUsers
                    val isLoading = if (searchQuery.length >= 2) isSearching else isLoadingSuggestions
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryPurple,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    } else if (usersToShow.isEmpty()) {
                        // Empty state - creative design
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                // Creative icon stack
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        PrimaryPurple.copy(alpha = 0.15f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                CircleShape
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        PrimaryPurple.copy(alpha = 0.2f),
                                                        Color(0xFF6366F1).copy(alpha = 0.15f)
                                                    )
                                                ),
                                                RoundedCornerShape(20.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PersonSearch,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text(
                                    text = if (searchQuery.length >= 2) "Sin resultados" else "Descubre personas",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.length >= 2) 
                                        "No encontramos usuarios con \"$searchQuery\".\nIntenta con otro nombre."
                                    else
                                        "Seguí a otros usuarios para verlos aquí\no buscá por nombre de usuario.",
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    } else {
                        // Section header
                        Text(
                            text = if (searchQuery.length >= 2) "Resultados" else "Sugeridos",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
                        )
                        
                        // 4-column grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(usersToShow.size) { index ->
                                val user = usersToShow[index]
                                val isSelected = selectedUser?.userId == user.userId
                                ShareUserGridItem(
                                    user = user,
                                    isSelected = isSelected,
                                    onClick = { selectedUser = if (isSelected) null else user }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@kotlinx.serialization.Serializable
private data class FollowedIdDB(
    @kotlinx.serialization.SerialName("followed_id") val followedId: String = ""
)

@Composable
private fun ShareUserGridItem(
    user: Usuario,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val avatarUrl = remember(user.avatarUrl, user.username) {
        when {
            user.avatarUrl.isNullOrEmpty() -> "https://ui-avatars.com/api/?name=${user.username}&background=A78BFA&color=fff&size=128"
            user.avatarUrl.startsWith("http") -> user.avatarUrl
            else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${user.avatarUrl}"
        }
    }
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = avatarUrl,
                contentDescription = user.username,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.border(2.dp, PrimaryPurple, CircleShape)
                        else Modifier
                    )
            )
            if (user.isVerified) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .background(Color(0xFF1D9BF0), CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = user.username,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimaryPurple else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   SHARED USER MESSAGE CONTENT
═══════════════════════════════════════════════════════════════ */

@Composable
private fun SharedUserMessageContent(
    data: SharedUserData,
    isFromMe: Boolean,
    timestamp: String,
    messageStatus: MessageStatus = MessageStatus.SENT,
    onViewProfile: () -> Unit = {}
) {
    val timeColor = Color.White.copy(alpha = 0.6f)
    val otherTimeColor = Color(0xFF8B98A5)
    
    val resolvedAvatar = remember(data.avatarUrl, data.username) {
        when {
            data.avatarUrl.isEmpty() -> "https://ui-avatars.com/api/?name=${data.username}&background=A78BFA&color=fff&size=128"
            data.avatarUrl.startsWith("http") -> data.avatarUrl
            else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${data.avatarUrl}"
        }
    }
    
    val resolvedBanner = remember(data.bannerUrl) {
        when {
            data.bannerUrl.isEmpty() -> null
            data.bannerUrl.startsWith("http") -> data.bannerUrl
            else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/banners/${data.bannerUrl}"
        }
    }
    
    Box(modifier = Modifier.width(230.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Banner (real image or gradient fallback)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (resolvedBanner != null) {
                    AsyncImage(
                        model = resolvedBanner,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dark overlay for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        PrimaryPurple.copy(alpha = 0.5f),
                                        Color(0xFF6366F1).copy(alpha = 0.4f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                }
            }
            
            // Avatar left (overlapping banner) + Stats below banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 0.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar with VerifiedBadge - overlapping banner
                Box(
                    modifier = Modifier.offset(y = (-20).dp)
                ) {
                    AsyncImage(
                        model = resolvedAvatar,
                        contentDescription = data.username,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (isFromMe) Color(0xFF2D3748) else Color(0xFF1E2732),
                                CircleShape
                            )
                    )
                    if (data.isVerified) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                        ) {
                            VerifiedBadge(size = 16.dp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                // Stats row - más separación entre iconos
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Seguidores
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatSharedUserStat(data.followers),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Publicaciones
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.GridView,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatSharedUserStat(data.postsCount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Reputación
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${data.reputation.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Username (below avatar section)
            Text(
                text = "@${data.username}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, top = 0.dp)
                    .offset(y = (-12).dp)
            )
            
            // Botón "Ver perfil" - ancho completo
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .offset(y = (-6).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onViewProfile() },
                color = Color(0xFF0A3D62).copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ver perfil",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64B5F6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            
            // Hora y ticks - DEBAJO del botón Ver perfil (dentro del Column)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMessageTimeWithAmPm(timestamp),
                    color = if (isFromMe) timeColor else otherTimeColor,
                    fontSize = 10.sp
                )
                if (isFromMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    MessageStatusIcon(
                        status = messageStatus,
                        tintColor = if (messageStatus == MessageStatus.READ)
                            Color(0xFF53BDEB) else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

private fun formatSharedUserStat(count: Int): String {
    return when {
        count >= 1_000_000 -> "${String.format("%.1f", count / 1_000_000.0)}M"
        count >= 1_000 -> "${String.format("%.1f", count / 1_000.0)}K"
        else -> count.toString()
    }
}

/* ═══════════════════════════════════════════════════════════════
   ARTICLE CARD MESSAGE CONTENT - Datos dinámicos desde Supabase
═══════════════════════════════════════════════════════════════ */

private data class ArticleCardData(
    val postId: String,
    val title: String,
    val price: Double,
    val imageUrl: String,
    val status: String, // active, reserved, sold
    val ownerUsername: String,
    val ownerAvatar: String,
    val isDeleted: Boolean = false
)

@Composable
private fun ArticleCardMessageContent(
    postId: String,
    isFromMe: Boolean,
    timestamp: String,
    messageStatus: MessageStatus = MessageStatus.SENT,
    onOpenArticle: () -> Unit = {}
) {
    val timeColor = Color.White.copy(alpha = 0.6f)
    val otherTimeColor = Color(0xFF8B98A5)
    
    var articleData by remember { mutableStateOf<ArticleCardData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    
    // Fetch dinámico del artículo desde Supabase
    LaunchedEffect(postId) {
        isLoading = true
        isError = false
        try {
            val postDB = com.rendly.app.data.remote.SupabaseClient.database
                .from("posts")
                .select {
                    filter { eq("id", postId) }
                }
                .decodeSingleOrNull<com.rendly.app.data.model.PostDB>()
            
            if (postDB != null) {
                // Obtener datos del dueño
                var ownerUsername = "usuario"
                var ownerAvatar = ""
                try {
                    val owner = com.rendly.app.data.remote.SupabaseClient.database
                        .from("usuarios")
                        .select {
                            filter { eq("user_id", postDB.userId) }
                        }
                        .decodeSingleOrNull<com.rendly.app.data.model.Usuario>()
                    if (owner != null) {
                        ownerUsername = owner.username
                        ownerAvatar = owner.avatarUrl ?: ""
                    }
                } catch (_: Exception) {}
                
                articleData = ArticleCardData(
                    postId = postDB.id,
                    title = postDB.title,
                    price = postDB.price,
                    imageUrl = postDB.images.firstOrNull() ?: "",
                    status = postDB.status,
                    ownerUsername = ownerUsername,
                    ownerAvatar = ownerAvatar
                )
            } else {
                // Artículo eliminado
                articleData = ArticleCardData(
                    postId = postId,
                    title = "",
                    price = 0.0,
                    imageUrl = "",
                    status = "deleted",
                    ownerUsername = "",
                    ownerAvatar = "",
                    isDeleted = true
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ArticleCard", "Error fetching article: ${e.message}")
            isError = true
        }
        isLoading = false
    }
    
    Column(modifier = Modifier.width(230.dp)) {
        when {
            isLoading -> {
                // Skeleton loader premium
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF00BFA5),
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Cargando artículo...",
                            color = if (isFromMe) Color.White.copy(alpha = 0.7f) else Color(0xFF8B98A5),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            isError -> {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error al cargar artículo",
                            color = Color(0xFFEF5350),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            articleData?.isDeleted == true -> {
                // Artículo eliminado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color(0xFF455A64).copy(alpha = 0.3f),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RemoveShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFF78909C),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Artículo no disponible",
                            color = Color(0xFF78909C),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Este artículo fue eliminado",
                            color = Color(0xFF546E7A),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Hora
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = formatMessageTimeWithAmPm(timestamp),
                                color = if (isFromMe) timeColor else otherTimeColor,
                                fontSize = 10.sp
                            )
                            if (isFromMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                MessageStatusIcon(
                                    status = messageStatus,
                                    tintColor = if (messageStatus == MessageStatus.READ)
                                        Color(0xFF53BDEB) else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
            articleData != null -> {
                val data = articleData!!
                val statusColor = when (data.status) {
                    "active" -> Color(0xFF00E676)
                    "reserved" -> Color(0xFFFFAB00)
                    "sold" -> Color(0xFFEF5350)
                    else -> Color(0xFF78909C)
                }
                val statusText = when (data.status) {
                    "active" -> "Disponible"
                    "reserved" -> "Reservado"
                    "sold" -> "Vendido"
                    else -> data.status.replaceFirstChar { it.uppercase() }
                }
                
                // Imagen principal
                if (data.imageUrl.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    ) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(data.imageUrl)
                                .crossfade(true)
                                .memoryCacheKey("article_card_${data.postId}")
                                .build(),
                            contentDescription = data.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Overlay degradado inferior
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                        // Badge de estado en esquina superior derecha
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = statusText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        // Precio sobre la imagen (esquina inferior izquierda)
                        if (data.price > 0) {
                            Text(
                                text = "$${String.format("%.2f", data.price)}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 12.dp, bottom = 8.dp)
                            )
                        }
                    }
                } else {
                    // Sin imagen - placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF00BFA5).copy(alpha = 0.2f),
                                        Color(0xFF00897B).copy(alpha = 0.15f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF00BFA5).copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                // Info del artículo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Título
                    Text(
                        text = data.title.ifEmpty { "Sin título" },
                        color = if (isFromMe) Color.White else Color(0xFFE7E9EA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Vendedor + Ver artículo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Vendedor
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val resolvedAvatar = when {
                                data.ownerAvatar.isEmpty() -> "https://ui-avatars.com/api/?name=${data.ownerUsername}&background=00BFA5&color=fff&size=64"
                                data.ownerAvatar.startsWith("http") -> data.ownerAvatar
                                else -> "https://wsiszffxlxupzbrgrklv.supabase.co/storage/v1/object/public/avatars_new/${data.ownerAvatar}"
                            }
                            AsyncImage(
                                model = resolvedAvatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "@${data.ownerUsername}",
                                color = if (isFromMe) Color.White.copy(alpha = 0.7f) else Color(0xFF8B98A5),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón "Ver artículo"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenArticle() },
                        color = Color(0xFF00BFA5).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Ver artículo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00BFA5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Hora y ticks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTimeWithAmPm(timestamp),
                            color = if (isFromMe) timeColor else otherTimeColor,
                            fontSize = 10.sp
                        )
                        if (isFromMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            MessageStatusIcon(
                                status = messageStatus,
                                tintColor = if (messageStatus == MessageStatus.READ)
                                    Color(0xFF53BDEB) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
/* ═══════════════════════════════════════════════════════════════
   MODAL PARA COMPARTIR ARTÍCULO
═══════════════════════════════════════════════════════════════ */

@kotlinx.serialization.Serializable
private data class PostSaveDB(
    val id: String = "",
    @kotlinx.serialization.SerialName("post_id") val postId: String = ""
)

@Composable
private fun ShareArticleModal(
    isVisible: Boolean,
    currentUserId: String,
    otherUserId: String = "",
    otherUsername: String = "",
    onDismiss: () -> Unit,
    onSelectArticle: (Post) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0=Todos, 1=De @Username, 2=Guardados
    var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var otherUserPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var savedPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoadingAll by remember { mutableStateOf(true) }
    var isLoadingOther by remember { mutableStateOf(true) }
    var isLoadingSaved by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    
    // Collapsible filter tabs state
    val gridState = rememberLazyGridState()
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var previousFirstVisibleItem by remember { mutableIntStateOf(0) }
    var filterTabsVisible by remember { mutableStateOf(true) }
    
    // Track scroll direction for collapsible tabs
    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        val currentFirst = gridState.firstVisibleItemIndex
        val currentOffset = gridState.firstVisibleItemScrollOffset
        val scrollingDown = currentFirst > previousFirstVisibleItem || 
            (currentFirst == previousFirstVisibleItem && currentOffset > previousScrollOffset + 10)
        val scrollingUp = currentFirst < previousFirstVisibleItem || 
            (currentFirst == previousFirstVisibleItem && currentOffset < previousScrollOffset - 10)
        
        if (scrollingDown && filterTabsVisible) {
            filterTabsVisible = false
        } else if (scrollingUp && !filterTabsVisible) {
            filterTabsVisible = true
        }
        previousFirstVisibleItem = currentFirst
        previousScrollOffset = currentOffset
    }
    
    // Reset y cargar datos cuando abre
    LaunchedEffect(isVisible) {
        if (isVisible) {
            searchQuery = ""
            selectedFilter = 0
            filterTabsVisible = true
            
            // Cargar todos los artículos
            isLoadingAll = true
            try {
                allPosts = PostRepository.getPostsByCategory(limit = 50)
            } catch (e: Exception) {
                android.util.Log.e("ShareArticleModal", "Error cargando artículos: ${e.message}")
            }
            isLoadingAll = false
            
            // Cargar artículos del otro usuario
            isLoadingOther = true
            try {
                if (otherUserId.isNotEmpty()) {
                    val otherPostDBs = com.rendly.app.data.remote.SupabaseClient.database
                        .from("posts")
                        .select {
                            filter { eq("user_id", otherUserId); eq("status", "active") }
                            order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        }
                        .decodeList<com.rendly.app.data.model.PostDB>()
                    
                    // Fetch other user info for mapping
                    val otherUserInfo = try {
                        com.rendly.app.data.remote.SupabaseClient.database
                            .from("usuarios")
                            .select { filter { eq("user_id", otherUserId) } }
                            .decodeSingleOrNull<com.rendly.app.data.model.Usuario>()
                    } catch (_: Exception) { null }
                    
                    otherUserPosts = otherPostDBs.map { postDB ->
                        Post.fromDB(
                            postDB = postDB,
                            username = otherUserInfo?.username ?: otherUsername,
                            avatarUrl = otherUserInfo?.avatarUrl ?: "",
                            storeName = otherUserInfo?.nombreTienda,
                            isUserVerified = otherUserInfo?.isVerified ?: false
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ShareArticleModal", "Error cargando artículos del usuario: ${e.message}")
            }
            isLoadingOther = false
            
            // Cargar artículos guardados (misma lógica que ProfileScreen)
            isLoadingSaved = true
            try {
                val saves = com.rendly.app.data.remote.SupabaseClient.database
                    .from("post_saves")
                    .select { filter { eq("user_id", currentUserId) } }
                    .decodeList<PostSaveDB>()
                
                val savedPostIds = saves.map { it.postId }.toSet()
                
                if (savedPostIds.isNotEmpty()) {
                    val posts = com.rendly.app.data.remote.SupabaseClient.database
                        .from("posts")
                        .select()
                        .decodeList<com.rendly.app.data.model.PostDB>()
                        .filter { it.id in savedPostIds }
                    
                    val userIds = posts.map { it.userId }.distinct()
                    val usersMap = mutableMapOf<String, com.rendly.app.data.model.Usuario>()
                    
                    for (userId in userIds) {
                        try {
                            val user = com.rendly.app.data.remote.SupabaseClient.database
                                .from("usuarios")
                                .select { filter { eq("user_id", userId) } }
                                .decodeSingleOrNull<com.rendly.app.data.model.Usuario>()
                            if (user != null) usersMap[userId] = user
                        } catch (_: Exception) {}
                    }
                    
                    savedPosts = posts.map { post ->
                        val user = usersMap[post.userId]
                        Post(
                            id = post.id,
                            userId = post.userId,
                            title = post.title,
                            description = post.description,
                            price = post.price,
                            previousPrice = post.previousPrice,
                            category = post.category ?: "",
                            condition = post.condition ?: "",
                            images = post.images,
                            likesCount = post.likesCount,
                            reviewsCount = post.reviewsCount,
                            savesCount = post.savesCount,
                            sharesCount = post.sharesCount,
                            createdAt = post.createdAt,
                            username = user?.username ?: "usuario",
                            userAvatar = user?.avatarUrl ?: "",
                            userStoreName = user?.nombreTienda,
                            isSaved = true,
                            isUserVerified = user?.isVerified ?: false
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ShareArticleModal", "Error cargando guardados: ${e.message}")
            }
            isLoadingSaved = false
        }
    }
    
    // Filtrar por búsqueda
    val filteredPosts = remember(searchQuery, selectedFilter, allPosts, otherUserPosts, savedPosts) {
        val basePosts = when (selectedFilter) {
            1 -> otherUserPosts
            2 -> savedPosts
            else -> allPosts
        }
        if (searchQuery.length >= 2) {
            basePosts.filter { it.title.contains(searchQuery, ignoreCase = true) }
        } else {
            basePosts
        }
    }
    
    val isCurrentLoading = when (selectedFilter) {
        1 -> isLoadingOther
        2 -> isLoadingSaved
        else -> isLoadingAll
    }
    
    val filterLabels = listOf(
        "Todos",
        if (otherUsername.isNotEmpty()) "De @$otherUsername" else "Del usuario",
        "Guardados"
    )
    
    // Backdrop
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }
    
    // Modal
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 300f)
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(0.dp),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
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
                    
                    // Header with close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Compartir artículo",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
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
                    
                    // Search bar (always visible - tabs collapse behind it)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF00BFA5)),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Buscar artículo por nombre...",
                                            color = TextMuted,
                                            fontSize = 15.sp
                                        )
                                    }
                                    inner()
                                }
                            )
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                    
                    // Collapsible filter tabs
                    AnimatedVisibility(
                        visible = filterTabsVisible,
                        enter = expandVertically(animationSpec = tween(200)) + fadeIn(tween(200)),
                        exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(150))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterLabels.forEachIndexed { index, label ->
                                val isSelected = selectedFilter == index
                                Surface(
                                    onClick = { selectedFilter = index },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Color(0xFF00BFA5) else Surface
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    
                    // Content
                    if (isCurrentLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF00BFA5),
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    } else if (filteredPosts.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        Color(0xFF00BFA5).copy(alpha = 0.15f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                CircleShape
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF00BFA5).copy(alpha = 0.2f),
                                                        Color(0xFF00897B).copy(alpha = 0.15f)
                                                    )
                                                ),
                                                RoundedCornerShape(20.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Inventory2,
                                            contentDescription = null,
                                            tint = Color(0xFF00BFA5),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text(
                                    text = if (searchQuery.length >= 2) "Sin resultados" else "Sin artículos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.length >= 2)
                                        "No encontramos artículos con \"$searchQuery\".\nIntentá con otro nombre."
                                    else when (selectedFilter) {
                                        1 -> "@$otherUsername no tiene artículos publicados."
                                        2 -> "No tenés artículos guardados."
                                        else -> "No hay artículos disponibles."
                                    },
                                    fontSize = 14.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    } else {
                        // Grid de artículos - UnifiedProductCard con altura fija uniforme
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                count = filteredPosts.size,
                                key = { filteredPosts[it].id }
                            ) { index ->
                                val post = filteredPosts[index]
                                Box(modifier = Modifier.height(355.dp)) {
                                    UnifiedProductCard(
                                        data = post.toProductCardData(),
                                        onClick = { onSelectArticle(post) },
                                        imageHeight = 170.dp
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

@Composable
private fun ShareArticleGridItem(
    post: Post,
    onClick: () -> Unit
) {
    val imageUrl = remember(post.images) { post.images.firstOrNull() ?: "" }
    val formattedPrice = remember(post.price) {
        if (post.price > 0) "$${String.format("%,.0f", post.price)}" else ""
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Surface,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .memoryCacheKey("share_article_${post.id}")
                        .build(),
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f))
                            )
                        )
                )
                if (post.images.size > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "1/${post.images.size}",
                            fontSize = 9.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = post.title.ifEmpty { "Sin título" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    lineHeight = 17.sp,
                    overflow = TextOverflow.Ellipsis
                )
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "@${post.username.ifEmpty { "vendedor" }}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (post.isUserVerified) {
                            VerifiedBadge(size = 12.dp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (formattedPrice.isNotEmpty()) {
                        Text(
                            text = formattedPrice,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    if (post.price > 15000) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF00A650),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Envío gratis",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00A650)
                            )
                        }
                    }
                }
            }
        }
    }
}
