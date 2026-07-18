package com.vinzay.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinzay.app.ui.theme.*

data class Invoice(
    val id: String,
    val period: String,
    val amount: Double,
    val status: String,
    val date: String,
    val pdfUrl: String? = null
)

private val mockInvoices = listOf(
    Invoice("inv-001", "Junio 2026", 12500.0, "Emitida", "2026-07-01"),
    Invoice("inv-002", "Mayo 2026", 8400.0, "Pagada", "2026-06-01", "/facturas/inv-002.pdf"),
    Invoice("inv-003", "Abril 2026", 3200.0, "Pagada", "2026-05-02", "/facturas/inv-003.pdf"),
    Invoice("inv-004", "Marzo 2026", 22000.0, "Pagada", "2026-04-01", "/facturas/inv-004.pdf"),
    Invoice("inv-005", "Febrero 2026", 5600.0, "Anulada", "2026-03-01"),
    Invoice("inv-006", "Enero 2026", 15000.0, "Pagada", "2026-02-01", "/facturas/inv-006.pdf"),
)

@Composable
fun BillingScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )

    if (!isVisible && slideOffset == 1f) return

    var rut by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var fiscalAddress by remember { mutableStateOf("") }
    var billingEmail by remember { mutableStateOf("") }
    var selectedInvoiceType by remember { mutableStateOf("B") }

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
                SettingsScreenHeader(
                    title = "Facturación",
                    subtitle = "Gestioná tus facturas y datos fiscales",
                    icon = Icons.Outlined.Receipt,
                    iconColor = AccentGold,
                    onBack = onDismiss
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSectionTitle("Datos fiscales")

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = rut,
                                onValueChange = { rut = it },
                                label = { Text("RUT / CI", color = TextSecondary) },
                                placeholder = { Text("Ej: 12.345.678-9", color = TextMuted) },
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

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = businessName,
                                onValueChange = { businessName = it },
                                label = { Text("Razón social", color = TextSecondary) },
                                placeholder = { Text("Nombre completo o empresa", color = TextMuted) },
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

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = fiscalAddress,
                                onValueChange = { fiscalAddress = it },
                                label = { Text("Dirección fiscal", color = TextSecondary) },
                                placeholder = { Text("Calle, número, ciudad", color = TextMuted) },
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

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = billingEmail,
                                onValueChange = { billingEmail = it },
                                label = { Text("Email de facturación", color = TextSecondary) },
                                placeholder = { Text("factura@ejemplo.com", color = TextMuted) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SettingsSectionTitle("Tipo de factura")

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            InvoiceTypeOption(
                                type = "A",
                                title = "Factura A",
                                description = "IVA responsable",
                                isSelected = selectedInvoiceType == "A",
                                onClick = { selectedInvoiceType = "A" }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            InvoiceTypeOption(
                                type = "B",
                                title = "Factura B",
                                description = "IVA no responsable / Consumidor final",
                                isSelected = selectedInvoiceType == "B",
                                onClick = { selectedInvoiceType = "B" }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            InvoiceTypeOption(
                                type = "C",
                                title = "Factura C",
                                description = "Monotributo",
                                isSelected = selectedInvoiceType == "C",
                                onClick = { selectedInvoiceType = "C" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SettingsSectionTitle("Facturas recientes")

                    if (mockInvoices.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Outlined.Receipt,
                            title = "Sin facturas",
                            subtitle = "Tus facturas aparecerán aquí"
                        )
                    } else {
                        mockInvoices.forEach { invoice ->
                            InvoiceCard(invoice = invoice)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* TODO: abrir solicitud */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solicitar factura",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AccentGold.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Las facturas se generan automáticamente al cierre de cada mes. Podés solicitar facturas adicionales por ventas pasadas desde el botón \"Solicitar factura\". Los comprobantes en PDF estarán disponibles en esta sección.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
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
private fun InvoiceTypeOption(
    type: String,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AccentGold.copy(alpha = 0.12f) else HomeBg,
        border = if (isSelected) BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)) else BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AccentGold,
                    unselectedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) AccentGold else TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun InvoiceCard(invoice: Invoice) {
    val statusColor = when (invoice.status) {
        "Emitida" -> AccentGold
        "Pagada" -> AccentGreen
        "Anulada" -> Color(0xFFEF4444)
        else -> TextMuted
    }
    val statusIcon = when (invoice.status) {
        "Emitida" -> Icons.Outlined.Receipt
        "Pagada" -> Icons.Filled.CheckCircle
        "Anulada" -> Icons.Filled.Cancel
        else -> Icons.Outlined.HelpOutline
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.period,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = invoice.date,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%,.0f", invoice.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (invoice.status == "Pagada") AccentGreen else TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = invoice.status,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }

            if (invoice.pdfUrl != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Descargar PDF",
                    tint = AccentGold,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentGold.copy(alpha = 0.1f))
                        .padding(4.dp)
                        .clickable { /* TODO: descargar PDF */ }
                )
            }
        }
    }
}
