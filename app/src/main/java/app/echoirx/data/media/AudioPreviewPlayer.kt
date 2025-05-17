package app.echoirx.data.media

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPreviewPlayer @Inject constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingUrl: String? = null
    private var progressUpdateJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    suspend fun play(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (url == currentlyPlayingUrl) {
                return@withContext togglePlayback()
            }

            releaseMediaPlayer()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracking()
                }
                setOnCompletionListener { _ ->
                    _isPlaying.value = false
                    _progress.value = 0f
                    stopProgressTracking()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPreviewPlayer", "Error: $what, extra: $extra")
                    _isPlaying.value = false
                    _progress.value = 0f
                    stopProgressTracking()
                    true
                }
                prepareAsync()
            }

            currentlyPlayingUrl = url
            true
        } catch (e: Exception) {
            Log.e("AudioPreviewPlayer", "Error playing audio", e)
            false
        }
    }

    fun stop() {
        releaseMediaPlayer()
        _isPlaying.value = false
        _progress.value = 0f
        currentlyPlayingUrl = null
        stopProgressTracking()
    }

    private fun togglePlayback(): Boolean {
        return try {
            if (_isPlaying.value) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                stopProgressTracking()
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracking()
            }
            true
        } catch (e: Exception) {
            Log.e("AudioPreviewPlayer", "Error toggling playback", e)
            false
        }
    }

    private fun releaseMediaPlayer() {
        stopProgressTracking()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressUpdateJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && mediaPlayer != null && _isPlaying.value) {
                try {
                    mediaPlayer?.let { player ->
                        val duration = player.duration.toFloat().coerceAtLeast(1f)
                        val currentPosition = player.currentPosition.toFloat()
                        _progress.value = currentPosition / duration
                    }
                } catch (e: Exception) {
                    Log.e("AudioPreviewPlayer", "Error updating progress", e)
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracking() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
}