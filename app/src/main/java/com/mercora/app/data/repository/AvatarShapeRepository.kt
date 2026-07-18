package com.mercora.app.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Shape
import com.mercora.app.data.model.AvatarShapeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object AvatarShapeRepository {
    private const val PREFS_NAME = "avatar_shapes"
    private const val KEY_SELECTED_SHAPE = "selected_avatar_shape"
    private const val KEY_OWNED_PACKS = "owned_shape_packs"

    private lateinit var prefs: android.content.SharedPreferences
    private val json = Json { ignoreUnknownKeys = true }

    // Flow reactivo: la UI se recompone al instante cuando cambia la forma
    private val _selectedShapeFlow = MutableStateFlow(AvatarShapeType.CIRCLE)
    val selectedShapeFlow: StateFlow<AvatarShapeType> = _selectedShapeFlow.asStateFlow()

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        _selectedShapeFlow.value = getSelectedShape()
    }

    fun getSelectedShape(): AvatarShapeType {
        val dbValue = prefs.getString(KEY_SELECTED_SHAPE, null) ?: return AvatarShapeType.CIRCLE
        return AvatarShapeType.fromDbValue(dbValue)
    }

    fun getSelectedShapeComposable(): Shape = getSelectedShape().toShape()

    fun setSelectedShape(shape: AvatarShapeType) {
        prefs.edit().putString(KEY_SELECTED_SHAPE, shape.dbValue).apply()
        _selectedShapeFlow.value = shape
    }

    fun getOwnedPacks(): Set<String> {
        val raw = prefs.getString(KEY_OWNED_PACKS, "[]") ?: "[]"
        return try {
            val arr = json.parseToJsonElement(raw).jsonArray
            arr.map { it.jsonPrimitive.content }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun ownsPack(packId: String): Boolean = packId in getOwnedPacks()

    fun addOwnedPack(packId: String) {
        val current = getOwnedPacks().toMutableSet()
        current.add(packId)
        val arr = buildJsonArray {
            current.forEach { add(JsonPrimitive(it)) }
        }
        prefs.edit().putString(KEY_OWNED_PACKS, arr.toString()).apply()
    }

    fun ownsShape(shape: AvatarShapeType): Boolean {
        if (shape == AvatarShapeType.CIRCLE) return true
        val allPacks = getAllOwnedShapes()
        return shape in allPacks
    }

    fun getAllOwnedShapes(): Set<AvatarShapeType> {
        val packs = getOwnedPacks()
        val shapes = mutableSetOf(AvatarShapeType.CIRCLE)
        for (pack in com.vinzay.app.data.model.AVATAR_SHAPE_PACKS) {
            if (pack.id in packs) {
                shapes.addAll(pack.shapes)
            }
        }
        return shapes
    }

    fun hasUnseenShapes(): Boolean {
        val ownedCount = getAllOwnedShapes().size
        if (ownedCount <= 1) return false
        return !prefs.getBoolean("has_seen_shapes", false)
    }

    fun markShapesAsSeen() {
        prefs.edit().putBoolean("has_seen_shapes", true).apply()
    }

    fun resetUnseenFlag() {
        prefs.edit().putBoolean("has_seen_shapes", false).apply()
    }

    suspend fun syncSelectedShapeToServer() = withContext(Dispatchers.IO) {
        try {
            val shape = getSelectedShape()
            val userId = com.vinzay.app.data.remote.SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
            com.vinzay.app.data.remote.SupabaseClient.database
                .from("usuarios")
                .update(mapOf("avatar_shape" to shape.dbValue)) {
                    filter { eq("user_id", userId) }
                }
        } catch (e: Exception) {
            android.util.Log.e("AvatarShapeRepo", "Error syncing shape: ${e.message}")
        }
    }
}
