package com.mercora.app.ui.screens.explore

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.ui.theme.*
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.data.repository.UserSizesRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// â”€â”€â”€ Data Model â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

private data class SizeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val sizesMen: List<String>,
    val sizesWomen: List<String>
)

private data class BodyMeasurements(
    val heightCm: String = "",
    val weightKg: String = "",
    val chestCm: String = "",
    val waistCm: String = "",
    val hipCm: String = "",
    val inseamCm: String = ""
)

private enum class Gender { Hombre, Mujer }
private enum class Fit { Ajustado, Regular, Holgado }

// â”€â”€â”€ Categories â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

private val SIZE_CATEGORIES = listOf(
    SizeCategory(
        "tops", "Partes de Arriba",
        "Camisetas, camisas, blusas, tops",
        Icons.Outlined.Checkroom,
        listOf(PrimaryBright, Color(0xFF1A6BB5)),
        listOf("XS", "S", "M", "L", "XL", "2XL", "3XL"),
        listOf("XS", "S", "M", "L", "XL", "2XL", "3XL")
    ),
    SizeCategory(
        "bottoms", "Partes de Abajo",
        "Pantalones, jeans, shorts, faldas",
        Icons.Outlined.Straighten,
        listOf(SecondaryPurple, Color(0xFF3BA66B)),
        listOf("28", "29", "30", "31", "32", "33", "34", "36", "38", "40", "42"),
        listOf("24", "25", "26", "27", "28", "29", "30", "32", "34", "36")
    ),
    SizeCategory(
        "dresses", "Vestidos & Monos",
        "Vestidos, enterizos, jumpsuits",
        Icons.Outlined.Woman,
        listOf(Color(0xFFCD7F32), Color(0xFFE8963E)),
        listOf("XS", "S", "M", "L", "XL", "2XL"),
        listOf("XS", "S", "M", "L", "XL", "2XL")
    ),
    SizeCategory(
        "outerwear", "Chaquetas & Abrigos",
        "Chaquetas, abrigos, sudaderas, hoodies",
        Icons.Outlined.Style,
        listOf(Color(0xFF4A6A7A), Color(0xFF6B8A9A)),
        listOf("XS", "S", "M", "L", "XL", "2XL", "3XL"),
        listOf("XS", "S", "M", "L", "XL", "2XL", "3XL")
    ),
    SizeCategory(
        "shoes", "Calzado",
        "Zapatos, zapatillas, botas, sandalias",
        Icons.Outlined.Hiking,
        listOf(AccentGold, Color(0xFFFF8C5A)),
        listOf("39", "40", "41", "42", "43", "44", "45", "46"),
        listOf("35", "36", "37", "38", "39", "40", "41", "42")
    ),
    SizeCategory(
        "underwear", "Ropa Interior",
        "Boxers, bóxer, calzoncillos, brasieres, medias",
        Icons.Outlined.FavoriteBorder,
        listOf(Color(0xFFD4578A), Color(0xFFE878A0)),
        listOf("XS", "S", "M", "L", "XL", "2XL"),
        listOf("XS", "S", "M", "L", "XL", "2XL")
    ),
    SizeCategory(
        "swimwear", "Trajes de Baño",
        "Bañadores, bikinis, trusas",
        Icons.Outlined.Pool,
        listOf(Color(0xFF1565A0), PrimaryBright),
        listOf("S", "M", "L", "XL", "2XL"),
        listOf("XS", "S", "M", "L", "XL", "2XL")
    ),
    SizeCategory(
        "accessories", "Accesorios",
        "Sombreros, gorras, cinturones, guantes",
        Icons.Outlined.Watch,
        listOf(Color(0xFF6B7280), Color(0xFF8B98A5)),
        listOf("Único", "XS", "S", "M", "L", "XL"),
        listOf("Único", "XS", "S", "M", "L", "XL")
    )
)

