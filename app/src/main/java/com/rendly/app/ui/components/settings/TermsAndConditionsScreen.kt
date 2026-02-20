package com.rendly.app.ui.components.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.ui.theme.*

@Composable
fun TermsAndConditionsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "slideOffset"
    )
    
    if (!isVisible && slideOffset == 1f) return
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f * (1f - slideOffset)))
            .clickable(onClick = onDismiss)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (slideOffset * 400).dp)
                .clickable(enabled = false) { },
            color = HomeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header
                SettingsScreenHeader(
                    title = "términos y Condiciones",
                    subtitle = "última actualización: Enero 2026",
                    icon = Icons.Outlined.Description,
                    iconColor = Color(0xFF444444),
                    onBack = onDismiss
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Aviso importante - Etapa inicial
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFF6B35).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = Color(0xFFFF6B35),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Plataforma en Etapa Inicial",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Merqora está actualmente en fase de lanzamiento. Estos Términos pueden actualizarse a medida que la plataforma evolucione y se incorporen nuevas funcionalidades.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 1. QUÉ ES Merqora
                    TermsSection(
                        number = "1",
                        title = "¿Qué es Merqora?",
                        icon = Icons.Outlined.Store,
                        iconColor = Color(0xFF1565A0)
                    ) {
                        TermsParagraph(
                            text = "Merqora es una plataforma de comercio social que conecta a personas que desean comprar y vender productos. Actualmente operamos únicamente en Uruguay."
                        )
                        TermsParagraph(
                            text = "Merqora actúa exclusivamente como intermediario. Esto significa que facilitamos el contacto entre usuarios, pero no participamos directamente en las transacciones, Envíos ni acuerdos entre las partes."
                        )
                        TermsParagraph(
                            text = "En esta etapa, la plataforma ofrece funcionalidades Básicas de Publicación, mensajería y descubrimiento de productos. Nuevas características se irán incorporando progresivamente."
                        )
                    }
                    
                    // 2. REGISTRO Y CUENTA
                    TermsSection(
                        number = "2",
                        title = "Tu Cuenta",
                        icon = Icons.Outlined.PersonAdd,
                        iconColor = Color(0xFF2E8B57)
                    ) {
                        TermsParagraph(
                            text = "Para usar Merqora debes tener al menos 18 Años. Al registrarte, te comprometes a proporcionar Información veraz y a mantenerla actualizada."
                        )
                        TermsParagraph(
                            text = "Eres responsable de tu cuenta y de todo lo que ocurra en ella. Mantené tu Contraseña segura y no la compartas con nadie."
                        )
                        TermsParagraph(
                            text = "Podemos suspender o eliminar cuentas que incumplan estos Términos, realicen actividades fraudulentas o hagan un uso indebido de la plataforma."
                        )
                    }
                    
                    // 3. USO ACEPTABLE
                    TermsSection(
                        number = "3",
                        title = "Uso de la Plataforma",
                        icon = Icons.Outlined.Rule,
                        iconColor = Color(0xFFFF6B35)
                    ) {
                        TermsParagraph(
                            text = "Al usar Merqora, te comprometés a hacerlo de manera responsable y legal."
                        )
                        
                        Text(
                            text = "No está permitido:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        ProhibitedItem("Publicar productos ilegales, falsificados o robados")
                        ProhibitedItem("Realizar estafas o engañar a otros usuarios")
                        ProhibitedItem("Acosar, amenazar o molestar a otras personas")
                        ProhibitedItem("Publicar contenido ofensivo, violento u obsceno")
                        ProhibitedItem("Crear cuentas falsas o hacerse pasar por otro")
                        ProhibitedItem("Intentar dañar o vulnerar la seguridad de la plataforma")
                    }
                    
                    // 4. TRANSACCIONES ENTRE USUARIOS
                    TermsSection(
                        number = "4",
                        title = "Compras y Ventas",
                        icon = Icons.Outlined.Handshake,
                        iconColor = Color(0xFFFF6B35)
                    ) {
                        TermsParagraph(
                            text = "Las transacciones en Merqora se realizan directamente entre usuarios. Merqora no participa en los acuerdos de precio, pago, Envío ni entrega."
                        )
                        TermsParagraph(
                            text = "Como vendedor, sos responsable de la veracidad de tus publicaciones y de cumplir con lo acordado con el comprador."
                        )
                        TermsParagraph(
                            text = "Como comprador, te recomendamos verificar la Información del producto y acordar claramente las condiciones antes de concretar una compra."
                        )
                        TermsParagraph(
                            text = "Merqora no garantiza las transacciones, Envíos, calidad de productos ni el comportamiento de otros usuarios. Cada usuario es responsable de sus propias acciones y decisiones."
                        )
                    }
                    
                    // 5. CONTENIDO
                    TermsSection(
                        number = "5",
                        title = "Tu Contenido",
                        icon = Icons.Outlined.Image,
                        iconColor = Color(0xFF2E8B57)
                    ) {
                        TermsParagraph(
                            text = "Vos mantenés la propiedad de todo el contenido que publicás (fotos, descripciones, etc.). Al subirlo a Merqora, nos das permiso para mostrarlo en la plataforma."
                        )
                        TermsParagraph(
                            text = "No publiques contenido que infrinja derechos de terceros o que no tengas derecho a compartir."
                        )
                        TermsParagraph(
                            text = "Podemos eliminar contenido que viole estos Términos o que consideremos inapropiado."
                        )
                    }
                    
                    // 6. LIMITACIONES
                    TermsSection(
                        number = "6",
                        title = "Limitaciones y Responsabilidad",
                        icon = Icons.Outlined.Shield,
                        iconColor = Color(0xFFEF4444)
                    ) {
                        TermsParagraph(
                            text = "Merqora se ofrece \"tal cual está\" y \"Según disponibilidad\". Al estar en etapa inicial, pueden existir errores, interrupciones o funcionalidades incompletas."
                        )
                        TermsParagraph(
                            text = "No nos hacemos responsables por problemas entre usuarios, transacciones fallidas, productos defectuosos, incumplimientos de Envío ni cualquier Daño derivado del uso de la plataforma."
                        )
                        TermsParagraph(
                            text = "No garantizamos resultados Específicos ni la disponibilidad continua del servicio. Haremos nuestro mejor esfuerzo, pero no podemos prometer perfección."
                        )
                    }
                    
                    // 7. CAMBIOS Y EVOLución
                    TermsSection(
                        number = "7",
                        title = "Cambios en la Plataforma",
                        icon = Icons.Outlined.Update,
                        iconColor = Color(0xFF6366F1)
                    ) {
                        TermsParagraph(
                            text = "Como plataforma en desarrollo, Merqora puede cambiar en cualquier momento. Podemos agregar, modificar o eliminar funcionalidades Según las necesidades del servicio."
                        )
                        TermsParagraph(
                            text = "Estos Términos también pueden actualizarse. Te notificaremos sobre cambios importantes y el uso continuado de la app implica aceptación de las nuevas condiciones."
                        )
                    }
                    
                    // 8. LEY APLICABLE
                    TermsSection(
                        number = "8",
                        title = "Ley Aplicable",
                        icon = Icons.Outlined.Balance,
                        iconColor = Color(0xFF444444)
                    ) {
                        TermsParagraph(
                            text = "Estos Términos se rigen por las leyes de la República Oriental del Uruguay."
                        )
                        TermsParagraph(
                            text = "Cualquier disputa relacionada con el uso de Merqora será resuelta en los tribunales competentes de Uruguay."
                        )
                    }
                    
                    // 9. CONTACTO
                    TermsSection(
                        number = "9",
                        title = "Contacto",
                        icon = Icons.Outlined.Email,
                        iconColor = Color(0xFF2E8B57)
                    ) {
                        TermsParagraph(
                            text = "Si tenés dudas o consultas sobre estos Términos, podés escribirnos a:"
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ContactInfoCard(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = "soporte@Merqora.app"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Footer con Versión
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Versión 1.0 - Enero 2026",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Merqora © Uruguay",
                                fontSize = 11.sp,
                                color = TextMuted.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun TermsSection(
    number: String,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        // Header de Sección
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            // número con gradiente
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(iconColor, iconColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Contenido de la Sección
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun TermsParagraph(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextSecondary,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun ProhibitedItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ContactInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = HomeBg
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}
