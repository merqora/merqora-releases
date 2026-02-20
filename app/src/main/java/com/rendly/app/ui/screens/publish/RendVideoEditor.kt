package com.rendly.app.ui.screens.publish

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.LruCache
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * REND VIDEO EDITOR - Editor de Video Profesional estilo Instagram EDITS
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Características:
 * - Timeline horizontal con scrubbing ultra fluido
 * - Playhead central fijo con desplazamiento del timeline
 * - Capas: video, audio, texto, efectos
 * - Preview en tiempo real
 * - Gestos nativos (drag, pinch, swipe)
 * - Animaciones suaves 60fps
 * - GPU accelerated rendering
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * ARQUITECTURA DE CAPAS - TimelineItem (Preparación para edición profesional)
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Modelo de datos escalable para soportar:
 * - Clips cortables
 * - Overlays reales
 * - Texto animado
 * - Efectos con duración
 */
enum class TimelineItemType {
    VIDEO, AUDIO, TEXT, EFFECT, IMAGE, STICKER
}

data class TimelineItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val layerId: String,
    val startMs: Long,
    val endMs: Long,
    val type: TimelineItemType,
    val data: Any? = null,
    val isSelected: Boolean = false,
    val originalDurationMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L
)

data class VideoClip(
    val id: String = java.util.UUID.randomUUID().toString(),
    val uri: Uri,
    val startMs: Long = 0,
    val endMs: Long = 0,
    val durationMs: Long = 0,
    val thumbnails: List<Bitmap> = emptyList()
)

/**
 * Texto overlay persistente sobre el video
 */
data class VideoTextOverlay(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val fontSize: Float = 24f,
    val color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    val fontFamily: androidx.compose.ui.text.font.FontFamily = androidx.compose.ui.text.font.FontFamily.Default,
    val fontWeight: androidx.compose.ui.text.font.FontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
    val startMs: Long = 0L,
    val endMs: Long = Long.MAX_VALUE
)

/**
 * Herramientas contextuales por tipo de item
 */
private val VIDEO_CONTEXT_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("split", Icons.Outlined.ContentCut, "Dividir"),
    EditorTool("duplicate", Icons.Outlined.ContentCopy, "Duplicar"),
    EditorTool("speed", Icons.Outlined.Speed, "Velocidad"),
    EditorTool("volume", Icons.Outlined.VolumeUp, "Volumen"),
    EditorTool("extract", Icons.Outlined.GraphicEq, "Extraer"),
    EditorTool("filters", Icons.Outlined.FilterVintage, "Filtros"),
    EditorTool("adjust", Icons.Outlined.Tune, "Ajustar"),
    EditorTool("delete", Icons.Outlined.Delete, "Eliminar")
)

private val AUDIO_CONTEXT_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("split", Icons.Outlined.ContentCut, "Dividir"),
    EditorTool("trim", Icons.Outlined.Crop, "Recortar"),
    EditorTool("duplicate", Icons.Outlined.ContentCopy, "Duplicar"),
    EditorTool("volume", Icons.Outlined.VolumeUp, "Volumen"),
    EditorTool("fade", Icons.Outlined.GraphicEq, "Fade"),
    EditorTool("delete", Icons.Outlined.Delete, "Eliminar")
)

private val TEXT_CONTEXT_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("edit", Icons.Outlined.Edit, "Editar"),
    EditorTool("style", Icons.Outlined.FormatColorText, "Estilo"),
    EditorTool("animation", Icons.Outlined.Animation, "Animar"),
    EditorTool("duplicate", Icons.Outlined.ContentCopy, "Duplicar"),
    EditorTool("delete", Icons.Outlined.Delete, "Eliminar")
)

private val EFFECT_CONTEXT_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("intensity", Icons.Outlined.Tune, "Intensidad"),
    EditorTool("duration", Icons.Outlined.Timer, "Duración"),
    EditorTool("duplicate", Icons.Outlined.ContentCopy, "Duplicar"),
    EditorTool("delete", Icons.Outlined.Delete, "Eliminar")
)

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * THUMBNAIL CACHE - LRU Cache para optimización de memoria
 * ═══════════════════════════════════════════════════════════════════════════════
 */
object ThumbnailCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // 1/8 de la memoria disponible
    
    val cache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
    
    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bitmap: Bitmap) = cache.put(key, bitmap)
    fun clear() = cache.evictAll()
}

data class TimelineLayer(
    val id: String,
    val type: LayerType,
    val name: String,
    val color: Color,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false
)

enum class LayerType {
    VIDEO, AUDIO, TEXT, EFFECT
}

data class EditorTool(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val color: Color = Color.White
)

private val EDITOR_TOOLS = listOf(
    EditorTool("audio", Icons.Outlined.MusicNote, "Audio"),
    EditorTool("text", Icons.Outlined.TextFields, "Texto"),
    EditorTool("voice", Icons.Outlined.Mic, "Voz"),
    EditorTool("stickers", Icons.Outlined.Face, "Stickers"),
    EditorTool("captions", Icons.Outlined.ClosedCaption, "Subtítulos"),
    EditorTool("adjust", Icons.Outlined.Tune, "Ajustar"),
    EditorTool("filters", Icons.Outlined.FilterVintage, "Filtros"),
    EditorTool("effects", Icons.Outlined.AutoAwesome, "Efectos")
)

// ═══════════════════════════════════════════════════════════════════════════════
// HERRAMIENTAS CONTEXTUALES PARA CADA TOOL PRINCIPAL
// ═══════════════════════════════════════════════════════════════════════════════

// Herramientas para AUDIO (al pulsar Audio en toolbar principal)
private val AUDIO_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("music", Icons.Outlined.LibraryMusic, "Música"),
    EditorTool("sounds", Icons.Outlined.SurroundSound, "Sonidos"),
    EditorTool("voiceover", Icons.Outlined.RecordVoiceOver, "Voz en off")
)

// Herramientas para TEXTO
private val TEXT_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("add_text", Icons.Outlined.Add, "Añadir"),
    EditorTool("templates", Icons.Outlined.TextSnippet, "Plantillas"),
    EditorTool("styles", Icons.Outlined.FormatColorText, "Estilos")
)

// Herramientas para VOZ
private val VOICE_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("record", Icons.Outlined.Mic, "Grabar"),
    EditorTool("effects", Icons.Outlined.AutoAwesome, "Efectos"),
    EditorTool("denoise", Icons.Outlined.NoiseAware, "Reducir ruido")
)

// Herramientas para STICKERS
private val STICKER_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("gif", Icons.Outlined.Gif, "GIFs"),
    EditorTool("emoji", Icons.Outlined.EmojiEmotions, "Emojis"),
    EditorTool("shapes", Icons.Outlined.Category, "Formas")
)

// Herramientas para SUBTÍTULOS
private val CAPTIONS_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("auto", Icons.Outlined.AutoFixHigh, "Auto"),
    EditorTool("manual", Icons.Outlined.Edit, "Manual"),
    EditorTool("import", Icons.Outlined.Upload, "Importar"),
    EditorTool("style", Icons.Outlined.FormatColorText, "Estilo")
)

// Herramientas para AJUSTAR - Nivel Instagram/Lightroom
private val ADJUST_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("brightness", Icons.Outlined.WbSunny, "Brillo"),
    EditorTool("contrast", Icons.Outlined.Contrast, "Contraste"),
    EditorTool("saturation", Icons.Outlined.Palette, "Saturación"),
    EditorTool("exposure", Icons.Outlined.Exposure, "Exposición"),
    EditorTool("highlights", Icons.Outlined.LightMode, "Luces"),
    EditorTool("shadows", Icons.Outlined.DarkMode, "Sombras"),
    EditorTool("temperature", Icons.Outlined.Thermostat, "Temperatura"),
    EditorTool("sharpen", Icons.Outlined.Deblur, "Nitidez"),
    EditorTool("vignette", Icons.Outlined.Vignette, "Viñeta")
)

// Herramientas para FILTROS - Presets profesionales
private val FILTER_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("none", Icons.Outlined.Block, "Original"),
    EditorTool("vivid", Icons.Outlined.AutoAwesome, "Vívido"),
    EditorTool("warm", Icons.Outlined.WbSunny, "Cálido"),
    EditorTool("cool", Icons.Outlined.AcUnit, "Frío"),
    EditorTool("bw", Icons.Outlined.FilterBAndW, "B&N"),
    EditorTool("vintage", Icons.Outlined.FilterVintage, "Vintage"),
    EditorTool("dramatic", Icons.Outlined.Thunderstorm, "Dramático"),
    EditorTool("fade", Icons.Outlined.Gradient, "Fade")
)

// Herramientas para EFECTOS - Transiciones y overlays
private val EFFECTS_TOOLS = listOf(
    EditorTool("back", Icons.Default.ArrowBack, ""),
    EditorTool("transitions", Icons.Outlined.SwapHoriz, "Transiciones"),
    EditorTool("blur", Icons.Outlined.BlurOn, "Desenfocar"),
    EditorTool("glitch", Icons.Outlined.BrokenImage, "Glitch"),
    EditorTool("zoom", Icons.Outlined.ZoomIn, "Zoom"),
    EditorTool("shake", Icons.Outlined.Vibration, "Shake"),
    EditorTool("overlay", Icons.Outlined.Layers, "Overlay"),
    EditorTool("speed", Icons.Outlined.Speed, "Velocidad")
)

/**
 * Modos de herramientas activas
 */
enum class EditorToolMode {
    MAIN,      // Herramientas principales
    AUDIO,
    TEXT,
    VOICE,
    STICKERS,
    CAPTIONS,
    ADJUST,
    FILTERS,
    EFFECTS,
    CONTEXT    // Herramientas contextuales de timeline item
}

