package app.echoirx.domain.model

data class AlbumDownloadContext(
    val id: Long,
    val title: String,
    val directory: String,
    val isExplicit: Boolean
)

sealed class DownloadRequest {
    data class Track(
        val track: SearchItem,
        val config: QualityConfig
    ) : DownloadRequest()

    data class Album(
        val album: SearchItem,
        val tracks: List<SearchItem>,
        val config: QualityConfig,
        val downloadContext: AlbumDownloadContext
    ) : DownloadRequest()
}