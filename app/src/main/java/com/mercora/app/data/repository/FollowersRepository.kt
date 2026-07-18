package com.mercora.app.data.repository

import android.util.Log
import com.mercora.app.data.model.Usuario
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

@Serializable
data class FollowerWithInfo(
    @SerialName("user_id") val userId: String = "",
    val username: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("nombre_tienda") val nombreTienda: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_client") val isClient: Boolean = false,
    @SerialName("is_pending") val isPending: Boolean = false,
    @SerialName("followed_at") val followedAt: String = "",
    @SerialName("is_following_back") val isFollowingBack: Boolean = false
)

enum class FollowType {
    NONE,              // No sigue
    FOLLOWER,          // Seguidor normal
    FOLLOWER_PENDING,  // Solicitud de seguimiento pendiente (perfil privado)
    CLIENT_PENDING,    // Solicitud de cliente pendiente
    CLIENT             // Cliente aceptado
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
            
            // Solo contar seguidores normales ACEPTADOS (ni clientes ni pendientes)
            val followers = allRelations.filter { !it.isClient && !it.isPending }
            followers.size
        } catch (e: Exception) {
            Log.e(TAG, "âœ– Error getFollowersCount: ${e.message}")
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
     * Calcula la reputaciÃ³n basada en clientes
     */
    suspend fun getReputation(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val clientsCount = getClientsCount(userId)
            // Base 70% + 2% por cada cliente (mÃ¡ximo 100%)
            minOf(100, 70 + (clientsCount * 2))
        } catch (e: Exception) {
            Log.e(TAG, "Error calculando reputaciÃ³n", e)
            70 // ReputaciÃ³n base por defecto
        }
    }
    
    /**
     * Verifica el tipo de relaciÃ³n entre dos usuarios
     */
    suspend fun getFollowType(followerId: String, followedId: String): FollowType = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "â•â•â• getFollowType INICIO â•â•â•")
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
            
            // Buscar la relaciÃ³n especÃ­fica
            val relation = allRelations.find { it.followerId == followerId }
            
            val result = when {
                relation == null -> FollowType.NONE
                relation.isClient && !relation.isPending -> FollowType.CLIENT
                relation.isClient && relation.isPending -> FollowType.CLIENT_PENDING
                !relation.isClient && relation.isPending -> FollowType.FOLLOWER_PENDING
                else -> FollowType.FOLLOWER
            }
            
            Log.d(TAG, "âœ“ Resultado: $result (relaciÃ³n encontrada=${relation != null})")
            if (relation != null) {
                Log.d(TAG, "  Detalles: is_client=${relation.isClient}, is_pending=${relation.isPending}")
            }
            Log.d(TAG, "â•â•â• getFollowType FIN â•â•â•")
            result
        } catch (e: Exception) {
            Log.e(TAG, "âœ– Error getFollowType: ${e.message}")
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
            
            // Verificar si ya existe la relaciÃ³n
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
            
            Log.d(TAG, "âœ“ Ahora sigues a $followedId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al seguir: ${e.message}", e)
            // Si el error es de duplicado, considerarlo exitoso
            if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                Log.d(TAG, "RelaciÃ³n ya existe, considerando exitoso")
                return@withContext Result.success(Unit)
            }
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Solicitar seguir a un usuario con perfil privado (requiere aceptaciÃ³n)
     */
    suspend fun requestFollow(followedId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

            if (currentUserId == followedId) {
                return@withContext Result.failure(Exception("No puedes seguirte a ti mismo"))
            }

            Log.d(TAG, "Solicitando seguir a perfil privado: $followedId")

            // Verificar si ya existe la relaciÃ³n
            val existingRelation = getFollowType(currentUserId, followedId)
            if (existingRelation != FollowType.NONE) {
                Log.d(TAG, "Ya tienes una relaciÃ³n con este usuario: $existingRelation")
                return@withContext Result.success(Unit)
            }

            // Crear solicitud pendiente (is_client=false, is_pending=true)
            SupabaseClient.database
                .from("followers")
                .insert(buildJsonObject {
                    put("follower_id", currentUserId)
                    put("followed_id", followedId)
                    put("is_client", false)
                    put("is_pending", true)
                })

            Log.d(TAG, "âœ“ Solicitud de seguimiento enviada a $followedId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar seguir: ${e.message}", e)
            if (e.message?.contains("duplicate") == true || e.message?.contains("unique") == true) {
                return@withContext Result.success(Unit)
            }
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Aceptar solicitud de seguimiento (para dueÃ±os de perfiles privados)
     * Cambia is_pending a false, convirtiendo al solicitante en seguidor
     */
    suspend fun acceptFollowRequest(followerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Usuario no autenticado")

            Log.d(TAG, "â•â•â• ACEPTANDO SOLICITUD DE SEGUIMIENTO â•â•â•")
            Log.d(TAG, "follower_id (quien solicita): $followerId")
            Log.d(TAG, "followed_id (yo): $currentUserId")

            // Buscar la relaciÃ³n especÃ­fica
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
            val relation = allRelations.firstOrNull()
            if (relation == null) {
                Log.e(TAG, "âœ– No existe relaciÃ³n para aceptar")
                return@withContext Result.failure(Exception("No existe solicitud pendiente"))
            }

            if (relation.isClient || !relation.isPending) {
                Log.e(TAG, "âœ– La relaciÃ³n no es una solicitud pendiente de seguimiento")
                return@withContext Result.failure(Exception("No hay solicitud pendiente"))
            }

            // Actualizar: is_pending = false
            SupabaseClient.database
                .from("followers")
                .update(buildJsonObject {
                    put("is_pending", false)
                }) {
                    filter {
                        eq("id", relation.id)
                    }
                }

            // Verificar
            val verifyRelation = SupabaseClient.database
                .from("followers")
                .select {
                    filter { eq("id", relation.id) }
                }
                .decodeList<FollowerRelation>()
                .firstOrNull()

            if (verifyRelation?.isPending == false) {
                Log.d(TAG, "âœ“ VERIFICADO: is_pending ahora es FALSE")
            } else {
                Log.e(TAG, "âœ– FALLO: is_pending sigue siendo ${verifyRelation?.isPending}")
                return@withContext Result.failure(Exception("Update no funcionÃ³ - verificar RLS"))
            }

            Log.d(TAG, "â•â•â• FIN ACEPTAR SEGUIMIENTO â•â•â•")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "âœ– Error al aceptar solicitud de seguimiento: ${e.message}", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Rechazar solicitud de seguimiento (para dueÃ±os de perfiles privados)
     * Elimina la relaciÃ³n pendiente
     */
    suspend fun rejectFollowRequest(followerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isLoading.value = true
            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id
                ?: throw Exception("Usuario no autenticado")

            Log.d(TAG, "Rechazando solicitud de seguimiento: $followerId")

            // Eliminar la relaciÃ³n pendiente
            SupabaseClient.database
                .from("followers")
                .delete {
                    filter {
                        eq("follower_id", followerId)
                        eq("followed_id", currentUserId)
                        eq("is_pending", true)
                    }
                }

            Log.d(TAG, "âœ“ Solicitud de seguimiento rechazada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al rechazar solicitud de seguimiento", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Solicitar ser cliente de un usuario (requiere aceptaciÃ³n del vendedor)
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
                // Crear nueva relaciÃ³n como solicitud de cliente pendiente
                SupabaseClient.database
                    .from("followers")
                    .insert(buildJsonObject {
                        put("follower_id", currentUserId)
                        put("followed_id", followedId)
                        put("is_client", true)
                        put("is_pending", true)
                    })
            } else if (existingRelation == FollowType.FOLLOWER) {
                // Actualizar relaciÃ³n existente a solicitud de cliente
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
            
            Log.d(TAG, "âœ“ Solicitud enviada a $followedId")
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
            
            Log.d(TAG, "â•â•â• ACEPTANDO CLIENTE â•â•â•")
            Log.d(TAG, "follower_id (quien solicita): $followerId")
            Log.d(TAG, "followed_id (yo/vendedor): $currentUserId")
            
            // Primero obtener el registro especÃ­fico para conseguir su ID
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
                Log.e(TAG, "âœ– No existe relaciÃ³n para aceptar")
                return@withContext Result.failure(Exception("No existe solicitud pendiente"))
            }
            
            if (!relation.isClient || !relation.isPending) {
                Log.e(TAG, "âœ– La relaciÃ³n no es una solicitud pendiente de cliente")
                return@withContext Result.failure(Exception("No hay solicitud pendiente"))
            }
            
            Log.d(TAG, "Actualizando registro con ID: ${relation.id}")
            
            // Actualizar usando el ID especÃ­fico del registro
            SupabaseClient.database
                .from("followers")
                .update(buildJsonObject {
                    put("is_pending", false)
                }) {
                    filter {
                        eq("id", relation.id)
                    }
                }
            
            // Verificar que se actualizÃ³ correctamente
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
                Log.d(TAG, "âœ“ VERIFICADO: is_pending ahora es FALSE")
            } else {
                Log.e(TAG, "âœ– FALLO: is_pending sigue siendo ${verifyRelation?.isPending}")
                return@withContext Result.failure(Exception("Update no funcionÃ³ - verificar RLS"))
            }
            
            Log.d(TAG, "â•â•â• FIN ACEPTAR CLIENTE â•â•â•")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "âœ– Error al aceptar cliente: ${e.message}", e)
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
            
            Log.d(TAG, "âœ“ Solicitud rechazada")
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
            
            Log.d(TAG, "âœ“ Ya no sigues a $followedId")
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

    /**
     * Obtiene la lista de SEGUIDORES con info de usuario (username, avatar, etc.)
     */
    suspend fun getFollowersWithInfo(userId: String): List<FollowerWithInfo> = withContext(Dispatchers.IO) {
        try {
            val relations = SupabaseClient.database
                .from("followers")
                .select {
                    filter { eq("followed_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FollowerRelation>()

            val accepted = relations.filter { !it.isClient && !it.isPending }
            if (accepted.isEmpty()) return@withContext emptyList()

            val followerIds = accepted.map { it.followerId }
            val users = SupabaseClient.database
                .from("usuarios")
                .select {
                    filter { isIn("user_id", followerIds) }
                }
                .decodeList<Usuario>()
                .associateBy { it.userId }

            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: ""
            val currentUserFollowing = if (currentUserId.isNotEmpty()) {
                SupabaseClient.database
                    .from("followers")
                    .select {
                        filter {
                            eq("follower_id", currentUserId)
                            isIn("followed_id", followerIds)
                            eq("is_pending", false)
                        }
                    }
                    .decodeList<FollowerRelation>()
                    .map { it.followedId }
                    .toSet()
            } else emptySet()

            accepted.map { rel ->
                val user = users[rel.followerId]
                FollowerWithInfo(
                    userId = rel.followerId,
                    username = user?.username ?: "unknown",
                    avatarUrl = user?.avatarUrl,
                    nombreTienda = user?.nombreTienda,
                    isVerified = user?.isVerified ?: false,
                    isClient = false,
                    isPending = false,
                    followedAt = rel.createdAt,
                    isFollowingBack = rel.followerId in currentUserFollowing
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo seguidores con info", e)
            emptyList()
        }
    }

    /**
     * Obtiene la lista de CLIENTES con info de usuario
     */
    suspend fun getClientsWithInfo(userId: String): List<FollowerWithInfo> = withContext(Dispatchers.IO) {
        try {
            val relations = SupabaseClient.database
                .from("followers")
                .select {
                    filter { eq("followed_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<FollowerRelation>()

            val accepted = relations.filter { it.isClient && !it.isPending }
            if (accepted.isEmpty()) return@withContext emptyList()

            val followerIds = accepted.map { it.followerId }
            val users = SupabaseClient.database
                .from("usuarios")
                .select {
                    filter { isIn("user_id", followerIds) }
                }
                .decodeList<Usuario>()
                .associateBy { it.userId }

            val currentUserId = SupabaseClient.auth.currentUserOrNull()?.id ?: ""
            val currentUserFollowing = if (currentUserId.isNotEmpty()) {
                SupabaseClient.database
                    .from("followers")
                    .select {
                        filter {
                            eq("follower_id", currentUserId)
                            isIn("followed_id", followerIds)
                            eq("is_pending", false)
                        }
                    }
                    .decodeList<FollowerRelation>()
                    .map { it.followedId }
                    .toSet()
            } else emptySet()

            accepted.map { rel ->
                val user = users[rel.followerId]
                FollowerWithInfo(
                    userId = rel.followerId,
                    username = user?.username ?: "unknown",
                    avatarUrl = user?.avatarUrl,
                    nombreTienda = user?.nombreTienda,
                    isVerified = user?.isVerified ?: false,
                    isClient = true,
                    isPending = false,
                    followedAt = rel.createdAt,
                    isFollowingBack = rel.followerId in currentUserFollowing
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo clientes con info", e)
            emptyList()
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // REALTIME: Seguimiento de cambios en followers
    // SuscripciÃ³n directa a la tabla followers para actualizar la UI
    // cuando una solicitud de seguimiento es aceptada/rechazada
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private val _followChangeTrigger = MutableStateFlow(Pair("", ""))
    val followChangeTrigger: StateFlow<Pair<String, String>> = _followChangeTrigger.asStateFlow()

    private val followScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var followersRealtimeChannel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var isFollowSubscribed = false

    /**
     * Suscribirse a cambios en la tabla followers para el usuario actual.
     * Escucha eventos UPDATE donde el usuario es el follower_id.
     * Cuando is_pending cambia (ej: de true a false), emite el cambio.
     */
    suspend fun subscribeToFollowChanges(currentUserId: String) {
        if (isFollowSubscribed) return
        try {
            followersRealtimeChannel = SupabaseClient.client.channel("followers:$currentUserId")

            followersRealtimeChannel?.postgresChangeFlow<PostgresAction.Update>(
                schema = "public"
            ) {
                table = "followers"
                filter = "follower_id=eq.$currentUserId"
            }?.onEach { action ->
                val followedId = action.record["followed_id"]?.toString() ?: return@onEach
                Log.d(TAG, "ðŸ”” Follow change detected: follower=$currentUserId followed=$followedId")
                _followChangeTrigger.value = Pair(currentUserId, followedId)
            }?.launchIn(followScope)

            followersRealtimeChannel?.subscribe()
            isFollowSubscribed = true
            Log.d(TAG, "âœ… Suscrito a cambios en followers en tiempo real")
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to followers realtime", e)
        }
    }

    /**
     * Desuscribirse de cambios en followers
     */
    suspend fun unsubscribeFromFollowChanges() {
        try {
            followersRealtimeChannel?.unsubscribe()
            isFollowSubscribed = false
            Log.d(TAG, "Desuscrito de followers realtime")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from followers", e)
        }
    }
}
