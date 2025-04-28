package app.echoirx.presentation.screens.search

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.SurroundSound
import androidx.compose.ui.graphics.vector.ImageVector
import app.echoirx.R
import app.echoirx.domain.model.SearchHistoryItem
import app.echoirx.domain.model.SearchResult

data class SearchState(
    val query: String = "",
    val searchType: SearchType = SearchType.TRACKS,
    val searchFilter: SearchFilter = SearchFilter(),
    val results: List<SearchResult> = emptyList(),
    val filteredResults: List<SearchResult> = emptyList(),
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    val suggestedQueries: List<SearchHistoryItem> = emptyList(),
    val status: SearchStatus = SearchStatus.Empty,
    val error: String? = null,
    val showServerRecommendation: Boolean = false,
    val isShowingHistory: Boolean = false
)

enum class SearchType(
    @param:StringRes val title: Int
) {
    TRACKS(R.string.label_tracks),
    ALBUMS(R.string.label_albums)
}

data class SearchFilter(
    val qualities: MutableList<SearchQuality> = mutableListOf(),
    var contentFilters: MutableList<SearchContentFilter> = mutableListOf()
)

enum class SearchContentFilter(
    @param:StringRes val label: Int,
    val explicit: Boolean
) {
    CLEAN(R.string.label_clean, false),
    EXPLICIT(R.string.label_explicit, true),
}

enum class SearchQuality(
    @param:StringRes val label: Int,
    val format: String,
    val icon: ImageVector
) {
    HIRES(R.string.quality_label_hires, "HIRES_LOSSLESS", Icons.Outlined.HighQuality),
    LOSSLESS(R.string.quality_label_lossless, "LOSSLESS", Icons.Outlined.Hd),
    ATMOS(R.string.label_dolby_atmos, "DOLBY_ATMOS", Icons.Outlined.SurroundSound),
}

sealed class SearchStatus {
    data object Empty : SearchStatus()
    data object Ready : SearchStatus()
    data object Loading : SearchStatus()
    data object Success : SearchStatus()
    data object NoResults : SearchStatus()
    data object Error : SearchStatus()
}