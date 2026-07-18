package com.mercora.app.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mercora.app.ui.theme.HomeBg

fun isVideoUrl(url: String): Boolean {
    val lower = url.lowercase()
    if (lower.contains("ik-thumbnail")) return false
    if (lower.contains(".mp4") || lower.contains(".mov") || lower.contains(".webm") || lower.contains(".m3u8")) return true
    if (lower.contains("ik.imagekit.io") && lower.contains("/rends/")) return true
    return false
}

fun videoUrlToThumbnail(url: String): String {
    if (url.lowercase().contains("ik.imagekit.io")) {
        val cleanUrl = url.split("?").first()
        return "$cleanUrl/ik-thumbnail.jpg"
    }
    return url
}

fun formatCountV2(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
}

@Composable
fun SectionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(HomeBg)
    )
}

val categoryDisplayNames = mapOf(
    "vestidos" to "Vestidos",
    "blusas" to "Blusas y Tops",
    "pantalones" to "Pantalones",
    "faldas" to "Faldas",
    "abrigos" to "Abrigos y Chaquetas",
    "zapatos_m" to "Zapatos Mujer",
    "camisas" to "Camisas",
    "pantalones_h" to "Pantalones Hombre",
    "chaquetas" to "Chaquetas",
    "trajes" to "Trajes",
    "zapatos_h" to "Zapatos Hombre",
    "bolsos" to "Bolsos y Carteras",
    "joyeria" to "JoyerÃ­a",
    "relojes" to "Relojes",
    "gafas" to "Gafas de Sol",
    "sombreros" to "Sombreros",
    "muebles" to "Muebles",
    "decoracion" to "DecoraciÃ³n",
    "plantas" to "Plantas",
    "iluminacion" to "IluminaciÃ³n",
    "smartphones" to "Smartphones",
    "laptops" to "Laptops",
    "audio" to "Audio",
    "gaming" to "Gaming"
)
