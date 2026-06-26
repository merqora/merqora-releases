package com.rendly.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.model.HandshakeTransaction
import com.rendly.app.data.repository.*
import com.rendly.app.ui.theme.*
import com.rendly.app.ui.components.MentionSuggestionPopup
import com.rendly.app.ui.components.extractMentionQuery
import com.rendly.app.ui.components.insertMention
import com.rendly.app.ui.components.ClientRequestMessageBubble
import com.rendly.app.ui.components.HandshakeActiveBanner
import com.rendly.app.ui.components.HandshakeBannerState
import com.rendly.app.ui.components.VerifiedBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.IntSize


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
    // Guard: evitar parpadeo/salto por reconciliaciÃ³n de mensajes tras carga inicial
    var settledAfterLoad by remember { mutableStateOf(false) }
    
    // Observar cuando la carga inicial de mensajes del servidor termina
    val initialLoadDone by ChatRepository.initialLoadDone.collectAsState()
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    
    // Estado para modal de opciones de mensaje
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var showMessageOptionsModal by remember { mutableStateOf(false) }
    
    // Estado para ediciÃ³n de mensaje
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    
    // Estado para respuesta a mensaje (swipe-to-reply)
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    
    // Estado para reenvÃ­o de posts compartidos
    var sharedPostToForward by remember { mutableStateOf<SharedPostData?>(null) }
    var showForwardSharedPostModal by remember { mutableStateOf(false) }
    
    // Estado para abrir RendScreen overlay al pulsar un video compartido
    var rendScreenRendId by remember { mutableStateOf<String?>(null) }
    var showRendScreenOverlay by remember { mutableStateOf(false) }
    
    // Estado para envÃ­o de media
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingMedia by remember { mutableStateOf(false) }
    
    // Estado para modal de handshake (confirmaciÃ³n de compra)
    var showHandshakeModal by remember { mutableStateOf(false) }
    var pendingHandshakeId by remember { mutableStateOf<String?>(null) }
    var isWaitingForAcceptance by remember { mutableStateOf(false) }
    
    // Estado para modal de ajustes del chat (3 puntitos)
    var showChatSettingsModal by remember { mutableStateOf(false) }
    
    // Chat wallpaper
    val wallpaperContext = LocalContext.current
    var chatWallpaper by remember { mutableStateOf<ChatWallpaper>(ChatWallpaper.load(wallpaperContext)) }
    
    // Estado para bÃºsqueda inline en el header
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
    var cancelledHandshakeInfo by remember { mutableStateOf<HandshakeTransaction?>(null) } // Para mantener referencia durante animaciÃ³n de cancelaciÃ³n
    var animateReputationBadge by remember { mutableStateOf(false) }
    var lastReputationChange by remember { mutableIntStateOf(0) } // +3, +4, -1, -5, etc.
    var showHandshakeBanner by remember { mutableStateOf(false) }
    var handshakeBannerState by remember { mutableStateOf(HandshakeBannerState.WAITING) }
    var completedBannerDismissed by remember { mutableStateOf(false) }
    
    // Handshake creado localmente (para mostrar banner antes de que Realtime sincronice)
    var localCreatedHandshake by remember { mutableStateOf<HandshakeTransaction?>(null) }
    
    // Tracking: si ya tuvimos un handshake activo (cachÃ© o red) para controlar cleanup
    var hadActiveHandshake by remember { mutableStateOf(false) }
    
    // Flag: la carga inicial del handshake desde red ya terminÃ³
    // Hasta que esto sea true, el LaunchedEffect(activeHandshake) NO debe interferir
    var isInitialHandshakeLoaded by remember { mutableStateOf(false) }
    
    // ReputaciÃ³n en tiempo real desde Supabase
    val otherUserReputation by ReputationRepository.otherUserReputation.collectAsState()
    val currentUserReputation by ReputationRepository.currentUserReputation.collectAsState()
    
    // VerificaciÃ³n en tiempo real del otro usuario
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
    
    // Estado para grabaciÃ³n de audio
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    
    // Estado para envÃ­o de ubicaciÃ³n
    var isGettingLocation by remember { mutableStateOf(false) }
    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }
    
    // FunciÃ³n para obtener y enviar ubicaciÃ³n
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
                        android.util.Log.e("ChatScreen", "No se pudo obtener ubicaciÃ³n")
                        isGettingLocation = false
                    }
                }.addOnFailureListener { e ->
                    android.util.Log.e("ChatScreen", "Error obteniendo ubicaciÃ³n: ${e.message}")
                    isGettingLocation = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Error obteniendo ubicaciÃ³n: ${e.message}")
                isGettingLocation = false
            }
        }
    }
    
    // Launcher para permiso de ubicaciÃ³n
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
            // Iniciar grabaciÃ³n
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
                android.util.Log.e("ChatScreen", "Error al iniciar grabaciÃ³n: ${e.message}")
            }
        }
    }
    
    // Estado para image preview modal (estilo WhatsApp)
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePreviewModal by remember { mutableStateOf(false) }
    var imageCaption by remember { mutableStateOf("") }
    
    // Estado para fullscreen gallery (al pulsar imagen en chat)
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var showFullscreenImage by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar imagen/video de galerÃ­a â†’ abre preview modal
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingImageUri = it
            imageCaption = ""
            showImagePreviewModal = true
        }
    }
    
    // Estado para modal de compartir usuario
    var showShareUserModal by remember { mutableStateOf(false) }
    
    // Estado para modal de compartir artÃ­culo
    var showShareArticleModal by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar archivo (OpenDocument para mÃ¡xima compatibilidad)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingMedia = true
                try {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        // Obtener nombre y tamaÃ±o del archivo
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
                        // Subir archivo directamente a ImageKit (sin crear mensaje [IMG])
                        val inputStream = contentResolver.openInputStream(selectedUri)
                        if (inputStream != null) {
                            val fileBytes = inputStream.use { it.readBytes() }
                            val uploadResult = com.rendly.app.data.remote.ImageKitService.uploadImageBytes(
                                fileBytes, folder = "chat_files"
                            )
                            if (uploadResult.isSuccess) {
                                val fileUrl = uploadResult.getOrNull() ?: ""
                                // Enviar SOLO como [FILE] con metadata
                                val fileMsg = "[FILE]{\"url\":\"$fileUrl\",\"name\":\"$fileName\",\"size\":$fileSize}"
                                ChatRepository.sendMessage(convId, fileMsg)
                            } else {
                                android.util.Log.e("ChatScreen", "Error subiendo archivo: ${uploadResult.exceptionOrNull()?.message}")
                            }
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
    
    // â•â•â• CLEANUP: Guardar handshake en cachÃ© al salir del chat â•â•â•
    DisposableEffect(Unit) {
        onDispose {
            val convId = currentConversationId
            if (convId != null) {
                // Guardar handshake activo en cachÃ© para restaurarlo al volver
                scope.launch {
                    HandshakeRepository.suspendForConversation(convId)
                }
            }
        }
    }
    
    // Verificar si el otro usuario estÃ¡ bloqueado
    LaunchedEffect(Unit) {
        isOtherUserBlocked = ChatRepository.isUserBlocked(otherUser.userId)
    }
    
    // Abrir chat: suscribir a realtime ANTES de cargar mensajes
    // Usar Unit como key para que SIEMPRE se ejecute al entrar a la pantalla
    LaunchedEffect(Unit) {
        try {
            // Buscar o crear conversaciÃ³n existente
            val existingConvId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
            if (existingConvId != null) {
                currentConversationId = existingConvId
                
                // â•â•â• RESTAURAR HANDSHAKE DESDE CACHÃ‰ INSTANTÃNEAMENTE â•â•â•
                // Antes de cualquier carga de red, restaurar el handshake cacheado
                // para que el banner aparezca sin delay al volver al chat
                val cachedHs = HandshakeRepository.getCachedHandshake(existingConvId)
                if (cachedHs != null) {
                    android.util.Log.d("ChatScreen", ">>> CACHE HIT: Restaurando handshake id=${cachedHs.id} status=${cachedHs.status}")
                    // Mostrar banner instantÃ¡neamente desde cachÃ©
                    when (cachedHs.status) {
                        "PROPOSED" -> {
                            if (cachedHs.initiatorId == currentUserId) {
                                showHandshakeBanner = true
                                handshakeBannerState = HandshakeBannerState.WAITING
                            }
                        }
                        "ACCEPTED", "IN_PROGRESS" -> {
                            showHandshakeBanner = true
                            handshakeBannerState = HandshakeBannerState.ACCEPTED
                        }
                        "RENEGOTIATING" -> {
                            showHandshakeBanner = true
                            handshakeBannerState = HandshakeBannerState.WAITING
                        }
                    }
                    // Marcar que tuvimos handshake desde cachÃ© para que el cleanup funcione
                    hadActiveHandshake = true
                }
                
                // Usar openChat que suscribe primero y luego carga
                val displayName = otherUser.nombreTienda ?: otherUser.nombre ?: otherUser.username
                ChatRepository.openChat(existingConvId, displayName)
                
                // Suscribirse a handshakes en tiempo real (NON-BLOCKING)
                // No bloquear el hilo principal esperando websocket + subscribe
                launch {
                    try {
                        HandshakeRepository.subscribeToHandshakes(currentUserId ?: "")
                    } catch (e: Exception) {
                        android.util.Log.e("ChatScreen", "Error subscribing to handshakes: ${e.message}")
                    }
                }
                
                // Suscribirse a cambios de reputaciÃ³n y verificaciÃ³n (non-blocking)
                launch { ReputationRepository.subscribeToReputation(currentUserId ?: "", otherUser.userId) }
                launch { VerificationRepository.subscribeToVerification(otherUser.userId) }
                
                // ALWAYS fetch fresh handshake from DB (independent of subscription)
                // This runs immediately, doesn't wait for Realtime setup
                try {
                    val freshHs = HandshakeRepository.getActiveHandshakeForConversation(existingConvId)
                    android.util.Log.d("ChatScreen", ">>> FRESH from network: id=${freshHs?.id} status=${freshHs?.status}")
                    if (freshHs != null) {
                        when (freshHs.status) {
                            "PROPOSED" -> {
                                if (freshHs.initiatorId == currentUserId) {
                                    showHandshakeBanner = true
                                    handshakeBannerState = HandshakeBannerState.WAITING
                                } else {
                                    showHandshakeBanner = false
                                }
                            }
                            "ACCEPTED", "IN_PROGRESS" -> {
                                showHandshakeBanner = true
                                handshakeBannerState = HandshakeBannerState.ACCEPTED
                            }
                            "RENEGOTIATING" -> {
                                showHandshakeBanner = true
                                handshakeBannerState = HandshakeBannerState.WAITING
                            }
                            "COMPLETED", "CANCELLED", "REJECTED" -> {
                                showHandshakeBanner = false
                            }
                        }
                        hadActiveHandshake = true
                    } else if (cachedHs != null) {
                        showHandshakeBanner = false
                        HandshakeRepository.clearCacheForConversation(existingConvId)
                    }
                    
                    // â•â•â• RECONCILIAR MENSAJES DE HANDSHAKE â•â•â•
                    // Buscar el handshake mÃ¡s reciente (incluyendo COMPLETED/CANCELLED)
                    // y verificar que sus mensajes de estado estÃ©n en el chat.
                    // PequeÃ±o delay para asegurar que loadMessages() haya propagado al StateFlow
                    kotlinx.coroutines.delay(200)
                    val msgCount = ChatRepository.currentMessages.value.size
                    android.util.Log.d("ChatScreen", ">>> Before reconcile: $msgCount messages loaded")
                    HandshakeRepository.reconcileHandshakeMessages(existingConvId)
                    android.util.Log.d("ChatScreen", ">>> Handshake messages reconciled for conv=$existingConvId")
                    
                    // Si el handshake mÃ¡s reciente estÃ¡ COMPLETED y no hay handshake activo,
                    // verificar si necesitamos mostrar el banner brevemente
                    if (freshHs == null) {
                        val latestHs = HandshakeRepository.getLatestHandshakeForConversation(existingConvId)
                        if (latestHs != null && latestHs.status == "COMPLETED" && !completedBannerDismissed) {
                            android.util.Log.d("ChatScreen", ">>> Found COMPLETED handshake on re-entry: ${latestHs.id}")
                            completedHandshakeInfo = latestHs
                            showHandshakeBanner = true
                            handshakeBannerState = HandshakeBannerState.COMPLETED
                            hadActiveHandshake = true
                            
                            // Auto-dismiss after showing
                            launch {
                                delay(4000)
                                showHandshakeBanner = false
                                completedBannerDismissed = true
                                completedHandshakeInfo = null
                            }
                        }
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("ChatScreen", "Error fetching fresh handshake: ${e.message}")
                }
                // ALWAYS mark initial load complete so Realtime + polling can update the banner
                isInitialHandshakeLoaded = true
                
            } else {
                android.util.Log.e("ChatScreen", "No se pudo obtener conversaciÃ³n: ${ChatRepository.lastError.value}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatScreen", "Exception al abrir chat: ${e.message}")
        } finally {
            // Safety net: always allow Realtime + polling to update the banner
            isInitialHandshakeLoaded = true
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
    // Este es el ÃšNICO LaunchedEffect que maneja cambios DESPUÃ‰S de la carga inicial
    LaunchedEffect(activeHandshake, isInitialHandshakeLoaded) {
        // NO procesar nada hasta que la carga inicial desde red haya terminado
        // Esto elimina la race condition donde activeHandshake=null llega antes que la red
        if (!isInitialHandshakeLoaded) return@LaunchedEffect
        
        val handshake = activeHandshake
        android.util.Log.d("ChatScreen", ">>> REALTIME: activeHandshake changed -> status=${handshake?.status} id=${handshake?.id}")
        
        if (handshake == null) {
            if (hadActiveHandshake && handshakeBannerState != HandshakeBannerState.CANCELLED) {
                showHandshakeBanner = false
                isWaitingForAcceptance = false
            }
            return@LaunchedEffect
        }
        hadActiveHandshake = true
        
        // Solo procesar si es de esta conversaciÃ³n
        if (handshake.conversationId != currentConversationId) return@LaunchedEffect
        
        // Si estamos mostrando el banner CANCELLED, NO interferir con su animaciÃ³n
        if (handshakeBannerState == HandshakeBannerState.CANCELLED && handshake.status == "CANCELLED") {
            android.util.Log.d("ChatScreen", ">>> Ignorando update CANCELLED - banner ya en animaciÃ³n")
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
                    // Mensaje de chat ya se envÃ­a automÃ¡ticamente desde HandshakeRepository
                    
                    // Incrementar reputaciÃ³n
                    val change = ReputationRepository.incrementReputation()
                    lastReputationChange = change
                    android.util.Log.d("ChatScreen", ">>> ReputaciÃ³n incrementada: +$change%")
                    
                    delay(100)
                    delay(3500)
                    
                    // Cerrar banner automÃ¡ticamente
                    showHandshakeBanner = false
                    completedBannerDismissed = true
                    lastReputationChange = 0
                    completedHandshakeInfo = null
                    android.util.Log.d("ChatScreen", ">>> Banner COMPLETED cerrado")
                }
            }
            "REJECTED", "CANCELLED" -> {
                // Solo ocultar si NO estamos mostrando el banner CANCELLED con animaciÃ³n
                if (handshakeBannerState != HandshakeBannerState.CANCELLED) {
                    android.util.Log.d("ChatScreen", ">>> Handshake rechazado/cancelado - ocultando banner")
                    isWaitingForAcceptance = false
                    pendingHandshakeId = null
                    showHandshakeBanner = false
                }
            }
        }
    }
    
    // *** POLLING FALLBACK: Verificar estado del handshake periÃ³dicamente ***
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
    
    // Scroll INSTANTÃNEO al fondo cuando la carga inicial del servidor termina
    // Esperar a initialLoadDone para evitar doble-scroll (cache + server)
    LaunchedEffect(initialLoadDone) {
        if (initialLoadDone && messages.isNotEmpty() && !initialScrollDone) {
            // Carga del servidor terminÃ³: scroll instantÃ¡neo al fondo SIN animaciÃ³n
            listState.scrollToItem(messages.size - 1)
            // Marcar como listo DESPUÃ‰S del scroll para hacer visible la lista
            initialScrollDone = true
            messagesReady = true
        }
    }
    
    // Safety net: si initialLoadDone llegÃ³ antes de que messages se propagara,
    // esperar a que messages tenga contenido
    LaunchedEffect(messages.size) {
        if (initialLoadDone && messages.isNotEmpty() && !initialScrollDone) {
            listState.scrollToItem(messages.size - 1)
            initialScrollDone = true
            messagesReady = true
        }
    }
    
    // Guard: tras el scroll inicial, esperar a que se estabilicen las reconciliaciones
    // para evitar parpadeo/salto visible causado por mensajes aÃ±adidos post-carga
    LaunchedEffect(messagesReady) {
        if (messagesReady) {
            delay(4000)
            settledAfterLoad = true
        }
    }
    
    // Scroll animado para mensajes nuevos en tiempo real (SOLO despuÃ©s del scroll inicial)
    var lastMessageCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(messages.size, initialScrollDone) {
        if (initialScrollDone && messagesReady && messages.isNotEmpty()) {
            // Solo reaccionar a mensajes NUEVOS (agregados al final), no a prepends
            if (messages.size > lastMessageCount && lastMessageCount > 0) {
                // Durante el perÃ­odo de estabilizaciÃ³n, IGNORAR cambios de mensajes
                // (reconciliaciÃ³n, handshakes, etc.) para evitar parpadeo/salto visible
                if (settledAfterLoad) {
                    val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val isNearBottom = lastVisibleIndex >= lastMessageCount - 3
                    if (isNearBottom) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
                // Si NO settled: no hacer NADA, ignorar el cambio silenciosamente
            }
            lastMessageCount = messages.size
        }
    }
    
    // Detectar scroll hacia arriba para cargar mÃ¡s mensajes
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    
    // Guardar el conteo previo para detectar cuando se agregan mensajes antiguos
    var previousMessageCount by remember { mutableStateOf(0) }
    var wasLoadingMore by remember { mutableStateOf(false) }
    
    // Trigger de carga cuando llegamos arriba - SOLO despuÃ©s del scroll inicial
    LaunchedEffect(firstVisibleItemIndex) {
        if (!initialScrollDone || !messagesReady) return@LaunchedEffect
        // Cargar mÃ¡s cuando estamos en los primeros 2 items y hay mÃ¡s por cargar
        if (firstVisibleItemIndex <= 2 && hasMoreMessages && !isLoadingMoreFromRepo && messages.isNotEmpty()) {
            previousMessageCount = messages.size
            wasLoadingMore = true
            ChatRepository.loadMoreMessages()
        }
    }
    
    // Mantener posiciÃ³n del scroll cuando se cargan mensajes antiguos (prepend)
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
            .background(Brush.verticalGradient(chatWallpaper.colors.map { Color(it) }))
    ) {
        // Imagen de fondo personalizada (Cloudinary)
        if (chatWallpaper.isImage && chatWallpaper.imageUrl != null) {
            AsyncImage(
                model = chatWallpaper.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.4f
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    // Solo aplicar imePadding cuando el modal de handshake NO estÃ¡ abierto
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
            
            // Banner dinÃ¡mico de handshake - maneja todos los estados: WAITING, ACCEPTED, COMPLETED
            // Excluir COMPLETED si el usuario ya cerrÃ³ el banner
            val shouldShowCompletedBanner = activeHandshake?.status == "COMPLETED" && !completedBannerDismissed
            // Usar activeHandshake si existe, sino usar el handshake creado localmente o el cancelado
            val effectiveHandshake = activeHandshake ?: localCreatedHandshake ?: cancelledHandshakeInfo ?: completedHandshakeInfo
            
            // AnimaciÃ³n fluida de entrada/salida del banner
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
                        // Usar el estado real del handshake para decidir quÃ© hacer
                        val isInWaitingState = activeHandshake?.status == "PROPOSED" || 
                                               handshakeBannerState == HandshakeBannerState.WAITING
                        
                        android.util.Log.d("ChatScreen", ">>> onCancel CALLED")
                        android.util.Log.d("ChatScreen", ">>> activeHandshake?.status = ${activeHandshake?.status}")
                        android.util.Log.d("ChatScreen", ">>> handshakeBannerState = $handshakeBannerState")
                        android.util.Log.d("ChatScreen", ">>> isInWaitingState = $isInWaitingState")
                        android.util.Log.d("ChatScreen", ">>> activeHandshake?.id = ${activeHandshake?.id}")
                        android.util.Log.d("ChatScreen", ">>> pendingHandshakeId = $pendingHandshakeId")
                        
                        if (isInWaitingState) {
                            // Cancelar propuesta pendiente - penalizaciÃ³n -2%
                            val idToCancel = activeHandshake?.id ?: pendingHandshakeId
                            android.util.Log.d("ChatScreen", ">>> WAITING state - idToCancel = $idToCancel")
                            
                            // IMPORTANTE: Guardar referencia al handshake ANTES de cancelar
                            // para que el banner pueda mostrarlo durante la animaciÃ³n
                            cancelledHandshakeInfo = activeHandshake
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
                                        // Mensaje de chat se envÃ­a automÃ¡ticamente desde HandshakeRepository
                                        // PenalizaciÃ³n -2% por cancelar en estado WAITING
                                        android.util.Log.d("ChatScreen", ">>> Aplicando penalizaciÃ³n -2% por cancelar en WAITING")
                                        ReputationRepository.decrementReputation(2)
                                        
                                        // Esperar a que se vea la animaciÃ³n y luego cerrar banner
                                        delay(2500)
                                        showHandshakeBanner = false
                                        lastReputationChange = 0
                                        cancelledHandshakeInfo = null
                                        android.util.Log.d("ChatScreen", ">>> Banner CANCELLED cerrado")
                                    }
                                }
                            }
                        } else {
                            // Mostrar modal de confirmaciÃ³n para cancelar transacciÃ³n aceptada
                            android.util.Log.d("ChatScreen", ">>> ACCEPTED state - showing confirmation modal")
                            showCancelConfirmModal = true
                        }
                    },
                    onDismiss = {
                        // Cerrar banner manualmente (el usuario hizo clic en X)
                        // La animaciÃ³n de reputaciÃ³n ya se maneja en el LaunchedEffect de COMPLETED
                        completedBannerDismissed = true
                        showHandshakeBanner = false
                    }
                )
            }
        
        // Contenedor de mensajes - Oculto hasta que estÃ©n listos y posicionados
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
                // â•â•â• Swipe-to-reply gesture â•â•â•
                var swipeOffset by remember { mutableFloatStateOf(0f) }
                val animatedSwipeOffset by animateFloatAsState(
                    targetValue = swipeOffset,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                    label = "swipe"
                )
                val haptic = LocalHapticFeedback.current
                var hasVibrated by remember { mutableStateOf(false) }
                
                val isSpecialMsg = message.content.startsWith("[HANDSHAKE") ||
                    ChatRepository.isClientRequestMessage(message.content) ||
                    ChatRepository.isClientAcceptedMessage(message.content) ||
                    ChatRepository.isClientRejectedMessage(message.content)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!isSpecialMsg) Modifier.pointerInput(message.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (swipeOffset > 72f) replyingToMessage = message
                                        swipeOffset = 0f
                                        hasVibrated = false
                                    },
                                    onDragCancel = { swipeOffset = 0f; hasVibrated = false },
                                    onHorizontalDrag = { change, dragAmount ->
                                        val newOffset = (swipeOffset + dragAmount).coerceIn(0f, 96f)
                                        swipeOffset = newOffset
                                        if (newOffset > 72f && !hasVibrated) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            hasVibrated = true
                                        }
                                        if (newOffset > 0f) change.consume()
                                    }
                                )
                            } else Modifier
                        )
                ) {
                    // Reply arrow revealed behind message
                    if (animatedSwipeOffset > 0f) {
                        val progress = (animatedSwipeOffset / 72f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                                .alpha(progress)
                                .graphicsLayer {
                                    scaleX = progress.coerceAtLeast(0.5f)
                                    scaleY = progress.coerceAtLeast(0.5f)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        if (progress >= 1f) PrimaryPurple.copy(alpha = 0.3f)
                                        else PrimaryPurple.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SubdirectoryArrowLeft,
                                    contentDescription = "Responder",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    Box(modifier = Modifier.offset { IntOffset(animatedSwipeOffset.toInt(), 0) }) {
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
                        onReject = { reason: String ->
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
                        senderUsername = if (message.isFromMe) 
                            (ProfileRepository.currentProfile.value?.username ?: "") 
                        else 
                            (otherUser.username ?: ""),
                        senderAvatar = if (message.isFromMe) 
                            (ProfileRepository.currentProfile.value?.avatarUrl ?: "") 
                        else 
                            (otherUser.avatarUrl ?: ""),
                        senderIsVerified = if (message.isFromMe) 
                            (ProfileRepository.currentProfile.value?.isVerified == true) 
                        else 
                            otherUser.isVerified,
                        onLongPress = { msg: Message ->
                            selectedMessage = msg
                            showMessageOptionsModal = true
                        },
                        onForwardSharedPost = { postData: SharedPostData ->
                            sharedPostToForward = postData
                            showForwardSharedPostModal = true
                        },
                        onSharedPostClick = { postId: String ->
                            onOpenProduct?.invoke(postId)
                        },
                        onSharedRendClick = { rendId: String ->
                            rendScreenRendId = rendId
                            showRendScreenOverlay = true
                        },
                        onNavigateToUserProfile = onNavigateToUserProfile,
                        onImageClick = { imageUrl: String ->
                            fullscreenImageUrl = imageUrl
                            showFullscreenImage = true
                        }
                    )
                }
                    } // close inner Box (offset)
                } // close outer Box (swipe container)
            }
            
            // Mostrar burbuja de "Escribiendo..." cuando el otro usuario estÃ¡ escribiendo
            if (isOtherUserTyping) {
                item {
                    TypingIndicatorBubble(
                        userAvatar = otherUser.avatarUrl
                    )
                }
            }
        }
        
        // â•â•â• Banner de usuario bloqueado O input normal â•â•â•
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
        // Indicador de modo ediciÃ³n
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
                            contentDescription = "Cancelar ediciÃ³n",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        // â•â•â• Reply banner (swipe-to-reply) â•â•â•
        AnimatedVisibility(
            visible = replyingToMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF00BFA5).copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(34.dp)
                            .background(Color(0xFF00BFA5), RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (replyingToMessage?.isFromMe == true) "TÃº" else (otherUser.username ?: ""),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BFA5)
                        )
                        val replyPreview = when {
                            replyingToMessage?.content?.startsWith("[IMG]") == true -> "ðŸ“· Foto"
                            replyingToMessage?.content?.startsWith("[VIDEO]") == true -> "ðŸŽ¬ Video"
                            replyingToMessage?.content?.startsWith("[AUDIO]") == true -> "ðŸŽµ Audio"
                            replyingToMessage?.content?.startsWith("[LOCATION]") == true -> "ðŸ“ UbicaciÃ³n"
                            replyingToMessage?.content?.startsWith("[FILE]") == true -> "ðŸ“Ž Archivo"
                            replyingToMessage?.content?.startsWith("[SHARED_POST]") == true -> "ðŸ“¦ PublicaciÃ³n"
                            replyingToMessage?.content?.startsWith("[SHARED_USER]") == true -> "ðŸ‘¤ Perfil"
                            replyingToMessage?.content?.startsWith("[SHARED_REND]") == true -> "ðŸŽ¬ Rend"
                            replyingToMessage?.content?.startsWith("[ARTICLE_CARD]") == true -> "ðŸ“¦ ArtÃ­culo"
                            replyingToMessage?.content?.startsWith("[REPLY]") == true -> {
                                try {
                                    val j = org.json.JSONObject(replyingToMessage!!.content.removePrefix("[REPLY]"))
                                    j.optString("text", "Mensaje")
                                } catch (_: Exception) { "Mensaje" }
                            }
                            else -> replyingToMessage?.content?.take(80) ?: ""
                        }
                        Text(
                            text = replyPreview,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { replyingToMessage = null },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar respuesta",
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
                    
                    // Modo ediciÃ³n - actualizar mensaje existente
                    if (isEditMode && editingMessage != null) {
                        val msgToEdit = editingMessage!!
                        scope.launch {
                            ChatRepository.updateMessage(msgToEdit.id, textToSend)
                            isEditMode = false
                            editingMessage = null
                        }
                    } else {
                        // EnvÃ­o normal o respuesta
                        scope.launch {
                            val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                            if (convId != null) {
                                currentConversationId = convId
                                
                                if (replyingToMessage != null) {
                                    ChatRepository.sendReply(convId, textToSend, replyingToMessage!!)
                                    replyingToMessage = null
                                } else {
                                    ChatRepository.sendMessage(convId, textToSend)
                                }
                            }
                        }
                    }
                }
            },
            onAttach = { showAttachmentMenu = !showAttachmentMenu },
            onEmoji = { showEmojiPicker = !showEmojiPicker },
            onMicStart = { /* AnimaciÃ³n mic ya incluida */ },
            onMicEnd = { file: File ->
                scope.launch {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        ChatRepository.uploadAndSendMedia(context, convId, Uri.fromFile(file))
                    }
                }
            },
            isRecording = isRecording,
            isUploadingMedia = isUploadingMedia
        )
        }
    }
    
    // Attachment Menu
    if (showAttachmentMenu) {
        AttachmentMenu(
            onDismiss = { showAttachmentMenu = false },
            onCamera = {
                showAttachmentMenu = false
                // TODO: Abrir cÃ¡mara personalizada
            },
            onGallery = {
                showAttachmentMenu = false
                mediaPickerLauncher.launch("image/* video/*")
            },
            onFile = {
                showAttachmentMenu = false
                filePickerLauncher.launch(arrayOf("*/*"))
            },
            onLocation = {
                showAttachmentMenu = false
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fetchAndSendLocation()
                } else {
                    locationPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
            },
            onContact = {
                showAttachmentMenu = false
                showShareUserModal = true
            },
            onArticle = {
                showAttachmentMenu = false
                showShareArticleModal = true
            }
        )
    }
    
    // Handshake Proposals & Modals
    if (showProposalModal && currentProposal != null) {
        ProposalConfirmationModal(
            proposal = currentProposal!!,
            onDismiss = { showProposalModal = false },
            onAccept = {
                scope.launch {
                    HandshakeRepository.acceptHandshake(currentProposal!!.id ?: "")
                    showProposalModal = false
                }
            },
            onReject = {
                scope.launch {
                    HandshakeRepository.rejectHandshake(currentProposal!!.id ?: "")
                    showProposalModal = false
                }
            }
        )
    }
    
    if (showHandshakeModal) {
        HandshakeConfirmationModal(
            isVisible = showHandshakeModal,
            otherUser = otherUser,
            onDismiss = { showHandshakeModal = false },
            onConfirm = { desc: String, price: Double ->
                scope.launch {
                    val convId = currentConversationId ?: ChatRepository.getOrCreateConversation(otherUser.userId)
                    if (convId != null) {
                        currentConversationId = convId
                        val result = HandshakeRepository.createHandshake(
                            convId, currentUserId ?: "", otherUser.userId, desc, price
                        )
                        if (result != null) {
                            localCreatedHandshake = result
                            pendingHandshakeId = result.id
                            isWaitingForAcceptance = true
                            showHandshakeBanner = true
                            handshakeBannerState = HandshakeBannerState.WAITING
                        }
                    }
                    showHandshakeModal = false
                }
            }
        )
    }
}
}
@Composable
private fun MessageBubble(
    message: Message,
    otherUserAvatar: String?,
    currentUserId: String?,
    senderUsername: String,
    senderAvatar: String,
    senderIsVerified: Boolean,
    onLongPress: (Message) -> Unit,
    onForwardSharedPost: ((SharedPostData) -> Unit)? = null,
    onSharedPostClick: ((String) -> Unit)? = null,
    onSharedRendClick: ((String) -> Unit)? = null,
    onNavigateToUserProfile: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null
) {
    val isFromMe = message.isFromMe
    val myBubbleColor = Color(0xFF005C4B)
    val otherBubbleColor = Color(0xFF202C33)
    val myMessageText = Color.White
    val otherMessageText = Color.White
    val timeColor = Color.White.copy(alpha = 0.6f)
    val otherTimeColor = Color.White.copy(alpha = 0.6f)
    
    val content = message.content
    val isImage = content.startsWith("[IMG]")
    val isVideo = content.startsWith("[VIDEO]")
    val isAudio = content.startsWith("[AUDIO]")
    val isLocation = content.startsWith("[LOCATION]")
    val isSharedPost = content.startsWith("[SHARED_POST]")
    val isSharedUser = content.startsWith("[SHARED_USER]")
    val isSharedRend = content.startsWith("[SHARED_REND]")
    val isArticleCard = content.startsWith("[ARTICLE_CARD]")
    val isHandshake = content.startsWith("[HANDSHAKE")
    val isReply = content.startsWith("[REPLY]")
    val isFile = content.startsWith("[FILE]")
    
    var mediaUrl: String? = null
    var imageCaption = ""
    var locationData: Pair<Double, Double>? = null
    var sharedPost: SharedPostData? = null
    var sharedUser: SharedUserData? = null
    var consultData: ConsultPostData? = null
    var handshakeStatus: String? = null
    var handshakePrice: Double? = null
    var replyData: Message? = null
    var fileData: org.json.JSONObject? = null
    
    try {
        when {
            isImage -> {
                val raw = content.removePrefix("[IMG]")
                if (raw.startsWith("{")) {
                    val j = org.json.JSONObject(raw)
                    mediaUrl = j.getString("url")
                    imageCaption = j.optString("caption", "")
                } else {
                    mediaUrl = raw
                }
            }
            isVideo -> mediaUrl = content.removePrefix("[VIDEO]")
            isAudio -> mediaUrl = content.removePrefix("[AUDIO]")
            isLocation -> {
                val coords = content.removePrefix("[LOCATION]").split(",")
                if (coords.size == 2) {
                    locationData = Pair(coords[0].toDouble(), coords[1].toDouble())
                }
            }
            isHandshake -> {
                val j = org.json.JSONObject(content.removePrefix("[HANDSHAKE]"))
                handshakeStatus = j.getString("status")
                handshakePrice = j.optDouble("price", 0.0)
            }
            isFile -> {
                fileData = org.json.JSONObject(content.removePrefix("[FILE]"))
            }
            isReply -> {
                val j = org.json.JSONObject(content.removePrefix("[REPLY]"))
                replyData = Message(
                    id = j.getString("replyId"),
                    conversationId = message.conversationId,
                    senderId = j.getString("senderId"),
                    content = j.getString("text"),
                    createdAt = "",
                    isRead = true,
                    isFromMe = j.getString("senderId") == currentUserId,
                    status = MessageStatus.SENT,
                    reactions = emptyMap()
                )
            }
        }
    } catch (_: Exception) {}

    val hasReactions = message.reactions.isNotEmpty()
    val reactionsOnRight = isFromMe

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress(message) }
                )
            },
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f).align(if (isFromMe) Alignment.End else Alignment.Start)
        ) {
            if (!isFromMe) {
                AsyncImage(
                    model = senderAvatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToUserProfile?.invoke(message.senderId) },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromMe) 16.dp else 4.dp,
                    bottomEnd = if (isFromMe) 4.dp else 16.dp
                ),
                color = if (isFromMe) myBubbleColor else otherBubbleColor,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(if (isImage || isVideo || isLocation) 0.dp else 8.dp)) {
                    when {
                        isReply && replyData != null -> {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp)) {
                                        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(PrimaryPurple, RoundedCornerShape(2.dp)))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (replyData!!.isFromMe) "Tu" else senderUsername,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryPurple
                                            )
                                            Text(
                                                text = replyData!!.content.take(60),
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = try { org.json.JSONObject(content.removePrefix("[REPLY]")).getString("text") } catch(_:Exception) { "" },
                                    color = Color.White,
                                    fontSize = 15.sp
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
                        isLocation && locationData != null -> {
                            val (lat, lng) = locationData
                            val locationContext = LocalContext.current
                            val latStr = String.format(java.util.Locale.US, "%.6f", lat)
                            val lngStr = String.format(java.util.Locale.US, "%.6f", lng)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(Color(0xFF0D1117))
                                    .clickable {
                                        try {
                                            val gmmUri = android.net.Uri.parse("geo:$latStr,$lngStr?q=$latStr,$lngStr")
                                            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmUri)
                                            mapIntent.setPackage("com.google.android.apps.maps")
                                            locationContext.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            val webUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$latStr,$lngStr")
                                            locationContext.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, webUri))
                                        }
                                    }
                            ) {
                                val mapHtml = remember(latStr, lngStr) {
                                    """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                        <meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>
                                        <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>
                                        <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>
                                        <style>
                                            body, html, #m { margin: 0; padding: 0; width: 100%; height: 100%; background: #0D1117; }
                                            .leaflet-tile-pane { filter: brightness(0.6) invert(1) contrast(3) hue-rotate(200deg) brightness(0.3) invert(1); }
                                            .leaflet-container { background: #0D1117 !important; outline: 0; }
                                        </style>
                                    </head>
                                    <body>
                                        <div id='m'></div>
                                        <script>
                                            var m = L.map('m', {
                                                zoomControl: false, 
                                                attributionControl: false, 
                                                dragging: false, 
                                                scrollWheelZoom: false, 
                                                doubleClickZoom: false, 
                                                touchZoom: false
                                            }).setView([$latStr, $lngStr], 15);
                                            L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                                                maxZoom: 20
                                            }).addTo(m);
                                            var icon = L.icon({
                                                iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
                                                shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
                                                iconSize: [25, 41],
                                                iconAnchor: [12, 41]
                                            });
                                            L.marker([$latStr, $lngStr], {icon: icon}).addTo(m);
                                            window.onload = function() {
                                                setTimeout(function(){ m.invalidateSize(); }, 500);
                                            };
                                        </script>
                                    </body>
                                    </html>
                                    """.trimIndent()
                                }
                                
                                AndroidView(
                                    factory = { ctx ->
                                        android.webkit.WebView(ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.domStorageEnabled = true
                                            settings.cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                                            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                            isVerticalScrollBarEnabled = false
                                            isHorizontalScrollBarEnabled = false
                                            setOnTouchListener { _, _ -> true }
                                            loadDataWithBaseURL("https://unpkg.com/", mapHtml, "text/html", "UTF-8", null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                var showLoader by remember { mutableStateOf(true) }
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(2000)
                                    showLoader = false
                                }
                                
                                if (showLoader) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryPurple, strokeWidth = 2.dp)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (message.isFromMe) Color.Transparent else Color(0xFF161B22).copy(alpha = 0.5f))
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
                                    text = "Ubicacion compartida",
                                    color = if (message.isFromMe) Color.White else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Outlined.OpenInNew,
                                    contentDescription = "Abrir en mapa",
                                    tint = if (message.isFromMe) Color.White.copy(alpha = 0.7f) else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        (isImage || isVideo) && mediaUrl != null -> {
                            val hasCaption = isImage && imageCaption.isNotEmpty()
                            Column(modifier = Modifier.width(220.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val stableCacheKey = remember(message.conversationId, message.createdAt) {
                                        "chat_img_${message.conversationId}_${message.createdAt}"
                                    }
                                    AsyncImage(
                                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                                            .data(mediaUrl)
                                            .crossfade(false)
                                            .memoryCacheKey(stableCacheKey)
                                            .placeholderMemoryCacheKey(stableCacheKey)
                                            .build(),
                                        contentDescription = if (isVideo) "Video" else "Imagen",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (senderUsername.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .align(Alignment.TopCenter)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Black.copy(alpha = 0.55f),
                                                            Color.Transparent
                                                        )
                                                    )
                                                )
                                        )
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = senderUsername,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (senderIsVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                VerifiedBadge(size = 14.dp)
                                            }
                                        }
                                    }
                                    if (isVideo) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .align(Alignment.Center)
                                                .background(Color.White.copy(alpha = 0.85f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                                if (hasCaption) {
                                    Text(
                                        text = imageCaption,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                        isFile && fileData != null -> {
                            val fileName = fileData.optString("name", "archivo")
                            val fileSize = fileData.optLong("size", 0L)
                            val fileUrl = fileData.optString("url", "")
                            
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        android.util.Log.d("ChatScreen", "Abriendo archivo: $fileUrl")
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.InsertDriveFile, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = fileName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = formatFileSize(fileSize), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = content,
                                color = if (isFromMe) myMessageText else otherMessageText,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp, end = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTimeWithAmPm(message.createdAt),
                            color = if (isFromMe) timeColor else otherTimeColor,
                            fontSize = 10.sp
                        )
                        if (isFromMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            MessageStatusIcon(status = message.status, color = timeColor)
                        }
                    }
                }
            }
            if (isFromMe) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
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

// Helpers para formato
private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}

private fun formatMessageTimeWithAmPm(timestamp: String): String {
    // Basic implementation or stub
    return timestamp.split("T").lastOrNull()?.take(5) ?: ""
}

// STUBS FOR MISSING COMPONENTS AND MODELS

data class ChatWallpaper(
    val colors: List<Long> = listOf(0xFF1C1C1C, 0xFF0D0D0D),
    val isImage: Boolean = false,
    val imageUrl: String? = null
) {
    companion object {
        fun load(context: android.content.Context): ChatWallpaper = ChatWallpaper()
    }
}

data class SharedPostData(val id: String)
data class SharedUserData(val id: String)
data class ConsultPostData(val id: String)

@Composable
fun ChatSearchHeader(
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1F2C33),
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Cerrar b\u00fasqueda",
                        tint = Color.White
                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    placeholder = {
                        Text(
                            text = "Buscar mensajes...",
                            color = TextMuted,
                            fontSize = 15.sp
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 15.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { onSearch() }
                    ),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                )
                if (resultCount > 0) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = currentIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Anterior",
                            tint = if (currentIndex > 0) Color.White else TextMuted
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        enabled = currentIndex < resultCount - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Siguiente",
                            tint = if (currentIndex < resultCount - 1) Color.White else TextMuted
                        )
                    }
                }
            }
            if (resultCount > 0) {
                Text(
                    text = "${currentIndex + 1} de $resultCount",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
                )
            } else if (query.isNotEmpty() && !isSearching) {
                Text(
                    text = "No se encontraron mensajes",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ChatHeader(
    user: Usuario,
    isOnline: Boolean,
    isTyping: Boolean,
    otherUserReputation: Double,
    isVerified: Boolean,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onMore: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1F2C33),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.3f))
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = (user.username ?: "?").take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .background(Color(0xFF25D366), CircleShape)
                            .border(2.dp, Color(0xFF1F2C33), CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username ?: "Usuario",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(size = 16.dp)
                    }
                }
                Text(
                    text = when {
                        isTyping -> "Escribiendo..."
                        isOnline -> "En l\u00ednea"
                        else -> ""
                    },
                    color = if (isTyping) Color(0xFF00BFA5) else TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            IconButton(onClick = onCall) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Llamar",
                    tint = Color.White
                )
            }
            IconButton(onClick = onMore) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "M\u00e1s opciones",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(userAvatar: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier
            .padding(start = 48.dp, bottom = 4.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(Color(0xFF202C33))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 300),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(TextMuted, CircleShape)
                )
                if (index < 2) Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun AudioMessagePlayer(
    audioUrl: String,
    isFromMe: Boolean,
    accentColor: Color,
    timeColor: Color,
    senderAvatar: String?,
    sentTime: String
) {
    var isPlaying by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .padding(8.dp)
            .width(200.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = if (isFromMe) Color.White else Color(0xFF1565A0),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isFromMe) Color.White.copy(alpha = 0.3f) else Color(0xFF1565A0).copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = if (isPlaying) 0.6f else 0f)
                    .background(if (isFromMe) Color.White else Color(0xFF1565A0), RoundedCornerShape(2.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "0:15",
            color = if (isFromMe) Color.White.copy(alpha = 0.7f) else timeColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun MessageStatusIcon(status: MessageStatus, color: Color) {
    val icon = when (status) {
        MessageStatus.SENT -> Icons.Default.Check
        MessageStatus.DELIVERED -> Icons.Default.DoneAll
        MessageStatus.READ -> Icons.Default.DoneAll
        else -> Icons.Default.Check
    }
    val tint = when (status) {
        MessageStatus.READ -> Color(0xFF53BDEB)
        MessageStatus.DELIVERED -> color
        else -> color.copy(alpha = 0.7f)
    }
    Icon(
        imageVector = icon,
        contentDescription = status.name.lowercase(),
        tint = tint,
        modifier = Modifier.size(14.dp)
    )
}

@Composable
fun ChatInputV2(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    onEmoji: () -> Unit,
    onMicStart: () -> Unit,
    onMicEnd: (File) -> Unit,
    isRecording: Boolean,
    isUploadingMedia: Boolean
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1F2C33),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAttach,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adjuntar",
                    tint = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2A3942))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 24.dp, max = 120.dp),
                        placeholder = {
                            Text(
                                text = "Mensaje",
                                color = TextMuted,
                                fontSize = 15.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.White,
                            fontSize = 15.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White
                        ),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSend = { if (text.isNotBlank()) onSend() }
                        )
                    )
                    if (text.isNotBlank()) {
                        IconButton(
                            onClick = onEmoji,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEmotions,
                                contentDescription = "Emoji",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            if (text.isNotBlank()) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF00A884), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (isRecording) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Grabando",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onMicStart,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microfono",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentMenu(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onFile: () -> Unit,
    onLocation: () -> Unit,
    onContact: () -> Unit,
    onArticle: () -> Unit
) {}

@Composable
fun ProposalConfirmationModal(
    proposal: HandshakeTransaction,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {}

@Composable
fun HandshakeConfirmationModal(
    isVisible: Boolean,
    otherUser: Usuario,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {}

