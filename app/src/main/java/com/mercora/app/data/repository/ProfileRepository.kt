package com.mercora.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.remote.CloudflareService
import com.mercora.app.data.remote.SupabaseClient
import com.mercora.app.ui.screens.profile.EditProfileData
import com.mercora.app.ui.screens.profile.ProfileData
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
    
    private val _selectedProfile = MutableStateFlow<ProfileData?>(null)
    val selectedProfile: StateFlow<ProfileData?> = _selectedProfile.asStateFlow()
    
    private val _selectedProfileIsLoading = MutableStateFlow(false)
    val selectedProfileIsLoading: StateFlow<Boolean> = _selectedProfileIsLoading.asStateFlow()
    
    private var lastFetchTime: Long = 0L
    
    private fun isCacheValid(): Boolean {
        return _currentProfile.value != null && 
               (System.currentTimeMillis() - lastFetchTime) < CACHE_DURATION_MS
    }
    
    suspend fun loadCurrentProfile(forceRefresh: Boolean = false): ProfileData? = withContext(Dispatchers.IO) {
        // Retornar cache si es vÃ¡lido y no se fuerza refresh
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
                Log.e(TAG, "âš ï¸ Usuario NO encontrado en tabla 'usuarios' para user_id: $userId")
                Log.e(TAG, "El usuario estÃ¡ autenticado pero no tiene registro en la BD")
            } else {
                Log.d(TAG, "âœ“ Usuario encontrado: ${usuario.username}")
            }
            
            val profileData = usuario?.let {
                // Obtener conteos desde FollowersRepository y PostRepository
                val seguidoresCount = FollowersRepository.getFollowersCount(it.userId)
                val clientesCount = FollowersRepository.getClientsCount(it.userId)
                val reputacionCalc = it.reputationScore?.toInt() 
                    ?: FollowersRepository.getReputation(it.userId)
                val publicacionesCount = PostRepository.getUserPostsCount(it.userId)
                
                // LOG DE DEPURACIÃ“N CRÃTICO PARA EL MODELO USUARIO (ENTRADA)
                Log.d(TAG, "==== USUARIO DESDE DB ====")
                Log.d(TAG, "ID: ${it.id}, UserID: ${it.userId}")
                Log.d(TAG, "Avatar DB: ${it.avatarUrl}")
                Log.d(TAG, "Banner DB: ${it.bannerUrl}")
                Log.d(TAG, "==========================")

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
                    isVerified = it.isVerified,
                    avatarShape = it.avatarShape
                )
            }
            
            // LOG DE DEPURACIÃ“N CRÃTICO PARA EL MODELO PROFILE DATA (SALIDA)
            if (profileData != null) {
                Log.d(TAG, "==== PROFILE DATA MAPEADO ====")
                Log.d(TAG, "User: ${profileData.username}")
                Log.d(TAG, "Avatar UI: ${profileData.avatarUrl}")
                Log.d(TAG, "Banner UI: ${profileData.bannerUrl}")
                Log.d(TAG, "==============================")
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
    
    suspend fun loadProfileByUserId(userId: String): ProfileData? = withContext(Dispatchers.IO) {
        try {
            _selectedProfileIsLoading.value = true
            
            val usuario = SupabaseClient.database
                .from("usuarios")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeSingleOrNull<Usuario>()
            
            if (usuario == null) {
                Log.e(TAG, "Usuario no encontrado: $userId")
                return@withContext null
            }
            
            val seguidoresCount = FollowersRepository.getFollowersCount(usuario.userId)
            val clientesCount = FollowersRepository.getClientsCount(usuario.userId)
            val reputacionCalc = usuario.reputationScore?.toInt()
                ?: FollowersRepository.getReputation(usuario.userId)
            val publicacionesCount = PostRepository.getUserPostsCount(usuario.userId)
            
            val profileData = ProfileData(
                userId = usuario.userId,
                username = usuario.username,
                nombre = usuario.nombre,
                nombreTienda = usuario.nombreTienda,
                descripcion = usuario.descripcion,
                avatarUrl = usuario.avatarUrl,
                bannerUrl = usuario.bannerUrl,
                ubicacion = usuario.ubicacion,
                telefono = usuario.whatsapp,
                sexo = usuario.genero,
                publicaciones = publicacionesCount,
                seguidores = seguidoresCount,
                clientes = clientesCount,
                reputacion = reputacionCalc,
                isVerified = usuario.isVerified
            )
            
            _selectedProfile.value = profileData
            Log.d(TAG, "Perfil cargado para userId: $userId -> ${profileData.username}")
            profileData
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando perfil para userId: $userId", e)
            null
        } finally {
            _selectedProfileIsLoading.value = false
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
            
            // Subir avatar a Cloudflare (portadas/perfiles - contenido de menor calidad)
            if (avatarUri != null) {
                Log.d(TAG, "Subiendo nuevo avatar a Cloudflare...")
                val oldAvatarUrl = data.avatarUrl
                if (!oldAvatarUrl.isNullOrBlank() && oldAvatarUrl.contains(CloudflareService.PUBLIC_DOMAIN)) {
                    Log.d(TAG, "Eliminando avatar anterior de Cloudflare: $oldAvatarUrl")
                    CloudflareService.deleteImage(oldAvatarUrl)
                }
                val bitmap = uriToBitmap(context, avatarUri)
                if (bitmap != null) {
                    val result = CloudflareService.uploadImage(
                        bitmap = bitmap,
                        folder = "avatars/$userId",
                        mediaType = com.vinzay.app.media.MediaOptimizer.MediaType.AVATAR
                    )
                    if (result.isFailure) {
                        Log.e(TAG, "Error subiendo avatar: ${result.exceptionOrNull()?.message}")
                        throw Exception("Error al subir foto de perfil: ${result.exceptionOrNull()?.message}")
                    }
                    finalAvatarUrl = result.getOrThrow()
                    Log.d(TAG, "Avatar subido a Cloudflare: $finalAvatarUrl")
                } else {
                    Log.e(TAG, "No se pudo convertir avatarUri a bitmap")
                    throw Exception("Error al procesar la imagen de perfil")
                }
            }
            
            // Subir banner a Cloudflare (portadas/perfiles)
            if (bannerUri != null) {
                Log.d(TAG, "Subiendo nuevo banner a Cloudflare...")
                val oldBannerUrl = data.bannerUrl
                if (!oldBannerUrl.isNullOrBlank() && oldBannerUrl.contains(CloudflareService.PUBLIC_DOMAIN)) {
                    Log.d(TAG, "Eliminando banner anterior de Cloudflare: $oldBannerUrl")
                    CloudflareService.deleteImage(oldBannerUrl)
                }
                val bitmap = uriToBitmap(context, bannerUri)
                if (bitmap != null) {
                    val result = CloudflareService.uploadImage(
                        bitmap = bitmap,
                        folder = "banners/$userId",
                        mediaType = com.vinzay.app.media.MediaOptimizer.MediaType.BANNER
                    )
                    if (result.isFailure) {
                        Log.e(TAG, "Error subiendo banner: ${result.exceptionOrNull()?.message}")
                        throw Exception("Error al subir banner: ${result.exceptionOrNull()?.message}")
                    }
                    finalBannerUrl = result.getOrThrow()
                    Log.d(TAG, "Banner subido a Cloudflare: $finalBannerUrl")
                } else {
                    Log.e(TAG, "No se pudo convertir bannerUri a bitmap")
                    throw Exception("Error al procesar la imagen del banner")
                }
            }
            
            // Actualizar en Supabase usando buildJsonObject
            val updateJson = buildJsonObject {
                put("username", data.username)
                put("nombre", data.nombre)
                put("descripcion", data.descripcion)
                put("ubicacion", data.ubicacion)
                put("nombre_tienda", data.nombreTienda)
                put("whatsapp", data.telefono) // TelÃ©fono se guarda en columna whatsapp
                put("genero", data.sexo)
                put("avatar_shape", data.avatarShape)
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
            
            // Recargar perfil (forzar refresh para que no devuelva cache viejo)
            val updatedProfile = loadCurrentProfile(forceRefresh = true)
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
