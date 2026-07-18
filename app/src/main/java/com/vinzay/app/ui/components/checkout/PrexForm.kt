package com.vinzay.app.ui.components.checkout

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinzay.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

data class SellerPrexInfo(
    val phone: String = "099 123 456",
    val accountNumber: String = "1234567890",
    val holderName: String = "Juan Carlos Pérez",
    val email: String = "",
    val currency: String = "UYU"
)

@Composable
fun PrexForm(
    prexInfo: SellerPrexInfo,
    amount: Double,
    orderId: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var reference by remember { mutableStateOf("Pago Vinzay - Orden $orderId") }
    var usePhone by remember { mutableStateOf(prexInfo.phone.isNotEmpty()) }
    var recipientValue by remember {
        mutableStateOf(
            if (prexInfo.phone.isNotEmpty()) prexInfo.phone else prexInfo.accountNumber
        )
    }
    val isPrexInstalled = remember {
        val pm = context.packageManager
        try {
            pm.getPackageInfo("air.Prex", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage("air.Prex")
                }
                pm.queryIntentActivities(intent, 0).isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
    }

    fun copyAllAndOpenPrex() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = buildString {
            appendLine("=== Transferencia Prex ===")
            appendLine("Destino: ${if (usePhone) "Teléfono" else "Nº Cuenta"}: $recipientValue")
            appendLine("Alias: ${prexInfo.email}")
            appendLine("Referencia: $reference")
            appendLine("Monto: $${NumberFormat.getNumberInstance(Locale.US).format(amount)} ${prexInfo.currency}")
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("Prex Transfer", data))

        val prexPackage = "air.Prex"
        var opened = false

        // 0. Intent deep link a sección de transferencia (si existe)
        try {
            val deepLink = Intent(Intent.ACTION_VIEW, Uri.parse("prex://transferir")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage(prexPackage)
            }
            if (deepLink.resolveActivity(context.packageManager) != null) {
                context.startActivity(deepLink)
                opened = true
            }
        } catch (_: Exception) {}

        // 1. Intent principal
        if (!opened) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(prexPackage)
                if (intent != null) {
                    context.startActivity(intent)
                    opened = true
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback: buscar cualquier activity del paquete
        if (!opened) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(prexPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val resolveInfo = context.packageManager.queryIntentActivities(intent, 0)
                if (resolveInfo.isNotEmpty()) {
                    val className = resolveInfo[0].activityInfo.name
                    intent.setClassName(prexPackage, className)
                    context.startActivity(intent)
                    opened = true
                }
            } catch (_: Exception) {}
        }

        // 3. Intent explícito directo al package
        if (!opened) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(prexPackage)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent != null) {
                    context.startActivity(intent)
                    opened = true
                }
            } catch (_: Exception) {}
        }

        if (opened) {
            Toast.makeText(context, "Datos copiados. Pegalos en Prex.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Datos copiados. Abrí Prex manualmente y pega la info.", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrexHeader(if (prexInfo.email.isNotBlank()) prexInfo.email else prexInfo.holderName)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceElevated
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Datos del vendedor", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))

                PrexDataRow("Alias", prexInfo.email)
                PrexDataRow("Teléfono", prexInfo.phone)
                PrexDataRow("Nº Cuenta", prexInfo.accountNumber)

                Spacer(Modifier.height(4.dp))
                Divider(color = BorderSubtle)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Transferir por:", fontSize = 13.sp, color = TextMuted, modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = usePhone,
                        onClick = { usePhone = true; recipientValue = prexInfo.phone },
                        label = { Text("Teléfono", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00B8A9).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF00B8A9)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = !usePhone,
                        onClick = { usePhone = false; recipientValue = prexInfo.accountNumber },
                        label = { Text("Nº Cuenta", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00B8A9).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF00B8A9)
                        )
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceElevated
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tu transferencia", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = recipientValue,
                    onValueChange = { recipientValue = it },
                    label = { Text(if (usePhone) "Número de teléfono" else "Número de cuenta") },
                    leadingIcon = {
                        Icon(
                            if (usePhone) Icons.Outlined.Phone else Icons.Outlined.Pin,
                            null, tint = Color(0xFF00B8A9), modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E2E),
                        unfocusedContainerColor = Color(0xFF1E1E2E),
                        focusedBorderColor = Color(0xFF00B8A9),
                        unfocusedBorderColor = Color(0xFF2D2D3D),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Color(0xFF00B8A9)
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Referencia / Apodo") },
                    leadingIcon = {
                        Icon(Icons.Outlined.EditNote, null, tint = Color(0xFF00B8A9), modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E2E),
                        unfocusedContainerColor = Color(0xFF1E1E2E),
                        focusedBorderColor = Color(0xFF00B8A9),
                        unfocusedBorderColor = Color(0xFF2D2D3D),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Color(0xFF00B8A9)
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = "$${NumberFormat.getNumberInstance(Locale.US).format(amount)}",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Monto") },
                    leadingIcon = {
                        Icon(Icons.Outlined.AttachMoney, null, tint = Color(0xFF00B8A9), modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Color(0xFF1E1E2E),
                        disabledBorderColor = Color(0xFF2D2D3D),
                        disabledTextColor = TextPrimary,
                        disabledLabelColor = TextMuted
                    )
                )
            }
        }

        if (isPrexInstalled) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF00B8A9).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFF00B8A9).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, null, tint = Color(0xFF00B8A9), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Al pagar se copiarán todos los datos y se abrirá la app de Prex para que confirmes la transferencia.",
                        fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFF6B35).copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Color(0xFFFF6B35).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Warning, null, tint = Color(0xFFFF6B35), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Prex no está instalada", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF6B35))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Instalá Prex desde Play Store y volvé para completar el pago. Los datos se copiarán al portapapeles.",
                        fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=air.Prex"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=air.Prex"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00B8A9)),
                        border = BorderStroke(1.dp, Color(0xFF00B8A9))
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Ir a Play Store", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                copyAllAndOpenPrex()
                onConfirm()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPrexInstalled) Color(0xFF00B8A9) else TextMuted.copy(alpha = 0.3f)
            ),
            enabled = isPrexInstalled
        ) {
            Icon(Icons.Outlined.Fingerprint, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("Pagar con Prex $${NumberFormat.getNumberInstance(Locale.US).format(amount)}",
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar", color = TextMuted, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PrexHeader(holderName: String) {
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
                        colors = listOf(Color(0xFF006D5B), Color(0xFF00B8A9), Color(0xFF00D2B5))
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
                    Text("P", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Pago con Prex", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(holderName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PrexDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