private val TOP_SIZE_MAP_MEN = mapOf(
    "XS" to 85f..90f, "S" to 91f..96f, "M" to 97f..102f,
    "L" to 103f..108f, "XL" to 109f..114f, "2XL" to 115f..122f, "3XL" to 123f..130f
)
private val TOP_SIZE_MAP_WOMEN = mapOf(
    "XS" to 78f..83f, "S" to 84f..89f, "M" to 90f..95f,
    "L" to 96f..102f, "XL" to 103f..109f, "2XL" to 110f..117f, "3XL" to 118f..125f
)
private val WAIST_SIZE_MAP_MEN = mapOf(
    "28" to 68f..73f, "29" to 73f..76f, "30" to 76f..79f, "31" to 79f..82f,
    "32" to 82f..85f, "33" to 85f..88f, "34" to 88f..91f, "36" to 91f..97f,
    "38" to 97f..103f, "40" to 103f..109f, "42" to 109f..115f
)
private val WAIST_SIZE_MAP_WOMEN = mapOf(
    "24" to 58f..62f, "25" to 62f..65f, "26" to 65f..68f, "27" to 68f..71f,
    "28" to 71f..74f, "29" to 74f..77f, "30" to 77f..80f, "32" to 80f..86f,
    "34" to 86f..92f, "36" to 92f..98f
)
private val SHOE_SIZE_MAP = mapOf(
    "35" to 22.0f..22.5f, "36" to 22.5f..23.0f, "37" to 23.0f..23.5f,
    "38" to 23.5f..24.0f, "39" to 24.0f..24.5f, "40" to 24.5f..25.0f,
    "41" to 25.0f..25.5f, "42" to 25.5f..26.0f, "43" to 26.0f..26.5f,
    "44" to 26.5f..27.0f, "45" to 27.0f..28.0f, "46" to 28.0f..29.0f
)

private val SHOE_SIZE_MAP_MEN = mapOf(
    "39" to 24.0f..24.5f, "40" to 24.5f..25.0f, "41" to 25.0f..25.5f,
    "42" to 25.5f..26.0f, "43" to 26.0f..26.5f, "44" to 26.5f..27.0f,
    "45" to 27.0f..28.0f, "46" to 28.0f..29.0f
)

private val SHOE_SIZE_MAP_WOMEN = mapOf(
    "35" to 22.0f..22.5f, "36" to 22.5f..23.0f, "37" to 23.0f..23.5f,
    "38" to 23.5f..24.0f, "39" to 24.0f..24.5f, "40" to 24.5f..25.0f,
    "41" to 25.0f..25.5f, "42" to 25.5f..26.0f
)

// â”€â”€â”€ Size Calculation â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

private fun suggestSize(
    categoryId: String,
    measurements: BodyMeasurements,
    gender: Gender,
    fit: Fit
): String? {
    val chest = measurements.chestCm.toFloatOrNull()
    val waist = measurements.waistCm.toFloatOrNull()
    val height = measurements.heightCm.toFloatOrNull()

    return when (categoryId) {
        "tops", "dresses", "outerwear", "underwear", "swimwear" -> {
            if (chest == null) return null
            val map = if (gender == Gender.Hombre) TOP_SIZE_MAP_MEN else TOP_SIZE_MAP_WOMEN
            val fitOffset = when (fit) {
                Fit.Ajustado -> -2
                Fit.Regular -> 0
                Fit.Holgado -> 3
            }
            val adjustedChest = chest + fitOffset
            map.entries.find { adjustedChest in it.value }?.key
        }
        "bottoms" -> {
            if (waist == null) return null
            val map = if (gender == Gender.Hombre) WAIST_SIZE_MAP_MEN else WAIST_SIZE_MAP_WOMEN
            val fitOffset = when (fit) {
                Fit.Ajustado -> -2
                Fit.Regular -> 0
                Fit.Holgado -> 2
            }
            val adjustedWaist = waist + fitOffset
            map.entries.find { adjustedWaist in it.value }?.key
        }
        "shoes" -> {
            if (height == null) return null
            val footCm = (height * 0.115f + 18f).coerceIn(22f, 29f)
            val map = if (gender == Gender.Hombre) SHOE_SIZE_MAP_MEN else SHOE_SIZE_MAP_WOMEN
            map.entries.find { footCm in it.value }?.key
        }
        "accessories" -> {
            if (waist == null) return null
            when {
                waist < 75 -> "S"
                waist < 90 -> "M"
                waist < 105 -> "L"
                else -> "XL"
            }
        }
        else -> null
    }
}

