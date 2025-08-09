package app.echoirx.domain.repository

import app.echoirx.domain.model.PlaybackResponse
import app.echoirx.domain.model.SearchItem
import app.echoirx.presentation.screens.search.SearchFilter
import app.echoirx.presentation.screens.search.SearchType

interface SearchRepository {
    suspend fun search(query: String, type: SearchType): List<SearchItem>
    suspend fun getAlbumTracks(albumId: Long): List<SearchItem>
    suspend fun filterSearchResults(
        results: List<SearchItem>,
        filter: SearchFilter
    ): List<SearchItem>

    suspend fun getTrackPreview(trackId: Long): PlaybackResponse
}