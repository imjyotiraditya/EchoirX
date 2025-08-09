package app.echoirx.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "downloads")
data class Download(
    @PrimaryKey
    val downloadId: String = UUID.randomUUID().toString(),
    val searchItem: SearchItem,
    val quality: String,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val filePath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val albumDirectory: String? = null,
    val albumId: Long? = null,
    val albumTitle: String? = null
)