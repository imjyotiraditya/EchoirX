package app.echoirx.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.echoirx.domain.model.CurrentMediaInfo

@Composable
fun CurrentMediaButton(
    currentMedia: CurrentMediaInfo?,
    hasPermission: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = when {
                !hasPermission -> MaterialTheme.colorScheme.errorContainer
                currentMedia?.isPlaying == true -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                !hasPermission -> MaterialTheme.colorScheme.onErrorContainer
                currentMedia?.isPlaying == true -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = when {
                !hasPermission -> Icons.Outlined.Settings
                currentMedia?.isPlaying == true -> Icons.Outlined.MusicNote
                else -> Icons.Outlined.MusicOff
            },
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}