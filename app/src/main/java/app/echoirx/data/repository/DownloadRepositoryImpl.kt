package app.echoirx.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import app.echoirx.data.local.dao.DownloadDao
import app.echoirx.data.media.FFmpegProcessor
import app.echoirx.data.media.MetadataManager
import app.echoirx.data.remote.api.ApiService
import app.echoirx.data.remote.mapper.PlaybackMapper.toDomain
import app.echoirx.domain.model.Download
import app.echoirx.domain.model.DownloadStatus
import app.echoirx.domain.model.PlaybackResponse
import app.echoirx.domain.repository.DownloadRepository
import app.echoirx.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val downloadDao: DownloadDao,
    private val settingsRepository: SettingsRepository,
    private val ffmpegProcessor: FFmpegProcessor,
    private val metadataManager: MetadataManager,
    @param:ApplicationContext private val context: Context
) : DownloadRepository {

    companion object {
        private const val TAG = "DownloadRepository"
    }

    override suspend fun createAlbumDirectory(albumTitle: String, explicit: Boolean): String {
        return withContext(Dispatchers.IO) {
            when (val customDir = settingsRepository.getOutputDirectory()) {
                null -> {
                    val musicDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    val echoirDir = File(musicDir, "Echoir").apply { mkdirs() }
                    val albumDirName = if (explicit) "$albumTitle (E)" else albumTitle
                    val safeDirName = getNextAvailableName(sanitizeFileName(albumDirName)) {
                        File(echoirDir, it).exists()
                    }
                    File(echoirDir, safeDirName).apply { mkdirs() }.absolutePath
                }

                else -> {
                    val uri = customDir.toUri()
                    val directory = DocumentFile.fromTreeUri(context, uri)
                        ?: throw IOException("Could not access directory")

                    val albumDirName = if (explicit) "$albumTitle (E)" else albumTitle
                    val safeDirName = getNextAvailableName(sanitizeFileName(albumDirName)) {
                        directory.findFile(it) != null
                    }
                    val albumDir = directory.createDirectory(safeDirName)
                        ?: throw IOException("Could not create album directory")

                    albumDir.uri.toString()
                }
            }
        }
    }

    override suspend fun processDownload(
        downloadId: String,
        trackId: Long,
        quality: String,
        modes: List<String>?,
        onProgress: suspend (Int) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(
                TAG,
                "Starting download for track: $trackId with quality: $quality and modes: $modes"
            )

            val (playbackInfo, metadata) = getDownloadInfo(trackId, quality, modes)

            updateDownloadStatus(downloadId, DownloadStatus.DOWNLOADING)

            // Get cache file path for final processing
            val uniqueUUID = UUID.randomUUID().toString()
            val extension = if (playbackInfo.codec == "flac") "flac" else "m4a"
            val cacheFile = File(context.cacheDir, "${trackId}_cache_$uniqueUUID.$extension")

            // Handle single vs multiple parts
            if (playbackInfo.urls.size == 1) {
                downloadSingleFile(playbackInfo.urls.first(), cacheFile)
                onProgress(100)
            } else {
                downloadMultipleFiles(playbackInfo.urls, cacheFile, onProgress)
            }

            // Process with FFmpeg if needed
            if (playbackInfo.urls.size > 1) {
                updateDownloadStatus(downloadId, DownloadStatus.MERGING)
                processDownloadedFile(cacheFile, extension)
            }

            // Embed metadata while file is still in cache
            metadataManager.embedMetadata(cacheFile.absolutePath, metadata)

            // Get download info and generate filename
            val download =
                getDownloadById(downloadId) ?: throw IOException("Download info not found")

            // Generate safe filename
            val safeFileName = generateFileName(download)

            // Move to final location
            val finalPath = when (val customDir = settingsRepository.getOutputDirectory()) {
                null -> {
                    // Standard file system approach
                    val musicDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    val echoirDir = File(musicDir, "Echoir").apply { mkdirs() }

                    val targetDir = when {
                        download.albumDirectory != null -> {
                            File(download.albumDirectory)
                        }

                        else -> echoirDir
                    }

                    val finalFileName = getNextAvailableName(safeFileName) { name ->
                        File(targetDir, "$name.$extension").exists()
                    }
                    val finalFile = File(targetDir, "$finalFileName.$extension")

                    cacheFile.copyTo(finalFile, overwrite = false)

                    // Handle extras based on settings
                    val saveCoverArt = settingsRepository.getSaveCoverArt()
                    val saveLyrics = settingsRepository.getSaveLyrics()

                    // Save cover art if enabled
                    if (saveCoverArt && download.searchItem.cover != null) {
                        try {
                            val coverFilePath = "${targetDir.absolutePath}/${finalFileName}.jpg"
                            if (metadataManager.extractAndSaveCoverArt(
                                    download.searchItem.cover.replace(
                                        "80x80",
                                        "1280x1280"
                                    ),
                                    coverFilePath
                                )
                            ) {
                                // Also scan the cover art file
                                scanMedia(coverFilePath, "image/jpeg")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save cover art as file", e)
                            // Continue with the download even if saving cover art fails
                        }
                    }

                    // Save lyrics if enabled
                    if (saveLyrics) {
                        try {
                            val lyricsFilePath = "${targetDir.absolutePath}/${finalFileName}.lrc"
                            metadataManager.extractAndSaveLyrics(metadata, lyricsFilePath)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save lyrics as file", e)
                            // Continue with the download even if saving lyrics fails
                        }
                    }

                    scanMedia(finalFile.absolutePath, "audio/*")
                    finalFile.absolutePath
                }

                else -> {
                    // Storage Access Framework approach
                    val uri = customDir.toUri()
                    val directory = DocumentFile.fromTreeUri(context, uri)
                        ?: throw IOException("Could not access directory")

                    val targetDir = when {
                        download.albumDirectory != null -> {
                            DocumentFile.fromTreeUri(context, download.albumDirectory.toUri())
                                ?: throw IOException("Could not access album directory")
                        }

                        else -> directory
                    }

                    val finalFileName = getNextAvailableName(safeFileName) { name ->
                        targetDir.findFile("$name.$extension") != null
                    }

                    // Create audio file
                    val finalFile = targetDir.createFile("audio/*", "$finalFileName.$extension")
                        ?: throw IOException("Could not create file")

                    context.contentResolver.openOutputStream(finalFile.uri)?.use { output ->
                        cacheFile.inputStream().use { it.copyTo(output) }
                    } ?: throw IOException("Could not open output stream")

                    // Handle extras based on settings
                    val saveCoverArt = settingsRepository.getSaveCoverArt()
                    val saveLyrics = settingsRepository.getSaveLyrics()

                    // Save cover art if enabled
                    if (saveCoverArt && download.searchItem.cover != null) {
                        try {
                            val coverImageData = metadataManager.downloadCoverArt(
                                download.searchItem.cover.replace(
                                    "80x80",
                                    "1280x1280"
                                )
                            )
                            if (coverImageData != null) {
                                val coverFile =
                                    targetDir.createFile("image/jpeg", "$finalFileName.jpg")
                                coverFile?.let {
                                    context.contentResolver.openOutputStream(it.uri)
                                        ?.use { output ->
                                            output.write(coverImageData)
                                        }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save cover art with SAF", e)
                            // Continue with the download even if saving cover art fails
                        }
                    }

                    // Save lyrics if enabled
                    if (saveLyrics) {
                        try {
                            val lyrics = metadataManager.extractLyrics(metadata)
                            if (lyrics != null) {
                                val lyricsFile =
                                    targetDir.createFile(
                                        "application/octet-stream",
                                        "$finalFileName.lrc"
                                    )
                                lyricsFile?.let {
                                    context.contentResolver.openOutputStream(it.uri)
                                        ?.use { output ->
                                            output.write(lyrics.toByteArray())
                                        }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save lyrics with SAF", e)
                            // Continue with the download even if saving lyrics fails
                        }
                    }

                    finalFile.uri.toString()
                }
            }

            // Clean up cache
            cacheFile.delete()

            // Update status
            updateDownloadStatus(downloadId, DownloadStatus.COMPLETED)
            updateDownloadProgress(downloadId, 100)
            updateDownloadFilePath(downloadId, finalPath)

            finalPath
        }.onFailure { error ->
            Log.e(TAG, "Download process failed", error)
            updateDownloadStatus(downloadId, DownloadStatus.FAILED)
        }
    }

    private suspend fun downloadSingleFile(url: String, outputFile: File) {
        outputFile.outputStream().use {
            apiService.downloadFileToStream(url, it)
        }
    }

    private suspend fun downloadMultipleFiles(
        urls: List<String>,
        outputFile: File,
        onProgress: suspend (Int) -> Unit
    ) = coroutineScope {
        val tempFiles = urls.mapIndexed { index, _ ->
            index to File(context.cacheDir, "${outputFile.nameWithoutExtension}_part_$index.tmp")
        }.toMap()

        val completedIndices = Collections.synchronizedSet(mutableSetOf<Int>())

        val downloadJobs = urls.mapIndexed { index, url ->
            async(Dispatchers.IO) {
                try {
                    val tempFile = tempFiles[index]
                    val response = apiService.downloadFile(url)
                    tempFile?.writeBytes(response)
                    completedIndices.add(index)
                    onProgress((completedIndices.size * 100) / urls.size)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }

        downloadJobs.awaitAll()

        outputFile.outputStream().use { output ->
            for (i in urls.indices) {
                tempFiles[i]?.let { input ->
                    input.inputStream().use { it.copyTo(output) }
                    input.delete()
                }
            }
        }
    }

    private suspend fun processDownloadedFile(file: File, extension: String) {
        val processedFile =
            File(context.cacheDir, "${file.nameWithoutExtension}_processed.$extension")
        ffmpegProcessor.processMergedFile(file.absolutePath, processedFile.absolutePath)
        processedFile.copyTo(file, overwrite = true)
        processedFile.delete()
    }

    private fun sanitizeFileName(title: String): String {
        return title.replace("""[\\/:*?"<>|]""".toRegex(), "")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private suspend fun generateFileName(download: Download): String {
        val format = settingsRepository.getFileNamingFormat()
        val includeTrackNumber = settingsRepository.getIncludeTrackNumber()

        val trackNumber = if (includeTrackNumber) download.searchItem.trackNumber else null

        val fileName = format.format(
            download.searchItem.artists.joinToString(", "),
            download.searchItem.title,
            trackNumber
        )
        return sanitizeFileName(fileName)
    }

    private fun getNextAvailableName(baseName: String, exists: (name: String) -> Boolean): String {
        var index = 0
        var name = baseName
        while (exists(name)) {
            index++
            name = "$baseName ($index)"
        }
        return name
    }

    private suspend fun scanMedia(path: String, mimeType: String = "audio/*") {
        withContext(Dispatchers.Main) {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(path),
                arrayOf(mimeType)
            ) { scannedPath, uri ->
                if (uri != null) {
                    Log.i(TAG, "Media scan completed for: $scannedPath")
                    Log.i(TAG, "Scanned URI: $uri")
                } else {
                    Log.e(TAG, "Media scan failed for: $scannedPath")
                }
            }
        }
    }

    override suspend fun getDownloadInfo(
        trackId: Long,
        quality: String,
        modes: List<String>?
    ): Pair<PlaybackResponse, Map<String, String>> =
        apiService.getDownloadInfo(trackId, quality, modes)
            .let { (playbackDto, metadata) ->
                playbackDto.toDomain() to metadata
            }

    override suspend fun saveDownload(download: Download) =
        downloadDao.insert(download)

    override suspend fun updateDownloadProgress(downloadId: String, progress: Int) =
        downloadDao.updateProgress(downloadId, progress)

    override suspend fun updateDownloadStatus(downloadId: String, status: DownloadStatus) =
        downloadDao.updateStatus(downloadId, status)

    override suspend fun updateDownloadFilePath(downloadId: String, filePath: String) =
        downloadDao.updateFilePath(downloadId, filePath)

    override suspend fun deleteDownload(download: Download) =
        downloadDao.delete(download)

    override suspend fun getDownloadById(downloadId: String): Download? =
        downloadDao.getDownloadById(downloadId)

    override suspend fun getDownloadsByTrackId(trackId: Long): List<Download> =
        downloadDao.getDownloadsByTrackId(trackId)

    override suspend fun getDownloadsByAlbumId(albumId: Long): List<Download> =
        downloadDao.getDownloadsByAlbumId(albumId)

    override fun getActiveDownloads(): Flow<List<Download>> =
        downloadDao.getDownloadsByStatus(
            listOf(
                DownloadStatus.QUEUED,
                DownloadStatus.DOWNLOADING,
                DownloadStatus.MERGING
            )
        )

    override fun getDownloadHistory(): Flow<List<Download>> =
        downloadDao.getDownloadsByStatus(
            listOf(
                DownloadStatus.COMPLETED,
                DownloadStatus.FAILED,
                DownloadStatus.DELETED
            )
        )
}