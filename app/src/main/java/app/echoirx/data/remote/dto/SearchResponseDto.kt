package app.echoirx.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val items: List<SearchItemDto>,
    val limit: Int,
    val query: String
)