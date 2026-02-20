package com.rendly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

data class QuickQuestion(
    val id: String,
    val icon: ImageVector,
    val text: String
)

private val QUICK_QUESTIONS = listOf(
    QuickQuestion("1", Icons.Default.Star, "¿Cuál es el precio final?"),
    QuickQuestion("2", Icons.Default.Send, "¿Haces envíos?"),
    QuickQuestion("3", Icons.Default.Refresh, "¿Aceptas cambios?"),
    QuickQuestion("4", Icons.Default.ShoppingCart, "¿Aceptas pagos en cuotas?"),
    QuickQuestion("5", Icons.Default.DateRange, "¿Cuánto demora el envío?"),
    QuickQuestion("6", Icons.Default.Check, "¿Tiene garantía?")
)

@Composable
fun FastConsultationModal(
    visible: Boolean,
    onDismiss: () -> Unit,
    productTitle: String,
    productPrice: String,
    sellerName: String,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf<String?>(null) }
    
    // Reset cuando se abre/cierra
    LaunchedEffect(visible) {
        if (!visible) {
            message = ""
            selectedQuestion = null
        }
    }
    
    // Backdrop
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )
    }
    
    // Modal content
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextMuted.copy(alpha = 0.3f))
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Consulta rápida",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "a $sellerName",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextMuted
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Product info card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = PrimaryPurple
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = productTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = productPrice,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Quick questions section
                    Text(
                        text = "Preguntas frecuentes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quick questions chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QUICK_QUESTIONS) { question ->
                            QuickQuestionChip(
                                question = question,
                                isSelected = selectedQuestion == question.id,
                                onClick = {
                                    selectedQuestion = question.id
                                    message = question.text
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Message input
                    Text(
                        text = "Tu mensaje",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Surface
                    ) {
                        BasicTextField(
                            value = message,
                            onValueChange = { 
                                message = it
                                selectedQuestion = null
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(PrimaryPurple),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (message.isEmpty()) {
                                        Text(
                                            text = "Escribe tu consulta aquí...",
                                            color = TextMuted,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Send button
                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
                                onSendMessage(message)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple,
                            disabledContainerColor = PrimaryPurple.copy(alpha = 0.3f)
                        ),
                        enabled = message.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar consulta",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickQuestionChip(
    question: QuickQuestion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else Surface,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = question.icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryPurple else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = question.text,
                fontSize = 12.sp,
                color = if (isSelected) PrimaryPurple else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
