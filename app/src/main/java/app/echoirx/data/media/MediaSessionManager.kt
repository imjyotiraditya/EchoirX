package app.echoirx.data.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import app.echoirx.domain.model.CurrentMediaInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSessionManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val _currentMediaInfo = MutableStateFlow<CurrentMediaInfo?>(null)
    val currentMediaInfo: StateFlow<CurrentMediaInfo?> = _currentMediaInfo.asStateFlow()

    private val _hasMediaPermission = MutableStateFlow(false)
    val hasMediaPermission: StateFlow<Boolean> = _hasMediaPermission.asStateFlow()

    private var activeControllers: List<MediaController> = emptyList()

    fun startMonitoring() {
        try {
            // Get active media sessions
            val notificationListener = ComponentName(context, MediaNotificationListener::class.java)
            activeControllers =
                mediaSessionManager.getActiveSessions(notificationListener) ?: emptyList()

            _hasMediaPermission.value = true
            updateCurrentMedia()

            // Register callback for session changes
            mediaSessionManager.addOnActiveSessionsChangedListener(
                { controllers ->
                    activeControllers = controllers ?: emptyList()
                    updateCurrentMedia()
                },
                notificationListener
            )
        } catch (e: SecurityException) {
            _hasMediaPermission.value = false
            _currentMediaInfo.value = null
        }
    }

    private fun updateCurrentMedia() {
        // Find the currently playing session
        val playingController = activeControllers.find { controller ->
            controller.playbackState?.state == PlaybackState.STATE_PLAYING
        }

        if (playingController != null) {
            val metadata = playingController.metadata
            val playbackState = playingController.playbackState

            if (metadata != null && playbackState != null) {
                val mediaInfo = CurrentMediaInfo(
                    title = metadata.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
                        ?: "",
                    artist = metadata.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                        ?: "",
                    album = metadata.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM)
                        ?: "",
                    packageName = playingController.packageName,
                    isPlaying = playbackState.state == PlaybackState.STATE_PLAYING
                )

                _currentMediaInfo.value = mediaInfo
            } else {
                _currentMediaInfo.value = null
            }
        } else {
            _currentMediaInfo.value = null
        }
    }

    fun stopMonitoring() {
        activeControllers.forEach { controller ->
            controller.unregisterCallback(mediaCallback)
        }
        activeControllers = emptyList()
        _currentMediaInfo.value = null
    }

    private val mediaCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
            updateCurrentMedia()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateCurrentMedia()
        }
    }

    fun registerCallbackForActiveControllers() {
        activeControllers.forEach { controller ->
            controller.registerCallback(mediaCallback)
        }
    }
}