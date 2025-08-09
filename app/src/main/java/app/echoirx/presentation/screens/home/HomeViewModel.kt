package app.echoirx.presentation.screens.home

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import app.echoirx.domain.model.Download
import app.echoirx.domain.model.DownloadRequest
import app.echoirx.domain.model.DownloadStatus
import app.echoirx.domain.model.QualityConfig
import app.echoirx.domain.repository.DownloadRepository
import app.echoirx.domain.usecase.ProcessDownloadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val processDownloadUseCase: ProcessDownloadUseCase,
    private val workManager: WorkManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            downloadRepository.getActiveDownloads()
                .combine(downloadRepository.getDownloadHistory()) { active, history ->
                    HomeState(
                        activeDownloads = active,
                        downloadHistory = history,
                        isLoading = false
                    )
                }
                .catch { error ->
                    _state.update {
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
                .collect { newState ->
                    _state.update { newState }
                }
        }
    }

    fun cancelDownload(download: Download): Boolean {
        return try {
            viewModelScope.launch {
                workManager.cancelUniqueWork("download_${download.downloadId}")
                downloadRepository.updateDownloadStatus(download.downloadId, DownloadStatus.FAILED)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun deleteDownload(download: Download): Boolean {
        return try {
            viewModelScope.launch {
                downloadRepository.deleteDownload(download)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun deleteFile(download: Download): Boolean {
        if (download.filePath.isNullOrEmpty()) return false

        return try {
            val path = download.filePath
            val fileDeleted = if (path.startsWith("content://")) {
                val uri = path.toUri()
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                documentFile?.exists() == true && documentFile.delete()
            } else {
                val file = File(path)
                file.exists() && file.delete()
            }

            if (fileDeleted) {
                viewModelScope.launch {
                    downloadRepository.updateDownloadStatus(
                        download.downloadId,
                        DownloadStatus.DELETED
                    )
                    downloadRepository.updateDownloadFilePath(download.downloadId, "")
                }
            }

            fileDeleted
        } catch (_: Exception) {
            false
        }
    }

    fun retryDownload(download: Download): Boolean {
        return try {
            viewModelScope.launch {
                downloadRepository.deleteDownload(download)

                val qualityConfig = when (download.quality) {
                    "HI_RES_LOSSLESS" -> QualityConfig.HiRes
                    "LOSSLESS" -> QualityConfig.Lossless
                    "DOLBY_ATMOS_AC3" -> QualityConfig.DolbyAtmosAC3
                    "DOLBY_ATMOS_AC4" -> QualityConfig.DolbyAtmosAC4
                    "HIGH" -> QualityConfig.AAC320
                    "LOW" -> QualityConfig.AAC96
                    else -> QualityConfig.AAC320
                }

                processDownloadUseCase(
                    DownloadRequest.Track(
                        track = download.searchItem,
                        config = qualityConfig
                    )
                )
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shareFile(download: Download): Intent? {
        if (download.filePath.isNullOrEmpty()) return null

        return try {
            val uri = if (download.filePath.startsWith("content://")) {
                download.filePath.toUri()
            } else {
                val file = File(download.filePath)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }

            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "audio/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (_: Exception) {
            null
        }
    }
}