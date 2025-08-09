package app.echoirx.data.local.converter

import androidx.room.TypeConverter
import app.echoirx.domain.model.DownloadStatus
import app.echoirx.domain.model.SearchItem
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String = value.name

    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)

    @TypeConverter
    fun fromSearchResult(value: SearchItem): String = json.encodeToString(value)

    @TypeConverter
    fun toSearchResult(value: String): SearchItem = json.decodeFromString(value)
}