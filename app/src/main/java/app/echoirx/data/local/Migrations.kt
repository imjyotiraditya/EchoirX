package app.echoirx.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE downloads_new (
                    downloadId TEXT NOT NULL PRIMARY KEY,
                    searchItem TEXT NOT NULL,
                    quality TEXT NOT NULL,
                    progress INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    filePath TEXT,
                    timestamp INTEGER NOT NULL,
                    albumDirectory TEXT,
                    albumId INTEGER,
                    albumTitle TEXT
                )
            """
            )

            db.execSQL(
                """
                INSERT INTO downloads_new (
                    downloadId, searchItem, quality, progress, status, 
                    filePath, timestamp, albumDirectory, albumId, albumTitle
                )
                SELECT 
                    downloadId, searchResult, quality, progress, status,
                    filePath, timestamp, albumDirectory, albumId, albumTitle
                FROM downloads
            """
            )

            db.execSQL("DROP TABLE downloads")
            db.execSQL("ALTER TABLE downloads_new RENAME TO downloads")
        }
    }
}