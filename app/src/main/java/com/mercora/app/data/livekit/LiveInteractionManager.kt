package com.mercora.app.data.livekit

import android.util.Log
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class LiveComment(
    val id: String,
    val username: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveReaction(
    val type: String,
    val userId: String,
    val username: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LiveInteractionManager(private val roomName: String) {
    companion object {
        private const val TAG = "LiveInteraction"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var channel: io.github.jan.supabase.realtime.RealtimeChannel? = null
    private var collectJob: Job? = null
    private var active = true

    private val _comments = MutableStateFlow<List<LiveComment>>(emptyList())
    val comments: StateFlow<List<LiveComment>> = _comments.asStateFlow()

    private val _reactions = MutableSharedFlow<LiveReaction>(extraBufferCapacity = 32)
    val reactions: SharedFlow<LiveReaction> = _reactions.asSharedFlow()

    private val _likeCount = MutableStateFlow(0)
    val likeCount: StateFlow<Int> = _likeCount.asStateFlow()

    fun connect() {
        scope.launch {
            try {
                val liveChannel = SupabaseClient.channel("live-stream-$roomName") {
                    broadcast {
                        receiveOwnBroadcasts = true
                    }
                }
                liveChannel.subscribe()
                channel = liveChannel

                collectJob = launch {
                    liveChannel.broadcastFlow<JsonObject>("like").collect { payload ->
                        if (!active) return@collect
                        val userId = payload["userId"]?.toString()?.removeSurrounding("\"") ?: ""
                        val username = payload["username"]?.toString()?.removeSurrounding("\"") ?: "Anónimo"
                        _reactions.emit(LiveReaction("â¤ï¸", userId, username))
                        _likeCount.value++
                    }
                }
                launch {
                    liveChannel.broadcastFlow<JsonObject>("comment").collect { payload ->
                        if (!active) return@collect
                        val userId = payload["userId"]?.toString()?.removeSurrounding("\"") ?: ""
                        val username = payload["username"]?.toString()?.removeSurrounding("\"") ?: "Anónimo"
                        val text = payload["text"]?.toString()?.removeSurrounding("\"") ?: ""
                        val comment = LiveComment(
                            id = "c_${System.currentTimeMillis()}_${userId.take(4)}",
                            username = username,
                            text = text
                        )
                        _comments.value = _comments.value + comment
                    }
                }
                launch {
                    liveChannel.broadcastFlow<JsonObject>("reaction").collect { payload ->
                        if (!active) return@collect
                        val userId = payload["userId"]?.toString()?.removeSurrounding("\"") ?: ""
                        val username = payload["username"]?.toString()?.removeSurrounding("\"") ?: "Anónimo"
                        val emoji = payload["emoji"]?.toString()?.removeSurrounding("\"") ?: "â¤ï¸"
                        _reactions.emit(LiveReaction(emoji, userId, username))
                    }
                }

                Log.d(TAG, "Conectado a canal live-stream-$roomName")
            } catch (e: Exception) {
                Log.e(TAG, "Error conectando interacción: ${e.message}")
            }
        }
    }

    fun sendLike(userId: String, username: String) {
        scope.launch {
            try {
                channel?.broadcast("like", buildJsonObject {
                    put("userId", userId)
                    put("username", username)
                })
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando like: ${e.message}")
            }
        }
    }

    fun sendComment(userId: String, username: String, text: String) {
        scope.launch {
            try {
                channel?.broadcast("comment", buildJsonObject {
                    put("userId", userId)
                    put("username", username)
                    put("text", text)
                })
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando comentario: ${e.message}")
            }
        }
    }

    fun disconnect() {
        active = false
        collectJob?.cancel()
        scope.launch {
            try { channel?.unsubscribe() } catch (_: Exception) {}
            channel = null
        }
        scope.cancel()
    }

    fun clearComments() {
        _comments.value = emptyList()
    }
}
