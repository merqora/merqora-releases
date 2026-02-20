package com.rendly.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.rendly.app.data.model.Usuario
import com.rendly.app.data.remote.CloudflareService
import com.rendly.app.data.remote.SupabaseClient
import com.rendly.app.ui.screens.profile.EditProfileData
import com.rendly.app.ui.screens.profile.ProfileData
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.InputStream

object ProfileRepository {
    private const val TAG = "ProfileRepository"
    private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutos
    
    private val _currentProfile = MutableStateFlow<ProfileData?>(null)
    val currentProfile: StateFlow<ProfileData?> = _currentProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var lastFetchTime: Long = 0L
    
    private fun isCacheValid(): Boolean {
        return _currentProfile.value != null && 
               (System.currentTimeMillis() - lastFetchTime) < CACHE_DURATION_MS
    }
    
    suspend fun loadCurrentProfile(forceRefresh: Boolean = false): ProfileData? = withContext(Dispatchers.IO) {
        // Retornar cache si es válido y no se fuerza refresh
        if (!forceRefresh && isCacheValid()) {
            Log.d(TAG, "Retornando perfil desde cache")
            return@withContext _currentProfile.value
        }
        try {
            _isLoading.value = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Usuario no autenticado")
            
            Log.d(TAG, "Cargando perfil para: $userId")
            
            val usuario = SupabaseClient.database
                .from("usuarios")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<Usuario>()
            
            if (usuario == null) {
                Log.e(TAG, "⚠️ Usuario NO encontrado en tabla 'usuarios' para user_id: $userId")
                Log.e(TAG, "El usuario está autenticado pero no tiene registro en la BD")
            } else {
                Log.d(TAG, "✓ Usuario encontrado: ${usuario.username}")
            }
            
            val profileData = usuario?.let {
                // Obtener conteos desde FollowersRepository y PostRepository
                val seguidoresCount = FollowersRepository.getFollowersCount(it.userId)
                val clientesCount = FollowersRepository.getClientsCount(it.userId)
                val reputacionCalc = FollowersRepository.getReputation(it.userId)
                val publicacionesCount = PostRepository.getUserPostsCount(it.userId)
                
                ProfileData(
                    userId = it.userId,
                    username = it.username,
                    nombre = it.nombre,
                    nombreTienda = it.nombreTienda,
                    descripcion = it.descripcion,
                    avatarUrl = it.avatarUrl,
                    bannerUrl = it.bannerUrl,
                    ubicacion = it.ubicacion,
                    telefono = it.whatsapp,
                    sexo = it.genero,
                    publicaciones = publicacionesCount,
                    seguidores = seguidoresCount,
                    clientes = clientesCount,
                    reputacion = reputacionCalc,
                    isVerified = it.isVerified
                )
            }
            
            _currentProfile.value = profileData
            lastFetchTime = System.currentTimeMillis()
            Log.d(TAG, "Perfil cargado: ${profileData?.username}")
            profileData
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando perfil", e)
            null
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateProfile(
        context: Context,
        data: EditProfileData,
        avatarUri: Uri?,
        bannerUri: Uri?
    ): Result<ProfileData> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val userId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Usuario no autenticado")
            
            Log.d(TAG, "Actualizando perfil para: $userId")
            
            var finalAvatarUrl = data.avatarUrl
            var finalBannerUrl = data.bannerUrl
            
            // Subir avatar a Cloudflare si hay uno nuevo
            if (avatarUri != null) {
                Log.d(TAG, "Subiendo nuevo avatar a Cloudflare...")
                val bitmap = uriToBitmap(context, avatarUri)
                if (bitmap != null) {
                    val result = CloudflareService.uploadImage(
                        bitmap = bitmap,
                        folder = "avatars/$userId"
                    )
                    finalAvatarUrl = result.getOrNull()
                    Log.d(TAG, "Avatar subido: $finalAvatarUrl")
                }
            }
            
            // Subir banner a Cloudflare si hay uno nuevo
            if (bannerUri != null) {
                Log.d(TAG, "Subiendo nuevo banner a Cloudflare...")
                val bitmap = uriToBitmap(context, bannerUri)
                if (bitmap != null) {
                    val result = CloudflareService.uploadImage(
                        bitmap = bitmap,
                        folder = "banners/$userId"
                    )
                    finalBannerUrl = result.getOrNull()
                    Log.d(TAG, "Banner subido: $finalBannerUrl")
                }
            }
            
            // Actualizar en Supabase usando buildJsonObject
            val updateJson = buildJsonObject {
                put("username", data.username)
                put("nombre", data.nombre)
                put("descripcion", data.descripcion)
                put("ubicacion", data.ubicacion)
                put("nombre_tienda", data.nombreTienda)
                put("whatsapp", data.telefono) // Teléfono se guarda en columna whatsapp
                put("genero", data.sexo)
                if (finalAvatarUrl != null) {
                    put("avatar_url", finalAvatarUrl)
                }
                if (finalBannerUrl != null) {
                    put("banner_url", finalBannerUrl)
                }
            }
            
            Log.d(TAG, "Actualizando en Supabase: $updateJson")
            
            SupabaseClient.database
                .from("usuarios")
                .update(updateJson) {
                    filter { eq("user_id", userId) }
                }
            
            // Recargar perfil
            val updatedProfile = loadCurrentProfile()
            Log.d(TAG, "Perfil actualizado correctamente")
            
            Result.success(updatedProfile ?: throw Exception("Error al recargar perfil"))
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando perfil", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo URI a Bitmap", e)
            null
        }
    }
    
    fun clearProfile() {
        _currentProfile.value = null
        lastFetchTime = 0L
    }
    
    fun invalidateCache() {
        lastFetchTime = 0L
    }
}
