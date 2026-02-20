package com.rendly.app.ui.components

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.rendly.app.data.model.Post
import com.rendly.app.ui.theme.*
import java.io.OutputStream

@Composable
fun PostQrCodeModal(
    isVisible: Boolean,
    post: Post?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf<Boolean?>(null) }
    
    // Reset state when modal opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isSaving = false
            saveSuccess = null
        }
    }
    
    // Generar QR solo cuando hay un post
    val qrBitmap = remember(post?.id) {
        post?.let { generateQrCode("Merqora://post/${it.id}") }
    }
    
    // Generar tarjeta QR completa para guardar
    val qrCardBitmap = remember(post?.id) {
        post?.let { generateQrCardBitmap(it, generateQrCode("Merqora://post/${it.id}", 400)) }
    }
    
    // Backdrop
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )
    }
    
    // Modal
    AnimatedVisibility(
        visible = isVisible && post != null,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
        ) + fadeIn(tween(200)),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(150)
        ) + fadeOut(tween(150)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(24.dp),
                color = Surface,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header con info del post
                    post?.let { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imageUrl = p.images.firstOrNull() ?: ""
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.title.ifEmpty { "Producto" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "@${p.username}",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )
                            }
                            
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // QR Code
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Código QR",
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: run {
                            CircularProgressIndicator(
                                color = PrimaryPurple,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        // Logo en el centro
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(PrimaryPurple, AccentPink)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Escanea para ver el producto",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botones verticales
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isSaving = true
                                qrCardBitmap?.let { bitmap ->
                                    val saved = saveQrToGallery(context, bitmap, "Merqora_qr_${post?.id?.take(8)}")
                                    saveSuccess = saved
                                    if (saved) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Código QR guardado en galería",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                isSaving = false
                            },
                            enabled = !isSaving && qrCardBitmap != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (saveSuccess == true) Icons.Filled.CheckCircle else Icons.Outlined.SaveAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (saveSuccess == true) "¡Guardado!" else "Guardar código QR",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextSecondary
                            )
                        ) {
                            Text(
                                text = "Cerrar",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateQrCode(content: String, size: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK 
                    else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

private fun generateQrCardBitmap(post: Post, qrBitmap: Bitmap?): Bitmap? {
    if (qrBitmap == null) return null
    
    return try {
        val cardWidth = 600
        val cardHeight = 750
        val padding = 40
        val qrSize = 400
        
        val bitmap = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Fondo blanco
        canvas.drawColor(android.graphics.Color.WHITE)
        
        // Gradiente superior (header)
        val headerPaint = android.graphics.Paint()
        val gradient = android.graphics.LinearGradient(
            0f, 0f, cardWidth.toFloat(), 100f,
            android.graphics.Color.parseColor("#8B5CF6"),
            android.graphics.Color.parseColor("#EC4899"),
            android.graphics.Shader.TileMode.CLAMP
        )
        headerPaint.shader = gradient
        canvas.drawRect(0f, 0f, cardWidth.toFloat(), 100f, headerPaint)
        
        // Logo "R"
        val logoPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 48f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Merqora", cardWidth / 2f, 65f, logoPaint)
        
        // QR Code centrado
        val qrX = (cardWidth - qrSize) / 2f
        val qrY = 140f
        val scaledQr = Bitmap.createScaledBitmap(qrBitmap, qrSize, qrSize, true)
        canvas.drawBitmap(scaledQr, qrX, qrY, null)
        
        // Título del producto
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1F2937")
            textSize = 28f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        val title = post.title.take(30) + if (post.title.length > 30) "..." else ""
        canvas.drawText(title, cardWidth / 2f, qrY + qrSize + 50f, titlePaint)
        
        // Username
        val userPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#6B7280")
            textSize = 22f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("@${post.username}", cardWidth / 2f, qrY + qrSize + 85f, userPaint)
        
        // Texto inferior
        val scanPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#9CA3AF")
            textSize = 18f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Escanea para ver el producto", cardWidth / 2f, cardHeight - 30f, scanPaint)
        
        bitmap
    } catch (e: Exception) {
        null
    }
}

private fun saveQrToGallery(context: android.content.Context, bitmap: Bitmap, fileName: String): Boolean {
    return try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Merqora")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let { imageUri ->
            val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } ?: false
    } catch (e: Exception) {
        android.util.Log.e("PostQrCodeModal", "Error saving QR: ${e.message}")
        false
    }
}
