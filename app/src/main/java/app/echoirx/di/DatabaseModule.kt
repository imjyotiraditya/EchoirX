package app.echoirx.di

import android.content.Context
import androidx.room.Room
import app.echoirx.data.local.AppDatabase
import app.echoirx.data.local.Migrations
import app.echoirx.data.local.dao.DownloadDao
import app.echoirx.data.local.dao.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    )
        .addMigrations(Migrations.MIGRATION_4_5)
        .fallbackToDestructiveMigration(false)
        .build()

    @Provides
    @Singleton
    fun provideDownloadDao(database: AppDatabase): DownloadDao = database.downloadDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao =
        database.searchHistoryDao()
}