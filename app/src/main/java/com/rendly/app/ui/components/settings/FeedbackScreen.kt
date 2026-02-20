package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.rendly.app.ui.theme.*
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.FeedbackRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FeedbackScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var feedbackText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Get current user info
    val currentUser = remember { SupabaseClient.client.auth.currentUserOrNull() }
    val userId = currentUser?.id ?: "anonymous"
    val userEmail = currentUser?.email
    
    // Load user name from usuarios table
    var userName by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(userId) {
        if (userId != "anonymous") {
            try {
                withContext(Dispatchers.IO) {
                    val result = SupabaseClient.client.from("usuarios")
                        .select() {
                            filter { eq("user_id", userId) }
                            limit(1)
                        }
                        .decodeSingleOrNull<Map<String, Any?>>()
                    
                    userName = result?.get("nombre")?.toString()
                        ?: result?.get("username")?.toString()
                }
            } catch (e: Exception) {
                android.util.Log.e("FeedbackScreen", "Error loading user name: ${e.message}")
            }
        }
    }
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp)
                .clickable(enabled = false) { },
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header
                SettingsScreenHeader(
                    title = "Enviar comentarios",
                    subtitle = "Tu opinión nos importa",
                    icon = Icons.Outlined.RateReview,
                    iconColor = PrimaryPurple,
                    onBack = onDismiss
                )
                
                if (showSuccess) {
                    // Pantalla de éxito
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color(0xFF2E8B57).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF2E8B57),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "¡Gracias por tu feedback!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Tu opinión nos ayuda a mejorar Merqora cada Día. Revisaremos tus comentarios con Atención.",
                            fontSize = 15.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Volver",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Intro
                        Text(
                            text = "¿Cómo podemos mejorar?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Tu opinión es muy importante para nosotros. Cuéntanos qué te gustaría ver en Merqora.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Rating
                        Text(
                            text = "¿Cómo calificarías tu experiencia?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (1..5).forEach { rating ->
                                IconButton(
                                    onClick = { selectedRating = rating },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (rating <= selectedRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = if (rating <= selectedRating) Color(0xFFFF6B35) else TextMuted.copy(alpha = 0.4f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // categoría
                        Text(
                            text = "¿Sobre ¿Qué quieres comentar?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val categories = listOf(
                            Triple("feature_request", "Nueva Función", Icons.Outlined.Lightbulb),
                            Triple("improvement", "Mejora", Icons.Outlined.TrendingUp),
                            Triple("complaint", "Queja", Icons.Outlined.ThumbDown),
                            Triple("praise", "Elogio", Icons.Outlined.ThumbUp),
                            Triple("other", "Otro", Icons.Outlined.MoreHoriz)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.take(3).forEach { (id, label, icon) ->
                                FeedbackCategoryChip(
                                    label = label,
                                    icon = icon,
                                    isSelected = selectedCategory == id,
                                    onClick = { selectedCategory = id },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.drop(3).forEach { (id, label, icon) ->
                                FeedbackCategoryChip(
                                    label = label,
                                    icon = icon,
                                    isSelected = selectedCategory == id,
                                    onClick = { selectedCategory = id },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Comentario
                        Text(
                            text = "Tu comentario",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = feedbackText,
                            onValueChange = { feedbackText = it },
                            placeholder = {
                                Text(
                                    "Cuéntanos tu idea o sugerencia...",
                                    color = TextMuted.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${feedbackText.length}/500 caracteres",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.align(Alignment.End)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Info
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1565A0).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF1565A0),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "No respondemos comentarios individuales, pero cada sugerencia es evaluada por nuestro equipo.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF1565A0),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // botón enviar
                        Button(
                            onClick = {
                                isSubmitting = true
                                scope.launch {
                                    val result = FeedbackRepository.submitFeedback(
                                        userId = userId,
                                        category = selectedCategory ?: "other",
                                        title = "Feedback de usuario",
                                        description = feedbackText,
                                        rating = selectedRating,
                                        userName = userName,
                                        userEmail = userEmail,
                                        context = context
                                    )
                                    
                                    isSubmitting = false
                                    when (result) {
                                        is FeedbackRepository.SubmissionResult.Success -> {
                                            showSuccess = true
                                        }
                                        is FeedbackRepository.SubmissionResult.Error -> {
                                            showError = true
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = feedbackText.isNotBlank() && selectedCategory != null && selectedRating > 0 && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple,
                                disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isSubmitting) "Enviando..." else "Enviar comentario",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        
                        // Mostrar error si hay
                        if (showError) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Error: $errorMessage",
                                        fontSize = 13.sp,
                                        color = Color(0xFFEF4444),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackCategoryChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, PrimaryPurple, RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryPurple else TextMuted,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PrimaryPurple else TextMuted
            )
        }
    }
}
