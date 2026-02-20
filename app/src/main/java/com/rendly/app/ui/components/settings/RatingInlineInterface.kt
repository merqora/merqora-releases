package com.rendly.app.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

@Composable
fun RatingInlineInterface(
    selectedRating: Int,
    comment: String,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Estrellas seleccionables (5 estrellas)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..5) {
                val isSelected = i <= selectedRating
                
                IconButton(
                    onClick = { onRatingChange(i) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "$i estrella${if (i > 1) "s" else ""}",
                        tint = if (isSelected) Color(0xFFFF6B35) else TextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Campo de comentario opcional
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Comentario (opcional)",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            minLines = 2,
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Botón enviar
        Button(
            onClick = onSubmit,
            enabled = selectedRating > 0,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryPurple,
                disabledContainerColor = PrimaryPurple.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = "Enviar calificación",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
