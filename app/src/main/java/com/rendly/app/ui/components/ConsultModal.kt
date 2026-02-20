package com.rendly.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Post
import com.rendly.app.ui.theme.*

@Composable
fun ConsultModal(
    isVisible: Boolean,
    post: Post?,
    onDismiss: () -> Unit,
    onSendConsult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }
    var showBargaining by remember { mutableStateOf(false) }
    var bargainPrice by remember { mutableStateOf("") }
    var bargainSuggestion by remember { mutableStateOf<String?>(null) }
    
    // Calcular límites de regateo (mínimo 70% del precio original - no visible al usuario)
    val originalPrice = post?.producto?.precio ?: 0.0
    val minBargainPrice = originalPrice * 0.70
    
    // Backdrop
    if (isVisible) {
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
                    .fillMaxHeight(), // Usar TODA la altura de la pantalla
                color = HomeBg,
                shape = RoundedCornerShape(0.dp) // Sin border radius en pantalla completa
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
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
                    
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showBargaining) "Hacer una oferta" else "Consultar",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { 
                            if (showBargaining) {
                                showBargaining = false
                                bargainPrice = ""
                                bargainSuggestion = null
                            } else {
                                onDismiss()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextMuted
                            )
                        }
                    }
                    
                    Divider(color = TextMuted.copy(alpha = 0.1f))
                    
                    // Product preview
                    post?.let { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = p.producto.imagenUrl.firstOrNull() ?: "",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.producto.titulo,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "$${String.format("%.2f", p.producto.precio)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }
                    
                    Divider(color = TextMuted.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                    
                    if (!showBargaining) {
                        // ═══════════════════════════════════════════════════════════
                        // SECCIÓN DE CONSULTA + REGATEO RÁPIDO
                        // ═══════════════════════════════════════════════════════════
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Botón de Regateo destacado
                        Surface(
                            onClick = { showBargaining = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = AccentGreen.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalOffer,
                                        contentDescription = null,
                                        tint = AccentGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hacer una oferta",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Propón tu precio y negocia con el vendedor",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = AccentGreen
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Consulta personalizada",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Textarea para mensaje
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(100.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                if (message.isEmpty()) {
                                    Text(
                                        text = "Escribe tu consulta al vendedor...",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = message,
                                    onValueChange = { message = it },
                                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                                    cursorBrush = SolidColor(PrimaryPurple),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                if (message.isNotBlank()) {
                                    onSendConsult(message)
                                    message = ""
                                    onDismiss()
                                }
                            },
                            enabled = message.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple,
                                disabledContainerColor = PrimaryPurple.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar consulta", fontWeight = FontWeight.SemiBold)
                        }
                        
                    } else {
                        // ═══════════════════════════════════════════════════════════
                        // SISTEMA DE REGATEO
                        // ═══════════════════════════════════════════════════════════
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Sugerencias de regateo rápido
                        Text(
                            text = "Ofertas sugeridas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val suggestions = listOf(
                            (originalPrice * 0.95) to "5% menos",
                            (originalPrice * 0.90) to "10% menos",
                            (originalPrice * 0.85) to "15% menos"
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestions.forEach { (price, label) ->
                                Surface(
                                    onClick = { 
                                        bargainPrice = String.format("%.2f", price)
                                        bargainSuggestion = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (bargainPrice == String.format("%.2f", price)) 
                                        PrimaryPurple.copy(alpha = 0.15f) else Surface,
                                    border = if (bargainPrice == String.format("%.2f", price))
                                        androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f)) else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$${String.format("%.2f", price)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (bargainPrice == String.format("%.2f", price)) PrimaryPurple else TextPrimary
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "O ingresa tu oferta",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Input de precio personalizado
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Surface
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = bargainPrice,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                            bargainPrice = newValue
                                            // Verificar si es muy bajo y sugerir
                                            val price = newValue.toDoubleOrNull() ?: 0.0
                                            bargainSuggestion = when {
                                                price < minBargainPrice && price > 0 -> 
                                                    "Te sugerimos ofrecer $${String.format("%.2f", minBargainPrice)} para mayor probabilidad de aceptación"
                                                else -> null
                                            }
                                        }
                                    },
                                    textStyle = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    cursorBrush = SolidColor(PrimaryPurple),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                        
                        // Sugerencia si el precio es muy bajo
                        bargainSuggestion?.let { suggestion ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = AccentYellow.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lightbulb,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = suggestion,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mensaje personalizado OPCIONAL para la oferta
                        Text(
                            text = "Mensaje personalizado (opcional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var offerMessage by remember { mutableStateOf("") }
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(80.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Surface
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                if (offerMessage.isEmpty()) {
                                    Text(
                                        text = "Ej: Me interesa mucho, ¿podemos negociar?",
                                        color = TextMuted.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = offerMessage,
                                    onValueChange = { offerMessage = it },
                                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                                    cursorBrush = SolidColor(PrimaryPurple),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Botón enviar oferta
                        val validPrice = bargainPrice.toDoubleOrNull() ?: 0.0
                        val canSend = validPrice >= minBargainPrice
                        
                        Button(
                            onClick = {
                                if (canSend) {
                                    val fullMessage = if (offerMessage.isNotBlank()) {
                                        "💰 OFERTA: ¿Aceptarías $$bargainPrice por este producto?\n\n📝 Mensaje: $offerMessage"
                                    } else {
                                        "💰 OFERTA: ¿Aceptarías $$bargainPrice por este producto?"
                                    }
                                    onSendConsult(fullMessage)
                                    bargainPrice = ""
                                    offerMessage = ""
                                    showBargaining = false
                                    onDismiss()
                                }
                            },
                            enabled = canSend,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGreen,
                                disabledContainerColor = AccentGreen.copy(alpha = 0.3f)
                            )
                        ) {
                            Icon(Icons.Outlined.LocalOffer, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (canSend) "Enviar oferta de $$bargainPrice" else "Ingresa una oferta válida",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
