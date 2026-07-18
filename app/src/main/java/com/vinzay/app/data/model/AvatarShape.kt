package com.vinzay.app.data.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class AvatarShapeType(
    val id: String,
    val displayName: String,
    val dbValue: String
) {
    CIRCLE("circle", "Círculo", "circle"),
    ROUNDED_SQUARE("rounded_square", "Cuadrado Redondeado", "rounded_square"),
    SQUIRCLE("squircle", "Squircle", "squircle"),
    TRIANGLE("triangle", "Triángulo", "triangle"),
    STAR("star", "Estrella", "star"),
    HEART("heart", "Corazón", "heart"),
    HEXAGON("hexagon", "Hexágono", "hexagon"),
    DIAMOND("diamond", "Diamante", "diamond");

    fun toShape(): Shape = when (this) {
        CIRCLE -> CircleShape
        ROUNDED_SQUARE -> RoundedCornerShape(20)
        SQUIRCLE -> SquircleShape
        TRIANGLE -> TriangleShape
        STAR -> StarShape
        HEART -> HeartShape
        HEXAGON -> HexagonShape
        DIAMOND -> DiamondShape
    }

    companion object {
        fun fromDbValue(value: String?): AvatarShapeType =
            entries.find { it.dbValue == value } ?: CIRCLE
    }
}

data class AvatarShapePack(
    val id: String,
    val title: String,
    val description: String,
    val shapes: List<AvatarShapeType>,
    val priceCredits: Int,
    val icon: String
)

val AVATAR_SHAPE_PACKS = listOf(
    AvatarShapePack(
        id = "shape_pack_basico",
        title = "Pack Básico",
        description = "Formas esenciales: cuadrado redondeado, squircle y diamante",
        shapes = listOf(AvatarShapeType.ROUNDED_SQUARE, AvatarShapeType.SQUIRCLE, AvatarShapeType.DIAMOND),
        priceCredits = 0,
        icon = "square"
    ),
    AvatarShapePack(
        id = "shape_pack_geometrico",
        title = "Pack Geométrico",
        description = "Formas geométricas: triángulo y hexágono",
        shapes = listOf(AvatarShapeType.TRIANGLE, AvatarShapeType.HEXAGON),
        priceCredits = 0,
        icon = "triangle"
    ),
    AvatarShapePack(
        id = "shape_pack_premium",
        title = "Pack Premium",
        description = "Formas premium: estrella y corazón",
        shapes = listOf(AvatarShapeType.STAR, AvatarShapeType.HEART),
        priceCredits = 0,
        icon = "star"
    ),
    AvatarShapePack(
        id = "shape_pack_completo",
        title = "Pack Completo",
        description = "TODAS las formas disponibles",
        shapes = AvatarShapeType.entries.filter { it != AvatarShapeType.CIRCLE },
        priceCredits = 0,
        icon = "stars"
    )
)

// ── Custom Shape Implementations ──────────────────────────────────────

private object SquircleShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val n = 3.2 // Super-ellipse exponent: lower = more rounded, higher = more square
        val steps = 120
        val path = Path().apply {
            for (i in 0..steps) {
                val t = (i.toDouble() / steps) * 2.0 * PI
                val cosT = cos(t)
                val sinT = sin(t)
                val absCos = abs(cosT).coerceAtLeast(1e-10)
                val absSin = abs(sinT).coerceAtLeast(1e-10)
                val rx = (w / 2) * Math.pow(absCos, 2.0 / n).toFloat()
                val ry = (h / 2) * Math.pow(absSin, 2.0 / n).toFloat()
                val x = cx + rx * (if (cosT >= 0) 1f else -1f)
                val y = cy + ry * (if (sinT >= 0) 1f else -1f)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

private object TriangleShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        return Outline.Generic(path)
    }
}

private object StarShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = minOf(size.width, size.height) / 2f
        val innerR = outerR * 0.88f
        val points = 15
        val path = Path().apply {
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = (i * PI / points) - PI / 2
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

private object HeartShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val path = Path().apply {
            // Bottom point of heart
            moveTo(cx, h * 0.88f)
            // Right lobe (panza)
            cubicTo(
                cx + w * 0.9f, h * 0.6f,
                cx + w * 0.58f, h * 0.05f,
                cx, h * 0.28f
            )
            // Left lobe (panza)
            cubicTo(
                cx - w * 0.58f, h * 0.05f,
                cx - w * 0.9f, h * 0.6f,
                cx, h * 0.88f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

private object HexagonShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(size.width, size.height) / 2f
        val path = Path().apply {
            for (i in 0..5) {
                val angle = (i * 60.0 - 30.0) * PI / 180.0
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

private object DiamondShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width / 2f
        val ry = size.height / 2f
        val path = Path().apply {
            moveTo(cx, 0f)
            lineTo(cx + rx, cy)
            lineTo(cx, cy + ry)
            lineTo(cx - rx, cy)
            close()
        }
        return Outline.Generic(path)
    }
}
