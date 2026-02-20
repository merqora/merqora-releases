package com.rendly.app.ui.components.settings

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.data.repository.SecurityRepository
import com.rendly.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TwoFactorSetupScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var secretKey by remember { mutableStateOf<String?>(null) }
    var verificationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSetupComplete by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    // Iniciar enrollment cuando se abre la pantalla
    LaunchedEffect(isVisible) {
        if (isVisible && secretKey == null) {
            isLoading = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
            if (userId != null) {
                val (success, secret) = SecurityRepository.enable2FA(userId)
                if (success && secret != null) {
                    secretKey = secret
                }
            }
            isLoading = false
        }
    }
    
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
            .clickable(onClick = {
                onDismiss()
            })
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
                    .navigationBarsPadding()
            ) {
                SettingsScreenHeader(
                    title = "Configurar 2FA",
                    subtitle = "Autenticación de dos factores",
                    icon = Icons.Outlined.Security,
                    iconColor = Color(0xFF2E8B57),
                    onBack = {
                        onDismiss()
                    }
                )
                
                if (isLoading && secretKey == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryPurple)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Generando clave secreta...",
                                color = TextSecondary
                            )
                        }
                    }
                } else if (isSetupComplete) {
                    // Pantalla de éxito
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFF2E8B57).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E8B57),
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "¡2FA Activado!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Tu cuenta ahora está protegida con autenticación de dos factores",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple
                                )
                            ) {
                                Text(
                                    text = "Continuar",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Progress indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            StepIndicator(step = 1, currentStep = currentStep, label = "App")
                            StepConnector(isActive = currentStep >= 2)
                            StepIndicator(step = 2, currentStep = currentStep, label = "Verificar")
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        when (currentStep) {
                            1 -> {
                                // Paso 1: Configurar app autenticadora
                                Text(
                                    text = "Configura tu app autenticadora",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Usa una app como Google Authenticator, Authy o Microsoft Authenticator",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Clave secreta
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Surface
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Clave secreta",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextMuted
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = secretKey?.chunked(4)?.joinToString(" ") ?: "...",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            IconButton(
                                                onClick = {
                                                    secretKey?.let {
                                                        clipboardManager.setText(AnnotatedString(it))
                                                        Toast.makeText(context, "Clave copiada", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.ContentCopy,
                                                    contentDescription = "Copiar",
                                                    tint = PrimaryPurple
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Instrucciones
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1565A0).copy(alpha = 0.08f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Info,
                                                contentDescription = null,
                                                tint = Color(0xFF1565A0),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Instrucciones",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1565A0)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        InstructionItem("1. Abre tu app autenticadora")
                                        InstructionItem("2. Toca el botón + para agregar cuenta")
                                        InstructionItem("3. Selecciona 'Introducir clave manualmente'")
                                        InstructionItem("4. Pega o escribe la clave secreta")
                                        InstructionItem("5. Guarda y usa el código generado")
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Button(
                                    onClick = { currentStep = 2 },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryPurple
                                    )
                                ) {
                                    Text(
                                        text = "Ya configuré mi app",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            2 -> {
                                // Paso 2: Verificar código
                                Text(
                                    text = "Verifica el código",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Ingresa el código de 6 dígitos que muestra tu app autenticadora",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                // Input de código
                                OTPInput(
                                    code = verificationCode,
                                    onCodeChange = { 
                                        verificationCode = it
                                        errorMessage = null
                                    }
                                )
                                
                                errorMessage?.let { error ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFEF4444).copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Error,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = error,
                                                color = Color(0xFFEF4444),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isLoading = true
                                            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                                            val success = SecurityRepository.verify2FACode(verificationCode, userId)
                                            if (success) {
                                                isSetupComplete = true
                                                Toast.makeText(context, "2FA activado correctamente", Toast.LENGTH_SHORT).show()
                                            } else {
                                                errorMessage = "Código incorrecto. Verifica e intenta de nuevo."
                                            }
                                            isLoading = false
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    enabled = verificationCode.length == 6 && !isLoading,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E8B57),
                                        disabledContainerColor = Color(0xFF2E8B57).copy(alpha = 0.5f)
                                    )
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verificar y activar",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                TextButton(
                                    onClick = { currentStep = 1 },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Volver al paso anterior",
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    step: Int,
    currentStep: Int,
    label: String
) {
    val isActive = currentStep >= step
    val isComplete = currentStep > step
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    when {
                        isComplete -> Color(0xFF2E8B57)
                        isActive -> PrimaryPurple
                        else -> Surface
                    }
                )
                .then(
                    if (!isActive) Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else TextMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) TextPrimary else TextMuted
        )
    }
}

@Composable
private fun StepConnector(isActive: Boolean) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .padding(bottom = 24.dp)
            .width(60.dp)
            .height(2.dp)
            .background(if (isActive) PrimaryPurple else BorderSubtle)
    )
}

@Composable
private fun InstructionItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun OTPInput(
    code: String,
    onCodeChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Hidden text field
        BasicTextField(
            value = code,
            onValueChange = { 
                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                    onCodeChange(it)
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        // Visual boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(6) { index ->
                val char = code.getOrNull(index)
                val isFocused = code.length == index
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                        .border(
                            width = 2.dp,
                            color = when {
                                isFocused -> PrimaryPurple
                                char != null -> Color(0xFF2E8B57)
                                else -> BorderSubtle
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char?.toString() ?: "",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
