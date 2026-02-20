package com.rendly.app.ui.screens.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.util.FCMHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.user.UserInfo
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
    val successMessage: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    // ═══════════════════════════════════════════════════════════════════
    // COLD START OPTIMIZATION: Removido init block que forzaba
    // inicialización de Supabase en Main Thread durante composición
    // 
    // La verificación de sesión ya se hace en MainActivity.LaunchedEffect
    // No necesitamos duplicarla aquí y causar bloqueo de ~2000ms
    // ═══════════════════════════════════════════════════════════════════
    
    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                val session = SupabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    val user = session.user
                    if (user != null) {
                        _uiState.update { it.copy(
                            isAuthenticated = true,
                            isAnonymous = false
                        )}
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error checking session", e)
            }
        }
    }
    
    fun login(emailOrUsername: String, password: String, recibirNovedades: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            
            try {
                var emailToUse = emailOrUsername.trim()
                val inputLower = emailOrUsername.trim().lowercase()
                
                // Si no es email, buscar username en DB con filtro directo
                if (!emailToUse.contains("@")) {
                    Log.d("LoginViewModel", "Buscando usuario: $inputLower")
                    
                    val userResult = try {
                        // Usar filtro eq para buscar directamente en la DB
                        SupabaseClient.database.from("usuarios")
                            .select {
                                filter {
                                    eq("username", inputLower)
                                }
                            }
                            .decodeSingleOrNull<Usuario>()
                    } catch (e: Exception) {
                        Log.e("LoginViewModel", "Error buscando usuario: ${e.message}", e)
                        null
                    }
                    
                    if (userResult == null || userResult.email.isNullOrEmpty()) {
                        Log.d("LoginViewModel", "Usuario no encontrado en DB o sin email")
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = "Usuario '$inputLower' no encontrado"
                        )}
                        return@launch
                    }
                    
                    emailToUse = userResult.email!!
                    Log.d("LoginViewModel", "Email encontrado: $emailToUse")
                }
                
                // Login con Supabase Auth
                Log.d("LoginViewModel", "Intentando login con email: $emailToUse")
                SupabaseClient.auth.signInWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    this.email = emailToUse
                    this.password = password
                }
                
                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                Log.d("LoginViewModel", "Login exitoso, userId: $userId")
                
                if (userId == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Error al obtener sesión"
                    )}
                    return@launch
                }
                
                // Verificar si el usuario existe en la tabla usuarios
                try {
                    val existingUser = SupabaseClient.database.from("usuarios")
                        .select {
                            filter {
                                eq("user_id", userId)
                            }
                        }
                        .decodeSingleOrNull<Usuario>()
                    
                    if (existingUser != null) {
                        // Usuario existe, actualizar estado online
                        SupabaseClient.database.from("usuarios")
                            .update(buildJsonObject {
                                put("is_online", true)
                                put("last_online", Instant.now().toString())
                                put("recibir_novedades", recibirNovedades)
                            }) {
                                filter {
                                    eq("user_id", userId)
                                }
                            }
                        Log.d("LoginViewModel", "Usuario actualizado en DB")
                    } else {
                        // Usuario NO existe en tabla usuarios - crearlo ahora
                        Log.w("LoginViewModel", "⚠️ Usuario no existe en tabla 'usuarios', creando perfil...")
                        val currentUserEmail = SupabaseClient.auth.currentUserOrNull()?.email ?: emailToUse
                        
                        SupabaseClient.database.from("usuarios")
                            .insert(buildJsonObject {
                                put("user_id", userId)
                                put("email", currentUserEmail)
                                put("username", currentUserEmail.substringBefore("@").lowercase())
                                put("fecha_registro", Instant.now().toString())
                                put("avatar_url", "")
                                put("is_online", true)
                                put("last_online", Instant.now().toString())
                                put("recibir_novedades", recibirNovedades)
                                put("tiene_tienda", false)
                                put("baneado", false)
                                put("is_verified", false)
                            })
                        Log.d("LoginViewModel", "✅ Perfil de usuario creado en tabla 'usuarios'")
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Error verificando/creando usuario: ${e.message}", e)
                    // No es crítico, continuar con el login
                }
                
                // SESSION PERSISTENCE: Guardar sesión
                com.rendly.app.data.remote.SessionPersistence.saveSession(userId)
                
                // FCM: Forzar regeneración del token FCM para asegurar que sea válido
                FCMHelper.forceTokenRefresh(application)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    isAnonymous = false
                )}
                
            } catch (e: RestException) {
                Log.e("LoginViewModel", "RestException: ${e.message}", e)
                val message = when {
                    e.message?.contains("Invalid login credentials") == true -> 
                        "Contraseña incorrecta"
                    e.message?.contains("Email not confirmed") == true ->
                        "Email no confirmado. Revisa tu correo."
                    e.message?.contains("User not found") == true ->
                        "Usuario no encontrado"
                    e.message?.contains("Network") == true || 
                    e.message?.contains("timeout") == true ||
                    e.message?.contains("Unable to resolve host") == true ->
                        "Sin conexión a internet"
                    else -> "Error: ${e.message?.take(50) ?: "desconocido"}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            } catch (e: java.net.UnknownHostException) {
                Log.e("LoginViewModel", "No internet", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Sin conexión a internet"
                )}
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("LoginViewModel", "Timeout", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Tiempo de espera agotado. Intenta de nuevo."
                )}
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception: ${e.javaClass.simpleName} - ${e.message}", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message?.take(50) ?: "desconocido"}"
                )}
            }
        }
    }
    
    fun loginAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            
            try {
                // Generar nombre de usuario invitado con timestamp
                val guestUsername = "usuario#${System.currentTimeMillis()}"
                val guestEmail = "guest_${System.currentTimeMillis()}@Merqora.temp"
                val guestPassword = java.util.UUID.randomUUID().toString()
                
                // Crear cuenta temporal
                SupabaseClient.auth.signUpWith(io.github.jan.supabase.gotrue.providers.builtin.Email) {
                    this.email = guestEmail
                    this.password = guestPassword
                }
                
                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: run {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Error al crear sesión de invitado"
                    )}
                    return@launch
                }
                
                // Crear registro de usuario invitado
                SupabaseClient.database.from("usuarios")
                    .insert(buildJsonObject {
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
                
                // SESSION PERSISTENCE: Guardar sesión de invitado
                com.rendly.app.data.remote.SessionPersistence.saveSession(userId)
                
                // FCM: Forzar regeneración del token FCM para invitados también
                FCMHelper.forceTokenRefresh(application)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    isAnonymous = true
                )}
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Guest login error", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "No se pudo crear sesión de invitado"
                )}
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                val userId = SupabaseClient.auth.currentUserOrNull()?.id
                
                if (userId != null && _uiState.value.isAnonymous) {
                    // Eliminar usuario invitado de DB
                    SupabaseClient.database.from("usuarios")
                        .delete()
                }
                
                // FCM: Desactivar token antes de cerrar sesión
                FCMHelper.onUserLogout(application)
                
                SupabaseClient.auth.signOut()
                
                // SESSION PERSISTENCE: Limpiar sesión guardada
                com.rendly.app.data.remote.SessionPersistence.clearSession()
                
                // Limpiar cache de badges
                com.rendly.app.data.cache.BadgeCountCache.clearAll()
                
                _uiState.update { it.copy(
                    isAuthenticated = false,
                    isAnonymous = false,
                    successMessage = "Sesión cerrada correctamente"
                )}
                
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Sign out error", e)
                _uiState.update { it.copy(
                    errorMessage = "Error al cerrar sesión"
                )}
            }
        }
    }
}
