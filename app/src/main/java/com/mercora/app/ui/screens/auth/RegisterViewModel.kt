package com.mercora.app.ui.screens.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.model.WelcomeState
import com.mercora.app.data.remote.SessionPersistence
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.util.FCMHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    
    // Field-level errors
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generoError: String? = null,
    val fechaError: String? = null,
    
    // Username availability
    val isCheckingUsername: Boolean = false,
    val isUsernameAvailable: Boolean? = null, // null = not checked, true = available, false = taken
    
    // Password strength
    val passwordStrength: Float = 0f,  // 0.0 to 1.0
    val passwordStrengthLabel: String = ""
)

data class PasswordStrength(
    val score: Float,
    val label: String
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    private var usernameCheckJob: Job? = null
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // VALIDATION HELPERS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    
    fun validateEmail(email: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(emailError = null) }
            return
        }
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(trimmed)) {
            _uiState.update { it.copy(emailError = "Correo electrÃ³nico no vÃ¡lido") }
            return
        }
        _uiState.update { it.copy(emailError = null) }
    }
    
    fun validateUsername(username: String) {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(usernameError = null, isUsernameAvailable = null) }
            return
        }
        if (trimmed.length < 3) {
            _uiState.update { it.copy(usernameError = "MÃ­nimo 3 caracteres", isUsernameAvailable = null) }
            return
        }
        if (!trimmed.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _uiState.update { it.copy(usernameError = "Solo letras, nÃºmeros y _", isUsernameAvailable = null) }
            return
        }
        _uiState.update { it.copy(usernameError = null) }
        
        // Debounced check for availability
        usernameCheckJob?.cancel()
        usernameCheckJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            checkUsernameAvailability(trimmed.lowercase())
        }
    }
    
    private suspend fun checkUsernameAvailability(username: String) {
        _uiState.update { it.copy(isCheckingUsername = true) }
        try {
            val existing = SupabaseClient.database.from("usuarios")
                .select { filter { eq("username", username) } }
                .decodeList<Usuario>()
            _uiState.update { it.copy(
                isCheckingUsername = false,
                isUsernameAvailable = existing.isEmpty(),
                usernameError = if (existing.isNotEmpty()) "Este usuario ya estÃ¡ en uso" else null
            )}
        } catch (e: Exception) {
            Log.e(TAG, "Error checking username", e)
            _uiState.update { it.copy(isCheckingUsername = false, isUsernameAvailable = null) }
        }
    }
    
    fun validatePassword(password: String) {
        if (password.isEmpty()) {
            _uiState.update { it.copy(passwordError = null, passwordStrength = 0f, passwordStrengthLabel = "") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "MÃ­nimo 6 caracteres") }
        } else {
            _uiState.update { it.copy(passwordError = null) }
        }
        
        // Calculate strength
        val strength = calculatePasswordStrength(password)
        _uiState.update { it.copy(
            passwordStrength = strength.score,
            passwordStrengthLabel = strength.label
        )}
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String) {
        if (confirmPassword.isEmpty()) {
            _uiState.update { it.copy(confirmPasswordError = null) }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Las contraseÃ±as no coinciden") }
        } else {
            _uiState.update { it.copy(confirmPasswordError = null) }
        }
    }
    
    fun validateDate(fechaNacimiento: String) {
        if (fechaNacimiento.isEmpty()) {
            _uiState.update { it.copy(fechaError = null) }
            return
        }
        if (fechaNacimiento.length != 10) {
            _uiState.update { it.copy(fechaError = "Formato: DD/MM/YYYY") }
            return
        }
        val parts = fechaNacimiento.split("/")
        if (parts.size != 3) {
            _uiState.update { it.copy(fechaError = "Formato invÃ¡lido") }
            return
        }
        val day = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val year = parts[2].toIntOrNull()
        if (day == null || month == null || year == null || day !in 1..31 || month !in 1..12 || year !in 1900..LocalDate.now().year) {
            _uiState.update { it.copy(fechaError = "Fecha invÃ¡lida") }
            return
        }
        try {
            val birthDate = LocalDate.of(year, month, day)
            if (Period.between(birthDate, LocalDate.now()).years < 13) {
                _uiState.update { it.copy(fechaError = "Debes tener al menos 13 aÃ±os") }
                return
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(fechaError = "Fecha invÃ¡lida") }
            return
        }
        _uiState.update { it.copy(fechaError = null) }
    }
    
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        var score = 0f
        val len = password.length
        
        // Length scoring
        score += when {
            len >= 12 -> 0.30f
            len >= 8 -> 0.20f
            len >= 6 -> 0.10f
            else -> 0.05f
        }
        
        // Has uppercase
        if (password.any { it.isUpperCase() }) score += 0.15f
        // Has lowercase
        if (password.any { it.isLowerCase() }) score += 0.10f
        // Has digit
        if (password.any { it.isDigit() }) score += 0.15f
        // Has special char
        if (password.any { !it.isLetterOrDigit() }) score += 0.20f
        // Length bonus
        if (len >= 16) score += 0.10f
        
        // Clamp to 0..1
        score = score.coerceIn(0f, 1f)
        
        val label = when {
            score < 0.25f -> "DÃ©bil"
            score < 0.50f -> "Regular"
            score < 0.75f -> "Buena"
            else -> "Segura"
        }
        
        return PasswordStrength(score, label)
    }
    
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // REGISTER
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    
    fun register(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        genero: String,
        fechaNacimiento: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            
            // === Client-side validation ===
            
            // Email
            if (email.isBlank() || !email.contains("@")) {
                _uiState.update { it.copy(isLoading = false, emailError = "Ingresa un correo vÃ¡lido") }
                return@launch
            }
            
            // Username
            val trimmedUsername = username.trim().lowercase()
            if (trimmedUsername.length < 3) {
                _uiState.update { it.copy(isLoading = false, usernameError = "MÃ­nimo 3 caracteres") }
                return@launch
            }
            if (!trimmedUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                _uiState.update { it.copy(isLoading = false, usernameError = "Solo letras, nÃºmeros y _") }
                return@launch
            }
            
            // Password
            if (password.length < 6) {
                _uiState.update { it.copy(isLoading = false, passwordError = "MÃ­nimo 6 caracteres") }
                return@launch
            }
            
            // Confirm password
            if (password != confirmPassword) {
                _uiState.update { it.copy(isLoading = false, confirmPasswordError = "Las contraseÃ±as no coinciden") }
                return@launch
            }
            
            // Gender
            if (genero.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, generoError = "Selecciona tu gÃ©nero") }
                return@launch
            }
            
            // Date
            val parts = fechaNacimiento.split("/")
            if (parts.size != 3) {
                _uiState.update { it.copy(isLoading = false, fechaError = "Fecha invÃ¡lida") }
                return@launch
            }
            val day = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            val year = parts[2].toIntOrNull()
            if (day == null || month == null || year == null) {
                _uiState.update { it.copy(isLoading = false, fechaError = "Fecha invÃ¡lida") }
                return@launch
            }
            
            // Age check
            try {
                val birthDate = LocalDate.of(year, month, day)
                if (Period.between(birthDate, LocalDate.now()).years < 13) {
                    _uiState.update { it.copy(isLoading = false, fechaError = "Debes tener al menos 13 aÃ±os") }
                    return@launch
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, fechaError = "Fecha invÃ¡lida") }
                return@launch
            }
            
            // === Register ===
            try {
                Log.d(TAG, "Iniciando registro en Supabase Auth...")
                val signUpResult = SupabaseClient.auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    this.email = email
                    this.password = password
                }
                
                val userId = signUpResult?.id
                    ?: SupabaseClient.auth.currentUserOrNull()?.id
                    ?: SupabaseClient.auth.currentSessionOrNull()?.user?.id
                
                if (userId == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Error al crear la cuenta. Intenta iniciar sesiÃ³n."
                    )}
                    return@launch
                }
                
                // Create user profile
                try {
                    SupabaseClient.database.from("usuarios").insert(buildJsonObject {
                        put("user_id", userId)
                        put("email", email)
                        put("username", trimmedUsername)
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
                    Log.d(TAG, "âœ… Usuario insertado correctamente en 'usuarios'")
                } catch (insertError: Exception) {
                    Log.e(TAG, "âŒ Error insertando en usuarios: ${insertError.message}")
                }
                
                // Save session + refresh FCM token
                SessionPersistence.saveSession(userId, trimmedUsername)
                FCMHelper.forceTokenRefresh(application)
                
                // Trigger welcome animation for next screen
                WelcomeState.trigger(trimmedUsername)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true
                )}
                
            } catch (e: RestException) {
                Log.e(TAG, "Register RestException: ${e.message}", e)
                val message = when {
                    e.message?.contains("already registered") == true || 
                    e.message?.contains("User already registered") == true -> 
                        "Este correo ya estÃ¡ registrado"
                    e.message?.contains("duplicate key") == true ->
                        "Este nombre de usuario ya estÃ¡ en uso"
                    else -> "Error: ${e.message?.take(100) ?: "desconocido"}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            } catch (e: Exception) {
                Log.e(TAG, "Register Exception: ${e.javaClass.simpleName} - ${e.message}", e)
                val message = when {
                    e.message?.contains("Unable to resolve host") == true || e.message?.contains("Network") == true ->
                        "Sin conexiÃ³n a internet"
                    e.message?.contains("timeout") == true -> "Tiempo de espera agotado"
                    else -> "Error: ${e.message?.take(80) ?: "desconocido"}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
    
    companion object {
        private const val TAG = "RegisterViewModel"
    }
}
