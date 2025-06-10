package app.echoirx.domain.model

data class CurrentMediaInfo(
    val title: String,
    val artist: String,
    val album: String,
    val packageName: String,
    val isPlaying: Boolean
) {
    fun getSearchQuery(): String {
        return when {
            title.isNotEmpty() && artist.isNotEmpty() -> "$artist $title"
            title.isNotEmpty() -> title
            artist.isNotEmpty() -> artist
            else -> ""
        }
    }

    fun getDisplayText(): String {
        return when {
            title.isNotEmpty() && artist.isNotEmpty() -> "$title - $artist"
            title.isNotEmpty() -> title
            artist.isNotEmpty() -> artist
            else -> "Unknown"
        }
    }
}