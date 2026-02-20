package com.rendly.app.ui.screens.checkout

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rendly.app.data.model.Order
import com.rendly.app.data.model.OrderStatus
import com.rendly.app.data.model.PaymentStatus
import com.rendly.app.data.model.PaymentResponse
import com.rendly.app.data.repository.CartRepository
import com.rendly.app.data.repository.CardPaymentRepository
import com.rendly.app.data.repository.MercadoPagoRepository
import com.rendly.app.data.repository.MPItem
import com.rendly.app.data.repository.OrderRepository
import com.rendly.app.ui.components.checkout.CardPaymentForm
import com.rendly.app.ui.theme.*
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * CHECKOUT SCREEN - Pantalla de pago con Mercado Pago
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Flujo de checkout profesional con soporte para Mercado Pago (Uruguay).
 * Incluye modo sandbox/test para pagos simulados.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

// Colores de Mercado Pago
private val MercadoPagoBlue = Color(0xFF009EE3)
private val MercadoPagoYellow = Color(0xFFFFE600)
private val MercadoPagoDark = Color(0xFF2D3277)

// Métodos de pago disponibles en Uruguay
enum class PaymentMethod(
    val id: String,
    val displayName: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isRecommended: Boolean = false
) {
    MERCADO_PAGO(
        id = "mercadopago",
        displayName = "Mercado Pago",
        description = "Paga con tu cuenta o tarjeta",
        icon = Icons.Filled.AccountBalanceWallet,
        color = MercadoPagoBlue,
        isRecommended = true
    ),
    CREDIT_CARD(
        id = "credit_card",
        displayName = "Tarjeta de crédito",
        description = "Visa, Mastercard, OCA, Diners",
        icon = Icons.Outlined.CreditCard,
        color = Color(0xFF1A1A2E)
    ),
    DEBIT_CARD(
        id = "debit_card",
        displayName = "Tarjeta de débito",
        description = "Visa Débito, Mastercard Débito",
        icon = Icons.Outlined.CreditCard,
        color = Color(0xFF4CAF50)
    ),
    BANK_TRANSFER(
        id = "bank_transfer",
        displayName = "Transferencia bancaria",
        description = "BROU, Santander, Itaú, Scotiabank",
        icon = Icons.Outlined.AccountBalance,
        color = Color(0xFF2196F3)
    ),
    PREX(
        id = "prex",
        displayName = "Prex",
        description = "Paga con tu cuenta Prex",
        icon = Icons.Outlined.Wallet,
        color = Color(0xFF0A3D62),
        isRecommended = true
    )
}

