package app.echoirx.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.echoirx.R
import app.echoirx.domain.model.Download
import app.echoirx.domain.model.DownloadStatus
import app.echoirx.presentation.components.models.ChipAction
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DownloadBottomSheet(
    download: Download,
    onOpenFile: () -> Unit,
    onDeleteFile: () -> Unit,
    onDeleteFromHistory: () -> Unit,
    onShareFile: () -> Unit,
    onRetryDownload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    val fileExists = remember(download.filePath) {
        if (download.filePath.isNullOrEmpty()) {
            false
        } else {
            try {
                if (download.filePath.startsWith("content://")) {
                    val uri = download.filePath.toUri()
                    val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.use {
                        it.statSize
                    } ?: 0L
                    fileSize > 0
                } else {
                    val file = File(download.filePath)
                    file.exists() && file.length() > 0
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    val isCompleted = download.status == DownloadStatus.COMPLETED && fileExists
    val isFailed = download.status == DownloadStatus.FAILED
    val isDeleted = download.status == DownloadStatus.DELETED
    val shouldShowFileOptions = isCompleted && fileExists

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = download.searchResult.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = download.searchResult.artists.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (download.searchResult.explicit) {
                            Icon(
                                painter = painterResource(R.drawable.ic_explicit),
                                contentDescription = stringResource(R.string.cd_explicit_content),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val statusText = when (download.status) {
                            DownloadStatus.COMPLETED -> if (fileExists) {
                                stringResource(R.string.label_completed)
                            } else {
                                stringResource(R.string.label_file_missing)
                            }

                            DownloadStatus.FAILED -> stringResource(R.string.label_failed)
                            DownloadStatus.DELETED -> stringResource(R.string.label_deleted)
                            else -> download.status.name
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                download.status == DownloadStatus.COMPLETED && fileExists ->
                                    MaterialTheme.colorScheme.primary

                                download.status == DownloadStatus.FAILED ||
                                        download.status == DownloadStatus.DELETED ||
                                        (download.status == DownloadStatus.COMPLETED && !fileExists) ->
                                    MaterialTheme.colorScheme.error

                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                TrackCover(
                    url = download.searchResult.cover?.replace("80x80", "160x160"),
                    size = 80.dp
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val actions = buildList {
                    if (shouldShowFileOptions) {
                        add(
                            ChipAction(
                                label = stringResource(R.string.action_open_in_player),
                                icon = Icons.Outlined.PlayArrow,
                                contentDescription = stringResource(R.string.cd_play),
                                onClick = onOpenFile
                            )
                        )

                        add(
                            ChipAction(
                                label = stringResource(R.string.action_delete_file),
                                icon = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.cd_delete),
                                onClick = onDeleteFile
                            )
                        )

                        add(
                            ChipAction(
                                label = stringResource(R.string.action_share),
                                icon = Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.cd_share),
                                onClick = onShareFile
                            )
                        )
                    }

                    if (isFailed || isDeleted) {
                        add(
                            ChipAction(
                                label = stringResource(R.string.action_retry_download),
                                icon = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.cd_retry),
                                onClick = onRetryDownload
                            )
                        )
                    }

                    add(
                        ChipAction(
                            label = stringResource(R.string.action_delete_from_history),
                            icon = Icons.Outlined.History,
                            contentDescription = stringResource(R.string.cd_history),
                            onClick = onDeleteFromHistory
                        )
                    )
                }

                actions.forEach { action ->
                    AssistChip(
                        onClick = action.onClick,
                        label = {
                            Text(
                                text = action.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }
        }
    }
}