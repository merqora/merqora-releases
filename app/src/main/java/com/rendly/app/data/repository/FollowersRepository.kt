package com.rendly.app.data.repository

import android.util.Log
import com.rendly.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class FollowerRelation(
    val id: String = "",
    @SerialName("follower_id") val followerId: String = "",
    @SerialName("followed_id") val followedId: String = "",
    @SerialName("is_client") val isClient: Boolean = false,
    @SerialName("is_pending") val isPending: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

enum class FollowType {
    NONE,           // No sigue
    FOLLOWER,       // Seguidor normal
    CLIENT_PENDING, // Solicitud de cliente pendiente
    CLIENT          // Cliente aceptado
}

object FollowersRepository {
    private const val TAG = "FollowersRepository"
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Obtiene el conteo de SEGUIDORES NORMALES de un usuario
     * (NO incluye clientes - solo is_client=false)
     */
    suspend fun getFollowersCount(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            // Obtener todas las relaciones y filtrar en memoria
            val allRelations = SupabaseClient.database
                .from("followers")
                .select {
                    filter {
                        eq("followed_id", userId)
                    }
                }
                .decodeList<FollowerRelation>()
            
            // Solo contar seguidores normales (NO clientes)
            val followers = allRelations.filter { !it.isClient }
            followers.size
        } catch (e: Exception) {
            Log.e(TAG, "✖ Error getFollowersCount: ${e.message}")
            0
        }
    }
    
    /**
     * Obtiene el conteo de clientes ACEPTADOS de un usuario
     * (is_client=true AND is_pending=false)
     */
    suspend fun getClientsCount(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val allRelations = SupabaseClient.database
                .from("followers")
                .select {
                    filter {
                        eq("followed_id", userId)
                    }
                }
                .decodeList<FollowerRelation>()
            
            // Solo clientes aceptados (is_client=true AND is_pending=false)
            val clients = allRelations.filter { it.isClient && !it.isPending }
            clients.size
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo conteo de clientes", e)
            0
        }
    }
    
    /**
     * Calcula la reputación basada en clientes
     */
    suspend fun getReputation(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val clientsCount = getClientsCount(userId)
            // Base 70% + 2% por cada cliente (máximo 100%)
            minOf(100, 70 + (clientsCount * 2))
        } catch (e: Exception) {
            Log.e(TAG, "Error calculando reputación", e)
            70 // Reputación base por defecto
        }
    }
    
    /**
     * Verifica el tipo de relación entre dos usuarios
     */
    suspend fun getFollowType(followerId: String, followedId: String): FollowType = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══ getFollowType INICIO ═══")
            Log.d(TAG, "Buscando: follower_id=$followerId -> followed_id=$followedId")
            
            // Primero obtener todos los registros para debug
            val allRelations = SupabaseClient.database
                .from("followers")
                .select {
                    filter {
                        eq("followed_id", followedId)
                    }
                }
                .decodeList<FollowerRelation>()
            
            Log.d(TAG, "Todos los seguidores de $followedId: ${allRelations.size}")
            allRelations.forEach { rel ->
                Log.d(TAG, "  - follower_id=${rel.followerId}, is_client=${rel.isClient}, is_pending=${rel.isPending}")
            }
            
            // Buscar la relación específica
            val relation = allRelations.find { it.followerId == followerId }
            
            val result = when {
                relation == null -> FollowType.NONE
                relation.isClient && !relation.isPending -> FollowType.CLIENT
                relation.isClient && relation.isPending -> FollowType.CLIENT_PENDING
                else -> FollowType.FOLLOWER
            }
            
            Log.d(TAG, "✓ Resultado: $result (relación encontrada=${relation != null})")
            if (relation != null) {
                Log.d(TAG, "  Detalles: is_client=${relation.isClient}, is_pending=${relation.isPending}")
            }
            Log.d(TAG, "═══ getFollowType FIN ═══")
            result
        } catch (e: Exception) {
            Log.e(TAG, "✖ Error getFollowType: ${e.message}")
            Log.e(TAG, "Stack: ", e)
            FollowType.NONE
        }
    }
    
    /**
     * Seguir a un usuario (como seguidor normal)
     */
    suspend fun follow(followedId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))
            
            if (currentUserId == followedId) {
                return@withContext Result.failure(Exception("No puedes seguirte a ti mismo"))
            }
            
            // Verificar si ya existe la relación
            val existingRelation = getFollowType(currentUserId, followedId)
            if (existingRelation != FollowType.NONE) {
                Log.d(TAG, "Ya sigues a este usuario: $existingRelation")
                return@withContext Result.success(Unit)
            }
            
            Log.d(TAG, "Siguiendo a: $followedId desde $currentUserId")
            
            // Usar insert en lugar de upsert para evitar problemas
            SupabaseClient.database
                .from("followers")
                .insert(buildJsonObject {
                    put("follower_id", currentUserId)
                    put("followed_id", followedId)
                    put("is_client", false)
                    put("is_pending", false)
                })
            
            Log.d(TAG, "✓ Ahora sigues a $followedId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al seguir: ${e.message}", e)
            // Si el error es de duplicado, considerarlo exitoso
            if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                Log.d(TAG, "Relación ya existe, considerando exitoso")
                return@withContext Result.success(Unit)
            }
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Solicitar ser cliente de un usuario (requiere aceptación del vendedor)
     */
    suspend fun requestClient(followedId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))
            
            Log.d(TAG, "Solicitando ser cliente de: $followedId")
            
            // Verificar si ya sigue
            val existingRelation = getFollowType(currentUserId, followedId)
            
            // Si ya es cliente pendiente o cliente, no hacer nada
            if (existingRelation == FollowType.CLIENT_PENDING) {
                Log.d(TAG, "Ya tienes una solicitud pendiente")
                return@withContext Result.success(Unit)
            }
            if (existingRelation == FollowType.CLIENT) {
                Log.d(TAG, "Ya eres cliente")
                return@withContext Result.success(Unit)
            }
            
            if (existingRelation == FollowType.NONE) {
                // Crear nueva relación como solicitud de cliente pendiente
                SupabaseClient.database
                    .from("followers")
                    .insert(buildJsonObject {
                        put("follower_id", currentUserId)
                        put("followed_id", followedId)
                        put("is_client", true)
                        put("is_pending", true)
                    })
            } else if (existingRelation == FollowType.FOLLOWER) {
                // Actualizar relación existente a solicitud de cliente
                SupabaseClient.database
                    .from("followers")
                    .update(buildJsonObject {
                        put("is_client", true)
                        put("is_pending", true)
                    }) {
                        filter {
                            eq("follower_id", currentUserId)
                            eq("followed_id", followedId)
                        }
                    }
            }
            
            Log.d(TAG, "✓ Solicitud enviada a $followedId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar ser cliente: ${e.message}", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Aceptar solicitud de cliente (para vendedores)
     */
    suspend fun acceptClientRequest(followerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Usuario no autenticado")
            
            Log.d(TAG, "═══ ACEPTANDO CLIENTE ═══")
            Log.d(TAG, "follower_id (quien solicita): $followerId")
            Log.d(TAG, "followed_id (yo/vendedor): $currentUserId")
            
            // Primero obtener el registro específico para conseguir su ID
            val allRelations = SupabaseClient.database
                .from("followers")
                .select {
                    filter {
                        eq("follower_id", followerId)
                        eq("followed_id", currentUserId)
                    }
                }
                .decodeList<FollowerRelation>()
            
            Log.d(TAG, "Relaciones encontradas: ${allRelations.size}")
            allRelations.forEach { rel ->
                Log.d(TAG, "  - id=${rel.id}, is_client=${rel.isClient}, is_pending=${rel.isPending}")
            }
            
            val relation = allRelations.firstOrNull()
            if (relation == null) {
                Log.e(TAG, "✖ No existe relación para aceptar")
                return@withContext Result.failure(Exception("No existe solicitud pendiente"))
            }
            
            if (!relation.isClient || !relation.isPending) {
                Log.e(TAG, "✖ La relación no es una solicitud pendiente de cliente")
                return@withContext Result.failure(Exception("No hay solicitud pendiente"))
            }
            
            Log.d(TAG, "Actualizando registro con ID: ${relation.id}")
            
            // Actualizar usando el ID específico del registro
            SupabaseClient.database
                .from("followers")
                .update(buildJsonObject {
                    put("is_pending", false)
                }) {
                    filter {
                        eq("id", relation.id)
                    }
                }
            
            // Verificar que se actualizó correctamente
            val verifyRelation = SupabaseClient.database
                .from("followers")
                .select {
                    filter {
                        eq("id", relation.id)
                    }
                }
                .decodeList<FollowerRelation>()
                .firstOrNull()
            
            if (verifyRelation?.isPending == false) {
                Log.d(TAG, "✓ VERIFICADO: is_pending ahora es FALSE")
            } else {
                Log.e(TAG, "✖ FALLO: is_pending sigue siendo ${verifyRelation?.isPending}")
                return@withContext Result.failure(Exception("Update no funcionó - verificar RLS"))
            }
            
            Log.d(TAG, "═══ FIN ACEPTAR CLIENTE ═══")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "✖ Error al aceptar cliente: ${e.message}", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Rechazar solicitud de cliente (para vendedores)
     */
    suspend fun rejectClientRequest(followerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Usuario no autenticado")
            
            Log.d(TAG, "Rechazando solicitud de cliente: $followerId")
            
            // Convertir de vuelta a seguidor normal
            SupabaseClient.database
                .from("followers")
                .update(buildJsonObject {
                    put("is_client", false)
                    put("is_pending", false)
                }) {
                    filter {
                        eq("follower_id", followerId)
                        eq("followed_id", currentUserId)
                    }
                }
            
            Log.d(TAG, "✓ Solicitud rechazada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al rechazar solicitud", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Dejar de seguir a un usuario
     */
    suspend fun unfollow(followedId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Usuario no autenticado")
            
            Log.d(TAG, "Dejando de seguir a: $followedId")
            
            SupabaseClient.database
                .from("followers")
                .delete {
                    filter {
                        eq("follower_id", currentUserId)
                        eq("followed_id", followedId)
                    }
                }
            
            Log.d(TAG, "✓ Ya no sigues a $followedId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al dejar de seguir", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Obtiene la lista de seguidores de un usuario
     */
    suspend fun getFollowers(userId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.database
                .from("followers")
                .select {
                    filter { eq("followed_id", userId) }
                }
                .decodeList<FollowerRelation>()
            
            result.map { it.followerId }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo lista de seguidores", e)
            emptyList()
        }
    }
    
    /**
     * Obtiene la lista de usuarios que sigue
     */
    suspend fun getFollowing(userId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.database
                .from("followers")
                .select {
                    filter { eq("follower_id", userId) }
                }
                .decodeList<FollowerRelation>()
            
            result.map { it.followedId }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo lista de seguidos", e)
            emptyList()
        }
    }
}
