package app.echoirx.presentation.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.data.utils.extensions.getFileSize
import app.echoirx.domain.model.Download
import app.echoirx.domain.model.DownloadStatus
import app.echoirx.domain.model.QualityConfig
import app.echoirx.presentation.components.TrackCover
import app.echoirx.presentation.components.preferences.PreferencePosition
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadItem(
    modifier: Modifier = Modifier,
    download: Download,
    position: PreferencePosition = PreferencePosition.Single,
    onCancel: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val qualityText = when (download.quality) {
        "HI_RES_LOSSLESS" -> stringResource(QualityConfig.HiRes.label)
        "LOSSLESS" -> stringResource(QualityConfig.Lossless.label)
        "DOLBY_ATMOS_AC3" -> stringResource(QualityConfig.DolbyAtmosAC3.label)
        "DOLBY_ATMOS_AC4" -> stringResource(QualityConfig.DolbyAtmosAC4.label)
        "HIGH" -> stringResource(QualityConfig.AAC320.label)
        "LOW" -> stringResource(QualityConfig.AAC96.label)
        else -> stringResource(R.string.label_unknown)
    }.uppercase(Locale.getDefault())

    val shape = when (position) {
        PreferencePosition.Single -> MaterialTheme.shapes.large
        PreferencePosition.Top -> MaterialTheme.shapes.large.copy(
            bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
            bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd
        )

        PreferencePosition.Bottom -> MaterialTheme.shapes.large.copy(
            topStart = MaterialTheme.shapes.extraSmall.topStart,
            topEnd = MaterialTheme.shapes.extraSmall.topEnd
        )

        PreferencePosition.Middle -> MaterialTheme.shapes.extraSmall
    }

    val isDownloading =
        download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.MERGING

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (onClick != null && !isDownloading) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = shape
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TrackCover(
                    url = download.searchItem.cover,
                    size = 60.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = qualityText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = download.searchItem.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = download.searchItem.artists.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (download.status) {
                        DownloadStatus.QUEUED -> {
                            Text(
                                text = stringResource(R.string.label_queued),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DownloadStatus.DOWNLOADING, DownloadStatus.MERGING -> {
                            if (onCancel != null) {
                                OutlinedButton(
                                    onClick = onCancel,
                                    modifier = Modifier.size(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = stringResource(R.string.action_cancel),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        DownloadStatus.COMPLETED -> {
                            Text(
                                text = download.filePath.getFileSize(context),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DownloadStatus.FAILED -> {
                            Icon(
                                imageVector = Icons.Outlined.Error,
                                contentDescription = stringResource(R.string.cd_error),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        DownloadStatus.DELETED -> {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.cd_delete),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Text(
                        text = download.searchItem.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (download.searchItem.explicit) {
                        Icon(
                            painter = painterResource(R.drawable.ic_explicit),
                            contentDescription = stringResource(R.string.cd_explicit_content),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isDownloading,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (download.status == DownloadStatus.MERGING) {
                        val stroke = Stroke(
                            width = with(LocalDensity.current) { 4.dp.toPx() }
                        )
                        LinearWavyProgressIndicator(
                            modifier = Modifier.weight(1f),
                            stroke = stroke,
                            trackStroke = stroke,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { download.progress / 100f },
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }

                    Text(
                        text = if (download.status == DownloadStatus.MERGING) {
                            "100%"
                        } else {
                            "${download.progress}%"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}