package com.rendly.app.ui.components.checkout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.model.*
import com.rendly.app.data.repository.CardPaymentRepository
import com.rendly.app.data.repository.CardPaymentState
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * CARD PAYMENT FORM - Formulario nativo de pago con tarjeta
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * UI nativa en Compose para ingresar datos de tarjeta.
 * Incluye detección automática de tipo de tarjeta, formateo, validación,
 * y selector de cuotas.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

// Colores
private val CardGradientStart = Color(0xFF1A1A2E)
private val CardGradientEnd = Color(0xFF16213E)
private val VisaBlue = Color(0xFF1A1F71)
private val MastercardRed = Color(0xFFEB001B)
private val MastercardOrange = Color(0xFFF79E1B)
private val AmexBlue = Color(0xFF006FCF)
private val FieldBg = Color(0xFF1E1E2E)
private val FieldBorder = Color(0xFF2D2D3D)
private val FieldBorderFocused = Color(0xFF6C63FF)
private val ErrorRed = Color(0xFFFF6B6B)

@Composable
fun CardPaymentForm(
    totalAmount: Double,
    orderId: String,
    payerEmail: String,
    onPaymentSuccess: (PaymentResponse) -> Unit,
    onPaymentFailed: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    // Estados del formulario con TextFieldValue para control de cursor
    var cardNumber by remember { mutableStateOf(TextFieldValue("")) }
    var cardholderName by remember { mutableStateOf(TextFieldValue("")) }
    var expirationDate by remember { mutableStateOf(TextFieldValue("")) }
    var cvv by remember { mutableStateOf(TextFieldValue("")) }
    var identificationNumber by remember { mutableStateOf(TextFieldValue("")) }
    var selectedInstallments by remember { mutableIntStateOf(1) }
    
    // Estados de validación
    var cardNumberError by remember { mutableStateOf<String?>(null) }
    var expirationError by remember { mutableStateOf<String?>(null) }
    var cvvError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var idError by remember { mutableStateOf<String?>(null) }
    
    // Tipo de tarjeta detectado
    val cardType = remember(cardNumber.text) { detectCardType(cardNumber.text) }
    
    // Estado del pago
    val paymentState by CardPaymentRepository.paymentState.collectAsState()
    val availableInstallments by CardPaymentRepository.installments.collectAsState()
    
    // Cargar cuotas cuando cambia el BIN
    LaunchedEffect(cardNumber.text) {
        val cleanNumber = cardNumber.text.replace(" ", "")
        if (cleanNumber.length >= 6) {
            CardPaymentRepository.getInstallments(
                amount = totalAmount,
                bin = cleanNumber.take(6)
            )
        }
    }
    
    // Manejar resultado del pago
    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is CardPaymentState.PaymentSuccess -> {
                onPaymentSuccess(state.response)
            }
            is CardPaymentState.PaymentRejected -> {
                onPaymentFailed(state.reason)
            }
            is CardPaymentState.Error -> {
                onPaymentFailed(state.message)
            }
            else -> {}
        }
    }
    
    val isProcessing = paymentState is CardPaymentState.TokenizingCard || 
                       paymentState is CardPaymentState.ProcessingPayment
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vista previa de la tarjeta
        CardPreview(
            cardNumber = cardNumber.text,
            cardholderName = cardholderName.text,
            expirationDate = expirationDate.text,
            cardType = cardType
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Número de tarjeta
        CardTextField(
            value = cardNumber,
            onValueChange = { newValue ->
                val cleaned = newValue.text.filter { it.isDigit() }
                if (cleaned.length <= cardType.cardNumberLength) {
                    val formatted = formatCardNumber(cleaned, cardType)
                    cardNumber = TextFieldValue(formatted, TextRange(formatted.length))
                    cardNumberError = null
                }
            },
            label = "Número de tarjeta",
            placeholder = "1234 5678 9012 3456",
            leadingIcon = {
                CardTypeIcon(cardType)
            },
            trailingIcon = if (cardNumber.text.isNotEmpty() && isValidCardNumber(cardNumber.text)) {
                {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            error = cardNumberError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        // Nombre del titular
        CardTextField(
            value = cardholderName,
            onValueChange = { newValue ->
                val upper = newValue.text.uppercase()
                cardholderName = TextFieldValue(upper, TextRange(upper.length))
                nameError = null
            },
            label = "Nombre del titular",
            placeholder = "NOMBRE APELLIDO",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            error = nameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        
        // Fecha de vencimiento y CVV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fecha de vencimiento
            CardTextField(
                value = expirationDate,
                onValueChange = { newValue ->
                    val cleaned = newValue.text.filter { it.isDigit() }
                    if (cleaned.length <= 4) {
                        val formatted = when {
                            cleaned.length <= 2 -> cleaned
                            else -> "${cleaned.take(2)}/${cleaned.drop(2)}"
                        }
                        expirationDate = TextFieldValue(formatted, TextRange(formatted.length))
                        expirationError = null
                    }
                },
                label = "Vencimiento",
                placeholder = "MM/AA",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                error = expirationError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Right) }
                ),
                modifier = Modifier.weight(1f)
            )
            
            // CVV
            CardTextField(
                value = cvv,
                onValueChange = { newValue ->
                    val cleaned = newValue.text.filter { it.isDigit() }
                    if (cleaned.length <= cardType.cvvLength) {
                        cvv = TextFieldValue(cleaned, TextRange(cleaned.length))
                        cvvError = null
                    }
                },
                label = "CVV",
                placeholder = if (cardType == CardType.AMEX) "1234" else "123",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                error = cvvError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.weight(1f)
            )
        }
        
        // Documento de identidad
        CardTextField(
            value = identificationNumber,
            onValueChange = { newValue ->
                val cleaned = newValue.text.filter { it.isDigit() || it == '-' || it == '.' }
                if (cleaned.length <= 12) {
                    identificationNumber = TextFieldValue(cleaned, TextRange(cleaned.length))
                    idError = null
                }
            },
            label = "Cédula de identidad",
            placeholder = "12345678",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Badge,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            error = idError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )
        
        // Selector de cuotas
        if (availableInstallments.isNotEmpty()) {
            InstallmentsSelector(
                installments = availableInstallments,
                selectedInstallments = selectedInstallments,
                onSelect = { selectedInstallments = it }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Info de seguridad
        SecurityBadge()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón de pago
        Button(
            onClick = {
                // Validar campos
                var hasErrors = false
                
                if (!isValidCardNumber(cardNumber.text)) {
                    cardNumberError = "Número de tarjeta inválido"
                    hasErrors = true
                }
                
                val (month, year) = parseExpirationDate(expirationDate.text)
                if (!isValidExpirationDate(month, year)) {
                    expirationError = "Fecha inválida"
                    hasErrors = true
                }
                
                if (!isValidCVV(cvv.text, cardType)) {
                    cvvError = "CVV inválido"
                    hasErrors = true
                }
                
                if (cardholderName.text.length < 3) {
                    nameError = "Ingresa el nombre completo"
                    hasErrors = true
                }
                
                if (identificationNumber.text.length < 6) {
                    idError = "Cédula inválida"
                    hasErrors = true
                }
                
                if (hasErrors) return@Button
                
                // Procesar pago
                scope.launch {
                    // 1. Tokenizar tarjeta
                    val tokenResult = CardPaymentRepository.createCardToken(
                        cardNumber = cardNumber.text,
                        expirationMonth = month,
                        expirationYear = year,
                        securityCode = cvv.text,
                        cardholderName = cardholderName.text,
                        identificationNumber = identificationNumber.text.replace("-", "").replace(".", "")
                    )
                    
                    tokenResult.fold(
                        onSuccess = { token ->
                            // 2. Procesar pago con el token
                            val paymentMethodId = when (cardType) {
                                CardType.VISA -> "visa"
                                CardType.MASTERCARD -> "master"
                                CardType.AMEX -> "amex"
                                CardType.OCA -> "oca"
                                CardType.DINERS -> "diners"
                                CardType.CABAL -> "cabal"
                                CardType.UNKNOWN -> "visa"
                            }
                            
                            CardPaymentRepository.processPayment(
                                orderId = orderId,
                                token = token.id,
                                amount = totalAmount,
                                description = "Compra en Merqora - Orden #$orderId",
                                installments = selectedInstallments,
                                paymentMethodId = paymentMethodId,
                                issuerId = null,
                                payerEmail = payerEmail,
                                identificationNumber = identificationNumber.text.replace("-", "").replace(".", "")
                            )
                        },
                        onFailure = { error ->
                            onPaymentFailed(error.message ?: "Error al procesar tarjeta")
                        }
                    )
                }
            },
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple,
                disabledContainerColor = PrimaryPurple.copy(alpha = 0.5f)
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (paymentState is CardPaymentState.TokenizingCard) 
                        "Procesando tarjeta..." else "Procesando pago...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pagar $${String.format("%,.2f", totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Botón cancelar
        TextButton(
            onClick = {
                CardPaymentRepository.resetState()
                onCancel()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Text(
                text = "Cancelar",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun CardPreview(
    cardNumber: String,
    cardholderName: String,
    expirationDate: String,
    cardType: CardType
) {
    val gradientColors = when (cardType) {
        CardType.VISA -> listOf(VisaBlue, Color(0xFF2D3A8C))
        CardType.MASTERCARD -> listOf(Color(0xFF1A1A2E), Color(0xFF2D2D3D))
        CardType.AMEX -> listOf(AmexBlue, Color(0xFF004B9A))
        else -> listOf(CardGradientStart, CardGradientEnd)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.586f), // Proporción estándar de tarjeta
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            // Logo del tipo de tarjeta
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                CardTypeLogo(cardType)
            }
            
            // Chip
            Box(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .size(width = 45.dp, height = 35.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFD4AF37), Color(0xFFFFD700), Color(0xFFD4AF37))
                        )
                    )
            )
            
            // Número de tarjeta
            Text(
                text = cardNumber.ifEmpty { "•••• •••• •••• ••••" },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 20.dp)
            )
            
            // Nombre y fecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TITULAR",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = cardholderName.ifEmpty { "NOMBRE APELLIDO" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "VENCE",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = expirationDate.ifEmpty { "MM/AA" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CardTypeLogo(cardType: CardType) {
    when (cardType) {
        CardType.VISA -> {
            Text(
                text = "VISA",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        CardType.MASTERCARD -> {
            Row {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MastercardRed)
                )
                Box(
                    modifier = Modifier
                        .offset(x = (-12).dp)
                        .size(30.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MastercardOrange.copy(alpha = 0.9f))
                )
            }
        }
        CardType.AMEX -> {
            Text(
                text = "AMEX",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        CardType.OCA -> {
            Text(
                text = "OCA",
                color = Color(0xFF00A0E3),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Outlined.CreditCard,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun CardTypeIcon(cardType: CardType) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (cardType) {
            CardType.VISA -> {
                Text(
                    text = "V",
                    color = VisaBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            CardType.MASTERCARD -> {
                Row {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MastercardRed)
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = (-4).dp)
                            .size(12.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MastercardOrange)
                    )
                }
            }
            CardType.AMEX -> {
                Text(
                    text = "A",
                    color = AmexBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    error: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.5f)) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = error != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 58.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg,
                errorContainerColor = FieldBg,
                focusedBorderColor = FieldBorderFocused,
                unfocusedBorderColor = FieldBorder,
                errorBorderColor = ErrorRed,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedLabelColor = FieldBorderFocused,
                unfocusedLabelColor = TextMuted,
                cursorColor = FieldBorderFocused
            )
        )
        
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = error ?: "",
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun InstallmentsSelector(
    installments: List<PayerCost>,
    selectedInstallments: Int,
    onSelect: (Int) -> Unit
) {
    Column {
        Text(
            text = "Cuotas",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceElevated
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                installments.take(6).forEach { payerCost ->
                    val isSelected = payerCost.installments == selectedInstallments
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(payerCost.installments) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelect(payerCost.installments) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PrimaryPurple
                                    )
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = "${payerCost.installments}x $${String.format("%,.2f", payerCost.installmentAmount)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    if (payerCost.installmentRate == 0.0) {
                                        Text(
                                            text = "Sin interés",
                                            fontSize = 12.sp,
                                            color = AccentGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = "$${String.format("%,.2f", payerCost.totalAmount)}",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityBadge() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AccentGreen.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Pago 100% seguro",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Tus datos están encriptados y protegidos",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Parsear fecha de expiración MM/YY
 */
private fun parseExpirationDate(date: String): Pair<Int, Int> {
    val parts = date.split("/")
    val month = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val year = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return Pair(month, if (year < 100) 2000 + year else year)
}
