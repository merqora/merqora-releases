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
    // Guard: evitar parpadeo/salto por reconciliación de mensajes tras carga inicial
    var settledAfterLoad by remember { mutableStateOf(false) }
    
    // Observar cuando la carga inicial de mensajes del servidor termina
    val initialLoadDone by ChatRepository.initialLoadDone.collectAsState()
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    
    // Estado para modal de opciones de mensaje
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var showMessageOptionsModal by remember { mutableStateOf(false) }
    
    // Estado para edición de mensaje
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    
    // Estado para respuesta a mensaje (swipe-to-reply)
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    
    // Estado para reenvío de posts compartidos
    var sharedPostToForward by remember { mutableStateOf<SharedPostData?>(null) }
    var showForwardSharedPostModal by remember { mutableStateOf(false) }
    
    // Estado para abrir RendScreen overlay al pulsar un video compartido
    var rendScreenRendId by remember { mutableStateOf<String?>(null) }
    var showRendScreenOverlay by remember { mutableStateOf(false) }
    
    // Estado para envío de media
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingMedia by remember { mutableStateOf(false) }
    
    // Estado para modal de handshake (confirmación de compra)
    var showHandshakeModal by remember { mutableStateOf(false) }
    var pendingHandshakeId by remember { mutableStateOf<String?>(null) }
    var isWaitingForAcceptance by remember { mutableStateOf(false) }
    
    // Estado para modal de ajustes del chat (3 puntitos)
    var showChatSettingsModal by remember { mutableStateOf(false) }
    
    // Chat wallpaper
    val wallpaperContext = LocalContext.current
    var chatWallpaper by remember { mutableStateOf(ChatWallpaper.load(wallpaperContext)) }
    
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
    
    // Tracking: si ya tuvimos un handshake activo (caché o red) para controlar cleanup
    var hadActiveHandshake by remember { mutableStateOf(false) }
    
    // Flag: la carga inicial del handshake desde red ya terminó
    // Hasta que esto sea true, el LaunchedEffect(activeHandshake) NO debe interferir
    var isInitialHandshakeLoaded by remember { mutableStateOf(false) }
    
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
