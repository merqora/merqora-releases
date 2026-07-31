package com.mercora.app.ui.components.checkout

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

data class SellerBankInfo(
    val bankName: String = "Banco República (BROU)",
    val accountHolder: String = "Juan Carlos Pérez",
    val accountType: String = "Cuenta Corriente",
    val accountNumber: String = "123456789-0",
    val cbu: String = "0010123456789012345678",
    val holderDocument: String = "1.234.567-8",
    val holderDocumentType: String = "CI",
    val currency: String = "UYU"
)

@Composable
fun BankTransferForm(
    bankInfo: SellerBankInfo,
    amount: Double,
    orderId: String,
    onMarkAsPaid: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reference = "MERQ-$orderId"

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BankTransferHeader(bankInfo.bankName)

        InfoCard(
            title = "Datos bancarios del vendedor",
            content = {
                InfoRowWithCopy("Banco", bankInfo.bankName) { copyToClipboard(bankInfo.bankName, "Banco") }
                InfoRowWithCopy("Titular", bankInfo.accountHolder) { copyToClipboard(bankInfo.accountHolder, "Titular") }
                InfoRowWithCopy("Tipo", bankInfo.accountType) { copyToClipboard(bankInfo.accountType, "Tipo") }
                InfoRowWithCopy("Nº Cuenta", bankInfo.accountNumber) { copyToClipboard(bankInfo.accountNumber, "Cuenta") }
                InfoRowWithCopy("CBU", bankInfo.cbu, isHighlighted = true) { copyToClipboard(bankInfo.cbu, "CBU") }
                InfoRowWithCopy("${bankInfo.holderDocumentType}/${"RUT"}", bankInfo.holderDocument) { copyToClipboard(bankInfo.holderDocument, "Documento") }
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AccentGreen.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Referencia obligatoria", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Incluí esta referencia en la transferencia para que el vendedor identifique tu pago:",
                    fontSize = 13.sp, color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                    color = SurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Referencia", fontSize = 11.sp, color = TextMuted)
                            Text(reference, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = 1.sp)
                        }
                        IconButton(onClick = { copyToClipboard(reference, "Referencia") }) {
                            Icon(Icons.Outlined.ContentCopy, null, tint = PrimaryPurple, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = PrimaryPurple.copy(alpha = 0.06f),
            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.AccountBalance, null, tint = PrimaryPurple, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total a transferir", fontSize = 12.sp, color = TextMuted)
                    Text("$${NumberFormat.getNumberInstance(Locale.US).format(amount)} ${bankInfo.currency}",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }

        InstructionsCard()

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                copyToClipboard(bankInfo.cbu, "CBU")
                copyToClipboard(reference, "Referencia")
                onMarkAsPaid()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
        ) {
            Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("Ya hice la transferencia", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar", color = TextMuted, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BankTransferHeader(bankName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D47A1),
                            Color(0xFF1565C0),
                            Color(0xFF1976D2)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AccountBalance, null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Transferencia Bancaria", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(bankName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRowWithCopy(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isHighlighted) PrimaryPurple.copy(alpha = 0.06f) else Color.Transparent,
        border = if (isHighlighted) BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = TextMuted, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = if (isHighlighted) 16.sp else 14.sp,
                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                    color = TextPrimary
                )
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.ContentCopy, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun InstructionsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceElevated
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.HelpOutline, null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Instrucciones", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            InstructionStep(1, "Copiá el CBU y la referencia tocando los botones")
            InstructionStep(2, "Ingresá a tu banco o app de tu banco")
            InstructionStep(3, "Creá una transferencia usando el CBU copiado")
            InstructionStep(4, "Incluí la referencia en el concepto")
            InstructionStep(5, "Transferí el monto exacto")
            InstructionStep(6, "Volvé a la app y tocá \"Ya hice la transferencia\"")
        }
    }
}

@Composable
private fun InstructionStep(step: Int, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(22.dp).clip(CircleShape)
                .background(PrimaryPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$step", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
    }
}
