package com.mercora.app.ui.components.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mercora.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ReportProductDialog(
    sku: String,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit,
    postId: String = "",
    postTitle: String = "",
    postImage: String = ""
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var detailsText by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    var blockSeller by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    data class ReportReason(
        val id: String,
        val title: String,
        val description: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val reasons = listOf(
        ReportReason("fake", "Producto falso o falsificado", "El producto es una imitación o réplica no autorizada", Icons.Outlined.Warning),
        ReportReason("misleading", "Descripción Engañosa", "La Descripción no coincide con el producto real", Icons.Outlined.EditNote),
        ReportReason("wrong_price", "Precio incorrecto", "El precio mostrado es diferente al cobrado", Icons.Outlined.AttachMoney),
        ReportReason("wrong_images", "Imágenes no corresponden", "Las fotos son de otro producto", Icons.Outlined.Image),
        ReportReason("inappropriate", "Contenido inapropiado", "Contenido ofensivo, ilegal o prohibido", Icons.Outlined.Block),
        ReportReason("scam", "Posible estafa", "Sospecha de actividad fraudulenta", Icons.Outlined.Security),
        ReportReason("copyright", "Violación de derechos", "Uso no autorizado de marcas o contenido", Icons.Outlined.Copyright),
        ReportReason("other", "Otro motivo", "Especificar en los detalles", Icons.Outlined.MoreHoriz)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() }
    )

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                color = HomeBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Denunciar Publicación",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "SKU: $sku",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Divider(color = BorderSubtle)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        if (postTitle.isNotEmpty() || postImage.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (postImage.isNotEmpty()) {
                                        AsyncImage(
                                            model = postImage,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = postTitle.ifEmpty { "Producto" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Producto a reportar",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Text(
                            text = "¿Cuál es el problema?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Selecciona el motivo que mejor describe tu reporte",
                            fontSize = 13.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        reasons.forEach { reason ->
                            val isSelected = selectedReason == reason.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedReason = reason.id },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.1f) else SurfaceElevated,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFEF4444)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) Color(0xFFEF4444).copy(alpha = 0.2f)
                                                else TextMuted.copy(alpha = 0.1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = reason.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFFEF4444) else TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reason.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color(0xFFEF4444) else TextPrimary
                                        )
                                        Text(
                                            text = reason.description,
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedReason = reason.id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFFEF4444)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Cuéntanos Más detalles",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Mientras Más Información nos des, Más Rápido podremos actuar",
                            fontSize = 13.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceElevated
                        ) {
                            BasicTextField(
                                value = detailsText,
                                onValueChange = { detailsText = it },
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFFEF4444)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp)
                                    .padding(14.dp),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (detailsText.isEmpty()) {
                                            Text(
                                                text = "Describe el problema con detalle. Por ejemplo: \"El producto recibido es diferente al de las fotos...\"",
                                                color = TextMuted,
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Enlace o Información adicional (opcional)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceElevated
                        ) {
                            BasicTextField(
                                value = additionalInfo,
                                onValueChange = { additionalInfo = it },
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFFEF4444)),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (additionalInfo.isEmpty()) {
                                            Text(
                                                text = "Link al producto original, prueba de compra, etc.",
                                                color = TextMuted,
                                                fontSize = 14.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Opciones adicionales",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { blockSeller = !blockSeller }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = blockSeller,
                                onCheckedChange = { blockSeller = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFEF4444)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Bloquear a este vendedor",
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "No verás Más publicaciones de este usuario",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = TextMuted.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Los reportes falsos o malintencionados pueden resultar en restricciones a tu cuenta. Nuestro equipo revisará este reporte en las Próximas 24-48 horas.",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Surface,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (selectedReason != null) {
                                        isSubmitting = true
                                        scope.launch {
                                            val fullReport = buildString {
                                                append("Motivo: ${reasons.find { it.id == selectedReason }?.title ?: selectedReason}\n")
                                                if (detailsText.isNotBlank()) {
                                                    append("Detalles: $detailsText\n")
                                                }
                                                if (additionalInfo.isNotBlank()) {
                                                    append("Info adicional: $additionalInfo\n")
                                                }
                                                if (blockSeller) {
                                                    append("Solicita bloquear vendedor")
                                                }
                                            }
                                            onReport(fullReport)
                                            isSubmitting = false
                                        }
                                    }
                                },
                                enabled = selectedReason != null && !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    disabledContainerColor = Color(0xFFEF4444).copy(alpha = 0.4f)
                                )
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Enviar reporte",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Al enviar, aceptas nuestras Políticas de uso",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
