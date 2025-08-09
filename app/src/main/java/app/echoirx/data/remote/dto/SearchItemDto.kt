package app.echoirx.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchItemDto(
    val id: Long,
    val title: String,
    val duration: String,
    val explicit: Boolean,
    val cover: String?,
    val artists: List<String>,
    val modes: List<String>?,
    val formats: List<String>?,
    @SerialName("track_number") val trackNumber: Int? = null
)