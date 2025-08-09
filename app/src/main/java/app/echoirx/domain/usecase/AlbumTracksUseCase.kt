package app.echoirx.domain.usecase

import app.echoirx.domain.model.PlaybackResponse
import app.echoirx.domain.model.SearchItem
import app.echoirx.domain.repository.SearchRepository
import javax.inject.Inject

class AlbumTracksUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(albumId: Long): List<SearchItem> =
        repository.getAlbumTracks(albumId)

    suspend fun getTrackPreview(trackId: Long): PlaybackResponse =
        repository.getTrackPreview(trackId)
}