package com.rendly.app.ui.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendly.app.data.remote.SupabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun register(
        email: String,
        username: String,
        password: String,
        genero: String,
        fechaNacimiento: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            
            try {
                // Validaciones
                if (!email.contains("@")) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Ingresa un correo válido"
                    )}
                    return@launch
                }
                
                if (username.length < 3) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "El nombre de usuario debe tener al menos 3 caracteres"
                    )}
                    return@launch
                }
                
                if (password.length < 6) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "La contraseña debe tener al menos 6 caracteres"
                    )}
                    return@launch
                }
                
                if (genero.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Selecciona tu género"
                    )}
                    return@launch
                }
                
                // Validar fecha de nacimiento
                if (fechaNacimiento.length != 10) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Formato de fecha inválido (DD/MM/YYYY)"
                    )}
                    return@launch
                }
                
                val parts = fechaNacimiento.split("/")
                if (parts.size != 3) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Formato de fecha inválido"
                    )}
                    return@launch
                }
                
                val day = parts[0].toIntOrNull()
                val month = parts[1].toIntOrNull()
                val year = parts[2].toIntOrNull()
                
                if (day == null || month == null || year == null || 
                    day !in 1..31 || month !in 1..12 || year < 1900) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Fecha de nacimiento inválida"
                    )}
                    return@launch
                }
                
                // Verificar edad mínima (13 años)
                try {
                    val birthDate = LocalDate.of(year, month, day)
                    val age = Period.between(birthDate, LocalDate.now()).years
                    
                    if (age < 13) {
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = "Debes tener al menos 13 años"
                        )}
                        return@launch
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Fecha de nacimiento inválida"
                    )}
                    return@launch
                }
                
                // Registrar en Supabase Auth
                Log.d("RegisterViewModel", "Iniciando registro en Supabase Auth...")
                val signUpResult = SupabaseClient.auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    this.email = email
                    this.password = password
                }
                
                Log.d("RegisterViewModel", "SignUp completado, obteniendo usuario...")
                
                // Obtener el user ID del resultado del signup o del usuario actual
                val userId = signUpResult?.id 
                    ?: SupabaseClient.auth.currentUserOrNull()?.id
                    ?: SupabaseClient.auth.currentSessionOrNull()?.user?.id
                
                if (userId == null) {
                    Log.e("RegisterViewModel", "No se pudo obtener el userId después del registro")
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Error al crear la cuenta. Intenta iniciar sesión."
                    )}
                    return@launch
                }
                
                Log.d("RegisterViewModel", "UserId obtenido: $userId, insertando en tabla usuarios...")
                
                // Crear registro en tabla usuarios
                try {
                    SupabaseClient.database.from("usuarios")
                        .insert(buildJsonObject {
                            put("user_id", userId)
                            put("email", email)
                            put("username", username.lowercase())
                            put("genero", genero)
                            put("fecha_nacimiento", fechaNacimiento)
                            put("fecha_registro", Instant.now().toString())
                            put("avatar_url", "")
                            put("is_online", true)
                            put("last_online", Instant.now().toString())
                            put("recibir_novedades", false)
                            put("tiene_tienda", false)
                            put("baneado", false)
                            put("is_verified", false)
                        })
                    
                    Log.d("RegisterViewModel", "✅ Usuario insertado correctamente en tabla 'usuarios'")
                } catch (insertError: Exception) {
                    Log.e("RegisterViewModel", "❌ Error al insertar en tabla usuarios: ${insertError.message}", insertError)
                    // Continuar de todos modos ya que el usuario de Auth se creó
                    // El perfil se puede crear después en el primer login
                }
                
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true
                )}
                
            } catch (e: RestException) {
                Log.e("RegisterViewModel", "Register RestException: ${e.message}", e)
                val message = when {
                    e.message?.contains("already registered") == true || 
                    e.message?.contains("User already registered") == true -> 
                        "Este correo ya está registrado"
                    e.message?.contains("duplicate key") == true ->
                        "Este nombre de usuario ya está en uso"
                    e.message?.contains("Email not confirmed") == true ->
                        "Revisa tu correo para confirmar (o desactiva confirmación en Supabase)"
                    e.message?.contains("Signup requires a valid password") == true ->
                        "La contraseña debe tener al menos 6 caracteres"
                    e.message?.contains("Unable to validate email") == true ->
                        "El correo electrónico no es válido"
                    e.message?.contains("row-level security") == true ||
                    e.message?.contains("policy") == true ->
                        "Error de permisos en Supabase. Verifica las políticas RLS."
                    else -> "Error: ${e.message?.take(100) ?: "desconocido"}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Register Exception: ${e.javaClass.simpleName} - ${e.message}", e)
                val message = when {
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("Network") == true ->
                        "Sin conexión a internet"
                    e.message?.contains("timeout") == true ->
                        "Tiempo de espera agotado"
                    else -> "Error: ${e.message?.take(80) ?: "desconocido"}"
                }
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = message
                )}
            }
        }
    }
}