private fun sizeToWeight(categoryId: String, size: String, gender: Gender): Float {
    return when (categoryId) {
        "tops", "dresses", "outerwear", "underwear", "swimwear" -> {
            val idx = if (gender == Gender.Hombre) {
                listOf("XS", "S", "M", "L", "XL", "2XL", "3XL").indexOf(size)
            } else {
                listOf("XS", "S", "M", "L", "XL", "2XL", "3XL").indexOf(size)
            }
            if (idx < 0) 0f else idx * 2f + 1f
        }
        "bottoms" -> {
            val sizes = if (gender == Gender.Hombre) {
                listOf("28", "29", "30", "31", "32", "33", "34", "36", "38", "40", "42")
            } else {
                listOf("24", "25", "26", "27", "28", "29", "30", "32", "34", "36")
            }
            val idx = sizes.indexOf(size)
            if (idx < 0) 0f else idx * 1.5f + 1f
        }
        "shoes" -> {
            val sizes = if (gender == Gender.Hombre) {
                listOf("39", "40", "41", "42", "43", "44", "45", "46")
            } else {
                listOf("35", "36", "37", "38", "39", "40", "41", "42")
            }
            val idx = sizes.indexOf(size)
            if (idx < 0) 0f else idx * 1.2f + 1f
        }
        "accessories" -> {
            val idx = listOf("Único", "XS", "S", "M", "L", "XL").indexOf(size)
            if (idx < 0) 0f else idx * 2f
        }
        else -> 0f
    }
}

