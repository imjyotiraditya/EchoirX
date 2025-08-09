package app.echoirx.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.echoirx.data.local.converter.Converters
import app.echoirx.data.local.dao.DownloadDao
import app.echoirx.data.local.dao.SearchHistoryDao
import app.echoirx.domain.model.Download
import app.echoirx.domain.model.SearchHistoryItem

@Database(
    entities = [Download::class, SearchHistoryItem::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        const val DATABASE_NAME = "echoir_db"
    }
}