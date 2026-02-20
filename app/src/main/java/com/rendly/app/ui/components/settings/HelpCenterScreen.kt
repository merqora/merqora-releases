package com.rendly.app.ui.components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.ui.theme.*
import com.rendly.app.data.repository.AISupportRepository
import io.github.jan.supabase.gotrue.auth
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun HelpCenterScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
    
    // Estados para pantallas de Categorías
    var showPurchasesHelp by remember { mutableStateOf(false) }
    var showSalesHelp by remember { mutableStateOf(false) }
    var showAccountHelp by remember { mutableStateOf(false) }
    var showPaymentsHelp by remember { mutableStateOf(false) }
    var showShippingHelp by remember { mutableStateOf(false) }
    var showSecurityHelp by remember { mutableStateOf(false) }
    
    // Estado para chat con soporte
    var showSupportChat by remember { mutableStateOf(false) }
    
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
                    title = "Centro de ayuda",
                    subtitle = "¿En qué podemos ayudarte?",
                    icon = Icons.Outlined.HelpOutline,
                    iconColor = Color(0xFF1565A0),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Barra de Búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Buscar ayuda...",
                                color = TextMuted.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // categorías de ayuda
                    Text(
                        text = "categorías",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HelpCategoryCard(
                            icon = Icons.Outlined.ShoppingBag,
                            title = "Compras",
                            color = Color(0xFF2E8B57),
                            modifier = Modifier.weight(1f),
                            onClick = { showPurchasesHelp = true }
                        )
                        HelpCategoryCard(
                            icon = Icons.Outlined.Sell,
                            title = "Ventas",
                            color = Color(0xFF1565A0),
                            modifier = Modifier.weight(1f),
                            onClick = { showSalesHelp = true }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HelpCategoryCard(
                            icon = Icons.Outlined.AccountCircle,
                            title = "Cuenta",
                            color = Color(0xFFFF6B35),
                            modifier = Modifier.weight(1f),
                            onClick = { showAccountHelp = true }
                        )
                        HelpCategoryCard(
                            icon = Icons.Outlined.Payment,
                            title = "Pagos",
                            color = Color(0xFFFF6B35),
                            modifier = Modifier.weight(1f),
                            onClick = { showPaymentsHelp = true }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HelpCategoryCard(
                            icon = Icons.Outlined.LocalShipping,
                            title = "envíos",
                            color = Color(0xFF2E8B57),
                            modifier = Modifier.weight(1f),
                            onClick = { showShippingHelp = true }
                        )
                        HelpCategoryCard(
                            icon = Icons.Outlined.Security,
                            title = "Seguridad",
                            color = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f),
                            onClick = { showSecurityHelp = true }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Preguntas frecuentes
                    Text(
                        text = "Preguntas frecuentes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val faqs = listOf(
                        "¿Cómo puedo crear una Publicación?" to "Para crear una Publicación, ve a la Pestaña 'Vender' en la barra de Navegación inferior. Toma fotos del producto, agrega Título, Descripción, precio y Categoría. Finalmente, pulsa 'Publicar'.",
                        "¿Cómo funciona el sistema de pagos?" to "Merqora utiliza pagos seguros a través de pasarelas certificadas. El dinero de tus ventas se deposita en tu billetera Merqora, desde donde puedes transferirlo a tu cuenta bancaria.",
                        "¿Puedo cancelar una compra?" to "Sí, puedes cancelar una compra antes de que el vendedor envíe el producto. Ve a 'Mis compras', selecciona el pedido y pulsa 'Cancelar'. Si ya fue enviado, deberás solicitar una Devolución.",
                        "¿Cómo verifico mi cuenta?" to "Ve a Configuración > Verificación y sigue los pasos. Necesitarás proporcionar un documento de identidad Válido y verificar tu Número de Teléfono.",
                        "¿Qué hago si tengo un problema con un producto?" to "Contacta primero al vendedor a través del chat. Si no se resuelve, abre una disputa desde 'Mis compras' > 'Reportar problema'. Nuestro equipo mediará para encontrar una Solución.",
                        "¿Cómo funcionan las Stories?" to "Las Stories son contenido efímero que desaparece en 24 horas. Puedes subir fotos o videos cortos para mostrar tus productos de forma dinámica. Aparecerán en la parte superior del feed.",
                        "¿Cuáles son las comisiones de venta?" to "Merqora cobra una pequeña comisión del 5% sobre cada venta completada. No hay costos de Publicación ni tarifas mensuales."
                    )
                    
                    faqs.forEachIndexed { index, (question, answer) ->
                        FaqItem(
                            question = question,
                            answer = answer,
                            isExpanded = expandedFaqIndex == index,
                            onClick = {
                                expandedFaqIndex = if (expandedFaqIndex == index) null else index
                            }
                        )
                        if (index < faqs.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    // Contacto directo
                    Text(
                        text = "¿Necesitas Más ayuda?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showSupportChat = true },
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryPurple.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(PrimaryPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SupportAgent,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chat con soporte",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Respuesta en menos de 24 horas",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = PrimaryPurple
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { },
                        shape = RoundedCornerShape(16.dp),
                        color = Surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF1565A0).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF1565A0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enviar email",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "soporte@Merqora.com",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
        
        // Pantallas de Categorías de ayuda
        PurchasesHelpScreen(isVisible = showPurchasesHelp, onDismiss = { showPurchasesHelp = false })
        SalesHelpScreen(isVisible = showSalesHelp, onDismiss = { showSalesHelp = false })
        AccountHelpScreen(isVisible = showAccountHelp, onDismiss = { showAccountHelp = false })
        PaymentsHelpScreen(isVisible = showPaymentsHelp, onDismiss = { showPaymentsHelp = false })
        ShippingHelpScreen(isVisible = showShippingHelp, onDismiss = { showShippingHelp = false })
        SecurityHelpScreen(isVisible = showSecurityHelp, onDismiss = { showSecurityHelp = false })
        
        // Chat con soporte
        SupportChatScreen(isVisible = showSupportChat, onDismiss = { showSupportChat = false })
    }
}

@Composable
private fun HelpCategoryCard(
    icon: ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = answer,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE AYUDA: COMPRAS
// ---------------------------------------------------------------
@Composable
private fun PurchasesHelpScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Compras",
                    subtitle = "Ayuda con tus compras",
                    icon = Icons.Outlined.ShoppingBag,
                    iconColor = Color(0xFF2E8B57),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection(
                        title = "¿Cómo realizar una compra?",
                        content = "1. Encuentra el producto que deseas\n2. Pulsa el Botón 'Comprar' o 'Agregar al carrito'\n3. Revisa tu carrito y procede al pago\n4. Selecciona método de pago y Dirección de Envío\n5. Confirma tu pedido",
                        icon = Icons.Outlined.ShoppingCart,
                        color = Color(0xFF2E8B57)
                    )
                    
                    HelpSection(
                        title = "Estados del pedido",
                        content = "• Pendiente: Esperando confirmación del vendedor\n• Confirmado: El vendedor aceptó tu pedido\n• Preparando: El producto está siendo preparado\n• Enviado: Tu pedido está en camino\n• Entregado: Recibiste tu pedido\n• Cancelado: El pedido fue cancelado",
                        icon = Icons.Outlined.LocalShipping,
                        color = Color(0xFF1565A0)
                    )
                    
                    HelpSection(
                        title = "¿Cómo cancelar una compra?",
                        content = "Puedes cancelar tu compra desde 'Mis compras' siempre que el vendedor no haya enviado el producto. Una vez enviado, deberás solicitar una Devolución.",
                        icon = Icons.Outlined.Cancel,
                        color = Color(0xFFEF4444)
                    )
                    
                    HelpSection(
                        title = "Protección al comprador",
                        content = "Todas las compras en Merqora están protegidas. Si el producto no llega o no coincide con la Descripción, puedes abrir una disputa y te devolveremos tu dinero.",
                        icon = Icons.Outlined.VerifiedUser,
                        color = Color(0xFFFF6B35)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE AYUDA: VENTAS
// ---------------------------------------------------------------
@Composable
private fun SalesHelpScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Ventas",
                    subtitle = "Ayuda para vendedores",
                    icon = Icons.Outlined.Sell,
                    iconColor = Color(0xFF1565A0),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection(
                        title = "¿Cómo publicar un producto?",
                        content = "1. Pulsa el Botón '+' en la barra de Navegación\n2. Selecciona 'Publicación'\n3. Toma o selecciona fotos del producto\n4. Agrega Título, Descripción y precio\n5. Selecciona Categoría y Condición\n6. Publica y listo!",
                        icon = Icons.Outlined.AddBox,
                        color = Color(0xFF2E8B57)
                    )
                    
                    HelpSection(
                        title = "Comisiones de venta",
                        content = "Merqora cobra una comisión del 5% sobre cada venta completada. No hay costos de Publicación, tarifas mensuales ni cargos ocultos.",
                        icon = Icons.Outlined.Percent,
                        color = Color(0xFFFF6B35)
                    )
                    
                    HelpSection(
                        title = "¿Cómo recibir pagos?",
                        content = "El dinero de tus ventas se deposita en tu billetera Merqora. Desde allí puedes transferirlo a tu cuenta bancaria en cualquier momento.",
                        icon = Icons.Outlined.AccountBalance,
                        color = Color(0xFF1565A0)
                    )
                    
                    HelpSection(
                        title = "Tips para vender Más",
                        content = "• Usa fotos de alta calidad con buena Iluminación\n• Escribe descripciones detalladas\n• Responde Rápido a los mensajes\n• Ofrece Envío gratis cuando sea posible\n• Mantén precios competitivos",
                        icon = Icons.Outlined.TipsAndUpdates,
                        color = Color(0xFFFF6B35)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE AYUDA: CUENTA
// ---------------------------------------------------------------
@Composable
private fun AccountHelpScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Cuenta",
                    subtitle = "Gestiona tu cuenta",
                    icon = Icons.Outlined.AccountCircle,
                    iconColor = Color(0xFFFF6B35),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection(
                        title = "Editar perfil",
                        content = "Desde tu perfil, pulsa 'Editar perfil' para cambiar tu foto, nombre, Descripción, Ubicación y redes sociales.",
                        icon = Icons.Outlined.Edit,
                        color = Color(0xFFFF6B35)
                    )
                    
                    HelpSection(
                        title = "Verificar cuenta",
                        content = "Verifica tu cuenta para obtener la insignia azul y generar Más confianza. Ve a Configuración > Verificación y sigue los pasos.",
                        icon = Icons.Outlined.Verified,
                        color = Color(0xFF1565A0)
                    )
                    
                    HelpSection(
                        title = "Cambiar Contraseña",
                        content = "Ve a Configuración > Seguridad > Cambiar Contraseña. Necesitarás tu Contraseña actual para realizar el cambio.",
                        icon = Icons.Outlined.Lock,
                        color = Color(0xFF2E8B57)
                    )
                    
                    HelpSection(
                        title = "Eliminar cuenta",
                        content = "Si deseas eliminar tu cuenta permanentemente, contacta a soporte. Ten en cuenta que esta Acción es irreversible y perderás todo tu historial.",
                        icon = Icons.Outlined.DeleteForever,
                        color = Color(0xFFEF4444)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE AYUDA: PAGOS
// ---------------------------------------------------------------
@Composable
private fun PaymentsHelpScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Pagos",
                    subtitle = "Métodos de pago y billetera",
                    icon = Icons.Outlined.Payment,
                    iconColor = Color(0xFFFF6B35),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection(
                        title = "Métodos de pago aceptados",
                        content = "• Tarjetas de crédito y débito (Visa, Mastercard, Amex)\n• Transferencia bancaria\n• Billetera Merqora\n• MercadoPago\n• Efectivo (acordar con vendedor)",
                        icon = Icons.Outlined.CreditCard,
                        color = Color(0xFF1565A0)
                    )
                    
                    HelpSection(
                        title = "Billetera Merqora",
                        content = "Tu billetera Merqora almacena el dinero de tus ventas. Puedes usarlo para comprar en la plataforma o transferirlo a tu cuenta bancaria sin comisión.",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        color = Color(0xFF2E8B57)
                    )
                    
                    HelpSection(
                        title = "¿Cómo agregar método de pago?",
                        content = "1. Ve a tu perfil > Configuración\n2. Selecciona 'Métodos de pago'\n3. Pulsa 'Agregar método'\n4. Ingresa los datos de tu tarjeta o cuenta",
                        icon = Icons.Outlined.AddCard,
                        color = Color(0xFFFF6B35)
                    )
                    
                    HelpSection(
                        title = "Reembolsos",
                        content = "Los reembolsos se procesan al mismo método de pago utilizado. El tiempo de acreditación depende de tu banco (3-15 Días hábiles).",
                        icon = Icons.Outlined.Replay,
                        color = Color(0xFFFF6B35)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE AYUDA: envíoS
// ---------------------------------------------------------------
@Composable
private fun ShippingHelpScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "envíos",
                    subtitle = "Información de entregas",
                    icon = Icons.Outlined.LocalShipping,
                    iconColor = Color(0xFF2E8B57),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection(
                        title = "Opciones de Envío",
                        content = "• envío estándar: 3-7 Días hábiles\n• envío express: 1-3 Días hábiles\n• Retiro en persona: Acordar con vendedor\n• envío gratis: Disponible en productos seleccionados",
                        icon = Icons.Outlined.Inventory,
                        color = Color(0xFF2E8B57)
                    )
                    
                    HelpSection(
                        title = "¿Cómo rastrear mi pedido?",
                        content = "1. Ve a 'Mis compras'\n2. Selecciona el pedido\n3. Pulsa 'Rastrear Envío'\n4. Verás el estado actual y Ubicación del paquete",
                        icon = Icons.Outlined.LocationOn,
                        color = Color(0xFF1565A0)
                    )
                    
                    HelpSection(
                        title = "Mi paquete no llegó",
                        content = "Si han pasado Más Días de los estimados:\n1. Verifica el tracking\n2. Contacta al vendedor\n3. Si no hay respuesta, abre una disputa desde 'Mis compras'",
                        icon = Icons.Outlined.Report,
                        color = Color(0xFFEF4444)
                    )
                    
                    HelpSection(
                        title = "Devoluciones",
                        content = "Tienes 7 Días después de recibir el producto para solicitar una Devolución. El producto debe estar en las mismas condiciones en que lo recibiste.",
                        icon = Icons.Outlined.AssignmentReturn,
                        color = Color(0xFF2E8B57)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// PANTALLA DE AYUDA: SEGURIDAD
// ---------------------------------------------------------------
@Composable
private fun SecurityHelpScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Seguridad",
                    subtitle = "Protege tu cuenta",
                    icon = Icons.Outlined.Security,
                    iconColor = Color(0xFFEF4444),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HelpSection(
                        title = "Autenticación de dos factores",
                        content = "Activa la Verificación en dos pasos para mayor seguridad. Recibirás un Código por SMS o email cada vez que inicies Sesión.",
                        icon = Icons.Outlined.PhonelinkLock,
                        color = Color(0xFF2E8B57)
                    )
                    
                    HelpSection(
                        title = "contraseña segura",
                        content = "Tu Contraseña debe tener:\n• mínimo 8 caracteres\n• Al menos una mayúscula\n• Al menos un Número\n• Al menos un símbolo especial",
                        icon = Icons.Outlined.Lock,
                        color = Color(0xFF1565A0)
                    )
                    
                    HelpSection(
                        title = "Actividad sospechosa",
                        content = "Si detectas actividad inusual en tu cuenta:\n1. Cambia tu Contraseña inmediatamente\n2. Revisa tus sesiones activas\n3. Contacta a soporte\n4. Activa la Verificación en dos pasos",
                        icon = Icons.Outlined.Warning,
                        color = Color(0xFFFF6B35)
                    )
                    
                    HelpSection(
                        title = "Reportar fraude",
                        content = "Si detectas un vendedor o comprador fraudulento, repórtalo inmediatamente. Ve al perfil del usuario > Menú > Reportar. Nuestro equipo lo investigará.",
                        icon = Icons.Outlined.Report,
                        color = Color(0xFFEF4444)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------
// COMPONENTE: SEcción DE AYUDA
// ---------------------------------------------------------------
@Composable
private fun HelpSection(
    title: String,
    content: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = content,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )
        }
    }
}

// ---------------------------------------------------------------
// CHAT CON SOPORTE - Pantalla completa de chat personalizado
// ---------------------------------------------------------------
@Composable
private fun SupportChatScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        SupportMessage(
            id = "welcome",
            content = "¡Hola! Soy el asistente virtual de Merqora. Estoy aquí para ayudarte con cualquier consulta sobre compras, ventas, pagos, envíos y más.\n\n¿En qué puedo ayudarte hoy?",
            isFromSupport = true,
            timestamp = System.currentTimeMillis()
        )
    )) }
    var isTyping by remember { mutableStateOf(false) }
    var isAgentTyping by remember { mutableStateOf(false) }
    var lastMessageId by remember { mutableStateOf("") }
    var isEscalated by remember { mutableStateOf(false) }
    var currentConversationId by remember { mutableStateOf<String?>(null) }
    var isConversationClosed by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(0) }
    var ratingComment by remember { mutableStateOf("") }
    
    // Get current user ID
    val userId = remember {
        SupabaseClient.client.auth.currentUserOrNull()?.id ?: "anonymous"
    }
    
    // Suscribirse a mensajes del agente humano en tiempo real
    // Re-subscribe when escalated OR when conversation_id becomes available
    LaunchedEffect(isEscalated, currentConversationId) {
        if (!isEscalated) return@LaunchedEffect
        
        // Use conversation_id (real UUID from DB) for realtime subscription
        val conversationId = currentConversationId
            ?: AISupportRepository.getCurrentConversationId() 
            ?: AISupportRepository.getCurrentSessionId() 
            ?: return@LaunchedEffect
        
        try {
            val realtime = SupabaseClient.client.realtime
            try {
                realtime.connect()
                delay(500)
            } catch (e: Exception) {
                // Ignorar si ya está conectado
            }
            
            // Channel name matches what admin-web uses
            val channelName = "support-$conversationId"
            val channel = SupabaseClient.client.channel(channelName)
            
            android.util.Log.d("SupportChat", "Suscribiendo a canal: $channelName")
            
            // Escuchar nuevos mensajes del agente humano
            val messageFlow = channel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = "support_messages"
            }
            
            // Escuchar eventos de typing del agente
            val typingFlow = channel.broadcastFlow<Map<String, Any>>("agent_typing")
            
            // Procesar mensajes entrantes
            messageFlow.onEach { change ->
                try {
                    val record = change.record
                    val msgConversationId = record["conversation_id"]?.toString()?.trim('"') ?: ""
                    val msgRole = record["role"]?.toString()?.trim('"') ?: ""
                    val msgContent = record["content"]?.toString()?.trim('"') ?: ""
                    val msgId = record["id"]?.toString()?.trim('"') ?: ""
                    
                    android.util.Log.d("SupportChat", "Mensaje recibido - convId: $msgConversationId, role: $msgRole, myConvId: $conversationId")
                    
                    // Mensajes del agente humano o del sistema para esta conversación
                    if (msgConversationId == conversationId && (msgRole == "human_support" || msgRole == "system")) {
                        // Si es mensaje especial de rating, NO agregarlo como mensaje visible
                        // Solo mostrar el rating interactivo
                        if (msgRole == "system" && msgContent.contains("__RATING_REQUEST__")) {
                            isConversationClosed = true
                            // Agregar solo el mensaje interactivo de calificación (sin el __RATING_REQUEST__)
                            val ratingMsg = SupportMessage(
                                id = "rating_${System.currentTimeMillis()}",
                                content = "? Tu consulta ha sido resuelta\n\n¿Cómo calificarías la ayuda recibida?",
                                isFromSupport = true,
                                timestamp = System.currentTimeMillis(),
                                isSystemMessage = true,
                                isRatingRequest = true
                            )
                            messages = messages + ratingMsg
                            isAgentTyping = false
                            android.util.Log.d("SupportChat", "? Conversación cerrada, mostrando rating interactivo")
                            return@onEach
                        }
                        
                        // Evitar duplicados para mensajes normales
                        if (messages.none { it.id == msgId }) {
                            val isSystem = msgRole == "system"
                            val supportMessage = SupportMessage(
                                id = msgId,
                                content = msgContent,
                                isFromSupport = true,
                                timestamp = System.currentTimeMillis(),
                                isFromAgent = !isSystem,
                                isSystemMessage = isSystem
                            )
                            messages = messages + supportMessage
                            isAgentTyping = false
                            
                            android.util.Log.d("SupportChat", "? Mensaje ${if (isSystem) "del sistema" else "del agente"} agregado: ${msgContent.take(50)}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SupportChat", "Error procesando mensaje: ${e.message}")
                }
            }.launchIn(this)
            
            // Procesar eventos de typing
            typingFlow.onEach { payload ->
                try {
                    val typing = payload["is_typing"]?.toString()?.toBoolean() ?: false
                    isAgentTyping = typing
                    
                    // Auto-reset después de 3 segundos
                    if (typing) {
                        delay(3000)
                        if (isAgentTyping) {
                            isAgentTyping = false
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SupportChat", "Error typing: ${e.message}")
                }
            }.launchIn(this)
            
            channel.subscribe(blockUntilSubscribed = true)
            android.util.Log.d("SupportChat", "? Suscrito a realtime: $channelName (conversationId: $conversationId)")
            
        } catch (e: Exception) {
            android.util.Log.e("SupportChat", "Error suscribiendo realtime: ${e.message}")
        }
    }
    
    // Function to send message to AI
    fun sendMessageToAI(userMessage: String) {
        scope.launch {
            isTyping = true
            
            when (val response = AISupportRepository.sendMessage(userId, userMessage)) {
                is AISupportRepository.AIResponse.Success -> {
                    lastMessageId = response.response.message_id
                    
                    // Save conversation_id for realtime subscription
                    response.response.conversation_id?.let { convId ->
                        currentConversationId = convId
                        android.util.Log.d("SupportChat", "Conversation ID guardado: $convId")
                    }
                    
                    val supportResponse = SupportMessage(
                        id = response.response.message_id,
                        content = response.response.content,
                        isFromSupport = true,
                        timestamp = System.currentTimeMillis(),
                        actionButtons = response.response.action_buttons
                    )
                    messages = messages + supportResponse
                    
                    // If escalated, show additional message and enable realtime
                    if (response.response.escalated) {
                        isEscalated = true
                        delay(500)
                        val escalationNote = SupportMessage(
                            id = "escalation_${System.currentTimeMillis()}",
                            content = "Tu consulta ha sido registrada. Un agente humano te contactará pronto. Recibirás sus mensajes en tiempo real.",
                            isFromSupport = true,
                            timestamp = System.currentTimeMillis()
                        )
                        messages = messages + escalationNote
                    }
                }
                is AISupportRepository.AIResponse.Offline -> {
                    val offlineResponse = SupportMessage(
                        id = "offline_${System.currentTimeMillis()}",
                        content = response.fallbackResponse,
                        isFromSupport = true,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + offlineResponse
                }
                is AISupportRepository.AIResponse.Error -> {
                    val errorResponse = SupportMessage(
                        id = "error_${System.currentTimeMillis()}",
                        content = "Lo siento, hubo un error al procesar tu mensaje. Por favor, intenta de nuevo.",
                        isFromSupport = true,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + errorResponse
                }
            }
            isTyping = false
        }
    }
    
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    // Scroll al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp),
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header del chat con soporte
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HomeBg,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary
                            )
                        }
                        
                        // Avatar de soporte con gradiente Merqora
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(PrimaryPurple, AccentPink)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SupportAgent,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Soporte Merqora",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verificado",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = when {
                                    isAgentTyping -> "Agente escribiendo..."
                                    isTyping -> "IA procesando..."
                                    isEscalated -> "Conectado con agente"
                                    else -> "En línea - Respuesta Rápida"
                                },
                                color = when {
                                    isAgentTyping -> Color(0xFF2196F3)
                                    isTyping -> PrimaryPurple
                                    isEscalated -> Color(0xFF4CAF50)
                                    else -> Color(0xFF4CAF50)
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                // Quick actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip("Problema con pedido") {
                        val userMsg = SupportMessage(
                            id = "user_${System.currentTimeMillis()}",
                            content = "Tengo un problema con mi pedido",
                            isFromSupport = false,
                            timestamp = System.currentTimeMillis()
                        )
                        messages = messages + userMsg
                        sendMessageToAI("Tengo un problema con mi pedido")
                    }
                    QuickActionChip("Reembolso") {
                        val userMsg = SupportMessage(
                            id = "user_${System.currentTimeMillis()}",
                            content = "¿Cómo funciona el reembolso?",
                            isFromSupport = false,
                            timestamp = System.currentTimeMillis()
                        )
                        messages = messages + userMsg
                        sendMessageToAI("¿Cómo funciona el reembolso?")
                    }
                }
                
                // Lista de mensajes
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Detectar índice del primer mensaje del agente para mostrar banner
                    val firstAgentMessageIndex = messages.indexOfFirst { it.isFromAgent }
                    
                    items(messages.size) { index ->
                        val message = messages[index]
                        
                        // Mostrar banner cuando aparece el primer mensaje del agente
                        if (index == firstAgentMessageIndex) {
                            AgentHandoffBanner()
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Si es mensaje de rating, mostrar con interfaz inline
                        if (message.isRatingRequest) {
                            RatingMessageBubble(
                                message = message,
                                selectedRating = selectedRating,
                                ratingComment = ratingComment,
                                onRatingChange = { rating ->
                                    selectedRating = rating
                                },
                                onCommentChange = { comment ->
                                    ratingComment = comment
                                },
                                onSubmit = {
                                    scope.launch {
                                        val success = AISupportRepository.saveUserRating(
                                            conversationId = currentConversationId ?: "",
                                            userId = userId,
                                            rating = selectedRating,
                                            feedbackText = ratingComment.ifEmpty { "Calificación: $selectedRating/5 estrellas" }
                                        )
                                        
                                        if (success) {
                                            // Generar Número de ticket único
                                            val ticketNumber = "TKT-${System.currentTimeMillis().toString().takeLast(8)}"
                                            
                                            // Mensaje de cierre con ticket
                                            val closureMsg = SupportMessage(
                                                id = "closure_${System.currentTimeMillis()}",
                                                content = """
                                                    |¡Gracias por tu calificación!
                                                    |
                                                    |??????????????????????
                                                    |?? TICKET: $ticketNumber
                                                    |? Calificación: $selectedRating/5
                                                    |?? ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}
                                                    |??????????????????????
                                                    |
                                                    |Tu conversación ha sido archivada.
                                                    |Guarda este Número de ticket para futuras referencias.
                                                """.trimMargin(),
                                                isFromSupport = true,
                                                timestamp = System.currentTimeMillis(),
                                                isSystemMessage = true,
                                                isTicketClosure = true
                                            )
                                            
                                            // Remover el mensaje de rating y agregar el de cierre
                                            messages = messages.filter { !it.isRatingRequest } + closureMsg
                                            
                                            android.util.Log.d("SupportChat", "? Ticket cerrado: $ticketNumber con $selectedRating?")
                                        }
                                        
                                        selectedRating = 0
                                        ratingComment = ""
                                    }
                                }
                            )
                        } else {
                            // Mensaje normal
                            SupportMessageBubble(
                                message = message,
                                onActionClick = { button ->
                                    when (button.action) {
                                        "navigate" -> {
                                            android.util.Log.d("SupportChat", "Navigate to: ${button.target}")
                                        }
                                        "open_url" -> {
                                            android.util.Log.d("SupportChat", "Open URL: ${button.target}")
                                        }
                                        "call_function" -> {
                                            when (button.target) {
                                                "escalate_to_human" -> {
                                                    sendMessageToAI("Quiero hablar con un agente humano")
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    if (isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
                
                // Footer: input de mensaje o banner de conversación cerrada
                if (isConversationClosed) {
                    // Banner de conversación cerrada con Botón de nueva conversación
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Conversación cerrada",
                                    color = Color(0xFF2E8B57),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // botón para nueva conversación
                            Button(
                                onClick = {
                                    // Reiniciar todo para nueva conversación
                                    messages = listOf(
                                        SupportMessage(
                                            id = "welcome_new",
                                            content = "¡Hola de nuevo! ¿En qué puedo ayudarte hoy?",
                                            isFromSupport = true,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    isConversationClosed = false
                                    isEscalated = false
                                    currentConversationId = null
                                    selectedRating = 0
                                    ratingComment = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nueva conversación",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    // Input normal
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Campo de texto
                            BasicTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(HomeBg)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(PrimaryPurple),
                                decorationBox = { innerTextField ->
                                    if (messageText.isEmpty()) {
                                        Text(
                                            text = "Escribe tu mensaje...",
                                            color = TextMuted,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // botón enviar
                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        val userMessage = messageText
                                        val userMsg = SupportMessage(
                                            id = "user_${System.currentTimeMillis()}",
                                            content = userMessage,
                                            isFromSupport = false,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        messages = messages + userMsg
                                        messageText = ""
                                        
                                        // Si está escalado, enviar directo a Supabase (no a la IA)
                                        if (isEscalated && currentConversationId != null) {
                                            scope.launch {
                                                val success = AISupportRepository.sendDirectMessage(
                                                    currentConversationId!!,
                                                    userMessage
                                                )
                                                if (!success) {
                                                    // Mostrar error si falla
                                                    val errorMsg = SupportMessage(
                                                        id = "error_${System.currentTimeMillis()}",
                                                        content = "Error al enviar mensaje. Intenta de nuevo.",
                                                        isFromSupport = true,
                                                        timestamp = System.currentTimeMillis(),
                                                        isFromAgent = false
                                                    )
                                                    messages = messages + errorMsg
                                                }
                                            }
                                        } else {
                                            // Send to AI support
                                            sendMessageToAI(userMessage)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (messageText.isNotBlank()) PrimaryPurple else PrimaryPurple.copy(alpha = 0.3f)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Enviar",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class SupportMessage(
    val id: String,
    val content: String,
    val isFromSupport: Boolean,
    val timestamp: Long,
    val actionButtons: List<AISupportRepository.ActionButton> = emptyList(),
    val isFromAgent: Boolean = false, // true = agente humano, false = IA
    val isSystemMessage: Boolean = false, // true = mensaje del sistema
    val isRatingRequest: Boolean = false, // true = mensaje interactivo de calificación
    val isTicketClosure: Boolean = false // true = mensaje de cierre con Número de ticket
)

@Composable
private fun SupportMessageBubble(
    message: SupportMessage,
    onActionClick: (AISupportRepository.ActionButton) -> Unit = {}
) {
    // Colores diferenciados para IA vs Agente Humano
    val bubbleColor = when {
        !message.isFromSupport -> PrimaryPurple // Usuario
        message.isFromAgent -> Color(0xFF2196F3).copy(alpha = 0.15f) // Agente humano - azul
        else -> Surface // IA - gris
    }
    val textColor = if (message.isFromSupport) TextPrimary else Color.White
    val alignment = if (message.isFromSupport) Alignment.CenterStart else Alignment.CenterEnd
    
    // Colores del avatar
    val avatarColors = if (message.isFromAgent) {
        listOf(Color(0xFF2196F3), Color(0xFF1976D2)) // Azul para agente
    } else {
        listOf(PrimaryPurple, AccentPink) // Morado/rosa para IA
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (message.isFromSupport) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = avatarColors)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (message.isFromAgent) 
                                Icons.Filled.Person 
                            else 
                                Icons.Outlined.SmartToy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Column {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isFromSupport) 4.dp else 16.dp,
                            bottomEnd = if (message.isFromSupport) 16.dp else 4.dp
                        ),
                        color = bubbleColor,
                        border = if (message.isFromAgent) 
                            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.3f))
                        else null
                    ) {
                        Text(
                            text = message.content,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
            
            // Action Buttons
            if (message.actionButtons.isNotEmpty() && message.isFromSupport) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .padding(start = 36.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    message.actionButtons.forEach { button ->
                        ActionButtonChip(
                            button = button,
                            onClick = { onActionClick(button) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentHandoffBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2196F3).copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Un agente se ha unido al chat",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1976D2)
                )
                Text(
                    text = "Ahora estás hablando con soporte humano",
                    fontSize = 11.sp,
                    color = Color(0xFF2196F3).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun RatingMessageBubble(
    message: SupportMessage,
    selectedRating: Int,
    ratingComment: String,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val bubbleColor = Surface
    val textColor = TextPrimary
    val avatarColors = listOf(PrimaryPurple, AccentPink)
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(colors = avatarColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    ),
                    color = bubbleColor
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = message.content,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        RatingInlineInterface(
                            selectedRating = selectedRating,
                            comment = ratingComment,
                            onRatingChange = onRatingChange,
                            onCommentChange = onCommentChange,
                            onSubmit = onSubmit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonChip(
    button: AISupportRepository.ActionButton,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = PrimaryPurple.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
    ) {
        Text(
            text = button.label,
            color = PrimaryPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun QuickActionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = PrimaryPurple.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            color = PrimaryPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryPurple, AccentPink)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SupportAgent,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Surface
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TextMuted.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

private fun simulateSupportResponse(
    response: String,
    currentMessages: List<SupportMessage>,
    onComplete: (List<SupportMessage>) -> Unit
) {
    kotlinx.coroutines.GlobalScope.launch {
        delay(1500)
        val supportResponse = SupportMessage(
            id = "support_${System.currentTimeMillis()}",
            content = response,
            isFromSupport = true,
            timestamp = System.currentTimeMillis()
        )
        onComplete(currentMessages + supportResponse)
    }
}