@OptIn(ExperimentalFoundationApi::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun RendVideoEditor(
    videoUri: Uri,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Estados del editor
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var videoDurationMs by remember { mutableLongStateOf(0L) }
    var projectName by remember { mutableStateOf("Nuevo proyecto") }
    
    // Estados del timeline
    var timelineZoom by remember { mutableFloatStateOf(1f) }
    var selectedLayerIndex by remember { mutableIntStateOf(1) } // Video layer by default
    var selectedToolId by remember { mutableStateOf<String?>(null) }
    
    // ═══ MODO DE HERRAMIENTAS ═══
    // Controla qué set de herramientas se muestra en el toolbar
    var toolMode by remember { mutableStateOf(EditorToolMode.MAIN) }
    
    // ═══ ESTADO DE MODALES ═══
    var showTextModal by remember { mutableStateOf(false) }
    var showFiltersModal by remember { mutableStateOf(false) }
    var showEffectsModal by remember { mutableStateOf(false) }
    var showAdjustModal by remember { mutableStateOf(false) }
    
    // ═══ ESTADO DE TEXTO OVERLAY ═══
    // Lista de textos persistentes sobre el video
    var textOverlays by remember { mutableStateOf(listOf<VideoTextOverlay>()) }
    // Estado temporal para edición
    var editingTextState by remember { mutableStateOf(com.rendly.app.ui.components.StoryTextState()) }
    
    // ═══ ESTADO PARA OCULTAR HEADER ═══
    val isAnyModalOpen = showTextModal || showFiltersModal || showEffectsModal || showAdjustModal
    
    // ═══ ESTADO DE AJUSTES DE VIDEO ═══
    var showAdjustPanel by remember { mutableStateOf(false) }
    var selectedAdjustTool by remember { mutableStateOf<String?>(null) }
    var adjustValues by remember { mutableStateOf(mapOf(
        "brightness" to 0f,
        "contrast" to 0f,
        "saturation" to 0f,
        "exposure" to 0f,
        "highlights" to 0f,
        "shadows" to 0f,
        "temperature" to 0f,
        "sharpen" to 0f,
        "vignette" to 0f
    )) }
    
    // ═══ ESTADO DE FILTROS ═══
    var selectedFilter by remember { mutableStateOf(com.rendly.app.ui.components.STORY_FILTERS.first()) }
    var selectedEffect by remember { mutableStateOf("none") }
    
    // ═══ TIMELINE SCROLL INTELIGENTE (Anti "peleas") ═══
    // Flag para detectar cuando el usuario está interactuando manualmente
    var isUserScrubbing by remember { mutableStateOf(false) }
    // Flag para distinguir scroll programático vs manual
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    
    // ═══ ITEM SELECCIONADO EN TIMELINE ═══
    // Cuando se pulsa un item, mostramos herramientas contextuales
    var selectedTimelineItem by remember { mutableStateOf<TimelineItem?>(null) }
    
    // ═══ MODAL DEL BOTÓN (+) PARA AGREGAR CLIPS ═══
    var showAddClipMenu by remember { mutableStateOf(false) }
    
    // ═══ ESTADOS DE HERRAMIENTAS DE EDICIÓN ═══
    var showSpeedModal by remember { mutableStateOf(false) }
    var showVolumeModal by remember { mutableStateOf(false) }
    var videoSpeed by remember { mutableFloatStateOf(1f) }
    var videoVolume by remember { mutableFloatStateOf(1f) }
    
    // Items del timeline (SOLO video principal - sin datos Mock)
    val timelineItems = remember(videoDurationMs) {
        mutableStateListOf(
            TimelineItem(
                id = "main_video",
                layerId = "video",
                startMs = 0L,
                endMs = videoDurationMs,
                type = TimelineItemType.VIDEO,
                data = videoUri,
                originalDurationMs = videoDurationMs,
                trimStartMs = 0L,
                trimEndMs = videoDurationMs
            )
        )
    }
    
    // Thumbnails del video
    var videoThumbnails by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoadingThumbnails by remember { mutableStateOf(true) }
    
    // Capas del timeline - solo mostramos video al inicio
    // Las demás capas se agregan dinámicamente cuando el usuario las usa
    val layers = remember(timelineItems.toList()) {
        val hasAudio = timelineItems.any { it.type == TimelineItemType.AUDIO }
        val hasEffects = timelineItems.any { it.type == TimelineItemType.EFFECT }
        
        buildList {
            if (hasEffects) add(TimelineLayer("effects", LayerType.EFFECT, "Efectos", AccentGold))
            add(TimelineLayer("video", LayerType.VIDEO, "Video", PrimaryPurple))
            if (hasAudio) add(TimelineLayer("audio", LayerType.AUDIO, "Audio", AccentPink))
        }
    }
    
    // ExoPlayer para reproducción
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }
    
    // ═════════════════════════════════════════════════════════════════════
    // SINCRONIZACIÓN DE PLAYBACK ESCALABLE (Player.Listener)
    // Arquitectura preparada para múltiples clips, layers y ediciones
    // ═════════════════════════════════════════════════════════════════════
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    videoDurationMs = exoPlayer.duration
                }
                // Cuando termina, resetear
                if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    currentPositionMs = 0
                }
            }
            
            override fun onIsPlayingChanged(playing: Boolean) {
                // Mantener estado sincronizado con el player real
                if (isPlaying != playing) {
                    isPlaying = playing
                }
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                // Capturar seeks externos o cambios de posición
                currentPositionMs = newPosition.positionMs
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }
    
    // Actualizar posición durante reproducción (loop de 60fps)
    // Solo actualiza si NO estamos haciendo scrubbing manual
    LaunchedEffect(isPlaying, isUserScrubbing) {
        while (isPlaying && !isUserScrubbing) {
            currentPositionMs = exoPlayer.currentPosition
            if (currentPositionMs >= videoDurationMs && videoDurationMs > 0) {
                isPlaying = false
                exoPlayer.pause()
                exoPlayer.seekTo(0)
                currentPositionMs = 0
            }
            delay(16) // ~60fps - mantener UI fluida
        }
    }
    
    // Sincronizar play/pause con el player
    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }
    
    // ═════════════════════════════════════════════════════════════════════
    // OPTIMIZACIÓN DE THUMBNAILS CON LRU CACHE
    // Cero OOM, scrolling fluido, estable en gama media/baja
    // ═════════════════════════════════════════════════════════════════════
    val videoUriKey = videoUri.toString()
    LaunchedEffect(videoUri) {
        withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, videoUri)
                
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L
                videoDurationMs = duration
                
                // Generar thumbnails con downscaling agresivo (80x120 para RAM)
                val thumbnailCount = 10
                val interval = if (duration > 0) duration / thumbnailCount else 1000L
                val thumbnails = mutableListOf<Bitmap>()
                
                for (i in 0 until thumbnailCount) {
                    val cacheKey = "${videoUriKey}_thumb_$i"
                    
                    // Intentar obtener del cache primero
                    var cachedBitmap = ThumbnailCache.get(cacheKey)
                    
                    if (cachedBitmap == null) {
                        val timeUs = (i * interval * 1000)
                        val frame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            retriever.getScaledFrameAtTime(
                                timeUs,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                                80, 120 // Downscaling agresivo para memoria
                            )
                        } else {
                            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        }
                        frame?.let {
                            // Guardar en cache
                            ThumbnailCache.put(cacheKey, it)
                            cachedBitmap = it
                        }
                    }
                    
                    cachedBitmap?.let { thumbnails.add(it) }
                }
                
                retriever.release()
                videoThumbnails = thumbnails
                isLoadingThumbnails = false
            } catch (e: Exception) {
                isLoadingThumbnails = false
            }
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0F))
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ═══════════════════════════════════════════════════════════════
            // HEADER - Animado, se mantiene visible pero vacío en modales
            // ═══════════════════════════════════════════════════════════════
            val headerHeight by animateDpAsState(
                targetValue = if (isAnyModalOpen && !showTextModal) 0.dp else 52.dp,
                animationSpec = tween(250),
                label = "headerHeight"
            )
            
            AnimatedVisibility(
                visible = !isAnyModalOpen || showTextModal,
                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showTextModal) {
                        // Header vacío para modo texto - el botón "Listo" está en StoryTextEditor
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        // Header normal del editor
                        IconButton(
                            onClick = {
                                exoPlayer.pause()
                                onBack()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = projectName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = {
                                exoPlayer.pause()
                                onNext()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Siguiente",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // VIDEO PREVIEW - Con textos overlay y filtros aplicados
            // ═══════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = if (isAnyModalOpen) 16.dp else 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Video preview con filtros y ajustes aplicados en tiempo real
                VideoPreview(
                    exoPlayer = exoPlayer,
                    isPlaying = isPlaying,
                    onTogglePlay = { isPlaying = !isPlaying },
                    adjustValues = adjustValues,
                    filterColorMatrix = selectedFilter.colorMatrix
                )
                
                // ═══ TEXTOS OVERLAY PERSISTENTES ═══
                textOverlays.forEach { textOverlay ->
                    // Solo mostrar si está en el rango de tiempo actual
                    if (currentPositionMs in textOverlay.startMs..textOverlay.endMs) {
                        var isDragging by remember { mutableStateOf(false) }
                        
                        Text(
                            text = textOverlay.text,
                            color = textOverlay.color,
                            fontSize = textOverlay.fontSize.sp,
                            fontFamily = textOverlay.fontFamily,
                            fontWeight = textOverlay.fontWeight,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .graphicsLayer {
                                    translationX = textOverlay.offsetX
                                    translationY = textOverlay.offsetY
                                    rotationZ = textOverlay.rotation
                                    scaleX = textOverlay.scale
                                    scaleY = textOverlay.scale
                                }
                                .pointerInput(textOverlay.id) {
                                    detectDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = { isDragging = false },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            // Actualizar posición del texto
                                            textOverlays = textOverlays.map { t ->
                                                if (t.id == textOverlay.id) {
                                                    t.copy(
                                                        offsetX = t.offsetX + dragAmount.x,
                                                        offsetY = t.offsetY + dragAmount.y
                                                    )
                                                } else t
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
                
                // Collapse arrow (solo cuando no hay modal)
                if (!isAnyModalOpen) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Colapsar",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .size(28.dp)
                    )
                }
            }
            
            // ═══════════════════════════════════════════════════════════════
            // PLAYBACK CONTROLS
            // ═══════════════════════════════════════════════════════════════
            PlaybackControls(
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = videoDurationMs,
                onPlayPause = { isPlaying = !isPlaying },
                onUndo = { /* TODO: Undo */ },
                onRedo = { /* TODO: Redo */ }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // TIMELINE AREA
            // ═══════════════════════════════════════════════════════════════
            TimelineArea(
                layers = layers,
                timelineItems = timelineItems,
                selectedLayerIndex = selectedLayerIndex,
                videoThumbnails = videoThumbnails,
                videoDurationMs = videoDurationMs,
                currentPositionMs = currentPositionMs,
                timelineZoom = timelineZoom,
                isLoadingThumbnails = isLoadingThumbnails,
                isUserScrubbing = isUserScrubbing,
                selectedTimelineItem = selectedTimelineItem,
                showAddClipMenu = showAddClipMenu,
                onSeek = { positionMs ->
                    currentPositionMs = positionMs
                    exoPlayer.seekTo(positionMs)
                },
                onZoomChange = { timelineZoom = it },
                onUserScrubbingChange = { scrubbing ->
                    // Solo activar scrubbing si NO es scroll programático
                    if (!isProgrammaticScroll) {
                        isUserScrubbing = scrubbing
                    }
                },
                isProgrammaticScroll = isProgrammaticScroll,
                onProgrammaticScrollChange = { isProgrammaticScroll = it },
                onItemSelected = { item -> selectedTimelineItem = item },
                onAddClipMenuToggle = { showAddClipMenu = it },
                onAddFromGallery = {
                    // TODO: Implementar selector de galería
                    showAddClipMenu = false
                },
                onAddFromCamera = {
                    // TODO: Implementar captura de cámara
                    showAddClipMenu = false
                },
                onTrimVideo = { itemId, startDeltaMs, endDeltaMs ->
                    // Actualizar los tiempos de trim del item en timelineItems
                    val index = timelineItems.indexOfFirst { it.id == itemId }
                    if (index >= 0) {
                        val item = timelineItems[index]
                        val originalDuration = if (item.originalDurationMs > 0) item.originalDurationMs else videoDurationMs
                        
                        // Calcular nuevos tiempos de trim
                        // trimStartMs: cuánto se recorta del inicio (0 = sin recorte)
                        // trimEndMs: posición final del video (originalDuration = sin recorte)
                        val newTrimStart = (item.trimStartMs + startDeltaMs).coerceIn(0L, item.trimEndMs - 500L)
                        val newTrimEnd = (item.trimEndMs + endDeltaMs).coerceIn(item.trimStartMs + 500L, originalDuration)
                        
                        // La duración visible es trimEndMs - trimStartMs
                        val newDuration = newTrimEnd - newTrimStart
                        
                        timelineItems[index] = item.copy(
                            trimStartMs = newTrimStart,
                            trimEndMs = newTrimEnd,
                            endMs = item.startMs + newDuration
                        )
                        
                        // Sincronizar el player con los nuevos límites de trim
                        if (item.type == TimelineItemType.VIDEO) {
                            exoPlayer.seekTo(newTrimStart)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // ═══════════════════════════════════════════════════════════════
            // BOTTOM TOOLBAR - Sistema de herramientas multinivel
            // ═══════════════════════════════════════════════════════════════
            
            // Determinar herramientas actuales basadas en toolMode y selectedTimelineItem
            val currentTools = when {
                // Si hay item del timeline seleccionado, mostrar herramientas contextuales
                selectedTimelineItem != null -> when (selectedTimelineItem?.type) {
                    TimelineItemType.VIDEO -> VIDEO_CONTEXT_TOOLS
                    TimelineItemType.AUDIO -> AUDIO_CONTEXT_TOOLS
                    TimelineItemType.TEXT -> TEXT_CONTEXT_TOOLS
                    TimelineItemType.EFFECT -> EFFECT_CONTEXT_TOOLS
                    else -> EDITOR_TOOLS
                }
                // Si no, mostrar según toolMode
                else -> when (toolMode) {
                    EditorToolMode.MAIN -> EDITOR_TOOLS
                    EditorToolMode.AUDIO -> AUDIO_TOOLS
                    EditorToolMode.TEXT -> TEXT_TOOLS
                    EditorToolMode.VOICE -> VOICE_TOOLS
                    EditorToolMode.STICKERS -> STICKER_TOOLS
                    EditorToolMode.CAPTIONS -> CAPTIONS_TOOLS
                    EditorToolMode.ADJUST -> ADJUST_TOOLS
                    EditorToolMode.FILTERS -> FILTER_TOOLS
                    EditorToolMode.EFFECTS -> EFFECTS_TOOLS
                    EditorToolMode.CONTEXT -> EDITOR_TOOLS
                }
            }
            
            val isInContextMode = selectedTimelineItem != null || toolMode != EditorToolMode.MAIN
            
            EditorToolbar(
                tools = currentTools,
                selectedToolId = selectedToolId,
                isContextMode = isInContextMode,
                onToolClick = { toolId ->
                    when (toolId) {
                        "back" -> {
                            // Volver a herramientas principales
                            selectedTimelineItem = null
                            toolMode = EditorToolMode.MAIN
                            selectedToolId = null
                            showAdjustPanel = false
                            selectedAdjustTool = null
                        }
                        // ═══ HERRAMIENTAS QUE ABREN MODALES ═══
                        "text" -> {
                            exoPlayer.pause()
                            isPlaying = false
                            showTextModal = true
                        }
                        "adjust" -> {
                            showAdjustModal = true
                            toolMode = EditorToolMode.ADJUST
                        }
                        "filters" -> {
                            showFiltersModal = true
                        }
                        "effects" -> {
                            showEffectsModal = true
                        }
                        // Herramientas que abren submenús
                        "audio" -> toolMode = EditorToolMode.AUDIO
                        "voice" -> toolMode = EditorToolMode.VOICE
                        "stickers" -> toolMode = EditorToolMode.STICKERS
                        "captions" -> toolMode = EditorToolMode.CAPTIONS
                        // Herramientas de ajuste que activan slider
                        "brightness", "contrast", "saturation", "exposure", 
                        "highlights", "shadows", "temperature", "sharpen", "vignette" -> {
                            selectedAdjustTool = if (selectedAdjustTool == toolId) null else toolId
                            selectedToolId = toolId
                            showAdjustPanel = true
                        }
                        
                        // ═══ HERRAMIENTAS CONTEXTUALES DE TIMELINE ═══
                        "split" -> {
                            // Dividir clip en la posición actual del playhead
                            selectedTimelineItem?.let { item ->
                                val splitPosition = currentPositionMs
                                if (splitPosition > item.startMs && splitPosition < item.endMs) {
                                    val index = timelineItems.indexOfFirst { it.id == item.id }
                                    if (index >= 0) {
                                        val originalItem = timelineItems[index]
                                        val origDur = if (originalItem.originalDurationMs > 0) originalItem.originalDurationMs else videoDurationMs
                                        
                                        // Primera parte: desde trimStart hasta la posición de corte
                                        val firstDuration = splitPosition - originalItem.startMs
                                        val firstPart = originalItem.copy(
                                            id = "${originalItem.id}_part1",
                                            endMs = splitPosition,
                                            originalDurationMs = origDur,
                                            trimEndMs = originalItem.trimStartMs + firstDuration
                                        )
                                        
                                        // Segunda parte: desde la posición de corte hasta trimEnd
                                        val secondPart = originalItem.copy(
                                            id = "${originalItem.id}_part2",
                                            startMs = splitPosition,
                                            originalDurationMs = origDur,
                                            trimStartMs = originalItem.trimStartMs + firstDuration
                                        )
                                        
                                        timelineItems.removeAt(index)
                                        timelineItems.add(index, firstPart)
                                        timelineItems.add(index + 1, secondPart)
                                        selectedTimelineItem = null
                                        
                                        // Seek al punto de corte
                                        exoPlayer.seekTo(splitPosition)
                                    }
                                }
                            }
                        }
                        "trim" -> {
                            // El trim se hace con los handles - solo indicar
                            selectedToolId = "trim"
                        }
                        "duplicate" -> {
                            // Duplicar el item seleccionado
                            selectedTimelineItem?.let { item ->
                                val newItem = item.copy(
                                    id = "${item.id}_copy_${System.currentTimeMillis()}",
                                    startMs = item.endMs,
                                    endMs = item.endMs + (item.endMs - item.startMs)
                                )
                                timelineItems.add(newItem)
                            }
                        }
                        "speed" -> {
                            showSpeedModal = true
                        }
                        "volume" -> {
                            showVolumeModal = true
                        }
                        "extract" -> {
                            // Extraer audio del video y agregarlo como capa de audio separada
                            selectedTimelineItem?.let { item ->
                                if (item.type == TimelineItemType.VIDEO) {
                                    val audioExists = timelineItems.any { 
                                        it.type == TimelineItemType.AUDIO && it.layerId == "audio_extracted_${item.id}" 
                                    }
                                    if (!audioExists) {
                                        val audioItem = TimelineItem(
                                            id = "audio_extracted_${item.id}_${System.currentTimeMillis()}",
                                            layerId = "audio",
                                            startMs = item.startMs,
                                            endMs = item.endMs,
                                            type = TimelineItemType.AUDIO,
                                            data = item.data, // Misma URI del video
                                            originalDurationMs = item.originalDurationMs,
                                            trimStartMs = item.trimStartMs,
                                            trimEndMs = item.trimEndMs
                                        )
                                        timelineItems.add(audioItem)
                                    }
                                }
                            }
                        }
                        "filters" -> {
                            // Abrir modal de filtros (desde herramienta contextual del timeline)
                            showFiltersModal = true
                        }
                        "adjust" -> {
                            // Abrir modal de ajustes (desde herramienta contextual del timeline)
                            showAdjustModal = true
                            toolMode = EditorToolMode.ADJUST
                        }
                        "delete" -> {
                            // Eliminar el item seleccionado
                            selectedTimelineItem?.let { item ->
                                // No permitir eliminar el video principal único
                                val videoItems = timelineItems.filter { it.type == TimelineItemType.VIDEO }
                                if (item.type != TimelineItemType.VIDEO || videoItems.size > 1) {
                                    timelineItems.removeAll { it.id == item.id }
                                }
                                selectedTimelineItem = null
                            }
                        }
                        
                        else -> {
                            selectedToolId = if (selectedToolId == toolId) null else toolId
                        }
                    }
                }
            )
            
            // ═══════════════════════════════════════════════════════════════
            // PANEL DE AJUSTES - Slider profesional
            // ═══════════════════════════════════════════════════════════════
            AnimatedVisibility(
                visible = showAdjustPanel && selectedAdjustTool != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                selectedAdjustTool?.let { adjustId ->
                    val adjustConfig = mapOf(
                        "brightness" to Triple("Brillo", -100f, 100f),
                        "contrast" to Triple("Contraste", -100f, 100f),
                        "saturation" to Triple("Saturación", -100f, 100f),
                        "exposure" to Triple("Exposición", -2f, 2f),
                        "highlights" to Triple("Luces", -100f, 100f),
                        "shadows" to Triple("Sombras", -100f, 100f),
                        "temperature" to Triple("Temperatura", -100f, 100f),
                        "sharpen" to Triple("Nitidez", 0f, 100f),
                        "vignette" to Triple("Viñeta", 0f, 100f)
                    )
                    
                    val config = adjustConfig[adjustId]
                    if (config != null) {
                        val currentValue = adjustValues[adjustId] ?: 0f
                        
                        AdjustSliderPanel(
                            label = config.first,
                            value = currentValue,
                            minValue = config.second,
                            maxValue = config.third,
                            onValueChange = { newValue ->
                                adjustValues = adjustValues.toMutableMap().apply {
                                    put(adjustId, newValue)
                                }
                            },
                            onReset = {
                                adjustValues = adjustValues.toMutableMap().apply {
                                    put(adjustId, 0f)
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // ═══════════════════════════════════════════════════════════════
        // MODAL DE TEXTO - Estilo Instagram con teclado
        // ═══════════════════════════════════════════════════════════════
        if (showTextModal) {
            com.rendly.app.ui.components.StoryTextEditor(
                visible = true,
                previewHeight = 400.dp,
                onDismiss = { 
                    // Al cerrar, si hay texto, agregarlo a la lista de overlays
                    if (editingTextState.text.isNotBlank()) {
                        val newOverlay = VideoTextOverlay(
                            text = editingTextState.text,
                            fontSize = editingTextState.fontSize,
                            color = editingTextState.color,
                            startMs = currentPositionMs,
                            endMs = videoDurationMs
                        )
                        textOverlays = textOverlays + newOverlay
                        // Reset estado de edición
                        editingTextState = com.rendly.app.ui.components.StoryTextState()
                    }
                    showTextModal = false 
                },
                onTextStateChanged = { newState ->
                    editingTextState = newState
                }
            )
        }
        
        // ═══════════════════════════════════════════════════════════════
        // MODAL DE FILTROS - Altura automática, no sobrepasa el preview
        // ═══════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showFiltersModal,
            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FiltersModalCompact(
                selectedFilter = selectedFilter,
                onFilterSelected = { filter -> selectedFilter = filter },
                onDismiss = { showFiltersModal = false }
            )
        }
        
        // ═══════════════════════════════════════════════════════════════
        // MODAL DE EFECTOS - Altura automática, no sobrepasa el preview
        // ═══════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showEffectsModal,
            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            EffectsModalCompact(
                onEffectSelected = { effect -> 
                    selectedEffect = effect
                },
                onDismiss = { showEffectsModal = false }
            )
        }
        
        // ═══════════════════════════════════════════════════════════════
        // MODAL DE AJUSTES - Altura automática, no sobrepasa el preview
        // ═══════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showAdjustModal,
            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AdjustModalCompact(
                adjustValues = adjustValues,
                onValueChange = { key, value ->
                    adjustValues = adjustValues.toMutableMap().apply { put(key, value) }
                },
                onDismiss = { 
                    showAdjustModal = false
                    toolMode = EditorToolMode.MAIN
                }
            )
        }
        
        // ═══════════════════════════════════════════════════════════════
        // MODAL DE VELOCIDAD
        // ═══════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showSpeedModal,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SpeedModalCompact(
                currentSpeed = videoSpeed,
                onSpeedChange = { speed -> 
                    videoSpeed = speed
                    // Aplicar velocidad al ExoPlayer
                    exoPlayer.setPlaybackSpeed(speed)
                },
                onDismiss = { showSpeedModal = false }
            )
        }
        
        // ═══════════════════════════════════════════════════════════════
        // MODAL DE VOLUMEN
        // ═══════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = showVolumeModal,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            VolumeModalCompact(
                currentVolume = videoVolume,
                onVolumeChange = { volume -> 
                    videoVolume = volume
                    // Aplicar volumen al ExoPlayer
                    exoPlayer.volume = volume
                },
                onDismiss = { showVolumeModal = false }
            )
        }
    }
}

@Composable
private fun EditorHeader(
    projectName: String,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp), // paddingTop reducido
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón cerrar
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Nombre del proyecto
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { /* TODO: Editar nombre */ },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = projectName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Botón Siguiente
        Surface(
            onClick = onNext,
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Text(
                text = "Siguiente",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun VideoPreview(
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    adjustValues: Map<String, Float> = emptyMap(),
    filterColorMatrix: FloatArray? = null
) {
    // Construir ColorMatrix combinando filtro + ajustes
    val colorMatrix = remember(adjustValues, filterColorMatrix) {
        buildVideoColorMatrix(adjustValues, filterColorMatrix)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTogglePlay
            ),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Aplicar ColorMatrix como RenderEffect (API 31+)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        val androidMatrix = android.graphics.ColorMatrix(colorMatrix)
                        renderEffect = android.graphics.RenderEffect
                            .createColorFilterEffect(
                                android.graphics.ColorMatrixColorFilter(androidMatrix)
                            )
                            .asComposeRenderEffect()
                    }
                }
        )
    }
}

/**
 * Construye un ColorMatrix combinando los ajustes del usuario y el filtro seleccionado.
 * Retorna un FloatArray de 20 elementos (4x5 matrix).
 */
private fun buildVideoColorMatrix(
    adjustValues: Map<String, Float>,
    filterColorMatrix: FloatArray? = null
): FloatArray {
    val matrix = android.graphics.ColorMatrix()
    
    // 1. Aplicar filtro preset (la colorMatrix viene directamente del ImageFilter)
    if (filterColorMatrix != null && filterColorMatrix.size == 20) {
        val isIdentity = filterColorMatrix[0] == 1f && filterColorMatrix[6] == 1f &&
                filterColorMatrix[12] == 1f && filterColorMatrix[18] == 1f &&
                filterColorMatrix[4] == 0f && filterColorMatrix[9] == 0f && filterColorMatrix[14] == 0f
        if (!isIdentity) {
            matrix.set(filterColorMatrix)
        }
    }
    
    // 2. Aplicar ajustes manuales
    val brightness = adjustValues["brightness"] ?: 0f
    val contrast = adjustValues["contrast"] ?: 0f
    val saturation = adjustValues["saturation"] ?: 0f
    val temperature = adjustValues["temperature"] ?: 0f
    val exposure = adjustValues["exposure"] ?: 0f
    val highlights = adjustValues["highlights"] ?: 0f
    val shadows = adjustValues["shadows"] ?: 0f
    
    // Brillo: desplaza los canales RGB
    if (brightness != 0f) {
        val b = brightness * 2.55f // Convertir de -100..100 a -255..255
        val bm = android.graphics.ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, b,
            0f, 1f, 0f, 0f, b,
            0f, 0f, 1f, 0f, b,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.postConcat(bm)
    }
    
    // Contraste: escala alrededor del punto medio
    if (contrast != 0f) {
        val c = 1f + contrast / 100f
        val t = (-.5f * c + .5f) * 255f
        val cm = android.graphics.ColorMatrix(floatArrayOf(
            c, 0f, 0f, 0f, t,
            0f, c, 0f, 0f, t,
            0f, 0f, c, 0f, t,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.postConcat(cm)
    }
    
    // Saturación
    if (saturation != 0f) {
        val s = 1f + saturation / 100f
        val sm = android.graphics.ColorMatrix()
        sm.setSaturation(s.coerceAtLeast(0f))
        matrix.postConcat(sm)
    }
    
    // Temperatura: desplaza rojo/azul
    if (temperature != 0f) {
        val t = temperature * 0.5f
        val tm = android.graphics.ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, t,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, -t,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.postConcat(tm)
    }
    
    // Exposición: multiplicador general
    if (exposure != 0f) {
        val e = Math.pow(2.0, exposure.toDouble()).toFloat()
        val em = android.graphics.ColorMatrix(floatArrayOf(
            e, 0f, 0f, 0f, 0f,
            0f, e, 0f, 0f, 0f,
            0f, 0f, e, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.postConcat(em)
    }
    
    // Highlights: afecta valores altos (simulado con gamma inverso suave)
    if (highlights != 0f) {
        val h = highlights * 0.5f
        val hm = android.graphics.ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, h,
            0f, 1f, 0f, 0f, h,
            0f, 0f, 1f, 0f, h,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.postConcat(hm)
    }
    
    // Shadows: afecta valores bajos (simulado)
    if (shadows != 0f) {
        val s = shadows * 0.3f
        val sm = android.graphics.ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, s,
            0f, 1f, 0f, 0f, s,
            0f, 0f, 1f, 0f, s,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.postConcat(sm)
    }
    
    return matrix.array
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // Time display
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTimeMs(currentPositionMs),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Duration badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Text(
                    text = formatDurationShort(durationMs),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        // Undo/Redo
        Row {
            IconButton(
                onClick = onUndo,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Deshacer",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onRedo,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = "Rehacer",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * TIMELINE AREA - Scroll Inteligente Anti "peleas"
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Características:
 * - Detecta cuando el usuario está interactuando manualmente (drag/fling/scrub)
 * - animateScrollTo() solo cuando el usuario NO está tocando
 * - Scroll manual sin interferencia del sistema
 * - Experiencia idéntica a Instagram/CapCut
 */
@Composable
private fun TimelineArea(
    layers: List<TimelineLayer>,
    timelineItems: List<TimelineItem>,
    selectedLayerIndex: Int,
    videoThumbnails: List<Bitmap>,
    videoDurationMs: Long,
    currentPositionMs: Long,
    timelineZoom: Float,
    isLoadingThumbnails: Boolean,
    isUserScrubbing: Boolean,
    selectedTimelineItem: TimelineItem?,
    showAddClipMenu: Boolean,
    onSeek: (Long) -> Unit,
    onZoomChange: (Float) -> Unit,
    onUserScrubbingChange: (Boolean) -> Unit,
    isProgrammaticScroll: Boolean,
    onProgrammaticScrollChange: (Boolean) -> Unit,
    onItemSelected: (TimelineItem?) -> Unit,
    onAddClipMenuToggle: (Boolean) -> Unit,
    onAddFromGallery: () -> Unit,
    onAddFromCamera: () -> Unit,
    onTrimVideo: ((String, Long, Long) -> Unit)? = null
) {
    val density = LocalDensity.current
    var timelineWidth by remember { mutableIntStateOf(0) }
    val timelineScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // ═══ ESTADO DE TRIMMING - Deshabilita scroll mientras se hace trim ═══
    var isTrimmingActive by remember { mutableStateOf(false) }
    
    // Resetear isTrimmingActive cuando no hay item seleccionado
    LaunchedEffect(selectedTimelineItem) {
        if (selectedTimelineItem == null) {
            isTrimmingActive = false
        }
    }
    
    // ═══ ESTADO DE DRAG & DROP CON GUÍAS ═══
    var isDraggingElement by remember { mutableStateOf(false) }
    var draggedElementId by remember { mutableStateOf<String?>(null) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var showVerticalGuide by remember { mutableStateOf(false) }
    var guidePositionX by remember { mutableFloatStateOf(0f) }
    
    // Calcular pixeles por milisegundo para conversiones
    val pixelsPerMs = remember(timelineWidth, videoDurationMs, timelineZoom) {
        if (videoDurationMs > 0 && timelineWidth > 0) {
            (timelineWidth * timelineZoom) / videoDurationMs.toFloat()
        } else 0.001f
    }
    
    // ═══ DETECCIÓN DE INTERACCIÓN DEL USUARIO ═══
    // Solo detecta scroll manual, ignora scroll programático
    LaunchedEffect(timelineScrollState.isScrollInProgress, isProgrammaticScroll) {
        if (timelineScrollState.isScrollInProgress && !isProgrammaticScroll) {
            onUserScrubbingChange(true)
        } else if (!timelineScrollState.isScrollInProgress) {
            delay(100)
            onUserScrubbingChange(false)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Layer icons (izquierda) - DINÁMICO basado en capas activas
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Solo mostrar iconos de las capas que existen
                layers.forEach { layer ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(layer.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (layer.type) {
                                LayerType.VIDEO -> Icons.Outlined.VideoLibrary
                                LayerType.AUDIO -> Icons.Outlined.VolumeUp
                                LayerType.EFFECT -> Icons.Outlined.AutoAwesome
                                LayerType.TEXT -> Icons.Outlined.TextFields
                            },
                            contentDescription = layer.name,
                            tint = layer.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Timeline content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onSizeChanged { timelineWidth = it.width }
            ) {
                // ═══ PADDING DINÁMICO PARA SCROLL COMPLETO ═══
                // El playhead está centrado, así que necesitamos padding = mitad del viewport
                // para que el inicio y fin del video puedan alinearse con el playhead
                val horizontalPadding = with(density) { (timelineWidth / 2).toDp() }
                
                // Scrollable timeline con detección de gestos
                // OPTIMIZACIÓN: graphicsLayer fuerza composición en capa separada (GPU)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { 
                            // Forzar composición en capa GPU para scroll ultra fluido
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                        .horizontalScroll(
                            state = timelineScrollState,
                            enabled = !isTrimmingActive // Deshabilitar scroll durante trimming
                        )
                ) {
                    // Padding inicial para que el inicio del video pueda llegar al playhead
                    Spacer(modifier = Modifier.width(horizontalPadding))
                    
                    Column(
                        modifier = Modifier
                            .width(with(density) { (timelineWidth * timelineZoom).toDp() })
                            .fillMaxHeight()
                            .padding(vertical = 8.dp)
                            .pointerInput(videoDurationMs, pixelsPerMs, horizontalPadding) {
                                detectTapGestures { offset ->
                                    // Tap para buscar posición (ajustado por padding)
                                    val tappedMs = (offset.x / pixelsPerMs).toLong()
                                        .coerceIn(0, videoDurationMs)
                                    onSeek(tappedMs)
                                }
                            }
                            // ═══ GUÍAS PUNTEADAS DE ALINEACIÓN ═══
                            .drawBehind {
                                if (isDraggingElement && showVerticalGuide) {
                                    val dashPath = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    drawLine(
                                        color = Color(0xFFFFD700),
                                        start = Offset(guidePositionX, 0f),
                                        end = Offset(guidePositionX, size.height),
                                        strokeWidth = 2f,
                                        pathEffect = dashPath
                                    )
                                }
                            },
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Renderizar capas dinámicamente basado en timelineItems
                        val videoItems = timelineItems.filter { it.type == TimelineItemType.VIDEO }
                        val audioItems = timelineItems.filter { it.type == TimelineItemType.AUDIO }
                        val effectItems = timelineItems.filter { it.type == TimelineItemType.EFFECT }
                        
                        // Effects layer (solo si hay efectos)
                        effectItems.forEach { effectItem ->
                            // Calcular ancho proporcional basado en el trim
                            val effectiveTrimEndEffect = if (effectItem.trimEndMs > 0) effectItem.trimEndMs else (if (effectItem.originalDurationMs > 0) effectItem.originalDurationMs else videoDurationMs)
                            val effectClipDuration = effectiveTrimEndEffect - effectItem.trimStartMs
                            val effectOriginalDuration = if (effectItem.originalDurationMs > 0) effectItem.originalDurationMs else videoDurationMs
                            val effectWidthFraction = if (effectOriginalDuration > 0 && effectClipDuration > 0) (effectClipDuration.toFloat() / effectOriginalDuration).coerceIn(0.1f, 1f) else 1f
                            
                            // Calcular offset para el handle izquierdo
                            val effectStartOffsetFraction = if (effectOriginalDuration > 0) (effectItem.trimStartMs.toFloat() / effectOriginalDuration) else 0f
                            val effectStartOffsetDp = with(density) { (timelineWidth * timelineZoom * effectStartOffsetFraction).toDp() }
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                if (effectStartOffsetFraction > 0f) {
                                    Spacer(modifier = Modifier.width(effectStartOffsetDp))
                                }
                                
                                EffectsLayerTrack(
                                    effectName = effectItem.data as? String ?: "Efecto",
                                    isSelected = selectedTimelineItem?.id == effectItem.id,
                                    widthFraction = effectWidthFraction,
                                    onClick = {
                                        onItemSelected(if (selectedTimelineItem?.id == effectItem.id) null else effectItem)
                                    },
                                    onTrimStart = { dragAmount ->
                                        val deltaMs = (dragAmount / pixelsPerMs).toLong()
                                        onTrimVideo?.invoke(effectItem.id, deltaMs, 0L)
                                    },
                                    onTrimEnd = { dragAmount ->
                                        val deltaMs = (dragAmount / pixelsPerMs).toLong()
                                        onTrimVideo?.invoke(effectItem.id, 0L, deltaMs)
                                    },
                                    onTrimmingStateChange = { isTrimming ->
                                        isTrimmingActive = isTrimming
                                    },
                                    modifier = Modifier
                                        .weight(effectWidthFraction)
                                        .height(36.dp)
                                )
                                
                                val effectEndSpaceFraction = 1f - effectStartOffsetFraction - effectWidthFraction
                                if (effectEndSpaceFraction > 0.01f) {
                                    Spacer(modifier = Modifier.weight(effectEndSpaceFraction.coerceAtLeast(0.01f)))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Video layer - siempre presente
                        videoItems.forEachIndexed { index, videoItem ->
                            // Calcular ancho proporcional basado en el trim
                            // Si trimEndMs es 0 (no inicializado), usar originalDurationMs o videoDurationMs
                            val effectiveTrimEnd = if (videoItem.trimEndMs > 0) videoItem.trimEndMs else (if (videoItem.originalDurationMs > 0) videoItem.originalDurationMs else videoDurationMs)
                            val clipDuration = effectiveTrimEnd - videoItem.trimStartMs
                            val originalDuration = if (videoItem.originalDurationMs > 0) videoItem.originalDurationMs else videoDurationMs
                            val widthFraction = if (originalDuration > 0 && clipDuration > 0) (clipDuration.toFloat() / originalDuration).coerceIn(0.1f, 1f) else 1f
                            
                            // Calcular offset para el handle izquierdo (cuando trimStartMs > 0)
                            val startOffsetFraction = if (originalDuration > 0) (videoItem.trimStartMs.toFloat() / originalDuration) else 0f
                            val startOffsetDp = with(density) { (timelineWidth * timelineZoom * startOffsetFraction).toDp() }
                            
                            // Row para aplicar el offset del trim izquierdo
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Spacer para offset del trim izquierdo
                                if (startOffsetFraction > 0f) {
                                    Spacer(modifier = Modifier.width(startOffsetDp))
                                }
                                
                                VideoLayerTrack(
                                    thumbnails = if (index == 0) videoThumbnails else emptyList(),
                                    isLoading = isLoadingThumbnails && index == 0,
                                    isSelected = selectedTimelineItem?.id == videoItem.id,
                                    onClick = {
                                        onItemSelected(if (selectedTimelineItem?.id == videoItem.id) null else videoItem)
                                    },
                                    onTrimStart = { dragAmount ->
                                        val deltaMs = (dragAmount / pixelsPerMs).toLong()
                                        onTrimVideo?.invoke(videoItem.id, deltaMs, 0L)
                                    },
                                    onTrimEnd = { dragAmount ->
                                        val deltaMs = (dragAmount / pixelsPerMs).toLong()
                                        onTrimVideo?.invoke(videoItem.id, 0L, deltaMs)
                                    },
                                    onLongPressDrag = { dragAmount ->
                                        isDraggingElement = true
                                        draggedElementId = videoItem.id
                                        dragOffsetX += dragAmount
                                    },
                                    onTrimmingStateChange = { isTrimming ->
                                        isTrimmingActive = isTrimming
                                    },
                                    modifier = Modifier
                                        .weight(widthFraction)
                                        .height(80.dp)
                                )
                                
                                // Spacer para el espacio restante después del trim derecho
                                val endSpaceFraction = 1f - startOffsetFraction - widthFraction
                                if (endSpaceFraction > 0.01f) {
                                    Spacer(modifier = Modifier.weight(endSpaceFraction.coerceAtLeast(0.01f)))
                                }
                            }
                            if (index < videoItems.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                        
                        // Audio layer (solo si hay audio)
                        if (audioItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            audioItems.forEach { audioItem ->
                                // Calcular ancho proporcional basado en el trim
                                val effectiveTrimEndAudio = if (audioItem.trimEndMs > 0) audioItem.trimEndMs else (if (audioItem.originalDurationMs > 0) audioItem.originalDurationMs else videoDurationMs)
                                val audioClipDuration = effectiveTrimEndAudio - audioItem.trimStartMs
                                val audioOriginalDuration = if (audioItem.originalDurationMs > 0) audioItem.originalDurationMs else videoDurationMs
                                val audioWidthFraction = if (audioOriginalDuration > 0 && audioClipDuration > 0) (audioClipDuration.toFloat() / audioOriginalDuration).coerceIn(0.1f, 1f) else 1f
                                
                                // Calcular offset para el handle izquierdo
                                val audioStartOffsetFraction = if (audioOriginalDuration > 0) (audioItem.trimStartMs.toFloat() / audioOriginalDuration) else 0f
                                val audioStartOffsetDp = with(density) { (timelineWidth * timelineZoom * audioStartOffsetFraction).toDp() }
                                
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    if (audioStartOffsetFraction > 0f) {
                                        Spacer(modifier = Modifier.width(audioStartOffsetDp))
                                    }
                                    
                                    AudioLayerTrack(
                                        isSelected = selectedTimelineItem?.id == audioItem.id,
                                        onClick = {
                                            onItemSelected(if (selectedTimelineItem?.id == audioItem.id) null else audioItem)
                                        },
                                        onTrimStart = { dragAmount ->
                                            val deltaMs = (dragAmount / pixelsPerMs).toLong()
                                            onTrimVideo?.invoke(audioItem.id, deltaMs, 0L)
                                        },
                                        onTrimEnd = { dragAmount ->
                                            val deltaMs = (dragAmount / pixelsPerMs).toLong()
                                            onTrimVideo?.invoke(audioItem.id, 0L, deltaMs)
                                        },
                                        onLongPressDrag = { dragAmount ->
                                            isDraggingElement = true
                                            draggedElementId = audioItem.id
                                            dragOffsetX += dragAmount
                                        },
                                        onTrimmingStateChange = { isTrimming ->
                                            isTrimmingActive = isTrimming
                                        },
                                        modifier = Modifier
                                            .weight(audioWidthFraction)
                                            .height(40.dp)
                                    )
                                    
                                    val audioEndSpaceFraction = 1f - audioStartOffsetFraction - audioWidthFraction
                                    if (audioEndSpaceFraction > 0.01f) {
                                        Spacer(modifier = Modifier.weight(audioEndSpaceFraction.coerceAtLeast(0.01f)))
                                    }
                                }
                            }
                        }
                    }
                    
                    // Padding final para que el fin del video pueda llegar al playhead
                    Spacer(modifier = Modifier.width(horizontalPadding))
                }
                
                // ═══ GUÍA VERTICAL CENTRAL PUNTEADA (visible durante drag) ═══
                if (isDraggingElement) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxHeight()
                            .width(2.dp)
                            .drawBehind {
                                val dashPath = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                drawLine(
                                    color = Color(0xFFFFD700).copy(alpha = 0.6f),
                                    start = Offset(size.width / 2, 0f),
                                    end = Offset(size.width / 2, size.height),
                                    strokeWidth = 2f,
                                    pathEffect = dashPath
                                )
                            }
                            .zIndex(5f)
                    )
                }
                
                // Playhead (fijo en el centro)
                Playhead(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                        .zIndex(10f)
                )
                
                // ═══ BOTÓN (+) CON MENÚ POPUP ═══
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    // Botón principal
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (showAddClipMenu) AccentGold 
                                else Color.White.copy(alpha = 0.15f)
                            )
                            .clickable { onAddClipMenuToggle(!showAddClipMenu) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (showAddClipMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Agregar clip",
                            tint = if (showAddClipMenu) Color.Black else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Menú popup fluido
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showAddClipMenu,
                        enter = fadeIn(animationSpec = tween(150)) + 
                               scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
                        exit = fadeOut(animationSpec = tween(100)) + 
                              scaleOut(targetScale = 0.8f, animationSpec = tween(100)),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = (-120).dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF2A2A3C),
                            shadowElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Opción Galería
                                Surface(
                                    onClick = onAddFromGallery,
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhotoLibrary,
                                            contentDescription = "Galería",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Galería",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .padding(horizontal = 8.dp)
                                        .background(Color.White.copy(alpha = 0.1f))
                                )
                                
                                // Opción Cámara
                                Surface(
                                    onClick = onAddFromCamera,
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Videocam,
                                            contentDescription = "Cámara",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Cámara",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
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
    
    // ═══ SINCRONIZACIÓN INTELIGENTE: Video → Scroll ═══
    // Usa scrollTo directo (sin animación) para máxima fluidez durante reproducción
    // El flag isProgrammaticScroll evita que esto dispare detección de scrubbing
    LaunchedEffect(currentPositionMs, pixelsPerMs, isUserScrubbing) {
        if (videoDurationMs > 0 && !isUserScrubbing) {
            val targetScroll = (currentPositionMs * pixelsPerMs).toInt()
                .coerceIn(0, timelineScrollState.maxValue)
            
            // Solo scrollear si hay diferencia significativa
            if ((targetScroll - timelineScrollState.value).absoluteValue > 2) {
                // Marcar como scroll programático ANTES de scrollear
                onProgrammaticScrollChange(true)
                scope.launch {
                    // Scroll directo sin animación para fluidez máxima
                    timelineScrollState.scrollTo(targetScroll)
                    // Pequeño delay y luego desactivar flag
                    delay(16)
                    onProgrammaticScrollChange(false)
                }
            }
        }
    }
    
    // ═══ SINCRONIZACIÓN: Scroll → Video ═══
    // Solo cuando el usuario hace scroll MANUAL (no programático)
    LaunchedEffect(timelineScrollState.value, isUserScrubbing, isProgrammaticScroll) {
        if (isUserScrubbing && !isProgrammaticScroll && videoDurationMs > 0 && timelineWidth > 0) {
            val newPositionMs = (timelineScrollState.value / pixelsPerMs).toLong()
                .coerceIn(0, videoDurationMs)
            onSeek(newPositionMs)
        }
    }
}

@Composable
private fun EffectsLayerTrack(
    effectName: String = "Efecto",
    isSelected: Boolean = false,
    widthFraction: Float = 0.4f,
    onClick: () -> Unit = {},
    onTrimStart: ((Float) -> Unit)? = null,
    onTrimEnd: ((Float) -> Unit)? = null,
    onTrimmingStateChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AccentGold else Color.Transparent
    var isTrimming by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF252530))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A2E))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxHeight()
                    .fillMaxWidth(widthFraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AccentGold.copy(alpha = 0.9f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = effectName,
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Handles de trim (solo cuando está seleccionado)
        if (isSelected) {
            // Handle izquierdo
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(20.dp)
                    .fillMaxHeight()
                    .zIndex(100f)
                    .pointerInput(onTrimStart, onTrimmingStateChange) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isTrimming = true
                            onTrimmingStateChange?.invoke(true)
                            var prevX = down.position.x
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pointer = event.changes.firstOrNull() ?: break
                                    if (!pointer.pressed) break
                                    pointer.consume()
                                    val deltaX = pointer.position.x - prevX
                                    prevX = pointer.position.x
                                    onTrimStart?.invoke(deltaX)
                                }
                            } finally {
                                isTrimming = false
                                onTrimmingStateChange?.invoke(false)
                            }
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .background(AccentGold)
                )
            }
            
            // Handle derecho
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(20.dp)
                    .fillMaxHeight()
                    .zIndex(100f)
                    .pointerInput(onTrimEnd, onTrimmingStateChange) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isTrimming = true
                            onTrimmingStateChange?.invoke(true)
                            var prevX = down.position.x
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pointer = event.changes.firstOrNull() ?: break
                                    if (!pointer.pressed) break
                                    pointer.consume()
                                    val deltaX = pointer.position.x - prevX
                                    prevX = pointer.position.x
                                    onTrimEnd?.invoke(deltaX)
                                }
                            } finally {
                                isTrimming = false
                                onTrimmingStateChange?.invoke(false)
                            }
                        }
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .background(AccentGold)
                )
            }
        }
    }
}

@Composable
private fun VideoLayerTrack(
    thumbnails: List<Bitmap>,
    isLoading: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onTrimStart: ((Float) -> Unit)? = null,
    onTrimEnd: ((Float) -> Unit)? = null,
    onLongPressDrag: ((Float) -> Unit)? = null,
    onTrimmingStateChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) PrimaryPurple else Color.White.copy(alpha = 0.2f)
    var isLongPressDragging by remember { mutableStateOf(false) }
    var isTrimming by remember { mutableStateOf(false) }
    
    // Animación de escala al arrastrar
    val scale by animateFloatAsState(
        targetValue = if (isLongPressDragging) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "videoTrackScale"
    )
    
    // Fondo grisáceo del canal
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF252530)) // Fondo gris oscuro del canal
    ) {
        // Track principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A1A2E))
                .border(2.dp, borderColor, RoundedCornerShape(10.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { isLongPressDragging = true }
                    )
                }
                .then(
                    if (isLongPressDragging) {
                        Modifier.pointerInput(isLongPressDragging) {
                            detectHorizontalDragGestures(
                                onDragEnd = { isLongPressDragging = false },
                                onDragCancel = { isLongPressDragging = false }
                            ) { change, dragAmount ->
                                change.consume()
                                onLongPressDrag?.invoke(dragAmount)
                            }
                        }
                    } else Modifier
                )
        ) {
            if (isLoading) {
                // Shimmer loading
                val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                val shimmerAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "shimmerAlpha"
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(8) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = shimmerAlpha * 0.3f))
                        )
                    }
                }
            } else if (thumbnails.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clipToBounds(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    thumbnails.forEach { thumbnail ->
                        AsyncImage(
                            model = thumbnail,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VideoLibrary,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // ═══ HANDLES DE CORTE (Zero-slop: sigue al dedo instantáneamente) ═══
        if (isSelected) {
            // Handle izquierdo (inicio)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(28.dp)
                    .fillMaxHeight()
                    .zIndex(100f)
                    .pointerInput(onTrimStart, onTrimmingStateChange) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isTrimming = true
                            onTrimmingStateChange?.invoke(true)
                            var prevX = down.position.x
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pointer = event.changes.firstOrNull() ?: break
                                    if (!pointer.pressed) break
                                    pointer.consume()
                                    val deltaX = pointer.position.x - prevX
                                    prevX = pointer.position.x
                                    onTrimStart?.invoke(deltaX)
                                }
                            } finally {
                                isTrimming = false
                                onTrimmingStateChange?.invoke(false)
                            }
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                        .background(PrimaryPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                            )
                        }
                    }
                }
            }
            
            // Handle derecho (fin)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(28.dp)
                    .fillMaxHeight()
                    .zIndex(100f)
                    .pointerInput(onTrimEnd, onTrimmingStateChange) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isTrimming = true
                            onTrimmingStateChange?.invoke(true)
                            var prevX = down.position.x
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pointer = event.changes.firstOrNull() ?: break
                                    if (!pointer.pressed) break
                                    pointer.consume()
                                    val deltaX = pointer.position.x - prevX
                                    prevX = pointer.position.x
                                    onTrimEnd?.invoke(deltaX)
                                }
                            } finally {
                                isTrimming = false
                                onTrimmingStateChange?.invoke(false)
                            }
                        }
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                        .background(PrimaryPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioLayerTrack(
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onTrimStart: ((Float) -> Unit)? = null,
    onTrimEnd: ((Float) -> Unit)? = null,
    onLongPressDrag: ((Float) -> Unit)? = null,
    onTrimmingStateChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AccentPink else Color.Transparent
    var isLongPressDragging by remember { mutableStateOf(false) }
    var isTrimming by remember { mutableStateOf(false) }
    
    // Animación de escala al arrastrar
    val scale by animateFloatAsState(
        targetValue = if (isLongPressDragging) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "audioTrackScale"
    )
    
    // Fondo grisáceo del canal
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF252530)) // Fondo gris oscuro del canal
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A2E))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { isLongPressDragging = true }
                    )
                }
                .then(
                    if (isLongPressDragging) {
                        Modifier.pointerInput(isLongPressDragging) {
                            detectHorizontalDragGestures(
                                onDragEnd = { isLongPressDragging = false },
                                onDragCancel = { isLongPressDragging = false }
                            ) { change, dragAmount ->
                                change.consume()
                                onLongPressDrag?.invoke(dragAmount)
                            }
                        }
                    } else Modifier
                )
        ) {
            // Decorative audio waveform
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val waveHeights = remember {
                    List(50) { (0.2f + Math.random().toFloat() * 0.8f) }
                }
                waveHeights.forEach { height ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(height)
                            .clip(RoundedCornerShape(1.dp))
                            .background(AccentPink.copy(alpha = 0.6f))
                    )
                }
            }
        }
        
        // ═══ HANDLES DE CORTE (Zero-slop: sigue al dedo instantáneamente) ═══
        if (isSelected) {
            // Handle izquierdo
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(24.dp)
                    .fillMaxHeight()
                    .zIndex(100f)
                    .pointerInput(onTrimStart, onTrimmingStateChange) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isTrimming = true
                            onTrimmingStateChange?.invoke(true)
                            var prevX = down.position.x
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pointer = event.changes.firstOrNull() ?: break
                                    if (!pointer.pressed) break
                                    pointer.consume()
                                    val deltaX = pointer.position.x - prevX
                                    prevX = pointer.position.x
                                    onTrimStart?.invoke(deltaX)
                                }
                            } finally {
                                isTrimming = false
                                onTrimmingStateChange?.invoke(false)
                            }
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .background(AccentPink),
                    contentAlignment = Alignment.Center
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                            )
                        }
                    }
                }
            }
            
            // Handle derecho
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(24.dp)
                    .fillMaxHeight()
                    .zIndex(100f)
                    .pointerInput(onTrimEnd, onTrimmingStateChange) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isTrimming = true
                            onTrimmingStateChange?.invoke(true)
                            var prevX = down.position.x
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pointer = event.changes.firstOrNull() ?: break
                                    if (!pointer.pressed) break
                                    pointer.consume()
                                    val deltaX = pointer.position.x - prevX
                                    prevX = pointer.position.x
                                    onTrimEnd?.invoke(deltaX)
                                }
                            } finally {
                                isTrimming = false
                                onTrimmingStateChange?.invoke(false)
                            }
                        }
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .background(AccentPink),
                    contentAlignment = Alignment.Center
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Playhead(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top handle
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(Color.White)
        )
        
        // Line
        Box(
            modifier = Modifier
                .width(2.dp)
                .weight(1f)
                .background(Color.White)
        )
        
        // Bottom handle
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                .background(Color.White)
        )
    }
}

