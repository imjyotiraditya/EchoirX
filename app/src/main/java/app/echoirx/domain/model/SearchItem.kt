package app.echoirx.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class SearchItem(
    val id: Long,
    val title: String,
    val duration: String,
    val explicit: Boolean,
    val cover: String?,
    val artists: List<String>,
    val modes: List<String>?,
    val formats: List<String>?,
    val trackNumber: Int? = null
) : Parcelable