package app.echoirx.data.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import app.echoirx.domain.model.Download
import app.echoirx.domain.model.QualityConfig
import java.util.concurrent.TimeUnit

fun enqueueDownloadWork(
    workManager: WorkManager,
    download: Download,
    config: QualityConfig,
    queueManager: DownloadQueueManager
) {
    val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            WorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setInputData(
            workDataOf(
                DownloadWorker.KEY_DOWNLOAD_ID to download.downloadId,
                DownloadWorker.KEY_TRACK_ID to download.searchItem.id,
                DownloadWorker.KEY_QUALITY to config.quality
            )
        )
        .build()

    queueManager.incrementActiveDownloads()
    workManager.enqueueUniqueWork(
        "download_${download.downloadId}",
        ExistingWorkPolicy.KEEP,
        workRequest
    )
}