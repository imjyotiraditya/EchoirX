package app.echoirx.presentation.screens.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.echoirx.R
import app.echoirx.data.utils.extensions.showSnackbar
import app.echoirx.domain.model.SearchResult
import app.echoirx.presentation.components.DownloadOptions
import app.echoirx.presentation.components.TrackBottomSheet
import app.echoirx.presentation.components.TrackCover

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DetailsScreen(
    result: SearchResult,
    viewModel: DetailsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTrack by remember { mutableStateOf<SearchResult?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsState()

    LaunchedEffect(result) {
        viewModel.initializeWithItem(result)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TrackCover(
                    url = result.cover?.replace("80x80", "160x160"),
                    size = 72.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = result.artists.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (result.formats?.let { "DOLBY_ATMOS" in it } == true) {
                            Icon(
                                painter = painterResource(R.drawable.ic_dolby),
                                contentDescription = stringResource(R.string.cd_dolby_atmos),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (result.explicit) {
                            Icon(
                                painter = painterResource(R.drawable.ic_explicit),
                                contentDescription = stringResource(R.string.cd_explicit_content),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = result.duration,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            DownloadOptions(
                formats = result.formats,
                modes = result.modes,
                onOptionSelected = { config ->
                    viewModel.downloadAlbum(config)
                    snackbarHostState.showSnackbar(
                        scope = coroutineScope,
                        message = context.getString(
                            R.string.msg_download_started,
                            context.getString(config.label)
                        )
                    )
                }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                    }
                }

                state.error != null -> {
                    Text(
                        text = state.error ?: stringResource(R.string.msg_unknown_error),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                state.tracks.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = state.tracks,
                            key = { index, track -> "album_track_${track.id}_$index" }
                        ) { index, track ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = track.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = track.artists.joinToString(", "),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingContent = {
                                    Text(
                                        text = String.format(
                                            java.util.Locale.getDefault(),
                                            "%02d",
                                            index + 1
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = track.duration,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            if (track.explicit) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_explicit),
                                                    contentDescription = stringResource(R.string.cd_explicit_content),
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }

                                        Icon(
                                            painter = painterResource(R.drawable.ic_download),
                                            contentDescription = stringResource(R.string.cd_download_button),
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTrack = track
                                        showBottomSheet = true
                                    },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet && selectedTrack != null) {
        TrackBottomSheet(
            track = selectedTrack!!,
            onDownload = { config ->
                viewModel.downloadTrack(selectedTrack!!, config)
                snackbarHostState.showSnackbar(
                    scope = coroutineScope,
                    message = context.getString(
                        R.string.msg_download_started,
                        context.getString(config.label)
                    )
                )
            },
            onPreviewClick = {
                if (isPreviewPlaying) {
                    viewModel.stopTrackPreview()
                } else {
                    viewModel.playTrackPreview(selectedTrack!!.id)
                }
            },
            isPreviewPlaying = isPreviewPlaying,
            showPreviewButton = true,
            onDismiss = {
                viewModel.stopTrackPreview()
                showBottomSheet = false
            }
        )
    }
}