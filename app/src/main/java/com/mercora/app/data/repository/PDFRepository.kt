package com.mercora.app.data.repository

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.mercora.app.data.model.Order
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object PDFRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("es", "ES"))

    suspend fun getUserPdfs(userId: String): List<JsonObject> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.database
                .from("order_pdfs")
                .select {
                    filter { eq("user_id", userId) }
                    io.github.jan.supabase.postgrest.query.Order.DESCENDING.let {
                        order("created_at", it)
                    }
                    limit(50)
                }
                .decodeList<JsonObject>()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun generateAndUpload(
        context: Context,
        order: Order,
        userId: String,
        role: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileName = "Mercora-${order.orderNumber}-${role}.pdf"
            val file = generatePdfFile(context, order, fileName)

            val bucket = "pdfs"
            val remotePath = "${userId}/${fileName}"

            SupabaseClient.client.storage.from(bucket).upload(
                path = remotePath,
                data = file.readBytes(),
                upsert = true
            )

            val publicUrl = SupabaseClient.client.storage.from(bucket).publicUrl(remotePath)

            SupabaseClient.database
                .from("order_pdfs")
                .insert(buildJsonObject {
                    put("order_id", order.id)
                    put("user_id", userId)
                    put("role", role)
                    put("file_url", publicUrl)
                    put("file_name", fileName)
                    put("file_size", file.length())
                })

            file.delete()
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generatePdfFile(context: Context, order: Order, fileName: String): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 28f
            isFakeBoldText = true
            typeface = Typeface.DEFAULT_BOLD
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 16f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
        }

        var y = 50f
        val leftMargin = 50f
        val rightLimit = 545f

        // Logo / título
        canvas.drawText("Mercora", leftMargin, y, titlePaint)
        y += 36f
        canvas.drawText("Comprobante de transacción", leftMargin, y, headerPaint)
        y += 10f
        canvas.drawLine(leftMargin, y, rightLimit, y, linePaint)
        y += 26f

        // N° de orden
        canvas.drawText("N° de orden", leftMargin, y, labelPaint)
        y += 14f
        canvas.drawText(order.orderNumber, leftMargin, y, bodyPaint)
        y += 26f

        // Fecha
        val formattedDate = try {
            val instant = Instant.parse(order.createdAt)
            val zoned = instant.atZone(ZoneId.systemDefault())
            zoned.format(dateFormatter)
        } catch (_: Exception) { order.createdAt.take(10) }
        canvas.drawText("Fecha", leftMargin, y, labelPaint)
        y += 14f
        canvas.drawText(formattedDate, leftMargin, y, bodyPaint)
        y += 26f

        // Estado
        canvas.drawText("Estado", leftMargin, y, labelPaint)
        y += 14f
        canvas.drawText(order.statusDisplayName, leftMargin, y, bodyPaint)
        y += 36f

        // Línea separadora
        canvas.drawLine(leftMargin, y, rightLimit, y, linePaint)
        y += 26f

        // Items
        canvas.drawText("Artículos", leftMargin, y, headerPaint)
        y += 26f

        for (item in order.items) {
            canvas.drawText("• ${item.title}", leftMargin, y, bodyPaint)
            y += 14f
            canvas.drawText(
                "  Cant: ${item.quantity} x $${
                    String.format("%,.0f", item.unitPrice)
                } = $${
                    String.format("%,.0f", item.totalPrice)
                }",
                leftMargin + 10f, y, bodyPaint
            )
            y += 22f
        }

        // Línea separadora
        y += 4f
        canvas.drawLine(leftMargin, y, rightLimit, y, linePaint)
        y += 26f

        // Totales
        canvas.drawText("Subtotal", leftMargin, y, bodyPaint)
        canvas.drawText("$${String.format("%,.0f", order.subtotal)}", rightLimit - 100f, y, bodyPaint)
        y += 18f
        canvas.drawText("Envío", leftMargin, y, bodyPaint)
        canvas.drawText("$${String.format("%,.0f", order.shippingCost)}", rightLimit - 100f, y, bodyPaint)
        y += 22f

        val totalPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("TOTAL", leftMargin, y, totalPaint)
        canvas.drawText(
            "$${String.format("%,.0f", order.totalAmount)} ${order.currency}",
            rightLimit - 130f, y, totalPaint
        )
        y += 36f

        // Método de pago
        canvas.drawLine(leftMargin, y, rightLimit, y, linePaint)
        y += 26f
        val paymentMethod = order.payment?.paymentMethodId ?: "Mercado Pago"
        canvas.drawText("Método de pago", leftMargin, y, labelPaint)
        y += 14f
        canvas.drawText(paymentMethod.replaceFirstChar { it.uppercase() }, leftMargin, y, bodyPaint)

        // Footer
        y = 800f
        canvas.drawLine(leftMargin, y, rightLimit, y, linePaint)
        y += 16f
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
        }
        canvas.drawText("Mercora â€” Comprobante generado el ${formattedDate}", leftMargin, y, footerPaint)

        document.finishPage(page)

        val dir = File(context.cacheDir, "pdfs")
        dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }
}
