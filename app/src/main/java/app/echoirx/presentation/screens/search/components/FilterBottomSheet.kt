package app.echoirx.presentation.screens.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.presentation.components.preferences.PreferencePosition
import app.echoirx.presentation.screens.search.SearchContentFilter
import app.echoirx.presentation.screens.search.SearchFilter
import app.echoirx.presentation.screens.search.SearchQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: SearchFilter,
    onQualityFilterAdded: (SearchQuality) -> Unit,
    onQualityFilterRemoved: (SearchQuality) -> Unit,
    onContentFilterAdded: (SearchContentFilter) -> Unit,
    onContentFilterRemoved: (SearchContentFilter) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.title_filter_options),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(SearchQuality.entries) { quality ->
                    var isSelected by remember {
                        mutableStateOf(currentFilter.qualities.contains(quality))
                    }

                    val position = when {
                        SearchQuality.entries.size == 1 -> PreferencePosition.Single
                        quality == SearchQuality.entries.first() -> PreferencePosition.Top
                        quality == SearchQuality.entries.last() -> PreferencePosition.Bottom
                        else -> PreferencePosition.Middle
                    }

                    val shape = when (position) {
                        PreferencePosition.Single -> MaterialTheme.shapes.extraLarge
                        PreferencePosition.Top -> MaterialTheme.shapes.extraLarge.copy(
                            bottomStart = MaterialTheme.shapes.small.bottomStart,
                            bottomEnd = MaterialTheme.shapes.small.bottomEnd
                        )

                        PreferencePosition.Bottom -> MaterialTheme.shapes.extraLarge.copy(
                            topStart = MaterialTheme.shapes.small.topStart,
                            topEnd = MaterialTheme.shapes.small.topEnd
                        )

                        PreferencePosition.Middle -> MaterialTheme.shapes.small
                    }

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(shape)
                            .clickable {
                                isSelected = !isSelected
                                if (isSelected) {
                                    onQualityFilterAdded(quality)
                                } else {
                                    onQualityFilterRemoved(quality)
                                }
                            },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        headlineContent = {
                            Text(
                                text = stringResource(quality.label),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = quality.icon,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchContentFilter.entries.forEach { contentFilter ->
                    var selected by remember {
                        mutableStateOf(currentFilter.contentFilters.contains(contentFilter))
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.large)
                            .clickable {
                                selected = !selected
                                if (selected) {
                                    onContentFilterAdded(contentFilter)
                                } else {
                                    onContentFilterRemoved(contentFilter)
                                }
                            },
                        color = if (selected)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = stringResource(contentFilter.label),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}