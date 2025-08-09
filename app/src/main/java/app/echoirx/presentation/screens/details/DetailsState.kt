package app.echoirx.presentation.screens.details

import app.echoirx.domain.model.Download
import app.echoirx.domain.model.SearchItem

data class DetailsState(
    val item: SearchItem? = null,
    val tracks: List<SearchItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val downloads: Map<Long, Map<String, Download>> = emptyMap()
)