// â”€â”€â”€ Main Screen â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun MySizeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSizes = remember { mutableStateMapOf<String, String>() }
    val measurements = remember { mutableStateOf(BodyMeasurements()) }
    var gender by remember { mutableStateOf(Gender.Hombre) }
    var fit by remember { mutableStateOf(Fit.Regular) }
    var showMeasurements by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val userId = remember { SupabaseClient.auth.currentUserOrNull()?.id }

    // Aplica una config (cache o red) a los estados de la UI
    fun applyConfig(existing: com.mercora.app.data.repository.UserSizesDB) {
        gender = when (existing.gender) {
            "Mujer" -> Gender.Mujer
            else -> Gender.Hombre
        }
        fit = when (existing.fitPreference) {
            "Ajustado" -> Fit.Ajustado
            "Holgado" -> Fit.Holgado
            else -> Fit.Regular
        }
        measurements.value = BodyMeasurements(
            heightCm = existing.heightCm ?: "",
            weightKg = existing.weightKg ?: "",
            chestCm = existing.chestCm ?: "",
            waistCm = existing.waistCm ?: "",
            hipCm = existing.hipCm ?: "",
            inseamCm = existing.inseamCm ?: ""
        )
        val catMap = mapOf(
            "tops" to existing.topsSize,
            "bottoms" to existing.bottomsSize,
            "dresses" to existing.dressesSize,
            "outerwear" to existing.outerwearSize,
            "shoes" to existing.shoesSize,
            "underwear" to existing.underwearSize,
            "swimwear" to existing.swimwearSize,
            "accessories" to existing.accessoriesSize
        )
        catMap.forEach { (k, v) -> if (!v.isNullOrBlank()) selectedSizes[k] = v }
    }

    // 1) Cache local instantáneo (sin esperar red) - la config aparece YA al entrar
    // 2) Luego refresca desde Supabase en segundo plano
    LaunchedEffect(Unit) {
        if (userId != null) {
            UserSizesRepository.init(context)
            val cached = UserSizesRepository.getCachedSizes(userId)
            if (cached != null) applyConfig(cached)

            val remote = UserSizesRepository.loadUserSizes(userId)
            if (remote != null) applyConfig(remote)
        }
    }

    val selectedCount = selectedSizes.size
    val totalCategories = SIZE_CATEGORIES.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg)
    ) {
        // Header
        MySizeTopBar(
            onBack = onBack,
            selectedCount = selectedCount,
            totalCount = totalCategories,
            saved = saved
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Banner informativo
            MySizeHeroBanner()

            // Gender selector
            GenderSelector(
                gender = gender,
                onGenderChange = { gender = it; autoSuggestSizes(it, measurements.value, fit, selectedSizes) }
            )

            // Body measurements (collapsible)
            MeasurementsCard(
                measurements = measurements.value,
                onMeasurementChange = { m ->
                    measurements.value = m
                    autoSuggestSizes(gender, m, fit, selectedSizes)
                },
                expanded = showMeasurements,
                onToggle = { showMeasurements = !showMeasurements }
            )

            // Fit preference
            FitSelector(
                fit = fit,
                onFitChange = { fit = it; autoSuggestSizes(gender, measurements.value, it, selectedSizes) }
            )

            // Section title
            SectionHeader("Tus tallas")

            // Category cards
            SIZE_CATEGORIES.forEach { category ->
                val sizes = if (gender == Gender.Hombre) category.sizesMen else category.sizesWomen
                val currentSize = selectedSizes[category.id]
                val suggested = suggestSize(category.id, measurements.value, gender, fit)

                SizeCategoryCardV2(
                    category = category,
                    sizes = sizes,
                    selectedSize = currentSize,
                    suggestedSize = suggested,
                    onSizeSelected = { selectedSizes[category.id] = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            SaveButton(
                selectedCount = selectedCount,
                totalCount = totalCategories,
                saved = saved,
                isSaving = isSaving,
                onSave = {
                    if (userId != null && !isSaving) {
                        isSaving = true
                        scope.launch {
                            val success = UserSizesRepository.saveUserSizes(
                                userId = userId,
                                gender = gender.name,
                                fitPreference = fit.name,
                                heightCm = measurements.value.heightCm.takeIf { it.isNotBlank() },
                                weightKg = measurements.value.weightKg.takeIf { it.isNotBlank() },
                                chestCm = measurements.value.chestCm.takeIf { it.isNotBlank() },
                                waistCm = measurements.value.waistCm.takeIf { it.isNotBlank() },
                                hipCm = measurements.value.hipCm.takeIf { it.isNotBlank() },
                                inseamCm = measurements.value.inseamCm.takeIf { it.isNotBlank() },
                                sizes = selectedSizes.toMap()
                            )
                            isSaving = false
                            saved = success
                        }
                    }
                }
            )
        }
    }
}

private fun autoSuggestSizes(
    gender: Gender,
    measurements: BodyMeasurements,
    fit: Fit,
    selectedSizes: MutableMap<String, String>
) {
    val hasMeasurements = measurements.chestCm.isNotBlank() || measurements.waistCm.isNotBlank()
    if (!hasMeasurements) return

    SIZE_CATEGORIES.forEach { category ->
        val suggested = suggestSize(category.id, measurements, gender, fit)
        if (suggested != null && !selectedSizes.containsKey(category.id)) {
            selectedSizes[category.id] = suggested
        }
    }
}

// â”€â”€â”€ Sub-components â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun MySizeTopBar(
    onBack: () -> Unit,
    selectedCount: Int,
    totalCount: Int,
    saved: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HomeBg,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Volver", tint = TextPrimary)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(PrimaryBright, AccentGold))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Straighten,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Mi Talla",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Recomendaciones inteligentes",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            if (saved) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Guardado",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceElevated
                ) {
                    Text(
                        "$selectedCount/$totalCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCount >= 3) AccentGreen else TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MySizeHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(PrimaryDark, PrimaryBright, Color(0xFF1A6BB5))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, null, tint = AccentGold, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Tu talla perfecta, siempre",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Selecciona tu género y medidas para recibir recomendaciones precisas. " +
                        "Cada producto mostrará primero las tallas que te quedan mejor.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(Icons.Filled.SmartToy, "IA", "Precisión"),
                    Triple(Icons.Filled.Speed, "Rápido", "Filtrado"),
                    Triple(Icons.Filled.Favorite, "Perfect", "Fit")
                ).forEach { (icon, _, label) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null, tint = AccentGold, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderSelector(
    gender: Gender,
    onGenderChange: (Gender) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            "Género",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Gender.entries.forEach { g ->
                val isSelected = gender == g
                val icon = if (g == Gender.Hombre) Icons.Filled.Male else Icons.Filled.Female
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onGenderChange(g) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) PrimaryBright else SurfaceElevated,
                    border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            null,
                            tint = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            g.name,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementsCard(
    measurements: BodyMeasurements,
    onMeasurementChange: (BodyMeasurements) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IconAccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Straighten, null, tint = IconAccentBlue, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tus medidas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Opcional â€” Mejora la precisión", fontSize = 12.sp, color = TextSecondary)
                }
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MeasurementField(
                            value = measurements.heightCm,
                            onValueChange = { onMeasurementChange(measurements.copy(heightCm = it)) },
                            label = "Estatura",
                            suffix = "cm",
                            modifier = Modifier.weight(1f)
                        )
                        MeasurementField(
                            value = measurements.weightKg,
                            onValueChange = { onMeasurementChange(measurements.copy(weightKg = it)) },
                            label = "Peso",
                            suffix = "kg",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MeasurementField(
                            value = measurements.chestCm,
                            onValueChange = { onMeasurementChange(measurements.copy(chestCm = it)) },
                            label = "Pecho",
                            suffix = "cm",
                            modifier = Modifier.weight(1f)
                        )
                        MeasurementField(
                            value = measurements.waistCm,
                            onValueChange = { onMeasurementChange(measurements.copy(waistCm = it)) },
                            label = "Cintura",
                            suffix = "cm",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MeasurementField(
                            value = measurements.hipCm,
                            onValueChange = { onMeasurementChange(measurements.copy(hipCm = it)) },
                            label = "Cadera",
                            suffix = "cm",
                            modifier = Modifier.weight(1f)
                        )
                        MeasurementField(
                            value = measurements.inseamCm,
                            onValueChange = { onMeasurementChange(measurements.copy(inseamCm = it)) },
                            label = "Entrepierna",
                            suffix = "cm",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { v ->
                        if (v.all { it.isDigit() } && v.length <= 3) onValueChange(v)
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    suffix,
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FitSelector(
    fit: Fit,
    onFitChange: (Fit) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Ajuste preferido",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val fitOptions = listOf(
                Triple(Fit.Ajustado, "Ajustado", "Ceñido al cuerpo"),
                Triple(Fit.Regular, "Regular", "Punto medio"),
                Triple(Fit.Holgado, "Holgado", "Cómodo y suelto")
            )
            fitOptions.forEach { (f, label, desc) ->
                val isSelected = fit == f
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onFitChange(f) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentGold.copy(alpha = 0.15f) else SurfaceElevated,
                    border = if (isSelected) BorderStroke(1.5.dp, AccentGold) else BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AccentGold else TextSecondary
                        )
                        Text(
                            desc,
                            fontSize = 10.sp,
                            color = if (isSelected) AccentGold.copy(alpha = 0.7f) else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(PrimaryBright, AccentGold)))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun SizeCategoryCardV2(
    category: SizeCategory,
    sizes: List<String>,
    selectedSize: String?,
    suggestedSize: String?,
    onSizeSelected: (String) -> Unit
) {
    val hasSuggestion = suggestedSize != null && category.id != "accessories"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        color = Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(category.gradient.first().copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        category.icon,
                        null,
                        tint = category.gradient.first(),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        category.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        category.subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                if (selectedSize != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = category.gradient.first()
                    ) {
                        Text(
                            selectedSize,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Size chips in a horizontal scrollable row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sizes.forEach { size ->
                    val isSelected = selectedSize == size
                    val isSuggested = suggestedSize == size && hasSuggestion && selectedSize != size

                    val chipColor = when {
                        isSelected -> category.gradient.first()
                        isSuggested -> AccentGreen.copy(alpha = 0.12f)
                        else -> HomeBg
                    }
                    val chipBorder = when {
                        isSelected -> null
                        isSuggested -> BorderStroke(1.5.dp, AccentGreen.copy(alpha = 0.5f))
                        else -> BorderStroke(1.dp, BorderSubtle)
                    }
                    val textColor = when {
                        isSelected -> Color.White
                        isSuggested -> AccentGreen
                        else -> TextPrimary
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipColor)
                            .then(
                                if (chipBorder != null) Modifier.border(chipBorder, RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable { onSizeSelected(size) }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                size,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || isSuggested) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                            if (isSuggested) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Suggested size indicator
            if (hasSuggestion && suggestedSize != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        null,
                        tint = AccentGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Sugerido: $suggestedSize",
                        fontSize = 12.sp,
                        color = AccentGold.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    if (selectedSize == suggestedSize) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = AccentGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            " Perfecto",
                            fontSize = 12.sp,
                            color = AccentGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Size conversion reference
            if (selectedSize != null && sizes.size > 3) {
                Spacer(modifier = Modifier.height(4.dp))
                val conversion = getSizeConversion(category.id, selectedSize)
                if (conversion != null) {
                    Text(
                        conversion,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

private fun getSizeConversion(categoryId: String, size: String): String? {
    return when (categoryId) {
        "shoes" -> {
            val eu = size.toIntOrNull() ?: return null
            val usMen = eu - 33
            val usWomen = eu - 31
            "US Hombre: $usMen · US Mujer: $usWomen · UK: ${eu - 32}"
        }
        "tops", "dresses", "outerwear", "underwear", "swimwear" -> {
            val map = mapOf("XS" to "34", "S" to "36", "M" to "38", "L" to "40", "XL" to "42", "2XL" to "44", "3XL" to "46")
            val eu = map[size]
            if (eu != null) "Europeo: $eu · UK: ${eu.toInt() - 4}" else null
        }
        "bottoms" -> {
            val num = size.toIntOrNull() ?: return null
            val eu = num + 16
            "Europeo: $eu · UK: ${num - 18}"
        }
        else -> null
    }
}

@Composable
private fun SaveButton(
    selectedCount: Int,
    totalCount: Int,
    saved: Boolean,
    isSaving: Boolean = false,
    onSave: () -> Unit
) {
    val canSave = selectedCount >= 3
    val buttonColor by animateColorAsState(
        targetValue = when {
            saved -> AccentGreen
            canSave -> PrimaryBright
            else -> PrimaryBright.copy(alpha = 0.4f)
        },
        label = "saveBtnColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Progress bar
        if (!saved) {
            val progress = selectedCount.toFloat() / totalCount
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = PrimaryBright,
                trackColor = SurfaceElevated
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (selectedCount < 3) "Selecciona al menos 3 categorías" else "$selectedCount de $totalCount categorías listas",
                fontSize = 12.sp,
                color = if (canSave) AccentGreen else TextMuted,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            enabled = canSave && !saved && !isSaving
        ) {
            if (saved) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("¡Talles guardados!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardando...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (canSave) "Guardar y activar recomendaciones"
                    else "Selecciona al menos 3 tallas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
