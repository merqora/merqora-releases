package com.rendly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import com.rendly.app.data.repository.CartRepository
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.repository.ChatRepository
import com.rendly.app.data.repository.NotificationRepository
import com.rendly.app.ui.theme.*

@Composable
fun HomeHeader(
    onMenuClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onLogoClick: () -> Unit = {}, // Scroll to top + refresh
    modifier: Modifier = Modifier
) {
    // Observar contador de notificaciones en tiempo real
    val unreadCount by NotificationRepository.unreadCount.collectAsState()
    val unreadMessagesCount by ChatRepository.totalUnreadCount.collectAsState()
    val cartItems by CartRepository.cartItems.collectAsState()
    val cachedCartCount by CartRepository.cachedItemCount.collectAsState()
    // Usar el conteo real si hay items cargados, sino el cacheado
    val cartItemCount = remember(cartItems, cachedCartCount) { 
        val liveCount = cartItems.sumOf { it.quantity }
        if (liveCount > 0) liveCount else cachedCartCount
    }
    
    val headerBg = themedHomeBg()
    val iconTint = themedIconColor()
    val textColor = themedTextPrimary()
    val accent = accentColor()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(headerBg)
            .statusBarsPadding() // Espacio para el status bar de Android
            .padding(horizontal = 12.dp)
            .padding(vertical = 10.dp), // Padding simétrico arriba y abajo
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menú de Categorías
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menú",
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        
        // Logo Merqora con fuente cursiva profesional estilo Instagram
        Text(
            text = "Merqora",
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = LogoCursiveFont,
            color = textColor,
            letterSpacing = 0.5.sp,
            modifier = Modifier.clickable { onLogoClick() }
        )
        
        // Carrito + Notificaciones + Mensajes
        Row {
            // botón de carrito con badge
            Box {
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Carrito",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Badge con contador de items
                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 6.dp)
                            .size(if (cartItemCount > 9) 20.dp else 18.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                            fontSize = if (cartItemCount > 9) 9.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // botón de notificaciones con badge
            Box {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notificaciones",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Badge con contador
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 6.dp)
                            .size(if (unreadCount > 9) 20.dp else 18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6B35)), // Naranja acento
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = if (unreadCount > 9) 9.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // botón de mensajes con badge
            Box {
                IconButton(onClick = onMessagesClick) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Mensajes",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Badge con contador de mensajes no leídos
                if (unreadMessagesCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 6.dp)
                            .size(if (unreadMessagesCount > 9) 20.dp else 18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A3D62)), // Azul marino paleta para mensajes
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadMessagesCount > 99) "99+" else unreadMessagesCount.toString(),
                            fontSize = if (unreadMessagesCount > 9) 9.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
