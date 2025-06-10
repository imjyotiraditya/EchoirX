package app.echoirx.di

import android.content.Context
import app.echoirx.data.local.dao.DownloadDao
import app.echoirx.data.local.dao.SearchHistoryDao
import app.echoirx.data.media.AudioPreviewPlayer
import app.echoirx.data.media.FFmpegProcessor
import app.echoirx.data.media.MediaSessionManager
import app.echoirx.data.media.MetadataManager
import app.echoirx.data.notification.DownloadNotificationManager
import app.echoirx.data.permission.PermissionsManager
import app.echoirx.data.remote.api.ApiService
import app.echoirx.data.repository.DownloadRepositoryImpl
import app.echoirx.data.repository.SearchHistoryRepositoryImpl
import app.echoirx.data.repository.SearchRepositoryImpl
import app.echoirx.data.repository.SettingsRepositoryImpl
import app.echoirx.domain.repository.DownloadRepository
import app.echoirx.domain.repository.SearchHistoryRepository
import app.echoirx.domain.repository.SearchRepository
import app.echoirx.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSearchRepository(
        apiService: ApiService
    ): SearchRepository = SearchRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideSearchHistoryRepository(
        searchHistoryDao: SearchHistoryDao
    ): SearchHistoryRepository = SearchHistoryRepositoryImpl(searchHistoryDao)

    @Provides
    @Singleton
    fun provideFFmpegProcessor(): FFmpegProcessor = FFmpegProcessor()

    @Provides
    @Singleton
    fun provideMetadataManager(
        apiService: ApiService,
    ): MetadataManager = MetadataManager(apiService)

    @Provides
    @Singleton
    fun providePermissionsManager(
        @ApplicationContext context: Context
    ): PermissionsManager = PermissionsManager(context)

    @Provides
    @Singleton
    fun provideMediaSessionManager(
        @ApplicationContext context: Context
    ): MediaSessionManager = MediaSessionManager(context)

    @Provides
    @Singleton
    fun provideDownloadNotificationManager(
        @ApplicationContext context: Context
    ): DownloadNotificationManager = DownloadNotificationManager(context)

    @Provides
    @Singleton
    fun provideDownloadRepository(
        apiService: ApiService,
        downloadDao: DownloadDao,
        settingsRepository: SettingsRepository,
        ffmpegProcessor: FFmpegProcessor,
        metadataManager: MetadataManager,
        @ApplicationContext context: Context
    ): DownloadRepository = DownloadRepositoryImpl(
        apiService = apiService,
        downloadDao = downloadDao,
        settingsRepository = settingsRepository,
        ffmpegProcessor = ffmpegProcessor,
        metadataManager = metadataManager,
        context = context
    )

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository = SettingsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideAudioPreviewPlayer(): AudioPreviewPlayer = AudioPreviewPlayer()
}