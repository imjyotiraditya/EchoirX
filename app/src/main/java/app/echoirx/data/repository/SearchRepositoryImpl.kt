package app.echoirx.data.repository

import app.echoirx.data.remote.api.ApiService
import app.echoirx.data.remote.mapper.PlaybackMapper.toDomain
import app.echoirx.data.remote.mapper.SearchItemMapper.toDomain
import app.echoirx.domain.model.PlaybackResponse
import app.echoirx.domain.model.SearchItem
import app.echoirx.domain.repository.SearchRepository
import app.echoirx.presentation.screens.search.SearchFilter
import app.echoirx.presentation.screens.search.SearchType
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SearchRepository {
    override suspend fun search(query: String, type: SearchType): List<SearchItem> =
        apiService.search(query, type.name.lowercase())
            .map { it.toDomain() }

    override suspend fun getAlbumTracks(albumId: Long): List<SearchItem> =
        apiService.getAlbumTracks(albumId)
            .map { it.toDomain() }

    override suspend fun filterSearchResults(
        results: List<SearchItem>,
        filter: SearchFilter
    ): List<SearchItem> {
        return results.filter { result ->
            val formatMatch = filter.qualities.isEmpty() || filter.qualities.any { quality ->
                result.formats?.let { formats -> quality.format in formats } ?: false
            }
            val explicitMatch = filter.contentFilters.isEmpty() || filter.contentFilters.any {
                it.explicit == result.explicit
            }
            formatMatch && explicitMatch
        }
    }

    override suspend fun getTrackPreview(trackId: Long): PlaybackResponse =
        apiService.getTrackPreview(trackId).toDomain()
}