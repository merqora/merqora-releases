package com.mercora.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.repository.SellerPaymentPrefsRepository
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch

data class PaymentMethodOption(
    val id: String,
    val label: String,
    val icon: @Composable () -> Unit,
    val color: Color,
    val description: String
)

private val PAYMENT_OPTIONS = listOf(
    PaymentMethodOption(
        id = "acceptsCard",
        label = "Tarjeta de crÃ©dito/dÃ©bito",
        icon = { Icon(Icons.Outlined.CreditCard, null, tint = Color(0xFF1565A0), modifier = Modifier.size(22.dp)) },
        color = Color(0xFF1565A0),
        description = "Visa, Mastercard, OCA, etc."
    ),
    PaymentMethodOption(
        id = "acceptsBankTransfer",
        label = "Transferencia bancaria",
        icon = { Icon(Icons.Outlined.AccountBalance, null, tint = Color(0xFF2E8B57), modifier = Modifier.size(22.dp)) },
        color = Color(0xFF2E8B57),
        description = "DepÃ³sito o transferencia"
    ),
    PaymentMethodOption(
        id = "acceptsCash",
        label = "Efectivo",
        icon = { Icon(Icons.Outlined.Payments, null, tint = Color(0xFFFF6B35), modifier = Modifier.size(22.dp)) },
        color = Color(0xFFFF6B35),
        description = "Pago en efectivo al recibir"
    ),
    PaymentMethodOption(
        id = "acceptsMercadoPago",
        label = "Mercado Pago",
        icon = {
            Icon(
                Icons.Filled.Circle,
                null,
                tint = Color(0xFF00BFFF),
                modifier = Modifier.size(22.dp)
            )
        },
        color = Color(0xFF00BFFF),
        description = "Pagos online con Mercado Pago"
    ),
    PaymentMethodOption(
        id = "acceptsPrex",
        label = "Prex",
        icon = {
            Icon(
                Icons.Filled.Circle,
                null,
                tint = Color(0xFF6B21A8),
                modifier = Modifier.size(22.dp)
            )
        },
        color = Color(0xFF6B21A8),
        description = "Tarjeta Prex Uruguay"
    )
)

@Composable
fun PaymentPrefsSettingsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var prefs by remember { mutableStateOf<SellerPaymentPrefsRepository.SellerPaymentPrefs?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var installmentsText by remember { mutableStateOf("12") }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            val loaded = SellerPaymentPrefsRepository.loadMyPrefs()
            if (loaded != null) {
                prefs = loaded
                installmentsText = loaded.maxInstallments.toString()
            }
            isLoading = false
        }
    }

    fun save(newPrefs: SellerPaymentPrefsRepository.SellerPaymentPrefs) {
        prefs = newPrefs
        scope.launch {
            isSaving = true
            SellerPaymentPrefsRepository.saveMyPrefs(newPrefs)
            isSaving = false
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Medios de pago",
                    subtitle = "Selecciona los mÃ©todos que aceptas",
                    icon = Icons.Outlined.CreditCard,
                    iconColor = Color(0xFF1565A0),
                    onBack = onDismiss
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryPurple)
                        }
                    } else {
                        val currentPrefs = prefs ?: SellerPaymentPrefsRepository.DEFAULTS

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Surface
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                PAYMENT_OPTIONS.forEach { option ->
                                    val isEnabled = when (option.id) {
                                        "acceptsCard" -> currentPrefs.acceptsCard
                                        "acceptsBankTransfer" -> currentPrefs.acceptsBankTransfer
                                        "acceptsCash" -> currentPrefs.acceptsCash
                                        "acceptsMercadoPago" -> currentPrefs.acceptsMercadoPago
                                        "acceptsPrex" -> currentPrefs.acceptsPrex
                                        else -> false
                                    }

                                    PaymentMethodToggle(
                                        option = option,
                                        isEnabled = isEnabled,
                                        onToggle = { enabled ->
                                            val updated = currentPrefs.copy(
                                                acceptsCard = if (option.id == "acceptsCard") enabled else currentPrefs.acceptsCard,
                                                acceptsBankTransfer = if (option.id == "acceptsBankTransfer") enabled else currentPrefs.acceptsBankTransfer,
                                                acceptsCash = if (option.id == "acceptsCash") enabled else currentPrefs.acceptsCash,
                                                acceptsMercadoPago = if (option.id == "acceptsMercadoPago") enabled else currentPrefs.acceptsMercadoPago,
                                                acceptsPrex = if (option.id == "acceptsPrex") enabled else currentPrefs.acceptsPrex
                                            )
                                            save(updated)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        SettingsSectionTitle("Cuotas sin interÃ©s")

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF2E8B57).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFF2E8B57),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "MÃ¡ximo de cuotas",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Hasta 12 cuotas sin interÃ©s",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        SettingsSectionTitle("Vista previa")

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceElevated
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Medios de pago",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val activeMethods = buildList {
                                        if (currentPrefs.acceptsCard) add("Tarjeta")
                                        if (currentPrefs.acceptsBankTransfer) add("Transferencia")
                                        if (currentPrefs.acceptsCash) add("Efectivo")
                                        if (currentPrefs.acceptsMercadoPago) add("M.Pago")
                                        if (currentPrefs.acceptsPrex) add("Prex")
                                    }
                                    activeMethods.forEach { method ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PrimaryPurple.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = method,
                                                fontSize = 11.sp,
                                                color = PrimaryPurple,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isSaving) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodToggle(
    option: PaymentMethodOption,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(!isEnabled) },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(option.color.copy(alpha = if (isEnabled) 0.15f else 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                option.icon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isEnabled) TextPrimary else TextMuted
                )
                Text(
                    text = option.description,
                    fontSize = 12.sp,
                    color = if (isEnabled) TextSecondary else TextMuted.copy(alpha = 0.6f)
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = option.color,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = TextMuted.copy(alpha = 0.3f)
                )
            )
        }
    }
}
