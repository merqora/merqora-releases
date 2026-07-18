package com.vinzay.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinzay.app.data.remote.SupabaseClient
import com.vinzay.app.data.repository.WalletRepository
import com.vinzay.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class WithdrawMethod(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

private val withdrawMethods = listOf(
    WithdrawMethod("prex", "Prex", Icons.Outlined.Wallet, Color(0xFF0A3D62), "Cuenta Prex"),
    WithdrawMethod("bank_transfer", "Transferencia bancaria", Icons.Outlined.AccountBalance, Color(0xFF2196F3), "Cuenta bancaria"),
    WithdrawMethod("mercadopago", "Mercado Pago", Icons.Outlined.AccountBalanceWallet, Color(0xFF009EE3), "Cuenta MP")
)

@Composable
fun WithdrawScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    if (!isVisible && slideOffset == 1f) return

    val scope = rememberCoroutineScope()
    val balance by WalletRepository.walletBalance.collectAsState()

    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<WithdrawMethod?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var payoutData by remember { mutableStateOf<kotlinx.serialization.json.JsonObject?>(null) }
    var showPayoutConfig by remember { mutableStateOf(false) }
    var loadVersion by remember { mutableStateOf(0) }

    // Cargar datos de payout del usuario al abrir
    LaunchedEffect(isVisible, loadVersion) {
        if (isVisible) {
            val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@LaunchedEffect
            try {
                val payout = SupabaseClient.database
                    .from("payout_methods")
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<kotlinx.serialization.json.JsonObject>()
                payoutData = payout
            } catch (_: Exception) {}
        }
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit = amount > 0 && amount <= balance && selectedMethod != null && !isProcessing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
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
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Solicitar retiro", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Saldo disponible
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0A3D62).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF0A3D62))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Saldo disponible", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    "$${String.format("%,.0f", balance)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0A3D62)
                                )
                            }
                        }
                    }

                    // Monto a retirar
                    Text("Monto a retirar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { v ->
                            if (v.all { c -> c.isDigit() || c == '.' }) {
                                amountText = v
                                errorMessage = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0") },
                        label = { Text("Monto en UYU") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    if (amount > balance) {
                        Text(
                            "El monto supera tu saldo disponible",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }

                    // Método de retiro
                    Text("Método de retiro", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)

                    withdrawMethods.forEach { method ->
                        val isConfigured = when (method.id) {
                            "prex" -> payoutData?.get("prex_phone") != null || payoutData?.get("prex_alias") != null
                            "bank_transfer" -> payoutData?.get("bank_name") != null && payoutData?.get("account_number") != null
                            "mercadopago" -> payoutData?.get("mp_email") != null || payoutData?.get("mp_account_number") != null
                            else -> false
                        }
                        val destText = when (method.id) {
                            "prex" -> {
                                val alias = payoutData?.get("prex_alias")?.toString()?.trim('"')
                                val phone = payoutData?.get("prex_phone")?.toString()?.trim('"')
                                alias ?: phone ?: "No configurado"
                            }
                            "bank_transfer" -> {
                                val bank = payoutData?.get("bank_name")?.toString()?.trim('"')
                                val acc = payoutData?.get("account_number")?.toString()?.trim('"')
                                if (bank != null && acc != null) "$bank - $acc" else "No configurado"
                            }
                            "mercadopago" -> {
                                val email = payoutData?.get("mp_email")?.toString()?.trim('"')
                                val acc = payoutData?.get("mp_account_number")?.toString()?.trim('"')
                                email ?: acc ?: "No configurado"
                            }
                            else -> "No configurado"
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isConfigured) { selectedMethod = method },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedMethod?.id == method.id) method.color.copy(alpha = 0.1f) else Surface,
                            border = if (selectedMethod?.id == method.id) BorderStroke(2.dp, method.color) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(method.color.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(method.icon, contentDescription = null, tint = method.color, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(method.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text(
                                        destText,
                                        fontSize = 12.sp,
                                        color = if (isConfigured) TextSecondary else Color(0xFFEF4444)
                                    )
                                }
                                if (!isConfigured) {
                                    Text(
                                        "Configurar",
                                        fontSize = 12.sp,
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable { showPayoutConfig = true }
                                    )
                                }
                            }
                        }
                    }

                    // Errores / Success
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                    if (successMessage != null) {
                        Text(successMessage!!, color = Color(0xFF22C55E), fontSize = 13.sp)
                    }

                    // Botón de solicitar
                    Button(
                        onClick = {
                            if (!canSubmit) return@Button
                            isProcessing = true
                            errorMessage = null
                            successMessage = null
                            scope.launch {
                                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                                val destObj = buildJsonObject {
                                    put("method", selectedMethod!!.id)
                                    when (selectedMethod!!.id) {
                                        "prex" -> {
                                            put("alias", payoutData?.get("prex_alias")?.toString()?.trim('"') ?: "")
                                            put("phone", payoutData?.get("prex_phone")?.toString()?.trim('"') ?: "")
                                        }
                                        "bank_transfer" -> {
                                            put("bank", payoutData?.get("bank_name")?.toString()?.trim('"') ?: "")
                                            put("account", payoutData?.get("account_number")?.toString()?.trim('"') ?: "")
                                            put("holder", payoutData?.get("holder_name")?.toString()?.trim('"') ?: "")
                                        }
                                        "mercadopago" -> {
                                            put("email", payoutData?.get("mp_email")?.toString()?.trim('"') ?: "")
                                            put("account", payoutData?.get("mp_account_number")?.toString()?.trim('"') ?: "")
                                        }
                                    }
                                }
                                val result = WalletRepository.requestWithdrawal(userId, amount, selectedMethod!!.id, destObj)
                                isProcessing = false
                                result.fold(
                                    onSuccess = {
                                        successMessage = "Solicitud de retiro creada"
                                        amountText = ""
                                        selectedMethod = null
                                        scope.launch {
                                            kotlinx.coroutines.delay(1500)
                                            onSuccess()
                                        }
                                    },
                                    onFailure = { errorMessage = it.message ?: "Error al solicitar retiro" }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A3D62),
                            disabledContainerColor = TextMuted.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Solicitar retiro", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Info
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFA726).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = Color(0xFFFFA726), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Los retiros son procesados manualmente por el equipo de Merqora. Recibirás una notificación cuando el retiro esté completado.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        PayoutMethodsScreen(
            isVisible = showPayoutConfig,
            onDismiss = {
                showPayoutConfig = false
                loadVersion++
            }
        )
    }
}