/**
 * EditorToolbar - Barra de herramientas con soporte para modo contextual
 * 
 * Cuando isContextMode = true, muestra herramientas específicas del item
 * seleccionado con un botón de "back" a la izquierda para volver a las
 * herramientas principales.
 */
@Composable
private fun EditorToolbar(
    tools: List<EditorTool>,
    selectedToolId: String?,
    isContextMode: Boolean = false,
    onToolClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tools.forEach { tool ->
            val isSelected = tool.id == selectedToolId
            val isBackButton = tool.id == "back"
            
            if (isBackButton && isContextMode) {
                // Botón de back especial - alineado a la izquierda
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onToolClick(tool.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Divider vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                
                Spacer(modifier = Modifier.width(8.dp))
            } else if (!isBackButton) {
                // Herramienta normal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToolClick(tool.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.label,
                        tint = if (isSelected) AccentGold else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tool.label,
                        color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatDurationShort(ms: Long): String {
    val totalSeconds = ms / 1000
    return "${totalSeconds}s"
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * PANEL DE AJUSTE - Slider profesional estilo Instagram/Lightroom
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun AdjustSliderPanel(
    label: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Valor actual con badge
        val displayValue = if (maxValue <= 2f) {
            // Para exposición usar formato EV
            String.format("%+.1f", value)
        } else {
            String.format("%+.0f", value)
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$label: $displayValue",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Slider profesional
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = minValue..maxValue,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AccentGold,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
        
        // Reset si hay cambio
        AnimatedVisibility(
            visible = value != 0f,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "Toca para restablecer",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onReset() }
            )
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODAL DE FILTROS - Carrusel profesional estilo Instagram
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun FiltersModal(
    selectedFilter: com.rendly.app.ui.components.ImageFilter,
    onFilterSelected: (com.rendly.app.ui.components.ImageFilter) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF1A1A2E))
                .clickable(enabled = false) {} // Evitar propagación del click
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Filtros",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Carrusel de filtros
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(com.rendly.app.ui.components.STORY_FILTERS.size) { index ->
                    val filter = com.rendly.app.ui.components.STORY_FILTERS[index]
                    val isSelected = filter.id == selectedFilter.id
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFilterSelected(filter) }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(filter.previewColor)
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp,
                                        AccentGold,
                                        RoundedCornerShape(12.dp)
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.name.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = filter.name,
                            color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón aplicar
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
            ) {
                Text("Aplicar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODAL DE EFECTOS - Panel profesional
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun EffectsModal(
    onEffectSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val effects = listOf(
        Triple("none", Icons.Outlined.Block, "Original"),
        Triple("blur", Icons.Outlined.BlurOn, "Desenfoque"),
        Triple("glitch", Icons.Outlined.BrokenImage, "Glitch"),
        Triple("zoom", Icons.Outlined.ZoomIn, "Zoom"),
        Triple("shake", Icons.Outlined.Vibration, "Shake"),
        Triple("vhs", Icons.Outlined.Tv, "VHS"),
        Triple("rgb", Icons.Outlined.Palette, "RGB Split"),
        Triple("slow", Icons.Outlined.SlowMotionVideo, "Slow Motion"),
        Triple("fast", Icons.Outlined.Speed, "Fast Motion")
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF1A1A2E))
                .clickable(enabled = false) {}
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Efectos",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Grid de efectos
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(effects.size) { index ->
                    val (id, icon, name) = effects[index]
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { 
                                onEffectSelected(id)
                                onDismiss()
                            }
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = name,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODAL DE AJUSTES - Panel completo profesional estilo Lightroom
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun AdjustModal(
    adjustValues: Map<String, Float>,
    onValueChange: (String, Float) -> Unit,
    onDismiss: () -> Unit
) {
    val adjustOptions = listOf(
        Triple("brightness", Icons.Outlined.WbSunny, "Brillo"),
        Triple("contrast", Icons.Outlined.Contrast, "Contraste"),
        Triple("saturation", Icons.Outlined.Palette, "Saturación"),
        Triple("exposure", Icons.Outlined.Exposure, "Exposición"),
        Triple("highlights", Icons.Outlined.LightMode, "Luces"),
        Triple("shadows", Icons.Outlined.DarkMode, "Sombras"),
        Triple("temperature", Icons.Outlined.Thermostat, "Temperatura"),
        Triple("sharpen", Icons.Outlined.Deblur, "Nitidez"),
        Triple("vignette", Icons.Outlined.Vignette, "Viñeta")
    )
    
    var selectedAdjust by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF1A1A2E))
                .clickable(enabled = false) {}
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Ajustes",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Carrusel de herramientas de ajuste
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(adjustOptions.size) { index ->
                    val (id, icon, name) = adjustOptions[index]
                    val isSelected = selectedAdjust == id
                    val value = adjustValues[id] ?: 0f
                    val hasValue = value != 0f
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedAdjust = if (isSelected) null else id }
                            .background(
                                if (isSelected) AccentGold.copy(alpha = 0.2f) 
                                else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = if (isSelected) AccentGold else if (hasValue) Color.White else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            if (hasValue) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentGold)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = name,
                            color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
            
            // Slider para el ajuste seleccionado
            AnimatedVisibility(
                visible = selectedAdjust != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedAdjust?.let { adjustId ->
                    val currentValue = adjustValues[adjustId] ?: 0f
                    val range = when (adjustId) {
                        "exposure" -> -2f..2f
                        "sharpen", "vignette" -> 0f..100f
                        else -> -100f..100f
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Valor actual
                        Text(
                            text = if (range.start == -2f) String.format("%+.1f", currentValue)
                                   else String.format("%+.0f", currentValue),
                            color = AccentGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Slider(
                            value = currentValue,
                            onValueChange = { onValueChange(adjustId, it) },
                            valueRange = range,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = AccentGold,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        
                        if (currentValue != 0f) {
                            Text(
                                text = "Toca para restablecer",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { onValueChange(adjustId, 0f) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón aplicar
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
            ) {
                Text("Aplicar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODALES COMPACTOS - Preview visible sobre el modal (más altos, tapan controles)
 * ═══════════════════════════════════════════════════════════════════════════════
 */

@Composable
private fun FiltersModalCompact(
    selectedFilter: com.rendly.app.ui.components.ImageFilter,
    onFilterSelected: (com.rendly.app.ui.components.ImageFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val filters = com.rendly.app.ui.components.STORY_FILTERS
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f) // Exactamente mitad de pantalla
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF1A1A2E))
            .padding(top = 12.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtros",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Listo",
                color = AccentGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contenido scrolleable sin barra visible
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Grid de filtros 4x columnas
            val chunkedFilters = filters.chunked(4)
            chunkedFilters.forEach { rowFilters ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowFilters.forEach { filter ->
                        val isSelected = filter.id == selectedFilter.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onFilterSelected(filter) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) AccentGold else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF3A3A5C), Color(0xFF2A2A4C))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.name.take(2).uppercase(),
                                    color = if (isSelected) AccentGold else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = filter.name,
                                color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                    repeat(4 - rowFilters.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EffectsModalCompact(
    onEffectSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val effects = listOf(
        Triple("none", Icons.Outlined.Block, "Ninguno"),
        Triple("blur", Icons.Outlined.BlurOn, "Blur"),
        Triple("glitch", Icons.Outlined.BrokenImage, "Glitch"),
        Triple("vhs", Icons.Outlined.Videocam, "VHS"),
        Triple("slowmo", Icons.Outlined.SlowMotionVideo, "Slow Mo"),
        Triple("shake", Icons.Outlined.Vibration, "Shake"),
        Triple("zoom", Icons.Outlined.ZoomIn, "Zoom"),
        Triple("flash", Icons.Outlined.FlashOn, "Flash")
    )
    
    var selectedEffectLocal by remember { mutableStateOf("none") }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f) // Exactamente mitad de pantalla
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF1A1A2E))
            .padding(top = 12.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Efectos",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Listo",
                color = AccentGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contenido scrolleable sin barra visible
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            val chunkedEffects = effects.chunked(4)
            chunkedEffects.forEach { rowEffects ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowEffects.forEach { (id, icon, name) ->
                        val isSelected = id == selectedEffectLocal
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    selectedEffectLocal = id
                                    onEffectSelected(id) 
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) AccentGold else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .background(
                                        if (isSelected) AccentGold.copy(alpha = 0.2f)
                                        else Color(0xFF2A2A4C)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = name,
                                    tint = if (isSelected) AccentGold else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name,
                                color = if (isSelected) AccentGold else Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                    repeat(4 - rowEffects.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AdjustModalCompact(
    adjustValues: Map<String, Float>,
    onValueChange: (String, Float) -> Unit,
    onDismiss: () -> Unit
) {
    val adjustOptions = listOf(
        Triple("brightness", Icons.Outlined.WbSunny, "Brillo"),
        Triple("contrast", Icons.Outlined.Contrast, "Contraste"),
        Triple("saturation", Icons.Outlined.Palette, "Saturación"),
        Triple("exposure", Icons.Outlined.Exposure, "Exposición"),
        Triple("highlights", Icons.Outlined.LightMode, "Luces"),
        Triple("shadows", Icons.Outlined.DarkMode, "Sombras"),
        Triple("temperature", Icons.Outlined.Thermostat, "Temp."),
        Triple("sharpen", Icons.Outlined.Deblur, "Nitidez"),
        Triple("vignette", Icons.Outlined.Vignette, "Viñeta")
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f) // Exactamente mitad de pantalla
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF1A1A2E))
            .padding(top = 12.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ajustes",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Listo",
                color = AccentGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Lista vertical de ajustes con sliders (scroll sin barra visible)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ocupa el espacio restante
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(adjustOptions.size) { index ->
                val (id, icon, name) = adjustOptions[index]
                val currentValue = adjustValues[id] ?: 0f
                val range = when (id) {
                    "exposure" -> -2f..2f
                    "sharpen", "vignette" -> 0f..100f
                    else -> -100f..100f
                }
                val hasValue = currentValue != 0f
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nombre del ajuste
                    Row(
                        modifier = Modifier.width(90.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = name,
                            tint = if (hasValue) AccentGold else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            color = if (hasValue) AccentGold else Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    
                    // Slider
                    Slider(
                        value = currentValue,
                        onValueChange = { onValueChange(id, it) },
                        valueRange = range,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = AccentGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    
                    // Valor actual
                    Text(
                        text = if (range.start == -2f) String.format("%+.1f", currentValue)
                               else String.format("%+.0f", currentValue),
                        color = if (hasValue) AccentGold else Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODAL DE VELOCIDAD - Selector profesional de velocidad
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun SpeedModalCompact(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speedOptions = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF1A1A2E))
            .padding(top = 12.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Velocidad",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Listo",
                color = AccentGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Valor actual grande
        Text(
            text = "${currentSpeed}x",
            color = AccentGold,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Grid de opciones de velocidad
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(speedOptions.size) { index ->
                val speed = speedOptions[index]
                val isSelected = speed == currentSpeed
                
                Surface(
                    onClick = { onSpeedChange(speed) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentGold else Color(0xFF2A2A3C)
                ) {
                    Text(
                        text = "${speed}x",
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Slider de velocidad
        Slider(
            value = currentSpeed,
            onValueChange = { onSpeedChange((it * 4).roundToInt() / 4f) }, // Pasos de 0.25
            valueRange = 0.25f..3f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AccentGold,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * MODAL DE VOLUMEN - Control profesional de volumen
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Composable
private fun VolumeModalCompact(
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF1A1A2E))
            .padding(top = 12.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Volumen",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Listo",
                color = AccentGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Icono y valor actual
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when {
                    currentVolume == 0f -> Icons.Outlined.VolumeOff
                    currentVolume < 0.5f -> Icons.Outlined.VolumeDown
                    else -> Icons.Outlined.VolumeUp
                },
                contentDescription = "Volumen",
                tint = AccentGold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${(currentVolume * 100).toInt()}%",
                color = AccentGold,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Slider de volumen
        Slider(
            value = currentVolume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AccentGold,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botones rápidos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(0f to "Mute", 0.5f to "50%", 1f to "100%").forEach { (volume, label) ->
                val isSelected = currentVolume == volume
                Surface(
                    onClick = { onVolumeChange(volume) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentGold else Color(0xFF2A2A3C)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
