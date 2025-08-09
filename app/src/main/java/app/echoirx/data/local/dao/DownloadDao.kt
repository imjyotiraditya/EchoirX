package app.echoirx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.echoirx.domain.model.Download
import app.echoirx.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE status IN (:statuses) ORDER BY timestamp DESC")
    fun getDownloadsByStatus(statuses: List<DownloadStatus>): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE albumId = :albumId")
    suspend fun getDownloadsByAlbumId(albumId: Long): List<Download>

    @Query("SELECT * FROM downloads WHERE json_extract(searchItem, '$.id') = :trackId")
    suspend fun getDownloadsByTrackId(trackId: Long): List<Download>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: Download)

    @Update
    suspend fun update(download: Download)

    @Delete
    suspend fun delete(download: Download)

    @Query("UPDATE downloads SET progress = :progress WHERE downloadId = :downloadId")
    suspend fun updateProgress(downloadId: String, progress: Int)

    @Query("UPDATE downloads SET status = :status WHERE downloadId = :downloadId")
    suspend fun updateStatus(downloadId: String, status: DownloadStatus)

    @Query("UPDATE downloads SET filePath = :filePath WHERE downloadId = :downloadId")
    suspend fun updateFilePath(downloadId: String, filePath: String)

    @Query("SELECT * FROM downloads WHERE downloadId = :downloadId")
    suspend fun getDownloadById(downloadId: String): Download?

    @Query("DELETE FROM downloads WHERE status = :status")
    suspend fun deleteByStatus(status: DownloadStatus)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()
}