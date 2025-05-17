package app.echoirx.presentation.screens.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.echoirx.data.media.AudioPreviewPlayer
import app.echoirx.domain.model.AlbumDownloadContext
import app.echoirx.domain.model.DownloadRequest
import app.echoirx.domain.model.QualityConfig
import app.echoirx.domain.model.SearchResult
import app.echoirx.domain.repository.DownloadRepository
import app.echoirx.domain.usecase.AlbumTracksUseCase
import app.echoirx.domain.usecase.ProcessDownloadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getAlbumTracksUseCase: AlbumTracksUseCase,
    private val processDownloadUseCase: ProcessDownloadUseCase,
    private val downloadRepository: DownloadRepository,
    private val audioPreviewPlayer: AudioPreviewPlayer
) : ViewModel() {
    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    val isPreviewPlaying = audioPreviewPlayer.isPlaying
    val previewProgress = audioPreviewPlayer.progress

    fun initializeWithItem(item: SearchResult) {
        _state.update { it.copy(item = item) }
        loadAlbumTracks(item.id)
    }

    fun downloadTrack(track: SearchResult, config: QualityConfig) {
        viewModelScope.launch {
            try {
                processDownloadUseCase(
                    DownloadRequest.Track(
                        track = track,
                        config = config
                    )
                )
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun downloadAlbum(config: QualityConfig) {
        viewModelScope.launch {
            try {
                val album = state.value.item ?: return@launch
                val tracks = state.value.tracks
                val isExplicit = tracks.any { it.explicit }

                val albumDirectory =
                    downloadRepository.createAlbumDirectory(album.title, isExplicit)

                val downloadContext = AlbumDownloadContext(
                    id = album.id,
                    title = album.title,
                    directory = albumDirectory,
                    isExplicit = isExplicit
                )

                processDownloadUseCase(
                    DownloadRequest.Album(
                        album = album,
                        tracks = tracks,
                        config = config,
                        downloadContext = downloadContext
                    )
                )
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun loadAlbumTracks(albumId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val tracks = getAlbumTracksUseCase(albumId)
                _state.update { it.copy(tracks = tracks, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun playTrackPreview(trackId: Long) {
        viewModelScope.launch {
            try {
                val preview = getAlbumTracksUseCase.getTrackPreview(trackId)
                if (preview.urls.isNotEmpty()) {
                    audioPreviewPlayer.play(preview.urls[0])
                }
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error playing preview", e)
            }
        }
    }

    fun stopTrackPreview() {
        audioPreviewPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioPreviewPlayer.stop()
    }
}