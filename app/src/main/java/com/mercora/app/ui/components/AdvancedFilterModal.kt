package com.mercora.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.mercora.app.ui.theme.*

data class SearchFilterState(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val condition: String? = null, // "Nuevo", "Usado"
    val freeShipping: Boolean = false,
    val location: String? = null // "Tu ciudad", "Nacional"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterModal(
    isVisible: Boolean,
    currentState: SearchFilterState,
    onDismiss: () -> Unit,
    onApply: (SearchFilterState) -> Unit
) {
    var localState by remember(currentState) { mutableStateOf(currentState) }
    
    // Control de Slider de precio (0 a 100 para escala logarÃ­tmica/personalizada)
    // 0 = $0, 25 = $500, 50 = $2000, 75 = $10000, 100 = $200000+
    fun priceToSteps(price: Double): Float {
        return when {
            price <= 500 -> (price / 500 * 25).toFloat()
            price <= 2000 -> (25 + (price - 500) / 1500 * 25).toFloat()
            price <= 10000 -> (50 + (price - 2000) / 8000 * 25).toFloat()
            else -> (75 + (price - 10000) / 190000 * 25).toFloat().coerceAtMost(100f)
        }
    }
    
    fun stepsToPrice(steps: Float): Double {
        return when {
            steps <= 25 -> (steps / 25 * 500).toDouble()
            steps <= 50 -> (500 + (steps - 25) / 25 * 1500).toDouble()
            steps <= 75 -> (2000 + (steps - 50) / 25 * 8000).toDouble()
            else -> (10000 + (steps - 75) / 25 * 190000).toDouble()
        }
    }

    var stepsRange by remember(localState) {
        val start = priceToSteps(localState.minPrice ?: 0.0)
        val end = priceToSteps(localState.maxPrice ?: 200000.0)
        mutableStateOf(start..end)
    }
    
    // Estados para inputs manuales sincronizados con el slider
    var minPriceInput by remember(stepsRange.start) { 
        mutableStateOf(String.format("%.0f", stepsToPrice(stepsRange.start))) 
    }
    var maxPriceInput by remember(stepsRange.endInclusive) { 
        mutableStateOf(String.format("%.0f", stepsToPrice(stepsRange.endInclusive))) 
    }

    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = SurfaceDark
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TextMuted.copy(alpha = 0.3f))
                        )
                    }
                    
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtros Avanzados",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Rango de Precio
                        item {
                            Column {
                                FilterTitle("Rango de Precio")
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PriceInput(
                                        label = "MÃ­nimo",
                                        value = minPriceInput,
                                        onValueChange = {
                                            minPriceInput = it
                                            it.toDoubleOrNull()?.let { price ->
                                                val step = priceToSteps(price)
                                                stepsRange = step..stepsRange.endInclusive
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(12.dp)
                                            .height(2.dp)
                                            .background(TextMuted.copy(alpha = 0.3f))
                                            .align(Alignment.CenterVertically)
                                            .offset(y = 8.dp)
                                    )
                                    
                                    PriceInput(
                                        label = "MÃ¡ximo",
                                        value = maxPriceInput,
                                        onValueChange = {
                                            maxPriceInput = it
                                            it.toDoubleOrNull()?.let { price ->
                                                val step = priceToSteps(price)
                                                stepsRange = stepsRange.start..step
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                RangeSlider(
                                    value = stepsRange,
                                    onValueChange = { 
                                        stepsRange = it
                                    },
                                    valueRange = 0f..100f,
                                    steps = 0, // Continuo para mÃ¡xima precisiÃ³n
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = PrimaryPurple,
                                        inactiveTrackColor = TextMuted.copy(alpha = 0.2f),
                                        thumbColor = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$0", fontSize = 11.sp, color = TextMuted)
                                    Text("$500", fontSize = 11.sp, color = TextMuted)
                                    Text("$2k", fontSize = 11.sp, color = TextMuted)
                                    Text("$10k", fontSize = 11.sp, color = TextMuted)
                                    Text("$200k+", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                        
                        // Estado del producto
                        item {
                            Column {
                                FilterTitle("Estado")
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ConditionChip(
                                        label = "Nuevo",
                                        icon = Icons.Outlined.NewReleases,
                                        isSelected = localState.condition == "Nuevo",
                                        onClick = { 
                                            localState = localState.copy(
                                                condition = if (localState.condition == "Nuevo") null else "Nuevo"
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    ConditionChip(
                                        label = "Usado",
                                        icon = Icons.Outlined.History,
                                        isSelected = localState.condition == "Usado",
                                        onClick = { 
                                            localState = localState.copy(
                                                condition = if (localState.condition == "Usado") null else "Usado"
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        
                        // EnvÃ­o
                        item {
                            Column {
                                FilterTitle("Opciones de EnvÃ­o")
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Surface.copy(alpha = 0.5f))
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.LocalShipping,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Solo envÃ­o gratis", color = TextPrimary, fontSize = 15.sp)
                                    }
                                    Switch(
                                        checked = localState.freeShipping,
                                        onCheckedChange = { 
                                            localState = localState.copy(freeShipping = it) 
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PrimaryPurple,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = SurfaceElevated
                                        )
                                    )
                                }
                            }
                        }
                        
                        // UbicaciÃ³n
                        item {
                            Column {
                                FilterTitle("UbicaciÃ³n")
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    LocationChip(
                                        label = "Tu ciudad",
                                        isSelected = localState.location == "Tu ciudad",
                                        onClick = { 
                                            localState = localState.copy(
                                                location = if (localState.location == "Tu ciudad") null else "Tu ciudad"
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    LocationChip(
                                        label = "Nacional",
                                        isSelected = localState.location == "Nacional",
                                        onClick = { 
                                            localState = localState.copy(
                                                location = if (localState.location == "Nacional") null else "Nacional"
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Botones de acciÃ³n
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceDark,
                        shadowElevation = 16.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    localState = SearchFilterState()
                                    stepsRange = 0f..100f
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(TextMuted.copy(alpha = 0.2f), TextMuted.copy(alpha = 0.2f)))
                                )
                            ) {
                                Text("Restablecer", fontWeight = FontWeight.SemiBold)
                            }
                            
                            Button(
                                onClick = {
                                    val finalState = localState.copy(
                                        minPrice = stepsToPrice(stepsRange.start),
                                        maxPrice = if (stepsRange.endInclusive >= 99f) null else stepsToPrice(stepsRange.endInclusive)
                                    )
                                    onApply(finalState)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                            ) {
                                Text("Aplicar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 8) onValueChange(it.filter { c -> c.isDigit() }) },
            prefix = { Text("$", color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = TextMuted.copy(alpha = 0.2f),
                unfocusedContainerColor = Surface.copy(alpha = 0.3f),
                focusedContainerColor = Surface.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun FilterTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ConditionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Surface.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.2.dp, PrimaryPurple) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryPurple else TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isSelected) PrimaryPurple else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LocationChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Surface.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (label == "Tu ciudad") Icons.Default.LocationCity else Icons.Default.Public,
                contentDescription = null,
                tint = if (isSelected) PrimaryPurple else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (isSelected) PrimaryPurple else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}
