package com.mercora.app.data.livekit

import android.content.Context
import android.util.Log
import com.mercora.app.data.remote.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.RoomListener
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.participant.VideoTrackPublishDefaults
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.LocalVideoTrackOptions
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.TrackPublication
import io.livekit.android.room.track.VideoCaptureParameter
import io.livekit.android.room.track.VideoEncoding
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class LiveKitState {
    object Disconnected : LiveKitState()
    object Connecting : LiveKitState()
    object Connected : LiveKitState()
    data class Error(val message: String) : LiveKitState()
}

sealed class TokenResult {
    data class Success(val token: String, val url: String, val identity: String) : TokenResult()
    data class Error(val message: String) : TokenResult()
}

class LiveKitManager(private val context: Context) {
    companion object {
        private const val TAG = "LiveKitManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var room: Room? = null

    private val _state = MutableStateFlow<LiveKitState>(LiveKitState.Disconnected)
    val state: StateFlow<LiveKitState> = _state.asStateFlow()

    private val _viewerCount = MutableStateFlow(0)
    val viewerCount: StateFlow<Int> = _viewerCount.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private val _currentRoomName = MutableStateFlow<String?>(null)
    val currentRoomName: StateFlow<String?> = _currentRoomName.asStateFlow()

    val isConnected: Boolean get() = _state.value is LiveKitState.Connected
    val roomRef: Room? get() = room

    suspend fun requestToken(roomName: String, role: String): TokenResult {
        return try {
            val session = SupabaseClient.auth.currentSessionOrNull()
            if (session == null) {
                Log.e(TAG, "No hay sesión activa")
                return TokenResult.Error("No hay sesión activa")
            }

            val bodyJson = buildJsonObject {
                put("roomName", roomName)
                put("role", role)
            }
            val response = SupabaseClient.client.functions.invoke(
                function = "livekit-token",
                body = bodyJson
            )
            val bodyText = response.body<String>()
            val result = Json.decodeFromString<JsonObject>(bodyText)

            val token = result["token"]?.toString()?.removeSurrounding("\"") ?: ""
            val url = result["url"]?.toString()?.removeSurrounding("\"") ?: ""
            val identity = result["identity"]?.toString()?.removeSurrounding("\"") ?: ""

            if (token.isBlank()) {
                Log.e(TAG, "Token vacío en respuesta: $result")
                return TokenResult.Error("Token inválido del servidor")
            }

            Log.d(TAG, "Token obtenido para room=$roomName role=$role identity=$identity")
            TokenResult.Success(token, url, identity)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo token: ${e.message}", e)
            TokenResult.Error("Error obteniendo token: ${e.message}")
        }
    }

    suspend fun connectAsBroadcaster(roomName: String) {
        Log.d(TAG, "connectAsBroadcaster: $roomName")
        _state.value = LiveKitState.Connecting
        _currentRoomName.value = roomName

        try {
            val result = requestToken(roomName, "broadcaster")
            if (result is TokenResult.Error) {
                _state.value = LiveKitState.Error(result.message)
                return
            }

            result as TokenResult.Success
            connectToRoom(result.url, result.token)
        } catch (e: Exception) {
            Log.e(TAG, "Error en connectAsBroadcaster: ${e.message}", e)
            _state.value = LiveKitState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun connectAsViewer(roomName: String) {
        Log.d(TAG, "connectAsViewer: $roomName")
        _state.value = LiveKitState.Connecting

        try {
            val result = requestToken(roomName, "viewer")
            if (result is TokenResult.Error) {
                _state.value = LiveKitState.Error(result.message)
                return
            }

            result as TokenResult.Success
            connectToRoom(result.url, result.token)
        } catch (e: Exception) {
            Log.e(TAG, "Error en connectAsViewer: ${e.message}", e)
            _state.value = LiveKitState.Error(e.message ?: "Error desconocido")
        }
    }

    private fun connectToRoom(url: String, token: String) {
        scope.launch {
            try {
                val roomOptions = RoomOptions(
                    adaptiveStream = true,
                    dynacast = true,
                    videoTrackCaptureDefaults = LocalVideoTrackOptions(
                        isScreencast = false,
                        deviceId = null,
                        position = null,
                        captureParams = VideoCaptureParameter(720, 1280, 24)
                    ),
                    videoTrackPublishDefaults = VideoTrackPublishDefaults(
                        videoEncoding = VideoEncoding(1_500_000, 24),
                        simulcast = false,
                        videoCodec = "H264"
                    )
                )
                val newRoom = LiveKit.create(context, roomOptions)
                room = newRoom

                launch {
                    newRoom.events.collect { event ->
                        when {
                            event is io.livekit.android.events.RoomEvent.Disconnected -> {
                                Log.d(TAG, "Desconectado de LiveKit")
                                _state.value = LiveKitState.Disconnected
                                _remoteVideoTrack.value = null
                            }
                            event is io.livekit.android.events.RoomEvent.ParticipantConnected -> {
                                Log.d(TAG, "Participante: ${event.participant.identity}")
                                _viewerCount.value = newRoom.remoteParticipants.size
                            }
                            event is io.livekit.android.events.RoomEvent.ParticipantDisconnected -> {
                                Log.d(TAG, "Participante salió: ${event.participant.identity}")
                                _viewerCount.value = newRoom.remoteParticipants.size
                            }
                            event is io.livekit.android.events.RoomEvent.TrackSubscribed -> {
                                Log.d(TAG, "Track suscrito: ${event.track.kind}")
                                if (event.track.kind == Track.Kind.VIDEO) {
                                    _remoteVideoTrack.value = event.track as? VideoTrack
                                }
                            }
                            event is io.livekit.android.events.RoomEvent.TrackUnsubscribed -> {
                                if (event.track.kind == Track.Kind.VIDEO) {
                                    _remoteVideoTrack.value = null
                                }
                            }
                            event is io.livekit.android.events.RoomEvent.FailedToConnect -> {
                                val msg = event.error.message ?: "Error de conexión"
                                Log.e(TAG, "Error conectando: $msg")
                                _state.value = LiveKitState.Error(msg)
                            }
                            event is io.livekit.android.events.RoomEvent.Reconnecting -> {
                                Log.d(TAG, "Reconectando...")
                            }
                            event is io.livekit.android.events.RoomEvent.Reconnected -> {
                                Log.d(TAG, "Reconectado")
                                _state.value = LiveKitState.Connected
                            }
                            event is io.livekit.android.events.RoomEvent.TrackMuted -> {
                                Log.d(TAG, "Track muteado")
                            }
                            else -> {}
                        }
                    }
                }

                newRoom.connect(url, token)
                Log.d(TAG, "Conectado a LiveKit en $url")
                _state.value = LiveKitState.Connected
            } catch (e: Exception) {
                Log.e(TAG, "Error conectando a LiveKit: ${e.message}", e)
                _state.value = LiveKitState.Error(e.message ?: "Error al conectar")
            }
        }
    }

    fun enableCamera(frontCamera: Boolean = true) {
        scope.launch {
            try {
                room?.localParticipant?.setCameraEnabled(true)
                Log.d(TAG, "Cámara habilitada con H264")
            } catch (e: Exception) {
                Log.e(TAG, "Error habilitando cámara: ${e.message}", e)
            }
        }
    }

    fun enableMicrophone() {
        scope.launch {
            try {
                room?.localParticipant?.setMicrophoneEnabled(true)
                Log.d(TAG, "Micrófono habilitado")
            } catch (e: Exception) {
                Log.e(TAG, "Error habilitando micrófono: ${e.message}")
            }
        }
    }

    fun disableCamera() {
        scope.launch {
            try { room?.localParticipant?.setCameraEnabled(false) }
            catch (e: Exception) { Log.e(TAG, "Error deshabilitando cámara: ${e.message}") }
        }
    }

    fun disableMicrophone() {
        scope.launch {
            try { room?.localParticipant?.setMicrophoneEnabled(false) }
            catch (e: Exception) { Log.e(TAG, "Error deshabilitando micrófono: ${e.message}") }
        }
    }

    fun switchCamera() {
        val track = room?.localParticipant?.getTrackPublication(Track.Source.CAMERA)?.track
        if (track is LocalVideoTrack) {
            track.switchCamera()
            Log.d(TAG, "Cámara cambiada")
        }
    }

    fun getLocalCameraTrack(): VideoTrack? {
        return room?.localParticipant?.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
    }

    fun disconnect() {
        Log.d(TAG, "Desconectando...")
        room?.disconnect()
        room = null
        _state.value = LiveKitState.Disconnected
        _viewerCount.value = 0
        _currentRoomName.value = null
        _remoteVideoTrack.value = null
    }
}