// Estados del checkout
sealed class CheckoutState {
    object Loading : CheckoutState()
    object SelectingPayment : CheckoutState()
    data class EnteringCardDetails(val orderId: String) : CheckoutState()
    object ProcessingPayment : CheckoutState()
    data class PaymentSuccess(val order: Order) : CheckoutState()
    data class ViewingReceipt(val order: Order) : CheckoutState()
    data class PaymentFailed(val error: String, val orderId: String? = null) : CheckoutState()
}

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onPaymentComplete: (Order) -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estados
    var checkoutState by remember { mutableStateOf<CheckoutState>(CheckoutState.SelectingPayment) }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(PaymentMethod.MERCADO_PAGO) }
    var selectedInstallments by remember { mutableIntStateOf(1) }
    var isCreatingOrder by remember { mutableStateOf(false) }
    
    // Datos del carrito
    val cartItems by CartRepository.cartItems.collectAsState()
    val subtotal = cartItems.sumOf { it.totalPrice }
    val shippingCost = if (subtotal >= 50000) 0.0 else 500.0
    val totalAmount = subtotal + shippingCost
    
    // Orden actual
    val currentOrder by OrderRepository.currentOrder.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
            .systemBarsPadding()
    ) {
        when (checkoutState) {
            is CheckoutState.Loading -> {
                CheckoutLoadingState()
            }
            
            is CheckoutState.SelectingPayment -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    CheckoutHeader(
                        title = "Checkout",
                        subtitle = "${cartItems.size} ${if (cartItems.size == 1) "artículo" else "artículos"}",
                        onBack = onBack
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Resumen de productos
                        item {
                            OrderSummaryCard(
                                cartItems = cartItems,
                                subtotal = subtotal,
                                shippingCost = shippingCost,
                                totalAmount = totalAmount
                            )
                        }
                        
                        // Métodos de pago
                        item {
                            PaymentMethodsSection(
                                selectedMethod = selectedPaymentMethod,
                                onMethodSelected = { selectedPaymentMethod = it }
                            )
                        }
                        
                        // Cuotas (solo para tarjetas)
                        if (selectedPaymentMethod == PaymentMethod.CREDIT_CARD) {
                            item {
                                InstallmentsSection(
                                    totalAmount = totalAmount,
                                    selectedInstallments = selectedInstallments,
                                    onInstallmentsSelected = { selectedInstallments = it }
                                )
                            }
                        }
                        
                        // Info de seguridad
                        item {
                            SecurityInfoCard()
                        }
                        
                        // Espacio para el botón
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                    
                    // Botón de pago
                    CheckoutBottomBar(
                        totalAmount = totalAmount,
                        isLoading = isCreatingOrder,
                        selectedMethod = selectedPaymentMethod,
                        onPay = {
                            if (selectedPaymentMethod == null) {
                                Toast.makeText(context, "Selecciona un método de pago", Toast.LENGTH_SHORT).show()
                                return@CheckoutBottomBar
                            }
                            
                            scope.launch {
                                isCreatingOrder = true
                                checkoutState = CheckoutState.ProcessingPayment
                                
                                // Crear orden en nuestra DB
                                val result = OrderRepository.createOrderFromCart(cartItems)
                                
                                result.fold(
                                    onSuccess = { order ->
                                        // Si es Mercado Pago, abrir el checkout de MP
                                        if (selectedPaymentMethod == PaymentMethod.MERCADO_PAGO) {
                                            // Mostrar formulario de tarjeta nativo (Checkout API)
                                            // No se necesita crear preferencia para pago directo con tarjeta
                                            CardPaymentRepository.resetState()
                                            checkoutState = CheckoutState.EnteringCardDetails(order.id)
                                            isCreatingOrder = false
                                        } else {
                                            // Otros métodos de pago (simulados por ahora)
                                            delay(2000)
                                            
                                            val paymentId = when (selectedPaymentMethod) {
                                                PaymentMethod.CREDIT_CARD -> "CARD-${System.currentTimeMillis()}"
                                                PaymentMethod.DEBIT_CARD -> "DEBIT-${System.currentTimeMillis()}"
                                                PaymentMethod.PREX -> "PREX-${System.currentTimeMillis()}"
                                                PaymentMethod.BANK_TRANSFER -> "TRANSFER-${System.currentTimeMillis()}"
                                                else -> "PAY-${System.currentTimeMillis()}"
                                            }
                                            
                                            OrderRepository.updatePaymentStatus(
                                                orderId = order.id,
                                                mpPaymentId = paymentId,
                                                status = PaymentStatus.APPROVED,
                                                statusDetail = "accredited",
                                                paymentMethodId = selectedPaymentMethod?.id,
                                                installments = selectedInstallments
                                            )
                                            
                                            CartRepository.clearCart()
                                            
                                            val updatedOrder = OrderRepository.getOrderById(order.id)
                                            checkoutState = CheckoutState.PaymentSuccess(updatedOrder ?: order)
                                            isCreatingOrder = false
                                        }
                                    },
                                    onFailure = { error ->
                                        checkoutState = CheckoutState.PaymentFailed(
                                            error.message ?: "Error procesando el pago"
                                        )
                                        isCreatingOrder = false
                                    }
                                )
                            }
                        }
                    )
                }
            }
            
            is CheckoutState.ProcessingPayment -> {
                PaymentProcessingState(
                    paymentMethod = selectedPaymentMethod
                )
            }
            
            is CheckoutState.EnteringCardDetails -> {
                val orderId = (checkoutState as CheckoutState.EnteringCardDetails).orderId
                // Resetear estado previo del repositorio al entrar
                LaunchedEffect(orderId) {
                    CardPaymentRepository.resetState()
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    CheckoutHeader(
                        title = "Datos de tarjeta",
                        subtitle = "Ingresá los datos de tu tarjeta",
                        onBack = { checkoutState = CheckoutState.SelectingPayment }
                    )
                    
                    // Obtener email del usuario logueado
                    val currentUserEmail = remember {
                        SupabaseClient.auth.currentUserOrNull()?.email ?: "comprador@Merqora.com"
                    }
                    
                    // Formulario de tarjeta nativo
                    CardPaymentForm(
                        totalAmount = totalAmount,
                        orderId = orderId,
                        payerEmail = currentUserEmail,
                        onPaymentSuccess = { paymentResponse ->
                            scope.launch {
                                // Actualizar estado del pago en nuestra DB
                                OrderRepository.updatePaymentStatus(
                                    orderId = orderId,
                                    mpPaymentId = paymentResponse.id.toString(),
                                    status = PaymentStatus.APPROVED,
                                    statusDetail = paymentResponse.statusDetail ?: "accredited",
                                    paymentMethodId = paymentResponse.paymentMethodId,
                                    installments = paymentResponse.installments ?: 1
                                )
                                
                                // Limpiar carrito
                                CartRepository.clearCart()
                                
                                // Obtener orden actualizada
                                val updatedOrder = OrderRepository.getOrderById(orderId)
                                if (updatedOrder != null) {
                                    checkoutState = CheckoutState.PaymentSuccess(updatedOrder)
                                }
                            }
                        },
                        onPaymentFailed = { errorMessage ->
                            checkoutState = CheckoutState.PaymentFailed(errorMessage, orderId)
                        },
                        onCancel = {
                            checkoutState = CheckoutState.SelectingPayment
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            is CheckoutState.PaymentSuccess -> {
                val order = (checkoutState as CheckoutState.PaymentSuccess).order
                PaymentSuccessState(
                    order = order,
                    onViewOrder = { checkoutState = CheckoutState.ViewingReceipt(order) },
                    onContinueShopping = {
                        OrderRepository.clearCurrentOrder()
                        onContinueShopping()
                    }
                )
            }
            
            is CheckoutState.ViewingReceipt -> {
                val order = (checkoutState as CheckoutState.ViewingReceipt).order
                OrderReceiptScreen(
                    order = order,
                    onBack = { checkoutState = CheckoutState.PaymentSuccess(order) },
                    onContinueShopping = {
                        OrderRepository.clearCurrentOrder()
                        onContinueShopping()
                    }
                )
            }
            
            is CheckoutState.PaymentFailed -> {
                val failedState = checkoutState as CheckoutState.PaymentFailed
                PaymentFailedState(
                    error = failedState.error,
                    onRetry = {
                        CardPaymentRepository.resetState()
                        if (failedState.orderId != null && selectedPaymentMethod == PaymentMethod.MERCADO_PAGO) {
                            checkoutState = CheckoutState.EnteringCardDetails(failedState.orderId)
                        } else {
                            checkoutState = CheckoutState.SelectingPayment
                        }
                    },
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun CheckoutHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Badge de seguridad
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentGreen.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Seguro",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    cartItems: List<CartRepository.CartItem>,
    subtotal: Double,
    shippingCost: Double,
    totalAmount: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen del pedido",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Items compactos
            cartItems.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = item.post.images.firstOrNull() ?: "",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.post.title.ifEmpty { "Producto" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Cant: ${item.quantity}" +
                                    (item.selectedColor?.let { " • $it" } ?: ""),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    
                    Text(
                        text = "$${String.format("%,.2f", item.totalPrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
            
            if (cartItems.size > 3) {
                Text(
                    text = "+${cartItems.size - 3} más",
                    fontSize = 12.sp,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = BorderSubtle
            )
            
            // Totales
            SummaryRow("Subtotal", subtotal)
            SummaryRow(
                label = "Envío",
                value = shippingCost,
                isFree = shippingCost == 0.0
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$${String.format("%,.2f", totalAmount)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PriceColor
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: Double,
    isFree: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        if (isFree) {
            Text(
                text = "GRATIS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
            )
        } else {
            Text(
                text = "$${String.format("%,.2f", value)}",
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun PaymentMethodsSection(
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Column {
        Text(
            text = "Método de pago",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PaymentMethod.entries.forEach { method ->
            PaymentMethodCard(
                method = method,
                isSelected = selectedMethod == method,
                onClick = { onMethodSelected(method) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) method.color else Color.Transparent,
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) method.color.copy(alpha = 0.08f) else SurfaceElevated
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(method.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = method.icon,
                    contentDescription = null,
                    tint = method.color,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = method.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (method.isRecommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Recomendado",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = method.description,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            
            // Radio button
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = method.color,
                    unselectedColor = TextMuted
                )
            )
        }
    }
}

@Composable
private fun InstallmentsSection(
    totalAmount: Double,
    selectedInstallments: Int,
    onInstallmentsSelected: (Int) -> Unit
) {
    val installmentOptions = listOf(1, 3, 6, 12)
    
    Column {
        Text(
            text = "Cuotas",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            installmentOptions.forEach { installments ->
                val monthlyPayment = totalAmount / installments
                val isSelected = selectedInstallments == installments
                
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onInstallmentsSelected(installments) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else SurfaceElevated,
                    border = if (isSelected) BorderStroke(2.dp, PrimaryPurple) else null
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${installments}x",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryPurple else TextPrimary
                        )
                        Text(
                            text = "$${String.format("%,.0f", monthlyPayment)}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        if (installments > 1) {
                            Text(
                                text = "sin interés",
                                fontSize = 9.sp,
                                color = AccentGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MercadoPagoBlue.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MercadoPagoBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = MercadoPagoBlue,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Compra Protegida",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tu pago está protegido por Mercado Pago. Recibe el producto o te devolvemos tu dinero.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CheckoutBottomBar(
    totalAmount: Double,
    isLoading: Boolean,
    selectedMethod: PaymentMethod?,
    onPay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total a pagar",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = "$${String.format("%,.2f", totalAmount)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onPay,
                enabled = !isLoading && selectedMethod != null,
                modifier = Modifier
                    .height(52.dp)
                    .widthIn(min = 160.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedMethod?.color ?: MercadoPagoBlue,
                    disabledContainerColor = TextMuted.copy(alpha = 0.3f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pagar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckoutLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryPurple)
    }
}

@Composable
private fun PaymentProcessingState(
    paymentMethod: PaymentMethod?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animación de carga
            val infiniteTransition = rememberInfiniteTransition(label = "processing")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                MercadoPagoBlue,
                                MercadoPagoBlue.copy(alpha = 0.3f),
                                MercadoPagoBlue
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(HomeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = null,
                        tint = MercadoPagoBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Procesando pago...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Conectando con ${paymentMethod?.displayName ?: "el procesador de pagos"}",
                fontSize = 14.sp,
                color = TextMuted
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MercadoPagoBlue,
                trackColor = MercadoPagoBlue.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun PaymentSuccessState(
    order: Order,
    onViewOrder: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de éxito
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "¡Pago exitoso!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tu compra ha sido procesada correctamente",
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Info de la orden
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceElevated
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Número de orden",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Text(
                            text = order.orderNumber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total pagado",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "$${String.format("%,.2f", order.totalAmount)} ${order.currency}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Artículos",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "${order.items.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de acción
            Button(
                onClick = onViewOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver mi pedido",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onContinueShopping,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, PrimaryPurple)
            ) {
                Text(
                    text = "Seguir comprando",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryPurple
                )
            }
        }
    }
}

@Composable
private fun OrderReceiptScreen(
    order: Order,
    onBack: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Header
        CheckoutHeader(
            title = "Comprobante de pago",
            subtitle = "Orden #${order.orderNumber}",
            onBack = onBack
        )
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado del pago
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AccentGreen.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AccentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pago aprobado",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                            Text(
                                text = order.formattedDate,
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
            
            // Datos de la orden
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Detalles del pedido",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ReceiptInfoRow("Número de orden", order.orderNumber)
                        ReceiptInfoRow("Estado", order.statusDisplayName)
                        ReceiptInfoRow("Fecha", order.formattedDate)
                        if (order.payment != null) {
                            val methodName = when (order.payment.paymentMethodId) {
                                "visa" -> "Visa"
                                "master" -> "Mastercard"
                                "amex" -> "American Express"
                                "oca" -> "OCA"
                                else -> order.payment.paymentMethodId ?: "Tarjeta"
                            }
                            ReceiptInfoRow("Método de pago", methodName)
                            if (order.payment.installments > 1) {
                                ReceiptInfoRow("Cuotas", "${order.payment.installments}x $${String.format("%,.2f", order.payment.amount / order.payment.installments)}")
                            }
                        }
                    }
                }
            }
            
            // Items
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Artículos (${order.items.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        order.items.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Imagen del producto
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (item.selectedColor != null) {
                                            Text(
                                                text = item.selectedColor,
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                        if (item.selectedSize != null) {
                                            Text(
                                                text = "Talle: ${item.selectedSize}",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Cant: ${item.quantity} × $${String.format("%,.2f", item.unitPrice)}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "$${String.format("%,.2f", item.totalPrice)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            if (index < order.items.size - 1) {
                                Divider(
                                    color = Color(0xFF2D2D3D),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Resumen de totales
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ReceiptInfoRow("Subtotal", "$${String.format("%,.2f", order.subtotal)}")
                        ReceiptInfoRow(
                            "Envío",
                            if (order.shippingCost <= 0) "Gratis" else "$${String.format("%,.2f", order.shippingCost)}"
                        )
                        if (order.discountAmount > 0) {
                            ReceiptInfoRow("Descuento", "-$${String.format("%,.2f", order.discountAmount)}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFF2D2D3D))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$${String.format("%,.2f", order.totalAmount)} ${order.currency}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen
                            )
                        }
                    }
                }
            }
            
            // Botón seguir comprando
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onContinueShopping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Seguir comprando",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun PaymentFailedState(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AccentPink.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = AccentPink,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Error en el pago",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Intentar de nuevo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(onClick = onBack) {
                Text(
                    text = "Volver al carrito",
                    fontSize = 14.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// Color del precio
private val PriceColor = Color(0xFF00D26A)

