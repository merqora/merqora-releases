package com.mercora.app.ui.components.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.repository.SellerPaymentPrefsRepository
import com.mercora.app.ui.theme.*

@Composable
fun PaymentMethodsSection(sellerId: String? = null) {
    var prefs by remember(sellerId) { mutableStateOf<SellerPaymentPrefsRepository.SellerPaymentPrefs?>(null) }

    LaunchedEffect(sellerId) {
        if (sellerId != null) {
            prefs = SellerPaymentPrefsRepository.getPrefs(sellerId)
        }
    }

    val effectivePrefs = prefs ?: SellerPaymentPrefsRepository.DEFAULTS

    val activeMethods = buildList {
        if (effectivePrefs.acceptsCard) add("Tarjeta")
        if (effectivePrefs.acceptsBankTransfer) add("Transferencia")
        if (effectivePrefs.acceptsCash) add("Efectivo")
        if (effectivePrefs.acceptsMercadoPago) add("Mercado Pago")
        if (effectivePrefs.acceptsPrex) add("Prex")
    }

    if (activeMethods.isEmpty()) return

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Medios de pago",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            activeMethods.forEach { method ->
                PaymentMethodChip(method)
            }
        }

        if (effectivePrefs.maxInstallments > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hasta ${effectivePrefs.maxInstallments} cuotas sin interÃ©s",
                fontSize = 13.sp,
                color = AccentGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PaymentMethodChip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = SurfaceElevated
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
