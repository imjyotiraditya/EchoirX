package app.echoirx.data.remote.mapper

import app.echoirx.data.remote.dto.SearchItemDto
import app.echoirx.domain.model.SearchItem

object SearchItemMapper {
    fun SearchItemDto.toDomain(): SearchItem =
        SearchItem(
            id = id,
            title = title,
            duration = duration,
            explicit = explicit,
            cover = cover,
            artists = artists,
            modes = modes,
            formats = formats,
            trackNumber = trackNumber
        )
}