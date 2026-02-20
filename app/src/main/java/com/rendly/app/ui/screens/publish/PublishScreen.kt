package com.rendly.app.ui.screens.publish

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalView
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class PublishMode(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val description: String,
    val accentColor: Color
)

private val PUBLISH_MODES = listOf(
    PublishMode(
        id = "post",
        label = "Publicación",
        icon = Icons.Outlined.AddBox,
        description = "Permanente",
        accentColor = Color(0xFFFF6B35)
    ),
    PublishMode(
        id = "story",
        label = "Historia",
        icon = Icons.Outlined.AutoAwesome,
        description = "24 horas",
        accentColor = Color(0xFF2E8B57)
    ),
    PublishMode(
        id = "rend",
        label = "Rend",
        icon = Icons.Outlined.PlayCircle,
        description = "Video corto",
        accentColor = Color(0xFFFF6B35)
    ),
    PublishMode(
        id = "live",
        label = "En Vivo",
        icon = Icons.Outlined.Videocam,
        description = "Streaming",
        accentColor = Color(0xFFEF4444)
    )
)

@Composable
fun PublishScreen(
    onClose: () -> Unit = {},
    onStoryPublished: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onEditingStateChange: (Boolean) -> Unit = {}, // Para deshabilitar swipe cuando hay imagen capturada
    initialMode: Int = 1, // 1 = Historia (modo por defecto)
    modifier: Modifier = Modifier
) {
    // Validar que initialMode esté dentro del rango válido
    val safeInitialMode = initialMode.coerceIn(0, PUBLISH_MODES.size - 1)
    var selectedModeIndex by remember { mutableIntStateOf(safeInitialMode) }
    
    // Callbacks memorizados para evitar recomposiciones
    val onModeSelectedCallback = remember<(Int) -> Unit> { 
        { newIndex -> selectedModeIndex = newIndex.coerceIn(0, PUBLISH_MODES.size - 1) } 
    }
    
    // StatusBar y NavigationBar negros para coincidir con el fondo de la cámara
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        val originalStatusBarColor = window?.statusBarColor
        val originalNavBarColor = window?.navigationBarColor
        window?.statusBarColor = android.graphics.Color.BLACK
        window?.navigationBarColor = android.graphics.Color.BLACK
        onDispose {
            originalStatusBarColor?.let { window?.statusBarColor = it }
            originalNavBarColor?.let { window?.navigationBarColor = it }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.Black)
    ) {
        // Usar Crossfade en lugar de AnimatedContent (más ligero)
        Crossfade(
            targetState = selectedModeIndex,
            animationSpec = tween(150),
            label = "mode_content"
        ) { modeIndex ->
            val modeId = PUBLISH_MODES[modeIndex].id
            when (modeId) {
                "post" -> {
                    PublicationScreen(
                        onClose = onClose,
                        onNavigateToHome = onNavigateToHome,
                        onModeSelected = onModeSelectedCallback,
                        currentModeIndex = modeIndex
                    )
                }
                "story" -> {
                    HistoryScreen(
                        onClose = onClose,
                        onModeSelected = onModeSelectedCallback,
                        currentModeIndex = modeIndex,
                        onEditingStateChange = onEditingStateChange
                    )
                }
                "live" -> {
                    LiveStreamScreen(
                        onClose = onClose,
                        onModeSelected = onModeSelectedCallback,
                        currentModeIndex = modeIndex
                    )
                }
                "rend" -> {
                    RendScreen(
                        onClose = onClose,
                        onModeSelected = onModeSelectedCallback,
                        currentModeIndex = modeIndex
                    )
                }
            }
        }
    }
}

// El nuevo ModeCarousel está en com.rendly.app.ui.components.ModeCarousel
