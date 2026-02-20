package com.rendly.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🎨 PALETA PREMIUM "Merqora" - Azul Marino / Verde / Naranja
// Identidad: Profesional, confiable, energético - Dark Mode First
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// 🌑 Backgrounds - Escala oscura con tinte azulado
val HomeBg = Color(0xFF0D1117)           // Fondo principal (oscuro con tinte azul)
val TabBarBg = Color(0xFF080C12)          // Tab bar más oscuro
val NavbarBg = Color(0xFF080C12)          // Navbar background
val Surface = Color(0xFF151B23)           // Superficie elevada
val SurfaceElevated = Color(0xFF1C2430)   // Superficie destacada

// ● Primario - Azul marino profundo (confianza, profesionalismo)
val PrimaryPurple = Color(0xFF0A3D62)     // Azul marino primario
val PrimaryBright = Color(0xFF1565A0)     // Azul marino más claro
val PrimaryDark = Color(0xFF072C47)       // Azul marino profundo

// 🟢 Secundario - Verde mar (naturaleza, éxito, crecimiento)
val SecondaryPurple = Color(0xFF2E8B57)   // Verde mar secundario
val AccentMagenta = Color(0xFF2E8B57)     // Alias → Verde mar

// ✨ Acentos
val AccentGold = Color(0xFFFF6B35)        // Naranja vibrante (acento principal)
val AccentPink = Color(0xFFFF6B35)        // Naranja acento (engagement, CTAs)
val AccentBlue = Color(0xFF0A3D62)        // Azul primario (compras)
val AccentGreen = Color(0xFF2E8B57)       // Verde mar (stock, éxito)
val AccentYellow = Color(0xFFFF6B35)      // Naranja (alertas, destacados)
val AccentCyan = Color(0xFF1565A0)        // Azul claro (IA, tecnología)

// 🛒 Paleta de botones ProductPage
val ButtonBuyNow = Color(0xFFFF6B35)      // Naranja acento - acción principal
val ButtonAddCart = Color(0xFF0A3D62)     // Azul primario
val ButtonConsult = Color(0xFF444444)     // Gris neutro oscuro
val PriceColor = Color(0xFF2E8B57)        // Verde para precios
val SavingsColor = Color(0xFF2E8B57)      // Verde para ahorro

// 📝 Textos
val TextPrimary = Color(0xFFF0F0F0)       // Blanco suave (no puro para menos fatiga)
val TextSecondary = Color(0xFF9CA3AF)     // Gris medio
val TextTertiary = Color(0xFF6B7280)      // Gris oscuro
val TextMuted = Color(0xFF4B5563)         // Gris muy tenue

// 🔲 Bordes & Efectos - Basados en primario azul
val BorderSubtle = Color(0x140A3D62)      // Borde casi invisible (8% opacity)
val BorderDefault = Color(0x1F0A3D62)     // Borde estándar (12% opacity)
val OverlayLight = Color(0x0F0A3D62)      // Overlay suave (6% opacity)
val OverlayMedium = Color(0x1A0A3D62)     // Overlay medio (10% opacity)

// Iconos
val IconColor = Color(0xFFF0F0F0)         // Color de iconos (blanco suave)
val InactiveColor = Color(0xB36B7280)     // Color inactivo (70% opacity)
val IconAccentBlue = Color(0xFF3A8FD4)    // Azul claro para iconos de perfil/acciones (visible en dark)

// Legacy aliases (para compatibilidad)
val Purple80 = PrimaryPurple
val PurpleGrey80 = Color(0xFF4A6A7A)
val Pink80 = AccentPink
val Purple40 = PrimaryDark
val PurpleGrey40 = Color(0xFF35505E)
val Pink40 = Color(0xFFCC5529)
val BackgroundDark = HomeBg
val SurfaceDark = Surface
val BorderDark = BorderDefault
val DarkBackground = HomeBg
val SurfaceColor = Surface

// 🌊 River Brush - Degradado azul-naranja para bordes de avatar/highlights
// Sin verde: la combinación verde+naranja queda mal en bordes
val RiverBrushSweep = Brush.sweepGradient(
    colorStops = arrayOf(
        0.00f to Color(0xFFFF6B35),   // Naranja
        0.25f to Color(0xFFFF6B35),   // Naranja
        0.45f to Color(0xFF0A3D62),   // Azul marino (transición suave)
        0.75f to Color(0xFF0A3D62),   // Azul marino
        0.90f to Color(0xFFFF6B35),   // Naranja (transición suave)
        1.00f to Color(0xFFFF6B35),   // Naranja
    )
)

val RiverBrushLinear = Brush.linearGradient(
    colorStops = arrayOf(
        0.00f to Color(0xFFFF6B35),   // Naranja
        0.35f to Color(0xFFFF6B35),   // Naranja
        0.50f to Color(0xFF0A3D62),   // Azul marino (transición suave)
        0.80f to Color(0xFF0A3D62),   // Azul marino
        0.95f to Color(0xFFFF6B35),   // Naranja
        1.00f to Color(0xFFFF6B35),   // Naranja
    )
)
