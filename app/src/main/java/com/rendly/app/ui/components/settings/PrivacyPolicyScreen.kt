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
fun PrivacyPolicyScreen(
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
                    title = "política de Privacidad",
                    subtitle = "última actualización: Enero 2026",
                    icon = Icons.Outlined.Policy,
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
                    
                    // Aviso - Etapa inicial
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
                                    text = "política en Etapa Inicial",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Merqora está en fase de lanzamiento. Esta Política refleja nuestras Prácticas actuales y se actualizará a medida que incorporemos nuevas funcionalidades.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 1. DATOS QUE RECOPILAMOS
                    PrivacySection(
                        number = "1",
                        title = "Datos que Recopilamos",
                        icon = Icons.Outlined.Person,
                        iconColor = Color(0xFF1565A0)
                    ) {
                        PrivacyParagraph(
                            text = "Actualmente recopilamos únicamente la Información necesaria para que puedas usar Merqora:"
                        )
                        
                        DataTypeItem(
                            icon = Icons.Outlined.Person,
                            title = "Datos de cuenta",
                            description = "Email, nombre de usuario y foto de perfil (opcional)"
                        )
                        DataTypeItem(
                            icon = Icons.Outlined.Image,
                            title = "Contenido que publicás",
                            description = "Fotos, descripciones de productos y mensajes"
                        )
                        DataTypeItem(
                            icon = Icons.Outlined.Devices,
                            title = "Información técnica Básica",
                            description = "Tipo de dispositivo y sistema operativo (para funcionamiento de la app)"
                        )
                    }
                    
                    // 2. cómo USAMOS TUS DATOS
                    PrivacySection(
                        number = "2",
                        title = "cómo Usamos tus Datos",
                        icon = Icons.Outlined.Settings,
                        iconColor = Color(0xFFFF6B35)
                    ) {
                        PrivacyParagraph(
                            text = "Usamos tu Información únicamente para:"
                        )
                        
                        UsageItem(
                            icon = Icons.Outlined.Person,
                            text = "Crear y gestionar tu cuenta"
                        )
                        UsageItem(
                            icon = Icons.Outlined.Store,
                            text = "Mostrar tus publicaciones a otros usuarios"
                        )
                        UsageItem(
                            icon = Icons.Outlined.Chat,
                            text = "Permitir la comunicación entre usuarios"
                        )
                        UsageItem(
                            icon = Icons.Outlined.Notifications,
                            text = "Enviarte notificaciones sobre mensajes y actividad"
                        )
                        UsageItem(
                            icon = Icons.Outlined.Build,
                            text = "Mejorar el funcionamiento de la app"
                        )
                    }
                    
                    // 3. NO VENDEMOS TUS DATOS
                    PrivacySection(
                        number = "3",
                        title = "Lo que NO Hacemos",
                        icon = Icons.Outlined.Block,
                        iconColor = Color(0xFFEF4444)
                    ) {
                        PrivacyParagraph(
                            text = "Queremos ser claros sobre lo que NO hacemos con tu Información:"
                        )
                        
                        ShareItem(
                            title = "No vendemos tus datos",
                            description = "Tu Información personal nunca será vendida a terceros.",
                            color = Color(0xFF2E8B57)
                        )
                        
                        ShareItem(
                            title = "No compartimos sin necesidad",
                            description = "Solo compartimos datos básicos (como tu nombre de usuario) que son necesarios para que otros vean tus publicaciones.",
                            color = Color(0xFF1565A0)
                        )
                    }
                    
                    // 4. INFORMación PÚBLICA
                    PrivacySection(
                        number = "4",
                        title = "Información Visible para Otros",
                        icon = Icons.Outlined.Visibility,
                        iconColor = Color(0xFFFF6B35)
                    ) {
                        PrivacyParagraph(
                            text = "Al usar Merqora, cierta Información es visible para otros usuarios:"
                        )
                        
                        UsageItem(
                            icon = Icons.Outlined.Person,
                            text = "Tu nombre de usuario y foto de perfil"
                        )
                        UsageItem(
                            icon = Icons.Outlined.Image,
                            text = "Tus publicaciones y productos"
                        )
                        UsageItem(
                            icon = Icons.Outlined.Store,
                            text = "Información de tu tienda (si la tenés)"
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PrivacyParagraph(
                            text = "Los mensajes privados solo son visibles entre vos y la persona con quien hablás."
                        )
                    }
                    
                    // 5. TUS DERECHOS
                    PrivacySection(
                        number = "5",
                        title = "Tus Derechos",
                        icon = Icons.Outlined.Security,
                        iconColor = Color(0xFF2E8B57)
                    ) {
                        PrivacyParagraph(
                            text = "Ten\u00e9s derecho a:"
                        )
                        
                        RightItem(
                            icon = Icons.Outlined.Visibility,
                            title = "Acceder",
                            description = "Solicitar ¿Qué datos tenemos sobre vos"
                        )
                        
                        RightItem(
                            icon = Icons.Outlined.Edit,
                            title = "Corregir",
                            description = "Modificar Información incorrecta desde tu perfil"
                        )
                        
                        RightItem(
                            icon = Icons.Outlined.Delete,
                            title = "Eliminar",
                            description = "Solicitar la eliminación de tu cuenta y datos"
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryPurple.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Para ejercer estos derechos, escribinos a soporte@Merqora.app",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    
                    // 6. SEGURIDAD
                    PrivacySection(
                        number = "6",
                        title = "Seguridad",
                        icon = Icons.Outlined.Lock,
                        iconColor = Color(0xFF6366F1)
                    ) {
                        PrivacyParagraph(
                            text = "Tomamos medidas razonables para proteger tu Información:"
                        )
                        
                        UsageItem(
                            icon = Icons.Outlined.Https,
                            text = "Conexiones seguras (HTTPS)"
                        )
                        UsageItem(
                            icon = Icons.Outlined.Key,
                            text = "contraseñas almacenadas de forma segura"
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PrivacyParagraph(
                            text = "Sin embargo, ningún sistema es 100% seguro. Hacemos nuestro mejor esfuerzo, pero no podemos garantizar seguridad absoluta."
                        )
                    }
                    
                    // 7. MENORES DE EDAD
                    PrivacySection(
                        number = "7",
                        title = "Menores de Edad",
                        icon = Icons.Outlined.ChildCare,
                        iconColor = Color(0xFF2E8B57)
                    ) {
                        PrivacyParagraph(
                            text = "Merqora es para usuarios mayores de 18 Años. No recopilamos intencionalmente Información de menores."
                        )
                        PrivacyParagraph(
                            text = "Si sos padre/tutor y creés que tu hijo menor usó la app, contactanos para eliminar la Información."
                        )
                    }
                    
                    // 8. CAMBIOS A ESTA política
                    PrivacySection(
                        number = "8",
                        title = "Cambios a esta política",
                        icon = Icons.Outlined.Update,
                        iconColor = Color(0xFF444444)
                    ) {
                        PrivacyParagraph(
                            text = "Esta Política puede actualizarse a medida que Merqora evolucione y se agreguen nuevas funcionalidades."
                        )
                        PrivacyParagraph(
                            text = "Te notificaremos sobre cambios importantes. El uso continuado de la app después de los cambios implica aceptación."
                        )
                    }
                    
                    // 9. LEY APLICABLE
                    PrivacySection(
                        number = "9",
                        title = "Ley Aplicable",
                        icon = Icons.Outlined.Balance,
                        iconColor = Color(0xFF444444)
                    ) {
                        PrivacyParagraph(
                            text = "Esta Política se rige por las leyes de la República Oriental del Uruguay y los principios generales de Protección de datos vigentes."
                        )
                    }
                    
                    // 10. CONTACTO
                    PrivacySection(
                        number = "10",
                        title = "Contacto",
                        icon = Icons.Outlined.Email,
                        iconColor = Color(0xFF2E8B57)
                    ) {
                        PrivacyParagraph(
                            text = "Si tenés preguntas sobre esta Política o tus datos:"
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PrivacyContactCard(
                            icon = Icons.Outlined.Email,
                            label = "Email",
                            value = "soporte@Merqora.app"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Footer
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
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Merqora © Uruguay",
                                fontSize = 10.sp,
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
private fun PrivacySection(
    number: String,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
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
private fun PrivacyParagraph(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextSecondary,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun DataTypeItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryPurple,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun UsageItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2E8B57),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ShareItem(
    title: String,
    description: String,
    color: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun SecurityFeature(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E8B57).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2E8B57),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RightItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2E8B57),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RetentionItem(
    type: String,
    period: String,
    reason: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = HomeBg
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = period,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6366F1)
                )
                Text(
                    text = reason,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun CookieType(
    name: String,
    description: String,
    canDisable: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (canDisable) Color(0xFF2E8B57).copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f)
        ) {
            Text(
                text = if (canDisable) "Opcional" else "Requerida",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (canDisable) Color(0xFF2E8B57) else TextMuted,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun TransferSafeguard(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF2E8B57),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun NotificationMethod(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = null,
            tint = PrimaryPurple,
            modifier = Modifier.size(6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun PrivacyContactCard(
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
