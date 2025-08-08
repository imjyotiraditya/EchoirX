package app.echoirx.presentation.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.echoirx.R
import app.echoirx.domain.model.FileNamingFormat
import app.echoirx.presentation.components.ThumbSwitch
import app.echoirx.presentation.components.preferences.PreferencePosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileNamingBottomSheet(
    selectedFormat: FileNamingFormat,
    includeTrackNumber: Boolean,
    onSelectFormat: (FileNamingFormat) -> Unit,
    onToggleTrackNumber: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    val formats = remember { FileNamingFormat.entries }

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
                    text = stringResource(R.string.title_file_naming_format),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(formats) { index, format ->
                    val isSelected = format == selectedFormat
                    val position = when {
                        formats.size == 1 -> PreferencePosition.Single
                        index == 0 -> PreferencePosition.Top
                        index == formats.size - 1 -> PreferencePosition.Bottom
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
                            .clickable { onSelectFormat(format) },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        headlineContent = {
                            Text(
                                text = stringResource(format.displayNameResId),
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
                                imageVector = format.icon,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectFormat(format) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    )
                }
            }

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(MaterialTheme.shapes.extraLarge),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.title_include_track_number),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.FormatListNumbered,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    ThumbSwitch(
                        checked = includeTrackNumber,
                        onCheckedChange = onToggleTrackNumber
                    )
                }
            )
        }
    }
}