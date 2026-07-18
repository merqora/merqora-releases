package com.vinzay.app.ui.screens.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinzay.app.data.biometric.BiometricEnrollmentManager
import com.vinzay.app.data.model.Usuario
import com.vinzay.app.data.remote.SessionPersistence
import com.vinzay.app.data.remote.SupabaseClient
import com.vinzay.app.util.FCMHelper
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
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isAnonymous: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    
    // Field-level validation hints
    val emailError: String? = null,
    val passwordError: String? = null,
    
    // Biometric availability
    val isBiometricAvailable: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkBiometricAvailability()
    }
    
    private fun checkBiometricAvailability() {
        val biometricManager = androidx.biometric.BiometricManager.from(application)
        val authResult = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
            androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        _uiState.update { it.copy(isBiometricAvailable = authResult == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) }
    }
    
    fun validateEmail(emailOrUsername: String) {
        val trimmed = emailOrUsername.trim()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(emailError = null) }
            return
        }
        if (trimmed.contains("@")) {
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (!emailRegex.matches(trimmed)) {
                _uiState.update { it.copy(emailError = "Correo electrónico no válido") }
                return
            }
        }
        _uiState.update { it.copy(emailError = null) }
    }
    
    fun validatePassword(password: String) {
        if (password.isEmpty()) {
            _uiState.update { it.copy(passwordError = null) }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Mínimo 6 caracteres") }
            return
        }
        _uiState.update { it.copy(passwordError = null) }
    }
    
    fun login(emailOrUsername: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "", emailError = null, passwordError = null) }
            
            val trimmed = emailOrUsername.trim()
            if (trimmed.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, emailError = "Ingresa tu correo o usuario") }
                return@launch
            }
            if (password.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, passwordError = "Ingresa tu contraseña") }
                return@launch
            }
            if (password.length < 6) {
                _uiState.update { it.copy(isLoading = false, passwordError = "Contraseña demasiado corta") }
                return@launch
            }
            
            try {
                var emailToUse = trimmed
                val inputLower = trimmed.lowercase()
                
                // Username lookup if not email
                if (!emailToUse.contains("@")) {
                    Log.d(TAG, "Buscando usuario: $inputLower")
                    val userResult = try {
                        SupabaseClient.database.from("usuarios")
                            .select { filter { eq("username", inputLower) } }
                            .decodeSingleOrNull<Usuario>()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error buscando usuario: ${e.message}", e)
                        null
                    }
                    
                    if (userResult == null || userResult.email.isNullOrEmpty()) {
                        _uiState.update { it.copy(isLoading = false, emailError = "Usuario '$inputLower' no encontrado") }
                        return@launch
                    }
                    emailToUse = userResult.email!!
                }
                
                // Sign in
                SupabaseClient.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    this.email = emailToUse
                    this.password = password
                }
                
                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                if (userId == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al obtener sesión") }
                    return@launch
                }
                
                // Sync user profile
                val username = syncUserProfile(userId, emailToUse, rememberMe)
                SessionPersistence.saveSession(userId, username)
                FCMHelper.forceTokenRefresh(application)

                // Save credentials for biometric re-login
                BiometricEnrollmentManager.saveLoginCredentials(
                    application.applicationContext,
                    emailToUse,
                    password
                )
                
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                
            } catch (e: RestException) {
                Log.e(TAG, "RestException: ${e.message}", e)
                val message = when {
                    e.message?.contains("Invalid login credentials") == true -> "Contraseña incorrecta"
                    e.message?.contains("Email not confirmed") == true -> "Email no confirmado. Revisa tu correo."
                    e.message?.contains("User not found") == true -> "Usuario no encontrado"
                    else -> "Error: ${e.message?.take(50) ?: "desconocido"}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.javaClass.simpleName} - ${e.message}", e)
                val message = when {
                    e.message?.contains("Unable to resolve host") == true || e.message?.contains("Network") == true -> "Sin conexión a internet"
                    e.message?.contains("timeout") == true -> "Tiempo de espera agotado. Intenta de nuevo."
                    else -> "Error: ${e.message?.take(50) ?: "desconocido"}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        }
    }
    
    private suspend fun syncUserProfile(userId: String, email: String, rememberMe: Boolean): String {
        return try {
            val existingUser = SupabaseClient.database.from("usuarios")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<Usuario>()
            
            if (existingUser != null) {
                SupabaseClient.database.from("usuarios").update(buildJsonObject {
                    put("is_online", true)
                    put("last_online", Instant.now().toString())
                    put("recibir_novedades", rememberMe)
                }) { filter { eq("user_id", userId) } }
                existingUser.username ?: email.substringBefore("@").lowercase()
            } else {
                val newUsername = email.substringBefore("@").lowercase()
                Log.w(TAG, "⚠️ Usuario no existe en 'usuarios', creando perfil...")
                SupabaseClient.database.from("usuarios").insert(buildJsonObject {
                    put("user_id", userId)
                    put("email", SupabaseClient.auth.currentUserOrNull()?.email ?: email)
                    put("username", newUsername)
                    put("fecha_registro", Instant.now().toString())
                    put("avatar_url", "")
                    put("is_online", true)
                    put("last_online", Instant.now().toString())
                    put("recibir_novedades", rememberMe)
                    put("tiene_tienda", false)
                    put("baneado", false)
                    put("is_verified", false)
                })
                newUsername
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sync user profile: ${e.message}", e)
            email.substringBefore("@").lowercase()
        }
    }
    
    fun loginAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                val guestUsername = "usuario#${System.currentTimeMillis()}"
                val guestEmail = "guest_${System.currentTimeMillis()}@merqora.temp"
                val guestPassword = java.util.UUID.randomUUID().toString()
                
                SupabaseClient.auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    this.email = guestEmail
                    this.password = guestPassword
                }
                
                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: run {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al crear sesión de invitado") }
                    return@launch
                }
                
                SupabaseClient.database.from("usuarios").insert(buildJsonObject {
                    put("user_id", userId)
                    put("email", null)
                    put("username", guestUsername)
                    put("fecha_registro", Instant.now().toString())
                    put("avatar_url", "")
                    put("is_online", true)
                    put("last_online", Instant.now().toString())
                    put("is_anonymous", true)
                    put("recibir_novedades", false)
                })
                
                SessionPersistence.saveSession(userId, guestUsername)
                FCMHelper.forceTokenRefresh(application)
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true, isAnonymous = true) }
            } catch (e: Exception) {
                Log.e(TAG, "Guest login error", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo crear sesión de invitado") }
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                if (userId != null && _uiState.value.isAnonymous) {
                    SupabaseClient.database.from("usuarios").delete()
                }
                FCMHelper.onUserLogout(application)
                SupabaseClient.auth.signOut()
                SessionPersistence.clearSession()
                com.vinzay.app.data.cache.BadgeCountCache.clearAll()
                _uiState.update { it.copy(isAuthenticated = false, isAnonymous = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Sign out error", e)
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
    
    companion object {
        private const val TAG = "LoginViewModel"
    }
}
