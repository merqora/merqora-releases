package com.mercora.app.ui.components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.remote.SessionPersistence
import com.mercora.app.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private enum class PayoutMethodType { MERCADO_PAGO, CARD, BANK_TRANSFER, PREX }

private data class SavedCard(
    val id: String,
    val lastFour: String,
    val cardholderName: String,
    val expiry: String,
    val brand: String
)

private data class BankTransferData(
    val bankName: String = "",
    val accountType: String = "savings",
    val accountNumber: String = "",
    val holderName: String = "",
    val rut: String = ""
)

@Serializable
private data class PayoutMethodRow(
    val id: String = "",
    val user_id: String = "",
    val mp_email: String? = null,
    val prex_phone: String? = null,
    val prex_account: String? = null,
    val prex_alias: String? = null,
    val bank_name: String? = null,
    val account_type: String? = null,
    val account_number: String? = null,
    val holder_name: String? = null,
    val holder_document: String? = null,
    val default_method: String? = "mercadopago",
)

@Composable
fun PayoutMethodsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    if (!isVisible && slideOffset == 1f) return

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var mpEmail by remember { mutableStateOf("") }
    var savedCards by remember { mutableStateOf(listOf<SavedCard>()) }
    var bankTransfer by remember { mutableStateOf(BankTransferData()) }
    var defaultMethod by remember { mutableStateOf(PayoutMethodType.MERCADO_PAGO) }
    var showAddCard by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    var prexPhone by remember { mutableStateOf("") }
    var prexAccount by remember { mutableStateOf("") }
    var prexAlias by remember { mutableStateOf("") }

    var expandedMercadoPago by remember { mutableStateOf(true) }
    var expandedCards by remember { mutableStateOf(false) }
    var expandedBank by remember { mutableStateOf(false) }
    var expandedPrex by remember { mutableStateOf(false) }

    // Cargar datos existentes al abrir
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            try {
                val userId = SessionPersistence.getUserId()
                if (userId != null) {
                    val response = SupabaseClient.database.from("payout_methods")
                        .select { filter { eq("user_id", userId) } }
                        .decodeSingleOrNull<PayoutMethodRow>()
                    if (response != null) {
                        mpEmail = response.mp_email ?: ""
                        prexPhone = response.prex_phone ?: ""
                        prexAccount = response.prex_account ?: ""
                        prexAlias = response.prex_alias ?: ""
                        bankTransfer = BankTransferData(
                            bankName = response.bank_name ?: "",
                            accountType = response.account_type ?: "savings",
                            accountNumber = response.account_number ?: "",
                            holderName = response.holder_name ?: "",
                            rut = response.holder_document ?: ""
                        )
                        defaultMethod = when (response.default_method) {
                            "card" -> PayoutMethodType.CARD
                            "bank_transfer" -> PayoutMethodType.BANK_TRANSFER
                            "prex" -> PayoutMethodType.PREX
                            else -> PayoutMethodType.MERCADO_PAGO
                        }
                    }
                }
            } catch (e: Exception) {
                // Primera vez, no hay datos
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(enabled = slideOffset == 0f) { onDismiss() }
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
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "M\u00e9todos de cobro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Configura c\u00f3mo recibir tus pagos",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    MethodSection(
                        title = "Mercado Pago",
                        subtitle = "Recib\u00ed el dinero directo en tu cuenta",
                        icon = Icons.Outlined.AccountBalance,
                        iconColor = AccentGreen,
                        isExpanded = expandedMercadoPago,
                        onToggle = { expandedMercadoPago = !expandedMercadoPago },
                        isSelected = defaultMethod == PayoutMethodType.MERCADO_PAGO,
                        onSelectDefault = { defaultMethod = PayoutMethodType.MERCADO_PAGO }
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Email de Mercado Pago",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = mpEmail,
                            onValueChange = { mpEmail = it },
                            placeholder = { Text("tuemail@ejemplo.com", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = AccentGreen
                            )
                        )

                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MethodSection(
                        title = "Tarjetas de d\u00e9bito/cr\u00e9dito",
                        subtitle = "Agreg\u00e1 tarjetas para recibir pagos",
                        icon = Icons.Outlined.CreditCard,
                        iconColor = AccentGold,
                        isExpanded = expandedCards,
                        onToggle = { expandedCards = !expandedCards },
                        isSelected = defaultMethod == PayoutMethodType.CARD,
                        onSelectDefault = { defaultMethod = PayoutMethodType.CARD }
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        savedCards.forEach { card ->
                            SavedCardItem(card = card)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (showAddCard) {
                            AddCardForm(
                                onSave = { savedCards = savedCards + it; showAddCard = false },
                                onCancel = { showAddCard = false }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { showAddCard = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showAddCard) TextMuted.copy(alpha = 0.15f) else AccentGold
                            ),
                            enabled = !showAddCard
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Agregar tarjeta",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (savedCards.isEmpty() && !showAddCard) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "A\u00fan no agregaste tarjetas. Agreg\u00e1 una para recibir pagos directamente en tu tarjeta.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MethodSection(
                        title = "Transferencia bancaria",
                        subtitle = "Recib\u00ed el dinero en tu cuenta bancaria",
                        icon = Icons.Outlined.AccountBalance,
                        iconColor = PrimaryBright,
                        isExpanded = expandedBank,
                        onToggle = { expandedBank = !expandedBank },
                        isSelected = defaultMethod == PayoutMethodType.BANK_TRANSFER,
                        onSelectDefault = { defaultMethod = PayoutMethodType.BANK_TRANSFER }
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        BankTransferForm(
                            data = bankTransfer,
                            onDataChange = { bankTransfer = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MethodSection(
                        title = "Prex",
                        subtitle = "Recib\u00ed el dinero en tu cuenta Prex",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconColor = Color(0xFF00B8A9),
                        isExpanded = expandedPrex,
                        onToggle = { expandedPrex = !expandedPrex },
                        isSelected = defaultMethod == PayoutMethodType.PREX,
                        onSelectDefault = { defaultMethod = PayoutMethodType.PREX }
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        PrexPayoutFields(
                            phone = prexPhone,
                            account = prexAccount,
                            alias = prexAlias,
                            onPhoneChange = { prexPhone = it },
                            onAccountChange = { prexAccount = it },
                            onAliasChange = { prexAlias = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(color = BorderSubtle)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "M\u00c9TODO PREDETERMINADO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Seleccion\u00e1 el m\u00e9todo que se usar\u00e1 por defecto para recibir tus pagos.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (showSuccess) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = AccentGreen.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "M\u00e9todo de cobro guardado",
                                    fontSize = 14.sp,
                                    color = AccentGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                showSuccess = false
                                try {
                                    val userId = SessionPersistence.getUserId() ?: return@launch
                                    val defaultStr = when (defaultMethod) {
                                        PayoutMethodType.CARD -> "card"
                                        PayoutMethodType.BANK_TRANSFER -> "bank_transfer"
                                        PayoutMethodType.PREX -> "prex"
                                        else -> "mercadopago"
                                    }

                                    // 1. Guardar campos base (existen en la tabla)
                                    SupabaseClient.database.from("payout_methods").upsert(
                                        value = buildJsonObject {
                                            put("user_id", userId)
                                            put("mp_email", mpEmail)
                                            put("default_method", defaultStr)
                                        },
                                        onConflict = "user_id"
                                    )

                                    // 2. Guardar datos de Prex (puede fallar si faltan columnas)
                                    if (prexPhone.isNotEmpty() || prexAccount.isNotEmpty()) {
                                        try {
                                            SupabaseClient.database.from("payout_methods").update(
                                                buildJsonObject {
                                                    put("prex_phone", prexPhone)
                                                    put("prex_account", prexAccount)
                                                    put("prex_alias", prexAlias)
                                                }
                                            ) { filter { eq("user_id", userId) } }
                                        } catch (_: Exception) {}
                                    }

                                    // 3. Guardar datos bancarios (puede fallar si faltan columnas)
                                    if (bankTransfer.bankName.isNotEmpty() || bankTransfer.accountNumber.isNotEmpty()) {
                                        try {
                                            SupabaseClient.database.from("payout_methods").update(
                                                buildJsonObject {
                                                    put("bank_name", bankTransfer.bankName)
                                                    put("account_type", bankTransfer.accountType)
                                                    put("account_number", bankTransfer.accountNumber)
                                                    put("holder_name", bankTransfer.holderName)
                                                    put("holder_document", bankTransfer.rut)
                                                }
                                            ) { filter { eq("user_id", userId) } }
                                        } catch (_: Exception) {}
                                    }

                                    showSuccess = true
                                    kotlinx.coroutines.delay(2500)
                                    showSuccess = false
                                } catch (e: Exception) {
                                    val msg = e.message?.lowercase() ?: ""
                                    if (msg.contains("column")) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "EjecutÃ¡ 003_payout_extra_columns.sql en Supabase SQL editor para agregar las columnas faltantes, o volvÃ© a intentar.",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        android.widget.Toast.makeText(
                                            context, "Error al guardar: ${e.message?.take(50)}",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                isSaving = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Guardar cambios",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun MethodSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isSelected: Boolean,
    onSelectDefault: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = BorderSubtle)
                    content()
                }
            }

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderSubtle)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onSelectDefault),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) AccentGreen else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Usar por defecto",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) AccentGreen else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedCardItem(card: SavedCard) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = HomeBg
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBright.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = PrimaryBright,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${card.brand} **** ${card.lastFour}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "${card.cardholderName} - Vence ${card.expiry}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun AddCardForm(
    onSave: (SavedCard) -> Unit,
    onCancel: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = HomeBg,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Nueva tarjeta",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it.take(19) },
                placeholder = { Text("1234 5678 9012 3456", color = TextMuted) },
                label = { Text("N\u00famero de tarjeta", color = TextSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentGold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cardholderName,
                onValueChange = { cardholderName = it },
                placeholder = { Text("Nombre del titular", color = TextMuted) },
                label = { Text("Titular", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentGold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { expiry = it.take(5) },
                    placeholder = { Text("MM/AA", color = TextMuted) },
                    label = { Text("Vencimiento", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentGold
                    )
                )

                OutlinedTextField(
                    value = cvv,
                    onValueChange = { cvv = it.take(4) },
                    placeholder = { Text("CVV", color = TextMuted) },
                    label = { Text("CVV", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentGold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        val cleanNumber = cardNumber.replace(" ", "")
                        val brand = when {
                            cleanNumber.startsWith("4") -> "Visa"
                            cleanNumber.startsWith("5") -> "Mastercard"
                            cleanNumber.startsWith("3") -> "American Express"
                            else -> "Tarjeta"
                        }
                        onSave(
                            SavedCard(
                                id = cleanNumber.takeLast(4),
                                lastFour = cleanNumber.takeLast(4),
                                cardholderName = cardholderName,
                                expiry = expiry,
                                brand = brand
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    enabled = cardNumber.replace(" ", "").length >= 13 && cardholderName.isNotBlank() && expiry.length == 5 && cvv.length >= 3
                ) {
                    Text("Guardar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BankTransferForm(
    data: BankTransferData,
    onDataChange: (BankTransferData) -> Unit
) {
    var accountTypeExpanded by remember { mutableStateOf(false) }
    val accountTypes = listOf("Caja de ahorros" to "savings", "Cuenta corriente" to "checking")

    OutlinedTextField(
        value = data.bankName,
        onValueChange = { onDataChange(data.copy(bankName = it)) },
        placeholder = { Text("Ej: Banco Santander", color = TextMuted) },
        label = { Text("Nombre del banco", color = TextSecondary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBright,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryBright
        )
    )

    Spacer(modifier = Modifier.height(10.dp))

    ExposedDropdownMenuBox(
        expanded = accountTypeExpanded,
        onExpandedChange = { accountTypeExpanded = !accountTypeExpanded }
    ) {
        OutlinedTextField(
            value = accountTypes.first { it.second == data.accountType }.first,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo de cuenta", color = TextSecondary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountTypeExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBright,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        ExposedDropdownMenu(
            expanded = accountTypeExpanded,
            onDismissRequest = { accountTypeExpanded = false },
            modifier = Modifier.background(Surface)
        ) {
            accountTypes.forEach { (name, value) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            color = if (data.accountType == value) PrimaryBright else TextPrimary
                        )
                    },
                    onClick = {
                        onDataChange(data.copy(accountType = value))
                        accountTypeExpanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = data.accountNumber,
        onValueChange = { onDataChange(data.copy(accountNumber = it)) },
        placeholder = { Text("N\u00famero de cuenta", color = TextMuted) },
        label = { Text("CBU / Alias / Cuenta", color = TextSecondary) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBright,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryBright
        )
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = data.holderName,
        onValueChange = { onDataChange(data.copy(holderName = it)) },
        placeholder = { Text("Nombre del titular", color = TextMuted) },
        label = { Text("Titular de la cuenta", color = TextSecondary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBright,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryBright
        )
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = data.rut,
        onValueChange = { onDataChange(data.copy(rut = it)) },
        placeholder = { Text("Ej: 12.345.678-9", color = TextMuted) },
        label = { Text("RUT / C\u00e9dula de identidad", color = TextSecondary) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBright,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = PrimaryBright
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = PrimaryBright.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = PrimaryBright,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Las transferencias pueden demorar 1-3 d\u00edas h\u00e1biles en acreditarse seg\u00fan el banco.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun PrexPayoutFields(
    phone: String,
    account: String,
    alias: String,
    onPhoneChange: (String) -> Unit,
    onAccountChange: (String) -> Unit,
    onAliasChange: (String) -> Unit
) {
    val prexColor = Color(0xFF00B8A9)

    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        placeholder = { Text("099 123 456", color = TextMuted) },
        label = { Text("Tel\u00e9fono Prex", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = prexColor, modifier = Modifier.size(20.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = prexColor,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = prexColor
        )
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = account,
        onValueChange = onAccountChange,
        placeholder = { Text("1234567890", color = TextMuted) },
        label = { Text("N\u00famero de cuenta", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Outlined.Pin, null, tint = prexColor, modifier = Modifier.size(20.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = prexColor,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = prexColor
        )
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = alias,
        onValueChange = onAliasChange,
        placeholder = { Text("tualias (opcional)", color = TextMuted) },
        label = { Text("Alias", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Outlined.AlternateEmail, null, tint = prexColor, modifier = Modifier.size(20.dp)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = prexColor,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = prexColor
        )
    )

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = prexColor.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = prexColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Los pagos con Prex se acreditan al instante. El comprador transferir\u00e1 directamente desde su app Prex.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
