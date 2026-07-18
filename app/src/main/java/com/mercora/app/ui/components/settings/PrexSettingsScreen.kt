package com.mercora.app.ui.components.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mercora.app.data.remote.SessionPersistence
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.theme.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun PrexSettingsScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var prexPhone by remember { mutableStateOf("") }
    var prexAccount by remember { mutableStateOf("") }
    var prexAlias by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            isLoading = true
            try {
                val userId = SessionPersistence.getUserId()
                if (userId != null) {
                    val methods = SupabaseClient.database
                        .from("payout_methods")
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<JsonObject>()
                    methods.firstOrNull()?.let { row ->
                        prexPhone = row["prex_phone"]?.toString()?.trim('"') ?: ""
                        prexAccount = row["prex_account"]?.toString()?.trim('"') ?: ""
                        prexAlias = row["prex_alias"]?.toString()?.trim('"') ?: ""
                    }
                }
            } catch (_: Exception) {}
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
            .background(HomeBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF006D5B), Color(0xFF00B8A9), Color(0xFF00D2B5))
                            ),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Configurar Prex", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Tus datos para recibir pagos", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00B8A9))
                    }
                    return@Column
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF00B8A9).copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, Color(0xFF00B8A9).copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(Color(0xFF00B8A9).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Info, null, tint = Color(0xFF00B8A9), modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Â¿CÃ³mo funciona?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                "ConfigurÃ¡ tus datos de Prex para recibir pagos de otros usuarios. Al pagarte, ellos transferirÃ¡n directo a tu cuenta.",
                                fontSize = 12.sp, color = TextSecondary
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Datos de tu cuenta Prex", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(16.dp))

                        PrexField(
                            value = prexPhone,
                            onValueChange = { prexPhone = it },
                            label = "TelÃ©fono",
                            placeholder = "099 123 456",
                            icon = Icons.Outlined.Phone,
                            keyboardType = KeyboardType.Phone
                        )
                        Spacer(Modifier.height(12.dp))
                        PrexField(
                            value = prexAccount,
                            onValueChange = { prexAccount = it },
                            label = "NÃºmero de cuenta",
                            placeholder = "1234567890",
                            icon = Icons.Outlined.Pin,
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(12.dp))
                        PrexField(
                            value = prexAlias,
                            onValueChange = { prexAlias = it },
                            label = "Alias (opcional)",
                            placeholder = "tualias",
                            icon = Icons.Outlined.AlternateEmail,
                            keyboardType = KeyboardType.Text
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showSuccess,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AccentGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Datos guardados correctamente", fontSize = 14.sp, color = AccentGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            try {
                                val userId = SessionPersistence.getUserId() ?: return@launch
                                val existing = SupabaseClient.database
                                    .from("payout_methods")
                                    .select { filter { eq("user_id", userId) } }
                                    .decodeList<JsonObject>()

                                if (existing.isEmpty()) {
                                    SupabaseClient.database.from("payout_methods").insert(
                                        buildJsonObject {
                                            put("user_id", userId)
                                            put("prex_phone", prexPhone)
                                            put("prex_account", prexAccount)
                                            put("prex_alias", prexAlias)
                                            put("default_method", "prex")
                                        }
                                    )
                                } else {
                                    SupabaseClient.database.from("payout_methods").update(
                                        buildJsonObject {
                                            put("prex_phone", prexPhone)
                                            put("prex_account", prexAccount)
                                            put("prex_alias", prexAlias)
                                            put("default_method", "prex")
                                        }
                                    ) { filter { eq("user_id", userId) } }
                                }
                                showSuccess = true
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Error al guardar: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            isSaving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = (prexPhone.isNotEmpty() || prexAccount.isNotEmpty()) && !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B8A9))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.Save, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar datos de Prex", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar", color = TextMuted, fontSize = 14.sp)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PrexField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(icon, null, tint = Color(0xFF00B8A9), modifier = Modifier.size(20.dp)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E2E),
                unfocusedContainerColor = Color(0xFF1E1E2E),
                focusedBorderColor = Color(0xFF00B8A9),
                unfocusedBorderColor = Color(0xFF2D2D3D),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Color(0xFF00B8A9)
            )
        )
    }
}
