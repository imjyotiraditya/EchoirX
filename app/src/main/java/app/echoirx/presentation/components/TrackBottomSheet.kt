package app.echoirx.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.domain.model.QualityConfig
import app.echoirx.domain.model.SearchItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TrackBottomSheet(
    modifier: Modifier = Modifier,
    track: SearchItem,
    onDownload: (QualityConfig) -> Unit,
    onPreviewClick: () -> Unit = {},
    isPreviewPlaying: Boolean = false,
    showPreviewButton: Boolean = false,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.large
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        TrackCover(
                            url = track.cover?.replace("80x80", "160x160"),
                            size = 72.dp
                        )

                        if (showPreviewButton) {
                            PreviewButton(
                                onPreviewClick = onPreviewClick,
                                isPlaying = isPreviewPlaying,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 8.dp, y = 8.dp)
                                    .size(32.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = track.artists.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (track.formats?.let { "DOLBY_ATMOS" in it } == true) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_dolby),
                                    contentDescription = stringResource(R.string.cd_dolby_atmos),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (track.explicit) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_explicit),
                                    contentDescription = stringResource(R.string.cd_explicit_content),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = track.duration,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                HorizontalDivider()

                DownloadOptions(
                    formats = track.formats,
                    modes = track.modes,
                    onOptionSelected = { config ->
                        onDownload(config)
                        onDismiss()
                    }
                )
            }
        }
    }
}