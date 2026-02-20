package com.rendly.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.rendly.app.data.model.Post
import com.rendly.app.ui.theme.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * FEATURED POSTS SECTION
 * ═══════════════════════════════════════════════════════════════════════════════
 * Sección de publicaciones destacadas para el Home.
 * Muestra 6 productos en formato de 2 columnas (3 filas) usando UnifiedProductCard.
 */

@Composable
fun FeaturedPostsSection(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    onViewMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35), // Naranja paleta
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Selección destacada",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
        
        // Grid de productos (2 columnas, 3 filas = 6 productos)
        val displayPosts = posts.take(6)
        val chunkedPosts = displayPosts.chunked(2)
        
        // Altura fija para todas las tarjetas para que sean uniformes
        val cardHeight = 330.dp
        
        chunkedPosts.forEachIndexed { rowIndex, rowPosts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (rowIndex < chunkedPosts.size - 1) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPosts.forEach { post ->
                    Box(modifier = Modifier.weight(1f).height(cardHeight)) {
                        UnifiedProductCard(
                            data = post.toProductCardData(),
                            onClick = { onPostClick(post) },
                            imageHeight = 150.dp
                        )
                    }
                }
                // Si la fila tiene solo 1 elemento, agregar espacio vacío
                if (rowPosts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Botón "Ver más publicaciones"
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onViewMoreClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3A8FD4).copy(alpha = 0.15f),
                contentColor = Color(0xFF3A8FD4)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.GridView,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ver más publicaciones",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
