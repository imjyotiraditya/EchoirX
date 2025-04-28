package app.echoirx.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.echoirx.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewButton(
    onPreviewClick: () -> Unit,
    isPlaying: Boolean,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AssistChip(
            onClick = onPreviewClick,
            label = {
                Text(
                    text = stringResource(
                        if (isPlaying) R.string.action_stop_preview
                        else R.string.action_play_preview
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = if (isPlaying)
                        Icons.Outlined.Stop
                    else
                        Icons.Outlined.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.cd_stop_preview
                        else R.string.cd_play_preview
                    ),
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            }
        )
    }
}